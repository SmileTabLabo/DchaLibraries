package com.android.systemui.volume;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
/* loaded from: a.zip:com/android/systemui/volume/VolumeDialogMotion.class */
public class VolumeDialogMotion {
    private static final String TAG = Util.logTag(VolumeDialogMotion.class);
    private boolean mAnimating;
    private final Callback mCallback;
    private final View mChevron;
    private ValueAnimator mChevronPositionAnimator;
    private final ViewGroup mContents;
    private ValueAnimator mContentsPositionAnimator;
    private final Dialog mDialog;
    private final View mDialogView;
    private boolean mDismissing;
    private final Handler mHandler = new Handler();
    private boolean mShowing;

    /* renamed from: com.android.systemui.volume.VolumeDialogMotion$7  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialogMotion$7.class */
    class AnonymousClass7 extends AnimatorListenerAdapter {
        private boolean mCancelled;
        final VolumeDialogMotion this$0;
        final Runnable val$onComplete;

        AnonymousClass7(VolumeDialogMotion volumeDialogMotion, Runnable runnable) {
            this.this$0 = volumeDialogMotion;
            this.val$onComplete = runnable;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
            if (D.BUG) {
                Log.d(VolumeDialogMotion.TAG, "dismiss.onAnimationCancel");
            }
            this.mCancelled = true;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            if (this.mCancelled) {
                return;
            }
            if (D.BUG) {
                Log.d(VolumeDialogMotion.TAG, "dismiss.onAnimationEnd");
            }
            this.this$0.mHandler.postDelayed(new Runnable(this, this.val$onComplete) { // from class: com.android.systemui.volume.VolumeDialogMotion.7.1
                final AnonymousClass7 this$1;
                final Runnable val$onComplete;

                {
                    this.this$1 = this;
                    this.val$onComplete = r5;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (D.BUG) {
                        Log.d(VolumeDialogMotion.TAG, "mDialog.dismiss()");
                    }
                    this.this$1.this$0.mDialog.dismiss();
                    this.val$onComplete.run();
                    this.this$1.this$0.setDismissing(false);
                }
            }, 50L);
        }
    }

    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialogMotion$Callback.class */
    public interface Callback {
        void onAnimatingChanged(boolean z);
    }

    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialogMotion$LogAccelerateInterpolator.class */
    private static final class LogAccelerateInterpolator implements TimeInterpolator {
        private final int mBase;
        private final int mDrift;
        private final float mLogScale;

        private LogAccelerateInterpolator() {
            this(100, 0);
        }

        private LogAccelerateInterpolator(int i, int i2) {
            this.mBase = i;
            this.mDrift = i2;
            this.mLogScale = 1.0f / computeLog(1.0f, this.mBase, this.mDrift);
        }

        /* synthetic */ LogAccelerateInterpolator(LogAccelerateInterpolator logAccelerateInterpolator) {
            this();
        }

        private static float computeLog(float f, int i, int i2) {
            return ((float) (-Math.pow(i, -f))) + 1.0f + (i2 * f);
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            return 1.0f - (computeLog(1.0f - f, this.mBase, this.mDrift) * this.mLogScale);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialogMotion$LogDecelerateInterpolator.class */
    public static final class LogDecelerateInterpolator implements TimeInterpolator {
        private final float mBase;
        private final float mDrift;
        private final float mOutputScale;
        private final float mTimeScale;

        private LogDecelerateInterpolator() {
            this(400.0f, 1.4f, 0.0f);
        }

        private LogDecelerateInterpolator(float f, float f2, float f3) {
            this.mBase = f;
            this.mDrift = f3;
            this.mTimeScale = 1.0f / f2;
            this.mOutputScale = 1.0f / computeLog(1.0f);
        }

        /* synthetic */ LogDecelerateInterpolator(LogDecelerateInterpolator logDecelerateInterpolator) {
            this();
        }

        private float computeLog(float f) {
            return (1.0f - ((float) Math.pow(this.mBase, (-f) * this.mTimeScale))) + (this.mDrift * f);
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            return computeLog(f) * this.mOutputScale;
        }
    }

    public VolumeDialogMotion(Dialog dialog, View view, ViewGroup viewGroup, View view2, Callback callback) {
        this.mDialog = dialog;
        this.mDialogView = view;
        this.mContents = viewGroup;
        this.mChevron = view2;
        this.mCallback = callback;
        this.mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(this) { // from class: com.android.systemui.volume.VolumeDialogMotion.1
            final VolumeDialogMotion this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialogInterface) {
                if (D.BUG) {
                    Log.d(VolumeDialogMotion.TAG, "mDialog.onDismiss");
                }
            }
        });
        this.mDialog.setOnShowListener(new DialogInterface.OnShowListener(this) { // from class: com.android.systemui.volume.VolumeDialogMotion.2
            final VolumeDialogMotion this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.DialogInterface.OnShowListener
            public void onShow(DialogInterface dialogInterface) {
                if (D.BUG) {
                    Log.d(VolumeDialogMotion.TAG, "mDialog.onShow");
                }
                this.this$0.mDialogView.setTranslationY(-this.this$0.mDialogView.getHeight());
                this.this$0.startShowAnimation();
            }
        });
    }

    private int chevronDistance() {
        return this.mChevron.getHeight() / 6;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int chevronPosY() {
        Integer num = null;
        if (this.mChevron != null) {
            num = this.mChevron.getTag();
        }
        return num == null ? 0 : num.intValue();
    }

    private static int scaledDuration(int i) {
        return (int) (i * 1.0f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDismissing(boolean z) {
        if (z == this.mDismissing) {
            return;
        }
        this.mDismissing = z;
        if (D.BUG) {
            Log.d(TAG, "mDismissing = " + this.mDismissing);
        }
        updateAnimating();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setShowing(boolean z) {
        if (z == this.mShowing) {
            return;
        }
        this.mShowing = z;
        if (D.BUG) {
            Log.d(TAG, "mShowing = " + this.mShowing);
        }
        updateAnimating();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startShowAnimation() {
        if (D.BUG) {
            Log.d(TAG, "startShowAnimation");
        }
        this.mDialogView.animate().translationY(0.0f).setDuration(scaledDuration(300)).setInterpolator(new LogDecelerateInterpolator(null)).setListener(null).setUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.volume.VolumeDialogMotion.3
            final VolumeDialogMotion this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (this.this$0.mChevronPositionAnimator == null) {
                    return;
                }
                this.this$0.mChevron.setTranslationY(this.this$0.chevronPosY() + ((Float) this.this$0.mChevronPositionAnimator.getAnimatedValue()).floatValue() + (-this.this$0.mDialogView.getTranslationY()));
            }
        }).start();
        this.mContentsPositionAnimator = ValueAnimator.ofFloat(-chevronDistance(), 0.0f).setDuration(scaledDuration(400));
        this.mContentsPositionAnimator.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.volume.VolumeDialogMotion.4
            private boolean mCancelled;
            final VolumeDialogMotion this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                if (D.BUG) {
                    Log.d(VolumeDialogMotion.TAG, "show.onAnimationCancel");
                }
                this.mCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.mCancelled) {
                    return;
                }
                if (D.BUG) {
                    Log.d(VolumeDialogMotion.TAG, "show.onAnimationEnd");
                }
                this.this$0.setShowing(false);
            }
        });
        this.mContentsPositionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.volume.VolumeDialogMotion.5
            final VolumeDialogMotion this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.mContents.setTranslationY((-this.this$0.mDialogView.getTranslationY()) + ((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        this.mContentsPositionAnimator.setInterpolator(new LogDecelerateInterpolator(null));
        this.mContentsPositionAnimator.start();
        this.mContents.setAlpha(0.0f);
        this.mContents.animate().alpha(1.0f).setDuration(scaledDuration(150)).setInterpolator(new PathInterpolator(0.0f, 0.0f, 0.2f, 1.0f)).start();
        this.mChevronPositionAnimator = ValueAnimator.ofFloat(-chevronDistance(), 0.0f).setDuration(scaledDuration(250));
        this.mChevronPositionAnimator.setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f));
        this.mChevronPositionAnimator.start();
        this.mChevron.setAlpha(0.0f);
        this.mChevron.animate().alpha(1.0f).setStartDelay(scaledDuration(50)).setDuration(scaledDuration(150)).setInterpolator(new PathInterpolator(0.4f, 0.0f, 1.0f, 1.0f)).start();
    }

    private void updateAnimating() {
        boolean z = !this.mShowing ? this.mDismissing : true;
        if (z == this.mAnimating) {
            return;
        }
        this.mAnimating = z;
        if (D.BUG) {
            Log.d(TAG, "mAnimating = " + this.mAnimating);
        }
        if (this.mCallback != null) {
            this.mCallback.onAnimatingChanged(this.mAnimating);
        }
    }

    public boolean isAnimating() {
        return this.mAnimating;
    }

    public void startDismiss(Runnable runnable) {
        if (D.BUG) {
            Log.d(TAG, "startDismiss");
        }
        if (this.mDismissing) {
            return;
        }
        setDismissing(true);
        if (this.mShowing) {
            this.mDialogView.animate().cancel();
            if (this.mContentsPositionAnimator != null) {
                this.mContentsPositionAnimator.cancel();
            }
            this.mContents.animate().cancel();
            if (this.mChevronPositionAnimator != null) {
                this.mChevronPositionAnimator.cancel();
            }
            this.mChevron.animate().cancel();
            setShowing(false);
        }
        this.mDialogView.animate().translationY(-this.mDialogView.getHeight()).setDuration(scaledDuration(250)).setInterpolator(new LogAccelerateInterpolator(null)).setUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.volume.VolumeDialogMotion.6
            final VolumeDialogMotion this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.mContents.setTranslationY(-this.this$0.mDialogView.getTranslationY());
                this.this$0.mChevron.setTranslationY(this.this$0.chevronPosY() + (-this.this$0.mDialogView.getTranslationY()));
            }
        }).setListener(new AnonymousClass7(this, runnable)).start();
    }

    public void startShow() {
        if (D.BUG) {
            Log.d(TAG, "startShow");
        }
        if (this.mShowing) {
            return;
        }
        setShowing(true);
        if (this.mDismissing) {
            this.mDialogView.animate().cancel();
            setDismissing(false);
            startShowAnimation();
            return;
        }
        if (D.BUG) {
            Log.d(TAG, "mDialog.show()");
        }
        this.mDialog.show();
    }
}
