package com.jokin.widget.gallery;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;

import java.lang.reflect.Field;

/**
 * A Basic Gallery View
 */
public class CGallery extends CGalleryAbsSpinner implements GestureDetector.OnGestureListener {

	private static final String TAG = CGallery.class.getSimpleName();

	private static final boolean VERB = false;

	/**
	 * Duration in milliseconds from the start of a scroll during which we're
	 * unsure whether the user is scrolling or flinging.
	 */
	private static final int SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT = 250;

	private static final String LOG_TAG = null;

    private boolean mOpMoving;
    private boolean mOpFliping;
    private int mFlipSpeed;
    private int mFlipResultPosition = -1;

	/**
	 * Horizontal spacing between items.
	 */
	private int mSpacing = 0;

	/**
	 * How long the transition animation should run when a child view changes
	 * position, measured in milliseconds.
	 * 切换效果时间，太短会看不到切换过程的渐变效果
	 */
	private int mAnimationDuration = 400;

	/**
	 * The alpha of items that are not selected.
	 */
	private float mUnselectedAlpha;

	private int mGravity;

	/**
	 * Helper for detecting touch gestures.
	 */
	private GestureDetector mGestureDetector;

	/**
	 * The position of the item that received the user's down touch.
	 */
	private int mDownTouchPosition;

	/**
	 * The view of the item that received the user's down touch.
	 */
	private View mDownTouchView;

	/**
	 * Executes the delta scrolls from a fling or scroll movement.
	 */
	private FlingRunnable mFlingRunnable = new FlingRunnable();

	/**
	 * Sets mSuppressSelectionChanged = false. This is used to set it to false
	 * in the future. It will also trigger a selection changed.
	 */
	private Runnable mDisableSuppressSelectionChangedRunnable = new Runnable() {
		public void run() {
			mSuppressSelectionChanged = false;
			selectionChanged();
		}
	};

	/**
	 * When fling runnable runs, it resets this to false. Any method along the
	 * path until the end of its run() can set this to true to abort any
	 * remaining fling. For example, if we've reached either the leftmost or
	 * rightmost item, we will set this to true.
	 */
	private boolean mShouldStopFling;

	/**
	 * The currently selected item's child.
	 */
	private View mSelectedChild;

	/**
	 * Whether to continuously callback on the item selected listener during a
	 * fling.
	 */
	private boolean mShouldCallbackDuringFling = true;

	/**
	 * Whether to callback when an item that is not selected is clicked.
	 */
	private boolean mShouldCallbackOnUnselectedItemClick = true;

	/**
	 * If true, do not callback to item selected listener.
	 */
	private boolean mSuppressSelectionChanged;

	/**
	 * If true, we have received the "invoke" (center or enter buttons) key
	 * down. This is checked before we action on the "invoke" key up, and is
	 * subsequently cleared.
	 */
	private boolean mReceivedInvokeKeyDown;

	private AdapterContextMenuInfo mContextMenuInfo;

	/**
	 * If true, this onScroll is the first for this user's drag (remember, a
	 * drag sends many onScrolls).
	 */
	private boolean mIsFirstScroll;

	/**
	 * If true the reflection calls failed and this widget will behave
	 * unpredictably if used further
	 */
	private boolean mBroken;

	/**
	 * Offset between the center of the selected child view and the center of the Gallery.
	 * Used to reset position correctly during layout.
	 */
	private int mSelectedCenterOffset;

	public CGallery(Context context) {
		this(context, null);
	}

	public CGallery(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.CGalleryStyle);
	}

	public CGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mBroken = true;

		mGestureDetector = new GestureDetector(context, this);
		mGestureDetector.setIsLongpressEnabled(true);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CGallery, defStyle, 0);

		int index = a.getInt(R.styleable.CGallery_gravity, -1);
		if (index >= 0) {
			setGravity(index);
		}

		int animationDuration = a.getInt(R.styleable.CGallery_animationDuration, -1);
		if (animationDuration > 0) {
			setAnimationDuration(animationDuration);
		}

		int spacing = a.getDimensionPixelOffset(R.styleable.CGallery_spacing, 0);
		setSpacing(spacing);

		float unselectedAlpha = a.getFloat(R.styleable.CGallery_unselectedAlpha, 0.5f);
		setUnselectedAlpha(unselectedAlpha);

		a.recycle();


		// We draw the selected item last (because otherwise the item to the
		// right overlaps it)
		int FLAG_USE_CHILD_DRAWING_ORDER = 0x400;
		int FLAG_SUPPORT_STATIC_TRANSFORMATIONS = 0x800;
		Class<ViewGroup> vgClass = ViewGroup.class;

		try {
			Field childDrawingOrder = vgClass.getDeclaredField("FLAG_USE_CHILD_DRAWING_ORDER");
			Field supportStaticTrans = vgClass.getDeclaredField("FLAG_SUPPORT_STATIC_TRANSFORMATIONS");

			childDrawingOrder.setAccessible(true);
			supportStaticTrans.setAccessible(true);

			FLAG_USE_CHILD_DRAWING_ORDER = childDrawingOrder.getInt(this);
			FLAG_SUPPORT_STATIC_TRANSFORMATIONS = supportStaticTrans.getInt(this);
		} catch (NoSuchFieldException e) {
			Log.e(LOG_TAG, "", e);
		} catch (IllegalAccessException e) {
			Log.e(LOG_TAG, "", e);
		}
		try {
			// set new group flags
			Field groupFlags = vgClass.getDeclaredField("mGroupFlags");
			groupFlags.setAccessible(true);
			int groupFlagsValue = groupFlags.getInt(this);

			groupFlagsValue |= FLAG_USE_CHILD_DRAWING_ORDER;
			groupFlagsValue |= FLAG_SUPPORT_STATIC_TRANSFORMATIONS;

			groupFlags.set(this, groupFlagsValue);

			// working!
			mBroken = false;
		} catch (NoSuchFieldException e) {
			Log.e(LOG_TAG, "", e);
		} catch (IllegalAccessException e) {
			Log.e(LOG_TAG, "", e);
		}
	}

	/**
	 * @return Whether the widget is broken or working (functional)
	 */
	public boolean isBroken() {
		return mBroken;
	}

	/**
	 * Whether or not to callback on any {@link #getOnItemSelectedListener()}
	 * while the items are being flinged. If false, only the final selected item
	 * will cause the callback. If true, all items between the first and the
	 * final will cause callbacks.
	 * 
	 * @param shouldCallback
	 *            Whether or not to callback on the listener while the items are
	 *            being flinged.
	 */
	public void setCallbackDuringFling(boolean shouldCallback) {
		mShouldCallbackDuringFling = shouldCallback;
	}

	/**
	 * Whether or not to callback when an item that is not selected is clicked.
	 * If false, the item will become selected (and re-centered). If true, the
	 * {@link #getOnItemClickListener()} will get the callback.
	 * 
	 * @param shouldCallback
	 *            Whether or not to callback on the listener when a item that is
	 *            not selected is clicked.
	 * @hide
	 */
	public void setCallbackOnUnselectedItemClick(boolean shouldCallback) {
		mShouldCallbackOnUnselectedItemClick = shouldCallback;
	}

	/**
	 * Sets how long the transition animation should run when a child view
	 * changes position. Only relevant if animation is turned on.
	 * 
	 * @param animationDurationMillis
	 *            The duration of the transition, in milliseconds.
	 * 
	 * @attr ref android.R.styleable#Gallery_animationDuration
	 */
	public void setAnimationDuration(int animationDurationMillis) {
		mAnimationDuration = animationDurationMillis;
	}

	/**
	 * Sets the spacing between items in a Gallery
	 * item之间的间隙可以近似认为是imageview的宽度与缩放比例的乘积的一半
	 *
	 * @param spacing
	 *            The spacing in pixels between items in the Gallery
	 * 
	 * @attr ref android.R.styleable#Gallery_spacing
	 */
	public void setSpacing(int spacing) {
		mSpacing = spacing;
	}

	/**
	 * Sets the alpha of items that are not selected in the Gallery.
	 * 
	 * @param unselectedAlpha
	 *            the alpha for the items that are not selected.
	 * 
	 * @attr ref android.R.styleable#Gallery_unselectedAlpha
	 */
	public void setUnselectedAlpha(float unselectedAlpha) {
		mUnselectedAlpha = unselectedAlpha;
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {

		t.clear();
		t.setAlpha(child == mSelectedChild ? 1.0f : mUnselectedAlpha);

		return true;
	}

	@Override
	protected int computeHorizontalScrollExtent() {
		// Only 1 item is considered to be selected
		return 1;
	}

	@Override
	protected int computeHorizontalScrollOffset() {
		// Current scroll position is the same as the selected position
		return mSelectedPosition;
	}

	@Override
	protected int computeHorizontalScrollRange() {
		// Scroll range is the same as the item count
		return mItemCount;
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams;
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		/*
		 * Gallery expects CGallery.LayoutParams.
		 */
		return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		/*
		 * Remember that we are in layout to prevent more layout request from
		 * being generated.
		 */
		mInLayout = true;
		layout(0, false);

		mInLayout = false;
	}

	@Override
	int getChildHeight(View child) {
		return child.getMeasuredHeight();
	}

    @Override
    int getChildWidth(View child) {
        return child.getMeasuredWidth();
    }

    /**
	 * Tracks a motion scroll. In reality, this is used to do just about any
	 * movement to items (touch scroll, arrow-key scroll, set an item as
	 * selected).
	 * 
	 * @param deltaX
	 *            Change in X from the previous event.
	 */
	void trackMotionScroll(int deltaX) {

		if (getChildCount() == 0) {
			return;
		}

		boolean toLeft = deltaX < 0;

		int limitedDeltaX = getLimitedMotionScrollAmount(toLeft, deltaX);
		if (VERB) {
			Log.d(TAG, String.format("limitedDeltaX:%d", limitedDeltaX));
		}
		if (limitedDeltaX != deltaX) {
			// The above call returned a limited amount, so stop any
			// scrolls/flings
			mFlingRunnable.endFling(false);
			onFinishedMovement();
		}

		offsetChildrenLeftAndRight(limitedDeltaX);

		detachOffScreenChildren(toLeft);

		if (toLeft) {
			// If moved left, there will be empty space on the right
			fillToGalleryRight();
		} else {
			// Similarly, empty space on the left
			fillToGalleryLeft();
		}

		// 正常情况下child数量会刚好合适，clear并没有实质作用，而动画过程child数量会多一个，进行clear会导致频繁recreate
		// 而且这里是计算偏移，在layout时已经进行了clear
		// mRecycler.clear();

		setSelectionToCenterChild();

		final View selChild = mSelectedChild;
		if (selChild != null) {
			final int childLeft = selChild.getLeft();
			final int childCenter = selChild.getWidth() / 2;
			final int galleryCenter = getWidth() / 2;
			mSelectedCenterOffset = childLeft + childCenter - galleryCenter;
		}

		onScrollChanged(0, 0, 0, 0); // dummy values, View's implementation does not use these.

		invalidate();
	}

	int getLimitedMotionScrollAmount(boolean motionToLeft, int deltaX) {
		int extremeItemPosition = motionToLeft ? mItemCount - 1 : 0;
		View extremeChild = getChildAt(extremeItemPosition - mFirstPosition);

		if (extremeChild == null) {
			return deltaX;
		}

		int extremeChildCenter = getCenterOfView(extremeChild);
		int galleryCenter = getCenterOfGallery();

		if (motionToLeft) {
			if (extremeChildCenter <= galleryCenter) {

				// The extreme child is past his boundary point!
				return 0;
			}
		} else {
			if (extremeChildCenter >= galleryCenter) {

				// The extreme child is past his boundary point!
				return 0;
			}
		}

		int centerDifference = galleryCenter - extremeChildCenter;

		return motionToLeft ? Math.max(centerDifference, deltaX) : Math.min(centerDifference, deltaX);
	}

	/**
	 * Offset the horizontal location of all children of this view by the
	 * specified number of pixels.
	 * 
	 * @param offset
	 *            the number of pixels to offset
	 */
	private void offsetChildrenLeftAndRight(int offset) {
		for (int i = getChildCount() - 1; i >= 0; i--) {
			getChildAt(i).offsetLeftAndRight(offset);
		}
	}

	/**
	 * @return The center of this Gallery.
	 */
	private int getCenterOfGallery() {
		int paddingLeft = getPaddingLeft();
		return (getWidth() - paddingLeft - getPaddingRight()) / 2 + paddingLeft;
	}

	/**
	 * @return The center of the given view.
	 */
	private static int getCenterOfView(View view) {
		return view.getLeft() + view.getWidth() / 2;
	}

	/**
	 * Detaches children that are off the screen (i.e.: Gallery bounds).
	 * 
	 * @param toLeft
	 *            Whether to detach children to the left of the Gallery, or to
	 *            the right.
	 */
	private void detachOffScreenChildren(boolean toLeft) {
		int numChildren = getChildCount();
		int firstPosition = mFirstPosition;
		int start = 0;
		int count = 0;

		if (toLeft) {
			final int galleryLeft = getPaddingLeft();
			for (int i = 0; i < numChildren; i++) {
				final View child = getChildAt(i);
                if (VERB) {
                    Log.d(TAG, String.format("numChild:%d index:%d, right:%d, left:%d",
                            numChildren, i, child.getRight(), galleryLeft));
                }
                if (child.getRight() >= galleryLeft) {
					break;
				} else {
					count++;
					// 只将不可见的View加入到回收区
					mRecycler.add(firstPosition + i, child);
				}
			}
		} else {
			final int galleryRight = getWidth() - getPaddingRight();
			for (int i = numChildren - 1; i >= 0; i--) {
				final View child = getChildAt(i);
				if (child.getLeft() <= galleryRight) {
					break;
				} else {
					start = i;
					count++;
					// 只将不可见的View加入到回收区
					mRecycler.add(firstPosition + i, child);
				}
			}
		}

		if (VERB) {
            Log.d(TAG, String.format("firstPosition:%d start:%d, count:%d", firstPosition, start, count));
        }
        detachViewsFromParent(start, count);

		if (toLeft) {
			mFirstPosition += count;
		}
	}

	/**
	 * Scrolls the items so that the selected item is in its 'slot' (its center
	 * is the gallery's center).
	 * 滚动到对应的槽
	 */
	private void scrollIntoSlots() {

		if (getChildCount() == 0 || mSelectedChild == null)
			return;

		int selectedCenter = getCenterOfView(mSelectedChild);
		int targetCenter = getCenterOfGallery();

		int scrollAmount = targetCenter - selectedCenter;
		if (VERB) {
			Log.d(TAG, String.format("selCenter:%d, tarCenter:%d, scrollAmount:%d", selectedCenter, targetCenter, scrollAmount));
		}
		// Do not do scrollIntoSlots until Animations stop
        if (scrollAmount != 0 && !mOpFliping) {
			mFlingRunnable.startUsingDistance(scrollAmount);
		} else {
			onFinishedMovement();
		}
	}

	// Only for The End Of Flip Animation
	private void scrollIntoSlotsImmediately() {
        if (getChildCount() == 0 || mSelectedChild == null)
            return;

        int selectedCenter = getCenterOfView(mSelectedChild);
        int targetCenter = getCenterOfGallery();

        int scrollAmount = targetCenter - selectedCenter;
        mFlingRunnable.startUsingDistance(scrollAmount, 1);
    }

	private void onFinishedMovement() {
		if (mSuppressSelectionChanged) {
			mSuppressSelectionChanged = false;

			// We haven't been callbacking during the fling, so do it now
			super.selectionChanged();
		}
		mSelectedCenterOffset = 0;

		invalidate();

        // Only op move would cause the callback
        if (mOpMoving) {
//            super.performMoveFinish(mSelectedView, mSelectedPosition, mAdapter.getItemId(mSelectedPosition));
            super.performMoveFinish(getmSelectedView(), mSelectedPosition, mAdapter.getItemId(mSelectedPosition));
        }
    }

	@Override
	void selectionChanged() {
		if (!mSuppressSelectionChanged) {
			super.selectionChanged();
		}
	}

	/**
	 * Looks for the child that is closest to the center and sets it as the
	 * selected child.
	 */
	private void setSelectionToCenterChild() {

		View selView = mSelectedChild;
		if (mSelectedChild == null)
			return;

		int galleryCenter = getCenterOfGallery();

		// Common case where the current selected position is correct
		// @bugfix 修复初始化第一次点击无法翻页到bug，将等号去掉，因为那一次layout前刚好到达边缘，可以提前设置到下一页。
		// 如果无法修复，需要修改layout方法，在scroll过程不进行layout。
		if (selView.getLeft() < galleryCenter && selView.getRight() > galleryCenter) {
			if (VERB) {
				Log.d(TAG, String.format("Center not changed! selViewLeft:%d, galleryCenter:%d, selViewRight:%d",
						selView.getLeft(), galleryCenter, selView.getRight()));
			}
			return;
		}

		// TODO better search
		int closestEdgeDistance = Integer.MAX_VALUE;
		int newSelectedChildIndex = 0;
		for (int i = getChildCount() - 1; i >= 0; i--) {

			View child = getChildAt(i);
			if (VERB) {
				Log.d(TAG, String.format("1  newSelIndex:%d, gallerycenter:%d, childLeft:%d, childRight:%d",
						i, galleryCenter, child.getLeft(), child.getRight()));
			}
			if (child.getLeft() <= galleryCenter && child.getRight() >= galleryCenter) {
				// This child is in the center
				if (VERB) {
					Log.d(TAG, String.format("2 newSelIndex:%d, Center change! gallerycenter:%d, childLeft:%d, childRight:%d",
							i, galleryCenter, child.getLeft(), child.getRight()));
				}
				newSelectedChildIndex = i;
				break;
			}

			int childClosestEdgeDistance = Math.min(Math.abs(child.getLeft() - galleryCenter),
					Math.abs(child.getRight() - galleryCenter));
			if (childClosestEdgeDistance < closestEdgeDistance) {
				closestEdgeDistance = childClosestEdgeDistance;
				newSelectedChildIndex = i;
			}
			if (VERB) {
				Log.d(TAG, String.format("childClosestEdgeDistance:%d, childLeft:%d, childRight:%d, galleryCenter:%d, newSel:%d",
						childClosestEdgeDistance, child.getLeft(), child.getRight(), galleryCenter, newSelectedChildIndex));
			}
		}



		int newPos = mFirstPosition + newSelectedChildIndex;

		if (VERB) {
			Log.d(TAG, String.format("first:%d, newIndex:%d newPos:%d", mFirstPosition, newSelectedChildIndex, newPos));
		}
		if (newPos != mSelectedPosition) {
			Log.d(TAG, "1");
			setSelectedPositionInt(newPos);
			setNextSelectedPositionInt(newPos);
			checkSelectionChanged();
		}
	}

	/**
	 * Creates and positions all views for this Gallery.
	 * <p>
	 * We layout rarely, most of the time {@link #trackMotionScroll(int)} takes
	 * care of repositioning, adding, and removing children.
	 * 
	 * @param delta
	 *            Change in the selected position. +1 means the selection is
	 *            moving to the right, so views are scrolling to the left. -1
	 *            means the selection is moving to the left.
	 */
	@Override
	void layout(int delta, boolean animate) {
		if (VERB) {
			Log.d(TAG, "layout!!!!!!!!");
		}
//		int childrenLeft = mSpinnerPadding.left;
		int childrenLeft = getSpinnerPadding().left;
//		int childrenWidth = getRight() - getLeft() - mSpinnerPadding.left - mSpinnerPadding.right;
		int childrenWidth = getRight() - getLeft() - getSpinnerPadding().left - getSpinnerPadding().right;

        if (mDataChanged) {
            reset();
            handleDataChanged();
		}

		// Handle an empty gallery by removing all views.
		if (mItemCount == 0) {
			resetList();
			return;
		}

		// @bugfix: 不知道是怎么触发的，偶发地，初始化完毕第一次点击下一页会触发onlayout，从而导致在scroll过程重新布局中心view，
		// 进而导致scroll的间距被缩短，无法滚动到下一页到bug。layout应该仅在selviewchanged时触发。scroll过程不触发。
        // PS：上一个办法无法修复，所以添加了一个判断，仅在pos发生变化才设置选中。

		// Update to the new selected position.
		if (mNextSelectedPosition >= 0 && mSelectedPosition != mNextSelectedPosition) {
			setSelectedPositionInt(mNextSelectedPosition);
		}

		// All views go in recycler while we are in layout
		recycleAllViews();

		// Clear out old views
		detachAllViewsFromParent();

		/*
         * These will be used to give initial positions to views entering the
         * gallery as we scroll
         */
		// mRightMost = 0;
		// mLeftMost = 0;

		// Make selected view and center it

		/*
		 * mFirstPosition will be decreased as we add views to the left later
		 * on. The 0 for x will be offset in a couple lines down.
		 */
		mFirstPosition = mSelectedPosition;
		View sel = makeAndAddView(mSelectedPosition, 0, 0, true);

		// Put the selected child in the center
		int selectedOffset = childrenLeft + (childrenWidth / 2) - (sel.getWidth() / 2);
		sel.offsetLeftAndRight(selectedOffset);

		fillToGalleryRight();
		fillToGalleryLeft();

		// Flush any cached views that did not get reused above
		// layout时，会收区多余的View回收掉，可能是动态改变了Gallery大小
		mRecycler.clear();

		invalidate();
		checkSelectionChanged();

		// FirstTime or DataChanged
		if (mFirstLayout || mDataChanged) {
			updateWidth();
			updateHeight();
			mFirstLayout = false;
		}

		mDataChanged = false;
		mNeedSync = false;
		setNextSelectedPositionInt(mSelectedPosition);

		updateSelectedItemMetadata();
	}

    private void reset() {
        mOpFliping = false;
//        mCollapsing = false;
		setCollapsing(false);
	}

    private int mMaxShowCount = 7;
	public void setMaxShowCount(int max) {
		mMaxShowCount = max;
	}
	public int getMaxShowCount() {
		return mMaxShowCount;
	}

	private int mRealCount;
	public void setRealCount(int count) {
		mRealCount = count;
	}
	public int getRealCount() {
		return mRealCount;
	}

	/**
     * 使得Gallery自适应可见Item数的宽高，用于少量Item的循环滑动，如果不开启循环滑动，则无需关注
     **/
	private boolean mFirstLayout = true;
	private int mWidth;
	private int mHeight;
	private void updateWidth() {
		// int showCount = mRealCount <= mMaxShowCount ? mRealCount : mMaxShowCount;
		int showCount = mMaxShowCount;
        View theMaxChild = getChildAt(mSelectedPosition-mFirstPosition+(showCount/2));
        if (theMaxChild == null) {
            return;
        }
        int width = (theMaxChild.getRight() - getCenterOfGallery())*2;
        Log.d(TAG, String.format("gallery center:%d, maxchildRight:%d, width:%d", theMaxChild.getRight(), getCenterOfGallery(), width));
        if (mWidth == width) {
            return;
        }
        mWidth = width;

        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = width;
        setLayoutParams(params);
    }

	private void updateHeight() {
		View centerView = getChildAt(mSelectedPosition-mFirstPosition);
		if (centerView == null) {
			return;
		}
		int height = centerView.getHeight();
		if (mHeight == height) {
			return;
		}
		mHeight = height;

		ViewGroup.LayoutParams params = getLayoutParams();
		params.height = height;
		setLayoutParams(params);
	}

	private void fillToGalleryLeft() {
		int itemSpacing = mSpacing;
		int galleryLeft = getPaddingLeft();

		// Set state for initial iteration
		View prevIterationView = getChildAt(0);
		int curPosition;
		int curRightEdge;

		if (prevIterationView != null) {
			curPosition = mFirstPosition - 1;
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
		} else {
			// No children available!
			curPosition = 0;
			curRightEdge = getRight() - getLeft() - getPaddingRight();
			mShouldStopFling = true;
		}

		while (curRightEdge > galleryLeft && curPosition >= 0) {
			prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curRightEdge, false);

			// Remember some state
			mFirstPosition = curPosition;

			// Set state for next iteration
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
			curPosition--;
		}
	}

	private void fillToGalleryRight() {
		int itemSpacing = mSpacing;
		int galleryRight = getRight() - getLeft() - getPaddingRight();
		int numChildren = getChildCount();
		int numItems = mItemCount;


		// Set state for initial iteration
		View prevIterationView = getChildAt(numChildren - 1);
		int curPosition;
		int curLeftEdge;

		if (VERB) {
			Log.d(TAG, String.format("numChildren:%d, numItems:%d, prevView:%s",
					numChildren, numItems, prevIterationView == null ? "null!!!!" : prevIterationView));
		}

		if (prevIterationView != null) {
			curPosition = mFirstPosition + numChildren;
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
		} else {
            // @bugfix 修复当prevView为null时一下子滑动到尾部的问题
			curPosition = mFirstPosition + numChildren;
			// mFirstPosition = curPosition = mItemCount - 1;
			curLeftEdge = getPaddingLeft();
			// mShouldStopFling = true;
		}

		// recreate
		while (curLeftEdge < galleryRight && curPosition < numItems) {
			prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curLeftEdge, true);

			// Set state for next iteration
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
			curPosition++;
		}
	}

	/**
	 * Obtain a view, either by pulling an existing view from the recycler or by
	 * getting a new one from the adapter. If we are animating, make sure there
	 * is enough information in the view's layout parameters to animate from the
	 * old to new positions.
	 * 
	 * @param position
	 *            Position in the gallery for the view to obtain
	 * @param offset
	 *            Offset from the selected position
	 * @param x
	 *            X-coordinate indicating where this view should be placed. This
	 *            will either be the left or right edge of the view, depending
	 *            on the fromLeft parameter
	 * @param fromLeft
	 *            Are we positioning views based on the left edge? (i.e.,
	 *            building from left to right)?
	 * @return A view that has been added to the gallery
	 */
	private View makeAndAddView(int position, int offset, int x, boolean fromLeft) {
		// 回收区通常只有一个View，就是左滑或右滑出去的那个不可见的View。
		// 左滑出去的View会作为右滑进来的View进行重用。
		// 可重用很重要，即使每次仅创建一个View，但使用在动画过程，就会变成不断地创建View。

		// DataSetChanged可以不重新创建View，因为Adapter是没变的，这里是策略问题，
		// 新创建View可以保证每次DataSetChanged都重新开始
		if (!mDataChanged) {
			// pop a view from RecycleBin
			View child = mRecycler.get();

			// pass child as convertview
			child = mAdapter.getView(position, child, this);

			// Position the view
			setUpChild(child, offset, x, fromLeft);

			return child;
		}
		// DataChanged, recreate all view
		mRecycler.clear();

		// pass child as convertview
		View child = mAdapter.getView(position, null, this);

		// Position the view
		setUpChild(child, offset, x, fromLeft);

		return child;
	}

	/**
	 * Helper for makeAndAddView to set the position of a view and fill out its
	 * layout paramters.
	 * 
	 * @param child
	 *            The view to position
	 * @param offset
	 *            Offset from the selected position
	 * @param x
	 *            X-coordintate indicating where this view should be placed.
	 *            This will either be the left or right edge of the view,
	 *            depending on the fromLeft paramter
	 * @param fromLeft
	 *            Are we posiitoning views based on the left edge? (i.e.,
	 *            building from left to right)?
	 */
	private void setUpChild(View child, int offset, int x, boolean fromLeft) {

		// Respect layout params that are already in the view. Otherwise
		// make some up...
		LayoutParams lp = (LayoutParams) child.getLayoutParams();
		if (lp == null) {
			lp = (LayoutParams) generateDefaultLayoutParams();
		}

		addViewInLayout(child, fromLeft ? -1 : 0, lp);

		child.setSelected(offset == 0);

		// Get measure specs
//		int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec, mSpinnerPadding.top + mSpinnerPadding.bottom,
//				lp.height);
		int childHeightSpec = ViewGroup.getChildMeasureSpec(getHeightMeasureSpec(), getSpinnerPadding().top + getSpinnerPadding().bottom,
				lp.height);
//		int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec, mSpinnerPadding.left + mSpinnerPadding.right,
//				lp.width);
		int childWidthSpec = ViewGroup.getChildMeasureSpec(getWidthMeasureSpec(), getSpinnerPadding().left + getSpinnerPadding().right,
				lp.width);

		// Measure child
		child.measure(childWidthSpec, childHeightSpec);

		int childLeft;
		int childRight;

		// Position vertically based on gravity setting
		int childTop = calculateTop(child, true);
		int childBottom = childTop + child.getMeasuredHeight();

		int width = child.getMeasuredWidth();
		if (fromLeft) {
			childLeft = x;
			childRight = childLeft + width;
		} else {
			childLeft = x - width;
			childRight = x;
		}

		child.layout(childLeft, childTop, childRight, childBottom);
	}

	/**
	 * Figure out vertical placement based on mGravity
	 * 
	 * @param child
	 *            Child to place
	 * @return Where the top of the child should be
	 */
	private int calculateTop(View child, boolean duringLayout) {
		int myHeight = duringLayout ? getMeasuredHeight() : getHeight();
		int childHeight = duringLayout ? child.getMeasuredHeight() : child.getHeight();

		int childTop = 0;

		switch (mGravity) {
		case Gravity.TOP:
			childTop = getSpinnerPadding().top;
			break;
		case Gravity.CENTER_VERTICAL:
			int availableSpace = myHeight - getSpinnerPadding().bottom - getSpinnerPadding().top - childHeight;
			childTop = getSpinnerPadding().top + (availableSpace / 2);
			break;
		case Gravity.BOTTOM:
			childTop = myHeight - getSpinnerPadding().bottom - childHeight;
			break;
		}
		return childTop;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// Give everything to the gesture detector
		boolean retValue = mGestureDetector.onTouchEvent(event);

		int action = event.getAction();
		if (action == MotionEvent.ACTION_UP) {
			// Helper method for lifted finger
			onUp();
		} else if (action == MotionEvent.ACTION_CANCEL) {
			onCancel();
		}

		return retValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean onSingleTapUp(MotionEvent e) {

		if (mDownTouchPosition >= 0) {

			// An item tap should make it selected, so scroll to this child.
			if (VERB) {
				Log.d(TAG, String.format("down:%d first:%d", mDownTouchPosition, mFirstPosition));
			}
			scrollToChild(mDownTouchPosition - mFirstPosition);

			// Also pass the click so the client knows, if it wants to.
			if (mShouldCallbackOnUnselectedItemClick || mDownTouchPosition == mSelectedPosition) {
				performItemClick(mDownTouchView, mDownTouchPosition, mAdapter.getItemId(mDownTouchPosition));
			}

			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

		if (!mShouldCallbackDuringFling) {
			// We want to suppress selection changes

			// Remove any future code to set mSuppressSelectionChanged = false
			removeCallbacks(mDisableSuppressSelectionChangedRunnable);

			// This will get reset once we scroll into slots
			if (!mSuppressSelectionChanged)
				mSuppressSelectionChanged = true;
		}

		Log.d(TAG, "flip:"+velocityX);

		// Fling the gallery!
		mFlingRunnable.startUsingVelocity((int) -velocityX);

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

		if (VERB)
			Log.v(TAG, String.valueOf(e2.getX() - e1.getX()));

		/*
		 * Now's a good time to tell our parent to stop intercepting our events!
		 * The user has moved more than the slop amount, since GestureDetector
		 * ensures this before calling this method. Also, if a parent is more
		 * interested in this touch's events than we are, it would have
		 * intercepted them by now (for example, we can assume when a Gallery is
		 * in the ListView, a vertical scroll would not end up in this method
		 * since a ListView would have intercepted it by now).
		 */
		getParent().requestDisallowInterceptTouchEvent(true);

		// As the user scrolls, we want to callback selection changes so
		// related-
		// info on the screen is up-to-date with the gallery's selection
		if (!mShouldCallbackDuringFling) {
			if (mIsFirstScroll) {
				/*
				 * We're not notifying the client of selection changes during
				 * the fling, and this scroll could possibly be a fling. Don't
				 * do selection changes until we're sure it is not a fling.
				 */
				if (!mSuppressSelectionChanged)
					mSuppressSelectionChanged = true;
				postDelayed(mDisableSuppressSelectionChangedRunnable, SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT);
			}
		} else {
			if (mSuppressSelectionChanged)
				mSuppressSelectionChanged = false;
		}

		// Track the motion
		trackMotionScroll(-1 * (int) distanceX);

		mIsFirstScroll = false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean onDown(MotionEvent e) {

		// Kill any existing fling/scroll
		mFlingRunnable.stop(false);

		// Get the item's view that was touched
		// 获取点击位置的View的坐标
		mDownTouchPosition = pointToPosition((int) e.getX(), (int) e.getY());

		if (mDownTouchPosition >= 0) {
			mDownTouchView = getChildAt(mDownTouchPosition - mFirstPosition);
			mDownTouchView.setPressed(true);
		}

		// Reset the multiple-scroll tracking state
		mIsFirstScroll = true;
        mOpMoving = false;
        mOpFliping = false;

		// Must return true to get matching events for this down event.
		return true;
	}

	/**
	 * Called when a touch event's action is MotionEvent.ACTION_UP.
	 */
	void onUp() {

		if (mFlingRunnable.mScroller.isFinished()) {
			scrollIntoSlots();
		}

		dispatchUnpress();
	}

	/**
	 * Called when a touch event's action is MotionEvent.ACTION_CANCEL.
	 */
	void onCancel() {
		onUp();
	}

	/**
	 * {@inheritDoc}
	 */
	public void onLongPress(MotionEvent e) {

		if (mDownTouchPosition < 0) {
			return;
		}

		performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		long id = getItemIdAtPosition(mDownTouchPosition);
		dispatchLongPress(mDownTouchView, mDownTouchPosition, id);
	}

	// Unused methods from GestureDetector.OnGestureListener below

	/**
	 * {@inheritDoc}
	 */
	public void onShowPress(MotionEvent e) {
	}

	// Unused methods from GestureDetector.OnGestureListener above

	private void dispatchPress(View child) {

		if (child != null) {
			child.setPressed(true);
		}

		setPressed(true);
	}

	private void dispatchUnpress() {

		for (int i = getChildCount() - 1; i >= 0; i--) {
			getChildAt(i).setPressed(false);
		}

		setPressed(false);
	}

	@Override
	public void dispatchSetSelected(boolean selected) {
		/*
		 * We don't want to pass the selected state given from its parent to its
		 * children since this widget itself has a selected state to give to its
		 * children.
		 */
	}

	@Override
	protected void dispatchSetPressed(boolean pressed) {

		// Show the pressed state on the selected child
		if (mSelectedChild != null) {
			mSelectedChild.setPressed(pressed);
		}
	}

	@Override
	protected ContextMenuInfo getContextMenuInfo() {
		return mContextMenuInfo;
	}

	@Override
	public boolean showContextMenuForChild(View originalView) {

		final int longPressPosition = getPositionForView(originalView);
		if (longPressPosition < 0) {
			return false;
		}

		final long longPressId = mAdapter.getItemId(longPressPosition);
		return dispatchLongPress(originalView, longPressPosition, longPressId);
	}

	@Override
	public boolean showContextMenu() {

		if (isPressed() && mSelectedPosition >= 0) {
			int index = mSelectedPosition - mFirstPosition;
			View v = getChildAt(index);
			return dispatchLongPress(v, mSelectedPosition, mSelectedRowId);
		}

		return false;
	}

	private boolean dispatchLongPress(View view, int position, long id) {
		boolean handled = false;

		if (mOnItemLongClickListener != null) {
			handled = mOnItemLongClickListener.onItemLongClick(this, mDownTouchView, mDownTouchPosition, id);
		}

		if (!handled) {
			mContextMenuInfo = new AdapterContextMenuInfo(view, position, id);
			handled = super.showContextMenuForChild(this);
		}

		if (handled) {
			performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		}

		return handled;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// Gallery steals all key events
		return event.dispatch(this, null, null);
	}

	/**
	 * Handles left, right, and clicking
	 * 
	 * @see View#onKeyDown
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {

		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (movePrevious()) {
				playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
			}
			return true;

		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (moveNext()) {
				playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
			}
			return true;

		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			mReceivedInvokeKeyDown = true;
			// fallthrough to default handling
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER: {

			if (mReceivedInvokeKeyDown) {
				if (mItemCount > 0) {

					dispatchPress(mSelectedChild);
					postDelayed(new Runnable() {
						public void run() {
							dispatchUnpress();
						}
					}, ViewConfiguration.getPressedStateDuration());

					int selectedIndex = mSelectedPosition - mFirstPosition;
					performItemClick(getChildAt(selectedIndex), mSelectedPosition, mAdapter.getItemId(mSelectedPosition));
				}
			}

			// Clear the flag
			mReceivedInvokeKeyDown = false;

			return true;
		}
		}

		return super.onKeyUp(keyCode, event);
	}

    // move by UI Touch
	boolean movePrevious() {
//        mCollapsing = false;
		setCollapsing(false);
        if (mItemCount > 0 && mSelectedPosition > 0) {
			scrollToChild(mSelectedPosition - mFirstPosition - 1);
			return true;
		} else {
			return false;
		}
	}

	// move by UI Touch
	boolean moveNext() {
//        mCollapsing = false;
		setCollapsing(false);
        if (mItemCount > 0 && mSelectedPosition < mItemCount - 1) {
			scrollToChild(mSelectedPosition - mFirstPosition + 1);
			return true;
		} else {
			return false;
		}
	}

	// move in code but not by UI Touch
	public void moveToPrevious() {
        mOpMoving = true;
		movePrevious();
    }

    // move in code but not by UI Touch
	public void moveToNext() {
        mOpMoving = true;
		moveNext();
    }

    // move in code but not by UI Touch
	public void moveToNext(int duration) {
		mOpMoving = true;
//		mCollapsing = false;
		setCollapsing(false);
		if (mItemCount > 0 && mSelectedPosition < mItemCount - 1) {
			scrollToChild(getChildCount()-1, duration);
		} else {
		}
	}

	// Move Animation
    public void startMoving(final int duration) {
        mOpFliping = true;
//        mCollapsing = false;
		setCollapsing(false);

        moveToNext(duration);
		setOnMoveListener(new OnMoveListener() {
			@Override
			public void onMoveFinish(CGalleryAdapterView<?> parent, View view, int position, long id) {
				moveToNext(duration);
			}
		});
	}


	public void setFlipResult(int position) {
        mFlipResultPosition = position;
    }

    /** @param speed dp */
	public void startFling(final int speed) {
		int pxspeed = Dp2Px(getContext(), speed);
		Log.d(TAG, "pxspeed:"+pxspeed);
		// Fling the gallery!
		mOpFliping = true;
//		mCollapsing = false;
		setCollapsing(false);
        mFlipSpeed = pxspeed;

        // mFlingRunnable.startScroll(1000000, 6000);
        mFlingRunnable.startFling();
		// mFlingRunnable.startUsingVelocity(10000000);
		// new Thread(new Runnable() {
		// 	@Override
		// 	public void run() {
		// 		while (mOpFliping) {
		// 			mFlingRunnable.startUsingDistance((int) -1000);
		// 			try {
		// 				Thread.sleep(200);
		// 			} catch (InterruptedException e) {
		// 				break;
		// 			}
		// 		}
		// 	}
		// }).start();
	}

	public void stop() {
        mOpMoving = false;
        mOpFliping = false;
//		mCollapsing = false;
		setCollapsing(false);

		// Kill any existing fling/scroll and slot into center
		mFlingRunnable.stopAnimation();

//		performFlipAnimationListener(mSelectedView, mSelectedPosition, mAdapter.getItemId(mSelectedPosition));
		performFlipAnimationListener(getmSelectedView(), mSelectedPosition, mAdapter.getItemId(mSelectedPosition));
    }

	private boolean scrollToChild(int childPosition) {
		View child = getChildAt(childPosition);
		if (child != null) {
			int distance = getCenterOfGallery() - getCenterOfView(child);
			if (VERB) {
				Log.d(TAG, String.format("clickPos:%d, center:%d, childCenter:%d, distance:%d",
						childPosition, getCenterOfGallery(), getCenterOfView(child), distance));
			}
			mFlingRunnable.startUsingDistance(distance);
			return true;
		}
		return false;
	}

	private boolean scrollToChild(int childPosition, int duration) {
		View child = getChildAt(childPosition);
		if (child != null) {
			int distance = getCenterOfGallery() - getCenterOfView(child);
			if (VERB) {
				Log.d(TAG, String.format("clickPos:%d, center:%d, childCenter:%d, distance:%d",
						childPosition, getCenterOfGallery(), getCenterOfView(child), distance));
			}
			mFlingRunnable.startUsingDistance(distance, duration);
			return true;
		}
		return false;
	}

	@Override
	void setSelectedPositionInt(int position) {
		super.setSelectedPositionInt(position);
		if (VERB) {
			Log.d(TAG, String.format("set selected:%d", position));
		}
		// Updates any metadata we keep about the selected item.
		updateSelectedItemMetadata();
	}

	private void updateSelectedItemMetadata() {

		View oldSelectedChild = mSelectedChild;

		View child = mSelectedChild = getChildAt(mSelectedPosition - mFirstPosition);
		if (VERB) {
			Log.d(TAG, String.format("oldSel:%s newSel:%s",
					oldSelectedChild == null ? "null" : String.valueOf(oldSelectedChild.hashCode()),
					mSelectedChild == null ? "null" : String.valueOf(mSelectedChild.hashCode())));
		}
		if (child == null) {
			return;
		}

		child.setSelected(true);
		child.setFocusable(true);

		if (hasFocus()) {
			child.requestFocus();
		}

		// We unfocus the old child down here so the above hasFocus check
		// returns true
		if (oldSelectedChild != null && oldSelectedChild != child) {

			// Make sure its drawable state doesn't contain 'selected'
			oldSelectedChild.setSelected(false);

			// Make sure it is not focusable anymore, since otherwise arrow keys
			// can make this one be focused
			oldSelectedChild.setFocusable(false);
		}

	}

	/**
	 * Describes how the child views are aligned.
	 * 
	 * @param gravity
	 * 
	 * @attr ref android.R.styleable#Gallery_gravity
	 */
	public void setGravity(int gravity) {
		if (mGravity != gravity) {
			mGravity = gravity;
			requestLayout();
		}
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		int selectedIndex = mSelectedPosition - mFirstPosition;

		if (i < selectedIndex) {
			LogIndex(i);
			return i;
		} else if (i >= selectedIndex) {
			LogIndex(childCount-1-i+selectedIndex);
			return childCount - 1 - i + selectedIndex;
		} else {
			LogIndex(i);
			return i;
		}
	}

	private void LogIndex(int i) {
		if (VERB) {
			// Log.d(TAG, String.format("draw i:%d", i));
		}
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

		/*
		 * The gallery shows focus by focusing the selected item. So, give focus
		 * to our selected item instead. We steal keys from our selected item
		 * elsewhere.
		 */
		if (gainFocus && mSelectedChild != null) {
			mSelectedChild.requestFocus(direction);
			mSelectedChild.setSelected(true);
		}

	}

    /**
	 * Responsible for fling behavior. Use {@link #startUsingVelocity(int)} to
	 * initiate a fling. Each frame of the fling is handled in {@link #run()}. A
	 * FlingRunnable will keep re-posting itself until the fling is done.
	 * 
	 */
	private class FlingRunnable implements Runnable {
		/**
		 * Tracks the decay of a fling scroll
		 */
		private Scroller mScroller;

		/**
		 * X value reported by mScroller on the previous fling
		 */
		private int mLastFlingX;

		public FlingRunnable() {
			mScroller = new Scroller(getContext());
		}

		private void startCommon() {
			// Remove any pending flings
			removeCallbacks(this);
		}

		public void startUsingVelocity(int initialVelocity) {
			if (initialVelocity == 0)
				return;

			startCommon();

			int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
			mLastFlingX = initialX;
			mScroller.fling(initialX, 0, initialVelocity, 0, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
			post(this);
		}

		public void startFling() {
            post(this);
        }

        public void startScroll(int distance, int duration) {
            mScroller.startScroll(0, 0, -distance, 0, duration);
        }

		public void startUsingDistance(int distance) {
			startUsingDistance(distance, mAnimationDuration);
		}

		public void startUsingDistance(int distance, int duration) {
			if (distance == 0)
				return;

			startCommon();

			mLastFlingX = 0;
			mScroller.startScroll(0, 0, -distance, 0, duration);
			post(this);
		}

		public void stop(boolean scrollIntoSlots) {
			removeCallbacks(this);
			endFling(scrollIntoSlots);
		}

		public void stopAnimation() {
            removeCallbacks(this);
            mScroller.forceFinished(true);
            scrollIntoSlotsImmediately();
        }

		private void endFling(boolean scrollIntoSlots) {
			/*
			 * Force the scroller's status to finished (without setting its
			 * position to the end)
			 */
			mScroller.forceFinished(true);

			if (scrollIntoSlots)
				scrollIntoSlots();
		}

		public void run() {

			if (mItemCount == 0) {
				endFling(true);
				return;
			}

			mShouldStopFling = false;

			final Scroller scroller = mScroller;
			boolean more = scroller.computeScrollOffset(); // 判断scroller是否还在滑动
			final int x = scroller.getCurrX();


			// Flip sign to convert finger direction to list items direction
			// (e.g. finger moving down means list is moving towards the top)
			int delta = mLastFlingX - x;
			if (VERB) {
				Log.d(TAG, String.format("1 lastFling:%d, scroller_x:%d, delta:%d, more:%s",
						mLastFlingX, x, delta, String.valueOf(more)));
			}

			// Pretend that each frame of a fling scroll is a touch scroll
			if (delta > 0) {
				// Moving towards the left. Use first view as mDownTouchPosition
				mDownTouchPosition = mFirstPosition;

				// Don't fling more than 1 screen
				delta = Math.min(getWidth() - getPaddingLeft() - getPaddingRight() - 1, delta);
			} else {
				// Moving towards the right. Use last view as mDownTouchPosition
				int offsetToLast = getChildCount() - 1;
				mDownTouchPosition = mFirstPosition + offsetToLast;

				// Don't fling more than 1 screen
				delta = Math.max(-(getWidth() - getPaddingRight() - getPaddingLeft() - 1), delta);
			}
			if (VERB) {
				Log.d(TAG, String.format("1 lastFling:%d, scroller_x:%d, delta:%d, more:%s",
						mLastFlingX, x, delta, String.valueOf(more)));
			}

			// hook
            if (mOpFliping) {
                trackMotionScroll(mFlipSpeed);
                post(this);
            } else {
                trackMotionScroll(delta);

                if (more && !mShouldStopFling) {
                    mLastFlingX = x;
                    post(this);
                } else {
                    endFling(true);
                }
            }
		}

	}

	/**
	 * Gallery extends LayoutParams to provide a place to hold current
	 * Transformation information along with previous position/transformation
	 * info.
	 * 
	 */
	public static class LayoutParams extends ViewGroup.LayoutParams {
		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
		}

		public LayoutParams(int w, int h) {
			super(w, h);
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
	}


	public int Dp2Px(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	public int Px2Dp(Context context, float px) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (px / scale + 0.5f);
	}
}