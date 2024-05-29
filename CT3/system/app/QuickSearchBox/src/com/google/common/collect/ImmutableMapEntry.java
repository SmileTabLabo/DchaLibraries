package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtIncompatible("unnecessary")
/* loaded from: a.zip:com/google/common/collect/ImmutableMapEntry.class */
public abstract class ImmutableMapEntry<K, V> extends ImmutableEntry<K, V> {

    /* loaded from: a.zip:com/google/common/collect/ImmutableMapEntry$TerminalEntry.class */
    static final class TerminalEntry<K, V> extends ImmutableMapEntry<K, V> {
        /* JADX INFO: Access modifiers changed from: package-private */
        public TerminalEntry(K k, V v) {
            super(k, v);
        }

        @Override // com.google.common.collect.ImmutableMapEntry
        @Nullable
        ImmutableMapEntry<K, V> getNextInKeyBucket() {
            return null;
        }

        @Override // com.google.common.collect.ImmutableMapEntry
        @Nullable
        ImmutableMapEntry<K, V> getNextInValueBucket() {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ImmutableMapEntry(ImmutableMapEntry<K, V> immutableMapEntry) {
        super(immutableMapEntry.getKey(), immutableMapEntry.getValue());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ImmutableMapEntry(K k, V v) {
        super(k, v);
        CollectPreconditions.checkEntryNotNull(k, v);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Nullable
    public abstract ImmutableMapEntry<K, V> getNextInKeyBucket();

    /* JADX INFO: Access modifiers changed from: package-private */
    @Nullable
    public abstract ImmutableMapEntry<K, V> getNextInValueBucket();
}
