package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import java.util.Iterator;
import java.util.NoSuchElementException;
@GwtCompatible
/* loaded from: a.zip:com/google/common/base/AbstractIterator.class */
abstract class AbstractIterator<T> implements Iterator<T> {

    /* renamed from: -com-google-common-base-AbstractIterator$StateSwitchesValues  reason: not valid java name */
    private static final int[] f0comgooglecommonbaseAbstractIterator$StateSwitchesValues = null;
    private T next;
    private State state = State.NOT_READY;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/google/common/base/AbstractIterator$State.class */
    public enum State {
        READY,
        NOT_READY,
        DONE,
        FAILED;

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static State[] valuesCustom() {
            return values();
        }
    }

    /* renamed from: -getcom-google-common-base-AbstractIterator$StateSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m31getcomgooglecommonbaseAbstractIterator$StateSwitchesValues() {
        if (f0comgooglecommonbaseAbstractIterator$StateSwitchesValues != null) {
            return f0comgooglecommonbaseAbstractIterator$StateSwitchesValues;
        }
        int[] iArr = new int[State.valuesCustom().length];
        try {
            iArr[State.DONE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.FAILED.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.NOT_READY.ordinal()] = 4;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.READY.ordinal()] = 2;
        } catch (NoSuchFieldError e4) {
        }
        f0comgooglecommonbaseAbstractIterator$StateSwitchesValues = iArr;
        return iArr;
    }

    private boolean tryToComputeNext() {
        this.state = State.FAILED;
        this.next = computeNext();
        if (this.state != State.DONE) {
            this.state = State.READY;
            return true;
        }
        return false;
    }

    protected abstract T computeNext();

    /* JADX INFO: Access modifiers changed from: protected */
    public final T endOfData() {
        this.state = State.DONE;
        return null;
    }

    @Override // java.util.Iterator
    public final boolean hasNext() {
        Preconditions.checkState(this.state != State.FAILED);
        switch (m31getcomgooglecommonbaseAbstractIterator$StateSwitchesValues()[this.state.ordinal()]) {
            case 1:
                return false;
            case 2:
                return true;
            default:
                return tryToComputeNext();
        }
    }

    @Override // java.util.Iterator
    public final T next() {
        if (hasNext()) {
            this.state = State.NOT_READY;
            T t = this.next;
            this.next = null;
            return t;
        }
        throw new NoSuchElementException();
    }

    @Override // java.util.Iterator
    public final void remove() {
        throw new UnsupportedOperationException();
    }
}
