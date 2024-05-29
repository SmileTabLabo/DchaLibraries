package com.google.common.hash;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
@VisibleForTesting
/* loaded from: a.zip:com/google/common/hash/Hashing$ConcatenatedHashFunction.class */
final class Hashing$ConcatenatedHashFunction extends AbstractCompositeHashFunction {
    private final int bits;

    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Hashing$ConcatenatedHashFunction) {
            Hashing$ConcatenatedHashFunction hashing$ConcatenatedHashFunction = (Hashing$ConcatenatedHashFunction) obj;
            if (this.bits == hashing$ConcatenatedHashFunction.bits && this.functions.length == hashing$ConcatenatedHashFunction.functions.length) {
                for (int i = 0; i < this.functions.length; i++) {
                    if (!this.functions[i].equals(hashing$ConcatenatedHashFunction.functions[i])) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    public int hashCode() {
        int i = this.bits;
        for (HashFunction hashFunction : this.functions) {
            i ^= hashFunction.hashCode();
        }
        return i;
    }
}
