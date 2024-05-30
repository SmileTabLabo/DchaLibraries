package com.android.launcher3;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Parcel;
/* loaded from: classes.dex */
public class LauncherAppWidgetProviderInfo extends AppWidgetProviderInfo {
    public static final String CLS_CUSTOM_WIDGET_PREFIX = "#custom-widget-";
    public int minSpanX;
    public int minSpanY;
    public int spanX;
    public int spanY;

    public static LauncherAppWidgetProviderInfo fromProviderInfo(Context context, AppWidgetProviderInfo appWidgetProviderInfo) {
        LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo;
        if (appWidgetProviderInfo instanceof LauncherAppWidgetProviderInfo) {
            launcherAppWidgetProviderInfo = (LauncherAppWidgetProviderInfo) appWidgetProviderInfo;
        } else {
            Parcel obtain = Parcel.obtain();
            appWidgetProviderInfo.writeToParcel(obtain, 0);
            obtain.setDataPosition(0);
            launcherAppWidgetProviderInfo = new LauncherAppWidgetProviderInfo(obtain);
            obtain.recycle();
        }
        launcherAppWidgetProviderInfo.initSpans(context);
        return launcherAppWidgetProviderInfo;
    }

    protected LauncherAppWidgetProviderInfo() {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public LauncherAppWidgetProviderInfo(Parcel parcel) {
        super(parcel);
    }

    public void initSpans(Context context) {
        InvariantDeviceProfile idp = LauncherAppState.getIDP(context);
        Point totalWorkspacePadding = idp.landscapeProfile.getTotalWorkspacePadding();
        Point totalWorkspacePadding2 = idp.portraitProfile.getTotalWorkspacePadding();
        float calculateCellWidth = DeviceProfile.calculateCellWidth(Math.min(idp.landscapeProfile.widthPx - totalWorkspacePadding.x, idp.portraitProfile.widthPx - totalWorkspacePadding2.x), idp.numColumns);
        float calculateCellWidth2 = DeviceProfile.calculateCellWidth(Math.min(idp.landscapeProfile.heightPx - totalWorkspacePadding.y, idp.portraitProfile.heightPx - totalWorkspacePadding2.y), idp.numRows);
        Rect defaultPaddingForWidget = AppWidgetHostView.getDefaultPaddingForWidget(context, this.provider, null);
        this.spanX = Math.max(1, (int) Math.ceil(((this.minWidth + defaultPaddingForWidget.left) + defaultPaddingForWidget.right) / calculateCellWidth));
        this.spanY = Math.max(1, (int) Math.ceil(((this.minHeight + defaultPaddingForWidget.top) + defaultPaddingForWidget.bottom) / calculateCellWidth2));
        this.minSpanX = Math.max(1, (int) Math.ceil(((this.minResizeWidth + defaultPaddingForWidget.left) + defaultPaddingForWidget.right) / calculateCellWidth));
        this.minSpanY = Math.max(1, (int) Math.ceil(((this.minResizeHeight + defaultPaddingForWidget.top) + defaultPaddingForWidget.bottom) / calculateCellWidth2));
    }

    public String getLabel(PackageManager packageManager) {
        return super.loadLabel(packageManager);
    }

    public Point getMinSpans() {
        return new Point((this.resizeMode & 1) != 0 ? this.minSpanX : -1, (this.resizeMode & 2) != 0 ? this.minSpanY : -1);
    }

    public boolean isCustomWidget() {
        return this.provider.getClassName().startsWith(CLS_CUSTOM_WIDGET_PREFIX);
    }

    public int getWidgetFeatures() {
        if (Utilities.ATLEAST_P) {
            return this.widgetFeatures;
        }
        return 0;
    }
}
