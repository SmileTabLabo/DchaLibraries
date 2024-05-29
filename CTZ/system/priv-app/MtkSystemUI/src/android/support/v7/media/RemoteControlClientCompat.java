package android.support.v7.media;
/* loaded from: classes.dex */
abstract class RemoteControlClientCompat {

    /* loaded from: classes.dex */
    public static final class PlaybackInfo {
        public int volume;
        public int volumeMax;
        public int volumeHandling = 0;
        public int playbackStream = 3;
        public int playbackType = 1;
    }

    public void setPlaybackInfo(PlaybackInfo info) {
    }
}
