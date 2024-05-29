package android.support.v4.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TabHost;
import java.util.ArrayList;
/* loaded from: a.zip:android/support/v4/app/FragmentTabHost.class */
public class FragmentTabHost extends TabHost implements TabHost.OnTabChangeListener {
    private boolean mAttached;
    private int mContainerId;
    private Context mContext;
    private FragmentManager mFragmentManager;
    private TabInfo mLastTab;
    private TabHost.OnTabChangeListener mOnTabChangeListener;
    private final ArrayList<TabInfo> mTabs;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:android/support/v4/app/FragmentTabHost$SavedState.class */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: android.support.v4.app.FragmentTabHost.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel, null);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        String curTab;

        private SavedState(Parcel parcel) {
            super(parcel);
            this.curTab = parcel.readString();
        }

        /* synthetic */ SavedState(Parcel parcel, SavedState savedState) {
            this(parcel);
        }

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        public String toString() {
            return "FragmentTabHost.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " curTab=" + this.curTab + "}";
        }

        @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeString(this.curTab);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:android/support/v4/app/FragmentTabHost$TabInfo.class */
    public static final class TabInfo {
        private final Bundle args;
        private final Class<?> clss;
        private Fragment fragment;
        private final String tag;
    }

    public FragmentTabHost(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTabs = new ArrayList<>();
        initFragmentTabHost(context, attributeSet);
    }

    private FragmentTransaction doTabChanged(String str, FragmentTransaction fragmentTransaction) {
        TabInfo tabInfo = null;
        for (int i = 0; i < this.mTabs.size(); i++) {
            TabInfo tabInfo2 = this.mTabs.get(i);
            if (tabInfo2.tag.equals(str)) {
                tabInfo = tabInfo2;
            }
        }
        if (tabInfo == null) {
            throw new IllegalStateException("No tab known for tag " + str);
        }
        FragmentTransaction fragmentTransaction2 = fragmentTransaction;
        if (this.mLastTab != tabInfo) {
            fragmentTransaction2 = fragmentTransaction;
            if (fragmentTransaction == null) {
                fragmentTransaction2 = this.mFragmentManager.beginTransaction();
            }
            if (this.mLastTab != null && this.mLastTab.fragment != null) {
                fragmentTransaction2.detach(this.mLastTab.fragment);
            }
            if (tabInfo != null) {
                if (tabInfo.fragment == null) {
                    tabInfo.fragment = Fragment.instantiate(this.mContext, tabInfo.clss.getName(), tabInfo.args);
                    fragmentTransaction2.add(this.mContainerId, tabInfo.fragment, tabInfo.tag);
                } else {
                    fragmentTransaction2.attach(tabInfo.fragment);
                }
            }
            this.mLastTab = tabInfo;
        }
        return fragmentTransaction2;
    }

    private void initFragmentTabHost(Context context, AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, new int[]{16842995}, 0, 0);
        this.mContainerId = obtainStyledAttributes.getResourceId(0, 0);
        obtainStyledAttributes.recycle();
        super.setOnTabChangedListener(this);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        String currentTabTag = getCurrentTabTag();
        FragmentTransaction fragmentTransaction = null;
        int i = 0;
        while (i < this.mTabs.size()) {
            TabInfo tabInfo = this.mTabs.get(i);
            tabInfo.fragment = this.mFragmentManager.findFragmentByTag(tabInfo.tag);
            FragmentTransaction fragmentTransaction2 = fragmentTransaction;
            if (tabInfo.fragment != null) {
                if (tabInfo.fragment.isDetached()) {
                    fragmentTransaction2 = fragmentTransaction;
                } else if (tabInfo.tag.equals(currentTabTag)) {
                    this.mLastTab = tabInfo;
                    fragmentTransaction2 = fragmentTransaction;
                } else {
                    fragmentTransaction2 = fragmentTransaction;
                    if (fragmentTransaction == null) {
                        fragmentTransaction2 = this.mFragmentManager.beginTransaction();
                    }
                    fragmentTransaction2.detach(tabInfo.fragment);
                }
            }
            i++;
            fragmentTransaction = fragmentTransaction2;
        }
        this.mAttached = true;
        FragmentTransaction doTabChanged = doTabChanged(currentTabTag, fragmentTransaction);
        if (doTabChanged != null) {
            doTabChanged.commit();
            this.mFragmentManager.executePendingTransactions();
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mAttached = false;
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        setCurrentTabByTag(savedState.curTab);
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.curTab = getCurrentTabTag();
        return savedState;
    }

    @Override // android.widget.TabHost.OnTabChangeListener
    public void onTabChanged(String str) {
        FragmentTransaction doTabChanged;
        if (this.mAttached && (doTabChanged = doTabChanged(str, null)) != null) {
            doTabChanged.commit();
        }
        if (this.mOnTabChangeListener != null) {
            this.mOnTabChangeListener.onTabChanged(str);
        }
    }

    @Override // android.widget.TabHost
    public void setOnTabChangedListener(TabHost.OnTabChangeListener onTabChangeListener) {
        this.mOnTabChangeListener = onTabChangeListener;
    }

    @Override // android.widget.TabHost
    @Deprecated
    public void setup() {
        throw new IllegalStateException("Must call setup() that takes a Context and FragmentManager");
    }
}
