package com.android.settings.applications;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import com.android.settings.R;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.notification.NotificationBackend;
/* loaded from: classes.dex */
public class NotificationApps extends ManageApplications {
    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryLoader.SummaryProviderFactory() { // from class: com.android.settings.applications.NotificationApps.1
        @Override // com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory
        public SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader, null);
        }
    };

    /* loaded from: classes.dex */
    private static class SummaryProvider implements SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mLoader;
        private final NotificationBackend mNotificationBackend;

        /* synthetic */ SummaryProvider(Context context, SummaryLoader loader, SummaryProvider summaryProvider) {
            this(context, loader);
        }

        private SummaryProvider(Context context, SummaryLoader loader) {
            this.mContext = context;
            this.mLoader = loader;
            this.mNotificationBackend = new NotificationBackend();
        }

        /* JADX WARN: Type inference failed for: r0v0, types: [com.android.settings.applications.NotificationApps$SummaryProvider$1] */
        @Override // com.android.settings.dashboard.SummaryLoader.SummaryProvider
        public void setListening(boolean listening) {
            if (!listening) {
                return;
            }
            new AppCounter(this.mContext) { // from class: com.android.settings.applications.NotificationApps.SummaryProvider.1
                @Override // com.android.settings.applications.AppCounter
                protected void onCountComplete(int num) {
                    SummaryProvider.this.updateSummary(num);
                }

                @Override // com.android.settings.applications.AppCounter
                protected boolean includeInCount(ApplicationInfo info) {
                    return SummaryProvider.this.mNotificationBackend.getNotificationsBanned(info.packageName, info.uid);
                }
            }.execute(new Void[0]);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updateSummary(int count) {
            if (count == 0) {
                this.mLoader.setSummary(this, this.mContext.getString(R.string.notification_summary_none));
            } else {
                this.mLoader.setSummary(this, this.mContext.getResources().getQuantityString(R.plurals.notification_summary, count, Integer.valueOf(count)));
            }
        }
    }
}
