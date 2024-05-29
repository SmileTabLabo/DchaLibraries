package com.android.systemui.recents;

import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
/* loaded from: classes.dex */
public interface IRecentsSystemUserCallbacks extends IInterface {
    void registerNonSystemUserCallbacks(IBinder iBinder, int i) throws RemoteException;

    void sendDockedFirstAnimationFrameEvent() throws RemoteException;

    void sendDockingTopTaskEvent(int i, Rect rect) throws RemoteException;

    void sendLaunchRecentsEvent() throws RemoteException;

    void sendRecentsDrawnEvent() throws RemoteException;

    void setWaitingForTransitionStartEvent(boolean z) throws RemoteException;

    void startScreenPinning(int i) throws RemoteException;

    void updateRecentsVisibility(boolean z) throws RemoteException;

    /* loaded from: classes.dex */
    public static abstract class Stub extends Binder implements IRecentsSystemUserCallbacks {
        public Stub() {
            attachInterface(this, "com.android.systemui.recents.IRecentsSystemUserCallbacks");
        }

        public static IRecentsSystemUserCallbacks asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
            if (queryLocalInterface != null && (queryLocalInterface instanceof IRecentsSystemUserCallbacks)) {
                return (IRecentsSystemUserCallbacks) queryLocalInterface;
            }
            return new Proxy(iBinder);
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            Rect rect;
            if (i == 1598968902) {
                parcel2.writeString("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                return true;
            }
            switch (i) {
                case 1:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    registerNonSystemUserCallbacks(parcel.readStrongBinder(), parcel.readInt());
                    return true;
                case 2:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    updateRecentsVisibility(parcel.readInt() != 0);
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
                    int readInt = parcel.readInt();
                    if (parcel.readInt() != 0) {
                        rect = (Rect) Rect.CREATOR.createFromParcel(parcel);
                    } else {
                        rect = null;
                    }
                    sendDockingTopTaskEvent(readInt, rect);
                    return true;
                case 6:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    sendLaunchRecentsEvent();
                    return true;
                case 7:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    sendDockedFirstAnimationFrameEvent();
                    return true;
                case 8:
                    parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    setWaitingForTransitionStartEvent(parcel.readInt() != 0);
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }

        /* loaded from: classes.dex */
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
            public void updateRecentsVisibility(boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    obtain.writeInt(z ? 1 : 0);
                    this.mRemote.transact(2, obtain, null, 1);
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
            public void sendDockedFirstAnimationFrameEvent() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    this.mRemote.transact(7, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.recents.IRecentsSystemUserCallbacks
            public void setWaitingForTransitionStartEvent(boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    obtain.writeInt(z ? 1 : 0);
                    this.mRemote.transact(8, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }
    }
}
