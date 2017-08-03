package com.cvte.widget.gallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
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
		// this.setSpacing(0);
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

	// 	called when drawn child
	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
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
		// Log.d(TAG, String.format("2 %d,%d, %d,%d,%d,%d, %d,%d", item.getWidth(), item.getHeight(),
		// 		item.getLeft(), item.getRight(), item.getTop(), item.getBottom(), childCenter, coverFlowCenter));

		final int actionDistance = (this.actionDistance == ACTION_DISTANCE_AUTO) ? (int) ((coverFlowWidth + childWidth) / 2.0f)
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

				// Log.d(TAG, String.format("3 %f,%f %f,%f %f,%f", effectsAmount, zoomAmount, translateX, translateY, (float)(ViewUtil.Dp2Px(getContext(), 25) * translateFactor), translateFactor));
			} else {
				// Log.d(TAG, String.format("3 %f,%f %f,%f %d", effectsAmount, zoomAmount, translateX, translateY, ViewUtil.Dp2Px(getContext(), 0)));
			}
		}

		// requestLayout();

		return true;
	}

	// 绘制顺序，先从左到中间，再从右到中间，绘制层叠效果
	// @Override
	// protected int getChildDrawingOrder(int childCount, int i) {
    //
	// 	int selectedIndex = getSelectedItemPosition()
	// 			- getFirstVisiblePosition();
    //
	// 	if (i < selectedIndex) {
	// 		return i;
	// 	} else if (i >= selectedIndex) {
	// 		return childCount - 1 - i + selectedIndex;
	// 	} else {
	// 		return i;
	// 	}
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
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
}