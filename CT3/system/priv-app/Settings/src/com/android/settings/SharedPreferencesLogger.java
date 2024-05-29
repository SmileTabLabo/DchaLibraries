package com.android.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.android.internal.logging.MetricsLogger;
import java.util.Map;
import java.util.Set;
/* loaded from: classes.dex */
public class SharedPreferencesLogger implements SharedPreferences {
    private final Context mContext;
    private final String mTag;

    public SharedPreferencesLogger(Context context, String tag) {
        this.mContext = context;
        this.mTag = tag;
    }

    @Override // android.content.SharedPreferences
    public Map<String, ?> getAll() {
        return null;
    }

    @Override // android.content.SharedPreferences
    public String getString(String key, String defValue) {
        return defValue;
    }

    @Override // android.content.SharedPreferences
    public Set<String> getStringSet(String key, Set<String> defValues) {
        return defValues;
    }

    @Override // android.content.SharedPreferences
    public int getInt(String key, int defValue) {
        return defValue;
    }

    @Override // android.content.SharedPreferences
    public long getLong(String key, long defValue) {
        return defValue;
    }

    @Override // android.content.SharedPreferences
    public float getFloat(String key, float defValue) {
        return defValue;
    }

    @Override // android.content.SharedPreferences
    public boolean getBoolean(String key, boolean defValue) {
        return defValue;
    }

    @Override // android.content.SharedPreferences
    public boolean contains(String key) {
        return false;
    }

    @Override // android.content.SharedPreferences
    public SharedPreferences.Editor edit() {
        return new EditorLogger();
    }

    @Override // android.content.SharedPreferences
    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
    }

    @Override // android.content.SharedPreferences
    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void logValue(String key, String value) {
        MetricsLogger.count(this.mContext, this.mTag + "/" + key + "|" + value, 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void logPackageName(String key, String value) {
        MetricsLogger.count(this.mContext, this.mTag + "/" + key, 1);
        MetricsLogger.action(this.mContext, 350, this.mTag + "/" + key + "|" + value);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void safeLogValue(String key, String value) {
        new AsyncPackageCheck(this, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, key, value);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class AsyncPackageCheck extends AsyncTask<String, Void, Void> {
        /* synthetic */ AsyncPackageCheck(SharedPreferencesLogger this$0, AsyncPackageCheck asyncPackageCheck) {
            this();
        }

        private AsyncPackageCheck() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Void doInBackground(String... params) {
            String key = params[0];
            String value = params[1];
            PackageManager pm = SharedPreferencesLogger.this.mContext.getPackageManager();
            try {
                ComponentName name = ComponentName.unflattenFromString(value);
                if (value != null) {
                    value = name.getPackageName();
                }
            } catch (Exception e) {
            }
            try {
                pm.getPackageInfo(value, 8192);
                SharedPreferencesLogger.this.logPackageName(key, value);
            } catch (PackageManager.NameNotFoundException e2) {
                SharedPreferencesLogger.this.logValue(key, value);
            }
            return null;
        }
    }

    /* loaded from: classes.dex */
    public class EditorLogger implements SharedPreferences.Editor {
        public EditorLogger() {
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putString(String key, String value) {
            SharedPreferencesLogger.this.safeLogValue(key, value);
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putStringSet(String key, Set<String> values) {
            SharedPreferencesLogger.this.safeLogValue(key, TextUtils.join(",", values));
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putInt(String key, int value) {
            SharedPreferencesLogger.this.logValue(key, String.valueOf(value));
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putLong(String key, long value) {
            SharedPreferencesLogger.this.logValue(key, String.valueOf(value));
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putFloat(String key, float value) {
            SharedPreferencesLogger.this.logValue(key, String.valueOf(value));
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            SharedPreferencesLogger.this.logValue(key, String.valueOf(value));
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor remove(String key) {
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
