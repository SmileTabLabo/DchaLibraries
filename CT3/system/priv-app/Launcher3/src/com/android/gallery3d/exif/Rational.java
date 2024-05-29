package com.android.gallery3d.exif;
/* loaded from: a.zip:com/android/gallery3d/exif/Rational.class */
public class Rational {
    private final long mDenominator;
    private final long mNumerator;

    public Rational(long j, long j2) {
        this.mNumerator = j;
        this.mDenominator = j2;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof Rational) {
            Rational rational = (Rational) obj;
            if (this.mNumerator != rational.mNumerator || this.mDenominator != rational.mDenominator) {
                z = false;
            }
            return z;
        }
        return false;
    }

    public long getDenominator() {
        return this.mDenominator;
    }

    public long getNumerator() {
        return this.mNumerator;
    }

    public String toString() {
        return this.mNumerator + "/" + this.mDenominator;
    }
}
