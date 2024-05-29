package androidx.slice.widget;

import android.app.PendingIntent;
import android.app.slice.SliceMetrics;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.os.BuildCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import androidx.slice.Slice;
import androidx.slice.SliceItem;
import androidx.slice.SliceMetadata;
import androidx.slice.core.SliceAction;
import androidx.slice.core.SliceActionImpl;
import androidx.slice.core.SliceQuery;
import androidx.slice.view.R;
import java.util.List;
/* loaded from: classes.dex */
public class SliceView extends ViewGroup implements Observer<Slice>, View.OnClickListener {
    private ActionRow mActionRow;
    private int mActionRowHeight;
    private List<SliceAction> mActions;
    private AttributeSet mAttrs;
    int[] mClickInfo;
    private Slice mCurrentSlice;
    private boolean mCurrentSliceLoggedVisible;
    private SliceMetrics mCurrentSliceMetrics;
    private SliceChildView mCurrentView;
    private int mDefStyleAttr;
    private int mDefStyleRes;
    private int mDownX;
    private int mDownY;
    private Handler mHandler;
    private boolean mInLongpress;
    private boolean mIsScrollable;
    private int mLargeHeight;
    private ListContent mListContent;
    private View.OnLongClickListener mLongClickListener;
    Runnable mLongpressCheck;
    private int mMode;
    private View.OnClickListener mOnClickListener;
    private boolean mPressing;
    private int mShortcutSize;
    private boolean mShowActions;
    private boolean mShowLastUpdated;
    private OnSliceActionListener mSliceObserver;
    private int mThemeTintColor;
    private int mTouchSlopSquared;

    /* loaded from: classes.dex */
    public interface OnSliceActionListener {
        void onSliceAction(EventInfo eventInfo, SliceItem sliceItem);
    }

    public SliceView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sliceViewStyle);
    }

    public SliceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mMode = 2;
        this.mShowActions = false;
        this.mIsScrollable = true;
        this.mShowLastUpdated = true;
        this.mCurrentSliceLoggedVisible = false;
        this.mThemeTintColor = -1;
        this.mLongpressCheck = new Runnable() { // from class: androidx.slice.widget.SliceView.1
            @Override // java.lang.Runnable
            public void run() {
                if (SliceView.this.mPressing && SliceView.this.mLongClickListener != null) {
                    SliceView.this.mInLongpress = true;
                    SliceView.this.mLongClickListener.onLongClick(SliceView.this);
                    SliceView.this.performHapticFeedback(0);
                }
            }
        };
        init(context, attrs, defStyleAttr, R.style.Widget_SliceView);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mAttrs = attrs;
        this.mDefStyleAttr = defStyleAttr;
        this.mDefStyleRes = defStyleRes;
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SliceView, defStyleAttr, defStyleRes);
        try {
            this.mThemeTintColor = a.getColor(R.styleable.SliceView_tintColor, -1);
            a.recycle();
            this.mShortcutSize = getContext().getResources().getDimensionPixelSize(R.dimen.abc_slice_shortcut_size);
            this.mLargeHeight = getResources().getDimensionPixelSize(R.dimen.abc_slice_large_height);
            this.mActionRowHeight = getResources().getDimensionPixelSize(R.dimen.abc_slice_action_row_height);
            this.mCurrentView = new LargeTemplateView(getContext());
            this.mCurrentView.setMode(getMode());
            addView(this.mCurrentView, getChildLp(this.mCurrentView));
            applyConfigurations();
            this.mActionRow = new ActionRow(getContext(), true);
            this.mActionRow.setBackground(new ColorDrawable(-1118482));
            addView(this.mActionRow, getChildLp(this.mActionRow));
            updateActions();
            int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            this.mTouchSlopSquared = slop * slop;
            this.mHandler = new Handler();
            super.setOnClickListener(this);
        } catch (Throwable th) {
            a.recycle();
            throw th;
        }
    }

    public boolean isSliceViewClickable() {
        return (this.mOnClickListener == null && (this.mListContent == null || this.mListContent.getPrimaryAction() == null)) ? false : true;
    }

    public void setClickInfo(int[] info) {
        this.mClickInfo = info;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        if (this.mListContent != null && this.mListContent.getPrimaryAction() != null) {
            try {
                SliceActionImpl sa = new SliceActionImpl(this.mListContent.getPrimaryAction());
                sa.getAction().send();
                if (this.mSliceObserver != null && this.mClickInfo != null && this.mClickInfo.length > 1) {
                    EventInfo eventInfo = new EventInfo(getMode(), 3, this.mClickInfo[0], this.mClickInfo[1]);
                    SliceItem sliceItem = this.mListContent.getPrimaryAction();
                    this.mSliceObserver.onSliceAction(eventInfo, sliceItem);
                    logSliceMetricsOnTouch(sliceItem, eventInfo);
                }
            } catch (PendingIntent.CanceledException e) {
                Log.e("SliceView", "PendingIntent for slice cannot be sent", e);
            }
        } else if (this.mOnClickListener != null) {
            this.mOnClickListener.onClick(this);
        }
    }

    @Override // android.view.View
    public void setOnClickListener(View.OnClickListener listener) {
        this.mOnClickListener = listener;
    }

    @Override // android.view.View
    public void setOnLongClickListener(View.OnLongClickListener listener) {
        super.setOnLongClickListener(listener);
        this.mLongClickListener = listener;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean ret = super.onInterceptTouchEvent(ev);
        if (this.mLongClickListener != null) {
            return handleTouchForLongpress(ev);
        }
        return ret;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        boolean ret = super.onTouchEvent(ev);
        if (this.mLongClickListener != null) {
            return handleTouchForLongpress(ev);
        }
        return ret;
    }

    private boolean handleTouchForLongpress(MotionEvent ev) {
        int action = ev.getActionMasked();
        switch (action) {
            case 0:
                this.mHandler.removeCallbacks(this.mLongpressCheck);
                this.mDownX = (int) ev.getRawX();
                this.mDownY = (int) ev.getRawY();
                this.mPressing = true;
                this.mInLongpress = false;
                this.mHandler.postDelayed(this.mLongpressCheck, ViewConfiguration.getLongPressTimeout());
                break;
            case 1:
            case 3:
                this.mPressing = false;
                this.mInLongpress = false;
                this.mHandler.removeCallbacks(this.mLongpressCheck);
                break;
            case 2:
                int deltaX = ((int) ev.getRawX()) - this.mDownX;
                int deltaY = ((int) ev.getRawY()) - this.mDownY;
                int distance = (deltaX * deltaX) + (deltaY * deltaY);
                if (distance > this.mTouchSlopSquared) {
                    this.mPressing = false;
                    this.mHandler.removeCallbacks(this.mLongpressCheck);
                    break;
                }
                break;
        }
        return this.mInLongpress;
    }

    private int getHeightForMode(int maxHeight) {
        if (this.mListContent == null || !this.mListContent.isValid()) {
            return 0;
        }
        int mode = getMode();
        if (mode == 3) {
            return this.mShortcutSize;
        }
        if (mode == 2) {
            return this.mListContent.getLargeHeight(maxHeight, this.mIsScrollable);
        }
        return this.mListContent.getSmallHeight();
    }

    @Override // android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int childWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        if (3 == this.mMode) {
            childWidth = this.mShortcutSize;
            width = this.mShortcutSize + getPaddingLeft() + getPaddingRight();
        }
        int actionHeight = this.mActionRow.getVisibility() != 8 ? this.mActionRowHeight : 0;
        int heightAvailable = View.MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        ViewGroup.LayoutParams lp = getLayoutParams();
        int maxHeight = ((lp == null || lp.height != -2) && heightMode != 0) ? heightAvailable : -1;
        int sliceHeight = getHeightForMode(maxHeight);
        int height = (heightAvailable - getPaddingTop()) - getPaddingBottom();
        if (heightAvailable >= sliceHeight + actionHeight || heightMode == 0) {
            if (heightMode == 1073741824) {
                height = Math.min(sliceHeight, height);
            } else {
                height = sliceHeight;
            }
        } else if (getMode() == 2 && heightAvailable >= this.mLargeHeight + actionHeight) {
            height = sliceHeight;
        } else if (getMode() == 3) {
            height = this.mShortcutSize;
        }
        int childHeight = getPaddingTop() + height + getPaddingBottom();
        int childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(childWidth, 1073741824);
        int childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(childHeight, 1073741824);
        measureChild(this.mCurrentView, childWidthMeasureSpec, childHeightMeasureSpec);
        int actionPaddedHeight = getPaddingTop() + actionHeight + getPaddingBottom();
        int actionHeightSpec = View.MeasureSpec.makeMeasureSpec(actionPaddedHeight, 1073741824);
        measureChild(this.mActionRow, childWidthMeasureSpec, actionHeightSpec);
        setMeasuredDimension(width, height + getPaddingTop() + actionHeight + getPaddingBottom());
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View v = this.mCurrentView;
        int left = getPaddingLeft();
        int top = getPaddingTop();
        v.layout(left, top, v.getMeasuredWidth() + left, v.getMeasuredHeight() + top);
        if (this.mActionRow.getVisibility() != 8) {
            this.mActionRow.layout(left, v.getMeasuredHeight() + top, this.mActionRow.getMeasuredWidth() + left, v.getMeasuredHeight() + top + this.mActionRow.getMeasuredHeight());
        }
    }

    @Override // android.arch.lifecycle.Observer
    public void onChanged(Slice slice) {
        setSlice(slice);
    }

    public void setSlice(Slice slice) {
        initSliceMetrics(slice);
        if (slice != null && (this.mCurrentSlice == null || !this.mCurrentSlice.getUri().equals(slice.getUri()))) {
            this.mCurrentView.resetView();
        }
        this.mCurrentSlice = slice;
        this.mListContent = new ListContent(getContext(), this.mCurrentSlice, this.mAttrs, this.mDefStyleAttr, this.mDefStyleRes);
        if (!this.mListContent.isValid()) {
            this.mActions = null;
            this.mCurrentView.resetView();
            updateActions();
            return;
        }
        this.mActions = this.mListContent.getSliceActions();
        SliceMetadata sliceMetadata = SliceMetadata.from(getContext(), this.mCurrentSlice);
        long lastUpdated = sliceMetadata.getLastUpdatedTime();
        long expiry = sliceMetadata.getExpiry();
        long now = System.currentTimeMillis();
        this.mCurrentView.setLastUpdated(lastUpdated);
        boolean z = false;
        boolean expired = (expiry == 0 || expiry == -1 || now <= expiry) ? false : true;
        SliceChildView sliceChildView = this.mCurrentView;
        if (this.mShowLastUpdated && expired) {
            z = true;
        }
        sliceChildView.setShowLastUpdated(z);
        this.mCurrentView.setTint(getTintColor());
        if (this.mListContent.getLayoutDirItem() != null) {
            this.mCurrentView.setLayoutDirection(this.mListContent.getLayoutDirItem().getInt());
        } else {
            this.mCurrentView.setLayoutDirection(2);
        }
        this.mCurrentView.setSliceContent(this.mListContent);
        updateActions();
        logSliceMetricsVisibilityChange(true);
    }

    public int getMode() {
        return this.mMode;
    }

    private void applyConfigurations() {
        this.mCurrentView.setSliceActionListener(this.mSliceObserver);
        if (this.mCurrentView instanceof LargeTemplateView) {
            ((LargeTemplateView) this.mCurrentView).setScrollable(this.mIsScrollable);
        }
        this.mCurrentView.setStyle(this.mAttrs, this.mDefStyleAttr, this.mDefStyleRes);
        this.mCurrentView.setTint(getTintColor());
        if (this.mListContent != null && this.mListContent.getLayoutDirItem() != null) {
            this.mCurrentView.setLayoutDirection(this.mListContent.getLayoutDirItem().getInt());
        } else {
            this.mCurrentView.setLayoutDirection(2);
        }
    }

    private void updateActions() {
        if (this.mActions == null || this.mActions.isEmpty()) {
            this.mActionRow.setVisibility(8);
            this.mCurrentView.setSliceActions(null);
        } else if (this.mShowActions && this.mMode != 3 && this.mActions.size() >= 2) {
            this.mActionRow.setActions(this.mActions, getTintColor());
            this.mActionRow.setVisibility(0);
            this.mCurrentView.setSliceActions(null);
        } else {
            this.mCurrentView.setSliceActions(this.mActions);
            this.mActionRow.setVisibility(8);
        }
    }

    private int getTintColor() {
        if (this.mThemeTintColor != -1) {
            return this.mThemeTintColor;
        }
        SliceItem colorItem = SliceQuery.findSubtype(this.mCurrentSlice, "int", "color");
        if (colorItem != null) {
            return colorItem.getInt();
        }
        return SliceViewUtil.getColorAccent(getContext());
    }

    private ViewGroup.LayoutParams getChildLp(View child) {
        if (child instanceof ShortcutView) {
            return new ViewGroup.LayoutParams(this.mShortcutSize, this.mShortcutSize);
        }
        return new ViewGroup.LayoutParams(-1, -1);
    }

    public static String modeToString(int mode) {
        switch (mode) {
            case 1:
                return "MODE SMALL";
            case 2:
                return "MODE LARGE";
            case 3:
                return "MODE SHORTCUT";
            default:
                return "unknown mode: " + mode;
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isShown()) {
            logSliceMetricsVisibilityChange(true);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        logSliceMetricsVisibilityChange(false);
    }

    @Override // android.view.View
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (isAttachedToWindow()) {
            logSliceMetricsVisibilityChange(visibility == 0);
        }
    }

    @Override // android.view.View
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        logSliceMetricsVisibilityChange(visibility == 0);
    }

    private void initSliceMetrics(Slice slice) {
        if (BuildCompat.isAtLeastP()) {
            if (slice == null || slice.getUri() == null) {
                logSliceMetricsVisibilityChange(false);
                this.mCurrentSliceMetrics = null;
            } else if (this.mCurrentSlice == null || !this.mCurrentSlice.getUri().equals(slice.getUri())) {
                logSliceMetricsVisibilityChange(false);
                this.mCurrentSliceMetrics = new SliceMetrics(getContext(), slice.getUri());
            }
        }
    }

    private void logSliceMetricsVisibilityChange(boolean visibility) {
        if (BuildCompat.isAtLeastP() && this.mCurrentSliceMetrics != null) {
            if (visibility && !this.mCurrentSliceLoggedVisible) {
                this.mCurrentSliceMetrics.logVisible();
                this.mCurrentSliceLoggedVisible = true;
            }
            if (!visibility && this.mCurrentSliceLoggedVisible) {
                this.mCurrentSliceMetrics.logHidden();
                this.mCurrentSliceLoggedVisible = false;
            }
        }
    }

    private void logSliceMetricsOnTouch(SliceItem item, EventInfo info) {
        if (BuildCompat.isAtLeastP() && this.mCurrentSliceMetrics != null && item.getSlice() != null && item.getSlice().getUri() != null) {
            this.mCurrentSliceMetrics.logTouch(info.actionType, this.mListContent.getPrimaryAction().getSlice().getUri());
        }
    }
}
