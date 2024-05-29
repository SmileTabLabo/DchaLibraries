package com.android.settings;

import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.View;
import android.widget.ImageView;
/* loaded from: classes.dex */
public class CancellablePreference extends Preference implements View.OnClickListener {
    private boolean mCancellable;
    private OnCancelListener mListener;

    /* loaded from: classes.dex */
    public interface OnCancelListener {
        void onCancel(CancellablePreference cancellablePreference);
    }

    public void setCancellable(boolean isCancellable) {
        this.mCancellable = isCancellable;
        notifyChanged();
    }

    public void setOnCancelListener(OnCancelListener listener) {
        this.mListener = listener;
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        ImageView cancel = (ImageView) view.findViewById(R.id.cancel);
        cancel.setVisibility(this.mCancellable ? 0 : 4);
        cancel.setOnClickListener(this);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        if (this.mListener == null) {
            return;
        }
        this.mListener.onCancel(this);
    }
}
