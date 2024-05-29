package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nullable;
@GwtCompatible(emulated = true)
/* loaded from: b.zip:com/google/common/collect/Lists.class */
public final class Lists {
    private Lists() {
    }

    @VisibleForTesting
    static int computeArrayListCapacity(int i) {
        CollectPreconditions.checkNonnegative(i, "arraySize");
        return Ints.saturatedCast(i + 5 + (i / 10));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean equalsImpl(List<?> list, @Nullable Object obj) {
        boolean z = false;
        if (obj == Preconditions.checkNotNull(list)) {
            return true;
        }
        if (obj instanceof List) {
            List list2 = (List) obj;
            if (list.size() == list2.size()) {
                z = Iterators.elementsEqual(list.iterator(), list2.iterator());
            }
            return z;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int indexOfImpl(List<?> list, @Nullable Object obj) {
        ListIterator<?> listIterator = list.listIterator();
        while (listIterator.hasNext()) {
            if (Objects.equal(obj, listIterator.next())) {
                return listIterator.previousIndex();
            }
        }
        return -1;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int lastIndexOfImpl(List<?> list, @Nullable Object obj) {
        ListIterator<?> listIterator = list.listIterator(list.size());
        while (listIterator.hasPrevious()) {
            if (Objects.equal(obj, listIterator.previous())) {
                return listIterator.nextIndex();
            }
        }
        return -1;
    }

    @GwtCompatible(serializable = true)
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<>();
    }

    @GwtCompatible(serializable = true)
    public static <E> ArrayList<E> newArrayList(Iterable<? extends E> iterable) {
        Preconditions.checkNotNull(iterable);
        return iterable instanceof Collection ? new ArrayList<>(Collections2.cast(iterable)) : newArrayList(iterable.iterator());
    }

    @GwtCompatible(serializable = true)
    public static <E> ArrayList<E> newArrayList(Iterator<? extends E> it) {
        ArrayList<E> newArrayList = newArrayList();
        Iterators.addAll(newArrayList, it);
        return newArrayList;
    }
}
