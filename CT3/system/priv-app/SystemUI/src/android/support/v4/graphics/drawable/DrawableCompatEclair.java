package android.support.v4.graphics.drawable;

import android.graphics.drawable.Drawable;
/* loaded from: a.zip:android/support/v4/graphics/drawable/DrawableCompatEclair.class */
class DrawableCompatEclair {
    DrawableCompatEclair() {
    }

    public static Drawable wrapForTinting(Drawable drawable) {
        return !(drawable instanceof TintAwareDrawable) ? new DrawableWrapperEclair(drawable) : drawable;
    }
}
