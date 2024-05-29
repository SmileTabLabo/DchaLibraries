package com.android.systemui.recents.events;

import com.android.systemui.recents.events.EventBus;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/systemui/recents/events/EventHandlerMethod.class */
public class EventHandlerMethod {
    Class<? extends EventBus.Event> eventType;
    private Method mMethod;

    /* JADX INFO: Access modifiers changed from: package-private */
    public EventHandlerMethod(Method method, Class<? extends EventBus.Event> cls) {
        this.mMethod = method;
        this.mMethod.setAccessible(true);
        this.eventType = cls;
    }

    public void invoke(Object obj, EventBus.Event event) throws InvocationTargetException, IllegalAccessException {
        this.mMethod.invoke(obj, event);
    }

    public String toString() {
        return this.mMethod.getName() + "(" + this.eventType.getSimpleName() + ")";
    }
}
