package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
@GwtCompatible
/* loaded from: b.zip:com/google/common/collect/Ordering.class */
public abstract class Ordering<T> implements Comparator<T> {

    @VisibleForTesting
    /* loaded from: b.zip:com/google/common/collect/Ordering$ArbitraryOrdering.class */
    static class ArbitraryOrdering extends Ordering<Object> {
        private Map<Object, Integer> uids = Platform.tryWeakKeys(new MapMaker()).makeComputingMap(new Function<Object, Integer>(this) { // from class: com.google.common.collect.Ordering.ArbitraryOrdering.1
            final AtomicInteger counter = new AtomicInteger(0);
            final ArbitraryOrdering this$1;

            {
                this.this$1 = this;
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // com.google.common.base.Function
            public Integer apply(Object obj) {
                return Integer.valueOf(this.counter.getAndIncrement());
            }
        });

        ArbitraryOrdering() {
        }

        @Override // com.google.common.collect.Ordering, java.util.Comparator
        public int compare(Object obj, Object obj2) {
            int i = -1;
            if (obj == obj2) {
                return 0;
            }
            if (obj == null) {
                return -1;
            }
            if (obj2 == null) {
                return 1;
            }
            int identityHashCode = identityHashCode(obj);
            int identityHashCode2 = identityHashCode(obj2);
            if (identityHashCode != identityHashCode2) {
                if (identityHashCode >= identityHashCode2) {
                    i = 1;
                }
                return i;
            }
            int compareTo = this.uids.get(obj).compareTo(this.uids.get(obj2));
            if (compareTo == 0) {
                throw new AssertionError();
            }
            return compareTo;
        }

        int identityHashCode(Object obj) {
            return System.identityHashCode(obj);
        }

        public String toString() {
            return "Ordering.arbitrary()";
        }
    }

    @VisibleForTesting
    /* loaded from: b.zip:com/google/common/collect/Ordering$IncomparableValueException.class */
    static class IncomparableValueException extends ClassCastException {
        private static final long serialVersionUID = 0;
        final Object value;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v5, types: [com.google.common.collect.Ordering] */
    @GwtCompatible(serializable = true)
    public static <T> Ordering<T> from(Comparator<T> comparator) {
        return comparator instanceof Ordering ? (Ordering) comparator : new ComparatorOrdering(comparator);
    }

    @GwtCompatible(serializable = true)
    public static <C extends Comparable> Ordering<C> natural() {
        return NaturalOrdering.INSTANCE;
    }

    @GwtCompatible(serializable = true)
    public static Ordering<Object> usingToString() {
        return UsingToStringOrdering.INSTANCE;
    }

    @Override // java.util.Comparator
    public abstract int compare(@Nullable T t, @Nullable T t2);

    /* JADX INFO: Access modifiers changed from: package-private */
    public <T2 extends T> Ordering<Map.Entry<T2, ?>> onKeys() {
        return (Ordering<Map.Entry<T2, ?>>) onResultOf(Maps.keyFunction());
    }

    @GwtCompatible(serializable = true)
    public <F> Ordering<F> onResultOf(Function<F, ? extends T> function) {
        return new ByFunctionOrdering(function, this);
    }

    @GwtCompatible(serializable = true)
    public <S extends T> Ordering<S> reverse() {
        return new ReverseOrdering(this);
    }
}
