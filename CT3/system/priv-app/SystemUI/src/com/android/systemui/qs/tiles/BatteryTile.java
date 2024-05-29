package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.BenesseExtension;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settingslib.BatteryInfo;
import com.android.settingslib.graph.UsageView;
import com.android.systemui.BatteryMeterDrawable;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.BatteryController;
import java.text.NumberFormat;
/* loaded from: a.zip:com/android/systemui/qs/tiles/BatteryTile.class */
public class BatteryTile extends QSTile<QSTile.State> implements BatteryController.BatteryStateChangeCallback {
    private final BatteryController mBatteryController;
    private final BatteryDetail mBatteryDetail;
    private boolean mCharging;
    private boolean mDetailShown;
    private int mLevel;
    private boolean mPluggedIn;
    private boolean mPowerSave;

    /* loaded from: a.zip:com/android/systemui/qs/tiles/BatteryTile$BatteryDetail.class */
    private final class BatteryDetail implements QSTile.DetailAdapter, View.OnClickListener, View.OnAttachStateChangeListener {
        private View mCurrentView;
        private final BatteryMeterDrawable mDrawable;
        private final BroadcastReceiver mReceiver;
        final BatteryTile this$0;

        private BatteryDetail(BatteryTile batteryTile) {
            this.this$0 = batteryTile;
            this.mDrawable = new BatteryMeterDrawable(this.this$0.mHost.getContext(), new Handler(), this.this$0.mHost.getContext().getColor(2131558514));
            this.mReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.qs.tiles.BatteryTile.BatteryDetail.1
                final BatteryDetail this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    this.this$1.postBindView();
                }
            };
        }

        /* synthetic */ BatteryDetail(BatteryTile batteryTile, BatteryDetail batteryDetail) {
            this(batteryTile);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void bindBatteryInfo(BatteryInfo batteryInfo) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(batteryInfo.batteryPercentString, new RelativeSizeSpan(2.6f), 17);
            if (batteryInfo.remainingLabel != null) {
                if (this.this$0.mContext.getResources().getBoolean(2131623953)) {
                    spannableStringBuilder.append(' ');
                } else {
                    spannableStringBuilder.append('\n');
                }
                spannableStringBuilder.append((CharSequence) batteryInfo.remainingLabel);
            }
            ((TextView) this.mCurrentView.findViewById(2131886256)).setText(spannableStringBuilder);
            batteryInfo.bindHistory((UsageView) this.mCurrentView.findViewById(2131886257), new BatteryInfo.BatteryDataParser[0]);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void bindView() {
            if (this.mCurrentView == null) {
                return;
            }
            this.mDrawable.onBatteryLevelChanged(100, false, false);
            this.mDrawable.onPowerSaveChanged(true);
            this.mDrawable.disableShowPercent();
            ((ImageView) this.mCurrentView.findViewById(16908294)).setImageDrawable(this.mDrawable);
            ((Checkable) this.mCurrentView.findViewById(16908311)).setChecked(this.this$0.mPowerSave);
            BatteryInfo.getBatteryInfo(this.this$0.mContext, new BatteryInfo.Callback(this) { // from class: com.android.systemui.qs.tiles.BatteryTile.BatteryDetail.3
                final BatteryDetail this$1;

                {
                    this.this$1 = this;
                }

                @Override // com.android.settingslib.BatteryInfo.Callback
                public void onBatteryInfoLoaded(BatteryInfo batteryInfo) {
                    if (this.this$1.mCurrentView != null) {
                        this.this$1.bindBatteryInfo(batteryInfo);
                    }
                }
            });
            TextView textView = (TextView) this.mCurrentView.findViewById(16908310);
            TextView textView2 = (TextView) this.mCurrentView.findViewById(16908304);
            if (this.this$0.mCharging) {
                this.mCurrentView.findViewById(2131886258).setAlpha(0.7f);
                textView.setTextSize(2, 14.0f);
                textView.setText(2131493798);
                this.mCurrentView.findViewById(16908311).setVisibility(8);
                this.mCurrentView.findViewById(2131886258).setClickable(false);
                return;
            }
            this.mCurrentView.findViewById(2131886258).setAlpha(1.0f);
            textView.setTextSize(2, 16.0f);
            textView.setText(2131493799);
            textView2.setText(2131493800);
            this.mCurrentView.findViewById(16908311).setVisibility(0);
            this.mCurrentView.findViewById(2131886258).setClickable(true);
            this.mCurrentView.findViewById(2131886258).setOnClickListener(this);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void postBindView() {
            if (this.mCurrentView == null) {
                return;
            }
            this.mCurrentView.post(new Runnable(this) { // from class: com.android.systemui.qs.tiles.BatteryTile.BatteryDetail.2
                final BatteryDetail this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.bindView();
                }
            });
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            View view2 = view;
            if (view == null) {
                view2 = LayoutInflater.from(this.this$0.mContext).inflate(2130968604, viewGroup, false);
            }
            this.mCurrentView = view2;
            this.mCurrentView.addOnAttachStateChangeListener(this);
            bindView();
            return view2;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public int getMetricsCategory() {
            return 274;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public Intent getSettingsIntent() {
            if (BenesseExtension.getDchaState() != 0) {
                return null;
            }
            return new Intent("android.intent.action.POWER_USAGE_SUMMARY");
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public CharSequence getTitle() {
            return this.this$0.mContext.getString(2131493797, Integer.valueOf(this.this$0.mLevel));
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public Boolean getToggleState() {
            return null;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.this$0.mBatteryController.setPowerSaveMode(!this.this$0.mPowerSave);
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View view) {
            if (this.this$0.mDetailShown) {
                return;
            }
            this.this$0.mDetailShown = true;
            view.getContext().registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.TIME_TICK"));
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View view) {
            if (this.this$0.mDetailShown) {
                this.this$0.mDetailShown = false;
                view.getContext().unregisterReceiver(this.mReceiver);
            }
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public void setToggleState(boolean z) {
        }
    }

    public BatteryTile(QSTile.Host host) {
        super(host);
        this.mBatteryDetail = new BatteryDetail(this, null);
        this.mBatteryController = host.getBatteryController();
    }

    @Override // com.android.systemui.qs.QSTile
    public QSTile.DetailAdapter getDetailAdapter() {
        return this.mBatteryDetail;
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        return new Intent("android.intent.action.POWER_USAGE_SUMMARY");
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 261;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(2131493847);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        showDetail(true);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleUpdateState(QSTile.State state, Object obj) {
        String format = NumberFormat.getPercentInstance().format((obj != null ? ((Integer) obj).intValue() : this.mLevel) / 100.0d);
        state.icon = new QSTile.Icon(this) { // from class: com.android.systemui.qs.tiles.BatteryTile.1
            final BatteryTile this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.qs.QSTile.Icon
            public Drawable getDrawable(Context context) {
                BatteryMeterDrawable batteryMeterDrawable = new BatteryMeterDrawable(context, new Handler(Looper.getMainLooper()), context.getColor(2131558514));
                batteryMeterDrawable.onBatteryLevelChanged(this.this$0.mLevel, this.this$0.mPluggedIn, this.this$0.mCharging);
                batteryMeterDrawable.onPowerSaveChanged(this.this$0.mPowerSave);
                return batteryMeterDrawable;
            }

            @Override // com.android.systemui.qs.QSTile.Icon
            public int getPadding() {
                return this.this$0.mHost.getContext().getResources().getDimensionPixelSize(2131689859);
            }
        };
        state.label = format;
        state.contentDescription = this.mContext.getString(2131493454, format) + "," + (this.mPowerSave ? this.mContext.getString(2131493649) : this.mCharging ? this.mContext.getString(2131493593) : "") + "," + this.mContext.getString(2131493423);
        String name = Button.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
    }

    @Override // com.android.systemui.qs.QSTile
    public QSTile.State newTileState() {
        return new QSTile.State();
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        this.mLevel = i;
        this.mPluggedIn = z;
        this.mCharging = z2;
        refreshState(Integer.valueOf(i));
        if (this.mDetailShown) {
            this.mBatteryDetail.postBindView();
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
        this.mPowerSave = z;
        refreshState(null);
        if (this.mDetailShown) {
            this.mBatteryDetail.postBindView();
        }
    }

    @Override // com.android.systemui.qs.QSTile
    public void setDetailListening(boolean z) {
        super.setDetailListening(z);
        if (z) {
            return;
        }
        this.mBatteryDetail.mCurrentView = null;
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (z) {
            this.mBatteryController.addStateChangedCallback(this);
        } else {
            this.mBatteryController.removeStateChangedCallback(this);
        }
    }
}
