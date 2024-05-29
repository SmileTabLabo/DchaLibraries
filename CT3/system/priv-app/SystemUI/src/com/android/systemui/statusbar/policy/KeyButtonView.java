package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import com.android.systemui.R$styleable;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/KeyButtonView.class */
public class KeyButtonView extends ImageView {
    private AudioManager mAudioManager;
    private final Runnable mCheckLongPress;
    private int mCode;
    private int mContentDescriptionRes;
    private long mDownTime;
    private boolean mGestureAborted;
    private boolean mLongClicked;
    private boolean mSupportsLongpress;
    private int mTouchSlop;

    public KeyButtonView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyButtonView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet);
        this.mSupportsLongpress = true;
        this.mCheckLongPress = new Runnable(this) { // from class: com.android.systemui.statusbar.policy.KeyButtonView.1
            final KeyButtonView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.isPressed()) {
                    if (this.this$0.isLongClickable()) {
                        this.this$0.performLongClick();
                        this.this$0.mLongClicked = true;
                    } else if (this.this$0.mSupportsLongpress) {
                        this.this$0.sendEvent(0, 128);
                        this.this$0.sendAccessibilityEvent(2);
                        this.this$0.mLongClicked = true;
                    }
                }
            }
        };
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.KeyButtonView, i, 0);
        this.mCode = obtainStyledAttributes.getInteger(1, 0);
        this.mSupportsLongpress = obtainStyledAttributes.getBoolean(2, true);
        TypedValue typedValue = new TypedValue();
        if (obtainStyledAttributes.getValue(0, typedValue)) {
            this.mContentDescriptionRes = typedValue.resourceId;
        }
        obtainStyledAttributes.recycle();
        setClickable(true);
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        setBackground(new KeyButtonRipple(context, this));
    }

    public void abortCurrentGesture() {
        setPressed(false);
        this.mGestureAborted = true;
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.systemui.statusbar.policy.KeyButtonView$2] */
    public void loadAsync(String str) {
        new AsyncTask<String, Void, Drawable>(this) { // from class: com.android.systemui.statusbar.policy.KeyButtonView.2
            final KeyButtonView this$0;

            {
                this.this$0 = this;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Drawable doInBackground(String... strArr) {
                return Icon.createWithContentUri(strArr[0]).loadDrawable(this.this$0.mContext);
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Drawable drawable) {
                this.this$0.setImageDrawable(drawable);
            }
        }.execute(str);
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
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 0) {
            this.mGestureAborted = false;
        }
        if (this.mGestureAborted) {
            return false;
        }
        switch (action) {
            case 0:
                this.mDownTime = SystemClock.uptimeMillis();
                this.mLongClicked = false;
                setPressed(true);
                if (this.mCode != 0) {
                    sendEvent(0, 0, this.mDownTime);
                } else {
                    performHapticFeedback(1);
                }
                removeCallbacks(this.mCheckLongPress);
                postDelayed(this.mCheckLongPress, ViewConfiguration.getLongPressTimeout());
                return true;
            case 1:
                boolean z = isPressed() && !this.mLongClicked;
                setPressed(false);
                if (this.mCode != 0) {
                    if (z) {
                        sendEvent(1, 0);
                        sendAccessibilityEvent(1);
                        playSoundEffect(0);
                    } else {
                        sendEvent(1, 32);
                    }
                } else if (z) {
                    performClick();
                }
                removeCallbacks(this.mCheckLongPress);
                return true;
            case 2:
                int x = (int) motionEvent.getX();
                int y = (int) motionEvent.getY();
                boolean z2 = false;
                if (x >= (-this.mTouchSlop)) {
                    z2 = false;
                    if (x < getWidth() + this.mTouchSlop) {
                        z2 = false;
                        if (y >= (-this.mTouchSlop)) {
                            z2 = false;
                            if (y < getHeight() + this.mTouchSlop) {
                                z2 = true;
                            }
                        }
                    }
                }
                setPressed(z2);
                return true;
            case 3:
                setPressed(false);
                if (this.mCode != 0) {
                    sendEvent(1, 32);
                }
                removeCallbacks(this.mCheckLongPress);
                return true;
            default:
                return true;
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
        } else if (i != 32 || this.mCode == 0) {
            return super.performAccessibilityActionInternal(i, bundle);
        } else {
            sendEvent(0, 128);
            sendEvent(1, 0);
            sendAccessibilityEvent(2);
            return true;
        }
    }

    @Override // android.view.View
    public void playSoundEffect(int i) {
        this.mAudioManager.playSoundEffect(i, ActivityManager.getCurrentUser());
    }

    public void sendEvent(int i, int i2) {
        sendEvent(i, i2, SystemClock.uptimeMillis());
    }

    void sendEvent(int i, int i2, long j) {
        InputManager.getInstance().injectInputEvent(new KeyEvent(this.mDownTime, j, i, this.mCode, (i2 & 128) != 0 ? 1 : 0, 0, -1, 0, i2 | 8 | 64, 257), 0);
    }

    public void setCode(int i) {
        this.mCode = i;
    }
}
