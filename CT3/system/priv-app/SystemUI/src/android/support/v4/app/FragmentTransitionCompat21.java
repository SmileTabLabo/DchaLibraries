package android.support.v4.app;

import android.graphics.Rect;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/* loaded from: a.zip:android/support/v4/app/FragmentTransitionCompat21.class */
class FragmentTransitionCompat21 {

    /* loaded from: a.zip:android/support/v4/app/FragmentTransitionCompat21$EpicenterView.class */
    public static class EpicenterView {
        public View epicenter;
    }

    /* loaded from: a.zip:android/support/v4/app/FragmentTransitionCompat21$ViewRetriever.class */
    public interface ViewRetriever {
        View getView();
    }

    FragmentTransitionCompat21() {
    }

    public static void addTargets(Object obj, ArrayList<View> arrayList) {
        Transition transition = (Transition) obj;
        if (transition instanceof TransitionSet) {
            TransitionSet transitionSet = (TransitionSet) transition;
            int transitionCount = transitionSet.getTransitionCount();
            for (int i = 0; i < transitionCount; i++) {
                addTargets(transitionSet.getTransitionAt(i), arrayList);
            }
        } else if (!hasSimpleTarget(transition) && isNullOrEmpty(transition.getTargets())) {
            int size = arrayList.size();
            for (int i2 = 0; i2 < size; i2++) {
                transition.addTarget(arrayList.get(i2));
            }
        }
    }

    public static void addTransitionTargets(Object obj, Object obj2, Object obj3, View view, ViewRetriever viewRetriever, View view2, EpicenterView epicenterView, Map<String, String> map, ArrayList<View> arrayList, ArrayList<View> arrayList2, Map<String, View> map2, Map<String, View> map3, ArrayList<View> arrayList3) {
        Transition transition = (Transition) obj;
        Transition transition2 = (Transition) obj3;
        Transition transition3 = (Transition) obj2;
        excludeViews(transition, transition2, arrayList2, true);
        if (obj == null && obj2 == null) {
            return;
        }
        if (transition != null) {
            transition.addTarget(view2);
        }
        if (obj2 != null) {
            setSharedElementTargets(transition3, view2, map2, arrayList3);
            excludeViews(transition, transition3, arrayList3, true);
            excludeViews(transition2, transition3, arrayList3, true);
        }
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(view, transition, view2, viewRetriever, map, map3, arrayList, transition2) { // from class: android.support.v4.app.FragmentTransitionCompat21.2
            final View val$container;
            final Transition val$enterTransition;
            final ArrayList val$enteringViews;
            final Transition val$exitTransition;
            final ViewRetriever val$inFragment;
            final Map val$nameOverrides;
            final View val$nonExistentView;
            final Map val$renamedViews;

            {
                this.val$container = view;
                this.val$enterTransition = transition;
                this.val$nonExistentView = view2;
                this.val$inFragment = viewRetriever;
                this.val$nameOverrides = map;
                this.val$renamedViews = map3;
                this.val$enteringViews = arrayList;
                this.val$exitTransition = transition2;
            }

            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                View view3;
                this.val$container.getViewTreeObserver().removeOnPreDrawListener(this);
                if (this.val$enterTransition != null) {
                    this.val$enterTransition.removeTarget(this.val$nonExistentView);
                }
                if (this.val$inFragment != null && (view3 = this.val$inFragment.getView()) != null) {
                    if (!this.val$nameOverrides.isEmpty()) {
                        FragmentTransitionCompat21.findNamedViews(this.val$renamedViews, view3);
                        this.val$renamedViews.keySet().retainAll(this.val$nameOverrides.values());
                        for (Map.Entry entry : this.val$nameOverrides.entrySet()) {
                            View view4 = (View) this.val$renamedViews.get((String) entry.getValue());
                            if (view4 != null) {
                                view4.setTransitionName((String) entry.getKey());
                            }
                        }
                    }
                    if (this.val$enterTransition != null) {
                        FragmentTransitionCompat21.captureTransitioningViews(this.val$enteringViews, view3);
                        this.val$enteringViews.removeAll(this.val$renamedViews.values());
                        this.val$enteringViews.add(this.val$nonExistentView);
                        FragmentTransitionCompat21.addTargets(this.val$enterTransition, this.val$enteringViews);
                    }
                }
                FragmentTransitionCompat21.excludeViews(this.val$exitTransition, this.val$enterTransition, this.val$enteringViews, true);
                return true;
            }
        });
        setSharedElementEpicenter(transition, epicenterView);
    }

    public static void beginDelayedTransition(ViewGroup viewGroup, Object obj) {
        TransitionManager.beginDelayedTransition(viewGroup, (Transition) obj);
    }

    private static void bfsAddViewChildren(List<View> list, View view) {
        int size = list.size();
        if (containedBeforeIndex(list, view, size)) {
            return;
        }
        list.add(view);
        for (int i = size; i < list.size(); i++) {
            View view2 = list.get(i);
            if (view2 instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view2;
                int childCount = viewGroup.getChildCount();
                for (int i2 = 0; i2 < childCount; i2++) {
                    View childAt = viewGroup.getChildAt(i2);
                    if (!containedBeforeIndex(list, childAt, size)) {
                        list.add(childAt);
                    }
                }
            }
        }
    }

    public static Object captureExitingViews(Object obj, View view, ArrayList<View> arrayList, Map<String, View> map, View view2) {
        Object obj2 = obj;
        if (obj != null) {
            captureTransitioningViews(arrayList, view);
            if (map != null) {
                arrayList.removeAll(map.values());
            }
            if (arrayList.isEmpty()) {
                obj2 = null;
            } else {
                arrayList.add(view2);
                addTargets((Transition) obj, arrayList);
                obj2 = obj;
            }
        }
        return obj2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void captureTransitioningViews(ArrayList<View> arrayList, View view) {
        if (view.getVisibility() == 0) {
            if (!(view instanceof ViewGroup)) {
                arrayList.add(view);
                return;
            }
            ViewGroup viewGroup = (ViewGroup) view;
            if (viewGroup.isTransitionGroup()) {
                arrayList.add(viewGroup);
                return;
            }
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                captureTransitioningViews(arrayList, viewGroup.getChildAt(i));
            }
        }
    }

    public static void cleanupTransitions(View view, View view2, Object obj, ArrayList<View> arrayList, Object obj2, ArrayList<View> arrayList2, Object obj3, ArrayList<View> arrayList3, Object obj4, ArrayList<View> arrayList4, Map<String, View> map) {
        Transition transition = (Transition) obj;
        Transition transition2 = (Transition) obj2;
        Transition transition3 = (Transition) obj3;
        Transition transition4 = (Transition) obj4;
        if (transition4 != null) {
            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(view, transition, arrayList, transition2, arrayList2, transition3, arrayList3, map, arrayList4, transition4, view2) { // from class: android.support.v4.app.FragmentTransitionCompat21.4
                final Transition val$enterTransition;
                final ArrayList val$enteringViews;
                final Transition val$exitTransition;
                final ArrayList val$exitingViews;
                final ArrayList val$hiddenViews;
                final View val$nonExistentView;
                final Transition val$overallTransition;
                final Map val$renamedViews;
                final View val$sceneRoot;
                final ArrayList val$sharedElementTargets;
                final Transition val$sharedElementTransition;

                {
                    this.val$sceneRoot = view;
                    this.val$enterTransition = transition;
                    this.val$enteringViews = arrayList;
                    this.val$exitTransition = transition2;
                    this.val$exitingViews = arrayList2;
                    this.val$sharedElementTransition = transition3;
                    this.val$sharedElementTargets = arrayList3;
                    this.val$renamedViews = map;
                    this.val$hiddenViews = arrayList4;
                    this.val$overallTransition = transition4;
                    this.val$nonExistentView = view2;
                }

                @Override // android.view.ViewTreeObserver.OnPreDrawListener
                public boolean onPreDraw() {
                    this.val$sceneRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (this.val$enterTransition != null) {
                        FragmentTransitionCompat21.removeTargets(this.val$enterTransition, this.val$enteringViews);
                        FragmentTransitionCompat21.excludeViews(this.val$enterTransition, this.val$exitTransition, this.val$exitingViews, false);
                        FragmentTransitionCompat21.excludeViews(this.val$enterTransition, this.val$sharedElementTransition, this.val$sharedElementTargets, false);
                    }
                    if (this.val$exitTransition != null) {
                        FragmentTransitionCompat21.removeTargets(this.val$exitTransition, this.val$exitingViews);
                        FragmentTransitionCompat21.excludeViews(this.val$exitTransition, this.val$enterTransition, this.val$enteringViews, false);
                        FragmentTransitionCompat21.excludeViews(this.val$exitTransition, this.val$sharedElementTransition, this.val$sharedElementTargets, false);
                    }
                    if (this.val$sharedElementTransition != null) {
                        FragmentTransitionCompat21.removeTargets(this.val$sharedElementTransition, this.val$sharedElementTargets);
                    }
                    for (Map.Entry entry : this.val$renamedViews.entrySet()) {
                        ((View) entry.getValue()).setTransitionName((String) entry.getKey());
                    }
                    int size = this.val$hiddenViews.size();
                    for (int i = 0; i < size; i++) {
                        this.val$overallTransition.excludeTarget((View) this.val$hiddenViews.get(i), false);
                    }
                    this.val$overallTransition.excludeTarget(this.val$nonExistentView, false);
                    return true;
                }
            });
        }
    }

    public static Object cloneTransition(Object obj) {
        Transition transition = obj;
        if (obj != null) {
            transition = ((Transition) obj).clone();
        }
        return transition;
    }

    private static boolean containedBeforeIndex(List<View> list, View view, int i) {
        for (int i2 = 0; i2 < i; i2++) {
            if (list.get(i2) == view) {
                return true;
            }
        }
        return false;
    }

    public static void excludeSharedElementViews(Object obj, Object obj2, Object obj3, ArrayList<View> arrayList, boolean z) {
        Transition transition = (Transition) obj3;
        excludeViews((Transition) obj, transition, arrayList, z);
        excludeViews((Transition) obj2, transition, arrayList, z);
    }

    public static void excludeTarget(Object obj, View view, boolean z) {
        ((Transition) obj).excludeTarget(view, z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void excludeViews(Transition transition, Transition transition2, ArrayList<View> arrayList, boolean z) {
        if (transition != null) {
            int size = transition2 == null ? 0 : arrayList.size();
            for (int i = 0; i < size; i++) {
                transition.excludeTarget(arrayList.get(i), z);
            }
        }
    }

    public static void findNamedViews(Map<String, View> map, View view) {
        if (view.getVisibility() == 0) {
            String transitionName = view.getTransitionName();
            if (transitionName != null) {
                map.put(transitionName, view);
            }
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    findNamedViews(map, viewGroup.getChildAt(i));
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Rect getBoundsOnScreen(View view) {
        Rect rect = new Rect();
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        rect.set(iArr[0], iArr[1], iArr[0] + view.getWidth(), iArr[1] + view.getHeight());
        return rect;
    }

    public static String getTransitionName(View view) {
        return view.getTransitionName();
    }

    private static boolean hasSimpleTarget(Transition transition) {
        boolean z = true;
        if (isNullOrEmpty(transition.getTargetIds())) {
            z = true;
            if (isNullOrEmpty(transition.getTargetNames())) {
                z = true;
                if (isNullOrEmpty(transition.getTargetTypes())) {
                    z = false;
                }
            }
        }
        return z;
    }

    private static boolean isNullOrEmpty(List list) {
        return list != null ? list.isEmpty() : true;
    }

    public static Object mergeTransitions(Object obj, Object obj2, Object obj3, boolean z) {
        Transition transition = (Transition) obj;
        TransitionSet transitionSet = (Transition) obj2;
        Transition transition2 = (Transition) obj3;
        boolean z2 = true;
        if (transition != null) {
            z2 = true;
            if (transitionSet != null) {
                z2 = z;
            }
        }
        if (z2) {
            TransitionSet transitionSet2 = new TransitionSet();
            if (transition != null) {
                transitionSet2.addTransition(transition);
            }
            if (transitionSet != null) {
                transitionSet2.addTransition(transitionSet);
            }
            if (transition2 != null) {
                transitionSet2.addTransition(transition2);
            }
            transitionSet = transitionSet2;
        } else {
            if (transitionSet != null && transition != null) {
                transitionSet = new TransitionSet().addTransition(transitionSet).addTransition(transition).setOrdering(1);
            } else if (transitionSet == null) {
                transitionSet = null;
                if (transition != null) {
                    transitionSet = transition;
                }
            }
            if (transition2 != null) {
                TransitionSet transitionSet3 = new TransitionSet();
                if (transitionSet != null) {
                    transitionSet3.addTransition(transitionSet);
                }
                transitionSet3.addTransition(transition2);
                transitionSet = transitionSet3;
            }
        }
        return transitionSet;
    }

    public static void removeTargets(Object obj, ArrayList<View> arrayList) {
        List<View> targets;
        Transition transition = (Transition) obj;
        if (transition instanceof TransitionSet) {
            TransitionSet transitionSet = (TransitionSet) transition;
            int transitionCount = transitionSet.getTransitionCount();
            for (int i = 0; i < transitionCount; i++) {
                removeTargets(transitionSet.getTransitionAt(i), arrayList);
            }
        } else if (!hasSimpleTarget(transition) && (targets = transition.getTargets()) != null && targets.size() == arrayList.size() && targets.containsAll(arrayList)) {
            for (int size = arrayList.size() - 1; size >= 0; size--) {
                transition.removeTarget(arrayList.get(size));
            }
        }
    }

    public static void setEpicenter(Object obj, View view) {
        ((Transition) obj).setEpicenterCallback(new Transition.EpicenterCallback(getBoundsOnScreen(view)) { // from class: android.support.v4.app.FragmentTransitionCompat21.1
            final Rect val$epicenter;

            {
                this.val$epicenter = r4;
            }

            @Override // android.transition.Transition.EpicenterCallback
            public Rect onGetEpicenter(Transition transition) {
                return this.val$epicenter;
            }
        });
    }

    private static void setSharedElementEpicenter(Transition transition, EpicenterView epicenterView) {
        if (transition != null) {
            transition.setEpicenterCallback(new Transition.EpicenterCallback(epicenterView) { // from class: android.support.v4.app.FragmentTransitionCompat21.3
                private Rect mEpicenter;
                final EpicenterView val$epicenterView;

                {
                    this.val$epicenterView = epicenterView;
                }

                @Override // android.transition.Transition.EpicenterCallback
                public Rect onGetEpicenter(Transition transition2) {
                    if (this.mEpicenter == null && this.val$epicenterView.epicenter != null) {
                        this.mEpicenter = FragmentTransitionCompat21.getBoundsOnScreen(this.val$epicenterView.epicenter);
                    }
                    return this.mEpicenter;
                }
            });
        }
    }

    public static void setSharedElementTargets(Object obj, View view, Map<String, View> map, ArrayList<View> arrayList) {
        TransitionSet transitionSet = (TransitionSet) obj;
        arrayList.clear();
        arrayList.addAll(map.values());
        List<View> targets = transitionSet.getTargets();
        targets.clear();
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            bfsAddViewChildren(targets, arrayList.get(i));
        }
        arrayList.add(view);
        addTargets(transitionSet, arrayList);
    }

    public static Object wrapSharedElementTransition(Object obj) {
        Transition transition;
        if (obj == null || (transition = (Transition) obj) == null) {
            return null;
        }
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(transition);
        return transitionSet;
    }
}
