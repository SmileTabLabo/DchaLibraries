package androidx.versionedparcelable;

import android.os.Parcelable;
/* loaded from: classes.dex */
public class ParcelUtils {
    public static Parcelable toParcelable(VersionedParcelable obj) {
        return new ParcelImpl(obj);
    }

    public static <T extends VersionedParcelable> T fromParcelable(Parcelable p) {
        if (!(p instanceof ParcelImpl)) {
            throw new IllegalArgumentException("Invalid parcel");
        }
        return (T) ((ParcelImpl) p).getVersionedParcel();
    }
}
