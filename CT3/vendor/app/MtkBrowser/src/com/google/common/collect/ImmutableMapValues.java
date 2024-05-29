package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtCompatible(emulated = true)
/* loaded from: b.zip:com/google/common/collect/ImmutableMapValues.class */
public final class ImmutableMapValues<K, V> extends ImmutableCollection<V> {
    private final ImmutableMap<K, V> map;

    @GwtIncompatible("serialization")
    /* loaded from: b.zip:com/google/common/collect/ImmutableMapValues$SerializedForm.class */
    private static class SerializedForm<V> implements Serializable {
        private static final long serialVersionUID = 0;
        final ImmutableMap<?, V> map;

        SerializedForm(ImmutableMap<?, V> immutableMap) {
            this.map = immutableMap;
        }

        Object readResolve() {
            return this.map.values();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ImmutableMapValues(ImmutableMap<K, V> immutableMap) {
        this.map = immutableMap;
    }

    @Override // com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.util.Set
    public boolean contains(@Nullable Object obj) {
        return obj != null ? Iterators.contains(iterator(), obj) : false;
    }

    @Override // com.google.common.collect.ImmutableCollection
    ImmutableList<V> createAsList() {
        return new ImmutableAsList<V>(this, this.map.entrySet().asList()) { // from class: com.google.common.collect.ImmutableMapValues.1
            final ImmutableMapValues this$0;
            final ImmutableList val$entryList;

            {
                this.this$0 = this;
                this.val$entryList = r5;
            }

            @Override // com.google.common.collect.ImmutableAsList
            ImmutableCollection<V> delegateCollection() {
                return this.this$0;
            }

            @Override // java.util.List
            public V get(int i) {
                return (V) ((Map.Entry) this.val$entryList.get(i)).getValue();
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableCollection
    public boolean isPartialView() {
        return true;
    }

    @Override // com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
    public UnmodifiableIterator<V> iterator() {
        return Maps.valueIterator((UnmodifiableIterator) this.map.entrySet().iterator());
    }

    @Override // java.util.AbstractCollection, java.util.Collection
    public int size() {
        return this.map.size();
    }

    @Override // com.google.common.collect.ImmutableCollection
    @GwtIncompatible("serialization")
    Object writeReplace() {
        return new SerializedForm(this.map);
    }
}
