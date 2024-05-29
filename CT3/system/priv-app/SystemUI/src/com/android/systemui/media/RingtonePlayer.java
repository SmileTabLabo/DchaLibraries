package com.android.systemui.media;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.IAudioService;
import android.media.IRingtonePlayer;
import android.media.Ringtone;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.util.Log;
import com.android.systemui.SystemUI;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
/* loaded from: a.zip:com/android/systemui/media/RingtonePlayer.class */
public class RingtonePlayer extends SystemUI {
    private IAudioService mAudioService;
    private final NotificationPlayer mAsyncPlayer = new NotificationPlayer("RingtonePlayer");
    private final HashMap<IBinder, Client> mClients = new HashMap<>();
    private IRingtonePlayer mCallback = new IRingtonePlayer.Stub(this) { // from class: com.android.systemui.media.RingtonePlayer.1
        final RingtonePlayer this$0;

        {
            this.this$0 = this;
        }

        public String getTitle(Uri uri) {
            return Ringtone.getTitle(this.this$0.getContextForUser(Binder.getCallingUserHandle()), uri, false, false);
        }

        public boolean isPlaying(IBinder iBinder) {
            Client client;
            synchronized (this.this$0.mClients) {
                client = (Client) this.this$0.mClients.get(iBinder);
            }
            if (client != null) {
                return client.mRingtone.isPlaying();
            }
            return false;
        }

        /* JADX WARN: Removed duplicated region for block: B:61:0x012d  */
        /* JADX WARN: Removed duplicated region for block: B:72:0x0157  */
        /* JADX WARN: Removed duplicated region for block: B:82:0x011e A[EXC_TOP_SPLITTER, SYNTHETIC] */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public ParcelFileDescriptor openRingtone(Uri uri) {
            Cursor cursor;
            Throwable th;
            Throwable th2;
            ContentResolver contentResolver = this.this$0.getContextForUser(Binder.getCallingUserHandle()).getContentResolver();
            if (uri.toString().startsWith(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())) {
                Cursor cursor2 = null;
                try {
                    Cursor query = contentResolver.query(uri, new String[]{"is_ringtone", "is_alarm", "is_notification"}, null, null, null);
                    if (!query.moveToFirst() || (query.getInt(0) == 0 && query.getInt(1) == 0 && query.getInt(2) == 0)) {
                        Throwable th3 = null;
                        if (query != null) {
                            try {
                                query.close();
                                th3 = null;
                            } catch (Throwable th4) {
                                th3 = th4;
                            }
                        }
                        if (th3 != null) {
                            throw th3;
                        }
                    } else {
                        cursor2 = query;
                        cursor = query;
                        try {
                            ParcelFileDescriptor openFileDescriptor = contentResolver.openFileDescriptor(uri, "r");
                            Throwable th5 = null;
                            if (query != null) {
                                try {
                                    query.close();
                                    th5 = null;
                                } catch (Throwable th6) {
                                    th5 = th6;
                                }
                            }
                            if (th5 != null) {
                                throw th5;
                            }
                            return openFileDescriptor;
                        } catch (IOException e) {
                            throw new SecurityException(e);
                        } catch (Throwable th7) {
                            th = th7;
                            th = null;
                            th2 = th;
                            if (cursor != null) {
                            }
                            if (th2 == null) {
                            }
                        }
                    }
                } catch (Throwable th8) {
                    th = th8;
                    try {
                        throw th;
                    } catch (Throwable th9) {
                        cursor = cursor2;
                        th = th9;
                        th2 = th;
                        if (cursor != null) {
                            try {
                                cursor.close();
                                th2 = th;
                            } catch (Throwable th10) {
                                if (th == null) {
                                    th2 = th10;
                                } else {
                                    th2 = th;
                                    if (th != th10) {
                                        th.addSuppressed(th10);
                                        th2 = th;
                                    }
                                }
                            }
                        }
                        if (th2 == null) {
                            throw th2;
                        }
                        throw th;
                    }
                }
            }
            throw new SecurityException("Uri is not ringtone, alarm, or notification: " + uri);
        }

        public void play(IBinder iBinder, Uri uri, AudioAttributes audioAttributes, float f, boolean z) throws RemoteException {
            Client client;
            synchronized (this.this$0.mClients) {
                Client client2 = (Client) this.this$0.mClients.get(iBinder);
                client = client2;
                if (client2 == null) {
                    client = new Client(this.this$0, iBinder, uri, Binder.getCallingUserHandle(), audioAttributes);
                    iBinder.linkToDeath(client, 0);
                    this.this$0.mClients.put(iBinder, client);
                }
            }
            client.mRingtone.setLooping(z);
            client.mRingtone.setVolume(f);
            client.mRingtone.play();
        }

        public void playAsync(Uri uri, UserHandle userHandle, boolean z, AudioAttributes audioAttributes) {
            if (Binder.getCallingUid() != 1000) {
                throw new SecurityException("Async playback only available from system UID.");
            }
            UserHandle userHandle2 = userHandle;
            if (UserHandle.ALL.equals(userHandle)) {
                userHandle2 = UserHandle.SYSTEM;
            }
            this.this$0.mAsyncPlayer.play(this.this$0.getContextForUser(userHandle2), uri, z, audioAttributes);
        }

        public void setPlaybackProperties(IBinder iBinder, float f, boolean z) {
            Client client;
            synchronized (this.this$0.mClients) {
                client = (Client) this.this$0.mClients.get(iBinder);
            }
            if (client != null) {
                client.mRingtone.setVolume(f);
                client.mRingtone.setLooping(z);
            }
        }

        public void stop(IBinder iBinder) {
            Client client;
            synchronized (this.this$0.mClients) {
                client = (Client) this.this$0.mClients.remove(iBinder);
            }
            if (client != null) {
                client.mToken.unlinkToDeath(client, 0);
                client.mRingtone.stop();
            }
        }

        public void stopAsync() {
            if (Binder.getCallingUid() != 1000) {
                throw new SecurityException("Async playback only available from system UID.");
            }
            this.this$0.mAsyncPlayer.stop();
        }
    };

    /* loaded from: a.zip:com/android/systemui/media/RingtonePlayer$Client.class */
    private class Client implements IBinder.DeathRecipient {
        private final Ringtone mRingtone;
        private final IBinder mToken;
        final RingtonePlayer this$0;

        public Client(RingtonePlayer ringtonePlayer, IBinder iBinder, Uri uri, UserHandle userHandle, AudioAttributes audioAttributes) {
            this.this$0 = ringtonePlayer;
            this.mToken = iBinder;
            this.mRingtone = new Ringtone(ringtonePlayer.getContextForUser(userHandle), false);
            this.mRingtone.setAudioAttributes(audioAttributes);
            this.mRingtone.setUri(uri);
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (this.this$0.mClients) {
                this.this$0.mClients.remove(this.mToken);
            }
            this.mRingtone.stop();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Context getContextForUser(UserHandle userHandle) {
        try {
            return this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, userHandle);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override // com.android.systemui.SystemUI
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("Clients:");
        synchronized (this.mClients) {
            for (Client client : this.mClients.values()) {
                printWriter.print("  mToken=");
                printWriter.print(client.mToken);
                printWriter.print(" mUri=");
                printWriter.println(client.mRingtone.getUri());
            }
        }
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mAsyncPlayer.setUsesWakeLock(this.mContext);
        this.mAudioService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        try {
            this.mAudioService.setRingtonePlayer(this.mCallback);
        } catch (RemoteException e) {
            Log.e("RingtonePlayer", "Problem registering RingtonePlayer: " + e);
        }
    }
}
