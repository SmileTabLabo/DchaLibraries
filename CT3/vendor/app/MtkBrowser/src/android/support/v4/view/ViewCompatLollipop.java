package android.support.v4.view;

import android.view.View;
import android.view.WindowInsets;
/* loaded from: b.zip:android/support/v4/view/ViewCompatLollipop.class */
class ViewCompatLollipop {
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
}
