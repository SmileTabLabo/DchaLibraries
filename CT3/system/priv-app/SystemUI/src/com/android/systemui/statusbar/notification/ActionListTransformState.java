package com.android.systemui.statusbar.notification;

import android.util.Pools;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/ActionListTransformState.class */
public class ActionListTransformState extends TransformState {
    private static Pools.SimplePool<ActionListTransformState> sInstancePool = new Pools.SimplePool<>(40);

    public static ActionListTransformState obtain() {
        ActionListTransformState actionListTransformState = (ActionListTransformState) sInstancePool.acquire();
        return actionListTransformState != null ? actionListTransformState : new ActionListTransformState();
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void recycle() {
        super.recycle();
        sInstancePool.release(this);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public void resetTransformedView() {
        float translationY = getTransformedView().getTranslationY();
        super.resetTransformedView();
        getTransformedView().setTranslationY(translationY);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public boolean sameAs(TransformState transformState) {
        return transformState instanceof ActionListTransformState;
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void transformViewFullyFrom(TransformState transformState, float f) {
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void transformViewFullyTo(TransformState transformState, float f) {
    }
}
