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

    public BottomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mRequiringScroll = false;
        this.mCheckScrollRunnable = new Runnable() { // from class: com.android.setupwizardlib.view.BottomScrollView.1
            @Override // java.lang.Runnable
            public void run() {
                BottomScrollView.this.checkScroll();
            }
        };
    }

    public BottomScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mRequiringScroll = false;
        this.mCheckScrollRunnable = new Runnable() { // from class: com.android.setupwizardlib.view.BottomScrollView.1
            @Override // java.lang.Runnable
            public void run() {
                BottomScrollView.this.checkScroll();
            }
        };
    }

    public int getScrollThreshold() {
        return this.mScrollThreshold;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.widget.ScrollView, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        View child = getChildAt(0);
        if (child != null) {
            this.mScrollThreshold = Math.max(0, ((child.getMeasuredHeight() - b) + t) - getPaddingBottom());
        }
        if (b - t <= 0) {
            return;
        }
        post(this.mCheckScrollRunnable);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (oldt == t) {
            return;
        }
        checkScroll();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkScroll() {
        if (this.mListener == null) {
            return;
        }
        if (getScrollY() >= this.mScrollThreshold) {
            this.mListener.onScrolledToBottom();
        } else if (this.mRequiringScroll) {
        } else {
            this.mRequiringScroll = true;
            this.mListener.onRequiresScroll();
        }
    }
}
