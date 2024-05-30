package com.android.settings.applications;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.util.IconDrawableFactory;
import android.util.Log;
import android.util.Pair;
import com.android.settings.R;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.applications.AppUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
/* loaded from: classes.dex */
public class DirectoryAccessDetails extends AppInfoBase {
    private boolean mCreated;

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        if (this.mCreated) {
            Log.w("DirectoryAccessDetails", "onActivityCreated(): ignoring duplicate call");
            return;
        }
        this.mCreated = true;
        if (this.mPackageInfo == null) {
            Log.w("DirectoryAccessDetails", "onActivityCreated(): no package info");
            return;
        }
        Activity activity = getActivity();
        getPreferenceScreen().addPreference(EntityHeaderController.newInstance(activity, this, null).setRecyclerView(getListView(), getLifecycle()).setIcon(IconDrawableFactory.newInstance(getPrefContext()).getBadgedIcon(this.mPackageInfo.applicationInfo)).setLabel(this.mPackageInfo.applicationInfo.loadLabel(this.mPm)).setIsInstantApp(AppUtils.isInstant(this.mPackageInfo.applicationInfo)).setPackageName(this.mPackageName).setUid(this.mPackageInfo.applicationInfo.uid).setHasAppInfoLink(false).setButtonActions(0, 0).done(activity, getPrefContext()));
    }

    @Override // com.android.settings.applications.AppInfoBase, com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.directory_access_details);
    }

    @Override // com.android.settings.applications.AppInfoBase
    protected boolean refreshUi() {
        final Context prefContext = getPrefContext();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.removeAll();
        HashMap hashMap = new HashMap();
        final Uri build = new Uri.Builder().scheme("content").authority("com.android.documentsui.scopedAccess").appendPath("permissions").appendPath("*").build();
        Cursor query = prefContext.getContentResolver().query(build, StorageVolume.ScopedAccessProviderContract.TABLE_PERMISSIONS_COLUMNS, null, new String[]{this.mPackageName}, null);
        try {
            if (query == null) {
                Log.w("DirectoryAccessDetails", "Didn't get cursor for " + this.mPackageName);
                if (query != null) {
                    query.close();
                }
                return true;
            } else if (query.getCount() == 0) {
                Log.w("DirectoryAccessDetails", "No permissions for " + this.mPackageName);
                if (query != null) {
                    query.close();
                }
                return true;
            } else {
                while (query.moveToNext()) {
                    String string = query.getString(0);
                    String string2 = query.getString(1);
                    String string3 = query.getString(2);
                    boolean z = query.getInt(3) == 1;
                    Log.v("DirectoryAccessDetails", "Pkg:" + string + " uuid: " + string2 + " dir: " + string3 + " granted:" + z);
                    if (!this.mPackageName.equals(string)) {
                        Log.w("DirectoryAccessDetails", "Ignoring " + string2 + "/" + string3 + " due to package mismatch: expected " + this.mPackageName + ", got " + string);
                    } else if (string2 != null) {
                        ExternalVolume externalVolume = (ExternalVolume) hashMap.get(string2);
                        if (externalVolume == null) {
                            externalVolume = new ExternalVolume(string2);
                            hashMap.put(string2, externalVolume);
                        }
                        if (string3 == null) {
                            externalVolume.granted = z;
                        } else {
                            externalVolume.children.add(new Pair<>(string3, Boolean.valueOf(z)));
                        }
                    } else if (string3 == null) {
                        Log.wtf("DirectoryAccessDetails", "Ignoring permission on primary storage root");
                    } else {
                        preferenceScreen.addPreference(newPreference(prefContext, string3, build, null, string3, z, null));
                    }
                }
                if (query != null) {
                    query.close();
                }
                Log.v("DirectoryAccessDetails", "external volumes: " + hashMap);
                if (hashMap.isEmpty()) {
                    return true;
                }
                StorageManager storageManager = (StorageManager) prefContext.getSystemService(StorageManager.class);
                List<VolumeInfo> volumes = storageManager.getVolumes();
                if (volumes.isEmpty()) {
                    Log.w("DirectoryAccessDetails", "StorageManager returned no secondary volumes");
                    return true;
                }
                HashMap hashMap2 = new HashMap(volumes.size());
                for (VolumeInfo volumeInfo : volumes) {
                    String fsUuid = volumeInfo.getFsUuid();
                    if (fsUuid != null) {
                        String bestVolumeDescription = storageManager.getBestVolumeDescription(volumeInfo);
                        if (bestVolumeDescription == null) {
                            Log.w("DirectoryAccessDetails", "No description for " + volumeInfo + "; using uuid instead: " + fsUuid);
                            bestVolumeDescription = fsUuid;
                        }
                        hashMap2.put(fsUuid, bestVolumeDescription);
                    }
                }
                Log.v("DirectoryAccessDetails", "UUID -> name mapping: " + hashMap2);
                for (final ExternalVolume externalVolume2 : hashMap.values()) {
                    final String str = (String) hashMap2.get(externalVolume2.uuid);
                    if (str == null) {
                        Log.w("DirectoryAccessDetails", "Ignoring entry for invalid UUID: " + externalVolume2.uuid);
                    } else {
                        final PreferenceCategory preferenceCategory = new PreferenceCategory(prefContext);
                        preferenceScreen.addPreference(preferenceCategory);
                        final HashSet hashSet = new HashSet(externalVolume2.children.size());
                        preferenceCategory.addPreference(newPreference(prefContext, str, build, externalVolume2.uuid, null, externalVolume2.granted, hashSet));
                        externalVolume2.children.forEach(new Consumer() { // from class: com.android.settings.applications.-$$Lambda$DirectoryAccessDetails$K0N0BhiTAIxLxuaXU9qwR-rLnAY
                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                DirectoryAccessDetails.lambda$refreshUi$0(DirectoryAccessDetails.this, prefContext, str, build, externalVolume2, preferenceCategory, hashSet, (Pair) obj);
                            }
                        });
                    }
                }
                return true;
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

    public static /* synthetic */ void lambda$refreshUi$0(DirectoryAccessDetails directoryAccessDetails, Context context, String str, Uri uri, ExternalVolume externalVolume, PreferenceCategory preferenceCategory, Set set, Pair pair) {
        String str2 = (String) pair.first;
        SwitchPreference newPreference = directoryAccessDetails.newPreference(context, context.getResources().getString(R.string.directory_on_volume, str, str2), uri, externalVolume.uuid, str2, ((Boolean) pair.second).booleanValue(), null);
        preferenceCategory.addPreference(newPreference);
        set.add(newPreference);
    }

    private SwitchPreference newPreference(final Context context, String str, final Uri uri, final String str2, final String str3, boolean z, final Set<SwitchPreference> set) {
        SwitchPreference switchPreference = new SwitchPreference(context);
        switchPreference.setKey(String.format("%s:%s", str2, str3));
        switchPreference.setTitle(str);
        switchPreference.setChecked(z);
        switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: com.android.settings.applications.-$$Lambda$DirectoryAccessDetails$lMkU9x3CDhpq6XQS106C_-FREgc
            @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
            public final boolean onPreferenceChange(Preference preference, Object obj) {
                return DirectoryAccessDetails.lambda$newPreference$1(DirectoryAccessDetails.this, context, uri, str2, str3, set, preference, obj);
            }
        });
        return switchPreference;
    }

    public static /* synthetic */ boolean lambda$newPreference$1(DirectoryAccessDetails directoryAccessDetails, Context context, Uri uri, String str, String str2, Set set, Preference preference, Object obj) {
        if (!Boolean.class.isInstance(obj)) {
            Log.wtf("DirectoryAccessDetails", "Invalid value from switch: " + obj);
            return true;
        }
        boolean booleanValue = ((Boolean) obj).booleanValue();
        directoryAccessDetails.resetDoNotAskAgain(context, booleanValue, uri, str, str2);
        if (set != null) {
            boolean z = !booleanValue;
            Iterator it = set.iterator();
            while (it.hasNext()) {
                ((SwitchPreference) it.next()).setVisible(z);
            }
        }
        return true;
    }

    private void resetDoNotAskAgain(Context context, boolean z, Uri uri, String str, String str2) {
        Log.d("DirectoryAccessDetails", "Asking " + uri + " to update " + str + "/" + str2 + " to " + z);
        ContentValues contentValues = new ContentValues(1);
        contentValues.put("granted", Boolean.valueOf(z));
        int update = context.getContentResolver().update(uri, contentValues, null, new String[]{this.mPackageName, str, str2});
        Log.d("DirectoryAccessDetails", "Updated " + update + " entries for " + str + "/" + str2);
    }

    @Override // com.android.settings.applications.AppInfoBase
    protected AlertDialog createDialog(int i, int i2) {
        return null;
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 1284;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class ExternalVolume {
        final List<Pair<String, Boolean>> children = new ArrayList();
        boolean granted;
        final String uuid;

        ExternalVolume(String str) {
            this.uuid = str;
        }

        public String toString() {
            return "ExternalVolume: [uuid=" + this.uuid + ", granted=" + this.granted + ", children=" + this.children + "]";
        }
    }
}
