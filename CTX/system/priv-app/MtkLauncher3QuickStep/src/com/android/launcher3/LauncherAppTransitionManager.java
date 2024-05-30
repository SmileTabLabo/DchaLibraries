package com.android.launcher3;

import android.app.ActivityOptions;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
/* loaded from: classes.dex */
public class LauncherAppTransitionManager {
    public static LauncherAppTransitionManager newInstance(Context context) {
        return (LauncherAppTransitionManager) Utilities.getOverrideObject(LauncherAppTransitionManager.class, context, R.string.app_transition_manager_class);
    }

    public ActivityOptions getActivityLaunchOptions(Launcher launcher, View view) {
        int i;
        int i2;
        Drawable icon;
        if (Utilities.ATLEAST_MARSHMALLOW) {
            int measuredWidth = view.getMeasuredWidth();
            int measuredHeight = view.getMeasuredHeight();
            int i3 = 0;
            if ((view instanceof BubbleTextView) && (icon = ((BubbleTextView) view).getIcon()) != null) {
                Rect bounds = icon.getBounds();
                i3 = (measuredWidth - bounds.width()) / 2;
                i2 = view.getPaddingTop();
                i = bounds.width();
                measuredHeight = bounds.height();
            } else {
                i = measuredWidth;
                i2 = 0;
            }
            return ActivityOptions.makeClipRevealAnimation(view, i3, i2, i, measuredHeight);
        } else if (Utilities.ATLEAST_LOLLIPOP_MR1) {
            return ActivityOptions.makeCustomAnimation(launcher, R.anim.task_open_enter, R.anim.no_anim);
        } else {
            return null;
        }
    }
}
