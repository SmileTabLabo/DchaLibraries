package com.android.settings.inputmethod;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.icu.text.ListFormatter;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.app.LocaleHelper;
import com.android.internal.inputmethod.InputMethodUtils;
import com.android.settings.SettingsPreferenceFragment;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
/* loaded from: classes.dex */
class InputMethodAndSubtypeUtil {
    private static final TextUtils.SimpleStringSplitter sStringInputMethodSplitter = new TextUtils.SimpleStringSplitter(':');
    private static final TextUtils.SimpleStringSplitter sStringInputMethodSubtypeSplitter = new TextUtils.SimpleStringSplitter(';');

    InputMethodAndSubtypeUtil() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String buildInputMethodsAndSubtypesString(HashMap<String, HashSet<String>> imeToSubtypesMap) {
        StringBuilder builder = new StringBuilder();
        for (String imi : imeToSubtypesMap.keySet()) {
            if (builder.length() > 0) {
                builder.append(':');
            }
            HashSet<String> subtypeIdSet = imeToSubtypesMap.get(imi);
            builder.append(imi);
            for (String subtypeId : subtypeIdSet) {
                builder.append(';').append(subtypeId);
            }
        }
        return builder.toString();
    }

    private static String buildInputMethodsString(HashSet<String> imiList) {
        StringBuilder builder = new StringBuilder();
        for (String imi : imiList) {
            if (builder.length() > 0) {
                builder.append(':');
            }
            builder.append(imi);
        }
        return builder.toString();
    }

    private static int getInputMethodSubtypeSelected(ContentResolver resolver) {
        try {
            return Settings.Secure.getInt(resolver, "selected_input_method_subtype");
        } catch (Settings.SettingNotFoundException e) {
            return -1;
        }
    }

    private static boolean isInputMethodSubtypeSelected(ContentResolver resolver) {
        return getInputMethodSubtypeSelected(resolver) != -1;
    }

    private static void putSelectedInputMethodSubtype(ContentResolver resolver, int hashCode) {
        Settings.Secure.putInt(resolver, "selected_input_method_subtype", hashCode);
    }

    private static HashMap<String, HashSet<String>> getEnabledInputMethodsAndSubtypeList(ContentResolver resolver) {
        String enabledInputMethodsStr = Settings.Secure.getString(resolver, "enabled_input_methods");
        return parseInputMethodsAndSubtypesString(enabledInputMethodsStr);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static HashMap<String, HashSet<String>> parseInputMethodsAndSubtypesString(String inputMethodsAndSubtypesString) {
        HashMap<String, HashSet<String>> subtypesMap = new HashMap<>();
        if (TextUtils.isEmpty(inputMethodsAndSubtypesString)) {
            return subtypesMap;
        }
        sStringInputMethodSplitter.setString(inputMethodsAndSubtypesString);
        while (sStringInputMethodSplitter.hasNext()) {
            String nextImsStr = sStringInputMethodSplitter.next();
            sStringInputMethodSubtypeSplitter.setString(nextImsStr);
            if (sStringInputMethodSubtypeSplitter.hasNext()) {
                HashSet<String> subtypeIdSet = new HashSet<>();
                String imiId = sStringInputMethodSubtypeSplitter.next();
                while (sStringInputMethodSubtypeSplitter.hasNext()) {
                    subtypeIdSet.add(sStringInputMethodSubtypeSplitter.next());
                }
                subtypesMap.put(imiId, subtypeIdSet);
            }
        }
        return subtypesMap;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void enableInputMethodSubtypesOf(ContentResolver resolver, String imiId, HashSet<String> enabledSubtypeIdSet) {
        HashMap<String, HashSet<String>> enabledImeAndSubtypeIdsMap = getEnabledInputMethodsAndSubtypeList(resolver);
        enabledImeAndSubtypeIdsMap.put(imiId, enabledSubtypeIdSet);
        String enabledImesAndSubtypesString = buildInputMethodsAndSubtypesString(enabledImeAndSubtypeIdsMap);
        Settings.Secure.putString(resolver, "enabled_input_methods", enabledImesAndSubtypesString);
    }

    private static HashSet<String> getDisabledSystemIMEs(ContentResolver resolver) {
        HashSet<String> set = new HashSet<>();
        String disabledIMEsStr = Settings.Secure.getString(resolver, "disabled_system_input_methods");
        if (TextUtils.isEmpty(disabledIMEsStr)) {
            return set;
        }
        sStringInputMethodSplitter.setString(disabledIMEsStr);
        while (sStringInputMethodSplitter.hasNext()) {
            set.add(sStringInputMethodSplitter.next());
        }
        return set;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void saveInputMethodSubtypeList(SettingsPreferenceFragment context, ContentResolver resolver, List<InputMethodInfo> inputMethodInfos, boolean hasHardKeyboard) {
        boolean isImeChecked;
        String currentInputMethodId = Settings.Secure.getString(resolver, "default_input_method");
        int selectedInputMethodSubtype = getInputMethodSubtypeSelected(resolver);
        HashMap<String, HashSet<String>> enabledIMEsAndSubtypesMap = getEnabledInputMethodsAndSubtypeList(resolver);
        HashSet<String> disabledSystemIMEs = getDisabledSystemIMEs(resolver);
        boolean needsToResetSelectedSubtype = false;
        for (InputMethodInfo imi : inputMethodInfos) {
            String imiId = imi.getId();
            Preference pref = context.findPreference(imiId);
            if (pref != null) {
                if (pref instanceof TwoStatePreference) {
                    isImeChecked = ((TwoStatePreference) pref).isChecked();
                } else {
                    isImeChecked = enabledIMEsAndSubtypesMap.containsKey(imiId);
                }
                boolean isCurrentInputMethod = imiId.equals(currentInputMethodId);
                boolean systemIme = InputMethodUtils.isSystemIme(imi);
                if ((!hasHardKeyboard && InputMethodSettingValuesWrapper.getInstance(context.getActivity()).isAlwaysCheckedIme(imi, context.getActivity())) || isImeChecked) {
                    if (!enabledIMEsAndSubtypesMap.containsKey(imiId)) {
                        enabledIMEsAndSubtypesMap.put(imiId, new HashSet<>());
                    }
                    HashSet<String> subtypesSet = enabledIMEsAndSubtypesMap.get(imiId);
                    boolean subtypePrefFound = false;
                    int subtypeCount = imi.getSubtypeCount();
                    for (int i = 0; i < subtypeCount; i++) {
                        InputMethodSubtype subtype = imi.getSubtypeAt(i);
                        String subtypeHashCodeStr = String.valueOf(subtype.hashCode());
                        TwoStatePreference subtypePref = (TwoStatePreference) context.findPreference(imiId + subtypeHashCodeStr);
                        if (subtypePref != null) {
                            if (!subtypePrefFound) {
                                subtypesSet.clear();
                                needsToResetSelectedSubtype = true;
                                subtypePrefFound = true;
                            }
                            if (subtypePref.isEnabled() && subtypePref.isChecked()) {
                                subtypesSet.add(subtypeHashCodeStr);
                                if (isCurrentInputMethod && selectedInputMethodSubtype == subtype.hashCode()) {
                                    needsToResetSelectedSubtype = false;
                                }
                            } else {
                                subtypesSet.remove(subtypeHashCodeStr);
                            }
                        }
                    }
                } else {
                    enabledIMEsAndSubtypesMap.remove(imiId);
                    if (isCurrentInputMethod) {
                        currentInputMethodId = null;
                    }
                }
                if (systemIme && hasHardKeyboard) {
                    if (disabledSystemIMEs.contains(imiId)) {
                        if (isImeChecked) {
                            disabledSystemIMEs.remove(imiId);
                        }
                    } else if (!isImeChecked) {
                        disabledSystemIMEs.add(imiId);
                    }
                }
            }
        }
        String enabledIMEsAndSubtypesString = buildInputMethodsAndSubtypesString(enabledIMEsAndSubtypesMap);
        String disabledSystemIMEsString = buildInputMethodsString(disabledSystemIMEs);
        if (needsToResetSelectedSubtype || !isInputMethodSubtypeSelected(resolver)) {
            putSelectedInputMethodSubtype(resolver, -1);
        }
        Settings.Secure.putString(resolver, "enabled_input_methods", enabledIMEsAndSubtypesString);
        if (disabledSystemIMEsString.length() > 0) {
            Settings.Secure.putString(resolver, "disabled_system_input_methods", disabledSystemIMEsString);
        }
        if (currentInputMethodId == null) {
            currentInputMethodId = "";
        }
        Settings.Secure.putString(resolver, "default_input_method", currentInputMethodId);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void loadInputMethodSubtypeList(SettingsPreferenceFragment context, ContentResolver resolver, List<InputMethodInfo> inputMethodInfos, Map<String, List<Preference>> inputMethodPrefsMap) {
        HashMap<String, HashSet<String>> enabledSubtypes = getEnabledInputMethodsAndSubtypeList(resolver);
        for (InputMethodInfo imi : inputMethodInfos) {
            String imiId = imi.getId();
            Preference pref = context.findPreference(imiId);
            if (pref instanceof TwoStatePreference) {
                TwoStatePreference subtypePref = (TwoStatePreference) pref;
                boolean isEnabled = enabledSubtypes.containsKey(imiId);
                subtypePref.setChecked(isEnabled);
                if (inputMethodPrefsMap != null) {
                    for (Preference childPref : inputMethodPrefsMap.get(imiId)) {
                        childPref.setEnabled(isEnabled);
                    }
                }
                setSubtypesPreferenceEnabled(context, inputMethodInfos, imiId, isEnabled);
            }
        }
        updateSubtypesPreferenceChecked(context, inputMethodInfos, enabledSubtypes);
    }

    static void setSubtypesPreferenceEnabled(SettingsPreferenceFragment context, List<InputMethodInfo> inputMethodProperties, String id, boolean enabled) {
        PreferenceScreen preferenceScreen = context.getPreferenceScreen();
        for (InputMethodInfo imi : inputMethodProperties) {
            if (id.equals(imi.getId())) {
                int subtypeCount = imi.getSubtypeCount();
                for (int i = 0; i < subtypeCount; i++) {
                    InputMethodSubtype subtype = imi.getSubtypeAt(i);
                    TwoStatePreference pref = (TwoStatePreference) preferenceScreen.findPreference(id + subtype.hashCode());
                    if (pref != null) {
                        pref.setEnabled(enabled);
                    }
                }
            }
        }
    }

    private static void updateSubtypesPreferenceChecked(SettingsPreferenceFragment context, List<InputMethodInfo> inputMethodProperties, HashMap<String, HashSet<String>> enabledSubtypes) {
        PreferenceScreen preferenceScreen = context.getPreferenceScreen();
        for (InputMethodInfo imi : inputMethodProperties) {
            String id = imi.getId();
            if (enabledSubtypes.containsKey(id)) {
                HashSet<String> enabledSubtypesSet = enabledSubtypes.get(id);
                int subtypeCount = imi.getSubtypeCount();
                for (int i = 0; i < subtypeCount; i++) {
                    InputMethodSubtype subtype = imi.getSubtypeAt(i);
                    String hashCode = String.valueOf(subtype.hashCode());
                    TwoStatePreference pref = (TwoStatePreference) preferenceScreen.findPreference(id + hashCode);
                    if (pref != null) {
                        pref.setChecked(enabledSubtypesSet.contains(hashCode));
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void removeUnnecessaryNonPersistentPreference(Preference pref) {
        SharedPreferences prefs;
        String key = pref.getKey();
        if (pref.isPersistent() || key == null || (prefs = pref.getSharedPreferences()) == null || !prefs.contains(key)) {
            return;
        }
        prefs.edit().remove(key).apply();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String getSubtypeLocaleNameAsSentence(InputMethodSubtype subtype, Context context, InputMethodInfo inputMethodInfo) {
        if (subtype == null) {
            return "";
        }
        Locale locale = getDisplayLocale(context);
        CharSequence subtypeName = subtype.getDisplayName(context, inputMethodInfo.getPackageName(), inputMethodInfo.getServiceInfo().applicationInfo);
        return LocaleHelper.toSentenceCase(subtypeName.toString(), locale);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String getSubtypeLocaleNameListAsSentence(List<InputMethodSubtype> subtypes, Context context, InputMethodInfo inputMethodInfo) {
        if (subtypes.isEmpty()) {
            return "";
        }
        Locale locale = getDisplayLocale(context);
        int subtypeCount = subtypes.size();
        CharSequence[] subtypeNames = new CharSequence[subtypeCount];
        for (int i = 0; i < subtypeCount; i++) {
            subtypeNames[i] = subtypes.get(i).getDisplayName(context, inputMethodInfo.getPackageName(), inputMethodInfo.getServiceInfo().applicationInfo);
        }
        return LocaleHelper.toSentenceCase(ListFormatter.getInstance(locale).format(subtypeNames), locale);
    }

    private static Locale getDisplayLocale(Context context) {
        if (context == null) {
            return Locale.getDefault();
        }
        if (context.getResources() == null) {
            return Locale.getDefault();
        }
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration == null) {
            return Locale.getDefault();
        }
        Locale configurationLocale = configuration.getLocales().get(0);
        if (configurationLocale == null) {
            return Locale.getDefault();
        }
        return configurationLocale;
    }
}
