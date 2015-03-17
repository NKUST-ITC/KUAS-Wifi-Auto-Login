package tw.edu.kuas.wifiautologin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alertdialogpro.ProgressDialogPro;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import tw.edu.kuas.network.NetworkStatus;

public class WifiAutoLogin extends Activity {
    public static final String PREF = "ACCOUNT_PREF";
    public static final String PREF_USERNAME = "USERNAME";
    public static final String PREF_PWD = "PASSWORD";
    public static final String TAG = "KUAS";
    public static final int TIMEOUT = 1500;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private NetworkStatus networkStatus;

    private static final int NATIVE_THEME = Integer.MIN_VALUE;
    private int mTheme = -1;
    AlertDialog LoadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        usernameEditText = (EditText) findViewById(R.id.Username);
        passwordEditText = (EditText) findViewById(R.id.Password);

        networkStatus = new NetworkStatus(this);

        mTheme = R.style.Theme_AlertDialogPro_Material;
        LoadingDialog = createProgressDialog();
        LoadingDialog.setMessage("Loading...");
        ProgressDialogPro progressDialog = (ProgressDialogPro) LoadingDialog;
        progressDialog.setProgressStyle(ProgressDialogPro.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        LoadingDialog.setCancelable(false);
        LoadingDialog.setCanceledOnTouchOutside(false);

        restorePrefs();

        AutoLogin();
    }


    private AlertDialog createProgressDialog() {
        if (mTheme == NATIVE_THEME) {
            return new ProgressDialog(this);
        }
        return new ProgressDialogPro(this, mTheme);
    }

    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(WifiAutoLogin.this, toast, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
    public boolean isConnect() {
        try {
            if (isLogin()) {
                Log.v(TAG, "is connected");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.v(TAG, e.toString());
            return false;
        }
    }
    public boolean checkAccountPassword() {
        if (usernameEditText.getText().toString().compareTo("") == 0
                || passwordEditText.getText().toString().compareTo("") == 0
                || (usernameEditText.getText().toString().length() != 10
                && usernameEditText.getText().toString().length() != 22
                && usernameEditText.getText().toString().length() != 25))
            return false;
        else
            return true;
    }
    public boolean checkSSID() {
        String ssid = networkStatus.getSSID();
        if (ssid.compareTo(getString(R.string.ssid_kuas_wireless)) != 0
                && ssid.compareTo(getString(R.string.ssid_KUAS)) != 0
                && ssid.compareTo(getString(R.string.ssid_KUAS_Dorm)) != 0) {
            if (ssid.compareTo("<unknown ssid>") != 0) {
                showToast(String
                        .format(getString(R.string.err_ssid_msg_format),
                                ssid.compareTo("<unknown ssid>") == 0 ? "NO CONNECTION"
                                        : ssid,
                                getString(R.string.err_ssid_msg)));
            } else {
                showToast(getString(R.string.err_ssid_msg));
            }
            return false;
        }
        return true;
    }
    public boolean isLogin() throws Exception {
        Document res = Jsoup.connect("http://www.example.com").timeout(TIMEOUT).ignoreContentType(true).method(Connection.Method.GET).get();
        if (res.title().compareTo("Example Domain") == 0)
            return true;
        else
            return false;
    }

    public void AutoLogin() {
        if (checkAccountPassword()) {
            Button loginButton = (Button) findViewById(R.id.SignIn);
            // AutoLogin when there have been saved account and password
            loginButton.performClick();
        }
    }

    public void LoginKUAS(String[] params_name, String[] params_value) throws IOException {
        String url = getString(R.string.url_KUAS);
        HashMap<String, String> postDatas = new HashMap<>();
        Document res;
        // POST It!!
        // Try kuas.edu.tw
        if (params_value[1].length() > 4 && Integer.parseInt(params_value[1].substring(1, 4)) <= 102) {
            Log.v(TAG, "lower then 102");
            postDatas.put(params_name[0], "1");
            if (!params_value[1].contains("@kuas.edu.tw"))
                postDatas.put(params_name[1], params_value[1] + "@kuas.edu.tw");
            else
                postDatas.put(params_name[1], params_value[1]);
            postDatas.put(params_name[2], params_value[2]);
            postDatas.put(params_name[3], params_value[3]);
            postDatas.put(params_name[4], "");
            Jsoup.connect(url).timeout(TIMEOUT).data(postDatas).referrer("http://172.16.61.253/login.php").ignoreContentType(true).method(Connection.Method.POST).post();
        } else {
            Log.v(TAG, "upper then 102");
            postDatas.put(params_name[0], "@gm.kuas.edu.tw");
            if (!params_value[1].contains("@gm.kuas.edu.tw"))
                postDatas.put(params_name[1], params_value[1] + "@gm.kuas.edu.tw");
            else
                postDatas.put(params_name[1], params_value[1]);
            postDatas.put(params_name[2], params_value[2]);
            postDatas.put(params_name[3], params_value[3]);
            postDatas.put(params_name[4], "");
            Jsoup.connect(url).timeout(TIMEOUT).data(postDatas).referrer("http://172.16.61.253/login.php").ignoreContentType(true).method(Connection.Method.POST).post();
        }
    }

    public void LoginKUAS_guest(String[] params_name, String[] params_value) throws IOException {
        String url = getString(R.string.url_KUAS);
        // POST It!!
        HashMap<String, String> postDatas = new HashMap<>();
        postDatas.put(params_name[0], "");
        postDatas.put(params_name[1], params_value[1]);
        postDatas.put(params_name[2], params_value[2]);
        postDatas.put(params_name[3], params_value[3]);
        postDatas.put(params_name[4], "");
        Jsoup.connect(url).timeout(TIMEOUT).data(postDatas).referrer("http://172.16.61.253/login.php").ignoreContentType(true).method(Connection.Method.POST).post();
    }

    public void LoginKUAS_cyber(String[] params_name, String[] params_value) throws IOException {
        String url = getString(R.string.url_KUAS);
        // POST It!!
        HashMap<String, String> postDatas = new HashMap<>();
        postDatas.put(params_name[0], "");
        postDatas.put(params_name[1], params_value[1]);
        postDatas.put(params_name[2], params_value[2]);
        postDatas.put(params_name[3], params_value[3]);
        postDatas.put(params_name[4], params_value[4]);
        Jsoup.connect(url).timeout(TIMEOUT).data(postDatas).referrer("http://172.16.61.253/login.php").ignoreContentType(true).method(Connection.Method.POST).post();
    }

    public void loginBthOnclick(View view) {
        Log.v(TAG, "Checking SSID");
        // Check SSID
        if (!checkSSID())
            return;

        LoginHandler.sendEmptyMessage(1);
        new Thread() {
            @Override
            public void run() {
                // Delay for progressDialog
                try {
                    Thread.sleep(800);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Resources resources = getResources();
                String login_msg = getString(R.string.login_successfully);
                String[] params_name = resources.getStringArray(R.array.params_name);
                String[] params_value = resources.getStringArray(R.array.params_value);
                // Connection Testing
                Log.v(TAG, "Testing Connection");
                if (isConnect()) {
                    showToast(getString(R.string.login_ready));
                    LoginHandler.sendEmptyMessage(-1);
                    finish();
                    return;
                }
                Log.v(TAG, "End test connection");
                // Check params
                if (params_name.length != params_value.length)
                    Log.e(getString(R.string.app_name), "Config file error!");

                try {
                    // Check if using guest or cyber login
                    if (!checkAccountPassword()) {
                        if (usernameEditText.getText().toString().equals("") || passwordEditText.getText().toString().equals(""))
                        {
                            Log.v(TAG, "use guest login");
                            login_msg = getString(R.string.login_guest_successfully);
                            params_value[1] = "0937808285@guest@guest";
                            params_value[2] = "1306";
                            LoginKUAS_guest(params_name, params_value);
                        }
                        else
                        {
                            Log.v(TAG, "use cyber surfing login");
                            login_msg = getString(R.string.login_cyber_successfully);
                            params_value[1] = usernameEditText.getText().toString();
                            params_value[2] = passwordEditText.getText().toString();
                            LoginKUAS_cyber(params_name, params_value);
                        }
                    } else {
                        params_value[1] = usernameEditText.getText().toString();
                        params_value[2] = passwordEditText.getText().toString();
                        LoginKUAS(params_name, params_value);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    showToast(getString(R.string.timeout));
                    LoginHandler.sendEmptyMessage(-1);
                    return;
                }
                LoginHandler.sendEmptyMessage(-1);
                // Check if login finish
                if (isConnect()) {
                    final int notifyID = 1;
                    final int requestCode = notifyID;
                    final Intent intent = getIntent();
                    final int flags = PendingIntent.FLAG_CANCEL_CURRENT;
                    final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestCode, intent, flags);

                    NotificationManager mNotificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    Notification notification=new Notification(R.mipmap.ic_launcher, "高應無線通",  System.currentTimeMillis());
                    notification.defaults=Notification.DEFAULT_ALL;
                    notification.setLatestEventInfo(WifiAutoLogin.this,"高應無線通", "Wi-Fi 登入成功！", pendingIntent);
                    mNotificationManager.notify(1000, notification);
                    showToast(login_msg);
                } else {
                    showToast(getString(R.string.login_error));
                    return;
                }
                finish();
            };
        }.start();
    }

    private Handler LoginHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
        switch (msg.what)
        {
            case -1:
                LoadingDialog.dismiss();
                break;
            case 1:
                LoadingDialog.show();
                break;
        }
    };
};

    private void restorePrefs() {
        SharedPreferences setting = getSharedPreferences(PREF, 0);
        String username = setting.getString(PREF_USERNAME, "");
        String password = setting.getString(PREF_PWD, "");
        usernameEditText.setText(username);
        passwordEditText.setText(password);
    }

    private void savePrefs() {
        SharedPreferences setting = getSharedPreferences(PREF, 0);
        setting.edit()
                .putString(PREF_USERNAME, usernameEditText.getText().toString())
                .putString(PREF_PWD, passwordEditText.getText().toString())
                .apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkSSID()) {
            Toast.makeText(this, getString(R.string.err_ssid_msg),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePrefs();
    }
}
