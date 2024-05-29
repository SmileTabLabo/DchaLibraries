package com.android.browser.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
/* loaded from: b.zip:com/android/browser/provider/SQLiteContentProvider.class */
public abstract class SQLiteContentProvider extends ContentProvider {
    private final ThreadLocal<Boolean> mApplyingBatch = new ThreadLocal<>();
    private Set<Uri> mChangedUris;
    protected SQLiteDatabase mDb;
    private SQLiteOpenHelper mOpenHelper;

    private boolean applyingBatch() {
        return this.mApplyingBatch.get() != null ? this.mApplyingBatch.get().booleanValue() : false;
    }

    @Override // android.content.ContentProvider
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) throws OperationApplicationException {
        int i = 0;
        int i2 = 0;
        boolean z = false;
        this.mDb = this.mOpenHelper.getWritableDatabase();
        this.mDb.beginTransaction();
        try {
            this.mApplyingBatch.set(true);
            int size = arrayList.size();
            ContentProviderResult[] contentProviderResultArr = new ContentProviderResult[size];
            int i3 = 0;
            while (i3 < size) {
                int i4 = i2 + 1;
                if (i4 >= 500) {
                    boolean z2 = z;
                    boolean z3 = z;
                    throw new OperationApplicationException("Too many content provider operations between yield points. The maximum number of operations per yield point is 500", i);
                }
                ContentProviderOperation contentProviderOperation = arrayList.get(i3);
                boolean z4 = z;
                if (!z) {
                    z4 = z;
                    if (isCallerSyncAdapter(contentProviderOperation.getUri())) {
                        z4 = true;
                    }
                }
                i2 = i4;
                int i5 = i;
                if (i3 > 0) {
                    i2 = i4;
                    i5 = i;
                    if (contentProviderOperation.isYieldAllowed()) {
                        i2 = 0;
                        i5 = i;
                        if (this.mDb.yieldIfContendedSafely(4000L)) {
                            i5 = i + 1;
                            i2 = 0;
                        }
                    }
                }
                contentProviderResultArr[i3] = contentProviderOperation.apply(this, contentProviderResultArr, i3);
                i3++;
                z = z4;
                i = i5;
            }
            this.mDb.setTransactionSuccessful();
            this.mApplyingBatch.set(false);
            this.mDb.endTransaction();
            onEndTransaction(z);
            return contentProviderResultArr;
        } catch (Throwable th) {
            this.mApplyingBatch.set(false);
            this.mDb.endTransaction();
            onEndTransaction(false);
            throw th;
        }
    }

    @Override // android.content.ContentProvider
    public int bulkInsert(Uri uri, ContentValues[] contentValuesArr) {
        int length = contentValuesArr.length;
        boolean isCallerSyncAdapter = isCallerSyncAdapter(uri);
        this.mDb = this.mOpenHelper.getWritableDatabase();
        this.mDb.beginTransaction();
        for (ContentValues contentValues : contentValuesArr) {
            try {
                insertInTransaction(uri, contentValues, isCallerSyncAdapter);
                this.mDb.yieldIfContendedSafely();
            } catch (Throwable th) {
                this.mDb.endTransaction();
                throw th;
            }
        }
        this.mDb.setTransactionSuccessful();
        this.mDb.endTransaction();
        onEndTransaction(isCallerSyncAdapter);
        return length;
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        int deleteInTransaction;
        boolean isCallerSyncAdapter = isCallerSyncAdapter(uri);
        if (applyingBatch()) {
            deleteInTransaction = deleteInTransaction(uri, str, strArr, isCallerSyncAdapter);
        } else {
            this.mDb = this.mOpenHelper.getWritableDatabase();
            this.mDb.beginTransaction();
            try {
                deleteInTransaction = deleteInTransaction(uri, str, strArr, isCallerSyncAdapter);
                this.mDb.setTransactionSuccessful();
                this.mDb.endTransaction();
                onEndTransaction(isCallerSyncAdapter);
            } catch (Throwable th) {
                this.mDb.endTransaction();
                throw th;
            }
        }
        return deleteInTransaction;
    }

    public abstract int deleteInTransaction(Uri uri, String str, String[] strArr, boolean z);

    public abstract SQLiteOpenHelper getDatabaseHelper(Context context);

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        Uri insertInTransaction;
        boolean isCallerSyncAdapter = isCallerSyncAdapter(uri);
        if (applyingBatch()) {
            insertInTransaction = insertInTransaction(uri, contentValues, isCallerSyncAdapter);
        } else {
            this.mDb = this.mOpenHelper.getWritableDatabase();
            this.mDb.beginTransaction();
            try {
                insertInTransaction = insertInTransaction(uri, contentValues, isCallerSyncAdapter);
                this.mDb.setTransactionSuccessful();
                this.mDb.endTransaction();
                onEndTransaction(isCallerSyncAdapter);
            } catch (Throwable th) {
                this.mDb.endTransaction();
                throw th;
            }
        }
        return insertInTransaction;
    }

    public abstract Uri insertInTransaction(Uri uri, ContentValues contentValues, boolean z);

    public boolean isCallerSyncAdapter(Uri uri) {
        return false;
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        this.mOpenHelper = getDatabaseHelper(getContext());
        this.mChangedUris = new HashSet();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onEndTransaction(boolean z) {
        HashSet<Uri> hashSet;
        synchronized (this.mChangedUris) {
            hashSet = new HashSet(this.mChangedUris);
            this.mChangedUris.clear();
        }
        ContentResolver contentResolver = getContext().getContentResolver();
        for (Uri uri : hashSet) {
            contentResolver.notifyChange(uri, (ContentObserver) null, !z ? syncToNetwork(uri) : false);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void postNotifyUri(Uri uri) {
        synchronized (this.mChangedUris) {
            this.mChangedUris.add(uri);
        }
    }

    protected boolean syncToNetwork(Uri uri) {
        return false;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        int updateInTransaction;
        boolean isCallerSyncAdapter = isCallerSyncAdapter(uri);
        if (applyingBatch()) {
            updateInTransaction = updateInTransaction(uri, contentValues, str, strArr, isCallerSyncAdapter);
        } else {
            this.mDb = this.mOpenHelper.getWritableDatabase();
            this.mDb.beginTransaction();
            try {
                updateInTransaction = updateInTransaction(uri, contentValues, str, strArr, isCallerSyncAdapter);
                this.mDb.setTransactionSuccessful();
                this.mDb.endTransaction();
                onEndTransaction(isCallerSyncAdapter);
            } catch (Throwable th) {
                this.mDb.endTransaction();
                throw th;
            }
        }
        return updateInTransaction;
    }

    public abstract int updateInTransaction(Uri uri, ContentValues contentValues, String str, String[] strArr, boolean z);
}
