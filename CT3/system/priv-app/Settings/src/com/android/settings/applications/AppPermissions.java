package com.android.settings.applications;

import android.util.ArrayMap;
/* loaded from: classes.dex */
public final class AppPermissions {

    /* loaded from: classes.dex */
    private static final class Permission {
    }

    /* loaded from: classes.dex */
    private static final class PermissionGroup {
        private final ArrayMap<String, Permission> mPermissions = new ArrayMap<>();

        private PermissionGroup() {
        }
    }
}
