package com.android.systemui.keyboard;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Toast;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;
import com.android.settingslib.bluetooth.Utils;
import com.android.systemui.SystemUI;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/keyboard/KeyboardUI.class */
public class KeyboardUI extends SystemUI implements InputManager.OnTabletModeChangedListener {
    private boolean mBootCompleted;
    private long mBootCompletedTime;
    private CachedBluetoothDeviceManager mCachedDeviceManager;
    protected volatile Context mContext;
    private BluetoothDialog mDialog;
    private boolean mEnabled;
    private volatile KeyboardHandler mHandler;
    private String mKeyboardName;
    private LocalBluetoothAdapter mLocalBluetoothAdapter;
    private LocalBluetoothProfileManager mProfileManager;
    private ScanCallback mScanCallback;
    private int mState;
    private volatile KeyboardUIHandler mUIHandler;
    private int mInTabletMode = -1;
    private int mScanAttempt = 0;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/keyboard/KeyboardUI$BluetoothCallbackHandler.class */
    public final class BluetoothCallbackHandler implements BluetoothCallback {
        final KeyboardUI this$0;

        private BluetoothCallbackHandler(KeyboardUI keyboardUI) {
            this.this$0 = keyboardUI;
        }

        /* synthetic */ BluetoothCallbackHandler(KeyboardUI keyboardUI, BluetoothCallbackHandler bluetoothCallbackHandler) {
            this(keyboardUI);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onBluetoothStateChanged(int i) {
            this.this$0.mHandler.obtainMessage(4, i, 0).sendToTarget();
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onDeviceAdded(CachedBluetoothDevice cachedBluetoothDevice) {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onDeviceBondStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
            this.this$0.mHandler.obtainMessage(5, i, 0, cachedBluetoothDevice).sendToTarget();
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onDeviceDeleted(CachedBluetoothDevice cachedBluetoothDevice) {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onScanningStateChanged(boolean z) {
        }
    }

    /* loaded from: a.zip:com/android/systemui/keyboard/KeyboardUI$BluetoothDialogClickListener.class */
    private final class BluetoothDialogClickListener implements DialogInterface.OnClickListener {
        final KeyboardUI this$0;

        private BluetoothDialogClickListener(KeyboardUI keyboardUI) {
            this.this$0 = keyboardUI;
        }

        /* synthetic */ BluetoothDialogClickListener(KeyboardUI keyboardUI, BluetoothDialogClickListener bluetoothDialogClickListener) {
            this(keyboardUI);
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            this.this$0.mHandler.obtainMessage(3, -1 == i ? 1 : 0, 0).sendToTarget();
            this.this$0.mDialog = null;
        }
    }

    /* loaded from: a.zip:com/android/systemui/keyboard/KeyboardUI$BluetoothDialogDismissListener.class */
    private final class BluetoothDialogDismissListener implements DialogInterface.OnDismissListener {
        final KeyboardUI this$0;

        private BluetoothDialogDismissListener(KeyboardUI keyboardUI) {
            this.this$0 = keyboardUI;
        }

        /* synthetic */ BluetoothDialogDismissListener(KeyboardUI keyboardUI, BluetoothDialogDismissListener bluetoothDialogDismissListener) {
            this(keyboardUI);
        }

        @Override // android.content.DialogInterface.OnDismissListener
        public void onDismiss(DialogInterface dialogInterface) {
            this.this$0.mDialog = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/keyboard/KeyboardUI$BluetoothErrorListener.class */
    public final class BluetoothErrorListener implements Utils.ErrorListener {
        final KeyboardUI this$0;

        private BluetoothErrorListener(KeyboardUI keyboardUI) {
            this.this$0 = keyboardUI;
        }

        /* synthetic */ BluetoothErrorListener(KeyboardUI keyboardUI, BluetoothErrorListener bluetoothErrorListener) {
            this(keyboardUI);
        }

        @Override // com.android.settingslib.bluetooth.Utils.ErrorListener
        public void onShowError(Context context, String str, int i) {
            this.this$0.mHandler.obtainMessage(11, i, 0, new Pair(context, str)).sendToTarget();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/keyboard/KeyboardUI$KeyboardHandler.class */
    public final class KeyboardHandler extends Handler {
        final KeyboardUI this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public KeyboardHandler(KeyboardUI keyboardUI, Looper looper) {
            super(looper, null, true);
            this.this$0 = keyboardUI;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            boolean z = true;
            switch (message.what) {
                case 0:
                    this.this$0.init();
                    return;
                case 1:
                    this.this$0.onBootCompletedInternal();
                    return;
                case 2:
                    this.this$0.processKeyboardState();
                    return;
                case 3:
                    if (message.arg1 != 1) {
                        z = false;
                    }
                    if (z) {
                        this.this$0.mLocalBluetoothAdapter.enable();
                        return;
                    } else {
                        this.this$0.mState = 8;
                        return;
                    }
                case 4:
                    this.this$0.onBluetoothStateChangedInternal(message.arg1);
                    return;
                case 5:
                    this.this$0.onDeviceBondStateChangedInternal((CachedBluetoothDevice) message.obj, message.arg1);
                    return;
                case 6:
                    this.this$0.onDeviceAddedInternal(this.this$0.getCachedBluetoothDevice((BluetoothDevice) message.obj));
                    return;
                case 7:
                    this.this$0.onBleScanFailedInternal();
                    return;
                case 8:
                case 9:
                default:
                    return;
                case 10:
                    this.this$0.bleAbortScanInternal(message.arg1);
                    return;
                case 11:
                    Pair pair = (Pair) message.obj;
                    this.this$0.onShowErrorInternal((Context) pair.first, (String) pair.second, message.arg1);
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/keyboard/KeyboardUI$KeyboardScanCallback.class */
    public final class KeyboardScanCallback extends ScanCallback {
        final KeyboardUI this$0;

        private KeyboardScanCallback(KeyboardUI keyboardUI) {
            this.this$0 = keyboardUI;
        }

        /* synthetic */ KeyboardScanCallback(KeyboardUI keyboardUI, KeyboardScanCallback keyboardScanCallback) {
            this(keyboardUI);
        }

        private boolean isDeviceDiscoverable(ScanResult scanResult) {
            boolean z = false;
            if ((scanResult.getScanRecord().getAdvertiseFlags() & 3) != 0) {
                z = true;
            }
            return z;
        }

        @Override // android.bluetooth.le.ScanCallback
        public void onBatchScanResults(List<ScanResult> list) {
            int i = Integer.MIN_VALUE;
            BluetoothDevice bluetoothDevice = null;
            for (ScanResult scanResult : list) {
                if (isDeviceDiscoverable(scanResult) && scanResult.getRssi() > i) {
                    bluetoothDevice = scanResult.getDevice();
                    i = scanResult.getRssi();
                }
            }
            if (bluetoothDevice != null) {
                this.this$0.mHandler.obtainMessage(6, bluetoothDevice).sendToTarget();
            }
        }

        @Override // android.bluetooth.le.ScanCallback
        public void onScanFailed(int i) {
            this.this$0.mHandler.obtainMessage(7).sendToTarget();
        }

        @Override // android.bluetooth.le.ScanCallback
        public void onScanResult(int i, ScanResult scanResult) {
            if (isDeviceDiscoverable(scanResult)) {
                this.this$0.mHandler.obtainMessage(6, scanResult.getDevice()).sendToTarget();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/keyboard/KeyboardUI$KeyboardUIHandler.class */
    public final class KeyboardUIHandler extends Handler {
        final KeyboardUI this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public KeyboardUIHandler(KeyboardUI keyboardUI) {
            super(Looper.getMainLooper(), null, true);
            this.this$0 = keyboardUI;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 8:
                    if (this.this$0.mDialog == null) {
                        BluetoothDialogClickListener bluetoothDialogClickListener = new BluetoothDialogClickListener(this.this$0, null);
                        BluetoothDialogDismissListener bluetoothDialogDismissListener = new BluetoothDialogDismissListener(this.this$0, null);
                        this.this$0.mDialog = new BluetoothDialog(this.this$0.mContext);
                        this.this$0.mDialog.setTitle(2131493751);
                        this.this$0.mDialog.setMessage(2131493752);
                        this.this$0.mDialog.setPositiveButton(2131493753, bluetoothDialogClickListener);
                        this.this$0.mDialog.setNegativeButton(17039360, bluetoothDialogClickListener);
                        this.this$0.mDialog.setOnDismissListener(bluetoothDialogDismissListener);
                        this.this$0.mDialog.show();
                        return;
                    }
                    return;
                case 9:
                    if (this.this$0.mDialog != null) {
                        this.this$0.mDialog.dismiss();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void bleAbortScanInternal(int i) {
        if (this.mState == 3 && i == this.mScanAttempt) {
            stopScanning();
            this.mState = 9;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public CachedBluetoothDevice getCachedBluetoothDevice(BluetoothDevice bluetoothDevice) {
        CachedBluetoothDevice findDevice = this.mCachedDeviceManager.findDevice(bluetoothDevice);
        CachedBluetoothDevice cachedBluetoothDevice = findDevice;
        if (findDevice == null) {
            cachedBluetoothDevice = this.mCachedDeviceManager.addDevice(this.mLocalBluetoothAdapter, this.mProfileManager, bluetoothDevice);
        }
        return cachedBluetoothDevice;
    }

    private CachedBluetoothDevice getDiscoveredKeyboard() {
        for (CachedBluetoothDevice cachedBluetoothDevice : this.mCachedDeviceManager.getCachedDevicesCopy()) {
            if (cachedBluetoothDevice.getName().equals(this.mKeyboardName)) {
                return cachedBluetoothDevice;
            }
        }
        return null;
    }

    private CachedBluetoothDevice getPairedKeyboard() {
        for (BluetoothDevice bluetoothDevice : this.mLocalBluetoothAdapter.getBondedDevices()) {
            if (this.mKeyboardName.equals(bluetoothDevice.getName())) {
                return getCachedBluetoothDevice(bluetoothDevice);
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void init() {
        LocalBluetoothManager localBluetoothManager;
        Context context = this.mContext;
        this.mKeyboardName = context.getString(17039468);
        if (TextUtils.isEmpty(this.mKeyboardName) || (localBluetoothManager = LocalBluetoothManager.getInstance(context, null)) == null) {
            return;
        }
        this.mEnabled = true;
        this.mCachedDeviceManager = localBluetoothManager.getCachedDeviceManager();
        this.mLocalBluetoothAdapter = localBluetoothManager.getBluetoothAdapter();
        this.mProfileManager = localBluetoothManager.getProfileManager();
        localBluetoothManager.getEventManager().registerCallback(new BluetoothCallbackHandler(this, null));
        Utils.setErrorListener(new BluetoothErrorListener(this, null));
        InputManager inputManager = (InputManager) context.getSystemService(InputManager.class);
        inputManager.registerOnTabletModeChangedListener(this, this.mHandler);
        this.mInTabletMode = inputManager.isInTabletMode();
        processKeyboardState();
        this.mUIHandler = new KeyboardUIHandler(this);
    }

    private boolean isUserSetupComplete() {
        boolean z = false;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, -2) != 0) {
            z = true;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBleScanFailedInternal() {
        this.mScanCallback = null;
        if (this.mState == 3) {
            this.mState = 9;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBluetoothStateChangedInternal(int i) {
        if (i == 12 && this.mState == 4) {
            processKeyboardState();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDeviceAddedInternal(CachedBluetoothDevice cachedBluetoothDevice) {
        if (this.mState == 3 && cachedBluetoothDevice.getName().equals(this.mKeyboardName)) {
            stopScanning();
            cachedBluetoothDevice.startPairing();
            this.mState = 5;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDeviceBondStateChangedInternal(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        if (this.mState == 5 && cachedBluetoothDevice.getName().equals(this.mKeyboardName)) {
            if (i == 12) {
                this.mState = 6;
            } else if (i == 10) {
                this.mState = 7;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onShowErrorInternal(Context context, String str, int i) {
        if ((this.mState == 5 || this.mState == 7) && this.mKeyboardName.equals(str)) {
            Toast.makeText(context, context.getString(i, str), 0).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void processKeyboardState() {
        this.mHandler.removeMessages(2);
        if (!this.mEnabled) {
            this.mState = -1;
        } else if (!this.mBootCompleted) {
            this.mState = 1;
        } else if (this.mInTabletMode != 0) {
            if (this.mState == 3) {
                stopScanning();
            } else if (this.mState == 4) {
                this.mUIHandler.sendEmptyMessage(9);
            }
            this.mState = 2;
        } else {
            int state = this.mLocalBluetoothAdapter.getState();
            if ((state == 11 || state == 12) && this.mState == 4) {
                this.mUIHandler.sendEmptyMessage(9);
            }
            if (state == 11) {
                this.mState = 4;
            } else if (state != 12) {
                this.mState = 4;
                showBluetoothDialog();
            } else {
                CachedBluetoothDevice pairedKeyboard = getPairedKeyboard();
                if (this.mState == 2 || this.mState == 4) {
                    if (pairedKeyboard != null) {
                        this.mState = 6;
                        pairedKeyboard.connect(false);
                        return;
                    }
                    this.mCachedDeviceManager.clearNonBondedDevices();
                }
                CachedBluetoothDevice discoveredKeyboard = getDiscoveredKeyboard();
                if (discoveredKeyboard != null) {
                    this.mState = 5;
                    discoveredKeyboard.startPairing();
                    return;
                }
                this.mState = 3;
                startScanning();
            }
        }
    }

    private void showBluetoothDialog() {
        if (!isUserSetupComplete()) {
            this.mLocalBluetoothAdapter.enable();
            return;
        }
        long uptimeMillis = SystemClock.uptimeMillis();
        long j = this.mBootCompletedTime + 10000;
        if (j < uptimeMillis) {
            this.mUIHandler.sendEmptyMessage(8);
        } else {
            this.mHandler.sendEmptyMessageAtTime(2, j);
        }
    }

    private void startScanning() {
        BluetoothLeScanner bluetoothLeScanner = this.mLocalBluetoothAdapter.getBluetoothLeScanner();
        ScanFilter build = new ScanFilter.Builder().setDeviceName(this.mKeyboardName).build();
        ScanSettings build2 = new ScanSettings.Builder().setCallbackType(1).setNumOfMatches(1).setScanMode(2).setReportDelay(0L).build();
        this.mScanCallback = new KeyboardScanCallback(this, null);
        bluetoothLeScanner.startScan(Arrays.asList(build), build2, this.mScanCallback);
        KeyboardHandler keyboardHandler = this.mHandler;
        int i = this.mScanAttempt + 1;
        this.mScanAttempt = i;
        this.mHandler.sendMessageDelayed(keyboardHandler.obtainMessage(10, i, 0), 30000L);
    }

    private static String stateToString(int i) {
        switch (i) {
            case -1:
                return "STATE_NOT_ENABLED";
            case 0:
            default:
                return "STATE_UNKNOWN (" + i + ")";
            case 1:
                return "STATE_WAITING_FOR_BOOT_COMPLETED";
            case 2:
                return "STATE_WAITING_FOR_TABLET_MODE_EXIT";
            case 3:
                return "STATE_WAITING_FOR_DEVICE_DISCOVERY";
            case 4:
                return "STATE_WAITING_FOR_BLUETOOTH";
            case 5:
                return "STATE_PAIRING";
            case 6:
                return "STATE_PAIRED";
            case 7:
                return "STATE_PAIRING_FAILED";
            case 8:
                return "STATE_USER_CANCELLED";
            case 9:
                return "STATE_DEVICE_NOT_FOUND";
        }
    }

    private void stopScanning() {
        if (this.mScanCallback != null) {
            BluetoothLeScanner bluetoothLeScanner = this.mLocalBluetoothAdapter.getBluetoothLeScanner();
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(this.mScanCallback);
            }
            this.mScanCallback = null;
        }
    }

    @Override // com.android.systemui.SystemUI
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyboardUI:");
        printWriter.println("  mEnabled=" + this.mEnabled);
        printWriter.println("  mBootCompleted=" + this.mEnabled);
        printWriter.println("  mBootCompletedTime=" + this.mBootCompletedTime);
        printWriter.println("  mKeyboardName=" + this.mKeyboardName);
        printWriter.println("  mInTabletMode=" + this.mInTabletMode);
        printWriter.println("  mState=" + stateToString(this.mState));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onBootCompleted() {
        this.mHandler.sendEmptyMessage(1);
    }

    public void onBootCompletedInternal() {
        this.mBootCompleted = true;
        this.mBootCompletedTime = SystemClock.uptimeMillis();
        if (this.mState == 1) {
            processKeyboardState();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
    }

    public void onTabletModeChanged(long j, boolean z) {
        int i = 1;
        if ((!z || this.mInTabletMode == 1) && (z || this.mInTabletMode == 0)) {
            return;
        }
        if (!z) {
            i = 0;
        }
        this.mInTabletMode = i;
        processKeyboardState();
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mContext = super.mContext;
        HandlerThread handlerThread = new HandlerThread("Keyboard", 10);
        handlerThread.start();
        this.mHandler = new KeyboardHandler(this, handlerThread.getLooper());
        this.mHandler.sendEmptyMessage(0);
    }
}
