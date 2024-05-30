package com.android.settings.notification;

import android.app.AutomaticZenRule;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.service.notification.ZenModeConfig;
import android.support.v7.preference.Preference;
import android.util.Pair;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.notification.ZenRuleNameDialog;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/* loaded from: classes.dex */
public abstract class AbstractZenModeAutomaticRulePreferenceController extends AbstractZenModePreferenceController implements PreferenceControllerMixin {
    private static final Comparator<Map.Entry<String, AutomaticZenRule>> RULE_COMPARATOR = new Comparator<Map.Entry<String, AutomaticZenRule>>() { // from class: com.android.settings.notification.AbstractZenModeAutomaticRulePreferenceController.1
        @Override // java.util.Comparator
        public int compare(Map.Entry<String, AutomaticZenRule> entry, Map.Entry<String, AutomaticZenRule> entry2) {
            boolean contains = AbstractZenModeAutomaticRulePreferenceController.access$000().contains(entry.getKey());
            if (contains != AbstractZenModeAutomaticRulePreferenceController.access$000().contains(entry2.getKey())) {
                return contains ? -1 : 1;
            }
            int compare = Long.compare(entry.getValue().getCreationTime(), entry2.getValue().getCreationTime());
            if (compare != 0) {
                return compare;
            }
            return key(entry.getValue()).compareTo(key(entry2.getValue()));
        }

        private String key(AutomaticZenRule automaticZenRule) {
            int i;
            if (ZenModeConfig.isValidScheduleConditionId(automaticZenRule.getConditionId())) {
                i = 1;
            } else {
                i = ZenModeConfig.isValidEventConditionId(automaticZenRule.getConditionId()) ? 2 : 3;
            }
            return i + automaticZenRule.getName().toString();
        }
    };
    private static List<String> mDefaultRuleIds;
    protected ZenModeBackend mBackend;
    protected Fragment mParent;
    protected PackageManager mPm;
    protected Set<Map.Entry<String, AutomaticZenRule>> mRules;

    static /* synthetic */ List access$000() {
        return getDefaultRuleIds();
    }

    public AbstractZenModeAutomaticRulePreferenceController(Context context, String str, Fragment fragment, Lifecycle lifecycle) {
        super(context, str, lifecycle);
        this.mBackend = ZenModeBackend.getInstance(context);
        this.mPm = this.mContext.getPackageManager();
        this.mParent = fragment;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        super.updateState(preference);
        this.mRules = getZenModeRules();
    }

    private static List<String> getDefaultRuleIds() {
        if (mDefaultRuleIds == null) {
            mDefaultRuleIds = ZenModeConfig.DEFAULT_RULE_IDS;
        }
        return mDefaultRuleIds;
    }

    private Set<Map.Entry<String, AutomaticZenRule>> getZenModeRules() {
        return NotificationManager.from(this.mContext).getAutomaticZenRules().entrySet();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void showNameRuleDialog(ZenRuleInfo zenRuleInfo, Fragment fragment) {
        ZenRuleNameDialog.show(fragment, null, zenRuleInfo.defaultConditionId, new RuleNameChangeListener(zenRuleInfo));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Map.Entry<String, AutomaticZenRule>[] sortedRules() {
        if (this.mRules == null) {
            this.mRules = getZenModeRules();
        }
        Map.Entry<String, AutomaticZenRule>[] entryArr = (Map.Entry[]) this.mRules.toArray(new Map.Entry[this.mRules.size()]);
        Arrays.sort(entryArr, RULE_COMPARATOR);
        return entryArr;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static Intent getRuleIntent(String str, ComponentName componentName, String str2) {
        Intent putExtra = new Intent().addFlags(67108864).putExtra("android.service.notification.extra.RULE_ID", str2);
        if (componentName != null) {
            putExtra.setComponent(componentName);
        } else {
            putExtra.setAction(str);
        }
        return putExtra;
    }

    public static ZenRuleInfo getRuleInfo(PackageManager packageManager, ServiceInfo serviceInfo) {
        if (serviceInfo == null || serviceInfo.metaData == null) {
            return null;
        }
        String string = serviceInfo.metaData.getString("android.service.zen.automatic.ruleType");
        ComponentName settingsActivity = getSettingsActivity(serviceInfo);
        if (string == null || string.trim().isEmpty() || settingsActivity == null) {
            return null;
        }
        ZenRuleInfo zenRuleInfo = new ZenRuleInfo();
        zenRuleInfo.serviceComponent = new ComponentName(serviceInfo.packageName, serviceInfo.name);
        zenRuleInfo.settingsAction = "android.settings.ZEN_MODE_EXTERNAL_RULE_SETTINGS";
        zenRuleInfo.title = string;
        zenRuleInfo.packageName = serviceInfo.packageName;
        zenRuleInfo.configurationActivity = getSettingsActivity(serviceInfo);
        zenRuleInfo.packageLabel = serviceInfo.applicationInfo.loadLabel(packageManager);
        zenRuleInfo.ruleInstanceLimit = serviceInfo.metaData.getInt("android.service.zen.automatic.ruleInstanceLimit", -1);
        return zenRuleInfo;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static ComponentName getSettingsActivity(ServiceInfo serviceInfo) {
        String string;
        if (serviceInfo == null || serviceInfo.metaData == null || (string = serviceInfo.metaData.getString("android.service.zen.automatic.configurationActivity")) == null) {
            return null;
        }
        return ComponentName.unflattenFromString(string);
    }

    /* loaded from: classes.dex */
    public class RuleNameChangeListener implements ZenRuleNameDialog.PositiveClickListener {
        ZenRuleInfo mRuleInfo;

        public RuleNameChangeListener(ZenRuleInfo zenRuleInfo) {
            this.mRuleInfo = zenRuleInfo;
        }

        @Override // com.android.settings.notification.ZenRuleNameDialog.PositiveClickListener
        public void onOk(String str, Fragment fragment) {
            AbstractZenModeAutomaticRulePreferenceController.this.mMetricsFeatureProvider.action(AbstractZenModeAutomaticRulePreferenceController.this.mContext, 1267, new Pair[0]);
            String addZenRule = AbstractZenModeAutomaticRulePreferenceController.this.mBackend.addZenRule(new AutomaticZenRule(str, this.mRuleInfo.serviceComponent, this.mRuleInfo.defaultConditionId, 2, true));
            if (addZenRule != null) {
                fragment.startActivity(AbstractZenModeAutomaticRulePreferenceController.getRuleIntent(this.mRuleInfo.settingsAction, null, addZenRule));
            }
        }
    }
}
