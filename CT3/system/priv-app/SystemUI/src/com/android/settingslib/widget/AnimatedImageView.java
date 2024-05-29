package com.android.settingslib.widget;

import android.content.Context;
import android.graphics.drawable.AnimatedRotateDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
/* loaded from: a.zip:com/android/settingslib/widget/AnimatedImageView.class */
public class AnimatedImageView extends ImageView {
    private boolean mAnimating;
    private AnimatedRotateDrawable mDrawable;

    public AnimatedImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void updateAnimating() {
        if (this.mDrawable != null) {
            if (isShown() && this.mAnimating) {
                this.mDrawable.start();
            } else {
                this.mDrawable.stop();
            }
        }
    }

    private void updateDrawable() {
        if (isShown() && this.mDrawable != null) {
            this.mDrawable.stop();
        }
        AnimatedRotateDrawable drawable = getDrawable();
        if (!(drawable instanceof AnimatedRotateDrawable)) {
            this.mDrawable = null;
            return;
        }
        this.mDrawable = drawable;
        this.mDrawable.setFramesCount(56);
        this.mDrawable.setFramesDuration(32);
        if (isShown() && this.mAnimating) {
            this.mDrawable.start();
        }
    }

    @Override // android.widget.ImageView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateAnimating();
    }

    @Override // android.widget.ImageView, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        updateAnimating();
    }

    @Override // android.view.View
    protected void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        updateAnimating();
    }

    @Override // android.widget.ImageView
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        updateDrawable();
    }

    @Override // android.widget.ImageView
    public void setImageResource(int i) {
        super.setImageResource(i);
        updateDrawable();
    }
}
