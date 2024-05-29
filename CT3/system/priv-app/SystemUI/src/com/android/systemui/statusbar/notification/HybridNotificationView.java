package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.keyguard.AlphaOptimizedLinearLayout;
import com.android.systemui.ViewInvertHelper;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/HybridNotificationView.class */
public class HybridNotificationView extends AlphaOptimizedLinearLayout implements TransformableView {
    private ViewInvertHelper mInvertHelper;
    protected TextView mTextView;
    protected TextView mTitleView;
    private ViewTransformationHelper mTransformationHelper;

    public HybridNotificationView(Context context) {
        this(context, null);
    }

    public HybridNotificationView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public HybridNotificationView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public HybridNotificationView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    public void bind(CharSequence charSequence, CharSequence charSequence2) {
        this.mTitleView.setText(charSequence);
        this.mTitleView.setVisibility(TextUtils.isEmpty(charSequence) ? 8 : 0);
        if (TextUtils.isEmpty(charSequence2)) {
            this.mTextView.setVisibility(8);
            this.mTextView.setText((CharSequence) null);
        } else {
            this.mTextView.setVisibility(0);
            this.mTextView.setText(charSequence2.toString());
        }
        requestLayout();
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public TransformState getCurrentState(int i) {
        return this.mTransformationHelper.getCurrentState(i);
    }

    public TextView getTextView() {
        return this.mTextView;
    }

    public TextView getTitleView() {
        return this.mTitleView;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTitleView = (TextView) findViewById(2131886284);
        this.mTextView = (TextView) findViewById(2131886285);
        this.mInvertHelper = new ViewInvertHelper(this, 700L);
        this.mTransformationHelper = new ViewTransformationHelper();
        this.mTransformationHelper.setCustomTransformation(new ViewTransformationHelper.CustomTransformation(this) { // from class: com.android.systemui.statusbar.notification.HybridNotificationView.1
            final HybridNotificationView this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
            public boolean transformFrom(TransformState transformState, TransformableView transformableView, float f) {
                TransformState currentState = transformableView.getCurrentState(1);
                CrossFadeHelper.fadeIn(this.this$0.mTextView, f);
                if (currentState != null) {
                    transformState.transformViewVerticalFrom(currentState, f);
                    currentState.recycle();
                    return true;
                }
                return true;
            }

            @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
            public boolean transformTo(TransformState transformState, TransformableView transformableView, float f) {
                TransformState currentState = transformableView.getCurrentState(1);
                CrossFadeHelper.fadeOut(this.this$0.mTextView, f);
                if (currentState != null) {
                    transformState.transformViewVerticalTo(currentState, f);
                    currentState.recycle();
                    return true;
                }
                return true;
            }
        }, 2);
        this.mTransformationHelper.addTransformedView(1, this.mTitleView);
        this.mTransformationHelper.addTransformedView(2, this.mTextView);
    }

    public void setDark(boolean z, boolean z2, long j) {
        this.mInvertHelper.setInverted(z, z2, j);
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void setVisible(boolean z) {
        setVisibility(z ? 0 : 4);
        this.mTransformationHelper.setVisible(z);
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformFrom(TransformableView transformableView) {
        this.mTransformationHelper.transformFrom(transformableView);
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformFrom(TransformableView transformableView, float f) {
        this.mTransformationHelper.transformFrom(transformableView, f);
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformTo(TransformableView transformableView, float f) {
        this.mTransformationHelper.transformTo(transformableView, f);
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformTo(TransformableView transformableView, Runnable runnable) {
        this.mTransformationHelper.transformTo(transformableView, runnable);
    }
}
