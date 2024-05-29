package com.android.browser;

import android.net.Uri;
import android.util.Patterns;
import android.webkit.URLUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/* loaded from: classes.dex */
public class UrlUtils {
    static final Pattern ACCEPTED_URI_SCHEMA_FOR_URLHANDLER = Pattern.compile("(?i)((?:http|https|file):\\/\\/|(?:inline|data|about|javascript):)(.*)");
    static final Pattern ACCEPTED_URI_SCHEMA = Pattern.compile("(?i)((?:http|https|file):\\/\\/|(?:data|about|javascript):|(?:.*:.*@))(.*)");
    private static final Pattern STRIP_URL_PATTERN = Pattern.compile("^http://(.*?)/?$");

    public static String stripUrl(String str) {
        if (str == null) {
            return null;
        }
        Matcher matcher = STRIP_URL_PATTERN.matcher(str);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return str;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static String smartUrlFilter(Uri uri) {
        if (uri != null) {
            return smartUrlFilter(uri.toString());
        }
        return null;
    }

    public static String smartUrlFilter(String str) {
        return smartUrlFilter(str, true);
    }

    public static String smartUrlFilter(String str, boolean z) {
        boolean z2;
        String trim = str.trim();
        if (trim.indexOf(32) == -1) {
            z2 = false;
        } else {
            z2 = true;
        }
        Matcher matcher = ACCEPTED_URI_SCHEMA.matcher(trim);
        if (matcher.matches()) {
            String group = matcher.group(1);
            String lowerCase = group.toLowerCase();
            if (!lowerCase.equals(group)) {
                trim = lowerCase + matcher.group(2);
            }
            if (z2 && Patterns.WEB_URL.matcher(trim).matches()) {
                return trim.replace(" ", "%20");
            }
            return trim;
        } else if (!z2 && Patterns.WEB_URL.matcher(trim).matches()) {
            return URLUtil.guessUrl(trim);
        } else {
            if (z) {
                return URLUtil.composeSearchUrl(trim, "http://www.google.com/m?q=%s", "%s");
            }
            return null;
        }
    }

    public static String fixUrl(String str) {
        int indexOf = str.indexOf(58);
        boolean z = true;
        String str2 = str;
        for (int i = 0; i < indexOf; i++) {
            char charAt = str2.charAt(i);
            if (!Character.isLetter(charAt)) {
                break;
            }
            z &= Character.isLowerCase(charAt);
            if (i == indexOf - 1 && !z) {
                str2 = str2.substring(0, indexOf).toLowerCase() + str2.substring(indexOf);
            }
        }
        if (str2.startsWith("http://") || str2.startsWith("https://")) {
            return str2;
        }
        if (str2.startsWith("http:") || str2.startsWith("https:")) {
            if (str2.startsWith("http:/") || str2.startsWith("https:/")) {
                return str2.replaceFirst("/", "//");
            }
            return str2.replaceFirst(":", "://");
        }
        return str2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String filteredUrl(String str) {
        if (str == null || str.startsWith("content:") || str.startsWith("browser:")) {
            return "";
        }
        return str;
    }
}
