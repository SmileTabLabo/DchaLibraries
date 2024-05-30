package com.android.settings.slices;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.accessibility.AccessibilitySettings;
import com.android.settings.accessibility.AccessibilitySlicePreferenceController;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceXmlParserUtils;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.Indexable;
import com.android.settings.slices.SliceData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: classes.dex */
class SliceDataConverter {
    private Context mContext;
    private List<SliceData> mSliceData = new ArrayList();

    public SliceDataConverter(Context context) {
        this.mContext = context;
    }

    public List<SliceData> getSliceData() {
        if (!this.mSliceData.isEmpty()) {
            return this.mSliceData;
        }
        for (Class cls : FeatureFactory.getFactory(this.mContext).getSearchFeatureProvider().getSearchIndexableResources().getProviderValues()) {
            String name = cls.getName();
            Indexable.SearchIndexProvider searchIndexProvider = DatabaseIndexingUtils.getSearchIndexProvider(cls);
            if (searchIndexProvider == null) {
                Log.e("SliceDataConverter", name + " dose not implement Search Index Provider");
            } else {
                this.mSliceData.addAll(getSliceDataFromProvider(searchIndexProvider, name));
            }
        }
        this.mSliceData.addAll(getAccessibilitySliceData());
        return this.mSliceData;
    }

    private List<SliceData> getSliceDataFromProvider(Indexable.SearchIndexProvider searchIndexProvider, String str) {
        ArrayList arrayList = new ArrayList();
        List<SearchIndexableResource> xmlResourcesToIndex = searchIndexProvider.getXmlResourcesToIndex(this.mContext, true);
        if (xmlResourcesToIndex == null) {
            return arrayList;
        }
        for (SearchIndexableResource searchIndexableResource : xmlResourcesToIndex) {
            int i = searchIndexableResource.xmlResId;
            if (i == 0) {
                Log.e("SliceDataConverter", str + " provides invalid XML (0) in search provider.");
            } else {
                arrayList.addAll(getSliceDataFromXML(i, str));
            }
        }
        return arrayList;
    }

    /* JADX WARN: Code restructure failed: missing block: B:49:0x0117, code lost:
        if (r1 == 0) goto L37;
     */
    /* JADX WARN: Code restructure failed: missing block: B:53:0x0134, code lost:
        if (r1 == 0) goto L37;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v10 */
    /* JADX WARN: Type inference failed for: r1v11 */
    /* JADX WARN: Type inference failed for: r1v18, types: [java.lang.CharSequence, java.lang.String] */
    /* JADX WARN: Type inference failed for: r1v19 */
    /* JADX WARN: Type inference failed for: r1v2 */
    /* JADX WARN: Type inference failed for: r1v20 */
    /* JADX WARN: Type inference failed for: r1v21 */
    /* JADX WARN: Type inference failed for: r1v22 */
    /* JADX WARN: Type inference failed for: r1v3 */
    /* JADX WARN: Type inference failed for: r1v4 */
    /* JADX WARN: Type inference failed for: r1v5, types: [android.content.res.XmlResourceParser] */
    /* JADX WARN: Type inference failed for: r1v6 */
    /* JADX WARN: Type inference failed for: r1v7 */
    /* JADX WARN: Type inference failed for: r1v8 */
    /* JADX WARN: Type inference failed for: r1v9 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private List<SliceData> getSliceDataFromXML(int i, String str) {
        XmlResourceParser xmlResourceParser;
        String name;
        ArrayList arrayList = new ArrayList();
        XmlResourceParser xmlResourceParser2 = 0;
        try {
            try {
                xmlResourceParser = this.mContext.getResources().getXml(i);
                while (true) {
                    try {
                        int next = xmlResourceParser.next();
                        if (next == 1 || next == 2) {
                            break;
                        }
                    } catch (Resources.NotFoundException e) {
                        e = e;
                        xmlResourceParser2 = xmlResourceParser;
                        Log.w("SliceDataConverter", "Resource not found error parsing PreferenceScreen: ", e);
                        if (xmlResourceParser2 != 0) {
                            xmlResourceParser2 = xmlResourceParser2;
                            xmlResourceParser2.close();
                        }
                        return arrayList;
                    } catch (SliceData.InvalidSliceDataException e2) {
                        e = e2;
                        xmlResourceParser2 = xmlResourceParser;
                        Log.w("SliceDataConverter", "Invalid data when building SliceData for " + str, e);
                        xmlResourceParser2 = xmlResourceParser2;
                    } catch (IOException e3) {
                        e = e3;
                        xmlResourceParser2 = xmlResourceParser;
                        Log.w("SliceDataConverter", "IO Error parsing PreferenceScreen: ", e);
                        if (xmlResourceParser2 != 0) {
                            xmlResourceParser2 = xmlResourceParser2;
                            xmlResourceParser2.close();
                        }
                        return arrayList;
                    } catch (XmlPullParserException e4) {
                        e = e4;
                        xmlResourceParser2 = xmlResourceParser;
                        Log.w("SliceDataConverter", "XML Error parsing PreferenceScreen: ", e);
                        xmlResourceParser2 = xmlResourceParser2;
                    } catch (Throwable th) {
                        th = th;
                        if (xmlResourceParser != null) {
                            xmlResourceParser.close();
                        }
                        throw th;
                    }
                }
                name = xmlResourceParser.getName();
            } catch (Throwable th2) {
                th = th2;
                xmlResourceParser = xmlResourceParser2;
            }
        } catch (Resources.NotFoundException e5) {
            e = e5;
        } catch (SliceData.InvalidSliceDataException e6) {
            e = e6;
        } catch (IOException e7) {
            e = e7;
        } catch (XmlPullParserException e8) {
            e = e8;
        }
        if (!"PreferenceScreen".equals(name)) {
            throw new RuntimeException("XML document must start with <PreferenceScreen> tag; found" + name + " at " + xmlResourceParser.getPositionDescription());
        }
        xmlResourceParser2 = PreferenceXmlParserUtils.getDataTitle(this.mContext, Xml.asAttributeSet(xmlResourceParser));
        for (Bundle bundle : PreferenceXmlParserUtils.extractMetadata(this.mContext, i, 254)) {
            String string = bundle.getString("controller");
            if (!TextUtils.isEmpty(string)) {
                String string2 = bundle.getString("key");
                String string3 = bundle.getString("title");
                String string4 = bundle.getString("summary");
                int i2 = bundle.getInt("icon");
                int sliceType = SliceBuilderUtils.getSliceType(this.mContext, string, string2);
                SliceData build = new SliceData.Builder().setKey(string2).setTitle(string3).setSummary(string4).setIcon(i2).setScreenTitle(xmlResourceParser2).setPreferenceControllerClassName(string).setFragmentName(str).setSliceType(sliceType).setPlatformDefined(bundle.getBoolean("platform_slice")).build();
                BasePreferenceController preferenceController = SliceBuilderUtils.getPreferenceController(this.mContext, build);
                if (preferenceController.isAvailable() && preferenceController.isSliceable()) {
                    arrayList.add(build);
                }
            }
        }
        if (xmlResourceParser != null) {
            xmlResourceParser.close();
        }
        return arrayList;
    }

    private List<SliceData> getAccessibilitySliceData() {
        ArrayList arrayList = new ArrayList();
        String name = AccessibilitySlicePreferenceController.class.getName();
        String name2 = AccessibilitySettings.class.getName();
        SliceData.Builder preferenceControllerClassName = new SliceData.Builder().setFragmentName(name2).setScreenTitle(this.mContext.getText(R.string.accessibility_settings)).setPreferenceControllerClassName(name);
        HashSet hashSet = new HashSet();
        Collections.addAll(hashSet, this.mContext.getResources().getStringArray(R.array.config_settings_slices_accessibility_components));
        List<AccessibilityServiceInfo> accessibilityServiceInfoList = getAccessibilityServiceInfoList();
        PackageManager packageManager = this.mContext.getPackageManager();
        for (AccessibilityServiceInfo accessibilityServiceInfo : accessibilityServiceInfoList) {
            ResolveInfo resolveInfo = accessibilityServiceInfo.getResolveInfo();
            ServiceInfo serviceInfo = resolveInfo.serviceInfo;
            String flattenToString = new ComponentName(serviceInfo.packageName, serviceInfo.name).flattenToString();
            if (hashSet.contains(flattenToString)) {
                String charSequence = resolveInfo.loadLabel(packageManager).toString();
                int iconResource = resolveInfo.getIconResource();
                if (iconResource == 0) {
                    iconResource = R.mipmap.ic_accessibility_generic;
                }
                preferenceControllerClassName.setKey(flattenToString).setTitle(charSequence).setIcon(iconResource).setSliceType(1);
                try {
                    arrayList.add(preferenceControllerClassName.build());
                } catch (SliceData.InvalidSliceDataException e) {
                    Log.w("SliceDataConverter", "Invalid data when building a11y SliceData for " + flattenToString, e);
                }
            }
        }
        return arrayList;
    }

    @VisibleForTesting
    List<AccessibilityServiceInfo> getAccessibilityServiceInfoList() {
        return AccessibilityManager.getInstance(this.mContext).getInstalledAccessibilityServiceList();
    }
}
