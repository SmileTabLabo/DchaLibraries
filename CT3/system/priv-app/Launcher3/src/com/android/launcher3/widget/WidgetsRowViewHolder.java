package com.android.launcher3.widget;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
/* loaded from: a.zip:com/android/launcher3/widget/WidgetsRowViewHolder.class */
public class WidgetsRowViewHolder extends RecyclerView.ViewHolder {
    ViewGroup mContent;

    public WidgetsRowViewHolder(ViewGroup viewGroup) {
        super(viewGroup);
        this.mContent = viewGroup;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ViewGroup getContent() {
        return this.mContent;
    }
}
