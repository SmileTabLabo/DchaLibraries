package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtCompatible(emulated = true, serializable = true)
/* loaded from: b.zip:com/google/common/collect/RegularImmutableList.class */
public class RegularImmutableList<E> extends ImmutableList<E> {
    private final transient Object[] array;
    private final transient int offset;
    private final transient int size;

    /* JADX INFO: Access modifiers changed from: package-private */
    public RegularImmutableList(Object[] objArr) {
        this(objArr, 0, objArr.length);
    }

    RegularImmutableList(Object[] objArr, int i, int i2) {
        this.offset = i;
        this.size = i2;
        this.array = objArr;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableList, com.google.common.collect.ImmutableCollection
    public int copyIntoArray(Object[] objArr, int i) {
        System.arraycopy(this.array, this.offset, objArr, i, this.size);
        return this.size + i;
    }

    @Override // java.util.List
    public E get(int i) {
        Preconditions.checkElementIndex(i, this.size);
        return (E) this.array[this.offset + i];
    }

    @Override // com.google.common.collect.ImmutableList, java.util.List
    public int indexOf(@Nullable Object obj) {
        if (obj == null) {
            return -1;
        }
        for (int i = 0; i < this.size; i++) {
            if (this.array[this.offset + i].equals(obj)) {
                return i;
            }
        }
        return -1;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableCollection
    public boolean isPartialView() {
        return this.size != this.array.length;
    }

    @Override // com.google.common.collect.ImmutableList, java.util.List
    public int lastIndexOf(@Nullable Object obj) {
        if (obj == null) {
            return -1;
        }
        for (int i = this.size - 1; i >= 0; i--) {
            if (this.array[this.offset + i].equals(obj)) {
                return i;
            }
        }
        return -1;
    }

    @Override // com.google.common.collect.ImmutableList, java.util.List
    public UnmodifiableListIterator<E> listIterator(int i) {
        return Iterators.forArray(this.array, this.offset, this.size, i);
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
    public int size() {
        return this.size;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableList
    public ImmutableList<E> subListUnchecked(int i, int i2) {
        return new RegularImmutableList(this.array, this.offset + i, i2 - i);
    }
}
