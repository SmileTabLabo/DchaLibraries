package com.android.launcher3.model;

import android.content.Context;
import com.android.launcher3.AppInfo;
import com.android.launcher3.ItemInfo;
import java.text.Collator;
import java.util.Comparator;
/* loaded from: a.zip:com/android/launcher3/model/AppNameComparator.class */
public class AppNameComparator {
    private final AbstractUserComparator<ItemInfo> mAppInfoComparator;
    private final Collator mCollator = Collator.getInstance();
    private final Comparator<String> mSectionNameComparator = new Comparator<String>(this) { // from class: com.android.launcher3.model.AppNameComparator.2
        final AppNameComparator this$0;

        {
            this.this$0 = this;
        }

        @Override // java.util.Comparator
        public int compare(String str, String str2) {
            return this.this$0.compareTitles(str, str2);
        }
    };

    public AppNameComparator(Context context) {
        this.mAppInfoComparator = new AbstractUserComparator<ItemInfo>(this, context) { // from class: com.android.launcher3.model.AppNameComparator.1
            final AppNameComparator this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.launcher3.model.AbstractUserComparator
            public final int compare(ItemInfo itemInfo, ItemInfo itemInfo2) {
                int compareTitles = this.this$0.compareTitles(itemInfo.title.toString(), itemInfo2.title.toString());
                int i = compareTitles;
                if (compareTitles == 0) {
                    i = compareTitles;
                    if (itemInfo instanceof AppInfo) {
                        i = compareTitles;
                        if (itemInfo2 instanceof AppInfo) {
                            int compareTo = ((AppInfo) itemInfo).componentName.compareTo(((AppInfo) itemInfo2).componentName);
                            i = compareTo;
                            if (compareTo == 0) {
                                return super.compare(itemInfo, itemInfo2);
                            }
                        }
                    }
                }
                return i;
            }
        };
    }

    int compareTitles(String str, String str2) {
        boolean isLetterOrDigit = str.length() > 0 ? Character.isLetterOrDigit(str.codePointAt(0)) : false;
        boolean isLetterOrDigit2 = str2.length() > 0 ? Character.isLetterOrDigit(str2.codePointAt(0)) : false;
        if (!isLetterOrDigit || isLetterOrDigit2) {
            if (isLetterOrDigit || !isLetterOrDigit2) {
                return this.mCollator.compare(str, str2);
            }
            return 1;
        }
        return -1;
    }

    public Comparator<ItemInfo> getAppInfoComparator() {
        return this.mAppInfoComparator;
    }

    public Comparator<String> getSectionNameComparator() {
        return this.mSectionNameComparator;
    }
}
