package com.android.settings.development;

import android.bluetooth.BluetoothCodecConfig;
import android.content.Context;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.Lifecycle;
/* loaded from: classes.dex */
public class BluetoothAudioSampleRatePreferenceController extends AbstractBluetoothA2dpPreferenceController {
    public BluetoothAudioSampleRatePreferenceController(Context context, Lifecycle lifecycle, BluetoothA2dpConfigStore bluetoothA2dpConfigStore) {
        super(context, lifecycle, bluetoothA2dpConfigStore);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "bluetooth_select_a2dp_sample_rate";
    }

    @Override // com.android.settings.development.AbstractBluetoothA2dpPreferenceController
    protected String[] getListValues() {
        return this.mContext.getResources().getStringArray(R.array.bluetooth_a2dp_codec_sample_rate_values);
    }

    @Override // com.android.settings.development.AbstractBluetoothA2dpPreferenceController
    protected String[] getListSummaries() {
        return this.mContext.getResources().getStringArray(R.array.bluetooth_a2dp_codec_sample_rate_summaries);
    }

    @Override // com.android.settings.development.AbstractBluetoothA2dpPreferenceController
    protected int getDefaultIndex() {
        return 0;
    }

    @Override // com.android.settings.development.AbstractBluetoothA2dpPreferenceController
    protected void writeConfigurationValues(Object obj) {
        int i = 0;
        switch (this.mPreference.findIndexOfValue(obj.toString())) {
            case 1:
                i = 1;
                break;
            case 2:
                i = 2;
                break;
            case 3:
                i = 4;
                break;
            case 4:
                i = 8;
                break;
        }
        this.mBluetoothA2dpConfigStore.setSampleRate(i);
    }

    @Override // com.android.settings.development.AbstractBluetoothA2dpPreferenceController
    protected int getCurrentA2dpSettingIndex(BluetoothCodecConfig bluetoothCodecConfig) {
        int sampleRate = bluetoothCodecConfig.getSampleRate();
        if (sampleRate != 4) {
            if (sampleRate == 8) {
                return 4;
            }
            switch (sampleRate) {
                case 1:
                    return 1;
                case 2:
                    return 2;
                default:
                    return 0;
            }
        }
        return 3;
    }
}
