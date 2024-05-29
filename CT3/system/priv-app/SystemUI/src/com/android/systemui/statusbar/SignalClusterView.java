package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.telephony.SubscriptionInfo;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.tuner.TunerService;
import com.mediatek.systemui.PluginManager;
import com.mediatek.systemui.ext.ISystemUIStatusBarExt;
import com.mediatek.systemui.statusbar.util.FeatureOptions;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/statusbar/SignalClusterView.class */
public class SignalClusterView extends LinearLayout implements NetworkController.SignalCallback, SecurityController.SecurityControllerCallback, TunerService.Tunable {
    static final boolean DEBUG = Log.isLoggable("SignalClusterView", 3);
    ImageView mAirplane;
    private String mAirplaneContentDescription;
    private int mAirplaneIconId;
    private boolean mBlockAirplane;
    private boolean mBlockEthernet;
    private boolean mBlockMobile;
    private boolean mBlockWifi;
    private float mDarkIntensity;
    private final int mEndPadding;
    private final int mEndPaddingNothingVisible;
    ImageView mEthernet;
    ImageView mEthernetDark;
    private String mEthernetDescription;
    ViewGroup mEthernetGroup;
    private int mEthernetIconId;
    private boolean mEthernetVisible;
    private final float mIconScaleFactor;
    private int mIconTint;
    private boolean mIsAirplaneMode;
    boolean mIsWfcEnable;
    private int mLastAirplaneIconId;
    private int mLastEthernetIconId;
    private int mLastWifiStrengthId;
    private final int mMobileDataIconStartPadding;
    LinearLayout mMobileSignalGroup;
    private final int mMobileSignalGroupEndPadding;
    NetworkControllerImpl mNC;
    ImageView mNoSims;
    View mNoSimsCombo;
    ImageView mNoSimsDark;
    private boolean mNoSimsVisible;
    private ArrayList<PhoneState> mPhoneStates;
    SecurityController mSC;
    private final int mSecondaryTelephonyPadding;
    private ISystemUIStatusBarExt mStatusBarExt;
    private final Rect mTintArea;
    ImageView mVpn;
    private boolean mVpnVisible;
    private final int mWideTypeIconStartPadding;
    ImageView mWifi;
    View mWifiAirplaneSpacer;
    ImageView mWifiDark;
    private String mWifiDescription;
    ViewGroup mWifiGroup;
    View mWifiSignalSpacer;
    private int mWifiStrengthId;
    private boolean mWifiVisible;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/SignalClusterView$PhoneState.class */
    public class PhoneState {
        private boolean mDataActivityIn;
        private boolean mDataActivityOut;
        private boolean mIsMobileTypeIconWide;
        private boolean mIsWfcCase;
        private ImageView mMobile;
        private ImageView mMobileDark;
        private String mMobileDescription;
        private ViewGroup mMobileGroup;
        private ImageView mMobileType;
        private String mMobileTypeDescription;
        private ImageView mNetworkType;
        private ISystemUIStatusBarExt mPhoneStateExt;
        private final int mSubId;
        private ImageView mVolteType;
        final SignalClusterView this$0;
        private boolean mMobileVisible = false;
        private int mMobileStrengthId = 0;
        private int mMobileTypeId = 0;
        private int mNetworkIcon = 0;
        private int mVolteIcon = 0;
        private int mLastMobileStrengthId = -1;
        private int mLastMobileTypeId = -1;

        public PhoneState(SignalClusterView signalClusterView, int i, Context context) {
            this.this$0 = signalClusterView;
            ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(2130968707, (ViewGroup) null);
            this.mPhoneStateExt = PluginManager.getSystemUIStatusBarExt(context);
            this.mPhoneStateExt.addCustomizedView(i, context, viewGroup);
            setViews(viewGroup);
            this.mSubId = i;
        }

        private void hideViewInWfcCase() {
            Log.d("SignalClusterView", "hideViewInWfcCase, isWfcEnabled = " + this.this$0.mIsWfcEnable + " mSubId =" + this.mSubId);
            this.mMobile.setVisibility(8);
            this.mMobileDark.setVisibility(8);
            this.mMobileType.setVisibility(8);
            this.mNetworkType.setVisibility(8);
            this.mIsWfcCase = true;
        }

        private void maybeStartAnimatableDrawable(ImageView imageView) {
            Drawable drawable = imageView.getDrawable();
            Drawable drawable2 = drawable;
            if (drawable instanceof ScalingDrawableWrapper) {
                drawable2 = ((ScalingDrawableWrapper) drawable).getDrawable();
            }
            if (drawable2 instanceof Animatable) {
                Animatable animatable = (Animatable) drawable2;
                if (animatable instanceof AnimatedVectorDrawable) {
                    ((AnimatedVectorDrawable) animatable).forceAnimationOnUI();
                }
                if (animatable.isRunning()) {
                    return;
                }
                animatable.start();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void maybeStopAnimatableDrawable(ImageView imageView) {
            Drawable drawable = imageView.getDrawable();
            Drawable drawable2 = drawable;
            if (drawable instanceof ScalingDrawableWrapper) {
                drawable2 = ((ScalingDrawableWrapper) drawable).getDrawable();
            }
            if (drawable2 instanceof Animatable) {
                Animatable animatable = (Animatable) drawable2;
                if (animatable.isRunning()) {
                    animatable.stop();
                }
            }
        }

        private void setCustomizeViewProperty() {
            setNetworkIcon();
            setVolteIcon();
        }

        private void setCustomizedOpViews() {
            if (!this.mMobileVisible || this.this$0.mIsAirplaneMode) {
                return;
            }
            this.mPhoneStateExt.getServiceStateForCustomizedView(this.mSubId);
            this.mPhoneStateExt.setCustomizedAirplaneView(this.this$0.mNoSimsCombo, this.this$0.mIsAirplaneMode);
            this.mPhoneStateExt.setCustomizedNetworkTypeView(this.mSubId, this.mNetworkIcon, this.mNetworkType);
            this.mPhoneStateExt.setCustomizedDataTypeView(this.mSubId, this.mMobileTypeId, this.mDataActivityIn, this.mDataActivityOut);
            this.mPhoneStateExt.setCustomizedSignalStrengthView(this.mSubId, this.mMobileStrengthId, this.mMobile);
            this.mPhoneStateExt.setCustomizedSignalStrengthView(this.mSubId, this.mMobileStrengthId, this.mMobileDark);
            this.mPhoneStateExt.setCustomizedMobileTypeView(this.mSubId, this.mMobileType);
            this.mPhoneStateExt.setCustomizedView(this.mSubId);
        }

        private void setNetworkIcon() {
            if (FeatureOptions.MTK_CTA_SET) {
                if (this.mNetworkIcon == 0) {
                    this.mNetworkType.setVisibility(8);
                    return;
                }
                this.mNetworkType.setImageResource(this.mNetworkIcon);
                this.mNetworkType.setVisibility(0);
            }
        }

        private void setVolteIcon() {
            if (this.mVolteIcon == 0) {
                this.mVolteType.setVisibility(8);
            } else {
                this.mVolteType.setImageResource(this.mVolteIcon);
                this.mVolteType.setVisibility(0);
            }
            this.this$0.mStatusBarExt.setCustomizedVolteView(this.mVolteIcon, this.mVolteType);
        }

        private void showViewInWfcCase() {
            if (this.mIsWfcCase) {
                Log.d("SignalClusterView", "showViewInWfcCase: mSubId = " + this.mSubId);
                this.mMobile.setVisibility(0);
                this.mMobileDark.setVisibility(0);
                this.mMobileType.setVisibility(0);
                this.mNetworkType.setVisibility(0);
                this.mIsWfcCase = false;
            }
        }

        private void updateAnimatableIcon(ImageView imageView, int i) {
            maybeStopAnimatableDrawable(imageView);
            this.this$0.setIconForView(imageView, i);
            maybeStartAnimatableDrawable(imageView);
        }

        public boolean apply(boolean z) {
            if (this.mMobileVisible && !this.this$0.mIsAirplaneMode) {
                if (this.mLastMobileStrengthId != this.mMobileStrengthId) {
                    updateAnimatableIcon(this.mMobile, this.mMobileStrengthId);
                    updateAnimatableIcon(this.mMobileDark, this.mMobileStrengthId);
                    this.mLastMobileStrengthId = this.mMobileStrengthId;
                }
                if (this.mLastMobileTypeId != this.mMobileTypeId) {
                    this.mMobileType.setImageResource(this.mMobileTypeId);
                    this.mLastMobileTypeId = this.mMobileTypeId;
                }
                this.mMobileGroup.setContentDescription(this.mMobileTypeDescription + " " + this.mMobileDescription);
                this.mMobileGroup.setVisibility(0);
                showViewInWfcCase();
            } else if (this.this$0.mIsAirplaneMode && this.this$0.mIsWfcEnable && this.mVolteIcon != 0) {
                this.mMobileGroup.setVisibility(0);
                hideViewInWfcCase();
            } else {
                this.mMobileGroup.setVisibility(8);
            }
            setCustomizeViewProperty();
            this.mMobileGroup.setPaddingRelative(z ? this.this$0.mSecondaryTelephonyPadding : 0, 0, 0, 0);
            this.mMobile.setPaddingRelative(this.mIsMobileTypeIconWide ? this.this$0.mWideTypeIconStartPadding : this.this$0.mMobileDataIconStartPadding, 0, 0, 0);
            this.mMobileDark.setPaddingRelative(this.mIsMobileTypeIconWide ? this.this$0.mWideTypeIconStartPadding : this.this$0.mMobileDataIconStartPadding, 0, 0, 0);
            if (SignalClusterView.DEBUG) {
                Log.d("SignalClusterView", String.format("mobile: %s sig=%d typ=%d", this.mMobileVisible ? "VISIBLE" : "GONE", Integer.valueOf(this.mMobileStrengthId), Integer.valueOf(this.mMobileTypeId)));
            }
            this.mMobileType.setVisibility(this.mMobileTypeId != 0 ? 0 : 8);
            setCustomizedOpViews();
            return this.mMobileVisible;
        }

        public void populateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
            if (!this.mMobileVisible || this.mMobileGroup == null || this.mMobileGroup.getContentDescription() == null) {
                return;
            }
            accessibilityEvent.getText().add(this.mMobileGroup.getContentDescription());
        }

        public void setIconTint(int i, float f, Rect rect) {
            this.this$0.applyDarkIntensity(StatusBarIconController.getDarkIntensity(rect, this.mMobile, f), this.mMobile, this.mMobileDark);
            this.this$0.setTint(this.mMobileType, StatusBarIconController.getTint(rect, this.mMobileType, i));
            this.this$0.setTint(this.mNetworkType, StatusBarIconController.getTint(rect, this.mNetworkType, i));
            this.this$0.setTint(this.mVolteType, StatusBarIconController.getTint(rect, this.mVolteType, i));
        }

        public void setViews(ViewGroup viewGroup) {
            this.mMobileGroup = viewGroup;
            this.mMobile = (ImageView) viewGroup.findViewById(2131886493);
            this.mMobileDark = (ImageView) viewGroup.findViewById(2131886494);
            this.mMobileType = (ImageView) viewGroup.findViewById(2131886495);
            this.mNetworkType = (ImageView) viewGroup.findViewById(2131886497);
            this.mVolteType = (ImageView) viewGroup.findViewById(2131886496);
        }
    }

    public SignalClusterView(Context context) {
        this(context, null);
    }

    public SignalClusterView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SignalClusterView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mNoSimsVisible = false;
        this.mVpnVisible = false;
        this.mEthernetVisible = false;
        this.mEthernetIconId = 0;
        this.mLastEthernetIconId = -1;
        this.mWifiVisible = false;
        this.mWifiStrengthId = 0;
        this.mLastWifiStrengthId = -1;
        this.mIsAirplaneMode = false;
        this.mAirplaneIconId = 0;
        this.mLastAirplaneIconId = -1;
        this.mPhoneStates = new ArrayList<>();
        this.mIconTint = -1;
        this.mTintArea = new Rect();
        Resources resources = getResources();
        this.mMobileSignalGroupEndPadding = resources.getDimensionPixelSize(2131689945);
        this.mMobileDataIconStartPadding = resources.getDimensionPixelSize(2131689946);
        this.mWideTypeIconStartPadding = resources.getDimensionPixelSize(2131689947);
        this.mSecondaryTelephonyPadding = resources.getDimensionPixelSize(2131689948);
        this.mEndPadding = resources.getDimensionPixelSize(2131689954);
        this.mEndPaddingNothingVisible = resources.getDimensionPixelSize(2131689955);
        TypedValue typedValue = new TypedValue();
        resources.getValue(2131689784, typedValue, true);
        this.mIconScaleFactor = typedValue.getFloat();
        this.mStatusBarExt = PluginManager.getSystemUIStatusBarExt(context);
        this.mIsWfcEnable = SystemProperties.get("persist.mtk_wfc_support").equals("1");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void apply() {
        if (this.mWifiGroup == null) {
            return;
        }
        this.mVpn.setVisibility(this.mVpnVisible ? 0 : 8);
        if (DEBUG) {
            Log.d("SignalClusterView", String.format("vpn: %s", this.mVpnVisible ? "VISIBLE" : "GONE"));
        }
        if (this.mEthernetVisible) {
            if (this.mLastEthernetIconId != this.mEthernetIconId) {
                setIconForView(this.mEthernet, this.mEthernetIconId);
                setIconForView(this.mEthernetDark, this.mEthernetIconId);
                this.mLastEthernetIconId = this.mEthernetIconId;
            }
            this.mEthernetGroup.setContentDescription(this.mEthernetDescription);
            this.mEthernetGroup.setVisibility(0);
        } else {
            this.mEthernetGroup.setVisibility(8);
        }
        if (DEBUG) {
            Log.d("SignalClusterView", String.format("ethernet: %s", this.mEthernetVisible ? "VISIBLE" : "GONE"));
        }
        if (this.mWifiVisible) {
            if (this.mWifiStrengthId != this.mLastWifiStrengthId) {
                setIconForView(this.mWifi, this.mWifiStrengthId);
                setIconForView(this.mWifiDark, this.mWifiStrengthId);
                this.mLastWifiStrengthId = this.mWifiStrengthId;
            }
            this.mWifiGroup.setContentDescription(this.mWifiDescription);
            this.mWifiGroup.setVisibility(0);
        } else {
            this.mWifiGroup.setVisibility(8);
        }
        if (DEBUG) {
            Log.d("SignalClusterView", String.format("wifi: %s sig=%d", this.mWifiVisible ? "VISIBLE" : "GONE", Integer.valueOf(this.mWifiStrengthId)));
        }
        int i = 0;
        boolean z = FeatureOptions.MTK_CTA_SET;
        for (PhoneState phoneState : this.mPhoneStates) {
            if (phoneState.apply(z) && !z) {
                i = phoneState.mMobileTypeId;
                z = true;
            }
        }
        if (this.mIsAirplaneMode) {
            if (this.mLastAirplaneIconId != this.mAirplaneIconId) {
                setIconForView(this.mAirplane, this.mAirplaneIconId);
                this.mLastAirplaneIconId = this.mAirplaneIconId;
            }
            this.mAirplane.setContentDescription(this.mAirplaneContentDescription);
            this.mAirplane.setVisibility(0);
        } else {
            this.mAirplane.setVisibility(8);
        }
        if (this.mIsAirplaneMode && this.mWifiVisible) {
            this.mWifiAirplaneSpacer.setVisibility(0);
        } else {
            this.mWifiAirplaneSpacer.setVisibility(8);
        }
        if (((!z || i == 0) && !this.mNoSimsVisible) || !this.mWifiVisible) {
            this.mWifiSignalSpacer.setVisibility(8);
        } else {
            this.mWifiSignalSpacer.setVisibility(0);
        }
        this.mNoSimsCombo.setVisibility(this.mNoSimsVisible ? 0 : 8);
        this.mStatusBarExt.setCustomizedNoSimsVisible(this.mNoSimsVisible);
        this.mStatusBarExt.setCustomizedAirplaneView(this.mNoSimsCombo, this.mIsAirplaneMode);
        boolean z2 = true;
        if (!this.mNoSimsVisible) {
            z2 = true;
            if (!this.mWifiVisible) {
                z2 = true;
                if (!this.mIsAirplaneMode) {
                    z2 = true;
                    if (!z) {
                        z2 = true;
                        if (!this.mVpnVisible) {
                            z2 = this.mEthernetVisible;
                        }
                    }
                }
            }
        }
        setPaddingRelative(0, 0, z2 ? this.mEndPadding : this.mEndPaddingNothingVisible, 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyDarkIntensity(float f, View view, View view2) {
        view.setAlpha(1.0f - f);
        view2.setAlpha(f);
    }

    private void applyIconTint() {
        setTint(this.mVpn, StatusBarIconController.getTint(this.mTintArea, this.mVpn, this.mIconTint));
        setTint(this.mAirplane, StatusBarIconController.getTint(this.mTintArea, this.mAirplane, this.mIconTint));
        applyDarkIntensity(StatusBarIconController.getDarkIntensity(this.mTintArea, this.mNoSims, this.mDarkIntensity), this.mNoSims, this.mNoSimsDark);
        applyDarkIntensity(StatusBarIconController.getDarkIntensity(this.mTintArea, this.mWifi, this.mDarkIntensity), this.mWifi, this.mWifiDark);
        applyDarkIntensity(StatusBarIconController.getDarkIntensity(this.mTintArea, this.mEthernet, this.mDarkIntensity), this.mEthernet, this.mEthernetDark);
        for (int i = 0; i < this.mPhoneStates.size(); i++) {
            this.mPhoneStates.get(i).setIconTint(this.mIconTint, this.mDarkIntensity, this.mTintArea);
        }
    }

    private PhoneState getState(int i) {
        for (PhoneState phoneState : this.mPhoneStates) {
            if (phoneState.mSubId == i) {
                return phoneState;
            }
        }
        Log.e("SignalClusterView", "Unexpected subscription " + i);
        return null;
    }

    private boolean hasCorrectSubs(List<SubscriptionInfo> list) {
        int size = list.size();
        if (size != this.mPhoneStates.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (this.mPhoneStates.get(i).mSubId != list.get(i).getSubscriptionId() || this.mStatusBarExt.checkIfSlotIdChanged(list.get(i).getSubscriptionId(), list.get(i).getSimSlotIndex())) {
                return false;
            }
        }
        return true;
    }

    private PhoneState inflatePhoneState(int i) {
        PhoneState phoneState = new PhoneState(this, i, this.mContext);
        if (this.mMobileSignalGroup != null) {
            this.mMobileSignalGroup.addView(phoneState.mMobileGroup);
        }
        this.mPhoneStates.add(phoneState);
        return phoneState;
    }

    private void maybeScaleVpnAndNoSimsIcons() {
        if (this.mIconScaleFactor == 1.0f) {
            return;
        }
        this.mVpn.setImageDrawable(new ScalingDrawableWrapper(this.mVpn.getDrawable(), this.mIconScaleFactor));
        this.mNoSims.setImageDrawable(new ScalingDrawableWrapper(this.mNoSims.getDrawable(), this.mIconScaleFactor));
        this.mNoSimsDark.setImageDrawable(new ScalingDrawableWrapper(this.mNoSimsDark.getDrawable(), this.mIconScaleFactor));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setIconForView(ImageView imageView, int i) {
        Drawable drawable = imageView.getContext().getDrawable(i);
        if (this.mIconScaleFactor == 1.0f) {
            imageView.setImageDrawable(drawable);
        } else {
            imageView.setImageDrawable(new ScalingDrawableWrapper(drawable, this.mIconScaleFactor));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setTint(ImageView imageView, int i) {
        imageView.setImageTintList(ColorStateList.valueOf(i));
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent accessibilityEvent) {
        if (this.mEthernetVisible && this.mEthernetGroup != null && this.mEthernetGroup.getContentDescription() != null) {
            accessibilityEvent.getText().add(this.mEthernetGroup.getContentDescription());
        }
        if (this.mWifiVisible && this.mWifiGroup != null && this.mWifiGroup.getContentDescription() != null) {
            accessibilityEvent.getText().add(this.mWifiGroup.getContentDescription());
        }
        for (PhoneState phoneState : this.mPhoneStates) {
            phoneState.populateAccessibilityEvent(accessibilityEvent);
        }
        return super.dispatchPopulateAccessibilityEventInternal(accessibilityEvent);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        for (PhoneState phoneState : this.mPhoneStates) {
            this.mMobileSignalGroup.addView(phoneState.mMobileGroup);
        }
        this.mMobileSignalGroup.setPaddingRelative(0, 0, this.mMobileSignalGroup.getChildCount() > 0 ? this.mMobileSignalGroupEndPadding : 0, 0);
        TunerService.get(this.mContext).addTunable(this, "icon_blacklist");
        this.mStatusBarExt.setCustomizedNoSimView(this.mNoSims);
        this.mStatusBarExt.setCustomizedNoSimView(this.mNoSimsDark);
        this.mStatusBarExt.addSignalClusterCustomizedView(this.mContext, this, indexOfChild(findViewById(2131886665)));
        apply();
        applyIconTint();
        this.mNC.addSignalCallback(this);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        this.mMobileSignalGroup.removeAllViews();
        TunerService.get(this.mContext).removeTunable(this);
        this.mSC.removeCallback(this);
        this.mNC.removeSignalCallback(this);
        super.onDetachedFromWindow();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mVpn = (ImageView) findViewById(2131886657);
        this.mEthernetGroup = (ViewGroup) findViewById(2131886658);
        this.mEthernet = (ImageView) findViewById(2131886659);
        this.mEthernetDark = (ImageView) findViewById(2131886660);
        this.mWifiGroup = (ViewGroup) findViewById(2131886661);
        this.mWifi = (ImageView) findViewById(2131886662);
        this.mWifiDark = (ImageView) findViewById(2131886663);
        this.mAirplane = (ImageView) findViewById(2131886670);
        this.mNoSims = (ImageView) findViewById(2131886667);
        this.mNoSimsDark = (ImageView) findViewById(2131886668);
        this.mNoSimsCombo = findViewById(2131886666);
        this.mWifiAirplaneSpacer = findViewById(2131886669);
        this.mWifiSignalSpacer = findViewById(2131886664);
        this.mMobileSignalGroup = (LinearLayout) findViewById(2131886665);
        maybeScaleVpnAndNoSimsIcons();
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        applyIconTint();
    }

    @Override // android.widget.LinearLayout, android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        if (this.mEthernet != null) {
            this.mEthernet.setImageDrawable(null);
            this.mEthernetDark.setImageDrawable(null);
            this.mLastEthernetIconId = -1;
        }
        if (this.mWifi != null) {
            this.mWifi.setImageDrawable(null);
            this.mWifiDark.setImageDrawable(null);
            this.mLastWifiStrengthId = -1;
        }
        for (PhoneState phoneState : this.mPhoneStates) {
            if (phoneState.mMobile != null) {
                phoneState.maybeStopAnimatableDrawable(phoneState.mMobile);
                phoneState.mMobile.setImageDrawable(null);
                phoneState.mLastMobileStrengthId = -1;
            }
            if (phoneState.mMobileDark != null) {
                phoneState.maybeStopAnimatableDrawable(phoneState.mMobileDark);
                phoneState.mMobileDark.setImageDrawable(null);
                phoneState.mLastMobileStrengthId = -1;
            }
            if (phoneState.mMobileType != null) {
                phoneState.mMobileType.setImageDrawable(null);
                phoneState.mLastMobileTypeId = -1;
            }
        }
        if (this.mAirplane != null) {
            this.mAirplane.setImageDrawable(null);
            this.mLastAirplaneIconId = -1;
        }
        apply();
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController.SecurityControllerCallback
    public void onStateChanged() {
        post(new Runnable(this) { // from class: com.android.systemui.statusbar.SignalClusterView.1
            final SignalClusterView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mVpnVisible = this.this$0.mSC.isVpnEnabled();
                this.this$0.apply();
            }
        });
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("icon_blacklist".equals(str)) {
            ArraySet<String> iconBlacklist = StatusBarIconController.getIconBlacklist(str2);
            boolean contains = iconBlacklist.contains("airplane");
            boolean contains2 = iconBlacklist.contains("mobile");
            boolean contains3 = iconBlacklist.contains("wifi");
            boolean contains4 = iconBlacklist.contains("ethernet");
            if (contains == this.mBlockAirplane && contains2 == this.mBlockMobile && contains4 == this.mBlockEthernet && contains3 == this.mBlockWifi) {
                return;
            }
            this.mBlockAirplane = contains;
            this.mBlockMobile = contains2;
            this.mBlockEthernet = contains4;
            this.mBlockWifi = contains3;
            this.mNC.removeSignalCallback(this);
            this.mNC.addSignalCallback(this);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setEthernetIndicators(NetworkController.IconState iconState) {
        boolean z = false;
        if (iconState.visible) {
            z = !this.mBlockEthernet;
        }
        this.mEthernetVisible = z;
        this.mEthernetIconId = iconState.icon;
        this.mEthernetDescription = iconState.contentDescription;
        apply();
    }

    public void setIconTint(int i, float f, Rect rect) {
        boolean z = (i == this.mIconTint && f == this.mDarkIntensity) ? !this.mTintArea.equals(rect) : true;
        this.mIconTint = i;
        this.mDarkIntensity = f;
        this.mTintArea.set(rect);
        if (z && isAttachedToWindow()) {
            applyIconTint();
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setIsAirplaneMode(NetworkController.IconState iconState) {
        boolean z = false;
        if (iconState.visible) {
            z = !this.mBlockAirplane;
        }
        this.mIsAirplaneMode = z;
        this.mAirplaneIconId = iconState.icon;
        this.mAirplaneContentDescription = iconState.contentDescription;
        apply();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataEnabled(boolean z) {
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, int i3, int i4, boolean z, boolean z2, String str, String str2, boolean z3, int i5) {
        PhoneState state = getState(i5);
        if (state == null) {
            return;
        }
        state.mMobileVisible = iconState.visible && !this.mBlockMobile;
        state.mMobileStrengthId = iconState.icon;
        state.mMobileTypeId = i;
        state.mMobileDescription = iconState.contentDescription;
        state.mMobileTypeDescription = str;
        if (i == 0) {
            z3 = false;
        }
        state.mIsMobileTypeIconWide = z3;
        state.mNetworkIcon = i2;
        state.mVolteIcon = i3;
        state.mDataActivityIn = z;
        state.mDataActivityOut = z2;
        apply();
    }

    public void setNetworkController(NetworkControllerImpl networkControllerImpl) {
        if (DEBUG) {
            Log.d("SignalClusterView", "NetworkController=" + networkControllerImpl);
        }
        this.mNC = networkControllerImpl;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setNoSims(boolean z) {
        boolean z2 = false;
        if (z) {
            z2 = !this.mBlockMobile;
        }
        this.mNoSimsVisible = z2;
        apply();
    }

    public void setSecurityController(SecurityController securityController) {
        if (DEBUG) {
            Log.d("SignalClusterView", "SecurityController=" + securityController);
        }
        this.mSC = securityController;
        this.mSC.addCallback(this);
        this.mVpnVisible = this.mSC.isVpnEnabled();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setSubs(List<SubscriptionInfo> list) {
        if (hasCorrectSubs(list)) {
            return;
        }
        for (PhoneState phoneState : this.mPhoneStates) {
            if (phoneState.mMobile != null) {
                phoneState.maybeStopAnimatableDrawable(phoneState.mMobile);
            }
            if (phoneState.mMobileDark != null) {
                phoneState.maybeStopAnimatableDrawable(phoneState.mMobileDark);
            }
        }
        this.mPhoneStates.clear();
        if (this.mMobileSignalGroup != null) {
            this.mMobileSignalGroup.removeAllViews();
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            inflatePhoneState(list.get(i).getSubscriptionId());
        }
        if (isAttachedToWindow()) {
            applyIconTint();
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str) {
        boolean z4 = false;
        if (iconState.visible) {
            z4 = !this.mBlockWifi;
        }
        this.mWifiVisible = z4;
        this.mWifiStrengthId = iconState.icon;
        this.mWifiDescription = iconState.contentDescription;
        apply();
    }
}
