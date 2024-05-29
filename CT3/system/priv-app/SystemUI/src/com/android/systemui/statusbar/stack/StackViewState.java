package com.android.systemui.statusbar.stack;
/* loaded from: a.zip:com/android/systemui/statusbar/stack/StackViewState.class */
public class StackViewState extends ViewState {
    public boolean belowSpeedBump;
    public int clipTopAmount;
    public boolean dark;
    public boolean dimmed;
    public int height;
    public boolean hideSensitive;
    public boolean isBottomClipped;
    public int location;
    public int notGoneIndex;
    public float shadowAlpha;

    @Override // com.android.systemui.statusbar.stack.ViewState
    public void copyFrom(ViewState viewState) {
        super.copyFrom(viewState);
        if (viewState instanceof StackViewState) {
            StackViewState stackViewState = (StackViewState) viewState;
            this.height = stackViewState.height;
            this.dimmed = stackViewState.dimmed;
            this.shadowAlpha = stackViewState.shadowAlpha;
            this.dark = stackViewState.dark;
            this.hideSensitive = stackViewState.hideSensitive;
            this.belowSpeedBump = stackViewState.belowSpeedBump;
            this.clipTopAmount = stackViewState.clipTopAmount;
            this.notGoneIndex = stackViewState.notGoneIndex;
            this.location = stackViewState.location;
            this.isBottomClipped = stackViewState.isBottomClipped;
        }
    }
}
