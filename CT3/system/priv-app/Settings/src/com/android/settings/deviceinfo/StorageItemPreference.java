package com.android.settings.deviceinfo;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.format.Formatter;
import android.widget.ProgressBar;
import com.android.settings.R;
/* loaded from: classes.dex */
public class StorageItemPreference extends Preference {
    private int progress;
    private ProgressBar progressBar;
    public int userHandle;

    public StorageItemPreference(Context context) {
        super(context);
        this.progress = -1;
        setLayoutResource(R.layout.storage_item);
    }

    public void setStorageSize(long size, long total) {
        String formatFileSize;
        if (size == 0) {
            formatFileSize = String.valueOf(0);
        } else {
            formatFileSize = Formatter.formatFileSize(getContext(), size);
        }
        setSummary(formatFileSize);
        if (total == 0) {
            this.progress = 0;
        } else {
            this.progress = (int) ((100 * size) / total);
        }
        updateProgressBar();
    }

    protected void updateProgressBar() {
        if (this.progressBar == null) {
            return;
        }
        if (this.progress == -1) {
            this.progressBar.setVisibility(8);
            return;
        }
        this.progressBar.setVisibility(0);
        this.progressBar.setMax(100);
        this.progressBar.setProgress(this.progress);
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder view) {
        this.progressBar = (ProgressBar) view.findViewById(16908301);
        updateProgressBar();
        super.onBindViewHolder(view);
    }
}
