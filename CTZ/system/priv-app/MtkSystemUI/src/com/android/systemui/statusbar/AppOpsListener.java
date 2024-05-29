package com.android.systemui.statusbar;

import android.app.AppOpsManager;
import android.content.Context;
import com.android.systemui.Dependency;
import com.android.systemui.ForegroundServiceController;
/* loaded from: classes.dex */
public class AppOpsListener implements AppOpsManager.OnOpActiveChangedListener {
    protected static final int[] OPS = {26, 24, 27};
    protected final AppOpsManager mAppOps;
    private final Context mContext;
    protected NotificationEntryManager mEntryManager;
    private final ForegroundServiceController mFsc = (ForegroundServiceController) Dependency.get(ForegroundServiceController.class);
    protected NotificationPresenter mPresenter;

    public AppOpsListener(Context context) {
        this.mContext = context;
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
    }

    public void setUpWithPresenter(NotificationPresenter notificationPresenter, NotificationEntryManager notificationEntryManager) {
        this.mPresenter = notificationPresenter;
        this.mEntryManager = notificationEntryManager;
        this.mAppOps.startWatchingActive(OPS, this);
    }

    public void onOpActiveChanged(final int i, final int i2, final String str, final boolean z) {
        this.mFsc.onAppOpChanged(i, i2, str, z);
        this.mPresenter.getHandler().post(new Runnable() { // from class: com.android.systemui.statusbar.-$$Lambda$AppOpsListener$q1rvytNuPQxuSlMkTPgexkQ-Hb4
            @Override // java.lang.Runnable
            public final void run() {
                AppOpsListener.this.mEntryManager.updateNotificationsForAppOp(i, i2, str, z);
            }
        });
    }
}
