package com.android.launcher3;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.BenesseExtension;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import com.android.launcher3.LauncherProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: a.zip:com/android/launcher3/AutoInstallsLayout.class */
public class AutoInstallsLayout {
    private static final String HOTSEAT_CONTAINER_NAME = LauncherSettings$Favorites.containerToString(-101);
    final AppWidgetHost mAppWidgetHost;
    protected final LayoutParserCallback mCallback;
    private final int mColumnCount;
    final Context mContext;
    protected SQLiteDatabase mDb;
    private final int mHotseatAllAppsRank;
    protected final int mLayoutId;
    protected final PackageManager mPackageManager;
    protected final String mRootTag;
    private final int mRowCount;
    protected final Resources mSourceRes;
    private final long[] mTemp = new long[2];
    final ContentValues mValues = new ContentValues();

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: a.zip:com/android/launcher3/AutoInstallsLayout$AppShortcutParser.class */
    public class AppShortcutParser implements TagParser {
        final AutoInstallsLayout this$0;

        /* JADX INFO: Access modifiers changed from: protected */
        public AppShortcutParser(AutoInstallsLayout autoInstallsLayout) {
            this.this$0 = autoInstallsLayout;
        }

        protected long invalidPackageOrClass(XmlResourceParser xmlResourceParser) {
            Log.w("AutoInstalls", "Skipping invalid <favorite> with no component");
            return -1L;
        }

        /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:13:0x0078 -> B:9:0x003c). Please submit an issue!!! */
        @Override // com.android.launcher3.AutoInstallsLayout.TagParser
        public long parseAndAdd(XmlResourceParser xmlResourceParser) {
            ComponentName componentName;
            ActivityInfo activityInfo;
            String attributeValue = AutoInstallsLayout.getAttributeValue(xmlResourceParser, "packageName");
            String attributeValue2 = AutoInstallsLayout.getAttributeValue(xmlResourceParser, "className");
            if (!TextUtils.isEmpty(attributeValue)) {
                try {
                    if (!TextUtils.isEmpty(attributeValue2)) {
                        try {
                            componentName = new ComponentName(attributeValue, attributeValue2);
                            activityInfo = this.this$0.mPackageManager.getActivityInfo(componentName, 0);
                        } catch (PackageManager.NameNotFoundException e) {
                            componentName = new ComponentName(this.this$0.mPackageManager.currentToCanonicalPackageNames(new String[]{attributeValue})[0], attributeValue2);
                            activityInfo = this.this$0.mPackageManager.getActivityInfo(componentName, 0);
                        }
                        return this.this$0.addShortcut(activityInfo.loadLabel(this.this$0.mPackageManager).toString(), new Intent("android.intent.action.MAIN", (Uri) null).addCategory("android.intent.category.LAUNCHER").setComponent(componentName).setFlags(270532608), 0);
                    }
                } catch (PackageManager.NameNotFoundException e2) {
                    Log.e("AutoInstalls", "Unable to add favorite: " + attributeValue + "/" + attributeValue2, e2);
                    return -1L;
                }
            }
            return invalidPackageOrClass(xmlResourceParser);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: a.zip:com/android/launcher3/AutoInstallsLayout$AppWidgetParser.class */
    public class AppWidgetParser implements TagParser {
        final AutoInstallsLayout this$0;

        /* JADX INFO: Access modifiers changed from: protected */
        public AppWidgetParser(AutoInstallsLayout autoInstallsLayout) {
            this.this$0 = autoInstallsLayout;
        }

        /* JADX WARN: Code restructure failed: missing block: B:32:0x00fb, code lost:
            throw new java.lang.RuntimeException("Widget extras must have a key and value");
         */
        @Override // com.android.launcher3.AutoInstallsLayout.TagParser
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public long parseAndAdd(XmlResourceParser xmlResourceParser) throws XmlPullParserException, IOException {
            long j;
            int allocateAppWidgetId;
            String attributeValue = AutoInstallsLayout.getAttributeValue(xmlResourceParser, "packageName");
            String attributeValue2 = AutoInstallsLayout.getAttributeValue(xmlResourceParser, "className");
            if (TextUtils.isEmpty(attributeValue) || TextUtils.isEmpty(attributeValue2)) {
                return -1L;
            }
            ComponentName componentName = new ComponentName(attributeValue, attributeValue2);
            try {
                this.this$0.mPackageManager.getReceiverInfo(componentName, 0);
            } catch (Exception e) {
                componentName = new ComponentName(this.this$0.mPackageManager.currentToCanonicalPackageNames(new String[]{attributeValue})[0], attributeValue2);
                try {
                    this.this$0.mPackageManager.getReceiverInfo(componentName, 0);
                } catch (Exception e2) {
                    return -1L;
                }
            }
            this.this$0.mValues.put("spanX", AutoInstallsLayout.getAttributeValue(xmlResourceParser, "spanX"));
            this.this$0.mValues.put("spanY", AutoInstallsLayout.getAttributeValue(xmlResourceParser, "spanY"));
            Bundle bundle = new Bundle();
            int depth = xmlResourceParser.getDepth();
            while (true) {
                int next = xmlResourceParser.next();
                if (next == 3 && xmlResourceParser.getDepth() <= depth) {
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.this$0.mContext);
                    try {
                        allocateAppWidgetId = this.this$0.mAppWidgetHost.allocateAppWidgetId();
                    } catch (RuntimeException e3) {
                        j = -1;
                    }
                    if (appWidgetManager.bindAppWidgetIdIfAllowed(allocateAppWidgetId, componentName)) {
                        this.this$0.mValues.put("itemType", (Integer) 4);
                        this.this$0.mValues.put("appWidgetId", Integer.valueOf(allocateAppWidgetId));
                        this.this$0.mValues.put("appWidgetProvider", componentName.flattenToString());
                        this.this$0.mValues.put("_id", Long.valueOf(this.this$0.mCallback.generateNewItemId()));
                        long insertAndCheck = this.this$0.mCallback.insertAndCheck(this.this$0.mDb, this.this$0.mValues);
                        if (insertAndCheck < 0) {
                            this.this$0.mAppWidgetHost.deleteAppWidgetId(allocateAppWidgetId);
                            return insertAndCheck;
                        }
                        j = insertAndCheck;
                        if (!bundle.isEmpty()) {
                            Intent intent = new Intent("com.android.launcher.action.APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE");
                            intent.setComponent(componentName);
                            intent.putExtras(bundle);
                            intent.putExtra("appWidgetId", allocateAppWidgetId);
                            this.this$0.mContext.sendBroadcast(intent);
                            j = insertAndCheck;
                        }
                        return j;
                    }
                    return -1L;
                } else if (next == 2) {
                    if (!"extra".equals(xmlResourceParser.getName())) {
                        throw new RuntimeException("Widgets can contain only extras");
                    }
                    String attributeValue3 = AutoInstallsLayout.getAttributeValue(xmlResourceParser, "key");
                    String attributeValue4 = AutoInstallsLayout.getAttributeValue(xmlResourceParser, "value");
                    if (attributeValue3 == null || attributeValue4 == null) {
                        break;
                    }
                    bundle.putString(attributeValue3, attributeValue4);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: a.zip:com/android/launcher3/AutoInstallsLayout$AutoInstallParser.class */
    public class AutoInstallParser implements TagParser {
        final AutoInstallsLayout this$0;

        protected AutoInstallParser(AutoInstallsLayout autoInstallsLayout) {
            this.this$0 = autoInstallsLayout;
        }

        @Override // com.android.launcher3.AutoInstallsLayout.TagParser
        public long parseAndAdd(XmlResourceParser xmlResourceParser) {
            String attributeValue = AutoInstallsLayout.getAttributeValue(xmlResourceParser, "packageName");
            String attributeValue2 = AutoInstallsLayout.getAttributeValue(xmlResourceParser, "className");
            if (TextUtils.isEmpty(attributeValue) || TextUtils.isEmpty(attributeValue2)) {
                return -1L;
            }
            this.this$0.mValues.put("restored", (Integer) 2);
            return this.this$0.addShortcut(this.this$0.mContext.getString(2131558458), new Intent("android.intent.action.MAIN", (Uri) null).addCategory("android.intent.category.LAUNCHER").setComponent(new ComponentName(attributeValue, attributeValue2)).setFlags(270532608), 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: a.zip:com/android/launcher3/AutoInstallsLayout$FolderParser.class */
    public class FolderParser implements TagParser {
        private final HashMap<String, TagParser> mFolderElements;
        final AutoInstallsLayout this$0;

        public FolderParser(AutoInstallsLayout autoInstallsLayout) {
            this(autoInstallsLayout, autoInstallsLayout.getFolderElementsMap());
        }

        public FolderParser(AutoInstallsLayout autoInstallsLayout, HashMap<String, TagParser> hashMap) {
            this.this$0 = autoInstallsLayout;
            this.mFolderElements = hashMap;
        }

        @Override // com.android.launcher3.AutoInstallsLayout.TagParser
        public long parseAndAdd(XmlResourceParser xmlResourceParser) throws XmlPullParserException, IOException {
            int attributeResourceValue = AutoInstallsLayout.getAttributeResourceValue(xmlResourceParser, "title", 0);
            this.this$0.mValues.put("title", attributeResourceValue != 0 ? this.this$0.mSourceRes.getString(attributeResourceValue) : this.this$0.mContext.getResources().getString(2131558405));
            this.this$0.mValues.put("itemType", (Integer) 2);
            this.this$0.mValues.put("spanX", (Integer) 1);
            this.this$0.mValues.put("spanY", (Integer) 1);
            this.this$0.mValues.put("_id", Long.valueOf(this.this$0.mCallback.generateNewItemId()));
            long insertAndCheck = this.this$0.mCallback.insertAndCheck(this.this$0.mDb, this.this$0.mValues);
            if (insertAndCheck < 0) {
                return -1L;
            }
            ContentValues contentValues = new ContentValues(this.this$0.mValues);
            ArrayList arrayList = new ArrayList();
            int depth = xmlResourceParser.getDepth();
            int i = 0;
            while (true) {
                int next = xmlResourceParser.next();
                if (next == 3 && xmlResourceParser.getDepth() <= depth) {
                    long j = insertAndCheck;
                    if (arrayList.size() < 2) {
                        LauncherProvider.SqlArguments sqlArguments = new LauncherProvider.SqlArguments(LauncherSettings$Favorites.getContentUri(insertAndCheck), null, null);
                        this.this$0.mDb.delete(sqlArguments.table, sqlArguments.where, sqlArguments.args);
                        j = -1;
                        if (arrayList.size() == 1) {
                            ContentValues contentValues2 = new ContentValues();
                            AutoInstallsLayout.copyInteger(contentValues, contentValues2, "container");
                            AutoInstallsLayout.copyInteger(contentValues, contentValues2, "screen");
                            AutoInstallsLayout.copyInteger(contentValues, contentValues2, "cellX");
                            AutoInstallsLayout.copyInteger(contentValues, contentValues2, "cellY");
                            j = ((Long) arrayList.get(0)).longValue();
                            this.this$0.mDb.update("favorites", contentValues2, "_id=" + j, null);
                        }
                    }
                    return j;
                } else if (next == 2) {
                    this.this$0.mValues.clear();
                    this.this$0.mValues.put("container", Long.valueOf(insertAndCheck));
                    this.this$0.mValues.put("rank", Integer.valueOf(i));
                    TagParser tagParser = this.mFolderElements.get(xmlResourceParser.getName());
                    if (tagParser == null) {
                        throw new RuntimeException("Invalid folder item " + xmlResourceParser.getName());
                    }
                    long parseAndAdd = tagParser.parseAndAdd(xmlResourceParser);
                    if (parseAndAdd >= 0) {
                        arrayList.add(Long.valueOf(parseAndAdd));
                        i++;
                    }
                }
            }
        }
    }

    /* loaded from: a.zip:com/android/launcher3/AutoInstallsLayout$LayoutParserCallback.class */
    public interface LayoutParserCallback {
        long generateNewItemId();

        long insertAndCheck(SQLiteDatabase sQLiteDatabase, ContentValues contentValues);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: a.zip:com/android/launcher3/AutoInstallsLayout$ShortcutParser.class */
    public class ShortcutParser implements TagParser {
        private final Resources mIconRes;
        final AutoInstallsLayout this$0;

        public ShortcutParser(AutoInstallsLayout autoInstallsLayout, Resources resources) {
            this.this$0 = autoInstallsLayout;
            this.mIconRes = resources;
        }

        @Override // com.android.launcher3.AutoInstallsLayout.TagParser
        public long parseAndAdd(XmlResourceParser xmlResourceParser) {
            Drawable drawable;
            int attributeResourceValue = AutoInstallsLayout.getAttributeResourceValue(xmlResourceParser, "title", 0);
            int attributeResourceValue2 = AutoInstallsLayout.getAttributeResourceValue(xmlResourceParser, "icon", 0);
            if (attributeResourceValue == 0 || attributeResourceValue2 == 0) {
                return -1L;
            }
            Intent parseIntent = BenesseExtension.getDchaState() == 0 ? parseIntent(xmlResourceParser) : null;
            if (parseIntent == null || (drawable = this.mIconRes.getDrawable(attributeResourceValue2)) == null) {
                return -1L;
            }
            ItemInfo.writeBitmap(this.this$0.mValues, Utilities.createIconBitmap(drawable, this.this$0.mContext));
            this.this$0.mValues.put("iconType", (Integer) 0);
            this.this$0.mValues.put("iconPackage", this.mIconRes.getResourcePackageName(attributeResourceValue2));
            this.this$0.mValues.put("iconResource", this.mIconRes.getResourceName(attributeResourceValue2));
            parseIntent.setFlags(270532608);
            return this.this$0.addShortcut(this.this$0.mSourceRes.getString(attributeResourceValue), parseIntent, 1);
        }

        protected Intent parseIntent(XmlResourceParser xmlResourceParser) {
            String attributeValue = AutoInstallsLayout.getAttributeValue(xmlResourceParser, "url");
            if (TextUtils.isEmpty(attributeValue) || !Patterns.WEB_URL.matcher(attributeValue).matches()) {
                return null;
            }
            return new Intent("android.intent.action.VIEW", (Uri) null).setData(Uri.parse(attributeValue));
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: a.zip:com/android/launcher3/AutoInstallsLayout$TagParser.class */
    public interface TagParser {
        long parseAndAdd(XmlResourceParser xmlResourceParser) throws XmlPullParserException, IOException;
    }

    public AutoInstallsLayout(Context context, AppWidgetHost appWidgetHost, LayoutParserCallback layoutParserCallback, Resources resources, int i, String str) {
        this.mContext = context;
        this.mAppWidgetHost = appWidgetHost;
        this.mCallback = layoutParserCallback;
        this.mPackageManager = context.getPackageManager();
        this.mRootTag = str;
        this.mSourceRes = resources;
        this.mLayoutId = i;
        InvariantDeviceProfile invariantDeviceProfile = LauncherAppState.getInstance().getInvariantDeviceProfile();
        this.mHotseatAllAppsRank = invariantDeviceProfile.hotseatAllAppsRank;
        this.mRowCount = invariantDeviceProfile.numRows;
        this.mColumnCount = invariantDeviceProfile.numColumns;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static final void beginDocument(XmlPullParser xmlPullParser, String str) throws XmlPullParserException, IOException {
        int next;
        do {
            next = xmlPullParser.next();
            if (next == 2) {
                break;
            }
        } while (next != 1);
        if (next != 2) {
            throw new XmlPullParserException("No start tag found");
        }
        if (!xmlPullParser.getName().equals(str)) {
            throw new XmlPullParserException("Unexpected start tag: found " + xmlPullParser.getName() + ", expected " + str);
        }
    }

    private static String convertToDistanceFromEnd(String str, int i) {
        int parseInt;
        return (TextUtils.isEmpty(str) || (parseInt = Integer.parseInt(str)) >= 0) ? str : Integer.toString(i + parseInt);
    }

    static void copyInteger(ContentValues contentValues, ContentValues contentValues2, String str) {
        contentValues2.put(str, contentValues.getAsInteger(str));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static AutoInstallsLayout get(Context context, AppWidgetHost appWidgetHost, LayoutParserCallback layoutParserCallback) {
        Pair<String, Resources> findSystemApk = Utilities.findSystemApk("android.autoinstalls.config.action.PLAY_AUTO_INSTALL", context.getPackageManager());
        if (findSystemApk == null) {
            return null;
        }
        return get(context, (String) findSystemApk.first, (Resources) findSystemApk.second, appWidgetHost, layoutParserCallback);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static AutoInstallsLayout get(Context context, String str, Resources resources, AppWidgetHost appWidgetHost, LayoutParserCallback layoutParserCallback) {
        InvariantDeviceProfile invariantDeviceProfile = LauncherAppState.getInstance().getInvariantDeviceProfile();
        String format = String.format(Locale.ENGLISH, "default_layout_%dx%d_h%s", Integer.valueOf(invariantDeviceProfile.numColumns), Integer.valueOf(invariantDeviceProfile.numRows), Integer.valueOf(invariantDeviceProfile.numHotseatIcons));
        int identifier = resources.getIdentifier(format, "xml", str);
        int i = identifier;
        String str2 = format;
        if (identifier == 0) {
            Log.d("AutoInstalls", "Formatted layout: " + format + " not found. Trying layout without hosteat");
            str2 = String.format(Locale.ENGLISH, "default_layout_%dx%d", Integer.valueOf(invariantDeviceProfile.numColumns), Integer.valueOf(invariantDeviceProfile.numRows));
            i = resources.getIdentifier(str2, "xml", str);
        }
        int i2 = i;
        if (i == 0) {
            Log.d("AutoInstalls", "Formatted layout: " + str2 + " not found. Trying the default layout");
            i2 = resources.getIdentifier("default_layout", "xml", str);
        }
        if (i2 == 0) {
            Log.e("AutoInstalls", "Layout definition not found in package: " + str);
            return null;
        }
        return new AutoInstallsLayout(context, appWidgetHost, layoutParserCallback, resources, i2, "workspace");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static int getAttributeResourceValue(XmlResourceParser xmlResourceParser, String str, int i) {
        int attributeResourceValue = xmlResourceParser.getAttributeResourceValue("http://schemas.android.com/apk/res-auto/com.android.launcher3", str, i);
        int i2 = attributeResourceValue;
        if (attributeResourceValue == i) {
            i2 = xmlResourceParser.getAttributeResourceValue(null, str, i);
        }
        return i2;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static String getAttributeValue(XmlResourceParser xmlResourceParser, String str) {
        String attributeValue = xmlResourceParser.getAttributeValue("http://schemas.android.com/apk/res-auto/com.android.launcher3", str);
        String str2 = attributeValue;
        if (attributeValue == null) {
            str2 = xmlResourceParser.getAttributeValue(null, str);
        }
        return str2;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public long addShortcut(String str, Intent intent, int i) {
        long generateNewItemId = this.mCallback.generateNewItemId();
        this.mValues.put("intent", intent.toUri(0));
        this.mValues.put("title", str);
        this.mValues.put("itemType", Integer.valueOf(i));
        this.mValues.put("spanX", (Integer) 1);
        this.mValues.put("spanY", (Integer) 1);
        this.mValues.put("_id", Long.valueOf(generateNewItemId));
        if (this.mCallback.insertAndCheck(this.mDb, this.mValues) < 0) {
            return -1L;
        }
        return generateNewItemId;
    }

    protected HashMap<String, TagParser> getFolderElementsMap() {
        HashMap<String, TagParser> hashMap = new HashMap<>();
        hashMap.put("appicon", new AppShortcutParser(this));
        hashMap.put("autoinstall", new AutoInstallParser(this));
        hashMap.put("shortcut", new ShortcutParser(this, this.mSourceRes));
        return hashMap;
    }

    protected HashMap<String, TagParser> getLayoutElementsMap() {
        HashMap<String, TagParser> hashMap = new HashMap<>();
        hashMap.put("appicon", new AppShortcutParser(this));
        hashMap.put("autoinstall", new AutoInstallParser(this));
        hashMap.put("folder", new FolderParser(this));
        hashMap.put("appwidget", new AppWidgetParser(this));
        hashMap.put("shortcut", new ShortcutParser(this, this.mSourceRes));
        return hashMap;
    }

    public int loadLayout(SQLiteDatabase sQLiteDatabase, ArrayList<Long> arrayList) {
        this.mDb = sQLiteDatabase;
        try {
            return parseLayout(this.mLayoutId, arrayList);
        } catch (Exception e) {
            Log.w("AutoInstalls", "Got exception parsing layout.", e);
            return -1;
        }
    }

    protected int parseAndAddNode(XmlResourceParser xmlResourceParser, HashMap<String, TagParser> hashMap, ArrayList<Long> arrayList) throws XmlPullParserException, IOException {
        if ("include".equals(xmlResourceParser.getName())) {
            int attributeResourceValue = getAttributeResourceValue(xmlResourceParser, "workspace", 0);
            if (attributeResourceValue != 0) {
                return parseLayout(attributeResourceValue, arrayList);
            }
            return 0;
        }
        this.mValues.clear();
        parseContainerAndScreen(xmlResourceParser, this.mTemp);
        long j = this.mTemp[0];
        long j2 = this.mTemp[1];
        this.mValues.put("container", Long.valueOf(j));
        this.mValues.put("screen", Long.valueOf(j2));
        this.mValues.put("cellX", convertToDistanceFromEnd(getAttributeValue(xmlResourceParser, "x"), this.mColumnCount));
        this.mValues.put("cellY", convertToDistanceFromEnd(getAttributeValue(xmlResourceParser, "y"), this.mRowCount));
        TagParser tagParser = hashMap.get(xmlResourceParser.getName());
        if (tagParser != null && tagParser.parseAndAdd(xmlResourceParser) >= 0) {
            if (arrayList.contains(Long.valueOf(j2)) || j != -100) {
                return 1;
            }
            arrayList.add(Long.valueOf(j2));
            return 1;
        }
        return 0;
    }

    protected void parseContainerAndScreen(XmlResourceParser xmlResourceParser, long[] jArr) {
        if (!HOTSEAT_CONTAINER_NAME.equals(getAttributeValue(xmlResourceParser, "container"))) {
            jArr[0] = -100;
            jArr[1] = Long.parseLong(getAttributeValue(xmlResourceParser, "screen"));
            return;
        }
        jArr[0] = -101;
        long parseLong = Long.parseLong(getAttributeValue(xmlResourceParser, "rank"));
        if (parseLong >= this.mHotseatAllAppsRank) {
            parseLong++;
        }
        jArr[1] = parseLong;
    }

    protected int parseLayout(int i, ArrayList<Long> arrayList) throws XmlPullParserException, IOException {
        XmlResourceParser xml = this.mSourceRes.getXml(i);
        beginDocument(xml, this.mRootTag);
        int depth = xml.getDepth();
        HashMap<String, TagParser> layoutElementsMap = getLayoutElementsMap();
        int i2 = 0;
        while (true) {
            int next = xml.next();
            if ((next != 3 || xml.getDepth() > depth) && next != 1) {
                if (next == 2) {
                    i2 += parseAndAddNode(xml, layoutElementsMap, arrayList);
                }
            }
        }
        return i2;
    }
}
