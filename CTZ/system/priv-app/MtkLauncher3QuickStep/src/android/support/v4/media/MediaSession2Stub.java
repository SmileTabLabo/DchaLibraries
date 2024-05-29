package android.support.v4.media;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Binder;
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
import android.support.v4.media.MediaLibraryService2;
import android.support.v4.media.MediaSession2;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/* JADX INFO: Access modifiers changed from: package-private */
@TargetApi(19)
/* loaded from: classes.dex */
public class MediaSession2Stub extends IMediaSession2.Stub {
    private static final boolean DEBUG = true;
    private static final String TAG = "MediaSession2Stub";
    private static final SparseArray<SessionCommand2> sCommandsForOnCommandRequest = new SparseArray<>();
    final Context mContext;
    final MediaSession2.SupportLibraryImpl mSession;
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private final ArrayMap<IBinder, MediaSession2.ControllerInfo> mControllers = new ArrayMap<>();
    @GuardedBy("mLock")
    private final Set<IBinder> mConnectingControllers = new HashSet();
    @GuardedBy("mLock")
    private final ArrayMap<MediaSession2.ControllerInfo, SessionCommandGroup2> mAllowedCommandGroupMap = new ArrayMap<>();

    /* JADX INFO: Access modifiers changed from: private */
    @FunctionalInterface
    /* loaded from: classes.dex */
    public interface SessionRunnable {
        void run(MediaSession2.ControllerInfo controllerInfo) throws RemoteException;
    }

    static {
        SessionCommandGroup2 group = new SessionCommandGroup2();
        group.addAllPlaybackCommands();
        group.addAllPlaylistCommands();
        group.addAllVolumeCommands();
        Set<SessionCommand2> commands = group.getCommands();
        for (SessionCommand2 command : commands) {
            sCommandsForOnCommandRequest.append(command.getCommandCode(), command);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public MediaSession2Stub(MediaSession2.SupportLibraryImpl session) {
        this.mSession = session;
        this.mContext = this.mSession.getContext();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public List<MediaSession2.ControllerInfo> getConnectedControllers() {
        ArrayList<MediaSession2.ControllerInfo> controllers = new ArrayList<>();
        synchronized (this.mLock) {
            for (int i = 0; i < this.mControllers.size(); i++) {
                controllers.add(this.mControllers.valueAt(i));
            }
        }
        return controllers;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setAllowedCommands(MediaSession2.ControllerInfo controller, SessionCommandGroup2 commands) {
        synchronized (this.mLock) {
            this.mAllowedCommandGroupMap.put(controller, commands);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isAllowedCommand(MediaSession2.ControllerInfo controller, SessionCommand2 command) {
        SessionCommandGroup2 allowedCommands;
        synchronized (this.mLock) {
            allowedCommands = this.mAllowedCommandGroupMap.get(controller);
        }
        return allowedCommands != null && allowedCommands.hasCommand(command);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isAllowedCommand(MediaSession2.ControllerInfo controller, int commandCode) {
        SessionCommandGroup2 allowedCommands;
        synchronized (this.mLock) {
            allowedCommands = this.mAllowedCommandGroupMap.get(controller);
        }
        return allowedCommands != null && allowedCommands.hasCommand(commandCode);
    }

    private void onSessionCommand(@NonNull IMediaController2 caller, int commandCode, @NonNull SessionRunnable runnable) {
        onSessionCommandInternal(caller, null, commandCode, runnable);
    }

    private void onSessionCommand(@NonNull IMediaController2 caller, @NonNull SessionCommand2 sessionCommand, @NonNull SessionRunnable runnable) {
        onSessionCommandInternal(caller, sessionCommand, 0, runnable);
    }

    private void onSessionCommandInternal(@NonNull IMediaController2 caller, @Nullable final SessionCommand2 sessionCommand, final int commandCode, @NonNull final SessionRunnable runnable) {
        MediaSession2.ControllerInfo controller;
        synchronized (this.mLock) {
            controller = caller != null ? this.mControllers.get(caller.asBinder()) : null;
        }
        if (this.mSession.isClosed() || controller == null) {
            return;
        }
        final MediaSession2.ControllerInfo controllerInfo = controller;
        this.mSession.getCallbackExecutor().execute(new Runnable() { // from class: android.support.v4.media.MediaSession2Stub.1
            @Override // java.lang.Runnable
            public void run() {
                SessionCommand2 command;
                synchronized (MediaSession2Stub.this.mLock) {
                    if (MediaSession2Stub.this.mControllers.containsValue(controllerInfo)) {
                        if (sessionCommand != null) {
                            if (MediaSession2Stub.this.isAllowedCommand(controllerInfo, sessionCommand)) {
                                command = (SessionCommand2) MediaSession2Stub.sCommandsForOnCommandRequest.get(sessionCommand.getCommandCode());
                            } else {
                                return;
                            }
                        } else if (MediaSession2Stub.this.isAllowedCommand(controllerInfo, commandCode)) {
                            command = (SessionCommand2) MediaSession2Stub.sCommandsForOnCommandRequest.get(commandCode);
                        } else {
                            return;
                        }
                        if (command != null) {
                            boolean accepted = MediaSession2Stub.this.mSession.getCallback().onCommandRequest(MediaSession2Stub.this.mSession.getInstance(), controllerInfo, command);
                            if (!accepted) {
                                Log.d(MediaSession2Stub.TAG, "Command (" + command + ") from " + controllerInfo + " was rejected by " + MediaSession2Stub.this.mSession);
                                return;
                            }
                        }
                        try {
                            runnable.run(controllerInfo);
                        } catch (RemoteException e) {
                            Log.w(MediaSession2Stub.TAG, "Exception in " + controllerInfo.toString(), e);
                        }
                    }
                }
            }
        });
    }

    private void onBrowserCommand(@NonNull IMediaController2 caller, int commandCode, @NonNull SessionRunnable runnable) {
        if (!(this.mSession instanceof MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl)) {
            throw new RuntimeException("MediaSession2 cannot handle MediaLibrarySession command");
        }
        onSessionCommandInternal(caller, null, commandCode, runnable);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeControllerInfo(MediaSession2.ControllerInfo controller) {
        synchronized (this.mLock) {
            MediaSession2.ControllerInfo controller2 = this.mControllers.remove(controller.getId());
            Log.d(TAG, "releasing " + controller2);
        }
    }

    private void releaseController(IMediaController2 iController) {
        final MediaSession2.ControllerInfo controller;
        synchronized (this.mLock) {
            controller = this.mControllers.remove(iController.asBinder());
            Log.d(TAG, "releasing " + controller);
        }
        if (this.mSession.isClosed() || controller == null) {
            return;
        }
        this.mSession.getCallbackExecutor().execute(new Runnable() { // from class: android.support.v4.media.MediaSession2Stub.2
            @Override // java.lang.Runnable
            public void run() {
                MediaSession2Stub.this.mSession.getCallback().onDisconnected(MediaSession2Stub.this.mSession.getInstance(), controller);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void connect(final IMediaController2 caller, String callingPackage) throws RuntimeException {
        final MediaSession2.ControllerInfo controllerInfo = new MediaSession2.ControllerInfo(callingPackage, Binder.getCallingPid(), Binder.getCallingUid(), new Controller2Cb(caller));
        this.mSession.getCallbackExecutor().execute(new Runnable() { // from class: android.support.v4.media.MediaSession2Stub.3
            @Override // java.lang.Runnable
            public void run() {
                if (!MediaSession2Stub.this.mSession.isClosed()) {
                    synchronized (MediaSession2Stub.this.mLock) {
                        MediaSession2Stub.this.mConnectingControllers.add(controllerInfo.getId());
                    }
                    SessionCommandGroup2 allowedCommands = MediaSession2Stub.this.mSession.getCallback().onConnect(MediaSession2Stub.this.mSession.getInstance(), controllerInfo);
                    boolean accept = allowedCommands != null || controllerInfo.isTrusted();
                    if (!accept) {
                        synchronized (MediaSession2Stub.this.mLock) {
                            MediaSession2Stub.this.mConnectingControllers.remove(controllerInfo.getId());
                        }
                        Log.d(MediaSession2Stub.TAG, "Rejecting connection, controllerInfo=" + controllerInfo);
                        try {
                            caller.onDisconnected();
                        } catch (RemoteException e) {
                        }
                        return;
                    }
                    Log.d(MediaSession2Stub.TAG, "Accepting connection, controllerInfo=" + controllerInfo + " allowedCommands=" + allowedCommands);
                    if (allowedCommands == null) {
                        allowedCommands = new SessionCommandGroup2();
                    }
                    SessionCommandGroup2 allowedCommands2 = allowedCommands;
                    synchronized (MediaSession2Stub.this.mLock) {
                        MediaSession2Stub.this.mConnectingControllers.remove(controllerInfo.getId());
                        MediaSession2Stub.this.mControllers.put(controllerInfo.getId(), controllerInfo);
                        MediaSession2Stub.this.mAllowedCommandGroupMap.put(controllerInfo, allowedCommands2);
                    }
                    int playerState = MediaSession2Stub.this.mSession.getPlayerState();
                    Bundle currentItem = MediaSession2Stub.this.mSession.getCurrentMediaItem() == null ? null : MediaSession2Stub.this.mSession.getCurrentMediaItem().toBundle();
                    long positionEventTimeMs = SystemClock.elapsedRealtime();
                    long positionMs = MediaSession2Stub.this.mSession.getCurrentPosition();
                    float playbackSpeed = MediaSession2Stub.this.mSession.getPlaybackSpeed();
                    long bufferedPositionMs = MediaSession2Stub.this.mSession.getBufferedPosition();
                    Bundle playbackInfoBundle = MediaSession2Stub.this.mSession.getPlaybackInfo().toBundle();
                    int repeatMode = MediaSession2Stub.this.mSession.getRepeatMode();
                    int shuffleMode = MediaSession2Stub.this.mSession.getShuffleMode();
                    PendingIntent sessionActivity = MediaSession2Stub.this.mSession.getSessionActivity();
                    List<MediaItem2> playlist = allowedCommands2.hasCommand(18) ? MediaSession2Stub.this.mSession.getPlaylist() : null;
                    List<Bundle> playlistBundle = MediaUtils2.convertMediaItem2ListToBundleList(playlist);
                    if (!MediaSession2Stub.this.mSession.isClosed()) {
                        try {
                            try {
                                caller.onConnected(MediaSession2Stub.this, allowedCommands2.toBundle(), playerState, currentItem, positionEventTimeMs, positionMs, playbackSpeed, bufferedPositionMs, playbackInfoBundle, repeatMode, shuffleMode, playlistBundle, sessionActivity);
                            } catch (RemoteException e2) {
                            }
                        } catch (RemoteException e3) {
                        }
                    }
                }
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void release(IMediaController2 caller) throws RemoteException {
        releaseController(caller);
    }

    @Override // android.support.v4.media.IMediaSession2
    public void setVolumeTo(IMediaController2 caller, final int value, final int flags) throws RuntimeException {
        onSessionCommand(caller, 10, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.4
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                VolumeProviderCompat volumeProvider = MediaSession2Stub.this.mSession.getVolumeProvider();
                if (volumeProvider == null) {
                    MediaSessionCompat sessionCompat = MediaSession2Stub.this.mSession.getSessionCompat();
                    if (sessionCompat != null) {
                        sessionCompat.getController().setVolumeTo(value, flags);
                        return;
                    }
                    return;
                }
                volumeProvider.onSetVolumeTo(value);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void adjustVolume(IMediaController2 caller, final int direction, final int flags) throws RuntimeException {
        onSessionCommand(caller, 11, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.5
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                VolumeProviderCompat volumeProvider = MediaSession2Stub.this.mSession.getVolumeProvider();
                if (volumeProvider == null) {
                    MediaSessionCompat sessionCompat = MediaSession2Stub.this.mSession.getSessionCompat();
                    if (sessionCompat != null) {
                        sessionCompat.getController().adjustVolume(direction, flags);
                        return;
                    }
                    return;
                }
                volumeProvider.onAdjustVolume(direction);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void play(IMediaController2 caller) throws RuntimeException {
        onSessionCommand(caller, 1, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.6
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.play();
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void pause(IMediaController2 caller) throws RuntimeException {
        onSessionCommand(caller, 2, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.7
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.pause();
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void reset(IMediaController2 caller) throws RuntimeException {
        onSessionCommand(caller, 3, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.8
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.reset();
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void prepare(IMediaController2 caller) throws RuntimeException {
        onSessionCommand(caller, 6, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.9
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.prepare();
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void fastForward(IMediaController2 caller) {
        onSessionCommand(caller, 7, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.10
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getCallback().onFastForward(MediaSession2Stub.this.mSession.getInstance(), controller);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void rewind(IMediaController2 caller) {
        onSessionCommand(caller, 8, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.11
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getCallback().onRewind(MediaSession2Stub.this.mSession.getInstance(), controller);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void seekTo(IMediaController2 caller, final long pos) throws RuntimeException {
        onSessionCommand(caller, 9, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.12
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.seekTo(pos);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void sendCustomCommand(IMediaController2 caller, Bundle commandBundle, final Bundle args, final ResultReceiver receiver) {
        final SessionCommand2 command = SessionCommand2.fromBundle(commandBundle);
        onSessionCommand(caller, SessionCommand2.fromBundle(commandBundle), new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.13
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getCallback().onCustomCommand(MediaSession2Stub.this.mSession.getInstance(), controller, command, args, receiver);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void prepareFromUri(IMediaController2 caller, final Uri uri, final Bundle extras) {
        onSessionCommand(caller, 26, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.14
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (uri == null) {
                    Log.w(MediaSession2Stub.TAG, "prepareFromUri(): Ignoring null uri from " + controller);
                    return;
                }
                MediaSession2Stub.this.mSession.getCallback().onPrepareFromUri(MediaSession2Stub.this.mSession.getInstance(), controller, uri, extras);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void prepareFromSearch(IMediaController2 caller, final String query, final Bundle extras) {
        onSessionCommand(caller, 27, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.15
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (TextUtils.isEmpty(query)) {
                    Log.w(MediaSession2Stub.TAG, "prepareFromSearch(): Ignoring empty query from " + controller);
                    return;
                }
                MediaSession2Stub.this.mSession.getCallback().onPrepareFromSearch(MediaSession2Stub.this.mSession.getInstance(), controller, query, extras);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void prepareFromMediaId(IMediaController2 caller, final String mediaId, final Bundle extras) {
        onSessionCommand(caller, 25, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.16
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (mediaId == null) {
                    Log.w(MediaSession2Stub.TAG, "prepareFromMediaId(): Ignoring null mediaId from " + controller);
                    return;
                }
                MediaSession2Stub.this.mSession.getCallback().onPrepareFromMediaId(MediaSession2Stub.this.mSession.getInstance(), controller, mediaId, extras);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void playFromUri(IMediaController2 caller, final Uri uri, final Bundle extras) {
        onSessionCommand(caller, 23, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.17
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (uri == null) {
                    Log.w(MediaSession2Stub.TAG, "playFromUri(): Ignoring null uri from " + controller);
                    return;
                }
                MediaSession2Stub.this.mSession.getCallback().onPlayFromUri(MediaSession2Stub.this.mSession.getInstance(), controller, uri, extras);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void playFromSearch(IMediaController2 caller, final String query, final Bundle extras) {
        onSessionCommand(caller, 24, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.18
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (TextUtils.isEmpty(query)) {
                    Log.w(MediaSession2Stub.TAG, "playFromSearch(): Ignoring empty query from " + controller);
                    return;
                }
                MediaSession2Stub.this.mSession.getCallback().onPlayFromSearch(MediaSession2Stub.this.mSession.getInstance(), controller, query, extras);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void playFromMediaId(IMediaController2 caller, final String mediaId, final Bundle extras) {
        onSessionCommand(caller, 22, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.19
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (mediaId == null) {
                    Log.w(MediaSession2Stub.TAG, "playFromMediaId(): Ignoring null mediaId from " + controller);
                    return;
                }
                MediaSession2Stub.this.mSession.getCallback().onPlayFromMediaId(MediaSession2Stub.this.mSession.getInstance(), controller, mediaId, extras);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void setRating(IMediaController2 caller, final String mediaId, final Bundle ratingBundle) {
        onSessionCommand(caller, 28, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.20
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (mediaId == null) {
                    Log.w(MediaSession2Stub.TAG, "setRating(): Ignoring null mediaId from " + controller);
                } else if (ratingBundle == null) {
                    Log.w(MediaSession2Stub.TAG, "setRating(): Ignoring null ratingBundle from " + controller);
                } else {
                    Rating2 rating = Rating2.fromBundle(ratingBundle);
                    if (rating == null) {
                        if (ratingBundle == null) {
                            Log.w(MediaSession2Stub.TAG, "setRating(): Ignoring null rating from " + controller);
                            return;
                        }
                        return;
                    }
                    MediaSession2Stub.this.mSession.getCallback().onSetRating(MediaSession2Stub.this.mSession.getInstance(), controller, mediaId, rating);
                }
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void setPlaybackSpeed(IMediaController2 caller, final float speed) {
        onSessionCommand(caller, 39, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.21
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().setPlaybackSpeed(speed);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void setPlaylist(IMediaController2 caller, final List<Bundle> playlist, final Bundle metadata) {
        onSessionCommand(caller, 19, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.22
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (playlist == null) {
                    Log.w(MediaSession2Stub.TAG, "setPlaylist(): Ignoring null playlist from " + controller);
                    return;
                }
                MediaSession2Stub.this.mSession.getInstance().setPlaylist(MediaUtils2.convertBundleListToMediaItem2List(playlist), MediaMetadata2.fromBundle(metadata));
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void updatePlaylistMetadata(IMediaController2 caller, final Bundle metadata) {
        onSessionCommand(caller, 21, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.23
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().updatePlaylistMetadata(MediaMetadata2.fromBundle(metadata));
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void addPlaylistItem(IMediaController2 caller, final int index, final Bundle mediaItem) {
        onSessionCommand(caller, 15, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.24
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().addPlaylistItem(index, MediaItem2.fromBundle(mediaItem, null));
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void removePlaylistItem(IMediaController2 caller, final Bundle mediaItem) {
        onSessionCommand(caller, 16, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.25
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaItem2 item = MediaItem2.fromBundle(mediaItem);
                MediaSession2Stub.this.mSession.getInstance().removePlaylistItem(item);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void replacePlaylistItem(IMediaController2 caller, final int index, final Bundle mediaItem) {
        onSessionCommand(caller, 17, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.26
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().replacePlaylistItem(index, MediaItem2.fromBundle(mediaItem, null));
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void skipToPlaylistItem(IMediaController2 caller, final Bundle mediaItem) {
        onSessionCommand(caller, 12, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.27
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (mediaItem == null) {
                    Log.w(MediaSession2Stub.TAG, "skipToPlaylistItem(): Ignoring null mediaItem from " + controller);
                }
                MediaSession2Stub.this.mSession.getInstance().skipToPlaylistItem(MediaItem2.fromBundle(mediaItem));
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void skipToPreviousItem(IMediaController2 caller) {
        onSessionCommand(caller, 5, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.28
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().skipToPreviousItem();
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void skipToNextItem(IMediaController2 caller) {
        onSessionCommand(caller, 4, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.29
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().skipToNextItem();
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void setRepeatMode(IMediaController2 caller, final int repeatMode) {
        onSessionCommand(caller, 14, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.30
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().setRepeatMode(repeatMode);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void setShuffleMode(IMediaController2 caller, final int shuffleMode) {
        onSessionCommand(caller, 13, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.31
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().setShuffleMode(shuffleMode);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void subscribeRoutesInfo(IMediaController2 caller) {
        onSessionCommand(caller, 36, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.32
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getCallback().onSubscribeRoutesInfo(MediaSession2Stub.this.mSession.getInstance(), controller);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void unsubscribeRoutesInfo(IMediaController2 caller) {
        onSessionCommand(caller, 37, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.33
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getCallback().onUnsubscribeRoutesInfo(MediaSession2Stub.this.mSession.getInstance(), controller);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void selectRoute(IMediaController2 caller, final Bundle route) {
        onSessionCommand(caller, 37, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.34
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getCallback().onSelectRoute(MediaSession2Stub.this.mSession.getInstance(), controller, route);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl getLibrarySession() {
        if (!(this.mSession instanceof MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl)) {
            throw new RuntimeException("Session cannot be casted to library session");
        }
        return (MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl) this.mSession;
    }

    @Override // android.support.v4.media.IMediaSession2
    public void getLibraryRoot(IMediaController2 caller, final Bundle rootHints) throws RuntimeException {
        onBrowserCommand(caller, 31, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.35
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.getLibrarySession().onGetLibraryRootOnExecutor(controller, rootHints);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void getItem(IMediaController2 caller, final String mediaId) throws RuntimeException {
        onBrowserCommand(caller, 30, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.36
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (mediaId != null) {
                    MediaSession2Stub.this.getLibrarySession().onGetItemOnExecutor(controller, mediaId);
                    return;
                }
                Log.w(MediaSession2Stub.TAG, "getItem(): Ignoring null mediaId from " + controller);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void getChildren(IMediaController2 caller, final String parentId, final int page, final int pageSize, final Bundle extras) throws RuntimeException {
        onBrowserCommand(caller, 29, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.37
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (parentId == null) {
                    Log.w(MediaSession2Stub.TAG, "getChildren(): Ignoring null parentId from " + controller);
                } else if (page >= 1 && pageSize >= 1) {
                    MediaSession2Stub.this.getLibrarySession().onGetChildrenOnExecutor(controller, parentId, page, pageSize, extras);
                } else {
                    Log.w(MediaSession2Stub.TAG, "getChildren(): Ignoring page nor pageSize less than 1 from " + controller);
                }
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void search(IMediaController2 caller, final String query, final Bundle extras) {
        onBrowserCommand(caller, 33, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.38
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (!TextUtils.isEmpty(query)) {
                    MediaSession2Stub.this.getLibrarySession().onSearchOnExecutor(controller, query, extras);
                    return;
                }
                Log.w(MediaSession2Stub.TAG, "search(): Ignoring empty query from " + controller);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void getSearchResult(IMediaController2 caller, final String query, final int page, final int pageSize, final Bundle extras) {
        onBrowserCommand(caller, 32, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.39
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (TextUtils.isEmpty(query)) {
                    Log.w(MediaSession2Stub.TAG, "getSearchResult(): Ignoring empty query from " + controller);
                } else if (page >= 1 && pageSize >= 1) {
                    MediaSession2Stub.this.getLibrarySession().onGetSearchResultOnExecutor(controller, query, page, pageSize, extras);
                } else {
                    Log.w(MediaSession2Stub.TAG, "getSearchResult(): Ignoring page nor pageSize less than 1  from " + controller);
                }
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void subscribe(IMediaController2 caller, final String parentId, final Bundle option) {
        onBrowserCommand(caller, 34, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.40
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (parentId != null) {
                    MediaSession2Stub.this.getLibrarySession().onSubscribeOnExecutor(controller, parentId, option);
                    return;
                }
                Log.w(MediaSession2Stub.TAG, "subscribe(): Ignoring null parentId from " + controller);
            }
        });
    }

    @Override // android.support.v4.media.IMediaSession2
    public void unsubscribe(IMediaController2 caller, final String parentId) {
        onBrowserCommand(caller, 35, new SessionRunnable() { // from class: android.support.v4.media.MediaSession2Stub.41
            @Override // android.support.v4.media.MediaSession2Stub.SessionRunnable
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (parentId != null) {
                    MediaSession2Stub.this.getLibrarySession().onUnsubscribeOnExecutor(controller, parentId);
                    return;
                }
                Log.w(MediaSession2Stub.TAG, "unsubscribe(): Ignoring null parentId from " + controller);
            }
        });
    }

    /* loaded from: classes.dex */
    static final class Controller2Cb extends MediaSession2.ControllerCb {
        private final IMediaController2 mIControllerCallback;

        Controller2Cb(@NonNull IMediaController2 callback) {
            this.mIControllerCallback = callback;
        }

        @Override // android.support.v4.media.MediaSession2.ControllerCb
        @NonNull
        IBinder getId() {
            return this.mIControllerCallback.asBinder();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onCustomLayoutChanged(List<MediaSession2.CommandButton> layout) throws RemoteException {
            this.mIControllerCallback.onCustomLayoutChanged(MediaUtils2.convertCommandButtonListToBundleList(layout));
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onPlaybackInfoChanged(MediaController2.PlaybackInfo info) throws RemoteException {
            this.mIControllerCallback.onPlaybackInfoChanged(info.toBundle());
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onAllowedCommandsChanged(SessionCommandGroup2 commands) throws RemoteException {
            this.mIControllerCallback.onAllowedCommandsChanged(commands.toBundle());
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onCustomCommand(SessionCommand2 command, Bundle args, ResultReceiver receiver) throws RemoteException {
            this.mIControllerCallback.onCustomCommand(command.toBundle(), args, receiver);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onPlayerStateChanged(long eventTimeMs, long positionMs, int playerState) throws RemoteException {
            this.mIControllerCallback.onPlayerStateChanged(eventTimeMs, positionMs, playerState);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onPlaybackSpeedChanged(long eventTimeMs, long positionMs, float speed) throws RemoteException {
            this.mIControllerCallback.onPlaybackSpeedChanged(eventTimeMs, positionMs, speed);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onBufferingStateChanged(MediaItem2 item, int state, long bufferedPositionMs) throws RemoteException {
            this.mIControllerCallback.onBufferingStateChanged(item == null ? null : item.toBundle(), state, bufferedPositionMs);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onSeekCompleted(long eventTimeMs, long positionMs, long seekPositionMs) throws RemoteException {
            this.mIControllerCallback.onSeekCompleted(eventTimeMs, positionMs, seekPositionMs);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onError(int errorCode, Bundle extras) throws RemoteException {
            this.mIControllerCallback.onError(errorCode, extras);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onCurrentMediaItemChanged(MediaItem2 item) throws RemoteException {
            this.mIControllerCallback.onCurrentMediaItemChanged(item == null ? null : item.toBundle());
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onPlaylistChanged(List<MediaItem2> playlist, MediaMetadata2 metadata) throws RemoteException {
            this.mIControllerCallback.onPlaylistChanged(MediaUtils2.convertMediaItem2ListToBundleList(playlist), metadata == null ? null : metadata.toBundle());
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onPlaylistMetadataChanged(MediaMetadata2 metadata) throws RemoteException {
            this.mIControllerCallback.onPlaylistMetadataChanged(metadata.toBundle());
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onShuffleModeChanged(int shuffleMode) throws RemoteException {
            this.mIControllerCallback.onShuffleModeChanged(shuffleMode);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onRepeatModeChanged(int repeatMode) throws RemoteException {
            this.mIControllerCallback.onRepeatModeChanged(repeatMode);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onRoutesInfoChanged(List<Bundle> routes) throws RemoteException {
            this.mIControllerCallback.onRoutesInfoChanged(routes);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onGetLibraryRootDone(Bundle rootHints, String rootMediaId, Bundle rootExtra) throws RemoteException {
            this.mIControllerCallback.onGetLibraryRootDone(rootHints, rootMediaId, rootExtra);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onChildrenChanged(String parentId, int itemCount, Bundle extras) throws RemoteException {
            this.mIControllerCallback.onChildrenChanged(parentId, itemCount, extras);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onGetChildrenDone(String parentId, int page, int pageSize, List<MediaItem2> result, Bundle extras) throws RemoteException {
            List<Bundle> bundleList = MediaUtils2.convertMediaItem2ListToBundleList(result);
            this.mIControllerCallback.onGetChildrenDone(parentId, page, pageSize, bundleList, extras);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onGetItemDone(String mediaId, MediaItem2 result) throws RemoteException {
            this.mIControllerCallback.onGetItemDone(mediaId, result == null ? null : result.toBundle());
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onSearchResultChanged(String query, int itemCount, Bundle extras) throws RemoteException {
            this.mIControllerCallback.onSearchResultChanged(query, itemCount, extras);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onGetSearchResultDone(String query, int page, int pageSize, List<MediaItem2> result, Bundle extras) throws RemoteException {
            List<Bundle> bundleList = MediaUtils2.convertMediaItem2ListToBundleList(result);
            this.mIControllerCallback.onGetSearchResultDone(query, page, pageSize, bundleList, extras);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onDisconnected() throws RemoteException {
            this.mIControllerCallback.onDisconnected();
        }
    }
}
