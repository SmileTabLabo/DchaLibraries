package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.IconDrawableFactory;
import android.util.Pair;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.applications.AppInfoBase;
import com.android.settings.notification.EmptyTextSettings;
import com.android.settings.widget.AppPreference;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
/* loaded from: classes.dex */
public class PictureInPictureSettings extends EmptyTextSettings {
    private Context mContext;
    private IconDrawableFactory mIconDrawableFactory;
    private PackageManagerWrapper mPackageManager;
    private UserManager mUserManager;
    private static final String TAG = PictureInPictureSettings.class.getSimpleName();
    @VisibleForTesting
    static final List<String> IGNORE_PACKAGE_LIST = new ArrayList();

    static {
        IGNORE_PACKAGE_LIST.add("com.android.systemui");
    }

    /* loaded from: classes.dex */
    static class AppComparator implements Comparator<Pair<ApplicationInfo, Integer>> {
        private final Collator mCollator = Collator.getInstance();
        private final PackageManager mPm;

        public AppComparator(PackageManager packageManager) {
            this.mPm = packageManager;
        }

        @Override // java.util.Comparator
        public final int compare(Pair<ApplicationInfo, Integer> pair, Pair<ApplicationInfo, Integer> pair2) {
            CharSequence loadLabel = ((ApplicationInfo) pair.first).loadLabel(this.mPm);
            if (loadLabel == null) {
                loadLabel = ((ApplicationInfo) pair.first).name;
            }
            CharSequence loadLabel2 = ((ApplicationInfo) pair2.first).loadLabel(this.mPm);
            if (loadLabel2 == null) {
                loadLabel2 = ((ApplicationInfo) pair2.first).name;
            }
            int compare = this.mCollator.compare(loadLabel.toString(), loadLabel2.toString());
            if (compare != 0) {
                return compare;
            }
            return ((Integer) pair.second).intValue() - ((Integer) pair2.second).intValue();
        }
    }

    public static boolean checkPackageHasPictureInPictureActivities(String str, ActivityInfo[] activityInfoArr) {
        if (!IGNORE_PACKAGE_LIST.contains(str) && activityInfoArr != null) {
            for (int length = activityInfoArr.length - 1; length >= 0; length--) {
                if (activityInfoArr[length].supportsPictureInPicture()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mContext = getActivity();
        this.mPackageManager = new PackageManagerWrapper(this.mContext.getPackageManager());
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mIconDrawableFactory = IconDrawableFactory.newInstance(this.mContext);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.removeAll();
        PackageManager packageManager = this.mPackageManager.getPackageManager();
        ArrayList<Pair<ApplicationInfo, Integer>> collectPipApps = collectPipApps(UserHandle.myUserId());
        Collections.sort(collectPipApps, new AppComparator(packageManager));
        Context prefContext = getPrefContext();
        Iterator<Pair<ApplicationInfo, Integer>> it = collectPipApps.iterator();
        while (it.hasNext()) {
            Pair<ApplicationInfo, Integer> next = it.next();
            final ApplicationInfo applicationInfo = (ApplicationInfo) next.first;
            int intValue = ((Integer) next.second).intValue();
            UserHandle of = UserHandle.of(intValue);
            final String str = applicationInfo.packageName;
            CharSequence loadLabel = applicationInfo.loadLabel(packageManager);
            AppPreference appPreference = new AppPreference(prefContext);
            appPreference.setIcon(this.mIconDrawableFactory.getBadgedIcon(applicationInfo, intValue));
            appPreference.setTitle(packageManager.getUserBadgedLabel(loadLabel, of));
            appPreference.setSummary(PictureInPictureDetails.getPreferenceSummary(prefContext, applicationInfo.uid, str));
            appPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.android.settings.applications.appinfo.PictureInPictureSettings.1
                @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    AppInfoBase.startAppInfoFragment(PictureInPictureDetails.class, R.string.picture_in_picture_app_detail_title, str, applicationInfo.uid, PictureInPictureSettings.this, -1, PictureInPictureSettings.this.getMetricsCategory());
                    return true;
                }
            });
            preferenceScreen.addPreference(appPreference);
        }
    }

    @Override // com.android.settings.notification.EmptyTextSettings, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        setEmptyText(R.string.picture_in_picture_empty_text);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.picture_in_picture_settings;
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 812;
    }

    ArrayList<Pair<ApplicationInfo, Integer>> collectPipApps(int i) {
        ArrayList<Pair<ApplicationInfo, Integer>> arrayList = new ArrayList<>();
        ArrayList arrayList2 = new ArrayList();
        for (UserInfo userInfo : this.mUserManager.getProfiles(i)) {
            arrayList2.add(Integer.valueOf(userInfo.id));
        }
        Iterator it = arrayList2.iterator();
        while (it.hasNext()) {
            int intValue = ((Integer) it.next()).intValue();
            for (PackageInfo packageInfo : this.mPackageManager.getInstalledPackagesAsUser(1, intValue)) {
                if (checkPackageHasPictureInPictureActivities(packageInfo.packageName, packageInfo.activities)) {
                    arrayList.add(new Pair<>(packageInfo.applicationInfo, Integer.valueOf(intValue)));
                }
            }
        }
        return arrayList;
    }
}
