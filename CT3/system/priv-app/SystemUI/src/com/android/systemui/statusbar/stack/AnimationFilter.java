package com.android.systemui.statusbar.stack;

import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/stack/AnimationFilter.class */
public class AnimationFilter {
    boolean animateAlpha;
    boolean animateDark;
    boolean animateDimmed;
    boolean animateHeight;
    boolean animateHideSensitive;
    public boolean animateShadowAlpha;
    boolean animateTopInset;
    boolean animateY;
    boolean animateZ;
    int darkAnimationOriginIndex;
    boolean hasDarkEvent;
    boolean hasDelays;
    boolean hasGoToFullShadeEvent;
    boolean hasHeadsUpDisappearClickEvent;

    private void combineFilter(AnimationFilter animationFilter) {
        this.animateAlpha |= animationFilter.animateAlpha;
        this.animateY |= animationFilter.animateY;
        this.animateZ |= animationFilter.animateZ;
        this.animateHeight |= animationFilter.animateHeight;
        this.animateTopInset |= animationFilter.animateTopInset;
        this.animateDimmed |= animationFilter.animateDimmed;
        this.animateDark |= animationFilter.animateDark;
        this.animateHideSensitive |= animationFilter.animateHideSensitive;
        this.animateShadowAlpha |= animationFilter.animateShadowAlpha;
        this.hasDelays |= animationFilter.hasDelays;
    }

    private void reset() {
        this.animateAlpha = false;
        this.animateY = false;
        this.animateZ = false;
        this.animateHeight = false;
        this.animateShadowAlpha = false;
        this.animateTopInset = false;
        this.animateDimmed = false;
        this.animateDark = false;
        this.animateHideSensitive = false;
        this.hasDelays = false;
        this.hasGoToFullShadeEvent = false;
        this.hasDarkEvent = false;
        this.hasHeadsUpDisappearClickEvent = false;
        this.darkAnimationOriginIndex = -1;
    }

    public AnimationFilter animateAlpha() {
        this.animateAlpha = true;
        return this;
    }

    public AnimationFilter animateDark() {
        this.animateDark = true;
        return this;
    }

    public AnimationFilter animateDimmed() {
        this.animateDimmed = true;
        return this;
    }

    public AnimationFilter animateHeight() {
        this.animateHeight = true;
        return this;
    }

    public AnimationFilter animateHideSensitive() {
        this.animateHideSensitive = true;
        return this;
    }

    public AnimationFilter animateShadowAlpha() {
        this.animateShadowAlpha = true;
        return this;
    }

    public AnimationFilter animateTopInset() {
        this.animateTopInset = true;
        return this;
    }

    public AnimationFilter animateY() {
        this.animateY = true;
        return this;
    }

    public AnimationFilter animateZ() {
        this.animateZ = true;
        return this;
    }

    public void applyCombination(ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList) {
        reset();
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            NotificationStackScrollLayout.AnimationEvent animationEvent = arrayList.get(i);
            combineFilter(arrayList.get(i).filter);
            if (animationEvent.animationType == 10) {
                this.hasGoToFullShadeEvent = true;
            }
            if (animationEvent.animationType == 9) {
                this.hasDarkEvent = true;
                this.darkAnimationOriginIndex = animationEvent.darkAnimationOriginIndex;
            }
            if (animationEvent.animationType == 16) {
                this.hasHeadsUpDisappearClickEvent = true;
            }
        }
    }

    public AnimationFilter hasDelays() {
        this.hasDelays = true;
        return this;
    }
}
