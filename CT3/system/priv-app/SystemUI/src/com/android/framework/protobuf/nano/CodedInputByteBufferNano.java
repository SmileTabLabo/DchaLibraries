package com.android.framework.protobuf.nano;
/* loaded from: a.zip:com/android/framework/protobuf/nano/CodedInputByteBufferNano.class */
public final class CodedInputByteBufferNano {
    private final byte[] buffer;
    private int bufferPos;
    private int bufferSize;
    private int bufferStart;
    private int lastTag;
    private int currentLimit = Integer.MAX_VALUE;
    private int recursionLimit = 64;
    private int sizeLimit = 67108864;

    private CodedInputByteBufferNano(byte[] bArr, int i, int i2) {
        this.buffer = bArr;
        this.bufferStart = i;
        this.bufferSize = i + i2;
        this.bufferPos = i;
    }

    public static CodedInputByteBufferNano newInstance(byte[] bArr, int i, int i2) {
        return new CodedInputByteBufferNano(bArr, i, i2);
    }

    public void checkLastTagWas(int i) throws InvalidProtocolBufferNanoException {
        if (this.lastTag != i) {
            throw InvalidProtocolBufferNanoException.invalidEndTag();
        }
    }
}
