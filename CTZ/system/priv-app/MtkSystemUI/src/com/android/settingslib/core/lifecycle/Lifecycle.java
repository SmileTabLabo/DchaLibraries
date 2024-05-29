package com.android.settingslib.core.lifecycle;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settingslib.core.lifecycle.events.OnAttach;
import com.android.settingslib.core.lifecycle.events.OnCreate;
import com.android.settingslib.core.lifecycle.events.OnCreateOptionsMenu;
import com.android.settingslib.core.lifecycle.events.OnDestroy;
import com.android.settingslib.core.lifecycle.events.OnOptionsItemSelected;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnPrepareOptionsMenu;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.core.lifecycle.events.OnSaveInstanceState;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.android.settingslib.utils.ThreadUtils;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class Lifecycle extends LifecycleRegistry {
    private final List<LifecycleObserver> mObservers;
    private final LifecycleProxy mProxy;

    public Lifecycle(LifecycleOwner lifecycleOwner) {
        super(lifecycleOwner);
        this.mObservers = new ArrayList();
        this.mProxy = new LifecycleProxy();
        addObserver(this.mProxy);
    }

    @Override // android.arch.lifecycle.LifecycleRegistry, android.arch.lifecycle.Lifecycle
    public void addObserver(android.arch.lifecycle.LifecycleObserver lifecycleObserver) {
        ThreadUtils.ensureMainThread();
        super.addObserver(lifecycleObserver);
        if (lifecycleObserver instanceof LifecycleObserver) {
            this.mObservers.add((LifecycleObserver) lifecycleObserver);
        }
    }

    @Override // android.arch.lifecycle.LifecycleRegistry, android.arch.lifecycle.Lifecycle
    public void removeObserver(android.arch.lifecycle.LifecycleObserver lifecycleObserver) {
        ThreadUtils.ensureMainThread();
        super.removeObserver(lifecycleObserver);
        if (lifecycleObserver instanceof LifecycleObserver) {
            this.mObservers.remove(lifecycleObserver);
        }
    }

    public void onAttach(Context context) {
        int size = this.mObservers.size();
        for (int i = 0; i < size; i++) {
            LifecycleObserver lifecycleObserver = this.mObservers.get(i);
            if (lifecycleObserver instanceof OnAttach) {
                ((OnAttach) lifecycleObserver).onAttach(context);
            }
        }
    }

    public void onCreate(Bundle bundle) {
        int size = this.mObservers.size();
        for (int i = 0; i < size; i++) {
            LifecycleObserver lifecycleObserver = this.mObservers.get(i);
            if (lifecycleObserver instanceof OnCreate) {
                ((OnCreate) lifecycleObserver).onCreate(bundle);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onStart() {
        int size = this.mObservers.size();
        for (int i = 0; i < size; i++) {
            LifecycleObserver lifecycleObserver = this.mObservers.get(i);
            if (lifecycleObserver instanceof OnStart) {
                ((OnStart) lifecycleObserver).onStart();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onResume() {
        int size = this.mObservers.size();
        for (int i = 0; i < size; i++) {
            LifecycleObserver lifecycleObserver = this.mObservers.get(i);
            if (lifecycleObserver instanceof OnResume) {
                ((OnResume) lifecycleObserver).onResume();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPause() {
        int size = this.mObservers.size();
        for (int i = 0; i < size; i++) {
            LifecycleObserver lifecycleObserver = this.mObservers.get(i);
            if (lifecycleObserver instanceof OnPause) {
                ((OnPause) lifecycleObserver).onPause();
            }
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        int size = this.mObservers.size();
        for (int i = 0; i < size; i++) {
            LifecycleObserver lifecycleObserver = this.mObservers.get(i);
            if (lifecycleObserver instanceof OnSaveInstanceState) {
                ((OnSaveInstanceState) lifecycleObserver).onSaveInstanceState(bundle);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onStop() {
        int size = this.mObservers.size();
        for (int i = 0; i < size; i++) {
            LifecycleObserver lifecycleObserver = this.mObservers.get(i);
            if (lifecycleObserver instanceof OnStop) {
                ((OnStop) lifecycleObserver).onStop();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDestroy() {
        int size = this.mObservers.size();
        for (int i = 0; i < size; i++) {
            LifecycleObserver lifecycleObserver = this.mObservers.get(i);
            if (lifecycleObserver instanceof OnDestroy) {
                ((OnDestroy) lifecycleObserver).onDestroy();
            }
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        int size = this.mObservers.size();
        for (int i = 0; i < size; i++) {
            LifecycleObserver lifecycleObserver = this.mObservers.get(i);
            if (lifecycleObserver instanceof OnCreateOptionsMenu) {
                ((OnCreateOptionsMenu) lifecycleObserver).onCreateOptionsMenu(menu, menuInflater);
            }
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        int size = this.mObservers.size();
        for (int i = 0; i < size; i++) {
            LifecycleObserver lifecycleObserver = this.mObservers.get(i);
            if (lifecycleObserver instanceof OnPrepareOptionsMenu) {
                ((OnPrepareOptionsMenu) lifecycleObserver).onPrepareOptionsMenu(menu);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int size = this.mObservers.size();
        for (int i = 0; i < size; i++) {
            LifecycleObserver lifecycleObserver = this.mObservers.get(i);
            if ((lifecycleObserver instanceof OnOptionsItemSelected) && ((OnOptionsItemSelected) lifecycleObserver).onOptionsItemSelected(menuItem)) {
                return true;
            }
        }
        return false;
    }

    /* loaded from: classes.dex */
    private class LifecycleProxy implements android.arch.lifecycle.LifecycleObserver {
        private LifecycleProxy() {
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
        public void onLifecycleEvent(LifecycleOwner lifecycleOwner, Lifecycle.Event event) {
            switch (event) {
                case ON_CREATE:
                default:
                    return;
                case ON_START:
                    Lifecycle.this.onStart();
                    return;
                case ON_RESUME:
                    Lifecycle.this.onResume();
                    return;
                case ON_PAUSE:
                    Lifecycle.this.onPause();
                    return;
                case ON_STOP:
                    Lifecycle.this.onStop();
                    return;
                case ON_DESTROY:
                    Lifecycle.this.onDestroy();
                    return;
                case ON_ANY:
                    Log.wtf("LifecycleObserver", "Should not receive an 'ANY' event!");
                    return;
            }
        }
    }
}
