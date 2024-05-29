package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.NavigableMap;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtCompatible(emulated = true)
/* loaded from: b.zip:com/google/common/collect/RegularImmutableSortedMap.class */
public final class RegularImmutableSortedMap<K, V> extends ImmutableSortedMap<K, V> {
    private final transient RegularImmutableSortedSet<K> keySet;
    private final transient ImmutableList<V> valueList;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/google/common/collect/RegularImmutableSortedMap$EntrySet.class */
    public class EntrySet extends ImmutableMapEntrySet<K, V> {
        final RegularImmutableSortedMap this$0;

        private EntrySet(RegularImmutableSortedMap regularImmutableSortedMap) {
            this.this$0 = regularImmutableSortedMap;
        }

        /* synthetic */ EntrySet(RegularImmutableSortedMap regularImmutableSortedMap, EntrySet entrySet) {
            this(regularImmutableSortedMap);
        }

        @Override // com.google.common.collect.ImmutableCollection
        ImmutableList<Map.Entry<K, V>> createAsList() {
            return new ImmutableAsList<Map.Entry<K, V>>(this) { // from class: com.google.common.collect.RegularImmutableSortedMap.EntrySet.1
                private final ImmutableList<K> keyList;
                final EntrySet this$1;

                {
                    this.this$1 = this;
                    this.keyList = this.this$1.this$0.keySet().asList();
                }

                @Override // com.google.common.collect.ImmutableAsList
                ImmutableCollection<Map.Entry<K, V>> delegateCollection() {
                    return this.this$1;
                }

                @Override // java.util.List
                public Map.Entry<K, V> get(int i) {
                    return Maps.immutableEntry(this.keyList.get(i), this.this$1.this$0.valueList.get(i));
                }
            };
        }

        @Override // com.google.common.collect.ImmutableSet, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
        public UnmodifiableIterator<Map.Entry<K, V>> iterator() {
            return asList().iterator();
        }

        @Override // com.google.common.collect.ImmutableMapEntrySet
        ImmutableMap<K, V> map() {
            return this.this$0;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public RegularImmutableSortedMap(RegularImmutableSortedSet<K> regularImmutableSortedSet, ImmutableList<V> immutableList) {
        this.keySet = regularImmutableSortedSet;
        this.valueList = immutableList;
    }

    RegularImmutableSortedMap(RegularImmutableSortedSet<K> regularImmutableSortedSet, ImmutableList<V> immutableList, ImmutableSortedMap<K, V> immutableSortedMap) {
        super(immutableSortedMap);
        this.keySet = regularImmutableSortedSet;
        this.valueList = immutableList;
    }

    private ImmutableSortedMap<K, V> getSubMap(int i, int i2) {
        return (i == 0 && i2 == size()) ? this : i == i2 ? emptyMap(comparator()) : from(this.keySet.getSubSet(i, i2), this.valueList.subList(i, i2));
    }

    @Override // com.google.common.collect.ImmutableSortedMap
    ImmutableSortedMap<K, V> createDescendingMap() {
        return new RegularImmutableSortedMap((RegularImmutableSortedSet) this.keySet.descendingSet(), this.valueList.reverse(), this);
    }

    @Override // com.google.common.collect.ImmutableMap
    ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        return new EntrySet(this, null);
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public V get(@Nullable Object obj) {
        int indexOf = this.keySet.indexOf(obj);
        return indexOf == -1 ? null : this.valueList.get(indexOf);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.google.common.collect.ImmutableSortedMap, java.util.NavigableMap
    public ImmutableSortedMap<K, V> headMap(K k, boolean z) {
        return getSubMap(0, this.keySet.headIndex(Preconditions.checkNotNull(k), z));
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.google.common.collect.ImmutableSortedMap, java.util.NavigableMap
    public /* bridge */ /* synthetic */ NavigableMap headMap(Object obj, boolean z) {
        return headMap((RegularImmutableSortedMap<K, V>) obj, z);
    }

    @Override // com.google.common.collect.ImmutableSortedMap, com.google.common.collect.ImmutableMap, java.util.Map
    public ImmutableSortedSet<K> keySet() {
        return this.keySet;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.google.common.collect.ImmutableSortedMap, java.util.NavigableMap
    public ImmutableSortedMap<K, V> tailMap(K k, boolean z) {
        return getSubMap(this.keySet.tailIndex(Preconditions.checkNotNull(k), z), size());
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.google.common.collect.ImmutableSortedMap, java.util.NavigableMap
    public /* bridge */ /* synthetic */ NavigableMap tailMap(Object obj, boolean z) {
        return tailMap((RegularImmutableSortedMap<K, V>) obj, z);
    }

    @Override // com.google.common.collect.ImmutableSortedMap, com.google.common.collect.ImmutableMap, java.util.Map, java.util.SortedMap
    public ImmutableCollection<V> values() {
        return this.valueList;
    }
}
