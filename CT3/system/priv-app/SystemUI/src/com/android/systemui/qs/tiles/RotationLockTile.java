package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.os.BenesseExtension;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.RotationLockController;
/* loaded from: a.zip:com/android/systemui/qs/tiles/RotationLockTile.class */
public class RotationLockTile extends QSTile<QSTile.BooleanState> {
    private final QSTile<QSTile.BooleanState>.AnimationIcon mAutoToLandscape;
    private final QSTile<QSTile.BooleanState>.AnimationIcon mAutoToPortrait;
    private final RotationLockController.RotationLockControllerCallback mCallback;
    private final RotationLockController mController;
    private final QSTile<QSTile.BooleanState>.AnimationIcon mLandscapeToAuto;
    private final QSTile<QSTile.BooleanState>.AnimationIcon mPortraitToAuto;

    public RotationLockTile(QSTile.Host host) {
        super(host);
        this.mPortraitToAuto = new QSTile.AnimationIcon(this, 2130837696, 2130837693);
        this.mAutoToPortrait = new QSTile.AnimationIcon(this, 2130837694, 2130837695);
        this.mLandscapeToAuto = new QSTile.AnimationIcon(this, 2130837677, 2130837674);
        this.mAutoToLandscape = new QSTile.AnimationIcon(this, 2130837675, 2130837676);
        this.mCallback = new RotationLockController.RotationLockControllerCallback(this) { // from class: com.android.systemui.qs.tiles.RotationLockTile.1
            final RotationLockTile this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.statusbar.policy.RotationLockController.RotationLockControllerCallback
            public void onRotationLockStateChanged(boolean z, boolean z2) {
                this.this$0.refreshState(Boolean.valueOf(z));
            }
        };
        this.mController = host.getRotationLockController();
    }

    private String getAccessibilityString(boolean z) {
        if (z) {
            return this.mContext.getString(2131493532) + "," + this.mContext.getString(2131493533, isCurrentOrientationLockPortrait(this.mController, this.mContext) ? this.mContext.getString(2131493535) : this.mContext.getString(2131493536));
        }
        return this.mContext.getString(2131493532);
    }

    public static boolean isCurrentOrientationLockPortrait(RotationLockController rotationLockController, Context context) {
        boolean z = true;
        int rotationLockOrientation = rotationLockController.getRotationLockOrientation();
        if (rotationLockOrientation != 0) {
            return rotationLockOrientation != 2;
        }
        if (context.getResources().getConfiguration().orientation == 2) {
            z = false;
        }
        return z;
    }

    @Override // com.android.systemui.qs.QSTile
    protected String composeChangeAnnouncement() {
        return getAccessibilityString(((QSTile.BooleanState) this.mState).value);
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        return new Intent("android.settings.DISPLAY_SETTINGS");
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 123;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return getState().label;
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        boolean z = false;
        if (this.mController == null) {
            return;
        }
        MetricsLogger.action(this.mContext, getMetricsCategory(), !((QSTile.BooleanState) this.mState).value);
        boolean z2 = !((QSTile.BooleanState) this.mState).value;
        RotationLockController rotationLockController = this.mController;
        if (!z2) {
            z = true;
        }
        rotationLockController.setRotationLocked(z);
        refreshState(Boolean.valueOf(z2));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        if (this.mController == null) {
            return;
        }
        boolean isRotationLocked = this.mController.isRotationLocked();
        booleanState.value = !isRotationLocked;
        boolean isCurrentOrientationLockPortrait = isCurrentOrientationLockPortrait(this.mController, this.mContext);
        if (isRotationLocked) {
            booleanState.label = this.mContext.getString(isCurrentOrientationLockPortrait ? 2131493535 : 2131493536);
            booleanState.icon = isCurrentOrientationLockPortrait ? this.mAutoToPortrait : this.mAutoToLandscape;
        } else {
            booleanState.label = this.mContext.getString(2131493531);
            booleanState.icon = isCurrentOrientationLockPortrait ? this.mPortraitToAuto : this.mLandscapeToAuto;
        }
        booleanState.contentDescription = getAccessibilityString(isRotationLocked);
        String name = Switch.class.getName();
        booleanState.expandedAccessibilityClassName = name;
        booleanState.minimalAccessibilityClassName = name;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (this.mController == null) {
            return;
        }
        if (z) {
            this.mController.addRotationLockControllerCallback(this.mCallback);
        } else {
            this.mController.removeRotationLockControllerCallback(this.mCallback);
        }
    }
}
