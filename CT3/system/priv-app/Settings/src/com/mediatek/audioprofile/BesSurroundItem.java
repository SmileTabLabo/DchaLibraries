package com.mediatek.audioprofile;

import android.content.Context;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.settings.R;
/* loaded from: classes.dex */
public class BesSurroundItem extends CheckBoxPreference {
    private OnClickListener mListener;

    /* loaded from: classes.dex */
    public interface OnClickListener {
        void onRadioButtonClicked(BesSurroundItem besSurroundItem);
    }

    public BesSurroundItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mListener = null;
        setWidgetLayoutResource(R.layout.preference_widget_radiobutton);
    }

    public BesSurroundItem(Context context, AttributeSet attrs) {
        this(context, attrs, 16842895);
    }

    public BesSurroundItem(Context context) {
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
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView title = (TextView) holder.findViewById(16908310);
        if (title == null) {
            return;
        }
        title.setSingleLine(false);
        title.setMaxLines(3);
    }
}
