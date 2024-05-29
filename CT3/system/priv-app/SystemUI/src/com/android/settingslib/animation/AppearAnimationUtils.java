package com.android.settingslib.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.settingslib.R$dimen;
/* loaded from: a.zip:com/android/settingslib/animation/AppearAnimationUtils.class */
public class AppearAnimationUtils implements AppearAnimationCreator<View> {
    protected boolean mAppearing;
    protected final float mDelayScale;
    private final long mDuration;
    private final Interpolator mInterpolator;
    private final AppearAnimationProperties mProperties;
    protected RowTranslationScaler mRowTranslationScaler;
    private final float mStartTranslation;

    /* loaded from: a.zip:com/android/settingslib/animation/AppearAnimationUtils$AppearAnimationProperties.class */
    public class AppearAnimationProperties {
        public long[][] delays;
        public int maxDelayColIndex;
        public int maxDelayRowIndex;
        final AppearAnimationUtils this$0;

        public AppearAnimationProperties(AppearAnimationUtils appearAnimationUtils) {
            this.this$0 = appearAnimationUtils;
        }
    }

    /* loaded from: a.zip:com/android/settingslib/animation/AppearAnimationUtils$RowTranslationScaler.class */
    public interface RowTranslationScaler {
        float getRowTranslationScale(int i, int i2);
    }

    public AppearAnimationUtils(Context context) {
        this(context, 220L, 1.0f, 1.0f, AnimationUtils.loadInterpolator(context, 17563662));
    }

    public AppearAnimationUtils(Context context, long j, float f, float f2, Interpolator interpolator) {
        this.mProperties = new AppearAnimationProperties(this);
        this.mInterpolator = interpolator;
        this.mStartTranslation = context.getResources().getDimensionPixelOffset(R$dimen.appear_y_translation_start) * f;
        this.mDelayScale = f2;
        this.mDuration = j;
        this.mAppearing = true;
    }

    /* JADX WARN: Type inference failed for: r1v4, types: [long[], long[][]] */
    private <T> AppearAnimationProperties getDelays(T[] tArr) {
        long j = -1;
        this.mProperties.maxDelayColIndex = -1;
        this.mProperties.maxDelayRowIndex = -1;
        this.mProperties.delays = new long[tArr.length];
        int i = 0;
        while (i < tArr.length) {
            this.mProperties.delays[i] = new long[1];
            long calculateDelay = calculateDelay(i, 0);
            this.mProperties.delays[i][0] = calculateDelay;
            long j2 = j;
            if (tArr[i] != null) {
                j2 = j;
                if (calculateDelay > j) {
                    j2 = calculateDelay;
                    this.mProperties.maxDelayColIndex = 0;
                    this.mProperties.maxDelayRowIndex = i;
                }
            }
            i++;
            j = j2;
        }
        return this.mProperties;
    }

    /* JADX WARN: Type inference failed for: r1v4, types: [long[], long[][]] */
    private <T> AppearAnimationProperties getDelays(T[][] tArr) {
        long j = -1;
        this.mProperties.maxDelayColIndex = -1;
        this.mProperties.maxDelayRowIndex = -1;
        this.mProperties.delays = new long[tArr.length];
        for (int i = 0; i < tArr.length; i++) {
            T[] tArr2 = tArr[i];
            this.mProperties.delays[i] = new long[tArr2.length];
            int i2 = 0;
            while (i2 < tArr2.length) {
                long calculateDelay = calculateDelay(i, i2);
                this.mProperties.delays[i][i2] = calculateDelay;
                long j2 = j;
                if (tArr[i][i2] != null) {
                    j2 = j;
                    if (calculateDelay > j) {
                        j2 = calculateDelay;
                        this.mProperties.maxDelayColIndex = i2;
                        this.mProperties.maxDelayRowIndex = i;
                    }
                }
                i2++;
                j = j2;
            }
        }
        return this.mProperties;
    }

    private <T> void startAnimations(AppearAnimationProperties appearAnimationProperties, T[] tArr, Runnable runnable, AppearAnimationCreator<T> appearAnimationCreator) {
        if (appearAnimationProperties.maxDelayRowIndex == -1 || appearAnimationProperties.maxDelayColIndex == -1) {
            runnable.run();
            return;
        }
        for (int i = 0; i < appearAnimationProperties.delays.length; i++) {
            long j = appearAnimationProperties.delays[i][0];
            Runnable runnable2 = null;
            if (appearAnimationProperties.maxDelayRowIndex == i) {
                runnable2 = null;
                if (appearAnimationProperties.maxDelayColIndex == 0) {
                    runnable2 = runnable;
                }
            }
            float rowTranslationScale = (this.mRowTranslationScaler != null ? this.mRowTranslationScaler.getRowTranslationScale(i, appearAnimationProperties.delays.length) : 1.0f) * this.mStartTranslation;
            T t = tArr[i];
            long j2 = this.mDuration;
            if (!this.mAppearing) {
                rowTranslationScale = -rowTranslationScale;
            }
            appearAnimationCreator.createAnimation(t, j, j2, rowTranslationScale, this.mAppearing, this.mInterpolator, runnable2);
        }
    }

    private <T> void startAnimations(AppearAnimationProperties appearAnimationProperties, T[][] tArr, Runnable runnable, AppearAnimationCreator<T> appearAnimationCreator) {
        if (appearAnimationProperties.maxDelayRowIndex == -1 || appearAnimationProperties.maxDelayColIndex == -1) {
            runnable.run();
            return;
        }
        for (int i = 0; i < appearAnimationProperties.delays.length; i++) {
            long[] jArr = appearAnimationProperties.delays[i];
            float rowTranslationScale = (this.mRowTranslationScaler != null ? this.mRowTranslationScaler.getRowTranslationScale(i, appearAnimationProperties.delays.length) : 1.0f) * this.mStartTranslation;
            for (int i2 = 0; i2 < jArr.length; i2++) {
                long j = jArr[i2];
                Runnable runnable2 = null;
                if (appearAnimationProperties.maxDelayRowIndex == i) {
                    runnable2 = null;
                    if (appearAnimationProperties.maxDelayColIndex == i2) {
                        runnable2 = runnable;
                    }
                }
                appearAnimationCreator.createAnimation(tArr[i][i2], j, this.mDuration, this.mAppearing ? rowTranslationScale : -rowTranslationScale, this.mAppearing, this.mInterpolator, runnable2);
            }
        }
    }

    public static void startTranslationYAnimation(View view, long j, long j2, float f, Interpolator interpolator) {
        ObjectAnimator ofFloat;
        if (view.isHardwareAccelerated()) {
            ObjectAnimator renderNodeAnimator = new RenderNodeAnimator(1, f);
            renderNodeAnimator.setTarget(view);
            ofFloat = renderNodeAnimator;
        } else {
            ofFloat = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getTranslationY(), f);
        }
        ofFloat.setInterpolator(interpolator);
        ofFloat.setDuration(j2);
        ofFloat.setStartDelay(j);
        ofFloat.start();
    }

    protected long calculateDelay(int i, int i2) {
        return (long) (((i * 40) + (i2 * (Math.pow(i, 0.4d) + 0.4d) * 20.0d)) * this.mDelayScale);
    }

    @Override // com.android.settingslib.animation.AppearAnimationCreator
    public void createAnimation(View view, long j, long j2, float f, boolean z, Interpolator interpolator, Runnable runnable) {
        ObjectAnimator ofFloat;
        if (view != null) {
            view.setAlpha(z ? 0.0f : 1.0f);
            view.setTranslationY(z ? f : 0.0f);
            float f2 = z ? 1.0f : 0.0f;
            if (view.isHardwareAccelerated()) {
                ofFloat = new RenderNodeAnimator(11, f2);
                ofFloat.setTarget(view);
            } else {
                ofFloat = ObjectAnimator.ofFloat(view, View.ALPHA, view.getAlpha(), f2);
            }
            ofFloat.setInterpolator(interpolator);
            ofFloat.setDuration(j2);
            ofFloat.setStartDelay(j);
            if (view.hasOverlappingRendering()) {
                view.setLayerType(2, null);
                ofFloat.addListener(new AnimatorListenerAdapter(this, view) { // from class: com.android.settingslib.animation.AppearAnimationUtils.1
                    final AppearAnimationUtils this$0;
                    final View val$view;

                    {
                        this.this$0 = this;
                        this.val$view = view;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        this.val$view.setLayerType(0, null);
                    }
                });
            }
            if (runnable != null) {
                ofFloat.addListener(new AnimatorListenerAdapter(this, runnable) { // from class: com.android.settingslib.animation.AppearAnimationUtils.2
                    final AppearAnimationUtils this$0;
                    final Runnable val$endRunnable;

                    {
                        this.this$0 = this;
                        this.val$endRunnable = runnable;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        this.val$endRunnable.run();
                    }
                });
            }
            ofFloat.start();
            if (z) {
                f = 0.0f;
            }
            startTranslationYAnimation(view, j, j2, f, interpolator);
        }
    }

    public Interpolator getInterpolator() {
        return this.mInterpolator;
    }

    public float getStartTranslation() {
        return this.mStartTranslation;
    }

    public void startAnimation(View[] viewArr, Runnable runnable) {
        startAnimation(viewArr, runnable, this);
    }

    public <T> void startAnimation(T[] tArr, Runnable runnable, AppearAnimationCreator<T> appearAnimationCreator) {
        startAnimations(getDelays(tArr), tArr, runnable, appearAnimationCreator);
    }

    public void startAnimation2d(View[][] viewArr, Runnable runnable) {
        startAnimation2d(viewArr, runnable, this);
    }

    public <T> void startAnimation2d(T[][] tArr, Runnable runnable, AppearAnimationCreator<T> appearAnimationCreator) {
        startAnimations(getDelays((Object[][]) tArr), (Object[][]) tArr, runnable, (AppearAnimationCreator) appearAnimationCreator);
    }
}
