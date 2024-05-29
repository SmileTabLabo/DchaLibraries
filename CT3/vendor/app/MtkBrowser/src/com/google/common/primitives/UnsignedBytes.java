package com.google.common.primitives;

import com.google.common.annotations.VisibleForTesting;
import java.util.Comparator;
/* loaded from: b.zip:com/google/common/primitives/UnsignedBytes.class */
public final class UnsignedBytes {

    @VisibleForTesting
    /* loaded from: b.zip:com/google/common/primitives/UnsignedBytes$LexicographicalComparatorHolder.class */
    static class LexicographicalComparatorHolder {
        static final Comparator<byte[]> BEST_COMPARATOR = UnsignedBytes.lexicographicalComparatorJavaImpl();

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: b.zip:com/google/common/primitives/UnsignedBytes$LexicographicalComparatorHolder$PureJavaComparator.class */
        public enum PureJavaComparator implements Comparator<byte[]> {
            INSTANCE;

            /* renamed from: values  reason: to resolve conflict with enum method */
            public static PureJavaComparator[] valuesCustom() {
                return values();
            }

            @Override // java.util.Comparator
            public int compare(byte[] bArr, byte[] bArr2) {
                int min = Math.min(bArr.length, bArr2.length);
                for (int i = 0; i < min; i++) {
                    int compare = UnsignedBytes.compare(bArr[i], bArr2[i]);
                    if (compare != 0) {
                        return compare;
                    }
                }
                return bArr.length - bArr2.length;
            }
        }

        LexicographicalComparatorHolder() {
        }
    }

    private UnsignedBytes() {
    }

    public static int compare(byte b, byte b2) {
        return toInt(b) - toInt(b2);
    }

    @VisibleForTesting
    static Comparator<byte[]> lexicographicalComparatorJavaImpl() {
        return LexicographicalComparatorHolder.PureJavaComparator.INSTANCE;
    }

    public static int toInt(byte b) {
        return b & 255;
    }
}
