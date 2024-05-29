package com.android.launcher3;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.launcher3.CellLayout;
import com.android.launcher3.Stats;
/* loaded from: a.zip:com/android/launcher3/Hotseat.class */
public class Hotseat extends FrameLayout implements Stats.LaunchSourceProvider {
    private int mAllAppsButtonRank;
    private CellLayout mContent;
    private final boolean mHasVerticalHotseat;
    private Launcher mLauncher;

    public Hotseat(Context context) {
        this(context, null);
    }

    public Hotseat(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public Hotseat(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mLauncher = (Launcher) context;
        this.mHasVerticalHotseat = this.mLauncher.getDeviceProfile().isVerticalBarLayout();
    }

    @Override // com.android.launcher3.Stats.LaunchSourceProvider
    public void fillInLaunchSourceData(View view, Bundle bundle) {
        bundle.putString("container", "hotseat");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getCellXFromOrder(int i) {
        if (this.mHasVerticalHotseat) {
            i = 0;
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getCellYFromOrder(int i) {
        return this.mHasVerticalHotseat ? this.mContent.getCountY() - (i + 1) : 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public CellLayout getLayout() {
        return this.mContent;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getOrderInHotseat(int i, int i2) {
        if (this.mHasVerticalHotseat) {
            i = (this.mContent.getCountY() - i2) - 1;
        }
        return i;
    }

    public boolean isAllAppsButtonRank(int i) {
        return i == this.mAllAppsButtonRank;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        this.mAllAppsButtonRank = deviceProfile.inv.hotseatAllAppsRank;
        this.mContent = (CellLayout) findViewById(2131296286);
        if (!deviceProfile.isLandscape || deviceProfile.isLargeTablet) {
            this.mContent.setGridSize(deviceProfile.inv.numHotseatIcons, 1);
        } else {
            this.mContent.setGridSize(1, deviceProfile.inv.numHotseatIcons);
        }
        this.mContent.setIsHotseat(true);
        resetLayout();
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return this.mLauncher.getWorkspace().workspaceInModalState();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void resetLayout() {
        this.mContent.removeAllViewsInLayout();
        Context context = getContext();
        TextView textView = (TextView) LayoutInflater.from(context).inflate(2130968578, (ViewGroup) this.mContent, false);
        Drawable drawable = context.getResources().getDrawable(2130837504);
        this.mLauncher.resizeIconDrawable(drawable);
        int dimensionPixelSize = getResources().getDimensionPixelSize(2131230763);
        Rect bounds = drawable.getBounds();
        drawable.setBounds(bounds.left, bounds.top + (dimensionPixelSize / 2), bounds.right - dimensionPixelSize, bounds.bottom - (dimensionPixelSize / 2));
        textView.setCompoundDrawables(null, drawable, null, null);
        textView.setContentDescription(context.getString(2131558420));
        textView.setOnKeyListener(new HotseatIconKeyEventListener());
        if (this.mLauncher != null) {
            this.mLauncher.setAllAppsButton(textView);
            textView.setOnTouchListener(this.mLauncher.getHapticFeedbackTouchListener());
            textView.setOnClickListener(this.mLauncher);
            textView.setOnLongClickListener(this.mLauncher);
            textView.setOnFocusChangeListener(this.mLauncher.mFocusHandler);
        }
        CellLayout.LayoutParams layoutParams = new CellLayout.LayoutParams(getCellXFromOrder(this.mAllAppsButtonRank), getCellYFromOrder(this.mAllAppsButtonRank), 1, 1);
        layoutParams.canReorder = false;
        this.mContent.addViewToCellLayout(textView, -1, textView.getId(), layoutParams, true);
    }

    @Override // android.view.View
    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.mContent.setOnLongClickListener(onLongClickListener);
    }
}
