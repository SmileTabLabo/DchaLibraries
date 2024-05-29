package android.support.v4.view;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewParent;
import java.lang.reflect.Field;
/* loaded from: a.zip:android/support/v4/view/ViewCompatBase.class */
class ViewCompatBase {
    private static Field sMinHeightField;
    private static boolean sMinHeightFieldFetched;
    private static Field sMinWidthField;
    private static boolean sMinWidthFieldFetched;

    ViewCompatBase() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static ColorStateList getBackgroundTintList(View view) {
        return view instanceof TintableBackgroundView ? ((TintableBackgroundView) view).getSupportBackgroundTintList() : null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static PorterDuff.Mode getBackgroundTintMode(View view) {
        return view instanceof TintableBackgroundView ? ((TintableBackgroundView) view).getSupportBackgroundTintMode() : null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getMinimumHeight(View view) {
        if (!sMinHeightFieldFetched) {
            try {
                sMinHeightField = View.class.getDeclaredField("mMinHeight");
                sMinHeightField.setAccessible(true);
            } catch (NoSuchFieldException e) {
            }
            sMinHeightFieldFetched = true;
        }
        if (sMinHeightField != null) {
            try {
                return ((Integer) sMinHeightField.get(view)).intValue();
            } catch (Exception e2) {
                return 0;
            }
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getMinimumWidth(View view) {
        if (!sMinWidthFieldFetched) {
            try {
                sMinWidthField = View.class.getDeclaredField("mMinWidth");
                sMinWidthField.setAccessible(true);
            } catch (NoSuchFieldException e) {
            }
            sMinWidthFieldFetched = true;
        }
        if (sMinWidthField != null) {
            try {
                return ((Integer) sMinWidthField.get(view)).intValue();
            } catch (Exception e2) {
                return 0;
            }
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean isAttachedToWindow(View view) {
        return view.getWindowToken() != null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean isLaidOut(View view) {
        boolean z = false;
        if (view.getWidth() > 0) {
            z = false;
            if (view.getHeight() > 0) {
                z = true;
            }
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void offsetLeftAndRight(View view, int i) {
        int left = view.getLeft();
        view.offsetLeftAndRight(i);
        if (i != 0) {
            ViewParent parent = view.getParent();
            if (!(parent instanceof View)) {
                view.invalidate();
                return;
            }
            int abs = Math.abs(i);
            ((View) parent).invalidate(left - abs, view.getTop(), view.getWidth() + left + abs, view.getBottom());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void offsetTopAndBottom(View view, int i) {
        int top = view.getTop();
        view.offsetTopAndBottom(i);
        if (i != 0) {
            ViewParent parent = view.getParent();
            if (!(parent instanceof View)) {
                view.invalidate();
                return;
            }
            int abs = Math.abs(i);
            ((View) parent).invalidate(view.getLeft(), top - abs, view.getRight(), view.getHeight() + top + abs);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setBackgroundTintList(View view, ColorStateList colorStateList) {
        if (view instanceof TintableBackgroundView) {
            ((TintableBackgroundView) view).setSupportBackgroundTintList(colorStateList);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setBackgroundTintMode(View view, PorterDuff.Mode mode) {
        if (view instanceof TintableBackgroundView) {
            ((TintableBackgroundView) view).setSupportBackgroundTintMode(mode);
        }
    }
}
