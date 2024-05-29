package android.support.v4.media.session;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
/* loaded from: classes.dex */
final class MediaSessionCompat$ResultReceiverWrapper implements Parcelable {
    public static final Parcelable.Creator<MediaSessionCompat$ResultReceiverWrapper> CREATOR = new Parcelable.Creator<MediaSessionCompat$ResultReceiverWrapper>() { // from class: android.support.v4.media.session.MediaSessionCompat$ResultReceiverWrapper.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaSessionCompat$ResultReceiverWrapper createFromParcel(Parcel p) {
            return new MediaSessionCompat$ResultReceiverWrapper(p);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaSessionCompat$ResultReceiverWrapper[] newArray(int size) {
            return new MediaSessionCompat$ResultReceiverWrapper[size];
        }
    };
    private ResultReceiver mResultReceiver;

    MediaSessionCompat$ResultReceiverWrapper(Parcel in) {
        this.mResultReceiver = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(in);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        this.mResultReceiver.writeToParcel(dest, flags);
    }
}
