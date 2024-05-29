package com.android.settings.applications;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.BidiFormatter;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.util.MemInfoReader;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.applications.RunningState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
/* loaded from: classes.dex */
public class RunningProcessesView extends FrameLayout implements AdapterView.OnItemClickListener, AbsListView.RecyclerListener, RunningState.OnRefreshUiListener {
    long FOREGROUND_APP_MEM;
    long SECONDARY_SERVER_MEM;
    final HashMap<View, ActiveItem> mActiveItems;
    ServiceListAdapter mAdapter;
    ActivityManager mAm;
    TextView mAppsProcessPrefix;
    TextView mAppsProcessText;
    TextView mBackgroundProcessPrefix;
    TextView mBackgroundProcessText;
    StringBuilder mBuilder;
    LinearColorBar mColorBar;
    long mCurHighRam;
    long mCurLowRam;
    long mCurMedRam;
    RunningState.BaseItem mCurSelected;
    boolean mCurShowCached;
    long mCurTotalRam;
    Runnable mDataAvail;
    TextView mForegroundProcessPrefix;
    TextView mForegroundProcessText;
    View mHeader;
    ListView mListView;
    MemInfoReader mMemInfoReader;
    final int mMyUserId;
    Fragment mOwner;
    RunningState mState;

    /* loaded from: classes.dex */
    static class TimeTicker extends TextView {
    }

    /* loaded from: classes.dex */
    public static class ActiveItem {
        long mFirstRunTime;
        ViewHolder mHolder;
        RunningState.BaseItem mItem;
        View mRootView;
        boolean mSetBackground;

        /* JADX INFO: Access modifiers changed from: package-private */
        public void updateTime(Context context, StringBuilder builder) {
            TextView uptimeView = null;
            if (this.mItem instanceof RunningState.ServiceItem) {
                uptimeView = this.mHolder.size;
            } else {
                String size = this.mItem.mSizeStr != null ? this.mItem.mSizeStr : "";
                if (!size.equals(this.mItem.mCurSizeStr)) {
                    this.mItem.mCurSizeStr = size;
                    this.mHolder.size.setText(size);
                }
                if (this.mItem.mBackground) {
                    if (!this.mSetBackground) {
                        this.mSetBackground = true;
                        this.mHolder.uptime.setText("");
                    }
                } else if (this.mItem instanceof RunningState.MergedItem) {
                    uptimeView = this.mHolder.uptime;
                }
            }
            if (uptimeView == null) {
                return;
            }
            this.mSetBackground = false;
            if (this.mFirstRunTime >= 0) {
                uptimeView.setText(DateUtils.formatElapsedTime(builder, (SystemClock.elapsedRealtime() - this.mFirstRunTime) / 1000));
                return;
            }
            boolean isService = false;
            if (this.mItem instanceof RunningState.MergedItem) {
                isService = ((RunningState.MergedItem) this.mItem).mServices.size() > 0;
            }
            if (isService) {
                uptimeView.setText(context.getResources().getText(R.string.service_restarting));
            } else {
                uptimeView.setText("");
            }
        }
    }

    /* loaded from: classes.dex */
    public static class ViewHolder {
        public TextView description;
        public ImageView icon;
        public TextView name;
        public View rootView;
        public TextView size;
        public TextView uptime;

        public ViewHolder(View v) {
            this.rootView = v;
            this.icon = (ImageView) v.findViewById(R.id.icon);
            this.name = (TextView) v.findViewById(R.id.name);
            this.description = (TextView) v.findViewById(R.id.description);
            this.size = (TextView) v.findViewById(R.id.size);
            this.uptime = (TextView) v.findViewById(R.id.uptime);
            v.setTag(this);
        }

        public ActiveItem bind(RunningState state, RunningState.BaseItem item, StringBuilder builder) {
            ActiveItem ai;
            synchronized (state.mLock) {
                PackageManager pm = this.rootView.getContext().getPackageManager();
                if (item.mPackageInfo == null && (item instanceof RunningState.MergedItem)) {
                    RunningState.MergedItem mergedItem = (RunningState.MergedItem) item;
                    if (mergedItem.mProcess != null) {
                        ((RunningState.MergedItem) item).mProcess.ensureLabel(pm);
                        item.mPackageInfo = ((RunningState.MergedItem) item).mProcess.mPackageInfo;
                        item.mDisplayLabel = ((RunningState.MergedItem) item).mProcess.mDisplayLabel;
                    }
                }
                this.name.setText(item.mDisplayLabel);
                ai = new ActiveItem();
                ai.mRootView = this.rootView;
                ai.mItem = item;
                ai.mHolder = this;
                ai.mFirstRunTime = item.mActiveSince;
                if (item.mBackground) {
                    this.description.setText(this.rootView.getContext().getText(R.string.cached));
                } else {
                    this.description.setText(item.mDescription);
                }
                item.mCurSizeStr = null;
                this.icon.setImageDrawable(item.loadIcon(this.rootView.getContext(), state));
                this.icon.setVisibility(0);
                ai.updateTime(this.rootView.getContext(), builder);
            }
            return ai;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class ServiceListAdapter extends BaseAdapter {
        final LayoutInflater mInflater;
        final ArrayList<RunningState.MergedItem> mItems = new ArrayList<>();
        ArrayList<RunningState.MergedItem> mOrigItems;
        boolean mShowBackground;
        final RunningState mState;

        ServiceListAdapter(RunningState state) {
            this.mState = state;
            this.mInflater = (LayoutInflater) RunningProcessesView.this.getContext().getSystemService("layout_inflater");
            refreshItems();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public void setShowBackground(boolean showBackground) {
            if (this.mShowBackground == showBackground) {
                return;
            }
            this.mShowBackground = showBackground;
            this.mState.setWatchingBackgroundItems(showBackground);
            refreshItems();
            RunningProcessesView.this.refreshUi(true);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public boolean getShowBackground() {
            return this.mShowBackground;
        }

        void refreshItems() {
            ArrayList<RunningState.MergedItem> newItems = this.mShowBackground ? this.mState.getCurrentBackgroundItems() : this.mState.getCurrentMergedItems();
            if (this.mOrigItems == newItems) {
                return;
            }
            this.mOrigItems = newItems;
            if (newItems == null) {
                this.mItems.clear();
                return;
            }
            this.mItems.clear();
            this.mItems.addAll(newItems);
            if (!this.mShowBackground) {
                return;
            }
            Collections.sort(this.mItems, this.mState.mBackgroundComparator);
        }

        @Override // android.widget.BaseAdapter, android.widget.Adapter
        public boolean hasStableIds() {
            return true;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return this.mItems.size();
        }

        @Override // android.widget.BaseAdapter, android.widget.Adapter
        public boolean isEmpty() {
            return this.mState.hasData() && this.mItems.size() == 0;
        }

        @Override // android.widget.Adapter
        public Object getItem(int position) {
            return this.mItems.get(position);
        }

        @Override // android.widget.Adapter
        public long getItemId(int position) {
            return this.mItems.get(position).hashCode();
        }

        @Override // android.widget.BaseAdapter, android.widget.ListAdapter
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override // android.widget.BaseAdapter, android.widget.ListAdapter
        public boolean isEnabled(int position) {
            return !this.mItems.get(position).mIsProcess;
        }

        @Override // android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            if (convertView == null) {
                v = newView(parent);
            } else {
                v = convertView;
            }
            bindView(v, position);
            return v;
        }

        public View newView(ViewGroup parent) {
            View v = this.mInflater.inflate(R.layout.running_processes_item, parent, false);
            new ViewHolder(v);
            return v;
        }

        public void bindView(View view, int position) {
            synchronized (this.mState.mLock) {
                if (position >= this.mItems.size()) {
                    return;
                }
                ViewHolder vh = (ViewHolder) view.getTag();
                RunningState.MergedItem item = this.mItems.get(position);
                ActiveItem ai = vh.bind(this.mState, item, RunningProcessesView.this.mBuilder);
                RunningProcessesView.this.mActiveItems.put(view, ai);
            }
        }
    }

    void refreshUi(boolean dataChanged) {
        long lowRam;
        long medRam;
        if (dataChanged) {
            ServiceListAdapter adapter = this.mAdapter;
            adapter.refreshItems();
            adapter.notifyDataSetChanged();
        }
        if (this.mDataAvail != null) {
            this.mDataAvail.run();
            this.mDataAvail = null;
        }
        this.mMemInfoReader.readMemInfo();
        boolean isDefaultCacheFree = SystemProperties.getBoolean("ro.default_cache_free", false);
        Log.d("RunningProcessesView", "isDefaultCacheFree = " + isDefaultCacheFree);
        synchronized (this.mState.mLock) {
            float zramCompressRatio = Process.getZramCompressRatio();
            long extraAvailableSize = 0;
            if (!isDefaultCacheFree) {
                long anonToCompress = (Process.getLruAnonMemory() - 15728640) - this.mState.mBackgroundProcessMemory;
                Log.d("RunningProcessesView", "Process.getLruAnonMemory() = " + Process.getLruAnonMemory());
                Log.d("RunningProcessesView", "mState.mBackgroundProcessMemory = " + this.mState.mBackgroundProcessMemory);
                if (anonToCompress > 0) {
                    extraAvailableSize = ((float) anonToCompress) * (1.0f - (1.0f / zramCompressRatio));
                } else {
                    Log.d("RunningProcessesView", "!!!ERROR!!! annonToCompress = " + anonToCompress);
                }
                if (extraAvailableSize > 0) {
                    extraAvailableSize = (((this.mMemInfoReader.getRawInfo()[9] * 1024) + extraAvailableSize) - (this.mMemInfoReader.getRawInfo()[2] * 1024)) - this.FOREGROUND_APP_MEM;
                }
            }
            Log.d("RunningProcessesView", "extraAvailableSize = " + extraAvailableSize);
            if (this.mCurShowCached != this.mAdapter.mShowBackground) {
                this.mCurShowCached = this.mAdapter.mShowBackground;
                if (this.mCurShowCached) {
                    this.mForegroundProcessPrefix.setText(getResources().getText(R.string.running_processes_header_used_prefix));
                    this.mAppsProcessPrefix.setText(getResources().getText(R.string.running_processes_header_cached_prefix));
                } else {
                    this.mForegroundProcessPrefix.setText(getResources().getText(R.string.running_processes_header_system_prefix));
                    this.mAppsProcessPrefix.setText(getResources().getText(R.string.running_processes_header_apps_prefix));
                }
            }
            long totalRam = this.mMemInfoReader.getTotalSize();
            Log.d("RunningProcessesView", "getFreeSize = " + this.mMemInfoReader.getFreeSize());
            Log.d("RunningProcessesView", "getCachedSize = " + this.mMemInfoReader.getCachedSize());
            Log.d("RunningProcessesView", "mBackgroundProcessMemory = " + this.mState.mBackgroundProcessMemory);
            Log.d("RunningProcessesView", "extraAvailableSize = " + extraAvailableSize);
            Log.d("RunningProcessesView", "mBackgroundProcessSwapMemory = " + this.mState.mBackgroundProcessSwapMemory);
            Log.d("RunningProcessesView", "Process.getZramCompressRatio() = " + zramCompressRatio);
            if (this.mCurShowCached) {
                lowRam = this.mMemInfoReader.getFreeSize() + this.mMemInfoReader.getCachedSize() + extraAvailableSize;
                medRam = ((float) this.mState.mBackgroundProcessMemory) + ((((float) this.mState.mBackgroundProcessSwapMemory) * 1.0f) / zramCompressRatio);
            } else {
                lowRam = ((float) (this.mMemInfoReader.getFreeSize() + this.mMemInfoReader.getCachedSize() + this.mState.mBackgroundProcessMemory + extraAvailableSize)) + ((((float) this.mState.mBackgroundProcessSwapMemory) * 1.0f) / zramCompressRatio);
                medRam = this.mState.mServiceProcessMemory;
            }
            Log.d("RunningProcessesView", "lowRam = " + lowRam);
            Log.d("RunningProcessesView", "medRam = " + medRam);
            long highRam = (totalRam - medRam) - lowRam;
            if (highRam < 0) {
                highRam = this.mCurHighRam;
            }
            if (this.mCurTotalRam != totalRam || this.mCurHighRam != highRam || this.mCurMedRam != medRam || this.mCurLowRam != lowRam) {
                this.mCurTotalRam = totalRam;
                this.mCurHighRam = highRam;
                this.mCurMedRam = medRam;
                this.mCurLowRam = lowRam;
                BidiFormatter bidiFormatter = BidiFormatter.getInstance();
                String sizeStr = bidiFormatter.unicodeWrap(Formatter.formatShortFileSize(getContext(), lowRam));
                this.mBackgroundProcessText.setText(getResources().getString(R.string.running_processes_header_ram, sizeStr));
                String sizeStr2 = bidiFormatter.unicodeWrap(Formatter.formatShortFileSize(getContext(), medRam));
                this.mAppsProcessText.setText(getResources().getString(R.string.running_processes_header_ram, sizeStr2));
                String sizeStr3 = bidiFormatter.unicodeWrap(Formatter.formatShortFileSize(getContext(), highRam));
                this.mForegroundProcessText.setText(getResources().getString(R.string.running_processes_header_ram, sizeStr3));
                this.mColorBar.setRatios(((float) highRam) / ((float) totalRam), ((float) medRam) / ((float) totalRam), ((float) lowRam) / ((float) totalRam));
            }
        }
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        ListView l = (ListView) parent;
        RunningState.MergedItem mi = (RunningState.MergedItem) l.getAdapter().getItem(position);
        this.mCurSelected = mi;
        startServiceDetailsActivity(mi);
    }

    private void startServiceDetailsActivity(RunningState.MergedItem mi) {
        if (this.mOwner == null || mi == null) {
            return;
        }
        Bundle args = new Bundle();
        if (mi.mProcess != null) {
            args.putInt("uid", mi.mProcess.mUid);
            args.putString("process", mi.mProcess.mProcessName);
        }
        args.putInt("user_id", mi.mUserId);
        args.putBoolean("background", this.mAdapter.mShowBackground);
        SettingsActivity sa = (SettingsActivity) this.mOwner.getActivity();
        sa.startPreferencePanel(RunningServiceDetails.class.getName(), args, R.string.runningservicedetails_settings_title, null, null, 0);
    }

    @Override // android.widget.AbsListView.RecyclerListener
    public void onMovedToScrapHeap(View view) {
        this.mActiveItems.remove(view);
    }

    public RunningProcessesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mActiveItems = new HashMap<>();
        this.mBuilder = new StringBuilder(128);
        this.mCurTotalRam = -1L;
        this.mCurHighRam = -1L;
        this.mCurMedRam = -1L;
        this.mCurLowRam = -1L;
        this.mCurShowCached = false;
        this.mMemInfoReader = new MemInfoReader();
        this.mMyUserId = UserHandle.myUserId();
    }

    public void doCreate() {
        this.mAm = (ActivityManager) getContext().getSystemService("activity");
        this.mState = RunningState.getInstance(getContext());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        inflater.inflate(R.layout.running_processes_view, this);
        this.mListView = (ListView) findViewById(16908298);
        View emptyView = findViewById(16908292);
        if (emptyView != null) {
            this.mListView.setEmptyView(emptyView);
        }
        this.mListView.setOnItemClickListener(this);
        this.mListView.setRecyclerListener(this);
        this.mAdapter = new ServiceListAdapter(this.mState);
        this.mListView.setAdapter((ListAdapter) this.mAdapter);
        this.mHeader = inflater.inflate(R.layout.running_processes_header, (ViewGroup) null);
        this.mListView.addHeaderView(this.mHeader, null, false);
        this.mColorBar = (LinearColorBar) this.mHeader.findViewById(R.id.color_bar);
        Context context = getContext();
        this.mColorBar.setColors(context.getColor(R.color.running_processes_system_ram), context.getColor(R.color.running_processes_apps_ram), context.getColor(R.color.running_processes_free_ram));
        this.mBackgroundProcessPrefix = (TextView) this.mHeader.findViewById(R.id.freeSizePrefix);
        this.mAppsProcessPrefix = (TextView) this.mHeader.findViewById(R.id.appsSizePrefix);
        this.mForegroundProcessPrefix = (TextView) this.mHeader.findViewById(R.id.systemSizePrefix);
        this.mBackgroundProcessText = (TextView) this.mHeader.findViewById(R.id.freeSize);
        this.mAppsProcessText = (TextView) this.mHeader.findViewById(R.id.appsSize);
        this.mForegroundProcessText = (TextView) this.mHeader.findViewById(R.id.systemSize);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        this.mAm.getMemoryInfo(memInfo);
        this.SECONDARY_SERVER_MEM = memInfo.secondaryServerThreshold;
        this.FOREGROUND_APP_MEM = memInfo.foregroundAppThreshold;
    }

    public void doPause() {
        this.mState.pause();
        this.mDataAvail = null;
        this.mOwner = null;
    }

    public boolean doResume(Fragment owner, Runnable dataAvail) {
        this.mOwner = owner;
        this.mState.resume(this);
        if (this.mState.hasData()) {
            refreshUi(true);
            return true;
        }
        this.mDataAvail = dataAvail;
        return false;
    }

    void updateTimes() {
        Iterator<ActiveItem> it = this.mActiveItems.values().iterator();
        while (it.hasNext()) {
            ActiveItem ai = it.next();
            if (ai.mRootView.getWindowToken() == null) {
                it.remove();
            } else {
                ai.updateTime(getContext(), this.mBuilder);
            }
        }
    }

    @Override // com.android.settings.applications.RunningState.OnRefreshUiListener
    public void onRefreshUi(int what) {
        switch (what) {
            case 0:
                updateTimes();
                return;
            case 1:
                refreshUi(false);
                updateTimes();
                return;
            case 2:
                refreshUi(true);
                updateTimes();
                return;
            default:
                return;
        }
    }
}
