package com.google.common.collect;

import com.google.common.collect.Maps;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/google/common/collect/AbstractNavigableMap.class */
public abstract class AbstractNavigableMap<K, V> extends AbstractMap<K, V> implements NavigableMap<K, V> {

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/google/common/collect/AbstractNavigableMap$DescendingMap.class */
    public final class DescendingMap extends Maps.DescendingMap<K, V> {
        final AbstractNavigableMap this$0;

        private DescendingMap(AbstractNavigableMap abstractNavigableMap) {
            this.this$0 = abstractNavigableMap;
        }

        /* synthetic */ DescendingMap(AbstractNavigableMap abstractNavigableMap, DescendingMap descendingMap) {
            this(abstractNavigableMap);
        }

        @Override // com.google.common.collect.Maps.DescendingMap
        Iterator<Map.Entry<K, V>> entryIterator() {
            return this.this$0.descendingEntryIterator();
        }

        @Override // com.google.common.collect.Maps.DescendingMap
        NavigableMap<K, V> forward() {
            return this.this$0;
        }
    }

    @Override // java.util.NavigableMap
    @Nullable
    public Map.Entry<K, V> ceilingEntry(K k) {
        return tailMap(k, true).firstEntry();
    }

    @Override // java.util.NavigableMap
    public K ceilingKey(K k) {
        return (K) Maps.keyOrNull(ceilingEntry(k));
    }

    abstract Iterator<Map.Entry<K, V>> descendingEntryIterator();

    @Override // java.util.NavigableMap
    public NavigableSet<K> descendingKeySet() {
        return descendingMap().navigableKeySet();
    }

    @Override // java.util.NavigableMap
    public NavigableMap<K, V> descendingMap() {
        return new DescendingMap(this, null);
    }

    abstract Iterator<Map.Entry<K, V>> entryIterator();

    @Override // java.util.AbstractMap, java.util.Map, java.util.SortedMap
    public Set<Map.Entry<K, V>> entrySet() {
        return new Maps.EntrySet<K, V>(this) { // from class: com.google.common.collect.AbstractNavigableMap.1
            final AbstractNavigableMap this$0;

            {
                this.this$0 = this;
            }

            @Override // java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set
            public Iterator<Map.Entry<K, V>> iterator() {
                return this.this$0.entryIterator();
            }

            @Override // com.google.common.collect.Maps.EntrySet
            Map<K, V> map() {
                return this.this$0;
            }
        };
    }

    @Override // java.util.NavigableMap
    @Nullable
    public Map.Entry<K, V> firstEntry() {
        return (Map.Entry) Iterators.getNext(entryIterator(), null);
    }

    @Override // java.util.SortedMap
    public K firstKey() {
        Map.Entry<K, V> firstEntry = firstEntry();
        if (firstEntry == null) {
            throw new NoSuchElementException();
        }
        return firstEntry.getKey();
    }

    @Override // java.util.NavigableMap
    @Nullable
    public Map.Entry<K, V> floorEntry(K k) {
        return headMap(k, true).lastEntry();
    }

    @Override // java.util.NavigableMap
    public K floorKey(K k) {
        return (K) Maps.keyOrNull(floorEntry(k));
    }

    @Override // java.util.AbstractMap, java.util.Map
    @Nullable
    public abstract V get(@Nullable Object obj);

    @Override // java.util.NavigableMap, java.util.SortedMap
    public SortedMap<K, V> headMap(K k) {
        return headMap(k, false);
    }

    @Override // java.util.NavigableMap
    @Nullable
    public Map.Entry<K, V> higherEntry(K k) {
        return tailMap(k, false).firstEntry();
    }

    @Override // java.util.NavigableMap
    public K higherKey(K k) {
        return (K) Maps.keyOrNull(higherEntry(k));
    }

    @Override // java.util.AbstractMap, java.util.Map, java.util.SortedMap
    public Set<K> keySet() {
        return navigableKeySet();
    }

    @Override // java.util.NavigableMap
    @Nullable
    public Map.Entry<K, V> lastEntry() {
        return (Map.Entry) Iterators.getNext(descendingEntryIterator(), null);
    }

    @Override // java.util.SortedMap
    public K lastKey() {
        Map.Entry<K, V> lastEntry = lastEntry();
        if (lastEntry == null) {
            throw new NoSuchElementException();
        }
        return lastEntry.getKey();
    }

    @Override // java.util.NavigableMap
    @Nullable
    public Map.Entry<K, V> lowerEntry(K k) {
        return headMap(k, false).lastEntry();
    }

    @Override // java.util.NavigableMap
    public K lowerKey(K k) {
        return (K) Maps.keyOrNull(lowerEntry(k));
    }

    @Override // java.util.NavigableMap
    public NavigableSet<K> navigableKeySet() {
        return new Maps.NavigableKeySet(this);
    }

    @Override // java.util.NavigableMap
    @Nullable
    public Map.Entry<K, V> pollFirstEntry() {
        return (Map.Entry) Iterators.pollNext(entryIterator());
    }

    @Override // java.util.NavigableMap
    @Nullable
    public Map.Entry<K, V> pollLastEntry() {
        return (Map.Entry) Iterators.pollNext(descendingEntryIterator());
    }

    @Override // java.util.AbstractMap, java.util.Map
    public abstract int size();

    @Override // java.util.NavigableMap, java.util.SortedMap
    public SortedMap<K, V> subMap(K k, K k2) {
        return subMap(k, true, k2, false);
    }

    @Override // java.util.NavigableMap, java.util.SortedMap
    public SortedMap<K, V> tailMap(K k) {
        return tailMap(k, true);
    }
}
