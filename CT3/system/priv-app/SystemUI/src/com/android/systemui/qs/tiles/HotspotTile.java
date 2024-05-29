package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BenesseExtension;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.HotspotController;
/* loaded from: a.zip:com/android/systemui/qs/tiles/HotspotTile.class */
public class HotspotTile extends QSTile<QSTile.AirplaneBooleanState> {
    private final GlobalSetting mAirplaneMode;
    private final Callback mCallback;
    private final HotspotController mController;
    private final QSTile<QSTile.AirplaneBooleanState>.AnimationIcon mDisable;
    private final QSTile.Icon mDisableNoAnimation;
    private final QSTile<QSTile.AirplaneBooleanState>.AnimationIcon mEnable;
    private boolean mListening;
    private final QSTile.Icon mUnavailable;

    /* loaded from: a.zip:com/android/systemui/qs/tiles/HotspotTile$Callback.class */
    private final class Callback implements HotspotController.Callback {
        final HotspotTile this$0;

        private Callback(HotspotTile hotspotTile) {
            this.this$0 = hotspotTile;
        }

        /* synthetic */ Callback(HotspotTile hotspotTile, Callback callback) {
            this(hotspotTile);
        }

        @Override // com.android.systemui.statusbar.policy.HotspotController.Callback
        public void onHotspotChanged(boolean z) {
            this.this$0.refreshState(Boolean.valueOf(z));
        }
    }

    public HotspotTile(QSTile.Host host) {
        super(host);
        this.mEnable = new QSTile.AnimationIcon(this, 2130837659, 2130837656);
        this.mDisable = new QSTile.AnimationIcon(this, 2130837657, 2130837658);
        this.mDisableNoAnimation = QSTile.ResourceIcon.get(2130837658);
        this.mUnavailable = QSTile.ResourceIcon.get(2130837660);
        this.mCallback = new Callback(this, null);
        this.mController = host.getHotspotController();
        this.mAirplaneMode = new GlobalSetting(this, this.mContext, this.mHandler, "airplane_mode_on") { // from class: com.android.systemui.qs.tiles.HotspotTile.1
            final HotspotTile this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.qs.GlobalSetting
            protected void handleValueChanged(int i) {
                this.this$0.refreshState();
            }
        };
    }

    @Override // com.android.systemui.qs.QSTile
    protected String composeChangeAnnouncement() {
        return ((QSTile.AirplaneBooleanState) this.mState).value ? this.mContext.getString(2131493489) : this.mContext.getString(2131493488);
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        return new Intent("android.settings.WIRELESS_SETTINGS");
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 120;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(2131493568);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        boolean booleanValue = Boolean.valueOf(((QSTile.AirplaneBooleanState) this.mState).value).booleanValue();
        if (booleanValue || this.mAirplaneMode.getValue() == 0) {
            MetricsLogger.action(this.mContext, getMetricsCategory(), !booleanValue);
            this.mController.setHotspotEnabled(!booleanValue);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleDestroy() {
        super.handleDestroy();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.AirplaneBooleanState airplaneBooleanState, Object obj) {
        airplaneBooleanState.label = this.mContext.getString(2131493568);
        checkIfRestrictionEnforcedByAdminOnly(airplaneBooleanState, "no_config_tethering");
        if (obj instanceof Boolean) {
            airplaneBooleanState.value = ((Boolean) obj).booleanValue();
        } else {
            airplaneBooleanState.value = this.mController.isHotspotEnabled();
        }
        airplaneBooleanState.icon = airplaneBooleanState.value ? this.mEnable : this.mDisable;
        boolean z = airplaneBooleanState.isAirplaneMode;
        airplaneBooleanState.isAirplaneMode = this.mAirplaneMode.getValue() != 0;
        if (airplaneBooleanState.isAirplaneMode) {
            airplaneBooleanState.label = new SpannableStringBuilder().append(airplaneBooleanState.label, new ForegroundColorSpan(this.mHost.getContext().getColor(2131558595)), 18);
            airplaneBooleanState.icon = this.mUnavailable;
        } else if (z) {
            airplaneBooleanState.icon = this.mDisableNoAnimation;
        }
        String name = Switch.class.getName();
        airplaneBooleanState.expandedAccessibilityClassName = name;
        airplaneBooleanState.minimalAccessibilityClassName = name;
        airplaneBooleanState.contentDescription = airplaneBooleanState.label;
    }

    @Override // com.android.systemui.qs.QSTile
    public boolean isAvailable() {
        return this.mController.isHotspotSupported();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.AirplaneBooleanState newTileState() {
        return new QSTile.AirplaneBooleanState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        if (z) {
            this.mController.addCallback(this.mCallback);
            new IntentFilter().addAction("android.intent.action.AIRPLANE_MODE");
            refreshState();
        } else {
            this.mController.removeCallback(this.mCallback);
        }
        this.mAirplaneMode.setListening(z);
    }
}
