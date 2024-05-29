package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
@GwtCompatible(emulated = true)
/* loaded from: b.zip:com/google/common/base/Platform.class */
final class Platform {
    private Platform() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static long systemNanoTime() {
        return System.nanoTime();
    }
}
