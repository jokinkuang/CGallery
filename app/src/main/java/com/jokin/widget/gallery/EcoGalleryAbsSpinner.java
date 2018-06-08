package com.jokin.widget.gallery;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

public abstract class EcoGalleryAbsSpinner extends EcoGalleryAdapterView<SpinnerAdapter> {
    private static final String TAG = "CGalleryAbsSpinner";
    private static final boolean VERB = false;

    SpinnerAdapter mAdapter;

    private boolean mCollapsing = false;

    private int mHeightMeasureSpec;
    private int mWidthMeasureSpec;
    boolean mBlockLayoutRequest;
//    int mSelectionLeftPadding = 0;
//    int mSelectionTopPadding = 0;
//    int mSelectionRightPadding = 0;
//    int mSelectionBottomPadding = 0;
    private Rect mSpinnerPadding = new Rect();
    private View mSelectedView = null;
//    Interpolator mInterpolator;

    RecycleBin mRecycler = new RecycleBin();
    private DataSetObserver mDataSetObserver;


    /** Temporary frame to hold a child View's frame rectangle */
    private Rect mTouchFrame;

    public EcoGalleryAbsSpinner(Context context) {
        super(context);
        initAbsSpinner();
    }

    public EcoGalleryAbsSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EcoGalleryAbsSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAbsSpinner();

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CustomAbsSpinner, defStyle, 0);

        CharSequence[] entries = a.getTextArray(R.styleable.CustomAbsSpinner_entries);
        if (entries != null) {
            ArrayAdapter<CharSequence> adapter =
                    new ArrayAdapter<CharSequence>(context,
                    		android.R.layout.simple_spinner_item, entries);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            setAdapter(adapter);
        }

        a.recycle();
    }

    /**
     * Common code for different constructor flavors
     */
    private void initAbsSpinner() {
        setFocusable(true);
        setWillNotDraw(false);
    }

    public boolean isCollapsing() {
        return mCollapsing;
    }

    public void setCollapsing(boolean collapsing) {
        this.mCollapsing = collapsing;
    }

    //has the same method getSelectedView()
    public View getmSelectedView() {
        return mSelectedView;
    }

    public void setmSelectedView(View selectedView) {
        this.mSelectedView = selectedView;
    }

    public int getHeightMeasureSpec() {
        return mHeightMeasureSpec;
    }

    public void setHeightMeasureSpec(int heightMeasureSpec) {
        this.mHeightMeasureSpec = heightMeasureSpec;
    }

    public int getWidthMeasureSpec() {
        return mWidthMeasureSpec;
    }

    public void setWidthMeasureSpec(int widthMeasureSpec) {
        this.mWidthMeasureSpec = widthMeasureSpec;
    }

    public Rect getSpinnerPadding() {
        return mSpinnerPadding;
    }

    public void setSpinnerPadding(Rect spinnerPadding) {
        this.mSpinnerPadding = spinnerPadding;
    }

    /**
     * The Adapter is used to provide the data which backs this Spinner.
     * It also provides methods to transform spinner items based on their position
     * relative to the selected item.
     * @param adapter The SpinnerAdapter to use for this Spinner
     */
    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        if (null != mAdapter) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
            resetList();
        }
       
        mAdapter = adapter;
       
        mOldSelectedPosition = INVALID_POSITION;
        mOldSelectedRowId = INVALID_ROW_ID;
       
        if (mAdapter != null) {
            mOldItemCount = mItemCount;
            mItemCount = mAdapter.getCount();
            checkFocus();

            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);

            int position = mItemCount > 0 ? 0 : INVALID_POSITION;

            setSelectedPositionInt(position);
            setNextSelectedPositionInt(position);
           
            if (mItemCount == 0) {
                // Nothing selected
                checkSelectionChanged();
            }
           
        } else {
            checkFocus();            
            resetList();
            // Nothing selected
            checkSelectionChanged();
        }

        requestLayout();
    }

    /**
     * Clear out all children from the list
     */
    void resetList() {
        mDataChanged = false;
        mNeedSync = false;
       
        removeAllViewsInLayout();
        mOldSelectedPosition = INVALID_POSITION;
        mOldSelectedRowId = INVALID_ROW_ID;
       
        setSelectedPositionInt(INVALID_POSITION);
        setNextSelectedPositionInt(INVALID_POSITION);
        invalidate();
    }

    /**
     * @see View#measure(int, int)
     *
     * Figure out the dimensions of this Spinner. The width comes from
     * the widthMeasureSpec as Spinnners can't have their width set to
     * UNSPECIFIED. The height is based on the height of the selected item
     * plus padding.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // int widthSize = 0;
        // int heightSize = 0;
        //
        // int paddingLeft = getPaddingLeft();
        // int paddingRight = getPaddingRight();
        // int paddingTop = getPaddingTop();
        // int paddingBottom = getPaddingBottom();
        //
        // mSpinnerPadding.left = paddingLeft > mSelectionLeftPadding ? paddingLeft
        //         : mSelectionLeftPadding;
        // mSpinnerPadding.top = paddingTop > mSelectionTopPadding ? paddingTop
        //         : mSelectionTopPadding;
        // mSpinnerPadding.right = paddingRight > mSelectionRightPadding ? paddingRight
        //         : mSelectionRightPadding;
        // mSpinnerPadding.bottom = paddingBottom > mSelectionBottomPadding ? paddingBottom
        //         : mSelectionBottomPadding;
        //
        // int preferredHeight = 0;
        // int preferredWidth = 0;
        //
        // final int size = getChildCount();
        // for (int i = 0; i < size; ++i) {
        //     final View child = getChildAt(i);
        //     if (child.getVisibility() != GONE) {
        //         // 测量单个视图
        //         measureChild(child, widthMeasureSpec, heightMeasureSpec);
        //         child.setTag(, i);
        //         preferredHeight = getChildHeight(child);
        //         preferredWidth = getChildWidth(child);
        //
        //         preferredHeight += mSpinnerPadding.top + mSpinnerPadding.bottom;
        //         preferredWidth += mSpinnerPadding.left + mSpinnerPadding.right;
        //
        //         preferredHeight = Math.max(preferredHeight, getSuggestedMinimumHeight());
        //         preferredWidth = Math.max(preferredWidth, getSuggestedMinimumWidth());
        //
        //         heightSize = resolveSize(preferredHeight, heightMeasureSpec);
        //         widthSize += resolveSize(preferredWidth, widthMeasureSpec);
        //         Log.d(TAG, String.format("preferHeightSize:%d, preferWidthSize:%d", preferredHeight, preferredWidth));
        //     }
        // }
        //
        // // heightSize = resolveSize(preferredHeight, heightMeasureSpec);
        // // widthSize += resolveSize(preferredWidth, widthMeasureSpec);
        //
        // Log.d(TAG, String.format("HeightSize:%d, WidthSize:%d", heightSize, widthSize));
        //
        // setMeasuredDimension(widthSize, heightSize);
        // mHeightMeasureSpec = heightMeasureSpec;
        // mWidthMeasureSpec = widthMeasureSpec;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        // int widthSize;
        // int heightSize;
        //
        // int paddingLeft = getPaddingLeft();
        // int paddingRight = getPaddingRight();
        // int paddingTop = getPaddingTop();
        // int paddingBottom = getPaddingBottom();
        //
        // mSpinnerPadding.left = paddingLeft > mSelectionLeftPadding ? paddingLeft
        //         : mSelectionLeftPadding;
        // mSpinnerPadding.top = paddingTop > mSelectionTopPadding ? paddingTop
        //         : mSelectionTopPadding;
        // mSpinnerPadding.right = paddingRight > mSelectionRightPadding ? paddingRight
        //         : mSelectionRightPadding;
        // mSpinnerPadding.bottom = paddingBottom > mSelectionBottomPadding ? paddingBottom
        //         : mSelectionBottomPadding;
        //
        // if (mDataChanged) {
        //     handleDataChanged();
        // }
        //
        // int preferredHeight = 0;
        // int preferredWidth = 0;
        // boolean needsMeasuring = true;
        //
        // int selectedPosition = getSelectedItemPosition();
        // if (selectedPosition >= 0 && mAdapter != null) {
        //     // Try looking in the recycler. (Maybe we were measured once already)
        //     View view = mRecycler.get();
        //     if (view == null) {
        //         // Make a new one
        //         view = mAdapter.getView(selectedPosition, null, this);
        //     }
        //
        //     if (view != null) {
        //         // Put in recycler for re-measuring and/or layout
        //         mRecycler.add(selectedPosition, view);
        //     }
        //
        //     if (view != null) {
        //         if (view.getLayoutParams() == null) {
        //             mBlockLayoutRequest = true;
        //             view.setLayoutParams(generateDefaultLayoutParams());
        //             mBlockLayoutRequest = false;
        //         }
        //         measureChild(view, widthMeasureSpec, heightMeasureSpec);
        //
        //         preferredHeight = getChildHeight(view) + mSpinnerPadding.top + mSpinnerPadding.bottom;
        //         preferredWidth = getChildWidth(view) + mSpinnerPadding.left + mSpinnerPadding.right;
        //
        //         needsMeasuring = false;
        //     }
        // }
        //
        // if (needsMeasuring) {
        //     // No views -- just use padding
        //     preferredHeight = mSpinnerPadding.top + mSpinnerPadding.bottom;
        //     if (widthMode == View.MeasureSpec.UNSPECIFIED) {
        //         preferredWidth = mSpinnerPadding.left + mSpinnerPadding.right;
        //     }
        // }
        //
        // preferredHeight = Math.max(preferredHeight, getSuggestedMinimumHeight());
        // preferredWidth = Math.max(preferredWidth, getSuggestedMinimumWidth());
        //
        // heightSize = resolveSize(preferredHeight, heightMeasureSpec);
        // widthSize = resolveSize(preferredWidth, widthMeasureSpec);
        //
        // setMeasuredDimension(widthSize, heightSize);
        // mHeightMeasureSpec = heightMeasureSpec;
        // mWidthMeasureSpec = widthMeasureSpec;
        // if (VERB) {
        //     Log.d(TAG, String.format("width:%d, height:%d", widthSize, heightSize));
        // }
    }


    int getChildHeight(View child) {
        return child.getMeasuredHeight();
    }

    int getChildWidth(View child) {
        return child.getMeasuredWidth();
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    void recycleAllViews() {
        int childCount = getChildCount();
        final EcoGalleryAbsSpinner.RecycleBin recycleBin = mRecycler;

        // All views go in recycler
        for (int i=0; i<childCount; i++) {
            View v = getChildAt(i);
            int index = mFirstPosition + i;
            recycleBin.put(index, v);
        }
    }

    @Override
    void handleDataChanged() {
        // FIXME -- this is called from both measure and layout.
        // This is harmless right now, but we don't want to do redundant work if
        // this gets more complicated
        super.handleDataChanged();
    }



    /**
     * Jump directly to a specific item in the adapter data.
     */
    public void setSelection(int position, boolean animate) {
        // Animate only if requested position is already on screen somewhere
        boolean shouldAnimate = animate && mFirstPosition <= position &&
                position <= mFirstPosition + getChildCount() - 1;
        setSelectionInt(position, shouldAnimate);
    }


    @Override
    public void setSelection(int position) {
        setNextSelectedPositionInt(position);
        requestLayout();
        invalidate();
    }


    /**
     * Makes the item at the supplied position selected.
     *
     * @param position Position to select
     * @param animate Should the transition be animated
     *
     */
    void setSelectionInt(int position, boolean animate) {
        if (position != mOldSelectedPosition) {
            mBlockLayoutRequest = true;
            int delta  = position - mSelectedPosition;
            setNextSelectedPositionInt(position);
            layout(delta, animate);
            mBlockLayoutRequest = false;
        }
    }

    abstract void layout(int delta, boolean animate);

    @Override
    public View getSelectedView() {
        if (mItemCount > 0 && mSelectedPosition >= 0) {
            return getChildAt(mSelectedPosition - mFirstPosition);
        } else {
            return null;
        }
    }

    /**
     * Override to prevent spamming ourselves with layout requests
     * as we place views
     *
     * @see View#requestLayout()
     */
    @Override
    public void requestLayout() {
        if (!mBlockLayoutRequest) {
            super.requestLayout();
        }
    }



    @Override
    public SpinnerAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public int getCount() {
        return mItemCount;
    }

    /**
     * Maps a point to a position in the list.
     *
     * @param x X in local coordinate
     * @param y Y in local coordinate
     * @return The position of the item which contains the specified point, or
     *         {@link #INVALID_POSITION} if the point does not intersect an item.
     */
    public int pointToPosition(int x, int y) {
        Rect frame = mTouchFrame;
        if (frame == null) {
            mTouchFrame = new Rect();
            frame = mTouchFrame;
        }

        // 旧逻辑
        // for (int i = 0; i <= count - 1; i++) {
        //     View child = getChildAt(i);
        //     if (child.getVisibility() == View.VISIBLE) {
        //         child.getHitRect(frame);
        //         if (frame.contains(x, y)) {
        //             return mFirstPosition + i;
        //         }
        //     }
        // }



        // click center
        final View selectedView = getSelectedView();
        if (selectedView == null) {
            return INVALID_POSITION;
        }
        selectedView.getHitRect(frame);
        if (frame.contains(x, y)) {
            return mSelectedPosition;
        }

        // @bugfix 不是从尾到头遍历，而是从中间到两边，形成层级更高的优先响应
        // 另：mSelectedPosition是逻辑下标，对应的childView下标需要做一下转换
        final int count = getChildCount();
        final int selectedViewLeft = getSelectedView().getLeft();
        final int selectedViewRight = getSelectedView().getRight();
        if (VERB) {
            Log.d(TAG, String.format("count:%d, selected:%d, first:%d, left:%d, right:%d, x:%d, y:%d",
                    count, mSelectedPosition, mFirstPosition, selectedViewLeft, selectedViewRight, x, y));
        }

        // click left
        if (x < selectedViewLeft) {
            int childPos = mSelectedPosition - mFirstPosition;
            for (int i = childPos; i >= 0 && i <= count - 1; i--) {
                View child = getChildAt(i);
                if (child.getVisibility() == View.VISIBLE) {
                    child.getHitRect(frame);
                    if (frame.contains(x, y)) {
                        if (VERB) {
                            Log.d(TAG, String.format("new selected i:%d, first:%d, pos:%d", i, mFirstPosition, mFirstPosition+i));
                        }
                        return mFirstPosition + i;
                    }
                }
            }
        }
        // click right
        else if (x > selectedViewRight) {
            int childPos = mSelectedPosition - mFirstPosition;
            for (int i = childPos; i >= 0 && i <= count - 1; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == View.VISIBLE) {
                    child.getHitRect(frame);
                    if (frame.contains(x, y)) {
                        if (VERB) {
                            Log.d(TAG, String.format("new selected i:%d, first:%d, pos:%d", i, mFirstPosition, mFirstPosition+i));
                        }
                        return mFirstPosition + i;
                    }
                }
            }
        }

        return INVALID_POSITION;
    }

    static class SavedState extends View.BaseSavedState {
        long selectedId;
        int position;

        /**
         * Constructor called from {@link EcoGalleryAbsSpinner#onSaveInstanceState()}
         */
        SavedState(Parcelable superState) {
            super(superState);
        }
       
        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            selectedId = in.readLong();
            position = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(selectedId);
            out.writeInt(position);
        }

        @Override
        public String toString() {
            return "AbsSpinner.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " selectedId=" + selectedId
                    + " position=" + position + "}";
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.selectedId = getSelectedItemId();
        if (ss.selectedId >= 0) {
            ss.position = getSelectedItemPosition();
        } else {
            ss.position = INVALID_POSITION;
        }
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
 
        super.onRestoreInstanceState(ss.getSuperState());

        if (ss.selectedId >= 0) {
            mDataChanged = true;
            mNeedSync = true;
            mSyncRowId = ss.selectedId;
            mSyncPosition = ss.position;
            mSyncMode = SYNC_SELECTED_POSITION;
            requestLayout();
        }
    }

    class RecycleBin {
        private SparseArray<View> mScrapHeap = new SparseArray<View>();

        public void put(int position, View v) {
            mScrapHeap.put(position, v);
        }
 
        public void add(int position, View v) {
            mScrapHeap.put(mScrapHeap.size(), v);
        }
        public View get() {
            if (mScrapHeap.size() < 1) return null;
           
            View result = mScrapHeap.valueAt(0);
            int key = mScrapHeap.keyAt(0);
           
            if (result != null) {
                    mScrapHeap.delete(key);
            }
            return result;
        }
       
        View get(int position) {
            // System.out.print("Looking for " + position);
            View result = mScrapHeap.get(position);
            if (result != null) {
                // System.out.println(" HIT");
                mScrapHeap.delete(position);
            } else {
                // System.out.println(" MISS");
            }
            return result;
        }

        int size() {
            return mScrapHeap.size();
        }
       
        View peek(int position) {
            // System.out.print("Looking for " + position);
            return mScrapHeap.get(position);
        }
       
        void clear() {
            final SparseArray<View> scrapHeap = mScrapHeap;
           
            final int count = scrapHeap.size();
            for (int i = 0; i < count; i++) {
                final View view = scrapHeap.valueAt(i);
                if (view != null) {
                    removeDetachedView(view, true);
                }
            }
                       
            scrapHeap.clear();
        }
    }
}