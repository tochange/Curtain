package com.sevenheaven.curtain;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * Created by caifangmao on 15/3/26.
 */
public class CurtainContentLayout extends FrameLayout {

    private static final int DEFAULT_INTERCEPT_LENGTH = 30;
    private int defaultMenuWidth;

    private View content;
    private CurtainView curtainView;

    private Bitmap curtainTexture;

    private int initX;
    private int initY;

    private int currentX;
    private int currentY;

    private int vx;

    private Paint paint;

    private final long DURATION = 5000;
    private boolean inSlidingMode = false;

    private static final int SLIDING_MODE_CLOSED = 0;
    private static final int SLIDING_MODE_OPENED = 1;
    private static final int SLIDING_MODE_SLIDING = 2;

    private int slidingMode = 0;

    private int minVelocity;
    private int maxVY;
    private VelocityTracker velocityTracker;

    private ValueAnimator slidingAnimator;

    private GestureDetector gestureDetector;
    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent event) {
            if (slidingMode != SLIDING_MODE_OPENED) {
                slidingAnimator.cancel();

                inSlidingMode = true;

                initX = (int) event.getX();
                initY = (int) event.getY();

                if (curtainTexture != null && !curtainTexture.isRecycled()) {
                    curtainTexture.recycle();
                }


//                curtainTexture = getRootViewWithoutStatusBarBitmap((Activity) getContext());
                curtainTexture = snapshot(CurtainContentLayout.this.content);
                curtainView.setTexture(curtainTexture);
                curtainView.setVisibility(View.VISIBLE);
                CurtainContentLayout.this.content.setVisibility(View.GONE);
                return true;
            } else {
                if (event.getX() < defaultMenuWidth) {
                    return false;
                } else {
                    slidingAnimator.cancel();

                    inSlidingMode = true;

                    initX = (int) event.getX();
                    initY = (int) event.getY();

                    vx = defaultMenuWidth;

                    if (curtainTexture != null && !curtainTexture.isRecycled()) {
                        curtainTexture.recycle();
                    }

                    curtainTexture = snapshot(CurtainContentLayout.this.content);
                    curtainView.setTexture(curtainTexture);
                    curtainView.setVisibility(View.VISIBLE);
                    CurtainContentLayout.this.content.setVisibility(View.GONE);
                    return true;
                }
            }

        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            if (slidingMode == SLIDING_MODE_OPENED && event.getX() >= defaultMenuWidth) {
                slidingAnimator.setIntValues(vx, 0);
                slidingAnimator.start();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean onScroll(MotionEvent ev1, MotionEvent ev2, float distanceX, float distanceY) {
            if (CurtainContentLayout.this.content != null) {
                currentX = (int) ev2.getX();
                currentY = (int) ev2.getY();

                vx = slidingMode == SLIDING_MODE_CLOSED ? currentX - initX : defaultMenuWidth - (initX - currentX);

                if (vx < 0) {
                    vx = 0;
                }

                if (vx > 1080) {
                    vx = 1080;
                }
                curtainView.flip(vx, currentY);
            }

            return true;
        }

        @Override
        public boolean onFling(MotionEvent ev1, MotionEvent ev2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) > minVelocity * 5) {
                if (velocityX > 0) {
                    slidingAnimator.setIntValues(vx, defaultMenuWidth);
                } else {
                    slidingAnimator.setIntValues(vx, 0);
                }

                slidingAnimator.setDuration((long) (((float) vx / Math.abs(velocityX)) * 1000.0F));
                slidingAnimator.start();
            } else {
                if (ev2.getX() > defaultMenuWidth / 2) {
                    slidingAnimator.setIntValues(vx, defaultMenuWidth);
                } else {
                    slidingAnimator.setIntValues(vx, 0);
                }

                slidingAnimator.setDuration(DURATION);
                slidingAnimator.start();
            }
            return true;
        }
    };

    void setContent(View content) {
        this.content = content;
    }

    public CurtainContentLayout(Context context) {
        this(context, null);
    }

    public CurtainContentLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CurtainContentLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        defaultMenuWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DURATION, getResources().getDisplayMetrics());
        defaultMenuWidth = 1000;

        curtainView = new CurtainView(context);
        curtainView.setDirection(CurtainView.DIRECTION_LEFT);

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(curtainView, 0, layoutParams);
        curtainView.setVisibility(View.GONE);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        gestureDetector = new GestureDetector(context, gestureListener);

        minVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();

        slidingAnimator = ValueAnimator.ofInt(vx, 1);
        slidingAnimator.setDuration(DURATION);
        slidingAnimator.setInterpolator(new DecelerateInterpolator());
        slidingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                setSlidingFactor(value);
            }
        });
        slidingAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationStart(Animator animator) {
                if (slidingMode == SLIDING_MODE_OPENED) {
                    callback.onCloseStart();
                } else if (slidingMode == SLIDING_MODE_CLOSED) {
                    callback.onOpenStart();
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (vx > defaultMenuWidth / 2) {
                    slidingMode = SLIDING_MODE_OPENED;
                    callback.onCurtainOpen();
                } else {
                    slidingMode = SLIDING_MODE_CLOSED;
                    callback.onCurtainClose();
                }
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

        });
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    Callback callback;

    public static Bitmap getRootViewWithoutStatusBarBitmap(Activity activity) {
        View root = activity.getWindow().getDecorView();
        boolean old = root.isDrawingCacheEnabled();
        root.setDrawingCacheEnabled(true);
        Bitmap bmp = activity.getWindow().getDecorView().getDrawingCache();
        int statusBarHeight = getStatusBarHeight(activity);
        bmp = Bitmap.createBitmap(bmp, 0, statusBarHeight, bmp.getWidth(), bmp.getHeight() - statusBarHeight);
        root.setDrawingCacheEnabled(old);
        return bmp;
    }


    public static int getStatusBarHeight(Context activityContext) {
        int resId = activityContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusHeight = resId > 0 ? activityContext.getResources().getDimensionPixelSize(resId) : 0;
        return statusHeight;
    }

    public void toggle() {
        switch (slidingMode) {
            case SLIDING_MODE_CLOSED:
                slidingAnimator.setIntValues(vx, defaultMenuWidth);
                slidingAnimator.setDuration(DURATION);
                slidingAnimator.start();
                break;
            case SLIDING_MODE_OPENED:
                slidingAnimator.setIntValues(vx, 0);
                slidingAnimator.setDuration(DURATION);
                slidingAnimator.start();
                break;
        }
    }


    void setSlidingFactor(int slidingFactor) {
        this.vx = slidingFactor;

        if (this.vx <= 0) {
            this.content.setVisibility(View.VISIBLE);
            this.curtainView.setVisibility(View.GONE);
            inSlidingMode = false;
        } else {
            this.content.setVisibility(View.GONE);
            this.curtainView.setVisibility(View.VISIBLE);
        }

        this.curtainView.flip(this.vx, currentY);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN && event.getX() <= DEFAULT_INTERCEPT_LENGTH) {
            return true;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }

                velocityTracker.addMovement(event);

                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                currentX = (int) event.getX();
                currentY = (int) event.getY();

                final VelocityTracker velocityTracker = this.velocityTracker;
                final int pointerId = event.getPointerId(0);
                velocityTracker.computeCurrentVelocity(1000, ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity());
                final float velocityY = velocityTracker.getYVelocity(pointerId);
                final float velocityX = velocityTracker.getXVelocity(pointerId);

                if (Math.abs(velocityX) <= 0 && initX != currentX) {
                    if (event.getX() > defaultMenuWidth / 2) {
                        slidingAnimator.setIntValues(vx, defaultMenuWidth);
                        slidingAnimator.setDuration(DURATION);
                        slidingAnimator.start();
                    } else {
                        slidingAnimator.setIntValues(vx, 0);
                        slidingAnimator.setDuration(DURATION);
                        slidingAnimator.start();
                    }
                }
                break;
        }

        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void setBackgroundColor(int color) {
    }

    private Bitmap snapshot(View v) {
        Bitmap b = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Drawable bg = getBackground();
        if (bg != null) {
            bg.setBounds(0, 0, getWidth(), getHeight());
            bg.draw(c);
        } else {
            paint.setColor(0xFFFFFFFF);
            c.drawRect(0, 0, getWidth(), getHeight(), paint);
        }
        v.draw(c);
        //make sure the color on the top will continue to the status bar area
//        paint.setColor(b.getPixel(0, getStatusBarHeight() + 1));
//        c.drawRect(0, 0, getContext().getResources().getDisplayMetrics().widthPixels, getStatusBarHeight(), paint);
        return b;
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }
}
