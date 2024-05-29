package com.android.launcher3.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
/* loaded from: a.zip:com/android/launcher3/util/SQLiteCacheHelper.class */
public abstract class SQLiteCacheHelper {
    private boolean mIgnoreWrites = false;
    private final MySQLiteOpenHelper mOpenHelper;
    private final String mTableName;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/util/SQLiteCacheHelper$MySQLiteOpenHelper.class */
    public class MySQLiteOpenHelper extends SQLiteOpenHelper {
        final SQLiteCacheHelper this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public MySQLiteOpenHelper(SQLiteCacheHelper sQLiteCacheHelper, Context context, String str, int i) {
            super(context, str, (SQLiteDatabase.CursorFactory) null, i);
            this.this$0 = sQLiteCacheHelper;
        }

        private void clearDB(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + this.this$0.mTableName);
            onCreate(sQLiteDatabase);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            this.this$0.onCreateTable(sQLiteDatabase);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            if (i != i2) {
                clearDB(sQLiteDatabase);
            }
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            if (i != i2) {
                clearDB(sQLiteDatabase);
            }
        }
    }

    public SQLiteCacheHelper(Context context, String str, int i, String str2) {
        this.mTableName = str2;
        this.mOpenHelper = new MySQLiteOpenHelper(this, context, str, i);
    }

    private void onDiskFull(SQLiteFullException sQLiteFullException) {
        Log.e("SQLiteCacheHelper", "Disk full, all write operations will be ignored", sQLiteFullException);
        this.mIgnoreWrites = true;
    }

    public void delete(String str, String[] strArr) {
        if (this.mIgnoreWrites) {
            return;
        }
        try {
            this.mOpenHelper.getWritableDatabase().delete(this.mTableName, str, strArr);
        } catch (SQLiteFullException e) {
            onDiskFull(e);
        } catch (SQLiteException e2) {
            Log.d("SQLiteCacheHelper", "Ignoring sqlite exception", e2);
        }
    }

    public void insertOrReplace(ContentValues contentValues) {
        if (this.mIgnoreWrites) {
            return;
        }
        try {
            this.mOpenHelper.getWritableDatabase().insertWithOnConflict(this.mTableName, null, contentValues, 5);
        } catch (SQLiteFullException e) {
            onDiskFull(e);
        } catch (SQLiteException e2) {
            Log.d("SQLiteCacheHelper", "Ignoring sqlite exception", e2);
        }
    }

    protected abstract void onCreateTable(SQLiteDatabase sQLiteDatabase);

    public Cursor query(String[] strArr, String str, String[] strArr2) {
        return this.mOpenHelper.getReadableDatabase().query(this.mTableName, strArr, str, strArr2, null, null, null);
    }

    public void update(ContentValues contentValues, String str, String[] strArr) {
        if (this.mIgnoreWrites) {
            return;
        }
        try {
            this.mOpenHelper.getWritableDatabase().update(this.mTableName, contentValues, str, strArr);
        } catch (SQLiteFullException e) {
            onDiskFull(e);
        } catch (SQLiteException e2) {
            Log.d("SQLiteCacheHelper", "Ignoring sqlite exception", e2);
        }
    }
}
