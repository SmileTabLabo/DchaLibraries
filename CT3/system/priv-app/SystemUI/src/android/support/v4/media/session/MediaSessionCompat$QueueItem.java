package android.support.v4.media.session;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaDescriptionCompat;
/* loaded from: a.zip:android/support/v4/media/session/MediaSessionCompat$QueueItem.class */
public final class MediaSessionCompat$QueueItem implements Parcelable {
    public static final Parcelable.Creator<MediaSessionCompat$QueueItem> CREATOR = new Parcelable.Creator<MediaSessionCompat$QueueItem>() { // from class: android.support.v4.media.session.MediaSessionCompat$QueueItem.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaSessionCompat$QueueItem createFromParcel(Parcel parcel) {
            return new MediaSessionCompat$QueueItem(parcel, null);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaSessionCompat$QueueItem[] newArray(int i) {
            return new MediaSessionCompat$QueueItem[i];
        }
    };
    private final MediaDescriptionCompat mDescription;
    private final long mId;

    private MediaSessionCompat$QueueItem(Parcel parcel) {
        this.mDescription = MediaDescriptionCompat.CREATOR.createFromParcel(parcel);
        this.mId = parcel.readLong();
    }

    /* synthetic */ MediaSessionCompat$QueueItem(Parcel parcel, MediaSessionCompat$QueueItem mediaSessionCompat$QueueItem) {
        this(parcel);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "MediaSession.QueueItem {Description=" + this.mDescription + ", Id=" + this.mId + " }";
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        this.mDescription.writeToParcel(parcel, i);
        parcel.writeLong(this.mId);
    }
}
