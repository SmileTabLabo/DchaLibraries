package com.android.launcher3.util;

import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.WindowManager;
import com.android.launcher3.Utilities;
/* loaded from: a.zip:com/android/launcher3/util/WallpaperUtils.class */
public final class WallpaperUtils {
    private static Point sDefaultWallpaperSize;

    @TargetApi(17)
    public static Point getDefaultWallpaperSize(Resources resources, WindowManager windowManager) {
        if (sDefaultWallpaperSize == null) {
            Point point = new Point();
            Point point2 = new Point();
            windowManager.getDefaultDisplay().getCurrentSizeRange(point, point2);
            int max = Math.max(point2.x, point2.y);
            int max2 = Math.max(point.x, point.y);
            if (Utilities.ATLEAST_JB_MR1) {
                Point point3 = new Point();
                windowManager.getDefaultDisplay().getRealSize(point3);
                max = Math.max(point3.x, point3.y);
                max2 = Math.min(point3.x, point3.y);
            }
            sDefaultWallpaperSize = new Point(resources.getConfiguration().smallestScreenWidthDp >= 720 ? (int) (max * wallpaperTravelToScreenWidthRatio(max, max2)) : Math.max((int) (max2 * 2.0f), max), max);
        }
        return sDefaultWallpaperSize;
    }

    /* JADX WARN: Code restructure failed: missing block: B:5:0x0029, code lost:
        if (r0 == (-1)) goto L13;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static void suggestWallpaperDimension(Resources resources, SharedPreferences sharedPreferences, WindowManager windowManager, WallpaperManager wallpaperManager, boolean z) {
        int i;
        Point defaultWallpaperSize = getDefaultWallpaperSize(resources, windowManager);
        int i2 = sharedPreferences.getInt("wallpaper.width", -1);
        int i3 = sharedPreferences.getInt("wallpaper.height", -1);
        if (i2 != -1) {
            i = i3;
        }
        if (z) {
            i2 = defaultWallpaperSize.x;
            i = defaultWallpaperSize.y;
            if (i2 == wallpaperManager.getDesiredMinimumWidth() && i == wallpaperManager.getDesiredMinimumHeight()) {
                return;
            }
            wallpaperManager.suggestDesiredDimensions(i2, i);
        }
    }

    public static float wallpaperTravelToScreenWidthRatio(int i, int i2) {
        return (0.30769226f * (i / i2)) + 1.0076923f;
    }
}
