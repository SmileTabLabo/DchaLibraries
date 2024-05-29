package android.support.v4.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/* loaded from: a.zip:android/support/v4/util/ArrayMap.class */
public class ArrayMap<K, V> extends SimpleArrayMap<K, V> implements Map<K, V> {
    MapCollections<K, V> mCollections;

    public ArrayMap() {
    }

    public ArrayMap(int i) {
        super(i);
    }

    private MapCollections<K, V> getCollection() {
        if (this.mCollections == null) {
            this.mCollections = new MapCollections<K, V>(this) { // from class: android.support.v4.util.ArrayMap.1
                final ArrayMap this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.support.v4.util.MapCollections
                protected void colClear() {
                    this.this$0.clear();
                }

                @Override // android.support.v4.util.MapCollections
                protected Object colGetEntry(int i, int i2) {
                    return this.this$0.mArray[(i << 1) + i2];
                }

                @Override // android.support.v4.util.MapCollections
                protected Map<K, V> colGetMap() {
                    return this.this$0;
                }

                @Override // android.support.v4.util.MapCollections
                protected int colGetSize() {
                    return this.this$0.mSize;
                }

                @Override // android.support.v4.util.MapCollections
                protected int colIndexOfKey(Object obj) {
                    return this.this$0.indexOfKey(obj);
                }

                @Override // android.support.v4.util.MapCollections
                protected int colIndexOfValue(Object obj) {
                    return this.this$0.indexOfValue(obj);
                }

                @Override // android.support.v4.util.MapCollections
                protected void colPut(K k, V v) {
                    this.this$0.put(k, v);
                }

                @Override // android.support.v4.util.MapCollections
                protected void colRemoveAt(int i) {
                    this.this$0.removeAt(i);
                }

                @Override // android.support.v4.util.MapCollections
                protected V colSetValue(int i, V v) {
                    return this.this$0.setValueAt(i, v);
                }
            };
        }
        return this.mCollections;
    }

    @Override // java.util.Map
    public Set<Map.Entry<K, V>> entrySet() {
        return getCollection().getEntrySet();
    }

    @Override // java.util.Map
    public Set<K> keySet() {
        return getCollection().getKeySet();
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.Map
    public void putAll(Map<? extends K, ? extends V> map) {
        ensureCapacity(this.mSize + map.size());
        Iterator<T> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            put(entry.getKey(), entry.getValue());
        }
    }

    public boolean retainAll(Collection<?> collection) {
        return MapCollections.retainAllHelper(this, collection);
    }

    @Override // java.util.Map
    public Collection<V> values() {
        return getCollection().getValues();
    }
}
