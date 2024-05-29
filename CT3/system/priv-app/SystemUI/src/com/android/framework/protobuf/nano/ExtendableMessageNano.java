package com.android.framework.protobuf.nano;

import com.android.framework.protobuf.nano.ExtendableMessageNano;
import java.io.IOException;
/* loaded from: a.zip:com/android/framework/protobuf/nano/ExtendableMessageNano.class */
public abstract class ExtendableMessageNano<M extends ExtendableMessageNano<M>> extends MessageNano {
    protected FieldArray unknownFieldData;

    @Override // com.android.framework.protobuf.nano.MessageNano
    public M clone() throws CloneNotSupportedException {
        M m = (M) super.m575clone();
        InternalNano.cloneUnknownFieldData(this, m);
        return m;
    }

    @Override // com.android.framework.protobuf.nano.MessageNano
    protected int computeSerializedSize() {
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

    @Override // com.android.framework.protobuf.nano.MessageNano
    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        if (this.unknownFieldData == null) {
            return;
        }
        for (int i = 0; i < this.unknownFieldData.size(); i++) {
            this.unknownFieldData.dataAt(i).writeTo(codedOutputByteBufferNano);
        }
    }
}
