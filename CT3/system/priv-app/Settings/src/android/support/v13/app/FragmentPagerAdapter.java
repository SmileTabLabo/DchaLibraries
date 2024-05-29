package android.support.v13.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
/* loaded from: classes.dex */
public abstract class FragmentPagerAdapter extends PagerAdapter {
    private FragmentTransaction mCurTransaction = null;
    private Fragment mCurrentPrimaryItem = null;
    private final FragmentManager mFragmentManager;

    public abstract Fragment getItem(int i);

    public FragmentPagerAdapter(FragmentManager fm) {
        this.mFragmentManager = fm;
    }

    @Override // android.support.v4.view.PagerAdapter
    public void startUpdate(ViewGroup container) {
        if (container.getId() != -1) {
            return;
        }
        throw new IllegalStateException("ViewPager with adapter " + this + " requires a view id");
    }

    @Override // android.support.v4.view.PagerAdapter
    public Object instantiateItem(ViewGroup container, int position) {
        if (this.mCurTransaction == null) {
            this.mCurTransaction = this.mFragmentManager.beginTransaction();
        }
        long itemId = getItemId(position);
        String name = makeFragmentName(container.getId(), itemId);
        Fragment fragment = this.mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            this.mCurTransaction.attach(fragment);
        } else {
            fragment = getItem(position);
            this.mCurTransaction.add(container.getId(), fragment, makeFragmentName(container.getId(), itemId));
        }
        if (fragment != this.mCurrentPrimaryItem) {
            FragmentCompat.setMenuVisibility(fragment, false);
            FragmentCompat.setUserVisibleHint(fragment, false);
        }
        return fragment;
    }

    @Override // android.support.v4.view.PagerAdapter
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (this.mCurTransaction == null) {
            this.mCurTransaction = this.mFragmentManager.beginTransaction();
        }
        this.mCurTransaction.detach((Fragment) object);
    }

    @Override // android.support.v4.view.PagerAdapter
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;
        if (fragment == this.mCurrentPrimaryItem) {
            return;
        }
        if (this.mCurrentPrimaryItem != null) {
            FragmentCompat.setMenuVisibility(this.mCurrentPrimaryItem, false);
            FragmentCompat.setUserVisibleHint(this.mCurrentPrimaryItem, false);
        }
        if (fragment != null) {
            FragmentCompat.setMenuVisibility(fragment, true);
            FragmentCompat.setUserVisibleHint(fragment, true);
        }
        this.mCurrentPrimaryItem = fragment;
    }

    @Override // android.support.v4.view.PagerAdapter
    public void finishUpdate(ViewGroup container) {
        if (this.mCurTransaction == null) {
            return;
        }
        this.mCurTransaction.commitAllowingStateLoss();
        this.mCurTransaction = null;
        this.mFragmentManager.executePendingTransactions();
    }

    @Override // android.support.v4.view.PagerAdapter
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

    @Override // android.support.v4.view.PagerAdapter
    public Parcelable saveState() {
        return null;
    }

    @Override // android.support.v4.view.PagerAdapter
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    public long getItemId(int position) {
        return position;
    }

    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }
}
