package com.android.launcher3.util;

import android.view.View;
import com.android.launcher3.CellLayout;
import com.android.launcher3.ShortcutAndWidgetContainer;
import java.util.Arrays;
/* loaded from: a.zip:com/android/launcher3/util/FocusLogic.class */
public class FocusLogic {
    private static int[][] createFullMatrix(int i, int i2) {
        int[][] iArr = new int[i][i2];
        for (int i3 = 0; i3 < i; i3++) {
            Arrays.fill(iArr[i3], -1);
        }
        return iArr;
    }

    public static int[][] createSparseMatrix(CellLayout cellLayout) {
        ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout.getShortcutsAndWidgets();
        int countX = cellLayout.getCountX();
        int countY = cellLayout.getCountY();
        boolean invertLayoutHorizontally = shortcutsAndWidgets.invertLayoutHorizontally();
        int[][] createFullMatrix = createFullMatrix(countX, countY);
        for (int i = 0; i < shortcutsAndWidgets.getChildCount(); i++) {
            View childAt = shortcutsAndWidgets.getChildAt(i);
            if (childAt.isFocusable()) {
                int i2 = ((CellLayout.LayoutParams) childAt.getLayoutParams()).cellX;
                int i3 = ((CellLayout.LayoutParams) childAt.getLayoutParams()).cellY;
                int i4 = i2;
                if (invertLayoutHorizontally) {
                    i4 = (countX - i2) - 1;
                }
                createFullMatrix[i4][i3] = i;
            }
        }
        return createFullMatrix;
    }

    public static int[][] createSparseMatrixWithHotseat(CellLayout cellLayout, CellLayout cellLayout2, boolean z, int i) {
        int countX;
        int countY;
        ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout.getShortcutsAndWidgets();
        ShortcutAndWidgetContainer shortcutsAndWidgets2 = cellLayout2.getShortcutsAndWidgets();
        boolean z2 = z ? cellLayout2.getCountX() > cellLayout.getCountX() : cellLayout2.getCountY() > cellLayout.getCountY();
        if (z) {
            countX = cellLayout2.getCountX();
            countY = cellLayout.getCountY() + cellLayout2.getCountY();
        } else {
            countX = cellLayout.getCountX() + cellLayout2.getCountX();
            countY = cellLayout2.getCountY();
        }
        int[][] createFullMatrix = createFullMatrix(countX, countY);
        if (z2) {
            if (z) {
                for (int i2 = 0; i2 < countY; i2++) {
                    createFullMatrix[i][i2] = -11;
                }
            } else {
                for (int i3 = 0; i3 < countX; i3++) {
                    createFullMatrix[i3][i] = -11;
                }
            }
        }
        for (int i4 = 0; i4 < shortcutsAndWidgets.getChildCount(); i4++) {
            View childAt = shortcutsAndWidgets.getChildAt(i4);
            if (childAt.isFocusable()) {
                int i5 = ((CellLayout.LayoutParams) childAt.getLayoutParams()).cellX;
                int i6 = ((CellLayout.LayoutParams) childAt.getLayoutParams()).cellY;
                int i7 = i5;
                int i8 = i6;
                if (z2) {
                    int i9 = i5;
                    if (z) {
                        i9 = i5;
                        if (i5 >= i) {
                            i9 = i5 + 1;
                        }
                    }
                    i7 = i9;
                    i8 = i6;
                    if (!z) {
                        i7 = i9;
                        i8 = i6;
                        if (i6 >= i) {
                            i8 = i6 + 1;
                            i7 = i9;
                        }
                    }
                }
                createFullMatrix[i7][i8] = i4;
            }
        }
        for (int childCount = shortcutsAndWidgets2.getChildCount() - 1; childCount >= 0; childCount--) {
            if (z) {
                createFullMatrix[((CellLayout.LayoutParams) shortcutsAndWidgets2.getChildAt(childCount).getLayoutParams()).cellX][cellLayout.getCountY()] = shortcutsAndWidgets.getChildCount() + childCount;
            } else {
                createFullMatrix[cellLayout.getCountX()][((CellLayout.LayoutParams) shortcutsAndWidgets2.getChildAt(childCount).getLayoutParams()).cellY] = shortcutsAndWidgets.getChildCount() + childCount;
            }
        }
        return createFullMatrix;
    }

    public static int[][] createSparseMatrixWithPivotColumn(CellLayout cellLayout, int i, int i2) {
        ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout.getShortcutsAndWidgets();
        int[][] createFullMatrix = createFullMatrix(cellLayout.getCountX() + 1, cellLayout.getCountY());
        for (int i3 = 0; i3 < shortcutsAndWidgets.getChildCount(); i3++) {
            View childAt = shortcutsAndWidgets.getChildAt(i3);
            if (childAt.isFocusable()) {
                int i4 = ((CellLayout.LayoutParams) childAt.getLayoutParams()).cellX;
                int i5 = ((CellLayout.LayoutParams) childAt.getLayoutParams()).cellY;
                if (i < 0) {
                    createFullMatrix[i4 - i][i5] = i3;
                } else {
                    createFullMatrix[i4][i5] = i3;
                }
            }
        }
        if (i < 0) {
            createFullMatrix[0][i2] = 100;
        } else {
            createFullMatrix[i][i2] = 100;
        }
        return createFullMatrix;
    }

    public static View getAdjacentChildInNextFolderPage(ShortcutAndWidgetContainer shortcutAndWidgetContainer, View view, int i) {
        int i2 = ((CellLayout.LayoutParams) view.getLayoutParams()).cellY;
        for (int countX = (i == -9) ^ shortcutAndWidgetContainer.invertLayoutHorizontally() ? 0 : ((CellLayout) shortcutAndWidgetContainer.getParent()).getCountX() - 1; countX >= 0; countX--) {
            for (int i3 = i2; i3 >= 0; i3--) {
                View childAt = shortcutAndWidgetContainer.getChildAt(countX, i3);
                if (childAt != null) {
                    return childAt;
                }
            }
        }
        return null;
    }

    private static int handleDpadHorizontal(int i, int i2, int i3, int[][] iArr, int i4, boolean z) {
        if (iArr == null) {
            throw new IllegalStateException("Dpad navigation requires a matrix.");
        }
        int i5 = -1;
        int i6 = -1;
        int i7 = 0;
        while (i7 < i2) {
            int i8 = i6;
            for (int i9 = 0; i9 < i3; i9++) {
                if (iArr[i7][i9] == i) {
                    i5 = i7;
                    i8 = i9;
                }
            }
            i7++;
            i6 = i8;
        }
        int i10 = i5 + i4;
        int i11 = -1;
        while (i10 >= 0 && i10 < i2) {
            i11 = inspectMatrix(i10, i6, i2, i3, iArr);
            if (i11 != -1 && i11 != -11) {
                return i11;
            }
            i10 += i4;
        }
        boolean z2 = false;
        boolean z3 = false;
        int i12 = i11;
        int i13 = 1;
        while (i13 < i3) {
            int i14 = i6 + (i13 * i4);
            int i15 = i6 - (i13 * i4);
            int i16 = i5 + (i4 * i13);
            if (inspectMatrix(i16, i14, i2, i3, iArr) == -11) {
                z2 = true;
            }
            int i17 = i12;
            int i18 = i16;
            if (inspectMatrix(i16, i15, i2, i3, iArr) == -11) {
                z3 = true;
                i18 = i16;
                i17 = i12;
            }
            while (i18 >= 0 && i18 < i2) {
                int inspectMatrix = inspectMatrix(i18, i14 + ((!z2 || i18 >= i2 - 1) ? 0 : i4), i2, i3, iArr);
                if (inspectMatrix != -1) {
                    return inspectMatrix;
                }
                i17 = inspectMatrix(i18, i15 + ((!z3 || i18 >= i2 - 1) ? 0 : -i4), i2, i3, iArr);
                if (i17 != -1) {
                    return i17;
                }
                i18 += i4;
            }
            i13++;
            i12 = i17;
        }
        if (i == 100) {
            if (z) {
                return i4 < 0 ? -8 : -4;
            }
            return i4 < 0 ? -4 : -8;
        }
        return i12;
    }

    private static int handleDpadVertical(int i, int i2, int i3, int[][] iArr, int i4) {
        if (iArr == null) {
            throw new IllegalStateException("Dpad navigation requires a matrix.");
        }
        int i5 = -1;
        int i6 = -1;
        int i7 = 0;
        while (i7 < i2) {
            int i8 = i6;
            for (int i9 = 0; i9 < i3; i9++) {
                if (iArr[i7][i9] == i) {
                    i5 = i7;
                    i8 = i9;
                }
            }
            i7++;
            i6 = i8;
        }
        int i10 = i6 + i4;
        int i11 = -1;
        while (i10 >= 0 && i10 < i3 && i10 >= 0) {
            i11 = inspectMatrix(i5, i10, i2, i3, iArr);
            if (i11 != -1 && i11 != -11) {
                return i11;
            }
            i10 += i4;
        }
        boolean z = false;
        boolean z2 = false;
        int i12 = i11;
        int i13 = 1;
        while (i13 < i2) {
            int i14 = i5 + (i13 * i4);
            int i15 = i5 - (i13 * i4);
            int i16 = i6 + (i4 * i13);
            if (inspectMatrix(i14, i16, i2, i3, iArr) == -11) {
                z = true;
            }
            int i17 = i12;
            int i18 = i16;
            if (inspectMatrix(i15, i16, i2, i3, iArr) == -11) {
                z2 = true;
                i18 = i16;
                i17 = i12;
            }
            while (i18 >= 0 && i18 < i3) {
                int inspectMatrix = inspectMatrix(i14 + ((!z || i18 >= i3 - 1) ? 0 : i4), i18, i2, i3, iArr);
                if (inspectMatrix != -1) {
                    return inspectMatrix;
                }
                i17 = inspectMatrix(i15 + ((!z2 || i18 >= i3 - 1) ? 0 : -i4), i18, i2, i3, iArr);
                if (i17 != -1) {
                    return i17;
                }
                i18 += i4;
            }
            i13++;
            i12 = i17;
        }
        return i12;
    }

    public static int handleKeyEvent(int i, int[][] iArr, int i2, int i3, int i4, boolean z) {
        int handlePageUp;
        int length = iArr == null ? -1 : iArr.length;
        int length2 = iArr == null ? -1 : iArr[0].length;
        switch (i) {
            case 19:
                handlePageUp = handleDpadVertical(i2, length, length2, iArr, -1);
                break;
            case 20:
                handlePageUp = handleDpadVertical(i2, length, length2, iArr, 1);
                break;
            case 21:
                int handleDpadHorizontal = handleDpadHorizontal(i2, length, length2, iArr, -1, z);
                if (!z && handleDpadHorizontal == -1 && i3 > 0) {
                    handlePageUp = -2;
                    break;
                } else {
                    handlePageUp = handleDpadHorizontal;
                    if (z) {
                        handlePageUp = handleDpadHorizontal;
                        if (handleDpadHorizontal == -1) {
                            handlePageUp = handleDpadHorizontal;
                            if (i3 < i4 - 1) {
                                handlePageUp = -10;
                                break;
                            }
                        }
                    }
                }
                break;
            case 22:
                int handleDpadHorizontal2 = handleDpadHorizontal(i2, length, length2, iArr, 1, z);
                if (!z && handleDpadHorizontal2 == -1 && i3 < i4 - 1) {
                    handlePageUp = -9;
                    break;
                } else {
                    handlePageUp = handleDpadHorizontal2;
                    if (z) {
                        handlePageUp = handleDpadHorizontal2;
                        if (handleDpadHorizontal2 == -1) {
                            handlePageUp = handleDpadHorizontal2;
                            if (i3 > 0) {
                                handlePageUp = -5;
                                break;
                            }
                        }
                    }
                }
                break;
            case 92:
                handlePageUp = handlePageUp(i3);
                break;
            case 93:
                handlePageUp = handlePageDown(i3, i4);
                break;
            case 122:
                handlePageUp = handleMoveHome();
                break;
            case 123:
                handlePageUp = handleMoveEnd();
                break;
            default:
                handlePageUp = -1;
                break;
        }
        return handlePageUp;
    }

    private static int handleMoveEnd() {
        return -7;
    }

    private static int handleMoveHome() {
        return -6;
    }

    private static int handlePageDown(int i, int i2) {
        return i < i2 - 1 ? -8 : -7;
    }

    private static int handlePageUp(int i) {
        return i > 0 ? -3 : -6;
    }

    private static int inspectMatrix(int i, int i2, int i3, int i4, int[][] iArr) {
        if (!isValid(i, i2, i3, i4) || iArr[i][i2] == -1) {
            return -1;
        }
        return iArr[i][i2];
    }

    private static boolean isValid(int i, int i2, int i3, int i4) {
        boolean z = false;
        if (i >= 0) {
            z = false;
            if (i < i3) {
                z = false;
                if (i2 >= 0) {
                    z = false;
                    if (i2 < i4) {
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    public static boolean shouldConsume(int i) {
        boolean z = true;
        if (i != 21) {
            if (i == 22) {
                z = true;
            } else {
                z = true;
                if (i != 19) {
                    z = true;
                    if (i != 20) {
                        z = true;
                        if (i != 122) {
                            z = true;
                            if (i != 123) {
                                z = true;
                                if (i != 92) {
                                    z = true;
                                    if (i != 93) {
                                        z = true;
                                        if (i != 67) {
                                            z = true;
                                            if (i != 112) {
                                                z = false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return z;
    }
}
