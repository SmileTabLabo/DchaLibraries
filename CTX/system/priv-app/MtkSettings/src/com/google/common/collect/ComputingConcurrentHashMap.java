package com.google.common.collect;

import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.common.collect.MapMakerInternalMap;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReferenceArray;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ComputingConcurrentHashMap<K, V> extends MapMakerInternalMap<K, V> {
    private static final long serialVersionUID = 4;
    final Function<? super K, ? extends V> computingFunction;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ComputingConcurrentHashMap(MapMaker mapMaker, Function<? super K, ? extends V> function) {
        super(mapMaker);
        this.computingFunction = (Function) Preconditions.checkNotNull(function);
    }

    @Override // com.google.common.collect.MapMakerInternalMap
    MapMakerInternalMap.Segment<K, V> createSegment(int i, int i2) {
        return new ComputingSegment(this, i, i2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.MapMakerInternalMap
    public ComputingSegment<K, V> segmentFor(int i) {
        return (ComputingSegment) super.segmentFor(i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public V getOrCompute(K k) throws ExecutionException {
        int hash = hash(Preconditions.checkNotNull(k));
        return segmentFor(hash).getOrCompute(k, hash, this.computingFunction);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class ComputingSegment<K, V> extends MapMakerInternalMap.Segment<K, V> {
        ComputingSegment(MapMakerInternalMap<K, V> mapMakerInternalMap, int i, int i2) {
            super(mapMakerInternalMap, i, i2);
        }

        /* JADX WARN: Removed duplicated region for block: B:63:0x00c9 A[SYNTHETIC] */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        V getOrCompute(K k, int i, Function<? super K, ? extends V> function) throws ExecutionException {
            MapMakerInternalMap.ReferenceEntry<K, V> entry;
            boolean z;
            ComputingValueReference<K, V> computingValueReference;
            V waitForValue;
            V liveValue;
            do {
                try {
                    entry = getEntry(k, i);
                    if (entry != null && (liveValue = getLiveValue(entry)) != null) {
                        recordRead(entry);
                        return liveValue;
                    }
                    if (entry == null || !entry.getValueReference().isComputingReference()) {
                        ComputingValueReference<K, V> computingValueReference2 = null;
                        lock();
                        preWriteCleanup();
                        int i2 = this.count - 1;
                        AtomicReferenceArray<MapMakerInternalMap.ReferenceEntry<K, V>> atomicReferenceArray = this.table;
                        int length = (atomicReferenceArray.length() - 1) & i;
                        MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry = atomicReferenceArray.get(length);
                        MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry2 = referenceEntry;
                        while (true) {
                            if (referenceEntry2 == null) {
                                break;
                            }
                            K key = referenceEntry2.getKey();
                            if (referenceEntry2.getHash() != i || key == null || !this.map.keyEquivalence.equivalent(k, key)) {
                                referenceEntry2 = referenceEntry2.getNext();
                            } else if (referenceEntry2.getValueReference().isComputingReference()) {
                                z = false;
                            } else {
                                V v = referenceEntry2.getValueReference().get();
                                if (v == null) {
                                    enqueueNotification(key, i, v, MapMaker.RemovalCause.COLLECTED);
                                } else if (!this.map.expires() || !this.map.isExpired(referenceEntry2)) {
                                    recordLockedRead(referenceEntry2);
                                    unlock();
                                    postWriteCleanup();
                                    return v;
                                } else {
                                    enqueueNotification(key, i, v, MapMaker.RemovalCause.EXPIRED);
                                }
                                this.evictionQueue.remove(referenceEntry2);
                                this.expirationQueue.remove(referenceEntry2);
                                this.count = i2;
                            }
                        }
                        z = true;
                        if (z) {
                            computingValueReference2 = new ComputingValueReference<>(function);
                            if (referenceEntry2 == null) {
                                MapMakerInternalMap.ReferenceEntry<K, V> newEntry = newEntry(k, i, referenceEntry);
                                newEntry.setValueReference(computingValueReference2);
                                atomicReferenceArray.set(length, newEntry);
                                computingValueReference = computingValueReference2;
                                entry = newEntry;
                                unlock();
                                postWriteCleanup();
                                if (z) {
                                    return compute(k, i, entry, computingValueReference);
                                }
                            } else {
                                referenceEntry2.setValueReference(computingValueReference2);
                            }
                        }
                        computingValueReference = computingValueReference2;
                        entry = referenceEntry2;
                        unlock();
                        postWriteCleanup();
                        if (z) {
                        }
                    }
                    Preconditions.checkState(true ^ Thread.holdsLock(entry), "Recursive computation");
                    waitForValue = entry.getValueReference().waitForValue();
                } finally {
                    postReadCleanup();
                }
            } while (waitForValue == null);
            recordRead(entry);
            return waitForValue;
        }

        /* JADX WARN: Removed duplicated region for block: B:32:0x0043  */
        /* JADX WARN: Removed duplicated region for block: B:34:0x0048  */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        V compute(K k, int i, MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry, ComputingValueReference<K, V> computingValueReference) throws ExecutionException {
            V compute;
            System.nanoTime();
            try {
                try {
                    try {
                        synchronized (referenceEntry) {
                            try {
                                compute = computingValueReference.compute(k, i);
                            } catch (Throwable th) {
                                th = th;
                            }
                            try {
                                long nanoTime = System.nanoTime();
                                if (compute != null && put(k, i, compute, true) != null) {
                                    enqueueNotification(k, i, compute, MapMaker.RemovalCause.REPLACED);
                                }
                                if (nanoTime == 0) {
                                    System.nanoTime();
                                }
                                if (compute == null) {
                                    clearValue(k, i, computingValueReference);
                                }
                                return compute;
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (0 == 0) {
                            System.nanoTime();
                        }
                        if (0 == 0) {
                            clearValue(k, i, computingValueReference);
                        }
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                }
            } catch (Throwable th5) {
                th = th5;
                if (0 == 0) {
                }
                if (0 == 0) {
                }
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class ComputationExceptionReference<K, V> implements MapMakerInternalMap.ValueReference<K, V> {
        final Throwable t;

        ComputationExceptionReference(Throwable th) {
            this.t = th;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public V get() {
            return null;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public MapMakerInternalMap.ReferenceEntry<K, V> getEntry() {
            return null;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public MapMakerInternalMap.ValueReference<K, V> copyFor(ReferenceQueue<V> referenceQueue, V v, MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry) {
            return this;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public boolean isComputingReference() {
            return false;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public V waitForValue() throws ExecutionException {
            throw new ExecutionException(this.t);
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public void clear(MapMakerInternalMap.ValueReference<K, V> valueReference) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class ComputedReference<K, V> implements MapMakerInternalMap.ValueReference<K, V> {
        final V value;

        ComputedReference(V v) {
            this.value = v;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public V get() {
            return this.value;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public MapMakerInternalMap.ReferenceEntry<K, V> getEntry() {
            return null;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public MapMakerInternalMap.ValueReference<K, V> copyFor(ReferenceQueue<V> referenceQueue, V v, MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry) {
            return this;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public boolean isComputingReference() {
            return false;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public V waitForValue() {
            return get();
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public void clear(MapMakerInternalMap.ValueReference<K, V> valueReference) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class ComputingValueReference<K, V> implements MapMakerInternalMap.ValueReference<K, V> {
        volatile MapMakerInternalMap.ValueReference<K, V> computedReference = MapMakerInternalMap.unset();
        final Function<? super K, ? extends V> computingFunction;

        public ComputingValueReference(Function<? super K, ? extends V> function) {
            this.computingFunction = function;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public V get() {
            return null;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public MapMakerInternalMap.ReferenceEntry<K, V> getEntry() {
            return null;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public MapMakerInternalMap.ValueReference<K, V> copyFor(ReferenceQueue<V> referenceQueue, V v, MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry) {
            return this;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public boolean isComputingReference() {
            return true;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public V waitForValue() throws ExecutionException {
            boolean z;
            Throwable th;
            if (this.computedReference == MapMakerInternalMap.UNSET) {
                try {
                    synchronized (this) {
                        z = false;
                        while (this.computedReference == MapMakerInternalMap.UNSET) {
                            try {
                                try {
                                    wait();
                                } catch (InterruptedException e) {
                                    z = true;
                                }
                            } catch (Throwable th2) {
                                try {
                                    throw th2;
                                } catch (Throwable th3) {
                                    th = th3;
                                    if (z) {
                                        Thread.currentThread().interrupt();
                                    }
                                    throw th;
                                }
                            }
                        }
                    }
                    if (z) {
                        Thread.currentThread().interrupt();
                    }
                } catch (Throwable th4) {
                    z = false;
                    th = th4;
                }
            }
            return this.computedReference.waitForValue();
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public void clear(MapMakerInternalMap.ValueReference<K, V> valueReference) {
            setValueReference(valueReference);
        }

        V compute(K k, int i) throws ExecutionException {
            try {
                V apply = this.computingFunction.apply(k);
                setValueReference(new ComputedReference(apply));
                return apply;
            } catch (Throwable th) {
                setValueReference(new ComputationExceptionReference(th));
                throw new ExecutionException(th);
            }
        }

        void setValueReference(MapMakerInternalMap.ValueReference<K, V> valueReference) {
            synchronized (this) {
                if (this.computedReference == MapMakerInternalMap.UNSET) {
                    this.computedReference = valueReference;
                    notifyAll();
                }
            }
        }
    }

    @Override // com.google.common.collect.MapMakerInternalMap
    Object writeReplace() {
        return new ComputingSerializationProxy(this.keyStrength, this.valueStrength, this.keyEquivalence, this.valueEquivalence, this.expireAfterWriteNanos, this.expireAfterAccessNanos, this.maximumSize, this.concurrencyLevel, this.removalListener, this, this.computingFunction);
    }

    /* loaded from: classes.dex */
    static final class ComputingSerializationProxy<K, V> extends MapMakerInternalMap.AbstractSerializationProxy<K, V> {
        private static final long serialVersionUID = 4;
        final Function<? super K, ? extends V> computingFunction;

        ComputingSerializationProxy(MapMakerInternalMap.Strength strength, MapMakerInternalMap.Strength strength2, Equivalence<Object> equivalence, Equivalence<Object> equivalence2, long j, long j2, int i, int i2, MapMaker.RemovalListener<? super K, ? super V> removalListener, ConcurrentMap<K, V> concurrentMap, Function<? super K, ? extends V> function) {
            super(strength, strength2, equivalence, equivalence2, j, j2, i, i2, removalListener, concurrentMap);
            this.computingFunction = function;
        }

        private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
            objectOutputStream.defaultWriteObject();
            writeMapTo(objectOutputStream);
        }

        private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
            objectInputStream.defaultReadObject();
            this.delegate = readMapMaker(objectInputStream).makeComputingMap(this.computingFunction);
            readEntries(objectInputStream);
        }

        Object readResolve() {
            return this.delegate;
        }
    }
}
