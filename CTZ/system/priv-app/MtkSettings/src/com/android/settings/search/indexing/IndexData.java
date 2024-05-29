package com.android.settings.search.indexing;

import java.text.Normalizer;
import java.util.regex.Pattern;
/* loaded from: classes.dex */
public class IndexData {
    private static final Pattern REMOVE_DIACRITICALS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    public final String updatedSummaryOn;
    public final String updatedTitle;

    public String toString() {
        return this.updatedTitle + ": " + this.updatedSummaryOn;
    }

    public static String normalizeJapaneseString(String str) {
        String normalize = Normalizer.normalize(str != null ? str.replaceAll("-", "") : "", Normalizer.Form.NFKD);
        StringBuffer stringBuffer = new StringBuffer();
        int length = normalize.length();
        for (int i = 0; i < length; i++) {
            char charAt = normalize.charAt(i);
            if (charAt >= 12353 && charAt <= 12438) {
                stringBuffer.append((char) ((charAt - 12353) + 12449));
            } else {
                stringBuffer.append(charAt);
            }
        }
        return REMOVE_DIACRITICALS_PATTERN.matcher(stringBuffer.toString()).replaceAll("").toLowerCase();
    }
}
