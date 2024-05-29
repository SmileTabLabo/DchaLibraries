package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.os.BenesseExtension;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
/* loaded from: a.zip:com/android/systemui/qs/tiles/LocationTile.class */
public class LocationTile extends QSTile<QSTile.BooleanState> {
    private final Callback mCallback;
    private final LocationController mController;
    private final QSTile<QSTile.BooleanState>.AnimationIcon mDisable;
    private final QSTile<QSTile.BooleanState>.AnimationIcon mEnable;
    private final KeyguardMonitor mKeyguard;

    /* loaded from: a.zip:com/android/systemui/qs/tiles/LocationTile$Callback.class */
    private final class Callback implements LocationController.LocationSettingsChangeCallback, KeyguardMonitor.Callback {
        final LocationTile this$0;

        private Callback(LocationTile locationTile) {
            this.this$0 = locationTile;
        }

        /* synthetic */ Callback(LocationTile locationTile, Callback callback) {
            this(locationTile);
        }

        @Override // com.android.systemui.statusbar.policy.KeyguardMonitor.Callback
        public void onKeyguardChanged() {
            this.this$0.refreshState();
        }

        @Override // com.android.systemui.statusbar.policy.LocationController.LocationSettingsChangeCallback
        public void onLocationSettingsChanged(boolean z) {
            this.this$0.refreshState();
        }
    }

    public LocationTile(QSTile.Host host) {
        super(host);
        this.mEnable = new QSTile.AnimationIcon(this, 2130837791, 2130837788);
        this.mDisable = new QSTile.AnimationIcon(this, 2130837789, 2130837790);
        this.mCallback = new Callback(this, null);
        this.mController = host.getLocationController();
        this.mKeyguard = host.getKeyguardMonitor();
    }

    @Override // com.android.systemui.qs.QSTile
    protected String composeChangeAnnouncement() {
        return ((QSTile.BooleanState) this.mState).value ? this.mContext.getString(2131493476) : this.mContext.getString(2131493475);
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        return new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 122;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(2131493538);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        if (this.mKeyguard.isSecure() && this.mKeyguard.isShowing()) {
            this.mHost.startRunnableDismissingKeyguard(new Runnable(this) { // from class: com.android.systemui.qs.tiles.LocationTile.1
                final LocationTile this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    boolean booleanValue = Boolean.valueOf(((QSTile.BooleanState) this.this$0.mState).value).booleanValue();
                    this.this$0.mHost.openPanels();
                    MetricsLogger.action(this.this$0.mContext, this.this$0.getMetricsCategory(), !booleanValue);
                    this.this$0.mController.setLocationEnabled(!booleanValue);
                }
            });
            return;
        }
        boolean booleanValue = Boolean.valueOf(((QSTile.BooleanState) this.mState).value).booleanValue();
        MetricsLogger.action(this.mContext, getMetricsCategory(), !booleanValue);
        this.mController.setLocationEnabled(!booleanValue);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean isLocationEnabled = this.mController.isLocationEnabled();
        booleanState.value = isLocationEnabled;
        checkIfRestrictionEnforcedByAdminOnly(booleanState, "no_share_location");
        if (isLocationEnabled) {
            booleanState.icon = this.mEnable;
            booleanState.label = this.mContext.getString(2131493538);
            booleanState.contentDescription = this.mContext.getString(2131493474);
        } else {
            booleanState.icon = this.mDisable;
            booleanState.label = this.mContext.getString(2131493538);
            booleanState.contentDescription = this.mContext.getString(2131493473);
        }
        String name = Switch.class.getName();
        booleanState.expandedAccessibilityClassName = name;
        booleanState.minimalAccessibilityClassName = name;
    }

    @Override // com.android.systemui.qs.QSTile
    public boolean isAvailable() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.location");
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (z) {
            this.mController.addSettingsChangedCallback(this.mCallback);
            this.mKeyguard.addCallback(this.mCallback);
            return;
        }
        this.mController.removeSettingsChangedCallback(this.mCallback);
        this.mKeyguard.removeCallback(this.mCallback);
    }
}
