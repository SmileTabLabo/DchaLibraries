package com.android.systemui.statusbar.phone;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.WindowInsets;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.HeadsUpStatusBarView;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
/* loaded from: classes.dex */
public class HeadsUpAppearanceController implements DarkIconDispatcher.DarkReceiver, OnHeadsUpChangedListener {
    private final View mClockView;
    private final DarkIconDispatcher mDarkIconDispatcher;
    private float mExpandFraction;
    private float mExpandedHeight;
    private final HeadsUpManagerPhone mHeadsUpManager;
    private final HeadsUpStatusBarView mHeadsUpStatusBarView;
    private boolean mIsExpanded;
    private final NotificationIconAreaController mNotificationIconAreaController;
    private final NotificationPanelView mPanelView;
    Point mPoint;
    private final BiConsumer<Float, Float> mSetExpandedHeight;
    private final Consumer<ExpandableNotificationRow> mSetTrackingHeadsUp;
    private boolean mShown;
    private final View.OnLayoutChangeListener mStackScrollLayoutChangeListener;
    private final NotificationStackScrollLayout mStackScroller;
    private ExpandableNotificationRow mTrackedChild;
    private final Runnable mUpdatePanelTranslation;

    public HeadsUpAppearanceController(NotificationIconAreaController notificationIconAreaController, HeadsUpManagerPhone headsUpManagerPhone, View view) {
        this(notificationIconAreaController, headsUpManagerPhone, (HeadsUpStatusBarView) view.findViewById(R.id.heads_up_status_bar_view), (NotificationStackScrollLayout) view.findViewById(R.id.notification_stack_scroller), (NotificationPanelView) view.findViewById(R.id.notification_panel), view.findViewById(R.id.clock));
    }

    @VisibleForTesting
    public HeadsUpAppearanceController(NotificationIconAreaController notificationIconAreaController, HeadsUpManagerPhone headsUpManagerPhone, HeadsUpStatusBarView headsUpStatusBarView, NotificationStackScrollLayout notificationStackScrollLayout, NotificationPanelView notificationPanelView, View view) {
        this.mSetTrackingHeadsUp = new Consumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$Ndod8fRabzshTVmNSVHx_M0XNU4
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                HeadsUpAppearanceController.this.setTrackingHeadsUp((ExpandableNotificationRow) obj);
            }
        };
        this.mUpdatePanelTranslation = new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$22QZFjoGlQJQoKOrFe-bHbZltB4
            @Override // java.lang.Runnable
            public final void run() {
                HeadsUpAppearanceController.this.updatePanelTranslation();
            }
        };
        this.mSetExpandedHeight = new BiConsumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$DialNTWPBOn27MISeLu6p9klZxI
            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                HeadsUpAppearanceController.this.setExpandedHeight(((Float) obj).floatValue(), ((Float) obj2).floatValue());
            }
        };
        this.mStackScrollLayoutChangeListener = new View.OnLayoutChangeListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$HeadsUpAppearanceController$hwNOwOgXItDjQM7QwL00pigpnrk
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                HeadsUpAppearanceController.this.updatePanelTranslation();
            }
        };
        this.mNotificationIconAreaController = notificationIconAreaController;
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mHeadsUpManager.addListener(this);
        this.mHeadsUpStatusBarView = headsUpStatusBarView;
        headsUpStatusBarView.setOnDrawingRectChangedListener(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$HeadsUpAppearanceController$1d3l5klDiH8maZOdHwrJBKgigPE
            @Override // java.lang.Runnable
            public final void run() {
                HeadsUpAppearanceController.this.updateIsolatedIconLocation(true);
            }
        });
        this.mStackScroller = notificationStackScrollLayout;
        this.mPanelView = notificationPanelView;
        notificationPanelView.addTrackingHeadsUpListener(this.mSetTrackingHeadsUp);
        notificationPanelView.addVerticalTranslationListener(this.mUpdatePanelTranslation);
        notificationPanelView.setHeadsUpAppearanceController(this);
        this.mStackScroller.addOnExpandedHeightListener(this.mSetExpandedHeight);
        this.mStackScroller.addOnLayoutChangeListener(this.mStackScrollLayoutChangeListener);
        this.mStackScroller.setHeadsUpAppearanceController(this);
        this.mClockView = view;
        this.mDarkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
        this.mDarkIconDispatcher.addDarkReceiver(this);
    }

    public void destroy() {
        this.mHeadsUpManager.removeListener(this);
        this.mHeadsUpStatusBarView.setOnDrawingRectChangedListener(null);
        this.mPanelView.removeTrackingHeadsUpListener(this.mSetTrackingHeadsUp);
        this.mPanelView.removeVerticalTranslationListener(this.mUpdatePanelTranslation);
        this.mPanelView.setHeadsUpAppearanceController(null);
        this.mStackScroller.removeOnExpandedHeightListener(this.mSetExpandedHeight);
        this.mStackScroller.removeOnLayoutChangeListener(this.mStackScrollLayoutChangeListener);
        this.mDarkIconDispatcher.removeDarkReceiver(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateIsolatedIconLocation(boolean z) {
        this.mNotificationIconAreaController.setIsolatedIconLocation(this.mHeadsUpStatusBarView.getIconDrawingRect(), z);
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow) {
        updateTopEntry();
        updateHeader(expandableNotificationRow.getEntry());
    }

    private int getRtlTranslation() {
        if (this.mPoint == null) {
            this.mPoint = new Point();
        }
        int i = 0;
        if (this.mStackScroller.getDisplay() != null) {
            this.mStackScroller.getDisplay().getRealSize(this.mPoint);
            i = this.mPoint.x;
        }
        WindowInsets rootWindowInsets = this.mStackScroller.getRootWindowInsets();
        return ((rootWindowInsets.getSystemWindowInsetLeft() + this.mStackScroller.getRight()) + rootWindowInsets.getSystemWindowInsetRight()) - i;
    }

    public void updatePanelTranslation() {
        float left;
        if (this.mStackScroller.isLayoutRtl()) {
            left = getRtlTranslation();
        } else {
            left = this.mStackScroller.getLeft();
        }
        this.mHeadsUpStatusBarView.setPanelTranslation(left + this.mStackScroller.getTranslationX());
    }

    /* JADX WARN: Removed duplicated region for block: B:20:0x0040  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void updateTopEntry() {
        NotificationData.Entry entry;
        boolean z;
        boolean z2;
        if (!this.mIsExpanded && this.mHeadsUpManager.hasPinnedHeadsUp()) {
            entry = this.mHeadsUpManager.getTopEntry();
        } else {
            entry = null;
        }
        NotificationData.Entry showingEntry = this.mHeadsUpStatusBarView.getShowingEntry();
        this.mHeadsUpStatusBarView.setEntry(entry);
        if (entry != showingEntry) {
            if (entry == null) {
                setShown(false);
                z2 = this.mIsExpanded;
            } else if (showingEntry == null) {
                setShown(true);
                z2 = this.mIsExpanded;
            } else {
                z = false;
                updateIsolatedIconLocation(false);
                this.mNotificationIconAreaController.showIconIsolated(entry != null ? entry.icon : null, z);
            }
            z = !z2;
            updateIsolatedIconLocation(false);
            this.mNotificationIconAreaController.showIconIsolated(entry != null ? entry.icon : null, z);
        }
    }

    private void setShown(boolean z) {
        if (this.mShown != z) {
            this.mShown = z;
            if (!z) {
                CrossFadeHelper.fadeIn(this.mClockView, 110L, 100);
                CrossFadeHelper.fadeOut(this.mHeadsUpStatusBarView, 110L, 0, new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$HeadsUpAppearanceController$5yJ6zXPrjSk_KB2fbcTxEu2j2TI
                    @Override // java.lang.Runnable
                    public final void run() {
                        HeadsUpAppearanceController.this.mHeadsUpStatusBarView.setVisibility(8);
                    }
                });
                return;
            }
            this.mHeadsUpStatusBarView.setVisibility(0);
            CrossFadeHelper.fadeIn(this.mHeadsUpStatusBarView, 110L, 100);
            CrossFadeHelper.fadeOut(this.mClockView, 110L, 0, new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$HeadsUpAppearanceController$iMPD_c-MpkAUOLIdQAujzNCdyYQ
                @Override // java.lang.Runnable
                public final void run() {
                    HeadsUpAppearanceController.this.mClockView.setVisibility(4);
                }
            });
        }
    }

    @VisibleForTesting
    public boolean isShown() {
        return this.mShown;
    }

    public boolean shouldBeVisible() {
        return !this.mIsExpanded && this.mHeadsUpManager.hasPinnedHeadsUp();
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow) {
        updateTopEntry();
        updateHeader(expandableNotificationRow.getEntry());
    }

    public void setExpandedHeight(float f, float f2) {
        boolean z = f != this.mExpandedHeight;
        this.mExpandedHeight = f;
        this.mExpandFraction = f2;
        boolean z2 = f > 0.0f;
        if (z) {
            updateHeadsUpHeaders();
        }
        if (z2 != this.mIsExpanded) {
            this.mIsExpanded = z2;
            updateTopEntry();
        }
    }

    public void setTrackingHeadsUp(ExpandableNotificationRow expandableNotificationRow) {
        ExpandableNotificationRow expandableNotificationRow2 = this.mTrackedChild;
        this.mTrackedChild = expandableNotificationRow;
        if (expandableNotificationRow2 != null) {
            updateHeader(expandableNotificationRow2.getEntry());
        }
    }

    private void updateHeadsUpHeaders() {
        this.mHeadsUpManager.getAllEntries().forEach(new Consumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$HeadsUpAppearanceController$1KnuQ6evxsUDWWnSk6M2Hz6dh50
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                HeadsUpAppearanceController.this.updateHeader((NotificationData.Entry) obj);
            }
        });
    }

    public void updateHeader(NotificationData.Entry entry) {
        float f;
        ExpandableNotificationRow expandableNotificationRow = entry.row;
        if (expandableNotificationRow.isPinned() || expandableNotificationRow.isHeadsUpAnimatingAway() || expandableNotificationRow == this.mTrackedChild) {
            f = this.mExpandFraction;
        } else {
            f = 1.0f;
        }
        expandableNotificationRow.setHeaderVisibleAmount(f);
    }

    @Override // com.android.systemui.statusbar.policy.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i) {
        this.mHeadsUpStatusBarView.onDarkChanged(rect, f, i);
    }

    public void setPublicMode(boolean z) {
        this.mHeadsUpStatusBarView.setPublicMode(z);
        updateTopEntry();
    }
}
