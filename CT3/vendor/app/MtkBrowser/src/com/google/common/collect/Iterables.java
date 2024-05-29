package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import javax.annotation.Nullable;
@GwtCompatible(emulated = true)
/* loaded from: b.zip:com/google/common/collect/Iterables.class */
public final class Iterables {
    private Iterables() {
    }

    @Nullable
    public static <T> T getFirst(Iterable<? extends T> iterable, @Nullable T t) {
        return (T) Iterators.getNext(iterable.iterator(), t);
    }

    public static <T> T getOnlyElement(Iterable<T> iterable) {
        return (T) Iterators.getOnlyElement(iterable.iterator());
    }
}
