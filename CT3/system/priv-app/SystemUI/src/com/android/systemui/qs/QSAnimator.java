package com.android.systemui.qs;

import android.graphics.Path;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.qs.PagedTileLayout;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.TouchAnimator;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.Collection;
/* loaded from: a.zip:com/android/systemui/qs/QSAnimator.class */
public class QSAnimator implements QSTile.Host.Callback, PagedTileLayout.PageListener, TouchAnimator.Listener, View.OnLayoutChangeListener, View.OnAttachStateChangeListener, TunerService.Tunable {
    private boolean mAllowFancy;
    private TouchAnimator mFirstPageAnimator;
    private TouchAnimator mFirstPageDelayedAnimator;
    private boolean mFullRows;
    private QSTileHost mHost;
    private float mLastPosition;
    private TouchAnimator mLastRowAnimator;
    private TouchAnimator mNonfirstPageAnimator;
    private int mNumQuickTiles;
    private boolean mOnKeyguard;
    private PagedTileLayout mPagedLayout;
    private final QSContainer mQsContainer;
    private final QSPanel mQsPanel;
    private final QuickQSPanel mQuickQsPanel;
    private TouchAnimator mTranslationXAnimator;
    private TouchAnimator mTranslationYAnimator;
    private final ArrayList<View> mAllViews = new ArrayList<>();
    private final ArrayList<View> mTopFiveQs = new ArrayList<>();
    private boolean mOnFirstPage = true;
    private final TouchAnimator.Listener mNonFirstPageListener = new TouchAnimator.ListenerAdapter(this) { // from class: com.android.systemui.qs.QSAnimator.1
        final QSAnimator this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.qs.TouchAnimator.ListenerAdapter, com.android.systemui.qs.TouchAnimator.Listener
        public void onAnimationStarted() {
            this.this$0.mQuickQsPanel.setVisibility(0);
        }
    };
    private Runnable mUpdateAnimators = new Runnable(this) { // from class: com.android.systemui.qs.QSAnimator.2
        final QSAnimator this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.updateAnimators();
            this.this$0.setPosition(this.this$0.mLastPosition);
        }
    };

    public QSAnimator(QSContainer qSContainer, QuickQSPanel quickQSPanel, QSPanel qSPanel) {
        this.mQsContainer = qSContainer;
        this.mQuickQsPanel = quickQSPanel;
        this.mQsPanel = qSPanel;
        this.mQsPanel.addOnAttachStateChangeListener(this);
        qSContainer.addOnLayoutChangeListener(this);
        QSPanel.QSTileLayout tileLayout = this.mQsPanel.getTileLayout();
        if (!(tileLayout instanceof PagedTileLayout)) {
            Log.w("QSAnimator", "QS Not using page layout");
            return;
        }
        this.mPagedLayout = (PagedTileLayout) tileLayout;
        this.mPagedLayout.setPageListener(this);
    }

    private void clearAnimationState() {
        int size = this.mAllViews.size();
        this.mQuickQsPanel.setAlpha(0.0f);
        for (int i = 0; i < size; i++) {
            View view = this.mAllViews.get(i);
            view.setAlpha(1.0f);
            view.setTranslationX(0.0f);
            view.setTranslationY(0.0f);
        }
        int size2 = this.mTopFiveQs.size();
        for (int i2 = 0; i2 < size2; i2++) {
            this.mTopFiveQs.get(i2).setVisibility(0);
        }
    }

    private void getRelativePosition(int[] iArr, View view, View view2) {
        iArr[0] = (view.getWidth() / 2) + 0;
        iArr[1] = 0;
        getRelativePositionInt(iArr, view, view2);
    }

    private void getRelativePositionInt(int[] iArr, View view, View view2) {
        if (view == view2 || view == null) {
            return;
        }
        if (!(view instanceof PagedTileLayout.TilePage)) {
            iArr[0] = iArr[0] + view.getLeft();
            iArr[1] = iArr[1] + view.getTop();
        }
        getRelativePositionInt(iArr, (View) view.getParent(), view2);
    }

    private boolean isIconInAnimatedRow(int i) {
        boolean z = false;
        if (this.mPagedLayout == null) {
            return false;
        }
        int columnCount = this.mPagedLayout.getColumnCount();
        if (i < (((this.mNumQuickTiles + columnCount) - 1) / columnCount) * columnCount) {
            z = true;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAnimators() {
        TouchAnimator.Builder builder = new TouchAnimator.Builder();
        TouchAnimator.Builder builder2 = new TouchAnimator.Builder();
        TouchAnimator.Builder builder3 = new TouchAnimator.Builder();
        TouchAnimator.Builder builder4 = new TouchAnimator.Builder();
        if (this.mQsPanel.getHost() == null) {
            return;
        }
        Collection<QSTile<?>> tiles = this.mQsPanel.getHost().getTiles();
        int i = 0;
        int[] iArr = new int[2];
        int[] iArr2 = new int[2];
        int i2 = 0;
        clearAnimationState();
        this.mAllViews.clear();
        this.mTopFiveQs.clear();
        this.mAllViews.add((View) this.mQsPanel.getTileLayout());
        for (QSTile<?> qSTile : tiles) {
            QSTileBaseView tileView = this.mQsPanel.getTileView(qSTile);
            TextView label = ((QSTileView) tileView).getLabel();
            View iconView = tileView.getIcon().getIconView();
            if (i < this.mNumQuickTiles && this.mAllowFancy) {
                QSTileBaseView tileView2 = this.mQuickQsPanel.getTileView(qSTile);
                int i3 = iArr[0];
                getRelativePosition(iArr, tileView2.getIcon(), this.mQsContainer);
                getRelativePosition(iArr2, iconView, this.mQsContainer);
                int i4 = iArr2[0] - iArr[0];
                int i5 = iArr2[1] - iArr[1];
                i2 = iArr[0] - i3;
                builder2.addFloat(tileView2, "translationX", 0.0f, i4);
                builder3.addFloat(tileView2, "translationY", 0.0f, i5);
                builder.addFloat(tileView, "translationY", this.mQsPanel.getHeight(), 0.0f);
                builder2.addFloat(label, "translationX", -i4, 0.0f);
                builder3.addFloat(label, "translationY", -i5, 0.0f);
                this.mTopFiveQs.add(iconView);
                this.mAllViews.add(iconView);
                this.mAllViews.add(tileView2);
            } else if (this.mFullRows && isIconInAnimatedRow(i)) {
                iArr[0] = iArr[0] + i2;
                getRelativePosition(iArr2, iconView, this.mQsContainer);
                int i6 = iArr2[0];
                int i7 = iArr[0];
                int i8 = iArr2[1] - iArr[1];
                builder.addFloat(tileView, "translationY", this.mQsPanel.getHeight(), 0.0f);
                builder2.addFloat(tileView, "translationX", -(i6 - i7), 0.0f);
                builder3.addFloat(label, "translationY", -i8, 0.0f);
                builder3.addFloat(iconView, "translationY", -i8, 0.0f);
                this.mAllViews.add(iconView);
            } else {
                builder4.addFloat(tileView, "alpha", 0.0f, 1.0f);
            }
            this.mAllViews.add(tileView);
            this.mAllViews.add(label);
            i++;
        }
        if (this.mAllowFancy) {
            this.mFirstPageAnimator = builder.setListener(this).build();
            this.mFirstPageDelayedAnimator = new TouchAnimator.Builder().setStartDelay(0.7f).addFloat(this.mQsPanel.getTileLayout(), "alpha", 0.0f, 1.0f).build();
            this.mLastRowAnimator = builder4.setStartDelay(0.86f).build();
            Path path = new Path();
            path.moveTo(0.0f, 0.0f);
            path.cubicTo(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
            PathInterpolatorBuilder pathInterpolatorBuilder = new PathInterpolatorBuilder(0.0f, 0.0f, 0.0f, 1.0f);
            builder2.setInterpolator(pathInterpolatorBuilder.getXInterpolator());
            builder3.setInterpolator(pathInterpolatorBuilder.getYInterpolator());
            this.mTranslationXAnimator = builder2.build();
            this.mTranslationYAnimator = builder3.build();
        }
        this.mNonfirstPageAnimator = new TouchAnimator.Builder().addFloat(this.mQuickQsPanel, "alpha", 1.0f, 0.0f).setListener(this.mNonFirstPageListener).setEndDelay(0.5f).build();
    }

    @Override // com.android.systemui.qs.TouchAnimator.Listener
    public void onAnimationAtEnd() {
        this.mQuickQsPanel.setVisibility(4);
        int size = this.mTopFiveQs.size();
        for (int i = 0; i < size; i++) {
            this.mTopFiveQs.get(i).setVisibility(0);
        }
    }

    @Override // com.android.systemui.qs.TouchAnimator.Listener
    public void onAnimationAtStart() {
        this.mQuickQsPanel.setVisibility(0);
    }

    @Override // com.android.systemui.qs.TouchAnimator.Listener
    public void onAnimationStarted() {
        this.mQuickQsPanel.setVisibility(this.mOnKeyguard ? 4 : 0);
        if (this.mOnFirstPage) {
            int size = this.mTopFiveQs.size();
            for (int i = 0; i < size; i++) {
                this.mTopFiveQs.get(i).setVisibility(4);
            }
        }
    }

    @Override // android.view.View.OnLayoutChangeListener
    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        this.mQsPanel.post(this.mUpdateAnimators);
    }

    @Override // com.android.systemui.qs.PagedTileLayout.PageListener
    public void onPageChanged(boolean z) {
        if (this.mOnFirstPage == z) {
            return;
        }
        if (!z) {
            clearAnimationState();
        }
        this.mOnFirstPage = z;
    }

    public void onRtlChanged() {
        updateAnimators();
    }

    @Override // com.android.systemui.qs.QSTile.Host.Callback
    public void onTilesChanged() {
        this.mQsPanel.post(this.mUpdateAnimators);
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("sysui_qs_fancy_anim".equals(str)) {
            boolean z = true;
            if (str2 != null) {
                z = Integer.parseInt(str2) != 0;
            }
            this.mAllowFancy = z;
            if (!this.mAllowFancy) {
                clearAnimationState();
            }
        } else if ("sysui_qs_move_whole_rows".equals(str)) {
            boolean z2 = true;
            if (str2 != null) {
                z2 = Integer.parseInt(str2) != 0;
            }
            this.mFullRows = z2;
        } else if ("sysui_qqs_count".equals(str)) {
            this.mNumQuickTiles = this.mQuickQsPanel.getNumQuickTiles(this.mQsContainer.getContext());
            clearAnimationState();
        }
        updateAnimators();
    }

    @Override // android.view.View.OnAttachStateChangeListener
    public void onViewAttachedToWindow(View view) {
        TunerService.get(this.mQsContainer.getContext()).addTunable(this, "sysui_qs_fancy_anim", "sysui_qs_move_whole_rows", "sysui_qqs_count");
    }

    @Override // android.view.View.OnAttachStateChangeListener
    public void onViewDetachedFromWindow(View view) {
        if (this.mHost != null) {
            this.mHost.removeCallback(this);
        }
        TunerService.get(this.mQsContainer.getContext()).removeTunable(this);
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        qSTileHost.addCallback(this);
        updateAnimators();
    }

    public void setOnKeyguard(boolean z) {
        this.mOnKeyguard = z;
        this.mQuickQsPanel.setVisibility(this.mOnKeyguard ? 4 : 0);
        if (this.mOnKeyguard) {
            clearAnimationState();
        }
    }

    public void setPosition(float f) {
        if (this.mFirstPageAnimator == null || this.mOnKeyguard) {
            return;
        }
        this.mLastPosition = f;
        if (!this.mOnFirstPage || !this.mAllowFancy) {
            this.mNonfirstPageAnimator.setPosition(f);
            return;
        }
        this.mQuickQsPanel.setAlpha(1.0f);
        this.mFirstPageAnimator.setPosition(f);
        this.mFirstPageDelayedAnimator.setPosition(f);
        this.mTranslationXAnimator.setPosition(f);
        this.mTranslationYAnimator.setPosition(f);
        this.mLastRowAnimator.setPosition(f);
    }
}
