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
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/google/common/collect/ComputingConcurrentHashMap.class */
public class ComputingConcurrentHashMap<K, V> extends MapMakerInternalMap<K, V> {
    private static final long serialVersionUID = 4;
    final Function<? super K, ? extends V> computingFunction;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/google/common/collect/ComputingConcurrentHashMap$ComputationExceptionReference.class */
    public static final class ComputationExceptionReference<K, V> implements MapMakerInternalMap.ValueReference<K, V> {
        final Throwable t;

        ComputationExceptionReference(Throwable th) {
            this.t = th;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public void clear(MapMakerInternalMap.ValueReference<K, V> valueReference) {
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public MapMakerInternalMap.ValueReference<K, V> copyFor(ReferenceQueue<V> referenceQueue, V v, MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry) {
            return this;
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
        public boolean isComputingReference() {
            return false;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public V waitForValue() throws ExecutionException {
            throw new ExecutionException(this.t);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/google/common/collect/ComputingConcurrentHashMap$ComputedReference.class */
    public static final class ComputedReference<K, V> implements MapMakerInternalMap.ValueReference<K, V> {
        final V value;

        ComputedReference(@Nullable V v) {
            this.value = v;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public void clear(MapMakerInternalMap.ValueReference<K, V> valueReference) {
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public MapMakerInternalMap.ValueReference<K, V> copyFor(ReferenceQueue<V> referenceQueue, V v, MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry) {
            return this;
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
        public boolean isComputingReference() {
            return false;
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public V waitForValue() {
            return get();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/google/common/collect/ComputingConcurrentHashMap$ComputingSegment.class */
    public static final class ComputingSegment<K, V> extends MapMakerInternalMap.Segment<K, V> {
        ComputingSegment(MapMakerInternalMap<K, V> mapMakerInternalMap, int i, int i2) {
            super(mapMakerInternalMap, i, i2);
        }

        V compute(K k, int i, MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry, ComputingValueReference<K, V> computingValueReference) throws ExecutionException {
            V compute;
            long nanoTime;
            V v = null;
            System.nanoTime();
            long j = 0;
            try {
                synchronized (referenceEntry) {
                    compute = computingValueReference.compute(k, i);
                    nanoTime = System.nanoTime();
                }
                if (compute != null && put(k, i, compute, true) != null) {
                    j = nanoTime;
                    v = compute;
                    enqueueNotification(k, i, compute, MapMaker.RemovalCause.REPLACED);
                }
                if (nanoTime == 0) {
                    System.nanoTime();
                }
                if (compute == null) {
                    clearValue(k, i, computingValueReference);
                }
                return compute;
            } catch (Throwable th) {
                if (j == 0) {
                    System.nanoTime();
                }
                if (v == null) {
                    clearValue(k, i, computingValueReference);
                }
                throw th;
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:37:0x00e0, code lost:
            if (r10.getValueReference().isComputingReference() == false) goto L66;
         */
        /* JADX WARN: Code restructure failed: missing block: B:39:0x00e4, code lost:
            r19 = false;
         */
        /* JADX WARN: Code restructure failed: missing block: B:52:0x0145, code lost:
            r0 = r10.getValueReference().get();
         */
        /* JADX WARN: Code restructure failed: missing block: B:53:0x0155, code lost:
            if (r0 != null) goto L72;
         */
        /* JADX WARN: Code restructure failed: missing block: B:54:0x0158, code lost:
            enqueueNotification(r0, r8, r0, com.google.common.collect.MapMaker.RemovalCause.COLLECTED);
         */
        /* JADX WARN: Code restructure failed: missing block: B:56:0x0165, code lost:
            r6.evictionQueue.remove(r10);
            r6.expirationQueue.remove(r10);
            r6.count = r0 - 1;
         */
        /* JADX WARN: Code restructure failed: missing block: B:57:0x0186, code lost:
            r19 = true;
         */
        /* JADX WARN: Code restructure failed: missing block: B:65:0x01a4, code lost:
            if (r6.map.expires() == false) goto L84;
         */
        /* JADX WARN: Code restructure failed: missing block: B:67:0x01b0, code lost:
            if (r6.map.isExpired(r10) == false) goto L77;
         */
        /* JADX WARN: Code restructure failed: missing block: B:68:0x01b3, code lost:
            enqueueNotification(r0, r8, r0, com.google.common.collect.MapMaker.RemovalCause.EXPIRED);
         */
        /* JADX WARN: Code restructure failed: missing block: B:69:0x01c2, code lost:
            recordLockedRead(r10);
         */
        /* JADX WARN: Code restructure failed: missing block: B:71:0x01c9, code lost:
            unlock();
            postWriteCleanup();
         */
        /* JADX WARN: Code restructure failed: missing block: B:73:0x01d6, code lost:
            return r0;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        V getOrCompute(K k, int i, Function<? super K, ? extends V> function) throws ExecutionException {
            MapMakerInternalMap.ReferenceEntry<K, V> entry;
            boolean z;
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
                        ComputingValueReference computingValueReference = null;
                        lock();
                        try {
                            preWriteCleanup();
                            int i2 = this.count;
                            AtomicReferenceArray<MapMakerInternalMap.ReferenceEntry<K, V>> atomicReferenceArray = this.table;
                            int length = i & (atomicReferenceArray.length() - 1);
                            MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry = atomicReferenceArray.get(length);
                            MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry2 = referenceEntry;
                            while (true) {
                                z = true;
                                if (referenceEntry2 == null) {
                                    break;
                                }
                                K key = referenceEntry2.getKey();
                                if (referenceEntry2.getHash() == i && key != null && this.map.keyEquivalence.equivalent(k, key)) {
                                    break;
                                }
                                referenceEntry2 = referenceEntry2.getNext();
                            }
                            MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry3 = referenceEntry2;
                            if (z) {
                                ComputingValueReference computingValueReference2 = new ComputingValueReference(function);
                                if (referenceEntry2 == null) {
                                    try {
                                        MapMakerInternalMap.ReferenceEntry<K, V> newEntry = newEntry(k, i, referenceEntry);
                                        newEntry.setValueReference(computingValueReference2);
                                        atomicReferenceArray.set(length, newEntry);
                                        computingValueReference = computingValueReference2;
                                        referenceEntry3 = newEntry;
                                    } catch (Throwable th) {
                                        th = th;
                                        unlock();
                                        postWriteCleanup();
                                        throw th;
                                    }
                                } else {
                                    referenceEntry2.setValueReference(computingValueReference2);
                                    computingValueReference = computingValueReference2;
                                    referenceEntry3 = referenceEntry2;
                                }
                            }
                            unlock();
                            postWriteCleanup();
                            entry = referenceEntry3;
                            if (z) {
                                return compute(k, i, referenceEntry3, computingValueReference);
                            }
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    }
                    Preconditions.checkState(!Thread.holdsLock(entry), "Recursive computation");
                    waitForValue = entry.getValueReference().waitForValue();
                } finally {
                    postReadCleanup();
                }
            } while (waitForValue == null);
            recordRead(entry);
            return waitForValue;
        }
    }

    /* loaded from: a.zip:com/google/common/collect/ComputingConcurrentHashMap$ComputingSerializationProxy.class */
    static final class ComputingSerializationProxy<K, V> extends MapMakerInternalMap.AbstractSerializationProxy<K, V> {
        private static final long serialVersionUID = 4;
        final Function<? super K, ? extends V> computingFunction;

        ComputingSerializationProxy(MapMakerInternalMap.Strength strength, MapMakerInternalMap.Strength strength2, Equivalence<Object> equivalence, Equivalence<Object> equivalence2, long j, long j2, int i, int i2, MapMaker.RemovalListener<? super K, ? super V> removalListener, ConcurrentMap<K, V> concurrentMap, Function<? super K, ? extends V> function) {
            super(strength, strength2, equivalence, equivalence2, j, j2, i, i2, removalListener, concurrentMap);
            this.computingFunction = function;
        }

        private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
            objectInputStream.defaultReadObject();
            this.delegate = readMapMaker(objectInputStream).makeComputingMap(this.computingFunction);
            readEntries(objectInputStream);
        }

        private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
            objectOutputStream.defaultWriteObject();
            writeMapTo(objectOutputStream);
        }

        Object readResolve() {
            return this.delegate;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/google/common/collect/ComputingConcurrentHashMap$ComputingValueReference.class */
    public static final class ComputingValueReference<K, V> implements MapMakerInternalMap.ValueReference<K, V> {
        @GuardedBy("ComputingValueReference.this")
        volatile MapMakerInternalMap.ValueReference<K, V> computedReference = ComputingConcurrentHashMap.unset();
        final Function<? super K, ? extends V> computingFunction;

        public ComputingValueReference(Function<? super K, ? extends V> function) {
            this.computingFunction = function;
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

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public MapMakerInternalMap.ValueReference<K, V> copyFor(ReferenceQueue<V> referenceQueue, @Nullable V v, MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry) {
            return this;
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
        public boolean isComputingReference() {
            return true;
        }

        void setValueReference(MapMakerInternalMap.ValueReference<K, V> valueReference) {
            synchronized (this) {
                if (this.computedReference == ComputingConcurrentHashMap.UNSET) {
                    this.computedReference = valueReference;
                    notifyAll();
                }
            }
        }

        @Override // com.google.common.collect.MapMakerInternalMap.ValueReference
        public V waitForValue() throws ExecutionException {
            if (this.computedReference == ComputingConcurrentHashMap.UNSET) {
                boolean z = false;
                boolean z2 = false;
                try {
                    synchronized (this) {
                        while (this.computedReference == ComputingConcurrentHashMap.UNSET) {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                z2 = true;
                            }
                        }
                        z = z;
                    }
                } finally {
                    if (z) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            return this.computedReference.waitForValue();
        }
    }

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
    public V getOrCompute(K k) throws ExecutionException {
        int hash = hash(Preconditions.checkNotNull(k));
        return segmentFor(hash).getOrCompute(k, hash, this.computingFunction);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.MapMakerInternalMap
    public ComputingSegment<K, V> segmentFor(int i) {
        return (ComputingSegment) super.segmentFor(i);
    }

    @Override // com.google.common.collect.MapMakerInternalMap
    Object writeReplace() {
        return new ComputingSerializationProxy(this.keyStrength, this.valueStrength, this.keyEquivalence, this.valueEquivalence, this.expireAfterWriteNanos, this.expireAfterAccessNanos, this.maximumSize, this.concurrencyLevel, this.removalListener, this, this.computingFunction);
    }
}
