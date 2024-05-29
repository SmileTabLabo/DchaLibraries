package com.android.settings.accessibility;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import com.android.settings.CustomDialogPreference;
/* loaded from: classes.dex */
public abstract class ListDialogPreference extends CustomDialogPreference {
    private CharSequence[] mEntryTitles;
    private int[] mEntryValues;
    private int mListItemLayout;
    private OnValueChangedListener mOnValueChangedListener;
    private int mValue;
    private int mValueIndex;
    private boolean mValueSet;

    /* loaded from: classes.dex */
    public interface OnValueChangedListener {
        void onValueChanged(ListDialogPreference listDialogPreference, int i);
    }

    protected abstract void onBindListItem(View view, int i);

    public ListDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnValueChangedListener(OnValueChangedListener listener) {
        this.mOnValueChangedListener = listener;
    }

    public void setListItemLayoutResource(int layoutResId) {
        this.mListItemLayout = layoutResId;
    }

    public void setValues(int[] values) {
        this.mEntryValues = values;
        if (!this.mValueSet || this.mValueIndex != -1) {
            return;
        }
        this.mValueIndex = getIndexForValue(this.mValue);
    }

    public void setTitles(CharSequence[] titles) {
        this.mEntryTitles = titles;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public CharSequence getTitleAt(int index) {
        if (this.mEntryTitles == null || this.mEntryTitles.length <= index) {
            return null;
        }
        return this.mEntryTitles[index];
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getValueAt(int index) {
        return this.mEntryValues[index];
    }

    @Override // android.support.v7.preference.Preference
    public CharSequence getSummary() {
        if (this.mValueIndex >= 0) {
            return getTitleAt(this.mValueIndex);
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.CustomDialogPreference
    public void onPrepareDialogBuilder(AlertDialog.Builder builder, DialogInterface.OnClickListener listener) {
        super.onPrepareDialogBuilder(builder, listener);
        Context context = getContext();
        int dialogLayout = getDialogLayoutResource();
        View picker = LayoutInflater.from(context).inflate(dialogLayout, (ViewGroup) null);
        ListPreferenceAdapter adapter = new ListPreferenceAdapter(this, null);
        AbsListView list = (AbsListView) picker.findViewById(16908298);
        list.setAdapter((ListAdapter) adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.android.settings.accessibility.ListDialogPreference.1
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapter2, View v, int position, long id) {
                if (ListDialogPreference.this.callChangeListener(Integer.valueOf((int) id))) {
                    ListDialogPreference.this.setValue((int) id);
                }
                Dialog dialog = ListDialogPreference.this.getDialog();
                if (dialog == null) {
                    return;
                }
                dialog.dismiss();
            }
        });
        int selectedPosition = getIndexForValue(this.mValue);
        if (selectedPosition != -1) {
            list.setSelection(selectedPosition);
        }
        builder.setView(picker);
        builder.setPositiveButton((CharSequence) null, (DialogInterface.OnClickListener) null);
    }

    protected int getIndexForValue(int value) {
        int[] values = this.mEntryValues;
        if (values != null) {
            int count = values.length;
            for (int i = 0; i < count; i++) {
                if (values[i] == value) {
                    return i;
                }
            }
            return -1;
        }
        return -1;
    }

    public void setValue(int value) {
        boolean changed = this.mValue != value;
        if (!changed && this.mValueSet) {
            return;
        }
        this.mValue = value;
        this.mValueIndex = getIndexForValue(value);
        this.mValueSet = true;
        persistInt(value);
        if (changed) {
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
        if (this.mOnValueChangedListener == null) {
            return;
        }
        this.mOnValueChangedListener.onValueChanged(this, value);
    }

    public int getValue() {
        return this.mValue;
    }

    @Override // android.support.v7.preference.Preference
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return Integer.valueOf(a.getInt(index, 0));
    }

    @Override // android.support.v7.preference.Preference
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(this.mValue) : ((Integer) defaultValue).intValue());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.value = getValue();
        return myState;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setValue(myState.value);
    }

    /* loaded from: classes.dex */
    private class ListPreferenceAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        /* synthetic */ ListPreferenceAdapter(ListDialogPreference this$0, ListPreferenceAdapter listPreferenceAdapter) {
            this();
        }

        private ListPreferenceAdapter() {
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return ListDialogPreference.this.mEntryValues.length;
        }

        @Override // android.widget.Adapter
        public Integer getItem(int position) {
            return Integer.valueOf(ListDialogPreference.this.mEntryValues[position]);
        }

        @Override // android.widget.Adapter
        public long getItemId(int position) {
            return ListDialogPreference.this.mEntryValues[position];
        }

        @Override // android.widget.BaseAdapter, android.widget.Adapter
        public boolean hasStableIds() {
            return true;
        }

        @Override // android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                if (this.mInflater == null) {
                    this.mInflater = LayoutInflater.from(parent.getContext());
                }
                convertView = this.mInflater.inflate(ListDialogPreference.this.mListItemLayout, parent, false);
            }
            ListDialogPreference.this.onBindListItem(convertView, position);
            return convertView;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: com.android.settings.accessibility.ListDialogPreference.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public int value;

        public SavedState(Parcel source) {
            super(source);
            this.value = source.readInt();
        }

        @Override // android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.value);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }
}
