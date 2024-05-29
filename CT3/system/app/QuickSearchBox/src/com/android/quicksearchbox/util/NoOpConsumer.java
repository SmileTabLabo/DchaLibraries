package com.android.quicksearchbox.util;
/* loaded from: a.zip:com/android/quicksearchbox/util/NoOpConsumer.class */
public class NoOpConsumer<A> implements Consumer<A> {
    @Override // com.android.quicksearchbox.util.Consumer
    public boolean consume(A a) {
        return false;
    }
}
