package android.support.v7.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;
/* loaded from: classes.dex */
class AppCompatTextHelperV17 extends AppCompatTextHelper {
    private static final int[] VIEW_ATTRS_v17 = {16843666, 16843667};
    private TintInfo mDrawableEndTint;
    private TintInfo mDrawableStartTint;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AppCompatTextHelperV17(TextView view) {
        super(view);
    }

    @Override // android.support.v7.widget.AppCompatTextHelper
    void loadFromAttributes(AttributeSet attrs, int defStyleAttr) {
        super.loadFromAttributes(attrs, defStyleAttr);
        Context context = this.mView.getContext();
        AppCompatDrawableManager drawableManager = AppCompatDrawableManager.get();
        TypedArray a = context.obtainStyledAttributes(attrs, VIEW_ATTRS_v17, defStyleAttr, 0);
        if (a.hasValue(0)) {
            this.mDrawableStartTint = createTintInfo(context, drawableManager, a.getResourceId(0, 0));
        }
        if (a.hasValue(1)) {
            this.mDrawableEndTint = createTintInfo(context, drawableManager, a.getResourceId(1, 0));
        }
        a.recycle();
    }

    @Override // android.support.v7.widget.AppCompatTextHelper
    void applyCompoundDrawablesTints() {
        super.applyCompoundDrawablesTints();
        if (this.mDrawableStartTint == null && this.mDrawableEndTint == null) {
            return;
        }
        Drawable[] compoundDrawables = this.mView.getCompoundDrawablesRelative();
        applyCompoundDrawableTint(compoundDrawables[0], this.mDrawableStartTint);
        applyCompoundDrawableTint(compoundDrawables[2], this.mDrawableEndTint);
    }
}
