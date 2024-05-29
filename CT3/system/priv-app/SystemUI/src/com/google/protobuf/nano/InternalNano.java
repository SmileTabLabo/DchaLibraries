package com.google.protobuf.nano;
/* loaded from: a.zip:com/google/protobuf/nano/InternalNano.class */
public final class InternalNano {
    public static final Object LAZY_INIT_LOCK = new Object();

    private InternalNano() {
    }

    public static void cloneUnknownFieldData(ExtendableMessageNano extendableMessageNano, ExtendableMessageNano extendableMessageNano2) {
        if (extendableMessageNano.unknownFieldData != null) {
            extendableMessageNano2.unknownFieldData = extendableMessageNano.unknownFieldData.m2270clone();
        }
    }
}
