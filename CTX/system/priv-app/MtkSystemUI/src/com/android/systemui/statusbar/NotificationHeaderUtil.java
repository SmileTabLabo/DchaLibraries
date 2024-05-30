package com.android.systemui.statusbar;

import android.app.Notification;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.NotificationHeaderView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
/* loaded from: classes.dex */
public class NotificationHeaderUtil {
    private final ArrayList<HeaderProcessor> mComparators = new ArrayList<>();
    private final HashSet<Integer> mDividers = new HashSet<>();
    private final ExpandableNotificationRow mRow;
    private static final TextViewComparator sTextViewComparator = new TextViewComparator();
    private static final VisibilityApplicator sVisibilityApplicator = new VisibilityApplicator();
    private static final DataExtractor sIconExtractor = new DataExtractor() { // from class: com.android.systemui.statusbar.NotificationHeaderUtil.1
        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.DataExtractor
        public Object extractData(ExpandableNotificationRow expandableNotificationRow) {
            return expandableNotificationRow.getStatusBarNotification().getNotification();
        }
    };
    private static final IconComparator sIconVisibilityComparator = new IconComparator() { // from class: com.android.systemui.statusbar.NotificationHeaderUtil.2
        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.IconComparator, com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean compare(View view, View view2, Object obj, Object obj2) {
            return hasSameIcon(obj, obj2) && hasSameColor(obj, obj2);
        }
    };
    private static final IconComparator sGreyComparator = new IconComparator() { // from class: com.android.systemui.statusbar.NotificationHeaderUtil.3
        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.IconComparator, com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean compare(View view, View view2, Object obj, Object obj2) {
            return !hasSameIcon(obj, obj2) || hasSameColor(obj, obj2);
        }
    };
    private static final ResultApplicator mGreyApplicator = new ResultApplicator() { // from class: com.android.systemui.statusbar.NotificationHeaderUtil.4
        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ResultApplicator
        public void apply(View view, boolean z) {
            NotificationHeaderView notificationHeaderView = (NotificationHeaderView) view;
            applyToChild((ImageView) view.findViewById(16908294), z, notificationHeaderView.getOriginalIconColor());
            applyToChild((ImageView) view.findViewById(16908862), z, notificationHeaderView.getOriginalNotificationColor());
        }

        private void applyToChild(View view, boolean z, int i) {
            if (i != 1) {
                ImageView imageView = (ImageView) view;
                imageView.getDrawable().mutate();
                if (z) {
                    imageView.getDrawable().setColorFilter(view.getContext().getColor(17170674), PorterDuff.Mode.SRC_ATOP);
                    return;
                }
                imageView.getDrawable().setColorFilter(i, PorterDuff.Mode.SRC_ATOP);
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public interface DataExtractor {
        Object extractData(ExpandableNotificationRow expandableNotificationRow);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public interface ResultApplicator {
        void apply(View view, boolean z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public interface ViewComparator {
        boolean compare(View view, View view2, Object obj, Object obj2);

        boolean isEmpty(View view);
    }

    public NotificationHeaderUtil(ExpandableNotificationRow expandableNotificationRow) {
        this.mRow = expandableNotificationRow;
        this.mComparators.add(new HeaderProcessor(this.mRow, 16908294, sIconExtractor, sIconVisibilityComparator, sVisibilityApplicator));
        this.mComparators.add(new HeaderProcessor(this.mRow, 16909117, sIconExtractor, sGreyComparator, mGreyApplicator));
        this.mComparators.add(new HeaderProcessor(this.mRow, 16909202, null, new ViewComparator() { // from class: com.android.systemui.statusbar.NotificationHeaderUtil.5
            @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
            public boolean compare(View view, View view2, Object obj, Object obj2) {
                return view.getVisibility() != 8;
            }

            @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
            public boolean isEmpty(View view) {
                return (view instanceof ImageView) && ((ImageView) view).getDrawable() == null;
            }
        }, sVisibilityApplicator));
        this.mComparators.add(HeaderProcessor.forTextView(this.mRow, 16908719));
        this.mComparators.add(HeaderProcessor.forTextView(this.mRow, 16908938));
        this.mDividers.add(16908939);
        this.mDividers.add(16908941);
        this.mDividers.add(16909409);
    }

    public void updateChildrenHeaderAppearance() {
        List<ExpandableNotificationRow> notificationChildren = this.mRow.getNotificationChildren();
        if (notificationChildren == null) {
            return;
        }
        for (int i = 0; i < this.mComparators.size(); i++) {
            this.mComparators.get(i).init();
        }
        for (int i2 = 0; i2 < notificationChildren.size(); i2++) {
            ExpandableNotificationRow expandableNotificationRow = notificationChildren.get(i2);
            for (int i3 = 0; i3 < this.mComparators.size(); i3++) {
                this.mComparators.get(i3).compareToHeader(expandableNotificationRow);
            }
        }
        for (int i4 = 0; i4 < notificationChildren.size(); i4++) {
            ExpandableNotificationRow expandableNotificationRow2 = notificationChildren.get(i4);
            for (int i5 = 0; i5 < this.mComparators.size(); i5++) {
                this.mComparators.get(i5).apply(expandableNotificationRow2);
            }
            sanitizeHeaderViews(expandableNotificationRow2);
        }
    }

    private void sanitizeHeaderViews(ExpandableNotificationRow expandableNotificationRow) {
        if (expandableNotificationRow.isSummaryWithChildren()) {
            sanitizeHeader(expandableNotificationRow.getNotificationHeader());
            return;
        }
        NotificationContentView privateLayout = expandableNotificationRow.getPrivateLayout();
        sanitizeChild(privateLayout.getContractedChild());
        sanitizeChild(privateLayout.getHeadsUpChild());
        sanitizeChild(privateLayout.getExpandedChild());
    }

    private void sanitizeChild(View view) {
        if (view != null) {
            sanitizeHeader((NotificationHeaderView) view.findViewById(16909117));
        }
    }

    private void sanitizeHeader(NotificationHeaderView notificationHeaderView) {
        int i;
        boolean z;
        View view;
        boolean z2;
        if (notificationHeaderView == null) {
            return;
        }
        int childCount = notificationHeaderView.getChildCount();
        View findViewById = notificationHeaderView.findViewById(16909405);
        int i2 = 1;
        while (true) {
            i = childCount - 1;
            if (i2 < i) {
                View childAt = notificationHeaderView.getChildAt(i2);
                if (!(childAt instanceof TextView) || childAt.getVisibility() == 8 || this.mDividers.contains(Integer.valueOf(childAt.getId())) || childAt == findViewById) {
                    i2++;
                } else {
                    z = true;
                    break;
                }
            } else {
                z = false;
                break;
            }
        }
        findViewById.setVisibility((!z || this.mRow.getStatusBarNotification().getNotification().showsTime()) ? 0 : 8);
        View view2 = null;
        int i3 = 1;
        while (i3 < i) {
            View childAt2 = notificationHeaderView.getChildAt(i3);
            if (this.mDividers.contains(Integer.valueOf(childAt2.getId()))) {
                while (true) {
                    i3++;
                    if (i3 >= i) {
                        break;
                    }
                    view = notificationHeaderView.getChildAt(i3);
                    if (this.mDividers.contains(Integer.valueOf(view.getId()))) {
                        i3--;
                        break;
                    } else if (view.getVisibility() != 8 && (view instanceof TextView)) {
                        z2 = view2 != null;
                    }
                }
                view = view2;
                z2 = false;
                childAt2.setVisibility(z2 ? 0 : 8);
                view2 = view;
            } else if (childAt2.getVisibility() != 8 && (childAt2 instanceof TextView)) {
                view2 = childAt2;
            }
            i3++;
        }
    }

    public void restoreNotificationHeader(ExpandableNotificationRow expandableNotificationRow) {
        for (int i = 0; i < this.mComparators.size(); i++) {
            this.mComparators.get(i).apply(expandableNotificationRow, true);
        }
        sanitizeHeaderViews(expandableNotificationRow);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class HeaderProcessor {
        private final ResultApplicator mApplicator;
        private boolean mApply;
        private ViewComparator mComparator;
        private final DataExtractor mExtractor;
        private final int mId;
        private Object mParentData;
        private final ExpandableNotificationRow mParentRow;
        private View mParentView;

        public static HeaderProcessor forTextView(ExpandableNotificationRow expandableNotificationRow, int i) {
            return new HeaderProcessor(expandableNotificationRow, i, null, NotificationHeaderUtil.sTextViewComparator, NotificationHeaderUtil.sVisibilityApplicator);
        }

        HeaderProcessor(ExpandableNotificationRow expandableNotificationRow, int i, DataExtractor dataExtractor, ViewComparator viewComparator, ResultApplicator resultApplicator) {
            this.mId = i;
            this.mExtractor = dataExtractor;
            this.mApplicator = resultApplicator;
            this.mComparator = viewComparator;
            this.mParentRow = expandableNotificationRow;
        }

        public void init() {
            this.mParentView = this.mParentRow.getNotificationHeader().findViewById(this.mId);
            this.mParentData = this.mExtractor == null ? null : this.mExtractor.extractData(this.mParentRow);
            this.mApply = !this.mComparator.isEmpty(this.mParentView);
        }

        public void compareToHeader(ExpandableNotificationRow expandableNotificationRow) {
            NotificationHeaderView contractedNotificationHeader;
            if (!this.mApply || (contractedNotificationHeader = expandableNotificationRow.getContractedNotificationHeader()) == null) {
                return;
            }
            this.mApply = this.mComparator.compare(this.mParentView, contractedNotificationHeader.findViewById(this.mId), this.mParentData, this.mExtractor == null ? null : this.mExtractor.extractData(expandableNotificationRow));
        }

        public void apply(ExpandableNotificationRow expandableNotificationRow) {
            apply(expandableNotificationRow, false);
        }

        public void apply(ExpandableNotificationRow expandableNotificationRow, boolean z) {
            boolean z2 = this.mApply && !z;
            if (expandableNotificationRow.isSummaryWithChildren()) {
                applyToView(z2, expandableNotificationRow.getNotificationHeader());
                return;
            }
            applyToView(z2, expandableNotificationRow.getPrivateLayout().getContractedChild());
            applyToView(z2, expandableNotificationRow.getPrivateLayout().getHeadsUpChild());
            applyToView(z2, expandableNotificationRow.getPrivateLayout().getExpandedChild());
        }

        private void applyToView(boolean z, View view) {
            View findViewById;
            if (view != null && (findViewById = view.findViewById(this.mId)) != null && !this.mComparator.isEmpty(findViewById)) {
                this.mApplicator.apply(findViewById, z);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class TextViewComparator implements ViewComparator {
        private TextViewComparator() {
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean compare(View view, View view2, Object obj, Object obj2) {
            return ((TextView) view).getText().equals(((TextView) view2).getText());
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean isEmpty(View view) {
            return TextUtils.isEmpty(((TextView) view).getText());
        }
    }

    /* loaded from: classes.dex */
    private static abstract class IconComparator implements ViewComparator {
        private IconComparator() {
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean compare(View view, View view2, Object obj, Object obj2) {
            return false;
        }

        protected boolean hasSameIcon(Object obj, Object obj2) {
            return ((Notification) obj).getSmallIcon().sameAs(((Notification) obj2).getSmallIcon());
        }

        protected boolean hasSameColor(Object obj, Object obj2) {
            return ((Notification) obj).color == ((Notification) obj2).color;
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean isEmpty(View view) {
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class VisibilityApplicator implements ResultApplicator {
        private VisibilityApplicator() {
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ResultApplicator
        public void apply(View view, boolean z) {
            view.setVisibility(z ? 8 : 0);
        }
    }
}
