package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
@GwtCompatible
/* loaded from: a.zip:com/google/common/collect/BoundType.class */
public enum BoundType {
    OPEN { // from class: com.google.common.collect.BoundType.1
    },
    CLOSED { // from class: com.google.common.collect.BoundType.2
    };

    /* synthetic */ BoundType(BoundType boundType) {
        this();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static BoundType forBoolean(boolean z) {
        return z ? CLOSED : OPEN;
    }

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static BoundType[] valuesCustom() {
        return values();
    }
}
