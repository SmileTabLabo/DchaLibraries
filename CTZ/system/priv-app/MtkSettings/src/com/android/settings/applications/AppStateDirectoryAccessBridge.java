package com.android.settings.applications;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.storage.StorageVolume;
import android.util.ArraySet;
import android.util.Log;
import com.android.settings.applications.AppStateBaseBridge;
import com.android.settingslib.applications.ApplicationsState;
import java.util.Set;
/* loaded from: classes.dex */
public class AppStateDirectoryAccessBridge extends AppStateBaseBridge {
    public static final ApplicationsState.AppFilter FILTER_APP_HAS_DIRECTORY_ACCESS = new ApplicationsState.AppFilter() { // from class: com.android.settings.applications.AppStateDirectoryAccessBridge.1
        private Set<String> mPackages;

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
            throw new UnsupportedOperationException("Need to call constructor that takes context");
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init(Context context) {
            this.mPackages = null;
            Uri build = new Uri.Builder().scheme("content").authority("com.android.documentsui.scopedAccess").appendPath("packages").appendPath("*").build();
            Cursor query = context.getContentResolver().query(build, StorageVolume.ScopedAccessProviderContract.TABLE_PACKAGES_COLUMNS, null, null);
            try {
                if (query == null) {
                    Log.w("DirectoryAccessBridge", "Didn't get cursor for " + build);
                    if (query != null) {
                        query.close();
                        return;
                    }
                    return;
                }
                int count = query.getCount();
                if (count == 0) {
                    Log.d("DirectoryAccessBridge", "No packages anymore (was " + this.mPackages + ")");
                    if (query != null) {
                        query.close();
                        return;
                    }
                    return;
                }
                this.mPackages = new ArraySet(count);
                while (query.moveToNext()) {
                    this.mPackages.add(query.getString(0));
                }
                Log.d("DirectoryAccessBridge", "init(): " + this.mPackages);
                if (query != null) {
                    query.close();
                }
            } catch (Throwable th) {
                try {
                    throw th;
                } catch (Throwable th2) {
                    if (query != null) {
                        if (th != null) {
                            try {
                                query.close();
                            } catch (Throwable th3) {
                                th.addSuppressed(th3);
                            }
                        } else {
                            query.close();
                        }
                    }
                    throw th2;
                }
            }
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(ApplicationsState.AppEntry appEntry) {
            return this.mPackages != null && this.mPackages.contains(appEntry.info.packageName);
        }
    };

    public AppStateDirectoryAccessBridge(ApplicationsState applicationsState, AppStateBaseBridge.Callback callback) {
        super(applicationsState, callback);
    }

    @Override // com.android.settings.applications.AppStateBaseBridge
    protected void loadAllExtraInfo() {
    }

    @Override // com.android.settings.applications.AppStateBaseBridge
    protected void updateExtraInfo(ApplicationsState.AppEntry appEntry, String str, int i) {
    }
}
