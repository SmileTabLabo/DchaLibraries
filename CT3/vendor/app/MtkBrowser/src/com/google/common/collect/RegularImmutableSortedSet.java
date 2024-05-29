package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.SortedLists;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtCompatible(emulated = true, serializable = true)
/* loaded from: b.zip:com/google/common/collect/RegularImmutableSortedSet.class */
public final class RegularImmutableSortedSet<E> extends ImmutableSortedSet<E> {
    private final transient ImmutableList<E> elements;

    /* JADX INFO: Access modifiers changed from: package-private */
    public RegularImmutableSortedSet(ImmutableList<E> immutableList, Comparator<? super E> comparator) {
        super(comparator);
        this.elements = immutableList;
        Preconditions.checkArgument(!immutableList.isEmpty());
    }

    private int unsafeBinarySearch(Object obj) throws ClassCastException {
        return Collections.binarySearch(this.elements, obj, unsafeComparator());
    }

    @Override // com.google.common.collect.ImmutableSortedSet, java.util.NavigableSet
    public E ceiling(E e) {
        int tailIndex = tailIndex(e, true);
        return tailIndex == size() ? null : this.elements.get(tailIndex);
    }

    @Override // com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.util.Set
    public boolean contains(Object obj) {
        boolean z = false;
        if (obj != null) {
            try {
                z = false;
                if (unsafeBinarySearch(obj) >= 0) {
                    z = true;
                }
            } catch (ClassCastException e) {
                return false;
            }
        }
        return z;
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
    public boolean containsAll(Collection<?> collection) {
        Set<E> set = collection;
        if (collection instanceof Multiset) {
            set = ((Multiset) collection).elementSet();
        }
        if (!SortedIterables.hasSameComparator(comparator(), set) || set.size() <= 1) {
            return super.containsAll(set);
        }
        PeekingIterator peekingIterator = Iterators.peekingIterator(iterator());
        Iterator<?> it = set.iterator();
        Object next = it.next();
        while (peekingIterator.hasNext()) {
            try {
                int unsafeCompare = unsafeCompare(peekingIterator.peek(), next);
                if (unsafeCompare < 0) {
                    peekingIterator.next();
                } else if (unsafeCompare == 0) {
                    if (!it.hasNext()) {
                        return true;
                    }
                    next = it.next();
                } else if (unsafeCompare > 0) {
                    return false;
                }
            } catch (ClassCastException e) {
                return false;
            } catch (NullPointerException e2) {
                return false;
            }
        }
        return false;
    }

    @Override // com.google.common.collect.ImmutableCollection
    int copyIntoArray(Object[] objArr, int i) {
        return this.elements.copyIntoArray(objArr, i);
    }

    @Override // com.google.common.collect.ImmutableCollection
    ImmutableList<E> createAsList() {
        return new ImmutableSortedAsList(this, this.elements);
    }

    @Override // com.google.common.collect.ImmutableSortedSet
    ImmutableSortedSet<E> createDescendingSet() {
        return new RegularImmutableSortedSet(this.elements.reverse(), Ordering.from(this.comparator).reverse());
    }

    @Override // com.google.common.collect.ImmutableSortedSet, java.util.NavigableSet
    @GwtIncompatible("NavigableSet")
    public UnmodifiableIterator<E> descendingIterator() {
        return this.elements.reverse().iterator();
    }

    @Override // com.google.common.collect.ImmutableSet, java.util.Collection, java.util.Set
    public boolean equals(@Nullable Object obj) {
        E next;
        E next2;
        if (obj == this) {
            return true;
        }
        if (obj instanceof Set) {
            Set set = (Set) obj;
            if (size() != set.size()) {
                return false;
            }
            if (SortedIterables.hasSameComparator(this.comparator, set)) {
                Iterator<E> it = set.iterator();
                try {
                    UnmodifiableIterator<E> it2 = iterator();
                    do {
                        if (!it2.hasNext()) {
                            return true;
                        }
                        next = it2.next();
                        next2 = it.next();
                        if (next2 == null) {
                            return false;
                        }
                    } while (unsafeCompare(next, next2) == 0);
                    return false;
                } catch (ClassCastException e) {
                    return false;
                } catch (NoSuchElementException e2) {
                    return false;
                }
            }
            return containsAll(set);
        }
        return false;
    }

    @Override // com.google.common.collect.ImmutableSortedSet, java.util.SortedSet
    public E first() {
        return this.elements.get(0);
    }

    @Override // com.google.common.collect.ImmutableSortedSet, java.util.NavigableSet
    public E floor(E e) {
        int headIndex = headIndex(e, true) - 1;
        return headIndex == -1 ? null : this.elements.get(headIndex);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ImmutableSortedSet<E> getSubSet(int i, int i2) {
        return (i == 0 && i2 == size()) ? this : i < i2 ? new RegularImmutableSortedSet(this.elements.subList(i, i2), this.comparator) : emptySet(this.comparator);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int headIndex(E e, boolean z) {
        return SortedLists.binarySearch(this.elements, Preconditions.checkNotNull(e), comparator(), z ? SortedLists.KeyPresentBehavior.FIRST_AFTER : SortedLists.KeyPresentBehavior.FIRST_PRESENT, SortedLists.KeyAbsentBehavior.NEXT_HIGHER);
    }

    @Override // com.google.common.collect.ImmutableSortedSet
    ImmutableSortedSet<E> headSetImpl(E e, boolean z) {
        return getSubSet(0, headIndex(e, z));
    }

    @Override // com.google.common.collect.ImmutableSortedSet, java.util.NavigableSet
    public E higher(E e) {
        int tailIndex = tailIndex(e, false);
        return tailIndex == size() ? null : this.elements.get(tailIndex);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableSortedSet
    public int indexOf(@Nullable Object obj) {
        if (obj == null) {
            return -1;
        }
        try {
            int binarySearch = SortedLists.binarySearch(this.elements, obj, unsafeComparator(), SortedLists.KeyPresentBehavior.ANY_PRESENT, SortedLists.KeyAbsentBehavior.INVERTED_INSERTION_INDEX);
            if (binarySearch < 0) {
                binarySearch = -1;
            }
            return binarySearch;
        } catch (ClassCastException e) {
            return -1;
        }
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
    public boolean isEmpty() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableCollection
    public boolean isPartialView() {
        return this.elements.isPartialView();
    }

    @Override // com.google.common.collect.ImmutableSortedSet, com.google.common.collect.ImmutableSet, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
    public UnmodifiableIterator<E> iterator() {
        return this.elements.iterator();
    }

    @Override // com.google.common.collect.ImmutableSortedSet, java.util.SortedSet
    public E last() {
        return this.elements.get(size() - 1);
    }

    @Override // com.google.common.collect.ImmutableSortedSet, java.util.NavigableSet
    public E lower(E e) {
        int headIndex = headIndex(e, false) - 1;
        return headIndex == -1 ? null : this.elements.get(headIndex);
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
    public int size() {
        return this.elements.size();
    }

    @Override // com.google.common.collect.ImmutableSortedSet
    ImmutableSortedSet<E> subSetImpl(E e, boolean z, E e2, boolean z2) {
        return tailSetImpl(e, z).headSetImpl(e2, z2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int tailIndex(E e, boolean z) {
        return SortedLists.binarySearch(this.elements, Preconditions.checkNotNull(e), comparator(), z ? SortedLists.KeyPresentBehavior.FIRST_PRESENT : SortedLists.KeyPresentBehavior.FIRST_AFTER, SortedLists.KeyAbsentBehavior.NEXT_HIGHER);
    }

    @Override // com.google.common.collect.ImmutableSortedSet
    ImmutableSortedSet<E> tailSetImpl(E e, boolean z) {
        return getSubSet(tailIndex(e, z), size());
    }

    Comparator<Object> unsafeComparator() {
        return this.comparator;
    }
}
