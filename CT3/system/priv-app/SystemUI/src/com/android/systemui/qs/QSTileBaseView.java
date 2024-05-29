package com.android.systemui.qs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.Switch;
import com.android.systemui.qs.QSTile;
/* loaded from: a.zip:com/android/systemui/qs/QSTileBaseView.class */
public class QSTileBaseView extends LinearLayout {
    private String mAccessibilityClass;
    private boolean mCollapsedView;
    private final H mHandler;
    private QSIconView mIcon;
    private RippleDrawable mRipple;
    private Drawable mTileBackground;
    private boolean mTileState;

    /* loaded from: a.zip:com/android/systemui/qs/QSTileBaseView$H.class */
    private class H extends Handler {
        final QSTileBaseView this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public H(QSTileBaseView qSTileBaseView) {
            super(Looper.getMainLooper());
            this.this$0 = qSTileBaseView;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1) {
                this.this$0.handleStateChanged((QSTile.State) message.obj);
            }
        }
    }

    public QSTileBaseView(Context context, QSIconView qSIconView, boolean z) {
        super(context);
        this.mHandler = new H(this);
        this.mIcon = qSIconView;
        addView(this.mIcon);
        this.mTileBackground = newTileBackground();
        if (this.mTileBackground instanceof RippleDrawable) {
            setRipple((RippleDrawable) this.mTileBackground);
        }
        setImportantForAccessibility(1);
        setBackground(this.mTileBackground);
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(2131689827);
        setPadding(0, dimensionPixelSize, 0, dimensionPixelSize);
        setClipChildren(false);
        setClipToPadding(false);
        this.mCollapsedView = z;
        setFocusable(true);
    }

    private Drawable newTileBackground() {
        TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(new int[]{16843868});
        Drawable drawable = obtainStyledAttributes.getDrawable(0);
        obtainStyledAttributes.recycle();
        return drawable;
    }

    private void setRipple(RippleDrawable rippleDrawable) {
        this.mRipple = rippleDrawable;
        if (getWidth() != 0) {
            updateRippleSize(getWidth(), getHeight());
        }
    }

    private void updateRippleSize(int i, int i2) {
        int i3 = i / 2;
        int i4 = i2 / 2;
        int height = (int) (this.mIcon.getHeight() * 0.85f);
        this.mRipple.setHotspotBounds(i3 - height, i4 - height, i3 + height, i4 + height);
    }

    public QSIconView getIcon() {
        return this.mIcon;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void handleStateChanged(QSTile.State state) {
        this.mIcon.setIcon(state);
        if (!this.mCollapsedView || TextUtils.isEmpty(state.minimalContentDescription)) {
            setContentDescription(state.contentDescription);
        } else {
            setContentDescription(state.minimalContentDescription);
        }
        if (this.mCollapsedView) {
            this.mAccessibilityClass = state.minimalAccessibilityClassName;
        } else {
            this.mAccessibilityClass = state.expandedAccessibilityClassName;
        }
        if (state instanceof QSTile.BooleanState) {
            this.mTileState = ((QSTile.BooleanState) state).value;
        }
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public void init(View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener) {
        setClickable(true);
        setOnClickListener(onClickListener);
        setOnLongClickListener(onLongClickListener);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        if (TextUtils.isEmpty(this.mAccessibilityClass)) {
            return;
        }
        accessibilityEvent.setClassName(this.mAccessibilityClass);
        if (Switch.class.getName().equals(this.mAccessibilityClass)) {
            accessibilityEvent.setContentDescription(getResources().getString(!this.mTileState ? 2131493855 : 2131493856));
            accessibilityEvent.setChecked(!this.mTileState);
        }
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (TextUtils.isEmpty(this.mAccessibilityClass)) {
            return;
        }
        accessibilityNodeInfo.setClassName(this.mAccessibilityClass);
        if (Switch.class.getName().equals(this.mAccessibilityClass)) {
            accessibilityNodeInfo.setText(getResources().getString(this.mTileState ? 2131493855 : 2131493856));
            accessibilityNodeInfo.setChecked(this.mTileState);
            accessibilityNodeInfo.setCheckable(true);
        }
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (this.mRipple != null) {
            updateRippleSize(measuredWidth, measuredHeight);
        }
    }

    public void onStateChanged(QSTile.State state) {
        this.mHandler.obtainMessage(1, state).sendToTarget();
    }

    public View updateAccessibilityOrder(View view) {
        setAccessibilityTraversalAfter(view.getId());
        return this;
    }
}
