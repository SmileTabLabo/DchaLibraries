package com.android.systemui.recents;

import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
/* loaded from: a.zip:com/android/systemui/recents/IRecentsSystemUserCallbacks.class */
public interface IRecentsSystemUserCallbacks extends IInterface {

    /* loaded from: a.zip:com/android/systemui/recents/IRecentsSystemUserCallbacks$Stub.class */
    public static abstract class Stub extends Binder implements IRecentsSystemUserCallbacks {

        /* loaded from: a.zip:com/android/systemui/recents/IRecentsSystemUserCallbacks$Stub$Proxy.class */
        private static class Proxy implements IRecentsSystemUserCallbacks {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.android.systemui.recents.IRecentsSystemUserCallbacks
            public void registerNonSystemUserCallbacks(IBinder iBinder, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    obtain.writeStrongBinder(iBinder);
                    obtain.writeInt(i);
                    this.mRemote.transact(1, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.recents.IRecentsSystemUserCallbacks
            public void sendDockingTopTaskEvent(int i, Rect rect) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    obtain.writeInt(i);
                    if (rect != null) {
                        obtain.writeInt(1);
                        rect.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(5, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.recents.IRecentsSystemUserCallbacks
            public void sendLaunchRecentsEvent() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    this.mRemote.transact(6, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.recents.IRecentsSystemUserCallbacks
            public void sendRecentsDrawnEvent() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    this.mRemote.transact(4, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.recents.IRecentsSystemUserCallbacks
            public void startScreenPinning(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    obtain.writeInt(i);
                    this.mRemote.transact(3, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.recents.IRecentsSystemUserCallbacks
            public void updateRecentsVisibility(boolean z) throws RemoteException {
                int i = 1;
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    if (!z) {
                        i = 0;
                    }
                    obtain.writeInt(i);
                    this.mRemote.transact(2, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.android.systemui.recents.IRecentsSystemUserCallbacks");
        }

        public static IRecentsSystemUserCallbacks asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
            return (queryLocalInterface == null || !(queryLocalInterface instanceof IRecentsSystemUserCallbacks)) ? new Proxy(iBinder) : (IRecentsSystemUserCallbacks) queryLocalInterface;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            boolean z = false;
            switch (i) {
                case 1:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    registerNonSystemUserCallbacks(parcel.readStrongBinder(), parcel.readInt());
                    return true;
                case 2:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    if (parcel.readInt() != 0) {
                        z = true;
                    }
                    updateRecentsVisibility(z);
                    return true;
                case 3:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    startScreenPinning(parcel.readInt());
                    return true;
                case 4:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    sendRecentsDrawnEvent();
                    return true;
                case 5:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    sendDockingTopTaskEvent(parcel.readInt(), parcel.readInt() != 0 ? (Rect) Rect.CREATOR.createFromParcel(parcel) : null);
                    return true;
                case 6:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    sendLaunchRecentsEvent();
                    return true;
                case 1598968902:
                    parcel2.writeString("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    void registerNonSystemUserCallbacks(IBinder iBinder, int i) throws RemoteException;

    void sendDockingTopTaskEvent(int i, Rect rect) throws RemoteException;

    void sendLaunchRecentsEvent() throws RemoteException;

    void sendRecentsDrawnEvent() throws RemoteException;

    void startScreenPinning(int i) throws RemoteException;

    void updateRecentsVisibility(boolean z) throws RemoteException;
}
