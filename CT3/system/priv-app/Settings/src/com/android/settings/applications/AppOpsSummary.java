package com.android.settings.applications;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceFrameLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.InstrumentedFragment;
import com.android.settings.R;
import com.android.settings.applications.AppOpsState;
/* loaded from: classes.dex */
public class AppOpsSummary extends InstrumentedFragment {
    static AppOpsState.OpsTemplate[] sPageTemplates = {AppOpsState.LOCATION_TEMPLATE, AppOpsState.PERSONAL_TEMPLATE, AppOpsState.MESSAGING_TEMPLATE, AppOpsState.MEDIA_TEMPLATE, AppOpsState.DEVICE_TEMPLATE};
    private ViewGroup mContentContainer;
    int mCurPos;
    private LayoutInflater mInflater;
    CharSequence[] mPageNames;
    private View mRootView;
    private ViewPager mViewPager;

    @Override // com.android.settings.InstrumentedFragment
    protected int getMetricsCategory() {
        return 15;
    }

    /* loaded from: classes.dex */
    class MyPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override // android.support.v13.app.FragmentPagerAdapter
        public Fragment getItem(int position) {
            return new AppOpsCategory(AppOpsSummary.sPageTemplates[position]);
        }

        @Override // android.support.v4.view.PagerAdapter
        public int getCount() {
            return AppOpsSummary.sPageTemplates.length;
        }

        @Override // android.support.v4.view.PagerAdapter
        public CharSequence getPageTitle(int position) {
            return AppOpsSummary.this.mPageNames[position];
        }

        @Override // android.support.v4.view.ViewPager.OnPageChangeListener
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override // android.support.v4.view.ViewPager.OnPageChangeListener
        public void onPageSelected(int position) {
            AppOpsSummary.this.mCurPos = position;
        }

        @Override // android.support.v4.view.ViewPager.OnPageChangeListener
        public void onPageScrollStateChanged(int state) {
            if (state == 0) {
            }
        }
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mInflater = inflater;
        View rootView = this.mInflater.inflate(R.layout.app_ops_summary, container, false);
        this.mContentContainer = container;
        this.mRootView = rootView;
        this.mPageNames = getResources().getTextArray(R.array.app_ops_categories);
        this.mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        MyPagerAdapter adapter = new MyPagerAdapter(getChildFragmentManager());
        this.mViewPager.setAdapter(adapter);
        this.mViewPager.setOnPageChangeListener(adapter);
        PagerTabStrip tabs = (PagerTabStrip) rootView.findViewById(R.id.tabs);
        TypedArray ta = tabs.getContext().obtainStyledAttributes(new int[]{16843829});
        int colorAccent = ta.getColor(0, 0);
        ta.recycle();
        tabs.setTabIndicatorColorResource(colorAccent);
        if (container instanceof PreferenceFrameLayout) {
            rootView.getLayoutParams().removeBorders = true;
        }
        return rootView;
    }
}
