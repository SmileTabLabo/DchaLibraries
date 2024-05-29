package com.panasonic.sanyo.ts.firmwareupdate;
/* loaded from: com.zip:com/panasonic/sanyo/ts/firmwareupdate/FirmwareUpdateSilentActivity.class */
public class FirmwareUpdateSilentActivity extends BaseActivity {
    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.panasonic.sanyo.ts.firmwareupdate.BaseActivity, android.app.Activity
    public void onPause() {
        super.onPause();
        finish();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.panasonic.sanyo.ts.firmwareupdate.BaseActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        this.UpdateCancel = false;
        this.SDPath = getIntent().getData().getPath();
        startprogress();
    }
}
