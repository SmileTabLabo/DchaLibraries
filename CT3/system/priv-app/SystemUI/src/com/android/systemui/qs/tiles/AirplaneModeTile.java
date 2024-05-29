package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.BenesseExtension;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.R$string;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.QSTile;
import com.mediatek.internal.telephony.ITelephonyEx;
/* loaded from: a.zip:com/android/systemui/qs/tiles/AirplaneModeTile.class */
public class AirplaneModeTile extends QSTile<QSTile.BooleanState> {
    private AnimationHandler mAnimHandler;
    private QSTile.Icon[] mAnimMembers;
    private int mCount;
    private final QSTile<QSTile.BooleanState>.AnimationIcon mDisable;
    private final QSTile<QSTile.BooleanState>.AnimationIcon mEnable;
    private boolean mListening;
    private final BroadcastReceiver mReceiver;
    private final GlobalSetting mSetting;
    private boolean mSwitching;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/qs/tiles/AirplaneModeTile$AnimationHandler.class */
    public class AnimationHandler extends Handler {
        final AirplaneModeTile this$0;

        private AnimationHandler(AirplaneModeTile airplaneModeTile) {
            this.this$0 = airplaneModeTile;
        }

        /* synthetic */ AnimationHandler(AirplaneModeTile airplaneModeTile, AnimationHandler animationHandler) {
            this(airplaneModeTile);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            super.handleMessage(message);
            this.this$0.mAnimHandler.sendEmptyMessageDelayed(0, 400L);
            AirplaneModeTile airplaneModeTile = this.this$0;
            int i = airplaneModeTile.mCount;
            airplaneModeTile.mCount = i + 1;
            if (i >= 60) {
                this.this$0.mCount = -1;
                if (this.this$0.isAirplanemodeAvailableNow()) {
                    Log.w(this.this$0.TAG, "No need show anim now...");
                    this.this$0.stopAnimation();
                }
            }
            this.this$0.refreshState();
        }
    }

    public AirplaneModeTile(QSTile.Host host) {
        super(host);
        this.mEnable = new QSTile.AnimationIcon(this, 2130837776, 2130837773);
        this.mDisable = new QSTile.AnimationIcon(this, 2130837774, 2130837775);
        this.mReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.qs.tiles.AirplaneModeTile.1
            final AirplaneModeTile this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (!"android.intent.action.AIRPLANE_MODE".equals(intent.getAction()) && "com.mediatek.intent.action.AIRPLANE_CHANGE_DONE".equals(intent.getAction())) {
                    Log.d(this.this$0.TAG, "onReceive() AIRPLANE_CHANGE_DONE,  airplaneModeOn= " + intent.getBooleanExtra("airplaneMode", false));
                    this.this$0.stopAnimation();
                    this.this$0.refreshState();
                }
            }
        };
        this.mCount = -1;
        this.mAnimMembers = new QSTile.Icon[]{QSTile.ResourceIcon.get(2130837778), QSTile.ResourceIcon.get(2130837779)};
        this.mAnimHandler = new AnimationHandler(this, null);
        this.mSetting = new GlobalSetting(this, this.mContext, this.mHandler, "airplane_mode_on") { // from class: com.android.systemui.qs.tiles.AirplaneModeTile.2
            final AirplaneModeTile this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.qs.GlobalSetting
            protected void handleValueChanged(int i) {
                this.this$0.handleRefreshState(Integer.valueOf(i));
            }
        };
    }

    private void handleAnimationState(QSTile.BooleanState booleanState, Object obj) {
        if (!this.mSwitching || this.mCount == -1) {
            return;
        }
        booleanState.icon = this.mAnimMembers[this.mCount % 2];
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isAirplanemodeAvailableNow() {
        boolean z;
        ITelephonyEx asInterface = ITelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx"));
        try {
            if (asInterface != null) {
                z = asInterface.isAirplanemodeAvailableNow();
            } else {
                Log.w(this.TAG, "telephonyEx == null");
                z = true;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            z = true;
        }
        Log.d(this.TAG, "isAirplaneModeAvailable = " + z);
        return z;
    }

    private void setEnabled(boolean z) {
        Log.d(this.TAG, "setEnabled = " + z);
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).setAirplaneMode(z);
    }

    private void startAnimation() {
        stopAnimation();
        this.mSwitching = true;
        this.mAnimHandler.sendEmptyMessage(0);
        Log.d(this.TAG, "startAnimation()");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopAnimation() {
        this.mSwitching = false;
        this.mCount = -1;
        if (this.mAnimHandler.hasMessages(0)) {
            this.mAnimHandler.removeMessages(0);
        }
        Log.d(this.TAG, "stopAnimation()");
    }

    @Override // com.android.systemui.qs.QSTile
    protected String composeChangeAnnouncement() {
        return ((QSTile.BooleanState) this.mState).value ? this.mContext.getString(2131493458) : this.mContext.getString(2131493457);
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        return new Intent("android.settings.AIRPLANE_MODE_SETTINGS");
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 112;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R$string.airplane_mode);
    }

    @Override // com.android.systemui.qs.QSTile
    public void handleClick() {
        if (this.mSwitching) {
            return;
        }
        startAnimation();
        MetricsLogger.action(this.mContext, getMetricsCategory(), !((QSTile.BooleanState) this.mState).value);
        setEnabled(!((QSTile.BooleanState) this.mState).value);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = false;
        if ((obj instanceof Integer ? ((Integer) obj).intValue() : this.mSetting.getValue()) != 0) {
            z = true;
        }
        booleanState.value = z;
        booleanState.label = this.mContext.getString(R$string.airplane_mode);
        if (z) {
            booleanState.icon = this.mEnable;
        } else {
            booleanState.icon = this.mDisable;
        }
        booleanState.contentDescription = booleanState.label;
        String name = Switch.class.getName();
        booleanState.expandedAccessibilityClassName = name;
        booleanState.minimalAccessibilityClassName = name;
        handleAnimationState(booleanState, obj);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        Log.d(this.TAG, "setListening(): " + this.mListening);
        if (z) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
            intentFilter.addAction("com.mediatek.intent.action.AIRPLANE_CHANGE_DONE");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
            if (!isAirplanemodeAvailableNow()) {
                Log.d(this.TAG, "setListening() Airplanemode not Available, start anim.");
                startAnimation();
            }
        } else {
            this.mContext.unregisterReceiver(this.mReceiver);
            stopAnimation();
        }
        this.mSetting.setListening(z);
    }
}
