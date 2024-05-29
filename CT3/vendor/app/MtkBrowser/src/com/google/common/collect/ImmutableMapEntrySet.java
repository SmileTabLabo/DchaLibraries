package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtCompatible(emulated = true)
/* loaded from: b.zip:com/google/common/collect/ImmutableMapEntrySet.class */
public abstract class ImmutableMapEntrySet<K, V> extends ImmutableSet<Map.Entry<K, V>> {

    @GwtIncompatible("serialization")
    /* loaded from: b.zip:com/google/common/collect/ImmutableMapEntrySet$EntrySetSerializedForm.class */
    private static class EntrySetSerializedForm<K, V> implements Serializable {
        private static final long serialVersionUID = 0;
        final ImmutableMap<K, V> map;

        EntrySetSerializedForm(ImmutableMap<K, V> immutableMap) {
            this.map = immutableMap;
        }

        Object readResolve() {
            return this.map.entrySet();
        }
    }

    @Override // com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.util.Set
    public boolean contains(@Nullable Object obj) {
        boolean z = false;
        if (obj instanceof Map.Entry) {
            Map.Entry entry = (Map.Entry) obj;
            V v = map().get(entry.getKey());
            if (v != null) {
                z = v.equals(entry.getValue());
            }
            return z;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableCollection
    public boolean isPartialView() {
        return map().isPartialView();
    }

    abstract ImmutableMap<K, V> map();

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
    public int size() {
        return map().size();
    }

    @Override // com.google.common.collect.ImmutableSet, com.google.common.collect.ImmutableCollection
    @GwtIncompatible("serialization")
    Object writeReplace() {
        return new EntrySetSerializedForm(map());
    }
}
