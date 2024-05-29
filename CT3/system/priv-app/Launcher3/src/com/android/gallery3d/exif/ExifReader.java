package com.android.gallery3d.exif;

import android.util.Log;
import com.android.launcher3.compat.PackageInstallerCompat;
import java.io.IOException;
import java.io.InputStream;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/gallery3d/exif/ExifReader.class */
public class ExifReader {
    private final ExifInterface mInterface;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ExifReader(ExifInterface exifInterface) {
        this.mInterface = exifInterface;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public ExifData read(InputStream inputStream) throws ExifInvalidFormatException, IOException {
        ExifParser parse = ExifParser.parse(inputStream, this.mInterface);
        ExifData exifData = new ExifData(parse.getByteOrder());
        int next = parse.next();
        while (true) {
            int i = next;
            if (i == 5) {
                return exifData;
            }
            switch (i) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    exifData.addIfdData(new IfdData(parse.getCurrentIfd()));
                    break;
                case 1:
                    ExifTag tag = parse.getTag();
                    if (!tag.hasValue()) {
                        parse.registerForTagValue(tag);
                        break;
                    } else {
                        exifData.getIfdData(tag.getIfd()).setTag(tag);
                        break;
                    }
                case 2:
                    ExifTag tag2 = parse.getTag();
                    if (tag2.getDataType() == 7) {
                        parse.readFullTagValue(tag2);
                    }
                    exifData.getIfdData(tag2.getIfd()).setTag(tag2);
                    break;
                case 3:
                    byte[] bArr = new byte[parse.getCompressedImageSize()];
                    if (bArr.length != parse.read(bArr)) {
                        Log.w("ExifReader", "Failed to read the compressed thumbnail");
                        break;
                    } else {
                        exifData.setCompressedThumbnail(bArr);
                        break;
                    }
                case 4:
                    byte[] bArr2 = new byte[parse.getStripSize()];
                    if (bArr2.length != parse.read(bArr2)) {
                        Log.w("ExifReader", "Failed to read the strip bytes");
                        break;
                    } else {
                        exifData.setStripBytes(parse.getStripIndex(), bArr2);
                        break;
                    }
            }
            next = parse.next();
        }
    }
}
