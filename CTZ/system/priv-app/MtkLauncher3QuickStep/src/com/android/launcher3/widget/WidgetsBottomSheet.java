package com.android.launcher3.widget;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.launcher3.DropTarget;
import com.android.launcher3.Insettable;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.model.WidgetItem;
import com.android.launcher3.userevent.nano.LauncherLogProto;
import com.android.launcher3.util.PackageUserKey;
import java.util.List;
/* loaded from: classes.dex */
public class WidgetsBottomSheet extends BaseWidgetSheet implements Insettable {
    private static final int DEFAULT_CLOSE_DURATION = 200;
    private Rect mInsets;
    private ItemInfo mOriginalItemInfo;

    @Override // com.android.launcher3.widget.BaseWidgetSheet, com.android.launcher3.logging.UserEventDispatcher.LogContainerProvider
    public /* bridge */ /* synthetic */ void fillInLogContainerData(View view, ItemInfo itemInfo, LauncherLogProto.Target target, LauncherLogProto.Target target2) {
        super.fillInLogContainerData(view, itemInfo, target, target2);
    }

    @Override // com.android.launcher3.widget.BaseWidgetSheet, com.android.launcher3.DragSource
    public /* bridge */ /* synthetic */ void onDropCompleted(View view, DropTarget.DragObject dragObject, boolean z) {
        super.onDropCompleted(view, dragObject, z);
    }

    public WidgetsBottomSheet(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public WidgetsBottomSheet(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setWillNotDraw(false);
        this.mInsets = new Rect();
        this.mContent = this;
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        setTranslationShift(this.mTranslationShift);
    }

    public void populateAndShow(ItemInfo itemInfo) {
        this.mOriginalItemInfo = itemInfo;
        ((TextView) findViewById(R.id.title)).setText(getContext().getString(R.string.widgets_bottom_sheet_title, this.mOriginalItemInfo.title));
        onWidgetsBound();
        this.mLauncher.getDragLayer().addView(this);
        this.mIsOpen = false;
        animateOpen();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.AbstractFloatingView
    public void onWidgetsBound() {
        List<WidgetItem> widgetsForPackageUser = this.mLauncher.getPopupDataProvider().getWidgetsForPackageUser(new PackageUserKey(this.mOriginalItemInfo.getTargetComponent().getPackageName(), this.mOriginalItemInfo.user));
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.widgets);
        ViewGroup viewGroup2 = (ViewGroup) viewGroup.findViewById(R.id.widgets_cell_list);
        viewGroup2.removeAllViews();
        for (int i = 0; i < widgetsForPackageUser.size(); i++) {
            WidgetCell addItemCell = addItemCell(viewGroup2);
            addItemCell.applyFromCellItem(widgetsForPackageUser.get(i), LauncherAppState.getInstance(this.mLauncher).getWidgetCache());
            addItemCell.ensurePreview();
            addItemCell.setVisibility(0);
            if (i < widgetsForPackageUser.size() - 1) {
                addDivider(viewGroup2);
            }
        }
        if (widgetsForPackageUser.size() != 1) {
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.widget_list_divider, viewGroup, false);
            inflate.getLayoutParams().width = Utilities.pxFromDp(16.0f, getResources().getDisplayMetrics());
            viewGroup2.addView(inflate, 0);
            return;
        }
        ((LinearLayout.LayoutParams) viewGroup.getLayoutParams()).gravity = 1;
    }

    private void addDivider(ViewGroup viewGroup) {
        LayoutInflater.from(getContext()).inflate(R.layout.widget_list_divider, viewGroup, true);
    }

    private WidgetCell addItemCell(ViewGroup viewGroup) {
        WidgetCell widgetCell = (WidgetCell) LayoutInflater.from(getContext()).inflate(R.layout.widget_cell, viewGroup, false);
        widgetCell.setOnClickListener(this);
        widgetCell.setOnLongClickListener(this);
        widgetCell.setAnimatePreview(false);
        viewGroup.addView(widgetCell);
        return widgetCell;
    }

    private void animateOpen() {
        if (this.mIsOpen || this.mOpenCloseAnimator.isRunning()) {
            return;
        }
        this.mIsOpen = true;
        setupNavBarColor();
        this.mOpenCloseAnimator.setValues(PropertyValuesHolder.ofFloat(TRANSLATION_SHIFT, 0.0f));
        this.mOpenCloseAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mOpenCloseAnimator.start();
    }

    @Override // com.android.launcher3.AbstractFloatingView
    protected void handleClose(boolean z) {
        handleClose(z, 200L);
    }

    @Override // com.android.launcher3.AbstractFloatingView
    protected boolean isOfType(int i) {
        return (i & 4) != 0;
    }

    @Override // com.android.launcher3.Insettable
    public void setInsets(Rect rect) {
        int i = rect.left - this.mInsets.left;
        int i2 = rect.right - this.mInsets.right;
        int i3 = rect.bottom - this.mInsets.bottom;
        this.mInsets.set(rect);
        setPadding(getPaddingLeft() + i, getPaddingTop(), getPaddingRight() + i2, getPaddingBottom() + i3);
    }

    @Override // com.android.launcher3.widget.BaseWidgetSheet
    protected int getElementsRowCount() {
        return 1;
    }

    @Override // com.android.launcher3.AbstractFloatingView
    protected Pair<View, String> getAccessibilityTarget() {
        return Pair.create(findViewById(R.id.title), getContext().getString(this.mIsOpen ? R.string.widgets_list : R.string.widgets_list_closed));
    }
}
