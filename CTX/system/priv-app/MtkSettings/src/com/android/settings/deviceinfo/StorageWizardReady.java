package com.android.settings.deviceinfo;

import android.os.Bundle;
import android.os.storage.StorageEventListener;
import android.os.storage.VolumeInfo;
import android.util.Log;
import android.view.View;
import com.android.settings.R;
import java.util.Objects;
/* loaded from: classes.dex */
public class StorageWizardReady extends StorageWizardBase {
    private final StorageEventListener mStorageMountListener = new StorageEventListener() { // from class: com.android.settings.deviceinfo.StorageWizardReady.1
        public void onVolumeStateChanged(VolumeInfo volumeInfo, int i, int i2) {
            Log.d("StorageWizardReady", "onVolumeStateChanged, disk : " + volumeInfo.getDiskId() + ", type : " + volumeInfo.getType() + ", state : " + volumeInfo.getState());
            if (Objects.equals(StorageWizardReady.this.mDisk.getId(), volumeInfo.getDiskId()) && volumeInfo.getType() == 0 && i2 == 2) {
                StorageWizardReady.this.setBodyText(R.string.storage_wizard_ready_v2_external_body, StorageWizardReady.this.mDisk.getDescription());
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.deviceinfo.StorageWizardBase, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (this.mDisk == null) {
            finish();
            return;
        }
        setContentView(R.layout.storage_wizard_generic);
        setHeaderText(R.string.storage_wizard_ready_title, getDiskShortDescription());
        if (findFirstVolume(1) != null) {
            if (getIntent().getBooleanExtra("migrate_skip", false)) {
                setBodyText(R.string.storage_wizard_ready_v2_internal_body, getDiskDescription());
            } else {
                setBodyText(R.string.storage_wizard_ready_v2_internal_moved_body, getDiskDescription(), getDiskShortDescription());
            }
        } else {
            setBodyText(R.string.storage_wizard_ready_v2_external_body, getDiskDescription());
        }
        setNextButtonText(R.string.done, new CharSequence[0]);
        this.mStorage.registerListener(this.mStorageMountListener);
    }

    @Override // com.android.settings.deviceinfo.StorageWizardBase
    public void onNavigateNext(View view) {
        finishAffinity();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.deviceinfo.StorageWizardBase, android.app.Activity
    public void onDestroy() {
        this.mStorage.unregisterListener(this.mStorageMountListener);
        super.onDestroy();
    }
}
