package com.google.protobuf.nano;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
/* loaded from: a.zip:com/google/protobuf/nano/FieldData.class */
class FieldData implements Cloneable {
    private Extension<?, ?> cachedExtension;
    private List<UnknownFieldData> unknownFieldData = new ArrayList();
    private Object value;

    private byte[] toByteArray() throws IOException {
        byte[] bArr = new byte[computeSerializedSize()];
        writeTo(CodedOutputByteBufferNano.newInstance(bArr));
        return bArr;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* renamed from: clone */
    public final FieldData m2271clone() {
        FieldData fieldData = new FieldData();
        try {
            fieldData.cachedExtension = this.cachedExtension;
            if (this.unknownFieldData == null) {
                fieldData.unknownFieldData = null;
            } else {
                fieldData.unknownFieldData.addAll(this.unknownFieldData);
            }
            if (this.value != null) {
                if (this.value instanceof MessageNano) {
                    fieldData.value = ((MessageNano) this.value).m2273clone();
                } else if (this.value instanceof byte[]) {
                    fieldData.value = ((byte[]) this.value).clone();
                } else if (this.value instanceof byte[][]) {
                    byte[][] bArr = (byte[][]) this.value;
                    byte[] bArr2 = new byte[bArr.length];
                    fieldData.value = bArr2;
                    for (int i = 0; i < bArr.length; i++) {
                        bArr2[i] = (byte[]) bArr[i].clone();
                    }
                } else if (this.value instanceof boolean[]) {
                    fieldData.value = ((boolean[]) this.value).clone();
                } else if (this.value instanceof int[]) {
                    fieldData.value = ((int[]) this.value).clone();
                } else if (this.value instanceof long[]) {
                    fieldData.value = ((long[]) this.value).clone();
                } else if (this.value instanceof float[]) {
                    fieldData.value = ((float[]) this.value).clone();
                } else if (this.value instanceof double[]) {
                    fieldData.value = ((double[]) this.value).clone();
                } else if (this.value instanceof MessageNano[]) {
                    MessageNano[] messageNanoArr = (MessageNano[]) this.value;
                    MessageNano[] messageNanoArr2 = new MessageNano[messageNanoArr.length];
                    fieldData.value = messageNanoArr2;
                    for (int i2 = 0; i2 < messageNanoArr.length; i2++) {
                        messageNanoArr2[i2] = messageNanoArr[i2].m2273clone();
                    }
                }
            }
            return fieldData;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int computeSerializedSize() {
        int i;
        int i2 = 0;
        if (this.value == null) {
            Iterator<T> it = this.unknownFieldData.iterator();
            while (true) {
                i = i2;
                if (!it.hasNext()) {
                    break;
                }
                i2 += ((UnknownFieldData) it.next()).computeSerializedSize();
            }
        } else {
            i = this.cachedExtension.computeSerializedSize(this.value);
        }
        return i;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof FieldData) {
            FieldData fieldData = (FieldData) obj;
            if (this.value != null && fieldData.value != null) {
                if (this.cachedExtension != fieldData.cachedExtension) {
                    return false;
                }
                return !this.cachedExtension.clazz.isArray() ? this.value.equals(fieldData.value) : this.value instanceof byte[] ? Arrays.equals((byte[]) this.value, (byte[]) fieldData.value) : this.value instanceof int[] ? Arrays.equals((int[]) this.value, (int[]) fieldData.value) : this.value instanceof long[] ? Arrays.equals((long[]) this.value, (long[]) fieldData.value) : this.value instanceof float[] ? Arrays.equals((float[]) this.value, (float[]) fieldData.value) : this.value instanceof double[] ? Arrays.equals((double[]) this.value, (double[]) fieldData.value) : this.value instanceof boolean[] ? Arrays.equals((boolean[]) this.value, (boolean[]) fieldData.value) : Arrays.deepEquals((Object[]) this.value, (Object[]) fieldData.value);
            } else if (this.unknownFieldData == null || fieldData.unknownFieldData == null) {
                try {
                    return Arrays.equals(toByteArray(), fieldData.toByteArray());
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                return this.unknownFieldData.equals(fieldData.unknownFieldData);
            }
        }
        return false;
    }

    public int hashCode() {
        try {
            return Arrays.hashCode(toByteArray()) + 527;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        if (this.value != null) {
            this.cachedExtension.writeTo(this.value, codedOutputByteBufferNano);
            return;
        }
        for (UnknownFieldData unknownFieldData : this.unknownFieldData) {
            unknownFieldData.writeTo(codedOutputByteBufferNano);
        }
    }
}
