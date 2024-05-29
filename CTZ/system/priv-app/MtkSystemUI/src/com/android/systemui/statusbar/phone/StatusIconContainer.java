package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import com.android.keyguard.AlphaOptimizedLinearLayout;
import com.android.systemui.R;
import com.android.systemui.statusbar.StatusIconDisplayable;
import com.android.systemui.statusbar.stack.AnimationFilter;
import com.android.systemui.statusbar.stack.AnimationProperties;
import com.android.systemui.statusbar.stack.ViewState;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class StatusIconContainer extends AlphaOptimizedLinearLayout {
    private static final AnimationProperties ADD_ICON_PROPERTIES = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.StatusIconContainer.1
        private AnimationFilter mAnimationFilter = new AnimationFilter().animateAlpha();

        @Override // com.android.systemui.statusbar.stack.AnimationProperties
        public AnimationFilter getAnimationFilter() {
            return this.mAnimationFilter;
        }
    }.setDuration(200).setDelay(50);
    private static final AnimationProperties DOT_ANIMATION_PROPERTIES = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.StatusIconContainer.2
        private AnimationFilter mAnimationFilter = new AnimationFilter().animateX();

        @Override // com.android.systemui.statusbar.stack.AnimationProperties
        public AnimationFilter getAnimationFilter() {
            return this.mAnimationFilter;
        }
    }.setDuration(200);
    private int mDotPadding;
    private int mIconDotFrameWidth;
    private ArrayList<StatusIconState> mLayoutStates;
    private ArrayList<View> mMeasureViews;
    private boolean mNeedsUnderflow;
    private boolean mShouldRestrictIcons;
    private int mStaticDotDiameter;
    private int mUnderflowStart;
    private int mUnderflowWidth;

    public StatusIconContainer(Context context) {
        this(context, null);
    }

    public StatusIconContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mUnderflowStart = 0;
        this.mShouldRestrictIcons = true;
        this.mLayoutStates = new ArrayList<>();
        this.mMeasureViews = new ArrayList<>();
        initDimens();
        setWillNotDraw(true);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setShouldRestrictIcons(boolean z) {
        this.mShouldRestrictIcons = z;
    }

    public boolean isRestrictingIcons() {
        return this.mShouldRestrictIcons;
    }

    private void initDimens() {
        this.mIconDotFrameWidth = getResources().getDimensionPixelSize(17105312);
        this.mDotPadding = getResources().getDimensionPixelSize(R.dimen.overflow_icon_dot_padding);
        this.mStaticDotDiameter = 2 * getResources().getDimensionPixelSize(R.dimen.overflow_dot_radius);
        this.mUnderflowWidth = this.mIconDotFrameWidth + (0 * (this.mStaticDotDiameter + this.mDotPadding));
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        float height = getHeight() / 2.0f;
        for (int i5 = 0; i5 < getChildCount(); i5++) {
            View childAt = getChildAt(i5);
            int measuredWidth = childAt.getMeasuredWidth();
            int measuredHeight = childAt.getMeasuredHeight();
            int i6 = (int) (height - (measuredHeight / 2.0f));
            childAt.layout(0, i6, measuredWidth, measuredHeight + i6);
        }
        resetViewStates();
        calculateIconTranslations();
        applyIconStates();
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int i3;
        this.mMeasureViews.clear();
        int mode = View.MeasureSpec.getMode(i);
        int size = View.MeasureSpec.getSize(i);
        int childCount = getChildCount();
        for (int i4 = 0; i4 < childCount; i4++) {
            StatusIconDisplayable statusIconDisplayable = (StatusIconDisplayable) getChildAt(i4);
            if (statusIconDisplayable.isIconVisible() && !statusIconDisplayable.isIconBlocked()) {
                this.mMeasureViews.add((View) statusIconDisplayable);
            }
        }
        int size2 = this.mMeasureViews.size();
        if (size2 > 7) {
            i3 = 6;
        } else {
            i3 = 7;
        }
        int i5 = this.mPaddingLeft + this.mPaddingRight;
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, 0);
        this.mNeedsUnderflow = this.mShouldRestrictIcons && size2 > 7;
        boolean z = true;
        for (int i6 = 0; i6 < this.mMeasureViews.size(); i6++) {
            View view = this.mMeasureViews.get((size2 - i6) - 1);
            measureChild(view, makeMeasureSpec, i2);
            if (this.mShouldRestrictIcons) {
                if (i6 < i3 && z) {
                    i5 += getViewTotalMeasuredWidth(view);
                } else if (z) {
                    i5 += this.mUnderflowWidth;
                    z = false;
                }
            } else {
                i5 += getViewTotalMeasuredWidth(view);
            }
        }
        if (mode == 1073741824) {
            if (!this.mNeedsUnderflow && i5 > size) {
                this.mNeedsUnderflow = true;
            }
            setMeasuredDimension(size, View.MeasureSpec.getSize(i2));
            return;
        }
        if (mode == Integer.MIN_VALUE && i5 > size) {
            this.mNeedsUnderflow = true;
        } else {
            size = i5;
        }
        setMeasuredDimension(size, View.MeasureSpec.getSize(i2));
    }

    @Override // android.view.ViewGroup
    public void onViewAdded(View view) {
        super.onViewAdded(view);
        StatusIconState statusIconState = new StatusIconState();
        statusIconState.justAdded = true;
        view.setTag(R.id.status_bar_view_state_tag, statusIconState);
    }

    @Override // android.view.ViewGroup
    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        view.setTag(R.id.status_bar_view_state_tag, null);
    }

    private void calculateIconTranslations() {
        int i;
        View childAt;
        this.mLayoutStates.clear();
        float width = getWidth();
        float paddingEnd = width - getPaddingEnd();
        float paddingStart = getPaddingStart();
        int childCount = getChildCount();
        int i2 = childCount - 1;
        while (true) {
            if (i2 < 0) {
                break;
            }
            View childAt2 = getChildAt(i2);
            StatusIconDisplayable statusIconDisplayable = (StatusIconDisplayable) childAt2;
            StatusIconState viewStateFromChild = getViewStateFromChild(childAt2);
            if (!statusIconDisplayable.isIconVisible() || statusIconDisplayable.isIconBlocked()) {
                viewStateFromChild.visibleState = 2;
            } else {
                viewStateFromChild.visibleState = 0;
                viewStateFromChild.xTranslation = paddingEnd - getViewTotalWidth(childAt2);
                this.mLayoutStates.add(0, viewStateFromChild);
                paddingEnd -= getViewTotalWidth(childAt2);
            }
            i2--;
        }
        int size = this.mLayoutStates.size();
        int i3 = size > 7 ? 6 : 7;
        this.mUnderflowStart = 0;
        int i4 = size - 1;
        int i5 = 0;
        while (true) {
            if (i4 >= 0) {
                StatusIconState statusIconState = this.mLayoutStates.get(i4);
                if ((this.mNeedsUnderflow && statusIconState.xTranslation < this.mUnderflowWidth + paddingStart) || (this.mShouldRestrictIcons && i5 >= i3)) {
                    break;
                }
                this.mUnderflowStart = (int) Math.max(paddingStart, statusIconState.xTranslation - this.mUnderflowWidth);
                i5++;
                i4--;
            } else {
                i4 = -1;
                break;
            }
        }
        if (i4 != -1) {
            int i6 = this.mStaticDotDiameter + this.mDotPadding;
            int i7 = (this.mUnderflowStart + this.mUnderflowWidth) - this.mIconDotFrameWidth;
            int i8 = 0;
            while (i4 >= 0) {
                StatusIconState statusIconState2 = this.mLayoutStates.get(i4);
                if (i8 < 1) {
                    statusIconState2.xTranslation = i7;
                    statusIconState2.visibleState = 1;
                    i7 -= i6;
                    i8++;
                } else {
                    statusIconState2.visibleState = 2;
                }
                i4--;
            }
        }
        if (isLayoutRtl()) {
            for (i = 0; i < childCount; i++) {
                StatusIconState viewStateFromChild2 = getViewStateFromChild(getChildAt(i));
                viewStateFromChild2.xTranslation = (width - viewStateFromChild2.xTranslation) - childAt.getWidth();
            }
        }
    }

    private void applyIconStates() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            StatusIconState viewStateFromChild = getViewStateFromChild(childAt);
            if (viewStateFromChild != null) {
                viewStateFromChild.applyToView(childAt);
            }
        }
    }

    private void resetViewStates() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            StatusIconState viewStateFromChild = getViewStateFromChild(childAt);
            if (viewStateFromChild != null) {
                viewStateFromChild.initFrom(childAt);
                viewStateFromChild.alpha = 1.0f;
                if (childAt instanceof StatusIconDisplayable) {
                    viewStateFromChild.hidden = !((StatusIconDisplayable) childAt).isIconVisible();
                } else {
                    viewStateFromChild.hidden = false;
                }
            }
        }
    }

    private static StatusIconState getViewStateFromChild(View view) {
        return (StatusIconState) view.getTag(R.id.status_bar_view_state_tag);
    }

    private static int getViewTotalMeasuredWidth(View view) {
        return view.getMeasuredWidth() + view.getPaddingStart() + view.getPaddingEnd();
    }

    private static int getViewTotalWidth(View view) {
        return view.getWidth() + view.getPaddingStart() + view.getPaddingEnd();
    }

    /* loaded from: classes.dex */
    public static class StatusIconState extends ViewState {
        public int visibleState = 0;
        public boolean justAdded = true;

        @Override // com.android.systemui.statusbar.stack.ViewState
        public void applyToView(View view) {
            if (!(view instanceof StatusIconDisplayable)) {
                return;
            }
            StatusIconDisplayable statusIconDisplayable = (StatusIconDisplayable) view;
            AnimationProperties animationProperties = null;
            boolean z = true;
            if (this.justAdded) {
                super.applyToView(view);
                animationProperties = StatusIconContainer.ADD_ICON_PROPERTIES;
            } else if (statusIconDisplayable.getVisibleState() != this.visibleState) {
                animationProperties = StatusIconContainer.DOT_ANIMATION_PROPERTIES;
            } else {
                z = false;
            }
            if (z) {
                animateTo(view, animationProperties);
                statusIconDisplayable.setVisibleState(this.visibleState);
            } else {
                statusIconDisplayable.setVisibleState(this.visibleState);
                super.applyToView(view);
            }
            this.justAdded = false;
        }
    }
}
