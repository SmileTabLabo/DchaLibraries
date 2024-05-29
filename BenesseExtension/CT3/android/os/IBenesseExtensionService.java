package android.os;
/* loaded from: framework.zip:android/os/IBenesseExtensionService.class */
public interface IBenesseExtensionService extends IInterface {
    boolean checkPassword(String str) throws RemoteException;

    boolean checkUsbCam() throws RemoteException;

    int getDchaState() throws RemoteException;

    String getString(String str) throws RemoteException;

    void setDchaState(int i) throws RemoteException;
}
