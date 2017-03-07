package tw.edu.kuas.wifiautologin.libs;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

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
import tw.edu.kuas.wifiautologin.base.SilentApplication;
import tw.edu.kuas.wifiautologin.callbacks.GeneralCallback;
import tw.edu.kuas.wifiautologin.models.UserModel;

public class LoginHelper {

	private static OkHttpClient mClient =
			new OkHttpClient().newBuilder().followRedirects(false).followSslRedirects(false)
					.connectTimeout(7, TimeUnit.SECONDS).writeTimeout(7, TimeUnit.SECONDS)
					.readTimeout(7, TimeUnit.SECONDS).build();

	private static Tracker mTracker;

	private static final String LOGIN_URL = "http://%s/cgi-bin/ace_web_auth.cgi";
	private static final String LOGOUT_URL = "http://%s/cgi-bin/ace_web_auth.cgi?logout";
	private static final String TEST_LOGOUT_URL = "http://%s";

	private static void initGA(Context context) {
		mTracker = ((SilentApplication) context.getApplicationContext()).getDefaultTracker();
		mTracker.setScreenName("Login Helper");
	}

	public static void login(final Context context, final UserModel model,
	                         @NonNull final GeneralCallback callback) {
		if (!checkSSID(context, callback)) {
			return;
		}

		Utils.requestNetwork(context);

		final RequestBody requestBody =
				new FormBody.Builder().add("username", model.username).add("userpwd", model.userpwd)
						.add("login", "").add("orig_referer", "").build();

		Request request = new Request.Builder().url("http://www.example.com").head().build();

		mClient.newCall(request).enqueue(new Callback() {

			@Override
			public void onFailure(Call call, IOException e) {
				call.cancel();
				loginFail(context, context.getString(R.string.login_timeout), callback);
			}

			@Override
			public void onResponse(Call call, Response response) {
				call.cancel();
				if (response.code() == 200) {
					callback.onAlready(context);
					if (!(context instanceof MainActivity)) {
						NotificationHelper.createNotification(context,
								context.getString(R.string.already_logged_in), false, false,
								Constant.NOTIFICATION_ALREADY_ID);
					}
				} else if (response.code() == 302) {
					Uri uri = Uri.parse(response.header("location"));
					String location = uri.getAuthority();
					showLoginNotification(context, model.loginType, location);
					login(context, location, model.loginType, requestBody, callback);
				} else {
					loginFail(context, context.getString(R.string.login_timeout), callback);
				}
			}
		});
	}

	private static boolean checkSSID(Context context, @NonNull GeneralCallback callback) {
		String ssid = Utils.getCurrentSSID(context);
		if (TextUtils.isEmpty(ssid) || !Utils.isExpectedSSID(ssid)) {
			if (TextUtils.isEmpty(ssid)) {
				callback.onFail(context, context.getString(R.string.no_wifi_connection));
			} else {
				callback.onFail(context,
						String.format(context.getString(R.string.ssid_no_support), ssid));
			}
			return false;
		}
		return true;
	}

	private static void login(final Context context, final String location,
	                          final UserModel.LoginType loginType, final RequestBody requestBody,
	                          @NonNull final GeneralCallback callback) {

		String url = String.format(Locale.getDefault(), LOGIN_URL, location);

		Request request = new Request.Builder().url(url).post(requestBody)
				.addHeader("Accept-Encoding", "gzip, deflate").build();

		mClient.newCall(request).enqueue(new Callback() {

			@Override
			public void onFailure(Call call, IOException e) {
				call.cancel();
				loginFail(context, context.getString(R.string.login_timeout), callback);
			}

			@Override
			public void onResponse(Call call, Response response) {
				call.cancel();
				try {
					String _response = response.body().string();
					if (_response.contains("login_online_detail.php")) {
						loginSuccess(context, location, loginType, callback);
					} else {
						if (_response.contains("reason")) {
							int _reason = Integer.parseInt(_response
									.substring(_response.indexOf("reason=") + 7,
											_response.indexOf("&", _response.indexOf("reason="))));
							if (_reason == 27 || _reason == 35) {
								loginFail(context, context.getString(R.string.user_pwd_error),
										callback);
							} else {
								loginFail(context, Reason.dumpReason(_reason), callback);
							}
						} else if (_response.contains("404 - Not Found")) {
							loginFail(context, context.getString(R.string.login_timeout), callback);
						} else {
							loginFail(context, context.getString(R.string.login_timeout), callback);
						}
					}
				} catch (Exception e) {
					loginFail(context, context.getString(R.string.login_timeout), callback);
				}
			}
		});
	}

	public static void logout(final Context context, final boolean recheck,
	                          @NonNull final GeneralCallback callback) {
		if (!checkSSID(context, callback)) {
			return;
		}

		Utils.requestNetwork(context);
		initGA(context);

		String url = String.format(Locale.getDefault(), TEST_LOGOUT_URL,
				!recheck ? Constant.JIANGONG_WIFI_SERVER : Constant.YANCHAO_WIFI_SERVER);

		Request request = new Request.Builder().url(url).head().build();

		mClient.newCall(request).enqueue(new Callback() {

			@Override
			public void onFailure(Call call, IOException e) {
				call.cancel();
				callback.onFail(context, context.getString(R.string.failed_to_logout));
			}

			@Override
			public void onResponse(Call call, Response response) {
				call.cancel();
				if (response.code() == 302) {
					checkLogoutLocation(context, response.header("location"), recheck, callback);
				} else {
					callback.onFail(context, context.getString(R.string.failed_to_logout));
				}
			}
		});
	}

	private static void checkLogoutLocation(Context context, String location, boolean recheck,
	                                        @NonNull GeneralCallback callback) {
		if (location.contains("login_online")) {
			Uri uri = Uri.parse(location);
			logout(context, uri.getAuthority(), callback);
		} else if (location.contains("login.php") || location.contains("auth_entry.php")) {
			if (recheck) {
				logout(context, false, callback);
			} else {
				callback.onAlready(context);
			}
		} else {
			mTracker.send(
					new HitBuilders.EventBuilder().setCategory("logout").setAction("unknown url")
							.setLabel(location).build());
			logout(context, !recheck ? Constant.JIANGONG_WIFI_SERVER : Constant.YANCHAO_WIFI_SERVER,
					callback);
		}
	}

	private static void logout(final Context context, String location,
	                           @NonNull final GeneralCallback callback) {

		String url = String.format(Locale.getDefault(), LOGOUT_URL, location);

		final Request request = new Request.Builder().url(url).get().build();

		mClient.newCall(request).enqueue(new Callback() {

			@Override
			public void onFailure(Call call, IOException e) {
				call.cancel();
				callback.onFail(context, context.getString(R.string.failed_to_logout));
			}

			@Override
			public void onResponse(Call call, Response response) {
				call.cancel();
				if (response.code() == 200) {
					callback.onSuccess(context, context.getString(R.string.logout_successful));
					NotificationHelper.cancelNotification(context, Constant.NOTIFICATION_LOGIN_ID);
					NotificationHelper
							.cancelNotification(context, Constant.NOTIFICATION_SUCCESS_ID);
					NotificationHelper.cancelNotification(context, Constant.NOTIFICATION_FAIL_ID);
					NotificationHelper
							.cancelNotification(context, Constant.NOTIFICATION_ALREADY_ID);
				} else {
					callback.onFail(context, context.getString(R.string.failed_to_logout));
				}
			}
		});
	}

	private static void showLoginNotification(Context context, UserModel.LoginType loginType,
	                                          String location) {
		NotificationHelper.createNotification(context, context.getString(
				loginType == UserModel.LoginType.DORM ? R.string.login_dorm : R.string.login_campus,
				context.getString(
						location.equals(Constant.JIANGONG_WIFI_SERVER) ? R.string.jiangong :
								R.string.yanchao)), true, false, Constant.NOTIFICATION_LOGIN_ID);
	}

	private static void loginSuccess(Context context, String location,
	                                 UserModel.LoginType loginType,
	                                 @NonNull GeneralCallback callback) {
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

		NotificationHelper.createNotification(context, result, false, false,
				Constant.NOTIFICATION_SUCCESS_ID);
		NotificationHelper.cancelNotification(context, Constant.NOTIFICATION_LOGIN_ID);
		NotificationHelper.cancelNotification(context, Constant.NOTIFICATION_FAIL_ID);
		NotificationHelper.cancelNotification(context, Constant.NOTIFICATION_ALREADY_ID);

		callback.onSuccess(context, result);
	}

	private static void loginFail(Context context, String reason,
	                              @NonNull GeneralCallback callback) {
		if (!(context instanceof MainActivity)) {
			NotificationHelper.createNotification(context, reason, false, true,
					Constant.NOTIFICATION_FAIL_ID);
		}
		NotificationHelper.cancelNotification(context, Constant.NOTIFICATION_LOGIN_ID);
		NotificationHelper.cancelNotification(context, Constant.NOTIFICATION_FAIL_ID);
		NotificationHelper.cancelNotification(context, Constant.NOTIFICATION_ALREADY_ID);

		callback.onFail(context, reason);
	}

}
