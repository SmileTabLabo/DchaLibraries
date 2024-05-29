package com.android.systemui.tv.pip;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Rect;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.systemui.Prefs;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.statusbar.tv.TvStatusBar;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/tv/pip/PipManager.class */
public class PipManager {
    private static PipManager sPipManager;
    private IActivityManager mActivityManager;
    private Context mContext;
    private Rect mCurrentPipBounds;
    private Rect mDefaultPipBounds;
    private boolean mInitialized;
    private String[] mLastPackagesResourceGranted;
    private MediaSessionManager mMediaSessionManager;
    private Rect mMenuModePipBounds;
    private boolean mOnboardingShown;
    private Rect mPipBounds;
    private ComponentName mPipComponentName;
    private MediaController mPipMediaController;
    private PipRecentsOverlayManager mPipRecentsOverlayManager;
    private int mRecentsFocusChangedAnimationDurationMs;
    private Rect mRecentsFocusedPipBounds;
    private Rect mRecentsPipBounds;
    private Rect mSettingsPipBounds;
    private int mSuspendPipResizingReason;
    private static final boolean DEBUG_FORCE_ONBOARDING = SystemProperties.getBoolean("debug.tv.pip_force_onboarding", false);
    private static final List<Pair<String, String>> sSettingsPackageAndClassNamePairList = new ArrayList();
    private int mState = 0;
    private final Handler mHandler = new Handler();
    private List<Listener> mListeners = new ArrayList();
    private List<MediaListener> mMediaListeners = new ArrayList();
    private int mPipTaskId = -1;
    private final Runnable mResizePinnedStackRunnable = new Runnable(this) { // from class: com.android.systemui.tv.pip.PipManager.1
        final PipManager this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.resizePinnedStack(this.this$0.mState);
        }
    };
    private final Runnable mClosePipRunnable = new Runnable(this) { // from class: com.android.systemui.tv.pip.PipManager.2
        final PipManager this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.closePip();
        }
    };
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.tv.pip.PipManager.3
        final PipManager this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.MEDIA_RESOURCE_GRANTED".equals(intent.getAction())) {
                String[] stringArrayExtra = intent.getStringArrayExtra("android.intent.extra.PACKAGES");
                int intExtra = intent.getIntExtra("android.intent.extra.MEDIA_RESOURCE_TYPE", -1);
                if (stringArrayExtra == null || stringArrayExtra.length <= 0 || intExtra != 0) {
                    return;
                }
                this.this$0.handleMediaResourceGranted(stringArrayExtra);
            }
        }
    };
    private final MediaSessionManager.OnActiveSessionsChangedListener mActiveMediaSessionListener = new MediaSessionManager.OnActiveSessionsChangedListener(this) { // from class: com.android.systemui.tv.pip.PipManager.4
        final PipManager this$0;

        {
            this.this$0 = this;
        }

        @Override // android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
        public void onActiveSessionsChanged(List<MediaController> list) {
            this.this$0.updateMediaController(list);
        }
    };
    private SystemServicesProxy.TaskStackListener mTaskStackListener = new SystemServicesProxy.TaskStackListener(this) { // from class: com.android.systemui.tv.pip.PipManager.5
        final PipManager this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.recents.misc.SystemServicesProxy.TaskStackListener
        public void onActivityPinned() {
            try {
                ActivityManager.StackInfo stackInfo = this.this$0.mActivityManager.getStackInfo(4);
                if (stackInfo == null) {
                    Log.w("PipManager", "Cannot find pinned stack");
                    return;
                }
                this.this$0.mPipTaskId = stackInfo.taskIds[stackInfo.taskIds.length - 1];
                this.this$0.mPipComponentName = ComponentName.unflattenFromString(stackInfo.taskNames[stackInfo.taskNames.length - 1]);
                this.this$0.mState = 1;
                this.this$0.mCurrentPipBounds = this.this$0.mPipBounds;
                this.this$0.launchPipOnboardingActivityIfNeeded();
                this.this$0.mMediaSessionManager.addOnActiveSessionsChangedListener(this.this$0.mActiveMediaSessionListener, null);
                this.this$0.updateMediaController(this.this$0.mMediaSessionManager.getActiveSessions(null));
                if (this.this$0.mPipRecentsOverlayManager.isRecentsShown()) {
                    this.this$0.resizePinnedStack(3);
                }
                for (int size = this.this$0.mListeners.size() - 1; size >= 0; size--) {
                    ((Listener) this.this$0.mListeners.get(size)).onPipEntered();
                }
                this.this$0.updatePipVisibility(true);
            } catch (RemoteException e) {
                Log.e("PipManager", "getStackInfo failed", e);
            }
        }

        @Override // com.android.systemui.recents.misc.SystemServicesProxy.TaskStackListener
        public void onPinnedActivityRestartAttempt() {
            this.this$0.movePipToFullscreen();
        }

        @Override // com.android.systemui.recents.misc.SystemServicesProxy.TaskStackListener
        public void onPinnedStackAnimationEnded() {
            switch (this.this$0.mState) {
                case 1:
                    if (this.this$0.mPipRecentsOverlayManager.isRecentsShown()) {
                        this.this$0.resizePinnedStack(this.this$0.mState);
                        return;
                    } else {
                        this.this$0.showPipOverlay();
                        return;
                    }
                case 2:
                    this.this$0.showPipMenu();
                    return;
                case 3:
                case 4:
                    this.this$0.mPipRecentsOverlayManager.addPipRecentsOverlayView();
                    return;
                default:
                    return;
            }
        }

        @Override // com.android.systemui.recents.misc.SystemServicesProxy.TaskStackListener
        public void onTaskStackChanged() {
            boolean z;
            if (this.this$0.mState != 0) {
                try {
                    ActivityManager.StackInfo stackInfo = this.this$0.mActivityManager.getStackInfo(4);
                    if (stackInfo == null) {
                        Log.w("PipManager", "There is no pinned stack");
                        this.this$0.closePipInternal(false);
                        return;
                    }
                    int length = stackInfo.taskIds.length - 1;
                    while (true) {
                        z = false;
                        if (length < 0) {
                            break;
                        } else if (stackInfo.taskIds[length] == this.this$0.mPipTaskId) {
                            z = true;
                            break;
                        } else {
                            length--;
                        }
                    }
                    if (!z) {
                        this.this$0.closePipInternal(true);
                        return;
                    }
                } catch (RemoteException e) {
                    Log.e("PipManager", "getStackInfo failed", e);
                    return;
                }
            }
            if (this.this$0.mState == 1) {
                try {
                    List tasks = this.this$0.mActivityManager.getTasks(1, 0);
                    if (tasks == null || tasks.size() == 0) {
                        return;
                    }
                    Rect rect = PipManager.isSettingsShown(((ActivityManager.RunningTaskInfo) tasks.get(0)).topActivity) ? this.this$0.mSettingsPipBounds : this.this$0.mDefaultPipBounds;
                    if (this.this$0.mPipBounds != rect) {
                        this.this$0.mPipBounds = rect;
                        this.this$0.resizePinnedStack(1);
                    }
                } catch (RemoteException e2) {
                    Log.d("PipManager", "Failed to detect top activity", e2);
                }
            }
        }
    };

    /* loaded from: a.zip:com/android/systemui/tv/pip/PipManager$Listener.class */
    public interface Listener {
        void onMoveToFullscreen();

        void onPipActivityClosed();

        void onPipEntered();

        void onPipResizeAboutToStart();

        void onShowPipMenu();
    }

    /* loaded from: a.zip:com/android/systemui/tv/pip/PipManager$MediaListener.class */
    public interface MediaListener {
        void onMediaControllerChanged();
    }

    static {
        sSettingsPackageAndClassNamePairList.add(new Pair<>("com.android.tv.settings", null));
        sSettingsPackageAndClassNamePairList.add(new Pair<>("com.google.android.leanbacklauncher", "com.google.android.leanbacklauncher.settings.HomeScreenSettingsActivity"));
    }

    private PipManager() {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void closePipInternal(boolean z) {
        this.mState = 0;
        this.mPipTaskId = -1;
        this.mPipMediaController = null;
        this.mMediaSessionManager.removeOnActiveSessionsChangedListener(this.mActiveMediaSessionListener);
        if (z) {
            try {
                this.mActivityManager.removeStack(4);
            } catch (RemoteException e) {
                Log.e("PipManager", "removeStack failed", e);
            }
        }
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onPipActivityClosed();
        }
        this.mHandler.removeCallbacks(this.mClosePipRunnable);
        updatePipVisibility(false);
    }

    public static PipManager getInstance() {
        if (sPipManager == null) {
            sPipManager = new PipManager();
        }
        return sPipManager;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleMediaResourceGranted(String[] strArr) {
        boolean z;
        if (this.mState == 0) {
            this.mLastPackagesResourceGranted = strArr;
            return;
        }
        boolean z2 = false;
        boolean z3 = false;
        if (this.mLastPackagesResourceGranted != null) {
            String[] strArr2 = this.mLastPackagesResourceGranted;
            int length = strArr2.length;
            int i = 0;
            while (true) {
                z2 = z3;
                if (i >= length) {
                    break;
                }
                String str = strArr2[i];
                int length2 = strArr.length;
                int i2 = 0;
                while (true) {
                    z = z3;
                    if (i2 >= length2) {
                        break;
                    } else if (TextUtils.equals(strArr[i2], str)) {
                        z = true;
                        break;
                    } else {
                        i2++;
                    }
                }
                i++;
                z3 = z;
            }
        }
        this.mLastPackagesResourceGranted = strArr;
        if (z2) {
            return;
        }
        closePip();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isSettingsShown(ComponentName componentName) {
        String str;
        Iterator<T> it = sSettingsPackageAndClassNamePairList.iterator();
        while (it.hasNext()) {
            Pair pair = (Pair) it.next();
            String str2 = (String) pair.first;
            if (componentName.getPackageName().equals(pair.first) && ((str = (String) pair.second) == null || componentName.getClassName().equals(str))) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void launchPipOnboardingActivityIfNeeded() {
        if (DEBUG_FORCE_ONBOARDING || !this.mOnboardingShown) {
            this.mOnboardingShown = true;
            Prefs.putBoolean(this.mContext, "TvPictureInPictureOnboardingShown", true);
            Intent intent = new Intent(this.mContext, PipOnboardingActivity.class);
            intent.setFlags(268435456);
            this.mContext.startActivity(intent);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showPipMenu() {
        if (this.mPipRecentsOverlayManager.isRecentsShown()) {
            return;
        }
        this.mState = 2;
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onShowPipMenu();
        }
        Intent intent = new Intent(this.mContext, PipMenuActivity.class);
        intent.setFlags(268435456);
        this.mContext.startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showPipOverlay() {
        PipOverlayActivity.showPipOverlay(this.mContext);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateMediaController(List<MediaController> list) {
        MediaController mediaController = null;
        if (list != null) {
            mediaController = null;
            if (this.mState != 0) {
                mediaController = null;
                if (this.mPipComponentName != null) {
                    int size = list.size() - 1;
                    while (true) {
                        mediaController = null;
                        if (size < 0) {
                            break;
                        }
                        mediaController = list.get(size);
                        if (mediaController.getPackageName().equals(this.mPipComponentName.getPackageName())) {
                            break;
                        }
                        size--;
                    }
                }
            }
        }
        if (this.mPipMediaController != mediaController) {
            this.mPipMediaController = mediaController;
            for (int size2 = this.mMediaListeners.size() - 1; size2 >= 0; size2--) {
                this.mMediaListeners.get(size2).onMediaControllerChanged();
            }
            if (this.mPipMediaController == null) {
                this.mHandler.postDelayed(this.mClosePipRunnable, 3000L);
            } else {
                this.mHandler.removeCallbacks(this.mClosePipRunnable);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePipVisibility(boolean z) {
        TvStatusBar tvStatusBar = (TvStatusBar) ((SystemUIApplication) this.mContext).getComponent(TvStatusBar.class);
        if (tvStatusBar != null) {
            tvStatusBar.updatePipVisibility(z);
        }
    }

    public void addListener(Listener listener) {
        this.mListeners.add(listener);
    }

    public void addMediaListener(MediaListener mediaListener) {
        this.mMediaListeners.add(mediaListener);
    }

    public void closePip() {
        closePipInternal(true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public MediaController getMediaController() {
        return this.mPipMediaController;
    }

    public PipRecentsOverlayManager getPipRecentsOverlayManager() {
        return this.mPipRecentsOverlayManager;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getPlaybackState() {
        if (this.mPipMediaController == null || this.mPipMediaController.getPlaybackState() == null) {
            return 2;
        }
        int state = this.mPipMediaController.getPlaybackState().getState();
        boolean z = (state == 6 || state == 8 || state == 3 || state == 4 || state == 5 || state == 9) ? true : state == 10;
        long actions = this.mPipMediaController.getPlaybackState().getActions();
        if (z || (4 & actions) == 0) {
            return (!z || (2 & actions) == 0) ? 2 : 0;
        }
        return 1;
    }

    public Rect getRecentsFocusedPipBounds() {
        return this.mRecentsFocusedPipBounds;
    }

    public void initialize(Context context) {
        if (this.mInitialized) {
            return;
        }
        this.mInitialized = true;
        this.mContext = context;
        Resources resources = context.getResources();
        this.mDefaultPipBounds = Rect.unflattenFromString(resources.getString(17039469));
        this.mSettingsPipBounds = Rect.unflattenFromString(resources.getString(2131493282));
        this.mMenuModePipBounds = Rect.unflattenFromString(resources.getString(2131493283));
        this.mRecentsPipBounds = Rect.unflattenFromString(resources.getString(2131493284));
        this.mRecentsFocusedPipBounds = Rect.unflattenFromString(resources.getString(2131493285));
        this.mRecentsFocusChangedAnimationDurationMs = resources.getInteger(2131755097);
        this.mPipBounds = this.mDefaultPipBounds;
        this.mActivityManager = ActivityManagerNative.getDefault();
        SystemServicesProxy.getInstance(context).registerTaskStackListener(this.mTaskStackListener);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MEDIA_RESOURCE_GRANTED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.mOnboardingShown = Prefs.getBoolean(this.mContext, "TvPictureInPictureOnboardingShown", false);
        this.mPipRecentsOverlayManager = new PipRecentsOverlayManager(context);
        this.mMediaSessionManager = (MediaSessionManager) this.mContext.getSystemService("media_session");
    }

    public boolean isPipShown() {
        boolean z = false;
        if (this.mState != 0) {
            z = true;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void movePipToFullscreen() {
        this.mState = 0;
        this.mPipTaskId = -1;
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onMoveToFullscreen();
        }
        resizePinnedStack(this.mState);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onConfigurationChanged() {
        this.mPipRecentsOverlayManager.onConfigurationChanged(this.mContext);
    }

    public void removeListener(Listener listener) {
        this.mListeners.remove(listener);
    }

    public void removeMediaListener(MediaListener mediaListener) {
        this.mMediaListeners.remove(mediaListener);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Code restructure failed: missing block: B:25:0x0093, code lost:
        if (r8.mState == 4) goto L34;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void resizePinnedStack(int i) {
        boolean z = this.mState == 3 || this.mState == 4;
        this.mState = i;
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onPipResizeAboutToStart();
        }
        if (this.mSuspendPipResizingReason != 0) {
            return;
        }
        switch (this.mState) {
            case 0:
                this.mCurrentPipBounds = null;
                break;
            case 1:
                this.mCurrentPipBounds = this.mPipBounds;
                break;
            case 2:
                this.mCurrentPipBounds = this.mMenuModePipBounds;
                break;
            case 3:
                this.mCurrentPipBounds = this.mRecentsPipBounds;
                break;
            case 4:
                this.mCurrentPipBounds = this.mRecentsFocusedPipBounds;
                break;
            default:
                this.mCurrentPipBounds = this.mPipBounds;
                break;
        }
        int i2 = -1;
        if (z) {
            try {
                if (this.mState != 3) {
                    i2 = -1;
                }
                i2 = this.mRecentsFocusChangedAnimationDurationMs;
            } catch (RemoteException e) {
                Log.e("PipManager", "resizeStack failed", e);
                return;
            }
        }
        this.mActivityManager.resizeStack(4, this.mCurrentPipBounds, true, true, true, i2);
    }

    public void resumePipResizing(int i) {
        if ((this.mSuspendPipResizingReason & i) == 0) {
            return;
        }
        this.mSuspendPipResizingReason &= i ^ (-1);
        this.mHandler.post(this.mResizePinnedStackRunnable);
    }

    public void showTvPictureInPictureMenu() {
        if (this.mState == 1) {
            resizePinnedStack(2);
        }
    }

    public void suspendPipResizing(int i) {
        this.mSuspendPipResizingReason |= i;
    }
}
