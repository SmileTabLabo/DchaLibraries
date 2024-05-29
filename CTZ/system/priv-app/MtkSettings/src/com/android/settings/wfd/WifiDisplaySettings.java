package com.android.settings.wfd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplay;
import android.hardware.display.WifiDisplayStatus;
import android.media.MediaRouter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Slog;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.app.MediaRouteDialogPresenter;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.mediatek.settings.FeatureOption;
import com.mediatek.settings.wfd.WfdChangeResolution;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public final class WifiDisplaySettings extends SettingsPreferenceFragment implements Indexable {
    private boolean mAutoGO;
    private PreferenceGroup mCertCategory;
    private DisplayManager mDisplayManager;
    private TextView mEmptyView;
    private boolean mListen;
    private int mListenChannel;
    private int mOperatingChannel;
    private int mPendingChanges;
    private MediaRouter mRouter;
    private boolean mStarted;
    private WfdChangeResolution mWfdChangeResolution;
    private boolean mWifiDisplayCertificationOn;
    private boolean mWifiDisplayOnSetting;
    private WifiDisplayStatus mWifiDisplayStatus;
    private WifiP2pManager.Channel mWifiP2pChannel;
    private WifiP2pManager mWifiP2pManager;
    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryLoader.SummaryProviderFactory() { // from class: com.android.settings.wfd.-$$Lambda$WifiDisplaySettings$FSGRkDMrB620EgLXH7J2ShDkw60
        @Override // com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory
        public final SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return WifiDisplaySettings.lambda$static$0(activity, summaryLoader);
        }
    };
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.android.settings.wfd.WifiDisplaySettings.17
        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean z) {
            ArrayList arrayList = new ArrayList();
            SearchIndexableResource searchIndexableResource = new SearchIndexableResource(context);
            searchIndexableResource.xmlResId = R.xml.wifi_display_settings;
            arrayList.add(searchIndexableResource);
            return arrayList;
        }
    };
    private int mWpsConfig = 4;
    private final Runnable mUpdateRunnable = new Runnable() { // from class: com.android.settings.wfd.WifiDisplaySettings.13
        @Override // java.lang.Runnable
        public void run() {
            int i = WifiDisplaySettings.this.mPendingChanges;
            WifiDisplaySettings.this.mPendingChanges = 0;
            WifiDisplaySettings.this.update(i);
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.settings.wfd.WifiDisplaySettings.14
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED")) {
                WifiDisplaySettings.this.scheduleUpdate(4);
            }
        }
    };
    private final ContentObserver mSettingsObserver = new ContentObserver(new Handler()) { // from class: com.android.settings.wfd.WifiDisplaySettings.15
        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            WifiDisplaySettings.this.scheduleUpdate(1);
        }
    };
    private final MediaRouter.Callback mRouterCallback = new MediaRouter.SimpleCallback() { // from class: com.android.settings.wfd.WifiDisplaySettings.16
        @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
        public void onRouteAdded(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            WifiDisplaySettings.this.scheduleUpdate(2);
        }

        @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
        public void onRouteChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            WifiDisplaySettings.this.scheduleUpdate(2);
        }

        @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
        public void onRouteRemoved(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            WifiDisplaySettings.this.scheduleUpdate(2);
        }

        @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
        public void onRouteSelected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
            WifiDisplaySettings.this.scheduleUpdate(2);
        }

        @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
        public void onRouteUnselected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
            WifiDisplaySettings.this.scheduleUpdate(2);
        }
    };
    private final Handler mHandler = new Handler();

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 102;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (FeatureOption.MTK_WFD_SUPPORT) {
            this.mWfdChangeResolution = new WfdChangeResolution(getActivity());
        }
        Activity activity = getActivity();
        this.mRouter = (MediaRouter) activity.getSystemService("media_router");
        this.mDisplayManager = (DisplayManager) activity.getSystemService("display");
        this.mWifiP2pManager = (WifiP2pManager) activity.getSystemService("wifip2p");
        this.mWifiP2pChannel = this.mWifiP2pManager.initialize(activity, Looper.getMainLooper(), null);
        addPreferencesFromResource(R.xml.wifi_display_settings);
        setHasOptionsMenu(true);
    }

    @Override // com.android.settings.support.actionbar.HelpResourceProvider
    public int getHelpResource() {
        return R.string.help_url_remote_display;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        this.mEmptyView = (TextView) getView().findViewById(16908292);
        this.mEmptyView.setText(R.string.wifi_display_no_devices_found);
        setEmptyView(this.mEmptyView);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStart() {
        super.onStart();
        this.mStarted = true;
        Activity activity = getActivity();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED");
        activity.registerReceiver(this.mReceiver, intentFilter);
        getContentResolver().registerContentObserver(Settings.Global.getUriFor("wifi_display_on"), false, this.mSettingsObserver);
        getContentResolver().registerContentObserver(Settings.Global.getUriFor("wifi_display_certification_on"), false, this.mSettingsObserver);
        getContentResolver().registerContentObserver(Settings.Global.getUriFor("wifi_display_wps_config"), false, this.mSettingsObserver);
        this.mRouter.addCallback(4, this.mRouterCallback, 1);
        update(-1);
        if (this.mWfdChangeResolution != null) {
            this.mWfdChangeResolution.onStart();
        }
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStop() {
        super.onStop();
        this.mStarted = false;
        if (this.mWfdChangeResolution != null) {
            this.mWfdChangeResolution.onStop();
        }
        getActivity().unregisterReceiver(this.mReceiver);
        getContentResolver().unregisterContentObserver(this.mSettingsObserver);
        this.mRouter.removeCallback(this.mRouterCallback);
        unscheduleUpdate();
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        if (this.mWifiDisplayStatus != null && this.mWifiDisplayStatus.getFeatureState() != 0) {
            MenuItem add = menu.add(0, 1, 0, R.string.wifi_display_enable_menu_item);
            add.setCheckable(true);
            add.setChecked(this.mWifiDisplayOnSetting);
        }
        if (this.mWfdChangeResolution != null) {
            this.mWfdChangeResolution.onCreateOptionMenu(menu, this.mWifiDisplayStatus);
        }
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 1) {
            this.mWifiDisplayOnSetting = !menuItem.isChecked();
            menuItem.setChecked(this.mWifiDisplayOnSetting);
            Settings.Global.putInt(getContentResolver(), "wifi_display_on", this.mWifiDisplayOnSetting ? 1 : 0);
            return true;
        } else if (this.mWfdChangeResolution == null || !this.mWfdChangeResolution.onOptionMenuSelected(menuItem, getFragmentManager())) {
            return super.onOptionsItemSelected(menuItem);
        } else {
            return true;
        }
    }

    public static boolean isAvailable(Context context) {
        return (context.getSystemService("display") == null || !context.getPackageManager().hasSystemFeature("android.hardware.wifi.direct") || context.getSystemService("wifip2p") == null) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scheduleUpdate(int i) {
        if (this.mStarted) {
            if (this.mPendingChanges == 0) {
                this.mHandler.post(this.mUpdateRunnable);
            }
            this.mPendingChanges = i | this.mPendingChanges;
        }
    }

    private void unscheduleUpdate() {
        if (this.mPendingChanges != 0) {
            this.mPendingChanges = 0;
            this.mHandler.removeCallbacks(this.mUpdateRunnable);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void update(int i) {
        boolean z;
        WifiDisplay[] displays;
        Preference preference;
        if ((i & 1) != 0) {
            this.mWifiDisplayOnSetting = Settings.Global.getInt(getContentResolver(), "wifi_display_on", 0) != 0;
            this.mWifiDisplayCertificationOn = Settings.Global.getInt(getContentResolver(), "wifi_display_certification_on", 0) != 0;
            this.mWpsConfig = Settings.Global.getInt(getContentResolver(), "wifi_display_wps_config", 4);
            z = true;
        } else {
            z = false;
        }
        int i2 = i & 4;
        if (i2 != 0) {
            this.mWifiDisplayStatus = this.mDisplayManager.getWifiDisplayStatus();
            z = true;
        }
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.removeAll();
        PreferenceCategory preferenceCategory = null;
        if (this.mWfdChangeResolution != null) {
            if (this.mWfdChangeResolution.addAdditionalPreference(preferenceScreen, this.mWifiDisplayStatus != null && this.mWifiDisplayStatus.getFeatureState() == 3) && (preference = preferenceScreen.getPreference(preferenceScreen.getPreferenceCount() - 1)) != null && (preference instanceof PreferenceCategory)) {
                preferenceCategory = (PreferenceCategory) preference;
            }
            if (i2 != 0) {
                this.mWfdChangeResolution.handleWfdStatusChanged(this.mWifiDisplayStatus);
            }
        }
        int routeCount = this.mRouter.getRouteCount();
        for (int i3 = 0; i3 < routeCount; i3++) {
            MediaRouter.RouteInfo routeAt = this.mRouter.getRouteAt(i3);
            if (routeAt.matchesTypes(4)) {
                if (preferenceCategory == null) {
                    preferenceScreen.addPreference(createRoutePreference(routeAt));
                } else {
                    preferenceCategory.addPreference(createRoutePreference(routeAt));
                }
            }
        }
        if (this.mWifiDisplayStatus != null && this.mWifiDisplayStatus.getFeatureState() == 3) {
            for (WifiDisplay wifiDisplay : this.mWifiDisplayStatus.getDisplays()) {
                if (!wifiDisplay.isRemembered() && wifiDisplay.isAvailable() && !wifiDisplay.equals(this.mWifiDisplayStatus.getActiveDisplay())) {
                    if (preferenceCategory == null) {
                        preferenceScreen.addPreference(new UnpairedWifiDisplayPreference(getPrefContext(), wifiDisplay));
                    } else {
                        preferenceCategory.addPreference(new UnpairedWifiDisplayPreference(getPrefContext(), wifiDisplay));
                    }
                }
            }
            if (this.mWifiDisplayCertificationOn) {
                buildCertificationMenu(preferenceScreen);
            }
        }
        if (z) {
            getActivity().invalidateOptionsMenu();
        }
    }

    private RoutePreference createRoutePreference(MediaRouter.RouteInfo routeInfo) {
        WifiDisplay findWifiDisplay = findWifiDisplay(routeInfo.getDeviceAddress());
        if (findWifiDisplay != null) {
            return new WifiDisplayRoutePreference(getPrefContext(), routeInfo, findWifiDisplay);
        }
        return new RoutePreference(getPrefContext(), routeInfo);
    }

    private WifiDisplay findWifiDisplay(String str) {
        WifiDisplay[] displays;
        if (this.mWifiDisplayStatus != null && str != null) {
            for (WifiDisplay wifiDisplay : this.mWifiDisplayStatus.getDisplays()) {
                if (wifiDisplay.getDeviceAddress().equals(str)) {
                    return wifiDisplay;
                }
            }
            return null;
        }
        return null;
    }

    private void buildCertificationMenu(PreferenceScreen preferenceScreen) {
        if (this.mCertCategory == null) {
            this.mCertCategory = new PreferenceCategory(getPrefContext());
            this.mCertCategory.setTitle(R.string.wifi_display_certification_heading);
            this.mCertCategory.setOrder(1);
        } else {
            this.mCertCategory.removeAll();
        }
        preferenceScreen.addPreference(this.mCertCategory);
        if (!this.mWifiDisplayStatus.getSessionInfo().getGroupId().isEmpty()) {
            Preference preference = new Preference(getPrefContext());
            preference.setTitle(R.string.wifi_display_session_info);
            preference.setSummary(this.mWifiDisplayStatus.getSessionInfo().toString());
            this.mCertCategory.addPreference(preference);
            if (this.mWifiDisplayStatus.getSessionInfo().getSessionId() != 0) {
                this.mCertCategory.addPreference(new Preference(getPrefContext()) { // from class: com.android.settings.wfd.WifiDisplaySettings.1
                    @Override // android.support.v7.preference.Preference
                    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
                        super.onBindViewHolder(preferenceViewHolder);
                        Button button = (Button) preferenceViewHolder.findViewById(R.id.left_button);
                        button.setText(R.string.wifi_display_pause);
                        button.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.wfd.WifiDisplaySettings.1.1
                            @Override // android.view.View.OnClickListener
                            public void onClick(View view) {
                                WifiDisplaySettings.this.mDisplayManager.pauseWifiDisplay();
                            }
                        });
                        Button button2 = (Button) preferenceViewHolder.findViewById(R.id.right_button);
                        button2.setText(R.string.wifi_display_resume);
                        button2.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.wfd.WifiDisplaySettings.1.2
                            @Override // android.view.View.OnClickListener
                            public void onClick(View view) {
                                WifiDisplaySettings.this.mDisplayManager.resumeWifiDisplay();
                            }
                        });
                    }
                });
                this.mCertCategory.setLayoutResource(R.layout.two_buttons_panel);
            }
        }
        SwitchPreference switchPreference = new SwitchPreference(getPrefContext()) { // from class: com.android.settings.wfd.WifiDisplaySettings.2
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.support.v7.preference.TwoStatePreference, android.support.v7.preference.Preference
            public void onClick() {
                WifiDisplaySettings.this.mListen = !WifiDisplaySettings.this.mListen;
                WifiDisplaySettings.this.setListenMode(WifiDisplaySettings.this.mListen);
                setChecked(WifiDisplaySettings.this.mListen);
            }
        };
        switchPreference.setTitle(R.string.wifi_display_listen_mode);
        switchPreference.setChecked(this.mListen);
        this.mCertCategory.addPreference(switchPreference);
        SwitchPreference switchPreference2 = new SwitchPreference(getPrefContext()) { // from class: com.android.settings.wfd.WifiDisplaySettings.3
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.support.v7.preference.TwoStatePreference, android.support.v7.preference.Preference
            public void onClick() {
                WifiDisplaySettings.this.mAutoGO = !WifiDisplaySettings.this.mAutoGO;
                if (WifiDisplaySettings.this.mAutoGO) {
                    WifiDisplaySettings.this.startAutoGO();
                } else {
                    WifiDisplaySettings.this.stopAutoGO();
                }
                setChecked(WifiDisplaySettings.this.mAutoGO);
            }
        };
        switchPreference2.setTitle(R.string.wifi_display_autonomous_go);
        switchPreference2.setChecked(this.mAutoGO);
        this.mCertCategory.addPreference(switchPreference2);
        ListPreference listPreference = new ListPreference(getPrefContext());
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: com.android.settings.wfd.WifiDisplaySettings.4
            @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference2, Object obj) {
                int parseInt = Integer.parseInt((String) obj);
                if (parseInt != WifiDisplaySettings.this.mWpsConfig) {
                    WifiDisplaySettings.this.mWpsConfig = parseInt;
                    WifiDisplaySettings.this.getActivity().invalidateOptionsMenu();
                    Settings.Global.putInt(WifiDisplaySettings.this.getActivity().getContentResolver(), "wifi_display_wps_config", WifiDisplaySettings.this.mWpsConfig);
                    return true;
                }
                return true;
            }
        });
        this.mWpsConfig = Settings.Global.getInt(getActivity().getContentResolver(), "wifi_display_wps_config", 4);
        listPreference.setKey("wps");
        listPreference.setTitle(R.string.wifi_display_wps_config);
        listPreference.setEntries(new String[]{"Default", "PBC", "KEYPAD", "DISPLAY"});
        listPreference.setEntryValues(new String[]{"4", "0", "2", "1"});
        listPreference.setValue("" + this.mWpsConfig);
        listPreference.setSummary("%1$s");
        this.mCertCategory.addPreference(listPreference);
        ListPreference listPreference2 = new ListPreference(getPrefContext());
        listPreference2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: com.android.settings.wfd.WifiDisplaySettings.5
            @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference2, Object obj) {
                int parseInt = Integer.parseInt((String) obj);
                if (parseInt != WifiDisplaySettings.this.mListenChannel) {
                    WifiDisplaySettings.this.mListenChannel = parseInt;
                    WifiDisplaySettings.this.getActivity().invalidateOptionsMenu();
                    WifiDisplaySettings.this.setWifiP2pChannels(WifiDisplaySettings.this.mListenChannel, WifiDisplaySettings.this.mOperatingChannel);
                    return true;
                }
                return true;
            }
        });
        listPreference2.setKey("listening_channel");
        listPreference2.setTitle(R.string.wifi_display_listen_channel);
        listPreference2.setEntries(new String[]{"Auto", "1", "6", "11"});
        listPreference2.setEntryValues(new String[]{"0", "1", "6", "11"});
        listPreference2.setValue("" + this.mListenChannel);
        listPreference2.setSummary("%1$s");
        this.mCertCategory.addPreference(listPreference2);
        ListPreference listPreference3 = new ListPreference(getPrefContext());
        listPreference3.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: com.android.settings.wfd.WifiDisplaySettings.6
            @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference2, Object obj) {
                int parseInt = Integer.parseInt((String) obj);
                if (parseInt != WifiDisplaySettings.this.mOperatingChannel) {
                    WifiDisplaySettings.this.mOperatingChannel = parseInt;
                    WifiDisplaySettings.this.getActivity().invalidateOptionsMenu();
                    WifiDisplaySettings.this.setWifiP2pChannels(WifiDisplaySettings.this.mListenChannel, WifiDisplaySettings.this.mOperatingChannel);
                    return true;
                }
                return true;
            }
        });
        listPreference3.setKey("operating_channel");
        listPreference3.setTitle(R.string.wifi_display_operating_channel);
        listPreference3.setEntries(new String[]{"Auto", "1", "6", "11", "36"});
        listPreference3.setEntryValues(new String[]{"0", "1", "6", "11", "36"});
        listPreference3.setValue("" + this.mOperatingChannel);
        listPreference3.setSummary("%1$s");
        this.mCertCategory.addPreference(listPreference3);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startAutoGO() {
        this.mWifiP2pManager.createGroup(this.mWifiP2pChannel, new WifiP2pManager.ActionListener() { // from class: com.android.settings.wfd.WifiDisplaySettings.7
            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
            public void onSuccess() {
            }

            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
            public void onFailure(int i) {
                Slog.e("WifiDisplaySettings", "Failed to start AutoGO with reason " + i + ".");
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopAutoGO() {
        this.mWifiP2pManager.removeGroup(this.mWifiP2pChannel, new WifiP2pManager.ActionListener() { // from class: com.android.settings.wfd.WifiDisplaySettings.8
            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
            public void onSuccess() {
            }

            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
            public void onFailure(int i) {
                Slog.e("WifiDisplaySettings", "Failed to stop AutoGO with reason " + i + ".");
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setListenMode(final boolean z) {
        this.mWifiP2pManager.listen(this.mWifiP2pChannel, z, new WifiP2pManager.ActionListener() { // from class: com.android.settings.wfd.WifiDisplaySettings.9
            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
            public void onSuccess() {
            }

            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
            public void onFailure(int i) {
                StringBuilder sb = new StringBuilder();
                sb.append("Failed to ");
                sb.append(z ? "entered" : "exited");
                sb.append(" listen mode with reason ");
                sb.append(i);
                sb.append(".");
                Slog.e("WifiDisplaySettings", sb.toString());
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setWifiP2pChannels(int i, int i2) {
        this.mWifiP2pManager.setWifiP2pChannels(this.mWifiP2pChannel, i, i2, new WifiP2pManager.ActionListener() { // from class: com.android.settings.wfd.WifiDisplaySettings.10
            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
            public void onSuccess() {
            }

            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
            public void onFailure(int i3) {
                Slog.e("WifiDisplaySettings", "Failed to set wifi p2p channels with reason " + i3 + ".");
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void toggleRoute(MediaRouter.RouteInfo routeInfo) {
        if (routeInfo.isSelected()) {
            MediaRouteDialogPresenter.showDialogFragment(getActivity(), 4, (View.OnClickListener) null);
            return;
        }
        if (this.mWfdChangeResolution != null) {
            this.mWfdChangeResolution.prepareWfdConnect();
        }
        routeInfo.select();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void pairWifiDisplay(WifiDisplay wifiDisplay) {
        if (wifiDisplay.canConnect()) {
            if (this.mWfdChangeResolution != null) {
                this.mWfdChangeResolution.prepareWfdConnect();
            }
            this.mDisplayManager.connectWifiDisplay(wifiDisplay.getDeviceAddress());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showWifiDisplayOptionsDialog(final WifiDisplay wifiDisplay) {
        View inflate = getActivity().getLayoutInflater().inflate(R.layout.wifi_display_options, (ViewGroup) null);
        final EditText editText = (EditText) inflate.findViewById(R.id.name);
        editText.setText(wifiDisplay.getFriendlyDisplayName());
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() { // from class: com.android.settings.wfd.WifiDisplaySettings.11
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                String trim = editText.getText().toString().trim();
                WifiDisplaySettings.this.mDisplayManager.renameWifiDisplay(wifiDisplay.getDeviceAddress(), (trim.isEmpty() || trim.equals(wifiDisplay.getDeviceName())) ? null : null);
            }
        };
        new AlertDialog.Builder(getActivity()).setCancelable(true).setTitle(R.string.wifi_display_options_title).setView(inflate).setPositiveButton(R.string.wifi_display_options_done, onClickListener).setNegativeButton(R.string.wifi_display_options_forget, new DialogInterface.OnClickListener() { // from class: com.android.settings.wfd.WifiDisplaySettings.12
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                WifiDisplaySettings.this.mDisplayManager.forgetWifiDisplay(wifiDisplay.getDeviceAddress());
            }
        }).create().show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class RoutePreference extends Preference implements Preference.OnPreferenceClickListener {
        private final MediaRouter.RouteInfo mRoute;

        public RoutePreference(Context context, MediaRouter.RouteInfo routeInfo) {
            super(context);
            this.mRoute = routeInfo;
            setTitle(routeInfo.getName());
            setSummary(routeInfo.getDescription());
            setEnabled(routeInfo.isEnabled());
            if (routeInfo.isSelected()) {
                setOrder(2);
                if (routeInfo.isConnecting()) {
                    setSummary(R.string.wifi_display_status_connecting);
                } else {
                    setSummary(R.string.wifi_display_status_connected);
                }
            } else if (isEnabled()) {
                setOrder(3);
            } else {
                setOrder(4);
                if (routeInfo.getStatusCode() == 5) {
                    setSummary(R.string.wifi_display_status_in_use);
                } else {
                    setSummary(R.string.wifi_display_status_not_available);
                }
            }
            setOnPreferenceClickListener(this);
        }

        @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
        public boolean onPreferenceClick(Preference preference) {
            WifiDisplaySettings.this.toggleRoute(this.mRoute);
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class WifiDisplayRoutePreference extends RoutePreference implements View.OnClickListener {
        private final WifiDisplay mDisplay;

        public WifiDisplayRoutePreference(Context context, MediaRouter.RouteInfo routeInfo, WifiDisplay wifiDisplay) {
            super(context, routeInfo);
            this.mDisplay = wifiDisplay;
            setWidgetLayoutResource(R.layout.wifi_display_preference);
        }

        @Override // android.support.v7.preference.Preference
        public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
            super.onBindViewHolder(preferenceViewHolder);
            ImageView imageView = (ImageView) preferenceViewHolder.findViewById(R.id.deviceDetails);
            if (imageView != null) {
                imageView.setOnClickListener(this);
                if (!isEnabled()) {
                    TypedValue typedValue = new TypedValue();
                    getContext().getTheme().resolveAttribute(16842803, typedValue, true);
                    imageView.setImageAlpha((int) (typedValue.getFloat() * 255.0f));
                    imageView.setEnabled(true);
                }
            }
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            WifiDisplaySettings.this.showWifiDisplayOptionsDialog(this.mDisplay);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class UnpairedWifiDisplayPreference extends Preference implements Preference.OnPreferenceClickListener {
        private final WifiDisplay mDisplay;

        public UnpairedWifiDisplayPreference(Context context, WifiDisplay wifiDisplay) {
            super(context);
            this.mDisplay = wifiDisplay;
            setTitle(wifiDisplay.getFriendlyDisplayName());
            setSummary(17041126);
            setEnabled(wifiDisplay.canConnect());
            if (isEnabled()) {
                setOrder(3);
            } else {
                setOrder(4);
                setSummary(R.string.wifi_display_status_in_use);
            }
            setOnPreferenceClickListener(this);
        }

        @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
        public boolean onPreferenceClick(Preference preference) {
            WifiDisplaySettings.this.pairWifiDisplay(this.mDisplay);
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SummaryProvider implements SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final MediaRouter mRouter;
        private final MediaRouter.Callback mRouterCallback = new MediaRouter.SimpleCallback() { // from class: com.android.settings.wfd.WifiDisplaySettings.SummaryProvider.1
            @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
            public void onRouteSelected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
                SummaryProvider.this.updateSummary();
            }

            @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
            public void onRouteUnselected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
                SummaryProvider.this.updateSummary();
            }

            @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
            public void onRouteAdded(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
                SummaryProvider.this.updateSummary();
            }

            @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
            public void onRouteRemoved(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
                SummaryProvider.this.updateSummary();
            }

            @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
            public void onRouteChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
                SummaryProvider.this.updateSummary();
            }
        };
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
            this.mRouter = (MediaRouter) context.getSystemService("media_router");
        }

        @Override // com.android.settings.dashboard.SummaryLoader.SummaryProvider
        public void setListening(boolean z) {
            if (z) {
                this.mRouter.addCallback(4, this.mRouterCallback);
                updateSummary();
                return;
            }
            this.mRouter.removeCallback(this.mRouterCallback);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updateSummary() {
            String string = this.mContext.getString(R.string.disconnected);
            int routeCount = this.mRouter.getRouteCount();
            int i = 0;
            while (true) {
                if (i >= routeCount) {
                    break;
                }
                MediaRouter.RouteInfo routeAt = this.mRouter.getRouteAt(i);
                if (!routeAt.matchesTypes(4) || !routeAt.isSelected() || routeAt.isConnecting()) {
                    i++;
                } else {
                    string = this.mContext.getString(R.string.wifi_display_status_connected);
                    break;
                }
            }
            this.mSummaryLoader.setSummary(this, string);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ SummaryLoader.SummaryProvider lambda$static$0(Activity activity, SummaryLoader summaryLoader) {
        return new SummaryProvider(activity, summaryLoader);
    }
}
