package com.android.launcher3;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.graphics.BitmapInfo;
import com.android.launcher3.graphics.LauncherIcons;
import com.android.launcher3.shortcuts.DeepShortcutManager;
import com.android.launcher3.shortcuts.ShortcutInfoCompat;
import com.android.launcher3.shortcuts.ShortcutKey;
import com.android.launcher3.util.PackageManagerHelper;
import com.android.launcher3.util.Preconditions;
import com.android.launcher3.util.Provider;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
/* loaded from: classes.dex */
public class InstallShortcutReceiver extends BroadcastReceiver {
    private static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    private static final String APPS_PENDING_INSTALL = "apps_to_install";
    private static final String APP_SHORTCUT_TYPE_KEY = "isAppShortcut";
    private static final String APP_WIDGET_TYPE_KEY = "isAppWidget";
    private static final boolean DBG = false;
    private static final String DEEPSHORTCUT_TYPE_KEY = "isDeepShortcut";
    public static final int FLAG_ACTIVITY_PAUSED = 1;
    public static final int FLAG_BULK_ADD = 4;
    public static final int FLAG_DRAG_AND_DROP = 4;
    public static final int FLAG_LOADER_RUNNING = 2;
    private static final String ICON_KEY = "icon";
    private static final String ICON_RESOURCE_NAME_KEY = "iconResource";
    private static final String ICON_RESOURCE_PACKAGE_NAME_KEY = "iconResourcePackage";
    private static final String LAUNCH_INTENT_KEY = "intent.launch";
    private static final int MSG_ADD_TO_QUEUE = 1;
    private static final int MSG_FLUSH_QUEUE = 2;
    private static final String NAME_KEY = "name";
    public static final int NEW_SHORTCUT_BOUNCE_DURATION = 450;
    public static final int NEW_SHORTCUT_STAGGER_DELAY = 85;
    private static final String TAG = "InstallShortcutReceiver";
    private static final String USER_HANDLE_KEY = "userHandle";
    private static int sInstallQueueDisabledFlags = 0;
    private static final Handler sHandler = new Handler(LauncherModel.getWorkerLooper()) { // from class: com.android.launcher3.InstallShortcutReceiver.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    Pair pair = (Pair) message.obj;
                    String encodeToString = ((PendingInstallShortcutInfo) pair.second).encodeToString();
                    SharedPreferences prefs = Utilities.getPrefs((Context) pair.first);
                    Set<String> stringSet = prefs.getStringSet(InstallShortcutReceiver.APPS_PENDING_INSTALL, null);
                    HashSet hashSet = stringSet != null ? new HashSet(stringSet) : new HashSet(1);
                    hashSet.add(encodeToString);
                    prefs.edit().putStringSet(InstallShortcutReceiver.APPS_PENDING_INSTALL, hashSet).apply();
                    return;
                case 2:
                    Context context = (Context) message.obj;
                    LauncherModel model = LauncherAppState.getInstance(context).getModel();
                    if (model.getCallback() == null) {
                        return;
                    }
                    ArrayList arrayList = new ArrayList();
                    SharedPreferences prefs2 = Utilities.getPrefs(context);
                    Set<String> stringSet2 = prefs2.getStringSet(InstallShortcutReceiver.APPS_PENDING_INSTALL, null);
                    if (stringSet2 == null) {
                        return;
                    }
                    LauncherAppsCompat launcherAppsCompat = LauncherAppsCompat.getInstance(context);
                    for (String str : stringSet2) {
                        PendingInstallShortcutInfo decode = InstallShortcutReceiver.decode(str, context);
                        if (decode != null) {
                            String intentPackage = InstallShortcutReceiver.getIntentPackage(decode.launchIntent);
                            if (TextUtils.isEmpty(intentPackage) || launcherAppsCompat.isPackageEnabledForProfile(intentPackage, decode.user)) {
                                arrayList.add(decode.getItemInfo());
                            }
                        }
                    }
                    prefs2.edit().remove(InstallShortcutReceiver.APPS_PENDING_INSTALL).apply();
                    if (!arrayList.isEmpty()) {
                        model.addAndBindAddedWorkspaceItems(arrayList);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };

    public static void removeFromInstallQueue(Context context, HashSet<String> hashSet, UserHandle userHandle) {
        if (hashSet.isEmpty()) {
            return;
        }
        Preconditions.assertWorkerThread();
        SharedPreferences prefs = Utilities.getPrefs(context);
        Set<String> stringSet = prefs.getStringSet(APPS_PENDING_INSTALL, null);
        if (Utilities.isEmpty(stringSet)) {
            return;
        }
        HashSet hashSet2 = new HashSet(stringSet);
        Iterator<String> it = hashSet2.iterator();
        while (it.hasNext()) {
            try {
                Decoder decoder = new Decoder(it.next(), context);
                if (hashSet.contains(getIntentPackage(decoder.launcherIntent)) && userHandle.equals(decoder.user)) {
                    it.remove();
                }
            } catch (URISyntaxException | JSONException e) {
                Log.d(TAG, "Exception reading shortcut to add: " + e);
                it.remove();
            }
        }
        prefs.edit().putStringSet(APPS_PENDING_INSTALL, hashSet2).apply();
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        PendingInstallShortcutInfo createPendingInfo;
        if (ACTION_INSTALL_SHORTCUT.equals(intent.getAction()) && (createPendingInfo = createPendingInfo(context, intent)) != null) {
            if (!createPendingInfo.isLauncherActivity() && !new PackageManagerHelper(context).hasPermissionForActivity(createPendingInfo.launchIntent, null)) {
                Log.e(TAG, "Ignoring malicious intent " + createPendingInfo.launchIntent.toUri(0));
                return;
            }
            queuePendingShortcutInfo(createPendingInfo, context);
        }
    }

    private static boolean isValidExtraType(Intent intent, String str, Class cls) {
        Parcelable parcelableExtra = intent.getParcelableExtra(str);
        return parcelableExtra == null || cls.isInstance(parcelableExtra);
    }

    private static PendingInstallShortcutInfo createPendingInfo(Context context, Intent intent) {
        if (isValidExtraType(intent, "android.intent.extra.shortcut.INTENT", Intent.class) && isValidExtraType(intent, "android.intent.extra.shortcut.ICON_RESOURCE", Intent.ShortcutIconResource.class) && isValidExtraType(intent, "android.intent.extra.shortcut.ICON", Bitmap.class)) {
            PendingInstallShortcutInfo pendingInstallShortcutInfo = new PendingInstallShortcutInfo(intent, Process.myUserHandle(), context);
            if (pendingInstallShortcutInfo.launchIntent == null || pendingInstallShortcutInfo.label == null) {
                return null;
            }
            return convertToLauncherActivityIfPossible(pendingInstallShortcutInfo);
        }
        return null;
    }

    public static ShortcutInfo fromShortcutIntent(Context context, Intent intent) {
        PendingInstallShortcutInfo createPendingInfo = createPendingInfo(context, intent);
        if (createPendingInfo == null) {
            return null;
        }
        return (ShortcutInfo) createPendingInfo.getItemInfo().first;
    }

    public static ShortcutInfo fromActivityInfo(LauncherActivityInfo launcherActivityInfo, Context context) {
        return (ShortcutInfo) new PendingInstallShortcutInfo(launcherActivityInfo, context).getItemInfo().first;
    }

    public static void queueShortcut(ShortcutInfoCompat shortcutInfoCompat, Context context) {
        queuePendingShortcutInfo(new PendingInstallShortcutInfo(shortcutInfoCompat, context), context);
    }

    public static void queueWidget(AppWidgetProviderInfo appWidgetProviderInfo, int i, Context context) {
        queuePendingShortcutInfo(new PendingInstallShortcutInfo(appWidgetProviderInfo, i, context), context);
    }

    public static void queueActivityInfo(LauncherActivityInfo launcherActivityInfo, Context context) {
        queuePendingShortcutInfo(new PendingInstallShortcutInfo(launcherActivityInfo, context), context);
    }

    public static HashSet<ShortcutKey> getPendingShortcuts(Context context) {
        HashSet<ShortcutKey> hashSet = new HashSet<>();
        Set<String> stringSet = Utilities.getPrefs(context).getStringSet(APPS_PENDING_INSTALL, null);
        if (Utilities.isEmpty(stringSet)) {
            return hashSet;
        }
        for (String str : stringSet) {
            try {
                Decoder decoder = new Decoder(str, context);
                if (decoder.optBoolean(DEEPSHORTCUT_TYPE_KEY)) {
                    hashSet.add(ShortcutKey.fromIntent(decoder.launcherIntent, decoder.user));
                }
            } catch (URISyntaxException | JSONException e) {
                Log.d(TAG, "Exception reading shortcut to add: " + e);
            }
        }
        return hashSet;
    }

    private static void queuePendingShortcutInfo(PendingInstallShortcutInfo pendingInstallShortcutInfo, Context context) {
        Message.obtain(sHandler, 1, Pair.create(context, pendingInstallShortcutInfo)).sendToTarget();
        flushInstallQueue(context);
    }

    public static void enableInstallQueue(int i) {
        sInstallQueueDisabledFlags = i | sInstallQueueDisabledFlags;
    }

    public static void disableAndFlushInstallQueue(int i, Context context) {
        sInstallQueueDisabledFlags = (~i) & sInstallQueueDisabledFlags;
        flushInstallQueue(context);
    }

    static void flushInstallQueue(Context context) {
        if (sInstallQueueDisabledFlags != 0) {
            return;
        }
        Message.obtain(sHandler, 2, context.getApplicationContext()).sendToTarget();
    }

    static CharSequence ensureValidName(Context context, Intent intent, CharSequence charSequence) {
        if (charSequence == null) {
            try {
                PackageManager packageManager = context.getPackageManager();
                return packageManager.getActivityInfo(intent.getComponent(), 0).loadLabel(packageManager);
            } catch (PackageManager.NameNotFoundException e) {
                return "";
            }
        }
        return charSequence;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class PendingInstallShortcutInfo {
        final LauncherActivityInfo activityInfo;
        final Intent data;
        final String label;
        final Intent launchIntent;
        final Context mContext;
        final AppWidgetProviderInfo providerInfo;
        final ShortcutInfoCompat shortcutInfo;
        final UserHandle user;

        public PendingInstallShortcutInfo(Intent intent, UserHandle userHandle, Context context) {
            this.activityInfo = null;
            this.shortcutInfo = null;
            this.providerInfo = null;
            this.data = intent;
            this.user = userHandle;
            this.mContext = context;
            this.launchIntent = (Intent) intent.getParcelableExtra("android.intent.extra.shortcut.INTENT");
            this.label = intent.getStringExtra("android.intent.extra.shortcut.NAME");
        }

        public PendingInstallShortcutInfo(LauncherActivityInfo launcherActivityInfo, Context context) {
            this.activityInfo = launcherActivityInfo;
            this.shortcutInfo = null;
            this.providerInfo = null;
            this.data = null;
            this.user = launcherActivityInfo.getUser();
            this.mContext = context;
            this.launchIntent = AppInfo.makeLaunchIntent(launcherActivityInfo);
            this.label = launcherActivityInfo.getLabel().toString();
        }

        public PendingInstallShortcutInfo(ShortcutInfoCompat shortcutInfoCompat, Context context) {
            this.activityInfo = null;
            this.shortcutInfo = shortcutInfoCompat;
            this.providerInfo = null;
            this.data = null;
            this.mContext = context;
            this.user = shortcutInfoCompat.getUserHandle();
            this.launchIntent = shortcutInfoCompat.makeIntent();
            this.label = shortcutInfoCompat.getShortLabel().toString();
        }

        public PendingInstallShortcutInfo(AppWidgetProviderInfo appWidgetProviderInfo, int i, Context context) {
            this.activityInfo = null;
            this.shortcutInfo = null;
            this.providerInfo = appWidgetProviderInfo;
            this.data = null;
            this.mContext = context;
            this.user = appWidgetProviderInfo.getProfile();
            this.launchIntent = new Intent().setComponent(appWidgetProviderInfo.provider).putExtra(LauncherSettings.Favorites.APPWIDGET_ID, i);
            this.label = appWidgetProviderInfo.label;
        }

        public String encodeToString() {
            try {
                if (this.activityInfo != null) {
                    return new JSONStringer().object().key(InstallShortcutReceiver.LAUNCH_INTENT_KEY).value(this.launchIntent.toUri(0)).key(InstallShortcutReceiver.APP_SHORTCUT_TYPE_KEY).value(true).key(InstallShortcutReceiver.USER_HANDLE_KEY).value(UserManagerCompat.getInstance(this.mContext).getSerialNumberForUser(this.user)).endObject().toString();
                }
                if (this.shortcutInfo != null) {
                    return new JSONStringer().object().key(InstallShortcutReceiver.LAUNCH_INTENT_KEY).value(this.launchIntent.toUri(0)).key(InstallShortcutReceiver.DEEPSHORTCUT_TYPE_KEY).value(true).key(InstallShortcutReceiver.USER_HANDLE_KEY).value(UserManagerCompat.getInstance(this.mContext).getSerialNumberForUser(this.user)).endObject().toString();
                }
                if (this.providerInfo != null) {
                    return new JSONStringer().object().key(InstallShortcutReceiver.LAUNCH_INTENT_KEY).value(this.launchIntent.toUri(0)).key(InstallShortcutReceiver.APP_WIDGET_TYPE_KEY).value(true).key(InstallShortcutReceiver.USER_HANDLE_KEY).value(UserManagerCompat.getInstance(this.mContext).getSerialNumberForUser(this.user)).endObject().toString();
                }
                if (this.launchIntent.getAction() == null) {
                    this.launchIntent.setAction("android.intent.action.VIEW");
                } else if (this.launchIntent.getAction().equals("android.intent.action.MAIN") && this.launchIntent.getCategories() != null && this.launchIntent.getCategories().contains("android.intent.category.LAUNCHER")) {
                    this.launchIntent.addFlags(270532608);
                }
                String charSequence = InstallShortcutReceiver.ensureValidName(this.mContext, this.launchIntent, this.label).toString();
                Bitmap bitmap = (Bitmap) this.data.getParcelableExtra("android.intent.extra.shortcut.ICON");
                Intent.ShortcutIconResource shortcutIconResource = (Intent.ShortcutIconResource) this.data.getParcelableExtra("android.intent.extra.shortcut.ICON_RESOURCE");
                JSONStringer value = new JSONStringer().object().key(InstallShortcutReceiver.LAUNCH_INTENT_KEY).value(this.launchIntent.toUri(0)).key(InstallShortcutReceiver.NAME_KEY).value(charSequence);
                if (bitmap != null) {
                    byte[] flattenBitmap = Utilities.flattenBitmap(bitmap);
                    value = value.key("icon").value(Base64.encodeToString(flattenBitmap, 0, flattenBitmap.length, 0));
                }
                if (shortcutIconResource != null) {
                    value = value.key("iconResource").value(shortcutIconResource.resourceName).key(InstallShortcutReceiver.ICON_RESOURCE_PACKAGE_NAME_KEY).value(shortcutIconResource.packageName);
                }
                return value.endObject().toString();
            } catch (JSONException e) {
                Log.d(InstallShortcutReceiver.TAG, "Exception when adding shortcut: " + e);
                return null;
            }
        }

        public Pair<ItemInfo, Object> getItemInfo() {
            if (this.activityInfo != null) {
                AppInfo appInfo = new AppInfo(this.mContext, this.activityInfo, this.user);
                final LauncherAppState launcherAppState = LauncherAppState.getInstance(this.mContext);
                appInfo.title = "";
                launcherAppState.getIconCache().getDefaultIcon(this.user).applyTo(appInfo);
                final ShortcutInfo makeShortcut = appInfo.makeShortcut();
                if (Looper.myLooper() == LauncherModel.getWorkerLooper()) {
                    launcherAppState.getIconCache().getTitleAndIcon(makeShortcut, this.activityInfo, false);
                } else {
                    launcherAppState.getModel().updateAndBindShortcutInfo(new Provider<ShortcutInfo>() { // from class: com.android.launcher3.InstallShortcutReceiver.PendingInstallShortcutInfo.1
                        /* JADX WARN: Can't rename method to resolve collision */
                        @Override // com.android.launcher3.util.Provider
                        public ShortcutInfo get() {
                            launcherAppState.getIconCache().getTitleAndIcon(makeShortcut, PendingInstallShortcutInfo.this.activityInfo, false);
                            return makeShortcut;
                        }
                    });
                }
                return Pair.create(makeShortcut, this.activityInfo);
            } else if (this.shortcutInfo != null) {
                ShortcutInfo shortcutInfo = new ShortcutInfo(this.shortcutInfo, this.mContext);
                LauncherIcons obtain = LauncherIcons.obtain(this.mContext);
                obtain.createShortcutIcon(this.shortcutInfo).applyTo(shortcutInfo);
                obtain.recycle();
                return Pair.create(shortcutInfo, this.shortcutInfo);
            } else if (this.providerInfo != null) {
                LauncherAppWidgetProviderInfo fromProviderInfo = LauncherAppWidgetProviderInfo.fromProviderInfo(this.mContext, this.providerInfo);
                LauncherAppWidgetInfo launcherAppWidgetInfo = new LauncherAppWidgetInfo(this.launchIntent.getIntExtra(LauncherSettings.Favorites.APPWIDGET_ID, 0), fromProviderInfo.provider);
                InvariantDeviceProfile idp = LauncherAppState.getIDP(this.mContext);
                launcherAppWidgetInfo.minSpanX = fromProviderInfo.minSpanX;
                launcherAppWidgetInfo.minSpanY = fromProviderInfo.minSpanY;
                launcherAppWidgetInfo.spanX = Math.min(fromProviderInfo.spanX, idp.numColumns);
                launcherAppWidgetInfo.spanY = Math.min(fromProviderInfo.spanY, idp.numRows);
                return Pair.create(launcherAppWidgetInfo, this.providerInfo);
            } else {
                return Pair.create(InstallShortcutReceiver.createShortcutInfo(this.data, LauncherAppState.getInstance(this.mContext)), null);
            }
        }

        public boolean isLauncherActivity() {
            return this.activityInfo != null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String getIntentPackage(Intent intent) {
        return intent.getComponent() == null ? intent.getPackage() : intent.getComponent().getPackageName();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static PendingInstallShortcutInfo decode(String str, Context context) {
        try {
            Decoder decoder = new Decoder(str, context);
            if (decoder.optBoolean(APP_SHORTCUT_TYPE_KEY)) {
                LauncherActivityInfo resolveActivity = LauncherAppsCompat.getInstance(context).resolveActivity(decoder.launcherIntent, decoder.user);
                if (resolveActivity == null) {
                    return null;
                }
                return new PendingInstallShortcutInfo(resolveActivity, context);
            } else if (decoder.optBoolean(DEEPSHORTCUT_TYPE_KEY)) {
                List<ShortcutInfoCompat> queryForFullDetails = DeepShortcutManager.getInstance(context).queryForFullDetails(decoder.launcherIntent.getPackage(), Arrays.asList(decoder.launcherIntent.getStringExtra(ShortcutInfoCompat.EXTRA_SHORTCUT_ID)), decoder.user);
                if (queryForFullDetails.isEmpty()) {
                    return null;
                }
                return new PendingInstallShortcutInfo(queryForFullDetails.get(0), context);
            } else if (decoder.optBoolean(APP_WIDGET_TYPE_KEY)) {
                int intExtra = decoder.launcherIntent.getIntExtra(LauncherSettings.Favorites.APPWIDGET_ID, 0);
                AppWidgetProviderInfo appWidgetInfo = AppWidgetManager.getInstance(context).getAppWidgetInfo(intExtra);
                if (appWidgetInfo != null && appWidgetInfo.provider.equals(decoder.launcherIntent.getComponent()) && appWidgetInfo.getProfile().equals(decoder.user)) {
                    return new PendingInstallShortcutInfo(appWidgetInfo, intExtra, context);
                }
                return null;
            } else {
                Intent intent = new Intent();
                intent.putExtra("android.intent.extra.shortcut.INTENT", decoder.launcherIntent);
                intent.putExtra("android.intent.extra.shortcut.NAME", decoder.getString(NAME_KEY));
                String optString = decoder.optString("icon");
                String optString2 = decoder.optString("iconResource");
                String optString3 = decoder.optString(ICON_RESOURCE_PACKAGE_NAME_KEY);
                if (optString != null && !optString.isEmpty()) {
                    byte[] decode = Base64.decode(optString, 0);
                    intent.putExtra("android.intent.extra.shortcut.ICON", BitmapFactory.decodeByteArray(decode, 0, decode.length));
                } else if (optString2 != null && !optString2.isEmpty()) {
                    Intent.ShortcutIconResource shortcutIconResource = new Intent.ShortcutIconResource();
                    shortcutIconResource.resourceName = optString2;
                    shortcutIconResource.packageName = optString3;
                    intent.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", shortcutIconResource);
                }
                return new PendingInstallShortcutInfo(intent, decoder.user, context);
            }
        } catch (URISyntaxException | JSONException e) {
            Log.d(TAG, "Exception reading shortcut to add: " + e);
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Decoder extends JSONObject {
        public final Intent launcherIntent;
        public final UserHandle user;

        private Decoder(String str, Context context) throws JSONException, URISyntaxException {
            super(str);
            UserHandle myUserHandle;
            this.launcherIntent = Intent.parseUri(getString(InstallShortcutReceiver.LAUNCH_INTENT_KEY), 0);
            if (has(InstallShortcutReceiver.USER_HANDLE_KEY)) {
                myUserHandle = UserManagerCompat.getInstance(context).getUserForSerialNumber(getLong(InstallShortcutReceiver.USER_HANDLE_KEY));
            } else {
                myUserHandle = Process.myUserHandle();
            }
            this.user = myUserHandle;
            if (this.user == null) {
                throw new JSONException("Invalid user");
            }
        }
    }

    private static PendingInstallShortcutInfo convertToLauncherActivityIfPossible(PendingInstallShortcutInfo pendingInstallShortcutInfo) {
        if (pendingInstallShortcutInfo.isLauncherActivity()) {
            return pendingInstallShortcutInfo;
        }
        if (!Utilities.isLauncherAppTarget(pendingInstallShortcutInfo.launchIntent)) {
            return pendingInstallShortcutInfo;
        }
        LauncherActivityInfo resolveActivity = LauncherAppsCompat.getInstance(pendingInstallShortcutInfo.mContext).resolveActivity(pendingInstallShortcutInfo.launchIntent, pendingInstallShortcutInfo.user);
        if (resolveActivity == null) {
            return pendingInstallShortcutInfo;
        }
        return new PendingInstallShortcutInfo(resolveActivity, pendingInstallShortcutInfo.mContext);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ShortcutInfo createShortcutInfo(Intent intent, LauncherAppState launcherAppState) {
        Intent intent2 = (Intent) intent.getParcelableExtra("android.intent.extra.shortcut.INTENT");
        String stringExtra = intent.getStringExtra("android.intent.extra.shortcut.NAME");
        Parcelable parcelableExtra = intent.getParcelableExtra("android.intent.extra.shortcut.ICON");
        BitmapInfo bitmapInfo = null;
        if (intent2 == null) {
            Log.e(TAG, "Can't construct ShorcutInfo with null intent");
            return null;
        }
        ShortcutInfo shortcutInfo = new ShortcutInfo();
        shortcutInfo.user = Process.myUserHandle();
        LauncherIcons obtain = LauncherIcons.obtain(launcherAppState.getContext());
        if (parcelableExtra instanceof Bitmap) {
            bitmapInfo = obtain.createIconBitmap((Bitmap) parcelableExtra);
        } else {
            Parcelable parcelableExtra2 = intent.getParcelableExtra("android.intent.extra.shortcut.ICON_RESOURCE");
            if (parcelableExtra2 instanceof Intent.ShortcutIconResource) {
                shortcutInfo.iconResource = (Intent.ShortcutIconResource) parcelableExtra2;
                bitmapInfo = obtain.createIconBitmap(shortcutInfo.iconResource);
            }
        }
        obtain.recycle();
        if (bitmapInfo == null) {
            bitmapInfo = launcherAppState.getIconCache().getDefaultIcon(shortcutInfo.user);
        }
        bitmapInfo.applyTo(shortcutInfo);
        shortcutInfo.title = Utilities.trim(stringExtra);
        shortcutInfo.contentDescription = UserManagerCompat.getInstance(launcherAppState.getContext()).getBadgedLabelForUser(shortcutInfo.title, shortcutInfo.user);
        shortcutInfo.intent = intent2;
        return shortcutInfo;
    }
}
