package android.support.v7.widget;

import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.graphics.drawable.WrappedDrawable;
import android.support.v7.graphics.drawable.DrawableWrapper;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
/* loaded from: classes.dex */
public class DrawableUtils {
    public static final Rect INSETS_NONE = new Rect();
    private static Class<?> sInsetsClazz;

    static {
        if (Build.VERSION.SDK_INT >= 18) {
            try {
                sInsetsClazz = Class.forName("android.graphics.Insets");
            } catch (ClassNotFoundException e) {
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:31:0x0079  */
    /* JADX WARN: Removed duplicated region for block: B:32:0x007a A[Catch: Exception -> 0x009b, TryCatch #0 {Exception -> 0x009b, blocks: (B:4:0x0004, B:6:0x001e, B:8:0x002d, B:30:0x0076, B:32:0x007a, B:33:0x0081, B:34:0x0088, B:35:0x008f, B:17:0x004d, B:20:0x0057, B:23:0x0061, B:26:0x006b), top: B:43:0x0004 }] */
    /* JADX WARN: Removed duplicated region for block: B:33:0x0081 A[Catch: Exception -> 0x009b, TryCatch #0 {Exception -> 0x009b, blocks: (B:4:0x0004, B:6:0x001e, B:8:0x002d, B:30:0x0076, B:32:0x007a, B:33:0x0081, B:34:0x0088, B:35:0x008f, B:17:0x004d, B:20:0x0057, B:23:0x0061, B:26:0x006b), top: B:43:0x0004 }] */
    /* JADX WARN: Removed duplicated region for block: B:34:0x0088 A[Catch: Exception -> 0x009b, TryCatch #0 {Exception -> 0x009b, blocks: (B:4:0x0004, B:6:0x001e, B:8:0x002d, B:30:0x0076, B:32:0x007a, B:33:0x0081, B:34:0x0088, B:35:0x008f, B:17:0x004d, B:20:0x0057, B:23:0x0061, B:26:0x006b), top: B:43:0x0004 }] */
    /* JADX WARN: Removed duplicated region for block: B:35:0x008f A[Catch: Exception -> 0x009b, TRY_LEAVE, TryCatch #0 {Exception -> 0x009b, blocks: (B:4:0x0004, B:6:0x001e, B:8:0x002d, B:30:0x0076, B:32:0x007a, B:33:0x0081, B:34:0x0088, B:35:0x008f, B:17:0x004d, B:20:0x0057, B:23:0x0061, B:26:0x006b), top: B:43:0x0004 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static Rect getOpticalBounds(Drawable drawable) {
        Field[] fields;
        char c;
        if (sInsetsClazz != null) {
            try {
                Drawable drawable2 = DrawableCompat.unwrap(drawable);
                Method getOpticalInsetsMethod = drawable2.getClass().getMethod("getOpticalInsets", new Class[0]);
                Object insets = getOpticalInsetsMethod.invoke(drawable2, new Object[0]);
                if (insets != null) {
                    Rect result = new Rect();
                    for (Field field : sInsetsClazz.getFields()) {
                        String name = field.getName();
                        int hashCode = name.hashCode();
                        if (hashCode == -1383228885) {
                            if (name.equals("bottom")) {
                                c = 3;
                                switch (c) {
                                }
                            }
                            c = 65535;
                            switch (c) {
                            }
                        } else if (hashCode == 115029) {
                            if (name.equals("top")) {
                                c = 1;
                                switch (c) {
                                }
                            }
                            c = 65535;
                            switch (c) {
                            }
                        } else if (hashCode != 3317767) {
                            if (hashCode == 108511772 && name.equals("right")) {
                                c = 2;
                                switch (c) {
                                    case 0:
                                        result.left = field.getInt(insets);
                                        break;
                                    case 1:
                                        result.top = field.getInt(insets);
                                        break;
                                    case 2:
                                        result.right = field.getInt(insets);
                                        break;
                                    case 3:
                                        result.bottom = field.getInt(insets);
                                        break;
                                }
                            }
                            c = 65535;
                            switch (c) {
                            }
                        } else {
                            if (name.equals("left")) {
                                c = 0;
                                switch (c) {
                                }
                            }
                            c = 65535;
                            switch (c) {
                            }
                        }
                    }
                    return result;
                }
            } catch (Exception e) {
                Log.e("DrawableUtils", "Couldn't obtain the optical insets. Ignoring.");
            }
        }
        return INSETS_NONE;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void fixDrawable(Drawable drawable) {
        if (Build.VERSION.SDK_INT == 21 && "android.graphics.drawable.VectorDrawable".equals(drawable.getClass().getName())) {
            fixVectorDrawableTinting(drawable);
        }
    }

    public static boolean canSafelyMutateDrawable(Drawable drawable) {
        Drawable[] children;
        if (Build.VERSION.SDK_INT >= 15 || !(drawable instanceof InsetDrawable)) {
            if (Build.VERSION.SDK_INT >= 15 || !(drawable instanceof GradientDrawable)) {
                if (Build.VERSION.SDK_INT >= 17 || !(drawable instanceof LayerDrawable)) {
                    if (!(drawable instanceof DrawableContainer)) {
                        if (drawable instanceof WrappedDrawable) {
                            return canSafelyMutateDrawable(((WrappedDrawable) drawable).getWrappedDrawable());
                        }
                        if (drawable instanceof DrawableWrapper) {
                            return canSafelyMutateDrawable(((DrawableWrapper) drawable).getWrappedDrawable());
                        }
                        if (drawable instanceof ScaleDrawable) {
                            return canSafelyMutateDrawable(((ScaleDrawable) drawable).getDrawable());
                        }
                        return true;
                    }
                    Drawable.ConstantState state = drawable.getConstantState();
                    if (state instanceof DrawableContainer.DrawableContainerState) {
                        DrawableContainer.DrawableContainerState containerState = (DrawableContainer.DrawableContainerState) state;
                        for (Drawable child : containerState.getChildren()) {
                            if (!canSafelyMutateDrawable(child)) {
                                return false;
                            }
                        }
                        return true;
                    }
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    private static void fixVectorDrawableTinting(Drawable drawable) {
        int[] originalState = drawable.getState();
        if (originalState == null || originalState.length == 0) {
            drawable.setState(ThemeUtils.CHECKED_STATE_SET);
        } else {
            drawable.setState(ThemeUtils.EMPTY_STATE_SET);
        }
        drawable.setState(originalState);
    }

    public static PorterDuff.Mode parseTintMode(int value, PorterDuff.Mode defaultMode) {
        if (value != 3) {
            if (value != 5) {
                if (value == 9) {
                    return PorterDuff.Mode.SRC_ATOP;
                }
                switch (value) {
                    case 14:
                        return PorterDuff.Mode.MULTIPLY;
                    case 15:
                        return PorterDuff.Mode.SCREEN;
                    case 16:
                        return PorterDuff.Mode.ADD;
                    default:
                        return defaultMode;
                }
            }
            return PorterDuff.Mode.SRC_IN;
        }
        return PorterDuff.Mode.SRC_OVER;
    }
}
