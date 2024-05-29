package com.android.systemui.recents.events;

import java.lang.ref.WeakReference;
/* compiled from: EventBus.java */
/* loaded from: classes.dex */
class Subscriber {
    private WeakReference<Object> mSubscriber;
    long registrationTime;

    /* JADX INFO: Access modifiers changed from: package-private */
    public Subscriber(Object obj, long j) {
        this.mSubscriber = new WeakReference<>(obj);
        this.registrationTime = j;
    }

    public String toString(int i) {
        Object obj = this.mSubscriber.get();
        String hexString = Integer.toHexString(System.identityHashCode(obj));
        return obj.getClass().getSimpleName() + " [0x" + hexString + ", P" + i + "]";
    }

    public Object getReference() {
        return this.mSubscriber.get();
    }
}
