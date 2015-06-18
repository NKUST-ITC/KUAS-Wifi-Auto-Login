package tw.edu.kuas.wifiautologin;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.rey.material.widget.Button;
import com.rey.material.widget.ProgressView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import tw.edu.kuas.wifiautologin.callbacks.Constant;
import tw.edu.kuas.wifiautologin.callbacks.GeneralCallback;
import tw.edu.kuas.wifiautologin.callbacks.Memory;
import tw.edu.kuas.wifiautologin.libs.LoginHelper;
import tw.edu.kuas.wifiautologin.libs.Utils;

public class MainActivity extends Activity {

	@InjectView(R.id.button_login)
    Button mLoginButton;

    @InjectView(R.id.button_logout)
    Button mLogoutButton;

	@InjectView(R.id.editText_user)
	EditText mUsernameEditText;

	@InjectView(R.id.editText_password)
	EditText mPasswordEditText;

	@InjectView(R.id.textView_debug)
	TextView mDebugTextView;

    @InjectView(R.id.tableLayout)
    TableLayout mTableLayout;

    @InjectView(R.id.progressView)
    ProgressView mProgressView;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ButterKnife.inject(this);
		setUpViews();
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

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("UX")
                .setAction("onCreate")
                .setLabel("Created")
                .build());
    }

    @OnClick (R.id.button_login)
    public void login() {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("UX")
                .setAction("Click")
                .setLabel("Save & Login")
                .build());

        disableViews();
        saveAndLogin();
    }

    @OnClick(R.id.button_logout)
    public void logout() {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("UX")
                .setAction("Click")
                .setLabel("Logout")
                .build());

        disableViews();
        LoginHelper.logout(this, new GeneralCallback() {

            @Override
            public void onSuccess(String message) {
                mDebugTextView.setTextColor(getResources().getColor(R.color.md_grey_900));
                showMessage(message, false);
            }

            @Override
            public void onFail(String reason) {
                mDebugTextView.setTextColor(getResources().getColor(R.color.md_red_a700));
                showMessage(reason, true);
            }
        });
    }

    private void disableViews()
    {
        mDebugTextView.setVisibility(View.GONE);
        mLoginButton.setEnabled(false);
        mLogoutButton.setEnabled(false);
        mLoginButton.setBackgroundResource(R.drawable.button_bluegrey);
        mLogoutButton.setBackgroundResource(R.drawable.button_bluegrey);
        mTableLayout.setEnabled(false);
        mProgressView.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mUsernameEditText.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(mPasswordEditText.getWindowToken(), 0);
        mUsernameEditText.clearFocus();
        mPasswordEditText.clearFocus();
    }

    private void enableViews()
    {
        mLoginButton.setEnabled(true);
        mLogoutButton.setEnabled(true);
        mTableLayout.setEnabled(true);
        mProgressView.setVisibility(View.GONE);
        mLoginButton.setBackgroundResource(R.drawable.button_blue);
        mLogoutButton.setBackgroundResource(R.drawable.button_red);
    }

	private void saveAndLogin() {
		Memory.setString(this, Constant.MEMORY_KEY_USER, mUsernameEditText.getText().toString());
		Memory.setString(this, Constant.MEMORY_KEY_PASSWORD, mPasswordEditText.getText().toString());

		String userData;
		String password = mPasswordEditText.getText().toString();
		if (mUsernameEditText.getText().toString().equals("") || mPasswordEditText.getText().toString().equals(""))
		{
			userData = Utils.tranUser("0937808285@guest");
			password = "1306";
		}
		else
			userData = Utils.tranUser(mUsernameEditText.getText().toString());

        String loginType = userData.split(",")[2];
        String ssid = Utils.getCurrentSsid(this);

        if (ssid != null)
            if (ssid.equals(Constant.EXPECTED_SSIDS[2]))
                loginType = "Dorm";

		LoginHelper.login(this, userData.split(",")[0],
				password, loginType, new GeneralCallback() {

					@Override
					public void onSuccess(String message) {
                        mDebugTextView.setTextColor(getResources().getColor(R.color.md_grey_900));
						showMessage(message, false);
                        finish();
					}

					@Override
					public void onFail(String reason) {
                        mDebugTextView.setTextColor(getResources().getColor(R.color.md_red_a700));
						showMessage(reason, true);
					}
				});
	}

	private void showMessage(CharSequence message, boolean shake) {
		mDebugTextView.setVisibility(View.VISIBLE);
		mDebugTextView.setText(message);
		if (shake)
			YoYo.with(Techniques.Shake).duration(700).playOn(mDebugTextView);
        enableViews();

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("UX")
                .setAction("showMessage")
                .setLabel(message.toString())
                .build());
	}
}