package com.android.systemui.statusbar;

import android.view.View;
import com.android.systemui.Interpolators;
/* loaded from: a.zip:com/android/systemui/statusbar/CrossFadeHelper.class */
public class CrossFadeHelper {
    public static void fadeIn(View view) {
        view.animate().cancel();
        if (view.getVisibility() == 4) {
            view.setAlpha(0.0f);
            view.setVisibility(0);
        }
        view.animate().alpha(1.0f).setDuration(210L).setInterpolator(Interpolators.ALPHA_IN).withEndAction(null);
        if (view.hasOverlappingRendering()) {
            view.animate().withLayer();
        }
    }

    public static void fadeIn(View view, float f) {
        view.animate().cancel();
        if (view.getVisibility() == 4) {
            view.setVisibility(0);
        }
        float interpolation = Interpolators.ALPHA_IN.getInterpolation(mapToFadeDuration(f));
        view.setAlpha(interpolation);
        updateLayerType(view, interpolation);
    }

    public static void fadeOut(View view, float f) {
        view.animate().cancel();
        if (f == 1.0f) {
            view.setVisibility(4);
        } else if (view.getVisibility() == 4) {
            view.setVisibility(0);
        }
        float interpolation = Interpolators.ALPHA_OUT.getInterpolation(1.0f - mapToFadeDuration(f));
        view.setAlpha(interpolation);
        updateLayerType(view, interpolation);
    }

    public static void fadeOut(View view, Runnable runnable) {
        view.animate().cancel();
        view.animate().alpha(0.0f).setDuration(210L).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable(runnable, view) { // from class: com.android.systemui.statusbar.CrossFadeHelper.1
            final Runnable val$endRunnable;
            final View val$view;

            {
                this.val$endRunnable = runnable;
                this.val$view = view;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.val$endRunnable != null) {
                    this.val$endRunnable.run();
                }
                this.val$view.setVisibility(4);
            }
        });
        if (view.hasOverlappingRendering()) {
            view.animate().withLayer();
        }
    }

    private static float mapToFadeDuration(float f) {
        return Math.min(f / 0.5833333f, 1.0f);
    }

    private static void updateLayerType(View view, float f) {
        if (view.hasOverlappingRendering() && f > 0.0f && f < 1.0f) {
            view.setLayerType(2, null);
        } else if (view.getLayerType() == 2) {
            view.setLayerType(0, null);
        }
    }
}
