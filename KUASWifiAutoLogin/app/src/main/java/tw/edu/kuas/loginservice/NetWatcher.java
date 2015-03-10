package tw.edu.kuas.loginservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import tw.edu.kuas.loginservice.LoginService;

public class NetWatcher extends BroadcastReceiver {
    private static final String TAG = "silent.loginservice.NetWatcher";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"Live");
        Intent srvIntent = new Intent();
        srvIntent.setClass(context, LoginService.class);
        context.startService(srvIntent);
    }
}