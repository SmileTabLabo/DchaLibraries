package com.android.browser;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.browser.util.ThreadedCursorAdapter;
import com.android.browser.view.BookmarkContainer;
/* loaded from: b.zip:com/android/browser/BrowserBookmarksAdapter.class */
public class BrowserBookmarksAdapter extends ThreadedCursorAdapter<BrowserBookmarksAdapterItem> {
    Context mContext;
    LayoutInflater mInflater;

    public BrowserBookmarksAdapter(Context context) {
        super(context, null);
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
    }

    void bindGridView(View view, Context context, BrowserBookmarksAdapterItem browserBookmarksAdapterItem) {
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(2131427367);
        view.setPadding(dimensionPixelSize, view.getPaddingTop(), dimensionPixelSize, view.getPaddingBottom());
        ImageView imageView = (ImageView) view.findViewById(2131558430);
        ((TextView) view.findViewById(2131558424)).setText(browserBookmarksAdapterItem.title);
        if (browserBookmarksAdapterItem.is_folder) {
            imageView.setImageResource(2130837612);
            imageView.setScaleType(ImageView.ScaleType.FIT_END);
            imageView.setBackground(null);
            return;
        }
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (browserBookmarksAdapterItem.thumbnail == null || !browserBookmarksAdapterItem.has_thumbnail) {
            imageView.setImageResource(2130837518);
        } else {
            imageView.setImageDrawable(browserBookmarksAdapterItem.thumbnail);
        }
        imageView.setBackgroundResource(2130837516);
    }

    @Override // com.android.browser.util.ThreadedCursorAdapter
    public void bindView(View view, BrowserBookmarksAdapterItem browserBookmarksAdapterItem) {
        BookmarkContainer bookmarkContainer = (BookmarkContainer) view;
        bookmarkContainer.setIgnoreRequestLayout(true);
        bindGridView(view, this.mContext, browserBookmarksAdapterItem);
        bookmarkContainer.setIgnoreRequestLayout(false);
    }

    @Override // com.android.browser.util.ThreadedCursorAdapter
    protected long getItemId(Cursor cursor) {
        return cursor.getLong(0);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.browser.util.ThreadedCursorAdapter
    public BrowserBookmarksAdapterItem getLoadingObject() {
        return new BrowserBookmarksAdapterItem();
    }

    @Override // com.android.browser.util.ThreadedCursorAdapter
    public BrowserBookmarksAdapterItem getRowObject(Cursor cursor, BrowserBookmarksAdapterItem browserBookmarksAdapterItem) {
        BrowserBookmarksAdapterItem browserBookmarksAdapterItem2 = browserBookmarksAdapterItem;
        if (browserBookmarksAdapterItem == null) {
            browserBookmarksAdapterItem2 = new BrowserBookmarksAdapterItem();
        }
        Bitmap bitmap = null;
        if (browserBookmarksAdapterItem2.thumbnail != null) {
            bitmap = browserBookmarksAdapterItem2.thumbnail.getBitmap();
        }
        Bitmap bitmap2 = BrowserBookmarksPage.getBitmap(cursor, 4, bitmap);
        browserBookmarksAdapterItem2.has_thumbnail = bitmap2 != null;
        if (bitmap2 != null && (browserBookmarksAdapterItem2.thumbnail == null || browserBookmarksAdapterItem2.thumbnail.getBitmap() != bitmap2)) {
            browserBookmarksAdapterItem2.thumbnail = new BitmapDrawable(this.mContext.getResources(), bitmap2);
        }
        boolean z = false;
        if (cursor.getInt(6) != 0) {
            z = true;
        }
        browserBookmarksAdapterItem2.is_folder = z;
        browserBookmarksAdapterItem2.title = getTitle(cursor);
        browserBookmarksAdapterItem2.url = cursor.getString(1);
        return browserBookmarksAdapterItem2;
    }

    CharSequence getTitle(Cursor cursor) {
        switch (cursor.getInt(9)) {
            case 4:
                return this.mContext.getText(2131492931);
            default:
                return cursor.getString(2);
        }
    }

    @Override // com.android.browser.util.ThreadedCursorAdapter
    public View newView(Context context, ViewGroup viewGroup) {
        return this.mInflater.inflate(2130968585, viewGroup, false);
    }
}
