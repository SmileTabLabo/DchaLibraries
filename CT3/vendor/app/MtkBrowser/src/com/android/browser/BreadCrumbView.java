package com.android.browser;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
/* loaded from: b.zip:com/android/browser/BreadCrumbView.class */
public class BreadCrumbView extends LinearLayout implements View.OnClickListener {
    private ImageButton mBackButton;
    private Context mContext;
    private Controller mController;
    private int mCrumbPadding;
    private List<Crumb> mCrumbs;
    private float mDividerPadding;
    private int mMaxVisible;
    private Drawable mSeparatorDrawable;
    private boolean mUseBackButton;

    /* loaded from: b.zip:com/android/browser/BreadCrumbView$Controller.class */
    public interface Controller {
        void onTop(BreadCrumbView breadCrumbView, int i, Object obj);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/BreadCrumbView$Crumb.class */
    public class Crumb {
        public boolean canGoBack;
        public View crumbView;
        public Object data;
        final BreadCrumbView this$0;

        public Crumb(BreadCrumbView breadCrumbView, String str, boolean z, Object obj) {
            this.this$0 = breadCrumbView;
            init(makeCrumbView(str), z, obj);
        }

        private void init(View view, boolean z, Object obj) {
            this.canGoBack = z;
            this.crumbView = view;
            this.data = obj;
        }

        private TextView makeCrumbView(String str) {
            TextView textView = new TextView(this.this$0.mContext);
            textView.setTextAppearance(this.this$0.mContext, 16973892);
            textView.setPadding(this.this$0.mCrumbPadding, 0, this.this$0.mCrumbPadding, 0);
            textView.setGravity(16);
            textView.setText(str);
            textView.setLayoutParams(new LinearLayout.LayoutParams(-2, -1));
            textView.setSingleLine();
            textView.setEllipsize(TextUtils.TruncateAt.END);
            return textView;
        }
    }

    public BreadCrumbView(Context context) {
        super(context);
        this.mMaxVisible = -1;
        init(context);
    }

    public BreadCrumbView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mMaxVisible = -1;
        init(context);
    }

    public BreadCrumbView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mMaxVisible = -1;
        init(context);
    }

    private void addBackButton() {
        this.mBackButton = new ImageButton(this.mContext);
        this.mBackButton.setImageResource(2130837532);
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(16843534, typedValue, true);
        this.mBackButton.setBackgroundResource(typedValue.resourceId);
        this.mBackButton.setLayoutParams(new LinearLayout.LayoutParams(-2, -1));
        this.mBackButton.setOnClickListener(this);
        this.mBackButton.setVisibility(8);
        this.mBackButton.setContentDescription(this.mContext.getText(2131493309));
        addView(this.mBackButton, 0);
    }

    private void addSeparator() {
        ImageView makeDividerView = makeDividerView();
        makeDividerView.setLayoutParams(makeDividerLayoutParams());
        addView(makeDividerView);
    }

    private void init(Context context) {
        this.mContext = context;
        setFocusable(true);
        this.mUseBackButton = false;
        this.mCrumbs = new ArrayList();
        TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(com.android.internal.R.styleable.Theme);
        this.mSeparatorDrawable = obtainStyledAttributes.getDrawable(155);
        obtainStyledAttributes.recycle();
        float f = this.mContext.getResources().getDisplayMetrics().density;
        this.mDividerPadding = 12.0f * f;
        this.mCrumbPadding = (int) (8.0f * f);
        addBackButton();
    }

    private LinearLayout.LayoutParams makeDividerLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -1);
        layoutParams.topMargin = (int) this.mDividerPadding;
        layoutParams.bottomMargin = (int) this.mDividerPadding;
        return layoutParams;
    }

    private ImageView makeDividerView() {
        ImageView imageView = new ImageView(this.mContext);
        imageView.setImageDrawable(this.mSeparatorDrawable);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imageView;
    }

    private void pop(boolean z) {
        int size = this.mCrumbs.size();
        if (size > 0) {
            removeLastView();
            if (!this.mUseBackButton || size > 1) {
                removeLastView();
            }
            this.mCrumbs.remove(size - 1);
            if (this.mUseBackButton) {
                Crumb topCrumb = getTopCrumb();
                if (topCrumb == null || !topCrumb.canGoBack) {
                    this.mBackButton.setVisibility(8);
                } else {
                    this.mBackButton.setVisibility(0);
                }
            }
            updateVisible();
            if (z) {
                notifyController();
            }
        }
    }

    private void pushCrumb(Crumb crumb) {
        if (this.mCrumbs.size() > 0) {
            addSeparator();
        }
        this.mCrumbs.add(crumb);
        addView(crumb.crumbView);
        updateVisible();
        crumb.crumbView.setOnClickListener(this);
    }

    private void removeLastView() {
        int childCount = getChildCount();
        if (childCount > 0) {
            removeViewAt(childCount - 1);
        }
    }

    private void updateVisible() {
        int i = 1;
        int i2 = 1;
        if (this.mMaxVisible >= 0) {
            int size = size() - this.mMaxVisible;
            if (size > 0) {
                int i3 = 0;
                while (true) {
                    i = i2;
                    if (i3 >= size) {
                        break;
                    }
                    getChildAt(i2).setVisibility(8);
                    int i4 = i2 + 1;
                    if (getChildAt(i4) != null) {
                        getChildAt(i4).setVisibility(8);
                    }
                    i2 = i4 + 1;
                    i3++;
                }
            }
            int childCount = getChildCount();
            while (i < childCount) {
                getChildAt(i).setVisibility(0);
                i++;
            }
        } else {
            int childCount2 = getChildCount();
            for (int i5 = 1; i5 < childCount2; i5++) {
                getChildAt(i5).setVisibility(0);
            }
        }
        if (!this.mUseBackButton) {
            this.mBackButton.setVisibility(8);
            return;
        }
        this.mBackButton.setVisibility(getTopCrumb() != null ? getTopCrumb().canGoBack : false ? 0 : 8);
    }

    public void clear() {
        while (this.mCrumbs.size() > 1) {
            pop(false);
        }
        pop(true);
    }

    @Override // android.widget.LinearLayout, android.view.View
    public int getBaseline() {
        int childCount = getChildCount();
        return childCount > 0 ? getChildAt(childCount - 1).getBaseline() : super.getBaseline();
    }

    Crumb getTopCrumb() {
        Crumb crumb = null;
        if (this.mCrumbs.size() > 0) {
            crumb = this.mCrumbs.get(this.mCrumbs.size() - 1);
        }
        return crumb;
    }

    public Object getTopData() {
        Crumb topCrumb = getTopCrumb();
        if (topCrumb != null) {
            return topCrumb.data;
        }
        return null;
    }

    public void notifyController() {
        if (this.mController != null) {
            if (this.mCrumbs.size() > 0) {
                this.mController.onTop(this, this.mCrumbs.size(), getTopCrumb().data);
            } else {
                this.mController.onTop(this, 0, null);
            }
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.mBackButton == view) {
            popView();
            notifyController();
            return;
        }
        while (view != getTopCrumb().crumbView) {
            pop(false);
        }
        notifyController();
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int intrinsicHeight = this.mSeparatorDrawable.getIntrinsicHeight();
        if (getMeasuredHeight() < intrinsicHeight) {
            switch (View.MeasureSpec.getMode(i2)) {
                case 1073741824:
                    return;
                case Integer.MIN_VALUE:
                    if (View.MeasureSpec.getSize(i2) < intrinsicHeight) {
                        return;
                    }
                    break;
            }
            setMeasuredDimension(getMeasuredWidth(), intrinsicHeight);
        }
    }

    public void popView() {
        pop(true);
    }

    public View pushView(String str, Object obj) {
        return pushView(str, true, obj);
    }

    public View pushView(String str, boolean z, Object obj) {
        Crumb crumb = new Crumb(this, str, z, obj);
        pushCrumb(crumb);
        return crumb.crumbView;
    }

    public void setController(Controller controller) {
        this.mController = controller;
    }

    public void setMaxVisible(int i) {
        this.mMaxVisible = i;
        updateVisible();
    }

    public void setUseBackButton(boolean z) {
        this.mUseBackButton = z;
        updateVisible();
    }

    public int size() {
        return this.mCrumbs.size();
    }
}
