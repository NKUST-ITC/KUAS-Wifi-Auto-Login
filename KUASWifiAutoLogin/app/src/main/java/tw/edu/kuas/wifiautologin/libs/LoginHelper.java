package tw.edu.kuas.wifiautologin.libs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import tw.edu.kuas.wifiautologin.MainActivity;
import tw.edu.kuas.wifiautologin.R;
import tw.edu.kuas.wifiautologin.callbacks.Constant;
import tw.edu.kuas.wifiautologin.callbacks.GeneralCallback;

public class LoginHelper {
	private static AsyncHttpClient mClient = init();
	private static AsyncHttpClient mTestClient = init();

	private static NotificationManager mNotificationManager;
	private static NotificationCompat.Builder mBuilder;

	private static AsyncHttpClient init() {
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("Connection", "Keep-Alive");
		client.setTimeout(30000);
		client.setEnableRedirects(false);
		return client;
	}

	public static void login(final Context context, String idType, String user, String password, final String loginType, final GeneralCallback callback) {
		String currentSsid = Utils.getCurrentSsid(context);
		if (currentSsid == null || !Utils.isExpectedSsid(currentSsid)) {
			if (currentSsid == null) {
				currentSsid = context.getString(R.string.no_wifi_connection);
			}
			if (callback != null) {
				callback.onFail(
						String.format(context.getString(R.string.ssid_no_support), currentSsid));
			}
			return;
		}

		final RequestParams params = new RequestParams();
		params.put("idtype", idType);
		params.put("username",  user);
		params.put("userpwd", password);
		params.put("login", "登入");
		params.put("orig_referer", "http://www.kuas.edu.tw/bin/home.php");

		mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setContentTitle(context.getString(R.string.app_name)).setContentText(
				String.format(context.getString(R.string.login_to_ssid), currentSsid))
				.setSmallIcon(R.drawable.ic_stat_login).setProgress(0, 0, true).setOngoing(true);
		mNotificationManager.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());

		mTestClient.get(context, "http://www.example.com", new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
				String resultString = context.getString(R.string.login_ready);
				if (statusCode == 200) {
					if (callback != null) {
						callback.onSuccess();
					}
					mBuilder.setContentTitle(context.getString(R.string.app_name))
							.setContentText(resultString).setSmallIcon(R.drawable.ic_stat_login)
							.setContentIntent(getDefaultPendingIntent(context))
							.setAutoCancel(true)
							.setVibrate(new long[]{300, 200, 300, 200})
							.setProgress(0, 0, false);
					mNotificationManager
							.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());
				}
				else
					loginJiangong(context, params, loginType, callback);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
				loginJiangong(context, params, loginType, callback);
			}
		});
	}

	private static void loginJiangong(final Context context, final RequestParams params, final String loginType, final GeneralCallback callback)
	{
		mClient.post(context, "http://172.16.61.253/cgi-bin/ace_web_auth.cgi", params,
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] response) {

						mTestClient.get(context, "http://www.example.com", new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
								String resultString;
								if (statusCode == 200) {
									if (loginType.equals("Student"))
										resultString = context.getString(R.string.login_successfully);
									else if (loginType.equals("Cyber"))
										resultString = context.getString(R.string.login_cyber_successfully);
									else
										resultString = context.getString(R.string.login_guest_successfully);
									if (callback != null) {
										callback.onSuccess();
									}

									mBuilder.setContentTitle(context.getString(R.string.app_name))
											.setContentText(resultString).setSmallIcon(R.drawable.ic_stat_login)
											.setContentIntent(getDefaultPendingIntent(context))
											.setAutoCancel(true)
											.setVibrate(new long[]{300, 200, 300, 200})
											.setLights(Color.GREEN, 800, 800)
											.setDefaults(Notification.DEFAULT_SOUND)
											.setProgress(0, 0, false);
									mNotificationManager
											.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());
								} else {
									loginYanchao(context, params, loginType, callback);
								}
							}

							@Override
							public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable e) {
								e.printStackTrace();
								loginYanchao(context, params, loginType, callback);
							}
						});
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
						e.printStackTrace();
						loginYanchao(context, params, loginType, callback);
					}
				});
	}

	private static void loginYanchao(final Context context, RequestParams params, final String loginType, final GeneralCallback callback)
	{
		mClient.post(context, "http://172.16.109.253/cgi-bin/ace_web_auth.cgi", params,
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers, final byte[] response) {

						mTestClient.get(context, "http://www.example.com", new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
								String resultString;
								if (statusCode == 200) {
									if (loginType.equals("Student"))
										resultString = context.getString(R.string.login_successfully);
									else if (loginType.equals("Cyber"))
										resultString = context.getString(R.string.login_cyber_successfully);
									else
										resultString = context.getString(R.string.login_guest_successfully);
									if (callback != null) {
										callback.onSuccess();
									}
								} else {
									resultString = "Status: " + statusCode;
									if (callback != null) {
										callback.onFail(resultString);
									}
								}

								mBuilder.setContentTitle(context.getString(R.string.app_name))
										.setContentText(resultString).setSmallIcon(R.drawable.ic_stat_login)
										.setContentIntent(getDefaultPendingIntent(context))
										.setAutoCancel(true)
										.setVibrate(new long[]{300, 200, 300, 200})
										.setLights(Color.GREEN, 800, 800)
										.setDefaults(Notification.DEFAULT_SOUND)
										.setProgress(0, 0, false);
								mNotificationManager
										.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());
							}

							@Override
							public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable e) {
								e.printStackTrace();
								String resultString, resultDetailString = "Connection problem.";

								if (headers != null) {
									resultDetailString = "";
									for (Header header : headers) {
										resultDetailString += header.toString() + "\n";
									}
								}

								resultString = context.getString(R.string.failed_to_login);
								mBuilder.setContentTitle(context.getString(R.string.app_name))
										.setContentText(resultString).setSmallIcon(R.drawable.ic_stat_login)
										.setContentIntent(getDefaultPendingIntent(context))
										.setAutoCancel(true)
										.setVibrate(new long[]{300, 200, 300, 200})
										.setLights(Color.RED, 800, 800)
										.setProgress(0, 0, false);

								if (callback != null) {
									callback.onFail(resultString + "\n" + resultDetailString);
								}
								// Show error details in the expanded notification
								mBuilder.setStyle(new NotificationCompat.BigTextStyle()
										.bigText(resultString + "\n" + resultDetailString));

								mNotificationManager
										.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());
							}
						});
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] errorResponse,
										  Throwable e) {
						e.printStackTrace();

						String resultString, resultDetailString = "Connection problem.";

						if (headers != null) {
							resultDetailString = "";
							for (Header header : headers) {
								resultDetailString += header.toString() + "\n";
							}
						}

						resultString = context.getString(R.string.failed_to_login);
						mBuilder.setContentTitle(context.getString(R.string.app_name))
								.setContentText(resultString).setSmallIcon(R.drawable.ic_stat_login)
								.setContentIntent(getDefaultPendingIntent(context))
								.setAutoCancel(true)
								.setVibrate(new long[]{300, 200, 300, 200})
								.setLights(Color.RED, 800, 800)
								.setProgress(0, 0, false);

						if (callback != null) {
							callback.onFail(resultString + "\n" + resultDetailString);
						}
						// Show error details in the expanded notification
						mBuilder.setStyle(new NotificationCompat.BigTextStyle()
								.bigText(resultString + "\n" + resultDetailString));

						mNotificationManager
								.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());
					}
				});
	}

	private static PendingIntent getDefaultPendingIntent(Context context) {
		Intent notificationIntent = new Intent(context, MainActivity.class);
		return PendingIntent.getActivity(context, 0, notificationIntent, 0);
	}
}
