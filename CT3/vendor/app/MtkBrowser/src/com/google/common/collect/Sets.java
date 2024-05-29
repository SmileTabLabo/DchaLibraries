package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;
@GwtCompatible(emulated = true)
/* loaded from: b.zip:com/google/common/collect/Sets.class */
public final class Sets {

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/google/common/collect/Sets$ImprovedAbstractSet.class */
    public static abstract class ImprovedAbstractSet<E> extends AbstractSet<E> {
        @Override // java.util.AbstractSet, java.util.AbstractCollection, java.util.Collection, java.util.Set
        public boolean removeAll(Collection<?> collection) {
            return Sets.removeAllImpl(this, collection);
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public boolean retainAll(Collection<?> collection) {
            return super.retainAll((Collection) Preconditions.checkNotNull(collection));
        }
    }

    private Sets() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean equalsImpl(Set<?> set, @Nullable Object obj) {
        boolean z = false;
        if (set == obj) {
            return true;
        }
        if (obj instanceof Set) {
            Set set2 = (Set) obj;
            try {
                if (set.size() == set2.size()) {
                    z = set.containsAll(set2);
                }
                return z;
            } catch (ClassCastException e) {
                return false;
            } catch (NullPointerException e2) {
                return false;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int hashCodeImpl(Set<?> set) {
        int i = 0;
        Iterator<T> it = set.iterator();
        while (it.hasNext()) {
            Object next = it.next();
            i = ((i + (next != null ? next.hashCode() : 0)) ^ (-1)) ^ (-1);
        }
        return i;
    }

    public static <E> HashSet<E> newHashSet() {
        return new HashSet<>();
    }

    public static <E> HashSet<E> newHashSetWithExpectedSize(int i) {
        return new HashSet<>(Maps.capacity(i));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean removeAllImpl(Set<?> set, Collection<?> collection) {
        Preconditions.checkNotNull(collection);
        Set set2 = collection;
        if (collection instanceof Multiset) {
            set2 = ((Multiset) collection).elementSet();
        }
        return (!(set2 instanceof Set) || set2.size() <= set.size()) ? removeAllImpl(set, set2.iterator()) : Iterators.removeAll(set.iterator(), set2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean removeAllImpl(Set<?> set, Iterator<?> it) {
        boolean z = false;
        while (true) {
            boolean z2 = z;
            if (!it.hasNext()) {
                return z2;
            }
            z = z2 | set.remove(it.next());
        }
    }
}
