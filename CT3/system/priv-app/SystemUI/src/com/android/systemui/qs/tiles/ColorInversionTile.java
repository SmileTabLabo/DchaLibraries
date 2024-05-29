package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.os.BenesseExtension;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.SecureSetting;
/* loaded from: a.zip:com/android/systemui/qs/tiles/ColorInversionTile.class */
public class ColorInversionTile extends QSTile<QSTile.BooleanState> {
    private final QSTile<QSTile.BooleanState>.AnimationIcon mDisable;
    private final QSTile<QSTile.BooleanState>.AnimationIcon mEnable;
    private final SecureSetting mSetting;

    public ColorInversionTile(QSTile.Host host) {
        super(host);
        this.mEnable = new QSTile.AnimationIcon(this, 2130837666, 2130837663);
        this.mDisable = new QSTile.AnimationIcon(this, 2130837664, 2130837665);
        this.mSetting = new SecureSetting(this, this.mContext, this.mHandler, "accessibility_display_inversion_enabled") { // from class: com.android.systemui.qs.tiles.ColorInversionTile.1
            final ColorInversionTile this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.qs.SecureSetting
            protected void handleValueChanged(int i, boolean z) {
                this.this$0.handleRefreshState(Integer.valueOf(i));
            }
        };
    }

    @Override // com.android.systemui.qs.QSTile
    protected String composeChangeAnnouncement() {
        return ((QSTile.BooleanState) this.mState).value ? this.mContext.getString(2131493487) : this.mContext.getString(2131493486);
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        return new Intent("android.settings.ACCESSIBILITY_SETTINGS");
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 116;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(2131493561);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        int i = 0;
        MetricsLogger.action(this.mContext, getMetricsCategory(), !((QSTile.BooleanState) this.mState).value);
        SecureSetting secureSetting = this.mSetting;
        if (!((QSTile.BooleanState) this.mState).value) {
            i = 1;
        }
        secureSetting.setValue(i);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleDestroy() {
        super.handleDestroy();
        this.mSetting.setListening(false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = false;
        if ((obj instanceof Integer ? ((Integer) obj).intValue() : this.mSetting.getValue()) != 0) {
            z = true;
        }
        booleanState.value = z;
        booleanState.label = this.mContext.getString(2131493561);
        booleanState.icon = z ? this.mEnable : this.mDisable;
        String name = Switch.class.getName();
        booleanState.expandedAccessibilityClassName = name;
        booleanState.minimalAccessibilityClassName = name;
        booleanState.contentDescription = booleanState.label;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUserSwitch(int i) {
        this.mSetting.setUserId(i);
        handleRefreshState(Integer.valueOf(this.mSetting.getValue()));
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        this.mSetting.setListening(z);
    }
}
