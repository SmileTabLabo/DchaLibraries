package com.google.protobuf.nano;

import com.google.protobuf.nano.ExtendableMessageNano;
/* loaded from: classes.dex */
public abstract class ExtendableMessageNano<M extends ExtendableMessageNano<M>> extends MessageNano {
    protected FieldArray unknownFieldData;

    @Override // com.google.protobuf.nano.MessageNano
    /* renamed from: clone */
    public M mo26clone() throws CloneNotSupportedException {
        M m = (M) super.mo26clone();
        InternalNano.cloneUnknownFieldData(this, m);
        return m;
    }
}
