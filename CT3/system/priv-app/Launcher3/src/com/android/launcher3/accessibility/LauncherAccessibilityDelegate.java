package com.android.launcher3.accessibility;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.appwidget.AppWidgetProviderInfo;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.launcher3.AppInfo;
import com.android.launcher3.AppWidgetResizeFrame;
import com.android.launcher3.CellLayout;
import com.android.launcher3.DeleteDropTarget;
import com.android.launcher3.DragController;
import com.android.launcher3.DragSource;
import com.android.launcher3.Folder;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.InfoDropTarget;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppWidgetHostView;
import com.android.launcher3.LauncherAppWidgetInfo;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.PendingAddItemInfo;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.UninstallDropTarget;
import com.android.launcher3.Workspace;
import java.util.ArrayList;
@TargetApi(21)
/* loaded from: a.zip:com/android/launcher3/accessibility/LauncherAccessibilityDelegate.class */
public class LauncherAccessibilityDelegate extends View.AccessibilityDelegate implements DragController.DragListener {
    private final SparseArray<AccessibilityNodeInfo.AccessibilityAction> mActions = new SparseArray<>();
    private DragInfo mDragInfo = null;
    private AccessibilityDragSource mDragSource = null;
    final Launcher mLauncher;

    /* loaded from: a.zip:com/android/launcher3/accessibility/LauncherAccessibilityDelegate$AccessibilityDragSource.class */
    public interface AccessibilityDragSource {
        void enableAccessibleDrag(boolean z);

        void startDrag(CellLayout.CellInfo cellInfo, boolean z);
    }

    /* loaded from: a.zip:com/android/launcher3/accessibility/LauncherAccessibilityDelegate$DragInfo.class */
    public static class DragInfo {
        public DragType dragType;
        public ItemInfo info;
        public View item;
    }

    /* loaded from: a.zip:com/android/launcher3/accessibility/LauncherAccessibilityDelegate$DragType.class */
    public enum DragType {
        ICON,
        FOLDER,
        WIDGET;

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static DragType[] valuesCustom() {
            return values();
        }
    }

    public LauncherAccessibilityDelegate(Launcher launcher) {
        this.mLauncher = launcher;
        this.mActions.put(2131296260, new AccessibilityNodeInfo.AccessibilityAction(2131296260, launcher.getText(2131558422)));
        this.mActions.put(2131296262, new AccessibilityNodeInfo.AccessibilityAction(2131296262, launcher.getText(2131558424)));
        this.mActions.put(2131296261, new AccessibilityNodeInfo.AccessibilityAction(2131296261, launcher.getText(2131558423)));
        this.mActions.put(2131296263, new AccessibilityNodeInfo.AccessibilityAction(2131296263, launcher.getText(2131558465)));
        this.mActions.put(2131296264, new AccessibilityNodeInfo.AccessibilityAction(2131296264, launcher.getText(2131558469)));
        this.mActions.put(2131296265, new AccessibilityNodeInfo.AccessibilityAction(2131296265, launcher.getText(2131558479)));
        this.mActions.put(2131296268, new AccessibilityNodeInfo.AccessibilityAction(2131296268, launcher.getText(2131558483)));
    }

    private long findSpaceOnWorkspace(ItemInfo itemInfo, int[] iArr) {
        Workspace workspace = this.mLauncher.getWorkspace();
        ArrayList<Long> screenOrder = workspace.getScreenOrder();
        int currentPage = workspace.getCurrentPage();
        long longValue = screenOrder.get(currentPage).longValue();
        boolean findCellForSpan = ((CellLayout) workspace.getPageAt(currentPage)).findCellForSpan(iArr, itemInfo.spanX, itemInfo.spanY);
        for (int i = workspace.hasCustomContent() ? 1 : 0; !findCellForSpan && i < screenOrder.size(); i++) {
            longValue = screenOrder.get(i).longValue();
            findCellForSpan = ((CellLayout) workspace.getPageAt(i)).findCellForSpan(iArr, itemInfo.spanX, itemInfo.spanY);
        }
        if (findCellForSpan) {
            return longValue;
        }
        workspace.addExtraEmptyScreen();
        long commitExtraEmptyScreen = workspace.commitExtraEmptyScreen();
        if (!workspace.getScreenWithId(commitExtraEmptyScreen).findCellForSpan(iArr, itemInfo.spanX, itemInfo.spanY)) {
            Log.wtf("LauncherAccessibilityDelegate", "Not enough space on an empty screen");
        }
        return commitExtraEmptyScreen;
    }

    private ArrayList<Integer> getSupportedResizeActions(View view, LauncherAppWidgetInfo launcherAppWidgetInfo) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        AppWidgetProviderInfo appWidgetInfo = ((LauncherAppWidgetHostView) view).getAppWidgetInfo();
        if (appWidgetInfo == null) {
            return arrayList;
        }
        CellLayout cellLayout = (CellLayout) view.getParent().getParent();
        if ((appWidgetInfo.resizeMode & 1) != 0) {
            if (cellLayout.isRegionVacant(launcherAppWidgetInfo.cellX + launcherAppWidgetInfo.spanX, launcherAppWidgetInfo.cellY, 1, launcherAppWidgetInfo.spanY) || cellLayout.isRegionVacant(launcherAppWidgetInfo.cellX - 1, launcherAppWidgetInfo.cellY, 1, launcherAppWidgetInfo.spanY)) {
                arrayList.add(2131558484);
            }
            if (launcherAppWidgetInfo.spanX > launcherAppWidgetInfo.minSpanX && launcherAppWidgetInfo.spanX > 1) {
                arrayList.add(2131558486);
            }
        }
        if ((appWidgetInfo.resizeMode & 2) != 0) {
            if (cellLayout.isRegionVacant(launcherAppWidgetInfo.cellX, launcherAppWidgetInfo.cellY + launcherAppWidgetInfo.spanY, launcherAppWidgetInfo.spanX, 1) || cellLayout.isRegionVacant(launcherAppWidgetInfo.cellX, launcherAppWidgetInfo.cellY - 1, launcherAppWidgetInfo.spanX, 1)) {
                arrayList.add(2131558485);
            }
            if (launcherAppWidgetInfo.spanY > launcherAppWidgetInfo.minSpanY && launcherAppWidgetInfo.spanY > 1) {
                arrayList.add(2131558487);
            }
        }
        return arrayList;
    }

    void announceConfirmation(int i) {
        announceConfirmation(this.mLauncher.getResources().getString(i));
    }

    void announceConfirmation(String str) {
        this.mLauncher.getDragLayer().announceForAccessibility(str);
    }

    public void beginAccessibleDrag(View view, ItemInfo itemInfo) {
        this.mDragInfo = new DragInfo();
        this.mDragInfo.info = itemInfo;
        this.mDragInfo.item = view;
        this.mDragInfo.dragType = DragType.ICON;
        if (itemInfo instanceof FolderInfo) {
            this.mDragInfo.dragType = DragType.FOLDER;
        } else if (itemInfo instanceof LauncherAppWidgetInfo) {
            this.mDragInfo.dragType = DragType.WIDGET;
        }
        CellLayout.CellInfo cellInfo = new CellLayout.CellInfo(view, itemInfo);
        Rect rect = new Rect();
        this.mLauncher.getDragLayer().getDescendantRectRelativeToSelf(view, rect);
        this.mLauncher.getDragController().prepareAccessibleDrag(rect.centerX(), rect.centerY());
        Workspace workspace = this.mLauncher.getWorkspace();
        Folder openFolder = workspace.getOpenFolder();
        if (openFolder != null) {
            if (openFolder.getItemsInReadingOrder().contains(view)) {
                this.mDragSource = openFolder;
            } else {
                this.mLauncher.closeFolder();
            }
        }
        if (this.mDragSource == null) {
            this.mDragSource = workspace;
        }
        this.mDragSource.enableAccessibleDrag(true);
        this.mDragSource.startDrag(cellInfo, true);
        if (this.mLauncher.getDragController().isDragging()) {
            this.mLauncher.getDragController().addDragListener(this);
        }
    }

    public DragInfo getDragInfo() {
        return this.mDragInfo;
    }

    public void handleAccessibleDrop(View view, Rect rect, String str) {
        if (isInAccessibleDrag()) {
            int[] iArr = new int[2];
            if (rect == null) {
                iArr[0] = view.getWidth() / 2;
                iArr[1] = view.getHeight() / 2;
            } else {
                iArr[0] = rect.centerX();
                iArr[1] = rect.centerY();
            }
            this.mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(view, iArr);
            this.mLauncher.getDragController().completeAccessibleDrag(iArr);
            if (TextUtils.isEmpty(str)) {
                return;
            }
            announceConfirmation(str);
        }
    }

    public boolean isInAccessibleDrag() {
        return this.mDragInfo != null;
    }

    @Override // com.android.launcher3.DragController.DragListener
    public void onDragEnd() {
        this.mLauncher.getDragController().removeDragListener(this);
        this.mDragInfo = null;
        if (this.mDragSource != null) {
            this.mDragSource.enableAccessibleDrag(false);
            this.mDragSource = null;
        }
    }

    @Override // com.android.launcher3.DragController.DragListener
    public void onDragStart(DragSource dragSource, Object obj, int i) {
    }

    @Override // android.view.View.AccessibilityDelegate
    public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
        if (view.getTag() instanceof ItemInfo) {
            ItemInfo itemInfo = (ItemInfo) view.getTag();
            if (DeleteDropTarget.supportsDrop(itemInfo)) {
                accessibilityNodeInfo.addAction(this.mActions.get(2131296260));
            }
            if (UninstallDropTarget.supportsDrop(view.getContext(), itemInfo)) {
                accessibilityNodeInfo.addAction(this.mActions.get(2131296261));
            }
            if (InfoDropTarget.supportsDrop(view.getContext(), itemInfo)) {
                accessibilityNodeInfo.addAction(this.mActions.get(2131296262));
            }
            if ((itemInfo instanceof ShortcutInfo) || (itemInfo instanceof LauncherAppWidgetInfo) || (itemInfo instanceof FolderInfo)) {
                accessibilityNodeInfo.addAction(this.mActions.get(2131296264));
                if (itemInfo.container >= 0) {
                    accessibilityNodeInfo.addAction(this.mActions.get(2131296265));
                } else if ((itemInfo instanceof LauncherAppWidgetInfo) && !getSupportedResizeActions(view, (LauncherAppWidgetInfo) itemInfo).isEmpty()) {
                    accessibilityNodeInfo.addAction(this.mActions.get(2131296268));
                }
            }
            if ((itemInfo instanceof AppInfo) || (itemInfo instanceof PendingAddItemInfo)) {
                accessibilityNodeInfo.addAction(this.mActions.get(2131296263));
            }
        }
    }

    @Override // android.view.View.AccessibilityDelegate
    public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
        if ((view.getTag() instanceof ItemInfo) && performAction(view, (ItemInfo) view.getTag(), i)) {
            return true;
        }
        return super.performAccessibilityAction(view, i, bundle);
    }

    public boolean performAction(View view, ItemInfo itemInfo, int i) {
        if (i == 2131296260) {
            DeleteDropTarget.removeWorkspaceOrFolderItem(this.mLauncher, itemInfo, view);
            return true;
        } else if (i == 2131296262) {
            InfoDropTarget.startDetailsActivityForInfo(itemInfo, this.mLauncher);
            return true;
        } else if (i == 2131296261) {
            return UninstallDropTarget.startUninstallActivity(this.mLauncher, itemInfo);
        } else {
            if (i == 2131296264) {
                beginAccessibleDrag(view, itemInfo);
                return false;
            } else if (i == 2131296263) {
                int[] iArr = new int[2];
                this.mLauncher.showWorkspace(true, new Runnable(this, itemInfo, findSpaceOnWorkspace(itemInfo, iArr), iArr) { // from class: com.android.launcher3.accessibility.LauncherAccessibilityDelegate.1
                    final LauncherAccessibilityDelegate this$0;
                    final int[] val$coordinates;
                    final ItemInfo val$item;
                    final long val$screenId;

                    {
                        this.this$0 = this;
                        this.val$item = itemInfo;
                        this.val$screenId = r7;
                        this.val$coordinates = iArr;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        if (this.val$item instanceof AppInfo) {
                            ShortcutInfo makeShortcut = ((AppInfo) this.val$item).makeShortcut();
                            LauncherModel.addItemToDatabase(this.this$0.mLauncher, makeShortcut, -100L, this.val$screenId, this.val$coordinates[0], this.val$coordinates[1]);
                            ArrayList<ItemInfo> arrayList = new ArrayList<>();
                            arrayList.add(makeShortcut);
                            this.this$0.mLauncher.bindItems(arrayList, 0, arrayList.size(), true);
                        } else if (this.val$item instanceof PendingAddItemInfo) {
                            PendingAddItemInfo pendingAddItemInfo = (PendingAddItemInfo) this.val$item;
                            Workspace workspace = this.this$0.mLauncher.getWorkspace();
                            workspace.snapToPage(workspace.getPageIndexForScreenId(this.val$screenId));
                            this.this$0.mLauncher.addPendingItem(pendingAddItemInfo, -100L, this.val$screenId, this.val$coordinates, pendingAddItemInfo.spanX, pendingAddItemInfo.spanY);
                        }
                        this.this$0.announceConfirmation(2131558467);
                    }
                });
                return true;
            } else if (i == 2131296265) {
                Folder openFolder = this.mLauncher.getWorkspace().getOpenFolder();
                this.mLauncher.closeFolder(openFolder, true);
                ShortcutInfo shortcutInfo = (ShortcutInfo) itemInfo;
                openFolder.getInfo().remove(shortcutInfo);
                int[] iArr2 = new int[2];
                LauncherModel.moveItemInDatabase(this.mLauncher, shortcutInfo, -100L, findSpaceOnWorkspace(itemInfo, iArr2), iArr2[0], iArr2[1]);
                new Handler().post(new Runnable(this, itemInfo) { // from class: com.android.launcher3.accessibility.LauncherAccessibilityDelegate.2
                    final LauncherAccessibilityDelegate this$0;
                    final ItemInfo val$item;

                    {
                        this.this$0 = this;
                        this.val$item = itemInfo;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        ArrayList<ItemInfo> arrayList = new ArrayList<>();
                        arrayList.add(this.val$item);
                        this.this$0.mLauncher.bindItems(arrayList, 0, arrayList.size(), true);
                        this.this$0.announceConfirmation(2131558473);
                    }
                });
                return false;
            } else if (i == 2131296268) {
                LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) itemInfo;
                ArrayList<Integer> supportedResizeActions = getSupportedResizeActions(view, launcherAppWidgetInfo);
                CharSequence[] charSequenceArr = new CharSequence[supportedResizeActions.size()];
                for (int i2 = 0; i2 < supportedResizeActions.size(); i2++) {
                    charSequenceArr[i2] = this.mLauncher.getText(supportedResizeActions.get(i2).intValue());
                }
                new AlertDialog.Builder(this.mLauncher).setTitle(2131558483).setItems(charSequenceArr, new DialogInterface.OnClickListener(this, supportedResizeActions, view, launcherAppWidgetInfo) { // from class: com.android.launcher3.accessibility.LauncherAccessibilityDelegate.3
                    final LauncherAccessibilityDelegate this$0;
                    final ArrayList val$actions;
                    final View val$host;
                    final LauncherAppWidgetInfo val$info;

                    {
                        this.this$0 = this;
                        this.val$actions = supportedResizeActions;
                        this.val$host = view;
                        this.val$info = launcherAppWidgetInfo;
                    }

                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i3) {
                        this.this$0.performResizeAction(((Integer) this.val$actions.get(i3)).intValue(), this.val$host, this.val$info);
                        dialogInterface.dismiss();
                    }
                }).show();
                return false;
            } else {
                return false;
            }
        }
    }

    void performResizeAction(int i, View view, LauncherAppWidgetInfo launcherAppWidgetInfo) {
        CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) view.getLayoutParams();
        CellLayout cellLayout = (CellLayout) view.getParent().getParent();
        cellLayout.markCellsAsUnoccupiedForView(view);
        if (i == 2131558484) {
            if ((view.getLayoutDirection() == 1 && cellLayout.isRegionVacant(launcherAppWidgetInfo.cellX - 1, launcherAppWidgetInfo.cellY, 1, launcherAppWidgetInfo.spanY)) || !cellLayout.isRegionVacant(launcherAppWidgetInfo.cellX + launcherAppWidgetInfo.spanX, launcherAppWidgetInfo.cellY, 1, launcherAppWidgetInfo.spanY)) {
                layoutParams.cellX--;
                launcherAppWidgetInfo.cellX--;
            }
            layoutParams.cellHSpan++;
            launcherAppWidgetInfo.spanX++;
        } else if (i == 2131558486) {
            layoutParams.cellHSpan--;
            launcherAppWidgetInfo.spanX--;
        } else if (i == 2131558485) {
            if (!cellLayout.isRegionVacant(launcherAppWidgetInfo.cellX, launcherAppWidgetInfo.cellY + launcherAppWidgetInfo.spanY, launcherAppWidgetInfo.spanX, 1)) {
                layoutParams.cellY--;
                launcherAppWidgetInfo.cellY--;
            }
            layoutParams.cellVSpan++;
            launcherAppWidgetInfo.spanY++;
        } else if (i == 2131558487) {
            layoutParams.cellVSpan--;
            launcherAppWidgetInfo.spanY--;
        }
        cellLayout.markCellsAsOccupiedForView(view);
        Rect rect = new Rect();
        AppWidgetResizeFrame.getWidgetSizeRanges(this.mLauncher, launcherAppWidgetInfo.spanX, launcherAppWidgetInfo.spanY, rect);
        ((LauncherAppWidgetHostView) view).updateAppWidgetSize(null, rect.left, rect.top, rect.right, rect.bottom);
        view.requestLayout();
        LauncherModel.updateItemInDatabase(this.mLauncher, launcherAppWidgetInfo);
        announceConfirmation(this.mLauncher.getString(2131558488, new Object[]{Integer.valueOf(launcherAppWidgetInfo.spanX), Integer.valueOf(launcherAppWidgetInfo.spanY)}));
    }
}
