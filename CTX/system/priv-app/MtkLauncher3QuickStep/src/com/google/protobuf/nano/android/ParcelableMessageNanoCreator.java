package com.google.protobuf.nano.android;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
/* loaded from: classes.dex */
public final class ParcelableMessageNanoCreator<T extends MessageNano> implements Parcelable.Creator<T> {
    private static final String TAG = "PMNCreator";
    private final Class<T> mClazz;

    public ParcelableMessageNanoCreator(Class<T> cls) {
        this.mClazz = cls;
    }

    @Override // android.os.Parcelable.Creator
    public T createFromParcel(Parcel parcel) {
        T t;
        String readString = parcel.readString();
        byte[] createByteArray = parcel.createByteArray();
        try {
            t = (T) Class.forName(readString, false, getClass().getClassLoader()).asSubclass(MessageNano.class).getConstructor(new Class[0]).newInstance(new Object[0]);
            try {
                MessageNano.mergeFrom(t, createByteArray);
            } catch (InvalidProtocolBufferNanoException e) {
                e = e;
                Log.e(TAG, "Exception trying to create proto from parcel", e);
                return t;
            } catch (ClassNotFoundException e2) {
                e = e2;
                Log.e(TAG, "Exception trying to create proto from parcel", e);
                return t;
            } catch (IllegalAccessException e3) {
                e = e3;
                Log.e(TAG, "Exception trying to create proto from parcel", e);
                return t;
            } catch (InstantiationException e4) {
                e = e4;
                Log.e(TAG, "Exception trying to create proto from parcel", e);
                return t;
            } catch (NoSuchMethodException e5) {
                e = e5;
                Log.e(TAG, "Exception trying to create proto from parcel", e);
                return t;
            } catch (InvocationTargetException e6) {
                e = e6;
                Log.e(TAG, "Exception trying to create proto from parcel", e);
                return t;
            }
        } catch (InvalidProtocolBufferNanoException e7) {
            e = e7;
            t = null;
        } catch (ClassNotFoundException e8) {
            e = e8;
            t = null;
        } catch (IllegalAccessException e9) {
            e = e9;
            t = null;
        } catch (InstantiationException e10) {
            e = e10;
            t = null;
        } catch (NoSuchMethodException e11) {
            e = e11;
            t = null;
        } catch (InvocationTargetException e12) {
            e = e12;
            t = null;
        }
        return t;
    }

    @Override // android.os.Parcelable.Creator
    public T[] newArray(int i) {
        return (T[]) ((MessageNano[]) Array.newInstance((Class<?>) this.mClazz, i));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <T extends MessageNano> void writeToParcel(Class<T> cls, MessageNano messageNano, Parcel parcel) {
        parcel.writeString(cls.getName());
        parcel.writeByteArray(MessageNano.toByteArray(messageNano));
    }
}
