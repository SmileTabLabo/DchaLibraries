package android.support.v4.media.session;

import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
/* loaded from: classes.dex */
public final class MediaSessionCompat$Token implements Parcelable {
    public static final Parcelable.Creator<MediaSessionCompat$Token> CREATOR = new Parcelable.Creator<MediaSessionCompat$Token>() { // from class: android.support.v4.media.session.MediaSessionCompat$Token.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaSessionCompat$Token createFromParcel(Parcel in) {
            Object inner;
            if (Build.VERSION.SDK_INT >= 21) {
                inner = in.readParcelable(null);
            } else {
                inner = in.readStrongBinder();
            }
            return new MediaSessionCompat$Token(inner);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaSessionCompat$Token[] newArray(int size) {
            return new MediaSessionCompat$Token[size];
        }
    };
    private final Object mInner;

    MediaSessionCompat$Token(Object inner) {
        this.mInner = inner;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        if (Build.VERSION.SDK_INT >= 21) {
            dest.writeParcelable((Parcelable) this.mInner, flags);
        } else {
            dest.writeStrongBinder((IBinder) this.mInner);
        }
    }
}
