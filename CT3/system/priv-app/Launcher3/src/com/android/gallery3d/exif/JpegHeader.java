package com.android.gallery3d.exif;
/* loaded from: a.zip:com/android/gallery3d/exif/JpegHeader.class */
class JpegHeader {
    JpegHeader() {
    }

    public static final boolean isSofMarker(short s) {
        boolean z = false;
        if (s >= -64) {
            z = false;
            if (s <= -49) {
                z = false;
                if (s != -60) {
                    z = false;
                    if (s != -56) {
                        z = false;
                        if (s != -52) {
                            z = true;
                        }
                    }
                }
            }
        }
        return z;
    }
}
