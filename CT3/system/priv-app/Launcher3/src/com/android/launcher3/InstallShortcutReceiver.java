package com.android.launcher3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.launcher3.compat.LauncherActivityInfoCompat;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.util.PackageManagerHelper;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;
/* loaded from: a.zip:com/android/launcher3/InstallShortcutReceiver.class */
public class InstallShortcutReceiver extends BroadcastReceiver {
    private static final Object sLock = new Object();
    private static boolean mUseInstallQueue = false;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/InstallShortcutReceiver$PendingInstallShortcutInfo.class */
    public static class PendingInstallShortcutInfo {
        final LauncherActivityInfoCompat activityInfo;
        final Intent data;
        final String label;
        final Intent launchIntent;
        final Context mContext;
        final UserHandleCompat user;

        public PendingInstallShortcutInfo(Intent intent, Context context) {
            this.data = intent;
            this.mContext = context;
            this.launchIntent = (Intent) intent.getParcelableExtra("android.intent.extra.shortcut.INTENT");
            this.label = intent.getStringExtra("android.intent.extra.shortcut.NAME");
            this.user = UserHandleCompat.myUserHandle();
            this.activityInfo = null;
        }

        public PendingInstallShortcutInfo(LauncherActivityInfoCompat launcherActivityInfoCompat, Context context) {
            this.data = null;
            this.mContext = context;
            this.activityInfo = launcherActivityInfoCompat;
            this.user = launcherActivityInfoCompat.getUser();
            this.launchIntent = AppInfo.makeLaunchIntent(context, launcherActivityInfoCompat, this.user);
            this.label = launcherActivityInfoCompat.getLabel().toString();
        }

        public String encodeToString() {
            if (this.activityInfo != null) {
                try {
                    return new JSONStringer().object().key("intent.launch").value(this.launchIntent.toUri(0)).key("isAppShortcut").value(true).key("userHandle").value(UserManagerCompat.getInstance(this.mContext).getSerialNumberForUser(this.user)).endObject().toString();
                } catch (JSONException e) {
                    Log.d("InstallShortcutReceiver", "Exception when adding shortcut: " + e);
                    return null;
                }
            }
            if (this.launchIntent.getAction() == null) {
                this.launchIntent.setAction("android.intent.action.VIEW");
            } else if (this.launchIntent.getAction().equals("android.intent.action.MAIN") && this.launchIntent.getCategories() != null && this.launchIntent.getCategories().contains("android.intent.category.LAUNCHER")) {
                this.launchIntent.addFlags(270532608);
            }
            String charSequence = InstallShortcutReceiver.ensureValidName(this.mContext, this.launchIntent, this.label).toString();
            Bitmap bitmap = (Bitmap) this.data.getParcelableExtra("android.intent.extra.shortcut.ICON");
            Intent.ShortcutIconResource shortcutIconResource = (Intent.ShortcutIconResource) this.data.getParcelableExtra("android.intent.extra.shortcut.ICON_RESOURCE");
            try {
                JSONStringer value = new JSONStringer().object().key("intent.launch").value(this.launchIntent.toUri(0)).key("name").value(charSequence);
                JSONStringer jSONStringer = value;
                if (bitmap != null) {
                    byte[] flattenBitmap = Utilities.flattenBitmap(bitmap);
                    jSONStringer = value.key("icon").value(Base64.encodeToString(flattenBitmap, 0, flattenBitmap.length, 0));
                }
                JSONStringer jSONStringer2 = jSONStringer;
                if (shortcutIconResource != null) {
                    jSONStringer2 = jSONStringer.key("iconResource").value(shortcutIconResource.resourceName).key("iconResourcePackage").value(shortcutIconResource.packageName);
                }
                return jSONStringer2.endObject().toString();
            } catch (JSONException e2) {
                Log.d("InstallShortcutReceiver", "Exception when adding shortcut: " + e2);
                return null;
            }
        }

        public ShortcutInfo getShortcutInfo() {
            return this.activityInfo != null ? ShortcutInfo.fromActivityInfo(this.activityInfo, this.mContext) : LauncherAppState.getInstance().getModel().infoFromShortcutIntent(this.mContext, this.data);
        }

        public String getTargetPackage() {
            String str = this.launchIntent.getPackage();
            String str2 = str;
            if (str == null) {
                str2 = this.launchIntent.getComponent() == null ? null : this.launchIntent.getComponent().getPackageName();
            }
            return str2;
        }

        public boolean isLauncherActivity() {
            return this.activityInfo != null;
        }
    }

    private static void addToInstallQueue(SharedPreferences sharedPreferences, PendingInstallShortcutInfo pendingInstallShortcutInfo) {
        synchronized (sLock) {
            String encodeToString = pendingInstallShortcutInfo.encodeToString();
            if (encodeToString != null) {
                Set<String> stringSet = sharedPreferences.getStringSet("apps_to_install", null);
                HashSet hashSet = stringSet == null ? new HashSet(1) : new HashSet(stringSet);
                hashSet.add(encodeToString);
                sharedPreferences.edit().putStringSet("apps_to_install", hashSet).apply();
            }
        }
    }

    private static PendingInstallShortcutInfo convertToLauncherActivityIfPossible(PendingInstallShortcutInfo pendingInstallShortcutInfo) {
        if (pendingInstallShortcutInfo.isLauncherActivity()) {
            return pendingInstallShortcutInfo;
        }
        if (Utilities.isLauncherAppTarget(pendingInstallShortcutInfo.launchIntent) && pendingInstallShortcutInfo.user.equals(UserHandleCompat.myUserHandle())) {
            ResolveInfo resolveActivity = pendingInstallShortcutInfo.mContext.getPackageManager().resolveActivity(pendingInstallShortcutInfo.launchIntent, 0);
            return resolveActivity == null ? pendingInstallShortcutInfo : new PendingInstallShortcutInfo(LauncherActivityInfoCompat.fromResolveInfo(resolveActivity, pendingInstallShortcutInfo.mContext), pendingInstallShortcutInfo.mContext);
        }
        return pendingInstallShortcutInfo;
    }

    private static PendingInstallShortcutInfo createPendingInfo(Context context, Intent intent) {
        if (isValidExtraType(intent, "android.intent.extra.shortcut.INTENT", Intent.class) && isValidExtraType(intent, "android.intent.extra.shortcut.ICON_RESOURCE", Intent.ShortcutIconResource.class) && isValidExtraType(intent, "android.intent.extra.shortcut.ICON", Bitmap.class)) {
            PendingInstallShortcutInfo pendingInstallShortcutInfo = new PendingInstallShortcutInfo(intent, context);
            if (pendingInstallShortcutInfo.launchIntent == null || pendingInstallShortcutInfo.label == null) {
                return null;
            }
            return convertToLauncherActivityIfPossible(pendingInstallShortcutInfo);
        }
        return null;
    }

    private static PendingInstallShortcutInfo decode(String str, Context context) {
        try {
            JSONObject jSONObject = (JSONObject) new JSONTokener(str).nextValue();
            Intent parseUri = Intent.parseUri(jSONObject.getString("intent.launch"), 0);
            if (jSONObject.optBoolean("isAppShortcut")) {
                UserHandleCompat userForSerialNumber = UserManagerCompat.getInstance(context).getUserForSerialNumber(jSONObject.getLong("userHandle"));
                if (userForSerialNumber == null) {
                    return null;
                }
                LauncherActivityInfoCompat resolveActivity = LauncherAppsCompat.getInstance(context).resolveActivity(parseUri, userForSerialNumber);
                return resolveActivity == null ? null : new PendingInstallShortcutInfo(resolveActivity, context);
            }
            Intent intent = new Intent();
            intent.putExtra("android.intent.extra.shortcut.INTENT", parseUri);
            intent.putExtra("android.intent.extra.shortcut.NAME", jSONObject.getString("name"));
            String optString = jSONObject.optString("icon");
            String optString2 = jSONObject.optString("iconResource");
            String optString3 = jSONObject.optString("iconResourcePackage");
            if (optString != null && !optString.isEmpty()) {
                byte[] decode = Base64.decode(optString, 0);
                intent.putExtra("android.intent.extra.shortcut.ICON", BitmapFactory.decodeByteArray(decode, 0, decode.length));
            } else if (optString2 != null && !optString2.isEmpty()) {
                Intent.ShortcutIconResource shortcutIconResource = new Intent.ShortcutIconResource();
                shortcutIconResource.resourceName = optString2;
                shortcutIconResource.packageName = optString3;
                intent.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", shortcutIconResource);
            }
            return new PendingInstallShortcutInfo(intent, context);
        } catch (URISyntaxException | JSONException e) {
            Log.d("InstallShortcutReceiver", "Exception reading shortcut to add: " + e);
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void disableAndFlushInstallQueue(Context context) {
        mUseInstallQueue = false;
        flushInstallQueue(context);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void enableInstallQueue() {
        mUseInstallQueue = true;
    }

    static CharSequence ensureValidName(Context context, Intent intent, CharSequence charSequence) {
        CharSequence charSequence2 = charSequence;
        if (charSequence == null) {
            try {
                PackageManager packageManager = context.getPackageManager();
                charSequence2 = packageManager.getActivityInfo(intent.getComponent(), 0).loadLabel(packageManager);
            } catch (PackageManager.NameNotFoundException e) {
                return "";
            }
        }
        return charSequence2;
    }

    static void flushInstallQueue(Context context) {
        ArrayList<PendingInstallShortcutInfo> andClearInstallQueue = getAndClearInstallQueue(Utilities.getPrefs(context), context);
        if (andClearInstallQueue.isEmpty()) {
            return;
        }
        Iterator<PendingInstallShortcutInfo> it = andClearInstallQueue.iterator();
        ArrayList<? extends ItemInfo> arrayList = new ArrayList<>();
        while (it.hasNext()) {
            PendingInstallShortcutInfo next = it.next();
            String targetPackage = next.getTargetPackage();
            if (TextUtils.isEmpty(targetPackage) || LauncherModel.isValidPackage(context, targetPackage, UserHandleCompat.myUserHandle())) {
                arrayList.add(next.getShortcutInfo());
            }
        }
        if (arrayList.isEmpty()) {
            return;
        }
        LauncherAppState.getInstance().getModel().addAndBindAddedWorkspaceItems(context, arrayList);
    }

    public static ShortcutInfo fromShortcutIntent(Context context, Intent intent) {
        PendingInstallShortcutInfo createPendingInfo = createPendingInfo(context, intent);
        return createPendingInfo == null ? null : createPendingInfo.getShortcutInfo();
    }

    private static ArrayList<PendingInstallShortcutInfo> getAndClearInstallQueue(SharedPreferences sharedPreferences, Context context) {
        synchronized (sLock) {
            Set<String> stringSet = sharedPreferences.getStringSet("apps_to_install", null);
            if (stringSet == null) {
                return new ArrayList<>();
            }
            ArrayList<PendingInstallShortcutInfo> arrayList = new ArrayList<>();
            for (String str : stringSet) {
                PendingInstallShortcutInfo decode = decode(str, context);
                if (decode != null) {
                    arrayList.add(decode);
                }
            }
            sharedPreferences.edit().putStringSet("apps_to_install", new HashSet()).apply();
            return arrayList;
        }
    }

    private static boolean isValidExtraType(Intent intent, String str, Class cls) {
        Parcelable parcelableExtra = intent.getParcelableExtra(str);
        return parcelableExtra != null ? cls.isInstance(parcelableExtra) : true;
    }

    private static void queuePendingShortcutInfo(PendingInstallShortcutInfo pendingInstallShortcutInfo, Context context) {
        boolean z = LauncherAppState.getInstance().getModel().getCallback() == null;
        addToInstallQueue(Utilities.getPrefs(context), pendingInstallShortcutInfo);
        if (mUseInstallQueue || z) {
            return;
        }
        flushInstallQueue(context);
    }

    public static void removeFromInstallQueue(Context context, HashSet<String> hashSet, UserHandleCompat userHandleCompat) {
        if (hashSet.isEmpty()) {
            return;
        }
        SharedPreferences prefs = Utilities.getPrefs(context);
        synchronized (sLock) {
            Set<String> stringSet = prefs.getStringSet("apps_to_install", null);
            if (stringSet != null) {
                HashSet hashSet2 = new HashSet(stringSet);
                Iterator<String> it = hashSet2.iterator();
                while (it.hasNext()) {
                    PendingInstallShortcutInfo decode = decode(it.next(), context);
                    if (decode == null || (hashSet.contains(decode.getTargetPackage()) && userHandleCompat.equals(decode.user))) {
                        it.remove();
                    }
                }
                prefs.edit().putStringSet("apps_to_install", hashSet2).apply();
            }
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        PendingInstallShortcutInfo createPendingInfo;
        if ("com.android.launcher.action.INSTALL_SHORTCUT".equals(intent.getAction()) && (createPendingInfo = createPendingInfo(context, intent)) != null) {
            if (createPendingInfo.isLauncherActivity() || PackageManagerHelper.hasPermissionForActivity(context, createPendingInfo.launchIntent, null)) {
                queuePendingShortcutInfo(createPendingInfo, context);
            } else {
                Log.e("InstallShortcutReceiver", "Ignoring malicious intent " + createPendingInfo.launchIntent.toUri(0));
            }
        }
    }
}
