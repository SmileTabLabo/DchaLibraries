package jp.co.benesse.dcha.systemsettings;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/WpsDialog.class */
public class WpsDialog extends AlertDialog implements View.OnClickListener {
    private Button mButton;
    private Context mContext;
    DialogState mDialogState;
    private final IntentFilter mFilter;
    private Handler mHandler;
    private String mMsgString;
    private ProgressBar mProgressBar;
    private BroadcastReceiver mReceiver;
    private TextView mTextView;
    private ProgressBar mTimeoutBar;
    private Timer mTimer;
    private View mView;
    private WifiManager mWifiManager;
    private WifiManager.WpsCallback mWpsListener;
    private int mWpsSetup;

    /* renamed from: jp.co.benesse.dcha.systemsettings.WpsDialog$2  reason: invalid class name */
    /* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/WpsDialog$2.class */
    class AnonymousClass2 extends TimerTask {
        final WpsDialog this$0;

        AnonymousClass2(WpsDialog wpsDialog) {
            this.this$0 = wpsDialog;
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            Logger.d("WpsDialog", "run 0001");
            this.this$0.mHandler.post(new Runnable(this) { // from class: jp.co.benesse.dcha.systemsettings.WpsDialog.2.1
                final AnonymousClass2 this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    Logger.d("WpsDialog", "run 0002");
                    this.this$1.this$0.mTimeoutBar.incrementProgressBy(1);
                    Logger.d("WpsDialog", "run 0003");
                }
            });
            Logger.d("WpsDialog", "run 0004");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/WpsDialog$DialogState.class */
    public enum DialogState {
        WPS_INIT,
        WPS_START,
        WPS_COMPLETE,
        CONNECTED,
        WPS_FAILED;

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static DialogState[] valuesCustom() {
            return values();
        }
    }

    public WpsDialog(Context context, int i) {
        super(context);
        this.mHandler = new Handler();
        this.mMsgString = "";
        this.mDialogState = DialogState.WPS_INIT;
        Logger.d("WpsDialog", "WpsDialog 0001");
        this.mContext = context;
        this.mWpsSetup = i;
        this.mWpsListener = new WifiManager.WpsCallback(this) { // from class: jp.co.benesse.dcha.systemsettings.WpsDialog.1WpsListener
            final WpsDialog this$0;

            {
                this.this$0 = this;
            }

            @Override // android.net.wifi.WifiManager.WpsCallback
            public void onFailed(int i2) {
                String string;
                Logger.d("WpsDialog", "onFailure 0001");
                switch (i2) {
                    case 1:
                        Logger.d("WpsDialog", "onFailure 0005");
                        string = this.this$0.mContext.getString(2131230818);
                        break;
                    case 2:
                    default:
                        Logger.d("WpsDialog", "onFailure 0006");
                        string = this.this$0.mContext.getString(2131230819);
                        break;
                    case 3:
                        Logger.d("WpsDialog", "onFailure 0002");
                        string = this.this$0.mContext.getString(2131230823);
                        break;
                    case 4:
                        Logger.d("WpsDialog", "onFailure 0003");
                        string = this.this$0.mContext.getString(2131230820);
                        break;
                    case 5:
                        Logger.d("WpsDialog", "onFailure 0004");
                        string = this.this$0.mContext.getString(2131230821);
                        break;
                }
                this.this$0.updateDialog(DialogState.WPS_FAILED, string);
                Logger.d("WpsDialog", "onFailure 0007");
            }

            @Override // android.net.wifi.WifiManager.WpsCallback
            public void onStarted(String str) {
                Logger.d("WpsDialog", "onStartSuccess 0001");
                if (str != null) {
                    Logger.d("WpsDialog", "onStartSuccess 0002");
                    this.this$0.updateDialog(DialogState.WPS_START, String.format(this.this$0.mContext.getString(2131230815), str));
                } else {
                    Logger.d("WpsDialog", "onStartSuccess 0003");
                    this.this$0.updateDialog(DialogState.WPS_START, this.this$0.mContext.getString(2131230814));
                }
                Logger.d("WpsDialog", "onStartSuccess 0004");
            }

            @Override // android.net.wifi.WifiManager.WpsCallback
            public void onSucceeded() {
                Logger.d("WpsDialog", "onCompletion 0001");
                this.this$0.updateDialog(DialogState.WPS_COMPLETE, this.this$0.mContext.getString(2131230816));
                Logger.d("WpsDialog", "onCompletion 0002");
            }
        };
        this.mFilter = new IntentFilter();
        this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mReceiver = new BroadcastReceiver(this) { // from class: jp.co.benesse.dcha.systemsettings.WpsDialog.1
            final WpsDialog this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                Logger.d("WpsDialog", "onReceive 0001");
                this.this$0.handleEvent(context2, intent);
                Logger.d("WpsDialog", "onReceive 0002");
            }
        };
        setCanceledOnTouchOutside(false);
        Logger.d("WpsDialog", "WpsDialog 0002");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleEvent(Context context, Intent intent) {
        Logger.d("WpsDialog", "handleEvent 0001");
        if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
            Logger.d("WpsDialog", "handleEvent 0002");
            if (((NetworkInfo) intent.getParcelableExtra("networkInfo")).getDetailedState() == NetworkInfo.DetailedState.CONNECTED && this.mDialogState == DialogState.WPS_COMPLETE) {
                Logger.d("WpsDialog", "handleEvent 0003");
                WifiInfo connectionInfo = this.mWifiManager.getConnectionInfo();
                if (connectionInfo != null) {
                    Logger.d("WpsDialog", "handleEvent 0004");
                    updateDialog(DialogState.CONNECTED, String.format(this.mContext.getString(2131230817), connectionInfo.getSSID()));
                }
            }
        }
        Logger.d("WpsDialog", "handleEvent 0005");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDialog(DialogState dialogState, String str) {
        Logger.d("WpsDialog", "updateDialog 0001");
        if (this.mDialogState.ordinal() >= dialogState.ordinal()) {
            Logger.d("WpsDialog", "updateDialog 0002");
            return;
        }
        this.mDialogState = dialogState;
        this.mMsgString = str;
        this.mHandler.post(new Runnable(this, dialogState, str) { // from class: jp.co.benesse.dcha.systemsettings.WpsDialog.3

            /* renamed from: -jp-co-benesse-dcha-systemsettings-WpsDialog$DialogStateSwitchesValues  reason: not valid java name */
            private static final int[] f0x31dcdfdc = null;
            final WpsDialog this$0;
            final String val$msg;
            final DialogState val$state;

            /* renamed from: -getjp-co-benesse-dcha-systemsettings-WpsDialog$DialogStateSwitchesValues  reason: not valid java name */
            private static /* synthetic */ int[] m47x17bc580() {
                if (f0x31dcdfdc != null) {
                    return f0x31dcdfdc;
                }
                int[] iArr = new int[DialogState.valuesCustom().length];
                try {
                    iArr[DialogState.CONNECTED.ordinal()] = 1;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[DialogState.WPS_COMPLETE.ordinal()] = 2;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[DialogState.WPS_FAILED.ordinal()] = 3;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[DialogState.WPS_INIT.ordinal()] = 4;
                } catch (NoSuchFieldError e4) {
                }
                try {
                    iArr[DialogState.WPS_START.ordinal()] = 5;
                } catch (NoSuchFieldError e5) {
                }
                f0x31dcdfdc = iArr;
                return iArr;
            }

            {
                this.this$0 = this;
                this.val$state = dialogState;
                this.val$msg = str;
            }

            /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
            /* JADX WARN: Removed duplicated region for block: B:10:0x00d1  */
            @Override // java.lang.Runnable
            /*
                Code decompiled incorrectly, please refer to instructions dump.
            */
            public void run() {
                Logger.d("WpsDialog", "run 0005");
                switch (m47x17bc580()[this.val$state.ordinal()]) {
                    case 1:
                        Logger.d("WpsDialog", "run 0007");
                        Logger.d("WpsDialog", "run 0008");
                        this.this$0.mButton.setText(this.this$0.mContext.getString(2131230902));
                        this.this$0.mTimeoutBar.setVisibility(8);
                        this.this$0.mProgressBar.setVisibility(8);
                        if (this.this$0.mReceiver != null) {
                            Logger.d("WpsDialog", "run 0009");
                            this.this$0.mContext.unregisterReceiver(this.this$0.mReceiver);
                            this.this$0.mReceiver = null;
                            break;
                        }
                        break;
                    case 2:
                        Logger.d("WpsDialog", "run 0006");
                        this.this$0.mTimeoutBar.setVisibility(8);
                        this.this$0.mProgressBar.setVisibility(0);
                        break;
                    case 3:
                        Logger.d("WpsDialog", "run 0008");
                        this.this$0.mButton.setText(this.this$0.mContext.getString(2131230902));
                        this.this$0.mTimeoutBar.setVisibility(8);
                        this.this$0.mProgressBar.setVisibility(8);
                        if (this.this$0.mReceiver != null) {
                        }
                        break;
                }
                this.this$0.mTextView.setText(this.val$msg);
                Logger.d("WpsDialog", "run 0010");
            }
        });
        Logger.d("WpsDialog", "updateDialog 0003");
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Logger.d("WpsDialog", "onClick 0001");
        if (view.getId() == this.mButton.getId()) {
            Logger.d("WpsDialog", "onClick 0002");
            dismiss();
        }
        Logger.d("WpsDialog", "onClick 0003");
    }

    @Override // android.app.AlertDialog, android.app.Dialog
    protected void onCreate(Bundle bundle) {
        Logger.d("WpsDialog", "onCreate 0001");
        this.mView = getLayoutInflater().inflate(2130903053, (ViewGroup) null);
        this.mTextView = (TextView) this.mView.findViewById(2131361906);
        this.mTextView.setText(2131230813);
        this.mTimeoutBar = (ProgressBar) this.mView.findViewById(2131361907);
        this.mTimeoutBar.setMax(120);
        this.mTimeoutBar.setProgress(0);
        this.mProgressBar = (ProgressBar) this.mView.findViewById(2131361908);
        this.mProgressBar.setVisibility(8);
        this.mButton = (Button) this.mView.findViewById(2131361909);
        this.mButton.setText(2131230861);
        setView(this.mView);
        this.mButton.setOnClickListener(this);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (bundle == null) {
            Logger.d("WpsDialog", "onCreate 0002");
            WpsInfo wpsInfo = new WpsInfo();
            wpsInfo.setup = this.mWpsSetup;
            this.mWifiManager.startWps(wpsInfo, this.mWpsListener);
        }
        super.onCreate(bundle);
        Logger.d("WpsDialog", "onCreate 0003");
    }

    @Override // android.app.Dialog
    public void onRestoreInstanceState(Bundle bundle) {
        Logger.d("WpsDialog", "onRestoreInstanceState 0001");
        if (bundle != null) {
            Logger.d("WpsDialog", "onRestoreInstanceState 0002");
            super.onRestoreInstanceState(bundle);
            updateDialog(DialogState.valueOf(bundle.getString("android:dialogState")), bundle.getString("android:dialogMsg"));
        }
        Logger.d("WpsDialog", "onRestoreInstanceState 0003");
    }

    @Override // android.app.Dialog
    public Bundle onSaveInstanceState() {
        Logger.d("WpsDialog", "onSaveInstanceState 0001");
        Bundle onSaveInstanceState = super.onSaveInstanceState();
        onSaveInstanceState.putString("android:dialogState", this.mDialogState.toString());
        onSaveInstanceState.putString("android:dialogMsg", this.mMsgString);
        Logger.d("WpsDialog", "onSaveInstanceState 0002");
        return onSaveInstanceState;
    }

    @Override // android.app.Dialog
    protected void onStart() {
        Logger.d("WpsDialog", "onStart 0001");
        this.mTimer = new Timer(false);
        this.mTimer.schedule(new AnonymousClass2(this), 1000L, 1000L);
        this.mContext.registerReceiver(this.mReceiver, this.mFilter);
        Logger.d("WpsDialog", "onStart 0002");
    }

    @Override // android.app.Dialog
    protected void onStop() {
        Logger.d("WpsDialog", "onStop 0001");
        if (this.mDialogState != DialogState.WPS_COMPLETE) {
            Logger.d("WpsDialog", "onStop 0002");
            this.mWifiManager.cancelWps(null);
        }
        if (this.mReceiver != null) {
            Logger.d("WpsDialog", "onStop 0003");
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
        if (this.mTimer != null) {
            Logger.d("WpsDialog", "onStop 0004");
            this.mTimer.cancel();
        }
        Logger.d("WpsDialog", "onStop 0005");
    }
}
