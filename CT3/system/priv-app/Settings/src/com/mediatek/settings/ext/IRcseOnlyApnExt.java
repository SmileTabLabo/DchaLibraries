package com.mediatek.settings.ext;
/* loaded from: classes.dex */
public interface IRcseOnlyApnExt {

    /* loaded from: classes.dex */
    public interface OnRcseOnlyApnStateChangedListener {
        void OnRcseOnlyApnStateChanged();
    }

    boolean isRcseOnlyApnEnabled(String str);

    void onCreate(OnRcseOnlyApnStateChangedListener onRcseOnlyApnStateChangedListener, int i);

    void onDestory();
}
