package android.support.v7.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ActionProvider;
import android.support.v7.appcompat.R$attr;
import android.support.v7.transition.ActionBarTransition;
import android.support.v7.view.ActionBarPolicy;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.view.menu.BaseMenuPresenter;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.view.menu.MenuPopup;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.view.menu.MenuPresenter;
import android.support.v7.view.menu.MenuView;
import android.support.v7.view.menu.ShowableListMenu;
import android.support.v7.view.menu.SubMenuBuilder;
import android.support.v7.widget.ActionMenuView;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v7/widget/ActionMenuPresenter.class */
public class ActionMenuPresenter extends BaseMenuPresenter implements ActionProvider.SubUiVisibilityListener {
    private final SparseBooleanArray mActionButtonGroups;
    private ActionButtonSubmenu mActionButtonPopup;
    private int mActionItemWidthLimit;
    private boolean mExpandedActionViewsExclusive;
    private int mMaxItems;
    private boolean mMaxItemsSet;
    private int mMinCellSize;
    int mOpenSubMenuId;
    private OverflowMenuButton mOverflowButton;
    private OverflowPopup mOverflowPopup;
    private Drawable mPendingOverflowIcon;
    private boolean mPendingOverflowIconSet;
    private ActionMenuPopupCallback mPopupCallback;
    final PopupPresenterCallback mPopupPresenterCallback;
    private OpenOverflowRunnable mPostedOpenRunnable;
    private boolean mReserveOverflow;
    private boolean mReserveOverflowSet;
    private View mScrapActionButtonView;
    private boolean mStrictWidthLimit;
    private int mWidthLimit;
    private boolean mWidthLimitSet;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/ActionMenuPresenter$ActionButtonSubmenu.class */
    public class ActionButtonSubmenu extends MenuPopupHelper {
        final ActionMenuPresenter this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public ActionButtonSubmenu(ActionMenuPresenter actionMenuPresenter, Context context, SubMenuBuilder subMenuBuilder, View view) {
            super(context, subMenuBuilder, view, false, R$attr.actionOverflowMenuStyle);
            this.this$0 = actionMenuPresenter;
            if (!((MenuItemImpl) subMenuBuilder.getItem()).isActionButton()) {
                setAnchorView(actionMenuPresenter.mOverflowButton == null ? (View) actionMenuPresenter.mMenuView : actionMenuPresenter.mOverflowButton);
            }
            setPresenterCallback(actionMenuPresenter.mPopupPresenterCallback);
        }

        @Override // android.support.v7.view.menu.MenuPopupHelper
        protected void onDismiss() {
            this.this$0.mActionButtonPopup = null;
            this.this$0.mOpenSubMenuId = 0;
            super.onDismiss();
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/ActionMenuPresenter$ActionMenuPopupCallback.class */
    private class ActionMenuPopupCallback extends ActionMenuItemView.PopupCallback {
        final ActionMenuPresenter this$0;

        private ActionMenuPopupCallback(ActionMenuPresenter actionMenuPresenter) {
            this.this$0 = actionMenuPresenter;
        }

        /* synthetic */ ActionMenuPopupCallback(ActionMenuPresenter actionMenuPresenter, ActionMenuPopupCallback actionMenuPopupCallback) {
            this(actionMenuPresenter);
        }

        @Override // android.support.v7.view.menu.ActionMenuItemView.PopupCallback
        public ShowableListMenu getPopup() {
            MenuPopup menuPopup = null;
            if (this.this$0.mActionButtonPopup != null) {
                menuPopup = this.this$0.mActionButtonPopup.getPopup();
            }
            return menuPopup;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/ActionMenuPresenter$OpenOverflowRunnable.class */
    public class OpenOverflowRunnable implements Runnable {
        private OverflowPopup mPopup;
        final ActionMenuPresenter this$0;

        public OpenOverflowRunnable(ActionMenuPresenter actionMenuPresenter, OverflowPopup overflowPopup) {
            this.this$0 = actionMenuPresenter;
            this.mPopup = overflowPopup;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.this$0.mMenu != null) {
                this.this$0.mMenu.changeMenuMode();
            }
            View view = (View) this.this$0.mMenuView;
            if (view != null && view.getWindowToken() != null && this.mPopup.tryShow()) {
                this.this$0.mOverflowPopup = this.mPopup;
            }
            this.this$0.mPostedOpenRunnable = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/ActionMenuPresenter$OverflowMenuButton.class */
    public class OverflowMenuButton extends AppCompatImageView implements ActionMenuView.ActionMenuChildView {
        private final float[] mTempPts;
        final ActionMenuPresenter this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public OverflowMenuButton(ActionMenuPresenter actionMenuPresenter, Context context) {
            super(context, null, R$attr.actionOverflowButtonStyle);
            this.this$0 = actionMenuPresenter;
            this.mTempPts = new float[2];
            setClickable(true);
            setFocusable(true);
            setVisibility(0);
            setEnabled(true);
            setOnTouchListener(new ForwardingListener(this, this) { // from class: android.support.v7.widget.ActionMenuPresenter.OverflowMenuButton.1
                final OverflowMenuButton this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.support.v7.widget.ForwardingListener
                public ShowableListMenu getPopup() {
                    if (this.this$1.this$0.mOverflowPopup == null) {
                        return null;
                    }
                    return this.this$1.this$0.mOverflowPopup.getPopup();
                }

                @Override // android.support.v7.widget.ForwardingListener
                public boolean onForwardingStarted() {
                    this.this$1.this$0.showOverflowMenu();
                    return true;
                }

                @Override // android.support.v7.widget.ForwardingListener
                public boolean onForwardingStopped() {
                    if (this.this$1.this$0.mPostedOpenRunnable != null) {
                        return false;
                    }
                    this.this$1.this$0.hideOverflowMenu();
                    return true;
                }
            });
        }

        @Override // android.support.v7.widget.ActionMenuView.ActionMenuChildView
        public boolean needsDividerAfter() {
            return false;
        }

        @Override // android.support.v7.widget.ActionMenuView.ActionMenuChildView
        public boolean needsDividerBefore() {
            return false;
        }

        @Override // android.view.View
        public boolean performClick() {
            if (super.performClick()) {
                return true;
            }
            playSoundEffect(0);
            this.this$0.showOverflowMenu();
            return true;
        }

        @Override // android.widget.ImageView
        protected boolean setFrame(int i, int i2, int i3, int i4) {
            boolean frame = super.setFrame(i, i2, i3, i4);
            Drawable drawable = getDrawable();
            Drawable background = getBackground();
            if (drawable != null && background != null) {
                int width = getWidth();
                int height = getHeight();
                int max = Math.max(width, height) / 2;
                int paddingLeft = (width + (getPaddingLeft() - getPaddingRight())) / 2;
                int paddingTop = (height + (getPaddingTop() - getPaddingBottom())) / 2;
                DrawableCompat.setHotspotBounds(background, paddingLeft - max, paddingTop - max, paddingLeft + max, paddingTop + max);
            }
            return frame;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/ActionMenuPresenter$OverflowPopup.class */
    public class OverflowPopup extends MenuPopupHelper {
        final ActionMenuPresenter this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public OverflowPopup(ActionMenuPresenter actionMenuPresenter, Context context, MenuBuilder menuBuilder, View view, boolean z) {
            super(context, menuBuilder, view, z, R$attr.actionOverflowMenuStyle);
            this.this$0 = actionMenuPresenter;
            setGravity(8388613);
            setPresenterCallback(actionMenuPresenter.mPopupPresenterCallback);
        }

        @Override // android.support.v7.view.menu.MenuPopupHelper
        protected void onDismiss() {
            if (this.this$0.mMenu != null) {
                this.this$0.mMenu.close();
            }
            this.this$0.mOverflowPopup = null;
            super.onDismiss();
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/ActionMenuPresenter$PopupPresenterCallback.class */
    private class PopupPresenterCallback implements MenuPresenter.Callback {
        final ActionMenuPresenter this$0;

        @Override // android.support.v7.view.menu.MenuPresenter.Callback
        public void onCloseMenu(MenuBuilder menuBuilder, boolean z) {
            if (menuBuilder instanceof SubMenuBuilder) {
                menuBuilder.getRootMenu().close(false);
            }
            MenuPresenter.Callback callback = this.this$0.getCallback();
            if (callback != null) {
                callback.onCloseMenu(menuBuilder, z);
            }
        }

        @Override // android.support.v7.view.menu.MenuPresenter.Callback
        public boolean onOpenSubMenu(MenuBuilder menuBuilder) {
            if (menuBuilder == null) {
                return false;
            }
            this.this$0.mOpenSubMenuId = ((SubMenuBuilder) menuBuilder).getItem().getItemId();
            MenuPresenter.Callback callback = this.this$0.getCallback();
            return callback != null ? callback.onOpenSubMenu(menuBuilder) : false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/ActionMenuPresenter$SavedState.class */
    public static class SavedState implements Parcelable {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: android.support.v7.widget.ActionMenuPresenter.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        public int openSubMenuId;

        SavedState() {
        }

        SavedState(Parcel parcel) {
            this.openSubMenuId = parcel.readInt();
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(this.openSubMenuId);
        }
    }

    private View findViewForItem(MenuItem menuItem) {
        ViewGroup viewGroup = (ViewGroup) this.mMenuView;
        if (viewGroup == null) {
            return null;
        }
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if ((childAt instanceof MenuView.ItemView) && ((MenuView.ItemView) childAt).getItemData() == menuItem) {
                return childAt;
            }
        }
        return null;
    }

    @Override // android.support.v7.view.menu.BaseMenuPresenter
    public void bindItemView(MenuItemImpl menuItemImpl, MenuView.ItemView itemView) {
        itemView.initialize(menuItemImpl, 0);
        ActionMenuItemView actionMenuItemView = (ActionMenuItemView) itemView;
        actionMenuItemView.setItemInvoker((ActionMenuView) this.mMenuView);
        if (this.mPopupCallback == null) {
            this.mPopupCallback = new ActionMenuPopupCallback(this, null);
        }
        actionMenuItemView.setPopupCallback(this.mPopupCallback);
    }

    public boolean dismissPopupMenus() {
        return hideOverflowMenu() | hideSubMenus();
    }

    @Override // android.support.v7.view.menu.BaseMenuPresenter
    public boolean filterLeftoverView(ViewGroup viewGroup, int i) {
        if (viewGroup.getChildAt(i) == this.mOverflowButton) {
            return false;
        }
        return super.filterLeftoverView(viewGroup, i);
    }

    /* JADX WARN: Code restructure failed: missing block: B:27:0x00ae, code lost:
        if ((r14 + r15) > r10) goto L30;
     */
    @Override // android.support.v7.view.menu.BaseMenuPresenter, android.support.v7.view.menu.MenuPresenter
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean flagActionItems() {
        ArrayList<MenuItemImpl> arrayList;
        int i;
        int i2;
        int i3;
        if (this.mMenu != null) {
            arrayList = this.mMenu.getVisibleItems();
            i = arrayList.size();
        } else {
            arrayList = null;
            i = 0;
        }
        int i4 = this.mMaxItems;
        int i5 = this.mActionItemWidthLimit;
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        ViewGroup viewGroup = (ViewGroup) this.mMenuView;
        int i6 = 0;
        int i7 = 0;
        boolean z = false;
        int i8 = 0;
        while (i8 < i) {
            MenuItemImpl menuItemImpl = arrayList.get(i8);
            if (menuItemImpl.requiresActionButton()) {
                i6++;
            } else if (menuItemImpl.requestsActionButton()) {
                i7++;
            } else {
                z = true;
            }
            int i9 = i4;
            if (this.mExpandedActionViewsExclusive) {
                i9 = i4;
                if (menuItemImpl.isActionViewExpanded()) {
                    i9 = 0;
                }
            }
            i8++;
            i4 = i9;
        }
        int i10 = i4;
        if (this.mReserveOverflow) {
            if (!z) {
                i10 = i4;
            }
            i10 = i4 - 1;
        }
        int i11 = i10 - i6;
        SparseBooleanArray sparseBooleanArray = this.mActionButtonGroups;
        sparseBooleanArray.clear();
        int i12 = 0;
        int i13 = 0;
        if (this.mStrictWidthLimit) {
            i13 = i5 / this.mMinCellSize;
            i12 = this.mMinCellSize + ((i5 % this.mMinCellSize) / i13);
        }
        int i14 = i5;
        int i15 = 0;
        int i16 = 0;
        while (i15 < i) {
            MenuItemImpl menuItemImpl2 = arrayList.get(i15);
            if (menuItemImpl2.requiresActionButton()) {
                View itemView = getItemView(menuItemImpl2, this.mScrapActionButtonView, viewGroup);
                if (this.mScrapActionButtonView == null) {
                    this.mScrapActionButtonView = itemView;
                }
                if (this.mStrictWidthLimit) {
                    i13 -= ActionMenuView.measureChildForCells(itemView, i12, i13, makeMeasureSpec, 0);
                } else {
                    itemView.measure(makeMeasureSpec, makeMeasureSpec);
                }
                int measuredWidth = itemView.getMeasuredWidth();
                i2 = i14 - measuredWidth;
                int i17 = i16;
                if (i16 == 0) {
                    i17 = measuredWidth;
                }
                int groupId = menuItemImpl2.getGroupId();
                if (groupId != 0) {
                    sparseBooleanArray.put(groupId, true);
                }
                menuItemImpl2.setIsActionButton(true);
                i16 = i17;
            } else if (menuItemImpl2.requestsActionButton()) {
                int groupId2 = menuItemImpl2.getGroupId();
                boolean z2 = sparseBooleanArray.get(groupId2);
                boolean z3 = ((i11 > 0 || z2) && i14 > 0) ? !this.mStrictWidthLimit || i13 > 0 : false;
                int i18 = i13;
                int i19 = i16;
                boolean z4 = z3;
                i2 = i14;
                if (z3) {
                    View itemView2 = getItemView(menuItemImpl2, this.mScrapActionButtonView, viewGroup);
                    if (this.mScrapActionButtonView == null) {
                        this.mScrapActionButtonView = itemView2;
                    }
                    if (this.mStrictWidthLimit) {
                        int measureChildForCells = ActionMenuView.measureChildForCells(itemView2, i12, i13, makeMeasureSpec, 0);
                        int i20 = i13 - measureChildForCells;
                        i13 = i20;
                        if (measureChildForCells == 0) {
                            z3 = false;
                            i13 = i20;
                        }
                    } else {
                        itemView2.measure(makeMeasureSpec, makeMeasureSpec);
                    }
                    int measuredWidth2 = itemView2.getMeasuredWidth();
                    i2 = i14 - measuredWidth2;
                    i19 = i16;
                    if (i16 == 0) {
                        i19 = measuredWidth2;
                    }
                    if (this.mStrictWidthLimit) {
                        z4 = z3 & (i2 >= 0);
                        i18 = i13;
                    } else {
                        z4 = z3 & (i2 + i19 > 0);
                        i18 = i13;
                    }
                }
                if (!z4 || groupId2 == 0) {
                    i3 = i11;
                    if (z2) {
                        sparseBooleanArray.put(groupId2, false);
                        int i21 = 0;
                        while (true) {
                            i3 = i11;
                            if (i21 >= i15) {
                                break;
                            }
                            MenuItemImpl menuItemImpl3 = arrayList.get(i21);
                            int i22 = i11;
                            if (menuItemImpl3.getGroupId() == groupId2) {
                                i22 = i11;
                                if (menuItemImpl3.isActionButton()) {
                                    i22 = i11 + 1;
                                }
                                menuItemImpl3.setIsActionButton(false);
                            }
                            i21++;
                            i11 = i22;
                        }
                    }
                } else {
                    sparseBooleanArray.put(groupId2, true);
                    i3 = i11;
                }
                i11 = i3;
                if (z4) {
                    i11 = i3 - 1;
                }
                menuItemImpl2.setIsActionButton(z4);
                i13 = i18;
                i16 = i19;
            } else {
                menuItemImpl2.setIsActionButton(false);
                i2 = i14;
            }
            i15++;
            i14 = i2;
        }
        return true;
    }

    @Override // android.support.v7.view.menu.BaseMenuPresenter
    public View getItemView(MenuItemImpl menuItemImpl, View view, ViewGroup viewGroup) {
        View actionView = menuItemImpl.getActionView();
        if (actionView == null || menuItemImpl.hasCollapsibleActionView()) {
            actionView = super.getItemView(menuItemImpl, view, viewGroup);
        }
        actionView.setVisibility(menuItemImpl.isActionViewExpanded() ? 8 : 0);
        ActionMenuView actionMenuView = (ActionMenuView) viewGroup;
        ViewGroup.LayoutParams layoutParams = actionView.getLayoutParams();
        if (!actionMenuView.checkLayoutParams(layoutParams)) {
            actionView.setLayoutParams(actionMenuView.generateLayoutParams(layoutParams));
        }
        return actionView;
    }

    public boolean hideOverflowMenu() {
        if (this.mPostedOpenRunnable != null && this.mMenuView != null) {
            ((View) this.mMenuView).removeCallbacks(this.mPostedOpenRunnable);
            this.mPostedOpenRunnable = null;
            return true;
        }
        OverflowPopup overflowPopup = this.mOverflowPopup;
        if (overflowPopup != null) {
            overflowPopup.dismiss();
            return true;
        }
        return false;
    }

    public boolean hideSubMenus() {
        if (this.mActionButtonPopup != null) {
            this.mActionButtonPopup.dismiss();
            return true;
        }
        return false;
    }

    @Override // android.support.v7.view.menu.BaseMenuPresenter, android.support.v7.view.menu.MenuPresenter
    public void initForMenu(@NonNull Context context, @Nullable MenuBuilder menuBuilder) {
        super.initForMenu(context, menuBuilder);
        Resources resources = context.getResources();
        ActionBarPolicy actionBarPolicy = ActionBarPolicy.get(context);
        if (!this.mReserveOverflowSet) {
            this.mReserveOverflow = actionBarPolicy.showsOverflowMenuButton();
        }
        if (!this.mWidthLimitSet) {
            this.mWidthLimit = actionBarPolicy.getEmbeddedMenuWidthLimit();
        }
        if (!this.mMaxItemsSet) {
            this.mMaxItems = actionBarPolicy.getMaxActionButtons();
        }
        int i = this.mWidthLimit;
        if (this.mReserveOverflow) {
            if (this.mOverflowButton == null) {
                this.mOverflowButton = new OverflowMenuButton(this, this.mSystemContext);
                if (this.mPendingOverflowIconSet) {
                    this.mOverflowButton.setImageDrawable(this.mPendingOverflowIcon);
                    this.mPendingOverflowIcon = null;
                    this.mPendingOverflowIconSet = false;
                }
                int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
                this.mOverflowButton.measure(makeMeasureSpec, makeMeasureSpec);
            }
            i -= this.mOverflowButton.getMeasuredWidth();
        } else {
            this.mOverflowButton = null;
        }
        this.mActionItemWidthLimit = i;
        this.mMinCellSize = (int) (resources.getDisplayMetrics().density * 56.0f);
        this.mScrapActionButtonView = null;
    }

    public boolean isOverflowMenuShowing() {
        return this.mOverflowPopup != null ? this.mOverflowPopup.isShowing() : false;
    }

    @Override // android.support.v7.view.menu.BaseMenuPresenter, android.support.v7.view.menu.MenuPresenter
    public void onCloseMenu(MenuBuilder menuBuilder, boolean z) {
        dismissPopupMenus();
        super.onCloseMenu(menuBuilder, z);
    }

    public void onConfigurationChanged(Configuration configuration) {
        if (!this.mMaxItemsSet) {
            this.mMaxItems = ActionBarPolicy.get(this.mContext).getMaxActionButtons();
        }
        if (this.mMenu != null) {
            this.mMenu.onItemsChanged(true);
        }
    }

    @Override // android.support.v7.view.menu.BaseMenuPresenter, android.support.v7.view.menu.MenuPresenter
    public boolean onSubMenuSelected(SubMenuBuilder subMenuBuilder) {
        SubMenuBuilder subMenuBuilder2;
        boolean z;
        if (subMenuBuilder.hasVisibleItems()) {
            SubMenuBuilder subMenuBuilder3 = subMenuBuilder;
            while (true) {
                subMenuBuilder2 = subMenuBuilder3;
                if (subMenuBuilder2.getParentMenu() == this.mMenu) {
                    break;
                }
                subMenuBuilder3 = (SubMenuBuilder) subMenuBuilder2.getParentMenu();
            }
            View findViewForItem = findViewForItem(subMenuBuilder2.getItem());
            if (findViewForItem == null) {
                return false;
            }
            this.mOpenSubMenuId = subMenuBuilder.getItem().getItemId();
            int size = subMenuBuilder.size();
            int i = 0;
            while (true) {
                z = false;
                if (i >= size) {
                    break;
                }
                MenuItem item = subMenuBuilder.getItem(i);
                if (item.isVisible() && item.getIcon() != null) {
                    z = true;
                    break;
                }
                i++;
            }
            this.mActionButtonPopup = new ActionButtonSubmenu(this, this.mContext, subMenuBuilder, findViewForItem);
            this.mActionButtonPopup.setForceShowIcon(z);
            this.mActionButtonPopup.show();
            super.onSubMenuSelected(subMenuBuilder);
            return true;
        }
        return false;
    }

    @Override // android.support.v4.view.ActionProvider.SubUiVisibilityListener
    public void onSubUiVisibilityChanged(boolean z) {
        if (z) {
            super.onSubMenuSelected(null);
        } else if (this.mMenu != null) {
            this.mMenu.close(false);
        }
    }

    @Override // android.support.v7.view.menu.BaseMenuPresenter
    public boolean shouldIncludeItem(int i, MenuItemImpl menuItemImpl) {
        return menuItemImpl.isActionButton();
    }

    public boolean showOverflowMenu() {
        if (!this.mReserveOverflow || isOverflowMenuShowing() || this.mMenu == null || this.mMenuView == null || this.mPostedOpenRunnable != null || this.mMenu.getNonActionItems().isEmpty()) {
            return false;
        }
        this.mPostedOpenRunnable = new OpenOverflowRunnable(this, new OverflowPopup(this, this.mContext, this.mMenu, this.mOverflowButton, true));
        ((View) this.mMenuView).post(this.mPostedOpenRunnable);
        super.onSubMenuSelected(null);
        return true;
    }

    @Override // android.support.v7.view.menu.BaseMenuPresenter, android.support.v7.view.menu.MenuPresenter
    public void updateMenuView(boolean z) {
        ViewGroup viewGroup = (ViewGroup) ((View) this.mMenuView).getParent();
        if (viewGroup != null) {
            ActionBarTransition.beginDelayedTransition(viewGroup);
        }
        super.updateMenuView(z);
        ((View) this.mMenuView).requestLayout();
        if (this.mMenu != null) {
            ArrayList<MenuItemImpl> actionItems = this.mMenu.getActionItems();
            int size = actionItems.size();
            for (int i = 0; i < size; i++) {
                ActionProvider supportActionProvider = actionItems.get(i).getSupportActionProvider();
                if (supportActionProvider != null) {
                    supportActionProvider.setSubUiVisibilityListener(this);
                }
            }
        }
        ArrayList<MenuItemImpl> nonActionItems = this.mMenu != null ? this.mMenu.getNonActionItems() : null;
        boolean z2 = false;
        if (this.mReserveOverflow) {
            z2 = false;
            if (nonActionItems != null) {
                int size2 = nonActionItems.size();
                z2 = size2 == 1 ? !nonActionItems.get(0).isActionViewExpanded() : size2 > 0;
            }
        }
        if (z2) {
            if (this.mOverflowButton == null) {
                this.mOverflowButton = new OverflowMenuButton(this, this.mSystemContext);
            }
            ViewGroup viewGroup2 = (ViewGroup) this.mOverflowButton.getParent();
            if (viewGroup2 != this.mMenuView) {
                if (viewGroup2 != null) {
                    viewGroup2.removeView(this.mOverflowButton);
                }
                ActionMenuView actionMenuView = (ActionMenuView) this.mMenuView;
                actionMenuView.addView(this.mOverflowButton, actionMenuView.generateOverflowButtonLayoutParams());
            }
        } else if (this.mOverflowButton != null && this.mOverflowButton.getParent() == this.mMenuView) {
            ((ViewGroup) this.mMenuView).removeView(this.mOverflowButton);
        }
        ((ActionMenuView) this.mMenuView).setOverflowReserved(this.mReserveOverflow);
    }
}
