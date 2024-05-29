package com.google.common.collect;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.MoreObjects;
import com.google.common.collect.MapMaker;
/* JADX INFO: Access modifiers changed from: package-private */
@Beta
@GwtCompatible(emulated = true)
@Deprecated
/* loaded from: a.zip:com/google/common/collect/GenericMapMaker.class */
public abstract class GenericMapMaker<K0, V0> {
    @GwtIncompatible("To be supported")
    MapMaker.RemovalListener<K0, V0> removalListener;

    /* JADX INFO: Access modifiers changed from: package-private */
    @GwtIncompatible("To be supported")
    /* loaded from: a.zip:com/google/common/collect/GenericMapMaker$NullListener.class */
    public enum NullListener implements MapMaker.RemovalListener<Object, Object> {
        INSTANCE;

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static NullListener[] valuesCustom() {
            return values();
        }

        @Override // com.google.common.collect.MapMaker.RemovalListener
        public void onRemoval(MapMaker.RemovalNotification<Object, Object> removalNotification) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @GwtIncompatible("To be supported")
    public <K extends K0, V extends V0> MapMaker.RemovalListener<K, V> getRemovalListener() {
        return (MapMaker.RemovalListener) MoreObjects.firstNonNull(this.removalListener, NullListener.INSTANCE);
    }
}
