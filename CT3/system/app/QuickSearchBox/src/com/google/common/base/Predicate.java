package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import javax.annotation.Nullable;
@GwtCompatible
/* loaded from: a.zip:com/google/common/base/Predicate.class */
public interface Predicate<T> {
    boolean apply(@Nullable T t);
}
