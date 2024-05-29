package com.android.launcher3;

import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import com.android.launcher3.CellLayout;
import com.android.launcher3.util.FocusLogic;
/* loaded from: a.zip:com/android/launcher3/FocusHelper.class */
public class FocusHelper {

    /* loaded from: a.zip:com/android/launcher3/FocusHelper$PagedFolderKeyEventListener.class */
    public static class PagedFolderKeyEventListener implements View.OnKeyListener {
        private final Folder mFolder;

        public PagedFolderKeyEventListener(Folder folder) {
            this.mFolder = folder;
        }

        public void handleNoopKey(int i, View view) {
            if (i == 20) {
                this.mFolder.mFolderName.requestFocus();
                FocusHelper.playSoundEffect(i, view);
            }
        }

        @Override // android.view.View.OnKeyListener
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            boolean shouldConsume = FocusLogic.shouldConsume(i);
            if (keyEvent.getAction() == 1) {
                return shouldConsume;
            }
            if (!(view.getParent() instanceof ShortcutAndWidgetContainer)) {
                if (LauncherAppState.isDogfoodBuild()) {
                    throw new IllegalStateException("Parent of the focused item is not supported.");
                }
                return false;
            }
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
                case -10:
                case -9:
                    ShortcutAndWidgetContainer cellLayoutChildrenForIndex = FocusHelper.getCellLayoutChildrenForIndex(folderPagedView, indexOfChild2 + 1);
                    if (cellLayoutChildrenForIndex != null) {
                        folderPagedView.snapToPage(indexOfChild2 + 1);
                        view2 = FocusLogic.getAdjacentChildInNextFolderPage(cellLayoutChildrenForIndex, view, handleKeyEvent);
                        break;
                    }
                    break;
                case -8:
                    ShortcutAndWidgetContainer cellLayoutChildrenForIndex2 = FocusHelper.getCellLayoutChildrenForIndex(folderPagedView, indexOfChild2 + 1);
                    if (cellLayoutChildrenForIndex2 != null) {
                        folderPagedView.snapToPage(indexOfChild2 + 1);
                        view2 = cellLayoutChildrenForIndex2.getChildAt(0, 0);
                        break;
                    }
                    break;
                case -7:
                    view2 = folderPagedView.getLastItem();
                    break;
                case -6:
                    view2 = cellLayout.getChildAt(0, 0);
                    break;
                case -5:
                case -2:
                    ShortcutAndWidgetContainer cellLayoutChildrenForIndex3 = FocusHelper.getCellLayoutChildrenForIndex(folderPagedView, indexOfChild2 - 1);
                    if (cellLayoutChildrenForIndex3 != null) {
                        int i2 = ((CellLayout.LayoutParams) view.getLayoutParams()).cellY;
                        folderPagedView.snapToPage(indexOfChild2 - 1);
                        view2 = cellLayoutChildrenForIndex3.getChildAt((handleKeyEvent == -5) ^ cellLayoutChildrenForIndex3.invertLayoutHorizontally() ? 0 : createSparseMatrix.length - 1, i2);
                        break;
                    }
                    break;
                case -4:
                    ShortcutAndWidgetContainer cellLayoutChildrenForIndex4 = FocusHelper.getCellLayoutChildrenForIndex(folderPagedView, indexOfChild2 - 1);
                    if (cellLayoutChildrenForIndex4 != null) {
                        folderPagedView.snapToPage(indexOfChild2 - 1);
                        view2 = cellLayoutChildrenForIndex4.getChildAt(createSparseMatrix.length - 1, createSparseMatrix[0].length - 1);
                        break;
                    }
                    break;
                case -3:
                    ShortcutAndWidgetContainer cellLayoutChildrenForIndex5 = FocusHelper.getCellLayoutChildrenForIndex(folderPagedView, indexOfChild2 - 1);
                    if (cellLayoutChildrenForIndex5 != null) {
                        folderPagedView.snapToPage(indexOfChild2 - 1);
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
    }

    private static boolean checkIsNull(CellLayout cellLayout) {
        boolean z = true;
        if (cellLayout != null) {
            if (cellLayout.getShortcutsAndWidgets() == null) {
                z = true;
            } else {
                z = true;
                if (cellLayout.getShortcutsAndWidgets().getChildAt(0) != null) {
                    z = true;
                    if (cellLayout.getShortcutsAndWidgets().getChildAt(0).getLayoutParams() != null) {
                        z = false;
                    }
                }
            }
        }
        return z;
    }

    static ShortcutAndWidgetContainer getCellLayoutChildrenForIndex(ViewGroup viewGroup, int i) {
        return ((CellLayout) viewGroup.getChildAt(i)).getShortcutsAndWidgets();
    }

    /* JADX WARN: Code restructure failed: missing block: B:24:0x0059, code lost:
        r7 = r7 + 1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static View getFirstFocusableIconInReadingOrder(CellLayout cellLayout, boolean z) {
        int countX = cellLayout.getCountX();
        int i = 0;
        while (i < cellLayout.getCountY()) {
            int i2 = z ? -1 : 1;
            int i3 = z ? countX - 1 : 0;
            while (true) {
                int i4 = i3;
                if (i4 >= 0 && i4 < countX) {
                    View childAt = cellLayout.getChildAt(i4, i);
                    if (childAt != null && childAt.isFocusable()) {
                        return childAt;
                    }
                    i3 = i4 + i2;
                }
            }
        }
        return null;
    }

    /* JADX WARN: Code restructure failed: missing block: B:24:0x005a, code lost:
        r7 = r7 - 1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static View getFirstFocusableIconInReverseReadingOrder(CellLayout cellLayout, boolean z) {
        int countX = cellLayout.getCountX();
        int countY = cellLayout.getCountY() - 1;
        while (countY >= 0) {
            int i = z ? 1 : -1;
            int i2 = z ? 0 : countX - 1;
            while (true) {
                int i3 = i2;
                if (i3 >= 0 && i3 < countX) {
                    View childAt = cellLayout.getChildAt(i3, countY);
                    if (childAt != null && childAt.isFocusable()) {
                        return childAt;
                    }
                    i2 = i3 + i;
                }
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean handleHotseatButtonKeyEvent(View view, int i, KeyEvent keyEvent) {
        int[][] createSparseMatrix;
        int i2;
        int i3;
        boolean shouldConsume = FocusLogic.shouldConsume(i);
        if (keyEvent.getAction() == 1 || !shouldConsume) {
            return shouldConsume;
        }
        Launcher launcher = (Launcher) view.getContext();
        DeviceProfile deviceProfile = launcher.getDeviceProfile();
        Workspace workspace = (Workspace) view.getRootView().findViewById(2131296271);
        ShortcutAndWidgetContainer shortcutAndWidgetContainer = (ShortcutAndWidgetContainer) view.getParent();
        CellLayout cellLayout = (CellLayout) shortcutAndWidgetContainer.getParent();
        ItemInfo itemInfo = (ItemInfo) view.getTag();
        int nextPage = workspace.getNextPage();
        int childCount = workspace.getChildCount();
        int indexOfChild = shortcutAndWidgetContainer.indexOfChild(view);
        int i4 = ((CellLayout.LayoutParams) cellLayout.getShortcutsAndWidgets().getChildAt(indexOfChild).getLayoutParams()).cellX;
        CellLayout cellLayout2 = (CellLayout) workspace.getChildAt(nextPage);
        if (cellLayout2 == null) {
            return shouldConsume;
        }
        ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout2.getShortcutsAndWidgets();
        if (i == 19 && !deviceProfile.isVerticalBarLayout()) {
            createSparseMatrix = FocusLogic.createSparseMatrixWithHotseat(cellLayout2, cellLayout, true, deviceProfile.inv.hotseatAllAppsRank);
            i2 = indexOfChild + shortcutsAndWidgets.getChildCount();
            shortcutAndWidgetContainer = shortcutsAndWidgets;
            i3 = i;
        } else if (i == 21 && deviceProfile.isVerticalBarLayout()) {
            createSparseMatrix = FocusLogic.createSparseMatrixWithHotseat(cellLayout2, cellLayout, false, deviceProfile.inv.hotseatAllAppsRank);
            i2 = indexOfChild + shortcutsAndWidgets.getChildCount();
            shortcutAndWidgetContainer = shortcutsAndWidgets;
            i3 = i;
        } else if (i == 22 && deviceProfile.isVerticalBarLayout()) {
            i3 = 93;
            createSparseMatrix = null;
            i2 = indexOfChild;
            shortcutAndWidgetContainer = null;
        } else if (isUninstallKeyChord(keyEvent)) {
            int[][] createSparseMatrix2 = FocusLogic.createSparseMatrix(cellLayout2);
            createSparseMatrix = createSparseMatrix2;
            i2 = indexOfChild;
            shortcutAndWidgetContainer = null;
            i3 = i;
            if (UninstallDropTarget.supportsDrop(launcher, itemInfo)) {
                UninstallDropTarget.startUninstallActivity(launcher, itemInfo);
                createSparseMatrix = createSparseMatrix2;
                i2 = indexOfChild;
                shortcutAndWidgetContainer = null;
                i3 = i;
            }
        } else if (isDeleteKeyChord(keyEvent)) {
            createSparseMatrix = FocusLogic.createSparseMatrix(cellLayout2);
            launcher.removeItem(view, itemInfo, true);
            i2 = indexOfChild;
            shortcutAndWidgetContainer = null;
            i3 = i;
        } else {
            createSparseMatrix = FocusLogic.createSparseMatrix(cellLayout);
            i2 = indexOfChild;
            i3 = i;
        }
        int handleKeyEvent = FocusLogic.handleKeyEvent(i3, createSparseMatrix, i2, nextPage, childCount, Utilities.isRtl(view.getResources()));
        View view2 = null;
        ShortcutAndWidgetContainer shortcutAndWidgetContainer2 = shortcutAndWidgetContainer;
        switch (handleKeyEvent) {
            case -10:
            case -9:
                workspace.snapToPage(nextPage + 1);
                CellLayout cellLayout3 = (CellLayout) workspace.getPageAt(nextPage + 1);
                if (!checkIsNull(cellLayout3)) {
                    view2 = null;
                    shortcutAndWidgetContainer2 = shortcutAndWidgetContainer;
                    if (((CellLayout.LayoutParams) cellLayout3.getShortcutsAndWidgets().getChildAt(0).getLayoutParams()).isFullscreen) {
                        workspace.getPageAt(nextPage + 1).requestFocus();
                        view2 = null;
                        shortcutAndWidgetContainer2 = shortcutAndWidgetContainer;
                        break;
                    }
                } else {
                    return shouldConsume;
                }
                break;
            case -8:
                shortcutAndWidgetContainer2 = getCellLayoutChildrenForIndex(workspace, nextPage + 1);
                view2 = shortcutAndWidgetContainer2.getChildAt(0);
                workspace.snapToPage(nextPage + 1);
                break;
            case -7:
            case -6:
                break;
            case -5:
            case -2:
                workspace.snapToPage(nextPage - 1);
                CellLayout cellLayout4 = (CellLayout) workspace.getPageAt(nextPage - 1);
                if (!checkIsNull(cellLayout4)) {
                    view2 = null;
                    shortcutAndWidgetContainer2 = shortcutAndWidgetContainer;
                    if (((CellLayout.LayoutParams) cellLayout4.getShortcutsAndWidgets().getChildAt(0).getLayoutParams()).isFullscreen) {
                        workspace.getPageAt(nextPage - 1).requestFocus();
                        view2 = null;
                        shortcutAndWidgetContainer2 = shortcutAndWidgetContainer;
                        break;
                    }
                } else {
                    return shouldConsume;
                }
                break;
            case -4:
                shortcutAndWidgetContainer2 = getCellLayoutChildrenForIndex(workspace, nextPage - 1);
                view2 = shortcutAndWidgetContainer2.getChildAt(shortcutAndWidgetContainer2.getChildCount() - 1);
                workspace.snapToPage(nextPage - 1);
                break;
            case -3:
                shortcutAndWidgetContainer2 = getCellLayoutChildrenForIndex(workspace, nextPage - 1);
                view2 = shortcutAndWidgetContainer2.getChildAt(0);
                workspace.snapToPage(nextPage - 1);
                break;
            default:
                shortcutAndWidgetContainer2 = shortcutAndWidgetContainer;
                view2 = null;
                break;
        }
        int i5 = handleKeyEvent;
        if (shortcutAndWidgetContainer2 == shortcutsAndWidgets) {
            i5 = handleKeyEvent;
            if (handleKeyEvent >= shortcutsAndWidgets.getChildCount()) {
                i5 = handleKeyEvent - shortcutsAndWidgets.getChildCount();
            }
        }
        if (shortcutAndWidgetContainer2 != null) {
            View view3 = view2;
            if (view2 == null) {
                view3 = view2;
                if (i5 >= 0) {
                    view3 = shortcutAndWidgetContainer2.getChildAt(i5);
                }
            }
            if (view3 != null) {
                view3.requestFocus();
                playSoundEffect(i3, view);
            }
        }
        return shouldConsume;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean handleIconKeyEvent(View view, int i, KeyEvent keyEvent) {
        int[][] createSparseMatrix;
        ViewGroup viewGroup;
        boolean shouldConsume = FocusLogic.shouldConsume(i);
        if (keyEvent.getAction() == 1 || !shouldConsume) {
            return shouldConsume;
        }
        Launcher launcher = (Launcher) view.getContext();
        DeviceProfile deviceProfile = launcher.getDeviceProfile();
        ShortcutAndWidgetContainer shortcutAndWidgetContainer = (ShortcutAndWidgetContainer) view.getParent();
        CellLayout cellLayout = (CellLayout) shortcutAndWidgetContainer.getParent();
        Workspace workspace = (Workspace) cellLayout.getParent();
        ViewGroup viewGroup2 = (ViewGroup) workspace.getParent();
        ViewGroup viewGroup3 = (ViewGroup) viewGroup2.findViewById(2131296293);
        Hotseat hotseat = (Hotseat) viewGroup2.findViewById(2131296291);
        ItemInfo itemInfo = (ItemInfo) view.getTag();
        int indexOfChild = shortcutAndWidgetContainer.indexOfChild(view);
        int indexOfChild2 = workspace.indexOfChild(cellLayout);
        int childCount = workspace.getChildCount();
        CellLayout cellLayout2 = (CellLayout) hotseat.getChildAt(0);
        ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout2.getShortcutsAndWidgets();
        if (i == 20 && !deviceProfile.isVerticalBarLayout()) {
            createSparseMatrix = FocusLogic.createSparseMatrixWithHotseat(cellLayout, cellLayout2, true, deviceProfile.inv.hotseatAllAppsRank);
        } else if (i == 22 && deviceProfile.isVerticalBarLayout()) {
            createSparseMatrix = FocusLogic.createSparseMatrixWithHotseat(cellLayout, cellLayout2, false, deviceProfile.inv.hotseatAllAppsRank);
        } else if (isUninstallKeyChord(keyEvent)) {
            int[][] createSparseMatrix2 = FocusLogic.createSparseMatrix(cellLayout);
            createSparseMatrix = createSparseMatrix2;
            if (UninstallDropTarget.supportsDrop(launcher, itemInfo)) {
                UninstallDropTarget.startUninstallActivity(launcher, itemInfo);
                createSparseMatrix = createSparseMatrix2;
            }
        } else if (isDeleteKeyChord(keyEvent)) {
            createSparseMatrix = FocusLogic.createSparseMatrix(cellLayout);
            launcher.removeItem(view, itemInfo, true);
        } else {
            createSparseMatrix = FocusLogic.createSparseMatrix(cellLayout);
        }
        int handleKeyEvent = FocusLogic.handleKeyEvent(i, createSparseMatrix, indexOfChild, indexOfChild2, childCount, Utilities.isRtl(view.getResources()));
        boolean isRtl = Utilities.isRtl(view.getResources());
        CellLayout cellLayout3 = (CellLayout) workspace.getChildAt(indexOfChild2);
        switch (handleKeyEvent) {
            case -10:
            case -2:
                int i2 = indexOfChild2 - 1;
                if (handleKeyEvent == -10) {
                    i2 = indexOfChild2 + 1;
                }
                int i3 = ((CellLayout.LayoutParams) view.getLayoutParams()).cellY;
                ShortcutAndWidgetContainer cellLayoutChildrenForIndex = getCellLayoutChildrenForIndex(workspace, i2);
                viewGroup = null;
                if (cellLayoutChildrenForIndex != null) {
                    CellLayout cellLayout4 = (CellLayout) cellLayoutChildrenForIndex.getParent();
                    int handleKeyEvent2 = FocusLogic.handleKeyEvent(i, FocusLogic.createSparseMatrixWithPivotColumn(cellLayout4, cellLayout4.getCountX(), i3), 100, i2, childCount, Utilities.isRtl(view.getResources()));
                    if (handleKeyEvent2 != -8) {
                        if (handleKeyEvent2 != -4) {
                            viewGroup = cellLayoutChildrenForIndex.getChildAt(handleKeyEvent2);
                            break;
                        } else {
                            viewGroup = handlePreviousPageLastItem(workspace, cellLayout2, indexOfChild2, isRtl);
                            break;
                        }
                    } else {
                        viewGroup = handleNextPageFirstItem(workspace, cellLayout2, indexOfChild2, isRtl);
                        break;
                    }
                }
                break;
            case -9:
            case -5:
                int i4 = indexOfChild2 + 1;
                if (handleKeyEvent == -5) {
                    i4 = indexOfChild2 - 1;
                }
                int i5 = ((CellLayout.LayoutParams) view.getLayoutParams()).cellY;
                ShortcutAndWidgetContainer cellLayoutChildrenForIndex2 = getCellLayoutChildrenForIndex(workspace, i4);
                viewGroup = null;
                if (cellLayoutChildrenForIndex2 != null) {
                    int handleKeyEvent3 = FocusLogic.handleKeyEvent(i, FocusLogic.createSparseMatrixWithPivotColumn((CellLayout) cellLayoutChildrenForIndex2.getParent(), -1, i5), 100, i4, childCount, Utilities.isRtl(view.getResources()));
                    if (handleKeyEvent3 != -8) {
                        if (handleKeyEvent3 != -4) {
                            viewGroup = cellLayoutChildrenForIndex2.getChildAt(handleKeyEvent3);
                            break;
                        } else {
                            viewGroup = handlePreviousPageLastItem(workspace, cellLayout2, indexOfChild2, isRtl);
                            break;
                        }
                    } else {
                        viewGroup = handleNextPageFirstItem(workspace, cellLayout2, indexOfChild2, isRtl);
                        break;
                    }
                }
                break;
            case -8:
                viewGroup = handleNextPageFirstItem(workspace, cellLayout2, indexOfChild2, isRtl);
                break;
            case -7:
                View firstFocusableIconInReverseReadingOrder = getFirstFocusableIconInReverseReadingOrder(cellLayout3, isRtl);
                viewGroup = firstFocusableIconInReverseReadingOrder;
                if (firstFocusableIconInReverseReadingOrder == null) {
                    viewGroup = getFirstFocusableIconInReverseReadingOrder(cellLayout2, isRtl);
                    break;
                }
                break;
            case -6:
                View firstFocusableIconInReadingOrder = getFirstFocusableIconInReadingOrder(cellLayout3, isRtl);
                viewGroup = firstFocusableIconInReadingOrder;
                if (firstFocusableIconInReadingOrder == null) {
                    viewGroup = getFirstFocusableIconInReadingOrder(cellLayout2, isRtl);
                    break;
                }
                break;
            case -4:
                viewGroup = handlePreviousPageLastItem(workspace, cellLayout2, indexOfChild2, isRtl);
                break;
            case -3:
                View firstFocusableIconInReadingOrder2 = getFirstFocusableIconInReadingOrder((CellLayout) workspace.getChildAt(indexOfChild2 - 1), isRtl);
                viewGroup = firstFocusableIconInReadingOrder2;
                if (firstFocusableIconInReadingOrder2 == null) {
                    viewGroup = getFirstFocusableIconInReadingOrder(cellLayout2, isRtl);
                    workspace.snapToPage(indexOfChild2 - 1);
                    break;
                }
                break;
            case -1:
                viewGroup = null;
                if (i == 19) {
                    viewGroup = viewGroup3;
                    break;
                }
                break;
            default:
                if (handleKeyEvent >= 0 && handleKeyEvent < shortcutAndWidgetContainer.getChildCount()) {
                    viewGroup = shortcutAndWidgetContainer.getChildAt(handleKeyEvent);
                    break;
                } else {
                    viewGroup = null;
                    if (shortcutAndWidgetContainer.getChildCount() <= handleKeyEvent) {
                        viewGroup = null;
                        if (handleKeyEvent < shortcutAndWidgetContainer.getChildCount() + shortcutsAndWidgets.getChildCount()) {
                            viewGroup = shortcutsAndWidgets.getChildAt(handleKeyEvent - shortcutAndWidgetContainer.getChildCount());
                            break;
                        }
                    }
                }
                break;
        }
        if (viewGroup != null) {
            viewGroup.requestFocus();
            playSoundEffect(i, view);
        }
        return shouldConsume;
    }

    private static View handleNextPageFirstItem(Workspace workspace, CellLayout cellLayout, int i, boolean z) {
        if (i + 1 >= workspace.getPageCount()) {
            return null;
        }
        View firstFocusableIconInReadingOrder = getFirstFocusableIconInReadingOrder((CellLayout) workspace.getChildAt(i + 1), z);
        View view = firstFocusableIconInReadingOrder;
        if (firstFocusableIconInReadingOrder == null) {
            view = getFirstFocusableIconInReadingOrder(cellLayout, z);
            workspace.snapToPage(i + 1);
        }
        return view;
    }

    private static View handlePreviousPageLastItem(Workspace workspace, CellLayout cellLayout, int i, boolean z) {
        if (i - 1 < 0) {
            return null;
        }
        View firstFocusableIconInReverseReadingOrder = getFirstFocusableIconInReverseReadingOrder((CellLayout) workspace.getChildAt(i - 1), z);
        View view = firstFocusableIconInReverseReadingOrder;
        if (firstFocusableIconInReverseReadingOrder == null) {
            view = getFirstFocusableIconInReverseReadingOrder(cellLayout, z);
            workspace.snapToPage(i - 1);
        }
        return view;
    }

    private static boolean isDeleteKeyChord(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        return (keyCode == 67 || keyCode == 112) ? keyEvent.hasModifiers(4096) : false;
    }

    private static boolean isUninstallKeyChord(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        return (keyCode == 67 || keyCode == 112) ? keyEvent.hasModifiers(4097) : false;
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
}
