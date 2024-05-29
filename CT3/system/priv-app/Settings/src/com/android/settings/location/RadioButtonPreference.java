package com.android.settings.location;

import android.content.Context;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.settings.R;
/* loaded from: classes.dex */
public class RadioButtonPreference extends CheckBoxPreference {
    private OnClickListener mListener;

    /* loaded from: classes.dex */
    public interface OnClickListener {
        void onRadioButtonClicked(RadioButtonPreference radioButtonPreference);
    }

    public RadioButtonPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mListener = null;
        setWidgetLayoutResource(R.layout.preference_widget_radiobutton);
    }

    public RadioButtonPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842895);
    }

    public RadioButtonPreference(Context context) {
        this(context, null);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setOnClickListener(OnClickListener listener) {
        this.mListener = listener;
    }

    @Override // android.support.v7.preference.TwoStatePreference, android.support.v7.preference.Preference
    public void onClick() {
        if (this.mListener == null) {
            return;
        }
        this.mListener.onRadioButtonClicked(this);
    }

    @Override // android.support.v7.preference.CheckBoxPreference, android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        TextView title = (TextView) view.findViewById(16908310);
        if (title == null) {
            return;
        }
        title.setSingleLine(false);
        title.setMaxLines(3);
    }
}
