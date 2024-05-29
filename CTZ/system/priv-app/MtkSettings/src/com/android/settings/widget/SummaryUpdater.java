package com.android.settings.widget;

import android.content.Context;
import android.text.TextUtils;
/* loaded from: classes.dex */
public abstract class SummaryUpdater {
    protected final Context mContext;
    private final OnSummaryChangeListener mListener;
    private String mSummary;

    /* loaded from: classes.dex */
    public interface OnSummaryChangeListener {
        void onSummaryChanged(String str);
    }

    protected abstract String getSummary();

    public SummaryUpdater(Context context, OnSummaryChangeListener onSummaryChangeListener) {
        this.mContext = context;
        this.mListener = onSummaryChangeListener;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void notifyChangeIfNeeded() {
        String summary = getSummary();
        if (!TextUtils.equals(this.mSummary, summary)) {
            this.mSummary = summary;
            if (this.mListener != null) {
                this.mListener.onSummaryChanged(summary);
            }
        }
    }
}
