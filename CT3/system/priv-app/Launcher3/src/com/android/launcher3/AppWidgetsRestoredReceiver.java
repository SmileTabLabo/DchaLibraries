package com.android.launcher3;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/launcher3/AppWidgetsRestoredReceiver.class */
public class AppWidgetsRestoredReceiver extends BroadcastReceiver {
    /* JADX WARN: Type inference failed for: r0v12, types: [com.android.launcher3.AppWidgetsRestoredReceiver$1] */
    static void restoreAppWidgetIds(Context context, int[] iArr, int[] iArr2) {
        ContentResolver contentResolver = context.getContentResolver();
        ArrayList arrayList = new ArrayList();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        for (int i = 0; i < iArr.length; i++) {
            Log.i("AppWidgetsRestoredReceiver", "Widget state restore id " + iArr[i] + " => " + iArr2[i]);
            int i2 = LauncherModel.isValidProvider(appWidgetManager.getAppWidgetInfo(iArr2[i])) ? 4 : 2;
            ContentValues contentValues = new ContentValues();
            contentValues.put("appWidgetId", Integer.valueOf(iArr2[i]));
            contentValues.put("restored", Integer.valueOf(i2));
            String[] strArr = {Integer.toString(iArr[i])};
            if (contentResolver.update(LauncherSettings$Favorites.CONTENT_URI, contentValues, "appWidgetId=? and (restored & 1) = 1", strArr) == 0) {
                Cursor query = contentResolver.query(LauncherSettings$Favorites.CONTENT_URI, new String[]{"appWidgetId"}, "appWidgetId=?", strArr, null);
                try {
                    if (!query.moveToFirst()) {
                        arrayList.add(Integer.valueOf(iArr2[i]));
                    }
                } finally {
                    query.close();
                }
            }
        }
        if (!arrayList.isEmpty()) {
            new AsyncTask<Void, Void, Void>(arrayList, new AppWidgetHost(context, 1024)) { // from class: com.android.launcher3.AppWidgetsRestoredReceiver.1
                final AppWidgetHost val$appWidgetHost;
                final List val$idsToRemove;

                {
                    this.val$idsToRemove = arrayList;
                    this.val$appWidgetHost = r5;
                }

                @Override // android.os.AsyncTask
                public Void doInBackground(Void... voidArr) {
                    for (Integer num : this.val$idsToRemove) {
                        this.val$appWidgetHost.deleteAppWidgetId(num.intValue());
                        Log.e("AppWidgetsRestoredReceiver", "Widget no longer present, appWidgetId=" + num);
                    }
                    return null;
                }
            }.executeOnExecutor(Utilities.THREAD_POOL_EXECUTOR, new Void[0]);
        }
        LauncherAppState instanceNoCreate = LauncherAppState.getInstanceNoCreate();
        if (instanceNoCreate != null) {
            instanceNoCreate.reloadWorkspace();
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if ("android.appwidget.action.APPWIDGET_HOST_RESTORED".equals(intent.getAction())) {
            int[] intArrayExtra = intent.getIntArrayExtra("appWidgetOldIds");
            int[] intArrayExtra2 = intent.getIntArrayExtra("appWidgetIds");
            if (intArrayExtra.length == intArrayExtra2.length) {
                restoreAppWidgetIds(context, intArrayExtra, intArrayExtra2);
            } else {
                Log.e("AppWidgetsRestoredReceiver", "Invalid host restored received");
            }
        }
    }
}
