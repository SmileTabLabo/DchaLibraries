package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.util.NoSuchElementException;
@GwtCompatible
/* loaded from: b.zip:com/google/common/collect/AbstractIterator.class */
public abstract class AbstractIterator<T> extends UnmodifiableIterator<T> {

    /* renamed from: -com-google-common-collect-AbstractIterator$StateSwitchesValues  reason: not valid java name */
    private static final int[] f7comgooglecommoncollectAbstractIterator$StateSwitchesValues = null;
    private T next;
    private State state = State.NOT_READY;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/google/common/collect/AbstractIterator$State.class */
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

    /* renamed from: -getcom-google-common-collect-AbstractIterator$StateSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m386x50f87187() {
        if (f7comgooglecommoncollectAbstractIterator$StateSwitchesValues != null) {
            return f7comgooglecommoncollectAbstractIterator$StateSwitchesValues;
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
        f7comgooglecommoncollectAbstractIterator$StateSwitchesValues = iArr;
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
        switch (m386x50f87187()[this.state.ordinal()]) {
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
}
