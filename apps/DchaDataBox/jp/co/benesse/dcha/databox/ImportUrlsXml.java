package jp.co.benesse.dcha.databox;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import jp.co.benesse.dcha.databox.db.ContractKvs;
import jp.co.benesse.dcha.util.FileUtils;
import org.xmlpull.v1.XmlPullParser;
/* loaded from: classes.dex */
public class ImportUrlsXml {
    private static final Uri URI_TEST_ENVIRONMENT_INFO = Uri.withAppendedPath(ContractKvs.KVS.contentUri, "test.environment.info");
    private static final String XML_TAG_CONNECT_INFO = "connect_info";
    private static final String XML_TAG_ENVIRONMENT = "environment";
    private static final String XML_TAG_ID = "id";
    private static final String XML_TAG_TEXT = "text";
    private static final String XML_TAG_URL = "url";
    private static final String XML_TAG_VERSION = "version";

    public void delete(Context context) {
        try {
            context.getContentResolver().delete(URI_TEST_ENVIRONMENT_INFO, null, null);
        } catch (Exception unused) {
        }
    }

    public boolean execImport(Context context, File file) {
        Map<String, String> parseXml;
        if (context == null || file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        if (file.canRead()) {
            try {
                ContentResolver contentResolver = context.getContentResolver();
                parseXml = parseXml(new FileInputStream(file));
                for (Map.Entry<String, String> entry : parseXml.entrySet()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("key", entry.getKey());
                    contentValues.put("value", entry.getValue());
                    contentResolver.insert(URI_TEST_ENVIRONMENT_INFO, contentValues);
                }
            } catch (Exception unused) {
                return false;
            }
        }
        return !parseXml.isEmpty();
    }

    protected Map<String, String> parseXml(InputStream inputStream) {
        HashMap hashMap = new HashMap();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
        try {
            try {
                XmlPullParser newPullParser = Xml.newPullParser();
                newPullParser.setInput(inputStreamReader);
                HashMap hashMap2 = null;
                String str = BuildConfig.FLAVOR;
                for (int eventType = newPullParser.getEventType(); eventType != 1; eventType = newPullParser.next()) {
                    if (eventType == 2) {
                        String name = newPullParser.getName();
                        if (XML_TAG_CONNECT_INFO.equals(name)) {
                            hashMap2 = new HashMap();
                        }
                        str = name;
                    } else if (eventType == 3) {
                        String name2 = newPullParser.getName();
                        if (hashMap2 != null && XML_TAG_CONNECT_INFO.equals(name2)) {
                            if (hashMap2.containsKey(XML_TAG_ID) && hashMap2.containsKey(XML_TAG_URL)) {
                                URL url = new URL((String) hashMap2.get(XML_TAG_URL));
                                if (TextUtils.isEmpty(url.getHost()) || TextUtils.isEmpty(url.getProtocol())) {
                                    throw new MalformedURLException();
                                }
                                hashMap.put(hashMap2.get(XML_TAG_ID), url.toString());
                            } else if (hashMap2.containsKey(XML_TAG_ID) && hashMap2.containsKey(XML_TAG_TEXT)) {
                                hashMap.put(hashMap2.get(XML_TAG_ID), hashMap2.get(XML_TAG_TEXT));
                            } else {
                                throw new IllegalArgumentException();
                            }
                            hashMap2.clear();
                            hashMap2 = null;
                        } else if ((XML_TAG_ENVIRONMENT.equals(name2) || XML_TAG_VERSION.equals(name2)) && !hashMap.containsKey(name2)) {
                            hashMap.put(name2, BuildConfig.FLAVOR);
                        }
                        str = BuildConfig.FLAVOR;
                    } else if (eventType == 4) {
                        if (!XML_TAG_ENVIRONMENT.equals(str) && !XML_TAG_VERSION.equals(str)) {
                            if (hashMap2 != null && (XML_TAG_ID.equals(str) || XML_TAG_URL.equals(str) || XML_TAG_TEXT.equals(str))) {
                                hashMap2.put(str, newPullParser.getText());
                            }
                        }
                        hashMap.put(str, newPullParser.getText());
                    }
                }
                if (!hashMap.containsKey(XML_TAG_ENVIRONMENT) || !hashMap.containsKey(XML_TAG_VERSION)) {
                    hashMap.clear();
                }
            } catch (Exception unused) {
                hashMap.clear();
            }
            return hashMap;
        } finally {
            FileUtils.close(inputStreamReader);
        }
    }
}
