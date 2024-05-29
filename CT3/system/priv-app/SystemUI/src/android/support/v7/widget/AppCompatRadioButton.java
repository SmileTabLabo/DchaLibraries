package android.support.v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TintableCompoundButton;
import android.support.v7.appcompat.R$attr;
import android.util.AttributeSet;
import android.widget.RadioButton;
/* loaded from: a.zip:android/support/v7/widget/AppCompatRadioButton.class */
public class AppCompatRadioButton extends RadioButton implements TintableCompoundButton {
    private AppCompatCompoundButtonHelper mCompoundButtonHelper;
    private AppCompatDrawableManager mDrawableManager;

    public AppCompatRadioButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.radioButtonStyle);
    }

    public AppCompatRadioButton(Context context, AttributeSet attributeSet, int i) {
        super(TintContextWrapper.wrap(context), attributeSet, i);
        this.mDrawableManager = AppCompatDrawableManager.get();
        this.mCompoundButtonHelper = new AppCompatCompoundButtonHelper(this, this.mDrawableManager);
        this.mCompoundButtonHelper.loadFromAttributes(attributeSet, i);
    }

    @Override // android.widget.CompoundButton, android.widget.TextView
    public int getCompoundPaddingLeft() {
        int compoundPaddingLeft = super.getCompoundPaddingLeft();
        int i = compoundPaddingLeft;
        if (this.mCompoundButtonHelper != null) {
            i = this.mCompoundButtonHelper.getCompoundPaddingLeft(compoundPaddingLeft);
        }
        return i;
    }

    @Override // android.widget.CompoundButton
    public void setButtonDrawable(@DrawableRes int i) {
        setButtonDrawable(this.mDrawableManager != null ? this.mDrawableManager.getDrawable(getContext(), i) : ContextCompat.getDrawable(getContext(), i));
    }

    @Override // android.widget.CompoundButton
    public void setButtonDrawable(Drawable drawable) {
        super.setButtonDrawable(drawable);
        if (this.mCompoundButtonHelper != null) {
            this.mCompoundButtonHelper.onSetButtonDrawable();
        }
    }

    @Override // android.support.v4.widget.TintableCompoundButton
    public void setSupportButtonTintList(@Nullable ColorStateList colorStateList) {
        if (this.mCompoundButtonHelper != null) {
            this.mCompoundButtonHelper.setSupportButtonTintList(colorStateList);
        }
    }

    @Override // android.support.v4.widget.TintableCompoundButton
    public void setSupportButtonTintMode(@Nullable PorterDuff.Mode mode) {
        if (this.mCompoundButtonHelper != null) {
            this.mCompoundButtonHelper.setSupportButtonTintMode(mode);
        }
    }
}
