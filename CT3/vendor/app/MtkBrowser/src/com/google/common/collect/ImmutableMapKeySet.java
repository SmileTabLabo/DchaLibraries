package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nullable;
@GwtCompatible(emulated = true)
/* loaded from: b.zip:com/google/common/collect/ImmutableMapKeySet.class */
final class ImmutableMapKeySet<K, V> extends ImmutableSet<K> {
    private final ImmutableMap<K, V> map;

    @GwtIncompatible("serialization")
    /* loaded from: b.zip:com/google/common/collect/ImmutableMapKeySet$KeySetSerializedForm.class */
    private static class KeySetSerializedForm<K> implements Serializable {
        private static final long serialVersionUID = 0;
        final ImmutableMap<K, ?> map;

        KeySetSerializedForm(ImmutableMap<K, ?> immutableMap) {
            this.map = immutableMap;
        }

        Object readResolve() {
            return this.map.keySet();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ImmutableMapKeySet(ImmutableMap<K, V> immutableMap) {
        this.map = immutableMap;
    }

    @Override // com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.util.Set
    public boolean contains(@Nullable Object obj) {
        return this.map.containsKey(obj);
    }

    @Override // com.google.common.collect.ImmutableCollection
    ImmutableList<K> createAsList() {
        return new ImmutableAsList<K>(this, this.map.entrySet().asList()) { // from class: com.google.common.collect.ImmutableMapKeySet.1
            final ImmutableMapKeySet this$0;
            final ImmutableList val$entryList;

            {
                this.this$0 = this;
                this.val$entryList = r5;
            }

            @Override // com.google.common.collect.ImmutableAsList
            ImmutableCollection<K> delegateCollection() {
                return this.this$0;
            }

            @Override // java.util.List
            public K get(int i) {
                return (K) ((Map.Entry) this.val$entryList.get(i)).getKey();
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableCollection
    public boolean isPartialView() {
        return true;
    }

    @Override // com.google.common.collect.ImmutableSet, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
    public UnmodifiableIterator<K> iterator() {
        return asList().iterator();
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
    public int size() {
        return this.map.size();
    }

    @Override // com.google.common.collect.ImmutableSet, com.google.common.collect.ImmutableCollection
    @GwtIncompatible("serialization")
    Object writeReplace() {
        return new KeySetSerializedForm(this.map);
    }
}
