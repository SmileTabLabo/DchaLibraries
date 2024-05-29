package com.android.systemui.qs;

import android.app.ActivityManager;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import com.android.systemui.statusbar.policy.Listenable;
/* loaded from: a.zip:com/android/systemui/qs/SecureSetting.class */
public abstract class SecureSetting extends ContentObserver implements Listenable {
    private final Context mContext;
    private boolean mListening;
    private int mObservedValue;
    private final String mSettingName;
    private int mUserId;

    public SecureSetting(Context context, Handler handler, String str) {
        super(handler);
        this.mObservedValue = 0;
        this.mContext = context;
        this.mSettingName = str;
        this.mUserId = ActivityManager.getCurrentUser();
    }

    public int getValue() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), this.mSettingName, 0, this.mUserId);
    }

    protected abstract void handleValueChanged(int i, boolean z);

    @Override // android.database.ContentObserver
    public void onChange(boolean z) {
        int value = getValue();
        handleValueChanged(value, value != this.mObservedValue);
        this.mObservedValue = value;
    }

    @Override // com.android.systemui.statusbar.policy.Listenable
    public void setListening(boolean z) {
        if (z == this.mListening) {
            return;
        }
        this.mListening = z;
        if (z) {
            this.mObservedValue = getValue();
            this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(this.mSettingName), false, this, this.mUserId);
            return;
        }
        this.mContext.getContentResolver().unregisterContentObserver(this);
        this.mObservedValue = 0;
    }

    public void setUserId(int i) {
        this.mUserId = i;
        if (this.mListening) {
            setListening(false);
            setListening(true);
        }
    }

    public void setValue(int i) {
        Settings.Secure.putIntForUser(this.mContext.getContentResolver(), this.mSettingName, i, this.mUserId);
    }
}
