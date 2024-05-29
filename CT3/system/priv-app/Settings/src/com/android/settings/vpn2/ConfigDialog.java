package com.android.settings.vpn2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.security.KeyStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.android.internal.net.VpnProfile;
import com.android.settings.R;
import java.net.InetAddress;
/* loaded from: classes.dex */
class ConfigDialog extends AlertDialog implements TextWatcher, View.OnClickListener, AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {
    private CheckBox mAlwaysOnVpn;
    private TextView mDnsServers;
    private boolean mEditing;
    private boolean mExists;
    private Spinner mIpsecCaCert;
    private TextView mIpsecIdentifier;
    private TextView mIpsecSecret;
    private Spinner mIpsecServerCert;
    private Spinner mIpsecUserCert;
    private final KeyStore mKeyStore;
    private TextView mL2tpSecret;
    private final DialogInterface.OnClickListener mListener;
    private CheckBox mMppe;
    private TextView mName;
    private TextView mPassword;
    private final VpnProfile mProfile;
    private TextView mRoutes;
    private CheckBox mSaveLogin;
    private TextView mSearchDomains;
    private TextView mServer;
    private CheckBox mShowOptions;
    private Spinner mType;
    private TextView mUsername;
    private View mView;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ConfigDialog(Context context, DialogInterface.OnClickListener listener, VpnProfile profile, boolean editing, boolean exists) {
        super(context);
        this.mKeyStore = KeyStore.getInstance();
        this.mListener = listener;
        this.mProfile = profile;
        this.mEditing = editing;
        this.mExists = exists;
    }

    @Override // android.app.AlertDialog, android.app.Dialog
    protected void onCreate(Bundle savedState) {
        this.mView = getLayoutInflater().inflate(R.layout.vpn_dialog, (ViewGroup) null);
        setView(this.mView);
        Context context = getContext();
        this.mName = (TextView) this.mView.findViewById(R.id.name);
        this.mType = (Spinner) this.mView.findViewById(R.id.type);
        this.mServer = (TextView) this.mView.findViewById(R.id.server);
        this.mUsername = (TextView) this.mView.findViewById(R.id.username);
        this.mPassword = (TextView) this.mView.findViewById(R.id.password);
        this.mSearchDomains = (TextView) this.mView.findViewById(R.id.search_domains);
        this.mDnsServers = (TextView) this.mView.findViewById(R.id.dns_servers);
        this.mRoutes = (TextView) this.mView.findViewById(R.id.routes);
        this.mMppe = (CheckBox) this.mView.findViewById(R.id.mppe);
        this.mL2tpSecret = (TextView) this.mView.findViewById(R.id.l2tp_secret);
        this.mIpsecIdentifier = (TextView) this.mView.findViewById(R.id.ipsec_identifier);
        this.mIpsecSecret = (TextView) this.mView.findViewById(R.id.ipsec_secret);
        this.mIpsecUserCert = (Spinner) this.mView.findViewById(R.id.ipsec_user_cert);
        this.mIpsecCaCert = (Spinner) this.mView.findViewById(R.id.ipsec_ca_cert);
        this.mIpsecServerCert = (Spinner) this.mView.findViewById(R.id.ipsec_server_cert);
        this.mSaveLogin = (CheckBox) this.mView.findViewById(R.id.save_login);
        this.mShowOptions = (CheckBox) this.mView.findViewById(R.id.show_options);
        this.mAlwaysOnVpn = (CheckBox) this.mView.findViewById(R.id.always_on_vpn);
        this.mName.setText(this.mProfile.name);
        this.mType.setSelection(this.mProfile.type);
        this.mServer.setText(this.mProfile.server);
        if (this.mProfile.saveLogin) {
            this.mUsername.setText(this.mProfile.username);
            this.mPassword.setText(this.mProfile.password);
        }
        this.mSearchDomains.setText(this.mProfile.searchDomains);
        this.mDnsServers.setText(this.mProfile.dnsServers);
        this.mRoutes.setText(this.mProfile.routes);
        this.mMppe.setChecked(this.mProfile.mppe);
        this.mL2tpSecret.setText(this.mProfile.l2tpSecret);
        this.mIpsecIdentifier.setText(this.mProfile.ipsecIdentifier);
        this.mIpsecSecret.setText(this.mProfile.ipsecSecret);
        loadCertificates(this.mIpsecUserCert, "USRPKEY_", 0, this.mProfile.ipsecUserCert);
        loadCertificates(this.mIpsecCaCert, "CACERT_", R.string.vpn_no_ca_cert, this.mProfile.ipsecCaCert);
        loadCertificates(this.mIpsecServerCert, "USRCERT_", R.string.vpn_no_server_cert, this.mProfile.ipsecServerCert);
        this.mSaveLogin.setChecked(this.mProfile.saveLogin);
        this.mAlwaysOnVpn.setChecked(this.mProfile.key.equals(VpnUtils.getLockdownVpn()));
        this.mAlwaysOnVpn.setOnCheckedChangeListener(this);
        updateSaveLoginStatus();
        if (SystemProperties.getBoolean("persist.radio.imsregrequired", false)) {
            this.mAlwaysOnVpn.setVisibility(8);
        }
        this.mName.addTextChangedListener(this);
        this.mType.setOnItemSelectedListener(this);
        this.mServer.addTextChangedListener(this);
        this.mUsername.addTextChangedListener(this);
        this.mPassword.addTextChangedListener(this);
        this.mDnsServers.addTextChangedListener(this);
        this.mRoutes.addTextChangedListener(this);
        this.mIpsecSecret.addTextChangedListener(this);
        this.mIpsecUserCert.setOnItemSelectedListener(this);
        this.mShowOptions.setOnClickListener(this);
        boolean valid = validate(true);
        this.mEditing = this.mEditing || !valid;
        if (this.mEditing) {
            setTitle(R.string.vpn_edit);
            this.mView.findViewById(R.id.editor).setVisibility(0);
            changeType(this.mProfile.type);
            this.mSaveLogin.setVisibility(8);
            if (!this.mProfile.searchDomains.isEmpty() || !this.mProfile.dnsServers.isEmpty() || !this.mProfile.routes.isEmpty()) {
                showAdvancedOptions();
            }
            if (this.mExists) {
                setButton(-3, context.getString(R.string.vpn_forget), this.mListener);
            }
            setButton(-1, context.getString(R.string.vpn_save), this.mListener);
        } else {
            setTitle(context.getString(R.string.vpn_connect_to, this.mProfile.name));
            setButton(-1, context.getString(R.string.vpn_connect), this.mListener);
        }
        setButton(-2, context.getString(R.string.vpn_cancel), this.mListener);
        super.onCreate(savedState);
        Button button = getButton(-1);
        if (!this.mEditing) {
            valid = validate(false);
        }
        button.setEnabled(valid);
        getWindow().setSoftInputMode(20);
    }

    @Override // android.app.Dialog
    public void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        if (!this.mShowOptions.isChecked()) {
            return;
        }
        showAdvancedOptions();
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable field) {
        getButton(-1).setEnabled(validate(this.mEditing));
    }

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override // android.text.TextWatcher
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view != this.mShowOptions) {
            return;
        }
        showAdvancedOptions();
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == this.mType) {
            changeType(position);
        }
        getButton(-1).setEnabled(validate(this.mEditing));
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override // android.widget.CompoundButton.OnCheckedChangeListener
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton != this.mAlwaysOnVpn) {
            return;
        }
        updateSaveLoginStatus();
        getButton(-1).setEnabled(validate(this.mEditing));
    }

    public boolean isVpnAlwaysOn() {
        return this.mAlwaysOnVpn.isChecked();
    }

    private void updateSaveLoginStatus() {
        if (this.mAlwaysOnVpn.isChecked()) {
            this.mSaveLogin.setChecked(true);
            this.mSaveLogin.setEnabled(false);
            return;
        }
        this.mSaveLogin.setChecked(this.mProfile.saveLogin);
        this.mSaveLogin.setEnabled(true);
    }

    private void showAdvancedOptions() {
        this.mView.findViewById(R.id.options).setVisibility(0);
        this.mShowOptions.setVisibility(8);
    }

    private void changeType(int type) {
        this.mMppe.setVisibility(8);
        this.mView.findViewById(R.id.l2tp).setVisibility(8);
        this.mView.findViewById(R.id.ipsec_psk).setVisibility(8);
        this.mView.findViewById(R.id.ipsec_user).setVisibility(8);
        this.mView.findViewById(R.id.ipsec_peer).setVisibility(8);
        switch (type) {
            case 0:
                this.mMppe.setVisibility(0);
                return;
            case 1:
                this.mView.findViewById(R.id.l2tp).setVisibility(0);
                this.mView.findViewById(R.id.ipsec_psk).setVisibility(0);
                return;
            case 2:
                this.mView.findViewById(R.id.l2tp).setVisibility(0);
                this.mView.findViewById(R.id.ipsec_user).setVisibility(0);
                this.mView.findViewById(R.id.ipsec_peer).setVisibility(0);
                return;
            case 3:
                this.mView.findViewById(R.id.ipsec_psk).setVisibility(0);
                return;
            case 4:
                this.mView.findViewById(R.id.ipsec_user).setVisibility(0);
                this.mView.findViewById(R.id.ipsec_peer).setVisibility(0);
                return;
            case 5:
                this.mView.findViewById(R.id.ipsec_peer).setVisibility(0);
                return;
            default:
                return;
        }
    }

    private boolean validate(boolean editing) {
        if (!editing) {
            return (this.mUsername.getText().length() == 0 || this.mPassword.getText().length() == 0) ? false : true;
        } else if ((!this.mAlwaysOnVpn.isChecked() || getProfile().isValidLockdownProfile()) && this.mName.getText().length() != 0 && this.mServer.getText().length() != 0 && validateAddresses(this.mDnsServers.getText().toString(), false) && validateAddresses(this.mRoutes.getText().toString(), true)) {
            switch (this.mType.getSelectedItemPosition()) {
                case 0:
                case 5:
                    return true;
                case 1:
                case 3:
                    return this.mIpsecSecret.getText().length() != 0;
                case 2:
                case 4:
                    return this.mIpsecUserCert.getSelectedItemPosition() != 0;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    private boolean validateAddresses(String addresses, boolean cidr) {
        String[] split;
        try {
            for (String address : addresses.split(" ")) {
                if (!address.isEmpty()) {
                    int prefixLength = 32;
                    if (cidr) {
                        String[] parts = address.split("/", 2);
                        address = parts[0];
                        prefixLength = Integer.parseInt(parts[1]);
                    }
                    byte[] bytes = InetAddress.parseNumericAddress(address).getAddress();
                    int integer = (bytes[3] & 255) | ((bytes[2] & 255) << 8) | ((bytes[1] & 255) << 16) | ((bytes[0] & 255) << 24);
                    if (bytes.length == 4 && prefixLength >= 0 && prefixLength <= 32) {
                        if (prefixLength < 32 && (integer << prefixLength) != 0) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void loadCertificates(Spinner spinner, String prefix, int firstId, String selected) {
        String[] certificates;
        Context context = getContext();
        String first = firstId == 0 ? "" : context.getString(firstId);
        String[] certificates2 = this.mKeyStore.list(prefix);
        if (certificates2 == null || certificates2.length == 0) {
            certificates = new String[]{first};
        } else {
            String[] array = new String[certificates2.length + 1];
            array[0] = first;
            System.arraycopy(certificates2, 0, array, 1, certificates2.length);
            certificates = array;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, 17367048, certificates);
        adapter.setDropDownViewResource(17367049);
        spinner.setAdapter((SpinnerAdapter) adapter);
        for (int i = 1; i < certificates.length; i++) {
            if (certificates[i].equals(selected)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isEditing() {
        return this.mEditing;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:21:0x00dd  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x00ef  */
    /* JADX WARN: Removed duplicated region for block: B:27:0x0101  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public VpnProfile getProfile() {
        VpnProfile profile = new VpnProfile(this.mProfile.key);
        profile.name = this.mName.getText().toString();
        profile.type = this.mType.getSelectedItemPosition();
        profile.server = this.mServer.getText().toString().trim();
        profile.username = this.mUsername.getText().toString();
        profile.password = this.mPassword.getText().toString();
        profile.searchDomains = this.mSearchDomains.getText().toString().trim();
        profile.dnsServers = this.mDnsServers.getText().toString().trim();
        profile.routes = this.mRoutes.getText().toString().trim();
        switch (profile.type) {
            case 0:
                profile.mppe = this.mMppe.isChecked();
                break;
            case 1:
                profile.l2tpSecret = this.mL2tpSecret.getText().toString();
                profile.ipsecIdentifier = this.mIpsecIdentifier.getText().toString();
                profile.ipsecSecret = this.mIpsecSecret.getText().toString();
                break;
            case 2:
                profile.l2tpSecret = this.mL2tpSecret.getText().toString();
                if (this.mIpsecUserCert.getSelectedItemPosition() != 0) {
                    profile.ipsecUserCert = (String) this.mIpsecUserCert.getSelectedItem();
                }
                if (this.mIpsecCaCert.getSelectedItemPosition() != 0) {
                    profile.ipsecCaCert = (String) this.mIpsecCaCert.getSelectedItem();
                }
                if (this.mIpsecServerCert.getSelectedItemPosition() != 0) {
                    profile.ipsecServerCert = (String) this.mIpsecServerCert.getSelectedItem();
                    break;
                }
                break;
            case 3:
                profile.ipsecIdentifier = this.mIpsecIdentifier.getText().toString();
                profile.ipsecSecret = this.mIpsecSecret.getText().toString();
                break;
            case 4:
                if (this.mIpsecUserCert.getSelectedItemPosition() != 0) {
                }
                if (this.mIpsecCaCert.getSelectedItemPosition() != 0) {
                }
                if (this.mIpsecServerCert.getSelectedItemPosition() != 0) {
                }
                break;
            case 5:
                if (this.mIpsecCaCert.getSelectedItemPosition() != 0) {
                }
                if (this.mIpsecServerCert.getSelectedItemPosition() != 0) {
                }
                break;
        }
        boolean hasLogin = (profile.username.isEmpty() && profile.password.isEmpty()) ? false : true;
        if (this.mSaveLogin.isChecked()) {
            hasLogin = true;
        } else if (!this.mEditing) {
            hasLogin = false;
        }
        profile.saveLogin = hasLogin;
        return profile;
    }
}
