package com.android.settings.fuelgauge.batterytip;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.settings.fuelgauge.batterytip.AppInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/* loaded from: classes.dex */
public class BatteryDatabaseManager {
    private static BatteryDatabaseManager sSingleton;
    private AnomalyDatabaseHelper mDatabaseHelper;

    private BatteryDatabaseManager(Context context) {
        this.mDatabaseHelper = AnomalyDatabaseHelper.getInstance(context);
    }

    public static BatteryDatabaseManager getInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new BatteryDatabaseManager(context);
        }
        return sSingleton;
    }

    public synchronized boolean insertAnomaly(int i, String str, int i2, int i3, long j) {
        boolean z;
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uid", Integer.valueOf(i));
        contentValues.put("package_name", str);
        contentValues.put("anomaly_type", Integer.valueOf(i2));
        contentValues.put("anomaly_state", Integer.valueOf(i3));
        contentValues.put("time_stamp_ms", Long.valueOf(j));
        z = writableDatabase.insertWithOnConflict("anomaly", null, contentValues, 4) != -1;
        if (writableDatabase != null) {
            $closeResource(null, writableDatabase);
        }
        return z;
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th == null) {
            autoCloseable.close();
            return;
        }
        try {
            autoCloseable.close();
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
    }

    public synchronized List<AppInfo> queryAllAnomalies(long j, int i) {
        ArrayList arrayList;
        arrayList = new ArrayList();
        SQLiteDatabase readableDatabase = this.mDatabaseHelper.getReadableDatabase();
        ArrayMap arrayMap = new ArrayMap();
        Cursor query = readableDatabase.query("anomaly", new String[]{"package_name", "anomaly_type", "uid"}, "time_stamp_ms > ? AND anomaly_state = ? ", new String[]{String.valueOf(j), String.valueOf(i)}, null, null, "time_stamp_ms DESC");
        while (query.moveToNext()) {
            try {
                int i2 = query.getInt(query.getColumnIndex("uid"));
                if (!arrayMap.containsKey(Integer.valueOf(i2))) {
                    arrayMap.put(Integer.valueOf(i2), new AppInfo.Builder().setUid(i2).setPackageName(query.getString(query.getColumnIndex("package_name"))));
                }
                ((AppInfo.Builder) arrayMap.get(Integer.valueOf(i2))).addAnomalyType(query.getInt(query.getColumnIndex("anomaly_type")));
            } finally {
            }
        }
        if (query != null) {
            $closeResource(null, query);
        }
        for (Integer num : arrayMap.keySet()) {
            arrayList.add(((AppInfo.Builder) arrayMap.get(num)).build());
        }
        if (readableDatabase != null) {
            $closeResource(null, readableDatabase);
        }
        return arrayList;
    }

    public synchronized void deleteAllAnomaliesBeforeTimeStamp(long j) {
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.delete("anomaly", "time_stamp_ms < ?", new String[]{String.valueOf(j)});
        if (writableDatabase != null) {
            $closeResource(null, writableDatabase);
        }
    }

    public synchronized void updateAnomalies(List<AppInfo> list, int i) {
        if (!list.isEmpty()) {
            int size = list.size();
            String[] strArr = new String[size];
            for (int i2 = 0; i2 < size; i2++) {
                strArr[i2] = list.get(i2).packageName;
            }
            SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("anomaly_state", Integer.valueOf(i));
            writableDatabase.update("anomaly", contentValues, "package_name IN (" + TextUtils.join(",", Collections.nCopies(list.size(), "?")) + ")", strArr);
            if (writableDatabase != null) {
                $closeResource(null, writableDatabase);
            }
        }
    }
}
