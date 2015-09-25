package tw.edu.kuas.wifiautologin.libs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.LinkedHashMap;

import tw.edu.kuas.wifiautologin.MainActivity;
import tw.edu.kuas.wifiautologin.R;
import tw.edu.kuas.wifiautologin.callbacks.GeneralCallback;

public class LoginHelper {
	private static AsyncHttpClient mClient = init();

	private static NotificationManager mNotificationManager;
	private static NotificationCompat.Builder mBuilder;

	public static GoogleAnalytics analytics;
	public static Tracker tracker;

	private static AsyncHttpClient init() {
		AsyncHttpClient client = new AsyncHttpClient();
		client.setUserAgent(Constant.USER_AGENT);
		client.setEnableRedirects(false);
		return client;
	}

	private static void initGA(Context context) {
		analytics = GoogleAnalytics.getInstance(context);
		analytics.setLocalDispatchPeriod(30);

		tracker = analytics.newTracker("UA-46334408-1");
		tracker.enableExceptionReporting(true);
		tracker.enableAdvertisingIdCollection(true);
		tracker.enableAutoActivityTracking(true);

		tracker.setScreenName("LoginHelper");
	}

	public static void login(final Context context, String user, String password,
	                         final String loginType, final GeneralCallback callback) {
		if (!checkSSID(context, callback)) {
			return;
		}

		initGA(context);

		String currentSsid = Utils.getCurrentSsid(context);

		final LinkedHashMap<String, String> paramsMap = new LinkedHashMap<>();
		paramsMap.put("username", user);
		paramsMap.put("userpwd", password);
		paramsMap.put("login", "");
		paramsMap.put("orig_referer", "");

		mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setContentTitle(context.getString(R.string.kuas_wifi_auto_login)).setContentText(
				String.format(context.getString(R.string.login_to_ssid), currentSsid))
				.setSmallIcon(R.drawable.ic_stat_login).setProgress(0, 0, true).setOngoing(false);

		mClient.setTimeout(Constant.TIMEOUT_LOGIN);
		mClient.get(context, "http://www.example.com", new AsyncHttpResponseHandler() {
			String _IP = getIPAddress(context);
			String loginServer = "";

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
				String resultString = context.getString(R.string.login_ready);

				if (statusCode == HttpStatus.SC_OK) {
					Log.d(Constant.TAG, "Already Login.");

					if (callback != null) {
						loginSuccess(context, loginType, callback,
								(_IP.split("\\.")[0].equals("172") &&
										_IP.split("\\.")[1].equals("17")) ?
										Constant.JIANGONG_WIFI_SERVER :
										Constant.YANCHAO_WIFI_SERVER, resultString, false);
						Toast.makeText(context, resultString, Toast.LENGTH_SHORT).show();

						tracker.send(new HitBuilders.EventBuilder().setCategory("onSuccess")
								.setAction("alreadyLogin").setLabel(_IP + "/" + loginType).build());

						callback.onSuccess(resultString);
					}

				} else {
					mNotificationManager.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());

					if (headers != null) {
						for (Header header : headers) {
							if (header.getName().toLowerCase().equals("location")) {
								Uri uri = Uri.parse(header.getValue());
								loginServer = uri.getAuthority();
								break;
							}
						}
					}

					if (!loginServer.equals("")) {
						login(context, paramsMap, loginType, callback, false, loginServer);
					} else if (_IP.split("\\.")[0].equals("172") &&
							_IP.split("\\.")[1].equals("17")) {
						login(context, paramsMap, loginType, callback, true,
								Constant.JIANGONG_WIFI_SERVER);
					} else {
						login(context, paramsMap, loginType, callback, true,
								Constant.YANCHAO_WIFI_SERVER);
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] bytes,
			                      Throwable throwable) {
				mNotificationManager.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());

				if (headers != null) {
					for (Header header : headers) {
						if (header.getName().toLowerCase().equals("location")) {
							Uri uri = Uri.parse(header.getValue());
							loginServer = uri.getAuthority();
							break;
						}
					}
				}

				if (!loginServer.equals("")) {
					login(context, paramsMap, loginType, callback, false, loginServer);
				} else if (_IP.split("\\.")[0].equals("172") && _IP.split("\\.")[1].equals("17")) {
					login(context, paramsMap, loginType, callback, true,
							Constant.JIANGONG_WIFI_SERVER);
				} else {
					login(context, paramsMap, loginType, callback, true,
							Constant.YANCHAO_WIFI_SERVER);
				}
			}
		});
	}

	public static void newlogin(final Context context, String user, String password,
	                            final String loginType, final GeneralCallback callback) {
		if (!checkSSID(context, callback)) {
			return;
		}

		initGA(context);

		String currentSsid = Utils.getCurrentSsid(context);

		final LinkedHashMap<String, String> paramsMap = new LinkedHashMap<>();
		paramsMap.put("username", user);
		paramsMap.put("userpwd", password);
		paramsMap.put("login", "");
		paramsMap.put("orig_referer", "");

		mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setContentTitle(context.getString(R.string.kuas_wifi_auto_login)).setContentText(
				String.format(context.getString(R.string.login_to_ssid), currentSsid))
				.setSmallIcon(R.drawable.ic_stat_login).setProgress(0, 0, true).setOngoing(false);

		mClient.setTimeout(Constant.TIMEOUT_LOGIN);
		mClient.get(String.format("http://%s/", Constant.JIANGONG_WIFI_SERVER),
				new AsyncHttpResponseHandler() {
					String location = "";

					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] response) {
						if (headers != null) {
							for (Header header : headers) {
								if (header.getName().toLowerCase().equals("location")) {
									location = header.getValue();
									break;
								}
							}
							checkLoginLocation(context, paramsMap, loginType, callback, location);
						} else {
							checkLoginLocation(context, paramsMap, loginType, callback, "");
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] errorResponse,
					                      Throwable e) {
						if (statusCode == HttpStatus.SC_NOT_FOUND) {
							callback.onFail(context.getText(R.string.login_timeout).toString());

							tracker.send(new HitBuilders.EventBuilder().setCategory("onFailure")
									.setAction("Logout").setLabel("Time Out").build());
						} else {
							if (headers != null) {
								for (Header header : headers) {
									if (header.getName().toLowerCase().equals("location")) {
										location = header.getValue();
										break;
									}
								}
								checkLoginLocation(context, paramsMap, loginType, callback,
										location);
							} else {
								checkLoginLocation(context, paramsMap, loginType, callback, "");
							}
						}
					}
				});
	}

	private static void checkLoginLocation(final Context context,
	                                       final LinkedHashMap<String, String> paramsMap,
	                                       final String loginType, final GeneralCallback callback,
	                                       String location) {
		if (!location.equals("")) {
			if (location.contains("auth_entry")) {
				mNotificationManager.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());
				Uri uri = Uri.parse(location);
				login(context, paramsMap, loginType, callback, false, uri.getAuthority());
			} else {
				String _IP = getIPAddress(context);
				String resultString = context.getString(R.string.login_ready);

				if (callback != null) {
					loginSuccess(context, loginType, callback,
							location.contains("login_online") ? Constant.JIANGONG_WIFI_SERVER :
									Constant.YANCHAO_WIFI_SERVER, resultString, false);
					Toast.makeText(context, resultString, Toast.LENGTH_SHORT).show();

					tracker.send(new HitBuilders.EventBuilder().setCategory("onSuccess")
							.setAction("alreadyLogin").setLabel(_IP + "/" + loginType).build());

					callback.onSuccess(resultString);
				}
			}
		} else {
			retryLogin(context, paramsMap, loginType, callback, false, "",
					context.getString(R.string.login_timeout), "Time Out");
		}
	}

	private static void login(final Context context, final LinkedHashMap<String, String> paramsMap,
	                          final String loginType, final GeneralCallback callback,
	                          final boolean retry, final String loginServer) {
		final Handler refresh = new Handler(Looper.getMainLooper());
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final Connection.Response response = Jsoup.connect(
							String.format("http://%s/cgi-bin/ace_web_auth.cgi", loginServer))
							.data(paramsMap).userAgent(Constant.USER_AGENT)
							.timeout(Constant.TIMEOUT_LOGIN).followRedirects(false)
							.ignoreContentType(true).ignoreHttpErrors(true)
							.method(Connection.Method.POST).execute();

					final String _response = response.body();

					if (_response.contains("reason=27&") || _response.contains("reason=35&")) {
						refresh.post(new Runnable() {
							public void run() {
								retryLogin(context, paramsMap, loginType, callback, false,
										loginServer, context.getString(R.string.user_pwd_error),
										_response);
							}
						});
					} else if (_response.contains("reason=")) {
						final String _reason = _response.substring(_response.indexOf("reason=") + 7,
								_response.indexOf("&", _response.indexOf("reason=")));
						refresh.post(new Runnable() {
							public void run() {
								retryLogin(context, paramsMap, loginType, callback, false,
										loginServer, Reason.dumpReason(Integer.parseInt(_reason)),
										_response);
							}
						});
					} else if (_response.contains("404 - Not Found")) {
						refresh.post(new Runnable() {
							public void run() {
								retryLogin(context, paramsMap, loginType, callback, retry,
										loginServer, context.getString(R.string.login_timeout),
										"Time Out");
							}
						});
					} else if (_response.contains("login_online_detail.php")) {
						refresh.post(new Runnable() {
							public void run() {
								loginSuccess(context, loginType, callback, loginServer, "", true);
							}
						});
					} else {
						refresh.post(new Runnable() {
							public void run() {
								mClient.get(context, "http://www.example.com/",
										new AsyncHttpResponseHandler() {
											@Override
											public void onSuccess(final int statusCode,
											                      Header[] headers, byte[] bytes) {
												if (statusCode == HttpStatus.SC_OK) {
													loginSuccess(context, loginType, callback,
															loginServer, "", true);
												} else {
													retryLogin(context, paramsMap, loginType,
															callback, retry, loginServer, "",
															retry ? "" : _response);
												}
											}

											@Override
											public void onFailure(final int statusCode,
											                      Header[] headers, byte[] bytes,
											                      Throwable e) {
												e.printStackTrace();

												retryLogin(context, paramsMap, loginType, callback,
														retry, loginServer, "",
														retry ? "" : _response);
											}
										});
							}
						});
					}
				} catch (IOException e) {
					e.printStackTrace();
					refresh.post(new Runnable() {
						public void run() {
							retryLogin(context, paramsMap, loginType, callback, retry, loginServer,
									context.getString(R.string.login_timeout), "Time Out");
						}
					});
				}
			}
		}).start();
	}

	public static void logout(final Context context, final GeneralCallback callback) {
		if (!checkSSID(context, callback)) {
			return;
		}
		initGA(context);

		mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		mClient.setTimeout(Constant.TIMEOUT_LOGOUT);
		mClient.get(String.format("http://%s/", Constant.JIANGONG_WIFI_SERVER),
				new AsyncHttpResponseHandler() {
					String location = "";

					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] response) {
						if (headers != null) {
							for (Header header : headers) {
								if (header.getName().toLowerCase().equals("location")) {
									location = header.getValue();
									break;
								}
							}
							checkLogoutLocation(context, callback, location);
						} else {
							checkLogoutLocation(context, callback, "");
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] errorResponse,
					                      Throwable e) {
						if (statusCode == HttpStatus.SC_NOT_FOUND) {
							callback.onFail(context.getText(R.string.login_timeout).toString());

							tracker.send(new HitBuilders.EventBuilder().setCategory("onFailure")
									.setAction("Logout").setLabel("Time Out").build());
						} else {
							if (headers != null) {
								for (Header header : headers) {
									if (header.getName().toLowerCase().equals("location")) {
										location = header.getValue();
										break;
									}
								}
								checkLogoutLocation(context, callback, location);
							} else {
								checkLogoutLocation(context, callback, "");
							}
						}
					}
				});
	}

	private static void checkLogoutLocation(final Context context, final GeneralCallback callback,
	                                        String location) {
		if (!location.equals("")) {
			if (location.contains("login_online")) {
				logout(context, callback, Constant.JIANGONG_WIFI_SERVER);
			} else if (location.contains("login.php")) {
				tracker.send(
						new HitBuilders.EventBuilder().setCategory("onFailure").setAction("Logout")
								.setLabel("Already logged out").build());
				callback.onFail(context.getText(R.string.already_logged_out).toString());
			} else {
				logout(context, callback, Constant.YANCHAO_WIFI_SERVER);
			}
		} else {
			callback.onFail(context.getText(R.string.failed_to_logout).toString());
			tracker.send(new HitBuilders.EventBuilder().setCategory("onFailure").setAction("Logout")
					.setLabel("Null headers").build());
		}
	}

	private static void logout(final Context context, final GeneralCallback callback,
	                           final String logoutServer) {
		mClient.get(String.format("http://%s/cgi-bin/ace_web_auth.cgi?logout", logoutServer),
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
						callback.onSuccess(context.getText(R.string.logout_successful).toString());
						mNotificationManager.cancel(Constant.NOTIFICATION_LOGIN_ID);

						tracker.send(new HitBuilders.EventBuilder().setCategory("onSuccess")
								.setAction("Logout").setLabel(
										logoutServer.equals(Constant.JIANGONG_WIFI_SERVER) ? "建工" :
												"燕巢").build());
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
					                      Throwable error) {
						callback.onFail(context.getText(R.string.failed_to_logout).toString());

						tracker.send(new HitBuilders.EventBuilder().setCategory("onFailure")
								.setAction("Logout").setLabel("Time Out").build());
					}
				});
	}

	private static void retryLogin(Context context, final LinkedHashMap<String, String> paramsMap,
	                               String loginType, GeneralCallback callback, boolean retry,
	                               String loginServer, String resultString, String response) {
		if (retry) {
			tracker.send(new HitBuilders.EventBuilder().setCategory("retryLogin").setAction("onTry")
					.setLabel((loginServer.equals(Constant.JIANGONG_WIFI_SERVER) ? "建工" : "燕巢") +
							"/" + loginType).build());

			if (loginServer.equals(Constant.JIANGONG_WIFI_SERVER)) {
				login(context, paramsMap, loginType, callback, false, Constant.YANCHAO_WIFI_SERVER);
			} else {
				login(context, paramsMap, loginType, callback, false,
						Constant.JIANGONG_WIFI_SERVER);
			}
			return;
		}

		if (resultString.equals("")) {
			resultString = context.getString(R.string.failed_to_login);
		}

		mBuilder.setContentTitle(context.getString(R.string.kuas_wifi_auto_login))
				.setContentText(resultString).setSmallIcon(R.drawable.ic_stat_login)
				.setContentIntent(getFailPendingIntent(context)).setAutoCancel(true)
				.setVibrate(new long[]{300, 200, 300, 200}).setLights(Color.RED, 800, 800)
				.setProgress(0, 0, false);

		if (callback != null) {
			callback.onFail(resultString);
		}

		int errorTimes = Memory.getInt(context, Constant.MEMORY_KEY_ERRORTIMES, 0);

		mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(resultString));

		if (!(callback == null || errorTimes >= 3)) {
			mNotificationManager.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());
		} else {
			errorTimes = 0;
			mNotificationManager.cancel(Constant.NOTIFICATION_LOGIN_ID);
		}

		errorTimes++;
		Memory.setInt(context, Constant.MEMORY_KEY_ERRORTIMES, errorTimes);

		tracker.send(new HitBuilders.EventBuilder().setCategory("onFailure").setAction(
				(loginServer.equals(Constant.JIANGONG_WIFI_SERVER) ? "建工" : "燕巢") + "/" + loginType)
				.setLabel(response).build());
	}

	private static void loginSuccess(Context context, String loginType, GeneralCallback callback,
	                                 String loginServer, String resultString, boolean notify) {
		if (resultString.equals("")) {
			switch (loginType) {
				case "Student":
					resultString = String.format(context.getString(R.string.login_successfully),
							(loginServer.equals(Constant.JIANGONG_WIFI_SERVER) ?
									context.getString(R.string.jiangong) :
									context.getString(R.string.yanchao)));
					break;
				case "Cyber":
					resultString =
							String.format(context.getString(R.string.login_cyber_successfully),
									(loginServer.equals(Constant.JIANGONG_WIFI_SERVER) ?
											context.getString(R.string.jiangong) :
											context.getString(R.string.yanchao)));
					break;
				case "Dorm":
					resultString =
							String.format(context.getString(R.string.login_dorm_successfully),
									(loginServer.equals(Constant.JIANGONG_WIFI_SERVER) ?
											context.getString(R.string.jiangong) :
											context.getString(R.string.yanchao)));
					break;
				case "Teacher":
					resultString =
							String.format(context.getString(R.string.login_teacher_successfully),
									(loginServer.equals(Constant.JIANGONG_WIFI_SERVER) ?
											context.getString(R.string.jiangong) :
											context.getString(R.string.yanchao)));
					break;
				default:
					resultString =
							String.format(context.getString(R.string.login_guest_successfully),
									(loginServer.equals(Constant.JIANGONG_WIFI_SERVER) ?
											context.getString(R.string.jiangong) :
											context.getString(R.string.yanchao)));
			}
		}

		if (callback != null) {
			if (resultString.equals(context.getString(R.string.login_ready))) {
				callback.onSuccess(resultString);
			} else {
				callback.onSuccess(resultString.split(",")[0] + ",\n" + resultString.split(",")[1]);
			}
		}

		mBuilder.setContentTitle(context.getString(R.string.kuas_wifi_auto_login))
				.setContentText(resultString).setSmallIcon(R.drawable.ic_stat_login)
				.setContentIntent(getDefaultPendingIntent(context)).setAutoCancel(true)
				.setProgress(0, 0, false);

		Memory.setInt(context, Constant.MEMORY_KEY_ERRORTIMES, 0);

		if (notify) {
			mBuilder.setVibrate(new long[]{300, 200, 300, 200}).setLights(Color.GREEN, 800, 800)
					.setDefaults(Notification.DEFAULT_SOUND);

			tracker.send(new HitBuilders.EventBuilder().setCategory("onSuccess")
					.setAction((loginServer.equals(Constant.JIANGONG_WIFI_SERVER) ? "建工" : "燕巢"))
					.setLabel(loginType).build());
		}

		if (resultString.equals(context.getString(R.string.login_ready))) {
			mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(resultString));
		} else {
			mBuilder.setStyle(new NotificationCompat.BigTextStyle()
					.bigText(resultString.split(",")[0] + ",\n" + resultString.split(",")[1]));
		}

		mNotificationManager.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());
	}

	public static boolean checkSSID(Context context, GeneralCallback callback) {
		String currentSsid = Utils.getCurrentSsid(context);
		if (currentSsid == null || !Utils.isExpectedSsid(currentSsid)) {
			if (currentSsid == null) {
				currentSsid = context.getString(R.string.no_wifi_connection);
			}
			if (callback != null) {
				callback.onFail(
						String.format(context.getString(R.string.ssid_no_support), currentSsid));
			}
			return false;
		}
		return true;
	}

	public static String getIPAddress(Context context) {
		WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInf = wifiMan.getConnectionInfo();
		long ip = wifiInf.getIpAddress();
		if (ip != 0) {
			return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff),
					(ip >> 24 & 0xff));
		}

		return "0.0.0.0";
	}

	private static PendingIntent getDefaultPendingIntent(Context context) {
		return PendingIntent.getActivity(context, 0, new Intent(), 0);
	}

	private static PendingIntent getFailPendingIntent(Context context) {
		Intent notificationIntent = new Intent(context, MainActivity.class);
		return PendingIntent.getActivity(context, 0, notificationIntent, 0);
	}
}