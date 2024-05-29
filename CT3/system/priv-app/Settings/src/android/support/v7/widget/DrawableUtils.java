package android.support.v7.widget;

import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.graphics.drawable.DrawableWrapper;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
/* loaded from: classes.dex */
public class DrawableUtils {
    public static final Rect INSETS_NONE = new Rect();
    private static Class<?> sInsetsClazz;

    static {
        if (Build.VERSION.SDK_INT < 18) {
            return;
        }
        try {
            sInsetsClazz = Class.forName("android.graphics.Insets");
        } catch (ClassNotFoundException e) {
        }
    }

    private DrawableUtils() {
    }

    public static Rect getOpticalBounds(Drawable drawable) {
        Field[] fields;
        if (sInsetsClazz != null) {
            try {
                Drawable drawable2 = DrawableCompat.unwrap(drawable);
                Method getOpticalInsetsMethod = drawable2.getClass().getMethod("getOpticalInsets", new Class[0]);
                Object insets = getOpticalInsetsMethod.invoke(drawable2, new Object[0]);
                if (insets != null) {
                    Rect result = new Rect();
                    for (Field field : sInsetsClazz.getFields()) {
                        String name = field.getName();
                        if (name.equals("left")) {
                            result.left = field.getInt(insets);
                        } else if (name.equals("top")) {
                            result.top = field.getInt(insets);
                        } else if (name.equals("right")) {
                            result.right = field.getInt(insets);
                        } else if (name.equals("bottom")) {
                            result.bottom = field.getInt(insets);
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
    public static void fixDrawable(@NonNull Drawable drawable) {
        if (Build.VERSION.SDK_INT != 21 || !"android.graphics.drawable.VectorDrawable".equals(drawable.getClass().getName())) {
            return;
        }
        fixVectorDrawableTinting(drawable);
    }

    public static boolean canSafelyMutateDrawable(@NonNull Drawable drawable) {
        Drawable[] children;
        if (Build.VERSION.SDK_INT >= 8 || !(drawable instanceof StateListDrawable)) {
            if (Build.VERSION.SDK_INT >= 15 || !(drawable instanceof InsetDrawable)) {
                if (Build.VERSION.SDK_INT >= 15 || !(drawable instanceof GradientDrawable)) {
                    if (Build.VERSION.SDK_INT >= 17 || !(drawable instanceof LayerDrawable)) {
                        if (drawable instanceof DrawableContainer) {
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
                        } else if (drawable instanceof DrawableWrapper) {
                            return canSafelyMutateDrawable(((DrawableWrapper) drawable).getWrappedDrawable());
                        } else {
                            if (drawable instanceof android.support.v7.graphics.drawable.DrawableWrapper) {
                                return canSafelyMutateDrawable(((android.support.v7.graphics.drawable.DrawableWrapper) drawable).getWrappedDrawable());
                            }
                            if (drawable instanceof ScaleDrawable) {
                                return canSafelyMutateDrawable(((ScaleDrawable) drawable).getDrawable());
                            }
                            return true;
                        }
                    }
                    return false;
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

    /* JADX INFO: Access modifiers changed from: package-private */
    public static PorterDuff.Mode parseTintMode(int value, PorterDuff.Mode defaultMode) {
        switch (value) {
            case 3:
                return PorterDuff.Mode.SRC_OVER;
            case 4:
            case 6:
            case 7:
            case 8:
            case 10:
            case 11:
            case 12:
            case 13:
            default:
                return defaultMode;
            case 5:
                return PorterDuff.Mode.SRC_IN;
            case 9:
                return PorterDuff.Mode.SRC_ATOP;
            case 14:
                return PorterDuff.Mode.MULTIPLY;
            case 15:
                return PorterDuff.Mode.SCREEN;
            case 16:
                if (Build.VERSION.SDK_INT < 11) {
                    return defaultMode;
                }
                return PorterDuff.Mode.valueOf("ADD");
        }
    }
}
