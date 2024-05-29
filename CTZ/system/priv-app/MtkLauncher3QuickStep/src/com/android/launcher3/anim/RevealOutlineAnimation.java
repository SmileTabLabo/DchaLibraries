package com.android.launcher3.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewOutlineProvider;
import com.android.launcher3.Utilities;
/* loaded from: classes.dex */
public abstract class RevealOutlineAnimation extends ViewOutlineProvider {
    protected Rect mOutline = new Rect();
    protected float mOutlineRadius;

    abstract void setProgress(float f);

    abstract boolean shouldRemoveElevationDuringAnimation();

    public ValueAnimator createRevealAnimator(final View view, boolean z) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(z ? new float[]{1.0f, 0.0f} : new float[]{0.0f, 1.0f});
        final float elevation = view.getElevation();
        ofFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.launcher3.anim.RevealOutlineAnimation.1
            private boolean mIsClippedToOutline;
            private ViewOutlineProvider mOldOutlineProvider;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                this.mIsClippedToOutline = view.getClipToOutline();
                this.mOldOutlineProvider = view.getOutlineProvider();
                view.setOutlineProvider(RevealOutlineAnimation.this);
                view.setClipToOutline(true);
                if (RevealOutlineAnimation.this.shouldRemoveElevationDuringAnimation()) {
                    view.setTranslationZ(-elevation);
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                view.setOutlineProvider(this.mOldOutlineProvider);
                view.setClipToOutline(this.mIsClippedToOutline);
                if (RevealOutlineAnimation.this.shouldRemoveElevationDuringAnimation()) {
                    view.setTranslationZ(0.0f);
                }
            }
        });
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.launcher3.anim.RevealOutlineAnimation.2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                RevealOutlineAnimation.this.setProgress(((Float) valueAnimator.getAnimatedValue()).floatValue());
                view.invalidateOutline();
                if (!Utilities.ATLEAST_LOLLIPOP_MR1) {
                    view.invalidate();
                }
            }
        });
        return ofFloat;
    }

    @Override // android.view.ViewOutlineProvider
    public void getOutline(View view, Outline outline) {
        outline.setRoundRect(this.mOutline, this.mOutlineRadius);
    }

    public float getRadius() {
        return this.mOutlineRadius;
    }

    public void getOutline(Rect rect) {
        rect.set(this.mOutline);
    }
}
