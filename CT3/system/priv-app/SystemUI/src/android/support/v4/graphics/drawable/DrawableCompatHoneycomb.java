package android.support.v4.graphics.drawable;

import android.graphics.drawable.Drawable;
/* loaded from: a.zip:android/support/v4/graphics/drawable/DrawableCompatHoneycomb.class */
class DrawableCompatHoneycomb {
    DrawableCompatHoneycomb() {
    }

    public static void jumpToCurrentState(Drawable drawable) {
        drawable.jumpToCurrentState();
    }

    public static Drawable wrapForTinting(Drawable drawable) {
        return !(drawable instanceof TintAwareDrawable) ? new DrawableWrapperHoneycomb(drawable) : drawable;
    }
}
