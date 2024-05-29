package com.android.systemui.recents.misc;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.ITaskStackListener;
import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.Log;
import android.util.MutableBoolean;
import android.view.Display;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.IDockedStackListener;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.app.AssistUtils;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.ThumbnailData;
import com.mediatek.systemui.statusbar.util.FeatureOptions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/recents/misc/SystemServicesProxy.class */
public class SystemServicesProxy {
    static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    static final List<String> sRecentsBlacklist;
    private static SystemServicesProxy sSystemServicesProxy;
    AccessibilityManager mAccm;
    ActivityManager mAm;
    ComponentName mAssistComponent;
    AssistUtils mAssistUtils;
    Canvas mBgProtectionCanvas;
    Paint mBgProtectionPaint;
    Display mDisplay;
    int mDummyThumbnailHeight;
    int mDummyThumbnailWidth;
    boolean mHasFreeformWorkspaceSupport;
    boolean mIsSafeMode;
    PackageManager mPm;
    String mRecentsPackage;
    UserManager mUm;
    WindowManager mWm;
    private final Handler mHandler = new H(this, null);
    private ITaskStackListener.Stub mTaskStackListener = new ITaskStackListener.Stub(this) { // from class: com.android.systemui.recents.misc.SystemServicesProxy.1
        final SystemServicesProxy this$0;

        {
            this.this$0 = this;
        }

        public void onActivityDismissingDockedStack() throws RemoteException {
            this.this$0.mHandler.sendEmptyMessage(6);
        }

        public void onActivityForcedResizable(String str, int i) throws RemoteException {
            this.this$0.mHandler.obtainMessage(5, i, 0, str).sendToTarget();
        }

        public void onActivityPinned() throws RemoteException {
            this.this$0.mHandler.removeMessages(2);
            this.this$0.mHandler.sendEmptyMessage(2);
        }

        public void onPinnedActivityRestartAttempt() throws RemoteException {
            this.this$0.mHandler.removeMessages(3);
            this.this$0.mHandler.sendEmptyMessage(3);
        }

        public void onPinnedStackAnimationEnded() throws RemoteException {
            this.this$0.mHandler.removeMessages(4);
            this.this$0.mHandler.sendEmptyMessage(4);
        }

        public void onTaskStackChanged() throws RemoteException {
            this.this$0.mHandler.removeMessages(1);
            this.this$0.mHandler.sendEmptyMessage(1);
        }
    };
    private List<TaskStackListener> mTaskStackListeners = new ArrayList();
    IActivityManager mIam = ActivityManagerNative.getDefault();
    IPackageManager mIpm = AppGlobals.getPackageManager();

    /* loaded from: a.zip:com/android/systemui/recents/misc/SystemServicesProxy$H.class */
    private final class H extends Handler {
        final SystemServicesProxy this$0;

        private H(SystemServicesProxy systemServicesProxy) {
            this.this$0 = systemServicesProxy;
        }

        /* synthetic */ H(SystemServicesProxy systemServicesProxy, H h) {
            this(systemServicesProxy);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    for (int size = this.this$0.mTaskStackListeners.size() - 1; size >= 0; size--) {
                        ((TaskStackListener) this.this$0.mTaskStackListeners.get(size)).onTaskStackChanged();
                    }
                    return;
                case 2:
                    for (int size2 = this.this$0.mTaskStackListeners.size() - 1; size2 >= 0; size2--) {
                        ((TaskStackListener) this.this$0.mTaskStackListeners.get(size2)).onActivityPinned();
                    }
                    return;
                case 3:
                    for (int size3 = this.this$0.mTaskStackListeners.size() - 1; size3 >= 0; size3--) {
                        ((TaskStackListener) this.this$0.mTaskStackListeners.get(size3)).onPinnedActivityRestartAttempt();
                    }
                    return;
                case 4:
                    for (int size4 = this.this$0.mTaskStackListeners.size() - 1; size4 >= 0; size4--) {
                        ((TaskStackListener) this.this$0.mTaskStackListeners.get(size4)).onPinnedStackAnimationEnded();
                    }
                    return;
                case 5:
                    for (int size5 = this.this$0.mTaskStackListeners.size() - 1; size5 >= 0; size5--) {
                        ((TaskStackListener) this.this$0.mTaskStackListeners.get(size5)).onActivityForcedResizable((String) message.obj, message.arg1);
                    }
                    return;
                case 6:
                    for (int size6 = this.this$0.mTaskStackListeners.size() - 1; size6 >= 0; size6--) {
                        ((TaskStackListener) this.this$0.mTaskStackListeners.get(size6)).onActivityDismissingDockedStack();
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/recents/misc/SystemServicesProxy$TaskStackListener.class */
    public static abstract class TaskStackListener {
        public void onActivityDismissingDockedStack() {
        }

        public void onActivityForcedResizable(String str, int i) {
        }

        public void onActivityPinned() {
        }

        public void onPinnedActivityRestartAttempt() {
        }

        public void onPinnedStackAnimationEnded() {
        }

        public void onTaskStackChanged() {
        }
    }

    static {
        sBitmapOptions.inMutable = true;
        sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        if (FeatureOptions.LOW_RAM_SUPPORT) {
            sBitmapOptions.inSampleSize = 2;
        }
        sRecentsBlacklist = new ArrayList();
        sRecentsBlacklist.add("com.android.systemui.tv.pip.PipOnboardingActivity");
        sRecentsBlacklist.add("com.android.systemui.tv.pip.PipMenuActivity");
    }

    private SystemServicesProxy(Context context) {
        this.mAccm = AccessibilityManager.getInstance(context);
        this.mAm = (ActivityManager) context.getSystemService("activity");
        this.mPm = context.getPackageManager();
        this.mAssistUtils = new AssistUtils(context);
        this.mWm = (WindowManager) context.getSystemService("window");
        this.mUm = UserManager.get(context);
        this.mDisplay = this.mWm.getDefaultDisplay();
        this.mRecentsPackage = context.getPackageName();
        this.mHasFreeformWorkspaceSupport = !this.mPm.hasSystemFeature("android.software.freeform_window_management") ? Settings.Global.getInt(context.getContentResolver(), "enable_freeform_support", 0) != 0 : true;
        this.mIsSafeMode = this.mPm.isSafeMode();
        Resources resources = context.getResources();
        this.mDummyThumbnailWidth = resources.getDimensionPixelSize(17104898);
        this.mDummyThumbnailHeight = resources.getDimensionPixelSize(17104897);
        this.mBgProtectionPaint = new Paint();
        this.mBgProtectionPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        this.mBgProtectionPaint.setColor(-1);
        this.mBgProtectionCanvas = new Canvas();
        this.mAssistComponent = this.mAssistUtils.getAssistComponentForUser(UserHandle.myUserId());
        if (((UiModeManager) context.getSystemService("uimode")).getCurrentModeType() == 4) {
            Collections.addAll(sRecentsBlacklist, resources.getStringArray(2131427378));
        }
    }

    private Drawable getBadgedIcon(Drawable drawable, int i) {
        Drawable drawable2 = drawable;
        if (i != UserHandle.myUserId()) {
            drawable2 = this.mPm.getUserBadgedIcon(drawable, new UserHandle(i));
        }
        return drawable2;
    }

    private String getBadgedLabel(String str, int i) {
        String str2 = str;
        if (i != UserHandle.myUserId()) {
            str2 = this.mPm.getUserBadgedLabel(str, new UserHandle(i)).toString();
        }
        return str2;
    }

    public static SystemServicesProxy getInstance(Context context) {
        if (Looper.getMainLooper().isCurrentThread()) {
            if (sSystemServicesProxy == null) {
                sSystemServicesProxy = new SystemServicesProxy(context);
            }
            return sSystemServicesProxy;
        }
        throw new RuntimeException("Must be called on the UI thread");
    }

    public static boolean isFreeformStack(int i) {
        return i == 2;
    }

    public static boolean isHomeStack(int i) {
        boolean z = false;
        if (i == 0) {
            z = true;
        }
        return z;
    }

    public void cancelThumbnailTransition(int i) {
        if (this.mWm == null) {
            return;
        }
        try {
            WindowManagerGlobal.getWindowManagerService().cancelTaskThumbnailTransition(i);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void cancelWindowTransition(int i) {
        if (this.mWm == null) {
            return;
        }
        try {
            WindowManagerGlobal.getWindowManagerService().cancelTaskWindowTransition(i);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void endProlongedAnimations() {
        if (this.mWm == null) {
            return;
        }
        try {
            WindowManagerGlobal.getWindowManagerService().endProlongedAnimations();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ActivityInfo getActivityInfo(ComponentName componentName, int i) {
        if (this.mIpm == null) {
            return null;
        }
        try {
            return this.mIpm.getActivityInfo(componentName, 128, i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Drawable getBadgedActivityIcon(ActivityInfo activityInfo, int i) {
        if (this.mPm == null) {
            return null;
        }
        return getBadgedIcon(activityInfo.loadIcon(this.mPm), i);
    }

    public String getBadgedActivityLabel(ActivityInfo activityInfo, int i) {
        if (this.mPm == null) {
            return null;
        }
        return getBadgedLabel(activityInfo.loadLabel(this.mPm).toString(), i);
    }

    public Drawable getBadgedApplicationIcon(ApplicationInfo applicationInfo, int i) {
        if (this.mPm == null) {
            return null;
        }
        return getBadgedIcon(applicationInfo.loadIcon(this.mPm), i);
    }

    public String getBadgedApplicationLabel(ApplicationInfo applicationInfo, int i) {
        if (this.mPm == null) {
            return null;
        }
        return getBadgedLabel(applicationInfo.loadLabel(this.mPm).toString(), i);
    }

    public String getBadgedContentDescription(ActivityInfo activityInfo, int i, Resources resources) {
        String charSequence = activityInfo.loadLabel(this.mPm).toString();
        String charSequence2 = activityInfo.applicationInfo.loadLabel(this.mPm).toString();
        String badgedLabel = getBadgedLabel(charSequence2, i);
        if (!charSequence2.equals(charSequence)) {
            badgedLabel = resources.getString(2131493441, badgedLabel, charSequence);
        }
        return badgedLabel;
    }

    public Drawable getBadgedTaskDescriptionIcon(ActivityManager.TaskDescription taskDescription, int i, Resources resources) {
        Bitmap inMemoryIcon = taskDescription.getInMemoryIcon();
        Bitmap bitmap = inMemoryIcon;
        if (inMemoryIcon == null) {
            bitmap = ActivityManager.TaskDescription.loadTaskDescriptionIcon(taskDescription.getIconFilename(), i);
        }
        if (bitmap != null) {
            return getBadgedIcon(new BitmapDrawable(resources, bitmap), i);
        }
        return null;
    }

    public int getCurrentUser() {
        if (this.mAm == null) {
            return 0;
        }
        ActivityManager activityManager = this.mAm;
        return ActivityManager.getCurrentUser();
    }

    public int getDeviceSmallestWidth() {
        if (this.mDisplay == null) {
            return 0;
        }
        Point point = new Point();
        this.mDisplay.getCurrentSizeRange(point, new Point());
        return point.x;
    }

    public Rect getDisplayRect() {
        Rect rect = new Rect();
        if (this.mDisplay == null) {
            return rect;
        }
        Point point = new Point();
        this.mDisplay.getRealSize(point);
        rect.set(0, 0, point.x, point.y);
        return rect;
    }

    public int getDockedDividerSize(Context context) {
        Resources resources = context.getResources();
        return resources.getDimensionPixelSize(17104929) - (resources.getDimensionPixelSize(17104930) * 2);
    }

    public int getProcessUser() {
        if (this.mUm == null) {
            return 0;
        }
        return this.mUm.getUserHandle();
    }

    public List<ActivityManager.RecentTaskInfo> getRecentTasks(int i, int i2, boolean z, ArraySet<Integer> arraySet) {
        if (this.mAm == null) {
            return null;
        }
        int max = Math.max(10, i);
        int i3 = 62;
        if (z) {
            i3 = 63;
        }
        List list = null;
        try {
            list = this.mAm.getRecentTasksForUser(max, i3, i2);
        } catch (Exception e) {
            Log.e("SystemServicesProxy", "Failed to get recent tasks", e);
        }
        if (list == null) {
            return new ArrayList();
        }
        boolean z2 = true;
        Iterator it = list.iterator();
        while (it.hasNext()) {
            ActivityManager.RecentTaskInfo recentTaskInfo = (ActivityManager.RecentTaskInfo) it.next();
            if (sRecentsBlacklist.contains(recentTaskInfo.realActivity.getClassName()) || sRecentsBlacklist.contains(recentTaskInfo.realActivity.getPackageName())) {
                it.remove();
            } else {
                boolean contains = ((recentTaskInfo.baseIntent.getFlags() & 8388608) == 8388608) | arraySet.contains(Integer.valueOf(recentTaskInfo.userId));
                Log.d("SystemServicesProxy", "getRecentTasks:TASK = " + new Task.TaskKey(recentTaskInfo.persistentId, recentTaskInfo.stackId, recentTaskInfo.baseIntent, recentTaskInfo.userId, recentTaskInfo.firstActiveTime, recentTaskInfo.lastActiveTime).toString() + "/isExcluded = " + contains + "/includeFrontMostExcludedTask = " + z + "/isFirstValidTask = " + z2 + "/t.id = " + recentTaskInfo.id);
                if (contains && (!z2 || !z)) {
                    it.remove();
                }
                z2 = false;
            }
        }
        return list.subList(0, Math.min(list.size(), i));
    }

    public ActivityManager.RunningTaskInfo getRunningTask() {
        List<ActivityManager.RunningTaskInfo> runningTasks = this.mAm.getRunningTasks(1);
        if (runningTasks == null || runningTasks.isEmpty()) {
            return null;
        }
        Log.d("SystemServicesProxy", "getTopMostTask: tasks: " + runningTasks.get(0).id);
        return runningTasks.get(0);
    }

    public void getStableInsets(Rect rect) {
        if (this.mWm == null) {
            return;
        }
        try {
            WindowManagerGlobal.getWindowManagerService().getStableInsets(rect);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getSystemSetting(Context context, String str) {
        return Settings.System.getInt(context.getContentResolver(), str, 0);
    }

    public ThumbnailData getTaskThumbnail(int i) {
        if (this.mAm == null) {
            return null;
        }
        ThumbnailData thumbnailData = new ThumbnailData();
        getThumbnail(i, thumbnailData);
        if (thumbnailData.thumbnail != null) {
            thumbnailData.thumbnail.setHasAlpha(false);
            if (Color.alpha(thumbnailData.thumbnail.getPixel(0, 0)) == 0) {
                this.mBgProtectionCanvas.setBitmap(thumbnailData.thumbnail);
                this.mBgProtectionCanvas.drawRect(0.0f, 0.0f, thumbnailData.thumbnail.getWidth(), thumbnailData.thumbnail.getHeight(), this.mBgProtectionPaint);
                this.mBgProtectionCanvas.setBitmap(null);
                Log.e("SystemServicesProxy", "Invalid screenshot detected from getTaskThumbnail()");
            }
        }
        return thumbnailData;
    }

    public void getThumbnail(int i, ThumbnailData thumbnailData) {
        ActivityManager.TaskThumbnail taskThumbnail;
        Bitmap bitmap;
        if (this.mAm == null || (taskThumbnail = this.mAm.getTaskThumbnail(i)) == null) {
            return;
        }
        Bitmap bitmap2 = taskThumbnail.mainThumbnail;
        ParcelFileDescriptor parcelFileDescriptor = taskThumbnail.thumbnailFileDescriptor;
        if (bitmap2 != null || parcelFileDescriptor == null) {
            bitmap = bitmap2;
            if (bitmap2 != null) {
                bitmap = bitmap2;
                if (FeatureOptions.LOW_RAM_SUPPORT) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                    Bitmap decodeStream = BitmapFactory.decodeStream(byteArrayInputStream, null, sBitmapOptions);
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            bitmap = decodeStream;
                        }
                    }
                    bitmap = decodeStream;
                    if (byteArrayInputStream != null) {
                        byteArrayInputStream.close();
                        bitmap = decodeStream;
                    }
                }
            }
        } else {
            bitmap = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor(), null, sBitmapOptions);
        }
        if (parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.close();
            } catch (IOException e2) {
            }
        }
        thumbnailData.thumbnail = bitmap;
        thumbnailData.thumbnailInfo = taskThumbnail.thumbnailInfo;
    }

    public Rect getWindowRect() {
        Rect rect = new Rect();
        try {
            if (this.mIam == null) {
                return rect;
            }
            try {
                ActivityManager.StackInfo stackInfo = this.mIam.getStackInfo(0);
                if (stackInfo != null) {
                    rect.set(stackInfo.bounds);
                }
                return rect;
            } catch (RemoteException e) {
                e.printStackTrace();
                return rect;
            }
        } catch (Throwable th) {
            return rect;
        }
    }

    public boolean hasDockedTask() {
        if (this.mIam == null) {
            return false;
        }
        ActivityManager.StackInfo stackInfo = null;
        try {
            stackInfo = this.mIam.getStackInfo(3);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (stackInfo != null) {
            int currentUser = getCurrentUser();
            boolean z = false;
            for (int length = stackInfo.taskUserIds.length - 1; length >= 0 && !z; length--) {
                z = stackInfo.taskUserIds[length] == currentUser;
            }
            return z;
        }
        return false;
    }

    public boolean hasFreeformWorkspaceSupport() {
        return this.mHasFreeformWorkspaceSupport;
    }

    public boolean hasSoftNavigationBar() {
        try {
            return WindowManagerGlobal.getWindowManagerService().hasNavigationBar();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasTransposedNavigationBar() {
        boolean z = false;
        Rect rect = new Rect();
        getStableInsets(rect);
        if (rect.right > 0) {
            z = true;
        }
        return z;
    }

    public boolean isInSafeMode() {
        return this.mIsSafeMode;
    }

    public boolean isRecentsActivityVisible() {
        return isRecentsActivityVisible(null);
    }

    public boolean isRecentsActivityVisible(MutableBoolean mutableBoolean) {
        boolean z;
        if (this.mIam == null) {
            return false;
        }
        try {
            ActivityManager.StackInfo stackInfo = this.mIam.getStackInfo(0);
            ActivityManager.StackInfo stackInfo2 = this.mIam.getStackInfo(1);
            ComponentName componentName = stackInfo.topActivity;
            boolean z2 = stackInfo.visible;
            boolean z3 = z2;
            if (stackInfo2 != null) {
                z3 = z2 & (!(stackInfo2.visible ? stackInfo2.position > stackInfo.position : false));
            }
            if (mutableBoolean != null) {
                mutableBoolean.value = z3;
            }
            if (z3 && componentName != null && componentName.getPackageName().equals("com.android.systemui")) {
                z = true;
                if (!componentName.getClassName().equals("com.android.systemui.recents.RecentsActivity")) {
                    z = componentName.getClassName().equals("com.android.systemui.recents.tv.RecentsTvActivity");
                }
            } else {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isScreenPinningActive() {
        if (this.mIam == null) {
            return false;
        }
        try {
            return this.mIam.isInLockTaskMode();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isSystemUser(int i) {
        boolean z = false;
        if (i == 0) {
            z = true;
        }
        return z;
    }

    public boolean isTouchExplorationEnabled() {
        boolean z = false;
        if (this.mAccm == null) {
            return false;
        }
        if (this.mAccm.isEnabled()) {
            z = this.mAccm.isTouchExplorationEnabled();
        }
        return z;
    }

    public boolean moveTaskToDockedStack(int i, int i2, Rect rect) {
        if (this.mIam == null) {
            return false;
        }
        try {
            return this.mIam.moveTaskToDockedStack(i, i2, true, false, rect, true);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void moveTaskToStack(int i, int i2) {
        if (this.mIam == null) {
            return;
        }
        try {
            this.mIam.positionTaskInStack(i, i2, 0);
        } catch (RemoteException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture iAppTransitionAnimationSpecsFuture, IRemoteCallback iRemoteCallback, boolean z) {
        try {
            WindowManagerGlobal.getWindowManagerService().overridePendingAppTransitionMultiThumbFuture(iAppTransitionAnimationSpecsFuture, iRemoteCallback, z);
        } catch (RemoteException e) {
            Log.w("SystemServicesProxy", "Failed to override transition: " + e);
        }
    }

    public void registerDockedStackListener(IDockedStackListener iDockedStackListener) {
        if (this.mWm == null) {
            return;
        }
        try {
            WindowManagerGlobal.getWindowManagerService().registerDockedStackListener(iDockedStackListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerTaskStackListener(TaskStackListener taskStackListener) {
        if (this.mIam == null) {
            return;
        }
        this.mTaskStackListeners.add(taskStackListener);
        if (this.mTaskStackListeners.size() == 1) {
            try {
                this.mIam.registerTaskStackListener(this.mTaskStackListener);
            } catch (Exception e) {
                Log.w("SystemServicesProxy", "Failed to call registerTaskStackListener", e);
            }
        }
    }

    public void removeTask(int i) {
        if (this.mAm == null) {
            return;
        }
        BackgroundThread.getHandler().post(new Runnable(this, i) { // from class: com.android.systemui.recents.misc.SystemServicesProxy.2
            final SystemServicesProxy this$0;
            final int val$taskId;

            {
                this.this$0 = this;
                this.val$taskId = i;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mAm.removeTask(this.val$taskId);
            }
        });
    }

    public void requestKeyboardShortcuts(Context context, WindowManager.KeyboardShortcutsReceiver keyboardShortcutsReceiver, int i) {
        this.mWm.requestAppKeyboardShortcuts(keyboardShortcutsReceiver, i);
    }

    public void restoreWindow() {
        Log.d("SystemServicesProxy", "restoreWindow");
        if (this.mIam != null) {
            try {
                this.mIam.restoreWindow();
            } catch (Exception e) {
                Log.e("SystemServicesProxy", "restoreWindow", e);
            }
        }
    }

    public void sendCloseSystemWindows(String str) {
        if (ActivityManagerNative.isSystemReady()) {
            try {
                this.mIam.closeSystemDialogs(str);
            } catch (RemoteException e) {
            }
        }
    }

    public boolean startActivityFromRecents(Context context, Task.TaskKey taskKey, String str, ActivityOptions activityOptions) {
        if (this.mIam != null) {
            ActivityOptions activityOptions2 = activityOptions;
            try {
                if (taskKey.stackId == 3) {
                    activityOptions2 = activityOptions;
                    if (activityOptions == null) {
                        activityOptions2 = ActivityOptions.makeBasic();
                    }
                    activityOptions2.setLaunchStackId(1);
                }
                this.mIam.startActivityFromRecents(taskKey.id, activityOptions2 == null ? null : activityOptions2.toBundle());
                return true;
            } catch (Exception e) {
                Log.e("SystemServicesProxy", context.getString(2131493584, str), e);
                return false;
            }
        }
        return false;
    }

    public void startInPlaceAnimationOnFrontMostApplication(ActivityOptions activityOptions) {
        if (this.mIam == null) {
            return;
        }
        try {
            this.mIam.startInPlaceAnimationOnFrontMostApplication(activityOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean startTaskInDockedMode(int i, int i2) {
        if (this.mIam == null) {
            return false;
        }
        try {
            ActivityOptions makeBasic = ActivityOptions.makeBasic();
            makeBasic.setDockCreateMode(i2);
            makeBasic.setLaunchStackId(3);
            this.mIam.startActivityFromRecents(i, makeBasic.toBundle());
            return true;
        } catch (Exception e) {
            Log.e("SystemServicesProxy", "Failed to dock task: " + i + " with createMode: " + i2, e);
            return false;
        }
    }
}
