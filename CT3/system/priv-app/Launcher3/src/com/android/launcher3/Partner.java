package com.android.launcher3;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import java.io.File;
/* loaded from: a.zip:com/android/launcher3/Partner.class */
public class Partner {
    private static Partner sPartner;
    private static boolean sSearched = false;
    private final String mPackageName;
    private final Resources mResources;

    private Partner(String str, Resources resources) {
        this.mPackageName = str;
        this.mResources = resources;
    }

    public static Partner get(PackageManager packageManager) {
        Partner partner;
        synchronized (Partner.class) {
            try {
                if (!sSearched) {
                    Pair<String, Resources> findSystemApk = Utilities.findSystemApk("com.android.launcher3.action.PARTNER_CUSTOMIZATION", packageManager);
                    if (findSystemApk != null) {
                        sPartner = new Partner((String) findSystemApk.first, (Resources) findSystemApk.second);
                    }
                    sSearched = true;
                }
                partner = sPartner;
            } catch (Throwable th) {
                throw th;
            }
        }
        return partner;
    }

    public void applyInvariantDeviceProfileOverrides(InvariantDeviceProfile invariantDeviceProfile, DisplayMetrics displayMetrics) {
        int i = -1;
        int i2 = -1;
        float f = -1.0f;
        try {
            int identifier = getResources().getIdentifier("grid_num_rows", "integer", getPackageName());
            if (identifier > 0) {
                i = getResources().getInteger(identifier);
            }
            int identifier2 = getResources().getIdentifier("grid_num_columns", "integer", getPackageName());
            if (identifier2 > 0) {
                i2 = getResources().getInteger(identifier2);
            }
            int identifier3 = getResources().getIdentifier("grid_icon_size_dp", "dimen", getPackageName());
            if (identifier3 > 0) {
                f = Utilities.dpiFromPx(getResources().getDimensionPixelSize(identifier3), displayMetrics);
            }
            if (i > 0 && i2 > 0) {
                invariantDeviceProfile.numRows = i;
                invariantDeviceProfile.numColumns = i2;
            }
            if (f > 0.0f) {
                invariantDeviceProfile.iconSize = f;
            }
        } catch (Resources.NotFoundException e) {
            Log.e("Launcher.Partner", "Invalid Partner grid resource!", e);
        }
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public Resources getResources() {
        return this.mResources;
    }

    public File getWallpaperDirectory() {
        int identifier = getResources().getIdentifier("system_wallpaper_directory", "string", getPackageName());
        return identifier != 0 ? new File(getResources().getString(identifier)) : null;
    }

    public boolean hasDefaultLayout() {
        boolean z = false;
        if (getResources().getIdentifier("partner_default_layout", "xml", getPackageName()) != 0) {
            z = true;
        }
        return z;
    }

    public boolean hideDefaultWallpaper() {
        boolean z = false;
        int identifier = getResources().getIdentifier("default_wallpapper_hidden", "bool", getPackageName());
        if (identifier != 0) {
            z = getResources().getBoolean(identifier);
        }
        return z;
    }
}
