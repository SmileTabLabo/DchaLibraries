package com.android.systemui.recents.misc;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.WindowConfiguration;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.dreams.IDreamManager;
import android.util.Log;
import android.util.MutableBoolean;
import android.view.Display;
import android.view.IDockedStackListener;
import android.view.IWindowManager;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.app.AssistUtils;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.Dependency;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.recents.Recents;
import com.android.systemui.statusbar.policy.UserInfoController;
import java.util.List;
/* loaded from: classes.dex */
public class SystemServicesProxy {
    static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static SystemServicesProxy sSystemServicesProxy;
    AccessibilityManager mAccm;
    ActivityManager mAm;
    AssistUtils mAssistUtils;
    Canvas mBgProtectionCanvas;
    Paint mBgProtectionPaint;
    private final Context mContext;
    private int mCurrentUserId;
    Display mDisplay;
    int mDummyThumbnailHeight;
    int mDummyThumbnailWidth;
    boolean mIsSafeMode;
    PackageManager mPm;
    String mRecentsPackage;
    UserManager mUm;
    WindowManager mWm;
    private final Runnable mGcRunnable = new Runnable() { // from class: com.android.systemui.recents.misc.SystemServicesProxy.1
        @Override // java.lang.Runnable
        public void run() {
            System.gc();
            System.runFinalization();
        }
    };
    private final UiOffloadThread mUiOffloadThread = (UiOffloadThread) Dependency.get(UiOffloadThread.class);
    private final UserInfoController.OnUserInfoChangedListener mOnUserInfoChangedListener = new UserInfoController.OnUserInfoChangedListener() { // from class: com.android.systemui.recents.misc.-$$Lambda$SystemServicesProxy$14WNoAPwhU0GwlQXHqE_l3lK1kI
        @Override // com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener
        public final void onUserInfoChanged(String str, Drawable drawable, String str2) {
            SystemServicesProxy.lambda$new$0(SystemServicesProxy.this, str, drawable, str2);
        }
    };
    IActivityManager mIam = ActivityManager.getService();
    IPackageManager mIpm = AppGlobals.getPackageManager();
    IWindowManager mIwm = WindowManagerGlobal.getWindowManagerService();
    private final IDreamManager mDreamManager = IDreamManager.Stub.asInterface(ServiceManager.checkService("dreams"));

    static {
        sBitmapOptions.inMutable = true;
        sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
    }

    public static /* synthetic */ void lambda$new$0(SystemServicesProxy systemServicesProxy, String str, Drawable drawable, String str2) {
        ActivityManager activityManager = systemServicesProxy.mAm;
        systemServicesProxy.mCurrentUserId = ActivityManager.getCurrentUser();
    }

    private SystemServicesProxy(Context context) {
        this.mContext = context.getApplicationContext();
        this.mAccm = AccessibilityManager.getInstance(context);
        this.mAm = (ActivityManager) context.getSystemService("activity");
        this.mPm = context.getPackageManager();
        this.mAssistUtils = new AssistUtils(context);
        this.mWm = (WindowManager) context.getSystemService("window");
        this.mUm = UserManager.get(context);
        this.mDisplay = this.mWm.getDefaultDisplay();
        this.mRecentsPackage = context.getPackageName();
        this.mIsSafeMode = this.mPm.isSafeMode();
        ActivityManager activityManager = this.mAm;
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        Resources resources = context.getResources();
        this.mDummyThumbnailWidth = resources.getDimensionPixelSize(17104898);
        this.mDummyThumbnailHeight = resources.getDimensionPixelSize(17104897);
        this.mBgProtectionPaint = new Paint();
        this.mBgProtectionPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        this.mBgProtectionPaint.setColor(-1);
        this.mBgProtectionCanvas = new Canvas();
        ((UserInfoController) Dependency.get(UserInfoController.class)).addCallback(this.mOnUserInfoChangedListener);
    }

    public static synchronized SystemServicesProxy getInstance(Context context) {
        SystemServicesProxy systemServicesProxy;
        synchronized (SystemServicesProxy.class) {
            if (sSystemServicesProxy == null) {
                sSystemServicesProxy = new SystemServicesProxy(context);
            }
            systemServicesProxy = sSystemServicesProxy;
        }
        return systemServicesProxy;
    }

    public void gc() {
        BackgroundThread.getHandler().post(this.mGcRunnable);
    }

    public boolean isRecentsActivityVisible() {
        return isRecentsActivityVisible(null);
    }

    public boolean isRecentsActivityVisible(MutableBoolean mutableBoolean) {
        if (this.mIam == null) {
            return false;
        }
        try {
            List allStackInfos = this.mIam.getAllStackInfos();
            ActivityManager.StackInfo stackInfo = null;
            ActivityManager.StackInfo stackInfo2 = null;
            ActivityManager.StackInfo stackInfo3 = null;
            for (int i = 0; i < allStackInfos.size(); i++) {
                ActivityManager.StackInfo stackInfo4 = (ActivityManager.StackInfo) allStackInfos.get(i);
                WindowConfiguration windowConfiguration = stackInfo4.configuration.windowConfiguration;
                int activityType = windowConfiguration.getActivityType();
                int windowingMode = windowConfiguration.getWindowingMode();
                if (stackInfo == null && activityType == 2) {
                    stackInfo = stackInfo4;
                } else if (stackInfo2 == null && activityType == 1 && (windowingMode == 1 || windowingMode == 4)) {
                    stackInfo2 = stackInfo4;
                } else if (stackInfo3 == null && activityType == 3) {
                    stackInfo3 = stackInfo4;
                }
            }
            boolean isStackNotOccluded = isStackNotOccluded(stackInfo, stackInfo2);
            boolean isStackNotOccluded2 = isStackNotOccluded(stackInfo3, stackInfo2);
            if (mutableBoolean != null) {
                mutableBoolean.value = isStackNotOccluded;
            }
            ComponentName componentName = stackInfo3 != null ? stackInfo3.topActivity : null;
            if (isStackNotOccluded2 && componentName != null && componentName.getPackageName().equals("com.android.systemui")) {
                return Recents.RECENTS_ACTIVITIES.contains(componentName.getClassName());
            }
            return false;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isStackNotOccluded(ActivityManager.StackInfo stackInfo, ActivityManager.StackInfo stackInfo2) {
        boolean z = false;
        boolean z2 = stackInfo == null || stackInfo.visible;
        if (stackInfo2 != null && stackInfo != null) {
            if (stackInfo2.visible && stackInfo2.position > stackInfo.position) {
                z = true;
            }
            return z2 & (!z);
        }
        return z2;
    }

    public boolean isInSafeMode() {
        return this.mIsSafeMode;
    }

    public boolean setTaskWindowingModeSplitScreenPrimary(int i, int i2, Rect rect) {
        if (this.mIam == null) {
            return false;
        }
        try {
            return this.mIam.setTaskWindowingModeSplitScreenPrimary(i, i2, true, false, rect, true);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ActivityManager.StackInfo getSplitScreenPrimaryStack() {
        try {
            return this.mIam.getStackInfo(3, 0);
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean hasDockedTask() {
        ActivityManager.StackInfo splitScreenPrimaryStack;
        if (this.mIam == null || (splitScreenPrimaryStack = getSplitScreenPrimaryStack()) == null) {
            return false;
        }
        int currentUser = getCurrentUser();
        boolean z = false;
        for (int length = splitScreenPrimaryStack.taskUserIds.length - 1; length >= 0 && !z; length--) {
            z = splitScreenPrimaryStack.taskUserIds[length] == currentUser;
        }
        return z;
    }

    public boolean hasSoftNavigationBar() {
        try {
            return this.mIwm.hasNavigationBar();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasTransposedNavigationBar() {
        Rect rect = new Rect();
        getStableInsets(rect);
        return rect.right > 0;
    }

    public boolean isSystemUser(int i) {
        return i == 0;
    }

    public int getCurrentUser() {
        return this.mCurrentUserId;
    }

    public int getProcessUser() {
        if (this.mUm == null) {
            return 0;
        }
        return this.mUm.getUserHandle();
    }

    public boolean isTouchExplorationEnabled() {
        return this.mAccm != null && this.mAccm.isEnabled() && this.mAccm.isTouchExplorationEnabled();
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

    public Rect getWindowRect() {
        Rect rect = new Rect();
        try {
            if (this.mIam == null) {
                return rect;
            }
            try {
                ActivityManager.StackInfo stackInfo = this.mIam.getStackInfo(0, 3);
                if (stackInfo == null) {
                    stackInfo = this.mIam.getStackInfo(1, 1);
                }
                if (stackInfo != null) {
                    rect.set(stackInfo.bounds);
                }
                return rect;
            } catch (RemoteException e) {
                e.printStackTrace();
                return rect;
            }
        } catch (Throwable th) {
        }
    }

    public void startActivityAsUserAsync(final Intent intent, final ActivityOptions activityOptions) {
        this.mUiOffloadThread.submit(new Runnable() { // from class: com.android.systemui.recents.misc.-$$Lambda$SystemServicesProxy$N7nq4D_yvcF7wooCA6t2HP24UJI
            @Override // java.lang.Runnable
            public final void run() {
                SystemServicesProxy.this.mContext.startActivityAsUser(intent, r4 != null ? activityOptions.toBundle() : null, UserHandle.CURRENT);
            }
        });
    }

    public void startInPlaceAnimationOnFrontMostApplication(ActivityOptions activityOptions) {
        if (this.mIam == null) {
            return;
        }
        try {
            this.mIam.startInPlaceAnimationOnFrontMostApplication(activityOptions == null ? null : activityOptions.toBundle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerDockedStackListener(IDockedStackListener iDockedStackListener) {
        if (this.mWm == null) {
            return;
        }
        try {
            this.mIwm.registerDockedStackListener(iDockedStackListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getDockedDividerSize(Context context) {
        Resources resources = context.getResources();
        return resources.getDimensionPixelSize(17105034) - (2 * resources.getDimensionPixelSize(17105033));
    }

    public void requestKeyboardShortcuts(Context context, WindowManager.KeyboardShortcutsReceiver keyboardShortcutsReceiver, int i) {
        this.mWm.requestAppKeyboardShortcuts(keyboardShortcutsReceiver, i);
    }

    public void getStableInsets(Rect rect) {
        if (this.mWm == null) {
            return;
        }
        try {
            this.mIwm.getStableInsets(0, rect);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRecentsVisibility(final boolean z) {
        this.mUiOffloadThread.submit(new Runnable() { // from class: com.android.systemui.recents.misc.-$$Lambda$SystemServicesProxy$ve6L74feVQWkpga-S7KU2FyhUuE
            @Override // java.lang.Runnable
            public final void run() {
                SystemServicesProxy.lambda$setRecentsVisibility$2(SystemServicesProxy.this, z);
            }
        });
    }

    public static /* synthetic */ void lambda$setRecentsVisibility$2(SystemServicesProxy systemServicesProxy, boolean z) {
        try {
            systemServicesProxy.mIwm.setRecentsVisibility(z);
        } catch (RemoteException e) {
            Log.e("SystemServicesProxy", "Unable to reach window manager", e);
        }
    }

    public void setPipVisibility(final boolean z) {
        this.mUiOffloadThread.submit(new Runnable() { // from class: com.android.systemui.recents.misc.-$$Lambda$SystemServicesProxy$yx6uKMR_Ve4h8p3CZXLJmEmcWpI
            @Override // java.lang.Runnable
            public final void run() {
                SystemServicesProxy.lambda$setPipVisibility$3(SystemServicesProxy.this, z);
            }
        });
    }

    public static /* synthetic */ void lambda$setPipVisibility$3(SystemServicesProxy systemServicesProxy, boolean z) {
        try {
            systemServicesProxy.mIwm.setPipVisibility(z);
        } catch (RemoteException e) {
            Log.e("SystemServicesProxy", "Unable to reach window manager", e);
        }
    }

    public boolean isDreaming() {
        try {
            return this.mDreamManager.isDreaming();
        } catch (RemoteException e) {
            Log.e("SystemServicesProxy", "Failed to query dream manager.", e);
            return false;
        }
    }

    public void awakenDreamsAsync() {
        this.mUiOffloadThread.submit(new Runnable() { // from class: com.android.systemui.recents.misc.-$$Lambda$SystemServicesProxy$qne41-2ZLAnF5q0szRXUTgxl7BA
            @Override // java.lang.Runnable
            public final void run() {
                SystemServicesProxy.lambda$awakenDreamsAsync$4(SystemServicesProxy.this);
            }
        });
    }

    public static /* synthetic */ void lambda$awakenDreamsAsync$4(SystemServicesProxy systemServicesProxy) {
        try {
            systemServicesProxy.mDreamManager.awaken();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
