package com.google.common.io;

import com.google.common.annotations.VisibleForTesting;
import java.lang.reflect.Method;
@VisibleForTesting
/* loaded from: b.zip:com/google/common/io/Closer$SuppressingSuppressor.class */
final class Closer$SuppressingSuppressor implements Closer$Suppressor {
    static final Closer$SuppressingSuppressor INSTANCE = new Closer$SuppressingSuppressor();
    static final Method addSuppressed = getAddSuppressed();

    Closer$SuppressingSuppressor() {
    }

    private static Method getAddSuppressed() {
        try {
            return Throwable.class.getMethod("addSuppressed", Throwable.class);
        } catch (Throwable th) {
            return null;
        }
    }
}
