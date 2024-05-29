package com.android.settings.deviceinfo;

import android.content.Intent;
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
import com.android.settings.InstrumentedFragment;
import com.android.settings.R;
/* loaded from: classes.dex */
public class PrivateVolumeFormat extends InstrumentedFragment {
    private final View.OnClickListener mConfirmListener = new View.OnClickListener() { // from class: com.android.settings.deviceinfo.PrivateVolumeFormat.1
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            Intent intent = new Intent(PrivateVolumeFormat.this.getActivity(), StorageWizardFormatProgress.class);
            intent.putExtra("android.os.storage.extra.DISK_ID", PrivateVolumeFormat.this.mDisk.getId());
            intent.putExtra("format_private", false);
            intent.putExtra("forget_uuid", PrivateVolumeFormat.this.mVolume.getFsUuid());
            PrivateVolumeFormat.this.startActivity(intent);
            PrivateVolumeFormat.this.getActivity().finish();
        }
    };
    private DiskInfo mDisk;
    private VolumeInfo mVolume;

    @Override // com.android.settings.InstrumentedFragment
    protected int getMetricsCategory() {
        return 42;
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        StorageManager storage = (StorageManager) getActivity().getSystemService(StorageManager.class);
        String volumeId = getArguments().getString("android.os.storage.extra.VOLUME_ID");
        this.mVolume = storage.findVolumeById(volumeId);
        this.mDisk = storage.findDiskById(this.mVolume.getDiskId());
        View view = inflater.inflate(R.layout.storage_internal_format, container, false);
        TextView body = (TextView) view.findViewById(R.id.body);
        Button confirm = (Button) view.findViewById(R.id.confirm);
        body.setText(TextUtils.expandTemplate(getText(R.string.storage_internal_format_details), this.mDisk.getDescription()));
        confirm.setOnClickListener(this.mConfirmListener);
        return view;
    }
}
