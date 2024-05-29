package com.android.settingslib.drawer;

import android.R;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.BenesseExtension;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toolbar;
import com.android.settingslib.R$drawable;
import com.android.settingslib.R$id;
import com.android.settingslib.R$layout;
import com.android.settingslib.applications.InterestingConfigChanges;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/* loaded from: a.zip:com/android/settingslib/drawer/SettingsDrawerActivity.class */
public class SettingsDrawerActivity extends Activity {
    private static InterestingConfigChanges sConfigTracker;
    private static List<DashboardCategory> sDashboardCategories;
    private static ArraySet<ComponentName> sTileBlacklist = new ArraySet<>();
    private static HashMap<Pair<String, String>, Tile> sTileCache;
    private SettingsDrawerAdapter mDrawerAdapter;
    private DrawerLayout mDrawerLayout;
    private boolean mShowingMenu;
    private final PackageReceiver mPackageReceiver = new PackageReceiver(this, null);
    private final List<CategoryListener> mCategoryListeners = new ArrayList();

    /* loaded from: a.zip:com/android/settingslib/drawer/SettingsDrawerActivity$CategoriesUpdater.class */
    private class CategoriesUpdater extends AsyncTask<Void, Void, List<DashboardCategory>> {
        final SettingsDrawerActivity this$0;

        private CategoriesUpdater(SettingsDrawerActivity settingsDrawerActivity) {
            this.this$0 = settingsDrawerActivity;
        }

        /* synthetic */ CategoriesUpdater(SettingsDrawerActivity settingsDrawerActivity, CategoriesUpdater categoriesUpdater) {
            this(settingsDrawerActivity);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public List<DashboardCategory> doInBackground(Void... voidArr) {
            if (SettingsDrawerActivity.sConfigTracker.applyNewConfig(this.this$0.getResources())) {
                SettingsDrawerActivity.sTileCache.clear();
            }
            return TileUtils.getCategories(this.this$0, SettingsDrawerActivity.sTileCache);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(List<DashboardCategory> list) {
            for (int i = 0; i < list.size(); i++) {
                DashboardCategory dashboardCategory = list.get(i);
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 < dashboardCategory.tiles.size()) {
                        int i4 = i3;
                        if (SettingsDrawerActivity.sTileBlacklist.contains(dashboardCategory.tiles.get(i3).intent.getComponent())) {
                            dashboardCategory.tiles.remove(i3);
                            i4 = i3 - 1;
                        }
                        i2 = i4 + 1;
                    }
                }
            }
            List unused = SettingsDrawerActivity.sDashboardCategories = list;
            this.this$0.onCategoriesChanged();
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            if (SettingsDrawerActivity.sConfigTracker == null || SettingsDrawerActivity.sTileCache == null) {
                this.this$0.getDashboardCategories();
            }
        }
    }

    /* loaded from: a.zip:com/android/settingslib/drawer/SettingsDrawerActivity$CategoryListener.class */
    public interface CategoryListener {
        void onCategoriesChanged();
    }

    /* loaded from: a.zip:com/android/settingslib/drawer/SettingsDrawerActivity$PackageReceiver.class */
    private class PackageReceiver extends BroadcastReceiver {
        final SettingsDrawerActivity this$0;

        private PackageReceiver(SettingsDrawerActivity settingsDrawerActivity) {
            this.this$0 = settingsDrawerActivity;
        }

        /* synthetic */ PackageReceiver(SettingsDrawerActivity settingsDrawerActivity, PackageReceiver packageReceiver) {
            this(settingsDrawerActivity);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            new CategoriesUpdater(this.this$0, null).execute(new Void[0]);
        }
    }

    public void closeDrawer() {
        if (this.mDrawerLayout != null) {
            this.mDrawerLayout.closeDrawers();
        }
    }

    public List<DashboardCategory> getDashboardCategories() {
        if (sDashboardCategories == null) {
            sTileCache = new HashMap<>();
            sConfigTracker = new InterestingConfigChanges();
            sConfigTracker.applyNewConfig(getResources());
            sDashboardCategories = TileUtils.getCategories(this, sTileCache);
        }
        return sDashboardCategories;
    }

    protected void onCategoriesChanged() {
        updateDrawer();
        int size = this.mCategoryListeners.size();
        for (int i = 0; i < size; i++) {
            this.mCategoryListeners.get(i).onCategoriesChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        System.currentTimeMillis();
        TypedArray obtainStyledAttributes = getTheme().obtainStyledAttributes(R.styleable.Theme);
        if (!obtainStyledAttributes.getBoolean(38, false)) {
            getWindow().addFlags(Integer.MIN_VALUE);
            getWindow().addFlags(67108864);
            requestWindowFeature(1);
        }
        super.setContentView(R$layout.settings_with_drawer);
        this.mDrawerLayout = (DrawerLayout) findViewById(R$id.drawer_layout);
        if (this.mDrawerLayout == null) {
            return;
        }
        Toolbar toolbar = (Toolbar) findViewById(R$id.action_bar);
        if (obtainStyledAttributes.getBoolean(38, false)) {
            toolbar.setVisibility(8);
            this.mDrawerLayout.setDrawerLockMode(1);
            this.mDrawerLayout = null;
            Log.d("SettingsDrawerActivity", "onCreate, set mode LOCKED_CLOSED!!!");
            return;
        }
        getDashboardCategories();
        setActionBar(toolbar);
        this.mDrawerAdapter = new SettingsDrawerAdapter(this);
        ListView listView = (ListView) findViewById(R$id.left_drawer);
        listView.setAdapter((ListAdapter) this.mDrawerAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(this) { // from class: com.android.settingslib.drawer.SettingsDrawerActivity.1
            final SettingsDrawerActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                this.this$0.onTileClicked(this.this$0.mDrawerAdapter.getTile(i));
            }
        });
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        boolean z = false;
        StringBuilder append = new StringBuilder().append("onOptionsItemSelected: showMenu=").append(this.mShowingMenu).append(" homeId? ");
        if (menuItem.getItemId() == 16908332) {
            z = true;
        }
        Log.d("SettingsDrawerActivity", append.append(z).append(" count=").append(this.mDrawerAdapter.getCount()).toString());
        if (!this.mShowingMenu || this.mDrawerLayout == null || menuItem.getItemId() != 16908332 || this.mDrawerAdapter.getCount() == 0) {
            return super.onOptionsItemSelected(menuItem);
        }
        openDrawer();
        return true;
    }

    @Override // android.app.Activity
    protected void onPause() {
        if (this.mDrawerLayout != null) {
            unregisterReceiver(this.mPackageReceiver);
        }
        super.onPause();
    }

    public void onProfileTileOpen() {
        finish();
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        if (this.mDrawerLayout != null) {
            IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
            intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
            intentFilter.addDataScheme("package");
            registerReceiver(this.mPackageReceiver, intentFilter);
            new CategoriesUpdater(this, null).execute(new Void[0]);
        }
        if (getIntent() == null || !getIntent().getBooleanExtra("show_drawer_menu", false)) {
            return;
        }
        showMenuIcon();
    }

    protected void onTileClicked(Tile tile) {
        if (openTile(tile)) {
            finish();
        }
    }

    public void openDrawer() {
        Log.d("SettingsDrawerActivity", "openDrawer: mDrawerLayout is null? " + (this.mDrawerLayout != null));
        if (this.mDrawerLayout != null) {
            this.mDrawerLayout.openDrawer(8388611);
        }
    }

    public boolean openTile(Tile tile) {
        closeDrawer();
        if (tile == null) {
            if (BenesseExtension.getDchaState() != 0) {
                return true;
            }
            startActivity(new Intent("android.settings.SETTINGS").addFlags(32768));
            return true;
        }
        try {
            int size = tile.userHandle.size();
            if (size > 1) {
                ProfileSelectDialog.show(getFragmentManager(), tile);
                return false;
            } else if (size == 1) {
                tile.intent.putExtra("show_drawer_menu", true);
                tile.intent.addFlags(32768);
                startActivityAsUser(tile.intent, tile.userHandle.get(0));
                return true;
            } else {
                tile.intent.putExtra("show_drawer_menu", true);
                tile.intent.addFlags(32768);
                startActivity(tile.intent);
                return true;
            }
        } catch (ActivityNotFoundException e) {
            Log.w("SettingsDrawerActivity", "Couldn't find tile " + tile.intent, e);
            return true;
        }
    }

    @Override // android.app.Activity
    public void setContentView(int i) {
        ViewGroup viewGroup = (ViewGroup) findViewById(R$id.content_frame);
        if (viewGroup != null) {
            viewGroup.removeAllViews();
        }
        LayoutInflater.from(this).inflate(i, viewGroup);
    }

    @Override // android.app.Activity
    public void setContentView(View view) {
        ((ViewGroup) findViewById(R$id.content_frame)).addView(view);
    }

    @Override // android.app.Activity
    public void setContentView(View view, ViewGroup.LayoutParams layoutParams) {
        ((ViewGroup) findViewById(R$id.content_frame)).addView(view, layoutParams);
    }

    public void showMenuIcon() {
        Log.d("SettingsDrawerActivity", "showMenuIcon");
        this.mShowingMenu = true;
        getActionBar().setHomeAsUpIndicator(R$drawable.ic_menu);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void updateDrawer() {
        if (this.mDrawerLayout == null) {
            return;
        }
        this.mDrawerAdapter.updateCategories();
        if (this.mDrawerAdapter.getCount() != 0) {
            this.mDrawerLayout.setDrawerLockMode(0);
            return;
        }
        this.mDrawerLayout.setDrawerLockMode(1);
        Log.d("SettingsDrawerActivity", "updateDrawer, set mode LOCKED_CLOSED!!!");
    }
}
