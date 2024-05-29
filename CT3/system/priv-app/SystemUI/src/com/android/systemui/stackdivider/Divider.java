package com.android.systemui.stackdivider;

import android.content.res.Configuration;
import android.os.RemoteException;
import android.view.IDockedStackListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.android.systemui.SystemUI;
import com.android.systemui.recents.Recents;
import java.io.FileDescriptor;
import java.io.PrintWriter;
/* loaded from: a.zip:com/android/systemui/stackdivider/Divider.class */
public class Divider extends SystemUI {
    private DockDividerVisibilityListener mDockDividerVisibilityListener;
    private ForcedResizableInfoActivityController mForcedResizableController;
    private DividerView mView;
    private DividerWindowManager mWindowManager;
    private final DividerState mDividerState = new DividerState();
    private boolean mVisible = false;
    private boolean mMinimized = false;
    private boolean mAdjustedForIme = false;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/systemui/stackdivider/Divider$DockDividerVisibilityListener.class */
    public class DockDividerVisibilityListener extends IDockedStackListener.Stub {
        final Divider this$0;

        DockDividerVisibilityListener(Divider divider) {
            this.this$0 = divider;
        }

        /* renamed from: -com_android_systemui_stackdivider_Divider$DockDividerVisibilityListener_lambda$1  reason: not valid java name */
        /* synthetic */ void m1351xc2b650df(boolean z, long j) {
            if (this.this$0.mAdjustedForIme != z) {
                this.this$0.mAdjustedForIme = z;
                this.this$0.updateTouchable();
                if (this.this$0.mMinimized) {
                    return;
                }
                if (j > 0) {
                    this.this$0.mView.setAdjustedForIme(z, j);
                } else {
                    this.this$0.mView.setAdjustedForIme(z);
                }
            }
        }

        /* renamed from: -com_android_systemui_stackdivider_Divider$DockDividerVisibilityListener_lambda$2  reason: not valid java name */
        /* synthetic */ void m1352xc2b650e0(int i) {
            this.this$0.mView.notifyDockSideChanged(i);
        }

        public void onAdjustedForImeChanged(boolean z, long j) throws RemoteException {
            this.this$0.mView.post(new Runnable(this, z, j) { // from class: com.android.systemui.stackdivider.Divider.DockDividerVisibilityListener._void_onAdjustedForImeChanged_boolean_adjustedForIme_long_animDuration_LambdaImpl0
                private boolean val$adjustedForIme;
                private long val$animDuration;
                private DockDividerVisibilityListener val$this;

                {
                    this.val$this = this;
                    this.val$adjustedForIme = z;
                    this.val$animDuration = j;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.val$this.m1351xc2b650df(this.val$adjustedForIme, this.val$animDuration);
                }
            });
        }

        public void onDividerVisibilityChanged(boolean z) throws RemoteException {
            this.this$0.updateVisibility(z);
        }

        public void onDockSideChanged(int i) throws RemoteException {
            this.this$0.mView.post(new Runnable(this, i) { // from class: com.android.systemui.stackdivider.Divider.DockDividerVisibilityListener._void_onDockSideChanged_int_newDockSide_LambdaImpl0
                private int val$newDockSide;
                private DockDividerVisibilityListener val$this;

                {
                    this.val$this = this;
                    this.val$newDockSide = i;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.val$this.m1352xc2b650e0(this.val$newDockSide);
                }
            });
        }

        public void onDockedStackExistsChanged(boolean z) throws RemoteException {
            this.this$0.notifyDockedStackExistsChanged(z);
        }

        public void onDockedStackMinimizedChanged(boolean z, long j) throws RemoteException {
            this.this$0.updateMinimizedDockedStack(z, j);
        }
    }

    private void addDivider(Configuration configuration) {
        this.mView = (DividerView) LayoutInflater.from(this.mContext).inflate(2130968614, (ViewGroup) null);
        this.mView.setVisibility(this.mVisible ? 0 : 4);
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(17104929);
        boolean z = configuration.orientation == 2;
        int i = z ? dimensionPixelSize : -1;
        if (z) {
            dimensionPixelSize = -1;
        }
        this.mWindowManager.add(this.mView, i, dimensionPixelSize);
        this.mView.injectDependencies(this.mWindowManager, this.mDividerState);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyDockedStackExistsChanged(boolean z) {
        this.mView.post(new Runnable(this, z) { // from class: com.android.systemui.stackdivider.Divider.3
            final Divider this$0;
            final boolean val$exists;

            {
                this.this$0 = this;
                this.val$exists = z;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mForcedResizableController.notifyDockedStackExistsChanged(this.val$exists);
            }
        });
    }

    private void removeDivider() {
        this.mWindowManager.remove();
    }

    private void update(Configuration configuration) {
        removeDivider();
        addDivider(configuration);
        if (this.mMinimized) {
            this.mView.setMinimizedDockStack(true);
            updateTouchable();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateMinimizedDockedStack(boolean z, long j) {
        this.mView.post(new Runnable(this, z, j) { // from class: com.android.systemui.stackdivider.Divider.2
            final Divider this$0;
            final long val$animDuration;
            final boolean val$minimized;

            {
                this.this$0 = this;
                this.val$minimized = z;
                this.val$animDuration = j;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.mMinimized != this.val$minimized) {
                    this.this$0.mMinimized = this.val$minimized;
                    this.this$0.updateTouchable();
                    if (this.val$animDuration > 0) {
                        this.this$0.mView.setMinimizedDockStack(this.val$minimized, this.val$animDuration);
                    } else {
                        this.this$0.mView.setMinimizedDockStack(this.val$minimized);
                    }
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTouchable() {
        DividerWindowManager dividerWindowManager = this.mWindowManager;
        boolean z = false;
        if (!this.mMinimized) {
            z = !this.mAdjustedForIme;
        }
        dividerWindowManager.setTouchable(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateVisibility(boolean z) {
        this.mView.post(new Runnable(this, z) { // from class: com.android.systemui.stackdivider.Divider.1
            final Divider this$0;
            final boolean val$visible;

            {
                this.this$0 = this;
                this.val$visible = z;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.mVisible != this.val$visible) {
                    this.this$0.mVisible = this.val$visible;
                    this.this$0.mView.setVisibility(this.val$visible ? 0 : 4);
                    this.this$0.mView.setMinimizedDockStack(this.this$0.mMinimized);
                }
            }
        });
    }

    @Override // com.android.systemui.SystemUI
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("  mVisible=");
        printWriter.println(this.mVisible);
        printWriter.print("  mMinimized=");
        printWriter.println(this.mMinimized);
        printWriter.print("  mAdjustedForIme=");
        printWriter.println(this.mAdjustedForIme);
    }

    public DividerView getView() {
        return this.mView;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        update(configuration);
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mWindowManager = new DividerWindowManager(this.mContext);
        update(this.mContext.getResources().getConfiguration());
        putComponent(Divider.class, this);
        this.mDockDividerVisibilityListener = new DockDividerVisibilityListener(this);
        Recents.getSystemServices().registerDockedStackListener(this.mDockDividerVisibilityListener);
        this.mForcedResizableController = new ForcedResizableInfoActivityController(this.mContext);
    }
}
