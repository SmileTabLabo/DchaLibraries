package com.android.systemui.volume;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioSystem;
import android.os.BenesseExtension;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.provider.Settings;
import android.text.InputFilter;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settingslib.Utils;
import com.android.systemui.Dependency;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.VolumeDialog;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.volume.SystemUIInterpolators;
import com.android.systemui.volume.VolumeDialogImpl;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/* loaded from: classes.dex */
public class VolumeDialogImpl implements VolumeDialog {
    private static final String TAG = Util.logTag(VolumeDialogImpl.class);
    private int mActiveAlpha;
    private int mActiveStream;
    private ColorStateList mActiveTint;
    private ConfigurableTexts mConfigurableTexts;
    private final Context mContext;
    private CustomDialog mDialog;
    private ViewGroup mDialogRowsView;
    private ViewGroup mDialogView;
    private int mInactiveAlpha;
    private ColorStateList mInactiveTint;
    private final KeyguardManager mKeyguard;
    private int mPrevActiveStream;
    private ViewGroup mRinger;
    private ImageButton mRingerIcon;
    private SafetyWarningDialog mSafetyWarning;
    private ImageButton mSettingsIcon;
    private View mSettingsView;
    private boolean mShowA11yStream;
    private boolean mShowing;
    private VolumeDialogController.State mState;
    private Window mWindow;
    private FrameLayout mZenIcon;
    private final H mHandler = new H();
    private final List<VolumeRow> mRows = new ArrayList();
    private final SparseBooleanArray mDynamic = new SparseBooleanArray();
    private final Object mSafetyWarningLock = new Object();
    private final Accessibility mAccessibility = new Accessibility();
    private boolean mAutomute = true;
    private boolean mSilentMode = true;
    private boolean mHovering = false;
    private boolean mConfigChanged = false;
    private Runnable mSinglePress = new Runnable() { // from class: com.android.systemui.volume.VolumeDialogImpl.3
        @Override // java.lang.Runnable
        public void run() {
            VolumeDialogImpl.this.mRingerIcon.setPressed(true);
            VolumeDialogImpl.this.mRingerIcon.postOnAnimationDelayed(VolumeDialogImpl.this.mSingleUnpress, 200L);
        }
    };
    private Runnable mSingleUnpress = new Runnable() { // from class: com.android.systemui.volume.VolumeDialogImpl.4
        @Override // java.lang.Runnable
        public void run() {
            VolumeDialogImpl.this.mRingerIcon.setPressed(false);
        }
    };
    private final VolumeDialogController.Callbacks mControllerCallbackH = new VolumeDialogController.Callbacks() { // from class: com.android.systemui.volume.VolumeDialogImpl.5
        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowRequested(int i) {
            VolumeDialogImpl.this.showH(i);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onDismissRequested(int i) {
            VolumeDialogImpl.this.dismissH(i);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onScreenOff() {
            VolumeDialogImpl.this.dismissH(4);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onStateChanged(VolumeDialogController.State state) {
            VolumeDialogImpl.this.onStateChangedH(state);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onLayoutDirectionChanged(int i) {
            VolumeDialogImpl.this.mDialogView.setLayoutDirection(i);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onConfigurationChanged() {
            VolumeDialogImpl.this.mDialog.dismiss();
            VolumeDialogImpl.this.mConfigChanged = true;
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowVibrateHint() {
            if (VolumeDialogImpl.this.mSilentMode) {
                VolumeDialogImpl.this.mController.setRingerMode(0, false);
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowSilentHint() {
            if (VolumeDialogImpl.this.mSilentMode) {
                VolumeDialogImpl.this.mController.setRingerMode(2, false);
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowSafetyWarning(int i) {
            VolumeDialogImpl.this.showSafetyWarningH(i);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onAccessibilityModeChanged(Boolean bool) {
            VolumeDialogImpl.this.mShowA11yStream = bool == null ? false : bool.booleanValue();
            VolumeRow activeRow = VolumeDialogImpl.this.getActiveRow();
            if (VolumeDialogImpl.this.mShowA11yStream || 10 != activeRow.stream) {
                VolumeDialogImpl.this.updateRowsH(activeRow);
            } else {
                VolumeDialogImpl.this.dismissH(7);
            }
        }
    };
    private final VolumeDialogController mController = (VolumeDialogController) Dependency.get(VolumeDialogController.class);
    private final AccessibilityManagerWrapper mAccessibilityMgr = (AccessibilityManagerWrapper) Dependency.get(AccessibilityManagerWrapper.class);
    private final DeviceProvisionedController mDeviceProvisionedController = (DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class);

    public VolumeDialogImpl(Context context) {
        this.mContext = new ContextThemeWrapper(context, (int) R.style.qs_theme);
        this.mKeyguard = (KeyguardManager) this.mContext.getSystemService("keyguard");
    }

    @Override // com.android.systemui.plugins.VolumeDialog
    public void init(int i, VolumeDialog.Callback callback) {
        initDialog();
        this.mAccessibility.init();
        this.mController.addCallback(this.mControllerCallbackH, this.mHandler);
        this.mController.getState();
    }

    @Override // com.android.systemui.plugins.VolumeDialog
    public void destroy() {
        this.mAccessibility.destroy();
        this.mController.removeCallback(this.mControllerCallbackH);
        this.mHandler.removeCallbacksAndMessages(null);
    }

    private void initDialog() {
        this.mDialog = new CustomDialog(this.mContext);
        this.mConfigurableTexts = new ConfigurableTexts(this.mContext);
        this.mHovering = false;
        this.mShowing = false;
        this.mWindow = this.mDialog.getWindow();
        this.mWindow.requestFeature(1);
        this.mWindow.setBackgroundDrawable(new ColorDrawable(0));
        this.mWindow.clearFlags(65538);
        this.mWindow.addFlags(17563944);
        this.mWindow.setType(2020);
        this.mWindow.setWindowAnimations(16973828);
        WindowManager.LayoutParams attributes = this.mWindow.getAttributes();
        attributes.format = -3;
        attributes.setTitle(VolumeDialogImpl.class.getSimpleName());
        attributes.gravity = 21;
        attributes.windowAnimations = -1;
        this.mWindow.setAttributes(attributes);
        this.mWindow.setLayout(-2, -2);
        this.mDialog.setCanceledOnTouchOutside(true);
        this.mDialog.setContentView(R.layout.volume_dialog);
        this.mDialog.setOnShowListener(new DialogInterface.OnShowListener() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$8BZhTIdOE2rPYfFa5HbcUDCtXeM
            @Override // android.content.DialogInterface.OnShowListener
            public final void onShow(DialogInterface dialogInterface) {
                VolumeDialogImpl.lambda$initDialog$1(VolumeDialogImpl.this, dialogInterface);
            }
        });
        this.mDialogView = (ViewGroup) this.mDialog.findViewById(R.id.volume_dialog);
        this.mDialogView.setOnHoverListener(new View.OnHoverListener() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$T52d0W13mYvykk6ORgbytqfZsps
            @Override // android.view.View.OnHoverListener
            public final boolean onHover(View view, MotionEvent motionEvent) {
                return VolumeDialogImpl.lambda$initDialog$2(VolumeDialogImpl.this, view, motionEvent);
            }
        });
        this.mActiveTint = ColorStateList.valueOf(Utils.getColorAccent(this.mContext));
        this.mActiveAlpha = Color.alpha(this.mActiveTint.getDefaultColor());
        this.mInactiveTint = ColorStateList.valueOf(Utils.getColorAttr(this.mContext, 16842800));
        this.mInactiveAlpha = getAlphaAttr(16844115);
        this.mDialogRowsView = (ViewGroup) this.mDialog.findViewById(R.id.volume_dialog_rows);
        this.mRinger = (ViewGroup) this.mDialog.findViewById(R.id.ringer);
        this.mRingerIcon = (ImageButton) this.mRinger.findViewById(R.id.ringer_icon);
        this.mZenIcon = (FrameLayout) this.mRinger.findViewById(R.id.dnd_icon);
        this.mSettingsView = this.mDialog.findViewById(R.id.settings_container);
        this.mSettingsIcon = (ImageButton) this.mDialog.findViewById(R.id.settings);
        if (this.mRows.isEmpty()) {
            if (!AudioSystem.isSingleVolume(this.mContext)) {
                addRow(10, R.drawable.ic_volume_accessibility, R.drawable.ic_volume_accessibility, true, false);
            }
            addRow(3, R.drawable.ic_volume_media, R.drawable.ic_volume_media_mute, true, true);
            if (!AudioSystem.isSingleVolume(this.mContext)) {
                addRow(2, R.drawable.ic_volume_ringer, R.drawable.ic_volume_ringer_mute, true, false);
                addRow(4, R.drawable.ic_volume_alarm, R.drawable.ic_volume_alarm_mute, true, false);
                addRow(0, R.drawable.ic_volume_voice, R.drawable.ic_volume_voice, false, false);
                addRow(6, R.drawable.ic_volume_bt_sco, R.drawable.ic_volume_bt_sco, false, false);
                addRow(1, R.drawable.ic_volume_system, R.drawable.ic_volume_system_mute, false, false);
            }
        } else {
            addExistingRows();
        }
        updateRowsH(getActiveRow());
        initRingerH();
        initSettingsH();
    }

    public static /* synthetic */ void lambda$initDialog$1(final VolumeDialogImpl volumeDialogImpl, DialogInterface dialogInterface) {
        if (!volumeDialogImpl.isLandscape()) {
            volumeDialogImpl.mDialogView.setTranslationX(volumeDialogImpl.mDialogView.getWidth() / 2);
        }
        volumeDialogImpl.mDialogView.setAlpha(0.0f);
        volumeDialogImpl.mDialogView.animate().alpha(1.0f).translationX(0.0f).setDuration(300L).setInterpolator(new SystemUIInterpolators.LogDecelerateInterpolator()).withEndAction(new Runnable() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$vBH_Cy2LsLvfluWDg0W4IzJ1dm8
            @Override // java.lang.Runnable
            public final void run() {
                VolumeDialogImpl.lambda$initDialog$0(VolumeDialogImpl.this);
            }
        }).start();
    }

    public static /* synthetic */ void lambda$initDialog$0(VolumeDialogImpl volumeDialogImpl) {
        if (!Prefs.getBoolean(volumeDialogImpl.mContext, "TouchedRingerToggle", false)) {
            volumeDialogImpl.mRingerIcon.postOnAnimationDelayed(volumeDialogImpl.mSinglePress, 1500L);
        }
    }

    public static /* synthetic */ boolean lambda$initDialog$2(VolumeDialogImpl volumeDialogImpl, View view, MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        volumeDialogImpl.mHovering = actionMasked == 9 || actionMasked == 7;
        volumeDialogImpl.rescheduleTimeoutH();
        return true;
    }

    private int getAlphaAttr(int i) {
        TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(new int[]{i});
        float f = obtainStyledAttributes.getFloat(0, 0.0f);
        obtainStyledAttributes.recycle();
        return (int) (f * 255.0f);
    }

    private boolean isLandscape() {
        return this.mContext.getResources().getConfiguration().orientation == 2;
    }

    public void setStreamImportant(int i, boolean z) {
        this.mHandler.obtainMessage(5, i, z ? 1 : 0).sendToTarget();
    }

    public void setAutomute(boolean z) {
        if (this.mAutomute == z) {
            return;
        }
        this.mAutomute = z;
        this.mHandler.sendEmptyMessage(4);
    }

    public void setSilentMode(boolean z) {
        if (this.mSilentMode == z) {
            return;
        }
        this.mSilentMode = z;
        this.mHandler.sendEmptyMessage(4);
    }

    private void addRow(int i, int i2, int i3, boolean z, boolean z2) {
        addRow(i, i2, i3, z, z2, false);
    }

    private void addRow(int i, int i2, int i3, boolean z, boolean z2, boolean z3) {
        if (D.BUG) {
            String str = TAG;
            Slog.d(str, "Adding row for stream " + i);
        }
        VolumeRow volumeRow = new VolumeRow();
        initRow(volumeRow, i, i2, i3, z, z2);
        this.mDialogRowsView.addView(volumeRow.view);
        this.mRows.add(volumeRow);
    }

    private void addExistingRows() {
        int size = this.mRows.size();
        for (int i = 0; i < size; i++) {
            VolumeRow volumeRow = this.mRows.get(i);
            initRow(volumeRow, volumeRow.stream, volumeRow.iconRes, volumeRow.iconMuteRes, volumeRow.important, volumeRow.defaultStream);
            this.mDialogRowsView.addView(volumeRow.view);
            updateVolumeRowH(volumeRow);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public VolumeRow getActiveRow() {
        for (VolumeRow volumeRow : this.mRows) {
            if (volumeRow.stream == this.mActiveStream) {
                return volumeRow;
            }
        }
        for (VolumeRow volumeRow2 : this.mRows) {
            if (volumeRow2.stream == 3) {
                return volumeRow2;
            }
        }
        return this.mRows.get(0);
    }

    private VolumeRow findRow(int i) {
        for (VolumeRow volumeRow : this.mRows) {
            if (volumeRow.stream == i) {
                return volumeRow;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int getImpliedLevel(SeekBar seekBar, int i) {
        int max = seekBar.getMax();
        int i2 = max / 100;
        int i3 = i2 - 1;
        if (i == 0) {
            return 0;
        }
        return i == max ? i2 : 1 + ((int) ((i / max) * i3));
    }

    @SuppressLint({"InflateParams"})
    private void initRow(final VolumeRow volumeRow, final int i, int i2, int i3, boolean z, boolean z2) {
        volumeRow.stream = i;
        volumeRow.iconRes = i2;
        volumeRow.iconMuteRes = i3;
        volumeRow.important = z;
        volumeRow.defaultStream = z2;
        volumeRow.view = this.mDialog.getLayoutInflater().inflate(R.layout.volume_dialog_row, (ViewGroup) null);
        volumeRow.view.setId(volumeRow.stream);
        volumeRow.view.setTag(volumeRow);
        volumeRow.header = (TextView) volumeRow.view.findViewById(R.id.volume_row_header);
        volumeRow.header.setId(20 * volumeRow.stream);
        if (i == 10) {
            volumeRow.header.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        }
        volumeRow.dndIcon = (FrameLayout) volumeRow.view.findViewById(R.id.dnd_icon);
        volumeRow.slider = (SeekBar) volumeRow.view.findViewById(R.id.volume_row_slider);
        volumeRow.slider.setOnSeekBarChangeListener(new VolumeSeekBarChangeListener(volumeRow));
        volumeRow.anim = null;
        volumeRow.icon = (ImageButton) volumeRow.view.findViewById(R.id.volume_row_icon);
        volumeRow.icon.setImageResource(i2);
        if (volumeRow.stream != 10) {
            volumeRow.icon.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$I-0sumSTzcnKtt5xn4YVlQQget8
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    VolumeDialogImpl.lambda$initRow$3(VolumeDialogImpl.this, volumeRow, i, view);
                }
            });
        } else {
            volumeRow.icon.setImportantForAccessibility(2);
        }
    }

    public static /* synthetic */ void lambda$initRow$3(VolumeDialogImpl volumeDialogImpl, VolumeRow volumeRow, int i, View view) {
        Events.writeEvent(volumeDialogImpl.mContext, 7, Integer.valueOf(volumeRow.stream), Integer.valueOf(volumeRow.iconState));
        volumeDialogImpl.mController.setActiveStream(volumeRow.stream);
        if (volumeRow.stream == 2) {
            boolean hasVibrator = volumeDialogImpl.mController.hasVibrator();
            if (volumeDialogImpl.mState.ringerModeInternal == 2) {
                if (hasVibrator) {
                    volumeDialogImpl.mController.setRingerMode(1, false);
                } else {
                    volumeDialogImpl.mController.setStreamVolume(i, volumeRow.ss.level == 0 ? volumeRow.lastAudibleLevel : 0);
                }
            } else {
                volumeDialogImpl.mController.setRingerMode(2, false);
                if (volumeRow.ss.level == 0) {
                    volumeDialogImpl.mController.setStreamVolume(i, 1);
                }
            }
        } else {
            volumeDialogImpl.mController.setStreamVolume(i, (volumeRow.ss.level == volumeRow.ss.levelMin ? 1 : 0) != 0 ? volumeRow.lastAudibleLevel : volumeRow.ss.levelMin);
        }
        volumeRow.userAttempt = 0L;
    }

    public void initSettingsH() {
        this.mSettingsView.setVisibility((this.mDeviceProvisionedController.isCurrentUserSetup() && BenesseExtension.getDchaState() == 0) ? 0 : 8);
        this.mSettingsIcon.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$7RdQKc1FND8ZrjtxSEsHEKXSyeY
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                VolumeDialogImpl.lambda$initSettingsH$4(VolumeDialogImpl.this, view);
            }
        });
    }

    public static /* synthetic */ void lambda$initSettingsH$4(VolumeDialogImpl volumeDialogImpl, View view) {
        Events.writeEvent(volumeDialogImpl.mContext, 8, new Object[0]);
        Intent intent = new Intent("android.settings.SOUND_SETTINGS");
        intent.setFlags(268435456);
        volumeDialogImpl.dismissH(5);
        ((ActivityStarter) Dependency.get(ActivityStarter.class)).startActivity(intent, true);
    }

    public void initRingerH() {
        this.mRingerIcon.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$leUR0c6hrY1TNx5XUG-xhXI1EHk
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                VolumeDialogImpl.lambda$initRingerH$5(VolumeDialogImpl.this, view);
            }
        });
        updateRingerH();
    }

    public static /* synthetic */ void lambda$initRingerH$5(VolumeDialogImpl volumeDialogImpl, View view) {
        Prefs.putBoolean(volumeDialogImpl.mContext, "TouchedRingerToggle", true);
        int i = 2;
        VolumeDialogController.StreamState streamState = volumeDialogImpl.mState.states.get(2);
        if (streamState == null) {
            return;
        }
        boolean hasVibrator = volumeDialogImpl.mController.hasVibrator();
        if (volumeDialogImpl.mState.ringerModeInternal == 2) {
            if (hasVibrator) {
                i = 1;
            }
            i = 0;
        } else {
            if (volumeDialogImpl.mState.ringerModeInternal != 1) {
                if (streamState.level == 0) {
                    volumeDialogImpl.mController.setStreamVolume(2, 1);
                }
            }
            i = 0;
        }
        Events.writeEvent(volumeDialogImpl.mContext, 18, Integer.valueOf(i));
        volumeDialogImpl.incrementManualToggleCount();
        volumeDialogImpl.updateRingerH();
        volumeDialogImpl.provideTouchFeedbackH(i);
        volumeDialogImpl.mController.setRingerMode(i, false);
        volumeDialogImpl.maybeShowToastH(i);
    }

    private void incrementManualToggleCount() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.Secure.putInt(contentResolver, "manual_ringer_toggle_count", Settings.Secure.getInt(contentResolver, "manual_ringer_toggle_count", 0) + 1);
    }

    private void provideTouchFeedbackH(int i) {
        VibrationEffect vibrationEffect;
        if (i == 0) {
            vibrationEffect = VibrationEffect.get(0);
        } else if (i == 2) {
            this.mController.scheduleTouchFeedback();
            vibrationEffect = null;
        } else {
            vibrationEffect = VibrationEffect.get(1);
        }
        if (vibrationEffect != null) {
            this.mController.vibrate(vibrationEffect);
        }
    }

    private void maybeShowToastH(int i) {
        int i2 = Prefs.getInt(this.mContext, "RingerGuidanceCount", 0);
        if (i2 > 12) {
            return;
        }
        String str = null;
        if (i == 0) {
            str = this.mContext.getString(17041033);
        } else if (i == 2) {
            VolumeDialogController.StreamState streamState = this.mState.states.get(2);
            if (streamState != null) {
                str = this.mContext.getString(R.string.volume_dialog_ringer_guidance_ring, Utils.formatPercentage(streamState.level, streamState.levelMax));
            }
        } else {
            str = this.mContext.getString(17041034);
        }
        Toast.makeText(this.mContext, str, 0).show();
        Prefs.putInt(this.mContext, "RingerGuidanceCount", i2 + 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showH(int i) {
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "showH r=" + Events.DISMISS_REASONS[i]);
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        rescheduleTimeoutH();
        this.mShowing = true;
        if (this.mConfigChanged) {
            initDialog();
            this.mConfigurableTexts.update();
            this.mConfigChanged = false;
        }
        initSettingsH();
        this.mDialog.show();
        Events.writeEvent(this.mContext, 0, Integer.valueOf(i), Boolean.valueOf(this.mKeyguard.isKeyguardLocked()));
        this.mController.notifyVisible(true);
    }

    protected void rescheduleTimeoutH() {
        this.mHandler.removeMessages(2);
        int computeTimeoutH = computeTimeoutH();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, 3, 0), computeTimeoutH);
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "rescheduleTimeout " + computeTimeoutH + " " + Debug.getCaller());
        }
        this.mController.userActivity();
    }

    private int computeTimeoutH() {
        if (this.mAccessibility.mFeedbackEnabled) {
            return 20000;
        }
        if (this.mHovering) {
            return 16000;
        }
        return this.mSafetyWarning != null ? 5000 : 3000;
    }

    protected void dismissH(int i) {
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(1);
        this.mDialogView.animate().cancel();
        this.mShowing = false;
        this.mDialogView.setTranslationX(0.0f);
        this.mDialogView.setAlpha(1.0f);
        ViewPropertyAnimator withEndAction = this.mDialogView.animate().alpha(0.0f).setDuration(250L).setInterpolator(new SystemUIInterpolators.LogAccelerateInterpolator()).withEndAction(new Runnable() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$UIyOXqV9FMC4LLCneqMCAKo8fVY
            @Override // java.lang.Runnable
            public final void run() {
                r0.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$TeBm8mGAiDkt4iXRVJq9pjcoyzA
                    @Override // java.lang.Runnable
                    public final void run() {
                        VolumeDialogImpl.lambda$dismissH$6(VolumeDialogImpl.this);
                    }
                }, 50L);
            }
        });
        if (!isLandscape()) {
            withEndAction.translationX(this.mDialogView.getWidth() / 2);
        }
        withEndAction.start();
        Events.writeEvent(this.mContext, 1, Integer.valueOf(i));
        this.mController.notifyVisible(false);
        synchronized (this.mSafetyWarningLock) {
            if (this.mSafetyWarning != null) {
                if (D.BUG) {
                    Log.d(TAG, "SafetyWarning dismissed");
                }
                this.mSafetyWarning.dismiss();
            }
        }
    }

    public static /* synthetic */ void lambda$dismissH$6(VolumeDialogImpl volumeDialogImpl) {
        if (D.BUG) {
            Log.d(TAG, "mDialog.dismiss()");
        }
        volumeDialogImpl.mDialog.dismiss();
    }

    private boolean shouldBeVisibleH(VolumeRow volumeRow, VolumeRow volumeRow2) {
        boolean z = volumeRow.stream == volumeRow2.stream;
        if (volumeRow.stream == 10) {
            return this.mShowA11yStream;
        }
        if ((volumeRow2.stream == 10 && volumeRow.stream == this.mPrevActiveStream) || z) {
            return true;
        }
        if (volumeRow.defaultStream) {
            return volumeRow2.stream == 2 || volumeRow2.stream == 4 || volumeRow2.stream == 0 || volumeRow2.stream == 10 || this.mDynamic.get(volumeRow2.stream);
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateRowsH(VolumeRow volumeRow) {
        if (D.BUG) {
            Log.d(TAG, "updateRowsH");
        }
        if (!this.mShowing) {
            trimObsoleteH();
        }
        Iterator<VolumeRow> it = this.mRows.iterator();
        while (it.hasNext()) {
            VolumeRow next = it.next();
            boolean z = next == volumeRow;
            Util.setVisOrGone(next.view, shouldBeVisibleH(next, volumeRow));
            if (next.view.isShown()) {
                updateVolumeRowTintH(next, z);
            }
        }
    }

    protected void updateRingerH() {
        VolumeDialogController.StreamState streamState;
        if (this.mState == null || (streamState = this.mState.states.get(2)) == null) {
            return;
        }
        boolean z = this.mState.zenMode == 3 || this.mState.zenMode == 2 || (this.mState.zenMode == 1 && this.mState.disallowRinger);
        enableRingerViewsH(!z);
        switch (this.mState.ringerModeInternal) {
            case 0:
                this.mRingerIcon.setImageResource(R.drawable.ic_volume_ringer_mute);
                this.mRingerIcon.setTag(2);
                addAccessibilityDescription(this.mRingerIcon, 0, this.mContext.getString(R.string.volume_ringer_hint_unmute));
                return;
            case 1:
                this.mRingerIcon.setImageResource(R.drawable.ic_volume_ringer_vibrate);
                addAccessibilityDescription(this.mRingerIcon, 1, this.mContext.getString(R.string.volume_ringer_hint_mute));
                this.mRingerIcon.setTag(3);
                return;
            default:
                boolean z2 = (this.mAutomute && streamState.level == 0) || streamState.muted;
                if (!z && z2) {
                    this.mRingerIcon.setImageResource(R.drawable.ic_volume_ringer_mute);
                    addAccessibilityDescription(this.mRingerIcon, 2, this.mContext.getString(R.string.volume_ringer_hint_unmute));
                    this.mRingerIcon.setTag(2);
                    return;
                }
                this.mRingerIcon.setImageResource(R.drawable.ic_volume_ringer);
                if (this.mController.hasVibrator()) {
                    addAccessibilityDescription(this.mRingerIcon, 2, this.mContext.getString(R.string.volume_ringer_hint_vibrate));
                } else {
                    addAccessibilityDescription(this.mRingerIcon, 2, this.mContext.getString(R.string.volume_ringer_hint_mute));
                }
                this.mRingerIcon.setTag(1);
                return;
        }
    }

    private void addAccessibilityDescription(View view, int i, final String str) {
        int i2;
        switch (i) {
            case 0:
                i2 = R.string.volume_ringer_status_silent;
                break;
            case 1:
                i2 = R.string.volume_ringer_status_vibrate;
                break;
            default:
                i2 = R.string.volume_ringer_status_normal;
                break;
        }
        view.setContentDescription(this.mContext.getString(i2));
        view.setAccessibilityDelegate(new View.AccessibilityDelegate() { // from class: com.android.systemui.volume.VolumeDialogImpl.1
            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view2, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view2, accessibilityNodeInfo);
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, str));
            }
        });
    }

    private void enableVolumeRowViewsH(VolumeRow volumeRow, boolean z) {
        volumeRow.dndIcon.setVisibility(z ^ true ? 0 : 8);
    }

    private void enableRingerViewsH(boolean z) {
        this.mRingerIcon.setEnabled(z);
        this.mZenIcon.setVisibility(z ? 8 : 0);
    }

    private void trimObsoleteH() {
        if (D.BUG) {
            Log.d(TAG, "trimObsoleteH");
        }
        for (int size = this.mRows.size() - 1; size >= 0; size--) {
            VolumeRow volumeRow = this.mRows.get(size);
            if (volumeRow.ss != null && volumeRow.ss.dynamic && !this.mDynamic.get(volumeRow.stream)) {
                this.mRows.remove(size);
                this.mDialogRowsView.removeView(volumeRow.view);
            }
        }
    }

    protected void onStateChangedH(VolumeDialogController.State state) {
        if (this.mState != null && state != null && this.mState.ringerModeInternal != state.ringerModeInternal && state.ringerModeInternal == 1) {
            this.mController.vibrate(VibrationEffect.get(5));
        }
        this.mState = state;
        this.mDynamic.clear();
        for (int i = 0; i < state.states.size(); i++) {
            int keyAt = state.states.keyAt(i);
            if (state.states.valueAt(i).dynamic) {
                this.mDynamic.put(keyAt, true);
                if (findRow(keyAt) == null) {
                    addRow(keyAt, R.drawable.ic_volume_remote, R.drawable.ic_volume_remote_mute, true, false, true);
                }
            }
        }
        if (this.mActiveStream != state.activeStream) {
            this.mPrevActiveStream = this.mActiveStream;
            this.mActiveStream = state.activeStream;
            updateRowsH(getActiveRow());
            rescheduleTimeoutH();
        }
        for (VolumeRow volumeRow : this.mRows) {
            updateVolumeRowH(volumeRow);
        }
        updateRingerH();
        this.mWindow.setTitle(composeWindowTitle());
    }

    CharSequence composeWindowTitle() {
        return this.mContext.getString(R.string.volume_dialog_title, getStreamLabelH(getActiveRow().ss));
    }

    private void updateVolumeRowH(VolumeRow volumeRow) {
        VolumeDialogController.StreamState streamState;
        if (D.BUG) {
            Log.d(TAG, "updateVolumeRowH s=" + volumeRow.stream);
        }
        if (this.mState == null || (streamState = this.mState.states.get(volumeRow.stream)) == null) {
            return;
        }
        volumeRow.ss = streamState;
        if (streamState.level > 0) {
            volumeRow.lastAudibleLevel = streamState.level;
        }
        if (streamState.level == volumeRow.requestedLevel) {
            volumeRow.requestedLevel = -1;
        }
        int i = 0;
        boolean z = volumeRow.stream == 10;
        int i2 = 2;
        boolean z2 = volumeRow.stream == 2;
        boolean z3 = volumeRow.stream == 1;
        boolean z4 = volumeRow.stream == 4;
        boolean z5 = volumeRow.stream == 3;
        boolean z6 = z2 && this.mState.ringerModeInternal == 1;
        boolean z7 = z2 && this.mState.ringerModeInternal == 0;
        boolean z8 = !(this.mState.zenMode == 3) ? !(this.mState.zenMode == 2) ? (this.mState.zenMode == 1) && ((z4 && this.mState.disallowAlarms) || ((z5 && this.mState.disallowMedia) || ((z2 && this.mState.disallowRinger) || (z3 && this.mState.disallowSystem)))) : z2 || z3 || z4 || z5 : !(z2 || z3);
        int i3 = streamState.levelMax * 100;
        if (i3 != volumeRow.slider.getMax()) {
            volumeRow.slider.setMax(i3);
        }
        int i4 = streamState.levelMin * 100;
        if (i4 != volumeRow.slider.getMin()) {
            volumeRow.slider.setMin(i4);
        }
        Util.setText(volumeRow.header, getStreamLabelH(streamState));
        volumeRow.slider.setContentDescription(volumeRow.header.getText());
        this.mConfigurableTexts.add(volumeRow.header, streamState.name);
        boolean z9 = (this.mAutomute || streamState.muteSupported) && !z8;
        volumeRow.icon.setEnabled(z9);
        volumeRow.icon.setAlpha(z9 ? 1.0f : 0.5f);
        int i5 = z6 ? R.drawable.ic_volume_ringer_vibrate : (z7 || z8) ? volumeRow.iconMuteRes : streamState.routedToBluetooth ? streamState.muted ? R.drawable.ic_volume_media_bt_mute : R.drawable.ic_volume_media_bt : (this.mAutomute && streamState.level == 0) ? volumeRow.iconMuteRes : streamState.muted ? volumeRow.iconMuteRes : volumeRow.iconRes;
        volumeRow.icon.setImageResource(i5);
        if (i5 == R.drawable.ic_volume_ringer_vibrate) {
            i2 = 3;
        } else if (i5 != R.drawable.ic_volume_media_bt_mute && i5 != volumeRow.iconMuteRes) {
            i2 = (i5 == R.drawable.ic_volume_media_bt || i5 == volumeRow.iconRes) ? 1 : 0;
        }
        volumeRow.iconState = i2;
        if (z9) {
            int i6 = R.string.volume_stream_content_description_mute;
            if (z2) {
                if (z6) {
                    volumeRow.icon.setContentDescription(this.mContext.getString(R.string.volume_stream_content_description_unmute, getStreamLabelH(streamState)));
                } else if (this.mController.hasVibrator()) {
                    volumeRow.icon.setContentDescription(this.mContext.getString(this.mShowA11yStream ? R.string.volume_stream_content_description_vibrate_a11y : R.string.volume_stream_content_description_vibrate, getStreamLabelH(streamState)));
                } else {
                    ImageButton imageButton = volumeRow.icon;
                    Context context = this.mContext;
                    if (this.mShowA11yStream) {
                        i6 = R.string.volume_stream_content_description_mute_a11y;
                    }
                    imageButton.setContentDescription(context.getString(i6, getStreamLabelH(streamState)));
                }
            } else if (z) {
                volumeRow.icon.setContentDescription(getStreamLabelH(streamState));
            } else if (streamState.muted || (this.mAutomute && streamState.level == 0)) {
                volumeRow.icon.setContentDescription(this.mContext.getString(R.string.volume_stream_content_description_unmute, getStreamLabelH(streamState)));
            } else {
                ImageButton imageButton2 = volumeRow.icon;
                Context context2 = this.mContext;
                if (this.mShowA11yStream) {
                    i6 = R.string.volume_stream_content_description_mute_a11y;
                }
                imageButton2.setContentDescription(context2.getString(i6, getStreamLabelH(streamState)));
            }
        } else {
            volumeRow.icon.setContentDescription(getStreamLabelH(streamState));
        }
        if (z8) {
            volumeRow.tracking = false;
        }
        enableVolumeRowViewsH(volumeRow, !z8);
        boolean z10 = !z8;
        if (!volumeRow.ss.muted || z2 || z8) {
            i = volumeRow.ss.level;
        }
        updateVolumeRowSliderH(volumeRow, z10, i);
    }

    private void updateVolumeRowTintH(VolumeRow volumeRow, boolean z) {
        if (z) {
            volumeRow.slider.requestFocus();
        }
        boolean z2 = z && volumeRow.slider.isEnabled();
        ColorStateList colorStateList = z2 ? this.mActiveTint : this.mInactiveTint;
        int i = z2 ? this.mActiveAlpha : this.mInactiveAlpha;
        if (colorStateList == volumeRow.cachedTint) {
            return;
        }
        volumeRow.slider.setProgressTintList(colorStateList);
        volumeRow.slider.setThumbTintList(colorStateList);
        volumeRow.slider.setProgressBackgroundTintList(colorStateList);
        volumeRow.slider.setAlpha(i / 255.0f);
        volumeRow.icon.setImageTintList(colorStateList);
        volumeRow.icon.setImageAlpha(i);
        volumeRow.cachedTint = colorStateList;
    }

    private void updateVolumeRowSliderH(VolumeRow volumeRow, boolean z, int i) {
        int i2;
        volumeRow.slider.setEnabled(z);
        updateVolumeRowTintH(volumeRow, volumeRow.stream == this.mActiveStream);
        if (volumeRow.tracking) {
            return;
        }
        int progress = volumeRow.slider.getProgress();
        int impliedLevel = getImpliedLevel(volumeRow.slider, progress);
        boolean z2 = volumeRow.view.getVisibility() == 0;
        boolean z3 = SystemClock.uptimeMillis() - volumeRow.userAttempt < 1000;
        this.mHandler.removeMessages(3, volumeRow);
        if (this.mShowing && z2 && z3) {
            if (D.BUG) {
                Log.d(TAG, "inGracePeriod");
            }
            this.mHandler.sendMessageAtTime(this.mHandler.obtainMessage(3, volumeRow), volumeRow.userAttempt + 1000);
        } else if ((i != impliedLevel || !this.mShowing || !z2) && progress != (i2 = i * 100)) {
            if (!this.mShowing || !z2) {
                if (volumeRow.anim != null) {
                    volumeRow.anim.cancel();
                }
                volumeRow.slider.setProgress(i2, true);
            } else if (volumeRow.anim != null && volumeRow.anim.isRunning() && volumeRow.animTargetProgress == i2) {
            } else {
                if (volumeRow.anim == null) {
                    volumeRow.anim = ObjectAnimator.ofInt(volumeRow.slider, "progress", progress, i2);
                    volumeRow.anim.setInterpolator(new DecelerateInterpolator());
                } else {
                    volumeRow.anim.cancel();
                    volumeRow.anim.setIntValues(progress, i2);
                }
                volumeRow.animTargetProgress = i2;
                volumeRow.anim.setDuration(80L);
                volumeRow.anim.start();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void recheckH(VolumeRow volumeRow) {
        if (volumeRow == null) {
            if (D.BUG) {
                Log.d(TAG, "recheckH ALL");
            }
            trimObsoleteH();
            for (VolumeRow volumeRow2 : this.mRows) {
                updateVolumeRowH(volumeRow2);
            }
            return;
        }
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "recheckH " + volumeRow.stream);
        }
        updateVolumeRowH(volumeRow);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setStreamImportantH(int i, boolean z) {
        for (VolumeRow volumeRow : this.mRows) {
            if (volumeRow.stream == i) {
                volumeRow.important = z;
                return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showSafetyWarningH(int i) {
        if ((i & 1025) != 0 || this.mShowing) {
            synchronized (this.mSafetyWarningLock) {
                if (this.mSafetyWarning != null) {
                    return;
                }
                this.mSafetyWarning = new SafetyWarningDialog(this.mContext, this.mController.getAudioManager()) { // from class: com.android.systemui.volume.VolumeDialogImpl.2
                    @Override // com.android.systemui.volume.SafetyWarningDialog
                    protected void cleanUp() {
                        synchronized (VolumeDialogImpl.this.mSafetyWarningLock) {
                            VolumeDialogImpl.this.mSafetyWarning = null;
                        }
                        VolumeDialogImpl.this.recheckH(null);
                    }
                };
                this.mSafetyWarning.show();
                recheckH(null);
            }
        }
        rescheduleTimeoutH();
    }

    private String getStreamLabelH(VolumeDialogController.StreamState streamState) {
        if (streamState == null) {
            return "";
        }
        if (streamState.remoteLabel != null) {
            return streamState.remoteLabel;
        }
        try {
            return this.mContext.getResources().getString(streamState.name);
        } catch (Resources.NotFoundException e) {
            String str = TAG;
            Slog.e(str, "Can't find translation for stream " + streamState);
            return "";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    VolumeDialogImpl.this.showH(message.arg1);
                    return;
                case 2:
                    VolumeDialogImpl.this.dismissH(message.arg1);
                    return;
                case 3:
                    VolumeDialogImpl.this.recheckH((VolumeRow) message.obj);
                    return;
                case 4:
                    VolumeDialogImpl.this.recheckH(null);
                    return;
                case 5:
                    VolumeDialogImpl.this.setStreamImportantH(message.arg1, message.arg2 != 0);
                    return;
                case 6:
                    VolumeDialogImpl.this.rescheduleTimeoutH();
                    return;
                case 7:
                    VolumeDialogImpl.this.onStateChangedH(VolumeDialogImpl.this.mState);
                    return;
                default:
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class CustomDialog extends Dialog implements DialogInterface {
        public CustomDialog(Context context) {
            super(context, R.style.qs_theme);
        }

        @Override // android.app.Dialog, android.view.Window.Callback
        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            VolumeDialogImpl.this.rescheduleTimeoutH();
            return super.dispatchTouchEvent(motionEvent);
        }

        @Override // android.app.Dialog
        protected void onStart() {
            super.setCanceledOnTouchOutside(true);
            super.onStart();
        }

        @Override // android.app.Dialog
        protected void onStop() {
            super.onStop();
            VolumeDialogImpl.this.mHandler.sendEmptyMessage(4);
        }

        @Override // android.app.Dialog
        public boolean onTouchEvent(MotionEvent motionEvent) {
            if (isShowing() && motionEvent.getAction() == 4) {
                VolumeDialogImpl.this.dismissH(1);
                return true;
            }
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class VolumeSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        private final VolumeRow mRow;

        private VolumeSeekBarChangeListener(VolumeRow volumeRow) {
            this.mRow = volumeRow;
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
            int i2;
            if (this.mRow.ss == null) {
                return;
            }
            if (D.BUG) {
                String str = VolumeDialogImpl.TAG;
                Log.d(str, AudioSystem.streamToString(this.mRow.stream) + " onProgressChanged " + i + " fromUser=" + z);
            }
            if (z) {
                if (this.mRow.ss.levelMin > 0 && i < (i2 = this.mRow.ss.levelMin * 100)) {
                    seekBar.setProgress(i2);
                    i = i2;
                }
                int impliedLevel = VolumeDialogImpl.getImpliedLevel(seekBar, i);
                if (this.mRow.ss.level == impliedLevel && (!this.mRow.ss.muted || impliedLevel <= 0)) {
                    return;
                }
                this.mRow.userAttempt = SystemClock.uptimeMillis();
                if (this.mRow.requestedLevel == impliedLevel) {
                    return;
                }
                VolumeDialogImpl.this.mController.setStreamVolume(this.mRow.stream, impliedLevel);
                this.mRow.requestedLevel = impliedLevel;
                Events.writeEvent(VolumeDialogImpl.this.mContext, 9, Integer.valueOf(this.mRow.stream), Integer.valueOf(impliedLevel));
            }
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (D.BUG) {
                String str = VolumeDialogImpl.TAG;
                Log.d(str, "onStartTrackingTouch " + this.mRow.stream);
            }
            VolumeDialogImpl.this.mController.setActiveStream(this.mRow.stream);
            this.mRow.tracking = true;
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (D.BUG) {
                String str = VolumeDialogImpl.TAG;
                Log.d(str, "onStopTrackingTouch " + this.mRow.stream);
            }
            this.mRow.tracking = false;
            this.mRow.userAttempt = SystemClock.uptimeMillis();
            int impliedLevel = VolumeDialogImpl.getImpliedLevel(seekBar, seekBar.getProgress());
            Events.writeEvent(VolumeDialogImpl.this.mContext, 16, Integer.valueOf(this.mRow.stream), Integer.valueOf(impliedLevel));
            if (this.mRow.ss.level != impliedLevel) {
                VolumeDialogImpl.this.mHandler.sendMessageDelayed(VolumeDialogImpl.this.mHandler.obtainMessage(3, this.mRow), 1000L);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class Accessibility extends View.AccessibilityDelegate {
        private boolean mFeedbackEnabled;
        private final AccessibilityManager.AccessibilityServicesStateChangeListener mListener;

        private Accessibility() {
            this.mListener = new AccessibilityManager.AccessibilityServicesStateChangeListener() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$Accessibility$kxl9eO7EXO-qLbFSXepX3MznHcY
                public final void onAccessibilityServicesStateChanged(AccessibilityManager accessibilityManager) {
                    VolumeDialogImpl.Accessibility.this.updateFeedbackEnabled();
                }
            };
        }

        public void init() {
            VolumeDialogImpl.this.mDialogView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() { // from class: com.android.systemui.volume.VolumeDialogImpl.Accessibility.1
                @Override // android.view.View.OnAttachStateChangeListener
                public void onViewDetachedFromWindow(View view) {
                    if (D.BUG) {
                        Log.d(VolumeDialogImpl.TAG, "onViewDetachedFromWindow");
                    }
                }

                @Override // android.view.View.OnAttachStateChangeListener
                public void onViewAttachedToWindow(View view) {
                    if (D.BUG) {
                        Log.d(VolumeDialogImpl.TAG, "onViewAttachedToWindow");
                    }
                    Accessibility.this.updateFeedbackEnabled();
                }
            });
            VolumeDialogImpl.this.mDialogView.setAccessibilityDelegate(this);
            VolumeDialogImpl.this.mAccessibilityMgr.addCallback(this.mListener);
            updateFeedbackEnabled();
        }

        public void destroy() {
            VolumeDialogImpl.this.mAccessibilityMgr.removeCallback(this.mListener);
        }

        @Override // android.view.View.AccessibilityDelegate
        public boolean dispatchPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
            accessibilityEvent.getText().add(VolumeDialogImpl.this.composeWindowTitle());
            return true;
        }

        @Override // android.view.View.AccessibilityDelegate
        public boolean onRequestSendAccessibilityEvent(ViewGroup viewGroup, View view, AccessibilityEvent accessibilityEvent) {
            VolumeDialogImpl.this.rescheduleTimeoutH();
            return super.onRequestSendAccessibilityEvent(viewGroup, view, accessibilityEvent);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updateFeedbackEnabled() {
            this.mFeedbackEnabled = computeFeedbackEnabled();
        }

        private boolean computeFeedbackEnabled() {
            for (AccessibilityServiceInfo accessibilityServiceInfo : VolumeDialogImpl.this.mAccessibilityMgr.getEnabledAccessibilityServiceList(-1)) {
                if (accessibilityServiceInfo.feedbackType != 0 && accessibilityServiceInfo.feedbackType != 16) {
                    return true;
                }
            }
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class VolumeRow {
        private ObjectAnimator anim;
        private int animTargetProgress;
        private ColorStateList cachedTint;
        private boolean defaultStream;
        private FrameLayout dndIcon;
        private TextView header;
        private ImageButton icon;
        private int iconMuteRes;
        private int iconRes;
        private int iconState;
        private boolean important;
        private int lastAudibleLevel;
        private int requestedLevel;
        private SeekBar slider;
        private VolumeDialogController.StreamState ss;
        private int stream;
        private boolean tracking;
        private long userAttempt;
        private View view;

        private VolumeRow() {
            this.requestedLevel = -1;
            this.lastAudibleLevel = 1;
        }
    }
}
