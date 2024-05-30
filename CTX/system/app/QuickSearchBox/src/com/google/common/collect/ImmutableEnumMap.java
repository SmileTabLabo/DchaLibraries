package com.google.common.collect;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.lang.Enum;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class ImmutableEnumMap<K extends Enum<K>, V> extends ImmutableMap<K, V> {
    private final transient EnumMap<K, V> delegate;

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <K extends Enum<K>, V> ImmutableMap<K, V> asImmutable(EnumMap<K, V> enumMap) {
        switch (enumMap.size()) {
            case 0:
                return ImmutableMap.of();
            case 1:
                Map.Entry entry = (Map.Entry) Iterables.getOnlyElement(enumMap.entrySet());
                return ImmutableMap.of(entry.getKey(), entry.getValue());
            default:
                return new ImmutableEnumMap(enumMap);
        }
    }

    private ImmutableEnumMap(EnumMap<K, V> enumMap) {
        this.delegate = enumMap;
        Preconditions.checkArgument(!enumMap.isEmpty());
    }

    @Override // com.google.common.collect.ImmutableMap
    ImmutableSet<K> createKeySet() {
        return (ImmutableSet<K>) new ImmutableSet<K>() { // from class: com.google.common.collect.ImmutableEnumMap.1
            @Override // com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.util.Set
            public boolean contains(Object obj) {
                return ImmutableEnumMap.this.delegate.containsKey(obj);
            }

            @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
            public int size() {
                return ImmutableEnumMap.this.size();
            }

            @Override // com.google.common.collect.ImmutableSet, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
            public UnmodifiableIterator<K> iterator() {
                return Iterators.unmodifiableIterator(ImmutableEnumMap.this.delegate.keySet().iterator());
            }

            /* JADX INFO: Access modifiers changed from: package-private */
            @Override // com.google.common.collect.ImmutableCollection
            public boolean isPartialView() {
                return true;
            }
        };
    }

    @Override // java.util.Map
    public int size() {
        return this.delegate.size();
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public boolean containsKey(Object obj) {
        return this.delegate.containsKey(obj);
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public V get(Object obj) {
        return this.delegate.get(obj);
    }

    @Override // com.google.common.collect.ImmutableMap
    ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        return new ImmutableMapEntrySet<K, V>() { // from class: com.google.common.collect.ImmutableEnumMap.2
            @Override // com.google.common.collect.ImmutableMapEntrySet
            ImmutableMap<K, V> map() {
                return ImmutableEnumMap.this;
            }

            @Override // com.google.common.collect.ImmutableSet, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
            public UnmodifiableIterator<Map.Entry<K, V>> iterator() {
                return (UnmodifiableIterator<Map.Entry<K, V>>) new UnmodifiableIterator<Map.Entry<K, V>>() { // from class: com.google.common.collect.ImmutableEnumMap.2.1
                    private final Iterator<Map.Entry<K, V>> backingIterator;

                    {
                        this.backingIterator = ImmutableEnumMap.this.delegate.entrySet().iterator();
                    }

                    @Override // java.util.Iterator
                    public boolean hasNext() {
                        return this.backingIterator.hasNext();
                    }

                    @Override // java.util.Iterator
                    public Map.Entry<K, V> next() {
                        Map.Entry<K, V> next = this.backingIterator.next();
                        return Maps.immutableEntry(next.getKey(), next.getValue());
                    }
                };
            }
        };
    }

    @Override // com.google.common.collect.ImmutableMap
    boolean isPartialView() {
        return false;
    }

    @Override // com.google.common.collect.ImmutableMap
    Object writeReplace() {
        return new EnumSerializedForm(this.delegate);
    }

    /* loaded from: classes.dex */
    private static class EnumSerializedForm<K extends Enum<K>, V> implements Serializable {
        private static final long serialVersionUID = 0;
        final EnumMap<K, V> delegate;

        EnumSerializedForm(EnumMap<K, V> enumMap) {
            this.delegate = enumMap;
        }

        Object readResolve() {
            return new ImmutableEnumMap(this.delegate);
        }
    }
}
