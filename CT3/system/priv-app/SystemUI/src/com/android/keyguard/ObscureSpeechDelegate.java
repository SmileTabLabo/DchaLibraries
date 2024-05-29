package com.android.keyguard;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
/* loaded from: a.zip:com/android/keyguard/ObscureSpeechDelegate.class */
class ObscureSpeechDelegate extends View.AccessibilityDelegate {
    static boolean sAnnouncedHeadset = false;
    private final AudioManager mAudioManager;
    private final ContentResolver mContentResolver;

    public ObscureSpeechDelegate(Context context) {
        this.mContentResolver = context.getContentResolver();
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
    }

    private boolean shouldObscureSpeech() {
        return (Settings.Secure.getIntForUser(this.mContentResolver, "speak_password", 0, -2) != 0 || this.mAudioManager.isWiredHeadsetOn() || this.mAudioManager.isBluetoothA2dpOn()) ? false : true;
    }

    @Override // android.view.View.AccessibilityDelegate
    public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
        if (shouldObscureSpeech()) {
            Context context = view.getContext();
            accessibilityNodeInfo.setText(null);
            accessibilityNodeInfo.setContentDescription(context.getString(17040567));
        }
    }

    @Override // android.view.View.AccessibilityDelegate
    public void onPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
        super.onPopulateAccessibilityEvent(view, accessibilityEvent);
        if (accessibilityEvent.getEventType() == 16384 || !shouldObscureSpeech()) {
            return;
        }
        accessibilityEvent.getText().clear();
        accessibilityEvent.setContentDescription(view.getContext().getString(17040567));
    }

    @Override // android.view.View.AccessibilityDelegate
    public void sendAccessibilityEvent(View view, int i) {
        super.sendAccessibilityEvent(view, i);
        if (i == 32768 && !sAnnouncedHeadset && shouldObscureSpeech()) {
            sAnnouncedHeadset = true;
            view.announceForAccessibility(view.getContext().getString(17040566));
        }
    }
}
