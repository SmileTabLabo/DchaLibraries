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
/* loaded from: classes.dex */
public class SettingsDrawerAdapter extends BaseAdapter {
    private final SettingsDrawerActivity mActivity;
    private final ArrayList<Item> mItems = new ArrayList<>();

    public SettingsDrawerAdapter(SettingsDrawerActivity activity) {
        this.mActivity = activity;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateCategories() {
        if (BenesseExtension.getDchaState() != 0) {
            return;
        }
        List<DashboardCategory> categories = this.mActivity.getDashboardCategories();
        this.mItems.clear();
        this.mItems.add(null);
        Item tile = new Item(null);
        tile.label = this.mActivity.getString(R$string.home);
        tile.icon = Icon.createWithResource(this.mActivity, R$drawable.home);
        this.mItems.add(tile);
        for (int i = 0; i < categories.size(); i++) {
            Item category = new Item(null);
            category.icon = null;
            DashboardCategory dashboardCategory = categories.get(i);
            category.label = dashboardCategory.title;
            this.mItems.add(category);
            for (int j = 0; j < dashboardCategory.tiles.size(); j++) {
                Item tile2 = new Item(null);
                Tile dashboardTile = dashboardCategory.tiles.get(j);
                tile2.label = dashboardTile.title;
                tile2.icon = dashboardTile.icon;
                tile2.tile = dashboardTile;
                this.mItems.add(tile2);
            }
        }
        notifyDataSetChanged();
    }

    public Tile getTile(int position) {
        if (this.mItems.get(position) != null) {
            return this.mItems.get(position).tile;
        }
        return null;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.mItems.size();
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        return this.mItems.get(position);
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return position;
    }

    @Override // android.widget.BaseAdapter, android.widget.ListAdapter
    public boolean isEnabled(int position) {
        return (this.mItems.get(position) == null || this.mItems.get(position).icon == null) ? false : true;
    }

    /* JADX WARN: Code restructure failed: missing block: B:20:0x003d, code lost:
        if (r0 != (r7.getId() == com.android.settingslib.R$id.tile_item)) goto L27;
     */
    @Override // android.widget.Adapter
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = this.mItems.get(position);
        if (item == null) {
            if (convertView == null || convertView.getId() != R$id.spacer) {
                return LayoutInflater.from(this.mActivity).inflate(R$layout.drawer_spacer, parent, false);
            }
            return convertView;
        }
        if (convertView != null && convertView.getId() == R$id.spacer) {
            convertView = null;
        }
        boolean isTile = item.icon != null;
        if (convertView != null) {
        }
        convertView = LayoutInflater.from(this.mActivity).inflate(isTile ? R$layout.drawer_item : R$layout.drawer_category, parent, false);
        if (isTile) {
            ((ImageView) convertView.findViewById(16908294)).setImageIcon(item.icon);
        }
        ((TextView) convertView.findViewById(16908310)).setText(item.label);
        return convertView;
    }

    /* loaded from: classes.dex */
    private static class Item {
        public Icon icon;
        public CharSequence label;
        public Tile tile;

        /* synthetic */ Item(Item item) {
            this();
        }

        private Item() {
        }
    }
}
