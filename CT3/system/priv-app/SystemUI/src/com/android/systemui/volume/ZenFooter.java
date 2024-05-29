package com.android.systemui.volume;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Context;
import android.service.notification.ZenModeConfig;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.statusbar.policy.ZenModeController;
import java.util.Objects;
/* loaded from: a.zip:com/android/systemui/volume/ZenFooter.class */
public class ZenFooter extends LinearLayout {
    private static final String TAG = Util.logTag(ZenFooter.class);
    private ZenModeConfig mConfig;
    private final Context mContext;
    private ZenModeController mController;
    private TextView mEndNowButton;
    private ImageView mIcon;
    private final SpTexts mSpTexts;
    private TextView mSummaryLine1;
    private TextView mSummaryLine2;
    private int mZen;
    private final ZenModeController.Callback mZenCallback;

    public ZenFooter(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mZen = -1;
        this.mZenCallback = new ZenModeController.Callback(this) { // from class: com.android.systemui.volume.ZenFooter.1
            final ZenFooter this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
            public void onConfigChanged(ZenModeConfig zenModeConfig) {
                this.this$0.setConfig(zenModeConfig);
            }

            @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
            public void onZenChanged(int i) {
                this.this$0.setZen(i);
            }
        };
        this.mContext = context;
        this.mSpTexts = new SpTexts(this.mContext);
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(new ValueAnimator().getDuration() / 2);
        setLayoutTransition(layoutTransition);
    }

    private boolean isZenAlarms() {
        return this.mZen == 3;
    }

    private boolean isZenNone() {
        return this.mZen == 2;
    }

    private boolean isZenPriority() {
        boolean z = true;
        if (this.mZen != 1) {
            z = false;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setConfig(ZenModeConfig zenModeConfig) {
        if (Objects.equals(this.mConfig, zenModeConfig)) {
            return;
        }
        this.mConfig = zenModeConfig;
        update();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setZen(int i) {
        if (this.mZen == i) {
            return;
        }
        this.mZen = i;
        update();
    }

    public void cleanup() {
        this.mController.removeCallback(this.mZenCallback);
    }

    public void init(ZenModeController zenModeController) {
        this.mEndNowButton.setOnClickListener(new View.OnClickListener(this, zenModeController) { // from class: com.android.systemui.volume.ZenFooter.2
            final ZenFooter this$0;
            final ZenModeController val$controller;

            {
                this.this$0 = this;
                this.val$controller = zenModeController;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.val$controller.setZen(0, null, ZenFooter.TAG);
            }
        });
        this.mZen = zenModeController.getZen();
        this.mConfig = zenModeController.getConfig();
        this.mController = zenModeController;
        this.mController.addCallback(this.mZenCallback);
        update();
    }

    public void onConfigurationChanged() {
        Util.setText(this.mEndNowButton, this.mContext.getString(2131493681));
        this.mSpTexts.update();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mIcon = (ImageView) findViewById(2131886759);
        this.mSummaryLine1 = (TextView) findViewById(2131886760);
        this.mSummaryLine2 = (TextView) findViewById(2131886761);
        this.mEndNowButton = (TextView) findViewById(2131886762);
        this.mSpTexts.add(this.mSummaryLine1);
        this.mSpTexts.add(this.mSummaryLine2);
        this.mSpTexts.add(this.mEndNowButton);
    }

    public void update() {
        this.mIcon.setImageResource(isZenNone() ? 2130837643 : 2130837640);
        Util.setText(this.mSummaryLine1, isZenPriority() ? this.mContext.getString(2131493614) : isZenAlarms() ? this.mContext.getString(2131493615) : isZenNone() ? this.mContext.getString(2131493613) : null);
        Util.setText(this.mSummaryLine2, (this.mConfig == null || this.mConfig.manualRule == null) ? false : this.mConfig.manualRule.conditionId == null ? this.mContext.getString(17040820) : ZenModeConfig.getConditionSummary(this.mContext, this.mConfig, this.mController.getCurrentUser(), true));
    }
}
