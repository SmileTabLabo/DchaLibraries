package com.android.gallery3d.glrenderer;

import javax.microedition.khronos.opengles.GL11;
/* loaded from: a.zip:com/android/gallery3d/glrenderer/GLId.class */
public interface GLId {
    int generateTexture();

    void glDeleteBuffers(GL11 gl11, int i, int[] iArr, int i2);

    void glDeleteTextures(GL11 gl11, int i, int[] iArr, int i2);

    void glGenBuffers(int i, int[] iArr, int i2);
}
