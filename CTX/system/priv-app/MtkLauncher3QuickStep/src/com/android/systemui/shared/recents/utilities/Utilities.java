package com.android.systemui.shared.recents.utilities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.RectEvaluator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Trace;
import android.util.ArraySet;
import android.util.IntProperty;
import android.util.Property;
import android.util.TypedValue;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.ViewStub;
import java.util.ArrayList;
import java.util.Collections;
/* loaded from: classes.dex */
public class Utilities {
    public static final Property<Drawable, Integer> DRAWABLE_ALPHA = new IntProperty<Drawable>("drawableAlpha") { // from class: com.android.systemui.shared.recents.utilities.Utilities.1
        @Override // android.util.IntProperty
        public void setValue(Drawable object, int alpha) {
            object.setAlpha(alpha);
        }

        @Override // android.util.Property
        public Integer get(Drawable object) {
            return Integer.valueOf(object.getAlpha());
        }
    };
    public static final Property<Drawable, Rect> DRAWABLE_RECT = new Property<Drawable, Rect>(Rect.class, "drawableBounds") { // from class: com.android.systemui.shared.recents.utilities.Utilities.2
        @Override // android.util.Property
        public void set(Drawable object, Rect bounds) {
            object.setBounds(bounds);
        }

        @Override // android.util.Property
        public Rect get(Drawable object) {
            return object.getBounds();
        }
    };
    public static final RectFEvaluator RECTF_EVALUATOR = new RectFEvaluator();
    public static final RectEvaluator RECT_EVALUATOR = new RectEvaluator(new Rect());

    public static <T extends View> T findParent(View v, Class<T> parentClass) {
        for (ViewParent parent = v.getParent(); parent != null; parent = parent.getParent()) {
            if (parentClass.isAssignableFrom(parent.getClass())) {
                return (T) parent;
            }
        }
        return null;
    }

    public static <T> ArraySet<T> objectToSet(T obj, ArraySet<T> setOut) {
        setOut.clear();
        if (obj != null) {
            setOut.add(obj);
        }
        return setOut;
    }

    public static <T> ArraySet<T> arrayToSet(T[] array, ArraySet<T> setOut) {
        setOut.clear();
        if (array != null) {
            Collections.addAll(setOut, array);
        }
        return setOut;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    public static float mapRange(float value, float min, float max) {
        return ((max - min) * value) + min;
    }

    public static float unmapRange(float value, float min, float max) {
        return (value - min) / (max - min);
    }

    public static void scaleRectAboutCenter(RectF r, float scale) {
        if (scale != 1.0f) {
            float cx = r.centerX();
            float cy = r.centerY();
            r.offset(-cx, -cy);
            r.left *= scale;
            r.top *= scale;
            r.right *= scale;
            r.bottom *= scale;
            r.offset(cx, cy);
        }
    }

    public static float computeContrastBetweenColors(int bg, int fg) {
        float fgB;
        float fgR;
        float bgR = Color.red(bg) / 255.0f;
        float bgG = Color.green(bg) / 255.0f;
        float bgB = Color.blue(bg) / 255.0f;
        float bgL = (0.2126f * (bgR < 0.03928f ? bgR / 12.92f : (float) Math.pow((bgR + 0.055f) / 1.055f, 2.4000000953674316d))) + (0.7152f * (bgG < 0.03928f ? bgG / 12.92f : (float) Math.pow((bgG + 0.055f) / 1.055f, 2.4000000953674316d))) + (0.0722f * (bgB < 0.03928f ? bgB / 12.92f : (float) Math.pow((bgB + 0.055f) / 1.055f, 2.4000000953674316d)));
        float fgR2 = Color.red(fg) / 255.0f;
        float fgG = Color.green(fg) / 255.0f;
        float fgB2 = Color.blue(fg) / 255.0f;
        if (fgR2 < 0.03928f) {
            fgR = fgR2 / 12.92f;
            fgB = fgB2;
        } else {
            fgB = fgB2;
            fgR = (float) Math.pow((fgR2 + 0.055f) / 1.055f, 2.4000000953674316d);
        }
        float fgL = (0.2126f * fgR) + (0.7152f * (fgG < 0.03928f ? fgG / 12.92f : (float) Math.pow((fgG + 0.055f) / 1.055f, 2.4000000953674316d))) + (0.0722f * (fgB < 0.03928f ? fgB / 12.92f : (float) Math.pow((fgB + 0.055f) / 1.055f, 2.4000000953674316d)));
        return Math.abs((fgL + 0.05f) / (0.05f + bgL));
    }

    public static int getColorWithOverlay(int baseColor, int overlayColor, float overlayAlpha) {
        return Color.rgb((int) ((Color.red(baseColor) * overlayAlpha) + ((1.0f - overlayAlpha) * Color.red(overlayColor))), (int) ((Color.green(baseColor) * overlayAlpha) + ((1.0f - overlayAlpha) * Color.green(overlayColor))), (int) ((Color.blue(baseColor) * overlayAlpha) + ((1.0f - overlayAlpha) * Color.blue(overlayColor))));
    }

    public static void cancelAnimationWithoutCallbacks(Animator animator) {
        if (animator != null && animator.isStarted()) {
            removeAnimationListenersRecursive(animator);
            animator.cancel();
        }
    }

    public static void removeAnimationListenersRecursive(Animator animator) {
        if (animator instanceof AnimatorSet) {
            ArrayList<Animator> animators = ((AnimatorSet) animator).getChildAnimations();
            for (int i = animators.size() - 1; i >= 0; i--) {
                removeAnimationListenersRecursive(animators.get(i));
            }
        }
        animator.removeAllListeners();
    }

    public static void setViewFrameFromTranslation(View v) {
        RectF taskViewRect = new RectF(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        taskViewRect.offset(v.getTranslationX(), v.getTranslationY());
        v.setTranslationX(0.0f);
        v.setTranslationY(0.0f);
        v.setLeftTopRightBottom((int) taskViewRect.left, (int) taskViewRect.top, (int) taskViewRect.right, (int) taskViewRect.bottom);
    }

    public static ViewStub findViewStubById(View v, int stubId) {
        return (ViewStub) v.findViewById(stubId);
    }

    public static ViewStub findViewStubById(Activity a, int stubId) {
        return (ViewStub) a.findViewById(stubId);
    }

    public static float dpToPx(Resources res, float dp) {
        return TypedValue.applyDimension(1, dp, res.getDisplayMetrics());
    }

    public static void addTraceEvent(String event) {
        Trace.traceBegin(8L, event);
        Trace.traceEnd(8L);
    }

    public static boolean isDescendentAccessibilityFocused(View v) {
        if (v.isAccessibilityFocused()) {
            return true;
        }
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            int childCount = vg.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (isDescendentAccessibilityFocused(vg.getChildAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Configuration getAppConfiguration(Context context) {
        return context.getApplicationContext().getResources().getConfiguration();
    }

    public static long getNextFrameNumber(Surface s) {
        if (s != null && s.isValid()) {
            return s.getNextFrameNumber();
        }
        return -1L;
    }

    public static Surface getSurface(View v) {
        ViewRootImpl viewRoot = v.getViewRootImpl();
        if (viewRoot == null) {
            return null;
        }
        return viewRoot.mSurface;
    }

    public static String dumpRect(Rect r) {
        if (r == null) {
            return "N:0,0-0,0";
        }
        return r.left + "," + r.top + "-" + r.right + "," + r.bottom;
    }

    public static void postAtFrontOfQueueAsynchronously(Handler h, Runnable r) {
        Message msg = h.obtainMessage().setCallback(r);
        h.sendMessageAtFrontOfQueue(msg);
    }
}
