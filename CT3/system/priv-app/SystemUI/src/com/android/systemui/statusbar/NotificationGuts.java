package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.INotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.Utils;
import com.android.systemui.Interpolators;
import com.android.systemui.tuner.TunerService;
/* loaded from: a.zip:com/android/systemui/statusbar/NotificationGuts.class */
public class NotificationGuts extends LinearLayout implements TunerService.Tunable {
    private float mActiveSliderAlpha;
    private ColorStateList mActiveSliderTint;
    private int mActualHeight;
    private boolean mAuto;
    private ImageView mAutoButton;
    private Drawable mBackground;
    private RadioButton mBlock;
    private int mClipTopAmount;
    private boolean mExposed;
    private Runnable mFalsingCheck;
    private Handler mHandler;
    private INotificationManager mINotificationManager;
    private TextView mImportanceSummary;
    private TextView mImportanceTitle;
    private float mInactiveSliderAlpha;
    private ColorStateList mInactiveSliderTint;
    private OnGutsClosedListener mListener;
    private boolean mNeedsFalsingProtection;
    private int mNotificationImportance;
    private RadioButton mReset;
    private SeekBar mSeekBar;
    private boolean mShowSlider;
    private RadioButton mSilent;
    private int mStartingUserImportance;

    /* loaded from: a.zip:com/android/systemui/statusbar/NotificationGuts$OnGutsClosedListener.class */
    public interface OnGutsClosedListener {
        void onGutsClosed(NotificationGuts notificationGuts);
    }

    public NotificationGuts(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mActiveSliderAlpha = 1.0f;
        setWillNotDraw(false);
        this.mHandler = new Handler();
        this.mFalsingCheck = new Runnable(this) { // from class: com.android.systemui.statusbar.NotificationGuts.1
            final NotificationGuts this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.mNeedsFalsingProtection && this.this$0.mExposed) {
                    this.this$0.closeControls(-1, -1, true);
                }
            }
        };
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.Theme, 0, 0);
        this.mInactiveSliderAlpha = obtainStyledAttributes.getFloat(3, 0.5f);
        obtainStyledAttributes.recycle();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyAuto() {
        this.mSeekBar.setEnabled(!this.mAuto);
        ColorStateList colorStateList = this.mAuto ? this.mActiveSliderTint : this.mInactiveSliderTint;
        float f = this.mAuto ? this.mInactiveSliderAlpha : this.mActiveSliderAlpha;
        Drawable mutate = this.mAutoButton.getDrawable().mutate();
        mutate.setTintList(colorStateList);
        this.mAutoButton.setImageDrawable(mutate);
        this.mSeekBar.setAlpha(f);
        if (!this.mAuto) {
            updateTitleAndSummary(this.mSeekBar.getProgress());
            return;
        }
        this.mSeekBar.setProgress(this.mNotificationImportance);
        this.mImportanceSummary.setText(this.mContext.getString(2131493769));
        this.mImportanceTitle.setText(this.mContext.getString(2131493762));
    }

    private void bindSlider(View view, boolean z) {
        this.mActiveSliderTint = loadColorStateList(2131558559);
        this.mInactiveSliderTint = loadColorStateList(2131558560);
        this.mImportanceSummary = (TextView) view.findViewById(2131886553);
        this.mImportanceTitle = (TextView) view.findViewById(2131886212);
        this.mSeekBar = (SeekBar) view.findViewById(2131886537);
        int i = z ? 1 : 0;
        this.mSeekBar.setMax(5);
        this.mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(this, i) { // from class: com.android.systemui.statusbar.NotificationGuts.3
            final NotificationGuts this$0;
            final int val$minProgress;

            {
                this.this$0 = this;
                this.val$minProgress = i;
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar, int i2, boolean z2) {
                this.this$0.resetFalsingCheck();
                int i3 = i2;
                if (i2 < this.val$minProgress) {
                    seekBar.setProgress(this.val$minProgress);
                    i3 = this.val$minProgress;
                }
                this.this$0.updateTitleAndSummary(i3);
                if (z2) {
                    MetricsLogger.action(this.this$0.mContext, 290);
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar) {
                this.this$0.resetFalsingCheck();
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.mSeekBar.setProgress(this.mNotificationImportance);
        this.mAutoButton = (ImageView) view.findViewById(2131886554);
        this.mAutoButton.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.statusbar.NotificationGuts.4
            final NotificationGuts this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                this.this$0.mAuto = !this.this$0.mAuto;
                this.this$0.applyAuto();
            }
        });
        this.mAuto = this.mStartingUserImportance == -1000;
        applyAuto();
    }

    private void bindToggles(View view, int i, boolean z) {
        ((RadioGroup) view).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(this) { // from class: com.android.systemui.statusbar.NotificationGuts.2
            final NotificationGuts this$0;

            {
                this.this$0 = this;
            }

            @Override // android.widget.RadioGroup.OnCheckedChangeListener
            public void onCheckedChanged(RadioGroup radioGroup, int i2) {
                this.this$0.resetFalsingCheck();
            }
        });
        this.mBlock = (RadioButton) view.findViewById(2131886550);
        this.mSilent = (RadioButton) view.findViewById(2131886549);
        this.mReset = (RadioButton) view.findViewById(2131886551);
        if (z) {
            this.mBlock.setVisibility(8);
            this.mReset.setText(this.mContext.getString(2131493756));
        } else {
            this.mReset.setText(this.mContext.getString(2131493757));
        }
        this.mBlock.setText(this.mContext.getString(2131493755));
        this.mSilent.setText(this.mContext.getString(2131493754));
        if (i == 2) {
            this.mSilent.setChecked(true);
        } else {
            this.mReset.setChecked(true);
        }
    }

    private void draw(Canvas canvas, Drawable drawable) {
        if (drawable != null) {
            drawable.setBounds(0, this.mClipTopAmount, getWidth(), this.mActualHeight);
            drawable.draw(canvas);
        }
    }

    private void drawableStateChanged(Drawable drawable) {
        if (drawable == null || !drawable.isStateful()) {
            return;
        }
        drawable.setState(getDrawableState());
    }

    private int getSelectedImportance() {
        if (this.mSeekBar == null || !this.mSeekBar.isShown()) {
            if (this.mBlock.isChecked()) {
                return 0;
            }
            return this.mSilent.isChecked() ? 2 : -1000;
        } else if (this.mSeekBar.isEnabled()) {
            return this.mSeekBar.getProgress();
        } else {
            return -1000;
        }
    }

    private ColorStateList loadColorStateList(int i) {
        return ColorStateList.valueOf(this.mContext.getColor(i));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTitleAndSummary(int i) {
        switch (i) {
            case 0:
                this.mImportanceSummary.setText(this.mContext.getString(2131493770));
                this.mImportanceTitle.setText(this.mContext.getString(2131493763));
                return;
            case 1:
                this.mImportanceSummary.setText(this.mContext.getString(2131493771));
                this.mImportanceTitle.setText(this.mContext.getString(2131493764));
                return;
            case 2:
                this.mImportanceSummary.setText(this.mContext.getString(2131493772));
                this.mImportanceTitle.setText(this.mContext.getString(2131493765));
                return;
            case 3:
                this.mImportanceSummary.setText(this.mContext.getString(2131493773));
                this.mImportanceTitle.setText(this.mContext.getString(2131493766));
                return;
            case 4:
                this.mImportanceSummary.setText(this.mContext.getString(2131493774));
                this.mImportanceTitle.setText(this.mContext.getString(2131493767));
                return;
            case 5:
                this.mImportanceSummary.setText(this.mContext.getString(2131493775));
                this.mImportanceTitle.setText(this.mContext.getString(2131493768));
                return;
            default:
                return;
        }
    }

    public boolean areGutsExposed() {
        return this.mExposed;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void bindImportance(PackageManager packageManager, StatusBarNotification statusBarNotification, int i) {
        boolean z;
        this.mINotificationManager = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        this.mStartingUserImportance = -1000;
        try {
            this.mStartingUserImportance = this.mINotificationManager.getImportance(statusBarNotification.getPackageName(), statusBarNotification.getUid());
        } catch (RemoteException e) {
        }
        this.mNotificationImportance = i;
        try {
            z = Utils.isSystemPackage(packageManager, packageManager.getPackageInfo(statusBarNotification.getPackageName(), 64));
        } catch (PackageManager.NameNotFoundException e2) {
            z = false;
        }
        View findViewById = findViewById(2131886552);
        View findViewById2 = findViewById(2131886548);
        if (this.mShowSlider) {
            bindSlider(findViewById, z);
            findViewById.setVisibility(0);
            findViewById2.setVisibility(8);
            return;
        }
        bindToggles(findViewById2, this.mStartingUserImportance, z);
        findViewById2.setVisibility(0);
        findViewById.setVisibility(8);
    }

    /* JADX WARN: Code restructure failed: missing block: B:13:0x0029, code lost:
        if (r10 == (-1)) goto L24;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void closeControls(int i, int i2, boolean z) {
        int left;
        int top;
        if (getWindowToken() == null) {
            if (!z || this.mListener == null) {
                return;
            }
            this.mListener.onGutsClosed(this);
            return;
        }
        if (i != -1) {
            left = i;
            top = i2;
        }
        left = (getLeft() + getRight()) / 2;
        top = getTop() + (getHeight() / 2);
        Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(this, left, top, (float) Math.hypot(Math.max(getWidth() - left, left), Math.max(getHeight() - top, top)), 0.0f);
        createCircularReveal.setDuration(360L);
        createCircularReveal.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
        createCircularReveal.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.NotificationGuts.5
            final NotificationGuts this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                this.this$0.setVisibility(8);
            }
        });
        createCircularReveal.start();
        setExposed(false, this.mNeedsFalsingProtection);
        if (!z || this.mListener == null) {
            return;
        }
        this.mListener.onGutsClosed(this);
    }

    @Override // android.view.View
    public void drawableHotspotChanged(float f, float f2) {
        if (this.mBackground != null) {
            this.mBackground.setHotspot(f, f2);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void drawableStateChanged() {
        drawableStateChanged(this.mBackground);
    }

    public int getActualHeight() {
        return this.mActualHeight;
    }

    public boolean hasImportanceChanged() {
        return this.mStartingUserImportance != getSelectedImportance();
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TunerService.get(this.mContext).addTunable(this, "show_importance_slider");
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        TunerService.get(this.mContext).removeTunable(this);
        super.onDetachedFromWindow();
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onDraw(Canvas canvas) {
        draw(canvas, this.mBackground);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mBackground = this.mContext.getDrawable(2130837952);
        if (this.mBackground != null) {
            this.mBackground.setCallback(this);
        }
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("show_importance_slider".equals(str)) {
            boolean z = false;
            if (str2 != null) {
                z = false;
                if (Integer.parseInt(str2) != 0) {
                    z = true;
                }
            }
            this.mShowSlider = z;
        }
    }

    public void resetFalsingCheck() {
        this.mHandler.removeCallbacks(this.mFalsingCheck);
        if (this.mNeedsFalsingProtection && this.mExposed) {
            this.mHandler.postDelayed(this.mFalsingCheck, 8000L);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void saveImportance(StatusBarNotification statusBarNotification) {
        int selectedImportance = getSelectedImportance();
        MetricsLogger.action(this.mContext, 291, selectedImportance - this.mStartingUserImportance);
        try {
            this.mINotificationManager.setImportance(statusBarNotification.getPackageName(), statusBarNotification.getUid(), selectedImportance);
        } catch (RemoteException e) {
        }
    }

    public void setActualHeight(int i) {
        this.mActualHeight = i;
        invalidate();
    }

    public void setClipTopAmount(int i) {
        this.mClipTopAmount = i;
        invalidate();
    }

    public void setClosedListener(OnGutsClosedListener onGutsClosedListener) {
        this.mListener = onGutsClosedListener;
    }

    public void setExposed(boolean z, boolean z2) {
        this.mExposed = z;
        this.mNeedsFalsingProtection = z2;
        if (this.mExposed && this.mNeedsFalsingProtection) {
            resetFalsingCheck();
        } else {
            this.mHandler.removeCallbacks(this.mFalsingCheck);
        }
    }

    @Override // android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        boolean z = true;
        if (!super.verifyDrawable(drawable)) {
            z = drawable == this.mBackground;
        }
        return z;
    }
}
