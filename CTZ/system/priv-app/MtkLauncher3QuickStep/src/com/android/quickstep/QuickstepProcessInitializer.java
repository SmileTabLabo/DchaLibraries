package com.android.quickstep;

import android.content.Context;
import com.android.launcher3.MainProcessInitializer;
import com.android.systemui.shared.system.ThreadedRendererCompat;
/* loaded from: classes.dex */
public class QuickstepProcessInitializer extends MainProcessInitializer {
    public QuickstepProcessInitializer(Context context) {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.MainProcessInitializer
    public void init(Context context) {
        super.init(context);
        ThreadedRendererCompat.setContextPriority(ThreadedRendererCompat.EGL_CONTEXT_PRIORITY_HIGH_IMG);
    }
}
