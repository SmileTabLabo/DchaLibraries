package com.android.systemui.statusbar.notification;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import com.android.internal.util.NotificationColorUtil;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/NotificationUtils.class */
public class NotificationUtils {
    private static final int[] sLocationBase = new int[2];
    private static final int[] sLocationOffset = new int[2];

    public static float getRelativeYOffset(View view, View view2) {
        view2.getLocationOnScreen(sLocationBase);
        view.getLocationOnScreen(sLocationOffset);
        return sLocationOffset[1] - sLocationBase[1];
    }

    public static float interpolate(float f, float f2, float f3) {
        return ((1.0f - f3) * f) + (f2 * f3);
    }

    public static int interpolateColors(int i, int i2, float f) {
        return Color.argb((int) interpolate(Color.alpha(i), Color.alpha(i2), f), (int) interpolate(Color.red(i), Color.red(i2), f), (int) interpolate(Color.green(i), Color.green(i2), f), (int) interpolate(Color.blue(i), Color.blue(i2), f));
    }

    public static boolean isGrayscale(ImageView imageView, NotificationColorUtil notificationColorUtil) {
        Object tag = imageView.getTag(2131886142);
        if (tag != null) {
            return Boolean.TRUE.equals(tag);
        }
        boolean isGrayscaleIcon = notificationColorUtil.isGrayscaleIcon(imageView.getDrawable());
        imageView.setTag(2131886142, Boolean.valueOf(isGrayscaleIcon));
        return isGrayscaleIcon;
    }
}
