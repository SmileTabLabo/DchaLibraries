package android.support.v4.view;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowInsets;
/* loaded from: classes.dex */
class ViewCompatLollipop {
    private static ThreadLocal<Rect> sThreadLocalRect;

    ViewCompatLollipop() {
    }

    public static void requestApplyInsets(View view) {
        view.requestApplyInsets();
    }

    public static void setElevation(View view, float elevation) {
        view.setElevation(elevation);
    }

    public static float getElevation(View view) {
        return view.getElevation();
    }

    public static void setOnApplyWindowInsetsListener(View view, final OnApplyWindowInsetsListener listener) {
        if (listener == null) {
            view.setOnApplyWindowInsetsListener(null);
        } else {
            view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: android.support.v4.view.ViewCompatLollipop.1
                @Override // android.view.View.OnApplyWindowInsetsListener
                public WindowInsets onApplyWindowInsets(View view2, WindowInsets windowInsets) {
                    WindowInsetsCompatApi21 insets = new WindowInsetsCompatApi21(windowInsets);
                    return ((WindowInsetsCompatApi21) OnApplyWindowInsetsListener.this.onApplyWindowInsets(view2, insets)).unwrap();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static ColorStateList getBackgroundTintList(View view) {
        return view.getBackgroundTintList();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setBackgroundTintList(View view, ColorStateList tintList) {
        boolean hasTint;
        view.setBackgroundTintList(tintList);
        if (Build.VERSION.SDK_INT != 21) {
            return;
        }
        Drawable background = view.getBackground();
        if (view.getBackgroundTintList() == null) {
            hasTint = false;
        } else {
            hasTint = view.getBackgroundTintMode() != null;
        }
        if (background == null || !hasTint) {
            return;
        }
        if (background.isStateful()) {
            background.setState(view.getDrawableState());
        }
        view.setBackground(background);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static PorterDuff.Mode getBackgroundTintMode(View view) {
        return view.getBackgroundTintMode();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setBackgroundTintMode(View view, PorterDuff.Mode mode) {
        boolean hasTint;
        view.setBackgroundTintMode(mode);
        if (Build.VERSION.SDK_INT != 21) {
            return;
        }
        Drawable background = view.getBackground();
        if (view.getBackgroundTintList() == null) {
            hasTint = false;
        } else {
            hasTint = view.getBackgroundTintMode() != null;
        }
        if (background == null || !hasTint) {
            return;
        }
        if (background.isStateful()) {
            background.setState(view.getDrawableState());
        }
        view.setBackground(background);
    }

    public static WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
        WindowInsets unwrapped;
        WindowInsets result;
        if ((insets instanceof WindowInsetsCompatApi21) && (result = v.onApplyWindowInsets((unwrapped = ((WindowInsetsCompatApi21) insets).unwrap()))) != unwrapped) {
            return new WindowInsetsCompatApi21(result);
        }
        return insets;
    }

    public static WindowInsetsCompat dispatchApplyWindowInsets(View v, WindowInsetsCompat insets) {
        WindowInsets unwrapped;
        WindowInsets result;
        if ((insets instanceof WindowInsetsCompatApi21) && (result = v.dispatchApplyWindowInsets((unwrapped = ((WindowInsetsCompatApi21) insets).unwrap()))) != unwrapped) {
            return new WindowInsetsCompatApi21(result);
        }
        return insets;
    }

    public static void stopNestedScroll(View view) {
        view.stopNestedScroll();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void offsetTopAndBottom(View view, int offset) {
        Rect parentRect = getEmptyTempRect();
        boolean needInvalidateWorkaround = false;
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            View p = (View) parent;
            parentRect.set(p.getLeft(), p.getTop(), p.getRight(), p.getBottom());
            needInvalidateWorkaround = !parentRect.intersects(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        }
        ViewCompatHC.offsetTopAndBottom(view, offset);
        if (!needInvalidateWorkaround || !parentRect.intersect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom())) {
            return;
        }
        ((View) parent).invalidate(parentRect);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void offsetLeftAndRight(View view, int offset) {
        Rect parentRect = getEmptyTempRect();
        boolean needInvalidateWorkaround = false;
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            View p = (View) parent;
            parentRect.set(p.getLeft(), p.getTop(), p.getRight(), p.getBottom());
            needInvalidateWorkaround = !parentRect.intersects(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        }
        ViewCompatHC.offsetLeftAndRight(view, offset);
        if (!needInvalidateWorkaround || !parentRect.intersect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom())) {
            return;
        }
        ((View) parent).invalidate(parentRect);
    }

    private static Rect getEmptyTempRect() {
        if (sThreadLocalRect == null) {
            sThreadLocalRect = new ThreadLocal<>();
        }
        Rect rect = sThreadLocalRect.get();
        if (rect == null) {
            rect = new Rect();
            sThreadLocalRect.set(rect);
        }
        rect.setEmpty();
        return rect;
    }
}
