package android.support.v4.media;

import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.v4.media.IMediaController2;
import java.util.List;
/* loaded from: classes.dex */
public interface IMediaSession2 extends IInterface {
    void addPlaylistItem(IMediaController2 iMediaController2, int i, Bundle bundle) throws RemoteException;

    void adjustVolume(IMediaController2 iMediaController2, int i, int i2) throws RemoteException;

    void connect(IMediaController2 iMediaController2, String str) throws RemoteException;

    void fastForward(IMediaController2 iMediaController2) throws RemoteException;

    void getChildren(IMediaController2 iMediaController2, String str, int i, int i2, Bundle bundle) throws RemoteException;

    void getItem(IMediaController2 iMediaController2, String str) throws RemoteException;

    void getLibraryRoot(IMediaController2 iMediaController2, Bundle bundle) throws RemoteException;

    void getSearchResult(IMediaController2 iMediaController2, String str, int i, int i2, Bundle bundle) throws RemoteException;

    void pause(IMediaController2 iMediaController2) throws RemoteException;

    void play(IMediaController2 iMediaController2) throws RemoteException;

    void playFromMediaId(IMediaController2 iMediaController2, String str, Bundle bundle) throws RemoteException;

    void playFromSearch(IMediaController2 iMediaController2, String str, Bundle bundle) throws RemoteException;

    void playFromUri(IMediaController2 iMediaController2, Uri uri, Bundle bundle) throws RemoteException;

    void prepare(IMediaController2 iMediaController2) throws RemoteException;

    void prepareFromMediaId(IMediaController2 iMediaController2, String str, Bundle bundle) throws RemoteException;

    void prepareFromSearch(IMediaController2 iMediaController2, String str, Bundle bundle) throws RemoteException;

    void prepareFromUri(IMediaController2 iMediaController2, Uri uri, Bundle bundle) throws RemoteException;

    void release(IMediaController2 iMediaController2) throws RemoteException;

    void removePlaylistItem(IMediaController2 iMediaController2, Bundle bundle) throws RemoteException;

    void replacePlaylistItem(IMediaController2 iMediaController2, int i, Bundle bundle) throws RemoteException;

    void reset(IMediaController2 iMediaController2) throws RemoteException;

    void rewind(IMediaController2 iMediaController2) throws RemoteException;

    void search(IMediaController2 iMediaController2, String str, Bundle bundle) throws RemoteException;

    void seekTo(IMediaController2 iMediaController2, long j) throws RemoteException;

    void selectRoute(IMediaController2 iMediaController2, Bundle bundle) throws RemoteException;

    void sendCustomCommand(IMediaController2 iMediaController2, Bundle bundle, Bundle bundle2, ResultReceiver resultReceiver) throws RemoteException;

    void setPlaybackSpeed(IMediaController2 iMediaController2, float f) throws RemoteException;

    void setPlaylist(IMediaController2 iMediaController2, List<Bundle> list, Bundle bundle) throws RemoteException;

    void setRating(IMediaController2 iMediaController2, String str, Bundle bundle) throws RemoteException;

    void setRepeatMode(IMediaController2 iMediaController2, int i) throws RemoteException;

    void setShuffleMode(IMediaController2 iMediaController2, int i) throws RemoteException;

    void setVolumeTo(IMediaController2 iMediaController2, int i, int i2) throws RemoteException;

    void skipToNextItem(IMediaController2 iMediaController2) throws RemoteException;

    void skipToPlaylistItem(IMediaController2 iMediaController2, Bundle bundle) throws RemoteException;

    void skipToPreviousItem(IMediaController2 iMediaController2) throws RemoteException;

    void subscribe(IMediaController2 iMediaController2, String str, Bundle bundle) throws RemoteException;

    void subscribeRoutesInfo(IMediaController2 iMediaController2) throws RemoteException;

    void unsubscribe(IMediaController2 iMediaController2, String str) throws RemoteException;

    void unsubscribeRoutesInfo(IMediaController2 iMediaController2) throws RemoteException;

    void updatePlaylistMetadata(IMediaController2 iMediaController2, Bundle bundle) throws RemoteException;

    /* loaded from: classes.dex */
    public static abstract class Stub extends Binder implements IMediaSession2 {
        public static IMediaSession2 asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("android.support.v4.media.IMediaSession2");
            if (iin != null && (iin instanceof IMediaSession2)) {
                return (IMediaSession2) iin;
            }
            return new Proxy(obj);
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            Bundle _arg2;
            Uri _arg12;
            Uri _arg13;
            if (code == 1598968902) {
                reply.writeString("android.support.v4.media.IMediaSession2");
                return true;
            }
            switch (code) {
                case 1:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg0 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    String _arg14 = data.readString();
                    connect(_arg0, _arg14);
                    return true;
                case 2:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg02 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    release(_arg02);
                    return true;
                case 3:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg03 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    int _arg15 = data.readInt();
                    int _arg22 = data.readInt();
                    setVolumeTo(_arg03, _arg15, _arg22);
                    return true;
                case 4:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg04 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    int _arg16 = data.readInt();
                    int _arg23 = data.readInt();
                    adjustVolume(_arg04, _arg16, _arg23);
                    return true;
                case 5:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg05 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    play(_arg05);
                    return true;
                case 6:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg06 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    pause(_arg06);
                    return true;
                case 7:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg07 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    reset(_arg07);
                    return true;
                case 8:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg08 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    prepare(_arg08);
                    return true;
                case 9:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg09 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    fastForward(_arg09);
                    return true;
                case 10:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg010 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    rewind(_arg010);
                    return true;
                case 11:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg011 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    long _arg17 = data.readLong();
                    seekTo(_arg011, _arg17);
                    return true;
                case 12:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg012 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    ResultReceiver _arg3 = data.readInt() != 0 ? (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(data) : null;
                    sendCustomCommand(_arg012, _arg1, _arg2, _arg3);
                    return true;
                case 13:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg013 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg12 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    Bundle _arg24 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    prepareFromUri(_arg013, _arg12, _arg24);
                    return true;
                case 14:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg014 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    String _arg18 = data.readString();
                    Bundle _arg25 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    prepareFromSearch(_arg014, _arg18, _arg25);
                    return true;
                case 15:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg015 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    String _arg19 = data.readString();
                    Bundle _arg26 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    prepareFromMediaId(_arg015, _arg19, _arg26);
                    return true;
                case 16:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg016 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg13 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        _arg13 = null;
                    }
                    Bundle _arg27 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    playFromUri(_arg016, _arg13, _arg27);
                    return true;
                case 17:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg017 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    String _arg110 = data.readString();
                    Bundle _arg28 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    playFromSearch(_arg017, _arg110, _arg28);
                    return true;
                case 18:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg018 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    String _arg111 = data.readString();
                    Bundle _arg29 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    playFromMediaId(_arg018, _arg111, _arg29);
                    return true;
                case 19:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg019 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    String _arg112 = data.readString();
                    Bundle _arg210 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    setRating(_arg019, _arg112, _arg210);
                    return true;
                case 20:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg020 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    float _arg113 = data.readFloat();
                    setPlaybackSpeed(_arg020, _arg113);
                    return true;
                case 21:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg021 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    List<Bundle> _arg114 = data.createTypedArrayList(Bundle.CREATOR);
                    Bundle _arg211 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    setPlaylist(_arg021, _arg114, _arg211);
                    return true;
                case 22:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg022 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    Bundle _arg115 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    updatePlaylistMetadata(_arg022, _arg115);
                    return true;
                case 23:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg023 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    int _arg116 = data.readInt();
                    Bundle _arg212 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    addPlaylistItem(_arg023, _arg116, _arg212);
                    return true;
                case 24:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg024 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    Bundle _arg117 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    removePlaylistItem(_arg024, _arg117);
                    return true;
                case 25:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg025 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    int _arg118 = data.readInt();
                    Bundle _arg213 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    replacePlaylistItem(_arg025, _arg118, _arg213);
                    return true;
                case 26:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg026 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    Bundle _arg119 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    skipToPlaylistItem(_arg026, _arg119);
                    return true;
                case 27:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg027 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    skipToPreviousItem(_arg027);
                    return true;
                case 28:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg028 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    skipToNextItem(_arg028);
                    return true;
                case 29:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg029 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    int _arg120 = data.readInt();
                    setRepeatMode(_arg029, _arg120);
                    return true;
                case 30:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg030 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    int _arg121 = data.readInt();
                    setShuffleMode(_arg030, _arg121);
                    return true;
                case 31:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg031 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    subscribeRoutesInfo(_arg031);
                    return true;
                case 32:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg032 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    unsubscribeRoutesInfo(_arg032);
                    return true;
                case 33:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg033 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    Bundle _arg122 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    selectRoute(_arg033, _arg122);
                    return true;
                case 34:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg034 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    Bundle _arg123 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    getLibraryRoot(_arg034, _arg123);
                    return true;
                case 35:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg035 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    String _arg124 = data.readString();
                    getItem(_arg035, _arg124);
                    return true;
                case 36:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg036 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    String _arg125 = data.readString();
                    int _arg214 = data.readInt();
                    int _arg32 = data.readInt();
                    Bundle _arg4 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    getChildren(_arg036, _arg125, _arg214, _arg32, _arg4);
                    return true;
                case 37:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg037 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    String _arg126 = data.readString();
                    Bundle _arg215 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    search(_arg037, _arg126, _arg215);
                    return true;
                case 38:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg038 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    String _arg127 = data.readString();
                    int _arg216 = data.readInt();
                    int _arg33 = data.readInt();
                    Bundle _arg42 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    getSearchResult(_arg038, _arg127, _arg216, _arg33, _arg42);
                    return true;
                case 39:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg039 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    String _arg128 = data.readString();
                    Bundle _arg217 = data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null;
                    subscribe(_arg039, _arg128, _arg217);
                    return true;
                case 40:
                    data.enforceInterface("android.support.v4.media.IMediaSession2");
                    IMediaController2 _arg040 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                    String _arg129 = data.readString();
                    unsubscribe(_arg040, _arg129);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: classes.dex */
        public static class Proxy implements IMediaSession2 {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // android.support.v4.media.IMediaSession2
            public void connect(IMediaController2 caller, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void release(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void setVolumeTo(IMediaController2 caller, int value, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(value);
                    _data.writeInt(flags);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void adjustVolume(IMediaController2 caller, int direction, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(direction);
                    _data.writeInt(flags);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void play(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void pause(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void reset(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void prepare(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void fastForward(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void rewind(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void seekTo(IMediaController2 caller, long pos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeLong(pos);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void sendCustomCommand(IMediaController2 caller, Bundle command, Bundle args, ResultReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (command != null) {
                        _data.writeInt(1);
                        command.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (receiver != null) {
                        _data.writeInt(1);
                        receiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void prepareFromUri(IMediaController2 caller, Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void prepareFromSearch(IMediaController2 caller, String query, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(query);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void prepareFromMediaId(IMediaController2 caller, String mediaId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(mediaId);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void playFromUri(IMediaController2 caller, Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void playFromSearch(IMediaController2 caller, String query, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(query);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void playFromMediaId(IMediaController2 caller, String mediaId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(mediaId);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void setRating(IMediaController2 caller, String mediaId, Bundle rating) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(mediaId);
                    if (rating != null) {
                        _data.writeInt(1);
                        rating.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void setPlaybackSpeed(IMediaController2 caller, float speed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeFloat(speed);
                    this.mRemote.transact(20, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void setPlaylist(IMediaController2 caller, List<Bundle> playlist, Bundle metadata) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeTypedList(playlist);
                    if (metadata != null) {
                        _data.writeInt(1);
                        metadata.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(21, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void updatePlaylistMetadata(IMediaController2 caller, Bundle metadata) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (metadata != null) {
                        _data.writeInt(1);
                        metadata.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(22, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void addPlaylistItem(IMediaController2 caller, int index, Bundle mediaItem) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(index);
                    if (mediaItem != null) {
                        _data.writeInt(1);
                        mediaItem.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(23, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void removePlaylistItem(IMediaController2 caller, Bundle mediaItem) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (mediaItem != null) {
                        _data.writeInt(1);
                        mediaItem.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(24, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void replacePlaylistItem(IMediaController2 caller, int index, Bundle mediaItem) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(index);
                    if (mediaItem != null) {
                        _data.writeInt(1);
                        mediaItem.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(25, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void skipToPlaylistItem(IMediaController2 caller, Bundle mediaItem) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (mediaItem != null) {
                        _data.writeInt(1);
                        mediaItem.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(26, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void skipToPreviousItem(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(27, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void skipToNextItem(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(28, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void setRepeatMode(IMediaController2 caller, int repeatMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(repeatMode);
                    this.mRemote.transact(29, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void setShuffleMode(IMediaController2 caller, int shuffleMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(shuffleMode);
                    this.mRemote.transact(30, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void subscribeRoutesInfo(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(31, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void unsubscribeRoutesInfo(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(32, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void selectRoute(IMediaController2 caller, Bundle route) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (route != null) {
                        _data.writeInt(1);
                        route.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(33, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void getLibraryRoot(IMediaController2 caller, Bundle rootHints) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (rootHints != null) {
                        _data.writeInt(1);
                        rootHints.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(34, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void getItem(IMediaController2 caller, String mediaId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(mediaId);
                    this.mRemote.transact(35, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void getChildren(IMediaController2 caller, String parentId, int page, int pageSize, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(parentId);
                    _data.writeInt(page);
                    _data.writeInt(pageSize);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(36, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void search(IMediaController2 caller, String query, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(query);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(37, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void getSearchResult(IMediaController2 caller, String query, int page, int pageSize, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(query);
                    _data.writeInt(page);
                    _data.writeInt(pageSize);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(38, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void subscribe(IMediaController2 caller, String parentId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(parentId);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(39, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void unsubscribe(IMediaController2 caller, String parentId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.IMediaSession2");
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(parentId);
                    this.mRemote.transact(40, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}
