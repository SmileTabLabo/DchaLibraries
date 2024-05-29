package com.android.browser;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.android.browser.PermissionHelper;
import com.android.browser.provider.SnapshotProvider;
import com.android.browser.stub.NullController;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
/* loaded from: b.zip:com/android/browser/BrowserActivity.class */
public class BrowserActivity extends Activity {
    private static final String[] DELETE_WHERE_ARGS = {"100", "0"};
    private boolean mAllGranted;
    private KeyguardManager mKeyguardManager;
    private PowerManager mPowerManager;
    private ActivityController mController = NullController.INSTANCE;
    private PermissionHelper.PermissionCallback mPermissionCallback = new PermissionHelper.PermissionCallback(this) { // from class: com.android.browser.BrowserActivity.1
        final BrowserActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.browser.PermissionHelper.PermissionCallback
        public void onPermissionsResult(int i, String[] strArr, int[] iArr) {
            if (iArr == null || iArr.length <= 0) {
                return;
            }
            this.this$0.mAllGranted = true;
            int i2 = 0;
            while (true) {
                if (i2 >= iArr.length) {
                    break;
                } else if (iArr[i2] != 0) {
                    this.this$0.mAllGranted = false;
                    Log.d("browser/BrowserActivity", strArr[i2] + " is not granted !");
                    break;
                } else {
                    i2++;
                }
            }
            if (!this.this$0.mAllGranted) {
                Toast.makeText(this.this$0.getApplicationContext(), this.this$0.getString(134545700), 1).show();
                this.this$0.finish();
            }
            this.this$0.doResume();
        }
    };

    /* loaded from: b.zip:com/android/browser/BrowserActivity$DeleteFailedDownload.class */
    private class DeleteFailedDownload implements Runnable {
        final BrowserActivity this$0;

        private DeleteFailedDownload(BrowserActivity browserActivity) {
            this.this$0 = browserActivity;
        }

        /* synthetic */ DeleteFailedDownload(BrowserActivity browserActivity, DeleteFailedDownload deleteFailedDownload) {
            this(browserActivity);
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.getContentResolver().delete(SnapshotProvider.Snapshots.CONTENT_URI, "progress < ? AND is_done = ?", BrowserActivity.DELETE_WHERE_ARGS);
        }
    }

    private Controller createController() {
        Controller controller = new Controller(this);
        controller.setUi(isTablet(this) ? new XLargeUi(this, controller) : new PhoneUi(this, controller));
        return controller;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doResume() {
        this.mController.onResume();
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(2131296256);
    }

    private boolean shouldIgnoreIntents() {
        if (this.mKeyguardManager == null) {
            this.mKeyguardManager = (KeyguardManager) getSystemService("keyguard");
        }
        if (this.mPowerManager == null) {
            this.mPowerManager = (PowerManager) getSystemService("power");
        }
        boolean z = !this.mPowerManager.isScreenOn();
        Log.v("browser", "ignore intents: " + z);
        return z;
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchGenericMotionEvent(MotionEvent motionEvent) {
        return !this.mController.dispatchGenericMotionEvent(motionEvent) ? super.dispatchGenericMotionEvent(motionEvent) : true;
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        return !this.mController.dispatchKeyEvent(keyEvent) ? super.dispatchKeyEvent(keyEvent) : true;
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchKeyShortcutEvent(KeyEvent keyEvent) {
        return !this.mController.dispatchKeyShortcutEvent(keyEvent) ? super.dispatchKeyShortcutEvent(keyEvent) : true;
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return !this.mController.dispatchTouchEvent(motionEvent) ? super.dispatchTouchEvent(motionEvent) : true;
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchTrackballEvent(MotionEvent motionEvent) {
        return !this.mController.dispatchTrackballEvent(motionEvent) ? super.dispatchTrackballEvent(motionEvent) : true;
    }

    @VisibleForTesting
    Controller getController() {
        return (Controller) this.mController;
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onActionModeFinished(ActionMode actionMode) {
        super.onActionModeFinished(actionMode);
        this.mController.onActionModeFinished(actionMode);
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onActionModeStarted(ActionMode actionMode) {
        super.onActionModeStarted(actionMode);
        this.mController.onActionModeStarted(actionMode);
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        this.mController.onActivityResult(i, i2, intent);
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mController.onConfgurationChanged(configuration);
    }

    @Override // android.app.Activity
    public boolean onContextItemSelected(MenuItem menuItem) {
        return this.mController.onContextItemSelected(menuItem);
    }

    @Override // android.app.Activity
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        this.mController.onContextMenuClosed(menu);
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mAllGranted = false;
        PermissionHelper.init(this);
        if (isTablet(this)) {
            getWindow().setSoftInputMode(16);
        }
        if (shouldIgnoreIntents()) {
            finish();
        } else if (IntentHandler.handleWebSearchIntent(this, null, getIntent())) {
            finish();
        } else {
            this.mController = createController();
            this.mController.start(bundle == null ? getIntent() : null);
            new Thread(new DeleteFailedDownload(this, null)).start();
        }
    }

    @Override // android.app.Activity, android.view.View.OnCreateContextMenuListener
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        this.mController.onCreateContextMenu(contextMenu, view, contextMenuInfo);
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return this.mController.onCreateOptionsMenu(menu);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.mController.onDestroy();
        this.mController = NullController.INSTANCE;
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        return !this.mController.onKeyDown(i, keyEvent) ? super.onKeyDown(i, keyEvent) : true;
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyLongPress(int i, KeyEvent keyEvent) {
        return !this.mController.onKeyLongPress(i, keyEvent) ? super.onKeyLongPress(i, keyEvent) : true;
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        return !this.mController.onKeyUp(i, keyEvent) ? super.onKeyUp(i, keyEvent) : true;
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onLowMemory() {
        super.onLowMemory();
        this.mController.onLowMemory();
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean onMenuOpened(int i, Menu menu) {
        if (i == 0) {
            this.mController.onMenuOpened(i, menu);
            return true;
        }
        return true;
    }

    @Override // android.app.Activity
    public void onMultiWindowModeChanged(boolean z) {
        WindowManager.LayoutParams attributes;
        if (z || !isTablet(this) || (attributes = getWindow().getAttributes()) == null) {
            return;
        }
        attributes.flags |= 16777216;
        attributes.flags &= Integer.MIN_VALUE;
        getWindow().setAttributes(attributes);
        Log.d("browser", "BrowserActivity.onMultiWindowModeChanged");
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        if (shouldIgnoreIntents()) {
            return;
        }
        if (this.mController == NullController.INSTANCE) {
            Log.w("browser/BrowserActivity", "onNewIntent for Action_Search Intent reached before finish(), so enter onNewIntent instead of on create");
            startActivity(intent);
            finish();
        } else if (!"--restart--".equals(intent.getAction())) {
            this.mController.handleNewIntent(intent);
        } else {
            Bundle bundle = new Bundle();
            this.mController.onSaveInstanceState(bundle);
            finish();
            getApplicationContext().startActivity(new Intent(getApplicationContext(), BrowserActivity.class).addFlags(268435456).putExtra("state", bundle));
        }
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (this.mController.onOptionsItemSelected(menuItem)) {
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override // android.app.Activity
    public void onOptionsMenuClosed(Menu menu) {
        this.mController.onOptionsMenuClosed(menu);
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
    }

    @Override // android.app.Activity
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return this.mController.onPrepareOptionsMenu(menu);
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        Log.d("browser/BrowserActivity", " onRequestPermissionsResult " + i);
        PermissionHelper.getInstance().onPermissionsResult(i, strArr, iArr);
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        if (this.mAllGranted) {
            doResume();
            return;
        }
        List<String> allUngrantedPermissions = PermissionHelper.getInstance().getAllUngrantedPermissions();
        if (allUngrantedPermissions.size() > 0) {
            PermissionHelper.getInstance().requestPermissions(allUngrantedPermissions, this.mPermissionCallback);
        } else {
            doResume();
        }
    }

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        this.mController.onSaveInstanceState(bundle);
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean onSearchRequested() {
        return this.mController.onSearchRequested();
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
    }

    @Override // android.app.Activity
    protected void onStop() {
        this.mController.onPause();
        super.onStop();
    }
}
