package com.android.gallery3d.exif;

import java.util.HashMap;
import java.util.Map;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/gallery3d/exif/IfdData.class */
public class IfdData {
    private static final int[] sIfds = {0, 1, 2, 3, 4};
    private final int mIfdId;
    private final Map<Short, ExifTag> mExifTags = new HashMap();
    private int mOffsetToNextIfd = 0;

    /* JADX INFO: Access modifiers changed from: package-private */
    public IfdData(int i) {
        this.mIfdId = i;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static int[] getIfds() {
        return sIfds;
    }

    public boolean equals(Object obj) {
        ExifTag[] allTags;
        if (this == obj) {
            return true;
        }
        if (obj != null && (obj instanceof IfdData)) {
            IfdData ifdData = (IfdData) obj;
            if (ifdData.getId() == this.mIfdId && ifdData.getTagCount() == getTagCount()) {
                for (ExifTag exifTag : ifdData.getAllTags()) {
                    if (!ExifInterface.isOffsetTag(exifTag.getTagId()) && !exifTag.equals(this.mExifTags.get(Short.valueOf(exifTag.getTagId())))) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    protected ExifTag[] getAllTags() {
        return (ExifTag[]) this.mExifTags.values().toArray(new ExifTag[this.mExifTags.size()]);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getId() {
        return this.mIfdId;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public ExifTag getTag(short s) {
        return this.mExifTags.get(Short.valueOf(s));
    }

    protected int getTagCount() {
        return this.mExifTags.size();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public ExifTag setTag(ExifTag exifTag) {
        exifTag.setIfd(this.mIfdId);
        return this.mExifTags.put(Short.valueOf(exifTag.getTagId()), exifTag);
    }
}
