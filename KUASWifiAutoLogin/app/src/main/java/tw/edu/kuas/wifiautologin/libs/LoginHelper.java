package tw.edu.kuas.wifiautologin.libs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import tw.edu.kuas.wifiautologin.R;
import tw.edu.kuas.wifiautologin.callbacks.Constant;
import tw.edu.kuas.wifiautologin.callbacks.GeneralCallback;

public class LoginHelper {
	private static AsyncHttpClient mClient = init();
	private static AsyncHttpClient mTestClient = initTest();

	private static NotificationManager mNotificationManager;
	private static NotificationCompat.Builder mBuilder;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

	private static AsyncHttpClient init() {
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("Connection", "Keep-Alive");
		client.setTimeout(5000);
		client.setEnableRedirects(false);
		return client;
	}

	private static AsyncHttpClient initTest() {
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("Connection", "Keep-Alive");
		client.setTimeout(3000);
		client.setEnableRedirects(false);
		return client;
	}

	public static void login(final Context context, String idType, String user, String password, final String loginType, final GeneralCallback callback) {
        // init GA
        analytics = GoogleAnalytics.getInstance(context);
        analytics.setLocalDispatchPeriod(1);

        tracker = analytics.newTracker("UA-46334408-1");
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);

        tracker.setScreenName("LoginHelper");

        String currentSsid = Utils.getCurrentSsid(context);
		if (currentSsid == null || !Utils.isExpectedSsid(currentSsid)) {
			if (currentSsid == null) {
				currentSsid = context.getString(R.string.no_wifi_connection);
				if (callback != null)
					callback.onFail(currentSsid);
				return;
			}
			if (callback != null)
				callback.onFail(String.format(context.getString(R.string.ssid_no_support), currentSsid));
			return;
		}

        Log.d(Constant.TAG, getIPAddress(context));

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

		mTestClient.get(context, "http://www.example.com", new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
				String resultString = context.getString(R.string.login_ready);
				if (statusCode == 200) {
                    Log.d(Constant.TAG, "Already Login.");

					if (callback != null)
						callback.onSuccess(resultString);
					Toast.makeText(context, resultString, Toast.LENGTH_LONG).show();
				}
				else
                {
                    mNotificationManager.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());

                    String _IP = getIPAddress(context);
                    if (_IP.split("\\.")[0].equals("172"))
                        if (_IP.split("\\.")[1].equals("17"))
                            loginJiangong(context, params, loginType, callback);
                        else
                            loginYanchaoDorm(context, params, loginType, callback);
                    else
                        loginYanchao(context, params, loginType, callback);
                }
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                mNotificationManager.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());

                String _IP = getIPAddress(context);
                if (_IP.split("\\.")[0].equals("172"))
                    if (_IP.split("\\.")[1].equals("17"))
                        loginJiangong(context, params, loginType, callback);
                    else
                        loginYanchaoDorm(context, params, loginType, callback);
                else
                    loginYanchao(context, params, loginType, callback);
			}
		});
	}

	private static void loginJiangong(final Context context, final RequestParams params, final String loginType, final GeneralCallback callback)
	{
        Log.d(Constant.TAG, "loginJiangong");

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
										callback.onSuccess(resultString);
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

                                    tracker.send(new HitBuilders.EventBuilder()
                                            .setCategory("UX")
                                            .setAction("onSuccess")
                                            .setLabel("建工/" + loginType)
                                            .build());
								} else {
									loginYanchao(context, params, loginType, callback);

                                    tracker.send(new HitBuilders.EventBuilder()
                                            .setCategory("UX")
                                            .setAction("onFailure")
                                            .setLabel("建工/" + loginType)
                                            .build());
								}
							}

							@Override
							public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable e) {
								e.printStackTrace();
								loginYanchao(context, params, loginType, callback);

                                tracker.send(new HitBuilders.EventBuilder()
                                        .setCategory("UX")
                                        .setAction("onFailure")
                                        .setLabel("建工/" + loginType)
                                        .build());
							}
						});
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        e.printStackTrace();
                        loginYanchao(context, params, loginType, callback);

                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory("UX")
                                .setAction("onFailure")
                                .setLabel("建工/" + loginType)
                                .build());
					}
				});
	}

	private static void loginYanchaoDorm(final Context context, RequestParams params, final String loginType, final GeneralCallback callback)
	{
        Log.d(Constant.TAG, "loginYanchaoDorm");

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
										callback.onSuccess(resultString);
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

                                tracker.send(new HitBuilders.EventBuilder()
                                        .setCategory("UX")
                                        .setAction("onSuccess")
                                        .setLabel("燕巢宿舍/" + loginType)
                                        .build());
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

                                tracker.send(new HitBuilders.EventBuilder()
                                        .setCategory("UX")
                                        .setAction("onFailure")
                                        .setLabel("燕巢宿舍/" + loginType)
                                        .build());
							}
						});
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] errorResponse,
										  Throwable e) {
						e.printStackTrace();

						String resultString, resultDetailString = "";

						/*if (headers != null) {
							resultDetailString = "";
							for (Header header : headers) {
								resultDetailString += header.toString() + "\n";
							}
						}*/

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

                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory("UX")
                                .setAction("onFailure")
                                .setLabel("燕巢宿舍/" + loginType)
                                .build());
					}
				});
	}

    private static void loginYanchao(final Context context, final RequestParams params, final String loginType, final GeneralCallback callback)
    {
        Log.d(Constant.TAG, "loginYanchao");

        mClient.post(context, "http://74.125.203.101/cgi-bin/ace_web_auth.cgi", params,
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
                                        callback.onSuccess(resultString);
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

                                tracker.send(new HitBuilders.EventBuilder()
                                        .setCategory("UX")
                                        .setAction("onSuccess")
                                        .setLabel("燕巢/" + loginType)
                                        .build());
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable e) {
                                e.printStackTrace();
                                loginYanchaoDorm(context, params, loginType, callback);

                                tracker.send(new HitBuilders.EventBuilder()
                                        .setCategory("UX")
                                        .setAction("onFailure")
                                        .setLabel("燕巢/" + loginType)
                                        .build());
                            }
                        });
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse,
                                          Throwable e) {
                        e.printStackTrace();
                        loginYanchaoDorm(context, params, loginType, callback);

                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory("UX")
                                .setAction("onFailure")
                                .setLabel("燕巢/" + loginType)
                                .build());
                    }
                });
    }

    public static String getIPAddress(Context context) {
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        long ip = wifiInf.getIpAddress();
        if( ip != 0 )
            return String.format( "%d.%d.%d.%d",
                    (ip & 0xff),
                    (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff),
                    (ip >> 24 & 0xff));
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
        }
        return "0.0.0.0";
    }

	private static PendingIntent getDefaultPendingIntent(Context context) {
		return PendingIntent.getActivity(context, 0, new Intent(), 0);
    }
}
