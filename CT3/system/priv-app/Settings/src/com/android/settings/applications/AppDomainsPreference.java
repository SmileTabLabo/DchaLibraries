package com.android.settings.applications;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.accessibility.ListDialogPreference;
/* loaded from: classes.dex */
public class AppDomainsPreference extends ListDialogPreference {
    private int mNumEntries;

    public AppDomainsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.app_domains_dialog);
        setListItemLayoutResource(R.layout.app_domains_item);
    }

    @Override // com.android.settings.accessibility.ListDialogPreference
    public void setTitles(CharSequence[] titles) {
        this.mNumEntries = titles != null ? titles.length : 0;
        super.setTitles(titles);
    }

    @Override // com.android.settings.accessibility.ListDialogPreference, android.support.v7.preference.Preference
    public CharSequence getSummary() {
        int whichVersion;
        Context context = getContext();
        if (this.mNumEntries == 0) {
            return context.getString(R.string.domain_urls_summary_none);
        }
        CharSequence summary = super.getSummary();
        if (this.mNumEntries == 1) {
            whichVersion = R.string.domain_urls_summary_one;
        } else {
            whichVersion = R.string.domain_urls_summary_some;
        }
        return context.getString(whichVersion, summary);
    }

    @Override // com.android.settings.accessibility.ListDialogPreference
    protected void onBindListItem(View view, int index) {
        CharSequence title = getTitleAt(index);
        if (title == null) {
            return;
        }
        TextView domainName = (TextView) view.findViewById(R.id.domain_name);
        domainName.setText(title);
    }
}
