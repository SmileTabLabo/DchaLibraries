package com.android.launcher3;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityManager;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/launcher3/LauncherClings.class */
public class LauncherClings implements View.OnClickListener {
    private LayoutInflater mInflater;
    boolean mIsVisible;
    Launcher mLauncher;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.launcher3.LauncherClings$1  reason: invalid class name */
    /* loaded from: a.zip:com/android/launcher3/LauncherClings$1.class */
    public class AnonymousClass1 implements Runnable {
        final LauncherClings this$0;

        AnonymousClass1(LauncherClings launcherClings) {
            this.this$0 = launcherClings;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.dismissCling(this.this$0.mLauncher.findViewById(2131296298), new Runnable(this) { // from class: com.android.launcher3.LauncherClings.1.1
                final AnonymousClass1 this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.showLongPressCling(false);
                }
            }, "cling_gel.migration.dismissed", 200);
        }
    }

    public LauncherClings(Launcher launcher) {
        this.mLauncher = launcher;
        this.mInflater = LayoutInflater.from(this.mLauncher);
    }

    @TargetApi(18)
    private boolean areClingsEnabled() {
        if (ActivityManager.isRunningInTestHarness() || ((AccessibilityManager) this.mLauncher.getSystemService("accessibility")).isTouchExplorationEnabled()) {
            return false;
        }
        return ((Utilities.ATLEAST_JB_MR2 && ((UserManager) this.mLauncher.getSystemService("user")).getUserRestrictions().getBoolean("no_modify_accounts", false)) || Settings.Secure.getInt(this.mLauncher.getContentResolver(), "skip_first_use_hints", 0) == 1) ? false : true;
    }

    private void dismissMigrationCling() {
        this.mLauncher.showWorkspaceSearchAndHotseat();
        this.mLauncher.getWorkspace().post(new AnonymousClass1(this));
    }

    public static void markFirstRunClingDismissed(Context context) {
        Utilities.getPrefs(context).edit().putBoolean("cling_gel.workspace.dismissed", true).apply();
    }

    void dismissCling(View view, Runnable runnable, String str, int i) {
        if (view == null || view.getVisibility() == 8) {
            return;
        }
        Runnable runnable2 = new Runnable(this, view, str, runnable) { // from class: com.android.launcher3.LauncherClings.5
            final LauncherClings this$0;
            final View val$cling;
            final String val$flag;
            final Runnable val$postAnimationCb;

            {
                this.this$0 = this;
                this.val$cling = view;
                this.val$flag = str;
                this.val$postAnimationCb = runnable;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$cling.setVisibility(8);
                this.this$0.mLauncher.getSharedPrefs().edit().putBoolean(this.val$flag, true).apply();
                this.this$0.mIsVisible = false;
                if (this.val$postAnimationCb != null) {
                    this.val$postAnimationCb.run();
                }
            }
        };
        if (i <= 0) {
            runnable2.run();
        } else {
            view.animate().alpha(0.0f).setDuration(i).withEndAction(runnable2);
        }
    }

    void dismissLongPressCling() {
        this.mLauncher.getWorkspace().post(new Runnable(this) { // from class: com.android.launcher3.LauncherClings.4
            final LauncherClings this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.dismissCling(this.this$0.mLauncher.findViewById(2131296295), null, "cling_gel.workspace.dismissed", 200);
            }
        });
    }

    public boolean isVisible() {
        return this.mIsVisible;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        int id = view.getId();
        if (id == 2131296301) {
            dismissMigrationCling();
        } else if (id != 2131296300) {
            if (id == 2131296297) {
                dismissLongPressCling();
            }
        } else {
            LauncherModel model = this.mLauncher.getModel();
            model.resetLoadedState(false, true);
            model.startLoader(-1001, 3);
            SharedPreferences.Editor edit = Utilities.getPrefs(this.mLauncher).edit();
            edit.putBoolean("launcher.user_migrated_from_old_data", true);
            edit.apply();
            dismissMigrationCling();
        }
    }

    public boolean shouldShowFirstRunOrMigrationClings() {
        SharedPreferences sharedPrefs = this.mLauncher.getSharedPrefs();
        boolean z = false;
        if (areClingsEnabled()) {
            if (sharedPrefs.getBoolean("cling_gel.workspace.dismissed", false)) {
                z = false;
            } else {
                z = false;
                if (!sharedPrefs.getBoolean("cling_gel.migration.dismissed", false)) {
                    z = true;
                }
            }
        }
        return z;
    }

    public void showLongPressCling(boolean z) {
        this.mIsVisible = true;
        ViewGroup viewGroup = (ViewGroup) this.mLauncher.findViewById(2131296287);
        View inflate = this.mInflater.inflate(2130968593, viewGroup, false);
        inflate.setOnLongClickListener(new View.OnLongClickListener(this) { // from class: com.android.launcher3.LauncherClings.2
            final LauncherClings this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view) {
                this.this$0.mLauncher.showOverviewMode(true);
                this.this$0.dismissLongPressCling();
                return true;
            }
        });
        ViewGroup viewGroup2 = (ViewGroup) inflate.findViewById(2131296296);
        this.mInflater.inflate(z ? 2130968595 : 2130968594, viewGroup2);
        viewGroup2.findViewById(2131296297).setOnClickListener(this);
        if ("crop_bg_top_and_sides".equals(viewGroup2.getTag())) {
            viewGroup2.setBackground(new BorderCropDrawable(this.mLauncher.getResources().getDrawable(2130837509), true, true, true, false));
        }
        viewGroup.addView(inflate);
        if (z) {
            return;
        }
        viewGroup2.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(this, viewGroup2) { // from class: com.android.launcher3.LauncherClings.3
            final LauncherClings this$0;
            final ViewGroup val$content;

            {
                this.this$0 = this;
                this.val$content = viewGroup2;
            }

            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                ObjectAnimator ofPropertyValuesHolder;
                this.val$content.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if ("crop_bg_top_and_sides".equals(this.val$content.getTag())) {
                    this.val$content.setTranslationY(-this.val$content.getMeasuredHeight());
                    ofPropertyValuesHolder = LauncherAnimUtils.ofFloat(this.val$content, "translationY", 0.0f);
                } else {
                    this.val$content.setScaleX(0.0f);
                    this.val$content.setScaleY(0.0f);
                    ofPropertyValuesHolder = LauncherAnimUtils.ofPropertyValuesHolder(this.val$content, PropertyValuesHolder.ofFloat("scaleX", 1.0f), PropertyValuesHolder.ofFloat("scaleY", 1.0f));
                }
                ofPropertyValuesHolder.setDuration(250L);
                ofPropertyValuesHolder.setInterpolator(new LogDecelerateInterpolator(100, 0));
                ofPropertyValuesHolder.start();
            }
        });
    }

    public void showMigrationCling() {
        this.mIsVisible = true;
        this.mLauncher.hideWorkspaceSearchAndHotseat();
        View inflate = this.mInflater.inflate(2130968596, (ViewGroup) this.mLauncher.findViewById(2131296287));
        inflate.findViewById(2131296300).setOnClickListener(this);
        inflate.findViewById(2131296301).setOnClickListener(this);
    }
}
