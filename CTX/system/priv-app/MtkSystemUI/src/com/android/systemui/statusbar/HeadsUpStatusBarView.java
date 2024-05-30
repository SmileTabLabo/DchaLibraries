package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.DisplayCutout;
import android.view.View;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.AlphaOptimizedLinearLayout;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import java.util.Iterator;
import java.util.List;
/* loaded from: classes.dex */
public class HeadsUpStatusBarView extends AlphaOptimizedLinearLayout {
    private int mAbsoluteStartPadding;
    private List<Rect> mCutOutBounds;
    private int mCutOutInset;
    private Point mDisplaySize;
    private int mEndMargin;
    private boolean mFirstLayout;
    private Rect mIconDrawingRect;
    private View mIconPlaceholder;
    private Rect mLayoutedIconRect;
    private int mMaxWidth;
    private Runnable mOnDrawingRectChangedListener;
    private boolean mPublicMode;
    private NotificationData.Entry mShowingEntry;
    private int mSysWinInset;
    private TextView mTextView;
    private int[] mTmpPosition;

    public HeadsUpStatusBarView(Context context) {
        this(context, null);
    }

    public HeadsUpStatusBarView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public HeadsUpStatusBarView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public HeadsUpStatusBarView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mLayoutedIconRect = new Rect();
        this.mTmpPosition = new int[2];
        this.mFirstLayout = true;
        this.mIconDrawingRect = new Rect();
        Resources resources = getResources();
        this.mAbsoluteStartPadding = resources.getDimensionPixelSize(R.dimen.notification_side_paddings) + resources.getDimensionPixelSize(17105200);
        this.mEndMargin = resources.getDimensionPixelSize(17105199);
        setPaddingRelative(this.mAbsoluteStartPadding, 0, this.mEndMargin, 0);
        updateMaxWidth();
    }

    private void updateMaxWidth() {
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.qs_panel_width);
        if (dimensionPixelSize != this.mMaxWidth) {
            this.mMaxWidth = dimensionPixelSize;
            requestLayout();
        }
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        if (this.mMaxWidth > 0) {
            i = View.MeasureSpec.makeMeasureSpec(Math.min(View.MeasureSpec.getSize(i), this.mMaxWidth), View.MeasureSpec.getMode(i));
        }
        super.onMeasure(i, i2);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateMaxWidth();
    }

    @VisibleForTesting
    public HeadsUpStatusBarView(Context context, View view, TextView textView) {
        this(context);
        this.mIconPlaceholder = view;
        this.mTextView = textView;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mIconPlaceholder = findViewById(R.id.icon_placeholder);
        this.mTextView = (TextView) findViewById(R.id.text);
    }

    public void setEntry(NotificationData.Entry entry) {
        if (entry != null) {
            this.mShowingEntry = entry;
            CharSequence charSequence = entry.headsUpStatusBarText;
            if (this.mPublicMode) {
                charSequence = entry.headsUpStatusBarTextPublic;
            }
            this.mTextView.setText(charSequence);
            return;
        }
        this.mShowingEntry = null;
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        super.onLayout(z, i, i2, i3, i4);
        this.mIconPlaceholder.getLocationOnScreen(this.mTmpPosition);
        int translationX = (int) (this.mTmpPosition[0] - getTranslationX());
        int i6 = this.mTmpPosition[1];
        int width = this.mIconPlaceholder.getWidth() + translationX;
        this.mLayoutedIconRect.set(translationX, i6, width, this.mIconPlaceholder.getHeight() + i6);
        updateDrawingRect();
        int i7 = this.mAbsoluteStartPadding + this.mSysWinInset + this.mCutOutInset;
        boolean isLayoutRtl = isLayoutRtl();
        if (isLayoutRtl) {
            translationX = this.mDisplaySize.x - width;
        }
        if (translationX != i7) {
            if (this.mCutOutBounds != null) {
                Iterator<Rect> it = this.mCutOutBounds.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Rect next = it.next();
                    if (isLayoutRtl) {
                        i5 = this.mDisplaySize.x - next.right;
                        continue;
                    } else {
                        i5 = next.left;
                        continue;
                    }
                    if (translationX > i5) {
                        translationX -= next.width();
                        break;
                    }
                }
            }
            setPaddingRelative((i7 - translationX) + getPaddingStart(), 0, this.mEndMargin, 0);
        }
        if (this.mFirstLayout) {
            setVisibility(8);
            this.mFirstLayout = false;
        }
    }

    public void setPanelTranslation(float f) {
        if (isLayoutRtl()) {
            setTranslationX(f + this.mCutOutInset);
        } else {
            setTranslationX(f - this.mCutOutInset);
        }
        updateDrawingRect();
    }

    private void updateDrawingRect() {
        this.mIconDrawingRect.set(this.mLayoutedIconRect);
        this.mIconDrawingRect.offset((int) getTranslationX(), 0);
        if (this.mIconDrawingRect.left != this.mIconDrawingRect.left && this.mOnDrawingRectChangedListener != null) {
            this.mOnDrawingRectChangedListener.run();
        }
    }

    @Override // android.view.View
    protected boolean fitSystemWindows(Rect rect) {
        int i;
        boolean isLayoutRtl = isLayoutRtl();
        this.mSysWinInset = isLayoutRtl ? rect.right : rect.left;
        DisplayCutout displayCutout = getRootWindowInsets().getDisplayCutout();
        if (displayCutout != null) {
            i = isLayoutRtl ? displayCutout.getSafeInsetRight() : displayCutout.getSafeInsetLeft();
        } else {
            i = 0;
        }
        this.mCutOutInset = i;
        getDisplaySize();
        this.mCutOutBounds = null;
        if (displayCutout != null && displayCutout.getSafeInsetRight() == 0 && displayCutout.getSafeInsetLeft() == 0) {
            this.mCutOutBounds = displayCutout.getBoundingRects();
        }
        if (this.mSysWinInset != 0) {
            this.mCutOutInset = 0;
        }
        return super.fitSystemWindows(rect);
    }

    public NotificationData.Entry getShowingEntry() {
        return this.mShowingEntry;
    }

    public Rect getIconDrawingRect() {
        return this.mIconDrawingRect;
    }

    public void onDarkChanged(Rect rect, float f, int i) {
        this.mTextView.setTextColor(DarkIconDispatcher.getTint(rect, this, i));
    }

    public void setPublicMode(boolean z) {
        this.mPublicMode = z;
    }

    public void setOnDrawingRectChangedListener(Runnable runnable) {
        this.mOnDrawingRectChangedListener = runnable;
    }

    private void getDisplaySize() {
        if (this.mDisplaySize == null) {
            this.mDisplaySize = new Point();
        }
        getDisplay().getRealSize(this.mDisplaySize);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getDisplaySize();
    }
}
