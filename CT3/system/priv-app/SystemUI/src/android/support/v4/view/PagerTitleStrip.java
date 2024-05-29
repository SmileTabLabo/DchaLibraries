package android.support.v4.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;
import java.lang.ref.WeakReference;
@ViewPager.DecorView
/* loaded from: a.zip:android/support/v4/view/PagerTitleStrip.class */
public class PagerTitleStrip extends ViewGroup {
    private static final PagerTitleStripImpl IMPL;
    TextView mCurrText;
    private int mGravity;
    private int mLastKnownCurrentPage;
    private float mLastKnownPositionOffset;
    TextView mNextText;
    private int mNonPrimaryAlpha;
    private final PageListener mPageListener;
    ViewPager mPager;
    TextView mPrevText;
    private int mScaledTextSpacing;
    int mTextColor;
    private boolean mUpdatingPositions;
    private boolean mUpdatingText;
    private WeakReference<PagerAdapter> mWatchingAdapter;
    private static final int[] ATTRS = {16842804, 16842901, 16842904, 16842927};
    private static final int[] TEXT_ATTRS = {16843660};

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v4/view/PagerTitleStrip$PageListener.class */
    public class PageListener extends DataSetObserver implements ViewPager.OnPageChangeListener, ViewPager.OnAdapterChangeListener {
        private int mScrollState;
        final PagerTitleStrip this$0;

        private PageListener(PagerTitleStrip pagerTitleStrip) {
            this.this$0 = pagerTitleStrip;
        }

        /* synthetic */ PageListener(PagerTitleStrip pagerTitleStrip, PageListener pageListener) {
            this(pagerTitleStrip);
        }

        @Override // android.support.v4.view.ViewPager.OnAdapterChangeListener
        public void onAdapterChanged(ViewPager viewPager, PagerAdapter pagerAdapter, PagerAdapter pagerAdapter2) {
            this.this$0.updateAdapter(pagerAdapter, pagerAdapter2);
        }

        @Override // android.database.DataSetObserver
        public void onChanged() {
            this.this$0.updateText(this.this$0.mPager.getCurrentItem(), this.this$0.mPager.getAdapter());
            this.this$0.updateTextPositions(this.this$0.mPager.getCurrentItem(), this.this$0.mLastKnownPositionOffset >= 0.0f ? this.this$0.mLastKnownPositionOffset : 0.0f, true);
        }

        @Override // android.support.v4.view.ViewPager.OnPageChangeListener
        public void onPageScrollStateChanged(int i) {
            this.mScrollState = i;
        }

        @Override // android.support.v4.view.ViewPager.OnPageChangeListener
        public void onPageScrolled(int i, float f, int i2) {
            int i3 = i;
            if (f > 0.5f) {
                i3 = i + 1;
            }
            this.this$0.updateTextPositions(i3, f, false);
        }

        @Override // android.support.v4.view.ViewPager.OnPageChangeListener
        public void onPageSelected(int i) {
            if (this.mScrollState == 0) {
                this.this$0.updateText(this.this$0.mPager.getCurrentItem(), this.this$0.mPager.getAdapter());
                this.this$0.updateTextPositions(this.this$0.mPager.getCurrentItem(), this.this$0.mLastKnownPositionOffset >= 0.0f ? this.this$0.mLastKnownPositionOffset : 0.0f, true);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:android/support/v4/view/PagerTitleStrip$PagerTitleStripImpl.class */
    public interface PagerTitleStripImpl {
        void setSingleLineAllCaps(TextView textView);
    }

    /* loaded from: a.zip:android/support/v4/view/PagerTitleStrip$PagerTitleStripImplBase.class */
    static class PagerTitleStripImplBase implements PagerTitleStripImpl {
        PagerTitleStripImplBase() {
        }

        @Override // android.support.v4.view.PagerTitleStrip.PagerTitleStripImpl
        public void setSingleLineAllCaps(TextView textView) {
            textView.setSingleLine();
        }
    }

    /* loaded from: a.zip:android/support/v4/view/PagerTitleStrip$PagerTitleStripImplIcs.class */
    static class PagerTitleStripImplIcs implements PagerTitleStripImpl {
        PagerTitleStripImplIcs() {
        }

        @Override // android.support.v4.view.PagerTitleStrip.PagerTitleStripImpl
        public void setSingleLineAllCaps(TextView textView) {
            PagerTitleStripIcs.setSingleLineAllCaps(textView);
        }
    }

    static {
        if (Build.VERSION.SDK_INT >= 14) {
            IMPL = new PagerTitleStripImplIcs();
        } else {
            IMPL = new PagerTitleStripImplBase();
        }
    }

    public PagerTitleStrip(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mLastKnownCurrentPage = -1;
        this.mLastKnownPositionOffset = -1.0f;
        this.mPageListener = new PageListener(this, null);
        TextView textView = new TextView(context);
        this.mPrevText = textView;
        addView(textView);
        TextView textView2 = new TextView(context);
        this.mCurrText = textView2;
        addView(textView2);
        TextView textView3 = new TextView(context);
        this.mNextText = textView3;
        addView(textView3);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, ATTRS);
        int resourceId = obtainStyledAttributes.getResourceId(0, 0);
        if (resourceId != 0) {
            this.mPrevText.setTextAppearance(context, resourceId);
            this.mCurrText.setTextAppearance(context, resourceId);
            this.mNextText.setTextAppearance(context, resourceId);
        }
        int dimensionPixelSize = obtainStyledAttributes.getDimensionPixelSize(1, 0);
        if (dimensionPixelSize != 0) {
            setTextSize(0, dimensionPixelSize);
        }
        if (obtainStyledAttributes.hasValue(2)) {
            int color = obtainStyledAttributes.getColor(2, 0);
            this.mPrevText.setTextColor(color);
            this.mCurrText.setTextColor(color);
            this.mNextText.setTextColor(color);
        }
        this.mGravity = obtainStyledAttributes.getInteger(3, 80);
        obtainStyledAttributes.recycle();
        this.mTextColor = this.mCurrText.getTextColors().getDefaultColor();
        setNonPrimaryAlpha(0.6f);
        boolean z = false;
        if (resourceId != 0) {
            TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(resourceId, TEXT_ATTRS);
            z = obtainStyledAttributes2.getBoolean(0, false);
            obtainStyledAttributes2.recycle();
        }
        if (z) {
            setSingleLineAllCaps(this.mPrevText);
            setSingleLineAllCaps(this.mCurrText);
            setSingleLineAllCaps(this.mNextText);
        } else {
            this.mPrevText.setSingleLine();
            this.mCurrText.setSingleLine();
            this.mNextText.setSingleLine();
        }
        this.mScaledTextSpacing = (int) (16.0f * context.getResources().getDisplayMetrics().density);
        initLongStringSetting();
    }

    private void initLongStringSetting() {
        if (this.mPrevText == null || this.mCurrText == null || this.mNextText == null) {
            return;
        }
        this.mPrevText.setSingleLine();
        this.mCurrText.setSingleLine();
        this.mNextText.setSingleLine();
        this.mPrevText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        this.mCurrText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        this.mNextText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        this.mPrevText.setMarqueeRepeatLimit(2);
        this.mCurrText.setMarqueeRepeatLimit(2);
        this.mNextText.setMarqueeRepeatLimit(2);
        this.mPrevText.setHorizontalFadingEdgeEnabled(true);
        this.mCurrText.setHorizontalFadingEdgeEnabled(true);
        this.mNextText.setHorizontalFadingEdgeEnabled(true);
    }

    private static void setSingleLineAllCaps(TextView textView) {
        IMPL.setSingleLineAllCaps(textView);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getMinHeight() {
        int i = 0;
        Drawable background = getBackground();
        if (background != null) {
            i = background.getIntrinsicHeight();
        }
        return i;
    }

    public int getTextSpacing() {
        return this.mScaledTextSpacing;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        PagerAdapter pagerAdapter = null;
        super.onAttachedToWindow();
        ViewParent parent = getParent();
        if (!(parent instanceof ViewPager)) {
            throw new IllegalStateException("PagerTitleStrip must be a direct child of a ViewPager.");
        }
        ViewPager viewPager = (ViewPager) parent;
        PagerAdapter adapter = viewPager.getAdapter();
        viewPager.setInternalPageChangeListener(this.mPageListener);
        viewPager.addOnAdapterChangeListener(this.mPageListener);
        this.mPager = viewPager;
        if (this.mWatchingAdapter != null) {
            pagerAdapter = this.mWatchingAdapter.get();
        }
        updateAdapter(pagerAdapter, adapter);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mPager != null) {
            updateAdapter(this.mPager.getAdapter(), null);
            this.mPager.setInternalPageChangeListener(null);
            this.mPager.removeOnAdapterChangeListener(this.mPageListener);
            this.mPager = null;
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.mPager != null) {
            updateTextPositions(this.mLastKnownCurrentPage, this.mLastKnownPositionOffset >= 0.0f ? this.mLastKnownPositionOffset : 0.0f, true);
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int max;
        if (View.MeasureSpec.getMode(i) != 1073741824) {
            throw new IllegalStateException("Must measure with an exact width");
        }
        int paddingTop = getPaddingTop() + getPaddingBottom();
        int childMeasureSpec = getChildMeasureSpec(i2, paddingTop, -2);
        int size = View.MeasureSpec.getSize(i);
        int childMeasureSpec2 = getChildMeasureSpec(i, (int) (size * 0.2f), -2);
        this.mPrevText.measure(childMeasureSpec2, childMeasureSpec);
        this.mCurrText.measure(childMeasureSpec2, childMeasureSpec);
        this.mNextText.measure(childMeasureSpec2, childMeasureSpec);
        if (View.MeasureSpec.getMode(i2) == 1073741824) {
            max = View.MeasureSpec.getSize(i2);
        } else {
            max = Math.max(getMinHeight(), this.mCurrText.getMeasuredHeight() + paddingTop);
        }
        setMeasuredDimension(size, ViewCompat.resolveSizeAndState(max, i2, ViewCompat.getMeasuredState(this.mCurrText) << 16));
    }

    @Override // android.view.View, android.view.ViewParent
    public void requestLayout() {
        if (this.mUpdatingText) {
            return;
        }
        super.requestLayout();
    }

    public void setNonPrimaryAlpha(@FloatRange(from = 0.0d, to = 1.0d) float f) {
        this.mNonPrimaryAlpha = ((int) (255.0f * f)) & 255;
        int i = (this.mNonPrimaryAlpha << 24) | (this.mTextColor & 16777215);
        this.mPrevText.setTextColor(i);
        this.mNextText.setTextColor(i);
    }

    public void setTextSize(int i, float f) {
        this.mPrevText.setTextSize(i, f);
        this.mCurrText.setTextSize(i, f);
        this.mNextText.setTextSize(i, f);
    }

    public void setTextSpacing(int i) {
        this.mScaledTextSpacing = i;
        requestLayout();
    }

    void updateAdapter(PagerAdapter pagerAdapter, PagerAdapter pagerAdapter2) {
        if (pagerAdapter != null) {
            pagerAdapter.unregisterDataSetObserver(this.mPageListener);
            this.mWatchingAdapter = null;
        }
        if (pagerAdapter2 != null) {
            pagerAdapter2.registerDataSetObserver(this.mPageListener);
            this.mWatchingAdapter = new WeakReference<>(pagerAdapter2);
        }
        if (this.mPager != null) {
            this.mLastKnownCurrentPage = -1;
            this.mLastKnownPositionOffset = -1.0f;
            updateText(this.mPager.getCurrentItem(), pagerAdapter2);
            requestLayout();
        }
    }

    void updateText(int i, PagerAdapter pagerAdapter) {
        int count = pagerAdapter != null ? pagerAdapter.getCount() : 0;
        this.mUpdatingText = true;
        CharSequence charSequence = null;
        if (i >= 1) {
            charSequence = null;
            if (pagerAdapter != null) {
                charSequence = pagerAdapter.getPageTitle(i - 1);
            }
        }
        this.mPrevText.setText(charSequence);
        TextView textView = this.mCurrText;
        CharSequence charSequence2 = null;
        if (pagerAdapter != null) {
            charSequence2 = null;
            if (i < count) {
                charSequence2 = pagerAdapter.getPageTitle(i);
            }
        }
        textView.setText(charSequence2);
        this.mCurrText.setSelected(false);
        this.mCurrText.setSelected(true);
        CharSequence charSequence3 = null;
        if (i + 1 < count) {
            charSequence3 = null;
            if (pagerAdapter != null) {
                charSequence3 = pagerAdapter.getPageTitle(i + 1);
            }
        }
        this.mNextText.setText(charSequence3);
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.max(0, (int) (((getWidth() - getPaddingLeft()) - getPaddingRight()) * 0.8f)), Integer.MIN_VALUE);
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(Math.max(0, (getHeight() - getPaddingTop()) - getPaddingBottom()), Integer.MIN_VALUE);
        this.mPrevText.measure(makeMeasureSpec, makeMeasureSpec2);
        this.mCurrText.measure(makeMeasureSpec, makeMeasureSpec2);
        this.mNextText.measure(makeMeasureSpec, makeMeasureSpec2);
        this.mLastKnownCurrentPage = i;
        if (!this.mUpdatingPositions) {
            updateTextPositions(i, this.mLastKnownPositionOffset, false);
        }
        this.mUpdatingText = false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateTextPositions(int i, float f, boolean z) {
        int i2;
        int i3;
        int i4;
        if (i != this.mLastKnownCurrentPage) {
            updateText(i, this.mPager.getAdapter());
        } else if (!z && f == this.mLastKnownPositionOffset) {
            return;
        }
        this.mUpdatingPositions = true;
        int measuredWidth = this.mPrevText.getMeasuredWidth();
        int measuredWidth2 = this.mCurrText.getMeasuredWidth();
        int measuredWidth3 = this.mNextText.getMeasuredWidth();
        int i5 = measuredWidth2 / 2;
        int width = getWidth();
        int height = getHeight();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int i6 = paddingRight + i5;
        float f2 = f + 0.5f;
        float f3 = f2;
        if (f2 > 1.0f) {
            f3 = f2 - 1.0f;
        }
        int i7 = ((width - i6) - ((int) (((width - (paddingLeft + i5)) - i6) * f3))) - (measuredWidth2 / 2);
        int i8 = i7 + measuredWidth2;
        int baseline = this.mPrevText.getBaseline();
        int baseline2 = this.mCurrText.getBaseline();
        int baseline3 = this.mNextText.getBaseline();
        int max = Math.max(Math.max(baseline, baseline2), baseline3);
        int i9 = max - baseline;
        int i10 = max - baseline2;
        int i11 = max - baseline3;
        int max2 = Math.max(Math.max(i9 + this.mPrevText.getMeasuredHeight(), i10 + this.mCurrText.getMeasuredHeight()), i11 + this.mNextText.getMeasuredHeight());
        switch (this.mGravity & 112) {
            case 16:
                int i12 = (((height - paddingTop) - paddingBottom) - max2) / 2;
                i2 = i12 + i9;
                i3 = i12 + i10;
                i4 = i12 + i11;
                break;
            case 48:
            default:
                i2 = paddingTop + i9;
                i3 = paddingTop + i10;
                i4 = paddingTop + i11;
                break;
            case 80:
                int i13 = (height - paddingBottom) - max2;
                i2 = i13 + i9;
                i3 = i13 + i10;
                i4 = i13 + i11;
                break;
        }
        this.mCurrText.layout(i7, i3, i8, this.mCurrText.getMeasuredHeight() + i3);
        int min = Math.min(paddingLeft, (i7 - this.mScaledTextSpacing) - measuredWidth);
        this.mPrevText.layout(min, i2, min + measuredWidth, this.mPrevText.getMeasuredHeight() + i2);
        int max3 = Math.max((width - paddingRight) - measuredWidth3, this.mScaledTextSpacing + i8);
        this.mNextText.layout(max3, i4, max3 + measuredWidth3, this.mNextText.getMeasuredHeight() + i4);
        this.mLastKnownPositionOffset = f;
        this.mUpdatingPositions = false;
    }
}
