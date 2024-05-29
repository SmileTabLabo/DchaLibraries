package com.mediatek.systemui.ext;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
/* loaded from: a.zip:com/mediatek/systemui/ext/DefaultStatusBarPlmnPlugin.class */
public class DefaultStatusBarPlmnPlugin extends ContextWrapper implements IStatusBarPlmnPlugin {
    private static final String TAG = "DefaultStatusBarPlmnPlugin";

    public DefaultStatusBarPlmnPlugin(Context context) {
        super(context);
    }

    @Override // com.mediatek.systemui.ext.IStatusBarPlmnPlugin
    public void addPlmn(LinearLayout linearLayout, Context context) {
        Log.d(TAG, "into addPlmn");
    }

    @Override // com.mediatek.systemui.ext.IStatusBarPlmnPlugin
    public View customizeCarrierLabel(ViewGroup viewGroup, View view) {
        Log.d(TAG, "into customizeCarrierLabel: null");
        return null;
    }

    @Override // com.mediatek.systemui.ext.IStatusBarPlmnPlugin
    public void setPlmnVisibility(int i) {
        Log.d(TAG, "setPlmnVisibility");
    }

    @Override // com.mediatek.systemui.ext.IStatusBarPlmnPlugin
    public boolean supportCustomizeCarrierLabel() {
        Log.d(TAG, "into supportCustomizeCarrierLabel: false");
        return false;
    }

    @Override // com.mediatek.systemui.ext.IStatusBarPlmnPlugin
    public void updateCarrierLabel(int i, boolean z, boolean z2, String[] strArr) {
        Log.d(TAG, "into updateCarrierLabel, slotId=" + i + ", isSimInserted=" + z + ", isHasSimService=" + z2);
    }

    @Override // com.mediatek.systemui.ext.IStatusBarPlmnPlugin
    public void updateCarrierLabelVisibility(boolean z, boolean z2) {
        Log.d(TAG, "into updateCarrierLabelVisibility");
    }
}
