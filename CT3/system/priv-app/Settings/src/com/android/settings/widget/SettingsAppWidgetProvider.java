package com.android.settings.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;
import com.android.settings.R;
import com.android.settings.bluetooth.Utils;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
/* loaded from: classes.dex */
public class SettingsAppWidgetProvider extends AppWidgetProvider {
    private static SettingsObserver sSettingsObserver;
    static final ComponentName THIS_APPWIDGET = new ComponentName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
    private static LocalBluetoothAdapter sLocalBluetoothAdapter = null;
    private static final int[] IND_DRAWABLE_OFF = {R.drawable.appwidget_settings_ind_off_l_holo, R.drawable.appwidget_settings_ind_off_c_holo, R.drawable.appwidget_settings_ind_off_r_holo};
    private static final int[] IND_DRAWABLE_MID = {R.drawable.appwidget_settings_ind_mid_l_holo, R.drawable.appwidget_settings_ind_mid_c_holo, R.drawable.appwidget_settings_ind_mid_r_holo};
    private static final int[] IND_DRAWABLE_ON = {R.drawable.appwidget_settings_ind_on_l_holo, R.drawable.appwidget_settings_ind_on_c_holo, R.drawable.appwidget_settings_ind_on_r_holo};
    private static final StateTracker sWifiState = new WifiStateTracker(null);
    private static final StateTracker sBluetoothState = new BluetoothStateTracker(null);
    private static final StateTracker sLocationState = new LocationStateTracker(null);
    private static final StateTracker sSyncState = new SyncStateTracker(null);

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static abstract class StateTracker {
        private Boolean mActualState;
        private boolean mDeferredStateChangeRequestNeeded;
        private boolean mInTransition;
        private Boolean mIntendedState;

        /* synthetic */ StateTracker(StateTracker stateTracker) {
            this();
        }

        public abstract int getActualState(Context context);

        public abstract int getButtonDescription();

        public abstract int getButtonId();

        public abstract int getButtonImageId(boolean z);

        public abstract int getContainerId();

        public abstract int getIndicatorId();

        public abstract void onActualStateChange(Context context, Intent intent);

        protected abstract void requestStateChange(Context context, boolean z);

        private StateTracker() {
            this.mInTransition = false;
            this.mActualState = null;
            this.mIntendedState = null;
            this.mDeferredStateChangeRequestNeeded = false;
        }

        public final void toggleState(Context context) {
            int currentState = getTriState(context);
            boolean newState = false;
            switch (currentState) {
                case 0:
                    newState = true;
                    break;
                case 1:
                    newState = false;
                    break;
                case 5:
                    if (this.mIntendedState != null) {
                        if (!this.mIntendedState.booleanValue()) {
                            newState = true;
                            break;
                        } else {
                            newState = false;
                            break;
                        }
                    }
                    break;
            }
            this.mIntendedState = Boolean.valueOf(newState);
            if (this.mInTransition) {
                this.mDeferredStateChangeRequestNeeded = true;
                return;
            }
            this.mInTransition = true;
            requestStateChange(context, newState);
        }

        public int getPosition() {
            return 1;
        }

        public final void setImageViewResources(Context context, RemoteViews views) {
            int containerId = getContainerId();
            int buttonId = getButtonId();
            int indicatorId = getIndicatorId();
            int pos = getPosition();
            switch (getTriState(context)) {
                case 0:
                    views.setContentDescription(containerId, getContentDescription(context, R.string.gadget_state_off));
                    views.setImageViewResource(buttonId, getButtonImageId(false));
                    views.setImageViewResource(indicatorId, SettingsAppWidgetProvider.IND_DRAWABLE_OFF[pos]);
                    return;
                case 1:
                    views.setContentDescription(containerId, getContentDescription(context, R.string.gadget_state_on));
                    views.setImageViewResource(buttonId, getButtonImageId(true));
                    views.setImageViewResource(indicatorId, SettingsAppWidgetProvider.IND_DRAWABLE_ON[pos]);
                    return;
                case 2:
                case 3:
                case 4:
                default:
                    return;
                case 5:
                    if (isTurningOn()) {
                        views.setContentDescription(containerId, getContentDescription(context, R.string.gadget_state_turning_on));
                        views.setImageViewResource(buttonId, getButtonImageId(true));
                        views.setImageViewResource(indicatorId, SettingsAppWidgetProvider.IND_DRAWABLE_MID[pos]);
                        return;
                    }
                    views.setContentDescription(containerId, getContentDescription(context, R.string.gadget_state_turning_off));
                    views.setImageViewResource(buttonId, getButtonImageId(false));
                    views.setImageViewResource(indicatorId, SettingsAppWidgetProvider.IND_DRAWABLE_OFF[pos]);
                    return;
            }
        }

        private final String getContentDescription(Context context, int stateResId) {
            String gadget = context.getString(getButtonDescription());
            String state = context.getString(stateResId);
            return context.getString(R.string.gadget_state_template, gadget, state);
        }

        protected final void setCurrentState(Context context, int newState) {
            boolean wasInTransition = this.mInTransition;
            switch (newState) {
                case 0:
                    this.mInTransition = false;
                    this.mActualState = false;
                    break;
                case 1:
                    this.mInTransition = false;
                    this.mActualState = true;
                    break;
                case 2:
                    this.mInTransition = true;
                    this.mActualState = false;
                    break;
                case 3:
                    this.mInTransition = true;
                    this.mActualState = true;
                    break;
            }
            if (!wasInTransition || this.mInTransition || !this.mDeferredStateChangeRequestNeeded) {
                return;
            }
            Log.v("SettingsAppWidgetProvider", "processing deferred state change");
            if (this.mActualState != null && this.mIntendedState != null && this.mIntendedState.equals(this.mActualState)) {
                Log.v("SettingsAppWidgetProvider", "... but intended state matches, so no changes.");
            } else if (this.mIntendedState != null) {
                this.mInTransition = true;
                requestStateChange(context, this.mIntendedState.booleanValue());
            }
            this.mDeferredStateChangeRequestNeeded = false;
        }

        public final boolean isTurningOn() {
            if (this.mIntendedState != null) {
                return this.mIntendedState.booleanValue();
            }
            return false;
        }

        public final int getTriState(Context context) {
            if (this.mInTransition) {
                return 5;
            }
            switch (getActualState(context)) {
                case 0:
                    return 0;
                case 1:
                    return 1;
                default:
                    return 5;
            }
        }
    }

    /* loaded from: classes.dex */
    private static final class WifiStateTracker extends StateTracker {
        /* synthetic */ WifiStateTracker(WifiStateTracker wifiStateTracker) {
            this();
        }

        private WifiStateTracker() {
            super(null);
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getContainerId() {
            return R.id.btn_wifi;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getButtonId() {
            return R.id.img_wifi;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getIndicatorId() {
            return R.id.ind_wifi;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getButtonDescription() {
            return R.string.gadget_wifi;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getButtonImageId(boolean on) {
            return on ? R.drawable.ic_appwidget_settings_wifi_on_holo : R.drawable.ic_appwidget_settings_wifi_off_holo;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getPosition() {
            return 0;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getActualState(Context context) {
            WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
            if (wifiManager != null) {
                return wifiStateToFiveState(wifiManager.getWifiState());
            }
            return 4;
        }

        /* JADX WARN: Type inference failed for: r1v1, types: [com.android.settings.widget.SettingsAppWidgetProvider$WifiStateTracker$1] */
        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        protected void requestStateChange(Context context, final boolean desiredState) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
            if (wifiManager == null) {
                Log.d("SettingsAppWidgetProvider", "No wifiManager.");
            } else {
                new AsyncTask<Void, Void, Void>() { // from class: com.android.settings.widget.SettingsAppWidgetProvider.WifiStateTracker.1
                    /* JADX INFO: Access modifiers changed from: protected */
                    @Override // android.os.AsyncTask
                    public Void doInBackground(Void... args) {
                        int wifiApState = wifiManager.getWifiApState();
                        if (desiredState && (wifiApState == 12 || wifiApState == 13)) {
                            wifiManager.setWifiApEnabled(null, false);
                        }
                        wifiManager.setWifiEnabled(desiredState);
                        return null;
                    }
                }.execute(new Void[0]);
            }
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public void onActualStateChange(Context context, Intent intent) {
            if (!"android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) {
                return;
            }
            int wifiState = intent.getIntExtra("wifi_state", -1);
            setCurrentState(context, wifiStateToFiveState(wifiState));
        }

        private static int wifiStateToFiveState(int wifiState) {
            switch (wifiState) {
                case 0:
                    return 3;
                case 1:
                    return 0;
                case 2:
                    return 2;
                case 3:
                    return 1;
                default:
                    return 4;
            }
        }
    }

    /* loaded from: classes.dex */
    private static final class BluetoothStateTracker extends StateTracker {
        /* synthetic */ BluetoothStateTracker(BluetoothStateTracker bluetoothStateTracker) {
            this();
        }

        private BluetoothStateTracker() {
            super(null);
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getContainerId() {
            return R.id.btn_bluetooth;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getButtonId() {
            return R.id.img_bluetooth;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getIndicatorId() {
            return R.id.ind_bluetooth;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getButtonDescription() {
            return R.string.gadget_bluetooth;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getButtonImageId(boolean on) {
            return on ? R.drawable.ic_appwidget_settings_bluetooth_on_holo : R.drawable.ic_appwidget_settings_bluetooth_off_holo;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getActualState(Context context) {
            if (SettingsAppWidgetProvider.sLocalBluetoothAdapter == null) {
                LocalBluetoothManager manager = Utils.getLocalBtManager(context);
                if (manager == null) {
                    return 4;
                }
                LocalBluetoothAdapter unused = SettingsAppWidgetProvider.sLocalBluetoothAdapter = manager.getBluetoothAdapter();
                if (SettingsAppWidgetProvider.sLocalBluetoothAdapter == null) {
                    return 4;
                }
            }
            return bluetoothStateToFiveState(SettingsAppWidgetProvider.sLocalBluetoothAdapter.getBluetoothState());
        }

        /* JADX WARN: Type inference failed for: r0v1, types: [com.android.settings.widget.SettingsAppWidgetProvider$BluetoothStateTracker$1] */
        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        protected void requestStateChange(Context context, final boolean desiredState) {
            if (SettingsAppWidgetProvider.sLocalBluetoothAdapter == null) {
                Log.d("SettingsAppWidgetProvider", "No LocalBluetoothManager");
            } else {
                new AsyncTask<Void, Void, Void>() { // from class: com.android.settings.widget.SettingsAppWidgetProvider.BluetoothStateTracker.1
                    /* JADX INFO: Access modifiers changed from: protected */
                    @Override // android.os.AsyncTask
                    public Void doInBackground(Void... args) {
                        SettingsAppWidgetProvider.sLocalBluetoothAdapter.setBluetoothEnabled(desiredState);
                        return null;
                    }
                }.execute(new Void[0]);
            }
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public void onActualStateChange(Context context, Intent intent) {
            if (!"android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                return;
            }
            int bluetoothState = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
            setCurrentState(context, bluetoothStateToFiveState(bluetoothState));
        }

        private static int bluetoothStateToFiveState(int bluetoothState) {
            switch (bluetoothState) {
                case 10:
                    return 0;
                case 11:
                    return 2;
                case 12:
                    return 1;
                case 13:
                    return 3;
                default:
                    return 4;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class LocationStateTracker extends StateTracker {
        private int mCurrentLocationMode;

        /* synthetic */ LocationStateTracker(LocationStateTracker locationStateTracker) {
            this();
        }

        private LocationStateTracker() {
            super(null);
            this.mCurrentLocationMode = 0;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getContainerId() {
            return R.id.btn_location;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getButtonId() {
            return R.id.img_location;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getIndicatorId() {
            return R.id.ind_location;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getButtonDescription() {
            return R.string.gadget_location;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getButtonImageId(boolean on) {
            if (on) {
                switch (this.mCurrentLocationMode) {
                    case 1:
                    case 3:
                        return R.drawable.ic_appwidget_settings_location_on_holo;
                    case 2:
                    default:
                        return R.drawable.ic_appwidget_settings_location_saving_holo;
                }
            }
            return R.drawable.ic_appwidget_settings_location_off_holo;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getActualState(Context context) {
            ContentResolver resolver = context.getContentResolver();
            this.mCurrentLocationMode = Settings.Secure.getInt(resolver, "location_mode", 0);
            return this.mCurrentLocationMode == 0 ? 0 : 1;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public void onActualStateChange(Context context, Intent unused) {
            setCurrentState(context, getActualState(context));
        }

        /* JADX WARN: Type inference failed for: r1v0, types: [com.android.settings.widget.SettingsAppWidgetProvider$LocationStateTracker$1] */
        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public void requestStateChange(final Context context, boolean desiredState) {
            final ContentResolver resolver = context.getContentResolver();
            new AsyncTask<Void, Void, Boolean>() { // from class: com.android.settings.widget.SettingsAppWidgetProvider.LocationStateTracker.1
                /* JADX INFO: Access modifiers changed from: protected */
                @Override // android.os.AsyncTask
                public Boolean doInBackground(Void... args) {
                    UserManager um = (UserManager) context.getSystemService("user");
                    if (um.hasUserRestriction("no_share_location")) {
                        return Boolean.valueOf(LocationStateTracker.this.getActualState(context) == 1);
                    }
                    int currentMode = Settings.Secure.getInt(resolver, "location_mode", 0);
                    int mode = 3;
                    switch (currentMode) {
                        case 0:
                            mode = -1;
                            break;
                        case 1:
                            mode = 0;
                            break;
                        case 2:
                            mode = 3;
                            break;
                        case 3:
                            mode = 2;
                            break;
                    }
                    Settings.Secure.putInt(resolver, "location_mode", mode);
                    return Boolean.valueOf(mode != 0);
                }

                /* JADX INFO: Access modifiers changed from: protected */
                @Override // android.os.AsyncTask
                public void onPostExecute(Boolean result) {
                    LocationStateTracker.this.setCurrentState(context, result.booleanValue() ? 1 : 0);
                    SettingsAppWidgetProvider.updateWidget(context);
                }
            }.execute(new Void[0]);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class SyncStateTracker extends StateTracker {
        /* synthetic */ SyncStateTracker(SyncStateTracker syncStateTracker) {
            this();
        }

        private SyncStateTracker() {
            super(null);
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getContainerId() {
            return R.id.btn_sync;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getButtonId() {
            return R.id.img_sync;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getIndicatorId() {
            return R.id.ind_sync;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getButtonDescription() {
            return R.string.gadget_sync;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getButtonImageId(boolean on) {
            return on ? R.drawable.ic_appwidget_settings_sync_on_holo : R.drawable.ic_appwidget_settings_sync_off_holo;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public int getActualState(Context context) {
            boolean on = ContentResolver.getMasterSyncAutomatically();
            return on ? 1 : 0;
        }

        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public void onActualStateChange(Context context, Intent unused) {
            setCurrentState(context, getActualState(context));
        }

        /* JADX WARN: Type inference failed for: r2v1, types: [com.android.settings.widget.SettingsAppWidgetProvider$SyncStateTracker$1] */
        @Override // com.android.settings.widget.SettingsAppWidgetProvider.StateTracker
        public void requestStateChange(final Context context, final boolean desiredState) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            final boolean sync = ContentResolver.getMasterSyncAutomatically();
            new AsyncTask<Void, Void, Boolean>() { // from class: com.android.settings.widget.SettingsAppWidgetProvider.SyncStateTracker.1
                /* JADX INFO: Access modifiers changed from: protected */
                @Override // android.os.AsyncTask
                public Boolean doInBackground(Void... args) {
                    if (desiredState) {
                        if (!sync) {
                            ContentResolver.setMasterSyncAutomatically(true);
                        }
                        return true;
                    }
                    if (sync) {
                        ContentResolver.setMasterSyncAutomatically(false);
                    }
                    return false;
                }

                /* JADX INFO: Access modifiers changed from: protected */
                @Override // android.os.AsyncTask
                public void onPostExecute(Boolean result) {
                    SyncStateTracker.this.setCurrentState(context, result.booleanValue() ? 1 : 0);
                    SettingsAppWidgetProvider.updateWidget(context);
                }
            }.execute(new Void[0]);
        }
    }

    private static void checkObserver(Context context) {
        if (sSettingsObserver != null) {
            return;
        }
        sSettingsObserver = new SettingsObserver(new Handler(), context.getApplicationContext());
        sSettingsObserver.startObserving();
    }

    @Override // android.appwidget.AppWidgetProvider
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews view = buildUpdate(context);
        for (int i : appWidgetIds) {
            appWidgetManager.updateAppWidget(i, view);
        }
    }

    @Override // android.appwidget.AppWidgetProvider
    public void onEnabled(Context context) {
        checkObserver(context);
    }

    @Override // android.appwidget.AppWidgetProvider
    public void onDisabled(Context context) {
        if (sSettingsObserver == null) {
            return;
        }
        sSettingsObserver.stopObserving();
        sSettingsObserver = null;
    }

    static RemoteViews buildUpdate(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), (int) R.layout.widget);
        views.setOnClickPendingIntent(R.id.btn_wifi, getLaunchPendingIntent(context, 0));
        views.setOnClickPendingIntent(R.id.btn_brightness, getLaunchPendingIntent(context, 1));
        views.setOnClickPendingIntent(R.id.btn_sync, getLaunchPendingIntent(context, 2));
        views.setOnClickPendingIntent(R.id.btn_location, getLaunchPendingIntent(context, 3));
        views.setOnClickPendingIntent(R.id.btn_bluetooth, getLaunchPendingIntent(context, 4));
        updateButtons(views, context);
        return views;
    }

    public static void updateWidget(Context context) {
        RemoteViews views = buildUpdate(context);
        AppWidgetManager gm = AppWidgetManager.getInstance(context);
        gm.updateAppWidget(THIS_APPWIDGET, views);
        checkObserver(context);
    }

    private static void updateButtons(RemoteViews views, Context context) {
        sWifiState.setImageViewResources(context, views);
        sBluetoothState.setImageViewResources(context, views);
        sLocationState.setImageViewResources(context, views);
        sSyncState.setImageViewResources(context, views);
        if (getBrightnessMode(context)) {
            views.setContentDescription(R.id.btn_brightness, context.getString(R.string.gadget_brightness_template, context.getString(R.string.gadget_brightness_state_auto)));
            views.setImageViewResource(R.id.img_brightness, R.drawable.ic_appwidget_settings_brightness_auto_holo);
            views.setImageViewResource(R.id.ind_brightness, R.drawable.appwidget_settings_ind_on_r_holo);
            return;
        }
        int brightness = getBrightness(context);
        PowerManager pm = (PowerManager) context.getSystemService("power");
        int full = (int) (pm.getMaximumScreenBrightnessSetting() * 0.8f);
        int half = (int) (pm.getMaximumScreenBrightnessSetting() * 0.3f);
        if (brightness > full) {
            views.setContentDescription(R.id.btn_brightness, context.getString(R.string.gadget_brightness_template, context.getString(R.string.gadget_brightness_state_full)));
            views.setImageViewResource(R.id.img_brightness, R.drawable.ic_appwidget_settings_brightness_full_holo);
        } else if (brightness > half) {
            views.setContentDescription(R.id.btn_brightness, context.getString(R.string.gadget_brightness_template, context.getString(R.string.gadget_brightness_state_half)));
            views.setImageViewResource(R.id.img_brightness, R.drawable.ic_appwidget_settings_brightness_half_holo);
        } else {
            views.setContentDescription(R.id.btn_brightness, context.getString(R.string.gadget_brightness_template, context.getString(R.string.gadget_brightness_state_off)));
            views.setImageViewResource(R.id.img_brightness, R.drawable.ic_appwidget_settings_brightness_off_holo);
        }
        if (brightness > half) {
            views.setImageViewResource(R.id.ind_brightness, R.drawable.appwidget_settings_ind_on_r_holo);
        } else {
            views.setImageViewResource(R.id.ind_brightness, R.drawable.appwidget_settings_ind_off_r_holo);
        }
    }

    private static PendingIntent getLaunchPendingIntent(Context context, int buttonId) {
        Intent launchIntent = new Intent();
        launchIntent.setClass(context, SettingsAppWidgetProvider.class);
        launchIntent.addCategory("android.intent.category.ALTERNATIVE");
        launchIntent.setData(Uri.parse("custom:" + buttonId));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, launchIntent, 0);
        return pi;
    }

    @Override // android.appwidget.AppWidgetProvider, android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
            sWifiState.onActualStateChange(context, intent);
        } else if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
            sBluetoothState.onActualStateChange(context, intent);
        } else if ("android.location.MODE_CHANGED".equals(action)) {
            sLocationState.onActualStateChange(context, intent);
        } else if (ContentResolver.ACTION_SYNC_CONN_STATUS_CHANGED.equals(action)) {
            sSyncState.onActualStateChange(context, intent);
        } else if (intent.hasCategory("android.intent.category.ALTERNATIVE")) {
            Uri data = intent.getData();
            int buttonId = Integer.parseInt(data.getSchemeSpecificPart());
            if (buttonId == 0) {
                sWifiState.toggleState(context);
            } else if (buttonId == 1) {
                toggleBrightness(context);
            } else if (buttonId == 2) {
                sSyncState.toggleState(context);
            } else if (buttonId == 3) {
                sLocationState.toggleState(context);
            } else if (buttonId == 4) {
                sBluetoothState.toggleState(context);
            }
        } else {
            return;
        }
        updateWidget(context);
    }

    private static int getBrightness(Context context) {
        try {
            int brightness = Settings.System.getInt(context.getContentResolver(), "screen_brightness");
            return brightness;
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean getBrightnessMode(Context context) {
        try {
            int brightnessMode = Settings.System.getInt(context.getContentResolver(), "screen_brightness_mode");
            return brightnessMode == 1;
        } catch (Exception e) {
            Log.d("SettingsAppWidgetProvider", "getBrightnessMode: " + e);
            return false;
        }
    }

    private void toggleBrightness(Context context) {
        int brightness;
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
            if (power == null) {
                return;
            }
            PowerManager pm = (PowerManager) context.getSystemService("power");
            ContentResolver cr = context.getContentResolver();
            int brightness2 = Settings.System.getInt(cr, "screen_brightness");
            int brightnessMode = 0;
            if (context.getResources().getBoolean(17956897)) {
                brightnessMode = Settings.System.getInt(cr, "screen_brightness_mode");
            }
            if (brightnessMode == 1) {
                brightness = pm.getMinimumScreenBrightnessSetting();
                brightnessMode = 0;
            } else if (brightness2 < pm.getDefaultScreenBrightnessSetting()) {
                brightness = pm.getDefaultScreenBrightnessSetting();
            } else if (brightness2 < pm.getMaximumScreenBrightnessSetting()) {
                brightness = pm.getMaximumScreenBrightnessSetting();
            } else {
                brightnessMode = 1;
                brightness = pm.getMinimumScreenBrightnessSetting();
            }
            if (context.getResources().getBoolean(17956897)) {
                Settings.System.putInt(context.getContentResolver(), "screen_brightness_mode", brightnessMode);
            } else {
                brightnessMode = 0;
            }
            if (brightnessMode != 0) {
                return;
            }
            power.setTemporaryScreenBrightnessSettingOverride(brightness);
            Settings.System.putInt(cr, "screen_brightness", brightness);
        } catch (RemoteException e) {
            Log.d("SettingsAppWidgetProvider", "toggleBrightness: " + e);
        } catch (Settings.SettingNotFoundException e2) {
            Log.d("SettingsAppWidgetProvider", "toggleBrightness: " + e2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SettingsObserver extends ContentObserver {
        private Context mContext;

        SettingsObserver(Handler handler, Context context) {
            super(handler);
            this.mContext = context;
        }

        void startObserving() {
            ContentResolver resolver = this.mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor("screen_brightness"), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor("screen_brightness_mode"), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor("screen_auto_brightness_adj"), false, this);
        }

        void stopObserving() {
            this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            SettingsAppWidgetProvider.updateWidget(this.mContext);
        }
    }
}
