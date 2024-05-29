package android.support.v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.TintableCompoundButton;
import android.support.v7.appcompat.R;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.widget.CheckBox;
/* loaded from: classes.dex */
public class AppCompatCheckBox extends CheckBox implements TintableCompoundButton {
    private final AppCompatCompoundButtonHelper mCompoundButtonHelper;

    public AppCompatCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.checkboxStyle);
    }

    public AppCompatCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(TintContextWrapper.wrap(context), attrs, defStyleAttr);
        this.mCompoundButtonHelper = new AppCompatCompoundButtonHelper(this);
        this.mCompoundButtonHelper.loadFromAttributes(attrs, defStyleAttr);
    }

    @Override // android.widget.CompoundButton
    public void setButtonDrawable(Drawable buttonDrawable) {
        super.setButtonDrawable(buttonDrawable);
        if (this.mCompoundButtonHelper != null) {
            this.mCompoundButtonHelper.onSetButtonDrawable();
        }
    }

    @Override // android.widget.CompoundButton
    public void setButtonDrawable(int resId) {
        setButtonDrawable(AppCompatResources.getDrawable(getContext(), resId));
    }

    @Override // android.widget.CompoundButton, android.widget.TextView
    public int getCompoundPaddingLeft() {
        int value = super.getCompoundPaddingLeft();
        return this.mCompoundButtonHelper != null ? this.mCompoundButtonHelper.getCompoundPaddingLeft(value) : value;
    }

    @Override // android.support.v4.widget.TintableCompoundButton
    public void setSupportButtonTintList(ColorStateList tint) {
        if (this.mCompoundButtonHelper != null) {
            this.mCompoundButtonHelper.setSupportButtonTintList(tint);
        }
    }

    @Override // android.support.v4.widget.TintableCompoundButton
    public void setSupportButtonTintMode(PorterDuff.Mode tintMode) {
        if (this.mCompoundButtonHelper != null) {
            this.mCompoundButtonHelper.setSupportButtonTintMode(tintMode);
        }
    }
}
