package com.android.setupwizardlib.items;

import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.setupwizardlib.R;
import com.android.setupwizardlib.items.ItemHierarchy;
/* loaded from: classes.dex */
public class RecyclerItemAdapter extends RecyclerView.Adapter<ItemViewHolder> implements ItemHierarchy.Observer {
    private final ItemHierarchy mItemHierarchy;
    private OnItemSelectedListener mListener;

    /* loaded from: classes.dex */
    public interface OnItemSelectedListener {
        void onItemSelected(IItem iItem);
    }

    public RecyclerItemAdapter(ItemHierarchy itemHierarchy) {
        this.mItemHierarchy = itemHierarchy;
        this.mItemHierarchy.registerObserver(this);
    }

    public IItem getItem(int i) {
        return this.mItemHierarchy.getItemAt(i);
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public long getItemId(int i) {
        int id;
        IItem item = getItem(i);
        if (!(item instanceof AbstractItem) || (id = ((AbstractItem) item).getId()) <= 0) {
            return -1L;
        }
        return id;
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mItemHierarchy.getCount();
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(i, viewGroup, false);
        final ItemViewHolder itemViewHolder = new ItemViewHolder(inflate);
        if (!"noBackground".equals(inflate.getTag())) {
            TypedArray obtainStyledAttributes = viewGroup.getContext().obtainStyledAttributes(R.styleable.SuwRecyclerItemAdapter);
            Drawable drawable = obtainStyledAttributes.getDrawable(R.styleable.SuwRecyclerItemAdapter_android_selectableItemBackground);
            if (drawable == null) {
                drawable = obtainStyledAttributes.getDrawable(R.styleable.SuwRecyclerItemAdapter_selectableItemBackground);
            }
            Drawable background = inflate.getBackground();
            if (background == null) {
                background = obtainStyledAttributes.getDrawable(R.styleable.SuwRecyclerItemAdapter_android_colorBackground);
            }
            if (drawable == null || background == null) {
                Log.e("RecyclerItemAdapter", "Cannot resolve required attributes. selectableItemBackground=" + drawable + " background=" + background);
            } else {
                inflate.setBackgroundDrawable(new PatchedLayerDrawable(new Drawable[]{background, drawable}));
            }
            obtainStyledAttributes.recycle();
        }
        inflate.setOnClickListener(new View.OnClickListener() { // from class: com.android.setupwizardlib.items.RecyclerItemAdapter.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                IItem item = itemViewHolder.getItem();
                if (RecyclerItemAdapter.this.mListener != null && item != null && item.isEnabled()) {
                    RecyclerItemAdapter.this.mListener.onItemSelected(item);
                }
            }
        });
        return itemViewHolder;
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public void onBindViewHolder(ItemViewHolder itemViewHolder, int i) {
        IItem item = getItem(i);
        itemViewHolder.setEnabled(item.isEnabled());
        itemViewHolder.setItem(item);
        item.onBindView(itemViewHolder.itemView);
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        return getItem(i).getLayoutResource();
    }

    @Override // com.android.setupwizardlib.items.ItemHierarchy.Observer
    public void onItemRangeChanged(ItemHierarchy itemHierarchy, int i, int i2) {
        notifyItemRangeChanged(i, i2);
    }

    @Override // com.android.setupwizardlib.items.ItemHierarchy.Observer
    public void onItemRangeInserted(ItemHierarchy itemHierarchy, int i, int i2) {
        notifyItemRangeInserted(i, i2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class PatchedLayerDrawable extends LayerDrawable {
        PatchedLayerDrawable(Drawable[] drawableArr) {
            super(drawableArr);
        }

        @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
        public boolean getPadding(Rect rect) {
            return super.getPadding(rect) && !(rect.left == 0 && rect.top == 0 && rect.right == 0 && rect.bottom == 0);
        }
    }
}
