package tw.edu.kuas.wifiautologin.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tw.edu.kuas.wifiautologin.R;
import tw.edu.kuas.wifiautologin.base.SilentApplication;
import tw.edu.kuas.wifiautologin.libs.Constant;
import tw.edu.kuas.wifiautologin.libs.Memory;
import tw.edu.kuas.wifiautologin.libs.Utils;

public class WidgetConfigureActivity extends AppCompatActivity {
    @BindView(R.id.button_save)Button mSaveButton;
    @BindView(R.id.button_finish) Button mFinishButton;
    @BindView(R.id.editText_user)EditText mUsernameEditText;
    @BindView(R.id.editText_password) EditText mPasswordEditText;
    @BindView(R.id.textInputLayout_user)TextInputLayout mUserNameTextInputLayout;
    @BindView(R.id.textInputLayout_password) TextInputLayout mPasswordTextInputLayout;
    private static Tracker mTracker;

    private static int REQUEST_WRITE_SYSTEM_SETTINGS_LOGIN = 200;

    private int mAppWidgetId =AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_configure);

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        setUpViews();

    }
    private void setUpViews() {
        mUsernameEditText.setText(Memory.getString(this, Constant.MEMORY_KEY_USER, ""));
        mPasswordEditText.setText(Memory.getString(this, Constant.MEMORY_KEY_PASSWORD, ""));
        mPasswordEditText.setImeActionLabel(getText(R.string.ime_submit), KeyEvent.KEYCODE_ENTER);
        mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mSaveButton.performClick();
                return false;
            }
        });
        mUserNameTextInputLayout.setHint(getString(R.string.id_hint));
        mPasswordTextInputLayout.setHint(getString(R.string.password_hint));
        initGA();
    }
    @OnClick(R.id.button_save)
    public void save(){
        mTracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("Click")
                .setLabel("Save").build());
        String user = mUsernameEditText.getText().toString();
        String pwd = mPasswordEditText.getText().toString();
        //save user & password to SharedPreferences
        Memory.setString(this, Constant.MEMORY_KEY_USER, user);
        Memory.setString(this, Constant.MEMORY_KEY_PASSWORD, pwd);

        Toast.makeText(this,R.string.save,Toast.LENGTH_SHORT).show();

        checkGoogleBug();

    }
    @OnClick(R.id.button_finish)
    public void mFinish(){
        mTracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("Click")
                .setLabel("Finish").build());
        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        save();

        finish();

    }
    private void initGA() {
        mTracker = ((SilentApplication) getApplication()).getDefaultTracker();
        mTracker.setScreenName("Widget Configure Screen");
    }
    private void checkGoogleBug(){

        if (Utils.checkGoogleBug()) {
            if (!Utils.checkSystemWritePermission(this)) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Write System Permission")
                        .setAction("Denied").setLabel(Utils.getPhoneName()).build());
                         Utils.showSystemWritePermissionDialog(this, REQUEST_WRITE_SYSTEM_SETTINGS_LOGIN);
            } else {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Write System Permission")
                        .setAction("Allowed").setLabel(Utils.getPhoneName()).build());
            }
        }

    }
}
