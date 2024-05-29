package com.android.gallery3d.glrenderer;

import android.util.Log;
/* loaded from: a.zip:com/android/gallery3d/glrenderer/RawTexture.class */
public class RawTexture extends BasicTexture {
    private boolean mIsFlipped;
    private final boolean mOpaque;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.gallery3d.glrenderer.BasicTexture
    public int getTarget() {
        return 3553;
    }

    @Override // com.android.gallery3d.glrenderer.BasicTexture
    public boolean isFlippedVertically() {
        return this.mIsFlipped;
    }

    @Override // com.android.gallery3d.glrenderer.Texture
    public boolean isOpaque() {
        return this.mOpaque;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.gallery3d.glrenderer.BasicTexture
    public boolean onBind(GLCanvas gLCanvas) {
        if (isLoaded()) {
            return true;
        }
        Log.w("RawTexture", "lost the content due to context change");
        return false;
    }

    @Override // com.android.gallery3d.glrenderer.BasicTexture
    public void yield() {
    }
}
