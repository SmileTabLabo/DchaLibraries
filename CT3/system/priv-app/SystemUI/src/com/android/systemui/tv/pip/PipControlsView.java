package com.android.systemui.tv.pip;

import android.content.Context;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.tv.pip.PipManager;
/* loaded from: a.zip:com/android/systemui/tv/pip/PipControlsView.class */
public class PipControlsView extends LinearLayout {
    private PipControlButtonView mCloseButtonView;
    private final View.OnFocusChangeListener mFocusChangeListener;
    private PipControlButtonView mFocusedChild;
    private PipControlButtonView mFullButtonView;
    Listener mListener;
    private MediaController mMediaController;
    private MediaController.Callback mMediaControllerCallback;
    final PipManager mPipManager;
    private final PipManager.MediaListener mPipMediaListener;
    private PipControlButtonView mPlayPauseButtonView;

    /* loaded from: a.zip:com/android/systemui/tv/pip/PipControlsView$Listener.class */
    public interface Listener {
        void onClosed();
    }

    public PipControlsView(Context context) {
        this(context, null, 0, 0);
    }

    public PipControlsView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0, 0);
    }

    public PipControlsView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public PipControlsView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mPipManager = PipManager.getInstance();
        this.mMediaControllerCallback = new MediaController.Callback(this) { // from class: com.android.systemui.tv.pip.PipControlsView.1
            final PipControlsView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.media.session.MediaController.Callback
            public void onPlaybackStateChanged(PlaybackState playbackState) {
                this.this$0.updatePlayPauseView();
            }
        };
        this.mPipMediaListener = new PipManager.MediaListener(this) { // from class: com.android.systemui.tv.pip.PipControlsView.2
            final PipControlsView this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.tv.pip.PipManager.MediaListener
            public void onMediaControllerChanged() {
                this.this$0.updateMediaController();
            }
        };
        this.mFocusChangeListener = new View.OnFocusChangeListener(this) { // from class: com.android.systemui.tv.pip.PipControlsView.3
            final PipControlsView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnFocusChangeListener
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    this.this$0.mFocusedChild = (PipControlButtonView) view;
                } else if (this.this$0.mFocusedChild == view) {
                    this.this$0.mFocusedChild = null;
                }
            }
        };
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(2130968826, this);
        setOrientation(0);
        setGravity(49);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateMediaController() {
        MediaController mediaController = this.mPipManager.getMediaController();
        if (this.mMediaController == mediaController) {
            return;
        }
        if (this.mMediaController != null) {
            this.mMediaController.unregisterCallback(this.mMediaControllerCallback);
        }
        this.mMediaController = mediaController;
        if (this.mMediaController != null) {
            this.mMediaController.registerCallback(this.mMediaControllerCallback);
        }
        updatePlayPauseView();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePlayPauseView() {
        int playbackState = this.mPipManager.getPlaybackState();
        if (playbackState == 2) {
            this.mPlayPauseButtonView.setVisibility(8);
            return;
        }
        this.mPlayPauseButtonView.setVisibility(0);
        if (playbackState == 0) {
            this.mPlayPauseButtonView.setImageResource(2130837689);
            this.mPlayPauseButtonView.setText(2131493929);
            return;
        }
        this.mPlayPauseButtonView.setImageResource(2130837692);
        this.mPlayPauseButtonView.setText(2131493928);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public PipControlButtonView getFocusedButton() {
        return this.mFocusedChild;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateMediaController();
        this.mPipManager.addMediaListener(this.mPipMediaListener);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mPipManager.removeMediaListener(this.mPipMediaListener);
        if (this.mMediaController != null) {
            this.mMediaController.unregisterCallback(this.mMediaControllerCallback);
        }
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mFullButtonView = (PipControlButtonView) findViewById(2131886730);
        this.mFullButtonView.setOnFocusChangeListener(this.mFocusChangeListener);
        this.mFullButtonView.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.tv.pip.PipControlsView.4
            final PipControlsView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.mPipManager.movePipToFullscreen();
            }
        });
        this.mCloseButtonView = (PipControlButtonView) findViewById(2131886731);
        this.mCloseButtonView.setOnFocusChangeListener(this.mFocusChangeListener);
        this.mCloseButtonView.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.tv.pip.PipControlsView.5
            final PipControlsView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.mPipManager.closePip();
                if (this.this$0.mListener != null) {
                    this.this$0.mListener.onClosed();
                }
            }
        });
        this.mPlayPauseButtonView = (PipControlButtonView) findViewById(2131886732);
        this.mPlayPauseButtonView.setOnFocusChangeListener(this.mFocusChangeListener);
        this.mPlayPauseButtonView.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.tv.pip.PipControlsView.6
            final PipControlsView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (this.this$0.mMediaController == null || this.this$0.mMediaController.getPlaybackState() == null) {
                    return;
                }
                this.this$0.mMediaController.getPlaybackState().getActions();
                this.this$0.mMediaController.getPlaybackState().getState();
                if (this.this$0.mPipManager.getPlaybackState() == 1) {
                    this.this$0.mMediaController.getTransportControls().play();
                } else if (this.this$0.mPipManager.getPlaybackState() == 0) {
                    this.this$0.mMediaController.getTransportControls().pause();
                }
            }
        });
    }

    public void reset() {
        this.mFullButtonView.reset();
        this.mCloseButtonView.reset();
        this.mPlayPauseButtonView.reset();
        this.mFullButtonView.requestFocus();
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }
}
