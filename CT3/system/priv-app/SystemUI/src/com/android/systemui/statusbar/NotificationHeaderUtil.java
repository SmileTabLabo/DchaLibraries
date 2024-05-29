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
/* loaded from: a.zip:com/android/systemui/statusbar/NotificationHeaderUtil.class */
public class NotificationHeaderUtil {
    private final ArrayList<HeaderProcessor> mComparators = new ArrayList<>();
    private final HashSet<Integer> mDividers = new HashSet<>();
    private final ExpandableNotificationRow mRow;
    private static final TextViewComparator sTextViewComparator = new TextViewComparator(null);
    private static final VisibilityApplicator sVisibilityApplicator = new VisibilityApplicator(null);
    private static final DataExtractor sIconExtractor = new DataExtractor() { // from class: com.android.systemui.statusbar.NotificationHeaderUtil.1
        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.DataExtractor
        public Object extractData(ExpandableNotificationRow expandableNotificationRow) {
            return expandableNotificationRow.getStatusBarNotification().getNotification();
        }
    };
    private static final IconComparator sIconVisibilityComparator = new IconComparator() { // from class: com.android.systemui.statusbar.NotificationHeaderUtil.2
        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.IconComparator, com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean compare(View view, View view2, Object obj, Object obj2) {
            return hasSameIcon(obj, obj2) ? hasSameColor(obj, obj2) : false;
        }
    };
    private static final IconComparator sGreyComparator = new IconComparator() { // from class: com.android.systemui.statusbar.NotificationHeaderUtil.3
        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.IconComparator, com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean compare(View view, View view2, Object obj, Object obj2) {
            return hasSameIcon(obj, obj2) ? hasSameColor(obj, obj2) : true;
        }
    };
    private static final ResultApplicator mGreyApplicator = new ResultApplicator() { // from class: com.android.systemui.statusbar.NotificationHeaderUtil.4
        private void applyToChild(View view, boolean z, int i) {
            if (i != -1) {
                ImageView imageView = (ImageView) view;
                imageView.getDrawable().mutate();
                if (!z) {
                    imageView.getDrawable().setColorFilter(i, PorterDuff.Mode.SRC_ATOP);
                    return;
                }
                imageView.getDrawable().setColorFilter(view.getContext().getColor(17170513), PorterDuff.Mode.SRC_ATOP);
            }
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ResultApplicator
        public void apply(View view, boolean z) {
            NotificationHeaderView notificationHeaderView = (NotificationHeaderView) view;
            applyToChild((ImageView) view.findViewById(16908294), z, notificationHeaderView.getOriginalIconColor());
            applyToChild((ImageView) view.findViewById(16909230), z, notificationHeaderView.getOriginalNotificationColor());
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/NotificationHeaderUtil$DataExtractor.class */
    public interface DataExtractor {
        Object extractData(ExpandableNotificationRow expandableNotificationRow);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/NotificationHeaderUtil$HeaderProcessor.class */
    public static class HeaderProcessor {
        private final ResultApplicator mApplicator;
        private boolean mApply;
        private ViewComparator mComparator;
        private final DataExtractor mExtractor;
        private final int mId;
        private Object mParentData;
        private final ExpandableNotificationRow mParentRow;
        private View mParentView;

        HeaderProcessor(ExpandableNotificationRow expandableNotificationRow, int i, DataExtractor dataExtractor, ViewComparator viewComparator, ResultApplicator resultApplicator) {
            this.mId = i;
            this.mExtractor = dataExtractor;
            this.mApplicator = resultApplicator;
            this.mComparator = viewComparator;
            this.mParentRow = expandableNotificationRow;
        }

        private void applyToView(boolean z, View view) {
            View findViewById;
            if (view == null || (findViewById = view.findViewById(this.mId)) == null || this.mComparator.isEmpty(findViewById)) {
                return;
            }
            this.mApplicator.apply(findViewById, z);
        }

        public static HeaderProcessor forTextView(ExpandableNotificationRow expandableNotificationRow, int i) {
            return new HeaderProcessor(expandableNotificationRow, i, null, NotificationHeaderUtil.sTextViewComparator, NotificationHeaderUtil.sVisibilityApplicator);
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

        public void compareToHeader(ExpandableNotificationRow expandableNotificationRow) {
            if (this.mApply) {
                NotificationHeaderView notificationHeader = expandableNotificationRow.getNotificationHeader();
                if (notificationHeader == null) {
                    this.mApply = false;
                } else {
                    this.mApply = this.mComparator.compare(this.mParentView, notificationHeader.findViewById(this.mId), this.mParentData, this.mExtractor == null ? null : this.mExtractor.extractData(expandableNotificationRow));
                }
            }
        }

        public void init() {
            Object obj = null;
            this.mParentView = this.mParentRow.getNotificationHeader().findViewById(this.mId);
            if (this.mExtractor != null) {
                obj = this.mExtractor.extractData(this.mParentRow);
            }
            this.mParentData = obj;
            this.mApply = !this.mComparator.isEmpty(this.mParentView);
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/NotificationHeaderUtil$IconComparator.class */
    private static abstract class IconComparator implements ViewComparator {
        private IconComparator() {
        }

        /* synthetic */ IconComparator(IconComparator iconComparator) {
            this();
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean compare(View view, View view2, Object obj, Object obj2) {
            return false;
        }

        protected boolean hasSameColor(Object obj, Object obj2) {
            return ((Notification) obj).color == ((Notification) obj2).color;
        }

        protected boolean hasSameIcon(Object obj, Object obj2) {
            return ((Notification) obj).getSmallIcon().sameAs(((Notification) obj2).getSmallIcon());
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean isEmpty(View view) {
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/NotificationHeaderUtil$ResultApplicator.class */
    public interface ResultApplicator {
        void apply(View view, boolean z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/NotificationHeaderUtil$TextViewComparator.class */
    public static class TextViewComparator implements ViewComparator {
        private TextViewComparator() {
        }

        /* synthetic */ TextViewComparator(TextViewComparator textViewComparator) {
            this();
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

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/NotificationHeaderUtil$ViewComparator.class */
    public interface ViewComparator {
        boolean compare(View view, View view2, Object obj, Object obj2);

        boolean isEmpty(View view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/NotificationHeaderUtil$VisibilityApplicator.class */
    public static class VisibilityApplicator implements ResultApplicator {
        private VisibilityApplicator() {
        }

        /* synthetic */ VisibilityApplicator(VisibilityApplicator visibilityApplicator) {
            this();
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ResultApplicator
        public void apply(View view, boolean z) {
            view.setVisibility(z ? 8 : 0);
        }
    }

    public NotificationHeaderUtil(ExpandableNotificationRow expandableNotificationRow) {
        this.mRow = expandableNotificationRow;
        this.mComparators.add(new HeaderProcessor(this.mRow, 16908294, sIconExtractor, sIconVisibilityComparator, sVisibilityApplicator));
        this.mComparators.add(new HeaderProcessor(this.mRow, 16909224, sIconExtractor, sGreyComparator, mGreyApplicator));
        this.mComparators.add(new HeaderProcessor(this.mRow, 16909231, null, new ViewComparator(this) { // from class: com.android.systemui.statusbar.NotificationHeaderUtil.5
            final NotificationHeaderUtil this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
            public boolean compare(View view, View view2, Object obj, Object obj2) {
                return view.getVisibility() != 8;
            }

            @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
            public boolean isEmpty(View view) {
                boolean z = false;
                if (view instanceof ImageView) {
                    if (((ImageView) view).getDrawable() == null) {
                        z = true;
                    }
                    return z;
                }
                return false;
            }
        }, sVisibilityApplicator));
        this.mComparators.add(HeaderProcessor.forTextView(this.mRow, 16909225));
        this.mComparators.add(HeaderProcessor.forTextView(this.mRow, 16909227));
        this.mDividers.add(16909226);
        this.mDividers.add(16909228);
    }

    private void sanitizeChild(View view) {
        if (view != null) {
            sanitizeHeader((NotificationHeaderView) view.findViewById(16909224));
        }
    }

    private void sanitizeHeader(NotificationHeaderView notificationHeaderView) {
        boolean z;
        int i;
        View view;
        int i2;
        boolean z2;
        if (notificationHeaderView == null) {
            return;
        }
        int childCount = notificationHeaderView.getChildCount();
        View findViewById = notificationHeaderView.findViewById(16908436);
        int i3 = 1;
        while (true) {
            z = false;
            if (i3 >= childCount - 1) {
                break;
            }
            View childAt = notificationHeaderView.getChildAt(i3);
            if ((childAt instanceof TextView) && childAt.getVisibility() != 8 && !this.mDividers.contains(Integer.valueOf(childAt.getId())) && childAt != findViewById) {
                z = true;
                break;
            }
            i3++;
        }
        findViewById.setVisibility((!z || this.mRow.getStatusBarNotification().getNotification().showsTime()) ? 0 : 8);
        View view2 = null;
        int i4 = 1;
        while (i4 < childCount - 1) {
            View childAt2 = notificationHeaderView.getChildAt(i4);
            if (this.mDividers.contains(Integer.valueOf(childAt2.getId()))) {
                int i5 = i4 + 1;
                while (true) {
                    i2 = i5;
                    view = view2;
                    z2 = false;
                    if (i5 >= childCount - 1) {
                        break;
                    }
                    view = notificationHeaderView.getChildAt(i5);
                    if (this.mDividers.contains(Integer.valueOf(view.getId()))) {
                        i2 = i5 - 1;
                        z2 = false;
                        view = view2;
                        break;
                    } else if (view.getVisibility() == 8 || !(view instanceof TextView)) {
                        i5++;
                    } else {
                        z2 = view2 != null;
                        i2 = i5;
                    }
                }
                childAt2.setVisibility(z2 ? 0 : 8);
                i = i2;
            } else {
                i = i4;
                view = view2;
                if (childAt2.getVisibility() != 8) {
                    i = i4;
                    view = view2;
                    if (childAt2 instanceof TextView) {
                        view = childAt2;
                        i = i4;
                    }
                }
            }
            i4 = i + 1;
            view2 = view;
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

    public void restoreNotificationHeader(ExpandableNotificationRow expandableNotificationRow) {
        for (int i = 0; i < this.mComparators.size(); i++) {
            this.mComparators.get(i).apply(expandableNotificationRow, true);
        }
        sanitizeHeaderViews(expandableNotificationRow);
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
}
