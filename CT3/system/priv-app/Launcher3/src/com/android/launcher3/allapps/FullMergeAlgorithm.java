package com.android.launcher3.allapps;

import com.android.launcher3.allapps.AlphabeticalAppsList;
/* loaded from: a.zip:com/android/launcher3/allapps/FullMergeAlgorithm.class */
final class FullMergeAlgorithm implements AlphabeticalAppsList.MergeAlgorithm {
    @Override // com.android.launcher3.allapps.AlphabeticalAppsList.MergeAlgorithm
    public boolean continueMerging(AlphabeticalAppsList.SectionInfo sectionInfo, AlphabeticalAppsList.SectionInfo sectionInfo2, int i, int i2, int i3) {
        return sectionInfo.firstAppItem.viewType == 1;
    }
}
