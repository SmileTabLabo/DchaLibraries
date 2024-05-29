package com.android.settings.inputmethod;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.UserDictionarySettings;
import com.android.settings.Utils;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
/* loaded from: classes.dex */
public class UserDictionaryList extends SettingsPreferenceFragment {
    private String mLocale;

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 61;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getActivity()));
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        String locale;
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setTitle(R.string.user_dict_settings_title);
        Intent intent = getActivity().getIntent();
        String stringExtra = intent == null ? null : intent.getStringExtra("locale");
        Bundle arguments = getArguments();
        String localeFromArguments = arguments != null ? arguments.getString("locale") : null;
        if (localeFromArguments != null) {
            locale = localeFromArguments;
        } else if (stringExtra != null) {
            locale = stringExtra;
        } else {
            locale = null;
        }
        this.mLocale = locale;
    }

    /* JADX WARN: Removed duplicated region for block: B:16:0x0057  */
    /* JADX WARN: Removed duplicated region for block: B:29:0x009b  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static TreeSet<String> getUserDictionaryLocalesSet(Context context) {
        Cursor cursor = context.getContentResolver().query(UserDictionary.Words.CONTENT_URI, new String[]{"locale"}, null, null, null);
        TreeSet<String> localeSet = new TreeSet<>();
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("locale");
                do {
                    String locale = cursor.getString(columnIndex);
                    if (locale == null) {
                        locale = "";
                    }
                    localeSet.add(locale);
                } while (cursor.moveToNext());
                cursor.close();
                InputMethodManager imm = (InputMethodManager) context.getSystemService("input_method");
                List<InputMethodInfo> imis = imm.getEnabledInputMethodList();
                for (InputMethodInfo imi : imis) {
                    List<InputMethodSubtype> subtypes = imm.getEnabledInputMethodSubtypeList(imi, true);
                    for (InputMethodSubtype subtype : subtypes) {
                        String locale2 = subtype.getLocale();
                        if (!TextUtils.isEmpty(locale2)) {
                            localeSet.add(locale2);
                        }
                    }
                }
                if (!localeSet.contains(Locale.getDefault().getLanguage().toString())) {
                    localeSet.add(Locale.getDefault().toString());
                }
                return localeSet;
            }
            cursor.close();
            InputMethodManager imm2 = (InputMethodManager) context.getSystemService("input_method");
            List<InputMethodInfo> imis2 = imm2.getEnabledInputMethodList();
            while (imi$iterator.hasNext()) {
            }
            if (!localeSet.contains(Locale.getDefault().getLanguage().toString())) {
            }
            return localeSet;
        } catch (Throwable th) {
            cursor.close();
            throw th;
        }
    }

    protected void createUserDictSettings(PreferenceGroup userDictGroup) {
        Activity activity = getActivity();
        userDictGroup.removeAll();
        TreeSet<String> localeSet = getUserDictionaryLocalesSet(activity);
        if (this.mLocale != null) {
            localeSet.add(this.mLocale);
        }
        if (localeSet.size() > 1) {
            localeSet.add("");
        }
        if (localeSet.isEmpty()) {
            userDictGroup.addPreference(createUserDictionaryPreference(null, activity));
            return;
        }
        for (String locale : localeSet) {
            userDictGroup.addPreference(createUserDictionaryPreference(locale, activity));
        }
    }

    protected Preference createUserDictionaryPreference(String locale, Activity activity) {
        Preference newPref = new Preference(getPrefContext());
        Intent intent = new Intent("android.settings.USER_DICTIONARY_SETTINGS");
        if (locale == null) {
            newPref.setTitle(Locale.getDefault().getDisplayName());
        } else {
            if ("".equals(locale)) {
                newPref.setTitle(getString(R.string.user_dict_settings_all_languages));
            } else {
                newPref.setTitle(Utils.createLocaleFromString(locale).getDisplayName());
            }
            intent.putExtra("locale", locale);
            newPref.getExtras().putString("locale", locale);
        }
        newPref.setIntent(intent);
        newPref.setFragment(UserDictionarySettings.class.getName());
        return newPref;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        createUserDictSettings(getPreferenceScreen());
    }
}
