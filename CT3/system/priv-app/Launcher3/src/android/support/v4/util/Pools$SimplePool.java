package android.support.v4.util;
/* loaded from: a.zip:android/support/v4/util/Pools$SimplePool.class */
public class Pools$SimplePool<T> implements Pools$Pool<T> {
    private final Object[] mPool;
    private int mPoolSize;

    public Pools$SimplePool(int i) {
        if (i <= 0) {
            throw new IllegalArgumentException("The max pool size must be > 0");
        }
        this.mPool = new Object[i];
    }

    private boolean isInPool(T t) {
        for (int i = 0; i < this.mPoolSize; i++) {
            if (this.mPool[i] == t) {
                return true;
            }
        }
        return false;
    }

    @Override // android.support.v4.util.Pools$Pool
    public T acquire() {
        if (this.mPoolSize > 0) {
            int i = this.mPoolSize - 1;
            T t = (T) this.mPool[i];
            this.mPool[i] = null;
            this.mPoolSize--;
            return t;
        }
        return null;
    }

    @Override // android.support.v4.util.Pools$Pool
    public boolean release(T t) {
        if (isInPool(t)) {
            throw new IllegalStateException("Already in the pool!");
        }
        if (this.mPoolSize < this.mPool.length) {
            this.mPool[this.mPoolSize] = t;
            this.mPoolSize++;
            return true;
        }
        return false;
    }
}
