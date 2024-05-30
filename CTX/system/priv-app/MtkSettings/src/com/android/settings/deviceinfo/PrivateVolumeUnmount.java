package com.android.settings.deviceinfo;

import android.os.Bundle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.deviceinfo.StorageSettings;
/* loaded from: classes.dex */
public class PrivateVolumeUnmount extends SettingsPreferenceFragment {
    private final View.OnClickListener mConfirmListener = new View.OnClickListener() { // from class: com.android.settings.deviceinfo.PrivateVolumeUnmount.1
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            new StorageSettings.UnmountTask(PrivateVolumeUnmount.this.getActivity(), PrivateVolumeUnmount.this.mVolume).execute(new Void[0]);
            PrivateVolumeUnmount.this.getActivity().finish();
        }
    };
    private DiskInfo mDisk;
    private VolumeInfo mVolume;

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 42;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        StorageManager storageManager = (StorageManager) getActivity().getSystemService(StorageManager.class);
        this.mVolume = storageManager.findVolumeById(getArguments().getString("android.os.storage.extra.VOLUME_ID"));
        this.mDisk = storageManager.findDiskById(this.mVolume.getDiskId());
        View inflate = layoutInflater.inflate(R.layout.storage_internal_unmount, viewGroup, false);
        ((TextView) inflate.findViewById(R.id.body)).setText(TextUtils.expandTemplate(getText(R.string.storage_internal_unmount_details), this.mDisk.getDescription()));
        ((Button) inflate.findViewById(R.id.confirm)).setOnClickListener(this.mConfirmListener);
        return inflate;
    }
}
