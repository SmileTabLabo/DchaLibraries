package com.android.systemui.statusbar.policy;

import android.content.Context;
import com.android.internal.view.RotationPolicy;
import com.android.systemui.statusbar.policy.RotationLockController;
import java.util.concurrent.CopyOnWriteArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/RotationLockControllerImpl.class */
public final class RotationLockControllerImpl implements RotationLockController {
    private final Context mContext;
    private final CopyOnWriteArrayList<RotationLockController.RotationLockControllerCallback> mCallbacks = new CopyOnWriteArrayList<>();
    private final RotationPolicy.RotationPolicyListener mRotationPolicyListener = new RotationPolicy.RotationPolicyListener(this) { // from class: com.android.systemui.statusbar.policy.RotationLockControllerImpl.1
        final RotationLockControllerImpl this$0;

        {
            this.this$0 = this;
        }

        public void onChange() {
            this.this$0.notifyChanged();
        }
    };

    public RotationLockControllerImpl(Context context) {
        this.mContext = context;
        setListening(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyChanged() {
        for (RotationLockController.RotationLockControllerCallback rotationLockControllerCallback : this.mCallbacks) {
            notifyChanged(rotationLockControllerCallback);
        }
    }

    private void notifyChanged(RotationLockController.RotationLockControllerCallback rotationLockControllerCallback) {
        rotationLockControllerCallback.onRotationLockStateChanged(RotationPolicy.isRotationLocked(this.mContext), RotationPolicy.isRotationLockToggleVisible(this.mContext));
    }

    @Override // com.android.systemui.statusbar.policy.RotationLockController
    public void addRotationLockControllerCallback(RotationLockController.RotationLockControllerCallback rotationLockControllerCallback) {
        this.mCallbacks.add(rotationLockControllerCallback);
        notifyChanged(rotationLockControllerCallback);
    }

    @Override // com.android.systemui.statusbar.policy.RotationLockController
    public int getRotationLockOrientation() {
        return RotationPolicy.getRotationLockOrientation(this.mContext);
    }

    @Override // com.android.systemui.statusbar.policy.RotationLockController
    public boolean isRotationLocked() {
        return RotationPolicy.isRotationLocked(this.mContext);
    }

    @Override // com.android.systemui.statusbar.policy.RotationLockController
    public void removeRotationLockControllerCallback(RotationLockController.RotationLockControllerCallback rotationLockControllerCallback) {
        this.mCallbacks.remove(rotationLockControllerCallback);
    }

    @Override // com.android.systemui.statusbar.policy.Listenable
    public void setListening(boolean z) {
        if (z) {
            RotationPolicy.registerRotationPolicyListener(this.mContext, this.mRotationPolicyListener, -1);
        } else {
            RotationPolicy.unregisterRotationPolicyListener(this.mContext, this.mRotationPolicyListener);
        }
    }

    @Override // com.android.systemui.statusbar.policy.RotationLockController
    public void setRotationLocked(boolean z) {
        RotationPolicy.setRotationLock(this.mContext, z);
    }
}
