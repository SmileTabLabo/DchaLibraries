package com.android.launcher3.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.view.View;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.folder.FolderIcon;
import com.android.launcher3.graphics.BitmapRenderer;
import com.android.launcher3.util.UiThreadHelper;
import com.android.launcher3.widget.LauncherAppWidgetHostView;
import java.nio.ByteBuffer;
/* loaded from: classes.dex */
public class DragPreviewProvider {
    protected final int blurSizeOutline;
    public Bitmap generatedDragOutline;
    private OutlineGeneratorCallback mOutlineGeneratorCallback;
    private final Rect mTempRect;
    protected final View mView;
    public final int previewPadding;

    public DragPreviewProvider(View view) {
        this(view, view.getContext());
    }

    public DragPreviewProvider(View view, Context context) {
        this.mTempRect = new Rect();
        this.mView = view;
        this.blurSizeOutline = context.getResources().getDimensionPixelSize(R.dimen.blur_size_medium_outline);
        if (this.mView instanceof BubbleTextView) {
            Rect drawableBounds = getDrawableBounds(((BubbleTextView) this.mView).getIcon());
            this.previewPadding = (this.blurSizeOutline - drawableBounds.left) - drawableBounds.top;
            return;
        }
        this.previewPadding = this.blurSizeOutline;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void drawDragView(Canvas canvas, float f) {
        canvas.save();
        canvas.scale(f, f);
        if (this.mView instanceof BubbleTextView) {
            Drawable icon = ((BubbleTextView) this.mView).getIcon();
            Rect drawableBounds = getDrawableBounds(icon);
            canvas.translate((this.blurSizeOutline / 2) - drawableBounds.left, (this.blurSizeOutline / 2) - drawableBounds.top);
            icon.draw(canvas);
        } else {
            Rect rect = this.mTempRect;
            this.mView.getDrawingRect(rect);
            boolean z = false;
            if ((this.mView instanceof FolderIcon) && ((FolderIcon) this.mView).getTextVisible()) {
                ((FolderIcon) this.mView).setTextVisible(false);
                z = true;
            }
            canvas.translate((-this.mView.getScrollX()) + (this.blurSizeOutline / 2), (-this.mView.getScrollY()) + (this.blurSizeOutline / 2));
            canvas.clipRect(rect);
            this.mView.draw(canvas);
            if (z) {
                ((FolderIcon) this.mView).setTextVisible(true);
            }
        }
        canvas.restore();
    }

    public Bitmap createDragBitmap() {
        int width = this.mView.getWidth();
        int height = this.mView.getHeight();
        if (this.mView instanceof BubbleTextView) {
            Rect drawableBounds = getDrawableBounds(((BubbleTextView) this.mView).getIcon());
            int width2 = drawableBounds.width();
            height = drawableBounds.height();
            width = width2;
        } else if (this.mView instanceof LauncherAppWidgetHostView) {
            final float scaleToFit = ((LauncherAppWidgetHostView) this.mView).getScaleToFit();
            return BitmapRenderer.createSoftwareBitmap(((int) (this.mView.getWidth() * scaleToFit)) + this.blurSizeOutline, ((int) (this.mView.getHeight() * scaleToFit)) + this.blurSizeOutline, new BitmapRenderer.Renderer() { // from class: com.android.launcher3.graphics.-$$Lambda$DragPreviewProvider$mOiJTXeJnD_eeMi0HJ3nPr-nzk4
                @Override // com.android.launcher3.graphics.BitmapRenderer.Renderer
                public final void draw(Canvas canvas) {
                    DragPreviewProvider.this.drawDragView(canvas, scaleToFit);
                }
            });
        }
        return BitmapRenderer.createHardwareBitmap(width + this.blurSizeOutline, height + this.blurSizeOutline, new BitmapRenderer.Renderer() { // from class: com.android.launcher3.graphics.-$$Lambda$DragPreviewProvider$ku63NENVzwDPH8dVoEkuHh0oxUk
            @Override // com.android.launcher3.graphics.BitmapRenderer.Renderer
            public final void draw(Canvas canvas) {
                DragPreviewProvider.this.drawDragView(canvas, 1.0f);
            }
        });
    }

    public final void generateDragOutline(Bitmap bitmap) {
        this.mOutlineGeneratorCallback = new OutlineGeneratorCallback(bitmap);
        new Handler(UiThreadHelper.getBackgroundLooper()).post(this.mOutlineGeneratorCallback);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static Rect getDrawableBounds(Drawable drawable) {
        Rect rect = new Rect();
        drawable.copyBounds(rect);
        if (rect.width() == 0 || rect.height() == 0) {
            rect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        } else {
            rect.offsetTo(0, 0);
        }
        return rect;
    }

    public float getScaleAndPosition(Bitmap bitmap, int[] iArr) {
        float locationInDragLayer = Launcher.getLauncher(this.mView.getContext()).getDragLayer().getLocationInDragLayer(this.mView, iArr);
        if (this.mView instanceof LauncherAppWidgetHostView) {
            locationInDragLayer /= ((LauncherAppWidgetHostView) this.mView).getScaleToFit();
        }
        iArr[0] = Math.round(iArr[0] - ((bitmap.getWidth() - ((this.mView.getWidth() * locationInDragLayer) * this.mView.getScaleX())) / 2.0f));
        iArr[1] = Math.round((iArr[1] - (((1.0f - locationInDragLayer) * bitmap.getHeight()) / 2.0f)) - (this.previewPadding / 2));
        return locationInDragLayer;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Bitmap convertPreviewToAlphaBitmap(Bitmap bitmap) {
        return bitmap.copy(Bitmap.Config.ALPHA_8, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class OutlineGeneratorCallback implements Runnable {
        private final Context mContext;
        private final Bitmap mPreviewSnapshot;

        OutlineGeneratorCallback(Bitmap bitmap) {
            this.mPreviewSnapshot = bitmap;
            this.mContext = DragPreviewProvider.this.mView.getContext();
        }

        @Override // java.lang.Runnable
        public void run() {
            int[] iArr;
            int[] iArr2;
            int[] iArr3;
            Bitmap convertPreviewToAlphaBitmap = DragPreviewProvider.this.convertPreviewToAlphaBitmap(this.mPreviewSnapshot);
            byte[] bArr = new byte[convertPreviewToAlphaBitmap.getWidth() * convertPreviewToAlphaBitmap.getHeight()];
            ByteBuffer wrap = ByteBuffer.wrap(bArr);
            wrap.rewind();
            convertPreviewToAlphaBitmap.copyPixelsToBuffer(wrap);
            for (int i = 0; i < bArr.length; i++) {
                if ((bArr[i] & 255) < 188) {
                    bArr[i] = 0;
                }
            }
            wrap.rewind();
            convertPreviewToAlphaBitmap.copyPixelsFromBuffer(wrap);
            Paint paint = new Paint(3);
            Canvas canvas = new Canvas();
            paint.setMaskFilter(new BlurMaskFilter(DragPreviewProvider.this.blurSizeOutline, BlurMaskFilter.Blur.OUTER));
            Bitmap extractAlpha = convertPreviewToAlphaBitmap.extractAlpha(paint, new int[2]);
            paint.setMaskFilter(new BlurMaskFilter(this.mContext.getResources().getDimension(R.dimen.blur_size_thin_outline), BlurMaskFilter.Blur.OUTER));
            Bitmap extractAlpha2 = convertPreviewToAlphaBitmap.extractAlpha(paint, new int[2]);
            canvas.setBitmap(convertPreviewToAlphaBitmap);
            canvas.drawColor(ViewCompat.MEASURED_STATE_MASK, PorterDuff.Mode.SRC_OUT);
            paint.setMaskFilter(new BlurMaskFilter(DragPreviewProvider.this.blurSizeOutline, BlurMaskFilter.Blur.NORMAL));
            Bitmap extractAlpha3 = convertPreviewToAlphaBitmap.extractAlpha(paint, new int[2]);
            paint.setMaskFilter(null);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            canvas.setBitmap(extractAlpha3);
            canvas.drawBitmap(convertPreviewToAlphaBitmap, -iArr3[0], -iArr3[1], paint);
            canvas.drawRect(0.0f, 0.0f, -iArr3[0], extractAlpha3.getHeight(), paint);
            canvas.drawRect(0.0f, 0.0f, extractAlpha3.getWidth(), -iArr3[1], paint);
            paint.setXfermode(null);
            canvas.setBitmap(convertPreviewToAlphaBitmap);
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            canvas.drawBitmap(extractAlpha3, iArr3[0], iArr3[1], paint);
            canvas.drawBitmap(extractAlpha, iArr[0], iArr[1], paint);
            canvas.drawBitmap(extractAlpha2, iArr2[0], iArr2[1], paint);
            canvas.setBitmap(null);
            extractAlpha2.recycle();
            extractAlpha.recycle();
            extractAlpha3.recycle();
            DragPreviewProvider.this.generatedDragOutline = convertPreviewToAlphaBitmap;
        }
    }
}
