package tw.edu.kuas.wifiautologin.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import tw.edu.kuas.wifiautologin.R;
import tw.edu.kuas.wifiautologin.base.SilentApplication;
import tw.edu.kuas.wifiautologin.callbacks.GeneralCallback;
import tw.edu.kuas.wifiautologin.libs.Constant;
import tw.edu.kuas.wifiautologin.libs.LoginHelper;
import tw.edu.kuas.wifiautologin.libs.Memory;
import tw.edu.kuas.wifiautologin.libs.Utils;
import tw.edu.kuas.wifiautologin.models.UserModel;

public class WidgetService extends Service {

	private static Tracker mTracker;

	public WidgetService() {
	}

	@Override
	public void onCreate() {
		initGA();
	}

	private void initGA() {
		mTracker = ((SilentApplication) getApplication()).getDefaultTracker();
		mTracker.setScreenName("widget");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		login();

		return START_NOT_STICKY;
	}

	private void changeWidgetMessage(String message, int color) {
		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		ComponentName thisAppWidget =
				new ComponentName(getPackageName(), WidgetProvider.class.getName());
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
		final int N = appWidgetIds.length;
		remoteViews.setTextViewText(R.id.widget_message, message);
		remoteViews.setTextColor(R.id.widget_message, color);
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		}

	}

	@Override
	public void onDestroy() {
		Log.d("service", "onDestroy");
	}

	public void login() {
		String user = Memory.getString(this, Constant.MEMORY_KEY_USER, "");
		String pwd = Memory.getString(this, Constant.MEMORY_KEY_PASSWORD, "");
		UserModel model;
		if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pwd)) {
			model = Utils.tranUser(Constant.DEFAULT_GUEST_ACCOUNT, Constant.DEFAULT_GUEST_PWD);
		} else {
			model = Utils.tranUser(user, pwd);
		}

		String ssid = Utils.getCurrentSSID(this);

		if (Constant.EXPECTED_SSIDS.get(2).equals(ssid)) {
			model.loginType = UserModel.LoginType.DORM;
		}

		LoginHelper.login(this, Utils.tranUser(user, pwd), new GeneralCallback() {

			@Override
			public void onSuccess(final String message) {
				mTracker.send(
						new HitBuilders.EventBuilder().setCategory("login").setAction("success")
								.setLabel(message).build());
				changeWidgetMessage(message, getResources().getColor(R.color.md_green_400));
				stopSelf();
			}

			@Override
			public void onFail(final String reason) {
				mTracker.send(new HitBuilders.EventBuilder().setCategory("login").setAction("fail")
						.setLabel(reason).build());
				changeWidgetMessage(reason, getResources().getColor(R.color.md_amber_800));
				stopSelf();
			}

			@Override
			public void onAlready() {
				mTracker.send(new HitBuilders.EventBuilder().setCategory("login")
						.setAction("alreadyLogin").build());
				changeWidgetMessage(getResources().getString(R.string.widget_already_connect),
						getResources().getColor(R.color.md_green_400));
				stopSelf();
			}
		});
	}

	public void logout() {
		mTracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("Click")
				.setLabel("Logout").build());
		if (Utils.checkGoogleBug()) {
			if (!Utils.checkSystemWritePermission(this)) {
				mTracker.send(new HitBuilders.EventBuilder().setCategory("Write System Permission")
						.setAction("Denied").setLabel(Utils.getPhoneName()).build());
				return;
			} else {
				mTracker.send(new HitBuilders.EventBuilder().setCategory("Write System Permission")
						.setAction("Allowed").setLabel(Utils.getPhoneName()).build());
			}
		}
		LoginHelper.logout(this, true, new GeneralCallback() {

			@Override
			public void onSuccess(final String message) {

				mTracker.send(
						new HitBuilders.EventBuilder().setCategory("logout").setAction("success")
								.setLabel(message).build());
				changeWidgetMessage(message, getResources().getColor(R.color.md_deep_orange_a400));
				stopSelf();
			}

			@Override
			public void onFail(final String reason) {

				mTracker.send(new HitBuilders.EventBuilder().setCategory("logout").setAction("fail")
						.setLabel(reason).build());
				changeWidgetMessage(reason, getResources().getColor(R.color.md_deep_orange_a400));
				stopSelf();
			}

			@Override
			public void onAlready() {
				mTracker.send(new HitBuilders.EventBuilder().setCategory("logout")
						.setAction("alreadyLogout").build());

				stopSelf();
			}
		});
	}

}
