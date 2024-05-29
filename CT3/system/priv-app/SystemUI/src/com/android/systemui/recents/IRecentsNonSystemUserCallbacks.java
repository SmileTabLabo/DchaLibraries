package com.android.systemui.recents;

import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
/* loaded from: a.zip:com/android/systemui/recents/IRecentsNonSystemUserCallbacks.class */
public interface IRecentsNonSystemUserCallbacks extends IInterface {

    /* loaded from: a.zip:com/android/systemui/recents/IRecentsNonSystemUserCallbacks$Stub.class */
    public static abstract class Stub extends Binder implements IRecentsNonSystemUserCallbacks {

        /* loaded from: a.zip:com/android/systemui/recents/IRecentsNonSystemUserCallbacks$Stub$Proxy.class */
        private static class Proxy implements IRecentsNonSystemUserCallbacks {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
            public void cancelPreloadingRecents() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    this.mRemote.transact(2, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
            public void dockTopTask(int i, int i2, int i3, Rect rect) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    if (rect != null) {
                        obtain.writeInt(1);
                        rect.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(7, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
            public void hideRecents(boolean z, boolean z2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    obtain.writeInt(z ? 1 : 0);
                    obtain.writeInt(z2 ? 1 : 0);
                    this.mRemote.transact(4, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
            public void onConfigurationChanged() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    this.mRemote.transact(6, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
            public void onDraggingInRecents(float f) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    obtain.writeFloat(f);
                    this.mRemote.transact(8, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
            public void onDraggingInRecentsEnded(float f) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    obtain.writeFloat(f);
                    this.mRemote.transact(9, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
            public void preloadRecents() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    this.mRemote.transact(1, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
            public void showRecents(boolean z, boolean z2, boolean z3, boolean z4, boolean z5, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    obtain.writeInt(z ? 1 : 0);
                    obtain.writeInt(z2 ? 1 : 0);
                    obtain.writeInt(z3 ? 1 : 0);
                    obtain.writeInt(z4 ? 1 : 0);
                    obtain.writeInt(z5 ? 1 : 0);
                    obtain.writeInt(i);
                    this.mRemote.transact(3, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.recents.IRecentsNonSystemUserCallbacks
            public void toggleRecents(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    obtain.writeInt(i);
                    this.mRemote.transact(5, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
        }

        public static IRecentsNonSystemUserCallbacks asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
            return (queryLocalInterface == null || !(queryLocalInterface instanceof IRecentsNonSystemUserCallbacks)) ? new Proxy(iBinder) : (IRecentsNonSystemUserCallbacks) queryLocalInterface;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            switch (i) {
                case 1:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    preloadRecents();
                    return true;
                case 2:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    cancelPreloadingRecents();
                    return true;
                case 3:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    showRecents(parcel.readInt() != 0, parcel.readInt() != 0, parcel.readInt() != 0, parcel.readInt() != 0, parcel.readInt() != 0, parcel.readInt());
                    return true;
                case 4:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    hideRecents(parcel.readInt() != 0, parcel.readInt() != 0);
                    return true;
                case 5:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    toggleRecents(parcel.readInt());
                    return true;
                case 6:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    onConfigurationChanged();
                    return true;
                case 7:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    dockTopTask(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt() != 0 ? (Rect) Rect.CREATOR.createFromParcel(parcel) : null);
                    return true;
                case 8:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    onDraggingInRecents(parcel.readFloat());
                    return true;
                case 9:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    onDraggingInRecentsEnded(parcel.readFloat());
                    return true;
                case 1598968902:
                    parcel2.writeString("com.android.systemui.recents.IRecentsNonSystemUserCallbacks");
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    void cancelPreloadingRecents() throws RemoteException;

    void dockTopTask(int i, int i2, int i3, Rect rect) throws RemoteException;

    void hideRecents(boolean z, boolean z2) throws RemoteException;

    void onConfigurationChanged() throws RemoteException;

    void onDraggingInRecents(float f) throws RemoteException;

    void onDraggingInRecentsEnded(float f) throws RemoteException;

    void preloadRecents() throws RemoteException;

    void showRecents(boolean z, boolean z2, boolean z3, boolean z4, boolean z5, int i) throws RemoteException;

    void toggleRecents(int i) throws RemoteException;
}
