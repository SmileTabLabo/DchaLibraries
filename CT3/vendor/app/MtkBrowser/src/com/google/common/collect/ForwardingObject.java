package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
@GwtCompatible
/* loaded from: b.zip:com/google/common/collect/ForwardingObject.class */
public abstract class ForwardingObject {
    protected abstract Object delegate();

    public String toString() {
        return delegate().toString();
    }
}
