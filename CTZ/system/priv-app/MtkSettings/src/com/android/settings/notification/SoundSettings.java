package com.android.settings.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.preference.SeekBarVolumizer;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.RingtonePreference;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.notification.VolumeSeekBarPreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.sound.AudioSwitchPreferenceController;
import com.android.settings.sound.HandsFreeProfileOutputPreferenceController;
import com.android.settings.sound.MediaOutputPreferenceController;
import com.android.settings.widget.PreferenceCategoryController;
import com.android.settings.widget.UpdatableListPreferenceDialogFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
/* loaded from: classes.dex */
public class SoundSettings extends DashboardFragment {
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.android.settings.notification.SoundSettings.2
        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean z) {
            SearchIndexableResource searchIndexableResource = new SearchIndexableResource(context);
            searchIndexableResource.xmlResId = R.xml.sound_settings;
            return Arrays.asList(searchIndexableResource);
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider
        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return SoundSettings.buildPreferenceControllers(context, null, null);
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<String> getNonIndexableKeys(Context context) {
            List<String> nonIndexableKeys = super.getNonIndexableKeys(context);
            nonIndexableKeys.add(new ZenModePreferenceController(context, null, "zen_mode").getPreferenceKey());
            return nonIndexableKeys;
        }
    };
    static final int STOP_SAMPLE = 1;
    private UpdatableListPreferenceDialogFragment mDialogFragment;
    private String mHfpOutputControllerKey;
    private String mMediaOutputControllerKey;
    private RingtonePreference mRequestPreference;
    final VolumePreferenceCallback mVolumeCallback = new VolumePreferenceCallback();
    final Handler mHandler = new Handler(Looper.getMainLooper()) { // from class: com.android.settings.notification.SoundSettings.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1) {
                SoundSettings.this.mVolumeCallback.stopSample();
            }
        }
    };

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 336;
    }

    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle != null) {
            String string = bundle.getString("selected_preference", null);
            if (!TextUtils.isEmpty(string)) {
                this.mRequestPreference = (RingtonePreference) findPreference(string);
            }
            this.mDialogFragment = (UpdatableListPreferenceDialogFragment) getFragmentManager().findFragmentByTag("SoundSettings");
        }
    }

    @Override // com.android.settings.support.actionbar.HelpResourceProvider
    public int getHelpResource() {
        return R.string.help_url_sound;
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mVolumeCallback.stopSample();
    }

    @Override // com.android.settings.dashboard.DashboardFragment, android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RingtonePreference) {
            this.mRequestPreference = (RingtonePreference) preference;
            this.mRequestPreference.onPrepareRingtonePickerIntent(this.mRequestPreference.getIntent());
            startActivityForResultAsUser(this.mRequestPreference.getIntent(), 200, null, UserHandle.of(this.mRequestPreference.getUserId()));
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnDisplayPreferenceDialogListener
    public void onDisplayPreferenceDialog(Preference preference) {
        int i;
        if (this.mHfpOutputControllerKey.equals(preference.getKey())) {
            i = 1416;
        } else if (this.mMediaOutputControllerKey.equals(preference.getKey())) {
            i = 1415;
        } else {
            i = 0;
        }
        this.mDialogFragment = UpdatableListPreferenceDialogFragment.newInstance(preference.getKey(), i);
        this.mDialogFragment.setTargetFragment(this, 0);
        this.mDialogFragment.show(getFragmentManager(), "SoundSettings");
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected String getLogTag() {
        return "SoundSettings";
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.sound_settings;
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, this, getLifecycle());
    }

    @Override // android.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        if (this.mRequestPreference != null) {
            this.mRequestPreference.onActivityResult(i, i2, intent);
            this.mRequestPreference = null;
        }
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (this.mRequestPreference != null) {
            bundle.putString("selected_preference", this.mRequestPreference.getKey());
        }
    }

    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        ArrayList arrayList = new ArrayList();
        arrayList.add((VolumeSeekBarPreferenceController) use(AlarmVolumePreferenceController.class));
        arrayList.add((VolumeSeekBarPreferenceController) use(MediaVolumePreferenceController.class));
        arrayList.add((VolumeSeekBarPreferenceController) use(RingVolumePreferenceController.class));
        arrayList.add((VolumeSeekBarPreferenceController) use(NotificationVolumePreferenceController.class));
        arrayList.add((VolumeSeekBarPreferenceController) use(CallVolumePreferenceController.class));
        ((MediaOutputPreferenceController) use(MediaOutputPreferenceController.class)).setCallback(new AudioSwitchPreferenceController.AudioSwitchCallback() { // from class: com.android.settings.notification.-$$Lambda$SoundSettings$jhjL65jex6M9bCQGfPcGogvuH7o
            @Override // com.android.settings.sound.AudioSwitchPreferenceController.AudioSwitchCallback
            public final void onPreferenceDataChanged(ListPreference listPreference) {
                SoundSettings.this.onPreferenceDataChanged(listPreference);
            }
        });
        this.mMediaOutputControllerKey = ((MediaOutputPreferenceController) use(MediaOutputPreferenceController.class)).getPreferenceKey();
        ((HandsFreeProfileOutputPreferenceController) use(HandsFreeProfileOutputPreferenceController.class)).setCallback(new AudioSwitchPreferenceController.AudioSwitchCallback() { // from class: com.android.settings.notification.-$$Lambda$SoundSettings$P7n4dsft5tPdi8YUFcItX8K9whw
            @Override // com.android.settings.sound.AudioSwitchPreferenceController.AudioSwitchCallback
            public final void onPreferenceDataChanged(ListPreference listPreference) {
                SoundSettings.this.onPreferenceDataChanged(listPreference);
            }
        });
        this.mHfpOutputControllerKey = ((HandsFreeProfileOutputPreferenceController) use(HandsFreeProfileOutputPreferenceController.class)).getPreferenceKey();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            VolumeSeekBarPreferenceController volumeSeekBarPreferenceController = (VolumeSeekBarPreferenceController) it.next();
            volumeSeekBarPreferenceController.setCallback(this.mVolumeCallback);
            getLifecycle().addObserver(volumeSeekBarPreferenceController);
        }
    }

    /* loaded from: classes.dex */
    final class VolumePreferenceCallback implements VolumeSeekBarPreference.Callback {
        private SeekBarVolumizer mCurrent;

        VolumePreferenceCallback() {
        }

        @Override // com.android.settings.notification.VolumeSeekBarPreference.Callback
        public void onSampleStarting(SeekBarVolumizer seekBarVolumizer) {
            if (this.mCurrent != null && this.mCurrent != seekBarVolumizer) {
                this.mCurrent.stopSample();
            }
            this.mCurrent = seekBarVolumizer;
            if (this.mCurrent != null) {
                SoundSettings.this.mHandler.removeMessages(1);
                SoundSettings.this.mHandler.sendEmptyMessageDelayed(1, 2000L);
            }
        }

        @Override // com.android.settings.notification.VolumeSeekBarPreference.Callback
        public void onStreamValueChanged(int i, int i2) {
            if (this.mCurrent != null) {
                SoundSettings.this.mHandler.removeMessages(1);
                SoundSettings.this.mHandler.sendEmptyMessageDelayed(1, 2000L);
            }
        }

        public void stopSample() {
            if (this.mCurrent != null) {
                this.mCurrent.stopSample();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static List<AbstractPreferenceController> buildPreferenceControllers(Context context, SoundSettings soundSettings, Lifecycle lifecycle) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new ZenModePreferenceController(context, lifecycle, "zen_mode"));
        arrayList.add(new PhoneRingtonePreferenceController(context));
        arrayList.add(new AlarmRingtonePreferenceController(context));
        arrayList.add(new NotificationRingtonePreferenceController(context));
        arrayList.add(new WorkSoundPreferenceController(context, soundSettings, lifecycle));
        DialPadTonePreferenceController dialPadTonePreferenceController = new DialPadTonePreferenceController(context, soundSettings, lifecycle);
        ScreenLockSoundPreferenceController screenLockSoundPreferenceController = new ScreenLockSoundPreferenceController(context, soundSettings, lifecycle);
        ChargingSoundPreferenceController chargingSoundPreferenceController = new ChargingSoundPreferenceController(context, soundSettings, lifecycle);
        DockingSoundPreferenceController dockingSoundPreferenceController = new DockingSoundPreferenceController(context, soundSettings, lifecycle);
        TouchSoundPreferenceController touchSoundPreferenceController = new TouchSoundPreferenceController(context, soundSettings, lifecycle);
        VibrateOnTouchPreferenceController vibrateOnTouchPreferenceController = new VibrateOnTouchPreferenceController(context, soundSettings, lifecycle);
        DockAudioMediaPreferenceController dockAudioMediaPreferenceController = new DockAudioMediaPreferenceController(context, soundSettings, lifecycle);
        BootSoundPreferenceController bootSoundPreferenceController = new BootSoundPreferenceController(context);
        EmergencyTonePreferenceController emergencyTonePreferenceController = new EmergencyTonePreferenceController(context, soundSettings, lifecycle);
        arrayList.add(dialPadTonePreferenceController);
        arrayList.add(screenLockSoundPreferenceController);
        arrayList.add(chargingSoundPreferenceController);
        arrayList.add(dockingSoundPreferenceController);
        arrayList.add(touchSoundPreferenceController);
        arrayList.add(vibrateOnTouchPreferenceController);
        arrayList.add(dockAudioMediaPreferenceController);
        arrayList.add(bootSoundPreferenceController);
        arrayList.add(emergencyTonePreferenceController);
        arrayList.add(new PreferenceCategoryController(context, "other_sounds_and_vibrations_category").setChildren(Arrays.asList(dialPadTonePreferenceController, screenLockSoundPreferenceController, chargingSoundPreferenceController, dockingSoundPreferenceController, touchSoundPreferenceController, vibrateOnTouchPreferenceController, dockAudioMediaPreferenceController, bootSoundPreferenceController, emergencyTonePreferenceController)));
        return arrayList;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void enableWorkSync() {
        WorkSoundPreferenceController workSoundPreferenceController = (WorkSoundPreferenceController) use(WorkSoundPreferenceController.class);
        if (workSoundPreferenceController != null) {
            workSoundPreferenceController.enableWorkSync();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPreferenceDataChanged(ListPreference listPreference) {
        if (this.mDialogFragment != null) {
            this.mDialogFragment.onListPreferenceUpdated(listPreference);
        }
    }
}
