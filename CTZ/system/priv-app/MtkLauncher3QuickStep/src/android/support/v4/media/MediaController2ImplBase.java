package android.support.v4.media;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.mediacompat.Rating2;
import android.support.v4.media.IMediaSession2;
import android.support.v4.media.MediaController2;
import android.support.v4.media.MediaSession2;
import android.util.Log;
import java.util.List;
import java.util.concurrent.Executor;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class MediaController2ImplBase implements MediaController2.SupportLibraryImpl {
    @GuardedBy("mLock")
    private SessionCommandGroup2 mAllowedCommands;
    @GuardedBy("mLock")
    private long mBufferedPositionMs;
    @GuardedBy("mLock")
    private int mBufferingState;
    private final MediaController2.ControllerCallback mCallback;
    private final Executor mCallbackExecutor;
    private final Context mContext;
    final MediaController2Stub mControllerStub;
    @GuardedBy("mLock")
    private MediaItem2 mCurrentMediaItem;
    private final IBinder.DeathRecipient mDeathRecipient;
    @GuardedBy("mLock")
    private volatile IMediaSession2 mISession2;
    private final MediaController2 mInstance;
    @GuardedBy("mLock")
    private boolean mIsReleased;
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private MediaController2.PlaybackInfo mPlaybackInfo;
    @GuardedBy("mLock")
    private float mPlaybackSpeed;
    @GuardedBy("mLock")
    private int mPlayerState;
    @GuardedBy("mLock")
    private List<MediaItem2> mPlaylist;
    @GuardedBy("mLock")
    private MediaMetadata2 mPlaylistMetadata;
    @GuardedBy("mLock")
    private long mPositionEventTimeMs;
    @GuardedBy("mLock")
    private long mPositionMs;
    @GuardedBy("mLock")
    private int mRepeatMode;
    @GuardedBy("mLock")
    private SessionServiceConnection mServiceConnection;
    @GuardedBy("mLock")
    private PendingIntent mSessionActivity;
    @GuardedBy("mLock")
    private int mShuffleMode;
    private final SessionToken2 mToken;
    static final String TAG = "MC2ImplBase";
    static final boolean DEBUG = Log.isLoggable(TAG, 3);

    /* JADX INFO: Access modifiers changed from: package-private */
    public MediaController2ImplBase(Context context, MediaController2 instance, SessionToken2 token, Executor executor, MediaController2.ControllerCallback callback) {
        this.mInstance = instance;
        if (context == null) {
            throw new IllegalArgumentException("context shouldn't be null");
        }
        if (token == null) {
            throw new IllegalArgumentException("token shouldn't be null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback shouldn't be null");
        }
        if (executor == null) {
            throw new IllegalArgumentException("executor shouldn't be null");
        }
        this.mContext = context;
        this.mControllerStub = new MediaController2Stub(this);
        this.mToken = token;
        this.mCallback = callback;
        this.mCallbackExecutor = executor;
        this.mDeathRecipient = new IBinder.DeathRecipient() { // from class: android.support.v4.media.MediaController2ImplBase.1
            @Override // android.os.IBinder.DeathRecipient
            public void binderDied() {
                MediaController2ImplBase.this.mInstance.close();
            }
        };
        IMediaSession2 iSession2 = IMediaSession2.Stub.asInterface((IBinder) this.mToken.getBinder());
        if (this.mToken.getType() == 0) {
            this.mServiceConnection = null;
            connectToSession(iSession2);
            return;
        }
        this.mServiceConnection = new SessionServiceConnection();
        connectToService();
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        if (DEBUG) {
            Log.d(TAG, "release from " + this.mToken);
        }
        synchronized (this.mLock) {
            try {
                try {
                    IMediaSession2 iSession2 = this.mISession2;
                    if (this.mIsReleased) {
                        return;
                    }
                    this.mIsReleased = true;
                    if (this.mServiceConnection != null) {
                        this.mContext.unbindService(this.mServiceConnection);
                        this.mServiceConnection = null;
                    }
                    this.mISession2 = null;
                    this.mControllerStub.destroy();
                    if (iSession2 != null) {
                        try {
                            iSession2.asBinder().unlinkToDeath(this.mDeathRecipient, 0);
                            iSession2.release(this.mControllerStub);
                        } catch (RemoteException e) {
                        }
                    }
                    this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.2
                        @Override // java.lang.Runnable
                        public void run() {
                            MediaController2ImplBase.this.mCallback.onDisconnected(MediaController2ImplBase.this.mInstance);
                        }
                    });
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public SessionToken2 getSessionToken() {
        return this.mToken;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public boolean isConnected() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mISession2 != null;
        }
        return z;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void play() {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(1);
        if (iSession2 != null) {
            try {
                iSession2.play(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void pause() {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(2);
        if (iSession2 != null) {
            try {
                iSession2.pause(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void reset() {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(3);
        if (iSession2 != null) {
            try {
                iSession2.reset(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void prepare() {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(6);
        if (iSession2 != null) {
            try {
                iSession2.prepare(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void fastForward() {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(7);
        if (iSession2 != null) {
            try {
                iSession2.fastForward(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void rewind() {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(8);
        if (iSession2 != null) {
            try {
                iSession2.rewind(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void seekTo(long pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("position shouldn't be negative");
        }
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(9);
        if (iSession2 != null) {
            try {
                iSession2.seekTo(this.mControllerStub, pos);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void skipForward() {
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void skipBackward() {
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void playFromMediaId(@NonNull String mediaId, @Nullable Bundle extras) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(22);
        if (iSession2 != null) {
            try {
                iSession2.playFromMediaId(this.mControllerStub, mediaId, extras);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void playFromSearch(@NonNull String query, @Nullable Bundle extras) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(24);
        if (iSession2 != null) {
            try {
                iSession2.playFromSearch(this.mControllerStub, query, extras);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void playFromUri(Uri uri, Bundle extras) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(23);
        if (iSession2 != null) {
            try {
                iSession2.playFromUri(this.mControllerStub, uri, extras);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void prepareFromMediaId(@NonNull String mediaId, @Nullable Bundle extras) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(25);
        if (iSession2 != null) {
            try {
                iSession2.prepareFromMediaId(this.mControllerStub, mediaId, extras);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void prepareFromSearch(@NonNull String query, @Nullable Bundle extras) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(27);
        if (iSession2 != null) {
            try {
                iSession2.prepareFromSearch(this.mControllerStub, query, extras);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void prepareFromUri(@NonNull Uri uri, @Nullable Bundle extras) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(26);
        if (iSession2 != null) {
            try {
                iSession2.prepareFromUri(this.mControllerStub, uri, extras);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void setVolumeTo(int value, int flags) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(10);
        if (iSession2 != null) {
            try {
                iSession2.setVolumeTo(this.mControllerStub, value, flags);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void adjustVolume(int direction, int flags) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(11);
        if (iSession2 != null) {
            try {
                iSession2.adjustVolume(this.mControllerStub, direction, flags);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public PendingIntent getSessionActivity() {
        PendingIntent pendingIntent;
        synchronized (this.mLock) {
            pendingIntent = this.mSessionActivity;
        }
        return pendingIntent;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public int getPlayerState() {
        int i;
        synchronized (this.mLock) {
            i = this.mPlayerState;
        }
        return i;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public long getDuration() {
        synchronized (this.mLock) {
            MediaMetadata2 metadata = this.mCurrentMediaItem.getMetadata();
            if (metadata != null && metadata.containsKey("android.media.metadata.DURATION")) {
                return metadata.getLong("android.media.metadata.DURATION");
            }
            return -1L;
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public long getCurrentPosition() {
        synchronized (this.mLock) {
            if (this.mISession2 == null) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return -1L;
            }
            long timeDiff = this.mInstance.mTimeDiff != null ? this.mInstance.mTimeDiff.longValue() : SystemClock.elapsedRealtime() - this.mPositionEventTimeMs;
            long expectedPosition = this.mPositionMs + (this.mPlaybackSpeed * ((float) timeDiff));
            return Math.max(0L, expectedPosition);
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public float getPlaybackSpeed() {
        synchronized (this.mLock) {
            if (this.mISession2 == null) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return 0.0f;
            }
            return this.mPlaybackSpeed;
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void setPlaybackSpeed(float speed) {
        synchronized (this.mLock) {
            IMediaSession2 iSession2 = getSessionInterfaceIfAble(39);
            if (iSession2 != null) {
                try {
                    iSession2.setPlaybackSpeed(this.mControllerStub, speed);
                } catch (RemoteException e) {
                    Log.w(TAG, "Cannot connect to the service or the session is gone", e);
                }
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public int getBufferingState() {
        synchronized (this.mLock) {
            if (this.mISession2 == null) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return 0;
            }
            return this.mBufferingState;
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public long getBufferedPosition() {
        synchronized (this.mLock) {
            if (this.mISession2 == null) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return -1L;
            }
            return this.mBufferedPositionMs;
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public MediaController2.PlaybackInfo getPlaybackInfo() {
        MediaController2.PlaybackInfo playbackInfo;
        synchronized (this.mLock) {
            playbackInfo = this.mPlaybackInfo;
        }
        return playbackInfo;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void setRating(@NonNull String mediaId, @NonNull Rating2 rating) {
        synchronized (this.mLock) {
            try {
                try {
                    IMediaSession2 iSession2 = this.mISession2;
                    if (iSession2 != null) {
                        try {
                            iSession2.setRating(this.mControllerStub, mediaId, rating.toBundle());
                        } catch (RemoteException e) {
                            Log.w(TAG, "Cannot connect to the service or the session is gone", e);
                        }
                    }
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void sendCustomCommand(@NonNull SessionCommand2 command, Bundle args, @Nullable ResultReceiver cb) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(command);
        if (iSession2 != null) {
            try {
                iSession2.sendCustomCommand(this.mControllerStub, command.toBundle(), args, cb);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public List<MediaItem2> getPlaylist() {
        List<MediaItem2> list;
        synchronized (this.mLock) {
            list = this.mPlaylist;
        }
        return list;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void setPlaylist(@NonNull List<MediaItem2> list, @Nullable MediaMetadata2 metadata) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(19);
        if (iSession2 != null) {
            try {
                iSession2.setPlaylist(this.mControllerStub, MediaUtils2.convertMediaItem2ListToBundleList(list), metadata == null ? null : metadata.toBundle());
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void updatePlaylistMetadata(@Nullable MediaMetadata2 metadata) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(21);
        if (iSession2 != null) {
            try {
                iSession2.updatePlaylistMetadata(this.mControllerStub, metadata == null ? null : metadata.toBundle());
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public MediaMetadata2 getPlaylistMetadata() {
        MediaMetadata2 mediaMetadata2;
        synchronized (this.mLock) {
            mediaMetadata2 = this.mPlaylistMetadata;
        }
        return mediaMetadata2;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void addPlaylistItem(int index, @NonNull MediaItem2 item) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(15);
        if (iSession2 != null) {
            try {
                iSession2.addPlaylistItem(this.mControllerStub, index, item.toBundle());
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void removePlaylistItem(@NonNull MediaItem2 item) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(16);
        if (iSession2 != null) {
            try {
                iSession2.removePlaylistItem(this.mControllerStub, item.toBundle());
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void replacePlaylistItem(int index, @NonNull MediaItem2 item) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(17);
        if (iSession2 != null) {
            try {
                iSession2.replacePlaylistItem(this.mControllerStub, index, item.toBundle());
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public MediaItem2 getCurrentMediaItem() {
        MediaItem2 mediaItem2;
        synchronized (this.mLock) {
            mediaItem2 = this.mCurrentMediaItem;
        }
        return mediaItem2;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void skipToPreviousItem() {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(5);
        synchronized (this.mLock) {
            if (iSession2 != null) {
                try {
                    iSession2.skipToPreviousItem(this.mControllerStub);
                } catch (RemoteException e) {
                    Log.w(TAG, "Cannot connect to the service or the session is gone", e);
                }
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void skipToNextItem() {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(4);
        synchronized (this.mLock) {
            if (iSession2 != null) {
                try {
                    this.mISession2.skipToNextItem(this.mControllerStub);
                } catch (RemoteException e) {
                    Log.w(TAG, "Cannot connect to the service or the session is gone", e);
                }
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void skipToPlaylistItem(@NonNull MediaItem2 item) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(12);
        synchronized (this.mLock) {
            if (iSession2 != null) {
                try {
                    this.mISession2.skipToPlaylistItem(this.mControllerStub, item.toBundle());
                } catch (RemoteException e) {
                    Log.w(TAG, "Cannot connect to the service or the session is gone", e);
                }
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public int getRepeatMode() {
        int i;
        synchronized (this.mLock) {
            i = this.mRepeatMode;
        }
        return i;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void setRepeatMode(int repeatMode) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(14);
        if (iSession2 != null) {
            try {
                iSession2.setRepeatMode(this.mControllerStub, repeatMode);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public int getShuffleMode() {
        int i;
        synchronized (this.mLock) {
            i = this.mShuffleMode;
        }
        return i;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void setShuffleMode(int shuffleMode) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(13);
        if (iSession2 != null) {
            try {
                iSession2.setShuffleMode(this.mControllerStub, shuffleMode);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void subscribeRoutesInfo() {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(36);
        if (iSession2 != null) {
            try {
                iSession2.subscribeRoutesInfo(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void unsubscribeRoutesInfo() {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(37);
        if (iSession2 != null) {
            try {
                iSession2.unsubscribeRoutesInfo(this.mControllerStub);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void selectRoute(@NonNull Bundle route) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(38);
        if (iSession2 != null) {
            try {
                iSession2.selectRoute(this.mControllerStub, route);
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    @NonNull
    public Context getContext() {
        return this.mContext;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    @NonNull
    public MediaController2.ControllerCallback getCallback() {
        return this.mCallback;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    @NonNull
    public Executor getCallbackExecutor() {
        return this.mCallbackExecutor;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    @Nullable
    public MediaBrowserCompat getBrowserCompat() {
        return null;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    @NonNull
    public MediaController2 getInstance() {
        return this.mInstance;
    }

    private void connectToService() {
        Intent intent = new Intent(MediaSessionService2.SERVICE_INTERFACE);
        intent.setClassName(this.mToken.getPackageName(), this.mToken.getServiceName());
        synchronized (this.mLock) {
            boolean result = this.mContext.bindService(intent, this.mServiceConnection, 1);
            if (!result) {
                Log.w(TAG, "bind to " + this.mToken + " failed");
            } else if (DEBUG) {
                Log.d(TAG, "bind to " + this.mToken + " success");
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void connectToSession(IMediaSession2 sessionBinder) {
        try {
            sessionBinder.connect(this.mControllerStub, this.mContext.getPackageName());
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to call connection request. Framework will retry automatically");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public IMediaSession2 getSessionInterfaceIfAble(int commandCode) {
        synchronized (this.mLock) {
            if (!this.mAllowedCommands.hasCommand(commandCode)) {
                Log.w(TAG, "Controller isn't allowed to call command, commandCode=" + commandCode);
                return null;
            }
            return this.mISession2;
        }
    }

    IMediaSession2 getSessionInterfaceIfAble(SessionCommand2 command) {
        synchronized (this.mLock) {
            if (!this.mAllowedCommands.hasCommand(command)) {
                Log.w(TAG, "Controller isn't allowed to call command, command=" + command);
                return null;
            }
            return this.mISession2;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void notifyCurrentMediaItemChanged(final MediaItem2 item) {
        synchronized (this.mLock) {
            this.mCurrentMediaItem = item;
        }
        this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.3
            @Override // java.lang.Runnable
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onCurrentMediaItemChanged(MediaController2ImplBase.this.mInstance, item);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void notifyPlayerStateChanges(long eventTimeMs, long positionMs, final int state) {
        synchronized (this.mLock) {
            this.mPositionEventTimeMs = eventTimeMs;
            this.mPositionMs = positionMs;
            this.mPlayerState = state;
        }
        this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.4
            @Override // java.lang.Runnable
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onPlayerStateChanged(MediaController2ImplBase.this.mInstance, state);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void notifyPlaybackSpeedChanges(long eventTimeMs, long positionMs, final float speed) {
        synchronized (this.mLock) {
            this.mPositionEventTimeMs = eventTimeMs;
            this.mPositionMs = positionMs;
            this.mPlaybackSpeed = speed;
        }
        this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.5
            @Override // java.lang.Runnable
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onPlaybackSpeedChanged(MediaController2ImplBase.this.mInstance, speed);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void notifyBufferingStateChanged(final MediaItem2 item, final int state, long bufferedPositionMs) {
        synchronized (this.mLock) {
            this.mBufferingState = state;
            this.mBufferedPositionMs = bufferedPositionMs;
        }
        this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.6
            @Override // java.lang.Runnable
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onBufferingStateChanged(MediaController2ImplBase.this.mInstance, item, state);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void notifyPlaylistChanges(final List<MediaItem2> playlist, final MediaMetadata2 metadata) {
        synchronized (this.mLock) {
            this.mPlaylist = playlist;
            this.mPlaylistMetadata = metadata;
        }
        this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.7
            @Override // java.lang.Runnable
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onPlaylistChanged(MediaController2ImplBase.this.mInstance, playlist, metadata);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void notifyPlaylistMetadataChanges(final MediaMetadata2 metadata) {
        synchronized (this.mLock) {
            this.mPlaylistMetadata = metadata;
        }
        this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.8
            @Override // java.lang.Runnable
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onPlaylistMetadataChanged(MediaController2ImplBase.this.mInstance, metadata);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void notifyPlaybackInfoChanges(final MediaController2.PlaybackInfo info) {
        synchronized (this.mLock) {
            this.mPlaybackInfo = info;
        }
        this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.9
            @Override // java.lang.Runnable
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onPlaybackInfoChanged(MediaController2ImplBase.this.mInstance, info);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void notifyRepeatModeChanges(final int repeatMode) {
        synchronized (this.mLock) {
            this.mRepeatMode = repeatMode;
        }
        this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.10
            @Override // java.lang.Runnable
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onRepeatModeChanged(MediaController2ImplBase.this.mInstance, repeatMode);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void notifyShuffleModeChanges(final int shuffleMode) {
        synchronized (this.mLock) {
            this.mShuffleMode = shuffleMode;
        }
        this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.11
            @Override // java.lang.Runnable
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onShuffleModeChanged(MediaController2ImplBase.this.mInstance, shuffleMode);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void notifySeekCompleted(long eventTimeMs, long positionMs, final long seekPositionMs) {
        synchronized (this.mLock) {
            this.mPositionEventTimeMs = eventTimeMs;
            this.mPositionMs = positionMs;
        }
        this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.12
            @Override // java.lang.Runnable
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onSeekCompleted(MediaController2ImplBase.this.mInstance, seekPositionMs);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void notifyError(final int errorCode, final Bundle extras) {
        this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.13
            @Override // java.lang.Runnable
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onError(MediaController2ImplBase.this.mInstance, errorCode, extras);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void notifyRoutesInfoChanged(final List<Bundle> routes) {
        this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.14
            @Override // java.lang.Runnable
            public void run() {
                if (MediaController2ImplBase.this.mInstance.isConnected()) {
                    MediaController2ImplBase.this.mCallback.onRoutesInfoChanged(MediaController2ImplBase.this.mInstance, routes);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onConnectedNotLocked(IMediaSession2 sessionBinder, final SessionCommandGroup2 allowedCommands, int playerState, MediaItem2 currentMediaItem, long positionEventTimeMs, long positionMs, float playbackSpeed, long bufferedPositionMs, MediaController2.PlaybackInfo info, int repeatMode, int shuffleMode, List<MediaItem2> playlist, PendingIntent sessionActivity) {
        if (DEBUG) {
            Log.d(TAG, "onConnectedNotLocked sessionBinder=" + sessionBinder + ", allowedCommands=" + allowedCommands);
        }
        if (sessionBinder == null || allowedCommands == null) {
            if (1 != 0) {
                this.mInstance.close();
                return;
            }
            return;
        }
        try {
            try {
                try {
                    synchronized (this.mLock) {
                        try {
                            if (this.mIsReleased) {
                                if (0 != 0) {
                                    this.mInstance.close();
                                }
                            } else if (this.mISession2 != null) {
                                Log.e(TAG, "Cannot be notified about the connection result many times. Probably a bug or malicious app.");
                                if (1 != 0) {
                                    this.mInstance.close();
                                }
                            } else {
                                this.mAllowedCommands = allowedCommands;
                                this.mPlayerState = playerState;
                                try {
                                    this.mCurrentMediaItem = currentMediaItem;
                                    try {
                                        this.mPositionEventTimeMs = positionEventTimeMs;
                                        try {
                                            this.mPositionMs = positionMs;
                                        } catch (Throwable th) {
                                            e = th;
                                            throw e;
                                        }
                                    } catch (Throwable th2) {
                                        e = th2;
                                        throw e;
                                    }
                                } catch (Throwable th3) {
                                    e = th3;
                                    throw e;
                                }
                                try {
                                    this.mPlaybackSpeed = playbackSpeed;
                                    try {
                                        this.mBufferedPositionMs = bufferedPositionMs;
                                        this.mPlaybackInfo = info;
                                        this.mRepeatMode = repeatMode;
                                        this.mShuffleMode = shuffleMode;
                                        this.mPlaylist = playlist;
                                        this.mSessionActivity = sessionActivity;
                                        this.mISession2 = sessionBinder;
                                        try {
                                            this.mISession2.asBinder().linkToDeath(this.mDeathRecipient, 0);
                                            this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.15
                                                @Override // java.lang.Runnable
                                                public void run() {
                                                    MediaController2ImplBase.this.mCallback.onConnected(MediaController2ImplBase.this.mInstance, allowedCommands);
                                                }
                                            });
                                            if (0 != 0) {
                                                this.mInstance.close();
                                            }
                                        } catch (RemoteException e) {
                                            if (DEBUG) {
                                                Log.d(TAG, "Session died too early.", e);
                                            }
                                            try {
                                                if (1 != 0) {
                                                    this.mInstance.close();
                                                }
                                            } catch (Throwable th4) {
                                                e = th4;
                                                throw e;
                                            }
                                        }
                                    } catch (Throwable th5) {
                                        e = th5;
                                        throw e;
                                    }
                                } catch (Throwable th6) {
                                    e = th6;
                                    throw e;
                                }
                            }
                        } catch (Throwable th7) {
                            e = th7;
                        }
                    }
                } catch (Throwable th8) {
                    th = th8;
                    if (0 != 0) {
                        this.mInstance.close();
                    }
                    throw th;
                }
            } catch (Throwable th9) {
                e = th9;
            }
        } catch (Throwable th10) {
            th = th10;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onCustomCommand(final SessionCommand2 command, final Bundle args, final ResultReceiver receiver) {
        if (DEBUG) {
            Log.d(TAG, "onCustomCommand cmd=" + command);
        }
        this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.16
            @Override // java.lang.Runnable
            public void run() {
                MediaController2ImplBase.this.mCallback.onCustomCommand(MediaController2ImplBase.this.mInstance, command, args, receiver);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onAllowedCommandsChanged(final SessionCommandGroup2 commands) {
        this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.17
            @Override // java.lang.Runnable
            public void run() {
                MediaController2ImplBase.this.mCallback.onAllowedCommandsChanged(MediaController2ImplBase.this.mInstance, commands);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onCustomLayoutChanged(final List<MediaSession2.CommandButton> layout) {
        this.mCallbackExecutor.execute(new Runnable() { // from class: android.support.v4.media.MediaController2ImplBase.18
            @Override // java.lang.Runnable
            public void run() {
                MediaController2ImplBase.this.mCallback.onCustomLayoutChanged(MediaController2ImplBase.this.mInstance, layout);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SessionServiceConnection implements ServiceConnection {
        private SessionServiceConnection() {
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (MediaController2ImplBase.DEBUG) {
                Log.d(MediaController2ImplBase.TAG, "onServiceConnected " + name + " " + this);
            }
            if (!MediaController2ImplBase.this.mToken.getPackageName().equals(name.getPackageName())) {
                Log.wtf(MediaController2ImplBase.TAG, name + " was connected, but expected pkg=" + MediaController2ImplBase.this.mToken.getPackageName() + " with id=" + MediaController2ImplBase.this.mToken.getId());
                return;
            }
            MediaController2ImplBase.this.connectToSession(IMediaSession2.Stub.asInterface(service));
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            if (MediaController2ImplBase.DEBUG) {
                Log.w(MediaController2ImplBase.TAG, "Session service " + name + " is disconnected.");
            }
        }

        @Override // android.content.ServiceConnection
        public void onBindingDied(ComponentName name) {
            MediaController2ImplBase.this.close();
        }
    }
}
