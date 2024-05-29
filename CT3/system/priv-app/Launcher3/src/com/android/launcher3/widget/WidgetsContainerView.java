package com.android.launcher3.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.android.launcher3.BaseContainerView;
import com.android.launcher3.CellLayout;
import com.android.launcher3.DeleteDropTarget;
import com.android.launcher3.DragController;
import com.android.launcher3.DragSource;
import com.android.launcher3.DropTarget;
import com.android.launcher3.Folder;
import com.android.launcher3.IconCache;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.PendingAddItemInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.WidgetPreviewLoader;
import com.android.launcher3.Workspace;
import com.android.launcher3.model.WidgetsModel;
/* loaded from: a.zip:com/android/launcher3/widget/WidgetsContainerView.class */
public class WidgetsContainerView extends BaseContainerView implements View.OnLongClickListener, View.OnClickListener, DragSource {
    private WidgetsListAdapter mAdapter;
    private DragController mDragController;
    private IconCache mIconCache;
    Launcher mLauncher;
    private WidgetsRecyclerView mRecyclerView;
    private Toast mWidgetInstructionToast;
    private WidgetPreviewLoader mWidgetPreviewLoader;

    public WidgetsContainerView(Context context) {
        this(context, null);
    }

    public WidgetsContainerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public WidgetsContainerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mLauncher = (Launcher) context;
        this.mDragController = this.mLauncher.getDragController();
        this.mAdapter = new WidgetsListAdapter(context, this, this, this.mLauncher);
        this.mIconCache = LauncherAppState.getInstance().getIconCache();
    }

    private boolean beginDragging(View view) {
        if (!(view instanceof WidgetCell)) {
            Log.e("WidgetsContainerView", "Unexpected dragging view: " + view);
        } else if (!beginDraggingWidget((WidgetCell) view)) {
            return false;
        }
        if (this.mLauncher.getDragController().isDragging()) {
            this.mLauncher.enterSpringLoadedDragMode();
            return true;
        }
        return true;
    }

    private boolean beginDraggingWidget(WidgetCell widgetCell) {
        Bitmap createIconBitmap;
        float width;
        WidgetImageView widgetImageView = (WidgetImageView) widgetCell.findViewById(2131296329);
        PendingAddItemInfo pendingAddItemInfo = (PendingAddItemInfo) widgetCell.getTag();
        if (widgetImageView.getBitmap() == null) {
            return false;
        }
        Rect bitmapBounds = widgetImageView.getBitmapBounds();
        if (pendingAddItemInfo instanceof PendingAddWidgetInfo) {
            PendingAddWidgetInfo pendingAddWidgetInfo = (PendingAddWidgetInfo) pendingAddItemInfo;
            int[] estimateItemSize = this.mLauncher.getWorkspace().estimateItemSize(pendingAddWidgetInfo, true);
            Bitmap bitmap = widgetImageView.getBitmap();
            int min = Math.min((int) (bitmap.getWidth() * 1.25f), estimateItemSize[0]);
            int[] iArr = new int[1];
            createIconBitmap = getWidgetPreviewLoader().generateWidgetPreview(this.mLauncher, pendingAddWidgetInfo.info, min, null, iArr);
            if (iArr[0] < bitmap.getWidth()) {
                int width2 = (bitmap.getWidth() - iArr[0]) / 2;
                int i = width2;
                if (bitmap.getWidth() > widgetImageView.getWidth()) {
                    i = (widgetImageView.getWidth() * width2) / bitmap.getWidth();
                }
                bitmapBounds.left += i;
                bitmapBounds.right -= i;
            }
            width = bitmapBounds.width() / createIconBitmap.getWidth();
        } else {
            createIconBitmap = Utilities.createIconBitmap(this.mIconCache.getFullResIcon(((PendingAddShortcutInfo) widgetCell.getTag()).activityInfo), this.mLauncher);
            pendingAddItemInfo.spanY = 1;
            pendingAddItemInfo.spanX = 1;
            width = this.mLauncher.getDeviceProfile().iconSizePx / createIconBitmap.getWidth();
        }
        boolean z = ((pendingAddItemInfo instanceof PendingAddWidgetInfo) && ((PendingAddWidgetInfo) pendingAddItemInfo).previewImage == 0) ? false : true;
        this.mLauncher.lockScreenOrientation();
        this.mLauncher.getWorkspace().onDragStartedWithItem(pendingAddItemInfo, createIconBitmap, z);
        this.mDragController.startDrag(widgetImageView, createIconBitmap, this, pendingAddItemInfo, bitmapBounds, DragController.DRAG_ACTION_COPY, width);
        createIconBitmap.recycle();
        return true;
    }

    private WidgetPreviewLoader getWidgetPreviewLoader() {
        if (this.mWidgetPreviewLoader == null) {
            this.mWidgetPreviewLoader = LauncherAppState.getInstance().getWidgetCache();
        }
        return this.mWidgetPreviewLoader;
    }

    public void addWidgets(WidgetsModel widgetsModel) {
        this.mRecyclerView.setWidgets(widgetsModel);
        this.mAdapter.setWidgetsModel(widgetsModel);
        this.mAdapter.notifyDataSetChanged();
    }

    @Override // com.android.launcher3.DragSource
    public float getIntrinsicIconScaleFactor() {
        return 0.0f;
    }

    public boolean isEmpty() {
        boolean z = false;
        if (this.mAdapter.getItemCount() == 0) {
            z = true;
        }
        return z;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.mLauncher.isWidgetsViewVisible() && !this.mLauncher.getWorkspace().isSwitchingState() && (view instanceof WidgetCell)) {
            if (this.mWidgetInstructionToast != null) {
                this.mWidgetInstructionToast.cancel();
            }
            this.mWidgetInstructionToast = Toast.makeText(getContext(), Utilities.wrapForTts(getContext().getText(2131558411), getContext().getString(2131558412)), 0);
            this.mWidgetInstructionToast.show();
        }
    }

    @Override // com.android.launcher3.DragSource
    public void onDropCompleted(View view, DropTarget.DragObject dragObject, boolean z, boolean z2) {
        if (z || !z2 || (view != this.mLauncher.getWorkspace() && !(view instanceof DeleteDropTarget) && !(view instanceof Folder))) {
            this.mLauncher.exitSpringLoadedDragModeDelayed(true, 300, null);
        }
        this.mLauncher.unlockScreenOrientation(false);
        if (z2) {
            return;
        }
        boolean z3 = false;
        if (view instanceof Workspace) {
            CellLayout cellLayout = (CellLayout) ((Workspace) view).getChildAt(this.mLauncher.getCurrentWorkspaceScreen());
            ItemInfo itemInfo = (ItemInfo) dragObject.dragInfo;
            z3 = false;
            if (cellLayout != null) {
                z3 = !cellLayout.findCellForSpan(null, itemInfo.spanX, itemInfo.spanY);
            }
        }
        if (z3) {
            this.mLauncher.showOutOfSpaceMessage(false);
        }
        dragObject.deferDragViewCleanupPostAnimation = false;
    }

    @Override // com.android.launcher3.BaseContainerView, android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mRecyclerView = (WidgetsRecyclerView) getContentView().findViewById(2131296334);
        this.mRecyclerView.setAdapter(this.mAdapter);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override // com.android.launcher3.DragSource
    public void onFlingToDeleteCompleted() {
        this.mLauncher.exitSpringLoadedDragModeDelayed(true, 300, null);
        this.mLauncher.unlockScreenOrientation(false);
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        if (view.isInTouchMode() && this.mLauncher.isWidgetsViewVisible() && !this.mLauncher.getWorkspace().isSwitchingState() && this.mLauncher.isDraggingEnabled()) {
            boolean beginDragging = beginDragging(view);
            if (beginDragging && (view.getTag() instanceof PendingAddWidgetInfo)) {
                WidgetHostViewLoader widgetHostViewLoader = new WidgetHostViewLoader(this.mLauncher, view);
                widgetHostViewLoader.preloadWidget();
                this.mLauncher.getDragController().addDragListener(widgetHostViewLoader);
            }
            return beginDragging;
        }
        return false;
    }

    @Override // com.android.launcher3.BaseContainerView
    protected void onUpdateBgPadding(Rect rect, Rect rect2) {
        if (Utilities.isRtl(getResources())) {
            getContentView().setPadding(0, rect2.top, rect2.right, rect2.bottom);
            this.mRecyclerView.updateBackgroundPadding(new Rect(rect2.left, 0, 0, 0));
            return;
        }
        getContentView().setPadding(rect2.left, rect2.top, 0, rect2.bottom);
        this.mRecyclerView.updateBackgroundPadding(new Rect(0, 0, rect2.right, 0));
    }

    public void scrollToTop() {
        this.mRecyclerView.scrollToPosition(0);
    }

    @Override // com.android.launcher3.DragSource
    public boolean supportsAppInfoDropTarget() {
        return true;
    }

    @Override // com.android.launcher3.DragSource
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override // com.android.launcher3.DragSource
    public boolean supportsFlingToDelete() {
        return false;
    }
}
