package com.google.common.collect;
/* loaded from: classes.dex */
final class Hashing {
    private static int MAX_TABLE_SIZE = 1073741824;

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int smear(int i) {
        return 461845907 * Integer.rotateLeft(i * (-862048943), 15);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int smearedHash(Object obj) {
        return smear(obj == null ? 0 : obj.hashCode());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int closedTableSize(int i, double d) {
        int max = Math.max(i, 2);
        int highestOneBit = Integer.highestOneBit(max);
        if (max > ((int) (d * highestOneBit))) {
            int i2 = highestOneBit << 1;
            return i2 > 0 ? i2 : MAX_TABLE_SIZE;
        }
        return highestOneBit;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean needsResizing(int i, int i2, double d) {
        return ((double) i) > d * ((double) i2) && i2 < MAX_TABLE_SIZE;
    }
}
