package com.android.systemui.statusbar.notification;

import android.util.Pools;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/ProgressTransformState.class */
public class ProgressTransformState extends TransformState {
    private static Pools.SimplePool<ProgressTransformState> sInstancePool = new Pools.SimplePool<>(40);

    public static ProgressTransformState obtain() {
        ProgressTransformState progressTransformState = (ProgressTransformState) sInstancePool.acquire();
        return progressTransformState != null ? progressTransformState : new ProgressTransformState();
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void recycle() {
        super.recycle();
        sInstancePool.release(this);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public boolean sameAs(TransformState transformState) {
        if (transformState instanceof ProgressTransformState) {
            return true;
        }
        return super.sameAs(transformState);
    }
}
