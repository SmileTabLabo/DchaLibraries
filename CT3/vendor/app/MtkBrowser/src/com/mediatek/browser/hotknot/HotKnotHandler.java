package com.mediatek.browser.hotknot;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import com.mediatek.hotknot.HotKnotAdapter;
import com.mediatek.hotknot.HotKnotMessage;
/* loaded from: b.zip:com/mediatek/browser/hotknot/HotKnotHandler.class */
public class HotKnotHandler {
    private static HotKnotAdapter mHotKnotAdapter = null;
    private static Activity mActivity = null;

    public static void hotKnotInit(Activity activity) {
        mActivity = activity;
        mHotKnotAdapter = HotKnotAdapter.getDefaultAdapter(mActivity);
        if (mHotKnotAdapter == null) {
            Log.d("browser/HotKnotHandler", "hotKnotInit fail, hotKnotAdapter is null");
        } else {
            Log.d("browser/HotKnotHandler", "hotKnotInit completed");
        }
    }

    public static void hotKnotStart(String str) {
        Log.d("browser/HotKnotHandler", "hotKnotStart, url:" + str);
        if (mHotKnotAdapter == null) {
            Log.e("browser/HotKnotHandler", "hotKnotStart fail, hotKnotAdapter is null");
        } else if (str == null || str.length() == 0) {
            StringBuilder append = new StringBuilder().append("hotKnotStart fail, url:");
            String str2 = str;
            if (str == null) {
                str2 = "url";
            }
            Log.e("browser/HotKnotHandler", append.append(str2).toString());
        } else {
            Parcelable hotKnotMessage = new HotKnotMessage("com.mediatek.browser.hotknot/com.mediatek.browser.hotknot.MIME_TYPE", str.getBytes());
            Intent intent = new Intent("com.mediatek.hotknot.action.SHARE");
            intent.putExtra("com.mediatek.hotknot.extra.SHARE_MSG", hotKnotMessage);
            intent.addFlags(134742016);
            mActivity.startActivity(intent);
        }
    }

    public static boolean isHotKnotSupported() {
        return mHotKnotAdapter != null;
    }
}
