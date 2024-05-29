package jp.co.benesse.dcha.setupwizard;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: classes.dex */
public class StartWidget extends AppWidgetProvider {
    private static final String TAG = StartWidget.class.getSimpleName();

    @Override // android.appwidget.AppWidgetProvider
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Logger.d(TAG, "onUpdate 0001");
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), (int) R.layout.widget);
        Intent intent = new Intent(context, IntroductionSettingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_btn, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        Logger.d(TAG, "onUpdate 0002");
    }
}
