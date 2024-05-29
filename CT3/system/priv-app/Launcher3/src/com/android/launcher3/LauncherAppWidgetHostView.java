package com.android.launcher3;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import com.android.launcher3.DragLayer;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.mediatek.launcher3.LauncherLog;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/launcher3/LauncherAppWidgetHostView.class */
public class LauncherAppWidgetHostView extends AppWidgetHostView implements DragLayer.TouchCompleteListener {
    private boolean mChildrenFocused;
    private Context mContext;
    private DragLayer mDragLayer;
    LayoutInflater mInflater;
    private CheckLongPressHelper mLongPressHelper;
    private int mPreviousOrientation;
    private float mSlop;
    private StylusEventHelper mStylusEventHelper;

    public LauncherAppWidgetHostView(Context context) {
        super(context);
        this.mContext = context;
        this.mLongPressHelper = new CheckLongPressHelper(this);
        this.mStylusEventHelper = new StylusEventHelper(this);
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mDragLayer = ((Launcher) context).getDragLayer();
        setAccessibilityDelegate(LauncherAppState.getInstance().getAccessibilityDelegate());
        setBackgroundResource(2130837565);
    }

    private void dispatchChildFocus(boolean z) {
        setSelected(z);
    }

    @Override // android.view.View
    public void cancelLongPress() {
        super.cancelLongPress();
        this.mLongPressHelper.cancelLongPress();
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void clearChildFocus(View view) {
        super.clearChildFocus(view);
        dispatchChildFocus(false);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (this.mChildrenFocused && keyEvent.getKeyCode() == 111 && keyEvent.getAction() == 1) {
            this.mChildrenFocused = false;
            requestFocus();
            return true;
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchUnhandledMove(View view, int i) {
        return this.mChildrenFocused;
    }

    @Override // android.appwidget.AppWidgetHostView
    public AppWidgetProviderInfo getAppWidgetInfo() {
        AppWidgetProviderInfo appWidgetInfo = super.getAppWidgetInfo();
        if (appWidgetInfo == null || (appWidgetInfo instanceof LauncherAppWidgetProviderInfo)) {
            return appWidgetInfo;
        }
        throw new IllegalStateException("Launcher widget must have LauncherAppWidgetProviderInfo");
    }

    @Override // android.view.ViewGroup
    public int getDescendantFocusability() {
        return this.mChildrenFocused ? 131072 : 393216;
    }

    @Override // android.appwidget.AppWidgetHostView
    protected View getErrorView() {
        return this.mInflater.inflate(2130968587, (ViewGroup) this, false);
    }

    public boolean isReinflateRequired() {
        return this.mPreviousOrientation != this.mContext.getResources().getConfiguration().orientation;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override // android.view.View
    protected void onFocusChanged(boolean z, int i, Rect rect) {
        if (z) {
            this.mChildrenFocused = false;
            dispatchChildFocus(false);
        }
        super.onFocusChanged(z, i, rect);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d("LauncherAppWidgetHostView", "onInterceptTouchEvent: ev = " + motionEvent);
        }
        if (motionEvent.getAction() == 0) {
            this.mLongPressHelper.cancelLongPress();
        }
        if (this.mLongPressHelper.hasPerformedLongPress()) {
            this.mLongPressHelper.cancelLongPress();
            return true;
        } else if (this.mStylusEventHelper.checkAndPerformStylusEvent(motionEvent)) {
            this.mLongPressHelper.cancelLongPress();
            return true;
        } else {
            switch (motionEvent.getAction()) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    if (!this.mStylusEventHelper.inStylusButtonPressed()) {
                        this.mLongPressHelper.postCheckForLongPress();
                    }
                    this.mDragLayer.setTouchCompleteListener(this);
                    return false;
                case 1:
                case 3:
                    this.mLongPressHelper.cancelLongPress();
                    return false;
                case 2:
                    if (Utilities.pointInView(this, motionEvent.getX(), motionEvent.getY(), this.mSlop)) {
                        return false;
                    }
                    this.mLongPressHelper.cancelLongPress();
                    return false;
                default:
                    return false;
            }
        }
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (this.mChildrenFocused || i != 66) {
            return super.onKeyDown(i, keyEvent);
        }
        keyEvent.startTracking();
        return true;
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (keyEvent.isTracking() && !this.mChildrenFocused && i == 66) {
            this.mChildrenFocused = true;
            ArrayList focusables = getFocusables(2);
            focusables.remove(this);
            switch (focusables.size()) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    this.mChildrenFocused = false;
                    break;
                case 1:
                    if (getTag() instanceof ItemInfo) {
                        ItemInfo itemInfo = (ItemInfo) getTag();
                        if (itemInfo.spanX == 1 && itemInfo.spanY == 1) {
                            ((View) focusables.get(0)).performClick();
                            this.mChildrenFocused = false;
                            return true;
                        }
                    }
                    break;
            }
            ((View) focusables.get(0)).requestFocus();
            return true;
        }
        return super.onKeyUp(i, keyEvent);
    }

    @Override // android.appwidget.AppWidgetHostView, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        try {
            super.onLayout(z, i, i2, i3, i4);
        } catch (RuntimeException e) {
            post(new Runnable(this) { // from class: com.android.launcher3.LauncherAppWidgetHostView.1
                final LauncherAppWidgetHostView this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.updateAppWidget(new RemoteViews(this.this$0.getAppWidgetInfo().provider.getPackageName(), 0));
                }
            });
        }
    }

    @Override // com.android.launcher3.DragLayer.TouchCompleteListener
    public void onTouchComplete() {
        if (this.mLongPressHelper.hasPerformedLongPress()) {
            return;
        }
        this.mLongPressHelper.cancelLongPress();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d("LauncherAppWidgetHostView", "onTouchEvent: ev = " + motionEvent);
        }
        switch (motionEvent.getAction()) {
            case 1:
            case 3:
                this.mLongPressHelper.cancelLongPress();
                return false;
            case 2:
                if (Utilities.pointInView(this, motionEvent.getX(), motionEvent.getY(), this.mSlop)) {
                    return false;
                }
                this.mLongPressHelper.cancelLongPress();
                return false;
            default:
                return false;
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestChildFocus(View view, View view2) {
        super.requestChildFocus(view, view2);
        dispatchChildFocus(this.mChildrenFocused && view2 != null);
        if (view2 != null) {
            view2.setFocusableInTouchMode(false);
        }
    }

    @Override // android.appwidget.AppWidgetHostView
    public void updateAppWidget(RemoteViews remoteViews) {
        updateLastInflationOrientation();
        super.updateAppWidget(remoteViews);
    }

    public void updateLastInflationOrientation() {
        this.mPreviousOrientation = this.mContext.getResources().getConfiguration().orientation;
    }
}
