package jp.co.benesse.dcha.dchaservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
/* loaded from: classes.dex */
public interface IDchaService extends IInterface {

    /* loaded from: classes.dex */
    public static class Default implements IDchaService {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void cancelSetup() throws RemoteException {
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean checkPadRooted() throws RemoteException {
            return false;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void clearDefaultPreferredApp(String str) throws RemoteException {
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean copyFile(String str, String str2) throws RemoteException {
            return false;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean copyUpdateImage(String str, String str2) throws RemoteException {
            return false;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean deleteFile(String str) throws RemoteException {
            return false;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void disableADB() throws RemoteException {
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public String getCanonicalExternalPath(String str) throws RemoteException {
            return null;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public String getForegroundPackageName() throws RemoteException {
            return null;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public int getSetupStatus() throws RemoteException {
            return 0;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public int getUserCount() throws RemoteException {
            return 0;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void hideNavigationBar(boolean z) throws RemoteException {
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean installApp(String str, int i) throws RemoteException {
            return false;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean isDeviceEncryptionEnabled() throws RemoteException {
            return false;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void rebootPad(int i, String str) throws RemoteException {
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void removeTask(String str) throws RemoteException {
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void sdUnmount() throws RemoteException {
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void setDefaultParam() throws RemoteException {
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void setDefaultPreferredHomeApp(String str) throws RemoteException {
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void setPermissionEnforced(boolean z) throws RemoteException {
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void setSetupStatus(int i) throws RemoteException {
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void setSystemTime(String str, String str2) throws RemoteException {
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean uninstallApp(String str, int i) throws RemoteException {
            return false;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean verifyUpdateImage(String str) throws RemoteException {
            return false;
        }
    }

    void cancelSetup() throws RemoteException;

    boolean checkPadRooted() throws RemoteException;

    void clearDefaultPreferredApp(String str) throws RemoteException;

    boolean copyFile(String str, String str2) throws RemoteException;

    boolean copyUpdateImage(String str, String str2) throws RemoteException;

    boolean deleteFile(String str) throws RemoteException;

    void disableADB() throws RemoteException;

    String getCanonicalExternalPath(String str) throws RemoteException;

    String getForegroundPackageName() throws RemoteException;

    int getSetupStatus() throws RemoteException;

    int getUserCount() throws RemoteException;

    void hideNavigationBar(boolean z) throws RemoteException;

    boolean installApp(String str, int i) throws RemoteException;

    boolean isDeviceEncryptionEnabled() throws RemoteException;

    void rebootPad(int i, String str) throws RemoteException;

    void removeTask(String str) throws RemoteException;

    void sdUnmount() throws RemoteException;

    void setDefaultParam() throws RemoteException;

    void setDefaultPreferredHomeApp(String str) throws RemoteException;

    void setPermissionEnforced(boolean z) throws RemoteException;

    void setSetupStatus(int i) throws RemoteException;

    void setSystemTime(String str, String str2) throws RemoteException;

    boolean uninstallApp(String str, int i) throws RemoteException;

    boolean verifyUpdateImage(String str) throws RemoteException;

    /* loaded from: classes.dex */
    public static abstract class Stub extends Binder implements IDchaService {
        private static final String DESCRIPTOR = "jp.co.benesse.dcha.dchaservice.IDchaService";
        static final int TRANSACTION_cancelSetup = 10;
        static final int TRANSACTION_checkPadRooted = 7;
        static final int TRANSACTION_clearDefaultPreferredApp = 5;
        static final int TRANSACTION_copyFile = 18;
        static final int TRANSACTION_copyUpdateImage = 2;
        static final int TRANSACTION_deleteFile = 19;
        static final int TRANSACTION_disableADB = 6;
        static final int TRANSACTION_getCanonicalExternalPath = 24;
        static final int TRANSACTION_getForegroundPackageName = 17;
        static final int TRANSACTION_getSetupStatus = 12;
        static final int TRANSACTION_getUserCount = 20;
        static final int TRANSACTION_hideNavigationBar = 22;
        static final int TRANSACTION_installApp = 8;
        static final int TRANSACTION_isDeviceEncryptionEnabled = 21;
        static final int TRANSACTION_rebootPad = 3;
        static final int TRANSACTION_removeTask = 14;
        static final int TRANSACTION_sdUnmount = 15;
        static final int TRANSACTION_setDefaultParam = 16;
        static final int TRANSACTION_setDefaultPreferredHomeApp = 4;
        static final int TRANSACTION_setPermissionEnforced = 23;
        static final int TRANSACTION_setSetupStatus = 11;
        static final int TRANSACTION_setSystemTime = 13;
        static final int TRANSACTION_uninstallApp = 9;
        static final int TRANSACTION_verifyUpdateImage = 1;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDchaService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface != null && (queryLocalInterface instanceof IDchaService)) {
                return (IDchaService) queryLocalInterface;
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
                case 1:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean verifyUpdateImage = verifyUpdateImage(parcel.readString());
                    parcel2.writeNoException();
                    parcel2.writeInt(verifyUpdateImage ? 1 : 0);
                    return true;
                case 2:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean copyUpdateImage = copyUpdateImage(parcel.readString(), parcel.readString());
                    parcel2.writeNoException();
                    parcel2.writeInt(copyUpdateImage ? 1 : 0);
                    return true;
                case 3:
                    parcel.enforceInterface(DESCRIPTOR);
                    rebootPad(parcel.readInt(), parcel.readString());
                    return true;
                case TRANSACTION_setDefaultPreferredHomeApp /* 4 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    setDefaultPreferredHomeApp(parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case TRANSACTION_clearDefaultPreferredApp /* 5 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    clearDefaultPreferredApp(parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case TRANSACTION_disableADB /* 6 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    disableADB();
                    parcel2.writeNoException();
                    return true;
                case 7:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean checkPadRooted = checkPadRooted();
                    parcel2.writeNoException();
                    parcel2.writeInt(checkPadRooted ? 1 : 0);
                    return true;
                case TRANSACTION_installApp /* 8 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean installApp = installApp(parcel.readString(), parcel.readInt());
                    parcel2.writeNoException();
                    parcel2.writeInt(installApp ? 1 : 0);
                    return true;
                case TRANSACTION_uninstallApp /* 9 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean uninstallApp = uninstallApp(parcel.readString(), parcel.readInt());
                    parcel2.writeNoException();
                    parcel2.writeInt(uninstallApp ? 1 : 0);
                    return true;
                case 10:
                    parcel.enforceInterface(DESCRIPTOR);
                    cancelSetup();
                    parcel2.writeNoException();
                    return true;
                case TRANSACTION_setSetupStatus /* 11 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    setSetupStatus(parcel.readInt());
                    parcel2.writeNoException();
                    return true;
                case TRANSACTION_getSetupStatus /* 12 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    int setupStatus = getSetupStatus();
                    parcel2.writeNoException();
                    parcel2.writeInt(setupStatus);
                    return true;
                case TRANSACTION_setSystemTime /* 13 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    setSystemTime(parcel.readString(), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case TRANSACTION_removeTask /* 14 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    removeTask(parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case TRANSACTION_sdUnmount /* 15 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    sdUnmount();
                    parcel2.writeNoException();
                    return true;
                case TRANSACTION_setDefaultParam /* 16 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    setDefaultParam();
                    parcel2.writeNoException();
                    return true;
                case TRANSACTION_getForegroundPackageName /* 17 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    String foregroundPackageName = getForegroundPackageName();
                    parcel2.writeNoException();
                    parcel2.writeString(foregroundPackageName);
                    return true;
                case TRANSACTION_copyFile /* 18 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean copyFile = copyFile(parcel.readString(), parcel.readString());
                    parcel2.writeNoException();
                    parcel2.writeInt(copyFile ? 1 : 0);
                    return true;
                case TRANSACTION_deleteFile /* 19 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean deleteFile = deleteFile(parcel.readString());
                    parcel2.writeNoException();
                    parcel2.writeInt(deleteFile ? 1 : 0);
                    return true;
                case TRANSACTION_getUserCount /* 20 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    int userCount = getUserCount();
                    parcel2.writeNoException();
                    parcel2.writeInt(userCount);
                    return true;
                case TRANSACTION_isDeviceEncryptionEnabled /* 21 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean isDeviceEncryptionEnabled = isDeviceEncryptionEnabled();
                    parcel2.writeNoException();
                    parcel2.writeInt(isDeviceEncryptionEnabled ? 1 : 0);
                    return true;
                case TRANSACTION_hideNavigationBar /* 22 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    hideNavigationBar(parcel.readInt() != 0);
                    parcel2.writeNoException();
                    return true;
                case TRANSACTION_setPermissionEnforced /* 23 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    setPermissionEnforced(parcel.readInt() != 0);
                    parcel2.writeNoException();
                    return true;
                case TRANSACTION_getCanonicalExternalPath /* 24 */:
                    parcel.enforceInterface(DESCRIPTOR);
                    String canonicalExternalPath = getCanonicalExternalPath(parcel.readString());
                    parcel2.writeNoException();
                    parcel2.writeString(canonicalExternalPath);
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: classes.dex */
        public static class Proxy implements IDchaService {
            public static IDchaService sDefaultImpl;
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

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public boolean verifyUpdateImage(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(1, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().verifyUpdateImage(str);
                    }
                    obtain2.readException();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public boolean copyUpdateImage(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    if (!this.mRemote.transact(2, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().copyUpdateImage(str, str2);
                    }
                    obtain2.readException();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void rebootPad(int i, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    if (this.mRemote.transact(3, obtain, null, 1) || Stub.getDefaultImpl() == null) {
                        return;
                    }
                    Stub.getDefaultImpl().rebootPad(i, str);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void setDefaultPreferredHomeApp(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setDefaultPreferredHomeApp, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        Stub.getDefaultImpl().setDefaultPreferredHomeApp(str);
                    } else {
                        obtain2.readException();
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void clearDefaultPreferredApp(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(Stub.TRANSACTION_clearDefaultPreferredApp, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        Stub.getDefaultImpl().clearDefaultPreferredApp(str);
                    } else {
                        obtain2.readException();
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void disableADB() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_disableADB, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        Stub.getDefaultImpl().disableADB();
                    } else {
                        obtain2.readException();
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public boolean checkPadRooted() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkPadRooted();
                    }
                    obtain2.readException();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public boolean installApp(String str, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    if (!this.mRemote.transact(Stub.TRANSACTION_installApp, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().installApp(str, i);
                    }
                    obtain2.readException();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public boolean uninstallApp(String str, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    if (!this.mRemote.transact(Stub.TRANSACTION_uninstallApp, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().uninstallApp(str, i);
                    }
                    obtain2.readException();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void cancelSetup() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(10, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        Stub.getDefaultImpl().cancelSetup();
                    } else {
                        obtain2.readException();
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void setSetupStatus(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setSetupStatus, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        Stub.getDefaultImpl().setSetupStatus(i);
                    } else {
                        obtain2.readException();
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public int getSetupStatus() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getSetupStatus, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSetupStatus();
                    }
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void setSystemTime(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setSystemTime, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        Stub.getDefaultImpl().setSystemTime(str, str2);
                    } else {
                        obtain2.readException();
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void removeTask(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(Stub.TRANSACTION_removeTask, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        Stub.getDefaultImpl().removeTask(str);
                    } else {
                        obtain2.readException();
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void sdUnmount() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_sdUnmount, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        Stub.getDefaultImpl().sdUnmount();
                    } else {
                        obtain2.readException();
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void setDefaultParam() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setDefaultParam, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        Stub.getDefaultImpl().setDefaultParam();
                    } else {
                        obtain2.readException();
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public String getForegroundPackageName() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getForegroundPackageName, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getForegroundPackageName();
                    }
                    obtain2.readException();
                    return obtain2.readString();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public boolean copyFile(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    if (!this.mRemote.transact(Stub.TRANSACTION_copyFile, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().copyFile(str, str2);
                    }
                    obtain2.readException();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public boolean deleteFile(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(Stub.TRANSACTION_deleteFile, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deleteFile(str);
                    }
                    obtain2.readException();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public int getUserCount() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getUserCount, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserCount();
                    }
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public boolean isDeviceEncryptionEnabled() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_isDeviceEncryptionEnabled, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDeviceEncryptionEnabled();
                    }
                    obtain2.readException();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void hideNavigationBar(boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(z ? 1 : 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_hideNavigationBar, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        Stub.getDefaultImpl().hideNavigationBar(z);
                    } else {
                        obtain2.readException();
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void setPermissionEnforced(boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(z ? 1 : 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setPermissionEnforced, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        Stub.getDefaultImpl().setPermissionEnforced(z);
                    } else {
                        obtain2.readException();
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public String getCanonicalExternalPath(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCanonicalExternalPath, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCanonicalExternalPath(str);
                    }
                    obtain2.readException();
                    return obtain2.readString();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDchaService iDchaService) {
            if (Proxy.sDefaultImpl != null || iDchaService == null) {
                return false;
            }
            Proxy.sDefaultImpl = iDchaService;
            return true;
        }

        public static IDchaService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
