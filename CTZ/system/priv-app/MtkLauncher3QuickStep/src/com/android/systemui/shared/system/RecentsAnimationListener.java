package com.android.systemui.shared.system;

import android.graphics.Rect;
/* loaded from: classes.dex */
public interface RecentsAnimationListener {
    void onAnimationCanceled();

    void onAnimationStart(RecentsAnimationControllerCompat recentsAnimationControllerCompat, RemoteAnimationTargetCompat[] remoteAnimationTargetCompatArr, Rect rect, Rect rect2);
}
