package com.android.systemui.recents.events;

import java.lang.ref.WeakReference;
/* loaded from: a.zip:com/android/systemui/recents/events/Subscriber.class */
class Subscriber {
    private WeakReference<Object> mSubscriber;
    long registrationTime;

    /* JADX INFO: Access modifiers changed from: package-private */
    public Subscriber(Object obj, long j) {
        this.mSubscriber = new WeakReference<>(obj);
        this.registrationTime = j;
    }

    public Object getReference() {
        return this.mSubscriber.get();
    }

    public String toString(int i) {
        Object obj = this.mSubscriber.get();
        return obj.getClass().getSimpleName() + " [0x" + Integer.toHexString(System.identityHashCode(obj)) + ", P" + i + "]";
    }
}
