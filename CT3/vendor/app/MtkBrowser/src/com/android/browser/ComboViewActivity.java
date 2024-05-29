package com.android.browser;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.android.browser.UI;
import java.util.ArrayList;
/* loaded from: b.zip:com/android/browser/ComboViewActivity.class */
public class ComboViewActivity extends Activity implements CombinedBookmarksCallbacks {

    /* renamed from: -com-android-browser-UI$ComboViewsSwitchesValues  reason: not valid java name */
    private static final int[] f1comandroidbrowserUI$ComboViewsSwitchesValues = null;
    private TabsAdapter mTabsAdapter;
    private ViewPager mViewPager;

    /* loaded from: b.zip:com/android/browser/ComboViewActivity$TabsAdapter.class */
    public static class TabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
        private final ActionBar mActionBar;
        private final Context mContext;
        private final ArrayList<TabInfo> mTabs;
        private final ViewPager mViewPager;

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: b.zip:com/android/browser/ComboViewActivity$TabsAdapter$TabInfo.class */
        public static final class TabInfo {
            private final Bundle args;
            private final Class<?> clss;

            TabInfo(Class<?> cls, Bundle bundle) {
                this.clss = cls;
                this.args = bundle;
            }
        }

        public TabsAdapter(Activity activity, ViewPager viewPager) {
            super(activity.getFragmentManager());
            this.mTabs = new ArrayList<>();
            this.mContext = activity;
            this.mActionBar = activity.getActionBar();
            this.mViewPager = viewPager;
            this.mViewPager.setAdapter(this);
            this.mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tab, Class<?> cls, Bundle bundle) {
            TabInfo tabInfo = new TabInfo(cls, bundle);
            tab.setTag(tabInfo);
            tab.setTabListener(this);
            this.mTabs.add(tabInfo);
            this.mActionBar.addTab(tab);
            notifyDataSetChanged();
        }

        @Override // android.support.v4.view.PagerAdapter
        public int getCount() {
            return this.mTabs.size();
        }

        @Override // android.support.v13.app.FragmentPagerAdapter
        public Fragment getItem(int i) {
            TabInfo tabInfo = this.mTabs.get(i);
            return Fragment.instantiate(this.mContext, tabInfo.clss.getName(), tabInfo.args);
        }

        @Override // android.support.v4.view.ViewPager.OnPageChangeListener
        public void onPageScrollStateChanged(int i) {
        }

        @Override // android.support.v4.view.ViewPager.OnPageChangeListener
        public void onPageScrolled(int i, float f, int i2) {
        }

        @Override // android.support.v4.view.ViewPager.OnPageChangeListener
        public void onPageSelected(int i) {
            this.mActionBar.setSelectedNavigationItem(i);
        }

        @Override // android.app.ActionBar.TabListener
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        }

        @Override // android.app.ActionBar.TabListener
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            Object tag = tab.getTag();
            for (int i = 0; i < this.mTabs.size(); i++) {
                if (this.mTabs.get(i) == tag) {
                    this.mViewPager.setCurrentItem(i);
                }
            }
        }

        @Override // android.app.ActionBar.TabListener
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        }
    }

    /* renamed from: -getcom-android-browser-UI$ComboViewsSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m102getcomandroidbrowserUI$ComboViewsSwitchesValues() {
        if (f1comandroidbrowserUI$ComboViewsSwitchesValues != null) {
            return f1comandroidbrowserUI$ComboViewsSwitchesValues;
        }
        int[] iArr = new int[UI.ComboViews.valuesCustom().length];
        try {
            iArr[UI.ComboViews.Bookmarks.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[UI.ComboViews.History.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[UI.ComboViews.Snapshots.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        f1comandroidbrowserUI$ComboViewsSwitchesValues = iArr;
        return iArr;
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setResult(0);
        Bundle extras = getIntent().getExtras();
        Bundle bundle2 = extras.getBundle("combo_args");
        String string = extras.getString("initial_view", null);
        UI.ComboViews valueOf = string != null ? UI.ComboViews.valueOf(string) : UI.ComboViews.Bookmarks;
        this.mViewPager = new ViewPager(this);
        this.mViewPager.setId(2131558403);
        setContentView(this.mViewPager);
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(2);
        if (BrowserActivity.isTablet(this)) {
            actionBar.setDisplayOptions(3);
            actionBar.setHomeButtonEnabled(true);
        } else {
            actionBar.setDisplayOptions(0);
        }
        this.mTabsAdapter = new TabsAdapter(this, this.mViewPager);
        this.mTabsAdapter.addTab(actionBar.newTab().setText(2131492950), BrowserBookmarksPage.class, bundle2);
        this.mTabsAdapter.addTab(actionBar.newTab().setText(2131492952), BrowserHistoryPage.class, bundle2);
        this.mTabsAdapter.addTab(actionBar.newTab().setText(2131492953), BrowserSnapshotPage.class, bundle2);
        if (bundle != null) {
            actionBar.setSelectedNavigationItem(bundle.getInt("tab", 0));
            return;
        }
        switch (m102getcomandroidbrowserUI$ComboViewsSwitchesValues()[valueOf.ordinal()]) {
            case 1:
                this.mViewPager.setCurrentItem(0);
                return;
            case 2:
                this.mViewPager.setCurrentItem(1);
                return;
            case 3:
                this.mViewPager.setCurrentItem(2);
                return;
            default:
                return;
        }
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(2131755012, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            finish();
            return true;
        } else if (menuItem.getItemId() == 2131558587) {
            String stringExtra = getIntent().getStringExtra("url");
            Intent intent = new Intent(this, BrowserPreferencesPage.class);
            intent.putExtra("currentPage", stringExtra);
            startActivityForResult(intent, 3);
            return true;
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }

    @Override // com.android.browser.CombinedBookmarksCallbacks
    public void openInNewTab(String... strArr) {
        Intent intent = new Intent();
        intent.putExtra("open_all", strArr);
        setResult(-1, intent);
        finish();
    }

    @Override // com.android.browser.CombinedBookmarksCallbacks
    public void openSnapshot(long j, String str, String str2) {
        Intent intent = new Intent(this, BrowserActivity.class);
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse(str2));
        intent.putExtra("snapshot_id", j);
        intent.putExtra("snapshot_title", str);
        intent.putExtra("snapshot_url", str2);
        setResult(0);
        startActivity(intent);
        finish();
    }

    @Override // com.android.browser.CombinedBookmarksCallbacks
    public void openUrl(String str) {
        if (str == null) {
            Toast.makeText(this, 2131493014, 1).show();
            return;
        }
        Intent intent = new Intent(this, BrowserActivity.class);
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse(str));
        setResult(-1, intent);
        finish();
    }
}
