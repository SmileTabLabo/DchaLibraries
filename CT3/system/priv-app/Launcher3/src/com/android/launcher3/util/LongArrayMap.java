package com.android.launcher3.util;

import android.util.LongSparseArray;
import java.util.Iterator;
/* loaded from: a.zip:com/android/launcher3/util/LongArrayMap.class */
public class LongArrayMap<E> extends LongSparseArray<E> implements Iterable<E> {

    /* loaded from: a.zip:com/android/launcher3/util/LongArrayMap$ValueIterator.class */
    class ValueIterator implements Iterator<E> {
        private int mNextIndex = 0;
        final LongArrayMap this$0;

        ValueIterator(LongArrayMap longArrayMap) {
            this.this$0 = longArrayMap;
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.mNextIndex < this.this$0.size();
        }

        @Override // java.util.Iterator
        public E next() {
            LongArrayMap longArrayMap = this.this$0;
            int i = this.mNextIndex;
            this.mNextIndex = i + 1;
            return longArrayMap.valueAt(i);
        }

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override // android.util.LongSparseArray
    public LongArrayMap<E> clone() {
        return (LongArrayMap) super.clone();
    }

    public boolean containsKey(long j) {
        boolean z = false;
        if (indexOfKey(j) >= 0) {
            z = true;
        }
        return z;
    }

    public boolean isEmpty() {
        boolean z = false;
        if (size() <= 0) {
            z = true;
        }
        return z;
    }

    @Override // java.lang.Iterable
    public Iterator<E> iterator() {
        return new ValueIterator(this);
    }
}
