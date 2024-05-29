package com.android.launcher3.allapps;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.android.launcher3.BaseRecyclerViewFastScrollBar;
import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.allapps.AllAppsGridAdapter;
import com.android.launcher3.allapps.AlphabeticalAppsList;
import java.util.HashSet;
/* loaded from: a.zip:com/android/launcher3/allapps/AllAppsFastScrollHelper.class */
public class AllAppsFastScrollHelper implements AllAppsGridAdapter.BindViewCallback {
    private AlphabeticalAppsList mApps;
    String mCurrentFastScrollSection;
    int mFastScrollFrameIndex;
    private boolean mHasFastScrollTouchSettled;
    private boolean mHasFastScrollTouchSettledAtLeastOnce;
    private AllAppsRecyclerView mRv;
    String mTargetFastScrollSection;
    int mTargetFastScrollPosition = -1;
    private HashSet<BaseRecyclerViewFastScrollBar.FastScrollFocusableView> mTrackedFastScrollViews = new HashSet<>();
    final int[] mFastScrollFrames = new int[10];
    Runnable mSmoothSnapNextFrameRunnable = new Runnable(this) { // from class: com.android.launcher3.allapps.AllAppsFastScrollHelper.1
        final AllAppsFastScrollHelper this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.this$0.mFastScrollFrameIndex < this.this$0.mFastScrollFrames.length) {
                this.this$0.mRv.scrollBy(0, this.this$0.mFastScrollFrames[this.this$0.mFastScrollFrameIndex]);
                this.this$0.mFastScrollFrameIndex++;
                this.this$0.mRv.postOnAnimation(this.this$0.mSmoothSnapNextFrameRunnable);
            }
        }
    };
    Runnable mFastScrollToTargetSectionRunnable = new Runnable(this) { // from class: com.android.launcher3.allapps.AllAppsFastScrollHelper.2
        final AllAppsFastScrollHelper this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.mCurrentFastScrollSection = this.this$0.mTargetFastScrollSection;
            this.this$0.mHasFastScrollTouchSettled = true;
            this.this$0.mHasFastScrollTouchSettledAtLeastOnce = true;
            this.this$0.updateTrackedViewsFastScrollFocusState();
        }
    };

    public AllAppsFastScrollHelper(AllAppsRecyclerView allAppsRecyclerView, AlphabeticalAppsList alphabeticalAppsList) {
        this.mRv = allAppsRecyclerView;
        this.mApps = alphabeticalAppsList;
    }

    private void smoothSnapToPosition(int i, int i2, AlphabeticalAppsList.FastScrollSectionInfo fastScrollSectionInfo) {
        this.mRv.removeCallbacks(this.mSmoothSnapNextFrameRunnable);
        this.mRv.removeCallbacks(this.mFastScrollToTargetSectionRunnable);
        trackAllChildViews();
        if (this.mHasFastScrollTouchSettled) {
            this.mCurrentFastScrollSection = fastScrollSectionInfo.sectionName;
            this.mTargetFastScrollSection = null;
            updateTrackedViewsFastScrollFocusState();
        } else {
            this.mCurrentFastScrollSection = null;
            this.mTargetFastScrollSection = fastScrollSectionInfo.sectionName;
            this.mHasFastScrollTouchSettled = false;
            updateTrackedViewsFastScrollFocusState();
            this.mRv.postDelayed(this.mFastScrollToTargetSectionRunnable, this.mHasFastScrollTouchSettledAtLeastOnce ? 200 : 100);
        }
        int min = Math.min(i2, this.mRv.getPaddingTop() + this.mRv.getTop(fastScrollSectionInfo.fastScrollToItem.rowIndex));
        int length = this.mFastScrollFrames.length;
        for (int i3 = 0; i3 < length; i3++) {
            this.mFastScrollFrames[i3] = (min - i) / length;
        }
        this.mFastScrollFrameIndex = 0;
        this.mRv.postOnAnimation(this.mSmoothSnapNextFrameRunnable);
    }

    private void trackAllChildViews() {
        int childCount = this.mRv.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = this.mRv.getChildAt(i);
            if (childAt instanceof BaseRecyclerViewFastScrollBar.FastScrollFocusableView) {
                this.mTrackedFastScrollViews.add((BaseRecyclerViewFastScrollBar.FastScrollFocusableView) childAt);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTrackedViewsFastScrollFocusState() {
        for (BaseRecyclerViewFastScrollBar.FastScrollFocusableView fastScrollFocusableView : this.mTrackedFastScrollViews) {
            RecyclerView.ViewHolder childViewHolder = this.mRv.getChildViewHolder((View) fastScrollFocusableView);
            updateViewFastScrollFocusState(fastScrollFocusableView, childViewHolder != null ? childViewHolder.getPosition() : -1, true);
        }
    }

    private void updateViewFastScrollFocusState(BaseRecyclerViewFastScrollBar.FastScrollFocusableView fastScrollFocusableView, int i, boolean z) {
        FastBitmapDrawable.State state = FastBitmapDrawable.State.NORMAL;
        FastBitmapDrawable.State state2 = state;
        if (this.mCurrentFastScrollSection != null) {
            state2 = state;
            if (i > -1) {
                AlphabeticalAppsList.AdapterItem adapterItem = this.mApps.getAdapterItems().get(i);
                boolean z2 = false;
                if (adapterItem.sectionName.equals(this.mCurrentFastScrollSection)) {
                    z2 = false;
                    if (adapterItem.position == this.mTargetFastScrollPosition) {
                        z2 = true;
                    }
                }
                state2 = z2 ? FastBitmapDrawable.State.FAST_SCROLL_HIGHLIGHTED : FastBitmapDrawable.State.FAST_SCROLL_UNHIGHLIGHTED;
            }
        }
        fastScrollFocusableView.setFastScrollFocusState(state2, z);
    }

    @Override // com.android.launcher3.allapps.AllAppsGridAdapter.BindViewCallback
    public void onBindView(AllAppsGridAdapter.ViewHolder viewHolder) {
        if (!(this.mCurrentFastScrollSection == null && this.mTargetFastScrollSection == null) && (viewHolder.mContent instanceof BaseRecyclerViewFastScrollBar.FastScrollFocusableView)) {
            BaseRecyclerViewFastScrollBar.FastScrollFocusableView fastScrollFocusableView = (BaseRecyclerViewFastScrollBar.FastScrollFocusableView) viewHolder.mContent;
            updateViewFastScrollFocusState(fastScrollFocusableView, viewHolder.getPosition(), false);
            this.mTrackedFastScrollViews.add(fastScrollFocusableView);
        }
    }

    public void onFastScrollCompleted() {
        this.mRv.removeCallbacks(this.mSmoothSnapNextFrameRunnable);
        this.mRv.removeCallbacks(this.mFastScrollToTargetSectionRunnable);
        this.mHasFastScrollTouchSettled = false;
        this.mHasFastScrollTouchSettledAtLeastOnce = false;
        this.mCurrentFastScrollSection = null;
        this.mTargetFastScrollSection = null;
        this.mTargetFastScrollPosition = -1;
        updateTrackedViewsFastScrollFocusState();
        this.mTrackedFastScrollViews.clear();
    }

    public void onSetAdapter(AllAppsGridAdapter allAppsGridAdapter) {
        allAppsGridAdapter.setBindViewCallback(this);
    }

    public boolean smoothScrollToSection(int i, int i2, AlphabeticalAppsList.FastScrollSectionInfo fastScrollSectionInfo) {
        if (this.mTargetFastScrollPosition != fastScrollSectionInfo.fastScrollToItem.position) {
            this.mTargetFastScrollPosition = fastScrollSectionInfo.fastScrollToItem.position;
            smoothSnapToPosition(i, i2, fastScrollSectionInfo);
            return true;
        }
        return false;
    }
}
