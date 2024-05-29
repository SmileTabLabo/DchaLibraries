package com.android.systemui.stackdivider;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
/* loaded from: a.zip:com/android/systemui/stackdivider/DividerWindowManager.class */
public class DividerWindowManager {
    private WindowManager.LayoutParams mLp;
    private View mView;
    private final WindowManager mWindowManager;

    public DividerWindowManager(Context context) {
        this.mWindowManager = (WindowManager) context.getSystemService(WindowManager.class);
    }

    public void add(View view, int i, int i2) {
        this.mLp = new WindowManager.LayoutParams(i, i2, 2034, 545521704, -3);
        this.mLp.setTitle("DockedStackDivider");
        this.mLp.privateFlags |= 64;
        view.setSystemUiVisibility(1792);
        this.mWindowManager.addView(view, this.mLp);
        this.mView = view;
    }

    public void remove() {
        if (this.mView != null) {
            this.mWindowManager.removeView(this.mView);
        }
        this.mView = null;
    }

    public void setSlippery(boolean z) {
        boolean z2;
        if (z && (this.mLp.flags & 536870912) == 0) {
            this.mLp.flags |= 536870912;
            z2 = true;
        } else {
            z2 = false;
            if (!z) {
                z2 = false;
                if ((this.mLp.flags & 536870912) != 0) {
                    this.mLp.flags &= -536870913;
                    z2 = true;
                }
            }
        }
        if (z2) {
            this.mWindowManager.updateViewLayout(this.mView, this.mLp);
        }
    }

    public void setTouchable(boolean z) {
        boolean z2;
        if (z || (this.mLp.flags & 16) != 0) {
            z2 = false;
            if (z) {
                z2 = false;
                if ((this.mLp.flags & 16) != 0) {
                    this.mLp.flags &= -17;
                    z2 = true;
                }
            }
        } else {
            this.mLp.flags |= 16;
            z2 = true;
        }
        if (z2) {
            this.mWindowManager.updateViewLayout(this.mView, this.mLp);
        }
    }
}
