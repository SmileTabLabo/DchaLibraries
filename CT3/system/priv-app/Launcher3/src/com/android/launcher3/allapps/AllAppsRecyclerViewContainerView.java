package com.android.launcher3.allapps;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.ClickShadowView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Launcher;
/* loaded from: a.zip:com/android/launcher3/allapps/AllAppsRecyclerViewContainerView.class */
public class AllAppsRecyclerViewContainerView extends FrameLayout implements BubbleTextView.BubbleTextShadowHandler {
    private final ClickShadowView mTouchFeedbackView;

    public AllAppsRecyclerViewContainerView(Context context) {
        this(context, null);
    }

    public AllAppsRecyclerViewContainerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AllAppsRecyclerViewContainerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        DeviceProfile deviceProfile = ((Launcher) context).getDeviceProfile();
        this.mTouchFeedbackView = new ClickShadowView(context);
        int extraSize = deviceProfile.allAppsIconSizePx + this.mTouchFeedbackView.getExtraSize();
        addView(this.mTouchFeedbackView, extraSize, extraSize);
    }

    @Override // com.android.launcher3.BubbleTextView.BubbleTextShadowHandler
    public void setPressedIcon(BubbleTextView bubbleTextView, Bitmap bitmap) {
        if (bubbleTextView == null || bitmap == null) {
            this.mTouchFeedbackView.setBitmap(null);
            this.mTouchFeedbackView.animate().cancel();
        } else if (this.mTouchFeedbackView.setBitmap(bitmap)) {
            this.mTouchFeedbackView.alignWithIconView(bubbleTextView, (ViewGroup) bubbleTextView.getParent());
            this.mTouchFeedbackView.animateShadow();
        }
    }
}
