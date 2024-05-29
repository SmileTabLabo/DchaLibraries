package androidx.car.widget;

import android.car.drivingstate.CarUxRestrictions;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.car.R;
import androidx.car.utils.CarUxRestrictionsUtils;
import androidx.car.widget.ListItem;
import androidx.car.widget.SubheaderListItem;
import java.util.List;
/* loaded from: classes.dex */
public class SubheaderListItem extends ListItem<ViewHolder> {
    private final List<ListItem.ViewBinder<ViewHolder>> mBinders;
    private final Context mContext;
    private boolean mIsEnabled;
    private int mListItemSubheaderTextAppearance;
    private String mText;
    private int mTextStartMarginType;

    public static ViewHolder createViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override // androidx.car.widget.ListItem
    public int getViewType() {
        return 3;
    }

    @Override // androidx.car.widget.ListItem
    protected void resolveDirtyState() {
        this.mBinders.clear();
        setItemLayoutHeight();
        setText();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.car.widget.ListItem
    public void onBind(ViewHolder viewHolder) {
        for (ListItem.ViewBinder binder : this.mBinders) {
            binder.bind(viewHolder);
        }
        viewHolder.getText().setEnabled(this.mIsEnabled);
    }

    private void setItemLayoutHeight() {
        final int height = this.mContext.getResources().getDimensionPixelSize(R.dimen.car_sub_header_height);
        this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$SubheaderListItem$pX63PRVy60h4-UCc1K6ZIvA0_uw
            @Override // androidx.car.widget.ListItem.ViewBinder
            public final void bind(Object obj) {
                SubheaderListItem.lambda$setItemLayoutHeight$34(height, (SubheaderListItem.ViewHolder) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$setItemLayoutHeight$34(int i, ViewHolder vh) {
        vh.itemView.getLayoutParams().height = i;
        vh.itemView.requestLayout();
    }

    private void setText() {
        final int textStartMarginDimen;
        switch (this.mTextStartMarginType) {
            case 0:
                textStartMarginDimen = R.dimen.car_keyline_1;
                break;
            case 1:
                textStartMarginDimen = R.dimen.car_keyline_3;
                break;
            case 2:
                textStartMarginDimen = R.dimen.car_keyline_4;
                break;
            default:
                throw new IllegalStateException("Unknown text start margin type.");
        }
        this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$SubheaderListItem$k9KJJv38Lo9m6s3jbPUlTHuO2bE
            @Override // androidx.car.widget.ListItem.ViewBinder
            public final void bind(Object obj) {
                SubheaderListItem.lambda$setText$35(SubheaderListItem.this, textStartMarginDimen, (SubheaderListItem.ViewHolder) obj);
            }
        });
    }

    public static /* synthetic */ void lambda$setText$35(SubheaderListItem subheaderListItem, int i, ViewHolder vh) {
        vh.getText().setText(subheaderListItem.mText);
        vh.getText().setTextAppearance(subheaderListItem.mListItemSubheaderTextAppearance);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) vh.getText().getLayoutParams();
        layoutParams.setMarginStart(subheaderListItem.mContext.getResources().getDimensionPixelSize(i));
        vh.getText().requestLayout();
    }

    /* loaded from: classes.dex */
    public static class ViewHolder extends ListItem.ViewHolder {
        private TextView mText;

        public ViewHolder(View itemView) {
            super(itemView);
            this.mText = (TextView) itemView.findViewById(R.id.text);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // androidx.car.widget.ListItem.ViewHolder
        public void applyUxRestrictions(CarUxRestrictions restrictions) {
            CarUxRestrictionsUtils.apply(this.itemView.getContext(), restrictions, getText());
        }

        public TextView getText() {
            return this.mText;
        }
    }
}
