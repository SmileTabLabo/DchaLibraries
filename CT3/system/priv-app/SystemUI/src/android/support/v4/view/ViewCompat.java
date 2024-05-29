package android.support.v4.view;

import android.content.res.ColorStateList;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v4.os.BuildCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.WeakHashMap;
/* loaded from: a.zip:android/support/v4/view/ViewCompat.class */
public class ViewCompat {
    static final ViewCompatImpl IMPL;

    /* loaded from: a.zip:android/support/v4/view/ViewCompat$Api24ViewCompatImpl.class */
    static class Api24ViewCompatImpl extends MarshmallowViewCompatImpl {
        Api24ViewCompatImpl() {
        }
    }

    /* loaded from: a.zip:android/support/v4/view/ViewCompat$BaseViewCompatImpl.class */
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

        private boolean canScrollingViewScrollVertically(ScrollingView scrollingView, int i) {
            boolean z = true;
            int computeVerticalScrollOffset = scrollingView.computeVerticalScrollOffset();
            int computeVerticalScrollRange = scrollingView.computeVerticalScrollRange() - scrollingView.computeVerticalScrollExtent();
            if (computeVerticalScrollRange == 0) {
                return false;
            }
            if (i >= 0) {
                return computeVerticalScrollOffset < computeVerticalScrollRange - 1;
            }
            if (computeVerticalScrollOffset <= 0) {
                z = false;
            }
            return z;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public ViewPropertyAnimatorCompat animate(View view) {
            return new ViewPropertyAnimatorCompat(view);
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean canScrollHorizontally(View view, int i) {
            return view instanceof ScrollingView ? canScrollingViewScrollHorizontally((ScrollingView) view, i) : false;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean canScrollVertically(View view, int i) {
            return view instanceof ScrollingView ? canScrollingViewScrollVertically((ScrollingView) view, i) : false;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public WindowInsetsCompat dispatchApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
            return windowInsetsCompat;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public float getAlpha(View view) {
            return 1.0f;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public ColorStateList getBackgroundTintList(View view) {
            return ViewCompatBase.getBackgroundTintList(view);
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public PorterDuff.Mode getBackgroundTintMode(View view) {
            return ViewCompatBase.getBackgroundTintMode(view);
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public float getElevation(View view) {
            return 0.0f;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean getFitsSystemWindows(View view) {
            return false;
        }

        long getFrameTime() {
            return 10L;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getImportantForAccessibility(View view) {
            return 0;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getLayerType(View view) {
            return 0;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getLayoutDirection(View view) {
            return 0;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public Matrix getMatrix(View view) {
            return null;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getMeasuredState(View view) {
            return 0;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getMeasuredWidthAndState(View view) {
            return view.getMeasuredWidth();
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getMinimumHeight(View view) {
            return ViewCompatBase.getMinimumHeight(view);
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getMinimumWidth(View view) {
            return ViewCompatBase.getMinimumWidth(view);
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getOverScrollMode(View view) {
            return 2;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public ViewParent getParentForAccessibility(View view) {
            return view.getParent();
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public float getScaleX(View view) {
            return 0.0f;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public float getTranslationX(View view) {
            return 0.0f;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public float getTranslationY(View view) {
            return 0.0f;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getWindowSystemUiVisibility(View view) {
            return 0;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public float getY(View view) {
            return 0.0f;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean hasAccessibilityDelegate(View view) {
            return false;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean hasOverlappingRendering(View view) {
            return true;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean hasTransientState(View view) {
            return false;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean isAttachedToWindow(View view) {
            return ViewCompatBase.isAttachedToWindow(view);
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean isLaidOut(View view) {
            return ViewCompatBase.isLaidOut(view);
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean isNestedScrollingEnabled(View view) {
            if (view instanceof NestedScrollingChild) {
                return ((NestedScrollingChild) view).isNestedScrollingEnabled();
            }
            return false;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean isOpaque(View view) {
            boolean z = false;
            Drawable background = view.getBackground();
            if (background != null) {
                if (background.getOpacity() == -1) {
                    z = true;
                }
                return z;
            }
            return false;
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void jumpDrawablesToCurrentState(View view) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void offsetLeftAndRight(View view, int i) {
            ViewCompatBase.offsetLeftAndRight(view, i);
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void offsetTopAndBottom(View view, int i) {
            ViewCompatBase.offsetTopAndBottom(view, i);
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
        public void postInvalidateOnAnimation(View view, int i, int i2, int i3, int i4) {
            view.invalidate(i, i2, i3, i4);
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void postOnAnimation(View view, Runnable runnable) {
            view.postDelayed(runnable, getFrameTime());
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void postOnAnimationDelayed(View view, Runnable runnable, long j) {
            view.postDelayed(runnable, getFrameTime() + j);
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void requestApplyInsets(View view) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public int resolveSizeAndState(int i, int i2, int i3) {
            return View.resolveSize(i, i2);
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setAccessibilityDelegate(View view, AccessibilityDelegateCompat accessibilityDelegateCompat) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setActivated(View view, boolean z) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setAlpha(View view, float f) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setBackgroundTintList(View view, ColorStateList colorStateList) {
            ViewCompatBase.setBackgroundTintList(view, colorStateList);
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setBackgroundTintMode(View view, PorterDuff.Mode mode) {
            ViewCompatBase.setBackgroundTintMode(view, mode);
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setChildrenDrawingOrderEnabled(ViewGroup viewGroup, boolean z) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setElevation(View view, float f) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setImportantForAccessibility(View view, int i) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setLayerPaint(View view, Paint paint) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setLayerType(View view, int i, Paint paint) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setOnApplyWindowInsetsListener(View view, OnApplyWindowInsetsListener onApplyWindowInsetsListener) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setSaveFromParentEnabled(View view, boolean z) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setScaleX(View view, float f) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setScaleY(View view, float f) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setTranslationX(View view, float f) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setTranslationY(View view, float f) {
        }

        @Override // android.support.v4.view.ViewCompat.ViewCompatImpl
        public void stopNestedScroll(View view) {
            if (view instanceof NestedScrollingChild) {
                ((NestedScrollingChild) view).stopNestedScroll();
            }
        }
    }

    /* loaded from: a.zip:android/support/v4/view/ViewCompat$EclairMr1ViewCompatImpl.class */
    static class EclairMr1ViewCompatImpl extends BaseViewCompatImpl {
        EclairMr1ViewCompatImpl() {
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean isOpaque(View view) {
            return ViewCompatEclairMr1.isOpaque(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setChildrenDrawingOrderEnabled(ViewGroup viewGroup, boolean z) {
            ViewCompatEclairMr1.setChildrenDrawingOrderEnabled(viewGroup, z);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/ViewCompat$GBViewCompatImpl.class */
    static class GBViewCompatImpl extends EclairMr1ViewCompatImpl {
        GBViewCompatImpl() {
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getOverScrollMode(View view) {
            return ViewCompatGingerbread.getOverScrollMode(view);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/ViewCompat$HCViewCompatImpl.class */
    static class HCViewCompatImpl extends GBViewCompatImpl {
        HCViewCompatImpl() {
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public float getAlpha(View view) {
            return ViewCompatHC.getAlpha(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl
        long getFrameTime() {
            return ViewCompatHC.getFrameTime();
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getLayerType(View view) {
            return ViewCompatHC.getLayerType(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public Matrix getMatrix(View view) {
            return ViewCompatHC.getMatrix(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getMeasuredState(View view) {
            return ViewCompatHC.getMeasuredState(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getMeasuredWidthAndState(View view) {
            return ViewCompatHC.getMeasuredWidthAndState(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public float getScaleX(View view) {
            return ViewCompatHC.getScaleX(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public float getTranslationX(View view) {
            return ViewCompatHC.getTranslationX(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public float getTranslationY(View view) {
            return ViewCompatHC.getTranslationY(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public float getY(View view) {
            return ViewCompatHC.getY(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void jumpDrawablesToCurrentState(View view) {
            ViewCompatHC.jumpDrawablesToCurrentState(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void offsetLeftAndRight(View view, int i) {
            ViewCompatHC.offsetLeftAndRight(view, i);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void offsetTopAndBottom(View view, int i) {
            ViewCompatHC.offsetTopAndBottom(view, i);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public int resolveSizeAndState(int i, int i2, int i3) {
            return ViewCompatHC.resolveSizeAndState(i, i2, i3);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setActivated(View view, boolean z) {
            ViewCompatHC.setActivated(view, z);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setAlpha(View view, float f) {
            ViewCompatHC.setAlpha(view, f);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setLayerPaint(View view, Paint paint) {
            setLayerType(view, getLayerType(view), paint);
            view.invalidate();
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setLayerType(View view, int i, Paint paint) {
            ViewCompatHC.setLayerType(view, i, paint);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setSaveFromParentEnabled(View view, boolean z) {
            ViewCompatHC.setSaveFromParentEnabled(view, z);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setScaleX(View view, float f) {
            ViewCompatHC.setScaleX(view, f);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setScaleY(View view, float f) {
            ViewCompatHC.setScaleY(view, f);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setTranslationX(View view, float f) {
            ViewCompatHC.setTranslationX(view, f);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setTranslationY(View view, float f) {
            ViewCompatHC.setTranslationY(view, f);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/ViewCompat$ICSMr1ViewCompatImpl.class */
    static class ICSMr1ViewCompatImpl extends ICSViewCompatImpl {
        ICSMr1ViewCompatImpl() {
        }
    }

    /* loaded from: a.zip:android/support/v4/view/ViewCompat$ICSViewCompatImpl.class */
    static class ICSViewCompatImpl extends HCViewCompatImpl {
        static boolean accessibilityDelegateCheckFailed = false;
        static Field mAccessibilityDelegateField;

        ICSViewCompatImpl() {
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public ViewPropertyAnimatorCompat animate(View view) {
            if (this.mViewPropertyAnimatorCompatMap == null) {
                this.mViewPropertyAnimatorCompatMap = new WeakHashMap<>();
            }
            ViewPropertyAnimatorCompat viewPropertyAnimatorCompat = this.mViewPropertyAnimatorCompatMap.get(view);
            ViewPropertyAnimatorCompat viewPropertyAnimatorCompat2 = viewPropertyAnimatorCompat;
            if (viewPropertyAnimatorCompat == null) {
                viewPropertyAnimatorCompat2 = new ViewPropertyAnimatorCompat(view);
                this.mViewPropertyAnimatorCompatMap.put(view, viewPropertyAnimatorCompat2);
            }
            return viewPropertyAnimatorCompat2;
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean canScrollHorizontally(View view, int i) {
            return ViewCompatICS.canScrollHorizontally(view, i);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean canScrollVertically(View view, int i) {
            return ViewCompatICS.canScrollVertically(view, i);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean hasAccessibilityDelegate(View view) {
            boolean z = true;
            if (accessibilityDelegateCheckFailed) {
                return false;
            }
            if (mAccessibilityDelegateField == null) {
                try {
                    mAccessibilityDelegateField = View.class.getDeclaredField("mAccessibilityDelegate");
                    mAccessibilityDelegateField.setAccessible(true);
                } catch (Throwable th) {
                    accessibilityDelegateCheckFailed = true;
                    return false;
                }
            }
            try {
                if (mAccessibilityDelegateField.get(view) == null) {
                    z = false;
                }
                return z;
            } catch (Throwable th2) {
                accessibilityDelegateCheckFailed = true;
                return false;
            }
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setAccessibilityDelegate(View view, @Nullable AccessibilityDelegateCompat accessibilityDelegateCompat) {
            ViewCompatICS.setAccessibilityDelegate(view, accessibilityDelegateCompat == null ? null : accessibilityDelegateCompat.getBridge());
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: a.zip:android/support/v4/view/ViewCompat$ImportantForAccessibility.class */
    private @interface ImportantForAccessibility {
    }

    /* loaded from: a.zip:android/support/v4/view/ViewCompat$JBViewCompatImpl.class */
    static class JBViewCompatImpl extends ICSMr1ViewCompatImpl {
        JBViewCompatImpl() {
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean getFitsSystemWindows(View view) {
            return ViewCompatJB.getFitsSystemWindows(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getImportantForAccessibility(View view) {
            return ViewCompatJB.getImportantForAccessibility(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getMinimumHeight(View view) {
            return ViewCompatJB.getMinimumHeight(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getMinimumWidth(View view) {
            return ViewCompatJB.getMinimumWidth(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public ViewParent getParentForAccessibility(View view) {
            return ViewCompatJB.getParentForAccessibility(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean hasOverlappingRendering(View view) {
            return ViewCompatJB.hasOverlappingRendering(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean hasTransientState(View view) {
            return ViewCompatJB.hasTransientState(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void postInvalidateOnAnimation(View view) {
            ViewCompatJB.postInvalidateOnAnimation(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void postInvalidateOnAnimation(View view, int i, int i2, int i3, int i4) {
            ViewCompatJB.postInvalidateOnAnimation(view, i, i2, i3, i4);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void postOnAnimation(View view, Runnable runnable) {
            ViewCompatJB.postOnAnimation(view, runnable);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void postOnAnimationDelayed(View view, Runnable runnable, long j) {
            ViewCompatJB.postOnAnimationDelayed(view, runnable, j);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void requestApplyInsets(View view) {
            ViewCompatJB.requestApplyInsets(view);
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

    /* loaded from: a.zip:android/support/v4/view/ViewCompat$JbMr1ViewCompatImpl.class */
    static class JbMr1ViewCompatImpl extends JBViewCompatImpl {
        JbMr1ViewCompatImpl() {
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getLayoutDirection(View view) {
            return ViewCompatJellybeanMr1.getLayoutDirection(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public int getWindowSystemUiVisibility(View view) {
            return ViewCompatJellybeanMr1.getWindowSystemUiVisibility(view);
        }

        @Override // android.support.v4.view.ViewCompat.HCViewCompatImpl, android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setLayerPaint(View view, Paint paint) {
            ViewCompatJellybeanMr1.setLayerPaint(view, paint);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/ViewCompat$JbMr2ViewCompatImpl.class */
    static class JbMr2ViewCompatImpl extends JbMr1ViewCompatImpl {
        JbMr2ViewCompatImpl() {
        }
    }

    /* loaded from: a.zip:android/support/v4/view/ViewCompat$KitKatViewCompatImpl.class */
    static class KitKatViewCompatImpl extends JbMr2ViewCompatImpl {
        KitKatViewCompatImpl() {
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean isAttachedToWindow(View view) {
            return ViewCompatKitKat.isAttachedToWindow(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean isLaidOut(View view) {
            return ViewCompatKitKat.isLaidOut(view);
        }

        @Override // android.support.v4.view.ViewCompat.JBViewCompatImpl, android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setImportantForAccessibility(View view, int i) {
            ViewCompatJB.setImportantForAccessibility(view, i);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: a.zip:android/support/v4/view/ViewCompat$LayerType.class */
    private @interface LayerType {
    }

    /* loaded from: a.zip:android/support/v4/view/ViewCompat$LollipopViewCompatImpl.class */
    static class LollipopViewCompatImpl extends KitKatViewCompatImpl {
        LollipopViewCompatImpl() {
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public WindowInsetsCompat dispatchApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
            return ViewCompatLollipop.dispatchApplyWindowInsets(view, windowInsetsCompat);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public ColorStateList getBackgroundTintList(View view) {
            return ViewCompatLollipop.getBackgroundTintList(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public PorterDuff.Mode getBackgroundTintMode(View view) {
            return ViewCompatLollipop.getBackgroundTintMode(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public float getElevation(View view) {
            return ViewCompatLollipop.getElevation(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public boolean isNestedScrollingEnabled(View view) {
            return ViewCompatLollipop.isNestedScrollingEnabled(view);
        }

        @Override // android.support.v4.view.ViewCompat.HCViewCompatImpl, android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void offsetLeftAndRight(View view, int i) {
            ViewCompatLollipop.offsetLeftAndRight(view, i);
        }

        @Override // android.support.v4.view.ViewCompat.HCViewCompatImpl, android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void offsetTopAndBottom(View view, int i) {
            ViewCompatLollipop.offsetTopAndBottom(view, i);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
            return ViewCompatLollipop.onApplyWindowInsets(view, windowInsetsCompat);
        }

        @Override // android.support.v4.view.ViewCompat.JBViewCompatImpl, android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void requestApplyInsets(View view) {
            ViewCompatLollipop.requestApplyInsets(view);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setBackgroundTintList(View view, ColorStateList colorStateList) {
            ViewCompatLollipop.setBackgroundTintList(view, colorStateList);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setBackgroundTintMode(View view, PorterDuff.Mode mode) {
            ViewCompatLollipop.setBackgroundTintMode(view, mode);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setElevation(View view, float f) {
            ViewCompatLollipop.setElevation(view, f);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void setOnApplyWindowInsetsListener(View view, OnApplyWindowInsetsListener onApplyWindowInsetsListener) {
            ViewCompatLollipop.setOnApplyWindowInsetsListener(view, onApplyWindowInsetsListener);
        }

        @Override // android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void stopNestedScroll(View view) {
            ViewCompatLollipop.stopNestedScroll(view);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/ViewCompat$MarshmallowViewCompatImpl.class */
    static class MarshmallowViewCompatImpl extends LollipopViewCompatImpl {
        MarshmallowViewCompatImpl() {
        }

        @Override // android.support.v4.view.ViewCompat.LollipopViewCompatImpl, android.support.v4.view.ViewCompat.HCViewCompatImpl, android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void offsetLeftAndRight(View view, int i) {
            ViewCompatMarshmallow.offsetLeftAndRight(view, i);
        }

        @Override // android.support.v4.view.ViewCompat.LollipopViewCompatImpl, android.support.v4.view.ViewCompat.HCViewCompatImpl, android.support.v4.view.ViewCompat.BaseViewCompatImpl, android.support.v4.view.ViewCompat.ViewCompatImpl
        public void offsetTopAndBottom(View view, int i) {
            ViewCompatMarshmallow.offsetTopAndBottom(view, i);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: a.zip:android/support/v4/view/ViewCompat$OverScroll.class */
    private @interface OverScroll {
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: a.zip:android/support/v4/view/ViewCompat$ResolvedLayoutDirectionMode.class */
    private @interface ResolvedLayoutDirectionMode {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:android/support/v4/view/ViewCompat$ViewCompatImpl.class */
    public interface ViewCompatImpl {
        ViewPropertyAnimatorCompat animate(View view);

        boolean canScrollHorizontally(View view, int i);

        boolean canScrollVertically(View view, int i);

        WindowInsetsCompat dispatchApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat);

        float getAlpha(View view);

        ColorStateList getBackgroundTintList(View view);

        PorterDuff.Mode getBackgroundTintMode(View view);

        float getElevation(View view);

        boolean getFitsSystemWindows(View view);

        int getImportantForAccessibility(View view);

        int getLayerType(View view);

        int getLayoutDirection(View view);

        @Nullable
        Matrix getMatrix(View view);

        int getMeasuredState(View view);

        int getMeasuredWidthAndState(View view);

        int getMinimumHeight(View view);

        int getMinimumWidth(View view);

        int getOverScrollMode(View view);

        ViewParent getParentForAccessibility(View view);

        float getScaleX(View view);

        float getTranslationX(View view);

        float getTranslationY(View view);

        int getWindowSystemUiVisibility(View view);

        float getY(View view);

        boolean hasAccessibilityDelegate(View view);

        boolean hasOverlappingRendering(View view);

        boolean hasTransientState(View view);

        boolean isAttachedToWindow(View view);

        boolean isLaidOut(View view);

        boolean isNestedScrollingEnabled(View view);

        boolean isOpaque(View view);

        void jumpDrawablesToCurrentState(View view);

        void offsetLeftAndRight(View view, int i);

        void offsetTopAndBottom(View view, int i);

        WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat);

        void postInvalidateOnAnimation(View view);

        void postInvalidateOnAnimation(View view, int i, int i2, int i3, int i4);

        void postOnAnimation(View view, Runnable runnable);

        void postOnAnimationDelayed(View view, Runnable runnable, long j);

        void requestApplyInsets(View view);

        int resolveSizeAndState(int i, int i2, int i3);

        void setAccessibilityDelegate(View view, @Nullable AccessibilityDelegateCompat accessibilityDelegateCompat);

        void setActivated(View view, boolean z);

        void setAlpha(View view, float f);

        void setBackgroundTintList(View view, ColorStateList colorStateList);

        void setBackgroundTintMode(View view, PorterDuff.Mode mode);

        void setChildrenDrawingOrderEnabled(ViewGroup viewGroup, boolean z);

        void setElevation(View view, float f);

        void setImportantForAccessibility(View view, int i);

        void setLayerPaint(View view, Paint paint);

        void setLayerType(View view, int i, Paint paint);

        void setOnApplyWindowInsetsListener(View view, OnApplyWindowInsetsListener onApplyWindowInsetsListener);

        void setSaveFromParentEnabled(View view, boolean z);

        void setScaleX(View view, float f);

        void setScaleY(View view, float f);

        void setTranslationX(View view, float f);

        void setTranslationY(View view, float f);

        void stopNestedScroll(View view);
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

    public static ViewPropertyAnimatorCompat animate(View view) {
        return IMPL.animate(view);
    }

    public static boolean canScrollHorizontally(View view, int i) {
        return IMPL.canScrollHorizontally(view, i);
    }

    public static boolean canScrollVertically(View view, int i) {
        return IMPL.canScrollVertically(view, i);
    }

    public static WindowInsetsCompat dispatchApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
        return IMPL.dispatchApplyWindowInsets(view, windowInsetsCompat);
    }

    public static float getAlpha(View view) {
        return IMPL.getAlpha(view);
    }

    public static ColorStateList getBackgroundTintList(View view) {
        return IMPL.getBackgroundTintList(view);
    }

    public static PorterDuff.Mode getBackgroundTintMode(View view) {
        return IMPL.getBackgroundTintMode(view);
    }

    public static float getElevation(View view) {
        return IMPL.getElevation(view);
    }

    public static boolean getFitsSystemWindows(View view) {
        return IMPL.getFitsSystemWindows(view);
    }

    public static int getImportantForAccessibility(View view) {
        return IMPL.getImportantForAccessibility(view);
    }

    public static int getLayerType(View view) {
        return IMPL.getLayerType(view);
    }

    public static int getLayoutDirection(View view) {
        return IMPL.getLayoutDirection(view);
    }

    @Nullable
    public static Matrix getMatrix(View view) {
        return IMPL.getMatrix(view);
    }

    public static int getMeasuredState(View view) {
        return IMPL.getMeasuredState(view);
    }

    public static int getMeasuredWidthAndState(View view) {
        return IMPL.getMeasuredWidthAndState(view);
    }

    public static int getMinimumHeight(View view) {
        return IMPL.getMinimumHeight(view);
    }

    public static int getMinimumWidth(View view) {
        return IMPL.getMinimumWidth(view);
    }

    public static int getOverScrollMode(View view) {
        return IMPL.getOverScrollMode(view);
    }

    public static ViewParent getParentForAccessibility(View view) {
        return IMPL.getParentForAccessibility(view);
    }

    public static float getScaleX(View view) {
        return IMPL.getScaleX(view);
    }

    public static float getTranslationX(View view) {
        return IMPL.getTranslationX(view);
    }

    public static float getTranslationY(View view) {
        return IMPL.getTranslationY(view);
    }

    public static int getWindowSystemUiVisibility(View view) {
        return IMPL.getWindowSystemUiVisibility(view);
    }

    public static float getY(View view) {
        return IMPL.getY(view);
    }

    public static boolean hasAccessibilityDelegate(View view) {
        return IMPL.hasAccessibilityDelegate(view);
    }

    public static boolean hasOverlappingRendering(View view) {
        return IMPL.hasOverlappingRendering(view);
    }

    public static boolean hasTransientState(View view) {
        return IMPL.hasTransientState(view);
    }

    public static boolean isAttachedToWindow(View view) {
        return IMPL.isAttachedToWindow(view);
    }

    public static boolean isLaidOut(View view) {
        return IMPL.isLaidOut(view);
    }

    public static boolean isNestedScrollingEnabled(View view) {
        return IMPL.isNestedScrollingEnabled(view);
    }

    public static boolean isOpaque(View view) {
        return IMPL.isOpaque(view);
    }

    public static void jumpDrawablesToCurrentState(View view) {
        IMPL.jumpDrawablesToCurrentState(view);
    }

    public static void offsetLeftAndRight(View view, int i) {
        IMPL.offsetLeftAndRight(view, i);
    }

    public static void offsetTopAndBottom(View view, int i) {
        IMPL.offsetTopAndBottom(view, i);
    }

    public static WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
        return IMPL.onApplyWindowInsets(view, windowInsetsCompat);
    }

    public static void postInvalidateOnAnimation(View view) {
        IMPL.postInvalidateOnAnimation(view);
    }

    public static void postInvalidateOnAnimation(View view, int i, int i2, int i3, int i4) {
        IMPL.postInvalidateOnAnimation(view, i, i2, i3, i4);
    }

    public static void postOnAnimation(View view, Runnable runnable) {
        IMPL.postOnAnimation(view, runnable);
    }

    public static void postOnAnimationDelayed(View view, Runnable runnable, long j) {
        IMPL.postOnAnimationDelayed(view, runnable, j);
    }

    public static void requestApplyInsets(View view) {
        IMPL.requestApplyInsets(view);
    }

    public static int resolveSizeAndState(int i, int i2, int i3) {
        return IMPL.resolveSizeAndState(i, i2, i3);
    }

    public static void setAccessibilityDelegate(View view, AccessibilityDelegateCompat accessibilityDelegateCompat) {
        IMPL.setAccessibilityDelegate(view, accessibilityDelegateCompat);
    }

    public static void setActivated(View view, boolean z) {
        IMPL.setActivated(view, z);
    }

    public static void setAlpha(View view, @FloatRange(from = 0.0d, to = 1.0d) float f) {
        IMPL.setAlpha(view, f);
    }

    public static void setBackgroundTintList(View view, ColorStateList colorStateList) {
        IMPL.setBackgroundTintList(view, colorStateList);
    }

    public static void setBackgroundTintMode(View view, PorterDuff.Mode mode) {
        IMPL.setBackgroundTintMode(view, mode);
    }

    public static void setChildrenDrawingOrderEnabled(ViewGroup viewGroup, boolean z) {
        IMPL.setChildrenDrawingOrderEnabled(viewGroup, z);
    }

    public static void setElevation(View view, float f) {
        IMPL.setElevation(view, f);
    }

    public static void setImportantForAccessibility(View view, int i) {
        IMPL.setImportantForAccessibility(view, i);
    }

    public static void setLayerPaint(View view, Paint paint) {
        IMPL.setLayerPaint(view, paint);
    }

    public static void setLayerType(View view, int i, Paint paint) {
        IMPL.setLayerType(view, i, paint);
    }

    public static void setOnApplyWindowInsetsListener(View view, OnApplyWindowInsetsListener onApplyWindowInsetsListener) {
        IMPL.setOnApplyWindowInsetsListener(view, onApplyWindowInsetsListener);
    }

    public static void setSaveFromParentEnabled(View view, boolean z) {
        IMPL.setSaveFromParentEnabled(view, z);
    }

    public static void setScaleX(View view, float f) {
        IMPL.setScaleX(view, f);
    }

    public static void setScaleY(View view, float f) {
        IMPL.setScaleY(view, f);
    }

    public static void setTranslationX(View view, float f) {
        IMPL.setTranslationX(view, f);
    }

    public static void setTranslationY(View view, float f) {
        IMPL.setTranslationY(view, f);
    }

    public static void stopNestedScroll(View view) {
        IMPL.stopNestedScroll(view);
    }
}
