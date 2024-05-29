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
import android.widget.AutoCompleteTextView;
/* loaded from: classes.dex */
public class AppCompatAutoCompleteTextView extends AutoCompleteTextView implements TintableBackgroundView {
    private static final int[] TINT_ATTRS = {16843126};
    private AppCompatBackgroundHelper mBackgroundTintHelper;
    private AppCompatDrawableManager mDrawableManager;
    private AppCompatTextHelper mTextHelper;

    public AppCompatAutoCompleteTextView(Context context) {
        this(context, null);
    }

    public AppCompatAutoCompleteTextView(Context context, AttributeSet attrs) {
        this(context, attrs, R$attr.autoCompleteTextViewStyle);
    }

    public AppCompatAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(TintContextWrapper.wrap(context), attrs, defStyleAttr);
        this.mDrawableManager = AppCompatDrawableManager.get();
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(getContext(), attrs, TINT_ATTRS, defStyleAttr, 0);
        if (a.hasValue(0)) {
            setDropDownBackgroundDrawable(a.getDrawable(0));
        }
        a.recycle();
        this.mBackgroundTintHelper = new AppCompatBackgroundHelper(this, this.mDrawableManager);
        this.mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr);
        this.mTextHelper = AppCompatTextHelper.create(this);
        this.mTextHelper.loadFromAttributes(attrs, defStyleAttr);
        this.mTextHelper.applyCompoundDrawablesTints();
    }

    @Override // android.widget.AutoCompleteTextView
    public void setDropDownBackgroundResource(@DrawableRes int resId) {
        if (this.mDrawableManager != null) {
            setDropDownBackgroundDrawable(this.mDrawableManager.getDrawable(getContext(), resId));
        } else {
            super.setDropDownBackgroundResource(resId);
        }
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

    @Override // android.widget.TextView, android.view.View
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mBackgroundTintHelper != null) {
            this.mBackgroundTintHelper.applySupportBackgroundTint();
        }
        if (this.mTextHelper == null) {
            return;
        }
        this.mTextHelper.applyCompoundDrawablesTints();
    }

    @Override // android.widget.TextView
    public void setTextAppearance(Context context, int resId) {
        super.setTextAppearance(context, resId);
        if (this.mTextHelper == null) {
            return;
        }
        this.mTextHelper.onSetTextAppearance(context, resId);
    }
}
