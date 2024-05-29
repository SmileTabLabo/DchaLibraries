package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.FontSizeUtils;
/* loaded from: a.zip:com/android/systemui/qs/QSDetailItems.class */
public class QSDetailItems extends FrameLayout {
    private static final boolean DEBUG = Log.isLoggable("QSDetailItems", 3);
    private final Adapter mAdapter;
    private Callback mCallback;
    private final Context mContext;
    private View mEmpty;
    private ImageView mEmptyIcon;
    private TextView mEmptyText;
    private final H mHandler;
    private AutoSizingList mItemList;
    private Item[] mItems;
    private boolean mItemsVisible;
    private String mTag;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/qs/QSDetailItems$Adapter.class */
    public class Adapter extends BaseAdapter {
        final QSDetailItems this$0;

        private Adapter(QSDetailItems qSDetailItems) {
            this.this$0 = qSDetailItems;
        }

        /* synthetic */ Adapter(QSDetailItems qSDetailItems, Adapter adapter) {
            this(qSDetailItems);
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return this.this$0.mItems != null ? this.this$0.mItems.length : 0;
        }

        @Override // android.widget.Adapter
        public Object getItem(int i) {
            return this.this$0.mItems[i];
        }

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return 0L;
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            Item item = this.this$0.mItems[i];
            View view2 = view;
            if (view == null) {
                view2 = LayoutInflater.from(this.this$0.mContext).inflate(2130968761, viewGroup, false);
            }
            view2.setVisibility(this.this$0.mItemsVisible ? 0 : 4);
            ImageView imageView = (ImageView) view2.findViewById(16908294);
            imageView.setImageResource(item.icon);
            imageView.getOverlay().clear();
            if (item.overlay != null) {
                item.overlay.setBounds(0, 0, item.overlay.getIntrinsicWidth(), item.overlay.getIntrinsicHeight());
                imageView.getOverlay().add(item.overlay);
            }
            TextView textView = (TextView) view2.findViewById(16908310);
            textView.setText(item.line1);
            TextView textView2 = (TextView) view2.findViewById(16908304);
            boolean z = !TextUtils.isEmpty(item.line2);
            textView.setMaxLines(z ? 1 : 2);
            textView2.setVisibility(z ? 0 : 8);
            textView2.setText(z ? item.line2 : null);
            view2.setOnClickListener(new View.OnClickListener(this, item) { // from class: com.android.systemui.qs.QSDetailItems.Adapter.1
                final Adapter this$1;
                final Item val$item;

                {
                    this.this$1 = this;
                    this.val$item = item;
                }

                @Override // android.view.View.OnClickListener
                public void onClick(View view3) {
                    if (this.this$1.this$0.mCallback != null) {
                        this.this$1.this$0.mCallback.onDetailItemClick(this.val$item);
                    }
                }
            });
            ImageView imageView2 = (ImageView) view2.findViewById(16908296);
            imageView2.setVisibility(item.canDisconnect ? 0 : 8);
            imageView2.setOnClickListener(new View.OnClickListener(this, item) { // from class: com.android.systemui.qs.QSDetailItems.Adapter.2
                final Adapter this$1;
                final Item val$item;

                {
                    this.this$1 = this;
                    this.val$item = item;
                }

                @Override // android.view.View.OnClickListener
                public void onClick(View view3) {
                    if (this.this$1.this$0.mCallback != null) {
                        this.this$1.this$0.mCallback.onDetailItemDisconnect(this.val$item);
                    }
                }
            });
            return view2;
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSDetailItems$Callback.class */
    public interface Callback {
        void onDetailItemClick(Item item);

        void onDetailItemDisconnect(Item item);
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSDetailItems$H.class */
    private class H extends Handler {
        final QSDetailItems this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public H(QSDetailItems qSDetailItems) {
            super(Looper.getMainLooper());
            this.this$0 = qSDetailItems;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            boolean z = true;
            if (message.what == 1) {
                this.this$0.handleSetItems((Item[]) message.obj);
            } else if (message.what == 2) {
                this.this$0.handleSetCallback((Callback) message.obj);
            } else if (message.what == 3) {
                QSDetailItems qSDetailItems = this.this$0;
                if (message.arg1 == 0) {
                    z = false;
                }
                qSDetailItems.handleSetItemsVisible(z);
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSDetailItems$Item.class */
    public static class Item {
        public boolean canDisconnect;
        public int icon;
        public CharSequence line1;
        public CharSequence line2;
        public Drawable overlay;
        public Object tag;
    }

    public QSDetailItems(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mHandler = new H(this);
        this.mAdapter = new Adapter(this, null);
        this.mItemsVisible = true;
        this.mContext = context;
        this.mTag = "QSDetailItems";
    }

    public static QSDetailItems convertOrInflate(Context context, View view, ViewGroup viewGroup) {
        return view instanceof QSDetailItems ? (QSDetailItems) view : (QSDetailItems) LayoutInflater.from(context).inflate(2130968762, viewGroup, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSetCallback(Callback callback) {
        this.mCallback = callback;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSetItems(Item[] itemArr) {
        int length = itemArr != null ? itemArr.length : 0;
        this.mEmpty.setVisibility(length == 0 ? 0 : 8);
        this.mItemList.setVisibility(length == 0 ? 8 : 0);
        this.mItems = itemArr;
        this.mAdapter.notifyDataSetChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSetItemsVisible(boolean z) {
        if (this.mItemsVisible == z) {
            return;
        }
        this.mItemsVisible = z;
        for (int i = 0; i < this.mItemList.getChildCount(); i++) {
            this.mItemList.getChildAt(i).setVisibility(this.mItemsVisible ? 0 : 4);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (DEBUG) {
            Log.d(this.mTag, "onAttachedToWindow");
        }
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        FontSizeUtils.updateFontSize(this.mEmptyText, 2131689854);
        int childCount = this.mItemList.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = this.mItemList.getChildAt(i);
            FontSizeUtils.updateFontSize(childAt, 16908310, 2131689852);
            FontSizeUtils.updateFontSize(childAt, 16908304, 2131689853);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (DEBUG) {
            Log.d(this.mTag, "onDetachedFromWindow");
        }
        this.mCallback = null;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mItemList = (AutoSizingList) findViewById(16908298);
        this.mItemList.setVisibility(8);
        this.mItemList.setAdapter(this.mAdapter);
        this.mEmpty = findViewById(16908292);
        this.mEmpty.setVisibility(8);
        this.mEmptyText = (TextView) this.mEmpty.findViewById(16908310);
        this.mEmptyIcon = (ImageView) this.mEmpty.findViewById(16908294);
    }

    public void setCallback(Callback callback) {
        this.mHandler.removeMessages(2);
        this.mHandler.obtainMessage(2, callback).sendToTarget();
    }

    public void setEmptyState(int i, int i2) {
        this.mEmptyIcon.setImageResource(i);
        this.mEmptyText.setText(i2);
    }

    public void setItems(Item[] itemArr) {
        this.mHandler.removeMessages(1);
        this.mHandler.obtainMessage(1, itemArr).sendToTarget();
    }

    public void setItemsVisible(boolean z) {
        this.mHandler.removeMessages(3);
        this.mHandler.obtainMessage(3, z ? 1 : 0, 0).sendToTarget();
    }

    public void setTagSuffix(String str) {
        this.mTag = "QSDetailItems." + str;
    }
}
