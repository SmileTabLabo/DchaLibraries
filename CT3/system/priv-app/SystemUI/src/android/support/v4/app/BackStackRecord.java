package android.support.v4.app;

import android.os.Build;
import android.support.v4.app.FragmentTransitionCompat21;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.LogWriter;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v4/app/BackStackRecord.class */
public final class BackStackRecord extends FragmentTransaction implements Runnable {
    static final boolean SUPPORTS_TRANSITIONS;
    boolean mAddToBackStack;
    int mBreadCrumbShortTitleRes;
    CharSequence mBreadCrumbShortTitleText;
    int mBreadCrumbTitleRes;
    CharSequence mBreadCrumbTitleText;
    boolean mCommitted;
    int mEnterAnim;
    int mExitAnim;
    Op mHead;
    final FragmentManagerImpl mManager;
    String mName;
    int mNumOp;
    int mPopEnterAnim;
    int mPopExitAnim;
    ArrayList<String> mSharedElementSourceNames;
    ArrayList<String> mSharedElementTargetNames;
    Op mTail;
    int mTransition;
    int mTransitionStyle;
    boolean mAllowAddToBackStack = true;
    int mIndex = -1;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:android/support/v4/app/BackStackRecord$Op.class */
    public static final class Op {
        int cmd;
        int enterAnim;
        int exitAnim;
        Fragment fragment;
        Op next;
        int popEnterAnim;
        int popExitAnim;
        Op prev;
        ArrayList<Fragment> removed;
    }

    /* loaded from: a.zip:android/support/v4/app/BackStackRecord$TransitionState.class */
    public class TransitionState {
        public View nonExistentView;
        final BackStackRecord this$0;
        public ArrayMap<String, String> nameOverrides = new ArrayMap<>();
        public ArrayList<View> hiddenFragmentViews = new ArrayList<>();
        public FragmentTransitionCompat21.EpicenterView enteringEpicenterView = new FragmentTransitionCompat21.EpicenterView();

        public TransitionState(BackStackRecord backStackRecord) {
            this.this$0 = backStackRecord;
        }
    }

    static {
        SUPPORTS_TRANSITIONS = Build.VERSION.SDK_INT >= 21;
    }

    public BackStackRecord(FragmentManagerImpl fragmentManagerImpl) {
        this.mManager = fragmentManagerImpl;
    }

    private TransitionState beginTransition(SparseArray<Fragment> sparseArray, SparseArray<Fragment> sparseArray2, boolean z) {
        TransitionState transitionState = new TransitionState(this);
        transitionState.nonExistentView = new View(this.mManager.mHost.getContext());
        boolean z2 = false;
        for (int i = 0; i < sparseArray.size(); i++) {
            if (configureTransitions(sparseArray.keyAt(i), transitionState, z, sparseArray, sparseArray2)) {
                z2 = true;
            }
        }
        int i2 = 0;
        while (i2 < sparseArray2.size()) {
            int keyAt = sparseArray2.keyAt(i2);
            boolean z3 = z2;
            if (sparseArray.get(keyAt) == null) {
                z3 = z2;
                if (configureTransitions(keyAt, transitionState, z, sparseArray, sparseArray2)) {
                    z3 = true;
                }
            }
            i2++;
            z2 = z3;
        }
        TransitionState transitionState2 = transitionState;
        if (!z2) {
            transitionState2 = null;
        }
        return transitionState2;
    }

    private void calculateFragments(SparseArray<Fragment> sparseArray, SparseArray<Fragment> sparseArray2) {
        Fragment fragment;
        if (!this.mManager.mContainer.onHasView()) {
            return;
        }
        Op op = this.mHead;
        while (true) {
            Op op2 = op;
            if (op2 == null) {
                return;
            }
            switch (op2.cmd) {
                case 1:
                    setLastIn(sparseArray, sparseArray2, op2.fragment);
                    break;
                case 2:
                    Fragment fragment2 = op2.fragment;
                    if (this.mManager.mAdded != null) {
                        int i = 0;
                        while (i < this.mManager.mAdded.size()) {
                            Fragment fragment3 = this.mManager.mAdded.get(i);
                            if (fragment2 != null) {
                                fragment = fragment2;
                                if (fragment3.mContainerId != fragment2.mContainerId) {
                                    i++;
                                    fragment2 = fragment;
                                }
                            }
                            if (fragment3 == fragment2) {
                                fragment = null;
                                sparseArray2.remove(fragment3.mContainerId);
                            } else {
                                setFirstOut(sparseArray, sparseArray2, fragment3);
                                fragment = fragment2;
                            }
                            i++;
                            fragment2 = fragment;
                        }
                    }
                    setLastIn(sparseArray, sparseArray2, op2.fragment);
                    break;
                case 3:
                    setFirstOut(sparseArray, sparseArray2, op2.fragment);
                    break;
                case 4:
                    setFirstOut(sparseArray, sparseArray2, op2.fragment);
                    break;
                case 5:
                    setLastIn(sparseArray, sparseArray2, op2.fragment);
                    break;
                case 6:
                    setFirstOut(sparseArray, sparseArray2, op2.fragment);
                    break;
                case 7:
                    setLastIn(sparseArray, sparseArray2, op2.fragment);
                    break;
            }
            op = op2.next;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callSharedElementEnd(TransitionState transitionState, Fragment fragment, Fragment fragment2, boolean z, ArrayMap<String, View> arrayMap) {
        SharedElementCallback sharedElementCallback = z ? fragment2.mEnterTransitionCallback : fragment.mEnterTransitionCallback;
        if (sharedElementCallback != null) {
            sharedElementCallback.onSharedElementEnd(new ArrayList(arrayMap.keySet()), new ArrayList(arrayMap.values()), null);
        }
    }

    private static Object captureExitingViews(Object obj, Fragment fragment, ArrayList<View> arrayList, ArrayMap<String, View> arrayMap, View view) {
        Object obj2 = obj;
        if (obj != null) {
            obj2 = FragmentTransitionCompat21.captureExitingViews(obj, fragment.getView(), arrayList, arrayMap, view);
        }
        return obj2;
    }

    private boolean configureTransitions(int i, TransitionState transitionState, boolean z, SparseArray<Fragment> sparseArray, SparseArray<Fragment> sparseArray2) {
        View view;
        ViewGroup viewGroup = (ViewGroup) this.mManager.mContainer.onFindViewById(i);
        if (viewGroup == null) {
            return false;
        }
        Fragment fragment = sparseArray2.get(i);
        Fragment fragment2 = sparseArray.get(i);
        Object enterTransition = getEnterTransition(fragment, z);
        Object sharedElementTransition = getSharedElementTransition(fragment, fragment2, z);
        Object exitTransition = getExitTransition(fragment2, z);
        ArrayMap<String, View> arrayMap = null;
        ArrayList<View> arrayList = new ArrayList<>();
        Object obj = sharedElementTransition;
        if (sharedElementTransition != null) {
            ArrayMap<String, View> remapSharedElements = remapSharedElements(transitionState, fragment2, z);
            if (remapSharedElements.isEmpty()) {
                obj = null;
                arrayMap = null;
            } else {
                SharedElementCallback sharedElementCallback = z ? fragment2.mEnterTransitionCallback : fragment.mEnterTransitionCallback;
                if (sharedElementCallback != null) {
                    sharedElementCallback.onSharedElementStart(new ArrayList(remapSharedElements.keySet()), new ArrayList(remapSharedElements.values()), null);
                }
                prepareSharedElementTransition(transitionState, viewGroup, sharedElementTransition, fragment, fragment2, z, arrayList, enterTransition, exitTransition);
                obj = sharedElementTransition;
                arrayMap = remapSharedElements;
            }
        }
        if (enterTransition == null && obj == null && exitTransition == null) {
            return false;
        }
        ArrayList arrayList2 = new ArrayList();
        Object captureExitingViews = captureExitingViews(exitTransition, fragment2, arrayList2, arrayMap, transitionState.nonExistentView);
        if (this.mSharedElementTargetNames != null && arrayMap != null && (view = arrayMap.get(this.mSharedElementTargetNames.get(0))) != null) {
            if (captureExitingViews != null) {
                FragmentTransitionCompat21.setEpicenter(captureExitingViews, view);
            }
            if (obj != null) {
                FragmentTransitionCompat21.setEpicenter(obj, view);
            }
        }
        FragmentTransitionCompat21.ViewRetriever viewRetriever = new FragmentTransitionCompat21.ViewRetriever(this, fragment) { // from class: android.support.v4.app.BackStackRecord.1
            final BackStackRecord this$0;
            final Fragment val$inFragment;

            {
                this.this$0 = this;
                this.val$inFragment = fragment;
            }

            @Override // android.support.v4.app.FragmentTransitionCompat21.ViewRetriever
            public View getView() {
                return this.val$inFragment.getView();
            }
        };
        ArrayList arrayList3 = new ArrayList();
        ArrayMap arrayMap2 = new ArrayMap();
        boolean z2 = true;
        if (fragment != null) {
            z2 = z ? fragment.getAllowReturnTransitionOverlap() : fragment.getAllowEnterTransitionOverlap();
        }
        Object mergeTransitions = FragmentTransitionCompat21.mergeTransitions(enterTransition, captureExitingViews, obj, z2);
        if (mergeTransitions != null) {
            FragmentTransitionCompat21.addTransitionTargets(enterTransition, obj, captureExitingViews, viewGroup, viewRetriever, transitionState.nonExistentView, transitionState.enteringEpicenterView, transitionState.nameOverrides, arrayList3, arrayList2, arrayMap, arrayMap2, arrayList);
            excludeHiddenFragmentsAfterEnter(viewGroup, transitionState, i, mergeTransitions);
            FragmentTransitionCompat21.excludeTarget(mergeTransitions, transitionState.nonExistentView, true);
            excludeHiddenFragments(transitionState, i, mergeTransitions);
            FragmentTransitionCompat21.beginDelayedTransition(viewGroup, mergeTransitions);
            FragmentTransitionCompat21.cleanupTransitions(viewGroup, transitionState.nonExistentView, enterTransition, arrayList3, captureExitingViews, arrayList2, obj, arrayList, mergeTransitions, transitionState.hiddenFragmentViews, arrayMap2);
        }
        return mergeTransitions != null;
    }

    private void doAddOp(int i, Fragment fragment, String str, int i2) {
        fragment.mFragmentManager = this.mManager;
        if (str != null) {
            if (fragment.mTag != null && !str.equals(fragment.mTag)) {
                throw new IllegalStateException("Can't change tag of fragment " + fragment + ": was " + fragment.mTag + " now " + str);
            }
            fragment.mTag = str;
        }
        if (i != 0) {
            if (i == -1) {
                throw new IllegalArgumentException("Can't add fragment " + fragment + " with tag " + str + " to container view with no id");
            }
            if (fragment.mFragmentId != 0 && fragment.mFragmentId != i) {
                throw new IllegalStateException("Can't change container ID of fragment " + fragment + ": was " + fragment.mFragmentId + " now " + i);
            }
            fragment.mFragmentId = i;
            fragment.mContainerId = i;
        }
        Op op = new Op();
        op.cmd = i2;
        op.fragment = fragment;
        addOp(op);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void excludeHiddenFragments(TransitionState transitionState, int i, Object obj) {
        if (this.mManager.mAdded != null) {
            for (int i2 = 0; i2 < this.mManager.mAdded.size(); i2++) {
                Fragment fragment = this.mManager.mAdded.get(i2);
                if (fragment.mView != null && fragment.mContainer != null && fragment.mContainerId == i) {
                    if (!fragment.mHidden) {
                        FragmentTransitionCompat21.excludeTarget(obj, fragment.mView, false);
                        transitionState.hiddenFragmentViews.remove(fragment.mView);
                    } else if (!transitionState.hiddenFragmentViews.contains(fragment.mView)) {
                        FragmentTransitionCompat21.excludeTarget(obj, fragment.mView, true);
                        transitionState.hiddenFragmentViews.add(fragment.mView);
                    }
                }
            }
        }
    }

    private void excludeHiddenFragmentsAfterEnter(View view, TransitionState transitionState, int i, Object obj) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(this, view, transitionState, i, obj) { // from class: android.support.v4.app.BackStackRecord.3
            final BackStackRecord this$0;
            final int val$containerId;
            final View val$sceneRoot;
            final TransitionState val$state;
            final Object val$transition;

            {
                this.this$0 = this;
                this.val$sceneRoot = view;
                this.val$state = transitionState;
                this.val$containerId = i;
                this.val$transition = obj;
            }

            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                this.val$sceneRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                this.this$0.excludeHiddenFragments(this.val$state, this.val$containerId, this.val$transition);
                return true;
            }
        });
    }

    private static Object getEnterTransition(Fragment fragment, boolean z) {
        if (fragment == null) {
            return null;
        }
        return FragmentTransitionCompat21.cloneTransition(z ? fragment.getReenterTransition() : fragment.getEnterTransition());
    }

    private static Object getExitTransition(Fragment fragment, boolean z) {
        if (fragment == null) {
            return null;
        }
        return FragmentTransitionCompat21.cloneTransition(z ? fragment.getReturnTransition() : fragment.getExitTransition());
    }

    private static Object getSharedElementTransition(Fragment fragment, Fragment fragment2, boolean z) {
        if (fragment == null || fragment2 == null) {
            return null;
        }
        return FragmentTransitionCompat21.wrapSharedElementTransition(z ? fragment2.getSharedElementReturnTransition() : fragment.getSharedElementEnterTransition());
    }

    private ArrayMap<String, View> mapEnteringSharedElements(TransitionState transitionState, Fragment fragment, boolean z) {
        ArrayMap<String, View> arrayMap = new ArrayMap<>();
        View view = fragment.getView();
        ArrayMap<String, View> arrayMap2 = arrayMap;
        if (view != null) {
            arrayMap2 = arrayMap;
            if (this.mSharedElementSourceNames != null) {
                FragmentTransitionCompat21.findNamedViews(arrayMap, view);
                if (z) {
                    arrayMap2 = remapNames(this.mSharedElementSourceNames, this.mSharedElementTargetNames, arrayMap);
                } else {
                    arrayMap.retainAll(this.mSharedElementTargetNames);
                    arrayMap2 = arrayMap;
                }
            }
        }
        return arrayMap2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ArrayMap<String, View> mapSharedElementsIn(TransitionState transitionState, boolean z, Fragment fragment) {
        ArrayMap<String, View> mapEnteringSharedElements = mapEnteringSharedElements(transitionState, fragment, z);
        if (z) {
            if (fragment.mExitTransitionCallback != null) {
                fragment.mExitTransitionCallback.onMapSharedElements(this.mSharedElementTargetNames, mapEnteringSharedElements);
            }
            setBackNameOverrides(transitionState, mapEnteringSharedElements, true);
        } else {
            if (fragment.mEnterTransitionCallback != null) {
                fragment.mEnterTransitionCallback.onMapSharedElements(this.mSharedElementTargetNames, mapEnteringSharedElements);
            }
            setNameOverrides(transitionState, mapEnteringSharedElements, true);
        }
        return mapEnteringSharedElements;
    }

    private void prepareSharedElementTransition(TransitionState transitionState, View view, Object obj, Fragment fragment, Fragment fragment2, boolean z, ArrayList<View> arrayList, Object obj2, Object obj3) {
        if (obj != null) {
            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(this, view, obj, arrayList, transitionState, obj2, obj3, z, fragment, fragment2) { // from class: android.support.v4.app.BackStackRecord.2
                final BackStackRecord this$0;
                final Object val$enterTransition;
                final Object val$exitTransition;
                final Fragment val$inFragment;
                final boolean val$isBack;
                final Fragment val$outFragment;
                final View val$sceneRoot;
                final ArrayList val$sharedElementTargets;
                final Object val$sharedElementTransition;
                final TransitionState val$state;

                {
                    this.this$0 = this;
                    this.val$sceneRoot = view;
                    this.val$sharedElementTransition = obj;
                    this.val$sharedElementTargets = arrayList;
                    this.val$state = transitionState;
                    this.val$enterTransition = obj2;
                    this.val$exitTransition = obj3;
                    this.val$isBack = z;
                    this.val$inFragment = fragment;
                    this.val$outFragment = fragment2;
                }

                @Override // android.view.ViewTreeObserver.OnPreDrawListener
                public boolean onPreDraw() {
                    this.val$sceneRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                    FragmentTransitionCompat21.removeTargets(this.val$sharedElementTransition, this.val$sharedElementTargets);
                    this.val$sharedElementTargets.remove(this.val$state.nonExistentView);
                    FragmentTransitionCompat21.excludeSharedElementViews(this.val$enterTransition, this.val$exitTransition, this.val$sharedElementTransition, this.val$sharedElementTargets, false);
                    this.val$sharedElementTargets.clear();
                    ArrayMap mapSharedElementsIn = this.this$0.mapSharedElementsIn(this.val$state, this.val$isBack, this.val$inFragment);
                    FragmentTransitionCompat21.setSharedElementTargets(this.val$sharedElementTransition, this.val$state.nonExistentView, mapSharedElementsIn, this.val$sharedElementTargets);
                    this.this$0.setEpicenterIn(mapSharedElementsIn, this.val$state);
                    this.this$0.callSharedElementEnd(this.val$state, this.val$inFragment, this.val$outFragment, this.val$isBack, mapSharedElementsIn);
                    FragmentTransitionCompat21.excludeSharedElementViews(this.val$enterTransition, this.val$exitTransition, this.val$sharedElementTransition, this.val$sharedElementTargets, true);
                    return true;
                }
            });
        }
    }

    private static ArrayMap<String, View> remapNames(ArrayList<String> arrayList, ArrayList<String> arrayList2, ArrayMap<String, View> arrayMap) {
        if (arrayMap.isEmpty()) {
            return arrayMap;
        }
        ArrayMap<String, View> arrayMap2 = new ArrayMap<>();
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            View view = arrayMap.get(arrayList.get(i));
            if (view != null) {
                arrayMap2.put(arrayList2.get(i), view);
            }
        }
        return arrayMap2;
    }

    private ArrayMap<String, View> remapSharedElements(TransitionState transitionState, Fragment fragment, boolean z) {
        ArrayMap<String, View> arrayMap = new ArrayMap<>();
        ArrayMap<String, View> arrayMap2 = arrayMap;
        if (this.mSharedElementSourceNames != null) {
            FragmentTransitionCompat21.findNamedViews(arrayMap, fragment.getView());
            if (z) {
                arrayMap.retainAll(this.mSharedElementTargetNames);
                arrayMap2 = arrayMap;
            } else {
                arrayMap2 = remapNames(this.mSharedElementSourceNames, this.mSharedElementTargetNames, arrayMap);
            }
        }
        if (z) {
            if (fragment.mEnterTransitionCallback != null) {
                fragment.mEnterTransitionCallback.onMapSharedElements(this.mSharedElementTargetNames, arrayMap2);
            }
            setBackNameOverrides(transitionState, arrayMap2, false);
        } else {
            if (fragment.mExitTransitionCallback != null) {
                fragment.mExitTransitionCallback.onMapSharedElements(this.mSharedElementTargetNames, arrayMap2);
            }
            setNameOverrides(transitionState, arrayMap2, false);
        }
        return arrayMap2;
    }

    private void setBackNameOverrides(TransitionState transitionState, ArrayMap<String, View> arrayMap, boolean z) {
        int size = this.mSharedElementTargetNames == null ? 0 : this.mSharedElementTargetNames.size();
        for (int i = 0; i < size; i++) {
            String str = this.mSharedElementSourceNames.get(i);
            View view = arrayMap.get(this.mSharedElementTargetNames.get(i));
            if (view != null) {
                String transitionName = FragmentTransitionCompat21.getTransitionName(view);
                if (z) {
                    setNameOverride(transitionState.nameOverrides, str, transitionName);
                } else {
                    setNameOverride(transitionState.nameOverrides, transitionName, str);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setEpicenterIn(ArrayMap<String, View> arrayMap, TransitionState transitionState) {
        View view;
        if (this.mSharedElementTargetNames == null || arrayMap.isEmpty() || (view = arrayMap.get(this.mSharedElementTargetNames.get(0))) == null) {
            return;
        }
        transitionState.enteringEpicenterView.epicenter = view;
    }

    private static void setFirstOut(SparseArray<Fragment> sparseArray, SparseArray<Fragment> sparseArray2, Fragment fragment) {
        int i;
        if (fragment == null || (i = fragment.mContainerId) == 0 || fragment.isHidden()) {
            return;
        }
        if (fragment.isAdded() && fragment.getView() != null && sparseArray.get(i) == null) {
            sparseArray.put(i, fragment);
        }
        if (sparseArray2.get(i) == fragment) {
            sparseArray2.remove(i);
        }
    }

    private void setLastIn(SparseArray<Fragment> sparseArray, SparseArray<Fragment> sparseArray2, Fragment fragment) {
        if (fragment != null) {
            int i = fragment.mContainerId;
            if (i != 0) {
                if (!fragment.isAdded()) {
                    sparseArray2.put(i, fragment);
                }
                if (sparseArray.get(i) == fragment) {
                    sparseArray.remove(i);
                }
            }
            if (fragment.mState >= 1 || this.mManager.mCurState < 1) {
                return;
            }
            this.mManager.makeActive(fragment);
            this.mManager.moveToState(fragment, 1, 0, 0, false);
        }
    }

    private static void setNameOverride(ArrayMap<String, String> arrayMap, String str, String str2) {
        if (str == null || str2 == null) {
            return;
        }
        for (int i = 0; i < arrayMap.size(); i++) {
            if (str.equals(arrayMap.valueAt(i))) {
                arrayMap.setValueAt(i, str2);
                return;
            }
        }
        arrayMap.put(str, str2);
    }

    private void setNameOverrides(TransitionState transitionState, ArrayMap<String, View> arrayMap, boolean z) {
        int size = arrayMap.size();
        for (int i = 0; i < size; i++) {
            String keyAt = arrayMap.keyAt(i);
            String transitionName = FragmentTransitionCompat21.getTransitionName(arrayMap.valueAt(i));
            if (z) {
                setNameOverride(transitionState.nameOverrides, keyAt, transitionName);
            } else {
                setNameOverride(transitionState.nameOverrides, transitionName, keyAt);
            }
        }
    }

    private static void setNameOverrides(TransitionState transitionState, ArrayList<String> arrayList, ArrayList<String> arrayList2) {
        if (arrayList != null) {
            for (int i = 0; i < arrayList.size(); i++) {
                setNameOverride(transitionState.nameOverrides, arrayList.get(i), arrayList2.get(i));
            }
        }
    }

    @Override // android.support.v4.app.FragmentTransaction
    public FragmentTransaction add(int i, Fragment fragment, String str) {
        doAddOp(i, fragment, str, 1);
        return this;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addOp(Op op) {
        if (this.mHead == null) {
            this.mTail = op;
            this.mHead = op;
        } else {
            op.prev = this.mTail;
            this.mTail.next = op;
            this.mTail = op;
        }
        op.enterAnim = this.mEnterAnim;
        op.exitAnim = this.mExitAnim;
        op.popEnterAnim = this.mPopEnterAnim;
        op.popExitAnim = this.mPopExitAnim;
        this.mNumOp++;
    }

    @Override // android.support.v4.app.FragmentTransaction
    public FragmentTransaction attach(Fragment fragment) {
        Op op = new Op();
        op.cmd = 7;
        op.fragment = fragment;
        addOp(op);
        return this;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void bumpBackStackNesting(int i) {
        if (!this.mAddToBackStack) {
            return;
        }
        if (FragmentManagerImpl.DEBUG) {
            Log.v("FragmentManager", "Bump nesting in " + this + " by " + i);
        }
        Op op = this.mHead;
        while (true) {
            Op op2 = op;
            if (op2 == null) {
                return;
            }
            if (op2.fragment != null) {
                op2.fragment.mBackStackNesting += i;
                if (FragmentManagerImpl.DEBUG) {
                    Log.v("FragmentManager", "Bump nesting of " + op2.fragment + " to " + op2.fragment.mBackStackNesting);
                }
            }
            if (op2.removed != null) {
                for (int size = op2.removed.size() - 1; size >= 0; size--) {
                    Fragment fragment = op2.removed.get(size);
                    fragment.mBackStackNesting += i;
                    if (FragmentManagerImpl.DEBUG) {
                        Log.v("FragmentManager", "Bump nesting of " + fragment + " to " + fragment.mBackStackNesting);
                    }
                }
            }
            op = op2.next;
        }
    }

    public void calculateBackFragments(SparseArray<Fragment> sparseArray, SparseArray<Fragment> sparseArray2) {
        if (!this.mManager.mContainer.onHasView()) {
            return;
        }
        Op op = this.mTail;
        while (true) {
            Op op2 = op;
            if (op2 == null) {
                return;
            }
            switch (op2.cmd) {
                case 1:
                    setFirstOut(sparseArray, sparseArray2, op2.fragment);
                    break;
                case 2:
                    if (op2.removed != null) {
                        for (int size = op2.removed.size() - 1; size >= 0; size--) {
                            setLastIn(sparseArray, sparseArray2, op2.removed.get(size));
                        }
                    }
                    setFirstOut(sparseArray, sparseArray2, op2.fragment);
                    break;
                case 3:
                    setLastIn(sparseArray, sparseArray2, op2.fragment);
                    break;
                case 4:
                    setLastIn(sparseArray, sparseArray2, op2.fragment);
                    break;
                case 5:
                    setFirstOut(sparseArray, sparseArray2, op2.fragment);
                    break;
                case 6:
                    setLastIn(sparseArray, sparseArray2, op2.fragment);
                    break;
                case 7:
                    setFirstOut(sparseArray, sparseArray2, op2.fragment);
                    break;
            }
            op = op2.prev;
        }
    }

    @Override // android.support.v4.app.FragmentTransaction
    public int commit() {
        return commitInternal(false);
    }

    int commitInternal(boolean z) {
        if (this.mCommitted) {
            throw new IllegalStateException("commit already called");
        }
        if (FragmentManagerImpl.DEBUG) {
            Log.v("FragmentManager", "Commit: " + this);
            dump("  ", null, new PrintWriter(new LogWriter("FragmentManager")), null);
        }
        this.mCommitted = true;
        if (this.mAddToBackStack) {
            this.mIndex = this.mManager.allocBackStackIndex(this);
        } else {
            this.mIndex = -1;
        }
        this.mManager.enqueueAction(this, z);
        return this.mIndex;
    }

    @Override // android.support.v4.app.FragmentTransaction
    public FragmentTransaction detach(Fragment fragment) {
        Op op = new Op();
        op.cmd = 6;
        op.fragment = fragment;
        addOp(op);
        return this;
    }

    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        dump(str, printWriter, true);
    }

    public void dump(String str, PrintWriter printWriter, boolean z) {
        String str2;
        if (z) {
            printWriter.print(str);
            printWriter.print("mName=");
            printWriter.print(this.mName);
            printWriter.print(" mIndex=");
            printWriter.print(this.mIndex);
            printWriter.print(" mCommitted=");
            printWriter.println(this.mCommitted);
            if (this.mTransition != 0) {
                printWriter.print(str);
                printWriter.print("mTransition=#");
                printWriter.print(Integer.toHexString(this.mTransition));
                printWriter.print(" mTransitionStyle=#");
                printWriter.println(Integer.toHexString(this.mTransitionStyle));
            }
            if (this.mEnterAnim != 0 || this.mExitAnim != 0) {
                printWriter.print(str);
                printWriter.print("mEnterAnim=#");
                printWriter.print(Integer.toHexString(this.mEnterAnim));
                printWriter.print(" mExitAnim=#");
                printWriter.println(Integer.toHexString(this.mExitAnim));
            }
            if (this.mPopEnterAnim != 0 || this.mPopExitAnim != 0) {
                printWriter.print(str);
                printWriter.print("mPopEnterAnim=#");
                printWriter.print(Integer.toHexString(this.mPopEnterAnim));
                printWriter.print(" mPopExitAnim=#");
                printWriter.println(Integer.toHexString(this.mPopExitAnim));
            }
            if (this.mBreadCrumbTitleRes != 0 || this.mBreadCrumbTitleText != null) {
                printWriter.print(str);
                printWriter.print("mBreadCrumbTitleRes=#");
                printWriter.print(Integer.toHexString(this.mBreadCrumbTitleRes));
                printWriter.print(" mBreadCrumbTitleText=");
                printWriter.println(this.mBreadCrumbTitleText);
            }
            if (this.mBreadCrumbShortTitleRes != 0 || this.mBreadCrumbShortTitleText != null) {
                printWriter.print(str);
                printWriter.print("mBreadCrumbShortTitleRes=#");
                printWriter.print(Integer.toHexString(this.mBreadCrumbShortTitleRes));
                printWriter.print(" mBreadCrumbShortTitleText=");
                printWriter.println(this.mBreadCrumbShortTitleText);
            }
        }
        if (this.mHead != null) {
            printWriter.print(str);
            printWriter.println("Operations:");
            String str3 = str + "    ";
            Op op = this.mHead;
            int i = 0;
            while (op != null) {
                switch (op.cmd) {
                    case 0:
                        str2 = "NULL";
                        break;
                    case 1:
                        str2 = "ADD";
                        break;
                    case 2:
                        str2 = "REPLACE";
                        break;
                    case 3:
                        str2 = "REMOVE";
                        break;
                    case 4:
                        str2 = "HIDE";
                        break;
                    case 5:
                        str2 = "SHOW";
                        break;
                    case 6:
                        str2 = "DETACH";
                        break;
                    case 7:
                        str2 = "ATTACH";
                        break;
                    default:
                        str2 = "cmd=" + op.cmd;
                        break;
                }
                printWriter.print(str);
                printWriter.print("  Op #");
                printWriter.print(i);
                printWriter.print(": ");
                printWriter.print(str2);
                printWriter.print(" ");
                printWriter.println(op.fragment);
                if (z) {
                    if (op.enterAnim != 0 || op.exitAnim != 0) {
                        printWriter.print(str);
                        printWriter.print("enterAnim=#");
                        printWriter.print(Integer.toHexString(op.enterAnim));
                        printWriter.print(" exitAnim=#");
                        printWriter.println(Integer.toHexString(op.exitAnim));
                    }
                    if (op.popEnterAnim != 0 || op.popExitAnim != 0) {
                        printWriter.print(str);
                        printWriter.print("popEnterAnim=#");
                        printWriter.print(Integer.toHexString(op.popEnterAnim));
                        printWriter.print(" popExitAnim=#");
                        printWriter.println(Integer.toHexString(op.popExitAnim));
                    }
                }
                if (op.removed != null && op.removed.size() > 0) {
                    for (int i2 = 0; i2 < op.removed.size(); i2++) {
                        printWriter.print(str3);
                        if (op.removed.size() == 1) {
                            printWriter.print("Removed: ");
                        } else {
                            if (i2 == 0) {
                                printWriter.println("Removed:");
                            }
                            printWriter.print(str3);
                            printWriter.print("  #");
                            printWriter.print(i2);
                            printWriter.print(": ");
                        }
                        printWriter.println(op.removed.get(i2));
                    }
                }
                op = op.next;
                i++;
            }
        }
    }

    public String getName() {
        return this.mName;
    }

    /* JADX WARN: Code restructure failed: missing block: B:14:0x0065, code lost:
        if (r13.size() != 0) goto L14;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public TransitionState popFromBackStack(boolean z, TransitionState transitionState, SparseArray<Fragment> sparseArray, SparseArray<Fragment> sparseArray2) {
        if (FragmentManagerImpl.DEBUG) {
            Log.v("FragmentManager", "popFromBackStack: " + this);
            dump("  ", null, new PrintWriter(new LogWriter("FragmentManager")), null);
        }
        TransitionState transitionState2 = transitionState;
        if (SUPPORTS_TRANSITIONS) {
            transitionState2 = transitionState;
            if (this.mManager.mCurState >= 1) {
                if (transitionState == null) {
                    if (sparseArray.size() == 0) {
                        transitionState2 = transitionState;
                    }
                    transitionState2 = beginTransition(sparseArray, sparseArray2, true);
                } else {
                    transitionState2 = transitionState;
                    if (!z) {
                        setNameOverrides(transitionState, this.mSharedElementTargetNames, this.mSharedElementSourceNames);
                        transitionState2 = transitionState;
                    }
                }
            }
        }
        bumpBackStackNesting(-1);
        int i = transitionState2 != null ? 0 : this.mTransitionStyle;
        int i2 = transitionState2 != null ? 0 : this.mTransition;
        Op op = this.mTail;
        while (true) {
            Op op2 = op;
            if (op2 == null) {
                if (z) {
                    this.mManager.moveToState(this.mManager.mCurState, FragmentManagerImpl.reverseTransit(i2), i, true);
                    transitionState2 = null;
                }
                if (this.mIndex >= 0) {
                    this.mManager.freeBackStackIndex(this.mIndex);
                    this.mIndex = -1;
                }
                return transitionState2;
            }
            int i3 = transitionState2 != null ? 0 : op2.popEnterAnim;
            int i4 = transitionState2 != null ? 0 : op2.popExitAnim;
            switch (op2.cmd) {
                case 1:
                    Fragment fragment = op2.fragment;
                    fragment.mNextAnim = i4;
                    this.mManager.removeFragment(fragment, FragmentManagerImpl.reverseTransit(i2), i);
                    break;
                case 2:
                    Fragment fragment2 = op2.fragment;
                    if (fragment2 != null) {
                        fragment2.mNextAnim = i4;
                        this.mManager.removeFragment(fragment2, FragmentManagerImpl.reverseTransit(i2), i);
                    }
                    if (op2.removed != null) {
                        for (int i5 = 0; i5 < op2.removed.size(); i5++) {
                            Fragment fragment3 = op2.removed.get(i5);
                            fragment3.mNextAnim = i3;
                            this.mManager.addFragment(fragment3, false);
                        }
                        break;
                    } else {
                        break;
                    }
                case 3:
                    Fragment fragment4 = op2.fragment;
                    fragment4.mNextAnim = i3;
                    this.mManager.addFragment(fragment4, false);
                    break;
                case 4:
                    Fragment fragment5 = op2.fragment;
                    fragment5.mNextAnim = i3;
                    this.mManager.showFragment(fragment5, FragmentManagerImpl.reverseTransit(i2), i);
                    break;
                case 5:
                    Fragment fragment6 = op2.fragment;
                    fragment6.mNextAnim = i4;
                    this.mManager.hideFragment(fragment6, FragmentManagerImpl.reverseTransit(i2), i);
                    break;
                case 6:
                    Fragment fragment7 = op2.fragment;
                    fragment7.mNextAnim = i3;
                    this.mManager.attachFragment(fragment7, FragmentManagerImpl.reverseTransit(i2), i);
                    break;
                case 7:
                    Fragment fragment8 = op2.fragment;
                    fragment8.mNextAnim = i3;
                    this.mManager.detachFragment(fragment8, FragmentManagerImpl.reverseTransit(i2), i);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown cmd: " + op2.cmd);
            }
            op = op2.prev;
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        if (FragmentManagerImpl.DEBUG) {
            Log.v("FragmentManager", "Run: " + this);
        }
        if (this.mAddToBackStack && this.mIndex < 0) {
            throw new IllegalStateException("addToBackStack() called after commit()");
        }
        bumpBackStackNesting(1);
        TransitionState transitionState = null;
        if (SUPPORTS_TRANSITIONS) {
            transitionState = null;
            if (this.mManager.mCurState >= 1) {
                SparseArray<Fragment> sparseArray = new SparseArray<>();
                SparseArray<Fragment> sparseArray2 = new SparseArray<>();
                calculateFragments(sparseArray, sparseArray2);
                transitionState = beginTransition(sparseArray, sparseArray2, false);
            }
        }
        int i = transitionState != null ? 0 : this.mTransitionStyle;
        int i2 = transitionState != null ? 0 : this.mTransition;
        Op op = this.mHead;
        while (true) {
            Op op2 = op;
            if (op2 == null) {
                this.mManager.moveToState(this.mManager.mCurState, i2, i, true);
                if (this.mAddToBackStack) {
                    this.mManager.addBackStackState(this);
                    return;
                }
                return;
            }
            int i3 = transitionState != null ? 0 : op2.enterAnim;
            int i4 = transitionState != null ? 0 : op2.exitAnim;
            switch (op2.cmd) {
                case 1:
                    Fragment fragment = op2.fragment;
                    fragment.mNextAnim = i3;
                    this.mManager.addFragment(fragment, false);
                    break;
                case 2:
                    Fragment fragment2 = op2.fragment;
                    int i5 = fragment2.mContainerId;
                    Fragment fragment3 = fragment2;
                    if (this.mManager.mAdded != null) {
                        int size = this.mManager.mAdded.size() - 1;
                        while (true) {
                            fragment3 = fragment2;
                            if (size >= 0) {
                                Fragment fragment4 = this.mManager.mAdded.get(size);
                                if (FragmentManagerImpl.DEBUG) {
                                    Log.v("FragmentManager", "OP_REPLACE: adding=" + fragment2 + " old=" + fragment4);
                                }
                                Fragment fragment5 = fragment2;
                                if (fragment4.mContainerId == i5) {
                                    if (fragment4 == fragment2) {
                                        fragment5 = null;
                                        op2.fragment = null;
                                    } else {
                                        if (op2.removed == null) {
                                            op2.removed = new ArrayList<>();
                                        }
                                        op2.removed.add(fragment4);
                                        fragment4.mNextAnim = i4;
                                        if (this.mAddToBackStack) {
                                            fragment4.mBackStackNesting++;
                                            if (FragmentManagerImpl.DEBUG) {
                                                Log.v("FragmentManager", "Bump nesting of " + fragment4 + " to " + fragment4.mBackStackNesting);
                                            }
                                        }
                                        this.mManager.removeFragment(fragment4, i2, i);
                                        fragment5 = fragment2;
                                    }
                                }
                                size--;
                                fragment2 = fragment5;
                            }
                        }
                    }
                    if (fragment3 == null) {
                        break;
                    } else {
                        fragment3.mNextAnim = i3;
                        this.mManager.addFragment(fragment3, false);
                        break;
                    }
                case 3:
                    Fragment fragment6 = op2.fragment;
                    fragment6.mNextAnim = i4;
                    this.mManager.removeFragment(fragment6, i2, i);
                    break;
                case 4:
                    Fragment fragment7 = op2.fragment;
                    fragment7.mNextAnim = i4;
                    this.mManager.hideFragment(fragment7, i2, i);
                    break;
                case 5:
                    Fragment fragment8 = op2.fragment;
                    fragment8.mNextAnim = i3;
                    this.mManager.showFragment(fragment8, i2, i);
                    break;
                case 6:
                    Fragment fragment9 = op2.fragment;
                    fragment9.mNextAnim = i4;
                    this.mManager.detachFragment(fragment9, i2, i);
                    break;
                case 7:
                    Fragment fragment10 = op2.fragment;
                    fragment10.mNextAnim = i3;
                    this.mManager.attachFragment(fragment10, i2, i);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown cmd: " + op2.cmd);
            }
            op = op2.next;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("BackStackEntry{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        if (this.mIndex >= 0) {
            sb.append(" #");
            sb.append(this.mIndex);
        }
        if (this.mName != null) {
            sb.append(" ");
            sb.append(this.mName);
        }
        sb.append("}");
        return sb.toString();
    }
}
