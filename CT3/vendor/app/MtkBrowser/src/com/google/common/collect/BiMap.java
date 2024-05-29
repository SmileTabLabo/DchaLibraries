package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Map;
import java.util.Set;
@GwtCompatible
/* loaded from: b.zip:com/google/common/collect/BiMap.class */
public interface BiMap<K, V> extends Map<K, V> {
    @Override // java.util.Map
    Set<V> values();
}
