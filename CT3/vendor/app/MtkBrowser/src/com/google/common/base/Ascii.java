package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
@GwtCompatible
/* loaded from: b.zip:com/google/common/base/Ascii.class */
public final class Ascii {
    private Ascii() {
    }

    public static boolean isUpperCase(char c) {
        boolean z = false;
        if (c >= 'A') {
            z = false;
            if (c <= 'Z') {
                z = true;
            }
        }
        return z;
    }

    public static String toLowerCase(String str) {
        int length = str.length();
        int i = 0;
        while (i < length) {
            if (isUpperCase(str.charAt(i))) {
                char[] charArray = str.toCharArray();
                while (i < length) {
                    char c = charArray[i];
                    if (isUpperCase(c)) {
                        charArray[i] = (char) (c ^ ' ');
                    }
                    i++;
                }
                return String.valueOf(charArray);
            }
            i++;
        }
        return str;
    }
}
