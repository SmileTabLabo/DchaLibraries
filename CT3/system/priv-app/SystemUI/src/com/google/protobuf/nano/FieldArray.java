package com.google.protobuf.nano;
/* loaded from: a.zip:com/google/protobuf/nano/FieldArray.class */
public final class FieldArray implements Cloneable {
    private static final FieldData DELETED = new FieldData();
    private FieldData[] mData;
    private int[] mFieldNumbers;
    private boolean mGarbage;
    private int mSize;

    FieldArray() {
        this(10);
    }

    FieldArray(int i) {
        this.mGarbage = false;
        int idealIntArraySize = idealIntArraySize(i);
        this.mFieldNumbers = new int[idealIntArraySize];
        this.mData = new FieldData[idealIntArraySize];
        this.mSize = 0;
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

    private void gc() {
        int i = this.mSize;
        int i2 = 0;
        int[] iArr = this.mFieldNumbers;
        FieldData[] fieldDataArr = this.mData;
        int i3 = 0;
        while (i3 < i) {
            FieldData fieldData = fieldDataArr[i3];
            int i4 = i2;
            if (fieldData != DELETED) {
                if (i3 != i2) {
                    iArr[i2] = iArr[i3];
                    fieldDataArr[i2] = fieldData;
                    fieldDataArr[i3] = null;
                }
                i4 = i2 + 1;
            }
            i3++;
            i2 = i4;
        }
        this.mGarbage = false;
        this.mSize = i2;
    }

    private int idealByteArraySize(int i) {
        for (int i2 = 4; i2 < 32; i2++) {
            if (i <= (1 << i2) - 12) {
                return (1 << i2) - 12;
            }
        }
        return i;
    }

    private int idealIntArraySize(int i) {
        return idealByteArraySize(i * 4) / 4;
    }

    /* renamed from: clone */
    public final FieldArray m2270clone() {
        int size = size();
        FieldArray fieldArray = new FieldArray(size);
        System.arraycopy(this.mFieldNumbers, 0, fieldArray.mFieldNumbers, 0, size);
        for (int i = 0; i < size; i++) {
            if (this.mData[i] != null) {
                fieldArray.mData[i] = this.mData[i].m2271clone();
            }
        }
        fieldArray.mSize = size;
        return fieldArray;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public FieldData dataAt(int i) {
        if (this.mGarbage) {
            gc();
        }
        return this.mData[i];
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == this) {
            return true;
        }
        if (obj instanceof FieldArray) {
            FieldArray fieldArray = (FieldArray) obj;
            if (size() != fieldArray.size()) {
                return false;
            }
            if (arrayEquals(this.mFieldNumbers, fieldArray.mFieldNumbers, this.mSize)) {
                z = arrayEquals(this.mData, fieldArray.mData, this.mSize);
            }
            return z;
        }
        return false;
    }

    public int hashCode() {
        if (this.mGarbage) {
            gc();
        }
        int i = 17;
        for (int i2 = 0; i2 < this.mSize; i2++) {
            i = (((i * 31) + this.mFieldNumbers[i2]) * 31) + this.mData[i2].hashCode();
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int size() {
        if (this.mGarbage) {
            gc();
        }
        return this.mSize;
    }
}
