package com.android.browser.addbookmark;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
/* loaded from: b.zip:com/android/browser/addbookmark/FolderSpinnerAdapter.class */
public class FolderSpinnerAdapter extends BaseAdapter {
    private Context mContext;
    private boolean mIncludeHomeScreen;
    private boolean mIncludesRecentFolder;
    private LayoutInflater mInflater;
    private String mOtherFolderDisplayText;
    private long mRecentFolderId;
    private String mRecentFolderName;

    public FolderSpinnerAdapter(Context context, boolean z) {
        this.mIncludeHomeScreen = z;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
    }

    private void bindView(int i, View view, boolean z) {
        int i2;
        int i3;
        int i4 = i;
        if (!this.mIncludeHomeScreen) {
            i4 = i + 1;
        }
        switch (i4) {
            case 0:
                i2 = 2131492995;
                i3 = 2130837560;
                break;
            case 1:
                i2 = 2131492994;
                i3 = 2130837542;
                break;
            case 2:
            case 3:
                i2 = 2131492996;
                i3 = 2130837550;
                break;
            default:
                i2 = 0;
                i3 = 0;
                break;
        }
        TextView textView = (TextView) view;
        if (i4 == 3) {
            textView.setText(this.mRecentFolderName);
        } else if (i4 != 2 || z || this.mOtherFolderDisplayText == null) {
            textView.setText(i2);
        } else {
            textView.setText(this.mOtherFolderDisplayText);
        }
        textView.setGravity(16);
        textView.setCompoundDrawablesWithIntrinsicBounds(this.mContext.getResources().getDrawable(i3), (Drawable) null, (Drawable) null, (Drawable) null);
    }

    public void addRecentFolder(long j, String str) {
        this.mIncludesRecentFolder = true;
        this.mRecentFolderId = j;
        this.mRecentFolderName = str;
    }

    public void clearRecentFolder() {
        if (this.mIncludesRecentFolder) {
            this.mIncludesRecentFolder = false;
            notifyDataSetChanged();
        }
    }

    @Override // android.widget.Adapter
    public int getCount() {
        int i = 2;
        if (this.mIncludeHomeScreen) {
            i = 3;
        }
        int i2 = i;
        if (this.mIncludesRecentFolder) {
            i2 = i + 1;
        }
        return i2;
    }

    @Override // android.widget.BaseAdapter, android.widget.SpinnerAdapter
    public View getDropDownView(int i, View view, ViewGroup viewGroup) {
        View view2 = view;
        if (view == null) {
            view2 = this.mInflater.inflate(17367049, viewGroup, false);
        }
        bindView(i, view2, true);
        return view2;
    }

    @Override // android.widget.Adapter
    public Object getItem(int i) {
        return null;
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        long j = i;
        long j2 = j;
        if (!this.mIncludeHomeScreen) {
            j2 = j + 1;
        }
        return j2;
    }

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        View view2 = view;
        if (view == null) {
            view2 = this.mInflater.inflate(17367048, viewGroup, false);
        }
        bindView(i, view2, false);
        return view2;
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public boolean hasStableIds() {
        return true;
    }

    public long recentFolderId() {
        return this.mRecentFolderId;
    }

    public void setOtherFolderDisplayText(String str) {
        this.mOtherFolderDisplayText = str;
        notifyDataSetChanged();
    }
}
