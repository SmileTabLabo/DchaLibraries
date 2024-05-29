package com.android.systemui.qs;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import com.android.systemui.statusbar.policy.Listenable;
/* loaded from: a.zip:com/android/systemui/qs/GlobalSetting.class */
public abstract class GlobalSetting extends ContentObserver implements Listenable {
    private final Context mContext;
    private final String mSettingName;

    public GlobalSetting(Context context, Handler handler, String str) {
        super(handler);
        this.mContext = context;
        this.mSettingName = str;
    }

    public int getValue() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), this.mSettingName, 0);
    }

    protected abstract void handleValueChanged(int i);

    @Override // android.database.ContentObserver
    public void onChange(boolean z) {
        handleValueChanged(getValue());
    }

    @Override // com.android.systemui.statusbar.policy.Listenable
    public void setListening(boolean z) {
        if (z) {
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(this.mSettingName), false, this);
        } else {
            this.mContext.getContentResolver().unregisterContentObserver(this);
        }
    }
}
