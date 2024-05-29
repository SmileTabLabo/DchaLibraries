package com.android.gallery3d.exif;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
/* loaded from: a.zip:com/android/gallery3d/exif/ExifTag.class */
public class ExifTag {
    private static final SimpleDateFormat TIME_FORMAT;
    private int mComponentCountActual;
    private final short mDataType;
    private boolean mHasDefinedDefaultComponentCount;
    private int mIfd;
    private int mOffset;
    private final short mTagId;
    private Object mValue = null;
    private static Charset US_ASCII = Charset.forName("US-ASCII");
    private static final int[] TYPE_TO_SIZE_MAP = new int[11];

    static {
        TYPE_TO_SIZE_MAP[1] = 1;
        TYPE_TO_SIZE_MAP[2] = 1;
        TYPE_TO_SIZE_MAP[3] = 2;
        TYPE_TO_SIZE_MAP[4] = 4;
        TYPE_TO_SIZE_MAP[5] = 8;
        TYPE_TO_SIZE_MAP[7] = 1;
        TYPE_TO_SIZE_MAP[9] = 4;
        TYPE_TO_SIZE_MAP[10] = 8;
        TIME_FORMAT = new SimpleDateFormat("yyyy:MM:dd kk:mm:ss");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ExifTag(short s, short s2, int i, int i2, boolean z) {
        this.mTagId = s;
        this.mDataType = s2;
        this.mComponentCountActual = i;
        this.mHasDefinedDefaultComponentCount = z;
        this.mIfd = i2;
    }

    private boolean checkBadComponentCount(int i) {
        return this.mHasDefinedDefaultComponentCount && this.mComponentCountActual != i;
    }

    private boolean checkOverflowForRational(Rational[] rationalArr) {
        for (Rational rational : rationalArr) {
            if (rational.getNumerator() < -2147483648L || rational.getDenominator() < -2147483648L || rational.getNumerator() > 2147483647L || rational.getDenominator() > 2147483647L) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedLong(int[] iArr) {
        for (int i : iArr) {
            if (i < 0) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedLong(long[] jArr) {
        for (long j : jArr) {
            if (j < 0 || j > 4294967295L) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedRational(Rational[] rationalArr) {
        for (Rational rational : rationalArr) {
            if (rational.getNumerator() < 0 || rational.getDenominator() < 0 || rational.getNumerator() > 4294967295L || rational.getDenominator() > 4294967295L) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedShort(int[] iArr) {
        for (int i : iArr) {
            if (i > 65535 || i < 0) {
                return true;
            }
        }
        return false;
    }

    private static String convertTypeToString(short s) {
        switch (s) {
            case 1:
                return "UNSIGNED_BYTE";
            case 2:
                return "ASCII";
            case 3:
                return "UNSIGNED_SHORT";
            case 4:
                return "UNSIGNED_LONG";
            case 5:
                return "UNSIGNED_RATIONAL";
            case 6:
            case 8:
            default:
                return "";
            case 7:
                return "UNDEFINED";
            case 9:
                return "LONG";
            case 10:
                return "RATIONAL";
        }
    }

    public static int getElementSize(short s) {
        return TYPE_TO_SIZE_MAP[s];
    }

    public static boolean isValidIfd(int i) {
        boolean z = true;
        if (i != 0) {
            if (i == 1) {
                z = true;
            } else {
                z = true;
                if (i != 2) {
                    z = true;
                    if (i != 3) {
                        z = true;
                        if (i != 4) {
                            z = false;
                        }
                    }
                }
            }
        }
        return z;
    }

    public static boolean isValidType(short s) {
        boolean z = true;
        if (s != 1) {
            if (s == 2) {
                z = true;
            } else {
                z = true;
                if (s != 3) {
                    z = true;
                    if (s != 4) {
                        z = true;
                        if (s != 5) {
                            z = true;
                            if (s != 7) {
                                z = true;
                                if (s != 9) {
                                    z = true;
                                    if (s != 10) {
                                        z = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return z;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj != null && (obj instanceof ExifTag)) {
            ExifTag exifTag = (ExifTag) obj;
            if (exifTag.mTagId == this.mTagId && exifTag.mComponentCountActual == this.mComponentCountActual && exifTag.mDataType == this.mDataType) {
                if (this.mValue == null) {
                    if (exifTag.mValue == null) {
                        z = true;
                    }
                    return z;
                } else if (exifTag.mValue == null) {
                    return false;
                } else {
                    if (this.mValue instanceof long[]) {
                        if (exifTag.mValue instanceof long[]) {
                            return Arrays.equals((long[]) this.mValue, (long[]) exifTag.mValue);
                        }
                        return false;
                    } else if (this.mValue instanceof Rational[]) {
                        if (exifTag.mValue instanceof Rational[]) {
                            return Arrays.equals((Rational[]) this.mValue, (Rational[]) exifTag.mValue);
                        }
                        return false;
                    } else if (this.mValue instanceof byte[]) {
                        if (exifTag.mValue instanceof byte[]) {
                            return Arrays.equals((byte[]) this.mValue, (byte[]) exifTag.mValue);
                        }
                        return false;
                    } else {
                        return this.mValue.equals(exifTag.mValue);
                    }
                }
            }
            return false;
        }
        return false;
    }

    public String forceGetValueAsString() {
        if (this.mValue == null) {
            return "";
        }
        if (this.mValue instanceof byte[]) {
            return this.mDataType == 2 ? new String((byte[]) this.mValue, US_ASCII) : Arrays.toString((byte[]) this.mValue);
        } else if (this.mValue instanceof long[]) {
            return ((long[]) this.mValue).length == 1 ? String.valueOf(((long[]) this.mValue)[0]) : Arrays.toString((long[]) this.mValue);
        } else if (this.mValue instanceof Object[]) {
            if (((Object[]) this.mValue).length == 1) {
                Object obj = ((Object[]) this.mValue)[0];
                return obj == null ? "" : obj.toString();
            }
            return Arrays.toString((Object[]) this.mValue);
        } else {
            return this.mValue.toString();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void forceSetComponentCount(int i) {
        this.mComponentCountActual = i;
    }

    public int getComponentCount() {
        return this.mComponentCountActual;
    }

    public int getDataSize() {
        return getComponentCount() * getElementSize(getDataType());
    }

    public short getDataType() {
        return this.mDataType;
    }

    public int getIfd() {
        return this.mIfd;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getOffset() {
        return this.mOffset;
    }

    public short getTagId() {
        return this.mTagId;
    }

    public int[] getValueAsInts() {
        if (this.mValue != null && (this.mValue instanceof long[])) {
            long[] jArr = (long[]) this.mValue;
            int[] iArr = new int[jArr.length];
            for (int i = 0; i < jArr.length; i++) {
                iArr[i] = (int) jArr[i];
            }
            return iArr;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public long getValueAt(int i) {
        if (this.mValue instanceof long[]) {
            return ((long[]) this.mValue)[i];
        }
        if (this.mValue instanceof byte[]) {
            return ((byte[]) this.mValue)[i];
        }
        throw new IllegalArgumentException("Cannot get integer value from " + convertTypeToString(this.mDataType));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean hasDefinedCount() {
        return this.mHasDefinedDefaultComponentCount;
    }

    public boolean hasValue() {
        return this.mValue != null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setHasDefinedCount(boolean z) {
        this.mHasDefinedDefaultComponentCount = z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setIfd(int i) {
        this.mIfd = i;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setOffset(int i) {
        this.mOffset = i;
    }

    public boolean setValue(String str) {
        if (this.mDataType == 2 || this.mDataType == 7) {
            byte[] bytes = str.getBytes(US_ASCII);
            if (bytes.length <= 0) {
                bytes = bytes;
                if (this.mDataType == 2) {
                    bytes = bytes;
                    if (this.mComponentCountActual == 1) {
                        bytes = new byte[]{0};
                    }
                }
            } else if (bytes[bytes.length - 1] != 0 && this.mDataType != 7) {
                bytes = Arrays.copyOf(bytes, bytes.length + 1);
            }
            int length = bytes.length;
            if (checkBadComponentCount(length)) {
                return false;
            }
            this.mComponentCountActual = length;
            this.mValue = bytes;
            return true;
        }
        return false;
    }

    public boolean setValue(byte[] bArr) {
        return setValue(bArr, 0, bArr.length);
    }

    public boolean setValue(byte[] bArr, int i, int i2) {
        if (checkBadComponentCount(i2)) {
            return false;
        }
        if (this.mDataType == 1 || this.mDataType == 7) {
            this.mValue = new byte[i2];
            System.arraycopy(bArr, i, this.mValue, 0, i2);
            this.mComponentCountActual = i2;
            return true;
        }
        return false;
    }

    public boolean setValue(int[] iArr) {
        if (checkBadComponentCount(iArr.length)) {
            return false;
        }
        if (this.mDataType == 3 || this.mDataType == 9 || this.mDataType == 4) {
            if (this.mDataType == 3 && checkOverflowForUnsignedShort(iArr)) {
                return false;
            }
            if (this.mDataType == 4 && checkOverflowForUnsignedLong(iArr)) {
                return false;
            }
            long[] jArr = new long[iArr.length];
            for (int i = 0; i < iArr.length; i++) {
                jArr[i] = iArr[i];
            }
            this.mValue = jArr;
            this.mComponentCountActual = iArr.length;
            return true;
        }
        return false;
    }

    public boolean setValue(long[] jArr) {
        if (checkBadComponentCount(jArr.length) || this.mDataType != 4 || checkOverflowForUnsignedLong(jArr)) {
            return false;
        }
        this.mValue = jArr;
        this.mComponentCountActual = jArr.length;
        return true;
    }

    public boolean setValue(Rational[] rationalArr) {
        if (checkBadComponentCount(rationalArr.length)) {
            return false;
        }
        if (this.mDataType == 5 || this.mDataType == 10) {
            if (this.mDataType == 5 && checkOverflowForUnsignedRational(rationalArr)) {
                return false;
            }
            if (this.mDataType == 10 && checkOverflowForRational(rationalArr)) {
                return false;
            }
            this.mValue = rationalArr;
            this.mComponentCountActual = rationalArr.length;
            return true;
        }
        return false;
    }

    public String toString() {
        return String.format("tag id: %04X\n", Short.valueOf(this.mTagId)) + "ifd id: " + this.mIfd + "\ntype: " + convertTypeToString(this.mDataType) + "\ncount: " + this.mComponentCountActual + "\noffset: " + this.mOffset + "\nvalue: " + forceGetValueAsString() + "\n";
    }
}
