package com.android.settings.deviceinfo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import com.android.settings.R;
/* loaded from: classes.dex */
public class StorageWizardMigrate extends StorageWizardBase {
    private MigrateEstimateTask mEstimate;
    private RadioButton mRadioLater;
    private final CompoundButton.OnCheckedChangeListener mRadioListener = new CompoundButton.OnCheckedChangeListener() { // from class: com.android.settings.deviceinfo.StorageWizardMigrate.1
        @Override // android.widget.CompoundButton.OnCheckedChangeListener
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isChecked) {
                return;
            }
            if (buttonView == StorageWizardMigrate.this.mRadioNow) {
                StorageWizardMigrate.this.mRadioLater.setChecked(false);
            } else if (buttonView == StorageWizardMigrate.this.mRadioLater) {
                StorageWizardMigrate.this.mRadioNow.setChecked(false);
            }
            StorageWizardMigrate.this.getNextButton().setEnabled(true);
        }
    };
    private RadioButton mRadioNow;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.deviceinfo.StorageWizardBase, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mDisk == null) {
            finish();
            return;
        }
        setContentView(R.layout.storage_wizard_migrate);
        setIllustrationType(1);
        setHeaderText(R.string.storage_wizard_migrate_title, this.mDisk.getDescription());
        setBodyText(R.string.memory_calculating_size, new String[0]);
        this.mRadioNow = (RadioButton) findViewById(R.id.storage_wizard_migrate_now);
        this.mRadioLater = (RadioButton) findViewById(R.id.storage_wizard_migrate_later);
        this.mRadioNow.setOnCheckedChangeListener(this.mRadioListener);
        this.mRadioLater.setOnCheckedChangeListener(this.mRadioListener);
        getNextButton().setEnabled(false);
        this.mEstimate = new MigrateEstimateTask(this) { // from class: com.android.settings.deviceinfo.StorageWizardMigrate.2
            @Override // com.android.settings.deviceinfo.MigrateEstimateTask
            public void onPostExecute(String size, String time) {
                StorageWizardMigrate.this.setBodyText(R.string.storage_wizard_migrate_body, StorageWizardMigrate.this.mDisk.getDescription(), time, size);
            }
        };
        this.mEstimate.copyFrom(getIntent());
        this.mEstimate.execute(new Void[0]);
    }

    @Override // com.android.settings.deviceinfo.StorageWizardBase
    public void onNavigateNext() {
        if (this.mRadioNow.isChecked()) {
            Intent intent = new Intent(this, StorageWizardMigrateConfirm.class);
            intent.putExtra("android.os.storage.extra.DISK_ID", this.mDisk.getId());
            this.mEstimate.copyTo(intent);
            startActivity(intent);
        } else if (!this.mRadioLater.isChecked()) {
        } else {
            Intent intent2 = new Intent(this, StorageWizardReady.class);
            intent2.putExtra("android.os.storage.extra.DISK_ID", this.mDisk.getId());
            startActivity(intent2);
        }
    }
}
