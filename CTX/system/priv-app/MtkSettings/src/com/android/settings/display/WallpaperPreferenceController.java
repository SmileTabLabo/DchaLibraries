package com.android.settings.display;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.List;
/* loaded from: classes.dex */
public class WallpaperPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private final String mWallpaperClass;
    private final String mWallpaperPackage;

    public WallpaperPreferenceController(Context context) {
        super(context);
        this.mWallpaperPackage = this.mContext.getString(R.string.config_wallpaper_picker_package);
        this.mWallpaperClass = this.mContext.getString(R.string.config_wallpaper_picker_class);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        if (TextUtils.isEmpty(this.mWallpaperPackage) || TextUtils.isEmpty(this.mWallpaperClass)) {
            Log.e("WallpaperPrefController", "No Wallpaper picker specified!");
            return false;
        }
        ComponentName componentName = new ComponentName(this.mWallpaperPackage, this.mWallpaperClass);
        PackageManager packageManager = this.mContext.getPackageManager();
        Intent intent = new Intent();
        intent.setComponent(componentName);
        List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(intent, 0);
        return (queryIntentActivities == null || queryIntentActivities.size() == 0) ? false : true;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "wallpaper";
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        disablePreferenceIfManaged((RestrictedPreference) preference);
    }

    private void disablePreferenceIfManaged(RestrictedPreference restrictedPreference) {
        if (restrictedPreference != null) {
            restrictedPreference.setDisabledByAdmin(null);
            if (RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_set_wallpaper", UserHandle.myUserId())) {
                restrictedPreference.setEnabled(false);
            } else {
                restrictedPreference.checkRestrictionAndSetDisabled("no_set_wallpaper");
            }
        }
    }
}
