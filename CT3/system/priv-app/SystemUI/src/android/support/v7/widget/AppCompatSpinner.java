package android.support.v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.TintableBackgroundView;
import android.support.v4.view.ViewCompat;
import android.support.v7.appcompat.R$attr;
import android.support.v7.appcompat.R$layout;
import android.support.v7.appcompat.R$styleable;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.view.menu.ShowableListMenu;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
/* loaded from: a.zip:android/support/v7/widget/AppCompatSpinner.class */
public class AppCompatSpinner extends Spinner implements TintableBackgroundView {
    private static final int[] ATTRS_ANDROID_SPINNERMODE;
    private static final boolean IS_AT_LEAST_JB;
    private static final boolean IS_AT_LEAST_M;
    private AppCompatBackgroundHelper mBackgroundTintHelper;
    private AppCompatDrawableManager mDrawableManager;
    private int mDropDownWidth;
    private ForwardingListener mForwardingListener;
    private DropdownPopup mPopup;
    private Context mPopupContext;
    private boolean mPopupSet;
    private SpinnerAdapter mTempAdapter;
    private final Rect mTempRect;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/AppCompatSpinner$DropDownAdapter.class */
    public static class DropDownAdapter implements ListAdapter, SpinnerAdapter {
        private SpinnerAdapter mAdapter;
        private ListAdapter mListAdapter;

        public DropDownAdapter(@Nullable SpinnerAdapter spinnerAdapter, @Nullable Resources.Theme theme) {
            this.mAdapter = spinnerAdapter;
            if (spinnerAdapter instanceof ListAdapter) {
                this.mListAdapter = (ListAdapter) spinnerAdapter;
            }
            if (theme != null) {
                if (AppCompatSpinner.IS_AT_LEAST_M && (spinnerAdapter instanceof android.widget.ThemedSpinnerAdapter)) {
                    android.widget.ThemedSpinnerAdapter themedSpinnerAdapter = (android.widget.ThemedSpinnerAdapter) spinnerAdapter;
                    if (themedSpinnerAdapter.getDropDownViewTheme() != theme) {
                        themedSpinnerAdapter.setDropDownViewTheme(theme);
                    }
                } else if (spinnerAdapter instanceof ThemedSpinnerAdapter) {
                    ThemedSpinnerAdapter themedSpinnerAdapter2 = (ThemedSpinnerAdapter) spinnerAdapter;
                    if (themedSpinnerAdapter2.getDropDownViewTheme() == null) {
                        themedSpinnerAdapter2.setDropDownViewTheme(theme);
                    }
                }
            }
        }

        @Override // android.widget.ListAdapter
        public boolean areAllItemsEnabled() {
            ListAdapter listAdapter = this.mListAdapter;
            if (listAdapter != null) {
                return listAdapter.areAllItemsEnabled();
            }
            return true;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return this.mAdapter == null ? 0 : this.mAdapter.getCount();
        }

        @Override // android.widget.SpinnerAdapter
        public View getDropDownView(int i, View view, ViewGroup viewGroup) {
            return this.mAdapter == null ? null : this.mAdapter.getDropDownView(i, view, viewGroup);
        }

        @Override // android.widget.Adapter
        public Object getItem(int i) {
            Object obj = null;
            if (this.mAdapter != null) {
                obj = this.mAdapter.getItem(i);
            }
            return obj;
        }

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return this.mAdapter == null ? -1L : this.mAdapter.getItemId(i);
        }

        @Override // android.widget.Adapter
        public int getItemViewType(int i) {
            return 0;
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            return getDropDownView(i, view, viewGroup);
        }

        @Override // android.widget.Adapter
        public int getViewTypeCount() {
            return 1;
        }

        @Override // android.widget.Adapter
        public boolean hasStableIds() {
            return this.mAdapter != null ? this.mAdapter.hasStableIds() : false;
        }

        @Override // android.widget.Adapter
        public boolean isEmpty() {
            boolean z = false;
            if (getCount() == 0) {
                z = true;
            }
            return z;
        }

        @Override // android.widget.ListAdapter
        public boolean isEnabled(int i) {
            ListAdapter listAdapter = this.mListAdapter;
            if (listAdapter != null) {
                return listAdapter.isEnabled(i);
            }
            return true;
        }

        @Override // android.widget.Adapter
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {
            if (this.mAdapter != null) {
                this.mAdapter.registerDataSetObserver(dataSetObserver);
            }
        }

        @Override // android.widget.Adapter
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
            if (this.mAdapter != null) {
                this.mAdapter.unregisterDataSetObserver(dataSetObserver);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/AppCompatSpinner$DropdownPopup.class */
    public class DropdownPopup extends ListPopupWindow {
        private ListAdapter mAdapter;
        private CharSequence mHintText;
        private final Rect mVisibleRect;
        final AppCompatSpinner this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public DropdownPopup(AppCompatSpinner appCompatSpinner, Context context, AttributeSet attributeSet, int i) {
            super(context, attributeSet, i);
            this.this$0 = appCompatSpinner;
            this.mVisibleRect = new Rect();
            setAnchorView(appCompatSpinner);
            setModal(true);
            setPromptPosition(0);
            setOnItemClickListener(new AdapterView.OnItemClickListener(this) { // from class: android.support.v7.widget.AppCompatSpinner.DropdownPopup.1
                final DropdownPopup this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.widget.AdapterView.OnItemClickListener
                public void onItemClick(AdapterView<?> adapterView, View view, int i2, long j) {
                    this.this$1.this$0.setSelection(i2);
                    if (this.this$1.this$0.getOnItemClickListener() != null) {
                        this.this$1.this$0.performItemClick(view, i2, this.this$1.mAdapter.getItemId(i2));
                    }
                    this.this$1.dismiss();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean isVisibleToUser(View view) {
            return ViewCompat.isAttachedToWindow(view) ? view.getGlobalVisibleRect(this.mVisibleRect) : false;
        }

        void computeContentWidth() {
            Drawable background = getBackground();
            int i = 0;
            if (background != null) {
                background.getPadding(this.this$0.mTempRect);
                i = ViewUtils.isLayoutRtl(this.this$0) ? this.this$0.mTempRect.right : -this.this$0.mTempRect.left;
            } else {
                Rect rect = this.this$0.mTempRect;
                this.this$0.mTempRect.right = 0;
                rect.left = 0;
            }
            int paddingLeft = this.this$0.getPaddingLeft();
            int paddingRight = this.this$0.getPaddingRight();
            int width = this.this$0.getWidth();
            if (this.this$0.mDropDownWidth == -2) {
                int compatMeasureContentWidth = this.this$0.compatMeasureContentWidth((SpinnerAdapter) this.mAdapter, getBackground());
                int i2 = (this.this$0.getContext().getResources().getDisplayMetrics().widthPixels - this.this$0.mTempRect.left) - this.this$0.mTempRect.right;
                int i3 = compatMeasureContentWidth;
                if (compatMeasureContentWidth > i2) {
                    i3 = i2;
                }
                setContentWidth(Math.max(i3, (width - paddingLeft) - paddingRight));
            } else if (this.this$0.mDropDownWidth == -1) {
                setContentWidth((width - paddingLeft) - paddingRight);
            } else {
                setContentWidth(this.this$0.mDropDownWidth);
            }
            setHorizontalOffset(ViewUtils.isLayoutRtl(this.this$0) ? i + ((width - paddingRight) - getWidth()) : i + paddingLeft);
        }

        public CharSequence getHintText() {
            return this.mHintText;
        }

        @Override // android.support.v7.widget.ListPopupWindow
        public void setAdapter(ListAdapter listAdapter) {
            super.setAdapter(listAdapter);
            this.mAdapter = listAdapter;
        }

        public void setPromptText(CharSequence charSequence) {
            this.mHintText = charSequence;
        }

        @Override // android.support.v7.widget.ListPopupWindow, android.support.v7.view.menu.ShowableListMenu
        public void show() {
            ViewTreeObserver viewTreeObserver;
            boolean isShowing = isShowing();
            computeContentWidth();
            setInputMethodMode(2);
            super.show();
            getListView().setChoiceMode(1);
            setSelection(this.this$0.getSelectedItemPosition());
            if (isShowing || (viewTreeObserver = this.this$0.getViewTreeObserver()) == null) {
                return;
            }
            ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener(this) { // from class: android.support.v7.widget.AppCompatSpinner.DropdownPopup.2
                final DropdownPopup this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                public void onGlobalLayout() {
                    if (!this.this$1.isVisibleToUser(this.this$1.this$0)) {
                        this.this$1.dismiss();
                        return;
                    }
                    this.this$1.computeContentWidth();
                    DropdownPopup.super.show();
                }
            };
            viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener);
            setOnDismissListener(new PopupWindow.OnDismissListener(this, onGlobalLayoutListener) { // from class: android.support.v7.widget.AppCompatSpinner.DropdownPopup.3
                final DropdownPopup this$1;
                final ViewTreeObserver.OnGlobalLayoutListener val$layoutListener;

                {
                    this.this$1 = this;
                    this.val$layoutListener = onGlobalLayoutListener;
                }

                @Override // android.widget.PopupWindow.OnDismissListener
                public void onDismiss() {
                    ViewTreeObserver viewTreeObserver2 = this.this$1.this$0.getViewTreeObserver();
                    if (viewTreeObserver2 != null) {
                        viewTreeObserver2.removeGlobalOnLayoutListener(this.val$layoutListener);
                    }
                }
            });
        }
    }

    static {
        IS_AT_LEAST_M = Build.VERSION.SDK_INT >= 23;
        IS_AT_LEAST_JB = Build.VERSION.SDK_INT >= 16;
        ATTRS_ANDROID_SPINNERMODE = new int[]{16843505};
    }

    public AppCompatSpinner(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.spinnerStyle);
    }

    public AppCompatSpinner(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, -1);
    }

    public AppCompatSpinner(Context context, AttributeSet attributeSet, int i, int i2) {
        this(context, attributeSet, i, i2, null);
    }

    /* JADX WARN: Finally extract failed */
    public AppCompatSpinner(Context context, AttributeSet attributeSet, int i, int i2, Resources.Theme theme) {
        super(context, attributeSet, i);
        this.mTempRect = new Rect();
        TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(context, attributeSet, R$styleable.Spinner, i, 0);
        this.mDrawableManager = AppCompatDrawableManager.get();
        this.mBackgroundTintHelper = new AppCompatBackgroundHelper(this, this.mDrawableManager);
        if (theme != null) {
            this.mPopupContext = new ContextThemeWrapper(context, theme);
        } else {
            int resourceId = obtainStyledAttributes.getResourceId(R$styleable.Spinner_popupTheme, 0);
            if (resourceId != 0) {
                this.mPopupContext = new ContextThemeWrapper(context, resourceId);
            } else {
                this.mPopupContext = !IS_AT_LEAST_M ? context : null;
            }
        }
        if (this.mPopupContext != null) {
            int i3 = i2;
            if (i2 == -1) {
                if (Build.VERSION.SDK_INT >= 11) {
                    TypedArray typedArray = null;
                    TypedArray typedArray2 = null;
                    try {
                        try {
                            TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(attributeSet, ATTRS_ANDROID_SPINNERMODE, i, 0);
                            int i4 = i2;
                            if (obtainStyledAttributes2.hasValue(0)) {
                                typedArray2 = obtainStyledAttributes2;
                                typedArray = obtainStyledAttributes2;
                                i4 = obtainStyledAttributes2.getInt(0, 0);
                            }
                            i3 = i4;
                            if (obtainStyledAttributes2 != null) {
                                obtainStyledAttributes2.recycle();
                                i3 = i4;
                            }
                        } catch (Exception e) {
                            Log.i("AppCompatSpinner", "Could not read android:spinnerMode", e);
                            i3 = i2;
                            if (typedArray2 != null) {
                                typedArray2.recycle();
                                i3 = i2;
                            }
                        }
                    } catch (Throwable th) {
                        if (typedArray != null) {
                            typedArray.recycle();
                        }
                        throw th;
                    }
                } else {
                    i3 = 1;
                }
            }
            if (i3 == 1) {
                DropdownPopup dropdownPopup = new DropdownPopup(this, this.mPopupContext, attributeSet, i);
                TintTypedArray obtainStyledAttributes3 = TintTypedArray.obtainStyledAttributes(this.mPopupContext, attributeSet, R$styleable.Spinner, i, 0);
                this.mDropDownWidth = obtainStyledAttributes3.getLayoutDimension(R$styleable.Spinner_android_dropDownWidth, -2);
                dropdownPopup.setBackgroundDrawable(obtainStyledAttributes3.getDrawable(R$styleable.Spinner_android_popupBackground));
                dropdownPopup.setPromptText(obtainStyledAttributes.getString(R$styleable.Spinner_android_prompt));
                obtainStyledAttributes3.recycle();
                this.mPopup = dropdownPopup;
                this.mForwardingListener = new ForwardingListener(this, this, dropdownPopup) { // from class: android.support.v7.widget.AppCompatSpinner.1
                    final AppCompatSpinner this$0;
                    final DropdownPopup val$popup;

                    {
                        this.this$0 = this;
                        this.val$popup = dropdownPopup;
                    }

                    @Override // android.support.v7.widget.ForwardingListener
                    public ShowableListMenu getPopup() {
                        return this.val$popup;
                    }

                    @Override // android.support.v7.widget.ForwardingListener
                    public boolean onForwardingStarted() {
                        if (this.this$0.mPopup.isShowing()) {
                            return true;
                        }
                        this.this$0.mPopup.show();
                        return true;
                    }
                };
            }
        }
        CharSequence[] textArray = obtainStyledAttributes.getTextArray(R$styleable.Spinner_android_entries);
        if (textArray != null) {
            ArrayAdapter arrayAdapter = new ArrayAdapter(context, 17367048, textArray);
            arrayAdapter.setDropDownViewResource(R$layout.support_simple_spinner_dropdown_item);
            setAdapter((SpinnerAdapter) arrayAdapter);
        }
        obtainStyledAttributes.recycle();
        this.mPopupSet = true;
        if (this.mTempAdapter != null) {
            setAdapter(this.mTempAdapter);
            this.mTempAdapter = null;
        }
        this.mBackgroundTintHelper.loadFromAttributes(attributeSet, i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int compatMeasureContentWidth(SpinnerAdapter spinnerAdapter, Drawable drawable) {
        if (spinnerAdapter == null) {
            return 0;
        }
        int i = 0;
        View view = null;
        int i2 = 0;
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 0);
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 0);
        int max = Math.max(0, getSelectedItemPosition());
        int min = Math.min(spinnerAdapter.getCount(), max + 15);
        int max2 = Math.max(0, max - (15 - (min - max)));
        while (max2 < min) {
            int itemViewType = spinnerAdapter.getItemViewType(max2);
            int i3 = i2;
            if (itemViewType != i2) {
                i3 = itemViewType;
                view = null;
            }
            view = spinnerAdapter.getView(max2, view, this);
            if (view.getLayoutParams() == null) {
                view.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
            }
            view.measure(makeMeasureSpec, makeMeasureSpec2);
            i = Math.max(i, view.getMeasuredWidth());
            max2++;
            i2 = i3;
        }
        int i4 = i;
        if (drawable != null) {
            drawable.getPadding(this.mTempRect);
            i4 = i + this.mTempRect.left + this.mTempRect.right;
        }
        return i4;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mBackgroundTintHelper != null) {
            this.mBackgroundTintHelper.applySupportBackgroundTint();
        }
    }

    @Override // android.widget.Spinner
    public int getDropDownHorizontalOffset() {
        if (this.mPopup != null) {
            return this.mPopup.getHorizontalOffset();
        }
        if (IS_AT_LEAST_JB) {
            return super.getDropDownHorizontalOffset();
        }
        return 0;
    }

    @Override // android.widget.Spinner
    public int getDropDownVerticalOffset() {
        if (this.mPopup != null) {
            return this.mPopup.getVerticalOffset();
        }
        if (IS_AT_LEAST_JB) {
            return super.getDropDownVerticalOffset();
        }
        return 0;
    }

    @Override // android.widget.Spinner
    public int getDropDownWidth() {
        if (this.mPopup != null) {
            return this.mDropDownWidth;
        }
        if (IS_AT_LEAST_JB) {
            return super.getDropDownWidth();
        }
        return 0;
    }

    @Override // android.widget.Spinner
    public Drawable getPopupBackground() {
        if (this.mPopup != null) {
            return this.mPopup.getBackground();
        }
        if (IS_AT_LEAST_JB) {
            return super.getPopupBackground();
        }
        return null;
    }

    @Override // android.widget.Spinner
    public Context getPopupContext() {
        if (this.mPopup != null) {
            return this.mPopupContext;
        }
        if (IS_AT_LEAST_M) {
            return super.getPopupContext();
        }
        return null;
    }

    @Override // android.widget.Spinner
    public CharSequence getPrompt() {
        return this.mPopup != null ? this.mPopup.getHintText() : super.getPrompt();
    }

    @Override // android.support.v4.view.TintableBackgroundView
    @Nullable
    public ColorStateList getSupportBackgroundTintList() {
        ColorStateList colorStateList = null;
        if (this.mBackgroundTintHelper != null) {
            colorStateList = this.mBackgroundTintHelper.getSupportBackgroundTintList();
        }
        return colorStateList;
    }

    @Override // android.support.v4.view.TintableBackgroundView
    @Nullable
    public PorterDuff.Mode getSupportBackgroundTintMode() {
        PorterDuff.Mode mode = null;
        if (this.mBackgroundTintHelper != null) {
            mode = this.mBackgroundTintHelper.getSupportBackgroundTintMode();
        }
        return mode;
    }

    @Override // android.widget.Spinner, android.widget.AdapterView, android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mPopup == null || !this.mPopup.isShowing()) {
            return;
        }
        this.mPopup.dismiss();
    }

    @Override // android.widget.Spinner, android.widget.AbsSpinner, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (this.mPopup == null || View.MeasureSpec.getMode(i) != Integer.MIN_VALUE) {
            return;
        }
        setMeasuredDimension(Math.min(Math.max(getMeasuredWidth(), compatMeasureContentWidth(getAdapter(), getBackground())), View.MeasureSpec.getSize(i)), getMeasuredHeight());
    }

    @Override // android.widget.Spinner, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mForwardingListener == null || !this.mForwardingListener.onTouch(this, motionEvent)) {
            return super.onTouchEvent(motionEvent);
        }
        return true;
    }

    @Override // android.widget.Spinner, android.view.View
    public boolean performClick() {
        if (this.mPopup != null) {
            if (this.mPopup.isShowing()) {
                return true;
            }
            this.mPopup.show();
            return true;
        }
        return super.performClick();
    }

    @Override // android.widget.Spinner, android.widget.AbsSpinner
    public void setAdapter(SpinnerAdapter spinnerAdapter) {
        if (!this.mPopupSet) {
            this.mTempAdapter = spinnerAdapter;
            return;
        }
        super.setAdapter(spinnerAdapter);
        if (this.mPopup != null) {
            this.mPopup.setAdapter(new DropDownAdapter(spinnerAdapter, (this.mPopupContext == null ? getContext() : this.mPopupContext).getTheme()));
        }
    }

    @Override // android.view.View
    public void setBackgroundDrawable(Drawable drawable) {
        super.setBackgroundDrawable(drawable);
        if (this.mBackgroundTintHelper != null) {
            this.mBackgroundTintHelper.onSetBackgroundDrawable(drawable);
        }
    }

    @Override // android.view.View
    public void setBackgroundResource(@DrawableRes int i) {
        super.setBackgroundResource(i);
        if (this.mBackgroundTintHelper != null) {
            this.mBackgroundTintHelper.onSetBackgroundResource(i);
        }
    }

    @Override // android.widget.Spinner
    public void setDropDownHorizontalOffset(int i) {
        if (this.mPopup != null) {
            this.mPopup.setHorizontalOffset(i);
        } else if (IS_AT_LEAST_JB) {
            super.setDropDownHorizontalOffset(i);
        }
    }

    @Override // android.widget.Spinner
    public void setDropDownVerticalOffset(int i) {
        if (this.mPopup != null) {
            this.mPopup.setVerticalOffset(i);
        } else if (IS_AT_LEAST_JB) {
            super.setDropDownVerticalOffset(i);
        }
    }

    @Override // android.widget.Spinner
    public void setDropDownWidth(int i) {
        if (this.mPopup != null) {
            this.mDropDownWidth = i;
        } else if (IS_AT_LEAST_JB) {
            super.setDropDownWidth(i);
        }
    }

    @Override // android.widget.Spinner
    public void setPopupBackgroundDrawable(Drawable drawable) {
        if (this.mPopup != null) {
            this.mPopup.setBackgroundDrawable(drawable);
        } else if (IS_AT_LEAST_JB) {
            super.setPopupBackgroundDrawable(drawable);
        }
    }

    @Override // android.widget.Spinner
    public void setPopupBackgroundResource(@DrawableRes int i) {
        setPopupBackgroundDrawable(ContextCompat.getDrawable(getPopupContext(), i));
    }

    @Override // android.widget.Spinner
    public void setPrompt(CharSequence charSequence) {
        if (this.mPopup != null) {
            this.mPopup.setPromptText(charSequence);
        } else {
            super.setPrompt(charSequence);
        }
    }

    @Override // android.support.v4.view.TintableBackgroundView
    public void setSupportBackgroundTintList(@Nullable ColorStateList colorStateList) {
        if (this.mBackgroundTintHelper != null) {
            this.mBackgroundTintHelper.setSupportBackgroundTintList(colorStateList);
        }
    }

    @Override // android.support.v4.view.TintableBackgroundView
    public void setSupportBackgroundTintMode(@Nullable PorterDuff.Mode mode) {
        if (this.mBackgroundTintHelper != null) {
            this.mBackgroundTintHelper.setSupportBackgroundTintMode(mode);
        }
    }
}
