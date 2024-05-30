package com.android.settings.search;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.provider.SearchIndexableResource;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerListHelper;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.PreferenceXmlParserUtils;
import com.android.settings.search.Indexable;
import com.android.settingslib.core.AbstractPreferenceController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: classes.dex */
public class BaseSearchIndexProvider implements Indexable.SearchIndexProvider {
    @Override // com.android.settings.search.Indexable.SearchIndexProvider
    public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean z) {
        return null;
    }

    @Override // com.android.settings.search.Indexable.SearchIndexProvider
    public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean z) {
        return null;
    }

    @Override // com.android.settings.search.Indexable.SearchIndexProvider
    public List<String> getNonIndexableKeys(Context context) {
        if (!isPageSearchEnabled(context)) {
            return getNonIndexableKeysFromXml(context);
        }
        List<AbstractPreferenceController> preferenceControllers = getPreferenceControllers(context);
        if (preferenceControllers != null && !preferenceControllers.isEmpty()) {
            ArrayList arrayList = new ArrayList();
            for (AbstractPreferenceController abstractPreferenceController : preferenceControllers) {
                if (abstractPreferenceController instanceof PreferenceControllerMixin) {
                    ((PreferenceControllerMixin) abstractPreferenceController).updateNonIndexableKeys(arrayList);
                } else if (abstractPreferenceController instanceof BasePreferenceController) {
                    ((BasePreferenceController) abstractPreferenceController).updateNonIndexableKeys(arrayList);
                } else {
                    Log.e("BaseSearchIndex", abstractPreferenceController.getClass().getName() + " must implement " + PreferenceControllerMixin.class.getName() + " treating the key non-indexable");
                    arrayList.add(abstractPreferenceController.getPreferenceKey());
                }
            }
            return arrayList;
        }
        return new ArrayList();
    }

    public List<AbstractPreferenceController> getPreferenceControllers(Context context) {
        List<AbstractPreferenceController> createPreferenceControllers = createPreferenceControllers(context);
        List<SearchIndexableResource> xmlResourcesToIndex = getXmlResourcesToIndex(context, true);
        if (xmlResourcesToIndex == null || xmlResourcesToIndex.isEmpty()) {
            return createPreferenceControllers;
        }
        ArrayList arrayList = new ArrayList();
        for (SearchIndexableResource searchIndexableResource : xmlResourcesToIndex) {
            arrayList.addAll(PreferenceControllerListHelper.getPreferenceControllersFromXml(context, searchIndexableResource.xmlResId));
        }
        List<BasePreferenceController> filterControllers = PreferenceControllerListHelper.filterControllers(arrayList, createPreferenceControllers);
        ArrayList arrayList2 = new ArrayList();
        if (createPreferenceControllers != null) {
            arrayList2.addAll(createPreferenceControllers);
        }
        arrayList2.addAll(filterControllers);
        return arrayList2;
    }

    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return null;
    }

    protected boolean isPageSearchEnabled(Context context) {
        return true;
    }

    private List<String> getNonIndexableKeysFromXml(Context context) {
        List<SearchIndexableResource> xmlResourcesToIndex = getXmlResourcesToIndex(context, true);
        if (xmlResourcesToIndex == null || xmlResourcesToIndex.isEmpty()) {
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList();
        for (SearchIndexableResource searchIndexableResource : xmlResourcesToIndex) {
            arrayList.addAll(getNonIndexableKeysFromXml(context, searchIndexableResource.xmlResId));
        }
        return arrayList;
    }

    public List<String> getNonIndexableKeysFromXml(Context context, int i) {
        ArrayList arrayList = new ArrayList();
        XmlResourceParser xml = context.getResources().getXml(i);
        AttributeSet asAttributeSet = Xml.asAttributeSet(xml);
        while (xml.next() != 1) {
            try {
                String dataKey = PreferenceXmlParserUtils.getDataKey(context, asAttributeSet);
                if (!TextUtils.isEmpty(dataKey)) {
                    arrayList.add(dataKey);
                }
            } catch (IOException | XmlPullParserException e) {
                Log.w("BaseSearchIndex", "Error parsing non-indexable from xml " + i);
            }
        }
        return arrayList;
    }
}
