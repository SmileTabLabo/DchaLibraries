package com.android.settingslib.wifi;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Looper;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.settingslib.R$attr;
import com.android.settingslib.R$string;
/* loaded from: a.zip:com/android/settingslib/wifi/AccessPointPreference.class */
public class AccessPointPreference extends Preference {
    private AccessPoint mAccessPoint;
    private Drawable mBadge;
    private final UserBadgeCache mBadgeCache;
    private final int mBadgePadding;
    private CharSequence mContentDescription;
    private boolean mForSavedNetworks;
    private int mLevel;
    private final Runnable mNotifyChanged;
    private TextView mTitleView;
    private final StateListDrawable mWifiSld;
    private static final int[] STATE_SECURED = {R$attr.state_encrypted};
    private static final int[] STATE_NONE = new int[0];
    private static int[] wifi_signal_attributes = {R$attr.wifi_signal};
    static final int[] WIFI_CONNECTION_STRENGTH = {R$string.accessibility_wifi_one_bar, R$string.accessibility_wifi_two_bars, R$string.accessibility_wifi_three_bars, R$string.accessibility_wifi_signal_full};

    /* loaded from: a.zip:com/android/settingslib/wifi/AccessPointPreference$UserBadgeCache.class */
    public static class UserBadgeCache {
    }

    public AccessPointPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mForSavedNetworks = false;
        this.mNotifyChanged = new Runnable(this) { // from class: com.android.settingslib.wifi.AccessPointPreference.1
            final AccessPointPreference this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.notifyChanged();
            }
        };
        this.mWifiSld = null;
        this.mBadgePadding = 0;
        this.mBadgeCache = null;
    }

    private void postNotifyChanged() {
        if (this.mTitleView != null) {
            this.mTitleView.post(this.mNotifyChanged);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void notifyChanged() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            postNotifyChanged();
        } else {
            super.notifyChanged();
        }
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        if (this.mAccessPoint == null) {
            return;
        }
        Drawable icon = getIcon();
        if (icon != null) {
            icon.setLevel(this.mLevel);
        }
        this.mTitleView = (TextView) preferenceViewHolder.findViewById(16908310);
        if (this.mTitleView != null) {
            this.mTitleView.setCompoundDrawablesRelativeWithIntrinsicBounds((Drawable) null, (Drawable) null, this.mBadge, (Drawable) null);
            this.mTitleView.setCompoundDrawablePadding(this.mBadgePadding);
        }
        preferenceViewHolder.itemView.setContentDescription(this.mContentDescription);
    }
}
