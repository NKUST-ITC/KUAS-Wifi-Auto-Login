package tw.edu.kuas.wifiautologin.base;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import io.fabric.sdk.android.Fabric;
import tw.edu.kuas.wifiautologin.BuildConfig;
import tw.edu.kuas.wifiautologin.R;

public class SilentApplication extends Application {

	private Tracker mTracker;

	@Override
	public void onCreate() {
		super.onCreate();

		// Init Fabric
		Fabric.with(this, new Crashlytics.Builder()
				.core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build());
	}

	/**
	 * Gets the default {@link Tracker} for this {@link Application}.
	 *
	 * @return tracker
	 */
	synchronized public Tracker getDefaultTracker() {
		if (mTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
			mTracker = analytics.newTracker(R.xml.global_tracker);
			mTracker.enableAdvertisingIdCollection(true);
			mTracker.enableExceptionReporting(false);
		}
		return mTracker;
	}
}
