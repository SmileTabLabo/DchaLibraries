package com.android.systemui.shared.recents.utilities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.RectEvaluator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.ArraySet;
import android.util.IntProperty;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewStub;
import java.util.ArrayList;
import java.util.Collections;
/* loaded from: classes.dex */
public class Utilities {
    public static final Property<Drawable, Integer> DRAWABLE_ALPHA = new IntProperty<Drawable>("drawableAlpha") { // from class: com.android.systemui.shared.recents.utilities.Utilities.1
        @Override // android.util.IntProperty
        public void setValue(Drawable drawable, int i) {
            drawable.setAlpha(i);
        }

        @Override // android.util.Property
        public Integer get(Drawable drawable) {
            return Integer.valueOf(drawable.getAlpha());
        }
    };
    public static final Property<Drawable, Rect> DRAWABLE_RECT = new Property<Drawable, Rect>(Rect.class, "drawableBounds") { // from class: com.android.systemui.shared.recents.utilities.Utilities.2
        @Override // android.util.Property
        public void set(Drawable drawable, Rect rect) {
            drawable.setBounds(rect);
        }

        @Override // android.util.Property
        public Rect get(Drawable drawable) {
            return drawable.getBounds();
        }
    };
    public static final RectFEvaluator RECTF_EVALUATOR = new RectFEvaluator();
    public static final RectEvaluator RECT_EVALUATOR = new RectEvaluator(new Rect());

    public static <T extends View> T findParent(View view, Class<T> cls) {
        for (ViewParent parent = view.getParent(); parent != null; parent = parent.getParent()) {
            if (cls.isAssignableFrom(parent.getClass())) {
                return (T) parent;
            }
        }
        return null;
    }

    public static <T> ArraySet<T> arrayToSet(T[] tArr, ArraySet<T> arraySet) {
        arraySet.clear();
        if (tArr != null) {
            Collections.addAll(arraySet, tArr);
        }
        return arraySet;
    }

    public static float clamp(float f, float f2, float f3) {
        return Math.max(f2, Math.min(f3, f));
    }

    public static int clamp(int i, int i2, int i3) {
        return Math.max(i2, Math.min(i3, i));
    }

    public static float clamp01(float f) {
        return Math.max(0.0f, Math.min(1.0f, f));
    }

    public static float mapRange(float f, float f2, float f3) {
        return f2 + (f * (f3 - f2));
    }

    public static float unmapRange(float f, float f2, float f3) {
        return (f - f2) / (f3 - f2);
    }

    public static void scaleRectAboutCenter(RectF rectF, float f) {
        if (f != 1.0f) {
            float centerX = rectF.centerX();
            float centerY = rectF.centerY();
            rectF.offset(-centerX, -centerY);
            rectF.left *= f;
            rectF.top *= f;
            rectF.right *= f;
            rectF.bottom *= f;
            rectF.offset(centerX, centerY);
        }
    }

    public static float computeContrastBetweenColors(int i, int i2) {
        float red = Color.red(i) / 255.0f;
        float green = Color.green(i) / 255.0f;
        float blue = Color.blue(i) / 255.0f;
        float pow = ((red < 0.03928f ? red / 12.92f : (float) Math.pow((red + 0.055f) / 1.055f, 2.4000000953674316d)) * 0.2126f) + ((green < 0.03928f ? green / 12.92f : (float) Math.pow((green + 0.055f) / 1.055f, 2.4000000953674316d)) * 0.7152f) + ((blue < 0.03928f ? blue / 12.92f : (float) Math.pow((blue + 0.055f) / 1.055f, 2.4000000953674316d)) * 0.0722f);
        float red2 = Color.red(i2) / 255.0f;
        float green2 = Color.green(i2) / 255.0f;
        float blue2 = Color.blue(i2) / 255.0f;
        return Math.abs(((((0.2126f * (red2 < 0.03928f ? red2 / 12.92f : (float) Math.pow((red2 + 0.055f) / 1.055f, 2.4000000953674316d))) + (0.7152f * (green2 < 0.03928f ? green2 / 12.92f : (float) Math.pow((green2 + 0.055f) / 1.055f, 2.4000000953674316d)))) + (0.0722f * (blue2 < 0.03928f ? blue2 / 12.92f : (float) Math.pow((blue2 + 0.055f) / 1.055f, 2.4000000953674316d)))) + 0.05f) / (pow + 0.05f));
    }

    public static int getColorWithOverlay(int i, int i2, float f) {
        float f2 = 1.0f - f;
        return Color.rgb((int) ((Color.red(i) * f) + (Color.red(i2) * f2)), (int) ((Color.green(i) * f) + (Color.green(i2) * f2)), (int) ((f * Color.blue(i)) + (f2 * Color.blue(i2))));
    }

    public static void cancelAnimationWithoutCallbacks(Animator animator) {
        if (animator != null && animator.isStarted()) {
            removeAnimationListenersRecursive(animator);
            animator.cancel();
        }
    }

    public static void removeAnimationListenersRecursive(Animator animator) {
        if (animator instanceof AnimatorSet) {
            ArrayList<Animator> childAnimations = ((AnimatorSet) animator).getChildAnimations();
            for (int size = childAnimations.size() - 1; size >= 0; size--) {
                removeAnimationListenersRecursive(childAnimations.get(size));
            }
        }
        animator.removeAllListeners();
    }

    public static void setViewFrameFromTranslation(View view) {
        RectF rectF = new RectF(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        rectF.offset(view.getTranslationX(), view.getTranslationY());
        view.setTranslationX(0.0f);
        view.setTranslationY(0.0f);
        view.setLeftTopRightBottom((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
    }

    public static ViewStub findViewStubById(View view, int i) {
        return (ViewStub) view.findViewById(i);
    }

    public static ViewStub findViewStubById(Activity activity, int i) {
        return (ViewStub) activity.findViewById(i);
    }

    public static boolean isDescendentAccessibilityFocused(View view) {
        if (view.isAccessibilityFocused()) {
            return true;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (isDescendentAccessibilityFocused(viewGroup.getChildAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Configuration getAppConfiguration(Context context) {
        return context.getApplicationContext().getResources().getConfiguration();
    }

    public static String dumpRect(Rect rect) {
        if (rect == null) {
            return "N:0,0-0,0";
        }
        return rect.left + "," + rect.top + "-" + rect.right + "," + rect.bottom;
    }
}
