package com.android.systemui.statusbar.policy;

import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextPaint;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.NotificationColorUtil;
import com.android.keyguard.KeyguardHostView;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;
import java.text.BreakIterator;
import java.util.Comparator;
import java.util.PriorityQueue;
/* loaded from: classes.dex */
public class SmartReplyView extends ViewGroup {
    private final BreakIterator mBreakIterator;
    private PriorityQueue<Button> mCandidateButtonQueueForSqueezing;
    private final SmartReplyConstants mConstants;
    private int mCurrentBackgroundColor;
    private final int mDefaultBackgroundColor;
    private final int mDefaultStrokeColor;
    private final int mDefaultTextColor;
    private final int mDefaultTextColorDarkBg;
    private final int mDoubleLineButtonPaddingHorizontal;
    private final int mHeightUpperLimit;
    private final KeyguardDismissUtil mKeyguardDismissUtil;
    private final double mMinStrokeContrast;
    private final int mRippleColor;
    private final int mRippleColorDarkBg;
    private final int mSingleLineButtonPaddingHorizontal;
    private final int mSingleToDoubleLineButtonWidthIncrease;
    private View mSmartReplyContainer;
    private final int mSpacing;
    private final int mStrokeWidth;
    private static final int MEASURE_SPEC_ANY_WIDTH = View.MeasureSpec.makeMeasureSpec(0, 0);
    private static final Comparator<View> DECREASING_MEASURED_WIDTH_WITHOUT_PADDING_COMPARATOR = new Comparator() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$SmartReplyView$UA3QkbRzztEFRlbb86djKcGIV5E
        @Override // java.util.Comparator
        public final int compare(Object obj, Object obj2) {
            return SmartReplyView.lambda$static$0((View) obj, (View) obj2);
        }
    };

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ int lambda$static$0(View view, View view2) {
        return ((view2.getMeasuredWidth() - view2.getPaddingLeft()) - view2.getPaddingRight()) - ((view.getMeasuredWidth() - view.getPaddingLeft()) - view.getPaddingRight());
    }

    public SmartReplyView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mConstants = (SmartReplyConstants) Dependency.get(SmartReplyConstants.class);
        this.mKeyguardDismissUtil = (KeyguardDismissUtil) Dependency.get(KeyguardDismissUtil.class);
        this.mHeightUpperLimit = NotificationUtils.getFontScaledHeight(this.mContext, R.dimen.smart_reply_button_max_height);
        this.mCurrentBackgroundColor = context.getColor(R.color.smart_reply_button_background);
        this.mDefaultBackgroundColor = this.mCurrentBackgroundColor;
        this.mDefaultTextColor = this.mContext.getColor(R.color.smart_reply_button_text);
        this.mDefaultTextColorDarkBg = this.mContext.getColor(R.color.smart_reply_button_text_dark_bg);
        this.mDefaultStrokeColor = this.mContext.getColor(R.color.smart_reply_button_stroke);
        this.mRippleColor = this.mContext.getColor(R.color.notification_ripple_untinted_color);
        this.mRippleColorDarkBg = Color.argb(Color.alpha(this.mRippleColor), 255, 255, 255);
        this.mMinStrokeContrast = NotificationColorUtil.calculateContrast(this.mDefaultStrokeColor, this.mDefaultBackgroundColor);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.SmartReplyView, 0, 0);
        int indexCount = obtainStyledAttributes.getIndexCount();
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        for (int i5 = 0; i5 < indexCount; i5++) {
            switch (obtainStyledAttributes.getIndex(i5)) {
                case 0:
                    i = obtainStyledAttributes.getDimensionPixelSize(i5, 0);
                    break;
                case 1:
                    i4 = obtainStyledAttributes.getDimensionPixelSize(i5, 0);
                    break;
                case 2:
                    i3 = obtainStyledAttributes.getDimensionPixelSize(i5, 0);
                    break;
                case 3:
                    i2 = obtainStyledAttributes.getDimensionPixelSize(i5, 0);
                    break;
            }
        }
        obtainStyledAttributes.recycle();
        this.mStrokeWidth = i;
        this.mSpacing = i2;
        this.mSingleLineButtonPaddingHorizontal = i3;
        this.mDoubleLineButtonPaddingHorizontal = i4;
        this.mSingleToDoubleLineButtonWidthIncrease = 2 * (i4 - i3);
        this.mBreakIterator = BreakIterator.getLineInstance();
        reallocateCandidateButtonQueueForSqueezing();
    }

    public int getHeightUpperLimit() {
        return this.mHeightUpperLimit;
    }

    private void reallocateCandidateButtonQueueForSqueezing() {
        this.mCandidateButtonQueueForSqueezing = new PriorityQueue<>(Math.max(getChildCount(), 1), DECREASING_MEASURED_WIDTH_WITHOUT_PADDING_COMPARATOR);
    }

    public void setRepliesFromRemoteInput(RemoteInput remoteInput, PendingIntent pendingIntent, SmartReplyController smartReplyController, NotificationData.Entry entry, View view) {
        CharSequence[] choices;
        this.mSmartReplyContainer = view;
        removeAllViews();
        this.mCurrentBackgroundColor = this.mDefaultBackgroundColor;
        if (remoteInput != null && pendingIntent != null && (choices = remoteInput.getChoices()) != null) {
            for (int i = 0; i < choices.length; i++) {
                addView(inflateReplyButton(getContext(), this, i, choices[i], remoteInput, pendingIntent, smartReplyController, entry));
            }
        }
        reallocateCandidateButtonQueueForSqueezing();
    }

    public static SmartReplyView inflate(Context context, ViewGroup viewGroup) {
        return (SmartReplyView) LayoutInflater.from(context).inflate(R.layout.smart_reply_view, viewGroup, false);
    }

    @VisibleForTesting
    Button inflateReplyButton(final Context context, ViewGroup viewGroup, final int i, final CharSequence charSequence, final RemoteInput remoteInput, final PendingIntent pendingIntent, final SmartReplyController smartReplyController, final NotificationData.Entry entry) {
        final Button button = (Button) LayoutInflater.from(context).inflate(R.layout.smart_reply_button, viewGroup, false);
        button.setText(charSequence);
        final KeyguardHostView.OnDismissAction onDismissAction = new KeyguardHostView.OnDismissAction() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$SmartReplyView$UwLdHmeZSqfnZQeDlBO5lJ6m-GM
            @Override // com.android.keyguard.KeyguardHostView.OnDismissAction
            public final boolean onDismiss() {
                return SmartReplyView.lambda$inflateReplyButton$1(SmartReplyView.this, smartReplyController, entry, i, button, remoteInput, charSequence, pendingIntent, context);
            }
        };
        button.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$SmartReplyView$oc5xsdTBKPaigeImxuPKm7py0NE
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SmartReplyView.this.mKeyguardDismissUtil.executeWhenUnlocked(onDismissAction);
            }
        });
        button.setAccessibilityDelegate(new View.AccessibilityDelegate() { // from class: com.android.systemui.statusbar.policy.SmartReplyView.1
            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, SmartReplyView.this.getResources().getString(R.string.accessibility_send_smart_reply)));
            }
        });
        setColors(button, this.mCurrentBackgroundColor, this.mDefaultStrokeColor, this.mDefaultTextColor, this.mRippleColor);
        return button;
    }

    public static /* synthetic */ boolean lambda$inflateReplyButton$1(SmartReplyView smartReplyView, SmartReplyController smartReplyController, NotificationData.Entry entry, int i, Button button, RemoteInput remoteInput, CharSequence charSequence, PendingIntent pendingIntent, Context context) {
        smartReplyController.smartReplySent(entry, i, button.getText());
        Bundle bundle = new Bundle();
        bundle.putString(remoteInput.getResultKey(), charSequence.toString());
        Intent addFlags = new Intent().addFlags(268435456);
        RemoteInput.addResultsToIntent(new RemoteInput[]{remoteInput}, addFlags, bundle);
        RemoteInput.setResultsSource(addFlags, 1);
        entry.setHasSentReply();
        try {
            pendingIntent.send(context, 0, addFlags);
        } catch (PendingIntent.CanceledException e) {
            Log.w("SmartReplyView", "Unable to send smart reply", e);
        }
        smartReplyView.mSmartReplyContainer.setVisibility(8);
        return false;
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(this.mContext, attributeSet);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    @Override // android.view.ViewGroup
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams.width, layoutParams.height);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int i3;
        int i4;
        int size = View.MeasureSpec.getMode(i) == 0 ? Integer.MAX_VALUE : View.MeasureSpec.getSize(i);
        resetButtonsLayoutParams();
        if (!this.mCandidateButtonQueueForSqueezing.isEmpty()) {
            Log.wtf("SmartReplyView", "Single line button queue leaked between onMeasure calls");
            this.mCandidateButtonQueueForSqueezing.clear();
        }
        int i5 = this.mPaddingLeft + this.mPaddingRight;
        int i6 = this.mSingleLineButtonPaddingHorizontal;
        int childCount = getChildCount();
        int i7 = i5;
        int i8 = 0;
        int i9 = 0;
        int i10 = 0;
        while (true) {
            if (i8 >= childCount) {
                break;
            }
            View childAt = getChildAt(i8);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            if (childAt.getVisibility() == 0 && (childAt instanceof Button)) {
                childAt.setPadding(i6, childAt.getPaddingTop(), i6, childAt.getPaddingBottom());
                childAt.measure(MEASURE_SPEC_ANY_WIDTH, i2);
                Button button = (Button) childAt;
                int lineCount = button.getLineCount();
                if (lineCount >= 1 && lineCount <= 2) {
                    if (lineCount == 1) {
                        this.mCandidateButtonQueueForSqueezing.add(button);
                    }
                    if (i10 != 0) {
                        i3 = this.mSpacing;
                    } else {
                        i3 = 0;
                    }
                    int measuredWidth = i3 + childAt.getMeasuredWidth() + i7;
                    int max = Math.max(i9, childAt.getMeasuredHeight());
                    if (i6 == this.mSingleLineButtonPaddingHorizontal && (lineCount == 2 || measuredWidth > size)) {
                        measuredWidth += (i10 + 1) * this.mSingleToDoubleLineButtonWidthIncrease;
                        i4 = this.mDoubleLineButtonPaddingHorizontal;
                    } else {
                        i4 = i6;
                    }
                    if (measuredWidth > size) {
                        while (measuredWidth > size && !this.mCandidateButtonQueueForSqueezing.isEmpty()) {
                            Button poll = this.mCandidateButtonQueueForSqueezing.poll();
                            int squeezeButton = squeezeButton(poll, i2);
                            if (squeezeButton != -1) {
                                max = Math.max(max, poll.getMeasuredHeight());
                                measuredWidth -= squeezeButton;
                            }
                        }
                        if (measuredWidth > size) {
                            markButtonsWithPendingSqueezeStatusAs(3, i8);
                            break;
                        }
                        markButtonsWithPendingSqueezeStatusAs(2, i8);
                    }
                    layoutParams.show = true;
                    i10++;
                    i7 = measuredWidth;
                    i9 = max;
                    i6 = i4;
                }
            }
            i8++;
        }
        this.mCandidateButtonQueueForSqueezing.clear();
        remeasureButtonsIfNecessary(i6, i9);
        setMeasuredDimension(resolveSize(Math.max(getSuggestedMinimumWidth(), i7), i), resolveSize(Math.max(getSuggestedMinimumHeight(), this.mPaddingTop + i9 + this.mPaddingBottom), i2));
    }

    private void resetButtonsLayoutParams() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            LayoutParams layoutParams = (LayoutParams) getChildAt(i).getLayoutParams();
            layoutParams.show = false;
            layoutParams.squeezeStatus = 0;
        }
    }

    private int squeezeButton(Button button, int i) {
        int estimateOptimalSqueezedButtonTextWidth = estimateOptimalSqueezedButtonTextWidth(button);
        if (estimateOptimalSqueezedButtonTextWidth == -1) {
            return -1;
        }
        return squeezeButtonToTextWidth(button, i, estimateOptimalSqueezedButtonTextWidth);
    }

    private int estimateOptimalSqueezedButtonTextWidth(Button button) {
        String charSequence = button.getText().toString();
        TransformationMethod transformationMethod = button.getTransformationMethod();
        if (transformationMethod != null) {
            charSequence = transformationMethod.getTransformation(charSequence, button).toString();
        }
        int length = charSequence.length();
        this.mBreakIterator.setText(charSequence);
        if (this.mBreakIterator.preceding(length / 2) == -1 && this.mBreakIterator.next() == -1) {
            return -1;
        }
        TextPaint paint = button.getPaint();
        int current = this.mBreakIterator.current();
        float desiredWidth = Layout.getDesiredWidth(charSequence, 0, current, paint);
        float desiredWidth2 = Layout.getDesiredWidth(charSequence, current, length, paint);
        float max = Math.max(desiredWidth, desiredWidth2);
        int i = (desiredWidth > desiredWidth2 ? 1 : (desiredWidth == desiredWidth2 ? 0 : -1));
        if (i != 0) {
            boolean z = i > 0;
            int maxSqueezeRemeasureAttempts = this.mConstants.getMaxSqueezeRemeasureAttempts();
            float f = max;
            int i2 = 0;
            while (i2 < maxSqueezeRemeasureAttempts) {
                int previous = z ? this.mBreakIterator.previous() : this.mBreakIterator.next();
                if (previous != -1) {
                    float desiredWidth3 = Layout.getDesiredWidth(charSequence, 0, previous, paint);
                    float desiredWidth4 = Layout.getDesiredWidth(charSequence, previous, length, paint);
                    float max2 = Math.max(desiredWidth3, desiredWidth4);
                    if (max2 >= f) {
                        break;
                    }
                    if (!(!z ? desiredWidth3 < desiredWidth4 : desiredWidth3 > desiredWidth4)) {
                        i2++;
                        f = max2;
                    } else {
                        max = max2;
                        break;
                    }
                } else {
                    break;
                }
            }
            max = f;
        }
        return (int) Math.ceil(max);
    }

    private int squeezeButtonToTextWidth(Button button, int i, int i2) {
        int measuredWidth = button.getMeasuredWidth();
        if (button.getPaddingLeft() != this.mDoubleLineButtonPaddingHorizontal) {
            measuredWidth += this.mSingleToDoubleLineButtonWidthIncrease;
        }
        button.setPadding(this.mDoubleLineButtonPaddingHorizontal, button.getPaddingTop(), this.mDoubleLineButtonPaddingHorizontal, button.getPaddingBottom());
        button.measure(View.MeasureSpec.makeMeasureSpec((this.mDoubleLineButtonPaddingHorizontal * 2) + i2, Integer.MIN_VALUE), i);
        int measuredWidth2 = button.getMeasuredWidth();
        LayoutParams layoutParams = (LayoutParams) button.getLayoutParams();
        if (button.getLineCount() > 2 || measuredWidth2 >= measuredWidth) {
            layoutParams.squeezeStatus = 3;
            return -1;
        }
        layoutParams.squeezeStatus = 1;
        return measuredWidth - measuredWidth2;
    }

    private void remeasureButtonsIfNecessary(int i, int i2) {
        boolean z;
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(i2, 1073741824);
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            if (layoutParams.show) {
                int measuredWidth = childAt.getMeasuredWidth();
                if (layoutParams.squeezeStatus != 3) {
                    z = false;
                } else {
                    measuredWidth = Integer.MAX_VALUE;
                    z = true;
                }
                if (childAt.getPaddingLeft() != i) {
                    if (measuredWidth != Integer.MAX_VALUE) {
                        if (i == this.mSingleLineButtonPaddingHorizontal) {
                            measuredWidth -= this.mSingleToDoubleLineButtonWidthIncrease;
                        } else {
                            measuredWidth += this.mSingleToDoubleLineButtonWidthIncrease;
                        }
                    }
                    childAt.setPadding(i, childAt.getPaddingTop(), i, childAt.getPaddingBottom());
                    z = true;
                }
                if (childAt.getMeasuredHeight() != i2) {
                    z = true;
                }
                if (z) {
                    childAt.measure(View.MeasureSpec.makeMeasureSpec(measuredWidth, Integer.MIN_VALUE), makeMeasureSpec);
                }
            }
        }
    }

    private void markButtonsWithPendingSqueezeStatusAs(int i, int i2) {
        for (int i3 = 0; i3 <= i2; i3++) {
            LayoutParams layoutParams = (LayoutParams) getChildAt(i3).getLayoutParams();
            if (layoutParams.squeezeStatus == 1) {
                layoutParams.squeezeStatus = i;
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        boolean z2 = getLayoutDirection() == 1;
        int i5 = z2 ? (i3 - i) - this.mPaddingRight : this.mPaddingLeft;
        int childCount = getChildCount();
        for (int i6 = 0; i6 < childCount; i6++) {
            View childAt = getChildAt(i6);
            if (((LayoutParams) childAt.getLayoutParams()).show) {
                int measuredWidth = childAt.getMeasuredWidth();
                int measuredHeight = childAt.getMeasuredHeight();
                int i7 = z2 ? i5 - measuredWidth : i5;
                childAt.layout(i7, 0, i7 + measuredWidth, measuredHeight);
                int i8 = measuredWidth + this.mSpacing;
                if (z2) {
                    i5 -= i8;
                } else {
                    i5 += i8;
                }
            }
        }
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View view, long j) {
        return ((LayoutParams) view.getLayoutParams()).show && super.drawChild(canvas, view, j);
    }

    public void setBackgroundTintColor(int i) {
        if (i == this.mCurrentBackgroundColor) {
            return;
        }
        this.mCurrentBackgroundColor = i;
        boolean z = !NotificationColorUtil.isColorLight(i);
        int i2 = (-16777216) | i;
        int ensureTextContrast = NotificationColorUtil.ensureTextContrast(z ? this.mDefaultTextColorDarkBg : this.mDefaultTextColor, i2, z);
        int ensureContrast = NotificationColorUtil.ensureContrast(this.mDefaultStrokeColor, i2, z, this.mMinStrokeContrast);
        int i3 = z ? this.mRippleColorDarkBg : this.mRippleColor;
        int childCount = getChildCount();
        for (int i4 = 0; i4 < childCount; i4++) {
            setColors((Button) getChildAt(i4), i, ensureContrast, ensureTextContrast, i3);
        }
    }

    private void setColors(Button button, int i, int i2, int i3, int i4) {
        Drawable background = button.getBackground();
        if (background instanceof RippleDrawable) {
            Drawable mutate = background.mutate();
            RippleDrawable rippleDrawable = (RippleDrawable) mutate;
            rippleDrawable.setColor(ColorStateList.valueOf(i4));
            Drawable drawable = rippleDrawable.getDrawable(0);
            if (drawable instanceof InsetDrawable) {
                Drawable drawable2 = ((InsetDrawable) drawable).getDrawable();
                if (drawable2 instanceof GradientDrawable) {
                    GradientDrawable gradientDrawable = (GradientDrawable) drawable2;
                    gradientDrawable.setColor(i);
                    gradientDrawable.setStroke(this.mStrokeWidth, i2);
                }
            }
            button.setBackground(mutate);
        }
        button.setTextColor(i3);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @VisibleForTesting
    /* loaded from: classes.dex */
    public static class LayoutParams extends ViewGroup.LayoutParams {
        private boolean show;
        private int squeezeStatus;

        private LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.show = false;
            this.squeezeStatus = 0;
        }

        private LayoutParams(int i, int i2) {
            super(i, i2);
            this.show = false;
            this.squeezeStatus = 0;
        }

        @VisibleForTesting
        boolean isShown() {
            return this.show;
        }
    }
}
