package com.android.browser;

import android.content.Context;
import com.mediatek.browser.ext.DefaultBrowserBookmarkExt;
import com.mediatek.browser.ext.DefaultBrowserDownloadExt;
import com.mediatek.browser.ext.DefaultBrowserHistoryExt;
import com.mediatek.browser.ext.DefaultBrowserMiscExt;
import com.mediatek.browser.ext.DefaultBrowserRegionalPhoneExt;
import com.mediatek.browser.ext.DefaultBrowserSettingExt;
import com.mediatek.browser.ext.DefaultBrowserSiteNavigationExt;
import com.mediatek.browser.ext.DefaultBrowserUrlExt;
import com.mediatek.browser.ext.DefaultNetworkStateHandlerExt;
import com.mediatek.browser.ext.IBrowserBookmarkExt;
import com.mediatek.browser.ext.IBrowserDownloadExt;
import com.mediatek.browser.ext.IBrowserHistoryExt;
import com.mediatek.browser.ext.IBrowserMiscExt;
import com.mediatek.browser.ext.IBrowserRegionalPhoneExt;
import com.mediatek.browser.ext.IBrowserSettingExt;
import com.mediatek.browser.ext.IBrowserSiteNavigationExt;
import com.mediatek.browser.ext.IBrowserUrlExt;
import com.mediatek.browser.ext.INetworkStateHandlerExt;
import com.mediatek.common.MPlugin;
/* loaded from: b.zip:com/android/browser/Extensions.class */
public class Extensions {
    private static volatile IBrowserBookmarkExt sBookmarkPlugin = null;
    private static volatile IBrowserDownloadExt sDownloadPlugin = null;
    private static volatile IBrowserHistoryExt sHistoryPlugin = null;
    private static volatile IBrowserMiscExt sMiscPlugin = null;
    private static volatile IBrowserRegionalPhoneExt sRegionalPhonePlugin = null;
    private static volatile IBrowserSettingExt sSettingPlugin = null;
    private static volatile IBrowserSiteNavigationExt sSiteNavigationPlugin = null;
    private static volatile IBrowserUrlExt sUrlPlugin = null;
    private static volatile INetworkStateHandlerExt sNetworkPlugin = null;

    private Extensions() {
    }

    public static IBrowserBookmarkExt getBookmarkPlugin(Context context) {
        if (sBookmarkPlugin == null) {
            synchronized (Extensions.class) {
                try {
                    if (sBookmarkPlugin == null) {
                        sBookmarkPlugin = (IBrowserBookmarkExt) MPlugin.createInstance(IBrowserBookmarkExt.class.getName(), context);
                        if (sBookmarkPlugin == null) {
                            sBookmarkPlugin = new DefaultBrowserBookmarkExt();
                        }
                    }
                } finally {
                }
            }
        }
        return sBookmarkPlugin;
    }

    public static IBrowserDownloadExt getDownloadPlugin(Context context) {
        if (sDownloadPlugin == null) {
            synchronized (Extensions.class) {
                try {
                    if (sDownloadPlugin == null) {
                        sDownloadPlugin = (IBrowserDownloadExt) MPlugin.createInstance(IBrowserDownloadExt.class.getName(), context);
                        if (sDownloadPlugin == null) {
                            sDownloadPlugin = new DefaultBrowserDownloadExt();
                        }
                    }
                } finally {
                }
            }
        }
        return sDownloadPlugin;
    }

    public static IBrowserHistoryExt getHistoryPlugin(Context context) {
        if (sHistoryPlugin == null) {
            synchronized (Extensions.class) {
                try {
                    if (sHistoryPlugin == null) {
                        sHistoryPlugin = (IBrowserHistoryExt) MPlugin.createInstance(IBrowserHistoryExt.class.getName(), context);
                        if (sHistoryPlugin == null) {
                            sHistoryPlugin = new DefaultBrowserHistoryExt();
                        }
                    }
                } finally {
                }
            }
        }
        return sHistoryPlugin;
    }

    public static IBrowserMiscExt getMiscPlugin(Context context) {
        if (sMiscPlugin == null) {
            synchronized (Extensions.class) {
                try {
                    if (sMiscPlugin == null) {
                        sMiscPlugin = (IBrowserMiscExt) MPlugin.createInstance(IBrowserMiscExt.class.getName(), context);
                        if (sMiscPlugin == null) {
                            sMiscPlugin = new DefaultBrowserMiscExt();
                        }
                    }
                } finally {
                }
            }
        }
        return sMiscPlugin;
    }

    public static INetworkStateHandlerExt getNetworkStateHandlerPlugin(Context context) {
        if (sNetworkPlugin == null) {
            synchronized (Extensions.class) {
                try {
                    if (sNetworkPlugin == null) {
                        sNetworkPlugin = (INetworkStateHandlerExt) MPlugin.createInstance(INetworkStateHandlerExt.class.getName(), context);
                        if (sNetworkPlugin == null) {
                            sNetworkPlugin = new DefaultNetworkStateHandlerExt();
                        }
                    }
                } finally {
                }
            }
        }
        return sNetworkPlugin;
    }

    public static IBrowserRegionalPhoneExt getRegionalPhonePlugin(Context context) {
        if (sRegionalPhonePlugin == null) {
            synchronized (Extensions.class) {
                try {
                    if (sRegionalPhonePlugin == null) {
                        sRegionalPhonePlugin = (IBrowserRegionalPhoneExt) MPlugin.createInstance(IBrowserRegionalPhoneExt.class.getName(), context);
                        if (sRegionalPhonePlugin == null) {
                            sRegionalPhonePlugin = new DefaultBrowserRegionalPhoneExt();
                        }
                    }
                } finally {
                }
            }
        }
        return sRegionalPhonePlugin;
    }

    public static IBrowserSettingExt getSettingPlugin(Context context) {
        if (sSettingPlugin == null) {
            synchronized (Extensions.class) {
                try {
                    if (sSettingPlugin == null) {
                        sSettingPlugin = (IBrowserSettingExt) MPlugin.createInstance(IBrowserSettingExt.class.getName(), context);
                        if (sSettingPlugin == null) {
                            sSettingPlugin = new DefaultBrowserSettingExt();
                        }
                    }
                } finally {
                }
            }
        }
        return sSettingPlugin;
    }

    public static IBrowserSiteNavigationExt getSiteNavigationPlugin(Context context) {
        if (sSiteNavigationPlugin == null) {
            synchronized (Extensions.class) {
                try {
                    if (sSiteNavigationPlugin == null) {
                        sSiteNavigationPlugin = (IBrowserSiteNavigationExt) MPlugin.createInstance(IBrowserSiteNavigationExt.class.getName(), context);
                        if (sSiteNavigationPlugin == null) {
                            sSiteNavigationPlugin = new DefaultBrowserSiteNavigationExt();
                        }
                    }
                } finally {
                }
            }
        }
        return sSiteNavigationPlugin;
    }

    public static IBrowserUrlExt getUrlPlugin(Context context) {
        if (sUrlPlugin == null) {
            synchronized (Extensions.class) {
                try {
                    if (sUrlPlugin == null) {
                        sUrlPlugin = (IBrowserUrlExt) MPlugin.createInstance(IBrowserUrlExt.class.getName(), context);
                        if (sUrlPlugin == null) {
                            sUrlPlugin = new DefaultBrowserUrlExt();
                        }
                    }
                } finally {
                }
            }
        }
        return sUrlPlugin;
    }
}
