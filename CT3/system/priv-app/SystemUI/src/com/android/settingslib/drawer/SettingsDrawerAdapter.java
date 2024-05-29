package com.android.settingslib.drawer;

import android.graphics.drawable.Icon;
import android.os.BenesseExtension;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settingslib.R$drawable;
import com.android.settingslib.R$id;
import com.android.settingslib.R$layout;
import com.android.settingslib.R$string;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/settingslib/drawer/SettingsDrawerAdapter.class */
public class SettingsDrawerAdapter extends BaseAdapter {
    private final SettingsDrawerActivity mActivity;
    private final ArrayList<Item> mItems = new ArrayList<>();

    /* loaded from: a.zip:com/android/settingslib/drawer/SettingsDrawerAdapter$Item.class */
    private static class Item {
        public Icon icon;
        public CharSequence label;
        public Tile tile;

        private Item() {
        }

        /* synthetic */ Item(Item item) {
            this();
        }
    }

    public SettingsDrawerAdapter(SettingsDrawerActivity settingsDrawerActivity) {
        this.mActivity = settingsDrawerActivity;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.mItems.size();
    }

    @Override // android.widget.Adapter
    public Object getItem(int i) {
        return this.mItems.get(i);
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return i;
    }

    public Tile getTile(int i) {
        Tile tile = null;
        if (this.mItems.get(i) != null) {
            tile = this.mItems.get(i).tile;
        }
        return tile;
    }

    /* JADX WARN: Code restructure failed: missing block: B:25:0x006e, code lost:
        if (r6 != (r10.getId() == com.android.settingslib.R$id.tile_item)) goto L32;
     */
    /* JADX WARN: Code restructure failed: missing block: B:7:0x0020, code lost:
        if (r7.getId() != com.android.settingslib.R$id.spacer) goto L10;
     */
    @Override // android.widget.Adapter
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public View getView(int i, View view, ViewGroup viewGroup) {
        View inflate;
        Item item = this.mItems.get(i);
        if (item == null) {
            if (view != null) {
                inflate = view;
            }
            inflate = LayoutInflater.from(this.mActivity).inflate(R$layout.drawer_spacer, viewGroup, false);
            return inflate;
        }
        View view2 = view;
        if (view != null) {
            view2 = view;
            if (view.getId() == R$id.spacer) {
                view2 = null;
            }
        }
        boolean z = item.icon != null;
        if (view2 != null) {
        }
        view2 = LayoutInflater.from(this.mActivity).inflate(z ? R$layout.drawer_item : R$layout.drawer_category, viewGroup, false);
        if (z) {
            ((ImageView) view2.findViewById(16908294)).setImageIcon(item.icon);
        }
        ((TextView) view2.findViewById(16908310)).setText(item.label);
        return view2;
    }

    @Override // android.widget.BaseAdapter, android.widget.ListAdapter
    public boolean isEnabled(int i) {
        return (this.mItems.get(i) == null || this.mItems.get(i).icon == null) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateCategories() {
        if (BenesseExtension.getDchaState() != 0) {
            return;
        }
        List<DashboardCategory> dashboardCategories = this.mActivity.getDashboardCategories();
        this.mItems.clear();
        this.mItems.add(null);
        Item item = new Item(null);
        item.label = this.mActivity.getString(R$string.home);
        item.icon = Icon.createWithResource(this.mActivity, R$drawable.home);
        this.mItems.add(item);
        for (int i = 0; i < dashboardCategories.size(); i++) {
            Item item2 = new Item(null);
            item2.icon = null;
            DashboardCategory dashboardCategory = dashboardCategories.get(i);
            item2.label = dashboardCategory.title;
            this.mItems.add(item2);
            for (int i2 = 0; i2 < dashboardCategory.tiles.size(); i2++) {
                Item item3 = new Item(null);
                Tile tile = dashboardCategory.tiles.get(i2);
                item3.label = tile.title;
                item3.icon = tile.icon;
                item3.tile = tile;
                this.mItems.add(item3);
            }
        }
        notifyDataSetChanged();
    }
}
