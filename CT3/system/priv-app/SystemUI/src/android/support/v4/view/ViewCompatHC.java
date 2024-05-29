package android.support.v4.view;

import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewParent;
/* loaded from: a.zip:android/support/v4/view/ViewCompatHC.class */
class ViewCompatHC {
    ViewCompatHC() {
    }

    public static float getAlpha(View view) {
        return view.getAlpha();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static long getFrameTime() {
        return ValueAnimator.getFrameDelay();
    }

    public static int getLayerType(View view) {
        return view.getLayerType();
    }

    public static Matrix getMatrix(View view) {
        return view.getMatrix();
    }

    public static int getMeasuredState(View view) {
        return view.getMeasuredState();
    }

    public static int getMeasuredWidthAndState(View view) {
        return view.getMeasuredWidthAndState();
    }

    public static float getScaleX(View view) {
        return view.getScaleX();
    }

    public static float getTranslationX(View view) {
        return view.getTranslationX();
    }

    public static float getTranslationY(View view) {
        return view.getTranslationY();
    }

    public static float getY(View view) {
        return view.getY();
    }

    public static void jumpDrawablesToCurrentState(View view) {
        view.jumpDrawablesToCurrentState();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void offsetLeftAndRight(View view, int i) {
        view.offsetLeftAndRight(i);
        tickleInvalidationFlag(view);
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            tickleInvalidationFlag((View) parent);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void offsetTopAndBottom(View view, int i) {
        view.offsetTopAndBottom(i);
        tickleInvalidationFlag(view);
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            tickleInvalidationFlag((View) parent);
        }
    }

    public static int resolveSizeAndState(int i, int i2, int i3) {
        return View.resolveSizeAndState(i, i2, i3);
    }

    public static void setActivated(View view, boolean z) {
        view.setActivated(z);
    }

    public static void setAlpha(View view, float f) {
        view.setAlpha(f);
    }

    public static void setLayerType(View view, int i, Paint paint) {
        view.setLayerType(i, paint);
    }

    public static void setSaveFromParentEnabled(View view, boolean z) {
        view.setSaveFromParentEnabled(z);
    }

    public static void setScaleX(View view, float f) {
        view.setScaleX(f);
    }

    public static void setScaleY(View view, float f) {
        view.setScaleY(f);
    }

    public static void setTranslationX(View view, float f) {
        view.setTranslationX(f);
    }

    public static void setTranslationY(View view, float f) {
        view.setTranslationY(f);
    }

    private static void tickleInvalidationFlag(View view) {
        float translationY = view.getTranslationY();
        view.setTranslationY(1.0f + translationY);
        view.setTranslationY(translationY);
    }
}
