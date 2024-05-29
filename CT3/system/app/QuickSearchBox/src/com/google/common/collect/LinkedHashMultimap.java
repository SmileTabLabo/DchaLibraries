package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nullable;
@GwtCompatible(emulated = true, serializable = true)
/* loaded from: a.zip:com/google/common/collect/LinkedHashMultimap.class */
public final class LinkedHashMultimap<K, V> extends AbstractSetMultimap<K, V> {
    @VisibleForTesting
    static final double VALUE_SET_LOAD_FACTOR = 1.0d;
    @GwtIncompatible("java serialization not supported")
    private static final long serialVersionUID = 1;
    private transient ValueEntry<K, V> multimapHeaderEntry;
    @VisibleForTesting
    transient int valueSetCapacity;

    /* JADX INFO: Access modifiers changed from: package-private */
    @VisibleForTesting
    /* loaded from: a.zip:com/google/common/collect/LinkedHashMultimap$ValueEntry.class */
    public static final class ValueEntry<K, V> extends ImmutableEntry<K, V> implements ValueSetLink<K, V> {
        @Nullable
        ValueEntry<K, V> nextInValueBucket;
        ValueEntry<K, V> predecessorInMultimap;
        ValueSetLink<K, V> predecessorInValueSet;
        final int smearedValueHash;
        ValueEntry<K, V> successorInMultimap;
        ValueSetLink<K, V> successorInValueSet;

        ValueEntry(@Nullable K k, @Nullable V v, int i, @Nullable ValueEntry<K, V> valueEntry) {
            super(k, v);
            this.smearedValueHash = i;
            this.nextInValueBucket = valueEntry;
        }

        public ValueEntry<K, V> getPredecessorInMultimap() {
            return this.predecessorInMultimap;
        }

        @Override // com.google.common.collect.LinkedHashMultimap.ValueSetLink
        public ValueSetLink<K, V> getPredecessorInValueSet() {
            return this.predecessorInValueSet;
        }

        public ValueEntry<K, V> getSuccessorInMultimap() {
            return this.successorInMultimap;
        }

        @Override // com.google.common.collect.LinkedHashMultimap.ValueSetLink
        public ValueSetLink<K, V> getSuccessorInValueSet() {
            return this.successorInValueSet;
        }

        boolean matchesValue(@Nullable Object obj, int i) {
            return this.smearedValueHash == i ? Objects.equal(getValue(), obj) : false;
        }

        public void setPredecessorInMultimap(ValueEntry<K, V> valueEntry) {
            this.predecessorInMultimap = valueEntry;
        }

        @Override // com.google.common.collect.LinkedHashMultimap.ValueSetLink
        public void setPredecessorInValueSet(ValueSetLink<K, V> valueSetLink) {
            this.predecessorInValueSet = valueSetLink;
        }

        public void setSuccessorInMultimap(ValueEntry<K, V> valueEntry) {
            this.successorInMultimap = valueEntry;
        }

        @Override // com.google.common.collect.LinkedHashMultimap.ValueSetLink
        public void setSuccessorInValueSet(ValueSetLink<K, V> valueSetLink) {
            this.successorInValueSet = valueSetLink;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @VisibleForTesting
    /* loaded from: a.zip:com/google/common/collect/LinkedHashMultimap$ValueSet.class */
    public final class ValueSet extends Sets.ImprovedAbstractSet<V> implements ValueSetLink<K, V> {
        @VisibleForTesting
        ValueEntry<K, V>[] hashTable;
        private final K key;
        final LinkedHashMultimap this$0;
        private int size = 0;
        private int modCount = 0;
        private ValueSetLink<K, V> firstEntry = this;
        private ValueSetLink<K, V> lastEntry = this;

        ValueSet(LinkedHashMultimap linkedHashMultimap, K k, int i) {
            this.this$0 = linkedHashMultimap;
            this.key = k;
            this.hashTable = new ValueEntry[Hashing.closedTableSize(i, LinkedHashMultimap.VALUE_SET_LOAD_FACTOR)];
        }

        private int mask() {
            return this.hashTable.length - 1;
        }

        private void rehashIfNecessary() {
            if (!Hashing.needsResizing(this.size, this.hashTable.length, LinkedHashMultimap.VALUE_SET_LOAD_FACTOR)) {
                return;
            }
            ValueEntry<K, V>[] valueEntryArr = new ValueEntry[this.hashTable.length * 2];
            this.hashTable = valueEntryArr;
            int length = valueEntryArr.length;
            ValueSetLink<K, V> valueSetLink = this.firstEntry;
            while (true) {
                ValueSet valueSet = valueSetLink;
                if (valueSet == this) {
                    return;
                }
                ValueEntry<K, V> valueEntry = (ValueEntry) valueSet;
                int i = valueEntry.smearedValueHash & (length - 1);
                valueEntry.nextInValueBucket = valueEntryArr[i];
                valueEntryArr[i] = valueEntry;
                valueSetLink = valueSet.getSuccessorInValueSet();
            }
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public boolean add(@Nullable V v) {
            int smearedHash = Hashing.smearedHash(v);
            int mask = smearedHash & mask();
            ValueEntry<K, V> valueEntry = this.hashTable[mask];
            ValueEntry<K, V> valueEntry2 = valueEntry;
            while (true) {
                ValueEntry<K, V> valueEntry3 = valueEntry2;
                if (valueEntry3 == null) {
                    ValueEntry<K, V> valueEntry4 = new ValueEntry<>(this.key, v, smearedHash, valueEntry);
                    LinkedHashMultimap.succeedsInValueSet(this.lastEntry, valueEntry4);
                    LinkedHashMultimap.succeedsInValueSet(valueEntry4, this);
                    LinkedHashMultimap.succeedsInMultimap(this.this$0.multimapHeaderEntry.getPredecessorInMultimap(), valueEntry4);
                    LinkedHashMultimap.succeedsInMultimap(valueEntry4, this.this$0.multimapHeaderEntry);
                    this.hashTable[mask] = valueEntry4;
                    this.size++;
                    this.modCount++;
                    rehashIfNecessary();
                    return true;
                } else if (valueEntry3.matchesValue(v, smearedHash)) {
                    return false;
                } else {
                    valueEntry2 = valueEntry3.nextInValueBucket;
                }
            }
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public void clear() {
            Arrays.fill(this.hashTable, (Object) null);
            this.size = 0;
            ValueSetLink<K, V> valueSetLink = this.firstEntry;
            while (true) {
                ValueSet valueSet = valueSetLink;
                if (valueSet == this) {
                    LinkedHashMultimap.succeedsInValueSet(this, this);
                    this.modCount++;
                    return;
                }
                LinkedHashMultimap.deleteFromMultimap((ValueEntry) valueSet);
                valueSetLink = valueSet.getSuccessorInValueSet();
            }
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public boolean contains(@Nullable Object obj) {
            int smearedHash = Hashing.smearedHash(obj);
            ValueEntry<K, V> valueEntry = this.hashTable[mask() & smearedHash];
            while (true) {
                ValueEntry<K, V> valueEntry2 = valueEntry;
                if (valueEntry2 == null) {
                    return false;
                }
                if (valueEntry2.matchesValue(obj, smearedHash)) {
                    return true;
                }
                valueEntry = valueEntry2.nextInValueBucket;
            }
        }

        @Override // com.google.common.collect.LinkedHashMultimap.ValueSetLink
        public ValueSetLink<K, V> getPredecessorInValueSet() {
            return this.lastEntry;
        }

        @Override // com.google.common.collect.LinkedHashMultimap.ValueSetLink
        public ValueSetLink<K, V> getSuccessorInValueSet() {
            return this.firstEntry;
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set
        public Iterator<V> iterator() {
            return new Iterator<V>(this) { // from class: com.google.common.collect.LinkedHashMultimap.ValueSet.1
                int expectedModCount;
                ValueSetLink<K, V> nextEntry;
                final ValueSet this$1;
                ValueEntry<K, V> toRemove;

                {
                    this.this$1 = this;
                    this.nextEntry = this.this$1.firstEntry;
                    this.expectedModCount = this.this$1.modCount;
                }

                private void checkForComodification() {
                    if (this.this$1.modCount != this.expectedModCount) {
                        throw new ConcurrentModificationException();
                    }
                }

                @Override // java.util.Iterator
                public boolean hasNext() {
                    checkForComodification();
                    return this.nextEntry != this.this$1;
                }

                @Override // java.util.Iterator
                public V next() {
                    if (hasNext()) {
                        ValueEntry<K, V> valueEntry = (ValueEntry) this.nextEntry;
                        V value = valueEntry.getValue();
                        this.toRemove = valueEntry;
                        this.nextEntry = valueEntry.getSuccessorInValueSet();
                        return value;
                    }
                    throw new NoSuchElementException();
                }

                @Override // java.util.Iterator
                public void remove() {
                    checkForComodification();
                    CollectPreconditions.checkRemove(this.toRemove != null);
                    this.this$1.remove(this.toRemove.getValue());
                    this.expectedModCount = this.this$1.modCount;
                    this.toRemove = null;
                }
            };
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public boolean remove(@Nullable Object obj) {
            int smearedHash = Hashing.smearedHash(obj);
            int mask = smearedHash & mask();
            ValueEntry<K, V> valueEntry = null;
            ValueEntry<K, V> valueEntry2 = this.hashTable[mask];
            while (true) {
                ValueEntry<K, V> valueEntry3 = valueEntry2;
                if (valueEntry3 == null) {
                    return false;
                }
                if (valueEntry3.matchesValue(obj, smearedHash)) {
                    if (valueEntry == null) {
                        this.hashTable[mask] = valueEntry3.nextInValueBucket;
                    } else {
                        valueEntry.nextInValueBucket = valueEntry3.nextInValueBucket;
                    }
                    LinkedHashMultimap.deleteFromValueSet(valueEntry3);
                    LinkedHashMultimap.deleteFromMultimap(valueEntry3);
                    this.size--;
                    this.modCount++;
                    return true;
                }
                valueEntry = valueEntry3;
                valueEntry2 = valueEntry3.nextInValueBucket;
            }
        }

        @Override // com.google.common.collect.LinkedHashMultimap.ValueSetLink
        public void setPredecessorInValueSet(ValueSetLink<K, V> valueSetLink) {
            this.lastEntry = valueSetLink;
        }

        @Override // com.google.common.collect.LinkedHashMultimap.ValueSetLink
        public void setSuccessorInValueSet(ValueSetLink<K, V> valueSetLink) {
            this.firstEntry = valueSetLink;
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public int size() {
            return this.size;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/google/common/collect/LinkedHashMultimap$ValueSetLink.class */
    public interface ValueSetLink<K, V> {
        ValueSetLink<K, V> getPredecessorInValueSet();

        ValueSetLink<K, V> getSuccessorInValueSet();

        void setPredecessorInValueSet(ValueSetLink<K, V> valueSetLink);

        void setSuccessorInValueSet(ValueSetLink<K, V> valueSetLink);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static <K, V> void deleteFromMultimap(ValueEntry<K, V> valueEntry) {
        succeedsInMultimap(valueEntry.getPredecessorInMultimap(), valueEntry.getSuccessorInMultimap());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static <K, V> void deleteFromValueSet(ValueSetLink<K, V> valueSetLink) {
        succeedsInValueSet(valueSetLink.getPredecessorInValueSet(), valueSetLink.getSuccessorInValueSet());
    }

    /* JADX WARN: Multi-variable type inference failed */
    @GwtIncompatible("java.io.ObjectInputStream")
    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.multimapHeaderEntry = new ValueEntry<>(null, null, 0, null);
        succeedsInMultimap(this.multimapHeaderEntry, this.multimapHeaderEntry);
        this.valueSetCapacity = objectInputStream.readInt();
        int readInt = objectInputStream.readInt();
        LinkedHashMap linkedHashMap = new LinkedHashMap(Maps.capacity(readInt));
        for (int i = 0; i < readInt; i++) {
            Object readObject = objectInputStream.readObject();
            linkedHashMap.put(readObject, createCollection(readObject));
        }
        int readInt2 = objectInputStream.readInt();
        for (int i2 = 0; i2 < readInt2; i2++) {
            ((Collection) linkedHashMap.get(objectInputStream.readObject())).add(objectInputStream.readObject());
        }
        setMap(linkedHashMap);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static <K, V> void succeedsInMultimap(ValueEntry<K, V> valueEntry, ValueEntry<K, V> valueEntry2) {
        valueEntry.setSuccessorInMultimap(valueEntry2);
        valueEntry2.setPredecessorInMultimap(valueEntry);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static <K, V> void succeedsInValueSet(ValueSetLink<K, V> valueSetLink, ValueSetLink<K, V> valueSetLink2) {
        valueSetLink.setSuccessorInValueSet(valueSetLink2);
        valueSetLink2.setPredecessorInValueSet(valueSetLink);
    }

    @GwtIncompatible("java.io.ObjectOutputStream")
    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeInt(this.valueSetCapacity);
        objectOutputStream.writeInt(keySet().size());
        for (Object obj : keySet()) {
            objectOutputStream.writeObject(obj);
        }
        objectOutputStream.writeInt(size());
        Iterator<T> it = entries().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            objectOutputStream.writeObject(entry.getKey());
            objectOutputStream.writeObject(entry.getValue());
        }
    }

    @Override // com.google.common.collect.AbstractSetMultimap, com.google.common.collect.AbstractMultimap, com.google.common.collect.Multimap
    public /* bridge */ /* synthetic */ Map asMap() {
        return super.asMap();
    }

    @Override // com.google.common.collect.AbstractMapBasedMultimap, com.google.common.collect.Multimap
    public void clear() {
        super.clear();
        succeedsInMultimap(this.multimapHeaderEntry, this.multimapHeaderEntry);
    }

    @Override // com.google.common.collect.AbstractMultimap, com.google.common.collect.Multimap
    public /* bridge */ /* synthetic */ boolean containsEntry(Object obj, Object obj2) {
        return super.containsEntry(obj, obj2);
    }

    Collection<V> createCollection(K k) {
        return new ValueSet(this, k, this.valueSetCapacity);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.AbstractSetMultimap, com.google.common.collect.AbstractMapBasedMultimap
    public Set<V> createCollection() {
        return new LinkedHashSet(this.valueSetCapacity);
    }

    @Override // com.google.common.collect.AbstractSetMultimap, com.google.common.collect.AbstractMapBasedMultimap, com.google.common.collect.AbstractMultimap
    public Set<Map.Entry<K, V>> entries() {
        return super.entries();
    }

    @Override // com.google.common.collect.AbstractMapBasedMultimap, com.google.common.collect.AbstractMultimap
    Iterator<Map.Entry<K, V>> entryIterator() {
        return new Iterator<Map.Entry<K, V>>(this) { // from class: com.google.common.collect.LinkedHashMultimap.1
            ValueEntry<K, V> nextEntry;
            final LinkedHashMultimap this$0;
            ValueEntry<K, V> toRemove;

            {
                this.this$0 = this;
                this.nextEntry = this.this$0.multimapHeaderEntry.successorInMultimap;
            }

            @Override // java.util.Iterator
            public boolean hasNext() {
                return this.nextEntry != this.this$0.multimapHeaderEntry;
            }

            @Override // java.util.Iterator
            public Map.Entry<K, V> next() {
                if (hasNext()) {
                    ValueEntry<K, V> valueEntry = this.nextEntry;
                    this.toRemove = valueEntry;
                    this.nextEntry = this.nextEntry.successorInMultimap;
                    return valueEntry;
                }
                throw new NoSuchElementException();
            }

            @Override // java.util.Iterator
            public void remove() {
                CollectPreconditions.checkRemove(this.toRemove != null);
                this.this$0.remove(this.toRemove.getKey(), this.toRemove.getValue());
                this.toRemove = null;
            }
        };
    }

    @Override // com.google.common.collect.AbstractSetMultimap, com.google.common.collect.AbstractMultimap
    public /* bridge */ /* synthetic */ boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.google.common.collect.AbstractMultimap
    public /* bridge */ /* synthetic */ int hashCode() {
        return super.hashCode();
    }

    @Override // com.google.common.collect.AbstractMultimap
    public /* bridge */ /* synthetic */ Set keySet() {
        return super.keySet();
    }

    @Override // com.google.common.collect.AbstractMultimap, com.google.common.collect.Multimap
    public /* bridge */ /* synthetic */ boolean remove(Object obj, Object obj2) {
        return super.remove(obj, obj2);
    }

    @Override // com.google.common.collect.AbstractMapBasedMultimap, com.google.common.collect.Multimap
    public /* bridge */ /* synthetic */ int size() {
        return super.size();
    }

    @Override // com.google.common.collect.AbstractMultimap
    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }
}
