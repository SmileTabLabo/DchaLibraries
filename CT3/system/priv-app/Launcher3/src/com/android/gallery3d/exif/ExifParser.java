package com.android.gallery3d.exif;

import android.util.Log;
import com.android.launcher3.compat.PackageInstallerCompat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/gallery3d/exif/ExifParser.class */
public class ExifParser {
    private int mApp1End;
    private boolean mContainExifData;
    private byte[] mDataAboveIfd0;
    private int mIfd0Position;
    private int mIfdType;
    private ImageEvent mImageEvent;
    private final ExifInterface mInterface;
    private ExifTag mJpegSizeTag;
    private boolean mNeedToParseOffsetsInCurrentIfd;
    private final int mOptions;
    private ExifTag mStripSizeTag;
    private ExifTag mTag;
    private int mTiffStartPosition;
    private final CountedDataInputStream mTiffStream;
    private static final Charset US_ASCII = Charset.forName("US-ASCII");
    private static final short TAG_EXIF_IFD = ExifInterface.getTrueTagKey(ExifInterface.TAG_EXIF_IFD);
    private static final short TAG_GPS_IFD = ExifInterface.getTrueTagKey(ExifInterface.TAG_GPS_IFD);
    private static final short TAG_INTEROPERABILITY_IFD = ExifInterface.getTrueTagKey(ExifInterface.TAG_INTEROPERABILITY_IFD);
    private static final short TAG_JPEG_INTERCHANGE_FORMAT = ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT);
    private static final short TAG_JPEG_INTERCHANGE_FORMAT_LENGTH = ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH);
    private static final short TAG_STRIP_OFFSETS = ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_OFFSETS);
    private static final short TAG_STRIP_BYTE_COUNTS = ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_BYTE_COUNTS);
    private int mIfdStartOffset = 0;
    private int mNumOfTagInIfd = 0;
    private int mOffsetToApp1EndFromSOF = 0;
    private final TreeMap<Integer, Object> mCorrespondingEvent = new TreeMap<>();

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/gallery3d/exif/ExifParser$ExifTagEvent.class */
    public static class ExifTagEvent {
        boolean isRequested;
        ExifTag tag;

        ExifTagEvent(ExifTag exifTag, boolean z) {
            this.tag = exifTag;
            this.isRequested = z;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/gallery3d/exif/ExifParser$IfdEvent.class */
    public static class IfdEvent {
        int ifd;
        boolean isRequested;

        IfdEvent(int i, boolean z) {
            this.ifd = i;
            this.isRequested = z;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/gallery3d/exif/ExifParser$ImageEvent.class */
    public static class ImageEvent {
        int stripIndex;
        int type;

        ImageEvent(int i) {
            this.stripIndex = 0;
            this.type = i;
        }

        ImageEvent(int i, int i2) {
            this.type = i;
            this.stripIndex = i2;
        }
    }

    private ExifParser(InputStream inputStream, int i, ExifInterface exifInterface) throws IOException, ExifInvalidFormatException {
        this.mContainExifData = false;
        if (inputStream == null) {
            throw new IOException("Null argument inputStream to ExifParser");
        }
        this.mInterface = exifInterface;
        this.mContainExifData = seekTiffData(inputStream);
        this.mTiffStream = new CountedDataInputStream(inputStream);
        this.mOptions = i;
        if (this.mContainExifData) {
            parseTiffHeader();
            long readUnsignedInt = this.mTiffStream.readUnsignedInt();
            if (readUnsignedInt > 2147483647L) {
                throw new ExifInvalidFormatException("Invalid offset " + readUnsignedInt);
            }
            this.mIfd0Position = (int) readUnsignedInt;
            this.mIfdType = 0;
            if (isIfdRequested(0) || needToParseOffsetsInCurrentIfd()) {
                registerIfd(0, readUnsignedInt);
                if (readUnsignedInt != 8) {
                    this.mDataAboveIfd0 = new byte[((int) readUnsignedInt) - 8];
                    read(this.mDataAboveIfd0);
                }
            }
        }
    }

    private boolean checkAllowed(int i, int i2) {
        int i3 = this.mInterface.getTagInfo().get(i2);
        if (i3 == 0) {
            return false;
        }
        return ExifInterface.isIfdAllowed(i3, i);
    }

    private void checkOffsetOrImageTag(ExifTag exifTag) {
        if (exifTag.getComponentCount() == 0) {
            return;
        }
        short tagId = exifTag.getTagId();
        int ifd = exifTag.getIfd();
        if (tagId == TAG_EXIF_IFD && checkAllowed(ifd, ExifInterface.TAG_EXIF_IFD)) {
            if (isIfdRequested(2) || isIfdRequested(3)) {
                registerIfd(2, exifTag.getValueAt(0));
            }
        } else if (tagId == TAG_GPS_IFD && checkAllowed(ifd, ExifInterface.TAG_GPS_IFD)) {
            if (isIfdRequested(4)) {
                registerIfd(4, exifTag.getValueAt(0));
            }
        } else if (tagId == TAG_INTEROPERABILITY_IFD && checkAllowed(ifd, ExifInterface.TAG_INTEROPERABILITY_IFD)) {
            if (isIfdRequested(3)) {
                registerIfd(3, exifTag.getValueAt(0));
            }
        } else if (tagId == TAG_JPEG_INTERCHANGE_FORMAT && checkAllowed(ifd, ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT)) {
            if (isThumbnailRequested()) {
                registerCompressedImage(exifTag.getValueAt(0));
            }
        } else if (tagId == TAG_JPEG_INTERCHANGE_FORMAT_LENGTH && checkAllowed(ifd, ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH)) {
            if (isThumbnailRequested()) {
                this.mJpegSizeTag = exifTag;
            }
        } else if (tagId != TAG_STRIP_OFFSETS || !checkAllowed(ifd, ExifInterface.TAG_STRIP_OFFSETS)) {
            if (tagId == TAG_STRIP_BYTE_COUNTS && checkAllowed(ifd, ExifInterface.TAG_STRIP_BYTE_COUNTS) && isThumbnailRequested() && exifTag.hasValue()) {
                this.mStripSizeTag = exifTag;
            }
        } else if (isThumbnailRequested()) {
            if (!exifTag.hasValue()) {
                this.mCorrespondingEvent.put(Integer.valueOf(exifTag.getOffset()), new ExifTagEvent(exifTag, false));
                return;
            }
            for (int i = 0; i < exifTag.getComponentCount(); i++) {
                if (exifTag.getDataType() == 3) {
                    registerUncompressedStrip(i, exifTag.getValueAt(i));
                } else {
                    registerUncompressedStrip(i, exifTag.getValueAt(i));
                }
            }
        }
    }

    private boolean isIfdRequested(int i) {
        boolean z = true;
        switch (i) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                if ((this.mOptions & 1) == 0) {
                    z = false;
                }
                return z;
            case 1:
                return (this.mOptions & 2) != 0;
            case 2:
                return (this.mOptions & 4) != 0;
            case 3:
                return (this.mOptions & 16) != 0;
            case 4:
                return (this.mOptions & 8) != 0;
            default:
                return false;
        }
    }

    private boolean isThumbnailRequested() {
        boolean z = false;
        if ((this.mOptions & 32) != 0) {
            z = true;
        }
        return z;
    }

    private boolean needToParseOffsetsInCurrentIfd() {
        switch (this.mIfdType) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                boolean z = true;
                if (!isIfdRequested(2)) {
                    z = true;
                    if (!isIfdRequested(4)) {
                        z = true;
                        if (!isIfdRequested(3)) {
                            z = isIfdRequested(1);
                        }
                    }
                }
                return z;
            case 1:
                return isThumbnailRequested();
            case 2:
                return isIfdRequested(3);
            default:
                return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static ExifParser parse(InputStream inputStream, ExifInterface exifInterface) throws IOException, ExifInvalidFormatException {
        return new ExifParser(inputStream, 63, exifInterface);
    }

    private void parseTiffHeader() throws IOException, ExifInvalidFormatException {
        short readShort = this.mTiffStream.readShort();
        if (18761 == readShort) {
            this.mTiffStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        } else if (19789 != readShort) {
            throw new ExifInvalidFormatException("Invalid TIFF header");
        } else {
            this.mTiffStream.setByteOrder(ByteOrder.BIG_ENDIAN);
        }
        if (this.mTiffStream.readShort() != 42) {
            throw new ExifInvalidFormatException("Invalid TIFF header");
        }
    }

    private ExifTag readTag() throws IOException, ExifInvalidFormatException {
        short readShort = this.mTiffStream.readShort();
        short readShort2 = this.mTiffStream.readShort();
        long readUnsignedInt = this.mTiffStream.readUnsignedInt();
        if (readUnsignedInt > 2147483647L) {
            throw new ExifInvalidFormatException("Number of component is larger then Integer.MAX_VALUE");
        }
        if (!ExifTag.isValidType(readShort2)) {
            Log.w("ExifParser", String.format("Tag %04x: Invalid data type %d", Short.valueOf(readShort), Short.valueOf(readShort2)));
            this.mTiffStream.skip(4L);
            return null;
        }
        ExifTag exifTag = new ExifTag(readShort, readShort2, (int) readUnsignedInt, this.mIfdType, ((int) readUnsignedInt) != 0);
        int dataSize = exifTag.getDataSize();
        if (dataSize > 4) {
            long readUnsignedInt2 = this.mTiffStream.readUnsignedInt();
            if (readUnsignedInt2 > 2147483647L) {
                throw new ExifInvalidFormatException("offset is larger then Integer.MAX_VALUE");
            }
            if (readUnsignedInt2 >= this.mIfd0Position || readShort2 != 7) {
                exifTag.setOffset((int) readUnsignedInt2);
            } else {
                byte[] bArr = new byte[(int) readUnsignedInt];
                System.arraycopy(this.mDataAboveIfd0, ((int) readUnsignedInt2) - 8, bArr, 0, (int) readUnsignedInt);
                exifTag.setValue(bArr);
            }
        } else {
            boolean hasDefinedCount = exifTag.hasDefinedCount();
            exifTag.setHasDefinedCount(false);
            readFullTagValue(exifTag);
            exifTag.setHasDefinedCount(hasDefinedCount);
            this.mTiffStream.skip(4 - dataSize);
            exifTag.setOffset(this.mTiffStream.getReadByteCount() - 4);
        }
        return exifTag;
    }

    private void registerCompressedImage(long j) {
        this.mCorrespondingEvent.put(Integer.valueOf((int) j), new ImageEvent(3));
    }

    private void registerIfd(int i, long j) {
        this.mCorrespondingEvent.put(Integer.valueOf((int) j), new IfdEvent(i, isIfdRequested(i)));
    }

    private void registerUncompressedStrip(int i, long j) {
        this.mCorrespondingEvent.put(Integer.valueOf((int) j), new ImageEvent(4, i));
    }

    private boolean seekTiffData(InputStream inputStream) throws IOException, ExifInvalidFormatException {
        CountedDataInputStream countedDataInputStream = new CountedDataInputStream(inputStream);
        if (countedDataInputStream.readShort() != -40) {
            throw new ExifInvalidFormatException("Invalid JPEG format");
        }
        short readShort = countedDataInputStream.readShort();
        while (true) {
            short s = readShort;
            if (s == -39 || JpegHeader.isSofMarker(s)) {
                return false;
            }
            int readUnsignedShort = countedDataInputStream.readUnsignedShort();
            int i = readUnsignedShort;
            if (s == -31) {
                i = readUnsignedShort;
                if (readUnsignedShort >= 8) {
                    int readInt = countedDataInputStream.readInt();
                    short readShort2 = countedDataInputStream.readShort();
                    int i2 = readUnsignedShort - 6;
                    i = i2;
                    if (readInt == 1165519206) {
                        i = i2;
                        if (readShort2 == 0) {
                            this.mTiffStartPosition = countedDataInputStream.getReadByteCount();
                            this.mApp1End = i2;
                            this.mOffsetToApp1EndFromSOF = this.mTiffStartPosition + this.mApp1End;
                            return true;
                        }
                    }
                }
            }
            if (i < 2 || i - 2 != countedDataInputStream.skip(i - 2)) {
                break;
            }
            readShort = countedDataInputStream.readShort();
        }
        Log.w("ExifParser", "Invalid JPEG format.");
        return false;
    }

    private void skipTo(int i) throws IOException {
        this.mTiffStream.skipTo(i);
        while (!this.mCorrespondingEvent.isEmpty() && this.mCorrespondingEvent.firstKey().intValue() < i) {
            this.mCorrespondingEvent.pollFirstEntry();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public ByteOrder getByteOrder() {
        return this.mTiffStream.getByteOrder();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getCompressedImageSize() {
        if (this.mJpegSizeTag == null) {
            return 0;
        }
        return (int) this.mJpegSizeTag.getValueAt(0);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getCurrentIfd() {
        return this.mIfdType;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getStripIndex() {
        return this.mImageEvent.stripIndex;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getStripSize() {
        if (this.mStripSizeTag == null) {
            return 0;
        }
        return (int) this.mStripSizeTag.getValueAt(0);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public ExifTag getTag() {
        return this.mTag;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int next() throws IOException, ExifInvalidFormatException {
        if (this.mContainExifData) {
            int readByteCount = this.mTiffStream.getReadByteCount();
            int i = this.mIfdStartOffset + 2 + (this.mNumOfTagInIfd * 12);
            if (readByteCount < i) {
                this.mTag = readTag();
                if (this.mTag == null) {
                    return next();
                }
                if (this.mNeedToParseOffsetsInCurrentIfd) {
                    checkOffsetOrImageTag(this.mTag);
                    return 1;
                }
                return 1;
            }
            if (readByteCount == i) {
                if (this.mIfdType == 0) {
                    long readUnsignedLong = readUnsignedLong();
                    if ((isIfdRequested(1) || isThumbnailRequested()) && readUnsignedLong != 0) {
                        registerIfd(1, readUnsignedLong);
                    }
                } else {
                    int i2 = 4;
                    if (this.mCorrespondingEvent.size() > 0) {
                        i2 = this.mCorrespondingEvent.firstEntry().getKey().intValue() - this.mTiffStream.getReadByteCount();
                    }
                    if (i2 < 4) {
                        Log.w("ExifParser", "Invalid size of link to next IFD: " + i2);
                    } else {
                        long readUnsignedLong2 = readUnsignedLong();
                        if (readUnsignedLong2 != 0) {
                            Log.w("ExifParser", "Invalid link to next IFD: " + readUnsignedLong2);
                        }
                    }
                }
            }
            while (this.mCorrespondingEvent.size() != 0) {
                Map.Entry<Integer, Object> pollFirstEntry = this.mCorrespondingEvent.pollFirstEntry();
                Object value = pollFirstEntry.getValue();
                try {
                    skipTo(pollFirstEntry.getKey().intValue());
                    if (value instanceof IfdEvent) {
                        this.mIfdType = ((IfdEvent) value).ifd;
                        this.mNumOfTagInIfd = this.mTiffStream.readUnsignedShort();
                        this.mIfdStartOffset = pollFirstEntry.getKey().intValue();
                        if ((this.mNumOfTagInIfd * 12) + this.mIfdStartOffset + 2 > this.mApp1End) {
                            Log.w("ExifParser", "Invalid size of IFD " + this.mIfdType);
                            return 5;
                        }
                        this.mNeedToParseOffsetsInCurrentIfd = needToParseOffsetsInCurrentIfd();
                        if (((IfdEvent) value).isRequested) {
                            return 0;
                        }
                        skipRemainingTagsInCurrentIfd();
                    } else if (value instanceof ImageEvent) {
                        this.mImageEvent = (ImageEvent) value;
                        return this.mImageEvent.type;
                    } else {
                        ExifTagEvent exifTagEvent = (ExifTagEvent) value;
                        this.mTag = exifTagEvent.tag;
                        if (this.mTag.getDataType() != 7) {
                            readFullTagValue(this.mTag);
                            checkOffsetOrImageTag(this.mTag);
                        }
                        if (exifTagEvent.isRequested) {
                            return 2;
                        }
                    }
                } catch (IOException e) {
                    Log.w("ExifParser", "Failed to skip to data at: " + pollFirstEntry.getKey() + " for " + value.getClass().getName() + ", the file may be broken.");
                }
            }
            return 5;
        }
        return 5;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int read(byte[] bArr) throws IOException {
        return this.mTiffStream.read(bArr);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void readFullTagValue(ExifTag exifTag) throws IOException {
        short dataType = exifTag.getDataType();
        if (dataType == 2 || dataType == 7 || dataType == 1) {
            int componentCount = exifTag.getComponentCount();
            if (this.mCorrespondingEvent.size() > 0 && this.mCorrespondingEvent.firstEntry().getKey().intValue() < this.mTiffStream.getReadByteCount() + componentCount) {
                Object value = this.mCorrespondingEvent.firstEntry().getValue();
                if (value instanceof ImageEvent) {
                    Log.w("ExifParser", "Thumbnail overlaps value for tag: \n" + exifTag.toString());
                    Log.w("ExifParser", "Invalid thumbnail offset: " + this.mCorrespondingEvent.pollFirstEntry().getKey());
                } else {
                    if (value instanceof IfdEvent) {
                        Log.w("ExifParser", "Ifd " + ((IfdEvent) value).ifd + " overlaps value for tag: \n" + exifTag.toString());
                    } else if (value instanceof ExifTagEvent) {
                        Log.w("ExifParser", "Tag value for tag: \n" + ((ExifTagEvent) value).tag.toString() + " overlaps value for tag: \n" + exifTag.toString());
                    }
                    int intValue = this.mCorrespondingEvent.firstEntry().getKey().intValue() - this.mTiffStream.getReadByteCount();
                    Log.w("ExifParser", "Invalid size of tag: \n" + exifTag.toString() + " setting count to: " + intValue);
                    exifTag.forceSetComponentCount(intValue);
                }
            }
        }
        switch (exifTag.getDataType()) {
            case 1:
            case 7:
                byte[] bArr = new byte[exifTag.getComponentCount()];
                read(bArr);
                exifTag.setValue(bArr);
                return;
            case 2:
                exifTag.setValue(readString(exifTag.getComponentCount()));
                return;
            case 3:
                int[] iArr = new int[exifTag.getComponentCount()];
                int length = iArr.length;
                for (int i = 0; i < length; i++) {
                    iArr[i] = readUnsignedShort();
                }
                exifTag.setValue(iArr);
                return;
            case 4:
                long[] jArr = new long[exifTag.getComponentCount()];
                int length2 = jArr.length;
                for (int i2 = 0; i2 < length2; i2++) {
                    jArr[i2] = readUnsignedLong();
                }
                exifTag.setValue(jArr);
                return;
            case 5:
                Rational[] rationalArr = new Rational[exifTag.getComponentCount()];
                int length3 = rationalArr.length;
                for (int i3 = 0; i3 < length3; i3++) {
                    rationalArr[i3] = readUnsignedRational();
                }
                exifTag.setValue(rationalArr);
                return;
            case 6:
            case 8:
            default:
                return;
            case 9:
                int[] iArr2 = new int[exifTag.getComponentCount()];
                int length4 = iArr2.length;
                for (int i4 = 0; i4 < length4; i4++) {
                    iArr2[i4] = readLong();
                }
                exifTag.setValue(iArr2);
                return;
            case 10:
                Rational[] rationalArr2 = new Rational[exifTag.getComponentCount()];
                int length5 = rationalArr2.length;
                for (int i5 = 0; i5 < length5; i5++) {
                    rationalArr2[i5] = readRational();
                }
                exifTag.setValue(rationalArr2);
                return;
        }
    }

    protected int readLong() throws IOException {
        return this.mTiffStream.readInt();
    }

    protected Rational readRational() throws IOException {
        return new Rational(readLong(), readLong());
    }

    protected String readString(int i) throws IOException {
        return readString(i, US_ASCII);
    }

    protected String readString(int i, Charset charset) throws IOException {
        return i > 0 ? this.mTiffStream.readString(i, charset) : "";
    }

    protected long readUnsignedLong() throws IOException {
        return readLong() & 4294967295L;
    }

    protected Rational readUnsignedRational() throws IOException {
        return new Rational(readUnsignedLong(), readUnsignedLong());
    }

    protected int readUnsignedShort() throws IOException {
        return this.mTiffStream.readShort() & 65535;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void registerForTagValue(ExifTag exifTag) {
        if (exifTag.getOffset() >= this.mTiffStream.getReadByteCount()) {
            this.mCorrespondingEvent.put(Integer.valueOf(exifTag.getOffset()), new ExifTagEvent(exifTag, true));
        }
    }

    protected void skipRemainingTagsInCurrentIfd() throws IOException, ExifInvalidFormatException {
        int i = this.mIfdStartOffset + 2 + (this.mNumOfTagInIfd * 12);
        int readByteCount = this.mTiffStream.getReadByteCount();
        if (readByteCount > i) {
            return;
        }
        if (this.mNeedToParseOffsetsInCurrentIfd) {
            while (readByteCount < i) {
                this.mTag = readTag();
                int i2 = readByteCount + 12;
                readByteCount = i2;
                if (this.mTag != null) {
                    checkOffsetOrImageTag(this.mTag);
                    readByteCount = i2;
                }
            }
        } else {
            skipTo(i);
        }
        long readUnsignedLong = readUnsignedLong();
        if (this.mIfdType == 0) {
            if ((isIfdRequested(1) || isThumbnailRequested()) && readUnsignedLong > 0) {
                registerIfd(1, readUnsignedLong);
            }
        }
    }
}
