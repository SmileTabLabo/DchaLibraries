package android.support.v4.content;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.os.BuildCompat;
import android.util.TypedValue;
/* loaded from: a.zip:android/support/v4/content/ContextCompat.class */
public class ContextCompat {
    private static final Object sLock = new Object();
    private static TypedValue sTempValue;

    public static Context createDeviceProtectedStorageContext(Context context) {
        if (BuildCompat.isAtLeastN()) {
            return ContextCompatApi24.createDeviceProtectedStorageContext(context);
        }
        return null;
    }

    public static final ColorStateList getColorStateList(Context context, int i) {
        return Build.VERSION.SDK_INT >= 23 ? ContextCompatApi23.getColorStateList(context, i) : context.getResources().getColorStateList(i);
    }

    public static final Drawable getDrawable(Context context, int i) {
        int i2;
        int i3 = Build.VERSION.SDK_INT;
        if (i3 >= 21) {
            return ContextCompatApi21.getDrawable(context, i);
        }
        if (i3 >= 16) {
            return context.getResources().getDrawable(i);
        }
        synchronized (sLock) {
            if (sTempValue == null) {
                sTempValue = new TypedValue();
            }
            context.getResources().getValue(i, sTempValue, true);
            i2 = sTempValue.resourceId;
        }
        return context.getResources().getDrawable(i2);
    }
}
