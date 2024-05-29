package androidx.car.widget;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;
import androidx.car.widget.MinTouchTargetHelper;
/* loaded from: classes.dex */
class MinTouchTargetHelper {
    /* JADX INFO: Access modifiers changed from: package-private */
    public static TouchTargetSubject ensureThat(View view) {
        if (view == null) {
            throw new IllegalArgumentException("View cannot be null.");
        }
        return new TouchTargetSubject(view);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class TouchTargetSubject {
        private View mSubjectView;

        private TouchTargetSubject(View subject) {
            this.mSubjectView = subject;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public void hasMinTouchSize(final int size) {
            if (size <= 0) {
                throw new IllegalStateException("Minimum touch target size must be greater than 0.");
            }
            if (!(this.mSubjectView.getParent() instanceof View)) {
                throw new IllegalStateException("Subject view does not have a valid parent of type View. Parent is: " + this.mSubjectView.getParent());
            }
            final View parentView = (View) this.mSubjectView.getParent();
            parentView.post(new Runnable() { // from class: androidx.car.widget.-$$Lambda$MinTouchTargetHelper$TouchTargetSubject$tk4-xQaGayVI8hrzPDplkXHcQlY
                @Override // java.lang.Runnable
                public final void run() {
                    MinTouchTargetHelper.TouchTargetSubject.lambda$hasMinTouchSize$40(MinTouchTargetHelper.TouchTargetSubject.this, size, parentView);
                }
            });
        }

        public static /* synthetic */ void lambda$hasMinTouchSize$40(TouchTargetSubject touchTargetSubject, int i, View view) {
            Rect rect = new Rect();
            touchTargetSubject.mSubjectView.getHitRect(rect);
            int hitWidth = Math.abs(rect.right - rect.left);
            if (hitWidth < i) {
                int amountToIncrease = (i - hitWidth) / 2;
                rect.left -= amountToIncrease;
                rect.right += amountToIncrease;
            }
            int hitHeight = Math.abs(rect.top - rect.bottom);
            if (hitHeight < i) {
                int amountToIncrease2 = (i - hitHeight) / 2;
                rect.top -= amountToIncrease2;
                rect.bottom += amountToIncrease2;
            }
            view.setTouchDelegate(new TouchDelegate(rect, touchTargetSubject.mSubjectView));
        }
    }
}
