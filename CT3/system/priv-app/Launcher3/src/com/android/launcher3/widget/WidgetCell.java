package com.android.launcher3.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.StylusEventHelper;
import com.android.launcher3.WidgetPreviewLoader;
import com.android.launcher3.compat.AppWidgetManagerCompat;
/* loaded from: a.zip:com/android/launcher3/widget/WidgetCell.class */
public class WidgetCell extends LinearLayout implements View.OnLayoutChangeListener {
    int cellSize;
    private WidgetPreviewLoader.PreviewLoadRequest mActiveRequest;
    private String mDimensionsFormatString;
    private Object mInfo;
    private Launcher mLauncher;
    private int mPresetPreviewSize;
    private StylusEventHelper mStylusEventHelper;
    private TextView mWidgetDims;
    private WidgetImageView mWidgetImage;
    private TextView mWidgetName;
    private WidgetPreviewLoader mWidgetPreviewLoader;

    public WidgetCell(Context context) {
        this(context, null);
    }

    public WidgetCell(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public WidgetCell(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        Resources resources = context.getResources();
        this.mLauncher = (Launcher) context;
        this.mStylusEventHelper = new StylusEventHelper(this);
        this.mDimensionsFormatString = resources.getString(2131558413);
        setContainerWidth();
        setWillNotDraw(false);
        setClipToPadding(false);
        setAccessibilityDelegate(LauncherAppState.getInstance().getAccessibilityDelegate());
    }

    private void setContainerWidth() {
        this.cellSize = (int) (this.mLauncher.getDeviceProfile().cellWidthPx * 2.6f);
        this.mPresetPreviewSize = (int) (this.cellSize * 0.8f);
    }

    public void applyFromAppWidgetProviderInfo(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, WidgetPreviewLoader widgetPreviewLoader) {
        InvariantDeviceProfile invariantDeviceProfile = LauncherAppState.getInstance().getInvariantDeviceProfile();
        this.mInfo = launcherAppWidgetProviderInfo;
        this.mWidgetName.setText(AppWidgetManagerCompat.getInstance(getContext()).loadLabel(launcherAppWidgetProviderInfo));
        this.mWidgetDims.setText(String.format(this.mDimensionsFormatString, Integer.valueOf(Math.min(launcherAppWidgetProviderInfo.spanX, invariantDeviceProfile.numColumns)), Integer.valueOf(Math.min(launcherAppWidgetProviderInfo.spanY, invariantDeviceProfile.numRows))));
        this.mWidgetPreviewLoader = widgetPreviewLoader;
    }

    public void applyFromResolveInfo(PackageManager packageManager, ResolveInfo resolveInfo, WidgetPreviewLoader widgetPreviewLoader) {
        this.mInfo = resolveInfo;
        this.mWidgetName.setText(resolveInfo.loadLabel(packageManager));
        this.mWidgetDims.setText(String.format(this.mDimensionsFormatString, 1, 1));
        this.mWidgetPreviewLoader = widgetPreviewLoader;
    }

    public void applyPreview(Bitmap bitmap) {
        if (bitmap != null) {
            this.mWidgetImage.setBitmap(bitmap);
            this.mWidgetImage.setAlpha(0.0f);
            this.mWidgetImage.animate().alpha(1.0f).setDuration(90L);
        }
    }

    public void clear() {
        this.mWidgetImage.animate().cancel();
        this.mWidgetImage.setBitmap(null);
        this.mWidgetName.setText((CharSequence) null);
        this.mWidgetDims.setText((CharSequence) null);
        if (this.mActiveRequest != null) {
            this.mActiveRequest.cleanup();
            this.mActiveRequest = null;
        }
    }

    public void ensurePreview() {
        if (this.mActiveRequest != null) {
            return;
        }
        int[] previewSize = getPreviewSize();
        this.mActiveRequest = this.mWidgetPreviewLoader.getPreview(this.mInfo, previewSize[0], previewSize[1], this);
    }

    public int[] getPreviewSize() {
        return new int[]{this.mPresetPreviewSize, this.mPresetPreviewSize};
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mWidgetImage = (WidgetImageView) findViewById(2131296329);
        this.mWidgetName = (TextView) findViewById(2131296327);
        this.mWidgetDims = (TextView) findViewById(2131296328);
    }

    @Override // android.view.View.OnLayoutChangeListener
    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        removeOnLayoutChangeListener(this);
        ensurePreview();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent = super.onTouchEvent(motionEvent);
        if (this.mStylusEventHelper.checkAndPerformStylusEvent(motionEvent)) {
            return true;
        }
        return onTouchEvent;
    }
}
