package com.android.launcher3.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.RemoteViews;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.DragSource;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.PendingAddItemInfo;
import com.android.launcher3.R;
import com.android.launcher3.dragndrop.DragOptions;
import com.android.launcher3.dragndrop.LivePreviewWidgetCell;
import com.android.launcher3.graphics.DragPreviewProvider;
import com.android.launcher3.graphics.LauncherIcons;
/* loaded from: classes.dex */
public class PendingItemDragHelper extends DragPreviewProvider {
    private static final float MAX_WIDGET_SCALE = 1.25f;
    private final PendingAddItemInfo mAddInfo;
    private int[] mEstimatedCellSize;
    private RemoteViews mPreview;

    public PendingItemDragHelper(View view) {
        super(view);
        this.mAddInfo = (PendingAddItemInfo) view.getTag();
    }

    public void setPreview(RemoteViews remoteViews) {
        this.mPreview = remoteViews;
    }

    public void startDrag(Rect rect, int i, int i2, Point point, DragSource dragSource, DragOptions dragOptions) {
        Bitmap bitmap;
        float width;
        Rect rect2;
        Point point2;
        Bitmap bitmap2;
        Launcher launcher = Launcher.getLauncher(this.mView.getContext());
        LauncherAppState launcherAppState = LauncherAppState.getInstance(launcher);
        this.mEstimatedCellSize = launcher.getWorkspace().estimateItemSize(this.mAddInfo);
        if (this.mAddInfo instanceof PendingAddWidgetInfo) {
            PendingAddWidgetInfo pendingAddWidgetInfo = (PendingAddWidgetInfo) this.mAddInfo;
            int min = Math.min((int) (i * MAX_WIDGET_SCALE), this.mEstimatedCellSize[0]);
            int[] iArr = new int[1];
            if (this.mPreview != null) {
                bitmap2 = LivePreviewWidgetCell.generateFromRemoteViews(launcher, this.mPreview, pendingAddWidgetInfo.info, min, iArr);
            } else {
                bitmap2 = null;
            }
            if (bitmap2 == null) {
                bitmap2 = launcherAppState.getWidgetCache().generateWidgetPreview(launcher, pendingAddWidgetInfo.info, min, null, iArr);
            }
            if (iArr[0] < i) {
                int i3 = (i - iArr[0]) / 2;
                if (i > i2) {
                    i3 = (i3 * i2) / i;
                }
                rect.left += i3;
                rect.right -= i3;
            }
            launcher.getDragController().addDragListener(new WidgetHostViewLoader(launcher, this.mView));
            width = rect.width() / bitmap2.getWidth();
            bitmap = bitmap2;
            point2 = null;
            rect2 = null;
        } else {
            Drawable fullResIcon = ((PendingAddShortcutInfo) this.mAddInfo).activityInfo.getFullResIcon(launcherAppState.getIconCache());
            LauncherIcons obtain = LauncherIcons.obtain(launcher);
            Bitmap createScaledBitmapWithoutShadow = obtain.createScaledBitmapWithoutShadow(fullResIcon, 0);
            obtain.recycle();
            Point point3 = new Point(this.previewPadding / 2, this.previewPadding / 2);
            DeviceProfile deviceProfile = launcher.getDeviceProfile();
            int i4 = deviceProfile.iconSizePx;
            int dimensionPixelSize = launcher.getResources().getDimensionPixelSize(R.dimen.widget_preview_shortcut_padding);
            rect.left += dimensionPixelSize;
            rect.top += dimensionPixelSize;
            Rect rect3 = new Rect();
            rect3.left = (this.mEstimatedCellSize[0] - i4) / 2;
            rect3.right = rect3.left + i4;
            rect3.top = (((this.mEstimatedCellSize[1] - i4) - deviceProfile.iconTextSizePx) - deviceProfile.iconDrawablePaddingPx) / 2;
            rect3.bottom = rect3.top + i4;
            bitmap = createScaledBitmapWithoutShadow;
            width = launcher.getDeviceProfile().iconSizePx / createScaledBitmapWithoutShadow.getWidth();
            rect2 = rect3;
            point2 = point3;
        }
        launcher.getWorkspace().prepareDragWithProvider(this);
        launcher.getDragController().startDrag(bitmap, point.x + rect.left + ((int) (((bitmap.getWidth() * width) - bitmap.getWidth()) / 2.0f)), point.y + rect.top + ((int) (((bitmap.getHeight() * width) - bitmap.getHeight()) / 2.0f)), dragSource, this.mAddInfo, point2, rect2, width, width, dragOptions);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.graphics.DragPreviewProvider
    public Bitmap convertPreviewToAlphaBitmap(Bitmap bitmap) {
        if ((this.mAddInfo instanceof PendingAddShortcutInfo) || this.mEstimatedCellSize == null) {
            return super.convertPreviewToAlphaBitmap(bitmap);
        }
        int i = this.mEstimatedCellSize[0];
        int i2 = this.mEstimatedCellSize[1];
        Bitmap createBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ALPHA_8);
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        float min = Math.min((i - this.blurSizeOutline) / bitmap.getWidth(), (i2 - this.blurSizeOutline) / bitmap.getHeight());
        int width = (int) (bitmap.getWidth() * min);
        int height = (int) (min * bitmap.getHeight());
        Rect rect2 = new Rect(0, 0, width, height);
        rect2.offset((i - width) / 2, (i2 - height) / 2);
        new Canvas(createBitmap).drawBitmap(bitmap, rect, rect2, new Paint(2));
        return createBitmap;
    }
}
