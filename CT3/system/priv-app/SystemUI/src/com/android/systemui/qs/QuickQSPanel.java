package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Space;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
/* loaded from: a.zip:com/android/systemui/qs/QuickQSPanel.class */
public class QuickQSPanel extends QSPanel {
    private QSPanel mFullPanel;
    private View mHeader;
    private int mMaxTiles;
    private final TunerService.Tunable mNumTiles;

    /* loaded from: a.zip:com/android/systemui/qs/QuickQSPanel$HeaderTileLayout.class */
    private static class HeaderTileLayout extends LinearLayout implements QSPanel.QSTileLayout {
        private final Space mEndSpacer;
        private boolean mListening;
        protected final ArrayList<QSPanel.TileRecord> mRecords;

        public HeaderTileLayout(Context context) {
            super(context);
            this.mRecords = new ArrayList<>();
            setClipChildren(false);
            setClipToPadding(false);
            setGravity(16);
            setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            this.mEndSpacer = new Space(context);
            this.mEndSpacer.setLayoutParams(generateLayoutParams());
            updateDownArrowMargin();
            addView(this.mEndSpacer);
            setOrientation(0);
        }

        private LinearLayout.LayoutParams generateLayoutParams() {
            int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(2131689826);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize);
            layoutParams.gravity = 17;
            return layoutParams;
        }

        private LinearLayout.LayoutParams generateSpaceParams() {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, this.mContext.getResources().getDimensionPixelSize(2131689826));
            layoutParams.weight = 1.0f;
            layoutParams.gravity = 17;
            return layoutParams;
        }

        private int getChildIndex(QSTileBaseView qSTileBaseView) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (getChildAt(i) == qSTileBaseView) {
                    return i;
                }
            }
            return -1;
        }

        private void updateDownArrowMargin() {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mEndSpacer.getLayoutParams();
            layoutParams.setMarginStart(this.mContext.getResources().getDimensionPixelSize(2131689858));
            this.mEndSpacer.setLayoutParams(layoutParams);
        }

        @Override // com.android.systemui.qs.QSPanel.QSTileLayout
        public void addTile(QSPanel.TileRecord tileRecord) {
            addView(tileRecord.tileView, getChildCount() - 1, generateLayoutParams());
            addView(new Space(this.mContext), getChildCount() - 1, generateSpaceParams());
            this.mRecords.add(tileRecord);
            tileRecord.tile.setListening(this, this.mListening);
        }

        @Override // com.android.systemui.qs.QSPanel.QSTileLayout
        public int getOffsetTop(QSPanel.TileRecord tileRecord) {
            return 0;
        }

        @Override // android.view.View
        public boolean hasOverlappingRendering() {
            return false;
        }

        @Override // android.view.View
        protected void onConfigurationChanged(Configuration configuration) {
            super.onConfigurationChanged(configuration);
            updateDownArrowMargin();
        }

        @Override // android.widget.LinearLayout, android.view.View
        protected void onMeasure(int i, int i2) {
            super.onMeasure(i, i2);
            if (this.mRecords == null || this.mRecords.size() <= 0) {
                return;
            }
            HeaderTileLayout headerTileLayout = this;
            for (QSPanel.TileRecord tileRecord : this.mRecords) {
                if (tileRecord.tileView.getVisibility() != 8) {
                    headerTileLayout = tileRecord.tileView.updateAccessibilityOrder(headerTileLayout);
                }
            }
            this.mRecords.get(0).tileView.setAccessibilityTraversalAfter(2131886679);
            this.mRecords.get(this.mRecords.size() - 1).tileView.setAccessibilityTraversalBefore(2131886599);
        }

        @Override // com.android.systemui.qs.QSPanel.QSTileLayout
        public void removeTile(QSPanel.TileRecord tileRecord) {
            int childIndex = getChildIndex(tileRecord.tileView);
            removeViewAt(childIndex);
            removeViewAt(childIndex);
            this.mRecords.remove(tileRecord);
            tileRecord.tile.setListening(this, false);
        }

        @Override // com.android.systemui.qs.QSPanel.QSTileLayout
        public void setListening(boolean z) {
            if (this.mListening == z) {
                return;
            }
            this.mListening = z;
            for (QSPanel.TileRecord tileRecord : this.mRecords) {
                tileRecord.tile.setListening(this, this.mListening);
            }
        }

        @Override // com.android.systemui.qs.QSPanel.QSTileLayout
        public boolean updateResources() {
            return false;
        }
    }

    public QuickQSPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mNumTiles = new TunerService.Tunable(this) { // from class: com.android.systemui.qs.QuickQSPanel.1
            final QuickQSPanel this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.tuner.TunerService.Tunable
            public void onTuningChanged(String str, String str2) {
                this.this$0.setMaxTiles(this.this$0.getNumQuickTiles(this.this$0.mContext));
            }
        };
        if (this.mTileLayout != null) {
            for (int i = 0; i < this.mRecords.size(); i++) {
                this.mTileLayout.removeTile(this.mRecords.get(i));
            }
            removeView((View) this.mTileLayout);
        }
        this.mTileLayout = new HeaderTileLayout(context);
        this.mTileLayout.setListening(this.mListening);
        addView((View) this.mTileLayout, 1);
    }

    @Override // com.android.systemui.qs.QSPanel
    protected QSTileBaseView createTileView(QSTile<?> qSTile, boolean z) {
        return new QSTileBaseView(this.mContext, qSTile.createTileView(this.mContext), z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public void drawTile(QSPanel.TileRecord tileRecord, QSTile.State state) {
        QSTile.State state2 = state;
        if (state instanceof QSTile.SignalState) {
            state2 = tileRecord.tile.newTileState();
            state.copyTo(state2);
            ((QSTile.SignalState) state2).activityIn = false;
            ((QSTile.SignalState) state2).activityOut = false;
        }
        super.drawTile(tileRecord, state2);
    }

    public int getNumQuickTiles(Context context) {
        return TunerService.get(context).getValue("sysui_qqs_count", 5);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel, android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        TunerService.get(this.mContext).addTunable(this.mNumTiles, "sysui_qqs_count");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel, android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        TunerService.get(this.mContext).removeTunable(this.mNumTiles);
    }

    @Override // com.android.systemui.qs.QSPanel
    protected void onTileClick(QSTile<?> qSTile) {
        qSTile.secondaryClick();
    }

    @Override // com.android.systemui.qs.QSPanel, com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if (str.equals("qs_show_brightness")) {
            super.onTuningChanged(str, "0");
        }
    }

    @Override // com.android.systemui.qs.QSPanel
    public void setHost(QSTileHost qSTileHost, QSCustomizer qSCustomizer) {
        super.setHost(qSTileHost, qSCustomizer);
        setTiles(this.mHost.getTiles());
    }

    public void setMaxTiles(int i) {
        this.mMaxTiles = i;
        if (this.mHost != null) {
            setTiles(this.mHost.getTiles());
        }
    }

    public void setQSPanelAndHeader(QSPanel qSPanel, View view) {
        this.mFullPanel = qSPanel;
        this.mHeader = view;
    }

    @Override // com.android.systemui.qs.QSPanel
    public void setTiles(Collection<QSTile<?>> collection) {
        ArrayList arrayList = new ArrayList();
        Iterator<T> it = collection.iterator();
        while (it.hasNext()) {
            arrayList.add((QSTile) it.next());
            if (arrayList.size() == this.mMaxTiles) {
                break;
            }
        }
        super.setTiles(arrayList, true);
    }

    @Override // com.android.systemui.qs.QSPanel
    protected boolean shouldShowDetail() {
        return !this.mExpanded;
    }
}
