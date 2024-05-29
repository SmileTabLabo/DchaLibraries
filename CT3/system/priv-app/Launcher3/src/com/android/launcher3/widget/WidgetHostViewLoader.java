package com.android.launcher3.widget;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import com.android.launcher3.AppWidgetResizeFrame;
import com.android.launcher3.DragController;
import com.android.launcher3.DragLayer;
import com.android.launcher3.DragSource;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.AppWidgetManagerCompat;
/* loaded from: a.zip:com/android/launcher3/widget/WidgetHostViewLoader.class */
public class WidgetHostViewLoader implements DragController.DragListener {
    final PendingAddWidgetInfo mInfo;
    Launcher mLauncher;
    final View mView;
    Runnable mInflateWidgetRunnable = null;
    private Runnable mBindWidgetRunnable = null;
    int mWidgetLoadingId = -1;
    Handler mHandler = new Handler();

    public WidgetHostViewLoader(Launcher launcher, View view) {
        this.mLauncher = launcher;
        this.mView = view;
        this.mInfo = (PendingAddWidgetInfo) view.getTag();
    }

    public static Bundle getDefaultOptionsForWidget(Launcher launcher, PendingAddWidgetInfo pendingAddWidgetInfo) {
        Bundle bundle = null;
        Rect rect = new Rect();
        if (Utilities.ATLEAST_JB_MR1) {
            AppWidgetResizeFrame.getWidgetSizeRanges(launcher, pendingAddWidgetInfo.spanX, pendingAddWidgetInfo.spanY, rect);
            Rect defaultPaddingForWidget = AppWidgetHostView.getDefaultPaddingForWidget(launcher, pendingAddWidgetInfo.componentName, null);
            float f = launcher.getResources().getDisplayMetrics().density;
            int i = (int) ((defaultPaddingForWidget.left + defaultPaddingForWidget.right) / f);
            int i2 = (int) ((defaultPaddingForWidget.top + defaultPaddingForWidget.bottom) / f);
            bundle = new Bundle();
            bundle.putInt("appWidgetMinWidth", rect.left - i);
            bundle.putInt("appWidgetMinHeight", rect.top - i2);
            bundle.putInt("appWidgetMaxWidth", rect.right - i);
            bundle.putInt("appWidgetMaxHeight", rect.bottom - i2);
        }
        return bundle;
    }

    @Override // com.android.launcher3.DragController.DragListener
    public void onDragEnd() {
        if (this.mLauncher.getDragController() != null) {
            this.mLauncher.getDragController().removeDragListener(this);
        }
        this.mHandler.removeCallbacks(this.mBindWidgetRunnable);
        this.mHandler.removeCallbacks(this.mInflateWidgetRunnable);
        if (this.mWidgetLoadingId != -1) {
            this.mLauncher.getAppWidgetHost().deleteAppWidgetId(this.mWidgetLoadingId);
            this.mWidgetLoadingId = -1;
        }
        if (this.mInfo.boundWidget != null) {
            this.mLauncher.getDragLayer().removeView(this.mInfo.boundWidget);
            this.mLauncher.getAppWidgetHost().deleteAppWidgetId(this.mInfo.boundWidget.getAppWidgetId());
            this.mInfo.boundWidget = null;
        }
    }

    @Override // com.android.launcher3.DragController.DragListener
    public void onDragStart(DragSource dragSource, Object obj, int i) {
    }

    public boolean preloadWidget() {
        LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo = this.mInfo.info;
        if (launcherAppWidgetProviderInfo.isCustomWidget) {
            return false;
        }
        Bundle defaultOptionsForWidget = getDefaultOptionsForWidget(this.mLauncher, this.mInfo);
        if (launcherAppWidgetProviderInfo.configure != null) {
            this.mInfo.bindOptions = defaultOptionsForWidget;
            return false;
        }
        this.mBindWidgetRunnable = new Runnable(this, launcherAppWidgetProviderInfo, defaultOptionsForWidget) { // from class: com.android.launcher3.widget.WidgetHostViewLoader.1
            final WidgetHostViewLoader this$0;
            final Bundle val$options;
            final LauncherAppWidgetProviderInfo val$pInfo;

            {
                this.this$0 = this;
                this.val$pInfo = launcherAppWidgetProviderInfo;
                this.val$options = defaultOptionsForWidget;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mWidgetLoadingId = this.this$0.mLauncher.getAppWidgetHost().allocateAppWidgetId();
                if (AppWidgetManagerCompat.getInstance(this.this$0.mLauncher).bindAppWidgetIdIfAllowed(this.this$0.mWidgetLoadingId, this.val$pInfo, this.val$options)) {
                    this.this$0.mHandler.post(this.this$0.mInflateWidgetRunnable);
                }
            }
        };
        this.mInflateWidgetRunnable = new Runnable(this, launcherAppWidgetProviderInfo) { // from class: com.android.launcher3.widget.WidgetHostViewLoader.2
            final WidgetHostViewLoader this$0;
            final LauncherAppWidgetProviderInfo val$pInfo;

            {
                this.this$0 = this;
                this.val$pInfo = launcherAppWidgetProviderInfo;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.mWidgetLoadingId == -1) {
                    return;
                }
                AppWidgetHostView createView = this.this$0.mLauncher.getAppWidgetHost().createView((Context) this.this$0.mLauncher, this.this$0.mWidgetLoadingId, this.val$pInfo);
                this.this$0.mInfo.boundWidget = createView;
                this.this$0.mWidgetLoadingId = -1;
                createView.setVisibility(4);
                int[] estimateItemSize = this.this$0.mLauncher.getWorkspace().estimateItemSize(this.this$0.mInfo, false);
                DragLayer.LayoutParams layoutParams = new DragLayer.LayoutParams(estimateItemSize[0], estimateItemSize[1]);
                layoutParams.y = 0;
                layoutParams.x = 0;
                layoutParams.customPosition = true;
                createView.setLayoutParams(layoutParams);
                this.this$0.mLauncher.getDragLayer().addView(createView);
                this.this$0.mView.setTag(this.this$0.mInfo);
            }
        };
        this.mHandler.post(this.mBindWidgetRunnable);
        return true;
    }
}
