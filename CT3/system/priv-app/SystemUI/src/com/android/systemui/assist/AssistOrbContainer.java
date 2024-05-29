package com.android.systemui.assist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.Interpolators;
/* loaded from: a.zip:com/android/systemui/assist/AssistOrbContainer.class */
public class AssistOrbContainer extends FrameLayout {
    private boolean mAnimatingOut;
    private View mNavbarScrim;
    private AssistOrbView mOrb;
    private View mScrim;

    public AssistOrbContainer(Context context) {
        this(context, null);
    }

    public AssistOrbContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AssistOrbContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    private void reset() {
        this.mAnimatingOut = false;
        this.mOrb.reset();
        this.mScrim.setAlpha(1.0f);
        this.mNavbarScrim.setAlpha(1.0f);
    }

    private void startEnterAnimation() {
        if (this.mAnimatingOut) {
            return;
        }
        this.mOrb.startEnterAnimation();
        this.mScrim.setAlpha(0.0f);
        this.mNavbarScrim.setAlpha(0.0f);
        post(new Runnable(this) { // from class: com.android.systemui.assist.AssistOrbContainer.2
            final AssistOrbContainer this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mScrim.animate().alpha(1.0f).setDuration(300L).setStartDelay(0L).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
                this.this$0.mNavbarScrim.animate().alpha(1.0f).setDuration(300L).setStartDelay(0L).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
            }
        });
    }

    private void startExitAnimation(Runnable runnable) {
        if (this.mAnimatingOut) {
            if (runnable != null) {
                runnable.run();
                return;
            }
            return;
        }
        this.mAnimatingOut = true;
        this.mOrb.startExitAnimation(150L);
        this.mScrim.animate().alpha(0.0f).setDuration(250L).setStartDelay(150L).setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mNavbarScrim.animate().alpha(0.0f).setDuration(250L).setStartDelay(150L).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).withEndAction(runnable);
    }

    public AssistOrbView getOrb() {
        return this.mOrb;
    }

    public boolean isShowing() {
        boolean z = false;
        if (getVisibility() == 0) {
            z = !this.mAnimatingOut;
        }
        return z;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mScrim = findViewById(2131886251);
        this.mNavbarScrim = findViewById(2131886254);
        this.mOrb = (AssistOrbView) findViewById(2131886252);
    }

    public void show(boolean z, boolean z2) {
        if (!z) {
            if (z2) {
                startExitAnimation(new Runnable(this) { // from class: com.android.systemui.assist.AssistOrbContainer.1
                    final AssistOrbContainer this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.mAnimatingOut = false;
                        this.this$0.setVisibility(8);
                    }
                });
            } else {
                setVisibility(8);
            }
        } else if (getVisibility() != 0) {
            setVisibility(0);
            if (z2) {
                startEnterAnimation();
            } else {
                reset();
            }
        }
    }
}
