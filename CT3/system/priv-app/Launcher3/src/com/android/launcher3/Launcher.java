package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BenesseExtension;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Advanceable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.launcher3.CellLayout;
import com.android.launcher3.DragLayer;
import com.android.launcher3.DropTarget;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.PagedView;
import com.android.launcher3.SearchDropTargetBar;
import com.android.launcher3.Workspace;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.DefaultAppSearchController;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.model.WidgetsModel;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.LongArrayMap;
import com.android.launcher3.util.PackageManagerHelper;
import com.android.launcher3.widget.PendingAddWidgetInfo;
import com.android.launcher3.widget.WidgetHostViewLoader;
import com.android.launcher3.widget.WidgetsContainerView;
import com.mediatek.launcher3.LauncherHelper;
import com.mediatek.launcher3.LauncherLog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
/* loaded from: a.zip:com/android/launcher3/Launcher.class */
public class Launcher extends Activity implements View.OnClickListener, View.OnLongClickListener, LauncherModel.Callbacks, View.OnTouchListener, PagedView.PageSwitchListener, LauncherProviderChangeListener {
    private static PendingAddArguments sPendingAddItem;
    private View mAllAppsButton;
    private LauncherAppWidgetHost mAppWidgetHost;
    private AppWidgetManagerCompat mAppWidgetManager;
    AllAppsContainerView mAppsView;
    private long mAutoAdvanceSentTime;
    private LauncherClings mClings;
    private DeviceProfile mDeviceProfile;
    private DragController mDragController;
    DragLayer mDragLayer;
    FocusIndicatorView mFocusHandler;
    private Bitmap mFolderIconBitmap;
    private Canvas mFolderIconCanvas;
    ImageView mFolderIconImageView;
    private View.OnTouchListener mHapticFeedbackTouchListener;
    Hotseat mHotseat;
    private IconCache mIconCache;
    private LayoutInflater mInflater;
    private boolean mIsSafeModeEnabled;
    private LauncherCallbacks mLauncherCallbacks;
    private View mLauncherView;
    private LauncherModel mModel;
    private boolean mMoveToDefaultScreenFromNewIntent;
    private boolean mOnResumeNeedsLoad;
    private ViewGroup mOverviewPanel;
    private View mPageIndicators;
    private LauncherAppWidgetProviderInfo mPendingAddWidgetInfo;
    private AppWidgetHostView mQsb;
    private boolean mRestoring;
    private Bundle mSavedInstanceState;
    private Bundle mSavedState;
    private SearchDropTargetBar mSearchDropTargetBar;
    private SharedPreferences mSharedPrefs;
    LauncherStateTransitionAnimation mStateTransitionAnimation;
    private Stats mStats;
    ArrayList<AppInfo> mTmpAppsList;
    private boolean mWaitingForResult;
    private BubbleTextView mWaitingForResume;
    private View mWidgetsButton;
    WidgetsModel mWidgetsModel;
    WidgetsContainerView mWidgetsView;
    Workspace mWorkspace;
    Drawable mWorkspaceBackgroundDrawable;
    private static int NEW_APPS_PAGE_MOVE_DELAY = 500;
    private static int NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS = 5;
    static int NEW_APPS_ANIMATION_DELAY = 500;
    private static LongArrayMap<FolderInfo> sFolders = new LongArrayMap<>();
    static final ArrayList<String> sDumpLogs = new ArrayList<>();
    static Date sDateStamp = new Date();
    static DateFormat sDateFormat = DateFormat.getDateTimeInstance(3, 3);
    static long sRunStart = System.currentTimeMillis();
    protected static HashMap<String, CustomAppWidget> sCustomAppWidgets = new HashMap<>();
    State mState = State.WORKSPACE;
    private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsIntentReceiver(this);
    PendingAddItemInfo mPendingAddInfo = new PendingAddItemInfo();
    private int mPendingAddWidgetId = -1;
    private int[] mTmpAddItemCellCoordinates = new int[2];
    private boolean mAutoAdvanceRunning = false;
    private State mOnResumeState = State.NONE;
    private SpannableStringBuilder mDefaultKeySsb = null;
    boolean mWorkspaceLoading = true;
    private boolean mPaused = true;
    private ArrayList<Runnable> mBindOnResumeCallbacks = new ArrayList<>();
    private ArrayList<Runnable> mOnResumeCallbacks = new ArrayList<>();
    boolean mUserPresent = true;
    private boolean mVisible = false;
    private boolean mHasFocus = false;
    private boolean mAttached = false;
    private final int ADVANCE_MSG = 1;
    private final int mAdvanceInterval = 20000;
    private final int mAdvanceStagger = 250;
    private long mAutoAdvanceTimeLeft = -1;
    HashMap<View, AppWidgetProviderInfo> mWidgetsToAdvance = new HashMap<>();
    private final int mRestoreScreenOrientationDelay = 500;
    private final ArrayList<Integer> mSynchronouslyBoundPages = new ArrayList<>();
    private Rect mRectForFolderAnimation = new Rect();
    Runnable mBuildLayersRunnable = new Runnable(this) { // from class: com.android.launcher3.Launcher.1
        final Launcher this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.this$0.mWorkspace != null) {
                this.this$0.mWorkspace.buildPageHardwareLayers();
            }
        }
    };
    private boolean mRotationEnabled = false;
    private Runnable mUpdateOrientationRunnable = new Runnable(this) { // from class: com.android.launcher3.Launcher.2
        final Launcher this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.setOrientation();
        }
    };
    private int mCurrentWorkSpaceScreen = -1001;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver(this) { // from class: com.android.launcher3.Launcher.3
        final Launcher this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!"android.intent.action.SCREEN_OFF".equals(action)) {
                if ("android.intent.action.USER_PRESENT".equals(action)) {
                    this.this$0.mUserPresent = true;
                    this.this$0.updateAutoAdvanceState();
                    return;
                }
                return;
            }
            LauncherLog.d("Launcher", "ACTION_SCREEN_OFF: mPendingAddInfo = " + this.this$0.mPendingAddInfo + ", this = " + this);
            this.this$0.mUserPresent = false;
            this.this$0.mDragLayer.clearAllResizeFrames();
            this.this$0.updateAutoAdvanceState();
            if (this.this$0.mAppsView == null || this.this$0.mWidgetsView == null || this.this$0.mPendingAddInfo.container != -1 || this.this$0.showWorkspace(false)) {
                return;
            }
            this.this$0.mAppsView.reset();
        }
    };
    final Handler mHandler = new Handler(new Handler.Callback(this) { // from class: com.android.launcher3.Launcher.4
        final Launcher this$0;

        {
            this.this$0 = this;
        }

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message message) {
            if (message.what == 1) {
                int i = 0;
                for (View view : this.this$0.mWidgetsToAdvance.keySet()) {
                    View findViewById = view.findViewById(this.this$0.mWidgetsToAdvance.get(view).autoAdvanceViewId);
                    if (findViewById instanceof Advanceable) {
                        this.this$0.mHandler.postDelayed(new Runnable(this, findViewById) { // from class: com.android.launcher3.Launcher.4.1
                            final AnonymousClass4 this$1;
                            final View val$v;

                            {
                                this.this$1 = this;
                                this.val$v = findViewById;
                            }

                            @Override // java.lang.Runnable
                            public void run() {
                                ((Advanceable) this.val$v).advance();
                            }
                        }, i * 250);
                    }
                    i++;
                }
                this.this$0.sendAdvanceMessage(20000L);
                return true;
            }
            return true;
        }
    });
    private Runnable mBindAllApplicationsRunnable = new Runnable(this) { // from class: com.android.launcher3.Launcher.5
        final Launcher this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.bindAllApplications(this.this$0.mTmpAppsList);
            this.this$0.mTmpAppsList = null;
        }
    };
    private Runnable mBindWidgetModelRunnable = new Runnable(this) { // from class: com.android.launcher3.Launcher.6
        final Launcher this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.bindWidgetsModel(this.this$0.mWidgetsModel);
        }
    };

    /* renamed from: com.android.launcher3.Launcher$16  reason: invalid class name */
    /* loaded from: a.zip:com/android/launcher3/Launcher$16.class */
    class AnonymousClass16 implements ViewTreeObserver.OnDrawListener {
        private boolean mStarted = false;
        final Launcher this$0;

        AnonymousClass16(Launcher launcher) {
            this.this$0 = launcher;
        }

        @Override // android.view.ViewTreeObserver.OnDrawListener
        public void onDraw() {
            if (this.mStarted) {
                return;
            }
            this.mStarted = true;
            this.this$0.mWorkspace.postDelayed(this.this$0.mBuildLayersRunnable, 500L);
            this.this$0.mWorkspace.post(new Runnable(this, this) { // from class: com.android.launcher3.Launcher.16.1
                final AnonymousClass16 this$1;
                final ViewTreeObserver.OnDrawListener val$listener;

                {
                    this.this$1 = this;
                    this.val$listener = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (this.this$1.this$0.mWorkspace == null || this.this$1.this$0.mWorkspace.getViewTreeObserver() == null) {
                        return;
                    }
                    this.this$1.this$0.mWorkspace.getViewTreeObserver().removeOnDrawListener(this.val$listener);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/Launcher$AppsUpdateTask.class */
    public class AppsUpdateTask implements Runnable {
        private ArrayList<AppInfo> mApps;
        final Launcher this$0;

        public AppsUpdateTask(Launcher launcher, ArrayList<AppInfo> arrayList) {
            this.this$0 = launcher;
            this.mApps = null;
            this.mApps = arrayList;
        }

        public ArrayList<AppInfo> getApps() {
            return this.mApps;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.bindAppsUpdated(this.mApps);
        }
    }

    /* loaded from: a.zip:com/android/launcher3/Launcher$CloseSystemDialogsIntentReceiver.class */
    class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
        final Launcher this$0;

        CloseSystemDialogsIntentReceiver(Launcher launcher) {
            this.this$0 = launcher;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Object obj;
            Bundle extras = intent.getExtras();
            if (extras != null && (obj = extras.get("reason")) != null) {
                String obj2 = obj.toString();
                LauncherLog.d("Launcher", "Close system dialogs: reason = " + obj2);
                if ("lock".equals(obj2)) {
                    return;
                }
            }
            this.this$0.closeSystemDialogs();
        }
    }

    /* loaded from: a.zip:com/android/launcher3/Launcher$CustomContentCallbacks.class */
    public interface CustomContentCallbacks {
        boolean isScrollingAllowed();

        void onHide();

        void onScrollProgressChanged(float f);

        void onShow(boolean z);
    }

    /* loaded from: a.zip:com/android/launcher3/Launcher$LauncherOverlay.class */
    public interface LauncherOverlay {
        void onScrollChange(float f, boolean z);

        void onScrollInteractionBegin();

        void onScrollInteractionEnd();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/Launcher$PendingAddArguments.class */
    public static class PendingAddArguments {
        int appWidgetId;
        int cellX;
        int cellY;
        long container;
        Intent intent;
        int requestCode;
        long screenId;

        PendingAddArguments() {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/Launcher$State.class */
    public enum State {
        NONE,
        WORKSPACE,
        APPS,
        APPS_SPRING_LOADED,
        WIDGETS,
        WIDGETS_SPRING_LOADED;

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static State[] valuesCustom() {
            return values();
        }
    }

    private boolean acceptFilter() {
        return !((InputMethodManager) getSystemService("input_method")).isFullscreenMode();
    }

    private void addAppWidgetFromDrop(PendingAddWidgetInfo pendingAddWidgetInfo, long j, long j2, int[] iArr, int[] iArr2) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "addAppWidgetFromDrop: info = " + pendingAddWidgetInfo + ", container = " + j + ", screenId = " + j2);
        }
        resetAddInfo();
        PendingAddItemInfo pendingAddItemInfo = this.mPendingAddInfo;
        pendingAddWidgetInfo.container = j;
        pendingAddItemInfo.container = j;
        PendingAddItemInfo pendingAddItemInfo2 = this.mPendingAddInfo;
        pendingAddWidgetInfo.screenId = j2;
        pendingAddItemInfo2.screenId = j2;
        this.mPendingAddInfo.dropPos = null;
        this.mPendingAddInfo.minSpanX = pendingAddWidgetInfo.minSpanX;
        this.mPendingAddInfo.minSpanY = pendingAddWidgetInfo.minSpanY;
        if (iArr != null) {
            this.mPendingAddInfo.cellX = iArr[0];
            this.mPendingAddInfo.cellY = iArr[1];
        }
        if (iArr2 != null) {
            this.mPendingAddInfo.spanX = iArr2[0];
            this.mPendingAddInfo.spanY = iArr2[1];
        }
        AppWidgetHostView appWidgetHostView = pendingAddWidgetInfo.boundWidget;
        if (appWidgetHostView != null) {
            getDragLayer().removeView(appWidgetHostView);
            addAppWidgetFromDropImpl(appWidgetHostView.getAppWidgetId(), pendingAddWidgetInfo, appWidgetHostView, pendingAddWidgetInfo.info);
            pendingAddWidgetInfo.boundWidget = null;
            return;
        }
        int allocateAppWidgetId = getAppWidgetHost().allocateAppWidgetId();
        if (this.mAppWidgetManager.bindAppWidgetIdIfAllowed(allocateAppWidgetId, pendingAddWidgetInfo.info, pendingAddWidgetInfo.bindOptions)) {
            addAppWidgetFromDropImpl(allocateAppWidgetId, pendingAddWidgetInfo, null, pendingAddWidgetInfo.info);
        } else if (BenesseExtension.getDchaState() != 0) {
        } else {
            this.mPendingAddWidgetInfo = pendingAddWidgetInfo.info;
            Intent intent = new Intent("android.appwidget.action.APPWIDGET_BIND");
            intent.putExtra("appWidgetId", allocateAppWidgetId);
            intent.putExtra("appWidgetProvider", pendingAddWidgetInfo.componentName);
            this.mAppWidgetManager.getUser(this.mPendingAddWidgetInfo).addToIntent(intent, "appWidgetProviderProfile");
            startActivityForResult(intent, 11);
        }
    }

    private void addAppWidgetToWorkspace(LauncherAppWidgetInfo launcherAppWidgetInfo, LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, boolean z) {
        launcherAppWidgetInfo.hostView.setTag(launcherAppWidgetInfo);
        launcherAppWidgetInfo.onBindAppWidget(this);
        launcherAppWidgetInfo.hostView.setFocusable(true);
        launcherAppWidgetInfo.hostView.setOnFocusChangeListener(this.mFocusHandler);
        if (this.mWorkspace != null) {
            this.mWorkspace.addInScreen(launcherAppWidgetInfo.hostView, launcherAppWidgetInfo.container, launcherAppWidgetInfo.screenId, launcherAppWidgetInfo.cellX, launcherAppWidgetInfo.cellY, launcherAppWidgetInfo.spanX, launcherAppWidgetInfo.spanY, z);
        } else {
            LauncherLog.d("Launcher", "error , mWorkspace is null");
        }
        if (launcherAppWidgetInfo.isCustomWidget()) {
            return;
        }
        addWidgetToAutoAdvanceIfNeeded(launcherAppWidgetInfo.hostView, launcherAppWidgetProviderInfo);
    }

    public static void addDumpLog(String str, String str2, Exception exc, boolean z) {
        if (z) {
            if (exc != null) {
                Log.d(str, str2, exc);
            } else {
                Log.d(str, str2);
            }
        }
    }

    public static void addDumpLog(String str, String str2, boolean z) {
        addDumpLog(str, str2, null, z);
    }

    private void addWidgetToAutoAdvanceIfNeeded(View view, AppWidgetProviderInfo appWidgetProviderInfo) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "addWidgetToAutoAdvanceIfNeeded hostView = " + view + ", appWidgetInfo = " + appWidgetProviderInfo);
        }
        if (appWidgetProviderInfo == null || appWidgetProviderInfo.autoAdvanceViewId == -1) {
            return;
        }
        View findViewById = view.findViewById(appWidgetProviderInfo.autoAdvanceViewId);
        if (findViewById instanceof Advanceable) {
            this.mWidgetsToAdvance.put(view, appWidgetProviderInfo);
            ((Advanceable) findViewById).fyiWillBeAdvancedByHostKThx();
            updateAutoAdvanceState();
        }
    }

    private void bindSafeModeWidget(LauncherAppWidgetInfo launcherAppWidgetInfo) {
        PendingAppWidgetHostView pendingAppWidgetHostView = new PendingAppWidgetHostView(this, launcherAppWidgetInfo, true);
        pendingAppWidgetHostView.updateIcon(this.mIconCache);
        launcherAppWidgetInfo.hostView = pendingAppWidgetHostView;
        launcherAppWidgetInfo.hostView.updateAppWidget(null);
        launcherAppWidgetInfo.hostView.setOnClickListener(this);
        addAppWidgetToWorkspace(launcherAppWidgetInfo, null, false);
        this.mWorkspace.requestLayout();
    }

    private boolean canRunNewAppsAnimation() {
        boolean z;
        if (System.currentTimeMillis() - this.mDragController.getLastGestureUpTime() > NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS * 1000) {
            z = true;
            if (this.mClings != null) {
                z = true;
                if (this.mClings.isVisible()) {
                    z = false;
                }
            }
        } else {
            z = false;
        }
        return z;
    }

    private void clearTypedText() {
        this.mDefaultKeySsb.clear();
        this.mDefaultKeySsb.clearSpans();
        Selection.setSelection(this.mDefaultKeySsb, 0);
    }

    private long completeAdd(PendingAddArguments pendingAddArguments) {
        long j = pendingAddArguments.screenId;
        if (pendingAddArguments.container == -100) {
            j = ensurePendingDropLayoutExists(pendingAddArguments.screenId);
        }
        switch (pendingAddArguments.requestCode) {
            case 1:
                completeAddShortcut(pendingAddArguments.intent, pendingAddArguments.container, j, pendingAddArguments.cellX, pendingAddArguments.cellY);
                break;
            case 5:
                completeAddAppWidget(pendingAddArguments.appWidgetId, pendingAddArguments.container, j, null, null);
                break;
            case 12:
                completeRestoreAppWidget(pendingAddArguments.appWidgetId);
                break;
        }
        resetAddInfo();
        return j;
    }

    private void completeAddShortcut(Intent intent, long j, long j2, int i, int i2) {
        boolean findCellForSpan;
        int[] iArr = this.mTmpAddItemCellCoordinates;
        int[] iArr2 = this.mPendingAddInfo.dropPos;
        CellLayout cellLayout = getCellLayout(j, j2);
        ShortcutInfo fromShortcutIntent = InstallShortcutReceiver.fromShortcutIntent(this, intent);
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "completeAddShortcut: info = " + fromShortcutIntent + ", data = " + intent + ", container = " + j + ", screenId = " + j2 + ", cellX = " + i + ", cellY = " + i2 + ", layout = " + cellLayout);
        }
        if (fromShortcutIntent == null || this.mPendingAddInfo.componentName == null) {
            return;
        }
        if (!PackageManagerHelper.hasPermissionForActivity(this, fromShortcutIntent.intent, this.mPendingAddInfo.componentName.getPackageName())) {
            Log.e("Launcher", "Ignoring malicious intent " + fromShortcutIntent.intent.toUri(0));
            return;
        }
        View createShortcut = createShortcut(fromShortcutIntent);
        if (i < 0 || i2 < 0) {
            findCellForSpan = iArr2 != null ? cellLayout.findNearestVacantArea(iArr2[0], iArr2[1], 1, 1, iArr) != null : cellLayout.findCellForSpan(iArr, 1, 1);
        } else {
            iArr[0] = i;
            iArr[1] = i2;
            findCellForSpan = true;
            if (this.mWorkspace.createUserFolderIfNecessary(createShortcut, j, cellLayout, iArr, 0.0f, true, null, null)) {
                return;
            }
            DropTarget.DragObject dragObject = new DropTarget.DragObject();
            dragObject.dragInfo = fromShortcutIntent;
            if (this.mWorkspace.addToExistingFolderIfNecessary(createShortcut, cellLayout, iArr, 0.0f, dragObject, true)) {
                return;
            }
        }
        if (!findCellForSpan) {
            showOutOfSpaceMessage(isHotseatLayout(cellLayout));
            return;
        }
        LauncherModel.addItemToDatabase(this, fromShortcutIntent, j, j2, iArr[0], iArr[1]);
        if (this.mRestoring) {
            return;
        }
        this.mWorkspace.addInScreen(createShortcut, j, j2, iArr[0], iArr[1], 1, 1, isWorkspaceLocked());
    }

    private void completeRestoreAppWidget(int i) {
        LauncherAppWidgetHostView widgetForAppWidgetId = this.mWorkspace.getWidgetForAppWidgetId(i);
        if (widgetForAppWidgetId == null || !(widgetForAppWidgetId instanceof PendingAppWidgetHostView)) {
            Log.e("Launcher", "Widget update called, when the widget no longer exists.");
            return;
        }
        LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) widgetForAppWidgetId.getTag();
        launcherAppWidgetInfo.restoreStatus = 0;
        this.mWorkspace.reinflateWidgetsIfNecessary();
        LauncherModel.updateItemInDatabase(this, launcherAppWidgetInfo);
    }

    private void copyFolderIconToImage(FolderIcon folderIcon) {
        int measuredWidth = folderIcon.getMeasuredWidth();
        int measuredHeight = folderIcon.getMeasuredHeight();
        if (this.mFolderIconImageView == null) {
            this.mFolderIconImageView = new ImageView(this);
        }
        if (this.mFolderIconBitmap == null || this.mFolderIconBitmap.getWidth() != measuredWidth || this.mFolderIconBitmap.getHeight() != measuredHeight) {
            this.mFolderIconBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
            this.mFolderIconCanvas = new Canvas(this.mFolderIconBitmap);
        }
        DragLayer.LayoutParams layoutParams = this.mFolderIconImageView.getLayoutParams() instanceof DragLayer.LayoutParams ? (DragLayer.LayoutParams) this.mFolderIconImageView.getLayoutParams() : new DragLayer.LayoutParams(measuredWidth, measuredHeight);
        float descendantRectRelativeToSelf = this.mDragLayer.getDescendantRectRelativeToSelf(folderIcon, this.mRectForFolderAnimation);
        layoutParams.customPosition = true;
        layoutParams.x = this.mRectForFolderAnimation.left;
        layoutParams.y = this.mRectForFolderAnimation.top;
        layoutParams.width = (int) (measuredWidth * descendantRectRelativeToSelf);
        layoutParams.height = (int) (measuredHeight * descendantRectRelativeToSelf);
        this.mFolderIconCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        folderIcon.draw(this.mFolderIconCanvas);
        this.mFolderIconImageView.setImageBitmap(this.mFolderIconBitmap);
        if (folderIcon.getFolder() != null) {
            this.mFolderIconImageView.setPivotX(folderIcon.getFolder().getPivotXForIconAnimation());
            this.mFolderIconImageView.setPivotY(folderIcon.getFolder().getPivotYForIconAnimation());
        }
        if (this.mDragLayer.indexOfChild(this.mFolderIconImageView) != -1) {
            this.mDragLayer.removeView(this.mFolderIconImageView);
        }
        this.mDragLayer.addView(this.mFolderIconImageView, layoutParams);
        if (folderIcon.getFolder() != null) {
            folderIcon.getFolder().bringToFront();
        }
    }

    private ValueAnimator createNewAppBounceAnimation(View view, int i) {
        ObjectAnimator ofPropertyValuesHolder = LauncherAnimUtils.ofPropertyValuesHolder(view, PropertyValuesHolder.ofFloat("alpha", 1.0f), PropertyValuesHolder.ofFloat("scaleX", 1.0f), PropertyValuesHolder.ofFloat("scaleY", 1.0f));
        ofPropertyValuesHolder.setDuration(450L);
        ofPropertyValuesHolder.setStartDelay(i * 85);
        ofPropertyValuesHolder.setInterpolator(new OvershootInterpolator(1.3f));
        return ofPropertyValuesHolder;
    }

    /* JADX WARN: Type inference failed for: r0v8, types: [com.android.launcher3.Launcher$19] */
    private void deleteWidgetInfo(LauncherAppWidgetInfo launcherAppWidgetInfo) {
        LauncherAppWidgetHost appWidgetHost = getAppWidgetHost();
        if (appWidgetHost != null && !launcherAppWidgetInfo.isCustomWidget() && launcherAppWidgetInfo.isWidgetIdValid()) {
            new AsyncTask<Void, Void, Void>(this, appWidgetHost, launcherAppWidgetInfo) { // from class: com.android.launcher3.Launcher.19
                final Launcher this$0;
                final LauncherAppWidgetHost val$appWidgetHost;
                final LauncherAppWidgetInfo val$widgetInfo;

                {
                    this.this$0 = this;
                    this.val$appWidgetHost = appWidgetHost;
                    this.val$widgetInfo = launcherAppWidgetInfo;
                }

                @Override // android.os.AsyncTask
                public Void doInBackground(Void... voidArr) {
                    this.val$appWidgetHost.deleteAppWidgetId(this.val$widgetInfo.appWidgetId);
                    return null;
                }
            }.executeOnExecutor(Utilities.THREAD_POOL_EXECUTOR, new Void[0]);
        }
        LauncherModel.deleteItemFromDatabase(this, launcherAppWidgetInfo);
    }

    private long ensurePendingDropLayoutExists(long j) {
        if (this.mWorkspace.getScreenWithId(j) == null) {
            this.mWorkspace.addExtraEmptyScreen();
            return this.mWorkspace.commitExtraEmptyScreen();
        }
        return j;
    }

    private String getTypedText() {
        return this.mDefaultKeySsb.toString();
    }

    private void growAndFadeOutFolderIcon(FolderIcon folderIcon) {
        if (folderIcon == null) {
            return;
        }
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat("alpha", 0.0f);
        PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat("scaleX", 1.5f);
        PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat("scaleY", 1.5f);
        if (((FolderInfo) folderIcon.getTag()).container == -101) {
            CellLayout cellLayout = (CellLayout) folderIcon.getParent().getParent();
            CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) folderIcon.getLayoutParams();
            cellLayout.setFolderLeaveBehindCell(layoutParams.cellX, layoutParams.cellY);
        }
        copyFolderIconToImage(folderIcon);
        folderIcon.setVisibility(4);
        ObjectAnimator ofPropertyValuesHolder = LauncherAnimUtils.ofPropertyValuesHolder(this.mFolderIconImageView, ofFloat, ofFloat2, ofFloat3);
        if (Utilities.ATLEAST_LOLLIPOP) {
            ofPropertyValuesHolder.setInterpolator(new LogDecelerateInterpolator(100, 0));
        }
        ofPropertyValuesHolder.setDuration(getResources().getInteger(2131427345));
        ofPropertyValuesHolder.start();
    }

    private void handleActivityResult(int i, int i2, Intent intent) {
        setWaitingForResult(false);
        int i3 = this.mPendingAddWidgetId;
        this.mPendingAddWidgetId = -1;
        Runnable runnable = new Runnable(this, i2) { // from class: com.android.launcher3.Launcher.8
            final Launcher this$0;
            final int val$resultCode;

            {
                this.this$0 = this;
                this.val$resultCode = i2;
            }

            @Override // java.lang.Runnable
            public void run() {
                boolean z = false;
                Launcher launcher = this.this$0;
                if (this.val$resultCode != 0) {
                    z = true;
                }
                launcher.exitSpringLoadedDragModeDelayed(z, 300, null);
            }
        };
        if (i == 11) {
            int intExtra = intent != null ? intent.getIntExtra("appWidgetId", -1) : -1;
            if (i2 == 0) {
                completeTwoStageWidgetDrop(0, intExtra);
                this.mWorkspace.removeExtraEmptyScreenDelayed(true, runnable, 500, false);
            } else if (i2 == -1) {
                addAppWidgetImpl(intExtra, this.mPendingAddInfo, null, this.mPendingAddWidgetInfo, 500);
                getOrCreateQsbBar();
            }
        } else if (i == 10) {
            if (i2 == -1 && this.mWorkspace.isInOverviewMode()) {
                this.mWorkspace.setCurrentPage(this.mWorkspace.getPageNearestToCenterOfScreen());
                showWorkspace(false);
            }
        } else {
            boolean z = i != 9 ? i == 5 : true;
            boolean isWorkspaceLocked = isWorkspaceLocked();
            if (LauncherLog.DEBUG) {
                LauncherLog.d("Launcher", "onActivityResult: requestCode = " + i + ", resultCode = " + i2 + ", data = " + intent + ", mPendingAddInfo = " + this.mPendingAddInfo);
            }
            if (!z) {
                if (i == 12) {
                    if (i2 == -1) {
                        PendingAddArguments preparePendingAddArgs = preparePendingAddArgs(i, intent, i3, this.mPendingAddInfo);
                        if (isWorkspaceLocked) {
                            sPendingAddItem = preparePendingAddArgs;
                            return;
                        } else {
                            completeAdd(preparePendingAddArgs);
                            return;
                        }
                    }
                    return;
                }
                if (i2 == -1 && this.mPendingAddInfo.container != -1) {
                    PendingAddArguments preparePendingAddArgs2 = preparePendingAddArgs(i, intent, i3, this.mPendingAddInfo);
                    if (isWorkspaceLocked()) {
                        sPendingAddItem = preparePendingAddArgs2;
                    } else {
                        completeAdd(preparePendingAddArgs2);
                        this.mWorkspace.removeExtraEmptyScreenDelayed(true, runnable, 500, false);
                    }
                } else if (i2 == 0) {
                    this.mWorkspace.removeExtraEmptyScreenDelayed(true, runnable, 500, false);
                }
                this.mDragLayer.clearAnimatedView();
                return;
            }
            int intExtra2 = intent != null ? intent.getIntExtra("appWidgetId", -1) : -1;
            if (intExtra2 < 0) {
                intExtra2 = i3;
            }
            if (intExtra2 < 0 || i2 == 0) {
                Log.e("Launcher", "Error: appWidgetId (EXTRA_APPWIDGET_ID) was not returned from the widget configuration activity.");
                completeTwoStageWidgetDrop(0, intExtra2);
                Runnable runnable2 = new Runnable(this) { // from class: com.android.launcher3.Launcher.9
                    final Launcher this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.exitSpringLoadedDragModeDelayed(false, 0, null);
                    }
                };
                if (isWorkspaceLocked) {
                    this.mWorkspace.postDelayed(runnable2, 500L);
                } else {
                    this.mWorkspace.removeExtraEmptyScreenDelayed(true, runnable2, 500, false);
                }
            } else if (isWorkspaceLocked) {
                sPendingAddItem = preparePendingAddArgs(i, intent, intExtra2, this.mPendingAddInfo);
            } else {
                if (this.mPendingAddInfo.container == -100) {
                    this.mPendingAddInfo.screenId = ensurePendingDropLayoutExists(this.mPendingAddInfo.screenId);
                }
                CellLayout screenWithId = this.mWorkspace.getScreenWithId(this.mPendingAddInfo.screenId);
                screenWithId.setDropPending(true);
                this.mWorkspace.removeExtraEmptyScreenDelayed(true, new Runnable(this, i2, intExtra2, screenWithId) { // from class: com.android.launcher3.Launcher.10
                    final Launcher this$0;
                    final int val$appWidgetId;
                    final CellLayout val$dropLayout;
                    final int val$resultCode;

                    {
                        this.this$0 = this;
                        this.val$resultCode = i2;
                        this.val$appWidgetId = intExtra2;
                        this.val$dropLayout = screenWithId;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.completeTwoStageWidgetDrop(this.val$resultCode, this.val$appWidgetId);
                        this.val$dropLayout.setDropPending(false);
                    }
                }, 500, false);
            }
        }
    }

    private static State intToState(int i) {
        State state;
        State state2 = State.WORKSPACE;
        State[] valuesCustom = State.valuesCustom();
        int i2 = 0;
        while (true) {
            state = state2;
            if (i2 >= valuesCustom.length) {
                break;
            } else if (valuesCustom[i2].ordinal() == i) {
                state = valuesCustom[i2];
                break;
            } else {
                i2++;
            }
        }
        return state;
    }

    private int mapConfigurationOriActivityInfoOri(int i) {
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        switch (defaultDisplay.getRotation()) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
            case 2:
                break;
            case 1:
            case 3:
                if (i != 2) {
                    i = 2;
                    break;
                } else {
                    i = 1;
                    break;
                }
            default:
                i = 2;
                break;
        }
        int i2 = 0;
        if (i == 2) {
            i2 = 1;
        }
        return new int[]{1, 0, 9, 8}[(defaultDisplay.getRotation() + i2) % 4];
    }

    private void markFirstRunActivityShown() {
        SharedPreferences.Editor edit = this.mSharedPrefs.edit();
        edit.putBoolean("launcher.first_run_activity_displayed", true);
        edit.apply();
    }

    private void onStartForResult(int i) {
        if (i >= 0) {
            setWaitingForResult(true);
        }
    }

    private PendingAddArguments preparePendingAddArgs(int i, Intent intent, int i2, ItemInfo itemInfo) {
        PendingAddArguments pendingAddArguments = new PendingAddArguments();
        pendingAddArguments.requestCode = i;
        pendingAddArguments.intent = intent;
        pendingAddArguments.container = itemInfo.container;
        pendingAddArguments.screenId = itemInfo.screenId;
        pendingAddArguments.cellX = itemInfo.cellX;
        pendingAddArguments.cellY = itemInfo.cellY;
        pendingAddArguments.appWidgetId = i2;
        return pendingAddArguments;
    }

    private void processShortcutFromDrop(ComponentName componentName, long j, long j2, int[] iArr) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "processShortcutFromDrop componentName = " + componentName + ", container = " + j + ", screenId = " + j2);
        }
        if (BenesseExtension.getDchaState() != 0) {
            return;
        }
        resetAddInfo();
        this.mPendingAddInfo.container = j;
        this.mPendingAddInfo.screenId = j2;
        this.mPendingAddInfo.dropPos = null;
        this.mPendingAddInfo.componentName = componentName;
        if (iArr != null) {
            this.mPendingAddInfo.cellX = iArr[0];
            this.mPendingAddInfo.cellY = iArr[1];
        }
        Intent intent = new Intent("android.intent.action.CREATE_SHORTCUT");
        intent.setComponent(componentName);
        Utilities.startActivityForResultSafely(this, intent, 1);
    }

    private void reinflateQSBIfNecessary() {
        if ((this.mQsb instanceof LauncherAppWidgetHostView) && ((LauncherAppWidgetHostView) this.mQsb).isReinflateRequired()) {
            this.mSearchDropTargetBar.removeView(this.mQsb);
            this.mQsb = null;
            this.mSearchDropTargetBar.setQsbSearchBar(getOrCreateQsbBar());
        }
    }

    private void removeWidgetToAutoAdvance(View view) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "removeWidgetToAutoAdvance hostView = " + view);
        }
        if (this.mWidgetsToAdvance.containsKey(view)) {
            this.mWidgetsToAdvance.remove(view);
            updateAutoAdvanceState();
        }
    }

    private void resetAddInfo() {
        this.mPendingAddInfo.container = -1L;
        this.mPendingAddInfo.screenId = -1L;
        PendingAddItemInfo pendingAddItemInfo = this.mPendingAddInfo;
        this.mPendingAddInfo.cellY = -1;
        pendingAddItemInfo.cellX = -1;
        PendingAddItemInfo pendingAddItemInfo2 = this.mPendingAddInfo;
        this.mPendingAddInfo.spanY = -1;
        pendingAddItemInfo2.spanX = -1;
        PendingAddItemInfo pendingAddItemInfo3 = this.mPendingAddInfo;
        this.mPendingAddInfo.minSpanY = 1;
        pendingAddItemInfo3.minSpanX = 1;
        this.mPendingAddInfo.dropPos = null;
        this.mPendingAddInfo.componentName = null;
    }

    private void restoreState(Bundle bundle) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "restoreState: savedState = " + bundle);
        }
        if (bundle == null) {
            return;
        }
        State intToState = intToState(bundle.getInt("launcher.state", State.WORKSPACE.ordinal()));
        if (intToState == State.APPS || intToState == State.WIDGETS) {
            this.mOnResumeState = intToState;
        }
        int i = bundle.getInt("launcher.current_screen", -1001);
        if (i != -1001) {
            this.mWorkspace.setRestorePage(i);
        }
        this.mCurrentWorkSpaceScreen = i;
        long j = bundle.getLong("launcher.add_container", -1L);
        long j2 = bundle.getLong("launcher.add_screen", -1L);
        if (j == -1 || j2 <= -1) {
            return;
        }
        this.mPendingAddInfo.container = j;
        this.mPendingAddInfo.screenId = j2;
        this.mPendingAddInfo.cellX = bundle.getInt("launcher.add_cell_x");
        this.mPendingAddInfo.cellY = bundle.getInt("launcher.add_cell_y");
        this.mPendingAddInfo.spanX = bundle.getInt("launcher.add_span_x");
        this.mPendingAddInfo.spanY = bundle.getInt("launcher.add_span_y");
        this.mPendingAddInfo.componentName = (ComponentName) bundle.getParcelable("launcher.add_component");
        AppWidgetProviderInfo appWidgetProviderInfo = (AppWidgetProviderInfo) bundle.getParcelable("launcher.add_widget_info");
        this.mPendingAddWidgetInfo = appWidgetProviderInfo == null ? null : LauncherAppWidgetProviderInfo.fromProviderInfo(this, appWidgetProviderInfo);
        this.mPendingAddWidgetId = bundle.getInt("launcher.add_widget_id");
        setWaitingForResult(true);
        this.mRestoring = true;
    }

    private void sendLoadingCompleteBroadcastIfNecessary() {
        if (this.mSharedPrefs.getBoolean("launcher.first_load_complete", false)) {
            return;
        }
        sendBroadcast(new Intent("com.android.launcher3.action.FIRST_LOAD_COMPLETE"), getResources().getString(2131558403));
        SharedPreferences.Editor edit = this.mSharedPrefs.edit();
        edit.putBoolean("launcher.first_load_complete", true);
        edit.apply();
    }

    private void setWaitingForResult(boolean z) {
        boolean isWorkspaceLocked = isWorkspaceLocked();
        this.mWaitingForResult = z;
        if (isWorkspaceLocked != isWorkspaceLocked()) {
            onWorkspaceLockedChanged();
        }
    }

    private void setWorkspaceBackground(int i) {
        switch (i) {
            case 1:
                getWindow().setBackgroundDrawable(new ColorDrawable(0));
                return;
            case 2:
                getWindow().setBackgroundDrawable(null);
                return;
            default:
                getWindow().setBackgroundDrawable(this.mWorkspaceBackgroundDrawable);
                return;
        }
    }

    private void setWorkspaceLoading(boolean z) {
        boolean isWorkspaceLocked = isWorkspaceLocked();
        this.mWorkspaceLoading = z;
        if (isWorkspaceLocked != isWorkspaceLocked()) {
            onWorkspaceLockedChanged();
        }
    }

    private void setupOverviewPanel() {
        this.mOverviewPanel = (ViewGroup) findViewById(2131296292);
        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener(this) { // from class: com.android.launcher3.Launcher.12
            final Launcher this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view) {
                return view.performClick();
            }
        };
        View findViewById = findViewById(2131296302);
        findViewById.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.launcher3.Launcher.13
            final Launcher this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (this.this$0.mWorkspace.isSwitchingState()) {
                    return;
                }
                this.this$0.onClickWallpaperPicker(view);
            }
        });
        findViewById.setOnLongClickListener(onLongClickListener);
        findViewById.setOnTouchListener(getHapticFeedbackTouchListener());
        this.mWidgetsButton = findViewById(2131296303);
        this.mWidgetsButton.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.launcher3.Launcher.14
            final Launcher this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (this.this$0.mWorkspace.isSwitchingState()) {
                    return;
                }
                this.this$0.onClickAddWidgetButton(view);
            }
        });
        this.mWidgetsButton.setOnLongClickListener(onLongClickListener);
        this.mWidgetsButton.setOnTouchListener(getHapticFeedbackTouchListener());
        View findViewById2 = findViewById(2131296304);
        if (hasSettings()) {
            findViewById2.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.launcher3.Launcher.15
                final Launcher this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    if (this.this$0.mWorkspace.isSwitchingState()) {
                        return;
                    }
                    this.this$0.onClickSettingsButton(view);
                }
            });
            findViewById2.setOnLongClickListener(onLongClickListener);
            findViewById2.setOnTouchListener(getHapticFeedbackTouchListener());
        } else {
            findViewById2.setVisibility(8);
        }
        this.mOverviewPanel.setAlpha(0.0f);
    }

    private void setupViews() {
        DragController dragController = this.mDragController;
        this.mLauncherView = findViewById(2131296287);
        this.mFocusHandler = (FocusIndicatorView) findViewById(2131296289);
        this.mDragLayer = (DragLayer) findViewById(2131296288);
        this.mWorkspace = (Workspace) this.mDragLayer.findViewById(2131296271);
        this.mWorkspace.setPageSwitchListener(this);
        this.mPageIndicators = this.mDragLayer.findViewById(2131296290);
        this.mLauncherView.setSystemUiVisibility(1792);
        this.mWorkspaceBackgroundDrawable = getResources().getDrawable(2130837570);
        this.mDragLayer.setup(this, dragController);
        this.mHotseat = (Hotseat) findViewById(2131296291);
        if (this.mHotseat != null) {
            this.mHotseat.setOnLongClickListener(this);
        }
        setupOverviewPanel();
        this.mWorkspace.setHapticFeedbackEnabled(false);
        this.mWorkspace.setOnLongClickListener(this);
        this.mWorkspace.setup(dragController);
        dragController.addDragListener(this.mWorkspace);
        this.mSearchDropTargetBar = (SearchDropTargetBar) this.mDragLayer.findViewById(2131296293);
        this.mAppsView = (AllAppsContainerView) findViewById(2131296273);
        this.mWidgetsView = (WidgetsContainerView) findViewById(2131296294);
        if (this.mLauncherCallbacks == null || this.mLauncherCallbacks.getAllAppsSearchBarController() == null) {
            this.mAppsView.setSearchBarController(new DefaultAppSearchController());
        } else {
            this.mAppsView.setSearchBarController(this.mLauncherCallbacks.getAllAppsSearchBarController());
        }
        dragController.setDragScoller(this.mWorkspace);
        dragController.setScrollView(this.mDragLayer);
        dragController.setMoveTarget(this.mWorkspace);
        dragController.addDropTarget(this.mWorkspace);
        if (this.mSearchDropTargetBar != null) {
            this.mSearchDropTargetBar.setup(this, dragController);
            this.mSearchDropTargetBar.setQsbSearchBar(getOrCreateQsbBar());
        }
    }

    private boolean shouldRunFirstRunActivity() {
        boolean z = false;
        if (!ActivityManager.isRunningInTestHarness()) {
            z = !this.mSharedPrefs.getBoolean("launcher.first_run_activity_displayed", false);
        }
        return z;
    }

    private boolean shouldShowIntroScreen() {
        boolean z = false;
        if (hasDismissableIntroScreen()) {
            z = !this.mSharedPrefs.getBoolean("launcher.intro_screen_dismissed", false);
        }
        return z;
    }

    private boolean showAppsOrWidgets(State state, boolean z, boolean z2) {
        if (this.mState == State.WORKSPACE || this.mState == State.APPS_SPRING_LOADED || this.mState == State.WIDGETS_SPRING_LOADED) {
            if (state == State.APPS || state == State.WIDGETS) {
                if (state == State.APPS) {
                    this.mStateTransitionAnimation.startAnimationToAllApps(this.mWorkspace.getState(), z, z2);
                } else {
                    this.mStateTransitionAnimation.startAnimationToWidgets(this.mWorkspace.getState(), z);
                }
                this.mState = state;
                this.mUserPresent = false;
                updateAutoAdvanceState();
                closeFolder();
                getWindow().getDecorView().sendAccessibilityEvent(32);
                return true;
            }
            return false;
        }
        return false;
    }

    private void showBrokenAppInstallDialog(String str, DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(this).setTitle(2131558461).setMessage(2131558462).setPositiveButton(2131558460, onClickListener).setNeutralButton(2131558459, new DialogInterface.OnClickListener(this, str) { // from class: com.android.launcher3.Launcher.21
            final Launcher this$0;
            final String val$packageName;

            {
                this.this$0 = this;
                this.val$packageName = str;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                this.this$0.mWorkspace.removeAbandonedPromise(this.val$packageName, UserHandleCompat.myUserHandle());
            }
        }).create().show();
    }

    private void shrinkAndFadeInFolderIcon(FolderIcon folderIcon, boolean z) {
        if (folderIcon == null) {
            return;
        }
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat("alpha", 1.0f);
        PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat("scaleX", 1.0f);
        PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat("scaleY", 1.0f);
        CellLayout cellLayout = (CellLayout) folderIcon.getParent().getParent();
        this.mDragLayer.removeView(this.mFolderIconImageView);
        copyFolderIconToImage(folderIcon);
        ObjectAnimator ofPropertyValuesHolder = LauncherAnimUtils.ofPropertyValuesHolder(this.mFolderIconImageView, ofFloat, ofFloat2, ofFloat3);
        ofPropertyValuesHolder.setDuration(getResources().getInteger(2131427345));
        ofPropertyValuesHolder.addListener(new AnimatorListenerAdapter(this, cellLayout, folderIcon) { // from class: com.android.launcher3.Launcher.24
            final Launcher this$0;
            final CellLayout val$cl;
            final FolderIcon val$fi;

            {
                this.this$0 = this;
                this.val$cl = cellLayout;
                this.val$fi = folderIcon;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.val$cl != null) {
                    this.val$cl.clearFolderLeaveBehind();
                    this.this$0.mDragLayer.removeView(this.this$0.mFolderIconImageView);
                    this.val$fi.setVisibility(0);
                }
            }
        });
        ofPropertyValuesHolder.start();
        if (z) {
            return;
        }
        ofPropertyValuesHolder.end();
    }

    private boolean startActivity(View view, Intent intent, Object obj) {
        boolean z;
        ActivityOptions activityOptions;
        intent.addFlags(268435456);
        if (view != null) {
            try {
                z = !intent.hasExtra("com.android.launcher3.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION");
            } catch (SecurityException e) {
                if (Utilities.ATLEAST_MARSHMALLOW && (obj instanceof ItemInfo) && intent.getComponent() == null && "android.intent.action.CALL".equals(intent.getAction()) && checkSelfPermission("android.permission.CALL_PHONE") != 0) {
                    sPendingAddItem = preparePendingAddArgs(13, intent, 0, (ItemInfo) obj);
                    requestPermissions(new String[]{"android.permission.CALL_PHONE"}, 13);
                    return false;
                }
                Toast.makeText(this, 2131558407, 0).show();
                Log.e("Launcher", "Launcher does not have the permission to launch " + intent + ". Make sure to create a MAIN intent-filter for the corresponding activity or use the exported attribute for this activity. tag=" + obj + " intent=" + intent, e);
                return false;
            }
        } else {
            z = false;
        }
        LauncherAppsCompat launcherAppsCompat = LauncherAppsCompat.getInstance(this);
        UserManagerCompat userManagerCompat = UserManagerCompat.getInstance(this);
        UserHandleCompat userHandleCompat = null;
        if (intent.hasExtra("profile")) {
            userHandleCompat = userManagerCompat.getUserForSerialNumber(intent.getLongExtra("profile", -1L));
        }
        Bundle bundle = null;
        if (z) {
            if (Utilities.ATLEAST_MARSHMALLOW) {
                int measuredWidth = view.getMeasuredWidth();
                int measuredHeight = view.getMeasuredHeight();
                int i = measuredHeight;
                int i2 = 0;
                int i3 = 0;
                int i4 = measuredWidth;
                if (view instanceof TextView) {
                    Drawable textViewIcon = Workspace.getTextViewIcon((TextView) view);
                    i = measuredHeight;
                    i2 = 0;
                    i3 = 0;
                    i4 = measuredWidth;
                    if (textViewIcon != null) {
                        Rect bounds = textViewIcon.getBounds();
                        i2 = (measuredWidth - bounds.width()) / 2;
                        i3 = view.getPaddingTop();
                        i4 = bounds.width();
                        i = bounds.height();
                    }
                }
                activityOptions = ActivityOptions.makeClipRevealAnimation(view, i2, i3, i4, i);
            } else if (Utilities.ATLEAST_LOLLIPOP) {
                activityOptions = null;
                if (Utilities.ATLEAST_LOLLIPOP_MR1) {
                    activityOptions = ActivityOptions.makeCustomAnimation(this, 2131034114, 2131034113);
                }
            } else {
                activityOptions = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            }
            bundle = activityOptions != null ? activityOptions.toBundle() : null;
        }
        if (userHandleCompat != null && !userHandleCompat.equals(UserHandleCompat.myUserHandle())) {
            launcherAppsCompat.startActivityForProfile(intent.getComponent(), userHandleCompat, intent.getSourceBounds(), bundle);
            return true;
        }
        StrictMode.VmPolicy vmPolicy = StrictMode.getVmPolicy();
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        startActivity(intent, bundle);
        StrictMode.setVmPolicy(vmPolicy);
        return true;
    }

    private void startGlobalSearch(String str, boolean z, Bundle bundle, Rect rect) {
        ComponentName globalSearchActivity = ((SearchManager) getSystemService("search")).getGlobalSearchActivity();
        if (globalSearchActivity == null) {
            Log.w("Launcher", "No global search activity found.");
            return;
        }
        Intent intent = new Intent("android.search.action.GLOBAL_SEARCH");
        intent.addFlags(268435456);
        intent.setComponent(globalSearchActivity);
        Bundle bundle2 = bundle == null ? new Bundle() : new Bundle(bundle);
        if (!bundle2.containsKey("source")) {
            bundle2.putString("source", getPackageName());
        }
        intent.putExtra("app_data", bundle2);
        if (!TextUtils.isEmpty(str)) {
            intent.putExtra("query", str);
        }
        if (z) {
            intent.putExtra("select_query", z);
        }
        intent.setSourceBounds(rect);
        try {
            if (BenesseExtension.getDchaState() == 0) {
                startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            Log.e("Launcher", "Global search activity not found: " + globalSearchActivity);
        }
    }

    private void tryAndUpdatePredictedApps() {
        List<ComponentKey> predictedApps;
        if (this.mLauncherCallbacks == null || (predictedApps = this.mLauncherCallbacks.getPredictedApps()) == null) {
            return;
        }
        this.mAppsView.setPredictedApps(predictedApps);
    }

    private void unbindFolder(FolderInfo folderInfo) {
        sFolders.remove(folderInfo.id);
    }

    private boolean waitUntilResume(Runnable runnable) {
        return waitUntilResume(runnable, false);
    }

    void addAppWidgetFromDropImpl(int i, ItemInfo itemInfo, AppWidgetHostView appWidgetHostView, LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo) {
        addAppWidgetImpl(i, itemInfo, appWidgetHostView, launcherAppWidgetProviderInfo, 0);
    }

    void addAppWidgetImpl(int i, ItemInfo itemInfo, AppWidgetHostView appWidgetHostView, LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, int i2) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "addAppWidgetImpl: appWidgetId = " + i + ", info = " + itemInfo + ", boundWidget = " + appWidgetHostView + ", appWidgetInfo = " + launcherAppWidgetProviderInfo + ", delay = " + i2);
        }
        if (launcherAppWidgetProviderInfo.configure == null) {
            Runnable runnable = new Runnable(this) { // from class: com.android.launcher3.Launcher.18
                final Launcher this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.exitSpringLoadedDragModeDelayed(true, 300, null);
                }
            };
            completeAddAppWidget(i, itemInfo.container, itemInfo.screenId, appWidgetHostView, launcherAppWidgetProviderInfo);
            this.mWorkspace.removeExtraEmptyScreenDelayed(true, runnable, i2, false);
            return;
        }
        this.mPendingAddWidgetInfo = launcherAppWidgetProviderInfo;
        this.mPendingAddWidgetId = i;
        setWaitingForResult(true);
        this.mAppWidgetManager.startConfigActivity(launcherAppWidgetProviderInfo, i, this, this.mAppWidgetHost, 5);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public FolderIcon addFolder(CellLayout cellLayout, long j, long j2, int i, int i2) {
        FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = getText(2131558405);
        LauncherModel.addItemToDatabase(this, folderInfo, j, j2, i, i2);
        sFolders.put(folderInfo.id, folderInfo);
        FolderIcon fromXml = FolderIcon.fromXml(2130968590, this, cellLayout, folderInfo, this.mIconCache);
        this.mWorkspace.addInScreen(fromXml, j, j2, i, i2, 1, 1, isWorkspaceLocked());
        this.mWorkspace.getParentCellLayoutForView(fromXml).getShortcutsAndWidgets().measureChild(fromXml);
        return fromXml;
    }

    public void addOnResumeCallback(Runnable runnable) {
        this.mOnResumeCallbacks.add(runnable);
    }

    public void addPendingItem(PendingAddItemInfo pendingAddItemInfo, long j, long j2, int[] iArr, int i, int i2) {
        switch (pendingAddItemInfo.itemType) {
            case 1:
                processShortcutFromDrop(pendingAddItemInfo.componentName, j, j2, iArr);
                return;
            case 2:
            case 3:
            default:
                throw new IllegalStateException("Unknown item type: " + pendingAddItemInfo.itemType);
            case 4:
            case 5:
                addAppWidgetFromDrop((PendingAddWidgetInfo) pendingAddItemInfo, j, j2, iArr, new int[]{i, i2});
                return;
        }
    }

    public void bindAddScreens(ArrayList<Long> arrayList) {
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            this.mWorkspace.insertNewWorkspaceScreenBeforeEmptyScreen(arrayList.get(i).longValue());
        }
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void bindAllApplications(ArrayList<AppInfo> arrayList) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "bindAllApplications: apps start");
        }
        if (waitUntilResume(this.mBindAllApplicationsRunnable, true)) {
            this.mTmpAppsList = arrayList;
            return;
        }
        if (this.mAppsView != null) {
            this.mAppsView.setApps(arrayList);
        }
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.bindAllApplications(arrayList);
        }
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void bindAppInfosRemoved(ArrayList<AppInfo> arrayList) {
        if (waitUntilResume(new Runnable(this, arrayList) { // from class: com.android.launcher3.Launcher.40
            final Launcher this$0;
            final ArrayList val$appInfos;

            {
                this.this$0 = this;
                this.val$appInfos = arrayList;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.bindAppInfosRemoved(this.val$appInfos);
            }
        }) || this.mAppsView == null) {
            return;
        }
        this.mAppsView.removeApps(arrayList);
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void bindAppWidget(LauncherAppWidgetInfo launcherAppWidgetInfo) {
        if (waitUntilResume(new Runnable(this, launcherAppWidgetInfo) { // from class: com.android.launcher3.Launcher.33
            final Launcher this$0;
            final LauncherAppWidgetInfo val$item;

            {
                this.this$0 = this;
                this.val$item = launcherAppWidgetInfo;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.bindAppWidget(this.val$item);
            }
        })) {
            return;
        }
        if (this.mIsSafeModeEnabled) {
            bindSafeModeWidget(launcherAppWidgetInfo);
            return;
        }
        LauncherAppWidgetProviderInfo findProvider = launcherAppWidgetInfo.hasRestoreFlag(2) ? null : launcherAppWidgetInfo.hasRestoreFlag(1) ? this.mAppWidgetManager.findProvider(launcherAppWidgetInfo.providerName, launcherAppWidgetInfo.user) : this.mAppWidgetManager.getLauncherAppWidgetInfo(launcherAppWidgetInfo.appWidgetId);
        if (!launcherAppWidgetInfo.hasRestoreFlag(2) && launcherAppWidgetInfo.restoreStatus != 0) {
            if (findProvider == null) {
                LauncherModel.deleteItemFromDatabase(this, launcherAppWidgetInfo);
                return;
            } else if (launcherAppWidgetInfo.hasRestoreFlag(1)) {
                PendingAddWidgetInfo pendingAddWidgetInfo = new PendingAddWidgetInfo(this, findProvider, null);
                pendingAddWidgetInfo.spanX = launcherAppWidgetInfo.spanX;
                pendingAddWidgetInfo.spanY = launcherAppWidgetInfo.spanY;
                pendingAddWidgetInfo.minSpanX = launcherAppWidgetInfo.minSpanX;
                pendingAddWidgetInfo.minSpanY = launcherAppWidgetInfo.minSpanY;
                Bundle defaultOptionsForWidget = WidgetHostViewLoader.getDefaultOptionsForWidget(this, pendingAddWidgetInfo);
                int allocateAppWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
                if (!this.mAppWidgetManager.bindAppWidgetIdIfAllowed(allocateAppWidgetId, findProvider, defaultOptionsForWidget)) {
                    this.mAppWidgetHost.deleteAppWidgetId(allocateAppWidgetId);
                    LauncherModel.deleteItemFromDatabase(this, launcherAppWidgetInfo);
                    return;
                }
                launcherAppWidgetInfo.appWidgetId = allocateAppWidgetId;
                launcherAppWidgetInfo.restoreStatus = findProvider.configure == null ? 0 : 4;
                LauncherModel.updateItemInDatabase(this, launcherAppWidgetInfo);
            } else if (launcherAppWidgetInfo.hasRestoreFlag(4) && findProvider.configure == null) {
                launcherAppWidgetInfo.restoreStatus = 0;
                LauncherModel.updateItemInDatabase(this, launcherAppWidgetInfo);
            }
        }
        if (launcherAppWidgetInfo.restoreStatus != 0) {
            PendingAppWidgetHostView pendingAppWidgetHostView = new PendingAppWidgetHostView(this, launcherAppWidgetInfo, this.mIsSafeModeEnabled);
            pendingAppWidgetHostView.updateIcon(this.mIconCache);
            launcherAppWidgetInfo.hostView = pendingAppWidgetHostView;
            launcherAppWidgetInfo.hostView.updateAppWidget(null);
            launcherAppWidgetInfo.hostView.setOnClickListener(this);
            addAppWidgetToWorkspace(launcherAppWidgetInfo, null, false);
        } else if (findProvider == null) {
            Log.e("Launcher", "Removing invalid widget: id=" + launcherAppWidgetInfo.appWidgetId);
            deleteWidgetInfo(launcherAppWidgetInfo);
            return;
        } else {
            launcherAppWidgetInfo.hostView = this.mAppWidgetHost.createView((Context) this, launcherAppWidgetInfo.appWidgetId, findProvider);
            launcherAppWidgetInfo.minSpanX = findProvider.minSpanX;
            launcherAppWidgetInfo.minSpanY = findProvider.minSpanY;
            addAppWidgetToWorkspace(launcherAppWidgetInfo, findProvider, false);
        }
        this.mWorkspace.requestLayout();
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void bindAppsAdded(ArrayList<Long> arrayList, ArrayList<ItemInfo> arrayList2, ArrayList<ItemInfo> arrayList3, ArrayList<AppInfo> arrayList4) {
        if (waitUntilResume(new Runnable(this, arrayList, arrayList2, arrayList3, arrayList4) { // from class: com.android.launcher3.Launcher.28
            final Launcher this$0;
            final ArrayList val$addAnimated;
            final ArrayList val$addNotAnimated;
            final ArrayList val$addedApps;
            final ArrayList val$newScreens;

            {
                this.this$0 = this;
                this.val$newScreens = arrayList;
                this.val$addNotAnimated = arrayList2;
                this.val$addAnimated = arrayList3;
                this.val$addedApps = arrayList4;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.bindAppsAdded(this.val$newScreens, this.val$addNotAnimated, this.val$addAnimated, this.val$addedApps);
            }
        })) {
            return;
        }
        if (arrayList != null) {
            bindAddScreens(arrayList);
        }
        if (arrayList2 != null && !arrayList2.isEmpty()) {
            bindItems(arrayList2, 0, arrayList2.size(), false);
        }
        if (arrayList3 != null && !arrayList3.isEmpty()) {
            bindItems(arrayList3, 0, arrayList3.size(), true);
        }
        this.mWorkspace.removeExtraEmptyScreen(false, false);
        if (arrayList4 == null || this.mAppsView == null) {
            return;
        }
        this.mAppsView.addApps(arrayList4);
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void bindAppsUpdated(ArrayList<AppInfo> arrayList) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "bindAppsUpdated: apps = " + arrayList);
        }
        if (waitUntilResume(new AppsUpdateTask(this, arrayList)) || this.mAppsView == null) {
            return;
        }
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "bindAppsUpdated()");
        }
        this.mAppsView.updateApps(arrayList);
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void bindFolders(LongArrayMap<FolderInfo> longArrayMap) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "bindFolders: this = " + this);
        }
        if (waitUntilResume(new Runnable(this, longArrayMap) { // from class: com.android.launcher3.Launcher.32
            final Launcher this$0;
            final LongArrayMap val$folders;

            {
                this.this$0 = this;
                this.val$folders = longArrayMap;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.bindFolders(this.val$folders);
            }
        })) {
            return;
        }
        sFolders = longArrayMap.clone();
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void bindItems(ArrayList<ItemInfo> arrayList, int i, int i2, boolean z) {
        FolderIcon fromXml;
        if (waitUntilResume(new Runnable(this, arrayList, i, i2, z) { // from class: com.android.launcher3.Launcher.29
            final Launcher this$0;
            final int val$end;
            final boolean val$forceAnimateIcons;
            final ArrayList val$shortcuts;
            final int val$start;

            {
                this.this$0 = this;
                this.val$shortcuts = arrayList;
                this.val$start = i;
                this.val$end = i2;
                this.val$forceAnimateIcons = z;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.bindItems(this.val$shortcuts, this.val$start, this.val$end, this.val$forceAnimateIcons);
            }
        })) {
            return;
        }
        AnimatorSet createAnimatorSet = LauncherAnimUtils.createAnimatorSet();
        ArrayList arrayList2 = new ArrayList();
        boolean canRunNewAppsAnimation = z ? canRunNewAppsAnimation() : false;
        Workspace workspace = this.mWorkspace;
        long j = -1;
        for (int i3 = i; i3 < i2; i3++) {
            ItemInfo itemInfo = arrayList.get(i3);
            if (LauncherLog.DEBUG) {
                LauncherLog.d("Launcher", "bindItems: start = " + i + ", end = " + i2 + "item = " + itemInfo + ", this = " + this);
            }
            if (itemInfo.container != -101 || this.mHotseat != null) {
                switch (itemInfo.itemType) {
                    case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    case 1:
                        View createShortcut = createShortcut((ShortcutInfo) itemInfo);
                        fromXml = createShortcut;
                        if (itemInfo.container == -100) {
                            CellLayout screenWithId = this.mWorkspace.getScreenWithId(itemInfo.screenId);
                            fromXml = createShortcut;
                            if (screenWithId != null) {
                                fromXml = createShortcut;
                                if (screenWithId.isOccupied(itemInfo.cellX, itemInfo.cellY)) {
                                    String str = "Collision while binding workspace item: " + itemInfo + ". Collides with " + screenWithId.getChildAt(itemInfo.cellX, itemInfo.cellY).getTag();
                                    if (LauncherAppState.isDogfoodBuild()) {
                                        throw new RuntimeException(str);
                                    }
                                    Log.d("Launcher", str);
                                    fromXml = createShortcut;
                                    break;
                                }
                            }
                        }
                        break;
                    case 2:
                        fromXml = FolderIcon.fromXml(2130968590, this, (ViewGroup) workspace.getChildAt(workspace.getCurrentPage()), (FolderInfo) itemInfo, this.mIconCache);
                        break;
                    default:
                        throw new RuntimeException("Invalid Item Type");
                }
                workspace.addInScreenFromBind(fromXml, itemInfo.container, itemInfo.screenId, itemInfo.cellX, itemInfo.cellY, 1, 1);
                if (canRunNewAppsAnimation) {
                    fromXml.setAlpha(0.0f);
                    fromXml.setScaleX(0.0f);
                    fromXml.setScaleY(0.0f);
                    arrayList2.add(createNewAppBounceAnimation(fromXml, i3));
                    j = itemInfo.screenId;
                }
            }
        }
        if (canRunNewAppsAnimation && j > -1) {
            long screenIdForPageIndex = this.mWorkspace.getScreenIdForPageIndex(this.mWorkspace.getNextPage());
            int pageIndexForScreenId = this.mWorkspace.getPageIndexForScreenId(j);
            Runnable runnable = new Runnable(this, createAnimatorSet, arrayList2) { // from class: com.android.launcher3.Launcher.30
                final Launcher this$0;
                final AnimatorSet val$anim;
                final Collection val$bounceAnims;

                {
                    this.this$0 = this;
                    this.val$anim = createAnimatorSet;
                    this.val$bounceAnims = arrayList2;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.val$anim.playTogether(this.val$bounceAnims);
                    this.val$anim.start();
                }
            };
            if (j != screenIdForPageIndex) {
                this.mWorkspace.postDelayed(new Runnable(this, pageIndexForScreenId, runnable) { // from class: com.android.launcher3.Launcher.31
                    final Launcher this$0;
                    final int val$newScreenIndex;
                    final Runnable val$startBounceAnimRunnable;

                    {
                        this.this$0 = this;
                        this.val$newScreenIndex = pageIndexForScreenId;
                        this.val$startBounceAnimRunnable = runnable;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        if (this.this$0.mWorkspace != null) {
                            this.this$0.mWorkspace.snapToPage(this.val$newScreenIndex);
                            this.this$0.mWorkspace.postDelayed(this.val$startBounceAnimRunnable, Launcher.NEW_APPS_ANIMATION_DELAY);
                        }
                    }
                }, NEW_APPS_PAGE_MOVE_DELAY);
            } else {
                this.mWorkspace.postDelayed(runnable, NEW_APPS_ANIMATION_DELAY);
            }
        }
        workspace.requestLayout();
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void bindRestoreItemsChange(HashSet<ItemInfo> hashSet) {
        if (waitUntilResume(new Runnable(this, hashSet) { // from class: com.android.launcher3.Launcher.38
            final Launcher this$0;
            final HashSet val$updates;

            {
                this.this$0 = this;
                this.val$updates = hashSet;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.bindRestoreItemsChange(this.val$updates);
            }
        })) {
            return;
        }
        this.mWorkspace.updateRestoreItems(hashSet);
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void bindScreens(ArrayList<Long> arrayList) {
        bindAddScreens(arrayList);
        if (arrayList.size() == 0) {
            this.mWorkspace.addExtraEmptyScreen();
        }
        if (hasCustomContentToLeft()) {
            this.mWorkspace.createCustomContentContainer();
            populateCustomContentContainer();
        }
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void bindSearchProviderChanged() {
        if (this.mSearchDropTargetBar == null) {
            return;
        }
        if (this.mQsb != null) {
            this.mSearchDropTargetBar.removeView(this.mQsb);
            this.mQsb = null;
        }
        this.mSearchDropTargetBar.setQsbSearchBar(getOrCreateQsbBar());
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void bindShortcutsChanged(ArrayList<ShortcutInfo> arrayList, ArrayList<ShortcutInfo> arrayList2, UserHandleCompat userHandleCompat) {
        if (waitUntilResume(new Runnable(this, arrayList, arrayList2, userHandleCompat) { // from class: com.android.launcher3.Launcher.37
            final Launcher this$0;
            final ArrayList val$removed;
            final ArrayList val$updated;
            final UserHandleCompat val$user;

            {
                this.this$0 = this;
                this.val$updated = arrayList;
                this.val$removed = arrayList2;
                this.val$user = userHandleCompat;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.bindShortcutsChanged(this.val$updated, this.val$removed, this.val$user);
            }
        })) {
            return;
        }
        if (!arrayList.isEmpty()) {
            this.mWorkspace.updateShortcuts(arrayList);
        }
        if (arrayList2.isEmpty()) {
            return;
        }
        HashSet<ComponentName> hashSet = new HashSet<>();
        for (ShortcutInfo shortcutInfo : arrayList2) {
            hashSet.add(shortcutInfo.getTargetComponent());
        }
        this.mWorkspace.removeItemsByComponentName(hashSet, userHandleCompat);
        this.mDragController.onAppsRemoved(new HashSet<>(), hashSet);
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void bindWidgetsModel(WidgetsModel widgetsModel) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "bindAllPackages()");
        }
        if (waitUntilResume(this.mBindWidgetModelRunnable, true)) {
            this.mWidgetsModel = widgetsModel;
        } else if (this.mWidgetsView == null || widgetsModel == null) {
        } else {
            this.mWidgetsView.addWidgets(widgetsModel);
            this.mWidgetsModel = null;
        }
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void bindWidgetsRestored(ArrayList<LauncherAppWidgetInfo> arrayList) {
        if (waitUntilResume(new Runnable(this, arrayList) { // from class: com.android.launcher3.Launcher.36
            final Launcher this$0;
            final ArrayList val$widgets;

            {
                this.this$0 = this;
                this.val$widgets = arrayList;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.bindWidgetsRestored(this.val$widgets);
            }
        })) {
            return;
        }
        this.mWorkspace.widgetsRestored(arrayList);
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void bindWorkspaceComponentsRemoved(HashSet<String> hashSet, HashSet<ComponentName> hashSet2, UserHandleCompat userHandleCompat) {
        if (waitUntilResume(new Runnable(this, hashSet, hashSet2, userHandleCompat) { // from class: com.android.launcher3.Launcher.39
            final Launcher this$0;
            final HashSet val$components;
            final HashSet val$packageNames;
            final UserHandleCompat val$user;

            {
                this.this$0 = this;
                this.val$packageNames = hashSet;
                this.val$components = hashSet2;
                this.val$user = userHandleCompat;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.bindWorkspaceComponentsRemoved(this.val$packageNames, this.val$components, this.val$user);
            }
        })) {
            return;
        }
        if (!hashSet.isEmpty()) {
            this.mWorkspace.removeItemsByPackageName(hashSet, userHandleCompat);
        }
        if (!hashSet2.isEmpty()) {
            this.mWorkspace.removeItemsByComponentName(hashSet2, userHandleCompat);
        }
        this.mDragController.onAppsRemoved(hashSet, hashSet2);
    }

    protected void changeWallpaperVisiblity(boolean z) {
        int i = z ? 1048576 : 0;
        if (i != (getWindow().getAttributes().flags & 1048576)) {
            getWindow().setFlags(i, 1048576);
        }
        setWorkspaceBackground(z ? 0 : 2);
    }

    public void closeFolder() {
        closeFolder(true);
    }

    public void closeFolder(Folder folder, boolean z) {
        folder.getInfo().opened = false;
        if (((ViewGroup) folder.getParent().getParent()) != null) {
            FolderIcon folderIcon = (FolderIcon) this.mWorkspace.getViewForTag(folder.mInfo);
            LauncherLog.d("Launcher", "closeFolder: fi = " + folderIcon);
            shrinkAndFadeInFolderIcon(folderIcon, z);
            if (folderIcon != null) {
                ((CellLayout.LayoutParams) folderIcon.getLayoutParams()).canReorder = true;
            }
        }
        if (z) {
            folder.animateClosed();
        } else {
            folder.close(false);
        }
        getDragLayer().sendAccessibilityEvent(32);
    }

    public void closeFolder(boolean z) {
        Folder folder = null;
        if (this.mWorkspace != null) {
            folder = this.mWorkspace.getOpenFolder();
        }
        if (folder != null) {
            if (folder.isEditingName()) {
                folder.dismissEditingName();
            }
            closeFolder(folder, z);
        }
    }

    public void closeSystemDialogs() {
        getWindow().closeAllPanels();
        setWaitingForResult(false);
    }

    void completeAddAppWidget(int i, long j, long j2, AppWidgetHostView appWidgetHostView, LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo) {
        PendingAddItemInfo pendingAddItemInfo = this.mPendingAddInfo;
        LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo2 = launcherAppWidgetProviderInfo;
        if (launcherAppWidgetProviderInfo == null) {
            launcherAppWidgetProviderInfo2 = this.mAppWidgetManager.getLauncherAppWidgetInfo(i);
        }
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "completeAddAppWidget: appWidgetId = " + i + ", container = " + j + ", screenId = " + j2);
        }
        if (launcherAppWidgetProviderInfo2.isCustomWidget) {
            i = -100;
        }
        LauncherAppWidgetInfo launcherAppWidgetInfo = new LauncherAppWidgetInfo(i, launcherAppWidgetProviderInfo2.provider);
        launcherAppWidgetInfo.spanX = pendingAddItemInfo.spanX;
        launcherAppWidgetInfo.spanY = pendingAddItemInfo.spanY;
        launcherAppWidgetInfo.minSpanX = pendingAddItemInfo.minSpanX;
        launcherAppWidgetInfo.minSpanY = pendingAddItemInfo.minSpanY;
        launcherAppWidgetInfo.user = this.mAppWidgetManager.getUser(launcherAppWidgetProviderInfo2);
        LauncherModel.addItemToDatabase(this, launcherAppWidgetInfo, j, j2, pendingAddItemInfo.cellX, pendingAddItemInfo.cellY);
        if (!this.mRestoring) {
            if (appWidgetHostView == null) {
                launcherAppWidgetInfo.hostView = this.mAppWidgetHost.createView((Context) this, i, launcherAppWidgetProviderInfo2);
            } else {
                launcherAppWidgetInfo.hostView = appWidgetHostView;
            }
            launcherAppWidgetInfo.hostView.setVisibility(0);
            addAppWidgetToWorkspace(launcherAppWidgetInfo, launcherAppWidgetProviderInfo2, isWorkspaceLocked());
        }
        resetAddInfo();
    }

    void completeTwoStageWidgetDrop(int i, int i2) {
        Runnable runnable;
        AppWidgetHostView appWidgetHostView;
        if (this.mWorkspace == null) {
            LauncherLog.d("Launcher", "completeTwoStageWidgetDrop: mWorkspace = " + this.mWorkspace + ",mPendingAddInfo:" + this.mPendingAddInfo);
            return;
        }
        CellLayout screenWithId = this.mWorkspace.getScreenWithId(this.mPendingAddInfo.screenId);
        int i3 = 0;
        if (i == -1) {
            i3 = 3;
            AppWidgetHostView createView = this.mAppWidgetHost.createView((Context) this, i2, this.mPendingAddWidgetInfo);
            appWidgetHostView = createView;
            runnable = new Runnable(this, i2, createView, i) { // from class: com.android.launcher3.Launcher.11
                final Launcher this$0;
                final int val$appWidgetId;
                final AppWidgetHostView val$layout;
                final int val$resultCode;

                {
                    this.this$0 = this;
                    this.val$appWidgetId = i2;
                    this.val$layout = createView;
                    this.val$resultCode = i;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.completeAddAppWidget(this.val$appWidgetId, this.this$0.mPendingAddInfo.container, this.this$0.mPendingAddInfo.screenId, this.val$layout, null);
                    this.this$0.exitSpringLoadedDragModeDelayed(this.val$resultCode != 0, 300, null);
                }
            };
        } else {
            runnable = null;
            appWidgetHostView = null;
            if (i == 0) {
                this.mAppWidgetHost.deleteAppWidgetId(i2);
                i3 = 4;
                runnable = null;
                appWidgetHostView = null;
            }
        }
        if (this.mDragLayer.getAnimatedView() != null) {
            this.mWorkspace.animateWidgetDrop(this.mPendingAddInfo, screenWithId, (DragView) this.mDragLayer.getAnimatedView(), runnable, i3, appWidgetHostView, true);
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public FastBitmapDrawable createIconDrawable(Bitmap bitmap) {
        FastBitmapDrawable fastBitmapDrawable = new FastBitmapDrawable(bitmap);
        fastBitmapDrawable.setFilterBitmap(true);
        resizeIconDrawable(fastBitmapDrawable);
        return fastBitmapDrawable;
    }

    public View createShortcut(ViewGroup viewGroup, ShortcutInfo shortcutInfo) {
        BubbleTextView bubbleTextView = (BubbleTextView) this.mInflater.inflate(2130968586, viewGroup, false);
        bubbleTextView.applyFromShortcutInfo(shortcutInfo, this.mIconCache);
        bubbleTextView.setCompoundDrawablePadding(this.mDeviceProfile.iconDrawablePaddingPx);
        bubbleTextView.setOnClickListener(this);
        bubbleTextView.setOnFocusChangeListener(this.mFocusHandler);
        return bubbleTextView;
    }

    View createShortcut(ShortcutInfo shortcutInfo) {
        return createShortcut((ViewGroup) this.mWorkspace.getChildAt(this.mWorkspace.getCurrentPage()), shortcutInfo);
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (LauncherLog.DEBUG_KEY) {
            LauncherLog.d("Launcher", "dispatchKeyEvent: keyEvent = " + keyEvent);
        }
        if (keyEvent.getAction() == 0) {
            switch (keyEvent.getKeyCode()) {
                case 3:
                    return true;
                case 25:
                    if (Utilities.isPropertyEnabled("launcher_dump_state")) {
                        dumpState();
                        return true;
                    }
                    break;
            }
        } else if (keyEvent.getAction() == 1) {
            switch (keyEvent.getKeyCode()) {
                case 3:
                    return true;
            }
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        boolean dispatchPopulateAccessibilityEvent = super.dispatchPopulateAccessibilityEvent(accessibilityEvent);
        List<CharSequence> text = accessibilityEvent.getText();
        text.clear();
        if (this.mState == State.APPS) {
            text.add(getString(2131558420));
        } else if (this.mState == State.WIDGETS) {
            text.add(getString(2131558453));
        } else if (this.mWorkspace != null) {
            text.add(this.mWorkspace.getCurrentPageDescription());
        } else {
            text.add(getString(2131558421));
        }
        return dispatchPopulateAccessibilityEvent;
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            LauncherHelper.beginSection("Launcher.dispatchTouchEvent:ACTION_DOWN");
        } else if (motionEvent.getAction() == 1) {
            LauncherHelper.beginSection("Launcher.dispatchTouchEvent:ACTION_UP");
        }
        LauncherHelper.endSection();
        return super.dispatchTouchEvent(motionEvent);
    }

    @Override // android.app.Activity
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dump(str, fileDescriptor, printWriter, strArr);
        synchronized (sDumpLogs) {
            printWriter.println(" ");
            printWriter.println("Debug logs: ");
            for (int i = 0; i < sDumpLogs.size(); i++) {
                printWriter.println("  " + sDumpLogs.get(i));
            }
        }
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.dump(str, fileDescriptor, printWriter, strArr);
        }
    }

    public void dumpState() {
        Log.d("Launcher", "BEGIN launcher3 dump state for launcher " + this);
        Log.d("Launcher", "mSavedState=" + this.mSavedState);
        Log.d("Launcher", "mWorkspaceLoading=" + this.mWorkspaceLoading);
        Log.d("Launcher", "mRestoring=" + this.mRestoring);
        Log.d("Launcher", "mWaitingForResult=" + this.mWaitingForResult);
        Log.d("Launcher", "mSavedInstanceState=" + this.mSavedInstanceState);
        Log.d("Launcher", "sFolders.size=" + sFolders.size());
        this.mModel.dumpState();
        Log.d("Launcher", "END launcher3 dump state");
    }

    public void enterSpringLoadedDragMode() {
        if (this.mState == State.WORKSPACE || this.mState == State.APPS_SPRING_LOADED || this.mState == State.WIDGETS_SPRING_LOADED) {
            return;
        }
        this.mStateTransitionAnimation.startAnimationToWorkspace(this.mState, this.mWorkspace.getState(), Workspace.State.SPRING_LOADED, -1, true, null);
        this.mState = isAppsViewVisible() ? State.APPS_SPRING_LOADED : State.WIDGETS_SPRING_LOADED;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void exitSpringLoadedDragMode() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "exitSpringLoadedDragMode mState = " + this.mState);
        }
        if (this.mState == State.APPS_SPRING_LOADED) {
            showAppsView(true, false, false, false);
        } else if (this.mState == State.WIDGETS_SPRING_LOADED) {
            showWidgetsView(true, false);
        }
    }

    public void exitSpringLoadedDragModeDelayed(boolean z, int i, Runnable runnable) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "exitSpringLoadedDragModeDelayed successfulDrop = " + z + ", delay = " + i + ", mState = " + this.mState);
        }
        if (this.mState == State.APPS_SPRING_LOADED || this.mState == State.WIDGETS_SPRING_LOADED) {
            this.mHandler.postDelayed(new Runnable(this, z, runnable) { // from class: com.android.launcher3.Launcher.27
                final Launcher this$0;
                final Runnable val$onCompleteRunnable;
                final boolean val$successfulDrop;

                {
                    this.this$0 = this;
                    this.val$successfulDrop = z;
                    this.val$onCompleteRunnable = runnable;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (!this.val$successfulDrop) {
                        this.this$0.exitSpringLoadedDragMode();
                        return;
                    }
                    this.this$0.mWidgetsView.setVisibility(8);
                    this.this$0.showWorkspace(true, this.val$onCompleteRunnable);
                }
            }, i);
        }
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void finishBindingItems() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "finishBindingItems: mSavedState = " + this.mSavedState + ", mSavedInstanceState = " + this.mSavedInstanceState + ", this = " + this);
        }
        if (waitUntilResume(new Runnable(this) { // from class: com.android.launcher3.Launcher.34
            final Launcher this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.finishBindingItems();
            }
        })) {
            return;
        }
        if (this.mSavedState != null) {
            if (!this.mWorkspace.hasFocus()) {
                this.mWorkspace.getChildAt(this.mWorkspace.getCurrentPage()).requestFocus();
            }
            this.mSavedState = null;
        }
        this.mWorkspace.restoreInstanceStateForRemainingPages();
        setWorkspaceLoading(false);
        sendLoadingCompleteBroadcastIfNecessary();
        if (sPendingAddItem != null) {
            this.mWorkspace.post(new Runnable(this, completeAdd(sPendingAddItem)) { // from class: com.android.launcher3.Launcher.35
                final Launcher this$0;
                final long val$screenId;

                {
                    this.this$0 = this;
                    this.val$screenId = r6;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mWorkspace.snapToScreenId(this.val$screenId);
                }
            });
            sPendingAddItem = null;
        }
        InstallShortcutReceiver.disableAndFlushInstallQueue(this);
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.finishBindingItems(false);
        }
        this.mWorkspace.removeExtraEmptyScreenDelayed(true, null, 10, false);
    }

    public View getAllAppsButton() {
        return this.mAllAppsButton;
    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return this.mAppWidgetHost;
    }

    public AllAppsContainerView getAppsView() {
        return this.mAppsView;
    }

    public CellLayout getCellLayout(long j, long j2) {
        if (j == -101) {
            if (this.mHotseat != null) {
                return this.mHotseat.getLayout();
            }
            return null;
        } else if (this.mWorkspace != null) {
            return this.mWorkspace.getScreenWithId(j2);
        } else {
            return null;
        }
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public int getCurrentWorkspaceScreen() {
        if (this.mWorkspace != null) {
            return this.mWorkspace.getCurrentPage();
        }
        return 2;
    }

    public DeviceProfile getDeviceProfile() {
        return this.mDeviceProfile;
    }

    public DragController getDragController() {
        return this.mDragController;
    }

    public DragLayer getDragLayer() {
        return this.mDragLayer;
    }

    protected Intent getFirstRunActivity() {
        if (this.mLauncherCallbacks != null) {
            return this.mLauncherCallbacks.getFirstRunActivity();
        }
        return null;
    }

    public View.OnTouchListener getHapticFeedbackTouchListener() {
        if (this.mHapticFeedbackTouchListener == null) {
            this.mHapticFeedbackTouchListener = new View.OnTouchListener(this) { // from class: com.android.launcher3.Launcher.23
                final Launcher this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.view.View.OnTouchListener
                @SuppressLint({"ClickableViewAccessibility"})
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if ((motionEvent.getAction() & 255) == 0) {
                        view.performHapticFeedback(1);
                        return false;
                    }
                    return false;
                }
            };
        }
        return this.mHapticFeedbackTouchListener;
    }

    public Hotseat getHotseat() {
        return this.mHotseat;
    }

    protected View getIntroScreen() {
        if (this.mLauncherCallbacks != null) {
            return this.mLauncherCallbacks.getIntroScreen();
        }
        return null;
    }

    public LauncherModel getModel() {
        return this.mModel;
    }

    public View getOrCreateQsbBar() {
        if (launcherCallbacksProvidesSearch()) {
            return this.mLauncherCallbacks.getQsbBar();
        }
        if (this.mQsb == null) {
            AppWidgetProviderInfo searchWidgetProvider = Utilities.getSearchWidgetProvider(this);
            if (searchWidgetProvider == null) {
                return null;
            }
            Bundle bundle = new Bundle();
            bundle.putInt("appWidgetCategory", 4);
            LauncherAppState launcherAppState = LauncherAppState.getInstance();
            DeviceProfile deviceProfile = launcherAppState.getInvariantDeviceProfile().portraitProfile;
            DeviceProfile deviceProfile2 = launcherAppState.getInvariantDeviceProfile().landscapeProfile;
            float f = getResources().getDisplayMetrics().density;
            Point searchBarDimensForWidgetOpts = deviceProfile.getSearchBarDimensForWidgetOpts(getResources());
            int i = (int) (searchBarDimensForWidgetOpts.y / f);
            int i2 = (int) (searchBarDimensForWidgetOpts.x / f);
            int i3 = i;
            int i4 = i2;
            int i5 = i;
            int i6 = i2;
            if (!deviceProfile2.isVerticalBarLayout()) {
                Point searchBarDimensForWidgetOpts2 = deviceProfile2.getSearchBarDimensForWidgetOpts(getResources());
                i3 = (int) Math.max(i, searchBarDimensForWidgetOpts2.y / f);
                i5 = (int) Math.min(i, searchBarDimensForWidgetOpts2.y / f);
                i4 = (int) Math.max(i2, searchBarDimensForWidgetOpts2.x / f);
                i6 = (int) Math.min(i2, searchBarDimensForWidgetOpts2.x / f);
            }
            bundle.putInt("appWidgetMaxHeight", i3);
            bundle.putInt("appWidgetMinHeight", i5);
            bundle.putInt("appWidgetMaxWidth", i4);
            bundle.putInt("appWidgetMinWidth", i6);
            if (this.mLauncherCallbacks != null) {
                bundle.putAll(this.mLauncherCallbacks.getAdditionalSearchWidgetOptions());
            }
            int i7 = this.mSharedPrefs.getInt("qsb_widget_id", -1);
            AppWidgetProviderInfo appWidgetInfo = this.mAppWidgetManager.getAppWidgetInfo(i7);
            if (!searchWidgetProvider.provider.flattenToString().equals(this.mSharedPrefs.getString("qsb_widget_provider", null)) || appWidgetInfo == null || !appWidgetInfo.provider.equals(searchWidgetProvider.provider)) {
                if (i7 > -1) {
                    this.mAppWidgetHost.deleteAppWidgetId(i7);
                }
                int allocateAppWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
                i7 = allocateAppWidgetId;
                if (!AppWidgetManagerCompat.getInstance(this).bindAppWidgetIdIfAllowed(allocateAppWidgetId, searchWidgetProvider, bundle)) {
                    this.mAppWidgetHost.deleteAppWidgetId(allocateAppWidgetId);
                    i7 = -1;
                }
                this.mSharedPrefs.edit().putInt("qsb_widget_id", i7).putString("qsb_widget_provider", searchWidgetProvider.provider.flattenToString()).apply();
            }
            this.mAppWidgetHost.setQsbWidgetId(i7);
            if (getResources().getBoolean(2131492873) && i7 != -1) {
                this.mQsb = this.mAppWidgetHost.createView(this, i7, searchWidgetProvider);
                this.mQsb.setId(2131296258);
                this.mQsb.updateAppWidgetOptions(bundle);
                this.mQsb.setPadding(0, 0, 0, 0);
                this.mSearchDropTargetBar.addView(this.mQsb);
                this.mSearchDropTargetBar.setQsbSearchBar(this.mQsb);
            }
        }
        return this.mQsb;
    }

    public ViewGroup getOverviewPanel() {
        return this.mOverviewPanel;
    }

    public int getSearchBarHeight() {
        if (this.mLauncherCallbacks != null) {
            return this.mLauncherCallbacks.getSearchBarHeight();
        }
        return 0;
    }

    public SearchDropTargetBar getSearchDropTargetBar() {
        return this.mSearchDropTargetBar;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public SharedPreferences getSharedPrefs() {
        return this.mSharedPrefs;
    }

    public int getViewIdForItem(ItemInfo itemInfo) {
        return (int) itemInfo.id;
    }

    public View getWidgetsButton() {
        return this.mWidgetsButton;
    }

    public WidgetsContainerView getWidgetsView() {
        return this.mWidgetsView;
    }

    public Workspace getWorkspace() {
        return this.mWorkspace;
    }

    protected boolean hasCustomContentToLeft() {
        if (this.mLauncherCallbacks != null) {
            return this.mLauncherCallbacks.hasCustomContentToLeft();
        }
        return false;
    }

    protected boolean hasDismissableIntroScreen() {
        if (this.mLauncherCallbacks != null) {
            return this.mLauncherCallbacks.hasDismissableIntroScreen();
        }
        return false;
    }

    protected boolean hasFirstRunActivity() {
        if (this.mLauncherCallbacks != null) {
            return this.mLauncherCallbacks.hasFirstRunActivity();
        }
        return false;
    }

    protected boolean hasSettings() {
        if (this.mLauncherCallbacks != null) {
            return this.mLauncherCallbacks.hasSettings();
        }
        return !getResources().getBoolean(2131492867);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void hideWorkspaceSearchAndHotseat() {
        if (this.mWorkspace != null) {
            this.mWorkspace.setAlpha(0.0f);
        }
        if (this.mHotseat != null) {
            this.mHotseat.setAlpha(0.0f);
        }
        if (this.mPageIndicators != null) {
            this.mPageIndicators.setAlpha(0.0f);
        }
        if (this.mSearchDropTargetBar != null) {
            this.mSearchDropTargetBar.animateToState(SearchDropTargetBar.State.INVISIBLE, 0);
        }
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public boolean isAllAppsButtonRank(int i) {
        if (this.mHotseat != null) {
            return this.mHotseat.isAllAppsButtonRank(i);
        }
        return false;
    }

    public boolean isAppsViewVisible() {
        boolean z = true;
        if (this.mState != State.APPS) {
            z = this.mOnResumeState == State.APPS;
        }
        return z;
    }

    public boolean isDraggingEnabled() {
        return !isWorkspaceLoading();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isHotseatLayout(View view) {
        boolean z = false;
        if (this.mHotseat != null) {
            z = false;
            if (view != null) {
                z = false;
                if (view instanceof CellLayout) {
                    z = false;
                    if (view == this.mHotseat.getLayout()) {
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isLauncherPreinstalled() {
        if (this.mLauncherCallbacks != null) {
            return this.mLauncherCallbacks.isLauncherPreinstalled();
        }
        try {
            return (getPackageManager().getApplicationInfo(getComponentName().getPackageName(), 0).flags & 1) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isOnCustomContent() {
        return this.mWorkspace.isOnOrMovingToCustomContent();
    }

    public boolean isWidgetsViewVisible() {
        boolean z = true;
        if (this.mState != State.WIDGETS) {
            z = this.mOnResumeState == State.WIDGETS;
        }
        return z;
    }

    public boolean isWorkspaceLoading() {
        return this.mWorkspaceLoading;
    }

    public boolean isWorkspaceLocked() {
        return !this.mWorkspaceLoading ? this.mWaitingForResult : true;
    }

    public boolean launcherCallbacksProvidesSearch() {
        return this.mLauncherCallbacks != null ? this.mLauncherCallbacks.providesSearch() : false;
    }

    void lockAllApps() {
    }

    @TargetApi(18)
    public void lockScreenOrientation() {
        if (this.mRotationEnabled) {
            if (Utilities.ATLEAST_JB_MR2) {
                setRequestedOrientation(14);
            } else {
                setRequestedOrientation(mapConfigurationOriActivityInfoOri(getResources().getConfiguration().orientation));
            }
        }
    }

    protected void moveWorkspaceToDefaultScreen() {
        this.mWorkspace.moveToDefaultScreen(false);
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void notifyWidgetProvidersChanged() {
        if (this.mWorkspace == null || !this.mWorkspace.getState().shouldUpdateWidget) {
            return;
        }
        this.mModel.refreshAndBindWidgetsAndShortcuts(this, this.mWidgetsView.isEmpty());
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        handleActivityResult(i, i2, intent);
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onActivityResult(i, i2, intent);
        }
    }

    @Override // com.android.launcher3.LauncherProviderChangeListener
    public void onAppWidgetHostReset() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "onAppWidgetReset.");
        }
        if (this.mAppWidgetHost != null) {
            this.mAppWidgetHost.startListening();
        }
        bindSearchProviderChanged();
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "onAttachedToWindow.");
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        registerReceiver(this.mReceiver, intentFilter);
        FirstFrameAnimatorHelper.initializeDrawListener(getWindow().getDecorView());
        this.mAttached = true;
        this.mVisible = true;
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onAttachedToWindow();
        }
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "Back key pressed, mState = " + this.mState + ", mOnResumeState = " + this.mOnResumeState);
        }
        if (this.mLauncherCallbacks == null || !this.mLauncherCallbacks.handleBackPressed()) {
            if (this.mDragController.isDragging()) {
                this.mDragController.cancelDrag();
            } else if (isAppsViewVisible()) {
                showWorkspace(true);
            } else if (isWidgetsViewVisible()) {
                showOverviewMode(true);
            } else if (this.mWorkspace.isInOverviewMode()) {
                showWorkspace(true);
            } else if (this.mWorkspace.getOpenFolder() == null) {
                this.mWorkspace.exitWidgetResizeMode();
                this.mWorkspace.showOutlinesTemporarily();
            } else {
                Folder openFolder = this.mWorkspace.getOpenFolder();
                if (openFolder.isEditingName()) {
                    openFolder.dismissEditingName();
                } else {
                    closeFolder();
                }
            }
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        LauncherHelper.beginSection("Launcher.onClick");
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "Click on view " + view);
        }
        if (view.getWindowToken() == null) {
            LauncherLog.d("Launcher", "Click on a view with no window token, directly return.");
        } else if (!this.mWorkspace.isFinishedSwitchingState()) {
            LauncherLog.d("Launcher", "The workspace is in switching state when clicking on view, directly return.");
        } else if (view instanceof Workspace) {
            if (this.mWorkspace.isInOverviewMode()) {
                showWorkspace(true);
            }
        } else {
            if ((view instanceof CellLayout) && this.mWorkspace.isInOverviewMode()) {
                showWorkspace(this.mWorkspace.indexOfChild(view), true);
            }
            Object tag = view.getTag();
            if (tag instanceof ShortcutInfo) {
                onClickAppShortcut(view);
            } else if (tag instanceof FolderInfo) {
                if (view instanceof FolderIcon) {
                    onClickFolderIcon(view);
                }
            } else if (view == this.mAllAppsButton) {
                onClickAllAppsButton(view);
            } else if (tag instanceof AppInfo) {
                startAppShortcutOrInfoActivity(view);
            } else if ((tag instanceof LauncherAppWidgetInfo) && (view instanceof PendingAppWidgetHostView)) {
                onClickPendingWidget((PendingAppWidgetHostView) view);
            }
            LauncherHelper.endSection();
        }
    }

    protected void onClickAddWidgetButton(View view) {
        if (this.mIsSafeModeEnabled) {
            Toast.makeText(this, 2131558410, 0).show();
            return;
        }
        showWidgetsView(true, true);
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onClickAddWidgetButton(view);
        }
    }

    protected void onClickAllAppsButton(View view) {
        if (isAppsViewVisible()) {
            return;
        }
        showAppsView(true, false, true, false);
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "[All apps launch time][Start] onClickAllAppsButton.");
        }
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onClickAllAppsButton(view);
        }
    }

    protected void onClickAppShortcut(View view) {
        Object tag = view.getTag();
        if (!(tag instanceof ShortcutInfo)) {
            throw new IllegalArgumentException("Input must be a Shortcut");
        }
        ShortcutInfo shortcutInfo = (ShortcutInfo) tag;
        if (shortcutInfo.isDisabled != 0 && (shortcutInfo.isDisabled & 4) == 0 && (shortcutInfo.isDisabled & 8) == 0) {
            int i = 2131558408;
            if ((shortcutInfo.isDisabled & 1) != 0) {
                i = 2131558409;
            }
            Toast.makeText(this, i, 0).show();
        } else if ((view instanceof BubbleTextView) && shortcutInfo.isPromise() && !shortcutInfo.hasStatusFlag(4)) {
            showBrokenAppInstallDialog(shortcutInfo.getTargetComponent().getPackageName(), new DialogInterface.OnClickListener(this, view) { // from class: com.android.launcher3.Launcher.22
                final Launcher this$0;
                final View val$v;

                {
                    this.this$0 = this;
                    this.val$v = view;
                }

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i2) {
                    this.this$0.startAppShortcutOrInfoActivity(this.val$v);
                }
            });
        } else {
            startAppShortcutOrInfoActivity(view);
            if (this.mLauncherCallbacks != null) {
                this.mLauncherCallbacks.onClickAppShortcut(view);
            }
        }
    }

    protected void onClickFolderIcon(View view) {
        if (!(view instanceof FolderIcon)) {
            throw new IllegalArgumentException("Input must be a FolderIcon");
        }
        FolderIcon folderIcon = (FolderIcon) view;
        FolderInfo folderInfo = folderIcon.getFolderInfo();
        Folder folderForTag = this.mWorkspace.getFolderForTag(folderInfo);
        if (folderInfo.opened && folderForTag == null) {
            Log.d("Launcher", "Folder info marked as open, but associated folder is not open. Screen: " + folderInfo.screenId + " (" + folderInfo.cellX + ", " + folderInfo.cellY + ")");
            folderInfo.opened = false;
        }
        if (!folderInfo.opened && !folderIcon.getFolder().isDestroyed()) {
            closeFolder();
            openFolder(folderIcon);
        } else if (folderForTag != null) {
            int pageForView = this.mWorkspace.getPageForView(folderForTag);
            closeFolder(folderForTag, true);
            if (pageForView != this.mWorkspace.getCurrentPage()) {
                closeFolder();
                openFolder(folderIcon);
            }
        }
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onClickFolderIcon(view);
        }
    }

    public void onClickPendingWidget(PendingAppWidgetHostView pendingAppWidgetHostView) {
        if (this.mIsSafeModeEnabled) {
            Toast.makeText(this, 2131558410, 0).show();
            return;
        }
        LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) pendingAppWidgetHostView.getTag();
        if (!pendingAppWidgetHostView.isReadyForClickSetup()) {
            if (launcherAppWidgetInfo.installProgress >= 0) {
                startActivitySafely(pendingAppWidgetHostView, LauncherModel.getMarketIntent(launcherAppWidgetInfo.providerName.getPackageName()), launcherAppWidgetInfo);
                return;
            }
            String packageName = launcherAppWidgetInfo.providerName.getPackageName();
            showBrokenAppInstallDialog(packageName, new DialogInterface.OnClickListener(this, pendingAppWidgetHostView, packageName, launcherAppWidgetInfo) { // from class: com.android.launcher3.Launcher.20
                final Launcher this$0;
                final LauncherAppWidgetInfo val$info;
                final String val$packageName;
                final PendingAppWidgetHostView val$v;

                {
                    this.this$0 = this;
                    this.val$v = pendingAppWidgetHostView;
                    this.val$packageName = packageName;
                    this.val$info = launcherAppWidgetInfo;
                }

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    this.this$0.startActivitySafely(this.val$v, LauncherModel.getMarketIntent(this.val$packageName), this.val$info);
                }
            });
            return;
        }
        int i = launcherAppWidgetInfo.appWidgetId;
        LauncherAppWidgetProviderInfo launcherAppWidgetInfo2 = this.mAppWidgetManager.getLauncherAppWidgetInfo(i);
        if (launcherAppWidgetInfo2 != null) {
            this.mPendingAddWidgetInfo = launcherAppWidgetInfo2;
            this.mPendingAddInfo.copyFrom(launcherAppWidgetInfo);
            this.mPendingAddWidgetId = i;
            AppWidgetManagerCompat.getInstance(this).startConfigActivity(launcherAppWidgetInfo2, launcherAppWidgetInfo.appWidgetId, this, this.mAppWidgetHost, 12);
        }
    }

    protected void onClickSettingsButton(View view) {
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onClickSettingsButton(view);
        } else {
            startActivity(new Intent(this, SettingsActivity.class));
        }
    }

    protected void onClickWallpaperPicker(View view) {
        if (!Utilities.isWallapaperAllowed(this)) {
            Toast.makeText(this, 2131558456, 0).show();
            return;
        }
        startActivityForResult(new Intent("android.intent.action.SET_WALLPAPER").setPackage(getPackageName()).putExtra("com.android.launcher3.WALLPAPER_OFFSET", this.mWorkspace.mWallpaperOffset.wallpaperOffsetForScroll(this.mWorkspace.getScrollForPage(this.mWorkspace.getPageNearestToCenterOfScreen()))), 10);
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onClickWallpaperPicker(view);
        }
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.preOnCreate();
        }
        super.onCreate(bundle);
        LauncherAppState launcherAppState = LauncherAppState.getInstance();
        this.mDeviceProfile = getResources().getConfiguration().orientation == 2 ? launcherAppState.getInvariantDeviceProfile().landscapeProfile : launcherAppState.getInvariantDeviceProfile().portraitProfile;
        this.mSharedPrefs = Utilities.getPrefs(this);
        this.mIsSafeModeEnabled = getPackageManager().isSafeMode();
        this.mModel = launcherAppState.setLauncher(this);
        this.mIconCache = launcherAppState.getIconCache();
        this.mDragController = new DragController(this);
        this.mInflater = getLayoutInflater();
        this.mStateTransitionAnimation = new LauncherStateTransitionAnimation(this);
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "(Launcher)onCreate: savedInstanceState = " + bundle + ", mModel = " + this.mModel + ", mIconCache = " + this.mIconCache + ", this = " + this);
        }
        this.mStats = new Stats(this);
        this.mAppWidgetManager = AppWidgetManagerCompat.getInstance(this);
        this.mAppWidgetHost = new LauncherAppWidgetHost(this, 1024);
        this.mAppWidgetHost.startListening();
        this.mPaused = false;
        setContentView(2130968592);
        launcherAppState.getInvariantDeviceProfile().landscapeProfile.setSearchBarHeight(getSearchBarHeight());
        launcherAppState.getInvariantDeviceProfile().portraitProfile.setSearchBarHeight(getSearchBarHeight());
        setupViews();
        this.mDeviceProfile.layout(this);
        lockAllApps();
        this.mSavedState = bundle;
        restoreState(this.mSavedState);
        if (!this.mRestoring) {
            this.mModel.startLoader(this.mWorkspace.getRestorePage());
        }
        this.mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(this.mDefaultKeySsb, 0);
        registerReceiver(this.mCloseSystemDialogsReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        this.mRotationEnabled = getResources().getBoolean(2131492867);
        if (!this.mRotationEnabled) {
            this.mRotationEnabled = Utilities.isAllowRotationPrefEnabled(getApplicationContext());
        }
        setOrientation();
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onCreate(bundle);
        }
        if (shouldShowIntroScreen()) {
            showIntroScreen();
            return;
        }
        showFirstRunActivity();
        showFirstRunClings();
    }

    @Override // android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "(Launcher)onDestroy: this = " + this);
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(0);
        this.mWorkspace.removeCallbacks(this.mBuildLayersRunnable);
        LauncherAppState launcherAppState = LauncherAppState.getInstance();
        if (this.mModel.isCurrentCallbacks(this)) {
            this.mModel.stopLoader();
            launcherAppState.setLauncher(null);
        }
        try {
            this.mAppWidgetHost.stopListening();
        } catch (NullPointerException e) {
            Log.w("Launcher", "problem while stopping AppWidgetHost during Launcher destruction", e);
        }
        this.mAppWidgetHost = null;
        this.mWidgetsToAdvance.clear();
        TextKeyListener.getInstance().release();
        unregisterReceiver(this.mCloseSystemDialogsReceiver);
        this.mDragLayer.clearAllResizeFrames();
        ((ViewGroup) this.mWorkspace.getParent()).removeAllViews();
        this.mWorkspace.removeAllWorkspaceScreens();
        this.mWorkspace = null;
        this.mDragController = null;
        PackageInstallerCompat.getInstance(this).onStop();
        LauncherAnimUtils.onDestroyActivity();
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onDestroy();
        }
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "onDetachedFromWindow.");
        }
        this.mVisible = false;
        if (this.mAttached) {
            unregisterReceiver(this.mReceiver);
            this.mAttached = false;
        }
        updateAutoAdvanceState();
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onDetachedFromWindow();
        }
    }

    public void onDragStarted(View view) {
        if (isOnCustomContent()) {
            moveWorkspaceToDefaultScreen();
        }
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onDragStarted(view);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onInteractionBegin() {
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onInteractionBegin();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onInteractionEnd() {
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onInteractionEnd();
        }
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        int unicodeChar = keyEvent.getUnicodeChar();
        boolean onKeyDown = super.onKeyDown(i, keyEvent);
        boolean z = unicodeChar > 0 && !Character.isWhitespace(unicodeChar);
        if (LauncherLog.DEBUG_KEY) {
            LauncherLog.d("Launcher", " onKeyDown: KeyCode = " + i + ", KeyEvent = " + keyEvent + ", uniChar = " + unicodeChar + ", handled = " + onKeyDown + ", isKeyNotWhitespace = " + z);
        }
        if (onKeyDown || !acceptFilter() || !z || !TextKeyListener.getInstance().onKeyDown(this.mWorkspace, this.mDefaultKeySsb, i, keyEvent) || this.mDefaultKeySsb == null || this.mDefaultKeySsb.length() <= 0) {
            if (i == 82 && keyEvent.isLongPress()) {
                return true;
            }
            return onKeyDown;
        }
        return onSearchRequested();
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (i == 82) {
            if (isOnCustomContent() || this.mDragController.isDragging()) {
                return true;
            }
            closeFolder();
            this.mWorkspace.exitWidgetResizeMode();
            if (this.mState != State.WORKSPACE || this.mWorkspace.isInOverviewMode() || this.mWorkspace.isSwitchingState()) {
                return true;
            }
            this.mOverviewPanel.requestFocus();
            showOverviewMode(true, true);
            return true;
        }
        return super.onKeyUp(i, keyEvent);
    }

    @Override // com.android.launcher3.LauncherProviderChangeListener
    public void onLauncherProviderChange() {
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onLauncherProviderChange();
        }
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        if (isDraggingEnabled() && !isWorkspaceLocked() && this.mState == State.WORKSPACE) {
            if (view == this.mAllAppsButton) {
                onLongClickAllAppsButton(view);
                return true;
            } else if (view instanceof Workspace) {
                if (this.mWorkspace.isInOverviewMode() || this.mWorkspace.isTouchActive()) {
                    return false;
                }
                showOverviewMode(true);
                this.mWorkspace.performHapticFeedback(0, 1);
                return true;
            } else {
                CellLayout.CellInfo cellInfo = null;
                View view2 = null;
                if (view.getTag() instanceof ItemInfo) {
                    cellInfo = new CellLayout.CellInfo(view, (ItemInfo) view.getTag());
                    view2 = cellInfo.cell;
                    resetAddInfo();
                }
                boolean isHotseatLayout = isHotseatLayout(view);
                if (this.mDragController.isDragging()) {
                    return true;
                }
                if (view2 == null) {
                    this.mWorkspace.performHapticFeedback(0, 1);
                    if (this.mWorkspace.isInOverviewMode()) {
                        this.mWorkspace.startReordering(view);
                        return true;
                    }
                    showOverviewMode(true);
                    return true;
                }
                boolean isAllAppsButtonRank = isHotseatLayout ? isAllAppsButtonRank(this.mHotseat.getOrderInHotseat(cellInfo.cellX, cellInfo.cellY)) : false;
                if (view2 instanceof Folder) {
                    isAllAppsButtonRank = true;
                }
                if (isAllAppsButtonRank) {
                    return true;
                }
                this.mWorkspace.startDrag(cellInfo);
                return true;
            }
        }
        return false;
    }

    protected void onLongClickAllAppsButton(View view) {
        if (isAppsViewVisible()) {
            return;
        }
        showAppsView(true, false, true, false);
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "onNewIntent: intent = " + intent);
        }
        Folder openFolder = this.mWorkspace.getOpenFolder();
        boolean z = this.mHasFocus && (intent.getFlags() & 4194304) != 4194304;
        boolean equals = "android.intent.action.MAIN".equals(intent.getAction());
        if (equals) {
            closeSystemDialogs();
            if (this.mWorkspace == null) {
                return;
            }
            this.mWorkspace.exitWidgetResizeMode();
            closeFolder(z);
            exitSpringLoadedDragMode();
            if (z) {
                showWorkspace(true);
            } else {
                this.mOnResumeState = State.WORKSPACE;
            }
            View peekDecorView = getWindow().peekDecorView();
            if (peekDecorView != null && peekDecorView.getWindowToken() != null) {
                ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(peekDecorView.getWindowToken(), 0);
            }
            if (!z && this.mAppsView != null) {
                this.mAppsView.scrollToTop();
            }
            if (!z && this.mWidgetsView != null) {
                this.mWidgetsView.scrollToTop();
            }
            if (this.mLauncherCallbacks != null) {
                this.mLauncherCallbacks.onHomeIntent();
            }
        }
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onNewIntent(intent);
        }
        if (equals) {
            boolean shouldMoveToDefaultScreenOnHomeIntent = this.mLauncherCallbacks != null ? this.mLauncherCallbacks.shouldMoveToDefaultScreenOnHomeIntent() : true;
            if (z && this.mState == State.WORKSPACE && !this.mWorkspace.isTouchActive() && openFolder == null && shouldMoveToDefaultScreenOnHomeIntent) {
                this.mMoveToDefaultScreenFromNewIntent = true;
                this.mWorkspace.post(new Runnable(this) { // from class: com.android.launcher3.Launcher.17
                    final Launcher this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        if (this.this$0.mWorkspace != null) {
                            this.this$0.mWorkspace.moveToDefaultScreen(true);
                        }
                    }
                });
            }
        }
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void onPageBoundSynchronously(int i) {
        this.mSynchronouslyBoundPages.add(Integer.valueOf(i));
    }

    @Override // com.android.launcher3.PagedView.PageSwitchListener
    public void onPageSwitch(View view, int i) {
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onPageSwitch(view, i);
        }
    }

    @Override // android.app.Activity
    protected void onPause() {
        InstallShortcutReceiver.enableInstallQueue();
        super.onPause();
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "(Launcher)onPause: this = " + this);
        }
        this.mPaused = true;
        this.mDragController.cancelDrag();
        this.mDragController.resetLastGestureUpTime();
        if (this.mWorkspace.getCustomContentCallbacks() != null) {
            this.mWorkspace.getCustomContentCallbacks().onHide();
        }
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onPause();
        }
    }

    @Override // android.app.Activity
    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onPostCreate(bundle);
        }
    }

    @Override // android.app.Activity
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (this.mLauncherCallbacks != null) {
            return this.mLauncherCallbacks.onPrepareOptionsMenu(menu);
        }
        return false;
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 13 && sPendingAddItem != null && sPendingAddItem.requestCode == 13) {
            View view = null;
            CellLayout cellLayout = getCellLayout(sPendingAddItem.container, sPendingAddItem.screenId);
            if (cellLayout != null) {
                view = cellLayout.getChildAt(sPendingAddItem.cellX, sPendingAddItem.cellY);
            }
            Intent intent = sPendingAddItem.intent;
            sPendingAddItem = null;
            if (iArr.length <= 0 || iArr[0] != 0) {
                Toast.makeText(this, getString(2131558431, new Object[]{getString(2131558404)}), 0).show();
            } else {
                startActivitySafely(view, intent, null);
            }
        }
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onRequestPermissionsResult(i, strArr, iArr);
        }
    }

    @Override // android.app.Activity
    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "onRestoreInstanceState: state = " + bundle + ", mSavedInstanceState = " + this.mSavedInstanceState);
        }
        for (Integer num : this.mSynchronouslyBoundPages) {
            this.mWorkspace.restoreInstanceStateForChild(num.intValue());
        }
    }

    @Override // android.app.Activity
    protected void onResume() {
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.preOnResume();
        }
        super.onResume();
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "(Launcher)onResume: mRestoring = " + this.mRestoring + ", mOnResumeNeedsLoad = " + this.mOnResumeNeedsLoad + ",mPagesAreRecreated = , this = " + this);
        }
        switch (BenesseExtension.getDchaState()) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
            case 1:
                Settings.System.putInt(getContentResolver(), "dcha_state", 0);
                Settings.System.putInt(getContentResolver(), "hide_navigation_bar", 0);
                break;
        }
        if (this.mOnResumeState == State.WORKSPACE) {
            showWorkspace(false);
        } else if (this.mOnResumeState == State.APPS) {
            showAppsView(false, false, !(this.mWaitingForResume != null), false);
        } else if (this.mOnResumeState == State.WIDGETS) {
            showWidgetsView(false, false);
        }
        this.mOnResumeState = State.NONE;
        setWorkspaceBackground(this.mState == State.WORKSPACE ? 0 : 1);
        this.mPaused = false;
        if (this.mRestoring || this.mOnResumeNeedsLoad) {
            setWorkspaceLoading(true);
            this.mBindOnResumeCallbacks.clear();
            this.mModel.startLoader(-1001);
            this.mRestoring = false;
            this.mOnResumeNeedsLoad = false;
        }
        if (this.mBindOnResumeCallbacks.size() > 0) {
            for (int i = 0; i < this.mBindOnResumeCallbacks.size(); i++) {
                this.mBindOnResumeCallbacks.get(i).run();
            }
            this.mBindOnResumeCallbacks.clear();
        }
        if (this.mOnResumeCallbacks.size() > 0) {
            for (int i2 = 0; i2 < this.mOnResumeCallbacks.size(); i2++) {
                this.mOnResumeCallbacks.get(i2).run();
            }
            this.mOnResumeCallbacks.clear();
        }
        if (this.mWaitingForResume != null) {
            this.mWaitingForResume.setStayPressed(false);
        }
        if (!isWorkspaceLoading()) {
            getWorkspace().reinflateWidgetsIfNecessary();
        }
        reinflateQSBIfNecessary();
        if (this.mWorkspace.getCustomContentCallbacks() != null && !this.mMoveToDefaultScreenFromNewIntent && this.mWorkspace.isOnOrMovingToCustomContent()) {
            this.mWorkspace.getCustomContentCallbacks().onShow(true);
        }
        this.mMoveToDefaultScreenFromNewIntent = false;
        updateInteraction(Workspace.State.NORMAL, this.mWorkspace.getState());
        this.mWorkspace.onResume();
        if (!isWorkspaceLoading()) {
            InstallShortcutReceiver.disableAndFlushInstallQueue(this);
        }
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onResume();
        }
    }

    @Override // android.app.Activity
    public Object onRetainNonConfigurationInstance() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "onRetainNonConfigurationInstance: mSavedState = " + this.mSavedState + ", mSavedInstanceState = " + this.mSavedInstanceState);
        }
        if (this.mModel.isCurrentCallbacks(this)) {
            this.mModel.stopLoader();
        }
        return Boolean.TRUE;
    }

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        if (isWorkspaceLoading() && this.mSavedState != null) {
            bundle.putAll(this.mSavedState);
            return;
        }
        if (this.mWorkspace.getChildCount() > 0) {
            bundle.putInt("launcher.current_screen", this.mWorkspace.getCurrentPageOffsetFromCustomContent());
        } else {
            bundle.putInt("launcher.current_screen", this.mCurrentWorkSpaceScreen);
        }
        super.onSaveInstanceState(bundle);
        bundle.putInt("launcher.state", this.mState.ordinal());
        closeFolder(false);
        if (this.mPendingAddInfo.container != -1 && this.mPendingAddInfo.screenId > -1 && this.mWaitingForResult) {
            bundle.putLong("launcher.add_container", this.mPendingAddInfo.container);
            bundle.putLong("launcher.add_screen", this.mPendingAddInfo.screenId);
            bundle.putInt("launcher.add_cell_x", this.mPendingAddInfo.cellX);
            bundle.putInt("launcher.add_cell_y", this.mPendingAddInfo.cellY);
            bundle.putInt("launcher.add_span_x", this.mPendingAddInfo.spanX);
            bundle.putInt("launcher.add_span_y", this.mPendingAddInfo.spanY);
            bundle.putParcelable("launcher.add_component", this.mPendingAddInfo.componentName);
            bundle.putParcelable("launcher.add_widget_info", this.mPendingAddWidgetInfo);
            bundle.putInt("launcher.add_widget_id", this.mPendingAddWidgetId);
        }
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onSaveInstanceState(bundle);
        }
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean onSearchRequested() {
        startSearch((String) null, false, (Bundle) null, true);
        return true;
    }

    @Override // com.android.launcher3.LauncherProviderChangeListener
    public void onSettingsChanged(String str, boolean z) {
        if ("pref_allowRotation".equals(str)) {
            this.mRotationEnabled = z;
            if (waitUntilResume(this.mUpdateOrientationRunnable, true)) {
                return;
            }
            this.mUpdateOrientationRunnable.run();
        }
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "(Launcher)onStart: this = " + this);
        }
        FirstFrameAnimatorHelper.setIsVisible(true);
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onStart();
        }
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "(Launcher)onStop: this = " + this);
        }
        FirstFrameAnimatorHelper.setIsVisible(false);
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onStop();
        }
    }

    @Override // android.view.View.OnTouchListener
    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks2
    public void onTrimMemory(int i) {
        super.onTrimMemory(i);
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "onTrimMemory: level = " + i);
        }
        if (i >= 20) {
            SQLiteDatabase.releaseMemory();
        }
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onTrimMemory(i);
        }
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        this.mHasFocus = z;
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onWindowFocusChanged(z);
        }
    }

    public void onWindowVisibilityChanged(int i) {
        boolean z = false;
        if (i == 0) {
            z = true;
        }
        this.mVisible = z;
        updateAutoAdvanceState();
        if (this.mVisible) {
            if (!this.mWorkspaceLoading) {
                this.mWorkspace.getViewTreeObserver().addOnDrawListener(new AnonymousClass16(this));
            }
            clearTypedText();
        }
    }

    protected void onWorkspaceLockedChanged() {
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.onWorkspaceLockedChanged();
        }
    }

    public void openFolder(FolderIcon folderIcon) {
        Folder folder = null;
        Folder folder2 = folderIcon.getFolder();
        if (this.mWorkspace != null) {
            folder = this.mWorkspace.getOpenFolder();
        }
        if (folder != null && folder != folder2) {
            closeFolder();
        }
        folder2.mInfo.opened = true;
        ((CellLayout.LayoutParams) folderIcon.getLayoutParams()).canReorder = false;
        if (folder2.getParent() == null) {
            this.mDragLayer.addView(folder2);
            this.mDragController.addDropTarget(folder2);
        } else {
            Log.w("Launcher", "Opening folder (" + folder2 + ") which already has a parent (" + folder2.getParent() + ").");
        }
        folder2.animateOpen();
        growAndFadeOutFolderIcon(folderIcon);
        folder2.sendAccessibilityEvent(32);
        getDragLayer().sendAccessibilityEvent(2048);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean overrideWallpaperDimensions() {
        if (this.mLauncherCallbacks != null) {
            return this.mLauncherCallbacks.overrideWallpaperDimensions();
        }
        return true;
    }

    protected void populateCustomContentContainer() {
        if (this.mLauncherCallbacks != null) {
            this.mLauncherCallbacks.populateCustomContentContainer();
        }
    }

    public boolean removeItem(View view, ItemInfo itemInfo, boolean z) {
        if (itemInfo instanceof ShortcutInfo) {
            FolderInfo folderInfo = sFolders.get(itemInfo.container);
            if (folderInfo != null) {
                folderInfo.remove((ShortcutInfo) itemInfo);
            } else {
                this.mWorkspace.removeWorkspaceItem(view);
            }
            if (z) {
                LauncherModel.deleteItemFromDatabase(this, itemInfo);
                return true;
            }
            return true;
        } else if (itemInfo instanceof FolderInfo) {
            FolderInfo folderInfo2 = (FolderInfo) itemInfo;
            unbindFolder(folderInfo2);
            this.mWorkspace.removeWorkspaceItem(view);
            if (z) {
                LauncherModel.deleteFolderAndContentsFromDatabase(this, folderInfo2);
                return true;
            }
            return true;
        } else if (itemInfo instanceof LauncherAppWidgetInfo) {
            LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) itemInfo;
            this.mWorkspace.removeWorkspaceItem(view);
            removeWidgetToAutoAdvance(launcherAppWidgetInfo.hostView);
            launcherAppWidgetInfo.hostView = null;
            if (z) {
                deleteWidgetInfo(launcherAppWidgetInfo);
                return true;
            }
            return true;
        } else {
            return false;
        }
    }

    public Drawable resizeIconDrawable(Drawable drawable) {
        drawable.setBounds(0, 0, this.mDeviceProfile.iconSizePx, this.mDeviceProfile.iconSizePx);
        return drawable;
    }

    void sendAdvanceMessage(long j) {
        this.mHandler.removeMessages(1);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), j);
        this.mAutoAdvanceSentTime = System.currentTimeMillis();
    }

    public void setAllAppsButton(View view) {
        this.mAllAppsButton = view;
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public boolean setLoadOnResume() {
        if (this.mPaused) {
            this.mOnResumeNeedsLoad = true;
            return true;
        }
        return false;
    }

    void setOrientation() {
        if (this.mRotationEnabled) {
            unlockScreenOrientation(true);
        } else {
            setRequestedOrientation(5);
        }
    }

    void showAppsView(boolean z, boolean z2, boolean z3, boolean z4) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "showAppsView: animated = " + z + ", mState = " + this.mState);
        }
        if (z2) {
            this.mAppsView.scrollToTop();
        }
        if (z3) {
            tryAndUpdatePredictedApps();
        }
        showAppsOrWidgets(State.APPS, z, z4);
    }

    public boolean showFirstRunActivity() {
        Intent firstRunActivity;
        if (shouldRunFirstRunActivity() && hasFirstRunActivity() && (firstRunActivity = getFirstRunActivity()) != null) {
            startActivity(firstRunActivity);
            markFirstRunActivityShown();
            return true;
        }
        return false;
    }

    void showFirstRunClings() {
        LauncherClings launcherClings = new LauncherClings(this);
        if (launcherClings.shouldShowFirstRunOrMigrationClings()) {
            this.mClings = launcherClings;
            if (this.mModel.canMigrateFromOldLauncherDb(this)) {
                launcherClings.showMigrationCling();
            } else {
                launcherClings.showLongPressCling(true);
            }
        }
    }

    protected void showIntroScreen() {
        View introScreen = getIntroScreen();
        changeWallpaperVisiblity(false);
        if (introScreen != null) {
            this.mDragLayer.showOverlayView(introScreen);
        }
    }

    public void showOutOfSpaceMessage(boolean z) {
        Toast.makeText(this, getString(z ? 2131558419 : 2131558418), 0).show();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void showOverviewMode(boolean z) {
        showOverviewMode(z, false);
    }

    void showOverviewMode(boolean z, boolean z2) {
        Runnable runnable = null;
        if (z2) {
            runnable = new Runnable(this) { // from class: com.android.launcher3.Launcher.25
                final Launcher this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mOverviewPanel.requestFocusFromTouch();
                }
            };
        }
        this.mWorkspace.setVisibility(0);
        this.mStateTransitionAnimation.startAnimationToWorkspace(this.mState, this.mWorkspace.getState(), Workspace.State.OVERVIEW, -1, z, runnable);
        this.mState = State.WORKSPACE;
    }

    void showWidgetsView(boolean z, boolean z2) {
        if (z2) {
            this.mWidgetsView.scrollToTop();
        }
        showAppsOrWidgets(State.WIDGETS, z, false);
        this.mWidgetsView.post(new Runnable(this) { // from class: com.android.launcher3.Launcher.26
            final Launcher this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mWidgetsView.requestFocus();
            }
        });
    }

    protected boolean showWorkspace(int i, boolean z) {
        return showWorkspace(i, z, null);
    }

    boolean showWorkspace(int i, boolean z, Runnable runnable) {
        LauncherHelper.beginSection("showWorkspace");
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "showWorkspace: animated = " + z + ", mState = " + this.mState);
        }
        if (this.mWorkspace == null) {
            LauncherHelper.endSection();
            return false;
        }
        boolean z2 = this.mState == State.WORKSPACE ? this.mWorkspace.getState() != Workspace.State.NORMAL : true;
        if (z2) {
            this.mWorkspace.setVisibility(0);
            this.mStateTransitionAnimation.startAnimationToWorkspace(this.mState, this.mWorkspace.getState(), Workspace.State.NORMAL, i, z, runnable);
            if (this.mAllAppsButton != null) {
                this.mAllAppsButton.requestFocus();
            }
        }
        this.mState = State.WORKSPACE;
        this.mUserPresent = true;
        updateAutoAdvanceState();
        if (z2) {
            getWindow().getDecorView().sendAccessibilityEvent(32);
        }
        LauncherHelper.endSection();
        return z2;
    }

    public boolean showWorkspace(boolean z) {
        return showWorkspace(-1, z, null);
    }

    public boolean showWorkspace(boolean z, Runnable runnable) {
        return showWorkspace(-1, z, runnable);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void showWorkspaceSearchAndHotseat() {
        if (this.mWorkspace != null) {
            this.mWorkspace.setAlpha(1.0f);
        }
        if (this.mHotseat != null) {
            this.mHotseat.setAlpha(1.0f);
        }
        if (this.mPageIndicators != null) {
            this.mPageIndicators.setAlpha(1.0f);
        }
        if (this.mSearchDropTargetBar != null) {
            this.mSearchDropTargetBar.animateToState(SearchDropTargetBar.State.SEARCH_BAR, 0);
        }
    }

    @Override // android.app.Activity
    public void startActivityForResult(Intent intent, int i) {
        onStartForResult(i);
        super.startActivityForResult(intent, i);
    }

    public boolean startActivitySafely(View view, Intent intent, Object obj) {
        boolean z;
        if (this.mIsSafeModeEnabled && !Utilities.isSystemApp(this, intent)) {
            Toast.makeText(this, 2131558409, 0).show();
            return false;
        }
        try {
            z = startActivity(view, intent, obj);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, 2131558407, 0).show();
            Log.e("Launcher", "Unable to launch. tag=" + obj + " intent=" + intent, e);
            z = false;
        }
        return z;
    }

    void startAppShortcutOrInfoActivity(View view) {
        ShortcutInfo shortcutInfo;
        Intent intent;
        Object tag = view.getTag();
        if (tag instanceof ShortcutInfo) {
            shortcutInfo = (ShortcutInfo) tag;
            intent = shortcutInfo.intent;
            int[] iArr = new int[2];
            view.getLocationOnScreen(iArr);
            intent.setSourceBounds(new Rect(iArr[0], iArr[1], iArr[0] + view.getWidth(), iArr[1] + view.getHeight()));
        } else if (!(tag instanceof AppInfo)) {
            throw new IllegalArgumentException("Input must be a Shortcut or AppInfo");
        } else {
            shortcutInfo = null;
            intent = ((AppInfo) tag).intent;
        }
        boolean startActivitySafely = startActivitySafely(view, intent, tag);
        this.mStats.recordLaunch(view, intent, shortcutInfo);
        if (startActivitySafely && (view instanceof BubbleTextView)) {
            this.mWaitingForResume = (BubbleTextView) view;
            this.mWaitingForResume.setStayPressed(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void startApplicationDetailsActivity(ComponentName componentName, UserHandleCompat userHandleCompat) {
        try {
            LauncherAppsCompat.getInstance(this).showAppDetailsForProfile(componentName, userHandleCompat);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, 2131558407, 0).show();
            Log.e("Launcher", "Unable to launch settings");
        } catch (SecurityException e2) {
            Toast.makeText(this, 2131558407, 0).show();
            Log.e("Launcher", "Launcher does not have permission to launch settings");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean startApplicationUninstallActivity(ComponentName componentName, int i, UserHandleCompat userHandleCompat) {
        if ((i & 1) == 0) {
            Toast.makeText(this, 2131558434, 0).show();
            return false;
        }
        Intent intent = new Intent("android.intent.action.DELETE", Uri.fromParts("package", componentName.getPackageName(), componentName.getClassName()));
        intent.setFlags(276824064);
        if (userHandleCompat != null) {
            userHandleCompat.addToIntent(intent, "android.intent.extra.USER");
        }
        startActivity(intent);
        return true;
    }

    @Override // com.android.launcher3.LauncherModel.Callbacks
    public void startBinding() {
        setWorkspaceLoading(true);
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "startBinding: this = " + this);
        }
        this.mBindOnResumeCallbacks.clear();
        this.mWorkspace.clearDropTargets();
        this.mWorkspace.removeAllWorkspaceScreens();
        this.mWidgetsToAdvance.clear();
        if (this.mHotseat != null) {
            this.mHotseat.resetLayout();
        }
    }

    @Override // android.app.Activity
    public void startIntentSenderForResult(IntentSender intentSender, int i, Intent intent, int i2, int i3, int i4, Bundle bundle) {
        onStartForResult(i);
        try {
            super.startIntentSenderForResult(intentSender, i, intent, i2, i3, i4, bundle);
        } catch (IntentSender.SendIntentException e) {
            throw new ActivityNotFoundException();
        }
    }

    @Override // android.app.Activity
    public void startSearch(String str, boolean z, Bundle bundle, boolean z2) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher", "startSearch.");
        }
        String str2 = str;
        if (str == null) {
            str2 = getTypedText();
        }
        Bundle bundle2 = bundle;
        if (bundle == null) {
            bundle2 = new Bundle();
            bundle2.putString("source", "launcher-search");
        }
        Rect rect = new Rect();
        if (this.mSearchDropTargetBar != null) {
            rect = this.mSearchDropTargetBar.getSearchBarBounds();
        }
        if (startSearch(str2, z, bundle2, rect)) {
            clearTypedText();
        }
        showWorkspace(true);
    }

    public boolean startSearch(String str, boolean z, Bundle bundle, Rect rect) {
        if (this.mLauncherCallbacks == null || !this.mLauncherCallbacks.providesSearch()) {
            startGlobalSearch(str, z, bundle, rect);
            return false;
        }
        return this.mLauncherCallbacks.startSearch(str, z, bundle, rect);
    }

    public Animator startWorkspaceStateChangeAnimation(Workspace.State state, int i, boolean z, HashMap<View, Integer> hashMap) {
        Workspace.State state2 = this.mWorkspace.getState();
        Animator stateWithAnimation = this.mWorkspace.setStateWithAnimation(state, i, z, hashMap);
        updateInteraction(state2, state);
        return stateWithAnimation;
    }

    public void unlockScreenOrientation(boolean z) {
        if (!this.mRotationEnabled) {
            setRequestedOrientation(5);
        } else if (z) {
            setRequestedOrientation(-1);
        } else {
            this.mHandler.postDelayed(new Runnable(this) { // from class: com.android.launcher3.Launcher.41
                final Launcher this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.setRequestedOrientation(-1);
                }
            }, 500L);
        }
    }

    void updateAutoAdvanceState() {
        long j = 20000;
        boolean z = this.mVisible && this.mUserPresent && !this.mWidgetsToAdvance.isEmpty();
        if (z != this.mAutoAdvanceRunning) {
            this.mAutoAdvanceRunning = z;
            if (z) {
                if (this.mAutoAdvanceTimeLeft != -1) {
                    j = this.mAutoAdvanceTimeLeft;
                }
                sendAdvanceMessage(j);
                return;
            }
            if (!this.mWidgetsToAdvance.isEmpty()) {
                this.mAutoAdvanceTimeLeft = Math.max(0L, 20000 - (System.currentTimeMillis() - this.mAutoAdvanceSentTime));
            }
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(0);
        }
    }

    public void updateInteraction(Workspace.State state, Workspace.State state2) {
        boolean z = state != Workspace.State.NORMAL;
        if (state2 != Workspace.State.NORMAL) {
            onInteractionBegin();
        } else if (z) {
            onInteractionEnd();
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:10:0x001d  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    boolean waitUntilResume(Runnable runnable, boolean z) {
        boolean z2;
        ComponentName componentName;
        if (this.mPaused) {
            if (!z) {
                if (runnable instanceof AppsUpdateTask) {
                }
                this.mBindOnResumeCallbacks.add(runnable);
                return true;
            }
            do {
            } while (this.mBindOnResumeCallbacks.remove(runnable));
            if (runnable instanceof AppsUpdateTask) {
                ArrayList<AppInfo> apps = ((AppsUpdateTask) runnable).getApps();
                if (apps.size() <= 0) {
                    Log.e("Launcher", "Error: curAppsSize is 0");
                } else {
                    ArrayList<Runnable> arrayList = new ArrayList();
                    for (Runnable runnable2 : this.mBindOnResumeCallbacks) {
                        if (runnable2 instanceof AppsUpdateTask) {
                            ArrayList<AppInfo> apps2 = ((AppsUpdateTask) runnable2).getApps();
                            if (apps2.size() <= 0) {
                                Log.e("Launcher", "Error: oldAppsSize is 0");
                            } else {
                                boolean z3 = false;
                                Iterator<T> it = apps2.iterator();
                                do {
                                    z2 = z3;
                                    if (!it.hasNext()) {
                                        break;
                                    }
                                    ComponentName componentName2 = ((AppInfo) it.next()).componentName;
                                    Iterator<T> it2 = apps.iterator();
                                    do {
                                        z2 = z3;
                                        if (!it2.hasNext()) {
                                            break;
                                        }
                                        componentName = ((AppInfo) it2.next()).componentName;
                                        if (componentName2 != null || componentName != null) {
                                            break;
                                        }
                                    } while (componentName2.toString() != componentName.toString());
                                    z2 = true;
                                    z3 = z2;
                                } while (z2);
                                if (z2) {
                                    arrayList.add(runnable2);
                                }
                            }
                        }
                    }
                    for (Runnable runnable3 : arrayList) {
                        Log.d("Launcher", "Debug: 1 pending task was removed");
                        this.mBindOnResumeCallbacks.remove(runnable3);
                    }
                    arrayList.clear();
                }
            }
            this.mBindOnResumeCallbacks.add(runnable);
            return true;
        }
        return false;
    }
}
