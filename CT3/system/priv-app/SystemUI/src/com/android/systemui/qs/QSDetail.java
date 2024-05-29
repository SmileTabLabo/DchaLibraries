package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Animatable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.phone.BaseStatusBarHeader;
import com.android.systemui.statusbar.phone.QSTileHost;
/* loaded from: a.zip:com/android/systemui/qs/QSDetail.class */
public class QSDetail extends LinearLayout {
    private QSDetailClipper mClipper;
    private boolean mClosingDetail;
    private QSTile.DetailAdapter mDetailAdapter;
    private ViewGroup mDetailContent;
    private TextView mDetailDoneButton;
    private TextView mDetailSettingsButton;
    private final SparseArray<View> mDetailViews;
    private boolean mFullyExpanded;
    private BaseStatusBarHeader mHeader;
    private final AnimatorListenerAdapter mHideGridContentWhenDone;
    private QSTileHost mHost;
    private int mOpenX;
    private int mOpenY;
    private View mQsDetailHeader;
    private View mQsDetailHeaderBack;
    private ImageView mQsDetailHeaderProgress;
    private Switch mQsDetailHeaderSwitch;
    private TextView mQsDetailHeaderTitle;
    private QSPanel mQsPanel;
    private final QSPanel.Callback mQsPanelCallback;
    private boolean mScanState;
    private final AnimatorListenerAdapter mTeardownDetailWhenDone;
    private boolean mTriggeredExpand;

    /* renamed from: com.android.systemui.qs.QSDetail$1  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/qs/QSDetail$1.class */
    class AnonymousClass1 implements QSPanel.Callback {
        final QSDetail this$0;

        AnonymousClass1(QSDetail qSDetail) {
            this.this$0 = qSDetail;
        }

        @Override // com.android.systemui.qs.QSPanel.Callback
        public void onScanStateChanged(boolean z) {
            this.this$0.post(new Runnable(this, z) { // from class: com.android.systemui.qs.QSDetail.1.3
                final AnonymousClass1 this$1;
                final boolean val$state;

                {
                    this.this$1 = this;
                    this.val$state = z;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.handleScanStateChanged(this.val$state);
                }
            });
        }

        @Override // com.android.systemui.qs.QSPanel.Callback
        public void onShowingDetail(QSTile.DetailAdapter detailAdapter, int i, int i2) {
            this.this$0.post(new Runnable(this, detailAdapter, i, i2) { // from class: com.android.systemui.qs.QSDetail.1.2
                final AnonymousClass1 this$1;
                final QSTile.DetailAdapter val$detail;
                final int val$x;
                final int val$y;

                {
                    this.this$1 = this;
                    this.val$detail = detailAdapter;
                    this.val$x = i;
                    this.val$y = i2;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.handleShowingDetail(this.val$detail, this.val$x, this.val$y);
                }
            });
        }

        @Override // com.android.systemui.qs.QSPanel.Callback
        public void onToggleStateChanged(boolean z) {
            this.this$0.post(new Runnable(this, z) { // from class: com.android.systemui.qs.QSDetail.1.1
                final AnonymousClass1 this$1;
                final boolean val$state;

                {
                    this.this$1 = this;
                    this.val$state = z;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.handleToggleStateChanged(this.val$state);
                }
            });
        }
    }

    public QSDetail(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mDetailViews = new SparseArray<>();
        this.mQsPanelCallback = new AnonymousClass1(this);
        this.mHideGridContentWhenDone = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.qs.QSDetail.2
            final QSDetail this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                animator.removeListener(this);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.this$0.mDetailAdapter != null) {
                    this.this$0.mQsPanel.setGridContentVisibility(false);
                    this.this$0.mHeader.setVisibility(4);
                }
            }
        };
        this.mTeardownDetailWhenDone = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.qs.QSDetail.3
            final QSDetail this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mDetailContent.removeAllViews();
                this.this$0.setVisibility(4);
                this.this$0.mClosingDetail = false;
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleScanStateChanged(boolean z) {
        if (this.mScanState == z) {
            return;
        }
        this.mScanState = z;
        Animatable animatable = (Animatable) this.mQsDetailHeaderProgress.getDrawable();
        if (z) {
            this.mQsDetailHeaderProgress.animate().alpha(1.0f);
            animatable.start();
            return;
        }
        this.mQsDetailHeaderProgress.animate().alpha(0.0f);
        animatable.stop();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleShowingDetail(QSTile.DetailAdapter detailAdapter, int i, int i2) {
        AnimatorListenerAdapter animatorListenerAdapter;
        boolean z = detailAdapter != null;
        setClickable(z);
        if (z) {
            this.mQsDetailHeaderTitle.setText(detailAdapter.getTitle());
            Boolean toggleState = detailAdapter.getToggleState();
            if (toggleState == null) {
                this.mQsDetailHeaderSwitch.setVisibility(4);
                this.mQsDetailHeader.setClickable(false);
            } else {
                this.mQsDetailHeaderSwitch.setVisibility(0);
                this.mQsDetailHeaderSwitch.setChecked(toggleState.booleanValue());
                this.mQsDetailHeader.setClickable(true);
                this.mQsDetailHeader.setOnClickListener(new View.OnClickListener(this, detailAdapter) { // from class: com.android.systemui.qs.QSDetail.5
                    final QSDetail this$0;
                    final QSTile.DetailAdapter val$adapter;

                    {
                        this.this$0 = this;
                        this.val$adapter = detailAdapter;
                    }

                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        boolean z2 = !this.this$0.mQsDetailHeaderSwitch.isChecked();
                        this.this$0.mQsDetailHeaderSwitch.setChecked(z2);
                        this.val$adapter.setToggleState(z2);
                    }
                });
            }
            if (this.mFullyExpanded) {
                this.mTriggeredExpand = false;
            } else {
                this.mTriggeredExpand = true;
                this.mHost.animateToggleQSExpansion();
            }
            this.mOpenX = i;
            this.mOpenY = i2;
        } else {
            int i3 = this.mOpenX;
            int i4 = this.mOpenY;
            i = i3;
            i2 = i4;
            if (this.mTriggeredExpand) {
                this.mHost.animateToggleQSExpansion();
                this.mTriggeredExpand = false;
                i = i3;
                i2 = i4;
            }
        }
        boolean z2 = (this.mDetailAdapter != null) != (detailAdapter != null);
        if (z2 || this.mDetailAdapter != detailAdapter) {
            if (detailAdapter != null) {
                int metricsCategory = detailAdapter.getMetricsCategory();
                View createDetailView = detailAdapter.createDetailView(this.mContext, this.mDetailViews.get(metricsCategory), this.mDetailContent);
                if (createDetailView == null) {
                    throw new IllegalStateException("Must return detail view");
                }
                Intent settingsIntent = detailAdapter.getSettingsIntent();
                this.mDetailSettingsButton.setVisibility(settingsIntent != null ? 0 : 8);
                this.mDetailSettingsButton.setOnClickListener(new View.OnClickListener(this, settingsIntent) { // from class: com.android.systemui.qs.QSDetail.6
                    final QSDetail this$0;
                    final Intent val$settingsIntent;

                    {
                        this.this$0 = this;
                        this.val$settingsIntent = settingsIntent;
                    }

                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        this.this$0.mHost.startActivityDismissingKeyguard(this.val$settingsIntent);
                    }
                });
                this.mDetailContent.removeAllViews();
                this.mDetailContent.addView(createDetailView);
                this.mDetailViews.put(metricsCategory, createDetailView);
                MetricsLogger.visible(this.mContext, detailAdapter.getMetricsCategory());
                announceForAccessibility(this.mContext.getString(2131493728, detailAdapter.getTitle()));
                this.mDetailAdapter = detailAdapter;
                animatorListenerAdapter = this.mHideGridContentWhenDone;
                setVisibility(0);
            } else {
                if (this.mDetailAdapter != null) {
                    MetricsLogger.hidden(this.mContext, this.mDetailAdapter.getMetricsCategory());
                }
                this.mClosingDetail = true;
                this.mDetailAdapter = null;
                animatorListenerAdapter = this.mTeardownDetailWhenDone;
                this.mHeader.setVisibility(0);
                this.mQsPanel.setGridContentVisibility(true);
                this.mQsPanelCallback.onScanStateChanged(false);
            }
            sendAccessibilityEvent(32);
            if (z2) {
                if (!this.mFullyExpanded && this.mDetailAdapter == null) {
                    animate().alpha(0.0f).setDuration(300L).setListener(animatorListenerAdapter).start();
                    return;
                }
                setAlpha(1.0f);
                this.mClipper.animateCircularClip(i, i2, this.mDetailAdapter != null, animatorListenerAdapter);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleToggleStateChanged(boolean z) {
        this.mQsDetailHeaderSwitch.setChecked(z);
    }

    private void updateDetailText() {
        this.mDetailDoneButton.setText(2131493564);
        this.mDetailSettingsButton.setText(2131493563);
    }

    public boolean isClosingDetail() {
        return this.mClosingDetail;
    }

    public boolean isShowingDetail() {
        return this.mDetailAdapter != null;
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        FontSizeUtils.updateFontSize(this.mDetailDoneButton, 2131689851);
        FontSizeUtils.updateFontSize(this.mDetailSettingsButton, 2131689851);
        for (int i = 0; i < this.mDetailViews.size(); i++) {
            this.mDetailViews.valueAt(i).dispatchConfigurationChanged(configuration);
        }
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDetailContent = (ViewGroup) findViewById(16908290);
        this.mDetailSettingsButton = (TextView) findViewById(16908314);
        this.mDetailDoneButton = (TextView) findViewById(16908313);
        this.mQsDetailHeader = findViewById(2131886581);
        this.mQsDetailHeaderBack = this.mQsDetailHeader.findViewById(16908363);
        this.mQsDetailHeaderTitle = (TextView) this.mQsDetailHeader.findViewById(16908310);
        this.mQsDetailHeaderSwitch = (Switch) this.mQsDetailHeader.findViewById(16908311);
        this.mQsDetailHeaderProgress = (ImageView) findViewById(2131886582);
        updateDetailText();
        this.mClipper = new QSDetailClipper(this);
        View.OnClickListener onClickListener = new View.OnClickListener(this) { // from class: com.android.systemui.qs.QSDetail.4
            final QSDetail this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.announceForAccessibility(this.this$0.mContext.getString(2131493444));
                this.this$0.mQsPanel.closeDetail();
            }
        };
        this.mQsDetailHeaderBack.setOnClickListener(onClickListener);
        this.mDetailDoneButton.setOnClickListener(onClickListener);
    }

    public void setExpanded(boolean z) {
        if (z) {
            return;
        }
        this.mTriggeredExpand = false;
    }

    public void setFullyExpanded(boolean z) {
        this.mFullyExpanded = z;
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
    }

    public void setQsPanel(QSPanel qSPanel, BaseStatusBarHeader baseStatusBarHeader) {
        this.mQsPanel = qSPanel;
        this.mHeader = baseStatusBarHeader;
        this.mHeader.setCallback(this.mQsPanelCallback);
        this.mQsPanel.setCallback(this.mQsPanelCallback);
    }
}
