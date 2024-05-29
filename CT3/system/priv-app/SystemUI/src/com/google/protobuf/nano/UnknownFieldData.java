package com.google.protobuf.nano;

import java.io.IOException;
import java.util.Arrays;
/* loaded from: a.zip:com/google/protobuf/nano/UnknownFieldData.class */
final class UnknownFieldData {
    final byte[] bytes;
    final int tag;

    /* JADX INFO: Access modifiers changed from: package-private */
    public int computeSerializedSize() {
        return CodedOutputByteBufferNano.computeRawVarint32Size(this.tag) + 0 + this.bytes.length;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == this) {
            return true;
        }
        if (obj instanceof UnknownFieldData) {
            UnknownFieldData unknownFieldData = (UnknownFieldData) obj;
            if (this.tag == unknownFieldData.tag) {
                z = Arrays.equals(this.bytes, unknownFieldData.bytes);
            }
            return z;
        }
        return false;
    }

    public int hashCode() {
        return ((this.tag + 527) * 31) + Arrays.hashCode(this.bytes);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        codedOutputByteBufferNano.writeRawVarint32(this.tag);
        codedOutputByteBufferNano.writeRawBytes(this.bytes);
    }
}
