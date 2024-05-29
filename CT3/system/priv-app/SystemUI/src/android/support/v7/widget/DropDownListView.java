package android.support.v7.widget;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.support.v7.appcompat.R$attr;
import android.view.MotionEvent;
import android.view.View;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v7/widget/DropDownListView.class */
public class DropDownListView extends ListViewCompat {
    private ViewPropertyAnimatorCompat mClickAnimation;
    private boolean mDrawsInPressedState;
    private boolean mHijackFocus;
    private boolean mListSelectionHidden;
    private ListViewAutoScrollHelper mScrollHelper;

    public DropDownListView(Context context, boolean z) {
        super(context, null, R$attr.dropDownListViewStyle);
        this.mHijackFocus = z;
        setCacheColorHint(0);
    }

    private void clearPressedItem() {
        this.mDrawsInPressedState = false;
        setPressed(false);
        drawableStateChanged();
        View childAt = getChildAt(this.mMotionPosition - getFirstVisiblePosition());
        if (childAt != null) {
            childAt.setPressed(false);
        }
        if (this.mClickAnimation != null) {
            this.mClickAnimation.cancel();
            this.mClickAnimation = null;
        }
    }

    private void clickPressedItem(View view, int i) {
        performItemClick(view, i, getItemIdAtPosition(i));
    }

    private void setPressedItem(View view, int i, float f, float f2) {
        View childAt;
        this.mDrawsInPressedState = true;
        if (Build.VERSION.SDK_INT >= 21) {
            drawableHotspotChanged(f, f2);
        }
        if (!isPressed()) {
            setPressed(true);
        }
        layoutChildren();
        if (this.mMotionPosition != -1 && (childAt = getChildAt(this.mMotionPosition - getFirstVisiblePosition())) != null && childAt != view && childAt.isPressed()) {
            childAt.setPressed(false);
        }
        this.mMotionPosition = i;
        float left = view.getLeft();
        float top = view.getTop();
        if (Build.VERSION.SDK_INT >= 21) {
            view.drawableHotspotChanged(f - left, f2 - top);
        }
        if (!view.isPressed()) {
            view.setPressed(true);
        }
        positionSelectorLikeTouchCompat(i, view, f, f2);
        setSelectorEnabled(false);
        refreshDrawableState();
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean hasFocus() {
        return !this.mHijackFocus ? super.hasFocus() : true;
    }

    @Override // android.view.View
    public boolean hasWindowFocus() {
        return !this.mHijackFocus ? super.hasWindowFocus() : true;
    }

    @Override // android.view.View
    public boolean isFocused() {
        return !this.mHijackFocus ? super.isFocused() : true;
    }

    @Override // android.view.View
    public boolean isInTouchMode() {
        return (this.mHijackFocus && this.mListSelectionHidden) ? true : super.isInTouchMode();
    }

    public boolean onForwardedEvent(MotionEvent motionEvent, int i) {
        boolean z;
        boolean z2 = true;
        int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
        switch (actionMasked) {
            case 1:
                z2 = false;
            case 2:
                int findPointerIndex = motionEvent.findPointerIndex(i);
                if (findPointerIndex >= 0) {
                    int x = (int) motionEvent.getX(findPointerIndex);
                    int y = (int) motionEvent.getY(findPointerIndex);
                    int pointToPosition = pointToPosition(x, y);
                    if (pointToPosition != -1) {
                        View childAt = getChildAt(pointToPosition - getFirstVisiblePosition());
                        setPressedItem(childAt, pointToPosition, x, y);
                        z = false;
                        z2 = true;
                        if (actionMasked == 1) {
                            clickPressedItem(childAt, pointToPosition);
                            z = false;
                            z2 = true;
                            break;
                        }
                    } else {
                        z = true;
                        break;
                    }
                } else {
                    z2 = false;
                    z = false;
                    break;
                }
                break;
            case 3:
                z2 = false;
                z = false;
                break;
            default:
                z2 = true;
                z = false;
                break;
        }
        if (!z2 || z) {
            clearPressedItem();
        }
        if (z2) {
            if (this.mScrollHelper == null) {
                this.mScrollHelper = new ListViewAutoScrollHelper(this);
            }
            this.mScrollHelper.setEnabled(true);
            this.mScrollHelper.onTouch(this, motionEvent);
        } else if (this.mScrollHelper != null) {
            this.mScrollHelper.setEnabled(false);
        }
        return z2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setListSelectionHidden(boolean z) {
        this.mListSelectionHidden = z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.widget.ListViewCompat
    public boolean touchModeDrawsInPressedStateCompat() {
        return !this.mDrawsInPressedState ? super.touchModeDrawsInPressedStateCompat() : true;
    }
}
