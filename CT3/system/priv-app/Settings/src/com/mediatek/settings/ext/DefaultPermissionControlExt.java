package com.mediatek.settings.ext;

import android.content.Context;
import android.content.ContextWrapper;
import android.provider.SearchIndexableData;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;
import java.util.List;
/* loaded from: classes.dex */
public class DefaultPermissionControlExt extends ContextWrapper implements IPermissionControlExt {
    private static final String TAG = "DefaultPermissionControlExt";

    public DefaultPermissionControlExt(Context context) {
        super(context);
    }

    @Override // com.mediatek.settings.ext.IPermissionControlExt
    public void addPermSwitchPrf(PreferenceGroup prefGroup) {
        Log.d(TAG, "will not add permission preference");
    }

    @Override // com.mediatek.settings.ext.IPermissionControlExt
    public void enablerResume() {
        Log.d(TAG, "enablerResume() default");
    }

    @Override // com.mediatek.settings.ext.IPermissionControlExt
    public void enablerPause() {
        Log.d(TAG, "enablerPause() default");
    }

    @Override // com.mediatek.settings.ext.IPermissionControlExt
    public void addAutoBootPrf(PreferenceGroup prefGroup) {
        Log.d(TAG, "will not add auto boot entry preference");
    }

    @Override // com.mediatek.settings.ext.IPermissionControlExt
    public List<SearchIndexableData> getRawDataToIndex(boolean enabled) {
        Log.d(TAG, "default , null");
        return null;
    }
}
