package com.android.systemui.statusbar.notification;

import android.graphics.drawable.Icon;
import android.util.Pools;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.notification.TransformState;
/* loaded from: classes.dex */
public class ImageTransformState extends TransformState {
    private static Pools.SimplePool<ImageTransformState> sInstancePool = new Pools.SimplePool<>(40);
    private Icon mIcon;

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void initFrom(View view, TransformState.TransformInfo transformInfo) {
        super.initFrom(view, transformInfo);
        if (view instanceof ImageView) {
            this.mIcon = (Icon) view.getTag(R.id.image_icon_tag);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public boolean sameAs(TransformState transformState) {
        if (super.sameAs(transformState)) {
            return true;
        }
        if (transformState instanceof ImageTransformState) {
            return this.mIcon != null && this.mIcon.sameAs(((ImageTransformState) transformState).getIcon());
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void appear(float f, TransformableView transformableView) {
        if (transformableView instanceof HybridNotificationView) {
            if (f == 0.0f) {
                this.mTransformedView.setPivotY(0.0f);
                this.mTransformedView.setPivotX(this.mTransformedView.getWidth() / 2);
                prepareFadeIn();
            }
            float mapToDuration = mapToDuration(f);
            CrossFadeHelper.fadeIn(this.mTransformedView, mapToDuration, false);
            float interpolation = Interpolators.LINEAR_OUT_SLOW_IN.getInterpolation(mapToDuration);
            this.mTransformedView.setScaleX(interpolation);
            this.mTransformedView.setScaleY(interpolation);
            return;
        }
        super.appear(f, transformableView);
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void disappear(float f, TransformableView transformableView) {
        if (transformableView instanceof HybridNotificationView) {
            if (f == 0.0f) {
                this.mTransformedView.setPivotY(0.0f);
                this.mTransformedView.setPivotX(this.mTransformedView.getWidth() / 2);
            }
            float mapToDuration = mapToDuration(1.0f - f);
            CrossFadeHelper.fadeOut(this.mTransformedView, 1.0f - mapToDuration, false);
            float interpolation = Interpolators.LINEAR_OUT_SLOW_IN.getInterpolation(mapToDuration);
            this.mTransformedView.setScaleX(interpolation);
            this.mTransformedView.setScaleY(interpolation);
            return;
        }
        super.disappear(f, transformableView);
    }

    private static float mapToDuration(float f) {
        return Math.max(Math.min(((f * 360.0f) - 150.0f) / 210.0f, 1.0f), 0.0f);
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public static ImageTransformState obtain() {
        ImageTransformState imageTransformState = (ImageTransformState) sInstancePool.acquire();
        if (imageTransformState != null) {
            return imageTransformState;
        }
        return new ImageTransformState();
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    protected boolean transformScale(TransformState transformState) {
        return sameAs(transformState);
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void recycle() {
        super.recycle();
        if (getClass() == ImageTransformState.class) {
            sInstancePool.release(this);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public void reset() {
        super.reset();
        this.mIcon = null;
    }
}
