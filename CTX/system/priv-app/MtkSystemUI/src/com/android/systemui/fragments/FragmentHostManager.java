package com.android.systemui.fragments;

import android.app.Fragment;
import android.app.FragmentController;
import android.app.FragmentHostCallback;
import android.app.FragmentManager;
import android.app.FragmentManagerNonConfig;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import com.android.settingslib.applications.InterestingConfigChanges;
import com.android.systemui.Dependency;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.util.leak.LeakDetector;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
/* loaded from: classes.dex */
public class FragmentHostManager {
    private final Context mContext;
    private FragmentController mFragments;
    private FragmentManager.FragmentLifecycleCallbacks mLifecycleCallbacks;
    private final FragmentService mManager;
    private final View mRootView;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final HashMap<String, ArrayList<FragmentListener>> mListeners = new HashMap<>();
    private final InterestingConfigChanges mConfigChanges = new InterestingConfigChanges(-1073741564);
    private final ExtensionFragmentManager mPlugins = new ExtensionFragmentManager();

    /* JADX INFO: Access modifiers changed from: package-private */
    public FragmentHostManager(Context context, FragmentService fragmentService, View view) {
        this.mContext = context;
        this.mManager = fragmentService;
        this.mRootView = view;
        this.mConfigChanges.applyNewConfig(context.getResources());
        createFragmentHost(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void createFragmentHost(Parcelable parcelable) {
        this.mFragments = FragmentController.createController(new HostCallbacks());
        this.mFragments.attachHost(null);
        this.mLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() { // from class: com.android.systemui.fragments.FragmentHostManager.1
            @Override // android.app.FragmentManager.FragmentLifecycleCallbacks
            public void onFragmentViewCreated(FragmentManager fragmentManager, Fragment fragment, View view, Bundle bundle) {
                FragmentHostManager.this.onFragmentViewCreated(fragment);
            }

            @Override // android.app.FragmentManager.FragmentLifecycleCallbacks
            public void onFragmentViewDestroyed(FragmentManager fragmentManager, Fragment fragment) {
                FragmentHostManager.this.onFragmentViewDestroyed(fragment);
            }

            @Override // android.app.FragmentManager.FragmentLifecycleCallbacks
            public void onFragmentDestroyed(FragmentManager fragmentManager, Fragment fragment) {
                ((LeakDetector) Dependency.get(LeakDetector.class)).trackGarbage(fragment);
            }
        };
        this.mFragments.getFragmentManager().registerFragmentLifecycleCallbacks(this.mLifecycleCallbacks, true);
        if (parcelable != null) {
            this.mFragments.restoreAllState(parcelable, (FragmentManagerNonConfig) null);
        }
        this.mFragments.dispatchCreate();
        this.mFragments.dispatchStart();
        this.mFragments.dispatchResume();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Parcelable destroyFragmentHost() {
        this.mFragments.dispatchPause();
        Parcelable saveAllState = this.mFragments.saveAllState();
        this.mFragments.dispatchStop();
        this.mFragments.dispatchDestroy();
        this.mFragments.getFragmentManager().unregisterFragmentLifecycleCallbacks(this.mLifecycleCallbacks);
        return saveAllState;
    }

    public FragmentHostManager addTagListener(String str, FragmentListener fragmentListener) {
        ArrayList<FragmentListener> arrayList = this.mListeners.get(str);
        if (arrayList == null) {
            arrayList = new ArrayList<>();
            this.mListeners.put(str, arrayList);
        }
        arrayList.add(fragmentListener);
        Fragment findFragmentByTag = getFragmentManager().findFragmentByTag(str);
        if (findFragmentByTag != null && findFragmentByTag.getView() != null) {
            fragmentListener.onFragmentViewCreated(str, findFragmentByTag);
        }
        return this;
    }

    public void removeTagListener(String str, FragmentListener fragmentListener) {
        ArrayList<FragmentListener> arrayList = this.mListeners.get(str);
        if (arrayList != null && arrayList.remove(fragmentListener) && arrayList.size() == 0) {
            this.mListeners.remove(str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onFragmentViewCreated(final Fragment fragment) {
        final String tag = fragment.getTag();
        ArrayList<FragmentListener> arrayList = this.mListeners.get(tag);
        if (arrayList != null) {
            arrayList.forEach(new Consumer() { // from class: com.android.systemui.fragments.-$$Lambda$FragmentHostManager$OsWXqtcfRJZBAvEEeN8CG6EN5T4
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((FragmentHostManager.FragmentListener) obj).onFragmentViewCreated(tag, fragment);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onFragmentViewDestroyed(final Fragment fragment) {
        final String tag = fragment.getTag();
        ArrayList<FragmentListener> arrayList = this.mListeners.get(tag);
        if (arrayList != null) {
            arrayList.forEach(new Consumer() { // from class: com.android.systemui.fragments.-$$Lambda$FragmentHostManager$AcJHY99nHc-JEzu3q8ny-wMOZ4E
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((FragmentHostManager.FragmentListener) obj).onFragmentViewDestroyed(tag, fragment);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        if (this.mConfigChanges.applyNewConfig(this.mContext.getResources())) {
            createFragmentHost(destroyFragmentHost());
        } else {
            this.mFragments.dispatchConfigurationChanged(configuration);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public <T extends View> T findViewById(int i) {
        return (T) this.mRootView.findViewById(i);
    }

    public FragmentManager getFragmentManager() {
        return this.mFragments.getFragmentManager();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ExtensionFragmentManager getExtensionManager() {
        return this.mPlugins;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void destroy() {
        this.mFragments.dispatchDestroy();
    }

    /* loaded from: classes.dex */
    public interface FragmentListener {
        void onFragmentViewCreated(String str, Fragment fragment);

        default void onFragmentViewDestroyed(String str, Fragment fragment) {
        }
    }

    public static FragmentHostManager get(View view) {
        try {
            return ((FragmentService) Dependency.get(FragmentService.class)).getFragmentHostManager(view);
        } catch (ClassCastException e) {
            throw e;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class HostCallbacks extends FragmentHostCallback<FragmentHostManager> {
        public HostCallbacks() {
            super(FragmentHostManager.this.mContext, FragmentHostManager.this.mHandler, 0);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.app.FragmentHostCallback
        public FragmentHostManager onGetHost() {
            return FragmentHostManager.this;
        }

        @Override // android.app.FragmentHostCallback
        public void onDump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
            FragmentHostManager.this.dump(str, fileDescriptor, printWriter, strArr);
        }

        public Fragment instantiate(Context context, String str, Bundle bundle) {
            return FragmentHostManager.this.mPlugins.instantiate(context, str, bundle);
        }

        @Override // android.app.FragmentHostCallback
        public boolean onShouldSaveFragmentState(Fragment fragment) {
            return true;
        }

        @Override // android.app.FragmentHostCallback
        public LayoutInflater onGetLayoutInflater() {
            return LayoutInflater.from(FragmentHostManager.this.mContext);
        }

        @Override // android.app.FragmentHostCallback
        public boolean onUseFragmentManagerInflaterFactory() {
            return true;
        }

        @Override // android.app.FragmentHostCallback
        public boolean onHasWindowAnimations() {
            return false;
        }

        @Override // android.app.FragmentHostCallback
        public int onGetWindowAnimations() {
            return 0;
        }

        @Override // android.app.FragmentHostCallback
        public void onAttachFragment(Fragment fragment) {
        }

        @Override // android.app.FragmentHostCallback, android.app.FragmentContainer
        public <T extends View> T onFindViewById(int i) {
            return (T) FragmentHostManager.this.findViewById(i);
        }

        @Override // android.app.FragmentHostCallback, android.app.FragmentContainer
        public boolean onHasView() {
            return true;
        }
    }

    /* loaded from: classes.dex */
    class ExtensionFragmentManager {
        private final ArrayMap<String, Context> mExtensionLookup = new ArrayMap<>();

        ExtensionFragmentManager() {
        }

        public void setCurrentExtension(int i, String str, String str2, String str3, Context context) {
            if (str2 != null) {
                this.mExtensionLookup.remove(str2);
            }
            this.mExtensionLookup.put(str3, context);
            FragmentHostManager.this.getFragmentManager().beginTransaction().replace(i, instantiate(context, str3, null), str).commit();
            reloadFragments();
        }

        private void reloadFragments() {
            FragmentHostManager.this.createFragmentHost(FragmentHostManager.this.destroyFragmentHost());
        }

        Fragment instantiate(Context context, String str, Bundle bundle) {
            Context context2 = this.mExtensionLookup.get(str);
            if (context2 != null) {
                Fragment instantiate = Fragment.instantiate(context2, str, bundle);
                if (instantiate instanceof Plugin) {
                    ((Plugin) instantiate).onCreate(FragmentHostManager.this.mContext, context2);
                }
                return instantiate;
            }
            return Fragment.instantiate(context, str, bundle);
        }
    }
}
