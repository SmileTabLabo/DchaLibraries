package com.android.systemui.tuner;

import android.content.Context;
import android.content.Intent;
import android.os.BenesseExtension;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.Prefs;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.volume.ZenModePanel;
/* loaded from: a.zip:com/android/systemui/tuner/TunerZenModePanel.class */
public class TunerZenModePanel extends LinearLayout implements View.OnClickListener {
    private View mButtons;
    private ZenModePanel.Callback mCallback;
    private ZenModeController mController;
    private View mDone;
    private View.OnClickListener mDoneListener;
    private boolean mEditing;
    private View mHeaderSwitch;
    private View mMoreSettings;
    private final Runnable mUpdate;
    private int mZenMode;
    private ZenModePanel mZenModePanel;

    public TunerZenModePanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mUpdate = new Runnable(this) { // from class: com.android.systemui.tuner.TunerZenModePanel.1
            final TunerZenModePanel this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.updatePanel();
            }
        };
    }

    private void postUpdatePanel() {
        removeCallbacks(this.mUpdate);
        postDelayed(this.mUpdate, 40L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePanel() {
        boolean z = this.mZenMode != 0;
        ((Checkable) this.mHeaderSwitch.findViewById(16908311)).setChecked(z);
        this.mZenModePanel.setVisibility(z ? 0 : 8);
        this.mButtons.setVisibility(z ? 0 : 8);
    }

    public void init(ZenModeController zenModeController) {
        this.mController = zenModeController;
        this.mHeaderSwitch = findViewById(2131886727);
        this.mHeaderSwitch.setVisibility(0);
        this.mHeaderSwitch.setOnClickListener(this);
        this.mHeaderSwitch.findViewById(16908363).setVisibility(8);
        ((TextView) this.mHeaderSwitch.findViewById(16908310)).setText(2131493522);
        this.mZenModePanel = (ZenModePanel) findViewById(2131886763);
        this.mZenModePanel.init(zenModeController);
        this.mButtons = findViewById(2131886728);
        this.mMoreSettings = this.mButtons.findViewById(16908314);
        this.mMoreSettings.setOnClickListener(this);
        ((TextView) this.mMoreSettings).setText(2131493563);
        this.mDone = this.mButtons.findViewById(16908313);
        this.mDone.setOnClickListener(this);
        ((TextView) this.mDone).setText(2131493564);
    }

    public boolean isEditing() {
        return this.mEditing;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.mHeaderSwitch) {
            this.mEditing = true;
            if (this.mZenMode == 0) {
                this.mZenMode = Prefs.getInt(this.mContext, "DndFavoriteZen", 3);
                this.mController.setZen(this.mZenMode, null, "TunerZenModePanel");
                postUpdatePanel();
                return;
            }
            this.mZenMode = 0;
            this.mController.setZen(0, null, "TunerZenModePanel");
            postUpdatePanel();
        } else if (view == this.mMoreSettings && BenesseExtension.getDchaState() == 0) {
            Intent intent = new Intent("android.settings.ZEN_MODE_SETTINGS");
            intent.addFlags(268435456);
            getContext().startActivity(intent);
        } else if (view == this.mDone) {
            this.mEditing = false;
            setVisibility(8);
            this.mDoneListener.onClick(view);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mEditing = false;
    }

    public void setCallback(ZenModePanel.Callback callback) {
        this.mCallback = callback;
        this.mZenModePanel.setCallback(callback);
    }

    public void setDoneListener(View.OnClickListener onClickListener) {
        this.mDoneListener = onClickListener;
    }

    public void setZenState(int i) {
        this.mZenMode = i;
        postUpdatePanel();
    }
}
