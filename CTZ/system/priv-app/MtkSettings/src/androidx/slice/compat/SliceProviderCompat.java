package androidx.slice.compat;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.v4.util.ArraySet;
import android.util.Log;
import androidx.slice.Slice;
import androidx.slice.SliceProvider;
import androidx.slice.SliceSpec;
import androidx.versionedparcelable.ParcelUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
/* loaded from: classes.dex */
public class SliceProviderCompat {
    private String mCallback;
    private final Context mContext;
    private CompatPermissionManager mPermissionManager;
    private CompatPinnedList mPinnedList;
    private final SliceProvider mProvider;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mAnr = new Runnable() { // from class: androidx.slice.compat.SliceProviderCompat.1
        @Override // java.lang.Runnable
        public void run() {
            Process.sendSignal(Process.myPid(), 3);
            Log.wtf("SliceProviderCompat", "Timed out while handling slice callback " + SliceProviderCompat.this.mCallback);
        }
    };

    public SliceProviderCompat(SliceProvider provider, CompatPermissionManager permissionManager, Context context) {
        this.mProvider = provider;
        this.mContext = context;
        String prefsFile = "slice_data_" + getClass().getName();
        SharedPreferences allFiles = this.mContext.getSharedPreferences("slice_data_all_slice_files", 0);
        Set<String> files = allFiles.getStringSet("slice_data_all_slice_files", Collections.emptySet());
        if (!files.contains(prefsFile)) {
            Set<String> files2 = new ArraySet<>(files);
            files2.add(prefsFile);
            allFiles.edit().putStringSet("slice_data_all_slice_files", files2).commit();
        }
        this.mPinnedList = new CompatPinnedList(this.mContext, prefsFile);
        this.mPermissionManager = permissionManager;
    }

    private Context getContext() {
        return this.mContext;
    }

    public String getCallingPackage() {
        return this.mProvider.getCallingPackage();
    }

    public Bundle call(String method, String arg, Bundle extras) {
        if (method.equals("bind_slice")) {
            Uri uri = (Uri) extras.getParcelable("slice_uri");
            Set<SliceSpec> specs = getSpecs(extras);
            Slice s = handleBindSlice(uri, specs, getCallingPackage());
            Bundle b = new Bundle();
            if ("supports_versioned_parcelable".equals(arg)) {
                b.putParcelable("slice", s != null ? ParcelUtils.toParcelable(s) : null);
            } else {
                b.putParcelable("slice", s != null ? s.toBundle() : null);
            }
            return b;
        } else if (method.equals("map_slice")) {
            Intent intent = (Intent) extras.getParcelable("slice_intent");
            Uri uri2 = this.mProvider.onMapIntentToUri(intent);
            Bundle b2 = new Bundle();
            if (uri2 != null) {
                Set<SliceSpec> specs2 = getSpecs(extras);
                Slice s2 = handleBindSlice(uri2, specs2, getCallingPackage());
                if ("supports_versioned_parcelable".equals(arg)) {
                    b2.putParcelable("slice", s2 != null ? ParcelUtils.toParcelable(s2) : null);
                } else {
                    b2.putParcelable("slice", s2 != null ? s2.toBundle() : null);
                }
            } else {
                b2.putParcelable("slice", null);
            }
            return b2;
        } else if (method.equals("map_only")) {
            Intent intent2 = (Intent) extras.getParcelable("slice_intent");
            Uri uri3 = this.mProvider.onMapIntentToUri(intent2);
            Bundle b3 = new Bundle();
            b3.putParcelable("slice", uri3);
            return b3;
        } else if (method.equals("pin_slice")) {
            Uri uri4 = (Uri) extras.getParcelable("slice_uri");
            Set<SliceSpec> specs3 = getSpecs(extras);
            String pkg = extras.getString("pkg");
            if (this.mPinnedList.addPin(uri4, pkg, specs3)) {
                handleSlicePinned(uri4);
            }
            return null;
        } else if (method.equals("unpin_slice")) {
            Uri uri5 = (Uri) extras.getParcelable("slice_uri");
            String pkg2 = extras.getString("pkg");
            if (this.mPinnedList.removePin(uri5, pkg2)) {
                handleSliceUnpinned(uri5);
            }
            return null;
        } else if (method.equals("get_specs")) {
            Uri uri6 = (Uri) extras.getParcelable("slice_uri");
            Bundle b4 = new Bundle();
            addSpecs(b4, this.mPinnedList.getSpecs(uri6));
            return b4;
        } else if (method.equals("get_descendants")) {
            Uri uri7 = (Uri) extras.getParcelable("slice_uri");
            Bundle b5 = new Bundle();
            b5.putParcelableArrayList("slice_descendants", new ArrayList<>(handleGetDescendants(uri7)));
            return b5;
        } else if (method.equals("check_perms")) {
            Uri uri8 = (Uri) extras.getParcelable("slice_uri");
            extras.getString("pkg");
            int pid = extras.getInt("pid");
            int uid = extras.getInt("uid");
            Bundle b6 = new Bundle();
            b6.putInt("result", this.mPermissionManager.checkSlicePermission(uri8, pid, uid));
            return b6;
        } else {
            if (method.equals("grant_perms")) {
                Uri uri9 = (Uri) extras.getParcelable("slice_uri");
                String toPkg = extras.getString("pkg");
                if (Binder.getCallingUid() != Process.myUid()) {
                    throw new SecurityException("Only the owning process can manage slice permissions");
                }
                this.mPermissionManager.grantSlicePermission(uri9, toPkg);
            } else if (method.equals("revoke_perms")) {
                Uri uri10 = (Uri) extras.getParcelable("slice_uri");
                String toPkg2 = extras.getString("pkg");
                if (Binder.getCallingUid() != Process.myUid()) {
                    throw new SecurityException("Only the owning process can manage slice permissions");
                }
                this.mPermissionManager.revokeSlicePermission(uri10, toPkg2);
            }
            return null;
        }
    }

    private Collection<Uri> handleGetDescendants(Uri uri) {
        this.mCallback = "onGetSliceDescendants";
        return this.mProvider.onGetSliceDescendants(uri);
    }

    private void handleSlicePinned(Uri sliceUri) {
        this.mCallback = "onSlicePinned";
        this.mHandler.postDelayed(this.mAnr, 2000L);
        try {
            this.mProvider.onSlicePinned(sliceUri);
            this.mProvider.handleSlicePinned(sliceUri);
        } finally {
            this.mHandler.removeCallbacks(this.mAnr);
        }
    }

    private void handleSliceUnpinned(Uri sliceUri) {
        this.mCallback = "onSliceUnpinned";
        this.mHandler.postDelayed(this.mAnr, 2000L);
        try {
            this.mProvider.onSliceUnpinned(sliceUri);
            this.mProvider.handleSliceUnpinned(sliceUri);
        } finally {
            this.mHandler.removeCallbacks(this.mAnr);
        }
    }

    private Slice handleBindSlice(Uri sliceUri, Set<SliceSpec> specs, String callingPkg) {
        String pkg = callingPkg != null ? callingPkg : getContext().getPackageManager().getNameForUid(Binder.getCallingUid());
        if (this.mPermissionManager.checkSlicePermission(sliceUri, Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
            SliceProvider sliceProvider = this.mProvider;
            return SliceProvider.createPermissionSlice(getContext(), sliceUri, pkg);
        }
        return onBindSliceStrict(sliceUri, specs);
    }

    private Slice onBindSliceStrict(Uri sliceUri, Set<SliceSpec> specs) {
        StrictMode.ThreadPolicy oldPolicy = StrictMode.getThreadPolicy();
        this.mCallback = "onBindSlice";
        this.mHandler.postDelayed(this.mAnr, 2000L);
        try {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDeath().build());
            SliceProvider.setSpecs(specs);
            Slice onBindSlice = this.mProvider.onBindSlice(sliceUri);
            SliceProvider.setSpecs(null);
            this.mHandler.removeCallbacks(this.mAnr);
            return onBindSlice;
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    public static Slice bindSlice(Context context, Uri uri, Set<SliceSpec> supportedSpecs) {
        ProviderHolder holder = acquireClient(context.getContentResolver(), uri);
        if (holder.mProvider == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Bundle extras = new Bundle();
            extras.putParcelable("slice_uri", uri);
            addSpecs(extras, supportedSpecs);
            Bundle res = holder.mProvider.call("bind_slice", "supports_versioned_parcelable", extras);
            if (res == null) {
                return null;
            }
            res.setClassLoader(SliceProviderCompat.class.getClassLoader());
            Parcelable parcel = res.getParcelable("slice");
            if (parcel == null) {
                return null;
            }
            if (parcel instanceof Bundle) {
                return new Slice((Bundle) parcel);
            }
            return (Slice) ParcelUtils.fromParcelable(parcel);
        } catch (RemoteException e) {
            Log.e("SliceProviderCompat", "Unable to bind slice", e);
            return null;
        }
    }

    public static void addSpecs(Bundle extras, Set<SliceSpec> supportedSpecs) {
        ArrayList<String> types = new ArrayList<>();
        ArrayList<Integer> revs = new ArrayList<>();
        for (SliceSpec spec : supportedSpecs) {
            types.add(spec.getType());
            revs.add(Integer.valueOf(spec.getRevision()));
        }
        extras.putStringArrayList("specs", types);
        extras.putIntegerArrayList("revs", revs);
    }

    public static Set<SliceSpec> getSpecs(Bundle extras) {
        ArraySet<SliceSpec> specs = new ArraySet<>();
        ArrayList<String> types = extras.getStringArrayList("specs");
        ArrayList<Integer> revs = extras.getIntegerArrayList("revs");
        if (types != null && revs != null) {
            for (int i = 0; i < types.size(); i++) {
                specs.add(new SliceSpec(types.get(i), revs.get(i).intValue()));
            }
        }
        return specs;
    }

    public static void pinSlice(Context context, Uri uri, Set<SliceSpec> supportedSpecs) {
        ProviderHolder holder = acquireClient(context.getContentResolver(), uri);
        if (holder.mProvider == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Bundle extras = new Bundle();
            extras.putParcelable("slice_uri", uri);
            extras.putString("pkg", context.getPackageName());
            addSpecs(extras, supportedSpecs);
            holder.mProvider.call("pin_slice", "supports_versioned_parcelable", extras);
        } catch (RemoteException e) {
            Log.e("SliceProviderCompat", "Unable to pin slice", e);
        }
    }

    public static void unpinSlice(Context context, Uri uri, Set<SliceSpec> supportedSpecs) {
        ProviderHolder holder = acquireClient(context.getContentResolver(), uri);
        if (holder.mProvider == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Bundle extras = new Bundle();
            extras.putParcelable("slice_uri", uri);
            extras.putString("pkg", context.getPackageName());
            addSpecs(extras, supportedSpecs);
            holder.mProvider.call("unpin_slice", "supports_versioned_parcelable", extras);
        } catch (RemoteException e) {
            Log.e("SliceProviderCompat", "Unable to unpin slice", e);
        }
    }

    public static Collection<Uri> getSliceDescendants(Context context, Uri uri) {
        ProviderHolder holder;
        Bundle res;
        ContentResolver resolver = context.getContentResolver();
        try {
            holder = acquireClient(resolver, uri);
            Bundle extras = new Bundle();
            extras.putParcelable("slice_uri", uri);
            res = holder.mProvider.call("get_descendants", "supports_versioned_parcelable", extras);
        } catch (RemoteException e) {
            Log.e("SliceProviderCompat", "Unable to get slice descendants", e);
        }
        if (res != null) {
            ArrayList parcelableArrayList = res.getParcelableArrayList("slice_descendants");
            if (holder != null) {
                holder.close();
            }
            return parcelableArrayList;
        }
        if (holder != null) {
            holder.close();
        }
        return Collections.emptyList();
    }

    public static List<Uri> getPinnedSlices(Context context) {
        ArrayList<Uri> pinnedSlices = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences("slice_data_all_slice_files", 0);
        Set<String> prefSet = prefs.getStringSet("slice_data_all_slice_files", Collections.emptySet());
        for (String pref : prefSet) {
            pinnedSlices.addAll(new CompatPinnedList(context, pref).getPinnedSlices());
        }
        return pinnedSlices;
    }

    private static ProviderHolder acquireClient(ContentResolver resolver, Uri uri) {
        ContentProviderClient provider = resolver.acquireContentProviderClient(uri);
        if (provider == null) {
            throw new IllegalArgumentException("No provider found for " + uri);
        }
        return new ProviderHolder(provider);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class ProviderHolder implements AutoCloseable {
        private final ContentProviderClient mProvider;

        ProviderHolder(ContentProviderClient provider) {
            this.mProvider = provider;
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            if (this.mProvider == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= 24) {
                this.mProvider.close();
            } else {
                this.mProvider.release();
            }
        }
    }
}
