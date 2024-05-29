package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
@GwtCompatible(emulated = true)
/* loaded from: a.zip:com/google/common/collect/ObjectArrays.class */
public final class ObjectArrays {
    static final Object[] EMPTY_ARRAY = new Object[0];

    private ObjectArrays() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <T> T[] arraysCopyOf(T[] tArr, int i) {
        T[] tArr2 = (T[]) newArray(tArr, i);
        System.arraycopy(tArr, 0, tArr2, 0, Math.min(tArr.length, i));
        return tArr2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Object checkElementNotNull(Object obj, int i) {
        if (obj == null) {
            throw new NullPointerException("at index " + i);
        }
        return obj;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Object[] checkElementsNotNull(Object... objArr) {
        return checkElementsNotNull(objArr, objArr.length);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Object[] checkElementsNotNull(Object[] objArr, int i) {
        for (int i2 = 0; i2 < i; i2++) {
            checkElementNotNull(objArr[i2], i2);
        }
        return objArr;
    }

    public static <T> T[] newArray(T[] tArr, int i) {
        return (T[]) Platform.newArray(tArr, i);
    }
}
