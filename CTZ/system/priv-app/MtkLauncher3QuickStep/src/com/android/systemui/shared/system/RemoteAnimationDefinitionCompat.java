package com.android.systemui.shared.system;

import android.view.RemoteAnimationDefinition;
/* loaded from: classes.dex */
public class RemoteAnimationDefinitionCompat {
    private final RemoteAnimationDefinition mWrapped = new RemoteAnimationDefinition();

    public void addRemoteAnimation(int transition, RemoteAnimationAdapterCompat adapter) {
        this.mWrapped.addRemoteAnimation(transition, adapter.getWrapped());
    }

    public void addRemoteAnimation(int transition, int activityTypeFilter, RemoteAnimationAdapterCompat adapter) {
        this.mWrapped.addRemoteAnimation(transition, activityTypeFilter, adapter.getWrapped());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public RemoteAnimationDefinition getWrapped() {
        return this.mWrapped;
    }
}
