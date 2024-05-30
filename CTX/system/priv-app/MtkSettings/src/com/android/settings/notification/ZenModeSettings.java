package com.android.settings.notification;

import android.app.AutomaticZenRule;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.Context;
import android.icu.text.ListFormatter;
import android.provider.SearchIndexableResource;
import android.service.notification.ZenModeConfig;
import com.android.settings.R;
import com.android.settings.notification.ZenModeSettings;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
/* loaded from: classes.dex */
public class ZenModeSettings extends ZenModeSettingsBase {
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.android.settings.notification.ZenModeSettings.1
        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean z) {
            SearchIndexableResource searchIndexableResource = new SearchIndexableResource(context);
            searchIndexableResource.xmlResId = R.xml.zen_mode_settings;
            return Arrays.asList(searchIndexableResource);
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<String> getNonIndexableKeys(Context context) {
            List<String> nonIndexableKeys = super.getNonIndexableKeys(context);
            nonIndexableKeys.add("zen_mode_duration_settings");
            nonIndexableKeys.add("zen_mode_settings_button_container");
            return nonIndexableKeys;
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider
        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return ZenModeSettings.buildPreferenceControllers(context, null, null);
        }
    };

    @Override // com.android.settings.notification.ZenModeSettingsBase, com.android.settings.dashboard.RestrictedDashboardFragment, com.android.settings.dashboard.DashboardFragment, com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.zen_mode_settings;
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 76;
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle(), getFragmentManager());
    }

    @Override // com.android.settings.support.actionbar.HelpResourceProvider
    public int getHelpResource() {
        return R.string.help_uri_interruptions;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Lifecycle lifecycle, FragmentManager fragmentManager) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new ZenModeBehaviorMsgEventReminderPreferenceController(context, lifecycle));
        arrayList.add(new ZenModeBehaviorSoundPreferenceController(context, lifecycle));
        arrayList.add(new ZenModeBehaviorCallsPreferenceController(context, lifecycle));
        arrayList.add(new ZenModeBlockedEffectsPreferenceController(context, lifecycle));
        arrayList.add(new ZenModeDurationPreferenceController(context, lifecycle, fragmentManager));
        arrayList.add(new ZenModeAutomationPreferenceController(context));
        arrayList.add(new ZenModeButtonPreferenceController(context, lifecycle, fragmentManager));
        arrayList.add(new ZenModeSettingsFooterPreferenceController(context, lifecycle));
        return arrayList;
    }

    /* loaded from: classes.dex */
    public static class SummaryBuilder {
        private static final int[] ALL_PRIORITY_CATEGORIES = {32, 64, 128, 4, 2, 1, 8, 16};
        private Context mContext;

        public SummaryBuilder(Context context) {
            this.mContext = context;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public String getSoundSettingSummary(NotificationManager.Policy policy) {
            List<String> enabledCategories = getEnabledCategories(policy, new Predicate() { // from class: com.android.settings.notification.-$$Lambda$ZenModeSettings$SummaryBuilder$-hUbn9epxyVxqc9qNo66a-LO5Ug
                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return ZenModeSettings.SummaryBuilder.lambda$getSoundSettingSummary$0((Integer) obj);
                }
            });
            int size = enabledCategories.size();
            if (size == 0) {
                return this.mContext.getString(R.string.zen_sound_all_muted);
            }
            if (size == 1) {
                return this.mContext.getString(R.string.zen_sound_one_allowed, enabledCategories.get(0).toLowerCase());
            }
            if (size == 2) {
                return this.mContext.getString(R.string.zen_sound_two_allowed, enabledCategories.get(0).toLowerCase(), enabledCategories.get(1).toLowerCase());
            }
            if (size == 3) {
                return this.mContext.getString(R.string.zen_sound_three_allowed, enabledCategories.get(0).toLowerCase(), enabledCategories.get(1).toLowerCase(), enabledCategories.get(2).toLowerCase());
            }
            return this.mContext.getString(R.string.zen_sound_none_muted);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static /* synthetic */ boolean lambda$getSoundSettingSummary$0(Integer num) {
            return 32 == num.intValue() || 64 == num.intValue() || 128 == num.intValue();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public String getCallsSettingSummary(NotificationManager.Policy policy) {
            List<String> enabledCategories = getEnabledCategories(policy, new Predicate() { // from class: com.android.settings.notification.-$$Lambda$ZenModeSettings$SummaryBuilder$_Gea8GbwXN997GXaupRdGPPi1FA
                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return ZenModeSettings.SummaryBuilder.lambda$getCallsSettingSummary$1((Integer) obj);
                }
            });
            int size = enabledCategories.size();
            if (size == 0) {
                return this.mContext.getString(R.string.zen_mode_no_exceptions);
            }
            if (size == 1) {
                return this.mContext.getString(R.string.zen_mode_calls_summary_one, enabledCategories.get(0).toLowerCase());
            }
            return this.mContext.getString(R.string.zen_mode_calls_summary_two, enabledCategories.get(0).toLowerCase(), enabledCategories.get(1).toLowerCase());
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static /* synthetic */ boolean lambda$getCallsSettingSummary$1(Integer num) {
            return 8 == num.intValue() || 16 == num.intValue();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public String getMsgEventReminderSettingSummary(NotificationManager.Policy policy) {
            List<String> enabledCategories = getEnabledCategories(policy, new Predicate() { // from class: com.android.settings.notification.-$$Lambda$ZenModeSettings$SummaryBuilder$Ydm8DmhkL6wV0O584-hfIH59p1A
                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return ZenModeSettings.SummaryBuilder.lambda$getMsgEventReminderSettingSummary$2((Integer) obj);
                }
            });
            int size = enabledCategories.size();
            if (size == 0) {
                return this.mContext.getString(R.string.zen_mode_no_exceptions);
            }
            if (size == 1) {
                return enabledCategories.get(0);
            }
            if (size == 2) {
                return this.mContext.getString(R.string.join_two_items, enabledCategories.get(0), enabledCategories.get(1).toLowerCase());
            }
            if (size == 3) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(enabledCategories.get(0));
                arrayList.add(enabledCategories.get(1).toLowerCase());
                arrayList.add(enabledCategories.get(2).toLowerCase());
                return ListFormatter.getInstance().format(arrayList);
            }
            ArrayList arrayList2 = new ArrayList();
            arrayList2.add(enabledCategories.get(0));
            arrayList2.add(enabledCategories.get(1).toLowerCase());
            arrayList2.add(enabledCategories.get(2).toLowerCase());
            arrayList2.add(this.mContext.getString(R.string.zen_mode_other_options));
            return ListFormatter.getInstance().format(arrayList2);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static /* synthetic */ boolean lambda$getMsgEventReminderSettingSummary$2(Integer num) {
            return 2 == num.intValue() || 1 == num.intValue() || 4 == num.intValue();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public String getSoundSummary() {
            if (NotificationManager.from(this.mContext).getZenMode() != 0) {
                String description = ZenModeConfig.getDescription(this.mContext, true, NotificationManager.from(this.mContext).getZenModeConfig(), false);
                return description == null ? this.mContext.getString(R.string.zen_mode_sound_summary_on) : this.mContext.getString(R.string.zen_mode_sound_summary_on_with_info, description);
            }
            int enabledAutomaticRulesCount = getEnabledAutomaticRulesCount();
            if (enabledAutomaticRulesCount > 0) {
                return this.mContext.getString(R.string.zen_mode_sound_summary_off_with_info, this.mContext.getResources().getQuantityString(R.plurals.zen_mode_sound_summary_summary_off_info, enabledAutomaticRulesCount, Integer.valueOf(enabledAutomaticRulesCount)));
            }
            return this.mContext.getString(R.string.zen_mode_sound_summary_off);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public String getBlockedEffectsSummary(NotificationManager.Policy policy) {
            if (policy.suppressedVisualEffects == 0) {
                return this.mContext.getResources().getString(R.string.zen_mode_restrict_notifications_summary_muted);
            }
            if (NotificationManager.Policy.areAllVisualEffectsSuppressed(policy.suppressedVisualEffects)) {
                return this.mContext.getResources().getString(R.string.zen_mode_restrict_notifications_summary_hidden);
            }
            return this.mContext.getResources().getString(R.string.zen_mode_restrict_notifications_summary_custom);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public String getAutomaticRulesSummary() {
            int enabledAutomaticRulesCount = getEnabledAutomaticRulesCount();
            return enabledAutomaticRulesCount == 0 ? this.mContext.getString(R.string.zen_mode_settings_summary_off) : this.mContext.getResources().getQuantityString(R.plurals.zen_mode_settings_summary_on, enabledAutomaticRulesCount, Integer.valueOf(enabledAutomaticRulesCount));
        }

        int getEnabledAutomaticRulesCount() {
            Map<String, AutomaticZenRule> automaticZenRules = NotificationManager.from(this.mContext).getAutomaticZenRules();
            int i = 0;
            if (automaticZenRules != null) {
                for (Map.Entry<String, AutomaticZenRule> entry : automaticZenRules.entrySet()) {
                    AutomaticZenRule value = entry.getValue();
                    if (value != null && value.isEnabled()) {
                        i++;
                    }
                }
            }
            return i;
        }

        private List<String> getEnabledCategories(NotificationManager.Policy policy, Predicate<Integer> predicate) {
            int[] iArr;
            ArrayList arrayList = new ArrayList();
            for (int i : ALL_PRIORITY_CATEGORIES) {
                if (predicate.test(Integer.valueOf(i)) && isCategoryEnabled(policy, i)) {
                    if (i == 32) {
                        arrayList.add(this.mContext.getString(R.string.zen_mode_alarms));
                    } else if (i == 64) {
                        arrayList.add(this.mContext.getString(R.string.zen_mode_media));
                    } else if (i == 128) {
                        arrayList.add(this.mContext.getString(R.string.zen_mode_system));
                    } else if (i == 4) {
                        if (policy.priorityMessageSenders == 0) {
                            arrayList.add(this.mContext.getString(R.string.zen_mode_all_messages));
                        } else {
                            arrayList.add(this.mContext.getString(R.string.zen_mode_selected_messages));
                        }
                    } else if (i == 2) {
                        arrayList.add(this.mContext.getString(R.string.zen_mode_events));
                    } else if (i == 1) {
                        arrayList.add(this.mContext.getString(R.string.zen_mode_reminders));
                    } else if (i == 8) {
                        if (policy.priorityCallSenders != 0) {
                            if (policy.priorityCallSenders == 1) {
                                arrayList.add(this.mContext.getString(R.string.zen_mode_contacts_callers));
                            } else {
                                arrayList.add(this.mContext.getString(R.string.zen_mode_starred_callers));
                            }
                        } else {
                            arrayList.add(this.mContext.getString(R.string.zen_mode_all_callers));
                        }
                    } else if (i == 16 && !arrayList.contains(this.mContext.getString(R.string.zen_mode_all_callers))) {
                        arrayList.add(this.mContext.getString(R.string.zen_mode_repeat_callers));
                    }
                }
            }
            return arrayList;
        }

        private boolean isCategoryEnabled(NotificationManager.Policy policy, int i) {
            return (policy.priorityCategories & i) != 0;
        }
    }
}
