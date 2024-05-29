package com.android.systemui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.RectF;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.statusbar.FlingAnimationUtils;
import java.util.HashMap;
/* loaded from: a.zip:com/android/systemui/SwipeHelper.class */
public class SwipeHelper {
    private Callback mCallback;
    private boolean mCanCurrViewBeDimissed;
    private View mCurrView;
    private float mDensityScale;
    private boolean mDisableHwLayers;
    private boolean mDragging;
    private FalsingManager mFalsingManager;
    private int mFalsingThreshold;
    private FlingAnimationUtils mFlingAnimationUtils;
    private float mInitialTouchPos;
    private LongPressListener mLongPressListener;
    private boolean mLongPressSent;
    private float mPagingTouchSlop;
    private float mPerpendicularInitialTouchPos;
    private boolean mSnappingChild;
    private int mSwipeDirection;
    private boolean mTouchAboveFalsingThreshold;
    private Runnable mWatchLongPress;
    private float SWIPE_ESCAPE_VELOCITY = 100.0f;
    private int DEFAULT_ESCAPE_ANIMATION_DURATION = 200;
    private int MAX_ESCAPE_ANIMATION_DURATION = 400;
    private int MAX_DISMISS_VELOCITY = 4000;
    private float mMinSwipeProgress = 0.0f;
    private float mMaxSwipeProgress = 1.0f;
    private float mTranslation = 0.0f;
    private final int[] mTmpPos = new int[2];
    private HashMap<View, Animator> mDismissPendingMap = new HashMap<>();
    private Handler mHandler = new Handler();
    private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
    private long mLongPressTimeout = ViewConfiguration.getLongPressTimeout() * 1.5f;

    /* loaded from: a.zip:com/android/systemui/SwipeHelper$Callback.class */
    public interface Callback {
        boolean canChildBeDismissed(View view);

        View getChildAtPosition(MotionEvent motionEvent);

        float getFalsingThresholdFactor();

        boolean isAntiFalsingNeeded();

        void onBeginDrag(View view);

        void onChildDismissed(View view);

        void onChildSnappedBack(View view, float f);

        void onDragCancelled(View view);

        boolean updateSwipeProgress(View view, boolean z, float f);
    }

    /* loaded from: a.zip:com/android/systemui/SwipeHelper$LongPressListener.class */
    public interface LongPressListener {
        boolean onLongPress(View view, int i, int i2);
    }

    public SwipeHelper(int i, Callback callback, Context context) {
        this.mCallback = callback;
        this.mSwipeDirection = i;
        this.mDensityScale = context.getResources().getDisplayMetrics().density;
        this.mPagingTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
        this.mFalsingThreshold = context.getResources().getDimensionPixelSize(2131689883);
        this.mFalsingManager = FalsingManager.getInstance(context);
        this.mFlingAnimationUtils = new FlingAnimationUtils(context, ((float) getMaxEscapeAnimDuration()) / 1000.0f);
    }

    private int getFalsingThreshold() {
        return (int) (this.mFalsingThreshold * this.mCallback.getFalsingThresholdFactor());
    }

    private float getMaxVelocity() {
        return this.MAX_DISMISS_VELOCITY * this.mDensityScale;
    }

    private float getPerpendicularPos(MotionEvent motionEvent) {
        return this.mSwipeDirection == 0 ? motionEvent.getY() : motionEvent.getX();
    }

    private float getPos(MotionEvent motionEvent) {
        return this.mSwipeDirection == 0 ? motionEvent.getX() : motionEvent.getY();
    }

    private float getSwipeAlpha(float f) {
        return Math.min(0.0f, Math.max(1.0f, f / 0.5f));
    }

    private float getSwipeProgressForOffset(View view, float f) {
        return Math.min(Math.max(this.mMinSwipeProgress, Math.abs(f / getSize(view))), this.mMaxSwipeProgress);
    }

    private float getVelocity(VelocityTracker velocityTracker) {
        return this.mSwipeDirection == 0 ? velocityTracker.getXVelocity() : velocityTracker.getYVelocity();
    }

    public static void invalidateGlobalRegion(View view) {
        invalidateGlobalRegion(view, new RectF(view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
    }

    public static void invalidateGlobalRegion(View view, RectF rectF) {
        while (view.getParent() != null && (view.getParent() instanceof View)) {
            view = (View) view.getParent();
            view.getMatrix().mapRect(rectF);
            view.invalidate((int) Math.floor(rectF.left), (int) Math.floor(rectF.top), (int) Math.ceil(rectF.right), (int) Math.ceil(rectF.bottom));
        }
    }

    private void snapChildInstantly(View view) {
        boolean canChildBeDismissed = this.mCallback.canChildBeDismissed(view);
        setTranslation(view, 0.0f);
        updateSwipeProgressFromOffset(view, canChildBeDismissed);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSwipeProgressFromOffset(View view, boolean z) {
        updateSwipeProgressFromOffset(view, z, getTranslation(view));
    }

    private void updateSwipeProgressFromOffset(View view, boolean z, float f) {
        float swipeProgressForOffset = getSwipeProgressForOffset(view, f);
        if (!this.mCallback.updateSwipeProgress(view, z, swipeProgressForOffset) && z) {
            if (!this.mDisableHwLayers) {
                if (swipeProgressForOffset == 0.0f || swipeProgressForOffset == 1.0f) {
                    view.setLayerType(0, null);
                } else {
                    view.setLayerType(2, null);
                }
            }
            view.setAlpha(getSwipeAlpha(swipeProgressForOffset));
        }
        invalidateGlobalRegion(view);
    }

    protected ObjectAnimator createTranslationAnimation(View view, float f) {
        return ObjectAnimator.ofFloat(view, this.mSwipeDirection == 0 ? View.TRANSLATION_X : View.TRANSLATION_Y, f);
    }

    public void dismissChild(View view, float f, Runnable runnable, long j, boolean z, long j2, boolean z2) {
        boolean canChildBeDismissed = this.mCallback.canChildBeDismissed(view);
        boolean z3 = view.getLayoutDirection() == 1;
        boolean z4 = (f == 0.0f && (getTranslation(view) == 0.0f || z2)) ? this.mSwipeDirection == 1 : false;
        float size = (((f > 0.0f ? 1 : (f == 0.0f ? 0 : -1)) >= 0 ? ((f > 0.0f ? 1 : (f == 0.0f ? 0 : -1)) != 0 || (getTranslation(view) > 0.0f ? 1 : (getTranslation(view) == 0.0f ? 0 : -1)) >= 0) ? false : !z2 : true) || (((f > 0.0f ? 1 : (f == 0.0f ? 0 : -1)) != 0 || ((getTranslation(view) > 0.0f ? 1 : (getTranslation(view) == 0.0f ? 0 : -1)) != 0 && !z2)) ? false : z3) || z4) ? -getSize(view) : getSize(view);
        if (j2 == 0) {
            j2 = f != 0.0f ? Math.min(this.MAX_ESCAPE_ANIMATION_DURATION, (int) ((Math.abs(size - getTranslation(view)) * 1000.0f) / Math.abs(f))) : this.DEFAULT_ESCAPE_ANIMATION_DURATION;
        }
        if (!this.mDisableHwLayers) {
            view.setLayerType(2, null);
        }
        Animator viewTranslationAnimator = getViewTranslationAnimator(view, size, new ValueAnimator.AnimatorUpdateListener(this, view, canChildBeDismissed) { // from class: com.android.systemui.SwipeHelper.2
            final SwipeHelper this$0;
            final View val$animView;
            final boolean val$canBeDismissed;

            {
                this.this$0 = this;
                this.val$animView = view;
                this.val$canBeDismissed = canChildBeDismissed;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.onTranslationUpdate(this.val$animView, ((Float) valueAnimator.getAnimatedValue()).floatValue(), this.val$canBeDismissed);
            }
        });
        if (viewTranslationAnimator == null) {
            return;
        }
        if (z) {
            viewTranslationAnimator.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
            viewTranslationAnimator.setDuration(j2);
        } else {
            this.mFlingAnimationUtils.applyDismissing(viewTranslationAnimator, getTranslation(view), size, f, getSize(view));
        }
        if (j > 0) {
            viewTranslationAnimator.setStartDelay(j);
        }
        viewTranslationAnimator.addListener(new AnimatorListenerAdapter(this, view, canChildBeDismissed, runnable) { // from class: com.android.systemui.SwipeHelper.3
            private boolean mCancelled;
            final SwipeHelper this$0;
            final View val$animView;
            final boolean val$canBeDismissed;
            final Runnable val$endAction;

            {
                this.this$0 = this;
                this.val$animView = view;
                this.val$canBeDismissed = canChildBeDismissed;
                this.val$endAction = runnable;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.updateSwipeProgressFromOffset(this.val$animView, this.val$canBeDismissed);
                this.this$0.mDismissPendingMap.remove(this.val$animView);
                if (!this.mCancelled) {
                    this.this$0.mCallback.onChildDismissed(this.val$animView);
                }
                if (this.val$endAction != null) {
                    this.val$endAction.run();
                }
                if (this.this$0.mDisableHwLayers) {
                    return;
                }
                this.val$animView.setLayerType(0, null);
            }
        });
        prepareDismissAnimation(view, viewTranslationAnimator);
        this.mDismissPendingMap.put(view, viewTranslationAnimator);
        viewTranslationAnimator.start();
    }

    public void dismissChild(View view, float f, boolean z) {
        dismissChild(view, f, null, 0L, z, 0L, false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public float getEscapeVelocity() {
        return getUnscaledEscapeVelocity() * this.mDensityScale;
    }

    protected long getMaxEscapeAnimDuration() {
        return this.MAX_ESCAPE_ANIMATION_DURATION;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public float getSize(View view) {
        return this.mSwipeDirection == 0 ? view.getMeasuredWidth() : view.getMeasuredHeight();
    }

    protected float getTranslation(View view) {
        return this.mSwipeDirection == 0 ? view.getTranslationX() : view.getTranslationY();
    }

    protected float getUnscaledEscapeVelocity() {
        return this.SWIPE_ESCAPE_VELOCITY;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Animator getViewTranslationAnimator(View view, float f, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        ObjectAnimator createTranslationAnimation = createTranslationAnimation(view, f);
        if (animatorUpdateListener != null) {
            createTranslationAnimation.addUpdateListener(animatorUpdateListener);
        }
        return createTranslationAnimation;
    }

    protected boolean handleUpEvent(MotionEvent motionEvent, View view, float f, float f2) {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isDismissGesture(MotionEvent motionEvent) {
        boolean isAntiFalsingNeeded = this.mCallback.isAntiFalsingNeeded();
        return ((this.mFalsingManager.isClassiferEnabled() ? isAntiFalsingNeeded ? this.mFalsingManager.isFalseTouch() : false : isAntiFalsingNeeded && !this.mTouchAboveFalsingThreshold) || !((swipedFastEnough() || swipedFarEnough()) && motionEvent.getActionMasked() == 1)) ? false : this.mCallback.canChildBeDismissed(this.mCurrView);
    }

    public void onDownUpdate(View view) {
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case 0:
                this.mTouchAboveFalsingThreshold = false;
                this.mDragging = false;
                this.mSnappingChild = false;
                this.mLongPressSent = false;
                this.mVelocityTracker.clear();
                this.mCurrView = this.mCallback.getChildAtPosition(motionEvent);
                if (this.mCurrView != null) {
                    onDownUpdate(this.mCurrView);
                    this.mCanCurrViewBeDimissed = this.mCallback.canChildBeDismissed(this.mCurrView);
                    this.mVelocityTracker.addMovement(motionEvent);
                    this.mInitialTouchPos = getPos(motionEvent);
                    this.mPerpendicularInitialTouchPos = getPerpendicularPos(motionEvent);
                    this.mTranslation = getTranslation(this.mCurrView);
                    if (this.mLongPressListener != null) {
                        if (this.mWatchLongPress == null) {
                            this.mWatchLongPress = new Runnable(this, motionEvent) { // from class: com.android.systemui.SwipeHelper.1
                                final SwipeHelper this$0;
                                final MotionEvent val$ev;

                                {
                                    this.this$0 = this;
                                    this.val$ev = motionEvent;
                                }

                                @Override // java.lang.Runnable
                                public void run() {
                                    if (this.this$0.mCurrView == null || this.this$0.mLongPressSent) {
                                        return;
                                    }
                                    this.this$0.mLongPressSent = true;
                                    this.this$0.mCurrView.sendAccessibilityEvent(2);
                                    this.this$0.mCurrView.getLocationOnScreen(this.this$0.mTmpPos);
                                    this.this$0.mLongPressListener.onLongPress(this.this$0.mCurrView, ((int) this.val$ev.getRawX()) - this.this$0.mTmpPos[0], ((int) this.val$ev.getRawY()) - this.this$0.mTmpPos[1]);
                                }
                            };
                        }
                        this.mHandler.postDelayed(this.mWatchLongPress, this.mLongPressTimeout);
                        break;
                    }
                }
                break;
            case 1:
            case 3:
                boolean z = !this.mDragging ? this.mLongPressSent : true;
                this.mDragging = false;
                this.mCurrView = null;
                this.mLongPressSent = false;
                removeLongPressCallback();
                if (z) {
                    return true;
                }
                break;
            case 2:
                if (this.mCurrView != null && !this.mLongPressSent) {
                    this.mVelocityTracker.addMovement(motionEvent);
                    float pos = getPos(motionEvent);
                    float perpendicularPos = getPerpendicularPos(motionEvent);
                    float f = pos - this.mInitialTouchPos;
                    float f2 = this.mPerpendicularInitialTouchPos;
                    if (Math.abs(f) > this.mPagingTouchSlop && Math.abs(f) > Math.abs(perpendicularPos - f2)) {
                        this.mCallback.onBeginDrag(this.mCurrView);
                        this.mDragging = true;
                        this.mInitialTouchPos = getPos(motionEvent);
                        this.mTranslation = getTranslation(this.mCurrView);
                        removeLongPressCallback();
                        break;
                    }
                }
                break;
        }
        boolean z2 = true;
        if (!this.mDragging) {
            z2 = this.mLongPressSent;
        }
        return z2;
    }

    protected void onMoveUpdate(View view, float f, float f2) {
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        float f;
        if (this.mLongPressSent) {
            return true;
        }
        if (!this.mDragging) {
            if (this.mCallback.getChildAtPosition(motionEvent) != null) {
                onInterceptTouchEvent(motionEvent);
                return true;
            }
            removeLongPressCallback();
            return false;
        }
        this.mVelocityTracker.addMovement(motionEvent);
        switch (motionEvent.getAction()) {
            case 1:
            case 3:
                if (this.mCurrView != null) {
                    this.mVelocityTracker.computeCurrentVelocity(1000, getMaxVelocity());
                    float velocity = getVelocity(this.mVelocityTracker);
                    if (!handleUpEvent(motionEvent, this.mCurrView, velocity, getTranslation(this.mCurrView))) {
                        if (isDismissGesture(motionEvent)) {
                            dismissChild(this.mCurrView, velocity, !swipedFastEnough());
                        } else {
                            this.mCallback.onDragCancelled(this.mCurrView);
                            snapChild(this.mCurrView, 0.0f, velocity);
                        }
                        this.mCurrView = null;
                    }
                    this.mDragging = false;
                    return true;
                }
                return true;
            case 2:
            case 4:
                if (this.mCurrView != null) {
                    float pos = getPos(motionEvent) - this.mInitialTouchPos;
                    float abs = Math.abs(pos);
                    if (abs >= getFalsingThreshold()) {
                        this.mTouchAboveFalsingThreshold = true;
                    }
                    if (this.mCallback.canChildBeDismissed(this.mCurrView)) {
                        f = pos;
                    } else {
                        float size = getSize(this.mCurrView);
                        f = 0.25f * size;
                        if (abs < size) {
                            f *= (float) Math.sin((pos / size) * 1.5707963267948966d);
                        } else if (pos <= 0.0f) {
                            f = -f;
                        }
                    }
                    setTranslation(this.mCurrView, this.mTranslation + f);
                    updateSwipeProgressFromOffset(this.mCurrView, this.mCanCurrViewBeDimissed);
                    onMoveUpdate(this.mCurrView, this.mTranslation + f, f);
                    return true;
                }
                return true;
            default:
                return true;
        }
    }

    public void onTranslationUpdate(View view, float f, boolean z) {
        updateSwipeProgressFromOffset(view, z, f);
    }

    protected void prepareDismissAnimation(View view, Animator animator) {
    }

    protected void prepareSnapBackAnimation(View view, Animator animator) {
    }

    public void removeLongPressCallback() {
        if (this.mWatchLongPress != null) {
            this.mHandler.removeCallbacks(this.mWatchLongPress);
            this.mWatchLongPress = null;
        }
    }

    public void setDensityScale(float f) {
        this.mDensityScale = f;
    }

    public void setDisableHardwareLayers(boolean z) {
        this.mDisableHwLayers = z;
    }

    public void setLongPressListener(LongPressListener longPressListener) {
        this.mLongPressListener = longPressListener;
    }

    public void setPagingTouchSlop(float f) {
        this.mPagingTouchSlop = f;
    }

    protected void setTranslation(View view, float f) {
        if (view == null) {
            return;
        }
        if (this.mSwipeDirection == 0) {
            view.setTranslationX(f);
        } else {
            view.setTranslationY(f);
        }
    }

    public void snapChild(View view, float f, float f2) {
        boolean canChildBeDismissed = this.mCallback.canChildBeDismissed(view);
        Animator viewTranslationAnimator = getViewTranslationAnimator(view, f, new ValueAnimator.AnimatorUpdateListener(this, view, canChildBeDismissed) { // from class: com.android.systemui.SwipeHelper.4
            final SwipeHelper this$0;
            final View val$animView;
            final boolean val$canBeDismissed;

            {
                this.this$0 = this;
                this.val$animView = view;
                this.val$canBeDismissed = canChildBeDismissed;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.onTranslationUpdate(this.val$animView, ((Float) valueAnimator.getAnimatedValue()).floatValue(), this.val$canBeDismissed);
            }
        });
        if (viewTranslationAnimator == null) {
            return;
        }
        viewTranslationAnimator.setDuration(150L);
        viewTranslationAnimator.addListener(new AnimatorListenerAdapter(this, view, canChildBeDismissed, f) { // from class: com.android.systemui.SwipeHelper.5
            final SwipeHelper this$0;
            final View val$animView;
            final boolean val$canBeDismissed;
            final float val$targetLeft;

            {
                this.this$0 = this;
                this.val$animView = view;
                this.val$canBeDismissed = canChildBeDismissed;
                this.val$targetLeft = f;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mSnappingChild = false;
                this.this$0.updateSwipeProgressFromOffset(this.val$animView, this.val$canBeDismissed);
                this.this$0.mCallback.onChildSnappedBack(this.val$animView, this.val$targetLeft);
            }
        });
        prepareSnapBackAnimation(view, viewTranslationAnimator);
        this.mSnappingChild = true;
        viewTranslationAnimator.start();
    }

    public void snapChildIfNeeded(View view, boolean z, float f) {
        if ((this.mDragging && this.mCurrView == view) || this.mSnappingChild) {
            return;
        }
        boolean z2 = false;
        Animator animator = this.mDismissPendingMap.get(view);
        if (animator != null) {
            z2 = true;
            animator.cancel();
        } else if (getTranslation(view) != 0.0f) {
            z2 = true;
        }
        if (z2) {
            if (z) {
                snapChild(view, f, 0.0f);
            } else {
                snapChildInstantly(view);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean swipedFarEnough() {
        return ((double) Math.abs(getTranslation(this.mCurrView))) > ((double) getSize(this.mCurrView)) * 0.4d;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean swipedFastEnough() {
        boolean z = true;
        float velocity = getVelocity(this.mVelocityTracker);
        float translation = getTranslation(this.mCurrView);
        if (Math.abs(velocity) > getEscapeVelocity()) {
            if ((velocity > 0.0f) != (translation > 0.0f)) {
                z = false;
            }
        } else {
            z = false;
        }
        return z;
    }
}
