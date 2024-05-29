package com.android.systemui.volume;

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.IRemoteVolumeController;
import android.media.MediaMetadata;
import android.media.session.ISessionController;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
/* loaded from: a.zip:com/android/systemui/volume/MediaSessions.class */
public class MediaSessions {
    private static final String TAG = Util.logTag(MediaSessions.class);
    private final Callbacks mCallbacks;
    private final Context mContext;
    private final H mHandler;
    private boolean mInit;
    private final MediaSessionManager mMgr;
    private final Map<MediaSession.Token, MediaControllerRecord> mRecords = new HashMap();
    private final MediaSessionManager.OnActiveSessionsChangedListener mSessionsListener = new MediaSessionManager.OnActiveSessionsChangedListener(this) { // from class: com.android.systemui.volume.MediaSessions.1
        final MediaSessions this$0;

        {
            this.this$0 = this;
        }

        @Override // android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
        public void onActiveSessionsChanged(List<MediaController> list) {
            this.this$0.onActiveSessionsUpdatedH(list);
        }
    };
    private final IRemoteVolumeController mRvc = new IRemoteVolumeController.Stub(this) { // from class: com.android.systemui.volume.MediaSessions.2
        final MediaSessions this$0;

        {
            this.this$0 = this;
        }

        public void remoteVolumeChanged(ISessionController iSessionController, int i) throws RemoteException {
            this.this$0.mHandler.obtainMessage(2, i, 0, iSessionController).sendToTarget();
        }

        public void updateRemoteController(ISessionController iSessionController) throws RemoteException {
            this.this$0.mHandler.obtainMessage(3, iSessionController).sendToTarget();
        }
    };

    /* loaded from: a.zip:com/android/systemui/volume/MediaSessions$Callbacks.class */
    public interface Callbacks {
        void onRemoteRemoved(MediaSession.Token token);

        void onRemoteUpdate(MediaSession.Token token, String str, MediaController.PlaybackInfo playbackInfo);

        void onRemoteVolumeChanged(MediaSession.Token token, int i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/MediaSessions$H.class */
    public final class H extends Handler {
        final MediaSessions this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        private H(MediaSessions mediaSessions, Looper looper) {
            super(looper);
            this.this$0 = mediaSessions;
        }

        /* synthetic */ H(MediaSessions mediaSessions, Looper looper, H h) {
            this(mediaSessions, looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    this.this$0.onActiveSessionsUpdatedH(this.this$0.mMgr.getActiveSessions(null));
                    return;
                case 2:
                    this.this$0.onRemoteVolumeChangedH((ISessionController) message.obj, message.arg1);
                    return;
                case 3:
                    this.this$0.onUpdateRemoteControllerH((ISessionController) message.obj);
                    return;
                default:
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/MediaSessions$MediaControllerRecord.class */
    public final class MediaControllerRecord extends MediaController.Callback {
        private final MediaController controller;
        private String name;
        private boolean sentRemote;
        final MediaSessions this$0;

        private MediaControllerRecord(MediaSessions mediaSessions, MediaController mediaController) {
            this.this$0 = mediaSessions;
            this.controller = mediaController;
        }

        /* synthetic */ MediaControllerRecord(MediaSessions mediaSessions, MediaController mediaController, MediaControllerRecord mediaControllerRecord) {
            this(mediaSessions, mediaController);
        }

        private String cb(String str) {
            return str + " " + this.controller.getPackageName() + " ";
        }

        @Override // android.media.session.MediaController.Callback
        public void onAudioInfoChanged(MediaController.PlaybackInfo playbackInfo) {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onAudioInfoChanged") + Util.playbackInfoToString(playbackInfo) + " sentRemote=" + this.sentRemote);
            }
            boolean isRemote = MediaSessions.isRemote(playbackInfo);
            if (!isRemote && this.sentRemote) {
                this.this$0.mCallbacks.onRemoteRemoved(this.controller.getSessionToken());
                this.sentRemote = false;
            } else if (isRemote) {
                this.this$0.updateRemoteH(this.controller.getSessionToken(), this.name, playbackInfo);
                this.sentRemote = true;
            }
        }

        @Override // android.media.session.MediaController.Callback
        public void onExtrasChanged(Bundle bundle) {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onExtrasChanged") + bundle);
            }
        }

        @Override // android.media.session.MediaController.Callback
        public void onMetadataChanged(MediaMetadata mediaMetadata) {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onMetadataChanged") + Util.mediaMetadataToString(mediaMetadata));
            }
        }

        @Override // android.media.session.MediaController.Callback
        public void onPlaybackStateChanged(PlaybackState playbackState) {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onPlaybackStateChanged") + Util.playbackStateToString(playbackState));
            }
        }

        @Override // android.media.session.MediaController.Callback
        public void onQueueChanged(List<MediaSession.QueueItem> list) {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onQueueChanged") + list);
            }
        }

        @Override // android.media.session.MediaController.Callback
        public void onQueueTitleChanged(CharSequence charSequence) {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onQueueTitleChanged") + charSequence);
            }
        }

        @Override // android.media.session.MediaController.Callback
        public void onSessionDestroyed() {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onSessionDestroyed"));
            }
        }

        @Override // android.media.session.MediaController.Callback
        public void onSessionEvent(String str, Bundle bundle) {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onSessionEvent") + "event=" + str + " extras=" + bundle);
            }
        }
    }

    public MediaSessions(Context context, Looper looper, Callbacks callbacks) {
        this.mContext = context;
        this.mHandler = new H(this, looper, null);
        this.mMgr = (MediaSessionManager) context.getSystemService("media_session");
        this.mCallbacks = callbacks;
    }

    private static void dump(int i, PrintWriter printWriter, MediaController mediaController) {
        printWriter.println("  Controller " + i + ": " + mediaController.getPackageName());
        Bundle extras = mediaController.getExtras();
        long flags = mediaController.getFlags();
        MediaMetadata metadata = mediaController.getMetadata();
        MediaController.PlaybackInfo playbackInfo = mediaController.getPlaybackInfo();
        PlaybackState playbackState = mediaController.getPlaybackState();
        List<MediaSession.QueueItem> queue = mediaController.getQueue();
        CharSequence queueTitle = mediaController.getQueueTitle();
        int ratingType = mediaController.getRatingType();
        PendingIntent sessionActivity = mediaController.getSessionActivity();
        printWriter.println("    PlaybackState: " + Util.playbackStateToString(playbackState));
        printWriter.println("    PlaybackInfo: " + Util.playbackInfoToString(playbackInfo));
        if (metadata != null) {
            printWriter.println("  MediaMetadata.desc=" + metadata.getDescription());
        }
        printWriter.println("    RatingType: " + ratingType);
        printWriter.println("    Flags: " + flags);
        if (extras != null) {
            printWriter.println("    Extras:");
            for (String str : extras.keySet()) {
                printWriter.println("      " + str + "=" + extras.get(str));
            }
        }
        if (queueTitle != null) {
            printWriter.println("    QueueTitle: " + queueTitle);
        }
        if (queue != null && !queue.isEmpty()) {
            printWriter.println("    Queue:");
            Iterator<T> it = queue.iterator();
            while (it.hasNext()) {
                printWriter.println("      " + ((MediaSession.QueueItem) it.next()));
            }
        }
        if (playbackInfo != null) {
            printWriter.println("    sessionActivity: " + sessionActivity);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isRemote(MediaController.PlaybackInfo playbackInfo) {
        boolean z = false;
        if (playbackInfo != null) {
            z = false;
            if (playbackInfo.getPlaybackType() == 2) {
                z = true;
            }
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRemoteVolumeChangedH(ISessionController iSessionController, int i) {
        MediaController mediaController = new MediaController(this.mContext, iSessionController);
        if (D.BUG) {
            Log.d(TAG, "remoteVolumeChangedH " + mediaController.getPackageName() + " " + Util.audioManagerFlagsToString(i));
        }
        this.mCallbacks.onRemoteVolumeChanged(mediaController.getSessionToken(), i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onUpdateRemoteControllerH(ISessionController iSessionController) {
        MediaController mediaController = null;
        if (iSessionController != null) {
            mediaController = new MediaController(this.mContext, iSessionController);
        }
        String packageName = mediaController != null ? mediaController.getPackageName() : null;
        if (D.BUG) {
            Log.d(TAG, "updateRemoteControllerH " + packageName);
        }
        postUpdateSessions();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateRemoteH(MediaSession.Token token, String str, MediaController.PlaybackInfo playbackInfo) {
        if (this.mCallbacks != null) {
            this.mCallbacks.onRemoteUpdate(token, str, playbackInfo);
        }
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println(getClass().getSimpleName() + " state:");
        printWriter.print("  mInit: ");
        printWriter.println(this.mInit);
        printWriter.print("  mRecords.size: ");
        printWriter.println(this.mRecords.size());
        int i = 0;
        for (MediaControllerRecord mediaControllerRecord : this.mRecords.values()) {
            i++;
            dump(i, printWriter, mediaControllerRecord.controller);
        }
    }

    protected String getControllerName(MediaController mediaController) {
        PackageManager packageManager = this.mContext.getPackageManager();
        String packageName = mediaController.getPackageName();
        try {
            String trim = Objects.toString(packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager), "").trim();
            if (trim.length() > 0) {
                return trim;
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return packageName;
    }

    public void init() {
        if (D.BUG) {
            Log.d(TAG, "init");
        }
        this.mMgr.addOnActiveSessionsChangedListener(this.mSessionsListener, null, this.mHandler);
        this.mInit = true;
        postUpdateSessions();
        this.mMgr.setRemoteVolumeController(this.mRvc);
    }

    protected void onActiveSessionsUpdatedH(List<MediaController> list) {
        if (D.BUG) {
            Log.d(TAG, "onActiveSessionsUpdatedH n=" + list.size());
        }
        HashSet<MediaSession.Token> hashSet = new HashSet(this.mRecords.keySet());
        for (MediaController mediaController : list) {
            MediaSession.Token sessionToken = mediaController.getSessionToken();
            MediaController.PlaybackInfo playbackInfo = mediaController.getPlaybackInfo();
            hashSet.remove(sessionToken);
            if (!this.mRecords.containsKey(sessionToken)) {
                MediaControllerRecord mediaControllerRecord = new MediaControllerRecord(this, mediaController, null);
                mediaControllerRecord.name = getControllerName(mediaController);
                this.mRecords.put(sessionToken, mediaControllerRecord);
                mediaController.registerCallback(mediaControllerRecord, this.mHandler);
            }
            MediaControllerRecord mediaControllerRecord2 = this.mRecords.get(sessionToken);
            if (isRemote(playbackInfo)) {
                updateRemoteH(sessionToken, mediaControllerRecord2.name, playbackInfo);
                mediaControllerRecord2.sentRemote = true;
            }
        }
        for (MediaSession.Token token : hashSet) {
            MediaControllerRecord mediaControllerRecord3 = this.mRecords.get(token);
            mediaControllerRecord3.controller.unregisterCallback(mediaControllerRecord3);
            this.mRecords.remove(token);
            if (D.BUG) {
                Log.d(TAG, "Removing " + mediaControllerRecord3.name + " sentRemote=" + mediaControllerRecord3.sentRemote);
            }
            if (mediaControllerRecord3.sentRemote) {
                this.mCallbacks.onRemoteRemoved(token);
                mediaControllerRecord3.sentRemote = false;
            }
        }
    }

    protected void postUpdateSessions() {
        if (this.mInit) {
            this.mHandler.sendEmptyMessage(1);
        }
    }

    public void setVolume(MediaSession.Token token, int i) {
        MediaControllerRecord mediaControllerRecord = this.mRecords.get(token);
        if (mediaControllerRecord == null) {
            Log.w(TAG, "setVolume: No record found for token " + token);
            return;
        }
        if (D.BUG) {
            Log.d(TAG, "Setting level to " + i);
        }
        mediaControllerRecord.controller.setVolumeTo(i, 0);
    }
}
