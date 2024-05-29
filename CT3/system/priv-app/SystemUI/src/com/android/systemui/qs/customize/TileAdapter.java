package com.android.systemui.qs.customize;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.qs.QSIconView;
import com.android.systemui.qs.customize.TileQueryHelper;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/qs/customize/TileAdapter.class */
public class TileAdapter extends RecyclerView.Adapter<Holder> implements TileQueryHelper.TileStateListener {
    private int mAccessibilityFromIndex;
    private final AccessibilityManager mAccessibilityManager;
    private boolean mAccessibilityMoving;
    private List<TileQueryHelper.TileInfo> mAllTiles;
    private final Context mContext;
    private Holder mCurrentDrag;
    private List<String> mCurrentSpecs;
    private int mEditIndex;
    private QSTileHost mHost;
    private boolean mNeedsFocus;
    private List<TileQueryHelper.TileInfo> mOtherTiles;
    private int mTileDividerIndex;
    private final Handler mHandler = new Handler();
    private final List<TileQueryHelper.TileInfo> mTiles = new ArrayList();
    private final GridLayoutManager.SpanSizeLookup mSizeLookup = new GridLayoutManager.SpanSizeLookup(this) { // from class: com.android.systemui.qs.customize.TileAdapter.1
        final TileAdapter this$0;

        {
            this.this$0 = this;
        }

        /* JADX WARN: Code restructure failed: missing block: B:5:0x0014, code lost:
            if (r0 == 4) goto L8;
         */
        @Override // android.support.v7.widget.GridLayoutManager.SpanSizeLookup
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public int getSpanSize(int i) {
            int i2;
            int itemViewType = this.this$0.getItemViewType(i);
            if (itemViewType != 1) {
                i2 = 1;
            }
            i2 = 3;
            return i2;
        }
    };
    private final RecyclerView.ItemDecoration mDecoration = new RecyclerView.ItemDecoration(this) { // from class: com.android.systemui.qs.customize.TileAdapter.2
        private final ColorDrawable mDrawable = new ColorDrawable(-13090232);
        final TileAdapter this$0;

        {
            this.this$0 = this;
        }

        @Override // android.support.v7.widget.RecyclerView.ItemDecoration
        public void onDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
            super.onDraw(canvas, recyclerView, state);
            int childCount = recyclerView.getChildCount();
            int width = recyclerView.getWidth();
            int bottom = recyclerView.getBottom();
            for (int i = 0; i < childCount; i++) {
                View childAt = recyclerView.getChildAt(i);
                if (recyclerView.getChildViewHolder(childAt).getAdapterPosition() >= this.this$0.mEditIndex || (childAt instanceof TextView)) {
                    RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) childAt.getLayoutParams();
                    this.mDrawable.setBounds(0, childAt.getTop() + layoutParams.topMargin + Math.round(ViewCompat.getTranslationY(childAt)), width, bottom);
                    this.mDrawable.draw(canvas);
                    return;
                }
            }
        }
    };
    private final ItemTouchHelper.Callback mCallbacks = new AnonymousClass3(this);
    private final ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(this.mCallbacks);

    /* renamed from: com.android.systemui.qs.customize.TileAdapter$3  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/qs/customize/TileAdapter$3.class */
    class AnonymousClass3 extends ItemTouchHelper.Callback {
        final TileAdapter this$0;

        AnonymousClass3(TileAdapter tileAdapter) {
            this.this$0 = tileAdapter;
        }

        @Override // android.support.v7.widget.helper.ItemTouchHelper.Callback
        public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            return viewHolder2.getAdapterPosition() <= this.this$0.mEditIndex + 1;
        }

        @Override // android.support.v7.widget.helper.ItemTouchHelper.Callback
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return viewHolder.getItemViewType() == 1 ? makeMovementFlags(0, 0) : makeMovementFlags(15, 0);
        }

        @Override // android.support.v7.widget.helper.ItemTouchHelper.Callback
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override // android.support.v7.widget.helper.ItemTouchHelper.Callback
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override // android.support.v7.widget.helper.ItemTouchHelper.Callback
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            return this.this$0.move(viewHolder.getAdapterPosition(), viewHolder2.getAdapterPosition(), viewHolder2.itemView);
        }

        @Override // android.support.v7.widget.helper.ItemTouchHelper.Callback
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int i) {
            super.onSelectedChanged(viewHolder, i);
            if (i != 2) {
                viewHolder = null;
            }
            if (viewHolder == this.this$0.mCurrentDrag) {
                return;
            }
            if (this.this$0.mCurrentDrag != null) {
                int adapterPosition = this.this$0.mCurrentDrag.getAdapterPosition();
                TileQueryHelper.TileInfo tileInfo = (TileQueryHelper.TileInfo) this.this$0.mTiles.get(adapterPosition);
                CustomizeTileView customizeTileView = this.this$0.mCurrentDrag.mTileView;
                boolean z = false;
                if (adapterPosition > this.this$0.mEditIndex) {
                    z = !tileInfo.isSystem;
                }
                customizeTileView.setShowAppLabel(z);
                this.this$0.mCurrentDrag.stopDrag();
                this.this$0.mCurrentDrag = null;
            }
            if (viewHolder != null) {
                this.this$0.mCurrentDrag = (Holder) viewHolder;
                this.this$0.mCurrentDrag.startDrag();
            }
            this.this$0.mHandler.post(new Runnable(this) { // from class: com.android.systemui.qs.customize.TileAdapter.3.1
                final AnonymousClass3 this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.notifyItemChanged(this.this$1.this$0.mEditIndex);
                }
            });
        }

        @Override // android.support.v7.widget.helper.ItemTouchHelper.Callback
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/customize/TileAdapter$Holder.class */
    public class Holder extends RecyclerView.ViewHolder {
        private CustomizeTileView mTileView;
        final TileAdapter this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Holder(TileAdapter tileAdapter, View view) {
            super(view);
            this.this$0 = tileAdapter;
            if (view instanceof FrameLayout) {
                this.mTileView = (CustomizeTileView) ((FrameLayout) view).getChildAt(0);
                this.mTileView.setBackground(null);
                this.mTileView.getIcon().disableAnimation();
            }
        }

        public void clearDrag() {
            this.itemView.clearAnimation();
            this.mTileView.findViewById(2131886589).clearAnimation();
            this.mTileView.findViewById(2131886589).setAlpha(1.0f);
            this.mTileView.getAppLabel().clearAnimation();
            this.mTileView.getAppLabel().setAlpha(0.6f);
        }

        public void startDrag() {
            this.itemView.animate().setDuration(100L).scaleX(1.2f).scaleY(1.2f);
            this.mTileView.findViewById(2131886589).animate().setDuration(100L).alpha(0.0f);
            this.mTileView.getAppLabel().animate().setDuration(100L).alpha(0.0f);
        }

        public void stopDrag() {
            this.itemView.animate().setDuration(100L).scaleX(1.0f).scaleY(1.0f);
            this.mTileView.findViewById(2131886589).animate().setDuration(100L).alpha(1.0f);
            this.mTileView.getAppLabel().animate().setDuration(100L).alpha(0.6f);
        }
    }

    public TileAdapter(Context context) {
        this.mContext = context;
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
    }

    private TileQueryHelper.TileInfo getAndRemoveOther(String str) {
        for (int i = 0; i < this.mOtherTiles.size(); i++) {
            if (this.mOtherTiles.get(i).spec.equals(str)) {
                return this.mOtherTiles.remove(i);
            }
        }
        return null;
    }

    private <T> void move(int i, int i2, List<T> list) {
        list.add(i2, list.remove(i));
        notifyItemMoved(i, i2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean move(int i, int i2, View view) {
        String string;
        if (i2 == i) {
            return true;
        }
        CharSequence charSequence = this.mTiles.get(i).state.label;
        move(i, i2, this.mTiles);
        updateDividerLocations();
        if (i2 >= this.mEditIndex) {
            MetricsLogger.action(this.mContext, 360, strip(this.mTiles.get(i2)));
            MetricsLogger.action(this.mContext, 361, i);
            string = this.mContext.getString(2131493899, charSequence);
        } else if (i >= this.mEditIndex) {
            MetricsLogger.action(this.mContext, 362, strip(this.mTiles.get(i2)));
            MetricsLogger.action(this.mContext, 363, i2);
            string = this.mContext.getString(2131493898, charSequence, Integer.valueOf(i2 + 1));
        } else {
            MetricsLogger.action(this.mContext, 364, strip(this.mTiles.get(i2)));
            MetricsLogger.action(this.mContext, 365, i2);
            string = this.mContext.getString(2131493900, charSequence, Integer.valueOf(i2 + 1));
        }
        view.announceForAccessibility(string);
        saveSpecs(this.mHost);
        return true;
    }

    private void recalcSpecs() {
        if (this.mCurrentSpecs == null || this.mAllTiles == null) {
            return;
        }
        this.mOtherTiles = new ArrayList(this.mAllTiles);
        this.mTiles.clear();
        for (int i = 0; i < this.mCurrentSpecs.size(); i++) {
            TileQueryHelper.TileInfo andRemoveOther = getAndRemoveOther(this.mCurrentSpecs.get(i));
            if (andRemoveOther != null) {
                this.mTiles.add(andRemoveOther);
            }
        }
        this.mTiles.add(null);
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 >= this.mOtherTiles.size()) {
                this.mTileDividerIndex = this.mTiles.size();
                this.mTiles.add(null);
                this.mTiles.addAll(this.mOtherTiles);
                updateDividerLocations();
                notifyDataSetChanged();
                return;
            }
            TileQueryHelper.TileInfo tileInfo = this.mOtherTiles.get(i3);
            int i4 = i3;
            if (tileInfo.isSystem) {
                this.mOtherTiles.remove(i3);
                this.mTiles.add(tileInfo);
                i4 = i3 - 1;
            }
            i2 = i4 + 1;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void selectPosition(int i, View view) {
        this.mAccessibilityMoving = false;
        List<TileQueryHelper.TileInfo> list = this.mTiles;
        int i2 = this.mEditIndex;
        this.mEditIndex = i2 - 1;
        list.remove(i2);
        notifyItemRemoved(this.mEditIndex - 1);
        move(this.mAccessibilityFromIndex, i, view);
        notifyDataSetChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showAccessibilityDialog(int i, View view) {
        TileQueryHelper.TileInfo tileInfo = this.mTiles.get(i);
        AlertDialog create = new AlertDialog.Builder(this.mContext).setItems(new CharSequence[]{this.mContext.getString(2131493896, tileInfo.state.label), this.mContext.getString(2131493897, tileInfo.state.label)}, new DialogInterface.OnClickListener(this, i, tileInfo, view) { // from class: com.android.systemui.qs.customize.TileAdapter.7
            final TileAdapter this$0;
            final TileQueryHelper.TileInfo val$info;
            final int val$position;
            final View val$v;

            {
                this.this$0 = this;
                this.val$position = i;
                this.val$info = tileInfo;
                this.val$v = view;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                if (i2 == 0) {
                    this.this$0.startAccessibleDrag(this.val$position);
                    return;
                }
                this.this$0.move(this.val$position, this.val$info.isSystem ? this.this$0.mEditIndex : this.this$0.mTileDividerIndex, this.val$v);
                this.this$0.notifyItemChanged(this.this$0.mTileDividerIndex);
                this.this$0.notifyDataSetChanged();
            }
        }).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
        SystemUIDialog.setShowForAllUsers(create, true);
        SystemUIDialog.applyFlags(create);
        create.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startAccessibleDrag(int i) {
        this.mAccessibilityMoving = true;
        this.mNeedsFocus = true;
        this.mAccessibilityFromIndex = i;
        List<TileQueryHelper.TileInfo> list = this.mTiles;
        int i2 = this.mEditIndex;
        this.mEditIndex = i2 + 1;
        list.add(i2, null);
        notifyDataSetChanged();
    }

    private static String strip(TileQueryHelper.TileInfo tileInfo) {
        String str = tileInfo.spec;
        return str.startsWith("custom(") ? CustomTile.getComponentFromSpec(str).getPackageName() : str;
    }

    private void updateDividerLocations() {
        this.mEditIndex = -1;
        this.mTileDividerIndex = this.mTiles.size();
        for (int i = 0; i < this.mTiles.size(); i++) {
            if (this.mTiles.get(i) == null) {
                if (this.mEditIndex == -1) {
                    this.mEditIndex = i;
                } else {
                    this.mTileDividerIndex = i;
                }
            }
        }
        if (this.mTiles.size() - 1 == this.mTileDividerIndex) {
            notifyItemChanged(this.mTileDividerIndex);
        }
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mTiles.size();
    }

    public RecyclerView.ItemDecoration getItemDecoration() {
        return this.mDecoration;
    }

    public ItemTouchHelper getItemTouchHelper() {
        return this.mItemTouchHelper;
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        if (this.mAccessibilityMoving && i == this.mEditIndex - 1) {
            return 2;
        }
        if (i == this.mTileDividerIndex) {
            return 4;
        }
        return this.mTiles.get(i) == null ? 1 : 0;
    }

    public GridLayoutManager.SpanSizeLookup getSizeLookup() {
        return this.mSizeLookup;
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public void onBindViewHolder(Holder holder, int i) {
        if (holder.getItemViewType() == 4) {
            holder.itemView.setVisibility(this.mTileDividerIndex < this.mTiles.size() - 1 ? 0 : 4);
        } else if (holder.getItemViewType() == 1) {
            ((TextView) holder.itemView.findViewById(16908310)).setText(this.mCurrentDrag != null ? 2131493878 : 2131493877);
        } else if (holder.getItemViewType() == 2) {
            holder.mTileView.setClickable(true);
            holder.mTileView.setFocusable(true);
            holder.mTileView.setFocusableInTouchMode(true);
            holder.mTileView.setVisibility(0);
            holder.mTileView.setImportantForAccessibility(1);
            holder.mTileView.setContentDescription(this.mContext.getString(2131493895, Integer.valueOf(i + 1)));
            holder.mTileView.setOnClickListener(new View.OnClickListener(this, holder) { // from class: com.android.systemui.qs.customize.TileAdapter.4
                final TileAdapter this$0;
                final Holder val$holder;

                {
                    this.this$0 = this;
                    this.val$holder = holder;
                }

                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    this.this$0.selectPosition(this.val$holder.getAdapterPosition(), view);
                }
            });
            if (this.mNeedsFocus) {
                holder.mTileView.requestLayout();
                holder.mTileView.addOnLayoutChangeListener(new View.OnLayoutChangeListener(this, holder) { // from class: com.android.systemui.qs.customize.TileAdapter.5
                    final TileAdapter this$0;
                    final Holder val$holder;

                    {
                        this.this$0 = this;
                        this.val$holder = holder;
                    }

                    @Override // android.view.View.OnLayoutChangeListener
                    public void onLayoutChange(View view, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
                        this.val$holder.mTileView.removeOnLayoutChangeListener(this);
                        this.val$holder.mTileView.requestFocus();
                    }
                });
                this.mNeedsFocus = false;
            }
        } else {
            TileQueryHelper.TileInfo tileInfo = this.mTiles.get(i);
            if (i > this.mEditIndex) {
                tileInfo.state.contentDescription = this.mContext.getString(2131493894, tileInfo.state.label);
            } else if (this.mAccessibilityMoving) {
                tileInfo.state.contentDescription = this.mContext.getString(2131493895, Integer.valueOf(i + 1));
            } else {
                tileInfo.state.contentDescription = this.mContext.getString(2131493893, Integer.valueOf(i + 1), tileInfo.state.label);
            }
            holder.mTileView.onStateChanged(tileInfo.state);
            holder.mTileView.setAppLabel(tileInfo.appLabel);
            CustomizeTileView customizeTileView = holder.mTileView;
            boolean z = false;
            if (i > this.mEditIndex) {
                z = !tileInfo.isSystem;
            }
            customizeTileView.setShowAppLabel(z);
            if (this.mAccessibilityManager.isTouchExplorationEnabled()) {
                boolean z2 = !this.mAccessibilityMoving || i < this.mEditIndex;
                holder.mTileView.setClickable(z2);
                holder.mTileView.setFocusable(z2);
                holder.mTileView.setImportantForAccessibility(z2 ? 1 : 4);
                if (z2) {
                    holder.mTileView.setOnClickListener(new View.OnClickListener(this, holder) { // from class: com.android.systemui.qs.customize.TileAdapter.6
                        final TileAdapter this$0;
                        final Holder val$holder;

                        {
                            this.this$0 = this;
                            this.val$holder = holder;
                        }

                        @Override // android.view.View.OnClickListener
                        public void onClick(View view) {
                            int adapterPosition = this.val$holder.getAdapterPosition();
                            if (this.this$0.mAccessibilityMoving) {
                                this.this$0.selectPosition(adapterPosition, view);
                            } else if (adapterPosition < this.this$0.mEditIndex) {
                                this.this$0.showAccessibilityDialog(adapterPosition, view);
                            } else {
                                this.this$0.startAccessibleDrag(adapterPosition);
                            }
                        }
                    });
                }
            }
        }
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater from = LayoutInflater.from(context);
        if (i == 4) {
            return new Holder(this, from.inflate(2130968756, viewGroup, false));
        }
        if (i == 1) {
            return new Holder(this, from.inflate(2130968753, viewGroup, false));
        }
        FrameLayout frameLayout = (FrameLayout) from.inflate(2130968757, viewGroup, false);
        frameLayout.addView(new CustomizeTileView(context, new QSIconView(context)));
        return new Holder(this, frameLayout);
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public boolean onFailedToRecycleView(Holder holder) {
        holder.clearDrag();
        return true;
    }

    @Override // com.android.systemui.qs.customize.TileQueryHelper.TileStateListener
    public void onTilesChanged(List<TileQueryHelper.TileInfo> list) {
        this.mAllTiles = list;
        recalcSpecs();
    }

    public void saveSpecs(QSTileHost qSTileHost) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < this.mTiles.size() && this.mTiles.get(i) != null; i++) {
            arrayList.add(this.mTiles.get(i).spec);
        }
        qSTileHost.changeTiles(this.mCurrentSpecs, arrayList);
        this.mCurrentSpecs = arrayList;
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
    }

    public void setTileSpecs(List<String> list) {
        if (list.equals(this.mCurrentSpecs)) {
            return;
        }
        this.mCurrentSpecs = list;
        recalcSpecs();
    }
}
