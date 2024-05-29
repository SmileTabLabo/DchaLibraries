package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import java.util.Comparator;
import java.util.Iterator;
import javax.annotation.Nullable;
@GwtCompatible(emulated = true)
/* loaded from: a.zip:com/google/common/collect/ImmutableSortedAsList.class */
final class ImmutableSortedAsList<E> extends RegularImmutableAsList<E> implements SortedIterable<E> {
    /* JADX INFO: Access modifiers changed from: package-private */
    public ImmutableSortedAsList(ImmutableSortedSet<E> immutableSortedSet, ImmutableList<E> immutableList) {
        super(immutableSortedSet, immutableList);
    }

    @Override // com.google.common.collect.SortedIterable
    public Comparator<? super E> comparator() {
        return delegateCollection().comparator();
    }

    @Override // com.google.common.collect.ImmutableAsList, com.google.common.collect.ImmutableList, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.util.Set
    public boolean contains(Object obj) {
        boolean z = false;
        if (indexOf(obj) >= 0) {
            z = true;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.RegularImmutableAsList, com.google.common.collect.ImmutableAsList
    public ImmutableSortedSet<E> delegateCollection() {
        return (ImmutableSortedSet) super.delegateCollection();
    }

    @Override // com.google.common.collect.ImmutableList, java.util.List
    @GwtIncompatible("ImmutableSortedSet.indexOf")
    public int indexOf(@Nullable Object obj) {
        int indexOf = delegateCollection().indexOf(obj);
        if (indexOf < 0 || !get(indexOf).equals(obj)) {
            indexOf = -1;
        }
        return indexOf;
    }

    @Override // com.google.common.collect.ImmutableList, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
    public /* bridge */ /* synthetic */ Iterator iterator() {
        return iterator();
    }

    @Override // com.google.common.collect.ImmutableList, java.util.List
    @GwtIncompatible("ImmutableSortedSet.indexOf")
    public int lastIndexOf(@Nullable Object obj) {
        return indexOf(obj);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableList
    @GwtIncompatible("super.subListUnchecked does not exist; inherited subList is valid if slow")
    public ImmutableList<E> subListUnchecked(int i, int i2) {
        return new RegularImmutableSortedSet(super.subListUnchecked(i, i2), comparator()).asList();
    }
}
