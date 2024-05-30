package androidx.car.widget;

import android.car.drivingstate.CarUxRestrictions;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import androidx.car.R;
import androidx.car.widget.ListItem.ViewHolder;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public abstract class ListItem<VH extends ViewHolder> {
    private boolean mDirty;
    private boolean mHideDivider;
    private final List<ViewBinder<VH>> mCustomBinders = new ArrayList();
    private final List<ViewBinder<VH>> mCustomBinderCleanUps = new ArrayList();
    private int mTitleTextAppearance = R.style.TextAppearance_Car_Body1;
    private int mBodyTextAppearance = R.style.TextAppearance_Car_Body2;

    /* loaded from: classes.dex */
    public interface ViewBinder<VH> {
        void bind(VH vh);
    }

    public abstract int getViewType();

    protected abstract void onBind(VH vh);

    protected abstract void resolveDirtyState();

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void bind(VH viewHolder) {
        viewHolder.cleanUp();
        for (ViewBinder cleanUp : this.mCustomBinderCleanUps) {
            viewHolder.addCleanUp(cleanUp);
        }
        if (isDirty()) {
            resolveDirtyState();
            markClean();
        }
        onBind(viewHolder);
        for (ViewBinder<VH> binder : this.mCustomBinders) {
            binder.bind(viewHolder);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setTitleTextAppearance(int titleTextAppearance) {
        this.mTitleTextAppearance = titleTextAppearance;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setBodyTextAppearance(int bodyTextAppearance) {
        this.mBodyTextAppearance = bodyTextAppearance;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final int getTitleTextAppearance() {
        return this.mTitleTextAppearance;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final int getBodyTextAppearance() {
        return this.mBodyTextAppearance;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void markDirty() {
        this.mDirty = true;
    }

    protected void markClean() {
        this.mDirty = false;
    }

    protected boolean isDirty() {
        return this.mDirty;
    }

    public boolean shouldHideDivider() {
        return this.mHideDivider;
    }

    /* loaded from: classes.dex */
    public static abstract class ViewHolder extends RecyclerView.ViewHolder {
        private final List<ViewBinder> mCleanUps;

        /* JADX INFO: Access modifiers changed from: protected */
        public abstract void applyUxRestrictions(CarUxRestrictions carUxRestrictions);

        public ViewHolder(View itemView) {
            super(itemView);
            this.mCleanUps = new ArrayList();
        }

        public final void cleanUp() {
            for (ViewBinder binder : this.mCleanUps) {
                binder.bind(this);
            }
        }

        public final void addCleanUp(ViewBinder<ViewHolder> cleanUp) {
            if (cleanUp != null) {
                this.mCleanUps.add(cleanUp);
            }
        }
    }
}
