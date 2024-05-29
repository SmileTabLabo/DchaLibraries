package android.support.v7.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;
import android.util.AttributeSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/* loaded from: a.zip:android/support/v7/preference/PreferenceGroup.class */
public abstract class PreferenceGroup extends Preference {
    private boolean mAttachedToHierarchy;
    private final Runnable mClearRecycleCacheRunnable;
    private int mCurrentPreferenceOrder;
    private final Handler mHandler;
    private final SimpleArrayMap<String, Long> mIdRecycleCache;
    private boolean mOrderingAsAdded;
    private List<Preference> mPreferenceList;

    public PreferenceGroup(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PreferenceGroup(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public PreferenceGroup(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mOrderingAsAdded = true;
        this.mCurrentPreferenceOrder = 0;
        this.mAttachedToHierarchy = false;
        this.mIdRecycleCache = new SimpleArrayMap<>();
        this.mHandler = new Handler();
        this.mClearRecycleCacheRunnable = new Runnable(this) { // from class: android.support.v7.preference.PreferenceGroup.1
            final PreferenceGroup this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                synchronized (this) {
                    this.this$0.mIdRecycleCache.clear();
                }
            }
        };
        this.mPreferenceList = new ArrayList();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.PreferenceGroup, i, i2);
        this.mOrderingAsAdded = TypedArrayUtils.getBoolean(obtainStyledAttributes, R$styleable.PreferenceGroup_orderingFromXml, R$styleable.PreferenceGroup_orderingFromXml, true);
        obtainStyledAttributes.recycle();
    }

    public void addItemFromInflater(Preference preference) {
        addPreference(preference);
    }

    public boolean addPreference(Preference preference) {
        long nextId;
        if (this.mPreferenceList.contains(preference)) {
            return true;
        }
        if (preference.getOrder() == Integer.MAX_VALUE) {
            if (this.mOrderingAsAdded) {
                int i = this.mCurrentPreferenceOrder;
                this.mCurrentPreferenceOrder = i + 1;
                preference.setOrder(i);
            }
            if (preference instanceof PreferenceGroup) {
                ((PreferenceGroup) preference).setOrderingAsAdded(this.mOrderingAsAdded);
            }
        }
        int binarySearch = Collections.binarySearch(this.mPreferenceList, preference);
        int i2 = binarySearch;
        if (binarySearch < 0) {
            i2 = (binarySearch * (-1)) - 1;
        }
        if (onPrepareAddPreference(preference)) {
            synchronized (this) {
                this.mPreferenceList.add(i2, preference);
            }
            PreferenceManager preferenceManager = getPreferenceManager();
            String key = preference.getKey();
            if (key == null || !this.mIdRecycleCache.containsKey(key)) {
                nextId = preferenceManager.getNextId();
            } else {
                nextId = this.mIdRecycleCache.get(key).longValue();
                this.mIdRecycleCache.remove(key);
            }
            preference.onAttachedToHierarchy(preferenceManager, nextId);
            if (this.mAttachedToHierarchy) {
                preference.onAttached();
            }
            notifyHierarchyChanged();
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void dispatchRestoreInstanceState(Bundle bundle) {
        super.dispatchRestoreInstanceState(bundle);
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).dispatchRestoreInstanceState(bundle);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void dispatchSaveInstanceState(Bundle bundle) {
        super.dispatchSaveInstanceState(bundle);
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).dispatchSaveInstanceState(bundle);
        }
    }

    public Preference findPreference(CharSequence charSequence) {
        Preference findPreference;
        if (TextUtils.equals(getKey(), charSequence)) {
            return this;
        }
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = getPreference(i);
            String key = preference.getKey();
            if (key != null && key.equals(charSequence)) {
                return preference;
            }
            if ((preference instanceof PreferenceGroup) && (findPreference = ((PreferenceGroup) preference).findPreference(charSequence)) != null) {
                return findPreference;
            }
        }
        return null;
    }

    public Preference getPreference(int i) {
        return this.mPreferenceList.get(i);
    }

    public int getPreferenceCount() {
        return this.mPreferenceList.size();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isOnSameScreenAsChildren() {
        return true;
    }

    @Override // android.support.v7.preference.Preference
    public void notifyDependencyChange(boolean z) {
        super.notifyDependencyChange(z);
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).onParentChanged(this, z);
        }
    }

    @Override // android.support.v7.preference.Preference
    public void onAttached() {
        super.onAttached();
        this.mAttachedToHierarchy = true;
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).onAttached();
        }
    }

    @Override // android.support.v7.preference.Preference
    public void onDetached() {
        super.onDetached();
        this.mAttachedToHierarchy = false;
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).onDetached();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean onPrepareAddPreference(Preference preference) {
        preference.onParentChanged(this, shouldDisableDependents());
        return true;
    }

    public void setOrderingAsAdded(boolean z) {
        this.mOrderingAsAdded = z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void sortPreferences() {
        synchronized (this) {
            Collections.sort(this.mPreferenceList);
        }
    }
}
