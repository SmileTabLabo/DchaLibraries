package com.android.settings.applications;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settingslib.applications.ApplicationsState;
/* loaded from: classes.dex */
public class AppViewHolder {
    public ImageView appIcon;
    public TextView appName;
    public TextView disabled;
    public ApplicationsState.AppEntry entry;
    public View rootView;
    public TextView summary;

    public static AppViewHolder createOrRecycle(LayoutInflater inflater, View convertView) {
        if (convertView == null) {
            View convertView2 = inflater.inflate(R.layout.preference_app, (ViewGroup) null);
            inflater.inflate(R.layout.widget_text_views, (ViewGroup) convertView2.findViewById(16908312));
            AppViewHolder holder = new AppViewHolder();
            holder.rootView = convertView2;
            holder.appName = (TextView) convertView2.findViewById(16908310);
            holder.appIcon = (ImageView) convertView2.findViewById(16908294);
            holder.summary = (TextView) convertView2.findViewById(R.id.widget_text1);
            holder.disabled = (TextView) convertView2.findViewById(R.id.widget_text2);
            convertView2.setTag(holder);
            return holder;
        }
        return (AppViewHolder) convertView.getTag();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateSizeText(CharSequence invalidSizeStr, int whichSize) {
        if (ManageApplications.DEBUG) {
            Log.i("ManageApplications", "updateSizeText of " + this.entry.label + " " + this.entry + ": " + this.entry.sizeStr);
        }
        if (this.entry.sizeStr == null) {
            if (this.entry.size == -2) {
                this.summary.setText(invalidSizeStr);
                return;
            }
            return;
        }
        switch (whichSize) {
            case 1:
                this.summary.setText(this.entry.internalSizeStr);
                return;
            case 2:
                this.summary.setText(this.entry.externalSizeStr);
                return;
            default:
                this.summary.setText(this.entry.sizeStr);
                return;
        }
    }
}
