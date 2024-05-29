package com.android.systemui.volume;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerZenModePanel;
import com.android.systemui.volume.VolumeDialogController;
import com.android.systemui.volume.VolumeDialogMotion;
import com.android.systemui.volume.ZenModePanel;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/volume/VolumeDialog.class */
public class VolumeDialog implements TunerService.Tunable {
    private static final String TAG = Util.logTag(VolumeDialog.class);
    private final AccessibilityManager mAccessibilityMgr;
    private int mActiveStream;
    private final AudioManager mAudioManager;
    private Callback mCallback;
    private long mCollapseTime;
    private final Context mContext;
    private final VolumeDialogController mController;
    private int mDensity;
    private CustomDialog mDialog;
    private ViewGroup mDialogContentView;
    private ViewGroup mDialogView;
    private ImageButton mExpandButton;
    private int mExpandButtonAnimationDuration;
    private boolean mExpandButtonAnimationRunning;
    private boolean mExpanded;
    private final KeyguardManager mKeyguard;
    private LayoutTransition mLayoutTransition;
    private VolumeDialogMotion mMotion;
    private boolean mPendingRecheckAll;
    private boolean mPendingStateChanged;
    private SafetyWarningDialog mSafetyWarning;
    private boolean mShowFullZen;
    private boolean mShowing;
    private SpTexts mSpTexts;
    private VolumeDialogController.State mState;
    private final int mWindowType;
    private ZenFooter mZenFooter;
    private final ZenModeController mZenModeController;
    private TunerZenModePanel mZenPanel;
    private final H mHandler = new H(this);
    private final List<VolumeRow> mRows = new ArrayList();
    private final SparseBooleanArray mDynamic = new SparseBooleanArray();
    private final Object mSafetyWarningLock = new Object();
    private final Accessibility mAccessibility = new Accessibility(this, null);
    private boolean mShowHeaders = false;
    private boolean mAutomute = true;
    private boolean mSilentMode = true;
    private boolean mHovering = false;
    private final VolumeDialogController.Callbacks mControllerCallbackH = new VolumeDialogController.Callbacks(this) { // from class: com.android.systemui.volume.VolumeDialog.1
        final VolumeDialog this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onConfigurationChanged() {
            if (this.this$0.mContext.getResources().getConfiguration().densityDpi != this.this$0.mDensity) {
                this.this$0.mDialog.dismiss();
                this.this$0.mZenFooter.cleanup();
                this.this$0.initDialog();
            }
            this.this$0.updateWindowWidthH();
            this.this$0.mSpTexts.update();
            this.this$0.mZenFooter.onConfigurationChanged();
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onDismissRequested(int i) {
            this.this$0.dismissH(i);
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onLayoutDirectionChanged(int i) {
            this.this$0.mDialogView.setLayoutDirection(i);
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onScreenOff() {
            this.this$0.dismissH(4);
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onShowRequested(int i) {
            this.this$0.showH(i);
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onShowSafetyWarning(int i) {
            this.this$0.showSafetyWarningH(i);
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onShowSilentHint() {
            if (this.this$0.mSilentMode) {
                this.this$0.mController.setRingerMode(2, false);
            }
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onShowVibrateHint() {
            if (this.this$0.mSilentMode) {
                this.this$0.mController.setRingerMode(0, false);
            }
        }

        @Override // com.android.systemui.volume.VolumeDialogController.Callbacks
        public void onStateChanged(VolumeDialogController.State state) {
            this.this$0.onStateChangedH(state);
        }
    };
    private final ZenModePanel.Callback mZenPanelCallback = new ZenModePanel.Callback(this) { // from class: com.android.systemui.volume.VolumeDialog.2
        final VolumeDialog this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.volume.ZenModePanel.Callback
        public void onExpanded(boolean z) {
        }

        @Override // com.android.systemui.volume.ZenModePanel.Callback
        public void onInteraction() {
            this.this$0.mHandler.sendEmptyMessage(6);
        }

        @Override // com.android.systemui.volume.ZenModePanel.Callback
        public void onPrioritySettings() {
            this.this$0.mCallback.onZenPrioritySettingsClicked();
        }
    };
    private final View.OnClickListener mClickExpand = new View.OnClickListener(this) { // from class: com.android.systemui.volume.VolumeDialog.3
        final VolumeDialog this$0;

        {
            this.this$0 = this;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (this.this$0.mExpandButtonAnimationRunning) {
                return;
            }
            boolean z = !this.this$0.mExpanded;
            Events.writeEvent(this.this$0.mContext, 3, Boolean.valueOf(z));
            this.this$0.setExpandedH(z);
        }
    };
    private final ColorStateList mActiveSliderTint = loadColorStateList(2131558520);
    private final ColorStateList mInactiveSliderTint = loadColorStateList(2131558584);

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialog$Accessibility.class */
    public final class Accessibility extends View.AccessibilityDelegate {
        private boolean mFeedbackEnabled;
        final VolumeDialog this$0;

        private Accessibility(VolumeDialog volumeDialog) {
            this.this$0 = volumeDialog;
        }

        /* synthetic */ Accessibility(VolumeDialog volumeDialog, Accessibility accessibility) {
            this(volumeDialog);
        }

        private boolean computeFeedbackEnabled() {
            for (AccessibilityServiceInfo accessibilityServiceInfo : this.this$0.mAccessibilityMgr.getEnabledAccessibilityServiceList(-1)) {
                if (accessibilityServiceInfo.feedbackType != 0 && accessibilityServiceInfo.feedbackType != 16) {
                    return true;
                }
            }
            return false;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updateFeedbackEnabled() {
            this.mFeedbackEnabled = computeFeedbackEnabled();
        }

        public void init() {
            this.this$0.mDialogView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(this) { // from class: com.android.systemui.volume.VolumeDialog.Accessibility.1
                final Accessibility this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.view.View.OnAttachStateChangeListener
                public void onViewAttachedToWindow(View view) {
                    if (D.BUG) {
                        Log.d(VolumeDialog.TAG, "onViewAttachedToWindow");
                    }
                    this.this$1.updateFeedbackEnabled();
                }

                @Override // android.view.View.OnAttachStateChangeListener
                public void onViewDetachedFromWindow(View view) {
                    if (D.BUG) {
                        Log.d(VolumeDialog.TAG, "onViewDetachedFromWindow");
                    }
                }
            });
            this.this$0.mDialogView.setAccessibilityDelegate(this);
            this.this$0.mAccessibilityMgr.addAccessibilityStateChangeListener(new AccessibilityManager.AccessibilityStateChangeListener(this) { // from class: com.android.systemui.volume.VolumeDialog.Accessibility.2
                final Accessibility this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener
                public void onAccessibilityStateChanged(boolean z) {
                    this.this$1.updateFeedbackEnabled();
                }
            });
            updateFeedbackEnabled();
        }

        @Override // android.view.View.AccessibilityDelegate
        public boolean onRequestSendAccessibilityEvent(ViewGroup viewGroup, View view, AccessibilityEvent accessibilityEvent) {
            this.this$0.rescheduleTimeoutH();
            return super.onRequestSendAccessibilityEvent(viewGroup, view, accessibilityEvent);
        }
    }

    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialog$Callback.class */
    public interface Callback {
        void onZenPrioritySettingsClicked();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialog$CustomDialog.class */
    public final class CustomDialog extends Dialog {
        final VolumeDialog this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public CustomDialog(VolumeDialog volumeDialog, Context context) {
            super(context);
            this.this$0 = volumeDialog;
        }

        @Override // android.app.Dialog, android.view.Window.Callback
        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
            accessibilityEvent.setClassName(getClass().getSuperclass().getName());
            accessibilityEvent.setPackageName(this.this$0.mContext.getPackageName());
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            accessibilityEvent.setFullScreen(((ViewGroup.LayoutParams) attributes).width == -1 ? ((ViewGroup.LayoutParams) attributes).height == -1 : false);
            if (accessibilityEvent.getEventType() == 32 && this.this$0.mShowing) {
                accessibilityEvent.getText().add(this.this$0.mContext.getString(2131493706, this.this$0.getActiveRow().ss.name));
                return true;
            }
            return false;
        }

        @Override // android.app.Dialog, android.view.Window.Callback
        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            this.this$0.rescheduleTimeoutH();
            return super.dispatchTouchEvent(motionEvent);
        }

        @Override // android.app.Dialog
        protected void onStop() {
            super.onStop();
            boolean isAnimating = this.this$0.mMotion.isAnimating();
            if (D.BUG) {
                Log.d(VolumeDialog.TAG, "onStop animating=" + isAnimating);
            }
            if (isAnimating) {
                this.this$0.mPendingRecheckAll = true;
            } else {
                this.this$0.mHandler.sendEmptyMessage(4);
            }
        }

        @Override // android.app.Dialog
        public boolean onTouchEvent(MotionEvent motionEvent) {
            if (isShowing() && motionEvent.getAction() == 4) {
                this.this$0.dismissH(1);
                return true;
            }
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialog$H.class */
    public final class H extends Handler {
        final VolumeDialog this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public H(VolumeDialog volumeDialog) {
            super(Looper.getMainLooper());
            this.this$0 = volumeDialog;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            boolean z = false;
            switch (message.what) {
                case 1:
                    this.this$0.showH(message.arg1);
                    return;
                case 2:
                    this.this$0.dismissH(message.arg1);
                    return;
                case 3:
                    this.this$0.recheckH((VolumeRow) message.obj);
                    return;
                case 4:
                    this.this$0.recheckH(null);
                    return;
                case 5:
                    VolumeDialog volumeDialog = this.this$0;
                    int i = message.arg1;
                    if (message.arg2 != 0) {
                        z = true;
                    }
                    volumeDialog.setStreamImportantH(i, z);
                    return;
                case 6:
                    this.this$0.rescheduleTimeoutH();
                    return;
                case 7:
                    this.this$0.onStateChangedH(this.this$0.mState);
                    return;
                case 8:
                    this.this$0.updateDialogBottomMarginH();
                    return;
                case 9:
                    this.this$0.updateFooterH();
                    return;
                default:
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialog$VolumeRow.class */
    public static class VolumeRow {
        private ObjectAnimator anim;
        private int animTargetProgress;
        private int cachedIconRes;
        private boolean cachedShowHeaders;
        private ColorStateList cachedSliderTint;
        private TextView header;
        private ImageButton icon;
        private int iconMuteRes;
        private int iconRes;
        private int iconState;
        private boolean important;
        private int lastAudibleLevel;
        private int requestedLevel;
        private SeekBar slider;
        private View space;
        private VolumeDialogController.StreamState ss;
        private int stream;
        private boolean tracking;
        private long userAttempt;
        private View view;

        private VolumeRow() {
            this.requestedLevel = -1;
            this.cachedShowHeaders = false;
            this.lastAudibleLevel = 1;
        }

        /* synthetic */ VolumeRow(VolumeRow volumeRow) {
            this();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/volume/VolumeDialog$VolumeSeekBarChangeListener.class */
    public final class VolumeSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        private final VolumeRow mRow;
        final VolumeDialog this$0;

        private VolumeSeekBarChangeListener(VolumeDialog volumeDialog, VolumeRow volumeRow) {
            this.this$0 = volumeDialog;
            this.mRow = volumeRow;
        }

        /* synthetic */ VolumeSeekBarChangeListener(VolumeDialog volumeDialog, VolumeRow volumeRow, VolumeSeekBarChangeListener volumeSeekBarChangeListener) {
            this(volumeDialog, volumeRow);
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
            if (this.mRow.ss == null) {
                return;
            }
            if (D.BUG) {
                Log.d(VolumeDialog.TAG, AudioSystem.streamToString(this.mRow.stream) + " onProgressChanged " + i + " fromUser=" + z);
            }
            if (z) {
                int i2 = i;
                if (this.mRow.ss.levelMin > 0) {
                    int i3 = this.mRow.ss.levelMin * 100;
                    i2 = i;
                    if (i < i3) {
                        seekBar.setProgress(i3);
                        i2 = i3;
                    }
                }
                int impliedLevel = VolumeDialog.getImpliedLevel(seekBar, i2);
                if (this.mRow.ss.level != impliedLevel || (this.mRow.ss.muted && impliedLevel > 0)) {
                    this.mRow.userAttempt = SystemClock.uptimeMillis();
                    if (this.mRow.requestedLevel != impliedLevel) {
                        this.this$0.mController.setStreamVolume(this.mRow.stream, impliedLevel);
                        this.mRow.requestedLevel = impliedLevel;
                        Events.writeEvent(this.this$0.mContext, 9, Integer.valueOf(this.mRow.stream), Integer.valueOf(impliedLevel));
                    }
                }
            }
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (D.BUG) {
                Log.d(VolumeDialog.TAG, "onStartTrackingTouch " + this.mRow.stream);
            }
            this.this$0.mController.setActiveStream(this.mRow.stream);
            this.mRow.tracking = true;
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (D.BUG) {
                Log.d(VolumeDialog.TAG, "onStopTrackingTouch " + this.mRow.stream);
            }
            this.mRow.tracking = false;
            this.mRow.userAttempt = SystemClock.uptimeMillis();
            int impliedLevel = VolumeDialog.getImpliedLevel(seekBar, seekBar.getProgress());
            Events.writeEvent(this.this$0.mContext, 16, Integer.valueOf(this.mRow.stream), Integer.valueOf(impliedLevel));
            if (this.mRow.ss.level != impliedLevel) {
                this.this$0.mHandler.sendMessageDelayed(this.this$0.mHandler.obtainMessage(3, this.mRow), 1000L);
            }
        }
    }

    public VolumeDialog(Context context, int i, VolumeDialogController volumeDialogController, ZenModeController zenModeController, Callback callback) {
        this.mContext = context;
        this.mController = volumeDialogController;
        this.mCallback = callback;
        this.mWindowType = i;
        this.mZenModeController = zenModeController;
        this.mKeyguard = (KeyguardManager) context.getSystemService("keyguard");
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mAccessibilityMgr = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        initDialog();
        this.mAccessibility.init();
        volumeDialogController.addCallback(this.mControllerCallbackH, this.mHandler);
        volumeDialogController.getState();
        TunerService.get(this.mContext).addTunable(this, "sysui_show_full_zen");
        this.mDensity = this.mContext.getResources().getConfiguration().densityDpi;
    }

    private void addExistingRows() {
        int size = this.mRows.size();
        for (int i = 0; i < size; i++) {
            VolumeRow volumeRow = this.mRows.get(i);
            initRow(volumeRow, volumeRow.stream, volumeRow.iconRes, volumeRow.iconMuteRes, volumeRow.important);
            if (i > 0) {
                addSpacer(volumeRow);
            }
            this.mDialogContentView.addView(volumeRow.view, this.mDialogContentView.getChildCount() - 2);
        }
    }

    private void addRow(int i, int i2, int i3, boolean z) {
        VolumeRow volumeRow = new VolumeRow(null);
        initRow(volumeRow, i, i2, i3, z);
        if (!this.mRows.isEmpty()) {
            addSpacer(volumeRow);
        }
        this.mDialogContentView.addView(volumeRow.view, this.mDialogContentView.getChildCount() - 2);
        this.mRows.add(volumeRow);
    }

    private void addSpacer(VolumeRow volumeRow) {
        View view = new View(this.mContext);
        view.setId(16908288);
        this.mDialogContentView.addView(view, this.mDialogContentView.getChildCount() - 2, new LinearLayout.LayoutParams(-1, this.mContext.getResources().getDimensionPixelSize(2131689968)));
        volumeRow.space = view;
    }

    private int computeTimeoutH() {
        if (this.mAccessibility.mFeedbackEnabled) {
            return 20000;
        }
        if (this.mHovering) {
            return 16000;
        }
        if (this.mSafetyWarning != null || this.mExpanded || this.mExpandButtonAnimationRunning) {
            return 5000;
        }
        return this.mActiveStream == 3 ? 1500 : 3000;
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
    public VolumeRow getActiveRow() {
        for (VolumeRow volumeRow : this.mRows) {
            if (volumeRow.stream == this.mActiveStream) {
                return volumeRow;
            }
        }
        return this.mRows.get(0);
    }

    private long getConservativeCollapseDuration() {
        return this.mExpandButtonAnimationDuration * 3;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int getImpliedLevel(SeekBar seekBar, int i) {
        int max = seekBar.getMax();
        return i == 0 ? 0 : i == max ? max / 100 : ((int) ((i / max) * ((max / 100) - 1))) + 1;
    }

    private boolean hasTouchFeature() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.touchscreen");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initDialog() {
        this.mDialog = new CustomDialog(this, this.mContext);
        this.mSpTexts = new SpTexts(this.mContext);
        this.mLayoutTransition = new LayoutTransition();
        this.mLayoutTransition.setDuration(new ValueAnimator().getDuration() / 2);
        this.mHovering = false;
        this.mShowing = false;
        Window window = this.mDialog.getWindow();
        window.requestFeature(1);
        window.setBackgroundDrawable(new ColorDrawable(0));
        window.clearFlags(2);
        window.addFlags(17563944);
        this.mDialog.setCanceledOnTouchOutside(true);
        Resources resources = this.mContext.getResources();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.type = this.mWindowType;
        attributes.format = -3;
        attributes.setTitle(VolumeDialog.class.getSimpleName());
        attributes.gravity = 49;
        attributes.y = resources.getDimensionPixelSize(2131689969);
        attributes.gravity = 48;
        attributes.windowAnimations = -1;
        window.setAttributes(attributes);
        window.setSoftInputMode(48);
        this.mDialog.setContentView(2130968835);
        this.mDialogView = (ViewGroup) this.mDialog.findViewById(2131886750);
        this.mDialogView.setOnHoverListener(new View.OnHoverListener(this) { // from class: com.android.systemui.volume.VolumeDialog.4
            final VolumeDialog this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnHoverListener
            public boolean onHover(View view, MotionEvent motionEvent) {
                int actionMasked = motionEvent.getActionMasked();
                this.this$0.mHovering = actionMasked != 9 ? actionMasked == 7 : true;
                this.this$0.rescheduleTimeoutH();
                return true;
            }
        });
        this.mDialogContentView = (ViewGroup) this.mDialog.findViewById(2131886751);
        this.mExpanded = false;
        this.mExpandButton = (ImageButton) this.mDialogView.findViewById(2131886752);
        this.mExpandButton.setOnClickListener(this.mClickExpand);
        updateWindowWidthH();
        updateExpandButtonH();
        this.mDialogContentView.setLayoutTransition(this.mLayoutTransition);
        this.mMotion = new VolumeDialogMotion(this.mDialog, this.mDialogView, this.mDialogContentView, this.mExpandButton, new VolumeDialogMotion.Callback(this) { // from class: com.android.systemui.volume.VolumeDialog.5
            final VolumeDialog this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.volume.VolumeDialogMotion.Callback
            public void onAnimatingChanged(boolean z) {
                if (z) {
                    return;
                }
                if (this.this$0.mPendingStateChanged) {
                    this.this$0.mHandler.sendEmptyMessage(7);
                    this.this$0.mPendingStateChanged = false;
                }
                if (this.this$0.mPendingRecheckAll) {
                    this.this$0.mHandler.sendEmptyMessage(4);
                    this.this$0.mPendingRecheckAll = false;
                }
            }
        });
        if (this.mRows.isEmpty()) {
            addRow(2, 2130837821, 2130837822, true);
            addRow(3, 2130837815, 2130837818, true);
            addRow(4, 2130837808, 2130837809, false);
            addRow(0, 2130837827, 2130837827, false);
            addRow(6, 2130837810, 2130837810, false);
            addRow(1, 2130837825, 2130837826, false);
        } else {
            addExistingRows();
        }
        this.mExpandButtonAnimationDuration = resources.getInteger(2131755089);
        this.mZenFooter = (ZenFooter) this.mDialog.findViewById(2131886758);
        this.mZenFooter.init(this.mZenModeController);
        this.mZenPanel = (TunerZenModePanel) this.mDialog.findViewById(2131886725);
        this.mZenPanel.init(this.mZenModeController);
        this.mZenPanel.setCallback(this.mZenPanelCallback);
    }

    @SuppressLint({"InflateParams"})
    private void initRow(VolumeRow volumeRow, int i, int i2, int i3, boolean z) {
        volumeRow.stream = i;
        volumeRow.iconRes = i2;
        volumeRow.iconMuteRes = i3;
        volumeRow.important = z;
        volumeRow.view = this.mDialog.getLayoutInflater().inflate(2130968836, (ViewGroup) null);
        volumeRow.view.setTag(volumeRow);
        volumeRow.header = (TextView) volumeRow.view.findViewById(2131886754);
        this.mSpTexts.add(volumeRow.header);
        volumeRow.slider = (SeekBar) volumeRow.view.findViewById(2131886756);
        volumeRow.slider.setOnSeekBarChangeListener(new VolumeSeekBarChangeListener(this, volumeRow, null));
        volumeRow.anim = null;
        volumeRow.view.setOnTouchListener(new View.OnTouchListener(this, volumeRow) { // from class: com.android.systemui.volume.VolumeDialog.6
            private boolean mDragging;
            private final Rect mSliderHitRect = new Rect();
            final VolumeDialog this$0;
            final VolumeRow val$row;

            {
                this.this$0 = this;
                this.val$row = volumeRow;
            }

            @Override // android.view.View.OnTouchListener
            @SuppressLint({"ClickableViewAccessibility"})
            public boolean onTouch(View view, MotionEvent motionEvent) {
                this.val$row.slider.getHitRect(this.mSliderHitRect);
                if (!this.mDragging && motionEvent.getActionMasked() == 0 && motionEvent.getY() < this.mSliderHitRect.top) {
                    this.mDragging = true;
                }
                if (this.mDragging) {
                    motionEvent.offsetLocation(-this.mSliderHitRect.left, -this.mSliderHitRect.top);
                    this.val$row.slider.dispatchTouchEvent(motionEvent);
                    if (motionEvent.getActionMasked() == 1 || motionEvent.getActionMasked() == 3) {
                        this.mDragging = false;
                        return true;
                    }
                    return true;
                }
                return false;
            }
        });
        volumeRow.icon = (ImageButton) volumeRow.view.findViewById(2131886755);
        volumeRow.icon.setImageResource(i2);
        volumeRow.icon.setOnClickListener(new View.OnClickListener(this, volumeRow, i) { // from class: com.android.systemui.volume.VolumeDialog.7
            final VolumeDialog this$0;
            final VolumeRow val$row;
            final int val$stream;

            {
                this.this$0 = this;
                this.val$row = volumeRow;
                this.val$stream = i;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                int i4 = 0;
                Events.writeEvent(this.this$0.mContext, 7, Integer.valueOf(this.val$row.stream), Integer.valueOf(this.val$row.iconState));
                this.this$0.mController.setActiveStream(this.val$row.stream);
                if (this.val$row.stream == 2) {
                    boolean hasVibrator = this.this$0.mController.hasVibrator();
                    if (this.this$0.mState.ringerModeInternal != 2) {
                        this.this$0.mController.setRingerMode(2, false);
                        if (this.val$row.ss.level == 0) {
                            this.this$0.mController.setStreamVolume(this.val$stream, 1);
                        }
                    } else if (hasVibrator) {
                        this.this$0.mController.setRingerMode(1, false);
                    } else {
                        boolean z2 = this.val$row.ss.level == 0;
                        VolumeDialogController volumeDialogController = this.this$0.mController;
                        int i5 = this.val$stream;
                        if (z2) {
                            i4 = this.val$row.lastAudibleLevel;
                        }
                        volumeDialogController.setStreamVolume(i5, i4);
                    }
                } else {
                    this.this$0.mController.setStreamVolume(this.val$stream, this.val$row.ss.level == this.val$row.ss.levelMin ? this.val$row.lastAudibleLevel : this.val$row.ss.levelMin);
                }
                this.val$row.userAttempt = 0L;
            }
        });
    }

    private boolean isAttached() {
        return this.mDialogContentView != null ? this.mDialogContentView.isAttachedToWindow() : false;
    }

    private boolean isVisibleH(VolumeRow volumeRow, boolean z) {
        if ((this.mExpanded && volumeRow.view.getVisibility() == 0) || (this.mExpanded && (volumeRow.important || z))) {
            z = true;
        } else if (this.mExpanded) {
            z = false;
        }
        return z;
    }

    private ColorStateList loadColorStateList(int i) {
        return ColorStateList.valueOf(this.mContext.getColor(i));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onStateChangedH(VolumeDialogController.State state) {
        boolean isAnimating = this.mMotion.isAnimating();
        if (D.BUG) {
            Log.d(TAG, "onStateChangedH animating=" + isAnimating);
        }
        this.mState = state;
        if (isAnimating) {
            this.mPendingStateChanged = true;
            return;
        }
        this.mDynamic.clear();
        for (int i = 0; i < state.states.size(); i++) {
            int keyAt = state.states.keyAt(i);
            if (state.states.valueAt(i).dynamic) {
                this.mDynamic.put(keyAt, true);
                if (findRow(keyAt) == null) {
                    addRow(keyAt, 2130837819, 2130837820, true);
                }
            }
        }
        if (this.mActiveStream != state.activeStream) {
            this.mActiveStream = state.activeStream;
            updateRowsH();
            rescheduleTimeoutH();
        }
        for (VolumeRow volumeRow : this.mRows) {
            updateVolumeRowH(volumeRow);
        }
        updateFooterH();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void prepareForCollapse() {
        this.mHandler.removeMessages(8);
        this.mCollapseTime = System.currentTimeMillis();
        updateDialogBottomMarginH();
        this.mHandler.sendEmptyMessageDelayed(8, getConservativeCollapseDuration());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void recheckH(VolumeRow volumeRow) {
        if (volumeRow != null) {
            if (D.BUG) {
                Log.d(TAG, "recheckH " + volumeRow.stream);
            }
            updateVolumeRowH(volumeRow);
            return;
        }
        if (D.BUG) {
            Log.d(TAG, "recheckH ALL");
        }
        trimObsoleteH();
        for (VolumeRow volumeRow2 : this.mRows) {
            updateVolumeRowH(volumeRow2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setExpandedH(boolean z) {
        if (this.mExpanded == z) {
            return;
        }
        this.mExpanded = z;
        this.mExpandButtonAnimationRunning = isAttached();
        if (D.BUG) {
            Log.d(TAG, "setExpandedH " + z);
        }
        if (!this.mExpanded && this.mExpandButtonAnimationRunning) {
            prepareForCollapse();
        }
        updateRowsH();
        if (this.mExpandButtonAnimationRunning) {
            Drawable drawable = this.mExpandButton.getDrawable();
            if (drawable instanceof AnimatedVectorDrawable) {
                AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) drawable.getConstantState().newDrawable();
                this.mExpandButton.setImageDrawable(animatedVectorDrawable);
                animatedVectorDrawable.start();
                this.mHandler.postDelayed(new Runnable(this) { // from class: com.android.systemui.volume.VolumeDialog.9
                    final VolumeDialog this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.mExpandButtonAnimationRunning = false;
                        this.this$0.updateExpandButtonH();
                        this.this$0.rescheduleTimeoutH();
                    }
                }, this.mExpandButtonAnimationDuration);
            }
        }
        rescheduleTimeoutH();
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
    public void showH(int i) {
        if (D.BUG) {
            Log.d(TAG, "showH r=" + Events.DISMISS_REASONS[i]);
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        rescheduleTimeoutH();
        if (this.mShowing) {
            return;
        }
        this.mShowing = true;
        this.mMotion.startShow();
        Events.writeEvent(this.mContext, 0, Integer.valueOf(i), Boolean.valueOf(this.mKeyguard.isKeyguardLocked()));
        this.mController.notifyVisible(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showSafetyWarningH(int i) {
        if ((i & 1025) != 0 || this.mShowing) {
            synchronized (this.mSafetyWarningLock) {
                if (this.mSafetyWarning != null) {
                    return;
                }
                this.mSafetyWarning = new SafetyWarningDialog(this, this.mContext, this.mController.getAudioManager()) { // from class: com.android.systemui.volume.VolumeDialog.11
                    final VolumeDialog this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // com.android.systemui.volume.SafetyWarningDialog
                    protected void cleanUp() {
                        synchronized (this.this$0.mSafetyWarningLock) {
                            this.this$0.mSafetyWarning = null;
                        }
                        this.this$0.recheckH(null);
                    }
                };
                this.mSafetyWarning.show();
                recheckH(null);
            }
        }
        rescheduleTimeoutH();
    }

    private void trimObsoleteH() {
        if (D.BUG) {
            Log.d(TAG, "trimObsoleteH");
        }
        for (int size = this.mRows.size() - 1; size >= 0; size--) {
            VolumeRow volumeRow = this.mRows.get(size);
            if (volumeRow.ss != null && volumeRow.ss.dynamic && !this.mDynamic.get(volumeRow.stream)) {
                this.mRows.remove(size);
                this.mDialogContentView.removeView(volumeRow.view);
                this.mDialogContentView.removeView(volumeRow.space);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDialogBottomMarginH() {
        boolean z = this.mCollapseTime != 0 && System.currentTimeMillis() - this.mCollapseTime < getConservativeCollapseDuration();
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mDialogView.getLayoutParams();
        int height = z ? this.mDialogContentView.getHeight() : this.mContext.getResources().getDimensionPixelSize(2131689971);
        if (height != marginLayoutParams.bottomMargin) {
            if (D.BUG) {
                Log.d(TAG, "bottomMargin " + marginLayoutParams.bottomMargin + " -> " + height);
            }
            marginLayoutParams.bottomMargin = height;
            this.mDialogView.setLayoutParams(marginLayoutParams);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateExpandButtonH() {
        if (D.BUG) {
            Log.d(TAG, "updateExpandButtonH");
        }
        this.mExpandButton.setClickable(!this.mExpandButtonAnimationRunning);
        if (this.mExpandButtonAnimationRunning && isAttached()) {
            return;
        }
        int i = this.mExpanded ? 2130837812 : 2130837814;
        if (hasTouchFeature()) {
            this.mExpandButton.setImageResource(i);
        } else {
            this.mExpandButton.setImageResource(2130837821);
            this.mExpandButton.setBackgroundResource(0);
        }
        this.mExpandButton.setContentDescription(this.mContext.getString(this.mExpanded ? 2131493683 : 2131493682));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateFooterH() {
        if (D.BUG) {
            Log.d(TAG, "updateFooterH");
        }
        boolean z = this.mZenFooter.getVisibility() == 0;
        boolean z2 = (this.mState.zenMode == 0 || !(this.mAudioManager.isStreamAffectedByRingerMode(this.mActiveStream) || this.mExpanded)) ? false : !this.mZenPanel.isEditing();
        if (z != z2 && !z2) {
            prepareForCollapse();
        }
        Util.setVisOrGone(this.mZenFooter, z2);
        this.mZenFooter.update();
        boolean z3 = this.mZenPanel.getVisibility() == 0;
        boolean z4 = false;
        if (this.mShowFullZen) {
            z4 = !z2;
        }
        if (z3 != z4 && !z4) {
            prepareForCollapse();
        }
        Util.setVisOrGone(this.mZenPanel, z4);
        if (z4) {
            this.mZenPanel.setZenState(this.mState.zenMode);
            this.mZenPanel.setDoneListener(new View.OnClickListener(this) { // from class: com.android.systemui.volume.VolumeDialog.10
                final VolumeDialog this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    this.this$0.prepareForCollapse();
                    this.this$0.mHandler.sendEmptyMessage(9);
                }
            });
        }
    }

    private void updateRowsH() {
        if (D.BUG) {
            Log.d(TAG, "updateRowsH");
        }
        VolumeRow activeRow = getActiveRow();
        updateFooterH();
        updateExpandButtonH();
        if (!this.mShowing) {
            trimObsoleteH();
        }
        for (VolumeRow volumeRow : this.mRows) {
            boolean z = volumeRow == activeRow;
            boolean isVisibleH = isVisibleH(volumeRow, z);
            Util.setVisOrGone(volumeRow.view, isVisibleH);
            Util.setVisOrGone(volumeRow.space, isVisibleH ? this.mExpanded : false);
            updateVolumeRowHeaderVisibleH(volumeRow);
            volumeRow.header.setAlpha((this.mExpanded && z) ? 1.0f : 0.5f);
            updateVolumeRowSliderTintH(volumeRow, z);
        }
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
        boolean z = volumeRow.stream == 2;
        boolean z2 = volumeRow.stream == 1;
        boolean z3 = volumeRow.stream == 4;
        boolean z4 = volumeRow.stream == 3;
        boolean z5 = z ? this.mState.ringerModeInternal == 1 : false;
        boolean z6 = z ? this.mState.ringerModeInternal == 0 : false;
        boolean z7 = this.mState.zenMode == 3;
        boolean z8 = this.mState.zenMode == 2;
        boolean z9 = this.mState.zenMode == 1;
        boolean z10 = (z || z2) ? z8 : false;
        if (!z) {
            z9 = false;
        }
        if (z7) {
            z4 = !z ? z2 : true;
        } else if (!z8) {
            z4 = false;
        } else if (z || z2 || z3) {
            z4 = true;
        }
        int i = streamState.levelMax * 100;
        if (i != volumeRow.slider.getMax()) {
            volumeRow.slider.setMax(i);
        }
        updateVolumeRowHeaderVisibleH(volumeRow);
        String str = streamState.name;
        String str2 = str;
        if (this.mShowHeaders) {
            if (z10) {
                str2 = this.mContext.getString(2131493700, streamState.name);
            } else if (z5 && z9) {
                str2 = this.mContext.getString(2131493702, streamState.name);
            } else if (z5) {
                str2 = this.mContext.getString(2131493698, streamState.name);
            } else if (streamState.muted || (this.mAutomute && streamState.level == 0)) {
                str2 = this.mContext.getString(2131493697, streamState.name);
            } else {
                str2 = str;
                if (z9) {
                    str2 = this.mContext.getString(2131493701, streamState.name);
                }
            }
        }
        Util.setText(volumeRow.header, str2);
        boolean z11 = (this.mAutomute || streamState.muteSupported) && !z4;
        volumeRow.icon.setEnabled(z11);
        volumeRow.icon.setAlpha(z11 ? 1.0f : 0.5f);
        int i2 = z5 ? 2130837823 : (z6 || z4) ? volumeRow.cachedIconRes : streamState.routedToBluetooth ? streamState.muted ? 2130837817 : 2130837816 : (this.mAutomute && streamState.level == 0) ? volumeRow.iconMuteRes : streamState.muted ? volumeRow.iconMuteRes : volumeRow.iconRes;
        if (i2 != volumeRow.cachedIconRes) {
            if (volumeRow.cachedIconRes != 0 && z5) {
                this.mController.vibrate();
            }
            volumeRow.cachedIconRes = i2;
            volumeRow.icon.setImageResource(i2);
        }
        volumeRow.iconState = i2 == 2130837823 ? 3 : (i2 == 2130837817 || i2 == volumeRow.iconMuteRes) ? 2 : (i2 == 2130837816 || i2 == volumeRow.iconRes) ? 1 : 0;
        if (!z11) {
            volumeRow.icon.setContentDescription(streamState.name);
        } else if (z) {
            if (z5) {
                volumeRow.icon.setContentDescription(this.mContext.getString(2131493703, streamState.name));
            } else if (this.mController.hasVibrator()) {
                volumeRow.icon.setContentDescription(this.mContext.getString(2131493704, streamState.name));
            } else {
                volumeRow.icon.setContentDescription(this.mContext.getString(2131493705, streamState.name));
            }
        } else if (streamState.muted || (this.mAutomute && streamState.level == 0)) {
            volumeRow.icon.setContentDescription(this.mContext.getString(2131493703, streamState.name));
        } else {
            volumeRow.icon.setContentDescription(this.mContext.getString(2131493705, streamState.name));
        }
        updateVolumeRowSliderH(volumeRow, !z4, (!volumeRow.ss.muted || (!z5 && (z || z4))) ? volumeRow.ss.level : 0);
    }

    private void updateVolumeRowHeaderVisibleH(VolumeRow volumeRow) {
        boolean z = volumeRow.ss != null ? volumeRow.ss.dynamic : false;
        if (this.mShowHeaders) {
            z = true;
        } else if (!this.mExpanded) {
            z = false;
        }
        if (volumeRow.cachedShowHeaders != z) {
            volumeRow.cachedShowHeaders = z;
            Util.setVisOrGone(volumeRow.header, z);
        }
    }

    private void updateVolumeRowSliderH(VolumeRow volumeRow, boolean z, int i) {
        int i2;
        volumeRow.slider.setEnabled(z);
        updateVolumeRowSliderTintH(volumeRow, volumeRow.stream == this.mActiveStream);
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
        } else if ((i == impliedLevel && this.mShowing && z2) || progress == (i2 = i * 100)) {
        } else {
            if (!this.mShowing || !z2) {
                if (volumeRow.anim != null) {
                    volumeRow.anim.cancel();
                }
                volumeRow.slider.setProgress(i2);
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

    private void updateVolumeRowSliderTintH(VolumeRow volumeRow, boolean z) {
        if (z && this.mExpanded) {
            volumeRow.slider.requestFocus();
        }
        ColorStateList colorStateList = (z && volumeRow.slider.isEnabled()) ? this.mActiveSliderTint : this.mInactiveSliderTint;
        if (colorStateList == volumeRow.cachedSliderTint) {
            return;
        }
        volumeRow.cachedSliderTint = colorStateList;
        volumeRow.slider.setProgressTintList(colorStateList);
        volumeRow.slider.setThumbTintList(colorStateList);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateWindowWidthH() {
        ViewGroup.LayoutParams layoutParams = this.mDialogView.getLayoutParams();
        DisplayMetrics displayMetrics = this.mContext.getResources().getDisplayMetrics();
        if (D.BUG) {
            Log.d(TAG, "updateWindowWidth dm.w=" + displayMetrics.widthPixels);
        }
        int i = displayMetrics.widthPixels;
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(2131689817);
        int i2 = i;
        if (i > dimensionPixelSize) {
            i2 = dimensionPixelSize;
        }
        layoutParams.width = i2;
        this.mDialogView.setLayoutParams(layoutParams);
    }

    protected void dismissH(int i) {
        if (this.mMotion.isAnimating()) {
            return;
        }
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(1);
        if (this.mShowing) {
            this.mShowing = false;
            this.mMotion.startDismiss(new Runnable(this) { // from class: com.android.systemui.volume.VolumeDialog.8
                final VolumeDialog this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.setExpandedH(false);
                }
            });
            if (this.mAccessibilityMgr.isEnabled()) {
                AccessibilityEvent obtain = AccessibilityEvent.obtain(32);
                obtain.setPackageName(this.mContext.getPackageName());
                obtain.setClassName(CustomDialog.class.getSuperclass().getName());
                obtain.getText().add(this.mContext.getString(2131493707));
                this.mAccessibilityMgr.sendAccessibilityEvent(obtain);
            }
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
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println(VolumeDialog.class.getSimpleName() + " state:");
        printWriter.print("  mShowing: ");
        printWriter.println(this.mShowing);
        printWriter.print("  mExpanded: ");
        printWriter.println(this.mExpanded);
        printWriter.print("  mExpandButtonAnimationRunning: ");
        printWriter.println(this.mExpandButtonAnimationRunning);
        printWriter.print("  mActiveStream: ");
        printWriter.println(this.mActiveStream);
        printWriter.print("  mDynamic: ");
        printWriter.println(this.mDynamic);
        printWriter.print("  mShowHeaders: ");
        printWriter.println(this.mShowHeaders);
        printWriter.print("  mAutomute: ");
        printWriter.println(this.mAutomute);
        printWriter.print("  mSilentMode: ");
        printWriter.println(this.mSilentMode);
        printWriter.print("  mCollapseTime: ");
        printWriter.println(this.mCollapseTime);
        printWriter.print("  mAccessibility.mFeedbackEnabled: ");
        printWriter.println(this.mAccessibility.mFeedbackEnabled);
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("sysui_show_full_zen".equals(str)) {
            boolean z = false;
            if (str2 != null) {
                z = false;
                if (Integer.parseInt(str2) != 0) {
                    z = true;
                }
            }
            this.mShowFullZen = z;
        }
    }

    protected void rescheduleTimeoutH() {
        this.mHandler.removeMessages(2);
        int computeTimeoutH = computeTimeoutH();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, 3, 0), computeTimeoutH);
        if (D.BUG) {
            Log.d(TAG, "rescheduleTimeout " + computeTimeoutH + " " + Debug.getCaller());
        }
        this.mController.userActivity();
    }

    public void setAutomute(boolean z) {
        if (this.mAutomute == z) {
            return;
        }
        this.mAutomute = z;
        this.mHandler.sendEmptyMessage(4);
    }

    public void setShowHeaders(boolean z) {
        if (z == this.mShowHeaders) {
            return;
        }
        this.mShowHeaders = z;
        this.mHandler.sendEmptyMessage(4);
    }

    public void setSilentMode(boolean z) {
        if (this.mSilentMode == z) {
            return;
        }
        this.mSilentMode = z;
        this.mHandler.sendEmptyMessage(4);
    }

    public void setStreamImportant(int i, boolean z) {
        this.mHandler.obtainMessage(5, i, z ? 1 : 0).sendToTarget();
    }
}
