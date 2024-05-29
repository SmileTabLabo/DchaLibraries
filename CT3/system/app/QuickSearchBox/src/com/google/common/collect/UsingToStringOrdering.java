package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.io.Serializable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtCompatible(serializable = true)
/* loaded from: a.zip:com/google/common/collect/UsingToStringOrdering.class */
public final class UsingToStringOrdering extends Ordering<Object> implements Serializable {
    static final UsingToStringOrdering INSTANCE = new UsingToStringOrdering();
    private static final long serialVersionUID = 0;

    private UsingToStringOrdering() {
    }

    private Object readResolve() {
        return INSTANCE;
    }

    @Override // com.google.common.collect.Ordering, java.util.Comparator
    public int compare(Object obj, Object obj2) {
        return obj.toString().compareTo(obj2.toString());
    }

    public String toString() {
        return "Ordering.usingToString()";
    }
}
