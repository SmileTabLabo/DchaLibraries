package com.mediatek.settings.ext;

import android.content.Context;
import android.view.View;
/* loaded from: classes.dex */
public interface IDataUsageSummaryExt {
    boolean isAllowDataEnable(int i);

    void onBindViewHolder(Context context, View view, View.OnClickListener onClickListener);

    boolean onDisablingData(int i);
}
