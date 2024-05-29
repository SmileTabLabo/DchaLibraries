package com.android.launcher3;

import android.view.View;
import com.android.launcher3.DropTarget;
/* loaded from: a.zip:com/android/launcher3/DragSource.class */
public interface DragSource {
    float getIntrinsicIconScaleFactor();

    void onDropCompleted(View view, DropTarget.DragObject dragObject, boolean z, boolean z2);

    void onFlingToDeleteCompleted();

    boolean supportsAppInfoDropTarget();

    boolean supportsDeleteDropTarget();

    boolean supportsFlingToDelete();
}
