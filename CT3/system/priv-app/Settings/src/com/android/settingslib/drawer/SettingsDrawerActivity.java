package com.android.settingslib.drawer;

import android.R;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
/* loaded from: classes.dex */
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

    /* loaded from: classes.dex */
    public interface CategoryListener {
        void onCategoriesChanged();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.currentTimeMillis();
        TypedArray theme = getTheme().obtainStyledAttributes(R.styleable.Theme);
        if (!theme.getBoolean(38, false)) {
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
        if (theme.getBoolean(38, false)) {
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.android.settingslib.drawer.SettingsDrawerActivity.1
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingsDrawerActivity.this.onTileClicked(SettingsDrawerActivity.this.mDrawerAdapter.getTile(position));
            }
        });
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("SettingsDrawerActivity", "onOptionsItemSelected: showMenu=" + this.mShowingMenu + " homeId? " + (item.getItemId() == 16908332) + " count=" + this.mDrawerAdapter.getCount());
        if (!this.mShowingMenu || this.mDrawerLayout == null || item.getItemId() != 16908332 || this.mDrawerAdapter.getCount() == 0) {
            return super.onOptionsItemSelected(item);
        }
        openDrawer();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        if (this.mDrawerLayout != null) {
            IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addAction("android.intent.action.PACKAGE_REPLACED");
            filter.addDataScheme("package");
            registerReceiver(this.mPackageReceiver, filter);
            new CategoriesUpdater(this, null).execute(new Void[0]);
        }
        if (getIntent() == null || !getIntent().getBooleanExtra("show_drawer_menu", false)) {
            return;
        }
        showMenuIcon();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onPause() {
        if (this.mDrawerLayout != null) {
            unregisterReceiver(this.mPackageReceiver);
        }
        super.onPause();
    }

    public void addCategoryListener(CategoryListener listener) {
        this.mCategoryListeners.add(listener);
    }

    public void remCategoryListener(CategoryListener listener) {
        this.mCategoryListeners.remove(listener);
    }

    public void setIsDrawerPresent(boolean isPresent) {
        if (isPresent) {
            this.mDrawerLayout = (DrawerLayout) findViewById(R$id.drawer_layout);
            updateDrawer();
        } else if (this.mDrawerLayout == null) {
        } else {
            this.mDrawerLayout.setDrawerLockMode(1);
            this.mDrawerLayout = null;
            Log.d("SettingsDrawerActivity", "setIsDrawerPresent, set mode LOCKED_CLOSED!!!");
        }
    }

    public void openDrawer() {
        Log.d("SettingsDrawerActivity", "openDrawer: mDrawerLayout is null? " + (this.mDrawerLayout != null));
        if (this.mDrawerLayout == null) {
            return;
        }
        this.mDrawerLayout.openDrawer(8388611);
    }

    public void closeDrawer() {
        if (this.mDrawerLayout == null) {
            return;
        }
        this.mDrawerLayout.closeDrawers();
    }

    @Override // android.app.Activity
    public void setContentView(int layoutResID) {
        ViewGroup parent = (ViewGroup) findViewById(R$id.content_frame);
        if (parent != null) {
            parent.removeAllViews();
        }
        LayoutInflater.from(this).inflate(layoutResID, parent);
    }

    @Override // android.app.Activity
    public void setContentView(View view) {
        ((ViewGroup) findViewById(R$id.content_frame)).addView(view);
    }

    @Override // android.app.Activity
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        ((ViewGroup) findViewById(R$id.content_frame)).addView(view, params);
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

    public void showMenuIcon() {
        Log.d("SettingsDrawerActivity", "showMenuIcon");
        this.mShowingMenu = true;
        getActionBar().setHomeAsUpIndicator(R$drawable.ic_menu);
        getActionBar().setDisplayHomeAsUpEnabled(true);
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
        int N = this.mCategoryListeners.size();
        for (int i = 0; i < N; i++) {
            this.mCategoryListeners.get(i).onCategoriesChanged();
        }
    }

    public boolean openTile(Tile tile) {
        int numUserHandles;
        closeDrawer();
        if (tile == null) {
            if (BenesseExtension.getDchaState() != 0) {
                return true;
            }
            startActivity(new Intent("android.settings.SETTINGS").addFlags(32768));
            return true;
        }
        try {
            numUserHandles = tile.userHandle.size();
        } catch (ActivityNotFoundException e) {
            Log.w("SettingsDrawerActivity", "Couldn't find tile " + tile.intent, e);
        }
        if (numUserHandles > 1) {
            ProfileSelectDialog.show(getFragmentManager(), tile);
            return false;
        }
        if (numUserHandles == 1) {
            tile.intent.putExtra("show_drawer_menu", true);
            tile.intent.addFlags(32768);
            startActivityAsUser(tile.intent, tile.userHandle.get(0));
        } else {
            tile.intent.putExtra("show_drawer_menu", true);
            tile.intent.addFlags(32768);
            startActivity(tile.intent);
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onTileClicked(Tile tile) {
        if (!openTile(tile)) {
            return;
        }
        finish();
    }

    public void onProfileTileOpen() {
        finish();
    }

    public void setTileEnabled(ComponentName component, boolean enabled) {
        PackageManager pm = getPackageManager();
        int state = pm.getComponentEnabledSetting(component);
        boolean isEnabled = state == 1;
        if (isEnabled == enabled && state != 0) {
            return;
        }
        if (enabled) {
            sTileBlacklist.remove(component);
        } else {
            sTileBlacklist.add(component);
        }
        pm.setComponentEnabledSetting(component, enabled ? 1 : 2, 1);
        new CategoriesUpdater(this, null).execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class CategoriesUpdater extends AsyncTask<Void, Void, List<DashboardCategory>> {
        /* synthetic */ CategoriesUpdater(SettingsDrawerActivity this$0, CategoriesUpdater categoriesUpdater) {
            this();
        }

        private CategoriesUpdater() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public List<DashboardCategory> doInBackground(Void... params) {
            if (SettingsDrawerActivity.sConfigTracker.applyNewConfig(SettingsDrawerActivity.this.getResources())) {
                SettingsDrawerActivity.sTileCache.clear();
            }
            return TileUtils.getCategories(SettingsDrawerActivity.this, SettingsDrawerActivity.sTileCache);
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            if (SettingsDrawerActivity.sConfigTracker != null && SettingsDrawerActivity.sTileCache != null) {
                return;
            }
            SettingsDrawerActivity.this.getDashboardCategories();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(List<DashboardCategory> dashboardCategories) {
            for (int i = 0; i < dashboardCategories.size(); i++) {
                DashboardCategory category = dashboardCategories.get(i);
                int j = 0;
                while (j < category.tiles.size()) {
                    Tile tile = category.tiles.get(j);
                    if (SettingsDrawerActivity.sTileBlacklist.contains(tile.intent.getComponent())) {
                        category.tiles.remove(j);
                        j--;
                    }
                    j++;
                }
            }
            List unused = SettingsDrawerActivity.sDashboardCategories = dashboardCategories;
            SettingsDrawerActivity.this.onCategoriesChanged();
        }
    }

    /* loaded from: classes.dex */
    private class PackageReceiver extends BroadcastReceiver {
        /* synthetic */ PackageReceiver(SettingsDrawerActivity this$0, PackageReceiver packageReceiver) {
            this();
        }

        private PackageReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            new CategoriesUpdater(SettingsDrawerActivity.this, null).execute(new Void[0]);
        }
    }
}
