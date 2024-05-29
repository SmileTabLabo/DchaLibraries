package com.android.systemui.recents.model;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.IntProperty;
import android.util.Property;
import android.util.SparseArray;
import android.view.animation.Interpolator;
import com.android.internal.policy.DockedDividerUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.AnimationProps;
import com.android.systemui.recents.views.DropTarget;
import com.android.systemui.recents.views.TaskStackLayoutAlgorithm;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/recents/model/TaskStack.class */
public class TaskStack {
    TaskStackCallbacks mCb;
    private Comparator<Task> FREEFORM_COMPARATOR = new Comparator<Task>(this) { // from class: com.android.systemui.recents.model.TaskStack.1
        final TaskStack this$0;

        {
            this.this$0 = this;
        }

        @Override // java.util.Comparator
        public int compare(Task task, Task task2) {
            if (!task.isFreeformTask() || task2.isFreeformTask()) {
                if (!task2.isFreeformTask() || task.isFreeformTask()) {
                    return Long.compare(task.temporarySortIndexInStack, task2.temporarySortIndexInStack);
                }
                return -1;
            }
            return 1;
        }
    };
    ArrayList<Task> mRawTaskList = new ArrayList<>();
    FilteredTaskList mStackTaskList = new FilteredTaskList();
    ArrayList<TaskGrouping> mGroups = new ArrayList<>();
    ArrayMap<Integer, TaskGrouping> mAffinitiesGroups = new ArrayMap<>();

    /* loaded from: a.zip:com/android/systemui/recents/model/TaskStack$DockState.class */
    public static class DockState implements DropTarget {
        public final int createMode;
        private final RectF dockArea;
        public final int dockSide;
        private final RectF expandedTouchDockArea;
        private final RectF touchArea;
        public final ViewState viewState;
        public static final DockState NONE = new DockState(-1, -1, 80, 255, 0, null, null, null);
        public static final DockState LEFT = new DockState(1, 0, 80, 0, 1, new RectF(0.0f, 0.0f, 0.125f, 1.0f), new RectF(0.0f, 0.0f, 0.125f, 1.0f), new RectF(0.0f, 0.0f, 0.5f, 1.0f));
        public static final DockState TOP = new DockState(2, 0, 80, 0, 0, new RectF(0.0f, 0.0f, 1.0f, 0.125f), new RectF(0.0f, 0.0f, 1.0f, 0.125f), new RectF(0.0f, 0.0f, 1.0f, 0.5f));
        public static final DockState RIGHT = new DockState(3, 1, 80, 0, 1, new RectF(0.875f, 0.0f, 1.0f, 1.0f), new RectF(0.875f, 0.0f, 1.0f, 1.0f), new RectF(0.5f, 0.0f, 1.0f, 1.0f));
        public static final DockState BOTTOM = new DockState(4, 1, 80, 0, 0, new RectF(0.0f, 0.875f, 1.0f, 1.0f), new RectF(0.0f, 0.875f, 1.0f, 1.0f), new RectF(0.0f, 0.5f, 1.0f, 1.0f));

        /* loaded from: a.zip:com/android/systemui/recents/model/TaskStack$DockState$ViewState.class */
        public static class ViewState {
            private static final IntProperty<ViewState> HINT_ALPHA = new IntProperty<ViewState>("drawableAlpha") { // from class: com.android.systemui.recents.model.TaskStack.DockState.ViewState.1
                @Override // android.util.Property
                public Integer get(ViewState viewState) {
                    return Integer.valueOf(viewState.mHintTextAlpha);
                }

                @Override // android.util.IntProperty
                public void setValue(ViewState viewState, int i) {
                    viewState.mHintTextAlpha = i;
                    viewState.dockAreaOverlay.invalidateSelf();
                }
            };
            public final int dockAreaAlpha;
            public final ColorDrawable dockAreaOverlay;
            public final int hintTextAlpha;
            public final int hintTextOrientation;
            private AnimatorSet mDockAreaOverlayAnimator;
            private String mHintText;
            private int mHintTextAlpha;
            private Point mHintTextBounds;
            private Paint mHintTextPaint;
            private final int mHintTextResId;
            private Rect mTmpRect;

            private ViewState(int i, int i2, int i3, int i4) {
                this.mHintTextBounds = new Point();
                this.mHintTextAlpha = 255;
                this.mTmpRect = new Rect();
                this.dockAreaAlpha = i;
                this.dockAreaOverlay = new ColorDrawable(-1);
                this.dockAreaOverlay.setAlpha(0);
                this.hintTextAlpha = i2;
                this.hintTextOrientation = i3;
                this.mHintTextResId = i4;
                this.mHintTextPaint = new Paint(1);
                this.mHintTextPaint.setColor(-1);
            }

            /* synthetic */ ViewState(int i, int i2, int i3, int i4, ViewState viewState) {
                this(i, i2, i3, i4);
            }

            public void draw(Canvas canvas) {
                if (this.dockAreaOverlay.getAlpha() > 0) {
                    this.dockAreaOverlay.draw(canvas);
                }
                if (this.mHintTextAlpha > 0) {
                    Rect bounds = this.dockAreaOverlay.getBounds();
                    int i = bounds.left;
                    int width = (bounds.width() - this.mHintTextBounds.x) / 2;
                    int i2 = bounds.top;
                    int height = (bounds.height() + this.mHintTextBounds.y) / 2;
                    this.mHintTextPaint.setAlpha(this.mHintTextAlpha);
                    if (this.hintTextOrientation == 1) {
                        canvas.save();
                        canvas.rotate(-90.0f, bounds.centerX(), bounds.centerY());
                    }
                    canvas.drawText(this.mHintText, i + width, i2 + height, this.mHintTextPaint);
                    if (this.hintTextOrientation == 1) {
                        canvas.restore();
                    }
                }
            }

            public void startAnimation(Rect rect, int i, int i2, int i3, Interpolator interpolator, boolean z, boolean z2) {
                if (this.mDockAreaOverlayAnimator != null) {
                    this.mDockAreaOverlayAnimator.cancel();
                }
                ArrayList arrayList = new ArrayList();
                if (this.dockAreaOverlay.getAlpha() != i) {
                    if (z) {
                        ObjectAnimator ofInt = ObjectAnimator.ofInt(this.dockAreaOverlay, (Property<ColorDrawable, Integer>) Utilities.DRAWABLE_ALPHA, this.dockAreaOverlay.getAlpha(), i);
                        ofInt.setDuration(i3);
                        ofInt.setInterpolator(interpolator);
                        arrayList.add(ofInt);
                    } else {
                        this.dockAreaOverlay.setAlpha(i);
                    }
                }
                if (this.mHintTextAlpha != i2) {
                    if (z) {
                        ObjectAnimator ofInt2 = ObjectAnimator.ofInt(this, HINT_ALPHA, this.mHintTextAlpha, i2);
                        ofInt2.setDuration(150L);
                        ofInt2.setInterpolator(i2 > this.mHintTextAlpha ? Interpolators.ALPHA_IN : Interpolators.ALPHA_OUT);
                        arrayList.add(ofInt2);
                    } else {
                        this.mHintTextAlpha = i2;
                        this.dockAreaOverlay.invalidateSelf();
                    }
                }
                if (rect != null && !this.dockAreaOverlay.getBounds().equals(rect)) {
                    if (z2) {
                        ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(this.dockAreaOverlay, PropertyValuesHolder.ofObject(Utilities.DRAWABLE_RECT, Utilities.RECT_EVALUATOR, new Rect(this.dockAreaOverlay.getBounds()), rect));
                        ofPropertyValuesHolder.setDuration(i3);
                        ofPropertyValuesHolder.setInterpolator(interpolator);
                        arrayList.add(ofPropertyValuesHolder);
                    } else {
                        this.dockAreaOverlay.setBounds(rect);
                    }
                }
                if (arrayList.isEmpty()) {
                    return;
                }
                this.mDockAreaOverlayAnimator = new AnimatorSet();
                this.mDockAreaOverlayAnimator.playTogether(arrayList);
                this.mDockAreaOverlayAnimator.start();
            }

            public void update(Context context) {
                Resources resources = context.getResources();
                this.mHintText = context.getString(this.mHintTextResId);
                this.mHintTextPaint.setTextSize(resources.getDimensionPixelSize(2131690022));
                this.mHintTextPaint.getTextBounds(this.mHintText, 0, this.mHintText.length(), this.mTmpRect);
                this.mHintTextBounds.set((int) this.mHintTextPaint.measureText(this.mHintText), this.mTmpRect.height());
            }
        }

        DockState(int i, int i2, int i3, int i4, int i5, RectF rectF, RectF rectF2, RectF rectF3) {
            this.dockSide = i;
            this.createMode = i2;
            this.viewState = new ViewState(i3, i4, i5, 2131493588, null);
            this.dockArea = rectF2;
            this.touchArea = rectF;
            this.expandedTouchDockArea = rectF3;
        }

        @Override // com.android.systemui.recents.views.DropTarget
        public boolean acceptsDrop(int i, int i2, int i3, int i4, boolean z) {
            return z ? areaContainsPoint(this.expandedTouchDockArea, i3, i4, i, i2) : areaContainsPoint(this.touchArea, i3, i4, i, i2);
        }

        public boolean areaContainsPoint(RectF rectF, int i, int i2, float f, float f2) {
            int i3 = (int) (rectF.left * i);
            int i4 = (int) (rectF.top * i2);
            int i5 = (int) (rectF.right * i);
            int i6 = (int) (rectF.bottom * i2);
            boolean z = false;
            if (f >= i3) {
                z = false;
                if (f2 >= i4) {
                    z = false;
                    if (f <= i5) {
                        z = false;
                        if (f2 <= i6) {
                            z = true;
                        }
                    }
                }
            }
            return z;
        }

        public Rect getDockedBounds(int i, int i2, int i3, Rect rect, Resources resources) {
            boolean z = true;
            if (resources.getConfiguration().orientation != 1) {
                z = false;
            }
            int calculateMiddlePosition = DockedDividerUtils.calculateMiddlePosition(z, rect, i, i2, i3);
            Rect rect2 = new Rect();
            DockedDividerUtils.calculateBoundsForPosition(calculateMiddlePosition, this.dockSide, rect2, i, i2, i3);
            return rect2;
        }

        public Rect getDockedTaskStackBounds(Rect rect, int i, int i2, int i3, Rect rect2, TaskStackLayoutAlgorithm taskStackLayoutAlgorithm, Resources resources, Rect rect3) {
            DockedDividerUtils.calculateBoundsForPosition(DockedDividerUtils.calculateMiddlePosition(resources.getConfiguration().orientation == 1, rect2, i, i2, i3), DockedDividerUtils.invertDockSide(this.dockSide), rect3, i, i2, i3);
            Rect rect4 = new Rect();
            taskStackLayoutAlgorithm.getTaskStackBounds(rect, rect3, this.dockArea.bottom < 1.0f ? 0 : rect2.top, rect2.right, rect4);
            return rect4;
        }

        public Rect getPreDockedBounds(int i, int i2) {
            return new Rect((int) (this.dockArea.left * i), (int) (this.dockArea.top * i2), (int) (this.dockArea.right * i), (int) (this.dockArea.bottom * i2));
        }

        public void update(Context context) {
            this.viewState.update(context);
        }
    }

    /* loaded from: a.zip:com/android/systemui/recents/model/TaskStack$TaskStackCallbacks.class */
    public interface TaskStackCallbacks {
        void onStackTaskAdded(TaskStack taskStack, Task task);

        void onStackTaskRemoved(TaskStack taskStack, Task task, Task task2, AnimationProps animationProps, boolean z);

        void onStackTasksRemoved(TaskStack taskStack);

        void onStackTasksUpdated(TaskStack taskStack);
    }

    public TaskStack() {
        this.mStackTaskList.setFilter(new TaskFilter(this) { // from class: com.android.systemui.recents.model.TaskStack.2
            final TaskStack this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.recents.model.TaskFilter
            public boolean acceptTask(SparseArray<Task> sparseArray, Task task, int i) {
                return task.isStackTask;
            }
        });
    }

    private ArrayMap<Task.TaskKey, Task> createTaskKeyMapFromList(List<Task> list) {
        ArrayMap<Task.TaskKey, Task> arrayMap = new ArrayMap<>(list.size());
        int size = list.size();
        for (int i = 0; i < size; i++) {
            Task task = list.get(i);
            arrayMap.put(task.key, task);
        }
        return arrayMap;
    }

    public void addGroup(TaskGrouping taskGrouping) {
        this.mGroups.add(taskGrouping);
        this.mAffinitiesGroups.put(Integer.valueOf(taskGrouping.affiliation), taskGrouping);
    }

    public ArrayList<Task> computeAllTasksList() {
        ArrayList<Task> arrayList = new ArrayList<>();
        arrayList.addAll(this.mStackTaskList.getTasks());
        return arrayList;
    }

    public ArraySet<ComponentName> computeComponentsRemoved(String str, int i) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        ArraySet arraySet = new ArraySet();
        ArraySet<ComponentName> arraySet2 = new ArraySet<>();
        ArrayList<Task.TaskKey> taskKeys = getTaskKeys();
        int size = taskKeys.size();
        for (int i2 = 0; i2 < size; i2++) {
            Task.TaskKey taskKey = taskKeys.get(i2);
            if (taskKey.userId == i) {
                ComponentName component = taskKey.getComponent();
                if (component.getPackageName().equals(str) && !arraySet.contains(component)) {
                    if (systemServices.getActivityInfo(component, i) != null) {
                        arraySet.add(component);
                    } else {
                        arraySet2.add(component);
                    }
                }
            }
        }
        return arraySet2;
    }

    void createAffiliatedGroupings(Context context) {
        this.mGroups.clear();
        this.mAffinitiesGroups.clear();
        ArrayMap arrayMap = new ArrayMap();
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int size = tasks.size();
        for (int i = 0; i < size; i++) {
            Task task = tasks.get(i);
            TaskGrouping taskGrouping = new TaskGrouping(task.key.id);
            addGroup(taskGrouping);
            taskGrouping.addTask(task);
            arrayMap.put(task.key, task);
        }
        float f = context.getResources().getFloat(2131690023);
        int size2 = this.mGroups.size();
        for (int i2 = 0; i2 < size2; i2++) {
            TaskGrouping taskGrouping2 = this.mGroups.get(i2);
            int taskCount = taskGrouping2.getTaskCount();
            if (taskCount > 1) {
                int i3 = ((Task) arrayMap.get(taskGrouping2.mTaskKeys.get(0))).affiliationColor;
                float f2 = (1.0f - f) / taskCount;
                float f3 = 1.0f;
                for (int i4 = 0; i4 < taskCount; i4++) {
                    ((Task) arrayMap.get(taskGrouping2.mTaskKeys.get(i4))).colorPrimary = Utilities.getColorWithOverlay(i3, -1, f3);
                    f3 -= f2;
                }
            }
        }
    }

    public void dump(String str, PrintWriter printWriter) {
        String str2 = str + "  ";
        printWriter.print(str);
        printWriter.print("TaskStack");
        printWriter.print(" numStackTasks=");
        printWriter.print(this.mStackTaskList.size());
        printWriter.println();
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int size = tasks.size();
        for (int i = 0; i < size; i++) {
            tasks.get(i).dump(str2, printWriter);
        }
    }

    public Task findTaskWithId(int i) {
        ArrayList<Task> computeAllTasksList = computeAllTasksList();
        int size = computeAllTasksList.size();
        for (int i2 = 0; i2 < size; i2++) {
            Task task = computeAllTasksList.get(i2);
            if (task.key.id == i) {
                return task;
            }
        }
        return null;
    }

    public int getFreeformTaskCount() {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int i = 0;
        int size = tasks.size();
        int i2 = 0;
        while (i2 < size) {
            int i3 = i;
            if (tasks.get(i2).isFreeformTask()) {
                i3 = i + 1;
            }
            i2++;
            i = i3;
        }
        return i;
    }

    public ArrayList<Task> getFreeformTasks() {
        ArrayList<Task> arrayList = new ArrayList<>();
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int size = tasks.size();
        for (int i = 0; i < size; i++) {
            Task task = tasks.get(i);
            if (task.isFreeformTask()) {
                arrayList.add(task);
            }
        }
        return arrayList;
    }

    public Task getLaunchTarget() {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int size = tasks.size();
        for (int i = 0; i < size; i++) {
            Task task = tasks.get(i);
            if (task.isLaunchTarget) {
                return task;
            }
        }
        return null;
    }

    public Task getStackFrontMostTask(boolean z) {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        if (tasks.isEmpty()) {
            return null;
        }
        for (int size = tasks.size() - 1; size >= 0; size--) {
            Task task = tasks.get(size);
            if (!task.isFreeformTask() || z) {
                return task;
            }
        }
        return null;
    }

    public int getStackTaskCount() {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int i = 0;
        int size = tasks.size();
        int i2 = 0;
        while (i2 < size) {
            int i3 = i;
            if (!tasks.get(i2).isFreeformTask()) {
                i3 = i + 1;
            }
            i2++;
            i = i3;
        }
        return i;
    }

    public ArrayList<Task> getStackTasks() {
        return this.mStackTaskList.getTasks();
    }

    public int getTaskCount() {
        return this.mStackTaskList.size();
    }

    public ArrayList<Task.TaskKey> getTaskKeys() {
        ArrayList<Task.TaskKey> arrayList = new ArrayList<>();
        ArrayList<Task> computeAllTasksList = computeAllTasksList();
        int size = computeAllTasksList.size();
        for (int i = 0; i < size; i++) {
            arrayList.add(computeAllTasksList.get(i).key);
        }
        return arrayList;
    }

    public int indexOfStackTask(Task task) {
        return this.mStackTaskList.indexOf(task);
    }

    public void moveTaskToStack(Task task, int i) {
        int i2;
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int size = tasks.size();
        if (!task.isFreeformTask() && i == 2) {
            this.mStackTaskList.moveTaskToStack(task, size, i);
        } else if (task.isFreeformTask() && i == 1) {
            while (true) {
                size--;
                i2 = 0;
                if (size < 0) {
                    break;
                } else if (!tasks.get(size).isFreeformTask()) {
                    i2 = size + 1;
                    break;
                }
            }
            this.mStackTaskList.moveTaskToStack(task, i2, i);
        }
    }

    public void removeAllTasks() {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        for (int size = tasks.size() - 1; size >= 0; size--) {
            Task task = tasks.get(size);
            removeTaskImpl(this.mStackTaskList, task);
            this.mRawTaskList.remove(task);
        }
        if (this.mCb != null) {
            this.mCb.onStackTasksRemoved(this);
        }
    }

    public void removeGroup(TaskGrouping taskGrouping) {
        this.mGroups.remove(taskGrouping);
        this.mAffinitiesGroups.remove(Integer.valueOf(taskGrouping.affiliation));
    }

    public void removeTask(Task task, AnimationProps animationProps, boolean z) {
        if (this.mStackTaskList.contains(task)) {
            removeTaskImpl(this.mStackTaskList, task);
            Task stackFrontMostTask = getStackFrontMostTask(false);
            if (this.mCb != null) {
                this.mCb.onStackTaskRemoved(this, task, stackFrontMostTask, animationProps, z);
            }
        }
        this.mRawTaskList.remove(task);
    }

    void removeTaskImpl(FilteredTaskList filteredTaskList, Task task) {
        filteredTaskList.remove(task);
        TaskGrouping taskGrouping = task.group;
        if (taskGrouping != null) {
            taskGrouping.removeTask(task);
            if (taskGrouping.getTaskCount() == 0) {
                removeGroup(taskGrouping);
            }
        }
    }

    public void setCallbacks(TaskStackCallbacks taskStackCallbacks) {
        this.mCb = taskStackCallbacks;
    }

    public void setTasks(Context context, List<Task> list, boolean z) {
        Task task;
        ArrayMap<Task.TaskKey, Task> createTaskKeyMapFromList = createTaskKeyMapFromList(this.mRawTaskList);
        ArrayMap<Task.TaskKey, Task> createTaskKeyMapFromList2 = createTaskKeyMapFromList(list);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        ArrayList<Task> arrayList3 = new ArrayList<>();
        if (this.mCb == null) {
            z = false;
        }
        for (int size = this.mRawTaskList.size() - 1; size >= 0; size--) {
            Task task2 = this.mRawTaskList.get(size);
            if (!createTaskKeyMapFromList2.containsKey(task2.key) && z) {
                arrayList2.add(task2);
            }
            task2.setGroup(null);
        }
        int size2 = list.size();
        for (int i = 0; i < size2; i++) {
            Task task3 = list.get(i);
            Task task4 = createTaskKeyMapFromList.get(task3.key);
            if (task4 == null && z) {
                arrayList.add(task3);
                task = task3;
            } else {
                task = task3;
                if (task4 != null) {
                    task4.copyFrom(task3);
                    task = task4;
                }
            }
            arrayList3.add(task);
        }
        for (int size3 = arrayList3.size() - 1; size3 >= 0; size3--) {
            arrayList3.get(size3).temporarySortIndexInStack = size3;
        }
        Collections.sort(arrayList3, this.FREEFORM_COMPARATOR);
        this.mStackTaskList.set(arrayList3);
        this.mRawTaskList = arrayList3;
        createAffiliatedGroupings(context);
        int size4 = arrayList2.size();
        Task stackFrontMostTask = getStackFrontMostTask(false);
        for (int i2 = 0; i2 < size4; i2++) {
            this.mCb.onStackTaskRemoved(this, (Task) arrayList2.get(i2), stackFrontMostTask, AnimationProps.IMMEDIATE, false);
        }
        int size5 = arrayList.size();
        for (int i3 = 0; i3 < size5; i3++) {
            this.mCb.onStackTaskAdded(this, (Task) arrayList.get(i3));
        }
        if (z) {
            this.mCb.onStackTasksUpdated(this);
        }
    }

    public String toString() {
        ArrayList<Task> tasks;
        String str = "Stack Tasks (" + this.mStackTaskList.size() + "):\n";
        for (int i = 0; i < this.mStackTaskList.getTasks().size(); i++) {
            str = str + "    " + tasks.get(i).toString() + "\n";
        }
        return str;
    }
}
