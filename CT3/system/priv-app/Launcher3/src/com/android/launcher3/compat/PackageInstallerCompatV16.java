package com.android.launcher3.compat;

import java.util.HashMap;
/* loaded from: a.zip:com/android/launcher3/compat/PackageInstallerCompatV16.class */
public class PackageInstallerCompatV16 extends PackageInstallerCompat {
    @Override // com.android.launcher3.compat.PackageInstallerCompat
    public void onStop() {
    }

    @Override // com.android.launcher3.compat.PackageInstallerCompat
    public HashMap<String, Integer> updateAndGetActiveSessionCache() {
        return new HashMap<>();
    }
}
