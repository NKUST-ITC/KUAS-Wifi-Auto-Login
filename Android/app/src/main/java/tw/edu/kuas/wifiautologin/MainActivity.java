package tw.edu.kuas.wifiautologin;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tw.edu.kuas.wifiautologin.base.SilentApplication;
import tw.edu.kuas.wifiautologin.callbacks.GeneralCallback;
import tw.edu.kuas.wifiautologin.libs.Constant;
import tw.edu.kuas.wifiautologin.libs.LoginHelper;
import tw.edu.kuas.wifiautologin.libs.Memory;
import tw.edu.kuas.wifiautologin.libs.Utils;
import tw.edu.kuas.wifiautologin.models.UserModel;

@SuppressWarnings("unused") public class MainActivity extends AppCompatActivity {

	@Bind(R.id.button_login) Button mLoginButton;

	@Bind(R.id.button_logout) Button mLogoutButton;

	@Bind(R.id.editText_user) EditText mUsernameEditText;

	@Bind(R.id.editText_password) EditText mPasswordEditText;

	@Bind(R.id.textView_debug) TextView mDebugTextView;

	@Bind(R.id.progressBar) ProgressBar mProgressBar;

	TextInputLayout mUserNameTextInputLayout, mPasswordTextInputLayout;

	private static Tracker mTracker;

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
		mTracker = ((SilentApplication) getApplication()).getDefaultTracker();
		mTracker.setScreenName("Main Screen");
	}

	@OnClick(R.id.button_login)
	public void login() {
		mTracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("Click")
				.setLabel("Save & Login").build());

		disableViews();
		saveAndLogin();
	}

	@OnClick(R.id.button_logout)
	public void logout() {
		mTracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("Click")
				.setLabel("Logout").build());

		if (Utils.checkGoogleBug()) {
			if (!Utils.checkSystemWritePermission(this)) {
				mTracker.send(new HitBuilders.EventBuilder().setCategory("Write System Permission")
						.setAction("Not allowed").setLabel(Utils.getPhoneName()).build());
				Utils.showSystemWritePermissionDialog(this);
				return;
			} else {
				mTracker.send(new HitBuilders.EventBuilder().setCategory("Write System Permission")
						.setAction("Allowed").setLabel(Utils.getPhoneName()).build());
			}
		}

		disableViews();
		LoginHelper.logout(this, true, new GeneralCallback() {
			@Override
			public void onSuccess(final String message) {
				mTracker.send(
						new HitBuilders.EventBuilder().setCategory("logout").setAction("success")
								.setLabel(message).build());
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mDebugTextView.setEnabled(true);
						showMessage(message);
					}
				});
			}

			@Override
			public void onFail(final String reason) {
				mTracker.send(new HitBuilders.EventBuilder().setCategory("logout").setAction("fail")
						.setLabel(reason).build());
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mDebugTextView.setEnabled(false);
						showMessage(reason);
					}
				});
			}

			@Override
			public void onAlready() {
				mTracker.send(new HitBuilders.EventBuilder().setCategory("logout")
						.setAction("alreadyLogout").build());
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mDebugTextView.setEnabled(true);
						showMessage(getString(R.string.already_logged_out));
					}
				});
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
		String user = mUsernameEditText.getText().toString();
		String pwd = mPasswordEditText.getText().toString();

		Memory.setString(this, Constant.MEMORY_KEY_USER, user);
		Memory.setString(this, Constant.MEMORY_KEY_PASSWORD, pwd);

		UserModel model;
		if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pwd)) {
			model = Utils.tranUser(Constant.DEFAULT_GUEST_ACCOUNT, Constant.DEFAULT_GUEST_PWD);
		} else {
			model = Utils.tranUser(user, pwd);
		}

		if (Utils.checkGoogleBug()) {
			if (!Utils.checkSystemWritePermission(this)) {
				mTracker.send(new HitBuilders.EventBuilder().setCategory("Write System Permission")
						.setAction("Not allowed").setLabel(Utils.getPhoneName()).build());
				Utils.showSystemWritePermissionDialog(this);
				enableViews();
				return;
			} else {
				mTracker.send(new HitBuilders.EventBuilder().setCategory("Write System Permission")
						.setAction("Allowed").setLabel(Utils.getPhoneName()).build());
			}
		}

		String ssid = Utils.getCurrentSSID(this);

		if (Constant.EXPECTED_SSIDS.get(2).equals(ssid)) {
			model.loginType = UserModel.LoginType.DORM;
		}

		LoginHelper.login(this, Utils.tranUser(user, pwd), new GeneralCallback() {
			@Override
			public void onSuccess(final String message) {
				if (isFinishing()) {
					return;
				}
				mTracker.send(
						new HitBuilders.EventBuilder().setCategory("login").setAction("success")
								.setLabel(message).build());
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mDebugTextView.setEnabled(true);
						showMessage(message);
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								finish();
							}
						}, 800);
					}
				});
			}

			@Override
			public void onFail(final String reason) {
				mTracker.send(new HitBuilders.EventBuilder().setCategory("login").setAction("fail")
						.setLabel(reason).build());
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mDebugTextView.setEnabled(false);
						showMessage(reason);
					}
				});
			}

			@Override
			public void onAlready() {
				mTracker.send(new HitBuilders.EventBuilder().setCategory("login")
						.setAction("alreadyLogin").build());
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this, R.string.already_logged_in,
								Toast.LENGTH_SHORT).show();
						finish();
					}
				});
			}
		});
	}

	private void showMessage(String message) {
		mDebugTextView.setVisibility(View.VISIBLE);
		mDebugTextView.setText(message);
		enableViews();
	}
}