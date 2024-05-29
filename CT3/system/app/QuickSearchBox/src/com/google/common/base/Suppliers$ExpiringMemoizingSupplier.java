package com.google.common.base;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
@VisibleForTesting
/* loaded from: a.zip:com/google/common/base/Suppliers$ExpiringMemoizingSupplier.class */
class Suppliers$ExpiringMemoizingSupplier<T> implements Supplier<T>, Serializable {
    private static final long serialVersionUID = 0;
    final Supplier<T> delegate;
    final long durationNanos;

    public String toString() {
        return "Suppliers.memoizeWithExpiration(" + this.delegate + ", " + this.durationNanos + ", NANOS)";
    }
}
