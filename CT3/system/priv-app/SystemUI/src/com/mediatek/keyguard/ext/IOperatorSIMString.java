package com.mediatek.keyguard.ext;

import android.content.Context;
/* loaded from: a.zip:com/mediatek/keyguard/ext/IOperatorSIMString.class */
public interface IOperatorSIMString {

    /* loaded from: a.zip:com/mediatek/keyguard/ext/IOperatorSIMString$SIMChangedTag.class */
    public enum SIMChangedTag {
        SIMTOUIM,
        UIMSIM,
        DELSIM;

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static SIMChangedTag[] valuesCustom() {
            return values();
        }
    }

    String getOperatorSIMString(String str, int i, SIMChangedTag sIMChangedTag, Context context);
}
