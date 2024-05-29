package com.android.browser;

import android.net.Uri;
import android.util.Patterns;
import android.webkit.URLUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/* loaded from: b.zip:com/android/browser/UrlUtils.class */
public class UrlUtils {
    static final Pattern ACCEPTED_URI_SCHEMA_FOR_URLHANDLER = Pattern.compile("(?i)((?:http|https|file):\\/\\/|(?:inline|data|about|javascript):)(.*)");
    static final Pattern ACCEPTED_URI_SCHEMA = Pattern.compile("(?i)((?:http|https|file):\\/\\/|(?:data|about|javascript):|(?:.*:.*@))(.*)");
    private static final Pattern STRIP_URL_PATTERN = Pattern.compile("^http://(.*?)/?$");

    private UrlUtils() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String filteredUrl(String str) {
        return (str == null || str.startsWith("content:") || str.startsWith("browser:")) ? "" : str;
    }

    /* JADX WARN: Code restructure failed: missing block: B:23:0x008a, code lost:
        if (r5.startsWith("https:") != false) goto L28;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static String fixUrl(String str) {
        String replaceFirst;
        int indexOf = str.indexOf(58);
        boolean z = true;
        int i = 0;
        while (i < indexOf) {
            char charAt = str.charAt(i);
            if (!Character.isLetter(charAt)) {
                break;
            }
            z &= Character.isLowerCase(charAt);
            String str2 = str;
            if (i == indexOf - 1) {
                str2 = z ? str : str.substring(0, indexOf).toLowerCase() + str.substring(indexOf);
            }
            i++;
            str = str2;
        }
        if (str.startsWith("http://") || str.startsWith("https://")) {
            return str;
        }
        if (!str.startsWith("http:")) {
            replaceFirst = str;
        }
        replaceFirst = (str.startsWith("http:/") || str.startsWith("https:/")) ? str.replaceFirst("/", "//") : str.replaceFirst(":", "://");
        return replaceFirst;
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
        String trim = str.trim();
        boolean z2 = trim.indexOf(32) != -1;
        Matcher matcher = ACCEPTED_URI_SCHEMA.matcher(trim);
        if (!matcher.matches()) {
            if (z2 || !Patterns.WEB_URL.matcher(trim).matches()) {
                if (z) {
                    return URLUtil.composeSearchUrl(trim, "http://www.google.com/m?q=%s", "%s");
                }
                return null;
            }
            return URLUtil.guessUrl(trim);
        }
        String group = matcher.group(1);
        String lowerCase = group.toLowerCase();
        if (!lowerCase.equals(group)) {
            trim = lowerCase + matcher.group(2);
        }
        String str2 = trim;
        if (z2) {
            str2 = trim;
            if (Patterns.WEB_URL.matcher(trim).matches()) {
                str2 = trim.replace(" ", "%20");
            }
        }
        return str2;
    }

    public static String stripUrl(String str) {
        if (str == null) {
            return null;
        }
        Matcher matcher = STRIP_URL_PATTERN.matcher(str);
        return matcher.matches() ? matcher.group(1) : str;
    }
}
