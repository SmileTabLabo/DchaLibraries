package com.android.systemui.pip.phone;

import android.animation.AnimationHandler;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.RectEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.IActivityManager;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.animation.Interpolator;
import com.android.internal.graphics.SfVsyncFrameCallbackProvider;
import com.android.internal.os.SomeArgs;
import com.android.internal.policy.PipSnapAlgorithm;
import com.android.systemui.Interpolators;
import com.android.systemui.recents.misc.ForegroundThread;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.statusbar.FlingAnimationUtils;
import java.io.PrintWriter;
/* loaded from: classes.dex */
public class PipMotionHelper implements Handler.Callback {
    private static final RectEvaluator RECT_EVALUATOR = new RectEvaluator(new Rect());
    private IActivityManager mActivityManager;
    private Context mContext;
    private FlingAnimationUtils mFlingAnimationUtils;
    private PipMenuActivityController mMenuController;
    private PipSnapAlgorithm mSnapAlgorithm;
    private final Rect mBounds = new Rect();
    private final Rect mStableInsets = new Rect();
    private ValueAnimator mBoundsAnimator = null;
    private Handler mHandler = new Handler(ForegroundThread.get().getLooper(), this);
    private AnimationHandler mAnimationHandler = new AnimationHandler();

    public PipMotionHelper(Context context, IActivityManager iActivityManager, PipMenuActivityController pipMenuActivityController, PipSnapAlgorithm pipSnapAlgorithm, FlingAnimationUtils flingAnimationUtils) {
        this.mContext = context;
        this.mActivityManager = iActivityManager;
        this.mMenuController = pipMenuActivityController;
        this.mSnapAlgorithm = pipSnapAlgorithm;
        this.mFlingAnimationUtils = flingAnimationUtils;
        this.mAnimationHandler.setProvider(new SfVsyncFrameCallbackProvider());
        onConfigurationChanged();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onConfigurationChanged() {
        this.mSnapAlgorithm.onConfigurationChanged();
        SystemServicesProxy.getInstance(this.mContext).getStableInsets(this.mStableInsets);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void synchronizePinnedStackBounds() {
        cancelAnimations();
        try {
            ActivityManager.StackInfo stackInfo = this.mActivityManager.getStackInfo(2, 0);
            if (stackInfo != null) {
                this.mBounds.set(stackInfo.bounds);
            }
        } catch (RemoteException e) {
            Log.w("PipMotionHelper", "Failed to get pinned stack bounds");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void movePip(Rect rect) {
        cancelAnimations();
        resizePipUnchecked(rect);
        this.mBounds.set(rect);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void expandPip() {
        expandPip(false);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void expandPip(final boolean z) {
        cancelAnimations();
        this.mMenuController.hideMenuWithoutResize();
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.-$$Lambda$PipMotionHelper$sKxCzHQTJVfrtc--kVVtTIgcND4
            @Override // java.lang.Runnable
            public final void run() {
                PipMotionHelper.lambda$expandPip$0(PipMotionHelper.this, z);
            }
        });
    }

    public static /* synthetic */ void lambda$expandPip$0(PipMotionHelper pipMotionHelper, boolean z) {
        try {
            pipMotionHelper.mActivityManager.dismissPip(!z, 300);
        } catch (RemoteException e) {
            Log.e("PipMotionHelper", "Error expanding PiP activity", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void dismissPip() {
        cancelAnimations();
        this.mMenuController.hideMenuWithoutResize();
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.-$$Lambda$PipMotionHelper$ExBmB11pCWcEFXztVKlantVNH0o
            @Override // java.lang.Runnable
            public final void run() {
                PipMotionHelper.lambda$dismissPip$1(PipMotionHelper.this);
            }
        });
    }

    public static /* synthetic */ void lambda$dismissPip$1(PipMotionHelper pipMotionHelper) {
        try {
            pipMotionHelper.mActivityManager.removeStacksInWindowingModes(new int[]{2});
        } catch (RemoteException e) {
            Log.e("PipMotionHelper", "Failed to remove PiP", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Rect getBounds() {
        return this.mBounds;
    }

    Rect getClosestMinimizedBounds(Rect rect, Rect rect2) {
        Point point = new Point();
        this.mContext.getDisplay().getRealSize(point);
        Rect findClosestSnapBounds = this.mSnapAlgorithm.findClosestSnapBounds(rect2, rect);
        this.mSnapAlgorithm.applyMinimizedOffset(findClosestSnapBounds, rect2, point, this.mStableInsets);
        return findClosestSnapBounds;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean shouldDismissPip() {
        Point point = new Point();
        this.mContext.getDisplay().getRealSize(point);
        int i = point.y - this.mStableInsets.bottom;
        return this.mBounds.bottom > i && ((float) (this.mBounds.bottom - i)) / ((float) this.mBounds.height()) >= 0.3f;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Rect animateToClosestMinimizedState(Rect rect, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        cancelAnimations();
        Rect closestMinimizedBounds = getClosestMinimizedBounds(this.mBounds, rect);
        if (!this.mBounds.equals(closestMinimizedBounds)) {
            this.mBoundsAnimator = createAnimationToBounds(this.mBounds, closestMinimizedBounds, 200, Interpolators.LINEAR_OUT_SLOW_IN);
            if (animatorUpdateListener != null) {
                this.mBoundsAnimator.addUpdateListener(animatorUpdateListener);
            }
            this.mBoundsAnimator.start();
        }
        return closestMinimizedBounds;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Rect flingToSnapTarget(float f, float f2, float f3, Rect rect, ValueAnimator.AnimatorUpdateListener animatorUpdateListener, Animator.AnimatorListener animatorListener, Point point) {
        cancelAnimations();
        Rect findClosestSnapBounds = this.mSnapAlgorithm.findClosestSnapBounds(rect, this.mBounds, f2, f3, point);
        if (!this.mBounds.equals(findClosestSnapBounds)) {
            this.mBoundsAnimator = createAnimationToBounds(this.mBounds, findClosestSnapBounds, 0, Interpolators.FAST_OUT_SLOW_IN);
            this.mFlingAnimationUtils.apply(this.mBoundsAnimator, 0.0f, distanceBetweenRectOffsets(this.mBounds, findClosestSnapBounds), f);
            if (animatorUpdateListener != null) {
                this.mBoundsAnimator.addUpdateListener(animatorUpdateListener);
            }
            if (animatorListener != null) {
                this.mBoundsAnimator.addListener(animatorListener);
            }
            this.mBoundsAnimator.start();
        }
        return findClosestSnapBounds;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Rect animateToClosestSnapTarget(Rect rect, ValueAnimator.AnimatorUpdateListener animatorUpdateListener, Animator.AnimatorListener animatorListener) {
        cancelAnimations();
        Rect findClosestSnapBounds = this.mSnapAlgorithm.findClosestSnapBounds(rect, this.mBounds);
        if (!this.mBounds.equals(findClosestSnapBounds)) {
            this.mBoundsAnimator = createAnimationToBounds(this.mBounds, findClosestSnapBounds, 225, Interpolators.FAST_OUT_SLOW_IN);
            if (animatorUpdateListener != null) {
                this.mBoundsAnimator.addUpdateListener(animatorUpdateListener);
            }
            if (animatorListener != null) {
                this.mBoundsAnimator.addListener(animatorListener);
            }
            this.mBoundsAnimator.start();
        }
        return findClosestSnapBounds;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float animateToExpandedState(Rect rect, Rect rect2, Rect rect3) {
        float snapFraction = this.mSnapAlgorithm.getSnapFraction(new Rect(this.mBounds), rect2);
        this.mSnapAlgorithm.applySnapFraction(rect, rect3, snapFraction);
        resizeAndAnimatePipUnchecked(rect, 250);
        return snapFraction;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void animateToUnexpandedState(Rect rect, float f, Rect rect2, Rect rect3, boolean z, boolean z2) {
        if (f < 0.0f) {
            f = this.mSnapAlgorithm.getSnapFraction(new Rect(this.mBounds), rect3);
        }
        this.mSnapAlgorithm.applySnapFraction(rect, rect2, f);
        if (z) {
            rect = getClosestMinimizedBounds(rect, rect2);
        }
        if (z2) {
            movePip(rect);
        } else {
            resizeAndAnimatePipUnchecked(rect, 250);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void animateToOffset(Rect rect) {
        cancelAnimations();
        resizeAndAnimatePipUnchecked(rect, 300);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Rect animateDismiss(Rect rect, float f, float f2, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        cancelAnimations();
        float length = PointF.length(f, f2);
        boolean z = length > this.mFlingAnimationUtils.getMinVelocityPxPerSecond();
        Point dismissEndPoint = getDismissEndPoint(rect, f, f2, z);
        Rect rect2 = new Rect(rect);
        rect2.offsetTo(dismissEndPoint.x, dismissEndPoint.y);
        this.mBoundsAnimator = createAnimationToBounds(this.mBounds, rect2, 175, Interpolators.FAST_OUT_LINEAR_IN);
        this.mBoundsAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.pip.phone.PipMotionHelper.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                PipMotionHelper.this.dismissPip();
            }
        });
        if (z) {
            this.mFlingAnimationUtils.apply(this.mBoundsAnimator, 0.0f, distanceBetweenRectOffsets(this.mBounds, rect2), length);
        }
        if (animatorUpdateListener != null) {
            this.mBoundsAnimator.addUpdateListener(animatorUpdateListener);
        }
        this.mBoundsAnimator.start();
        return rect2;
    }

    void cancelAnimations() {
        if (this.mBoundsAnimator != null) {
            this.mBoundsAnimator.cancel();
            this.mBoundsAnimator = null;
        }
    }

    private ValueAnimator createAnimationToBounds(Rect rect, Rect rect2, int i, Interpolator interpolator) {
        ValueAnimator valueAnimator = new ValueAnimator() { // from class: com.android.systemui.pip.phone.PipMotionHelper.2
            public AnimationHandler getAnimationHandler() {
                return PipMotionHelper.this.mAnimationHandler;
            }
        };
        valueAnimator.setObjectValues(rect, rect2);
        valueAnimator.setEvaluator(RECT_EVALUATOR);
        valueAnimator.setDuration(i);
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.pip.phone.-$$Lambda$PipMotionHelper$UijvXdqv_A_f2ZSKr4tqG6uf9mk
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                PipMotionHelper.this.resizePipUnchecked((Rect) valueAnimator2.getAnimatedValue());
            }
        });
        return valueAnimator;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resizePipUnchecked(Rect rect) {
        if (!rect.equals(this.mBounds)) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = rect;
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, obtain));
        }
    }

    private void resizeAndAnimatePipUnchecked(Rect rect, int i) {
        if (!rect.equals(this.mBounds)) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = rect;
            obtain.argi1 = i;
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2, obtain));
        }
    }

    private Point getDismissEndPoint(Rect rect, float f, float f2, boolean z) {
        Point point = new Point();
        this.mContext.getDisplay().getRealSize(point);
        float height = point.y + (rect.height() * 0.1f);
        if (z && f != 0.0f && f2 != 0.0f) {
            float f3 = f2 / f;
            return new Point((int) ((height - (rect.top - (rect.left * f3))) / f3), (int) height);
        }
        return new Point(rect.left, (int) height);
    }

    private float distanceBetweenRectOffsets(Rect rect, Rect rect2) {
        return PointF.length(rect.left - rect2.left, rect.top - rect2.top);
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message message) {
        ActivityManager.StackInfo stackInfo;
        switch (message.what) {
            case 1:
                Rect rect = (Rect) ((SomeArgs) message.obj).arg1;
                try {
                    this.mActivityManager.resizePinnedStack(rect, (Rect) null);
                    this.mBounds.set(rect);
                } catch (RemoteException e) {
                    Log.e("PipMotionHelper", "Could not resize pinned stack to bounds: " + rect, e);
                }
                return true;
            case 2:
                SomeArgs someArgs = (SomeArgs) message.obj;
                Rect rect2 = (Rect) someArgs.arg1;
                int i = someArgs.argi1;
                try {
                    stackInfo = this.mActivityManager.getStackInfo(2, 0);
                } catch (RemoteException e2) {
                    Log.e("PipMotionHelper", "Could not animate resize pinned stack to bounds: " + rect2, e2);
                }
                if (stackInfo == null) {
                    return true;
                }
                this.mActivityManager.resizeStack(stackInfo.stackId, rect2, false, true, true, i);
                this.mBounds.set(rect2);
                return true;
            default:
                return false;
        }
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + "PipMotionHelper");
        printWriter.println(str2 + "mBounds=" + this.mBounds);
        printWriter.println(str2 + "mStableInsets=" + this.mStableInsets);
    }
}
