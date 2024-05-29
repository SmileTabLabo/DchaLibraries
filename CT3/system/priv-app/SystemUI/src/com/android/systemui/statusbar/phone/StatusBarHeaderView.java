package com.android.systemui.statusbar.phone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.RippleDrawable;
import android.os.BenesseExtension;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.android.keyguard.KeyguardStatusView;
import com.android.keyguard.R$id;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.tuner.TunerService;
import java.text.NumberFormat;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/StatusBarHeaderView.class */
public class StatusBarHeaderView extends BaseStatusBarHeader implements View.OnClickListener, BatteryController.BatteryStateChangeCallback, NextAlarmController.NextAlarmChangeCallback, NetworkController.EmergencyListener {
    private ActivityStarter mActivityStarter;
    private boolean mAlarmShowing;
    private TextView mAlarmStatus;
    private boolean mAllowExpand;
    private TextView mAmPm;
    private float mAvatarCollapsedScaleFactor;
    private BatteryController mBatteryController;
    private TextView mBatteryLevel;
    private boolean mCaptureValues;
    private final Rect mClipBounds;
    private View mClock;
    private float mClockCollapsedScaleFactor;
    private int mClockCollapsedSize;
    private int mClockExpandedSize;
    private int mClockMarginBottomCollapsed;
    private int mClockMarginBottomExpanded;
    private int mCollapsedHeight;
    private final LayoutValues mCollapsedValues;
    private float mCurrentT;
    private final LayoutValues mCurrentValues;
    private TextView mDateCollapsed;
    private TextView mDateExpanded;
    private View mDateGroup;
    private boolean mDetailTransitioning;
    private TextView mEmergencyCallsOnly;
    private boolean mExpanded;
    private int mExpandedHeight;
    private final LayoutValues mExpandedValues;
    private boolean mListening;
    private ImageView mMultiUserAvatar;
    private int mMultiUserCollapsedMargin;
    private int mMultiUserExpandedMargin;
    private MultiUserSwitch mMultiUserSwitch;
    private int mMultiUserSwitchWidthCollapsed;
    private int mMultiUserSwitchWidthExpanded;
    private AlarmManager.AlarmClockInfo mNextAlarm;
    private NextAlarmController mNextAlarmController;
    private QSPanel mQSPanel;
    private View mQsDetailHeader;
    private ImageView mQsDetailHeaderProgress;
    private Switch mQsDetailHeaderSwitch;
    private TextView mQsDetailHeaderTitle;
    private final QSPanel.Callback mQsPanelCallback;
    private SettingsButton mSettingsButton;
    private View mSettingsContainer;
    private boolean mShowEmergencyCallsOnly;
    private boolean mShowingDetail;
    private View mSignalCluster;
    private boolean mSignalClusterDetached;
    private LinearLayout mSystemIcons;
    private ViewGroup mSystemIconsContainer;
    private View mSystemIconsSuperContainer;
    private TextView mTime;

    /* renamed from: com.android.systemui.statusbar.phone.StatusBarHeaderView$1  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/StatusBarHeaderView$1.class */
    class AnonymousClass1 implements QSPanel.Callback {
        private boolean mScanState;
        final StatusBarHeaderView this$0;

        AnonymousClass1(StatusBarHeaderView statusBarHeaderView) {
            this.this$0 = statusBarHeaderView;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void handleScanStateChanged(boolean z) {
            if (this.mScanState == z) {
                return;
            }
            this.mScanState = z;
            Animatable animatable = (Animatable) this.this$0.mQsDetailHeaderProgress.getDrawable();
            if (z) {
                this.this$0.mQsDetailHeaderProgress.animate().alpha(1.0f);
                animatable.start();
                return;
            }
            this.this$0.mQsDetailHeaderProgress.animate().alpha(0.0f);
            animatable.stop();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void handleShowingDetail(QSTile.DetailAdapter detailAdapter) {
            boolean z = detailAdapter != null;
            transition(this.this$0.mClock, !z);
            transition(this.this$0.mDateGroup, !z);
            if (this.this$0.mAlarmShowing) {
                transition(this.this$0.mAlarmStatus, !z);
            }
            transition(this.this$0.mQsDetailHeader, z);
            this.this$0.mShowingDetail = z;
            if (!z) {
                this.this$0.mQsDetailHeader.setClickable(false);
                return;
            }
            this.this$0.mQsDetailHeaderTitle.setText(detailAdapter.getTitle());
            Boolean toggleState = detailAdapter.getToggleState();
            if (toggleState == null) {
                this.this$0.mQsDetailHeaderSwitch.setVisibility(4);
                this.this$0.mQsDetailHeader.setClickable(false);
                return;
            }
            this.this$0.mQsDetailHeaderSwitch.setVisibility(0);
            this.this$0.mQsDetailHeaderSwitch.setChecked(toggleState.booleanValue());
            this.this$0.mQsDetailHeader.setClickable(true);
            this.this$0.mQsDetailHeader.setOnClickListener(new View.OnClickListener(this, detailAdapter) { // from class: com.android.systemui.statusbar.phone.StatusBarHeaderView.1.4
                final AnonymousClass1 this$1;
                final QSTile.DetailAdapter val$detail;

                {
                    this.this$1 = this;
                    this.val$detail = detailAdapter;
                }

                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    boolean z2 = !this.this$1.this$0.mQsDetailHeaderSwitch.isChecked();
                    this.this$1.this$0.mQsDetailHeaderSwitch.setChecked(z2);
                    this.val$detail.setToggleState(z2);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void handleToggleStateChanged(boolean z) {
            this.this$0.mQsDetailHeaderSwitch.setChecked(z);
        }

        private void transition(View view, boolean z) {
            int i = 0;
            if (z) {
                view.bringToFront();
                view.setVisibility(0);
            }
            if (view.hasOverlappingRendering()) {
                view.animate().withLayer();
            }
            ViewPropertyAnimator animate = view.animate();
            if (z) {
                i = 1;
            }
            animate.alpha(i).withEndAction(new Runnable(this, z, view) { // from class: com.android.systemui.statusbar.phone.StatusBarHeaderView.1.5
                final AnonymousClass1 this$1;
                final boolean val$in;
                final View val$v;

                {
                    this.this$1 = this;
                    this.val$in = z;
                    this.val$v = view;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (!this.val$in) {
                        this.val$v.setVisibility(4);
                    }
                    this.this$1.this$0.mDetailTransitioning = false;
                }
            }).start();
        }

        @Override // com.android.systemui.qs.QSPanel.Callback
        public void onScanStateChanged(boolean z) {
            this.this$0.post(new Runnable(this, z) { // from class: com.android.systemui.statusbar.phone.StatusBarHeaderView.1.3
                final AnonymousClass1 this$1;
                final boolean val$state;

                {
                    this.this$1 = this;
                    this.val$state = z;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.handleScanStateChanged(this.val$state);
                }
            });
        }

        @Override // com.android.systemui.qs.QSPanel.Callback
        public void onShowingDetail(QSTile.DetailAdapter detailAdapter, int i, int i2) {
            this.this$0.mDetailTransitioning = true;
            this.this$0.post(new Runnable(this, detailAdapter) { // from class: com.android.systemui.statusbar.phone.StatusBarHeaderView.1.2
                final AnonymousClass1 this$1;
                final QSTile.DetailAdapter val$detail;

                {
                    this.this$1 = this;
                    this.val$detail = detailAdapter;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.handleShowingDetail(this.val$detail);
                }
            });
        }

        @Override // com.android.systemui.qs.QSPanel.Callback
        public void onToggleStateChanged(boolean z) {
            this.this$0.post(new Runnable(this, z) { // from class: com.android.systemui.statusbar.phone.StatusBarHeaderView.1.1
                final AnonymousClass1 this$1;
                final boolean val$state;

                {
                    this.this$1 = this;
                    this.val$state = z;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.handleToggleStateChanged(this.val$state);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/StatusBarHeaderView$LayoutValues.class */
    public static final class LayoutValues {
        float alarmStatusAlpha;
        float avatarScale;
        float avatarX;
        float avatarY;
        float batteryLevelAlpha;
        float batteryX;
        float batteryY;
        float clockY;
        float dateCollapsedAlpha;
        float dateExpandedAlpha;
        float dateY;
        float emergencyCallsOnlyAlpha;
        float settingsAlpha;
        float settingsRotation;
        float settingsTranslation;
        float signalClusterAlpha;
        float timeScale;

        private LayoutValues() {
            this.timeScale = 1.0f;
        }

        /* synthetic */ LayoutValues(LayoutValues layoutValues) {
            this();
        }

        public void interpoloate(LayoutValues layoutValues, LayoutValues layoutValues2, float f) {
            this.timeScale = (layoutValues.timeScale * (1.0f - f)) + (layoutValues2.timeScale * f);
            this.clockY = (layoutValues.clockY * (1.0f - f)) + (layoutValues2.clockY * f);
            this.dateY = (layoutValues.dateY * (1.0f - f)) + (layoutValues2.dateY * f);
            this.avatarScale = (layoutValues.avatarScale * (1.0f - f)) + (layoutValues2.avatarScale * f);
            this.avatarX = (layoutValues.avatarX * (1.0f - f)) + (layoutValues2.avatarX * f);
            this.avatarY = (layoutValues.avatarY * (1.0f - f)) + (layoutValues2.avatarY * f);
            this.batteryX = (layoutValues.batteryX * (1.0f - f)) + (layoutValues2.batteryX * f);
            this.batteryY = (layoutValues.batteryY * (1.0f - f)) + (layoutValues2.batteryY * f);
            this.settingsTranslation = (layoutValues.settingsTranslation * (1.0f - f)) + (layoutValues2.settingsTranslation * f);
            float max = Math.max(0.0f, f - 0.5f) * 2.0f;
            this.settingsRotation = (layoutValues.settingsRotation * (1.0f - max)) + (layoutValues2.settingsRotation * max);
            this.emergencyCallsOnlyAlpha = (layoutValues.emergencyCallsOnlyAlpha * (1.0f - max)) + (layoutValues2.emergencyCallsOnlyAlpha * max);
            float min = Math.min(1.0f, 2.0f * f);
            this.signalClusterAlpha = (layoutValues.signalClusterAlpha * (1.0f - min)) + (layoutValues2.signalClusterAlpha * min);
            float max2 = Math.max(0.0f, f - 0.7f) / 0.3f;
            this.batteryLevelAlpha = (layoutValues.batteryLevelAlpha * (1.0f - max2)) + (layoutValues2.batteryLevelAlpha * max2);
            this.settingsAlpha = (layoutValues.settingsAlpha * (1.0f - max2)) + (layoutValues2.settingsAlpha * max2);
            this.dateExpandedAlpha = (layoutValues.dateExpandedAlpha * (1.0f - max2)) + (layoutValues2.dateExpandedAlpha * max2);
            this.dateCollapsedAlpha = (layoutValues.dateCollapsedAlpha * (1.0f - max2)) + (layoutValues2.dateCollapsedAlpha * max2);
            this.alarmStatusAlpha = (layoutValues.alarmStatusAlpha * (1.0f - max2)) + (layoutValues2.alarmStatusAlpha * max2);
        }
    }

    public StatusBarHeaderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mClipBounds = new Rect();
        this.mCollapsedValues = new LayoutValues(null);
        this.mExpandedValues = new LayoutValues(null);
        this.mCurrentValues = new LayoutValues(null);
        this.mAllowExpand = true;
        this.mQsPanelCallback = new AnonymousClass1(this);
    }

    private void applyAlpha(View view, float f) {
        if (view == null || view.getVisibility() == 8) {
            return;
        }
        if (f == 0.0f) {
            view.setVisibility(4);
            return;
        }
        view.setVisibility(0);
        view.setAlpha(f);
    }

    private void applyLayoutValues(LayoutValues layoutValues) {
        this.mTime.setScaleX(layoutValues.timeScale);
        this.mTime.setScaleY(layoutValues.timeScale);
        this.mClock.setY(layoutValues.clockY - this.mClock.getHeight());
        this.mDateGroup.setY(layoutValues.dateY);
        this.mAlarmStatus.setY(layoutValues.dateY - this.mAlarmStatus.getPaddingTop());
        this.mMultiUserAvatar.setScaleX(layoutValues.avatarScale);
        this.mMultiUserAvatar.setScaleY(layoutValues.avatarScale);
        this.mMultiUserAvatar.setX(layoutValues.avatarX - this.mMultiUserSwitch.getLeft());
        this.mMultiUserAvatar.setY(layoutValues.avatarY - this.mMultiUserSwitch.getTop());
        if (getLayoutDirection() == 0) {
            this.mSystemIconsSuperContainer.setX(layoutValues.batteryX - this.mSystemIconsContainer.getRight());
        } else {
            this.mSystemIconsSuperContainer.setX(layoutValues.batteryX - this.mSystemIconsContainer.getLeft());
        }
        this.mSystemIconsSuperContainer.setY(layoutValues.batteryY - this.mSystemIconsContainer.getTop());
        if (this.mSignalCluster != null && this.mExpanded) {
            if (getLayoutDirection() == 0) {
                this.mSignalCluster.setX(this.mSystemIconsSuperContainer.getX() - this.mSignalCluster.getWidth());
            } else {
                this.mSignalCluster.setX(this.mSystemIconsSuperContainer.getX() + this.mSystemIconsSuperContainer.getWidth());
            }
            this.mSignalCluster.setY((this.mSystemIconsSuperContainer.getY() + (this.mSystemIconsSuperContainer.getHeight() / 2)) - (this.mSignalCluster.getHeight() / 2));
        } else if (this.mSignalCluster != null) {
            this.mSignalCluster.setTranslationX(0.0f);
            this.mSignalCluster.setTranslationY(0.0f);
        }
        if (!this.mSettingsButton.isAnimating()) {
            this.mSettingsContainer.setTranslationY(this.mSystemIconsSuperContainer.getTranslationY());
            this.mSettingsContainer.setTranslationX(layoutValues.settingsTranslation);
            this.mSettingsButton.setRotation(layoutValues.settingsRotation);
        }
        applyAlpha(this.mEmergencyCallsOnly, layoutValues.emergencyCallsOnlyAlpha);
        if (!this.mShowingDetail && !this.mDetailTransitioning) {
            applyAlpha(this.mAlarmStatus, layoutValues.alarmStatusAlpha);
        }
        applyAlpha(this.mDateCollapsed, layoutValues.dateCollapsedAlpha);
        applyAlpha(this.mDateExpanded, layoutValues.dateExpandedAlpha);
        applyAlpha(this.mBatteryLevel, layoutValues.batteryLevelAlpha);
        applyAlpha(this.mSettingsContainer, layoutValues.settingsAlpha);
        applyAlpha(this.mSignalCluster, layoutValues.signalClusterAlpha);
        if (!this.mExpanded) {
            this.mTime.setScaleX(1.0f);
            this.mTime.setScaleY(1.0f);
        }
        updateAmPmTranslation();
    }

    private void captureLayoutValues(LayoutValues layoutValues) {
        layoutValues.timeScale = this.mExpanded ? 1.0f : this.mClockCollapsedScaleFactor;
        layoutValues.clockY = this.mClock.getBottom();
        layoutValues.dateY = this.mDateGroup.getTop();
        layoutValues.emergencyCallsOnlyAlpha = getAlphaForVisibility(this.mEmergencyCallsOnly);
        layoutValues.alarmStatusAlpha = getAlphaForVisibility(this.mAlarmStatus);
        layoutValues.dateCollapsedAlpha = getAlphaForVisibility(this.mDateCollapsed);
        layoutValues.dateExpandedAlpha = getAlphaForVisibility(this.mDateExpanded);
        layoutValues.avatarScale = this.mMultiUserAvatar.getScaleX();
        layoutValues.avatarX = this.mMultiUserSwitch.getLeft() + this.mMultiUserAvatar.getLeft();
        layoutValues.avatarY = this.mMultiUserSwitch.getTop() + this.mMultiUserAvatar.getTop();
        if (getLayoutDirection() == 0) {
            layoutValues.batteryX = this.mSystemIconsSuperContainer.getLeft() + this.mSystemIconsContainer.getRight();
        } else {
            layoutValues.batteryX = this.mSystemIconsSuperContainer.getLeft() + this.mSystemIconsContainer.getLeft();
        }
        layoutValues.batteryY = this.mSystemIconsSuperContainer.getTop() + this.mSystemIconsContainer.getTop();
        layoutValues.batteryLevelAlpha = getAlphaForVisibility(this.mBatteryLevel);
        layoutValues.settingsAlpha = getAlphaForVisibility(this.mSettingsContainer);
        layoutValues.settingsTranslation = this.mExpanded ? 0 : this.mMultiUserSwitch.getLeft() - this.mSettingsContainer.getLeft();
        float f = 1.0f;
        if (this.mSignalClusterDetached) {
            f = 0.0f;
        }
        layoutValues.signalClusterAlpha = f;
        float f2 = 0.0f;
        if (!this.mExpanded) {
            f2 = 90.0f;
        }
        layoutValues.settingsRotation = f2;
    }

    private float getAlphaForVisibility(View view) {
        return (view == null || view.getVisibility() == 0) ? 1.0f : 0.0f;
    }

    private void loadDimens() {
        this.mCollapsedHeight = getResources().getDimensionPixelSize(2131689810);
        this.mExpandedHeight = getResources().getDimensionPixelSize(2131689811);
        this.mMultiUserExpandedMargin = getResources().getDimensionPixelSize(2131689899);
        this.mMultiUserCollapsedMargin = getResources().getDimensionPixelSize(2131689900);
        this.mClockMarginBottomExpanded = getResources().getDimensionPixelSize(2131689905);
        updateClockCollapsedMargin();
        this.mMultiUserSwitchWidthCollapsed = getResources().getDimensionPixelSize(2131689908);
        this.mMultiUserSwitchWidthExpanded = getResources().getDimensionPixelSize(2131689909);
        this.mAvatarCollapsedScaleFactor = getResources().getDimensionPixelSize(2131689911) / this.mMultiUserAvatar.getLayoutParams().width;
        this.mClockCollapsedSize = getResources().getDimensionPixelSize(2131689914);
        this.mClockExpandedSize = getResources().getDimensionPixelSize(2131689915);
        this.mClockCollapsedScaleFactor = this.mClockCollapsedSize / this.mClockExpandedSize;
    }

    private void reattachSignalCluster() {
        getOverlay().remove(this.mSignalCluster);
        this.mSystemIcons.addView(this.mSignalCluster, 1);
    }

    private void requestCaptureValues() {
        this.mCaptureValues = true;
        requestLayout();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setClipping(float f) {
        this.mClipBounds.set(getPaddingLeft(), 0, getWidth() - getPaddingRight(), (int) f);
        setClipBounds(this.mClipBounds);
        invalidateOutline();
    }

    private void startBatteryActivity() {
        if (BenesseExtension.getDchaState() != 0) {
            return;
        }
        this.mActivityStarter.startActivity(new Intent("android.intent.action.POWER_USAGE_SUMMARY"), true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startSettingsActivity() {
        if (BenesseExtension.getDchaState() != 0) {
            return;
        }
        this.mActivityStarter.startActivity(new Intent("android.settings.SETTINGS"), true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAmPmTranslation() {
        this.mAmPm.setTranslationX((getLayoutDirection() == 1 ? 1 : -1) * this.mTime.getWidth() * (1.0f - this.mTime.getScaleX()));
    }

    private void updateAvatarScale() {
        if (this.mExpanded) {
            this.mMultiUserAvatar.setScaleX(1.0f);
            this.mMultiUserAvatar.setScaleY(1.0f);
            return;
        }
        this.mMultiUserAvatar.setScaleX(this.mAvatarCollapsedScaleFactor);
        this.mMultiUserAvatar.setScaleY(this.mAvatarCollapsedScaleFactor);
    }

    private void updateClickTargets() {
        this.mMultiUserSwitch.setClickable(this.mExpanded);
        this.mMultiUserSwitch.setFocusable(this.mExpanded);
        this.mSystemIconsSuperContainer.setClickable(this.mExpanded);
        this.mSystemIconsSuperContainer.setFocusable(this.mExpanded);
        TextView textView = this.mAlarmStatus;
        boolean z = false;
        if (this.mNextAlarm != null) {
            z = false;
            if (this.mNextAlarm.getShowIntent() != null) {
                z = true;
            }
        }
        textView.setClickable(z);
    }

    private void updateClockCollapsedMargin() {
        Resources resources = getResources();
        int dimensionPixelSize = resources.getDimensionPixelSize(2131689906);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(2131689907);
        float constrain = (MathUtils.constrain(getResources().getConfiguration().fontScale, 1.0f, 1.3f) - 1.0f) / 0.29999995f;
        this.mClockMarginBottomCollapsed = Math.round(((1.0f - constrain) * dimensionPixelSize) + (dimensionPixelSize2 * constrain));
        requestLayout();
    }

    private void updateClockLp() {
        int i = this.mExpanded ? this.mClockMarginBottomExpanded : this.mClockMarginBottomCollapsed;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mDateGroup.getLayoutParams();
        if (i != layoutParams.bottomMargin) {
            layoutParams.bottomMargin = i;
            this.mDateGroup.setLayoutParams(layoutParams);
        }
    }

    private void updateClockScale() {
        this.mTime.setTextSize(0, this.mExpanded ? this.mClockExpandedSize : this.mClockCollapsedSize);
        this.mTime.setScaleX(1.0f);
        this.mTime.setScaleY(1.0f);
        updateAmPmTranslation();
    }

    private void updateHeights() {
        int i = this.mExpanded ? this.mExpandedHeight : this.mCollapsedHeight;
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams.height != i) {
            layoutParams.height = i;
            setLayoutParams(layoutParams);
        }
    }

    private void updateLayoutValues(float f) {
        if (this.mCaptureValues) {
            return;
        }
        this.mCurrentValues.interpoloate(this.mCollapsedValues, this.mExpandedValues, f);
        applyLayoutValues(this.mCurrentValues);
    }

    private void updateListeners() {
        if (this.mListening) {
            this.mBatteryController.addStateChangedCallback(this);
            this.mNextAlarmController.addStateChangedCallback(this);
            return;
        }
        this.mBatteryController.removeStateChangedCallback(this);
        this.mNextAlarmController.removeStateChangedCallback(this);
    }

    private void updateMultiUserSwitch() {
        int i;
        int i2;
        if (this.mExpanded) {
            i = this.mMultiUserExpandedMargin;
            i2 = this.mMultiUserSwitchWidthExpanded;
        } else {
            i = this.mMultiUserCollapsedMargin;
            i2 = this.mMultiUserSwitchWidthCollapsed;
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mMultiUserSwitch.getLayoutParams();
        if (i == marginLayoutParams.getMarginEnd() && marginLayoutParams.width == i2) {
            return;
        }
        marginLayoutParams.setMarginEnd(i);
        marginLayoutParams.width = i2;
        this.mMultiUserSwitch.setLayoutParams(marginLayoutParams);
    }

    private void updateSignalClusterDetachment() {
        boolean z = this.mExpanded;
        if (z != this.mSignalClusterDetached) {
            if (z) {
                getOverlay().add(this.mSignalCluster);
            } else {
                reattachSignalCluster();
            }
        }
        this.mSignalClusterDetached = z;
    }

    private void updateSystemIconsLayoutParams() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mSystemIconsSuperContainer.getLayoutParams();
        int id = this.mExpanded ? this.mSettingsContainer.getId() : this.mMultiUserSwitch.getId();
        if (id != layoutParams.getRules()[16]) {
            layoutParams.addRule(16, id);
            this.mSystemIconsSuperContainer.setLayoutParams(layoutParams);
        }
    }

    private void updateVisibilities() {
        Log.d("StatusBarHeaderView", "updateVisibilities: " + this.mExpanded + ", " + this.mAlarmShowing + ", " + this.mShowingDetail + ", " + this.mShowEmergencyCallsOnly);
        this.mDateCollapsed.setVisibility((this.mExpanded && this.mAlarmShowing) ? 0 : 4);
        this.mDateExpanded.setVisibility((this.mExpanded && this.mAlarmShowing) ? 4 : 0);
        this.mAlarmStatus.setVisibility((this.mExpanded && this.mAlarmShowing) ? 0 : 4);
        this.mSettingsContainer.setVisibility(this.mExpanded ? 0 : 4);
        this.mQsDetailHeader.setVisibility((this.mExpanded && this.mShowingDetail) ? 0 : 4);
        if (this.mSignalCluster != null) {
            updateSignalClusterDetachment();
        }
        this.mEmergencyCallsOnly.setVisibility((this.mExpanded && this.mShowEmergencyCallsOnly) ? 0 : 8);
        TextView textView = this.mBatteryLevel;
        int i = 8;
        if (this.mExpanded) {
            i = 0;
        }
        textView.setVisibility(i);
        this.mSettingsContainer.findViewById(2131886598).setVisibility(TunerService.isTunerEnabled(this.mContext) ? 0 : 4);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchSetPressed(boolean z) {
    }

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public int getCollapsedHeight() {
        return this.mCollapsedHeight;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        this.mBatteryLevel.setText(NumberFormat.getPercentInstance().format(i / 100.0d));
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        PendingIntent showIntent;
        if (view == this.mSettingsButton) {
            if (this.mSettingsButton.isTunerClick()) {
                if (TunerService.isTunerEnabled(this.mContext)) {
                    TunerService.showResetRequest(this.mContext, new Runnable(this) { // from class: com.android.systemui.statusbar.phone.StatusBarHeaderView.5
                        final StatusBarHeaderView this$0;

                        {
                            this.this$0 = this;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            this.this$0.startSettingsActivity();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), 2131493735, 1).show();
                    TunerService.setTunerEnabled(this.mContext, true);
                }
            }
            startSettingsActivity();
        } else if (view == this.mSystemIconsSuperContainer) {
            startBatteryActivity();
        } else if (view != this.mAlarmStatus || this.mNextAlarm == null || (showIntent = this.mNextAlarm.getShowIntent()) == null) {
        } else {
            this.mActivityStarter.startPendingIntentDismissingKeyguard(showIntent);
        }
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        FontSizeUtils.updateFontSize(this.mBatteryLevel, 2131689938);
        FontSizeUtils.updateFontSize(this.mEmergencyCallsOnly, 2131689916);
        FontSizeUtils.updateFontSize(this.mDateCollapsed, 2131689917);
        FontSizeUtils.updateFontSize(this.mDateExpanded, 2131689917);
        FontSizeUtils.updateFontSize(this.mAlarmStatus, 2131689917);
        FontSizeUtils.updateFontSize(this, 16908310, 2131689850);
        FontSizeUtils.updateFontSize(this, 16908311, 2131689850);
        FontSizeUtils.updateFontSize(this.mAmPm, 2131689914);
        FontSizeUtils.updateFontSize(this, 2131886673, 2131689915);
        this.mEmergencyCallsOnly.setText(17040033);
        this.mClockCollapsedSize = getResources().getDimensionPixelSize(2131689914);
        this.mClockExpandedSize = getResources().getDimensionPixelSize(2131689915);
        this.mClockCollapsedScaleFactor = this.mClockCollapsedSize / this.mClockExpandedSize;
        updateClockScale();
        updateClockCollapsedMargin();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSystemIconsSuperContainer = findViewById(2131886351);
        this.mSystemIconsContainer = (ViewGroup) findViewById(2131886352);
        this.mSystemIconsSuperContainer.setOnClickListener(this);
        this.mDateGroup = findViewById(2131886687);
        this.mClock = findViewById(R$id.clock);
        this.mTime = (TextView) findViewById(2131886671);
        this.mAmPm = (TextView) findViewById(2131886672);
        this.mMultiUserSwitch = (MultiUserSwitch) findViewById(2131886349);
        this.mMultiUserAvatar = (ImageView) findViewById(2131886350);
        this.mDateCollapsed = (TextView) findViewById(2131886688);
        this.mDateExpanded = (TextView) findViewById(2131886689);
        this.mSettingsButton = (SettingsButton) findViewById(2131886597);
        this.mSettingsContainer = findViewById(2131886596);
        this.mSettingsButton.setOnClickListener(this);
        this.mQsDetailHeader = findViewById(2131886581);
        this.mQsDetailHeader.setAlpha(0.0f);
        this.mQsDetailHeaderTitle = (TextView) this.mQsDetailHeader.findViewById(16908310);
        this.mQsDetailHeaderSwitch = (Switch) this.mQsDetailHeader.findViewById(16908311);
        this.mQsDetailHeaderProgress = (ImageView) findViewById(2131886582);
        this.mEmergencyCallsOnly = (TextView) findViewById(2131886600);
        this.mBatteryLevel = (TextView) findViewById(2131886353);
        this.mAlarmStatus = (TextView) findViewById(R$id.alarm_status);
        this.mAlarmStatus.setOnClickListener(this);
        this.mSignalCluster = findViewById(2131886656);
        this.mSystemIcons = (LinearLayout) findViewById(2131886718);
        loadDimens();
        updateVisibilities();
        updateClockScale();
        updateAvatarScale();
        addOnLayoutChangeListener(new View.OnLayoutChangeListener(this) { // from class: com.android.systemui.statusbar.phone.StatusBarHeaderView.2
            final StatusBarHeaderView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnLayoutChangeListener
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                if (i3 - i != i7 - i5) {
                    this.this$0.setClipping(this.this$0.getHeight());
                }
                this.this$0.mTime.setPivotX(this.this$0.getLayoutDirection() == 1 ? this.this$0.mTime.getWidth() : 0);
                this.this$0.mTime.setPivotY(this.this$0.mTime.getBaseline());
                this.this$0.updateAmPmTranslation();
            }
        });
        setOutlineProvider(new ViewOutlineProvider(this) { // from class: com.android.systemui.statusbar.phone.StatusBarHeaderView.3
            final StatusBarHeaderView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                outline.setRect(this.this$0.mClipBounds);
            }
        });
        requestCaptureValues();
        ((RippleDrawable) getBackground()).setForceSoftware(true);
        ((RippleDrawable) this.mSettingsButton.getBackground()).setForceSoftware(true);
        ((RippleDrawable) this.mSystemIconsSuperContainer.getBackground()).setForceSoftware(true);
    }

    @Override // android.widget.RelativeLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.mCaptureValues) {
            if (this.mExpanded) {
                captureLayoutValues(this.mExpandedValues);
            } else {
                captureLayoutValues(this.mCollapsedValues);
            }
            this.mCaptureValues = false;
            updateLayoutValues(this.mCurrentT);
        }
        this.mAlarmStatus.setX(this.mDateGroup.getLeft() + this.mDateCollapsed.getRight());
    }

    @Override // com.android.systemui.statusbar.policy.NextAlarmController.NextAlarmChangeCallback
    public void onNextAlarmChanged(AlarmManager.AlarmClockInfo alarmClockInfo) {
        this.mNextAlarm = alarmClockInfo;
        if (alarmClockInfo != null) {
            this.mAlarmStatus.setText(KeyguardStatusView.formatNextAlarm(getContext(), alarmClockInfo));
        }
        this.mAlarmShowing = alarmClockInfo != null;
        updateEverything();
        requestCaptureValues();
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
    }

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public void setActivityStarter(ActivityStarter activityStarter) {
        this.mActivityStarter = activityStarter;
    }

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public void setCallback(QSPanel.Callback callback) {
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.EmergencyListener
    public void setEmergencyCallsOnly(boolean z) {
        Log.d("StatusBarHeaderView", "setShowEmergencyCallsOnly: show= " + z + ", mShowEmergencyCallsOnly= " + this.mShowEmergencyCallsOnly + ", mExpanded= " + this.mExpanded);
        if (z != this.mShowEmergencyCallsOnly) {
            this.mShowEmergencyCallsOnly = z;
            if (this.mExpanded) {
                updateEverything();
                requestCaptureValues();
            }
        }
    }

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public void setExpanded(boolean z) {
        if (!this.mAllowExpand) {
            z = false;
        }
        boolean z2 = z != this.mExpanded;
        this.mExpanded = z;
        if (z2) {
            updateEverything();
        }
    }

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public void setExpansion(float f) {
        float f2 = f;
        if (!this.mExpanded) {
            f2 = 0.0f;
        }
        this.mCurrentT = f2;
        float f3 = this.mCollapsedHeight + ((this.mExpandedHeight - this.mCollapsedHeight) * f2);
        float f4 = f3;
        if (f3 < this.mCollapsedHeight) {
            f4 = this.mCollapsedHeight;
        }
        float f5 = f4;
        if (f4 > this.mExpandedHeight) {
            f5 = this.mExpandedHeight;
        }
        setClipping(f5);
        updateLayoutValues(f2);
    }

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public void setListening(boolean z) {
        if (z == this.mListening) {
            return;
        }
        this.mListening = z;
        updateListeners();
    }

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public void setQSPanel(QSPanel qSPanel) {
        this.mQSPanel = qSPanel;
        if (this.mQSPanel != null) {
            this.mQSPanel.setCallback(this.mQsPanelCallback);
        }
        this.mMultiUserSwitch.setQsPanel(qSPanel);
    }

    @Override // android.widget.RelativeLayout, android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public void updateEverything() {
        updateHeights();
        updateVisibilities();
        updateSystemIconsLayoutParams();
        updateClickTargets();
        updateMultiUserSwitch();
        updateClockScale();
        updateAvatarScale();
        updateClockLp();
        requestCaptureValues();
    }
}
