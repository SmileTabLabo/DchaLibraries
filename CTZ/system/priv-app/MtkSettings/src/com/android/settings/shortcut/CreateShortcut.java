package com.android.settings.shortcut;

import android.app.LauncherActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import com.android.settings.R;
import com.android.settings.Settings;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.Utils;
import com.mediatek.settings.FeatureOption;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class CreateShortcut extends LauncherActivity {
    static final String SHORTCUT_ID_PREFIX = "component-shortcut-";

    @Override // android.app.LauncherActivity
    protected Intent getTargetIntent() {
        return getBaseIntent().addFlags(268435456);
    }

    @Override // android.app.LauncherActivity, android.app.ListActivity
    protected void onListItemClick(ListView listView, View view, int i, long j) {
        LauncherActivity.ListItem itemForPosition = itemForPosition(i);
        logCreateShortcut(itemForPosition.resolveInfo);
        setResult(-1, createResultIntent(intentForPosition(i), itemForPosition.resolveInfo, itemForPosition.label));
        finish();
    }

    Intent createResultIntent(Intent intent, ResolveInfo resolveInfo, CharSequence charSequence) {
        Icon createWithResource;
        intent.setFlags(335544320);
        ShortcutManager shortcutManager = (ShortcutManager) getSystemService(ShortcutManager.class);
        ActivityInfo activityInfo = resolveInfo.activityInfo;
        if (activityInfo.icon != 0) {
            createWithResource = Icon.createWithAdaptiveBitmap(createIcon(activityInfo.icon, R.layout.shortcut_badge_maskable, getResources().getDimensionPixelSize(R.dimen.shortcut_size_maskable)));
        } else {
            createWithResource = Icon.createWithResource(this, (int) R.drawable.ic_launcher_settings);
        }
        Intent createShortcutResultIntent = shortcutManager.createShortcutResultIntent(new ShortcutInfo.Builder(this, SHORTCUT_ID_PREFIX + intent.getComponent().flattenToShortString()).setShortLabel(charSequence).setIntent(intent).setIcon(createWithResource).build());
        if (createShortcutResultIntent == null) {
            createShortcutResultIntent = new Intent();
        }
        createShortcutResultIntent.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher_settings));
        createShortcutResultIntent.putExtra("android.intent.extra.shortcut.INTENT", intent);
        createShortcutResultIntent.putExtra("android.intent.extra.shortcut.NAME", charSequence);
        if (activityInfo.icon != 0) {
            createShortcutResultIntent.putExtra("android.intent.extra.shortcut.ICON", createIcon(activityInfo.icon, R.layout.shortcut_badge, getResources().getDimensionPixelSize(R.dimen.shortcut_size)));
        }
        return createShortcutResultIntent;
    }

    private void logCreateShortcut(ResolveInfo resolveInfo) {
        if (resolveInfo == null || resolveInfo.activityInfo == null) {
            return;
        }
        FeatureFactory.getFactory(this).getMetricsFeatureProvider().action(this, 829, resolveInfo.activityInfo.name, new Pair[0]);
    }

    private Bitmap createIcon(int i, int i2, int i3) {
        View inflate = LayoutInflater.from(new ContextThemeWrapper(this, 16974372)).inflate(i2, (ViewGroup) null);
        Drawable drawable = getDrawable(i);
        if (drawable instanceof LayerDrawable) {
            drawable = ((LayerDrawable) drawable).getDrawable(1);
        }
        ((ImageView) inflate.findViewById(16908294)).setImageDrawable(drawable);
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(i3, 1073741824);
        inflate.measure(makeMeasureSpec, makeMeasureSpec);
        Bitmap createBitmap = Bitmap.createBitmap(inflate.getMeasuredWidth(), inflate.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        inflate.layout(0, 0, inflate.getMeasuredWidth(), inflate.getMeasuredHeight());
        inflate.draw(canvas);
        return createBitmap;
    }

    protected boolean onEvaluateShowIcons() {
        return false;
    }

    @Override // android.app.LauncherActivity
    protected void onSetContentView() {
        setContentView(R.layout.activity_list);
    }

    @Override // android.app.LauncherActivity
    protected List<ResolveInfo> onQueryPackageManager(Intent intent) {
        List<ResolveInfo> queryIntentActivities = getPackageManager().queryIntentActivities(intent, 128);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService("connectivity");
        if (queryIntentActivities == null) {
            return null;
        }
        for (int size = queryIntentActivities.size() - 1; size >= 0; size--) {
            ResolveInfo resolveInfo = queryIntentActivities.get(size);
            if (resolveInfo.activityInfo.name.endsWith(Settings.TetherSettingsActivity.class.getSimpleName())) {
                if (!connectivityManager.isTetheringSupported() || Utils.isWifiOnly(this)) {
                    queryIntentActivities.remove(size);
                }
            } else if (resolveInfo.activityInfo.name.endsWith(Settings.DreamSettingsActivity.class.getSimpleName()) && FeatureOption.MTK_GMO_RAM_OPTIMIZE) {
                queryIntentActivities.remove(size);
            }
        }
        return queryIntentActivities;
    }

    static Intent getBaseIntent() {
        return new Intent("android.intent.action.MAIN").addCategory("com.android.settings.SHORTCUT");
    }

    /* loaded from: classes.dex */
    public static class ShortcutsUpdateTask extends AsyncTask<Void, Void, Void> {
        private final Context mContext;

        public ShortcutsUpdateTask(Context context) {
            this.mContext = context;
        }

        @Override // android.os.AsyncTask
        public Void doInBackground(Void... voidArr) {
            ShortcutManager shortcutManager = (ShortcutManager) this.mContext.getSystemService(ShortcutManager.class);
            PackageManager packageManager = this.mContext.getPackageManager();
            ArrayList arrayList = new ArrayList();
            for (ShortcutInfo shortcutInfo : shortcutManager.getPinnedShortcuts()) {
                if (shortcutInfo.getId().startsWith(CreateShortcut.SHORTCUT_ID_PREFIX)) {
                    ResolveInfo resolveActivity = packageManager.resolveActivity(CreateShortcut.getBaseIntent().setComponent(ComponentName.unflattenFromString(shortcutInfo.getId().substring(CreateShortcut.SHORTCUT_ID_PREFIX.length()))), 0);
                    if (resolveActivity != null) {
                        arrayList.add(new ShortcutInfo.Builder(this.mContext, shortcutInfo.getId()).setShortLabel(resolveActivity.loadLabel(packageManager)).build());
                    }
                }
            }
            if (!arrayList.isEmpty()) {
                shortcutManager.updateShortcuts(arrayList);
                return null;
            }
            return null;
        }
    }
}
