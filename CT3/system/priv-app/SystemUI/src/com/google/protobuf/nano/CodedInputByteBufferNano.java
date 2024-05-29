package com.google.protobuf.nano;

import java.io.IOException;
/* loaded from: a.zip:com/google/protobuf/nano/CodedInputByteBufferNano.class */
public final class CodedInputByteBufferNano {
    private final byte[] buffer;
    private int bufferPos;
    private int bufferSize;
    private int bufferSizeAfterLimit;
    private int bufferStart;
    private int lastTag;
    private int recursionDepth;
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

    private void recomputeBufferSizeAfterLimit() {
        this.bufferSize += this.bufferSizeAfterLimit;
        int i = this.bufferSize;
        if (i <= this.currentLimit) {
            this.bufferSizeAfterLimit = 0;
            return;
        }
        this.bufferSizeAfterLimit = i - this.currentLimit;
        this.bufferSize -= this.bufferSizeAfterLimit;
    }

    public void checkLastTagWas(int i) throws InvalidProtocolBufferNanoException {
        if (this.lastTag != i) {
            throw InvalidProtocolBufferNanoException.invalidEndTag();
        }
    }

    public int getPosition() {
        return this.bufferPos - this.bufferStart;
    }

    public boolean isAtEnd() {
        return this.bufferPos == this.bufferSize;
    }

    public void popLimit(int i) {
        this.currentLimit = i;
        recomputeBufferSizeAfterLimit();
    }

    public int pushLimit(int i) throws InvalidProtocolBufferNanoException {
        if (i < 0) {
            throw InvalidProtocolBufferNanoException.negativeSize();
        }
        int i2 = i + this.bufferPos;
        int i3 = this.currentLimit;
        if (i2 > i3) {
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        }
        this.currentLimit = i2;
        recomputeBufferSizeAfterLimit();
        return i3;
    }

    public boolean readBool() throws IOException {
        boolean z = false;
        if (readRawVarint32() != 0) {
            z = true;
        }
        return z;
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readRawLittleEndian32());
    }

    public int readInt32() throws IOException {
        return readRawVarint32();
    }

    public void readMessage(MessageNano messageNano) throws IOException {
        int readRawVarint32 = readRawVarint32();
        if (this.recursionDepth >= this.recursionLimit) {
            throw InvalidProtocolBufferNanoException.recursionLimitExceeded();
        }
        int pushLimit = pushLimit(readRawVarint32);
        this.recursionDepth++;
        messageNano.mergeFrom(this);
        checkLastTagWas(0);
        this.recursionDepth--;
        popLimit(pushLimit);
    }

    public byte readRawByte() throws IOException {
        if (this.bufferPos == this.bufferSize) {
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        }
        byte[] bArr = this.buffer;
        int i = this.bufferPos;
        this.bufferPos = i + 1;
        return bArr[i];
    }

    public int readRawLittleEndian32() throws IOException {
        return (readRawByte() & 255) | ((readRawByte() & 255) << 8) | ((readRawByte() & 255) << 16) | ((readRawByte() & 255) << 24);
    }

    public long readRawLittleEndian64() throws IOException {
        return (readRawByte() & 255) | ((readRawByte() & 255) << 8) | ((readRawByte() & 255) << 16) | ((readRawByte() & 255) << 24) | ((readRawByte() & 255) << 32) | ((readRawByte() & 255) << 40) | ((readRawByte() & 255) << 48) | ((readRawByte() & 255) << 56);
    }

    public int readRawVarint32() throws IOException {
        int i;
        byte readRawByte = readRawByte();
        if (readRawByte >= 0) {
            return readRawByte;
        }
        int i2 = readRawByte & Byte.MAX_VALUE;
        byte readRawByte2 = readRawByte();
        if (readRawByte2 >= 0) {
            i = i2 | (readRawByte2 << 7);
        } else {
            int i3 = i2 | ((readRawByte2 & Byte.MAX_VALUE) << 7);
            byte readRawByte3 = readRawByte();
            if (readRawByte3 >= 0) {
                i = i3 | (readRawByte3 << 14);
            } else {
                int i4 = i3 | ((readRawByte3 & Byte.MAX_VALUE) << 14);
                byte readRawByte4 = readRawByte();
                if (readRawByte4 >= 0) {
                    i = i4 | (readRawByte4 << 21);
                } else {
                    byte readRawByte5 = readRawByte();
                    int i5 = i4 | ((readRawByte4 & Byte.MAX_VALUE) << 21) | (readRawByte5 << 28);
                    i = i5;
                    if (readRawByte5 < 0) {
                        for (int i6 = 0; i6 < 5; i6++) {
                            if (readRawByte() >= 0) {
                                return i5;
                            }
                        }
                        throw InvalidProtocolBufferNanoException.malformedVarint();
                    }
                }
            }
        }
        return i;
    }

    public long readRawVarint64() throws IOException {
        byte readRawByte;
        long j = 0;
        for (int i = 0; i < 64; i += 7) {
            j |= (readRawByte & Byte.MAX_VALUE) << i;
            if ((readRawByte() & 128) == 0) {
                return j;
            }
        }
        throw InvalidProtocolBufferNanoException.malformedVarint();
    }

    public String readString() throws IOException {
        int readRawVarint32 = readRawVarint32();
        if (readRawVarint32 < 0) {
            throw InvalidProtocolBufferNanoException.negativeSize();
        }
        if (readRawVarint32 > this.bufferSize - this.bufferPos) {
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        }
        String str = new String(this.buffer, this.bufferPos, readRawVarint32, "UTF-8");
        this.bufferPos += readRawVarint32;
        return str;
    }

    public int readTag() throws IOException {
        if (isAtEnd()) {
            this.lastTag = 0;
            return 0;
        }
        this.lastTag = readRawVarint32();
        if (this.lastTag == 0) {
            throw InvalidProtocolBufferNanoException.invalidTag();
        }
        return this.lastTag;
    }

    public long readUInt64() throws IOException {
        return readRawVarint64();
    }

    public void rewindToPosition(int i) {
        if (i > this.bufferPos - this.bufferStart) {
            throw new IllegalArgumentException("Position " + i + " is beyond current " + (this.bufferPos - this.bufferStart));
        }
        if (i < 0) {
            throw new IllegalArgumentException("Bad position " + i);
        }
        this.bufferPos = this.bufferStart + i;
    }

    public boolean skipField(int i) throws IOException {
        switch (WireFormatNano.getTagWireType(i)) {
            case 0:
                readInt32();
                return true;
            case 1:
                readRawLittleEndian64();
                return true;
            case 2:
                skipRawBytes(readRawVarint32());
                return true;
            case 3:
                skipMessage();
                checkLastTagWas(WireFormatNano.makeTag(WireFormatNano.getTagFieldNumber(i), 4));
                return true;
            case 4:
                return false;
            case 5:
                readRawLittleEndian32();
                return true;
            default:
                throw InvalidProtocolBufferNanoException.invalidWireType();
        }
    }

    public void skipMessage() throws IOException {
        int readTag;
        do {
            readTag = readTag();
            if (readTag == 0) {
                return;
            }
        } while (skipField(readTag));
    }

    public void skipRawBytes(int i) throws IOException {
        if (i < 0) {
            throw InvalidProtocolBufferNanoException.negativeSize();
        }
        if (this.bufferPos + i > this.currentLimit) {
            skipRawBytes(this.currentLimit - this.bufferPos);
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        } else if (i > this.bufferSize - this.bufferPos) {
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        } else {
            this.bufferPos += i;
        }
    }
}
