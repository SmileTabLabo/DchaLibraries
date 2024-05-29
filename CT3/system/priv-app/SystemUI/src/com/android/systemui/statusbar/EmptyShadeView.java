package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
/* loaded from: a.zip:com/android/systemui/statusbar/EmptyShadeView.class */
public class EmptyShadeView extends StackScrollerDecorView {
    public EmptyShadeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // com.android.systemui.statusbar.StackScrollerDecorView
    protected View findContentView() {
        return findViewById(2131886690);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        ((TextView) findViewById(2131886690)).setText(2131493656);
    }
}
