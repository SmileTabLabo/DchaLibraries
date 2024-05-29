package android.support.v4.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;
/* loaded from: a.zip:android/support/v4/widget/ContentLoadingProgressBar.class */
public class ContentLoadingProgressBar extends ProgressBar {
    private final Runnable mDelayedHide;
    private final Runnable mDelayedShow;
    private boolean mDismissed;
    private boolean mPostedHide;
    private boolean mPostedShow;
    private long mStartTime;

    public ContentLoadingProgressBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet, 0);
        this.mStartTime = -1L;
        this.mPostedHide = false;
        this.mPostedShow = false;
        this.mDismissed = false;
        this.mDelayedHide = new Runnable(this) { // from class: android.support.v4.widget.ContentLoadingProgressBar.1
            final ContentLoadingProgressBar this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mPostedHide = false;
                this.this$0.mStartTime = -1L;
                this.this$0.setVisibility(8);
            }
        };
        this.mDelayedShow = new Runnable(this) { // from class: android.support.v4.widget.ContentLoadingProgressBar.2
            final ContentLoadingProgressBar this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mPostedShow = false;
                if (this.this$0.mDismissed) {
                    return;
                }
                this.this$0.mStartTime = System.currentTimeMillis();
                this.this$0.setVisibility(0);
            }
        };
    }

    private void removeCallbacks() {
        removeCallbacks(this.mDelayedHide);
        removeCallbacks(this.mDelayedShow);
    }

    @Override // android.widget.ProgressBar, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks();
    }

    @Override // android.widget.ProgressBar, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks();
    }
}
