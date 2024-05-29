package com.android.systemui.qs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;
/* loaded from: a.zip:com/android/systemui/qs/NonInterceptingScrollView.class */
public class NonInterceptingScrollView extends ScrollView {
    public NonInterceptingScrollView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // android.widget.ScrollView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case 0:
                if (canScrollVertically(1)) {
                    requestDisallowInterceptTouchEvent(true);
                    break;
                }
                break;
        }
        return super.onTouchEvent(motionEvent);
    }
}
