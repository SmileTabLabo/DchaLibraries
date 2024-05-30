package com.android.systemui.shared.recents;

import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.systemui.shared.system.GraphicBufferCompat;
/* loaded from: classes.dex */
public interface ISystemUiProxy extends IInterface {
    Rect getNonMinimizedSplitScreenSecondaryBounds() throws RemoteException;

    void onOverviewShown(boolean z) throws RemoteException;

    void onSplitScreenInvoked() throws RemoteException;

    GraphicBufferCompat screenshot(Rect rect, int i, int i2, int i3, int i4, boolean z, int i5) throws RemoteException;

    void setBackButtonAlpha(float f, boolean z) throws RemoteException;

    void setInteractionState(int i) throws RemoteException;

    void startScreenPinning(int i) throws RemoteException;

    /* loaded from: classes.dex */
    public static abstract class Stub extends Binder implements ISystemUiProxy {
        public Stub() {
            attachInterface(this, "com.android.systemui.shared.recents.ISystemUiProxy");
        }

        public static ISystemUiProxy asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.android.systemui.shared.recents.ISystemUiProxy");
            if (queryLocalInterface != null && (queryLocalInterface instanceof ISystemUiProxy)) {
                return (ISystemUiProxy) queryLocalInterface;
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
                parcel2.writeString("com.android.systemui.shared.recents.ISystemUiProxy");
                return true;
            }
            switch (i) {
                case 1:
                    parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                    if (parcel.readInt() != 0) {
                        rect = (Rect) Rect.CREATOR.createFromParcel(parcel);
                    } else {
                        rect = null;
                    }
                    GraphicBufferCompat screenshot = screenshot(rect, parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt() != 0, parcel.readInt());
                    parcel2.writeNoException();
                    if (screenshot != null) {
                        parcel2.writeInt(1);
                        screenshot.writeToParcel(parcel2, 1);
                    } else {
                        parcel2.writeInt(0);
                    }
                    return true;
                case 2:
                    parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                    startScreenPinning(parcel.readInt());
                    parcel2.writeNoException();
                    return true;
                default:
                    switch (i) {
                        case 5:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            setInteractionState(parcel.readInt());
                            parcel2.writeNoException();
                            return true;
                        case 6:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            onSplitScreenInvoked();
                            parcel2.writeNoException();
                            return true;
                        case 7:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            onOverviewShown(parcel.readInt() != 0);
                            parcel2.writeNoException();
                            return true;
                        case 8:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            Rect nonMinimizedSplitScreenSecondaryBounds = getNonMinimizedSplitScreenSecondaryBounds();
                            parcel2.writeNoException();
                            if (nonMinimizedSplitScreenSecondaryBounds != null) {
                                parcel2.writeInt(1);
                                nonMinimizedSplitScreenSecondaryBounds.writeToParcel(parcel2, 1);
                            } else {
                                parcel2.writeInt(0);
                            }
                            return true;
                        case 9:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            setBackButtonAlpha(parcel.readFloat(), parcel.readInt() != 0);
                            parcel2.writeNoException();
                            return true;
                        default:
                            return super.onTransact(i, parcel, parcel2, i2);
                    }
            }
        }

        /* loaded from: classes.dex */
        private static class Proxy implements ISystemUiProxy {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public GraphicBufferCompat screenshot(Rect rect, int i, int i2, int i3, int i4, boolean z, int i5) throws RemoteException {
                GraphicBufferCompat graphicBufferCompat;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.shared.recents.ISystemUiProxy");
                    if (rect != null) {
                        obtain.writeInt(1);
                        rect.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(z ? 1 : 0);
                    obtain.writeInt(i5);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        graphicBufferCompat = GraphicBufferCompat.CREATOR.createFromParcel(obtain2);
                    } else {
                        graphicBufferCompat = null;
                    }
                    return graphicBufferCompat;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void startScreenPinning(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.shared.recents.ISystemUiProxy");
                    obtain.writeInt(i);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void setInteractionState(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.shared.recents.ISystemUiProxy");
                    obtain.writeInt(i);
                    this.mRemote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void onSplitScreenInvoked() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.shared.recents.ISystemUiProxy");
                    this.mRemote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void onOverviewShown(boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.shared.recents.ISystemUiProxy");
                    obtain.writeInt(z ? 1 : 0);
                    this.mRemote.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public Rect getNonMinimizedSplitScreenSecondaryBounds() throws RemoteException {
                Rect rect;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.shared.recents.ISystemUiProxy");
                    this.mRemote.transact(8, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        rect = (Rect) Rect.CREATOR.createFromParcel(obtain2);
                    } else {
                        rect = null;
                    }
                    return rect;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void setBackButtonAlpha(float f, boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.shared.recents.ISystemUiProxy");
                    obtain.writeFloat(f);
                    obtain.writeInt(z ? 1 : 0);
                    this.mRemote.transact(9, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
