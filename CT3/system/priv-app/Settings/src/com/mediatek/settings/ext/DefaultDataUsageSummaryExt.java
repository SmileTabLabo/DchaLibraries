package com.mediatek.settings.ext;

import android.content.Context;
import android.view.View;
/* loaded from: classes.dex */
public class DefaultDataUsageSummaryExt implements IDataUsageSummaryExt {
    public DefaultDataUsageSummaryExt(Context context) {
    }

    @Override // com.mediatek.settings.ext.IDataUsageSummaryExt
    public boolean onDisablingData(int subId) {
        return true;
    }

    @Override // com.mediatek.settings.ext.IDataUsageSummaryExt
    public boolean isAllowDataEnable(int subId) {
        return true;
    }

    @Override // com.mediatek.settings.ext.IDataUsageSummaryExt
    public void onBindViewHolder(Context context, View view, View.OnClickListener listener) {
    }
}
