package com.google.common.collect;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class ImmutableMapEntry<K, V> extends ImmutableEntry<K, V> {
    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract ImmutableMapEntry<K, V> getNextInKeyBucket();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract ImmutableMapEntry<K, V> getNextInValueBucket();

    /* JADX INFO: Access modifiers changed from: package-private */
    public ImmutableMapEntry(K k, V v) {
        super(k, v);
        CollectPreconditions.checkEntryNotNull(k, v);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ImmutableMapEntry(ImmutableMapEntry<K, V> immutableMapEntry) {
        super(immutableMapEntry.getKey(), immutableMapEntry.getValue());
    }

    /* loaded from: classes.dex */
    static final class TerminalEntry<K, V> extends ImmutableMapEntry<K, V> {
        /* JADX INFO: Access modifiers changed from: package-private */
        public TerminalEntry(K k, V v) {
            super(k, v);
        }

        @Override // com.google.common.collect.ImmutableMapEntry
        ImmutableMapEntry<K, V> getNextInKeyBucket() {
            return null;
        }

        @Override // com.google.common.collect.ImmutableMapEntry
        ImmutableMapEntry<K, V> getNextInValueBucket() {
            return null;
        }
    }
}
