package android.support.v7.widget;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.appcompat.R$styleable;
import android.util.AttributeSet;
import android.widget.CompoundButton;
/* loaded from: a.zip:android/support/v7/widget/AppCompatCompoundButtonHelper.class */
class AppCompatCompoundButtonHelper {
    private final AppCompatDrawableManager mDrawableManager;
    private boolean mSkipNextApply;
    private final CompoundButton mView;
    private ColorStateList mButtonTintList = null;
    private PorterDuff.Mode mButtonTintMode = null;
    private boolean mHasButtonTint = false;
    private boolean mHasButtonTintMode = false;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AppCompatCompoundButtonHelper(CompoundButton compoundButton, AppCompatDrawableManager appCompatDrawableManager) {
        this.mView = compoundButton;
        this.mDrawableManager = appCompatDrawableManager;
    }

    void applyButtonTint() {
        Drawable buttonDrawable = CompoundButtonCompat.getButtonDrawable(this.mView);
        if (buttonDrawable != null) {
            if (this.mHasButtonTint || this.mHasButtonTintMode) {
                Drawable mutate = DrawableCompat.wrap(buttonDrawable).mutate();
                if (this.mHasButtonTint) {
                    DrawableCompat.setTintList(mutate, this.mButtonTintList);
                }
                if (this.mHasButtonTintMode) {
                    DrawableCompat.setTintMode(mutate, this.mButtonTintMode);
                }
                if (mutate.isStateful()) {
                    mutate.setState(this.mView.getDrawableState());
                }
                this.mView.setButtonDrawable(mutate);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getCompoundPaddingLeft(int i) {
        int i2 = i;
        if (Build.VERSION.SDK_INT < 17) {
            Drawable buttonDrawable = CompoundButtonCompat.getButtonDrawable(this.mView);
            i2 = i;
            if (buttonDrawable != null) {
                i2 = i + buttonDrawable.getIntrinsicWidth();
            }
        }
        return i2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void loadFromAttributes(AttributeSet attributeSet, int i) {
        int resourceId;
        TypedArray obtainStyledAttributes = this.mView.getContext().obtainStyledAttributes(attributeSet, R$styleable.CompoundButton, i, 0);
        try {
            if (obtainStyledAttributes.hasValue(R$styleable.CompoundButton_android_button) && (resourceId = obtainStyledAttributes.getResourceId(R$styleable.CompoundButton_android_button, 0)) != 0) {
                this.mView.setButtonDrawable(this.mDrawableManager.getDrawable(this.mView.getContext(), resourceId));
            }
            if (obtainStyledAttributes.hasValue(R$styleable.CompoundButton_buttonTint)) {
                CompoundButtonCompat.setButtonTintList(this.mView, obtainStyledAttributes.getColorStateList(R$styleable.CompoundButton_buttonTint));
            }
            if (obtainStyledAttributes.hasValue(R$styleable.CompoundButton_buttonTintMode)) {
                CompoundButtonCompat.setButtonTintMode(this.mView, DrawableUtils.parseTintMode(obtainStyledAttributes.getInt(R$styleable.CompoundButton_buttonTintMode, -1), null));
            }
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onSetButtonDrawable() {
        if (this.mSkipNextApply) {
            this.mSkipNextApply = false;
            return;
        }
        this.mSkipNextApply = true;
        applyButtonTint();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setSupportButtonTintList(ColorStateList colorStateList) {
        this.mButtonTintList = colorStateList;
        this.mHasButtonTint = true;
        applyButtonTint();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setSupportButtonTintMode(@Nullable PorterDuff.Mode mode) {
        this.mButtonTintMode = mode;
        this.mHasButtonTintMode = true;
        applyButtonTint();
    }
}
