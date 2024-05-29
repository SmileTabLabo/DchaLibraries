package com.android.systemui.statusbar.notification;

import android.text.Layout;
import android.text.TextUtils;
import android.util.Pools;
import android.view.View;
import android.widget.TextView;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/TextViewTransformState.class */
public class TextViewTransformState extends TransformState {
    private static Pools.SimplePool<TextViewTransformState> sInstancePool = new Pools.SimplePool<>(40);
    private TextView mText;

    private int getEllipsisCount() {
        Layout layout = this.mText.getLayout();
        if (layout == null || layout.getLineCount() <= 0) {
            return 0;
        }
        return layout.getEllipsisCount(0);
    }

    private int getInnerHeight(TextView textView) {
        return (textView.getHeight() - textView.getPaddingTop()) - textView.getPaddingBottom();
    }

    public static TextViewTransformState obtain() {
        TextViewTransformState textViewTransformState = (TextViewTransformState) sInstancePool.acquire();
        return textViewTransformState != null ? textViewTransformState : new TextViewTransformState();
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void initFrom(View view) {
        super.initFrom(view);
        if (view instanceof TextView) {
            this.mText = (TextView) view;
        }
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void recycle() {
        super.recycle();
        sInstancePool.release(this);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public void reset() {
        super.reset();
        this.mText = null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public boolean sameAs(TransformState transformState) {
        if (transformState instanceof TextViewTransformState) {
            TextViewTransformState textViewTransformState = (TextViewTransformState) transformState;
            if (TextUtils.equals(textViewTransformState.mText.getText(), this.mText.getText())) {
                boolean z = false;
                if (getEllipsisCount() == textViewTransformState.getEllipsisCount()) {
                    z = false;
                    if (getInnerHeight(this.mText) == getInnerHeight(textViewTransformState.mText)) {
                        z = true;
                    }
                }
                return z;
            }
        }
        return super.sameAs(transformState);
    }
}
