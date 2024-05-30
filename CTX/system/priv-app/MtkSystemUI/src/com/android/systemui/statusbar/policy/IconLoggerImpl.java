package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.metrics.LogMaker;
import android.os.Handler;
import android.os.Looper;
import android.util.ArraySet;
import com.android.internal.logging.MetricsLogger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
/* loaded from: classes.dex */
public class IconLoggerImpl implements IconLogger {
    protected static long MIN_LOG_INTERVAL = 1000;
    private final Context mContext;
    private final Handler mHandler;
    private final List<String> mIconIndex;
    private final ArraySet<String> mIcons = new ArraySet<>();
    private long mLastLog = System.currentTimeMillis();
    private final Runnable mLog = new Runnable() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$IconLoggerImpl$R-UnLK2IlCvdUfqG-_mI_-TEpe4
        @Override // java.lang.Runnable
        public final void run() {
            IconLoggerImpl.this.doLog();
        }
    };
    private final MetricsLogger mLogger;

    public IconLoggerImpl(Context context, Looper looper, MetricsLogger metricsLogger) {
        this.mContext = context;
        this.mHandler = new Handler(looper);
        this.mLogger = metricsLogger;
        this.mIconIndex = Arrays.asList(this.mContext.getResources().getStringArray(17236037));
        doLog();
    }

    @Override // com.android.systemui.statusbar.policy.IconLogger
    public void onIconShown(String str) {
        synchronized (this.mIcons) {
            if (this.mIcons.contains(str)) {
                return;
            }
            this.mIcons.add(str);
            if (!this.mHandler.hasCallbacks(this.mLog)) {
                this.mHandler.postDelayed(this.mLog, MIN_LOG_INTERVAL);
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.IconLogger
    public void onIconHidden(String str) {
        synchronized (this.mIcons) {
            if (this.mIcons.contains(str)) {
                this.mIcons.remove(str);
                if (!this.mHandler.hasCallbacks(this.mLog)) {
                    this.mHandler.postDelayed(this.mLog, MIN_LOG_INTERVAL);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doLog() {
        ArraySet<String> arraySet;
        long currentTimeMillis = System.currentTimeMillis();
        long j = currentTimeMillis - this.mLastLog;
        this.mLastLog = currentTimeMillis;
        synchronized (this.mIcons) {
            arraySet = new ArraySet<>(this.mIcons);
        }
        this.mLogger.write(new LogMaker(1093).setType(4).setLatency(j).addTaggedData(1095, Integer.valueOf(arraySet.size())).addTaggedData(1094, Integer.valueOf(getBitField(arraySet))));
    }

    private int getBitField(ArraySet<String> arraySet) {
        Iterator<String> it = arraySet.iterator();
        int i = 0;
        while (it.hasNext()) {
            int indexOf = this.mIconIndex.indexOf(it.next());
            if (indexOf >= 0) {
                i |= 1 << indexOf;
            }
        }
        return i;
    }
}
