package com.android.settings.security.trustagent;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedSwitchPreference;
import java.util.List;
/* loaded from: classes.dex */
public class TrustAgentSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private final ArraySet<ComponentName> mActiveAgents = new ArraySet<>();
    private ArrayMap<ComponentName, AgentInfo> mAvailableAgents;
    private DevicePolicyManager mDpm;
    private LockPatternUtils mLockPatternUtils;
    private TrustAgentManager mTrustAgentManager;

    /* loaded from: classes.dex */
    public static final class AgentInfo {
        ComponentName component;
        public Drawable icon;
        CharSequence label;
        SwitchPreference preference;

        public boolean equals(Object obj) {
            if (obj instanceof AgentInfo) {
                return this.component.equals(((AgentInfo) obj).component);
            }
            return true;
        }
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 91;
    }

    @Override // com.android.settings.support.actionbar.HelpResourceProvider
    public int getHelpResource() {
        return R.string.help_url_trust_agent;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mDpm = (DevicePolicyManager) getActivity().getSystemService(DevicePolicyManager.class);
        this.mTrustAgentManager = FeatureFactory.getFactory(getActivity()).getSecurityFeatureProvider().getTrustAgentManager();
        addPreferencesFromResource(R.xml.trust_agent_settings);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        removePreference("dummy_preference");
        updateAgents();
    }

    private void updateAgents() {
        Activity activity = getActivity();
        if (this.mAvailableAgents == null) {
            this.mAvailableAgents = findAvailableTrustAgents();
        }
        if (this.mLockPatternUtils == null) {
            this.mLockPatternUtils = new LockPatternUtils(getActivity());
        }
        loadActiveAgents();
        PreferenceGroup preferenceGroup = (PreferenceGroup) getPreferenceScreen().findPreference("trust_agents");
        preferenceGroup.removeAll();
        RestrictedLockUtils.EnforcedAdmin checkIfKeyguardFeaturesDisabled = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(activity, 16, UserHandle.myUserId());
        int size = this.mAvailableAgents.size();
        for (int i = 0; i < size; i++) {
            AgentInfo valueAt = this.mAvailableAgents.valueAt(i);
            RestrictedSwitchPreference restrictedSwitchPreference = new RestrictedSwitchPreference(getPrefContext());
            restrictedSwitchPreference.useAdminDisabledSummary(true);
            valueAt.preference = restrictedSwitchPreference;
            restrictedSwitchPreference.setPersistent(false);
            restrictedSwitchPreference.setTitle(valueAt.label);
            restrictedSwitchPreference.setIcon(valueAt.icon);
            restrictedSwitchPreference.setPersistent(false);
            restrictedSwitchPreference.setOnPreferenceChangeListener(this);
            restrictedSwitchPreference.setChecked(this.mActiveAgents.contains(valueAt.component));
            if (checkIfKeyguardFeaturesDisabled != null && this.mDpm.getTrustAgentConfiguration(null, valueAt.component) == null) {
                restrictedSwitchPreference.setChecked(false);
                restrictedSwitchPreference.setDisabledByAdmin(checkIfKeyguardFeaturesDisabled);
            }
            preferenceGroup.addPreference(valueAt.preference);
        }
    }

    private void loadActiveAgents() {
        List enabledTrustAgents = this.mLockPatternUtils.getEnabledTrustAgents(UserHandle.myUserId());
        if (enabledTrustAgents != null) {
            this.mActiveAgents.addAll(enabledTrustAgents);
        }
    }

    private void saveActiveAgents() {
        this.mLockPatternUtils.setEnabledTrustAgents(this.mActiveAgents, UserHandle.myUserId());
    }

    private ArrayMap<ComponentName, AgentInfo> findAvailableTrustAgents() {
        PackageManager packageManager = getActivity().getPackageManager();
        List<ResolveInfo> queryIntentServices = packageManager.queryIntentServices(new Intent("android.service.trust.TrustAgentService"), 128);
        ArrayMap<ComponentName, AgentInfo> arrayMap = new ArrayMap<>();
        int size = queryIntentServices.size();
        arrayMap.ensureCapacity(size);
        for (int i = 0; i < size; i++) {
            ResolveInfo resolveInfo = queryIntentServices.get(i);
            if (resolveInfo.serviceInfo != null && this.mTrustAgentManager.shouldProvideTrust(resolveInfo, packageManager)) {
                ComponentName componentName = this.mTrustAgentManager.getComponentName(resolveInfo);
                AgentInfo agentInfo = new AgentInfo();
                agentInfo.label = resolveInfo.loadLabel(packageManager);
                agentInfo.icon = resolveInfo.loadIcon(packageManager);
                agentInfo.component = componentName;
                arrayMap.put(componentName, agentInfo);
            }
        }
        return arrayMap;
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        if (preference instanceof SwitchPreference) {
            int size = this.mAvailableAgents.size();
            for (int i = 0; i < size; i++) {
                AgentInfo valueAt = this.mAvailableAgents.valueAt(i);
                if (valueAt.preference == preference) {
                    if (((Boolean) obj).booleanValue()) {
                        if (!this.mActiveAgents.contains(valueAt.component)) {
                            this.mActiveAgents.add(valueAt.component);
                        }
                    } else {
                        this.mActiveAgents.remove(valueAt.component);
                    }
                    saveActiveAgents();
                    return true;
                }
            }
        }
        return false;
    }
}
