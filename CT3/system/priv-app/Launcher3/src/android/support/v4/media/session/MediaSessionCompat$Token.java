package android.support.v4.media.session;

import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
/* loaded from: a.zip:android/support/v4/media/session/MediaSessionCompat$Token.class */
public final class MediaSessionCompat$Token implements Parcelable {
    public static final Parcelable.Creator<MediaSessionCompat$Token> CREATOR = new Parcelable.Creator<MediaSessionCompat$Token>() { // from class: android.support.v4.media.session.MediaSessionCompat$Token.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaSessionCompat$Token createFromParcel(Parcel parcel) {
            return new MediaSessionCompat$Token(Build.VERSION.SDK_INT >= 21 ? parcel.readParcelable(null) : parcel.readStrongBinder());
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaSessionCompat$Token[] newArray(int i) {
            return new MediaSessionCompat$Token[i];
        }
    };
    private final Object mInner;

    MediaSessionCompat$Token(Object obj) {
        this.mInner = obj;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        if (Build.VERSION.SDK_INT >= 21) {
            parcel.writeParcelable((Parcelable) this.mInner, i);
        } else {
            parcel.writeStrongBinder((IBinder) this.mInner);
        }
    }
}
