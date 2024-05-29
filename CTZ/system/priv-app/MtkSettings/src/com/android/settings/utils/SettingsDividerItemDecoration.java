package com.android.settings.utils;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.RecyclerView;
import com.android.setupwizardlib.DividerItemDecoration;
/* loaded from: classes.dex */
public class SettingsDividerItemDecoration extends DividerItemDecoration {
    public SettingsDividerItemDecoration(Context context) {
        super(context);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.setupwizardlib.DividerItemDecoration
    public boolean isDividerAllowedAbove(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof PreferenceViewHolder) {
            return ((PreferenceViewHolder) viewHolder).isDividerAllowedAbove();
        }
        return super.isDividerAllowedAbove(viewHolder);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.setupwizardlib.DividerItemDecoration
    public boolean isDividerAllowedBelow(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof PreferenceViewHolder) {
            return ((PreferenceViewHolder) viewHolder).isDividerAllowedBelow();
        }
        return super.isDividerAllowedBelow(viewHolder);
    }
}
