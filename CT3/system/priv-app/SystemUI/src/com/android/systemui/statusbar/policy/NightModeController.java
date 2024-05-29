package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.Matrix;
import android.util.MathUtils;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/NightModeController.class */
public class NightModeController implements TunerService.Tunable {
    private boolean mAdjustTint;
    private float mAmount;
    private final Context mContext;
    private float[] mCustomMatrix;
    private boolean mIsAuto;
    private boolean mIsNight;
    private final ArrayList<Listener> mListeners;
    private boolean mListening;
    private final BroadcastReceiver mReceiver;
    private final boolean mUpdateMatrix;
    private static final float[] NIGHT_VALUES = {1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.754f, 0.0f, 0.0f, 0.0f, 0.0f, 0.516f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
    public static final float[] IDENTITY_MATRIX = {1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/NightModeController$Listener.class */
    public interface Listener {
        void onNightModeChanged();
    }

    public NightModeController(Context context) {
        this(context, false);
    }

    public NightModeController(Context context, boolean z) {
        this.mListeners = new ArrayList<>();
        this.mReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.policy.NightModeController.1
            final NightModeController this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                if ("android.intent.action.TWILIGHT_CHANGED".equals(intent.getAction())) {
                    this.this$0.updateNightMode(intent);
                    this.this$0.updateCurrentMatrix();
                    for (int i = 0; i < this.this$0.mListeners.size(); i++) {
                        ((Listener) this.this$0.mListeners.get(i)).onNightModeChanged();
                    }
                }
            }
        };
        this.mContext = context;
        this.mUpdateMatrix = z;
        TunerService.get(this.mContext).addTunable(this, "tuner_night_mode_adjust_tint", "tuner_color_custom_values", "twilight_mode");
    }

    private static float[] multiply(float[] fArr, float[] fArr2) {
        if (fArr == null) {
            return fArr2;
        }
        float[] fArr3 = new float[16];
        Matrix.multiplyMM(fArr3, 0, fArr, 0, fArr2, 0);
        return fArr3;
    }

    private float[] scaleValues(float[] fArr, float[] fArr2, float f) {
        float[] fArr3 = new float[fArr.length];
        for (int i = 0; i < fArr3.length; i++) {
            fArr3[i] = MathUtils.lerp(fArr[i], fArr2[i], f);
        }
        return fArr3;
    }

    public static String toString(float[] fArr) {
        StringBuilder sb = new StringBuilder();
        for (float f : fArr) {
            if (sb.length() != 0) {
                sb.append(',');
            }
            sb.append(f);
        }
        return sb.toString();
    }

    public static float[] toValues(String str) {
        String[] split = str.split(",");
        float[] fArr = new float[split.length];
        for (int i = 0; i < fArr.length; i++) {
            fArr[i] = Float.parseFloat(split[i]);
        }
        return fArr;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateCurrentMatrix() {
        float f = 0.0f;
        if (this.mUpdateMatrix) {
            if ((!this.mAdjustTint || this.mAmount == 0.0f) && this.mCustomMatrix == null) {
                TunerService.get(this.mContext).setValue("accessibility_display_color_matrix", (String) null);
                return;
            }
            float[] fArr = IDENTITY_MATRIX;
            float[] fArr2 = NIGHT_VALUES;
            if (this.mAdjustTint) {
                f = this.mAmount;
            }
            float[] scaleValues = scaleValues(fArr, fArr2, f);
            float[] fArr3 = scaleValues;
            if (this.mCustomMatrix != null) {
                fArr3 = multiply(scaleValues, this.mCustomMatrix);
            }
            TunerService.get(this.mContext).setValue("accessibility_display_color_matrix", toString(fArr3));
        }
    }

    private void updateListening() {
        boolean z = this.mListeners.size() == 0 ? this.mUpdateMatrix ? this.mAdjustTint : false : true;
        if (z == this.mListening) {
            return;
        }
        this.mListening = z;
        if (this.mListening) {
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.TWILIGHT_CHANGED"));
        } else {
            this.mContext.unregisterReceiver(this.mReceiver);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateNightMode(Intent intent) {
        boolean z = false;
        if (intent != null) {
            z = intent.getBooleanExtra("isNight", false);
        }
        this.mIsNight = z;
        this.mAmount = intent != null ? intent.getFloatExtra("amount", 0.0f) : 0.0f;
    }

    public void addListener(Listener listener) {
        this.mListeners.add(listener);
        listener.onNightModeChanged();
        updateListening();
    }

    public String getCustomValues() {
        return TunerService.get(this.mContext).getValue("tuner_color_custom_values");
    }

    public boolean isEnabled() {
        if (!this.mListening) {
            updateNightMode(this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.TWILIGHT_CHANGED")));
        }
        return this.mIsNight;
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("tuner_color_custom_values".equals(str)) {
            float[] fArr = null;
            if (str2 != null) {
                fArr = toValues(str2);
            }
            this.mCustomMatrix = fArr;
            updateCurrentMatrix();
        } else if ("tuner_night_mode_adjust_tint".equals(str)) {
            this.mAdjustTint = str2 == null || Integer.parseInt(str2) != 0;
            updateListening();
            updateCurrentMatrix();
        } else if ("twilight_mode".equals(str)) {
            boolean z = false;
            if (str2 != null) {
                z = false;
                if (Integer.parseInt(str2) >= 2) {
                    z = true;
                }
            }
            this.mIsAuto = z;
        }
    }

    public void removeListener(Listener listener) {
        this.mListeners.remove(listener);
        updateListening();
    }

    public void setAdjustTint(Boolean bool) {
        TunerService.get(this.mContext).setValue("tuner_night_mode_adjust_tint", bool.booleanValue() ? 1 : 0);
    }

    public void setAuto(boolean z) {
        this.mIsAuto = z;
        if (z) {
            TunerService.get(this.mContext).setValue("twilight_mode", 2);
        } else {
            TunerService.get(this.mContext).setValue("twilight_mode", this.mIsNight ? 1 : 0);
        }
    }

    public void setCustomValues(String str) {
        TunerService.get(this.mContext).setValue("tuner_color_custom_values", str);
    }

    public void setNightMode(boolean z) {
        if (!this.mIsAuto) {
            TunerService.get(this.mContext).setValue("twilight_mode", z ? 1 : 0);
        } else if (this.mIsNight != z) {
            TunerService.get(this.mContext).setValue("twilight_mode", z ? 4 : 3);
        } else {
            TunerService.get(this.mContext).setValue("twilight_mode", 2);
        }
    }
}
