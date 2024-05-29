package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.util.Set;
import javax.annotation.Nullable;
@GwtCompatible(emulated = true, serializable = true)
/* loaded from: a.zip:com/google/common/collect/SingletonImmutableSet.class */
final class SingletonImmutableSet<E> extends ImmutableSet<E> {
    private transient int cachedHashCode;
    final transient E element;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SingletonImmutableSet(E e) {
        this.element = (E) Preconditions.checkNotNull(e);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public SingletonImmutableSet(E e, int i) {
        this.element = e;
        this.cachedHashCode = i;
    }

    @Override // com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.util.Set
    public boolean contains(Object obj) {
        return this.element.equals(obj);
    }

    @Override // com.google.common.collect.ImmutableCollection
    int copyIntoArray(Object[] objArr, int i) {
        objArr[i] = this.element;
        return i + 1;
    }

    @Override // com.google.common.collect.ImmutableSet, java.util.Collection, java.util.Set
    public boolean equals(@Nullable Object obj) {
        boolean z = false;
        if (obj == this) {
            return true;
        }
        if (obj instanceof Set) {
            Set set = (Set) obj;
            if (set.size() == 1) {
                z = this.element.equals(set.iterator().next());
            }
            return z;
        }
        return false;
    }

    @Override // com.google.common.collect.ImmutableSet, java.util.Collection, java.util.Set
    public final int hashCode() {
        int i = this.cachedHashCode;
        int i2 = i;
        if (i == 0) {
            i2 = this.element.hashCode();
            this.cachedHashCode = i2;
        }
        return i2;
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
    public boolean isEmpty() {
        return false;
    }

    @Override // com.google.common.collect.ImmutableSet
    boolean isHashCodeFast() {
        boolean z = false;
        if (this.cachedHashCode != 0) {
            z = true;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableCollection
    public boolean isPartialView() {
        return false;
    }

    @Override // com.google.common.collect.ImmutableSet, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
    public UnmodifiableIterator<E> iterator() {
        return Iterators.singletonIterator(this.element);
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
    public int size() {
        return 1;
    }

    @Override // java.util.AbstractCollection
    public String toString() {
        String obj = this.element.toString();
        return new StringBuilder(obj.length() + 2).append('[').append(obj).append(']').toString();
    }
}
