package com.android.launcher3;

import android.appwidget.AppWidgetHostView;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import com.android.launcher3.compat.UserHandleCompat;
/* loaded from: a.zip:com/android/launcher3/LauncherAppWidgetInfo.class */
public class LauncherAppWidgetInfo extends ItemInfo {
    int appWidgetId;
    private boolean mHasNotifiedInitialWidgetSizeChanged;
    ComponentName providerName;
    int restoreStatus;
    int installProgress = -1;
    AppWidgetHostView hostView = null;

    /* JADX INFO: Access modifiers changed from: package-private */
    public LauncherAppWidgetInfo(int i, ComponentName componentName) {
        this.appWidgetId = -1;
        if (i == -100) {
            this.itemType = 5;
        } else {
            this.itemType = 4;
        }
        this.appWidgetId = i;
        this.providerName = componentName;
        this.spanX = -1;
        this.spanY = -1;
        this.user = UserHandleCompat.myUserHandle();
        this.restoreStatus = 0;
    }

    public final boolean hasRestoreFlag(int i) {
        return (this.restoreStatus & i) == i;
    }

    public boolean isCustomWidget() {
        return this.appWidgetId == -100;
    }

    public final boolean isWidgetIdValid() {
        boolean z = false;
        if ((this.restoreStatus & 1) == 0) {
            z = true;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.android.launcher3.ItemInfo
    public void onAddToDatabase(Context context, ContentValues contentValues) {
        super.onAddToDatabase(context, contentValues);
        contentValues.put("appWidgetId", Integer.valueOf(this.appWidgetId));
        contentValues.put("appWidgetProvider", this.providerName.flattenToString());
        contentValues.put("restored", Integer.valueOf(this.restoreStatus));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onBindAppWidget(Launcher launcher) {
        if (this.mHasNotifiedInitialWidgetSizeChanged) {
            return;
        }
        AppWidgetResizeFrame.updateWidgetSizeRanges(this.hostView, launcher, this.spanX, this.spanY);
        this.mHasNotifiedInitialWidgetSizeChanged = true;
    }

    @Override // com.android.launcher3.ItemInfo
    public String toString() {
        return "AppWidget(id=" + Integer.toString(this.appWidgetId) + " screenId=" + this.screenId + " cellX=" + this.cellX + " cellY=" + this.cellY + " spanX=" + this.spanX + " spanY=" + this.spanY + " providerName = " + this.providerName + ")";
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.android.launcher3.ItemInfo
    public void unbind() {
        super.unbind();
        this.hostView = null;
    }
}
