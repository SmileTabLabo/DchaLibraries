package com.android.systemui.qs;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.RestrictedLockUtils;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.qs.external.TileServices;
import com.android.systemui.statusbar.phone.ManagedProfileController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NightModeController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.mediatek.systemui.statusbar.policy.HotKnotController;
import java.util.ArrayList;
import java.util.Objects;
/* loaded from: a.zip:com/android/systemui/qs/QSTile.class */
public abstract class QSTile<TState extends State> {
    protected static final boolean DEBUG = Log.isLoggable("Tile", 3);
    private boolean mAnnounceNextStateChange;
    protected final Context mContext;
    protected final QSTile<TState>.H mHandler;
    protected final Host mHost;
    private String mTileSpec;
    protected final String TAG = "Tile." + getClass().getSimpleName();
    protected final Handler mUiHandler = new Handler(Looper.getMainLooper());
    private final ArraySet<Object> mListeners = new ArraySet<>();
    private final ArrayList<Callback> mCallbacks = new ArrayList<>();
    protected TState mState = newTileState();
    private TState mTmpState = newTileState();

    /* loaded from: a.zip:com/android/systemui/qs/QSTile$AirplaneBooleanState.class */
    public static class AirplaneBooleanState extends BooleanState {
        public boolean isAirplaneMode;

        @Override // com.android.systemui.qs.QSTile.BooleanState, com.android.systemui.qs.QSTile.State
        public boolean copyTo(State state) {
            AirplaneBooleanState airplaneBooleanState = (AirplaneBooleanState) state;
            boolean z = super.copyTo(state) || airplaneBooleanState.isAirplaneMode != this.isAirplaneMode;
            airplaneBooleanState.isAirplaneMode = this.isAirplaneMode;
            return z;
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSTile$AnimationIcon.class */
    protected class AnimationIcon extends ResourceIcon {
        private final int mAnimatedResId;
        final QSTile this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public AnimationIcon(QSTile qSTile, int i, int i2) {
            super(i2, null);
            this.this$0 = qSTile;
            this.mAnimatedResId = i;
        }

        @Override // com.android.systemui.qs.QSTile.ResourceIcon, com.android.systemui.qs.QSTile.Icon
        public Drawable getDrawable(Context context) {
            return context.getDrawable(this.mAnimatedResId).getConstantState().newDrawable();
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSTile$BooleanState.class */
    public static class BooleanState extends State {
        public boolean value;

        @Override // com.android.systemui.qs.QSTile.State
        public boolean copyTo(State state) {
            BooleanState booleanState = (BooleanState) state;
            boolean z = super.copyTo(state) || booleanState.value != this.value;
            booleanState.value = this.value;
            return z;
        }

        @Override // com.android.systemui.qs.QSTile.State
        protected StringBuilder toStringBuilder() {
            StringBuilder stringBuilder = super.toStringBuilder();
            stringBuilder.insert(stringBuilder.length() - 1, ",value=" + this.value);
            return stringBuilder;
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSTile$Callback.class */
    public interface Callback {
        void onAnnouncementRequested(CharSequence charSequence);

        void onScanStateChanged(boolean z);

        void onShowDetail(boolean z);

        void onStateChanged(State state);

        void onToggleStateChanged(boolean z);
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSTile$DetailAdapter.class */
    public interface DetailAdapter {
        View createDetailView(Context context, View view, ViewGroup viewGroup);

        int getMetricsCategory();

        Intent getSettingsIntent();

        CharSequence getTitle();

        Boolean getToggleState();

        void setToggleState(boolean z);
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSTile$DrawableIcon.class */
    public static class DrawableIcon extends Icon {
        protected final Drawable mDrawable;

        public DrawableIcon(Drawable drawable) {
            this.mDrawable = drawable;
        }

        @Override // com.android.systemui.qs.QSTile.Icon
        public Drawable getDrawable(Context context) {
            return this.mDrawable;
        }

        @Override // com.android.systemui.qs.QSTile.Icon
        public Drawable getInvisibleDrawable(Context context) {
            return this.mDrawable;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: a.zip:com/android/systemui/qs/QSTile$H.class */
    public final class H extends Handler {
        final QSTile this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        private H(QSTile qSTile, Looper looper) {
            super(looper);
            this.this$0 = qSTile;
        }

        /* synthetic */ H(QSTile qSTile, Looper looper, H h) {
            this(qSTile, looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            boolean z = true;
            try {
                if (message.what == 1) {
                    this.this$0.handleAddCallback((Callback) message.obj);
                } else if (message.what == 12) {
                    this.this$0.handleRemoveCallbacks();
                } else if (message.what == 13) {
                    this.this$0.handleRemoveCallback((Callback) message.obj);
                } else if (message.what == 2) {
                    if (!this.this$0.mState.disabledByPolicy) {
                        this.this$0.mAnnounceNextStateChange = true;
                        this.this$0.handleClick();
                        return;
                    }
                    Intent showAdminSupportDetailsIntent = RestrictedLockUtils.getShowAdminSupportDetailsIntent(this.this$0.mContext, this.this$0.mState.enforcedAdmin);
                    if (showAdminSupportDetailsIntent != null) {
                        this.this$0.mHost.startActivityDismissingKeyguard(showAdminSupportDetailsIntent);
                    }
                } else if (message.what == 3) {
                    this.this$0.handleSecondaryClick();
                } else if (message.what == 4) {
                    this.this$0.handleLongClick();
                } else if (message.what == 5) {
                    this.this$0.handleRefreshState(message.obj);
                } else if (message.what == 6) {
                    QSTile qSTile = this.this$0;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    qSTile.handleShowDetail(z);
                } else if (message.what == 7) {
                    this.this$0.handleUserSwitch(message.arg1);
                } else if (message.what == 8) {
                    this.this$0.handleToggleStateChanged(message.arg1 != 0);
                } else if (message.what == 9) {
                    this.this$0.handleScanStateChanged(message.arg1 != 0);
                } else if (message.what == 10) {
                    this.this$0.handleDestroy();
                } else if (message.what == 11) {
                    this.this$0.handleClearState();
                } else if (message.what != 14) {
                    throw new IllegalArgumentException("Unknown msg: " + message.what);
                } else {
                    this.this$0.setListening(message.arg1 != 0);
                }
            } catch (Throwable th) {
                String str = "Error in " + ((String) null);
                Log.w(this.this$0.TAG, str, th);
                this.this$0.mHost.warn(str, th);
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSTile$Host.class */
    public interface Host {

        /* loaded from: a.zip:com/android/systemui/qs/QSTile$Host$Callback.class */
        public interface Callback {
            void onTilesChanged();
        }

        void collapsePanels();

        BatteryController getBatteryController();

        BluetoothController getBluetoothController();

        CastController getCastController();

        Context getContext();

        FlashlightController getFlashlightController();

        HotKnotController getHotKnotController();

        HotspotController getHotspotController();

        KeyguardMonitor getKeyguardMonitor();

        LocationController getLocationController();

        Looper getLooper();

        ManagedProfileController getManagedProfileController();

        NetworkController getNetworkController();

        NightModeController getNightModeController();

        RotationLockController getRotationLockController();

        TileServices getTileServices();

        UserInfoController getUserInfoController();

        UserSwitcherController getUserSwitcherController();

        ZenModeController getZenModeController();

        void openPanels();

        void removeTile(String str);

        void startActivityDismissingKeyguard(PendingIntent pendingIntent);

        void startActivityDismissingKeyguard(Intent intent);

        void startRunnableDismissingKeyguard(Runnable runnable);

        void warn(String str, Throwable th);
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSTile$Icon.class */
    public static abstract class Icon {
        public abstract Drawable getDrawable(Context context);

        public Drawable getInvisibleDrawable(Context context) {
            return getDrawable(context);
        }

        public int getPadding() {
            return 0;
        }

        public int hashCode() {
            return Icon.class.hashCode();
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSTile$ResourceIcon.class */
    public static class ResourceIcon extends Icon {
        private static final SparseArray<Icon> ICONS = new SparseArray<>();
        protected final int mResId;

        private ResourceIcon(int i) {
            this.mResId = i;
        }

        /* synthetic */ ResourceIcon(int i, ResourceIcon resourceIcon) {
            this(i);
        }

        public static Icon get(int i) {
            Icon icon = ICONS.get(i);
            ResourceIcon resourceIcon = icon;
            if (icon == null) {
                resourceIcon = new ResourceIcon(i);
                ICONS.put(i, resourceIcon);
            }
            return resourceIcon;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (obj instanceof ResourceIcon) {
                z = false;
                if (((ResourceIcon) obj).mResId == this.mResId) {
                    z = true;
                }
            }
            return z;
        }

        @Override // com.android.systemui.qs.QSTile.Icon
        public Drawable getDrawable(Context context) {
            return context.getDrawable(this.mResId);
        }

        @Override // com.android.systemui.qs.QSTile.Icon
        public Drawable getInvisibleDrawable(Context context) {
            return context.getDrawable(this.mResId);
        }

        public String toString() {
            return String.format("ResourceIcon[resId=0x%08x]", Integer.valueOf(this.mResId));
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSTile$SignalState.class */
    public static final class SignalState extends BooleanState {
        public boolean activityIn;
        public boolean activityOut;
        public boolean connected;
        public boolean filter;
        public boolean isOverlayIconWide;
        public int overlayIconId;

        @Override // com.android.systemui.qs.QSTile.BooleanState, com.android.systemui.qs.QSTile.State
        public boolean copyTo(State state) {
            SignalState signalState = (SignalState) state;
            boolean z = (signalState.connected == this.connected && signalState.activityIn == this.activityIn && signalState.activityOut == this.activityOut && signalState.overlayIconId == this.overlayIconId) ? signalState.isOverlayIconWide != this.isOverlayIconWide : true;
            signalState.connected = this.connected;
            signalState.activityIn = this.activityIn;
            signalState.activityOut = this.activityOut;
            signalState.overlayIconId = this.overlayIconId;
            signalState.filter = this.filter;
            signalState.isOverlayIconWide = this.isOverlayIconWide;
            if (super.copyTo(state)) {
                z = true;
            }
            return z;
        }

        @Override // com.android.systemui.qs.QSTile.BooleanState, com.android.systemui.qs.QSTile.State
        protected StringBuilder toStringBuilder() {
            StringBuilder stringBuilder = super.toStringBuilder();
            stringBuilder.insert(stringBuilder.length() - 1, ",connected=" + this.connected);
            stringBuilder.insert(stringBuilder.length() - 1, ",activityIn=" + this.activityIn);
            stringBuilder.insert(stringBuilder.length() - 1, ",activityOut=" + this.activityOut);
            stringBuilder.insert(stringBuilder.length() - 1, ",overlayIconId=" + this.overlayIconId);
            stringBuilder.insert(stringBuilder.length() - 1, ",filter=" + this.filter);
            stringBuilder.insert(stringBuilder.length() - 1, ",wideOverlayIcon=" + this.isOverlayIconWide);
            return stringBuilder;
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSTile$State.class */
    public static class State {
        public boolean autoMirrorDrawable = true;
        public CharSequence contentDescription;
        public boolean disabledByPolicy;
        public CharSequence dualLabelContentDescription;
        public RestrictedLockUtils.EnforcedAdmin enforcedAdmin;
        public String expandedAccessibilityClassName;
        public Icon icon;
        public CharSequence label;
        public String minimalAccessibilityClassName;
        public CharSequence minimalContentDescription;

        public boolean copyTo(State state) {
            if (state == null) {
                throw new IllegalArgumentException();
            }
            if (state.getClass().equals(getClass())) {
                boolean z = (Objects.equals(state.icon, this.icon) && Objects.equals(state.label, this.label) && Objects.equals(state.contentDescription, this.contentDescription) && Objects.equals(Boolean.valueOf(state.autoMirrorDrawable), Boolean.valueOf(this.autoMirrorDrawable)) && Objects.equals(state.dualLabelContentDescription, this.dualLabelContentDescription) && Objects.equals(state.minimalContentDescription, this.minimalContentDescription) && Objects.equals(state.minimalAccessibilityClassName, this.minimalAccessibilityClassName) && Objects.equals(state.expandedAccessibilityClassName, this.expandedAccessibilityClassName) && Objects.equals(Boolean.valueOf(state.disabledByPolicy), Boolean.valueOf(this.disabledByPolicy))) ? !Objects.equals(state.enforcedAdmin, this.enforcedAdmin) : true;
                state.icon = this.icon;
                state.label = this.label;
                state.contentDescription = this.contentDescription;
                state.dualLabelContentDescription = this.dualLabelContentDescription;
                state.minimalContentDescription = this.minimalContentDescription;
                state.minimalAccessibilityClassName = this.minimalAccessibilityClassName;
                state.expandedAccessibilityClassName = this.expandedAccessibilityClassName;
                state.autoMirrorDrawable = this.autoMirrorDrawable;
                state.disabledByPolicy = this.disabledByPolicy;
                if (this.enforcedAdmin == null) {
                    state.enforcedAdmin = null;
                } else if (state.enforcedAdmin == null) {
                    state.enforcedAdmin = new RestrictedLockUtils.EnforcedAdmin(this.enforcedAdmin);
                } else {
                    this.enforcedAdmin.copyTo(state.enforcedAdmin);
                }
                return z;
            }
            throw new IllegalArgumentException();
        }

        public String toString() {
            return toStringBuilder().toString();
        }

        protected StringBuilder toStringBuilder() {
            StringBuilder append = new StringBuilder(getClass().getSimpleName()).append('[');
            append.append(",icon=").append(this.icon);
            append.append(",label=").append(this.label);
            append.append(",contentDescription=").append(this.contentDescription);
            append.append(",dualLabelContentDescription=").append(this.dualLabelContentDescription);
            append.append(",minimalContentDescription=").append(this.minimalContentDescription);
            append.append(",minimalAccessibilityClassName=").append(this.minimalAccessibilityClassName);
            append.append(",expandedAccessibilityClassName=").append(this.expandedAccessibilityClassName);
            append.append(",autoMirrorDrawable=").append(this.autoMirrorDrawable);
            append.append(",disabledByPolicy=").append(this.disabledByPolicy);
            append.append(",enforcedAdmin=").append(this.enforcedAdmin);
            return append.append(']');
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public QSTile(Host host) {
        this.mHost = host;
        this.mContext = host.getContext();
        this.mHandler = new H(this, host.getLooper(), null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAddCallback(Callback callback) {
        this.mCallbacks.add(callback);
        callback.onStateChanged(this.mState);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleRemoveCallback(Callback callback) {
        this.mCallbacks.remove(callback);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleRemoveCallbacks() {
        this.mCallbacks.clear();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleScanStateChanged(boolean z) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onScanStateChanged(z);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleShowDetail(boolean z) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onShowDetail(z);
        }
    }

    private void handleStateChanged() {
        String composeChangeAnnouncement;
        boolean shouldAnnouncementBeDelayed = shouldAnnouncementBeDelayed();
        if (this.mCallbacks.size() != 0) {
            TState newTileState = newTileState();
            this.mState.copyTo(newTileState);
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                this.mCallbacks.get(i).onStateChanged(newTileState);
            }
            if (this.mAnnounceNextStateChange && !shouldAnnouncementBeDelayed && (composeChangeAnnouncement = composeChangeAnnouncement()) != null) {
                this.mCallbacks.get(0).onAnnouncementRequested(composeChangeAnnouncement);
            }
        }
        if (!this.mAnnounceNextStateChange) {
            shouldAnnouncementBeDelayed = false;
        }
        this.mAnnounceNextStateChange = shouldAnnouncementBeDelayed;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleToggleStateChanged(boolean z) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onToggleStateChanged(z);
        }
    }

    public void addCallback(Callback callback) {
        this.mHandler.obtainMessage(1, callback).sendToTarget();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void checkIfRestrictionEnforcedByAdminOnly(State state, String str) {
        RestrictedLockUtils.EnforcedAdmin checkIfRestrictionEnforced = RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, str, ActivityManager.getCurrentUser());
        if (checkIfRestrictionEnforced == null || RestrictedLockUtils.hasBaseUserRestriction(this.mContext, str, ActivityManager.getCurrentUser())) {
            state.disabledByPolicy = false;
            state.enforcedAdmin = null;
            return;
        }
        state.disabledByPolicy = true;
        state.enforcedAdmin = checkIfRestrictionEnforced;
    }

    public final void clearState() {
        this.mHandler.sendEmptyMessage(11);
    }

    public void click() {
        this.mHandler.sendEmptyMessage(2);
    }

    protected String composeChangeAnnouncement() {
        return null;
    }

    public QSIconView createTileView(Context context) {
        return new QSIconView(context);
    }

    public void destroy() {
        this.mHandler.sendEmptyMessage(10);
    }

    public void fireScanStateChanged(boolean z) {
        this.mHandler.obtainMessage(9, z ? 1 : 0, 0).sendToTarget();
    }

    public void fireToggleStateChanged(boolean z) {
        this.mHandler.obtainMessage(8, z ? 1 : 0, 0).sendToTarget();
    }

    public DetailAdapter getDetailAdapter() {
        return null;
    }

    public Host getHost() {
        return this.mHost;
    }

    public abstract Intent getLongClickIntent();

    public abstract int getMetricsCategory();

    public TState getState() {
        return this.mState;
    }

    public abstract CharSequence getTileLabel();

    public String getTileSpec() {
        return this.mTileSpec;
    }

    protected void handleClearState() {
        this.mTmpState = newTileState();
        this.mState = newTileState();
    }

    protected abstract void handleClick();

    /* JADX INFO: Access modifiers changed from: protected */
    public void handleDestroy() {
        setListening(false);
        this.mCallbacks.clear();
    }

    protected void handleLongClick() {
        MetricsLogger.action(this.mContext, 366, getTileSpec());
        this.mHost.startActivityDismissingKeyguard(getLongClickIntent());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void handleRefreshState(Object obj) {
        handleUpdateState(this.mTmpState, obj);
        if (this.mTmpState.copyTo(this.mState)) {
            handleStateChanged();
        }
    }

    protected void handleSecondaryClick() {
        handleClick();
    }

    protected abstract void handleUpdateState(TState tstate, Object obj);

    /* JADX INFO: Access modifiers changed from: protected */
    public void handleUserSwitch(int i) {
        handleRefreshState(null);
    }

    public boolean isAvailable() {
        return true;
    }

    public void longClick() {
        this.mHandler.sendEmptyMessage(4);
    }

    public abstract TState newTileState();

    public final void refreshState() {
        refreshState(null);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final void refreshState(Object obj) {
        this.mHandler.obtainMessage(5, obj).sendToTarget();
    }

    public void removeCallback(Callback callback) {
        this.mHandler.obtainMessage(13, callback).sendToTarget();
    }

    public void removeCallbacks() {
        this.mHandler.sendEmptyMessage(12);
    }

    public void secondaryClick() {
        this.mHandler.sendEmptyMessage(3);
    }

    public void setDetailListening(boolean z) {
    }

    public void setListening(Object obj, boolean z) {
        if (z) {
            if (this.mListeners.add(obj) && this.mListeners.size() == 1) {
                if (DEBUG) {
                    Log.d(this.TAG, "setListening true");
                }
                this.mHandler.obtainMessage(14, 1, 0).sendToTarget();
            }
        } else if (this.mListeners.remove(obj) && this.mListeners.size() == 0) {
            if (DEBUG) {
                Log.d(this.TAG, "setListening false");
            }
            this.mHandler.obtainMessage(14, 0, 0).sendToTarget();
        }
    }

    protected abstract void setListening(boolean z);

    public void setTileSpec(String str) {
        this.mTileSpec = str;
    }

    protected boolean shouldAnnouncementBeDelayed() {
        return false;
    }

    public void showDetail(boolean z) {
        this.mHandler.obtainMessage(6, z ? 1 : 0, 0).sendToTarget();
    }

    public void userSwitch(int i) {
        this.mHandler.obtainMessage(7, i, 0).sendToTarget();
    }
}
