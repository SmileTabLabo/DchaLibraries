package com.android.settings.wifi.details;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v4.text.BidiFormatter;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.ActionButtonPreference;
import com.android.settings.widget.EntityHeaderController;
import com.android.settings.wifi.WifiDetailPreference;
import com.android.settings.wifi.WifiDialog;
import com.android.settings.wifi.WifiUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.wifi.AccessPoint;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
/* loaded from: classes.dex */
public class WifiDetailPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, WifiDialog.WifiDialogListener, LifecycleObserver, OnPause, OnResume {
    private static final boolean DEBUG = Log.isLoggable("WifiDetailsPrefCtrl", 3);
    @VisibleForTesting
    static final String KEY_BUTTONS_PREF = "buttons";
    @VisibleForTesting
    static final String KEY_DNS_PREF = "dns";
    @VisibleForTesting
    static final String KEY_FREQUENCY_PREF = "frequency";
    @VisibleForTesting
    static final String KEY_GATEWAY_PREF = "gateway";
    @VisibleForTesting
    static final String KEY_HEADER = "connection_header";
    @VisibleForTesting
    static final String KEY_IPV6_ADDRESSES_PREF = "ipv6_addresses";
    @VisibleForTesting
    static final String KEY_IPV6_CATEGORY = "ipv6_category";
    @VisibleForTesting
    static final String KEY_IP_ADDRESS_PREF = "ip_address";
    @VisibleForTesting
    static final String KEY_LINK_SPEED = "link_speed";
    @VisibleForTesting
    static final String KEY_MAC_ADDRESS_PREF = "mac_address";
    @VisibleForTesting
    static final String KEY_SECURITY_PREF = "security";
    @VisibleForTesting
    static final String KEY_SIGNAL_STRENGTH_PREF = "signal_strength";
    @VisibleForTesting
    static final String KEY_SUBNET_MASK_PREF = "subnet_mask";
    private AccessPoint mAccessPoint;
    private ActionButtonPreference mButtonsPref;
    private final ConnectivityManager mConnectivityManager;
    private WifiDetailPreference mDnsPref;
    private EntityHeaderController mEntityHeaderController;
    private final IntentFilter mFilter;
    private final Fragment mFragment;
    private WifiDetailPreference mFrequencyPref;
    private WifiDetailPreference mGatewayPref;
    private final Handler mHandler;
    private final IconInjector mIconInjector;
    private WifiDetailPreference mIpAddressPref;
    private Preference mIpv6AddressPref;
    private PreferenceCategory mIpv6Category;
    private LinkProperties mLinkProperties;
    private WifiDetailPreference mLinkSpeedPref;
    private WifiDetailPreference mMacAddressPref;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private Network mNetwork;
    private final ConnectivityManager.NetworkCallback mNetworkCallback;
    private NetworkCapabilities mNetworkCapabilities;
    private NetworkInfo mNetworkInfo;
    private final NetworkRequest mNetworkRequest;
    private final BroadcastReceiver mReceiver;
    private int mRssiSignalLevel;
    private WifiDetailPreference mSecurityPref;
    private String[] mSignalStr;
    private WifiDetailPreference mSignalStrengthPref;
    private WifiDetailPreference mSubnetPref;
    private WifiConfiguration mWifiConfig;
    private WifiInfo mWifiInfo;
    private final WifiManager mWifiManager;

    public static WifiDetailPreferenceController newInstance(AccessPoint accessPoint, ConnectivityManager connectivityManager, Context context, Fragment fragment, Handler handler, Lifecycle lifecycle, WifiManager wifiManager, MetricsFeatureProvider metricsFeatureProvider) {
        return new WifiDetailPreferenceController(accessPoint, connectivityManager, context, fragment, handler, lifecycle, wifiManager, metricsFeatureProvider, new IconInjector(context));
    }

    @VisibleForTesting
    WifiDetailPreferenceController(AccessPoint accessPoint, ConnectivityManager connectivityManager, Context context, Fragment fragment, Handler handler, Lifecycle lifecycle, WifiManager wifiManager, MetricsFeatureProvider metricsFeatureProvider, IconInjector iconInjector) {
        super(context);
        this.mRssiSignalLevel = -1;
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.settings.wifi.details.WifiDetailPreferenceController.1
            /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                char c;
                String action = intent.getAction();
                int hashCode = action.hashCode();
                if (hashCode == -385684331) {
                    if (action.equals("android.net.wifi.RSSI_CHANGED")) {
                        c = 2;
                    }
                    c = 65535;
                } else if (hashCode != -343630553) {
                    if (hashCode == 1625920338 && action.equals("android.net.wifi.CONFIGURED_NETWORKS_CHANGE")) {
                        c = 0;
                    }
                    c = 65535;
                } else {
                    if (action.equals("android.net.wifi.STATE_CHANGE")) {
                        c = 1;
                    }
                    c = 65535;
                }
                switch (c) {
                    case 0:
                        if (!intent.getBooleanExtra("multipleChanges", WifiDetailPreferenceController.DEBUG)) {
                            WifiConfiguration wifiConfiguration = (WifiConfiguration) intent.getParcelableExtra("wifiConfiguration");
                            if (WifiDetailPreferenceController.this.mAccessPoint.matches(wifiConfiguration)) {
                                WifiDetailPreferenceController.this.mWifiConfig = wifiConfiguration;
                                break;
                            }
                        }
                        break;
                    case 1:
                    case 2:
                        break;
                    default:
                        return;
                }
                WifiDetailPreferenceController.this.updateInfo();
            }
        };
        this.mNetworkRequest = new NetworkRequest.Builder().clearCapabilities().addTransportType(1).build();
        this.mNetworkCallback = new ConnectivityManager.NetworkCallback() { // from class: com.android.settings.wifi.details.WifiDetailPreferenceController.2
            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                if (network.equals(WifiDetailPreferenceController.this.mNetwork) && !linkProperties.equals(WifiDetailPreferenceController.this.mLinkProperties)) {
                    WifiDetailPreferenceController.this.mLinkProperties = linkProperties;
                    WifiDetailPreferenceController.this.updateIpLayerInfo();
                }
            }

            private boolean hasCapabilityChanged(NetworkCapabilities networkCapabilities, int i) {
                if (WifiDetailPreferenceController.this.mNetworkCapabilities != null && WifiDetailPreferenceController.this.mNetworkCapabilities.hasCapability(i) == networkCapabilities.hasCapability(i)) {
                    return WifiDetailPreferenceController.DEBUG;
                }
                return true;
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                if (network.equals(WifiDetailPreferenceController.this.mNetwork) && !networkCapabilities.equals(WifiDetailPreferenceController.this.mNetworkCapabilities)) {
                    if (hasCapabilityChanged(networkCapabilities, 16) || hasCapabilityChanged(networkCapabilities, 17)) {
                        WifiDetailPreferenceController.this.refreshNetworkState();
                    }
                    WifiDetailPreferenceController.this.mNetworkCapabilities = networkCapabilities;
                    WifiDetailPreferenceController.this.updateIpLayerInfo();
                }
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLost(Network network) {
                if (network.equals(WifiDetailPreferenceController.this.mNetwork)) {
                    WifiDetailPreferenceController.this.exitActivity();
                }
            }
        };
        this.mAccessPoint = accessPoint;
        this.mConnectivityManager = connectivityManager;
        this.mFragment = fragment;
        this.mHandler = handler;
        this.mSignalStr = context.getResources().getStringArray(R.array.wifi_signal);
        this.mWifiConfig = accessPoint.getConfig();
        this.mWifiManager = wifiManager;
        this.mMetricsFeatureProvider = metricsFeatureProvider;
        this.mIconInjector = iconInjector;
        this.mFilter = new IntentFilter();
        this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.mFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        lifecycle.addObserver(this);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        return true;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return null;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        setupEntityHeader(preferenceScreen);
        this.mButtonsPref = ((ActionButtonPreference) preferenceScreen.findPreference(KEY_BUTTONS_PREF)).setButton1Text(R.string.forget).setButton1Positive(DEBUG).setButton1OnClickListener(new View.OnClickListener() { // from class: com.android.settings.wifi.details.-$$Lambda$WifiDetailPreferenceController$HDOTYXVF80U7sCZa22KqorlzriY
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WifiDetailPreferenceController.this.forgetNetwork();
            }
        }).setButton2Text(R.string.wifi_sign_in_button_text).setButton2Positive(true).setButton2OnClickListener(new View.OnClickListener() { // from class: com.android.settings.wifi.details.-$$Lambda$WifiDetailPreferenceController$PxMNywf_HXiVAESmLubuiIo869s
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WifiDetailPreferenceController.this.signIntoNetwork();
            }
        });
        this.mSignalStrengthPref = (WifiDetailPreference) preferenceScreen.findPreference(KEY_SIGNAL_STRENGTH_PREF);
        this.mLinkSpeedPref = (WifiDetailPreference) preferenceScreen.findPreference(KEY_LINK_SPEED);
        this.mFrequencyPref = (WifiDetailPreference) preferenceScreen.findPreference(KEY_FREQUENCY_PREF);
        this.mSecurityPref = (WifiDetailPreference) preferenceScreen.findPreference(KEY_SECURITY_PREF);
        this.mMacAddressPref = (WifiDetailPreference) preferenceScreen.findPreference(KEY_MAC_ADDRESS_PREF);
        this.mIpAddressPref = (WifiDetailPreference) preferenceScreen.findPreference(KEY_IP_ADDRESS_PREF);
        this.mGatewayPref = (WifiDetailPreference) preferenceScreen.findPreference(KEY_GATEWAY_PREF);
        this.mSubnetPref = (WifiDetailPreference) preferenceScreen.findPreference(KEY_SUBNET_MASK_PREF);
        this.mDnsPref = (WifiDetailPreference) preferenceScreen.findPreference(KEY_DNS_PREF);
        this.mIpv6Category = (PreferenceCategory) preferenceScreen.findPreference(KEY_IPV6_CATEGORY);
        this.mIpv6AddressPref = preferenceScreen.findPreference(KEY_IPV6_ADDRESSES_PREF);
        this.mSecurityPref.setDetailText(this.mAccessPoint.getSecurityString(DEBUG));
    }

    private void setupEntityHeader(PreferenceScreen preferenceScreen) {
        LayoutPreference layoutPreference = (LayoutPreference) preferenceScreen.findPreference(KEY_HEADER);
        this.mEntityHeaderController = EntityHeaderController.newInstance(this.mFragment.getActivity(), this.mFragment, layoutPreference.findViewById(R.id.entity_header));
        ImageView imageView = (ImageView) layoutPreference.findViewById(R.id.entity_header_icon);
        imageView.setBackground(this.mContext.getDrawable(R.drawable.ic_settings_widget_background));
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        this.mEntityHeaderController.setLabel(this.mAccessPoint.getSsidStr());
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnResume
    public void onResume() {
        this.mNetwork = this.mWifiManager.getCurrentNetwork();
        this.mLinkProperties = this.mConnectivityManager.getLinkProperties(this.mNetwork);
        this.mNetworkCapabilities = this.mConnectivityManager.getNetworkCapabilities(this.mNetwork);
        updateInfo();
        this.mContext.registerReceiver(this.mReceiver, this.mFilter);
        this.mConnectivityManager.registerNetworkCallback(this.mNetworkRequest, this.mNetworkCallback, this.mHandler);
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnPause
    public void onPause() {
        this.mNetwork = null;
        this.mLinkProperties = null;
        this.mNetworkCapabilities = null;
        this.mNetworkInfo = null;
        this.mWifiInfo = null;
        this.mContext.unregisterReceiver(this.mReceiver);
        this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateInfo() {
        this.mNetworkInfo = this.mConnectivityManager.getNetworkInfo(this.mNetwork);
        this.mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (this.mNetwork == null || this.mNetworkInfo == null || this.mWifiInfo == null) {
            exitActivity();
            return;
        }
        this.mButtonsPref.setButton1Visible(canForgetNetwork());
        refreshNetworkState();
        refreshRssiViews();
        this.mMacAddressPref.setDetailText(this.mWifiInfo.getMacAddress());
        this.mLinkSpeedPref.setVisible(this.mWifiInfo.getLinkSpeed() >= 0);
        this.mLinkSpeedPref.setDetailText(this.mContext.getString(R.string.link_speed, Integer.valueOf(this.mWifiInfo.getLinkSpeed())));
        int frequency = this.mWifiInfo.getFrequency();
        String str = null;
        if (frequency >= 2400 && frequency < 2500) {
            str = this.mContext.getResources().getString(R.string.wifi_band_24ghz);
        } else if (frequency >= 4900 && frequency < 5900) {
            str = this.mContext.getResources().getString(R.string.wifi_band_5ghz);
        } else {
            Log.e("WifiDetailsPrefCtrl", "Unexpected frequency " + frequency);
        }
        this.mFrequencyPref.setDetailText(str);
        updateIpLayerInfo();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void exitActivity() {
        if (DEBUG) {
            Log.d("WifiDetailsPrefCtrl", "Exiting the WifiNetworkDetailsPage");
        }
        this.mFragment.getActivity().finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshNetworkState() {
        this.mAccessPoint.update(this.mWifiConfig, this.mWifiInfo, this.mNetworkInfo);
        this.mEntityHeaderController.setSummary(this.mAccessPoint.getSettingsSummary()).done(this.mFragment.getActivity(), true);
    }

    private void refreshRssiViews() {
        int level = this.mAccessPoint.getLevel();
        if (this.mRssiSignalLevel == level) {
            return;
        }
        this.mRssiSignalLevel = level;
        Drawable icon = this.mIconInjector.getIcon(this.mRssiSignalLevel);
        icon.setTint(Utils.getColorAccent(this.mContext));
        this.mEntityHeaderController.setIcon(icon).done(this.mFragment.getActivity(), true);
        Drawable mutate = icon.getConstantState().newDrawable().mutate();
        mutate.setTint(this.mContext.getResources().getColor(R.color.wifi_details_icon_color, this.mContext.getTheme()));
        this.mSignalStrengthPref.setIcon(mutate);
        this.mSignalStrengthPref.setDetailText(this.mSignalStr[this.mRssiSignalLevel]);
    }

    private void updatePreference(WifiDetailPreference wifiDetailPreference, String str) {
        if (!TextUtils.isEmpty(str)) {
            wifiDetailPreference.setDetailText(str);
            wifiDetailPreference.setVisible(true);
            return;
        }
        wifiDetailPreference.setVisible(DEBUG);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateIpLayerInfo() {
        this.mButtonsPref.setButton2Visible(canSignIntoNetwork());
        this.mButtonsPref.setVisible(canSignIntoNetwork() || canForgetNetwork());
        if (this.mNetwork == null || this.mLinkProperties == null) {
            this.mIpAddressPref.setVisible(DEBUG);
            this.mSubnetPref.setVisible(DEBUG);
            this.mGatewayPref.setVisible(DEBUG);
            this.mDnsPref.setVisible(DEBUG);
            this.mIpv6Category.setVisible(DEBUG);
            return;
        }
        StringJoiner stringJoiner = new StringJoiner("\n");
        String str = null;
        String str2 = null;
        String str3 = null;
        for (LinkAddress linkAddress : this.mLinkProperties.getLinkAddresses()) {
            if (linkAddress.getAddress() instanceof Inet4Address) {
                str2 = linkAddress.getAddress().getHostAddress();
                str3 = ipv4PrefixLengthToSubnetMask(linkAddress.getPrefixLength());
            } else if (linkAddress.getAddress() instanceof Inet6Address) {
                stringJoiner.add(linkAddress.getAddress().getHostAddress());
            }
        }
        Iterator<RouteInfo> it = this.mLinkProperties.getRoutes().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            RouteInfo next = it.next();
            if (next.isIPv4Default() && next.hasGateway()) {
                str = next.getGateway().getHostAddress();
                break;
            }
        }
        updatePreference(this.mIpAddressPref, str2);
        updatePreference(this.mSubnetPref, str3);
        updatePreference(this.mGatewayPref, str);
        updatePreference(this.mDnsPref, (String) this.mLinkProperties.getDnsServers().stream().map(new Function() { // from class: com.android.settings.wifi.details.-$$Lambda$WifiDetailPreferenceController$XZAGhHrbkIDyusER4MAM6luKcT0
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                String hostAddress;
                hostAddress = ((InetAddress) obj).getHostAddress();
                return hostAddress;
            }
        }).collect(Collectors.joining("\n")));
        if (stringJoiner.length() > 0) {
            this.mIpv6AddressPref.setSummary(BidiFormatter.getInstance().unicodeWrap(stringJoiner.toString()));
            this.mIpv6Category.setVisible(true);
            return;
        }
        this.mIpv6Category.setVisible(DEBUG);
    }

    private static String ipv4PrefixLengthToSubnetMask(int i) {
        try {
            return NetworkUtils.getNetworkPart(InetAddress.getByAddress(new byte[]{-1, -1, -1, -1}), i).getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private boolean canForgetNetwork() {
        if ((this.mWifiInfo == null || !this.mWifiInfo.isEphemeral()) && !canModifyNetwork()) {
            return DEBUG;
        }
        return true;
    }

    public boolean canModifyNetwork() {
        if (this.mWifiConfig == null || WifiUtils.isNetworkLockedDown(this.mContext, this.mWifiConfig)) {
            return DEBUG;
        }
        return true;
    }

    private boolean canSignIntoNetwork() {
        return WifiUtils.canSignIntoNetwork(this.mNetworkCapabilities);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void forgetNetwork() {
        if (this.mWifiInfo != null && this.mWifiInfo.isEphemeral()) {
            this.mWifiManager.disableEphemeralNetwork(this.mWifiInfo.getSSID());
        } else if (this.mWifiConfig != null) {
            if (this.mWifiConfig.isPasspoint()) {
                this.mWifiManager.removePasspointConfiguration(this.mWifiConfig.FQDN);
            } else {
                this.mWifiManager.forget(this.mWifiConfig.networkId, null);
            }
        }
        this.mMetricsFeatureProvider.action(this.mFragment.getActivity(), 137, new Pair[0]);
        this.mFragment.getActivity().finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void signIntoNetwork() {
        this.mMetricsFeatureProvider.action(this.mFragment.getActivity(), 1008, new Pair[0]);
        this.mConnectivityManager.startCaptivePortalApp(this.mNetwork);
    }

    @Override // com.android.settings.wifi.WifiDialog.WifiDialogListener
    public void onForget(WifiDialog wifiDialog) {
    }

    @Override // com.android.settings.wifi.WifiDialog.WifiDialogListener
    public void onSubmit(WifiDialog wifiDialog) {
        if (wifiDialog.getController() != null) {
            this.mWifiManager.save(wifiDialog.getController().getConfig(), new WifiManager.ActionListener() { // from class: com.android.settings.wifi.details.WifiDetailPreferenceController.3
                public void onSuccess() {
                }

                public void onFailure(int i) {
                    Activity activity = WifiDetailPreferenceController.this.mFragment.getActivity();
                    if (activity != null) {
                        Toast.makeText(activity, (int) R.string.wifi_failed_save_message, 0).show();
                    }
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @VisibleForTesting
    /* loaded from: classes.dex */
    public static class IconInjector {
        private final Context mContext;

        public IconInjector(Context context) {
            this.mContext = context;
        }

        public Drawable getIcon(int i) {
            return this.mContext.getDrawable(Utils.getWifiIconResource(i)).mutate();
        }
    }
}
