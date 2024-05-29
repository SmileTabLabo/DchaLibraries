package android.support.design.button;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
@TargetApi(21)
/* loaded from: classes.dex */
class MaterialButtonBackgroundDrawable extends RippleDrawable {
    /* JADX INFO: Access modifiers changed from: package-private */
    public MaterialButtonBackgroundDrawable(ColorStateList color, InsetDrawable content, Drawable mask) {
        super(color, content, mask);
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        if (getDrawable(0) != null) {
            InsetDrawable insetDrawable = (InsetDrawable) getDrawable(0);
            LayerDrawable layerDrawable = (LayerDrawable) insetDrawable.getDrawable();
            GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.getDrawable(0);
            gradientDrawable.setColorFilter(colorFilter);
        }
    }
}
