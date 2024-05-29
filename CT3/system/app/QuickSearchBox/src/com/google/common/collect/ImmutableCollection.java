package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import javax.annotation.Nullable;
@GwtCompatible(emulated = true)
/* loaded from: a.zip:com/google/common/collect/ImmutableCollection.class */
public abstract class ImmutableCollection<E> extends AbstractCollection<E> implements Serializable {
    private transient ImmutableList<E> asList;

    /* loaded from: a.zip:com/google/common/collect/ImmutableCollection$ArrayBasedBuilder.class */
    static abstract class ArrayBasedBuilder<E> extends Builder<E> {
        Object[] contents;
        int size;

        /* JADX INFO: Access modifiers changed from: package-private */
        public ArrayBasedBuilder(int i) {
            CollectPreconditions.checkNonnegative(i, "initialCapacity");
            this.contents = new Object[i];
            this.size = 0;
        }

        private void ensureCapacity(int i) {
            if (this.contents.length < i) {
                this.contents = ObjectArrays.arraysCopyOf(this.contents, expandedCapacity(this.contents.length, i));
            }
        }

        @Override // com.google.common.collect.ImmutableCollection.Builder
        public ArrayBasedBuilder<E> add(E e) {
            Preconditions.checkNotNull(e);
            ensureCapacity(this.size + 1);
            Object[] objArr = this.contents;
            int i = this.size;
            this.size = i + 1;
            objArr[i] = e;
            return this;
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // com.google.common.collect.ImmutableCollection.Builder
        public /* bridge */ /* synthetic */ Builder add(Object obj) {
            return add((ArrayBasedBuilder<E>) obj);
        }

        public Builder<E> add(E... eArr) {
            ObjectArrays.checkElementsNotNull(eArr);
            ensureCapacity(this.size + eArr.length);
            System.arraycopy(eArr, 0, this.contents, this.size, eArr.length);
            this.size += eArr.length;
            return this;
        }

        @Override // com.google.common.collect.ImmutableCollection.Builder
        public Builder<E> addAll(Iterable<? extends E> iterable) {
            if (iterable instanceof Collection) {
                ensureCapacity(this.size + ((Collection) iterable).size());
            }
            super.addAll(iterable);
            return this;
        }
    }

    /* loaded from: a.zip:com/google/common/collect/ImmutableCollection$Builder.class */
    public static abstract class Builder<E> {
        Builder() {
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static int expandedCapacity(int i, int i2) {
            if (i2 < 0) {
                throw new AssertionError("cannot store more than MAX_VALUE elements");
            }
            int i3 = (i >> 1) + i + 1;
            int i4 = i3;
            if (i3 < i2) {
                i4 = Integer.highestOneBit(i2 - 1) << 1;
            }
            int i5 = i4;
            if (i4 < 0) {
                i5 = Integer.MAX_VALUE;
            }
            return i5;
        }

        public abstract Builder<E> add(E e);

        public Builder<E> addAll(Iterable<? extends E> iterable) {
            for (E e : iterable) {
                add(e);
            }
            return this;
        }
    }

    @Override // java.util.AbstractCollection, java.util.Collection
    @Deprecated
    public final boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.AbstractCollection, java.util.Collection
    @Deprecated
    public final boolean addAll(Collection<? extends E> collection) {
        throw new UnsupportedOperationException();
    }

    public ImmutableList<E> asList() {
        ImmutableList<E> immutableList = this.asList;
        ImmutableList<E> immutableList2 = immutableList;
        if (immutableList == null) {
            immutableList2 = createAsList();
            this.asList = immutableList2;
        }
        return immutableList2;
    }

    @Override // java.util.AbstractCollection, java.util.Collection
    @Deprecated
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
    public boolean contains(@Nullable Object obj) {
        return obj != null ? super.contains(obj) : false;
    }

    int copyIntoArray(Object[] objArr, int i) {
        for (E e : this) {
            objArr[i] = e;
            i++;
        }
        return i;
    }

    ImmutableList<E> createAsList() {
        switch (size()) {
            case 0:
                return ImmutableList.of();
            case 1:
                return ImmutableList.of((Object) iterator().next());
            default:
                return new RegularImmutableAsList(this, toArray());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract boolean isPartialView();

    @Override // java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
    public abstract UnmodifiableIterator<E> iterator();

    @Override // java.util.AbstractCollection, java.util.Collection
    @Deprecated
    public final boolean remove(Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.AbstractCollection, java.util.Collection
    @Deprecated
    public final boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.AbstractCollection, java.util.Collection
    @Deprecated
    public final boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.AbstractCollection, java.util.Collection
    public final Object[] toArray() {
        int size = size();
        if (size == 0) {
            return ObjectArrays.EMPTY_ARRAY;
        }
        Object[] objArr = new Object[size];
        copyIntoArray(objArr, 0);
        return objArr;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v15, types: [java.lang.Object[]] */
    @Override // java.util.AbstractCollection, java.util.Collection
    public final <T> T[] toArray(T[] tArr) {
        T[] tArr2;
        Preconditions.checkNotNull(tArr);
        int size = size();
        if (tArr.length < size) {
            tArr2 = ObjectArrays.newArray(tArr, size);
        } else {
            tArr2 = tArr;
            if (tArr.length > size) {
                tArr[size] = null;
                tArr2 = tArr;
            }
        }
        copyIntoArray(tArr2, 0);
        return tArr2;
    }

    Object writeReplace() {
        return new ImmutableList.SerializedForm(toArray());
    }
}
