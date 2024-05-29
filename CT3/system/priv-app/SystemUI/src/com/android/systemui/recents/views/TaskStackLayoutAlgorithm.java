package com.android.systemui.recents.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.ArraySet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.ViewDebug;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.misc.FreePathInterpolator;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import java.io.PrintWriter;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/recents/views/TaskStackLayoutAlgorithm.class */
public class TaskStackLayoutAlgorithm {
    @ViewDebug.ExportedProperty(category = "recents")
    private int mBaseBottomMargin;
    private int mBaseInitialBottomOffset;
    private int mBaseInitialTopOffset;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mBaseSideMargin;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mBaseTopMargin;
    private TaskStackLayoutAlgorithmCallbacks mCb;
    Context mContext;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mFocusState;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mFocusedBottomPeekHeight;
    private Path mFocusedCurve;
    private FreePathInterpolator mFocusedCurveInterpolator;
    private Path mFocusedDimCurve;
    private FreePathInterpolator mFocusedDimCurveInterpolator;
    private Range mFocusedRange;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mFocusedTopPeekHeight;
    FreeformWorkspaceLayoutAlgorithm mFreeformLayoutAlgorithm;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mFreeformStackGap;
    @ViewDebug.ExportedProperty(category = "recents")
    float mFrontMostTaskP;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mInitialBottomOffset;
    @ViewDebug.ExportedProperty(category = "recents")
    float mInitialScrollP;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mInitialTopOffset;
    @ViewDebug.ExportedProperty(category = "recents")
    float mMaxScrollP;
    @ViewDebug.ExportedProperty(category = "recents")
    int mMaxTranslationZ;
    private int mMinMargin;
    @ViewDebug.ExportedProperty(category = "recents")
    float mMinScrollP;
    @ViewDebug.ExportedProperty(category = "recents")
    int mMinTranslationZ;
    @ViewDebug.ExportedProperty(category = "recents")
    int mNumFreeformTasks;
    @ViewDebug.ExportedProperty(category = "recents")
    int mNumStackTasks;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mStackBottomOffset;
    private Path mUnfocusedCurve;
    private FreePathInterpolator mUnfocusedCurveInterpolator;
    private Path mUnfocusedDimCurve;
    private FreePathInterpolator mUnfocusedDimCurveInterpolator;
    private Range mUnfocusedRange;
    private StackState mState = StackState.SPLIT;
    @ViewDebug.ExportedProperty(category = "recents")
    public Rect mTaskRect = new Rect();
    @ViewDebug.ExportedProperty(category = "recents")
    public Rect mFreeformRect = new Rect();
    @ViewDebug.ExportedProperty(category = "recents")
    public Rect mStackRect = new Rect();
    @ViewDebug.ExportedProperty(category = "recents")
    public Rect mSystemInsets = new Rect();
    @ViewDebug.ExportedProperty(category = "recents")
    public Rect mStackActionButtonRect = new Rect();
    private SparseIntArray mTaskIndexMap = new SparseIntArray();
    private SparseArray<Float> mTaskIndexOverrideMap = new SparseArray<>();
    TaskViewTransform mBackOfStackTransform = new TaskViewTransform();
    TaskViewTransform mFrontOfStackTransform = new TaskViewTransform();

    /* loaded from: a.zip:com/android/systemui/recents/views/TaskStackLayoutAlgorithm$StackState.class */
    public static class StackState {
        public final int freeformBackgroundAlpha;
        public final float freeformHeightPct;
        public static final StackState FREEFORM_ONLY = new StackState(1.0f, 255);
        public static final StackState STACK_ONLY = new StackState(0.0f, 0);
        public static final StackState SPLIT = new StackState(0.5f, 255);

        private StackState(float f, int i) {
            this.freeformHeightPct = f;
            this.freeformBackgroundAlpha = i;
        }

        public static StackState getStackStateForStack(TaskStack taskStack) {
            boolean hasFreeformWorkspaceSupport = Recents.getSystemServices().hasFreeformWorkspaceSupport();
            int freeformTaskCount = taskStack.getFreeformTaskCount();
            return (!hasFreeformWorkspaceSupport || taskStack.getStackTaskCount() <= 0 || freeformTaskCount <= 0) ? (!hasFreeformWorkspaceSupport || freeformTaskCount <= 0) ? STACK_ONLY : FREEFORM_ONLY : SPLIT;
        }

        public void computeRects(Rect rect, Rect rect2, Rect rect3, int i, int i2, int i3) {
            int height = (int) (((rect3.height() - i) - i3) * this.freeformHeightPct);
            rect.set(rect3.left, rect3.top + i, rect3.right, rect3.top + i + Math.max(0, height - i2));
            rect2.set(rect3.left, rect3.top, rect3.right, rect3.bottom);
            if (height > 0) {
                rect2.top += height;
            } else {
                rect2.top += i;
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/recents/views/TaskStackLayoutAlgorithm$TaskStackLayoutAlgorithmCallbacks.class */
    public interface TaskStackLayoutAlgorithmCallbacks {
        void onFocusStateChanged(int i, int i2);
    }

    /* loaded from: a.zip:com/android/systemui/recents/views/TaskStackLayoutAlgorithm$VisibilityReport.class */
    public class VisibilityReport {
        public int numVisibleTasks;
        public int numVisibleThumbnails;
        final TaskStackLayoutAlgorithm this$0;

        VisibilityReport(TaskStackLayoutAlgorithm taskStackLayoutAlgorithm, int i, int i2) {
            this.this$0 = taskStackLayoutAlgorithm;
            this.numVisibleTasks = i;
            this.numVisibleThumbnails = i2;
        }
    }

    public TaskStackLayoutAlgorithm(Context context, TaskStackLayoutAlgorithmCallbacks taskStackLayoutAlgorithmCallbacks) {
        Resources resources = context.getResources();
        this.mContext = context;
        this.mCb = taskStackLayoutAlgorithmCallbacks;
        this.mFreeformLayoutAlgorithm = new FreeformWorkspaceLayoutAlgorithm(context);
        this.mMinMargin = resources.getDimensionPixelSize(2131689990);
        this.mBaseTopMargin = getDimensionForDevice(context, 2131689991, 2131689992, 2131689993);
        this.mBaseSideMargin = getDimensionForDevice(context, 2131689995, 2131689996, 2131689998);
        this.mBaseBottomMargin = resources.getDimensionPixelSize(2131689994);
        this.mFreeformStackGap = resources.getDimensionPixelSize(2131690010);
        reloadOnConfigurationChange(context);
    }

    private Path constructFocusedCurve() {
        float height = this.mFocusedTopPeekHeight / this.mStackRect.height();
        float height2 = (this.mStackBottomOffset + this.mFocusedBottomPeekHeight) / this.mStackRect.height();
        float height3 = ((this.mFocusedTopPeekHeight + this.mTaskRect.height()) - this.mMinMargin) / this.mStackRect.height();
        Path path = new Path();
        path.moveTo(0.0f, 1.0f);
        path.lineTo(0.5f, 1.0f - height);
        path.lineTo(1.0f - (0.5f / this.mFocusedRange.relativeMax), Math.max(1.0f - height3, height2));
        path.lineTo(1.0f, 0.0f);
        return path;
    }

    private Path constructFocusedDimCurve() {
        Path path = new Path();
        path.moveTo(0.0f, 0.25f);
        path.lineTo(0.5f, 0.0f);
        path.lineTo((0.5f / this.mFocusedRange.relativeMax) + 0.5f, 0.25f);
        path.lineTo(1.0f, 0.25f);
        return path;
    }

    private Path constructUnfocusedCurve() {
        float height = this.mFocusedTopPeekHeight / this.mStackRect.height();
        float f = ((1.0f - height) - 0.975f) / 0.099999994f;
        Path path = new Path();
        path.moveTo(0.0f, 1.0f);
        path.cubicTo(0.0f, 1.0f, 0.4f, 0.975f, 0.5f, 1.0f - height);
        path.cubicTo(0.5f, 1.0f - height, 0.65f, (0.65f * f) + (1.0f - (0.4f * f)), 1.0f, 0.0f);
        return path;
    }

    private Path constructUnfocusedDimCurve() {
        float normalizedXFromUnfocusedY = getNormalizedXFromUnfocusedY(this.mInitialTopOffset, 0);
        float f = normalizedXFromUnfocusedY + ((1.0f - normalizedXFromUnfocusedY) / 2.0f);
        Path path = new Path();
        path.moveTo(0.0f, 0.25f);
        path.cubicTo(0.5f * normalizedXFromUnfocusedY, 0.25f, 0.75f * normalizedXFromUnfocusedY, 0.1875f, normalizedXFromUnfocusedY, 0.0f);
        path.cubicTo(f, 0.0f, f, 0.15f, 1.0f, 0.15f);
        return path;
    }

    public static int getDimensionForDevice(Context context, int i, int i2, int i3) {
        return getDimensionForDevice(context, i, i, i2, i2, i3, i3);
    }

    public static int getDimensionForDevice(Context context, int i, int i2, int i3, int i4, int i5, int i6) {
        RecentsConfiguration configuration = Recents.getConfiguration();
        Resources resources = context.getResources();
        boolean z = Utilities.getAppConfiguration(context).orientation == 2;
        if (configuration.isXLargeScreen) {
            if (z) {
                i5 = i6;
            }
            return resources.getDimensionPixelSize(i5);
        } else if (configuration.isLargeScreen) {
            if (!z) {
                i4 = i3;
            }
            return resources.getDimensionPixelSize(i4);
        } else {
            if (!z) {
                i2 = i;
            }
            return resources.getDimensionPixelSize(i2);
        }
    }

    private float getNormalizedXFromFocusedY(float f, int i) {
        if (i == 0) {
            f = this.mStackRect.height() - f;
        }
        return this.mFocusedCurveInterpolator.getX(f / this.mStackRect.height());
    }

    private float getNormalizedXFromUnfocusedY(float f, int i) {
        if (i == 0) {
            f = this.mStackRect.height() - f;
        }
        return this.mUnfocusedCurveInterpolator.getX(f / this.mStackRect.height());
    }

    private int getScaleForExtent(Rect rect, Rect rect2, int i, int i2, int i3) {
        if (i3 == 0) {
            return Math.max(i2, (int) (i * Utilities.clamp01(rect.width() / rect2.width())));
        } else if (i3 == 1) {
            return Math.max(i2, (int) (i * Utilities.clamp01(rect.height() / rect2.height())));
        } else {
            return i;
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:21:0x0055, code lost:
        if (r4 > r6) goto L17;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private boolean isInvalidOverrideX(float f, float f2, float f3) {
        boolean z = true;
        if (!(this.mUnfocusedRange.getNormalizedX(f3) >= 0.0f ? this.mUnfocusedRange.getNormalizedX(f3) > 1.0f : true)) {
            if (f2 < f || f < f3) {
                if (f2 <= f) {
                    z = true;
                }
                z = false;
            } else {
                z = true;
            }
        }
        return z;
    }

    private void updateFrontBackTransforms() {
        if (this.mStackRect.isEmpty()) {
            return;
        }
        float mapRange = Utilities.mapRange(this.mFocusState, this.mUnfocusedRange.relativeMin, this.mFocusedRange.relativeMin);
        float mapRange2 = Utilities.mapRange(this.mFocusState, this.mUnfocusedRange.relativeMax, this.mFocusedRange.relativeMax);
        getStackTransform(mapRange, mapRange, 0.0f, this.mFocusState, this.mBackOfStackTransform, null, true, true);
        getStackTransform(mapRange2, mapRange2, 0.0f, this.mFocusState, this.mFrontOfStackTransform, null, true, true);
        this.mBackOfStackTransform.visible = true;
        this.mFrontOfStackTransform.visible = true;
    }

    public void addUnfocusedTaskOverride(Task task, float f) {
        if (this.mFocusState != 0) {
            this.mFocusedRange.offset(f);
            this.mUnfocusedRange.offset(f);
            float normalizedX = this.mFocusedRange.getNormalizedX(this.mTaskIndexMap.get(task.key.id));
            float x = this.mUnfocusedCurveInterpolator.getX(this.mFocusedCurveInterpolator.getInterpolation(normalizedX));
            float absoluteX = this.mUnfocusedRange.getAbsoluteX(x);
            if (Float.compare(normalizedX, x) != 0) {
                this.mTaskIndexOverrideMap.put(task.key.id, Float.valueOf(f + absoluteX));
            }
        }
    }

    public void addUnfocusedTaskOverride(TaskView taskView, float f) {
        this.mFocusedRange.offset(f);
        this.mUnfocusedRange.offset(f);
        Task task = taskView.getTask();
        int top = taskView.getTop() - this.mTaskRect.top;
        float normalizedXFromFocusedY = getNormalizedXFromFocusedY(top, 0);
        float normalizedXFromUnfocusedY = getNormalizedXFromUnfocusedY(top, 0);
        float absoluteX = this.mUnfocusedRange.getAbsoluteX(normalizedXFromUnfocusedY);
        if (Float.compare(normalizedXFromFocusedY, normalizedXFromUnfocusedY) != 0) {
            this.mTaskIndexOverrideMap.put(task.key.id, Float.valueOf(f + absoluteX));
        }
    }

    public void clearUnfocusedTaskOverrides() {
        this.mTaskIndexOverrideMap.clear();
    }

    public VisibilityReport computeStackVisibilityReport(ArrayList<Task> arrayList) {
        int i;
        int i2;
        int i3;
        float f;
        if (arrayList.size() <= 1) {
            return new VisibilityReport(this, 1, 1);
        }
        if (this.mNumStackTasks == 0) {
            return new VisibilityReport(this, Math.max(this.mNumFreeformTasks, 1), Math.max(this.mNumFreeformTasks, 1));
        }
        TaskViewTransform taskViewTransform = new TaskViewTransform();
        Range range = ((float) getInitialFocusState()) > 0.0f ? this.mFocusedRange : this.mUnfocusedRange;
        range.offset(this.mInitialScrollP);
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(2131690012);
        int max = Math.max(this.mNumFreeformTasks, 1);
        int max2 = Math.max(this.mNumFreeformTasks, 1);
        float f2 = 2.14748365E9f;
        int size = arrayList.size() - 1;
        while (true) {
            i = max;
            if (size < 0) {
                break;
            }
            Task task = arrayList.get(size);
            if (task.isFreeformTask()) {
                f = f2;
                i3 = max2;
                i2 = max;
            } else {
                float stackScrollForTask = getStackScrollForTask(task);
                i2 = max;
                i3 = max2;
                f = f2;
                if (range.isInRange(stackScrollForTask)) {
                    boolean isFrontMostTask = task.group != null ? task.group.isFrontMostTask(task) : true;
                    if (isFrontMostTask) {
                        getStackTransform(stackScrollForTask, stackScrollForTask, this.mInitialScrollP, this.mFocusState, taskViewTransform, null, false, false);
                        f = taskViewTransform.rect.top;
                        if (!(f2 - f > ((float) dimensionPixelSize))) {
                            while (true) {
                                i = max;
                                if (size < 0) {
                                    break;
                                }
                                max++;
                                if (!range.isInRange(getStackScrollForTask(arrayList.get(size)))) {
                                }
                                size--;
                            }
                        } else {
                            i3 = max2 + 1;
                            i2 = max + 1;
                        }
                    } else {
                        i2 = max;
                        i3 = max2;
                        f = f2;
                        if (!isFrontMostTask) {
                            i2 = max + 1;
                            i3 = max2;
                            f = f2;
                        }
                    }
                } else {
                    continue;
                }
            }
            size--;
            max = i2;
            max2 = i3;
            f2 = f;
        }
        return new VisibilityReport(this, i, max2);
    }

    public void dump(String str, PrintWriter printWriter) {
        String str2 = str + "  ";
        printWriter.print(str);
        printWriter.print("TaskStackLayoutAlgorithm");
        printWriter.write(" numStackTasks=");
        printWriter.write(this.mNumStackTasks);
        printWriter.println();
        printWriter.print(str2);
        printWriter.print("insets=");
        printWriter.print(Utilities.dumpRect(this.mSystemInsets));
        printWriter.print(" stack=");
        printWriter.print(Utilities.dumpRect(this.mStackRect));
        printWriter.print(" task=");
        printWriter.print(Utilities.dumpRect(this.mTaskRect));
        printWriter.print(" freeform=");
        printWriter.print(Utilities.dumpRect(this.mFreeformRect));
        printWriter.print(" actionButton=");
        printWriter.print(Utilities.dumpRect(this.mStackActionButtonRect));
        printWriter.println();
        printWriter.print(str2);
        printWriter.print("minScroll=");
        printWriter.print(this.mMinScrollP);
        printWriter.print(" maxScroll=");
        printWriter.print(this.mMaxScrollP);
        printWriter.print(" initialScroll=");
        printWriter.print(this.mInitialScrollP);
        printWriter.println();
        printWriter.print(str2);
        printWriter.print("focusState=");
        printWriter.print(this.mFocusState);
        printWriter.println();
        if (this.mTaskIndexOverrideMap.size() > 0) {
            for (int size = this.mTaskIndexOverrideMap.size() - 1; size >= 0; size--) {
                int keyAt = this.mTaskIndexOverrideMap.keyAt(size);
                float f = this.mTaskIndexMap.get(keyAt);
                float floatValue = this.mTaskIndexOverrideMap.get(keyAt, Float.valueOf(0.0f)).floatValue();
                printWriter.print(str2);
                printWriter.print("taskId= ");
                printWriter.print(keyAt);
                printWriter.print(" x= ");
                printWriter.print(f);
                printWriter.print(" overrideX= ");
                printWriter.print(floatValue);
                printWriter.println();
            }
        }
    }

    public TaskViewTransform getBackOfStackTransform() {
        return this.mBackOfStackTransform;
    }

    public float getDeltaPForY(int i, int i2) {
        return -(((i2 - i) / this.mStackRect.height()) * this.mUnfocusedCurveInterpolator.getArcLength());
    }

    public int getFocusState() {
        return this.mFocusState;
    }

    public TaskViewTransform getFrontOfStackTransform() {
        return this.mFrontOfStackTransform;
    }

    public int getInitialFocusState() {
        return (Recents.getDebugFlags().isPagingEnabled() || Recents.getConfiguration().getLaunchState().launchedWithAltTab) ? 1 : 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getStackScrollForTask(Task task) {
        Float f = this.mTaskIndexOverrideMap.get(task.key.id, null);
        return f == null ? this.mTaskIndexMap.get(task.key.id, 0) : f.floatValue();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getStackScrollForTaskAtInitialOffset(Task task) {
        float normalizedXFromUnfocusedY = getNormalizedXFromUnfocusedY(this.mInitialTopOffset, 0);
        this.mUnfocusedRange.offset(0.0f);
        return Utilities.clamp(this.mTaskIndexMap.get(task.key.id, 0) - Math.max(0.0f, this.mUnfocusedRange.getAbsoluteX(normalizedXFromUnfocusedY)), this.mMinScrollP, this.mMaxScrollP);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getStackScrollForTaskIgnoreOverrides(Task task) {
        return this.mTaskIndexMap.get(task.key.id, 0);
    }

    public StackState getStackState() {
        return this.mState;
    }

    public TaskViewTransform getStackTransform(Task task, float f, int i, TaskViewTransform taskViewTransform, TaskViewTransform taskViewTransform2, boolean z, boolean z2) {
        if (this.mFreeformLayoutAlgorithm.isTransformAvailable(task, this)) {
            this.mFreeformLayoutAlgorithm.getTransform(task, taskViewTransform, this);
            return taskViewTransform;
        }
        int i2 = this.mTaskIndexMap.get(task.key.id, -1);
        if (task == null || i2 == -1) {
            taskViewTransform.reset();
            return taskViewTransform;
        }
        getStackTransform(z2 ? i2 : getStackScrollForTask(task), i2, f, i, taskViewTransform, taskViewTransform2, false, z);
        return taskViewTransform;
    }

    public TaskViewTransform getStackTransform(Task task, float f, TaskViewTransform taskViewTransform, TaskViewTransform taskViewTransform2) {
        return getStackTransform(task, f, this.mFocusState, taskViewTransform, taskViewTransform2, false, false);
    }

    public TaskViewTransform getStackTransform(Task task, float f, TaskViewTransform taskViewTransform, TaskViewTransform taskViewTransform2, boolean z) {
        return getStackTransform(task, f, this.mFocusState, taskViewTransform, taskViewTransform2, false, z);
    }

    public void getStackTransform(float f, float f2, float f3, int i, TaskViewTransform taskViewTransform, TaskViewTransform taskViewTransform2, boolean z, boolean z2) {
        float mapRange;
        float mapRange2;
        float mapRange3;
        int i2;
        SystemServicesProxy systemServices = Recents.getSystemServices();
        this.mUnfocusedRange.offset(f3);
        this.mFocusedRange.offset(f3);
        boolean isInRange = this.mUnfocusedRange.isInRange(f);
        boolean isInRange2 = this.mFocusedRange.isInRange(f);
        if (!z2 && !isInRange && !isInRange2) {
            taskViewTransform.reset();
            return;
        }
        this.mUnfocusedRange.offset(f3);
        this.mFocusedRange.offset(f3);
        float normalizedX = this.mUnfocusedRange.getNormalizedX(f);
        float normalizedX2 = this.mFocusedRange.getNormalizedX(f);
        float clamp = Utilities.clamp(f3, this.mMinScrollP, this.mMaxScrollP);
        this.mUnfocusedRange.offset(clamp);
        this.mFocusedRange.offset(clamp);
        float normalizedX3 = this.mUnfocusedRange.getNormalizedX(f);
        float normalizedX4 = this.mUnfocusedRange.getNormalizedX(f2);
        float clamp2 = Utilities.clamp(f3, -3.4028235E38f, this.mMaxScrollP);
        this.mUnfocusedRange.offset(clamp2);
        this.mFocusedRange.offset(clamp2);
        float normalizedX5 = this.mUnfocusedRange.getNormalizedX(f);
        float normalizedX6 = this.mFocusedRange.getNormalizedX(f);
        int width = (this.mStackRect.width() - this.mTaskRect.width()) / 2;
        if (systemServices.hasFreeformWorkspaceSupport() || this.mNumStackTasks != 1 || z) {
            int interpolation = (int) ((1.0f - this.mUnfocusedCurveInterpolator.getInterpolation(normalizedX)) * this.mStackRect.height());
            int interpolation2 = (int) ((1.0f - this.mFocusedCurveInterpolator.getInterpolation(normalizedX2)) * this.mStackRect.height());
            float interpolation3 = this.mUnfocusedDimCurveInterpolator.getInterpolation(normalizedX5);
            float interpolation4 = this.mFocusedDimCurveInterpolator.getInterpolation(normalizedX6);
            float f4 = interpolation3;
            if (this.mNumStackTasks <= 2) {
                f4 = interpolation3;
                if (f2 == 0.0f) {
                    if (normalizedX3 >= 0.5f) {
                        f4 = 0.0f;
                    } else {
                        float interpolation5 = this.mUnfocusedDimCurveInterpolator.getInterpolation(0.5f);
                        f4 = (interpolation3 - interpolation5) * (0.25f / (0.25f - interpolation5));
                    }
                }
            }
            int mapRange4 = (this.mStackRect.top - this.mTaskRect.top) + ((int) Utilities.mapRange(i, interpolation, interpolation2));
            mapRange = Utilities.mapRange(Utilities.clamp01(normalizedX4), this.mMinTranslationZ, this.mMaxTranslationZ);
            mapRange2 = Utilities.mapRange(i, f4, interpolation4);
            mapRange3 = Utilities.mapRange(Utilities.clamp01(normalizedX3), 0.0f, 2.0f);
            i2 = mapRange4;
        } else {
            i2 = (this.mStackRect.top - this.mTaskRect.top) + (((this.mStackRect.height() - this.mSystemInsets.bottom) - this.mTaskRect.height()) / 2) + getYForDeltaP((this.mMinScrollP - f3) / this.mNumStackTasks, 0.0f);
            mapRange = this.mMaxTranslationZ;
            mapRange2 = 0.0f;
            mapRange3 = 1.0f;
        }
        taskViewTransform.scale = 1.0f;
        taskViewTransform.alpha = 1.0f;
        taskViewTransform.translationZ = mapRange;
        taskViewTransform.dimAlpha = mapRange2;
        taskViewTransform.viewOutlineAlpha = mapRange3;
        taskViewTransform.rect.set(this.mTaskRect);
        taskViewTransform.rect.offset(width, i2);
        Utilities.scaleRectAboutCenter(taskViewTransform.rect, taskViewTransform.scale);
        taskViewTransform.visible = taskViewTransform.rect.top < ((float) this.mStackRect.bottom) ? taskViewTransform2 == null || taskViewTransform.rect.top != taskViewTransform2.rect.top : false;
    }

    public TaskViewTransform getStackTransformScreenCoordinates(Task task, float f, TaskViewTransform taskViewTransform, TaskViewTransform taskViewTransform2, Rect rect) {
        return transformToScreenCoordinates(getStackTransform(task, f, this.mFocusState, taskViewTransform, taskViewTransform2, true, false), rect);
    }

    public void getTaskStackBounds(Rect rect, Rect rect2, int i, int i2, Rect rect3) {
        rect3.set(rect2.left, rect2.top + i, rect2.right - i2, rect2.bottom);
        int width = rect3.width() - (getScaleForExtent(rect2, rect, this.mBaseSideMargin, this.mMinMargin, 0) * 2);
        int i3 = width;
        if (Utilities.getAppConfiguration(this.mContext).orientation == 2) {
            Rect rect4 = new Rect(0, 0, Math.min(rect.width(), rect.height()), Math.max(rect.width(), rect.height()));
            i3 = Math.min(width, rect4.width() - (getScaleForExtent(rect4, rect4, this.mBaseSideMargin, this.mMinMargin, 0) * 2));
        }
        rect3.inset((rect3.width() - i3) / 2, 0);
    }

    public Rect getUntransformedTaskViewBounds() {
        return new Rect(this.mTaskRect);
    }

    public int getYForDeltaP(float f, float f2) {
        return -((int) ((f2 - f) * this.mStackRect.height() * (1.0f / this.mUnfocusedCurveInterpolator.getArcLength())));
    }

    public void initialize(Rect rect, Rect rect2, Rect rect3, StackState stackState) {
        Rect rect4 = new Rect(this.mStackRect);
        int scaleForExtent = getScaleForExtent(rect2, rect, this.mBaseTopMargin, this.mMinMargin, 1);
        int scaleForExtent2 = getScaleForExtent(rect2, rect, this.mBaseBottomMargin, this.mMinMargin, 1);
        this.mInitialTopOffset = getScaleForExtent(rect2, rect, this.mBaseInitialTopOffset, this.mMinMargin, 1);
        this.mInitialBottomOffset = this.mBaseInitialBottomOffset;
        this.mState = stackState;
        this.mStackBottomOffset = this.mSystemInsets.bottom + scaleForExtent2;
        stackState.computeRects(this.mFreeformRect, this.mStackRect, rect3, scaleForExtent, this.mFreeformStackGap, this.mStackBottomOffset);
        this.mStackActionButtonRect.set(this.mStackRect.left, this.mStackRect.top - scaleForExtent, this.mStackRect.right, this.mStackRect.top + this.mFocusedTopPeekHeight);
        int height = this.mStackRect.height();
        int i = this.mInitialTopOffset;
        this.mTaskRect.set(this.mStackRect.left, this.mStackRect.top, this.mStackRect.right, this.mStackRect.top + ((height - i) - this.mStackBottomOffset));
        if (rect4.equals(this.mStackRect)) {
            return;
        }
        this.mUnfocusedCurve = constructUnfocusedCurve();
        this.mUnfocusedCurveInterpolator = new FreePathInterpolator(this.mUnfocusedCurve);
        this.mFocusedCurve = constructFocusedCurve();
        this.mFocusedCurveInterpolator = new FreePathInterpolator(this.mFocusedCurve);
        this.mUnfocusedDimCurve = constructUnfocusedDimCurve();
        this.mUnfocusedDimCurveInterpolator = new FreePathInterpolator(this.mUnfocusedDimCurve);
        this.mFocusedDimCurve = constructFocusedDimCurve();
        this.mFocusedDimCurveInterpolator = new FreePathInterpolator(this.mFocusedDimCurve);
        updateFrontBackTransforms();
    }

    public boolean isInitialized() {
        return !this.mStackRect.isEmpty();
    }

    public void reloadOnConfigurationChange(Context context) {
        Resources resources = context.getResources();
        this.mFocusedRange = new Range(resources.getFloat(2131755072), resources.getFloat(2131755073));
        this.mUnfocusedRange = new Range(resources.getFloat(2131755074), resources.getFloat(2131755075));
        this.mFocusState = getInitialFocusState();
        this.mFocusedTopPeekHeight = resources.getDimensionPixelSize(2131690000);
        this.mFocusedBottomPeekHeight = resources.getDimensionPixelSize(2131690001);
        this.mMinTranslationZ = resources.getDimensionPixelSize(2131690008);
        this.mMaxTranslationZ = resources.getDimensionPixelSize(2131690009);
        this.mBaseInitialTopOffset = getDimensionForDevice(context, 2131690002, 2131690004, 2131690006, 2131690006, 2131690006, 2131690006);
        this.mBaseInitialBottomOffset = getDimensionForDevice(context, 2131690003, 2131690005, 2131690007, 2131690007, 2131690007, 2131690007);
        this.mFreeformLayoutAlgorithm.reloadOnConfigurationChange(context);
    }

    public void reset() {
        this.mTaskIndexOverrideMap.clear();
        setFocusState(getInitialFocusState());
    }

    public void setFocusState(int i) {
        int i2 = this.mFocusState;
        this.mFocusState = i;
        updateFrontBackTransforms();
        if (this.mCb != null) {
            this.mCb.onFocusStateChanged(i2, i);
        }
    }

    public boolean setSystemInsets(Rect rect) {
        boolean z = !this.mSystemInsets.equals(rect);
        this.mSystemInsets.set(rect);
        return z;
    }

    public void setTaskOverridesForInitialState(TaskStack taskStack, boolean z) {
        int i;
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        this.mTaskIndexOverrideMap.clear();
        boolean z2 = !launchState.launchedFromHome ? launchState.launchedViaDockGesture : true;
        if (getInitialFocusState() != 0 || this.mNumStackTasks <= 1) {
            return;
        }
        if (z || !(launchState.launchedWithAltTab || z2)) {
            float normalizedXFromUnfocusedY = getNormalizedXFromUnfocusedY(this.mSystemInsets.bottom + this.mInitialBottomOffset, 1);
            float[] fArr = this.mNumStackTasks <= 2 ? new float[]{Math.min(getNormalizedXFromUnfocusedY((this.mFocusedTopPeekHeight + this.mTaskRect.height()) - this.mMinMargin, 0), normalizedXFromUnfocusedY), getNormalizedXFromUnfocusedY(this.mFocusedTopPeekHeight, 0)} : new float[]{normalizedXFromUnfocusedY, getNormalizedXFromUnfocusedY(this.mInitialTopOffset, 0)};
            this.mUnfocusedRange.offset(0.0f);
            ArrayList<Task> stackTasks = taskStack.getStackTasks();
            int size = stackTasks.size();
            for (int i2 = size - 1; i2 >= 0 && (i = (size - i2) - 1) < fArr.length; i2--) {
                this.mTaskIndexOverrideMap.put(stackTasks.get(i2).key.id, Float.valueOf(this.mInitialScrollP + this.mUnfocusedRange.getAbsoluteX(fArr[i])));
            }
        }
    }

    public TaskViewTransform transformToScreenCoordinates(TaskViewTransform taskViewTransform, Rect rect) {
        if (rect == null) {
            rect = Recents.getSystemServices().getWindowRect();
        }
        taskViewTransform.rect.offset(rect.left, rect.top);
        return taskViewTransform;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void update(TaskStack taskStack, ArraySet<Task.TaskKey> arraySet) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        this.mTaskIndexMap.clear();
        ArrayList<Task> stackTasks = taskStack.getStackTasks();
        if (stackTasks.isEmpty()) {
            this.mFrontMostTaskP = 0.0f;
            this.mInitialScrollP = 0.0f;
            this.mMaxScrollP = 0.0f;
            this.mMinScrollP = 0.0f;
            this.mNumFreeformTasks = 0;
            this.mNumStackTasks = 0;
            return;
        }
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (int i = 0; i < stackTasks.size(); i++) {
            Task task = stackTasks.get(i);
            if (!arraySet.contains(task.key)) {
                if (task.isFreeformTask()) {
                    arrayList.add(task);
                } else {
                    arrayList2.add(task);
                }
            }
        }
        this.mNumStackTasks = arrayList2.size();
        this.mNumFreeformTasks = arrayList.size();
        int size = arrayList2.size();
        for (int i2 = 0; i2 < size; i2++) {
            this.mTaskIndexMap.put(((Task) arrayList2.get(i2)).key.id, i2);
        }
        if (!arrayList.isEmpty()) {
            this.mFreeformLayoutAlgorithm.update(arrayList, this);
        }
        Task launchTarget = taskStack.getLaunchTarget();
        int indexOfStackTask = launchTarget != null ? taskStack.indexOfStackTask(launchTarget) : this.mNumStackTasks - 1;
        if (getInitialFocusState() == 1) {
            float normalizedXFromFocusedY = getNormalizedXFromFocusedY(this.mStackBottomOffset + this.mTaskRect.height(), 1);
            this.mFocusedRange.offset(0.0f);
            this.mMinScrollP = 0.0f;
            this.mMaxScrollP = Math.max(this.mMinScrollP, (this.mNumStackTasks - 1) - Math.max(0.0f, this.mFocusedRange.getAbsoluteX(normalizedXFromFocusedY)));
            if (launchState.launchedFromHome) {
                this.mInitialScrollP = Utilities.clamp(indexOfStackTask, this.mMinScrollP, this.mMaxScrollP);
            } else {
                this.mInitialScrollP = Utilities.clamp(indexOfStackTask - 1, this.mMinScrollP, this.mMaxScrollP);
            }
        } else if (!systemServices.hasFreeformWorkspaceSupport() && this.mNumStackTasks == 1) {
            this.mMinScrollP = 0.0f;
            this.mMaxScrollP = 0.0f;
            this.mInitialScrollP = 0.0f;
        } else {
            float normalizedXFromUnfocusedY = getNormalizedXFromUnfocusedY(this.mStackBottomOffset + this.mTaskRect.height(), 1);
            this.mUnfocusedRange.offset(0.0f);
            this.mMinScrollP = 0.0f;
            this.mMaxScrollP = Math.max(this.mMinScrollP, (this.mNumStackTasks - 1) - Math.max(0.0f, this.mUnfocusedRange.getAbsoluteX(normalizedXFromUnfocusedY)));
            boolean z = !launchState.launchedFromHome ? launchState.launchedViaDockGesture : true;
            if (launchState.launchedWithAltTab) {
                this.mInitialScrollP = Utilities.clamp(indexOfStackTask, this.mMinScrollP, this.mMaxScrollP);
            } else if (z) {
                this.mInitialScrollP = Utilities.clamp(indexOfStackTask, this.mMinScrollP, this.mMaxScrollP);
            } else {
                this.mInitialScrollP = Math.max(this.mMinScrollP, Math.min(this.mMaxScrollP, this.mNumStackTasks - 2) - Math.max(0.0f, this.mUnfocusedRange.getAbsoluteX(getNormalizedXFromUnfocusedY(this.mInitialTopOffset, 0))));
            }
        }
    }

    public float updateFocusStateOnScroll(float f, float f2, float f3) {
        if (f2 == f3) {
            return f2;
        }
        float f4 = f2 - f3;
        float f5 = f2;
        this.mUnfocusedRange.offset(f2);
        for (int size = this.mTaskIndexOverrideMap.size() - 1; size >= 0; size--) {
            int keyAt = this.mTaskIndexOverrideMap.keyAt(size);
            float f6 = this.mTaskIndexMap.get(keyAt);
            float floatValue = this.mTaskIndexOverrideMap.get(keyAt, Float.valueOf(0.0f)).floatValue();
            float f7 = floatValue + f4;
            if (isInvalidOverrideX(f6, floatValue, f7)) {
                this.mTaskIndexOverrideMap.removeAt(size);
            } else if ((floatValue < f6 || f4 > 0.0f) && (floatValue > f6 || f4 < 0.0f)) {
                f5 = f3;
                float f8 = floatValue - (f2 - f);
                if (isInvalidOverrideX(f6, floatValue, f8)) {
                    this.mTaskIndexOverrideMap.removeAt(size);
                } else {
                    this.mTaskIndexOverrideMap.put(keyAt, Float.valueOf(f8));
                }
            } else {
                this.mTaskIndexOverrideMap.put(keyAt, Float.valueOf(f7));
            }
        }
        return f5;
    }
}
