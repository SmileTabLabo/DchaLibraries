package android.support.v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.view.TintableBackgroundView;
import android.support.v7.appcompat.R$attr;
import android.util.AttributeSet;
import android.widget.ImageButton;
/* loaded from: a.zip:android/support/v7/widget/AppCompatImageButton.class */
public class AppCompatImageButton extends ImageButton implements TintableBackgroundView {
    private AppCompatBackgroundHelper mBackgroundTintHelper;
    private AppCompatImageHelper mImageHelper;

    public AppCompatImageButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.imageButtonStyle);
    }

    public AppCompatImageButton(Context context, AttributeSet attributeSet, int i) {
        super(TintContextWrapper.wrap(context), attributeSet, i);
        AppCompatDrawableManager appCompatDrawableManager = AppCompatDrawableManager.get();
        this.mBackgroundTintHelper = new AppCompatBackgroundHelper(this, appCompatDrawableManager);
        this.mBackgroundTintHelper.loadFromAttributes(attributeSet, i);
        this.mImageHelper = new AppCompatImageHelper(this, appCompatDrawableManager);
        this.mImageHelper.loadFromAttributes(attributeSet, i);
    }

    @Override // android.widget.ImageView, android.view.View
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mBackgroundTintHelper != null) {
            this.mBackgroundTintHelper.applySupportBackgroundTint();
        }
    }

    @Override // android.support.v4.view.TintableBackgroundView
    @Nullable
    public ColorStateList getSupportBackgroundTintList() {
        ColorStateList colorStateList = null;
        if (this.mBackgroundTintHelper != null) {
            colorStateList = this.mBackgroundTintHelper.getSupportBackgroundTintList();
        }
        return colorStateList;
    }

    @Override // android.support.v4.view.TintableBackgroundView
    @Nullable
    public PorterDuff.Mode getSupportBackgroundTintMode() {
        PorterDuff.Mode mode = null;
        if (this.mBackgroundTintHelper != null) {
            mode = this.mBackgroundTintHelper.getSupportBackgroundTintMode();
        }
        return mode;
    }

    @Override // android.widget.ImageView, android.view.View
    public boolean hasOverlappingRendering() {
        return this.mImageHelper.hasOverlappingRendering() ? super.hasOverlappingRendering() : false;
    }

    @Override // android.view.View
    public void setBackgroundDrawable(Drawable drawable) {
        super.setBackgroundDrawable(drawable);
        if (this.mBackgroundTintHelper != null) {
            this.mBackgroundTintHelper.onSetBackgroundDrawable(drawable);
        }
    }

    @Override // android.view.View
    public void setBackgroundResource(@DrawableRes int i) {
        super.setBackgroundResource(i);
        if (this.mBackgroundTintHelper != null) {
            this.mBackgroundTintHelper.onSetBackgroundResource(i);
        }
    }

    @Override // android.widget.ImageView
    public void setImageResource(@DrawableRes int i) {
        this.mImageHelper.setImageResource(i);
    }

    @Override // android.support.v4.view.TintableBackgroundView
    public void setSupportBackgroundTintList(@Nullable ColorStateList colorStateList) {
        if (this.mBackgroundTintHelper != null) {
            this.mBackgroundTintHelper.setSupportBackgroundTintList(colorStateList);
        }
    }

    @Override // android.support.v4.view.TintableBackgroundView
    public void setSupportBackgroundTintMode(@Nullable PorterDuff.Mode mode) {
        if (this.mBackgroundTintHelper != null) {
            this.mBackgroundTintHelper.setSupportBackgroundTintMode(mode);
        }
    }
}
