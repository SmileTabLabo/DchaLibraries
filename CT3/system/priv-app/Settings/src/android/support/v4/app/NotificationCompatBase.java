package android.support.v4.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
/* loaded from: classes.dex */
public class NotificationCompatBase {

    /* loaded from: classes.dex */
    public static abstract class Action {

        /* loaded from: classes.dex */
        public interface Factory {
        }

        public abstract PendingIntent getActionIntent();

        public abstract boolean getAllowGeneratedReplies();

        public abstract Bundle getExtras();

        public abstract int getIcon();

        public abstract RemoteInputCompatBase$RemoteInput[] getRemoteInputs();

        public abstract CharSequence getTitle();
    }

    public static Notification add(Notification notification, Context context, CharSequence contentTitle, CharSequence contentText, PendingIntent contentIntent) {
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        return notification;
    }
}
