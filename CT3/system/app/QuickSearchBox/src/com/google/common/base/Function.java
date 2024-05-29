package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import javax.annotation.Nullable;
@GwtCompatible
/* loaded from: a.zip:com/google/common/base/Function.class */
public interface Function<F, T> {
    @Nullable
    T apply(@Nullable F f);

    boolean equals(@Nullable Object obj);
}
