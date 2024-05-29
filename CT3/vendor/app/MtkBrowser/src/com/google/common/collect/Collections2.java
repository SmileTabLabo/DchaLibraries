package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.util.Collection;
import javax.annotation.Nullable;
@GwtCompatible
/* loaded from: b.zip:com/google/common/collect/Collections2.class */
public final class Collections2 {
    static final Joiner STANDARD_JOINER = Joiner.on(", ").useForNull("null");

    private Collections2() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <T> Collection<T> cast(Iterable<T> iterable) {
        return (Collection) iterable;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static StringBuilder newStringBuilderForCollection(int i) {
        CollectPreconditions.checkNonnegative(i, "size");
        return new StringBuilder((int) Math.min(i * 8, 1073741824L));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean safeContains(Collection<?> collection, @Nullable Object obj) {
        Preconditions.checkNotNull(collection);
        try {
            return collection.contains(obj);
        } catch (ClassCastException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }
}
