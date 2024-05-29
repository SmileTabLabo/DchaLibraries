package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtCompatible(emulated = true)
/* loaded from: b.zip:com/google/common/collect/AbstractMapBasedMultimap.class */
public abstract class AbstractMapBasedMultimap<K, V> extends AbstractMultimap<K, V> implements Serializable {
    private static final long serialVersionUID = 2447537837011683357L;
    private transient Map<K, Collection<V>> map;
    private transient int totalSize;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/google/common/collect/AbstractMapBasedMultimap$AsMap.class */
    public class AsMap extends Maps.ImprovedAbstractMap<K, Collection<V>> {
        final transient Map<K, Collection<V>> submap;
        final AbstractMapBasedMultimap this$0;

        /* loaded from: b.zip:com/google/common/collect/AbstractMapBasedMultimap$AsMap$AsMapEntries.class */
        class AsMapEntries extends Maps.EntrySet<K, Collection<V>> {
            final AsMap this$1;

            AsMapEntries(AsMap asMap) {
                this.this$1 = asMap;
            }

            @Override // com.google.common.collect.Maps.EntrySet, java.util.AbstractCollection, java.util.Collection, java.util.Set
            public boolean contains(Object obj) {
                return Collections2.safeContains(this.this$1.submap.entrySet(), obj);
            }

            @Override // java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set
            public Iterator<Map.Entry<K, Collection<V>>> iterator() {
                return new AsMapIterator(this.this$1);
            }

            @Override // com.google.common.collect.Maps.EntrySet
            Map<K, Collection<V>> map() {
                return this.this$1;
            }

            @Override // com.google.common.collect.Maps.EntrySet, java.util.AbstractCollection, java.util.Collection, java.util.Set
            public boolean remove(Object obj) {
                if (contains(obj)) {
                    this.this$1.this$0.removeValuesForKey(((Map.Entry) obj).getKey());
                    return true;
                }
                return false;
            }
        }

        /* loaded from: b.zip:com/google/common/collect/AbstractMapBasedMultimap$AsMap$AsMapIterator.class */
        class AsMapIterator implements Iterator<Map.Entry<K, Collection<V>>> {
            Collection<V> collection;
            final Iterator<Map.Entry<K, Collection<V>>> delegateIterator;
            final AsMap this$1;

            AsMapIterator(AsMap asMap) {
                this.this$1 = asMap;
                this.delegateIterator = this.this$1.submap.entrySet().iterator();
            }

            @Override // java.util.Iterator
            public boolean hasNext() {
                return this.delegateIterator.hasNext();
            }

            @Override // java.util.Iterator
            public Map.Entry<K, Collection<V>> next() {
                Map.Entry<K, Collection<V>> next = this.delegateIterator.next();
                this.collection = next.getValue();
                return this.this$1.wrapEntry(next);
            }

            @Override // java.util.Iterator
            public void remove() {
                this.delegateIterator.remove();
                this.this$1.this$0.totalSize -= this.collection.size();
                this.collection.clear();
            }
        }

        AsMap(AbstractMapBasedMultimap abstractMapBasedMultimap, Map<K, Collection<V>> map) {
            this.this$0 = abstractMapBasedMultimap;
            this.submap = map;
        }

        @Override // java.util.AbstractMap, java.util.Map
        public void clear() {
            if (this.submap == this.this$0.map) {
                this.this$0.clear();
            } else {
                Iterators.clear(new AsMapIterator(this));
            }
        }

        @Override // java.util.AbstractMap, java.util.Map
        public boolean containsKey(Object obj) {
            return Maps.safeContainsKey(this.submap, obj);
        }

        @Override // com.google.common.collect.Maps.ImprovedAbstractMap
        protected Set<Map.Entry<K, Collection<V>>> createEntrySet() {
            return new AsMapEntries(this);
        }

        @Override // java.util.AbstractMap, java.util.Map
        public boolean equals(@Nullable Object obj) {
            return this != obj ? this.submap.equals(obj) : true;
        }

        @Override // java.util.AbstractMap, java.util.Map
        public Collection<V> get(Object obj) {
            Collection<V> collection = (Collection) Maps.safeGet(this.submap, obj);
            if (collection == null) {
                return null;
            }
            return this.this$0.wrapCollection(obj, collection);
        }

        @Override // java.util.AbstractMap, java.util.Map
        public int hashCode() {
            return this.submap.hashCode();
        }

        @Override // com.google.common.collect.Maps.ImprovedAbstractMap, java.util.AbstractMap, java.util.Map
        public Set<K> keySet() {
            return this.this$0.keySet();
        }

        @Override // java.util.AbstractMap, java.util.Map
        public Collection<V> remove(Object obj) {
            Collection<V> remove = this.submap.remove(obj);
            if (remove == null) {
                return null;
            }
            Collection<V> createCollection = this.this$0.createCollection();
            createCollection.addAll(remove);
            this.this$0.totalSize -= remove.size();
            remove.clear();
            return createCollection;
        }

        @Override // java.util.AbstractMap, java.util.Map
        public int size() {
            return this.submap.size();
        }

        @Override // java.util.AbstractMap
        public String toString() {
            return this.submap.toString();
        }

        Map.Entry<K, Collection<V>> wrapEntry(Map.Entry<K, Collection<V>> entry) {
            K key = entry.getKey();
            return Maps.immutableEntry(key, this.this$0.wrapCollection(key, entry.getValue()));
        }
    }

    /* loaded from: b.zip:com/google/common/collect/AbstractMapBasedMultimap$Itr.class */
    private abstract class Itr<T> implements Iterator<T> {
        final Iterator<Map.Entry<K, Collection<V>>> keyIterator;
        final AbstractMapBasedMultimap this$0;
        K key = null;
        Collection<V> collection = null;
        Iterator<V> valueIterator = Iterators.emptyModifiableIterator();

        Itr(AbstractMapBasedMultimap abstractMapBasedMultimap) {
            this.this$0 = abstractMapBasedMultimap;
            this.keyIterator = (Iterator<Map.Entry<K, V>>) abstractMapBasedMultimap.map.entrySet().iterator();
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return !this.keyIterator.hasNext() ? this.valueIterator.hasNext() : true;
        }

        @Override // java.util.Iterator
        public T next() {
            if (!this.valueIterator.hasNext()) {
                Map.Entry<K, Collection<V>> next = this.keyIterator.next();
                this.key = next.getKey();
                this.collection = next.getValue();
                this.valueIterator = this.collection.iterator();
            }
            return output(this.key, this.valueIterator.next());
        }

        abstract T output(K k, V v);

        @Override // java.util.Iterator
        public void remove() {
            this.valueIterator.remove();
            if (this.collection.isEmpty()) {
                this.keyIterator.remove();
            }
            this.this$0.totalSize--;
        }
    }

    /* loaded from: b.zip:com/google/common/collect/AbstractMapBasedMultimap$KeySet.class */
    private class KeySet extends Maps.KeySet<K, Collection<V>> {
        final AbstractMapBasedMultimap this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        KeySet(AbstractMapBasedMultimap abstractMapBasedMultimap, Map<K, Collection<V>> map) {
            super(map);
            this.this$0 = abstractMapBasedMultimap;
        }

        @Override // com.google.common.collect.Maps.KeySet, java.util.AbstractCollection, java.util.Collection, java.util.Set
        public void clear() {
            Iterators.clear(iterator());
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public boolean containsAll(Collection<?> collection) {
            return map().keySet().containsAll(collection);
        }

        @Override // java.util.AbstractSet, java.util.Collection, java.util.Set
        public boolean equals(@Nullable Object obj) {
            return this != obj ? map().keySet().equals(obj) : true;
        }

        @Override // java.util.AbstractSet, java.util.Collection, java.util.Set
        public int hashCode() {
            return map().keySet().hashCode();
        }

        @Override // com.google.common.collect.Maps.KeySet, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set
        public Iterator<K> iterator() {
            return new Iterator<K>(this, map().entrySet().iterator()) { // from class: com.google.common.collect.AbstractMapBasedMultimap.KeySet.1
                Map.Entry<K, Collection<V>> entry;
                final KeySet this$1;
                final Iterator val$entryIterator;

                {
                    this.this$1 = this;
                    this.val$entryIterator = r5;
                }

                @Override // java.util.Iterator
                public boolean hasNext() {
                    return this.val$entryIterator.hasNext();
                }

                @Override // java.util.Iterator
                public K next() {
                    this.entry = (Map.Entry) this.val$entryIterator.next();
                    return this.entry.getKey();
                }

                @Override // java.util.Iterator
                public void remove() {
                    CollectPreconditions.checkRemove(this.entry != null);
                    Collection<V> value = this.entry.getValue();
                    this.val$entryIterator.remove();
                    this.this$1.this$0.totalSize -= value.size();
                    value.clear();
                }
            };
        }

        @Override // com.google.common.collect.Maps.KeySet, java.util.AbstractCollection, java.util.Collection, java.util.Set
        public boolean remove(Object obj) {
            boolean z = false;
            int i = 0;
            Collection<V> remove = map().remove(obj);
            if (remove != null) {
                i = remove.size();
                remove.clear();
                this.this$0.totalSize -= i;
            }
            if (i > 0) {
                z = true;
            }
            return z;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/google/common/collect/AbstractMapBasedMultimap$RandomAccessWrappedList.class */
    public class RandomAccessWrappedList extends AbstractMapBasedMultimap<K, V>.WrappedList implements RandomAccess {
        final AbstractMapBasedMultimap this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        RandomAccessWrappedList(AbstractMapBasedMultimap abstractMapBasedMultimap, @Nullable K k, List<V> list, @Nullable AbstractMapBasedMultimap<K, V>.WrappedCollection wrappedCollection) {
            super(abstractMapBasedMultimap, k, list, wrappedCollection);
            this.this$0 = abstractMapBasedMultimap;
        }
    }

    /* loaded from: b.zip:com/google/common/collect/AbstractMapBasedMultimap$SortedAsMap.class */
    private class SortedAsMap extends AbstractMapBasedMultimap<K, V>.AsMap implements SortedMap<K, Collection<V>> {
        SortedSet<K> sortedKeySet;
        final AbstractMapBasedMultimap this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        SortedAsMap(AbstractMapBasedMultimap abstractMapBasedMultimap, SortedMap<K, Collection<V>> sortedMap) {
            super(abstractMapBasedMultimap, sortedMap);
            this.this$0 = abstractMapBasedMultimap;
        }

        @Override // java.util.SortedMap
        public Comparator<? super K> comparator() {
            return sortedMap().comparator();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.common.collect.Maps.ImprovedAbstractMap
        public SortedSet<K> createKeySet() {
            return new SortedKeySet(this.this$0, sortedMap());
        }

        @Override // java.util.SortedMap
        public K firstKey() {
            return sortedMap().firstKey();
        }

        @Override // java.util.SortedMap
        public SortedMap<K, Collection<V>> headMap(K k) {
            return new SortedAsMap(this.this$0, sortedMap().headMap(k));
        }

        @Override // com.google.common.collect.AbstractMapBasedMultimap.AsMap, com.google.common.collect.Maps.ImprovedAbstractMap, java.util.AbstractMap, java.util.Map
        public SortedSet<K> keySet() {
            SortedSet<K> sortedSet = this.sortedKeySet;
            SortedSet<K> sortedSet2 = sortedSet;
            if (sortedSet == null) {
                sortedSet2 = createKeySet();
                this.sortedKeySet = sortedSet2;
            }
            return sortedSet2;
        }

        @Override // java.util.SortedMap
        public K lastKey() {
            return sortedMap().lastKey();
        }

        SortedMap<K, Collection<V>> sortedMap() {
            return (SortedMap) this.submap;
        }

        @Override // java.util.SortedMap
        public SortedMap<K, Collection<V>> subMap(K k, K k2) {
            return new SortedAsMap(this.this$0, sortedMap().subMap(k, k2));
        }

        @Override // java.util.SortedMap
        public SortedMap<K, Collection<V>> tailMap(K k) {
            return new SortedAsMap(this.this$0, sortedMap().tailMap(k));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/google/common/collect/AbstractMapBasedMultimap$SortedKeySet.class */
    public class SortedKeySet extends AbstractMapBasedMultimap<K, V>.KeySet implements SortedSet<K> {
        final AbstractMapBasedMultimap this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        SortedKeySet(AbstractMapBasedMultimap abstractMapBasedMultimap, SortedMap<K, Collection<V>> sortedMap) {
            super(abstractMapBasedMultimap, sortedMap);
            this.this$0 = abstractMapBasedMultimap;
        }

        @Override // java.util.SortedSet
        public Comparator<? super K> comparator() {
            return sortedMap().comparator();
        }

        @Override // java.util.SortedSet
        public K first() {
            return sortedMap().firstKey();
        }

        @Override // java.util.SortedSet
        public SortedSet<K> headSet(K k) {
            return new SortedKeySet(this.this$0, sortedMap().headMap(k));
        }

        @Override // java.util.SortedSet
        public K last() {
            return sortedMap().lastKey();
        }

        SortedMap<K, Collection<V>> sortedMap() {
            return (SortedMap) super.map();
        }

        @Override // java.util.SortedSet
        public SortedSet<K> subSet(K k, K k2) {
            return new SortedKeySet(this.this$0, sortedMap().subMap(k, k2));
        }

        @Override // java.util.SortedSet
        public SortedSet<K> tailSet(K k) {
            return new SortedKeySet(this.this$0, sortedMap().tailMap(k));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/google/common/collect/AbstractMapBasedMultimap$WrappedCollection.class */
    public class WrappedCollection extends AbstractCollection<V> {
        final AbstractMapBasedMultimap<K, V>.WrappedCollection ancestor;
        final Collection<V> ancestorDelegate;
        Collection<V> delegate;
        final K key;
        final AbstractMapBasedMultimap this$0;

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: b.zip:com/google/common/collect/AbstractMapBasedMultimap$WrappedCollection$WrappedIterator.class */
        public class WrappedIterator implements Iterator<V> {
            final Iterator<V> delegateIterator;
            final Collection<V> originalDelegate;
            final WrappedCollection this$1;

            WrappedIterator(WrappedCollection wrappedCollection) {
                this.this$1 = wrappedCollection;
                this.originalDelegate = this.this$1.delegate;
                this.delegateIterator = wrappedCollection.this$0.iteratorOrListIterator(wrappedCollection.delegate);
            }

            WrappedIterator(WrappedCollection wrappedCollection, Iterator<V> it) {
                this.this$1 = wrappedCollection;
                this.originalDelegate = this.this$1.delegate;
                this.delegateIterator = it;
            }

            Iterator<V> getDelegateIterator() {
                validateIterator();
                return this.delegateIterator;
            }

            @Override // java.util.Iterator
            public boolean hasNext() {
                validateIterator();
                return this.delegateIterator.hasNext();
            }

            @Override // java.util.Iterator
            public V next() {
                validateIterator();
                return this.delegateIterator.next();
            }

            @Override // java.util.Iterator
            public void remove() {
                this.delegateIterator.remove();
                this.this$1.this$0.totalSize--;
                this.this$1.removeIfEmpty();
            }

            void validateIterator() {
                this.this$1.refreshIfEmpty();
                if (this.this$1.delegate != this.originalDelegate) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        WrappedCollection(AbstractMapBasedMultimap abstractMapBasedMultimap, @Nullable K k, Collection<V> collection, @Nullable AbstractMapBasedMultimap<K, V>.WrappedCollection wrappedCollection) {
            this.this$0 = abstractMapBasedMultimap;
            this.key = k;
            this.delegate = collection;
            this.ancestor = wrappedCollection;
            this.ancestorDelegate = wrappedCollection == null ? null : wrappedCollection.getDelegate();
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public boolean add(V v) {
            refreshIfEmpty();
            boolean isEmpty = this.delegate.isEmpty();
            boolean add = this.delegate.add(v);
            if (add) {
                this.this$0.totalSize++;
                if (isEmpty) {
                    addToMap();
                }
            }
            return add;
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public boolean addAll(Collection<? extends V> collection) {
            if (collection.isEmpty()) {
                return false;
            }
            int size = size();
            boolean addAll = this.delegate.addAll(collection);
            if (addAll) {
                int size2 = this.delegate.size();
                this.this$0.totalSize += size2 - size;
                if (size == 0) {
                    addToMap();
                }
            }
            return addAll;
        }

        void addToMap() {
            if (this.ancestor != null) {
                this.ancestor.addToMap();
            } else {
                this.this$0.map.put(this.key, this.delegate);
            }
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public void clear() {
            int size = size();
            if (size == 0) {
                return;
            }
            this.delegate.clear();
            this.this$0.totalSize -= size;
            removeIfEmpty();
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public boolean contains(Object obj) {
            refreshIfEmpty();
            return this.delegate.contains(obj);
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public boolean containsAll(Collection<?> collection) {
            refreshIfEmpty();
            return this.delegate.containsAll(collection);
        }

        @Override // java.util.Collection
        public boolean equals(@Nullable Object obj) {
            if (obj == this) {
                return true;
            }
            refreshIfEmpty();
            return this.delegate.equals(obj);
        }

        AbstractMapBasedMultimap<K, V>.WrappedCollection getAncestor() {
            return this.ancestor;
        }

        Collection<V> getDelegate() {
            return this.delegate;
        }

        K getKey() {
            return this.key;
        }

        @Override // java.util.Collection
        public int hashCode() {
            refreshIfEmpty();
            return this.delegate.hashCode();
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.lang.Iterable
        public Iterator<V> iterator() {
            refreshIfEmpty();
            return new WrappedIterator(this);
        }

        void refreshIfEmpty() {
            Collection<V> collection;
            if (this.ancestor != null) {
                this.ancestor.refreshIfEmpty();
                if (this.ancestor.getDelegate() != this.ancestorDelegate) {
                    throw new ConcurrentModificationException();
                }
            } else if (!this.delegate.isEmpty() || (collection = (Collection) this.this$0.map.get(this.key)) == null) {
            } else {
                this.delegate = collection;
            }
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public boolean remove(Object obj) {
            refreshIfEmpty();
            boolean remove = this.delegate.remove(obj);
            if (remove) {
                this.this$0.totalSize--;
                removeIfEmpty();
            }
            return remove;
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public boolean removeAll(Collection<?> collection) {
            if (collection.isEmpty()) {
                return false;
            }
            int size = size();
            boolean removeAll = this.delegate.removeAll(collection);
            if (removeAll) {
                int size2 = this.delegate.size();
                this.this$0.totalSize += size2 - size;
                removeIfEmpty();
            }
            return removeAll;
        }

        void removeIfEmpty() {
            if (this.ancestor != null) {
                this.ancestor.removeIfEmpty();
            } else if (this.delegate.isEmpty()) {
                this.this$0.map.remove(this.key);
            }
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public boolean retainAll(Collection<?> collection) {
            Preconditions.checkNotNull(collection);
            int size = size();
            boolean retainAll = this.delegate.retainAll(collection);
            if (retainAll) {
                int size2 = this.delegate.size();
                this.this$0.totalSize += size2 - size;
                removeIfEmpty();
            }
            return retainAll;
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public int size() {
            refreshIfEmpty();
            return this.delegate.size();
        }

        @Override // java.util.AbstractCollection
        public String toString() {
            refreshIfEmpty();
            return this.delegate.toString();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/google/common/collect/AbstractMapBasedMultimap$WrappedList.class */
    public class WrappedList extends AbstractMapBasedMultimap<K, V>.WrappedCollection implements List<V> {
        final AbstractMapBasedMultimap this$0;

        /* loaded from: b.zip:com/google/common/collect/AbstractMapBasedMultimap$WrappedList$WrappedListIterator.class */
        private class WrappedListIterator extends AbstractMapBasedMultimap<K, V>.WrappedCollection.WrappedIterator implements ListIterator<V> {
            final WrappedList this$1;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            WrappedListIterator(WrappedList wrappedList) {
                super(wrappedList);
                this.this$1 = wrappedList;
            }

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            public WrappedListIterator(WrappedList wrappedList, int i) {
                super(wrappedList, wrappedList.getListDelegate().listIterator(i));
                this.this$1 = wrappedList;
            }

            private ListIterator<V> getDelegateListIterator() {
                return (ListIterator) getDelegateIterator();
            }

            @Override // java.util.ListIterator
            public void add(V v) {
                boolean isEmpty = this.this$1.isEmpty();
                getDelegateListIterator().add(v);
                this.this$1.this$0.totalSize++;
                if (isEmpty) {
                    this.this$1.addToMap();
                }
            }

            @Override // java.util.ListIterator
            public boolean hasPrevious() {
                return getDelegateListIterator().hasPrevious();
            }

            @Override // java.util.ListIterator
            public int nextIndex() {
                return getDelegateListIterator().nextIndex();
            }

            @Override // java.util.ListIterator
            public V previous() {
                return getDelegateListIterator().previous();
            }

            @Override // java.util.ListIterator
            public int previousIndex() {
                return getDelegateListIterator().previousIndex();
            }

            @Override // java.util.ListIterator
            public void set(V v) {
                getDelegateListIterator().set(v);
            }
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        WrappedList(AbstractMapBasedMultimap abstractMapBasedMultimap, @Nullable K k, List<V> list, @Nullable AbstractMapBasedMultimap<K, V>.WrappedCollection wrappedCollection) {
            super(abstractMapBasedMultimap, k, list, wrappedCollection);
            this.this$0 = abstractMapBasedMultimap;
        }

        @Override // java.util.List
        public void add(int i, V v) {
            refreshIfEmpty();
            boolean isEmpty = getDelegate().isEmpty();
            getListDelegate().add(i, v);
            this.this$0.totalSize++;
            if (isEmpty) {
                addToMap();
            }
        }

        @Override // java.util.List
        public boolean addAll(int i, Collection<? extends V> collection) {
            if (collection.isEmpty()) {
                return false;
            }
            int size = size();
            boolean addAll = getListDelegate().addAll(i, collection);
            if (addAll) {
                int size2 = getDelegate().size();
                this.this$0.totalSize += size2 - size;
                if (size == 0) {
                    addToMap();
                }
            }
            return addAll;
        }

        @Override // java.util.List
        public V get(int i) {
            refreshIfEmpty();
            return getListDelegate().get(i);
        }

        List<V> getListDelegate() {
            return (List) getDelegate();
        }

        @Override // java.util.List
        public int indexOf(Object obj) {
            refreshIfEmpty();
            return getListDelegate().indexOf(obj);
        }

        @Override // java.util.List
        public int lastIndexOf(Object obj) {
            refreshIfEmpty();
            return getListDelegate().lastIndexOf(obj);
        }

        @Override // java.util.List
        public ListIterator<V> listIterator() {
            refreshIfEmpty();
            return new WrappedListIterator(this);
        }

        @Override // java.util.List
        public ListIterator<V> listIterator(int i) {
            refreshIfEmpty();
            return new WrappedListIterator(this, i);
        }

        @Override // java.util.List
        public V remove(int i) {
            refreshIfEmpty();
            V remove = getListDelegate().remove(i);
            this.this$0.totalSize--;
            removeIfEmpty();
            return remove;
        }

        @Override // java.util.List
        public V set(int i, V v) {
            refreshIfEmpty();
            return getListDelegate().set(i, v);
        }

        @Override // java.util.List
        public List<V> subList(int i, int i2) {
            refreshIfEmpty();
            return this.this$0.wrapList(getKey(), getListDelegate().subList(i, i2), getAncestor() == null ? this : getAncestor());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/google/common/collect/AbstractMapBasedMultimap$WrappedSet.class */
    public class WrappedSet extends AbstractMapBasedMultimap<K, V>.WrappedCollection implements Set<V> {
        final AbstractMapBasedMultimap this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        WrappedSet(AbstractMapBasedMultimap abstractMapBasedMultimap, @Nullable K k, Set<V> set) {
            super(abstractMapBasedMultimap, k, set, null);
            this.this$0 = abstractMapBasedMultimap;
        }

        @Override // com.google.common.collect.AbstractMapBasedMultimap.WrappedCollection, java.util.AbstractCollection, java.util.Collection
        public boolean removeAll(Collection<?> collection) {
            if (collection.isEmpty()) {
                return false;
            }
            int size = size();
            boolean removeAllImpl = Sets.removeAllImpl((Set) this.delegate, collection);
            if (removeAllImpl) {
                int size2 = this.delegate.size();
                this.this$0.totalSize += size2 - size;
                removeIfEmpty();
            }
            return removeAllImpl;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/google/common/collect/AbstractMapBasedMultimap$WrappedSortedSet.class */
    public class WrappedSortedSet extends AbstractMapBasedMultimap<K, V>.WrappedCollection implements SortedSet<V> {
        final AbstractMapBasedMultimap this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        WrappedSortedSet(AbstractMapBasedMultimap abstractMapBasedMultimap, @Nullable K k, SortedSet<V> sortedSet, @Nullable AbstractMapBasedMultimap<K, V>.WrappedCollection wrappedCollection) {
            super(abstractMapBasedMultimap, k, sortedSet, wrappedCollection);
            this.this$0 = abstractMapBasedMultimap;
        }

        @Override // java.util.SortedSet
        public Comparator<? super V> comparator() {
            return getSortedSetDelegate().comparator();
        }

        @Override // java.util.SortedSet
        public V first() {
            refreshIfEmpty();
            return getSortedSetDelegate().first();
        }

        SortedSet<V> getSortedSetDelegate() {
            return (SortedSet) getDelegate();
        }

        @Override // java.util.SortedSet
        public SortedSet<V> headSet(V v) {
            refreshIfEmpty();
            return new WrappedSortedSet(this.this$0, getKey(), getSortedSetDelegate().headSet(v), getAncestor() == null ? this : getAncestor());
        }

        @Override // java.util.SortedSet
        public V last() {
            refreshIfEmpty();
            return getSortedSetDelegate().last();
        }

        @Override // java.util.SortedSet
        public SortedSet<V> subSet(V v, V v2) {
            refreshIfEmpty();
            return new WrappedSortedSet(this.this$0, getKey(), getSortedSetDelegate().subSet(v, v2), getAncestor() == null ? this : getAncestor());
        }

        @Override // java.util.SortedSet
        public SortedSet<V> tailSet(V v) {
            refreshIfEmpty();
            return new WrappedSortedSet(this.this$0, getKey(), getSortedSetDelegate().tailSet(v), getAncestor() == null ? this : getAncestor());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Iterator<V> iteratorOrListIterator(Collection<V> collection) {
        return collection instanceof List ? ((List) collection).listIterator() : collection.iterator();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int removeValuesForKey(Object obj) {
        Collection collection = (Collection) Maps.safeRemove(this.map, obj);
        int i = 0;
        if (collection != null) {
            i = collection.size();
            collection.clear();
            this.totalSize -= i;
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public List<V> wrapList(@Nullable K k, List<V> list, @Nullable AbstractMapBasedMultimap<K, V>.WrappedCollection wrappedCollection) {
        return list instanceof RandomAccess ? new RandomAccessWrappedList(this, k, list, wrappedCollection) : new WrappedList(this, k, list, wrappedCollection);
    }

    @Override // com.google.common.collect.Multimap
    public void clear() {
        Iterator<T> it = this.map.values().iterator();
        while (it.hasNext()) {
            ((Collection) it.next()).clear();
        }
        this.map.clear();
        this.totalSize = 0;
    }

    @Override // com.google.common.collect.AbstractMultimap
    Map<K, Collection<V>> createAsMap() {
        return this.map instanceof SortedMap ? new SortedAsMap(this, (SortedMap) this.map) : new AsMap(this, this.map);
    }

    abstract Collection<V> createCollection();

    @Override // com.google.common.collect.AbstractMultimap
    Set<K> createKeySet() {
        return this.map instanceof SortedMap ? new SortedKeySet(this, (SortedMap) this.map) : new KeySet(this, this.map);
    }

    @Override // com.google.common.collect.AbstractMultimap
    public Collection<Map.Entry<K, V>> entries() {
        return super.entries();
    }

    @Override // com.google.common.collect.AbstractMultimap
    Iterator<Map.Entry<K, V>> entryIterator() {
        return new AbstractMapBasedMultimap<K, V>.Itr<Map.Entry<K, V>>(this, this) { // from class: com.google.common.collect.AbstractMapBasedMultimap.2
            final AbstractMapBasedMultimap this$0;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(this);
                this.this$0 = this;
            }

            @Override // com.google.common.collect.AbstractMapBasedMultimap.Itr
            /* bridge */ /* synthetic */ Object output(Object obj, Object obj2) {
                return output((AnonymousClass2) obj, obj2);
            }

            @Override // com.google.common.collect.AbstractMapBasedMultimap.Itr
            Map.Entry<K, V> output(K k, V v) {
                return Maps.immutableEntry(k, v);
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void setMap(Map<K, Collection<V>> map) {
        this.map = map;
        this.totalSize = 0;
        Iterator<T> it = map.values().iterator();
        while (it.hasNext()) {
            Collection collection = (Collection) it.next();
            Preconditions.checkArgument(!collection.isEmpty());
            this.totalSize += collection.size();
        }
    }

    @Override // com.google.common.collect.Multimap
    public int size() {
        return this.totalSize;
    }

    Collection<V> wrapCollection(@Nullable K k, Collection<V> collection) {
        return collection instanceof SortedSet ? new WrappedSortedSet(this, k, (SortedSet) collection, null) : collection instanceof Set ? new WrappedSet(this, k, (Set) collection) : collection instanceof List ? wrapList(k, (List) collection, null) : new WrappedCollection(this, k, collection, null);
    }
}
