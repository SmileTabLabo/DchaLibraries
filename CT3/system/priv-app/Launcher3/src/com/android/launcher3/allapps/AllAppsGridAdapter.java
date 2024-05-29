package com.android.launcher3.allapps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityRecordCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;
import com.android.launcher3.AppInfo;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AlphabeticalAppsList;
import com.android.launcher3.compat.PackageInstallerCompat;
import java.util.HashMap;
import java.util.List;
/* loaded from: a.zip:com/android/launcher3/allapps/AllAppsGridAdapter.class */
public class AllAppsGridAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final AlphabeticalAppsList mApps;
    private int mAppsPerRow;
    private final Rect mBackgroundPadding = new Rect();
    private BindViewCallback mBindViewCallback;
    private String mEmptySearchMessage;
    private final GridLayoutManager mGridLayoutMgr;
    private final GridSpanSizer mGridSizer;
    private final View.OnClickListener mIconClickListener;
    private final View.OnLongClickListener mIconLongClickListener;
    private final boolean mIsRtl;
    private final GridItemDecoration mItemDecoration;
    private final Launcher mLauncher;
    private final LayoutInflater mLayoutInflater;
    private String mMarketAppName;
    private Intent mMarketSearchIntent;
    private String mMarketSearchMessage;
    private final Paint mPredictedAppsDividerPaint;
    private final int mPredictionBarDividerOffset;
    private AllAppsSearchBarController mSearchController;
    private final int mSectionHeaderOffset;
    private final int mSectionNamesMargin;
    private final Paint mSectionTextPaint;
    private final View.OnTouchListener mTouchListener;

    /* loaded from: a.zip:com/android/launcher3/allapps/AllAppsGridAdapter$AppsGridLayoutManager.class */
    public class AppsGridLayoutManager extends GridLayoutManager {
        final AllAppsGridAdapter this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public AppsGridLayoutManager(AllAppsGridAdapter allAppsGridAdapter, Context context) {
            super(context, 1, 1, false);
            this.this$0 = allAppsGridAdapter;
        }

        private int getEmptyRowForAccessibility(int i) {
            int i2;
            if (this.this$0.mApps.hasFilter()) {
                i2 = 1;
            } else {
                i2 = 1;
                if (this.this$0.mApps.hasPredictedComponents()) {
                    if (i == 2) {
                        i2 = 1;
                    } else if (i == 1) {
                        i2 = 2;
                    }
                } else if (i == 1) {
                    i2 = 1;
                }
            }
            return i2;
        }

        @Override // android.support.v7.widget.GridLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
        public int getRowCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
            return super.getRowCountForAccessibility(recycler, state) - getEmptyRowForAccessibility(-1);
        }

        @Override // android.support.v7.widget.LinearLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
        public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
            super.onInitializeAccessibilityEvent(accessibilityEvent);
            AccessibilityRecordCompat asRecord = AccessibilityEventCompat.asRecord(accessibilityEvent);
            int emptyRowForAccessibility = getEmptyRowForAccessibility(-1);
            asRecord.setFromIndex(accessibilityEvent.getFromIndex() - emptyRowForAccessibility);
            asRecord.setToIndex(accessibilityEvent.getToIndex() - emptyRowForAccessibility);
            asRecord.setItemCount(this.this$0.mApps.getNumFilteredApps());
        }

        @Override // android.support.v7.widget.GridLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
        public void onInitializeAccessibilityNodeInfoForItem(RecyclerView.Recycler recycler, RecyclerView.State state, View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            int itemViewType = getItemViewType(view);
            if (itemViewType == 1 || itemViewType == 2) {
                super.onInitializeAccessibilityNodeInfoForItem(recycler, state, view, accessibilityNodeInfoCompat);
                AccessibilityNodeInfoCompat.CollectionItemInfoCompat collectionItemInfo = accessibilityNodeInfoCompat.getCollectionItemInfo();
                if (collectionItemInfo != null) {
                    accessibilityNodeInfoCompat.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(collectionItemInfo.getRowIndex() - getEmptyRowForAccessibility(itemViewType), collectionItemInfo.getRowSpan(), collectionItemInfo.getColumnIndex(), collectionItemInfo.getColumnSpan(), collectionItemInfo.isHeading(), collectionItemInfo.isSelected()));
                }
            }
        }
    }

    /* loaded from: a.zip:com/android/launcher3/allapps/AllAppsGridAdapter$BindViewCallback.class */
    public interface BindViewCallback {
        void onBindView(ViewHolder viewHolder);
    }

    /* loaded from: a.zip:com/android/launcher3/allapps/AllAppsGridAdapter$GridItemDecoration.class */
    public class GridItemDecoration extends RecyclerView.ItemDecoration {
        private HashMap<String, PointF> mCachedSectionBounds = new HashMap<>();
        private Rect mTmpBounds = new Rect();
        final AllAppsGridAdapter this$0;

        public GridItemDecoration(AllAppsGridAdapter allAppsGridAdapter) {
            this.this$0 = allAppsGridAdapter;
        }

        private PointF getAndCacheSectionBounds(String str) {
            PointF pointF = this.mCachedSectionBounds.get(str);
            PointF pointF2 = pointF;
            if (pointF == null) {
                this.this$0.mSectionTextPaint.getTextBounds(str, 0, str.length(), this.mTmpBounds);
                pointF2 = new PointF(this.this$0.mSectionTextPaint.measureText(str), this.mTmpBounds.height());
                this.mCachedSectionBounds.put(str, pointF2);
            }
            return pointF2;
        }

        private boolean isValidHolderAndChild(ViewHolder viewHolder, View view, List<AlphabeticalAppsList.AdapterItem> list) {
            int position;
            return !((GridLayoutManager.LayoutParams) view.getLayoutParams()).isItemRemoved() && viewHolder != null && (position = viewHolder.getPosition()) >= 0 && position < list.size();
        }

        private boolean shouldDrawItemDivider(ViewHolder viewHolder, List<AlphabeticalAppsList.AdapterItem> list) {
            return list.get(viewHolder.getPosition()).viewType == 2;
        }

        private boolean shouldDrawItemSection(ViewHolder viewHolder, int i, List<AlphabeticalAppsList.AdapterItem> list) {
            int position = viewHolder.getPosition();
            if (list.get(position).viewType != 1) {
                return false;
            }
            return i != 0 ? list.get(position - 1).viewType == 0 : true;
        }

        @Override // android.support.v7.widget.RecyclerView.ItemDecoration
        public void getItemOffsets(Rect rect, View view, RecyclerView recyclerView, RecyclerView.State state) {
        }

        @Override // android.support.v7.widget.RecyclerView.ItemDecoration
        public void onDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
            boolean z;
            int i;
            int i2;
            int i3;
            if (this.this$0.mApps.hasFilter() || this.this$0.mAppsPerRow == 0) {
                return;
            }
            List<AlphabeticalAppsList.AdapterItem> adapterItems = this.this$0.mApps.getAdapterItems();
            boolean z2 = false;
            boolean z3 = this.this$0.mSectionNamesMargin > 0;
            int childCount = recyclerView.getChildCount();
            int i4 = 0;
            int i5 = 0;
            int i6 = 0;
            while (i6 < childCount) {
                View childAt = recyclerView.getChildAt(i6);
                ViewHolder viewHolder = (ViewHolder) recyclerView.getChildViewHolder(childAt);
                if (!isValidHolderAndChild(viewHolder, childAt, adapterItems)) {
                    i3 = i4;
                    i2 = i5;
                    i = i6;
                    z = z2;
                } else if (!shouldDrawItemDivider(viewHolder, adapterItems) || z2) {
                    z = z2;
                    i = i6;
                    i2 = i5;
                    i3 = i4;
                    if (z3) {
                        z = z2;
                        i = i6;
                        i2 = i5;
                        i3 = i4;
                        if (shouldDrawItemSection(viewHolder, i6, adapterItems)) {
                            int paddingTop = childAt.getPaddingTop();
                            int position = viewHolder.getPosition();
                            AlphabeticalAppsList.AdapterItem adapterItem = adapterItems.get(position);
                            AlphabeticalAppsList.SectionInfo sectionInfo = adapterItem.sectionInfo;
                            String str = adapterItem.sectionName;
                            int i7 = adapterItem.sectionAppIndex;
                            while (i7 < sectionInfo.numApps) {
                                AlphabeticalAppsList.AdapterItem adapterItem2 = adapterItems.get(position);
                                String str2 = adapterItem2.sectionName;
                                if (adapterItem2.sectionInfo != sectionInfo) {
                                    break;
                                }
                                if (i7 <= adapterItem.sectionAppIndex || !str2.equals(str)) {
                                    PointF andCacheSectionBounds = getAndCacheSectionBounds(str2);
                                    int i8 = (int) ((paddingTop * 2) + andCacheSectionBounds.y);
                                    int width = this.this$0.mIsRtl ? (recyclerView.getWidth() - this.this$0.mBackgroundPadding.left) - this.this$0.mSectionNamesMargin : this.this$0.mBackgroundPadding.left;
                                    int i9 = (int) ((this.this$0.mSectionNamesMargin - andCacheSectionBounds.x) / 2.0f);
                                    int top = childAt.getTop() + i8;
                                    int i10 = top;
                                    if (!(!str2.equals(adapterItems.get(Math.min(adapterItems.size() - 1, (this.this$0.mAppsPerRow + position) - (adapterItems.get(position).sectionAppIndex % this.this$0.mAppsPerRow))).sectionName))) {
                                        i10 = Math.max(i8, top);
                                    }
                                    int i11 = i10;
                                    if (i5 > 0) {
                                        i11 = i10;
                                        if (i10 <= i4 + i5) {
                                            i11 = i10 + (i4 - i10) + i5;
                                        }
                                    }
                                    canvas.drawText(str2, width + i9, i11, this.this$0.mSectionTextPaint);
                                    i4 = i11;
                                    i5 = (int) (andCacheSectionBounds.y + this.this$0.mSectionHeaderOffset);
                                    str = str2;
                                }
                                i7++;
                                position++;
                            }
                            i = i6 + (sectionInfo.numApps - adapterItem.sectionAppIndex);
                            z = z2;
                            i2 = i5;
                            i3 = i4;
                        }
                    }
                } else {
                    int top2 = childAt.getTop() + childAt.getHeight() + this.this$0.mPredictionBarDividerOffset;
                    canvas.drawLine(this.this$0.mBackgroundPadding.left, top2, recyclerView.getWidth() - this.this$0.mBackgroundPadding.right, top2, this.this$0.mPredictedAppsDividerPaint);
                    z = true;
                    i = i6;
                    i2 = i5;
                    i3 = i4;
                }
                i6 = i + 1;
                z2 = z;
                i5 = i2;
                i4 = i3;
            }
        }
    }

    /* loaded from: a.zip:com/android/launcher3/allapps/AllAppsGridAdapter$GridSpanSizer.class */
    public class GridSpanSizer extends GridLayoutManager.SpanSizeLookup {
        final AllAppsGridAdapter this$0;

        public GridSpanSizer(AllAppsGridAdapter allAppsGridAdapter) {
            this.this$0 = allAppsGridAdapter;
            setSpanIndexCacheEnabled(true);
        }

        @Override // android.support.v7.widget.GridLayoutManager.SpanSizeLookup
        public int getSpanSize(int i) {
            switch (this.this$0.mApps.getAdapterItems().get(i).viewType) {
                case 1:
                case 2:
                    return 1;
                default:
                    return this.this$0.mAppsPerRow;
            }
        }
    }

    /* loaded from: a.zip:com/android/launcher3/allapps/AllAppsGridAdapter$ViewHolder.class */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mContent;

        public ViewHolder(View view) {
            super(view);
            this.mContent = view;
        }
    }

    public AllAppsGridAdapter(Launcher launcher, AlphabeticalAppsList alphabeticalAppsList, View.OnTouchListener onTouchListener, View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener) {
        Resources resources = launcher.getResources();
        this.mLauncher = launcher;
        this.mApps = alphabeticalAppsList;
        this.mEmptySearchMessage = resources.getString(2131558415);
        this.mGridSizer = new GridSpanSizer(this);
        this.mGridLayoutMgr = new AppsGridLayoutManager(this, launcher);
        this.mGridLayoutMgr.setSpanSizeLookup(this.mGridSizer);
        this.mItemDecoration = new GridItemDecoration(this);
        this.mLayoutInflater = LayoutInflater.from(launcher);
        this.mTouchListener = onTouchListener;
        this.mIconClickListener = onClickListener;
        this.mIconLongClickListener = onLongClickListener;
        this.mSectionNamesMargin = resources.getDimensionPixelSize(2131230764);
        this.mSectionHeaderOffset = resources.getDimensionPixelSize(2131230765);
        this.mIsRtl = Utilities.isRtl(resources);
        this.mSectionTextPaint = new Paint(1);
        this.mSectionTextPaint.setTextSize(resources.getDimensionPixelSize(2131230766));
        this.mSectionTextPaint.setColor(resources.getColor(2131361806));
        this.mPredictedAppsDividerPaint = new Paint(1);
        this.mPredictedAppsDividerPaint.setStrokeWidth(Utilities.pxFromDp(1.0f, resources.getDisplayMetrics()));
        this.mPredictedAppsDividerPaint.setColor(503316480);
        this.mPredictionBarDividerOffset = ((-resources.getDimensionPixelSize(2131230772)) + resources.getDimensionPixelSize(2131230769)) / 2;
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mApps.getAdapterItems().size();
    }

    public RecyclerView.ItemDecoration getItemDecoration() {
        return this.mItemDecoration;
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        return this.mApps.getAdapterItems().get(i).viewType;
    }

    public GridLayoutManager getLayoutManager() {
        return this.mGridLayoutMgr;
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        int i2 = 17;
        switch (viewHolder.getItemViewType()) {
            case 1:
                AppInfo appInfo = this.mApps.getAdapterItems().get(i).appInfo;
                BubbleTextView bubbleTextView = (BubbleTextView) viewHolder.mContent;
                bubbleTextView.applyFromApplicationInfo(appInfo);
                bubbleTextView.setAccessibilityDelegate(LauncherAppState.getInstance().getAccessibilityDelegate());
                break;
            case 2:
                AppInfo appInfo2 = this.mApps.getAdapterItems().get(i).appInfo;
                BubbleTextView bubbleTextView2 = (BubbleTextView) viewHolder.mContent;
                bubbleTextView2.applyFromApplicationInfo(appInfo2);
                bubbleTextView2.setAccessibilityDelegate(LauncherAppState.getInstance().getAccessibilityDelegate());
                break;
            case 3:
                TextView textView = (TextView) viewHolder.mContent;
                textView.setText(this.mEmptySearchMessage);
                if (!this.mApps.hasNoFilteredResults()) {
                    i2 = 8388627;
                }
                textView.setGravity(i2);
                break;
            case 5:
                TextView textView2 = (TextView) viewHolder.mContent;
                if (this.mMarketSearchIntent == null) {
                    textView2.setVisibility(8);
                    break;
                } else {
                    textView2.setVisibility(0);
                    textView2.setContentDescription(this.mMarketSearchMessage);
                    if (!this.mApps.hasNoFilteredResults()) {
                        i2 = 8388627;
                    }
                    textView2.setGravity(i2);
                    textView2.setText(this.mMarketSearchMessage);
                    break;
                }
        }
        if (this.mBindViewCallback != null) {
            this.mBindViewCallback.onBindView(viewHolder);
        }
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        switch (i) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                return new ViewHolder(new View(viewGroup.getContext()));
            case 1:
                BubbleTextView bubbleTextView = (BubbleTextView) this.mLayoutInflater.inflate(2130968581, viewGroup, false);
                bubbleTextView.setOnTouchListener(this.mTouchListener);
                bubbleTextView.setOnClickListener(this.mIconClickListener);
                bubbleTextView.setOnLongClickListener(this.mIconLongClickListener);
                ViewConfiguration.get(viewGroup.getContext());
                bubbleTextView.setLongPressTimeout(ViewConfiguration.getLongPressTimeout());
                bubbleTextView.setFocusable(true);
                return new ViewHolder(bubbleTextView);
            case 2:
                BubbleTextView bubbleTextView2 = (BubbleTextView) this.mLayoutInflater.inflate(2130968582, viewGroup, false);
                bubbleTextView2.setOnTouchListener(this.mTouchListener);
                bubbleTextView2.setOnClickListener(this.mIconClickListener);
                bubbleTextView2.setOnLongClickListener(this.mIconLongClickListener);
                ViewConfiguration.get(viewGroup.getContext());
                bubbleTextView2.setLongPressTimeout(ViewConfiguration.getLongPressTimeout());
                bubbleTextView2.setFocusable(true);
                return new ViewHolder(bubbleTextView2);
            case 3:
                return new ViewHolder(this.mLayoutInflater.inflate(2130968580, viewGroup, false));
            case 4:
                return new ViewHolder(this.mLayoutInflater.inflate(2130968585, viewGroup, false));
            case 5:
                View inflate = this.mLayoutInflater.inflate(2130968584, viewGroup, false);
                inflate.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.launcher3.allapps.AllAppsGridAdapter.1
                    final AllAppsGridAdapter this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        this.this$0.mLauncher.startActivitySafely(view, this.this$0.mMarketSearchIntent, null);
                    }
                });
                return new ViewHolder(inflate);
            default:
                throw new RuntimeException("Unexpected view type");
        }
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public boolean onFailedToRecycleView(ViewHolder viewHolder) {
        return true;
    }

    public void setBindViewCallback(BindViewCallback bindViewCallback) {
        this.mBindViewCallback = bindViewCallback;
    }

    public void setLastSearchQuery(String str) {
        Resources resources = this.mLauncher.getResources();
        this.mEmptySearchMessage = String.format(resources.getString(2131558416), str);
        if (this.mMarketAppName != null) {
            this.mMarketSearchMessage = String.format(resources.getString(2131558417), this.mMarketAppName);
            this.mMarketSearchIntent = this.mSearchController.createMarketSearchIntent(str);
        }
    }

    public void setNumAppsPerRow(int i) {
        this.mAppsPerRow = i;
        this.mGridLayoutMgr.setSpanCount(i);
    }

    public void setSearchController(AllAppsSearchBarController allAppsSearchBarController) {
        this.mSearchController = allAppsSearchBarController;
        PackageManager packageManager = this.mLauncher.getPackageManager();
        ResolveInfo resolveActivity = packageManager.resolveActivity(this.mSearchController.createMarketSearchIntent(""), 65536);
        if (resolveActivity != null) {
            this.mMarketAppName = resolveActivity.loadLabel(packageManager).toString();
        }
    }

    public void updateBackgroundPadding(Rect rect) {
        this.mBackgroundPadding.set(rect);
    }
}
