package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import javax.annotation.Nullable;
@GwtCompatible(emulated = true, serializable = true)
/* loaded from: b.zip:com/google/common/collect/ImmutableSet.class */
public abstract class ImmutableSet<E> extends ImmutableCollection<E> implements Set<E> {

    /* loaded from: b.zip:com/google/common/collect/ImmutableSet$Builder.class */
    public static class Builder<E> extends ImmutableCollection.ArrayBasedBuilder<E> {
        public Builder() {
            this(4);
        }

        Builder(int i) {
            super(i);
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // com.google.common.collect.ImmutableCollection.ArrayBasedBuilder, com.google.common.collect.ImmutableCollection.Builder
        public /* bridge */ /* synthetic */ ImmutableCollection.ArrayBasedBuilder add(Object obj) {
            return add((Builder<E>) obj);
        }

        @Override // com.google.common.collect.ImmutableCollection.ArrayBasedBuilder, com.google.common.collect.ImmutableCollection.Builder
        public /* bridge */ /* synthetic */ ImmutableCollection.Builder add(Object obj) {
            return super.add(obj);
        }

        @Override // com.google.common.collect.ImmutableCollection.ArrayBasedBuilder, com.google.common.collect.ImmutableCollection.Builder
        public Builder<E> add(E e) {
            super.add((Builder<E>) e);
            return this;
        }

        @Override // com.google.common.collect.ImmutableCollection.ArrayBasedBuilder
        public Builder<E> add(E... eArr) {
            super.add((Object[]) eArr);
            return this;
        }

        @Override // com.google.common.collect.ImmutableCollection.ArrayBasedBuilder, com.google.common.collect.ImmutableCollection.Builder
        public Builder<E> addAll(Iterable<? extends E> iterable) {
            super.addAll((Iterable) iterable);
            return this;
        }

        public ImmutableSet<E> build() {
            ImmutableSet<E> construct = ImmutableSet.construct(this.size, this.contents);
            this.size = construct.size();
            return construct;
        }
    }

    /* loaded from: b.zip:com/google/common/collect/ImmutableSet$SerializedForm.class */
    private static class SerializedForm implements Serializable {
        private static final long serialVersionUID = 0;
        final Object[] elements;

        SerializedForm(Object[] objArr) {
            this.elements = objArr;
        }

        Object readResolve() {
            return ImmutableSet.copyOf(this.elements);
        }
    }

    public static <E> Builder<E> builder() {
        return new Builder<>();
    }

    @VisibleForTesting
    static int chooseTableSize(int i) {
        if (i >= 751619276) {
            Preconditions.checkArgument(i < 1073741824, "collection too large");
            return 1073741824;
        }
        int highestOneBit = Integer.highestOneBit(i - 1);
        while (true) {
            int i2 = highestOneBit << 1;
            if (i2 * 0.7d >= i) {
                return i2;
            }
            highestOneBit = i2;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static <E> ImmutableSet<E> construct(int i, Object... objArr) {
        switch (i) {
            case 0:
                return of();
            case 1:
                return of(objArr[0]);
            default:
                int chooseTableSize = chooseTableSize(i);
                Object[] objArr2 = new Object[chooseTableSize];
                int i2 = chooseTableSize - 1;
                int i3 = 0;
                int i4 = 0;
                for (int i5 = 0; i5 < i; i5++) {
                    Object checkElementNotNull = ObjectArrays.checkElementNotNull(objArr[i5], i5);
                    int hashCode = checkElementNotNull.hashCode();
                    int smear = Hashing.smear(hashCode);
                    while (true) {
                        int i6 = smear & i2;
                        Object obj = objArr2[i6];
                        if (obj == null) {
                            objArr[i4] = checkElementNotNull;
                            objArr2[i6] = checkElementNotNull;
                            i3 += hashCode;
                            i4++;
                        } else if (obj.equals(checkElementNotNull)) {
                            break;
                        } else {
                            smear++;
                        }
                    }
                }
                Arrays.fill(objArr, i4, i, (Object) null);
                if (i4 == 1) {
                    return new SingletonImmutableSet(objArr[0], i3);
                }
                if (chooseTableSize != chooseTableSize(i4)) {
                    return construct(i4, objArr);
                }
                if (i4 < objArr.length) {
                    objArr = ObjectArrays.arraysCopyOf(objArr, i4);
                }
                return new RegularImmutableSet(objArr, i3, objArr2, i2);
        }
    }

    public static <E> ImmutableSet<E> copyOf(E[] eArr) {
        switch (eArr.length) {
            case 0:
                return of();
            case 1:
                return of((Object) eArr[0]);
            default:
                return construct(eArr.length, (Object[]) eArr.clone());
        }
    }

    public static <E> ImmutableSet<E> of() {
        return EmptyImmutableSet.INSTANCE;
    }

    public static <E> ImmutableSet<E> of(E e) {
        return new SingletonImmutableSet(e);
    }

    @Override // java.util.Collection, java.util.Set
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if ((obj instanceof ImmutableSet) && isHashCodeFast() && ((ImmutableSet) obj).isHashCodeFast() && hashCode() != obj.hashCode()) {
            return false;
        }
        return Sets.equalsImpl(this, obj);
    }

    @Override // java.util.Collection, java.util.Set
    public int hashCode() {
        return Sets.hashCodeImpl(this);
    }

    boolean isHashCodeFast() {
        return false;
    }

    @Override // com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet
    public abstract UnmodifiableIterator<E> iterator();

    @Override // com.google.common.collect.ImmutableCollection
    Object writeReplace() {
        return new SerializedForm(toArray());
    }
}
