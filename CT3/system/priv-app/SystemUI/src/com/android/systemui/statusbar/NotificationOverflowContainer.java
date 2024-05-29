package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.ViewInvertHelper;
/* loaded from: a.zip:com/android/systemui/statusbar/NotificationOverflowContainer.class */
public class NotificationOverflowContainer extends ActivatableNotificationView {
    private View mContent;
    private boolean mDark;
    private NotificationOverflowIconsView mIconsView;
    private ViewInvertHelper mViewInvertHelper;

    public NotificationOverflowContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView
    protected View getContentView() {
        return this.mContent;
    }

    public NotificationOverflowIconsView getIconsView() {
        return this.mIconsView;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.ActivatableNotificationView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mIconsView = (NotificationOverflowIconsView) findViewById(2131886697);
        this.mIconsView.setMoreText((TextView) findViewById(2131886695));
        this.mIconsView.setOverflowIndicator(findViewById(2131886696));
        this.mContent = findViewById(2131886694);
        this.mViewInvertHelper = new ViewInvertHelper(this.mContent, 700L);
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView, com.android.systemui.statusbar.ExpandableView
    public void setDark(boolean z, boolean z2, long j) {
        super.setDark(z, z2, j);
        if (this.mDark == z) {
            return;
        }
        this.mDark = z;
        if (z2) {
            this.mViewInvertHelper.fade(z, j);
        } else {
            this.mViewInvertHelper.update(z);
        }
    }
}
