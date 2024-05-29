package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/FlashlightController.class */
public class FlashlightController {
    private static final boolean DEBUG = Log.isLoggable("FlashlightController", 3);
    private final String mCameraId;
    private final CameraManager mCameraManager;
    private boolean mFlashlightEnabled;
    private Handler mHandler;
    private boolean mTorchAvailable;
    private final ArrayList<WeakReference<FlashlightListener>> mListeners = new ArrayList<>(1);
    private final CameraManager.TorchCallback mTorchCallback = new CameraManager.TorchCallback(this) { // from class: com.android.systemui.statusbar.policy.FlashlightController.1
        final FlashlightController this$0;

        {
            this.this$0 = this;
        }

        private void setCameraAvailable(boolean z) {
            boolean z2;
            synchronized (this.this$0) {
                z2 = this.this$0.mTorchAvailable != z;
                this.this$0.mTorchAvailable = z;
            }
            if (z2) {
                if (FlashlightController.DEBUG) {
                    Log.d("FlashlightController", "dispatchAvailabilityChanged(" + z + ")");
                }
                this.this$0.dispatchAvailabilityChanged(z);
            }
        }

        private void setTorchMode(boolean z) {
            boolean z2;
            synchronized (this.this$0) {
                z2 = this.this$0.mFlashlightEnabled != z;
                this.this$0.mFlashlightEnabled = z;
            }
            if (z2) {
                if (FlashlightController.DEBUG) {
                    Log.d("FlashlightController", "dispatchModeChanged(" + z + ")");
                }
                this.this$0.dispatchModeChanged(z);
            }
        }

        @Override // android.hardware.camera2.CameraManager.TorchCallback
        public void onTorchModeChanged(String str, boolean z) {
            if (TextUtils.equals(str, this.this$0.mCameraId)) {
                setCameraAvailable(true);
                setTorchMode(z);
            }
        }

        @Override // android.hardware.camera2.CameraManager.TorchCallback
        public void onTorchModeUnavailable(String str) {
            if (TextUtils.equals(str, this.this$0.mCameraId)) {
                setCameraAvailable(false);
            }
        }
    };

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/FlashlightController$FlashlightListener.class */
    public interface FlashlightListener {
        void onFlashlightAvailabilityChanged(boolean z);

        void onFlashlightChanged(boolean z);

        void onFlashlightError();
    }

    public FlashlightController(Context context) {
        this.mCameraManager = (CameraManager) context.getSystemService("camera");
        try {
            this.mCameraId = getCameraId();
            if (this.mCameraId != null) {
                ensureHandler();
                this.mCameraManager.registerTorchCallback(this.mTorchCallback, this.mHandler);
            }
        } catch (Throwable th) {
            try {
                Log.e("FlashlightController", "Couldn't initialize.", th);
            } finally {
                this.mCameraId = null;
            }
        }
    }

    private void cleanUpListenersLocked(FlashlightListener flashlightListener) {
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            FlashlightListener flashlightListener2 = this.mListeners.get(size).get();
            if (flashlightListener2 == null || flashlightListener2 == flashlightListener) {
                this.mListeners.remove(size);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchAvailabilityChanged(boolean z) {
        dispatchListeners(2, z);
    }

    private void dispatchError() {
        dispatchListeners(1, false);
    }

    private void dispatchListeners(int i, boolean z) {
        boolean z2;
        synchronized (this.mListeners) {
            int size = this.mListeners.size();
            boolean z3 = false;
            int i2 = 0;
            while (i2 < size) {
                FlashlightListener flashlightListener = this.mListeners.get(i2).get();
                if (flashlightListener == null) {
                    z2 = true;
                } else if (i == 0) {
                    flashlightListener.onFlashlightError();
                    z2 = z3;
                } else if (i == 1) {
                    flashlightListener.onFlashlightChanged(z);
                    z2 = z3;
                } else {
                    z2 = z3;
                    if (i == 2) {
                        flashlightListener.onFlashlightAvailabilityChanged(z);
                        z2 = z3;
                    }
                }
                i2++;
                z3 = z2;
            }
            if (z3) {
                cleanUpListenersLocked(null);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchModeChanged(boolean z) {
        dispatchListeners(1, z);
    }

    private void ensureHandler() {
        synchronized (this) {
            if (this.mHandler == null) {
                HandlerThread handlerThread = new HandlerThread("FlashlightController", 10);
                handlerThread.start();
                this.mHandler = new Handler(handlerThread.getLooper());
            }
        }
    }

    private String getCameraId() throws CameraAccessException {
        String[] cameraIdList;
        for (String str : this.mCameraManager.getCameraIdList()) {
            CameraCharacteristics cameraCharacteristics = this.mCameraManager.getCameraCharacteristics(str);
            Boolean bool = (Boolean) cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer num = (Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
            if (bool != null && bool.booleanValue() && num != null && num.intValue() == 1) {
                return str;
            }
        }
        return null;
    }

    public void addListener(FlashlightListener flashlightListener) {
        synchronized (this.mListeners) {
            cleanUpListenersLocked(flashlightListener);
            this.mListeners.add(new WeakReference<>(flashlightListener));
        }
    }

    public boolean hasFlashlight() {
        return this.mCameraId != null;
    }

    public boolean isAvailable() {
        boolean z;
        synchronized (this) {
            z = this.mTorchAvailable;
        }
        return z;
    }

    public boolean isEnabled() {
        boolean z;
        synchronized (this) {
            z = this.mFlashlightEnabled;
        }
        return z;
    }

    public void removeListener(FlashlightListener flashlightListener) {
        synchronized (this.mListeners) {
            cleanUpListenersLocked(flashlightListener);
        }
    }

    public void setFlashlight(boolean z) {
        boolean z2;
        synchronized (this) {
            z2 = false;
            if (this.mFlashlightEnabled != z) {
                this.mFlashlightEnabled = z;
                try {
                    this.mCameraManager.setTorchMode(this.mCameraId, z);
                    z2 = false;
                } catch (CameraAccessException e) {
                    Log.e("FlashlightController", "Couldn't set torch mode", e);
                    this.mFlashlightEnabled = false;
                    z2 = true;
                }
            }
        }
        dispatchModeChanged(this.mFlashlightEnabled);
        if (z2) {
            dispatchError();
        }
    }
}
