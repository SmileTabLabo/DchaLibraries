package com.android.settings.utils;

import android.app.Activity;
import android.app.VoiceInteractor;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
/* loaded from: classes.dex */
public abstract class VoiceSettingsActivity extends Activity {
    protected abstract boolean onVoiceSettingInteraction(Intent intent);

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isVoiceInteractionRoot()) {
            if (!onVoiceSettingInteraction(getIntent())) {
                return;
            }
            finish();
            return;
        }
        Log.v("VoiceSettingsActivity", "Cannot modify settings without voice interaction");
        finish();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void notifySuccess(CharSequence prompt) {
        if (getVoiceInteractor() == null) {
            return;
        }
        getVoiceInteractor().submitRequest(new VoiceInteractor.CompleteVoiceRequest(prompt, null) { // from class: com.android.settings.utils.VoiceSettingsActivity.1
            @Override // android.app.VoiceInteractor.CompleteVoiceRequest
            public void onCompleteResult(Bundle options) {
                VoiceSettingsActivity.this.finish();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void notifyFailure(CharSequence prompt) {
        if (getVoiceInteractor() == null) {
            return;
        }
        getVoiceInteractor().submitRequest(new VoiceInteractor.AbortVoiceRequest(prompt, (Bundle) null));
    }
}
