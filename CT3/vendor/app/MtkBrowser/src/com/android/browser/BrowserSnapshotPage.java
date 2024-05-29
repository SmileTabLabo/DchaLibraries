package com.android.browser;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import com.android.browser.provider.SnapshotProvider;
import java.text.DateFormat;
import java.util.Date;
/* loaded from: b.zip:com/android/browser/BrowserSnapshotPage.class */
public class BrowserSnapshotPage extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    private static final String[] PROJECTION = {"_id", "title", "viewstate_size", "thumbnail", "favicon", "url", "date_created", "viewstate_path", "progress", "job_id"};
    SnapshotAdapter mAdapter;
    long mAnimateId;
    CombinedBookmarksCallbacks mCallback;
    View mEmpty;
    GridView mGrid;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/BrowserSnapshotPage$SnapshotAdapter.class */
    public static class SnapshotAdapter extends ResourceCursorAdapter {
        private long mAnimateId;
        private AnimatorSet mAnimation;
        private View mAnimationTarget;

        public SnapshotAdapter(Context context, Cursor cursor) {
            super(context, 2130968621, cursor, 0);
            this.mAnimation = new AnimatorSet();
            this.mAnimation.playTogether(ObjectAnimator.ofFloat((Object) null, View.SCALE_X, 0.0f, 1.0f), ObjectAnimator.ofFloat((Object) null, View.SCALE_Y, 0.0f, 1.0f));
            this.mAnimation.setStartDelay(100L);
            this.mAnimation.setDuration(400L);
            this.mAnimation.addListener(new Animator.AnimatorListener(this) { // from class: com.android.browser.BrowserSnapshotPage.SnapshotAdapter.1
                final SnapshotAdapter this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.this$1.mAnimateId = 0L;
                    this.this$1.mAnimationTarget = null;
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animator) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                }
            });
        }

        public void animateIn(long j) {
            this.mAnimateId = j;
        }

        @Override // android.widget.BaseAdapter, android.widget.ListAdapter
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override // android.widget.CursorAdapter
        public void bindView(View view, Context context, Cursor cursor) {
            if (cursor.getLong(0) == this.mAnimateId) {
                if (this.mAnimationTarget != view) {
                    float f = 0.0f;
                    if (this.mAnimationTarget != null) {
                        f = this.mAnimationTarget.getScaleX();
                        this.mAnimationTarget.setScaleX(1.0f);
                        this.mAnimationTarget.setScaleY(1.0f);
                    }
                    view.setScaleX(f);
                    view.setScaleY(f);
                }
                this.mAnimation.setTarget(view);
                this.mAnimationTarget = view;
                if (!this.mAnimation.isRunning()) {
                    this.mAnimation.start();
                }
            }
            ImageView imageView = (ImageView) view.findViewById(2131558430);
            byte[] blob = cursor.getBlob(3);
            if (blob == null) {
                imageView.setImageResource(2130837518);
            } else {
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(blob, 0, blob.length));
            }
            ((TextView) view.findViewById(2131558407)).setText(cursor.getString(1));
            TextView textView = (TextView) view.findViewById(2131558516);
            if (textView != null) {
                textView.setText(String.format("%.2fMB", Float.valueOf((cursor.getInt(2) / 1024.0f) / 1024.0f)));
            }
            ((TextView) view.findViewById(2131558513)).setText(DateFormat.getDateInstance(3).format(new Date(cursor.getLong(6))));
            ProgressBar progressBar = (ProgressBar) view.findViewById(2131558515);
            progressBar.setProgress(cursor.getInt(8));
            ImageView imageView2 = (ImageView) view.findViewById(2131558514);
            if (progressBar.getProgress() >= 100) {
                imageView2.setVisibility(8);
                return;
            }
            imageView2.setPadding(imageView.getPaddingStart(), imageView.getPaddingTop(), imageView.getPaddingEnd(), imageView.getPaddingBottom());
            imageView2.setVisibility(0);
        }

        @Override // android.widget.CursorAdapter, android.widget.Adapter
        public Cursor getItem(int i) {
            return (Cursor) super.getItem(i);
        }

        @Override // android.widget.BaseAdapter, android.widget.ListAdapter
        public boolean isEnabled(int i) {
            return getItem(i).getInt(8) >= 100;
        }
    }

    static Bitmap getBitmap(Cursor cursor, int i) {
        byte[] blob = cursor.getBlob(i);
        if (blob == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(blob, 0, blob.length);
    }

    private void populateBookmarkItem(Cursor cursor, BookmarkItem bookmarkItem) {
        bookmarkItem.setName(cursor.getString(1));
        bookmarkItem.setUrl(cursor.getString(5));
        bookmarkItem.setFavicon(getBitmap(cursor, 4));
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [com.android.browser.BrowserSnapshotPage$1] */
    void deleteSnapshot(long j) {
        new Thread(this, getActivity().getContentResolver(), ContentUris.withAppendedId(SnapshotProvider.Snapshots.CONTENT_URI, j)) { // from class: com.android.browser.BrowserSnapshotPage.1
            final BrowserSnapshotPage this$0;
            final ContentResolver val$cr;
            final Uri val$uri;

            {
                this.this$0 = this;
                this.val$cr = r5;
                this.val$uri = r6;
            }

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                this.val$cr.delete(this.val$uri, null, null);
            }
        }.start();
    }

    View getTargetView(ContextMenu.ContextMenuInfo contextMenuInfo) {
        if (contextMenuInfo instanceof AdapterView.AdapterContextMenuInfo) {
            return ((AdapterView.AdapterContextMenuInfo) contextMenuInfo).targetView;
        }
        if (contextMenuInfo instanceof ExpandableListView.ExpandableListContextMenuInfo) {
            return ((ExpandableListView.ExpandableListContextMenuInfo) contextMenuInfo).targetView;
        }
        return null;
    }

    @Override // android.app.Fragment
    public boolean onContextItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 2131558568) {
            ContextMenu.ContextMenuInfo menuInfo = menuItem.getMenuInfo();
            if (menuInfo == null || (getTargetView(menuInfo) instanceof HistoryItem) || !(menuItem.getMenuInfo() instanceof AdapterView.AdapterContextMenuInfo)) {
                return false;
            }
            deleteSnapshot(((AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo()).id);
            return true;
        }
        return super.onContextItemSelected(menuItem);
    }

    @Override // android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mCallback = (CombinedBookmarksCallbacks) getActivity();
        this.mAnimateId = getArguments().getLong("animate_id");
    }

    @Override // android.app.Fragment, android.view.View.OnCreateContextMenuListener
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        getActivity().getMenuInflater().inflate(2131755015, contextMenu);
        BookmarkItem bookmarkItem = new BookmarkItem(getActivity());
        bookmarkItem.setEnableScrolling(true);
        populateBookmarkItem(this.mAdapter.getItem(((AdapterView.AdapterContextMenuInfo) contextMenuInfo).position), bookmarkItem);
        contextMenu.setHeaderView(bookmarkItem);
    }

    @Override // android.app.LoaderManager.LoaderCallbacks
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (i == 1) {
            return new CursorLoader(getActivity(), SnapshotProvider.Snapshots.CONTENT_URI, PROJECTION, null, null, "date_created DESC");
        }
        return null;
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(2130968622, viewGroup, false);
        this.mEmpty = inflate.findViewById(16908292);
        this.mGrid = (GridView) inflate.findViewById(2131558432);
        setupGrid(layoutInflater);
        getLoaderManager().initLoader(1, null, this);
        return inflate;
    }

    @Override // android.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        getLoaderManager().destroyLoader(1);
        if (this.mAdapter != null) {
            this.mAdapter.changeCursor(null);
            this.mAdapter = null;
        }
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
        Cursor item = this.mAdapter.getItem(i);
        String string = item.getString(1);
        String str = "file://" + item.getString(7);
        if (item.getInt(9) == -1) {
            str = item.getString(5);
        }
        this.mCallback.openSnapshot(j, string, str);
    }

    @Override // android.app.LoaderManager.LoaderCallbacks
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == 1) {
            if (this.mAdapter == null) {
                this.mAdapter = new SnapshotAdapter(getActivity(), cursor);
                this.mGrid.setAdapter((ListAdapter) this.mAdapter);
            } else {
                this.mAdapter.changeCursor(cursor);
            }
            if (this.mAnimateId > 0) {
                this.mAdapter.animateIn(this.mAnimateId);
                this.mAnimateId = 0L;
                getArguments().remove("animate_id");
            }
            boolean isEmpty = this.mAdapter.isEmpty();
            this.mGrid.setVisibility(isEmpty ? 8 : 0);
            this.mEmpty.setVisibility(isEmpty ? 0 : 8);
        }
    }

    @Override // android.app.LoaderManager.LoaderCallbacks
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    void setupGrid(LayoutInflater layoutInflater) {
        View inflate = layoutInflater.inflate(2130968621, (ViewGroup) this.mGrid, false);
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        inflate.measure(makeMeasureSpec, makeMeasureSpec);
        this.mGrid.setColumnWidth(inflate.getMeasuredWidth());
        this.mGrid.setOnItemClickListener(this);
        this.mGrid.setOnCreateContextMenuListener(this);
    }
}
