package com.google.protobuf.nano.android;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.protobuf.nano.MessageNano;
/* loaded from: a.zip:com/google/protobuf/nano/android/ParcelableMessageNano.class */
public abstract class ParcelableMessageNano extends MessageNano implements Parcelable {
    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        ParcelableMessageNanoCreator.writeToParcel(getClass(), this, parcel);
    }
}
