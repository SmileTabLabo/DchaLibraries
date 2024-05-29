package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import com.android.systemui.R$styleable;
@RemoteViews.RemoteView
/* loaded from: a.zip:com/android/systemui/statusbar/AnimatedImageView.class */
public class AnimatedImageView extends ImageView {
    AnimationDrawable mAnim;
    boolean mAttached;
    int mDrawableId;
    private final boolean mHasOverlappingRendering;

    public AnimatedImageView(Context context) {
        this(context, null);
    }

    public AnimatedImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R$styleable.AnimatedImageView, 0, 0);
        try {
            this.mHasOverlappingRendering = obtainStyledAttributes.getBoolean(0, true);
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    private void updateAnim() {
        Drawable drawable = getDrawable();
        if (this.mAttached && this.mAnim != null) {
            this.mAnim.stop();
        }
        if (!(drawable instanceof AnimationDrawable)) {
            this.mAnim = null;
            return;
        }
        this.mAnim = (AnimationDrawable) drawable;
        if (isShown()) {
            this.mAnim.start();
        }
    }

    @Override // android.widget.ImageView, android.view.View
    public boolean hasOverlappingRendering() {
        return this.mHasOverlappingRendering;
    }

    @Override // android.widget.ImageView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mAttached = true;
        updateAnim();
    }

    @Override // android.widget.ImageView, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mAnim != null) {
            this.mAnim.stop();
        }
        this.mAttached = false;
    }

    @Override // android.view.View
    protected void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        if (this.mAnim != null) {
            if (isShown()) {
                this.mAnim.start();
            } else {
                this.mAnim.stop();
            }
        }
    }

    @Override // android.widget.ImageView
    public void setImageDrawable(Drawable drawable) {
        if (drawable == null) {
            this.mDrawableId = 0;
        } else if (this.mDrawableId == drawable.hashCode()) {
            return;
        } else {
            this.mDrawableId = drawable.hashCode();
        }
        super.setImageDrawable(drawable);
        updateAnim();
    }

    @Override // android.widget.ImageView
    @RemotableViewMethod
    public void setImageResource(int i) {
        if (this.mDrawableId == i) {
            return;
        }
        this.mDrawableId = i;
        super.setImageResource(i);
        updateAnim();
    }
}
