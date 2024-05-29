package com.android.settings.applications;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.applications.AppStateAppOpsBridge;
import com.android.settings.applications.AppStateOverlayBridge;
import com.android.settingslib.applications.ApplicationsState;
/* loaded from: classes.dex */
public class DrawOverlayDetails extends AppInfoWithHeader implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static final int[] APP_OPS_OP_CODE = {24};
    private AppOpsManager mAppOpsManager;
    private AppStateOverlayBridge mOverlayBridge;
    private Preference mOverlayDesc;
    private Preference mOverlayPrefs;
    private AppStateOverlayBridge.OverlayState mOverlayState;
    private Intent mSettingsIntent;
    private SwitchPreference mSwitchPref;

    @Override // com.android.settings.applications.AppInfoBase, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        this.mOverlayBridge = new AppStateOverlayBridge(context, this.mState, null);
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        addPreferencesFromResource(R.xml.app_ops_permissions_details);
        this.mSwitchPref = (SwitchPreference) findPreference("app_ops_settings_switch");
        this.mOverlayPrefs = findPreference("app_ops_settings_preference");
        this.mOverlayDesc = findPreference("app_ops_settings_description");
        getPreferenceScreen().setTitle(R.string.draw_overlay);
        this.mSwitchPref.setTitle(R.string.permit_draw_overlay);
        this.mOverlayPrefs.setTitle(R.string.app_overlay_permission_preference);
        this.mOverlayDesc.setSummary(R.string.allow_overlay_description);
        this.mSwitchPref.setOnPreferenceChangeListener(this);
        this.mOverlayPrefs.setOnPreferenceClickListener(this);
        this.mSettingsIntent = new Intent("android.intent.action.MAIN").setAction("android.settings.action.MANAGE_OVERLAY_PERMISSION");
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
    public boolean onPreferenceClick(Preference preference) {
        if (preference == this.mOverlayPrefs) {
            if (this.mSettingsIntent != null) {
                try {
                    getActivity().startActivityAsUser(this.mSettingsIntent, new UserHandle(this.mUserId));
                    return true;
                } catch (ActivityNotFoundException e) {
                    Log.w("DrawOverlayDetails", "Unable to launch app draw overlay settings " + this.mSettingsIntent, e);
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mSwitchPref) {
            if (this.mOverlayState != null && ((Boolean) newValue).booleanValue() != this.mOverlayState.isPermissible()) {
                setCanDrawOverlay(this.mOverlayState.isPermissible() ? false : true);
                refreshUi();
            }
            return true;
        }
        return false;
    }

    private void setCanDrawOverlay(boolean newState) {
        this.mAppOpsManager.setMode(24, this.mPackageInfo.applicationInfo.uid, this.mPackageName, newState ? 0 : 2);
    }

    @Override // com.android.settings.applications.AppInfoBase
    protected boolean refreshUi() {
        this.mOverlayState = this.mOverlayBridge.getOverlayInfo(this.mPackageName, this.mPackageInfo.applicationInfo.uid);
        boolean isAllowed = this.mOverlayState.isPermissible();
        this.mSwitchPref.setChecked(isAllowed);
        this.mSwitchPref.setEnabled(this.mOverlayState.permissionDeclared);
        this.mOverlayPrefs.setEnabled(isAllowed);
        getPreferenceScreen().removePreference(this.mOverlayPrefs);
        return true;
    }

    @Override // com.android.settings.applications.AppInfoBase
    protected AlertDialog createDialog(int id, int errorCode) {
        return null;
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 221;
    }

    public static CharSequence getSummary(Context context, ApplicationsState.AppEntry entry) {
        AppStateOverlayBridge.OverlayState state;
        if (entry.extraInfo instanceof AppStateOverlayBridge.OverlayState) {
            state = (AppStateOverlayBridge.OverlayState) entry.extraInfo;
        } else if (entry.extraInfo instanceof AppStateAppOpsBridge.PermissionState) {
            state = new AppStateOverlayBridge.OverlayState((AppStateAppOpsBridge.PermissionState) entry.extraInfo);
        } else {
            state = new AppStateOverlayBridge(context, null, null).getOverlayInfo(entry.info.packageName, entry.info.uid);
        }
        return getSummary(context, state);
    }

    public static CharSequence getSummary(Context context, AppStateOverlayBridge.OverlayState overlayState) {
        return context.getString(overlayState.isPermissible() ? R.string.system_alert_window_on : R.string.system_alert_window_off);
    }
}
