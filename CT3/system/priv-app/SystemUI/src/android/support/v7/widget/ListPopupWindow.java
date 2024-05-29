package android.support.v7.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.appcompat.R$attr;
import android.support.v7.appcompat.R$styleable;
import android.support.v7.view.menu.ShowableListMenu;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import java.lang.reflect.Method;
/* loaded from: a.zip:android/support/v7/widget/ListPopupWindow.class */
public class ListPopupWindow implements ShowableListMenu {
    private static Method sClipToWindowEnabledMethod;
    private static Method sGetMaxAvailableHeightMethod;
    private static Method sSetEpicenterBoundsMethod;
    private ListAdapter mAdapter;
    private Context mContext;
    private boolean mDropDownAlwaysVisible;
    private View mDropDownAnchorView;
    private int mDropDownGravity;
    private int mDropDownHeight;
    private int mDropDownHorizontalOffset;
    private DropDownListView mDropDownList;
    private Drawable mDropDownListHighlight;
    private int mDropDownVerticalOffset;
    private boolean mDropDownVerticalOffsetSet;
    private int mDropDownWidth;
    private int mDropDownWindowLayoutType;
    private Rect mEpicenterBounds;
    private boolean mForceIgnoreOutsideTouch;
    private final Handler mHandler;
    private final ListSelectorHider mHideSelector;
    private boolean mIsAnimatedFromAnchor;
    private AdapterView.OnItemClickListener mItemClickListener;
    private AdapterView.OnItemSelectedListener mItemSelectedListener;
    int mListItemExpandMaximum;
    private boolean mModal;
    private DataSetObserver mObserver;
    PopupWindow mPopup;
    private int mPromptPosition;
    private View mPromptView;
    private final ResizePopupRunnable mResizePopupRunnable;
    private final PopupScrollListener mScrollListener;
    private Runnable mShowDropDownRunnable;
    private final Rect mTempRect;
    private final PopupTouchInterceptor mTouchInterceptor;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/ListPopupWindow$ListSelectorHider.class */
    public class ListSelectorHider implements Runnable {
        final ListPopupWindow this$0;

        private ListSelectorHider(ListPopupWindow listPopupWindow) {
            this.this$0 = listPopupWindow;
        }

        /* synthetic */ ListSelectorHider(ListPopupWindow listPopupWindow, ListSelectorHider listSelectorHider) {
            this(listPopupWindow);
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.clearListSelection();
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/ListPopupWindow$PopupDataSetObserver.class */
    private class PopupDataSetObserver extends DataSetObserver {
        final ListPopupWindow this$0;

        private PopupDataSetObserver(ListPopupWindow listPopupWindow) {
            this.this$0 = listPopupWindow;
        }

        /* synthetic */ PopupDataSetObserver(ListPopupWindow listPopupWindow, PopupDataSetObserver popupDataSetObserver) {
            this(listPopupWindow);
        }

        @Override // android.database.DataSetObserver
        public void onChanged() {
            if (this.this$0.isShowing()) {
                this.this$0.show();
            }
        }

        @Override // android.database.DataSetObserver
        public void onInvalidated() {
            this.this$0.dismiss();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/ListPopupWindow$PopupScrollListener.class */
    public class PopupScrollListener implements AbsListView.OnScrollListener {
        final ListPopupWindow this$0;

        private PopupScrollListener(ListPopupWindow listPopupWindow) {
            this.this$0 = listPopupWindow;
        }

        /* synthetic */ PopupScrollListener(ListPopupWindow listPopupWindow, PopupScrollListener popupScrollListener) {
            this(listPopupWindow);
        }

        @Override // android.widget.AbsListView.OnScrollListener
        public void onScroll(AbsListView absListView, int i, int i2, int i3) {
        }

        @Override // android.widget.AbsListView.OnScrollListener
        public void onScrollStateChanged(AbsListView absListView, int i) {
            if (i != 1 || this.this$0.isInputMethodNotNeeded() || this.this$0.mPopup.getContentView() == null) {
                return;
            }
            this.this$0.mHandler.removeCallbacks(this.this$0.mResizePopupRunnable);
            this.this$0.mResizePopupRunnable.run();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/ListPopupWindow$PopupTouchInterceptor.class */
    public class PopupTouchInterceptor implements View.OnTouchListener {
        final ListPopupWindow this$0;

        private PopupTouchInterceptor(ListPopupWindow listPopupWindow) {
            this.this$0 = listPopupWindow;
        }

        /* synthetic */ PopupTouchInterceptor(ListPopupWindow listPopupWindow, PopupTouchInterceptor popupTouchInterceptor) {
            this(listPopupWindow);
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();
            if (action == 0 && this.this$0.mPopup != null && this.this$0.mPopup.isShowing() && x >= 0 && x < this.this$0.mPopup.getWidth() && y >= 0 && y < this.this$0.mPopup.getHeight()) {
                this.this$0.mHandler.postDelayed(this.this$0.mResizePopupRunnable, 250L);
                return false;
            } else if (action == 1) {
                this.this$0.mHandler.removeCallbacks(this.this$0.mResizePopupRunnable);
                return false;
            } else {
                return false;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/ListPopupWindow$ResizePopupRunnable.class */
    public class ResizePopupRunnable implements Runnable {
        final ListPopupWindow this$0;

        private ResizePopupRunnable(ListPopupWindow listPopupWindow) {
            this.this$0 = listPopupWindow;
        }

        /* synthetic */ ResizePopupRunnable(ListPopupWindow listPopupWindow, ResizePopupRunnable resizePopupRunnable) {
            this(listPopupWindow);
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.this$0.mDropDownList == null || !ViewCompat.isAttachedToWindow(this.this$0.mDropDownList) || this.this$0.mDropDownList.getCount() <= this.this$0.mDropDownList.getChildCount() || this.this$0.mDropDownList.getChildCount() > this.this$0.mListItemExpandMaximum) {
                return;
            }
            this.this$0.mPopup.setInputMethodMode(2);
            this.this$0.show();
        }
    }

    static {
        try {
            sClipToWindowEnabledMethod = PopupWindow.class.getDeclaredMethod("setClipToScreenEnabled", Boolean.TYPE);
        } catch (NoSuchMethodException e) {
            Log.i("ListPopupWindow", "Could not find method setClipToScreenEnabled() on PopupWindow. Oh well.");
        }
        try {
            sGetMaxAvailableHeightMethod = PopupWindow.class.getDeclaredMethod("getMaxAvailableHeight", View.class, Integer.TYPE, Boolean.TYPE);
        } catch (NoSuchMethodException e2) {
            Log.i("ListPopupWindow", "Could not find method getMaxAvailableHeight(View, int, boolean) on PopupWindow. Oh well.");
        }
        try {
            sSetEpicenterBoundsMethod = PopupWindow.class.getDeclaredMethod("setEpicenterBounds", Rect.class);
        } catch (NoSuchMethodException e3) {
            Log.i("ListPopupWindow", "Could not find method setEpicenterBounds(Rect) on PopupWindow. Oh well.");
        }
    }

    public ListPopupWindow(@NonNull Context context) {
        this(context, null, R$attr.listPopupWindowStyle);
    }

    public ListPopupWindow(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.listPopupWindowStyle);
    }

    public ListPopupWindow(@NonNull Context context, @Nullable AttributeSet attributeSet, @AttrRes int i) {
        this(context, attributeSet, i, 0);
    }

    public ListPopupWindow(@NonNull Context context, @Nullable AttributeSet attributeSet, @AttrRes int i, @StyleRes int i2) {
        this.mDropDownHeight = -2;
        this.mDropDownWidth = -2;
        this.mDropDownWindowLayoutType = 1002;
        this.mIsAnimatedFromAnchor = true;
        this.mDropDownGravity = 0;
        this.mDropDownAlwaysVisible = false;
        this.mForceIgnoreOutsideTouch = false;
        this.mListItemExpandMaximum = Integer.MAX_VALUE;
        this.mPromptPosition = 0;
        this.mResizePopupRunnable = new ResizePopupRunnable(this, null);
        this.mTouchInterceptor = new PopupTouchInterceptor(this, null);
        this.mScrollListener = new PopupScrollListener(this, null);
        this.mHideSelector = new ListSelectorHider(this, null);
        this.mTempRect = new Rect();
        this.mContext = context;
        this.mHandler = new Handler(context.getMainLooper());
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.ListPopupWindow, i, i2);
        this.mDropDownHorizontalOffset = obtainStyledAttributes.getDimensionPixelOffset(R$styleable.ListPopupWindow_android_dropDownHorizontalOffset, 0);
        this.mDropDownVerticalOffset = obtainStyledAttributes.getDimensionPixelOffset(R$styleable.ListPopupWindow_android_dropDownVerticalOffset, 0);
        if (this.mDropDownVerticalOffset != 0) {
            this.mDropDownVerticalOffsetSet = true;
        }
        obtainStyledAttributes.recycle();
        if (Build.VERSION.SDK_INT >= 11) {
            this.mPopup = new AppCompatPopupWindow(context, attributeSet, i, i2);
        } else {
            this.mPopup = new AppCompatPopupWindow(context, attributeSet, i);
        }
        this.mPopup.setInputMethodMode(1);
    }

    private int buildDropDown() {
        int i;
        int makeMeasureSpec;
        int i2;
        int i3;
        int i4 = 0;
        if (this.mDropDownList == null) {
            Context context = this.mContext;
            this.mShowDropDownRunnable = new Runnable(this) { // from class: android.support.v7.widget.ListPopupWindow.2
                final ListPopupWindow this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    View anchorView = this.this$0.getAnchorView();
                    if (anchorView == null || anchorView.getWindowToken() == null) {
                        return;
                    }
                    this.this$0.show();
                }
            };
            this.mDropDownList = createDropDownListView(context, !this.mModal);
            if (this.mDropDownListHighlight != null) {
                this.mDropDownList.setSelector(this.mDropDownListHighlight);
            }
            this.mDropDownList.setAdapter(this.mAdapter);
            this.mDropDownList.setOnItemClickListener(this.mItemClickListener);
            this.mDropDownList.setFocusable(true);
            this.mDropDownList.setFocusableInTouchMode(true);
            this.mDropDownList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(this) { // from class: android.support.v7.widget.ListPopupWindow.3
                final ListPopupWindow this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.widget.AdapterView.OnItemSelectedListener
                public void onItemSelected(AdapterView<?> adapterView, View view, int i5, long j) {
                    DropDownListView dropDownListView;
                    if (i5 == -1 || (dropDownListView = this.this$0.mDropDownList) == null) {
                        return;
                    }
                    dropDownListView.setListSelectionHidden(false);
                }

                @Override // android.widget.AdapterView.OnItemSelectedListener
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            this.mDropDownList.setOnScrollListener(this.mScrollListener);
            if (this.mItemSelectedListener != null) {
                this.mDropDownList.setOnItemSelectedListener(this.mItemSelectedListener);
            }
            DropDownListView dropDownListView = this.mDropDownList;
            View view = this.mPromptView;
            LinearLayout linearLayout = dropDownListView;
            if (view != null) {
                LinearLayout linearLayout2 = new LinearLayout(context);
                linearLayout2.setOrientation(1);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, 0, 1.0f);
                switch (this.mPromptPosition) {
                    case 0:
                        linearLayout2.addView(view);
                        linearLayout2.addView(dropDownListView, layoutParams);
                        break;
                    case 1:
                        linearLayout2.addView(dropDownListView, layoutParams);
                        linearLayout2.addView(view);
                        break;
                    default:
                        Log.e("ListPopupWindow", "Invalid hint position " + this.mPromptPosition);
                        break;
                }
                if (this.mDropDownWidth >= 0) {
                    i2 = Integer.MIN_VALUE;
                    i3 = this.mDropDownWidth;
                } else {
                    i2 = 0;
                    i3 = 0;
                }
                view.measure(View.MeasureSpec.makeMeasureSpec(i3, i2), 0);
                LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) view.getLayoutParams();
                i4 = view.getMeasuredHeight() + layoutParams2.topMargin + layoutParams2.bottomMargin;
                linearLayout = linearLayout2;
            }
            this.mPopup.setContentView(linearLayout);
        } else {
            ViewGroup viewGroup = (ViewGroup) this.mPopup.getContentView();
            View view2 = this.mPromptView;
            i4 = 0;
            if (view2 != null) {
                LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) view2.getLayoutParams();
                i4 = view2.getMeasuredHeight() + layoutParams3.topMargin + layoutParams3.bottomMargin;
            }
        }
        Drawable background = this.mPopup.getBackground();
        if (background != null) {
            background.getPadding(this.mTempRect);
            int i5 = this.mTempRect.top + this.mTempRect.bottom;
            i = i5;
            if (!this.mDropDownVerticalOffsetSet) {
                this.mDropDownVerticalOffset = -this.mTempRect.top;
                i = i5;
            }
        } else {
            this.mTempRect.setEmpty();
            i = 0;
        }
        int maxAvailableHeight = getMaxAvailableHeight(getAnchorView(), this.mDropDownVerticalOffset, this.mPopup.getInputMethodMode() == 2);
        if (this.mDropDownAlwaysVisible || this.mDropDownHeight == -1) {
            return maxAvailableHeight + i;
        }
        switch (this.mDropDownWidth) {
            case -2:
                makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mContext.getResources().getDisplayMetrics().widthPixels - (this.mTempRect.left + this.mTempRect.right), Integer.MIN_VALUE);
                break;
            case -1:
                makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mContext.getResources().getDisplayMetrics().widthPixels - (this.mTempRect.left + this.mTempRect.right), 1073741824);
                break;
            default:
                makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mDropDownWidth, 1073741824);
                break;
        }
        int measureHeightOfChildrenCompat = this.mDropDownList.measureHeightOfChildrenCompat(makeMeasureSpec, 0, -1, maxAvailableHeight - i4, -1);
        int i6 = i4;
        if (measureHeightOfChildrenCompat > 0) {
            i6 = i4 + i + this.mDropDownList.getPaddingTop() + this.mDropDownList.getPaddingBottom();
        }
        return measureHeightOfChildrenCompat + i6;
    }

    private int getMaxAvailableHeight(View view, int i, boolean z) {
        if (sGetMaxAvailableHeightMethod != null) {
            try {
                return ((Integer) sGetMaxAvailableHeightMethod.invoke(this.mPopup, view, Integer.valueOf(i), Boolean.valueOf(z))).intValue();
            } catch (Exception e) {
                Log.i("ListPopupWindow", "Could not call getMaxAvailableHeightMethod(View, int, boolean) on PopupWindow. Using the public version.");
            }
        }
        return this.mPopup.getMaxAvailableHeight(view, i);
    }

    private void removePromptView() {
        if (this.mPromptView != null) {
            ViewParent parent = this.mPromptView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(this.mPromptView);
            }
        }
    }

    private void setPopupClipToScreenEnabled(boolean z) {
        if (sClipToWindowEnabledMethod != null) {
            try {
                sClipToWindowEnabledMethod.invoke(this.mPopup, Boolean.valueOf(z));
            } catch (Exception e) {
                Log.i("ListPopupWindow", "Could not call setClipToScreenEnabled() on PopupWindow. Oh well.");
            }
        }
    }

    public void clearListSelection() {
        DropDownListView dropDownListView = this.mDropDownList;
        if (dropDownListView != null) {
            dropDownListView.setListSelectionHidden(true);
            dropDownListView.requestLayout();
        }
    }

    @NonNull
    DropDownListView createDropDownListView(Context context, boolean z) {
        return new DropDownListView(context, z);
    }

    @Override // android.support.v7.view.menu.ShowableListMenu
    public void dismiss() {
        this.mPopup.dismiss();
        removePromptView();
        this.mPopup.setContentView(null);
        this.mDropDownList = null;
        this.mHandler.removeCallbacks(this.mResizePopupRunnable);
    }

    @Nullable
    public View getAnchorView() {
        return this.mDropDownAnchorView;
    }

    @Nullable
    public Drawable getBackground() {
        return this.mPopup.getBackground();
    }

    public int getHorizontalOffset() {
        return this.mDropDownHorizontalOffset;
    }

    @Override // android.support.v7.view.menu.ShowableListMenu
    @Nullable
    public ListView getListView() {
        return this.mDropDownList;
    }

    public int getVerticalOffset() {
        if (this.mDropDownVerticalOffsetSet) {
            return this.mDropDownVerticalOffset;
        }
        return 0;
    }

    public int getWidth() {
        return this.mDropDownWidth;
    }

    public boolean isInputMethodNotNeeded() {
        return this.mPopup.getInputMethodMode() == 2;
    }

    public boolean isModal() {
        return this.mModal;
    }

    @Override // android.support.v7.view.menu.ShowableListMenu
    public boolean isShowing() {
        return this.mPopup.isShowing();
    }

    public void setAdapter(@Nullable ListAdapter listAdapter) {
        if (this.mObserver == null) {
            this.mObserver = new PopupDataSetObserver(this, null);
        } else if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(this.mObserver);
        }
        this.mAdapter = listAdapter;
        if (this.mAdapter != null) {
            listAdapter.registerDataSetObserver(this.mObserver);
        }
        if (this.mDropDownList != null) {
            this.mDropDownList.setAdapter(this.mAdapter);
        }
    }

    public void setAnchorView(@Nullable View view) {
        this.mDropDownAnchorView = view;
    }

    public void setAnimationStyle(@StyleRes int i) {
        this.mPopup.setAnimationStyle(i);
    }

    public void setBackgroundDrawable(@Nullable Drawable drawable) {
        this.mPopup.setBackgroundDrawable(drawable);
    }

    public void setContentWidth(int i) {
        Drawable background = this.mPopup.getBackground();
        if (background == null) {
            setWidth(i);
            return;
        }
        background.getPadding(this.mTempRect);
        this.mDropDownWidth = this.mTempRect.left + this.mTempRect.right + i;
    }

    public void setDropDownGravity(int i) {
        this.mDropDownGravity = i;
    }

    public void setEpicenterBounds(Rect rect) {
        this.mEpicenterBounds = rect;
    }

    public void setHorizontalOffset(int i) {
        this.mDropDownHorizontalOffset = i;
    }

    public void setInputMethodMode(int i) {
        this.mPopup.setInputMethodMode(i);
    }

    public void setModal(boolean z) {
        this.mModal = z;
        this.mPopup.setFocusable(z);
    }

    public void setOnDismissListener(@Nullable PopupWindow.OnDismissListener onDismissListener) {
        this.mPopup.setOnDismissListener(onDismissListener);
    }

    public void setOnItemClickListener(@Nullable AdapterView.OnItemClickListener onItemClickListener) {
        this.mItemClickListener = onItemClickListener;
    }

    public void setPromptPosition(int i) {
        this.mPromptPosition = i;
    }

    public void setSelection(int i) {
        DropDownListView dropDownListView = this.mDropDownList;
        if (!isShowing() || dropDownListView == null) {
            return;
        }
        dropDownListView.setListSelectionHidden(false);
        dropDownListView.setSelection(i);
        if (Build.VERSION.SDK_INT < 11 || dropDownListView.getChoiceMode() == 0) {
            return;
        }
        dropDownListView.setItemChecked(i, true);
    }

    public void setVerticalOffset(int i) {
        this.mDropDownVerticalOffset = i;
        this.mDropDownVerticalOffsetSet = true;
    }

    public void setWidth(int i) {
        this.mDropDownWidth = i;
    }

    @Override // android.support.v7.view.menu.ShowableListMenu
    public void show() {
        int buildDropDown = buildDropDown();
        boolean isInputMethodNotNeeded = isInputMethodNotNeeded();
        PopupWindowCompat.setWindowLayoutType(this.mPopup, this.mDropDownWindowLayoutType);
        if (this.mPopup.isShowing()) {
            int width = this.mDropDownWidth == -1 ? -1 : this.mDropDownWidth == -2 ? getAnchorView().getWidth() : this.mDropDownWidth;
            if (this.mDropDownHeight == -1) {
                if (!isInputMethodNotNeeded) {
                    buildDropDown = -1;
                }
                if (isInputMethodNotNeeded) {
                    this.mPopup.setWidth(this.mDropDownWidth == -1 ? -1 : 0);
                    this.mPopup.setHeight(0);
                } else {
                    this.mPopup.setWidth(this.mDropDownWidth == -1 ? -1 : 0);
                    this.mPopup.setHeight(-1);
                }
            } else if (this.mDropDownHeight != -2) {
                buildDropDown = this.mDropDownHeight;
            }
            PopupWindow popupWindow = this.mPopup;
            boolean z = false;
            if (!this.mForceIgnoreOutsideTouch) {
                z = !this.mDropDownAlwaysVisible;
            }
            popupWindow.setOutsideTouchable(z);
            PopupWindow popupWindow2 = this.mPopup;
            View anchorView = getAnchorView();
            int i = this.mDropDownHorizontalOffset;
            int i2 = this.mDropDownVerticalOffset;
            if (width < 0) {
                width = -1;
            }
            if (buildDropDown < 0) {
                buildDropDown = -1;
            }
            popupWindow2.update(anchorView, i, i2, width, buildDropDown);
            return;
        }
        int width2 = this.mDropDownWidth == -1 ? -1 : this.mDropDownWidth == -2 ? getAnchorView().getWidth() : this.mDropDownWidth;
        if (this.mDropDownHeight == -1) {
            buildDropDown = -1;
        } else if (this.mDropDownHeight != -2) {
            buildDropDown = this.mDropDownHeight;
        }
        this.mPopup.setWidth(width2);
        this.mPopup.setHeight(buildDropDown);
        setPopupClipToScreenEnabled(true);
        PopupWindow popupWindow3 = this.mPopup;
        boolean z2 = false;
        if (!this.mForceIgnoreOutsideTouch) {
            z2 = !this.mDropDownAlwaysVisible;
        }
        popupWindow3.setOutsideTouchable(z2);
        this.mPopup.setTouchInterceptor(this.mTouchInterceptor);
        if (sSetEpicenterBoundsMethod != null) {
            try {
                sSetEpicenterBoundsMethod.invoke(this.mPopup, this.mEpicenterBounds);
            } catch (Exception e) {
                Log.e("ListPopupWindow", "Could not invoke setEpicenterBounds on PopupWindow", e);
            }
        }
        PopupWindowCompat.showAsDropDown(this.mPopup, getAnchorView(), this.mDropDownHorizontalOffset, this.mDropDownVerticalOffset, this.mDropDownGravity);
        this.mDropDownList.setSelection(-1);
        if (!this.mModal || this.mDropDownList.isInTouchMode()) {
            clearListSelection();
        }
        if (this.mModal) {
            return;
        }
        this.mHandler.post(this.mHideSelector);
    }
}
