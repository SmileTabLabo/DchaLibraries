package com.mediatek.browser.ext;

import android.content.Context;
import com.mediatek.common.util.OperatorCustomizationFactoryLoader;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class OpBrowserCustomizationFactoryBase {
    private static final List<OperatorCustomizationFactoryLoader.OperatorFactoryInfo> sOperatorFactoryInfoList = new ArrayList();
    static OpBrowserCustomizationFactoryBase sFactory = null;

    static {
        sOperatorFactoryInfoList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP02Browser.apk", "com.mediatek.browser.plugin.Op02BrowserCustomizationFactory", "com.mediatek.browser.plugin", "OP02"));
        sOperatorFactoryInfoList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP03Browser.apk", "com.mediatek.browser.plugin.Op03BrowserCustomizationFactory", "com.mediatek.browser.plugin", "OP03", "SEGDEFAULT", "SPEC0200"));
        sOperatorFactoryInfoList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP07Browser.apk", "com.mediatek.op07.browser.Op07BrowserCustomizationFactory", "com.mediatek.op07.browser", "OP07", "SEGDEFAULT", "SPEC0407"));
    }

    public static synchronized OpBrowserCustomizationFactoryBase getOpFactory(Context context) {
        OpBrowserCustomizationFactoryBase opBrowserCustomizationFactoryBase;
        synchronized (OpBrowserCustomizationFactoryBase.class) {
            if (sFactory == null) {
                sFactory = (OpBrowserCustomizationFactoryBase) OperatorCustomizationFactoryLoader.loadFactory(context, sOperatorFactoryInfoList);
                if (sFactory == null) {
                    sFactory = new OpBrowserCustomizationFactoryBase();
                }
            }
            opBrowserCustomizationFactoryBase = sFactory;
        }
        return opBrowserCustomizationFactoryBase;
    }

    public static synchronized void resetOpFactory() {
        synchronized (OpBrowserCustomizationFactoryBase.class) {
            sFactory = null;
        }
    }

    public IBrowserBookmarkExt makeBrowserBookmarkExt() {
        return new DefaultBrowserBookmarkExt();
    }

    public IBrowserDownloadExt makeBrowserDownloadExt() {
        return new DefaultBrowserDownloadExt();
    }

    public IBrowserHistoryExt makeBrowserHistoryExt() {
        return new DefaultBrowserHistoryExt();
    }

    public IBrowserMiscExt makeBrowserMiscExt() {
        return new DefaultBrowserMiscExt();
    }

    public IBrowserRegionalPhoneExt makeBrowserRegionalPhoneExt() {
        return new DefaultBrowserRegionalPhoneExt();
    }

    public IBrowserSettingExt makeBrowserSettingExt() {
        return new DefaultBrowserSettingExt();
    }

    public IBrowserSiteNavigationExt makeBrowserSiteNavigationExt() {
        return new DefaultBrowserSiteNavigationExt();
    }

    public IBrowserUrlExt makeBrowserUrlExt() {
        return new DefaultBrowserUrlExt();
    }

    public INetworkStateHandlerExt makeNetworkStateHandlerExt() {
        return new DefaultNetworkStateHandlerExt();
    }
}
