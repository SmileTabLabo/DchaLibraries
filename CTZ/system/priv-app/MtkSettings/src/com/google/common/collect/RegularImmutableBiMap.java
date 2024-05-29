package com.google.common.collect;

import com.google.common.collect.ImmutableMapEntry;
import java.io.Serializable;
import java.util.Map;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class RegularImmutableBiMap<K, V> extends ImmutableBiMap<K, V> {
    private final transient ImmutableMapEntry<K, V>[] entries;
    private final transient int hashCode;
    private transient ImmutableBiMap<V, K> inverse;
    private final transient ImmutableMapEntry<K, V>[] keyTable;
    private final transient int mask;
    private final transient ImmutableMapEntry<K, V>[] valueTable;

    /* JADX INFO: Access modifiers changed from: package-private */
    public RegularImmutableBiMap(int i, ImmutableMapEntry.TerminalEntry<?, ?>[] terminalEntryArr) {
        ImmutableMapEntry<K, V> nonTerminalBiMapEntry;
        int i2 = i;
        int closedTableSize = Hashing.closedTableSize(i2, 1.2d);
        this.mask = closedTableSize - 1;
        ImmutableMapEntry<K, V>[] createEntryArray = createEntryArray(closedTableSize);
        ImmutableMapEntry<K, V>[] createEntryArray2 = createEntryArray(closedTableSize);
        ImmutableMapEntry<K, V>[] createEntryArray3 = createEntryArray(i);
        int i3 = 0;
        int i4 = 0;
        while (i3 < i2) {
            ImmutableMapEntry.TerminalEntry<?, ?> terminalEntry = terminalEntryArr[i3];
            Object key = terminalEntry.getKey();
            Object value = terminalEntry.getValue();
            int hashCode = key.hashCode();
            int hashCode2 = value.hashCode();
            int smear = Hashing.smear(hashCode) & this.mask;
            int smear2 = Hashing.smear(hashCode2) & this.mask;
            ImmutableMapEntry<K, V> immutableMapEntry = createEntryArray[smear];
            ImmutableMapEntry<K, V> immutableMapEntry2 = immutableMapEntry;
            while (immutableMapEntry2 != null) {
                checkNoConflict(!key.equals(immutableMapEntry2.getKey()), "key", terminalEntry, immutableMapEntry2);
                immutableMapEntry2 = immutableMapEntry2.getNextInKeyBucket();
                key = key;
            }
            ImmutableMapEntry<K, V> immutableMapEntry3 = createEntryArray2[smear2];
            ImmutableMapEntry<K, V> immutableMapEntry4 = immutableMapEntry3;
            while (immutableMapEntry4 != null) {
                checkNoConflict(!value.equals(immutableMapEntry4.getValue()), "value", terminalEntry, immutableMapEntry4);
                immutableMapEntry4 = immutableMapEntry4.getNextInValueBucket();
                value = value;
            }
            if (immutableMapEntry != null || immutableMapEntry3 != null) {
                nonTerminalBiMapEntry = new NonTerminalBiMapEntry<>(terminalEntry, immutableMapEntry, immutableMapEntry3);
            } else {
                nonTerminalBiMapEntry = terminalEntry;
            }
            createEntryArray[smear] = nonTerminalBiMapEntry;
            createEntryArray2[smear2] = nonTerminalBiMapEntry;
            createEntryArray3[i3] = nonTerminalBiMapEntry;
            i4 += hashCode ^ hashCode2;
            i3++;
            i2 = i;
        }
        this.keyTable = createEntryArray;
        this.valueTable = createEntryArray2;
        this.entries = createEntryArray3;
        this.hashCode = i4;
    }

    /* loaded from: classes.dex */
    private static final class NonTerminalBiMapEntry<K, V> extends ImmutableMapEntry<K, V> {
        private final ImmutableMapEntry<K, V> nextInKeyBucket;
        private final ImmutableMapEntry<K, V> nextInValueBucket;

        NonTerminalBiMapEntry(ImmutableMapEntry<K, V> immutableMapEntry, ImmutableMapEntry<K, V> immutableMapEntry2, ImmutableMapEntry<K, V> immutableMapEntry3) {
            super(immutableMapEntry);
            this.nextInKeyBucket = immutableMapEntry2;
            this.nextInValueBucket = immutableMapEntry3;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.common.collect.ImmutableMapEntry
        public ImmutableMapEntry<K, V> getNextInKeyBucket() {
            return this.nextInKeyBucket;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.common.collect.ImmutableMapEntry
        public ImmutableMapEntry<K, V> getNextInValueBucket() {
            return this.nextInValueBucket;
        }
    }

    private static <K, V> ImmutableMapEntry<K, V>[] createEntryArray(int i) {
        return new ImmutableMapEntry[i];
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public V get(Object obj) {
        if (obj == null) {
            return null;
        }
        for (ImmutableMapEntry<K, V> immutableMapEntry = this.keyTable[Hashing.smear(obj.hashCode()) & this.mask]; immutableMapEntry != null; immutableMapEntry = immutableMapEntry.getNextInKeyBucket()) {
            if (obj.equals(immutableMapEntry.getKey())) {
                return immutableMapEntry.getValue();
            }
        }
        return null;
    }

    @Override // com.google.common.collect.ImmutableMap
    ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        return new ImmutableMapEntrySet<K, V>() { // from class: com.google.common.collect.RegularImmutableBiMap.1
            @Override // com.google.common.collect.ImmutableMapEntrySet
            ImmutableMap<K, V> map() {
                return RegularImmutableBiMap.this;
            }

            @Override // com.google.common.collect.ImmutableSet, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
            public UnmodifiableIterator<Map.Entry<K, V>> iterator() {
                return asList().iterator();
            }

            @Override // com.google.common.collect.ImmutableCollection
            ImmutableList<Map.Entry<K, V>> createAsList() {
                return new RegularImmutableAsList(this, RegularImmutableBiMap.this.entries);
            }

            @Override // com.google.common.collect.ImmutableSet
            boolean isHashCodeFast() {
                return true;
            }

            @Override // com.google.common.collect.ImmutableSet, java.util.Collection, java.util.Set
            public int hashCode() {
                return RegularImmutableBiMap.this.hashCode;
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableMap
    public boolean isPartialView() {
        return false;
    }

    @Override // java.util.Map
    public int size() {
        return this.entries.length;
    }

    @Override // com.google.common.collect.ImmutableBiMap
    public ImmutableBiMap<V, K> inverse() {
        ImmutableBiMap<V, K> immutableBiMap = this.inverse;
        if (immutableBiMap == null) {
            Inverse inverse = new Inverse();
            this.inverse = inverse;
            return inverse;
        }
        return immutableBiMap;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class Inverse extends ImmutableBiMap<V, K> {
        private Inverse() {
        }

        @Override // java.util.Map
        public int size() {
            return inverse().size();
        }

        @Override // com.google.common.collect.ImmutableBiMap
        public ImmutableBiMap<K, V> inverse() {
            return RegularImmutableBiMap.this;
        }

        @Override // com.google.common.collect.ImmutableMap, java.util.Map
        public K get(Object obj) {
            if (obj != null) {
                for (ImmutableMapEntry immutableMapEntry = RegularImmutableBiMap.this.valueTable[Hashing.smear(obj.hashCode()) & RegularImmutableBiMap.this.mask]; immutableMapEntry != null; immutableMapEntry = immutableMapEntry.getNextInValueBucket()) {
                    if (obj.equals(immutableMapEntry.getValue())) {
                        return immutableMapEntry.getKey();
                    }
                }
                return null;
            }
            return null;
        }

        @Override // com.google.common.collect.ImmutableMap
        ImmutableSet<Map.Entry<V, K>> createEntrySet() {
            return new InverseEntrySet();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: classes.dex */
        public final class InverseEntrySet extends ImmutableMapEntrySet<V, K> {
            InverseEntrySet() {
            }

            @Override // com.google.common.collect.ImmutableMapEntrySet
            ImmutableMap<V, K> map() {
                return Inverse.this;
            }

            @Override // com.google.common.collect.ImmutableSet
            boolean isHashCodeFast() {
                return true;
            }

            @Override // com.google.common.collect.ImmutableSet, java.util.Collection, java.util.Set
            public int hashCode() {
                return RegularImmutableBiMap.this.hashCode;
            }

            @Override // com.google.common.collect.ImmutableSet, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
            public UnmodifiableIterator<Map.Entry<V, K>> iterator() {
                return (UnmodifiableIterator<Map.Entry<K, V>>) asList().iterator();
            }

            @Override // com.google.common.collect.ImmutableCollection
            ImmutableList<Map.Entry<V, K>> createAsList() {
                return new ImmutableAsList<Map.Entry<V, K>>() { // from class: com.google.common.collect.RegularImmutableBiMap.Inverse.InverseEntrySet.1
                    @Override // java.util.List
                    public Map.Entry<V, K> get(int i) {
                        ImmutableMapEntry immutableMapEntry = RegularImmutableBiMap.this.entries[i];
                        return Maps.immutableEntry(immutableMapEntry.getValue(), immutableMapEntry.getKey());
                    }

                    @Override // com.google.common.collect.ImmutableAsList
                    ImmutableCollection<Map.Entry<V, K>> delegateCollection() {
                        return InverseEntrySet.this;
                    }
                };
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.common.collect.ImmutableMap
        public boolean isPartialView() {
            return false;
        }

        @Override // com.google.common.collect.ImmutableBiMap, com.google.common.collect.ImmutableMap
        Object writeReplace() {
            return new InverseSerializedForm(RegularImmutableBiMap.this);
        }
    }

    /* loaded from: classes.dex */
    private static class InverseSerializedForm<K, V> implements Serializable {
        private static final long serialVersionUID = 1;
        private final ImmutableBiMap<K, V> forward;

        InverseSerializedForm(ImmutableBiMap<K, V> immutableBiMap) {
            this.forward = immutableBiMap;
        }

        Object readResolve() {
            return this.forward.inverse();
        }
    }
}
