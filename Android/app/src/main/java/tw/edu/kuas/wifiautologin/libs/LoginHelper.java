package tw.edu.kuas.wifiautologin.libs;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tw.edu.kuas.wifiautologin.MainActivity;
import tw.edu.kuas.wifiautologin.R;
import tw.edu.kuas.wifiautologin.callbacks.GeneralCallback;
import tw.edu.kuas.wifiautologin.models.UserModel;

public class LoginHelper {

	private static OkHttpClient client =
			new OkHttpClient().newBuilder().followRedirects(false).followSslRedirects(false)
					.connectTimeout(7, TimeUnit.SECONDS).writeTimeout(7, TimeUnit.SECONDS)
					.readTimeout(7, TimeUnit.SECONDS).build();

	private static final String LOGIN_URL = "http://%s/cgi-bin/ace_web_auth.cgi";
	private static final String LOGOUT_URL = "http://%s/cgi-bin/ace_web_auth.cgi?logout";
	private static final String TEST_LOGOUT_URL = "http://%s";

	public static void login(final Context context, final UserModel model,
	                         final GeneralCallback callback) {
		if (!checkSSID(context, callback)) {
			return;
		}

		final RequestBody requestBody =
				new FormBody.Builder().add("username", model.username).add("userpwd", model.userpwd)
						.add("login", "").add("orig_referer", "").build();

		Request request = new Request.Builder().url("http://www.example.com").head().build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				if (callback != null) {
					callback.onFail(context.getString(R.string.login_timeout));
				}
			}

			@Override
			public void onResponse(Call call, Response response) {
				call.cancel();
				if (response.code() == 200) {
					if (context instanceof MainActivity) {
						if (callback != null) {
							callback.onAlready();
						}
					} else {
						NotificationHelper.createNotification(context,
								context.getString(R.string.already_logged_in), false, false,
								Constant.NOTIFICATION_ALREADY_ID);
					}
				} else if (response.code() == 302) {
					NotificationHelper.createNotification(context,
							context.getString(R.string.login_to_ssid,
									Utils.getCurrentSSID(context)), true, false,
							Constant.NOTIFICATION_ID);
					Uri uri = Uri.parse(response.header("location"));
					login(context, uri.getAuthority(), model.loginType, requestBody, callback);
				} else {
					if (callback != null) {
						callback.onFail(context.getString(R.string.login_timeout));
					}
				}
			}
		});
	}

	private static boolean checkSSID(Context context, GeneralCallback callback) {
		String ssid = Utils.getCurrentSSID(context);
		if (TextUtils.isEmpty(ssid) || !Utils.isExpectedSSID(ssid)) {
			if (callback != null) {
				if (TextUtils.isEmpty(ssid)) {
					callback.onFail(context.getString(R.string.no_wifi_connection));
				} else {
					callback.onFail(
							String.format(context.getString(R.string.ssid_no_support), ssid));
				}
			}
			return false;
		}
		return true;
	}

	private static void login(final Context context, final String location,
	                          final UserModel.LoginType loginType, final RequestBody requestBody,
	                          final GeneralCallback callback) {

		String url = String.format(Locale.getDefault(), LOGIN_URL, location);

		Request request = new Request.Builder().url(url).post(requestBody)
				.addHeader("Accept-Encoding", "gzip, deflate").build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				NotificationHelper.cancelNotification(context, Constant.NOTIFICATION_ID);
				if (callback != null) {
					callback.onFail(context.getString(R.string.login_timeout));
				}
			}

			@Override
			public void onResponse(Call call, Response response) {
				try {
					String _response = response.body().string();
					if (_response.contains("login_online_detail.php")) {
						loginSuccess(context, location, loginType, callback);
					} else {
						NotificationHelper.cancelNotification(context, Constant.NOTIFICATION_ID);
						if (_response.contains("reason")) {
							int _reason = Integer.parseInt(_response
									.substring(_response.indexOf("reason=") + 7,
											_response.indexOf("&", _response.indexOf("reason="))));
							if (_reason == 27 || _reason == 35) {
								if (callback != null) {
									callback.onFail(context.getString(R.string.user_pwd_error));
								} else {
									NotificationHelper.createNotification(context,
											context.getString(R.string.user_pwd_error), false,
											false, Constant.NOTIFICATION_FAIL_ID);
								}
							} else {
								if (callback != null) {
									callback.onFail(Reason.dumpReason(_reason));
								} else {
									NotificationHelper
											.createNotification(context, Reason.dumpReason(_reason),
													false, false, Constant.NOTIFICATION_FAIL_ID);
								}
							}
						} else if (_response.contains("404 - Not Found")) {
							if (callback != null) {
								callback.onFail(context.getString(R.string.login_timeout));
							} else {
								NotificationHelper.createNotification(context,
										context.getString(R.string.login_timeout), false, false,
										Constant.NOTIFICATION_FAIL_ID);
							}
						} else {
							if (callback != null) {
								callback.onFail(context.getString(R.string.login_timeout));
							} else {
								NotificationHelper.createNotification(context,
										context.getString(R.string.login_timeout), false, false,
										Constant.NOTIFICATION_FAIL_ID);
							}
						}
					}
				} catch (Exception e) {
					NotificationHelper.cancelNotification(context, Constant.NOTIFICATION_ID);
					if (callback != null) {
						callback.onFail(context.getString(R.string.login_timeout));
					}
				}
			}
		});
	}

	public static void logout(final Context context, @NonNull final GeneralCallback callback) {
		if (!checkSSID(context, callback)) {
			return;
		}

		String url =
				String.format(Locale.getDefault(), TEST_LOGOUT_URL, Constant.JIANGONG_WIFI_SERVER);

		Request request = new Request.Builder().url(url).head().build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				callback.onFail(context.getString(R.string.failed_to_logout));
			}

			@Override
			public void onResponse(Call call, Response response) {
				call.cancel();
				if (response.code() == 302) {
					checkLogoutLocation(context, response.header("location"), callback);
				} else {
					callback.onFail(context.getString(R.string.failed_to_logout));
				}
			}
		});
	}

	private static void checkLogoutLocation(final Context context, String location,
	                                        @NonNull final GeneralCallback callback) {
		if (location.contains("login_online")) {
			logout(context, Constant.JIANGONG_WIFI_SERVER, callback);
		} else if (location.contains("login.php") || location.contains("auth_entry.php")) {
			callback.onAlready();
		} else {
			logout(context, Constant.YANCHAO_WIFI_SERVER, callback);
		}
	}

	private static void logout(final Context context, String location,
	                           @NonNull final GeneralCallback callback) {

		String url = String.format(Locale.getDefault(), LOGOUT_URL, location);

		final Request request = new Request.Builder().url(url).get().build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				callback.onFail(context.getString(R.string.failed_to_logout));
			}

			@Override
			public void onResponse(Call call, Response response) {
				if (response.code() == 200) {
					callback.onSuccess(context.getString(R.string.logout_successful));
					NotificationHelper.cancelNotification(context, Constant.NOTIFICATION_ID);
				} else {
					callback.onFail(context.getString(R.string.failed_to_logout));
				}
			}
		});
	}

	private static void loginSuccess(Context context, String location,
	                                 UserModel.LoginType loginType, GeneralCallback callback) {
		String campus = location.equals(Constant.JIANGONG_WIFI_SERVER) ?
				context.getString(R.string.jiangong) : context.getString(R.string.yanchao);
		String result;

		switch (loginType) {
			case STUDENT:
				result = context.getString(R.string.login_successfully, campus);
				break;
			case CYBER:
				result = context.getString(R.string.login_cyber_successfully, campus);
				break;
			case DORM:
				result = context.getString(R.string.login_dorm_successfully, campus);
				break;
			case TEACHER:
				result = context.getString(R.string.login_teacher_successfully, campus);
				break;
			default:
				result = context.getString(R.string.login_guest_successfully, campus);
		}

		NotificationHelper
				.createNotification(context, result, false, false, Constant.NOTIFICATION_ID);
		NotificationHelper.cancelNotification(context, Constant.NOTIFICATION_FAIL_ID);
		NotificationHelper.cancelNotification(context, Constant.NOTIFICATION_ALREADY_ID);

		if (callback != null) {
			callback.onSuccess(result);
		}
	}
}
