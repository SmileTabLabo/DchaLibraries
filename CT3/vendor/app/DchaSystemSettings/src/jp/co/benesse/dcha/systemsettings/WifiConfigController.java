package jp.co.benesse.dcha.systemsettings;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.UserManager;
import android.security.KeyStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/WifiConfigController.class */
public class WifiConfigController implements TextWatcher, View.OnClickListener, AdapterView.OnItemSelectedListener, TextView.OnEditorActionListener, View.OnKeyListener {
    private final AccessPoint mAccessPoint;
    private int mAccessPointSecurity;
    private final WifiConfigUiBase mConfigUi;
    private Context mContext;
    private TextView mDns1View;
    private TextView mDns2View;
    private String mDoNotProvideEapUserCertString;
    private String mDoNotValidateEapServerString;
    private TextView mEapAnonymousView;
    private Spinner mEapCaCertSpinner;
    private TextView mEapDomainView;
    private TextView mEapIdentityView;
    private Spinner mEapMethodSpinner;
    private Spinner mEapUserCertSpinner;
    private TextView mGatewayView;
    private TextView mIpAddressView;
    private Spinner mIpSettingsSpinner;
    private String[] mLevels;
    private int mMode;
    private String mMultipleCertSetString;
    private TextView mNetworkPrefixLengthView;
    private TextView mPasswordView;
    private ArrayAdapter<String> mPhase2Adapter;
    private final ArrayAdapter<String> mPhase2FullAdapter;
    private final ArrayAdapter<String> mPhase2PeapAdapter;
    private Spinner mPhase2Spinner;
    private TextView mProxyExclusionListView;
    private TextView mProxyHostView;
    private TextView mProxyPacView;
    private TextView mProxyPortView;
    private Spinner mProxySettingsSpinner;
    private Spinner mSecuritySpinner;
    private CheckBox mSharedCheckBox;
    private TextView mSsidView;
    private final Handler mTextViewChangedHandler;
    private String mUnspecifiedCertString;
    private String mUseSystemCertsString;
    private final View mView;
    private static final Pattern HOSTNAME_PATTERN = Pattern.compile("^$|^[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*(\\.[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*)*$");
    private static final Pattern EXCLUSION_PATTERN = Pattern.compile("$|^[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*(\\.[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*)*$");
    private IpConfiguration.IpAssignment mIpAssignment = IpConfiguration.IpAssignment.UNASSIGNED;
    private IpConfiguration.ProxySettings mProxySettings = IpConfiguration.ProxySettings.UNASSIGNED;
    private ProxyInfo mHttpProxy = null;
    private StaticIpConfiguration mStaticIpConfiguration = null;

    public WifiConfigController(NetworkSettingActivity networkSettingActivity, WifiConfigUiBase wifiConfigUiBase, View view, AccessPoint accessPoint, int i) {
        Logger.d("WifiConfigController", "WifiConfigController 0001");
        this.mConfigUi = wifiConfigUiBase;
        this.mView = view;
        this.mAccessPoint = accessPoint;
        this.mAccessPointSecurity = accessPoint == null ? 0 : accessPoint.getSecurity();
        this.mMode = i;
        this.mTextViewChangedHandler = new Handler();
        this.mContext = this.mConfigUi.getContext();
        Resources resources = this.mContext.getResources();
        this.mLevels = resources.getStringArray(2131034117);
        this.mPhase2PeapAdapter = new ArrayAdapter<>(this.mContext, 17367048, resources.getStringArray(2131034118));
        this.mPhase2PeapAdapter.setDropDownViewResource(17367049);
        this.mPhase2FullAdapter = new ArrayAdapter<>(this.mContext, 17367048, resources.getStringArray(2131034119));
        this.mPhase2FullAdapter.setDropDownViewResource(17367049);
        this.mUnspecifiedCertString = this.mContext.getString(2131230846);
        this.mMultipleCertSetString = this.mContext.getString(2131230847);
        this.mUseSystemCertsString = this.mContext.getString(2131230848);
        this.mDoNotProvideEapUserCertString = this.mContext.getString(2131230849);
        this.mDoNotValidateEapServerString = this.mContext.getString(2131230850);
        this.mIpSettingsSpinner = (Spinner) this.mView.findViewById(2131361896);
        this.mIpSettingsSpinner.setOnItemSelectedListener(this);
        this.mProxySettingsSpinner = (Spinner) this.mView.findViewById(2131361887);
        this.mProxySettingsSpinner.setOnItemSelectedListener(this);
        this.mSharedCheckBox = (CheckBox) this.mView.findViewById(2131361903);
        if (this.mAccessPoint == null) {
            Logger.d("WifiConfigController", "WifiConfigController 0002");
            this.mConfigUi.setTitle(2131230805);
            this.mSsidView = (TextView) this.mView.findViewById(2131361858);
            this.mSsidView.addTextChangedListener(this);
            this.mSecuritySpinner = (Spinner) this.mView.findViewById(2131361859);
            this.mSecuritySpinner.setOnItemSelectedListener(this);
            this.mView.findViewById(2131361857).setVisibility(0);
            showIpConfigFields();
            showProxyFields();
            this.mView.findViewById(2131361882).setVisibility(0);
            ((CheckBox) this.mView.findViewById(2131361883)).setOnClickListener(this);
            this.mConfigUi.setSubmitButton(resources.getString(2131230859));
        } else {
            Logger.d("WifiConfigController", "WifiConfigController 0003");
            this.mConfigUi.setTitle(this.mAccessPoint.getSsid());
            ViewGroup viewGroup = (ViewGroup) this.mView.findViewById(2131361856);
            boolean z = false;
            boolean z2 = false;
            if (this.mAccessPoint.isSaved()) {
                Logger.d("WifiConfigController", "WifiConfigController 0004");
                WifiConfiguration config = this.mAccessPoint.getConfig();
                if (config.getIpAssignment() == IpConfiguration.IpAssignment.STATIC) {
                    Logger.d("WifiConfigController", "WifiConfigController 0005");
                    this.mIpSettingsSpinner.setSelection(1);
                    StaticIpConfiguration staticIpConfiguration = config.getStaticIpConfiguration();
                    z2 = true;
                    if (staticIpConfiguration != null) {
                        z2 = true;
                        if (staticIpConfiguration.ipAddress != null) {
                            Logger.d("WifiConfigController", "WifiConfigController 0006");
                            addRow(viewGroup, 2131230831, staticIpConfiguration.ipAddress.getAddress().getHostAddress());
                            z2 = true;
                        }
                    }
                } else {
                    Logger.d("WifiConfigController", "WifiConfigController 0007");
                    this.mIpSettingsSpinner.setSelection(0);
                }
                this.mSharedCheckBox.setEnabled(config.shared);
                if (!config.shared) {
                    Logger.d("WifiConfigController", "WifiConfigController 0008");
                    z2 = true;
                }
                if (config.getProxySettings() == IpConfiguration.ProxySettings.STATIC) {
                    Logger.d("WifiConfigController", "WifiConfigController 0009");
                    this.mProxySettingsSpinner.setSelection(1);
                    z2 = true;
                } else if (config.getProxySettings() == IpConfiguration.ProxySettings.PAC) {
                    Logger.d("WifiConfigController", "WifiConfigController 0010");
                    this.mProxySettingsSpinner.setSelection(2);
                    z2 = true;
                } else {
                    Logger.d("WifiConfigController", "WifiConfigController 0011");
                    this.mProxySettingsSpinner.setSelection(0);
                }
                z = z2;
                if (config != null) {
                    z = z2;
                    if (config.isPasspoint()) {
                        Logger.d("WifiConfigController", "WifiConfigController 0012");
                        addRow(viewGroup, 2131230832, String.format(this.mContext.getString(2131230833), config.providerFriendlyName));
                        z = z2;
                    }
                }
            }
            if ((!this.mAccessPoint.isSaved() && !this.mAccessPoint.isActive()) || this.mMode != 0) {
                Logger.d("WifiConfigController", "WifiConfigController 0013");
                showSecurityFields();
                showIpConfigFields();
                showProxyFields();
                this.mView.findViewById(2131361882).setVisibility(0);
                ((CheckBox) this.mView.findViewById(2131361883)).setOnClickListener(this);
                if (z) {
                    Logger.d("WifiConfigController", "WifiConfigController 0014");
                    ((CheckBox) this.mView.findViewById(2131361883)).setChecked(true);
                    this.mView.findViewById(2131361884).setVisibility(0);
                }
            }
            if (this.mMode == 2) {
                Logger.d("WifiConfigController", "WifiConfigController 0015");
                this.mConfigUi.setSubmitButton(resources.getString(2131230859));
            } else if (this.mMode == 1) {
                Logger.d("WifiConfigController", "WifiConfigController 0016");
                this.mConfigUi.setSubmitButton(resources.getString(2131230855));
            }
            NetworkInfo.DetailedState detailedState = this.mAccessPoint.getDetailedState();
            String signalString = getSignalString();
            if (detailedState == null && signalString != null) {
                Logger.d("WifiConfigController", "WifiConfigController 0017");
                this.mConfigUi.setSubmitButton(resources.getString(2131230855));
            }
            if (detailedState != null) {
                Logger.d("WifiConfigController", "WifiConfigController 0018");
                boolean isEphemeral = this.mAccessPoint.isEphemeral();
                WifiConfiguration config2 = this.mAccessPoint.getConfig();
                String str = null;
                if (config2 != null) {
                    str = null;
                    if (config2.isPasspoint()) {
                        Logger.d("WifiConfigController", "WifiConfigController 0019");
                        str = config2.providerFriendlyName;
                    }
                }
                addRow(viewGroup, 2131230828, AccessPoint.getSummary(this.mConfigUi.getContext(), detailedState, isEphemeral, str));
            }
            if (signalString != null) {
                Logger.d("WifiConfigController", "WifiConfigController 0020");
                addRow(viewGroup, 2131230827, signalString);
            }
            WifiInfo info = this.mAccessPoint.getInfo();
            if (info != null && info.getLinkSpeed() != -1) {
                Logger.d("WifiConfigController", "WifiConfigController 0021");
                addRow(viewGroup, 2131230829, String.format(resources.getString(2131230800), Integer.valueOf(info.getLinkSpeed())));
            }
            if (info != null && info.getFrequency() != -1) {
                Logger.d("WifiConfigController", "WifiConfigController 0022");
                int frequency = info.getFrequency();
                String str2 = null;
                if (frequency >= 2400 && frequency < 2500) {
                    Logger.d("WifiConfigController", "WifiConfigController 0023");
                    str2 = resources.getString(2131230798);
                } else if (frequency < 4900 || frequency >= 5900) {
                    Logger.d("WifiConfigController", "WifiConfigController 0025");
                    Logger.d("WifiConfigController", "Unexpected frequency " + frequency);
                } else {
                    Logger.d("WifiConfigController", "WifiConfigController 0024");
                    str2 = resources.getString(2131230799);
                }
                if (str2 != null) {
                    Logger.d("WifiConfigController", "WifiConfigController 0026");
                    addRow(viewGroup, 2131230830, str2);
                }
            }
            addRow(viewGroup, 2131230826, this.mAccessPoint.getSecurityString(false));
            this.mView.findViewById(2131361895).setVisibility(8);
            if (this.mAccessPoint.isSaved() || this.mAccessPoint.isActive()) {
                Logger.d("WifiConfigController", "WifiConfigController 0027");
                this.mConfigUi.setForgetButton(resources.getString(2131230857));
            }
        }
        UserManager userManager = (UserManager) this.mContext.getSystemService("user");
        if (!UserManager.isSplitSystemUser()) {
            Logger.d("WifiConfigController", "WifiConfigController 0027");
            this.mSharedCheckBox.setVisibility(8);
        }
        this.mConfigUi.setCancelButton(resources.getString(2131230861));
        if (this.mConfigUi.getSubmitButton() != null) {
            Logger.d("WifiConfigController", "WifiConfigController 0028");
            enableSubmitIfAppropriate();
        }
        Logger.d("WifiConfigController", "WifiConfigController 0029");
    }

    private void addRow(ViewGroup viewGroup, int i, String str) {
        Logger.d("WifiConfigController", "addRow 0001");
        View inflate = this.mConfigUi.getLayoutInflater().inflate(2130903052, viewGroup, false);
        ((TextView) inflate.findViewById(2131361904)).setText(i);
        ((TextView) inflate.findViewById(2131361905)).setText(str);
        viewGroup.addView(inflate);
        Logger.d("WifiConfigController", "addRow 0002");
    }

    private Inet4Address getIPv4Address(String str) {
        try {
            Logger.d("WifiConfigController", "getIPv4Address 0001");
            return (Inet4Address) NetworkUtils.numericToInetAddress(str);
        } catch (ClassCastException | IllegalArgumentException e) {
            Logger.d("WifiConfigController", "getIPv4Address 0002");
            return null;
        }
    }

    private String getSignalString() {
        Logger.d("WifiConfigController", "getSignalString 0001");
        int level = this.mAccessPoint.getLevel();
        return (level <= -1 || level >= this.mLevels.length) ? null : this.mLevels[level];
    }

    private boolean ipAndProxyFieldsAreValid() {
        int i;
        int i2;
        Logger.d("WifiConfigController", "ipAndProxyFieldsAreValid 0001");
        this.mIpAssignment = (this.mIpSettingsSpinner == null || this.mIpSettingsSpinner.getSelectedItemPosition() != 1) ? IpConfiguration.IpAssignment.DHCP : IpConfiguration.IpAssignment.STATIC;
        if (this.mIpAssignment == IpConfiguration.IpAssignment.STATIC) {
            Logger.d("WifiConfigController", "ipAndProxyFieldsAreValid 0002");
            this.mStaticIpConfiguration = new StaticIpConfiguration();
            if (validateIpConfigFields(this.mStaticIpConfiguration) != 0) {
                Logger.d("WifiConfigController", "ipAndProxyFieldsAreValid 0003");
                return false;
            }
        }
        int selectedItemPosition = this.mProxySettingsSpinner.getSelectedItemPosition();
        this.mProxySettings = IpConfiguration.ProxySettings.NONE;
        this.mHttpProxy = null;
        if (selectedItemPosition == 1 && this.mProxyHostView != null) {
            Logger.d("WifiConfigController", "ipAndProxyFieldsAreValid 0004");
            this.mProxySettings = IpConfiguration.ProxySettings.STATIC;
            String charSequence = this.mProxyHostView.getText().toString();
            String charSequence2 = this.mProxyPortView.getText().toString();
            String charSequence3 = this.mProxyExclusionListView.getText().toString();
            int i3 = 0;
            try {
                i = Integer.parseInt(charSequence2);
                i3 = i;
                i2 = validate(charSequence, charSequence2, charSequence3);
            } catch (NumberFormatException e) {
                Logger.d("WifiConfigController", "ipAndProxyFieldsAreValid 0005");
                Logger.e("WifiConfigController", "NumberFormatException", e);
                i = i3;
                i2 = 2131230794;
            }
            if (i2 != 0) {
                Logger.d("WifiConfigController", "ipAndProxyFieldsAreValid 0007");
                return false;
            }
            Logger.d("WifiConfigController", "ipAndProxyFieldsAreValid 0006");
            this.mHttpProxy = new ProxyInfo(charSequence, i, charSequence3);
        } else if (selectedItemPosition == 2 && this.mProxyPacView != null) {
            Logger.d("WifiConfigController", "ipAndProxyFieldsAreValid 0008");
            this.mProxySettings = IpConfiguration.ProxySettings.PAC;
            CharSequence text = this.mProxyPacView.getText();
            if (TextUtils.isEmpty(text)) {
                Logger.d("WifiConfigController", "ipAndProxyFieldsAreValid 0009");
                return false;
            }
            Uri parse = Uri.parse(text.toString());
            if (parse == null) {
                Logger.d("WifiConfigController", "ipAndProxyFieldsAreValid 0010");
                return false;
            }
            this.mHttpProxy = new ProxyInfo(parse);
        }
        Logger.d("WifiConfigController", "ipAndProxyFieldsAreValid 0011");
        return true;
    }

    private static boolean isWifiOnly(Context context) {
        boolean z = true;
        Logger.d("WifiConfigController", "isWifiOnly 0001");
        if (((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(0)) {
            z = false;
        }
        return z;
    }

    private void loadCertificates(Spinner spinner, String str, String str2, boolean z, boolean z2) {
        Logger.d("WifiConfigController", "loadCertificates 0001");
        Context context = this.mConfigUi.getContext();
        ArrayList arrayList = new ArrayList();
        arrayList.add(this.mUnspecifiedCertString);
        if (z) {
            Logger.d("WifiConfigController", "loadCertificates 0002");
            arrayList.add(this.mMultipleCertSetString);
        }
        if (z2) {
            Logger.d("WifiConfigController", "loadCertificates 0003");
            arrayList.add(this.mUseSystemCertsString);
        }
        arrayList.addAll(Arrays.asList(KeyStore.getInstance().list(str, 1010)));
        arrayList.add(str2);
        ArrayAdapter arrayAdapter = new ArrayAdapter(context, 17367048, (String[]) arrayList.toArray(new String[arrayList.size()]));
        arrayAdapter.setDropDownViewResource(17367049);
        spinner.setAdapter((SpinnerAdapter) arrayAdapter);
        Logger.d("WifiConfigController", "loadCertificates 0004");
    }

    private void setAnonymousIdentInvisible() {
        Logger.d("WifiConfigController", "setAnonymousIdentInvisible 0001");
        this.mView.findViewById(2131361876).setVisibility(8);
        this.mEapAnonymousView.setText("");
        Logger.d("WifiConfigController", "setAnonymousIdentInvisible 0002");
    }

    private void setCaCertInvisible() {
        Logger.d("WifiConfigController", "setCaCertInvisible 0001");
        this.mView.findViewById(2131361866).setVisibility(8);
        setSelection(this.mEapCaCertSpinner, this.mUnspecifiedCertString);
        Logger.d("WifiConfigController", "setCaCertInvisible 0002");
    }

    private void setDomainInvisible() {
        Logger.d("WifiConfigController", "setDomainInvisible 0001");
        this.mView.findViewById(2131361869).setVisibility(8);
        this.mEapDomainView.setText("");
        Logger.d("WifiConfigController", "setDomainInvisible 0002");
    }

    private void setIdentityInvisible() {
        Logger.d("WifiConfigController", "setIdentityInvisible 0001");
        this.mView.findViewById(2131361874).setVisibility(8);
        this.mPhase2Spinner.setSelection(0);
        Logger.d("WifiConfigController", "setIdentityInvisible 0002");
    }

    private void setPasswordInvisible() {
        Logger.d("WifiConfigController", "setPasswordInvisible 0001");
        this.mPasswordView.setText("");
        this.mView.findViewById(2131361878).setVisibility(8);
        this.mView.findViewById(2131361880).setVisibility(8);
        Logger.d("WifiConfigController", "setPasswordInvisible 0002");
    }

    private void setPhase2Invisible() {
        Logger.d("WifiConfigController", "setPhase2Invisible 0001");
        this.mView.findViewById(2131361864).setVisibility(8);
        this.mPhase2Spinner.setSelection(0);
        Logger.d("WifiConfigController", "setPhase2Invisible 0002");
    }

    private void setSelection(Spinner spinner, String str) {
        Logger.d("WifiConfigController", "setSelection 0001");
        if (str != null) {
            Logger.d("WifiConfigController", "setSelection 0002");
            ArrayAdapter arrayAdapter = (ArrayAdapter) spinner.getAdapter();
            int count = arrayAdapter.getCount() - 1;
            while (true) {
                if (count < 0) {
                    break;
                }
                Logger.d("WifiConfigController", "setSelection 0003");
                if (str.equals(arrayAdapter.getItem(count))) {
                    Logger.d("WifiConfigController", "setSelection 0004");
                    spinner.setSelection(count);
                    break;
                }
                count--;
            }
        }
        Logger.d("WifiConfigController", "setSelection 0005");
    }

    private void setUserCertInvisible() {
        Logger.d("WifiConfigController", "setUserCertInvisible 0001");
        this.mView.findViewById(2131361872).setVisibility(8);
        setSelection(this.mEapUserCertSpinner, this.mUnspecifiedCertString);
        Logger.d("WifiConfigController", "setUserCertInvisible 0002");
    }

    private void setVisibility(int i, int i2) {
        View findViewById = this.mView.findViewById(i);
        if (findViewById != null) {
            findViewById.setVisibility(i2);
        }
    }

    private void showEapFieldsByMethod(int i) {
        this.mView.findViewById(2131361862).setVisibility(0);
        this.mView.findViewById(2131361874).setVisibility(0);
        this.mView.findViewById(2131361869).setVisibility(0);
        this.mView.findViewById(2131361866).setVisibility(0);
        this.mView.findViewById(2131361878).setVisibility(0);
        this.mView.findViewById(2131361880).setVisibility(0);
        this.mConfigUi.getContext();
        switch (i) {
            case 0:
                Logger.d("WifiConfigController", "showEapFieldsByMethod 0003");
                if (this.mPhase2Adapter != this.mPhase2PeapAdapter) {
                    Logger.d("WifiConfigController", "showEapFieldsByMethod 0004");
                    this.mPhase2Adapter = this.mPhase2PeapAdapter;
                    this.mPhase2Spinner.setAdapter((SpinnerAdapter) this.mPhase2Adapter);
                }
                this.mView.findViewById(2131361864).setVisibility(0);
                this.mView.findViewById(2131361876).setVisibility(0);
                setUserCertInvisible();
                break;
            case 1:
                Logger.d("WifiConfigController", "showEapFieldsByMethod 0002");
                this.mView.findViewById(2131361872).setVisibility(0);
                setPhase2Invisible();
                setAnonymousIdentInvisible();
                setPasswordInvisible();
                break;
            case 2:
                Logger.d("WifiConfigController", "showEapFieldsByMethod 0005");
                if (this.mPhase2Adapter != this.mPhase2FullAdapter) {
                    Logger.d("WifiConfigController", "showEapFieldsByMethod 0006");
                    this.mPhase2Adapter = this.mPhase2FullAdapter;
                    this.mPhase2Spinner.setAdapter((SpinnerAdapter) this.mPhase2Adapter);
                }
                this.mView.findViewById(2131361864).setVisibility(0);
                this.mView.findViewById(2131361876).setVisibility(0);
                setUserCertInvisible();
                break;
            case 3:
                Logger.d("WifiConfigController", "showEapFieldsByMethod 0001");
                setPhase2Invisible();
                setCaCertInvisible();
                setDomainInvisible();
                setAnonymousIdentInvisible();
                setUserCertInvisible();
                break;
            case 4:
                Logger.d("WifiConfigController", "showEapFieldsByMethod 0007");
            case 5:
                Logger.d("WifiConfigController", "showEapFieldsByMethod 0008");
            case 6:
                Logger.d("WifiConfigController", "showEapFieldsByMethod 0009");
                setPhase2Invisible();
                setAnonymousIdentInvisible();
                setCaCertInvisible();
                setDomainInvisible();
                setUserCertInvisible();
                setPasswordInvisible();
                setIdentityInvisible();
                break;
        }
        if (this.mView.findViewById(2131361866).getVisibility() != 8) {
            String str = (String) this.mEapCaCertSpinner.getSelectedItem();
            Logger.d("WifiConfigController", "showEapFieldsByMethod 0010");
            if (str.equals(this.mDoNotValidateEapServerString) || str.equals(this.mUnspecifiedCertString)) {
                Logger.d("WifiConfigController", "showEapFieldsByMethod 0011");
                setDomainInvisible();
            }
        }
        Logger.d("WifiConfigController", "showEapFieldsByMethod 0012");
    }

    private void showIpConfigFields() {
        Logger.d("WifiConfigController", "showIpConfigFields 0001");
        this.mView.findViewById(2131361895).setVisibility(0);
        WifiConfiguration wifiConfiguration = null;
        if (this.mAccessPoint != null) {
            wifiConfiguration = null;
            if (this.mAccessPoint.isSaved()) {
                Logger.d("WifiConfigController", "showIpConfigFields 0002");
                wifiConfiguration = this.mAccessPoint.getConfig();
            }
        }
        if (this.mIpSettingsSpinner.getSelectedItemPosition() == 1) {
            Logger.d("WifiConfigController", "showIpConfigFields 0003");
            this.mView.findViewById(2131361897).setVisibility(0);
            if (this.mIpAddressView == null) {
                Logger.d("WifiConfigController", "showIpConfigFields 0004");
                this.mIpAddressView = (TextView) this.mView.findViewById(2131361898);
                this.mIpAddressView.addTextChangedListener(this);
                this.mGatewayView = (TextView) this.mView.findViewById(2131361899);
                this.mGatewayView.addTextChangedListener(this);
                this.mNetworkPrefixLengthView = (TextView) this.mView.findViewById(2131361900);
                this.mNetworkPrefixLengthView.addTextChangedListener(this);
                this.mDns1View = (TextView) this.mView.findViewById(2131361901);
                this.mDns1View.addTextChangedListener(this);
                this.mDns2View = (TextView) this.mView.findViewById(2131361902);
                this.mDns2View.addTextChangedListener(this);
            }
            if (wifiConfiguration != null) {
                Logger.d("WifiConfigController", "showIpConfigFields 0005");
                StaticIpConfiguration staticIpConfiguration = wifiConfiguration.getStaticIpConfiguration();
                if (staticIpConfiguration != null) {
                    Logger.d("WifiConfigController", "showIpConfigFields 0006");
                    if (staticIpConfiguration.ipAddress != null) {
                        Logger.d("WifiConfigController", "showIpConfigFields 0007");
                        this.mIpAddressView.setText(staticIpConfiguration.ipAddress.getAddress().getHostAddress());
                        this.mNetworkPrefixLengthView.setText(Integer.toString(staticIpConfiguration.ipAddress.getNetworkPrefixLength()));
                    }
                    if (staticIpConfiguration.gateway != null) {
                        Logger.d("WifiConfigController", "showIpConfigFields 0008");
                        this.mGatewayView.setText(staticIpConfiguration.gateway.getHostAddress());
                    }
                    Iterator it = staticIpConfiguration.dnsServers.iterator();
                    if (it.hasNext()) {
                        Logger.d("WifiConfigController", "showIpConfigFields 0009");
                        this.mDns1View.setText(((InetAddress) it.next()).getHostAddress());
                    }
                    if (it.hasNext()) {
                        Logger.d("WifiConfigController", "showIpConfigFields 0010");
                        this.mDns2View.setText(((InetAddress) it.next()).getHostAddress());
                    }
                }
            }
        } else {
            Logger.d("WifiConfigController", "showIpConfigFields 0011");
            this.mView.findViewById(2131361897).setVisibility(8);
        }
        Logger.d("WifiConfigController", "showIpConfigFields 0012");
    }

    private void showProxyFields() {
        Logger.d("WifiConfigController", "showProxyFields 0001");
        this.mView.findViewById(2131361885).setVisibility(0);
        WifiConfiguration wifiConfiguration = null;
        if (this.mAccessPoint != null) {
            wifiConfiguration = null;
            if (this.mAccessPoint.isSaved()) {
                Logger.d("WifiConfigController", "showProxyFields 0002");
                wifiConfiguration = this.mAccessPoint.getConfig();
            }
        }
        if (this.mProxySettingsSpinner.getSelectedItemPosition() == 1) {
            Logger.d("WifiConfigController", "showProxyFields 0003");
            setVisibility(2131361888, 0);
            setVisibility(2131361891, 0);
            setVisibility(2131361889, 8);
            if (this.mProxyHostView == null) {
                Logger.d("WifiConfigController", "showProxyFields 0004");
                this.mProxyHostView = (TextView) this.mView.findViewById(2131361892);
                this.mProxyHostView.addTextChangedListener(this);
                this.mProxyPortView = (TextView) this.mView.findViewById(2131361893);
                this.mProxyPortView.addTextChangedListener(this);
                this.mProxyExclusionListView = (TextView) this.mView.findViewById(2131361894);
                this.mProxyExclusionListView.addTextChangedListener(this);
            }
            if (wifiConfiguration != null) {
                Logger.d("WifiConfigController", "showProxyFields 0005");
                ProxyInfo httpProxy = wifiConfiguration.getHttpProxy();
                if (httpProxy != null) {
                    Logger.d("WifiConfigController", "showProxyFields 0006");
                    this.mProxyHostView.setText(httpProxy.getHost());
                    this.mProxyPortView.setText(Integer.toString(httpProxy.getPort()));
                    this.mProxyExclusionListView.setText(httpProxy.getExclusionListAsString());
                }
            }
        } else if (this.mProxySettingsSpinner.getSelectedItemPosition() == 2) {
            Logger.d("WifiConfigController", "showProxyFields 0007");
            setVisibility(2131361888, 8);
            setVisibility(2131361891, 8);
            setVisibility(2131361889, 0);
            if (this.mProxyPacView == null) {
                Logger.d("WifiConfigController", "showProxyFields 0008");
                this.mProxyPacView = (TextView) this.mView.findViewById(2131361890);
                this.mProxyPacView.addTextChangedListener(this);
            }
            if (wifiConfiguration != null) {
                Logger.d("WifiConfigController", "showProxyFields 0009");
                ProxyInfo httpProxy2 = wifiConfiguration.getHttpProxy();
                if (httpProxy2 != null) {
                    Logger.d("WifiConfigController", "showProxyFields 0010");
                    this.mProxyPacView.setText(httpProxy2.getPacFileUrl().toString());
                }
            }
        } else {
            Logger.d("WifiConfigController", "showProxyFields 0011");
            setVisibility(2131361888, 8);
            setVisibility(2131361891, 8);
        }
        Logger.d("WifiConfigController", "showProxyFields 0012");
    }

    private void showSecurityFields() {
        Logger.d("WifiConfigController", "showSecurityFields 0001");
        if (this.mAccessPointSecurity == 0) {
            Logger.d("WifiConfigController", "showSecurityFields 0002");
            this.mView.findViewById(2131361860).setVisibility(8);
            return;
        }
        this.mView.findViewById(2131361860).setVisibility(0);
        if (this.mPasswordView == null) {
            Logger.d("WifiConfigController", "showSecurityFields 0003");
            this.mPasswordView = (TextView) this.mView.findViewById(2131361879);
            this.mPasswordView.addTextChangedListener(this);
            this.mPasswordView.setOnEditorActionListener(this);
            this.mPasswordView.setOnKeyListener(this);
            ((CheckBox) this.mView.findViewById(2131361881)).setOnClickListener(this);
            if (this.mAccessPoint != null && this.mAccessPoint.isSaved()) {
                Logger.d("WifiConfigController", "showSecurityFields 0004");
                this.mPasswordView.setHint(2131230845);
            }
        }
        if (this.mAccessPointSecurity != 3) {
            Logger.d("WifiConfigController", "showSecurityFields 0005");
            this.mView.findViewById(2131361861).setVisibility(8);
            return;
        }
        this.mView.findViewById(2131361861).setVisibility(0);
        if (this.mEapMethodSpinner == null) {
            Logger.d("WifiConfigController", "showSecurityFields 0006");
            this.mEapMethodSpinner = (Spinner) this.mView.findViewById(2131361863);
            this.mEapMethodSpinner.setOnItemSelectedListener(this);
            if (isWifiOnly(this.mContext) || !this.mContext.getResources().getBoolean(17957030)) {
                Logger.d("WifiConfigController", "showSecurityFields 0007");
                ArrayAdapter arrayAdapter = new ArrayAdapter(this.mContext, 17367048, this.mContext.getResources().getStringArray(2131034116));
                arrayAdapter.setDropDownViewResource(17367049);
                this.mEapMethodSpinner.setAdapter((SpinnerAdapter) arrayAdapter);
            }
            this.mPhase2Spinner = (Spinner) this.mView.findViewById(2131361865);
            this.mEapCaCertSpinner = (Spinner) this.mView.findViewById(2131361867);
            this.mEapCaCertSpinner.setOnItemSelectedListener(this);
            this.mEapDomainView = (TextView) this.mView.findViewById(2131361870);
            this.mEapDomainView.addTextChangedListener(this);
            this.mEapUserCertSpinner = (Spinner) this.mView.findViewById(2131361873);
            this.mEapUserCertSpinner.setOnItemSelectedListener(this);
            this.mEapIdentityView = (TextView) this.mView.findViewById(2131361875);
            this.mEapAnonymousView = (TextView) this.mView.findViewById(2131361877);
            loadCertificates(this.mEapCaCertSpinner, "CACERT_", this.mDoNotValidateEapServerString, false, true);
            loadCertificates(this.mEapUserCertSpinner, "USRPKEY_", this.mDoNotProvideEapUserCertString, false, false);
            if (this.mAccessPoint == null || !this.mAccessPoint.isSaved()) {
                Logger.d("WifiConfigController", "showSecurityFields 0022");
                showEapFieldsByMethod(this.mEapMethodSpinner.getSelectedItemPosition());
            } else {
                Logger.d("WifiConfigController", "showSecurityFields 0008");
                WifiEnterpriseConfig wifiEnterpriseConfig = this.mAccessPoint.getConfig().enterpriseConfig;
                int eapMethod = wifiEnterpriseConfig.getEapMethod();
                int phase2Method = wifiEnterpriseConfig.getPhase2Method();
                this.mEapMethodSpinner.setSelection(eapMethod);
                switch (eapMethod) {
                    case 0:
                        Logger.d("WifiConfigController", "showSecurityFields 0009");
                        switch (phase2Method) {
                            case 0:
                                Logger.d("WifiConfigController", "showSecurityFields 0010");
                                this.mPhase2Spinner.setSelection(0);
                                break;
                            case 1:
                            case 2:
                            default:
                                Logger.d("WifiConfigController", "showSecurityFields 0013");
                                Logger.e("WifiConfigController", "Invalid phase 2 method " + phase2Method);
                                break;
                            case 3:
                                Logger.d("WifiConfigController", "showSecurityFields 0011");
                                this.mPhase2Spinner.setSelection(1);
                                break;
                            case 4:
                                Logger.d("WifiConfigController", "showSecurityFields 0012");
                                this.mPhase2Spinner.setSelection(2);
                                break;
                        }
                    default:
                        Logger.d("WifiConfigController", "showSecurityFields 0014");
                        this.mPhase2Spinner.setSelection(phase2Method);
                        break;
                }
                if (TextUtils.isEmpty(wifiEnterpriseConfig.getCaPath())) {
                    Logger.d("WifiConfigController", "showSecurityFields 0016");
                    String[] caCertificateAliases = wifiEnterpriseConfig.getCaCertificateAliases();
                    if (caCertificateAliases == null) {
                        Logger.d("WifiConfigController", "showSecurityFields 0017");
                        setSelection(this.mEapCaCertSpinner, this.mDoNotValidateEapServerString);
                    } else if (caCertificateAliases.length == 1) {
                        Logger.d("WifiConfigController", "showSecurityFields 0018");
                        setSelection(this.mEapCaCertSpinner, caCertificateAliases[0]);
                    } else {
                        Logger.d("WifiConfigController", "showSecurityFields 0019");
                        loadCertificates(this.mEapCaCertSpinner, "CACERT_", this.mDoNotValidateEapServerString, true, true);
                        setSelection(this.mEapCaCertSpinner, this.mMultipleCertSetString);
                    }
                } else {
                    Logger.d("WifiConfigController", "showSecurityFields 0015");
                    setSelection(this.mEapCaCertSpinner, this.mUseSystemCertsString);
                }
                this.mEapDomainView.setText(wifiEnterpriseConfig.getDomainSuffixMatch());
                String clientCertificateAlias = wifiEnterpriseConfig.getClientCertificateAlias();
                if (TextUtils.isEmpty(clientCertificateAlias)) {
                    Logger.d("WifiConfigController", "showSecurityFields 0020");
                    setSelection(this.mEapUserCertSpinner, this.mDoNotProvideEapUserCertString);
                } else {
                    Logger.d("WifiConfigController", "showSecurityFields 0021");
                    setSelection(this.mEapUserCertSpinner, clientCertificateAlias);
                }
                this.mEapIdentityView.setText(wifiEnterpriseConfig.getIdentity());
                this.mEapAnonymousView.setText(wifiEnterpriseConfig.getAnonymousIdentity());
            }
        } else {
            Logger.d("WifiConfigController", "showSecurityFields 0023");
            showEapFieldsByMethod(this.mEapMethodSpinner.getSelectedItemPosition());
        }
        Logger.d("WifiConfigController", "showSecurityFields 0024");
    }

    public static int validate(String str, String str2, String str3) {
        Logger.d("WifiConfigController", "validate 0001");
        Matcher matcher = HOSTNAME_PATTERN.matcher(str);
        String[] split = str3.split(",");
        if (!matcher.matches()) {
            Logger.d("WifiConfigController", "validate 0002");
            return 2131230790;
        }
        for (String str4 : split) {
            Logger.d("WifiConfigController", "validate 0003");
            if (!EXCLUSION_PATTERN.matcher(str4).matches()) {
                Logger.d("WifiConfigController", "validate 0004");
                return 2131230791;
            }
        }
        if (str.length() > 0 && str2.length() == 0) {
            Logger.d("WifiConfigController", "validate 0005");
            return 2131230792;
        }
        if (str2.length() > 0) {
            Logger.d("WifiConfigController", "validate 0006");
            if (str.length() == 0) {
                Logger.d("WifiConfigController", "validate 0007");
                return 2131230793;
            }
            try {
                int parseInt = Integer.parseInt(str2);
                if (parseInt <= 0 || parseInt > 65535) {
                    Logger.d("WifiConfigController", "validate 0009");
                    return 2131230794;
                }
            } catch (NumberFormatException e) {
                Logger.d("WifiConfigController", "validate 0008");
                Logger.e("WifiConfigController", "NumberFormatException", e);
                return 2131230794;
            }
        }
        Logger.d("WifiConfigController", "validate 0010");
        return 0;
    }

    private int validateIpConfigFields(StaticIpConfiguration staticIpConfiguration) {
        int parseInt;
        Logger.d("WifiConfigController", "validateIpConfigFields 0001");
        if (this.mIpAddressView == null) {
            Logger.d("WifiConfigController", "validateIpConfigFields 0002");
            return 0;
        }
        String charSequence = this.mIpAddressView.getText().toString();
        if (TextUtils.isEmpty(charSequence)) {
            Logger.d("WifiConfigController", "validateIpConfigFields 0003");
            return 2131230867;
        }
        Inet4Address iPv4Address = getIPv4Address(charSequence);
        if (iPv4Address == null || iPv4Address.isAnyLocalAddress()) {
            Logger.d("WifiConfigController", "validateIpConfigFields 0004");
            return 2131230867;
        }
        int i = -1;
        try {
            Logger.d("WifiConfigController", "validateIpConfigFields 0005");
            parseInt = Integer.parseInt(this.mNetworkPrefixLengthView.getText().toString());
        } catch (NumberFormatException e) {
            Logger.d("WifiConfigController", "validateIpConfigFields 0007");
            Logger.e("WifiConfigController", "NumberFormatException", e);
            this.mNetworkPrefixLengthView.setText(this.mConfigUi.getContext().getString(2131230775));
        }
        if (parseInt < 0 || parseInt > 32) {
            Logger.d("WifiConfigController", "validateIpConfigFields 0006");
            return 2131230870;
        }
        staticIpConfiguration.ipAddress = new LinkAddress(iPv4Address, parseInt);
        i = parseInt;
        String charSequence2 = this.mGatewayView.getText().toString();
        if (TextUtils.isEmpty(charSequence2)) {
            Logger.d("WifiConfigController", "validateIpConfigFields 0008");
            try {
                byte[] address = NetworkUtils.getNetworkPart(iPv4Address, i).getAddress();
                address[address.length - 1] = 1;
                this.mGatewayView.setText(InetAddress.getByAddress(address).getHostAddress());
            } catch (RuntimeException e2) {
                Logger.d("WifiConfigController", "validateIpConfigFields 0009");
                Logger.e("WifiConfigController", "RuntimeException", e2);
            } catch (UnknownHostException e3) {
                Logger.d("WifiConfigController", "validateIpConfigFields 0010");
                Logger.e("WifiConfigController", "UnknownHostException", e3);
            }
        } else {
            Logger.d("WifiConfigController", "validateIpConfigFields 0011");
            Inet4Address iPv4Address2 = getIPv4Address(charSequence2);
            if (iPv4Address2 == null) {
                Logger.d("WifiConfigController", "validateIpConfigFields 0012");
                return 2131230868;
            } else if (iPv4Address2.isMulticastAddress()) {
                Logger.d("WifiConfigController", "validateIpConfigFields 0013");
                return 2131230868;
            } else {
                staticIpConfiguration.gateway = iPv4Address2;
            }
        }
        String charSequence3 = this.mDns1View.getText().toString();
        if (TextUtils.isEmpty(charSequence3)) {
            Logger.d("WifiConfigController", "validateIpConfigFields 0014");
            this.mDns1View.setText(this.mConfigUi.getContext().getString(2131230772));
        } else {
            Logger.d("WifiConfigController", "validateIpConfigFields 0015");
            Inet4Address iPv4Address3 = getIPv4Address(charSequence3);
            if (iPv4Address3 == null) {
                Logger.d("WifiConfigController", "validateIpConfigFields 0016");
                return 2131230869;
            }
            staticIpConfiguration.dnsServers.add(iPv4Address3);
        }
        if (this.mDns2View.length() > 0) {
            Logger.d("WifiConfigController", "validateIpConfigFields 0017");
            Inet4Address iPv4Address4 = getIPv4Address(this.mDns2View.getText().toString());
            if (iPv4Address4 == null) {
                Logger.d("WifiConfigController", "validateIpConfigFields 0018");
                return 2131230869;
            }
            staticIpConfiguration.dnsServers.add(iPv4Address4);
        }
        Logger.d("WifiConfigController", "validateIpConfigFields 0019");
        return 0;
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable editable) {
        Logger.d("WifiConfigController", "afterTextChanged 0001");
        this.mTextViewChangedHandler.post(new Runnable(this) { // from class: jp.co.benesse.dcha.systemsettings.WifiConfigController.1
            final WifiConfigController this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                Logger.d("WifiConfigController", "run 0001");
                this.this$0.setSubmitOrEnable();
            }
        });
        Logger.d("WifiConfigController", "afterTextChanged 0002");
    }

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        Logger.d("WifiConfigController", "beforeTextChanged 0001");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void enableSubmitIfAppropriate() {
        Logger.d("WifiConfigController", "enableSubmitIfAppropriate 0001");
        Button submitButton = this.mConfigUi.getSubmitButton();
        if (submitButton == null) {
            Logger.d("WifiConfigController", "enableSubmitIfAppropriate 0002");
            return;
        }
        submitButton.setEnabled(isSubmittable());
        Logger.d("WifiConfigController", "enableSubmitIfAppropriate 0003");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Code restructure failed: missing block: B:54:0x03c6, code lost:
        if (r0.equals(r8.mDoNotProvideEapUserCertString) != false) goto L70;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public WifiConfiguration getConfig() {
        String str;
        Logger.d("WifiConfigController", "getConfig 0001");
        if (this.mMode == 0) {
            Logger.d("WifiConfigController", "getConfig 0002");
            return null;
        }
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        if (this.mAccessPoint == null) {
            Logger.d("WifiConfigController", "getConfig 0003");
            wifiConfiguration.SSID = AccessPoint.convertToQuotedString(this.mSsidView.getText().toString());
            wifiConfiguration.hiddenSSID = true;
        } else if (this.mAccessPoint.isSaved()) {
            Logger.d("WifiConfigController", "getConfig 0005");
            wifiConfiguration.networkId = this.mAccessPoint.getConfig().networkId;
        } else {
            Logger.d("WifiConfigController", "getConfig 0004");
            wifiConfiguration.SSID = AccessPoint.convertToQuotedString(this.mAccessPoint.getSsidStr());
        }
        wifiConfiguration.shared = this.mSharedCheckBox.isChecked();
        switch (this.mAccessPointSecurity) {
            case 0:
                Logger.d("WifiConfigController", "getConfig 0006");
                wifiConfiguration.allowedKeyManagement.set(0);
                break;
            case 1:
                Logger.d("WifiConfigController", "getConfig 0007");
                wifiConfiguration.allowedKeyManagement.set(0);
                wifiConfiguration.allowedAuthAlgorithms.set(0);
                wifiConfiguration.allowedAuthAlgorithms.set(1);
                if (this.mPasswordView.length() != 0) {
                    Logger.d("WifiConfigController", "getConfig 0008");
                    int length = this.mPasswordView.length();
                    String charSequence = this.mPasswordView.getText().toString();
                    if ((length != 10 && length != 26 && length != 58) || !charSequence.matches("[0-9A-Fa-f]*")) {
                        Logger.d("WifiConfigController", "getConfig 0010");
                        wifiConfiguration.wepKeys[0] = '\"' + charSequence + '\"';
                        break;
                    } else {
                        Logger.d("WifiConfigController", "getConfig 0009");
                        wifiConfiguration.wepKeys[0] = charSequence;
                        break;
                    }
                }
                break;
            case 2:
                Logger.d("WifiConfigController", "getConfig 0011");
                wifiConfiguration.allowedKeyManagement.set(1);
                if (this.mPasswordView.length() != 0) {
                    Logger.d("WifiConfigController", "getConfig 0012");
                    String charSequence2 = this.mPasswordView.getText().toString();
                    if (!charSequence2.matches("[0-9A-Fa-f]{64}")) {
                        Logger.d("WifiConfigController", "getConfig 0014");
                        wifiConfiguration.preSharedKey = '\"' + charSequence2 + '\"';
                        break;
                    } else {
                        Logger.d("WifiConfigController", "getConfig 0013");
                        wifiConfiguration.preSharedKey = charSequence2;
                        break;
                    }
                }
                break;
            case 3:
                Logger.d("WifiConfigController", "getConfig 0015");
                wifiConfiguration.allowedKeyManagement.set(2);
                wifiConfiguration.allowedKeyManagement.set(3);
                wifiConfiguration.enterpriseConfig = new WifiEnterpriseConfig();
                int selectedItemPosition = this.mEapMethodSpinner.getSelectedItemPosition();
                int selectedItemPosition2 = this.mPhase2Spinner.getSelectedItemPosition();
                wifiConfiguration.enterpriseConfig.setEapMethod(selectedItemPosition);
                switch (selectedItemPosition) {
                    case 0:
                        Logger.d("WifiConfigController", "getConfig 0016");
                        switch (selectedItemPosition2) {
                            case 0:
                                Logger.d("WifiConfigController", "getConfig 0017");
                                wifiConfiguration.enterpriseConfig.setPhase2Method(0);
                                break;
                            case 1:
                                Logger.d("WifiConfigController", "getConfig 0018");
                                wifiConfiguration.enterpriseConfig.setPhase2Method(3);
                                break;
                            case 2:
                                Logger.d("WifiConfigController", "getConfig 0019");
                                wifiConfiguration.enterpriseConfig.setPhase2Method(4);
                                break;
                            default:
                                Logger.d("WifiConfigController", "getConfig 0020");
                                Logger.e("WifiConfigController", "Unknown phase2 method" + selectedItemPosition2);
                                break;
                        }
                    default:
                        Logger.d("WifiConfigController", "getConfig 0021");
                        wifiConfiguration.enterpriseConfig.setPhase2Method(selectedItemPosition2);
                        break;
                }
                String str2 = (String) this.mEapCaCertSpinner.getSelectedItem();
                wifiConfiguration.enterpriseConfig.setCaCertificateAliases(null);
                wifiConfiguration.enterpriseConfig.setCaPath(null);
                wifiConfiguration.enterpriseConfig.setDomainSuffixMatch(this.mEapDomainView.getText().toString());
                if (str2.equals(this.mUnspecifiedCertString) || str2.equals(this.mDoNotValidateEapServerString)) {
                    Logger.d("WifiConfigController", "getConfig 0022");
                } else if (str2.equals(this.mUseSystemCertsString)) {
                    Logger.d("WifiConfigController", "getConfig 0023");
                    wifiConfiguration.enterpriseConfig.setCaPath("/system/etc/security/cacerts");
                } else if (str2.equals(this.mMultipleCertSetString)) {
                    Logger.d("WifiConfigController", "getConfig 0024");
                    if (this.mAccessPoint != null) {
                        Logger.d("WifiConfigController", "getConfig 0025");
                        if (!this.mAccessPoint.isSaved()) {
                            Logger.d("WifiConfigController", "getConfig 0026");
                            Logger.e("WifiConfigController", "Multiple certs can only be set when editing saved network");
                        }
                        wifiConfiguration.enterpriseConfig.setCaCertificateAliases(this.mAccessPoint.getConfig().enterpriseConfig.getCaCertificateAliases());
                    }
                } else {
                    Logger.d("WifiConfigController", "getConfig 0027");
                    wifiConfiguration.enterpriseConfig.setCaCertificateAliases(new String[]{str2});
                }
                if (wifiConfiguration.enterpriseConfig.getCaCertificateAliases() != null && wifiConfiguration.enterpriseConfig.getCaPath() != null) {
                    Logger.d("WifiConfigController", "getConfig 0028");
                    Logger.e("WifiConfigController", "ca_cert (" + wifiConfiguration.enterpriseConfig.getCaCertificateAliases() + ") and ca_path (" + wifiConfiguration.enterpriseConfig.getCaPath() + ") should not both be non-null");
                }
                String str3 = (String) this.mEapUserCertSpinner.getSelectedItem();
                if (!str3.equals(this.mUnspecifiedCertString)) {
                    str = str3;
                    break;
                }
                Logger.d("WifiConfigController", "getConfig 0029");
                str = "";
                wifiConfiguration.enterpriseConfig.setClientCertificateAlias(str);
                if (selectedItemPosition == 4 || selectedItemPosition == 5 || selectedItemPosition == 6) {
                    Logger.d("WifiConfigController", "getConfig 0030");
                    wifiConfiguration.enterpriseConfig.setIdentity("");
                    wifiConfiguration.enterpriseConfig.setAnonymousIdentity("");
                } else if (selectedItemPosition == 3) {
                    Logger.d("WifiConfigController", "getConfig 0031");
                    wifiConfiguration.enterpriseConfig.setIdentity(this.mEapIdentityView.getText().toString());
                    wifiConfiguration.enterpriseConfig.setAnonymousIdentity("");
                } else {
                    Logger.d("WifiConfigController", "getConfig 0032");
                    wifiConfiguration.enterpriseConfig.setIdentity(this.mEapIdentityView.getText().toString());
                    wifiConfiguration.enterpriseConfig.setAnonymousIdentity(this.mEapAnonymousView.getText().toString());
                }
                if (!this.mPasswordView.isShown()) {
                    Logger.d("WifiConfigController", "getConfig 0035");
                    wifiConfiguration.enterpriseConfig.setPassword(this.mPasswordView.getText().toString());
                    break;
                } else {
                    Logger.d("WifiConfigController", "getConfig 0033");
                    if (this.mPasswordView.length() > 0) {
                        Logger.d("WifiConfigController", "getConfig 0034");
                        wifiConfiguration.enterpriseConfig.setPassword(this.mPasswordView.getText().toString());
                        break;
                    }
                }
                break;
            default:
                Logger.d("WifiConfigController", "getConfig 0036");
                return null;
        }
        wifiConfiguration.setIpConfiguration(new IpConfiguration(this.mIpAssignment, this.mProxySettings, this.mStaticIpConfiguration, this.mHttpProxy));
        Logger.d("WifiConfigController", "getConfig 0037");
        return wifiConfiguration;
    }

    public int getMode() {
        Logger.d("WifiConfigController", "getMode 0001");
        return this.mMode;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void hideForgetButton() {
        Logger.d("WifiConfigController", "hideForgetButton 0001");
        Button forgetButton = this.mConfigUi.getForgetButton();
        if (forgetButton == null) {
            Logger.d("WifiConfigController", "hideForgetButton 0002");
            return;
        }
        forgetButton.setVisibility(8);
        Logger.d("WifiConfigController", "hideForgetButton 0003");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void hideSubmitButton() {
        Logger.d("WifiConfigController", "hideSubmitButton 0001");
        Button submitButton = this.mConfigUi.getSubmitButton();
        if (submitButton == null) {
            Logger.d("WifiConfigController", "hideSubmitButton 0002");
            return;
        }
        submitButton.setVisibility(8);
        Logger.d("WifiConfigController", "hideSubmitButton 0003");
    }

    /* JADX WARN: Code restructure failed: missing block: B:42:0x0180, code lost:
        if (r6.mPasswordView.length() < 8) goto L7;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    boolean isSubmittable() {
        boolean ipAndProxyFieldsAreValid;
        Logger.d("WifiConfigController", "isSubmittable 0001");
        boolean z = false;
        if (this.mPasswordView != null) {
            if (this.mAccessPointSecurity != 1 || this.mPasswordView.length() != 0) {
                z = false;
                if (this.mAccessPointSecurity == 2) {
                    z = false;
                }
            }
            Logger.d("WifiConfigController", "isSubmittable 0002");
            z = true;
        }
        if ((this.mSsidView == null || this.mSsidView.length() != 0) && ((this.mAccessPoint != null && this.mAccessPoint.isSaved()) || !z)) {
            Logger.d("WifiConfigController", "isSubmittable 0004");
            ipAndProxyFieldsAreValid = ipAndProxyFieldsAreValid();
        } else {
            Logger.d("WifiConfigController", "isSubmittable 0003");
            ipAndProxyFieldsAreValid = false;
        }
        boolean z2 = ipAndProxyFieldsAreValid;
        if (this.mEapCaCertSpinner != null) {
            z2 = ipAndProxyFieldsAreValid;
            if (this.mView.findViewById(2131361866).getVisibility() != 8) {
                Logger.d("WifiConfigController", "isSubmittable 0005");
                String str = (String) this.mEapCaCertSpinner.getSelectedItem();
                if (str.equals(this.mUnspecifiedCertString)) {
                    Logger.d("WifiConfigController", "isSubmittable 0006");
                    ipAndProxyFieldsAreValid = false;
                }
                z2 = ipAndProxyFieldsAreValid;
                if (str.equals(this.mUseSystemCertsString)) {
                    z2 = ipAndProxyFieldsAreValid;
                    if (this.mEapDomainView != null) {
                        z2 = ipAndProxyFieldsAreValid;
                        if (this.mView.findViewById(2131361869).getVisibility() != 8) {
                            z2 = ipAndProxyFieldsAreValid;
                            if (TextUtils.isEmpty(this.mEapDomainView.getText().toString())) {
                                Logger.d("WifiConfigController", "isSubmittable 0007");
                                z2 = false;
                            }
                        }
                    }
                }
            }
        }
        boolean z3 = z2;
        if (this.mEapUserCertSpinner != null) {
            z3 = z2;
            if (this.mView.findViewById(2131361872).getVisibility() != 8) {
                z3 = z2;
                if (((String) this.mEapUserCertSpinner.getSelectedItem()).equals(this.mUnspecifiedCertString)) {
                    Logger.d("WifiConfigController", "isSubmittable 0008");
                    z3 = false;
                }
            }
        }
        Logger.d("WifiConfigController", "isSubmittable 0009");
        return z3;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Logger.d("WifiConfigController", "onClick 0001");
        if (view.getId() == 2131361881) {
            Logger.d("WifiConfigController", "onClick 0002");
            int selectionEnd = this.mPasswordView.getSelectionEnd();
            this.mPasswordView.setInputType((((CheckBox) view).isChecked() ? 144 : 128) | 1);
            if (selectionEnd >= 0) {
                Logger.d("WifiConfigController", "onClick 0003");
                ((EditText) this.mPasswordView).setSelection(selectionEnd);
            }
        } else if (view.getId() == 2131361883) {
            Logger.d("WifiConfigController", "onClick 0004");
            if (((CheckBox) view).isChecked()) {
                Logger.d("WifiConfigController", "onClick 0005");
                this.mView.findViewById(2131361884).setVisibility(0);
            } else {
                Logger.d("WifiConfigController", "onClick 0006");
                this.mView.findViewById(2131361884).setVisibility(8);
            }
        }
        Logger.d("WifiConfigController", "onClick 0007");
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        Logger.d("WifiConfigController", "onEditorAction 0001");
        if (textView == this.mPasswordView) {
            Logger.d("WifiConfigController", "onEditorAction 0002");
            if (i == 6 && isSubmittable()) {
                Logger.d("WifiConfigController", "onEditorAction 0003");
                this.mConfigUi.dispatchSubmit();
                return true;
            }
        }
        Logger.d("WifiConfigController", "onEditorAction 0004");
        return false;
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
        Logger.d("WifiConfigController", "onItemSelected 0001");
        if (adapterView == this.mSecuritySpinner) {
            Logger.d("WifiConfigController", "onItemSelected 0002");
            this.mAccessPointSecurity = i;
            showSecurityFields();
        } else if (adapterView == this.mEapMethodSpinner || adapterView == this.mEapCaCertSpinner) {
            Logger.d("WifiConfigController", "onItemSelected 0003");
            showSecurityFields();
        } else if (adapterView == this.mProxySettingsSpinner) {
            Logger.d("WifiConfigController", "onItemSelected 0004");
            showProxyFields();
        } else {
            Logger.d("WifiConfigController", "onItemSelected 0005");
            showIpConfigFields();
        }
        showWarningMessagesIfAppropriate();
        enableSubmitIfAppropriate();
        setSubmitOrEnable();
        Logger.d("WifiConfigController", "onItemSelected 0006");
    }

    @Override // android.view.View.OnKeyListener
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        Logger.d("WifiConfigController", "onKey 0001");
        if (view == this.mPasswordView) {
            Logger.d("WifiConfigController", "onKey 0002");
            if (i == 66 && isSubmittable()) {
                Logger.d("WifiConfigController", "onKey 0003");
                this.mConfigUi.dispatchSubmit();
                return true;
            }
        }
        Logger.d("WifiConfigController", "onKey 0004");
        return false;
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onNothingSelected(AdapterView<?> adapterView) {
        Logger.d("WifiConfigController", "onNothingSelected 0001");
    }

    @Override // android.text.TextWatcher
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        Logger.d("WifiConfigController", "onTextChanged 0001");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Code restructure failed: missing block: B:23:0x00ae, code lost:
        if (r6.mPasswordView.length() < 8) goto L12;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void setSubmitOrEnable() {
        boolean z;
        Logger.d("WifiConfigController", "setSubmitOrEnable 0001");
        Button submitButton = this.mConfigUi.getSubmitButton();
        if (submitButton == null) {
            Logger.d("WifiConfigController", "setSubmitOrEnable 0002");
            return;
        }
        boolean z2 = false;
        if (this.mPasswordView != null) {
            if (this.mAccessPointSecurity != 1 || this.mPasswordView.length() != 0) {
                z2 = false;
                if (this.mAccessPointSecurity == 2) {
                    z2 = false;
                }
            }
            Logger.d("WifiConfigController", "setSubmitOrEnable 0003");
            z2 = true;
        }
        if ((this.mSsidView == null || this.mSsidView.length() != 0) && ((this.mAccessPoint != null && this.mAccessPoint.isSaved()) || !z2)) {
            Logger.d("WifiConfigController", "setSubmitOrEnable 0005");
            if (ipAndProxyFieldsAreValid()) {
                Logger.d("WifiConfigController", "setSubmitOrEnable 0006");
                z = true;
            } else {
                Logger.d("WifiConfigController", "setSubmitOrEnable 0007");
                z = false;
            }
        } else {
            Logger.d("WifiConfigController", "setSubmitOrEnable 0004");
            z = false;
        }
        submitButton.setEnabled(z);
        Logger.d("WifiConfigController", "setSubmitOrEnable 0008");
    }

    void showWarningMessagesIfAppropriate() {
        Logger.d("WifiConfigController", "showWarningMessagesIfAppropriate 0001");
        this.mView.findViewById(2131361868).setVisibility(8);
        this.mView.findViewById(2131361871).setVisibility(8);
        if (this.mEapCaCertSpinner != null && this.mView.findViewById(2131361866).getVisibility() != 8) {
            Logger.d("WifiConfigController", "showWarningMessagesIfAppropriate 0002");
            String str = (String) this.mEapCaCertSpinner.getSelectedItem();
            if (str.equals(this.mDoNotValidateEapServerString)) {
                Logger.d("WifiConfigController", "showWarningMessagesIfAppropriate 0003");
                this.mView.findViewById(2131361868).setVisibility(0);
            }
            if (str.equals(this.mUseSystemCertsString) && this.mEapDomainView != null && this.mView.findViewById(2131361869).getVisibility() != 8 && TextUtils.isEmpty(this.mEapDomainView.getText().toString())) {
                Logger.d("WifiConfigController", "showWarningMessagesIfAppropriate 0004");
                this.mView.findViewById(2131361871).setVisibility(0);
            }
        }
        Logger.d("WifiConfigController", "showWarningMessagesIfAppropriate 0005");
    }

    public void updatePassword() {
        Logger.d("WifiConfigController", "updatePassword 0001");
        ((TextView) this.mView.findViewById(2131361879)).setInputType((((CheckBox) this.mView.findViewById(2131361881)).isChecked() ? 144 : 128) | 1);
        Logger.d("WifiConfigController", "updatePassword 0002");
    }
}
