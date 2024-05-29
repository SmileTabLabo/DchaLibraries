package com.android.systemui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.View;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/ViewInvertHelper.class */
public class ViewInvertHelper {
    private final Paint mDarkPaint;
    private final long mFadeDuration;
    private final ColorMatrix mGrayscaleMatrix;
    private final ColorMatrix mMatrix;
    private final ArrayList<View> mTargets;

    public ViewInvertHelper(Context context, long j) {
        this.mDarkPaint = new Paint();
        this.mMatrix = new ColorMatrix();
        this.mGrayscaleMatrix = new ColorMatrix();
        this.mTargets = new ArrayList<>();
        this.mFadeDuration = j;
    }

    public ViewInvertHelper(View view, long j) {
        this(view.getContext(), j);
        addTarget(view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateInvertPaint(float f) {
        float f2 = 1.0f - (2.0f * f);
        this.mMatrix.set(new float[]{f2, 0.0f, 0.0f, 0.0f, 255.0f * f, 0.0f, f2, 0.0f, 0.0f, 255.0f * f, 0.0f, 0.0f, f2, 0.0f, 255.0f * f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
        this.mGrayscaleMatrix.setSaturation(1.0f - f);
        this.mMatrix.preConcat(this.mGrayscaleMatrix);
        this.mDarkPaint.setColorFilter(new ColorMatrixColorFilter(this.mMatrix));
    }

    public void addTarget(View view) {
        this.mTargets.add(view);
    }

    public void clearTargets() {
        this.mTargets.clear();
    }

    public void fade(boolean z, long j) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(z ? 0.0f : 1.0f, z ? 1.0f : 0.0f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.ViewInvertHelper.1
            final ViewInvertHelper this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.updateInvertPaint(((Float) valueAnimator.getAnimatedValue()).floatValue());
                for (int i = 0; i < this.this$0.mTargets.size(); i++) {
                    ((View) this.this$0.mTargets.get(i)).setLayerType(2, this.this$0.mDarkPaint);
                }
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter(this, z) { // from class: com.android.systemui.ViewInvertHelper.2
            final ViewInvertHelper this$0;
            final boolean val$invert;

            {
                this.this$0 = this;
                this.val$invert = z;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.val$invert) {
                    return;
                }
                for (int i = 0; i < this.this$0.mTargets.size(); i++) {
                    ((View) this.this$0.mTargets.get(i)).setLayerType(0, null);
                }
            }
        });
        ofFloat.setDuration(this.mFadeDuration);
        ofFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        ofFloat.setStartDelay(j);
        ofFloat.start();
    }

    public void setInverted(boolean z, boolean z2, long j) {
        if (z2) {
            fade(z, j);
        } else {
            update(z);
        }
    }

    public void update(boolean z) {
        if (!z) {
            for (int i = 0; i < this.mTargets.size(); i++) {
                this.mTargets.get(i).setLayerType(0, null);
            }
            return;
        }
        updateInvertPaint(1.0f);
        for (int i2 = 0; i2 < this.mTargets.size(); i2++) {
            this.mTargets.get(i2).setLayerType(2, this.mDarkPaint);
        }
    }
}
