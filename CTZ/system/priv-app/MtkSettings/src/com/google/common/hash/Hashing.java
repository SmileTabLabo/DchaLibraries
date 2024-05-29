package com.google.common.hash;
/* loaded from: classes.dex */
public final class Hashing {
    private static final int GOOD_FAST_HASH_SEED = (int) System.currentTimeMillis();

    /* loaded from: classes.dex */
    static final class ConcatenatedHashFunction extends AbstractCompositeHashFunction {
        private final int bits;

        public boolean equals(Object obj) {
            if (obj instanceof ConcatenatedHashFunction) {
                ConcatenatedHashFunction concatenatedHashFunction = (ConcatenatedHashFunction) obj;
                if (this.bits == concatenatedHashFunction.bits && this.functions.length == concatenatedHashFunction.functions.length) {
                    for (int i = 0; i < this.functions.length; i++) {
                        if (!this.functions[i].equals(concatenatedHashFunction.functions[i])) {
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
}
