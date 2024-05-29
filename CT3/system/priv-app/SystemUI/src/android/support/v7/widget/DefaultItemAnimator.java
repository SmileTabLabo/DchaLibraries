package android.support.v7.widget;

import android.support.annotation.NonNull;
import android.support.v4.animation.AnimatorCompatHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:android/support/v7/widget/DefaultItemAnimator.class */
public class DefaultItemAnimator extends SimpleItemAnimator {
    private ArrayList<RecyclerView.ViewHolder> mPendingRemovals = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> mPendingAdditions = new ArrayList<>();
    private ArrayList<MoveInfo> mPendingMoves = new ArrayList<>();
    private ArrayList<ChangeInfo> mPendingChanges = new ArrayList<>();
    private ArrayList<ArrayList<RecyclerView.ViewHolder>> mAdditionsList = new ArrayList<>();
    private ArrayList<ArrayList<MoveInfo>> mMovesList = new ArrayList<>();
    private ArrayList<ArrayList<ChangeInfo>> mChangesList = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> mAddAnimations = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> mMoveAnimations = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> mRemoveAnimations = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> mChangeAnimations = new ArrayList<>();

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/DefaultItemAnimator$ChangeInfo.class */
    public static class ChangeInfo {
        public int fromX;
        public int fromY;
        public RecyclerView.ViewHolder newHolder;
        public RecyclerView.ViewHolder oldHolder;
        public int toX;
        public int toY;

        private ChangeInfo(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            this.oldHolder = viewHolder;
            this.newHolder = viewHolder2;
        }

        private ChangeInfo(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2, int i, int i2, int i3, int i4) {
            this(viewHolder, viewHolder2);
            this.fromX = i;
            this.fromY = i2;
            this.toX = i3;
            this.toY = i4;
        }

        /* synthetic */ ChangeInfo(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2, int i, int i2, int i3, int i4, ChangeInfo changeInfo) {
            this(viewHolder, viewHolder2, i, i2, i3, i4);
        }

        public String toString() {
            return "ChangeInfo{oldHolder=" + this.oldHolder + ", newHolder=" + this.newHolder + ", fromX=" + this.fromX + ", fromY=" + this.fromY + ", toX=" + this.toX + ", toY=" + this.toY + '}';
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/DefaultItemAnimator$MoveInfo.class */
    public static class MoveInfo {
        public int fromX;
        public int fromY;
        public RecyclerView.ViewHolder holder;
        public int toX;
        public int toY;

        private MoveInfo(RecyclerView.ViewHolder viewHolder, int i, int i2, int i3, int i4) {
            this.holder = viewHolder;
            this.fromX = i;
            this.fromY = i2;
            this.toX = i3;
            this.toY = i4;
        }

        /* synthetic */ MoveInfo(RecyclerView.ViewHolder viewHolder, int i, int i2, int i3, int i4, MoveInfo moveInfo) {
            this(viewHolder, i, i2, i3, i4);
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/DefaultItemAnimator$VpaListenerAdapter.class */
    private static class VpaListenerAdapter implements ViewPropertyAnimatorListener {
        private VpaListenerAdapter() {
        }

        /* synthetic */ VpaListenerAdapter(VpaListenerAdapter vpaListenerAdapter) {
            this();
        }

        @Override // android.support.v4.view.ViewPropertyAnimatorListener
        public void onAnimationCancel(View view) {
        }

        @Override // android.support.v4.view.ViewPropertyAnimatorListener
        public void onAnimationEnd(View view) {
        }

        @Override // android.support.v4.view.ViewPropertyAnimatorListener
        public void onAnimationStart(View view) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateAddImpl(RecyclerView.ViewHolder viewHolder) {
        ViewPropertyAnimatorCompat animate = ViewCompat.animate(viewHolder.itemView);
        this.mAddAnimations.add(viewHolder);
        animate.alpha(1.0f).setDuration(getAddDuration()).setListener(new VpaListenerAdapter(this, viewHolder, animate) { // from class: android.support.v7.widget.DefaultItemAnimator.5
            final DefaultItemAnimator this$0;
            final ViewPropertyAnimatorCompat val$animation;
            final RecyclerView.ViewHolder val$holder;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(null);
                this.this$0 = this;
                this.val$holder = viewHolder;
                this.val$animation = animate;
            }

            @Override // android.support.v7.widget.DefaultItemAnimator.VpaListenerAdapter, android.support.v4.view.ViewPropertyAnimatorListener
            public void onAnimationCancel(View view) {
                ViewCompat.setAlpha(view, 1.0f);
            }

            @Override // android.support.v7.widget.DefaultItemAnimator.VpaListenerAdapter, android.support.v4.view.ViewPropertyAnimatorListener
            public void onAnimationEnd(View view) {
                this.val$animation.setListener(null);
                this.this$0.dispatchAddFinished(this.val$holder);
                this.this$0.mAddAnimations.remove(this.val$holder);
                this.this$0.dispatchFinishedWhenDone();
            }

            @Override // android.support.v7.widget.DefaultItemAnimator.VpaListenerAdapter, android.support.v4.view.ViewPropertyAnimatorListener
            public void onAnimationStart(View view) {
                this.this$0.dispatchAddStarting(this.val$holder);
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateChangeImpl(ChangeInfo changeInfo) {
        RecyclerView.ViewHolder viewHolder = changeInfo.oldHolder;
        View view = viewHolder == null ? null : viewHolder.itemView;
        RecyclerView.ViewHolder viewHolder2 = changeInfo.newHolder;
        View view2 = viewHolder2 != null ? viewHolder2.itemView : null;
        if (view != null) {
            ViewPropertyAnimatorCompat duration = ViewCompat.animate(view).setDuration(getChangeDuration());
            this.mChangeAnimations.add(changeInfo.oldHolder);
            duration.translationX(changeInfo.toX - changeInfo.fromX);
            duration.translationY(changeInfo.toY - changeInfo.fromY);
            duration.alpha(0.0f).setListener(new VpaListenerAdapter(this, changeInfo, duration) { // from class: android.support.v7.widget.DefaultItemAnimator.7
                final DefaultItemAnimator this$0;
                final ChangeInfo val$changeInfo;
                final ViewPropertyAnimatorCompat val$oldViewAnim;

                /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                {
                    super(null);
                    this.this$0 = this;
                    this.val$changeInfo = changeInfo;
                    this.val$oldViewAnim = duration;
                }

                @Override // android.support.v7.widget.DefaultItemAnimator.VpaListenerAdapter, android.support.v4.view.ViewPropertyAnimatorListener
                public void onAnimationEnd(View view3) {
                    this.val$oldViewAnim.setListener(null);
                    ViewCompat.setAlpha(view3, 1.0f);
                    ViewCompat.setTranslationX(view3, 0.0f);
                    ViewCompat.setTranslationY(view3, 0.0f);
                    this.this$0.dispatchChangeFinished(this.val$changeInfo.oldHolder, true);
                    this.this$0.mChangeAnimations.remove(this.val$changeInfo.oldHolder);
                    this.this$0.dispatchFinishedWhenDone();
                }

                @Override // android.support.v7.widget.DefaultItemAnimator.VpaListenerAdapter, android.support.v4.view.ViewPropertyAnimatorListener
                public void onAnimationStart(View view3) {
                    this.this$0.dispatchChangeStarting(this.val$changeInfo.oldHolder, true);
                }
            }).start();
        }
        if (view2 != null) {
            ViewPropertyAnimatorCompat animate = ViewCompat.animate(view2);
            this.mChangeAnimations.add(changeInfo.newHolder);
            animate.translationX(0.0f).translationY(0.0f).setDuration(getChangeDuration()).alpha(1.0f).setListener(new VpaListenerAdapter(this, changeInfo, animate, view2) { // from class: android.support.v7.widget.DefaultItemAnimator.8
                final DefaultItemAnimator this$0;
                final ChangeInfo val$changeInfo;
                final View val$newView;
                final ViewPropertyAnimatorCompat val$newViewAnimation;

                /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                {
                    super(null);
                    this.this$0 = this;
                    this.val$changeInfo = changeInfo;
                    this.val$newViewAnimation = animate;
                    this.val$newView = view2;
                }

                @Override // android.support.v7.widget.DefaultItemAnimator.VpaListenerAdapter, android.support.v4.view.ViewPropertyAnimatorListener
                public void onAnimationEnd(View view3) {
                    this.val$newViewAnimation.setListener(null);
                    ViewCompat.setAlpha(this.val$newView, 1.0f);
                    ViewCompat.setTranslationX(this.val$newView, 0.0f);
                    ViewCompat.setTranslationY(this.val$newView, 0.0f);
                    this.this$0.dispatchChangeFinished(this.val$changeInfo.newHolder, false);
                    this.this$0.mChangeAnimations.remove(this.val$changeInfo.newHolder);
                    this.this$0.dispatchFinishedWhenDone();
                }

                @Override // android.support.v7.widget.DefaultItemAnimator.VpaListenerAdapter, android.support.v4.view.ViewPropertyAnimatorListener
                public void onAnimationStart(View view3) {
                    this.this$0.dispatchChangeStarting(this.val$changeInfo.newHolder, false);
                }
            }).start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateMoveImpl(RecyclerView.ViewHolder viewHolder, int i, int i2, int i3, int i4) {
        View view = viewHolder.itemView;
        int i5 = i3 - i;
        int i6 = i4 - i2;
        if (i5 != 0) {
            ViewCompat.animate(view).translationX(0.0f);
        }
        if (i6 != 0) {
            ViewCompat.animate(view).translationY(0.0f);
        }
        ViewPropertyAnimatorCompat animate = ViewCompat.animate(view);
        this.mMoveAnimations.add(viewHolder);
        animate.setDuration(getMoveDuration()).setListener(new VpaListenerAdapter(this, viewHolder, i5, i6, animate) { // from class: android.support.v7.widget.DefaultItemAnimator.6
            final DefaultItemAnimator this$0;
            final ViewPropertyAnimatorCompat val$animation;
            final int val$deltaX;
            final int val$deltaY;
            final RecyclerView.ViewHolder val$holder;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(null);
                this.this$0 = this;
                this.val$holder = viewHolder;
                this.val$deltaX = i5;
                this.val$deltaY = i6;
                this.val$animation = animate;
            }

            @Override // android.support.v7.widget.DefaultItemAnimator.VpaListenerAdapter, android.support.v4.view.ViewPropertyAnimatorListener
            public void onAnimationCancel(View view2) {
                if (this.val$deltaX != 0) {
                    ViewCompat.setTranslationX(view2, 0.0f);
                }
                if (this.val$deltaY != 0) {
                    ViewCompat.setTranslationY(view2, 0.0f);
                }
            }

            @Override // android.support.v7.widget.DefaultItemAnimator.VpaListenerAdapter, android.support.v4.view.ViewPropertyAnimatorListener
            public void onAnimationEnd(View view2) {
                this.val$animation.setListener(null);
                this.this$0.dispatchMoveFinished(this.val$holder);
                this.this$0.mMoveAnimations.remove(this.val$holder);
                this.this$0.dispatchFinishedWhenDone();
            }

            @Override // android.support.v7.widget.DefaultItemAnimator.VpaListenerAdapter, android.support.v4.view.ViewPropertyAnimatorListener
            public void onAnimationStart(View view2) {
                this.this$0.dispatchMoveStarting(this.val$holder);
            }
        }).start();
    }

    private void animateRemoveImpl(RecyclerView.ViewHolder viewHolder) {
        ViewPropertyAnimatorCompat animate = ViewCompat.animate(viewHolder.itemView);
        this.mRemoveAnimations.add(viewHolder);
        animate.setDuration(getRemoveDuration()).alpha(0.0f).setListener(new VpaListenerAdapter(this, viewHolder, animate) { // from class: android.support.v7.widget.DefaultItemAnimator.4
            final DefaultItemAnimator this$0;
            final ViewPropertyAnimatorCompat val$animation;
            final RecyclerView.ViewHolder val$holder;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(null);
                this.this$0 = this;
                this.val$holder = viewHolder;
                this.val$animation = animate;
            }

            @Override // android.support.v7.widget.DefaultItemAnimator.VpaListenerAdapter, android.support.v4.view.ViewPropertyAnimatorListener
            public void onAnimationEnd(View view) {
                this.val$animation.setListener(null);
                ViewCompat.setAlpha(view, 1.0f);
                this.this$0.dispatchRemoveFinished(this.val$holder);
                this.this$0.mRemoveAnimations.remove(this.val$holder);
                this.this$0.dispatchFinishedWhenDone();
            }

            @Override // android.support.v7.widget.DefaultItemAnimator.VpaListenerAdapter, android.support.v4.view.ViewPropertyAnimatorListener
            public void onAnimationStart(View view) {
                this.this$0.dispatchRemoveStarting(this.val$holder);
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchFinishedWhenDone() {
        if (isRunning()) {
            return;
        }
        dispatchAnimationsFinished();
    }

    private void endChangeAnimation(List<ChangeInfo> list, RecyclerView.ViewHolder viewHolder) {
        for (int size = list.size() - 1; size >= 0; size--) {
            ChangeInfo changeInfo = list.get(size);
            if (endChangeAnimationIfNecessary(changeInfo, viewHolder) && changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                list.remove(changeInfo);
            }
        }
    }

    private void endChangeAnimationIfNecessary(ChangeInfo changeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder);
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder);
        }
    }

    private boolean endChangeAnimationIfNecessary(ChangeInfo changeInfo, RecyclerView.ViewHolder viewHolder) {
        boolean z = false;
        if (changeInfo.newHolder == viewHolder) {
            changeInfo.newHolder = null;
        } else if (changeInfo.oldHolder != viewHolder) {
            return false;
        } else {
            changeInfo.oldHolder = null;
            z = true;
        }
        ViewCompat.setAlpha(viewHolder.itemView, 1.0f);
        ViewCompat.setTranslationX(viewHolder.itemView, 0.0f);
        ViewCompat.setTranslationY(viewHolder.itemView, 0.0f);
        dispatchChangeFinished(viewHolder, z);
        return true;
    }

    private void resetAnimation(RecyclerView.ViewHolder viewHolder) {
        AnimatorCompatHelper.clearInterpolator(viewHolder.itemView);
        endAnimation(viewHolder);
    }

    @Override // android.support.v7.widget.SimpleItemAnimator
    public boolean animateAdd(RecyclerView.ViewHolder viewHolder) {
        resetAnimation(viewHolder);
        ViewCompat.setAlpha(viewHolder.itemView, 0.0f);
        this.mPendingAdditions.add(viewHolder);
        return true;
    }

    @Override // android.support.v7.widget.SimpleItemAnimator
    public boolean animateChange(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2, int i, int i2, int i3, int i4) {
        if (viewHolder == viewHolder2) {
            return animateMove(viewHolder, i, i2, i3, i4);
        }
        float translationX = ViewCompat.getTranslationX(viewHolder.itemView);
        float translationY = ViewCompat.getTranslationY(viewHolder.itemView);
        float alpha = ViewCompat.getAlpha(viewHolder.itemView);
        resetAnimation(viewHolder);
        int i5 = (int) ((i3 - i) - translationX);
        int i6 = (int) ((i4 - i2) - translationY);
        ViewCompat.setTranslationX(viewHolder.itemView, translationX);
        ViewCompat.setTranslationY(viewHolder.itemView, translationY);
        ViewCompat.setAlpha(viewHolder.itemView, alpha);
        if (viewHolder2 != null) {
            resetAnimation(viewHolder2);
            ViewCompat.setTranslationX(viewHolder2.itemView, -i5);
            ViewCompat.setTranslationY(viewHolder2.itemView, -i6);
            ViewCompat.setAlpha(viewHolder2.itemView, 0.0f);
        }
        this.mPendingChanges.add(new ChangeInfo(viewHolder, viewHolder2, i, i2, i3, i4, null));
        return true;
    }

    @Override // android.support.v7.widget.SimpleItemAnimator
    public boolean animateMove(RecyclerView.ViewHolder viewHolder, int i, int i2, int i3, int i4) {
        View view = viewHolder.itemView;
        int translationX = (int) (i + ViewCompat.getTranslationX(viewHolder.itemView));
        int translationY = (int) (i2 + ViewCompat.getTranslationY(viewHolder.itemView));
        resetAnimation(viewHolder);
        int i5 = i3 - translationX;
        int i6 = i4 - translationY;
        if (i5 == 0 && i6 == 0) {
            dispatchMoveFinished(viewHolder);
            return false;
        }
        if (i5 != 0) {
            ViewCompat.setTranslationX(view, -i5);
        }
        if (i6 != 0) {
            ViewCompat.setTranslationY(view, -i6);
        }
        this.mPendingMoves.add(new MoveInfo(viewHolder, translationX, translationY, i3, i4, null));
        return true;
    }

    @Override // android.support.v7.widget.SimpleItemAnimator
    public boolean animateRemove(RecyclerView.ViewHolder viewHolder) {
        resetAnimation(viewHolder);
        this.mPendingRemovals.add(viewHolder);
        return true;
    }

    @Override // android.support.v7.widget.RecyclerView.ItemAnimator
    public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> list) {
        return list.isEmpty() ? super.canReuseUpdatedViewHolder(viewHolder, list) : true;
    }

    void cancelAll(List<RecyclerView.ViewHolder> list) {
        for (int size = list.size() - 1; size >= 0; size--) {
            ViewCompat.animate(list.get(size).itemView).cancel();
        }
    }

    @Override // android.support.v7.widget.RecyclerView.ItemAnimator
    public void endAnimation(RecyclerView.ViewHolder viewHolder) {
        View view = viewHolder.itemView;
        ViewCompat.animate(view).cancel();
        for (int size = this.mPendingMoves.size() - 1; size >= 0; size--) {
            if (this.mPendingMoves.get(size).holder == viewHolder) {
                ViewCompat.setTranslationY(view, 0.0f);
                ViewCompat.setTranslationX(view, 0.0f);
                dispatchMoveFinished(viewHolder);
                this.mPendingMoves.remove(size);
            }
        }
        endChangeAnimation(this.mPendingChanges, viewHolder);
        if (this.mPendingRemovals.remove(viewHolder)) {
            ViewCompat.setAlpha(view, 1.0f);
            dispatchRemoveFinished(viewHolder);
        }
        if (this.mPendingAdditions.remove(viewHolder)) {
            ViewCompat.setAlpha(view, 1.0f);
            dispatchAddFinished(viewHolder);
        }
        for (int size2 = this.mChangesList.size() - 1; size2 >= 0; size2--) {
            ArrayList<ChangeInfo> arrayList = this.mChangesList.get(size2);
            endChangeAnimation(arrayList, viewHolder);
            if (arrayList.isEmpty()) {
                this.mChangesList.remove(size2);
            }
        }
        for (int size3 = this.mMovesList.size() - 1; size3 >= 0; size3--) {
            ArrayList<MoveInfo> arrayList2 = this.mMovesList.get(size3);
            int size4 = arrayList2.size() - 1;
            while (true) {
                if (size4 < 0) {
                    break;
                } else if (arrayList2.get(size4).holder == viewHolder) {
                    ViewCompat.setTranslationY(view, 0.0f);
                    ViewCompat.setTranslationX(view, 0.0f);
                    dispatchMoveFinished(viewHolder);
                    arrayList2.remove(size4);
                    if (arrayList2.isEmpty()) {
                        this.mMovesList.remove(size3);
                    }
                } else {
                    size4--;
                }
            }
        }
        for (int size5 = this.mAdditionsList.size() - 1; size5 >= 0; size5--) {
            ArrayList<RecyclerView.ViewHolder> arrayList3 = this.mAdditionsList.get(size5);
            if (arrayList3.remove(viewHolder)) {
                ViewCompat.setAlpha(view, 1.0f);
                dispatchAddFinished(viewHolder);
                if (arrayList3.isEmpty()) {
                    this.mAdditionsList.remove(size5);
                }
            }
        }
        if (this.mRemoveAnimations.remove(viewHolder)) {
        }
        if (this.mAddAnimations.remove(viewHolder)) {
        }
        if (this.mChangeAnimations.remove(viewHolder)) {
        }
        if (this.mMoveAnimations.remove(viewHolder)) {
        }
        dispatchFinishedWhenDone();
    }

    @Override // android.support.v7.widget.RecyclerView.ItemAnimator
    public void endAnimations() {
        for (int size = this.mPendingMoves.size() - 1; size >= 0; size--) {
            MoveInfo moveInfo = this.mPendingMoves.get(size);
            View view = moveInfo.holder.itemView;
            ViewCompat.setTranslationY(view, 0.0f);
            ViewCompat.setTranslationX(view, 0.0f);
            dispatchMoveFinished(moveInfo.holder);
            this.mPendingMoves.remove(size);
        }
        for (int size2 = this.mPendingRemovals.size() - 1; size2 >= 0; size2--) {
            dispatchRemoveFinished(this.mPendingRemovals.get(size2));
            this.mPendingRemovals.remove(size2);
        }
        for (int size3 = this.mPendingAdditions.size() - 1; size3 >= 0; size3--) {
            RecyclerView.ViewHolder viewHolder = this.mPendingAdditions.get(size3);
            ViewCompat.setAlpha(viewHolder.itemView, 1.0f);
            dispatchAddFinished(viewHolder);
            this.mPendingAdditions.remove(size3);
        }
        for (int size4 = this.mPendingChanges.size() - 1; size4 >= 0; size4--) {
            endChangeAnimationIfNecessary(this.mPendingChanges.get(size4));
        }
        this.mPendingChanges.clear();
        if (isRunning()) {
            for (int size5 = this.mMovesList.size() - 1; size5 >= 0; size5--) {
                ArrayList<MoveInfo> arrayList = this.mMovesList.get(size5);
                for (int size6 = arrayList.size() - 1; size6 >= 0; size6--) {
                    MoveInfo moveInfo2 = arrayList.get(size6);
                    View view2 = moveInfo2.holder.itemView;
                    ViewCompat.setTranslationY(view2, 0.0f);
                    ViewCompat.setTranslationX(view2, 0.0f);
                    dispatchMoveFinished(moveInfo2.holder);
                    arrayList.remove(size6);
                    if (arrayList.isEmpty()) {
                        this.mMovesList.remove(arrayList);
                    }
                }
            }
            for (int size7 = this.mAdditionsList.size() - 1; size7 >= 0; size7--) {
                ArrayList<RecyclerView.ViewHolder> arrayList2 = this.mAdditionsList.get(size7);
                for (int size8 = arrayList2.size() - 1; size8 >= 0; size8--) {
                    RecyclerView.ViewHolder viewHolder2 = arrayList2.get(size8);
                    ViewCompat.setAlpha(viewHolder2.itemView, 1.0f);
                    dispatchAddFinished(viewHolder2);
                    arrayList2.remove(size8);
                    if (arrayList2.isEmpty()) {
                        this.mAdditionsList.remove(arrayList2);
                    }
                }
            }
            for (int size9 = this.mChangesList.size() - 1; size9 >= 0; size9--) {
                ArrayList<ChangeInfo> arrayList3 = this.mChangesList.get(size9);
                for (int size10 = arrayList3.size() - 1; size10 >= 0; size10--) {
                    endChangeAnimationIfNecessary(arrayList3.get(size10));
                    if (arrayList3.isEmpty()) {
                        this.mChangesList.remove(arrayList3);
                    }
                }
            }
            cancelAll(this.mRemoveAnimations);
            cancelAll(this.mMoveAnimations);
            cancelAll(this.mAddAnimations);
            cancelAll(this.mChangeAnimations);
            dispatchAnimationsFinished();
        }
    }

    @Override // android.support.v7.widget.RecyclerView.ItemAnimator
    public boolean isRunning() {
        boolean z = true;
        if (this.mPendingAdditions.isEmpty()) {
            z = true;
            if (this.mPendingChanges.isEmpty()) {
                z = true;
                if (this.mPendingMoves.isEmpty()) {
                    z = true;
                    if (this.mPendingRemovals.isEmpty()) {
                        z = true;
                        if (this.mMoveAnimations.isEmpty()) {
                            z = true;
                            if (this.mRemoveAnimations.isEmpty()) {
                                z = true;
                                if (this.mAddAnimations.isEmpty()) {
                                    z = true;
                                    if (this.mChangeAnimations.isEmpty()) {
                                        z = true;
                                        if (this.mMovesList.isEmpty()) {
                                            z = true;
                                            if (this.mAdditionsList.isEmpty()) {
                                                z = true;
                                                if (this.mChangesList.isEmpty()) {
                                                    z = false;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return z;
    }

    @Override // android.support.v7.widget.RecyclerView.ItemAnimator
    public void runPendingAnimations() {
        boolean z = !this.mPendingRemovals.isEmpty();
        boolean z2 = !this.mPendingMoves.isEmpty();
        boolean z3 = !this.mPendingChanges.isEmpty();
        boolean z4 = !this.mPendingAdditions.isEmpty();
        if (z || z2 || z4 || z3) {
            for (RecyclerView.ViewHolder viewHolder : this.mPendingRemovals) {
                animateRemoveImpl(viewHolder);
            }
            this.mPendingRemovals.clear();
            if (z2) {
                ArrayList<MoveInfo> arrayList = new ArrayList<>();
                arrayList.addAll(this.mPendingMoves);
                this.mMovesList.add(arrayList);
                this.mPendingMoves.clear();
                Runnable runnable = new Runnable(this, arrayList) { // from class: android.support.v7.widget.DefaultItemAnimator.1
                    final DefaultItemAnimator this$0;
                    final ArrayList val$moves;

                    {
                        this.this$0 = this;
                        this.val$moves = arrayList;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        for (MoveInfo moveInfo : this.val$moves) {
                            this.this$0.animateMoveImpl(moveInfo.holder, moveInfo.fromX, moveInfo.fromY, moveInfo.toX, moveInfo.toY);
                        }
                        this.val$moves.clear();
                        this.this$0.mMovesList.remove(this.val$moves);
                    }
                };
                if (z) {
                    ViewCompat.postOnAnimationDelayed(arrayList.get(0).holder.itemView, runnable, getRemoveDuration());
                } else {
                    runnable.run();
                }
            }
            if (z3) {
                ArrayList<ChangeInfo> arrayList2 = new ArrayList<>();
                arrayList2.addAll(this.mPendingChanges);
                this.mChangesList.add(arrayList2);
                this.mPendingChanges.clear();
                Runnable runnable2 = new Runnable(this, arrayList2) { // from class: android.support.v7.widget.DefaultItemAnimator.2
                    final DefaultItemAnimator this$0;
                    final ArrayList val$changes;

                    {
                        this.this$0 = this;
                        this.val$changes = arrayList2;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        for (ChangeInfo changeInfo : this.val$changes) {
                            this.this$0.animateChangeImpl(changeInfo);
                        }
                        this.val$changes.clear();
                        this.this$0.mChangesList.remove(this.val$changes);
                    }
                };
                if (z) {
                    ViewCompat.postOnAnimationDelayed(arrayList2.get(0).oldHolder.itemView, runnable2, getRemoveDuration());
                } else {
                    runnable2.run();
                }
            }
            if (z4) {
                ArrayList<RecyclerView.ViewHolder> arrayList3 = new ArrayList<>();
                arrayList3.addAll(this.mPendingAdditions);
                this.mAdditionsList.add(arrayList3);
                this.mPendingAdditions.clear();
                Runnable runnable3 = new Runnable(this, arrayList3) { // from class: android.support.v7.widget.DefaultItemAnimator.3
                    final DefaultItemAnimator this$0;
                    final ArrayList val$additions;

                    {
                        this.this$0 = this;
                        this.val$additions = arrayList3;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        for (RecyclerView.ViewHolder viewHolder2 : this.val$additions) {
                            this.this$0.animateAddImpl(viewHolder2);
                        }
                        this.val$additions.clear();
                        this.this$0.mAdditionsList.remove(this.val$additions);
                    }
                };
                if (!z && !z2 && !z3) {
                    runnable3.run();
                    return;
                }
                ViewCompat.postOnAnimationDelayed(arrayList3.get(0).itemView, runnable3, (z ? getRemoveDuration() : 0L) + Math.max(z2 ? getMoveDuration() : 0L, z3 ? getChangeDuration() : 0L));
            }
        }
    }
}
