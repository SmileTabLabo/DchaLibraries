package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;
@GwtCompatible(emulated = true)
/* loaded from: b.zip:com/google/common/collect/Iterators.class */
public final class Iterators {
    static final UnmodifiableListIterator<Object> EMPTY_LIST_ITERATOR = new UnmodifiableListIterator<Object>() { // from class: com.google.common.collect.Iterators.1
        @Override // java.util.Iterator, java.util.ListIterator
        public boolean hasNext() {
            return false;
        }

        @Override // java.util.ListIterator
        public boolean hasPrevious() {
            return false;
        }

        @Override // java.util.Iterator, java.util.ListIterator
        public Object next() {
            throw new NoSuchElementException();
        }

        @Override // java.util.ListIterator
        public int nextIndex() {
            return 0;
        }

        @Override // java.util.ListIterator
        public Object previous() {
            throw new NoSuchElementException();
        }

        @Override // java.util.ListIterator
        public int previousIndex() {
            return -1;
        }
    };
    private static final Iterator<Object> EMPTY_MODIFIABLE_ITERATOR = new Iterator<Object>() { // from class: com.google.common.collect.Iterators.2
        @Override // java.util.Iterator
        public boolean hasNext() {
            return false;
        }

        @Override // java.util.Iterator
        public Object next() {
            throw new NoSuchElementException();
        }

        @Override // java.util.Iterator
        public void remove() {
            CollectPreconditions.checkRemove(false);
        }
    };

    /* loaded from: b.zip:com/google/common/collect/Iterators$PeekingImpl.class */
    private static class PeekingImpl<E> implements PeekingIterator<E> {
        private boolean hasPeeked;
        private final Iterator<? extends E> iterator;
        private E peekedElement;

        public PeekingImpl(Iterator<? extends E> it) {
            this.iterator = (Iterator) Preconditions.checkNotNull(it);
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return !this.hasPeeked ? this.iterator.hasNext() : true;
        }

        @Override // com.google.common.collect.PeekingIterator, java.util.Iterator
        public E next() {
            if (this.hasPeeked) {
                E e = this.peekedElement;
                this.hasPeeked = false;
                this.peekedElement = null;
                return e;
            }
            return this.iterator.next();
        }

        @Override // com.google.common.collect.PeekingIterator
        public E peek() {
            if (!this.hasPeeked) {
                this.peekedElement = this.iterator.next();
                this.hasPeeked = true;
            }
            return this.peekedElement;
        }

        @Override // java.util.Iterator
        public void remove() {
            Preconditions.checkState(!this.hasPeeked, "Can't remove after you've peeked at next");
            this.iterator.remove();
        }
    }

    private Iterators() {
    }

    public static <T> boolean addAll(Collection<T> collection, Iterator<? extends T> it) {
        Preconditions.checkNotNull(collection);
        Preconditions.checkNotNull(it);
        boolean z = false;
        while (true) {
            boolean z2 = z;
            if (!it.hasNext()) {
                return z2;
            }
            z = z2 | collection.add(it.next());
        }
    }

    public static <T> boolean any(Iterator<T> it, Predicate<? super T> predicate) {
        return indexOf(it, predicate) != -1;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void clear(Iterator<?> it) {
        Preconditions.checkNotNull(it);
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    public static boolean contains(Iterator<?> it, @Nullable Object obj) {
        return any(it, Predicates.equalTo(obj));
    }

    /* JADX WARN: Removed duplicated region for block: B:5:0x000b  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean elementsEqual(Iterator<?> it, Iterator<?> it2) {
        boolean z = false;
        while (it.hasNext()) {
            if (!it2.hasNext() || !Objects.equal(it.next(), it2.next())) {
                return false;
            }
            while (it.hasNext()) {
            }
        }
        if (!it2.hasNext()) {
            z = true;
        }
        return z;
    }

    @Deprecated
    public static <T> UnmodifiableIterator<T> emptyIterator() {
        return emptyListIterator();
    }

    static <T> UnmodifiableListIterator<T> emptyListIterator() {
        return (UnmodifiableListIterator<T>) EMPTY_LIST_ITERATOR;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <T> Iterator<T> emptyModifiableIterator() {
        return (Iterator<T>) EMPTY_MODIFIABLE_ITERATOR;
    }

    public static <T> UnmodifiableIterator<T> forArray(T... tArr) {
        return forArray(tArr, 0, tArr.length, 0);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <T> UnmodifiableListIterator<T> forArray(T[] tArr, int i, int i2, int i3) {
        boolean z = false;
        if (i2 >= 0) {
            z = true;
        }
        Preconditions.checkArgument(z);
        Preconditions.checkPositionIndexes(i, i + i2, tArr.length);
        Preconditions.checkPositionIndex(i3, i2);
        return i2 == 0 ? emptyListIterator() : new AbstractIndexedListIterator<T>(i2, i3, tArr, i) { // from class: com.google.common.collect.Iterators.11
            final Object[] val$array;
            final int val$offset;

            {
                this.val$array = tArr;
                this.val$offset = i;
            }

            @Override // com.google.common.collect.AbstractIndexedListIterator
            protected T get(int i4) {
                return (T) this.val$array[this.val$offset + i4];
            }
        };
    }

    @Nullable
    public static <T> T getNext(Iterator<? extends T> it, @Nullable T t) {
        if (it.hasNext()) {
            t = it.next();
        }
        return t;
    }

    public static <T> T getOnlyElement(Iterator<T> it) {
        T next = it.next();
        if (it.hasNext()) {
            StringBuilder sb = new StringBuilder();
            sb.append("expected one element but was: <").append(next);
            for (int i = 0; i < 4 && it.hasNext(); i++) {
                sb.append(", ").append(it.next());
            }
            if (it.hasNext()) {
                sb.append(", ...");
            }
            sb.append('>');
            throw new IllegalArgumentException(sb.toString());
        }
        return next;
    }

    public static <T> int indexOf(Iterator<T> it, Predicate<? super T> predicate) {
        Preconditions.checkNotNull(predicate, "predicate");
        int i = 0;
        while (it.hasNext()) {
            if (predicate.apply(it.next())) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static <T> PeekingIterator<T> peekingIterator(Iterator<? extends T> it) {
        return it instanceof PeekingImpl ? (PeekingImpl) it : new PeekingImpl(it);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Nullable
    public static <T> T pollNext(Iterator<T> it) {
        if (it.hasNext()) {
            T next = it.next();
            it.remove();
            return next;
        }
        return null;
    }

    public static boolean removeAll(Iterator<?> it, Collection<?> collection) {
        return removeIf(it, Predicates.in(collection));
    }

    public static <T> boolean removeIf(Iterator<T> it, Predicate<? super T> predicate) {
        Preconditions.checkNotNull(predicate);
        boolean z = false;
        while (it.hasNext()) {
            if (predicate.apply(it.next())) {
                it.remove();
                z = true;
            }
        }
        return z;
    }

    public static <T> UnmodifiableIterator<T> singletonIterator(@Nullable T t) {
        return new UnmodifiableIterator<T>(t) { // from class: com.google.common.collect.Iterators.12
            boolean done;
            final Object val$value;

            {
                this.val$value = t;
            }

            @Override // java.util.Iterator
            public boolean hasNext() {
                return !this.done;
            }

            @Override // java.util.Iterator
            public T next() {
                if (this.done) {
                    throw new NoSuchElementException();
                }
                this.done = true;
                return (T) this.val$value;
            }
        };
    }

    public static int size(Iterator<?> it) {
        int i = 0;
        while (it.hasNext()) {
            it.next();
            i++;
        }
        return i;
    }

    public static <F, T> Iterator<T> transform(Iterator<F> it, Function<? super F, ? extends T> function) {
        Preconditions.checkNotNull(function);
        return new TransformedIterator<F, T>(it, function) { // from class: com.google.common.collect.Iterators.8
            final Function val$function;

            {
                this.val$function = function;
            }

            @Override // com.google.common.collect.TransformedIterator
            T transform(F f) {
                return (T) this.val$function.apply(f);
            }
        };
    }

    public static <T> UnmodifiableIterator<T> unmodifiableIterator(Iterator<T> it) {
        Preconditions.checkNotNull(it);
        return it instanceof UnmodifiableIterator ? (UnmodifiableIterator) it : new UnmodifiableIterator<T>(it) { // from class: com.google.common.collect.Iterators.3
            final Iterator val$iterator;

            {
                this.val$iterator = it;
            }

            @Override // java.util.Iterator
            public boolean hasNext() {
                return this.val$iterator.hasNext();
            }

            @Override // java.util.Iterator
            public T next() {
                return (T) this.val$iterator.next();
            }
        };
    }
}
