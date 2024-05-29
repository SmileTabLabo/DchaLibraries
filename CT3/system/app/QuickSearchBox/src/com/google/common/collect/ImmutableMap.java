package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMapEntry;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
@GwtCompatible(emulated = true, serializable = true)
/* loaded from: a.zip:com/google/common/collect/ImmutableMap.class */
public abstract class ImmutableMap<K, V> implements Map<K, V>, Serializable {
    private static final Map.Entry<?, ?>[] EMPTY_ENTRY_ARRAY = new Map.Entry[0];
    private transient ImmutableSet<Map.Entry<K, V>> entrySet;
    private transient ImmutableSet<K> keySet;
    private transient ImmutableCollection<V> values;

    /* loaded from: a.zip:com/google/common/collect/ImmutableMap$Builder.class */
    public static class Builder<K, V> {
        ImmutableMapEntry.TerminalEntry<K, V>[] entries;
        int size;

        public Builder() {
            this(4);
        }

        Builder(int i) {
            this.entries = new ImmutableMapEntry.TerminalEntry[i];
            this.size = 0;
        }

        private void ensureCapacity(int i) {
            if (i > this.entries.length) {
                this.entries = (ImmutableMapEntry.TerminalEntry[]) ObjectArrays.arraysCopyOf(this.entries, ImmutableCollection.Builder.expandedCapacity(this.entries.length, i));
            }
        }

        public ImmutableMap<K, V> build() {
            switch (this.size) {
                case 0:
                    return ImmutableMap.of();
                case 1:
                    return ImmutableMap.of((Object) this.entries[0].getKey(), (Object) this.entries[0].getValue());
                default:
                    return new RegularImmutableMap(this.size, this.entries);
            }
        }

        public Builder<K, V> put(K k, V v) {
            ensureCapacity(this.size + 1);
            ImmutableMapEntry.TerminalEntry<K, V> entryOf = ImmutableMap.entryOf(k, v);
            ImmutableMapEntry.TerminalEntry<K, V>[] terminalEntryArr = this.entries;
            int i = this.size;
            this.size = i + 1;
            terminalEntryArr[i] = entryOf;
            return this;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/google/common/collect/ImmutableMap$SerializedForm.class */
    public static class SerializedForm implements Serializable {
        private static final long serialVersionUID = 0;
        private final Object[] keys;
        private final Object[] values;

        /* JADX INFO: Access modifiers changed from: package-private */
        public SerializedForm(ImmutableMap<?, ?> immutableMap) {
            this.keys = new Object[immutableMap.size()];
            this.values = new Object[immutableMap.size()];
            int i = 0;
            for (Map.Entry<?, ?> entry : immutableMap.entrySet()) {
                this.keys[i] = entry.getKey();
                this.values[i] = entry.getValue();
                i++;
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public Object createMap(Builder<Object, Object> builder) {
            for (int i = 0; i < this.keys.length; i++) {
                builder.put(this.keys[i], this.values[i]);
            }
            return builder.build();
        }

        Object readResolve() {
            return createMap(new Builder<>());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void checkNoConflict(boolean z, String str, Map.Entry<?, ?> entry, Map.Entry<?, ?> entry2) {
        if (!z) {
            throw new IllegalArgumentException("Multiple entries with same " + str + ": " + entry + " and " + entry2);
        }
    }

    public static <K, V> ImmutableMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        if ((map instanceof ImmutableMap) && !(map instanceof ImmutableSortedMap)) {
            ImmutableMap<K, V> immutableMap = (ImmutableMap) map;
            if (!immutableMap.isPartialView()) {
                return immutableMap;
            }
        } else if (map instanceof EnumMap) {
            return copyOfEnumMapUnsafe(map);
        }
        Map.Entry[] entryArr = (Map.Entry[]) map.entrySet().toArray(EMPTY_ENTRY_ARRAY);
        switch (entryArr.length) {
            case 0:
                return of();
            case 1:
                Map.Entry entry = entryArr[0];
                return of(entry.getKey(), entry.getValue());
            default:
                return new RegularImmutableMap(entryArr);
        }
    }

    private static <K extends Enum<K>, V> ImmutableMap<K, V> copyOfEnumMap(Map<K, ? extends V> map) {
        EnumMap enumMap = new EnumMap(map);
        Iterator<T> it = enumMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            CollectPreconditions.checkEntryNotNull(entry.getKey(), entry.getValue());
        }
        return ImmutableEnumMap.asImmutable(enumMap);
    }

    private static <K, V> ImmutableMap<K, V> copyOfEnumMapUnsafe(Map<? extends K, ? extends V> map) {
        return copyOfEnumMap((EnumMap) map);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <K, V> ImmutableMapEntry.TerminalEntry<K, V> entryOf(K k, V v) {
        CollectPreconditions.checkEntryNotNull(k, v);
        return new ImmutableMapEntry.TerminalEntry<>(k, v);
    }

    public static <K, V> ImmutableMap<K, V> of() {
        return ImmutableBiMap.of();
    }

    public static <K, V> ImmutableMap<K, V> of(K k, V v) {
        return ImmutableBiMap.of((Object) k, (Object) v);
    }

    @Override // java.util.Map
    @Deprecated
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.Map
    public boolean containsKey(@Nullable Object obj) {
        return get(obj) != null;
    }

    @Override // java.util.Map
    public boolean containsValue(@Nullable Object obj) {
        return values().contains(obj);
    }

    abstract ImmutableSet<Map.Entry<K, V>> createEntrySet();

    ImmutableSet<K> createKeySet() {
        return new ImmutableMapKeySet(this);
    }

    @Override // java.util.Map
    public ImmutableSet<Map.Entry<K, V>> entrySet() {
        ImmutableSet<Map.Entry<K, V>> immutableSet = this.entrySet;
        ImmutableSet<Map.Entry<K, V>> immutableSet2 = immutableSet;
        if (immutableSet == null) {
            immutableSet2 = createEntrySet();
            this.entrySet = immutableSet2;
        }
        return immutableSet2;
    }

    @Override // java.util.Map
    public boolean equals(@Nullable Object obj) {
        return Maps.equalsImpl(this, obj);
    }

    @Override // java.util.Map
    public abstract V get(@Nullable Object obj);

    @Override // java.util.Map
    public int hashCode() {
        return entrySet().hashCode();
    }

    @Override // java.util.Map
    public boolean isEmpty() {
        boolean z = false;
        if (size() == 0) {
            z = true;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract boolean isPartialView();

    @Override // java.util.Map
    public ImmutableSet<K> keySet() {
        ImmutableSet<K> immutableSet = this.keySet;
        ImmutableSet<K> immutableSet2 = immutableSet;
        if (immutableSet == null) {
            immutableSet2 = createKeySet();
            this.keySet = immutableSet2;
        }
        return immutableSet2;
    }

    @Override // java.util.Map
    @Deprecated
    public final V put(K k, V v) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.Map
    @Deprecated
    public final void putAll(Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.Map
    @Deprecated
    public final V remove(Object obj) {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return Maps.toStringImpl(this);
    }

    @Override // java.util.Map, java.util.SortedMap
    public ImmutableCollection<V> values() {
        ImmutableCollection<V> immutableCollection = this.values;
        ImmutableMapValues immutableMapValues = immutableCollection;
        if (immutableCollection == null) {
            immutableMapValues = new ImmutableMapValues(this);
            this.values = immutableMapValues;
        }
        return immutableMapValues;
    }

    Object writeReplace() {
        return new SerializedForm(this);
    }
}
