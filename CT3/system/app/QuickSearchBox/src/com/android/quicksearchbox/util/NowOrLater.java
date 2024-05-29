package com.android.quicksearchbox.util;
/* loaded from: a.zip:com/android/quicksearchbox/util/NowOrLater.class */
public interface NowOrLater<C> {
    void getLater(Consumer<? super C> consumer);

    C getNow();

    boolean haveNow();
}
