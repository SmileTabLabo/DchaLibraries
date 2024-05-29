package com.android.settings.password;

import android.content.Context;
import android.content.Intent;
/* loaded from: classes.dex */
public class ManagedLockPasswordProvider {
    public static ManagedLockPasswordProvider get(Context context, int i) {
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
    public CharSequence getPickerOptionTitle(boolean z) {
        return "";
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Intent createIntent(boolean z, String str) {
        return null;
    }
}
