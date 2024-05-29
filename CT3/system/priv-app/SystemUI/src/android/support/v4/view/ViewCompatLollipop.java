package android.support.v4.view;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowInsets;
/* loaded from: a.zip:android/support/v4/view/ViewCompatLollipop.class */
class ViewCompatLollipop {
    private static ThreadLocal<Rect> sThreadLocalRect;

    ViewCompatLollipop() {
    }

    public static WindowInsetsCompat dispatchApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
        WindowInsetsCompatApi21 windowInsetsCompatApi21 = windowInsetsCompat;
        if (windowInsetsCompat instanceof WindowInsetsCompatApi21) {
            WindowInsets unwrap = ((WindowInsetsCompatApi21) windowInsetsCompat).unwrap();
            WindowInsets dispatchApplyWindowInsets = view.dispatchApplyWindowInsets(unwrap);
            windowInsetsCompatApi21 = windowInsetsCompat;
            if (dispatchApplyWindowInsets != unwrap) {
                windowInsetsCompatApi21 = new WindowInsetsCompatApi21(dispatchApplyWindowInsets);
            }
        }
        return windowInsetsCompatApi21;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static ColorStateList getBackgroundTintList(View view) {
        return view.getBackgroundTintList();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static PorterDuff.Mode getBackgroundTintMode(View view) {
        return view.getBackgroundTintMode();
    }

    public static float getElevation(View view) {
        return view.getElevation();
    }

    private static Rect getEmptyTempRect() {
        if (sThreadLocalRect == null) {
            sThreadLocalRect = new ThreadLocal<>();
        }
        Rect rect = sThreadLocalRect.get();
        Rect rect2 = rect;
        if (rect == null) {
            rect2 = new Rect();
            sThreadLocalRect.set(rect2);
        }
        rect2.setEmpty();
        return rect2;
    }

    public static boolean isNestedScrollingEnabled(View view) {
        return view.isNestedScrollingEnabled();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void offsetLeftAndRight(View view, int i) {
        Rect emptyTempRect = getEmptyTempRect();
        boolean z = false;
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            View view2 = (View) parent;
            emptyTempRect.set(view2.getLeft(), view2.getTop(), view2.getRight(), view2.getBottom());
            z = !emptyTempRect.intersects(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        }
        ViewCompatHC.offsetLeftAndRight(view, i);
        if (z && emptyTempRect.intersect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom())) {
            ((View) parent).invalidate(emptyTempRect);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void offsetTopAndBottom(View view, int i) {
        Rect emptyTempRect = getEmptyTempRect();
        boolean z = false;
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            View view2 = (View) parent;
            emptyTempRect.set(view2.getLeft(), view2.getTop(), view2.getRight(), view2.getBottom());
            z = !emptyTempRect.intersects(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        }
        ViewCompatHC.offsetTopAndBottom(view, i);
        if (z && emptyTempRect.intersect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom())) {
            ((View) parent).invalidate(emptyTempRect);
        }
    }

    public static WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
        WindowInsetsCompatApi21 windowInsetsCompatApi21 = windowInsetsCompat;
        if (windowInsetsCompat instanceof WindowInsetsCompatApi21) {
            WindowInsets unwrap = ((WindowInsetsCompatApi21) windowInsetsCompat).unwrap();
            WindowInsets onApplyWindowInsets = view.onApplyWindowInsets(unwrap);
            windowInsetsCompatApi21 = windowInsetsCompat;
            if (onApplyWindowInsets != unwrap) {
                windowInsetsCompatApi21 = new WindowInsetsCompatApi21(onApplyWindowInsets);
            }
        }
        return windowInsetsCompatApi21;
    }

    public static void requestApplyInsets(View view) {
        view.requestApplyInsets();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setBackgroundTintList(View view, ColorStateList colorStateList) {
        view.setBackgroundTintList(colorStateList);
        if (Build.VERSION.SDK_INT == 21) {
            Drawable background = view.getBackground();
            boolean z = view.getBackgroundTintList() != null ? view.getBackgroundTintMode() != null : false;
            if (background == null || !z) {
                return;
            }
            if (background.isStateful()) {
                background.setState(view.getDrawableState());
            }
            view.setBackground(background);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setBackgroundTintMode(View view, PorterDuff.Mode mode) {
        view.setBackgroundTintMode(mode);
        if (Build.VERSION.SDK_INT == 21) {
            Drawable background = view.getBackground();
            boolean z = view.getBackgroundTintList() != null ? view.getBackgroundTintMode() != null : false;
            if (background == null || !z) {
                return;
            }
            if (background.isStateful()) {
                background.setState(view.getDrawableState());
            }
            view.setBackground(background);
        }
    }

    public static void setElevation(View view, float f) {
        view.setElevation(f);
    }

    public static void setOnApplyWindowInsetsListener(View view, OnApplyWindowInsetsListener onApplyWindowInsetsListener) {
        if (onApplyWindowInsetsListener == null) {
            view.setOnApplyWindowInsetsListener(null);
        } else {
            view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener(onApplyWindowInsetsListener) { // from class: android.support.v4.view.ViewCompatLollipop.1
                final OnApplyWindowInsetsListener val$listener;

                {
                    this.val$listener = onApplyWindowInsetsListener;
                }

                @Override // android.view.View.OnApplyWindowInsetsListener
                public WindowInsets onApplyWindowInsets(View view2, WindowInsets windowInsets) {
                    return ((WindowInsetsCompatApi21) this.val$listener.onApplyWindowInsets(view2, new WindowInsetsCompatApi21(windowInsets))).unwrap();
                }
            });
        }
    }

    public static void stopNestedScroll(View view) {
        view.stopNestedScroll();
    }
}
