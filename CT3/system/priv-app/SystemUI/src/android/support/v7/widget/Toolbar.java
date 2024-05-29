package android.support.v7.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar$LayoutParams;
import android.support.v7.appcompat.R$attr;
import android.support.v7.appcompat.R$styleable;
import android.support.v7.view.CollapsibleActionView;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.view.menu.MenuPresenter;
import android.support.v7.view.menu.SubMenuBuilder;
import android.support.v7.widget.ActionMenuView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:android/support/v7/widget/Toolbar.class */
public class Toolbar extends ViewGroup {
    private int mButtonGravity;
    private ImageButton mCollapseButtonView;
    private CharSequence mCollapseDescription;
    private Drawable mCollapseIcon;
    private boolean mCollapsible;
    private int mContentInsetEndWithActions;
    private int mContentInsetStartWithNavigation;
    private final RtlSpacingHelper mContentInsets;
    private final AppCompatDrawableManager mDrawableManager;
    private boolean mEatingHover;
    private boolean mEatingTouch;
    View mExpandedActionView;
    private ExpandedActionViewMenuPresenter mExpandedMenuPresenter;
    private int mGravity;
    private final ArrayList<View> mHiddenViews;
    private ImageView mLogoView;
    private int mMaxButtonHeight;
    private ActionMenuView mMenuView;
    private final ActionMenuView.OnMenuItemClickListener mMenuViewItemClickListener;
    private ImageButton mNavButtonView;
    private Context mPopupContext;
    private int mPopupTheme;
    private final Runnable mShowOverflowMenuRunnable;
    private CharSequence mSubtitleText;
    private int mSubtitleTextAppearance;
    private int mSubtitleTextColor;
    private TextView mSubtitleTextView;
    private final int[] mTempMargins;
    private final ArrayList<View> mTempViews;
    private int mTitleMarginBottom;
    private int mTitleMarginEnd;
    private int mTitleMarginStart;
    private int mTitleMarginTop;
    private CharSequence mTitleText;
    private int mTitleTextAppearance;
    private int mTitleTextColor;
    private TextView mTitleTextView;
    private ToolbarWidgetWrapper mWrapper;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/Toolbar$ExpandedActionViewMenuPresenter.class */
    public class ExpandedActionViewMenuPresenter implements MenuPresenter {
        MenuItemImpl mCurrentExpandedItem;
        MenuBuilder mMenu;
        final Toolbar this$0;

        @Override // android.support.v7.view.menu.MenuPresenter
        public boolean collapseItemActionView(MenuBuilder menuBuilder, MenuItemImpl menuItemImpl) {
            if (this.this$0.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) this.this$0.mExpandedActionView).onActionViewCollapsed();
            }
            this.this$0.removeView(this.this$0.mExpandedActionView);
            this.this$0.removeView(this.this$0.mCollapseButtonView);
            this.this$0.mExpandedActionView = null;
            this.this$0.addChildrenForExpandedActionView();
            this.mCurrentExpandedItem = null;
            this.this$0.requestLayout();
            menuItemImpl.setActionViewExpanded(false);
            return true;
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public boolean expandItemActionView(MenuBuilder menuBuilder, MenuItemImpl menuItemImpl) {
            this.this$0.ensureCollapseButtonView();
            if (this.this$0.mCollapseButtonView.getParent() != this.this$0) {
                this.this$0.addView(this.this$0.mCollapseButtonView);
            }
            this.this$0.mExpandedActionView = menuItemImpl.getActionView();
            this.mCurrentExpandedItem = menuItemImpl;
            if (this.this$0.mExpandedActionView.getParent() != this.this$0) {
                LayoutParams generateDefaultLayoutParams = this.this$0.generateDefaultLayoutParams();
                generateDefaultLayoutParams.gravity = (this.this$0.mButtonGravity & 112) | 8388611;
                generateDefaultLayoutParams.mViewType = 2;
                this.this$0.mExpandedActionView.setLayoutParams(generateDefaultLayoutParams);
                this.this$0.addView(this.this$0.mExpandedActionView);
            }
            this.this$0.removeChildrenForExpandedActionView();
            this.this$0.requestLayout();
            menuItemImpl.setActionViewExpanded(true);
            if (this.this$0.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) this.this$0.mExpandedActionView).onActionViewExpanded();
                return true;
            }
            return true;
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public boolean flagActionItems() {
            return false;
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public void initForMenu(Context context, MenuBuilder menuBuilder) {
            if (this.mMenu != null && this.mCurrentExpandedItem != null) {
                this.mMenu.collapseItemActionView(this.mCurrentExpandedItem);
            }
            this.mMenu = menuBuilder;
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public void onCloseMenu(MenuBuilder menuBuilder, boolean z) {
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public boolean onSubMenuSelected(SubMenuBuilder subMenuBuilder) {
            return false;
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public void setCallback(MenuPresenter.Callback callback) {
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public void updateMenuView(boolean z) {
            if (this.mCurrentExpandedItem != null) {
                boolean z2 = false;
                if (this.mMenu != null) {
                    int size = this.mMenu.size();
                    int i = 0;
                    while (true) {
                        z2 = false;
                        if (i >= size) {
                            break;
                        } else if (this.mMenu.getItem(i) == this.mCurrentExpandedItem) {
                            z2 = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                }
                if (z2) {
                    return;
                }
                collapseItemActionView(this.mMenu, this.mCurrentExpandedItem);
            }
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/Toolbar$LayoutParams.class */
    public static class LayoutParams extends ActionBar$LayoutParams {
        int mViewType;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.mViewType = 0;
            this.gravity = 8388627;
        }

        public LayoutParams(@NonNull Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.mViewType = 0;
        }

        public LayoutParams(ActionBar$LayoutParams actionBar$LayoutParams) {
            super(actionBar$LayoutParams);
            this.mViewType = 0;
        }

        public LayoutParams(LayoutParams layoutParams) {
            super((ActionBar$LayoutParams) layoutParams);
            this.mViewType = 0;
            this.mViewType = layoutParams.mViewType;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.mViewType = 0;
        }

        public LayoutParams(ViewGroup.MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
            this.mViewType = 0;
            copyMarginsFromCompat(marginLayoutParams);
        }

        void copyMarginsFromCompat(ViewGroup.MarginLayoutParams marginLayoutParams) {
            this.leftMargin = marginLayoutParams.leftMargin;
            this.topMargin = marginLayoutParams.topMargin;
            this.rightMargin = marginLayoutParams.rightMargin;
            this.bottomMargin = marginLayoutParams.bottomMargin;
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/Toolbar$SavedState.class */
    public static class SavedState extends AbsSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() { // from class: android.support.v7.widget.Toolbar.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
            public SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
                return new SavedState(parcel, classLoader);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        });
        int expandedMenuItemId;
        boolean isOverflowOpen;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public SavedState(Parcel parcel, ClassLoader classLoader) {
            super(parcel, classLoader);
            boolean z = false;
            this.expandedMenuItemId = parcel.readInt();
            this.isOverflowOpen = parcel.readInt() != 0 ? true : z;
        }

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        @Override // android.support.v4.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.expandedMenuItemId);
            parcel.writeInt(this.isOverflowOpen ? 1 : 0);
        }
    }

    public Toolbar(Context context) {
        this(context, null);
    }

    public Toolbar(Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.toolbarStyle);
    }

    public Toolbar(Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mContentInsets = new RtlSpacingHelper();
        this.mGravity = 8388627;
        this.mTempViews = new ArrayList<>();
        this.mHiddenViews = new ArrayList<>();
        this.mTempMargins = new int[2];
        this.mMenuViewItemClickListener = new ActionMenuView.OnMenuItemClickListener(this) { // from class: android.support.v7.widget.Toolbar.1
            final Toolbar this$0;

            {
                this.this$0 = this;
            }
        };
        this.mShowOverflowMenuRunnable = new Runnable(this) { // from class: android.support.v7.widget.Toolbar.2
            final Toolbar this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.showOverflowMenu();
            }
        };
        TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(getContext(), attributeSet, R$styleable.Toolbar, i, 0);
        this.mTitleTextAppearance = obtainStyledAttributes.getResourceId(R$styleable.Toolbar_titleTextAppearance, 0);
        this.mSubtitleTextAppearance = obtainStyledAttributes.getResourceId(R$styleable.Toolbar_subtitleTextAppearance, 0);
        this.mGravity = obtainStyledAttributes.getInteger(R$styleable.Toolbar_android_gravity, this.mGravity);
        this.mButtonGravity = obtainStyledAttributes.getInteger(R$styleable.Toolbar_buttonGravity, 48);
        int dimensionPixelOffset = obtainStyledAttributes.getDimensionPixelOffset(R$styleable.Toolbar_titleMargin, 0);
        int dimensionPixelOffset2 = obtainStyledAttributes.hasValue(R$styleable.Toolbar_titleMargins) ? obtainStyledAttributes.getDimensionPixelOffset(R$styleable.Toolbar_titleMargins, dimensionPixelOffset) : dimensionPixelOffset;
        this.mTitleMarginBottom = dimensionPixelOffset2;
        this.mTitleMarginTop = dimensionPixelOffset2;
        this.mTitleMarginEnd = dimensionPixelOffset2;
        this.mTitleMarginStart = dimensionPixelOffset2;
        int dimensionPixelOffset3 = obtainStyledAttributes.getDimensionPixelOffset(R$styleable.Toolbar_titleMarginStart, -1);
        if (dimensionPixelOffset3 >= 0) {
            this.mTitleMarginStart = dimensionPixelOffset3;
        }
        int dimensionPixelOffset4 = obtainStyledAttributes.getDimensionPixelOffset(R$styleable.Toolbar_titleMarginEnd, -1);
        if (dimensionPixelOffset4 >= 0) {
            this.mTitleMarginEnd = dimensionPixelOffset4;
        }
        int dimensionPixelOffset5 = obtainStyledAttributes.getDimensionPixelOffset(R$styleable.Toolbar_titleMarginTop, -1);
        if (dimensionPixelOffset5 >= 0) {
            this.mTitleMarginTop = dimensionPixelOffset5;
        }
        int dimensionPixelOffset6 = obtainStyledAttributes.getDimensionPixelOffset(R$styleable.Toolbar_titleMarginBottom, -1);
        if (dimensionPixelOffset6 >= 0) {
            this.mTitleMarginBottom = dimensionPixelOffset6;
        }
        this.mMaxButtonHeight = obtainStyledAttributes.getDimensionPixelSize(R$styleable.Toolbar_maxButtonHeight, -1);
        int dimensionPixelOffset7 = obtainStyledAttributes.getDimensionPixelOffset(R$styleable.Toolbar_contentInsetStart, Integer.MIN_VALUE);
        int dimensionPixelOffset8 = obtainStyledAttributes.getDimensionPixelOffset(R$styleable.Toolbar_contentInsetEnd, Integer.MIN_VALUE);
        this.mContentInsets.setAbsolute(obtainStyledAttributes.getDimensionPixelSize(R$styleable.Toolbar_contentInsetLeft, 0), obtainStyledAttributes.getDimensionPixelSize(R$styleable.Toolbar_contentInsetRight, 0));
        if (dimensionPixelOffset7 != Integer.MIN_VALUE || dimensionPixelOffset8 != Integer.MIN_VALUE) {
            this.mContentInsets.setRelative(dimensionPixelOffset7, dimensionPixelOffset8);
        }
        this.mContentInsetStartWithNavigation = obtainStyledAttributes.getDimensionPixelOffset(R$styleable.Toolbar_contentInsetStartWithNavigation, Integer.MIN_VALUE);
        this.mContentInsetEndWithActions = obtainStyledAttributes.getDimensionPixelOffset(R$styleable.Toolbar_contentInsetEndWithActions, Integer.MIN_VALUE);
        this.mCollapseIcon = obtainStyledAttributes.getDrawable(R$styleable.Toolbar_collapseIcon);
        this.mCollapseDescription = obtainStyledAttributes.getText(R$styleable.Toolbar_collapseContentDescription);
        CharSequence text = obtainStyledAttributes.getText(R$styleable.Toolbar_title);
        if (!TextUtils.isEmpty(text)) {
            setTitle(text);
        }
        CharSequence text2 = obtainStyledAttributes.getText(R$styleable.Toolbar_subtitle);
        if (!TextUtils.isEmpty(text2)) {
            setSubtitle(text2);
        }
        this.mPopupContext = getContext();
        setPopupTheme(obtainStyledAttributes.getResourceId(R$styleable.Toolbar_popupTheme, 0));
        Drawable drawable = obtainStyledAttributes.getDrawable(R$styleable.Toolbar_navigationIcon);
        if (drawable != null) {
            setNavigationIcon(drawable);
        }
        CharSequence text3 = obtainStyledAttributes.getText(R$styleable.Toolbar_navigationContentDescription);
        if (!TextUtils.isEmpty(text3)) {
            setNavigationContentDescription(text3);
        }
        Drawable drawable2 = obtainStyledAttributes.getDrawable(R$styleable.Toolbar_logo);
        if (drawable2 != null) {
            setLogo(drawable2);
        }
        CharSequence text4 = obtainStyledAttributes.getText(R$styleable.Toolbar_logoDescription);
        if (!TextUtils.isEmpty(text4)) {
            setLogoDescription(text4);
        }
        if (obtainStyledAttributes.hasValue(R$styleable.Toolbar_titleTextColor)) {
            setTitleTextColor(obtainStyledAttributes.getColor(R$styleable.Toolbar_titleTextColor, -1));
        }
        if (obtainStyledAttributes.hasValue(R$styleable.Toolbar_subtitleTextColor)) {
            setSubtitleTextColor(obtainStyledAttributes.getColor(R$styleable.Toolbar_subtitleTextColor, -1));
        }
        obtainStyledAttributes.recycle();
        this.mDrawableManager = AppCompatDrawableManager.get();
    }

    private void addCustomViewsWithGravity(List<View> list, int i) {
        boolean z = ViewCompat.getLayoutDirection(this) == 1;
        int childCount = getChildCount();
        int absoluteGravity = GravityCompat.getAbsoluteGravity(i, ViewCompat.getLayoutDirection(this));
        list.clear();
        if (z) {
            for (int i2 = childCount - 1; i2 >= 0; i2--) {
                View childAt = getChildAt(i2);
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (layoutParams.mViewType == 0 && shouldLayout(childAt) && getChildHorizontalGravity(layoutParams.gravity) == absoluteGravity) {
                    list.add(childAt);
                }
            }
            return;
        }
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt2 = getChildAt(i3);
            LayoutParams layoutParams2 = (LayoutParams) childAt2.getLayoutParams();
            if (layoutParams2.mViewType == 0 && shouldLayout(childAt2) && getChildHorizontalGravity(layoutParams2.gravity) == absoluteGravity) {
                list.add(childAt2);
            }
        }
    }

    private void addSystemView(View view, boolean z) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        LayoutParams generateDefaultLayoutParams = layoutParams == null ? generateDefaultLayoutParams() : !checkLayoutParams(layoutParams) ? generateLayoutParams(layoutParams) : (LayoutParams) layoutParams;
        generateDefaultLayoutParams.mViewType = 1;
        if (!z || this.mExpandedActionView == null) {
            addView(view, generateDefaultLayoutParams);
            return;
        }
        view.setLayoutParams(generateDefaultLayoutParams);
        this.mHiddenViews.add(view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void ensureCollapseButtonView() {
        if (this.mCollapseButtonView == null) {
            this.mCollapseButtonView = new ImageButton(getContext(), null, R$attr.toolbarNavigationButtonStyle);
            this.mCollapseButtonView.setImageDrawable(this.mCollapseIcon);
            this.mCollapseButtonView.setContentDescription(this.mCollapseDescription);
            LayoutParams generateDefaultLayoutParams = generateDefaultLayoutParams();
            generateDefaultLayoutParams.gravity = (this.mButtonGravity & 112) | 8388611;
            generateDefaultLayoutParams.mViewType = 2;
            this.mCollapseButtonView.setLayoutParams(generateDefaultLayoutParams);
            this.mCollapseButtonView.setOnClickListener(new View.OnClickListener(this) { // from class: android.support.v7.widget.Toolbar.3
                final Toolbar this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    this.this$0.collapseActionView();
                }
            });
        }
    }

    private void ensureLogoView() {
        if (this.mLogoView == null) {
            this.mLogoView = new ImageView(getContext());
        }
    }

    private void ensureNavButtonView() {
        if (this.mNavButtonView == null) {
            this.mNavButtonView = new ImageButton(getContext(), null, R$attr.toolbarNavigationButtonStyle);
            LayoutParams generateDefaultLayoutParams = generateDefaultLayoutParams();
            generateDefaultLayoutParams.gravity = (this.mButtonGravity & 112) | 8388611;
            this.mNavButtonView.setLayoutParams(generateDefaultLayoutParams);
        }
    }

    private int getChildHorizontalGravity(int i) {
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        int absoluteGravity = GravityCompat.getAbsoluteGravity(i, layoutDirection) & 7;
        switch (absoluteGravity) {
            case 1:
            case 3:
            case 5:
                return absoluteGravity;
            case 2:
            case 4:
            default:
                return layoutDirection == 1 ? 5 : 3;
        }
    }

    private int getChildTop(View view, int i) {
        int i2;
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int measuredHeight = view.getMeasuredHeight();
        int i3 = i > 0 ? (measuredHeight - i) / 2 : 0;
        switch (getChildVerticalGravity(layoutParams.gravity)) {
            case 16:
            default:
                int paddingTop = getPaddingTop();
                int paddingBottom = getPaddingBottom();
                int height = getHeight();
                int i4 = (((height - paddingTop) - paddingBottom) - measuredHeight) / 2;
                if (i4 < layoutParams.topMargin) {
                    i2 = layoutParams.topMargin;
                } else {
                    int i5 = (((height - paddingBottom) - measuredHeight) - i4) - paddingTop;
                    i2 = i4;
                    if (i5 < layoutParams.bottomMargin) {
                        i2 = Math.max(0, i4 - (layoutParams.bottomMargin - i5));
                    }
                }
                return paddingTop + i2;
            case 48:
                return getPaddingTop() - i3;
            case 80:
                return (((getHeight() - getPaddingBottom()) - measuredHeight) - layoutParams.bottomMargin) - i3;
        }
    }

    private int getChildVerticalGravity(int i) {
        int i2 = i & 112;
        switch (i2) {
            case 16:
            case 48:
            case 80:
                return i2;
            default:
                return this.mGravity & 112;
        }
    }

    private int getHorizontalMargins(View view) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        return MarginLayoutParamsCompat.getMarginStart(marginLayoutParams) + MarginLayoutParamsCompat.getMarginEnd(marginLayoutParams);
    }

    private int getVerticalMargins(View view) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        return marginLayoutParams.topMargin + marginLayoutParams.bottomMargin;
    }

    private int getViewListMeasuredWidth(List<View> list, int[] iArr) {
        int i = iArr[0];
        int i2 = iArr[1];
        int i3 = 0;
        int size = list.size();
        for (int i4 = 0; i4 < size; i4++) {
            View view = list.get(i4);
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            int i5 = layoutParams.leftMargin - i;
            int i6 = layoutParams.rightMargin - i2;
            int max = Math.max(0, i5);
            int max2 = Math.max(0, i6);
            i = Math.max(0, -i5);
            i2 = Math.max(0, -i6);
            i3 += view.getMeasuredWidth() + max + max2;
        }
        return i3;
    }

    private boolean isChildOrHidden(View view) {
        return view.getParent() != this ? this.mHiddenViews.contains(view) : true;
    }

    private int layoutChildLeft(View view, int i, int[] iArr, int i2) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int i3 = layoutParams.leftMargin - iArr[0];
        int max = i + Math.max(0, i3);
        iArr[0] = Math.max(0, -i3);
        int childTop = getChildTop(view, i2);
        int measuredWidth = view.getMeasuredWidth();
        view.layout(max, childTop, max + measuredWidth, view.getMeasuredHeight() + childTop);
        return max + layoutParams.rightMargin + measuredWidth;
    }

    private int layoutChildRight(View view, int i, int[] iArr, int i2) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int i3 = layoutParams.rightMargin - iArr[1];
        int max = i - Math.max(0, i3);
        iArr[1] = Math.max(0, -i3);
        int childTop = getChildTop(view, i2);
        int measuredWidth = view.getMeasuredWidth();
        view.layout(max - measuredWidth, childTop, max, view.getMeasuredHeight() + childTop);
        return max - (layoutParams.leftMargin + measuredWidth);
    }

    private int measureChildCollapseMargins(View view, int i, int i2, int i3, int i4, int[] iArr) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int i5 = marginLayoutParams.leftMargin - iArr[0];
        int i6 = marginLayoutParams.rightMargin - iArr[1];
        int max = Math.max(0, i5) + Math.max(0, i6);
        iArr[0] = Math.max(0, -i5);
        iArr[1] = Math.max(0, -i6);
        view.measure(getChildMeasureSpec(i, getPaddingLeft() + getPaddingRight() + max + i2, marginLayoutParams.width), getChildMeasureSpec(i3, getPaddingTop() + getPaddingBottom() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin + i4, marginLayoutParams.height));
        return view.getMeasuredWidth() + max;
    }

    private void measureChildConstrained(View view, int i, int i2, int i3, int i4, int i5) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int childMeasureSpec = getChildMeasureSpec(i, getPaddingLeft() + getPaddingRight() + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin + i2, marginLayoutParams.width);
        int childMeasureSpec2 = getChildMeasureSpec(i3, getPaddingTop() + getPaddingBottom() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin + i4, marginLayoutParams.height);
        int mode = View.MeasureSpec.getMode(childMeasureSpec2);
        int i6 = childMeasureSpec2;
        if (mode != 1073741824) {
            i6 = childMeasureSpec2;
            if (i5 >= 0) {
                i6 = View.MeasureSpec.makeMeasureSpec(mode != 0 ? Math.min(View.MeasureSpec.getSize(childMeasureSpec2), i5) : i5, 1073741824);
            }
        }
        view.measure(childMeasureSpec, i6);
    }

    private void postShowOverflowMenu() {
        removeCallbacks(this.mShowOverflowMenuRunnable);
        post(this.mShowOverflowMenuRunnable);
    }

    private boolean shouldCollapse() {
        if (this.mCollapsible) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = getChildAt(i);
                if (shouldLayout(childAt) && childAt.getMeasuredWidth() > 0 && childAt.getMeasuredHeight() > 0) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean shouldLayout(View view) {
        boolean z = false;
        if (view != null) {
            z = false;
            if (view.getParent() == this) {
                z = false;
                if (view.getVisibility() != 8) {
                    z = true;
                }
            }
        }
        return z;
    }

    void addChildrenForExpandedActionView() {
        for (int size = this.mHiddenViews.size() - 1; size >= 0; size--) {
            addView(this.mHiddenViews.get(size));
        }
        this.mHiddenViews.clear();
    }

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return super.checkLayoutParams(layoutParams) ? layoutParams instanceof LayoutParams : false;
    }

    public void collapseActionView() {
        MenuItemImpl menuItemImpl = null;
        if (this.mExpandedMenuPresenter != null) {
            menuItemImpl = this.mExpandedMenuPresenter.mCurrentExpandedItem;
        }
        if (menuItemImpl != null) {
            menuItemImpl.collapseActionView();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams ? new LayoutParams((LayoutParams) layoutParams) : layoutParams instanceof ActionBar$LayoutParams ? new LayoutParams((ActionBar$LayoutParams) layoutParams) : layoutParams instanceof ViewGroup.MarginLayoutParams ? new LayoutParams((ViewGroup.MarginLayoutParams) layoutParams) : new LayoutParams(layoutParams);
    }

    public int getContentInsetEnd() {
        return this.mContentInsets.getEnd();
    }

    public int getContentInsetStart() {
        return this.mContentInsets.getStart();
    }

    public int getCurrentContentInsetEnd() {
        boolean z = false;
        if (this.mMenuView != null) {
            MenuBuilder peekMenu = this.mMenuView.peekMenu();
            z = peekMenu != null ? peekMenu.hasVisibleItems() : false;
        }
        return z ? Math.max(getContentInsetEnd(), Math.max(this.mContentInsetEndWithActions, 0)) : getContentInsetEnd();
    }

    public int getCurrentContentInsetLeft() {
        return ViewCompat.getLayoutDirection(this) == 1 ? getCurrentContentInsetEnd() : getCurrentContentInsetStart();
    }

    public int getCurrentContentInsetRight() {
        return ViewCompat.getLayoutDirection(this) == 1 ? getCurrentContentInsetStart() : getCurrentContentInsetEnd();
    }

    public int getCurrentContentInsetStart() {
        return getNavigationIcon() != null ? Math.max(getContentInsetStart(), Math.max(this.mContentInsetStartWithNavigation, 0)) : getContentInsetStart();
    }

    @Nullable
    public CharSequence getNavigationContentDescription() {
        CharSequence charSequence = null;
        if (this.mNavButtonView != null) {
            charSequence = this.mNavButtonView.getContentDescription();
        }
        return charSequence;
    }

    @Nullable
    public Drawable getNavigationIcon() {
        Drawable drawable = null;
        if (this.mNavButtonView != null) {
            drawable = this.mNavButtonView.getDrawable();
        }
        return drawable;
    }

    public CharSequence getSubtitle() {
        return this.mSubtitleText;
    }

    public CharSequence getTitle() {
        return this.mTitleText;
    }

    public DecorToolbar getWrapper() {
        if (this.mWrapper == null) {
            this.mWrapper = new ToolbarWidgetWrapper(this, true);
        }
        return this.mWrapper;
    }

    public boolean isOverflowMenuShowing() {
        return this.mMenuView != null ? this.mMenuView.isOverflowMenuShowing() : false;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mShowOverflowMenuRunnable);
    }

    @Override // android.view.View
    public boolean onHoverEvent(MotionEvent motionEvent) {
        int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
        if (actionMasked == 9) {
            this.mEatingHover = false;
        }
        if (!this.mEatingHover) {
            boolean onHoverEvent = super.onHoverEvent(motionEvent);
            if (actionMasked == 9 && !onHoverEvent) {
                this.mEatingHover = true;
            }
        }
        if (actionMasked == 10 || actionMasked == 3) {
            this.mEatingHover = false;
            return true;
        }
        return true;
    }

    /* JADX WARN: Code restructure failed: missing block: B:39:0x01c8, code lost:
        if (r0 != false) goto L68;
     */
    @Override // android.view.ViewGroup, android.view.View
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int max;
        int i6;
        int i7;
        int i8;
        boolean z2 = ViewCompat.getLayoutDirection(this) == 1;
        int width = getWidth();
        int height = getHeight();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int i9 = width - paddingRight;
        int[] iArr = this.mTempMargins;
        iArr[1] = 0;
        iArr[0] = 0;
        int minimumHeight = ViewCompat.getMinimumHeight(this);
        int i10 = paddingLeft;
        int i11 = i9;
        if (shouldLayout(this.mNavButtonView)) {
            if (z2) {
                i11 = layoutChildRight(this.mNavButtonView, i9, iArr, minimumHeight);
                i10 = paddingLeft;
            } else {
                i10 = layoutChildLeft(this.mNavButtonView, paddingLeft, iArr, minimumHeight);
                i11 = i9;
            }
        }
        int i12 = i10;
        int i13 = i11;
        if (shouldLayout(this.mCollapseButtonView)) {
            if (z2) {
                i13 = layoutChildRight(this.mCollapseButtonView, i11, iArr, minimumHeight);
                i12 = i10;
            } else {
                i12 = layoutChildLeft(this.mCollapseButtonView, i10, iArr, minimumHeight);
                i13 = i11;
            }
        }
        int i14 = i12;
        int i15 = i13;
        if (shouldLayout(this.mMenuView)) {
            if (z2) {
                i14 = layoutChildLeft(this.mMenuView, i12, iArr, minimumHeight);
                i15 = i13;
            } else {
                i15 = layoutChildRight(this.mMenuView, i13, iArr, minimumHeight);
                i14 = i12;
            }
        }
        int currentContentInsetLeft = getCurrentContentInsetLeft();
        int currentContentInsetRight = getCurrentContentInsetRight();
        iArr[0] = Math.max(0, currentContentInsetLeft - i14);
        iArr[1] = Math.max(0, currentContentInsetRight - ((width - paddingRight) - i15));
        int max2 = Math.max(i14, currentContentInsetLeft);
        int min = Math.min(i15, (width - paddingRight) - currentContentInsetRight);
        int i16 = max2;
        int i17 = min;
        if (shouldLayout(this.mExpandedActionView)) {
            if (z2) {
                i17 = layoutChildRight(this.mExpandedActionView, min, iArr, minimumHeight);
                i16 = max2;
            } else {
                i16 = layoutChildLeft(this.mExpandedActionView, max2, iArr, minimumHeight);
                i17 = min;
            }
        }
        int i18 = i16;
        int i19 = i17;
        if (shouldLayout(this.mLogoView)) {
            if (z2) {
                i19 = layoutChildRight(this.mLogoView, i17, iArr, minimumHeight);
                i18 = i16;
            } else {
                i18 = layoutChildLeft(this.mLogoView, i16, iArr, minimumHeight);
                i19 = i17;
            }
        }
        boolean shouldLayout = shouldLayout(this.mTitleTextView);
        boolean shouldLayout2 = shouldLayout(this.mSubtitleTextView);
        int i20 = 0;
        if (shouldLayout) {
            LayoutParams layoutParams = (LayoutParams) this.mTitleTextView.getLayoutParams();
            i20 = layoutParams.topMargin + this.mTitleTextView.getMeasuredHeight() + layoutParams.bottomMargin + 0;
        }
        int i21 = i20;
        if (shouldLayout2) {
            LayoutParams layoutParams2 = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
            i21 = i20 + layoutParams2.topMargin + this.mSubtitleTextView.getMeasuredHeight() + layoutParams2.bottomMargin;
        }
        if (!shouldLayout) {
            max = i18;
            i6 = i19;
        }
        TextView textView = shouldLayout ? this.mTitleTextView : this.mSubtitleTextView;
        TextView textView2 = shouldLayout2 ? this.mSubtitleTextView : this.mTitleTextView;
        LayoutParams layoutParams3 = (LayoutParams) textView.getLayoutParams();
        LayoutParams layoutParams4 = (LayoutParams) textView2.getLayoutParams();
        boolean z3 = (!shouldLayout || this.mTitleTextView.getMeasuredWidth() <= 0) ? shouldLayout2 && this.mSubtitleTextView.getMeasuredWidth() > 0 : true;
        switch (this.mGravity & 112) {
            case 16:
            default:
                int i22 = (((height - paddingTop) - paddingBottom) - i21) / 2;
                if (i22 < layoutParams3.topMargin + this.mTitleMarginTop) {
                    i7 = layoutParams3.topMargin + this.mTitleMarginTop;
                } else {
                    int i23 = (((height - paddingBottom) - i21) - i22) - paddingTop;
                    i7 = i22;
                    if (i23 < layoutParams3.bottomMargin + this.mTitleMarginBottom) {
                        i7 = Math.max(0, i22 - ((layoutParams4.bottomMargin + this.mTitleMarginBottom) - i23));
                    }
                }
                i5 = paddingTop + i7;
                break;
            case 48:
                i5 = getPaddingTop() + layoutParams3.topMargin + this.mTitleMarginTop;
                break;
            case 80:
                i5 = (((height - paddingBottom) - layoutParams4.bottomMargin) - this.mTitleMarginBottom) - i21;
                break;
        }
        if (z2) {
            int i24 = (z3 ? this.mTitleMarginStart : 0) - iArr[1];
            int max3 = i19 - Math.max(0, i24);
            iArr[1] = Math.max(0, -i24);
            int i25 = max3;
            int i26 = i5;
            if (shouldLayout) {
                LayoutParams layoutParams5 = (LayoutParams) this.mTitleTextView.getLayoutParams();
                int measuredWidth = max3 - this.mTitleTextView.getMeasuredWidth();
                int measuredHeight = i5 + this.mTitleTextView.getMeasuredHeight();
                this.mTitleTextView.layout(measuredWidth, i5, max3, measuredHeight);
                i25 = measuredWidth - this.mTitleMarginEnd;
                i26 = measuredHeight + layoutParams5.bottomMargin;
            }
            int i27 = max3;
            if (shouldLayout2) {
                LayoutParams layoutParams6 = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
                int i28 = i26 + layoutParams6.topMargin;
                this.mSubtitleTextView.layout(max3 - this.mSubtitleTextView.getMeasuredWidth(), i28, max3, i28 + this.mSubtitleTextView.getMeasuredHeight());
                i27 = max3 - this.mTitleMarginEnd;
                int i29 = layoutParams6.bottomMargin;
            }
            max = i18;
            i6 = max3;
            if (z3) {
                i6 = Math.min(i25, i27);
                max = i18;
            }
        } else {
            int i30 = (z3 ? this.mTitleMarginStart : 0) - iArr[0];
            max = i18 + Math.max(0, i30);
            iArr[0] = Math.max(0, -i30);
            int i31 = max;
            int i32 = max;
            int i33 = i5;
            if (shouldLayout) {
                LayoutParams layoutParams7 = (LayoutParams) this.mTitleTextView.getLayoutParams();
                int measuredWidth2 = max + this.mTitleTextView.getMeasuredWidth();
                int measuredHeight2 = i5 + this.mTitleTextView.getMeasuredHeight();
                this.mTitleTextView.layout(max, i5, measuredWidth2, measuredHeight2);
                i31 = measuredWidth2 + this.mTitleMarginEnd;
                i33 = measuredHeight2 + layoutParams7.bottomMargin;
            }
            if (shouldLayout2) {
                LayoutParams layoutParams8 = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
                int i34 = i33 + layoutParams8.topMargin;
                int measuredWidth3 = max + this.mSubtitleTextView.getMeasuredWidth();
                this.mSubtitleTextView.layout(max, i34, measuredWidth3, i34 + this.mSubtitleTextView.getMeasuredHeight());
                i32 = measuredWidth3 + this.mTitleMarginEnd;
                int i35 = layoutParams8.bottomMargin;
            }
            i6 = i19;
            if (z3) {
                max = Math.max(i31, i32);
                i6 = i19;
            }
        }
        addCustomViewsWithGravity(this.mTempViews, 3);
        int size = this.mTempViews.size();
        for (int i36 = 0; i36 < size; i36++) {
            max = layoutChildLeft(this.mTempViews.get(i36), max, iArr, minimumHeight);
        }
        addCustomViewsWithGravity(this.mTempViews, 5);
        int size2 = this.mTempViews.size();
        int i37 = i6;
        for (int i38 = 0; i38 < size2; i38++) {
            i37 = layoutChildRight(this.mTempViews.get(i38), i37, iArr, minimumHeight);
        }
        addCustomViewsWithGravity(this.mTempViews, 1);
        int viewListMeasuredWidth = getViewListMeasuredWidth(this.mTempViews, iArr);
        int i39 = (paddingLeft + (((width - paddingLeft) - paddingRight) / 2)) - (viewListMeasuredWidth / 2);
        int i40 = i39 + viewListMeasuredWidth;
        if (i39 < max) {
            i8 = max;
        } else {
            i8 = i39;
            if (i40 > i37) {
                i8 = i39 - (i40 - i37);
            }
        }
        int size3 = this.mTempViews.size();
        for (int i41 = 0; i41 < size3; i41++) {
            i8 = layoutChildLeft(this.mTempViews.get(i41), i8, iArr, minimumHeight);
        }
        this.mTempViews.clear();
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        Object[] objArr;
        Object[] objArr2;
        int i3 = 0;
        int i4 = 0;
        int[] iArr = this.mTempMargins;
        if (ViewUtils.isLayoutRtl(this)) {
            objArr = 1;
            objArr2 = null;
        } else {
            objArr = null;
            objArr2 = 1;
        }
        int i5 = 0;
        if (shouldLayout(this.mNavButtonView)) {
            measureChildConstrained(this.mNavButtonView, i, 0, i2, 0, this.mMaxButtonHeight);
            i5 = this.mNavButtonView.getMeasuredWidth() + getHorizontalMargins(this.mNavButtonView);
            i3 = Math.max(0, this.mNavButtonView.getMeasuredHeight() + getVerticalMargins(this.mNavButtonView));
            i4 = ViewUtils.combineMeasuredStates(0, ViewCompat.getMeasuredState(this.mNavButtonView));
        }
        int i6 = i4;
        int i7 = i3;
        if (shouldLayout(this.mCollapseButtonView)) {
            measureChildConstrained(this.mCollapseButtonView, i, 0, i2, 0, this.mMaxButtonHeight);
            i5 = this.mCollapseButtonView.getMeasuredWidth() + getHorizontalMargins(this.mCollapseButtonView);
            i7 = Math.max(i3, this.mCollapseButtonView.getMeasuredHeight() + getVerticalMargins(this.mCollapseButtonView));
            i6 = ViewUtils.combineMeasuredStates(i4, ViewCompat.getMeasuredState(this.mCollapseButtonView));
        }
        int currentContentInsetStart = getCurrentContentInsetStart();
        int max = Math.max(currentContentInsetStart, i5) + 0;
        iArr[objArr == 1 ? 1 : 0] = Math.max(0, currentContentInsetStart - i5);
        int i8 = 0;
        int i9 = i6;
        int i10 = i7;
        if (shouldLayout(this.mMenuView)) {
            measureChildConstrained(this.mMenuView, i, max, i2, 0, this.mMaxButtonHeight);
            i8 = this.mMenuView.getMeasuredWidth() + getHorizontalMargins(this.mMenuView);
            i10 = Math.max(i7, this.mMenuView.getMeasuredHeight() + getVerticalMargins(this.mMenuView));
            i9 = ViewUtils.combineMeasuredStates(i6, ViewCompat.getMeasuredState(this.mMenuView));
        }
        int currentContentInsetEnd = getCurrentContentInsetEnd();
        int max2 = max + Math.max(currentContentInsetEnd, i8);
        iArr[objArr2 == 1 ? 1 : 0] = Math.max(0, currentContentInsetEnd - i8);
        int i11 = max2;
        int i12 = i9;
        int i13 = i10;
        if (shouldLayout(this.mExpandedActionView)) {
            i11 = max2 + measureChildCollapseMargins(this.mExpandedActionView, i, max2, i2, 0, iArr);
            i13 = Math.max(i10, this.mExpandedActionView.getMeasuredHeight() + getVerticalMargins(this.mExpandedActionView));
            i12 = ViewUtils.combineMeasuredStates(i9, ViewCompat.getMeasuredState(this.mExpandedActionView));
        }
        int i14 = i11;
        int i15 = i12;
        int i16 = i13;
        if (shouldLayout(this.mLogoView)) {
            i14 = i11 + measureChildCollapseMargins(this.mLogoView, i, i11, i2, 0, iArr);
            i16 = Math.max(i13, this.mLogoView.getMeasuredHeight() + getVerticalMargins(this.mLogoView));
            i15 = ViewUtils.combineMeasuredStates(i12, ViewCompat.getMeasuredState(this.mLogoView));
        }
        int childCount = getChildCount();
        int i17 = 0;
        int i18 = i16;
        int i19 = i15;
        int i20 = i14;
        while (i17 < childCount) {
            View childAt = getChildAt(i17);
            int i21 = i20;
            int i22 = i19;
            int i23 = i18;
            if (((LayoutParams) childAt.getLayoutParams()).mViewType == 0) {
                i21 = i20;
                i22 = i19;
                i23 = i18;
                if (shouldLayout(childAt)) {
                    i21 = i20 + measureChildCollapseMargins(childAt, i, i20, i2, 0, iArr);
                    i23 = Math.max(i18, childAt.getMeasuredHeight() + getVerticalMargins(childAt));
                    i22 = ViewUtils.combineMeasuredStates(i19, ViewCompat.getMeasuredState(childAt));
                }
            }
            i17++;
            i20 = i21;
            i19 = i22;
            i18 = i23;
        }
        int i24 = 0;
        int i25 = 0;
        int i26 = this.mTitleMarginTop + this.mTitleMarginBottom;
        int i27 = this.mTitleMarginStart + this.mTitleMarginEnd;
        int i28 = i19;
        if (shouldLayout(this.mTitleTextView)) {
            measureChildCollapseMargins(this.mTitleTextView, i, i20 + i27, i2, i26, iArr);
            i24 = this.mTitleTextView.getMeasuredWidth() + getHorizontalMargins(this.mTitleTextView);
            i25 = this.mTitleTextView.getMeasuredHeight() + getVerticalMargins(this.mTitleTextView);
            i28 = ViewUtils.combineMeasuredStates(i19, ViewCompat.getMeasuredState(this.mTitleTextView));
        }
        int i29 = i28;
        int i30 = i25;
        int i31 = i24;
        if (shouldLayout(this.mSubtitleTextView)) {
            i31 = Math.max(i24, measureChildCollapseMargins(this.mSubtitleTextView, i, i20 + i27, i2, i25 + i26, iArr));
            i30 = i25 + this.mSubtitleTextView.getMeasuredHeight() + getVerticalMargins(this.mSubtitleTextView);
            i29 = ViewUtils.combineMeasuredStates(i28, ViewCompat.getMeasuredState(this.mSubtitleTextView));
        }
        int max3 = Math.max(i18, i30);
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int resolveSizeAndState = ViewCompat.resolveSizeAndState(Math.max(i20 + i31 + paddingLeft + paddingRight, getSuggestedMinimumWidth()), i, (-16777216) & i29);
        int resolveSizeAndState2 = ViewCompat.resolveSizeAndState(Math.max(max3 + paddingTop + paddingBottom, getSuggestedMinimumHeight()), i2, i29 << 16);
        if (shouldCollapse()) {
            resolveSizeAndState2 = 0;
        }
        setMeasuredDimension(resolveSizeAndState, resolveSizeAndState2);
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        MenuItem findItem;
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        MenuBuilder peekMenu = this.mMenuView != null ? this.mMenuView.peekMenu() : null;
        if (savedState.expandedMenuItemId != 0 && this.mExpandedMenuPresenter != null && peekMenu != null && (findItem = peekMenu.findItem(savedState.expandedMenuItemId)) != null) {
            MenuItemCompat.expandActionView(findItem);
        }
        if (savedState.isOverflowOpen) {
            postShowOverflowMenu();
        }
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        boolean z = true;
        if (Build.VERSION.SDK_INT >= 17) {
            super.onRtlPropertiesChanged(i);
        }
        RtlSpacingHelper rtlSpacingHelper = this.mContentInsets;
        if (i != 1) {
            z = false;
        }
        rtlSpacingHelper.setDirection(z);
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        if (this.mExpandedMenuPresenter != null && this.mExpandedMenuPresenter.mCurrentExpandedItem != null) {
            savedState.expandedMenuItemId = this.mExpandedMenuPresenter.mCurrentExpandedItem.getItemId();
        }
        savedState.isOverflowOpen = isOverflowMenuShowing();
        return savedState;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
        if (actionMasked == 0) {
            this.mEatingTouch = false;
        }
        if (!this.mEatingTouch) {
            boolean onTouchEvent = super.onTouchEvent(motionEvent);
            if (actionMasked == 0 && !onTouchEvent) {
                this.mEatingTouch = true;
            }
        }
        if (actionMasked == 1 || actionMasked == 3) {
            this.mEatingTouch = false;
            return true;
        }
        return true;
    }

    void removeChildrenForExpandedActionView() {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = getChildAt(childCount);
            if (((LayoutParams) childAt.getLayoutParams()).mViewType != 2 && childAt != this.mMenuView) {
                removeViewAt(childCount);
                this.mHiddenViews.add(childAt);
            }
        }
    }

    public void setContentInsetsRelative(int i, int i2) {
        this.mContentInsets.setRelative(i, i2);
    }

    public void setLogo(Drawable drawable) {
        if (drawable != null) {
            ensureLogoView();
            if (!isChildOrHidden(this.mLogoView)) {
                addSystemView(this.mLogoView, true);
            }
        } else if (this.mLogoView != null && isChildOrHidden(this.mLogoView)) {
            removeView(this.mLogoView);
            this.mHiddenViews.remove(this.mLogoView);
        }
        if (this.mLogoView != null) {
            this.mLogoView.setImageDrawable(drawable);
        }
    }

    public void setLogoDescription(CharSequence charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            ensureLogoView();
        }
        if (this.mLogoView != null) {
            this.mLogoView.setContentDescription(charSequence);
        }
    }

    public void setNavigationContentDescription(@StringRes int i) {
        setNavigationContentDescription(i != 0 ? getContext().getText(i) : null);
    }

    public void setNavigationContentDescription(@Nullable CharSequence charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            ensureNavButtonView();
        }
        if (this.mNavButtonView != null) {
            this.mNavButtonView.setContentDescription(charSequence);
        }
    }

    public void setNavigationIcon(@Nullable Drawable drawable) {
        if (drawable != null) {
            ensureNavButtonView();
            if (!isChildOrHidden(this.mNavButtonView)) {
                addSystemView(this.mNavButtonView, true);
            }
        } else if (this.mNavButtonView != null && isChildOrHidden(this.mNavButtonView)) {
            removeView(this.mNavButtonView);
            this.mHiddenViews.remove(this.mNavButtonView);
        }
        if (this.mNavButtonView != null) {
            this.mNavButtonView.setImageDrawable(drawable);
        }
    }

    public void setNavigationOnClickListener(View.OnClickListener onClickListener) {
        ensureNavButtonView();
        this.mNavButtonView.setOnClickListener(onClickListener);
    }

    public void setPopupTheme(@StyleRes int i) {
        if (this.mPopupTheme != i) {
            this.mPopupTheme = i;
            if (i == 0) {
                this.mPopupContext = getContext();
            } else {
                this.mPopupContext = new ContextThemeWrapper(getContext(), i);
            }
        }
    }

    public void setSubtitle(CharSequence charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            if (this.mSubtitleTextView == null) {
                Context context = getContext();
                this.mSubtitleTextView = new TextView(context);
                this.mSubtitleTextView.setSingleLine();
                this.mSubtitleTextView.setEllipsize(TextUtils.TruncateAt.END);
                if (this.mSubtitleTextAppearance != 0) {
                    this.mSubtitleTextView.setTextAppearance(context, this.mSubtitleTextAppearance);
                }
                if (this.mSubtitleTextColor != 0) {
                    this.mSubtitleTextView.setTextColor(this.mSubtitleTextColor);
                }
            }
            if (!isChildOrHidden(this.mSubtitleTextView)) {
                addSystemView(this.mSubtitleTextView, true);
            }
        } else if (this.mSubtitleTextView != null && isChildOrHidden(this.mSubtitleTextView)) {
            removeView(this.mSubtitleTextView);
            this.mHiddenViews.remove(this.mSubtitleTextView);
        }
        if (this.mSubtitleTextView != null) {
            this.mSubtitleTextView.setText(charSequence);
        }
        this.mSubtitleText = charSequence;
    }

    public void setSubtitleTextAppearance(Context context, @StyleRes int i) {
        this.mSubtitleTextAppearance = i;
        if (this.mSubtitleTextView != null) {
            this.mSubtitleTextView.setTextAppearance(context, i);
        }
    }

    public void setSubtitleTextColor(@ColorInt int i) {
        this.mSubtitleTextColor = i;
        if (this.mSubtitleTextView != null) {
            this.mSubtitleTextView.setTextColor(i);
        }
    }

    public void setTitle(CharSequence charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            if (this.mTitleTextView == null) {
                Context context = getContext();
                this.mTitleTextView = new TextView(context);
                this.mTitleTextView.setSingleLine();
                this.mTitleTextView.setEllipsize(TextUtils.TruncateAt.END);
                if (this.mTitleTextAppearance != 0) {
                    this.mTitleTextView.setTextAppearance(context, this.mTitleTextAppearance);
                }
                if (this.mTitleTextColor != 0) {
                    this.mTitleTextView.setTextColor(this.mTitleTextColor);
                }
            }
            if (!isChildOrHidden(this.mTitleTextView)) {
                addSystemView(this.mTitleTextView, true);
            }
        } else if (this.mTitleTextView != null && isChildOrHidden(this.mTitleTextView)) {
            removeView(this.mTitleTextView);
            this.mHiddenViews.remove(this.mTitleTextView);
        }
        if (this.mTitleTextView != null) {
            this.mTitleTextView.setText(charSequence);
        }
        this.mTitleText = charSequence;
    }

    public void setTitleTextAppearance(Context context, @StyleRes int i) {
        this.mTitleTextAppearance = i;
        if (this.mTitleTextView != null) {
            this.mTitleTextView.setTextAppearance(context, i);
        }
    }

    public void setTitleTextColor(@ColorInt int i) {
        this.mTitleTextColor = i;
        if (this.mTitleTextView != null) {
            this.mTitleTextView.setTextColor(i);
        }
    }

    public boolean showOverflowMenu() {
        return this.mMenuView != null ? this.mMenuView.showOverflowMenu() : false;
    }
}
