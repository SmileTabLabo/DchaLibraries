package android.support.v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.view.TintableBackgroundView;
import android.util.AttributeSet;
import android.widget.ImageView;
/* loaded from: classes.dex */
public class AppCompatImageView extends ImageView implements TintableBackgroundView {
    private AppCompatBackgroundHelper mBackgroundTintHelper;
    private AppCompatImageHelper mImageHelper;

    public AppCompatImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(TintContextWrapper.wrap(context), attrs, defStyleAttr);
        AppCompatDrawableManager drawableManager = AppCompatDrawableManager.get();
        this.mBackgroundTintHelper = new AppCompatBackgroundHelper(this, drawableManager);
        this.mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr);
        this.mImageHelper = new AppCompatImageHelper(this, drawableManager);
        this.mImageHelper.loadFromAttributes(attrs, defStyleAttr);
    }

    @Override // android.widget.ImageView
    public void setImageResource(@DrawableRes int resId) {
        this.mImageHelper.setImageResource(resId);
    }

    @Override // android.view.View
    public void setBackgroundResource(@DrawableRes int resId) {
        super.setBackgroundResource(resId);
        if (this.mBackgroundTintHelper == null) {
            return;
        }
        this.mBackgroundTintHelper.onSetBackgroundResource(resId);
    }

    @Override // android.view.View
    public void setBackgroundDrawable(Drawable background) {
        super.setBackgroundDrawable(background);
        if (this.mBackgroundTintHelper == null) {
            return;
        }
        this.mBackgroundTintHelper.onSetBackgroundDrawable(background);
    }

    @Override // android.support.v4.view.TintableBackgroundView
    public void setSupportBackgroundTintList(@Nullable ColorStateList tint) {
        if (this.mBackgroundTintHelper == null) {
            return;
        }
        this.mBackgroundTintHelper.setSupportBackgroundTintList(tint);
    }

    @Override // android.support.v4.view.TintableBackgroundView
    @Nullable
    public ColorStateList getSupportBackgroundTintList() {
        if (this.mBackgroundTintHelper != null) {
            return this.mBackgroundTintHelper.getSupportBackgroundTintList();
        }
        return null;
    }

    @Override // android.support.v4.view.TintableBackgroundView
    public void setSupportBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
        if (this.mBackgroundTintHelper == null) {
            return;
        }
        this.mBackgroundTintHelper.setSupportBackgroundTintMode(tintMode);
    }

    @Override // android.support.v4.view.TintableBackgroundView
    @Nullable
    public PorterDuff.Mode getSupportBackgroundTintMode() {
        if (this.mBackgroundTintHelper != null) {
            return this.mBackgroundTintHelper.getSupportBackgroundTintMode();
        }
        return null;
    }

    @Override // android.widget.ImageView, android.view.View
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mBackgroundTintHelper == null) {
            return;
        }
        this.mBackgroundTintHelper.applySupportBackgroundTint();
    }

    @Override // android.widget.ImageView, android.view.View
    public boolean hasOverlappingRendering() {
        if (this.mImageHelper.hasOverlappingRendering()) {
            return super.hasOverlappingRendering();
        }
        return false;
    }
}
