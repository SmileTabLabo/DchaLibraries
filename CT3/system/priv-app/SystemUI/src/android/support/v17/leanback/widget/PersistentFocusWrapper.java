package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import java.util.ArrayList;
/* loaded from: a.zip:android/support/v17/leanback/widget/PersistentFocusWrapper.class */
class PersistentFocusWrapper extends FrameLayout {
    private boolean mPersistFocusVertical;
    private int mSelectedPosition;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:android/support/v17/leanback/widget/PersistentFocusWrapper$SavedState.class */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: android.support.v17.leanback.widget.PersistentFocusWrapper.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        int mSelectedPosition;

        SavedState(Parcel parcel) {
            super(parcel);
            this.mSelectedPosition = parcel.readInt();
        }

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.mSelectedPosition);
        }
    }

    public PersistentFocusWrapper(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSelectedPosition = -1;
        this.mPersistFocusVertical = true;
    }

    public PersistentFocusWrapper(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mSelectedPosition = -1;
        this.mPersistFocusVertical = true;
    }

    /* JADX WARN: Code restructure failed: missing block: B:16:0x0030, code lost:
        if (r4 != 66) goto L17;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private boolean shouldPersistFocusFromDirection(int i) {
        boolean z;
        if (this.mPersistFocusVertical) {
            z = true;
            if (i != 33) {
                if (i == 130) {
                    z = true;
                }
            }
            return z;
        }
        if (!this.mPersistFocusVertical) {
            z = true;
            if (i != 17) {
                z = true;
            }
            return z;
        }
        z = false;
        return z;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void addFocusables(ArrayList<View> arrayList, int i, int i2) {
        if (hasFocus() || getGrandChildCount() == 0 || !shouldPersistFocusFromDirection(i)) {
            super.addFocusables(arrayList, i, i2);
        } else {
            arrayList.add(this);
        }
    }

    int getGrandChildCount() {
        int i = 0;
        ViewGroup viewGroup = (ViewGroup) getChildAt(0);
        if (viewGroup != null) {
            i = viewGroup.getChildCount();
        }
        return i;
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        this.mSelectedPosition = ((SavedState) parcelable).mSelectedPosition;
        super.onRestoreInstanceState(((SavedState) parcelable).getSuperState());
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mSelectedPosition = this.mSelectedPosition;
        return savedState;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestChildFocus(View view, View view2) {
        super.requestChildFocus(view, view2);
        while (view2 != null && view2.getParent() != view) {
            view2 = (View) view2.getParent();
        }
        this.mSelectedPosition = view2 == null ? -1 : ((ViewGroup) view).indexOfChild(view2);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean requestFocus(int i, Rect rect) {
        ViewGroup viewGroup = (ViewGroup) getChildAt(0);
        if (viewGroup == null || this.mSelectedPosition < 0 || this.mSelectedPosition >= getGrandChildCount() || !viewGroup.getChildAt(this.mSelectedPosition).requestFocus(i, rect)) {
            return super.requestFocus(i, rect);
        }
        return true;
    }
}
