package com.google.protobuf.nano;

import com.google.protobuf.nano.ExtendableMessageNano;
import java.io.IOException;
import java.lang.reflect.Array;
/* loaded from: a.zip:com/google/protobuf/nano/Extension.class */
public class Extension<M extends ExtendableMessageNano<M>, T> {
    protected final Class<T> clazz;
    protected final boolean repeated;
    public final int tag;
    protected final int type;

    protected int computeRepeatedSerializedSize(Object obj) {
        int i = 0;
        int length = Array.getLength(obj);
        int i2 = 0;
        while (i2 < length) {
            int i3 = i;
            if (Array.get(obj, i2) != null) {
                i3 = i + computeSingularSerializedSize(Array.get(obj, i2));
            }
            i2++;
            i = i3;
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int computeSerializedSize(Object obj) {
        return this.repeated ? computeRepeatedSerializedSize(obj) : computeSingularSerializedSize(obj);
    }

    protected int computeSingularSerializedSize(Object obj) {
        int tagFieldNumber = WireFormatNano.getTagFieldNumber(this.tag);
        switch (this.type) {
            case 10:
                return CodedOutputByteBufferNano.computeGroupSize(tagFieldNumber, (MessageNano) obj);
            case 11:
                return CodedOutputByteBufferNano.computeMessageSize(tagFieldNumber, (MessageNano) obj);
            default:
                throw new IllegalArgumentException("Unknown type " + this.type);
        }
    }

    protected void writeRepeatedData(Object obj, CodedOutputByteBufferNano codedOutputByteBufferNano) {
        int length = Array.getLength(obj);
        for (int i = 0; i < length; i++) {
            Object obj2 = Array.get(obj, i);
            if (obj2 != null) {
                writeSingularData(obj2, codedOutputByteBufferNano);
            }
        }
    }

    protected void writeSingularData(Object obj, CodedOutputByteBufferNano codedOutputByteBufferNano) {
        try {
            codedOutputByteBufferNano.writeRawVarint32(this.tag);
            switch (this.type) {
                case 10:
                    int tagFieldNumber = WireFormatNano.getTagFieldNumber(this.tag);
                    codedOutputByteBufferNano.writeGroupNoTag((MessageNano) obj);
                    codedOutputByteBufferNano.writeTag(tagFieldNumber, 4);
                    return;
                case 11:
                    codedOutputByteBufferNano.writeMessageNoTag((MessageNano) obj);
                    return;
                default:
                    throw new IllegalArgumentException("Unknown type " + this.type);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void writeTo(Object obj, CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        if (this.repeated) {
            writeRepeatedData(obj, codedOutputByteBufferNano);
        } else {
            writeSingularData(obj, codedOutputByteBufferNano);
        }
    }
}
