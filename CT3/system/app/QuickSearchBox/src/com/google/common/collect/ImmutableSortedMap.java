package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import javax.annotation.Nullable;
@GwtCompatible(emulated = true, serializable = true)
/* loaded from: a.zip:com/google/common/collect/ImmutableSortedMap.class */
public abstract class ImmutableSortedMap<K, V> extends ImmutableSortedMapFauxverideShim<K, V> implements NavigableMap<K, V> {
    private static final long serialVersionUID = 0;
    private transient ImmutableSortedMap<K, V> descendingMap;
    private static final Comparator<Comparable> NATURAL_ORDER = Ordering.natural();
    private static final ImmutableSortedMap<Comparable, Object> NATURAL_EMPTY_MAP = new EmptyImmutableSortedMap(NATURAL_ORDER);

    /* loaded from: a.zip:com/google/common/collect/ImmutableSortedMap$Builder.class */
    public static class Builder<K, V> extends ImmutableMap.Builder<K, V> {
        private final Comparator<? super K> comparator;

        public Builder(Comparator<? super K> comparator) {
            this.comparator = (Comparator) Preconditions.checkNotNull(comparator);
        }

        @Override // com.google.common.collect.ImmutableMap.Builder
        public ImmutableSortedMap<K, V> build() {
            return ImmutableSortedMap.fromEntries(this.comparator, false, this.size, this.entries);
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // com.google.common.collect.ImmutableMap.Builder
        public /* bridge */ /* synthetic */ ImmutableMap.Builder put(Object obj, Object obj2) {
            return put((Builder<K, V>) obj, obj2);
        }

        @Override // com.google.common.collect.ImmutableMap.Builder
        public Builder<K, V> put(K k, V v) {
            super.put((Builder<K, V>) k, (K) v);
            return this;
        }
    }

    /* loaded from: a.zip:com/google/common/collect/ImmutableSortedMap$SerializedForm.class */
    private static class SerializedForm extends ImmutableMap.SerializedForm {
        private static final long serialVersionUID = 0;
        private final Comparator<Object> comparator;

        SerializedForm(ImmutableSortedMap<?, ?> immutableSortedMap) {
            super(immutableSortedMap);
            this.comparator = immutableSortedMap.comparator();
        }

        @Override // com.google.common.collect.ImmutableMap.SerializedForm
        Object readResolve() {
            return createMap(new Builder(this.comparator));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ImmutableSortedMap() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ImmutableSortedMap(ImmutableSortedMap<K, V> immutableSortedMap) {
        this.descendingMap = immutableSortedMap;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <K, V> ImmutableSortedMap<K, V> emptyMap(Comparator<? super K> comparator) {
        return Ordering.natural().equals(comparator) ? of() : new EmptyImmutableSortedMap(comparator);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <K, V> ImmutableSortedMap<K, V> from(ImmutableSortedSet<K> immutableSortedSet, ImmutableList<V> immutableList) {
        return immutableSortedSet.isEmpty() ? emptyMap(immutableSortedSet.comparator()) : new RegularImmutableSortedMap((RegularImmutableSortedSet) immutableSortedSet, immutableList);
    }

    static <K, V> ImmutableSortedMap<K, V> fromEntries(Comparator<? super K> comparator, boolean z, int i, Map.Entry<K, V>... entryArr) {
        for (int i2 = 0; i2 < i; i2++) {
            Map.Entry<K, V> entry = entryArr[i2];
            entryArr[i2] = entryOf(entry.getKey(), entry.getValue());
        }
        if (!z) {
            sortEntries(comparator, i, entryArr);
            validateEntries(i, entryArr, comparator);
        }
        return fromSortedEntries(comparator, i, entryArr);
    }

    static <K, V> ImmutableSortedMap<K, V> fromSortedEntries(Comparator<? super K> comparator, int i, Map.Entry<K, V>[] entryArr) {
        if (i == 0) {
            return emptyMap(comparator);
        }
        ImmutableList.Builder builder = ImmutableList.builder();
        ImmutableList.Builder builder2 = ImmutableList.builder();
        for (int i2 = 0; i2 < i; i2++) {
            Map.Entry<K, V> entry = entryArr[i2];
            builder.add((ImmutableList.Builder) entry.getKey());
            builder2.add((ImmutableList.Builder) entry.getValue());
        }
        return new RegularImmutableSortedMap(new RegularImmutableSortedSet(builder.build(), comparator), builder2.build());
    }

    public static <K, V> ImmutableSortedMap<K, V> of() {
        return (ImmutableSortedMap<K, V>) NATURAL_EMPTY_MAP;
    }

    private static <K, V> void sortEntries(Comparator<? super K> comparator, int i, Map.Entry<K, V>[] entryArr) {
        Arrays.sort(entryArr, 0, i, Ordering.from(comparator).onKeys());
    }

    private static <K, V> void validateEntries(int i, Map.Entry<K, V>[] entryArr, Comparator<? super K> comparator) {
        for (int i2 = 1; i2 < i; i2++) {
            checkNoConflict(comparator.compare(entryArr[i2 - 1].getKey(), entryArr[i2].getKey()) != 0, "key", entryArr[i2 - 1], entryArr[i2]);
        }
    }

    @Override // java.util.NavigableMap
    public Map.Entry<K, V> ceilingEntry(K k) {
        return tailMap((ImmutableSortedMap<K, V>) k, true).firstEntry();
    }

    @Override // java.util.NavigableMap
    public K ceilingKey(K k) {
        return (K) Maps.keyOrNull(ceilingEntry(k));
    }

    @Override // java.util.SortedMap
    public Comparator<? super K> comparator() {
        return keySet().comparator();
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public boolean containsValue(@Nullable Object obj) {
        return values().contains(obj);
    }

    abstract ImmutableSortedMap<K, V> createDescendingMap();

    @Override // java.util.NavigableMap
    public ImmutableSortedSet<K> descendingKeySet() {
        return keySet().descendingSet();
    }

    @Override // java.util.NavigableMap
    public ImmutableSortedMap<K, V> descendingMap() {
        ImmutableSortedMap<K, V> immutableSortedMap = this.descendingMap;
        ImmutableSortedMap<K, V> immutableSortedMap2 = immutableSortedMap;
        if (immutableSortedMap == null) {
            immutableSortedMap2 = createDescendingMap();
            this.descendingMap = immutableSortedMap2;
        }
        return immutableSortedMap2;
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public ImmutableSet<Map.Entry<K, V>> entrySet() {
        return super.entrySet();
    }

    @Override // java.util.NavigableMap
    public Map.Entry<K, V> firstEntry() {
        return isEmpty() ? null : entrySet().asList().get(0);
    }

    @Override // java.util.SortedMap
    public K firstKey() {
        return keySet().first();
    }

    @Override // java.util.NavigableMap
    public Map.Entry<K, V> floorEntry(K k) {
        return headMap((ImmutableSortedMap<K, V>) k, true).lastEntry();
    }

    @Override // java.util.NavigableMap
    public K floorKey(K k) {
        return (K) Maps.keyOrNull(floorEntry(k));
    }

    @Override // java.util.NavigableMap, java.util.SortedMap
    public ImmutableSortedMap<K, V> headMap(K k) {
        return headMap((ImmutableSortedMap<K, V>) k, false);
    }

    public abstract ImmutableSortedMap<K, V> headMap(K k, boolean z);

    /* JADX WARN: Multi-variable type inference failed */
    public /* bridge */ /* synthetic */ NavigableMap headMap(Object obj, boolean z) {
        return headMap((ImmutableSortedMap<K, V>) obj, z);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.NavigableMap, java.util.SortedMap
    public /* bridge */ /* synthetic */ SortedMap headMap(Object obj) {
        return headMap((ImmutableSortedMap<K, V>) obj);
    }

    @Override // java.util.NavigableMap
    public Map.Entry<K, V> higherEntry(K k) {
        return tailMap((ImmutableSortedMap<K, V>) k, false).firstEntry();
    }

    @Override // java.util.NavigableMap
    public K higherKey(K k) {
        return (K) Maps.keyOrNull(higherEntry(k));
    }

    @Override // com.google.common.collect.ImmutableMap
    boolean isPartialView() {
        return !keySet().isPartialView() ? values().isPartialView() : true;
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public abstract ImmutableSortedSet<K> keySet();

    @Override // java.util.NavigableMap
    public Map.Entry<K, V> lastEntry() {
        return isEmpty() ? null : entrySet().asList().get(size() - 1);
    }

    @Override // java.util.SortedMap
    public K lastKey() {
        return keySet().last();
    }

    @Override // java.util.NavigableMap
    public Map.Entry<K, V> lowerEntry(K k) {
        return headMap((ImmutableSortedMap<K, V>) k, false).lastEntry();
    }

    @Override // java.util.NavigableMap
    public K lowerKey(K k) {
        return (K) Maps.keyOrNull(lowerEntry(k));
    }

    @Override // java.util.NavigableMap
    public ImmutableSortedSet<K> navigableKeySet() {
        return keySet();
    }

    @Override // java.util.NavigableMap
    @Deprecated
    public final Map.Entry<K, V> pollFirstEntry() {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.NavigableMap
    @Deprecated
    public final Map.Entry<K, V> pollLastEntry() {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.Map
    public int size() {
        return values().size();
    }

    @Override // java.util.NavigableMap, java.util.SortedMap
    public ImmutableSortedMap<K, V> subMap(K k, K k2) {
        return subMap((boolean) k, true, (boolean) k2, false);
    }

    @Override // java.util.NavigableMap
    public ImmutableSortedMap<K, V> subMap(K k, boolean z, K k2, boolean z2) {
        Preconditions.checkNotNull(k);
        Preconditions.checkNotNull(k2);
        Preconditions.checkArgument(comparator().compare(k, k2) <= 0, "expected fromKey <= toKey but %s > %s", k, k2);
        return headMap((ImmutableSortedMap<K, V>) k2, z2).tailMap((ImmutableSortedMap<K, V>) k, z);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.NavigableMap
    public /* bridge */ /* synthetic */ NavigableMap subMap(Object obj, boolean z, Object obj2, boolean z2) {
        return subMap((boolean) obj, z, (boolean) obj2, z2);
    }

    @Override // java.util.NavigableMap, java.util.SortedMap
    public ImmutableSortedMap<K, V> tailMap(K k) {
        return tailMap((ImmutableSortedMap<K, V>) k, true);
    }

    public abstract ImmutableSortedMap<K, V> tailMap(K k, boolean z);

    /* JADX WARN: Multi-variable type inference failed */
    public /* bridge */ /* synthetic */ NavigableMap tailMap(Object obj, boolean z) {
        return tailMap((ImmutableSortedMap<K, V>) obj, z);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.NavigableMap, java.util.SortedMap
    public /* bridge */ /* synthetic */ SortedMap tailMap(Object obj) {
        return tailMap((ImmutableSortedMap<K, V>) obj);
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map, java.util.SortedMap
    public abstract ImmutableCollection<V> values();

    @Override // com.google.common.collect.ImmutableMap
    Object writeReplace() {
        return new SerializedForm(this);
    }
}
