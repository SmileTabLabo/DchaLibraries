package com.android.systemui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ImageView;
import com.android.internal.logging.MetricsLogger;
/* loaded from: a.zip:com/android/systemui/settings/BrightnessDialog.class */
public class BrightnessDialog extends Activity {
    private BrightnessController mBrightnessController;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Window window = getWindow();
        window.setGravity(48);
        window.clearFlags(2);
        window.requestFeature(1);
        setContentView(2130968772);
        this.mBrightnessController = new BrightnessController(this, (ImageView) findViewById(2131886591), (ToggleSlider) findViewById(2131886592));
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 25 || i == 24 || i == 164) {
            finish();
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        this.mBrightnessController.registerCallbacks();
        MetricsLogger.visible(this, 220);
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
        MetricsLogger.hidden(this, 220);
        this.mBrightnessController.unregisterCallbacks();
    }
}
