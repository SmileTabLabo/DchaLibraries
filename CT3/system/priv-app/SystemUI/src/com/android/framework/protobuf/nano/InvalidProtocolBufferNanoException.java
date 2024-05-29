package com.android.framework.protobuf.nano;

import java.io.IOException;
/* loaded from: a.zip:com/android/framework/protobuf/nano/InvalidProtocolBufferNanoException.class */
public class InvalidProtocolBufferNanoException extends IOException {
    private static final long serialVersionUID = -1616151763072450476L;

    public InvalidProtocolBufferNanoException(String str) {
        super(str);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static InvalidProtocolBufferNanoException invalidEndTag() {
        return new InvalidProtocolBufferNanoException("Protocol message end-group tag did not match expected tag.");
    }
}
