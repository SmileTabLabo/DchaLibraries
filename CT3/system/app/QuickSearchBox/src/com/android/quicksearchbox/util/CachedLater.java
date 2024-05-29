package com.android.quicksearchbox.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/* loaded from: a.zip:com/android/quicksearchbox/util/CachedLater.class */
public abstract class CachedLater<A> implements NowOrLater<A> {
    private boolean mCreating;
    private final Object mLock = new Object();
    private boolean mValid;
    private A mValue;
    private List<Consumer<? super A>> mWaitingConsumers;

    protected abstract void create();

    @Override // com.android.quicksearchbox.util.NowOrLater
    public void getLater(Consumer<? super A> consumer) {
        boolean z;
        Object obj;
        synchronized (this.mLock) {
            z = this.mValid;
            obj = (A) this.mValue;
            if (!z) {
                if (this.mWaitingConsumers == null) {
                    this.mWaitingConsumers = new ArrayList();
                }
                this.mWaitingConsumers.add(consumer);
            }
        }
        if (z) {
            consumer.consume(obj);
            return;
        }
        boolean z2 = false;
        synchronized (this.mLock) {
            if (!this.mCreating) {
                this.mCreating = true;
                z2 = true;
            }
        }
        if (z2) {
            create();
        }
    }

    @Override // com.android.quicksearchbox.util.NowOrLater
    public A getNow() {
        A a;
        synchronized (this) {
            synchronized (this.mLock) {
                if (!haveNow()) {
                    throw new IllegalStateException("getNow() called when haveNow() is false");
                }
                a = this.mValue;
            }
        }
        return a;
    }

    @Override // com.android.quicksearchbox.util.NowOrLater
    public boolean haveNow() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mValid;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void store(A a) {
        List<Consumer<? super A>> list;
        synchronized (this.mLock) {
            this.mValue = a;
            this.mValid = true;
            this.mCreating = false;
            list = this.mWaitingConsumers;
            this.mWaitingConsumers = null;
        }
        if (list != null) {
            Iterator<T> it = list.iterator();
            while (it.hasNext()) {
                ((Consumer) it.next()).consume(a);
            }
        }
    }
}
