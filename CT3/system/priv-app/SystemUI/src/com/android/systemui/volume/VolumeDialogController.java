package com.android.systemui.volume;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.IVolumeController;
import android.media.VolumePolicy;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.notification.Condition;
import android.util.Log;
import android.util.SparseArray;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.volume.MediaSessions;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
/* loaded from: a.zip:com/android/systemui/volume/VolumeDialogController.class */
public class VolumeDialogController {
    private final AudioManager mAudio;
    private final ComponentName mComponent;
    private final Context mContext;
    private boolean mDestroyed;
    private boolean mEnabled;
    private final boolean mHasVibrator;
    private final MediaSessions mMediaSessions;
    private final NotificationManager mNoMan;
    private final SettingObserver mObserver;
    private final String[] mStreamTitles;
    private final Vibrator mVibrator;
    private VolumePolicy mVolumePolicy;
    private final W mWorker;
    private final HandlerThread mWorkerThread;
    private static final String TAG = Util.logTag(VolumeDialogController.class);
    private static final int[] STREAMS = {4, 6, 8, 3, 5, 2, 1, 7, 9, 0};
    private final Receiver mReceiver = new Receiver(this, null);
    private final VC mVolumeController = new VC(this, null);
    private final C mCallbacks = new C(this, null);
    private final State mState = new State();
    private final MediaSessionsCallbacks mMediaSessionsCallbacksW = new MediaSessionsCallbacks(this, null);
    private boolean mShowDndTile = true;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialogController$C.class */
    public final class C implements Callbacks {
        private final HashMap<Callbacks, Handler> mCallbackMap;
        final VolumeDialogController this$0;

        private C(VolumeDialogController volumeDialogController) {
            this.this$0 = volumeDialogController;
            this.mCallbackMap = new HashMap<>();
        }

        /* synthetic */ C(VolumeDialogController volumeDialogController, C c) {
            this(volumeDialogController);
        }

        public void add(Callbacks callbacks, Handler handler) {
            if (callbacks == null || handler == null) {
                throw new IllegalArgumentException();
            }
            this.mCallbackMap.put(callbacks, handler);
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onConfigurationChanged() {
            Iterator<T> it = this.mCallbackMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ((Handler) entry.getValue()).post(new Runnable(this, entry) { // from class: com.android.systemui.volume.VolumeDialogController.C.5
                    final C this$1;
                    final Map.Entry val$entry;

                    {
                        this.this$1 = this;
                        this.val$entry = entry;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        ((Callbacks) this.val$entry.getKey()).onConfigurationChanged();
                    }
                });
            }
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onDismissRequested(int i) {
            Iterator<T> it = this.mCallbackMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ((Handler) entry.getValue()).post(new Runnable(this, entry, i) { // from class: com.android.systemui.volume.VolumeDialogController.C.2
                    final C this$1;
                    final Map.Entry val$entry;
                    final int val$reason;

                    {
                        this.this$1 = this;
                        this.val$entry = entry;
                        this.val$reason = i;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        ((Callbacks) this.val$entry.getKey()).onDismissRequested(this.val$reason);
                    }
                });
            }
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onLayoutDirectionChanged(int i) {
            Iterator<T> it = this.mCallbackMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ((Handler) entry.getValue()).post(new Runnable(this, entry, i) { // from class: com.android.systemui.volume.VolumeDialogController.C.4
                    final C this$1;
                    final Map.Entry val$entry;
                    final int val$layoutDirection;

                    {
                        this.this$1 = this;
                        this.val$entry = entry;
                        this.val$layoutDirection = i;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        ((Callbacks) this.val$entry.getKey()).onLayoutDirectionChanged(this.val$layoutDirection);
                    }
                });
            }
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onScreenOff() {
            Iterator<T> it = this.mCallbackMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ((Handler) entry.getValue()).post(new Runnable(this, entry) { // from class: com.android.systemui.volume.VolumeDialogController.C.8
                    final C this$1;
                    final Map.Entry val$entry;

                    {
                        this.this$1 = this;
                        this.val$entry = entry;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        ((Callbacks) this.val$entry.getKey()).onScreenOff();
                    }
                });
            }
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onShowRequested(int i) {
            Iterator<T> it = this.mCallbackMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ((Handler) entry.getValue()).post(new Runnable(this, entry, i) { // from class: com.android.systemui.volume.VolumeDialogController.C.1
                    final C this$1;
                    final Map.Entry val$entry;
                    final int val$reason;

                    {
                        this.this$1 = this;
                        this.val$entry = entry;
                        this.val$reason = i;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        ((Callbacks) this.val$entry.getKey()).onShowRequested(this.val$reason);
                    }
                });
            }
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onShowSafetyWarning(int i) {
            Iterator<T> it = this.mCallbackMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ((Handler) entry.getValue()).post(new Runnable(this, entry, i) { // from class: com.android.systemui.volume.VolumeDialogController.C.9
                    final C this$1;
                    final Map.Entry val$entry;
                    final int val$flags;

                    {
                        this.this$1 = this;
                        this.val$entry = entry;
                        this.val$flags = i;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        ((Callbacks) this.val$entry.getKey()).onShowSafetyWarning(this.val$flags);
                    }
                });
            }
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onShowSilentHint() {
            Iterator<T> it = this.mCallbackMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ((Handler) entry.getValue()).post(new Runnable(this, entry) { // from class: com.android.systemui.volume.VolumeDialogController.C.7
                    final C this$1;
                    final Map.Entry val$entry;

                    {
                        this.this$1 = this;
                        this.val$entry = entry;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        ((Callbacks) this.val$entry.getKey()).onShowSilentHint();
                    }
                });
            }
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onShowVibrateHint() {
            Iterator<T> it = this.mCallbackMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ((Handler) entry.getValue()).post(new Runnable(this, entry) { // from class: com.android.systemui.volume.VolumeDialogController.C.6
                    final C this$1;
                    final Map.Entry val$entry;

                    {
                        this.this$1 = this;
                        this.val$entry = entry;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        ((Callbacks) this.val$entry.getKey()).onShowVibrateHint();
                    }
                });
            }
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onStateChanged(State state) {
            long currentTimeMillis = System.currentTimeMillis();
            State copy = state.copy();
            Iterator<T> it = this.mCallbackMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ((Handler) entry.getValue()).post(new Runnable(this, entry, copy) { // from class: com.android.systemui.volume.VolumeDialogController.C.3
                    final C this$1;
                    final State val$copy;
                    final Map.Entry val$entry;

                    {
                        this.this$1 = this;
                        this.val$entry = entry;
                        this.val$copy = copy;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        ((Callbacks) this.val$entry.getKey()).onStateChanged(this.val$copy);
                    }
                });
            }
            Events.writeState(currentTimeMillis, copy);
        }
    }

    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialogController$Callbacks.class */
    public interface Callbacks {
        void onConfigurationChanged();

        void onDismissRequested(int i);

        void onLayoutDirectionChanged(int i);

        void onScreenOff();

        void onShowRequested(int i);

        void onShowSafetyWarning(int i);

        void onShowSilentHint();

        void onShowVibrateHint();

        void onStateChanged(State state);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialogController$MediaSessionsCallbacks.class */
    public final class MediaSessionsCallbacks implements MediaSessions.Callbacks {
        private int mNextStream;
        private final HashMap<MediaSession.Token, Integer> mRemoteStreams;
        final VolumeDialogController this$0;

        private MediaSessionsCallbacks(VolumeDialogController volumeDialogController) {
            this.this$0 = volumeDialogController;
            this.mRemoteStreams = new HashMap<>();
            this.mNextStream = 100;
        }

        /* synthetic */ MediaSessionsCallbacks(VolumeDialogController volumeDialogController, MediaSessionsCallbacks mediaSessionsCallbacks) {
            this(volumeDialogController);
        }

        private MediaSession.Token findToken(int i) {
            Iterator<T> it = this.mRemoteStreams.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                if (((Integer) entry.getValue()).equals(Integer.valueOf(i))) {
                    return (MediaSession.Token) entry.getKey();
                }
            }
            return null;
        }

        @Override // com.android.systemui.volume.MediaSessions.Callbacks
        public void onRemoteRemoved(MediaSession.Token token) {
            int intValue = this.mRemoteStreams.get(token).intValue();
            this.this$0.mState.states.remove(intValue);
            if (this.this$0.mState.activeStream == intValue) {
                this.this$0.updateActiveStreamW(-1);
            }
            this.this$0.mCallbacks.onStateChanged(this.this$0.mState);
        }

        @Override // com.android.systemui.volume.MediaSessions.Callbacks
        public void onRemoteUpdate(MediaSession.Token token, String str, MediaController.PlaybackInfo playbackInfo) {
            if (!this.mRemoteStreams.containsKey(token)) {
                this.mRemoteStreams.put(token, Integer.valueOf(this.mNextStream));
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onRemoteUpdate: " + str + " is stream " + this.mNextStream);
                }
                this.mNextStream++;
            }
            int intValue = this.mRemoteStreams.get(token).intValue();
            boolean z = this.this$0.mState.states.indexOfKey(intValue) < 0;
            StreamState streamStateW = this.this$0.streamStateW(intValue);
            streamStateW.dynamic = true;
            streamStateW.levelMin = 0;
            streamStateW.levelMax = playbackInfo.getMaxVolume();
            if (streamStateW.level != playbackInfo.getCurrentVolume()) {
                streamStateW.level = playbackInfo.getCurrentVolume();
                z = true;
            }
            if (!Objects.equals(streamStateW.name, str)) {
                streamStateW.name = str;
                z = true;
            }
            if (z) {
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onRemoteUpdate: " + str + ": " + streamStateW.level + " of " + streamStateW.levelMax);
                }
                this.this$0.mCallbacks.onStateChanged(this.this$0.mState);
            }
        }

        @Override // com.android.systemui.volume.MediaSessions.Callbacks
        public void onRemoteVolumeChanged(MediaSession.Token token, int i) {
            if (this.mRemoteStreams.get(token) != null) {
                int intValue = this.mRemoteStreams.get(token).intValue();
                boolean z = (i & 1) != 0;
                boolean updateActiveStreamW = this.this$0.updateActiveStreamW(intValue);
                boolean z2 = updateActiveStreamW;
                if (z) {
                    z2 = updateActiveStreamW | this.this$0.checkRoutedToBluetoothW(3);
                }
                if (z2) {
                    this.this$0.mCallbacks.onStateChanged(this.this$0.mState);
                }
                if (z) {
                    this.this$0.mCallbacks.onShowRequested(2);
                }
            }
        }

        public void setStreamVolume(int i, int i2) {
            MediaSession.Token findToken = findToken(i);
            if (findToken == null) {
                Log.w(VolumeDialogController.TAG, "setStreamVolume: No token found for stream: " + i);
            } else {
                this.this$0.mMediaSessions.setVolume(findToken, i2);
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialogController$Receiver.class */
    private final class Receiver extends BroadcastReceiver {
        final VolumeDialogController this$0;

        private Receiver(VolumeDialogController volumeDialogController) {
            this.this$0 = volumeDialogController;
        }

        /* synthetic */ Receiver(VolumeDialogController volumeDialogController, Receiver receiver) {
            this(volumeDialogController);
        }

        public void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
            intentFilter.addAction("android.media.STREAM_DEVICES_CHANGED_ACTION");
            intentFilter.addAction("android.media.RINGER_MODE_CHANGED");
            intentFilter.addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
            intentFilter.addAction("android.media.STREAM_MUTE_CHANGED_ACTION");
            intentFilter.addAction("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
            intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            this.this$0.mContext.registerReceiver(this, intentFilter, null, this.this$0.mWorker);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            boolean z;
            String action = intent.getAction();
            if (action.equals("android.media.VOLUME_CHANGED_ACTION")) {
                int intExtra = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                int intExtra2 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1);
                int intExtra3 = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", -1);
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive VOLUME_CHANGED_ACTION stream=" + intExtra + " level=" + intExtra2 + " oldLevel=" + intExtra3);
                }
                z = this.this$0.updateStreamLevelW(intExtra, intExtra2);
            } else if (action.equals("android.media.STREAM_DEVICES_CHANGED_ACTION")) {
                int intExtra4 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                int intExtra5 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_DEVICES", -1);
                int intExtra6 = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_DEVICES", -1);
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive STREAM_DEVICES_CHANGED_ACTION stream=" + intExtra4 + " devices=" + intExtra5 + " oldDevices=" + intExtra6);
                }
                z = this.this$0.checkRoutedToBluetoothW(intExtra4) | this.this$0.onVolumeChangedW(intExtra4, 0);
            } else if (action.equals("android.media.RINGER_MODE_CHANGED")) {
                int intExtra7 = intent.getIntExtra("android.media.EXTRA_RINGER_MODE", -1);
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive RINGER_MODE_CHANGED_ACTION rm=" + Util.ringerModeToString(intExtra7));
                }
                z = this.this$0.updateRingerModeExternalW(intExtra7);
            } else if (action.equals("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION")) {
                int intExtra8 = intent.getIntExtra("android.media.EXTRA_RINGER_MODE", -1);
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive INTERNAL_RINGER_MODE_CHANGED_ACTION rm=" + Util.ringerModeToString(intExtra8));
                }
                z = this.this$0.updateRingerModeInternalW(intExtra8);
            } else if (action.equals("android.media.STREAM_MUTE_CHANGED_ACTION")) {
                int intExtra9 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                boolean booleanExtra = intent.getBooleanExtra("android.media.EXTRA_STREAM_VOLUME_MUTED", false);
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive STREAM_MUTE_CHANGED_ACTION stream=" + intExtra9 + " muted=" + booleanExtra);
                }
                z = this.this$0.updateStreamMuteW(intExtra9, booleanExtra);
            } else if (action.equals("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED")) {
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive ACTION_EFFECTS_SUPPRESSOR_CHANGED");
                }
                z = this.this$0.updateEffectsSuppressorW(this.this$0.mNoMan.getEffectsSuppressor());
            } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive ACTION_CONFIGURATION_CHANGED");
                }
                this.this$0.mCallbacks.onConfigurationChanged();
                z = false;
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive ACTION_SCREEN_OFF");
                }
                this.this$0.mCallbacks.onScreenOff();
                z = false;
            } else {
                z = false;
                if (action.equals("android.intent.action.CLOSE_SYSTEM_DIALOGS")) {
                    if (D.BUG) {
                        Log.d(VolumeDialogController.TAG, "onReceive ACTION_CLOSE_SYSTEM_DIALOGS");
                    }
                    this.this$0.dismiss();
                    z = false;
                }
            }
            if (z) {
                this.this$0.mCallbacks.onStateChanged(this.this$0.mState);
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialogController$SettingObserver.class */
    private final class SettingObserver extends ContentObserver {
        private final Uri SERVICE_URI;
        private final Uri ZEN_MODE_CONFIG_URI;
        private final Uri ZEN_MODE_URI;
        final VolumeDialogController this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public SettingObserver(VolumeDialogController volumeDialogController, Handler handler) {
            super(handler);
            this.this$0 = volumeDialogController;
            this.SERVICE_URI = Settings.Secure.getUriFor("volume_controller_service_component");
            this.ZEN_MODE_URI = Settings.Global.getUriFor("zen_mode");
            this.ZEN_MODE_CONFIG_URI = Settings.Global.getUriFor("zen_mode_config_etag");
        }

        public void init() {
            this.this$0.mContext.getContentResolver().registerContentObserver(this.SERVICE_URI, false, this);
            this.this$0.mContext.getContentResolver().registerContentObserver(this.ZEN_MODE_URI, false, this);
            this.this$0.mContext.getContentResolver().registerContentObserver(this.ZEN_MODE_CONFIG_URI, false, this);
            onChange(true, this.SERVICE_URI);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (this.SERVICE_URI.equals(uri)) {
                String string = Settings.Secure.getString(this.this$0.mContext.getContentResolver(), "volume_controller_service_component");
                boolean equals = (string == null || this.this$0.mComponent == null) ? false : this.this$0.mComponent.equals(ComponentName.unflattenFromString(string));
                if (equals == this.this$0.mEnabled) {
                    return;
                }
                if (equals) {
                    this.this$0.register();
                }
                this.this$0.mEnabled = equals;
            }
            boolean z2 = false;
            if (this.ZEN_MODE_URI.equals(uri)) {
                z2 = this.this$0.updateZenModeW();
            }
            if (z2) {
                this.this$0.mCallbacks.onStateChanged(this.this$0.mState);
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialogController$State.class */
    public static final class State {
        public static int NO_ACTIVE_STREAM = -1;
        public ComponentName effectsSuppressor;
        public String effectsSuppressorName;
        public int ringerModeExternal;
        public int ringerModeInternal;
        public int zenMode;
        public final SparseArray<StreamState> states = new SparseArray<>();
        public int activeStream = NO_ACTIVE_STREAM;

        private static void sep(StringBuilder sb, int i) {
            if (i <= 0) {
                sb.append(',');
                return;
            }
            sb.append('\n');
            for (int i2 = 0; i2 < i; i2++) {
                sb.append(' ');
            }
        }

        public State copy() {
            State state = new State();
            for (int i = 0; i < this.states.size(); i++) {
                state.states.put(this.states.keyAt(i), this.states.valueAt(i).copy());
            }
            state.ringerModeExternal = this.ringerModeExternal;
            state.ringerModeInternal = this.ringerModeInternal;
            state.zenMode = this.zenMode;
            if (this.effectsSuppressor != null) {
                state.effectsSuppressor = this.effectsSuppressor.clone();
            }
            state.effectsSuppressorName = this.effectsSuppressorName;
            state.activeStream = this.activeStream;
            return state;
        }

        public String toString() {
            return toString(0);
        }

        public String toString(int i) {
            StringBuilder sb = new StringBuilder("{");
            if (i > 0) {
                sep(sb, i);
            }
            for (int i2 = 0; i2 < this.states.size(); i2++) {
                if (i2 > 0) {
                    sep(sb, i);
                }
                int keyAt = this.states.keyAt(i2);
                StreamState valueAt = this.states.valueAt(i2);
                sb.append(AudioSystem.streamToString(keyAt)).append(":").append(valueAt.level).append('[').append(valueAt.levelMin).append("..").append(valueAt.levelMax).append(']');
                if (valueAt.muted) {
                    sb.append(" [MUTED]");
                }
            }
            sep(sb, i);
            sb.append("ringerModeExternal:").append(this.ringerModeExternal);
            sep(sb, i);
            sb.append("ringerModeInternal:").append(this.ringerModeInternal);
            sep(sb, i);
            sb.append("zenMode:").append(this.zenMode);
            sep(sb, i);
            sb.append("effectsSuppressor:").append(this.effectsSuppressor);
            sep(sb, i);
            sb.append("effectsSuppressorName:").append(this.effectsSuppressorName);
            sep(sb, i);
            sb.append("activeStream:").append(this.activeStream);
            if (i > 0) {
                sep(sb, i);
            }
            return sb.append('}').toString();
        }
    }

    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialogController$StreamState.class */
    public static final class StreamState {
        public boolean dynamic;
        public int level;
        public int levelMax;
        public int levelMin;
        public boolean muteSupported;
        public boolean muted;
        public String name;
        public boolean routedToBluetooth;

        public StreamState copy() {
            StreamState streamState = new StreamState();
            streamState.dynamic = this.dynamic;
            streamState.level = this.level;
            streamState.levelMin = this.levelMin;
            streamState.levelMax = this.levelMax;
            streamState.muted = this.muted;
            streamState.muteSupported = this.muteSupported;
            streamState.name = this.name;
            streamState.routedToBluetooth = this.routedToBluetooth;
            return streamState;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialogController$VC.class */
    public final class VC extends IVolumeController.Stub {
        private final String TAG;
        final VolumeDialogController this$0;

        private VC(VolumeDialogController volumeDialogController) {
            this.this$0 = volumeDialogController;
            this.TAG = VolumeDialogController.TAG + ".VC";
        }

        /* synthetic */ VC(VolumeDialogController volumeDialogController, VC vc) {
            this(volumeDialogController);
        }

        public void dismiss() throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "dismiss requested");
            }
            if (this.this$0.mDestroyed) {
                return;
            }
            this.this$0.mWorker.obtainMessage(2, 2, 0).sendToTarget();
            this.this$0.mWorker.sendEmptyMessage(2);
        }

        public void displaySafeVolumeWarning(int i) throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "displaySafeVolumeWarning " + Util.audioManagerFlagsToString(i));
            }
            if (this.this$0.mDestroyed) {
                return;
            }
            this.this$0.mWorker.obtainMessage(14, i, 0).sendToTarget();
        }

        public void masterMuteChanged(int i) throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "masterMuteChanged");
            }
        }

        public void setLayoutDirection(int i) throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "setLayoutDirection");
            }
            if (this.this$0.mDestroyed) {
                return;
            }
            this.this$0.mWorker.obtainMessage(8, i, 0).sendToTarget();
        }

        public void volumeChanged(int i, int i2) throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "volumeChanged " + AudioSystem.streamToString(i) + " " + Util.audioManagerFlagsToString(i2));
            }
            if (this.this$0.mDestroyed) {
                return;
            }
            this.this$0.mWorker.obtainMessage(1, i, i2).sendToTarget();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialogController$W.class */
    public final class W extends Handler {
        final VolumeDialogController this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        W(VolumeDialogController volumeDialogController, Looper looper) {
            super(looper);
            this.this$0 = volumeDialogController;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            boolean z = true;
            switch (message.what) {
                case 1:
                    this.this$0.onVolumeChangedW(message.arg1, message.arg2);
                    return;
                case 2:
                    this.this$0.onDismissRequestedW(message.arg1);
                    return;
                case 3:
                    this.this$0.onGetStateW();
                    return;
                case 4:
                    VolumeDialogController volumeDialogController = this.this$0;
                    int i = message.arg1;
                    if (message.arg2 == 0) {
                        z = false;
                    }
                    volumeDialogController.onSetRingerModeW(i, z);
                    return;
                case 5:
                    this.this$0.onSetZenModeW(message.arg1);
                    return;
                case 6:
                    this.this$0.onSetExitConditionW((Condition) message.obj);
                    return;
                case 7:
                    this.this$0.onSetStreamMuteW(message.arg1, message.arg2 != 0);
                    return;
                case 8:
                    this.this$0.mCallbacks.onLayoutDirectionChanged(message.arg1);
                    return;
                case 9:
                    this.this$0.mCallbacks.onConfigurationChanged();
                    return;
                case 10:
                    this.this$0.onSetStreamVolumeW(message.arg1, message.arg2);
                    return;
                case 11:
                    this.this$0.onSetActiveStreamW(message.arg1);
                    return;
                case 12:
                    this.this$0.onNotifyVisibleW(message.arg1 != 0);
                    return;
                case 13:
                    this.this$0.onUserActivityW();
                    return;
                case 14:
                    this.this$0.onShowSafetyWarningW(message.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    public VolumeDialogController(Context context, ComponentName componentName) {
        this.mContext = context.getApplicationContext();
        Events.writeEvent(this.mContext, 5, new Object[0]);
        this.mComponent = componentName;
        this.mWorkerThread = new HandlerThread(VolumeDialogController.class.getSimpleName());
        this.mWorkerThread.start();
        this.mWorker = new W(this, this.mWorkerThread.getLooper());
        this.mMediaSessions = createMediaSessions(this.mContext, this.mWorkerThread.getLooper(), this.mMediaSessionsCallbacksW);
        this.mAudio = (AudioManager) this.mContext.getSystemService("audio");
        this.mNoMan = (NotificationManager) this.mContext.getSystemService("notification");
        this.mObserver = new SettingObserver(this, this.mWorker);
        this.mObserver.init();
        this.mReceiver.init();
        this.mStreamTitles = this.mContext.getResources().getStringArray(2131427375);
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mHasVibrator = this.mVibrator != null ? this.mVibrator.hasVibrator() : false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean checkRoutedToBluetoothW(int i) {
        boolean z = false;
        if (i == 3) {
            boolean z2 = false;
            if ((this.mAudio.getDevicesForStream(3) & 896) != 0) {
                z2 = true;
            }
            z = updateStreamRoutedToBluetoothW(i, z2);
        }
        return z;
    }

    private static String getApplicationName(Context context, ComponentName componentName) {
        if (componentName == null) {
            return null;
        }
        PackageManager packageManager = context.getPackageManager();
        String packageName = componentName.getPackageName();
        try {
            String trim = Objects.toString(packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager), "").trim();
            if (trim.length() > 0) {
                return trim;
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return packageName;
    }

    private static boolean isLogWorthy(int i) {
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 6:
                return true;
            case 5:
            default:
                return false;
        }
    }

    private static boolean isRinger(int i) {
        boolean z = true;
        if (i != 2) {
            z = i == 5;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDismissRequestedW(int i) {
        this.mCallbacks.onDismissRequested(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onGetStateW() {
        int[] iArr;
        for (int i : STREAMS) {
            updateStreamLevelW(i, this.mAudio.getLastAudibleStreamVolume(i));
            streamStateW(i).levelMin = this.mAudio.getStreamMinVolume(i);
            streamStateW(i).levelMax = this.mAudio.getStreamMaxVolume(i);
            updateStreamMuteW(i, this.mAudio.isStreamMute(i));
            StreamState streamStateW = streamStateW(i);
            streamStateW.muteSupported = this.mAudio.isStreamAffectedByMute(i);
            streamStateW.name = this.mStreamTitles[i];
            checkRoutedToBluetoothW(i);
        }
        updateRingerModeExternalW(this.mAudio.getRingerMode());
        updateZenModeW();
        updateEffectsSuppressorW(this.mNoMan.getEffectsSuppressor());
        this.mCallbacks.onStateChanged(this.mState);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onNotifyVisibleW(boolean z) {
        if (this.mDestroyed) {
            return;
        }
        this.mAudio.notifyVolumeControllerVisible(this.mVolumeController, z);
        if (z || !updateActiveStreamW(-1)) {
            return;
        }
        this.mCallbacks.onStateChanged(this.mState);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSetActiveStreamW(int i) {
        if (updateActiveStreamW(i)) {
            this.mCallbacks.onStateChanged(this.mState);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSetExitConditionW(Condition condition) {
        Uri uri = null;
        NotificationManager notificationManager = this.mNoMan;
        int i = this.mState.zenMode;
        if (condition != null) {
            uri = condition.id;
        }
        notificationManager.setZenMode(i, uri, TAG);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSetRingerModeW(int i, boolean z) {
        if (z) {
            this.mAudio.setRingerMode(i);
        } else {
            this.mAudio.setRingerModeInternal(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSetStreamMuteW(int i, boolean z) {
        this.mAudio.adjustStreamVolume(i, z ? -100 : 100, 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSetStreamVolumeW(int i, int i2) {
        if (D.BUG) {
            Log.d(TAG, "onSetStreamVolume " + i + " level=" + i2);
        }
        if (i >= 100) {
            this.mMediaSessionsCallbacksW.setStreamVolume(i, i2);
        } else {
            this.mAudio.setStreamVolume(i, i2, 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSetZenModeW(int i) {
        if (D.BUG) {
            Log.d(TAG, "onSetZenModeW " + i);
        }
        this.mNoMan.setZenMode(i, null, TAG);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onShowSafetyWarningW(int i) {
        this.mCallbacks.onShowSafetyWarning(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean onVolumeChangedW(int i, int i2) {
        boolean z = (i2 & 1) != 0;
        boolean z2 = (i2 & 4096) != 0;
        boolean z3 = (i2 & 2048) != 0;
        boolean z4 = (i2 & 128) != 0;
        boolean z5 = false;
        if (z) {
            z5 = updateActiveStreamW(i);
        }
        int lastAudibleStreamVolume = this.mAudio.getLastAudibleStreamVolume(i);
        boolean updateStreamLevelW = z5 | updateStreamLevelW(i, lastAudibleStreamVolume) | checkRoutedToBluetoothW(z ? 3 : i);
        if (updateStreamLevelW) {
            this.mCallbacks.onStateChanged(this.mState);
        }
        if (z) {
            this.mCallbacks.onShowRequested(1);
        }
        if (z3) {
            this.mCallbacks.onShowVibrateHint();
        }
        if (z4) {
            this.mCallbacks.onShowSilentHint();
        }
        if (updateStreamLevelW && z2) {
            Events.writeEvent(this.mContext, 4, Integer.valueOf(i), Integer.valueOf(lastAudibleStreamVolume));
        }
        return updateStreamLevelW;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public StreamState streamStateW(int i) {
        StreamState streamState = this.mState.states.get(i);
        StreamState streamState2 = streamState;
        if (streamState == null) {
            streamState2 = new StreamState();
            this.mState.states.put(i, streamState2);
        }
        return streamState2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateActiveStreamW(int i) {
        if (i == this.mState.activeStream) {
            return false;
        }
        this.mState.activeStream = i;
        Events.writeEvent(this.mContext, 2, Integer.valueOf(i));
        if (D.BUG) {
            Log.d(TAG, "updateActiveStreamW " + i);
        }
        if (i >= 100) {
            i = -1;
        }
        if (D.BUG) {
            Log.d(TAG, "forceVolumeControlStream " + i);
        }
        this.mAudio.forceVolumeControlStream(i);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateEffectsSuppressorW(ComponentName componentName) {
        if (Objects.equals(this.mState.effectsSuppressor, componentName)) {
            return false;
        }
        this.mState.effectsSuppressor = componentName;
        this.mState.effectsSuppressorName = getApplicationName(this.mContext, this.mState.effectsSuppressor);
        Events.writeEvent(this.mContext, 14, this.mState.effectsSuppressor, this.mState.effectsSuppressorName);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateRingerModeExternalW(int i) {
        if (i == this.mState.ringerModeExternal) {
            return false;
        }
        this.mState.ringerModeExternal = i;
        Events.writeEvent(this.mContext, 12, Integer.valueOf(i));
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateRingerModeInternalW(int i) {
        if (i == this.mState.ringerModeInternal) {
            return false;
        }
        this.mState.ringerModeInternal = i;
        Events.writeEvent(this.mContext, 11, Integer.valueOf(i));
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateStreamLevelW(int i, int i2) {
        StreamState streamStateW = streamStateW(i);
        if (streamStateW.level == i2) {
            return false;
        }
        streamStateW.level = i2;
        if (isLogWorthy(i)) {
            Events.writeEvent(this.mContext, 10, Integer.valueOf(i), Integer.valueOf(i2));
            return true;
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateStreamMuteW(int i, boolean z) {
        StreamState streamStateW = streamStateW(i);
        if (streamStateW.muted == z) {
            return false;
        }
        streamStateW.muted = z;
        if (isLogWorthy(i)) {
            Events.writeEvent(this.mContext, 15, Integer.valueOf(i), Boolean.valueOf(z));
        }
        if (z && isRinger(i)) {
            updateRingerModeInternalW(this.mAudio.getRingerModeInternal());
            return true;
        }
        return true;
    }

    private boolean updateStreamRoutedToBluetoothW(int i, boolean z) {
        StreamState streamStateW = streamStateW(i);
        if (streamStateW.routedToBluetooth == z) {
            return false;
        }
        streamStateW.routedToBluetooth = z;
        if (D.BUG) {
            Log.d(TAG, "updateStreamRoutedToBluetoothW stream=" + i + " routedToBluetooth=" + z);
            return true;
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateZenModeW() {
        int i = Settings.Global.getInt(this.mContext.getContentResolver(), "zen_mode", 0);
        if (this.mState.zenMode == i) {
            return false;
        }
        this.mState.zenMode = i;
        Events.writeEvent(this.mContext, 13, Integer.valueOf(i));
        return true;
    }

    public void addCallback(Callbacks callbacks, Handler handler) {
        this.mCallbacks.add(callbacks, handler);
    }

    protected MediaSessions createMediaSessions(Context context, Looper looper, MediaSessions.Callbacks callbacks) {
        return new MediaSessions(context, looper, callbacks);
    }

    public void dismiss() {
        this.mCallbacks.onDismissRequested(2);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(VolumeDialogController.class.getSimpleName() + " state:");
        printWriter.print("  mEnabled: ");
        printWriter.println(this.mEnabled);
        printWriter.print("  mDestroyed: ");
        printWriter.println(this.mDestroyed);
        printWriter.print("  mVolumePolicy: ");
        printWriter.println(this.mVolumePolicy);
        printWriter.print("  mState: ");
        printWriter.println(this.mState.toString(4));
        printWriter.print("  mShowDndTile: ");
        printWriter.println(this.mShowDndTile);
        printWriter.print("  mHasVibrator: ");
        printWriter.println(this.mHasVibrator);
        printWriter.print("  mRemoteStreams: ");
        printWriter.println(this.mMediaSessionsCallbacksW.mRemoteStreams.values());
        printWriter.println();
        this.mMediaSessions.dump(printWriter);
    }

    public AudioManager getAudioManager() {
        return this.mAudio;
    }

    public void getState() {
        if (this.mDestroyed) {
            return;
        }
        this.mWorker.sendEmptyMessage(3);
    }

    public boolean hasVibrator() {
        return this.mHasVibrator;
    }

    public void notifyVisible(boolean z) {
        if (this.mDestroyed) {
            return;
        }
        this.mWorker.obtainMessage(12, z ? 1 : 0, 0).sendToTarget();
    }

    protected void onUserActivityW() {
    }

    public void register() {
        try {
            this.mAudio.setVolumeController(this.mVolumeController);
            setVolumePolicy(this.mVolumePolicy);
            showDndTile(this.mShowDndTile);
            try {
                this.mMediaSessions.init();
            } catch (SecurityException e) {
                Log.w(TAG, "No access to media sessions", e);
            }
        } catch (SecurityException e2) {
            Log.w(TAG, "Unable to set the volume controller", e2);
        }
    }

    public void setActiveStream(int i) {
        if (this.mDestroyed) {
            return;
        }
        this.mWorker.obtainMessage(11, i, 0).sendToTarget();
    }

    public void setRingerMode(int i, boolean z) {
        if (this.mDestroyed) {
            return;
        }
        this.mWorker.obtainMessage(4, i, z ? 1 : 0).sendToTarget();
    }

    public void setStreamVolume(int i, int i2) {
        if (this.mDestroyed) {
            return;
        }
        this.mWorker.obtainMessage(10, i, i2).sendToTarget();
    }

    public void setVolumePolicy(VolumePolicy volumePolicy) {
        this.mVolumePolicy = volumePolicy;
        if (this.mVolumePolicy == null) {
            return;
        }
        try {
            this.mAudio.setVolumePolicy(this.mVolumePolicy);
        } catch (NoSuchMethodError e) {
            Log.w(TAG, "No volume policy api");
        }
    }

    public void showDndTile(boolean z) {
        if (D.BUG) {
            Log.d(TAG, "showDndTile");
        }
        DndTile.setVisible(this.mContext, z);
    }

    public void userActivity() {
        if (this.mDestroyed) {
            return;
        }
        this.mWorker.removeMessages(13);
        this.mWorker.sendEmptyMessage(13);
    }

    public void vibrate() {
        if (this.mHasVibrator) {
            this.mVibrator.vibrate(50L);
        }
    }
}
