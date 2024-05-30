package com.google.protobuf.nano;

import java.nio.charset.Charset;
/* loaded from: classes.dex */
public final class InternalNano {
    protected static final Charset UTF_8 = Charset.forName("UTF-8");
    protected static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    public static final Object LAZY_INIT_LOCK = new Object();

    public static void cloneUnknownFieldData(ExtendableMessageNano extendableMessageNano, ExtendableMessageNano extendableMessageNano2) {
        if (extendableMessageNano.unknownFieldData != null) {
            extendableMessageNano2.unknownFieldData = extendableMessageNano.unknownFieldData.m27clone();
        }
    }
}
