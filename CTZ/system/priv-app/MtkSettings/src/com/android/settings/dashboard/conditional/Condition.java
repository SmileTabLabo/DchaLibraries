package com.android.settings.dashboard.conditional;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.PersistableBundle;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
/* loaded from: classes.dex */
public abstract class Condition {
    private boolean mIsActive;
    private boolean mIsSilenced;
    private long mLastStateChange;
    protected final ConditionManager mManager;
    protected final MetricsFeatureProvider mMetricsFeatureProvider;
    protected boolean mReceiverRegistered;

    public abstract CharSequence[] getActions();

    public abstract Drawable getIcon();

    public abstract int getMetricsConstant();

    public abstract CharSequence getSummary();

    public abstract CharSequence getTitle();

    public abstract void onActionClick(int i);

    public abstract void onPrimaryClick();

    public abstract void refreshState();

    /* JADX INFO: Access modifiers changed from: package-private */
    public Condition(ConditionManager conditionManager) {
        this(conditionManager, FeatureFactory.getFactory(conditionManager.getContext()).getMetricsFeatureProvider());
    }

    Condition(ConditionManager conditionManager, MetricsFeatureProvider metricsFeatureProvider) {
        this.mManager = conditionManager;
        this.mMetricsFeatureProvider = metricsFeatureProvider;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void restoreState(PersistableBundle persistableBundle) {
        this.mIsSilenced = persistableBundle.getBoolean("silence");
        this.mIsActive = persistableBundle.getBoolean("active");
        this.mLastStateChange = persistableBundle.getLong("last_state");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean saveState(PersistableBundle persistableBundle) {
        if (this.mIsSilenced) {
            persistableBundle.putBoolean("silence", this.mIsSilenced);
        }
        if (this.mIsActive) {
            persistableBundle.putBoolean("active", this.mIsActive);
            persistableBundle.putLong("last_state", this.mLastStateChange);
        }
        return this.mIsSilenced || this.mIsActive;
    }

    protected void notifyChanged() {
        this.mManager.notifyChanged(this);
    }

    public boolean isSilenced() {
        return this.mIsSilenced;
    }

    public boolean isActive() {
        return this.mIsActive;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setActive(boolean z) {
        if (this.mIsActive == z) {
            return;
        }
        this.mIsActive = z;
        this.mLastStateChange = System.currentTimeMillis();
        if (this.mIsSilenced && !z) {
            this.mIsSilenced = false;
            onSilenceChanged(this.mIsSilenced);
        }
        notifyChanged();
    }

    public void silence() {
        if (!this.mIsSilenced) {
            this.mIsSilenced = true;
            this.mMetricsFeatureProvider.action(this.mManager.getContext(), 372, getMetricsConstant());
            onSilenceChanged(this.mIsSilenced);
            notifyChanged();
        }
    }

    void onSilenceChanged(boolean z) {
        BroadcastReceiver receiver = getReceiver();
        if (receiver == null) {
            return;
        }
        if (z) {
            if (!this.mReceiverRegistered) {
                this.mManager.getContext().registerReceiver(receiver, getIntentFilter());
                this.mReceiverRegistered = true;
            }
        } else if (this.mReceiverRegistered) {
            this.mManager.getContext().unregisterReceiver(receiver);
            this.mReceiverRegistered = false;
        }
    }

    protected BroadcastReceiver getReceiver() {
        return null;
    }

    protected IntentFilter getIntentFilter() {
        return null;
    }

    public boolean shouldShow() {
        return isActive() && !isSilenced();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public long getLastChange() {
        return this.mLastStateChange;
    }

    public void onResume() {
    }

    public void onPause() {
    }
}
