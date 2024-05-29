package android.support.v17.leanback.widget.picker;

import android.content.Context;
import android.graphics.Rect;
import android.support.v17.leanback.R$dimen;
import android.support.v17.leanback.R$id;
import android.support.v17.leanback.R$layout;
import android.support.v17.leanback.widget.OnChildViewHolderSelectedListener;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:android/support/v17/leanback/widget/picker/Picker.class */
public class Picker extends FrameLayout {
    private Interpolator mAccelerateInterpolator;
    private int mAlphaAnimDuration;
    private final OnChildViewHolderSelectedListener mColumnChangeListener;
    private final List<VerticalGridView> mColumnViews;
    private ArrayList<PickerColumn> mColumns;
    private Interpolator mDecelerateInterpolator;
    private float mFocusedAlpha;
    private float mInvisibleColumnAlpha;
    private ArrayList<PickerValueListener> mListeners;
    private int mPickerItemLayoutId;
    private int mPickerItemTextViewId;
    private ViewGroup mPickerView;
    private ViewGroup mRootView;
    private int mSelectedColumn;
    private CharSequence mSeparator;
    private float mUnfocusedAlpha;
    private float mVisibleColumnAlpha;
    private float mVisibleItems;
    private float mVisibleItemsActivated;

    /* loaded from: a.zip:android/support/v17/leanback/widget/picker/Picker$PickerScrollArrayAdapter.class */
    class PickerScrollArrayAdapter extends RecyclerView.Adapter<ViewHolder> {
        private final int mColIndex;
        private PickerColumn mData;
        private final int mResource;
        private final int mTextViewResourceId;
        final Picker this$0;

        PickerScrollArrayAdapter(Picker picker, Context context, int i, int i2, int i3) {
            this.this$0 = picker;
            this.mResource = i;
            this.mColIndex = i3;
            this.mTextViewResourceId = i2;
            this.mData = (PickerColumn) picker.mColumns.get(this.mColIndex);
        }

        @Override // android.support.v7.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.mData == null ? 0 : this.mData.getCount();
        }

        @Override // android.support.v7.widget.RecyclerView.Adapter
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            if (viewHolder.textView != null && this.mData != null) {
                viewHolder.textView.setText(this.mData.getLabelFor(this.mData.getMinValue() + i));
            }
            this.this$0.setOrAnimateAlpha(viewHolder.itemView, ((VerticalGridView) this.this$0.mColumnViews.get(this.mColIndex)).getSelectedPosition() == i, this.mColIndex, false);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.support.v7.widget.RecyclerView.Adapter
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(this.mResource, viewGroup, false);
            return new ViewHolder(inflate, this.mTextViewResourceId != 0 ? (TextView) inflate.findViewById(this.mTextViewResourceId) : (TextView) inflate);
        }

        @Override // android.support.v7.widget.RecyclerView.Adapter
        public void onViewAttachedToWindow(ViewHolder viewHolder) {
            viewHolder.itemView.setFocusable(this.this$0.isActivated());
        }
    }

    /* loaded from: a.zip:android/support/v17/leanback/widget/picker/Picker$PickerValueListener.class */
    public interface PickerValueListener {
        void onValueChanged(Picker picker, int i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:android/support/v17/leanback/widget/picker/Picker$ViewHolder.class */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;

        ViewHolder(View view, TextView textView) {
            super(view);
            this.textView = textView;
        }
    }

    public Picker(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mColumnViews = new ArrayList();
        this.mVisibleItemsActivated = 3.0f;
        this.mVisibleItems = 1.0f;
        this.mSelectedColumn = 0;
        this.mPickerItemLayoutId = R$layout.lb_picker_item;
        this.mPickerItemTextViewId = 0;
        this.mColumnChangeListener = new OnChildViewHolderSelectedListener(this) { // from class: android.support.v17.leanback.widget.picker.Picker.1
            final Picker this$0;

            {
                this.this$0 = this;
            }

            @Override // android.support.v17.leanback.widget.OnChildViewHolderSelectedListener
            public void onChildViewHolderSelected(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int i2, int i3) {
                PickerScrollArrayAdapter pickerScrollArrayAdapter = (PickerScrollArrayAdapter) recyclerView.getAdapter();
                int indexOf = this.this$0.mColumnViews.indexOf(recyclerView);
                this.this$0.updateColumnAlpha(indexOf, true);
                if (viewHolder != null) {
                    this.this$0.onColumnValueChanged(indexOf, ((PickerColumn) this.this$0.mColumns.get(indexOf)).getMinValue() + i2);
                }
            }
        };
        setEnabled(true);
        this.mFocusedAlpha = 1.0f;
        this.mUnfocusedAlpha = 1.0f;
        this.mVisibleColumnAlpha = 0.5f;
        this.mInvisibleColumnAlpha = 0.0f;
        this.mAlphaAnimDuration = 200;
        this.mDecelerateInterpolator = new DecelerateInterpolator(2.5f);
        this.mAccelerateInterpolator = new AccelerateInterpolator(2.5f);
        this.mRootView = (ViewGroup) LayoutInflater.from(getContext()).inflate(R$layout.lb_picker, (ViewGroup) this, true);
        this.mPickerView = (ViewGroup) this.mRootView.findViewById(R$id.picker);
    }

    private void notifyValueChanged(int i) {
        if (this.mListeners != null) {
            for (int size = this.mListeners.size() - 1; size >= 0; size--) {
                this.mListeners.get(size).onValueChanged(this, i);
            }
        }
    }

    private void setOrAnimateAlpha(View view, boolean z, float f, float f2, Interpolator interpolator) {
        view.animate().cancel();
        if (!z) {
            view.setAlpha(f);
            return;
        }
        if (f2 >= 0.0f) {
            view.setAlpha(f2);
        }
        view.animate().alpha(f).setDuration(this.mAlphaAnimDuration).setInterpolator(interpolator).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setOrAnimateAlpha(View view, boolean z, int i, boolean z2) {
        boolean z3 = i == this.mSelectedColumn || !hasFocus();
        if (z) {
            if (z3) {
                setOrAnimateAlpha(view, z2, this.mFocusedAlpha, -1.0f, this.mDecelerateInterpolator);
            } else {
                setOrAnimateAlpha(view, z2, this.mUnfocusedAlpha, -1.0f, this.mDecelerateInterpolator);
            }
        } else if (z3) {
            setOrAnimateAlpha(view, z2, this.mVisibleColumnAlpha, -1.0f, this.mDecelerateInterpolator);
        } else {
            setOrAnimateAlpha(view, z2, this.mInvisibleColumnAlpha, -1.0f, this.mDecelerateInterpolator);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateColumnAlpha(int i, boolean z) {
        VerticalGridView verticalGridView = this.mColumnViews.get(i);
        int selectedPosition = verticalGridView.getSelectedPosition();
        int i2 = 0;
        while (i2 < verticalGridView.getAdapter().getItemCount()) {
            View findViewByPosition = verticalGridView.getLayoutManager().findViewByPosition(i2);
            if (findViewByPosition != null) {
                setOrAnimateAlpha(findViewByPosition, selectedPosition == i2, i, z);
            }
            i2++;
        }
    }

    private void updateColumnSize() {
        for (int i = 0; i < getColumnsCount(); i++) {
            updateColumnSize(this.mColumnViews.get(i));
        }
    }

    private void updateColumnSize(VerticalGridView verticalGridView) {
        ViewGroup.LayoutParams layoutParams = verticalGridView.getLayoutParams();
        layoutParams.height = (int) ((isActivated() ? getActivatedVisibleItemCount() : getVisibleItemCount()) * getPickerItemHeightPixels());
        verticalGridView.setLayoutParams(layoutParams);
    }

    private void updateItemFocusable() {
        boolean isActivated = isActivated();
        for (int i = 0; i < getColumnsCount(); i++) {
            VerticalGridView verticalGridView = this.mColumnViews.get(i);
            for (int i2 = 0; i2 < verticalGridView.getChildCount(); i2++) {
                verticalGridView.getChildAt(i2).setFocusable(isActivated);
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (isActivated()) {
            switch (keyEvent.getKeyCode()) {
                case 23:
                case 66:
                    if (keyEvent.getAction() == 1) {
                        performClick();
                        return true;
                    }
                    return true;
                default:
                    return super.dispatchKeyEvent(keyEvent);
            }
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    public float getActivatedVisibleItemCount() {
        return this.mVisibleItemsActivated;
    }

    public PickerColumn getColumnAt(int i) {
        if (this.mColumns == null) {
            return null;
        }
        return this.mColumns.get(i);
    }

    public int getColumnsCount() {
        if (this.mColumns == null) {
            return 0;
        }
        return this.mColumns.size();
    }

    protected int getPickerItemHeightPixels() {
        return getContext().getResources().getDimensionPixelSize(R$dimen.picker_item_height);
    }

    public final int getPickerItemLayoutId() {
        return this.mPickerItemLayoutId;
    }

    public final int getPickerItemTextViewId() {
        return this.mPickerItemTextViewId;
    }

    public int getSelectedColumn() {
        return this.mSelectedColumn;
    }

    public final CharSequence getSeparator() {
        return this.mSeparator;
    }

    public float getVisibleItemCount() {
        return 1.0f;
    }

    public void onColumnValueChanged(int i, int i2) {
        PickerColumn pickerColumn = this.mColumns.get(i);
        if (pickerColumn.getCurrentValue() != i2) {
            pickerColumn.setCurrentValue(i2);
            notifyValueChanged(i);
        }
    }

    @Override // android.view.ViewGroup
    protected boolean onRequestFocusInDescendants(int i, Rect rect) {
        int selectedColumn = getSelectedColumn();
        if (selectedColumn < this.mColumnViews.size()) {
            return this.mColumnViews.get(selectedColumn).requestFocus(i, rect);
        }
        return false;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestChildFocus(View view, View view2) {
        super.requestChildFocus(view, view2);
        for (int i = 0; i < this.mColumnViews.size(); i++) {
            if (this.mColumnViews.get(i).hasFocus()) {
                setSelectedColumn(i);
            }
        }
    }

    @Override // android.view.View
    public void setActivated(boolean z) {
        if (z == isActivated()) {
            super.setActivated(z);
            return;
        }
        super.setActivated(z);
        updateColumnSize();
        updateItemFocusable();
    }

    public void setColumnAt(int i, PickerColumn pickerColumn) {
        this.mColumns.set(i, pickerColumn);
        VerticalGridView verticalGridView = this.mColumnViews.get(i);
        PickerScrollArrayAdapter pickerScrollArrayAdapter = (PickerScrollArrayAdapter) verticalGridView.getAdapter();
        if (pickerScrollArrayAdapter != null) {
            pickerScrollArrayAdapter.notifyDataSetChanged();
        }
        verticalGridView.setSelectedPosition(pickerColumn.getCurrentValue() - pickerColumn.getMinValue());
    }

    public void setColumnValue(int i, int i2, boolean z) {
        PickerColumn pickerColumn = this.mColumns.get(i);
        if (pickerColumn.getCurrentValue() != i2) {
            pickerColumn.setCurrentValue(i2);
            notifyValueChanged(i);
            VerticalGridView verticalGridView = this.mColumnViews.get(i);
            if (verticalGridView != null) {
                int minValue = i2 - this.mColumns.get(i).getMinValue();
                if (z) {
                    verticalGridView.setSelectedPositionSmooth(minValue);
                } else {
                    verticalGridView.setSelectedPosition(minValue);
                }
            }
        }
    }

    public void setColumns(List<PickerColumn> list) {
        this.mColumnViews.clear();
        this.mPickerView.removeAllViews();
        this.mColumns = new ArrayList<>(list);
        if (this.mSelectedColumn > this.mColumns.size() - 1) {
            this.mSelectedColumn = this.mColumns.size() - 1;
        }
        LayoutInflater from = LayoutInflater.from(getContext());
        int columnsCount = getColumnsCount();
        for (int i = 0; i < columnsCount; i++) {
            VerticalGridView verticalGridView = (VerticalGridView) from.inflate(R$layout.lb_picker_column, this.mPickerView, false);
            updateColumnSize(verticalGridView);
            verticalGridView.setWindowAlignment(0);
            verticalGridView.setHasFixedSize(false);
            this.mColumnViews.add(verticalGridView);
            this.mPickerView.addView(verticalGridView);
            if (i != columnsCount - 1 && getSeparator() != null) {
                TextView textView = (TextView) from.inflate(R$layout.lb_picker_separator, this.mPickerView, false);
                textView.setText(getSeparator());
                this.mPickerView.addView(textView);
            }
            verticalGridView.setAdapter(new PickerScrollArrayAdapter(this, getContext(), getPickerItemLayoutId(), getPickerItemTextViewId(), i));
            verticalGridView.setOnChildViewHolderSelectedListener(this.mColumnChangeListener);
        }
    }

    public void setSelectedColumn(int i) {
        if (this.mSelectedColumn != i) {
            this.mSelectedColumn = i;
            for (int i2 = 0; i2 < this.mColumnViews.size(); i2++) {
                updateColumnAlpha(i2, true);
            }
        }
    }

    public final void setSeparator(CharSequence charSequence) {
        this.mSeparator = charSequence;
    }
}
