package com.android.settings.dashboard.conditional;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
/* loaded from: classes.dex */
public class FocusRecyclerView extends RecyclerView {
    private DetachListener mDetachListener;
    private FocusListener mListener;

    /* loaded from: classes.dex */
    public interface DetachListener {
        void onDetachedFromWindow();
    }

    /* loaded from: classes.dex */
    public interface FocusListener {
        void onWindowFocusChanged(boolean z);
    }

    public FocusRecyclerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // android.view.View
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (this.mListener != null) {
            this.mListener.onWindowFocusChanged(z);
        }
    }

    public void setListener(FocusListener focusListener) {
        this.mListener = focusListener;
    }

    @Override // android.support.v7.widget.RecyclerView, android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mDetachListener != null) {
            this.mDetachListener.onDetachedFromWindow();
        }
    }

    public void setDetachListener(DetachListener detachListener) {
        this.mDetachListener = detachListener;
    }
}
