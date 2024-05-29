package android.support.v7.content.res;

import java.lang.reflect.Array;
/* loaded from: classes.dex */
final class GrowingArrayUtils {

    /* renamed from: -assertionsDisabled  reason: not valid java name */
    static final /* synthetic */ boolean f0assertionsDisabled;

    static {
        f0assertionsDisabled = !GrowingArrayUtils.class.desiredAssertionStatus();
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1, types: [java.lang.Object[], java.lang.Object] */
    public static <T> T[] append(T[] array, int currentSize, T element) {
        if (!f0assertionsDisabled) {
            if (!(currentSize <= array.length)) {
                throw new AssertionError();
            }
        }
        if (currentSize + 1 > array.length) {
            ?? r0 = (Object[]) Array.newInstance(array.getClass().getComponentType(), growSize(currentSize));
            System.arraycopy(array, 0, r0, 0, currentSize);
            array = r0;
        }
        array[currentSize] = element;
        return array;
    }

    public static int[] append(int[] array, int currentSize, int element) {
        if (!f0assertionsDisabled) {
            if (!(currentSize <= array.length)) {
                throw new AssertionError();
            }
        }
        if (currentSize + 1 > array.length) {
            int[] newArray = new int[growSize(currentSize)];
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }

    public static int growSize(int currentSize) {
        if (currentSize <= 4) {
            return 8;
        }
        return currentSize * 2;
    }

    private GrowingArrayUtils() {
    }
}
