package com.android.launcher3;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.os.DeadObjectException;
import android.os.TransactionTooLargeException;
import android.view.LayoutInflater;
import android.view.View;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/launcher3/LauncherAppWidgetHost.class */
public class LauncherAppWidgetHost extends AppWidgetHost {
    private Launcher mLauncher;
    private final ArrayList<Runnable> mProviderChangeListeners;
    private int mQsbWidgetId;

    public LauncherAppWidgetHost(Launcher launcher, int i) {
        super(launcher, i);
        this.mProviderChangeListeners = new ArrayList<>();
        this.mQsbWidgetId = -1;
        this.mLauncher = launcher;
    }

    public void addProviderChangeListener(Runnable runnable) {
        this.mProviderChangeListeners.add(runnable);
    }

    public AppWidgetHostView createView(Context context, int i, LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo) {
        if (launcherAppWidgetProviderInfo.isCustomWidget) {
            LauncherAppWidgetHostView launcherAppWidgetHostView = new LauncherAppWidgetHostView(context);
            ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(launcherAppWidgetProviderInfo.initialLayout, launcherAppWidgetHostView);
            launcherAppWidgetHostView.setAppWidget(0, launcherAppWidgetProviderInfo);
            launcherAppWidgetHostView.updateLastInflationOrientation();
            return launcherAppWidgetHostView;
        }
        return super.createView(context, i, (AppWidgetProviderInfo) launcherAppWidgetProviderInfo);
    }

    @Override // android.appwidget.AppWidgetHost
    protected AppWidgetHostView onCreateView(Context context, int i, AppWidgetProviderInfo appWidgetProviderInfo) {
        return i == this.mQsbWidgetId ? new LauncherAppWidgetHostView(this, context) { // from class: com.android.launcher3.LauncherAppWidgetHost.1
            final LauncherAppWidgetHost this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.launcher3.LauncherAppWidgetHostView, android.appwidget.AppWidgetHostView
            protected View getErrorView() {
                return new View(getContext());
            }
        } : new LauncherAppWidgetHostView(context);
    }

    @Override // android.appwidget.AppWidgetHost
    protected void onProviderChanged(int i, AppWidgetProviderInfo appWidgetProviderInfo) {
        LauncherAppWidgetProviderInfo fromProviderInfo = LauncherAppWidgetProviderInfo.fromProviderInfo(this.mLauncher, appWidgetProviderInfo);
        super.onProviderChanged(i, fromProviderInfo);
        fromProviderInfo.initSpans();
    }

    @Override // android.appwidget.AppWidgetHost
    protected void onProvidersChanged() {
        if (!this.mProviderChangeListeners.isEmpty()) {
            for (Runnable runnable : new ArrayList(this.mProviderChangeListeners)) {
                runnable.run();
            }
        }
        if (Utilities.ATLEAST_MARSHMALLOW) {
            this.mLauncher.notifyWidgetProvidersChanged();
        }
    }

    public void removeProviderChangeListener(Runnable runnable) {
        this.mProviderChangeListeners.remove(runnable);
    }

    public void setQsbWidgetId(int i) {
        this.mQsbWidgetId = i;
    }

    @Override // android.appwidget.AppWidgetHost
    public void startListening() {
        try {
            super.startListening();
        } catch (Exception e) {
            if (!(e.getCause() instanceof TransactionTooLargeException) && !(e.getCause() instanceof DeadObjectException)) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override // android.appwidget.AppWidgetHost
    public void stopListening() {
        super.stopListening();
        clearViews();
    }
}
