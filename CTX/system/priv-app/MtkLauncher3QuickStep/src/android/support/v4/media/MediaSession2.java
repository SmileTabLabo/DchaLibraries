package android.support.v4.media;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.mediacompat.Rating2;
import android.support.v4.media.MediaController2;
import android.support.v4.media.MediaInterface2;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
@TargetApi(19)
/* loaded from: classes.dex */
public class MediaSession2 implements MediaInterface2.SessionPlayer, AutoCloseable {
    public static final int ERROR_CODE_ACTION_ABORTED = 10;
    public static final int ERROR_CODE_APP_ERROR = 1;
    public static final int ERROR_CODE_AUTHENTICATION_EXPIRED = 3;
    public static final int ERROR_CODE_CONCURRENT_STREAM_LIMIT = 5;
    public static final int ERROR_CODE_CONTENT_ALREADY_PLAYING = 8;
    public static final int ERROR_CODE_END_OF_QUEUE = 11;
    public static final int ERROR_CODE_NOT_AVAILABLE_IN_REGION = 7;
    public static final int ERROR_CODE_NOT_SUPPORTED = 2;
    public static final int ERROR_CODE_PARENTAL_CONTROL_RESTRICTED = 6;
    public static final int ERROR_CODE_PREMIUM_ACCOUNT_REQUIRED = 4;
    public static final int ERROR_CODE_SETUP_REQUIRED = 12;
    public static final int ERROR_CODE_SKIP_LIMIT_REACHED = 9;
    public static final int ERROR_CODE_UNKNOWN_ERROR = 0;
    static final String TAG = "MediaSession2";
    private final SupportLibraryImpl mImpl;

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    /* loaded from: classes.dex */
    public @interface ErrorCode {
    }

    /* loaded from: classes.dex */
    public interface OnDataSourceMissingHelper {
        @Nullable
        DataSourceDesc onDataSourceMissing(@NonNull MediaSession2 mediaSession2, @NonNull MediaItem2 mediaItem2);
    }

    /* loaded from: classes.dex */
    interface SupportLibraryImpl extends MediaInterface2.SessionPlayer, AutoCloseable {
        AudioFocusHandler getAudioFocusHandler();

        SessionCallback getCallback();

        Executor getCallbackExecutor();

        @NonNull
        List<ControllerInfo> getConnectedControllers();

        Context getContext();

        MediaSession2 getInstance();

        MediaController2.PlaybackInfo getPlaybackInfo();

        PlaybackStateCompat getPlaybackStateCompat();

        @NonNull
        BaseMediaPlayer getPlayer();

        @NonNull
        MediaPlaylistAgent getPlaylistAgent();

        PendingIntent getSessionActivity();

        IBinder getSessionBinder();

        MediaSessionCompat getSessionCompat();

        @NonNull
        SessionToken2 getToken();

        @Nullable
        VolumeProviderCompat getVolumeProvider();

        boolean isClosed();

        void notifyRoutesInfoChanged(@NonNull ControllerInfo controllerInfo, @Nullable List<Bundle> list);

        void sendCustomCommand(@NonNull ControllerInfo controllerInfo, @NonNull SessionCommand2 sessionCommand2, @Nullable Bundle bundle, @Nullable ResultReceiver resultReceiver);

        void sendCustomCommand(@NonNull SessionCommand2 sessionCommand2, @Nullable Bundle bundle);

        void setAllowedCommands(@NonNull ControllerInfo controllerInfo, @NonNull SessionCommandGroup2 sessionCommandGroup2);

        void setCustomLayout(@NonNull ControllerInfo controllerInfo, @NonNull List<CommandButton> list);

        void updatePlayer(@NonNull BaseMediaPlayer baseMediaPlayer, @Nullable MediaPlaylistAgent mediaPlaylistAgent, @Nullable VolumeProviderCompat volumeProviderCompat);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public MediaSession2(Context context, String id, BaseMediaPlayer player, MediaPlaylistAgent playlistAgent, VolumeProviderCompat volumeProvider, PendingIntent sessionActivity, Executor callbackExecutor, SessionCallback callback) {
        this.mImpl = createImpl(context, id, player, playlistAgent, volumeProvider, sessionActivity, callbackExecutor, callback);
    }

    SupportLibraryImpl createImpl(Context context, String id, BaseMediaPlayer player, MediaPlaylistAgent playlistAgent, VolumeProviderCompat volumeProvider, PendingIntent sessionActivity, Executor callbackExecutor, SessionCallback callback) {
        return new MediaSession2ImplBase(this, context, id, player, playlistAgent, volumeProvider, sessionActivity, callbackExecutor, callback);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public SupportLibraryImpl getImpl() {
        return this.mImpl;
    }

    public void updatePlayer(@NonNull BaseMediaPlayer player, @Nullable MediaPlaylistAgent playlistAgent, @Nullable VolumeProviderCompat volumeProvider) {
        this.mImpl.updatePlayer(player, playlistAgent, volumeProvider);
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        try {
            this.mImpl.close();
        } catch (Exception e) {
        }
    }

    @NonNull
    public BaseMediaPlayer getPlayer() {
        return this.mImpl.getPlayer();
    }

    @NonNull
    public MediaPlaylistAgent getPlaylistAgent() {
        return this.mImpl.getPlaylistAgent();
    }

    @Nullable
    public VolumeProviderCompat getVolumeProvider() {
        return this.mImpl.getVolumeProvider();
    }

    @NonNull
    public SessionToken2 getToken() {
        return this.mImpl.getToken();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @NonNull
    public Context getContext() {
        return this.mImpl.getContext();
    }

    @NonNull
    Executor getCallbackExecutor() {
        return this.mImpl.getCallbackExecutor();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @NonNull
    public SessionCallback getCallback() {
        return this.mImpl.getCallback();
    }

    @NonNull
    AudioFocusHandler getAudioFocusHandler() {
        return this.mImpl.getAudioFocusHandler();
    }

    @NonNull
    public List<ControllerInfo> getConnectedControllers() {
        return this.mImpl.getConnectedControllers();
    }

    public void setCustomLayout(@NonNull ControllerInfo controller, @NonNull List<CommandButton> layout) {
        this.mImpl.setCustomLayout(controller, layout);
    }

    public void setAllowedCommands(@NonNull ControllerInfo controller, @NonNull SessionCommandGroup2 commands) {
        this.mImpl.setAllowedCommands(controller, commands);
    }

    public void sendCustomCommand(@NonNull SessionCommand2 command, @Nullable Bundle args) {
        this.mImpl.sendCustomCommand(command, args);
    }

    public void sendCustomCommand(@NonNull ControllerInfo controller, @NonNull SessionCommand2 command, @Nullable Bundle args, @Nullable ResultReceiver receiver) {
        this.mImpl.sendCustomCommand(controller, command, args, receiver);
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public void play() {
        this.mImpl.play();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public void pause() {
        this.mImpl.pause();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public void reset() {
        this.mImpl.reset();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public void prepare() {
        this.mImpl.prepare();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public void seekTo(long pos) {
        this.mImpl.seekTo(pos);
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlayer
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public void skipForward() {
        this.mImpl.skipForward();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlayer
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public void skipBackward() {
        this.mImpl.skipBackward();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlayer
    public void notifyError(int errorCode, @Nullable Bundle extras) {
        this.mImpl.notifyError(errorCode, extras);
    }

    public void notifyRoutesInfoChanged(@NonNull ControllerInfo controller, @Nullable List<Bundle> routes) {
        this.mImpl.notifyRoutesInfoChanged(controller, routes);
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public int getPlayerState() {
        return this.mImpl.getPlayerState();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public long getCurrentPosition() {
        return this.mImpl.getCurrentPosition();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public long getDuration() {
        return this.mImpl.getDuration();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public long getBufferedPosition() {
        return this.mImpl.getBufferedPosition();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public int getBufferingState() {
        return this.mImpl.getBufferingState();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public float getPlaybackSpeed() {
        return this.mImpl.getPlaybackSpeed();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public void setPlaybackSpeed(float speed) {
        this.mImpl.setPlaybackSpeed(speed);
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void setOnDataSourceMissingHelper(@NonNull OnDataSourceMissingHelper helper) {
        this.mImpl.setOnDataSourceMissingHelper(helper);
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void clearOnDataSourceMissingHelper() {
        this.mImpl.clearOnDataSourceMissingHelper();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public List<MediaItem2> getPlaylist() {
        return this.mImpl.getPlaylist();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void setPlaylist(@NonNull List<MediaItem2> list, @Nullable MediaMetadata2 metadata) {
        this.mImpl.setPlaylist(list, metadata);
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void skipToPlaylistItem(@NonNull MediaItem2 item) {
        this.mImpl.skipToPlaylistItem(item);
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void skipToPreviousItem() {
        this.mImpl.skipToPreviousItem();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void skipToNextItem() {
        this.mImpl.skipToNextItem();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public MediaMetadata2 getPlaylistMetadata() {
        return this.mImpl.getPlaylistMetadata();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void addPlaylistItem(int index, @NonNull MediaItem2 item) {
        this.mImpl.addPlaylistItem(index, item);
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void removePlaylistItem(@NonNull MediaItem2 item) {
        this.mImpl.removePlaylistItem(item);
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void replacePlaylistItem(int index, @NonNull MediaItem2 item) {
        this.mImpl.replacePlaylistItem(index, item);
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public MediaItem2 getCurrentMediaItem() {
        return this.mImpl.getCurrentMediaItem();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void updatePlaylistMetadata(@Nullable MediaMetadata2 metadata) {
        this.mImpl.updatePlaylistMetadata(metadata);
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public int getRepeatMode() {
        return this.mImpl.getRepeatMode();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void setRepeatMode(int repeatMode) {
        this.mImpl.setRepeatMode(repeatMode);
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public int getShuffleMode() {
        return this.mImpl.getShuffleMode();
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void setShuffleMode(int shuffleMode) {
        this.mImpl.setShuffleMode(shuffleMode);
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public MediaSessionCompat getSessionCompat() {
        return this.mImpl.getSessionCompat();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public IBinder getSessionBinder() {
        return this.mImpl.getSessionBinder();
    }

    /* loaded from: classes.dex */
    public static abstract class SessionCallback {
        @Nullable
        public SessionCommandGroup2 onConnect(@NonNull MediaSession2 session, @NonNull ControllerInfo controller) {
            SessionCommandGroup2 commands = new SessionCommandGroup2();
            commands.addAllPredefinedCommands();
            return commands;
        }

        public void onDisconnected(@NonNull MediaSession2 session, @NonNull ControllerInfo controller) {
        }

        public boolean onCommandRequest(@NonNull MediaSession2 session, @NonNull ControllerInfo controller, @NonNull SessionCommand2 command) {
            return true;
        }

        public void onSetRating(@NonNull MediaSession2 session, @NonNull ControllerInfo controller, @NonNull String mediaId, @NonNull Rating2 rating) {
        }

        public void onCustomCommand(@NonNull MediaSession2 session, @NonNull ControllerInfo controller, @NonNull SessionCommand2 customCommand, @Nullable Bundle args, @Nullable ResultReceiver cb) {
        }

        public void onPlayFromMediaId(@NonNull MediaSession2 session, @NonNull ControllerInfo controller, @NonNull String mediaId, @Nullable Bundle extras) {
        }

        public void onPlayFromSearch(@NonNull MediaSession2 session, @NonNull ControllerInfo controller, @NonNull String query, @Nullable Bundle extras) {
        }

        public void onPlayFromUri(@NonNull MediaSession2 session, @NonNull ControllerInfo controller, @NonNull Uri uri, @Nullable Bundle extras) {
        }

        public void onPrepareFromMediaId(@NonNull MediaSession2 session, @NonNull ControllerInfo controller, @NonNull String mediaId, @Nullable Bundle extras) {
        }

        public void onPrepareFromSearch(@NonNull MediaSession2 session, @NonNull ControllerInfo controller, @NonNull String query, @Nullable Bundle extras) {
        }

        public void onPrepareFromUri(@NonNull MediaSession2 session, @NonNull ControllerInfo controller, @NonNull Uri uri, @Nullable Bundle extras) {
        }

        public void onFastForward(@NonNull MediaSession2 session, ControllerInfo controller) {
        }

        public void onRewind(@NonNull MediaSession2 session, ControllerInfo controller) {
        }

        public void onSubscribeRoutesInfo(@NonNull MediaSession2 session, @NonNull ControllerInfo controller) {
        }

        public void onUnsubscribeRoutesInfo(@NonNull MediaSession2 session, @NonNull ControllerInfo controller) {
        }

        public void onSelectRoute(@NonNull MediaSession2 session, @NonNull ControllerInfo controller, @NonNull Bundle route) {
        }

        public void onCurrentMediaItemChanged(@NonNull MediaSession2 session, @NonNull BaseMediaPlayer player, @Nullable MediaItem2 item) {
        }

        public void onMediaPrepared(@NonNull MediaSession2 session, @NonNull BaseMediaPlayer player, @NonNull MediaItem2 item) {
        }

        public void onPlayerStateChanged(@NonNull MediaSession2 session, @NonNull BaseMediaPlayer player, int state) {
        }

        public void onBufferingStateChanged(@NonNull MediaSession2 session, @NonNull BaseMediaPlayer player, @NonNull MediaItem2 item, int state) {
        }

        public void onPlaybackSpeedChanged(@NonNull MediaSession2 session, @NonNull BaseMediaPlayer player, float speed) {
        }

        public void onSeekCompleted(@NonNull MediaSession2 session, @NonNull BaseMediaPlayer player, long position) {
        }

        public void onPlaylistChanged(@NonNull MediaSession2 session, @NonNull MediaPlaylistAgent playlistAgent, @NonNull List<MediaItem2> list, @Nullable MediaMetadata2 metadata) {
        }

        public void onPlaylistMetadataChanged(@NonNull MediaSession2 session, @NonNull MediaPlaylistAgent playlistAgent, @Nullable MediaMetadata2 metadata) {
        }

        public void onShuffleModeChanged(@NonNull MediaSession2 session, @NonNull MediaPlaylistAgent playlistAgent, int shuffleMode) {
        }

        public void onRepeatModeChanged(@NonNull MediaSession2 session, @NonNull MediaPlaylistAgent playlistAgent, int repeatMode) {
        }
    }

    /* loaded from: classes.dex */
    public static final class Builder extends BuilderBase<MediaSession2, Builder, SessionCallback> {
        public Builder(Context context) {
            super(context);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.support.v4.media.MediaSession2.BuilderBase
        @NonNull
        public Builder setPlayer(@NonNull BaseMediaPlayer player) {
            return (Builder) super.setPlayer(player);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.support.v4.media.MediaSession2.BuilderBase
        @NonNull
        public Builder setPlaylistAgent(@NonNull MediaPlaylistAgent playlistAgent) {
            return (Builder) super.setPlaylistAgent(playlistAgent);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.support.v4.media.MediaSession2.BuilderBase
        @NonNull
        public Builder setVolumeProvider(@Nullable VolumeProviderCompat volumeProvider) {
            return (Builder) super.setVolumeProvider(volumeProvider);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.support.v4.media.MediaSession2.BuilderBase
        @NonNull
        public Builder setSessionActivity(@Nullable PendingIntent pi) {
            return (Builder) super.setSessionActivity(pi);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.support.v4.media.MediaSession2.BuilderBase
        @NonNull
        public Builder setId(@NonNull String id) {
            return (Builder) super.setId(id);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.support.v4.media.MediaSession2.BuilderBase
        @NonNull
        public Builder setSessionCallback(@NonNull Executor executor, @NonNull SessionCallback callback) {
            return (Builder) super.setSessionCallback(executor, (Executor) callback);
        }

        /* JADX WARN: Type inference failed for: r0v3, types: [android.support.v4.media.MediaSession2$Builder$1, C extends android.support.v4.media.MediaSession2$SessionCallback] */
        @Override // android.support.v4.media.MediaSession2.BuilderBase
        @NonNull
        public MediaSession2 build() {
            if (this.mCallbackExecutor == null) {
                this.mCallbackExecutor = new MainHandlerExecutor(this.mContext);
            }
            if (this.mCallback == 0) {
                this.mCallback = new SessionCallback() { // from class: android.support.v4.media.MediaSession2.Builder.1
                };
            }
            return new MediaSession2(this.mContext, this.mId, this.mPlayer, this.mPlaylistAgent, this.mVolumeProvider, this.mSessionActivity, this.mCallbackExecutor, this.mCallback);
        }
    }

    /* loaded from: classes.dex */
    public static final class ControllerInfo {
        private final ControllerCb mControllerCb;
        private final boolean mIsTrusted = false;
        private final String mPackageName;
        private final int mUid;

        /* JADX INFO: Access modifiers changed from: package-private */
        @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
        public ControllerInfo(@NonNull String packageName, int pid, int uid, @NonNull ControllerCb cb) {
            this.mUid = uid;
            this.mPackageName = packageName;
            this.mControllerCb = cb;
        }

        @NonNull
        public String getPackageName() {
            return this.mPackageName;
        }

        public int getUid() {
            return this.mUid;
        }

        @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
        public boolean isTrusted() {
            return this.mIsTrusted;
        }

        public int hashCode() {
            return this.mControllerCb.hashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ControllerInfo)) {
                return false;
            }
            ControllerInfo other = (ControllerInfo) obj;
            return this.mControllerCb.equals(other.mControllerCb);
        }

        public String toString() {
            return "ControllerInfo {pkg=" + this.mPackageName + ", uid=" + this.mUid + "})";
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @NonNull
        public IBinder getId() {
            return this.mControllerCb.getId();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @NonNull
        public ControllerCb getControllerCb() {
            return this.mControllerCb;
        }
    }

    /* loaded from: classes.dex */
    public static final class CommandButton {
        private static final String KEY_COMMAND = "android.media.media_session2.command_button.command";
        private static final String KEY_DISPLAY_NAME = "android.media.media_session2.command_button.display_name";
        private static final String KEY_ENABLED = "android.media.media_session2.command_button.enabled";
        private static final String KEY_EXTRAS = "android.media.media_session2.command_button.extras";
        private static final String KEY_ICON_RES_ID = "android.media.media_session2.command_button.icon_res_id";
        private SessionCommand2 mCommand;
        private String mDisplayName;
        private boolean mEnabled;
        private Bundle mExtras;
        private int mIconResId;

        private CommandButton(@Nullable SessionCommand2 command, int iconResId, @Nullable String displayName, Bundle extras, boolean enabled) {
            this.mCommand = command;
            this.mIconResId = iconResId;
            this.mDisplayName = displayName;
            this.mExtras = extras;
            this.mEnabled = enabled;
        }

        @Nullable
        public SessionCommand2 getCommand() {
            return this.mCommand;
        }

        public int getIconResId() {
            return this.mIconResId;
        }

        @Nullable
        public String getDisplayName() {
            return this.mDisplayName;
        }

        @Nullable
        public Bundle getExtras() {
            return this.mExtras;
        }

        public boolean isEnabled() {
            return this.mEnabled;
        }

        @NonNull
        @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            bundle.putBundle(KEY_COMMAND, this.mCommand.toBundle());
            bundle.putInt(KEY_ICON_RES_ID, this.mIconResId);
            bundle.putString(KEY_DISPLAY_NAME, this.mDisplayName);
            bundle.putBundle(KEY_EXTRAS, this.mExtras);
            bundle.putBoolean(KEY_ENABLED, this.mEnabled);
            return bundle;
        }

        @Nullable
        @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
        public static CommandButton fromBundle(Bundle bundle) {
            if (bundle == null) {
                return null;
            }
            Builder builder = new Builder();
            builder.setCommand(SessionCommand2.fromBundle(bundle.getBundle(KEY_COMMAND)));
            builder.setIconResId(bundle.getInt(KEY_ICON_RES_ID, 0));
            builder.setDisplayName(bundle.getString(KEY_DISPLAY_NAME));
            builder.setExtras(bundle.getBundle(KEY_EXTRAS));
            builder.setEnabled(bundle.getBoolean(KEY_ENABLED));
            try {
                return builder.build();
            } catch (IllegalStateException e) {
                return null;
            }
        }

        /* loaded from: classes.dex */
        public static final class Builder {
            private SessionCommand2 mCommand;
            private String mDisplayName;
            private boolean mEnabled;
            private Bundle mExtras;
            private int mIconResId;

            @NonNull
            public Builder setCommand(@Nullable SessionCommand2 command) {
                this.mCommand = command;
                return this;
            }

            @NonNull
            public Builder setIconResId(int resId) {
                this.mIconResId = resId;
                return this;
            }

            @NonNull
            public Builder setDisplayName(@Nullable String displayName) {
                this.mDisplayName = displayName;
                return this;
            }

            @NonNull
            public Builder setEnabled(boolean enabled) {
                this.mEnabled = enabled;
                return this;
            }

            @NonNull
            public Builder setExtras(@Nullable Bundle extras) {
                this.mExtras = extras;
                return this;
            }

            @NonNull
            public CommandButton build() {
                return new CommandButton(this.mCommand, this.mIconResId, this.mDisplayName, this.mExtras, this.mEnabled);
            }
        }
    }

    /* loaded from: classes.dex */
    static abstract class ControllerCb {
        @NonNull
        abstract IBinder getId();

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onAllowedCommandsChanged(@NonNull SessionCommandGroup2 sessionCommandGroup2) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onBufferingStateChanged(@NonNull MediaItem2 mediaItem2, int i, long j) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onChildrenChanged(@NonNull String str, int i, @Nullable Bundle bundle) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onCurrentMediaItemChanged(@Nullable MediaItem2 mediaItem2) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onCustomCommand(@NonNull SessionCommand2 sessionCommand2, @Nullable Bundle bundle, @Nullable ResultReceiver resultReceiver) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onCustomLayoutChanged(@NonNull List<CommandButton> list) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onDisconnected() throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onError(int i, @Nullable Bundle bundle) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onGetChildrenDone(@NonNull String str, int i, int i2, @Nullable List<MediaItem2> list, @Nullable Bundle bundle) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onGetItemDone(@NonNull String str, @Nullable MediaItem2 mediaItem2) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onGetLibraryRootDone(@Nullable Bundle bundle, @Nullable String str, @Nullable Bundle bundle2) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onGetSearchResultDone(@NonNull String str, int i, int i2, @Nullable List<MediaItem2> list, @Nullable Bundle bundle) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onPlaybackInfoChanged(@NonNull MediaController2.PlaybackInfo playbackInfo) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onPlaybackSpeedChanged(long j, long j2, float f) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onPlayerStateChanged(long j, long j2, int i) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onPlaylistChanged(@NonNull List<MediaItem2> list, @Nullable MediaMetadata2 mediaMetadata2) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onPlaylistMetadataChanged(@Nullable MediaMetadata2 mediaMetadata2) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onRepeatModeChanged(int i) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onRoutesInfoChanged(@Nullable List<Bundle> list) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onSearchResultChanged(@NonNull String str, int i, @Nullable Bundle bundle) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onSeekCompleted(long j, long j2, long j3) throws RemoteException;

        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract void onShuffleModeChanged(int i) throws RemoteException;

        public int hashCode() {
            return getId().hashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ControllerCb)) {
                return false;
            }
            ControllerCb other = (ControllerCb) obj;
            return getId().equals(other.getId());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    /* loaded from: classes.dex */
    public static abstract class BuilderBase<T extends MediaSession2, U extends BuilderBase<T, U, C>, C extends SessionCallback> {
        C mCallback;
        Executor mCallbackExecutor;
        final Context mContext;
        String mId;
        BaseMediaPlayer mPlayer;
        MediaPlaylistAgent mPlaylistAgent;
        PendingIntent mSessionActivity;
        VolumeProviderCompat mVolumeProvider;

        @NonNull
        abstract T build();

        /* JADX INFO: Access modifiers changed from: package-private */
        public BuilderBase(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("context shouldn't be null");
            }
            this.mContext = context;
            this.mId = MediaSession2.TAG;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @NonNull
        public U setPlayer(@NonNull BaseMediaPlayer player) {
            if (player == null) {
                throw new IllegalArgumentException("player shouldn't be null");
            }
            this.mPlayer = player;
            return this;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public U setPlaylistAgent(@NonNull MediaPlaylistAgent playlistAgent) {
            if (playlistAgent == null) {
                throw new IllegalArgumentException("playlistAgent shouldn't be null");
            }
            this.mPlaylistAgent = playlistAgent;
            return this;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @NonNull
        public U setVolumeProvider(@Nullable VolumeProviderCompat volumeProvider) {
            this.mVolumeProvider = volumeProvider;
            return this;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @NonNull
        public U setSessionActivity(@Nullable PendingIntent pi) {
            this.mSessionActivity = pi;
            return this;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @NonNull
        public U setId(@NonNull String id) {
            if (id == null) {
                throw new IllegalArgumentException("id shouldn't be null");
            }
            this.mId = id;
            return this;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @NonNull
        public U setSessionCallback(@NonNull Executor executor, @NonNull C callback) {
            if (executor == null) {
                throw new IllegalArgumentException("executor shouldn't be null");
            }
            if (callback == null) {
                throw new IllegalArgumentException("callback shouldn't be null");
            }
            this.mCallbackExecutor = executor;
            this.mCallback = callback;
            return this;
        }
    }

    /* loaded from: classes.dex */
    static class MainHandlerExecutor implements Executor {
        private final Handler mHandler;

        /* JADX INFO: Access modifiers changed from: package-private */
        public MainHandlerExecutor(Context context) {
            this.mHandler = new Handler(context.getMainLooper());
        }

        @Override // java.util.concurrent.Executor
        public void execute(Runnable command) {
            if (!this.mHandler.post(command)) {
                throw new RejectedExecutionException(this.mHandler + " is shutting down");
            }
        }
    }
}
