package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Comparator;
@GwtCompatible
/* loaded from: a.zip:com/google/common/collect/SortedIterable.class */
interface SortedIterable<T> extends Iterable<T> {
    Comparator<? super T> comparator();
}
