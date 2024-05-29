package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.FlashlightController;
/* loaded from: a.zip:com/android/systemui/qs/tiles/FlashlightTile.class */
public class FlashlightTile extends QSTile<QSTile.BooleanState> implements FlashlightController.FlashlightListener {
    private final QSTile<QSTile.BooleanState>.AnimationIcon mDisable;
    private final QSTile<QSTile.BooleanState>.AnimationIcon mEnable;
    private final FlashlightController mFlashlightController;

    public FlashlightTile(QSTile.Host host) {
        super(host);
        this.mEnable = new QSTile.AnimationIcon(this, 2130837783, 2130837780);
        this.mDisable = new QSTile.AnimationIcon(this, 2130837781, 2130837782);
        this.mFlashlightController = host.getFlashlightController();
        this.mFlashlightController.addListener(this);
    }

    @Override // com.android.systemui.qs.QSTile
    protected String composeChangeAnnouncement() {
        return ((QSTile.BooleanState) this.mState).value ? this.mContext.getString(2131493485) : this.mContext.getString(2131493484);
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        return new Intent("android.media.action.STILL_IMAGE_CAMERA");
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 119;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(2131493570);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        if (ActivityManager.isUserAMonkey()) {
            return;
        }
        MetricsLogger.action(this.mContext, getMetricsCategory(), !((QSTile.BooleanState) this.mState).value);
        boolean z = !((QSTile.BooleanState) this.mState).value;
        refreshState(Boolean.valueOf(z));
        this.mFlashlightController.setFlashlight(z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleDestroy() {
        super.handleDestroy();
        this.mFlashlightController.removeListener(this);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleLongClick() {
        handleClick();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.label = this.mHost.getContext().getString(2131493570);
        if (!this.mFlashlightController.isAvailable()) {
            Drawable mutate = this.mHost.getContext().getDrawable(2130837780).mutate();
            int color = this.mHost.getContext().getColor(2131558595);
            mutate.setTint(color);
            booleanState.icon = new QSTile.DrawableIcon(mutate);
            booleanState.label = new SpannableStringBuilder().append(booleanState.label, new ForegroundColorSpan(color), 18);
            booleanState.contentDescription = this.mContext.getString(2131493482);
            return;
        }
        if (obj instanceof Boolean) {
            boolean booleanValue = ((Boolean) obj).booleanValue();
            if (booleanValue == booleanState.value) {
                return;
            }
            booleanState.value = booleanValue;
        } else {
            booleanState.value = this.mFlashlightController.isEnabled();
        }
        booleanState.icon = booleanState.value ? this.mEnable : this.mDisable;
        booleanState.contentDescription = this.mContext.getString(2131493570);
        String name = Switch.class.getName();
        booleanState.expandedAccessibilityClassName = name;
        booleanState.minimalAccessibilityClassName = name;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUserSwitch(int i) {
    }

    @Override // com.android.systemui.qs.QSTile
    public boolean isAvailable() {
        return this.mFlashlightController.hasFlashlight();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.statusbar.policy.FlashlightController.FlashlightListener
    public void onFlashlightAvailabilityChanged(boolean z) {
        refreshState();
    }

    @Override // com.android.systemui.statusbar.policy.FlashlightController.FlashlightListener
    public void onFlashlightChanged(boolean z) {
        refreshState(Boolean.valueOf(z));
    }

    @Override // com.android.systemui.statusbar.policy.FlashlightController.FlashlightListener
    public void onFlashlightError() {
        refreshState(false);
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
    }
}
