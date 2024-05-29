package android.support.v7.widget;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.ViewCompat;
import android.support.v7.appcompat.R$dimen;
import android.support.v7.appcompat.R$id;
import android.support.v7.appcompat.R$layout;
import android.support.v7.appcompat.R$string;
import android.support.v7.appcompat.R$styleable;
import android.support.v7.view.menu.ShowableListMenu;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
/* loaded from: a.zip:android/support/v7/widget/ActivityChooserView.class */
public class ActivityChooserView extends ViewGroup {
    private final LinearLayoutCompat mActivityChooserContent;
    private final Drawable mActivityChooserContentBackground;
    private final ActivityChooserViewAdapter mAdapter;
    private final Callbacks mCallbacks;
    private int mDefaultActionButtonContentDescription;
    private final FrameLayout mDefaultActivityButton;
    private final ImageView mDefaultActivityButtonImage;
    private final FrameLayout mExpandActivityOverflowButton;
    private final ImageView mExpandActivityOverflowButtonImage;
    private int mInitialActivityCount;
    private boolean mIsAttachedToWindow;
    private boolean mIsSelectingDefaultActivity;
    private final int mListPopupMaxWidth;
    private ListPopupWindow mListPopupWindow;
    private final DataSetObserver mModelDataSetOberver;
    private PopupWindow.OnDismissListener mOnDismissListener;
    private final ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener;
    ActionProvider mProvider;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/ActivityChooserView$ActivityChooserViewAdapter.class */
    public class ActivityChooserViewAdapter extends BaseAdapter {
        private ActivityChooserModel mDataModel;
        private boolean mHighlightDefaultActivity;
        private int mMaxActivityCount;
        private boolean mShowDefaultActivity;
        private boolean mShowFooterView;
        final ActivityChooserView this$0;

        private ActivityChooserViewAdapter(ActivityChooserView activityChooserView) {
            this.this$0 = activityChooserView;
            this.mMaxActivityCount = 4;
        }

        /* synthetic */ ActivityChooserViewAdapter(ActivityChooserView activityChooserView, ActivityChooserViewAdapter activityChooserViewAdapter) {
            this(activityChooserView);
        }

        public int getActivityCount() {
            return this.mDataModel.getActivityCount();
        }

        @Override // android.widget.Adapter
        public int getCount() {
            int activityCount = this.mDataModel.getActivityCount();
            int i = activityCount;
            if (!this.mShowDefaultActivity) {
                i = activityCount;
                if (this.mDataModel.getDefaultActivity() != null) {
                    i = activityCount - 1;
                }
            }
            int min = Math.min(i, this.mMaxActivityCount);
            int i2 = min;
            if (this.mShowFooterView) {
                i2 = min + 1;
            }
            return i2;
        }

        public ActivityChooserModel getDataModel() {
            return this.mDataModel;
        }

        public ResolveInfo getDefaultActivity() {
            return this.mDataModel.getDefaultActivity();
        }

        public int getHistorySize() {
            return this.mDataModel.getHistorySize();
        }

        @Override // android.widget.Adapter
        public Object getItem(int i) {
            switch (getItemViewType(i)) {
                case 0:
                    int i2 = i;
                    if (!this.mShowDefaultActivity) {
                        i2 = i;
                        if (this.mDataModel.getDefaultActivity() != null) {
                            i2 = i + 1;
                        }
                    }
                    return this.mDataModel.getActivity(i2);
                case 1:
                    return null;
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        @Override // android.widget.BaseAdapter, android.widget.Adapter
        public int getItemViewType(int i) {
            return (this.mShowFooterView && i == getCount() - 1) ? 1 : 0;
        }

        public boolean getShowDefaultActivity() {
            return this.mShowDefaultActivity;
        }

        /* JADX WARN: Code restructure failed: missing block: B:16:0x0079, code lost:
            if (r7.getId() != android.support.v7.appcompat.R$id.list_item) goto L27;
         */
        /* JADX WARN: Code restructure failed: missing block: B:9:0x0030, code lost:
            if (r7.getId() != 1) goto L12;
         */
        @Override // android.widget.Adapter
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public View getView(int i, View view, ViewGroup viewGroup) {
            View inflate;
            View inflate2;
            switch (getItemViewType(i)) {
                case 0:
                    if (view != null) {
                        inflate = view;
                        break;
                    }
                    inflate = LayoutInflater.from(this.this$0.getContext()).inflate(R$layout.abc_activity_chooser_view_list_item, viewGroup, false);
                    PackageManager packageManager = this.this$0.getContext().getPackageManager();
                    ImageView imageView = (ImageView) inflate.findViewById(R$id.icon);
                    ResolveInfo resolveInfo = (ResolveInfo) getItem(i);
                    imageView.setImageDrawable(resolveInfo.loadIcon(packageManager));
                    ((TextView) inflate.findViewById(R$id.title)).setText(resolveInfo.loadLabel(packageManager));
                    if (this.mShowDefaultActivity && i == 0 && this.mHighlightDefaultActivity) {
                        ViewCompat.setActivated(inflate, true);
                    } else {
                        ViewCompat.setActivated(inflate, false);
                    }
                    return inflate;
                case 1:
                    if (view != null) {
                        inflate2 = view;
                        break;
                    }
                    inflate2 = LayoutInflater.from(this.this$0.getContext()).inflate(R$layout.abc_activity_chooser_view_list_item, viewGroup, false);
                    inflate2.setId(1);
                    ((TextView) inflate2.findViewById(R$id.title)).setText(this.this$0.getContext().getString(R$string.abc_activity_chooser_view_see_all));
                    return inflate2;
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override // android.widget.BaseAdapter, android.widget.Adapter
        public int getViewTypeCount() {
            return 3;
        }

        public int measureContentWidth() {
            int i = this.mMaxActivityCount;
            this.mMaxActivityCount = Integer.MAX_VALUE;
            int i2 = 0;
            View view = null;
            int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
            int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(0, 0);
            int count = getCount();
            for (int i3 = 0; i3 < count; i3++) {
                view = getView(i3, view, null);
                view.measure(makeMeasureSpec, makeMeasureSpec2);
                i2 = Math.max(i2, view.getMeasuredWidth());
            }
            this.mMaxActivityCount = i;
            return i2;
        }

        public void setMaxActivityCount(int i) {
            if (this.mMaxActivityCount != i) {
                this.mMaxActivityCount = i;
                notifyDataSetChanged();
            }
        }

        public void setShowDefaultActivity(boolean z, boolean z2) {
            if (this.mShowDefaultActivity == z && this.mHighlightDefaultActivity == z2) {
                return;
            }
            this.mShowDefaultActivity = z;
            this.mHighlightDefaultActivity = z2;
            notifyDataSetChanged();
        }

        public void setShowFooterView(boolean z) {
            if (this.mShowFooterView != z) {
                this.mShowFooterView = z;
                notifyDataSetChanged();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/ActivityChooserView$Callbacks.class */
    public class Callbacks implements AdapterView.OnItemClickListener, View.OnClickListener, View.OnLongClickListener, PopupWindow.OnDismissListener {
        final ActivityChooserView this$0;

        private Callbacks(ActivityChooserView activityChooserView) {
            this.this$0 = activityChooserView;
        }

        /* synthetic */ Callbacks(ActivityChooserView activityChooserView, Callbacks callbacks) {
            this(activityChooserView);
        }

        private void notifyOnDismissListener() {
            if (this.this$0.mOnDismissListener != null) {
                this.this$0.mOnDismissListener.onDismiss();
            }
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (view != this.this$0.mDefaultActivityButton) {
                if (view != this.this$0.mExpandActivityOverflowButton) {
                    throw new IllegalArgumentException();
                }
                this.this$0.mIsSelectingDefaultActivity = false;
                this.this$0.showPopupUnchecked(this.this$0.mInitialActivityCount);
                return;
            }
            this.this$0.dismissPopup();
            Intent chooseActivity = this.this$0.mAdapter.getDataModel().chooseActivity(this.this$0.mAdapter.getDataModel().getActivityIndex(this.this$0.mAdapter.getDefaultActivity()));
            if (chooseActivity != null) {
                chooseActivity.addFlags(524288);
                this.this$0.getContext().startActivity(chooseActivity);
            }
        }

        @Override // android.widget.PopupWindow.OnDismissListener
        public void onDismiss() {
            notifyOnDismissListener();
            if (this.this$0.mProvider != null) {
                this.this$0.mProvider.subUiVisibilityChanged(false);
            }
        }

        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            switch (((ActivityChooserViewAdapter) adapterView.getAdapter()).getItemViewType(i)) {
                case 0:
                    this.this$0.dismissPopup();
                    if (this.this$0.mIsSelectingDefaultActivity) {
                        if (i > 0) {
                            this.this$0.mAdapter.getDataModel().setDefaultActivity(i);
                            return;
                        }
                        return;
                    }
                    if (!this.this$0.mAdapter.getShowDefaultActivity()) {
                        i++;
                    }
                    Intent chooseActivity = this.this$0.mAdapter.getDataModel().chooseActivity(i);
                    if (chooseActivity != null) {
                        chooseActivity.addFlags(524288);
                        this.this$0.getContext().startActivity(chooseActivity);
                        return;
                    }
                    return;
                case 1:
                    this.this$0.showPopupUnchecked(Integer.MAX_VALUE);
                    return;
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View view) {
            if (view == this.this$0.mDefaultActivityButton) {
                if (this.this$0.mAdapter.getCount() > 0) {
                    this.this$0.mIsSelectingDefaultActivity = true;
                    this.this$0.showPopupUnchecked(this.this$0.mInitialActivityCount);
                    return true;
                }
                return true;
            }
            throw new IllegalArgumentException();
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/ActivityChooserView$InnerLayout.class */
    public static class InnerLayout extends LinearLayoutCompat {
        private static final int[] TINT_ATTRS = {16842964};

        public InnerLayout(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(context, attributeSet, TINT_ATTRS);
            setBackgroundDrawable(obtainStyledAttributes.getDrawable(0));
            obtainStyledAttributes.recycle();
        }
    }

    public ActivityChooserView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ActivityChooserView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mModelDataSetOberver = new DataSetObserver(this) { // from class: android.support.v7.widget.ActivityChooserView.1
            final ActivityChooserView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.database.DataSetObserver
            public void onChanged() {
                super.onChanged();
                this.this$0.mAdapter.notifyDataSetChanged();
            }

            @Override // android.database.DataSetObserver
            public void onInvalidated() {
                super.onInvalidated();
                this.this$0.mAdapter.notifyDataSetInvalidated();
            }
        };
        this.mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener(this) { // from class: android.support.v7.widget.ActivityChooserView.2
            final ActivityChooserView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                if (this.this$0.isShowingPopup()) {
                    if (!this.this$0.isShown()) {
                        this.this$0.getListPopupWindow().dismiss();
                        return;
                    }
                    this.this$0.getListPopupWindow().show();
                    if (this.this$0.mProvider != null) {
                        this.this$0.mProvider.subUiVisibilityChanged(true);
                    }
                }
            }
        };
        this.mInitialActivityCount = 4;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.ActivityChooserView, i, 0);
        this.mInitialActivityCount = obtainStyledAttributes.getInt(R$styleable.ActivityChooserView_initialActivityCount, 4);
        Drawable drawable = obtainStyledAttributes.getDrawable(R$styleable.ActivityChooserView_expandActivityOverflowButtonDrawable);
        obtainStyledAttributes.recycle();
        LayoutInflater.from(getContext()).inflate(R$layout.abc_activity_chooser_view, (ViewGroup) this, true);
        this.mCallbacks = new Callbacks(this, null);
        this.mActivityChooserContent = (LinearLayoutCompat) findViewById(R$id.activity_chooser_view_content);
        this.mActivityChooserContentBackground = this.mActivityChooserContent.getBackground();
        this.mDefaultActivityButton = (FrameLayout) findViewById(R$id.default_activity_button);
        this.mDefaultActivityButton.setOnClickListener(this.mCallbacks);
        this.mDefaultActivityButton.setOnLongClickListener(this.mCallbacks);
        this.mDefaultActivityButtonImage = (ImageView) this.mDefaultActivityButton.findViewById(R$id.image);
        FrameLayout frameLayout = (FrameLayout) findViewById(R$id.expand_activities_button);
        frameLayout.setOnClickListener(this.mCallbacks);
        frameLayout.setOnTouchListener(new ForwardingListener(this, frameLayout) { // from class: android.support.v7.widget.ActivityChooserView.3
            final ActivityChooserView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.support.v7.widget.ForwardingListener
            public ShowableListMenu getPopup() {
                return this.this$0.getListPopupWindow();
            }

            @Override // android.support.v7.widget.ForwardingListener
            protected boolean onForwardingStarted() {
                this.this$0.showPopup();
                return true;
            }

            @Override // android.support.v7.widget.ForwardingListener
            protected boolean onForwardingStopped() {
                this.this$0.dismissPopup();
                return true;
            }
        });
        this.mExpandActivityOverflowButton = frameLayout;
        this.mExpandActivityOverflowButtonImage = (ImageView) frameLayout.findViewById(R$id.image);
        this.mExpandActivityOverflowButtonImage.setImageDrawable(drawable);
        this.mAdapter = new ActivityChooserViewAdapter(this, null);
        this.mAdapter.registerDataSetObserver(new DataSetObserver(this) { // from class: android.support.v7.widget.ActivityChooserView.4
            final ActivityChooserView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.database.DataSetObserver
            public void onChanged() {
                super.onChanged();
                this.this$0.updateAppearance();
            }
        });
        Resources resources = context.getResources();
        this.mListPopupMaxWidth = Math.max(resources.getDisplayMetrics().widthPixels / 2, resources.getDimensionPixelSize(R$dimen.abc_config_prefDialogWidth));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ListPopupWindow getListPopupWindow() {
        if (this.mListPopupWindow == null) {
            this.mListPopupWindow = new ListPopupWindow(getContext());
            this.mListPopupWindow.setAdapter(this.mAdapter);
            this.mListPopupWindow.setAnchorView(this);
            this.mListPopupWindow.setModal(true);
            this.mListPopupWindow.setOnItemClickListener(this.mCallbacks);
            this.mListPopupWindow.setOnDismissListener(this.mCallbacks);
        }
        return this.mListPopupWindow;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showPopupUnchecked(int i) {
        if (this.mAdapter.getDataModel() == null) {
            throw new IllegalStateException("No data model. Did you call #setDataModel?");
        }
        getViewTreeObserver().addOnGlobalLayoutListener(this.mOnGlobalLayoutListener);
        boolean z = this.mDefaultActivityButton.getVisibility() == 0;
        int activityCount = this.mAdapter.getActivityCount();
        int i2 = z ? 1 : 0;
        if (i == Integer.MAX_VALUE || activityCount <= i + i2) {
            this.mAdapter.setShowFooterView(false);
            this.mAdapter.setMaxActivityCount(i);
        } else {
            this.mAdapter.setShowFooterView(true);
            this.mAdapter.setMaxActivityCount(i - 1);
        }
        ListPopupWindow listPopupWindow = getListPopupWindow();
        if (listPopupWindow.isShowing()) {
            return;
        }
        if (this.mIsSelectingDefaultActivity || !z) {
            this.mAdapter.setShowDefaultActivity(true, z);
        } else {
            this.mAdapter.setShowDefaultActivity(false, false);
        }
        listPopupWindow.setContentWidth(Math.min(this.mAdapter.measureContentWidth(), this.mListPopupMaxWidth));
        listPopupWindow.show();
        if (this.mProvider != null) {
            this.mProvider.subUiVisibilityChanged(true);
        }
        listPopupWindow.getListView().setContentDescription(getContext().getString(R$string.abc_activitychooserview_choose_application));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAppearance() {
        if (this.mAdapter.getCount() > 0) {
            this.mExpandActivityOverflowButton.setEnabled(true);
        } else {
            this.mExpandActivityOverflowButton.setEnabled(false);
        }
        int activityCount = this.mAdapter.getActivityCount();
        int historySize = this.mAdapter.getHistorySize();
        if (activityCount == 1 || (activityCount > 1 && historySize > 0)) {
            this.mDefaultActivityButton.setVisibility(0);
            ResolveInfo defaultActivity = this.mAdapter.getDefaultActivity();
            PackageManager packageManager = getContext().getPackageManager();
            this.mDefaultActivityButtonImage.setImageDrawable(defaultActivity.loadIcon(packageManager));
            if (this.mDefaultActionButtonContentDescription != 0) {
                this.mDefaultActivityButton.setContentDescription(getContext().getString(this.mDefaultActionButtonContentDescription, defaultActivity.loadLabel(packageManager)));
            }
        } else {
            this.mDefaultActivityButton.setVisibility(8);
        }
        if (this.mDefaultActivityButton.getVisibility() == 0) {
            this.mActivityChooserContent.setBackgroundDrawable(this.mActivityChooserContentBackground);
        } else {
            this.mActivityChooserContent.setBackgroundDrawable(null);
        }
    }

    public boolean dismissPopup() {
        if (isShowingPopup()) {
            getListPopupWindow().dismiss();
            ViewTreeObserver viewTreeObserver = getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.removeGlobalOnLayoutListener(this.mOnGlobalLayoutListener);
                return true;
            }
            return true;
        }
        return true;
    }

    public boolean isShowingPopup() {
        return getListPopupWindow().isShowing();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ActivityChooserModel dataModel = this.mAdapter.getDataModel();
        if (dataModel != null) {
            dataModel.registerObserver(this.mModelDataSetOberver);
        }
        this.mIsAttachedToWindow = true;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ActivityChooserModel dataModel = this.mAdapter.getDataModel();
        if (dataModel != null) {
            dataModel.unregisterObserver(this.mModelDataSetOberver);
        }
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.removeGlobalOnLayoutListener(this.mOnGlobalLayoutListener);
        }
        if (isShowingPopup()) {
            dismissPopup();
        }
        this.mIsAttachedToWindow = false;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        this.mActivityChooserContent.layout(0, 0, i3 - i, i4 - i2);
        if (isShowingPopup()) {
            return;
        }
        dismissPopup();
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        LinearLayoutCompat linearLayoutCompat = this.mActivityChooserContent;
        int i3 = i2;
        if (this.mDefaultActivityButton.getVisibility() != 0) {
            i3 = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i2), 1073741824);
        }
        measureChild(linearLayoutCompat, i, i3);
        setMeasuredDimension(linearLayoutCompat.getMeasuredWidth(), linearLayoutCompat.getMeasuredHeight());
    }

    public boolean showPopup() {
        if (isShowingPopup() || !this.mIsAttachedToWindow) {
            return false;
        }
        this.mIsSelectingDefaultActivity = false;
        showPopupUnchecked(this.mInitialActivityCount);
        return true;
    }
}
