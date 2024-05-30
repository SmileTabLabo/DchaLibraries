package jp.co.benesse.dcha.databox;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
/* loaded from: classes.dex */
public interface ISbox extends IInterface {

    /* loaded from: classes.dex */
    public static class Default implements ISbox {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }

        @Override // jp.co.benesse.dcha.databox.ISbox
        public String getAppIdentifier(int i) throws RemoteException {
            return null;
        }

        @Override // jp.co.benesse.dcha.databox.ISbox
        public String getArrayValues(String str) throws RemoteException {
            return null;
        }

        @Override // jp.co.benesse.dcha.databox.ISbox
        public String getAuthUrl(int i) throws RemoteException {
            return null;
        }

        @Override // jp.co.benesse.dcha.databox.ISbox
        public String getStringValue(String str) throws RemoteException {
            return null;
        }

        @Override // jp.co.benesse.dcha.databox.ISbox
        public void setArrayValues(String str, String str2) throws RemoteException {
        }

        @Override // jp.co.benesse.dcha.databox.ISbox
        public void setStringValue(String str, String str2) throws RemoteException {
        }
    }

    String getAppIdentifier(int i) throws RemoteException;

    String getArrayValues(String str) throws RemoteException;

    String getAuthUrl(int i) throws RemoteException;

    String getStringValue(String str) throws RemoteException;

    void setArrayValues(String str, String str2) throws RemoteException;

    void setStringValue(String str, String str2) throws RemoteException;

    /* loaded from: classes.dex */
    public static abstract class Stub extends Binder implements ISbox {
        private static final String DESCRIPTOR = "jp.co.benesse.dcha.databox.ISbox";
        static final int TRANSACTION_getAppIdentifier = 5;
        static final int TRANSACTION_getArrayValues = 3;
        static final int TRANSACTION_getAuthUrl = 6;
        static final int TRANSACTION_getStringValue = 1;
        static final int TRANSACTION_setArrayValues = 4;
        static final int TRANSACTION_setStringValue = 2;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISbox asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface != null && (queryLocalInterface instanceof ISbox)) {
                return (ISbox) queryLocalInterface;
            }
            return new Proxy(iBinder);
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i == 1598968902) {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
            switch (i) {
                case TRANSACTION_getStringValue /* 1 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    String stringValue = getStringValue(parcel.readString());
                    parcel2.writeNoException();
                    parcel2.writeString(stringValue);
                    return true;
                case 2:
                    parcel.enforceInterface(DESCRIPTOR);
                    setStringValue(parcel.readString(), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case TRANSACTION_getArrayValues /* 3 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    String arrayValues = getArrayValues(parcel.readString());
                    parcel2.writeNoException();
                    parcel2.writeString(arrayValues);
                    return true;
                case TRANSACTION_setArrayValues /* 4 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    setArrayValues(parcel.readString(), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case TRANSACTION_getAppIdentifier /* 5 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    String appIdentifier = getAppIdentifier(parcel.readInt());
                    parcel2.writeNoException();
                    parcel2.writeString(appIdentifier);
                    return true;
                case TRANSACTION_getAuthUrl /* 6 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    String authUrl = getAuthUrl(parcel.readInt());
                    parcel2.writeNoException();
                    parcel2.writeString(authUrl);
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: classes.dex */
        public static class Proxy implements ISbox {
            public static ISbox sDefaultImpl;
            private IBinder mRemote;

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // jp.co.benesse.dcha.databox.ISbox
            public String getStringValue(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getStringValue, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getStringValue(str);
                    }
                    obtain2.readException();
                    return obtain2.readString();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.databox.ISbox
            public void setStringValue(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    if (!this.mRemote.transact(2, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        Stub.getDefaultImpl().setStringValue(str, str2);
                    } else {
                        obtain2.readException();
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.databox.ISbox
            public String getArrayValues(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getArrayValues, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getArrayValues(str);
                    }
                    obtain2.readException();
                    return obtain2.readString();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.databox.ISbox
            public void setArrayValues(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setArrayValues, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        Stub.getDefaultImpl().setArrayValues(str, str2);
                    } else {
                        obtain2.readException();
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.databox.ISbox
            public String getAppIdentifier(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getAppIdentifier, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppIdentifier(i);
                    }
                    obtain2.readException();
                    return obtain2.readString();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.databox.ISbox
            public String getAuthUrl(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getAuthUrl, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAuthUrl(i);
                    }
                    obtain2.readException();
                    return obtain2.readString();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISbox iSbox) {
            if (Proxy.sDefaultImpl != null || iSbox == null) {
                return false;
            }
            Proxy.sDefaultImpl = iSbox;
            return true;
        }

        public static ISbox getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
