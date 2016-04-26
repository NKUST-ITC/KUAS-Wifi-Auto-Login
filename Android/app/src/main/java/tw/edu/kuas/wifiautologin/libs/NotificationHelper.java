package tw.edu.kuas.wifiautologin.libs;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import tw.edu.kuas.wifiautologin.MainActivity;
import tw.edu.kuas.wifiautologin.R;

public class NotificationHelper {

	private static long[] vibrationPattern = {300, 200, 300, 200};

	/**
	 * Create a notification
	 *
	 * @param context  The application context.
	 * @param content  Notification content.
	 * @param progress Set notification progress visible
	 * @param retry    Set notification click to retry
	 * @param id       Notification id
	 */
	public static void createNotification(Context context, String content, boolean progress,
	                                      boolean retry, int id) {
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setContentTitle(context.getString(R.string.kuas_wifi_auto_login))
				.setStyle(new NotificationCompat.BigTextStyle().bigText(content))
				.setColor(ContextCompat.getColor(context, R.color.accent))
				.extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
				.setContentText(content).setAutoCancel(!progress)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setContentIntent(
						!retry ? getDefaultPendingIntent(context) : getFailPendingIntent(context))
				.setSmallIcon(R.drawable.ic_network_wifi_white_24dp).setProgress(0, 0, progress)
				.setOngoing(progress);

		if (id == Constant.NOTIFICATION_ID) {
			builder.setVibrate(vibrationPattern);
			builder.setLights(Color.GREEN, 800, 800);
		}

		notificationManager.notify(id, builder.build());
	}

	/**
	 * Cancel a notification
	 *
	 * @param context The application context.
	 * @param id      Notification id
	 */
	public static void cancelNotification(Context context, int id) {
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
		notificationManager.cancel(id);
	}

	private static PendingIntent getDefaultPendingIntent(Context context) {
		return PendingIntent.getActivity(context, 0, new Intent(), 0);
	}

	private static PendingIntent getFailPendingIntent(Context context) {
		Intent notificationIntent = new Intent(context, MainActivity.class);
		return PendingIntent.getActivity(context, 0, notificationIntent, 0);
	}

}
