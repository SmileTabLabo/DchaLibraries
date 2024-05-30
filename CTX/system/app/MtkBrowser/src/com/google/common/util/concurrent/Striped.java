package com.google.common.util.concurrent;

import com.google.common.base.Supplier;
import java.util.concurrent.locks.ReadWriteLock;
/* loaded from: classes.dex */
public abstract class Striped<L> {
    private static final Supplier<ReadWriteLock> READ_WRITE_LOCK_SUPPLIER = new Supplier<ReadWriteLock>() { // from class: com.google.common.util.concurrent.Striped.5
    };

    /* loaded from: classes.dex */
    static class LargeLazyStriped<L> extends PowerOfTwoStriped<L> {
    }

    /* loaded from: classes.dex */
    private static abstract class PowerOfTwoStriped<L> extends Striped<L> {
    }

    /* loaded from: classes.dex */
    static class SmallLazyStriped<L> extends PowerOfTwoStriped<L> {
    }
}
