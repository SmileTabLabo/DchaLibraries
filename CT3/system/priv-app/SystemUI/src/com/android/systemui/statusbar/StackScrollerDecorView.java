package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
/* loaded from: a.zip:com/android/systemui/statusbar/StackScrollerDecorView.class */
public abstract class StackScrollerDecorView extends ExpandableView {
    private boolean mAnimating;
    protected View mContent;
    private boolean mIsVisible;

    public StackScrollerDecorView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void animateText(boolean z, Runnable runnable) {
        if (z == this.mIsVisible) {
            if (runnable != null) {
                runnable.run();
                return;
            }
            return;
        }
        float f = z ? 1.0f : 0.0f;
        Interpolator interpolator = z ? Interpolators.ALPHA_IN : Interpolators.ALPHA_OUT;
        this.mAnimating = true;
        this.mContent.animate().alpha(f).setInterpolator(interpolator).setDuration(260L).withEndAction(new Runnable(this, runnable) { // from class: com.android.systemui.statusbar.StackScrollerDecorView.1
            final StackScrollerDecorView this$0;
            final Runnable val$onFinishedRunnable;

            {
                this.this$0 = this;
                this.val$onFinishedRunnable = runnable;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mAnimating = false;
                if (this.val$onFinishedRunnable != null) {
                    this.val$onFinishedRunnable.run();
                }
            }
        });
        this.mIsVisible = z;
    }

    public void cancelAnimation() {
        this.mContent.animate().cancel();
    }

    protected abstract View findContentView();

    @Override // com.android.systemui.statusbar.ExpandableView, android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public boolean isTransparent() {
        return true;
    }

    public boolean isVisible() {
        return !this.mIsVisible ? this.mAnimating : true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mContent = findContentView();
        setInvisible();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.ExpandableView, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        setOutlineProvider(null);
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void performAddAnimation(long j, long j2) {
        performVisibilityAnimation(true);
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void performRemoveAnimation(long j, float f, Runnable runnable) {
        performVisibilityAnimation(false);
    }

    public void performVisibilityAnimation(boolean z) {
        animateText(z, null);
    }

    public void performVisibilityAnimation(boolean z, Runnable runnable) {
        animateText(z, runnable);
    }

    public void setInvisible() {
        this.mContent.setAlpha(0.0f);
        this.mIsVisible = false;
    }
}
