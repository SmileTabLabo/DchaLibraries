package com.android.settingslib.inputmethod;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.icu.text.ListFormatter;
import android.provider.Settings;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.app.LocaleHelper;
import com.android.internal.inputmethod.InputMethodUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
/* loaded from: classes.dex */
public class InputMethodAndSubtypeUtil {
    private static final TextUtils.SimpleStringSplitter sStringInputMethodSplitter = new TextUtils.SimpleStringSplitter(':');
    private static final TextUtils.SimpleStringSplitter sStringInputMethodSubtypeSplitter = new TextUtils.SimpleStringSplitter(';');

    private static String buildInputMethodsAndSubtypesString(HashMap<String, HashSet<String>> hashMap) {
        StringBuilder sb = new StringBuilder();
        for (String str : hashMap.keySet()) {
            if (sb.length() > 0) {
                sb.append(':');
            }
            sb.append(str);
            Iterator<String> it = hashMap.get(str).iterator();
            while (it.hasNext()) {
                sb.append(';');
                sb.append(it.next());
            }
        }
        return sb.toString();
    }

    private static String buildInputMethodsString(HashSet<String> hashSet) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = hashSet.iterator();
        while (it.hasNext()) {
            String next = it.next();
            if (sb.length() > 0) {
                sb.append(':');
            }
            sb.append(next);
        }
        return sb.toString();
    }

    private static int getInputMethodSubtypeSelected(ContentResolver contentResolver) {
        try {
            return Settings.Secure.getInt(contentResolver, "selected_input_method_subtype");
        } catch (Settings.SettingNotFoundException e) {
            return -1;
        }
    }

    private static boolean isInputMethodSubtypeSelected(ContentResolver contentResolver) {
        return getInputMethodSubtypeSelected(contentResolver) != -1;
    }

    private static void putSelectedInputMethodSubtype(ContentResolver contentResolver, int i) {
        Settings.Secure.putInt(contentResolver, "selected_input_method_subtype", i);
    }

    private static HashMap<String, HashSet<String>> getEnabledInputMethodsAndSubtypeList(ContentResolver contentResolver) {
        return parseInputMethodsAndSubtypesString(Settings.Secure.getString(contentResolver, "enabled_input_methods"));
    }

    private static HashMap<String, HashSet<String>> parseInputMethodsAndSubtypesString(String str) {
        HashMap<String, HashSet<String>> hashMap = new HashMap<>();
        if (TextUtils.isEmpty(str)) {
            return hashMap;
        }
        sStringInputMethodSplitter.setString(str);
        while (sStringInputMethodSplitter.hasNext()) {
            sStringInputMethodSubtypeSplitter.setString(sStringInputMethodSplitter.next());
            if (sStringInputMethodSubtypeSplitter.hasNext()) {
                HashSet<String> hashSet = new HashSet<>();
                String next = sStringInputMethodSubtypeSplitter.next();
                while (sStringInputMethodSubtypeSplitter.hasNext()) {
                    hashSet.add(sStringInputMethodSubtypeSplitter.next());
                }
                hashMap.put(next, hashSet);
            }
        }
        return hashMap;
    }

    private static HashSet<String> getDisabledSystemIMEs(ContentResolver contentResolver) {
        HashSet<String> hashSet = new HashSet<>();
        String string = Settings.Secure.getString(contentResolver, "disabled_system_input_methods");
        if (TextUtils.isEmpty(string)) {
            return hashSet;
        }
        sStringInputMethodSplitter.setString(string);
        while (sStringInputMethodSplitter.hasNext()) {
            hashSet.add(sStringInputMethodSplitter.next());
        }
        return hashSet;
    }

    public static void saveInputMethodSubtypeList(PreferenceFragment preferenceFragment, ContentResolver contentResolver, List<InputMethodInfo> list, boolean z) {
        boolean containsKey;
        Iterator<InputMethodInfo> it;
        String string = Settings.Secure.getString(contentResolver, "default_input_method");
        int inputMethodSubtypeSelected = getInputMethodSubtypeSelected(contentResolver);
        HashMap<String, HashSet<String>> enabledInputMethodsAndSubtypeList = getEnabledInputMethodsAndSubtypeList(contentResolver);
        HashSet<String> disabledSystemIMEs = getDisabledSystemIMEs(contentResolver);
        Iterator<InputMethodInfo> it2 = list.iterator();
        String str = string;
        boolean z2 = false;
        while (it2.hasNext()) {
            InputMethodInfo next = it2.next();
            String id = next.getId();
            Preference findPreference = preferenceFragment.findPreference(id);
            if (findPreference != null) {
                if (findPreference instanceof TwoStatePreference) {
                    containsKey = ((TwoStatePreference) findPreference).isChecked();
                } else {
                    containsKey = enabledInputMethodsAndSubtypeList.containsKey(id);
                }
                boolean equals = id.equals(str);
                boolean isSystemIme = InputMethodUtils.isSystemIme(next);
                if ((!z && InputMethodSettingValuesWrapper.getInstance(preferenceFragment.getActivity()).isAlwaysCheckedIme(next, preferenceFragment.getActivity())) || containsKey) {
                    if (!enabledInputMethodsAndSubtypeList.containsKey(id)) {
                        enabledInputMethodsAndSubtypeList.put(id, new HashSet<>());
                    }
                    HashSet<String> hashSet = enabledInputMethodsAndSubtypeList.get(id);
                    int subtypeCount = next.getSubtypeCount();
                    boolean z3 = z2;
                    int i = 0;
                    boolean z4 = false;
                    while (i < subtypeCount) {
                        Iterator<InputMethodInfo> it3 = it2;
                        InputMethodSubtype subtypeAt = next.getSubtypeAt(i);
                        InputMethodInfo inputMethodInfo = next;
                        String valueOf = String.valueOf(subtypeAt.hashCode());
                        int i2 = subtypeCount;
                        TwoStatePreference twoStatePreference = (TwoStatePreference) preferenceFragment.findPreference(id + valueOf);
                        if (twoStatePreference != null) {
                            if (!z4) {
                                hashSet.clear();
                                z4 = true;
                                z3 = true;
                            }
                            if (twoStatePreference.isEnabled() && twoStatePreference.isChecked()) {
                                hashSet.add(valueOf);
                                if (equals && inputMethodSubtypeSelected == subtypeAt.hashCode()) {
                                    z3 = false;
                                }
                            } else {
                                hashSet.remove(valueOf);
                            }
                        }
                        i++;
                        it2 = it3;
                        next = inputMethodInfo;
                        subtypeCount = i2;
                    }
                    it = it2;
                    z2 = z3;
                } else {
                    it = it2;
                    enabledInputMethodsAndSubtypeList.remove(id);
                    if (equals) {
                        str = null;
                    }
                }
                if (isSystemIme && z) {
                    if (disabledSystemIMEs.contains(id)) {
                        if (containsKey) {
                            disabledSystemIMEs.remove(id);
                        }
                    } else if (!containsKey) {
                        disabledSystemIMEs.add(id);
                    }
                }
                it2 = it;
            }
        }
        String buildInputMethodsAndSubtypesString = buildInputMethodsAndSubtypesString(enabledInputMethodsAndSubtypeList);
        String buildInputMethodsString = buildInputMethodsString(disabledSystemIMEs);
        if (z2 || !isInputMethodSubtypeSelected(contentResolver)) {
            putSelectedInputMethodSubtype(contentResolver, -1);
        }
        Settings.Secure.putString(contentResolver, "enabled_input_methods", buildInputMethodsAndSubtypesString);
        if (buildInputMethodsString.length() > 0) {
            Settings.Secure.putString(contentResolver, "disabled_system_input_methods", buildInputMethodsString);
        }
        if (str == null) {
            str = "";
        }
        Settings.Secure.putString(contentResolver, "default_input_method", str);
    }

    public static void loadInputMethodSubtypeList(PreferenceFragment preferenceFragment, ContentResolver contentResolver, List<InputMethodInfo> list, Map<String, List<Preference>> map) {
        HashMap<String, HashSet<String>> enabledInputMethodsAndSubtypeList = getEnabledInputMethodsAndSubtypeList(contentResolver);
        for (InputMethodInfo inputMethodInfo : list) {
            String id = inputMethodInfo.getId();
            Preference findPreference = preferenceFragment.findPreference(id);
            if (findPreference instanceof TwoStatePreference) {
                boolean containsKey = enabledInputMethodsAndSubtypeList.containsKey(id);
                ((TwoStatePreference) findPreference).setChecked(containsKey);
                if (map != null) {
                    for (Preference preference : map.get(id)) {
                        preference.setEnabled(containsKey);
                    }
                }
                setSubtypesPreferenceEnabled(preferenceFragment, list, id, containsKey);
            }
        }
        updateSubtypesPreferenceChecked(preferenceFragment, list, enabledInputMethodsAndSubtypeList);
    }

    private static void setSubtypesPreferenceEnabled(PreferenceFragment preferenceFragment, List<InputMethodInfo> list, String str, boolean z) {
        PreferenceScreen preferenceScreen = preferenceFragment.getPreferenceScreen();
        for (InputMethodInfo inputMethodInfo : list) {
            if (str.equals(inputMethodInfo.getId())) {
                int subtypeCount = inputMethodInfo.getSubtypeCount();
                for (int i = 0; i < subtypeCount; i++) {
                    InputMethodSubtype subtypeAt = inputMethodInfo.getSubtypeAt(i);
                    TwoStatePreference twoStatePreference = (TwoStatePreference) preferenceScreen.findPreference(str + subtypeAt.hashCode());
                    if (twoStatePreference != null) {
                        twoStatePreference.setEnabled(z);
                    }
                }
            }
        }
    }

    private static void updateSubtypesPreferenceChecked(PreferenceFragment preferenceFragment, List<InputMethodInfo> list, HashMap<String, HashSet<String>> hashMap) {
        PreferenceScreen preferenceScreen = preferenceFragment.getPreferenceScreen();
        for (InputMethodInfo inputMethodInfo : list) {
            String id = inputMethodInfo.getId();
            if (hashMap.containsKey(id)) {
                HashSet<String> hashSet = hashMap.get(id);
                int subtypeCount = inputMethodInfo.getSubtypeCount();
                for (int i = 0; i < subtypeCount; i++) {
                    String valueOf = String.valueOf(inputMethodInfo.getSubtypeAt(i).hashCode());
                    TwoStatePreference twoStatePreference = (TwoStatePreference) preferenceScreen.findPreference(id + valueOf);
                    if (twoStatePreference != null) {
                        twoStatePreference.setChecked(hashSet.contains(valueOf));
                    }
                }
            }
        }
    }

    public static void removeUnnecessaryNonPersistentPreference(Preference preference) {
        SharedPreferences sharedPreferences;
        String key = preference.getKey();
        if (!preference.isPersistent() && key != null && (sharedPreferences = preference.getSharedPreferences()) != null && sharedPreferences.contains(key)) {
            sharedPreferences.edit().remove(key).apply();
        }
    }

    public static String getSubtypeLocaleNameAsSentence(InputMethodSubtype inputMethodSubtype, Context context, InputMethodInfo inputMethodInfo) {
        if (inputMethodSubtype == null) {
            return "";
        }
        return LocaleHelper.toSentenceCase(inputMethodSubtype.getDisplayName(context, inputMethodInfo.getPackageName(), inputMethodInfo.getServiceInfo().applicationInfo).toString(), getDisplayLocale(context));
    }

    public static String getSubtypeLocaleNameListAsSentence(List<InputMethodSubtype> list, Context context, InputMethodInfo inputMethodInfo) {
        if (list.isEmpty()) {
            return "";
        }
        Locale displayLocale = getDisplayLocale(context);
        int size = list.size();
        CharSequence[] charSequenceArr = new CharSequence[size];
        for (int i = 0; i < size; i++) {
            charSequenceArr[i] = list.get(i).getDisplayName(context, inputMethodInfo.getPackageName(), inputMethodInfo.getServiceInfo().applicationInfo);
        }
        return LocaleHelper.toSentenceCase(ListFormatter.getInstance(displayLocale).format(charSequenceArr), displayLocale);
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
        Locale locale = configuration.getLocales().get(0);
        if (locale == null) {
            return Locale.getDefault();
        }
        return locale;
    }
}
