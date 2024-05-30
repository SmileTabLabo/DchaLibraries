package com.android.settings.wifi.calling;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.ims.ImsManager;
import com.android.settings.R;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.search.actionbar.SearchMenuController;
import com.android.settings.support.actionbar.HelpMenuController;
import com.android.settings.support.actionbar.HelpResourceProvider;
import com.android.settings.widget.RtlCompatibleViewPager;
import com.android.settings.widget.SlidingTabLayout;
import java.util.List;
/* loaded from: classes.dex */
public class WifiCallingSettings extends InstrumentedFragment implements HelpResourceProvider {
    private WifiCallingViewPagerAdapter mPagerAdapter;
    private List<SubscriptionInfo> mSil;
    private SlidingTabLayout mTabLayout;
    private RtlCompatibleViewPager mViewPager;

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 105;
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.wifi_calling_settings_tabs, viewGroup, false);
        this.mTabLayout = (SlidingTabLayout) inflate.findViewById(R.id.sliding_tabs);
        this.mViewPager = (RtlCompatibleViewPager) inflate.findViewById(R.id.view_pager);
        this.mPagerAdapter = new WifiCallingViewPagerAdapter(getChildFragmentManager(), this.mViewPager);
        this.mViewPager.setAdapter(this.mPagerAdapter);
        return inflate;
    }

    @Override // com.android.settingslib.core.lifecycle.ObservableFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
        SearchMenuController.init(this);
        HelpMenuController.init(this);
        updateSubList();
    }

    @Override // com.android.settingslib.core.lifecycle.ObservableFragment, android.app.Fragment
    public void onStart() {
        super.onStart();
        if (this.mSil != null && this.mSil.size() > 1) {
            this.mTabLayout.setViewPager(this.mViewPager);
        } else {
            this.mTabLayout.setVisibility(8);
        }
    }

    @Override // com.android.settings.support.actionbar.HelpResourceProvider
    public int getHelpResource() {
        return R.string.help_uri_wifi_calling;
    }

    /* loaded from: classes.dex */
    private final class WifiCallingViewPagerAdapter extends FragmentPagerAdapter {
        private final RtlCompatibleViewPager mViewPager;

        public WifiCallingViewPagerAdapter(FragmentManager fragmentManager, RtlCompatibleViewPager rtlCompatibleViewPager) {
            super(fragmentManager);
            this.mViewPager = rtlCompatibleViewPager;
        }

        @Override // android.support.v4.view.PagerAdapter
        public CharSequence getPageTitle(int i) {
            return String.valueOf(((SubscriptionInfo) WifiCallingSettings.this.mSil.get(i)).getDisplayName());
        }

        @Override // android.support.v13.app.FragmentPagerAdapter
        public Fragment getItem(int i) {
            Log.d("WifiCallingSettings", "Adapter getItem " + i);
            Bundle bundle = new Bundle();
            bundle.putBoolean("need_search_icon_in_action_bar", false);
            bundle.putInt("subId", ((SubscriptionInfo) WifiCallingSettings.this.mSil.get(i)).getSubscriptionId());
            WifiCallingSettingsForSub wifiCallingSettingsForSub = new WifiCallingSettingsForSub();
            wifiCallingSettingsForSub.setArguments(bundle);
            return wifiCallingSettingsForSub;
        }

        @Override // android.support.v13.app.FragmentPagerAdapter, android.support.v4.view.PagerAdapter
        public Object instantiateItem(ViewGroup viewGroup, int i) {
            Log.d("WifiCallingSettings", "Adapter instantiateItem " + i);
            return super.instantiateItem(viewGroup, this.mViewPager.getRtlAwareIndex(i));
        }

        @Override // android.support.v4.view.PagerAdapter
        public int getCount() {
            if (WifiCallingSettings.this.mSil != null) {
                Log.d("WifiCallingSettings", "Adapter getCount " + WifiCallingSettings.this.mSil.size());
                return WifiCallingSettings.this.mSil.size();
            }
            Log.d("WifiCallingSettings", "Adapter getCount null mSil ");
            return 0;
        }
    }

    private void updateSubList() {
        this.mSil = SubscriptionManager.from(getActivity()).getActiveSubscriptionInfoList();
        if (this.mSil == null) {
            return;
        }
        int i = 0;
        while (i < this.mSil.size()) {
            if (!ImsManager.getInstance(getActivity(), this.mSil.get(i).getSimSlotIndex()).isWfcEnabledByPlatform()) {
                this.mSil.remove(i);
            } else {
                i++;
            }
        }
    }
}
