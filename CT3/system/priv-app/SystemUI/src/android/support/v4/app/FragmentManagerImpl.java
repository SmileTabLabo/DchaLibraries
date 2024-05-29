package android.support.v4.app;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.v4.app.BackStackRecord;
import android.support.v4.app.FragmentManager;
import android.support.v4.os.BuildCompat;
import android.support.v4.util.DebugUtils;
import android.support.v4.util.LogWriter;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v4/app/FragmentManagerImpl.class */
public final class FragmentManagerImpl extends FragmentManager implements LayoutInflaterFactory {
    static final Interpolator ACCELERATE_CUBIC;
    static final Interpolator ACCELERATE_QUINT;
    static boolean DEBUG = false;
    static final Interpolator DECELERATE_CUBIC;
    static final Interpolator DECELERATE_QUINT;
    static final boolean HONEYCOMB;
    static Field sAnimationListenerField;
    ArrayList<Fragment> mActive;
    ArrayList<Fragment> mAdded;
    ArrayList<Integer> mAvailBackStackIndices;
    ArrayList<Integer> mAvailIndices;
    ArrayList<BackStackRecord> mBackStack;
    ArrayList<FragmentManager.OnBackStackChangedListener> mBackStackChangeListeners;
    ArrayList<BackStackRecord> mBackStackIndices;
    FragmentContainer mContainer;
    ArrayList<Fragment> mCreatedMenus;
    boolean mDestroyed;
    boolean mExecutingActions;
    boolean mHavePendingDeferredStart;
    FragmentHostCallback mHost;
    boolean mNeedMenuInvalidate;
    String mNoTransactionsBecause;
    Fragment mParent;
    ArrayList<Runnable> mPendingActions;
    boolean mStateSaved;
    Runnable[] mTmpActions;
    int mCurState = 0;
    Bundle mStateBundle = null;
    SparseArray<Parcelable> mStateArray = null;
    Runnable mExecCommit = new Runnable(this) { // from class: android.support.v4.app.FragmentManagerImpl.1
        final FragmentManagerImpl this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.execPendingActions();
        }
    };

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:android/support/v4/app/FragmentManagerImpl$AnimateOnHWLayerIfNeededListener.class */
    public static class AnimateOnHWLayerIfNeededListener implements Animation.AnimationListener {
        private Animation.AnimationListener mOrignalListener;
        private boolean mShouldRunOnHWLayer;
        private View mView;

        public AnimateOnHWLayerIfNeededListener(View view, Animation animation) {
            if (view == null || animation == null) {
                return;
            }
            this.mView = view;
        }

        public AnimateOnHWLayerIfNeededListener(View view, Animation animation, Animation.AnimationListener animationListener) {
            if (view == null || animation == null) {
                return;
            }
            this.mOrignalListener = animationListener;
            this.mView = view;
            this.mShouldRunOnHWLayer = true;
        }

        @Override // android.view.animation.Animation.AnimationListener
        @CallSuper
        public void onAnimationEnd(Animation animation) {
            if (this.mView != null && this.mShouldRunOnHWLayer) {
                if (ViewCompat.isAttachedToWindow(this.mView) || BuildCompat.isAtLeastN()) {
                    this.mView.post(new Runnable(this) { // from class: android.support.v4.app.FragmentManagerImpl.AnimateOnHWLayerIfNeededListener.1
                        final AnimateOnHWLayerIfNeededListener this$1;

                        {
                            this.this$1 = this;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            ViewCompat.setLayerType(this.this$1.mView, 0, null);
                        }
                    });
                } else {
                    ViewCompat.setLayerType(this.mView, 0, null);
                }
            }
            if (this.mOrignalListener != null) {
                this.mOrignalListener.onAnimationEnd(animation);
            }
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationRepeat(Animation animation) {
            if (this.mOrignalListener != null) {
                this.mOrignalListener.onAnimationRepeat(animation);
            }
        }

        @Override // android.view.animation.Animation.AnimationListener
        @CallSuper
        public void onAnimationStart(Animation animation) {
            if (this.mOrignalListener != null) {
                this.mOrignalListener.onAnimationStart(animation);
            }
        }
    }

    /* loaded from: a.zip:android/support/v4/app/FragmentManagerImpl$FragmentTag.class */
    static class FragmentTag {
        public static final int[] Fragment = {16842755, 16842960, 16842961};

        FragmentTag() {
        }
    }

    static {
        boolean z = false;
        if (Build.VERSION.SDK_INT >= 11) {
            z = true;
        }
        HONEYCOMB = z;
        sAnimationListenerField = null;
        DECELERATE_QUINT = new DecelerateInterpolator(2.5f);
        DECELERATE_CUBIC = new DecelerateInterpolator(1.5f);
        ACCELERATE_QUINT = new AccelerateInterpolator(2.5f);
        ACCELERATE_CUBIC = new AccelerateInterpolator(1.5f);
    }

    private void checkStateLoss() {
        if (this.mStateSaved) {
            throw new IllegalStateException("Can not perform this action after onSaveInstanceState");
        }
        if (this.mNoTransactionsBecause != null) {
            throw new IllegalStateException("Can not perform this action inside of " + this.mNoTransactionsBecause);
        }
    }

    static Animation makeFadeAnimation(Context context, float f, float f2) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(f, f2);
        alphaAnimation.setInterpolator(DECELERATE_CUBIC);
        alphaAnimation.setDuration(220L);
        return alphaAnimation;
    }

    static Animation makeOpenCloseAnimation(Context context, float f, float f2, float f3, float f4) {
        AnimationSet animationSet = new AnimationSet(false);
        ScaleAnimation scaleAnimation = new ScaleAnimation(f, f2, f, f2, 1, 0.5f, 1, 0.5f);
        scaleAnimation.setInterpolator(DECELERATE_QUINT);
        scaleAnimation.setDuration(220L);
        animationSet.addAnimation(scaleAnimation);
        AlphaAnimation alphaAnimation = new AlphaAnimation(f3, f4);
        alphaAnimation.setInterpolator(DECELERATE_CUBIC);
        alphaAnimation.setDuration(220L);
        animationSet.addAnimation(alphaAnimation);
        return animationSet;
    }

    static boolean modifiesAlpha(Animation animation) {
        if (animation instanceof AlphaAnimation) {
            return true;
        }
        if (animation instanceof AnimationSet) {
            List<Animation> animations = ((AnimationSet) animation).getAnimations();
            for (int i = 0; i < animations.size(); i++) {
                if (animations.get(i) instanceof AlphaAnimation) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public static int reverseTransit(int i) {
        int i2;
        switch (i) {
            case 4097:
                i2 = 8194;
                break;
            case 4099:
                i2 = 4099;
                break;
            case 8194:
                i2 = 4097;
                break;
            default:
                i2 = 0;
                break;
        }
        return i2;
    }

    private void setHWLayerAnimListenerIfAlpha(View view, Animation animation) {
        if (view == null || animation == null || !shouldRunOnHWLayer(view, animation)) {
            return;
        }
        Animation.AnimationListener animationListener = null;
        try {
            if (sAnimationListenerField == null) {
                sAnimationListenerField = Animation.class.getDeclaredField("mListener");
                sAnimationListenerField.setAccessible(true);
            }
            animationListener = (Animation.AnimationListener) sAnimationListenerField.get(animation);
        } catch (IllegalAccessException e) {
            Log.e("FragmentManager", "Cannot access Animation's mListener field", e);
        } catch (NoSuchFieldException e2) {
            Log.e("FragmentManager", "No field with the name mListener is found in Animation class", e2);
        }
        ViewCompat.setLayerType(view, 2, null);
        animation.setAnimationListener(new AnimateOnHWLayerIfNeededListener(view, animation, animationListener));
    }

    static boolean shouldRunOnHWLayer(View view, Animation animation) {
        boolean z = false;
        if (Build.VERSION.SDK_INT >= 19) {
            z = false;
            if (ViewCompat.getLayerType(view) == 0) {
                z = false;
                if (ViewCompat.hasOverlappingRendering(view)) {
                    z = modifiesAlpha(animation);
                }
            }
        }
        return z;
    }

    private void throwException(RuntimeException runtimeException) {
        Log.e("FragmentManager", runtimeException.getMessage());
        Log.e("FragmentManager", "Activity state:");
        PrintWriter printWriter = new PrintWriter(new LogWriter("FragmentManager"));
        if (this.mHost != null) {
            try {
                this.mHost.onDump("  ", null, printWriter, new String[0]);
            } catch (Exception e) {
                Log.e("FragmentManager", "Failed dumping state", e);
            }
        } else {
            try {
                dump("  ", null, printWriter, new String[0]);
            } catch (Exception e2) {
                Log.e("FragmentManager", "Failed dumping state", e2);
            }
        }
        throw runtimeException;
    }

    public static int transitToStyleIndex(int i, boolean z) {
        int i2;
        switch (i) {
            case 4097:
                if (!z) {
                    i2 = 2;
                    break;
                } else {
                    i2 = 1;
                    break;
                }
            case 4099:
                if (!z) {
                    i2 = 6;
                    break;
                } else {
                    i2 = 5;
                    break;
                }
            case 8194:
                if (!z) {
                    i2 = 4;
                    break;
                } else {
                    i2 = 3;
                    break;
                }
            default:
                i2 = -1;
                break;
        }
        return i2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addBackStackState(BackStackRecord backStackRecord) {
        if (this.mBackStack == null) {
            this.mBackStack = new ArrayList<>();
        }
        this.mBackStack.add(backStackRecord);
        reportBackStackChanged();
    }

    public void addFragment(Fragment fragment, boolean z) {
        if (this.mAdded == null) {
            this.mAdded = new ArrayList<>();
        }
        if (DEBUG) {
            Log.v("FragmentManager", "add: " + fragment);
        }
        makeActive(fragment);
        if (fragment.mDetached) {
            return;
        }
        if (this.mAdded.contains(fragment)) {
            throw new IllegalStateException("Fragment already added: " + fragment);
        }
        this.mAdded.add(fragment);
        fragment.mAdded = true;
        fragment.mRemoving = false;
        if (fragment.mHasMenu && fragment.mMenuVisible) {
            this.mNeedMenuInvalidate = true;
        }
        if (z) {
            moveToState(fragment);
        }
    }

    public int allocBackStackIndex(BackStackRecord backStackRecord) {
        synchronized (this) {
            if (this.mAvailBackStackIndices != null && this.mAvailBackStackIndices.size() > 0) {
                int intValue = this.mAvailBackStackIndices.remove(this.mAvailBackStackIndices.size() - 1).intValue();
                if (DEBUG) {
                    Log.v("FragmentManager", "Adding back stack index " + intValue + " with " + backStackRecord);
                }
                this.mBackStackIndices.set(intValue, backStackRecord);
                return intValue;
            }
            if (this.mBackStackIndices == null) {
                this.mBackStackIndices = new ArrayList<>();
            }
            int size = this.mBackStackIndices.size();
            if (DEBUG) {
                Log.v("FragmentManager", "Setting back stack index " + size + " to " + backStackRecord);
            }
            this.mBackStackIndices.add(backStackRecord);
            return size;
        }
    }

    public void attachController(FragmentHostCallback fragmentHostCallback, FragmentContainer fragmentContainer, Fragment fragment) {
        if (this.mHost != null) {
            throw new IllegalStateException("Already attached");
        }
        this.mHost = fragmentHostCallback;
        this.mContainer = fragmentContainer;
        this.mParent = fragment;
    }

    public void attachFragment(Fragment fragment, int i, int i2) {
        if (DEBUG) {
            Log.v("FragmentManager", "attach: " + fragment);
        }
        if (fragment.mDetached) {
            fragment.mDetached = false;
            if (fragment.mAdded) {
                return;
            }
            if (this.mAdded == null) {
                this.mAdded = new ArrayList<>();
            }
            if (this.mAdded.contains(fragment)) {
                throw new IllegalStateException("Fragment already added: " + fragment);
            }
            if (DEBUG) {
                Log.v("FragmentManager", "add from attach: " + fragment);
            }
            this.mAdded.add(fragment);
            fragment.mAdded = true;
            if (fragment.mHasMenu && fragment.mMenuVisible) {
                this.mNeedMenuInvalidate = true;
            }
            moveToState(fragment, this.mCurState, i, i2, false);
        }
    }

    @Override // android.support.v4.app.FragmentManager
    public FragmentTransaction beginTransaction() {
        return new BackStackRecord(this);
    }

    public void detachFragment(Fragment fragment, int i, int i2) {
        if (DEBUG) {
            Log.v("FragmentManager", "detach: " + fragment);
        }
        if (fragment.mDetached) {
            return;
        }
        fragment.mDetached = true;
        if (fragment.mAdded) {
            if (this.mAdded != null) {
                if (DEBUG) {
                    Log.v("FragmentManager", "remove from detach: " + fragment);
                }
                this.mAdded.remove(fragment);
            }
            if (fragment.mHasMenu && fragment.mMenuVisible) {
                this.mNeedMenuInvalidate = true;
            }
            fragment.mAdded = false;
            moveToState(fragment, 1, i, i2, false);
        }
    }

    public void dispatchActivityCreated() {
        this.mStateSaved = false;
        moveToState(2, false);
    }

    public void dispatchConfigurationChanged(Configuration configuration) {
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment fragment = this.mAdded.get(i);
                if (fragment != null) {
                    fragment.performConfigurationChanged(configuration);
                }
            }
        }
    }

    public boolean dispatchContextItemSelected(MenuItem menuItem) {
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment fragment = this.mAdded.get(i);
                if (fragment != null && fragment.performContextItemSelected(menuItem)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public void dispatchCreate() {
        this.mStateSaved = false;
        moveToState(1, false);
    }

    public boolean dispatchCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        boolean z = false;
        boolean z2 = false;
        ArrayList<Fragment> arrayList = null;
        ArrayList<Fragment> arrayList2 = null;
        if (this.mAdded != null) {
            int i = 0;
            while (true) {
                arrayList = arrayList2;
                z = z2;
                if (i >= this.mAdded.size()) {
                    break;
                }
                Fragment fragment = this.mAdded.get(i);
                ArrayList<Fragment> arrayList3 = arrayList2;
                boolean z3 = z2;
                if (fragment != null) {
                    arrayList3 = arrayList2;
                    z3 = z2;
                    if (fragment.performCreateOptionsMenu(menu, menuInflater)) {
                        z3 = true;
                        arrayList3 = arrayList2;
                        if (arrayList2 == null) {
                            arrayList3 = new ArrayList<>();
                        }
                        arrayList3.add(fragment);
                    }
                }
                i++;
                arrayList2 = arrayList3;
                z2 = z3;
            }
        }
        if (this.mCreatedMenus != null) {
            for (int i2 = 0; i2 < this.mCreatedMenus.size(); i2++) {
                Fragment fragment2 = this.mCreatedMenus.get(i2);
                if (arrayList == null || !arrayList.contains(fragment2)) {
                    fragment2.onDestroyOptionsMenu();
                }
            }
        }
        this.mCreatedMenus = arrayList;
        return z;
    }

    public void dispatchDestroy() {
        this.mDestroyed = true;
        execPendingActions();
        moveToState(0, false);
        this.mHost = null;
        this.mContainer = null;
        this.mParent = null;
    }

    public void dispatchDestroyView() {
        moveToState(1, false);
    }

    public void dispatchLowMemory() {
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment fragment = this.mAdded.get(i);
                if (fragment != null) {
                    fragment.performLowMemory();
                }
            }
        }
    }

    public void dispatchMultiWindowModeChanged(boolean z) {
        if (this.mAdded == null) {
            return;
        }
        for (int size = this.mAdded.size() - 1; size >= 0; size--) {
            Fragment fragment = this.mAdded.get(size);
            if (fragment != null) {
                fragment.performMultiWindowModeChanged(z);
            }
        }
    }

    public boolean dispatchOptionsItemSelected(MenuItem menuItem) {
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment fragment = this.mAdded.get(i);
                if (fragment != null && fragment.performOptionsItemSelected(menuItem)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public void dispatchOptionsMenuClosed(Menu menu) {
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment fragment = this.mAdded.get(i);
                if (fragment != null) {
                    fragment.performOptionsMenuClosed(menu);
                }
            }
        }
    }

    public void dispatchPause() {
        moveToState(4, false);
    }

    public void dispatchPictureInPictureModeChanged(boolean z) {
        if (this.mAdded == null) {
            return;
        }
        for (int size = this.mAdded.size() - 1; size >= 0; size--) {
            Fragment fragment = this.mAdded.get(size);
            if (fragment != null) {
                fragment.performPictureInPictureModeChanged(z);
            }
        }
    }

    public boolean dispatchPrepareOptionsMenu(Menu menu) {
        boolean z = false;
        boolean z2 = false;
        if (this.mAdded != null) {
            int i = 0;
            while (true) {
                z = z2;
                if (i >= this.mAdded.size()) {
                    break;
                }
                Fragment fragment = this.mAdded.get(i);
                boolean z3 = z2;
                if (fragment != null) {
                    z3 = z2;
                    if (fragment.performPrepareOptionsMenu(menu)) {
                        z3 = true;
                    }
                }
                i++;
                z2 = z3;
            }
        }
        return z;
    }

    public void dispatchReallyStop() {
        moveToState(2, false);
    }

    public void dispatchResume() {
        this.mStateSaved = false;
        moveToState(5, false);
    }

    public void dispatchStart() {
        this.mStateSaved = false;
        moveToState(4, false);
    }

    public void dispatchStop() {
        this.mStateSaved = true;
        moveToState(3, false);
    }

    void doPendingDeferredStart() {
        if (this.mHavePendingDeferredStart) {
            boolean z = false;
            int i = 0;
            while (i < this.mActive.size()) {
                Fragment fragment = this.mActive.get(i);
                boolean z2 = z;
                if (fragment != null) {
                    z2 = z;
                    if (fragment.mLoaderManager != null) {
                        z2 = z | fragment.mLoaderManager.hasRunningLoaders();
                    }
                }
                i++;
                z = z2;
            }
            if (z) {
                return;
            }
            this.mHavePendingDeferredStart = false;
            startPendingDeferredFragments();
        }
    }

    @Override // android.support.v4.app.FragmentManager
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        int size;
        int size2;
        int size3;
        int size4;
        int size5;
        int size6;
        String str2 = str + "    ";
        if (this.mActive != null && (size6 = this.mActive.size()) > 0) {
            printWriter.print(str);
            printWriter.print("Active Fragments in ");
            printWriter.print(Integer.toHexString(System.identityHashCode(this)));
            printWriter.println(":");
            for (int i = 0; i < size6; i++) {
                Fragment fragment = this.mActive.get(i);
                printWriter.print(str);
                printWriter.print("  #");
                printWriter.print(i);
                printWriter.print(": ");
                printWriter.println(fragment);
                if (fragment != null) {
                    fragment.dump(str2, fileDescriptor, printWriter, strArr);
                }
            }
        }
        if (this.mAdded != null && (size5 = this.mAdded.size()) > 0) {
            printWriter.print(str);
            printWriter.println("Added Fragments:");
            for (int i2 = 0; i2 < size5; i2++) {
                printWriter.print(str);
                printWriter.print("  #");
                printWriter.print(i2);
                printWriter.print(": ");
                printWriter.println(this.mAdded.get(i2).toString());
            }
        }
        if (this.mCreatedMenus != null && (size4 = this.mCreatedMenus.size()) > 0) {
            printWriter.print(str);
            printWriter.println("Fragments Created Menus:");
            for (int i3 = 0; i3 < size4; i3++) {
                printWriter.print(str);
                printWriter.print("  #");
                printWriter.print(i3);
                printWriter.print(": ");
                printWriter.println(this.mCreatedMenus.get(i3).toString());
            }
        }
        if (this.mBackStack != null && (size3 = this.mBackStack.size()) > 0) {
            printWriter.print(str);
            printWriter.println("Back Stack:");
            for (int i4 = 0; i4 < size3; i4++) {
                BackStackRecord backStackRecord = this.mBackStack.get(i4);
                printWriter.print(str);
                printWriter.print("  #");
                printWriter.print(i4);
                printWriter.print(": ");
                printWriter.println(backStackRecord.toString());
                backStackRecord.dump(str2, fileDescriptor, printWriter, strArr);
            }
        }
        synchronized (this) {
            if (this.mBackStackIndices != null && (size2 = this.mBackStackIndices.size()) > 0) {
                printWriter.print(str);
                printWriter.println("Back Stack Indices:");
                for (int i5 = 0; i5 < size2; i5++) {
                    printWriter.print(str);
                    printWriter.print("  #");
                    printWriter.print(i5);
                    printWriter.print(": ");
                    printWriter.println((BackStackRecord) this.mBackStackIndices.get(i5));
                }
            }
            if (this.mAvailBackStackIndices != null && this.mAvailBackStackIndices.size() > 0) {
                printWriter.print(str);
                printWriter.print("mAvailBackStackIndices: ");
                printWriter.println(Arrays.toString(this.mAvailBackStackIndices.toArray()));
            }
        }
        if (this.mPendingActions != null && (size = this.mPendingActions.size()) > 0) {
            printWriter.print(str);
            printWriter.println("Pending Actions:");
            for (int i6 = 0; i6 < size; i6++) {
                printWriter.print(str);
                printWriter.print("  #");
                printWriter.print(i6);
                printWriter.print(": ");
                printWriter.println((Runnable) this.mPendingActions.get(i6));
            }
        }
        printWriter.print(str);
        printWriter.println("FragmentManager misc state:");
        printWriter.print(str);
        printWriter.print("  mHost=");
        printWriter.println(this.mHost);
        printWriter.print(str);
        printWriter.print("  mContainer=");
        printWriter.println(this.mContainer);
        if (this.mParent != null) {
            printWriter.print(str);
            printWriter.print("  mParent=");
            printWriter.println(this.mParent);
        }
        printWriter.print(str);
        printWriter.print("  mCurState=");
        printWriter.print(this.mCurState);
        printWriter.print(" mStateSaved=");
        printWriter.print(this.mStateSaved);
        printWriter.print(" mDestroyed=");
        printWriter.println(this.mDestroyed);
        if (this.mNeedMenuInvalidate) {
            printWriter.print(str);
            printWriter.print("  mNeedMenuInvalidate=");
            printWriter.println(this.mNeedMenuInvalidate);
        }
        if (this.mNoTransactionsBecause != null) {
            printWriter.print(str);
            printWriter.print("  mNoTransactionsBecause=");
            printWriter.println(this.mNoTransactionsBecause);
        }
        if (this.mAvailIndices == null || this.mAvailIndices.size() <= 0) {
            return;
        }
        printWriter.print(str);
        printWriter.print("  mAvailIndices: ");
        printWriter.println(Arrays.toString(this.mAvailIndices.toArray()));
    }

    public void enqueueAction(Runnable runnable, boolean z) {
        if (!z) {
            checkStateLoss();
        }
        synchronized (this) {
            if (this.mDestroyed || this.mHost == null) {
                throw new IllegalStateException("Activity has been destroyed");
            }
            if (this.mPendingActions == null) {
                this.mPendingActions = new ArrayList<>();
            }
            this.mPendingActions.add(runnable);
            if (this.mPendingActions.size() == 1) {
                this.mHost.getHandler().removeCallbacks(this.mExecCommit);
                this.mHost.getHandler().post(this.mExecCommit);
            }
        }
    }

    public boolean execPendingActions() {
        boolean z;
        int size;
        if (this.mExecutingActions) {
            throw new IllegalStateException("FragmentManager is already executing transactions");
        }
        if (Looper.myLooper() != this.mHost.getHandler().getLooper()) {
            throw new IllegalStateException("Must be called from main thread of fragment host");
        }
        boolean z2 = false;
        while (true) {
            z = z2;
            synchronized (this) {
                if (this.mPendingActions == null || this.mPendingActions.size() == 0) {
                    break;
                }
                size = this.mPendingActions.size();
                if (this.mTmpActions == null || this.mTmpActions.length < size) {
                    this.mTmpActions = new Runnable[size];
                }
                this.mPendingActions.toArray(this.mTmpActions);
                this.mPendingActions.clear();
                this.mHost.getHandler().removeCallbacks(this.mExecCommit);
            }
            this.mExecutingActions = true;
            for (int i = 0; i < size; i++) {
                this.mTmpActions[i].run();
                this.mTmpActions[i] = null;
            }
            this.mExecutingActions = false;
            z2 = true;
        }
        doPendingDeferredStart();
        return z;
    }

    @Override // android.support.v4.app.FragmentManager
    public boolean executePendingTransactions() {
        return execPendingActions();
    }

    public Fragment findFragmentById(int i) {
        if (this.mAdded != null) {
            for (int size = this.mAdded.size() - 1; size >= 0; size--) {
                Fragment fragment = this.mAdded.get(size);
                if (fragment != null && fragment.mFragmentId == i) {
                    return fragment;
                }
            }
        }
        if (this.mActive != null) {
            for (int size2 = this.mActive.size() - 1; size2 >= 0; size2--) {
                Fragment fragment2 = this.mActive.get(size2);
                if (fragment2 != null && fragment2.mFragmentId == i) {
                    return fragment2;
                }
            }
            return null;
        }
        return null;
    }

    @Override // android.support.v4.app.FragmentManager
    public Fragment findFragmentByTag(String str) {
        if (this.mAdded != null && str != null) {
            for (int size = this.mAdded.size() - 1; size >= 0; size--) {
                Fragment fragment = this.mAdded.get(size);
                if (fragment != null && str.equals(fragment.mTag)) {
                    return fragment;
                }
            }
        }
        if (this.mActive == null || str == null) {
            return null;
        }
        for (int size2 = this.mActive.size() - 1; size2 >= 0; size2--) {
            Fragment fragment2 = this.mActive.get(size2);
            if (fragment2 != null && str.equals(fragment2.mTag)) {
                return fragment2;
            }
        }
        return null;
    }

    public Fragment findFragmentByWho(String str) {
        Fragment findFragmentByWho;
        if (this.mActive == null || str == null) {
            return null;
        }
        for (int size = this.mActive.size() - 1; size >= 0; size--) {
            Fragment fragment = this.mActive.get(size);
            if (fragment != null && (findFragmentByWho = fragment.findFragmentByWho(str)) != null) {
                return findFragmentByWho;
            }
        }
        return null;
    }

    public void freeBackStackIndex(int i) {
        synchronized (this) {
            this.mBackStackIndices.set(i, null);
            if (this.mAvailBackStackIndices == null) {
                this.mAvailBackStackIndices = new ArrayList<>();
            }
            if (DEBUG) {
                Log.v("FragmentManager", "Freeing back stack index " + i);
            }
            this.mAvailBackStackIndices.add(Integer.valueOf(i));
        }
    }

    public Fragment getFragment(Bundle bundle, String str) {
        int i = bundle.getInt(str, -1);
        if (i == -1) {
            return null;
        }
        if (i >= this.mActive.size()) {
            throwException(new IllegalStateException("Fragment no longer exists for key " + str + ": index " + i));
        }
        Fragment fragment = this.mActive.get(i);
        if (fragment == null) {
            throwException(new IllegalStateException("Fragment no longer exists for key " + str + ": index " + i));
        }
        return fragment;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LayoutInflaterFactory getLayoutInflaterFactory() {
        return this;
    }

    public void hideFragment(Fragment fragment, int i, int i2) {
        if (DEBUG) {
            Log.v("FragmentManager", "hide: " + fragment);
        }
        if (fragment.mHidden) {
            return;
        }
        fragment.mHidden = true;
        if (fragment.mView != null) {
            Animation loadAnimation = loadAnimation(fragment, i, false, i2);
            if (loadAnimation != null) {
                setHWLayerAnimListenerIfAlpha(fragment.mView, loadAnimation);
                fragment.mView.startAnimation(loadAnimation);
            }
            fragment.mView.setVisibility(8);
        }
        if (fragment.mAdded && fragment.mHasMenu && fragment.mMenuVisible) {
            this.mNeedMenuInvalidate = true;
        }
        fragment.onHiddenChanged(true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isStateAtLeast(int i) {
        return this.mCurState >= i;
    }

    Animation loadAnimation(Fragment fragment, int i, boolean z, int i2) {
        int transitToStyleIndex;
        Animation loadAnimation;
        Animation onCreateAnimation = fragment.onCreateAnimation(i, z, fragment.mNextAnim);
        if (onCreateAnimation != null) {
            return onCreateAnimation;
        }
        if (fragment.mNextAnim == 0 || (loadAnimation = AnimationUtils.loadAnimation(this.mHost.getContext(), fragment.mNextAnim)) == null) {
            if (i != 0 && (transitToStyleIndex = transitToStyleIndex(i, z)) >= 0) {
                switch (transitToStyleIndex) {
                    case 1:
                        return makeOpenCloseAnimation(this.mHost.getContext(), 1.125f, 1.0f, 0.0f, 1.0f);
                    case 2:
                        return makeOpenCloseAnimation(this.mHost.getContext(), 1.0f, 0.975f, 1.0f, 0.0f);
                    case 3:
                        return makeOpenCloseAnimation(this.mHost.getContext(), 0.975f, 1.0f, 0.0f, 1.0f);
                    case 4:
                        return makeOpenCloseAnimation(this.mHost.getContext(), 1.0f, 1.075f, 1.0f, 0.0f);
                    case 5:
                        return makeFadeAnimation(this.mHost.getContext(), 0.0f, 1.0f);
                    case 6:
                        return makeFadeAnimation(this.mHost.getContext(), 1.0f, 0.0f);
                    default:
                        int i3 = i2;
                        if (i2 == 0) {
                            i3 = i2;
                            if (this.mHost.onHasWindowAnimations()) {
                                i3 = this.mHost.onGetWindowAnimations();
                            }
                        }
                        return i3 == 0 ? null : null;
                }
            }
            return null;
        }
        return loadAnimation;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void makeActive(Fragment fragment) {
        if (fragment.mIndex >= 0) {
            return;
        }
        if (this.mAvailIndices == null || this.mAvailIndices.size() <= 0) {
            if (this.mActive == null) {
                this.mActive = new ArrayList<>();
            }
            fragment.setIndex(this.mActive.size(), this.mParent);
            this.mActive.add(fragment);
        } else {
            fragment.setIndex(this.mAvailIndices.remove(this.mAvailIndices.size() - 1).intValue(), this.mParent);
            this.mActive.set(fragment.mIndex, fragment);
        }
        if (DEBUG) {
            Log.v("FragmentManager", "Allocated fragment index " + fragment);
        }
    }

    void makeInactive(Fragment fragment) {
        if (fragment.mIndex < 0) {
            return;
        }
        if (DEBUG) {
            Log.v("FragmentManager", "Freeing fragment index " + fragment);
        }
        this.mActive.set(fragment.mIndex, null);
        if (this.mAvailIndices == null) {
            this.mAvailIndices = new ArrayList<>();
        }
        this.mAvailIndices.add(Integer.valueOf(fragment.mIndex));
        this.mHost.inactivateFragment(fragment.mWho);
        fragment.initState();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void moveToState(int i, int i2, int i3, boolean z) {
        if (this.mHost == null && i != 0) {
            throw new IllegalStateException("No host");
        }
        if (z || this.mCurState != i) {
            this.mCurState = i;
            if (this.mActive != null) {
                boolean z2 = false;
                int i4 = 0;
                while (i4 < this.mActive.size()) {
                    Fragment fragment = this.mActive.get(i4);
                    boolean z3 = z2;
                    if (fragment != null) {
                        moveToState(fragment, i, i2, i3, false);
                        z3 = z2;
                        if (fragment.mLoaderManager != null) {
                            z3 = z2 | fragment.mLoaderManager.hasRunningLoaders();
                        }
                    }
                    i4++;
                    z2 = z3;
                }
                if (!z2) {
                    startPendingDeferredFragments();
                }
                if (this.mNeedMenuInvalidate && this.mHost != null && this.mCurState == 5) {
                    this.mHost.onSupportInvalidateOptionsMenu();
                    this.mNeedMenuInvalidate = false;
                }
            }
        }
    }

    void moveToState(int i, boolean z) {
        moveToState(i, 0, 0, z);
    }

    void moveToState(Fragment fragment) {
        moveToState(fragment, this.mCurState, 0, 0, false);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Code restructure failed: missing block: B:5:0x000e, code lost:
        if (r9.mDetached != false) goto L211;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void moveToState(Fragment fragment, int i, int i2, int i3, boolean z) {
        int i4;
        int i5;
        String str;
        if (fragment.mAdded) {
            i4 = i;
        }
        i4 = i;
        if (i > 1) {
            i4 = 1;
        }
        int i6 = i4;
        if (fragment.mRemoving) {
            i6 = i4;
            if (i4 > fragment.mState) {
                i6 = fragment.mState;
            }
        }
        int i7 = i6;
        if (fragment.mDeferStart) {
            i7 = i6;
            if (fragment.mState < 4) {
                i7 = i6;
                if (i6 > 3) {
                    i7 = 3;
                }
            }
        }
        if (fragment.mState >= i7) {
            i5 = i7;
            if (fragment.mState > i7) {
                switch (fragment.mState) {
                    case 5:
                        if (i7 < 5) {
                            if (DEBUG) {
                                Log.v("FragmentManager", "movefrom RESUMED: " + fragment);
                            }
                            fragment.performPause();
                        }
                    case 4:
                        if (i7 < 4) {
                            if (DEBUG) {
                                Log.v("FragmentManager", "movefrom STARTED: " + fragment);
                            }
                            fragment.performStop();
                        }
                    case 3:
                        if (i7 < 3) {
                            if (DEBUG) {
                                Log.v("FragmentManager", "movefrom STOPPED: " + fragment);
                            }
                            fragment.performReallyStop();
                        }
                    case 2:
                        if (i7 < 2) {
                            if (DEBUG) {
                                Log.v("FragmentManager", "movefrom ACTIVITY_CREATED: " + fragment);
                            }
                            if (fragment.mView != null && this.mHost.onShouldSaveFragmentState(fragment) && fragment.mSavedViewState == null) {
                                saveFragmentViewState(fragment);
                            }
                            fragment.performDestroyView();
                            if (fragment.mView != null && fragment.mContainer != null) {
                                Animation animation = null;
                                if (this.mCurState > 0) {
                                    animation = this.mDestroyed ? null : loadAnimation(fragment, i2, false, i3);
                                }
                                if (animation != null) {
                                    fragment.mAnimatingAway = fragment.mView;
                                    fragment.mStateAfterAnimating = i7;
                                    animation.setAnimationListener(new AnimateOnHWLayerIfNeededListener(this, fragment.mView, animation, fragment) { // from class: android.support.v4.app.FragmentManagerImpl.5
                                        final FragmentManagerImpl this$0;
                                        final Fragment val$fragment;

                                        {
                                            this.this$0 = this;
                                            this.val$fragment = fragment;
                                        }

                                        @Override // android.support.v4.app.FragmentManagerImpl.AnimateOnHWLayerIfNeededListener, android.view.animation.Animation.AnimationListener
                                        public void onAnimationEnd(Animation animation2) {
                                            super.onAnimationEnd(animation2);
                                            if (this.val$fragment.mAnimatingAway != null) {
                                                this.val$fragment.mAnimatingAway = null;
                                                this.this$0.moveToState(this.val$fragment, this.val$fragment.mStateAfterAnimating, 0, 0, false);
                                            }
                                        }
                                    });
                                    fragment.mView.startAnimation(animation);
                                }
                                fragment.mContainer.removeView(fragment.mView);
                            }
                            fragment.mContainer = null;
                            fragment.mView = null;
                            fragment.mInnerView = null;
                        }
                        break;
                    case 1:
                        i5 = i7;
                        if (i7 < 1) {
                            if (this.mDestroyed && fragment.mAnimatingAway != null) {
                                View view = fragment.mAnimatingAway;
                                fragment.mAnimatingAway = null;
                                view.clearAnimation();
                            }
                            if (fragment.mAnimatingAway == null) {
                                if (DEBUG) {
                                    Log.v("FragmentManager", "movefrom CREATED: " + fragment);
                                }
                                if (fragment.mRetaining) {
                                    fragment.mState = 0;
                                } else {
                                    fragment.performDestroy();
                                }
                                fragment.performDetach();
                                i5 = i7;
                                if (!z) {
                                    if (!fragment.mRetaining) {
                                        makeInactive(fragment);
                                        i5 = i7;
                                        break;
                                    } else {
                                        fragment.mHost = null;
                                        fragment.mParentFragment = null;
                                        fragment.mFragmentManager = null;
                                        i5 = i7;
                                        break;
                                    }
                                }
                            } else {
                                fragment.mStateAfterAnimating = i7;
                                i5 = 1;
                                break;
                            }
                        }
                        break;
                    default:
                        i5 = i7;
                        break;
                }
            }
        } else if (fragment.mFromLayout && !fragment.mInLayout) {
            return;
        } else {
            if (fragment.mAnimatingAway != null) {
                fragment.mAnimatingAway = null;
                moveToState(fragment, fragment.mStateAfterAnimating, 0, 0, true);
            }
            int i8 = i7;
            int i9 = i7;
            int i10 = i7;
            int i11 = i7;
            switch (fragment.mState) {
                case 0:
                    if (DEBUG) {
                        Log.v("FragmentManager", "moveto CREATED: " + fragment);
                    }
                    int i12 = i7;
                    if (fragment.mSavedFragmentState != null) {
                        fragment.mSavedFragmentState.setClassLoader(this.mHost.getContext().getClassLoader());
                        fragment.mSavedViewState = fragment.mSavedFragmentState.getSparseParcelableArray("android:view_state");
                        fragment.mTarget = getFragment(fragment.mSavedFragmentState, "android:target_state");
                        if (fragment.mTarget != null) {
                            fragment.mTargetRequestCode = fragment.mSavedFragmentState.getInt("android:target_req_state", 0);
                        }
                        fragment.mUserVisibleHint = fragment.mSavedFragmentState.getBoolean("android:user_visible_hint", true);
                        i12 = i7;
                        if (!fragment.mUserVisibleHint) {
                            fragment.mDeferStart = true;
                            i12 = i7;
                            if (i7 > 3) {
                                i12 = 3;
                            }
                        }
                    }
                    fragment.mHost = this.mHost;
                    fragment.mParentFragment = this.mParent;
                    fragment.mFragmentManager = this.mParent != null ? this.mParent.mChildFragmentManager : this.mHost.getFragmentManagerImpl();
                    fragment.mCalled = false;
                    fragment.onAttach(this.mHost.getContext());
                    if (!fragment.mCalled) {
                        throw new SuperNotCalledException("Fragment " + fragment + " did not call through to super.onAttach()");
                    }
                    if (fragment.mParentFragment == null) {
                        this.mHost.onAttachFragment(fragment);
                    } else {
                        fragment.mParentFragment.onAttachFragment(fragment);
                    }
                    if (fragment.mRetaining) {
                        fragment.restoreChildFragmentState(fragment.mSavedFragmentState);
                        fragment.mState = 1;
                    } else {
                        fragment.performCreate(fragment.mSavedFragmentState);
                    }
                    fragment.mRetaining = false;
                    i8 = i12;
                    if (fragment.mFromLayout) {
                        fragment.mView = fragment.performCreateView(fragment.getLayoutInflater(fragment.mSavedFragmentState), null, fragment.mSavedFragmentState);
                        if (fragment.mView != null) {
                            fragment.mInnerView = fragment.mView;
                            if (Build.VERSION.SDK_INT >= 11) {
                                ViewCompat.setSaveFromParentEnabled(fragment.mView, false);
                            } else {
                                fragment.mView = NoSaveStateFrameLayout.wrap(fragment.mView);
                            }
                            if (fragment.mHidden) {
                                fragment.mView.setVisibility(8);
                            }
                            fragment.onViewCreated(fragment.mView, fragment.mSavedFragmentState);
                            i8 = i12;
                        } else {
                            fragment.mInnerView = null;
                            i8 = i12;
                        }
                    }
                case 1:
                    i9 = i8;
                    if (i8 > 1) {
                        if (DEBUG) {
                            Log.v("FragmentManager", "moveto ACTIVITY_CREATED: " + fragment);
                        }
                        if (!fragment.mFromLayout) {
                            ViewGroup viewGroup = null;
                            if (fragment.mContainerId != 0) {
                                if (fragment.mContainerId == -1) {
                                    throwException(new IllegalArgumentException("Cannot create fragment " + fragment + " for a container view with no id"));
                                }
                                ViewGroup viewGroup2 = (ViewGroup) this.mContainer.onFindViewById(fragment.mContainerId);
                                viewGroup = viewGroup2;
                                if (viewGroup2 == null) {
                                    if (fragment.mRestored) {
                                        viewGroup = viewGroup2;
                                    } else {
                                        try {
                                            str = fragment.getResources().getResourceName(fragment.mContainerId);
                                        } catch (Resources.NotFoundException e) {
                                            str = "unknown";
                                        }
                                        throwException(new IllegalArgumentException("No view found for id 0x" + Integer.toHexString(fragment.mContainerId) + " (" + str + ") for fragment " + fragment));
                                        viewGroup = viewGroup2;
                                    }
                                }
                            }
                            fragment.mContainer = viewGroup;
                            fragment.mView = fragment.performCreateView(fragment.getLayoutInflater(fragment.mSavedFragmentState), viewGroup, fragment.mSavedFragmentState);
                            if (fragment.mView != null) {
                                fragment.mInnerView = fragment.mView;
                                if (Build.VERSION.SDK_INT >= 11) {
                                    ViewCompat.setSaveFromParentEnabled(fragment.mView, false);
                                } else {
                                    fragment.mView = NoSaveStateFrameLayout.wrap(fragment.mView);
                                }
                                if (viewGroup != null) {
                                    Animation loadAnimation = loadAnimation(fragment, i2, true, i3);
                                    if (loadAnimation != null) {
                                        setHWLayerAnimListenerIfAlpha(fragment.mView, loadAnimation);
                                        fragment.mView.startAnimation(loadAnimation);
                                    }
                                    viewGroup.addView(fragment.mView);
                                }
                                if (fragment.mHidden) {
                                    fragment.mView.setVisibility(8);
                                }
                                fragment.onViewCreated(fragment.mView, fragment.mSavedFragmentState);
                            } else {
                                fragment.mInnerView = null;
                            }
                        }
                        fragment.performActivityCreated(fragment.mSavedFragmentState);
                        if (fragment.mView != null) {
                            fragment.restoreViewState(fragment.mSavedFragmentState);
                        }
                        fragment.mSavedFragmentState = null;
                        i9 = i8;
                    }
                case 2:
                    i10 = i9;
                    if (i9 > 2) {
                        fragment.mState = 3;
                        i10 = i9;
                    }
                case 3:
                    i11 = i10;
                    if (i10 > 3) {
                        if (DEBUG) {
                            Log.v("FragmentManager", "moveto STARTED: " + fragment);
                        }
                        fragment.performStart();
                        i11 = i10;
                    }
                case 4:
                    i5 = i11;
                    if (i11 > 4) {
                        if (DEBUG) {
                            Log.v("FragmentManager", "moveto RESUMED: " + fragment);
                        }
                        fragment.performResume();
                        fragment.mSavedFragmentState = null;
                        fragment.mSavedViewState = null;
                        i5 = i11;
                        break;
                    }
                    break;
                default:
                    i5 = i7;
                    break;
            }
        }
        if (fragment.mState != i5) {
            Log.w("FragmentManager", "moveToState: Fragment state for " + fragment + " not updated inline; expected state " + i5 + " found " + fragment.mState);
            fragment.mState = i5;
        }
    }

    public void noteStateNotSaved() {
        this.mStateSaved = false;
    }

    @Override // android.support.v4.view.LayoutInflaterFactory
    public View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        Fragment fragment;
        if ("fragment".equals(str)) {
            String attributeValue = attributeSet.getAttributeValue(null, "class");
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, FragmentTag.Fragment);
            String str2 = attributeValue;
            if (attributeValue == null) {
                str2 = obtainStyledAttributes.getString(0);
            }
            int resourceId = obtainStyledAttributes.getResourceId(1, -1);
            String string = obtainStyledAttributes.getString(2);
            obtainStyledAttributes.recycle();
            if (Fragment.isSupportFragmentClass(this.mHost.getContext(), str2)) {
                int id = view != null ? view.getId() : 0;
                if (id == -1 && resourceId == -1 && string == null) {
                    throw new IllegalArgumentException(attributeSet.getPositionDescription() + ": Must specify unique android:id, android:tag, or have a parent with an id for " + str2);
                }
                Fragment fragment2 = null;
                if (resourceId != -1) {
                    fragment2 = findFragmentById(resourceId);
                }
                Fragment fragment3 = fragment2;
                if (fragment2 == null) {
                    fragment3 = fragment2;
                    if (string != null) {
                        fragment3 = findFragmentByTag(string);
                    }
                }
                Fragment fragment4 = fragment3;
                if (fragment3 == null) {
                    fragment4 = fragment3;
                    if (id != -1) {
                        fragment4 = findFragmentById(id);
                    }
                }
                if (DEBUG) {
                    Log.v("FragmentManager", "onCreateView: id=0x" + Integer.toHexString(resourceId) + " fname=" + str2 + " existing=" + fragment4);
                }
                if (fragment4 == null) {
                    fragment = Fragment.instantiate(context, str2);
                    fragment.mFromLayout = true;
                    fragment.mFragmentId = resourceId != 0 ? resourceId : id;
                    fragment.mContainerId = id;
                    fragment.mTag = string;
                    fragment.mInLayout = true;
                    fragment.mFragmentManager = this;
                    fragment.mHost = this.mHost;
                    fragment.onInflate(this.mHost.getContext(), attributeSet, fragment.mSavedFragmentState);
                    addFragment(fragment, true);
                } else if (fragment4.mInLayout) {
                    throw new IllegalArgumentException(attributeSet.getPositionDescription() + ": Duplicate id 0x" + Integer.toHexString(resourceId) + ", tag " + string + ", or parent id 0x" + Integer.toHexString(id) + " with another fragment for " + str2);
                } else {
                    fragment4.mInLayout = true;
                    fragment4.mHost = this.mHost;
                    fragment = fragment4;
                    if (!fragment4.mRetaining) {
                        fragment4.onInflate(this.mHost.getContext(), attributeSet, fragment4.mSavedFragmentState);
                        fragment = fragment4;
                    }
                }
                if (this.mCurState >= 1 || !fragment.mFromLayout) {
                    moveToState(fragment);
                } else {
                    moveToState(fragment, 1, 0, 0, false);
                }
                if (fragment.mView == null) {
                    throw new IllegalStateException("Fragment " + str2 + " did not create a view.");
                }
                if (resourceId != 0) {
                    fragment.mView.setId(resourceId);
                }
                if (fragment.mView.getTag() == null) {
                    fragment.mView.setTag(string);
                }
                return fragment.mView;
            }
            return null;
        }
        return null;
    }

    public void performPendingDeferredStart(Fragment fragment) {
        if (fragment.mDeferStart) {
            if (this.mExecutingActions) {
                this.mHavePendingDeferredStart = true;
                return;
            }
            fragment.mDeferStart = false;
            moveToState(fragment, this.mCurState, 0, 0, false);
        }
    }

    @Override // android.support.v4.app.FragmentManager
    public boolean popBackStackImmediate() {
        checkStateLoss();
        executePendingTransactions();
        return popBackStackState(this.mHost.getHandler(), null, -1, 0);
    }

    boolean popBackStackState(Handler handler, String str, int i, int i2) {
        if (this.mBackStack == null) {
            return false;
        }
        if (str == null && i < 0 && (i2 & 1) == 0) {
            int size = this.mBackStack.size() - 1;
            if (size < 0) {
                return false;
            }
            BackStackRecord remove = this.mBackStack.remove(size);
            SparseArray<Fragment> sparseArray = new SparseArray<>();
            SparseArray<Fragment> sparseArray2 = new SparseArray<>();
            if (this.mCurState >= 1) {
                remove.calculateBackFragments(sparseArray, sparseArray2);
            }
            remove.popFromBackStack(true, null, sparseArray, sparseArray2);
            reportBackStackChanged();
            return true;
        }
        int i3 = -1;
        if (str != null || i >= 0) {
            int size2 = this.mBackStack.size() - 1;
            while (size2 >= 0) {
                BackStackRecord backStackRecord = this.mBackStack.get(size2);
                if ((str != null && str.equals(backStackRecord.getName())) || (i >= 0 && i == backStackRecord.mIndex)) {
                    break;
                }
                size2--;
            }
            if (size2 < 0) {
                return false;
            }
            i3 = size2;
            if ((i2 & 1) != 0) {
                int i4 = size2 - 1;
                while (true) {
                    i3 = i4;
                    if (i4 < 0) {
                        break;
                    }
                    BackStackRecord backStackRecord2 = this.mBackStack.get(i4);
                    if (str == null || !str.equals(backStackRecord2.getName())) {
                        i3 = i4;
                        if (i < 0) {
                            break;
                        }
                        i3 = i4;
                        if (i != backStackRecord2.mIndex) {
                            break;
                        }
                    }
                    i4--;
                }
            }
        }
        if (i3 == this.mBackStack.size() - 1) {
            return false;
        }
        ArrayList arrayList = new ArrayList();
        for (int size3 = this.mBackStack.size() - 1; size3 > i3; size3--) {
            arrayList.add(this.mBackStack.remove(size3));
        }
        int size4 = arrayList.size() - 1;
        SparseArray<Fragment> sparseArray3 = new SparseArray<>();
        SparseArray<Fragment> sparseArray4 = new SparseArray<>();
        if (this.mCurState >= 1) {
            for (int i5 = 0; i5 <= size4; i5++) {
                ((BackStackRecord) arrayList.get(i5)).calculateBackFragments(sparseArray3, sparseArray4);
            }
        }
        BackStackRecord.TransitionState transitionState = null;
        int i6 = 0;
        while (i6 <= size4) {
            if (DEBUG) {
                Log.v("FragmentManager", "Popping back stack state: " + arrayList.get(i6));
            }
            transitionState = ((BackStackRecord) arrayList.get(i6)).popFromBackStack(i6 == size4, transitionState, sparseArray3, sparseArray4);
            i6++;
        }
        reportBackStackChanged();
        return true;
    }

    public void putFragment(Bundle bundle, String str, Fragment fragment) {
        if (fragment.mIndex < 0) {
            throwException(new IllegalStateException("Fragment " + fragment + " is not currently in the FragmentManager"));
        }
        bundle.putInt(str, fragment.mIndex);
    }

    public void removeFragment(Fragment fragment, int i, int i2) {
        int i3 = 1;
        if (DEBUG) {
            Log.v("FragmentManager", "remove: " + fragment + " nesting=" + fragment.mBackStackNesting);
        }
        boolean z = !fragment.isInBackStack();
        if (!fragment.mDetached || z) {
            if (this.mAdded != null) {
                this.mAdded.remove(fragment);
            }
            if (fragment.mHasMenu && fragment.mMenuVisible) {
                this.mNeedMenuInvalidate = true;
            }
            fragment.mAdded = false;
            fragment.mRemoving = true;
            if (z) {
                i3 = 0;
            }
            moveToState(fragment, i3, i, i2, false);
        }
    }

    void reportBackStackChanged() {
        if (this.mBackStackChangeListeners != null) {
            for (int i = 0; i < this.mBackStackChangeListeners.size(); i++) {
                this.mBackStackChangeListeners.get(i).onBackStackChanged();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void restoreAllState(Parcelable parcelable, FragmentManagerNonConfig fragmentManagerNonConfig) {
        if (parcelable == null) {
            return;
        }
        FragmentManagerState fragmentManagerState = (FragmentManagerState) parcelable;
        if (fragmentManagerState.mActive == null) {
            return;
        }
        List<FragmentManagerNonConfig> list = null;
        if (fragmentManagerNonConfig != null) {
            List<Fragment> fragments = fragmentManagerNonConfig.getFragments();
            List<FragmentManagerNonConfig> childNonConfigs = fragmentManagerNonConfig.getChildNonConfigs();
            int size = fragments != null ? fragments.size() : 0;
            int i = 0;
            while (true) {
                list = childNonConfigs;
                if (i >= size) {
                    break;
                }
                Fragment fragment = fragments.get(i);
                if (DEBUG) {
                    Log.v("FragmentManager", "restoreAllState: re-attaching retained " + fragment);
                }
                FragmentState fragmentState = fragmentManagerState.mActive[fragment.mIndex];
                fragmentState.mInstance = fragment;
                fragment.mSavedViewState = null;
                fragment.mBackStackNesting = 0;
                fragment.mInLayout = false;
                fragment.mAdded = false;
                fragment.mTarget = null;
                if (fragmentState.mSavedFragmentState != null) {
                    fragmentState.mSavedFragmentState.setClassLoader(this.mHost.getContext().getClassLoader());
                    fragment.mSavedViewState = fragmentState.mSavedFragmentState.getSparseParcelableArray("android:view_state");
                    fragment.mSavedFragmentState = fragmentState.mSavedFragmentState;
                }
                i++;
            }
        }
        this.mActive = new ArrayList<>(fragmentManagerState.mActive.length);
        if (this.mAvailIndices != null) {
            this.mAvailIndices.clear();
        }
        for (int i2 = 0; i2 < fragmentManagerState.mActive.length; i2++) {
            FragmentState fragmentState2 = fragmentManagerState.mActive[i2];
            if (fragmentState2 != null) {
                FragmentManagerNonConfig fragmentManagerNonConfig2 = null;
                if (list != null) {
                    fragmentManagerNonConfig2 = null;
                    if (i2 < list.size()) {
                        fragmentManagerNonConfig2 = list.get(i2);
                    }
                }
                Fragment instantiate = fragmentState2.instantiate(this.mHost, this.mParent, fragmentManagerNonConfig2);
                if (DEBUG) {
                    Log.v("FragmentManager", "restoreAllState: active #" + i2 + ": " + instantiate);
                }
                this.mActive.add(instantiate);
                fragmentState2.mInstance = null;
            } else {
                this.mActive.add(null);
                if (this.mAvailIndices == null) {
                    this.mAvailIndices = new ArrayList<>();
                }
                if (DEBUG) {
                    Log.v("FragmentManager", "restoreAllState: avail #" + i2);
                }
                this.mAvailIndices.add(Integer.valueOf(i2));
            }
        }
        if (fragmentManagerNonConfig != null) {
            List<Fragment> fragments2 = fragmentManagerNonConfig.getFragments();
            int size2 = fragments2 != null ? fragments2.size() : 0;
            for (int i3 = 0; i3 < size2; i3++) {
                Fragment fragment2 = fragments2.get(i3);
                if (fragment2.mTargetIndex >= 0) {
                    if (fragment2.mTargetIndex < this.mActive.size()) {
                        fragment2.mTarget = this.mActive.get(fragment2.mTargetIndex);
                    } else {
                        Log.w("FragmentManager", "Re-attaching retained fragment " + fragment2 + " target no longer exists: " + fragment2.mTargetIndex);
                        fragment2.mTarget = null;
                    }
                }
            }
        }
        if (fragmentManagerState.mAdded != null) {
            this.mAdded = new ArrayList<>(fragmentManagerState.mAdded.length);
            for (int i4 = 0; i4 < fragmentManagerState.mAdded.length; i4++) {
                Fragment fragment3 = this.mActive.get(fragmentManagerState.mAdded[i4]);
                if (fragment3 == null) {
                    throwException(new IllegalStateException("No instantiated fragment for index #" + fragmentManagerState.mAdded[i4]));
                }
                fragment3.mAdded = true;
                if (DEBUG) {
                    Log.v("FragmentManager", "restoreAllState: added #" + i4 + ": " + fragment3);
                }
                if (this.mAdded.contains(fragment3)) {
                    throw new IllegalStateException("Already added!");
                }
                this.mAdded.add(fragment3);
            }
        } else {
            this.mAdded = null;
        }
        if (fragmentManagerState.mBackStack == null) {
            this.mBackStack = null;
            return;
        }
        this.mBackStack = new ArrayList<>(fragmentManagerState.mBackStack.length);
        for (int i5 = 0; i5 < fragmentManagerState.mBackStack.length; i5++) {
            BackStackRecord instantiate2 = fragmentManagerState.mBackStack[i5].instantiate(this);
            if (DEBUG) {
                Log.v("FragmentManager", "restoreAllState: back stack #" + i5 + " (index " + instantiate2.mIndex + "): " + instantiate2);
                instantiate2.dump("  ", new PrintWriter(new LogWriter("FragmentManager")), false);
            }
            this.mBackStack.add(instantiate2);
            if (instantiate2.mIndex >= 0) {
                setBackStackIndex(instantiate2.mIndex, instantiate2);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public FragmentManagerNonConfig retainNonConfig() {
        ArrayList arrayList = null;
        ArrayList arrayList2 = null;
        ArrayList arrayList3 = null;
        ArrayList arrayList4 = null;
        if (this.mActive != null) {
            int i = 0;
            while (true) {
                arrayList3 = arrayList4;
                arrayList = arrayList2;
                if (i >= this.mActive.size()) {
                    break;
                }
                Fragment fragment = this.mActive.get(i);
                ArrayList arrayList5 = arrayList4;
                ArrayList arrayList6 = arrayList2;
                if (fragment != null) {
                    ArrayList arrayList7 = arrayList2;
                    if (fragment.mRetainInstance) {
                        ArrayList arrayList8 = arrayList2;
                        if (arrayList2 == null) {
                            arrayList8 = new ArrayList();
                        }
                        arrayList8.add(fragment);
                        fragment.mRetaining = true;
                        fragment.mTargetIndex = fragment.mTarget != null ? fragment.mTarget.mIndex : -1;
                        arrayList7 = arrayList8;
                        if (DEBUG) {
                            Log.v("FragmentManager", "retainNonConfig: keeping retained " + fragment);
                            arrayList7 = arrayList8;
                        }
                    }
                    boolean z = false;
                    ArrayList arrayList9 = arrayList4;
                    if (fragment.mChildFragmentManager != null) {
                        FragmentManagerNonConfig retainNonConfig = fragment.mChildFragmentManager.retainNonConfig();
                        z = false;
                        arrayList9 = arrayList4;
                        if (retainNonConfig != null) {
                            arrayList9 = arrayList4;
                            if (arrayList4 == null) {
                                ArrayList arrayList10 = new ArrayList();
                                int i2 = 0;
                                while (true) {
                                    arrayList9 = arrayList10;
                                    if (i2 >= i) {
                                        break;
                                    }
                                    arrayList10.add(null);
                                    i2++;
                                }
                            }
                            arrayList9.add(retainNonConfig);
                            z = true;
                        }
                    }
                    arrayList5 = arrayList9;
                    arrayList6 = arrayList7;
                    if (arrayList9 != null) {
                        if (z) {
                            arrayList6 = arrayList7;
                            arrayList5 = arrayList9;
                        } else {
                            arrayList9.add(null);
                            arrayList5 = arrayList9;
                            arrayList6 = arrayList7;
                        }
                    }
                }
                i++;
                arrayList4 = arrayList5;
                arrayList2 = arrayList6;
            }
        }
        if (arrayList == null && arrayList3 == null) {
            return null;
        }
        return new FragmentManagerNonConfig(arrayList, arrayList3);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Parcelable saveAllState() {
        execPendingActions();
        if (HONEYCOMB) {
            this.mStateSaved = true;
        }
        if (this.mActive == null || this.mActive.size() <= 0) {
            return null;
        }
        int size = this.mActive.size();
        FragmentState[] fragmentStateArr = new FragmentState[size];
        boolean z = false;
        for (int i = 0; i < size; i++) {
            Fragment fragment = this.mActive.get(i);
            if (fragment != null) {
                if (fragment.mIndex < 0) {
                    throwException(new IllegalStateException("Failure saving state: active " + fragment + " has cleared index: " + fragment.mIndex));
                }
                FragmentState fragmentState = new FragmentState(fragment);
                fragmentStateArr[i] = fragmentState;
                if (fragment.mState <= 0 || fragmentState.mSavedFragmentState != null) {
                    fragmentState.mSavedFragmentState = fragment.mSavedFragmentState;
                } else {
                    fragmentState.mSavedFragmentState = saveFragmentBasicState(fragment);
                    if (fragment.mTarget != null) {
                        if (fragment.mTarget.mIndex < 0) {
                            throwException(new IllegalStateException("Failure saving state: " + fragment + " has target not in fragment manager: " + fragment.mTarget));
                        }
                        if (fragmentState.mSavedFragmentState == null) {
                            fragmentState.mSavedFragmentState = new Bundle();
                        }
                        putFragment(fragmentState.mSavedFragmentState, "android:target_state", fragment.mTarget);
                        if (fragment.mTargetRequestCode != 0) {
                            fragmentState.mSavedFragmentState.putInt("android:target_req_state", fragment.mTargetRequestCode);
                        }
                    }
                }
                z = true;
                if (DEBUG) {
                    Log.v("FragmentManager", "Saved state of " + fragment + ": " + fragmentState.mSavedFragmentState);
                    z = true;
                }
            }
        }
        if (!z) {
            if (DEBUG) {
                Log.v("FragmentManager", "saveAllState: no fragments!");
                return null;
            }
            return null;
        }
        int[] iArr = null;
        if (this.mAdded != null) {
            int size2 = this.mAdded.size();
            iArr = null;
            if (size2 > 0) {
                int[] iArr2 = new int[size2];
                int i2 = 0;
                while (true) {
                    iArr = iArr2;
                    if (i2 >= size2) {
                        break;
                    }
                    iArr2[i2] = this.mAdded.get(i2).mIndex;
                    if (iArr2[i2] < 0) {
                        throwException(new IllegalStateException("Failure saving state: active " + this.mAdded.get(i2) + " has cleared index: " + iArr2[i2]));
                    }
                    if (DEBUG) {
                        Log.v("FragmentManager", "saveAllState: adding fragment #" + i2 + ": " + this.mAdded.get(i2));
                    }
                    i2++;
                }
            }
        }
        BackStackState[] backStackStateArr = null;
        if (this.mBackStack != null) {
            int size3 = this.mBackStack.size();
            backStackStateArr = null;
            if (size3 > 0) {
                BackStackState[] backStackStateArr2 = new BackStackState[size3];
                int i3 = 0;
                while (true) {
                    backStackStateArr = backStackStateArr2;
                    if (i3 >= size3) {
                        break;
                    }
                    backStackStateArr2[i3] = new BackStackState(this.mBackStack.get(i3));
                    if (DEBUG) {
                        Log.v("FragmentManager", "saveAllState: adding back stack #" + i3 + ": " + this.mBackStack.get(i3));
                    }
                    i3++;
                }
            }
        }
        FragmentManagerState fragmentManagerState = new FragmentManagerState();
        fragmentManagerState.mActive = fragmentStateArr;
        fragmentManagerState.mAdded = iArr;
        fragmentManagerState.mBackStack = backStackStateArr;
        return fragmentManagerState;
    }

    Bundle saveFragmentBasicState(Fragment fragment) {
        Bundle bundle = null;
        if (this.mStateBundle == null) {
            this.mStateBundle = new Bundle();
        }
        fragment.performSaveInstanceState(this.mStateBundle);
        if (!this.mStateBundle.isEmpty()) {
            bundle = this.mStateBundle;
            this.mStateBundle = null;
        }
        if (fragment.mView != null) {
            saveFragmentViewState(fragment);
        }
        Bundle bundle2 = bundle;
        if (fragment.mSavedViewState != null) {
            bundle2 = bundle;
            if (bundle == null) {
                bundle2 = new Bundle();
            }
            bundle2.putSparseParcelableArray("android:view_state", fragment.mSavedViewState);
        }
        Bundle bundle3 = bundle2;
        if (!fragment.mUserVisibleHint) {
            bundle3 = bundle2;
            if (bundle2 == null) {
                bundle3 = new Bundle();
            }
            bundle3.putBoolean("android:user_visible_hint", fragment.mUserVisibleHint);
        }
        return bundle3;
    }

    void saveFragmentViewState(Fragment fragment) {
        if (fragment.mInnerView == null) {
            return;
        }
        if (this.mStateArray == null) {
            this.mStateArray = new SparseArray<>();
        } else {
            this.mStateArray.clear();
        }
        fragment.mInnerView.saveHierarchyState(this.mStateArray);
        if (this.mStateArray.size() > 0) {
            fragment.mSavedViewState = this.mStateArray;
            this.mStateArray = null;
        }
    }

    public void setBackStackIndex(int i, BackStackRecord backStackRecord) {
        synchronized (this) {
            if (this.mBackStackIndices == null) {
                this.mBackStackIndices = new ArrayList<>();
            }
            int size = this.mBackStackIndices.size();
            if (i < size) {
                if (DEBUG) {
                    Log.v("FragmentManager", "Setting back stack index " + i + " to " + backStackRecord);
                }
                this.mBackStackIndices.set(i, backStackRecord);
            } else {
                for (int i2 = size; i2 < i; i2++) {
                    this.mBackStackIndices.add(null);
                    if (this.mAvailBackStackIndices == null) {
                        this.mAvailBackStackIndices = new ArrayList<>();
                    }
                    if (DEBUG) {
                        Log.v("FragmentManager", "Adding available back stack index " + i2);
                    }
                    this.mAvailBackStackIndices.add(Integer.valueOf(i2));
                }
                if (DEBUG) {
                    Log.v("FragmentManager", "Adding back stack index " + i + " with " + backStackRecord);
                }
                this.mBackStackIndices.add(backStackRecord);
            }
        }
    }

    public void showFragment(Fragment fragment, int i, int i2) {
        if (DEBUG) {
            Log.v("FragmentManager", "show: " + fragment);
        }
        if (fragment.mHidden) {
            fragment.mHidden = false;
            if (fragment.mView != null) {
                Animation loadAnimation = loadAnimation(fragment, i, true, i2);
                if (loadAnimation != null) {
                    setHWLayerAnimListenerIfAlpha(fragment.mView, loadAnimation);
                    fragment.mView.startAnimation(loadAnimation);
                }
                fragment.mView.setVisibility(0);
            }
            if (fragment.mAdded && fragment.mHasMenu && fragment.mMenuVisible) {
                this.mNeedMenuInvalidate = true;
            }
            fragment.onHiddenChanged(false);
        }
    }

    void startPendingDeferredFragments() {
        if (this.mActive == null) {
            return;
        }
        for (int i = 0; i < this.mActive.size(); i++) {
            Fragment fragment = this.mActive.get(i);
            if (fragment != null) {
                performPendingDeferredStart(fragment);
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("FragmentManager{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" in ");
        if (this.mParent != null) {
            DebugUtils.buildShortClassTag(this.mParent, sb);
        } else {
            DebugUtils.buildShortClassTag(this.mHost, sb);
        }
        sb.append("}}");
        return sb.toString();
    }
}
