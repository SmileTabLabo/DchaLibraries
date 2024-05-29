package android.support.v14.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.SharedPreferencesCompat$EditorCompat;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.R$attr;
import android.util.AttributeSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
/* loaded from: a.zip:android/support/v14/preference/MultiSelectListPreference.class */
public class MultiSelectListPreference extends DialogPreference {
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private Set<String> mValues;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v14/preference/MultiSelectListPreference$SavedState.class */
    public static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: android.support.v14.preference.MultiSelectListPreference.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        Set<String> values;

        public SavedState(Parcel parcel) {
            super(parcel);
            int readInt = parcel.readInt();
            this.values = new HashSet();
            String[] strArr = new String[readInt];
            parcel.readStringArray(strArr);
            Collections.addAll(this.values, strArr);
        }

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        @Override // android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(@NonNull Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.values.size());
            parcel.writeStringArray((String[]) this.values.toArray(new String[this.values.size()]));
        }
    }

    public MultiSelectListPreference(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, TypedArrayUtils.getAttr(context, R$attr.dialogPreferenceStyle, 16842897));
    }

    public MultiSelectListPreference(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public MultiSelectListPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mValues = new HashSet();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, android.support.v7.preference.R$styleable.MultiSelectListPreference, i, i2);
        this.mEntries = TypedArrayUtils.getTextArray(obtainStyledAttributes, android.support.v7.preference.R$styleable.MultiSelectListPreference_entries, android.support.v7.preference.R$styleable.MultiSelectListPreference_android_entries);
        this.mEntryValues = TypedArrayUtils.getTextArray(obtainStyledAttributes, android.support.v7.preference.R$styleable.MultiSelectListPreference_entryValues, android.support.v7.preference.R$styleable.MultiSelectListPreference_android_entryValues);
        obtainStyledAttributes.recycle();
    }

    public CharSequence[] getEntries() {
        return this.mEntries;
    }

    public CharSequence[] getEntryValues() {
        return this.mEntryValues;
    }

    protected Set<String> getPersistedStringSet(Set<String> set) {
        return !shouldPersist() ? set : getPreferenceManager().getSharedPreferences().getStringSet(getKey(), set);
    }

    public Set<String> getValues() {
        return this.mValues;
    }

    @Override // android.support.v7.preference.Preference
    protected Object onGetDefaultValue(TypedArray typedArray, int i) {
        CharSequence[] textArray = typedArray.getTextArray(i);
        HashSet hashSet = new HashSet();
        for (CharSequence charSequence : textArray) {
            hashSet.add(charSequence.toString());
        }
        return hashSet;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable == null || !parcelable.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        setValues(savedState.values);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public Parcelable onSaveInstanceState() {
        Parcelable onSaveInstanceState = super.onSaveInstanceState();
        if (isPersistent()) {
            return onSaveInstanceState;
        }
        SavedState savedState = new SavedState(onSaveInstanceState);
        savedState.values = getValues();
        return savedState;
    }

    @Override // android.support.v7.preference.Preference
    protected void onSetInitialValue(boolean z, Object obj) {
        setValues(z ? getPersistedStringSet(this.mValues) : (Set) obj);
    }

    protected boolean persistStringSet(Set<String> set) {
        if (shouldPersist()) {
            if (set.equals(getPersistedStringSet(null))) {
                return true;
            }
            SharedPreferences.Editor edit = getPreferenceManager().getSharedPreferences().edit();
            edit.putStringSet(getKey(), set);
            SharedPreferencesCompat$EditorCompat.getInstance().apply(edit);
            return true;
        }
        return false;
    }

    public void setValues(Set<String> set) {
        this.mValues.clear();
        this.mValues.addAll(set);
        persistStringSet(set);
    }
}
