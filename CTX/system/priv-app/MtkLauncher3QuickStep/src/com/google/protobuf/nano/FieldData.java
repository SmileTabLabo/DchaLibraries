package com.google.protobuf.nano;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/* loaded from: classes.dex */
class FieldData implements Cloneable {
    private Extension<?, ?> cachedExtension;
    private List<UnknownFieldData> unknownFieldData;
    private Object value;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Multi-variable type inference failed */
    public <T> FieldData(Extension<?, T> extension, T t) {
        this.cachedExtension = extension;
        this.value = t;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public FieldData() {
        this.unknownFieldData = new ArrayList();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addUnknownField(UnknownFieldData unknownFieldData) {
        this.unknownFieldData.add(unknownFieldData);
    }

    UnknownFieldData getUnknownField(int i) {
        if (this.unknownFieldData != null && i < this.unknownFieldData.size()) {
            return this.unknownFieldData.get(i);
        }
        return null;
    }

    int getUnknownFieldSize() {
        if (this.unknownFieldData == null) {
            return 0;
        }
        return this.unknownFieldData.size();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Multi-variable type inference failed */
    public <T> T getValue(Extension<?, T> extension) {
        if (this.value != null) {
            if (this.cachedExtension != extension) {
                throw new IllegalStateException("Tried to getExtension with a differernt Extension.");
            }
        } else {
            this.cachedExtension = extension;
            this.value = extension.getValueFrom(this.unknownFieldData);
            this.unknownFieldData = null;
        }
        return (T) this.value;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Multi-variable type inference failed */
    public <T> void setValue(Extension<?, T> extension, T t) {
        this.cachedExtension = extension;
        this.value = t;
        this.unknownFieldData = null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int computeSerializedSize() {
        if (this.value != null) {
            return this.cachedExtension.computeSerializedSize(this.value);
        }
        int i = 0;
        for (UnknownFieldData unknownFieldData : this.unknownFieldData) {
            i += unknownFieldData.computeSerializedSize();
        }
        return i;
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
                if (!this.cachedExtension.clazz.isArray()) {
                    return this.value.equals(fieldData.value);
                }
                if (this.value instanceof byte[]) {
                    return Arrays.equals((byte[]) this.value, (byte[]) fieldData.value);
                }
                if (this.value instanceof int[]) {
                    return Arrays.equals((int[]) this.value, (int[]) fieldData.value);
                }
                if (this.value instanceof long[]) {
                    return Arrays.equals((long[]) this.value, (long[]) fieldData.value);
                }
                if (this.value instanceof float[]) {
                    return Arrays.equals((float[]) this.value, (float[]) fieldData.value);
                }
                if (this.value instanceof double[]) {
                    return Arrays.equals((double[]) this.value, (double[]) fieldData.value);
                }
                if (this.value instanceof boolean[]) {
                    return Arrays.equals((boolean[]) this.value, (boolean[]) fieldData.value);
                }
                return Arrays.deepEquals((Object[]) this.value, (Object[]) fieldData.value);
            } else if (this.unknownFieldData != null && fieldData.unknownFieldData != null) {
                return this.unknownFieldData.equals(fieldData.unknownFieldData);
            } else {
                try {
                    return Arrays.equals(toByteArray(), fieldData.toByteArray());
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return false;
    }

    public int hashCode() {
        try {
            return 527 + Arrays.hashCode(toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] toByteArray() throws IOException {
        byte[] bArr = new byte[computeSerializedSize()];
        writeTo(CodedOutputByteBufferNano.newInstance(bArr));
        return bArr;
    }

    /* renamed from: clone */
    public final FieldData m24clone() {
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
                    fieldData.value = ((MessageNano) this.value).mo22clone();
                } else if (this.value instanceof byte[]) {
                    fieldData.value = ((byte[]) this.value).clone();
                } else {
                    int i = 0;
                    if (this.value instanceof byte[][]) {
                        byte[][] bArr = (byte[][]) this.value;
                        byte[][] bArr2 = new byte[bArr.length];
                        fieldData.value = bArr2;
                        while (i < bArr.length) {
                            bArr2[i] = (byte[]) bArr[i].clone();
                            i++;
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
                        while (i < messageNanoArr.length) {
                            messageNanoArr2[i] = messageNanoArr[i].mo22clone();
                            i++;
                        }
                    }
                }
            }
            return fieldData;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
