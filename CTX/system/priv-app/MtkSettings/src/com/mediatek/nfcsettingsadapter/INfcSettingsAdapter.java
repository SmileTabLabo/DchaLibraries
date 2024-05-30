package com.mediatek.nfcsettingsadapter;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;
/* loaded from: classes.dex */
public interface INfcSettingsAdapter extends IInterface {
    void commitServiceEntryList(List<ServiceEntry> list) throws RemoteException;

    int getModeFlag(int i) throws RemoteException;

    List<ServiceEntry> getServiceEntryList(int i) throws RemoteException;

    boolean isRoutingTableOverflow() throws RemoteException;

    boolean isShowOverflowMenu() throws RemoteException;

    void setModeFlag(int i, int i2) throws RemoteException;

    boolean testServiceEntryList(List<ServiceEntry> list) throws RemoteException;

    /* loaded from: classes.dex */
    public static abstract class Stub extends Binder implements INfcSettingsAdapter {
        public static INfcSettingsAdapter asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
            if (queryLocalInterface != null && (queryLocalInterface instanceof INfcSettingsAdapter)) {
                return (INfcSettingsAdapter) queryLocalInterface;
            }
            return new Proxy(iBinder);
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i == 1598968902) {
                parcel2.writeString("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
                return true;
            }
            switch (i) {
                case 1:
                    parcel.enforceInterface("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
                    int modeFlag = getModeFlag(parcel.readInt());
                    parcel2.writeNoException();
                    parcel2.writeInt(modeFlag);
                    return true;
                case 2:
                    parcel.enforceInterface("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
                    setModeFlag(parcel.readInt(), parcel.readInt());
                    parcel2.writeNoException();
                    return true;
                case 3:
                    parcel.enforceInterface("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
                    boolean isRoutingTableOverflow = isRoutingTableOverflow();
                    parcel2.writeNoException();
                    parcel2.writeInt(isRoutingTableOverflow ? 1 : 0);
                    return true;
                case 4:
                    parcel.enforceInterface("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
                    boolean isShowOverflowMenu = isShowOverflowMenu();
                    parcel2.writeNoException();
                    parcel2.writeInt(isShowOverflowMenu ? 1 : 0);
                    return true;
                case 5:
                    parcel.enforceInterface("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
                    List<ServiceEntry> serviceEntryList = getServiceEntryList(parcel.readInt());
                    parcel2.writeNoException();
                    parcel2.writeTypedList(serviceEntryList);
                    return true;
                case 6:
                    parcel.enforceInterface("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
                    boolean testServiceEntryList = testServiceEntryList(parcel.createTypedArrayList(ServiceEntry.CREATOR));
                    parcel2.writeNoException();
                    parcel2.writeInt(testServiceEntryList ? 1 : 0);
                    return true;
                case 7:
                    parcel.enforceInterface("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
                    commitServiceEntryList(parcel.createTypedArrayList(ServiceEntry.CREATOR));
                    parcel2.writeNoException();
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }

        /* loaded from: classes.dex */
        private static class Proxy implements INfcSettingsAdapter {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.mediatek.nfcsettingsadapter.INfcSettingsAdapter
            public int getModeFlag(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
                    obtain.writeInt(i);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.mediatek.nfcsettingsadapter.INfcSettingsAdapter
            public void setModeFlag(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.mediatek.nfcsettingsadapter.INfcSettingsAdapter
            public boolean isRoutingTableOverflow() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.mediatek.nfcsettingsadapter.INfcSettingsAdapter
            public boolean isShowOverflowMenu() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
                    this.mRemote.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.mediatek.nfcsettingsadapter.INfcSettingsAdapter
            public List<ServiceEntry> getServiceEntryList(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
                    obtain.writeInt(i);
                    this.mRemote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createTypedArrayList(ServiceEntry.CREATOR);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.mediatek.nfcsettingsadapter.INfcSettingsAdapter
            public boolean testServiceEntryList(List<ServiceEntry> list) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
                    obtain.writeTypedList(list);
                    this.mRemote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.mediatek.nfcsettingsadapter.INfcSettingsAdapter
            public void commitServiceEntryList(List<ServiceEntry> list) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.nfcsettingsadapter.INfcSettingsAdapter");
                    obtain.writeTypedList(list);
                    this.mRemote.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
