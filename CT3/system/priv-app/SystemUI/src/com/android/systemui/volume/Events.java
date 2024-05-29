package com.android.systemui.volume;

import android.content.Context;
import android.media.AudioSystem;
import android.util.Log;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.volume.VolumeDialogController;
import java.util.Arrays;
/* loaded from: a.zip:com/android/systemui/volume/Events.class */
public class Events {
    public static Callback sCallback;
    private static final String TAG = Util.logTag(Events.class);
    private static final String[] EVENT_TAGS = {"show_dialog", "dismiss_dialog", "active_stream_changed", "expand", "key", "collection_started", "collection_stopped", "icon_click", "settings_click", "touch_level_changed", "level_changed", "internal_ringer_mode_changed", "external_ringer_mode_changed", "zen_mode_changed", "suppressor_changed", "mute_changed", "touch_level_done"};
    public static final String[] DISMISS_REASONS = {"unknown", "touch_outside", "volume_controller", "timeout", "screen_off", "settings_clicked", "done_clicked"};
    public static final String[] SHOW_REASONS = {"unknown", "volume_changed", "remote_volume_changed"};

    /* loaded from: a.zip:com/android/systemui/volume/Events$Callback.class */
    public interface Callback {
        void writeEvent(long j, int i, Object[] objArr);

        void writeState(long j, VolumeDialogController.State state);
    }

    private static String iconStateToString(int i) {
        switch (i) {
            case 1:
                return "unmute";
            case 2:
                return "mute";
            case 3:
                return "vibrate";
            default:
                return "unknown_state_" + i;
        }
    }

    private static String ringerModeToString(int i) {
        switch (i) {
            case 0:
                return "silent";
            case 1:
                return "vibrate";
            case 2:
                return "normal";
            default:
                return "unknown";
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public static void writeEvent(Context context, int i, Object... objArr) {
        long currentTimeMillis = System.currentTimeMillis();
        StringBuilder append = new StringBuilder("writeEvent ").append(EVENT_TAGS[i]);
        if (objArr != null && objArr.length > 0) {
            append.append(" ");
            switch (i) {
                case 0:
                    MetricsLogger.visible(context, 207);
                    MetricsLogger.histogram(context, "volume_from_keyguard", ((Boolean) objArr[1]).booleanValue() ? 1 : 0);
                    append.append(SHOW_REASONS[((Integer) objArr[0]).intValue()]).append(" keyguard=").append(objArr[1]);
                    break;
                case 1:
                    MetricsLogger.hidden(context, 207);
                    append.append(DISMISS_REASONS[((Integer) objArr[0]).intValue()]);
                    break;
                case 2:
                    MetricsLogger.action(context, 210, ((Integer) objArr[0]).intValue());
                    append.append(AudioSystem.streamToString(((Integer) objArr[0]).intValue()));
                    break;
                case 3:
                    MetricsLogger.visibility(context, 208, ((Boolean) objArr[0]).booleanValue());
                    append.append(objArr[0]);
                    break;
                case 4:
                    MetricsLogger.action(context, 211, ((Integer) objArr[1]).intValue());
                    append.append(AudioSystem.streamToString(((Integer) objArr[0]).intValue())).append(' ').append(objArr[1]);
                    break;
                case 5:
                case 6:
                case 8:
                default:
                    append.append(Arrays.asList(objArr));
                    break;
                case 7:
                    MetricsLogger.action(context, 212, ((Integer) objArr[1]).intValue());
                    append.append(AudioSystem.streamToString(((Integer) objArr[0]).intValue())).append(' ').append(iconStateToString(((Integer) objArr[1]).intValue()));
                    break;
                case 9:
                case 10:
                case 15:
                    append.append(AudioSystem.streamToString(((Integer) objArr[0]).intValue())).append(' ').append(objArr[1]);
                    break;
                case 11:
                    append.append(ringerModeToString(((Integer) objArr[0]).intValue()));
                    break;
                case 12:
                    MetricsLogger.action(context, 213, ((Integer) objArr[0]).intValue());
                    append.append(ringerModeToString(((Integer) objArr[0]).intValue()));
                    break;
                case 13:
                    append.append(zenModeToString(((Integer) objArr[0]).intValue()));
                    break;
                case 14:
                    append.append(objArr[0]).append(' ').append(objArr[1]);
                    break;
                case 16:
                    MetricsLogger.action(context, 209, ((Integer) objArr[1]).intValue());
                    append.append(AudioSystem.streamToString(((Integer) objArr[0]).intValue())).append(' ').append(objArr[1]);
                    break;
            }
        }
        Log.i(TAG, append.toString());
        if (sCallback != null) {
            sCallback.writeEvent(currentTimeMillis, i, objArr);
        }
    }

    public static void writeState(long j, VolumeDialogController.State state) {
        if (sCallback != null) {
            sCallback.writeState(j, state);
        }
    }

    private static String zenModeToString(int i) {
        switch (i) {
            case 0:
                return "off";
            case 1:
                return "important_interruptions";
            case 2:
                return "no_interruptions";
            case 3:
                return "alarms";
            default:
                return "unknown";
        }
    }
}
