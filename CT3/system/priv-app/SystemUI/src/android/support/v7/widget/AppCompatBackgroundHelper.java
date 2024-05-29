package android.support.v7.widget;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.appcompat.R$styleable;
import android.util.AttributeSet;
import android.view.View;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v7/widget/AppCompatBackgroundHelper.class */
public class AppCompatBackgroundHelper {
    private TintInfo mBackgroundTint;
    private final AppCompatDrawableManager mDrawableManager;
    private TintInfo mInternalBackgroundTint;
    private TintInfo mTmpInfo;
    private final View mView;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AppCompatBackgroundHelper(View view, AppCompatDrawableManager appCompatDrawableManager) {
        this.mView = view;
        this.mDrawableManager = appCompatDrawableManager;
    }

    private boolean applyFrameworkTintUsingColorFilter(@NonNull Drawable drawable) {
        if (this.mTmpInfo == null) {
            this.mTmpInfo = new TintInfo();
        }
        TintInfo tintInfo = this.mTmpInfo;
        tintInfo.clear();
        ColorStateList backgroundTintList = ViewCompat.getBackgroundTintList(this.mView);
        if (backgroundTintList != null) {
            tintInfo.mHasTintList = true;
            tintInfo.mTintList = backgroundTintList;
        }
        PorterDuff.Mode backgroundTintMode = ViewCompat.getBackgroundTintMode(this.mView);
        if (backgroundTintMode != null) {
            tintInfo.mHasTintMode = true;
            tintInfo.mTintMode = backgroundTintMode;
        }
        if (tintInfo.mHasTintList || tintInfo.mHasTintMode) {
            AppCompatDrawableManager.tintDrawable(drawable, tintInfo, this.mView.getDrawableState());
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void applySupportBackgroundTint() {
        Drawable background = this.mView.getBackground();
        if (background != null) {
            if (Build.VERSION.SDK_INT == 21 && applyFrameworkTintUsingColorFilter(background)) {
                return;
            }
            if (this.mBackgroundTint != null) {
                AppCompatDrawableManager.tintDrawable(background, this.mBackgroundTint, this.mView.getDrawableState());
            } else if (this.mInternalBackgroundTint != null) {
                AppCompatDrawableManager.tintDrawable(background, this.mInternalBackgroundTint, this.mView.getDrawableState());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ColorStateList getSupportBackgroundTintList() {
        ColorStateList colorStateList = null;
        if (this.mBackgroundTint != null) {
            colorStateList = this.mBackgroundTint.mTintList;
        }
        return colorStateList;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public PorterDuff.Mode getSupportBackgroundTintMode() {
        PorterDuff.Mode mode = null;
        if (this.mBackgroundTint != null) {
            mode = this.mBackgroundTint.mTintMode;
        }
        return mode;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void loadFromAttributes(AttributeSet attributeSet, int i) {
        ColorStateList tintList;
        TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(this.mView.getContext(), attributeSet, R$styleable.ViewBackgroundHelper, i, 0);
        try {
            if (obtainStyledAttributes.hasValue(R$styleable.ViewBackgroundHelper_android_background) && (tintList = this.mDrawableManager.getTintList(this.mView.getContext(), obtainStyledAttributes.getResourceId(R$styleable.ViewBackgroundHelper_android_background, -1))) != null) {
                setInternalBackgroundTint(tintList);
            }
            if (obtainStyledAttributes.hasValue(R$styleable.ViewBackgroundHelper_backgroundTint)) {
                ViewCompat.setBackgroundTintList(this.mView, obtainStyledAttributes.getColorStateList(R$styleable.ViewBackgroundHelper_backgroundTint));
            }
            if (obtainStyledAttributes.hasValue(R$styleable.ViewBackgroundHelper_backgroundTintMode)) {
                ViewCompat.setBackgroundTintMode(this.mView, DrawableUtils.parseTintMode(obtainStyledAttributes.getInt(R$styleable.ViewBackgroundHelper_backgroundTintMode, -1), null));
            }
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onSetBackgroundDrawable(Drawable drawable) {
        setInternalBackgroundTint(null);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onSetBackgroundResource(int i) {
        ColorStateList colorStateList = null;
        if (this.mDrawableManager != null) {
            colorStateList = this.mDrawableManager.getTintList(this.mView.getContext(), i);
        }
        setInternalBackgroundTint(colorStateList);
    }

    void setInternalBackgroundTint(ColorStateList colorStateList) {
        if (colorStateList != null) {
            if (this.mInternalBackgroundTint == null) {
                this.mInternalBackgroundTint = new TintInfo();
            }
            this.mInternalBackgroundTint.mTintList = colorStateList;
            this.mInternalBackgroundTint.mHasTintList = true;
        } else {
            this.mInternalBackgroundTint = null;
        }
        applySupportBackgroundTint();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setSupportBackgroundTintList(ColorStateList colorStateList) {
        if (this.mBackgroundTint == null) {
            this.mBackgroundTint = new TintInfo();
        }
        this.mBackgroundTint.mTintList = colorStateList;
        this.mBackgroundTint.mHasTintList = true;
        applySupportBackgroundTint();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setSupportBackgroundTintMode(PorterDuff.Mode mode) {
        if (this.mBackgroundTint == null) {
            this.mBackgroundTint = new TintInfo();
        }
        this.mBackgroundTint.mTintMode = mode;
        this.mBackgroundTint.mHasTintMode = true;
        applySupportBackgroundTint();
    }
}
