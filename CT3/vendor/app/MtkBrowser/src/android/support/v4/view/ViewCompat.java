package android.support.v4.view;

import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.os.BuildCompat;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.WeakHashMap;
/* loaded from: b.zip:android/support/v4/view/ViewCompat.class */
public class ViewCompat {
    static final ViewCompatImpl IMPL;

    /* loaded from: b.zip:android/support/v4/view/ViewCompat$Api24ViewCompatImpl.class */
    static class Api24ViewCompatImpl extends MarshmallowViewCompatImpl {
        Api24ViewCompatImpl() {
        }
    }

    /* loaded from: b.zip:android/support/v4/view/ViewCompat$BaseViewCompatImpl.class */
    static class BaseViewCompatImpl implements ViewCompatImpl {
        WeakHashMap<View, ViewPropertyAnimatorCompat> mViewPropertyAnimatorCompatMap = null;

        BaseViewCompatImpl() {
        }

        private boolean canScrollingViewScrollHorizontally(ScrollingView scrollingView, int i) {
            boolean z = true;
            int computeHorizontalScrollOffset = scrollingView.computeHorizontalScrollOffset();
            int computeHorizontalScrollRange = scrollingView.computeHorizontalScrollRange() - scrollingView.computeHorizontalScrollExtent();
            if (computeHorizontalScrollRange == 0) {
                return false;
            }
            if (i >= 0) {
                return computeHorizontalScrollOffset < computeHorizontalScrollRange - 1;
            }
            if (computeHorizontalScrollOffset <= 0) {
                z = false;
            }
            return z;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean canScrollHorizontally(View view, int i) {
            return view instanceof ScrollingView ? canScrollingViewScrollHorizontally((ScrollingView) view, i) : false;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public WindowInsetsCompat dispatchApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
            return windowInsetsCompat;
        }

        long getFrameTime() {
            return 10L;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getImportantForAccessibility(View view) {
            return 0;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getOverScrollMode(View view) {
            return 2;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
            return windowInsetsCompat;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void postInvalidateOnAnimation(View view) {
            view.invalidate();
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void postOnAnimation(View view, Runnable runnable) {
            view.postDelayed(runnable, getFrameTime());
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setAccessibilityDelegate(View view, AccessibilityDelegateCompat accessibilityDelegateCompat) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setImportantForAccessibility(View view, int i) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setLayerType(View view, int i, Paint paint) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setOnApplyWindowInsetsListener(View view, OnApplyWindowInsetsListener onApplyWindowInsetsListener) {
        }
    }

    /* loaded from: b.zip:android/support/v4/view/ViewCompat$EclairMr1ViewCompatImpl.class */
    static class EclairMr1ViewCompatImpl extends BaseViewCompatImpl {
        EclairMr1ViewCompatImpl() {
        }
    }

    /* loaded from: b.zip:android/support/v4/view/ViewCompat$GBViewCompatImpl.class */
    static class GBViewCompatImpl extends EclairMr1ViewCompatImpl {
        GBViewCompatImpl() {
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getOverScrollMode(View view) {
            return ViewCompatGingerbread.getOverScrollMode(view);
        }
    }

    /* loaded from: b.zip:android/support/v4/view/ViewCompat$HCViewCompatImpl.class */
    static class HCViewCompatImpl extends GBViewCompatImpl {
        HCViewCompatImpl() {
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl
        long getFrameTime() {
            return ViewCompatHC.getFrameTime();
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setLayerType(View view, int i, Paint paint) {
            ViewCompatHC.setLayerType(view, i, paint);
        }
    }

    /* loaded from: b.zip:android/support/v4/view/ViewCompat$ICSMr1ViewCompatImpl.class */
    static class ICSMr1ViewCompatImpl extends ICSViewCompatImpl {
        ICSMr1ViewCompatImpl() {
        }
    }

    /* loaded from: b.zip:android/support/v4/view/ViewCompat$ICSViewCompatImpl.class */
    static class ICSViewCompatImpl extends HCViewCompatImpl {
        static boolean accessibilityDelegateCheckFailed = false;

        ICSViewCompatImpl() {
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean canScrollHorizontally(View view, int i) {
            return ViewCompatICS.canScrollHorizontally(view, i);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setAccessibilityDelegate(View view, @Nullable AccessibilityDelegateCompat accessibilityDelegateCompat) {
            ViewCompatICS.setAccessibilityDelegate(view, accessibilityDelegateCompat == null ? null : accessibilityDelegateCompat.getBridge());
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: b.zip:android/support/v4/view/ViewCompat$ImportantForAccessibility.class */
    private @interface ImportantForAccessibility {
    }

    /* loaded from: b.zip:android/support/v4/view/ViewCompat$JBViewCompatImpl.class */
    static class JBViewCompatImpl extends ICSMr1ViewCompatImpl {
        JBViewCompatImpl() {
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getImportantForAccessibility(View view) {
            return ViewCompatJB.getImportantForAccessibility(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void postInvalidateOnAnimation(View view) {
            ViewCompatJB.postInvalidateOnAnimation(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void postOnAnimation(View view, Runnable runnable) {
            ViewCompatJB.postOnAnimation(view, runnable);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setImportantForAccessibility(View view, int i) {
            int i2 = i;
            if (i == 4) {
                i2 = 2;
            }
            ViewCompatJB.setImportantForAccessibility(view, i2);
        }
    }

    /* loaded from: b.zip:android/support/v4/view/ViewCompat$JbMr1ViewCompatImpl.class */
    static class JbMr1ViewCompatImpl extends JBViewCompatImpl {
        JbMr1ViewCompatImpl() {
        }
    }

    /* loaded from: b.zip:android/support/v4/view/ViewCompat$JbMr2ViewCompatImpl.class */
    static class JbMr2ViewCompatImpl extends JbMr1ViewCompatImpl {
        JbMr2ViewCompatImpl() {
        }
    }

    /* loaded from: b.zip:android/support/v4/view/ViewCompat$KitKatViewCompatImpl.class */
    static class KitKatViewCompatImpl extends JbMr2ViewCompatImpl {
        KitKatViewCompatImpl() {
        }

        @Override // android.support.v4.view.ViewCompat.JBViewCompatImpl, android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setImportantForAccessibility(View view, int i) {
            ViewCompatJB.setImportantForAccessibility(view, i);
        }
    }

    /* loaded from: b.zip:android/support/v4/view/ViewCompat$LollipopViewCompatImpl.class */
    static class LollipopViewCompatImpl extends KitKatViewCompatImpl {
        LollipopViewCompatImpl() {
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public WindowInsetsCompat dispatchApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
            return ViewCompatLollipop.dispatchApplyWindowInsets(view, windowInsetsCompat);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
            return ViewCompatLollipop.onApplyWindowInsets(view, windowInsetsCompat);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setOnApplyWindowInsetsListener(View view, OnApplyWindowInsetsListener onApplyWindowInsetsListener) {
            ViewCompatLollipop.setOnApplyWindowInsetsListener(view, onApplyWindowInsetsListener);
        }
    }

    /* loaded from: b.zip:android/support/v4/view/ViewCompat$MarshmallowViewCompatImpl.class */
    static class MarshmallowViewCompatImpl extends LollipopViewCompatImpl {
        MarshmallowViewCompatImpl() {
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: b.zip:android/support/v4/view/ViewCompat$OverScroll.class */
    private @interface OverScroll {
    }

    /* loaded from: b.zip:android/support/v4/view/ViewCompat$ViewCompatImpl.class */
    interface ViewCompatImpl {
        boolean canScrollHorizontally(View view, int i);

        WindowInsetsCompat dispatchApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat);

        int getImportantForAccessibility(View view);

        int getOverScrollMode(View view);

        WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat);

        void postInvalidateOnAnimation(View view);

        void postOnAnimation(View view, Runnable runnable);

        void setAccessibilityDelegate(View view, @Nullable AccessibilityDelegateCompat accessibilityDelegateCompat);

        void setImportantForAccessibility(View view, int i);

        void setLayerType(View view, int i, Paint paint);

        void setOnApplyWindowInsetsListener(View view, OnApplyWindowInsetsListener onApplyWindowInsetsListener);
    }

    static {
        int i = Build.VERSION.SDK_INT;
        if (BuildCompat.isAtLeastN()) {
            IMPL = new Api24ViewCompatImpl();
        } else if (i >= 23) {
            IMPL = new MarshmallowViewCompatImpl();
        } else if (i >= 21) {
            IMPL = new LollipopViewCompatImpl();
        } else if (i >= 19) {
            IMPL = new KitKatViewCompatImpl();
        } else if (i >= 18) {
            IMPL = new JbMr2ViewCompatImpl();
        } else if (i >= 17) {
            IMPL = new JbMr1ViewCompatImpl();
        } else if (i >= 16) {
            IMPL = new JBViewCompatImpl();
        } else if (i >= 15) {
            IMPL = new ICSMr1ViewCompatImpl();
        } else if (i >= 14) {
            IMPL = new ICSViewCompatImpl();
        } else if (i >= 11) {
            IMPL = new HCViewCompatImpl();
        } else if (i >= 9) {
            IMPL = new GBViewCompatImpl();
        } else if (i >= 7) {
            IMPL = new EclairMr1ViewCompatImpl();
        } else {
            IMPL = new BaseViewCompatImpl();
        }
    }

    protected ViewCompat() {
    }

    public static boolean canScrollHorizontally(View view, int i) {
        return IMPL.canScrollHorizontally(view, i);
    }

    public static WindowInsetsCompat dispatchApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
        return IMPL.dispatchApplyWindowInsets(view, windowInsetsCompat);
    }

    public static int getImportantForAccessibility(View view) {
        return IMPL.getImportantForAccessibility(view);
    }

    public static int getOverScrollMode(View view) {
        return IMPL.getOverScrollMode(view);
    }

    public static WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
        return IMPL.onApplyWindowInsets(view, windowInsetsCompat);
    }

    public static void postInvalidateOnAnimation(View view) {
        IMPL.postInvalidateOnAnimation(view);
    }

    public static void postOnAnimation(View view, Runnable runnable) {
        IMPL.postOnAnimation(view, runnable);
    }

    public static void setAccessibilityDelegate(View view, AccessibilityDelegateCompat accessibilityDelegateCompat) {
        IMPL.setAccessibilityDelegate(view, accessibilityDelegateCompat);
    }

    public static void setImportantForAccessibility(View view, int i) {
        IMPL.setImportantForAccessibility(view, i);
    }

    public static void setLayerType(View view, int i, Paint paint) {
        IMPL.setLayerType(view, i, paint);
    }

    public static void setOnApplyWindowInsetsListener(View view, OnApplyWindowInsetsListener onApplyWindowInsetsListener) {
        IMPL.setOnApplyWindowInsetsListener(view, onApplyWindowInsetsListener);
    }
}
