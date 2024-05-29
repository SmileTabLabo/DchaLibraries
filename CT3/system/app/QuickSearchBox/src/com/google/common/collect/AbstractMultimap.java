package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtCompatible
/* loaded from: a.zip:com/google/common/collect/AbstractMultimap.class */
public abstract class AbstractMultimap<K, V> implements Multimap<K, V> {
    private transient Map<K, Collection<V>> asMap;
    private transient Collection<Map.Entry<K, V>> entries;
    private transient Set<K> keySet;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/google/common/collect/AbstractMultimap$Entries.class */
    public class Entries extends Multimaps.Entries<K, V> {
        final AbstractMultimap this$0;

        private Entries(AbstractMultimap abstractMultimap) {
            this.this$0 = abstractMultimap;
        }

        /* synthetic */ Entries(AbstractMultimap abstractMultimap, Entries entries) {
            this(abstractMultimap);
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.lang.Iterable
        public Iterator<Map.Entry<K, V>> iterator() {
            return this.this$0.entryIterator();
        }

        @Override // com.google.common.collect.Multimaps.Entries
        Multimap<K, V> multimap() {
            return this.this$0;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/google/common/collect/AbstractMultimap$EntrySet.class */
    public class EntrySet extends AbstractMultimap<K, V>.Entries implements Set<Map.Entry<K, V>> {
        final AbstractMultimap this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        private EntrySet(AbstractMultimap abstractMultimap) {
            super(abstractMultimap, null);
            this.this$0 = abstractMultimap;
        }

        /* synthetic */ EntrySet(AbstractMultimap abstractMultimap, EntrySet entrySet) {
            this(abstractMultimap);
        }

        @Override // java.util.Collection, java.util.Set
        public boolean equals(@Nullable Object obj) {
            return Sets.equalsImpl(this, obj);
        }

        @Override // java.util.Collection, java.util.Set
        public int hashCode() {
            return Sets.hashCodeImpl(this);
        }
    }

    AbstractMultimap() {
    }

    @Override // com.google.common.collect.Multimap
    public Map<K, Collection<V>> asMap() {
        Map<K, Collection<V>> map = this.asMap;
        Map<K, Collection<V>> map2 = map;
        if (map == null) {
            map2 = createAsMap();
            this.asMap = map2;
        }
        return map2;
    }

    @Override // com.google.common.collect.Multimap
    public boolean containsEntry(@Nullable Object obj, @Nullable Object obj2) {
        Collection<V> collection = asMap().get(obj);
        return collection != null ? collection.contains(obj2) : false;
    }

    abstract Map<K, Collection<V>> createAsMap();

    Collection<Map.Entry<K, V>> createEntries() {
        return this instanceof SetMultimap ? new EntrySet(this, null) : new Entries(this, null);
    }

    Set<K> createKeySet() {
        return new Maps.KeySet(asMap());
    }

    public Collection<Map.Entry<K, V>> entries() {
        Collection<Map.Entry<K, V>> collection = this.entries;
        Collection<Map.Entry<K, V>> collection2 = collection;
        if (collection == null) {
            collection2 = createEntries();
            this.entries = collection2;
        }
        return collection2;
    }

    abstract Iterator<Map.Entry<K, V>> entryIterator();

    public boolean equals(@Nullable Object obj) {
        return Multimaps.equalsImpl(this, obj);
    }

    public int hashCode() {
        return asMap().hashCode();
    }

    public Set<K> keySet() {
        Set<K> set = this.keySet;
        Set<K> set2 = set;
        if (set == null) {
            set2 = createKeySet();
            this.keySet = set2;
        }
        return set2;
    }

    @Override // com.google.common.collect.Multimap
    public boolean remove(@Nullable Object obj, @Nullable Object obj2) {
        Collection<V> collection = asMap().get(obj);
        return collection != null ? collection.remove(obj2) : false;
    }

    public String toString() {
        return asMap().toString();
    }
}
