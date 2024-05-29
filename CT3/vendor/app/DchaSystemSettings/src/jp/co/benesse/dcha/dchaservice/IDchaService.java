package jp.co.benesse.dcha.dchaservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
/* loaded from: s.zip:jp/co/benesse/dcha/dchaservice/IDchaService.class */
public interface IDchaService extends IInterface {

    /* loaded from: s.zip:jp/co/benesse/dcha/dchaservice/IDchaService$Stub.class */
    public static abstract class Stub extends Binder implements IDchaService {

        /* loaded from: s.zip:jp/co/benesse/dcha/dchaservice/IDchaService$Stub$Proxy.class */
        private static class Proxy implements IDchaService {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void cancelSetup() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    this.mRemote.transact(10, obtain, obtain2, 0);
                    obtain2.readException();
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
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    this.mRemote.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                    boolean z = obtain2.readInt() != 0;
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void clearDefaultPreferredApp(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    obtain.writeString(str);
                    this.mRemote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
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
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    this.mRemote.transact(18, obtain, obtain2, 0);
                    obtain2.readException();
                    boolean z = obtain2.readInt() != 0;
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public boolean copyUpdateImage(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    boolean z = obtain2.readInt() != 0;
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public boolean deleteFile(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    obtain.writeString(str);
                    this.mRemote.transact(19, obtain, obtain2, 0);
                    obtain2.readException();
                    boolean z = obtain2.readInt() != 0;
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void disableADB() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    this.mRemote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
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
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    this.mRemote.transact(17, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readString();
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
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    this.mRemote.transact(12, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
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
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    this.mRemote.transact(20, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void hideNavigationBar(boolean z) throws RemoteException {
                int i = 0;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    if (z) {
                        i = 1;
                    }
                    obtain.writeInt(i);
                    this.mRemote.transact(22, obtain, obtain2, 0);
                    obtain2.readException();
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
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    this.mRemote.transact(8, obtain, obtain2, 0);
                    obtain2.readException();
                    boolean z = obtain2.readInt() != 0;
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public boolean isDeviceEncryptionEnabled() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    this.mRemote.transact(21, obtain, obtain2, 0);
                    obtain2.readException();
                    boolean z = obtain2.readInt() != 0;
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void rebootPad(int i, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    this.mRemote.transact(3, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void removeTask(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    obtain.writeString(str);
                    this.mRemote.transact(14, obtain, obtain2, 0);
                    obtain2.readException();
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
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    this.mRemote.transact(15, obtain, obtain2, 0);
                    obtain2.readException();
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
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    this.mRemote.transact(16, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void setDefaultPreferredHomeApp(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    obtain.writeString(str);
                    this.mRemote.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public void setPermissionEnforced(boolean z) throws RemoteException {
                int i = 0;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    if (z) {
                        i = 1;
                    }
                    obtain.writeInt(i);
                    this.mRemote.transact(23, obtain, obtain2, 0);
                    obtain2.readException();
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
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    obtain.writeInt(i);
                    this.mRemote.transact(11, obtain, obtain2, 0);
                    obtain2.readException();
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
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    this.mRemote.transact(13, obtain, obtain2, 0);
                    obtain2.readException();
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
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    this.mRemote.transact(9, obtain, obtain2, 0);
                    obtain2.readException();
                    boolean z = obtain2.readInt() != 0;
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }

            @Override // jp.co.benesse.dcha.dchaservice.IDchaService
            public boolean verifyUpdateImage(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("jp.co.benesse.dcha.dchaservice.IDchaService");
                    obtain.writeString(str);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    boolean z = obtain2.readInt() != 0;
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }
        }

        public Stub() {
            attachInterface(this, "jp.co.benesse.dcha.dchaservice.IDchaService");
        }

        public static IDchaService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
            return (queryLocalInterface == null || !(queryLocalInterface instanceof IDchaService)) ? new Proxy(iBinder) : (IDchaService) queryLocalInterface;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            switch (i) {
                case 1:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    boolean verifyUpdateImage = verifyUpdateImage(parcel.readString());
                    parcel2.writeNoException();
                    int i3 = 0;
                    if (verifyUpdateImage) {
                        i3 = 1;
                    }
                    parcel2.writeInt(i3);
                    return true;
                case 2:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    boolean copyUpdateImage = copyUpdateImage(parcel.readString(), parcel.readString());
                    parcel2.writeNoException();
                    int i4 = 0;
                    if (copyUpdateImage) {
                        i4 = 1;
                    }
                    parcel2.writeInt(i4);
                    return true;
                case 3:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    rebootPad(parcel.readInt(), parcel.readString());
                    return true;
                case 4:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    setDefaultPreferredHomeApp(parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 5:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    clearDefaultPreferredApp(parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 6:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    disableADB();
                    parcel2.writeNoException();
                    return true;
                case 7:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    boolean checkPadRooted = checkPadRooted();
                    parcel2.writeNoException();
                    int i5 = 0;
                    if (checkPadRooted) {
                        i5 = 1;
                    }
                    parcel2.writeInt(i5);
                    return true;
                case 8:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    boolean installApp = installApp(parcel.readString(), parcel.readInt());
                    parcel2.writeNoException();
                    int i6 = 0;
                    if (installApp) {
                        i6 = 1;
                    }
                    parcel2.writeInt(i6);
                    return true;
                case 9:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    boolean uninstallApp = uninstallApp(parcel.readString(), parcel.readInt());
                    parcel2.writeNoException();
                    int i7 = 0;
                    if (uninstallApp) {
                        i7 = 1;
                    }
                    parcel2.writeInt(i7);
                    return true;
                case 10:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    cancelSetup();
                    parcel2.writeNoException();
                    return true;
                case 11:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    setSetupStatus(parcel.readInt());
                    parcel2.writeNoException();
                    return true;
                case 12:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    int setupStatus = getSetupStatus();
                    parcel2.writeNoException();
                    parcel2.writeInt(setupStatus);
                    return true;
                case 13:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    setSystemTime(parcel.readString(), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 14:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    removeTask(parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 15:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    sdUnmount();
                    parcel2.writeNoException();
                    return true;
                case 16:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    setDefaultParam();
                    parcel2.writeNoException();
                    return true;
                case 17:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    String foregroundPackageName = getForegroundPackageName();
                    parcel2.writeNoException();
                    parcel2.writeString(foregroundPackageName);
                    return true;
                case 18:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    boolean copyFile = copyFile(parcel.readString(), parcel.readString());
                    parcel2.writeNoException();
                    int i8 = 0;
                    if (copyFile) {
                        i8 = 1;
                    }
                    parcel2.writeInt(i8);
                    return true;
                case 19:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    boolean deleteFile = deleteFile(parcel.readString());
                    parcel2.writeNoException();
                    int i9 = 0;
                    if (deleteFile) {
                        i9 = 1;
                    }
                    parcel2.writeInt(i9);
                    return true;
                case 20:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    int userCount = getUserCount();
                    parcel2.writeNoException();
                    parcel2.writeInt(userCount);
                    return true;
                case 21:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    boolean isDeviceEncryptionEnabled = isDeviceEncryptionEnabled();
                    parcel2.writeNoException();
                    int i10 = 0;
                    if (isDeviceEncryptionEnabled) {
                        i10 = 1;
                    }
                    parcel2.writeInt(i10);
                    return true;
                case 22:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    hideNavigationBar(parcel.readInt() != 0);
                    parcel2.writeNoException();
                    return true;
                case 23:
                    parcel.enforceInterface("jp.co.benesse.dcha.dchaservice.IDchaService");
                    setPermissionEnforced(parcel.readInt() != 0);
                    parcel2.writeNoException();
                    return true;
                case 1598968902:
                    parcel2.writeString("jp.co.benesse.dcha.dchaservice.IDchaService");
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    void cancelSetup() throws RemoteException;

    boolean checkPadRooted() throws RemoteException;

    void clearDefaultPreferredApp(String str) throws RemoteException;

    boolean copyFile(String str, String str2) throws RemoteException;

    boolean copyUpdateImage(String str, String str2) throws RemoteException;

    boolean deleteFile(String str) throws RemoteException;

    void disableADB() throws RemoteException;

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
}
