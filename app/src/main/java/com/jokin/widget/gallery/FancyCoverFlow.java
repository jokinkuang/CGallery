package com.jokin.widget.gallery;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.SpinnerAdapter;

public class FancyCoverFlow extends EcoGallery {
	private static final String TAG = "CoverFlowGallery";
    private static boolean VERB = false;

	public static final int ACTION_DISTANCE_AUTO = Integer.MAX_VALUE;
    private static final int CollapseAnimationDuration = 1000;

	public static final float SCALEDOWN_GRAVITY_TOP = 0.0f;
	public static final float SCALEDOWN_GRAVITY_CENTER = 0.5f;
	public static final float SCALEDOWN_GRAVITY_BOTTOM = 1.0f;

    private boolean reflectionEnabled = false;
	private float reflectionRatio = 0.3f;
	private int reflectionGap = 4;

	private float unselectedAlpha;
	private float unselectedScale;
	private float unselectedSaturation;

    private float scaleDownGravity = SCALEDOWN_GRAVITY_CENTER;
    private int actionDistance;
    private int maxRotation = 0;

    private Camera transformationCamera;

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
				R.styleable.CoverFlowGallery);

        this.actionDistance = a
                .getInteger(R.styleable.CoverFlowGallery_actionDistance,
                        ACTION_DISTANCE_AUTO);
        this.scaleDownGravity = a.getFloat(
                R.styleable.CoverFlowGallery_scaleDownGravity, 0.5f);
        this.maxRotation = a.getInteger(R.styleable.CoverFlowGallery_maxRotation,
                0);
        this.unselectedAlpha = a.getFloat(
                R.styleable.CoverFlowGallery_unselectedAlpha1, 0.8f);
        this.unselectedSaturation = a.getFloat(
                R.styleable.CoverFlowGallery_unselectedSaturation, 0.0f);
        this.unselectedScale = a.getFloat(
                R.styleable.CoverFlowGallery_unselectedScale, 0.75f);
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


    @Override
	public void setAdapter(SpinnerAdapter adapter) {
		if (!(adapter instanceof FancyCoverFlowAdapter)) {
			throw new ClassCastException(FancyCoverFlow.class.getSimpleName()
					+ " only works in conjunction with a "
					+ FancyCoverFlowAdapter.class.getSimpleName());
		}

		super.setAdapter(adapter);
	}

    public interface CollapseAnimationListener {
        void onCollapseAnimatonEnd();
    }

    private CollapseAnimationListener mListener;
    public void setCollapseListener(CollapseAnimationListener listener) {
        mListener = listener;
    }

	public int preLeftOffset = 0;
	public int count = 0;

	public void startCollapseAnimation() {
//		mCollapsing = true;
		setCollapsing(true);

		int selPos = mSelectedPosition - mFirstPosition;
        if (VERB) {
            Log.d(TAG, String.format("selPos:%d, childCount:%d", selPos, getChildCount()));
        }
        if (selPos < 0 || selPos > getChildCount() - 1) {
			return;
		}
		for (int i = 0; i < getChildCount(); ++i) {
            // center child animation
			if (i == selPos) {
				View center = getChildAt(selPos);
				View rchild = getChildAt(selPos+1);
				if (center == null || rchild == null) {
					return;
				}

                float scale = 0;
				if (rchild.getTag() != null) {
                    scale = (Float) rchild.getTag();
                }
                int distance = center.getLeft() - rchild.getLeft();
				scaleAnimRun(center, scale);
				Log.d(TAG, String.format("child:%d, selPos:%d, childCount:%d, scale:%f, distance:%d",
						i, selPos, getChildCount(), scale, distance));
				continue;
			}
			// other child animation
			View center = getChildAt(selPos);
			View ichild = getChildAt(i);
			if (center == null || ichild == null) {
				return;
			}

            float scale = 0;
            if (ichild.getTag() != null) {
                scale = (Float) ichild.getTag();
            }
			int width = (ichild.getRight()-ichild.getLeft());
			int distance = center.getLeft() - ichild.getLeft();
			transAnimRun(ichild, scale, distance);
            if (VERB) {
                Log.d(TAG, String.format("child:%d, selPos:%d, childCount:%d, scale:%f, distance:%d",
                        i, selPos, getChildCount(), scale, distance));
            }
        }
	}

	public void transAnimRun(final View view, final float width, int distance)
	{
		int left = view.getLeft();
		PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("x", left, left+distance);
		PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("alpha", 1f, 0);
		ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhZ);

		anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				view.setScaleX(width);
				view.setScaleY(width);
			}
		});
        anim.setDuration(CollapseAnimationDuration);
        anim.start();
    }

	public void scaleAnimRun(final View view, final float scale) {
		PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", 1f, scale);
		PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", 1f, scale);
		PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("alpha", 1f, 0f);
		ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY, pvhZ);

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }
            @Override
            public void onAnimationEnd(Animator animator) {
                if (mListener != null) {
                    mListener.onCollapseAnimatonEnd();
                }
            }
            @Override
            public void onAnimationCancel(Animator animator) {
            }
            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        anim.setDuration(CollapseAnimationDuration);
        anim.start();
	}


	// 	called when drawing
	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
//		if (mCollapsing) {
		if (isCollapsing()) {
			return super.getChildStaticTransformation(child, t);
		}

		FancyCoverFlowItemWrapper item = (FancyCoverFlowItemWrapper) child;

		preLeftOffset = getChildAt(0).getLeft();

		if (Build.VERSION.SDK_INT >= 16) {
			item.postInvalidate();
		}
		if (VERB) {
            Log.d(TAG, String.format("1 %d,%d, %d,%d,%d,%d", this.getWidth(), this.getHeight(), this.getLeft()
                    , this.getRight(), this.getTop(), this.getBottom()));
        }

		final int coverFlowWidth = this.getWidth();
		final int coverFlowCenter = coverFlowWidth / 2;
		final int childWidth = item.getWidth();
		final int childHeight = item.getHeight();
		final int childCenter = item.getLeft() + childWidth / 2;
        if (VERB) {
            Log.d(TAG, String.format("2 %d %d,%d, %d,%d,%d,%d, %d,%d", item.hashCode(), item.getWidth(), item.getHeight(),
                    item.getLeft(), item.getRight(), item.getTop(), item.getBottom(), childCenter, coverFlowCenter));
        }

        // 受影响距离
		final int actionDistance = (this.actionDistance == ACTION_DISTANCE_AUTO) ? (int) ((coverFlowWidth))
				: this.actionDistance;

		float effectsAmount = Math.min(
				1.0f,
				Math.max(-1.0f, (1.0f / actionDistance / 2)
						* (childCenter - coverFlowCenter)));

		t.clear();
		t.setTransformationType(Transformation.TYPE_BOTH);

		if (this.unselectedAlpha != 1) {
			final float alphaAmount = 1 - (1 - this.unselectedAlpha)
					* Math.abs(effectsAmount);
			Log.d(TAG, String.format("alphaAmount:%f unselAlpha:%f", alphaAmount, unselectedAlpha));
			t.setAlpha(alphaAmount);
		}

		// if (this.unselectedSaturation != 1) {
		// 	// Pass over saturation to the wrapper.
		// 	final float saturationAmount = (this.unselectedSaturation - 1)
		// 			* Math.abs(effectsAmount) + 1;
		// 	item.setSaturation(saturationAmount);
		// }

		final Matrix imageMatrix = t.getMatrix();

		// 旋转角度不为0则开始图片旋转.
		// if (this.maxRotation != 0) {
		// 	final int rotationAngle = (int) (-effectsAmount * this.maxRotation);
		// 	this.transformationCamera.save();
		// 	this.transformationCamera.rotateY(rotationAngle);
		// 	this.transformationCamera.getMatrix(imageMatrix);
		// 	this.transformationCamera.restore();
		// }

		// 缩放.
		if (this.unselectedScale != 0) {
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
								(float) (this.Dp2Px(getContext(), 50) * translateFactor),
								0);
                if (VERB) {
                    Log.d(TAG, String.format("3 %f,%f %f,%f %f,%f", effectsAmount, zoomAmount, translateX, translateY, (float)(this.Dp2Px(getContext(), 25) * translateFactor), translateFactor));
                }
            } else {
                if (VERB) {
                    Log.d(TAG, String.format("3 %f,%f %f,%f %d", effectsAmount, zoomAmount, translateX, translateY, this.Dp2Px(getContext(), 0)));
                }
            }
		}

		return true;
	}

	// 使快速滑动减速
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        return super.onFling(e1, e2, velocityX / 1.5f, velocityY);
	}
}