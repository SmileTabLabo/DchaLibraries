package android.support.v4.media;

import android.annotation.TargetApi;
import android.media.MediaTimestamp;
import android.support.annotation.RestrictTo;
/* loaded from: classes.dex */
public final class MediaTimestamp2 {
    public static final MediaTimestamp2 TIMESTAMP_UNKNOWN = new MediaTimestamp2(-1, -1, 0.0f);
    private final float mClockRate;
    private final long mMediaTimeUs;
    private final long mNanoTime;

    public long getAnchorMediaTimeUs() {
        return this.mMediaTimeUs;
    }

    public long getAnchorSytemNanoTime() {
        return this.mNanoTime;
    }

    public float getMediaClockRate() {
        return this.mClockRate;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @TargetApi(23)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public MediaTimestamp2(MediaTimestamp timestamp) {
        this.mMediaTimeUs = timestamp.getAnchorMediaTimeUs();
        this.mNanoTime = timestamp.getAnchorSytemNanoTime();
        this.mClockRate = timestamp.getMediaClockRate();
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    MediaTimestamp2(long mediaUs, long systemNs, float rate) {
        this.mMediaTimeUs = mediaUs;
        this.mNanoTime = systemNs;
        this.mClockRate = rate;
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    MediaTimestamp2() {
        this.mMediaTimeUs = 0L;
        this.mNanoTime = 0L;
        this.mClockRate = 1.0f;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MediaTimestamp2 that = (MediaTimestamp2) obj;
        if (this.mMediaTimeUs == that.mMediaTimeUs && this.mNanoTime == that.mNanoTime && this.mClockRate == that.mClockRate) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = Long.valueOf(this.mMediaTimeUs).hashCode();
        return (int) ((31 * ((int) ((31 * result) + this.mNanoTime))) + this.mClockRate);
    }

    public String toString() {
        return getClass().getName() + "{AnchorMediaTimeUs=" + this.mMediaTimeUs + " AnchorSystemNanoTime=" + this.mNanoTime + " ClockRate=" + this.mClockRate + "}";
    }
}
