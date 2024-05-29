package com.android.gallery3d.glrenderer;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
/* loaded from: a.zip:com/android/gallery3d/glrenderer/GLES20Canvas.class */
public class GLES20Canvas implements GLCanvas {
    private int mBoxCoordinates;
    private int mDrawProgram;
    private int mHeight;
    private int mMeshProgram;
    private int mOesTextureProgram;
    private int mScreenHeight;
    private int mScreenWidth;
    private int mTextureProgram;
    private int mWidth;
    private static final String TAG = GLES20Canvas.class.getSimpleName();
    private static final float[] BOX_COORDINATES = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f};
    private static final float[] BOUNDS_COORDINATES = {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f};
    private static final GLId mGLId = new GLES20IdImpl();
    private float[] mMatrices = new float[128];
    private float[] mAlphas = new float[8];
    private IntArray mSaveFlags = new IntArray();
    private int mCurrentAlphaIndex = 0;
    private int mCurrentMatrixIndex = 0;
    private float[] mProjectionMatrix = new float[16];
    ShaderParameter[] mDrawParameters = {new AttributeShaderParameter("aPosition"), new UniformShaderParameter("uMatrix"), new UniformShaderParameter("uColor")};
    ShaderParameter[] mTextureParameters = {new AttributeShaderParameter("aPosition"), new UniformShaderParameter("uMatrix"), new UniformShaderParameter("uTextureMatrix"), new UniformShaderParameter("uTextureSampler"), new UniformShaderParameter("uAlpha")};
    ShaderParameter[] mOesTextureParameters = {new AttributeShaderParameter("aPosition"), new UniformShaderParameter("uMatrix"), new UniformShaderParameter("uTextureMatrix"), new UniformShaderParameter("uTextureSampler"), new UniformShaderParameter("uAlpha")};
    ShaderParameter[] mMeshParameters = {new AttributeShaderParameter("aPosition"), new UniformShaderParameter("uMatrix"), new AttributeShaderParameter("aTextureCoordinate"), new UniformShaderParameter("uTextureSampler"), new UniformShaderParameter("uAlpha")};
    private final IntArray mUnboundTextures = new IntArray();
    private final IntArray mDeleteBuffers = new IntArray();
    private int mCountDrawMesh = 0;
    private int mCountTextureRect = 0;
    private int mCountFillRect = 0;
    private int mCountDrawLine = 0;
    private int[] mFrameBuffer = new int[1];
    private ArrayList<RawTexture> mTargetTextures = new ArrayList<>();
    private final float[] mTempMatrix = new float[32];
    private final float[] mTempColor = new float[4];
    private final RectF mTempSourceRect = new RectF();
    private final RectF mTempTargetRect = new RectF();
    private final float[] mTempTextureMatrix = new float[16];
    private final int[] mTempIntArray = new int[1];

    /* loaded from: a.zip:com/android/gallery3d/glrenderer/GLES20Canvas$AttributeShaderParameter.class */
    private static class AttributeShaderParameter extends ShaderParameter {
        public AttributeShaderParameter(String str) {
            super(str);
        }

        @Override // com.android.gallery3d.glrenderer.GLES20Canvas.ShaderParameter
        public void loadHandle(int i) {
            this.handle = GLES20.glGetAttribLocation(i, this.mName);
            GLES20Canvas.checkError();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/gallery3d/glrenderer/GLES20Canvas$ShaderParameter.class */
    public static abstract class ShaderParameter {
        public int handle;
        protected final String mName;

        public ShaderParameter(String str) {
            this.mName = str;
        }

        public abstract void loadHandle(int i);
    }

    /* loaded from: a.zip:com/android/gallery3d/glrenderer/GLES20Canvas$UniformShaderParameter.class */
    private static class UniformShaderParameter extends ShaderParameter {
        public UniformShaderParameter(String str) {
            super(str);
        }

        @Override // com.android.gallery3d.glrenderer.GLES20Canvas.ShaderParameter
        public void loadHandle(int i) {
            this.handle = GLES20.glGetUniformLocation(i, this.mName);
            GLES20Canvas.checkError();
        }
    }

    public GLES20Canvas() {
        Matrix.setIdentityM(this.mTempTextureMatrix, 0);
        Matrix.setIdentityM(this.mMatrices, this.mCurrentMatrixIndex);
        this.mAlphas[this.mCurrentAlphaIndex] = 1.0f;
        this.mTargetTextures.add(null);
        this.mBoxCoordinates = uploadBuffer(createBuffer(BOX_COORDINATES));
        int loadShader = loadShader(35633, "uniform mat4 uMatrix;\nattribute vec2 aPosition;\nvoid main() {\n  vec4 pos = vec4(aPosition, 0.0, 1.0);\n  gl_Position = uMatrix * pos;\n}\n");
        int loadShader2 = loadShader(35633, "uniform mat4 uMatrix;\nuniform mat4 uTextureMatrix;\nattribute vec2 aPosition;\nvarying vec2 vTextureCoord;\nvoid main() {\n  vec4 pos = vec4(aPosition, 0.0, 1.0);\n  gl_Position = uMatrix * pos;\n  vTextureCoord = (uTextureMatrix * pos).xy;\n}\n");
        int loadShader3 = loadShader(35633, "uniform mat4 uMatrix;\nattribute vec2 aPosition;\nattribute vec2 aTextureCoordinate;\nvarying vec2 vTextureCoord;\nvoid main() {\n  vec4 pos = vec4(aPosition, 0.0, 1.0);\n  gl_Position = uMatrix * pos;\n  vTextureCoord = aTextureCoordinate;\n}\n");
        int loadShader4 = loadShader(35632, "precision mediump float;\nuniform vec4 uColor;\nvoid main() {\n  gl_FragColor = uColor;\n}\n");
        int loadShader5 = loadShader(35632, "precision mediump float;\nvarying vec2 vTextureCoord;\nuniform float uAlpha;\nuniform sampler2D uTextureSampler;\nvoid main() {\n  gl_FragColor = texture2D(uTextureSampler, vTextureCoord);\n  gl_FragColor *= uAlpha;\n}\n");
        int loadShader6 = loadShader(35632, "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTextureCoord;\nuniform float uAlpha;\nuniform samplerExternalOES uTextureSampler;\nvoid main() {\n  gl_FragColor = texture2D(uTextureSampler, vTextureCoord);\n  gl_FragColor *= uAlpha;\n}\n");
        this.mDrawProgram = assembleProgram(loadShader, loadShader4, this.mDrawParameters);
        this.mTextureProgram = assembleProgram(loadShader2, loadShader5, this.mTextureParameters);
        this.mOesTextureProgram = assembleProgram(loadShader2, loadShader6, this.mOesTextureParameters);
        this.mMeshProgram = assembleProgram(loadShader3, loadShader5, this.mMeshParameters);
        GLES20.glBlendFunc(1, 771);
        checkError();
    }

    private int assembleProgram(int i, int i2, ShaderParameter[] shaderParameterArr) {
        int glCreateProgram = GLES20.glCreateProgram();
        checkError();
        if (glCreateProgram == 0) {
            throw new RuntimeException("Cannot create GL program: " + GLES20.glGetError());
        }
        GLES20.glAttachShader(glCreateProgram, i);
        checkError();
        GLES20.glAttachShader(glCreateProgram, i2);
        checkError();
        GLES20.glLinkProgram(glCreateProgram);
        checkError();
        int[] iArr = this.mTempIntArray;
        GLES20.glGetProgramiv(glCreateProgram, 35714, iArr, 0);
        int i3 = glCreateProgram;
        if (iArr[0] != 1) {
            Log.e(TAG, "Could not link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(glCreateProgram));
            GLES20.glDeleteProgram(glCreateProgram);
            i3 = 0;
        }
        for (ShaderParameter shaderParameter : shaderParameterArr) {
            shaderParameter.loadHandle(i3);
        }
        return i3;
    }

    public static void checkError() {
        int glGetError = GLES20.glGetError();
        if (glGetError != 0) {
            Log.e(TAG, "GL error: " + glGetError, new Throwable());
        }
    }

    private static void convertCoordinate(RectF rectF, RectF rectF2, BasicTexture basicTexture) {
        int width = basicTexture.getWidth();
        int height = basicTexture.getHeight();
        int textureWidth = basicTexture.getTextureWidth();
        int textureHeight = basicTexture.getTextureHeight();
        rectF.left /= textureWidth;
        rectF.right /= textureWidth;
        rectF.top /= textureHeight;
        rectF.bottom /= textureHeight;
        float f = width / textureWidth;
        if (rectF.right > f) {
            rectF2.right = rectF2.left + ((rectF2.width() * (f - rectF.left)) / rectF.width());
            rectF.right = f;
        }
        float f2 = height / textureHeight;
        if (rectF.bottom > f2) {
            rectF2.bottom = rectF2.top + ((rectF2.height() * (f2 - rectF.top)) / rectF.height());
            rectF.bottom = f2;
        }
    }

    private static void copyTextureCoordinates(BasicTexture basicTexture, RectF rectF) {
        int i = 0;
        int i2 = 0;
        int width = basicTexture.getWidth();
        int height = basicTexture.getHeight();
        int i3 = height;
        int i4 = width;
        if (basicTexture.hasBorder()) {
            i = 1;
            i2 = 1;
            i4 = width - 1;
            i3 = height - 1;
        }
        rectF.set(i, i2, i4, i3);
    }

    private static FloatBuffer createBuffer(float[] fArr) {
        FloatBuffer asFloatBuffer = ByteBuffer.allocateDirect(fArr.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        asFloatBuffer.put(fArr, 0, fArr.length).position(0);
        return asFloatBuffer;
    }

    private void draw(ShaderParameter[] shaderParameterArr, int i, int i2, float f, float f2, float f3, float f4) {
        setMatrix(shaderParameterArr, f, f2, f3, f4);
        int i3 = shaderParameterArr[0].handle;
        GLES20.glEnableVertexAttribArray(i3);
        checkError();
        GLES20.glDrawArrays(i, 0, i2);
        checkError();
        GLES20.glDisableVertexAttribArray(i3);
        checkError();
    }

    private void drawTextureRect(BasicTexture basicTexture, RectF rectF, RectF rectF2) {
        setTextureMatrix(rectF);
        drawTextureRect(basicTexture, this.mTempTextureMatrix, rectF2);
    }

    private void drawTextureRect(BasicTexture basicTexture, float[] fArr, RectF rectF) {
        ShaderParameter[] prepareTexture = prepareTexture(basicTexture);
        setPosition(prepareTexture, 0);
        GLES20.glUniformMatrix4fv(prepareTexture[2].handle, 1, false, fArr, 0);
        checkError();
        if (basicTexture.isFlippedVertically()) {
            save(2);
            translate(0.0f, rectF.centerY());
            scale(1.0f, -1.0f, 1.0f);
            translate(0.0f, -rectF.centerY());
        }
        draw(prepareTexture, 5, 4, rectF.left, rectF.top, rectF.width(), rectF.height());
        if (basicTexture.isFlippedVertically()) {
            restore();
        }
        this.mCountTextureRect++;
    }

    private void enableBlending(boolean z) {
        if (z) {
            GLES20.glEnable(3042);
            checkError();
            return;
        }
        GLES20.glDisable(3042);
        checkError();
    }

    private RawTexture getTargetTexture() {
        return this.mTargetTextures.get(this.mTargetTextures.size() - 1);
    }

    private static int loadShader(int i, String str) {
        int glCreateShader = GLES20.glCreateShader(i);
        GLES20.glShaderSource(glCreateShader, str);
        checkError();
        GLES20.glCompileShader(glCreateShader);
        checkError();
        return glCreateShader;
    }

    private void prepareTexture(BasicTexture basicTexture, int i, ShaderParameter[] shaderParameterArr) {
        deleteRecycledResources();
        GLES20.glUseProgram(i);
        checkError();
        boolean z = true;
        if (basicTexture.isOpaque()) {
            z = getAlpha() < 0.95f;
        }
        enableBlending(z);
        GLES20.glActiveTexture(33984);
        checkError();
        basicTexture.onBind(this);
        GLES20.glBindTexture(basicTexture.getTarget(), basicTexture.getId());
        checkError();
        GLES20.glUniform1i(shaderParameterArr[3].handle, 0);
        checkError();
        GLES20.glUniform1f(shaderParameterArr[4].handle, getAlpha());
        checkError();
    }

    private ShaderParameter[] prepareTexture(BasicTexture basicTexture) {
        ShaderParameter[] shaderParameterArr;
        int i;
        if (basicTexture.getTarget() == 3553) {
            shaderParameterArr = this.mTextureParameters;
            i = this.mTextureProgram;
        } else {
            shaderParameterArr = this.mOesTextureParameters;
            i = this.mOesTextureProgram;
        }
        prepareTexture(basicTexture, i, shaderParameterArr);
        return shaderParameterArr;
    }

    private void setMatrix(ShaderParameter[] shaderParameterArr, float f, float f2, float f3, float f4) {
        Matrix.translateM(this.mTempMatrix, 0, this.mMatrices, this.mCurrentMatrixIndex, f, f2, 0.0f);
        Matrix.scaleM(this.mTempMatrix, 0, f3, f4, 1.0f);
        Matrix.multiplyMM(this.mTempMatrix, 16, this.mProjectionMatrix, 0, this.mTempMatrix, 0);
        GLES20.glUniformMatrix4fv(shaderParameterArr[1].handle, 1, false, this.mTempMatrix, 16);
        checkError();
    }

    private void setPosition(ShaderParameter[] shaderParameterArr, int i) {
        GLES20.glBindBuffer(34962, this.mBoxCoordinates);
        checkError();
        GLES20.glVertexAttribPointer(shaderParameterArr[0].handle, 2, 5126, false, 8, i * 8);
        checkError();
        GLES20.glBindBuffer(34962, 0);
        checkError();
    }

    private void setTextureMatrix(RectF rectF) {
        this.mTempTextureMatrix[0] = rectF.width();
        this.mTempTextureMatrix[5] = rectF.height();
        this.mTempTextureMatrix[12] = rectF.left;
        this.mTempTextureMatrix[13] = rectF.top;
    }

    private int uploadBuffer(Buffer buffer, int i) {
        mGLId.glGenBuffers(1, this.mTempIntArray, 0);
        checkError();
        int i2 = this.mTempIntArray[0];
        GLES20.glBindBuffer(34962, i2);
        checkError();
        GLES20.glBufferData(34962, buffer.capacity() * i, buffer, 35044);
        checkError();
        return i2;
    }

    public void clearBuffer() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        checkError();
        GLES20.glClear(16384);
        checkError();
    }

    public void deleteRecycledResources() {
        synchronized (this.mUnboundTextures) {
            IntArray intArray = this.mUnboundTextures;
            if (this.mUnboundTextures.size() > 0) {
                mGLId.glDeleteTextures(null, intArray.size(), intArray.getInternalArray(), 0);
                intArray.clear();
            }
            IntArray intArray2 = this.mDeleteBuffers;
            if (intArray2.size() > 0) {
                mGLId.glDeleteBuffers(null, intArray2.size(), intArray2.getInternalArray(), 0);
                intArray2.clear();
            }
        }
    }

    @Override // com.android.gallery3d.glrenderer.GLCanvas
    public void drawTexture(BasicTexture basicTexture, int i, int i2, int i3, int i4) {
        if (i3 <= 0 || i4 <= 0) {
            return;
        }
        copyTextureCoordinates(basicTexture, this.mTempSourceRect);
        this.mTempTargetRect.set(i, i2, i + i3, i2 + i4);
        convertCoordinate(this.mTempSourceRect, this.mTempTargetRect, basicTexture);
        drawTextureRect(basicTexture, this.mTempSourceRect, this.mTempTargetRect);
    }

    @Override // com.android.gallery3d.glrenderer.GLCanvas
    public void drawTexture(BasicTexture basicTexture, RectF rectF, RectF rectF2) {
        if (rectF2.width() <= 0.0f || rectF2.height() <= 0.0f) {
            return;
        }
        this.mTempSourceRect.set(rectF);
        this.mTempTargetRect.set(rectF2);
        convertCoordinate(this.mTempSourceRect, this.mTempTargetRect, basicTexture);
        drawTextureRect(basicTexture, this.mTempSourceRect, this.mTempTargetRect);
    }

    public float getAlpha() {
        return this.mAlphas[this.mCurrentAlphaIndex];
    }

    @Override // com.android.gallery3d.glrenderer.GLCanvas
    public GLId getGLId() {
        return mGLId;
    }

    @Override // com.android.gallery3d.glrenderer.GLCanvas
    public void initializeTexture(BasicTexture basicTexture, Bitmap bitmap) {
        int target = basicTexture.getTarget();
        GLES20.glBindTexture(target, basicTexture.getId());
        checkError();
        GLUtils.texImage2D(target, 0, bitmap, 0);
    }

    @Override // com.android.gallery3d.glrenderer.GLCanvas
    public void initializeTextureSize(BasicTexture basicTexture, int i, int i2) {
        int target = basicTexture.getTarget();
        GLES20.glBindTexture(target, basicTexture.getId());
        checkError();
        GLES20.glTexImage2D(target, 0, i, basicTexture.getTextureWidth(), basicTexture.getTextureHeight(), 0, i, i2, null);
    }

    @Override // com.android.gallery3d.glrenderer.GLCanvas
    public void restore() {
        int removeLast = this.mSaveFlags.removeLast();
        if ((removeLast & 1) == 1) {
            this.mCurrentAlphaIndex--;
        }
        if ((removeLast & 2) == 2) {
            this.mCurrentMatrixIndex -= 16;
        }
    }

    @Override // com.android.gallery3d.glrenderer.GLCanvas
    public void rotate(float f, float f2, float f3, float f4) {
        if (f == 0.0f) {
            return;
        }
        float[] fArr = this.mTempMatrix;
        Matrix.setRotateM(fArr, 0, f, f2, f3, f4);
        float[] fArr2 = this.mMatrices;
        int i = this.mCurrentMatrixIndex;
        Matrix.multiplyMM(fArr, 16, fArr2, i, fArr, 0);
        System.arraycopy(fArr, 16, fArr2, i, 16);
    }

    @Override // com.android.gallery3d.glrenderer.GLCanvas
    public void save(int i) {
        if ((i & 1) == 1) {
            float alpha = getAlpha();
            this.mCurrentAlphaIndex++;
            if (this.mAlphas.length <= this.mCurrentAlphaIndex) {
                this.mAlphas = Arrays.copyOf(this.mAlphas, this.mAlphas.length * 2);
            }
            this.mAlphas[this.mCurrentAlphaIndex] = alpha;
        }
        if ((i & 2) == 2) {
            int i2 = this.mCurrentMatrixIndex;
            this.mCurrentMatrixIndex += 16;
            if (this.mMatrices.length <= this.mCurrentMatrixIndex) {
                this.mMatrices = Arrays.copyOf(this.mMatrices, this.mMatrices.length * 2);
            }
            System.arraycopy(this.mMatrices, i2, this.mMatrices, this.mCurrentMatrixIndex, 16);
        }
        this.mSaveFlags.add(i);
    }

    public void scale(float f, float f2, float f3) {
        Matrix.scaleM(this.mMatrices, this.mCurrentMatrixIndex, f, f2, f3);
    }

    public void setSize(int i, int i2) {
        this.mWidth = i;
        this.mHeight = i2;
        GLES20.glViewport(0, 0, this.mWidth, this.mHeight);
        checkError();
        Matrix.setIdentityM(this.mMatrices, this.mCurrentMatrixIndex);
        Matrix.orthoM(this.mProjectionMatrix, 0, 0.0f, i, 0.0f, i2, -1.0f, 1.0f);
        if (getTargetTexture() == null) {
            this.mScreenWidth = i;
            this.mScreenHeight = i2;
            Matrix.translateM(this.mMatrices, this.mCurrentMatrixIndex, 0.0f, i2, 0.0f);
            Matrix.scaleM(this.mMatrices, this.mCurrentMatrixIndex, 1.0f, -1.0f, 1.0f);
        }
    }

    @Override // com.android.gallery3d.glrenderer.GLCanvas
    public void setTextureParameters(BasicTexture basicTexture) {
        int target = basicTexture.getTarget();
        GLES20.glBindTexture(target, basicTexture.getId());
        checkError();
        GLES20.glTexParameteri(target, 10242, 33071);
        GLES20.glTexParameteri(target, 10243, 33071);
        GLES20.glTexParameterf(target, 10241, 9729.0f);
        GLES20.glTexParameterf(target, 10240, 9729.0f);
    }

    @Override // com.android.gallery3d.glrenderer.GLCanvas
    public void texSubImage2D(BasicTexture basicTexture, int i, int i2, Bitmap bitmap, int i3, int i4) {
        int target = basicTexture.getTarget();
        GLES20.glBindTexture(target, basicTexture.getId());
        checkError();
        GLUtils.texSubImage2D(target, 0, i, i2, bitmap, i3, i4);
    }

    @Override // com.android.gallery3d.glrenderer.GLCanvas
    public void translate(float f, float f2) {
        int i = this.mCurrentMatrixIndex;
        float[] fArr = this.mMatrices;
        int i2 = i + 12;
        fArr[i2] = fArr[i2] + (fArr[i + 0] * f) + (fArr[i + 4] * f2);
        int i3 = i + 13;
        fArr[i3] = fArr[i3] + (fArr[i + 1] * f) + (fArr[i + 5] * f2);
        int i4 = i + 14;
        fArr[i4] = fArr[i4] + (fArr[i + 2] * f) + (fArr[i + 6] * f2);
        int i5 = i + 15;
        fArr[i5] = fArr[i5] + (fArr[i + 3] * f) + (fArr[i + 7] * f2);
    }

    @Override // com.android.gallery3d.glrenderer.GLCanvas
    public boolean unloadTexture(BasicTexture basicTexture) {
        boolean isLoaded = basicTexture.isLoaded();
        if (isLoaded) {
            synchronized (this.mUnboundTextures) {
                this.mUnboundTextures.add(basicTexture.getId());
            }
        }
        return isLoaded;
    }

    public int uploadBuffer(FloatBuffer floatBuffer) {
        return uploadBuffer(floatBuffer, 4);
    }
}
