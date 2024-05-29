package com.google.protobuf.nano.android;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import java.lang.reflect.Array;
/* loaded from: a.zip:com/google/protobuf/nano/android/ParcelableMessageNanoCreator.class */
public final class ParcelableMessageNanoCreator<T extends MessageNano> implements Parcelable.Creator<T> {
    private final Class<T> mClazz;

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <T extends MessageNano> void writeToParcel(Class<T> cls, MessageNano messageNano, Parcel parcel) {
        parcel.writeString(cls.getName());
        parcel.writeByteArray(MessageNano.toByteArray(messageNano));
    }

    @Override // android.os.Parcelable.Creator
    public T createFromParcel(Parcel parcel) {
        T t;
        String readString = parcel.readString();
        byte[] createByteArray = parcel.createByteArray();
        T t2 = null;
        MessageNano messageNano = null;
        MessageNano messageNano2 = null;
        MessageNano messageNano3 = null;
        try {
            t = (MessageNano) Class.forName(readString).newInstance();
            messageNano3 = t;
            t2 = t;
            messageNano = t;
            messageNano2 = t;
            MessageNano.mergeFrom(t, createByteArray);
        } catch (InvalidProtocolBufferNanoException e) {
            Log.e("PMNCreator", "Exception trying to create proto from parcel", e);
            t = messageNano3;
        } catch (ClassNotFoundException e2) {
            Log.e("PMNCreator", "Exception trying to create proto from parcel", e2);
            t = messageNano2;
        } catch (IllegalAccessException e3) {
            Log.e("PMNCreator", "Exception trying to create proto from parcel", e3);
            t = messageNano;
        } catch (InstantiationException e4) {
            Log.e("PMNCreator", "Exception trying to create proto from parcel", e4);
            t = t2;
        }
        return t;
    }

    @Override // android.os.Parcelable.Creator
    public T[] newArray(int i) {
        return (T[]) ((MessageNano[]) Array.newInstance((Class<?>) this.mClazz, i));
    }
}
