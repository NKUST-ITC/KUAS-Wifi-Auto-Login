package tw.edu.kuas.wifiautologin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.edu.kuas.wifiautologin.callbacks.Constant;
import tw.edu.kuas.wifiautologin.callbacks.GeneralCallback;
import tw.edu.kuas.wifiautologin.callbacks.Memory;
import tw.edu.kuas.wifiautologin.libs.LoginHelper;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	@InjectView(R.id.button_login)
	Button mLoginButton;

	@InjectView(R.id.editText_user)
	EditText mUsernameEditText;

	@InjectView(R.id.editText_password)
	EditText mPasswordEditText;

	@InjectView(R.id.textView_debug)
	TextView mDebugTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ButterKnife.inject(this);
		setUpViews();
	}

	private void setUpViews() {
		mLoginButton.setOnClickListener(this);
		mUsernameEditText.setText(Memory.getString(this, Constant.MEMORY_KEY_USER, ""));
		mPasswordEditText.setText(Memory.getString(this, Constant.MEMORY_KEY_PASSWORD, ""));
		mPasswordEditText.setImeActionLabel(getText(R.string.ime_submit), KeyEvent.KEYCODE_ENTER);
		mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				saveAndLogin();
				return false;
			}
		});
	}

	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case R.id.about:
				startActivity(new Intent(this, AboutActivity.class));
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onClick(View v) {
		if (v == mLoginButton) {
			saveAndLogin();
		}
	}

	private void saveAndLogin() {
		Memory.setString(this, Constant.MEMORY_KEY_USER, mUsernameEditText.getText().toString());
		Memory.setString(this, Constant.MEMORY_KEY_PASSWORD,
				mPasswordEditText.getText().toString());

		String userData;
		String password = mPasswordEditText.getText().toString();
		if (mUsernameEditText.getText().toString().equals("") || mPasswordEditText.getText().toString().equals(""))
		{
			userData = tranUser("0937808285@guest@guest");
			password = "1306";
		}
		else
			userData = tranUser(mUsernameEditText.getText().toString());

		LoginHelper.login(this, userData.split(",")[1], userData.split(",")[0],
				password, userData.split(",")[2], new GeneralCallback() {

					@Override
					public void onSuccess() {
						showMessage(R.string.login_sucessful);
					}

					@Override
					public void onFail(String reason) {
						showMessage(reason);
					}
				});
	}

	private String tranUser(String user)
	{
		if (user.contains("@kuas.edu.tw") || user.contains("@gm.kuas.edu.tw"))
			if (user.contains("@kuas.edu.tw"))
				return user + ",1,Student";
			else
				return user + ",@gm.kuas.edu.tw,Student";
		else if (user.length() == 10)
			if (Integer.parseInt(user.substring(1,4)) <= 102)
				return user + "@kuas.edu.tw" + ",1,Student";
			else
				return user + "@gm.kuas.edu.tw" + ",@gm.kuas.edu.tw,Student";
		else if (user.contains("@") && !user.contains("@guest"))
			return user + ",,Cyber";
		else
			return user + ",,Guest";
	}

	private void showMessage(int messageRes) {
		mDebugTextView.setVisibility(View.VISIBLE);
		mDebugTextView.setText(getText(messageRes));
	}

	private void showMessage(CharSequence message) {
		mDebugTextView.setVisibility(View.VISIBLE);
		mDebugTextView.setText(message);
	}
}
