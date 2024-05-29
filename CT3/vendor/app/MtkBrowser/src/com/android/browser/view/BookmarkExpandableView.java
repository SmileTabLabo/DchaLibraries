package com.android.browser.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.browser.BreadCrumbView;
import com.android.browser.BrowserBookmarksAdapter;
import com.android.browser.provider.BrowserContract;
import com.android.internal.view.menu.MenuBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: b.zip:com/android/browser/view/BookmarkExpandableView.class */
public class BookmarkExpandableView extends ExpandableListView implements BreadCrumbView.Controller {
    private BookmarkAccountAdapter mAdapter;
    private BreadCrumbView.Controller mBreadcrumbController;
    private View.OnClickListener mChildClickListener;
    private int mColumnWidth;
    private Context mContext;
    private ContextMenu.ContextMenuInfo mContextMenuInfo;
    private View.OnClickListener mGroupOnClickListener;
    private boolean mLongClickable;
    private int mMaxColumnCount;
    private ExpandableListView.OnChildClickListener mOnChildClickListener;
    private View.OnCreateContextMenuListener mOnCreateContextMenuListener;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/view/BookmarkExpandableView$BookmarkAccountAdapter.class */
    public class BookmarkAccountAdapter extends BaseExpandableListAdapter {
        ArrayList<BrowserBookmarksAdapter> mChildren;
        ArrayList<String> mGroups;
        LayoutInflater mInflater;
        final BookmarkExpandableView this$0;
        HashMap<Integer, BreadCrumbView> mBreadcrumbs = new HashMap<>();
        int mRowCount = 1;
        int mLastViewWidth = -1;
        int mRowPadding = -1;
        DataSetObserver mObserver = new DataSetObserver(this) { // from class: com.android.browser.view.BookmarkExpandableView.BookmarkAccountAdapter.1
            final BookmarkAccountAdapter this$1;

            {
                this.this$1 = this;
            }

            @Override // android.database.DataSetObserver
            public void onChanged() {
                this.this$1.notifyDataSetChanged();
            }

            @Override // android.database.DataSetObserver
            public void onInvalidated() {
                this.this$1.notifyDataSetInvalidated();
            }
        };

        public BookmarkAccountAdapter(BookmarkExpandableView bookmarkExpandableView, Context context) {
            this.this$0 = bookmarkExpandableView;
            bookmarkExpandableView.mContext = context;
            this.mInflater = LayoutInflater.from(bookmarkExpandableView.mContext);
            this.mChildren = new ArrayList<>();
            this.mGroups = new ArrayList<>();
        }

        public void clear() {
            this.mGroups.clear();
            this.mChildren.clear();
            notifyDataSetChanged();
        }

        public BreadCrumbView getBreadCrumbView(int i) {
            BreadCrumbView breadCrumbView = this.mBreadcrumbs.get(Integer.valueOf(i));
            BreadCrumbView breadCrumbView2 = breadCrumbView;
            if (breadCrumbView == null) {
                breadCrumbView2 = (BreadCrumbView) this.mInflater.inflate(2130968588, (ViewGroup) null);
                breadCrumbView2.setController(this.this$0);
                breadCrumbView2.setUseBackButton(true);
                breadCrumbView2.setMaxVisible(2);
                breadCrumbView2.pushView(this.this$0.mContext.getString(2131493025), false, BrowserContract.Bookmarks.CONTENT_URI_DEFAULT_FOLDER);
                breadCrumbView2.setTag(2131558400, Integer.valueOf(i));
                breadCrumbView2.setVisibility(8);
                this.mBreadcrumbs.put(Integer.valueOf(i), breadCrumbView2);
            }
            return breadCrumbView2;
        }

        @Override // android.widget.ExpandableListAdapter
        public Object getChild(int i, int i2) {
            return this.mChildren.get(i).getItem(i2);
        }

        @Override // android.widget.ExpandableListAdapter
        public long getChildId(int i, int i2) {
            return i2;
        }

        @Override // android.widget.ExpandableListAdapter
        public View getChildView(int i, int i2, boolean z, View view, ViewGroup viewGroup) {
            View view2 = view;
            if (view == null) {
                view2 = this.mInflater.inflate(2130968580, viewGroup, false);
            }
            BrowserBookmarksAdapter browserBookmarksAdapter = this.mChildren.get(i);
            int i3 = this.mRowCount;
            LinearLayout linearLayout = (LinearLayout) view2;
            if (linearLayout.getChildCount() > i3) {
                linearLayout.removeViews(i3, linearLayout.getChildCount() - i3);
            }
            for (int i4 = 0; i4 < i3; i4++) {
                View childAt = linearLayout.getChildCount() > i4 ? linearLayout.getChildAt(i4) : null;
                int i5 = (i2 * i3) + i4;
                if (i5 < browserBookmarksAdapter.getCount()) {
                    View view3 = browserBookmarksAdapter.getView(i5, childAt, linearLayout);
                    view3.setTag(2131558400, Integer.valueOf(i));
                    view3.setTag(2131558401, Integer.valueOf(i5));
                    view3.setOnClickListener(this.this$0.mChildClickListener);
                    view3.setLongClickable(this.this$0.mLongClickable);
                    if (linearLayout.getChildCount() > 1) {
                        view3.setPadding(linearLayout.getChildAt(0).getPaddingLeft(), linearLayout.getChildAt(0).getPaddingTop(), linearLayout.getChildAt(0).getPaddingRight(), linearLayout.getChildAt(0).getPaddingBottom());
                    }
                    if (childAt == null) {
                        linearLayout.addView(view3);
                    } else if (childAt != view3) {
                        linearLayout.removeViewAt(i4);
                        linearLayout.addView(view3, i4);
                    } else {
                        childAt.setVisibility(0);
                    }
                } else if (childAt != null) {
                    childAt.setVisibility(8);
                }
            }
            return linearLayout;
        }

        @Override // android.widget.ExpandableListAdapter
        public int getChildrenCount(int i) {
            return (int) Math.ceil(this.mChildren.get(i).getCount() / this.mRowCount);
        }

        @Override // android.widget.ExpandableListAdapter
        public Object getGroup(int i) {
            return this.mChildren.get(i);
        }

        @Override // android.widget.ExpandableListAdapter
        public int getGroupCount() {
            return this.mGroups.size();
        }

        @Override // android.widget.ExpandableListAdapter
        public long getGroupId(int i) {
            return i;
        }

        @Override // android.widget.ExpandableListAdapter
        public View getGroupView(int i, boolean z, View view, ViewGroup viewGroup) {
            View view2 = view;
            if (view == null) {
                view2 = this.mInflater.inflate(2130968581, viewGroup, false);
                view2.setOnClickListener(this.this$0.mGroupOnClickListener);
            }
            view2.setTag(2131558400, Integer.valueOf(i));
            FrameLayout frameLayout = (FrameLayout) view2.findViewById(2131558422);
            frameLayout.removeAllViews();
            BreadCrumbView breadCrumbView = getBreadCrumbView(i);
            if (breadCrumbView.getParent() != null) {
                ((ViewGroup) breadCrumbView.getParent()).removeView(breadCrumbView);
            }
            frameLayout.addView(breadCrumbView);
            TextView textView = (TextView) view2.findViewById(2131558421);
            String str = this.mGroups.get(i);
            String str2 = str;
            if (str == null) {
                str2 = this.this$0.mContext.getString(2131493284);
            }
            textView.setText(str2);
            return view2;
        }

        @Override // android.widget.ExpandableListAdapter
        public boolean hasStableIds() {
            return false;
        }

        @Override // android.widget.ExpandableListAdapter
        public boolean isChildSelectable(int i, int i2) {
            return true;
        }

        public void measureChildren(int i) {
            if (this.mLastViewWidth == i) {
                return;
            }
            int i2 = i / this.this$0.mColumnWidth;
            int i3 = i2;
            if (this.this$0.mMaxColumnCount > 0) {
                i3 = Math.min(i2, this.this$0.mMaxColumnCount);
            }
            int i4 = (i - (this.this$0.mColumnWidth * i3)) / 2;
            boolean z = (i3 == this.mRowCount && i4 == this.mRowPadding) ? false : true;
            this.mRowCount = i3;
            this.mRowPadding = i4;
            this.mLastViewWidth = i;
            if (z) {
                notifyDataSetChanged();
            }
        }
    }

    /* loaded from: b.zip:com/android/browser/view/BookmarkExpandableView$BookmarkContextMenuInfo.class */
    public static class BookmarkContextMenuInfo implements ContextMenu.ContextMenuInfo {
        public int childPosition;
        public int groupPosition;

        private BookmarkContextMenuInfo(int i, int i2) {
            this.childPosition = i;
            this.groupPosition = i2;
        }

        /* synthetic */ BookmarkContextMenuInfo(int i, int i2, BookmarkContextMenuInfo bookmarkContextMenuInfo) {
            this(i, i2);
        }
    }

    public BookmarkExpandableView(Context context) {
        super(context);
        this.mContextMenuInfo = null;
        this.mChildClickListener = new View.OnClickListener(this) { // from class: com.android.browser.view.BookmarkExpandableView.1
            final BookmarkExpandableView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (view.getVisibility() != 0) {
                    return;
                }
                int intValue = ((Integer) view.getTag(2131558400)).intValue();
                int intValue2 = ((Integer) view.getTag(2131558401)).intValue();
                if (this.this$0.mAdapter.getGroupCount() <= intValue || this.this$0.mAdapter.mChildren.get(intValue).getCount() <= intValue2) {
                    return;
                }
                long itemId = this.this$0.mAdapter.mChildren.get(intValue).getItemId(intValue2);
                if (this.this$0.mOnChildClickListener != null) {
                    this.this$0.mOnChildClickListener.onChildClick(this.this$0, view, intValue, intValue2, itemId);
                }
            }
        };
        this.mGroupOnClickListener = new View.OnClickListener(this) { // from class: com.android.browser.view.BookmarkExpandableView.2
            final BookmarkExpandableView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                int intValue = ((Integer) view.getTag(2131558400)).intValue();
                if (this.this$0.isGroupExpanded(intValue)) {
                    this.this$0.collapseGroup(intValue);
                    return;
                }
                this.this$0.hideAllGroups();
                this.this$0.expandGroup(intValue, true);
            }
        };
        init(context);
    }

    public BookmarkExpandableView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContextMenuInfo = null;
        this.mChildClickListener = new View.OnClickListener(this) { // from class: com.android.browser.view.BookmarkExpandableView.1
            final BookmarkExpandableView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (view.getVisibility() != 0) {
                    return;
                }
                int intValue = ((Integer) view.getTag(2131558400)).intValue();
                int intValue2 = ((Integer) view.getTag(2131558401)).intValue();
                if (this.this$0.mAdapter.getGroupCount() <= intValue || this.this$0.mAdapter.mChildren.get(intValue).getCount() <= intValue2) {
                    return;
                }
                long itemId = this.this$0.mAdapter.mChildren.get(intValue).getItemId(intValue2);
                if (this.this$0.mOnChildClickListener != null) {
                    this.this$0.mOnChildClickListener.onChildClick(this.this$0, view, intValue, intValue2, itemId);
                }
            }
        };
        this.mGroupOnClickListener = new View.OnClickListener(this) { // from class: com.android.browser.view.BookmarkExpandableView.2
            final BookmarkExpandableView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                int intValue = ((Integer) view.getTag(2131558400)).intValue();
                if (this.this$0.isGroupExpanded(intValue)) {
                    this.this$0.collapseGroup(intValue);
                    return;
                }
                this.this$0.hideAllGroups();
                this.this$0.expandGroup(intValue, true);
            }
        };
        init(context);
    }

    public BookmarkExpandableView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mContextMenuInfo = null;
        this.mChildClickListener = new View.OnClickListener(this) { // from class: com.android.browser.view.BookmarkExpandableView.1
            final BookmarkExpandableView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (view.getVisibility() != 0) {
                    return;
                }
                int intValue = ((Integer) view.getTag(2131558400)).intValue();
                int intValue2 = ((Integer) view.getTag(2131558401)).intValue();
                if (this.this$0.mAdapter.getGroupCount() <= intValue || this.this$0.mAdapter.mChildren.get(intValue).getCount() <= intValue2) {
                    return;
                }
                long itemId = this.this$0.mAdapter.mChildren.get(intValue).getItemId(intValue2);
                if (this.this$0.mOnChildClickListener != null) {
                    this.this$0.mOnChildClickListener.onChildClick(this.this$0, view, intValue, intValue2, itemId);
                }
            }
        };
        this.mGroupOnClickListener = new View.OnClickListener(this) { // from class: com.android.browser.view.BookmarkExpandableView.2
            final BookmarkExpandableView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                int intValue = ((Integer) view.getTag(2131558400)).intValue();
                if (this.this$0.isGroupExpanded(intValue)) {
                    this.this$0.collapseGroup(intValue);
                    return;
                }
                this.this$0.hideAllGroups();
                this.this$0.expandGroup(intValue, true);
            }
        };
        init(context);
    }

    public void addAccount(String str, BrowserBookmarksAdapter browserBookmarksAdapter, boolean z) {
        int indexOf = this.mAdapter.mGroups.indexOf(str);
        if (indexOf >= 0) {
            BrowserBookmarksAdapter browserBookmarksAdapter2 = this.mAdapter.mChildren.get(indexOf);
            if (browserBookmarksAdapter2 != browserBookmarksAdapter) {
                browserBookmarksAdapter2.unregisterDataSetObserver(this.mAdapter.mObserver);
                this.mAdapter.mChildren.remove(indexOf);
                this.mAdapter.mChildren.add(indexOf, browserBookmarksAdapter);
                browserBookmarksAdapter.registerDataSetObserver(this.mAdapter.mObserver);
            }
        } else {
            this.mAdapter.mGroups.add(str);
            this.mAdapter.mChildren.add(browserBookmarksAdapter);
            browserBookmarksAdapter.registerDataSetObserver(this.mAdapter.mObserver);
        }
        this.mAdapter.notifyDataSetChanged();
        if (z) {
            expandGroup(this.mAdapter.getGroupCount() - 1);
        }
    }

    public void clearAccounts() {
        this.mAdapter.clear();
    }

    @Override // android.view.View, android.view.ViewParent
    public void createContextMenu(ContextMenu contextMenu) {
        ContextMenu.ContextMenuInfo contextMenuInfo = getContextMenuInfo();
        ((MenuBuilder) contextMenu).setCurrentMenuInfo(contextMenuInfo);
        onCreateContextMenu(contextMenu);
        if (this.mOnCreateContextMenuListener != null) {
            this.mOnCreateContextMenuListener.onCreateContextMenu(contextMenu, this, contextMenuInfo);
        }
        ((MenuBuilder) contextMenu).setCurrentMenuInfo((ContextMenu.ContextMenuInfo) null);
        if (this.mParent != null) {
            this.mParent.createContextMenu(contextMenu);
        }
    }

    public BreadCrumbView getBreadCrumbs(int i) {
        return this.mAdapter.getBreadCrumbView(i);
    }

    public BrowserBookmarksAdapter getChildAdapter(int i) {
        return this.mAdapter.mChildren.get(i);
    }

    @Override // android.widget.AbsListView, android.view.View
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return this.mContextMenuInfo;
    }

    public void hideAllGroups() {
        for (int i = 0; i < this.mAdapter.getGroupCount(); i++) {
            collapseGroup(i);
        }
    }

    void init(Context context) {
        this.mContext = context;
        setItemsCanFocus(true);
        setLongClickable(false);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(2131623943);
        setScrollBarStyle(33554432);
        this.mAdapter = new BookmarkAccountAdapter(this, this.mContext);
        super.setAdapter(this.mAdapter);
    }

    @Override // android.widget.ListView, android.widget.AbsListView, android.view.View
    protected void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        int mode = View.MeasureSpec.getMode(i);
        if (size > 0) {
            this.mAdapter.measureChildren(size);
            setPadding(this.mAdapter.mRowPadding, 0, this.mAdapter.mRowPadding, 0);
            i = View.MeasureSpec.makeMeasureSpec(size, mode);
        }
        super.onMeasure(i, i2);
        if (size != getMeasuredWidth()) {
            this.mAdapter.measureChildren(getMeasuredWidth());
        }
    }

    @Override // com.android.browser.BreadCrumbView.Controller
    public void onTop(BreadCrumbView breadCrumbView, int i, Object obj) {
        if (this.mBreadcrumbController != null) {
            this.mBreadcrumbController.onTop(breadCrumbView, i, obj);
        }
    }

    public JSONObject saveGroupState() throws JSONException {
        JSONObject jSONObject = new JSONObject();
        int groupCount = this.mAdapter.getGroupCount();
        for (int i = 0; i < groupCount; i++) {
            String str = this.mAdapter.mGroups.get(i);
            if (!isGroupExpanded(i)) {
                if (str == null) {
                    str = "local";
                }
                jSONObject.put(str, false);
            }
        }
        return jSONObject;
    }

    @Override // android.widget.ExpandableListView
    public void setAdapter(ExpandableListAdapter expandableListAdapter) {
        throw new RuntimeException("Not supported");
    }

    public void setBreadcrumbController(BreadCrumbView.Controller controller) {
        this.mBreadcrumbController = controller;
    }

    public void setColumnWidthFromLayout(int i) {
        View inflate = LayoutInflater.from(this.mContext).inflate(i, (ViewGroup) this, false);
        inflate.measure(0, 0);
        this.mColumnWidth = inflate.getMeasuredWidth();
    }

    @Override // android.widget.ExpandableListView
    public void setOnChildClickListener(ExpandableListView.OnChildClickListener onChildClickListener) {
        this.mOnChildClickListener = onChildClickListener;
    }

    @Override // android.view.View
    public void setOnCreateContextMenuListener(View.OnCreateContextMenuListener onCreateContextMenuListener) {
        this.mOnCreateContextMenuListener = onCreateContextMenuListener;
        if (this.mLongClickable) {
            return;
        }
        this.mLongClickable = true;
        if (this.mAdapter != null) {
            this.mAdapter.notifyDataSetChanged();
        }
    }

    @Override // android.widget.AbsListView, android.view.ViewGroup, android.view.ViewParent
    public boolean showContextMenuForChild(View view) {
        Integer num = (Integer) view.getTag(2131558400);
        Integer num2 = (Integer) view.getTag(2131558401);
        if (num == null || num2 == null) {
            return false;
        }
        this.mContextMenuInfo = new BookmarkContextMenuInfo(num2.intValue(), num.intValue(), null);
        if (getParent() != null) {
            getParent().showContextMenuForChild(this);
            return true;
        }
        return true;
    }
}
