package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import java.util.Arrays;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
@GwtCompatible
/* loaded from: b.zip:com/google/common/base/Objects.class */
public final class Objects {
    private Objects() {
    }

    @CheckReturnValue
    public static boolean equal(@Nullable Object obj, @Nullable Object obj2) {
        return obj != obj2 ? obj != null ? obj.equals(obj2) : false : true;
    }

    public static int hashCode(@Nullable Object... objArr) {
        return Arrays.hashCode(objArr);
    }
}
