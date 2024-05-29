package com.android.settings.accessibility;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;
import com.android.settings.R;
import com.android.settings.widget.ToggleSwitch;
/* loaded from: classes.dex */
public class ToggleScreenMagnificationPreferenceFragment extends ToggleFeaturePreferenceFragment {
    protected VideoPreference mVideoPreference;

    /* loaded from: classes.dex */
    protected class VideoPreference extends Preference {
        private ViewTreeObserver.OnGlobalLayoutListener mLayoutListener;
        private ImageView mVideoBackgroundView;

        public VideoPreference(Context context) {
            super(context);
        }

        @Override // android.support.v7.preference.Preference
        public void onBindViewHolder(PreferenceViewHolder view) {
            super.onBindViewHolder(view);
            Resources res = ToggleScreenMagnificationPreferenceFragment.this.getPrefContext().getResources();
            final int backgroundAssetWidth = res.getDimensionPixelSize(R.dimen.screen_magnification_video_background_width);
            final int videoAssetWidth = res.getDimensionPixelSize(R.dimen.screen_magnification_video_width);
            final int videoAssetHeight = res.getDimensionPixelSize(R.dimen.screen_magnification_video_height);
            final int videoAssetMarginTop = res.getDimensionPixelSize(R.dimen.screen_magnification_video_margin_top);
            view.setDividerAllowedAbove(false);
            view.setDividerAllowedBelow(false);
            this.mVideoBackgroundView = (ImageView) view.findViewById(R.id.video_background);
            final VideoView videoView = (VideoView) view.findViewById(R.id.video);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { // from class: com.android.settings.accessibility.ToggleScreenMagnificationPreferenceFragment.VideoPreference.1
                @Override // android.media.MediaPlayer.OnPreparedListener
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setLooping(true);
                }
            });
            videoView.setVideoURI(Uri.parse(String.format("%s://%s/%s", "android.resource", ToggleScreenMagnificationPreferenceFragment.this.getPrefContext().getPackageName(), Integer.valueOf((int) R.raw.accessibility_screen_magnification))));
            videoView.setMediaController(null);
            this.mLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.android.settings.accessibility.ToggleScreenMagnificationPreferenceFragment.VideoPreference.2
                @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                public void onGlobalLayout() {
                    int backgroundViewWidth = VideoPreference.this.mVideoBackgroundView.getWidth();
                    RelativeLayout.LayoutParams videoLp = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
                    videoLp.width = (videoAssetWidth * backgroundViewWidth) / backgroundAssetWidth;
                    videoLp.height = (videoAssetHeight * backgroundViewWidth) / backgroundAssetWidth;
                    videoLp.setMargins(0, (videoAssetMarginTop * backgroundViewWidth) / backgroundAssetWidth, 0, 0);
                    videoView.setLayoutParams(videoLp);
                    videoView.invalidate();
                    videoView.start();
                }
            };
            this.mVideoBackgroundView.getViewTreeObserver().addOnGlobalLayoutListener(this.mLayoutListener);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.support.v7.preference.Preference
        public void onPrepareForRemoval() {
            this.mVideoBackgroundView.getViewTreeObserver().removeOnGlobalLayoutListener(this.mLayoutListener);
        }
    }

    @Override // com.android.settings.accessibility.ToggleFeaturePreferenceFragment, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mVideoPreference = new VideoPreference(getPrefContext());
        this.mVideoPreference.setSelectable(false);
        this.mVideoPreference.setPersistent(false);
        this.mVideoPreference.setLayoutResource(R.layout.video_preference);
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();
        preferenceScreen.setOrderingAsAdded(false);
        this.mVideoPreference.setOrder(0);
        this.mSummaryPreference.setOrder(1);
        preferenceScreen.addPreference(this.mVideoPreference);
    }

    protected void onPreferenceToggled(String preferenceKey, boolean enabled) {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.accessibility.ToggleFeaturePreferenceFragment
    public void onInstallSwitchBarToggleSwitch() {
        super.onInstallSwitchBarToggleSwitch();
        this.mToggleSwitch.setOnBeforeCheckedChangeListener(new ToggleSwitch.OnBeforeCheckedChangeListener() { // from class: com.android.settings.accessibility.ToggleScreenMagnificationPreferenceFragment.1
            @Override // com.android.settings.widget.ToggleSwitch.OnBeforeCheckedChangeListener
            public boolean onBeforeCheckedChanged(ToggleSwitch toggleSwitch, boolean checked) {
                ToggleScreenMagnificationPreferenceFragment.this.mSwitchBar.setCheckedInternal(checked);
                ToggleScreenMagnificationPreferenceFragment.this.getArguments().putBoolean("checked", checked);
                ToggleScreenMagnificationPreferenceFragment.this.onPreferenceToggled(ToggleScreenMagnificationPreferenceFragment.this.mPreferenceKey, checked);
                return false;
            }
        });
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        if (Settings.Secure.getInt(getContentResolver(), "accessibility_display_magnification_enabled", 0) == 0) {
            setMagnificationEnabled(1);
        }
        VideoView videoView = (VideoView) getView().findViewById(R.id.video);
        if (videoView == null) {
            return;
        }
        videoView.start();
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        if (this.mToggleSwitch.isChecked()) {
            return;
        }
        setMagnificationEnabled(0);
    }

    private void setMagnificationEnabled(int enabled) {
        Settings.Secure.putInt(getContentResolver(), "accessibility_display_magnification_enabled", enabled);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 7;
    }
}
