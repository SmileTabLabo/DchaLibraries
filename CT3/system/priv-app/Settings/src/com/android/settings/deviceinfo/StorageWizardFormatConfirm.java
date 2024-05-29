package com.android.settings.deviceinfo;

import android.content.Intent;
import android.os.Bundle;
import com.android.settings.R;
/* loaded from: classes.dex */
public class StorageWizardFormatConfirm extends StorageWizardBase {
    private boolean mFormatPrivate;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.deviceinfo.StorageWizardBase, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mDisk == null) {
            finish();
            return;
        }
        setContentView(R.layout.storage_wizard_generic);
        this.mFormatPrivate = getIntent().getBooleanExtra("format_private", false);
        setIllustrationType(this.mFormatPrivate ? 1 : 2);
        if (this.mFormatPrivate) {
            setHeaderText(R.string.storage_wizard_format_confirm_title, new String[0]);
            setBodyText(R.string.storage_wizard_format_confirm_body, this.mDisk.getDescription());
        } else {
            setHeaderText(R.string.storage_wizard_format_confirm_public_title, new String[0]);
            setBodyText(R.string.storage_wizard_format_confirm_public_body, this.mDisk.getDescription());
        }
        getNextButton().setText(R.string.storage_wizard_format_confirm_next);
        getNextButton().setBackgroundTintList(getColorStateList(R.color.storage_wizard_button_red));
    }

    @Override // com.android.settings.deviceinfo.StorageWizardBase
    public void onNavigateNext() {
        Intent intent = new Intent(this, StorageWizardFormatProgress.class);
        intent.putExtra("android.os.storage.extra.DISK_ID", this.mDisk.getId());
        intent.putExtra("format_private", this.mFormatPrivate);
        intent.putExtra("forget_uuid", getIntent().getStringExtra("forget_uuid"));
        startActivity(intent);
        finishAffinity();
    }
}
