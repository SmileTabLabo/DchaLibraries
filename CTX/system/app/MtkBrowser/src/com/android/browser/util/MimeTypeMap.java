package com.android.browser.util;

import android.text.TextUtils;
import java.util.regex.Pattern;
import libcore.net.MimeUtils;
/* loaded from: classes.dex */
public class MimeTypeMap {
    private static final MimeTypeMap sMimeTypeMap = new MimeTypeMap();

    private MimeTypeMap() {
    }

    public static String getFileExtensionFromUrl(String str) {
        int lastIndexOf;
        if (!TextUtils.isEmpty(str)) {
            int lastIndexOf2 = str.lastIndexOf(35);
            if (lastIndexOf2 > 0) {
                str = str.substring(0, lastIndexOf2);
            }
            int lastIndexOf3 = str.lastIndexOf(63);
            if (lastIndexOf3 > 0) {
                str = str.substring(0, lastIndexOf3);
            }
            int lastIndexOf4 = str.lastIndexOf(47);
            if (lastIndexOf4 >= 0) {
                str = str.substring(lastIndexOf4 + 1);
            }
            if (!str.isEmpty() && Pattern.matches("[a-zA-Z_0-9\\.\\-\\(\\)\\%]+", str) && (lastIndexOf = str.lastIndexOf(46)) >= 0) {
                return str.substring(lastIndexOf + 1);
            }
            return "";
        }
        return "";
    }

    public String getMimeTypeFromExtension(String str) {
        return MimeUtils.guessMimeTypeFromExtension(str);
    }

    public String remapGenericMimeTypePublic(String str, String str2, String str3) {
        return remapGenericMimeType(str, str2, str3);
    }

    String remapGenericMimeType(String str, String str2, String str3) {
        String str4;
        if ("text/plain".equals(str) || "application/octet-stream".equals(str)) {
            if (str3 != null) {
                str4 = URLUtil.parseContentDisposition(str3);
            } else {
                str4 = null;
            }
            if (str4 != null) {
                str2 = str4;
            }
            String mimeTypeFromExtension = getMimeTypeFromExtension(getFileExtensionFromUrl(str2));
            if (mimeTypeFromExtension != null) {
                return mimeTypeFromExtension;
            }
            return str;
        } else if ("text/vnd.wap.wml".equals(str)) {
            return "text/plain";
        } else {
            if ("application/vnd.wap.xhtml+xml".equals(str)) {
                return "application/xhtml+xml";
            }
            return str;
        }
    }

    public static MimeTypeMap getSingleton() {
        return sMimeTypeMap;
    }
}
