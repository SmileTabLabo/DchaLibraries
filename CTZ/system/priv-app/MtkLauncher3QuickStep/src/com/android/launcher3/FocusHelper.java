package com.android.launcher3;

import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import com.android.launcher3.CellLayout;
import com.android.launcher3.folder.Folder;
import com.android.launcher3.folder.FolderPagedView;
import com.android.launcher3.util.FocusLogic;
/* loaded from: classes.dex */
public class FocusHelper {
    private static final boolean DEBUG = false;
    private static final String TAG = "FocusHelper";

    /* loaded from: classes.dex */
    public static class PagedFolderKeyEventListener implements View.OnKeyListener {
        private final Folder mFolder;

        public PagedFolderKeyEventListener(Folder folder) {
            this.mFolder = folder;
        }

        @Override // android.view.View.OnKeyListener
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            boolean shouldConsume = FocusLogic.shouldConsume(i);
            if (keyEvent.getAction() == 1) {
                return shouldConsume;
            }
            if (view.getParent() instanceof ShortcutAndWidgetContainer) {
                ShortcutAndWidgetContainer shortcutAndWidgetContainer = (ShortcutAndWidgetContainer) view.getParent();
                CellLayout cellLayout = (CellLayout) shortcutAndWidgetContainer.getParent();
                int indexOfChild = shortcutAndWidgetContainer.indexOfChild(view);
                FolderPagedView folderPagedView = (FolderPagedView) cellLayout.getParent();
                int indexOfChild2 = folderPagedView.indexOfChild(cellLayout);
                int pageCount = folderPagedView.getPageCount();
                boolean isRtl = Utilities.isRtl(view.getResources());
                int[][] createSparseMatrix = FocusLogic.createSparseMatrix(cellLayout);
                int handleKeyEvent = FocusLogic.handleKeyEvent(i, createSparseMatrix, indexOfChild, indexOfChild2, pageCount, isRtl);
                if (handleKeyEvent == -1) {
                    handleNoopKey(i, view);
                    return shouldConsume;
                }
                View view2 = null;
                switch (handleKeyEvent) {
                    case FocusLogic.NEXT_PAGE_RIGHT_COLUMN /* -10 */:
                    case FocusLogic.NEXT_PAGE_LEFT_COLUMN /* -9 */:
                        int i2 = indexOfChild2 + 1;
                        ShortcutAndWidgetContainer cellLayoutChildrenForIndex = FocusHelper.getCellLayoutChildrenForIndex(folderPagedView, i2);
                        if (cellLayoutChildrenForIndex != null) {
                            folderPagedView.snapToPage(i2);
                            view2 = FocusLogic.getAdjacentChildInNextFolderPage(cellLayoutChildrenForIndex, view, handleKeyEvent);
                            break;
                        }
                        break;
                    case FocusLogic.NEXT_PAGE_FIRST_ITEM /* -8 */:
                        int i3 = indexOfChild2 + 1;
                        ShortcutAndWidgetContainer cellLayoutChildrenForIndex2 = FocusHelper.getCellLayoutChildrenForIndex(folderPagedView, i3);
                        if (cellLayoutChildrenForIndex2 != null) {
                            folderPagedView.snapToPage(i3);
                            view2 = cellLayoutChildrenForIndex2.getChildAt(0, 0);
                            break;
                        }
                        break;
                    case FocusLogic.CURRENT_PAGE_LAST_ITEM /* -7 */:
                        view2 = folderPagedView.getLastItem();
                        break;
                    case FocusLogic.CURRENT_PAGE_FIRST_ITEM /* -6 */:
                        view2 = cellLayout.getChildAt(0, 0);
                        break;
                    case FocusLogic.PREVIOUS_PAGE_LEFT_COLUMN /* -5 */:
                    case -2:
                        int i4 = indexOfChild2 - 1;
                        ShortcutAndWidgetContainer cellLayoutChildrenForIndex3 = FocusHelper.getCellLayoutChildrenForIndex(folderPagedView, i4);
                        if (cellLayoutChildrenForIndex3 != null) {
                            int i5 = ((CellLayout.LayoutParams) view.getLayoutParams()).cellY;
                            folderPagedView.snapToPage(i4);
                            view2 = cellLayoutChildrenForIndex3.getChildAt((handleKeyEvent == -5) ^ cellLayoutChildrenForIndex3.invertLayoutHorizontally() ? 0 : createSparseMatrix.length - 1, i5);
                            break;
                        }
                        break;
                    case -4:
                        int i6 = indexOfChild2 - 1;
                        ShortcutAndWidgetContainer cellLayoutChildrenForIndex4 = FocusHelper.getCellLayoutChildrenForIndex(folderPagedView, i6);
                        if (cellLayoutChildrenForIndex4 != null) {
                            folderPagedView.snapToPage(i6);
                            view2 = cellLayoutChildrenForIndex4.getChildAt(createSparseMatrix.length - 1, createSparseMatrix[0].length - 1);
                            break;
                        }
                        break;
                    case -3:
                        int i7 = indexOfChild2 - 1;
                        ShortcutAndWidgetContainer cellLayoutChildrenForIndex5 = FocusHelper.getCellLayoutChildrenForIndex(folderPagedView, i7);
                        if (cellLayoutChildrenForIndex5 != null) {
                            folderPagedView.snapToPage(i7);
                            view2 = cellLayoutChildrenForIndex5.getChildAt(0, 0);
                            break;
                        }
                        break;
                    default:
                        view2 = shortcutAndWidgetContainer.getChildAt(handleKeyEvent);
                        break;
                }
                if (view2 != null) {
                    view2.requestFocus();
                    FocusHelper.playSoundEffect(i, view);
                } else {
                    handleNoopKey(i, view);
                }
                return shouldConsume;
            }
            return false;
        }

        public void handleNoopKey(int i, View view) {
            if (i == 20) {
                this.mFolder.mFolderName.requestFocus();
                FocusHelper.playSoundEffect(i, view);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Removed duplicated region for block: B:29:0x00c3  */
    /* JADX WARN: Removed duplicated region for block: B:30:0x00d0  */
    /* JADX WARN: Removed duplicated region for block: B:31:0x00e2  */
    /* JADX WARN: Removed duplicated region for block: B:32:0x00e7  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x00f4  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x0107  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean handleHotseatButtonKeyEvent(View view, int i, KeyEvent keyEvent) {
        int[][] createSparseMatrix;
        int i2;
        int[][] createSparseMatrixWithHotseat;
        int childCount;
        int handleKeyEvent;
        int i3 = i;
        boolean shouldConsume = FocusLogic.shouldConsume(i);
        if (keyEvent.getAction() == 1 || !shouldConsume) {
            return shouldConsume;
        }
        DeviceProfile deviceProfile = Launcher.getLauncher(view.getContext()).getDeviceProfile();
        Workspace workspace = (Workspace) view.getRootView().findViewById(R.id.workspace);
        ShortcutAndWidgetContainer shortcutAndWidgetContainer = (ShortcutAndWidgetContainer) view.getParent();
        CellLayout cellLayout = (CellLayout) shortcutAndWidgetContainer.getParent();
        ItemInfo itemInfo = (ItemInfo) view.getTag();
        int nextPage = workspace.getNextPage();
        int childCount2 = workspace.getChildCount();
        int indexOfChild = shortcutAndWidgetContainer.indexOfChild(view);
        int i4 = ((CellLayout.LayoutParams) cellLayout.getShortcutsAndWidgets().getChildAt(indexOfChild).getLayoutParams()).cellX;
        CellLayout cellLayout2 = (CellLayout) workspace.getChildAt(nextPage);
        if (cellLayout2 == null) {
            return shouldConsume;
        }
        ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout2.getShortcutsAndWidgets();
        View view2 = null;
        if (i3 == 19 && !deviceProfile.isVerticalBarLayout()) {
            createSparseMatrixWithHotseat = FocusLogic.createSparseMatrixWithHotseat(cellLayout2, cellLayout, deviceProfile);
            childCount = indexOfChild + shortcutsAndWidgets.getChildCount();
        } else if (i3 != 21 || !deviceProfile.isVerticalBarLayout()) {
            if (i3 == 22 && deviceProfile.isVerticalBarLayout()) {
                i3 = 93;
                i2 = indexOfChild;
                shortcutAndWidgetContainer = null;
                createSparseMatrix = null;
            } else {
                createSparseMatrix = FocusLogic.createSparseMatrix(cellLayout);
                i2 = indexOfChild;
            }
            handleKeyEvent = FocusLogic.handleKeyEvent(i3, createSparseMatrix, i2, nextPage, childCount2, Utilities.isRtl(view.getResources()));
            switch (handleKeyEvent) {
                case FocusLogic.NEXT_PAGE_RIGHT_COLUMN /* -10 */:
                case FocusLogic.NEXT_PAGE_LEFT_COLUMN /* -9 */:
                    workspace.snapToPage(nextPage + 1);
                    break;
                case FocusLogic.NEXT_PAGE_FIRST_ITEM /* -8 */:
                    int i5 = nextPage + 1;
                    shortcutAndWidgetContainer = getCellLayoutChildrenForIndex(workspace, i5);
                    view2 = shortcutAndWidgetContainer.getChildAt(0);
                    workspace.snapToPage(i5);
                    break;
                case FocusLogic.PREVIOUS_PAGE_LEFT_COLUMN /* -5 */:
                case -2:
                    workspace.snapToPage(nextPage - 1);
                    break;
                case -4:
                    int i6 = nextPage - 1;
                    shortcutAndWidgetContainer = getCellLayoutChildrenForIndex(workspace, i6);
                    view2 = shortcutAndWidgetContainer.getChildAt(shortcutAndWidgetContainer.getChildCount() - 1);
                    workspace.snapToPage(i6);
                    break;
                case -3:
                    int i7 = nextPage - 1;
                    shortcutAndWidgetContainer = getCellLayoutChildrenForIndex(workspace, i7);
                    view2 = shortcutAndWidgetContainer.getChildAt(0);
                    workspace.snapToPage(i7);
                    break;
            }
            if (shortcutAndWidgetContainer == shortcutsAndWidgets && handleKeyEvent >= shortcutsAndWidgets.getChildCount()) {
                handleKeyEvent -= shortcutsAndWidgets.getChildCount();
            }
            if (shortcutAndWidgetContainer != null) {
                if (view2 == null && handleKeyEvent >= 0) {
                    view2 = shortcutAndWidgetContainer.getChildAt(handleKeyEvent);
                }
                View view3 = view2;
                if (view3 != null) {
                    view3.requestFocus();
                    playSoundEffect(i3, view);
                }
            }
            return shouldConsume;
        } else {
            createSparseMatrixWithHotseat = FocusLogic.createSparseMatrixWithHotseat(cellLayout2, cellLayout, deviceProfile);
            childCount = indexOfChild + shortcutsAndWidgets.getChildCount();
        }
        createSparseMatrix = createSparseMatrixWithHotseat;
        i2 = childCount;
        shortcutAndWidgetContainer = shortcutsAndWidgets;
        handleKeyEvent = FocusLogic.handleKeyEvent(i3, createSparseMatrix, i2, nextPage, childCount2, Utilities.isRtl(view.getResources()));
        switch (handleKeyEvent) {
        }
        if (shortcutAndWidgetContainer == shortcutsAndWidgets) {
            handleKeyEvent -= shortcutsAndWidgets.getChildCount();
        }
        if (shortcutAndWidgetContainer != null) {
        }
        return shouldConsume;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Code restructure failed: missing block: B:25:0x00c7, code lost:
        if (r20 == 19) goto L16;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean handleIconKeyEvent(View view, int i, KeyEvent keyEvent) {
        int[][] createSparseMatrix;
        boolean shouldConsume = FocusLogic.shouldConsume(i);
        if (keyEvent.getAction() == 1 || !shouldConsume) {
            return shouldConsume;
        }
        DeviceProfile deviceProfile = Launcher.getLauncher(view.getContext()).getDeviceProfile();
        ShortcutAndWidgetContainer shortcutAndWidgetContainer = (ShortcutAndWidgetContainer) view.getParent();
        CellLayout cellLayout = (CellLayout) shortcutAndWidgetContainer.getParent();
        Workspace workspace = (Workspace) cellLayout.getParent();
        ViewGroup viewGroup = (ViewGroup) workspace.getParent();
        View view2 = (ViewGroup) viewGroup.findViewById(R.id.drop_target_bar);
        ItemInfo itemInfo = (ItemInfo) view.getTag();
        int indexOfChild = shortcutAndWidgetContainer.indexOfChild(view);
        int indexOfChild2 = workspace.indexOfChild(cellLayout);
        int childCount = workspace.getChildCount();
        CellLayout cellLayout2 = (CellLayout) ((Hotseat) viewGroup.findViewById(R.id.hotseat)).getChildAt(0);
        ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout2.getShortcutsAndWidgets();
        if (i == 20 && !deviceProfile.isVerticalBarLayout()) {
            createSparseMatrix = FocusLogic.createSparseMatrixWithHotseat(cellLayout, cellLayout2, deviceProfile);
        } else if (i == 22 && deviceProfile.isVerticalBarLayout()) {
            createSparseMatrix = FocusLogic.createSparseMatrixWithHotseat(cellLayout, cellLayout2, deviceProfile);
        } else {
            createSparseMatrix = FocusLogic.createSparseMatrix(cellLayout);
        }
        int handleKeyEvent = FocusLogic.handleKeyEvent(i, createSparseMatrix, indexOfChild, indexOfChild2, childCount, Utilities.isRtl(view.getResources()));
        boolean isRtl = Utilities.isRtl(view.getResources());
        CellLayout cellLayout3 = (CellLayout) workspace.getChildAt(indexOfChild2);
        switch (handleKeyEvent) {
            case FocusLogic.NEXT_PAGE_RIGHT_COLUMN /* -10 */:
            case -2:
                int i2 = handleKeyEvent == -10 ? indexOfChild2 + 1 : indexOfChild2 - 1;
                int i3 = ((CellLayout.LayoutParams) view.getLayoutParams()).cellY;
                ShortcutAndWidgetContainer cellLayoutChildrenForIndex = getCellLayoutChildrenForIndex(workspace, i2);
                if (cellLayoutChildrenForIndex != null) {
                    CellLayout cellLayout4 = (CellLayout) cellLayoutChildrenForIndex.getParent();
                    int handleKeyEvent2 = FocusLogic.handleKeyEvent(i, FocusLogic.createSparseMatrixWithPivotColumn(cellLayout4, cellLayout4.getCountX(), i3), 100, i2, childCount, Utilities.isRtl(view.getResources()));
                    if (handleKeyEvent2 == -8) {
                        view2 = handleNextPageFirstItem(workspace, cellLayout2, indexOfChild2, isRtl);
                        break;
                    } else if (handleKeyEvent2 == -4) {
                        view2 = handlePreviousPageLastItem(workspace, cellLayout2, indexOfChild2, isRtl);
                        break;
                    } else {
                        view2 = cellLayoutChildrenForIndex.getChildAt(handleKeyEvent2);
                        break;
                    }
                }
                view2 = null;
                break;
            case FocusLogic.NEXT_PAGE_LEFT_COLUMN /* -9 */:
            case FocusLogic.PREVIOUS_PAGE_LEFT_COLUMN /* -5 */:
                int i4 = handleKeyEvent == -5 ? indexOfChild2 - 1 : indexOfChild2 + 1;
                int i5 = ((CellLayout.LayoutParams) view.getLayoutParams()).cellY;
                ShortcutAndWidgetContainer cellLayoutChildrenForIndex2 = getCellLayoutChildrenForIndex(workspace, i4);
                if (cellLayoutChildrenForIndex2 != null) {
                    int handleKeyEvent3 = FocusLogic.handleKeyEvent(i, FocusLogic.createSparseMatrixWithPivotColumn((CellLayout) cellLayoutChildrenForIndex2.getParent(), -1, i5), 100, i4, childCount, Utilities.isRtl(view.getResources()));
                    if (handleKeyEvent3 == -8) {
                        view2 = handleNextPageFirstItem(workspace, cellLayout2, indexOfChild2, isRtl);
                        break;
                    } else if (handleKeyEvent3 == -4) {
                        view2 = handlePreviousPageLastItem(workspace, cellLayout2, indexOfChild2, isRtl);
                        break;
                    } else {
                        view2 = cellLayoutChildrenForIndex2.getChildAt(handleKeyEvent3);
                        break;
                    }
                }
                view2 = null;
                break;
            case FocusLogic.NEXT_PAGE_FIRST_ITEM /* -8 */:
                view2 = handleNextPageFirstItem(workspace, cellLayout2, indexOfChild2, isRtl);
                break;
            case FocusLogic.CURRENT_PAGE_LAST_ITEM /* -7 */:
                view2 = getFirstFocusableIconInReverseReadingOrder(cellLayout3, isRtl);
                if (view2 == null) {
                    view2 = getFirstFocusableIconInReverseReadingOrder(cellLayout2, isRtl);
                    break;
                }
                break;
            case FocusLogic.CURRENT_PAGE_FIRST_ITEM /* -6 */:
                view2 = getFirstFocusableIconInReadingOrder(cellLayout3, isRtl);
                if (view2 == null) {
                    view2 = getFirstFocusableIconInReadingOrder(cellLayout2, isRtl);
                    break;
                }
                break;
            case -4:
                view2 = handlePreviousPageLastItem(workspace, cellLayout2, indexOfChild2, isRtl);
                break;
            case -3:
                int i6 = indexOfChild2 - 1;
                view2 = getFirstFocusableIconInReadingOrder((CellLayout) workspace.getChildAt(i6), isRtl);
                if (view2 == null) {
                    view2 = getFirstFocusableIconInReadingOrder(cellLayout2, isRtl);
                    workspace.snapToPage(i6);
                    break;
                }
                break;
            case -1:
                break;
            default:
                if (handleKeyEvent >= 0 && handleKeyEvent < shortcutAndWidgetContainer.getChildCount()) {
                    view2 = shortcutAndWidgetContainer.getChildAt(handleKeyEvent);
                    break;
                } else {
                    if (shortcutAndWidgetContainer.getChildCount() <= handleKeyEvent && handleKeyEvent < shortcutAndWidgetContainer.getChildCount() + shortcutsAndWidgets.getChildCount()) {
                        view2 = shortcutsAndWidgets.getChildAt(handleKeyEvent - shortcutAndWidgetContainer.getChildCount());
                        break;
                    }
                    view2 = null;
                    break;
                }
                break;
        }
        if (view2 != null) {
            view2.requestFocus();
            playSoundEffect(i, view);
        }
        return shouldConsume;
    }

    static ShortcutAndWidgetContainer getCellLayoutChildrenForIndex(ViewGroup viewGroup, int i) {
        return ((CellLayout) viewGroup.getChildAt(i)).getShortcutsAndWidgets();
    }

    static void playSoundEffect(int i, View view) {
        switch (i) {
            case 19:
            case 92:
            case 122:
                view.playSoundEffect(2);
                return;
            case 20:
            case 93:
            case 123:
                view.playSoundEffect(4);
                return;
            case 21:
                view.playSoundEffect(1);
                return;
            case 22:
                view.playSoundEffect(3);
                return;
            default:
                return;
        }
    }

    private static View handlePreviousPageLastItem(Workspace workspace, CellLayout cellLayout, int i, boolean z) {
        int i2 = i - 1;
        if (i2 < 0) {
            return null;
        }
        View firstFocusableIconInReverseReadingOrder = getFirstFocusableIconInReverseReadingOrder((CellLayout) workspace.getChildAt(i2), z);
        if (firstFocusableIconInReverseReadingOrder == null) {
            View firstFocusableIconInReverseReadingOrder2 = getFirstFocusableIconInReverseReadingOrder(cellLayout, z);
            workspace.snapToPage(i2);
            return firstFocusableIconInReverseReadingOrder2;
        }
        return firstFocusableIconInReverseReadingOrder;
    }

    private static View handleNextPageFirstItem(Workspace workspace, CellLayout cellLayout, int i, boolean z) {
        int i2 = i + 1;
        if (i2 >= workspace.getPageCount()) {
            return null;
        }
        View firstFocusableIconInReadingOrder = getFirstFocusableIconInReadingOrder((CellLayout) workspace.getChildAt(i2), z);
        if (firstFocusableIconInReadingOrder == null) {
            View firstFocusableIconInReadingOrder2 = getFirstFocusableIconInReadingOrder(cellLayout, z);
            workspace.snapToPage(i2);
            return firstFocusableIconInReadingOrder2;
        }
        return firstFocusableIconInReadingOrder;
    }

    private static View getFirstFocusableIconInReadingOrder(CellLayout cellLayout, boolean z) {
        int countX = cellLayout.getCountX();
        for (int i = 0; i < cellLayout.getCountY(); i++) {
            int i2 = z ? -1 : 1;
            for (int i3 = z ? countX - 1 : 0; i3 >= 0 && i3 < countX; i3 += i2) {
                View childAt = cellLayout.getChildAt(i3, i);
                if (childAt != null && childAt.isFocusable()) {
                    return childAt;
                }
            }
        }
        return null;
    }

    private static View getFirstFocusableIconInReverseReadingOrder(CellLayout cellLayout, boolean z) {
        int countX = cellLayout.getCountX();
        for (int countY = cellLayout.getCountY() - 1; countY >= 0; countY--) {
            int i = z ? 1 : -1;
            for (int i2 = z ? 0 : countX - 1; i2 >= 0 && i2 < countX; i2 += i) {
                View childAt = cellLayout.getChildAt(i2, countY);
                if (childAt != null && childAt.isFocusable()) {
                    return childAt;
                }
            }
        }
        return null;
    }
}
