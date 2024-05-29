package com.android.setupwizardlib.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.android.setupwizardlib.DividerItemDecoration;
/* loaded from: classes.dex */
class ItemViewHolder extends RecyclerView.ViewHolder implements DividerItemDecoration.DividedViewHolder {
    private boolean mIsEnabled;
    private IItem mItem;

    public ItemViewHolder(View itemView) {
        super(itemView);
    }

    @Override // com.android.setupwizardlib.DividerItemDecoration.DividedViewHolder
    public boolean isDividerAllowedAbove() {
        return this.mIsEnabled;
    }

    @Override // com.android.setupwizardlib.DividerItemDecoration.DividedViewHolder
    public boolean isDividerAllowedBelow() {
        return this.mIsEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.mIsEnabled = isEnabled;
        this.itemView.setClickable(isEnabled);
        this.itemView.setEnabled(isEnabled);
        this.itemView.setFocusable(isEnabled);
    }

    public void setItem(IItem item) {
        this.mItem = item;
    }

    public IItem getItem() {
        return this.mItem;
    }
}
