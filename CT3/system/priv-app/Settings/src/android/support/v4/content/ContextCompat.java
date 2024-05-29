package android.support.v4.content;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.os.BuildCompat;
import android.util.TypedValue;
/* loaded from: classes.dex */
public class ContextCompat {
    private static final Object sLock = new Object();
    private static TypedValue sTempValue;

    public static final Drawable getDrawable(Context context, int id) {
        int resolvedId;
        int version = Build.VERSION.SDK_INT;
        if (version >= 21) {
            return ContextCompatApi21.getDrawable(context, id);
        }
        if (version >= 16) {
            return context.getResources().getDrawable(id);
        }
        synchronized (sLock) {
            if (sTempValue == null) {
                sTempValue = new TypedValue();
            }
            context.getResources().getValue(id, sTempValue, true);
            resolvedId = sTempValue.resourceId;
        }
        return context.getResources().getDrawable(resolvedId);
    }

    public static final ColorStateList getColorStateList(Context context, int id) {
        int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompatApi23.getColorStateList(context, id);
        }
        return context.getResources().getColorStateList(id);
    }

    public static Context createDeviceProtectedStorageContext(Context context) {
        if (BuildCompat.isAtLeastN()) {
            return ContextCompatApi24.createDeviceProtectedStorageContext(context);
        }
        return null;
    }
}
