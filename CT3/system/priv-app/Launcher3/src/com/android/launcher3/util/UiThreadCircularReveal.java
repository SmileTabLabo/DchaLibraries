package com.android.launcher3.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.view.View;
import android.view.ViewOutlineProvider;
import com.android.launcher3.Utilities;
@TargetApi(21)
/* loaded from: a.zip:com/android/launcher3/util/UiThreadCircularReveal.class */
public class UiThreadCircularReveal {
    public static ValueAnimator createCircularReveal(View view, int i, int i2, float f, float f2) {
        return createCircularReveal(view, i, i2, f, f2, ViewOutlineProvider.BACKGROUND);
    }

    public static ValueAnimator createCircularReveal(View view, int i, int i2, float f, float f2, ViewOutlineProvider viewOutlineProvider) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        RevealOutlineProvider revealOutlineProvider = new RevealOutlineProvider(i, i2, f, f2);
        ofFloat.addListener(new AnimatorListenerAdapter(view, revealOutlineProvider, view.getElevation(), viewOutlineProvider) { // from class: com.android.launcher3.util.UiThreadCircularReveal.1
            final float val$elevation;
            final ViewOutlineProvider val$originalProvider;
            final RevealOutlineProvider val$outlineProvider;
            final View val$revealView;

            {
                this.val$revealView = view;
                this.val$outlineProvider = revealOutlineProvider;
                this.val$elevation = r6;
                this.val$originalProvider = viewOutlineProvider;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.val$revealView.setOutlineProvider(this.val$originalProvider);
                this.val$revealView.setClipToOutline(false);
                this.val$revealView.setTranslationZ(0.0f);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                this.val$revealView.setOutlineProvider(this.val$outlineProvider);
                this.val$revealView.setClipToOutline(true);
                this.val$revealView.setTranslationZ(-this.val$elevation);
            }
        });
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(revealOutlineProvider, view) { // from class: com.android.launcher3.util.UiThreadCircularReveal.2
            final RevealOutlineProvider val$outlineProvider;
            final View val$revealView;

            {
                this.val$outlineProvider = revealOutlineProvider;
                this.val$revealView = view;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.val$outlineProvider.setProgress(valueAnimator.getAnimatedFraction());
                this.val$revealView.invalidateOutline();
                if (Utilities.ATLEAST_LOLLIPOP_MR1) {
                    return;
                }
                this.val$revealView.invalidate();
            }
        });
        return ofFloat;
    }
}
