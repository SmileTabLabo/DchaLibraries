package com.android.settings;

import android.content.Context;
import android.content.Intent;
/* loaded from: classes.dex */
public class ManagedLockPasswordProvider {
    /* JADX INFO: Access modifiers changed from: package-private */
    public static ManagedLockPasswordProvider get(Context context, int userId) {
        return new ManagedLockPasswordProvider();
    }

    protected ManagedLockPasswordProvider() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isSettingManagedPasswordSupported() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isManagedPasswordChoosable() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String getPickerOptionTitle(boolean forFingerprint) {
        return "";
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getResIdForLockUnlockScreen(boolean forProfile) {
        return forProfile ? R.xml.security_settings_password_profile : R.xml.security_settings_password;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getResIdForLockUnlockSubScreen() {
        return R.xml.security_settings_password_sub;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Intent createIntent(boolean requirePasswordToDecrypt, String password) {
        return null;
    }
}
