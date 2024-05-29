package com.android.systemui;

import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.AsyncTask;
import android.os.SystemProperties;
import android.os.Trace;
import android.renderscript.Matrix4f;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.GraphicBuffer;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
/* loaded from: a.zip:com/android/systemui/ImageWallpaper.class */
public class ImageWallpaper extends WallpaperService {
    private Context mContext;
    DrawableEngine mEngine;
    boolean mIsHwAccelerated;
    private boolean mIstabletConfig;
    private BroadcastReceiver mReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.ImageWallpaper.1
        final ImageWallpaper this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null || !"android.intent.action.CONFIGURATION_CHANGED".equals(intent.getAction()) || this.this$0.mEngine == null) {
                return;
            }
            this.this$0.mEngine.drawFrame();
        }
    };
    WallpaperManager mWallpaperManager;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/systemui/ImageWallpaper$DrawableEngine.class */
    public class DrawableEngine extends WallpaperService.Engine {
        Bitmap mBackground;
        int mBackgroundHeight;
        int mBackgroundWidth;
        private GraphicBuffer mBuffer;
        private Display mDefaultDisplay;
        private int mDisplayHeightAtLastSurfaceSizeUpdate;
        private int mDisplayWidthAtLastSurfaceSizeUpdate;
        private EGL10 mEgl;
        private EGLConfig mEglConfig;
        private EGLContext mEglContext;
        private EGLDisplay mEglDisplay;
        private EGLSurface mEglSurface;
        private int mLastRequestedHeight;
        private int mLastRequestedWidth;
        int mLastRotation;
        int mLastSurfaceHeight;
        int mLastSurfaceWidth;
        int mLastXTranslation;
        int mLastYTranslation;
        private AsyncTask<Void, Void, Bitmap> mLoader;
        private boolean mNeedsDrawAfterLoadingWallpaper;
        boolean mOffsetsChanged;
        private int mRotationAtLastSurfaceSizeUpdate;
        float mScale;
        private boolean mSurfaceValid;
        private final DisplayInfo mTmpDisplayInfo;
        boolean mVisible;
        float mXOffset;
        float mYOffset;
        private boolean mYv12Enhancement;
        final ImageWallpaper this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public DrawableEngine(ImageWallpaper imageWallpaper) {
            super(imageWallpaper);
            this.this$0 = imageWallpaper;
            this.mBuffer = null;
            this.mBackgroundWidth = -1;
            this.mBackgroundHeight = -1;
            this.mLastSurfaceWidth = -1;
            this.mLastSurfaceHeight = -1;
            this.mLastRotation = -1;
            this.mXOffset = 0.5f;
            this.mYOffset = 0.5f;
            this.mScale = 1.0f;
            this.mTmpDisplayInfo = new DisplayInfo();
            this.mVisible = true;
            this.mRotationAtLastSurfaceSizeUpdate = -1;
            this.mDisplayWidthAtLastSurfaceSizeUpdate = -1;
            this.mDisplayHeightAtLastSurfaceSizeUpdate = -1;
            this.mLastRequestedWidth = -1;
            this.mLastRequestedHeight = -1;
            this.mYv12Enhancement = false;
            setFixedSizeAllowed(true);
        }

        private int buildProgram(String str, String str2) {
            int buildShader;
            int buildShader2 = buildShader(str, 35633);
            if (buildShader2 == 0 || (buildShader = buildShader(str2, 35632)) == 0) {
                return 0;
            }
            int glCreateProgram = GLES20.glCreateProgram();
            GLES20.glAttachShader(glCreateProgram, buildShader2);
            GLES20.glAttachShader(glCreateProgram, buildShader);
            GLES20.glLinkProgram(glCreateProgram);
            checkGlError();
            GLES20.glDeleteShader(buildShader2);
            GLES20.glDeleteShader(buildShader);
            int[] iArr = new int[1];
            GLES20.glGetProgramiv(glCreateProgram, 35714, iArr, 0);
            if (iArr[0] != 1) {
                Log.d("ImageWallpaperGL", "Error while linking program:\n" + GLES20.glGetProgramInfoLog(glCreateProgram));
                GLES20.glDeleteProgram(glCreateProgram);
                return 0;
            }
            return glCreateProgram;
        }

        private int buildShader(String str, int i) {
            int glCreateShader = GLES20.glCreateShader(i);
            GLES20.glShaderSource(glCreateShader, str);
            checkGlError();
            GLES20.glCompileShader(glCreateShader);
            checkGlError();
            int[] iArr = new int[1];
            GLES20.glGetShaderiv(glCreateShader, 35713, iArr, 0);
            if (iArr[0] != 1) {
                Log.d("ImageWallpaperGL", "Error while compiling shader:\n" + GLES20.glGetShaderInfoLog(glCreateShader));
                GLES20.glDeleteShader(glCreateShader);
                return 0;
            }
            return glCreateShader;
        }

        private void checkEglError() {
            int eglGetError = this.mEgl.eglGetError();
            if (eglGetError != 12288) {
                Log.w("ImageWallpaperGL", "EGL error = " + GLUtils.getEGLErrorString(eglGetError));
            }
        }

        private void checkGlError() {
            int glGetError = GLES20.glGetError();
            if (glGetError != 0) {
                Log.w("ImageWallpaperGL", "GL error = 0x" + Integer.toHexString(glGetError), new Throwable());
            }
        }

        private EGLConfig chooseEglConfig() {
            int[] iArr = new int[1];
            EGLConfig[] eGLConfigArr = new EGLConfig[1];
            if (this.mEgl.eglChooseConfig(this.mEglDisplay, getConfig(), eGLConfigArr, 1, iArr)) {
                if (iArr[0] > 0) {
                    return eGLConfigArr[0];
                }
                return null;
            }
            throw new IllegalArgumentException("eglChooseConfig failed " + GLUtils.getEGLErrorString(this.mEgl.eglGetError()));
        }

        private FloatBuffer createMesh(int i, int i2, float f, float f2) {
            float[] fArr = {i, f2, 0.0f, 0.0f, 1.0f, f, f2, 0.0f, 1.0f, 1.0f, i, i2, 0.0f, 0.0f, 0.0f, f, i2, 0.0f, 1.0f, 0.0f};
            FloatBuffer asFloatBuffer = ByteBuffer.allocateDirect(fArr.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            asFloatBuffer.put(fArr).position(0);
            return asFloatBuffer;
        }

        private void drawWallpaperWithCanvas(SurfaceHolder surfaceHolder, int i, int i2, int i3, int i4) {
            Canvas lockCanvas = surfaceHolder.lockCanvas();
            if (lockCanvas != null) {
                try {
                    float width = i3 + (this.mBackground.getWidth() * this.mScale);
                    float height = i4 + (this.mBackground.getHeight() * this.mScale);
                    if (i < 0 || i2 < 0) {
                        lockCanvas.save(2);
                        lockCanvas.clipRect(i3, i4, width, height, Region.Op.DIFFERENCE);
                        lockCanvas.drawColor(-16777216);
                        lockCanvas.restore();
                    }
                    if (this.mBackground != null) {
                        lockCanvas.drawBitmap(this.mBackground, (Rect) null, new RectF(i3, i4, width, height), (Paint) null);
                    }
                } finally {
                    surfaceHolder.unlockCanvasAndPost(lockCanvas);
                }
            }
        }

        private boolean drawWallpaperWithOpenGL(SurfaceHolder surfaceHolder, int i, int i2, int i3, int i4) {
            if (initGL(surfaceHolder)) {
                float width = this.mBackground.getWidth();
                float f = this.mScale;
                float height = this.mBackground.getHeight();
                float f2 = this.mScale;
                Rect surfaceFrame = surfaceHolder.getSurfaceFrame();
                Matrix4f matrix4f = new Matrix4f();
                matrix4f.loadOrtho(0.0f, surfaceFrame.width(), surfaceFrame.height(), 0.0f, -1.0f, 1.0f);
                FloatBuffer createMesh = createMesh(i3, i4, i3 + (width * f), i4 + (height * f2));
                int loadTexture_ext = "1".equals(SystemProperties.get("ro.mtk_gmo_ram_optimize", "0")) ? loadTexture_ext(this.mBackground) : loadTexture(this.mBackground);
                int buildProgram = ("1".equals(SystemProperties.get("ro.mtk_gmo_ram_optimize", "0")) && this.mYv12Enhancement && Utils.useYv12) ? buildProgram("attribute vec4 position;\nattribute vec2 texCoords;\nvarying vec2 outTexCoords;\nuniform mat4 projection;\n\nvoid main(void) {\n    outTexCoords = texCoords;\n    gl_Position = projection * position;\n}\n\n", "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\n\nvarying vec2 outTexCoords;\nuniform samplerExternalOES texture;\n\nvoid main(void) {\n    gl_FragColor = texture2D(texture, outTexCoords);\n}\n\n") : buildProgram("attribute vec4 position;\nattribute vec2 texCoords;\nvarying vec2 outTexCoords;\nuniform mat4 projection;\n\nvoid main(void) {\n    outTexCoords = texCoords;\n    gl_Position = projection * position;\n}\n\n", "precision mediump float;\n\nvarying vec2 outTexCoords;\nuniform sampler2D texture;\n\nvoid main(void) {\n    gl_FragColor = texture2D(texture, outTexCoords);\n}\n\n");
                int glGetAttribLocation = GLES20.glGetAttribLocation(buildProgram, "position");
                int glGetAttribLocation2 = GLES20.glGetAttribLocation(buildProgram, "texCoords");
                int glGetUniformLocation = GLES20.glGetUniformLocation(buildProgram, "texture");
                int glGetUniformLocation2 = GLES20.glGetUniformLocation(buildProgram, "projection");
                checkGlError();
                GLES20.glViewport(0, 0, surfaceFrame.width(), surfaceFrame.height());
                if ("1".equals(SystemProperties.get("ro.mtk_gmo_ram_optimize", "0")) && this.mYv12Enhancement) {
                    GLES20.glBindTexture(36197, loadTexture_ext);
                } else {
                    GLES20.glBindTexture(3553, loadTexture_ext);
                }
                GLES20.glUseProgram(buildProgram);
                GLES20.glEnableVertexAttribArray(glGetAttribLocation);
                GLES20.glEnableVertexAttribArray(glGetAttribLocation2);
                GLES20.glUniform1i(glGetUniformLocation, 0);
                GLES20.glUniformMatrix4fv(glGetUniformLocation2, 1, false, matrix4f.getArray(), 0);
                checkGlError();
                if (i > 0 || i2 > 0) {
                    if ("1".equals(SystemProperties.get("ro.mtk_gmo_ram_optimize", "0")) && this.mYv12Enhancement && Utils.useYv12) {
                        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                    } else {
                        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                    }
                    GLES20.glClear(16384);
                }
                createMesh.position(0);
                GLES20.glVertexAttribPointer(glGetAttribLocation, 3, 5126, false, 20, (Buffer) createMesh);
                createMesh.position(3);
                GLES20.glVertexAttribPointer(glGetAttribLocation2, 3, 5126, false, 20, (Buffer) createMesh);
                GLES20.glDrawArrays(5, 0, 4);
                boolean eglSwapBuffers = this.mEgl.eglSwapBuffers(this.mEglDisplay, this.mEglSurface);
                checkEglError();
                finishGL(loadTexture_ext, buildProgram);
                return eglSwapBuffers;
            }
            return false;
        }

        private void finishGL(int i, int i2) {
            GLES20.glDeleteTextures(1, new int[]{i}, 0);
            GLES20.glDeleteProgram(i2);
            this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            this.mEgl.eglDestroySurface(this.mEglDisplay, this.mEglSurface);
            this.mEgl.eglDestroyContext(this.mEglDisplay, this.mEglContext);
            this.mEgl.eglTerminate(this.mEglDisplay);
        }

        private int[] getConfig() {
            return new int[]{12352, 4, 12324, 8, 12323, 8, 12322, 8, 12321, 0, 12325, 0, 12326, 0, 12327, 12344, 12344};
        }

        /* JADX INFO: Access modifiers changed from: private */
        public DisplayInfo getDefaultDisplayInfo() {
            this.mDefaultDisplay.getDisplayInfo(this.mTmpDisplayInfo);
            return this.mTmpDisplayInfo;
        }

        private boolean initGL(SurfaceHolder surfaceHolder) {
            this.mEgl = (EGL10) EGLContext.getEGL();
            this.mEglDisplay = this.mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (this.mEglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed " + GLUtils.getEGLErrorString(this.mEgl.eglGetError()));
            }
            if (this.mEgl.eglInitialize(this.mEglDisplay, new int[2])) {
                this.mEglConfig = chooseEglConfig();
                if (this.mEglConfig == null) {
                    throw new RuntimeException("eglConfig not initialized");
                }
                this.mEglContext = createContext(this.mEgl, this.mEglDisplay, this.mEglConfig);
                if (this.mEglContext == EGL10.EGL_NO_CONTEXT) {
                    throw new RuntimeException("createContext failed " + GLUtils.getEGLErrorString(this.mEgl.eglGetError()));
                }
                EGLSurface eglCreatePbufferSurface = this.mEgl.eglCreatePbufferSurface(this.mEglDisplay, this.mEglConfig, new int[]{12375, 1, 12374, 1, 12344});
                this.mEgl.eglMakeCurrent(this.mEglDisplay, eglCreatePbufferSurface, eglCreatePbufferSurface, this.mEglContext);
                int[] iArr = new int[1];
                Rect surfaceFrame = surfaceHolder.getSurfaceFrame();
                GLES20.glGetIntegerv(3379, iArr, 0);
                this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                this.mEgl.eglDestroySurface(this.mEglDisplay, eglCreatePbufferSurface);
                if (surfaceFrame.width() > iArr[0] || surfaceFrame.height() > iArr[0]) {
                    this.mEgl.eglDestroyContext(this.mEglDisplay, this.mEglContext);
                    this.mEgl.eglTerminate(this.mEglDisplay);
                    Log.e("ImageWallpaperGL", "requested  texture size " + surfaceFrame.width() + "x" + surfaceFrame.height() + " exceeds the support maximum of " + iArr[0] + "x" + iArr[0]);
                    return false;
                }
                this.mEglSurface = this.mEgl.eglCreateWindowSurface(this.mEglDisplay, this.mEglConfig, surfaceHolder, null);
                if (this.mEglSurface != null && this.mEglSurface != EGL10.EGL_NO_SURFACE) {
                    if (this.mEgl.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext)) {
                        return true;
                    }
                    throw new RuntimeException("eglMakeCurrent failed " + GLUtils.getEGLErrorString(this.mEgl.eglGetError()));
                }
                int eglGetError = this.mEgl.eglGetError();
                if (eglGetError == 12299 || eglGetError == 12291) {
                    Log.e("ImageWallpaperGL", "createWindowSurface returned " + GLUtils.getEGLErrorString(eglGetError) + ".");
                    return false;
                }
                throw new RuntimeException("createWindowSurface failed " + GLUtils.getEGLErrorString(eglGetError));
            }
            throw new RuntimeException("eglInitialize failed " + GLUtils.getEGLErrorString(this.mEgl.eglGetError()));
        }

        private int loadTexture(Bitmap bitmap) {
            int[] iArr = new int[1];
            this.mYv12Enhancement = false;
            GLES20.glActiveTexture(33984);
            GLES20.glGenTextures(1, iArr, 0);
            checkGlError();
            int i = iArr[0];
            GLES20.glBindTexture(3553, i);
            checkGlError();
            Log.d("ImageWallpaper", "inside loadTexture");
            GLES20.glTexParameteri(3553, 10241, 9729);
            GLES20.glTexParameteri(3553, 10240, 9729);
            GLES20.glTexParameteri(3553, 10242, 33071);
            GLES20.glTexParameteri(3553, 10243, 33071);
            GLUtils.texImage2D(3553, 0, 6408, bitmap, 5121, 0);
            checkGlError();
            return i;
        }

        private int loadTexture_ext(Bitmap bitmap) {
            int[] iArr = new int[1];
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            this.mYv12Enhancement = true;
            Log.d("ImageWallpaper", "loadTexture_ext bitmap width " + width + ", height" + height);
            int i = width;
            if (width % 2 != 0) {
                i = width - 1;
            }
            int i2 = height;
            if (height % 2 != 0) {
                i2 = height - 1;
            }
            if (this.mBuffer == null) {
                this.mBuffer = GraphicBuffer.create(i, i2, 842094169, 288);
            }
            if (this.mBuffer == null || !Utils.useYv12) {
                Log.d("ImageWallpaper", "graphic buffer is null executing normal jpg useYv12 = " + Utils.useYv12);
                return loadTexture(bitmap);
            }
            GLES20.glActiveTexture(33984);
            GLES20.glGenTextures(1, iArr, 0);
            checkGlError();
            int i3 = iArr[0];
            GLES20.glBindTexture(36197, i3);
            checkGlError();
            GLES20.glTexParameteri(36197, 10241, 9729);
            GLES20.glTexParameteri(36197, 10240, 9729);
            GLES20.glTexParameteri(36197, 10242, 33071);
            GLES20.glTexParameteri(36197, 10243, 33071);
            Utils.BitmapToYv12(bitmap, this.mBuffer);
            Utils.createTexture2D(i3, this.mBuffer);
            if (this.mBackground != null) {
                this.mBackground.recycle();
                this.mBackground = null;
            }
            this.this$0.mWallpaperManager.forgetLoadedWallpaper();
            checkGlError();
            return i3;
        }

        /* JADX WARN: Type inference failed for: r1v3, types: [com.android.systemui.ImageWallpaper$DrawableEngine$1] */
        private void loadWallpaper(boolean z) {
            this.mNeedsDrawAfterLoadingWallpaper |= z;
            if (this.mLoader != null) {
                return;
            }
            this.mLoader = new AsyncTask<Void, Void, Bitmap>(this) { // from class: com.android.systemui.ImageWallpaper.DrawableEngine.1
                final DrawableEngine this$1;

                {
                    this.this$1 = this;
                }

                /* JADX INFO: Access modifiers changed from: protected */
                @Override // android.os.AsyncTask
                public Bitmap doInBackground(Void... voidArr) {
                    try {
                        return this.this$1.this$0.mWallpaperManager.getBitmap();
                    } catch (OutOfMemoryError | RuntimeException e) {
                        if (e != null) {
                            Log.w("ImageWallpaper", "Unable to load wallpaper!", e);
                            try {
                                this.this$1.this$0.mWallpaperManager.clear();
                            } catch (IOException e2) {
                                Log.w("ImageWallpaper", "Unable reset to default wallpaper!", e2);
                            }
                            try {
                                return this.this$1.this$0.mWallpaperManager.getBitmap();
                            } catch (OutOfMemoryError | RuntimeException e3) {
                                Log.w("ImageWallpaper", "Unable to load default wallpaper!", e3);
                                return null;
                            }
                        }
                        return null;
                    }
                }

                /* JADX INFO: Access modifiers changed from: protected */
                @Override // android.os.AsyncTask
                public void onPostExecute(Bitmap bitmap) {
                    this.this$1.mBackground = null;
                    this.this$1.mBackgroundWidth = -1;
                    this.this$1.mBackgroundHeight = -1;
                    if (bitmap != null) {
                        this.this$1.mBackground = bitmap;
                        this.this$1.mBackgroundWidth = this.this$1.mBackground.getWidth();
                        this.this$1.mBackgroundHeight = this.this$1.mBackground.getHeight();
                    }
                    this.this$1.updateSurfaceSize(this.this$1.getSurfaceHolder(), this.this$1.getDefaultDisplayInfo(), false);
                    if (this.this$1.mNeedsDrawAfterLoadingWallpaper) {
                        this.this$1.drawFrame();
                    }
                    this.this$1.mLoader = null;
                    this.this$1.mNeedsDrawAfterLoadingWallpaper = false;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }

        EGLContext createContext(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig) {
            return egl10.eglCreateContext(eGLDisplay, eGLConfig, EGL10.EGL_NO_CONTEXT, new int[]{12440, 2, 12344});
        }

        void drawFrame() {
            boolean z;
            if (this.mSurfaceValid) {
                try {
                    Trace.traceBegin(8L, "drawWallpaper");
                    DisplayInfo defaultDisplayInfo = getDefaultDisplayInfo();
                    int i = defaultDisplayInfo.rotation;
                    if (i != this.mLastRotation || this.mDisplayWidthAtLastSurfaceSizeUpdate != defaultDisplayInfo.logicalWidth || this.mDisplayHeightAtLastSurfaceSizeUpdate != defaultDisplayInfo.logicalHeight) {
                        if (!updateSurfaceSize(getSurfaceHolder(), defaultDisplayInfo, true)) {
                            Trace.traceEnd(8L);
                            if (this.this$0.mIsHwAccelerated) {
                                return;
                            }
                            this.mBackground = null;
                            this.this$0.mWallpaperManager.forgetLoadedWallpaper();
                            return;
                        }
                        this.mRotationAtLastSurfaceSizeUpdate = i;
                        this.mDisplayWidthAtLastSurfaceSizeUpdate = defaultDisplayInfo.logicalWidth;
                        this.mDisplayHeightAtLastSurfaceSizeUpdate = defaultDisplayInfo.logicalHeight;
                    }
                    SurfaceHolder surfaceHolder = getSurfaceHolder();
                    Rect surfaceFrame = surfaceHolder.getSurfaceFrame();
                    int width = surfaceFrame.width();
                    int height = surfaceFrame.height();
                    boolean z2 = width == this.mLastSurfaceWidth ? height != this.mLastSurfaceHeight : true;
                    boolean z3 = z2 || i != this.mLastRotation;
                    if (!z3 && !this.mOffsetsChanged) {
                        if (z) {
                            return;
                        }
                        return;
                    }
                    this.mLastRotation = i;
                    if (this.mBackground == null) {
                        this.this$0.mWallpaperManager.forgetLoadedWallpaper();
                        loadWallpaper(true);
                        Trace.traceEnd(8L);
                        if (this.this$0.mIsHwAccelerated) {
                            return;
                        }
                        this.mBackground = null;
                        this.this$0.mWallpaperManager.forgetLoadedWallpaper();
                        return;
                    }
                    this.mScale = Math.max(1.0f, Math.max(width / this.mBackground.getWidth(), height / this.mBackground.getHeight()));
                    this.mScale = width / this.mBackground.getWidth();
                    int width2 = width - ((int) (this.mBackground.getWidth() * this.mScale));
                    int height2 = height - ((int) (this.mBackground.getHeight() * this.mScale));
                    int i2 = width2 / 2;
                    int i3 = height2 / 2;
                    int width3 = ((int) (width / this.mScale)) - this.mBackground.getWidth();
                    int height3 = ((int) (height / this.mScale)) - this.mBackground.getHeight();
                    int i4 = i2;
                    if (width3 < 0) {
                        i4 = i2 + ((int) ((width3 * (this.mXOffset - 0.5f) * this.mScale) + 0.5f));
                    }
                    int i5 = i3;
                    if (height3 < 0) {
                        i5 = i3 + ((int) ((height3 * (this.mYOffset - 0.5f) * this.mScale) + 0.5f));
                    }
                    this.mOffsetsChanged = false;
                    if (z2) {
                        this.mLastSurfaceWidth = width;
                        this.mLastSurfaceHeight = height;
                    }
                    if (!z3 && i4 == this.mLastXTranslation && i5 == this.mLastYTranslation) {
                        Trace.traceEnd(8L);
                        if (this.this$0.mIsHwAccelerated) {
                            return;
                        }
                        this.mBackground = null;
                        this.this$0.mWallpaperManager.forgetLoadedWallpaper();
                        return;
                    }
                    this.mLastXTranslation = i4;
                    this.mLastYTranslation = i5;
                    if (!this.this$0.mIsHwAccelerated) {
                        drawWallpaperWithCanvas(surfaceHolder, width2, height2, i4, i5);
                    } else if (!drawWallpaperWithOpenGL(surfaceHolder, width2, height2, i4, i5)) {
                        drawWallpaperWithCanvas(surfaceHolder, width2, height2, i4, i5);
                    }
                    Trace.traceEnd(8L);
                    if (this.this$0.mIsHwAccelerated) {
                        return;
                    }
                    this.mBackground = null;
                    this.this$0.mWallpaperManager.forgetLoadedWallpaper();
                } finally {
                    Trace.traceEnd(8L);
                    if (!this.this$0.mIsHwAccelerated) {
                        this.mBackground = null;
                        this.this$0.mWallpaperManager.forgetLoadedWallpaper();
                    }
                }
            }
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        protected void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
            super.dump(str, fileDescriptor, printWriter, strArr);
            printWriter.print(str);
            printWriter.println("ImageWallpaper.DrawableEngine:");
            printWriter.print(str);
            printWriter.print(" mBackground=");
            printWriter.print(this.mBackground);
            printWriter.print(" mBackgroundWidth=");
            printWriter.print(this.mBackgroundWidth);
            printWriter.print(" mBackgroundHeight=");
            printWriter.println(this.mBackgroundHeight);
            printWriter.print(str);
            printWriter.print(" mLastRotation=");
            printWriter.print(this.mLastRotation);
            printWriter.print(" mLastSurfaceWidth=");
            printWriter.print(this.mLastSurfaceWidth);
            printWriter.print(" mLastSurfaceHeight=");
            printWriter.println(this.mLastSurfaceHeight);
            printWriter.print(str);
            printWriter.print(" mXOffset=");
            printWriter.print(this.mXOffset);
            printWriter.print(" mYOffset=");
            printWriter.println(this.mYOffset);
            printWriter.print(str);
            printWriter.print(" mVisible=");
            printWriter.print(this.mVisible);
            printWriter.print(" mOffsetsChanged=");
            printWriter.println(this.mOffsetsChanged);
            printWriter.print(str);
            printWriter.print(" mLastXTranslation=");
            printWriter.print(this.mLastXTranslation);
            printWriter.print(" mLastYTranslation=");
            printWriter.print(this.mLastYTranslation);
            printWriter.print(" mScale=");
            printWriter.println(this.mScale);
            printWriter.print(str);
            printWriter.print(" mLastRequestedWidth=");
            printWriter.print(this.mLastRequestedWidth);
            printWriter.print(" mLastRequestedHeight=");
            printWriter.println(this.mLastRequestedHeight);
            printWriter.print(str);
            printWriter.println(" DisplayInfo at last updateSurfaceSize:");
            printWriter.print(str);
            printWriter.print("  rotation=");
            printWriter.print(this.mRotationAtLastSurfaceSizeUpdate);
            printWriter.print("  width=");
            printWriter.print(this.mDisplayWidthAtLastSurfaceSizeUpdate);
            printWriter.print("  height=");
            printWriter.println(this.mDisplayHeightAtLastSurfaceSizeUpdate);
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            this.mDefaultDisplay = ((WindowManager) this.this$0.getSystemService(WindowManager.class)).getDefaultDisplay();
            setOffsetNotificationsEnabled(false);
            if (this.this$0.mIstabletConfig && (this.mBackgroundWidth <= 0 || this.mBackgroundHeight <= 0)) {
                this.this$0.mWallpaperManager.forgetLoadedWallpaper();
                loadWallpaper(false);
            }
            updateSurfaceSize(surfaceHolder, getDefaultDisplayInfo(), false);
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onDestroy() {
            super.onDestroy();
            this.mBackground = null;
            if ("1".equals(SystemProperties.get("ro.mtk_gmo_ram_optimize", "0")) && this.mYv12Enhancement && Utils.useYv12 && this.mBuffer != null) {
                this.mBuffer.destroy();
                this.mBuffer = null;
            }
            this.this$0.mWallpaperManager.forgetLoadedWallpaper();
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onOffsetsChanged(float f, float f2, float f3, float f4, int i, int i2) {
            if (this.mXOffset != f || this.mYOffset != f2) {
                this.mXOffset = f;
                this.mYOffset = f2;
                this.mOffsetsChanged = true;
            }
            drawFrame();
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onSurfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
            super.onSurfaceChanged(surfaceHolder, i, i2, i3);
            drawFrame();
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
            super.onSurfaceCreated(surfaceHolder);
            this.mLastSurfaceHeight = -1;
            this.mLastSurfaceWidth = -1;
            this.mSurfaceValid = true;
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onSurfaceDestroyed(SurfaceHolder surfaceHolder) {
            super.onSurfaceDestroyed(surfaceHolder);
            this.mLastSurfaceHeight = -1;
            this.mLastSurfaceWidth = -1;
            this.mSurfaceValid = false;
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onSurfaceRedrawNeeded(SurfaceHolder surfaceHolder) {
            super.onSurfaceRedrawNeeded(surfaceHolder);
            drawFrame();
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onVisibilityChanged(boolean z) {
            if (this.mVisible != z) {
                this.mVisible = z;
                if (z) {
                    drawFrame();
                }
            }
        }

        public void trimMemory(int i) {
            if (i >= 10 && i <= 15) {
                if (this.mBackground != null) {
                    this.mBackground.recycle();
                    this.mBackground = null;
                }
                this.mBackgroundWidth = -1;
                this.mBackgroundHeight = -1;
                this.this$0.mWallpaperManager.forgetLoadedWallpaper();
            }
            if ("1".equals(SystemProperties.get("ro.mtk_gmo_ram_optimize", "0")) && this.mYv12Enhancement && Utils.useYv12 && this.mBuffer != null) {
                this.mBuffer.destroy();
                this.mBuffer = null;
            }
        }

        boolean updateSurfaceSize(SurfaceHolder surfaceHolder, DisplayInfo displayInfo, boolean z) {
            boolean z2 = true;
            if (this.mBackgroundWidth <= 0 || this.mBackgroundHeight <= 0) {
                this.this$0.mWallpaperManager.forgetLoadedWallpaper();
                loadWallpaper(z);
                z2 = false;
            }
            int max = Math.max(displayInfo.logicalWidth, this.mBackgroundWidth);
            int max2 = Math.max(displayInfo.logicalHeight, this.mBackgroundHeight);
            surfaceHolder.setFixedSize(max, max2);
            this.mLastRequestedWidth = max;
            this.mLastRequestedHeight = max2;
            return z2;
        }
    }

    private static boolean isEmulator() {
        return "1".equals(SystemProperties.get("ro.kernel.qemu", "0"));
    }

    @Override // android.service.wallpaper.WallpaperService, android.app.Service
    public void onCreate() {
        super.onCreate();
        this.mContext = getApplicationContext();
        this.mWallpaperManager = (WallpaperManager) getSystemService("wallpaper");
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.CONFIGURATION_CHANGED"));
        if (!isEmulator()) {
            this.mIsHwAccelerated = ActivityManager.isHighEndGfx();
        }
        this.mIstabletConfig = this.mContext.getResources().getBoolean(2131623968);
    }

    @Override // android.service.wallpaper.WallpaperService
    public WallpaperService.Engine onCreateEngine() {
        this.mEngine = new DrawableEngine(this);
        return this.mEngine;
    }

    @Override // android.service.wallpaper.WallpaperService, android.app.Service
    public void onDestroy() {
        super.onDestroy();
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    @Override // android.app.Service, android.content.ComponentCallbacks2
    public void onTrimMemory(int i) {
        if (this.mEngine != null) {
            this.mEngine.trimMemory(i);
        }
    }
}
