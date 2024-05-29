package com.android.settings.notification;

import android.app.AlertDialog;
import android.app.AutomaticZenRule;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.service.notification.ZenModeConfig;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.View;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.utils.ManagedServiceSettings;
import com.android.settings.utils.ZenServiceListing;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
/* loaded from: classes.dex */
public class ZenModeAutomationSettings extends ZenModeSettingsBase {
    static final ManagedServiceSettings.Config CONFIG = getConditionProviderConfig();
    private static final Comparator<Map.Entry<String, AutomaticZenRule>> RULE_COMPARATOR = new Comparator<Map.Entry<String, AutomaticZenRule>>() { // from class: com.android.settings.notification.ZenModeAutomationSettings.1
        @Override // java.util.Comparator
        public int compare(Map.Entry<String, AutomaticZenRule> lhs, Map.Entry<String, AutomaticZenRule> rhs) {
            int byDate = Long.compare(lhs.getValue().getCreationTime(), rhs.getValue().getCreationTime());
            if (byDate != 0) {
                return byDate;
            }
            return key(lhs.getValue()).compareTo(key(rhs.getValue()));
        }

        private String key(AutomaticZenRule rule) {
            int type;
            if (ZenModeConfig.isValidScheduleConditionId(rule.getConditionId())) {
                type = 1;
            } else {
                type = ZenModeConfig.isValidEventConditionId(rule.getConditionId()) ? 2 : 3;
            }
            return type + rule.getName().toString();
        }
    };
    private PackageManager mPm;
    private ZenServiceListing mServiceListing;

    @Override // com.android.settings.notification.ZenModeSettingsBase, com.android.settings.RestrictedSettingsFragment, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.zen_mode_automation_settings);
        this.mPm = this.mContext.getPackageManager();
        this.mServiceListing = new ZenServiceListing(this.mContext, CONFIG);
        this.mServiceListing.reloadApprovedServices();
    }

    @Override // com.android.settings.RestrictedSettingsFragment, android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
    }

    @Override // com.android.settings.notification.ZenModeSettingsBase
    protected void onZenModeChanged() {
    }

    @Override // com.android.settings.notification.ZenModeSettingsBase
    protected void onZenModeConfigChanged() {
        updateControls();
    }

    @Override // com.android.settings.notification.ZenModeSettingsBase, com.android.settings.RestrictedSettingsFragment, com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        if (isUiRestricted()) {
            return;
        }
        updateControls();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showAddRuleDialog() {
        new ZenRuleSelectionDialog(this.mContext, this.mServiceListing) { // from class: com.android.settings.notification.ZenModeAutomationSettings.2
            @Override // com.android.settings.notification.ZenRuleSelectionDialog
            public void onSystemRuleSelected(ZenRuleInfo ri) {
                ZenModeAutomationSettings.this.showNameRuleDialog(ri);
            }

            @Override // com.android.settings.notification.ZenRuleSelectionDialog
            public void onExternalRuleSelected(ZenRuleInfo ri) {
                Intent intent = new Intent().setComponent(ri.configurationActivity);
                ZenModeAutomationSettings.this.startActivity(intent);
            }
        }.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showNameRuleDialog(final ZenRuleInfo ri) {
        new ZenRuleNameDialog(this.mContext, null) { // from class: com.android.settings.notification.ZenModeAutomationSettings.3
            @Override // com.android.settings.notification.ZenRuleNameDialog
            public void onOk(String ruleName) {
                MetricsLogger.action(ZenModeAutomationSettings.this.mContext, 173);
                AutomaticZenRule rule = new AutomaticZenRule(ruleName, ri.serviceComponent, ri.defaultConditionId, 2, true);
                String savedRuleId = ZenModeAutomationSettings.this.addZenRule(rule);
                if (savedRuleId == null) {
                    return;
                }
                ZenModeAutomationSettings.this.startActivity(ZenModeAutomationSettings.this.getRuleIntent(ri.settingsAction, null, savedRuleId));
            }
        }.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showDeleteRuleDialog(final String ruleId, CharSequence ruleName) {
        new AlertDialog.Builder(this.mContext).setMessage(getString(R.string.zen_mode_delete_rule_confirmation, new Object[]{ruleName})).setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) null).setPositiveButton(R.string.zen_mode_delete_rule_button, new DialogInterface.OnClickListener() { // from class: com.android.settings.notification.ZenModeAutomationSettings.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                MetricsLogger.action(ZenModeAutomationSettings.this.mContext, 175);
                ZenModeAutomationSettings.this.removeZenRule(ruleId);
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Intent getRuleIntent(String settingsAction, ComponentName configurationActivity, String ruleId) {
        Intent intent = new Intent().addFlags(67108864).putExtra("android.service.notification.extra.RULE_ID", ruleId);
        if (configurationActivity != null) {
            intent.setComponent(configurationActivity);
        } else {
            intent.setAction(settingsAction);
        }
        return intent;
    }

    private Map.Entry<String, AutomaticZenRule>[] sortedRules() {
        Map.Entry<String, AutomaticZenRule>[] rt = (Map.Entry[]) this.mRules.toArray(new Map.Entry[this.mRules.size()]);
        Arrays.sort(rt, RULE_COMPARATOR);
        return rt;
    }

    private void updateControls() {
        PreferenceScreen root = getPreferenceScreen();
        root.removeAll();
        Map.Entry<String, AutomaticZenRule>[] sortedRules = sortedRules();
        for (Map.Entry<String, AutomaticZenRule> sortedRule : sortedRules) {
            ZenRulePreference pref = new ZenRulePreference(getPrefContext(), sortedRule);
            if (pref.appExists) {
                root.addPreference(pref);
            }
        }
        Preference p = new Preference(getPrefContext());
        p.setIcon(R.drawable.ic_add);
        p.setTitle(R.string.zen_mode_add_rule);
        p.setPersistent(false);
        p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.android.settings.notification.ZenModeAutomationSettings.5
            @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
            public boolean onPreferenceClick(Preference preference) {
                MetricsLogger.action(ZenModeAutomationSettings.this.mContext, 172);
                ZenModeAutomationSettings.this.showAddRuleDialog();
                return true;
            }
        });
        root.addPreference(p);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 142;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String computeRuleSummary(AutomaticZenRule rule, boolean isSystemRule, CharSequence providerLabel) {
        String mode = computeZenModeCaption(getResources(), rule.getInterruptionFilter());
        String ruleState = (rule == null || !rule.isEnabled()) ? getString(R.string.switch_off_text) : getString(R.string.zen_mode_rule_summary_enabled_combination, new Object[]{mode});
        return isSystemRule ? ruleState : getString(R.string.zen_mode_rule_summary_provider_combination, new Object[]{providerLabel, ruleState});
    }

    private static ManagedServiceSettings.Config getConditionProviderConfig() {
        ManagedServiceSettings.Config c = new ManagedServiceSettings.Config();
        c.tag = "ZenModeSettings";
        c.setting = "enabled_notification_policy_access_packages";
        c.secondarySetting = "enabled_notification_listeners";
        c.intentAction = "android.service.notification.ConditionProviderService";
        c.permission = "android.permission.BIND_CONDITION_PROVIDER_SERVICE";
        c.noun = "condition provider";
        return c;
    }

    private static String computeZenModeCaption(Resources res, int zenMode) {
        switch (zenMode) {
            case 2:
                return res.getString(R.string.zen_mode_option_important_interruptions);
            case 3:
                return res.getString(R.string.zen_mode_option_no_interruptions);
            case 4:
                return res.getString(R.string.zen_mode_option_alarms);
            default:
                return null;
        }
    }

    public static ZenRuleInfo getRuleInfo(PackageManager pm, ServiceInfo si) {
        if (si == null || si.metaData == null) {
            return null;
        }
        String ruleType = si.metaData.getString("android.service.zen.automatic.ruleType");
        ComponentName configurationActivity = getSettingsActivity(si);
        if (ruleType == null || ruleType.trim().isEmpty() || configurationActivity == null) {
            return null;
        }
        ZenRuleInfo ri = new ZenRuleInfo();
        ri.serviceComponent = new ComponentName(si.packageName, si.name);
        ri.settingsAction = "android.settings.ZEN_MODE_EXTERNAL_RULE_SETTINGS";
        ri.title = ruleType;
        ri.packageName = si.packageName;
        ri.configurationActivity = getSettingsActivity(si);
        ri.packageLabel = si.applicationInfo.loadLabel(pm);
        ri.ruleInstanceLimit = si.metaData.getInt("android.service.zen.automatic.ruleInstanceLimit", -1);
        return ri;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ComponentName getSettingsActivity(ServiceInfo si) {
        String configurationActivity;
        if (si == null || si.metaData == null || (configurationActivity = si.metaData.getString("android.service.zen.automatic.configurationActivity")) == null) {
            return null;
        }
        return ComponentName.unflattenFromString(configurationActivity);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ZenRulePreference extends Preference {
        final boolean appExists;
        private final View.OnClickListener mDeleteListener;
        final String mId;
        final CharSequence mName;

        public ZenRulePreference(Context context, Map.Entry<String, AutomaticZenRule> ruleEntry) {
            super(context);
            String action;
            this.mDeleteListener = new View.OnClickListener() { // from class: com.android.settings.notification.ZenModeAutomationSettings.ZenRulePreference.1
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    ZenModeAutomationSettings.this.showDeleteRuleDialog(ZenRulePreference.this.mId, ZenRulePreference.this.mName);
                }
            };
            AutomaticZenRule rule = ruleEntry.getValue();
            this.mName = rule.getName();
            this.mId = ruleEntry.getKey();
            boolean isSchedule = ZenModeConfig.isValidScheduleConditionId(rule.getConditionId());
            boolean isEvent = ZenModeConfig.isValidEventConditionId(rule.getConditionId());
            boolean z = !isSchedule ? isEvent : true;
            try {
                ApplicationInfo info = ZenModeAutomationSettings.this.mPm.getApplicationInfo(rule.getOwner().getPackageName(), 0);
                LoadIconTask task = new LoadIconTask(this);
                task.execute(info);
                setSummary(ZenModeAutomationSettings.this.computeRuleSummary(rule, z, info.loadLabel(ZenModeAutomationSettings.this.mPm)));
                this.appExists = true;
                setTitle(rule.getName());
                setPersistent(false);
                if (isSchedule) {
                    action = "android.settings.ZEN_MODE_SCHEDULE_RULE_SETTINGS";
                } else {
                    action = isEvent ? "android.settings.ZEN_MODE_EVENT_RULE_SETTINGS" : "";
                }
                ServiceInfo si = ZenModeAutomationSettings.this.mServiceListing.findService(rule.getOwner());
                ComponentName settingsActivity = ZenModeAutomationSettings.getSettingsActivity(si);
                setIntent(ZenModeAutomationSettings.this.getRuleIntent(action, settingsActivity, this.mId));
                setSelectable(settingsActivity != null ? true : z);
                setWidgetLayoutResource(R.layout.zen_rule_widget);
            } catch (PackageManager.NameNotFoundException e) {
                setIcon(R.drawable.ic_label);
                this.appExists = false;
            }
        }

        @Override // android.support.v7.preference.Preference
        public void onBindViewHolder(PreferenceViewHolder view) {
            super.onBindViewHolder(view);
            View v = view.findViewById(R.id.delete_zen_rule);
            if (v != null) {
                v.setOnClickListener(this.mDeleteListener);
            }
            view.setDividerAllowedAbove(true);
            view.setDividerAllowedBelow(true);
        }
    }

    /* loaded from: classes.dex */
    private class LoadIconTask extends AsyncTask<ApplicationInfo, Void, Drawable> {
        private final WeakReference<Preference> prefReference;

        public LoadIconTask(Preference pref) {
            this.prefReference = new WeakReference<>(pref);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Drawable doInBackground(ApplicationInfo... params) {
            return params[0].loadIcon(ZenModeAutomationSettings.this.mPm);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Drawable icon) {
            Preference pref;
            if (icon == null || (pref = this.prefReference.get()) == null) {
                return;
            }
            pref.setIcon(icon);
        }
    }
}
