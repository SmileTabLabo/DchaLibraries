package jp.co.benesse.dcha.databox;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import jp.co.benesse.dcha.util.FileUtils;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: classes.dex */
public class SboxProviderAdapter {
    public static final String SBOX_DB_COLUMN_NAME_KEY = "key";
    public static final String SBOX_DB_COLUMN_NAME_VALUE = "value";
    private static final String TAG = SboxProviderAdapter.class.getSimpleName();
    public static final Uri SBOX_URI = Uri.parse("content://jp.co.benesse.touch.sbox/jp.co.benesse.dcha.databox");
    public static final Uri SBOX_WIPE_URI = Uri.parse("content://jp.co.benesse.touch.sbox/cmd/wipe");

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r12v0, types: [android.content.ContentResolver] */
    /* JADX WARN: Type inference failed for: r12v1 */
    /* JADX WARN: Type inference failed for: r12v10 */
    /* JADX WARN: Type inference failed for: r12v11 */
    /* JADX WARN: Type inference failed for: r12v3, types: [java.io.Closeable] */
    /* JADX WARN: Type inference failed for: r12v4 */
    /* JADX WARN: Type inference failed for: r12v6, types: [java.io.Closeable] */
    /* JADX WARN: Type inference failed for: r12v9 */
    public String getValue(ContentResolver contentResolver, String str) {
        Cursor cursor;
        Logger.d(TAG, "getValue key:", str);
        String str2 = null;
        try {
            try {
                cursor = contentResolver.query(SBOX_URI, null, "key = ?", new String[]{str}, null);
                try {
                    boolean moveToFirst = cursor.moveToFirst();
                    contentResolver = cursor;
                    if (moveToFirst) {
                        str2 = cursor.getString(cursor.getColumnIndex("value"));
                        contentResolver = cursor;
                    }
                } catch (Exception e) {
                    e = e;
                    Logger.e(TAG, "getValue Exception", e);
                    contentResolver = cursor;
                    FileUtils.close(contentResolver);
                    Logger.d(TAG, "getValue value:", str2);
                    return str2;
                }
            } catch (Throwable th) {
                th = th;
                FileUtils.close(contentResolver);
                throw th;
            }
        } catch (Exception e2) {
            e = e2;
            cursor = null;
        } catch (Throwable th2) {
            th = th2;
            contentResolver = 0;
            FileUtils.close(contentResolver);
            throw th;
        }
        FileUtils.close(contentResolver);
        Logger.d(TAG, "getValue value:", str2);
        return str2;
    }

    public void setValue(ContentResolver contentResolver, String str, String str2) {
        Logger.d(TAG, "setValue key:", str, "value:", str2);
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("key", str);
            contentValues.put("value", str2);
            contentResolver.delete(SBOX_URI, "key = ?", new String[]{str});
            contentResolver.insert(SBOX_URI, contentValues);
        } catch (Exception e) {
            Logger.e(TAG, "setValue Exception", e);
        }
    }

    public void wipe(ContentResolver contentResolver) {
        Logger.d(TAG, "wipe");
        try {
            contentResolver.delete(SBOX_WIPE_URI, null, null);
        } catch (Exception e) {
            Logger.e(TAG, "wipe Exception", e);
        }
    }
}
