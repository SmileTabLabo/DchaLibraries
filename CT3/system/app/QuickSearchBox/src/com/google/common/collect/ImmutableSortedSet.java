package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.SortedSet;
import javax.annotation.Nullable;
@GwtCompatible(emulated = true, serializable = true)
/* loaded from: a.zip:com/google/common/collect/ImmutableSortedSet.class */
public abstract class ImmutableSortedSet<E> extends ImmutableSortedSetFauxverideShim<E> implements NavigableSet<E>, SortedIterable<E> {
    final transient Comparator<? super E> comparator;
    @GwtIncompatible("NavigableSet")
    transient ImmutableSortedSet<E> descendingSet;
    private static final Comparator<Comparable> NATURAL_ORDER = Ordering.natural();
    private static final ImmutableSortedSet<Comparable> NATURAL_EMPTY_SET = new EmptyImmutableSortedSet(NATURAL_ORDER);

    /* loaded from: a.zip:com/google/common/collect/ImmutableSortedSet$Builder.class */
    public static final class Builder<E> extends ImmutableSet.Builder<E> {
        private final Comparator<? super E> comparator;

        public Builder(Comparator<? super E> comparator) {
            this.comparator = (Comparator) Preconditions.checkNotNull(comparator);
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // com.google.common.collect.ImmutableSet.Builder, com.google.common.collect.ImmutableCollection.ArrayBasedBuilder, com.google.common.collect.ImmutableCollection.Builder
        public /* bridge */ /* synthetic */ ImmutableSet.Builder add(Object obj) {
            return add((Builder<E>) obj);
        }

        @Override // com.google.common.collect.ImmutableSet.Builder, com.google.common.collect.ImmutableCollection.ArrayBasedBuilder, com.google.common.collect.ImmutableCollection.Builder
        public Builder<E> add(E e) {
            super.add((Builder<E>) e);
            return this;
        }

        @Override // com.google.common.collect.ImmutableSet.Builder, com.google.common.collect.ImmutableCollection.ArrayBasedBuilder
        public Builder<E> add(E... eArr) {
            super.add((Object[]) eArr);
            return this;
        }

        @Override // com.google.common.collect.ImmutableSet.Builder, com.google.common.collect.ImmutableCollection.ArrayBasedBuilder, com.google.common.collect.ImmutableCollection.Builder
        public Builder<E> addAll(Iterable<? extends E> iterable) {
            super.addAll((Iterable) iterable);
            return this;
        }

        @Override // com.google.common.collect.ImmutableSet.Builder
        public ImmutableSortedSet<E> build() {
            ImmutableSortedSet<E> construct = ImmutableSortedSet.construct(this.comparator, this.size, this.contents);
            this.size = construct.size();
            return construct;
        }
    }

    /* loaded from: a.zip:com/google/common/collect/ImmutableSortedSet$SerializedForm.class */
    private static class SerializedForm<E> implements Serializable {
        private static final long serialVersionUID = 0;
        final Comparator<? super E> comparator;
        final Object[] elements;

        public SerializedForm(Comparator<? super E> comparator, Object[] objArr) {
            this.comparator = comparator;
            this.elements = objArr;
        }

        /* JADX WARN: Multi-variable type inference failed */
        Object readResolve() {
            return new Builder(this.comparator).add(this.elements).build();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ImmutableSortedSet(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }

    /* JADX WARN: Multi-variable type inference failed */
    static <E> ImmutableSortedSet<E> construct(Comparator<? super E> comparator, int i, E... eArr) {
        if (i == 0) {
            return emptySet(comparator);
        }
        ObjectArrays.checkElementsNotNull(eArr, i);
        Arrays.sort(eArr, 0, i, comparator);
        int i2 = 1;
        for (int i3 = 1; i3 < i; i3++) {
            Object obj = (Object) eArr[i3];
            if (comparator.compare(obj, (Object) eArr[i2 - 1]) != 0) {
                eArr[i2] = obj;
                i2++;
            }
        }
        Arrays.fill(eArr, i2, i, (Object) null);
        return new RegularImmutableSortedSet(ImmutableList.asImmutableList(eArr, i2), comparator);
    }

    private static <E> ImmutableSortedSet<E> emptySet() {
        return (ImmutableSortedSet<E>) NATURAL_EMPTY_SET;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <E> ImmutableSortedSet<E> emptySet(Comparator<? super E> comparator) {
        return NATURAL_ORDER.equals(comparator) ? emptySet() : new EmptyImmutableSortedSet(comparator);
    }

    private void readObject(ObjectInputStream objectInputStream) throws InvalidObjectException {
        throw new InvalidObjectException("Use SerializedForm");
    }

    static int unsafeCompare(Comparator<?> comparator, Object obj, Object obj2) {
        return comparator.compare(obj, obj2);
    }

    @GwtIncompatible("NavigableSet")
    public E ceiling(E e) {
        return (E) Iterables.getFirst(tailSet((ImmutableSortedSet<E>) e, true), null);
    }

    @Override // java.util.SortedSet, com.google.common.collect.SortedIterable
    public Comparator<? super E> comparator() {
        return this.comparator;
    }

    @GwtIncompatible("NavigableSet")
    ImmutableSortedSet<E> createDescendingSet() {
        return new DescendingImmutableSortedSet(this);
    }

    @Override // java.util.NavigableSet
    @GwtIncompatible("NavigableSet")
    public abstract UnmodifiableIterator<E> descendingIterator();

    @Override // java.util.NavigableSet
    @GwtIncompatible("NavigableSet")
    public ImmutableSortedSet<E> descendingSet() {
        ImmutableSortedSet<E> immutableSortedSet = this.descendingSet;
        ImmutableSortedSet<E> immutableSortedSet2 = immutableSortedSet;
        if (immutableSortedSet == null) {
            immutableSortedSet2 = createDescendingSet();
            this.descendingSet = immutableSortedSet2;
            immutableSortedSet2.descendingSet = this;
        }
        return immutableSortedSet2;
    }

    public E first() {
        return iterator().next();
    }

    @GwtIncompatible("NavigableSet")
    public E floor(E e) {
        return (E) Iterators.getNext(headSet((ImmutableSortedSet<E>) e, true).descendingIterator(), null);
    }

    @Override // java.util.NavigableSet, java.util.SortedSet
    public ImmutableSortedSet<E> headSet(E e) {
        return headSet((ImmutableSortedSet<E>) e, false);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.NavigableSet
    @GwtIncompatible("NavigableSet")
    public ImmutableSortedSet<E> headSet(E e, boolean z) {
        return headSetImpl(Preconditions.checkNotNull(e), z);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.NavigableSet
    public /* bridge */ /* synthetic */ NavigableSet headSet(Object obj, boolean z) {
        return headSet((ImmutableSortedSet<E>) obj, z);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.NavigableSet, java.util.SortedSet
    public /* bridge */ /* synthetic */ SortedSet headSet(Object obj) {
        return headSet((ImmutableSortedSet<E>) obj);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract ImmutableSortedSet<E> headSetImpl(E e, boolean z);

    @GwtIncompatible("NavigableSet")
    public E higher(E e) {
        return (E) Iterables.getFirst(tailSet((ImmutableSortedSet<E>) e, false), null);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract int indexOf(@Nullable Object obj);

    @Override // com.google.common.collect.ImmutableSet, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
    public abstract UnmodifiableIterator<E> iterator();

    public E last() {
        return descendingIterator().next();
    }

    @GwtIncompatible("NavigableSet")
    public E lower(E e) {
        return (E) Iterators.getNext(headSet((ImmutableSortedSet<E>) e, false).descendingIterator(), null);
    }

    @Override // java.util.NavigableSet
    @GwtIncompatible("NavigableSet")
    @Deprecated
    public final E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.NavigableSet
    @GwtIncompatible("NavigableSet")
    @Deprecated
    public final E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.NavigableSet, java.util.SortedSet
    public ImmutableSortedSet<E> subSet(E e, E e2) {
        return subSet((boolean) e, true, (boolean) e2, false);
    }

    @Override // java.util.NavigableSet
    @GwtIncompatible("NavigableSet")
    public ImmutableSortedSet<E> subSet(E e, boolean z, E e2, boolean z2) {
        boolean z3 = false;
        Preconditions.checkNotNull(e);
        Preconditions.checkNotNull(e2);
        if (this.comparator.compare(e, e2) <= 0) {
            z3 = true;
        }
        Preconditions.checkArgument(z3);
        return subSetImpl(e, z, e2, z2);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.NavigableSet
    public /* bridge */ /* synthetic */ NavigableSet subSet(Object obj, boolean z, Object obj2, boolean z2) {
        return subSet((boolean) obj, z, (boolean) obj2, z2);
    }

    abstract ImmutableSortedSet<E> subSetImpl(E e, boolean z, E e2, boolean z2);

    @Override // java.util.NavigableSet, java.util.SortedSet
    public ImmutableSortedSet<E> tailSet(E e) {
        return tailSet((ImmutableSortedSet<E>) e, true);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.NavigableSet
    @GwtIncompatible("NavigableSet")
    public ImmutableSortedSet<E> tailSet(E e, boolean z) {
        return tailSetImpl(Preconditions.checkNotNull(e), z);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.NavigableSet
    public /* bridge */ /* synthetic */ NavigableSet tailSet(Object obj, boolean z) {
        return tailSet((ImmutableSortedSet<E>) obj, z);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.NavigableSet, java.util.SortedSet
    public /* bridge */ /* synthetic */ SortedSet tailSet(Object obj) {
        return tailSet((ImmutableSortedSet<E>) obj);
    }

    abstract ImmutableSortedSet<E> tailSetImpl(E e, boolean z);

    /* JADX INFO: Access modifiers changed from: package-private */
    public int unsafeCompare(Object obj, Object obj2) {
        return unsafeCompare(this.comparator, obj, obj2);
    }

    @Override // com.google.common.collect.ImmutableSet, com.google.common.collect.ImmutableCollection
    Object writeReplace() {
        return new SerializedForm(this.comparator, toArray());
    }
}
