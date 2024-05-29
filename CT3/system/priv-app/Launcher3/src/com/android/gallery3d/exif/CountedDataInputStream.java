package com.android.gallery3d.exif;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
/* loaded from: a.zip:com/android/gallery3d/exif/CountedDataInputStream.class */
class CountedDataInputStream extends FilterInputStream {

    /* renamed from: -assertionsDisabled  reason: not valid java name */
    static final boolean f0assertionsDisabled;
    private final byte[] mByteArray;
    private final ByteBuffer mByteBuffer;
    private int mCount;

    static {
        f0assertionsDisabled = !CountedDataInputStream.class.desiredAssertionStatus();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public CountedDataInputStream(InputStream inputStream) {
        super(inputStream);
        this.mCount = 0;
        this.mByteArray = new byte[8];
        this.mByteBuffer = ByteBuffer.wrap(this.mByteArray);
    }

    public ByteOrder getByteOrder() {
        return this.mByteBuffer.order();
    }

    public int getReadByteCount() {
        return this.mCount;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public int read() throws IOException {
        int i = 0;
        int read = this.in.read();
        int i2 = this.mCount;
        if (read >= 0) {
            i = 1;
        }
        this.mCount = i + i2;
        return read;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public int read(byte[] bArr) throws IOException {
        int i = 0;
        int read = this.in.read(bArr);
        int i2 = this.mCount;
        if (read >= 0) {
            i = read;
        }
        this.mCount = i + i2;
        return read;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public int read(byte[] bArr, int i, int i2) throws IOException {
        int read = this.in.read(bArr, i, i2);
        int i3 = this.mCount;
        int i4 = 0;
        if (read >= 0) {
            i4 = read;
        }
        this.mCount = i4 + i3;
        return read;
    }

    public int readInt() throws IOException {
        readOrThrow(this.mByteArray, 0, 4);
        this.mByteBuffer.rewind();
        return this.mByteBuffer.getInt();
    }

    public void readOrThrow(byte[] bArr) throws IOException {
        readOrThrow(bArr, 0, bArr.length);
    }

    public void readOrThrow(byte[] bArr, int i, int i2) throws IOException {
        if (read(bArr, i, i2) != i2) {
            throw new EOFException();
        }
    }

    public short readShort() throws IOException {
        readOrThrow(this.mByteArray, 0, 2);
        this.mByteBuffer.rewind();
        return this.mByteBuffer.getShort();
    }

    public String readString(int i, Charset charset) throws IOException {
        byte[] bArr = new byte[i];
        readOrThrow(bArr);
        return new String(bArr, charset);
    }

    public long readUnsignedInt() throws IOException {
        return readInt() & 4294967295L;
    }

    public int readUnsignedShort() throws IOException {
        return readShort() & 65535;
    }

    public void setByteOrder(ByteOrder byteOrder) {
        this.mByteBuffer.order(byteOrder);
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public long skip(long j) throws IOException {
        long skip = this.in.skip(j);
        this.mCount = (int) (this.mCount + skip);
        return skip;
    }

    public void skipOrThrow(long j) throws IOException {
        if (skip(j) != j) {
            throw new EOFException();
        }
    }

    public void skipTo(long j) throws IOException {
        long j2 = j - this.mCount;
        if (!f0assertionsDisabled) {
            if (!(j2 >= 0)) {
                throw new AssertionError();
            }
        }
        skipOrThrow(j2);
    }
}
