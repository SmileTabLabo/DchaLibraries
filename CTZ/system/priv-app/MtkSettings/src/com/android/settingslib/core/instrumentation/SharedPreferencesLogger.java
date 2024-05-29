package com.android.settingslib.core.instrumentation;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.settingslib.wifi.AccessPoint;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
/* loaded from: classes.dex */
public class SharedPreferencesLogger implements SharedPreferences {
    private final Context mContext;
    private final MetricsFeatureProvider mMetricsFeature;
    private final Set<String> mPreferenceKeySet = new ConcurrentSkipListSet();
    private final String mTag;

    public SharedPreferencesLogger(Context context, String str, MetricsFeatureProvider metricsFeatureProvider) {
        this.mContext = context;
        this.mTag = str;
        this.mMetricsFeature = metricsFeatureProvider;
    }

    @Override // android.content.SharedPreferences
    public Map<String, ?> getAll() {
        return null;
    }

    @Override // android.content.SharedPreferences
    public String getString(String str, String str2) {
        return str2;
    }

    @Override // android.content.SharedPreferences
    public Set<String> getStringSet(String str, Set<String> set) {
        return set;
    }

    @Override // android.content.SharedPreferences
    public int getInt(String str, int i) {
        return i;
    }

    @Override // android.content.SharedPreferences
    public long getLong(String str, long j) {
        return j;
    }

    @Override // android.content.SharedPreferences
    public float getFloat(String str, float f) {
        return f;
    }

    @Override // android.content.SharedPreferences
    public boolean getBoolean(String str, boolean z) {
        return z;
    }

    @Override // android.content.SharedPreferences
    public boolean contains(String str) {
        return false;
    }

    @Override // android.content.SharedPreferences
    public SharedPreferences.Editor edit() {
        return new EditorLogger();
    }

    @Override // android.content.SharedPreferences
    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
    }

    @Override // android.content.SharedPreferences
    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void logValue(String str, Object obj) {
        logValue(str, obj, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void logValue(String str, Object obj, boolean z) {
        int intValue;
        String buildPrefKey = buildPrefKey(this.mTag, str);
        if (!z && !this.mPreferenceKeySet.contains(buildPrefKey)) {
            this.mPreferenceKeySet.add(buildPrefKey);
            return;
        }
        this.mMetricsFeature.count(this.mContext, buildCountName(buildPrefKey, obj), 1);
        Pair<Integer, Object> pair = null;
        if (obj instanceof Long) {
            Long l = (Long) obj;
            if (l.longValue() > 2147483647L) {
                intValue = Preference.DEFAULT_ORDER;
            } else if (l.longValue() < -2147483648L) {
                intValue = AccessPoint.UNREACHABLE_RSSI;
            } else {
                intValue = l.intValue();
            }
            pair = Pair.create(1089, Integer.valueOf(intValue));
        } else if (obj instanceof Integer) {
            pair = Pair.create(1089, obj);
        } else if (obj instanceof Boolean) {
            pair = Pair.create(1089, Integer.valueOf(((Boolean) obj).booleanValue() ? 1 : 0));
        } else if (obj instanceof Float) {
            pair = Pair.create(995, obj);
        } else if (obj instanceof String) {
            Log.d("SharedPreferencesLogger", "Tried to log string preference " + buildPrefKey + " = " + obj);
        } else {
            Log.w("SharedPreferencesLogger", "Tried to log unloggable object" + obj);
        }
        if (pair != null) {
            this.mMetricsFeature.action(this.mContext, 853, Pair.create(854, buildPrefKey), pair);
        }
    }

    void logPackageName(String str, String str2) {
        this.mMetricsFeature.action(this.mContext, 853, str2, Pair.create(854, this.mTag + "/" + str));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void safeLogValue(String str, String str2) {
        new AsyncPackageCheck().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, str, str2);
    }

    public static String buildCountName(String str, Object obj) {
        return str + "|" + obj;
    }

    public static String buildPrefKey(String str, String str2) {
        return str + "/" + str2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class AsyncPackageCheck extends AsyncTask<String, Void, Void> {
        private AsyncPackageCheck() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Void doInBackground(String... strArr) {
            String str = strArr[0];
            String str2 = strArr[1];
            PackageManager packageManager = SharedPreferencesLogger.this.mContext.getPackageManager();
            try {
                ComponentName unflattenFromString = ComponentName.unflattenFromString(str2);
                if (str2 != null) {
                    str2 = unflattenFromString.getPackageName();
                }
            } catch (Exception e) {
            }
            try {
                packageManager.getPackageInfo(str2, 4194304);
                SharedPreferencesLogger.this.logPackageName(str, str2);
                return null;
            } catch (PackageManager.NameNotFoundException e2) {
                SharedPreferencesLogger.this.logValue(str, str2, true);
                return null;
            }
        }
    }

    /* loaded from: classes.dex */
    public class EditorLogger implements SharedPreferences.Editor {
        public EditorLogger() {
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putString(String str, String str2) {
            SharedPreferencesLogger.this.safeLogValue(str, str2);
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putStringSet(String str, Set<String> set) {
            SharedPreferencesLogger.this.safeLogValue(str, TextUtils.join(",", set));
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putInt(String str, int i) {
            SharedPreferencesLogger.this.logValue(str, Integer.valueOf(i));
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putLong(String str, long j) {
            SharedPreferencesLogger.this.logValue(str, Long.valueOf(j));
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putFloat(String str, float f) {
            SharedPreferencesLogger.this.logValue(str, Float.valueOf(f));
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putBoolean(String str, boolean z) {
            SharedPreferencesLogger.this.logValue(str, Boolean.valueOf(z));
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor remove(String str) {
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor clear() {
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public boolean commit() {
            return true;
        }

        @Override // android.content.SharedPreferences.Editor
        public void apply() {
        }
    }
}
