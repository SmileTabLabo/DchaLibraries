package com.android.launcher3.logging;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.DropTarget;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.userevent.nano.LauncherLogProto;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.InstantAppResolver;
import java.util.Locale;
import java.util.UUID;
/* loaded from: classes.dex */
public class UserEventDispatcher {
    private static final boolean IS_VERBOSE = false;
    private static final int MAXIMUM_VIEW_HIERARCHY_LEVEL = 5;
    private static final String TAG = "UserEvent";
    private static final String UUID_STORAGE = "uuid";
    private long mActionDurationMillis;
    private boolean mAppOrTaskLaunch;
    private UserEventDelegate mDelegate;
    private long mElapsedContainerMillis;
    private long mElapsedSessionMillis;
    protected InstantAppResolver mInstantAppResolver;
    private boolean mIsInLandscapeMode;
    private boolean mIsInMultiWindowMode;
    private boolean mSessionStarted;
    private String mUuidStr;

    /* loaded from: classes.dex */
    public interface LogContainerProvider {
        void fillInLogContainerData(View view, ItemInfo itemInfo, LauncherLogProto.Target target, LauncherLogProto.Target target2);
    }

    /* loaded from: classes.dex */
    public interface UserEventDelegate {
        void modifyUserEvent(LauncherLogProto.LauncherEvent launcherEvent);
    }

    public static UserEventDispatcher newInstance(Context context, DeviceProfile deviceProfile, UserEventDelegate userEventDelegate) {
        SharedPreferences devicePrefs = Utilities.getDevicePrefs(context);
        String string = devicePrefs.getString(UUID_STORAGE, null);
        if (string == null) {
            string = UUID.randomUUID().toString();
            devicePrefs.edit().putString(UUID_STORAGE, string).apply();
        }
        UserEventDispatcher userEventDispatcher = (UserEventDispatcher) Utilities.getOverrideObject(UserEventDispatcher.class, context.getApplicationContext(), R.string.user_event_dispatcher_class);
        userEventDispatcher.mDelegate = userEventDelegate;
        userEventDispatcher.mIsInLandscapeMode = deviceProfile.isVerticalBarLayout();
        userEventDispatcher.mIsInMultiWindowMode = deviceProfile.isMultiWindowMode;
        userEventDispatcher.mUuidStr = string;
        userEventDispatcher.mInstantAppResolver = InstantAppResolver.newInstance(context);
        return userEventDispatcher;
    }

    public static UserEventDispatcher newInstance(Context context, DeviceProfile deviceProfile) {
        return newInstance(context, deviceProfile, null);
    }

    public static LogContainerProvider getLaunchProviderRecursive(@Nullable View view) {
        if (view == null) {
            return null;
        }
        ViewParent parent = view.getParent();
        int i = 5;
        while (parent != null) {
            int i2 = i - 1;
            if (i <= 0) {
                break;
            } else if (parent instanceof LogContainerProvider) {
                return (LogContainerProvider) parent;
            } else {
                parent = parent.getParent();
                i = i2;
            }
        }
        return null;
    }

    protected boolean fillInLogContainerData(LauncherLogProto.LauncherEvent launcherEvent, @Nullable View view) {
        LogContainerProvider launchProviderRecursive = getLaunchProviderRecursive(view);
        if (view == null || !(view.getTag() instanceof ItemInfo) || launchProviderRecursive == null) {
            return false;
        }
        launchProviderRecursive.fillInLogContainerData(view, (ItemInfo) view.getTag(), launcherEvent.srcTarget[0], launcherEvent.srcTarget[1]);
        return true;
    }

    public void logAppLaunch(View view, Intent intent) {
        LauncherLogProto.LauncherEvent newLauncherEvent = LoggerUtils.newLauncherEvent(LoggerUtils.newTouchAction(0), LoggerUtils.newItemTarget(view, this.mInstantAppResolver), LoggerUtils.newTarget(3));
        if (fillInLogContainerData(newLauncherEvent, view)) {
            if (this.mDelegate != null) {
                this.mDelegate.modifyUserEvent(newLauncherEvent);
            }
            fillIntentInfo(newLauncherEvent.srcTarget[0], intent);
        }
        dispatchUserEvent(newLauncherEvent, intent);
        this.mAppOrTaskLaunch = true;
    }

    public void logActionTip(int i, int i2) {
    }

    public void logTaskLaunchOrDismiss(int i, int i2, int i3, ComponentKey componentKey) {
        LauncherLogProto.LauncherEvent newLauncherEvent = LoggerUtils.newLauncherEvent(LoggerUtils.newTouchAction(i), LoggerUtils.newTarget(1));
        if (i == 3 || i == 4) {
            newLauncherEvent.action.dir = i2;
        }
        newLauncherEvent.srcTarget[0].itemType = 9;
        newLauncherEvent.srcTarget[0].pageIndex = i3;
        fillComponentInfo(newLauncherEvent.srcTarget[0], componentKey.componentName);
        dispatchUserEvent(newLauncherEvent, null);
        this.mAppOrTaskLaunch = true;
    }

    protected void fillIntentInfo(LauncherLogProto.Target target, Intent intent) {
        target.intentHash = intent.hashCode();
        fillComponentInfo(target, intent.getComponent());
    }

    private void fillComponentInfo(LauncherLogProto.Target target, ComponentName componentName) {
        if (componentName != null) {
            target.packageNameHash = (this.mUuidStr + componentName.getPackageName()).hashCode();
            target.componentHash = (this.mUuidStr + componentName.flattenToString()).hashCode();
        }
    }

    public void logNotificationLaunch(View view, PendingIntent pendingIntent) {
        LauncherLogProto.LauncherEvent newLauncherEvent = LoggerUtils.newLauncherEvent(LoggerUtils.newTouchAction(0), LoggerUtils.newItemTarget(view, this.mInstantAppResolver), LoggerUtils.newTarget(3));
        if (fillInLogContainerData(newLauncherEvent, view)) {
            LauncherLogProto.Target target = newLauncherEvent.srcTarget[0];
            target.packageNameHash = (this.mUuidStr + pendingIntent.getCreatorPackage()).hashCode();
        }
        dispatchUserEvent(newLauncherEvent, null);
    }

    public void logActionCommand(int i, LauncherLogProto.Target target) {
        logActionCommand(i, target, (LauncherLogProto.Target) null);
    }

    public void logActionCommand(int i, int i2, int i3) {
        logActionCommand(i, LoggerUtils.newContainerTarget(i2), i3 >= 0 ? LoggerUtils.newContainerTarget(i3) : null);
    }

    public void logActionCommand(int i, LauncherLogProto.Target target, LauncherLogProto.Target target2) {
        LauncherLogProto.LauncherEvent newLauncherEvent = LoggerUtils.newLauncherEvent(LoggerUtils.newCommandAction(i), target);
        if (i == 5 && (this.mAppOrTaskLaunch || !this.mSessionStarted)) {
            this.mSessionStarted = false;
            return;
        }
        if (target2 != null) {
            newLauncherEvent.destTarget = new LauncherLogProto.Target[1];
            newLauncherEvent.destTarget[0] = target2;
            newLauncherEvent.action.isStateChange = true;
        }
        dispatchUserEvent(newLauncherEvent, null);
    }

    public void logActionCommand(int i, View view, int i2) {
        LauncherLogProto.LauncherEvent newLauncherEvent = LoggerUtils.newLauncherEvent(LoggerUtils.newCommandAction(i), LoggerUtils.newItemTarget(view, this.mInstantAppResolver), LoggerUtils.newTarget(3));
        if (fillInLogContainerData(newLauncherEvent, view)) {
            newLauncherEvent.srcTarget[0].type = 3;
            newLauncherEvent.srcTarget[0].containerType = i2;
        }
        dispatchUserEvent(newLauncherEvent, null);
    }

    public void logActionOnControl(int i, int i2) {
        logActionOnControl(i, i2, (View) null, -1);
    }

    public void logActionOnControl(int i, int i2, int i3) {
        logActionOnControl(i, i2, (View) null, i3);
    }

    public void logActionOnControl(int i, int i2, @Nullable View view) {
        logActionOnControl(i, i2, view, -1);
    }

    public void logActionOnControl(int i, int i2, int i3, int i4) {
        dispatchUserEvent(LoggerUtils.newLauncherEvent(LoggerUtils.newTouchAction(i), LoggerUtils.newControlTarget(i2), LoggerUtils.newContainerTarget(i3), LoggerUtils.newContainerTarget(i4)), null);
    }

    public void logActionOnControl(int i, int i2, @Nullable View view, int i3) {
        LauncherLogProto.LauncherEvent newLauncherEvent;
        if (view == null && i3 < 0) {
            newLauncherEvent = LoggerUtils.newLauncherEvent(LoggerUtils.newTouchAction(i), LoggerUtils.newTarget(2));
        } else {
            newLauncherEvent = LoggerUtils.newLauncherEvent(LoggerUtils.newTouchAction(i), LoggerUtils.newTarget(2), LoggerUtils.newTarget(3));
        }
        newLauncherEvent.srcTarget[0].controlType = i2;
        if (view != null) {
            fillInLogContainerData(newLauncherEvent, view);
        }
        if (i3 >= 0) {
            newLauncherEvent.srcTarget[1].containerType = i3;
        }
        if (i == 2) {
            newLauncherEvent.actionDurationMillis = SystemClock.uptimeMillis() - this.mActionDurationMillis;
        }
        dispatchUserEvent(newLauncherEvent, null);
    }

    public void logActionTapOutside(LauncherLogProto.Target target) {
        LauncherLogProto.LauncherEvent newLauncherEvent = LoggerUtils.newLauncherEvent(LoggerUtils.newTouchAction(0), target);
        newLauncherEvent.action.isOutside = true;
        dispatchUserEvent(newLauncherEvent, null);
    }

    public void logActionBounceTip(int i) {
        LauncherLogProto.LauncherEvent newLauncherEvent = LoggerUtils.newLauncherEvent(LoggerUtils.newAction(3), LoggerUtils.newContainerTarget(i));
        newLauncherEvent.srcTarget[0].tipType = 1;
        dispatchUserEvent(newLauncherEvent, null);
    }

    public void logActionOnContainer(int i, int i2, int i3) {
        logActionOnContainer(i, i2, i3, 0);
    }

    public void logActionOnContainer(int i, int i2, int i3, int i4) {
        LauncherLogProto.LauncherEvent newLauncherEvent = LoggerUtils.newLauncherEvent(LoggerUtils.newTouchAction(i), LoggerUtils.newContainerTarget(i3));
        newLauncherEvent.action.dir = i2;
        newLauncherEvent.srcTarget[0].pageIndex = i4;
        dispatchUserEvent(newLauncherEvent, null);
    }

    public void logStateChangeAction(int i, int i2, int i3, int i4, int i5, int i6) {
        LauncherLogProto.LauncherEvent newLauncherEvent;
        if (i3 == 9) {
            newLauncherEvent = LoggerUtils.newLauncherEvent(LoggerUtils.newTouchAction(i), LoggerUtils.newItemTarget(i3), LoggerUtils.newContainerTarget(i4));
        } else {
            newLauncherEvent = LoggerUtils.newLauncherEvent(LoggerUtils.newTouchAction(i), LoggerUtils.newContainerTarget(i3), LoggerUtils.newContainerTarget(i4));
        }
        newLauncherEvent.destTarget = new LauncherLogProto.Target[1];
        newLauncherEvent.destTarget[0] = LoggerUtils.newContainerTarget(i5);
        newLauncherEvent.action.dir = i2;
        newLauncherEvent.action.isStateChange = true;
        newLauncherEvent.srcTarget[0].pageIndex = i6;
        dispatchUserEvent(newLauncherEvent, null);
        resetElapsedContainerMillis("state changed");
    }

    public void logActionOnItem(int i, int i2, int i3) {
        LauncherLogProto.Target newTarget = LoggerUtils.newTarget(1);
        newTarget.itemType = i3;
        LauncherLogProto.LauncherEvent newLauncherEvent = LoggerUtils.newLauncherEvent(LoggerUtils.newTouchAction(i), newTarget);
        newLauncherEvent.action.dir = i2;
        dispatchUserEvent(newLauncherEvent, null);
    }

    public void logDeepShortcutsOpen(View view) {
        LogContainerProvider launchProviderRecursive = getLaunchProviderRecursive(view);
        if (view == null || !(view.getTag() instanceof ItemInfo)) {
            return;
        }
        ItemInfo itemInfo = (ItemInfo) view.getTag();
        LauncherLogProto.LauncherEvent newLauncherEvent = LoggerUtils.newLauncherEvent(LoggerUtils.newTouchAction(1), LoggerUtils.newItemTarget(itemInfo, this.mInstantAppResolver), LoggerUtils.newTarget(3));
        launchProviderRecursive.fillInLogContainerData(view, itemInfo, newLauncherEvent.srcTarget[0], newLauncherEvent.srcTarget[1]);
        dispatchUserEvent(newLauncherEvent, null);
        resetElapsedContainerMillis("deep shortcut open");
    }

    public void logOverviewReorder() {
        dispatchUserEvent(LoggerUtils.newLauncherEvent(LoggerUtils.newTouchAction(2), LoggerUtils.newContainerTarget(1), LoggerUtils.newContainerTarget(6)), null);
    }

    public void logDragNDrop(DropTarget.DragObject dragObject, View view) {
        LauncherLogProto.LauncherEvent newLauncherEvent = LoggerUtils.newLauncherEvent(LoggerUtils.newTouchAction(2), LoggerUtils.newItemTarget(dragObject.originalDragInfo, this.mInstantAppResolver), LoggerUtils.newTarget(3));
        newLauncherEvent.destTarget = new LauncherLogProto.Target[]{LoggerUtils.newItemTarget(dragObject.originalDragInfo, this.mInstantAppResolver), LoggerUtils.newDropTarget(view)};
        dragObject.dragSource.fillInLogContainerData(null, dragObject.originalDragInfo, newLauncherEvent.srcTarget[0], newLauncherEvent.srcTarget[1]);
        if (view instanceof LogContainerProvider) {
            ((LogContainerProvider) view).fillInLogContainerData(null, dragObject.dragInfo, newLauncherEvent.destTarget[0], newLauncherEvent.destTarget[1]);
        }
        newLauncherEvent.actionDurationMillis = SystemClock.uptimeMillis() - this.mActionDurationMillis;
        dispatchUserEvent(newLauncherEvent, null);
    }

    public final void resetElapsedContainerMillis(String str) {
        this.mElapsedContainerMillis = SystemClock.uptimeMillis();
        if (!IS_VERBOSE) {
            return;
        }
        Log.d("UserEvent", "resetElapsedContainerMillis reason=" + str);
    }

    public final void startSession() {
        this.mSessionStarted = true;
        this.mElapsedSessionMillis = SystemClock.uptimeMillis();
        this.mElapsedContainerMillis = SystemClock.uptimeMillis();
    }

    public final void resetActionDurationMillis() {
        this.mActionDurationMillis = SystemClock.uptimeMillis();
    }

    public void dispatchUserEvent(LauncherLogProto.LauncherEvent launcherEvent, Intent intent) {
        this.mAppOrTaskLaunch = false;
        launcherEvent.isInLandscapeMode = this.mIsInLandscapeMode;
        launcherEvent.isInMultiWindowMode = this.mIsInMultiWindowMode;
        launcherEvent.elapsedContainerMillis = SystemClock.uptimeMillis() - this.mElapsedContainerMillis;
        launcherEvent.elapsedSessionMillis = SystemClock.uptimeMillis() - this.mElapsedSessionMillis;
        if (!IS_VERBOSE) {
            return;
        }
        String str = "\n-----------------------------------------------------\naction:" + LoggerUtils.getActionStr(launcherEvent.action);
        if (launcherEvent.srcTarget != null && launcherEvent.srcTarget.length > 0) {
            str = str + "\n Source " + getTargetsStr(launcherEvent.srcTarget);
        }
        if (launcherEvent.destTarget != null && launcherEvent.destTarget.length > 0) {
            str = str + "\n Destination " + getTargetsStr(launcherEvent.destTarget);
        }
        Log.d("UserEvent", (((str + String.format(Locale.US, "\n Elapsed container %d ms, session %d ms, action %d ms", Long.valueOf(launcherEvent.elapsedContainerMillis), Long.valueOf(launcherEvent.elapsedSessionMillis), Long.valueOf(launcherEvent.actionDurationMillis))) + "\n isInLandscapeMode " + launcherEvent.isInLandscapeMode) + "\n isInMultiWindowMode " + launcherEvent.isInMultiWindowMode) + "\n\n");
    }

    private static String getTargetsStr(LauncherLogProto.Target[] targetArr) {
        String str = "child:" + LoggerUtils.getTargetStr(targetArr[0]);
        for (int i = 1; i < targetArr.length; i++) {
            str = str + "\tparent:" + LoggerUtils.getTargetStr(targetArr[i]);
        }
        return str;
    }
}
