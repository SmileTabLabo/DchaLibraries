package android.support.v4.content.res;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
/* loaded from: a.zip:android/support/v4/content/res/ResourcesCompat.class */
public final class ResourcesCompat {
    private ResourcesCompat() {
    }

    @Nullable
    public static Drawable getDrawable(@NonNull Resources resources, @DrawableRes int i, @Nullable Resources.Theme theme) throws Resources.NotFoundException {
        return Build.VERSION.SDK_INT >= 21 ? ResourcesCompatApi21.getDrawable(resources, i, theme) : resources.getDrawable(i);
    }
}
