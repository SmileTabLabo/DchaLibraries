package com.android.launcher3.util;

import java.text.Collator;
import java.util.Comparator;
/* loaded from: classes.dex */
public class LabelComparator implements Comparator<String> {
    private final Collator mCollator = Collator.getInstance();

    @Override // java.util.Comparator
    public int compare(String str, String str2) {
        boolean z = false;
        boolean z2 = str.length() > 0 && Character.isLetterOrDigit(str.codePointAt(0));
        if (str2.length() > 0 && Character.isLetterOrDigit(str2.codePointAt(0))) {
            z = true;
        }
        if (z2 && !z) {
            return -1;
        }
        if (z2 || !z) {
            return this.mCollator.compare(str, str2);
        }
        return 1;
    }
}
