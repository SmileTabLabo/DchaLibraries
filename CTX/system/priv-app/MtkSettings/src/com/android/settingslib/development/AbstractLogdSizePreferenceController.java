package com.android.settingslib.development;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settingslib.R;
/* loaded from: classes.dex */
public abstract class AbstractLogdSizePreferenceController extends DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener {
    static final String DEFAULT_SNET_TAG = "I";
    static final String LOW_RAM_CONFIG_PROPERTY_KEY = "ro.config.low_ram";
    static final String SELECT_LOGD_DEFAULT_SIZE_VALUE = "262144";
    static final String SELECT_LOGD_MINIMUM_SIZE_VALUE = "65536";
    static final String SELECT_LOGD_SIZE_PROPERTY = "persist.logd.size";
    static final String SELECT_LOGD_SNET_TAG_PROPERTY = "persist.log.tag.snet_event_log";
    private ListPreference mLogdSize;

    public AbstractLogdSizePreferenceController(Context context) {
        super(context);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "select_logd_size";
    }

    @Override // com.android.settingslib.development.DeveloperOptionsPreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        if (isAvailable()) {
            this.mLogdSize = (ListPreference) preferenceScreen.findPreference("select_logd_size");
        }
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        if (preference == this.mLogdSize) {
            writeLogdSizeOption(obj);
            return true;
        }
        return false;
    }

    private String defaultLogdSizeValue() {
        String str = SystemProperties.get("ro.logd.size");
        if (str == null || str.length() == 0) {
            if (SystemProperties.get(LOW_RAM_CONFIG_PROPERTY_KEY).equals("true")) {
                return SELECT_LOGD_MINIMUM_SIZE_VALUE;
            }
            return SELECT_LOGD_DEFAULT_SIZE_VALUE;
        }
        return str;
    }

    public void updateLogdSizeValues() {
        if (this.mLogdSize != null) {
            String str = SystemProperties.get("persist.log.tag");
            String str2 = SystemProperties.get(SELECT_LOGD_SIZE_PROPERTY);
            if (str != null && str.startsWith("Settings")) {
                str2 = "32768";
            }
            LocalBroadcastManager.getInstance(this.mContext).sendBroadcastSync(new Intent("com.android.settingslib.development.AbstractLogdSizePreferenceController.LOGD_SIZE_UPDATED").putExtra("CURRENT_LOGD_VALUE", str2));
            if (str2 == null || str2.length() == 0) {
                str2 = defaultLogdSizeValue();
            }
            String[] stringArray = this.mContext.getResources().getStringArray(R.array.select_logd_size_values);
            String[] stringArray2 = this.mContext.getResources().getStringArray(R.array.select_logd_size_titles);
            int i = 2;
            if (SystemProperties.get(LOW_RAM_CONFIG_PROPERTY_KEY).equals("true")) {
                this.mLogdSize.setEntries(R.array.select_logd_size_lowram_titles);
                stringArray2 = this.mContext.getResources().getStringArray(R.array.select_logd_size_lowram_titles);
                i = 1;
            }
            String[] stringArray3 = this.mContext.getResources().getStringArray(R.array.select_logd_size_summaries);
            for (int i2 = 0; i2 < stringArray2.length; i2++) {
                if (str2.equals(stringArray[i2]) || str2.equals(stringArray2[i2])) {
                    i = i2;
                    break;
                }
            }
            this.mLogdSize.setValue(stringArray[i]);
            this.mLogdSize.setSummary(stringArray3[i]);
        }
    }

    public void writeLogdSizeOption(Object obj) {
        String str;
        String str2;
        boolean z = obj != null && obj.toString().equals("32768");
        String str3 = SystemProperties.get("persist.log.tag");
        if (str3 == null) {
            str3 = "";
        }
        String replaceFirst = str3.replaceAll(",+Settings", "").replaceFirst("^Settings,*", "").replaceAll(",+", ",").replaceFirst(",+$", "");
        if (z) {
            obj = SELECT_LOGD_MINIMUM_SIZE_VALUE;
            String str4 = SystemProperties.get(SELECT_LOGD_SNET_TAG_PROPERTY);
            if ((str4 == null || str4.length() == 0) && ((str2 = SystemProperties.get("log.tag.snet_event_log")) == null || str2.length() == 0)) {
                SystemProperties.set(SELECT_LOGD_SNET_TAG_PROPERTY, DEFAULT_SNET_TAG);
            }
            if (replaceFirst.length() != 0) {
                replaceFirst = "," + replaceFirst;
            }
            replaceFirst = "Settings" + replaceFirst;
        }
        if (!replaceFirst.equals(str3)) {
            SystemProperties.set("persist.log.tag", replaceFirst);
        }
        String defaultLogdSizeValue = defaultLogdSizeValue();
        if (obj != null && obj.toString().length() != 0) {
            str = obj.toString();
        } else {
            str = defaultLogdSizeValue;
        }
        if (defaultLogdSizeValue.equals(str)) {
            str = "";
        }
        SystemProperties.set(SELECT_LOGD_SIZE_PROPERTY, str);
        SystemProperties.set("ctl.start", "logd-reinit");
        SystemPropPoker.getInstance().poke();
        updateLogdSizeValues();
    }
}
