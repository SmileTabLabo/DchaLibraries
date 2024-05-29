package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import com.android.systemui.Interpolators;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.statusbar.phone.BaseStatusBarHeader;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.QSTileHost;
/* loaded from: a.zip:com/android/systemui/qs/QSContainer.class */
public class QSContainer extends FrameLayout {
    private final Animator.AnimatorListener mAnimateHeaderSlidingInListener;
    private long mDelay;
    protected BaseStatusBarHeader mHeader;
    private boolean mHeaderAnimating;
    private int mHeightOverride;
    private boolean mKeyguardShowing;
    private boolean mListening;
    private NotificationPanelView mPanelView;
    private QSAnimator mQSAnimator;
    private QSCustomizer mQSCustomizer;
    private QSDetail mQSDetail;
    protected QSPanel mQSPanel;
    private final Rect mQsBounds;
    private boolean mQsExpanded;
    protected float mQsExpansion;
    private final Point mSizePoint;
    private boolean mStackScrollerOverscrolling;
    private final ViewTreeObserver.OnPreDrawListener mStartHeaderSlidingIn;

    public QSContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSizePoint = new Point();
        this.mQsBounds = new Rect();
        this.mHeightOverride = -1;
        this.mStartHeaderSlidingIn = new ViewTreeObserver.OnPreDrawListener(this) { // from class: com.android.systemui.qs.QSContainer.1
            final QSContainer this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                this.this$0.getViewTreeObserver().removeOnPreDrawListener(this);
                this.this$0.animate().translationY(0.0f).setStartDelay(this.this$0.mDelay).setDuration(448L).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(this.this$0.mAnimateHeaderSlidingInListener).start();
                this.this$0.setY(-this.this$0.mHeader.getHeight());
                return true;
            }
        };
        this.mAnimateHeaderSlidingInListener = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.qs.QSContainer.2
            final QSContainer this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mHeaderAnimating = false;
                this.this$0.updateQsState();
            }
        };
    }

    private void updateBottom() {
        int calculateContainerHeight = calculateContainerHeight();
        setBottom(getTop() + calculateContainerHeight);
        this.mQSDetail.setBottom(getTop() + calculateContainerHeight);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateQsState() {
        boolean z = (this.mQsExpanded || this.mStackScrollerOverscrolling) ? true : this.mHeaderAnimating;
        this.mQSPanel.setExpanded(this.mQsExpanded);
        this.mQSDetail.setExpanded(this.mQsExpanded);
        this.mHeader.setVisibility((this.mQsExpanded || !this.mKeyguardShowing || this.mHeaderAnimating) ? 0 : 4);
        this.mHeader.setExpanded((!this.mKeyguardShowing || this.mHeaderAnimating) ? this.mQsExpanded && !this.mStackScrollerOverscrolling : true);
        this.mQSPanel.setVisibility(z ? 0 : 4);
    }

    public void animateHeaderSlidingIn(long j) {
        if (this.mQsExpanded) {
            return;
        }
        this.mHeaderAnimating = true;
        this.mDelay = j;
        getViewTreeObserver().addOnPreDrawListener(this.mStartHeaderSlidingIn);
    }

    public void animateHeaderSlidingOut() {
        this.mHeaderAnimating = true;
        animate().y(-this.mHeader.getHeight()).setStartDelay(0L).setDuration(360L).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.qs.QSContainer.3
            final QSContainer this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.animate().setListener(null);
                this.this$0.mHeaderAnimating = false;
                this.this$0.updateQsState();
            }
        }).start();
    }

    protected int calculateContainerHeight() {
        return this.mQSCustomizer.isCustomizing() ? this.mQSCustomizer.getHeight() : ((int) (this.mQsExpansion * ((this.mHeightOverride != -1 ? this.mHeightOverride : getMeasuredHeight()) - this.mHeader.getCollapsedHeight()))) + this.mHeader.getCollapsedHeight();
    }

    public QSCustomizer getCustomizer() {
        return this.mQSCustomizer;
    }

    public int getDesiredHeight() {
        return isCustomizing() ? getHeight() : this.mQSDetail.isClosingDetail() ? this.mQSPanel.getGridHeight() + this.mHeader.getCollapsedHeight() + getPaddingBottom() : getMeasuredHeight();
    }

    public BaseStatusBarHeader getHeader() {
        return this.mHeader;
    }

    public int getQsMinExpansionHeight() {
        return this.mHeader.getHeight();
    }

    public QSPanel getQsPanel() {
        return this.mQSPanel;
    }

    public boolean isCustomizing() {
        return this.mQSCustomizer.isCustomizing();
    }

    public boolean isShowingDetail() {
        return !this.mQSPanel.isShowingCustomize() ? this.mQSDetail.isShowingDetail() : true;
    }

    public void notifyCustomizeChanged() {
        updateBottom();
        this.mQSPanel.setVisibility(!this.mQSCustomizer.isCustomizing() ? 0 : 4);
        this.mHeader.setVisibility(!this.mQSCustomizer.isCustomizing() ? 0 : 4);
        this.mPanelView.onQsHeightChanged();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mQSPanel = (QSPanel) findViewById(2131886586);
        this.mQSDetail = (QSDetail) findViewById(2131886587);
        this.mHeader = (BaseStatusBarHeader) findViewById(2131886595);
        this.mQSDetail.setQsPanel(this.mQSPanel, this.mHeader);
        this.mQSAnimator = new QSAnimator(this, (QuickQSPanel) this.mHeader.findViewById(2131886602), this.mQSPanel);
        this.mQSCustomizer = (QSCustomizer) findViewById(2131886588);
        this.mQSCustomizer.setQsContainer(this);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateBottom();
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        this.mQSPanel.measure(i, View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i2), 0));
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(this.mQSPanel.getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(((FrameLayout.LayoutParams) this.mQSPanel.getLayoutParams()).topMargin + this.mQSPanel.getMeasuredHeight(), 1073741824));
        getDisplay().getRealSize(this.mSizePoint);
        this.mQSCustomizer.measure(i, View.MeasureSpec.makeMeasureSpec(this.mSizePoint.y, 1073741824));
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        this.mQSAnimator.onRtlChanged();
    }

    public void setExpanded(boolean z) {
        this.mQsExpanded = z;
        this.mQSPanel.setListening(this.mListening ? this.mQsExpanded : false);
        updateQsState();
    }

    public void setHeaderClickable(boolean z) {
        this.mHeader.setClickable(z);
    }

    public void setHeightOverride(int i) {
        this.mHeightOverride = i;
        updateBottom();
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mQSPanel.setHost(qSTileHost, this.mQSCustomizer);
        this.mHeader.setQSPanel(this.mQSPanel);
        this.mQSDetail.setHost(qSTileHost);
        this.mQSAnimator.setHost(qSTileHost);
    }

    public void setKeyguardShowing(boolean z) {
        this.mKeyguardShowing = z;
        this.mQSAnimator.setOnKeyguard(z);
        updateQsState();
    }

    public void setListening(boolean z) {
        this.mListening = z;
        this.mHeader.setListening(z);
        this.mQSPanel.setListening(this.mListening ? this.mQsExpanded : false);
    }

    public void setOverscrolling(boolean z) {
        this.mStackScrollerOverscrolling = z;
        updateQsState();
    }

    public void setPanelView(NotificationPanelView notificationPanelView) {
        this.mPanelView = notificationPanelView;
    }

    public void setQsExpansion(float f, float f2) {
        this.mQsExpansion = f;
        float f3 = f - 1.0f;
        if (!this.mHeaderAnimating) {
            if (this.mKeyguardShowing) {
                f2 = f3 * this.mHeader.getHeight();
            }
            setTranslationY(f2);
        }
        this.mHeader.setExpansion(this.mKeyguardShowing ? 1.0f : f);
        this.mQSPanel.setTranslationY(this.mQSPanel.getHeight() * f3);
        this.mQSDetail.setFullyExpanded(f == 1.0f);
        this.mQSAnimator.setPosition(f);
        updateBottom();
        this.mQsBounds.top = (int) ((1.0f - f) * this.mQSPanel.getHeight());
        this.mQsBounds.right = this.mQSPanel.getWidth();
        this.mQsBounds.bottom = this.mQSPanel.getHeight();
        this.mQSPanel.setClipBounds(this.mQsBounds);
    }
}
