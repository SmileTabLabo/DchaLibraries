package com.android.settings.slices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.settings.bluetooth.BluetoothSliceBuilder;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.SliderPreferenceController;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.notification.ZenModeSliceBuilder;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.wifi.WifiSliceBuilder;
/* loaded from: classes.dex */
public class SliceBroadcastReceiver extends BroadcastReceiver {
    private static String TAG = "SettSliceBroadcastRec";

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        char c;
        String action = intent.getAction();
        String stringExtra = intent.getStringExtra("com.android.settings.slice.extra.key");
        boolean booleanExtra = intent.getBooleanExtra("com.android.settings.slice.extra.platform", false);
        switch (action.hashCode()) {
            case -2075790298:
                if (action.equals("com.android.settings.slice.action.TOGGLE_CHANGED")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -932197342:
                if (action.equals("com.android.settings.bluetooth.action.BLUETOOTH_MODE_CHANGED")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -362341757:
                if (action.equals("com.android.settings.wifi.calling.action.WIFI_CALLING_CHANGED")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 17552563:
                if (action.equals("com.android.settings.slice.action.SLIDER_CHANGED")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 775016264:
                if (action.equals("com.android.settings.wifi.action.WIFI_CHANGED")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1913359032:
                if (action.equals("com.android.settings.notification.ZEN_MODE_CHANGED")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                handleToggleAction(context, stringExtra, intent.getBooleanExtra("android.app.slice.extra.TOGGLE_STATE", false), booleanExtra);
                return;
            case 1:
                handleSliderAction(context, stringExtra, intent.getIntExtra("android.app.slice.extra.RANGE_VALUE", -1), booleanExtra);
                return;
            case 2:
                BluetoothSliceBuilder.handleUriChange(context, intent);
                return;
            case 3:
                WifiSliceBuilder.handleUriChange(context, intent);
                return;
            case 4:
                FeatureFactory.getFactory(context).getSlicesFeatureProvider().getNewWifiCallingSliceHelper(context).handleWifiCallingChanged(intent);
                return;
            case 5:
                ZenModeSliceBuilder.handleUriChange(context, intent);
                return;
            default:
                return;
        }
    }

    private void handleToggleAction(Context context, String str, boolean z, boolean z2) {
        if (TextUtils.isEmpty(str)) {
            throw new IllegalStateException("No key passed to Intent for toggle controller");
        }
        BasePreferenceController preferenceController = getPreferenceController(context, str);
        if (!(preferenceController instanceof TogglePreferenceController)) {
            throw new IllegalStateException("Toggle action passed for a non-toggle key: " + str);
        } else if (!preferenceController.isAvailable()) {
            String str2 = TAG;
            Log.w(str2, "Can't update " + str + " since the setting is unavailable");
            if (!preferenceController.hasAsyncUpdate()) {
                updateUri(context, str, z2);
            }
        } else {
            ((TogglePreferenceController) preferenceController).setChecked(z);
            logSliceValueChange(context, str, z ? 1 : 0);
            if (!preferenceController.hasAsyncUpdate()) {
                updateUri(context, str, z2);
            }
        }
    }

    private void handleSliderAction(Context context, String str, int i, boolean z) {
        if (TextUtils.isEmpty(str)) {
            throw new IllegalArgumentException("No key passed to Intent for slider controller. Use extra: com.android.settings.slice.extra.key");
        }
        if (i == -1) {
            throw new IllegalArgumentException("Invalid position passed to Slider controller");
        }
        BasePreferenceController preferenceController = getPreferenceController(context, str);
        if (!(preferenceController instanceof SliderPreferenceController)) {
            throw new IllegalArgumentException("Slider action passed for a non-slider key: " + str);
        } else if (!preferenceController.isAvailable()) {
            String str2 = TAG;
            Log.w(str2, "Can't update " + str + " since the setting is unavailable");
            updateUri(context, str, z);
        } else {
            SliderPreferenceController sliderPreferenceController = (SliderPreferenceController) preferenceController;
            int maxSteps = sliderPreferenceController.getMaxSteps();
            if (i < 0 || i > maxSteps) {
                throw new IllegalArgumentException("Invalid position passed to Slider controller. Expected between 0 and " + maxSteps + " but found " + i);
            }
            sliderPreferenceController.setSliderPosition(i);
            logSliceValueChange(context, str, i);
            updateUri(context, str, z);
        }
    }

    private void logSliceValueChange(Context context, String str, int i) {
        FeatureFactory.getFactory(context).getMetricsFeatureProvider().action(context, 1372, Pair.create(854, str), Pair.create(1089, Integer.valueOf(i)));
    }

    private BasePreferenceController getPreferenceController(Context context, String str) {
        return SliceBuilderUtils.getPreferenceController(context, new SlicesDatabaseAccessor(context).getSliceDataFromKey(str));
    }

    private void updateUri(Context context, String str, boolean z) {
        String str2;
        if (z) {
            str2 = "android.settings.slices";
        } else {
            str2 = "com.android.settings.slices";
        }
        context.getContentResolver().notifyChange(new Uri.Builder().scheme("content").authority(str2).appendPath("action").appendPath(str).build(), null);
    }
}
