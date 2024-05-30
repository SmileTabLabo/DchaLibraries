package com.mediatek.settings.wfd;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.StatusBarManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.settings.R;
import com.mediatek.settings.FeatureOption;
/* loaded from: classes.dex */
public final class WfdSinkSurfaceFragment extends DialogFragment implements SurfaceHolder.Callback, View.OnLongClickListener {
    private static final String TAG = WfdSinkSurfaceFragment.class.getSimpleName();
    private Activity mActivity;
    private Dialog mDialog;
    private WfdSinkExt mExt;
    private SurfaceView mSinkView;
    private WfdSinkLayout mSinkViewLayout;
    private StatusBarManager mStatusBar;
    private boolean mSurfaceShowing = false;
    private boolean mGuideShowing = false;
    private boolean mCountdownShowing = false;
    private int mOrientationBak = -100;
    private boolean mLatinCharTest = false;
    private int mTestLatinChar = 160;

    static /* synthetic */ int access$1308(WfdSinkSurfaceFragment wfdSinkSurfaceFragment) {
        int i = wfdSinkSurfaceFragment.mTestLatinChar;
        wfdSinkSurfaceFragment.mTestLatinChar = i + 1;
        return i;
    }

    @Override // android.app.DialogFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle != null || !FeatureOption.MTK_WFD_SINK_SUPPORT) {
            Log.d("@M_" + TAG, "bundle is not null, recreate");
            dismissAllowingStateLoss();
            getActivity().finish();
            return;
        }
        this.mActivity = getActivity();
        this.mExt = new WfdSinkExt(this.mActivity);
        this.mExt.registerSinkFragment(this);
        this.mActivity.getActionBar().hide();
        setShowsDialog(true);
    }

    @Override // android.app.DialogFragment, android.app.Fragment
    public void onStart() {
        Log.d("@M_" + TAG, "onStart");
        super.onStart();
        this.mExt.onStart();
        this.mStatusBar = (StatusBarManager) this.mActivity.getSystemService("statusbar");
        this.mStatusBar.disable(52887552);
    }

    @Override // android.app.DialogFragment, android.app.Fragment
    public void onStop() {
        Log.d("@M_" + TAG, "onStop");
        this.mExt.onStop();
        dismissAllowingStateLoss();
        if (this.mStatusBar != null) {
            this.mStatusBar.disable(0);
        }
        this.mActivity.finish();
        super.onStop();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void disconnect() {
        if (this.mSurfaceShowing) {
            this.mExt.disconnectWfdSinkConnection();
        }
        this.mSurfaceShowing = false;
        if (this.mGuideShowing) {
            removeWfdSinkGuide();
        }
    }

    @Override // android.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        String str = "@M_" + TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("mDialog is null? ");
        sb.append(this.mDialog == null);
        Log.d(str, sb.toString());
        this.mLatinCharTest = SystemProperties.get("wfd.uibc.latintest", "0").equals("1");
        if (this.mDialog == null) {
            this.mDialog = new FullScreenDialog(getActivity());
        }
        return this.mDialog;
    }

    public void addWfdSinkGuide() {
        if (this.mGuideShowing) {
            return;
        }
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.wfd_sink_guide, (ViewGroup) null);
        ((Button) viewGroup.findViewById(R.id.wfd_sink_guide_ok_btn)).setOnClickListener(new View.OnClickListener() { // from class: com.mediatek.settings.wfd.WfdSinkSurfaceFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "ok button onClick");
                WfdSinkSurfaceFragment.this.removeWfdSinkGuide();
            }
        });
        ((TextView) viewGroup.findViewById(R.id.wfd_sink_guide_content)).setText(getActivity().getResources().getString(R.string.wfd_sink_guide_content, 3));
        this.mSinkViewLayout.addView(viewGroup);
        this.mSinkViewLayout.setTag(R.string.wfd_sink_guide_content, viewGroup);
        this.mSinkViewLayout.setCatchEvents(false);
        this.mGuideShowing = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeWfdSinkGuide() {
        View view;
        if (this.mGuideShowing && (view = (View) this.mSinkViewLayout.getTag(R.string.wfd_sink_guide_content)) != null) {
            this.mSinkViewLayout.removeView(view);
            this.mSinkViewLayout.setTag(R.string.wfd_sink_guide_content, null);
        }
        this.mSinkViewLayout.setCatchEvents(true);
        this.mGuideShowing = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addCountdownView(String str) {
        if (this.mCountdownShowing) {
            return;
        }
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.wfd_sink_countdown, (ViewGroup) null);
        ((TextView) viewGroup.findViewById(R.id.wfd_sink_countdown_num)).setText(str);
        this.mSinkViewLayout.addView(viewGroup);
        this.mSinkViewLayout.setTag(R.id.wfd_sink_countdown_num, viewGroup);
        this.mCountdownShowing = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeCountDown() {
        View view;
        if (this.mCountdownShowing && (view = (View) this.mSinkViewLayout.getTag(R.id.wfd_sink_countdown_num)) != null) {
            this.mSinkViewLayout.removeView(view);
            this.mSinkViewLayout.setTag(R.id.wfd_sink_countdown_num, null);
        }
        this.mCountdownShowing = false;
    }

    public void requestOrientation(boolean z) {
        this.mOrientationBak = this.mActivity.getRequestedOrientation();
        this.mActivity.setRequestedOrientation(z ? 1 : 0);
    }

    public void restoreOrientation() {
        if (this.mOrientationBak != -100) {
            this.mActivity.setRequestedOrientation(this.mOrientationBak);
        }
    }

    /* loaded from: classes.dex */
    private class FullScreenDialog extends Dialog {
        private Activity mActivity;
        private int mSystemUiBak;

        public FullScreenDialog(Activity activity) {
            super(activity, 16973841);
            this.mActivity = activity;
        }

        @Override // android.app.Dialog
        protected void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "dialog onCreate");
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-1, -1);
            WfdSinkSurfaceFragment.this.mSinkViewLayout = new WfdSinkLayout(this.mActivity);
            WfdSinkSurfaceFragment.this.mSinkViewLayout.setFocusableInTouchMode(true);
            setContentView(WfdSinkSurfaceFragment.this.mSinkViewLayout);
            WfdSinkSurfaceFragment.this.mSinkView = new SurfaceView(this.mActivity);
            WfdSinkSurfaceFragment.this.mSinkView.setFocusableInTouchMode(false);
            WfdSinkSurfaceFragment.this.mSinkView.setFocusable(false);
            WfdSinkSurfaceFragment.this.mSinkViewLayout.addView(WfdSinkSurfaceFragment.this.mSinkView, layoutParams);
        }

        @Override // android.app.Dialog
        protected void onStart() {
            Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "dialog onStart");
            super.onStart();
            this.mSystemUiBak = WfdSinkSurfaceFragment.this.mSinkViewLayout.getSystemUiVisibility();
            WfdSinkSurfaceFragment.this.mSinkViewLayout.setOnFocusGetCallback(new Runnable() { // from class: com.mediatek.settings.wfd.WfdSinkSurfaceFragment.FullScreenDialog.1
                @Override // java.lang.Runnable
                public void run() {
                    WfdSinkSurfaceFragment.this.requestFullScreen(FullScreenDialog.this.mSystemUiBak);
                }
            });
            WfdSinkSurfaceFragment.this.mSinkViewLayout.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() { // from class: com.mediatek.settings.wfd.WfdSinkSurfaceFragment.FullScreenDialog.2
                @Override // android.view.View.OnSystemUiVisibilityChangeListener
                public void onSystemUiVisibilityChange(int i) {
                    Log.i("@M_" + WfdSinkSurfaceFragment.TAG, "onSystemUiVisibilityChange: " + i);
                    if (i == 0) {
                        WfdSinkSurfaceFragment.this.mSinkViewLayout.setFullScreenFlag(false);
                        if (WfdSinkSurfaceFragment.this.mSinkViewLayout.mHasFocus) {
                            WfdSinkSurfaceFragment.this.requestFullScreen(FullScreenDialog.this.mSystemUiBak);
                            return;
                        }
                        return;
                    }
                    WfdSinkSurfaceFragment.this.mSinkViewLayout.setFullScreenFlag(true);
                }
            });
            WfdSinkSurfaceFragment.this.requestFullScreen(this.mSystemUiBak);
            this.mActivity.getWindow().addFlags(128);
            WfdSinkSurfaceFragment.this.mSinkView.getHolder().addCallback(WfdSinkSurfaceFragment.this);
        }

        @Override // android.app.Dialog
        protected void onStop() {
            Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "dialog onStop");
            WfdSinkSurfaceFragment.this.mSinkViewLayout.setSystemUiVisibility(this.mSystemUiBak);
            this.mActivity.getWindow().clearFlags(128);
            WfdSinkSurfaceFragment.this.mSinkView.getHolder().removeCallback(WfdSinkSurfaceFragment.this);
            WfdSinkSurfaceFragment.this.restoreOrientation();
            super.onStop();
        }

        @Override // android.app.Dialog, android.content.DialogInterface
        public void dismiss() {
            Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "dialog dismiss");
            WfdSinkSurfaceFragment.this.disconnect();
            this.mActivity.finish();
            super.dismiss();
        }

        @Override // android.app.Dialog
        public void onBackPressed() {
            Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "dialog onBackPressed");
            if (WfdSinkSurfaceFragment.this.mGuideShowing) {
                WfdSinkSurfaceFragment.this.removeWfdSinkGuide();
                return;
            }
            WfdSinkSurfaceFragment.this.disconnect();
            super.onBackPressed();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestFullScreen(final int i) {
        if (Build.VERSION.SDK_INT >= 14) {
            i |= 2;
        }
        if (Build.VERSION.SDK_INT >= 16) {
            i |= 4;
        }
        if (Build.VERSION.SDK_INT >= 18) {
            i |= 16781312;
        }
        this.mSinkViewLayout.postDelayed(new Runnable() { // from class: com.mediatek.settings.wfd.WfdSinkSurfaceFragment.2
            @Override // java.lang.Runnable
            public void run() {
                Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "request full screen: " + Integer.toHexString(i));
                WfdSinkSurfaceFragment.this.mSinkViewLayout.setSystemUiVisibility(i);
            }
        }, 500L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class WfdSinkLayout extends FrameLayout {
        private boolean mCatchEvents;
        private CountDown mCountDown;
        private Runnable mFocusGetCallback;
        private boolean mFullScreenFlag;
        private boolean mHasFocus;
        private boolean mHasPerformedLongPress;
        private float mInitX;
        private float mInitY;
        private int mTouchSlop;

        public WfdSinkLayout(Context context) {
            super(context);
            this.mHasPerformedLongPress = false;
            this.mCatchEvents = true;
            this.mFullScreenFlag = false;
            this.mHasFocus = false;
            this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            if (this.mCatchEvents) {
                int action = motionEvent.getAction();
                Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "onTouchEvent action=" + action);
                switch (action & 255) {
                    case 0:
                        if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                            sendUibcInputEvent(String.valueOf(0) + "," + getTouchEventDesc(motionEvent));
                        }
                        this.mInitX = motionEvent.getX();
                        this.mInitY = motionEvent.getY();
                        this.mHasPerformedLongPress = false;
                        checkForLongClick(0);
                        break;
                    case 1:
                        if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                            sendUibcInputEvent(String.valueOf(1) + "," + getTouchEventDesc(motionEvent));
                        }
                        removePendingCallback();
                        break;
                    case 2:
                        if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                            sendUibcInputEvent(String.valueOf(2) + "," + getTouchEventDesc(motionEvent));
                        }
                        if (Math.hypot(motionEvent.getX() - this.mInitX, motionEvent.getY() - this.mInitY) > this.mTouchSlop) {
                            removePendingCallback();
                            break;
                        }
                        break;
                    case 3:
                        removePendingCallback();
                        break;
                }
                return true;
            }
            return false;
        }

        @Override // android.view.View
        public boolean onGenericMotionEvent(MotionEvent motionEvent) {
            if (!this.mCatchEvents) {
                return false;
            }
            Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "onGenericMotionEvent event.getSource()=" + motionEvent.getSource());
            if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT && motionEvent.getSource() == 8194) {
                switch (motionEvent.getAction()) {
                    case 7:
                        sendUibcInputEvent(String.valueOf(2) + "," + getTouchEventDesc(motionEvent));
                        return true;
                    case 8:
                        return true;
                }
            }
            return true;
        }

        @Override // android.view.View
        public boolean onKeyPreIme(int i, KeyEvent keyEvent) {
            if (this.mCatchEvents && this.mFullScreenFlag) {
                Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "onKeyPreIme keyCode=" + i + ", action=" + keyEvent.getAction());
                if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                    int unicodeChar = keyEvent.getUnicodeChar();
                    if (unicodeChar == 0 || unicodeChar < 32) {
                        Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "Can't find unicode for keyCode=" + i);
                        unicodeChar = KeyCodeConverter.keyCodeToAscii(i);
                    }
                    boolean z = keyEvent.getAction() == 1;
                    if (WfdSinkSurfaceFragment.this.mLatinCharTest && i == 131) {
                        Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "Latin Test Mode enabled");
                        unicodeChar = WfdSinkSurfaceFragment.this.mTestLatinChar;
                        if (z) {
                            if (WfdSinkSurfaceFragment.this.mTestLatinChar == 255) {
                                WfdSinkSurfaceFragment.this.mTestLatinChar = 160;
                            } else {
                                WfdSinkSurfaceFragment.access$1308(WfdSinkSurfaceFragment.this);
                            }
                        }
                    }
                    Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "onKeyPreIme asciiCode=" + unicodeChar);
                    if (unicodeChar == 0) {
                        Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "Can't find control for keyCode=" + i);
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append(String.valueOf(z ? 4 : 3));
                        sb.append(",");
                        sb.append(String.format("0x%04x", Integer.valueOf(unicodeChar)));
                        sb.append(", 0x0000");
                        sendUibcInputEvent(sb.toString());
                        return true;
                    }
                }
                return false;
            }
            return false;
        }

        @Override // android.view.View
        public void onWindowFocusChanged(boolean z) {
            super.onWindowFocusChanged(z);
            Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "onWindowFocusChanged: " + z);
            this.mHasFocus = z;
            if (z && this.mFocusGetCallback != null) {
                this.mFocusGetCallback.run();
            }
        }

        private String getTouchEventDesc(MotionEvent motionEvent) {
            String sb;
            int pointerCount = motionEvent.getPointerCount();
            StringBuilder sb2 = new StringBuilder();
            sb2.append(String.valueOf(pointerCount));
            sb2.append(",");
            for (int i = 0; i < pointerCount; i++) {
                sb2.append(String.valueOf(motionEvent.getPointerId(i)));
                sb2.append(",");
                sb2.append(String.valueOf((int) (motionEvent.getXPrecision() * motionEvent.getX(i))));
                sb2.append(",");
                sb2.append(String.valueOf((int) (motionEvent.getYPrecision() * motionEvent.getY(i))));
                sb2.append(",");
            }
            return sb2.toString().substring(0, sb.length() - 1);
        }

        private void sendUibcInputEvent(String str) {
            Log.d("@M_" + WfdSinkSurfaceFragment.TAG, "sendUibcInputEvent: " + str);
            WfdSinkSurfaceFragment.this.mExt.sendUibcEvent(str);
        }

        private void checkForLongClick(int i) {
            this.mHasPerformedLongPress = false;
            if (this.mCountDown == null) {
                this.mCountDown = new CountDown();
            }
            this.mCountDown.rememberWindowAttachCount();
            postDelayed(this.mCountDown, (1000 + ViewConfiguration.getLongPressTimeout()) - i);
        }

        private void removePendingCallback() {
            Log.v("@M_" + WfdSinkSurfaceFragment.TAG, "removePendingCallback");
            if (this.mCountDown != null && !this.mHasPerformedLongPress) {
                removeCallbacks(this.mCountDown);
                WfdSinkSurfaceFragment.this.removeCountDown();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setCatchEvents(boolean z) {
            this.mCatchEvents = z;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setFullScreenFlag(boolean z) {
            this.mFullScreenFlag = z;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setOnFocusGetCallback(Runnable runnable) {
            this.mFocusGetCallback = runnable;
        }

        @Override // android.view.ViewGroup, android.view.View
        protected void onDetachedFromWindow() {
            removePendingCallback();
            super.onDetachedFromWindow();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: classes.dex */
        public class CountDown implements Runnable {
            private int mCountDownNum;
            private int mOriginalWindowAttachCount;

            CountDown() {
            }

            @Override // java.lang.Runnable
            public void run() {
                ViewGroup viewGroup;
                TextView textView;
                if (!WfdSinkSurfaceFragment.this.mCountdownShowing) {
                    this.mCountDownNum = 3;
                    WfdSinkSurfaceFragment.this.addCountdownView(this.mCountDownNum + "");
                } else {
                    this.mCountDownNum--;
                    if (this.mCountDownNum > 0) {
                        if (WfdSinkSurfaceFragment.this.mCountdownShowing && (viewGroup = (ViewGroup) WfdSinkSurfaceFragment.this.mSinkViewLayout.getTag(R.id.wfd_sink_countdown_num)) != null && (textView = (TextView) viewGroup.findViewById(R.id.wfd_sink_countdown_num)) != null) {
                            textView.setText(this.mCountDownNum + "");
                            textView.postInvalidate();
                        }
                    } else if (WfdSinkLayout.this.mParent != null && this.mOriginalWindowAttachCount == WfdSinkLayout.this.getWindowAttachCount() && WfdSinkSurfaceFragment.this.onLongClick(WfdSinkSurfaceFragment.this.mSinkViewLayout)) {
                        WfdSinkLayout.this.mHasPerformedLongPress = true;
                        return;
                    } else {
                        return;
                    }
                }
                WfdSinkLayout.this.postDelayed(this, 1000L);
            }

            public void rememberWindowAttachCount() {
                this.mOriginalWindowAttachCount = WfdSinkLayout.this.getWindowAttachCount();
            }
        }
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        Log.d("@M_" + TAG, "surface changed: " + i2 + "x" + i3);
        int systemUiVisibility = this.mSinkViewLayout.getSystemUiVisibility();
        if (this.mSinkViewLayout.mHasFocus && (systemUiVisibility & 2) == 0) {
            requestFullScreen(systemUiVisibility);
        }
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d("@M_" + TAG, "surface created");
        if (!this.mSurfaceShowing) {
            this.mExt.setupWfdSinkConnection(surfaceHolder.getSurface());
        }
        this.mSurfaceShowing = true;
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d("@M_" + TAG, "surface destroyed");
        disconnect();
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        Log.d("@M_" + TAG, "onLongClick");
        dismissAllowingStateLoss();
        this.mActivity.finish();
        return true;
    }

    /* loaded from: classes.dex */
    private static class KeyCodeConverter {
        private static final SparseIntArray KEYCODE_ASCII = new SparseIntArray();

        static {
            populateKeycodeAscii();
        }

        private static void populateKeycodeAscii() {
            SparseIntArray sparseIntArray = KEYCODE_ASCII;
            sparseIntArray.put(57, 18);
            sparseIntArray.put(58, 18);
            sparseIntArray.put(android.support.v7.appcompat.R.styleable.AppCompatTheme_windowActionBar, 27);
            sparseIntArray.put(59, 15);
            sparseIntArray.put(60, 15);
            sparseIntArray.put(123, 0);
            sparseIntArray.put(122, 0);
            sparseIntArray.put(android.support.v7.appcompat.R.styleable.AppCompatTheme_windowActionModeOverlay, 0);
            sparseIntArray.put(android.support.v7.appcompat.R.styleable.AppCompatTheme_windowFixedHeightMajor, 0);
            sparseIntArray.put(android.support.v7.appcompat.R.styleable.AppCompatTheme_windowFixedHeightMinor, 0);
            sparseIntArray.put(67, 8);
            sparseIntArray.put(93, 12);
            sparseIntArray.put(66, 13);
            sparseIntArray.put(android.support.v7.appcompat.R.styleable.AppCompatTheme_windowActionBarOverlay, 127);
            sparseIntArray.put(61, 9);
        }

        public static int keyCodeToAscii(int i) {
            return KEYCODE_ASCII.get(i);
        }
    }
}
