package android.support.v4.os;

import android.os.Parcelable;
/* loaded from: a.zip:android/support/v4/os/ParcelableCompatCreatorHoneycombMR2Stub.class */
class ParcelableCompatCreatorHoneycombMR2Stub {
    ParcelableCompatCreatorHoneycombMR2Stub() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <T> Parcelable.Creator<T> instantiate(ParcelableCompatCreatorCallbacks<T> parcelableCompatCreatorCallbacks) {
        return new ParcelableCompatCreatorHoneycombMR2(parcelableCompatCreatorCallbacks);
    }
}
