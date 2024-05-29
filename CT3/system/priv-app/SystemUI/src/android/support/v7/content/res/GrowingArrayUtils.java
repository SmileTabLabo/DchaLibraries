package android.support.v7.content.res;

import java.lang.reflect.Array;
/* loaded from: a.zip:android/support/v7/content/res/GrowingArrayUtils.class */
final class GrowingArrayUtils {

    /* renamed from: -assertionsDisabled  reason: not valid java name */
    static final boolean f0assertionsDisabled;

    static {
        f0assertionsDisabled = !GrowingArrayUtils.class.desiredAssertionStatus();
    }

    private GrowingArrayUtils() {
    }

    public static int[] append(int[] iArr, int i, int i2) {
        if (!f0assertionsDisabled) {
            if (!(i <= iArr.length)) {
                throw new AssertionError();
            }
        }
        int[] iArr2 = iArr;
        if (i + 1 > iArr.length) {
            iArr2 = new int[growSize(i)];
            System.arraycopy(iArr, 0, iArr2, 0, i);
        }
        iArr2[i] = i2;
        return iArr2;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v10, types: [java.lang.Object[]] */
    public static <T> T[] append(T[] tArr, int i, T t) {
        if (!f0assertionsDisabled) {
            if (!(i <= tArr.length)) {
                throw new AssertionError();
            }
        }
        T[] tArr2 = tArr;
        if (i + 1 > tArr.length) {
            tArr2 = (Object[]) Array.newInstance(tArr.getClass().getComponentType(), growSize(i));
            System.arraycopy(tArr, 0, tArr2, 0, i);
        }
        tArr2[i] = t;
        return tArr2;
    }

    public static int growSize(int i) {
        return i <= 4 ? 8 : i * 2;
    }
}
