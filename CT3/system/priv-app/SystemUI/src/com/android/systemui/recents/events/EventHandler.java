package com.android.systemui.recents.events;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/systemui/recents/events/EventHandler.class */
public class EventHandler {
    EventHandlerMethod method;
    int priority;
    Subscriber subscriber;

    /* JADX INFO: Access modifiers changed from: package-private */
    public EventHandler(Subscriber subscriber, EventHandlerMethod eventHandlerMethod, int i) {
        this.subscriber = subscriber;
        this.method = eventHandlerMethod;
        this.priority = i;
    }

    public String toString() {
        return this.subscriber.toString(this.priority) + " " + this.method.toString();
    }
}
