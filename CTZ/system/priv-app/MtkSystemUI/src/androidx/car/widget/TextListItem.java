package androidx.car.widget;

import android.car.drivingstate.CarUxRestrictions;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.car.R;
import androidx.car.utils.CarUxRestrictionsUtils;
import androidx.car.widget.ListItem;
import androidx.car.widget.TextListItem;
import java.util.List;
/* loaded from: classes.dex */
public class TextListItem extends ListItem<ViewHolder> {
    private View.OnClickListener mAction1OnClickListener;
    private String mAction1Text;
    private View.OnClickListener mAction2OnClickListener;
    private String mAction2Text;
    private final List<ListItem.ViewBinder<ViewHolder>> mBinders;
    private String mBody;
    private final Context mContext;
    private boolean mIsBodyPrimary;
    private boolean mIsEnabled;
    private View.OnClickListener mOnClickListener;
    private Drawable mPrimaryActionIconDrawable;
    private int mPrimaryActionIconSize;
    private int mPrimaryActionType;
    private boolean mShowAction1Divider;
    private boolean mShowAction2Divider;
    private boolean mShowSupplementalIconDivider;
    private boolean mShowSwitchDivider;
    private int mSupplementalActionType;
    private Drawable mSupplementalIconDrawable;
    private View.OnClickListener mSupplementalIconOnClickListener;
    private boolean mSwitchChecked;
    private CompoundButton.OnCheckedChangeListener mSwitchOnCheckedChangeListener;
    private String mTitle;

    public static ViewHolder createViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override // androidx.car.widget.ListItem
    public int getViewType() {
        return 1;
    }

    @Override // androidx.car.widget.ListItem
    protected void resolveDirtyState() {
        this.mBinders.clear();
        setItemLayoutHeight();
        setPrimaryAction();
        setText();
        setSupplementalActions();
        setOnClickListener();
    }

    @Override // androidx.car.widget.ListItem
    public void onBind(ViewHolder viewHolder) {
        View[] widgetViews;
        hideSubViews(viewHolder);
        for (ListItem.ViewBinder binder : this.mBinders) {
            binder.bind(viewHolder);
        }
        for (View v : viewHolder.getWidgetViews()) {
            v.setEnabled(this.mIsEnabled);
        }
        viewHolder.itemView.setEnabled(this.mIsEnabled);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // androidx.car.widget.ListItem
    public void setTitleTextAppearance(int titleTextAppearance) {
        super.setTitleTextAppearance(titleTextAppearance);
        setTextContent();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // androidx.car.widget.ListItem
    public void setBodyTextAppearance(int bodyTextAppearance) {
        super.setBodyTextAppearance(bodyTextAppearance);
        setTextContent();
    }

    private void hideSubViews(ViewHolder vh) {
        View[] widgetViews;
        for (View v : vh.getWidgetViews()) {
            v.setVisibility(8);
        }
    }

    private void setItemLayoutHeight() {
        if (TextUtils.isEmpty(this.mBody)) {
            final int height = this.mContext.getResources().getDimensionPixelSize(R.dimen.car_single_line_list_item_height);
            this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$jlDxX8iWVyw9PLzOPOSdexv_SeA
                @Override // androidx.car.widget.ListItem.ViewBinder
                public final void bind(Object obj) {
                    TextListItem.lambda$setItemLayoutHeight$9(height, (TextListItem.ViewHolder) obj);
                }
            });
            return;
        }
        final int minHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.car_double_line_list_item_height);
        this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$4OJISyXQlZZoDXsL4UvKKaIQ8pg
            @Override // androidx.car.widget.ListItem.ViewBinder
            public final void bind(Object obj) {
                TextListItem.lambda$setItemLayoutHeight$10(minHeight, (TextListItem.ViewHolder) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$setItemLayoutHeight$9(int i, ViewHolder vh) {
        ViewGroup.LayoutParams layoutParams = vh.itemView.getLayoutParams();
        layoutParams.height = i;
        vh.itemView.requestLayout();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$setItemLayoutHeight$10(int i, ViewHolder vh) {
        vh.itemView.setMinimumHeight(i);
        vh.getContainerLayout().setMinimumHeight(i);
        ViewGroup.LayoutParams layoutParams = vh.itemView.getLayoutParams();
        layoutParams.height = -2;
        vh.itemView.requestLayout();
    }

    private void setPrimaryAction() {
        setPrimaryIconContent();
        setPrimaryIconLayout();
    }

    private void setText() {
        setTextContent();
        setTextVerticalMargin();
        setTextStartMargin();
        setTextEndLayout();
    }

    private void setOnClickListener() {
        this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$1PuGCq5Cyi-OnogDSiOc6QykmXg
            @Override // androidx.car.widget.ListItem.ViewBinder
            public final void bind(Object obj) {
                TextListItem.lambda$setOnClickListener$11(TextListItem.this, (TextListItem.ViewHolder) obj);
            }
        });
    }

    public static /* synthetic */ void lambda$setOnClickListener$11(TextListItem textListItem, ViewHolder vh) {
        vh.itemView.setOnClickListener(textListItem.mOnClickListener);
        vh.itemView.setClickable(textListItem.mOnClickListener != null);
    }

    private void setPrimaryIconContent() {
        switch (this.mPrimaryActionType) {
            case 0:
            case 1:
                return;
            case 2:
                this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$QkyLhTnr2AZU1cOvZl2jTm88nDs
                    @Override // androidx.car.widget.ListItem.ViewBinder
                    public final void bind(Object obj) {
                        TextListItem.lambda$setPrimaryIconContent$12(TextListItem.this, (TextListItem.ViewHolder) obj);
                    }
                });
                return;
            default:
                throw new IllegalStateException("Unknown primary action type.");
        }
    }

    public static /* synthetic */ void lambda$setPrimaryIconContent$12(TextListItem textListItem, ViewHolder vh) {
        vh.getPrimaryIcon().setVisibility(0);
        vh.getPrimaryIcon().setImageDrawable(textListItem.mPrimaryActionIconDrawable);
    }

    private void setPrimaryIconLayout() {
        int sizeResId;
        final int startMargin;
        if (this.mPrimaryActionType == 1 || this.mPrimaryActionType == 0) {
            return;
        }
        switch (this.mPrimaryActionIconSize) {
            case 0:
                sizeResId = R.dimen.car_primary_icon_size;
                break;
            case 1:
                sizeResId = R.dimen.car_avatar_icon_size;
                break;
            case 2:
                sizeResId = R.dimen.car_single_line_list_item_height;
                break;
            default:
                throw new IllegalStateException("Unknown primary action icon size.");
        }
        final int iconSize = this.mContext.getResources().getDimensionPixelSize(sizeResId);
        switch (this.mPrimaryActionIconSize) {
            case 0:
            case 1:
                startMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.car_keyline_1);
                break;
            case 2:
                startMargin = 0;
                break;
            default:
                throw new IllegalStateException("Unknown primary action icon size.");
        }
        this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$jitVVETB5LgxH-6cmLSh4bpI4Nc
            @Override // androidx.car.widget.ListItem.ViewBinder
            public final void bind(Object obj) {
                TextListItem.lambda$setPrimaryIconLayout$13(TextListItem.this, iconSize, startMargin, (TextListItem.ViewHolder) obj);
            }
        });
    }

    public static /* synthetic */ void lambda$setPrimaryIconLayout$13(TextListItem textListItem, int i, int i2, ViewHolder vh) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) vh.getPrimaryIcon().getLayoutParams();
        layoutParams.width = i;
        layoutParams.height = i;
        layoutParams.setMarginStart(i2);
        if (textListItem.mPrimaryActionIconSize == 2) {
            layoutParams.addRule(15);
            layoutParams.topMargin = 0;
        } else {
            layoutParams.removeRule(15);
            int itemHeight = textListItem.mContext.getResources().getDimensionPixelSize(R.dimen.car_double_line_list_item_height);
            layoutParams.topMargin = (itemHeight - i) / 2;
        }
        vh.getPrimaryIcon().requestLayout();
    }

    private void setTextContent() {
        if (!TextUtils.isEmpty(this.mTitle)) {
            this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$hMHjD9dvt1E3eDQaOD7fD3BslWs
                @Override // androidx.car.widget.ListItem.ViewBinder
                public final void bind(Object obj) {
                    TextListItem.lambda$setTextContent$14(TextListItem.this, (TextListItem.ViewHolder) obj);
                }
            });
        }
        if (!TextUtils.isEmpty(this.mBody)) {
            this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$aSCi-SoSVZ3RsXs84htsNyba8-8
                @Override // androidx.car.widget.ListItem.ViewBinder
                public final void bind(Object obj) {
                    TextListItem.lambda$setTextContent$15(TextListItem.this, (TextListItem.ViewHolder) obj);
                }
            });
        }
        if (this.mIsBodyPrimary) {
            this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$se0KlOTemhhwhqfZF5c066e0hC0
                @Override // androidx.car.widget.ListItem.ViewBinder
                public final void bind(Object obj) {
                    TextListItem.lambda$setTextContent$16(TextListItem.this, (TextListItem.ViewHolder) obj);
                }
            });
        } else {
            this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$6TeQsN7-Mf2dV79YoPGa0_tJGbA
                @Override // androidx.car.widget.ListItem.ViewBinder
                public final void bind(Object obj) {
                    TextListItem.lambda$setTextContent$17(TextListItem.this, (TextListItem.ViewHolder) obj);
                }
            });
        }
    }

    public static /* synthetic */ void lambda$setTextContent$14(TextListItem textListItem, ViewHolder vh) {
        vh.getTitle().setVisibility(0);
        vh.getTitle().setText(textListItem.mTitle);
    }

    public static /* synthetic */ void lambda$setTextContent$15(TextListItem textListItem, ViewHolder vh) {
        vh.getBody().setVisibility(0);
        vh.getBody().setText(textListItem.mBody);
    }

    public static /* synthetic */ void lambda$setTextContent$16(TextListItem textListItem, ViewHolder vh) {
        vh.getTitle().setTextAppearance(textListItem.getBodyTextAppearance());
        vh.getBody().setTextAppearance(textListItem.getTitleTextAppearance());
    }

    public static /* synthetic */ void lambda$setTextContent$17(TextListItem textListItem, ViewHolder vh) {
        vh.getTitle().setTextAppearance(textListItem.getTitleTextAppearance());
        vh.getBody().setTextAppearance(textListItem.getBodyTextAppearance());
    }

    private void setTextStartMargin() {
        int startMarginResId;
        switch (this.mPrimaryActionType) {
            case 0:
                startMarginResId = R.dimen.car_keyline_1;
                break;
            case 1:
                startMarginResId = R.dimen.car_keyline_3;
                break;
            case 2:
                if (this.mPrimaryActionIconSize != 2) {
                    startMarginResId = R.dimen.car_keyline_3;
                    break;
                } else {
                    startMarginResId = R.dimen.car_keyline_4;
                    break;
                }
            default:
                throw new IllegalStateException("Unknown primary action type.");
        }
        final int startMargin = this.mContext.getResources().getDimensionPixelSize(startMarginResId);
        this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$bzBjZsqv0Wn7XdN2kyq8-IemRlg
            @Override // androidx.car.widget.ListItem.ViewBinder
            public final void bind(Object obj) {
                TextListItem.lambda$setTextStartMargin$18(startMargin, (TextListItem.ViewHolder) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$setTextStartMargin$18(int i, ViewHolder vh) {
        RelativeLayout.LayoutParams titleLayoutParams = (RelativeLayout.LayoutParams) vh.getTitle().getLayoutParams();
        titleLayoutParams.setMarginStart(i);
        vh.getTitle().requestLayout();
        RelativeLayout.LayoutParams bodyLayoutParams = (RelativeLayout.LayoutParams) vh.getBody().getLayoutParams();
        bodyLayoutParams.setMarginStart(i);
        vh.getBody().requestLayout();
    }

    private void setTextVerticalMargin() {
        if (!TextUtils.isEmpty(this.mTitle) && TextUtils.isEmpty(this.mBody)) {
            this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$-X0rGQ5OC-u6gYjfdtudgJjL0uE
                @Override // androidx.car.widget.ListItem.ViewBinder
                public final void bind(Object obj) {
                    TextListItem.lambda$setTextVerticalMargin$19((TextListItem.ViewHolder) obj);
                }
            });
        } else if (TextUtils.isEmpty(this.mTitle) && !TextUtils.isEmpty(this.mBody)) {
            this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$9FvqZ6rDPbkZArZzcbagObIw4W0
                @Override // androidx.car.widget.ListItem.ViewBinder
                public final void bind(Object obj) {
                    TextListItem.lambda$setTextVerticalMargin$20(TextListItem.this, (TextListItem.ViewHolder) obj);
                }
            });
        } else {
            this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$U2StzL-VEcPgCjHHiMjs1ITF3gk
                @Override // androidx.car.widget.ListItem.ViewBinder
                public final void bind(Object obj) {
                    TextListItem.lambda$setTextVerticalMargin$21(TextListItem.this, (TextListItem.ViewHolder) obj);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$setTextVerticalMargin$19(ViewHolder vh) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) vh.getTitle().getLayoutParams();
        layoutParams.addRule(15);
        layoutParams.topMargin = 0;
        vh.getTitle().requestLayout();
    }

    public static /* synthetic */ void lambda$setTextVerticalMargin$20(TextListItem textListItem, ViewHolder vh) {
        int margin = textListItem.mContext.getResources().getDimensionPixelSize(R.dimen.car_padding_3);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) vh.getBody().getLayoutParams();
        layoutParams.addRule(15);
        layoutParams.removeRule(3);
        layoutParams.topMargin = margin;
        layoutParams.bottomMargin = margin;
        vh.getBody().requestLayout();
    }

    public static /* synthetic */ void lambda$setTextVerticalMargin$21(TextListItem textListItem, ViewHolder vh) {
        Resources resources = textListItem.mContext.getResources();
        int padding1 = resources.getDimensionPixelSize(R.dimen.car_padding_1);
        int padding3 = resources.getDimensionPixelSize(R.dimen.car_padding_3);
        RelativeLayout.LayoutParams titleLayoutParams = (RelativeLayout.LayoutParams) vh.getTitle().getLayoutParams();
        titleLayoutParams.removeRule(15);
        titleLayoutParams.topMargin = padding3;
        vh.getTitle().requestLayout();
        RelativeLayout.LayoutParams bodyLayoutParams = (RelativeLayout.LayoutParams) vh.getBody().getLayoutParams();
        bodyLayoutParams.removeRule(15);
        bodyLayoutParams.addRule(3, R.id.title);
        bodyLayoutParams.topMargin = padding1;
        bodyLayoutParams.bottomMargin = padding3;
        vh.getBody().requestLayout();
    }

    private int getSupplementalActionLeadingView() {
        switch (this.mSupplementalActionType) {
            case 0:
                return 0;
            case 1:
                if (this.mShowSupplementalIconDivider) {
                    int leadingViewId = R.id.supplemental_icon_divider;
                    return leadingViewId;
                }
                int leadingViewId2 = R.id.supplemental_icon;
                return leadingViewId2;
            case 2:
                if (this.mShowAction1Divider) {
                    int leadingViewId3 = R.id.action1_divider;
                    return leadingViewId3;
                }
                int leadingViewId4 = R.id.action1;
                return leadingViewId4;
            case 3:
                if (this.mShowAction2Divider) {
                    int leadingViewId5 = R.id.action2_divider;
                    return leadingViewId5;
                }
                int leadingViewId6 = R.id.action2;
                return leadingViewId6;
            case 4:
                if (this.mShowSwitchDivider) {
                    int leadingViewId7 = R.id.switch_divider;
                    return leadingViewId7;
                }
                int leadingViewId8 = R.id.switch_widget;
                return leadingViewId8;
            default:
                throw new IllegalStateException("Unknown supplemental action type.");
        }
    }

    private void setTextEndLayout() {
        final int leadingViewId = getSupplementalActionLeadingView();
        if (leadingViewId == 0) {
            this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$l3eaS3HeBPLgMHu-jUP8LDdjegE
                @Override // androidx.car.widget.ListItem.ViewBinder
                public final void bind(Object obj) {
                    TextListItem.lambda$setTextEndLayout$22(TextListItem.this, (TextListItem.ViewHolder) obj);
                }
            });
        } else {
            this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$1oXppzcJPh7QiVNxqP7XeOpaOMo
                @Override // androidx.car.widget.ListItem.ViewBinder
                public final void bind(Object obj) {
                    TextListItem.lambda$setTextEndLayout$23(TextListItem.this, leadingViewId, (TextListItem.ViewHolder) obj);
                }
            });
        }
    }

    public static /* synthetic */ void lambda$setTextEndLayout$22(TextListItem textListItem, ViewHolder vh) {
        Resources resources = textListItem.mContext.getResources();
        int padding = resources.getDimensionPixelSize(R.dimen.car_keyline_1);
        RelativeLayout.LayoutParams titleLayoutParams = (RelativeLayout.LayoutParams) vh.getTitle().getLayoutParams();
        titleLayoutParams.setMarginEnd(padding);
        titleLayoutParams.addRule(21);
        titleLayoutParams.removeRule(16);
        RelativeLayout.LayoutParams bodyLayoutParams = (RelativeLayout.LayoutParams) vh.getBody().getLayoutParams();
        bodyLayoutParams.setMarginEnd(padding);
        bodyLayoutParams.addRule(21);
        bodyLayoutParams.removeRule(16);
    }

    public static /* synthetic */ void lambda$setTextEndLayout$23(TextListItem textListItem, int i, ViewHolder vh) {
        Resources resources = textListItem.mContext.getResources();
        int padding = resources.getDimensionPixelSize(R.dimen.car_padding_4);
        RelativeLayout.LayoutParams titleLayoutParams = (RelativeLayout.LayoutParams) vh.getTitle().getLayoutParams();
        titleLayoutParams.setMarginEnd(padding);
        titleLayoutParams.removeRule(21);
        titleLayoutParams.addRule(16, i);
        vh.getTitle().requestLayout();
        RelativeLayout.LayoutParams bodyLayoutParams = (RelativeLayout.LayoutParams) vh.getBody().getLayoutParams();
        bodyLayoutParams.setMarginEnd(padding);
        bodyLayoutParams.removeRule(21);
        bodyLayoutParams.addRule(16, i);
        vh.getBody().requestLayout();
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    private void setSupplementalActions() {
        switch (this.mSupplementalActionType) {
            case 0:
                return;
            case 1:
                this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$lfvKTpIOsbIQJ2BKIgqNGHY5qJ4
                    @Override // androidx.car.widget.ListItem.ViewBinder
                    public final void bind(Object obj) {
                        TextListItem.lambda$setSupplementalActions$24(TextListItem.this, (TextListItem.ViewHolder) obj);
                    }
                });
                return;
            case 2:
                break;
            case 3:
                this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$q0ErhwEYjZ-60NLI4XYUdCzM10w
                    @Override // androidx.car.widget.ListItem.ViewBinder
                    public final void bind(Object obj) {
                        TextListItem.lambda$setSupplementalActions$25(TextListItem.this, (TextListItem.ViewHolder) obj);
                    }
                });
                break;
            case 4:
                this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$ru6B6lZ3eqcpsUcCwoaSRpiICUY
                    @Override // androidx.car.widget.ListItem.ViewBinder
                    public final void bind(Object obj) {
                        TextListItem.lambda$setSupplementalActions$27(TextListItem.this, (TextListItem.ViewHolder) obj);
                    }
                });
                return;
            default:
                throw new IllegalStateException("Unknown supplemental action type.");
        }
        this.mBinders.add(new ListItem.ViewBinder() { // from class: androidx.car.widget.-$$Lambda$TextListItem$8wdsszlPkmieAzX22fpMRjdFcak
            @Override // androidx.car.widget.ListItem.ViewBinder
            public final void bind(Object obj) {
                TextListItem.lambda$setSupplementalActions$26(TextListItem.this, (TextListItem.ViewHolder) obj);
            }
        });
    }

    public static /* synthetic */ void lambda$setSupplementalActions$24(TextListItem textListItem, ViewHolder vh) {
        vh.getSupplementalIcon().setVisibility(0);
        if (textListItem.mShowSupplementalIconDivider) {
            vh.getSupplementalIconDivider().setVisibility(0);
        }
        vh.getSupplementalIcon().setImageDrawable(textListItem.mSupplementalIconDrawable);
        vh.getSupplementalIcon().setOnClickListener(textListItem.mSupplementalIconOnClickListener);
        vh.getSupplementalIcon().setClickable(textListItem.mSupplementalIconOnClickListener != null);
    }

    public static /* synthetic */ void lambda$setSupplementalActions$25(TextListItem textListItem, ViewHolder vh) {
        vh.getAction2().setVisibility(0);
        if (textListItem.mShowAction2Divider) {
            vh.getAction2Divider().setVisibility(0);
        }
        vh.getAction2().setText(textListItem.mAction2Text);
        vh.getAction2().setOnClickListener(textListItem.mAction2OnClickListener);
    }

    public static /* synthetic */ void lambda$setSupplementalActions$26(TextListItem textListItem, ViewHolder vh) {
        vh.getAction1().setVisibility(0);
        if (textListItem.mShowAction1Divider) {
            vh.getAction1Divider().setVisibility(0);
        }
        vh.getAction1().setText(textListItem.mAction1Text);
        vh.getAction1().setOnClickListener(textListItem.mAction1OnClickListener);
    }

    public static /* synthetic */ void lambda$setSupplementalActions$27(TextListItem textListItem, ViewHolder vh) {
        vh.getSwitch().setVisibility(0);
        vh.getSwitch().setChecked(textListItem.mSwitchChecked);
        vh.getSwitch().setOnCheckedChangeListener(textListItem.mSwitchOnCheckedChangeListener);
        if (textListItem.mShowSwitchDivider) {
            vh.getSwitchDivider().setVisibility(0);
        }
    }

    /* loaded from: classes.dex */
    public static class ViewHolder extends ListItem.ViewHolder {
        private Button mAction1;
        private View mAction1Divider;
        private Button mAction2;
        private View mAction2Divider;
        private TextView mBody;
        private RelativeLayout mContainerLayout;
        private ImageView mPrimaryIcon;
        private ImageView mSupplementalIcon;
        private View mSupplementalIconDivider;
        private Switch mSwitch;
        private View mSwitchDivider;
        private TextView mTitle;
        private final View[] mWidgetViews;

        public ViewHolder(View itemView) {
            super(itemView);
            this.mContainerLayout = (RelativeLayout) itemView.findViewById(R.id.container);
            this.mPrimaryIcon = (ImageView) itemView.findViewById(R.id.primary_icon);
            this.mTitle = (TextView) itemView.findViewById(R.id.title);
            this.mBody = (TextView) itemView.findViewById(R.id.body);
            this.mSupplementalIcon = (ImageView) itemView.findViewById(R.id.supplemental_icon);
            this.mSupplementalIconDivider = itemView.findViewById(R.id.supplemental_icon_divider);
            this.mSwitch = (Switch) itemView.findViewById(R.id.switch_widget);
            this.mSwitchDivider = itemView.findViewById(R.id.switch_divider);
            this.mAction1 = (Button) itemView.findViewById(R.id.action1);
            this.mAction1Divider = itemView.findViewById(R.id.action1_divider);
            this.mAction2 = (Button) itemView.findViewById(R.id.action2);
            this.mAction2Divider = itemView.findViewById(R.id.action2_divider);
            int minTouchSize = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.car_touch_target_size);
            MinTouchTargetHelper.ensureThat(this.mSupplementalIcon).hasMinTouchSize(minTouchSize);
            this.mWidgetViews = new View[]{this.mPrimaryIcon, this.mTitle, this.mBody, this.mSupplementalIcon, this.mSupplementalIconDivider, this.mAction1, this.mAction1Divider, this.mAction2, this.mAction2Divider, this.mSwitch, this.mSwitchDivider};
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // androidx.car.widget.ListItem.ViewHolder
        public void applyUxRestrictions(CarUxRestrictions restrictions) {
            CarUxRestrictionsUtils.apply(this.itemView.getContext(), restrictions, getBody());
        }

        public RelativeLayout getContainerLayout() {
            return this.mContainerLayout;
        }

        public ImageView getPrimaryIcon() {
            return this.mPrimaryIcon;
        }

        public TextView getTitle() {
            return this.mTitle;
        }

        public TextView getBody() {
            return this.mBody;
        }

        public ImageView getSupplementalIcon() {
            return this.mSupplementalIcon;
        }

        public View getSupplementalIconDivider() {
            return this.mSupplementalIconDivider;
        }

        public View getSwitchDivider() {
            return this.mSwitchDivider;
        }

        public Switch getSwitch() {
            return this.mSwitch;
        }

        public Button getAction1() {
            return this.mAction1;
        }

        public View getAction1Divider() {
            return this.mAction1Divider;
        }

        public Button getAction2() {
            return this.mAction2;
        }

        public View getAction2Divider() {
            return this.mAction2Divider;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public View[] getWidgetViews() {
            return this.mWidgetViews;
        }
    }
}
