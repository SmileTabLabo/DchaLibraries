package com.android.systemui.stackdivider;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManagerGlobal;
import com.android.internal.annotations.GuardedBy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/* loaded from: a.zip:com/android/systemui/stackdivider/WindowManagerProxy.class */
public class WindowManagerProxy {
    private static final WindowManagerProxy sInstance = new WindowManagerProxy();
    private float mDimLayerAlpha;
    private int mDimLayerTargetStack;
    private boolean mDimLayerVisible;
    @GuardedBy("mDockedRect")
    private final Rect mDockedRect = new Rect();
    private final Rect mTempDockedTaskRect = new Rect();
    private final Rect mTempDockedInsetRect = new Rect();
    private final Rect mTempOtherTaskRect = new Rect();
    private final Rect mTempOtherInsetRect = new Rect();
    private final Rect mTmpRect1 = new Rect();
    private final Rect mTmpRect2 = new Rect();
    private final Rect mTmpRect3 = new Rect();
    private final Rect mTmpRect4 = new Rect();
    private final Rect mTmpRect5 = new Rect();
    @GuardedBy("mDockedRect")
    private final Rect mTouchableRegion = new Rect();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Runnable mResizeRunnable = new Runnable(this) { // from class: com.android.systemui.stackdivider.WindowManagerProxy.1
        final WindowManagerProxy this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            Rect rect = null;
            synchronized (this.this$0.mDockedRect) {
                this.this$0.mTmpRect1.set(this.this$0.mDockedRect);
                this.this$0.mTmpRect2.set(this.this$0.mTempDockedTaskRect);
                this.this$0.mTmpRect3.set(this.this$0.mTempDockedInsetRect);
                this.this$0.mTmpRect4.set(this.this$0.mTempOtherTaskRect);
                this.this$0.mTmpRect5.set(this.this$0.mTempOtherInsetRect);
            }
            try {
                IActivityManager iActivityManager = ActivityManagerNative.getDefault();
                Rect rect2 = this.this$0.mTmpRect1;
                Rect rect3 = this.this$0.mTmpRect2.isEmpty() ? null : this.this$0.mTmpRect2;
                Rect rect4 = this.this$0.mTmpRect3.isEmpty() ? null : this.this$0.mTmpRect3;
                Rect rect5 = this.this$0.mTmpRect4.isEmpty() ? null : this.this$0.mTmpRect4;
                if (!this.this$0.mTmpRect5.isEmpty()) {
                    rect = this.this$0.mTmpRect5;
                }
                iActivityManager.resizeDockedStack(rect2, rect3, rect4, rect5, rect);
            } catch (RemoteException e) {
                Log.w("WindowManagerProxy", "Failed to resize stack: " + e);
            }
        }
    };
    private final Runnable mDismissRunnable = new Runnable(this) { // from class: com.android.systemui.stackdivider.WindowManagerProxy.2
        final WindowManagerProxy this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                ActivityManagerNative.getDefault().moveTasksToFullscreenStack(3, false);
            } catch (RemoteException e) {
                Log.w("WindowManagerProxy", "Failed to remove stack: " + e);
            }
        }
    };
    private final Runnable mMaximizeRunnable = new Runnable(this) { // from class: com.android.systemui.stackdivider.WindowManagerProxy.3
        final WindowManagerProxy this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                ActivityManagerNative.getDefault().resizeStack(3, (Rect) null, true, true, false, -1);
            } catch (RemoteException e) {
                Log.w("WindowManagerProxy", "Failed to resize stack: " + e);
            }
        }
    };
    private final Runnable mDimLayerRunnable = new Runnable(this) { // from class: com.android.systemui.stackdivider.WindowManagerProxy.4
        final WindowManagerProxy this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                WindowManagerGlobal.getWindowManagerService().setResizeDimLayer(this.this$0.mDimLayerVisible, this.this$0.mDimLayerTargetStack, this.this$0.mDimLayerAlpha);
            } catch (RemoteException e) {
                Log.w("WindowManagerProxy", "Failed to resize stack: " + e);
            }
        }
    };
    private final Runnable mSwapRunnable = new Runnable(this) { // from class: com.android.systemui.stackdivider.WindowManagerProxy.5
        final WindowManagerProxy this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                ActivityManagerNative.getDefault().swapDockedAndFullscreenStack();
            } catch (RemoteException e) {
                Log.w("WindowManagerProxy", "Failed to resize stack: " + e);
            }
        }
    };
    private final Runnable mSetTouchableRegionRunnable = new Runnable(this) { // from class: com.android.systemui.stackdivider.WindowManagerProxy.6
        final WindowManagerProxy this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                synchronized (this.this$0.mDockedRect) {
                    this.this$0.mTmpRect1.set(this.this$0.mTouchableRegion);
                }
                WindowManagerGlobal.getWindowManagerService().setDockedStackDividerTouchRegion(this.this$0.mTmpRect1);
            } catch (RemoteException e) {
                Log.w("WindowManagerProxy", "Failed to set touchable region: " + e);
            }
        }
    };

    private WindowManagerProxy() {
    }

    public static WindowManagerProxy getInstance() {
        return sInstance;
    }

    public void dismissDockedStack() {
        this.mExecutor.execute(this.mDismissRunnable);
    }

    public int getDockSide() {
        try {
            return WindowManagerGlobal.getWindowManagerService().getDockedStackSide();
        } catch (RemoteException e) {
            Log.w("WindowManagerProxy", "Failed to get dock side: " + e);
            return -1;
        }
    }

    public void maximizeDockedStack() {
        this.mExecutor.execute(this.mMaximizeRunnable);
    }

    public void resizeDockedStack(Rect rect, Rect rect2, Rect rect3, Rect rect4, Rect rect5) {
        synchronized (this.mDockedRect) {
            this.mDockedRect.set(rect);
            if (rect2 != null) {
                this.mTempDockedTaskRect.set(rect2);
            } else {
                this.mTempDockedTaskRect.setEmpty();
            }
            if (rect3 != null) {
                this.mTempDockedInsetRect.set(rect3);
            } else {
                this.mTempDockedInsetRect.setEmpty();
            }
            if (rect4 != null) {
                this.mTempOtherTaskRect.set(rect4);
            } else {
                this.mTempOtherTaskRect.setEmpty();
            }
            if (rect5 != null) {
                this.mTempOtherInsetRect.set(rect5);
            } else {
                this.mTempOtherInsetRect.setEmpty();
            }
        }
        this.mExecutor.execute(this.mResizeRunnable);
    }

    public void setResizeDimLayer(boolean z, int i, float f) {
        this.mDimLayerVisible = z;
        this.mDimLayerTargetStack = i;
        this.mDimLayerAlpha = f;
        this.mExecutor.execute(this.mDimLayerRunnable);
    }

    public void setResizing(boolean z) {
        this.mExecutor.execute(new Runnable(this, z) { // from class: com.android.systemui.stackdivider.WindowManagerProxy.7
            final WindowManagerProxy this$0;
            final boolean val$resizing;

            {
                this.this$0 = this;
                this.val$resizing = z;
            }

            @Override // java.lang.Runnable
            public void run() {
                try {
                    WindowManagerGlobal.getWindowManagerService().setDockedStackResizing(this.val$resizing);
                } catch (RemoteException e) {
                    Log.w("WindowManagerProxy", "Error calling setDockedStackResizing: " + e);
                }
            }
        });
    }

    public void setTouchRegion(Rect rect) {
        synchronized (this.mDockedRect) {
            this.mTouchableRegion.set(rect);
        }
        this.mExecutor.execute(this.mSetTouchableRegionRunnable);
    }
}
