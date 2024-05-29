package com.panasonic.sanyo.ts.firmwareupdate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
/* loaded from: com.zip:com/panasonic/sanyo/ts/firmwareupdate/FirmwareUpdateActivity.class */
public class FirmwareUpdateActivity extends BaseActivity {
    private Button FirmUpButton;

    @Override // com.panasonic.sanyo.ts.firmwareupdate.BaseActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(2130903040);
        this.FirmUpButton = (Button) findViewById(2131099649);
        this.FirmUpButton.setOnClickListener(new View.OnClickListener(this) { // from class: com.panasonic.sanyo.ts.firmwareupdate.FirmwareUpdateActivity.1
            final FirmwareUpdateActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.UpdateCancel = false;
                this.this$0.startprogress();
            }
        });
    }
}
