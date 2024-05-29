package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BenesseExtension;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Prefs;
import com.android.systemui.SysUIToast;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.volume.ZenModePanel;
/* loaded from: a.zip:com/android/systemui/qs/tiles/DndTile.class */
public class DndTile extends QSTile<QSTile.BooleanState> {
    private final ZenModeController mController;
    private final DndDetailAdapter mDetailAdapter;
    private final QSTile<QSTile.BooleanState>.AnimationIcon mDisable;
    private final QSTile<QSTile.BooleanState>.AnimationIcon mDisableTotalSilence;
    private boolean mListening;
    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;
    private final BroadcastReceiver mReceiver;
    private boolean mShowingDetail;
    private final ZenModeController.Callback mZenCallback;
    private final ZenModePanel.Callback mZenModePanelCallback;
    private static final Intent ZEN_SETTINGS = new Intent("android.settings.ZEN_MODE_SETTINGS");
    private static final Intent ZEN_PRIORITY_SETTINGS = new Intent("android.settings.ZEN_MODE_PRIORITY_SETTINGS");
    private static final QSTile.Icon TOTAL_SILENCE = QSTile.ResourceIcon.get(2130837719);

    /* loaded from: a.zip:com/android/systemui/qs/tiles/DndTile$DndDetailAdapter.class */
    private final class DndDetailAdapter implements QSTile.DetailAdapter, View.OnAttachStateChangeListener {
        final DndTile this$0;

        private DndDetailAdapter(DndTile dndTile) {
            this.this$0 = dndTile;
        }

        /* synthetic */ DndDetailAdapter(DndTile dndTile, DndDetailAdapter dndDetailAdapter) {
            this(dndTile);
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            ZenModePanel zenModePanel = view != null ? (ZenModePanel) view : (ZenModePanel) LayoutInflater.from(context).inflate(2130968840, viewGroup, false);
            if (view == null) {
                zenModePanel.init(this.this$0.mController);
                zenModePanel.addOnAttachStateChangeListener(this);
                zenModePanel.setCallback(this.this$0.mZenModePanelCallback);
            }
            return zenModePanel;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public int getMetricsCategory() {
            return 149;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public Intent getSettingsIntent() {
            if (BenesseExtension.getDchaState() != 0) {
                return null;
            }
            return DndTile.ZEN_SETTINGS;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public CharSequence getTitle() {
            return this.this$0.mContext.getString(2131493522);
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public Boolean getToggleState() {
            return Boolean.valueOf(((QSTile.BooleanState) this.this$0.mState).value);
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View view) {
            this.this$0.mShowingDetail = true;
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View view) {
            this.this$0.mShowingDetail = false;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public void setToggleState(boolean z) {
            MetricsLogger.action(this.this$0.mContext, 166, z);
            if (z) {
                return;
            }
            this.this$0.mController.setZen(0, null, this.this$0.TAG);
            this.this$0.showDetail(false);
        }
    }

    public DndTile(QSTile.Host host) {
        super(host);
        this.mDisable = new QSTile.AnimationIcon(this, 2130837642, 2130837717);
        this.mDisableTotalSilence = new QSTile.AnimationIcon(this, 2130837645, 2130837717);
        this.mPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener(this) { // from class: com.android.systemui.qs.tiles.DndTile.1
            final DndTile this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
                if ("DndTileCombinedIcon".equals(str) || "DndTileVisible".equals(str)) {
                    this.this$0.refreshState();
                }
            }
        };
        this.mZenCallback = new ZenModeController.Callback(this) { // from class: com.android.systemui.qs.tiles.DndTile.2
            final DndTile this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
            public void onZenChanged(int i) {
                this.this$0.refreshState(Integer.valueOf(i));
            }
        };
        this.mReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.qs.tiles.DndTile.3
            final DndTile this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                DndTile.setVisible(this.this$0.mContext, intent.getBooleanExtra("visible", false));
                this.this$0.refreshState();
            }
        };
        this.mZenModePanelCallback = new ZenModePanel.Callback(this) { // from class: com.android.systemui.qs.tiles.DndTile.4
            final DndTile this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.volume.ZenModePanel.Callback
            public void onExpanded(boolean z) {
            }

            @Override // com.android.systemui.volume.ZenModePanel.Callback
            public void onInteraction() {
            }

            @Override // com.android.systemui.volume.ZenModePanel.Callback
            public void onPrioritySettings() {
                if (BenesseExtension.getDchaState() != 0) {
                    return;
                }
                this.this$0.mHost.startActivityDismissingKeyguard(DndTile.ZEN_PRIORITY_SETTINGS);
            }
        };
        this.mController = host.getZenModeController();
        this.mDetailAdapter = new DndDetailAdapter(this, null);
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter("com.android.systemui.dndtile.SET_VISIBLE"));
    }

    public static boolean isCombinedIcon(Context context) {
        return Prefs.getBoolean(context, "DndTileCombinedIcon", false);
    }

    public static boolean isVisible(Context context) {
        return Prefs.getBoolean(context, "DndTileVisible", false);
    }

    public static void setCombinedIcon(Context context, boolean z) {
        Prefs.putBoolean(context, "DndTileCombinedIcon", z);
    }

    public static void setVisible(Context context, boolean z) {
        Prefs.putBoolean(context, "DndTileVisible", z);
    }

    @Override // com.android.systemui.qs.QSTile
    protected String composeChangeAnnouncement() {
        return ((QSTile.BooleanState) this.mState).value ? this.mContext.getString(2131493465) : this.mContext.getString(2131493464);
    }

    @Override // com.android.systemui.qs.QSTile
    public QSTile.DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        return ZEN_SETTINGS;
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 118;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(2131493522);
    }

    @Override // com.android.systemui.qs.QSTile
    public void handleClick() {
        if (this.mController.isVolumeRestricted()) {
            this.mHost.collapsePanels();
            SysUIToast.makeText(this.mContext, this.mContext.getString(17040683), 1).show();
            return;
        }
        MetricsLogger.action(this.mContext, getMetricsCategory(), !((QSTile.BooleanState) this.mState).value);
        if (((QSTile.BooleanState) this.mState).value) {
            this.mController.setZen(0, null, this.TAG);
            return;
        }
        this.mController.setZen(Prefs.getInt(this.mContext, "DndFavoriteZen", 3), null, this.TAG);
        showDetail(true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int intValue = obj instanceof Integer ? ((Integer) obj).intValue() : this.mController.getZen();
        boolean z = intValue != 0;
        boolean z2 = booleanState.value != z;
        booleanState.value = z;
        checkIfRestrictionEnforcedByAdminOnly(booleanState, "no_adjust_volume");
        switch (intValue) {
            case 1:
                booleanState.icon = QSTile.ResourceIcon.get(2130837718);
                booleanState.label = this.mContext.getString(2131493523);
                booleanState.contentDescription = this.mContext.getString(2131493459);
                break;
            case 2:
                booleanState.icon = TOTAL_SILENCE;
                booleanState.label = this.mContext.getString(2131493525);
                booleanState.contentDescription = this.mContext.getString(2131493460);
                break;
            case 3:
                booleanState.icon = QSTile.ResourceIcon.get(2130837718);
                booleanState.label = this.mContext.getString(2131493524);
                booleanState.contentDescription = this.mContext.getString(2131493461);
                break;
            default:
                booleanState.icon = TOTAL_SILENCE.equals(booleanState.icon) ? this.mDisableTotalSilence : this.mDisable;
                booleanState.label = this.mContext.getString(2131493522);
                booleanState.contentDescription = this.mContext.getString(2131493462);
                break;
        }
        if (this.mShowingDetail && !booleanState.value) {
            showDetail(false);
        }
        if (z2) {
            fireToggleStateChanged(booleanState.value);
        }
        String name = Switch.class.getName();
        booleanState.expandedAccessibilityClassName = name;
        booleanState.minimalAccessibilityClassName = name;
    }

    @Override // com.android.systemui.qs.QSTile
    public boolean isAvailable() {
        return isVisible(this.mContext);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        if (this.mListening) {
            this.mController.addCallback(this.mZenCallback);
            Prefs.registerListener(this.mContext, this.mPrefListener);
            return;
        }
        this.mController.removeCallback(this.mZenCallback);
        Prefs.unregisterListener(this.mContext, this.mPrefListener);
    }
}
