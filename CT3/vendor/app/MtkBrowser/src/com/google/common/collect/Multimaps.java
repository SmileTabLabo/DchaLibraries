package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.AbstractCollection;
import java.util.Map;
import javax.annotation.Nullable;
@GwtCompatible(emulated = true)
/* loaded from: b.zip:com/google/common/collect/Multimaps.class */
public final class Multimaps {

    /* loaded from: b.zip:com/google/common/collect/Multimaps$Entries.class */
    static abstract class Entries<K, V> extends AbstractCollection<Map.Entry<K, V>> {
        @Override // java.util.AbstractCollection, java.util.Collection
        public void clear() {
            multimap().clear();
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public boolean contains(@Nullable Object obj) {
            if (obj instanceof Map.Entry) {
                Map.Entry entry = (Map.Entry) obj;
                return multimap().containsEntry(entry.getKey(), entry.getValue());
            }
            return false;
        }

        abstract Multimap<K, V> multimap();

        @Override // java.util.AbstractCollection, java.util.Collection
        public boolean remove(@Nullable Object obj) {
            if (obj instanceof Map.Entry) {
                Map.Entry entry = (Map.Entry) obj;
                return multimap().remove(entry.getKey(), entry.getValue());
            }
            return false;
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public int size() {
            return multimap().size();
        }
    }

    private Multimaps() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean equalsImpl(Multimap<?, ?> multimap, @Nullable Object obj) {
        if (obj == multimap) {
            return true;
        }
        if (obj instanceof Multimap) {
            return multimap.asMap().equals(((Multimap) obj).asMap());
        }
        return false;
    }
}
