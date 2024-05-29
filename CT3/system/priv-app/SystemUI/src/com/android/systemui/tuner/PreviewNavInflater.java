package com.android.systemui.tuner;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.android.systemui.statusbar.phone.NavigationBarInflaterView;
/* loaded from: a.zip:com/android/systemui/tuner/PreviewNavInflater.class */
public class PreviewNavInflater extends NavigationBarInflaterView {
    public PreviewNavInflater(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private boolean isValidLayout(String str) {
        boolean z = true;
        if (str == null) {
            return true;
        }
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        while (i3 < str.length()) {
            int i4 = i2;
            int i5 = i;
            if (str.charAt(i3) == ";".charAt(0)) {
                if (i3 == 0 || i3 - i2 == 1) {
                    return false;
                }
                i4 = i3;
                i5 = i + 1;
            }
            i3++;
            i2 = i4;
            i = i5;
        }
        if (i != 2 || str.length() - i2 == 1) {
            z = false;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NavigationBarInflaterView, android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        TunerService.get(getContext()).removeTunable(this);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return true;
    }

    @Override // com.android.systemui.statusbar.phone.NavigationBarInflaterView, com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if (!"sysui_nav_bar".equals(str)) {
            super.onTuningChanged(str, str2);
        } else if (isValidLayout(str2)) {
            super.onTuningChanged(str, str2);
        }
    }
}
