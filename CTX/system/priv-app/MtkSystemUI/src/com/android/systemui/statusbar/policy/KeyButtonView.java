package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.metrics.LogMaker;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Dependency;
import com.android.systemui.OverviewProxyService;
import com.android.systemui.R;
import com.android.systemui.plugins.statusbar.phone.NavBarButtonProvider;
import com.android.systemui.shared.system.NavigationBarCompat;
/* loaded from: classes.dex */
public class KeyButtonView extends ImageView implements NavBarButtonProvider.ButtonInterface {
    private static final String TAG = KeyButtonView.class.getSimpleName();
    private AudioManager mAudioManager;
    private final Runnable mCheckLongPress;
    private int mCode;
    private int mContentDescriptionRes;
    private long mDownTime;
    private boolean mGestureAborted;
    private boolean mIsVertical;
    private boolean mLongClicked;
    private final MetricsLogger mMetricsLogger;
    private View.OnClickListener mOnClickListener;
    private final OverviewProxyService mOverviewProxyService;
    private final boolean mPlaySounds;
    private final KeyButtonRipple mRipple;
    private boolean mSupportsLongpress;
    private int mTouchDownX;
    private int mTouchDownY;

    public KeyButtonView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyButtonView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet);
        this.mSupportsLongpress = true;
        this.mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
        this.mCheckLongPress = new Runnable() { // from class: com.android.systemui.statusbar.policy.KeyButtonView.1
            @Override // java.lang.Runnable
            public void run() {
                if (KeyButtonView.this.isPressed()) {
                    if (!KeyButtonView.this.isLongClickable()) {
                        if (KeyButtonView.this.mSupportsLongpress) {
                            KeyButtonView.this.sendEvent(0, 128);
                            KeyButtonView.this.sendAccessibilityEvent(2);
                            KeyButtonView.this.mLongClicked = true;
                            return;
                        }
                        return;
                    }
                    KeyButtonView.this.performLongClick();
                    KeyButtonView.this.mLongClicked = true;
                }
            }
        };
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.KeyButtonView, i, 0);
        this.mCode = obtainStyledAttributes.getInteger(1, 0);
        this.mSupportsLongpress = obtainStyledAttributes.getBoolean(2, true);
        this.mPlaySounds = obtainStyledAttributes.getBoolean(3, true);
        TypedValue typedValue = new TypedValue();
        if (obtainStyledAttributes.getValue(0, typedValue)) {
            this.mContentDescriptionRes = typedValue.resourceId;
        }
        obtainStyledAttributes.recycle();
        setClickable(true);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mRipple = new KeyButtonRipple(context, this);
        this.mOverviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
        setBackground(this.mRipple);
        forceHasOverlappingRendering(false);
    }

    @Override // android.view.View
    public boolean isClickable() {
        return this.mCode != 0 || super.isClickable();
    }

    public void setCode(int i) {
        this.mCode = i;
    }

    @Override // android.view.View
    public void setOnClickListener(View.OnClickListener onClickListener) {
        super.setOnClickListener(onClickListener);
        this.mOnClickListener = onClickListener;
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.systemui.statusbar.policy.KeyButtonView$2] */
    public void loadAsync(Icon icon) {
        new AsyncTask<Icon, Void, Drawable>() { // from class: com.android.systemui.statusbar.policy.KeyButtonView.2
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Drawable doInBackground(Icon... iconArr) {
                return iconArr[0].loadDrawable(KeyButtonView.this.mContext);
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Drawable drawable) {
                KeyButtonView.this.setImageDrawable(drawable);
            }
        }.execute(icon);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mContentDescriptionRes != 0) {
            setContentDescription(this.mContext.getString(this.mContentDescriptionRes));
        }
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (this.mCode != 0) {
            accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, null));
            if (this.mSupportsLongpress || isLongClickable()) {
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(32, null));
            }
        }
    }

    @Override // android.view.View
    protected void onWindowVisibilityChanged(int i) {
        super.onWindowVisibilityChanged(i);
        if (i != 0) {
            jumpDrawablesToCurrentState();
        }
    }

    public boolean performAccessibilityActionInternal(int i, Bundle bundle) {
        if (i == 16 && this.mCode != 0) {
            sendEvent(0, 0, SystemClock.uptimeMillis());
            sendEvent(1, 0);
            sendAccessibilityEvent(1);
            playSoundEffect(0);
            return true;
        } else if (i == 32 && this.mCode != 0) {
            sendEvent(0, 128);
            sendEvent(1, 0);
            sendAccessibilityEvent(2);
            return true;
        } else {
            return super.performAccessibilityActionInternal(i, bundle);
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int quickStepTouchSlopPx;
        int quickScrubTouchSlopPx;
        boolean shouldShowSwipeUpUI = this.mOverviewProxyService.shouldShowSwipeUpUI();
        int action = motionEvent.getAction();
        if (action == 0) {
            this.mGestureAborted = false;
        }
        if (this.mGestureAborted) {
            setPressed(false);
            return false;
        }
        switch (action) {
            case 0:
                this.mDownTime = SystemClock.uptimeMillis();
                this.mLongClicked = false;
                setPressed(true);
                this.mTouchDownX = (int) motionEvent.getRawX();
                this.mTouchDownY = (int) motionEvent.getRawY();
                if (this.mCode != 0) {
                    sendEvent(0, 0, this.mDownTime);
                } else {
                    performHapticFeedback(1);
                }
                if (!shouldShowSwipeUpUI) {
                    playSoundEffect(0);
                }
                removeCallbacks(this.mCheckLongPress);
                postDelayed(this.mCheckLongPress, ViewConfiguration.getLongPressTimeout());
                break;
            case 1:
                boolean z = isPressed() && !this.mLongClicked;
                setPressed(false);
                boolean z2 = SystemClock.uptimeMillis() - this.mDownTime > 150;
                if (shouldShowSwipeUpUI) {
                    if (z) {
                        performHapticFeedback(1);
                        playSoundEffect(0);
                    }
                } else if (z2 && !this.mLongClicked) {
                    performHapticFeedback(8);
                }
                if (this.mCode != 0) {
                    if (z) {
                        sendEvent(1, 0);
                        sendAccessibilityEvent(1);
                    } else {
                        sendEvent(1, 32);
                    }
                } else if (z && this.mOnClickListener != null) {
                    this.mOnClickListener.onClick(this);
                    sendAccessibilityEvent(1);
                }
                removeCallbacks(this.mCheckLongPress);
                break;
            case 2:
                int rawY = (int) motionEvent.getRawY();
                int abs = Math.abs(((int) motionEvent.getRawX()) - this.mTouchDownX);
                if (this.mIsVertical) {
                    quickStepTouchSlopPx = NavigationBarCompat.getQuickScrubTouchSlopPx();
                } else {
                    quickStepTouchSlopPx = NavigationBarCompat.getQuickStepTouchSlopPx();
                }
                boolean z3 = abs > quickStepTouchSlopPx;
                int abs2 = Math.abs(rawY - this.mTouchDownY);
                if (this.mIsVertical) {
                    quickScrubTouchSlopPx = NavigationBarCompat.getQuickStepTouchSlopPx();
                } else {
                    quickScrubTouchSlopPx = NavigationBarCompat.getQuickScrubTouchSlopPx();
                }
                boolean z4 = abs2 > quickScrubTouchSlopPx;
                if (z3 || z4) {
                    setPressed(false);
                    removeCallbacks(this.mCheckLongPress);
                    break;
                }
            case 3:
                setPressed(false);
                if (this.mCode != 0) {
                    sendEvent(1, 32);
                }
                removeCallbacks(this.mCheckLongPress);
                break;
        }
        return true;
    }

    @Override // android.view.View
    public void playSoundEffect(int i) {
        if (this.mPlaySounds) {
            this.mAudioManager.playSoundEffect(i, ActivityManager.getCurrentUser());
        }
    }

    public void sendEvent(int i, int i2) {
        sendEvent(i, i2, SystemClock.uptimeMillis());
    }

    void sendEvent(int i, int i2, long j) {
        this.mMetricsLogger.write(new LogMaker(931).setType(4).setSubtype(this.mCode).addTaggedData(933, Integer.valueOf(i)).addTaggedData(932, Integer.valueOf(i2)));
        InputManager.getInstance().injectInputEvent(new KeyEvent(this.mDownTime, j, i, this.mCode, (i2 & 128) != 0 ? 1 : 0, 0, -1, 0, i2 | 8 | 64, 257), 0);
    }

    @Override // com.android.systemui.plugins.statusbar.phone.NavBarButtonProvider.ButtonInterface
    public void abortCurrentGesture() {
        setPressed(false);
        this.mRipple.abortDelayedRipple();
        this.mGestureAborted = true;
    }

    @Override // com.android.systemui.plugins.statusbar.phone.NavBarButtonProvider.ButtonInterface
    public void setDarkIntensity(float f) {
        if (getDrawable() != null) {
            ((KeyButtonDrawable) getDrawable()).setDarkIntensity(f);
            invalidate();
        }
        this.mRipple.setDarkIntensity(f);
    }

    @Override // com.android.systemui.plugins.statusbar.phone.NavBarButtonProvider.ButtonInterface
    public void setDelayTouchFeedback(boolean z) {
        this.mRipple.setDelayTouchFeedback(z);
    }

    @Override // com.android.systemui.plugins.statusbar.phone.NavBarButtonProvider.ButtonInterface
    public void setVertical(boolean z) {
        this.mIsVertical = z;
    }
}
