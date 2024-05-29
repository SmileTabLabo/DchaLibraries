package com.android.systemui.tv.pip;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.tv.pip.PipRecentsControlsView;
/* loaded from: a.zip:com/android/systemui/tv/pip/PipRecentsOverlayManager.class */
public class PipRecentsOverlayManager {
    private Callback mCallback;
    private boolean mHasFocusableInRecents;
    private boolean mIsPipFocusedInRecent;
    private boolean mIsPipRecentsOverlayShown;
    private boolean mIsRecentsShown;
    private View mOverlayView;
    private PipRecentsControlsView mPipControlsView;
    private WindowManager.LayoutParams mPipRecentsControlsViewFocusedLayoutParams;
    private WindowManager.LayoutParams mPipRecentsControlsViewLayoutParams;
    private View mRecentsView;
    private final SystemServicesProxy mSystemServicesProxy;
    private boolean mTalkBackEnabled;
    private final WindowManager mWindowManager;
    private final PipManager mPipManager = PipManager.getInstance();
    private PipRecentsControlsView.Listener mPipControlsViewListener = new PipRecentsControlsView.Listener(this) { // from class: com.android.systemui.tv.pip.PipRecentsOverlayManager.1
        final PipRecentsOverlayManager this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.tv.pip.PipRecentsControlsView.Listener
        public void onBackPressed() {
            if (this.this$0.mCallback != null) {
                this.this$0.mCallback.onBackPressed();
            }
        }

        @Override // com.android.systemui.tv.pip.PipControlsView.Listener
        public void onClosed() {
            if (this.this$0.mCallback != null) {
                this.this$0.mCallback.onClosed();
            }
        }
    };

    /* loaded from: a.zip:com/android/systemui/tv/pip/PipRecentsOverlayManager$Callback.class */
    public interface Callback {
        void onBackPressed();

        void onClosed();

        void onRecentsFocused();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public PipRecentsOverlayManager(Context context) {
        this.mWindowManager = (WindowManager) context.getSystemService(WindowManager.class);
        this.mSystemServicesProxy = SystemServicesProxy.getInstance(context);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mOverlayView = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(2130968830, (ViewGroup) null);
        this.mPipControlsView = (PipRecentsControlsView) this.mOverlayView.findViewById(2131886733);
        this.mRecentsView = this.mOverlayView.findViewById(2131886739);
        this.mRecentsView.setOnFocusChangeListener(new View.OnFocusChangeListener(this) { // from class: com.android.systemui.tv.pip.PipRecentsOverlayManager.2
            final PipRecentsOverlayManager this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnFocusChangeListener
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    this.this$0.clearFocus();
                }
            }
        });
        this.mOverlayView.measure(0, 0);
        this.mPipRecentsControlsViewLayoutParams = new WindowManager.LayoutParams(this.mOverlayView.getMeasuredWidth(), this.mOverlayView.getMeasuredHeight(), 2008, 24, -3);
        this.mPipRecentsControlsViewLayoutParams.gravity = 49;
        this.mPipRecentsControlsViewFocusedLayoutParams = new WindowManager.LayoutParams(this.mOverlayView.getMeasuredWidth(), this.mOverlayView.getMeasuredHeight(), 2008, 0, -3);
        this.mPipRecentsControlsViewFocusedLayoutParams.gravity = 49;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addPipRecentsOverlayView() {
        if (this.mIsPipRecentsOverlayShown) {
            return;
        }
        this.mTalkBackEnabled = this.mSystemServicesProxy.isTouchExplorationEnabled();
        this.mRecentsView.setVisibility(this.mTalkBackEnabled ? 0 : 8);
        this.mIsPipRecentsOverlayShown = true;
        this.mIsPipFocusedInRecent = true;
        this.mWindowManager.addView(this.mOverlayView, this.mPipRecentsControlsViewFocusedLayoutParams);
    }

    public void clearFocus() {
        if (this.mIsPipRecentsOverlayShown && this.mIsRecentsShown && this.mIsPipFocusedInRecent && this.mPipManager.isPipShown() && this.mHasFocusableInRecents) {
            this.mIsPipFocusedInRecent = false;
            this.mPipControlsView.startFocusLossAnimation();
            this.mWindowManager.updateViewLayout(this.mOverlayView, this.mPipRecentsControlsViewLayoutParams);
            this.mPipManager.resizePinnedStack(3);
            if (this.mCallback != null) {
                this.mCallback.onRecentsFocused();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isRecentsShown() {
        return this.mIsRecentsShown;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onConfigurationChanged(Context context) {
        if (this.mIsRecentsShown) {
            Log.w("PipRecentsOverlayManager", "Configuration is changed while Recents is shown");
        }
        initViews(context);
    }

    public void onRecentsPaused() {
        this.mIsRecentsShown = false;
        this.mIsPipFocusedInRecent = false;
        removePipRecentsOverlayView();
        if (this.mPipManager.isPipShown()) {
            this.mPipManager.resizePinnedStack(1);
        }
    }

    public void onRecentsResumed() {
        if (this.mPipManager.isPipShown()) {
            this.mIsRecentsShown = true;
            this.mIsPipFocusedInRecent = true;
            this.mPipManager.resizePinnedStack(4);
        }
    }

    public void removePipRecentsOverlayView() {
        if (this.mIsPipRecentsOverlayShown) {
            this.mWindowManager.removeView(this.mOverlayView);
            this.mPipControlsView.reset();
            this.mIsPipRecentsOverlayShown = false;
        }
    }

    public void requestFocus(boolean z) {
        this.mHasFocusableInRecents = z;
        if (this.mIsPipRecentsOverlayShown && this.mIsRecentsShown && !this.mIsPipFocusedInRecent && this.mPipManager.isPipShown()) {
            this.mIsPipFocusedInRecent = true;
            this.mPipControlsView.startFocusGainAnimation();
            this.mWindowManager.updateViewLayout(this.mOverlayView, this.mPipRecentsControlsViewFocusedLayoutParams);
            this.mPipManager.resizePinnedStack(4);
            if (this.mTalkBackEnabled) {
                this.mPipControlsView.requestFocus();
                this.mPipControlsView.sendAccessibilityEvent(8);
            }
        }
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
        PipRecentsControlsView pipRecentsControlsView = this.mPipControlsView;
        PipRecentsControlsView.Listener listener = null;
        if (this.mCallback != null) {
            listener = this.mPipControlsViewListener;
        }
        pipRecentsControlsView.setListener(listener);
    }
}
