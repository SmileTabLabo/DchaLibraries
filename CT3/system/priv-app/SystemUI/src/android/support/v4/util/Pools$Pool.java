package android.support.v4.util;
/* loaded from: a.zip:android/support/v4/util/Pools$Pool.class */
public interface Pools$Pool<T> {
    T acquire();

    boolean release(T t);
}
