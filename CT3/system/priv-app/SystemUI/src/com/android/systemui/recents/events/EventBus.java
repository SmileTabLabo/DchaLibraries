package com.android.systemui.recents.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.util.MutableBoolean;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/recents/events/EventBus.class */
public class EventBus extends BroadcastReceiver {
    private static EventBus sDefaultBus;
    private Handler mHandler;
    private static final Comparator<EventHandler> EVENT_HANDLER_COMPARATOR = new Comparator<EventHandler>() { // from class: com.android.systemui.recents.events.EventBus.1
        @Override // java.util.Comparator
        public int compare(EventHandler eventHandler, EventHandler eventHandler2) {
            return eventHandler.priority != eventHandler2.priority ? eventHandler2.priority - eventHandler.priority : Long.compare(eventHandler2.subscriber.registrationTime, eventHandler.subscriber.registrationTime);
        }
    };
    private static final Object sLock = new Object();
    private HashMap<Class<? extends Event>, ArrayList<EventHandler>> mEventTypeMap = new HashMap<>();
    private HashMap<Class<? extends Object>, ArrayList<EventHandlerMethod>> mSubscriberTypeMap = new HashMap<>();
    private HashMap<String, Class<? extends InterprocessEvent>> mInterprocessEventNameMap = new HashMap<>();
    private ArrayList<Subscriber> mSubscribers = new ArrayList<>();

    /* loaded from: a.zip:com/android/systemui/recents/events/EventBus$AnimatedEvent.class */
    public static class AnimatedEvent extends Event {
        private final ReferenceCountedTrigger mTrigger = new ReferenceCountedTrigger();

        public void addPostAnimationCallback(Runnable runnable) {
            this.mTrigger.addLastDecrementRunnable(runnable);
        }

        @Override // com.android.systemui.recents.events.EventBus.Event
        protected Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }

        public ReferenceCountedTrigger getAnimationTrigger() {
            return this.mTrigger;
        }

        @Override // com.android.systemui.recents.events.EventBus.Event
        void onPostDispatch() {
            this.mTrigger.decrement();
        }

        @Override // com.android.systemui.recents.events.EventBus.Event
        void onPreDispatch() {
            this.mTrigger.increment();
        }
    }

    /* loaded from: a.zip:com/android/systemui/recents/events/EventBus$Event.class */
    public static class Event implements Cloneable {
        boolean cancelled;
        boolean requiresPost;
        boolean trace;

        protected Object clone() throws CloneNotSupportedException {
            Event event = (Event) super.clone();
            event.cancelled = false;
            return event;
        }

        void onPostDispatch() {
        }

        void onPreDispatch() {
        }
    }

    /* loaded from: a.zip:com/android/systemui/recents/events/EventBus$InterprocessEvent.class */
    public static class InterprocessEvent extends Event {
    }

    /* loaded from: a.zip:com/android/systemui/recents/events/EventBus$ReusableEvent.class */
    public static class ReusableEvent extends Event {
        private int mDispatchCount;

        @Override // com.android.systemui.recents.events.EventBus.Event
        protected Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }

        @Override // com.android.systemui.recents.events.EventBus.Event
        void onPostDispatch() {
            super.onPostDispatch();
            this.mDispatchCount++;
        }
    }

    private EventBus(Looper looper) {
        this.mHandler = new Handler(looper);
    }

    private boolean findRegisteredSubscriber(Object obj, boolean z) {
        for (int size = this.mSubscribers.size() - 1; size >= 0; size--) {
            if (this.mSubscribers.get(size).getReference() == obj) {
                if (z) {
                    this.mSubscribers.remove(size);
                    return true;
                }
                return true;
            }
        }
        return false;
    }

    public static EventBus getDefault() {
        if (sDefaultBus == null) {
            synchronized (sLock) {
                if (sDefaultBus == null) {
                    sDefaultBus = new EventBus(Looper.getMainLooper());
                }
            }
        }
        return sDefaultBus;
    }

    private boolean isValidEventBusHandlerMethod(Method method, Class<?>[] clsArr, MutableBoolean mutableBoolean) {
        int modifiers = method.getModifiers();
        if (Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers) && method.getReturnType().equals(Void.TYPE) && clsArr.length == 1) {
            if (InterprocessEvent.class.isAssignableFrom(clsArr[0]) && method.getName().startsWith("onInterprocessBusEvent")) {
                mutableBoolean.value = true;
                return true;
            } else if (Event.class.isAssignableFrom(clsArr[0]) && method.getName().startsWith("onBusEvent")) {
                mutableBoolean.value = false;
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private static void logWithPid(String str) {
        Log.d("EventBus", "[" + Process.myPid() + ", u" + UserHandle.myUserId() + "] " + str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void processEvent(EventHandler eventHandler, Event event) {
        if (event.cancelled) {
            if (event.trace) {
                logWithPid("Event dispatch cancelled");
                return;
            }
            return;
        }
        try {
            if (event.trace) {
                logWithPid(" -> " + eventHandler.toString());
            }
            Object reference = eventHandler.subscriber.getReference();
            if (reference != null) {
                eventHandler.method.invoke(reference, event);
            } else {
                Log.e("EventBus", "Failed to deliver event to null subscriber");
            }
        } catch (IllegalAccessException e) {
            Log.e("EventBus", "Failed to invoke method", e.getCause());
        } catch (InvocationTargetException e2) {
            throw new RuntimeException(e2.getCause());
        }
    }

    private void queueEvent(Event event) {
        ArrayList<EventHandler> arrayList = this.mEventTypeMap.get(event.getClass());
        if (arrayList == null) {
            return;
        }
        boolean z = false;
        event.onPreDispatch();
        ArrayList arrayList2 = (ArrayList) arrayList.clone();
        int size = arrayList2.size();
        int i = 0;
        while (i < size) {
            EventHandler eventHandler = (EventHandler) arrayList2.get(i);
            boolean z2 = z;
            if (eventHandler.subscriber.getReference() != null) {
                if (event.requiresPost) {
                    this.mHandler.post(new Runnable(this, eventHandler, event) { // from class: com.android.systemui.recents.events.EventBus.4
                        final EventBus this$0;
                        final Event val$event;
                        final EventHandler val$eventHandler;

                        {
                            this.this$0 = this;
                            this.val$eventHandler = eventHandler;
                            this.val$event = event;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            this.this$0.processEvent(this.val$eventHandler, this.val$event);
                        }
                    });
                    z2 = true;
                } else {
                    processEvent(eventHandler, event);
                    z2 = z;
                }
            }
            i++;
            z = z2;
        }
        if (z) {
            this.mHandler.post(new Runnable(this, event) { // from class: com.android.systemui.recents.events.EventBus.5
                final EventBus this$0;
                final Event val$event;

                {
                    this.this$0 = this;
                    this.val$event = event;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.val$event.onPostDispatch();
                }
            });
        } else {
            event.onPostDispatch();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void registerSubscriber(Object obj, int i, MutableBoolean mutableBoolean) {
        Method[] declaredMethods;
        if (Thread.currentThread().getId() != this.mHandler.getLooper().getThread().getId()) {
            throw new RuntimeException("Can not register() a subscriber from a non-main thread.");
        }
        if (findRegisteredSubscriber(obj, false)) {
            return;
        }
        Subscriber subscriber = new Subscriber(obj, SystemClock.uptimeMillis());
        Class<?> cls = obj.getClass();
        ArrayList<EventHandlerMethod> arrayList = this.mSubscriberTypeMap.get(cls);
        if (arrayList != null) {
            for (EventHandlerMethod eventHandlerMethod : arrayList) {
                ArrayList<EventHandler> arrayList2 = this.mEventTypeMap.get(eventHandlerMethod.eventType);
                arrayList2.add(new EventHandler(subscriber, eventHandlerMethod, i));
                sortEventHandlersByPriority(arrayList2);
            }
            this.mSubscribers.add(subscriber);
            return;
        }
        ArrayList arrayList3 = new ArrayList();
        this.mSubscriberTypeMap.put(cls, arrayList3);
        this.mSubscribers.add(subscriber);
        MutableBoolean mutableBoolean2 = new MutableBoolean(false);
        for (Method method : cls.getDeclaredMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            mutableBoolean2.value = false;
            if (isValidEventBusHandlerMethod(method, parameterTypes, mutableBoolean2)) {
                Class<?> cls2 = parameterTypes[0];
                ArrayList<EventHandler> arrayList4 = this.mEventTypeMap.get(cls2);
                ArrayList<EventHandler> arrayList5 = arrayList4;
                if (arrayList4 == null) {
                    arrayList5 = new ArrayList<>();
                    this.mEventTypeMap.put(cls2, arrayList5);
                }
                if (mutableBoolean2.value) {
                    try {
                        cls2.getConstructor(Bundle.class);
                        this.mInterprocessEventNameMap.put(cls2.getName(), cls2);
                        if (mutableBoolean != null) {
                            mutableBoolean.value = true;
                        }
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("Expected InterprocessEvent to have a Bundle constructor");
                    }
                }
                EventHandlerMethod eventHandlerMethod2 = new EventHandlerMethod(method, cls2);
                arrayList5.add(new EventHandler(subscriber, eventHandlerMethod2, i));
                arrayList3.add(eventHandlerMethod2);
                sortEventHandlersByPriority(arrayList5);
            }
        }
    }

    private void sortEventHandlersByPriority(List<EventHandler> list) {
        Collections.sort(list, EVENT_HANDLER_COMPARATOR);
    }

    public void dump(String str, PrintWriter printWriter) {
        printWriter.println(dumpInternal(str));
    }

    public String dumpInternal(String str) {
        String str2 = str + "  ";
        String str3 = str2 + "  ";
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append("Registered class types:");
        sb.append("\n");
        ArrayList arrayList = new ArrayList(this.mSubscriberTypeMap.keySet());
        Collections.sort(arrayList, new Comparator<Class<?>>(this) { // from class: com.android.systemui.recents.events.EventBus.2
            final EventBus this$0;

            {
                this.this$0 = this;
            }

            @Override // java.util.Comparator
            public int compare(Class<?> cls, Class<?> cls2) {
                return cls.getSimpleName().compareTo(cls2.getSimpleName());
            }
        });
        for (int i = 0; i < arrayList.size(); i++) {
            sb.append(str2);
            sb.append(((Class) arrayList.get(i)).getSimpleName());
            sb.append("\n");
        }
        sb.append(str);
        sb.append("Event map:");
        sb.append("\n");
        ArrayList arrayList2 = new ArrayList(this.mEventTypeMap.keySet());
        Collections.sort(arrayList2, new Comparator<Class<?>>(this) { // from class: com.android.systemui.recents.events.EventBus.3
            final EventBus this$0;

            {
                this.this$0 = this;
            }

            @Override // java.util.Comparator
            public int compare(Class<?> cls, Class<?> cls2) {
                return cls.getSimpleName().compareTo(cls2.getSimpleName());
            }
        });
        for (int i2 = 0; i2 < arrayList2.size(); i2++) {
            Class cls = (Class) arrayList2.get(i2);
            sb.append(str2);
            sb.append(cls.getSimpleName());
            sb.append(" -> ");
            sb.append("\n");
            for (EventHandler eventHandler : this.mEventTypeMap.get(cls)) {
                Object reference = eventHandler.subscriber.getReference();
                if (reference != null) {
                    String hexString = Integer.toHexString(System.identityHashCode(reference));
                    sb.append(str3);
                    sb.append(reference.getClass().getSimpleName());
                    sb.append(" [0x").append(hexString).append(", #").append(eventHandler.priority).append("]");
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        try {
            send(this.mInterprocessEventNameMap.get(intent.getAction()).getConstructor(Bundle.class).newInstance(intent.getBundleExtra("interprocess_event_bundle")));
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            Log.e("EventBus", "Failed to create InterprocessEvent", e.getCause());
        }
    }

    public void post(Event event) {
        event.requiresPost = true;
        event.cancelled = false;
        queueEvent(event);
    }

    public void register(Object obj) {
        registerSubscriber(obj, 1, null);
    }

    public void register(Object obj, int i) {
        registerSubscriber(obj, i, null);
    }

    public void send(Event event) {
        if (Thread.currentThread().getId() != this.mHandler.getLooper().getThread().getId()) {
            throw new RuntimeException("Can not send() a message from a non-main thread.");
        }
        event.requiresPost = false;
        event.cancelled = false;
        queueEvent(event);
    }

    public void sendOntoMainThread(Event event) {
        if (Thread.currentThread().getId() != this.mHandler.getLooper().getThread().getId()) {
            post(event);
        } else {
            send(event);
        }
    }

    public void unregister(Object obj) {
        if (Thread.currentThread().getId() != this.mHandler.getLooper().getThread().getId()) {
            throw new RuntimeException("Can not unregister() a subscriber from a non-main thread.");
        }
        if (findRegisteredSubscriber(obj, true)) {
            ArrayList<EventHandlerMethod> arrayList = this.mSubscriberTypeMap.get(obj.getClass());
            if (arrayList != null) {
                for (EventHandlerMethod eventHandlerMethod : arrayList) {
                    ArrayList<EventHandler> arrayList2 = this.mEventTypeMap.get(eventHandlerMethod.eventType);
                    for (int size = arrayList2.size() - 1; size >= 0; size--) {
                        if (arrayList2.get(size).subscriber.getReference() == obj) {
                            arrayList2.remove(size);
                        }
                    }
                }
            }
        }
    }
}
