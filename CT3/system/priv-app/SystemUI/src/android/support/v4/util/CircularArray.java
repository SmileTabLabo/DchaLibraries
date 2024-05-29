package android.support.v4.util;
/* loaded from: a.zip:android/support/v4/util/CircularArray.class */
public final class CircularArray<E> {
    private int mCapacityBitmask;
    private E[] mElements;
    private int mHead;
    private int mTail;

    public CircularArray() {
        this(8);
    }

    public CircularArray(int i) {
        if (i < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        if (i > 1073741824) {
            throw new IllegalArgumentException("capacity must be <= 2^30");
        }
        i = Integer.bitCount(i) != 1 ? Integer.highestOneBit(i - 1) << 1 : i;
        this.mCapacityBitmask = i - 1;
        this.mElements = (E[]) new Object[i];
    }

    private void doubleCapacity() {
        int length = this.mElements.length;
        int i = length - this.mHead;
        int i2 = length << 1;
        if (i2 < 0) {
            throw new RuntimeException("Max array capacity exceeded");
        }
        E[] eArr = (E[]) new Object[i2];
        System.arraycopy(this.mElements, this.mHead, eArr, 0, i);
        System.arraycopy(this.mElements, 0, eArr, i, this.mHead);
        this.mElements = eArr;
        this.mHead = 0;
        this.mTail = length;
        this.mCapacityBitmask = i2 - 1;
    }

    public void addFirst(E e) {
        this.mHead = (this.mHead - 1) & this.mCapacityBitmask;
        this.mElements[this.mHead] = e;
        if (this.mHead == this.mTail) {
            doubleCapacity();
        }
    }

    public void addLast(E e) {
        this.mElements[this.mTail] = e;
        this.mTail = (this.mTail + 1) & this.mCapacityBitmask;
        if (this.mTail == this.mHead) {
            doubleCapacity();
        }
    }

    public void clear() {
        removeFromStart(size());
    }

    public E get(int i) {
        if (i < 0 || i >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return this.mElements[(this.mHead + i) & this.mCapacityBitmask];
    }

    public void removeFromEnd(int i) {
        if (i <= 0) {
            return;
        }
        if (i > size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int i2 = i < this.mTail ? this.mTail - i : 0;
        for (int i3 = i2; i3 < this.mTail; i3++) {
            this.mElements[i3] = null;
        }
        int i4 = this.mTail - i2;
        int i5 = i - i4;
        this.mTail -= i4;
        if (i5 > 0) {
            this.mTail = this.mElements.length;
            int i6 = this.mTail - i5;
            for (int i7 = i6; i7 < this.mTail; i7++) {
                this.mElements[i7] = null;
            }
            this.mTail = i6;
        }
    }

    public void removeFromStart(int i) {
        if (i <= 0) {
            return;
        }
        if (i > size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int length = this.mElements.length;
        int i2 = length;
        if (i < length - this.mHead) {
            i2 = this.mHead + i;
        }
        for (int i3 = this.mHead; i3 < i2; i3++) {
            this.mElements[i3] = null;
        }
        int i4 = i2 - this.mHead;
        int i5 = i - i4;
        this.mHead = (this.mHead + i4) & this.mCapacityBitmask;
        if (i5 > 0) {
            for (int i6 = 0; i6 < i5; i6++) {
                this.mElements[i6] = null;
            }
            this.mHead = i5;
        }
    }

    public int size() {
        return (this.mTail - this.mHead) & this.mCapacityBitmask;
    }
}
