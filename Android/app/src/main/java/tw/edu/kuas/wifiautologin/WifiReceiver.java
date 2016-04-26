package tw.edu.kuas.wifiautologin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import tw.edu.kuas.wifiautologin.callbacks.GeneralCallback;
import tw.edu.kuas.wifiautologin.libs.Constant;
import tw.edu.kuas.wifiautologin.libs.LoginHelper;
import tw.edu.kuas.wifiautologin.libs.Memory;
import tw.edu.kuas.wifiautologin.libs.Utils;
import tw.edu.kuas.wifiautologin.models.UserModel;

public class WifiReceiver extends BroadcastReceiver {

	static Tracker mTracker;

	private void initGA(Context context) {
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
		mTracker = analytics.newTracker("UA-46334408-1");
		mTracker.enableExceptionReporting(true);
		mTracker.enableAdvertisingIdCollection(true);
		mTracker.enableAutoActivityTracking(true);

		mTracker.setScreenName("Wifi Receiver");
	}

	@Override
	public void onReceive(final Context context, Intent intent) {
		String action = intent.getAction();

		if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			Utils.forceUseWifi(context);
			initGA(context);

			NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			NetworkInfo.State state = networkInfo.getState();

			if (state == NetworkInfo.State.CONNECTED) {
				String ssid = Utils.getCurrentSSID(context);
				if (Utils.isExpectedSSID(ssid)) {
					String user = Memory.getString(context, Constant.MEMORY_KEY_USER, null);
					String pwd = Memory.getString(context, Constant.MEMORY_KEY_PASSWORD, null);
					if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pwd)) {
						UserModel model = Utils.tranUser(user, pwd);
						if (Constant.EXPECTED_SSIDS.get(2).equals(ssid)) {
							model.loginType = UserModel.LoginType.DORM;
						}
						login(context, model);
					}
				}
			}
		}
	}

	private void login(Context context, UserModel model) {
		LoginHelper.login(context, model, new GeneralCallback() {
			@Override
			public void onSuccess(final String message) {
				mTracker.send(
						new HitBuilders.EventBuilder().setCategory("logout").setAction("success")
								.setLabel(message).build());
			}

			@Override
			public void onFail(final String reason) {
				mTracker.send(new HitBuilders.EventBuilder().setCategory("logout").setAction("fail")
						.setLabel(reason).build());
			}

			@Override
			public void onAlready() {
				mTracker.send(new HitBuilders.EventBuilder().setCategory("logout")
						.setAction("alreadyLogout").build());
			}
		});
	}

}