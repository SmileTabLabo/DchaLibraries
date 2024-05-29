package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Map;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtCompatible(emulated = true, serializable = true)
/* loaded from: b.zip:com/google/common/collect/SingletonImmutableBiMap.class */
public final class SingletonImmutableBiMap<K, V> extends ImmutableBiMap<K, V> {
    transient ImmutableBiMap<V, K> inverse;
    final transient K singleKey;
    final transient V singleValue;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SingletonImmutableBiMap(K k, V v) {
        CollectPreconditions.checkEntryNotNull(k, v);
        this.singleKey = k;
        this.singleValue = v;
    }

    private SingletonImmutableBiMap(K k, V v, ImmutableBiMap<V, K> immutableBiMap) {
        this.singleKey = k;
        this.singleValue = v;
        this.inverse = immutableBiMap;
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public boolean containsKey(@Nullable Object obj) {
        return this.singleKey.equals(obj);
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public boolean containsValue(@Nullable Object obj) {
        return this.singleValue.equals(obj);
    }

    @Override // com.google.common.collect.ImmutableMap
    ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        return ImmutableSet.of(Maps.immutableEntry(this.singleKey, this.singleValue));
    }

    @Override // com.google.common.collect.ImmutableMap
    ImmutableSet<K> createKeySet() {
        return ImmutableSet.of(this.singleKey);
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public V get(@Nullable Object obj) {
        return this.singleKey.equals(obj) ? this.singleValue : null;
    }

    @Override // com.google.common.collect.ImmutableBiMap
    public ImmutableBiMap<V, K> inverse() {
        ImmutableBiMap<V, K> immutableBiMap = this.inverse;
        if (immutableBiMap == null) {
            SingletonImmutableBiMap singletonImmutableBiMap = new SingletonImmutableBiMap(this.singleValue, this.singleKey, this);
            this.inverse = singletonImmutableBiMap;
            return singletonImmutableBiMap;
        }
        return immutableBiMap;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableMap
    public boolean isPartialView() {
        return false;
    }

    @Override // java.util.Map
    public int size() {
        return 1;
    }
}
