package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import javax.annotation.Nullable;
@GwtCompatible
/* loaded from: a.zip:com/google/common/collect/Hashing.class */
final class Hashing {
    private static int MAX_TABLE_SIZE = 1073741824;

    private Hashing() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int closedTableSize(int i, double d) {
        int max = Math.max(i, 2);
        int highestOneBit = Integer.highestOneBit(max);
        if (max > ((int) (highestOneBit * d))) {
            int i2 = highestOneBit << 1;
            if (i2 <= 0) {
                i2 = MAX_TABLE_SIZE;
            }
            return i2;
        }
        return highestOneBit;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean needsResizing(int i, int i2, double d) {
        boolean z = false;
        if (i > i2 * d) {
            z = false;
            if (i2 < MAX_TABLE_SIZE) {
                z = true;
            }
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int smear(int i) {
        return Integer.rotateLeft((-862048943) * i, 15) * 461845907;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int smearedHash(@Nullable Object obj) {
        return smear(obj == null ? 0 : obj.hashCode());
    }
}
