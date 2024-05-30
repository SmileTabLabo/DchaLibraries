package com.android.launcher3.util;

import android.util.Log;
import android.view.View;
import com.android.launcher3.CellLayout;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.ShortcutAndWidgetContainer;
import java.lang.reflect.Array;
import java.util.Arrays;
/* loaded from: classes.dex */
public class FocusLogic {
    public static final int ALL_APPS_COLUMN = -11;
    public static final int CURRENT_PAGE_FIRST_ITEM = -6;
    public static final int CURRENT_PAGE_LAST_ITEM = -7;
    private static final boolean DEBUG = false;
    public static final int EMPTY = -1;
    public static final int NEXT_PAGE_FIRST_ITEM = -8;
    public static final int NEXT_PAGE_LEFT_COLUMN = -9;
    public static final int NEXT_PAGE_RIGHT_COLUMN = -10;
    public static final int NOOP = -1;
    public static final int PIVOT = 100;
    public static final int PREVIOUS_PAGE_FIRST_ITEM = -3;
    public static final int PREVIOUS_PAGE_LAST_ITEM = -4;
    public static final int PREVIOUS_PAGE_LEFT_COLUMN = -5;
    public static final int PREVIOUS_PAGE_RIGHT_COLUMN = -2;
    private static final String TAG = "FocusLogic";

    public static boolean shouldConsume(int i) {
        return i == 21 || i == 22 || i == 19 || i == 20 || i == 122 || i == 123 || i == 92 || i == 93;
    }

    public static int handleKeyEvent(int i, int[][] iArr, int i2, int i3, int i4, boolean z) {
        int length;
        int length2;
        int handleDpadHorizontal;
        if (iArr != null) {
            length = iArr.length;
        } else {
            length = -1;
        }
        if (iArr != null) {
            length2 = iArr[0].length;
        } else {
            length2 = -1;
        }
        switch (i) {
            case 19:
                return handleDpadVertical(i2, length, length2, iArr, -1);
            case 20:
                return handleDpadVertical(i2, length, length2, iArr, 1);
            case 21:
                handleDpadHorizontal = handleDpadHorizontal(i2, length, length2, iArr, -1, z);
                if (!z && handleDpadHorizontal == -1 && i3 > 0) {
                    return -2;
                }
                if (z && handleDpadHorizontal == -1 && i3 < i4 - 1) {
                    return -10;
                }
                break;
            case 22:
                handleDpadHorizontal = handleDpadHorizontal(i2, length, length2, iArr, 1, z);
                if (!z && handleDpadHorizontal == -1 && i3 < i4 - 1) {
                    return -9;
                }
                if (z && handleDpadHorizontal == -1 && i3 > 0) {
                    return -5;
                }
                break;
            case 92:
                return handlePageUp(i3);
            case 93:
                return handlePageDown(i3, i4);
            case 122:
                return handleMoveHome();
            case 123:
                return handleMoveEnd();
            default:
                return -1;
        }
        return handleDpadHorizontal;
    }

    private static int[][] createFullMatrix(int i, int i2) {
        int[][] iArr = (int[][]) Array.newInstance(int.class, i, i2);
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
                if (invertLayoutHorizontally) {
                    i2 = (countX - i2) - 1;
                }
                if (i2 < countX && i3 < countY) {
                    createFullMatrix[i2][i3] = i;
                }
            }
        }
        return createFullMatrix;
    }

    public static int[][] createSparseMatrixWithHotseat(CellLayout cellLayout, CellLayout cellLayout2, DeviceProfile deviceProfile) {
        int countX;
        int countY;
        ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout.getShortcutsAndWidgets();
        ShortcutAndWidgetContainer shortcutsAndWidgets2 = cellLayout2.getShortcutsAndWidgets();
        boolean z = !deviceProfile.isVerticalBarLayout();
        if (z) {
            countX = cellLayout2.getCountX();
            countY = cellLayout.getCountY() + cellLayout2.getCountY();
        } else {
            countX = cellLayout.getCountX() + cellLayout2.getCountX();
            countY = cellLayout2.getCountY();
        }
        int[][] createFullMatrix = createFullMatrix(countX, countY);
        for (int i = 0; i < shortcutsAndWidgets.getChildCount(); i++) {
            View childAt = shortcutsAndWidgets.getChildAt(i);
            if (childAt.isFocusable()) {
                createFullMatrix[((CellLayout.LayoutParams) childAt.getLayoutParams()).cellX][((CellLayout.LayoutParams) childAt.getLayoutParams()).cellY] = i;
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

    private static int handleDpadHorizontal(int i, int i2, int i3, int[][] iArr, int i4, boolean z) {
        if (iArr == null) {
            throw new IllegalStateException("Dpad navigation requires a matrix.");
        }
        int i5 = -1;
        int i6 = -1;
        int i7 = 0;
        while (i7 < i2) {
            int i8 = i6;
            int i9 = i5;
            for (int i10 = 0; i10 < i3; i10++) {
                if (iArr[i7][i10] == i) {
                    i9 = i7;
                    i8 = i10;
                }
            }
            i7++;
            i5 = i9;
            i6 = i8;
        }
        int i11 = i5 + i4;
        int i12 = -1;
        while (i11 >= 0 && i11 < i2) {
            i12 = inspectMatrix(i11, i6, i2, i3, iArr);
            if (i12 == -1 || i12 == -11) {
                i11 += i4;
            } else {
                return i12;
            }
        }
        int i13 = i12;
        boolean z2 = false;
        boolean z3 = false;
        for (int i14 = 1; i14 < i3; i14++) {
            int i15 = i14 * i4;
            int i16 = i6 + i15;
            int i17 = i6 - i15;
            int i18 = i15 + i5;
            if (inspectMatrix(i18, i16, i2, i3, iArr) == -11) {
                z2 = true;
            }
            if (inspectMatrix(i18, i17, i2, i3, iArr) == -11) {
                z3 = true;
            }
            while (i18 >= 0 && i18 < i2) {
                int inspectMatrix = inspectMatrix(i18, ((!z2 || i18 >= i2 + (-1)) ? 0 : i4) + i16, i2, i3, iArr);
                if (inspectMatrix != -1) {
                    return inspectMatrix;
                }
                i13 = inspectMatrix(i18, ((!z3 || i18 >= i2 + (-1)) ? 0 : -i4) + i17, i2, i3, iArr);
                if (i13 == -1) {
                    i18 += i4;
                } else {
                    return i13;
                }
            }
        }
        if (i == 100) {
            return z ? i4 < 0 ? -8 : -4 : i4 < 0 ? -4 : -8;
        }
        return i13;
    }

    private static int handleDpadVertical(int i, int i2, int i3, int[][] iArr, int i4) {
        int i5;
        if (iArr == null) {
            throw new IllegalStateException("Dpad navigation requires a matrix.");
        }
        int i6 = -1;
        int i7 = -1;
        int i8 = 0;
        while (i8 < i2) {
            int i9 = i7;
            int i10 = i6;
            for (int i11 = 0; i11 < i3; i11++) {
                if (iArr[i8][i11] == i) {
                    i9 = i8;
                    i10 = i11;
                }
            }
            i8++;
            i6 = i10;
            i7 = i9;
        }
        int i12 = i6 + i4;
        int i13 = -1;
        while (i12 >= 0 && i12 < i3 && i12 >= 0) {
            i13 = inspectMatrix(i7, i12, i2, i3, iArr);
            if (i13 == -1 || i13 == -11) {
                i12 += i4;
            } else {
                return i13;
            }
        }
        int i14 = i13;
        boolean z = false;
        boolean z2 = false;
        for (int i15 = 1; i15 < i2; i15++) {
            int i16 = i15 * i4;
            int i17 = i7 + i16;
            int i18 = i7 - i16;
            int i19 = i16 + i6;
            if (inspectMatrix(i17, i19, i2, i3, iArr) == -11) {
                z = true;
            }
            if (inspectMatrix(i18, i19, i2, i3, iArr) == -11) {
                z2 = true;
            }
            while (i19 >= 0 && i19 < i3) {
                int inspectMatrix = inspectMatrix(((!z || i19 >= i3 + (-1)) ? 0 : i4) + i17, i19, i2, i3, iArr);
                if (inspectMatrix != -1) {
                    return inspectMatrix;
                }
                if (z2 && i19 < i3 - 1) {
                    i5 = -i4;
                } else {
                    i5 = 0;
                }
                i14 = inspectMatrix(i5 + i18, i19, i2, i3, iArr);
                if (i14 == -1) {
                    i19 += i4;
                } else {
                    return i14;
                }
            }
        }
        return i14;
    }

    private static int handleMoveHome() {
        return -6;
    }

    private static int handleMoveEnd() {
        return -7;
    }

    private static int handlePageDown(int i, int i2) {
        if (i < i2 - 1) {
            return -8;
        }
        return -7;
    }

    private static int handlePageUp(int i) {
        if (i > 0) {
            return -3;
        }
        return -6;
    }

    private static boolean isValid(int i, int i2, int i3, int i4) {
        return i >= 0 && i < i3 && i2 >= 0 && i2 < i4;
    }

    private static int inspectMatrix(int i, int i2, int i3, int i4, int[][] iArr) {
        if (!isValid(i, i2, i3, i4) || iArr[i][i2] == -1) {
            return -1;
        }
        return iArr[i][i2];
    }

    private static String getStringIndex(int i) {
        switch (i) {
            case ALL_APPS_COLUMN /* -11 */:
                return "ALL_APPS_COLUMN";
            case NEXT_PAGE_RIGHT_COLUMN /* -10 */:
            case PREVIOUS_PAGE_LEFT_COLUMN /* -5 */:
            default:
                return Integer.toString(i);
            case NEXT_PAGE_LEFT_COLUMN /* -9 */:
                return "NEXT_PAGE_LEFT_COLUMN";
            case NEXT_PAGE_FIRST_ITEM /* -8 */:
                return "NEXT_PAGE_FIRST";
            case CURRENT_PAGE_LAST_ITEM /* -7 */:
                return "CURRENT_PAGE_LAST";
            case CURRENT_PAGE_FIRST_ITEM /* -6 */:
                return "CURRENT_PAGE_FIRST";
            case -4:
                return "PREVIOUS_PAGE_LAST";
            case -3:
                return "PREVIOUS_PAGE_FIRST";
            case -2:
                return "PREVIOUS_PAGE_RIGHT_COLUMN";
            case -1:
                return "NOOP";
        }
    }

    private static void printMatrix(int[][] iArr) {
        Log.v(TAG, "\tprintMap:");
        int length = iArr.length;
        int length2 = iArr[0].length;
        for (int i = 0; i < length2; i++) {
            String str = "\t\t";
            for (int i2 = 0; i2 < length; i2++) {
                str = str + String.format("%3d", Integer.valueOf(iArr[i2][i]));
            }
            Log.v(TAG, str);
        }
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
}
