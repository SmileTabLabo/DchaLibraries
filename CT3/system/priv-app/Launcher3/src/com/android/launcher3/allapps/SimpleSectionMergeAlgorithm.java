package com.android.launcher3.allapps;

import com.android.launcher3.allapps.AlphabeticalAppsList;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
/* loaded from: a.zip:com/android/launcher3/allapps/SimpleSectionMergeAlgorithm.class */
final class SimpleSectionMergeAlgorithm implements AlphabeticalAppsList.MergeAlgorithm {
    private CharsetEncoder mAsciiEncoder = Charset.forName("US-ASCII").newEncoder();
    private int mMaxAllowableMerges;
    private int mMinAppsPerRow;
    private int mMinRowsInMergedSection;

    public SimpleSectionMergeAlgorithm(int i, int i2, int i3) {
        this.mMinAppsPerRow = i;
        this.mMinRowsInMergedSection = i2;
        this.mMaxAllowableMerges = i3;
    }

    @Override // com.android.launcher3.allapps.AlphabeticalAppsList.MergeAlgorithm
    public boolean continueMerging(AlphabeticalAppsList.SectionInfo sectionInfo, AlphabeticalAppsList.SectionInfo sectionInfo2, int i, int i2, int i3) {
        if (sectionInfo.firstAppItem.viewType != 1) {
            return false;
        }
        int i4 = i / i2;
        int i5 = i % i2;
        boolean z = false;
        if (sectionInfo.firstAppItem != null) {
            z = false;
            if (sectionInfo2.firstAppItem != null) {
                z = this.mAsciiEncoder.canEncode(sectionInfo.firstAppItem.sectionName) != this.mAsciiEncoder.canEncode(sectionInfo2.firstAppItem.sectionName);
            }
        }
        boolean z2 = false;
        if (i5 > 0) {
            z2 = false;
            if (i5 < this.mMinAppsPerRow) {
                z2 = false;
                if (i4 < this.mMinRowsInMergedSection) {
                    z2 = false;
                    if (i3 < this.mMaxAllowableMerges) {
                        z2 = !z;
                    }
                }
            }
        }
        return z2;
    }
}
