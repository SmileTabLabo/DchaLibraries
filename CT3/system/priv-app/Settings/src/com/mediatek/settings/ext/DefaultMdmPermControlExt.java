package com.mediatek.settings.ext;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;
/* loaded from: classes.dex */
public class DefaultMdmPermControlExt extends ContextWrapper implements IMdmPermissionControlExt {
    private static final String TAG = "DefaultMdmPermControlExt";

    public DefaultMdmPermControlExt(Context context) {
        super(context);
    }

    @Override // com.mediatek.settings.ext.IMdmPermissionControlExt
    public void addMdmPermCtrlPrf(PreferenceGroup prefGroup) {
        Log.d(TAG, "will not add mdm permission control");
    }
}
