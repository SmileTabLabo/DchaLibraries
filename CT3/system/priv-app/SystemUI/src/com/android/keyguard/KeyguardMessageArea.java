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
/* loaded from: a.zip:com/android/keyguard/KeyguardMessageArea.class */
public class KeyguardMessageArea extends TextView implements SecurityMessageDisplay {
    private static final Object ANNOUNCE_TOKEN = new Object();
    private final Runnable mClearMessageRunnable;
    private final int mDefaultColor;
    private final Handler mHandler;
    private KeyguardUpdateMonitorCallback mInfoCallback;
    CharSequence mMessage;
    private int mNextMessageColor;
    private KeyguardSecurityModel mSecurityModel;
    private CharSequence mSeparator;
    long mTimeout;
    private final KeyguardUpdateMonitor mUpdateMonitor;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/keyguard/KeyguardMessageArea$AnnounceRunnable.class */
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

    public KeyguardMessageArea(Context context) {
        this(context, null);
    }

    public KeyguardMessageArea(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTimeout = 5000L;
        this.mNextMessageColor = -1;
        this.mClearMessageRunnable = new Runnable(this) { // from class: com.android.keyguard.KeyguardMessageArea.1
            final KeyguardMessageArea this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mMessage = null;
                this.this$0.update();
            }
        };
        this.mInfoCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.android.keyguard.KeyguardMessageArea.2
            final KeyguardMessageArea this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFinishedGoingToSleep(int i) {
                this.this$0.setSelected(false);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStartedWakingUp() {
                this.this$0.setSelected(true);
            }
        };
        setLayerType(2, null);
        this.mSecurityModel = new KeyguardSecurityModel(context);
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(getContext());
        this.mUpdateMonitor.registerCallback(this.mInfoCallback);
        this.mHandler = new Handler(Looper.myLooper());
        this.mSeparator = getResources().getString(17040672);
        this.mDefaultColor = getCurrentTextColor();
        update();
    }

    private void clearMessage() {
        this.mHandler.removeCallbacks(this.mClearMessageRunnable);
        this.mHandler.post(this.mClearMessageRunnable);
    }

    public static SecurityMessageDisplay findSecurityMessageDisplay(View view) {
        KeyguardMessageArea keyguardMessageArea = (KeyguardMessageArea) view.findViewById(R$id.keyguard_message_area);
        if (keyguardMessageArea == null) {
            throw new RuntimeException("Can't find keyguard_message_area in " + view.getClass());
        }
        return keyguardMessageArea;
    }

    private void securityMessageChanged(CharSequence charSequence) {
        this.mMessage = charSequence;
        update();
        this.mHandler.removeCallbacks(this.mClearMessageRunnable);
        if (this.mTimeout > 0) {
            this.mHandler.postDelayed(this.mClearMessageRunnable, this.mTimeout);
        }
        this.mHandler.removeCallbacksAndMessages(ANNOUNCE_TOKEN);
        this.mHandler.postAtTime(new AnnounceRunnable(this, getText()), ANNOUNCE_TOKEN, SystemClock.uptimeMillis() + 250);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void update() {
        CharSequence charSequence = this.mMessage;
        setVisibility(TextUtils.isEmpty(charSequence) ? 4 : 0);
        if (this.mSecurityModel.getSecurityMode() == KeyguardSecurityModel.SecurityMode.AntiTheft) {
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

    @Override // android.view.View
    protected void onFinishInflate() {
        setSelected(KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceInteractive());
    }

    @Override // com.android.keyguard.SecurityMessageDisplay
    public void setMessage(int i, boolean z) {
        if (i == 0 || !z) {
            clearMessage();
        } else {
            securityMessageChanged(getContext().getResources().getText(i));
        }
    }

    @Override // com.android.keyguard.SecurityMessageDisplay
    public void setMessage(int i, boolean z, Object... objArr) {
        if (i == 0 || !z) {
            clearMessage();
        } else {
            securityMessageChanged(getContext().getString(i, objArr));
        }
    }

    @Override // com.android.keyguard.SecurityMessageDisplay
    public void setMessage(CharSequence charSequence, boolean z) {
        if (TextUtils.isEmpty(charSequence) || !z) {
            clearMessage();
        } else {
            securityMessageChanged(charSequence);
        }
    }

    @Override // com.android.keyguard.SecurityMessageDisplay
    public void setNextMessageColor(int i) {
        this.mNextMessageColor = i;
    }

    @Override // com.android.keyguard.SecurityMessageDisplay
    public void setTimeout(int i) {
        this.mTimeout = i;
    }
}
