package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ImmutableMapEntry;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtCompatible(emulated = true, serializable = true)
/* loaded from: a.zip:com/google/common/collect/RegularImmutableBiMap.class */
public class RegularImmutableBiMap<K, V> extends ImmutableBiMap<K, V> {
    private final transient ImmutableMapEntry<K, V>[] entries;
    private final transient int hashCode;
    private transient ImmutableBiMap<V, K> inverse;
    private final transient ImmutableMapEntry<K, V>[] keyTable;
    private final transient int mask;
    private final transient ImmutableMapEntry<K, V>[] valueTable;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/google/common/collect/RegularImmutableBiMap$Inverse.class */
    public final class Inverse extends ImmutableBiMap<V, K> {
        final RegularImmutableBiMap this$0;

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: a.zip:com/google/common/collect/RegularImmutableBiMap$Inverse$InverseEntrySet.class */
        public final class InverseEntrySet extends ImmutableMapEntrySet<V, K> {
            final Inverse this$1;

            InverseEntrySet(Inverse inverse) {
                this.this$1 = inverse;
            }

            @Override // com.google.common.collect.ImmutableCollection
            ImmutableList<Map.Entry<V, K>> createAsList() {
                return new ImmutableAsList<Map.Entry<V, K>>(this) { // from class: com.google.common.collect.RegularImmutableBiMap.Inverse.InverseEntrySet.1
                    final InverseEntrySet this$2;

                    {
                        this.this$2 = this;
                    }

                    @Override // com.google.common.collect.ImmutableAsList
                    ImmutableCollection<Map.Entry<V, K>> delegateCollection() {
                        return this.this$2;
                    }

                    @Override // java.util.List
                    public Map.Entry<V, K> get(int i) {
                        ImmutableMapEntry immutableMapEntry = this.this$2.this$1.this$0.entries[i];
                        return Maps.immutableEntry(immutableMapEntry.getValue(), immutableMapEntry.getKey());
                    }
                };
            }

            @Override // com.google.common.collect.ImmutableSet, java.util.Collection, java.util.Set
            public int hashCode() {
                return this.this$1.this$0.hashCode;
            }

            @Override // com.google.common.collect.ImmutableSet
            boolean isHashCodeFast() {
                return true;
            }

            @Override // com.google.common.collect.ImmutableSet, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
            public UnmodifiableIterator<Map.Entry<V, K>> iterator() {
                return (UnmodifiableIterator<Map.Entry<K, V>>) asList().iterator();
            }

            @Override // com.google.common.collect.ImmutableMapEntrySet
            ImmutableMap<V, K> map() {
                return this.this$1;
            }
        }

        private Inverse(RegularImmutableBiMap regularImmutableBiMap) {
            this.this$0 = regularImmutableBiMap;
        }

        /* synthetic */ Inverse(RegularImmutableBiMap regularImmutableBiMap, Inverse inverse) {
            this(regularImmutableBiMap);
        }

        @Override // com.google.common.collect.ImmutableMap
        ImmutableSet<Map.Entry<V, K>> createEntrySet() {
            return new InverseEntrySet(this);
        }

        @Override // com.google.common.collect.ImmutableMap, java.util.Map
        public K get(@Nullable Object obj) {
            if (obj == null) {
                return null;
            }
            ImmutableMapEntry immutableMapEntry = this.this$0.valueTable[Hashing.smear(obj.hashCode()) & this.this$0.mask];
            while (true) {
                ImmutableMapEntry immutableMapEntry2 = immutableMapEntry;
                if (immutableMapEntry2 == null) {
                    return null;
                }
                if (obj.equals(immutableMapEntry2.getValue())) {
                    return immutableMapEntry2.getKey();
                }
                immutableMapEntry = immutableMapEntry2.getNextInValueBucket();
            }
        }

        @Override // com.google.common.collect.ImmutableBiMap
        public ImmutableBiMap<K, V> inverse() {
            return this.this$0;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.common.collect.ImmutableMap
        public boolean isPartialView() {
            return false;
        }

        @Override // java.util.Map
        public int size() {
            return inverse().size();
        }

        @Override // com.google.common.collect.ImmutableBiMap, com.google.common.collect.ImmutableMap
        Object writeReplace() {
            return new InverseSerializedForm(this.this$0);
        }
    }

    /* loaded from: a.zip:com/google/common/collect/RegularImmutableBiMap$InverseSerializedForm.class */
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

    /* loaded from: a.zip:com/google/common/collect/RegularImmutableBiMap$NonTerminalBiMapEntry.class */
    private static final class NonTerminalBiMapEntry<K, V> extends ImmutableMapEntry<K, V> {
        @Nullable
        private final ImmutableMapEntry<K, V> nextInKeyBucket;
        @Nullable
        private final ImmutableMapEntry<K, V> nextInValueBucket;

        NonTerminalBiMapEntry(ImmutableMapEntry<K, V> immutableMapEntry, @Nullable ImmutableMapEntry<K, V> immutableMapEntry2, @Nullable ImmutableMapEntry<K, V> immutableMapEntry3) {
            super(immutableMapEntry);
            this.nextInKeyBucket = immutableMapEntry2;
            this.nextInValueBucket = immutableMapEntry3;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.common.collect.ImmutableMapEntry
        @Nullable
        public ImmutableMapEntry<K, V> getNextInKeyBucket() {
            return this.nextInKeyBucket;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.common.collect.ImmutableMapEntry
        @Nullable
        public ImmutableMapEntry<K, V> getNextInValueBucket() {
            return this.nextInValueBucket;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public RegularImmutableBiMap(int i, ImmutableMapEntry.TerminalEntry<?, ?>[] terminalEntryArr) {
        int closedTableSize = Hashing.closedTableSize(i, 1.2d);
        this.mask = closedTableSize - 1;
        ImmutableMapEntry<K, V>[] createEntryArray = createEntryArray(closedTableSize);
        ImmutableMapEntry<K, V>[] createEntryArray2 = createEntryArray(closedTableSize);
        ImmutableMapEntry<K, V>[] createEntryArray3 = createEntryArray(i);
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            ImmutableMapEntry.TerminalEntry<?, ?> terminalEntry = terminalEntryArr[i3];
            Object key = terminalEntry.getKey();
            Object value = terminalEntry.getValue();
            int hashCode = key.hashCode();
            int hashCode2 = value.hashCode();
            int smear = Hashing.smear(hashCode) & this.mask;
            int smear2 = Hashing.smear(hashCode2) & this.mask;
            ImmutableMapEntry<K, V> immutableMapEntry = createEntryArray[smear];
            ImmutableMapEntry<K, V> immutableMapEntry2 = immutableMapEntry;
            while (true) {
                ImmutableMapEntry<K, V> immutableMapEntry3 = immutableMapEntry2;
                if (immutableMapEntry3 == null) {
                    break;
                }
                checkNoConflict(!key.equals(immutableMapEntry3.getKey()), "key", terminalEntry, immutableMapEntry3);
                immutableMapEntry2 = immutableMapEntry3.getNextInKeyBucket();
            }
            ImmutableMapEntry<K, V> immutableMapEntry4 = createEntryArray2[smear2];
            ImmutableMapEntry<K, V> immutableMapEntry5 = immutableMapEntry4;
            while (true) {
                ImmutableMapEntry<K, V> immutableMapEntry6 = immutableMapEntry5;
                if (immutableMapEntry6 == null) {
                    break;
                }
                checkNoConflict(!value.equals(immutableMapEntry6.getValue()), "value", terminalEntry, immutableMapEntry6);
                immutableMapEntry5 = immutableMapEntry6.getNextInValueBucket();
            }
            ImmutableMapEntry<K, V> nonTerminalBiMapEntry = (immutableMapEntry == null && immutableMapEntry4 == null) ? terminalEntry : new NonTerminalBiMapEntry<>(terminalEntry, immutableMapEntry, immutableMapEntry4);
            createEntryArray[smear] = nonTerminalBiMapEntry;
            createEntryArray2[smear2] = nonTerminalBiMapEntry;
            createEntryArray3[i3] = nonTerminalBiMapEntry;
            i2 += hashCode ^ hashCode2;
        }
        this.keyTable = createEntryArray;
        this.valueTable = createEntryArray2;
        this.entries = createEntryArray3;
        this.hashCode = i2;
    }

    private static <K, V> ImmutableMapEntry<K, V>[] createEntryArray(int i) {
        return new ImmutableMapEntry[i];
    }

    @Override // com.google.common.collect.ImmutableMap
    ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        return new ImmutableMapEntrySet<K, V>(this) { // from class: com.google.common.collect.RegularImmutableBiMap.1
            final RegularImmutableBiMap this$0;

            {
                this.this$0 = this;
            }

            @Override // com.google.common.collect.ImmutableCollection
            ImmutableList<Map.Entry<K, V>> createAsList() {
                return new RegularImmutableAsList(this, this.this$0.entries);
            }

            @Override // com.google.common.collect.ImmutableSet, java.util.Collection, java.util.Set
            public int hashCode() {
                return this.this$0.hashCode;
            }

            @Override // com.google.common.collect.ImmutableSet
            boolean isHashCodeFast() {
                return true;
            }

            @Override // com.google.common.collect.ImmutableSet, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
            public UnmodifiableIterator<Map.Entry<K, V>> iterator() {
                return asList().iterator();
            }

            @Override // com.google.common.collect.ImmutableMapEntrySet
            ImmutableMap<K, V> map() {
                return this.this$0;
            }
        };
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    @Nullable
    public V get(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        ImmutableMapEntry<K, V> immutableMapEntry = this.keyTable[Hashing.smear(obj.hashCode()) & this.mask];
        while (true) {
            ImmutableMapEntry<K, V> immutableMapEntry2 = immutableMapEntry;
            if (immutableMapEntry2 == null) {
                return null;
            }
            if (obj.equals(immutableMapEntry2.getKey())) {
                return immutableMapEntry2.getValue();
            }
            immutableMapEntry = immutableMapEntry2.getNextInKeyBucket();
        }
    }

    @Override // com.google.common.collect.ImmutableBiMap
    public ImmutableBiMap<V, K> inverse() {
        ImmutableBiMap<V, K> immutableBiMap = this.inverse;
        Inverse inverse = immutableBiMap;
        if (immutableBiMap == null) {
            inverse = new Inverse(this, null);
            this.inverse = inverse;
        }
        return inverse;
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
}
