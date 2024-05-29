package com.android.systemui.volume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;
import com.android.systemui.statusbar.phone.SystemUIDialog;
/* loaded from: a.zip:com/android/systemui/volume/SafetyWarningDialog.class */
public abstract class SafetyWarningDialog extends SystemUIDialog implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener {
    private static final String TAG = Util.logTag(SafetyWarningDialog.class);
    private final AudioManager mAudioManager;
    private final Context mContext;
    private boolean mNewVolumeUp;
    private final BroadcastReceiver mReceiver;
    private long mShowTime;

    public SafetyWarningDialog(Context context, AudioManager audioManager) {
        super(context);
        this.mReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.volume.SafetyWarningDialog.1
            final SafetyWarningDialog this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                    if (D.BUG) {
                        Log.d(SafetyWarningDialog.TAG, "Received ACTION_CLOSE_SYSTEM_DIALOGS");
                    }
                    this.this$0.cancel();
                    this.this$0.cleanUp();
                }
            }
        };
        this.mContext = context;
        this.mAudioManager = audioManager;
        getWindow().setType(2010);
        setShowForAllUsers(true);
        setMessage(this.mContext.getString(17040674));
        setButton(-1, this.mContext.getString(17039379), this);
        setButton(-2, this.mContext.getString(17039369), (DialogInterface.OnClickListener) null);
        setOnDismissListener(this);
        context.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    protected abstract void cleanUp();

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        this.mAudioManager.disableSafeMediaVolume();
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        this.mContext.unregisterReceiver(this.mReceiver);
        cleanUp();
    }

    @Override // android.app.AlertDialog, android.app.Dialog, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 24 && keyEvent.getRepeatCount() == 0) {
            this.mNewVolumeUp = true;
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override // android.app.AlertDialog, android.app.Dialog, android.view.KeyEvent.Callback
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (i == 24 && this.mNewVolumeUp && System.currentTimeMillis() - this.mShowTime > 1000) {
            if (D.BUG) {
                Log.d(TAG, "Confirmed warning via VOLUME_UP");
            }
            this.mAudioManager.disableSafeMediaVolume();
            dismiss();
        }
        return super.onKeyUp(i, keyEvent);
    }

    @Override // android.app.Dialog
    protected void onStart() {
        super.onStart();
        this.mShowTime = System.currentTimeMillis();
    }
}
