package android.support.v4.app;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
/* loaded from: b.zip:android/support/v4/app/Fragment$SavedState.class */
public class Fragment$SavedState implements Parcelable {
    public static final Parcelable.Creator<Fragment$SavedState> CREATOR = new Parcelable.Creator<Fragment$SavedState>() { // from class: android.support.v4.app.Fragment$SavedState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public Fragment$SavedState createFromParcel(Parcel parcel) {
            return new Fragment$SavedState(parcel, null);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public Fragment$SavedState[] newArray(int i) {
            return new Fragment$SavedState[i];
        }
    };
    final Bundle mState;

    Fragment$SavedState(Parcel parcel, ClassLoader classLoader) {
        this.mState = parcel.readBundle();
        if (classLoader == null || this.mState == null) {
            return;
        }
        this.mState.setClassLoader(classLoader);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBundle(this.mState);
    }
}
