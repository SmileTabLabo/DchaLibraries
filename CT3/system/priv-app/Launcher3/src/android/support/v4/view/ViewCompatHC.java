package android.support.v4.view;

import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;
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

    public static Matrix getMatrix(View view) {
        return view.getMatrix();
    }

    public static float getTranslationX(View view) {
        return view.getTranslationX();
    }

    public static float getTranslationY(View view) {
        return view.getTranslationY();
    }

    public static void setAlpha(View view, float f) {
        view.setAlpha(f);
    }

    public static void setLayerType(View view, int i, Paint paint) {
        view.setLayerType(i, paint);
    }

    public static void setTranslationX(View view, float f) {
        view.setTranslationX(f);
    }

    public static void setTranslationY(View view, float f) {
        view.setTranslationY(f);
    }
}
