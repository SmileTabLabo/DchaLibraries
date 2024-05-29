package com.mediatek.keyguard.Telephony;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUtils;
import com.mediatek.keyguard.PowerOffAlarm.PowerOffAlarmManager;
import java.util.LinkedList;
import java.util.Queue;
/* loaded from: a.zip:com/mediatek/keyguard/Telephony/KeyguardDialogManager.class */
public class KeyguardDialogManager {
    private static KeyguardDialogManager sInstance;
    private final Context mContext;
    private DialogSequenceManager mDialogSequenceManager = new DialogSequenceManager(this);
    private KeyguardUpdateMonitor mUpdateMonitor;

    /* loaded from: a.zip:com/mediatek/keyguard/Telephony/KeyguardDialogManager$DialogSequenceManager.class */
    private class DialogSequenceManager {
        private Queue<DialogShowCallBack> mDialogShowCallbackQueue;
        final KeyguardDialogManager this$0;
        private boolean mInnerDialogShowing = false;
        private boolean mLocked = false;
        private ContentObserver mDialogSequenceObserver = new ContentObserver(this, new Handler()) { // from class: com.mediatek.keyguard.Telephony.KeyguardDialogManager.DialogSequenceManager.1
            final DialogSequenceManager this$1;

            {
                this.this$1 = this;
            }

            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                int queryDialogSequenceSeetings = this.this$1.queryDialogSequenceSeetings();
                Log.d("KeyguardDialogManager", "DialogSequenceManager DialogSequenceObserver--onChange()--dialog_sequence_settings = " + queryDialogSequenceSeetings);
                if (queryDialogSequenceSeetings == 0) {
                    this.this$1.setLocked(false);
                    this.this$1.handleShowDialog();
                } else if (queryDialogSequenceSeetings == 1) {
                    this.this$1.setLocked(true);
                    this.this$1.handleShowDialog();
                }
            }
        };

        public DialogSequenceManager(KeyguardDialogManager keyguardDialogManager) {
            this.this$0 = keyguardDialogManager;
            Log.d("KeyguardDialogManager", "DialogSequenceManager DialogSequenceManager()");
            this.mDialogShowCallbackQueue = new LinkedList();
            keyguardDialogManager.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("dialog_sequence_settings"), false, this.mDialogSequenceObserver);
        }

        private boolean enableShow() {
            Log.d("KeyguardDialogManager", "DialogSequenceManager --enableShow()-- !mDialogShowCallbackQueue.isEmpty() = " + (!this.mDialogShowCallbackQueue.isEmpty()) + " !getInnerDialogShowing() = " + (!getInnerDialogShowing()) + " !isOtherModuleShowing() = " + (!isOtherModuleShowing()) + "!isAlarmBoot() = " + (!PowerOffAlarmManager.isAlarmBoot()) + " isDeviceProvisioned() = " + this.this$0.mUpdateMonitor.isDeviceProvisioned());
            boolean z = false;
            if (!this.mDialogShowCallbackQueue.isEmpty()) {
                if (getInnerDialogShowing()) {
                    z = false;
                } else {
                    z = false;
                    if (!isOtherModuleShowing()) {
                        z = false;
                        if (!PowerOffAlarmManager.isAlarmBoot()) {
                            z = false;
                            if (this.this$0.mUpdateMonitor.isDeviceProvisioned()) {
                                z = false;
                                if (!KeyguardUtils.isSystemEncrypted()) {
                                    z = true;
                                }
                            }
                        }
                    }
                }
            }
            return z;
        }

        private boolean getInnerDialogShowing() {
            return this.mInnerDialogShowing;
        }

        private boolean getLocked() {
            return this.mLocked;
        }

        private boolean isOtherModuleShowing() {
            int queryDialogSequenceSeetings = queryDialogSequenceSeetings();
            Log.d("KeyguardDialogManager", "DialogSequenceManager --isOtherModuleShowing()--dialog_sequence_settings = " + queryDialogSequenceSeetings);
            return (queryDialogSequenceSeetings == 0 || queryDialogSequenceSeetings == 1) ? false : true;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public int queryDialogSequenceSeetings() {
            return Settings.System.getInt(this.this$0.mContext.getContentResolver(), "dialog_sequence_settings", 0);
        }

        private void setInnerDialogShowing(boolean z) {
            this.mInnerDialogShowing = z;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setLocked(boolean z) {
            this.mLocked = z;
        }

        public void handleShowDialog() {
            Log.d("KeyguardDialogManager", "DialogSequenceManager --handleShowDialog()--enableShow() = " + enableShow());
            if (enableShow()) {
                if (!getLocked()) {
                    Log.d("KeyguardDialogManager", "DialogSequenceManager --handleShowDialog()--System.putInt( dialog_sequence_settings value = 1");
                    Settings.System.putInt(this.this$0.mContext.getContentResolver(), "dialog_sequence_settings", 1);
                    return;
                }
                DialogShowCallBack poll = this.mDialogShowCallbackQueue.poll();
                Log.d("KeyguardDialogManager", "DialogSequenceManager --handleShowDialog()--dialogCallBack = " + poll);
                if (poll != null) {
                    poll.show();
                    setInnerDialogShowing(true);
                }
            }
        }

        public void reportDialogClose() {
            Log.d("KeyguardDialogManager", "DialogSequenceManager --reportDialogClose()--mDialogShowCallbackQueue.isEmpty() = " + this.mDialogShowCallbackQueue.isEmpty());
            setInnerDialogShowing(false);
            if (!this.mDialogShowCallbackQueue.isEmpty()) {
                handleShowDialog();
                return;
            }
            Log.d("KeyguardDialogManager", "DialogSequenceManager --reportDialogClose()--System.putInt( dialog_sequence_settings value = 0 --setLocked(false)--");
            Settings.System.putInt(this.this$0.mContext.getContentResolver(), "dialog_sequence_settings", 0);
            setLocked(false);
        }

        public void requestShowDialog(DialogShowCallBack dialogShowCallBack) {
            Log.d("KeyguardDialogManager", "DialogSequenceManager --requestShowDialog()");
            this.mDialogShowCallbackQueue.add(dialogShowCallBack);
            handleShowDialog();
        }
    }

    /* loaded from: a.zip:com/mediatek/keyguard/Telephony/KeyguardDialogManager$DialogShowCallBack.class */
    public interface DialogShowCallBack {
        void show();
    }

    private KeyguardDialogManager(Context context) {
        this.mContext = context;
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
    }

    public static KeyguardDialogManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new KeyguardDialogManager(context);
        }
        return sInstance;
    }

    public void reportDialogClose() {
        this.mDialogSequenceManager.reportDialogClose();
    }

    public void requestShowDialog(DialogShowCallBack dialogShowCallBack) {
        this.mDialogSequenceManager.requestShowDialog(dialogShowCallBack);
    }
}
