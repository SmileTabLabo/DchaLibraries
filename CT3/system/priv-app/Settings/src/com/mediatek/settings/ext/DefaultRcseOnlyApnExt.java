package com.mediatek.settings.ext;

import com.mediatek.settings.ext.IRcseOnlyApnExt;
/* loaded from: classes.dex */
public class DefaultRcseOnlyApnExt implements IRcseOnlyApnExt {
    @Override // com.mediatek.settings.ext.IRcseOnlyApnExt
    public boolean isRcseOnlyApnEnabled(String type) {
        return true;
    }

    @Override // com.mediatek.settings.ext.IRcseOnlyApnExt
    public void onCreate(IRcseOnlyApnExt.OnRcseOnlyApnStateChangedListener listener, int subId) {
    }

    @Override // com.mediatek.settings.ext.IRcseOnlyApnExt
    public void onDestory() {
    }
}
