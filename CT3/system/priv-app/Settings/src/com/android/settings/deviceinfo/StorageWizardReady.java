package com.android.settings.deviceinfo;

import android.os.Bundle;
import android.os.storage.StorageEventListener;
import android.os.storage.VolumeInfo;
import android.util.Log;
import com.android.settings.R;
import java.util.Objects;
/* loaded from: classes.dex */
public class StorageWizardReady extends StorageWizardBase {
    private final StorageEventListener mStorageMountListener = new StorageEventListener() { // from class: com.android.settings.deviceinfo.StorageWizardReady.1
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            Log.d("StorageWizardReady", "onVolumeStateChanged, disk : " + vol.getDiskId() + ", type : " + vol.getType() + ", state : " + vol.getState());
            if (Objects.equals(StorageWizardReady.this.mDisk.getId(), vol.getDiskId()) && vol.getType() == 0 && newState == 2) {
                StorageWizardReady.this.setIllustrationType(2);
                StorageWizardReady.this.setBodyText(R.string.storage_wizard_ready_external_body, StorageWizardReady.this.mDisk.getDescription());
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.deviceinfo.StorageWizardBase, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mDisk == null) {
            finish();
            return;
        }
        setContentView(R.layout.storage_wizard_generic);
        setHeaderText(R.string.storage_wizard_ready_title, this.mDisk.getDescription());
        VolumeInfo publicVol = findFirstVolume(0);
        VolumeInfo privateVol = findFirstVolume(1);
        Log.d("StorageWizardReady", "onCreate(), publicVol : " + publicVol + " privateVol : " + privateVol);
        if (publicVol != null) {
            setIllustrationType(2);
            setBodyText(R.string.storage_wizard_ready_external_body, this.mDisk.getDescription());
        } else if (privateVol != null) {
            setIllustrationType(1);
            setBodyText(R.string.storage_wizard_ready_internal_body, this.mDisk.getDescription());
        }
        getNextButton().setText(R.string.done);
        this.mStorage.registerListener(this.mStorageMountListener);
    }

    @Override // com.android.settings.deviceinfo.StorageWizardBase
    public void onNavigateNext() {
        finishAffinity();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.deviceinfo.StorageWizardBase, android.app.Activity
    public void onDestroy() {
        this.mStorage.unregisterListener(this.mStorageMountListener);
        super.onDestroy();
    }
}
