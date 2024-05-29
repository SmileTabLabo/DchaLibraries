package com.android.settings.localepicker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.android.settings.R;
/* loaded from: classes.dex */
public class LocaleLinearLayoutManager extends LinearLayoutManager {
    private final AccessibilityNodeInfoCompat.AccessibilityActionCompat mActionMoveBottom;
    private final AccessibilityNodeInfoCompat.AccessibilityActionCompat mActionMoveDown;
    private final AccessibilityNodeInfoCompat.AccessibilityActionCompat mActionMoveTop;
    private final AccessibilityNodeInfoCompat.AccessibilityActionCompat mActionMoveUp;
    private final AccessibilityNodeInfoCompat.AccessibilityActionCompat mActionRemove;
    private final LocaleDragAndDropAdapter mAdapter;
    private final Context mContext;

    public LocaleLinearLayoutManager(Context context, LocaleDragAndDropAdapter adapter) {
        super(context);
        this.mContext = context;
        this.mAdapter = adapter;
        this.mActionMoveUp = new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.id.action_drag_move_up, this.mContext.getString(R.string.action_drag_label_move_up));
        this.mActionMoveDown = new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.id.action_drag_move_down, this.mContext.getString(R.string.action_drag_label_move_down));
        this.mActionMoveTop = new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.id.action_drag_move_top, this.mContext.getString(R.string.action_drag_label_move_top));
        this.mActionMoveBottom = new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.id.action_drag_move_bottom, this.mContext.getString(R.string.action_drag_label_move_bottom));
        this.mActionRemove = new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.id.action_drag_remove, this.mContext.getString(R.string.action_drag_label_remove));
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onInitializeAccessibilityNodeInfoForItem(RecyclerView.Recycler recycler, RecyclerView.State state, View host, AccessibilityNodeInfoCompat info) {
        super.onInitializeAccessibilityNodeInfoForItem(recycler, state, host, info);
        int itemCount = getItemCount();
        int position = getPosition(host);
        LocaleDragCell dragCell = (LocaleDragCell) host;
        String description = (position + 1) + ", " + dragCell.getCheckbox().getContentDescription();
        info.setContentDescription(description);
        if (this.mAdapter.isRemoveMode()) {
            return;
        }
        if (position > 0) {
            info.addAction(this.mActionMoveUp);
            info.addAction(this.mActionMoveTop);
        }
        if (position + 1 < itemCount) {
            info.addAction(this.mActionMoveDown);
            info.addAction(this.mActionMoveBottom);
        }
        if (itemCount <= 1) {
            return;
        }
        info.addAction(this.mActionRemove);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public boolean performAccessibilityActionForItem(RecyclerView.Recycler recycler, RecyclerView.State state, View host, int action, Bundle args) {
        int itemCount = getItemCount();
        int position = getPosition(host);
        boolean result = false;
        switch (action) {
            case R.id.action_drag_move_up /* 2131886095 */:
                if (position > 0) {
                    this.mAdapter.onItemMove(position, position - 1);
                    result = true;
                    break;
                }
                break;
            case R.id.action_drag_move_down /* 2131886096 */:
                if (position + 1 < itemCount) {
                    this.mAdapter.onItemMove(position, position + 1);
                    result = true;
                    break;
                }
                break;
            case R.id.action_drag_move_top /* 2131886097 */:
                if (position != 0) {
                    this.mAdapter.onItemMove(position, 0);
                    result = true;
                    break;
                }
                break;
            case R.id.action_drag_move_bottom /* 2131886098 */:
                if (position != itemCount - 1) {
                    this.mAdapter.onItemMove(position, itemCount - 1);
                    result = true;
                    break;
                }
                break;
            case R.id.action_drag_remove /* 2131886099 */:
                if (itemCount > 1) {
                    this.mAdapter.removeItem(position);
                    result = true;
                    break;
                }
                break;
            default:
                return super.performAccessibilityActionForItem(recycler, state, host, action, args);
        }
        if (result) {
            this.mAdapter.doTheUpdate();
        }
        return result;
    }
}
