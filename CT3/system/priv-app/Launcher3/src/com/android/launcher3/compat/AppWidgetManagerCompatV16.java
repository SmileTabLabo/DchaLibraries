package com.android.launcher3.compat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.android.launcher3.IconCache;
import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.ComponentKey;
import java.util.HashMap;
import java.util.List;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/launcher3/compat/AppWidgetManagerCompatV16.class */
public class AppWidgetManagerCompatV16 extends AppWidgetManagerCompat {
    /* JADX INFO: Access modifiers changed from: package-private */
    public AppWidgetManagerCompatV16(Context context) {
        super(context);
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    @TargetApi(17)
    public boolean bindAppWidgetIdIfAllowed(int i, AppWidgetProviderInfo appWidgetProviderInfo, Bundle bundle) {
        return Utilities.ATLEAST_JB_MR1 ? this.mAppWidgetManager.bindAppWidgetIdIfAllowed(i, appWidgetProviderInfo.provider, bundle) : this.mAppWidgetManager.bindAppWidgetIdIfAllowed(i, appWidgetProviderInfo.provider);
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public LauncherAppWidgetProviderInfo findProvider(ComponentName componentName, UserHandleCompat userHandleCompat) {
        for (AppWidgetProviderInfo appWidgetProviderInfo : this.mAppWidgetManager.getInstalledProviders()) {
            if (appWidgetProviderInfo.provider.equals(componentName)) {
                return LauncherAppWidgetProviderInfo.fromProviderInfo(this.mContext, appWidgetProviderInfo);
            }
        }
        return null;
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public List<AppWidgetProviderInfo> getAllProviders() {
        return this.mAppWidgetManager.getInstalledProviders();
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public HashMap<ComponentKey, AppWidgetProviderInfo> getAllProvidersMap() {
        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap = new HashMap<>();
        UserHandleCompat myUserHandle = UserHandleCompat.myUserHandle();
        for (AppWidgetProviderInfo appWidgetProviderInfo : this.mAppWidgetManager.getInstalledProviders()) {
            hashMap.put(new ComponentKey(appWidgetProviderInfo.provider, myUserHandle), appWidgetProviderInfo);
        }
        return hashMap;
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public Bitmap getBadgeBitmap(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, Bitmap bitmap, int i, int i2) {
        return bitmap;
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public UserHandleCompat getUser(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo) {
        return UserHandleCompat.myUserHandle();
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public Drawable loadIcon(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, IconCache iconCache) {
        return iconCache.getFullResIcon(launcherAppWidgetProviderInfo.provider.getPackageName(), launcherAppWidgetProviderInfo.icon);
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public String loadLabel(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo) {
        return Utilities.trim(launcherAppWidgetProviderInfo.label);
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public Drawable loadPreview(AppWidgetProviderInfo appWidgetProviderInfo) {
        return this.mContext.getPackageManager().getDrawable(appWidgetProviderInfo.provider.getPackageName(), appWidgetProviderInfo.previewImage, null);
    }

    @Override // com.android.launcher3.compat.AppWidgetManagerCompat
    public void startConfigActivity(AppWidgetProviderInfo appWidgetProviderInfo, int i, Activity activity, AppWidgetHost appWidgetHost, int i2) {
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_CONFIGURE");
        intent.setComponent(appWidgetProviderInfo.configure);
        intent.putExtra("appWidgetId", i);
        Utilities.startActivityForResultSafely(activity, intent, i2);
    }
}
