package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ImmutableMapEntry;
import java.util.Map;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtCompatible(emulated = true, serializable = true)
/* loaded from: b.zip:com/google/common/collect/RegularImmutableMap.class */
public final class RegularImmutableMap<K, V> extends ImmutableMap<K, V> {
    private static final long serialVersionUID = 0;
    private final transient ImmutableMapEntry<K, V>[] entries;
    private final transient int mask;
    private final transient ImmutableMapEntry<K, V>[] table;

    /* loaded from: b.zip:com/google/common/collect/RegularImmutableMap$EntrySet.class */
    private class EntrySet extends ImmutableMapEntrySet<K, V> {
        final RegularImmutableMap this$0;

        private EntrySet(RegularImmutableMap regularImmutableMap) {
            this.this$0 = regularImmutableMap;
        }

        /* synthetic */ EntrySet(RegularImmutableMap regularImmutableMap, EntrySet entrySet) {
            this(regularImmutableMap);
        }

        @Override // com.google.common.collect.ImmutableCollection
        ImmutableList<Map.Entry<K, V>> createAsList() {
            return new RegularImmutableAsList(this, this.this$0.entries);
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

    /* loaded from: b.zip:com/google/common/collect/RegularImmutableMap$NonTerminalMapEntry.class */
    private static final class NonTerminalMapEntry<K, V> extends ImmutableMapEntry<K, V> {
        private final ImmutableMapEntry<K, V> nextInKeyBucket;

        NonTerminalMapEntry(ImmutableMapEntry<K, V> immutableMapEntry, ImmutableMapEntry<K, V> immutableMapEntry2) {
            super(immutableMapEntry);
            this.nextInKeyBucket = immutableMapEntry2;
        }

        NonTerminalMapEntry(K k, V v, ImmutableMapEntry<K, V> immutableMapEntry) {
            super(k, v);
            this.nextInKeyBucket = immutableMapEntry;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.common.collect.ImmutableMapEntry
        public ImmutableMapEntry<K, V> getNextInKeyBucket() {
            return this.nextInKeyBucket;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.common.collect.ImmutableMapEntry
        @Nullable
        public ImmutableMapEntry<K, V> getNextInValueBucket() {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v25, types: [com.google.common.collect.RegularImmutableMap$NonTerminalMapEntry] */
    /* JADX WARN: Type inference failed for: r5v0, types: [com.google.common.collect.RegularImmutableMap, com.google.common.collect.RegularImmutableMap<K, V>] */
    public RegularImmutableMap(int i, ImmutableMapEntry.TerminalEntry<?, ?>[] terminalEntryArr) {
        this.entries = createEntryArray(i);
        int closedTableSize = Hashing.closedTableSize(i, 1.2d);
        this.table = createEntryArray(closedTableSize);
        this.mask = closedTableSize - 1;
        for (int i2 = 0; i2 < i; i2++) {
            ImmutableMapEntry.TerminalEntry<?, ?> terminalEntry = terminalEntryArr[i2];
            Object key = terminalEntry.getKey();
            int smear = Hashing.smear(key.hashCode()) & this.mask;
            ImmutableMapEntry<K, V> immutableMapEntry = this.table[smear];
            if (immutableMapEntry != null) {
                terminalEntry = new NonTerminalMapEntry(terminalEntry, immutableMapEntry);
            }
            this.table[smear] = terminalEntry;
            this.entries[i2] = terminalEntry;
            checkNoConflictInBucket(key, terminalEntry, immutableMapEntry);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Multi-variable type inference failed */
    public RegularImmutableMap(Map.Entry<?, ?>[] entryArr) {
        int length = entryArr.length;
        this.entries = createEntryArray(length);
        int closedTableSize = Hashing.closedTableSize(length, 1.2d);
        this.table = createEntryArray(closedTableSize);
        this.mask = closedTableSize - 1;
        for (int i = 0; i < length; i++) {
            Map.Entry<?, ?> entry = entryArr[i];
            Object key = entry.getKey();
            Object value = entry.getValue();
            CollectPreconditions.checkEntryNotNull(key, value);
            int smear = Hashing.smear(key.hashCode()) & this.mask;
            ImmutableMapEntry<K, V> immutableMapEntry = this.table[smear];
            ImmutableMapEntry<K, V> terminalEntry = immutableMapEntry == null ? new ImmutableMapEntry.TerminalEntry<>(key, value) : new NonTerminalMapEntry<>(key, value, immutableMapEntry);
            this.table[smear] = terminalEntry;
            this.entries[i] = terminalEntry;
            checkNoConflictInBucket(key, terminalEntry, immutableMapEntry);
        }
    }

    private void checkNoConflictInBucket(K k, ImmutableMapEntry<K, V> immutableMapEntry, ImmutableMapEntry<K, V> immutableMapEntry2) {
        while (immutableMapEntry2 != null) {
            checkNoConflict(!k.equals(immutableMapEntry2.getKey()), "key", immutableMapEntry, immutableMapEntry2);
            immutableMapEntry2 = immutableMapEntry2.getNextInKeyBucket();
        }
    }

    private ImmutableMapEntry<K, V>[] createEntryArray(int i) {
        return new ImmutableMapEntry[i];
    }

    @Override // com.google.common.collect.ImmutableMap
    ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        return new EntrySet(this, null);
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public V get(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        ImmutableMapEntry<K, V> immutableMapEntry = this.table[Hashing.smear(obj.hashCode()) & this.mask];
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

    @Override // com.google.common.collect.ImmutableMap
    boolean isPartialView() {
        return false;
    }

    @Override // java.util.Map
    public int size() {
        return this.entries.length;
    }
}
