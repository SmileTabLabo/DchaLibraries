package com.android.systemui.statusbar.stack;

import android.view.View;
/* loaded from: a.zip:com/android/systemui/statusbar/stack/ScrollContainer.class */
public interface ScrollContainer {
    void lockScrollTo(View view);

    void requestDisallowDismiss();

    void requestDisallowLongPress();
}
