package com.google.protobuf.nano;

import com.google.protobuf.nano.ExtendableMessageNano;
import java.io.IOException;
/* loaded from: a.zip:com/google/protobuf/nano/ExtendableMessageNano.class */
public abstract class ExtendableMessageNano<M extends ExtendableMessageNano<M>> extends MessageNano {
    protected FieldArray unknownFieldData;

    @Override // com.google.protobuf.nano.MessageNano
    public M clone() throws CloneNotSupportedException {
        M m = (M) super.m2273clone();
        InternalNano.cloneUnknownFieldData(this, m);
        return m;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.google.protobuf.nano.MessageNano
    public int computeSerializedSize() {
        int i = 0;
        int i2 = 0;
        if (this.unknownFieldData != null) {
            int i3 = 0;
            while (true) {
                i = i2;
                if (i3 >= this.unknownFieldData.size()) {
                    break;
                }
                i2 += this.unknownFieldData.dataAt(i3).computeSerializedSize();
                i3++;
            }
        }
        return i;
    }

    @Override // com.google.protobuf.nano.MessageNano
    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        if (this.unknownFieldData == null) {
            return;
        }
        for (int i = 0; i < this.unknownFieldData.size(); i++) {
            this.unknownFieldData.dataAt(i).writeTo(codedOutputByteBufferNano);
        }
    }
}
