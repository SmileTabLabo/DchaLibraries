package com.android.settings.datausage;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import com.android.settings.R;
import com.android.settings.datausage.CycleAdapter;
/* loaded from: classes.dex */
public class SpinnerPreference extends Preference implements CycleAdapter.SpinnerInterface {
    private CycleAdapter mAdapter;
    private Object mCurrentObject;
    private AdapterView.OnItemSelectedListener mListener;
    private final AdapterView.OnItemSelectedListener mOnSelectedListener;
    private int mPosition;

    public SpinnerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mOnSelectedListener = new AdapterView.OnItemSelectedListener() { // from class: com.android.settings.datausage.SpinnerPreference.1
            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (SpinnerPreference.this.mPosition == position) {
                    return;
                }
                SpinnerPreference.this.mPosition = position;
                SpinnerPreference.this.mCurrentObject = SpinnerPreference.this.mAdapter.getItem(position);
                SpinnerPreference.this.mListener.onItemSelected(parent, view, position, id);
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onNothingSelected(AdapterView<?> parent) {
                SpinnerPreference.this.mListener.onNothingSelected(parent);
            }
        };
        setLayoutResource(R.layout.data_usage_cycles);
    }

    @Override // com.android.settings.datausage.CycleAdapter.SpinnerInterface
    public void setAdapter(CycleAdapter cycleAdapter) {
        this.mAdapter = cycleAdapter;
        notifyChanged();
    }

    @Override // com.android.settings.datausage.CycleAdapter.SpinnerInterface
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        this.mListener = listener;
    }

    @Override // com.android.settings.datausage.CycleAdapter.SpinnerInterface
    public Object getSelectedItem() {
        return this.mCurrentObject;
    }

    @Override // com.android.settings.datausage.CycleAdapter.SpinnerInterface
    public void setSelection(int position) {
        this.mPosition = position;
        this.mCurrentObject = this.mAdapter.getItem(this.mPosition);
        notifyChanged();
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        Spinner spinner = (Spinner) holder.findViewById(R.id.cycles_spinner);
        spinner.setAdapter((SpinnerAdapter) this.mAdapter);
        spinner.setSelection(this.mPosition);
        spinner.setOnItemSelectedListener(this.mOnSelectedListener);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void performClick(View view) {
        view.findViewById(R.id.cycles_spinner).performClick();
    }
}
