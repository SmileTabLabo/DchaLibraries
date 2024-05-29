package com.google.common.primitives;

import com.google.common.annotations.GwtCompatible;
import java.util.Arrays;
@GwtCompatible(emulated = true)
/* loaded from: b.zip:com/google/common/primitives/Ints.class */
public final class Ints {
    private static final byte[] asciiDigits = new byte[128];

    static {
        Arrays.fill(asciiDigits, (byte) -1);
        for (int i = 0; i <= 9; i++) {
            asciiDigits[i + 48] = (byte) i;
        }
        for (int i2 = 0; i2 <= 26; i2++) {
            asciiDigits[i2 + 65] = (byte) (i2 + 10);
            asciiDigits[i2 + 97] = (byte) (i2 + 10);
        }
    }

    private Ints() {
    }

    public static int saturatedCast(long j) {
        if (j > 2147483647L) {
            return Integer.MAX_VALUE;
        }
        if (j < -2147483648L) {
            return Integer.MIN_VALUE;
        }
        return (int) j;
    }
}
