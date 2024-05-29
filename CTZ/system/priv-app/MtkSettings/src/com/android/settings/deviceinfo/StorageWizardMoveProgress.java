package com.android.settings.deviceinfo;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;
/* loaded from: classes.dex */
public class StorageWizardMoveProgress extends StorageWizardBase {
    private final PackageManager.MoveCallback mCallback = new PackageManager.MoveCallback() { // from class: com.android.settings.deviceinfo.StorageWizardMoveProgress.1
        public void onStatusChanged(int i, int i2, long j) {
            if (StorageWizardMoveProgress.this.mMoveId != i) {
                return;
            }
            if (PackageManager.isMoveStatusFinished(i2)) {
                Log.d("StorageSettings", "Finished with status " + i2);
                if (i2 != -100) {
                    Toast.makeText(StorageWizardMoveProgress.this, StorageWizardMoveProgress.this.moveStatusToMessage(i2), 1).show();
                }
                StorageWizardMoveProgress.this.finishAffinity();
                return;
            }
            StorageWizardMoveProgress.this.setCurrentProgress(i2);
        }
    };
    private int mMoveId;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.deviceinfo.StorageWizardBase, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (this.mVolume == null) {
            finish();
            return;
        }
        setContentView(R.layout.storage_wizard_progress);
        this.mMoveId = getIntent().getIntExtra("android.content.pm.extra.MOVE_ID", -1);
        String stringExtra = getIntent().getStringExtra("android.intent.extra.TITLE");
        String bestVolumeDescription = this.mStorage.getBestVolumeDescription(this.mVolume);
        setIcon(R.drawable.ic_swap_horiz);
        setHeaderText(R.string.storage_wizard_move_progress_title, stringExtra);
        setBodyText(R.string.storage_wizard_move_progress_body, bestVolumeDescription, stringExtra);
        getPackageManager().registerMoveCallback(this.mCallback, new Handler());
        this.mCallback.onStatusChanged(this.mMoveId, getPackageManager().getMoveStatus(this.mMoveId), -1L);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.deviceinfo.StorageWizardBase, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        getPackageManager().unregisterMoveCallback(this.mCallback);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public CharSequence moveStatusToMessage(int i) {
        if (i == -8) {
            return getString(R.string.move_error_device_admin);
        }
        switch (i) {
            case -5:
                return getString(R.string.invalid_location);
            case -4:
                return getString(R.string.app_forward_locked);
            case -3:
                return getString(R.string.system_package);
            case -2:
                return getString(R.string.does_not_exist);
            case -1:
                return getString(R.string.insufficient_storage);
            default:
                return getString(R.string.insufficient_storage);
        }
    }
}
