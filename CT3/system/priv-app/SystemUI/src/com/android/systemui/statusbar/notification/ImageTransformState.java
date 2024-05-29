package com.android.systemui.statusbar.notification;

import android.graphics.drawable.Icon;
import android.util.Pools;
import android.view.View;
import android.widget.ImageView;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/ImageTransformState.class */
public class ImageTransformState extends TransformState {
    private static Pools.SimplePool<ImageTransformState> sInstancePool = new Pools.SimplePool<>(40);
    private Icon mIcon;

    public static ImageTransformState obtain() {
        ImageTransformState imageTransformState = (ImageTransformState) sInstancePool.acquire();
        return imageTransformState != null ? imageTransformState : new ImageTransformState();
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void initFrom(View view) {
        super.initFrom(view);
        if (view instanceof ImageView) {
            this.mIcon = (Icon) view.getTag(2131886146);
        }
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void recycle() {
        super.recycle();
        sInstancePool.release(this);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public void reset() {
        super.reset();
        this.mIcon = null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public boolean sameAs(TransformState transformState) {
        if (transformState instanceof ImageTransformState) {
            return this.mIcon != null ? this.mIcon.sameAs(((ImageTransformState) transformState).getIcon()) : false;
        }
        return super.sameAs(transformState);
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    protected boolean transformScale() {
        return true;
    }
}
