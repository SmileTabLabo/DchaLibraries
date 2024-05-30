package com.android.launcher3.widget.custom;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Parcel;
import android.os.Process;
import android.util.SparseArray;
import android.util.Xml;
import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: classes.dex */
public class CustomWidgetParser {
    private static List<LauncherAppWidgetProviderInfo> sCustomWidgets;
    private static SparseArray<ComponentName> sWidgetsIdMap;

    public static List<LauncherAppWidgetProviderInfo> getCustomWidgets(Context context) {
        if (sCustomWidgets == null) {
            parseCustomWidgets(context);
        }
        return sCustomWidgets;
    }

    public static int getWidgetIdForCustomProvider(Context context, ComponentName componentName) {
        if (sWidgetsIdMap == null) {
            parseCustomWidgets(context);
        }
        int indexOfValue = sWidgetsIdMap.indexOfValue(componentName);
        if (indexOfValue >= 0) {
            return (-100) - sWidgetsIdMap.keyAt(indexOfValue);
        }
        return 0;
    }

    public static LauncherAppWidgetProviderInfo getWidgetProvider(Context context, int i) {
        if (sWidgetsIdMap == null || sCustomWidgets == null) {
            parseCustomWidgets(context);
        }
        ComponentName componentName = sWidgetsIdMap.get((-100) - i);
        for (LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo : sCustomWidgets) {
            if (launcherAppWidgetProviderInfo.provider.equals(componentName)) {
                return launcherAppWidgetProviderInfo;
            }
        }
        return null;
    }

    private static void parseCustomWidgets(Context context) {
        ArrayList arrayList = new ArrayList();
        SparseArray<ComponentName> sparseArray = new SparseArray<>();
        List<AppWidgetProviderInfo> installedProvidersForProfile = AppWidgetManager.getInstance(context).getInstalledProvidersForProfile(Process.myUserHandle());
        if (installedProvidersForProfile.isEmpty()) {
            sCustomWidgets = arrayList;
            sWidgetsIdMap = sparseArray;
            return;
        }
        Parcel obtain = Parcel.obtain();
        installedProvidersForProfile.get(0).writeToParcel(obtain, 0);
        try {
            XmlResourceParser xml = context.getResources().getXml(R.xml.custom_widgets);
            int depth = xml.getDepth();
            while (true) {
                int next = xml.next();
                if ((next != 3 || xml.getDepth() > depth) && next != 1) {
                    if (next == 2 && "widget".equals(xml.getName())) {
                        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(Xml.asAttributeSet(xml), R.styleable.CustomAppWidgetProviderInfo);
                        obtain.setDataPosition(0);
                        CustomAppWidgetProviderInfo newInfo = newInfo(obtainStyledAttributes, obtain, context);
                        arrayList.add(newInfo);
                        obtainStyledAttributes.recycle();
                        sparseArray.put(newInfo.providerId, newInfo.provider);
                    }
                }
            }
            if (xml != null) {
                xml.close();
            }
            obtain.recycle();
            sCustomWidgets = arrayList;
            sWidgetsIdMap = sparseArray;
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    private static CustomAppWidgetProviderInfo newInfo(TypedArray typedArray, Parcel parcel, Context context) {
        int i = typedArray.getInt(9, 0);
        CustomAppWidgetProviderInfo customAppWidgetProviderInfo = new CustomAppWidgetProviderInfo(parcel, false, i);
        String packageName = context.getPackageName();
        customAppWidgetProviderInfo.provider = new ComponentName(packageName, LauncherAppWidgetProviderInfo.CLS_CUSTOM_WIDGET_PREFIX + i);
        customAppWidgetProviderInfo.label = typedArray.getString(0);
        customAppWidgetProviderInfo.initialLayout = typedArray.getResourceId(2, 0);
        customAppWidgetProviderInfo.icon = typedArray.getResourceId(1, 0);
        customAppWidgetProviderInfo.previewImage = typedArray.getResourceId(3, 0);
        customAppWidgetProviderInfo.resizeMode = typedArray.getInt(4, 0);
        customAppWidgetProviderInfo.spanX = typedArray.getInt(5, 1);
        customAppWidgetProviderInfo.spanY = typedArray.getInt(8, 1);
        customAppWidgetProviderInfo.minSpanX = typedArray.getInt(6, 1);
        customAppWidgetProviderInfo.minSpanY = typedArray.getInt(7, 1);
        return customAppWidgetProviderInfo;
    }
}
