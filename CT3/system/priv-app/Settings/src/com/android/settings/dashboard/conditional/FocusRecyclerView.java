package com.android.settings.dashboard.conditional;

import android.content.Context;
import android.support.annotation.Nullable;
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

    public FocusRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override // android.view.View
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (this.mListener == null) {
            return;
        }
        this.mListener.onWindowFocusChanged(hasWindowFocus);
    }

    public void setListener(FocusListener listener) {
        this.mListener = listener;
    }

    @Override // android.support.v7.widget.RecyclerView, android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mDetachListener == null) {
            return;
        }
        this.mDetachListener.onDetachedFromWindow();
    }

    public void setDetachListener(DetachListener detachListener) {
        this.mDetachListener = detachListener;
    }
}
