package com.android.systemui.tuner;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.widget.ScrollView;
/* loaded from: a.zip:com/android/systemui/tuner/AutoScrollView.class */
public class AutoScrollView extends ScrollView {
    public AutoScrollView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // android.view.View
    public boolean onDragEvent(DragEvent dragEvent) {
        switch (dragEvent.getAction()) {
            case 2:
                int y = (int) dragEvent.getY();
                int height = getHeight();
                int i = (int) (height * 0.1f);
                if (y < i) {
                    scrollBy(0, y - i);
                    return false;
                } else if (y > height - i) {
                    scrollBy(0, (y - height) + i);
                    return false;
                } else {
                    return false;
                }
            default:
                return false;
        }
    }
}
