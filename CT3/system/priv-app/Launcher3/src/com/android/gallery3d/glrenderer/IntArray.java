package com.android.gallery3d.glrenderer;
/* loaded from: a.zip:com/android/gallery3d/glrenderer/IntArray.class */
public class IntArray {
    private int[] mData = new int[8];
    private int mSize = 0;

    public void add(int i) {
        if (this.mData.length == this.mSize) {
            int[] iArr = new int[this.mSize + this.mSize];
            System.arraycopy(this.mData, 0, iArr, 0, this.mSize);
            this.mData = iArr;
        }
        int[] iArr2 = this.mData;
        int i2 = this.mSize;
        this.mSize = i2 + 1;
        iArr2[i2] = i;
    }

    public void clear() {
        this.mSize = 0;
        if (this.mData.length != 8) {
            this.mData = new int[8];
        }
    }

    public int[] getInternalArray() {
        return this.mData;
    }

    public int removeLast() {
        this.mSize--;
        return this.mData[this.mSize];
    }

    public int size() {
        return this.mSize;
    }
}
