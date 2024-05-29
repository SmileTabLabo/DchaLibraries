package com.android.systemui.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.widget.ImageView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.settings.ToggleSlider;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/settings/BrightnessController.class */
public class BrightnessController implements ToggleSlider.Listener {
    private boolean mAutomatic;
    private final boolean mAutomaticAvailable;
    private final Context mContext;
    private final ToggleSlider mControl;
    private boolean mExternalChange;
    private final ImageView mIcon;
    private boolean mListening;
    private final int mMaximumBacklight;
    private final int mMinimumBacklight;
    private final IPowerManager mPower;
    private final CurrentUserTracker mUserTracker;
    private ArrayList<BrightnessStateChangeCallback> mChangeCallbacks = new ArrayList<>();
    private final Handler mHandler = new Handler();
    private final BrightnessObserver mBrightnessObserver = new BrightnessObserver(this, this.mHandler);

    /* loaded from: a.zip:com/android/systemui/settings/BrightnessController$BrightnessObserver.class */
    private class BrightnessObserver extends ContentObserver {
        private final Uri BRIGHTNESS_ADJ_URI;
        private final Uri BRIGHTNESS_MODE_URI;
        private final Uri BRIGHTNESS_URI;
        final BrightnessController this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public BrightnessObserver(BrightnessController brightnessController, Handler handler) {
            super(handler);
            this.this$0 = brightnessController;
            this.BRIGHTNESS_MODE_URI = Settings.System.getUriFor("screen_brightness_mode");
            this.BRIGHTNESS_URI = Settings.System.getUriFor("screen_brightness");
            this.BRIGHTNESS_ADJ_URI = Settings.System.getUriFor("screen_auto_brightness_adj");
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            onChange(z, null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (z) {
                return;
            }
            try {
                this.this$0.mExternalChange = true;
                if (this.BRIGHTNESS_MODE_URI.equals(uri)) {
                    this.this$0.updateMode();
                    this.this$0.updateSlider();
                } else if (this.BRIGHTNESS_URI.equals(uri) && !this.this$0.mAutomatic) {
                    this.this$0.updateSlider();
                } else if (this.BRIGHTNESS_ADJ_URI.equals(uri) && this.this$0.mAutomatic) {
                    this.this$0.updateSlider();
                } else {
                    this.this$0.updateMode();
                    this.this$0.updateSlider();
                }
                for (BrightnessStateChangeCallback brightnessStateChangeCallback : this.this$0.mChangeCallbacks) {
                    brightnessStateChangeCallback.onBrightnessLevelChanged();
                }
            } finally {
                this.this$0.mExternalChange = false;
            }
        }

        public void startObserving() {
            ContentResolver contentResolver = this.this$0.mContext.getContentResolver();
            contentResolver.unregisterContentObserver(this);
            contentResolver.registerContentObserver(this.BRIGHTNESS_MODE_URI, false, this, -1);
            contentResolver.registerContentObserver(this.BRIGHTNESS_URI, false, this, -1);
            contentResolver.registerContentObserver(this.BRIGHTNESS_ADJ_URI, false, this, -1);
        }

        public void stopObserving() {
            this.this$0.mContext.getContentResolver().unregisterContentObserver(this);
        }
    }

    /* loaded from: a.zip:com/android/systemui/settings/BrightnessController$BrightnessStateChangeCallback.class */
    public interface BrightnessStateChangeCallback {
        void onBrightnessLevelChanged();
    }

    public BrightnessController(Context context, ImageView imageView, ToggleSlider toggleSlider) {
        this.mContext = context;
        this.mIcon = imageView;
        this.mControl = toggleSlider;
        this.mUserTracker = new CurrentUserTracker(this, this.mContext) { // from class: com.android.systemui.settings.BrightnessController.1
            final BrightnessController this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                this.this$0.updateMode();
                this.this$0.updateSlider();
            }
        };
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        this.mMinimumBacklight = powerManager.getMinimumScreenBrightnessSetting();
        this.mMaximumBacklight = powerManager.getMaximumScreenBrightnessSetting();
        this.mAutomaticAvailable = context.getResources().getBoolean(17956897);
        this.mPower = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
    }

    private void setBrightness(int i) {
        try {
            this.mPower.setTemporaryScreenBrightnessSettingOverride(i);
        } catch (RemoteException e) {
        }
    }

    private void setBrightnessAdj(float f) {
        try {
            this.mPower.setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(f);
        } catch (RemoteException e) {
        }
    }

    private void updateIcon(boolean z) {
        if (this.mIcon != null) {
            ImageView imageView = this.mIcon;
            if (z) {
            }
            imageView.setImageResource(2130837705);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateMode() {
        boolean z = false;
        if (!this.mAutomaticAvailable) {
            this.mControl.setChecked(false);
            updateIcon(false);
            return;
        }
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -2) != 0) {
            z = true;
        }
        this.mAutomatic = z;
        updateIcon(this.mAutomatic);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSlider() {
        if (this.mAutomatic) {
            float floatForUser = Settings.System.getFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", 0.0f, -2);
            this.mControl.setMax(2048);
            this.mControl.setValue((int) (((1.0f + floatForUser) * 2048.0f) / 2.0f));
            return;
        }
        int intForUser = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", this.mMaximumBacklight, -2);
        this.mControl.setMax(this.mMaximumBacklight - this.mMinimumBacklight);
        this.mControl.setValue(intForUser - this.mMinimumBacklight);
    }

    @Override // com.android.systemui.settings.ToggleSlider.Listener
    public void onChanged(ToggleSlider toggleSlider, boolean z, boolean z2, int i, boolean z3) {
        updateIcon(this.mAutomatic);
        if (this.mExternalChange) {
            return;
        }
        if (this.mAutomatic) {
            float f = (i / 1024.0f) - 1.0f;
            if (z3) {
                MetricsLogger.action(this.mContext, 219, i);
            }
            setBrightnessAdj(f);
            if (!z) {
                AsyncTask.execute(new Runnable(this, f) { // from class: com.android.systemui.settings.BrightnessController.3
                    final BrightnessController this$0;
                    final float val$adj;

                    {
                        this.this$0 = this;
                        this.val$adj = f;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        Settings.System.putFloatForUser(this.this$0.mContext.getContentResolver(), "screen_auto_brightness_adj", this.val$adj, -2);
                    }
                });
            }
        } else {
            int i2 = i + this.mMinimumBacklight;
            if (z3) {
                MetricsLogger.action(this.mContext, 218, i2);
            }
            setBrightness(i2);
            if (!z) {
                AsyncTask.execute(new Runnable(this, i2) { // from class: com.android.systemui.settings.BrightnessController.2
                    final BrightnessController this$0;
                    final int val$val;

                    {
                        this.this$0 = this;
                        this.val$val = i2;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        Settings.System.putIntForUser(this.this$0.mContext.getContentResolver(), "screen_brightness", this.val$val, -2);
                    }
                });
            }
        }
        for (BrightnessStateChangeCallback brightnessStateChangeCallback : this.mChangeCallbacks) {
            brightnessStateChangeCallback.onBrightnessLevelChanged();
        }
    }

    @Override // com.android.systemui.settings.ToggleSlider.Listener
    public void onInit(ToggleSlider toggleSlider) {
    }

    public void registerCallbacks() {
        if (this.mListening) {
            return;
        }
        this.mBrightnessObserver.startObserving();
        this.mUserTracker.startTracking();
        updateMode();
        updateSlider();
        this.mControl.setOnChangedListener(this);
        this.mListening = true;
    }

    public void unregisterCallbacks() {
        if (this.mListening) {
            this.mBrightnessObserver.stopObserving();
            this.mUserTracker.stopTracking();
            this.mControl.setOnChangedListener(null);
            this.mListening = false;
        }
    }
}
