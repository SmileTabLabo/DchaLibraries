package android.support.transition;

import android.view.View;
import android.view.ViewGroup;
/* loaded from: classes.dex */
public class Scene {
    private Runnable mExitAction;
    private ViewGroup mSceneRoot;

    public void exit() {
        if (getCurrentScene(this.mSceneRoot) == this && this.mExitAction != null) {
            this.mExitAction.run();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setCurrentScene(View view, Scene scene) {
        view.setTag(R.id.transition_current_scene, scene);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Scene getCurrentScene(View view) {
        return (Scene) view.getTag(R.id.transition_current_scene);
    }
}
