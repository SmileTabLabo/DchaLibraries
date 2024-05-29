package android.support.v4.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v4/util/MapCollections.class */
public abstract class MapCollections<K, V> {
    MapCollections<K, V>.EntrySet mEntrySet;
    MapCollections<K, V>.KeySet mKeySet;
    MapCollections<K, V>.ValuesCollection mValues;

    /* loaded from: a.zip:android/support/v4/util/MapCollections$ArrayIterator.class */
    final class ArrayIterator<T> implements Iterator<T> {
        boolean mCanRemove = false;
        int mIndex;
        final int mOffset;
        int mSize;
        final MapCollections this$0;

        ArrayIterator(MapCollections mapCollections, int i) {
            this.this$0 = mapCollections;
            this.mOffset = i;
            this.mSize = mapCollections.colGetSize();
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.mIndex < this.mSize;
        }

        @Override // java.util.Iterator
        public T next() {
            T t = (T) this.this$0.colGetEntry(this.mIndex, this.mOffset);
            this.mIndex++;
            this.mCanRemove = true;
            return t;
        }

        @Override // java.util.Iterator
        public void remove() {
            if (!this.mCanRemove) {
                throw new IllegalStateException();
            }
            this.mIndex--;
            this.mSize--;
            this.mCanRemove = false;
            this.this$0.colRemoveAt(this.mIndex);
        }
    }

    /* loaded from: a.zip:android/support/v4/util/MapCollections$EntrySet.class */
    final class EntrySet implements Set<Map.Entry<K, V>> {
        final MapCollections this$0;

        EntrySet(MapCollections mapCollections) {
            this.this$0 = mapCollections;
        }

        @Override // java.util.Set, java.util.Collection
        public /* bridge */ /* synthetic */ boolean add(Object obj) {
            return add((Map.Entry) ((Map.Entry) obj));
        }

        public boolean add(Map.Entry<K, V> entry) {
            throw new UnsupportedOperationException();
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // java.util.Set, java.util.Collection
        public boolean addAll(Collection<? extends Map.Entry<K, V>> collection) {
            int colGetSize = this.this$0.colGetSize();
            Iterator<T> it = collection.iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                this.this$0.colPut(entry.getKey(), entry.getValue());
            }
            return colGetSize != this.this$0.colGetSize();
        }

        @Override // java.util.Set, java.util.Collection
        public void clear() {
            this.this$0.colClear();
        }

        @Override // java.util.Set, java.util.Collection
        public boolean contains(Object obj) {
            if (obj instanceof Map.Entry) {
                Map.Entry entry = (Map.Entry) obj;
                int colIndexOfKey = this.this$0.colIndexOfKey(entry.getKey());
                if (colIndexOfKey < 0) {
                    return false;
                }
                return ContainerHelpers.equal(this.this$0.colGetEntry(colIndexOfKey, 1), entry.getValue());
            }
            return false;
        }

        @Override // java.util.Set, java.util.Collection
        public boolean containsAll(Collection<?> collection) {
            Iterator<?> it = collection.iterator();
            while (it.hasNext()) {
                if (!contains(it.next())) {
                    return false;
                }
            }
            return true;
        }

        @Override // java.util.Set, java.util.Collection
        public boolean equals(Object obj) {
            return MapCollections.equalsSetHelper(this, obj);
        }

        @Override // java.util.Set, java.util.Collection
        public int hashCode() {
            int i = 0;
            for (int colGetSize = this.this$0.colGetSize() - 1; colGetSize >= 0; colGetSize--) {
                Object colGetEntry = this.this$0.colGetEntry(colGetSize, 0);
                Object colGetEntry2 = this.this$0.colGetEntry(colGetSize, 1);
                i += (colGetEntry2 == null ? 0 : colGetEntry2.hashCode()) ^ (colGetEntry == null ? 0 : colGetEntry.hashCode());
            }
            return i;
        }

        @Override // java.util.Set, java.util.Collection
        public boolean isEmpty() {
            boolean z = false;
            if (this.this$0.colGetSize() == 0) {
                z = true;
            }
            return z;
        }

        @Override // java.util.Set, java.util.Collection, java.lang.Iterable
        public Iterator<Map.Entry<K, V>> iterator() {
            return new MapIterator(this.this$0);
        }

        @Override // java.util.Set, java.util.Collection
        public boolean remove(Object obj) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Set, java.util.Collection
        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Set, java.util.Collection
        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Set, java.util.Collection
        public int size() {
            return this.this$0.colGetSize();
        }

        @Override // java.util.Set, java.util.Collection
        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Set, java.util.Collection
        public <T> T[] toArray(T[] tArr) {
            throw new UnsupportedOperationException();
        }
    }

    /* loaded from: a.zip:android/support/v4/util/MapCollections$KeySet.class */
    final class KeySet implements Set<K> {
        final MapCollections this$0;

        KeySet(MapCollections mapCollections) {
            this.this$0 = mapCollections;
        }

        @Override // java.util.Set, java.util.Collection
        public boolean add(K k) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Set, java.util.Collection
        public boolean addAll(Collection<? extends K> collection) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Set, java.util.Collection
        public void clear() {
            this.this$0.colClear();
        }

        @Override // java.util.Set, java.util.Collection
        public boolean contains(Object obj) {
            boolean z = false;
            if (this.this$0.colIndexOfKey(obj) >= 0) {
                z = true;
            }
            return z;
        }

        @Override // java.util.Set, java.util.Collection
        public boolean containsAll(Collection<?> collection) {
            return MapCollections.containsAllHelper(this.this$0.colGetMap(), collection);
        }

        @Override // java.util.Set, java.util.Collection
        public boolean equals(Object obj) {
            return MapCollections.equalsSetHelper(this, obj);
        }

        @Override // java.util.Set, java.util.Collection
        public int hashCode() {
            int i = 0;
            for (int colGetSize = this.this$0.colGetSize() - 1; colGetSize >= 0; colGetSize--) {
                Object colGetEntry = this.this$0.colGetEntry(colGetSize, 0);
                i += colGetEntry == null ? 0 : colGetEntry.hashCode();
            }
            return i;
        }

        @Override // java.util.Set, java.util.Collection
        public boolean isEmpty() {
            boolean z = false;
            if (this.this$0.colGetSize() == 0) {
                z = true;
            }
            return z;
        }

        @Override // java.util.Set, java.util.Collection, java.lang.Iterable
        public Iterator<K> iterator() {
            return new ArrayIterator(this.this$0, 0);
        }

        @Override // java.util.Set, java.util.Collection
        public boolean remove(Object obj) {
            int colIndexOfKey = this.this$0.colIndexOfKey(obj);
            if (colIndexOfKey >= 0) {
                this.this$0.colRemoveAt(colIndexOfKey);
                return true;
            }
            return false;
        }

        @Override // java.util.Set, java.util.Collection
        public boolean removeAll(Collection<?> collection) {
            return MapCollections.removeAllHelper(this.this$0.colGetMap(), collection);
        }

        @Override // java.util.Set, java.util.Collection
        public boolean retainAll(Collection<?> collection) {
            return MapCollections.retainAllHelper(this.this$0.colGetMap(), collection);
        }

        @Override // java.util.Set, java.util.Collection
        public int size() {
            return this.this$0.colGetSize();
        }

        @Override // java.util.Set, java.util.Collection
        public Object[] toArray() {
            return this.this$0.toArrayHelper(0);
        }

        @Override // java.util.Set, java.util.Collection
        public <T> T[] toArray(T[] tArr) {
            return (T[]) this.this$0.toArrayHelper(tArr, 0);
        }
    }

    /* loaded from: a.zip:android/support/v4/util/MapCollections$MapIterator.class */
    final class MapIterator implements Iterator<Map.Entry<K, V>>, Map.Entry<K, V> {
        int mEnd;
        boolean mEntryValid = false;
        int mIndex = -1;
        final MapCollections this$0;

        MapIterator(MapCollections mapCollections) {
            this.this$0 = mapCollections;
            this.mEnd = mapCollections.colGetSize() - 1;
        }

        @Override // java.util.Map.Entry
        public final boolean equals(Object obj) {
            boolean z = false;
            if (this.mEntryValid) {
                if (obj instanceof Map.Entry) {
                    Map.Entry entry = (Map.Entry) obj;
                    if (ContainerHelpers.equal(entry.getKey(), this.this$0.colGetEntry(this.mIndex, 0))) {
                        z = ContainerHelpers.equal(entry.getValue(), this.this$0.colGetEntry(this.mIndex, 1));
                    }
                    return z;
                }
                return false;
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        @Override // java.util.Map.Entry
        public K getKey() {
            if (this.mEntryValid) {
                return (K) this.this$0.colGetEntry(this.mIndex, 0);
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        @Override // java.util.Map.Entry
        public V getValue() {
            if (this.mEntryValid) {
                return (V) this.this$0.colGetEntry(this.mIndex, 1);
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.mIndex < this.mEnd;
        }

        @Override // java.util.Map.Entry
        public final int hashCode() {
            int i = 0;
            if (this.mEntryValid) {
                Object colGetEntry = this.this$0.colGetEntry(this.mIndex, 0);
                Object colGetEntry2 = this.this$0.colGetEntry(this.mIndex, 1);
                int hashCode = colGetEntry == null ? 0 : colGetEntry.hashCode();
                if (colGetEntry2 != null) {
                    i = colGetEntry2.hashCode();
                }
                return i ^ hashCode;
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        @Override // java.util.Iterator
        public Map.Entry<K, V> next() {
            this.mIndex++;
            this.mEntryValid = true;
            return this;
        }

        @Override // java.util.Iterator
        public void remove() {
            if (!this.mEntryValid) {
                throw new IllegalStateException();
            }
            this.this$0.colRemoveAt(this.mIndex);
            this.mIndex--;
            this.mEnd--;
            this.mEntryValid = false;
        }

        @Override // java.util.Map.Entry
        public V setValue(V v) {
            if (this.mEntryValid) {
                return (V) this.this$0.colSetValue(this.mIndex, v);
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        public final String toString() {
            return getKey() + "=" + getValue();
        }
    }

    /* loaded from: a.zip:android/support/v4/util/MapCollections$ValuesCollection.class */
    final class ValuesCollection implements Collection<V> {
        final MapCollections this$0;

        ValuesCollection(MapCollections mapCollections) {
            this.this$0 = mapCollections;
        }

        @Override // java.util.Collection
        public boolean add(V v) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Collection
        public boolean addAll(Collection<? extends V> collection) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Collection
        public void clear() {
            this.this$0.colClear();
        }

        @Override // java.util.Collection
        public boolean contains(Object obj) {
            boolean z = false;
            if (this.this$0.colIndexOfValue(obj) >= 0) {
                z = true;
            }
            return z;
        }

        @Override // java.util.Collection
        public boolean containsAll(Collection<?> collection) {
            Iterator<?> it = collection.iterator();
            while (it.hasNext()) {
                if (!contains(it.next())) {
                    return false;
                }
            }
            return true;
        }

        @Override // java.util.Collection
        public boolean isEmpty() {
            boolean z = false;
            if (this.this$0.colGetSize() == 0) {
                z = true;
            }
            return z;
        }

        @Override // java.util.Collection, java.lang.Iterable
        public Iterator<V> iterator() {
            return new ArrayIterator(this.this$0, 1);
        }

        @Override // java.util.Collection
        public boolean remove(Object obj) {
            int colIndexOfValue = this.this$0.colIndexOfValue(obj);
            if (colIndexOfValue >= 0) {
                this.this$0.colRemoveAt(colIndexOfValue);
                return true;
            }
            return false;
        }

        @Override // java.util.Collection
        public boolean removeAll(Collection<?> collection) {
            int colGetSize = this.this$0.colGetSize();
            boolean z = false;
            int i = 0;
            while (i < colGetSize) {
                int i2 = colGetSize;
                int i3 = i;
                if (collection.contains(this.this$0.colGetEntry(i, 1))) {
                    this.this$0.colRemoveAt(i);
                    i3 = i - 1;
                    i2 = colGetSize - 1;
                    z = true;
                }
                i = i3 + 1;
                colGetSize = i2;
            }
            return z;
        }

        @Override // java.util.Collection
        public boolean retainAll(Collection<?> collection) {
            int colGetSize = this.this$0.colGetSize();
            boolean z = false;
            int i = 0;
            while (i < colGetSize) {
                int i2 = colGetSize;
                int i3 = i;
                if (!collection.contains(this.this$0.colGetEntry(i, 1))) {
                    this.this$0.colRemoveAt(i);
                    i3 = i - 1;
                    i2 = colGetSize - 1;
                    z = true;
                }
                i = i3 + 1;
                colGetSize = i2;
            }
            return z;
        }

        @Override // java.util.Collection
        public int size() {
            return this.this$0.colGetSize();
        }

        @Override // java.util.Collection
        public Object[] toArray() {
            return this.this$0.toArrayHelper(1);
        }

        @Override // java.util.Collection
        public <T> T[] toArray(T[] tArr) {
            return (T[]) this.this$0.toArrayHelper(tArr, 1);
        }
    }

    public static <K, V> boolean containsAllHelper(Map<K, V> map, Collection<?> collection) {
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            if (!map.containsKey(it.next())) {
                return false;
            }
        }
        return true;
    }

    public static <T> boolean equalsSetHelper(Set<T> set, Object obj) {
        boolean z = false;
        if (set == obj) {
            return true;
        }
        if (obj instanceof Set) {
            Set set2 = (Set) obj;
            try {
                if (set.size() == set2.size()) {
                    z = set.containsAll(set2);
                }
                return z;
            } catch (ClassCastException e) {
                return false;
            } catch (NullPointerException e2) {
                return false;
            }
        }
        return false;
    }

    public static <K, V> boolean removeAllHelper(Map<K, V> map, Collection<?> collection) {
        int size = map.size();
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            map.remove(it.next());
        }
        return size != map.size();
    }

    public static <K, V> boolean retainAllHelper(Map<K, V> map, Collection<?> collection) {
        int size = map.size();
        Iterator<K> it = map.keySet().iterator();
        while (it.hasNext()) {
            if (!collection.contains(it.next())) {
                it.remove();
            }
        }
        return size != map.size();
    }

    protected abstract void colClear();

    protected abstract Object colGetEntry(int i, int i2);

    protected abstract Map<K, V> colGetMap();

    protected abstract int colGetSize();

    protected abstract int colIndexOfKey(Object obj);

    protected abstract int colIndexOfValue(Object obj);

    protected abstract void colPut(K k, V v);

    protected abstract void colRemoveAt(int i);

    protected abstract V colSetValue(int i, V v);

    public Set<Map.Entry<K, V>> getEntrySet() {
        if (this.mEntrySet == null) {
            this.mEntrySet = new EntrySet(this);
        }
        return this.mEntrySet;
    }

    public Set<K> getKeySet() {
        if (this.mKeySet == null) {
            this.mKeySet = new KeySet(this);
        }
        return this.mKeySet;
    }

    public Collection<V> getValues() {
        if (this.mValues == null) {
            this.mValues = new ValuesCollection(this);
        }
        return this.mValues;
    }

    public Object[] toArrayHelper(int i) {
        int colGetSize = colGetSize();
        Object[] objArr = new Object[colGetSize];
        for (int i2 = 0; i2 < colGetSize; i2++) {
            objArr[i2] = colGetEntry(i2, i);
        }
        return objArr;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v16, types: [java.lang.Object[]] */
    public <T> T[] toArrayHelper(T[] tArr, int i) {
        int colGetSize = colGetSize();
        T[] tArr2 = tArr;
        if (tArr.length < colGetSize) {
            tArr2 = (Object[]) Array.newInstance(tArr.getClass().getComponentType(), colGetSize);
        }
        for (int i2 = 0; i2 < colGetSize; i2++) {
            tArr2[i2] = colGetEntry(i2, i);
        }
        if (tArr2.length > colGetSize) {
            tArr2[colGetSize] = null;
        }
        return tArr2;
    }
}
