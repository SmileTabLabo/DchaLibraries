package com.android.launcher3.anim;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.util.SparseArray;
import android.view.animation.Interpolator;
import com.android.launcher3.LauncherAnimUtils;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class AnimatorSetBuilder {
    public static final int ANIM_OVERVIEW_FADE = 4;
    public static final int ANIM_OVERVIEW_SCALE = 3;
    public static final int ANIM_VERTICAL_PROGRESS = 0;
    public static final int ANIM_WORKSPACE_FADE = 2;
    public static final int ANIM_WORKSPACE_SCALE = 1;
    protected final ArrayList<Animator> mAnims = new ArrayList<>();
    private final SparseArray<Interpolator> mInterpolators = new SparseArray<>();
    private List<Runnable> mOnFinishRunnables = new ArrayList();

    public void startTag(Object obj) {
    }

    public void play(Animator animator) {
        this.mAnims.add(animator);
    }

    public void addOnFinishRunnable(Runnable runnable) {
        this.mOnFinishRunnables.add(runnable);
    }

    public AnimatorSet build() {
        AnimatorSet createAnimatorSet = LauncherAnimUtils.createAnimatorSet();
        createAnimatorSet.playTogether(this.mAnims);
        if (!this.mOnFinishRunnables.isEmpty()) {
            createAnimatorSet.addListener(new AnimationSuccessListener() { // from class: com.android.launcher3.anim.AnimatorSetBuilder.1
                @Override // com.android.launcher3.anim.AnimationSuccessListener
                public void onAnimationSuccess(Animator animator) {
                    for (Runnable runnable : AnimatorSetBuilder.this.mOnFinishRunnables) {
                        runnable.run();
                    }
                    AnimatorSetBuilder.this.mOnFinishRunnables.clear();
                }
            });
        }
        return createAnimatorSet;
    }

    public Interpolator getInterpolator(int i, Interpolator interpolator) {
        return this.mInterpolators.get(i, interpolator);
    }

    public void setInterpolator(int i, Interpolator interpolator) {
        this.mInterpolators.put(i, interpolator);
    }
}
