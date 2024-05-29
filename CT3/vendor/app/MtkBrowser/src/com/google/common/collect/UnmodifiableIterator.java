package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Iterator;
@GwtCompatible
/* loaded from: b.zip:com/google/common/collect/UnmodifiableIterator.class */
public abstract class UnmodifiableIterator<E> implements Iterator<E> {
    @Override // java.util.Iterator
    @Deprecated
    public final void remove() {
        throw new UnsupportedOperationException();
    }
}
