package com.google.common.base;

import javax.annotation.Nullable;
/* loaded from: a.zip:com/google/common/base/Throwables.class */
public final class Throwables {
    private Throwables() {
    }

    public static <X extends Throwable> void propagateIfInstanceOf(@Nullable Throwable th, Class<X> cls) throws Throwable {
        if (th != null && cls.isInstance(th)) {
            throw cls.cast(th);
        }
    }
}
