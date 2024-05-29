package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.qs.QSPanel;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/qs/PagedTileLayout.class */
public class PagedTileLayout extends ViewPager implements QSPanel.QSTileLayout {
    private final PagerAdapter mAdapter;
    private View mDecorGroup;
    private final Runnable mDistribute;
    private boolean mListening;
    private int mNumPages;
    private boolean mOffPage;
    private PageIndicator mPageIndicator;
    private PageListener mPageListener;
    private final ArrayList<TilePage> mPages;
    private int mPosition;
    private final ArrayList<QSPanel.TileRecord> mTiles;

    /* loaded from: a.zip:com/android/systemui/qs/PagedTileLayout$PageListener.class */
    public interface PageListener {
        void onPageChanged(boolean z);
    }

    /* loaded from: a.zip:com/android/systemui/qs/PagedTileLayout$TilePage.class */
    public static class TilePage extends TileLayout {
        private int mMaxRows;

        public TilePage(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.mMaxRows = 3;
            updateResources();
            setContentDescription(this.mContext.getString(2131493444));
        }

        private int getRows() {
            Resources resources = getContext().getResources();
            if (resources.getConfiguration().orientation == 1) {
                return 3;
            }
            return Math.max(1, resources.getInteger(2131755053));
        }

        public boolean isFull() {
            return this.mRecords.size() >= this.mColumns * this.mMaxRows;
        }

        @Override // com.android.systemui.qs.TileLayout, com.android.systemui.qs.QSPanel.QSTileLayout
        public boolean updateResources() {
            int rows = getRows();
            boolean z = rows != this.mMaxRows;
            if (z) {
                this.mMaxRows = rows;
                requestLayout();
            }
            if (super.updateResources()) {
                z = true;
            }
            return z;
        }
    }

    public PagedTileLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTiles = new ArrayList<>();
        this.mPages = new ArrayList<>();
        this.mDistribute = new Runnable(this) { // from class: com.android.systemui.qs.PagedTileLayout.1
            final PagedTileLayout this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.distributeTiles();
            }
        };
        this.mAdapter = new PagerAdapter(this) { // from class: com.android.systemui.qs.PagedTileLayout.2
            final PagedTileLayout this$0;

            {
                this.this$0 = this;
            }

            @Override // android.support.v4.view.PagerAdapter
            public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
                viewGroup.removeView((View) obj);
            }

            @Override // android.support.v4.view.PagerAdapter
            public int getCount() {
                return this.this$0.mNumPages;
            }

            @Override // android.support.v4.view.PagerAdapter
            public Object instantiateItem(ViewGroup viewGroup, int i) {
                int i2 = i;
                if (this.this$0.isLayoutRtl()) {
                    i2 = (this.this$0.mPages.size() - 1) - i;
                }
                ViewGroup viewGroup2 = (ViewGroup) this.this$0.mPages.get(i2);
                viewGroup.addView(viewGroup2);
                return viewGroup2;
            }

            @Override // android.support.v4.view.PagerAdapter
            public boolean isViewFromObject(View view, Object obj) {
                return view == obj;
            }
        };
        setAdapter(this.mAdapter);
        setOnPageChangeListener(new ViewPager.OnPageChangeListener(this) { // from class: com.android.systemui.qs.PagedTileLayout.3
            final PagedTileLayout this$0;

            {
                this.this$0 = this;
            }

            @Override // android.support.v4.view.ViewPager.OnPageChangeListener
            public void onPageScrollStateChanged(int i) {
            }

            /* JADX WARN: Code restructure failed: missing block: B:18:0x0084, code lost:
                if (r5 == (r4.this$0.mPages.size() - 1)) goto L19;
             */
            /* JADX WARN: Code restructure failed: missing block: B:19:0x0087, code lost:
                r11 = true;
             */
            /* JADX WARN: Code restructure failed: missing block: B:25:0x00a5, code lost:
                if (r5 == 0) goto L19;
             */
            @Override // android.support.v4.view.ViewPager.OnPageChangeListener
            /*
                Code decompiled incorrectly, please refer to instructions dump.
            */
            public void onPageScrolled(int i, float f, int i2) {
                if (this.this$0.mPageIndicator == null) {
                    return;
                }
                this.this$0.setCurrentPage(this.this$0.isLayoutRtl() ? (this.this$0.mPages.size() - 1) - i : i, f != 0.0f);
                this.this$0.mPageIndicator.setLocation(i + f);
                if (this.this$0.mPageListener != null) {
                    PageListener pageListener = this.this$0.mPageListener;
                    boolean z = false;
                    if (i2 == 0) {
                        z = this.this$0.isLayoutRtl() ? false : false;
                    }
                    pageListener.onPageChanged(z);
                }
            }

            @Override // android.support.v4.view.ViewPager.OnPageChangeListener
            public void onPageSelected(int i) {
                boolean z = true;
                if (this.this$0.mPageIndicator == null || this.this$0.mPageListener == null) {
                    return;
                }
                PageListener pageListener = this.this$0.mPageListener;
                if (this.this$0.isLayoutRtl()) {
                    if (i != this.this$0.mPages.size() - 1) {
                        z = false;
                    }
                } else if (i != 0) {
                    z = false;
                }
                pageListener.onPageChanged(z);
            }
        });
        setCurrentItem(0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void distributeTiles() {
        int size = this.mPages.size();
        for (int i = 0; i < size; i++) {
            this.mPages.get(i).removeAllViews();
        }
        int i2 = 0;
        int size2 = this.mTiles.size();
        int i3 = 0;
        while (i3 < size2) {
            QSPanel.TileRecord tileRecord = this.mTiles.get(i3);
            int i4 = i2;
            if (this.mPages.get(i2).isFull()) {
                int i5 = i2 + 1;
                i4 = i5;
                if (i5 == this.mPages.size()) {
                    this.mPages.add((TilePage) LayoutInflater.from(this.mContext).inflate(2130968763, (ViewGroup) this, false));
                    i4 = i5;
                }
            }
            this.mPages.get(i4).addTile(tileRecord);
            i3++;
            i2 = i4;
        }
        if (this.mNumPages != i2 + 1) {
            this.mNumPages = i2 + 1;
            while (this.mPages.size() > this.mNumPages) {
                this.mPages.remove(this.mPages.size() - 1);
            }
            this.mPageIndicator.setNumPages(this.mNumPages);
            setAdapter(this.mAdapter);
            this.mAdapter.notifyDataSetChanged();
            setCurrentItem(0, false);
        }
    }

    private void postDistributeTiles() {
        removeCallbacks(this.mDistribute);
        post(this.mDistribute);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setCurrentPage(int i, boolean z) {
        if (this.mPosition == i && this.mOffPage == z) {
            return;
        }
        if (this.mListening) {
            if (this.mPosition != i) {
                setPageListening(this.mPosition, false);
                if (this.mOffPage) {
                    setPageListening(this.mPosition + 1, false);
                }
                setPageListening(i, true);
                if (z) {
                    setPageListening(i + 1, true);
                }
            } else if (this.mOffPage != z) {
                setPageListening(this.mPosition + 1, z);
            }
        }
        this.mPosition = i;
        this.mOffPage = z;
    }

    private void setPageListening(int i, boolean z) {
        if (i >= this.mPages.size()) {
            return;
        }
        this.mPages.get(i).setListening(z);
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void addTile(QSPanel.TileRecord tileRecord) {
        this.mTiles.add(tileRecord);
        postDistributeTiles();
    }

    public int getColumnCount() {
        if (this.mPages.size() == 0) {
            return 0;
        }
        return this.mPages.get(0).mColumns;
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public int getOffsetTop(QSPanel.TileRecord tileRecord) {
        ViewGroup viewGroup = (ViewGroup) tileRecord.tileView.getParent();
        if (viewGroup == null) {
            return 0;
        }
        return viewGroup.getTop() + getTop();
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mPageIndicator = (PageIndicator) findViewById(2131886433);
        this.mDecorGroup = findViewById(2131886584);
        ((ViewPager.LayoutParams) this.mDecorGroup.getLayoutParams()).isDecor = true;
        this.mPages.add((TilePage) LayoutInflater.from(this.mContext).inflate(2130968763, (ViewGroup) this, false));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v4.view.ViewPager, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int i3 = 0;
        int childCount = getChildCount();
        int i4 = 0;
        while (i4 < childCount) {
            int measuredHeight = getChildAt(i4).getMeasuredHeight();
            int i5 = i3;
            if (measuredHeight > i3) {
                i5 = measuredHeight;
            }
            i4++;
            i3 = i5;
        }
        setMeasuredDimension(getMeasuredWidth(), this.mDecorGroup.getMeasuredHeight() + i3);
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        setAdapter(this.mAdapter);
        setCurrentItem(0, false);
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void removeTile(QSPanel.TileRecord tileRecord) {
        if (this.mTiles.remove(tileRecord)) {
            postDistributeTiles();
        }
    }

    @Override // android.support.v4.view.ViewPager
    public void setCurrentItem(int i, boolean z) {
        int i2 = i;
        if (isLayoutRtl()) {
            i2 = (this.mPages.size() - 1) - i;
        }
        super.setCurrentItem(i2, z);
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void setListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        if (this.mListening) {
            this.mPages.get(this.mPosition).setListening(z);
            if (this.mOffPage) {
                this.mPages.get(this.mPosition + 1).setListening(z);
                return;
            }
            return;
        }
        for (int i = 0; i < this.mPages.size(); i++) {
            this.mPages.get(i).setListening(false);
        }
    }

    public void setPageListener(PageListener pageListener) {
        this.mPageListener = pageListener;
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public boolean updateResources() {
        boolean z = false;
        for (int i = 0; i < this.mPages.size(); i++) {
            z |= this.mPages.get(i).updateResources();
        }
        if (z) {
            distributeTiles();
        }
        return z;
    }
}
