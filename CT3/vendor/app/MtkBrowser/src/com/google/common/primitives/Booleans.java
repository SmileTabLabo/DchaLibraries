package com.google.common.primitives;

import com.google.common.annotations.GwtCompatible;
@GwtCompatible
/* loaded from: b.zip:com/google/common/primitives/Booleans.class */
public final class Booleans {
    private Booleans() {
    }

    public static int compare(boolean z, boolean z2) {
        return z == z2 ? 0 : z ? 1 : -1;
    }
}
