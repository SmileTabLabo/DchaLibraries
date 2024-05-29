package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Map;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtCompatible(emulated = true)
/* loaded from: a.zip:com/google/common/collect/EmptyImmutableBiMap.class */
public final class EmptyImmutableBiMap extends ImmutableBiMap<Object, Object> {
    static final EmptyImmutableBiMap INSTANCE = new EmptyImmutableBiMap();

    private EmptyImmutableBiMap() {
    }

    @Override // com.google.common.collect.ImmutableMap
    ImmutableSet<Map.Entry<Object, Object>> createEntrySet() {
        throw new AssertionError("should never be called");
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public ImmutableSet<Map.Entry<Object, Object>> entrySet() {
        return ImmutableSet.of();
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public Object get(@Nullable Object obj) {
        return null;
    }

    @Override // com.google.common.collect.ImmutableBiMap
    public ImmutableBiMap<Object, Object> inverse() {
        return this;
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public boolean isEmpty() {
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableMap
    public boolean isPartialView() {
        return false;
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public ImmutableSet<Object> keySet() {
        return ImmutableSet.of();
    }

    Object readResolve() {
        return INSTANCE;
    }

    @Override // java.util.Map
    public int size() {
        return 0;
    }
}
