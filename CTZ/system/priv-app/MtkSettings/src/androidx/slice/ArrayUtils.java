package androidx.slice;

import android.support.v4.util.ObjectsCompat;
import java.lang.reflect.Array;
/* loaded from: classes.dex */
class ArrayUtils {
    public static <T> boolean contains(T[] array, T item) {
        for (T t : array) {
            if (ObjectsCompat.equals(t, item)) {
                return true;
            }
        }
        return false;
    }

    public static <T> T[] appendElement(Class<T> kind, T[] array, T element) {
        int end;
        T[] result;
        if (array != null) {
            end = array.length;
            result = (T[]) ((Object[]) Array.newInstance((Class<?>) kind, end + 1));
            System.arraycopy(array, 0, result, 0, end);
        } else {
            end = 0;
            result = (T[]) ((Object[]) Array.newInstance((Class<?>) kind, 1));
        }
        result[end] = element;
        return result;
    }
}
