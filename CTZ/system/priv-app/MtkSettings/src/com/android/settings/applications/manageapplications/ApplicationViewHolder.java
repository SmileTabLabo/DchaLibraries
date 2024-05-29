package com.android.settings.applications.manageapplications;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settingslib.applications.ApplicationsState;
/* loaded from: classes.dex */
public class ApplicationViewHolder extends RecyclerView.ViewHolder {
    private final ImageView mAppIcon;
    private final TextView mAppName;
    final TextView mDisabled;
    private final boolean mKeepStableHeight;
    final TextView mSummary;
    View mSummaryContainer;
    final Switch mSwitch;
    final ViewGroup mWidgetContainer;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ApplicationViewHolder(View view, boolean z) {
        super(view);
        this.mAppName = (TextView) view.findViewById(16908310);
        this.mAppIcon = (ImageView) view.findViewById(16908294);
        this.mSummaryContainer = view.findViewById(R.id.summary_container);
        this.mSummary = (TextView) view.findViewById(16908304);
        this.mDisabled = (TextView) view.findViewById(R.id.appendix);
        this.mKeepStableHeight = z;
        this.mSwitch = (Switch) view.findViewById(R.id.switchWidget);
        this.mWidgetContainer = (ViewGroup) view.findViewById(16908312);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static View newView(ViewGroup viewGroup, boolean z) {
        ViewGroup viewGroup2;
        ViewGroup viewGroup3 = (ViewGroup) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.preference_app, viewGroup, false);
        if (z && (viewGroup2 = (ViewGroup) viewGroup3.findViewById(16908312)) != null) {
            LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.preference_widget_master_switch, viewGroup2, true);
            viewGroup3.addView(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.preference_two_target_divider, viewGroup3, false), viewGroup3.getChildCount() - 1);
        }
        return viewGroup3;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setSummary(CharSequence charSequence) {
        this.mSummary.setText(charSequence);
        updateSummaryContainer();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setSummary(int i) {
        this.mSummary.setText(i);
        updateSummaryContainer();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setEnabled(boolean z) {
        this.itemView.setEnabled(z);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setTitle(CharSequence charSequence) {
        if (charSequence == null) {
            return;
        }
        this.mAppName.setText(charSequence);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setIcon(int i) {
        this.mAppIcon.setImageResource(i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setIcon(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        this.mAppIcon.setImageDrawable(drawable);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateDisableView(ApplicationInfo applicationInfo) {
        if ((applicationInfo.flags & 8388608) == 0) {
            this.mDisabled.setVisibility(0);
            this.mDisabled.setText(R.string.not_installed);
        } else if (!applicationInfo.enabled || applicationInfo.enabledSetting == 4) {
            this.mDisabled.setVisibility(0);
            this.mDisabled.setText(R.string.disabled);
        } else {
            this.mDisabled.setVisibility(8);
        }
        updateSummaryContainer();
    }

    void updateSummaryContainer() {
        boolean z;
        if (this.mKeepStableHeight) {
            this.mSummaryContainer.setVisibility(0);
            return;
        }
        if (!TextUtils.isEmpty(this.mDisabled.getText()) || !TextUtils.isEmpty(this.mSummary.getText())) {
            z = true;
        } else {
            z = false;
        }
        this.mSummaryContainer.setVisibility(z ? 0 : 8);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateSizeText(ApplicationsState.AppEntry appEntry, CharSequence charSequence, int i) {
        if (ManageApplications.DEBUG) {
            Log.d("ManageApplications", "updateSizeText of " + appEntry.label + " " + appEntry + ": " + appEntry.sizeStr);
        }
        if (appEntry.sizeStr != null) {
            switch (i) {
                case 1:
                    setSummary(appEntry.internalSizeStr);
                    return;
                case 2:
                    setSummary(appEntry.externalSizeStr);
                    return;
                default:
                    setSummary(appEntry.sizeStr);
                    return;
            }
        } else if (appEntry.size == -2) {
            setSummary(charSequence);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateSwitch(View.OnClickListener onClickListener, boolean z, boolean z2) {
        if (this.mSwitch != null && this.mWidgetContainer != null) {
            this.mWidgetContainer.setOnClickListener(onClickListener);
            this.mSwitch.setChecked(z2);
            this.mSwitch.setEnabled(z);
        }
    }
}
