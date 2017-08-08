package com.cvte.widget.gallery;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.SpinnerAdapter;

public class FancyCoverFlow extends EcoGallery {
	private static final String TAG = "FancyCoverFlow";

	public static final int ACTION_DISTANCE_AUTO = Integer.MAX_VALUE;

	/**
	 * 图片向上突出，可以通过代码控制，也可以在xml上控制
	 */
	public static final float SCALEDOWN_GRAVITY_TOP = 0.0f;
	/**
	 * 图片中间突出
	 */
	public static final float SCALEDOWN_GRAVITY_CENTER = 0.5f;
	/**
	 * 图片向下突出
	 */
	public static final float SCALEDOWN_GRAVITY_BOTTOM = 1.0f;

	private float reflectionRatio = 0.3f;

	private int reflectionGap = 4;

	private boolean reflectionEnabled = false;

	private float unselectedAlpha;

	private Camera transformationCamera;

	private int maxRotation = 0;

	private float unselectedScale;

	private float scaleDownGravity = SCALEDOWN_GRAVITY_CENTER;

	private int actionDistance;

	private float unselectedSaturation;

	public FancyCoverFlow(Context context) {
		super(context);
		this.initialize();
	}

	public FancyCoverFlow(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.initialize();
		this.applyXmlAttributes(attrs);
	}

	@SuppressLint("NewApi")
	public FancyCoverFlow(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (Build.VERSION.SDK_INT >= 11) {
			this.setLayerType(LAYER_TYPE_SOFTWARE, null);
		}
		this.initialize();
		this.applyXmlAttributes(attrs);
	}

	private void initialize() {
		this.transformationCamera = new Camera();
	}

	private void applyXmlAttributes(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.FancyCoverFlow);

		this.actionDistance = a
				.getInteger(R.styleable.FancyCoverFlow_actionDistance,
						ACTION_DISTANCE_AUTO);
		this.scaleDownGravity = a.getFloat(
				R.styleable.FancyCoverFlow_scaleDownGravity, 0.5f);
		this.maxRotation = a.getInteger(R.styleable.FancyCoverFlow_maxRotation,
				0);
		this.unselectedAlpha = a.getFloat(
				R.styleable.FancyCoverFlow_unselectedAlpha1, 0.5f);
		this.unselectedSaturation = a.getFloat(
				R.styleable.FancyCoverFlow_unselectedSaturation, 0.0f);
		this.unselectedScale = a.getFloat(
				R.styleable.FancyCoverFlow_unselectedScale, 0.75f);
	}

	public float getReflectionRatio() {
		return reflectionRatio;
	}

	public void setReflectionRatio(float reflectionRatio) {
		if (reflectionRatio <= 0 || reflectionRatio > 0.5f) {
			throw new IllegalArgumentException(
					"reflectionRatio may only be in the interval (0, 0.5]");
		}

		this.reflectionRatio = reflectionRatio;

		if (this.getAdapter() != null) {
			((FancyCoverFlowAdapter) this.getAdapter()).notifyDataSetChanged();
		}
	}

	public int getReflectionGap() {
		return reflectionGap;
	}

	public void setReflectionGap(int reflectionGap) {
		this.reflectionGap = reflectionGap;

		if (this.getAdapter() != null) {
			((FancyCoverFlowAdapter) this.getAdapter()).notifyDataSetChanged();
		}
	}

	public boolean isReflectionEnabled() {
		return reflectionEnabled;
	}

	public void setReflectionEnabled(boolean reflectionEnabled) {
		this.reflectionEnabled = reflectionEnabled;
		if (this.getAdapter() != null) {
			((FancyCoverFlowAdapter) this.getAdapter()).notifyDataSetChanged();
		}
	}

	@Override
	public void setAdapter(SpinnerAdapter adapter) {
		if (!(adapter instanceof FancyCoverFlowAdapter)) {
			throw new ClassCastException(FancyCoverFlow.class.getSimpleName()
					+ " only works in conjunction with a "
					+ FancyCoverFlowAdapter.class.getSimpleName());
		}

		super.setAdapter(adapter);
	}

	public int getMaxRotation() {
		return maxRotation;
	}

	public void setMaxRotation(int maxRotation) {
		this.maxRotation = maxRotation;
	}

	public float getUnselectedAlpha() {
		return this.unselectedAlpha;
	}

	public float getUnselectedScale() {
		return unselectedScale;
	}

	public void setUnselectedScale(float unselectedScale) {
		this.unselectedScale = unselectedScale;
	}

	public float getScaleDownGravity() {
		return scaleDownGravity;
	}

	public void setScaleDownGravity(float scaleDownGravity) {
		this.scaleDownGravity = scaleDownGravity;
	}

	public int getActionDistance() {
		return actionDistance;
	}

	public void setActionDistance(int actionDistance) {
		this.actionDistance = actionDistance;
	}

	@Override
	public void setUnselectedAlpha(float unselectedAlpha) {
		super.setUnselectedAlpha(unselectedAlpha);
		this.unselectedAlpha = unselectedAlpha;
	}

	public float getUnselectedSaturation() {
		return unselectedSaturation;
	}

	public void setUnselectedSaturation(float unselectedSaturation) {
		this.unselectedSaturation = unselectedSaturation;
	}

	public int preLeftOffset = 0;
	public int count = 0;
	public boolean isPlayDraw = true;
    //
	// @Override
	// protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
	// 	// TODO Auto-generated method stub
	// 	boolean ret;
	// 	//Android SDK 4.1
	// 	if(android.os.Build.VERSION.SDK_INT > 15){
	// 		final float offset = calculateOffsetOfCenter(child);
	// 		getTransformationMatrix(child, offset);
    //
	// 		child.setAlpha(1 - Math.abs(offset));
    //
	// 		final int saveCount = canvas.save();
	// 		canvas.concat(mMatrix);
	// 		ret = super.drawChild(canvas, child, drawingTime);
	// 		canvas.restoreToCount(saveCount);
	// 	}else{
	// 		ret = super.drawChild(canvas, child, drawingTime);
	// 	}
	// 	return ret;
	// }


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
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// int widthSize = 0;
		// int heightSize = 0;
        //
		// int paddingLeft = getPaddingLeft();
		// int paddingRight = getPaddingRight();
		// int paddingTop = getPaddingTop();
		// int paddingBottom = getPaddingBottom();
        //
		// mSpinnerPadding.left = paddingLeft > mSelectionLeftPadding ? paddingLeft
		// 		: mSelectionLeftPadding;
		// mSpinnerPadding.top = paddingTop > mSelectionTopPadding ? paddingTop
		// 		: mSelectionTopPadding;
		// mSpinnerPadding.right = paddingRight > mSelectionRightPadding ? paddingRight
		// 		: mSelectionRightPadding;
		// mSpinnerPadding.bottom = paddingBottom > mSelectionBottomPadding ? paddingBottom
		// 		: mSelectionBottomPadding;
        //
		// int preferredHeight = 0;
		// int preferredWidth = 0;
        //
		// final int size = getChildCount();
		// for (int i = 0; i < size; ++i) {
		// 	final View child = getChildAt(i);
		// 	if (child.getVisibility() != GONE) {
		// 		// 测量单个视图
		// 		measureChild(child, widthMeasureSpec, heightMeasureSpec);
		// 		preferredHeight = getChildHeight(child);
		// 		preferredWidth = getChildWidth(child);
        //
		// 		preferredHeight += mSpinnerPadding.top + mSpinnerPadding.bottom;
		// 		preferredWidth += mSpinnerPadding.left + mSpinnerPadding.right;
        //
		// 		preferredHeight = Math.max(preferredHeight, getSuggestedMinimumHeight());
		// 		preferredWidth = Math.max(preferredWidth, getSuggestedMinimumWidth());
        //
		// 		heightSize = resolveSize(preferredHeight, heightMeasureSpec);
		// 		widthSize += resolveSize(preferredWidth, widthMeasureSpec);
		// 		Log.d(TAG, String.format("preferHeightSize:%d, preferWidthSize:%d", preferredHeight, preferredWidth));
		// 	}
		// }
        //
		// // heightSize = resolveSize(preferredHeight, heightMeasureSpec);
		// // widthSize += resolveSize(preferredWidth, widthMeasureSpec);
        //
		// Log.d(TAG, String.format("HeightSize:%d, WidthSize:%d", heightSize, widthSize));
        //
		// setMeasuredDimension(getPreferredWidth(5), heightSize);
		// mHeightMeasureSpec = heightMeasureSpec;
		// mWidthMeasureSpec = widthMeasureSpec;
	}

	private static final int Size = 300;
	private static final int EffectDistance = Size * 5;

	private int getPreferredWidth(int num) {
		int preferredWidth = 0;
		for (int i = 0; i < num; ++i) {
			float effectsAmount = Math.min(
					1.0f,
					Math.max(-1.0f, (1.0f / EffectDistance / 2) * (Size*i)));

			// 距离中心越远，缩放越小
			final float zoomAmount = 1f / 2f * (1 - Math.abs(effectsAmount))
					* (1 - Math.abs(effectsAmount))
					* (1 - Math.abs(effectsAmount)) + 0.5f;

			double point = 0.4;
			double translateFactor = (-1f / (point * point)
					* (Math.abs(effectsAmount) - point)
					* (Math.abs(effectsAmount) - point) + 1)
					* (effectsAmount > 0 ? 1 : -1);

			// 约靠近中心，间距越短，间距最大为25dp
			int gap = (int) (ViewUtil.Dp2Px(getContext(), 30) * translateFactor);
			int width = (int) (Size*zoomAmount);

			Log.d(TAG, String.format("effect:%f, zoom:%f, gap:%d, width:%d", effectsAmount, zoomAmount, gap, width));
			preferredWidth += (Size*zoomAmount);
		}

		Log.d(TAG, String.format("preferredWidth:%d", preferredWidth));
		return preferredWidth;
	}


	public void startCollapse() {
        mCollapsing = true;

		int selPos = mSelectedPosition - mFirstPosition;
        Log.d(TAG, String.format("selPos:%d, childCount:%d", selPos, getChildCount()));
		if (selPos < 0 || selPos > getChildCount() - 1) {
			return;
		}
		for (int i = 0; i < getChildCount(); ++i) {
			if (i == selPos) {
                View center = getChildAt(selPos);
                View child = getChildAt(selPos+1);
                if (center == null || child == null) {
                    return;
                }
                if (child.getTag() == null) {
                    return;
                }
                float scale = (float)child.getTag();
                int distance = center.getLeft() - child.getLeft();
                // child.startAnimation(createTranslateAniSet(distance));
                scaleAnimRun(center, scale);
                Log.d(TAG, String.format("child:%d, selPos:%d, childCount:%d, scale:%f, distance:%d",
                        i, selPos, getChildCount(), scale, distance));
				continue;
			}
			View center = getChildAt(selPos);
			View child = getChildAt(i);
            if (center == null || child == null) {
                return;
            }
            if (child.getTag() == null) {
                return;
            }
            int width = (child.getRight()-child.getLeft());
			int distance = center.getLeft() - child.getLeft();
            float scale = (float) child.getTag();
            // child.startAnimation(createTranslateAniSet(distance));
            rotateyAnimRun(child, scale, distance);
            Log.d(TAG, String.format("child:%d, selPos:%d, childCount:%d, scale:%f, distance:%d",
                    i, selPos, getChildCount(), scale, distance));
		}
	}

	private AnimationSet createTranslateAniSet(int distance) {
		AnimationSet animationSet = new AnimationSet(true);

        TranslateAnimation translateAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 1000f,
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 1f);
        translateAnimation.setDuration(5000);

		AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
		//设置动画执行的时间（单位：毫秒）
		// alphaAnimation.setDuration(5000);

		animationSet.addAnimation(translateAnimation);
		// animationSet.addAnimation(alphaAnimation);
        // animationSet.setDuration(1500);
        // animationSet.setInterpolator(new AccelerateDecelerateInterpolator());

		return animationSet;
	}

    public void rotateyAnimRun(final View view, final float width, int distance)
    {
        int left = view.getLeft();
        // ObjectAnimator anim = ObjectAnimator//
        //         .ofFloat(view, "x", left,  left+distance)
        //         // .ofFloat(view, "scaleX", width, width)
        //         // .ofFloat(view, "scaleY", width, width)
        //         //
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("x", left,
                left+distance);
        PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("alpha", 1f,
                0f);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhZ);

        anim.setDuration(500);
        anim.start();
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                // float cVal = (Float) animation.getAnimatedValue();
                // view.setLeft((int) cVal);
                view.setScaleX(width);
                view.setScaleY(width);
            }
        });
    }

    public void scaleAnimRun(final View view, final float scale) {
        // int left = view.getLeft();
        // ObjectAnimator anim = ObjectAnimator//
        //         .ofFloat(view, "scaleX", 1, scale)
        //         .ofFloat(view, "scaleY", 1, scale)
        //         // .ofFloat(view, "scaleX", width, width)
        //         // .ofFloat(view, "scaleY", width, width)
        //         .setDuration(500);//

        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", 1f,
                scale);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", 1f,
                scale);
        PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("alpha", 1f,
                0f);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY, pvhZ);

        anim.setDuration(500);
        anim.start();
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                // float cVal = (Float) animation.getAnimatedValue();
                // view.setLeft((int) cVal);
                // view.setScaleX(width);
                // view.setScaleY(width);
            }
        });
    }

    private AnimationSet createScaleAniSet(int scale) {
		AnimationSet animationSet = new AnimationSet(false);

		ScaleAnimation scaleAnimation = new ScaleAnimation(1, 0.8f, 1, 1,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);

		AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
		//设置动画执行的时间（单位：毫秒）
		alphaAnimation.setDuration(1000);

		animationSet.addAnimation(scaleAnimation);
		animationSet.addAnimation(alphaAnimation);

		return animationSet;
	}

	// 	called when drawn child
	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
        if (mCollapsing) {
            return super.getChildStaticTransformation(child, t);
        }

        FancyCoverFlowItemWrapper item = (FancyCoverFlowItemWrapper) child;

		preLeftOffset = getChildAt(0).getLeft();

		if (android.os.Build.VERSION.SDK_INT >= 16) {
			item.postInvalidate();
		}
		// Log.d(TAG, String.format("1 %d,%d, %d,%d,%d,%d", this.getWidth(), this.getHeight(), this.getLeft()
		// , this.getRight(), this.getTop(), this.getBottom()));

		final int coverFlowWidth = this.getWidth();
		final int coverFlowCenter = coverFlowWidth / 2;
		final int childWidth = item.getWidth();
		final int childHeight = item.getHeight();
		final int childCenter = item.getLeft() + childWidth / 2;
		final int num =
		Log.d(TAG, String.format("2 %d %d,%d, %d,%d,%d,%d, %d,%d", item.hashCode(), item.getWidth(), item.getHeight(),
				item.getLeft(), item.getRight(), item.getTop(), item.getBottom(), childCenter, coverFlowCenter));

		final int actionDistance = (this.actionDistance == ACTION_DISTANCE_AUTO) ? (int) ((coverFlowWidth + childWidth))
				: this.actionDistance;

		float effectsAmount = Math.min(
				1.0f,
				Math.max(-1.0f, (1.0f / actionDistance / 2)
						* (childCenter - coverFlowCenter)));

		t.clear();
		t.setTransformationType(Transformation.TYPE_BOTH);

		if (this.unselectedAlpha != 1) {
			final float alphaAmount = (this.unselectedAlpha - 1)
					* Math.abs(effectsAmount) + 1;
			t.setAlpha(alphaAmount);
		}

		if (this.unselectedSaturation != 1) {
			// Pass over saturation to the wrapper.
			final float saturationAmount = (this.unselectedSaturation - 1)
					* Math.abs(effectsAmount) + 1;
			item.setSaturation(saturationAmount);
		}

		final Matrix imageMatrix = t.getMatrix();

		// 旋转角度不为0则开始图片旋转.
		if (this.maxRotation != 0) {
			final int rotationAngle = (int) (-effectsAmount * this.maxRotation);
			this.transformationCamera.save();
			this.transformationCamera.rotateY(rotationAngle);
			this.transformationCamera.getMatrix(imageMatrix);
			this.transformationCamera.restore();
		}

		// 缩放.
		if (this.unselectedScale != 1) {
			// 距离中心越远，缩放越小
			final float zoomAmount = 1f / 2f * (1 - Math.abs(effectsAmount))
					* (1 - Math.abs(effectsAmount))
					* (1 - Math.abs(effectsAmount)) + 0.5f;
			final float translateX = childWidth / 2.0f;
			final float translateY = childHeight * this.scaleDownGravity;
			imageMatrix.preTranslate(-translateX, -translateY);
			imageMatrix.postScale(zoomAmount, zoomAmount);
			imageMatrix.postTranslate(translateX, translateY);
            child.setTag(zoomAmount);

			if (effectsAmount != 0) {

				double point = 0.4;
				double translateFactor = (-1f / (point * point)
						* (Math.abs(effectsAmount) - point)
						* (Math.abs(effectsAmount) - point) + 1)
						* (effectsAmount > 0 ? 1 : -1);

				// 约靠近中心，间距越短，间距最大为25dp
				imageMatrix
						.postTranslate(
								(float) (ViewUtil.Dp2Px(getContext(), 30) * translateFactor),
								0);

				Log.d(TAG, String.format("3 %f,%f %f,%f %f,%f", effectsAmount, zoomAmount, translateX, translateY, (float)(ViewUtil.Dp2Px(getContext(), 25) * translateFactor), translateFactor));
			} else {
				Log.d(TAG, String.format("3 %f,%f %f,%f %d", effectsAmount, zoomAmount, translateX, translateY, ViewUtil.Dp2Px(getContext(), 0)));
			}
		}

		return true;
	}

	// 绘制顺序，先从左到中间，再从右到中间，绘制层叠效果
	// @Override
	// protected int getChildDrawingOrder(int childCount, int i) {
		// int selectedIndex = getSelectedItemPosition()
		// 		- getFirstVisiblePosition();
        //
		// if (i < selectedIndex) {
		// 	return i;
		// } else if (i >= selectedIndex) {
		// 	return childCount - 1 - i + selectedIndex;
		// } else {
		// 	return i;
		// }
	// }

	private boolean isTouchAble = true;

	public void disableTouch() {
		isTouchAble = false;
	}

	public void enableTouch() {
		isTouchAble = true;
	}

	public boolean isTouchAble() {
		return isTouchAble;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		count = 0;
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).invalidate();
		}

		if (isTouchAble) {
			return super.onTouchEvent(event);
		} else {
			return false;
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (isTouchAble) {
			return super.onInterceptTouchEvent(event);
		} else {
			return true;
		}
	}

	//
	// @Override
	// public boolean onSingleTapUp(MotionEvent e) {
	// return false;
	// }

	// 使快速滑动失效
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        return super.onFling(e1, e2, velocityX / 1.5f, velocityY);
		// return super.onFling(e1, e2, velocityX / 2, velocityY);
		// return false;
	}

	public void setListViewHeightBasedOnChildren() {

		//获取listview的适配器
		FancyCoverFlowAdapter listAdapter = (FancyCoverFlowAdapter) this.getAdapter(); //item的高度

		if (listAdapter == null) {
			return;
		}
		int totalHeight = 0;

		// for (int i = 0; i < listAdapter.getCount(); i++) {
		// 	View listItem = mAdapter.getView(i, null, this);
        //
		// 	listItem.measure(0, 0); //计算子项View 的宽高 //统计所有子项的总高度
		// 	totalHeight += Dp2Px(getApplicationContext(),listItem.getMeasuredHeight());
		// }
		// ViewGroup.LayoutParams params = listView.getLayoutParams();
        //
		// params.height = totalHeight;
		// listView.setLayoutParams(params);

	}

	public int Dp2Px(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;

		return (int) (dp * scale + 0.5f);
	}
}