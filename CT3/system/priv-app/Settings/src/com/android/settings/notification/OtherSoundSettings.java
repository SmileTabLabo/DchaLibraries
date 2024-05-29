package com.android.settings.notification;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.SearchIndexableResource;
import android.telephony.TelephonyManager;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/* loaded from: classes.dex */
public class OtherSoundSettings extends SettingsPreferenceFragment implements Indexable {
    private Context mContext;
    private final SettingsObserver mSettingsObserver = new SettingsObserver();
    private static final SettingPref PREF_DIAL_PAD_TONES = new SettingPref(2, "dial_pad_tones", "dtmf_tone", 1, new int[0]) { // from class: com.android.settings.notification.OtherSoundSettings.1
        @Override // com.android.settings.notification.SettingPref
        public boolean isApplicable(Context context) {
            return Utils.isVoiceCapable(context);
        }
    };
    private static final SettingPref PREF_SCREEN_LOCKING_SOUNDS = new SettingPref(2, "screen_locking_sounds", "lockscreen_sounds_enabled", 1, new int[0]);
    private static final SettingPref PREF_CHARGING_SOUNDS = new SettingPref(1, "charging_sounds", "charging_sounds_enabled", 1, new int[0]) { // from class: com.android.settings.notification.OtherSoundSettings.2
        @Override // com.android.settings.notification.SettingPref
        public boolean isApplicable(Context context) {
            return false;
        }
    };
    private static final SettingPref PREF_DOCKING_SOUNDS = new SettingPref(1, "docking_sounds", "dock_sounds_enabled", 1, new int[0]) { // from class: com.android.settings.notification.OtherSoundSettings.3
        @Override // com.android.settings.notification.SettingPref
        public boolean isApplicable(Context context) {
            return OtherSoundSettings.hasDockSettings(context);
        }
    };
    private static final SettingPref PREF_TOUCH_SOUNDS = new SettingPref(2, "touch_sounds", "sound_effects_enabled", 1, new int[0]) { // from class: com.android.settings.notification.OtherSoundSettings.4
        /* JADX INFO: Access modifiers changed from: protected */
        @Override // com.android.settings.notification.SettingPref
        public boolean setSetting(final Context context, final int value) {
            AsyncTask.execute(new Runnable() { // from class: com.android.settings.notification.OtherSoundSettings.4.1
                @Override // java.lang.Runnable
                public void run() {
                    AudioManager am = (AudioManager) context.getSystemService("audio");
                    if (value != 0) {
                        am.loadSoundEffects();
                    } else {
                        am.unloadSoundEffects();
                    }
                }
            });
            return super.setSetting(context, value);
        }
    };
    private static final SettingPref PREF_VIBRATE_ON_TOUCH = new SettingPref(2, "vibrate_on_touch", "haptic_feedback_enabled", 1, new int[0]) { // from class: com.android.settings.notification.OtherSoundSettings.5
        @Override // com.android.settings.notification.SettingPref
        public boolean isApplicable(Context context) {
            return OtherSoundSettings.hasHaptic(context);
        }
    };
    private static final SettingPref PREF_DOCK_AUDIO_MEDIA = new SettingPref(1, "dock_audio_media", "dock_audio_media_enabled", 0, 0, 1) { // from class: com.android.settings.notification.OtherSoundSettings.6
        @Override // com.android.settings.notification.SettingPref
        public boolean isApplicable(Context context) {
            return OtherSoundSettings.hasDockSettings(context);
        }

        @Override // com.android.settings.notification.SettingPref
        protected String getCaption(Resources res, int value) {
            switch (value) {
                case 0:
                    return res.getString(R.string.dock_audio_media_disabled);
                case 1:
                    return res.getString(R.string.dock_audio_media_enabled);
                default:
                    throw new IllegalArgumentException();
            }
        }
    };
    private static final SettingPref PREF_EMERGENCY_TONE = new SettingPref(1, "emergency_tone", "emergency_tone", 0, 1, 2, 0) { // from class: com.android.settings.notification.OtherSoundSettings.7
        @Override // com.android.settings.notification.SettingPref
        public boolean isApplicable(Context context) {
            int activePhoneType = TelephonyManager.getDefault().getCurrentPhoneType();
            return activePhoneType == 2;
        }

        @Override // com.android.settings.notification.SettingPref
        protected String getCaption(Resources res, int value) {
            switch (value) {
                case 0:
                    return res.getString(R.string.emergency_tone_silent);
                case 1:
                    return res.getString(R.string.emergency_tone_alert);
                case 2:
                    return res.getString(R.string.emergency_tone_vibrate);
                default:
                    throw new IllegalArgumentException();
            }
        }
    };
    private static final SettingPref[] PREFS = {PREF_DIAL_PAD_TONES, PREF_SCREEN_LOCKING_SOUNDS, PREF_CHARGING_SOUNDS, PREF_DOCKING_SOUNDS, PREF_TOUCH_SOUNDS, PREF_VIBRATE_ON_TOUCH, PREF_DOCK_AUDIO_MEDIA, PREF_EMERGENCY_TONE};
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.android.settings.notification.OtherSoundSettings.8
        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.other_sound_settings;
            return Arrays.asList(sir);
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<String> getNonIndexableKeys(Context context) {
            SettingPref[] settingPrefArr;
            ArrayList<String> rt = new ArrayList<>();
            for (SettingPref pref : OtherSoundSettings.PREFS) {
                if (!pref.isApplicable(context)) {
                    rt.add(pref.getKey());
                }
            }
            return rt;
        }
    };

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 73;
    }

    @Override // com.android.settings.SettingsPreferenceFragment
    protected int getHelpResource() {
        return R.string.help_uri_other_sounds;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        SettingPref[] settingPrefArr;
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.other_sound_settings);
        this.mContext = getActivity();
        for (SettingPref pref : PREFS) {
            pref.init(this);
        }
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mSettingsObserver.register(true);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mSettingsObserver.register(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean hasDockSettings(Context context) {
        return context.getResources().getBoolean(R.bool.has_dock_settings);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean hasHaptic(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
        if (vibrator != null) {
            return vibrator.hasVibrator();
        }
        return false;
    }

    /* loaded from: classes.dex */
    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver() {
            super(new Handler());
        }

        public void register(boolean register) {
            SettingPref[] settingPrefArr;
            ContentResolver cr = OtherSoundSettings.this.getContentResolver();
            if (register) {
                for (SettingPref pref : OtherSoundSettings.PREFS) {
                    cr.registerContentObserver(pref.getUri(), false, this);
                }
                return;
            }
            cr.unregisterContentObserver(this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            SettingPref[] settingPrefArr;
            super.onChange(selfChange, uri);
            for (SettingPref pref : OtherSoundSettings.PREFS) {
                if (pref.getUri().equals(uri)) {
                    pref.update(OtherSoundSettings.this.mContext);
                    return;
                }
            }
        }
    }
}
