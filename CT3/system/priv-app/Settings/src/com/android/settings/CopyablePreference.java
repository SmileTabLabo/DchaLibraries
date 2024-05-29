package com.android.settings;

import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;
/* loaded from: classes.dex */
public class CopyablePreference extends Preference {
    public CopyablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CopyablePreference(Context context) {
        this(context, null);
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.setDividerAllowedAbove(true);
        holder.setDividerAllowedBelow(true);
        holder.itemView.setLongClickable(true);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.settings.CopyablePreference.1
            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View v) {
                CopyablePreference.copyPreference(CopyablePreference.this.getContext(), CopyablePreference.this);
                return true;
            }
        });
    }

    public CharSequence getCopyableText() {
        return getSummary();
    }

    public static void copyPreference(Context context, CopyablePreference pref) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService("clipboard");
        cm.setText(pref.getCopyableText());
        Toast.makeText(context, 17040181, 0).show();
    }
}
