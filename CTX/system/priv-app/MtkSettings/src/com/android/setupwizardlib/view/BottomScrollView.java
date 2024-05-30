package com.android.setupwizardlib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;
/* loaded from: classes.dex */
public class BottomScrollView extends ScrollView {
    private final Runnable mCheckScrollRunnable;
    private BottomScrollListener mListener;
    private boolean mRequiringScroll;
    private int mScrollThreshold;

    /* loaded from: classes.dex */
    public interface BottomScrollListener {
        void onRequiresScroll();

        void onScrolledToBottom();
    }

    public BottomScrollView(Context context) {
        super(context);
        this.mRequiringScroll = false;
        this.mCheckScrollRunnable = new Runnable() { // from class: com.android.setupwizardlib.view.BottomScrollView.1
            @Override // java.lang.Runnable
            public void run() {
                BottomScrollView.this.checkScroll();
            }
        };
    }

    public BottomScrollView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRequiringScroll = false;
        this.mCheckScrollRunnable = new Runnable() { // from class: com.android.setupwizardlib.view.BottomScrollView.1
            @Override // java.lang.Runnable
            public void run() {
                BottomScrollView.this.checkScroll();
            }
        };
    }

    public BottomScrollView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mRequiringScroll = false;
        this.mCheckScrollRunnable = new Runnable() { // from class: com.android.setupwizardlib.view.BottomScrollView.1
            @Override // java.lang.Runnable
            public void run() {
                BottomScrollView.this.checkScroll();
            }
        };
    }

    public void setBottomScrollListener(BottomScrollListener bottomScrollListener) {
        this.mListener = bottomScrollListener;
    }

    public int getScrollThreshold() {
        return this.mScrollThreshold;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.widget.ScrollView, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        View childAt = getChildAt(0);
        if (childAt != null) {
            this.mScrollThreshold = Math.max(0, ((childAt.getMeasuredHeight() - i4) + i2) - getPaddingBottom());
        }
        if (i4 - i2 > 0) {
            post(this.mCheckScrollRunnable);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onScrollChanged(int i, int i2, int i3, int i4) {
        super.onScrollChanged(i, i2, i3, i4);
        if (i4 != i2) {
            checkScroll();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkScroll() {
        if (this.mListener != null) {
            if (getScrollY() >= this.mScrollThreshold) {
                this.mListener.onScrolledToBottom();
            } else if (!this.mRequiringScroll) {
                this.mRequiringScroll = true;
                this.mListener.onRequiresScroll();
            }
        }
    }
}
