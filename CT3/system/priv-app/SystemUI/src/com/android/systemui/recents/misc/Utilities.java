package com.android.systemui.recents.misc;

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
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.TaskViewTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/recents/misc/Utilities.class */
public class Utilities {
    public static final Property<Drawable, Integer> DRAWABLE_ALPHA = new IntProperty<Drawable>("drawableAlpha") { // from class: com.android.systemui.recents.misc.Utilities.1
        @Override // android.util.Property
        public Integer get(Drawable drawable) {
            return Integer.valueOf(drawable.getAlpha());
        }

        @Override // android.util.IntProperty
        public void setValue(Drawable drawable, int i) {
            drawable.setAlpha(i);
        }
    };
    public static final Property<Drawable, Rect> DRAWABLE_RECT = new Property<Drawable, Rect>(Rect.class, "drawableBounds") { // from class: com.android.systemui.recents.misc.Utilities.2
        @Override // android.util.Property
        public Rect get(Drawable drawable) {
            return drawable.getBounds();
        }

        @Override // android.util.Property
        public void set(Drawable drawable, Rect rect) {
            drawable.setBounds(rect);
        }
    };
    public static final RectFEvaluator RECTF_EVALUATOR = new RectFEvaluator();
    public static final RectEvaluator RECT_EVALUATOR = new RectEvaluator(new Rect());
    public static final Rect EMPTY_RECT = new Rect();

    public static <T> ArraySet<T> arrayToSet(T[] tArr, ArraySet<T> arraySet) {
        arraySet.clear();
        if (tArr != null) {
            Collections.addAll(arraySet, tArr);
        }
        return arraySet;
    }

    public static void cancelAnimationWithoutCallbacks(Animator animator) {
        if (animator == null || !animator.isStarted()) {
            return;
        }
        removeAnimationListenersRecursive(animator);
        animator.cancel();
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

    public static float computeContrastBetweenColors(int i, int i2) {
        float red = Color.red(i) / 255.0f;
        float green = Color.green(i) / 255.0f;
        float blue = Color.blue(i) / 255.0f;
        float pow = red < 0.03928f ? red / 12.92f : (float) Math.pow((0.055f + red) / 1.055f, 2.4000000953674316d);
        float pow2 = green < 0.03928f ? green / 12.92f : (float) Math.pow((0.055f + green) / 1.055f, 2.4000000953674316d);
        float pow3 = blue < 0.03928f ? blue / 12.92f : (float) Math.pow((0.055f + blue) / 1.055f, 2.4000000953674316d);
        float red2 = Color.red(i2) / 255.0f;
        float green2 = Color.green(i2) / 255.0f;
        float blue2 = Color.blue(i2) / 255.0f;
        return Math.abs((0.05f + (((0.2126f * (red2 < 0.03928f ? red2 / 12.92f : (float) Math.pow((0.055f + red2) / 1.055f, 2.4000000953674316d))) + (0.7152f * (green2 < 0.03928f ? green2 / 12.92f : (float) Math.pow((0.055f + green2) / 1.055f, 2.4000000953674316d)))) + (0.0722f * (blue2 < 0.03928f ? blue2 / 12.92f : (float) Math.pow((0.055f + blue2) / 1.055f, 2.4000000953674316d))))) / (0.05f + (((0.2126f * pow) + (0.7152f * pow2)) + (0.0722f * pow3))));
    }

    public static String dumpRect(Rect rect) {
        return rect == null ? "N:0,0-0,0" : rect.left + "," + rect.top + "-" + rect.right + "," + rect.bottom;
    }

    public static <T extends View> T findParent(View view, Class<T> cls) {
        ViewParent parent = view.getParent();
        while (true) {
            ViewParent viewParent = parent;
            if (viewParent == null) {
                return null;
            }
            if (viewParent.getClass().equals(cls)) {
                return (T) viewParent;
            }
            parent = viewParent.getParent();
        }
    }

    public static ViewStub findViewStubById(Activity activity, int i) {
        return (ViewStub) activity.findViewById(i);
    }

    public static ViewStub findViewStubById(View view, int i) {
        return (ViewStub) view.findViewById(i);
    }

    public static Configuration getAppConfiguration(Context context) {
        return context.getApplicationContext().getResources().getConfiguration();
    }

    public static int getColorWithOverlay(int i, int i2, float f) {
        return Color.rgb((int) ((Color.red(i) * f) + ((1.0f - f) * Color.red(i2))), (int) ((Color.green(i) * f) + ((1.0f - f) * Color.green(i2))), (int) ((Color.blue(i) * f) + ((1.0f - f) * Color.blue(i2))));
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
            return false;
        }
        return false;
    }

    public static float mapRange(float f, float f2, float f3) {
        return ((f3 - f2) * f) + f2;
    }

    public static void matchTaskListSize(List<Task> list, List<TaskViewTransform> list2) {
        int size = list2.size();
        int size2 = list.size();
        if (size < size2) {
            while (size < size2) {
                list2.add(new TaskViewTransform());
                size++;
            }
        } else if (size > size2) {
            list2.subList(size2, size).clear();
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

    public static void setViewFrameFromTranslation(View view) {
        RectF rectF = new RectF(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        rectF.offset(view.getTranslationX(), view.getTranslationY());
        view.setTranslationX(0.0f);
        view.setTranslationY(0.0f);
        view.setLeftTopRightBottom((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
    }
}
