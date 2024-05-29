package com.mediatek.keyguard.PowerOffAlarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityCallback;
import com.android.keyguard.KeyguardSecurityView;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$id;
import com.mediatek.keyguard.PowerOffAlarm.multiwaveview.GlowPadView;
/* loaded from: a.zip:com/mediatek/keyguard/PowerOffAlarm/PowerOffAlarmView.class */
public class PowerOffAlarmView extends RelativeLayout implements KeyguardSecurityView, GlowPadView.OnTriggerListener {
    private final int DELAY_TIME_SECONDS;
    private KeyguardSecurityCallback mCallback;
    private Context mContext;
    private int mFailedPatternAttemptsSinceLastTimeout;
    private GlowPadView mGlowPadView;
    private final Handler mHandler;
    private boolean mIsDocked;
    private boolean mIsRegistered;
    private LockPatternUtils mLockPatternUtils;
    private boolean mPingEnabled;
    private final BroadcastReceiver mReceiver;
    private TextView mTitleView;
    private int mTotalFailedPatternAttempts;

    public PowerOffAlarmView(Context context) {
        this(context, null);
    }

    public PowerOffAlarmView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.DELAY_TIME_SECONDS = 7;
        this.mFailedPatternAttemptsSinceLastTimeout = 0;
        this.mTotalFailedPatternAttempts = 0;
        this.mTitleView = null;
        this.mIsRegistered = false;
        this.mIsDocked = false;
        this.mPingEnabled = true;
        this.mHandler = new Handler(this) { // from class: com.mediatek.keyguard.PowerOffAlarm.PowerOffAlarmView.1
            final PowerOffAlarmView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 99:
                        if (this.this$0.mTitleView != null) {
                            this.this$0.mTitleView.setText(message.getData().getString("label"));
                            return;
                        }
                        return;
                    case 100:
                    default:
                        return;
                    case 101:
                        this.this$0.triggerPing();
                        return;
                }
            }
        };
        this.mReceiver = new BroadcastReceiver(this) { // from class: com.mediatek.keyguard.PowerOffAlarm.PowerOffAlarmView.2
            final PowerOffAlarmView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                Log.v("PowerOffAlarmView", "receive action : " + action);
                if (!"update.power.off.alarm.label".equals(action)) {
                    if (PowerOffAlarmManager.isAlarmBoot()) {
                        this.this$0.snooze();
                        return;
                    }
                    return;
                }
                Message message = new Message();
                message.what = 99;
                Bundle bundle = new Bundle();
                bundle.putString("label", intent.getStringExtra("label"));
                message.setData(bundle);
                this.this$0.mHandler.sendMessage(message);
            }
        };
        this.mContext = context;
    }

    private void enableEventDispatching(boolean z) {
        try {
            IWindowManager asInterface = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
            if (asInterface != null) {
                asInterface.setEventDispatching(z);
            }
        } catch (RemoteException e) {
            Log.w("PowerOffAlarmView", e.toString());
        }
    }

    private void powerOff() {
        Log.d("PowerOffAlarmView", "powerOff selected");
        sendBR("com.android.deskclock.DISMISS_ALARM");
    }

    private void powerOn() {
        enableEventDispatching(false);
        Log.d("PowerOffAlarmView", "powerOn selected");
        sendBR("com.android.deskclock.POWER_ON_ALARM");
        sendBR("android.intent.action.normal.boot");
    }

    private void sendBR(String str) {
        Log.w("PowerOffAlarmView", "send BR: " + str);
        this.mContext.sendBroadcast(new Intent(str));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void snooze() {
        Log.d("PowerOffAlarmView", "snooze selected");
        sendBR("com.android.deskclock.SNOOZE_ALARM");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void triggerPing() {
        if (this.mPingEnabled) {
            this.mGlowPadView.ping();
            this.mHandler.sendEmptyMessageDelayed(101, 1200L);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        return false;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        Log.v("PowerOffAlarmView", "onDetachedFromWindow ....");
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    @Override // com.mediatek.keyguard.PowerOffAlarm.multiwaveview.GlowPadView.OnTriggerListener
    public void onFinishFinalAnimation() {
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.w("PowerOffAlarmView", "onFinishInflate ... ");
        setKeepScreenOn(true);
        this.mTitleView = (TextView) findViewById(R$id.alertTitle);
        this.mGlowPadView = (GlowPadView) findViewById(R$id.glow_pad_view);
        this.mGlowPadView.setOnTriggerListener(this);
        setFocusableInTouchMode(true);
        triggerPing();
        Intent registerReceiver = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.DOCK_EVENT"));
        if (registerReceiver != null) {
            this.mIsDocked = registerReceiver.getIntExtra("android.intent.extra.DOCK_STATE", -1) != 0;
        }
        IntentFilter intentFilter = new IntentFilter("alarm_killed");
        intentFilter.addAction("com.android.deskclock.ALARM_SNOOZE");
        intentFilter.addAction("com.android.deskclock.ALARM_DISMISS");
        intentFilter.addAction("update.power.off.alarm.label");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        this.mLockPatternUtils = this.mLockPatternUtils == null ? new LockPatternUtils(this.mContext) : this.mLockPatternUtils;
        enableEventDispatching(true);
    }

    @Override // com.mediatek.keyguard.PowerOffAlarm.multiwaveview.GlowPadView.OnTriggerListener
    public void onGrabbed(View view, int i) {
    }

    @Override // com.mediatek.keyguard.PowerOffAlarm.multiwaveview.GlowPadView.OnTriggerListener
    public void onGrabbedStateChange(View view, int i) {
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (PowerOffAlarmManager.isAlarmBoot()) {
            switch (i) {
                case 24:
                    Log.d("PowerOffAlarmView", "onKeyDown() - KeyEvent.KEYCODE_VOLUME_UP, do nothing.");
                    return true;
                case 25:
                    Log.d("PowerOffAlarmView", "onKeyDown() - KeyEvent.KEYCODE_VOLUME_DOWN, do nothing.");
                    return true;
            }
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (PowerOffAlarmManager.isAlarmBoot()) {
            switch (i) {
                case 24:
                    Log.d("PowerOffAlarmView", "onKeyUp() - KeyEvent.KEYCODE_VOLUME_UP, do nothing.");
                    return true;
                case 25:
                    Log.d("PowerOffAlarmView", "onKeyUp() - KeyEvent.KEYCODE_VOLUME_DOWN, do nothing.");
                    return true;
            }
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onPause() {
    }

    @Override // com.mediatek.keyguard.PowerOffAlarm.multiwaveview.GlowPadView.OnTriggerListener
    public void onReleased(View view, int i) {
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        reset();
        Log.v("PowerOffAlarmView", "onResume");
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return super.onTouchEvent(motionEvent);
    }

    @Override // com.mediatek.keyguard.PowerOffAlarm.multiwaveview.GlowPadView.OnTriggerListener
    public void onTrigger(View view, int i) {
        int resourceIdForTarget = this.mGlowPadView.getResourceIdForTarget(i);
        if (resourceIdForTarget == R$drawable.mtk_ic_alarm_alert_snooze) {
            snooze();
        } else if (resourceIdForTarget == R$drawable.mtk_ic_alarm_alert_dismiss_pwroff) {
            powerOff();
        } else if (resourceIdForTarget == R$drawable.mtk_ic_alarm_alert_dismiss_pwron) {
            powerOn();
        } else {
            Log.e("PowerOffAlarmView", "Trigger detected on unhandled resource. Skipping.");
        }
    }

    public void reset() {
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback) {
        this.mCallback = keyguardSecurityCallback;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showMessage(String str, int i) {
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showPromptReason(int i) {
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        return false;
    }
}
