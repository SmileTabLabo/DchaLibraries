package android.support.v7.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.SharedPreferencesCompat$EditorCompat;
/* loaded from: a.zip:android/support/v7/preference/PreferenceManager.class */
public class PreferenceManager {
    private Context mContext;
    private SharedPreferences.Editor mEditor;
    private boolean mNoCommit;
    private OnDisplayPreferenceDialogListener mOnDisplayPreferenceDialogListener;
    private OnNavigateToScreenListener mOnNavigateToScreenListener;
    private OnPreferenceTreeClickListener mOnPreferenceTreeClickListener;
    private PreferenceScreen mPreferenceScreen;
    private SharedPreferences mSharedPreferences;
    private int mSharedPreferencesMode;
    private String mSharedPreferencesName;
    private long mNextId = 0;
    private int mStorage = 0;

    /* loaded from: a.zip:android/support/v7/preference/PreferenceManager$OnDisplayPreferenceDialogListener.class */
    public interface OnDisplayPreferenceDialogListener {
        void onDisplayPreferenceDialog(Preference preference);
    }

    /* loaded from: a.zip:android/support/v7/preference/PreferenceManager$OnNavigateToScreenListener.class */
    public interface OnNavigateToScreenListener {
        void onNavigateToScreen(PreferenceScreen preferenceScreen);
    }

    /* loaded from: a.zip:android/support/v7/preference/PreferenceManager$OnPreferenceTreeClickListener.class */
    public interface OnPreferenceTreeClickListener {
        boolean onPreferenceTreeClick(Preference preference);
    }

    public PreferenceManager(Context context) {
        this.mContext = context;
        setSharedPreferencesName(getDefaultSharedPreferencesName(context));
    }

    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    private void setNoCommit(boolean z) {
        if (!z && this.mEditor != null) {
            SharedPreferencesCompat$EditorCompat.getInstance().apply(this.mEditor);
        }
        this.mNoCommit = z;
    }

    public PreferenceScreen createPreferenceScreen(Context context) {
        PreferenceScreen preferenceScreen = new PreferenceScreen(context, null);
        preferenceScreen.onAttachedToHierarchy(this);
        return preferenceScreen;
    }

    public Preference findPreference(CharSequence charSequence) {
        if (this.mPreferenceScreen == null) {
            return null;
        }
        return this.mPreferenceScreen.findPreference(charSequence);
    }

    public Context getContext() {
        return this.mContext;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public SharedPreferences.Editor getEditor() {
        if (this.mNoCommit) {
            if (this.mEditor == null) {
                this.mEditor = getSharedPreferences().edit();
            }
            return this.mEditor;
        }
        return getSharedPreferences().edit();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public long getNextId() {
        long j;
        synchronized (this) {
            j = this.mNextId;
            this.mNextId = 1 + j;
        }
        return j;
    }

    public OnNavigateToScreenListener getOnNavigateToScreenListener() {
        return this.mOnNavigateToScreenListener;
    }

    public OnPreferenceTreeClickListener getOnPreferenceTreeClickListener() {
        return this.mOnPreferenceTreeClickListener;
    }

    public PreferenceScreen getPreferenceScreen() {
        return this.mPreferenceScreen;
    }

    public SharedPreferences getSharedPreferences() {
        Context createDeviceProtectedStorageContext;
        if (this.mSharedPreferences == null) {
            switch (this.mStorage) {
                case 1:
                    createDeviceProtectedStorageContext = ContextCompat.createDeviceProtectedStorageContext(this.mContext);
                    break;
                default:
                    createDeviceProtectedStorageContext = this.mContext;
                    break;
            }
            this.mSharedPreferences = createDeviceProtectedStorageContext.getSharedPreferences(this.mSharedPreferencesName, this.mSharedPreferencesMode);
        }
        return this.mSharedPreferences;
    }

    public PreferenceScreen inflateFromResource(Context context, int i, PreferenceScreen preferenceScreen) {
        setNoCommit(true);
        PreferenceScreen preferenceScreen2 = (PreferenceScreen) new PreferenceInflater(context, this).inflate(i, preferenceScreen);
        preferenceScreen2.onAttachedToHierarchy(this);
        setNoCommit(false);
        return preferenceScreen2;
    }

    public void setOnDisplayPreferenceDialogListener(OnDisplayPreferenceDialogListener onDisplayPreferenceDialogListener) {
        this.mOnDisplayPreferenceDialogListener = onDisplayPreferenceDialogListener;
    }

    public void setOnNavigateToScreenListener(OnNavigateToScreenListener onNavigateToScreenListener) {
        this.mOnNavigateToScreenListener = onNavigateToScreenListener;
    }

    public void setOnPreferenceTreeClickListener(OnPreferenceTreeClickListener onPreferenceTreeClickListener) {
        this.mOnPreferenceTreeClickListener = onPreferenceTreeClickListener;
    }

    public boolean setPreferences(PreferenceScreen preferenceScreen) {
        if (preferenceScreen != this.mPreferenceScreen) {
            if (this.mPreferenceScreen != null) {
                this.mPreferenceScreen.onDetached();
            }
            this.mPreferenceScreen = preferenceScreen;
            return true;
        }
        return false;
    }

    public void setSharedPreferencesName(String str) {
        this.mSharedPreferencesName = str;
        this.mSharedPreferences = null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean shouldCommit() {
        return !this.mNoCommit;
    }

    public void showDialog(Preference preference) {
        if (this.mOnDisplayPreferenceDialogListener != null) {
            this.mOnDisplayPreferenceDialogListener.onDisplayPreferenceDialog(preference);
        }
    }
}
