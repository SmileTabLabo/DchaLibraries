package android.support.v7.view.menu;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.content.res.ConfigurationHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.appcompat.R$styleable;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.ForwardingListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
/* loaded from: a.zip:android/support/v7/view/menu/ActionMenuItemView.class */
public class ActionMenuItemView extends AppCompatTextView implements MenuView.ItemView, View.OnClickListener, View.OnLongClickListener, ActionMenuView.ActionMenuChildView {
    private boolean mAllowTextWithIcon;
    private boolean mExpandedFormat;
    private ForwardingListener mForwardingListener;
    private Drawable mIcon;
    private MenuItemImpl mItemData;
    private MenuBuilder.ItemInvoker mItemInvoker;
    private int mMaxIconSize;
    private int mMinWidth;
    private PopupCallback mPopupCallback;
    private int mSavedPaddingLeft;
    private CharSequence mTitle;

    /* loaded from: a.zip:android/support/v7/view/menu/ActionMenuItemView$ActionMenuItemForwardingListener.class */
    private class ActionMenuItemForwardingListener extends ForwardingListener {
        final ActionMenuItemView this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public ActionMenuItemForwardingListener(ActionMenuItemView actionMenuItemView) {
            super(actionMenuItemView);
            this.this$0 = actionMenuItemView;
        }

        @Override // android.support.v7.widget.ForwardingListener
        public ShowableListMenu getPopup() {
            if (this.this$0.mPopupCallback != null) {
                return this.this$0.mPopupCallback.getPopup();
            }
            return null;
        }

        @Override // android.support.v7.widget.ForwardingListener
        protected boolean onForwardingStarted() {
            boolean z = false;
            if (this.this$0.mItemInvoker == null || !this.this$0.mItemInvoker.invokeItem(this.this$0.mItemData)) {
                return false;
            }
            ShowableListMenu popup = getPopup();
            if (popup != null) {
                z = popup.isShowing();
            }
            return z;
        }
    }

    /* loaded from: a.zip:android/support/v7/view/menu/ActionMenuItemView$PopupCallback.class */
    public static abstract class PopupCallback {
        public abstract ShowableListMenu getPopup();
    }

    public ActionMenuItemView(Context context) {
        this(context, null);
    }

    public ActionMenuItemView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ActionMenuItemView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        Resources resources = context.getResources();
        this.mAllowTextWithIcon = shouldAllowTextWithIcon();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.ActionMenuItemView, i, 0);
        this.mMinWidth = obtainStyledAttributes.getDimensionPixelSize(R$styleable.ActionMenuItemView_android_minWidth, 0);
        obtainStyledAttributes.recycle();
        this.mMaxIconSize = (int) ((32.0f * resources.getDisplayMetrics().density) + 0.5f);
        setOnClickListener(this);
        setOnLongClickListener(this);
        this.mSavedPaddingLeft = -1;
        setSaveEnabled(false);
    }

    private boolean shouldAllowTextWithIcon() {
        Configuration configuration = getContext().getResources().getConfiguration();
        int screenWidthDp = ConfigurationHelper.getScreenWidthDp(getResources());
        int screenHeightDp = ConfigurationHelper.getScreenHeightDp(getResources());
        boolean z = true;
        if (screenWidthDp < 480) {
            if (screenWidthDp < 640 || screenHeightDp < 480) {
                z = true;
                if (configuration.orientation != 2) {
                    z = false;
                }
            } else {
                z = true;
            }
        }
        return z;
    }

    private void updateTextButtonVisibility() {
        CharSequence charSequence = null;
        boolean z = !TextUtils.isEmpty(this.mTitle);
        boolean z2 = true;
        if (this.mIcon != null) {
            if (this.mItemData.showsTextAsAction()) {
                z2 = true;
                if (!this.mAllowTextWithIcon) {
                    z2 = this.mExpandedFormat;
                }
            } else {
                z2 = false;
            }
        }
        if (z & z2) {
            charSequence = this.mTitle;
        }
        setText(charSequence);
    }

    @Override // android.support.v7.view.menu.MenuView.ItemView
    public MenuItemImpl getItemData() {
        return this.mItemData;
    }

    public boolean hasText() {
        return !TextUtils.isEmpty(getText());
    }

    @Override // android.support.v7.view.menu.MenuView.ItemView
    public void initialize(MenuItemImpl menuItemImpl, int i) {
        this.mItemData = menuItemImpl;
        setIcon(menuItemImpl.getIcon());
        setTitle(menuItemImpl.getTitleForItemView(this));
        setId(menuItemImpl.getItemId());
        setVisibility(menuItemImpl.isVisible() ? 0 : 8);
        setEnabled(menuItemImpl.isEnabled());
        if (menuItemImpl.hasSubMenu() && this.mForwardingListener == null) {
            this.mForwardingListener = new ActionMenuItemForwardingListener(this);
        }
    }

    @Override // android.support.v7.widget.ActionMenuView.ActionMenuChildView
    public boolean needsDividerAfter() {
        return hasText();
    }

    @Override // android.support.v7.widget.ActionMenuView.ActionMenuChildView
    public boolean needsDividerBefore() {
        boolean z = false;
        if (hasText()) {
            z = false;
            if (this.mItemData.getIcon() == null) {
                z = true;
            }
        }
        return z;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.mItemInvoker != null) {
            this.mItemInvoker.invokeItem(this.mItemData);
        }
    }

    @Override // android.widget.TextView, android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        if (Build.VERSION.SDK_INT >= 8) {
            super.onConfigurationChanged(configuration);
        }
        this.mAllowTextWithIcon = shouldAllowTextWithIcon();
        updateTextButtonVisibility();
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        if (hasText()) {
            return false;
        }
        int[] iArr = new int[2];
        Rect rect = new Rect();
        getLocationOnScreen(iArr);
        getWindowVisibleDisplayFrame(rect);
        Context context = getContext();
        int width = getWidth();
        int height = getHeight();
        int i = iArr[1];
        int i2 = height / 2;
        int i3 = iArr[0] + (width / 2);
        int i4 = i3;
        if (ViewCompat.getLayoutDirection(view) == 0) {
            i4 = context.getResources().getDisplayMetrics().widthPixels - i3;
        }
        Toast makeText = Toast.makeText(context, this.mItemData.getTitle(), 0);
        if (i + i2 < rect.height()) {
            makeText.setGravity(8388661, i4, (iArr[1] + height) - rect.top);
        } else {
            makeText.setGravity(81, 0, height);
        }
        makeText.show();
        return true;
    }

    @Override // android.widget.TextView, android.view.View
    protected void onMeasure(int i, int i2) {
        boolean hasText = hasText();
        if (hasText && this.mSavedPaddingLeft >= 0) {
            super.setPadding(this.mSavedPaddingLeft, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        }
        super.onMeasure(i, i2);
        int mode = View.MeasureSpec.getMode(i);
        int size = View.MeasureSpec.getSize(i);
        int measuredWidth = getMeasuredWidth();
        int min = mode == Integer.MIN_VALUE ? Math.min(size, this.mMinWidth) : this.mMinWidth;
        if (mode != 1073741824 && this.mMinWidth > 0 && measuredWidth < min) {
            super.onMeasure(View.MeasureSpec.makeMeasureSpec(min, 1073741824), i2);
        }
        if (hasText || this.mIcon == null) {
            return;
        }
        super.setPadding((getMeasuredWidth() - this.mIcon.getBounds().width()) / 2, getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    @Override // android.widget.TextView, android.view.View
    public void onRestoreInstanceState(Parcelable parcelable) {
        super.onRestoreInstanceState(null);
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mItemData.hasSubMenu() && this.mForwardingListener != null && this.mForwardingListener.onTouch(this, motionEvent)) {
            return true;
        }
        return super.onTouchEvent(motionEvent);
    }

    @Override // android.support.v7.view.menu.MenuView.ItemView
    public boolean prefersCondensedTitle() {
        return true;
    }

    public void setIcon(Drawable drawable) {
        this.mIcon = drawable;
        if (drawable != null) {
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            int i = intrinsicHeight;
            int i2 = intrinsicWidth;
            if (intrinsicWidth > this.mMaxIconSize) {
                float f = this.mMaxIconSize / intrinsicWidth;
                i2 = this.mMaxIconSize;
                i = (int) (intrinsicHeight * f);
            }
            int i3 = i;
            int i4 = i2;
            if (i > this.mMaxIconSize) {
                float f2 = this.mMaxIconSize / i;
                i3 = this.mMaxIconSize;
                i4 = (int) (i2 * f2);
            }
            drawable.setBounds(0, 0, i4, i3);
        }
        setCompoundDrawables(drawable, null, null, null);
        updateTextButtonVisibility();
    }

    public void setItemInvoker(MenuBuilder.ItemInvoker itemInvoker) {
        this.mItemInvoker = itemInvoker;
    }

    @Override // android.widget.TextView, android.view.View
    public void setPadding(int i, int i2, int i3, int i4) {
        this.mSavedPaddingLeft = i;
        super.setPadding(i, i2, i3, i4);
    }

    public void setPopupCallback(PopupCallback popupCallback) {
        this.mPopupCallback = popupCallback;
    }

    public void setTitle(CharSequence charSequence) {
        this.mTitle = charSequence;
        setContentDescription(this.mTitle);
        updateTextButtonVisibility();
    }
}
