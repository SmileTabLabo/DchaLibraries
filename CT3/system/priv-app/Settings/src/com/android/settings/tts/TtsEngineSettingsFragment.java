package com.android.settings.tts;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TtsEngines;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
/* loaded from: classes.dex */
public class TtsEngineSettingsFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private Intent mEngineSettingsIntent;
    private Preference mEngineSettingsPreference;
    private TtsEngines mEnginesHelper;
    private Preference mInstallVoicesPreference;
    private ListPreference mLocalePreference;
    private TextToSpeech mTts;
    private Intent mVoiceDataDetails;
    private int mSelectedLocaleIndex = -1;
    private final TextToSpeech.OnInitListener mTtsInitListener = new TextToSpeech.OnInitListener() { // from class: com.android.settings.tts.TtsEngineSettingsFragment.1
        @Override // android.speech.tts.TextToSpeech.OnInitListener
        public void onInit(int status) {
            if (status != 0) {
                TtsEngineSettingsFragment.this.finishFragment();
            } else {
                TtsEngineSettingsFragment.this.getActivity().runOnUiThread(new Runnable() { // from class: com.android.settings.tts.TtsEngineSettingsFragment.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        TtsEngineSettingsFragment.this.mLocalePreference.setEnabled(true);
                    }
                });
            }
        }
    };
    private final BroadcastReceiver mLanguagesChangedReceiver = new BroadcastReceiver() { // from class: com.android.settings.tts.TtsEngineSettingsFragment.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (!"android.speech.tts.engine.TTS_DATA_INSTALLED".equals(intent.getAction())) {
                return;
            }
            TtsEngineSettingsFragment.this.checkTtsData();
        }
    };

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 93;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tts_engine_settings);
        this.mEnginesHelper = new TtsEngines(getActivity());
        PreferenceScreen root = getPreferenceScreen();
        this.mLocalePreference = (ListPreference) root.findPreference("tts_default_lang");
        this.mLocalePreference.setOnPreferenceChangeListener(this);
        this.mEngineSettingsPreference = root.findPreference("tts_engine_settings");
        this.mEngineSettingsPreference.setOnPreferenceClickListener(this);
        this.mInstallVoicesPreference = root.findPreference("tts_install_data");
        this.mInstallVoicesPreference.setOnPreferenceClickListener(this);
        root.setTitle(getEngineLabel());
        root.setKey(getEngineName());
        this.mEngineSettingsPreference.setTitle(getResources().getString(R.string.tts_engine_settings_title, getEngineLabel()));
        this.mEngineSettingsIntent = this.mEnginesHelper.getSettingsIntent(getEngineName());
        if (this.mEngineSettingsIntent == null) {
            this.mEngineSettingsPreference.setEnabled(false);
        }
        this.mInstallVoicesPreference.setEnabled(false);
        if (savedInstanceState == null) {
            this.mLocalePreference.setEnabled(false);
            this.mLocalePreference.setEntries(new CharSequence[0]);
            this.mLocalePreference.setEntryValues(new CharSequence[0]);
        } else {
            CharSequence[] entries = savedInstanceState.getCharSequenceArray("locale_entries");
            CharSequence[] entryValues = savedInstanceState.getCharSequenceArray("locale_entry_values");
            CharSequence value = savedInstanceState.getCharSequence("locale_value");
            this.mLocalePreference.setEntries(entries);
            this.mLocalePreference.setEntryValues(entryValues);
            this.mLocalePreference.setValue(value != null ? value.toString() : null);
            this.mLocalePreference.setEnabled(entries.length > 0);
        }
        this.mVoiceDataDetails = (Intent) getArguments().getParcelable("voices");
        this.mTts = new TextToSpeech(getActivity().getApplicationContext(), this.mTtsInitListener, getEngineName());
        checkTtsData();
        getActivity().registerReceiver(this.mLanguagesChangedReceiver, new IntentFilter("android.speech.tts.engine.TTS_DATA_INSTALLED"));
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        getActivity().unregisterReceiver(this.mLanguagesChangedReceiver);
        this.mTts.shutdown();
        super.onDestroy();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequenceArray("locale_entries", this.mLocalePreference.getEntries());
        outState.putCharSequenceArray("locale_entry_values", this.mLocalePreference.getEntryValues());
        outState.putCharSequence("locale_value", this.mLocalePreference.getValue());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void checkTtsData() {
        Intent intent = new Intent("android.speech.tts.engine.CHECK_TTS_DATA");
        intent.setPackage(getEngineName());
        try {
            startActivityForResult(intent, 1977);
        } catch (ActivityNotFoundException e) {
            Log.e("TtsEngineSettings", "Failed to check TTS data, no activity found for " + intent + ")");
        }
    }

    @Override // android.app.Fragment
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1977) {
            return;
        }
        if (resultCode != 0) {
            updateVoiceDetails(data);
        } else {
            Log.e("TtsEngineSettings", "CheckVoiceData activity failed");
        }
    }

    private void updateVoiceDetails(Intent data) {
        if (data == null) {
            Log.e("TtsEngineSettings", "Engine failed voice data integrity check (null return)" + this.mTts.getCurrentEngine());
            return;
        }
        this.mVoiceDataDetails = data;
        ArrayList<String> available = this.mVoiceDataDetails.getStringArrayListExtra("availableVoices");
        ArrayList<String> unavailable = this.mVoiceDataDetails.getStringArrayListExtra("unavailableVoices");
        if (unavailable != null && unavailable.size() > 0) {
            this.mInstallVoicesPreference.setEnabled(true);
        } else {
            this.mInstallVoicesPreference.setEnabled(false);
        }
        if (available == null) {
            Log.e("TtsEngineSettings", "TTS data check failed (available == null).");
            this.mLocalePreference.setEnabled(false);
            return;
        }
        updateDefaultLocalePref(available);
        Intent market = new Intent("android.intent.action.VIEW", Uri.parse("market://search?q=dummy"));
        PackageManager manager = getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(market, 0);
        if (list.size() != 0) {
            return;
        }
        Log.e("TtsEngineSettings", "Google Market is not installed.");
        this.mEngineSettingsPreference.setEnabled(false);
        this.mInstallVoicesPreference.setEnabled(false);
    }

    private void updateDefaultLocalePref(ArrayList<String> availableLangs) {
        if (availableLangs == null || availableLangs.size() == 0) {
            this.mLocalePreference.setEnabled(false);
            return;
        }
        Locale currentLocale = null;
        if (!this.mEnginesHelper.isLocaleSetToDefaultForEngine(getEngineName())) {
            currentLocale = this.mEnginesHelper.getLocalePrefForEngine(getEngineName());
        }
        ArrayList<Pair<String, Locale>> entryPairs = new ArrayList<>(availableLangs.size());
        for (int i = 0; i < availableLangs.size(); i++) {
            Locale locale = this.mEnginesHelper.parseLocaleString(availableLangs.get(i));
            if (locale != null) {
                entryPairs.add(new Pair<>(locale.getDisplayName(), locale));
            }
        }
        Collections.sort(entryPairs, new Comparator<Pair<String, Locale>>() { // from class: com.android.settings.tts.TtsEngineSettingsFragment.3
            @Override // java.util.Comparator
            public int compare(Pair<String, Locale> lhs, Pair<String, Locale> rhs) {
                return ((String) lhs.first).compareToIgnoreCase((String) rhs.first);
            }
        });
        this.mSelectedLocaleIndex = 0;
        CharSequence[] entries = new CharSequence[availableLangs.size() + 1];
        CharSequence[] entryValues = new CharSequence[availableLangs.size() + 1];
        entries[0] = getActivity().getString(R.string.tts_lang_use_system);
        entryValues[0] = "";
        int i2 = 1;
        for (Pair<String, Locale> entry : entryPairs) {
            if (((Locale) entry.second).equals(currentLocale)) {
                this.mSelectedLocaleIndex = i2;
            }
            entries[i2] = (CharSequence) entry.first;
            entryValues[i2] = ((Locale) entry.second).toString();
            i2++;
        }
        this.mLocalePreference.setEntries(entries);
        this.mLocalePreference.setEntryValues(entryValues);
        this.mLocalePreference.setEnabled(true);
        setLocalePreference(this.mSelectedLocaleIndex);
    }

    private void setLocalePreference(int index) {
        if (index < 0) {
            this.mLocalePreference.setValue("");
            this.mLocalePreference.setSummary(R.string.tts_lang_not_selected);
            return;
        }
        this.mLocalePreference.setValueIndex(index);
        this.mLocalePreference.setSummary(this.mLocalePreference.getEntries()[index]);
    }

    private void installVoiceData() {
        if (TextUtils.isEmpty(getEngineName())) {
            return;
        }
        Intent intent = new Intent("android.speech.tts.engine.INSTALL_TTS_DATA");
        intent.setPackage(getEngineName());
        try {
            Log.v("TtsEngineSettings", "Installing voice data: " + intent.toUri(0));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("TtsEngineSettings", "Failed to install TTS data, no acitivty found for " + intent + ")");
        }
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
    public boolean onPreferenceClick(Preference preference) {
        if (preference == this.mInstallVoicesPreference) {
            installVoiceData();
            return true;
        } else if (preference == this.mEngineSettingsPreference) {
            startActivity(this.mEngineSettingsIntent);
            return true;
        } else {
            return false;
        }
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mLocalePreference) {
            String localeString = (String) newValue;
            updateLanguageTo(!TextUtils.isEmpty(localeString) ? this.mEnginesHelper.parseLocaleString(localeString) : null);
            return true;
        }
        return false;
    }

    private void updateLanguageTo(Locale locale) {
        int selectedLocaleIndex = -1;
        String localeString = locale != null ? locale.toString() : "";
        int i = 0;
        while (true) {
            if (i >= this.mLocalePreference.getEntryValues().length) {
                break;
            } else if (!localeString.equalsIgnoreCase(this.mLocalePreference.getEntryValues()[i].toString())) {
                i++;
            } else {
                selectedLocaleIndex = i;
                break;
            }
        }
        if (selectedLocaleIndex == -1) {
            Log.w("TtsEngineSettings", "updateLanguageTo called with unknown locale argument");
            return;
        }
        this.mLocalePreference.setSummary(this.mLocalePreference.getEntries()[selectedLocaleIndex]);
        this.mSelectedLocaleIndex = selectedLocaleIndex;
        this.mEnginesHelper.updateLocalePrefForEngine(getEngineName(), locale);
        if (!getEngineName().equals(this.mTts.getCurrentEngine())) {
            return;
        }
        TextToSpeech textToSpeech = this.mTts;
        if (locale == null) {
            locale = Locale.getDefault();
        }
        textToSpeech.setLanguage(locale);
    }

    private String getEngineName() {
        return getArguments().getString("name");
    }

    private String getEngineLabel() {
        return getArguments().getString("label");
    }
}
