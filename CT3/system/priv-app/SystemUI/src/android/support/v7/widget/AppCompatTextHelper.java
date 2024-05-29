package android.support.v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.appcompat.R$styleable;
import android.support.v7.text.AllCapsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.widget.TextView;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v7/widget/AppCompatTextHelper.class */
public class AppCompatTextHelper {
    private static final int[] VIEW_ATTRS = {16842804, 16843119, 16843117, 16843120, 16843118};
    private TintInfo mDrawableBottomTint;
    private TintInfo mDrawableLeftTint;
    private TintInfo mDrawableRightTint;
    private TintInfo mDrawableTopTint;
    final TextView mView;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AppCompatTextHelper(TextView textView) {
        this.mView = textView;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static AppCompatTextHelper create(TextView textView) {
        return Build.VERSION.SDK_INT >= 17 ? new AppCompatTextHelperV17(textView) : new AppCompatTextHelper(textView);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static TintInfo createTintInfo(Context context, AppCompatDrawableManager appCompatDrawableManager, int i) {
        ColorStateList tintList = appCompatDrawableManager.getTintList(context, i);
        if (tintList != null) {
            TintInfo tintInfo = new TintInfo();
            tintInfo.mHasTintList = true;
            tintInfo.mTintList = tintList;
            return tintInfo;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void applyCompoundDrawableTint(Drawable drawable, TintInfo tintInfo) {
        if (drawable == null || tintInfo == null) {
            return;
        }
        AppCompatDrawableManager.tintDrawable(drawable, tintInfo, this.mView.getDrawableState());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void applyCompoundDrawablesTints() {
        if (this.mDrawableLeftTint == null && this.mDrawableTopTint == null && this.mDrawableRightTint == null && this.mDrawableBottomTint == null) {
            return;
        }
        Drawable[] compoundDrawables = this.mView.getCompoundDrawables();
        applyCompoundDrawableTint(compoundDrawables[0], this.mDrawableLeftTint);
        applyCompoundDrawableTint(compoundDrawables[1], this.mDrawableTopTint);
        applyCompoundDrawableTint(compoundDrawables[2], this.mDrawableRightTint);
        applyCompoundDrawableTint(compoundDrawables[3], this.mDrawableBottomTint);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void loadFromAttributes(AttributeSet attributeSet, int i) {
        Context context = this.mView.getContext();
        AppCompatDrawableManager appCompatDrawableManager = AppCompatDrawableManager.get();
        TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(context, attributeSet, VIEW_ATTRS, i, 0);
        int resourceId = obtainStyledAttributes.getResourceId(0, -1);
        if (obtainStyledAttributes.hasValue(1)) {
            this.mDrawableLeftTint = createTintInfo(context, appCompatDrawableManager, obtainStyledAttributes.getResourceId(1, 0));
        }
        if (obtainStyledAttributes.hasValue(2)) {
            this.mDrawableTopTint = createTintInfo(context, appCompatDrawableManager, obtainStyledAttributes.getResourceId(2, 0));
        }
        if (obtainStyledAttributes.hasValue(3)) {
            this.mDrawableRightTint = createTintInfo(context, appCompatDrawableManager, obtainStyledAttributes.getResourceId(3, 0));
        }
        if (obtainStyledAttributes.hasValue(4)) {
            this.mDrawableBottomTint = createTintInfo(context, appCompatDrawableManager, obtainStyledAttributes.getResourceId(4, 0));
        }
        obtainStyledAttributes.recycle();
        boolean z = this.mView.getTransformationMethod() instanceof PasswordTransformationMethod;
        boolean z2 = false;
        boolean z3 = false;
        ColorStateList colorStateList = null;
        if (resourceId != -1) {
            TintTypedArray obtainStyledAttributes2 = TintTypedArray.obtainStyledAttributes(context, resourceId, R$styleable.TextAppearance);
            z2 = false;
            z3 = false;
            if (!z) {
                z2 = false;
                z3 = false;
                if (obtainStyledAttributes2.hasValue(R$styleable.TextAppearance_textAllCaps)) {
                    z3 = true;
                    z2 = obtainStyledAttributes2.getBoolean(R$styleable.TextAppearance_textAllCaps, false);
                }
            }
            colorStateList = null;
            if (Build.VERSION.SDK_INT < 23) {
                colorStateList = null;
                if (obtainStyledAttributes2.hasValue(R$styleable.TextAppearance_android_textColor)) {
                    colorStateList = obtainStyledAttributes2.getColorStateList(R$styleable.TextAppearance_android_textColor);
                }
            }
            obtainStyledAttributes2.recycle();
        }
        TintTypedArray obtainStyledAttributes3 = TintTypedArray.obtainStyledAttributes(context, attributeSet, R$styleable.TextAppearance, i, 0);
        boolean z4 = z2;
        boolean z5 = z3;
        if (!z) {
            z4 = z2;
            z5 = z3;
            if (obtainStyledAttributes3.hasValue(R$styleable.TextAppearance_textAllCaps)) {
                z5 = true;
                z4 = obtainStyledAttributes3.getBoolean(R$styleable.TextAppearance_textAllCaps, false);
            }
        }
        ColorStateList colorStateList2 = colorStateList;
        if (Build.VERSION.SDK_INT < 23) {
            colorStateList2 = colorStateList;
            if (obtainStyledAttributes3.hasValue(R$styleable.TextAppearance_android_textColor)) {
                colorStateList2 = obtainStyledAttributes3.getColorStateList(R$styleable.TextAppearance_android_textColor);
            }
        }
        obtainStyledAttributes3.recycle();
        if (colorStateList2 != null) {
            this.mView.setTextColor(colorStateList2);
        }
        if (z || !z5) {
            return;
        }
        setAllCaps(z4);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onSetTextAppearance(Context context, int i) {
        ColorStateList colorStateList;
        TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(context, i, R$styleable.TextAppearance);
        if (obtainStyledAttributes.hasValue(R$styleable.TextAppearance_textAllCaps)) {
            setAllCaps(obtainStyledAttributes.getBoolean(R$styleable.TextAppearance_textAllCaps, false));
        }
        if (Build.VERSION.SDK_INT < 23 && obtainStyledAttributes.hasValue(R$styleable.TextAppearance_android_textColor) && (colorStateList = obtainStyledAttributes.getColorStateList(R$styleable.TextAppearance_android_textColor)) != null) {
            this.mView.setTextColor(colorStateList);
        }
        obtainStyledAttributes.recycle();
    }

    void setAllCaps(boolean z) {
        this.mView.setTransformationMethod(z ? new AllCapsTransformationMethod(this.mView.getContext()) : null);
    }
}
