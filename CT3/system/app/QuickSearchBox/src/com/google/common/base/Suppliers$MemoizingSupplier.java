package com.google.common.base;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
@VisibleForTesting
/* loaded from: a.zip:com/google/common/base/Suppliers$MemoizingSupplier.class */
class Suppliers$MemoizingSupplier<T> implements Supplier<T>, Serializable {
    private static final long serialVersionUID = 0;
    final Supplier<T> delegate;

    public String toString() {
        return "Suppliers.memoize(" + this.delegate + ")";
    }
}
