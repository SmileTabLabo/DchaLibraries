package com.android.launcher3.util;

import java.util.Set;
/* loaded from: a.zip:com/android/launcher3/util/StringFilter.class */
public abstract class StringFilter {
    private StringFilter() {
    }

    /* synthetic */ StringFilter(StringFilter stringFilter) {
        this();
    }

    public static StringFilter matchesAll() {
        return new StringFilter() { // from class: com.android.launcher3.util.StringFilter.1
            @Override // com.android.launcher3.util.StringFilter
            public boolean matches(String str) {
                return true;
            }
        };
    }

    public static StringFilter of(Set<String> set) {
        return new StringFilter(set) { // from class: com.android.launcher3.util.StringFilter.2
            final Set val$validEntries;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(null);
                this.val$validEntries = set;
            }

            @Override // com.android.launcher3.util.StringFilter
            public boolean matches(String str) {
                return this.val$validEntries.contains(str);
            }
        };
    }

    public abstract boolean matches(String str);
}
