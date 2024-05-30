package android.support.v7.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.media.MediaRouteProvider;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.util.SparseArray;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class RegisteredMediaRouteProvider extends MediaRouteProvider implements ServiceConnection {
    static final boolean DEBUG = Log.isLoggable("MediaRouteProviderProxy", 3);
    private Connection mActiveConnection;
    private boolean mBound;
    private final ComponentName mComponentName;
    private boolean mConnectionReady;
    private final ArrayList<Controller> mControllers;
    final PrivateHandler mPrivateHandler;
    private boolean mStarted;

    public RegisteredMediaRouteProvider(Context context, ComponentName componentName) {
        super(context, new MediaRouteProvider.ProviderMetadata(componentName));
        this.mControllers = new ArrayList<>();
        this.mComponentName = componentName;
        this.mPrivateHandler = new PrivateHandler();
    }

    @Override // android.support.v7.media.MediaRouteProvider
    public MediaRouteProvider.RouteController onCreateRouteController(String routeId) {
        if (routeId == null) {
            throw new IllegalArgumentException("routeId cannot be null");
        }
        return createRouteController(routeId, null);
    }

    @Override // android.support.v7.media.MediaRouteProvider
    public MediaRouteProvider.RouteController onCreateRouteController(String routeId, String routeGroupId) {
        if (routeId == null) {
            throw new IllegalArgumentException("routeId cannot be null");
        }
        if (routeGroupId == null) {
            throw new IllegalArgumentException("routeGroupId cannot be null");
        }
        return createRouteController(routeId, routeGroupId);
    }

    @Override // android.support.v7.media.MediaRouteProvider
    public void onDiscoveryRequestChanged(MediaRouteDiscoveryRequest request) {
        if (this.mConnectionReady) {
            this.mActiveConnection.setDiscoveryRequest(request);
        }
        updateBinding();
    }

    @Override // android.content.ServiceConnection
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (DEBUG) {
            Log.d("MediaRouteProviderProxy", this + ": Connected");
        }
        if (this.mBound) {
            disconnect();
            Messenger messenger = service != null ? new Messenger(service) : null;
            if (MediaRouteProviderProtocol.isValidRemoteMessenger(messenger)) {
                Connection connection = new Connection(messenger);
                if (connection.register()) {
                    this.mActiveConnection = connection;
                    return;
                } else if (DEBUG) {
                    Log.d("MediaRouteProviderProxy", this + ": Registration failed");
                    return;
                } else {
                    return;
                }
            }
            Log.e("MediaRouteProviderProxy", this + ": Service returned invalid messenger binder");
        }
    }

    @Override // android.content.ServiceConnection
    public void onServiceDisconnected(ComponentName name) {
        if (DEBUG) {
            Log.d("MediaRouteProviderProxy", this + ": Service disconnected");
        }
        disconnect();
    }

    public String toString() {
        return "Service connection " + this.mComponentName.flattenToShortString();
    }

    public boolean hasComponentName(String packageName, String className) {
        return this.mComponentName.getPackageName().equals(packageName) && this.mComponentName.getClassName().equals(className);
    }

    public void start() {
        if (!this.mStarted) {
            if (DEBUG) {
                Log.d("MediaRouteProviderProxy", this + ": Starting");
            }
            this.mStarted = true;
            updateBinding();
        }
    }

    public void stop() {
        if (this.mStarted) {
            if (DEBUG) {
                Log.d("MediaRouteProviderProxy", this + ": Stopping");
            }
            this.mStarted = false;
            updateBinding();
        }
    }

    public void rebindIfDisconnected() {
        if (this.mActiveConnection == null && shouldBind()) {
            unbind();
            bind();
        }
    }

    private void updateBinding() {
        if (shouldBind()) {
            bind();
        } else {
            unbind();
        }
    }

    private boolean shouldBind() {
        if (this.mStarted) {
            return (getDiscoveryRequest() == null && this.mControllers.isEmpty()) ? false : true;
        }
        return false;
    }

    private void bind() {
        if (!this.mBound) {
            if (DEBUG) {
                Log.d("MediaRouteProviderProxy", this + ": Binding");
            }
            Intent service = new Intent("android.media.MediaRouteProviderService");
            service.setComponent(this.mComponentName);
            try {
                this.mBound = getContext().bindService(service, this, 1);
                if (!this.mBound && DEBUG) {
                    Log.d("MediaRouteProviderProxy", this + ": Bind failed");
                }
            } catch (SecurityException ex) {
                if (DEBUG) {
                    Log.d("MediaRouteProviderProxy", this + ": Bind failed", ex);
                }
            }
        }
    }

    private void unbind() {
        if (this.mBound) {
            if (DEBUG) {
                Log.d("MediaRouteProviderProxy", this + ": Unbinding");
            }
            this.mBound = false;
            disconnect();
            getContext().unbindService(this);
        }
    }

    private MediaRouteProvider.RouteController createRouteController(String routeId, String routeGroupId) {
        MediaRouteProviderDescriptor descriptor = getDescriptor();
        if (descriptor != null) {
            List<MediaRouteDescriptor> routes = descriptor.getRoutes();
            int count = routes.size();
            for (int i = 0; i < count; i++) {
                MediaRouteDescriptor route = routes.get(i);
                if (route.getId().equals(routeId)) {
                    Controller controller = new Controller(routeId, routeGroupId);
                    this.mControllers.add(controller);
                    if (this.mConnectionReady) {
                        controller.attachConnection(this.mActiveConnection);
                    }
                    updateBinding();
                    return controller;
                }
            }
            return null;
        }
        return null;
    }

    void onConnectionReady(Connection connection) {
        if (this.mActiveConnection == connection) {
            this.mConnectionReady = true;
            attachControllersToConnection();
            MediaRouteDiscoveryRequest request = getDiscoveryRequest();
            if (request != null) {
                this.mActiveConnection.setDiscoveryRequest(request);
            }
        }
    }

    void onConnectionDied(Connection connection) {
        if (this.mActiveConnection == connection) {
            if (DEBUG) {
                Log.d("MediaRouteProviderProxy", this + ": Service connection died");
            }
            disconnect();
        }
    }

    void onConnectionError(Connection connection, String error) {
        if (this.mActiveConnection == connection) {
            if (DEBUG) {
                Log.d("MediaRouteProviderProxy", this + ": Service connection error - " + error);
            }
            unbind();
        }
    }

    void onConnectionDescriptorChanged(Connection connection, MediaRouteProviderDescriptor descriptor) {
        if (this.mActiveConnection == connection) {
            if (DEBUG) {
                Log.d("MediaRouteProviderProxy", this + ": Descriptor changed, descriptor=" + descriptor);
            }
            setDescriptor(descriptor);
        }
    }

    private void disconnect() {
        if (this.mActiveConnection != null) {
            setDescriptor(null);
            this.mConnectionReady = false;
            detachControllersFromConnection();
            this.mActiveConnection.dispose();
            this.mActiveConnection = null;
        }
    }

    void onControllerReleased(Controller controller) {
        this.mControllers.remove(controller);
        controller.detachConnection();
        updateBinding();
    }

    private void attachControllersToConnection() {
        int count = this.mControllers.size();
        for (int i = 0; i < count; i++) {
            this.mControllers.get(i).attachConnection(this.mActiveConnection);
        }
    }

    private void detachControllersFromConnection() {
        int count = this.mControllers.size();
        for (int i = 0; i < count; i++) {
            this.mControllers.get(i).detachConnection();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class Controller extends MediaRouteProvider.RouteController {
        private Connection mConnection;
        private int mControllerId;
        private int mPendingSetVolume = -1;
        private int mPendingUpdateVolumeDelta;
        private final String mRouteGroupId;
        private final String mRouteId;
        private boolean mSelected;

        public Controller(String routeId, String routeGroupId) {
            this.mRouteId = routeId;
            this.mRouteGroupId = routeGroupId;
        }

        public void attachConnection(Connection connection) {
            this.mConnection = connection;
            this.mControllerId = connection.createRouteController(this.mRouteId, this.mRouteGroupId);
            if (this.mSelected) {
                connection.selectRoute(this.mControllerId);
                if (this.mPendingSetVolume >= 0) {
                    connection.setVolume(this.mControllerId, this.mPendingSetVolume);
                    this.mPendingSetVolume = -1;
                }
                if (this.mPendingUpdateVolumeDelta != 0) {
                    connection.updateVolume(this.mControllerId, this.mPendingUpdateVolumeDelta);
                    this.mPendingUpdateVolumeDelta = 0;
                }
            }
        }

        public void detachConnection() {
            if (this.mConnection != null) {
                this.mConnection.releaseRouteController(this.mControllerId);
                this.mConnection = null;
                this.mControllerId = 0;
            }
        }

        @Override // android.support.v7.media.MediaRouteProvider.RouteController
        public void onRelease() {
            RegisteredMediaRouteProvider.this.onControllerReleased(this);
        }

        @Override // android.support.v7.media.MediaRouteProvider.RouteController
        public void onSelect() {
            this.mSelected = true;
            if (this.mConnection != null) {
                this.mConnection.selectRoute(this.mControllerId);
            }
        }

        @Override // android.support.v7.media.MediaRouteProvider.RouteController
        public void onUnselect() {
            onUnselect(0);
        }

        @Override // android.support.v7.media.MediaRouteProvider.RouteController
        public void onUnselect(int reason) {
            this.mSelected = false;
            if (this.mConnection != null) {
                this.mConnection.unselectRoute(this.mControllerId, reason);
            }
        }

        @Override // android.support.v7.media.MediaRouteProvider.RouteController
        public void onSetVolume(int volume) {
            if (this.mConnection != null) {
                this.mConnection.setVolume(this.mControllerId, volume);
                return;
            }
            this.mPendingSetVolume = volume;
            this.mPendingUpdateVolumeDelta = 0;
        }

        @Override // android.support.v7.media.MediaRouteProvider.RouteController
        public void onUpdateVolume(int delta) {
            if (this.mConnection != null) {
                this.mConnection.updateVolume(this.mControllerId, delta);
            } else {
                this.mPendingUpdateVolumeDelta += delta;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class Connection implements IBinder.DeathRecipient {
        private int mPendingRegisterRequestId;
        private final Messenger mServiceMessenger;
        private int mServiceVersion;
        private int mNextRequestId = 1;
        private int mNextControllerId = 1;
        private final SparseArray<MediaRouter.ControlRequestCallback> mPendingCallbacks = new SparseArray<>();
        private final ReceiveHandler mReceiveHandler = new ReceiveHandler(this);
        private final Messenger mReceiveMessenger = new Messenger(this.mReceiveHandler);

        public Connection(Messenger serviceMessenger) {
            this.mServiceMessenger = serviceMessenger;
        }

        public boolean register() {
            int i = this.mNextRequestId;
            this.mNextRequestId = i + 1;
            this.mPendingRegisterRequestId = i;
            if (sendRequest(1, this.mPendingRegisterRequestId, 2, null, null)) {
                try {
                    this.mServiceMessenger.getBinder().linkToDeath(this, 0);
                    return true;
                } catch (RemoteException e) {
                    binderDied();
                    return false;
                }
            }
            return false;
        }

        public void dispose() {
            sendRequest(2, 0, 0, null, null);
            this.mReceiveHandler.dispose();
            this.mServiceMessenger.getBinder().unlinkToDeath(this, 0);
            RegisteredMediaRouteProvider.this.mPrivateHandler.post(new Runnable() { // from class: android.support.v7.media.RegisteredMediaRouteProvider.Connection.1
                @Override // java.lang.Runnable
                public void run() {
                    Connection.this.failPendingCallbacks();
                }
            });
        }

        void failPendingCallbacks() {
            for (int i = 0; i < this.mPendingCallbacks.size(); i++) {
                this.mPendingCallbacks.valueAt(i).onError(null, null);
            }
            this.mPendingCallbacks.clear();
        }

        public boolean onGenericFailure(int requestId) {
            if (requestId == this.mPendingRegisterRequestId) {
                this.mPendingRegisterRequestId = 0;
                RegisteredMediaRouteProvider.this.onConnectionError(this, "Registration failed");
            }
            MediaRouter.ControlRequestCallback callback = this.mPendingCallbacks.get(requestId);
            if (callback != null) {
                this.mPendingCallbacks.remove(requestId);
                callback.onError(null, null);
                return true;
            }
            return true;
        }

        public boolean onGenericSuccess(int requestId) {
            return true;
        }

        public boolean onRegistered(int requestId, int serviceVersion, Bundle descriptorBundle) {
            if (this.mServiceVersion == 0 && requestId == this.mPendingRegisterRequestId && serviceVersion >= 1) {
                this.mPendingRegisterRequestId = 0;
                this.mServiceVersion = serviceVersion;
                RegisteredMediaRouteProvider.this.onConnectionDescriptorChanged(this, MediaRouteProviderDescriptor.fromBundle(descriptorBundle));
                RegisteredMediaRouteProvider.this.onConnectionReady(this);
                return true;
            }
            return false;
        }

        public boolean onDescriptorChanged(Bundle descriptorBundle) {
            if (this.mServiceVersion != 0) {
                RegisteredMediaRouteProvider.this.onConnectionDescriptorChanged(this, MediaRouteProviderDescriptor.fromBundle(descriptorBundle));
                return true;
            }
            return false;
        }

        public boolean onControlRequestSucceeded(int requestId, Bundle data) {
            MediaRouter.ControlRequestCallback callback = this.mPendingCallbacks.get(requestId);
            if (callback != null) {
                this.mPendingCallbacks.remove(requestId);
                callback.onResult(data);
                return true;
            }
            return false;
        }

        public boolean onControlRequestFailed(int requestId, String error, Bundle data) {
            MediaRouter.ControlRequestCallback callback = this.mPendingCallbacks.get(requestId);
            if (callback != null) {
                this.mPendingCallbacks.remove(requestId);
                callback.onError(error, data);
                return true;
            }
            return false;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            RegisteredMediaRouteProvider.this.mPrivateHandler.post(new Runnable() { // from class: android.support.v7.media.RegisteredMediaRouteProvider.Connection.2
                @Override // java.lang.Runnable
                public void run() {
                    RegisteredMediaRouteProvider.this.onConnectionDied(Connection.this);
                }
            });
        }

        public int createRouteController(String routeId, String routeGroupId) {
            int controllerId = this.mNextControllerId;
            this.mNextControllerId = controllerId + 1;
            Bundle data = new Bundle();
            data.putString("routeId", routeId);
            data.putString("routeGroupId", routeGroupId);
            int i = this.mNextRequestId;
            this.mNextRequestId = i + 1;
            sendRequest(3, i, controllerId, null, data);
            return controllerId;
        }

        public void releaseRouteController(int controllerId) {
            int i = this.mNextRequestId;
            this.mNextRequestId = i + 1;
            sendRequest(4, i, controllerId, null, null);
        }

        public void selectRoute(int controllerId) {
            int i = this.mNextRequestId;
            this.mNextRequestId = i + 1;
            sendRequest(5, i, controllerId, null, null);
        }

        public void unselectRoute(int controllerId, int reason) {
            Bundle extras = new Bundle();
            extras.putInt("unselectReason", reason);
            int i = this.mNextRequestId;
            this.mNextRequestId = i + 1;
            sendRequest(6, i, controllerId, null, extras);
        }

        public void setVolume(int controllerId, int volume) {
            Bundle data = new Bundle();
            data.putInt("volume", volume);
            int i = this.mNextRequestId;
            this.mNextRequestId = i + 1;
            sendRequest(7, i, controllerId, null, data);
        }

        public void updateVolume(int controllerId, int delta) {
            Bundle data = new Bundle();
            data.putInt("volume", delta);
            int i = this.mNextRequestId;
            this.mNextRequestId = i + 1;
            sendRequest(8, i, controllerId, null, data);
        }

        public void setDiscoveryRequest(MediaRouteDiscoveryRequest request) {
            int i = this.mNextRequestId;
            this.mNextRequestId = i + 1;
            sendRequest(10, i, 0, request != null ? request.asBundle() : null, null);
        }

        private boolean sendRequest(int what, int requestId, int arg, Object obj, Bundle data) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.arg1 = requestId;
            msg.arg2 = arg;
            msg.obj = obj;
            msg.setData(data);
            msg.replyTo = this.mReceiveMessenger;
            try {
                this.mServiceMessenger.send(msg);
                return true;
            } catch (DeadObjectException e) {
                return false;
            } catch (RemoteException ex) {
                if (what != 2) {
                    Log.e("MediaRouteProviderProxy", "Could not send message to service.", ex);
                    return false;
                }
                return false;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class PrivateHandler extends Handler {
        PrivateHandler() {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class ReceiveHandler extends Handler {
        private final WeakReference<Connection> mConnectionRef;

        public ReceiveHandler(Connection connection) {
            this.mConnectionRef = new WeakReference<>(connection);
        }

        public void dispose() {
            this.mConnectionRef.clear();
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            Connection connection = this.mConnectionRef.get();
            if (connection != null) {
                int what = msg.what;
                int requestId = msg.arg1;
                int arg = msg.arg2;
                Object obj = msg.obj;
                Bundle data = msg.peekData();
                if (!processMessage(connection, what, requestId, arg, obj, data) && RegisteredMediaRouteProvider.DEBUG) {
                    Log.d("MediaRouteProviderProxy", "Unhandled message from server: " + msg);
                }
            }
        }

        private boolean processMessage(Connection connection, int what, int requestId, int arg, Object obj, Bundle data) {
            switch (what) {
                case 0:
                    connection.onGenericFailure(requestId);
                    return true;
                case 1:
                    connection.onGenericSuccess(requestId);
                    return true;
                case 2:
                    if (obj == null || (obj instanceof Bundle)) {
                        return connection.onRegistered(requestId, arg, (Bundle) obj);
                    }
                    return false;
                case 3:
                    if (obj == null || (obj instanceof Bundle)) {
                        return connection.onControlRequestSucceeded(requestId, (Bundle) obj);
                    }
                    return false;
                case 4:
                    if (obj == null || (obj instanceof Bundle)) {
                        String error = data == null ? null : data.getString("error");
                        return connection.onControlRequestFailed(requestId, error, (Bundle) obj);
                    }
                    return false;
                case 5:
                    if (obj == null || (obj instanceof Bundle)) {
                        return connection.onDescriptorChanged((Bundle) obj);
                    }
                    return false;
                default:
                    return false;
            }
        }
    }
}
