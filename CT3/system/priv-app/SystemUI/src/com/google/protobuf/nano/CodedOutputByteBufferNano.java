package com.google.protobuf.nano;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ReadOnlyBufferException;
/* loaded from: a.zip:com/google/protobuf/nano/CodedOutputByteBufferNano.class */
public final class CodedOutputByteBufferNano {
    private final ByteBuffer buffer;

    /* loaded from: a.zip:com/google/protobuf/nano/CodedOutputByteBufferNano$OutOfSpaceException.class */
    public static class OutOfSpaceException extends IOException {
        private static final long serialVersionUID = -6947486886997889499L;

        OutOfSpaceException(int i, int i2) {
            super("CodedOutputStream was writing to a flat byte array and ran out of space (pos " + i + " limit " + i2 + ").");
        }
    }

    private CodedOutputByteBufferNano(ByteBuffer byteBuffer) {
        this.buffer = byteBuffer;
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    private CodedOutputByteBufferNano(byte[] bArr, int i, int i2) {
        this(ByteBuffer.wrap(bArr, i, i2));
    }

    public static int computeBoolSize(int i, boolean z) {
        return computeTagSize(i) + computeBoolSizeNoTag(z);
    }

    public static int computeBoolSizeNoTag(boolean z) {
        return 1;
    }

    public static int computeFloatSize(int i, float f) {
        return computeTagSize(i) + computeFloatSizeNoTag(f);
    }

    public static int computeFloatSizeNoTag(float f) {
        return 4;
    }

    public static int computeGroupSize(int i, MessageNano messageNano) {
        return (computeTagSize(i) * 2) + computeGroupSizeNoTag(messageNano);
    }

    public static int computeGroupSizeNoTag(MessageNano messageNano) {
        return messageNano.getSerializedSize();
    }

    public static int computeInt32Size(int i, int i2) {
        return computeTagSize(i) + computeInt32SizeNoTag(i2);
    }

    public static int computeInt32SizeNoTag(int i) {
        if (i >= 0) {
            return computeRawVarint32Size(i);
        }
        return 10;
    }

    public static int computeMessageSize(int i, MessageNano messageNano) {
        return computeTagSize(i) + computeMessageSizeNoTag(messageNano);
    }

    public static int computeMessageSizeNoTag(MessageNano messageNano) {
        int serializedSize = messageNano.getSerializedSize();
        return computeRawVarint32Size(serializedSize) + serializedSize;
    }

    public static int computeRawVarint32Size(int i) {
        if ((i & (-128)) == 0) {
            return 1;
        }
        if ((i & (-16384)) == 0) {
            return 2;
        }
        if (((-2097152) & i) == 0) {
            return 3;
        }
        return ((-268435456) & i) == 0 ? 4 : 5;
    }

    public static int computeRawVarint64Size(long j) {
        if (((-128) & j) == 0) {
            return 1;
        }
        if (((-16384) & j) == 0) {
            return 2;
        }
        if (((-2097152) & j) == 0) {
            return 3;
        }
        if (((-268435456) & j) == 0) {
            return 4;
        }
        if (((-34359738368L) & j) == 0) {
            return 5;
        }
        if (((-4398046511104L) & j) == 0) {
            return 6;
        }
        if (((-562949953421312L) & j) == 0) {
            return 7;
        }
        if (((-72057594037927936L) & j) == 0) {
            return 8;
        }
        return (Long.MIN_VALUE & j) == 0 ? 9 : 10;
    }

    public static int computeStringSize(int i, String str) {
        return computeTagSize(i) + computeStringSizeNoTag(str);
    }

    public static int computeStringSizeNoTag(String str) {
        int encodedLength = encodedLength(str);
        return computeRawVarint32Size(encodedLength) + encodedLength;
    }

    public static int computeTagSize(int i) {
        return computeRawVarint32Size(WireFormatNano.makeTag(i, 0));
    }

    public static int computeUInt64Size(int i, long j) {
        return computeTagSize(i) + computeUInt64SizeNoTag(j);
    }

    public static int computeUInt64SizeNoTag(long j) {
        return computeRawVarint64Size(j);
    }

    private static int encode(CharSequence charSequence, byte[] bArr, int i, int i2) {
        char charAt;
        int length = charSequence.length();
        int i3 = i + i2;
        int i4 = 0;
        while (i4 < length && i4 + i < i3 && (charAt = charSequence.charAt(i4)) < 128) {
            bArr[i + i4] = (byte) charAt;
            i4++;
        }
        if (i4 == length) {
            return i + length;
        }
        int i5 = i + i4;
        while (i4 < length) {
            char charAt2 = charSequence.charAt(i4);
            if (charAt2 < 128 && i5 < i3) {
                bArr[i5] = (byte) charAt2;
                i5++;
            } else if (charAt2 < 2048 && i5 <= i3 - 2) {
                int i6 = i5 + 1;
                bArr[i5] = (byte) ((charAt2 >>> 6) | 960);
                bArr[i6] = (byte) ((charAt2 & '?') | 128);
                i5 = i6 + 1;
            } else if ((charAt2 >= 55296 && 57343 >= charAt2) || i5 > i3 - 3) {
                if (i5 > i3 - 4) {
                    if (55296 > charAt2 || charAt2 > 57343 || (i4 + 1 != charSequence.length() && Character.isSurrogatePair(charAt2, charSequence.charAt(i4 + 1)))) {
                        throw new ArrayIndexOutOfBoundsException("Failed writing " + charAt2 + " at index " + i5);
                    }
                    throw new IllegalArgumentException("Unpaired surrogate at index " + i4);
                }
                int i7 = i4;
                if (i4 + 1 != charSequence.length()) {
                    i4++;
                    char charAt3 = charSequence.charAt(i4);
                    i7 = i4;
                    if (Character.isSurrogatePair(charAt2, charAt3)) {
                        int codePoint = Character.toCodePoint(charAt2, charAt3);
                        int i8 = i5 + 1;
                        bArr[i5] = (byte) ((codePoint >>> 18) | 240);
                        int i9 = i8 + 1;
                        bArr[i8] = (byte) (((codePoint >>> 12) & 63) | 128);
                        int i10 = i9 + 1;
                        bArr[i9] = (byte) (((codePoint >>> 6) & 63) | 128);
                        bArr[i10] = (byte) ((codePoint & 63) | 128);
                        i5 = i10 + 1;
                    }
                }
                throw new IllegalArgumentException("Unpaired surrogate at index " + (i7 - 1));
            } else {
                int i11 = i5 + 1;
                bArr[i5] = (byte) ((charAt2 >>> '\f') | 480);
                int i12 = i11 + 1;
                bArr[i11] = (byte) (((charAt2 >>> 6) & 63) | 128);
                i5 = i12 + 1;
                bArr[i12] = (byte) ((charAt2 & '?') | 128);
            }
            i4++;
        }
        return i5;
    }

    private static void encode(CharSequence charSequence, ByteBuffer byteBuffer) {
        if (byteBuffer.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        if (!byteBuffer.hasArray()) {
            encodeDirect(charSequence, byteBuffer);
            return;
        }
        try {
            byteBuffer.position(encode(charSequence, byteBuffer.array(), byteBuffer.arrayOffset() + byteBuffer.position(), byteBuffer.remaining()) - byteBuffer.arrayOffset());
        } catch (ArrayIndexOutOfBoundsException e) {
            BufferOverflowException bufferOverflowException = new BufferOverflowException();
            bufferOverflowException.initCause(e);
            throw bufferOverflowException;
        }
    }

    private static void encodeDirect(CharSequence charSequence, ByteBuffer byteBuffer) {
        int length = charSequence.length();
        int i = 0;
        while (i < length) {
            char charAt = charSequence.charAt(i);
            if (charAt < 128) {
                byteBuffer.put((byte) charAt);
            } else if (charAt < 2048) {
                byteBuffer.put((byte) ((charAt >>> 6) | 960));
                byteBuffer.put((byte) ((charAt & '?') | 128));
            } else if (charAt >= 55296 && 57343 >= charAt) {
                int i2 = i;
                if (i + 1 != charSequence.length()) {
                    i++;
                    char charAt2 = charSequence.charAt(i);
                    i2 = i;
                    if (Character.isSurrogatePair(charAt, charAt2)) {
                        int codePoint = Character.toCodePoint(charAt, charAt2);
                        byteBuffer.put((byte) ((codePoint >>> 18) | 240));
                        byteBuffer.put((byte) (((codePoint >>> 12) & 63) | 128));
                        byteBuffer.put((byte) (((codePoint >>> 6) & 63) | 128));
                        byteBuffer.put((byte) ((codePoint & 63) | 128));
                    }
                }
                throw new IllegalArgumentException("Unpaired surrogate at index " + (i2 - 1));
            } else {
                byteBuffer.put((byte) ((charAt >>> '\f') | 480));
                byteBuffer.put((byte) (((charAt >>> 6) & 63) | 128));
                byteBuffer.put((byte) ((charAt & '?') | 128));
            }
            i++;
        }
    }

    private static int encodedLength(CharSequence charSequence) {
        int i;
        int i2;
        int i3;
        int length = charSequence.length();
        int i4 = 0;
        while (true) {
            i = i4;
            i2 = length;
            if (i4 >= length) {
                break;
            }
            i = i4;
            i2 = length;
            if (charSequence.charAt(i4) >= 128) {
                break;
            }
            i4++;
        }
        while (true) {
            i3 = i2;
            if (i < length) {
                char charAt = charSequence.charAt(i);
                if (charAt >= 2048) {
                    i3 = i2 + encodedLengthGeneral(charSequence, i);
                    break;
                }
                i2 += (127 - charAt) >>> 31;
                i++;
            } else {
                break;
            }
        }
        if (i3 < length) {
            throw new IllegalArgumentException("UTF-8 length does not fit in int: " + (i3 + 4294967296L));
        }
        return i3;
    }

    private static int encodedLengthGeneral(CharSequence charSequence, int i) {
        int i2;
        int length = charSequence.length();
        int i3 = 0;
        while (i < length) {
            char charAt = charSequence.charAt(i);
            if (charAt < 2048) {
                i3 += (127 - charAt) >>> 31;
                i2 = i;
            } else {
                int i4 = i3 + 2;
                i2 = i;
                i3 = i4;
                if (55296 <= charAt) {
                    i2 = i;
                    i3 = i4;
                    if (charAt > 57343) {
                        continue;
                    } else if (Character.codePointAt(charSequence, i) < 65536) {
                        throw new IllegalArgumentException("Unpaired surrogate at index " + i);
                    } else {
                        i2 = i + 1;
                        i3 = i4;
                    }
                } else {
                    continue;
                }
            }
            i = i2 + 1;
        }
        return i3;
    }

    public static CodedOutputByteBufferNano newInstance(byte[] bArr) {
        return newInstance(bArr, 0, bArr.length);
    }

    public static CodedOutputByteBufferNano newInstance(byte[] bArr, int i, int i2) {
        return new CodedOutputByteBufferNano(bArr, i, i2);
    }

    public void checkNoSpaceLeft() {
        if (spaceLeft() != 0) {
            throw new IllegalStateException("Did not write as much data as expected.");
        }
    }

    public int spaceLeft() {
        return this.buffer.remaining();
    }

    public void writeBool(int i, boolean z) throws IOException {
        writeTag(i, 0);
        writeBoolNoTag(z);
    }

    public void writeBoolNoTag(boolean z) throws IOException {
        writeRawByte(z ? 1 : 0);
    }

    public void writeFloat(int i, float f) throws IOException {
        writeTag(i, 5);
        writeFloatNoTag(f);
    }

    public void writeFloatNoTag(float f) throws IOException {
        writeRawLittleEndian32(Float.floatToIntBits(f));
    }

    public void writeGroupNoTag(MessageNano messageNano) throws IOException {
        messageNano.writeTo(this);
    }

    public void writeInt32(int i, int i2) throws IOException {
        writeTag(i, 0);
        writeInt32NoTag(i2);
    }

    public void writeInt32NoTag(int i) throws IOException {
        if (i >= 0) {
            writeRawVarint32(i);
        } else {
            writeRawVarint64(i);
        }
    }

    public void writeMessage(int i, MessageNano messageNano) throws IOException {
        writeTag(i, 2);
        writeMessageNoTag(messageNano);
    }

    public void writeMessageNoTag(MessageNano messageNano) throws IOException {
        writeRawVarint32(messageNano.getCachedSize());
        messageNano.writeTo(this);
    }

    public void writeRawByte(byte b) throws IOException {
        if (!this.buffer.hasRemaining()) {
            throw new OutOfSpaceException(this.buffer.position(), this.buffer.limit());
        }
        this.buffer.put(b);
    }

    public void writeRawByte(int i) throws IOException {
        writeRawByte((byte) i);
    }

    public void writeRawBytes(byte[] bArr) throws IOException {
        writeRawBytes(bArr, 0, bArr.length);
    }

    public void writeRawBytes(byte[] bArr, int i, int i2) throws IOException {
        if (this.buffer.remaining() < i2) {
            throw new OutOfSpaceException(this.buffer.position(), this.buffer.limit());
        }
        this.buffer.put(bArr, i, i2);
    }

    public void writeRawLittleEndian32(int i) throws IOException {
        if (this.buffer.remaining() < 4) {
            throw new OutOfSpaceException(this.buffer.position(), this.buffer.limit());
        }
        this.buffer.putInt(i);
    }

    public void writeRawVarint32(int i) throws IOException {
        while ((i & (-128)) != 0) {
            writeRawByte((i & 127) | 128);
            i >>>= 7;
        }
        writeRawByte(i);
    }

    public void writeRawVarint64(long j) throws IOException {
        while (((-128) & j) != 0) {
            writeRawByte((((int) j) & 127) | 128);
            j >>>= 7;
        }
        writeRawByte((int) j);
    }

    public void writeString(int i, String str) throws IOException {
        writeTag(i, 2);
        writeStringNoTag(str);
    }

    public void writeStringNoTag(String str) throws IOException {
        try {
            int computeRawVarint32Size = computeRawVarint32Size(str.length());
            if (computeRawVarint32Size != computeRawVarint32Size(str.length() * 3)) {
                writeRawVarint32(encodedLength(str));
                encode(str, this.buffer);
                return;
            }
            int position = this.buffer.position();
            if (this.buffer.remaining() < computeRawVarint32Size) {
                throw new OutOfSpaceException(position + computeRawVarint32Size, this.buffer.limit());
            }
            this.buffer.position(position + computeRawVarint32Size);
            encode(str, this.buffer);
            int position2 = this.buffer.position();
            this.buffer.position(position);
            writeRawVarint32((position2 - position) - computeRawVarint32Size);
            this.buffer.position(position2);
        } catch (BufferOverflowException e) {
            OutOfSpaceException outOfSpaceException = new OutOfSpaceException(this.buffer.position(), this.buffer.limit());
            outOfSpaceException.initCause(e);
            throw outOfSpaceException;
        }
    }

    public void writeTag(int i, int i2) throws IOException {
        writeRawVarint32(WireFormatNano.makeTag(i, i2));
    }

    public void writeUInt64(int i, long j) throws IOException {
        writeTag(i, 0);
        writeUInt64NoTag(j);
    }

    public void writeUInt64NoTag(long j) throws IOException {
        writeRawVarint64(j);
    }
}
