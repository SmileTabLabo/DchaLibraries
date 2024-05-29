package com.android.settings.inputmethod;

import android.content.Context;
import android.support.v14.preference.SwitchPreference;
/* loaded from: classes.dex */
class SwitchWithNoTextPreference extends SwitchPreference {
    /* JADX INFO: Access modifiers changed from: package-private */
    public SwitchWithNoTextPreference(Context context) {
        super(context);
        setSwitchTextOn("");
        setSwitchTextOff("");
    }
}
