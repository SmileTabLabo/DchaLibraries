package com.android.systemui.qs;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.settings.BrightnessController;
import com.android.systemui.settings.ToggleSlider;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.tuner.TunerService;
import com.mediatek.systemui.PluginManager;
import com.mediatek.systemui.ext.IQuickSettingsPlugin;
import java.util.ArrayList;
import java.util.Collection;
/* loaded from: a.zip:com/android/systemui/qs/QSPanel.class */
public class QSPanel extends LinearLayout implements TunerService.Tunable, QSTile.Host.Callback {
    private BrightnessController mBrightnessController;
    private BrightnessMirrorController mBrightnessMirrorController;
    private int mBrightnessPaddingTop;
    protected final View mBrightnessView;
    private Callback mCallback;
    protected final Context mContext;
    private QSCustomizer mCustomizePanel;
    private Record mDetailRecord;
    protected boolean mExpanded;
    protected QSFooter mFooter;
    private boolean mGridContentVisible;
    private final H mHandler;
    protected QSTileHost mHost;
    protected boolean mListening;
    private int mPanelPaddingBottom;
    private IQuickSettingsPlugin mQuickSettingsPlugin;
    protected final ArrayList<TileRecord> mRecords;
    protected QSTileLayout mTileLayout;

    /* loaded from: a.zip:com/android/systemui/qs/QSPanel$Callback.class */
    public interface Callback {
        void onScanStateChanged(boolean z);

        void onShowingDetail(QSTile.DetailAdapter detailAdapter, int i, int i2);

        void onToggleStateChanged(boolean z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/qs/QSPanel$H.class */
    public class H extends Handler {
        final QSPanel this$0;

        private H(QSPanel qSPanel) {
            this.this$0 = qSPanel;
        }

        /* synthetic */ H(QSPanel qSPanel, H h) {
            this(qSPanel);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            boolean z = true;
            if (message.what == 1) {
                QSPanel qSPanel = this.this$0;
                Record record = (Record) message.obj;
                if (message.arg1 == 0) {
                    z = false;
                }
                qSPanel.handleShowDetail(record, z);
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSPanel$QSTileLayout.class */
    public interface QSTileLayout {
        void addTile(TileRecord tileRecord);

        int getOffsetTop(TileRecord tileRecord);

        void removeTile(TileRecord tileRecord);

        void setListening(boolean z);

        boolean updateResources();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: a.zip:com/android/systemui/qs/QSPanel$Record.class */
    public static class Record {
        QSTile.DetailAdapter detailAdapter;
        int x;
        int y;

        protected Record() {
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSPanel$TileRecord.class */
    public static final class TileRecord extends Record {
        public QSTile.Callback callback;
        public boolean scanState;
        public QSTile<?> tile;
        public QSTileBaseView tileView;
    }

    public QSPanel(Context context) {
        this(context, null);
    }

    public QSPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRecords = new ArrayList<>();
        this.mHandler = new H(this, null);
        this.mGridContentVisible = true;
        this.mContext = context;
        setOrientation(1);
        this.mBrightnessView = LayoutInflater.from(context).inflate(2130968772, (ViewGroup) this, false);
        addView(this.mBrightnessView);
        this.mQuickSettingsPlugin = PluginManager.getQuickSettingsPlugin(this.mContext);
        this.mQuickSettingsPlugin.addOpViews(this);
        setupTileLayout();
        this.mFooter = new QSFooter(this, context);
        addView(this.mFooter.getView());
        updateResources();
        this.mBrightnessController = new BrightnessController(getContext(), (ImageView) findViewById(2131886591), (ToggleSlider) findViewById(2131886592));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireScanStateChanged(boolean z) {
        if (this.mCallback != null) {
            this.mCallback.onScanStateChanged(z);
        }
    }

    private void fireShowingDetail(QSTile.DetailAdapter detailAdapter, int i, int i2) {
        if (this.mCallback != null) {
            this.mCallback.onShowingDetail(detailAdapter, i, i2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireToggleStateChanged(boolean z) {
        if (this.mCallback != null) {
            this.mCallback.onToggleStateChanged(z);
        }
    }

    private QSTile<?> getTile(String str) {
        for (int i = 0; i < this.mRecords.size(); i++) {
            if (str.equals(this.mRecords.get(i).tile.getTileSpec())) {
                return this.mRecords.get(i).tile;
            }
        }
        return this.mHost.createTile(str);
    }

    private void handleShowDetailImpl(Record record, boolean z, int i, int i2) {
        setDetailRecord(z ? record : null);
        QSTile.DetailAdapter detailAdapter = null;
        if (z) {
            detailAdapter = record.detailAdapter;
        }
        fireShowingDetail(detailAdapter, i, i2);
    }

    private void handleShowDetailTile(TileRecord tileRecord, boolean z) {
        if ((this.mDetailRecord != null) == z && this.mDetailRecord == tileRecord) {
            return;
        }
        if (z) {
            tileRecord.detailAdapter = tileRecord.tile.getDetailAdapter();
            if (tileRecord.detailAdapter == null) {
                return;
            }
        }
        tileRecord.tile.setDetailListening(z);
        handleShowDetailImpl(tileRecord, z, tileRecord.tileView.getLeft() + (tileRecord.tileView.getWidth() / 2), tileRecord.tileView.getTop() + this.mTileLayout.getOffsetTop(tileRecord) + (tileRecord.tileView.getHeight() / 2) + getTop());
    }

    private void logTiles() {
        for (int i = 0; i < this.mRecords.size(); i++) {
            MetricsLogger.visible(this.mContext, this.mRecords.get(i).tile.getMetricsCategory());
        }
    }

    private void setDetailRecord(Record record) {
        if (record == this.mDetailRecord) {
            return;
        }
        this.mDetailRecord = record;
        fireScanStateChanged(this.mDetailRecord instanceof TileRecord ? ((TileRecord) this.mDetailRecord).scanState : false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: showEdit */
    public void m1092com_android_systemui_qs_QSPanel_lambda$2(View view) {
        view.post(new Runnable(this, view) { // from class: com.android.systemui.qs.QSPanel.4
            final QSPanel this$0;
            final View val$v;

            {
                this.this$0 = this;
                this.val$v = view;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.mCustomizePanel == null || this.this$0.mCustomizePanel.isCustomizing()) {
                    return;
                }
                int[] iArr = new int[2];
                this.val$v.getLocationInWindow(iArr);
                this.this$0.mCustomizePanel.show(iArr[0], iArr[1]);
            }
        });
    }

    /* renamed from: -com_android_systemui_qs_QSPanel_lambda$1  reason: not valid java name */
    /* synthetic */ void m1091com_android_systemui_qs_QSPanel_lambda$1(final View view) {
        this.mHost.startRunnableDismissingKeyguard(new Runnable(this, view) { // from class: com.android.systemui.qs.QSPanel$_void__com_android_systemui_qs_QSPanel_lambda$1_android_view_View_view_LambdaImpl0
            private QSPanel val$this;
            private View val$view;

            {
                this.val$this = this;
                this.val$view = view;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$this.m1092com_android_systemui_qs_QSPanel_lambda$2(this.val$view);
            }
        });
    }

    protected void addTile(QSTile<?> qSTile, boolean z) {
        TileRecord tileRecord = new TileRecord();
        tileRecord.tile = qSTile;
        tileRecord.tileView = createTileView(qSTile, z);
        QSTile.Callback callback = new QSTile.Callback(this, tileRecord) { // from class: com.android.systemui.qs.QSPanel.1
            final QSPanel this$0;
            final TileRecord val$r;

            {
                this.this$0 = this;
                this.val$r = tileRecord;
            }

            @Override // com.android.systemui.qs.QSTile.Callback
            public void onAnnouncementRequested(CharSequence charSequence) {
                this.this$0.announceForAccessibility(charSequence);
            }

            @Override // com.android.systemui.qs.QSTile.Callback
            public void onScanStateChanged(boolean z2) {
                this.val$r.scanState = z2;
                if (this.this$0.mDetailRecord == this.val$r) {
                    this.this$0.fireScanStateChanged(this.val$r.scanState);
                }
            }

            @Override // com.android.systemui.qs.QSTile.Callback
            public void onShowDetail(boolean z2) {
                if (this.this$0.shouldShowDetail()) {
                    this.this$0.showDetail(z2, this.val$r);
                }
            }

            @Override // com.android.systemui.qs.QSTile.Callback
            public void onStateChanged(QSTile.State state) {
                this.this$0.drawTile(this.val$r, state);
            }

            @Override // com.android.systemui.qs.QSTile.Callback
            public void onToggleStateChanged(boolean z2) {
                if (this.this$0.mDetailRecord == this.val$r) {
                    this.this$0.fireToggleStateChanged(z2);
                }
            }
        };
        tileRecord.tile.addCallback(callback);
        tileRecord.callback = callback;
        tileRecord.tileView.init(new View.OnClickListener(this, tileRecord) { // from class: com.android.systemui.qs.QSPanel.2
            final QSPanel this$0;
            final TileRecord val$r;

            {
                this.this$0 = this;
                this.val$r = tileRecord;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.onTileClick(this.val$r.tile);
            }
        }, new View.OnLongClickListener(this, tileRecord) { // from class: com.android.systemui.qs.QSPanel.3
            final QSPanel this$0;
            final TileRecord val$r;

            {
                this.this$0 = this;
                this.val$r = tileRecord;
            }

            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view) {
                this.val$r.tile.longClick();
                return true;
            }
        });
        tileRecord.tile.refreshState();
        this.mRecords.add(tileRecord);
        if (this.mTileLayout != null) {
            this.mTileLayout.addTile(tileRecord);
        }
    }

    public void clickTile(ComponentName componentName) {
        String spec = CustomTile.toSpec(componentName);
        int size = this.mRecords.size();
        for (int i = 0; i < size; i++) {
            if (this.mRecords.get(i).tile.getTileSpec().equals(spec)) {
                this.mRecords.get(i).tile.click();
                return;
            }
        }
    }

    public void closeDetail() {
        if (this.mCustomizePanel == null || !this.mCustomizePanel.isCustomizing()) {
            showDetail(false, this.mDetailRecord);
        } else {
            this.mCustomizePanel.hide(this.mCustomizePanel.getWidth() / 2, this.mCustomizePanel.getHeight() / 2);
        }
    }

    protected QSTileBaseView createTileView(QSTile<?> qSTile, boolean z) {
        return new QSTileView(this.mContext, qSTile.createTileView(this.mContext), z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void drawTile(TileRecord tileRecord, QSTile.State state) {
        tileRecord.tileView.onStateChanged(state);
    }

    public int getGridHeight() {
        return getMeasuredHeight();
    }

    public QSTileHost getHost() {
        return this.mHost;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public QSTileLayout getTileLayout() {
        return this.mTileLayout;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public QSTileBaseView getTileView(QSTile<?> qSTile) {
        for (TileRecord tileRecord : this.mRecords) {
            if (tileRecord.tile == qSTile) {
                return tileRecord.tileView;
            }
        }
        return null;
    }

    protected void handleShowDetail(Record record, boolean z) {
        if (record instanceof TileRecord) {
            handleShowDetailTile((TileRecord) record, z);
            return;
        }
        int i = 0;
        int i2 = 0;
        if (record != null) {
            i = record.x;
            i2 = record.y;
        }
        handleShowDetailImpl(record, z, i, i2);
    }

    public boolean isShowingCustomize() {
        return this.mCustomizePanel != null ? this.mCustomizePanel.isCustomizing() : false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        TunerService.get(this.mContext).addTunable(this, "qs_show_brightness");
        if (this.mHost != null) {
            setTiles(this.mHost.getTiles());
        }
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mFooter.onConfigurationChanged();
        if (this.mBrightnessMirrorController != null) {
            setBrightnessMirror(this.mBrightnessMirrorController);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        TunerService.get(this.mContext).removeTunable(this);
        this.mHost.removeCallback(this);
        for (TileRecord tileRecord : this.mRecords) {
            tileRecord.tile.removeCallbacks();
        }
        super.onDetachedFromWindow();
    }

    protected void onTileClick(QSTile<?> qSTile) {
        qSTile.click();
    }

    @Override // com.android.systemui.qs.QSTile.Host.Callback
    public void onTilesChanged() {
        setTiles(this.mHost.getTiles());
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("qs_show_brightness".equals(str)) {
            View view = this.mBrightnessView;
            int i = 0;
            if (str2 != null) {
                i = Integer.parseInt(str2) != 0 ? 0 : 8;
            }
            view.setVisibility(i);
        }
    }

    public void openDetails(String str) {
        showDetailAdapter(true, getTile(str).getDetailAdapter(), new int[]{getWidth() / 2, 0});
    }

    public void refreshAllTiles() {
        for (TileRecord tileRecord : this.mRecords) {
            tileRecord.tile.refreshState();
        }
        this.mFooter.refreshState();
    }

    public void setBrightnessMirror(BrightnessMirrorController brightnessMirrorController) {
        this.mBrightnessMirrorController = brightnessMirrorController;
        ToggleSlider toggleSlider = (ToggleSlider) findViewById(2131886592);
        toggleSlider.setMirror((ToggleSlider) brightnessMirrorController.getMirror().findViewById(2131886592));
        toggleSlider.setMirrorController(brightnessMirrorController);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void setExpanded(boolean z) {
        if (this.mExpanded == z) {
            return;
        }
        this.mExpanded = z;
        if (!this.mExpanded && (this.mTileLayout instanceof PagedTileLayout)) {
            ((PagedTileLayout) this.mTileLayout).setCurrentItem(0, false);
        }
        MetricsLogger.visibility(this.mContext, 111, this.mExpanded);
        if (this.mExpanded) {
            logTiles();
        } else {
            closeDetail();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setGridContentVisibility(boolean z) {
        int i = z ? 0 : 4;
        setVisibility(i);
        this.mQuickSettingsPlugin.setViewsVisibility(i);
        if (this.mGridContentVisible != z) {
            MetricsLogger.visibility(this.mContext, 111, i);
        }
        this.mGridContentVisible = z;
    }

    public void setHost(QSTileHost qSTileHost, QSCustomizer qSCustomizer) {
        this.mHost = qSTileHost;
        this.mHost.addCallback(this);
        setTiles(this.mHost.getTiles());
        this.mFooter.setHost(qSTileHost);
        this.mCustomizePanel = qSCustomizer;
        if (this.mCustomizePanel != null) {
            this.mCustomizePanel.setHost(this.mHost);
        }
    }

    public void setListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        if (this.mTileLayout != null) {
            this.mTileLayout.setListening(z);
        }
        this.mFooter.setListening(this.mListening);
        if (this.mListening) {
            refreshAllTiles();
        }
        if (z) {
            this.mBrightnessController.registerCallbacks();
            this.mQuickSettingsPlugin.registerCallbacks();
            return;
        }
        this.mBrightnessController.unregisterCallbacks();
        this.mQuickSettingsPlugin.unregisterCallbacks();
    }

    public void setTiles(Collection<QSTile<?>> collection) {
        setTiles(collection, false);
    }

    public void setTiles(Collection<QSTile<?>> collection, boolean z) {
        for (TileRecord tileRecord : this.mRecords) {
            this.mTileLayout.removeTile(tileRecord);
            tileRecord.tile.removeCallback(tileRecord.callback);
        }
        this.mRecords.clear();
        for (QSTile<?> qSTile : collection) {
            addTile(qSTile, z);
        }
    }

    protected void setupTileLayout() {
        this.mTileLayout = (QSTileLayout) LayoutInflater.from(this.mContext).inflate(2130968764, (ViewGroup) this, false);
        this.mTileLayout.setListening(this.mListening);
        addView((View) this.mTileLayout);
        findViewById(16908291).setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.qs.QSPanel._void_setupTileLayout__LambdaImpl0
            private QSPanel val$this;

            {
                this.val$this = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.val$this.m1091com_android_systemui_qs_QSPanel_lambda$1(view);
            }
        });
    }

    protected boolean shouldShowDetail() {
        return this.mExpanded;
    }

    protected void showDetail(boolean z, Record record) {
        this.mHandler.obtainMessage(1, z ? 1 : 0, 0, record).sendToTarget();
    }

    public void showDetailAdapter(boolean z, QSTile.DetailAdapter detailAdapter, int[] iArr) {
        int i = iArr[0];
        int i2 = iArr[1];
        ((View) getParent()).getLocationInWindow(iArr);
        Record record = new Record();
        record.detailAdapter = detailAdapter;
        record.x = i - iArr[0];
        record.y = i2 - iArr[1];
        iArr[0] = i;
        iArr[1] = i2;
        showDetail(z, record);
    }

    public void updateResources() {
        Resources resources = this.mContext.getResources();
        this.mPanelPaddingBottom = resources.getDimensionPixelSize(2131689847);
        this.mBrightnessPaddingTop = resources.getDimensionPixelSize(2131689849);
        setPadding(0, this.mBrightnessPaddingTop, 0, this.mPanelPaddingBottom);
        for (TileRecord tileRecord : this.mRecords) {
            tileRecord.tile.clearState();
        }
        if (this.mListening) {
            refreshAllTiles();
        }
        if (this.mTileLayout != null) {
            this.mTileLayout.updateResources();
        }
    }
}
