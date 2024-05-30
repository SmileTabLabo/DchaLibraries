package com.google.protobuf.nano;
/* loaded from: classes.dex */
public final class FieldArray implements Cloneable {
    private static final FieldData DELETED = new FieldData();
    private FieldData[] mData;
    private int[] mFieldNumbers;
    private boolean mGarbage;
    private int mSize;

    /* JADX INFO: Access modifiers changed from: package-private */
    public FieldArray() {
        this(10);
    }

    FieldArray(int i) {
        this.mGarbage = false;
        int idealIntArraySize = idealIntArraySize(i);
        this.mFieldNumbers = new int[idealIntArraySize];
        this.mData = new FieldData[idealIntArraySize];
        this.mSize = 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public FieldData get(int i) {
        int binarySearch = binarySearch(i);
        if (binarySearch < 0 || this.mData[binarySearch] == DELETED) {
            return null;
        }
        return this.mData[binarySearch];
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void remove(int i) {
        int binarySearch = binarySearch(i);
        if (binarySearch >= 0 && this.mData[binarySearch] != DELETED) {
            this.mData[binarySearch] = DELETED;
            this.mGarbage = true;
        }
    }

    private void gc() {
        int i = this.mSize;
        int[] iArr = this.mFieldNumbers;
        FieldData[] fieldDataArr = this.mData;
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            FieldData fieldData = fieldDataArr[i3];
            if (fieldData != DELETED) {
                if (i3 != i2) {
                    iArr[i2] = iArr[i3];
                    fieldDataArr[i2] = fieldData;
                    fieldDataArr[i3] = null;
                }
                i2++;
            }
        }
        this.mGarbage = false;
        this.mSize = i2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void put(int i, FieldData fieldData) {
        int binarySearch = binarySearch(i);
        if (binarySearch >= 0) {
            this.mData[binarySearch] = fieldData;
            return;
        }
        int i2 = ~binarySearch;
        if (i2 < this.mSize && this.mData[i2] == DELETED) {
            this.mFieldNumbers[i2] = i;
            this.mData[i2] = fieldData;
            return;
        }
        if (this.mGarbage && this.mSize >= this.mFieldNumbers.length) {
            gc();
            i2 = ~binarySearch(i);
        }
        if (this.mSize >= this.mFieldNumbers.length) {
            int idealIntArraySize = idealIntArraySize(this.mSize + 1);
            int[] iArr = new int[idealIntArraySize];
            FieldData[] fieldDataArr = new FieldData[idealIntArraySize];
            System.arraycopy(this.mFieldNumbers, 0, iArr, 0, this.mFieldNumbers.length);
            System.arraycopy(this.mData, 0, fieldDataArr, 0, this.mData.length);
            this.mFieldNumbers = iArr;
            this.mData = fieldDataArr;
        }
        if (this.mSize - i2 != 0) {
            int i3 = i2 + 1;
            System.arraycopy(this.mFieldNumbers, i2, this.mFieldNumbers, i3, this.mSize - i2);
            System.arraycopy(this.mData, i2, this.mData, i3, this.mSize - i2);
        }
        this.mFieldNumbers[i2] = i;
        this.mData[i2] = fieldData;
        this.mSize++;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int size() {
        if (this.mGarbage) {
            gc();
        }
        return this.mSize;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public FieldData dataAt(int i) {
        if (this.mGarbage) {
            gc();
        }
        return this.mData[i];
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof FieldArray) {
            FieldArray fieldArray = (FieldArray) obj;
            if (size() != fieldArray.size()) {
                return false;
            }
            return arrayEquals(this.mFieldNumbers, fieldArray.mFieldNumbers, this.mSize) && arrayEquals(this.mData, fieldArray.mData, this.mSize);
        }
        return false;
    }

    public int hashCode() {
        if (this.mGarbage) {
            gc();
        }
        int i = 17;
        for (int i2 = 0; i2 < this.mSize; i2++) {
            i = this.mData[i2].hashCode() + (31 * ((i * 31) + this.mFieldNumbers[i2]));
        }
        return i;
    }

    private int idealIntArraySize(int i) {
        return idealByteArraySize(i * 4) / 4;
    }

    private int idealByteArraySize(int i) {
        for (int i2 = 4; i2 < 32; i2++) {
            int i3 = (1 << i2) - 12;
            if (i <= i3) {
                return i3;
            }
        }
        return i;
    }

    private int binarySearch(int i) {
        int i2 = this.mSize - 1;
        int i3 = 0;
        while (i3 <= i2) {
            int i4 = (i3 + i2) >>> 1;
            int i5 = this.mFieldNumbers[i4];
            if (i5 < i) {
                i3 = i4 + 1;
            } else if (i5 > i) {
                i2 = i4 - 1;
            } else {
                return i4;
            }
        }
        return ~i3;
    }

    private boolean arrayEquals(int[] iArr, int[] iArr2, int i) {
        for (int i2 = 0; i2 < i; i2++) {
            if (iArr[i2] != iArr2[i2]) {
                return false;
            }
        }
        return true;
    }

    private boolean arrayEquals(FieldData[] fieldDataArr, FieldData[] fieldDataArr2, int i) {
        for (int i2 = 0; i2 < i; i2++) {
            if (!fieldDataArr[i2].equals(fieldDataArr2[i2])) {
                return false;
            }
        }
        return true;
    }

    /* renamed from: clone */
    public final FieldArray m23clone() {
        int size = size();
        FieldArray fieldArray = new FieldArray(size);
        System.arraycopy(this.mFieldNumbers, 0, fieldArray.mFieldNumbers, 0, size);
        for (int i = 0; i < size; i++) {
            if (this.mData[i] != null) {
                fieldArray.mData[i] = this.mData[i].m24clone();
            }
        }
        fieldArray.mSize = size;
        return fieldArray;
    }
}
