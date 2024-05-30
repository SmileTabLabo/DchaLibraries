package jp.co.benesse.dcha.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
/* loaded from: classes.dex */
public class UrlUtil {
    private static final String AKAMAI_URL1 = "https://townak.benesse.ne.jp/test2/A/sp_84/";
    private static final String AKAMAI_URL10 = "https://townak.benesse.ne.jp/rel/A/sp_84/";
    private static final String AKAMAI_URL11 = "https://townak.benesse.ne.jp/rel/B/sp_84/";
    private static final String AKAMAI_URL12 = "https://townak.benesse.ne.jp/rel/B/sp_84/";
    private static final String AKAMAI_URL2 = "https://townak.benesse.ne.jp/test2/B/sp_84/";
    private static final String AKAMAI_URL3 = "https://townak.benesse.ne.jp/test2/A/sp_84/";
    private static final String AKAMAI_URL4 = "https://townak.benesse.ne.jp/test2/B/sp_84/";
    private static final String AKAMAI_URL5 = "https://townak.benesse.ne.jp/test/A/sp_84/";
    private static final String AKAMAI_URL6 = "https://townak.benesse.ne.jp/test/B/sp_84/";
    private static final String AKAMAI_URL7 = "https://townak.benesse.ne.jp/test/A/sp_84/";
    private static final String AKAMAI_URL8 = "https://townak.benesse.ne.jp/test/B/sp_84/";
    private static final String AKAMAI_URL9 = "https://townak.benesse.ne.jp/rel/B/sp_84/";
    private static final String COLUMN_KVS_SELECTION = "key=?";
    private static final String COLUMN_KVS_VALUE = "value";
    private static final String CONNECT_ID_AKAMAI = "townak";
    private static final String OS_TYPE_001 = "001";
    private static final String OS_TYPE_002 = "092";
    private static final String OS_TYPE_003 = "003";
    private static final String OS_TYPE_004 = "094";
    private static final String OS_TYPE_005 = "005";
    private static final String OS_TYPE_006 = "096";
    private static final String OS_TYPE_007 = "007";
    private static final String OS_TYPE_008 = "098";
    private static final String OS_TYPE_009 = "099";
    private static final String OS_TYPE_010 = "000";
    private static final String OS_TYPE_011 = "011";
    private static final String OS_TYPE_012 = "012";
    private static final String TAG = UrlUtil.class.getSimpleName();
    private static final Uri URI_TEST_ENVIRONMENT_INFO = Uri.parse("content://jp.co.benesse.dcha.databox.db.KvsProvider/kvs/test.environment.info");
    private static final String VER_SPLIT = "\\.";
    private static final int VER_SPLIT_NUM = 3;

    public String getUrlAkamai(Context context) {
        String kvsValue = getKvsValue(context, URI_TEST_ENVIRONMENT_INFO, CONNECT_ID_AKAMAI, null);
        if (!TextUtils.isEmpty(kvsValue)) {
            if (!kvsValue.endsWith("/")) {
                kvsValue = kvsValue + "/";
            }
            Logger.d(TAG, "result(kvs):", kvsValue);
            return kvsValue;
        }
        String urlType = getUrlType(getBuildID());
        String str = "https://townak.benesse.ne.jp/rel/B/sp_84/";
        if (!urlType.equals(OS_TYPE_001)) {
            if (!urlType.equals(OS_TYPE_002)) {
                if (!urlType.equals(OS_TYPE_003)) {
                    if (!urlType.equals(OS_TYPE_004)) {
                        if (!urlType.equals(OS_TYPE_005)) {
                            if (!urlType.equals(OS_TYPE_006)) {
                                if (!urlType.equals(OS_TYPE_007)) {
                                    if (!urlType.equals(OS_TYPE_008)) {
                                        if (!urlType.equals(OS_TYPE_009) && (urlType.equals(OS_TYPE_010) || (!urlType.equals(OS_TYPE_011) && !urlType.equals(OS_TYPE_012)))) {
                                            str = AKAMAI_URL10;
                                        }
                                        Logger.d(TAG, "result:", str);
                                        return str;
                                    }
                                }
                            }
                            str = "https://townak.benesse.ne.jp/test/B/sp_84/";
                            Logger.d(TAG, "result:", str);
                            return str;
                        }
                        str = "https://townak.benesse.ne.jp/test/A/sp_84/";
                        Logger.d(TAG, "result:", str);
                        return str;
                    }
                }
            }
            str = "https://townak.benesse.ne.jp/test2/B/sp_84/";
            Logger.d(TAG, "result:", str);
            return str;
        }
        str = "https://townak.benesse.ne.jp/test2/A/sp_84/";
        Logger.d(TAG, "result:", str);
        return str;
    }

    protected String getBuildID() {
        return Build.ID;
    }

    protected String getUrlType(String str) {
        if (!TextUtils.isEmpty(str)) {
            String[] split = str.split(VER_SPLIT);
            if (split.length == 3) {
                return split[2].replace('T', '0');
            }
        }
        return OS_TYPE_010;
    }

    /* JADX WARN: Code restructure failed: missing block: B:10:0x002c, code lost:
        if (r10 != null) goto L12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:11:0x002e, code lost:
        r10.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:17:0x003a, code lost:
        if (r10 == null) goto L20;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    protected String getKvsValue(Context context, Uri uri, String str, String str2) {
        if (context != null) {
            String[] strArr = {str};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, new String[]{COLUMN_KVS_VALUE}, COLUMN_KVS_SELECTION, strArr, null);
                if (cursor != null && cursor.moveToFirst()) {
                    str2 = cursor.getString(cursor.getColumnIndex(COLUMN_KVS_VALUE));
                }
            } catch (Exception unused) {
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
        return str2;
    }
}
