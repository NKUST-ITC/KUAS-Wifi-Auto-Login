package silent.kuaswifiautologin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alertdialogpro.ProgressDialogPro;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import silent.network.NetworkStatus;

import static android.view.Gravity.START;

public class WifiAutoLogin extends Activity {
    private DrawerArrowDrawable drawerArrowDrawable;
    private float offset;
    private boolean flipped;

    public static final String PREF = "ACCOUNT_PREF";
    public static final String PREF_USERNAME = "USERNAME";
    public static final String PREF_PWD = "PASSWORD";
    public static final String TAG = "KUAS";
    public static final int TIMEOUT = 1000;

    private MaterialEditText usernameEditText;
    private MaterialEditText passwordEditText;
    private NetworkStatus networkStatus;

    private ListView listView;
    private String[] show_text = {"About"};

    private static final int NATIVE_THEME = Integer.MIN_VALUE;
    private int mTheme = -1;
    AlertDialog LoadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEditText = (MaterialEditText) findViewById(R.id.UserName);
        passwordEditText = (MaterialEditText) findViewById(R.id.Password);

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

        listView = (ListView)findViewById(R.id.listView);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(
                this,android.R.layout.simple_list_item_1, show_text){

            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent) {
                View view =super.getView(position, convertView, parent);
                TextView textView=(TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.BLACK);
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ImageView imageView = (ImageView) findViewById(R.id.drawer_indicator);
        final Resources resources = getResources();
        drawerArrowDrawable = new DrawerArrowDrawable(resources);
        drawerArrowDrawable.setStrokeColor(Color.WHITE);
        imageView.setImageDrawable(drawerArrowDrawable);
        drawer.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override public void onDrawerSlide(View drawerView, float slideOffset) {
                offset = slideOffset;
                // Sometimes slideOffset ends up so close to but not quite 1 or 0.
                if (slideOffset >= .995) {
                    flipped = true;
                    drawerArrowDrawable.setFlip(flipped);
                } else if (slideOffset <= .005) {
                    flipped = false;
                    drawerArrowDrawable.setFlip(flipped);
                }
                drawerArrowDrawable.setParameter(offset);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (drawer.isDrawerVisible(START)) {
                    drawer.closeDrawer(START);
                } else {
                    drawer.openDrawer(START);
                }
            }
        });

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
        if (usernameEditText.getText().toString().compareTo("") == 0 || passwordEditText.getText().toString().compareTo("") == 0)
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
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if (username.compareTo("") != 0 && password.compareTo("") != 0) {
            Button loginButton = (Button) findViewById(R.id.SignIn);
            // AutoLogin when there have been saved account and password
            loginButton.performClick();
        }
    }
    public void Loginkuas_wireless(String[] params_name, String[] params_value) throws IOException {
        String url = getString(R.string.url_kuas_wireless);
        // POST It!!
        // Try kuas.edu.tw
        HashMap<String, String> postDatas = new HashMap<>();
        postDatas.put(params_name[0], params_value[0] + "@kuas.edu.tw");
        postDatas.put(params_name[1], params_value[1]);
        postDatas.put(params_name[2], params_value[2]);
        Jsoup.connect(url).timeout(TIMEOUT).data(postDatas).ignoreContentType(true).method(Connection.Method.POST).execute();

        // Try gm.kuas.edu.tw
        postDatas.clear();
        postDatas.put(params_name[0], params_value[0] + "@gm.kuas.edu.tw");
        postDatas.put(params_name[1], params_value[1]);
        postDatas.put(params_name[2], params_value[2]);
        Jsoup.connect(url).timeout(TIMEOUT).data(postDatas).ignoreContentType(true).method(Connection.Method.POST).execute();

        // Try if other school
        //sendHttpPost(url,
        // "username=" + params_value[0] +
        // "&userpwd=" + params_value[1] +
        // "&login=login");
    }
    public void LoginKUAS(String[] params_name, String[] params_value) throws IOException {
        String url = getString(R.string.url_KUAS);
        HashMap<String, String> postDatas = new HashMap<>();
        // POST It!!
        // Try kuas.edu.tw
        if (params_value[0].length() > 4 && Integer.parseInt(params_value[0].substring(1, 4)) <= 102) {
            Log.v(TAG, "lower then 102");
            postDatas.put(params_name[0], params_value[0] + "@kuas.edu.tw");
            postDatas.put(params_name[1], params_value[1]);
            postDatas.put(params_name[2], params_value[2]);
            Jsoup.connect(url).timeout(TIMEOUT).data(postDatas).ignoreContentType(true).method(Connection.Method.POST).execute();
        } else {
            Log.v(TAG, "upper then 102");
            postDatas.put(params_name[0], params_value[0] + "@gm.kuas.edu.tw");
            postDatas.put(params_name[1], params_value[1]);
            postDatas.put(params_name[2], params_value[2]);
            Jsoup.connect(url).timeout(TIMEOUT).data(postDatas).ignoreContentType(true).method(Connection.Method.POST).execute();
        }
    }
    public void LoginKUAS_guest(String[] params_name) throws IOException {
        String url = getString(R.string.url_KUAS);
        // POST It!!
        HashMap<String, String> postDatas = new HashMap<>();
        postDatas.put(params_name[0], "0937808285@guest");
        postDatas.put(params_name[1], "1306");
        postDatas.put(params_name[2], "login");
        postDatas.put(params_name[3], "");
        Jsoup.connect(url).timeout(TIMEOUT).data(postDatas).ignoreContentType(true).method(Connection.Method.POST).execute();
    }
    public void loginBthOnclick(View view) {
//        if (!checkAccountPassword())
//            return;
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
                params_value[0] = usernameEditText.getText().toString();
                params_value[1] = passwordEditText.getText().toString();
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
                    // Check if using guest login
                    if (params_value[0].compareTo("") == 0 ||
                            params_value[1].compareTo("") == 0) {
                        Log.v(TAG, "use guest login");
                        login_msg = getString(R.string.login_guest_successfully);
                        LoginKUAS_guest(params_name);
                    } else {
                        LoginKUAS(params_name, params_value);
                    }
                    Log.v(TAG, "CHECK IS CONNECT: " + isConnect());
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
                    NotificationManager mNotificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    Notification notification=new Notification(R.drawable.ic_launcher, "高應無線通",  System.currentTimeMillis());
                    notification.defaults=Notification.DEFAULT_ALL;
                    notification.setLatestEventInfo(WifiAutoLogin.this,"高應無線通", "Wi-Fi 登入成功！", null);
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
