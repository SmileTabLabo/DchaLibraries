package android.support.v7.view;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.content.res.ConfigurationHelper;
import android.support.v4.view.ViewConfigurationCompat;
import android.view.ViewConfiguration;
/* loaded from: a.zip:android/support/v7/view/ActionBarPolicy.class */
public class ActionBarPolicy {
    private Context mContext;

    private ActionBarPolicy(Context context) {
        this.mContext = context;
    }

    public static ActionBarPolicy get(Context context) {
        return new ActionBarPolicy(context);
    }

    public int getEmbeddedMenuWidthLimit() {
        return this.mContext.getResources().getDisplayMetrics().widthPixels / 2;
    }

    public int getMaxActionButtons() {
        Resources resources = this.mContext.getResources();
        int screenWidthDp = ConfigurationHelper.getScreenWidthDp(resources);
        int screenHeightDp = ConfigurationHelper.getScreenHeightDp(resources);
        if (ConfigurationHelper.getSmallestScreenWidthDp(resources) > 600 || screenWidthDp > 600) {
            return 5;
        }
        if (screenWidthDp <= 960 || screenHeightDp <= 720) {
            if (screenWidthDp <= 720 || screenHeightDp <= 960) {
                if (screenWidthDp < 500) {
                    if (screenWidthDp <= 640 || screenHeightDp <= 480) {
                        if (screenWidthDp <= 480 || screenHeightDp <= 640) {
                            return screenWidthDp >= 360 ? 3 : 2;
                        }
                        return 4;
                    }
                    return 4;
                }
                return 4;
            }
            return 5;
        }
        return 5;
    }

    public boolean showsOverflowMenuButton() {
        boolean z = true;
        if (Build.VERSION.SDK_INT >= 19) {
            return true;
        }
        if (ViewConfigurationCompat.hasPermanentMenuKey(ViewConfiguration.get(this.mContext))) {
            z = false;
        }
        return z;
    }
}
