package android.support.v4.media.session;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaDescriptionCompat;
/* loaded from: classes.dex */
public final class MediaSessionCompat$QueueItem implements Parcelable {
    public static final Parcelable.Creator<MediaSessionCompat$QueueItem> CREATOR = new Parcelable.Creator<MediaSessionCompat$QueueItem>() { // from class: android.support.v4.media.session.MediaSessionCompat$QueueItem.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaSessionCompat$QueueItem createFromParcel(Parcel p) {
            return new MediaSessionCompat$QueueItem(p, null);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaSessionCompat$QueueItem[] newArray(int size) {
            return new MediaSessionCompat$QueueItem[size];
        }
    };
    private final MediaDescriptionCompat mDescription;
    private final long mId;

    /* synthetic */ MediaSessionCompat$QueueItem(Parcel in, MediaSessionCompat$QueueItem mediaSessionCompat$QueueItem) {
        this(in);
    }

    private MediaSessionCompat$QueueItem(Parcel in) {
        this.mDescription = MediaDescriptionCompat.CREATOR.createFromParcel(in);
        this.mId = in.readLong();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        this.mDescription.writeToParcel(dest, flags);
        dest.writeLong(this.mId);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "MediaSession.QueueItem {Description=" + this.mDescription + ", Id=" + this.mId + " }";
    }
}
