package tw.edu.kuas.wifiautologin.widget;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.Calendar;

import tw.edu.kuas.wifiautologin.R;

public class WidgetProvider extends AppWidgetProvider {

	public static final String WIDGET_CLICK_ACTION = "WIDGET_CLICK_ACTION";
	public static final String WIDGET_DEFAULT_UPDATE = "WIDGET_DEFAULT_UPDATE";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];

			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

			//set click event intent
			Intent intent = new Intent(context, WidgetProvider.class);

			intent.setAction(WIDGET_CLICK_ACTION);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetId);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			remoteViews.setOnClickPendingIntent(R.id.widget_message, pendingIntent);
			remoteViews.setOnClickPendingIntent(R.id.widget_img, pendingIntent);
			//set default message
			changeMessage(remoteViews, context.getString(R.string.widget_default),
					context.getResources().getColor(R.color.white));
			//update widget
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		}

	}

	@Override
	public void onReceive(final Context context, Intent intent) {

		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

		final Intent serviceIntent = new Intent(context, WidgetService.class);

		if (intent.getAction() == null) {
			return;
		}

		if (intent.getAction().equals(WIDGET_CLICK_ACTION)) {
			updateAlarm(context);
			if (!isWidgetServiceRunning(WidgetService.class, context)) {
				changeMessage(remoteViews, context.getString(R.string.widget_connecting),
						context.getResources().getColor(R.color.md_teal_a700));
				widgetUpdate(context, appWidgetManager, remoteViews);
				context.startService(serviceIntent);
			}
			widgetUpdate(context, appWidgetManager, remoteViews);
		} else if (intent.getAction().equals(WIDGET_DEFAULT_UPDATE)) {
			//set default message
			changeMessage(remoteViews, context.getString(R.string.widget_default),
					context.getResources().getColor(R.color.white));
			//update widget
			widgetUpdate(context, appWidgetManager, remoteViews);

		}
		//update all widget

		super.onReceive(context, intent);
	}

	private void widgetUpdate(Context context, AppWidgetManager appWidgetManager,
	                          RemoteViews remoteViews) {
		ComponentName thisAppWidget =
				new ComponentName(context.getPackageName(), WidgetProvider.class.getName());
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		}
	}

	private void changeMessage(RemoteViews remoteViews, String message, int color) {
		remoteViews.setTextViewText(R.id.widget_message, message);
		remoteViews.setTextColor(R.id.widget_message, color);
	}

	private boolean isWidgetServiceRunning(Class<?> serviceClass, Context context) {
		ActivityManager manager =
				(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		if (manager == null) {
			return false;
		}
		for (ActivityManager.RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public void updateAlarm(Context context) {
		Calendar cal = Calendar.getInstance();
		// start up after 1 minute
		cal.add(Calendar.MINUTE, 1);

		Intent intent = new Intent(context, WidgetProvider.class);
		intent.setAction(WIDGET_DEFAULT_UPDATE);

		PendingIntent pi =
				PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (am == null) {
			return;
		}
		//cancel last register AlarmManager
		am.cancel(pi);
		//register AlarmManager
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
	}
}

