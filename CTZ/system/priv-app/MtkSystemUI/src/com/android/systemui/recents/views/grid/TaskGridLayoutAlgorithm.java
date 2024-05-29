package com.android.systemui.recents.views.grid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.WindowManager;
import com.android.systemui.R;
import com.android.systemui.recents.events.ui.focus.NavigateTaskViewEvent;
import com.android.systemui.recents.views.TaskStackLayoutAlgorithm;
import com.android.systemui.recents.views.TaskViewTransform;
import com.android.systemui.shared.recents.model.Task;
import com.android.systemui.shared.recents.utilities.Utilities;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class TaskGridLayoutAlgorithm {
    private float mAppAspectRatio;
    private int mFocusedFrameThickness;
    private int mPaddingLeftRight;
    private int mPaddingTaskView;
    private int mPaddingTopBottom;
    private Rect mTaskGridRect;
    private TaskGridRectInfo[] mTaskGridRectInfoList;
    private int mTitleBarHeight;
    private Rect mWindowRect;
    private final String TAG = "TaskGridLayoutAlgorithm";
    private Point mScreenSize = new Point();
    private Rect mSystemInsets = new Rect();

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class TaskGridRectInfo {
        int lines;
        Rect size = new Rect();
        int tasksPerLine;
        int[] xOffsets;
        int[] yOffsets;

        TaskGridRectInfo(int i) {
            float f;
            this.xOffsets = new int[i];
            this.yOffsets = new int[i];
            int min = Math.min(8, i);
            this.tasksPerLine = getTasksPerLine(min);
            int i2 = 4;
            this.lines = min < 4 ? 1 : 2;
            boolean z = TaskGridLayoutAlgorithm.this.mWindowRect.width() > TaskGridLayoutAlgorithm.this.mWindowRect.height();
            boolean z2 = TaskGridLayoutAlgorithm.this.mAppAspectRatio > 1.0f;
            if (!z && z2) {
                this.tasksPerLine = min < 2 ? 1 : 2;
                if (min >= 3) {
                    if (min < 5) {
                        i2 = 2;
                    } else if (min < 7) {
                        i2 = 3;
                    }
                } else {
                    i2 = 1;
                }
                this.lines = i2;
            }
            if (z && !z2) {
                this.tasksPerLine = min < 7 ? min : 6;
                this.lines = min < 7 ? 1 : 2;
            }
            int width = ((TaskGridLayoutAlgorithm.this.mWindowRect.width() - (TaskGridLayoutAlgorithm.this.mPaddingLeftRight * 2)) - ((this.tasksPerLine - 1) * TaskGridLayoutAlgorithm.this.mPaddingTaskView)) / this.tasksPerLine;
            int height = ((TaskGridLayoutAlgorithm.this.mWindowRect.height() - (TaskGridLayoutAlgorithm.this.mPaddingTopBottom * 2)) - ((this.lines - 1) * TaskGridLayoutAlgorithm.this.mPaddingTaskView)) / this.lines;
            if (height >= (width / TaskGridLayoutAlgorithm.this.mAppAspectRatio) + TaskGridLayoutAlgorithm.this.mTitleBarHeight) {
                height = (int) ((f / TaskGridLayoutAlgorithm.this.mAppAspectRatio) + TaskGridLayoutAlgorithm.this.mTitleBarHeight + 0.5d);
            } else {
                width = (int) (((height - TaskGridLayoutAlgorithm.this.mTitleBarHeight) * TaskGridLayoutAlgorithm.this.mAppAspectRatio) + 0.5d);
            }
            this.size.set(0, 0, width, height);
            int width2 = ((TaskGridLayoutAlgorithm.this.mWindowRect.width() - (TaskGridLayoutAlgorithm.this.mPaddingLeftRight * 2)) - (this.tasksPerLine * width)) - ((this.tasksPerLine - 1) * TaskGridLayoutAlgorithm.this.mPaddingTaskView);
            int height2 = ((TaskGridLayoutAlgorithm.this.mWindowRect.height() - (TaskGridLayoutAlgorithm.this.mPaddingTopBottom * 2)) - (this.lines * height)) - ((this.lines - 1) * TaskGridLayoutAlgorithm.this.mPaddingTaskView);
            for (int i3 = 0; i3 < i; i3++) {
                int i4 = (i - i3) - 1;
                int i5 = i4 % this.tasksPerLine;
                int i6 = i4 / this.tasksPerLine;
                this.xOffsets[i3] = TaskGridLayoutAlgorithm.this.mWindowRect.left + (width2 / 2) + TaskGridLayoutAlgorithm.this.mPaddingLeftRight + ((TaskGridLayoutAlgorithm.this.mPaddingTaskView + width) * i5);
                this.yOffsets[i3] = TaskGridLayoutAlgorithm.this.mWindowRect.top + (height2 / 2) + TaskGridLayoutAlgorithm.this.mPaddingTopBottom + ((TaskGridLayoutAlgorithm.this.mPaddingTaskView + height) * i6);
            }
        }

        private int getTasksPerLine(int i) {
            switch (i) {
                case 0:
                    return 0;
                case 1:
                    return 1;
                case 2:
                case 4:
                    return 2;
                case 3:
                case 5:
                case 6:
                    return 3;
                case 7:
                case 8:
                    return 4;
                default:
                    throw new IllegalArgumentException("Unsupported task count " + i);
            }
        }
    }

    public TaskGridLayoutAlgorithm(Context context) {
        reloadOnConfigurationChange(context);
    }

    public void reloadOnConfigurationChange(Context context) {
        Resources resources = context.getResources();
        this.mPaddingTaskView = resources.getDimensionPixelSize(R.dimen.recents_grid_padding_task_view);
        this.mFocusedFrameThickness = resources.getDimensionPixelSize(R.dimen.recents_grid_task_view_focused_frame_thickness);
        this.mTaskGridRect = new Rect();
        this.mTitleBarHeight = resources.getDimensionPixelSize(R.dimen.recents_grid_task_view_header_height);
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealSize(this.mScreenSize);
        updateAppAspectRatio();
    }

    public TaskViewTransform getTransform(int i, int i2, TaskViewTransform taskViewTransform, TaskStackLayoutAlgorithm taskStackLayoutAlgorithm) {
        float f;
        if (i2 == 0) {
            taskViewTransform.reset();
            return taskViewTransform;
        }
        TaskGridRectInfo taskGridRectInfo = this.mTaskGridRectInfoList[i2 - 1];
        this.mTaskGridRect.set(taskGridRectInfo.size);
        int i3 = taskGridRectInfo.xOffsets[i];
        int i4 = taskGridRectInfo.yOffsets[i];
        float f2 = taskStackLayoutAlgorithm.mMaxTranslationZ;
        boolean z = (i2 - i) - 1 < 8;
        taskViewTransform.scale = 1.0f;
        if (z) {
            f = 1.0f;
        } else {
            f = 0.0f;
        }
        taskViewTransform.alpha = f;
        taskViewTransform.translationZ = f2;
        taskViewTransform.dimAlpha = 0.0f;
        taskViewTransform.viewOutlineAlpha = 1.0f;
        taskViewTransform.rect.set(this.mTaskGridRect);
        taskViewTransform.rect.offset(i3, i4);
        Utilities.scaleRectAboutCenter(taskViewTransform.rect, taskViewTransform.scale);
        taskViewTransform.visible = z;
        return taskViewTransform;
    }

    public int navigateFocus(int i, int i2, NavigateTaskViewEvent.Direction direction) {
        if (i < 1 || i > 8) {
            return -1;
        }
        if (i2 == -1) {
            return 0;
        }
        int i3 = i - 1;
        TaskGridRectInfo taskGridRectInfo = this.mTaskGridRectInfoList[i3];
        int i4 = (i3 - i2) / taskGridRectInfo.tasksPerLine;
        switch (direction) {
            case UP:
                int i5 = taskGridRectInfo.tasksPerLine + i2;
                if (i5 < i) {
                    return i5;
                }
                return i2;
            case DOWN:
                int i6 = i2 - taskGridRectInfo.tasksPerLine;
                if (i6 >= 0) {
                    return i6;
                }
                return i2;
            case LEFT:
                int i7 = i2 + 1;
                if (i7 <= i3 - (i4 * taskGridRectInfo.tasksPerLine)) {
                    return i7;
                }
                return i2;
            case RIGHT:
                int i8 = i2 - 1;
                int i9 = (i3 - ((i4 + 1) * taskGridRectInfo.tasksPerLine)) + 1;
                if (i9 < 0) {
                    i9 = 0;
                }
                return i8 < i9 ? i2 : i8;
            default:
                return i2;
        }
    }

    public void initialize(Rect rect) {
        this.mWindowRect = rect;
        this.mPaddingLeftRight = (int) (0.025f * Math.min(this.mWindowRect.width(), this.mWindowRect.height()));
        this.mPaddingTopBottom = (int) (0.1d * this.mWindowRect.height());
        this.mTaskGridRectInfoList = new TaskGridRectInfo[8];
        int i = 0;
        while (i < 8) {
            int i2 = i + 1;
            this.mTaskGridRectInfoList[i] = new TaskGridRectInfo(i2);
            i = i2;
        }
    }

    public void setSystemInsets(Rect rect) {
        this.mSystemInsets = rect;
        updateAppAspectRatio();
    }

    private void updateAppAspectRatio() {
        this.mAppAspectRatio = ((this.mScreenSize.x - this.mSystemInsets.left) - this.mSystemInsets.right) / ((this.mScreenSize.y - this.mSystemInsets.top) - this.mSystemInsets.bottom);
    }

    public Rect getStackActionButtonRect() {
        Rect rect = new Rect(this.mWindowRect);
        rect.right -= this.mPaddingLeftRight;
        rect.left += this.mPaddingLeftRight;
        rect.bottom = rect.top + this.mPaddingTopBottom;
        return rect;
    }

    public void updateTaskGridRect(int i) {
        if (i > 0) {
            this.mTaskGridRect.set(this.mTaskGridRectInfoList[i - 1].size);
        }
    }

    public Rect getTaskGridRect() {
        return this.mTaskGridRect;
    }

    public int getFocusFrameThickness() {
        return this.mFocusedFrameThickness;
    }

    public TaskStackLayoutAlgorithm.VisibilityReport computeStackVisibilityReport(ArrayList<Task> arrayList) {
        int min = Math.min(8, arrayList.size());
        return new TaskStackLayoutAlgorithm.VisibilityReport(min, min);
    }
}
