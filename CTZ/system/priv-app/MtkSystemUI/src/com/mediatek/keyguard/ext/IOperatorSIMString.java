package com.mediatek.keyguard.ext;

import android.content.Context;
/* loaded from: classes.dex */
public interface IOperatorSIMString {

    /* loaded from: classes.dex */
    public enum SIMChangedTag {
        SIMTOUIM,
        UIMSIM,
        DELSIM
    }

    String getOperatorSIMString(String str, int i, SIMChangedTag sIMChangedTag, Context context);
}
