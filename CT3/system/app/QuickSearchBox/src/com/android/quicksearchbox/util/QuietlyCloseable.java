package com.android.quicksearchbox.util;

import java.io.Closeable;
/* loaded from: a.zip:com/android/quicksearchbox/util/QuietlyCloseable.class */
public interface QuietlyCloseable extends Closeable {
    @Override // java.io.Closeable, java.lang.AutoCloseable
    void close();
}
