package android.support.v7.media;

import android.os.Bundle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/* loaded from: classes.dex */
public final class MediaRouteProviderDescriptor {
    private final Bundle mBundle;
    private List<MediaRouteDescriptor> mRoutes;

    private MediaRouteProviderDescriptor(Bundle bundle, List<MediaRouteDescriptor> routes) {
        this.mBundle = bundle;
        this.mRoutes = routes;
    }

    public List<MediaRouteDescriptor> getRoutes() {
        ensureRoutes();
        return this.mRoutes;
    }

    private void ensureRoutes() {
        if (this.mRoutes == null) {
            ArrayList<Bundle> routeBundles = this.mBundle.getParcelableArrayList("routes");
            if (routeBundles == null || routeBundles.isEmpty()) {
                this.mRoutes = Collections.emptyList();
                return;
            }
            int count = routeBundles.size();
            this.mRoutes = new ArrayList(count);
            for (int i = 0; i < count; i++) {
                this.mRoutes.add(MediaRouteDescriptor.fromBundle(routeBundles.get(i)));
            }
        }
    }

    public boolean isValid() {
        ensureRoutes();
        int routeCount = this.mRoutes.size();
        for (int i = 0; i < routeCount; i++) {
            MediaRouteDescriptor route = this.mRoutes.get(i);
            if (route == null || !route.isValid()) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return "MediaRouteProviderDescriptor{ routes=" + Arrays.toString(getRoutes().toArray()) + ", isValid=" + isValid() + " }";
    }

    public static MediaRouteProviderDescriptor fromBundle(Bundle bundle) {
        if (bundle != null) {
            return new MediaRouteProviderDescriptor(bundle, null);
        }
        return null;
    }

    /* loaded from: classes.dex */
    public static final class Builder {
        private final Bundle mBundle = new Bundle();
        private ArrayList<MediaRouteDescriptor> mRoutes;

        public Builder addRoute(MediaRouteDescriptor route) {
            if (route == null) {
                throw new IllegalArgumentException("route must not be null");
            }
            if (this.mRoutes == null) {
                this.mRoutes = new ArrayList<>();
            } else if (this.mRoutes.contains(route)) {
                throw new IllegalArgumentException("route descriptor already added");
            }
            this.mRoutes.add(route);
            return this;
        }

        public MediaRouteProviderDescriptor build() {
            if (this.mRoutes != null) {
                int count = this.mRoutes.size();
                ArrayList<Bundle> routeBundles = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    routeBundles.add(this.mRoutes.get(i).asBundle());
                }
                this.mBundle.putParcelableArrayList("routes", routeBundles);
            }
            return new MediaRouteProviderDescriptor(this.mBundle, this.mRoutes);
        }
    }
}
