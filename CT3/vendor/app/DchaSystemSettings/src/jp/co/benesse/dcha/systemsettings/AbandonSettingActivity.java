package jp.co.benesse.dcha.systemsettings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/AbandonSettingActivity.class */
public class AbandonSettingActivity extends ParentSettingActivity implements View.OnClickListener {
    private ImageView mNoBtn;
    private ImageView mYesBtn;

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Logger.d("AbandonSettingActivity", "onClick 0001");
        this.mNoBtn.setClickable(false);
        this.mYesBtn.setClickable(false);
        if (view.getId() == this.mNoBtn.getId()) {
            Logger.d("AbandonSettingActivity", "onClick 0002");
            finish();
        } else if (view.getId() == this.mYesBtn.getId()) {
            Logger.d("AbandonSettingActivity", "onClick 0003");
            Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
            intent.putExtra("Terminate", "sY4r50Og");
            sendBroadcast(intent);
            finish();
        }
        Logger.d("AbandonSettingActivity", "onClick 0004");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.systemsettings.ParentSettingActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        Logger.d("AbandonSettingActivity", "onCreate 0001");
        super.onCreate(bundle);
        setContentView(2130903040);
        this.mYesBtn = (ImageView) findViewById(2131361792);
        this.mNoBtn = (ImageView) findViewById(2131361793);
        this.mYesBtn.setOnClickListener(this);
        this.mNoBtn.setOnClickListener(this);
        Logger.d("AbandonSettingActivity", "onCreate 0002");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.systemsettings.ParentSettingActivity, android.app.Activity
    public void onDestroy() {
        Logger.d("AbandonSettingActivity", "onDestroy 0001");
        super.onDestroy();
        this.mYesBtn.setOnClickListener(null);
        this.mNoBtn.setOnClickListener(null);
        this.mYesBtn = null;
        this.mNoBtn = null;
        Logger.d("AbandonSettingActivity", "onDestroy 0002");
    }
}
