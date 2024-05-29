package com.android.launcher3.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.WidgetPreviewLoader;
import com.android.launcher3.model.WidgetsModel;
import java.util.List;
/* loaded from: a.zip:com/android/launcher3/widget/WidgetsListAdapter.class */
public class WidgetsListAdapter extends RecyclerView.Adapter<WidgetsRowViewHolder> {
    private View.OnClickListener mIconClickListener;
    private View.OnLongClickListener mIconLongClickListener;
    private int mIndent = 0;
    private Launcher mLauncher;
    private LayoutInflater mLayoutInflater;
    private WidgetPreviewLoader mWidgetPreviewLoader;
    private WidgetsModel mWidgetsModel;

    public WidgetsListAdapter(Context context, View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener, Launcher launcher) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mIconClickListener = onClickListener;
        this.mIconLongClickListener = onLongClickListener;
        this.mLauncher = launcher;
        setContainerHeight();
    }

    private WidgetPreviewLoader getWidgetPreviewLoader() {
        if (this.mWidgetPreviewLoader == null) {
            this.mWidgetPreviewLoader = LauncherAppState.getInstance().getWidgetCache();
        }
        return this.mWidgetPreviewLoader;
    }

    private void setContainerHeight() {
        Resources resources = this.mLauncher.getResources();
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        if (deviceProfile.isLargeTablet || deviceProfile.isTablet) {
            this.mIndent = Utilities.pxFromDp(56.0f, resources.getDisplayMetrics());
        }
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public int getItemCount() {
        if (this.mWidgetsModel == null) {
            return 0;
        }
        return this.mWidgetsModel.getPackageSize();
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public long getItemId(int i) {
        return i;
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public void onBindViewHolder(WidgetsRowViewHolder widgetsRowViewHolder, int i) {
        List<Object> sortedWidgets = this.mWidgetsModel.getSortedWidgets(i);
        ViewGroup viewGroup = (ViewGroup) widgetsRowViewHolder.getContent().findViewById(2131296333);
        int size = sortedWidgets.size() - viewGroup.getChildCount();
        if (size > 0) {
            for (int i2 = 0; i2 < size; i2++) {
                WidgetCell widgetCell = (WidgetCell) this.mLayoutInflater.inflate(2130968609, viewGroup, false);
                widgetCell.setOnClickListener(this.mIconClickListener);
                widgetCell.setOnLongClickListener(this.mIconLongClickListener);
                ViewGroup.LayoutParams layoutParams = widgetCell.getLayoutParams();
                layoutParams.height = widgetCell.cellSize;
                layoutParams.width = widgetCell.cellSize;
                widgetCell.setLayoutParams(layoutParams);
                viewGroup.addView(widgetCell);
            }
        } else if (size < 0) {
            for (int size2 = sortedWidgets.size(); size2 < viewGroup.getChildCount(); size2++) {
                viewGroup.getChildAt(size2).setVisibility(8);
            }
        }
        ((BubbleTextView) widgetsRowViewHolder.getContent().findViewById(2131296331)).applyFromPackageItemInfo(this.mWidgetsModel.getPackageItemInfo(i));
        if (getWidgetPreviewLoader() == null) {
            return;
        }
        for (int i3 = 0; i3 < sortedWidgets.size(); i3++) {
            WidgetCell widgetCell2 = (WidgetCell) viewGroup.getChildAt(i3);
            if (sortedWidgets.get(i3) instanceof LauncherAppWidgetProviderInfo) {
                LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo = (LauncherAppWidgetProviderInfo) sortedWidgets.get(i3);
                widgetCell2.setTag(new PendingAddWidgetInfo(this.mLauncher, launcherAppWidgetProviderInfo, null));
                widgetCell2.applyFromAppWidgetProviderInfo(launcherAppWidgetProviderInfo, this.mWidgetPreviewLoader);
            } else if (sortedWidgets.get(i3) instanceof ResolveInfo) {
                ResolveInfo resolveInfo = (ResolveInfo) sortedWidgets.get(i3);
                widgetCell2.setTag(new PendingAddShortcutInfo(resolveInfo.activityInfo));
                widgetCell2.applyFromResolveInfo(this.mLauncher.getPackageManager(), resolveInfo, this.mWidgetPreviewLoader);
            }
            widgetCell2.ensurePreview();
            widgetCell2.setVisibility(0);
        }
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    @TargetApi(17)
    public WidgetsRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        ViewGroup viewGroup2 = (ViewGroup) this.mLayoutInflater.inflate(2130968610, viewGroup, false);
        LinearLayout linearLayout = (LinearLayout) viewGroup2.findViewById(2131296333);
        if (Utilities.ATLEAST_JB_MR1) {
            linearLayout.setPaddingRelative(this.mIndent, 0, 1, 0);
        } else {
            linearLayout.setPadding(this.mIndent, 0, 1, 0);
        }
        return new WidgetsRowViewHolder(viewGroup2);
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public boolean onFailedToRecycleView(WidgetsRowViewHolder widgetsRowViewHolder) {
        return true;
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public void onViewRecycled(WidgetsRowViewHolder widgetsRowViewHolder) {
        ViewGroup viewGroup = (ViewGroup) widgetsRowViewHolder.getContent().findViewById(2131296333);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            ((WidgetCell) viewGroup.getChildAt(i)).clear();
        }
    }

    public void setWidgetsModel(WidgetsModel widgetsModel) {
        this.mWidgetsModel = widgetsModel;
    }
}
