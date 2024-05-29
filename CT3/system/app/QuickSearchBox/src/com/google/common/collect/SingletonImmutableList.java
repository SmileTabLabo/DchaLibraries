package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.util.List;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtCompatible(emulated = true, serializable = true)
/* loaded from: a.zip:com/google/common/collect/SingletonImmutableList.class */
public final class SingletonImmutableList<E> extends ImmutableList<E> {
    final transient E element;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SingletonImmutableList(E e) {
        this.element = (E) Preconditions.checkNotNull(e);
    }

    @Override // com.google.common.collect.ImmutableList, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.util.Set
    public boolean contains(@Nullable Object obj) {
        return this.element.equals(obj);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableList, com.google.common.collect.ImmutableCollection
    public int copyIntoArray(Object[] objArr, int i) {
        objArr[i] = this.element;
        return i + 1;
    }

    @Override // com.google.common.collect.ImmutableList, java.util.Collection, java.util.List
    public boolean equals(@Nullable Object obj) {
        boolean z = false;
        if (obj == this) {
            return true;
        }
        if (obj instanceof List) {
            List list = (List) obj;
            if (list.size() == 1) {
                z = this.element.equals(list.get(0));
            }
            return z;
        }
        return false;
    }

    @Override // java.util.List
    public E get(int i) {
        Preconditions.checkElementIndex(i, 1);
        return this.element;
    }

    @Override // com.google.common.collect.ImmutableList, java.util.Collection, java.util.List
    public int hashCode() {
        return this.element.hashCode() + 31;
    }

    @Override // com.google.common.collect.ImmutableList, java.util.List
    public int indexOf(@Nullable Object obj) {
        return this.element.equals(obj) ? 0 : -1;
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
    public boolean isEmpty() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableCollection
    public boolean isPartialView() {
        return false;
    }

    @Override // com.google.common.collect.ImmutableList, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
    public UnmodifiableIterator<E> iterator() {
        return Iterators.singletonIterator(this.element);
    }

    @Override // com.google.common.collect.ImmutableList, java.util.List
    public int lastIndexOf(@Nullable Object obj) {
        return indexOf(obj);
    }

    @Override // com.google.common.collect.ImmutableList
    public ImmutableList<E> reverse() {
        return this;
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
    public int size() {
        return 1;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v4, types: [com.google.common.collect.ImmutableList] */
    @Override // com.google.common.collect.ImmutableList, java.util.List
    public ImmutableList<E> subList(int i, int i2) {
        Preconditions.checkPositionIndexes(i, i2, 1);
        SingletonImmutableList<E> singletonImmutableList = this;
        if (i == i2) {
            singletonImmutableList = ImmutableList.of();
        }
        return singletonImmutableList;
    }

    @Override // java.util.AbstractCollection
    public String toString() {
        String obj = this.element.toString();
        return new StringBuilder(obj.length() + 2).append('[').append(obj).append(']').toString();
    }
}
