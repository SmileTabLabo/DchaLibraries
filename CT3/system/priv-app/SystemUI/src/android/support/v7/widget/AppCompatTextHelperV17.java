package android.support.v7.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;
/* loaded from: a.zip:android/support/v7/widget/AppCompatTextHelperV17.class */
class AppCompatTextHelperV17 extends AppCompatTextHelper {
    private static final int[] VIEW_ATTRS_v17 = {16843666, 16843667};
    private TintInfo mDrawableEndTint;
    private TintInfo mDrawableStartTint;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AppCompatTextHelperV17(TextView textView) {
        super(textView);
    }

    @Override // android.support.v7.widget.AppCompatTextHelper
    void applyCompoundDrawablesTints() {
        super.applyCompoundDrawablesTints();
        if (this.mDrawableStartTint == null && this.mDrawableEndTint == null) {
            return;
        }
        Drawable[] compoundDrawablesRelative = this.mView.getCompoundDrawablesRelative();
        applyCompoundDrawableTint(compoundDrawablesRelative[0], this.mDrawableStartTint);
        applyCompoundDrawableTint(compoundDrawablesRelative[2], this.mDrawableEndTint);
    }

    @Override // android.support.v7.widget.AppCompatTextHelper
    void loadFromAttributes(AttributeSet attributeSet, int i) {
        super.loadFromAttributes(attributeSet, i);
        Context context = this.mView.getContext();
        AppCompatDrawableManager appCompatDrawableManager = AppCompatDrawableManager.get();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, VIEW_ATTRS_v17, i, 0);
        if (obtainStyledAttributes.hasValue(0)) {
            this.mDrawableStartTint = createTintInfo(context, appCompatDrawableManager, obtainStyledAttributes.getResourceId(0, 0));
        }
        if (obtainStyledAttributes.hasValue(1)) {
            this.mDrawableEndTint = createTintInfo(context, appCompatDrawableManager, obtainStyledAttributes.getResourceId(1, 0));
        }
        obtainStyledAttributes.recycle();
    }
}
