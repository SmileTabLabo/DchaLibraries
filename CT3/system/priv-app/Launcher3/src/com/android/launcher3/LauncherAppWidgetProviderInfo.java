package com.android.launcher3;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
/* loaded from: a.zip:com/android/launcher3/LauncherAppWidgetProviderInfo.class */
public class LauncherAppWidgetProviderInfo extends AppWidgetProviderInfo {
    public boolean isCustomWidget;
    public int minSpanX;
    public int minSpanY;
    public int spanX;
    public int spanY;

    public LauncherAppWidgetProviderInfo(Parcel parcel) {
        super(parcel);
        this.isCustomWidget = false;
        initSpans();
    }

    public static LauncherAppWidgetProviderInfo fromProviderInfo(Context context, AppWidgetProviderInfo appWidgetProviderInfo) {
        Parcel obtain = Parcel.obtain();
        appWidgetProviderInfo.writeToParcel(obtain, 0);
        obtain.setDataPosition(0);
        LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo = new LauncherAppWidgetProviderInfo(obtain);
        obtain.recycle();
        return launcherAppWidgetProviderInfo;
    }

    @TargetApi(21)
    public Drawable getIcon(Context context, IconCache iconCache) {
        return this.isCustomWidget ? iconCache.getFullResIcon(this.provider.getPackageName(), this.icon) : super.loadIcon(context, LauncherAppState.getInstance().getInvariantDeviceProfile().fillResIconDpi);
    }

    @TargetApi(21)
    public String getLabel(PackageManager packageManager) {
        return this.isCustomWidget ? Utilities.trim(this.label) : super.loadLabel(packageManager);
    }

    public Point getMinSpans(InvariantDeviceProfile invariantDeviceProfile, Context context) {
        int i = -1;
        int i2 = (this.resizeMode & 1) != 0 ? this.minSpanX : -1;
        if ((this.resizeMode & 2) != 0) {
            i = this.minSpanY;
        }
        return new Point(i2, i);
    }

    public void initSpans() {
        LauncherAppState launcherAppState = LauncherAppState.getInstance();
        InvariantDeviceProfile invariantDeviceProfile = launcherAppState.getInvariantDeviceProfile();
        Rect workspacePadding = invariantDeviceProfile.landscapeProfile.getWorkspacePadding(false);
        Rect workspacePadding2 = invariantDeviceProfile.portraitProfile.getWorkspacePadding(false);
        float calculateCellWidth = DeviceProfile.calculateCellWidth(Math.min((invariantDeviceProfile.landscapeProfile.widthPx - workspacePadding.left) - workspacePadding.right, (invariantDeviceProfile.portraitProfile.widthPx - workspacePadding2.left) - workspacePadding2.right), invariantDeviceProfile.numColumns);
        float calculateCellWidth2 = DeviceProfile.calculateCellWidth(Math.min((invariantDeviceProfile.landscapeProfile.heightPx - workspacePadding.top) - workspacePadding.bottom, (invariantDeviceProfile.portraitProfile.heightPx - workspacePadding2.top) - workspacePadding2.bottom), invariantDeviceProfile.numRows);
        Rect defaultPaddingForWidget = AppWidgetHostView.getDefaultPaddingForWidget(launcherAppState.getContext(), this.provider, null);
        this.spanX = Math.max(1, (int) Math.ceil(((this.minWidth + defaultPaddingForWidget.left) + defaultPaddingForWidget.right) / calculateCellWidth));
        this.spanY = Math.max(1, (int) Math.ceil(((this.minHeight + defaultPaddingForWidget.top) + defaultPaddingForWidget.bottom) / calculateCellWidth2));
        this.minSpanX = Math.max(1, (int) Math.ceil(((this.minResizeWidth + defaultPaddingForWidget.left) + defaultPaddingForWidget.right) / calculateCellWidth));
        this.minSpanY = Math.max(1, (int) Math.ceil(((this.minResizeHeight + defaultPaddingForWidget.top) + defaultPaddingForWidget.bottom) / calculateCellWidth2));
    }
}
