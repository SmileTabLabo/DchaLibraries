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
/* loaded from: classes.dex */
public class ListPopupWindow implements ShowableListMenu {
    private static Method sClipToWindowEnabledMethod;
    private static Method sGetMaxAvailableHeightMethod;
    private static Method sSetEpicenterBoundsMethod;
    private ListAdapter mAdapter;
    private Context mContext;
    private View mDropDownAnchorView;
    private int mDropDownHorizontalOffset;
    private DropDownListView mDropDownList;
    private Drawable mDropDownListHighlight;
    private int mDropDownVerticalOffset;
    private boolean mDropDownVerticalOffsetSet;
    private Rect mEpicenterBounds;
    private final Handler mHandler;
    private AdapterView.OnItemClickListener mItemClickListener;
    private AdapterView.OnItemSelectedListener mItemSelectedListener;
    private boolean mModal;
    private DataSetObserver mObserver;
    PopupWindow mPopup;
    private View mPromptView;
    private Runnable mShowDropDownRunnable;
    private int mDropDownHeight = -2;
    private int mDropDownWidth = -2;
    private int mDropDownWindowLayoutType = 1002;
    private boolean mIsAnimatedFromAnchor = true;
    private int mDropDownGravity = 0;
    private boolean mDropDownAlwaysVisible = false;
    private boolean mForceIgnoreOutsideTouch = false;
    int mListItemExpandMaximum = Integer.MAX_VALUE;
    private int mPromptPosition = 0;
    private final ResizePopupRunnable mResizePopupRunnable = new ResizePopupRunnable(this, null);
    private final PopupTouchInterceptor mTouchInterceptor = new PopupTouchInterceptor(this, null);
    private final PopupScrollListener mScrollListener = new PopupScrollListener(this, null);
    private final ListSelectorHider mHideSelector = new ListSelectorHider(this, null);
    private final Rect mTempRect = new Rect();

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

    public ListPopupWindow(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        this.mContext = context;
        this.mHandler = new Handler(context.getMainLooper());
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.ListPopupWindow, defStyleAttr, defStyleRes);
        this.mDropDownHorizontalOffset = a.getDimensionPixelOffset(R$styleable.ListPopupWindow_android_dropDownHorizontalOffset, 0);
        this.mDropDownVerticalOffset = a.getDimensionPixelOffset(R$styleable.ListPopupWindow_android_dropDownVerticalOffset, 0);
        if (this.mDropDownVerticalOffset != 0) {
            this.mDropDownVerticalOffsetSet = true;
        }
        a.recycle();
        if (Build.VERSION.SDK_INT >= 11) {
            this.mPopup = new AppCompatPopupWindow(context, attrs, defStyleAttr, defStyleRes);
        } else {
            this.mPopup = new AppCompatPopupWindow(context, attrs, defStyleAttr);
        }
        this.mPopup.setInputMethodMode(1);
    }

    public void setAdapter(@Nullable ListAdapter adapter) {
        if (this.mObserver == null) {
            this.mObserver = new PopupDataSetObserver(this, null);
        } else if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(this.mObserver);
        }
        this.mAdapter = adapter;
        if (this.mAdapter != null) {
            adapter.registerDataSetObserver(this.mObserver);
        }
        if (this.mDropDownList == null) {
            return;
        }
        this.mDropDownList.setAdapter(this.mAdapter);
    }

    public void setModal(boolean modal) {
        this.mModal = modal;
        this.mPopup.setFocusable(modal);
    }

    public boolean isModal() {
        return this.mModal;
    }

    public void setAnimationStyle(@StyleRes int animationStyle) {
        this.mPopup.setAnimationStyle(animationStyle);
    }

    @Nullable
    public View getAnchorView() {
        return this.mDropDownAnchorView;
    }

    public void setAnchorView(@Nullable View anchor) {
        this.mDropDownAnchorView = anchor;
    }

    public int getHorizontalOffset() {
        return this.mDropDownHorizontalOffset;
    }

    public void setHorizontalOffset(int offset) {
        this.mDropDownHorizontalOffset = offset;
    }

    public int getVerticalOffset() {
        if (!this.mDropDownVerticalOffsetSet) {
            return 0;
        }
        return this.mDropDownVerticalOffset;
    }

    public void setVerticalOffset(int offset) {
        this.mDropDownVerticalOffset = offset;
        this.mDropDownVerticalOffsetSet = true;
    }

    public void setEpicenterBounds(Rect bounds) {
        this.mEpicenterBounds = bounds;
    }

    public void setDropDownGravity(int gravity) {
        this.mDropDownGravity = gravity;
    }

    public void setWidth(int width) {
        this.mDropDownWidth = width;
    }

    public void setContentWidth(int width) {
        Drawable popupBackground = this.mPopup.getBackground();
        if (popupBackground != null) {
            popupBackground.getPadding(this.mTempRect);
            this.mDropDownWidth = this.mTempRect.left + this.mTempRect.right + width;
            return;
        }
        setWidth(width);
    }

    public void setOnItemClickListener(@Nullable AdapterView.OnItemClickListener clickListener) {
        this.mItemClickListener = clickListener;
    }

    @Override // android.support.v7.view.menu.ShowableListMenu
    public void show() {
        int widthSpec;
        int heightSpec;
        int widthSpec2;
        int heightSpec2;
        boolean z = false;
        int height = buildDropDown();
        boolean noInputMethod = isInputMethodNotNeeded();
        PopupWindowCompat.setWindowLayoutType(this.mPopup, this.mDropDownWindowLayoutType);
        if (this.mPopup.isShowing()) {
            if (this.mDropDownWidth == -1) {
                widthSpec2 = -1;
            } else if (this.mDropDownWidth == -2) {
                widthSpec2 = getAnchorView().getWidth();
            } else {
                widthSpec2 = this.mDropDownWidth;
            }
            if (this.mDropDownHeight == -1) {
                heightSpec2 = noInputMethod ? height : -1;
                if (noInputMethod) {
                    this.mPopup.setWidth(this.mDropDownWidth == -1 ? -1 : 0);
                    this.mPopup.setHeight(0);
                } else {
                    this.mPopup.setWidth(this.mDropDownWidth == -1 ? -1 : 0);
                    this.mPopup.setHeight(-1);
                }
            } else {
                heightSpec2 = this.mDropDownHeight == -2 ? height : this.mDropDownHeight;
            }
            PopupWindow popupWindow = this.mPopup;
            if (!this.mForceIgnoreOutsideTouch && !this.mDropDownAlwaysVisible) {
                z = true;
            }
            popupWindow.setOutsideTouchable(z);
            this.mPopup.update(getAnchorView(), this.mDropDownHorizontalOffset, this.mDropDownVerticalOffset, widthSpec2 < 0 ? -1 : widthSpec2, heightSpec2 >= 0 ? heightSpec2 : -1);
            return;
        }
        if (this.mDropDownWidth == -1) {
            widthSpec = -1;
        } else if (this.mDropDownWidth == -2) {
            widthSpec = getAnchorView().getWidth();
        } else {
            widthSpec = this.mDropDownWidth;
        }
        if (this.mDropDownHeight == -1) {
            heightSpec = -1;
        } else if (this.mDropDownHeight == -2) {
            heightSpec = height;
        } else {
            heightSpec = this.mDropDownHeight;
        }
        this.mPopup.setWidth(widthSpec);
        this.mPopup.setHeight(heightSpec);
        setPopupClipToScreenEnabled(true);
        PopupWindow popupWindow2 = this.mPopup;
        if (!this.mForceIgnoreOutsideTouch && !this.mDropDownAlwaysVisible) {
            z = true;
        }
        popupWindow2.setOutsideTouchable(z);
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

    @Override // android.support.v7.view.menu.ShowableListMenu
    public void dismiss() {
        this.mPopup.dismiss();
        removePromptView();
        this.mPopup.setContentView(null);
        this.mDropDownList = null;
        this.mHandler.removeCallbacks(this.mResizePopupRunnable);
    }

    public void setOnDismissListener(@Nullable PopupWindow.OnDismissListener listener) {
        this.mPopup.setOnDismissListener(listener);
    }

    private void removePromptView() {
        if (this.mPromptView == null) {
            return;
        }
        ViewParent parent = this.mPromptView.getParent();
        if (!(parent instanceof ViewGroup)) {
            return;
        }
        ViewGroup group = (ViewGroup) parent;
        group.removeView(this.mPromptView);
    }

    public void setInputMethodMode(int mode) {
        this.mPopup.setInputMethodMode(mode);
    }

    public void clearListSelection() {
        DropDownListView list = this.mDropDownList;
        if (list == null) {
            return;
        }
        list.setListSelectionHidden(true);
        list.requestLayout();
    }

    @Override // android.support.v7.view.menu.ShowableListMenu
    public boolean isShowing() {
        return this.mPopup.isShowing();
    }

    public boolean isInputMethodNotNeeded() {
        return this.mPopup.getInputMethodMode() == 2;
    }

    @Override // android.support.v7.view.menu.ShowableListMenu
    @Nullable
    public ListView getListView() {
        return this.mDropDownList;
    }

    @NonNull
    DropDownListView createDropDownListView(Context context, boolean hijackFocus) {
        return new DropDownListView(context, hijackFocus);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private int buildDropDown() {
        int padding;
        int childWidthSpec;
        int widthMode;
        int widthSize;
        int otherHeights = 0;
        if (this.mDropDownList == null) {
            Context context = this.mContext;
            this.mShowDropDownRunnable = new Runnable() { // from class: android.support.v7.widget.ListPopupWindow.2
                @Override // java.lang.Runnable
                public void run() {
                    View view = ListPopupWindow.this.getAnchorView();
                    if (view == null || view.getWindowToken() == null) {
                        return;
                    }
                    ListPopupWindow.this.show();
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
            this.mDropDownList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // from class: android.support.v7.widget.ListPopupWindow.3
                @Override // android.widget.AdapterView.OnItemSelectedListener
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    DropDownListView dropDownList;
                    if (position == -1 || (dropDownList = ListPopupWindow.this.mDropDownList) == null) {
                        return;
                    }
                    dropDownList.setListSelectionHidden(false);
                }

                @Override // android.widget.AdapterView.OnItemSelectedListener
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            this.mDropDownList.setOnScrollListener(this.mScrollListener);
            if (this.mItemSelectedListener != null) {
                this.mDropDownList.setOnItemSelectedListener(this.mItemSelectedListener);
            }
            ViewGroup dropDownView = this.mDropDownList;
            View hintView = this.mPromptView;
            if (hintView != null) {
                LinearLayout hintContainer = new LinearLayout(context);
                hintContainer.setOrientation(1);
                LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(-1, 0, 1.0f);
                switch (this.mPromptPosition) {
                    case 0:
                        hintContainer.addView(hintView);
                        hintContainer.addView(dropDownView, hintParams);
                        break;
                    case 1:
                        hintContainer.addView(dropDownView, hintParams);
                        hintContainer.addView(hintView);
                        break;
                    default:
                        Log.e("ListPopupWindow", "Invalid hint position " + this.mPromptPosition);
                        break;
                }
                if (this.mDropDownWidth >= 0) {
                    widthMode = Integer.MIN_VALUE;
                    widthSize = this.mDropDownWidth;
                } else {
                    widthMode = 0;
                    widthSize = 0;
                }
                int widthSpec = View.MeasureSpec.makeMeasureSpec(widthSize, widthMode);
                hintView.measure(widthSpec, 0);
                LinearLayout.LayoutParams hintParams2 = (LinearLayout.LayoutParams) hintView.getLayoutParams();
                otherHeights = hintView.getMeasuredHeight() + hintParams2.topMargin + hintParams2.bottomMargin;
                dropDownView = hintContainer;
            }
            this.mPopup.setContentView(dropDownView);
        } else {
            ViewGroup viewGroup = (ViewGroup) this.mPopup.getContentView();
            View view = this.mPromptView;
            if (view != null) {
                LinearLayout.LayoutParams hintParams3 = (LinearLayout.LayoutParams) view.getLayoutParams();
                otherHeights = view.getMeasuredHeight() + hintParams3.topMargin + hintParams3.bottomMargin;
            }
        }
        Drawable background = this.mPopup.getBackground();
        if (background != null) {
            background.getPadding(this.mTempRect);
            padding = this.mTempRect.top + this.mTempRect.bottom;
            if (!this.mDropDownVerticalOffsetSet) {
                this.mDropDownVerticalOffset = -this.mTempRect.top;
            }
        } else {
            this.mTempRect.setEmpty();
            padding = 0;
        }
        boolean ignoreBottomDecorations = this.mPopup.getInputMethodMode() == 2;
        int maxHeight = getMaxAvailableHeight(getAnchorView(), this.mDropDownVerticalOffset, ignoreBottomDecorations);
        if (this.mDropDownAlwaysVisible || this.mDropDownHeight == -1) {
            return maxHeight + padding;
        }
        switch (this.mDropDownWidth) {
            case -2:
                childWidthSpec = View.MeasureSpec.makeMeasureSpec(this.mContext.getResources().getDisplayMetrics().widthPixels - (this.mTempRect.left + this.mTempRect.right), Integer.MIN_VALUE);
                break;
            case -1:
                childWidthSpec = View.MeasureSpec.makeMeasureSpec(this.mContext.getResources().getDisplayMetrics().widthPixels - (this.mTempRect.left + this.mTempRect.right), 1073741824);
                break;
            default:
                childWidthSpec = View.MeasureSpec.makeMeasureSpec(this.mDropDownWidth, 1073741824);
                break;
        }
        int listContent = this.mDropDownList.measureHeightOfChildrenCompat(childWidthSpec, 0, -1, maxHeight - otherHeights, -1);
        if (listContent > 0) {
            int listPadding = this.mDropDownList.getPaddingTop() + this.mDropDownList.getPaddingBottom();
            otherHeights += padding + listPadding;
        }
        return listContent + otherHeights;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class PopupDataSetObserver extends DataSetObserver {
        /* synthetic */ PopupDataSetObserver(ListPopupWindow this$0, PopupDataSetObserver popupDataSetObserver) {
            this();
        }

        private PopupDataSetObserver() {
        }

        @Override // android.database.DataSetObserver
        public void onChanged() {
            if (!ListPopupWindow.this.isShowing()) {
                return;
            }
            ListPopupWindow.this.show();
        }

        @Override // android.database.DataSetObserver
        public void onInvalidated() {
            ListPopupWindow.this.dismiss();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ListSelectorHider implements Runnable {
        /* synthetic */ ListSelectorHider(ListPopupWindow this$0, ListSelectorHider listSelectorHider) {
            this();
        }

        private ListSelectorHider() {
        }

        @Override // java.lang.Runnable
        public void run() {
            ListPopupWindow.this.clearListSelection();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ResizePopupRunnable implements Runnable {
        /* synthetic */ ResizePopupRunnable(ListPopupWindow this$0, ResizePopupRunnable resizePopupRunnable) {
            this();
        }

        private ResizePopupRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            if (ListPopupWindow.this.mDropDownList == null || !ViewCompat.isAttachedToWindow(ListPopupWindow.this.mDropDownList) || ListPopupWindow.this.mDropDownList.getCount() <= ListPopupWindow.this.mDropDownList.getChildCount() || ListPopupWindow.this.mDropDownList.getChildCount() > ListPopupWindow.this.mListItemExpandMaximum) {
                return;
            }
            ListPopupWindow.this.mPopup.setInputMethodMode(2);
            ListPopupWindow.this.show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class PopupTouchInterceptor implements View.OnTouchListener {
        /* synthetic */ PopupTouchInterceptor(ListPopupWindow this$0, PopupTouchInterceptor popupTouchInterceptor) {
            this();
        }

        private PopupTouchInterceptor() {
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (action == 0 && ListPopupWindow.this.mPopup != null && ListPopupWindow.this.mPopup.isShowing() && x >= 0 && x < ListPopupWindow.this.mPopup.getWidth() && y >= 0 && y < ListPopupWindow.this.mPopup.getHeight()) {
                ListPopupWindow.this.mHandler.postDelayed(ListPopupWindow.this.mResizePopupRunnable, 250L);
            } else if (action == 1) {
                ListPopupWindow.this.mHandler.removeCallbacks(ListPopupWindow.this.mResizePopupRunnable);
            }
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class PopupScrollListener implements AbsListView.OnScrollListener {
        /* synthetic */ PopupScrollListener(ListPopupWindow this$0, PopupScrollListener popupScrollListener) {
            this();
        }

        private PopupScrollListener() {
        }

        @Override // android.widget.AbsListView.OnScrollListener
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }

        @Override // android.widget.AbsListView.OnScrollListener
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState != 1 || ListPopupWindow.this.isInputMethodNotNeeded() || ListPopupWindow.this.mPopup.getContentView() == null) {
                return;
            }
            ListPopupWindow.this.mHandler.removeCallbacks(ListPopupWindow.this.mResizePopupRunnable);
            ListPopupWindow.this.mResizePopupRunnable.run();
        }
    }

    private void setPopupClipToScreenEnabled(boolean clip) {
        if (sClipToWindowEnabledMethod == null) {
            return;
        }
        try {
            sClipToWindowEnabledMethod.invoke(this.mPopup, Boolean.valueOf(clip));
        } catch (Exception e) {
            Log.i("ListPopupWindow", "Could not call setClipToScreenEnabled() on PopupWindow. Oh well.");
        }
    }

    private int getMaxAvailableHeight(View anchor, int yOffset, boolean ignoreBottomDecorations) {
        if (sGetMaxAvailableHeightMethod != null) {
            try {
                return ((Integer) sGetMaxAvailableHeightMethod.invoke(this.mPopup, anchor, Integer.valueOf(yOffset), Boolean.valueOf(ignoreBottomDecorations))).intValue();
            } catch (Exception e) {
                Log.i("ListPopupWindow", "Could not call getMaxAvailableHeightMethod(View, int, boolean) on PopupWindow. Using the public version.");
            }
        }
        return this.mPopup.getMaxAvailableHeight(anchor, yOffset);
    }
}
