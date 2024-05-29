package android.support.v7.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
/* loaded from: a.zip:android/support/v7/preference/DropDownPreference.class */
public class DropDownPreference extends ListPreference {
    private final ArrayAdapter<String> mAdapter;
    private final Context mContext;
    private final AdapterView.OnItemSelectedListener mItemSelectedListener;
    private Spinner mSpinner;

    public DropDownPreference(Context context) {
        this(context, null);
    }

    public DropDownPreference(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.dropdownPreferenceStyle);
    }

    public DropDownPreference(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public DropDownPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mItemSelectedListener = new AdapterView.OnItemSelectedListener(this) { // from class: android.support.v7.preference.DropDownPreference.1
            final DropDownPreference this$0;

            {
                this.this$0 = this;
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onItemSelected(AdapterView<?> adapterView, View view, int i3, long j) {
                if (i3 >= 0) {
                    String charSequence = this.this$0.getEntryValues()[i3].toString();
                    if (charSequence.equals(this.this$0.getValue()) || !this.this$0.callChangeListener(charSequence)) {
                        return;
                    }
                    this.this$0.setValue(charSequence);
                }
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };
        this.mContext = context;
        this.mAdapter = createAdapter();
        updateEntries();
    }

    private void updateEntries() {
        this.mAdapter.clear();
        if (getEntries() != null) {
            for (CharSequence charSequence : getEntries()) {
                this.mAdapter.add(charSequence.toString());
            }
        }
    }

    protected ArrayAdapter createAdapter() {
        return new ArrayAdapter(this.mContext, 17367049);
    }

    public int findSpinnerIndexOfValue(String str) {
        CharSequence[] entryValues = getEntryValues();
        if (str == null || entryValues == null) {
            return -1;
        }
        for (int length = entryValues.length - 1; length >= 0; length--) {
            if (entryValues[length].equals(str)) {
                return length;
            }
        }
        return -1;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void notifyChanged() {
        super.notifyChanged();
        this.mAdapter.notifyDataSetChanged();
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        this.mSpinner = (Spinner) preferenceViewHolder.itemView.findViewById(R$id.spinner);
        this.mSpinner.setAdapter((SpinnerAdapter) this.mAdapter);
        this.mSpinner.setOnItemSelectedListener(this.mItemSelectedListener);
        this.mSpinner.setSelection(findSpinnerIndexOfValue(getValue()));
        super.onBindViewHolder(preferenceViewHolder);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.DialogPreference, android.support.v7.preference.Preference
    public void onClick() {
        this.mSpinner.performClick();
    }
}
