package com.android.launcher3.widget;

import android.appwidget.AppWidgetHostView;
import android.os.Bundle;
import android.os.Parcelable;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.PendingAddItemInfo;
import com.android.launcher3.compat.AppWidgetManagerCompat;
/* loaded from: a.zip:com/android/launcher3/widget/PendingAddWidgetInfo.class */
public class PendingAddWidgetInfo extends PendingAddItemInfo {
    public Bundle bindOptions = null;
    public AppWidgetHostView boundWidget;
    public int icon;
    public LauncherAppWidgetProviderInfo info;
    public int previewImage;

    public PendingAddWidgetInfo(Launcher launcher, LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, Parcelable parcelable) {
        if (launcherAppWidgetProviderInfo.isCustomWidget) {
            this.itemType = 5;
        } else {
            this.itemType = 4;
        }
        this.info = launcherAppWidgetProviderInfo;
        this.user = AppWidgetManagerCompat.getInstance(launcher).getUser(launcherAppWidgetProviderInfo);
        this.componentName = launcherAppWidgetProviderInfo.provider;
        this.previewImage = launcherAppWidgetProviderInfo.previewImage;
        this.icon = launcherAppWidgetProviderInfo.icon;
        this.spanX = launcherAppWidgetProviderInfo.spanX;
        this.spanY = launcherAppWidgetProviderInfo.spanY;
        this.minSpanX = launcherAppWidgetProviderInfo.minSpanX;
        this.minSpanY = launcherAppWidgetProviderInfo.minSpanY;
    }

    @Override // com.android.launcher3.ItemInfo
    public String toString() {
        return String.format("PendingAddWidgetInfo package=%s, name=%s", this.componentName.getPackageName(), this.componentName.getShortClassName());
    }
}
