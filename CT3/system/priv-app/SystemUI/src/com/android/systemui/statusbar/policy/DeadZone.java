package com.android.systemui.statusbar.policy;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.View;
import com.android.systemui.R$styleable;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/DeadZone.class */
public class DeadZone extends View {
    private final Runnable mDebugFlash;
    private int mDecay;
    private float mFlashFrac;
    private int mHold;
    private long mLastPokeTime;
    private boolean mShouldFlash;
    private int mSizeMax;
    private int mSizeMin;
    private boolean mVertical;

    public DeadZone(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public DeadZone(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet);
        boolean z = true;
        this.mFlashFrac = 0.0f;
        this.mDebugFlash = new Runnable(this) { // from class: com.android.systemui.statusbar.policy.DeadZone.1
            final DeadZone this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                ObjectAnimator.ofFloat(this.this$0, "flash", 1.0f, 0.0f).setDuration(150L).start();
            }
        };
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.DeadZone, i, 0);
        this.mHold = obtainStyledAttributes.getInteger(2, 0);
        this.mDecay = obtainStyledAttributes.getInteger(3, 0);
        this.mSizeMin = obtainStyledAttributes.getDimensionPixelSize(0, 0);
        this.mSizeMax = obtainStyledAttributes.getDimensionPixelSize(1, 0);
        this.mVertical = obtainStyledAttributes.getInt(4, -1) != 1 ? false : z;
        setFlashOnTouchCapture(context.getResources().getBoolean(2131623952));
    }

    private float getSize(long j) {
        if (this.mSizeMax == 0) {
            return 0.0f;
        }
        long j2 = j - this.mLastPokeTime;
        return j2 > ((long) (this.mHold + this.mDecay)) ? this.mSizeMin : j2 < ((long) this.mHold) ? this.mSizeMax : (int) lerp(this.mSizeMax, this.mSizeMin, ((float) (j2 - this.mHold)) / this.mDecay);
    }

    static float lerp(float f, float f2, float f3) {
        return ((f2 - f) * f3) + f;
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        if (!this.mShouldFlash || this.mFlashFrac <= 0.0f) {
            return;
        }
        int size = (int) getSize(SystemClock.uptimeMillis());
        int width = this.mVertical ? size : canvas.getWidth();
        if (this.mVertical) {
            size = canvas.getHeight();
        }
        canvas.clipRect(0, 0, width, size);
        canvas.drawARGB((int) (255.0f * this.mFlashFrac), 221, 238, 170);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getToolType(0) == 3) {
            return false;
        }
        int action = motionEvent.getAction();
        if (action == 4) {
            poke(motionEvent);
            return false;
        } else if (action == 0) {
            int size = (int) getSize(motionEvent.getEventTime());
            if (!this.mVertical ? motionEvent.getY() >= ((float) size) : motionEvent.getX() >= ((float) size)) {
                Slog.v("DeadZone", "consuming errant click: (" + motionEvent.getX() + "," + motionEvent.getY() + ")");
                if (this.mShouldFlash) {
                    post(this.mDebugFlash);
                    postInvalidate();
                    return true;
                }
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    public void poke(MotionEvent motionEvent) {
        this.mLastPokeTime = motionEvent.getEventTime();
        if (this.mShouldFlash) {
            postInvalidate();
        }
    }

    public void setFlashOnTouchCapture(boolean z) {
        this.mShouldFlash = z;
        this.mFlashFrac = 0.0f;
        postInvalidate();
    }
}
