package com.android.keyguard;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.keyguard.KeyguardSecurityModel;
import com.mediatek.keyguard.AntiTheft.AntiTheftManager;
import java.lang.ref.WeakReference;
/* loaded from: classes.dex */
public class KeyguardMessageArea extends TextView implements SecurityMessageDisplay {
    private static final Object ANNOUNCE_TOKEN = new Object();
    private final int mDefaultColor;
    private final Handler mHandler;
    private KeyguardUpdateMonitorCallback mInfoCallback;
    private CharSequence mMessage;
    private int mNextMessageColor;
    private KeyguardSecurityModel mSecurityModel;
    private CharSequence mSeparator;

    public KeyguardMessageArea(Context context) {
        this(context, null);
    }

    public KeyguardMessageArea(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, KeyguardUpdateMonitor.getInstance(context));
    }

    public KeyguardMessageArea(Context context, AttributeSet attributeSet, KeyguardUpdateMonitor keyguardUpdateMonitor) {
        super(context, attributeSet);
        this.mNextMessageColor = -1;
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.KeyguardMessageArea.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFinishedGoingToSleep(int i) {
                KeyguardMessageArea.this.setSelected(false);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStartedWakingUp() {
                KeyguardMessageArea.this.setSelected(true);
            }
        };
        setLayerType(2, null);
        this.mSecurityModel = new KeyguardSecurityModel(context);
        this.mSeparator = getResources().getString(17040110);
        keyguardUpdateMonitor.registerCallback(this.mInfoCallback);
        this.mHandler = new Handler(Looper.myLooper());
        this.mDefaultColor = getCurrentTextColor();
        update();
    }

    @Override // com.android.keyguard.SecurityMessageDisplay
    public void setNextMessageColor(int i) {
        this.mNextMessageColor = i;
    }

    @Override // com.android.keyguard.SecurityMessageDisplay
    public void setMessage(CharSequence charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            securityMessageChanged(charSequence);
        } else {
            clearMessage();
        }
    }

    @Override // com.android.keyguard.SecurityMessageDisplay
    public void setMessage(int i) {
        CharSequence charSequence;
        if (i != 0) {
            charSequence = getContext().getResources().getText(i);
        } else {
            charSequence = null;
        }
        setMessage(charSequence);
    }

    public static SecurityMessageDisplay findSecurityMessageDisplay(View view) {
        KeyguardMessageArea keyguardMessageArea = (KeyguardMessageArea) view.findViewById(com.android.systemui.R.id.keyguard_message_area);
        if (keyguardMessageArea == null) {
            throw new RuntimeException("Can't find keyguard_message_area in " + view.getClass());
        }
        return keyguardMessageArea;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        setSelected(KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceInteractive());
    }

    private void securityMessageChanged(CharSequence charSequence) {
        this.mMessage = charSequence;
        update();
        this.mHandler.removeCallbacksAndMessages(ANNOUNCE_TOKEN);
        this.mHandler.postAtTime(new AnnounceRunnable(this, getText()), ANNOUNCE_TOKEN, SystemClock.uptimeMillis() + 250);
    }

    private void clearMessage() {
        this.mMessage = null;
        update();
    }

    private void update() {
        CharSequence charSequence = this.mMessage;
        setVisibility(TextUtils.isEmpty(charSequence) ? 4 : 0);
        if (this.mSecurityModel.getSecurityMode(KeyguardUpdateMonitor.getCurrentUser()) == KeyguardSecurityModel.SecurityMode.AntiTheft) {
            setText(AntiTheftManager.getAntiTheftMessageAreaText(charSequence, this.mSeparator));
        } else {
            setText(charSequence);
        }
        int i = this.mDefaultColor;
        if (this.mNextMessageColor != -1) {
            i = this.mNextMessageColor;
            this.mNextMessageColor = -1;
        }
        setTextColor(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class AnnounceRunnable implements Runnable {
        private final WeakReference<View> mHost;
        private final CharSequence mTextToAnnounce;

        AnnounceRunnable(View view, CharSequence charSequence) {
            this.mHost = new WeakReference<>(view);
            this.mTextToAnnounce = charSequence;
        }

        @Override // java.lang.Runnable
        public void run() {
            View view = this.mHost.get();
            if (view != null) {
                view.announceForAccessibility(this.mTextToAnnounce);
            }
        }
    }
}
