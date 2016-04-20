package tw.edu.kuas.wifiautologin;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tw.edu.kuas.wifiautologin.libs.Constant;
import tw.edu.kuas.wifiautologin.callbacks.GeneralCallback;
import tw.edu.kuas.wifiautologin.libs.Memory;
import tw.edu.kuas.wifiautologin.libs.LoginHelper;
import tw.edu.kuas.wifiautologin.libs.Utils;

public class MainActivity extends AppCompatActivity {

	@Bind(R.id.button_login) Button mLoginButton;

	@Bind(R.id.button_logout) Button mLogoutButton;

	@Bind(R.id.editText_user) EditText mUsernameEditText;

	@Bind(R.id.editText_password) EditText mPasswordEditText;

	@Bind(R.id.textView_debug) TextView mDebugTextView;

	@Bind(R.id.progressBar) ProgressBar mProgressBar;

	TextInputLayout mUserNameTextInputLayout, mPasswordTextInputLayout;

	public static GoogleAnalytics analytics;
	public static Tracker tracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ButterKnife.bind(this);
		findViews();
		setUpViews();
	}

	private void findViews() {
		mUserNameTextInputLayout = (TextInputLayout) mUsernameEditText.getParent();
		mPasswordTextInputLayout = (TextInputLayout) mPasswordEditText.getParent();
	}

	private void setUpViews() {
		mUsernameEditText.setText(Memory.getString(this, Constant.MEMORY_KEY_USER, ""));
		mPasswordEditText.setText(Memory.getString(this, Constant.MEMORY_KEY_PASSWORD, ""));
		mPasswordEditText.setImeActionLabel(getText(R.string.ime_submit), KeyEvent.KEYCODE_ENTER);
		mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				mLoginButton.performClick();
				return false;
			}
		});
		mUserNameTextInputLayout.setHint(getString(R.string.id_hint));
		mPasswordTextInputLayout.setHint(getString(R.string.password_hint));
		mProgressBar.getIndeterminateDrawable().setColorFilter(
				ContextCompat.getColor(MainActivity.this, R.color.md_light_green_500),
				PorterDuff.Mode.SRC_IN);

		initGA();
	}

	private void initGA() {
		analytics = GoogleAnalytics.getInstance(this);
		analytics.setLocalDispatchPeriod(30);

		tracker = analytics.newTracker("UA-46334408-1");
		tracker.enableExceptionReporting(true);
		tracker.enableAdvertisingIdCollection(true);
		tracker.enableAutoActivityTracking(true);

		tracker.setScreenName("Main");

		tracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("onCreate")
				.setLabel("Created").build());
	}

	@OnClick(R.id.button_login)
	public void login() {
		tracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("Click")
				.setLabel("Save & Login").build());

		disableViews();
		saveAndLogin();
	}

	@OnClick(R.id.button_logout)
	public void logout() {
		tracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("Click")
				.setLabel("Logout").build());

		disableViews();
		LoginHelper.logout(this, new GeneralCallback() {

			@Override
			public void onSuccess(String message) {
				mDebugTextView.setTextColor(
						ContextCompat.getColor(MainActivity.this, R.color.black_text));
				showMessage(message);
			}

			@Override
			public void onFail(String reason) {
				mDebugTextView.setTextColor(
						ContextCompat.getColor(MainActivity.this, R.color.md_red_a700));
				showMessage(reason);
			}
		});
	}

	private void disableViews() {
		mDebugTextView.setVisibility(View.GONE);
		mLoginButton.setEnabled(false);
		mLogoutButton.setEnabled(false);
		mProgressBar.setVisibility(View.VISIBLE);

		mLoginButton.setBackgroundResource(R.drawable.button_disable);
		mLogoutButton.setBackgroundResource(R.drawable.button_disable);

		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mUsernameEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(mPasswordEditText.getWindowToken(), 0);

		mUsernameEditText.clearFocus();
		mPasswordEditText.clearFocus();
		mUsernameEditText.setEnabled(false);
		mPasswordEditText.setEnabled(false);
	}

	private void enableViews() {
		mLoginButton.setEnabled(true);
		mLogoutButton.setEnabled(true);
		mUsernameEditText.setEnabled(true);
		mPasswordEditText.setEnabled(true);
		mProgressBar.setVisibility(View.GONE);

		mLoginButton.setBackgroundResource(R.drawable.button_login);
		mLogoutButton.setBackgroundResource(R.drawable.button_logout);
	}

	private void saveAndLogin() {
		Memory.setString(this, Constant.MEMORY_KEY_USER, mUsernameEditText.getText().toString());
		Memory.setString(this, Constant.MEMORY_KEY_PASSWORD,
				mPasswordEditText.getText().toString());

		String userData;
		String password = mPasswordEditText.getText().toString();
		if (TextUtils.isEmpty(mUsernameEditText.getText().toString()) ||
				TextUtils.isEmpty(password)) {
			userData = Utils.tranUser("0937808285@guest");
			password = "1306";
		} else {
			userData = Utils.tranUser(mUsernameEditText.getText().toString());
		}

		String loginType = userData.split(",")[2];
		String ssid = Utils.getCurrentSsid(this);

		if (!TextUtils.isEmpty(ssid)) {
			if (ssid.equals(Constant.EXPECTED_SSIDS[2])) {
				loginType = "Dorm";
			}
		}

		LoginHelper.login(this, userData.split(",")[0], password, loginType, new GeneralCallback() {

			@Override
			public void onSuccess(String message) {
				mDebugTextView.setTextColor(
						ContextCompat.getColor(MainActivity.this, R.color.black_text));
				showMessage(message);
				finish();
			}

			@Override
			public void onFail(String reason) {
				mDebugTextView.setTextColor(
						ContextCompat.getColor(MainActivity.this, R.color.md_red_a700));
				showMessage(reason);
			}
		});
	}

	private void showMessage(CharSequence message) {
		mDebugTextView.setVisibility(View.VISIBLE);
		mDebugTextView.setText(message);
		enableViews();

		tracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("showMessage")
				.setLabel(message.toString()).build());
	}
}