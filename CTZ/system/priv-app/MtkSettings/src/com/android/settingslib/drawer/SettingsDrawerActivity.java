package com.android.settingslib.drawer;

import android.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toolbar;
import com.android.settingslib.wifi.AccessPoint;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class SettingsDrawerActivity extends Activity {
    private static final boolean DEBUG = Log.isLoggable("SettingsDrawerActivity", 3);
    private static ArraySet<ComponentName> sTileBlacklist = new ArraySet<>();
    private FrameLayout mContentHeaderContainer;
    private final PackageReceiver mPackageReceiver = new PackageReceiver();
    private final List<CategoryListener> mCategoryListeners = new ArrayList();

    /* loaded from: classes.dex */
    public interface CategoryListener {
        void onCategoriesChanged();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        System.currentTimeMillis();
        TypedArray obtainStyledAttributes = getTheme().obtainStyledAttributes(R.styleable.Theme);
        if (!obtainStyledAttributes.getBoolean(38, false)) {
            getWindow().addFlags(AccessPoint.UNREACHABLE_RSSI);
            requestWindowFeature(1);
        }
        super.setContentView(com.android.settingslib.R.layout.settings_with_drawer);
        this.mContentHeaderContainer = (FrameLayout) findViewById(com.android.settingslib.R.id.content_header_container);
        Toolbar toolbar = (Toolbar) findViewById(com.android.settingslib.R.id.action_bar);
        if (obtainStyledAttributes.getBoolean(38, false)) {
            toolbar.setVisibility(8);
        } else {
            setActionBar(toolbar);
        }
    }

    @Override // android.app.Activity
    public boolean onNavigateUp() {
        if (!super.onNavigateUp()) {
            finish();
            return true;
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter.addDataScheme("package");
        registerReceiver(this.mPackageReceiver, intentFilter);
        new CategoriesUpdateTask().execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onPause() {
        unregisterReceiver(this.mPackageReceiver);
        super.onPause();
    }

    public void addCategoryListener(CategoryListener categoryListener) {
        this.mCategoryListeners.add(categoryListener);
    }

    public void remCategoryListener(CategoryListener categoryListener) {
        this.mCategoryListeners.remove(categoryListener);
    }

    @Override // android.app.Activity
    public void setContentView(int i) {
        ViewGroup viewGroup = (ViewGroup) findViewById(com.android.settingslib.R.id.content_frame);
        if (viewGroup != null) {
            viewGroup.removeAllViews();
        }
        LayoutInflater.from(this).inflate(i, viewGroup);
    }

    @Override // android.app.Activity
    public void setContentView(View view) {
        ((ViewGroup) findViewById(com.android.settingslib.R.id.content_frame)).addView(view);
    }

    @Override // android.app.Activity
    public void setContentView(View view, ViewGroup.LayoutParams layoutParams) {
        ((ViewGroup) findViewById(com.android.settingslib.R.id.content_frame)).addView(view, layoutParams);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onCategoriesChanged() {
        int size = this.mCategoryListeners.size();
        for (int i = 0; i < size; i++) {
            this.mCategoryListeners.get(i).onCategoriesChanged();
        }
    }

    public boolean setTileEnabled(ComponentName componentName, boolean z) {
        PackageManager packageManager = getPackageManager();
        int componentEnabledSetting = packageManager.getComponentEnabledSetting(componentName);
        if ((componentEnabledSetting == 1) == z && componentEnabledSetting != 0) {
            return false;
        }
        if (z) {
            sTileBlacklist.remove(componentName);
        } else {
            sTileBlacklist.add(componentName);
        }
        packageManager.setComponentEnabledSetting(componentName, z ? 1 : 2, 1);
        return true;
    }

    public void updateCategories() {
        new CategoriesUpdateTask().execute(new Void[0]);
    }

    public String getSettingPkg() {
        return "com.android.settings";
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class CategoriesUpdateTask extends AsyncTask<Void, Void, Void> {
        private final CategoryManager mCategoryManager;

        public CategoriesUpdateTask() {
            this.mCategoryManager = CategoryManager.get(SettingsDrawerActivity.this);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Void doInBackground(Void... voidArr) {
            this.mCategoryManager.reloadAllCategories(SettingsDrawerActivity.this, SettingsDrawerActivity.this.getSettingPkg());
            return null;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Void r2) {
            this.mCategoryManager.updateCategoryFromBlacklist(SettingsDrawerActivity.sTileBlacklist);
            SettingsDrawerActivity.this.onCategoriesChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class PackageReceiver extends BroadcastReceiver {
        private PackageReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            new CategoriesUpdateTask().execute(new Void[0]);
        }
    }
}
