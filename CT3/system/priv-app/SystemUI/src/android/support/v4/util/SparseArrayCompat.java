package android.support.v4.util;
/* loaded from: a.zip:android/support/v4/util/SparseArrayCompat.class */
public class SparseArrayCompat<E> implements Cloneable {
    private static final Object DELETED = new Object();
    private boolean mGarbage;
    private int[] mKeys;
    private int mSize;
    private Object[] mValues;

    public SparseArrayCompat() {
        this(10);
    }

    public SparseArrayCompat(int i) {
        this.mGarbage = false;
        if (i == 0) {
            this.mKeys = ContainerHelpers.EMPTY_INTS;
            this.mValues = ContainerHelpers.EMPTY_OBJECTS;
        } else {
            int idealIntArraySize = ContainerHelpers.idealIntArraySize(i);
            this.mKeys = new int[idealIntArraySize];
            this.mValues = new Object[idealIntArraySize];
        }
        this.mSize = 0;
    }

    private void gc() {
        int i = this.mSize;
        int i2 = 0;
        int[] iArr = this.mKeys;
        Object[] objArr = this.mValues;
        int i3 = 0;
        while (i3 < i) {
            Object obj = objArr[i3];
            int i4 = i2;
            if (obj != DELETED) {
                if (i3 != i2) {
                    iArr[i2] = iArr[i3];
                    objArr[i2] = obj;
                    objArr[i3] = null;
                }
                i4 = i2 + 1;
            }
            i3++;
            i2 = i4;
        }
        this.mGarbage = false;
        this.mSize = i2;
    }

    public void clear() {
        int i = this.mSize;
        Object[] objArr = this.mValues;
        for (int i2 = 0; i2 < i; i2++) {
            objArr[i2] = null;
        }
        this.mSize = 0;
        this.mGarbage = false;
    }

    /* renamed from: clone */
    public SparseArrayCompat<E> m196clone() {
        SparseArrayCompat<E> sparseArrayCompat = null;
        try {
            SparseArrayCompat<E> sparseArrayCompat2 = (SparseArrayCompat) super.clone();
            sparseArrayCompat2.mKeys = (int[]) this.mKeys.clone();
            sparseArrayCompat = sparseArrayCompat2;
            sparseArrayCompat2.mValues = (Object[]) this.mValues.clone();
            sparseArrayCompat = sparseArrayCompat2;
        } catch (CloneNotSupportedException e) {
        }
        return sparseArrayCompat;
    }

    public void delete(int i) {
        int binarySearch = ContainerHelpers.binarySearch(this.mKeys, this.mSize, i);
        if (binarySearch < 0 || this.mValues[binarySearch] == DELETED) {
            return;
        }
        this.mValues[binarySearch] = DELETED;
        this.mGarbage = true;
    }

    public E get(int i) {
        return get(i, null);
    }

    public E get(int i, E e) {
        int binarySearch = ContainerHelpers.binarySearch(this.mKeys, this.mSize, i);
        return (binarySearch < 0 || this.mValues[binarySearch] == DELETED) ? e : (E) this.mValues[binarySearch];
    }

    public int keyAt(int i) {
        if (this.mGarbage) {
            gc();
        }
        return this.mKeys[i];
    }

    public void put(int i, E e) {
        int binarySearch = ContainerHelpers.binarySearch(this.mKeys, this.mSize, i);
        if (binarySearch >= 0) {
            this.mValues[binarySearch] = e;
            return;
        }
        int i2 = binarySearch ^ (-1);
        if (i2 < this.mSize && this.mValues[i2] == DELETED) {
            this.mKeys[i2] = i;
            this.mValues[i2] = e;
            return;
        }
        int i3 = i2;
        if (this.mGarbage) {
            i3 = i2;
            if (this.mSize >= this.mKeys.length) {
                gc();
                i3 = ContainerHelpers.binarySearch(this.mKeys, this.mSize, i) ^ (-1);
            }
        }
        if (this.mSize >= this.mKeys.length) {
            int idealIntArraySize = ContainerHelpers.idealIntArraySize(this.mSize + 1);
            int[] iArr = new int[idealIntArraySize];
            Object[] objArr = new Object[idealIntArraySize];
            System.arraycopy(this.mKeys, 0, iArr, 0, this.mKeys.length);
            System.arraycopy(this.mValues, 0, objArr, 0, this.mValues.length);
            this.mKeys = iArr;
            this.mValues = objArr;
        }
        if (this.mSize - i3 != 0) {
            System.arraycopy(this.mKeys, i3, this.mKeys, i3 + 1, this.mSize - i3);
            System.arraycopy(this.mValues, i3, this.mValues, i3 + 1, this.mSize - i3);
        }
        this.mKeys[i3] = i;
        this.mValues[i3] = e;
        this.mSize++;
    }

    public void remove(int i) {
        delete(i);
    }

    public int size() {
        if (this.mGarbage) {
            gc();
        }
        return this.mSize;
    }

    public String toString() {
        if (size() <= 0) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder(this.mSize * 28);
        sb.append('{');
        for (int i = 0; i < this.mSize; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(keyAt(i));
            sb.append('=');
            E valueAt = valueAt(i);
            if (valueAt != this) {
                sb.append(valueAt);
            } else {
                sb.append("(this Map)");
            }
        }
        sb.append('}');
        return sb.toString();
    }

    public E valueAt(int i) {
        if (this.mGarbage) {
            gc();
        }
        return (E) this.mValues[i];
    }
}
