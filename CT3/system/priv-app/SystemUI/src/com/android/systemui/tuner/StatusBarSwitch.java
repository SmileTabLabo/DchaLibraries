package com.android.systemui.tuner;

import android.app.ActivityManager;
import android.content.Context;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService;
import java.util.Set;
/* loaded from: a.zip:com/android/systemui/tuner/StatusBarSwitch.class */
public class StatusBarSwitch extends SwitchPreference implements TunerService.Tunable {
    private Set<String> mBlacklist;

    public StatusBarSwitch(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void setList(Set<String> set) {
        Settings.Secure.putStringForUser(getContext().getContentResolver(), "icon_blacklist", TextUtils.join(",", set), ActivityManager.getCurrentUser());
    }

    @Override // android.support.v7.preference.Preference
    public void onAttached() {
        super.onAttached();
        TunerService.get(getContext()).addTunable(this, "icon_blacklist");
    }

    @Override // android.support.v7.preference.Preference
    public void onDetached() {
        TunerService.get(getContext()).removeTunable(this);
        super.onDetached();
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("icon_blacklist".equals(str)) {
            this.mBlacklist = StatusBarIconController.getIconBlacklist(str2);
            setChecked(!this.mBlacklist.contains(getKey()));
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public boolean persistBoolean(boolean z) {
        if (z) {
            if (this.mBlacklist.remove(getKey())) {
                MetricsLogger.action(getContext(), 233, getKey());
                setList(this.mBlacklist);
                return true;
            }
            return true;
        } else if (this.mBlacklist.contains(getKey())) {
            return true;
        } else {
            MetricsLogger.action(getContext(), 234, getKey());
            this.mBlacklist.add(getKey());
            setList(this.mBlacklist);
            return true;
        }
    }
}
