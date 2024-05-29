package android.support.v4.content.res;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
/* loaded from: a.zip:android/support/v4/content/res/ResourcesCompatApi21.class */
class ResourcesCompatApi21 {
    ResourcesCompatApi21() {
    }

    public static Drawable getDrawable(Resources resources, int i, Resources.Theme theme) throws Resources.NotFoundException {
        return resources.getDrawable(i, theme);
    }
}
