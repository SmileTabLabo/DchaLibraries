package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtCompatible(emulated = true)
/* loaded from: b.zip:com/google/common/collect/EmptyImmutableSortedMap.class */
public final class EmptyImmutableSortedMap<K, V> extends ImmutableSortedMap<K, V> {
    private final transient ImmutableSortedSet<K> keySet;

    /* JADX INFO: Access modifiers changed from: package-private */
    public EmptyImmutableSortedMap(Comparator<? super K> comparator) {
        this.keySet = ImmutableSortedSet.emptySet(comparator);
    }

    EmptyImmutableSortedMap(Comparator<? super K> comparator, ImmutableSortedMap<K, V> immutableSortedMap) {
        super(immutableSortedMap);
        this.keySet = ImmutableSortedSet.emptySet(comparator);
    }

    @Override // com.google.common.collect.ImmutableSortedMap
    ImmutableSortedMap<K, V> createDescendingMap() {
        return new EmptyImmutableSortedMap(Ordering.from(comparator()).reverse(), this);
    }

    @Override // com.google.common.collect.ImmutableMap
    ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        throw new AssertionError("should never be called");
    }

    @Override // com.google.common.collect.ImmutableSortedMap, com.google.common.collect.ImmutableMap, java.util.Map
    public ImmutableSet<Map.Entry<K, V>> entrySet() {
        return ImmutableSet.of();
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public V get(@Nullable Object obj) {
        return null;
    }

    @Override // com.google.common.collect.ImmutableSortedMap, java.util.NavigableMap
    public ImmutableSortedMap<K, V> headMap(K k, boolean z) {
        Preconditions.checkNotNull(k);
        return this;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.google.common.collect.ImmutableSortedMap, java.util.NavigableMap
    public /* bridge */ /* synthetic */ NavigableMap headMap(Object obj, boolean z) {
        return headMap((EmptyImmutableSortedMap<K, V>) obj, z);
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public boolean isEmpty() {
        return true;
    }

    @Override // com.google.common.collect.ImmutableSortedMap, com.google.common.collect.ImmutableMap
    boolean isPartialView() {
        return false;
    }

    @Override // com.google.common.collect.ImmutableSortedMap, com.google.common.collect.ImmutableMap, java.util.Map
    public ImmutableSortedSet<K> keySet() {
        return this.keySet;
    }

    @Override // com.google.common.collect.ImmutableSortedMap, java.util.Map
    public int size() {
        return 0;
    }

    @Override // com.google.common.collect.ImmutableSortedMap, java.util.NavigableMap
    public ImmutableSortedMap<K, V> tailMap(K k, boolean z) {
        Preconditions.checkNotNull(k);
        return this;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.google.common.collect.ImmutableSortedMap, java.util.NavigableMap
    public /* bridge */ /* synthetic */ NavigableMap tailMap(Object obj, boolean z) {
        return tailMap((EmptyImmutableSortedMap<K, V>) obj, z);
    }

    @Override // com.google.common.collect.ImmutableMap
    public String toString() {
        return "{}";
    }

    @Override // com.google.common.collect.ImmutableSortedMap, com.google.common.collect.ImmutableMap, java.util.Map, java.util.SortedMap
    public ImmutableCollection<V> values() {
        return ImmutableList.of();
    }
}
