package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler;
/* loaded from: classes.dex */
public class QSFragment extends Fragment implements QS, CommandQueue.Callbacks {
    private QSContainerImpl mContainer;
    private long mDelay;
    private QSFooter mFooter;
    protected QuickStatusBarHeader mHeader;
    private boolean mHeaderAnimating;
    private boolean mKeyguardShowing;
    private int mLayoutDirection;
    private boolean mListening;
    private QS.HeightListener mPanelView;
    private QSAnimator mQSAnimator;
    private QSCustomizer mQSCustomizer;
    private QSDetail mQSDetail;
    protected QSPanel mQSPanel;
    private boolean mQsDisabled;
    private boolean mQsExpanded;
    private boolean mStackScrollerOverscrolling;
    private final Rect mQsBounds = new Rect();
    private float mLastQSExpansion = -1.0f;
    private RemoteInputQuickSettingsDisabler mRemoteInputQuickSettingsDisabler = (RemoteInputQuickSettingsDisabler) Dependency.get(RemoteInputQuickSettingsDisabler.class);
    private final ViewTreeObserver.OnPreDrawListener mStartHeaderSlidingIn = new ViewTreeObserver.OnPreDrawListener() { // from class: com.android.systemui.qs.QSFragment.2
        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            QSFragment.this.getView().getViewTreeObserver().removeOnPreDrawListener(this);
            QSFragment.this.getView().animate().translationY(0.0f).setStartDelay(QSFragment.this.mDelay).setDuration(448L).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(QSFragment.this.mAnimateHeaderSlidingInListener).start();
            QSFragment.this.getView().setY(-QSFragment.this.mHeader.getHeight());
            return true;
        }
    };
    private final Animator.AnimatorListener mAnimateHeaderSlidingInListener = new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.QSFragment.3
        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            QSFragment.this.mHeaderAnimating = false;
            QSFragment.this.updateQsState();
        }
    };

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.cloneInContext(new ContextThemeWrapper(getContext(), (int) R.style.qs_theme)).inflate(R.layout.qs_panel, viewGroup, false);
    }

    @Override // android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mQSPanel = (QSPanel) view.findViewById(R.id.quick_settings_panel);
        this.mQSDetail = (QSDetail) view.findViewById(R.id.qs_detail);
        this.mHeader = (QuickStatusBarHeader) view.findViewById(R.id.header);
        this.mFooter = (QSFooter) view.findViewById(R.id.qs_footer);
        this.mContainer = (QSContainerImpl) view.findViewById(R.id.quick_settings_container);
        this.mQSDetail.setQsPanel(this.mQSPanel, this.mHeader, (View) this.mFooter);
        this.mQSAnimator = new QSAnimator(this, (QuickQSPanel) this.mHeader.findViewById(R.id.quick_qs_panel), this.mQSPanel);
        this.mQSCustomizer = (QSCustomizer) view.findViewById(R.id.qs_customize);
        this.mQSCustomizer.setQs(this);
        if (bundle != null) {
            setExpanded(bundle.getBoolean("expanded"));
            setListening(bundle.getBoolean("listening"));
            int[] iArr = new int[2];
            View findViewById = view.findViewById(16908291);
            findViewById.getLocationInWindow(iArr);
            int height = iArr[1] + (findViewById.getHeight() / 2);
            this.mQSCustomizer.setEditLocation(iArr[0] + (findViewById.getWidth() / 2), height);
            this.mQSCustomizer.restoreInstanceState(bundle);
        }
        ((CommandQueue) SysUiServiceProvider.getComponent(getContext(), CommandQueue.class)).addCallbacks(this);
    }

    @Override // android.app.Fragment
    public void onDestroyView() {
        ((CommandQueue) SysUiServiceProvider.getComponent(getContext(), CommandQueue.class)).removeCallbacks(this);
        super.onDestroyView();
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        if (this.mListening) {
            setListening(false);
        }
    }

    @Override // android.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("expanded", this.mQsExpanded);
        bundle.putBoolean("listening", this.mListening);
        this.mQSCustomizer.saveInstanceState(bundle);
    }

    boolean isListening() {
        return this.mListening;
    }

    boolean isExpanded() {
        return this.mQsExpanded;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public View getHeader() {
        return this.mHeader;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHasNotifications(boolean z) {
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setPanelView(QS.HeightListener heightListener) {
        this.mPanelView = heightListener;
    }

    @Override // android.app.Fragment, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (configuration.getLayoutDirection() != this.mLayoutDirection) {
            this.mLayoutDirection = configuration.getLayoutDirection();
            if (this.mQSAnimator != null) {
                this.mQSAnimator.onRtlChanged();
            }
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setContainer(ViewGroup viewGroup) {
        if (viewGroup instanceof NotificationsQuickSettingsContainer) {
            this.mQSCustomizer.setContainer((NotificationsQuickSettingsContainer) viewGroup);
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public boolean isCustomizing() {
        return this.mQSCustomizer.isCustomizing();
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mQSPanel.setHost(qSTileHost, this.mQSCustomizer);
        this.mHeader.setQSPanel(this.mQSPanel);
        this.mFooter.setQSPanel(this.mQSPanel);
        this.mQSDetail.setHost(qSTileHost);
        if (this.mQSAnimator != null) {
            this.mQSAnimator.setHost(qSTileHost);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, boolean z) {
        boolean z2;
        int adjustDisableFlags = this.mRemoteInputQuickSettingsDisabler.adjustDisableFlags(i2);
        if ((adjustDisableFlags & 1) == 0) {
            z2 = false;
        } else {
            z2 = true;
        }
        if (z2 == this.mQsDisabled) {
            return;
        }
        this.mQsDisabled = z2;
        this.mContainer.disable(i, adjustDisableFlags, z);
        this.mHeader.disable(i, adjustDisableFlags, z);
        this.mFooter.disable(i, adjustDisableFlags, z);
        updateQsState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateQsState() {
        boolean z = true;
        int i = 0;
        boolean z2 = this.mQsExpanded || this.mStackScrollerOverscrolling || this.mHeaderAnimating;
        this.mQSPanel.setExpanded(this.mQsExpanded);
        this.mQSDetail.setExpanded(this.mQsExpanded);
        this.mHeader.setVisibility((this.mQsExpanded || !this.mKeyguardShowing || this.mHeaderAnimating) ? 0 : 4);
        this.mHeader.setExpanded((this.mKeyguardShowing && !this.mHeaderAnimating) || (this.mQsExpanded && !this.mStackScrollerOverscrolling));
        this.mFooter.setVisibility((this.mQsDisabled || !(this.mQsExpanded || !this.mKeyguardShowing || this.mHeaderAnimating)) ? 4 : 0);
        QSFooter qSFooter = this.mFooter;
        if ((!this.mKeyguardShowing || this.mHeaderAnimating) && (!this.mQsExpanded || this.mStackScrollerOverscrolling)) {
            z = false;
        }
        qSFooter.setExpanded(z);
        QSPanel qSPanel = this.mQSPanel;
        if (this.mQsDisabled || !z2) {
            i = 4;
        }
        qSPanel.setVisibility(i);
    }

    public QSPanel getQsPanel() {
        return this.mQSPanel;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public boolean isShowingDetail() {
        return this.mQSPanel.isShowingCustomize() || this.mQSDetail.isShowingDetail();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return isCustomizing();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHeaderClickable(boolean z) {
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setExpanded(boolean z) {
        this.mQsExpanded = z;
        this.mQSPanel.setListening(this.mListening && this.mQsExpanded);
        updateQsState();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setKeyguardShowing(boolean z) {
        this.mKeyguardShowing = z;
        this.mLastQSExpansion = -1.0f;
        if (this.mQSAnimator != null) {
            this.mQSAnimator.setOnKeyguard(z);
        }
        this.mFooter.setKeyguardShowing(z);
        updateQsState();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setOverscrolling(boolean z) {
        this.mStackScrollerOverscrolling = z;
        updateQsState();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setListening(boolean z) {
        this.mListening = z;
        this.mHeader.setListening(z);
        this.mFooter.setListening(z);
        this.mQSPanel.setListening(this.mListening && this.mQsExpanded);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHeaderListening(boolean z) {
        this.mHeader.setListening(z);
        this.mFooter.setListening(z);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setQsExpansion(float f, float f2) {
        this.mContainer.setExpansion(f);
        float f3 = f - 1.0f;
        if (!this.mHeaderAnimating) {
            View view = getView();
            if (this.mKeyguardShowing) {
                f2 = this.mHeader.getHeight() * f3;
            }
            view.setTranslationY(f2);
        }
        if (f == this.mLastQSExpansion) {
            return;
        }
        this.mLastQSExpansion = f;
        boolean z = f == 1.0f;
        float bottom = f3 * ((this.mQSPanel.getBottom() - this.mHeader.getBottom()) + this.mHeader.getPaddingBottom() + this.mFooter.getHeight());
        this.mHeader.setExpansion(this.mKeyguardShowing, f, bottom);
        this.mFooter.setExpansion(this.mKeyguardShowing ? 1.0f : f);
        this.mQSPanel.getQsTileRevealController().setExpansion(f);
        this.mQSPanel.getTileLayout().setExpansion(f);
        this.mQSPanel.setTranslationY(bottom);
        this.mQSDetail.setFullyExpanded(z);
        if (z) {
            this.mQSPanel.setClipBounds(null);
        } else {
            this.mQsBounds.top = (int) (-this.mQSPanel.getTranslationY());
            this.mQsBounds.right = this.mQSPanel.getWidth();
            this.mQsBounds.bottom = this.mQSPanel.getHeight();
            this.mQSPanel.setClipBounds(this.mQsBounds);
        }
        if (this.mQSAnimator != null) {
            this.mQSAnimator.setPosition(f);
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void animateHeaderSlidingIn(long j) {
        if (!this.mQsExpanded) {
            this.mHeaderAnimating = true;
            this.mDelay = j;
            getView().getViewTreeObserver().addOnPreDrawListener(this.mStartHeaderSlidingIn);
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void animateHeaderSlidingOut() {
        this.mHeaderAnimating = true;
        getView().animate().y(-this.mHeader.getHeight()).setStartDelay(0L).setDuration(360L).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.QSFragment.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (QSFragment.this.getView() == null) {
                    Log.e(QS.TAG, "current view is null, return");
                    return;
                }
                QSFragment.this.getView().animate().setListener(null);
                QSFragment.this.mHeaderAnimating = false;
                QSFragment.this.updateQsState();
            }
        }).start();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setExpandClickListener(View.OnClickListener onClickListener) {
        this.mFooter.setExpandClickListener(onClickListener);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void closeDetail() {
        this.mQSPanel.closeDetail();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void notifyCustomizeChanged() {
        this.mContainer.updateExpansion();
        this.mQSPanel.setVisibility(!this.mQSCustomizer.isCustomizing() ? 0 : 4);
        this.mFooter.setVisibility(this.mQSCustomizer.isCustomizing() ? 4 : 0);
        this.mPanelView.onQsHeightChanged();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public int getDesiredHeight() {
        if (this.mQSCustomizer.isCustomizing()) {
            return getView().getHeight();
        }
        if (this.mQSDetail.isClosingDetail()) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mQSPanel.getLayoutParams();
            return layoutParams.topMargin + layoutParams.bottomMargin + this.mQSPanel.getMeasuredHeight() + getView().getPaddingBottom();
        }
        return getView().getMeasuredHeight();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHeightOverride(int i) {
        this.mContainer.setHeightOverride(i);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public int getQsMinExpansionHeight() {
        return this.mHeader.getHeight();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void hideImmediately() {
        getView().animate().cancel();
        getView().setY(-this.mHeader.getHeight());
    }
}
