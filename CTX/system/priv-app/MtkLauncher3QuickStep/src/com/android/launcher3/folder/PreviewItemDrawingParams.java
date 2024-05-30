package com.android.launcher3.folder;

import android.graphics.drawable.Drawable;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class PreviewItemDrawingParams {
    FolderPreviewItemAnim anim;
    Drawable drawable;
    public boolean hidden;
    float overlayAlpha;
    float scale;
    float transX;
    float transY;

    /* JADX INFO: Access modifiers changed from: package-private */
    public PreviewItemDrawingParams(float f, float f2, float f3, float f4) {
        this.transX = f;
        this.transY = f2;
        this.scale = f3;
        this.overlayAlpha = f4;
    }

    public void update(float f, float f2, float f3) {
        if (this.anim != null) {
            if (this.anim.finalTransX == f || this.anim.finalTransY == f2 || this.anim.finalScale == f3) {
                return;
            }
            this.anim.cancel();
        }
        this.transX = f;
        this.transY = f2;
        this.scale = f3;
    }
}
