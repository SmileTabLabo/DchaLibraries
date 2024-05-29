package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
@GwtCompatible
/* loaded from: b.zip:com/google/common/collect/CollectPreconditions.class */
final class CollectPreconditions {
    CollectPreconditions() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void checkEntryNotNull(Object obj, Object obj2) {
        if (obj == null) {
            throw new NullPointerException("null key in entry: null=" + obj2);
        }
        if (obj2 == null) {
            throw new NullPointerException("null value in entry: " + obj + "=null");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int checkNonnegative(int i, String str) {
        if (i < 0) {
            throw new IllegalArgumentException(str + " cannot be negative but was: " + i);
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void checkRemove(boolean z) {
        Preconditions.checkState(z, "no calls to next() since the last call to remove()");
    }
}
