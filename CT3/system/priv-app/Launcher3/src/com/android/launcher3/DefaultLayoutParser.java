package com.android.launcher3;

import android.appwidget.AppWidgetHost;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Log;
import com.android.launcher3.AutoInstallsLayout;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: a.zip:com/android/launcher3/DefaultLayoutParser.class */
public class DefaultLayoutParser extends AutoInstallsLayout {

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/DefaultLayoutParser$AppShortcutWithUriParser.class */
    public class AppShortcutWithUriParser extends AutoInstallsLayout.AppShortcutParser {
        final DefaultLayoutParser this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AppShortcutWithUriParser(DefaultLayoutParser defaultLayoutParser) {
            super(defaultLayoutParser);
            this.this$0 = defaultLayoutParser;
        }

        private ResolveInfo getSingleSystemActivity(List<ResolveInfo> list) {
            ResolveInfo resolveInfo = null;
            int size = list.size();
            int i = 0;
            while (i < size) {
                ResolveInfo resolveInfo2 = resolveInfo;
                try {
                    if ((this.this$0.mPackageManager.getApplicationInfo(list.get(i).activityInfo.packageName, 0).flags & 1) != 0) {
                        if (resolveInfo != null) {
                            return null;
                        }
                        resolveInfo2 = list.get(i);
                    }
                    i++;
                    resolveInfo = resolveInfo2;
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w("DefaultLayoutParser", "Unable to get info about resolve results", e);
                    return null;
                }
            }
            return resolveInfo;
        }

        private boolean wouldLaunchResolverActivity(ResolveInfo resolveInfo, List<ResolveInfo> list) {
            for (int i = 0; i < list.size(); i++) {
                ResolveInfo resolveInfo2 = list.get(i);
                if (resolveInfo2.activityInfo.name.equals(resolveInfo.activityInfo.name) && resolveInfo2.activityInfo.packageName.equals(resolveInfo.activityInfo.packageName)) {
                    return false;
                }
            }
            return true;
        }

        @Override // com.android.launcher3.AutoInstallsLayout.AppShortcutParser
        protected long invalidPackageOrClass(XmlResourceParser xmlResourceParser) {
            String attributeValue = DefaultLayoutParser.getAttributeValue(xmlResourceParser, "uri");
            if (TextUtils.isEmpty(attributeValue)) {
                Log.e("DefaultLayoutParser", "Skipping invalid <favorite> with no component or uri");
                return -1L;
            }
            try {
                Intent parseUri = Intent.parseUri(attributeValue, 0);
                ResolveInfo resolveActivity = this.this$0.mPackageManager.resolveActivity(parseUri, 65536);
                List<ResolveInfo> queryIntentActivities = this.this$0.mPackageManager.queryIntentActivities(parseUri, 65536);
                ResolveInfo resolveInfo = resolveActivity;
                if (wouldLaunchResolverActivity(resolveActivity, queryIntentActivities)) {
                    resolveInfo = getSingleSystemActivity(queryIntentActivities);
                    if (resolveInfo == null) {
                        Log.w("DefaultLayoutParser", "No preference or single system activity found for " + parseUri.toString());
                        return -1L;
                    }
                }
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                Intent launchIntentForPackage = this.this$0.mPackageManager.getLaunchIntentForPackage(activityInfo.packageName);
                if (launchIntentForPackage == null) {
                    return -1L;
                }
                if ("com.android.gallery3d".equals(activityInfo.packageName)) {
                    launchIntentForPackage.setPackage(null);
                }
                launchIntentForPackage.setFlags(270532608);
                return this.this$0.addShortcut(activityInfo.loadLabel(this.this$0.mPackageManager).toString(), launchIntentForPackage, 0);
            } catch (URISyntaxException e) {
                Log.e("DefaultLayoutParser", "Unable to add meta-favorite: " + attributeValue, e);
                return -1L;
            }
        }
    }

    /* loaded from: a.zip:com/android/launcher3/DefaultLayoutParser$MyFolderParser.class */
    class MyFolderParser extends AutoInstallsLayout.FolderParser {
        final DefaultLayoutParser this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        MyFolderParser(DefaultLayoutParser defaultLayoutParser) {
            super(defaultLayoutParser);
            this.this$0 = defaultLayoutParser;
        }

        @Override // com.android.launcher3.AutoInstallsLayout.FolderParser, com.android.launcher3.AutoInstallsLayout.TagParser
        public long parseAndAdd(XmlResourceParser xmlResourceParser) throws XmlPullParserException, IOException {
            int attributeResourceValue = DefaultLayoutParser.getAttributeResourceValue(xmlResourceParser, "folderItems", 0);
            if (attributeResourceValue != 0) {
                xmlResourceParser = this.this$0.mSourceRes.getXml(attributeResourceValue);
                DefaultLayoutParser.beginDocument(xmlResourceParser, "folder");
            }
            return super.parseAndAdd(xmlResourceParser);
        }
    }

    /* loaded from: a.zip:com/android/launcher3/DefaultLayoutParser$PartnerFolderParser.class */
    class PartnerFolderParser implements AutoInstallsLayout.TagParser {
        final DefaultLayoutParser this$0;

        PartnerFolderParser(DefaultLayoutParser defaultLayoutParser) {
            this.this$0 = defaultLayoutParser;
        }

        @Override // com.android.launcher3.AutoInstallsLayout.TagParser
        public long parseAndAdd(XmlResourceParser xmlResourceParser) throws XmlPullParserException, IOException {
            Resources resources;
            int identifier;
            Partner partner = Partner.get(this.this$0.mPackageManager);
            if (partner == null || (identifier = (resources = partner.getResources()).getIdentifier("partner_folder", "xml", partner.getPackageName())) == 0) {
                return -1L;
            }
            XmlResourceParser xml = resources.getXml(identifier);
            DefaultLayoutParser.beginDocument(xml, "folder");
            return new AutoInstallsLayout.FolderParser(this.this$0, this.this$0.getFolderElementsMap(resources)).parseAndAdd(xml);
        }
    }

    /* loaded from: a.zip:com/android/launcher3/DefaultLayoutParser$ResolveParser.class */
    protected class ResolveParser implements AutoInstallsLayout.TagParser {
        private final AppShortcutWithUriParser mChildParser;
        final DefaultLayoutParser this$0;

        /* JADX INFO: Access modifiers changed from: protected */
        public ResolveParser(DefaultLayoutParser defaultLayoutParser) {
            this.this$0 = defaultLayoutParser;
            this.mChildParser = new AppShortcutWithUriParser(this.this$0);
        }

        @Override // com.android.launcher3.AutoInstallsLayout.TagParser
        public long parseAndAdd(XmlResourceParser xmlResourceParser) throws XmlPullParserException, IOException {
            int depth = xmlResourceParser.getDepth();
            long j = -1;
            while (true) {
                int next = xmlResourceParser.next();
                if (next == 3 && xmlResourceParser.getDepth() <= depth) {
                    return j;
                }
                if (next == 2 && j <= -1) {
                    String name = xmlResourceParser.getName();
                    if ("favorite".equals(name)) {
                        j = this.mChildParser.parseAndAdd(xmlResourceParser);
                    } else {
                        Log.e("DefaultLayoutParser", "Fallback groups can contain only favorites, found " + name);
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/DefaultLayoutParser$UriShortcutParser.class */
    public class UriShortcutParser extends AutoInstallsLayout.ShortcutParser {
        final DefaultLayoutParser this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public UriShortcutParser(DefaultLayoutParser defaultLayoutParser, Resources resources) {
            super(defaultLayoutParser, resources);
            this.this$0 = defaultLayoutParser;
        }

        @Override // com.android.launcher3.AutoInstallsLayout.ShortcutParser
        protected Intent parseIntent(XmlResourceParser xmlResourceParser) {
            String str = null;
            try {
                String attributeValue = DefaultLayoutParser.getAttributeValue(xmlResourceParser, "uri");
                str = attributeValue;
                return Intent.parseUri(attributeValue, 0);
            } catch (URISyntaxException e) {
                Log.w("DefaultLayoutParser", "Shortcut has malformed uri: " + str);
                return null;
            }
        }
    }

    public DefaultLayoutParser(Context context, AppWidgetHost appWidgetHost, AutoInstallsLayout.LayoutParserCallback layoutParserCallback, Resources resources, int i) {
        super(context, appWidgetHost, layoutParserCallback, resources, i, "favorites");
    }

    public DefaultLayoutParser(Context context, AppWidgetHost appWidgetHost, AutoInstallsLayout.LayoutParserCallback layoutParserCallback, Resources resources, int i, String str) {
        super(context, appWidgetHost, layoutParserCallback, resources, i, str);
    }

    @Override // com.android.launcher3.AutoInstallsLayout
    protected HashMap<String, AutoInstallsLayout.TagParser> getFolderElementsMap() {
        return getFolderElementsMap(this.mSourceRes);
    }

    HashMap<String, AutoInstallsLayout.TagParser> getFolderElementsMap(Resources resources) {
        HashMap<String, AutoInstallsLayout.TagParser> hashMap = new HashMap<>();
        hashMap.put("favorite", new AppShortcutWithUriParser(this));
        hashMap.put("shortcut", new UriShortcutParser(this, resources));
        return hashMap;
    }

    @Override // com.android.launcher3.AutoInstallsLayout
    protected HashMap<String, AutoInstallsLayout.TagParser> getLayoutElementsMap() {
        HashMap<String, AutoInstallsLayout.TagParser> hashMap = new HashMap<>();
        hashMap.put("favorite", new AppShortcutWithUriParser(this));
        hashMap.put("appwidget", new AutoInstallsLayout.AppWidgetParser(this));
        hashMap.put("shortcut", new UriShortcutParser(this, this.mSourceRes));
        hashMap.put("resolve", new ResolveParser(this));
        hashMap.put("folder", new MyFolderParser(this));
        hashMap.put("partner-folder", new PartnerFolderParser(this));
        return hashMap;
    }

    @Override // com.android.launcher3.AutoInstallsLayout
    protected void parseContainerAndScreen(XmlResourceParser xmlResourceParser, long[] jArr) {
        jArr[0] = -100;
        String attributeValue = getAttributeValue(xmlResourceParser, "container");
        if (attributeValue != null) {
            jArr[0] = Long.valueOf(attributeValue).longValue();
        }
        jArr[1] = Long.parseLong(getAttributeValue(xmlResourceParser, "screen"));
    }
}
