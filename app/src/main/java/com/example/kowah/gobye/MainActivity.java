package com.example.kowah.gobye;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.text.InputType.TYPE_CLASS_NUMBER;

public class MainActivity extends AppCompatActivity {
    Context mcontext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RESULT_FIRST_USER);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RESULT_FIRST_USER);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, RESULT_FIRST_USER);

        final SharedPreferences user_info = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        final String card_id = user_info.getString("card_id", "default");
        long ddlVaild = System.currentTimeMillis() - user_info.getLong("ddlValid", 0);

        if (card_id.equals("default")) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

        if (ddlVaild > 180 * 1000) {
            new DDLTask().execute(mcontext);
        }

        CardView login = findViewById(R.id.loginWiFi);
        login.setOnClickListener(new View.OnClickListener() {
            long[] mHints = new long[2];

            @Override
            public void onClick(View v) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                registerReceiver(new WifiReceiver(), filter);

                WifiManager wifiManager = (WifiManager) mcontext.getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                WifiConfiguration conf = new WifiConfiguration();
                conf.SSID = "\\SZU_WLAN\\";
                conf.status = WifiConfiguration.Status.ENABLED;

                int res = wifiManager.addNetwork(conf);
                boolean b = wifiManager.enableNetwork(res, false);
                Log.d("Kowah",res+""+b);

                WebView autoLogin = findViewById(R.id.autoLogin);
                String card_password = user_info.getString("card_password", "default");
                if (card_password.equals("default")) {
                    showInputDialog();
                } else {
                    System.arraycopy(mHints, 1, mHints, 0, mHints.length - 1);
                    mHints[mHints.length - 1] = SystemClock.uptimeMillis();
                    if (SystemClock.uptimeMillis() - mHints[0] < 500) {
                        Snackbar.make(autoLogin, "请勿连续重复点击", Snackbar.LENGTH_SHORT).show();
                    }
                    if (!wifiManager.isWifiEnabled()) {
                        final ProgressDialog on = new ProgressDialog(MainActivity.this);
                        on.setMessage("正在打开WiFi...请稍等");
                        on.show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                on.dismiss();
                            }
                        }).start();
                    } else {
                        wifiManager.addNetwork(conf);
                        String html = "<!doctype html><html><head><meta charset=\"utf-8\"><title>Autologin</title></head><body>\n" +
                                "<form action=\"http://172.30.255.2/\" method=post enctype=\"application/x-www-form-urlencoded\" id=form1 name=form1>\n" +
                                "<INPUT TYPE=\"text\" NAME=\"DDDDD\" id=DDDDD VALUE=\"" + card_id + "\">\n" +
                                "<INPUT TYPE=\"password\" NAME=\"upass\" id=upass VALUE=\"" + card_password + "\">\n" +
                                "<INPUT TYPE=\"hidden\" NAME=\"0MKKey\" VALUE=\"%B5%C7%C2%BC+Login\" id=0MKKey>\n" +
                                "<INPUT TYPE=\"submit\" ACTION=\"http://172.30.255.2/\" VALUE=\"Login\" METHOD=\"post\" id=\"btn\" NAME=\"Submit\">\n" +
                                "</form><script>var btn = document.getElementById('btn');btn.click();</script></body></html>";
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

//                        if (wifiInfo.getSSID().equals("SZU_WLAN")) {
                        Toast toast = Toast.makeText(getApplicationContext(), "连接到" + wifiInfo.getSSID(), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        autoLogin.setWebViewClient(new WebViewClient());
                        autoLogin.getSettings().setJavaScriptEnabled(true);
                        autoLogin.loadData(html, "", "");
//                        }
                    }
                }
            }
        });

        CardView gobye = findViewById(R.id.gobye);
        gobye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GobyeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        CardView course = findViewById(R.id.course);
        course.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CourseActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        CardView search = findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DDLTask().execute(mcontext);
                Intent intent = new Intent(MainActivity.this, DeadlineActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        ImageButton account = findViewById(R.id.imageButton);
        account.getBackground().setAlpha(0);
        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "请允许程序运行所需的权限", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class WifiReceiver extends BroadcastReceiver {
        private static final String TAG = "wifi666";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    Log.i(TAG, "wifi断开");
                }
                if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    //获取WiFi名称
                    Log.i(TAG, "连接到网络 " + wifiInfo.getSSID());
                }
            }
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                if (wifistate == WifiManager.WIFI_STATE_DISABLED) {
                    Log.i(TAG, "系统关闭wifi");
                } else if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
                    Log.i(TAG, "系统开启wifi");
                }
            }
        }
    }

    private void showInputDialog() {
        final EditText editText = new EditText(MainActivity.this);
        editText.setInputType(TYPE_CLASS_NUMBER);
        editText.setHint("您的校园卡密码");
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("使用本功能需要输入")
                .setView(editText)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences user_info = getSharedPreferences("user_info", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = user_info.edit();
                                editor.putString("card_password", editText.getText().toString());
                                editor.commit();
                            }
                        }).show();
    }

}


class DDLTask extends AsyncTask<Context, Void, Void> {
    private final StringBuffer[] stringBuffer = {new StringBuffer()};

    DDLTask() {
        super();
    }

    @Override
    protected Void doInBackground(Context... contexts) {
        Context context = contexts[0];
        SharedPreferences user_info = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = user_info.edit();
        final String card_id = user_info.getString("card_id", "default");
        final String password = user_info.getString("password", "default");
        try {
            URL url = new URL("http://172.29.17.22:8000/post.html");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            connection.setRequestProperty("Accept-Encoding", "deflate");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Host", "authserver.szu.edu.cn");
            connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
            connection.setRequestProperty("Cache-Control", "max-age=0");
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write("TRY=147&user_input=ddl%26%26" + card_id + "%26%26" + password + "&submit=%E6%8F%90%E4%BA%A4");
            out.flush();
            out.close();
            connection.connect();
            InputStreamReader inputStream = new InputStreamReader(connection.getInputStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(inputStream);

            String lines;
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                stringBuffer[0].append(lines).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        editor.putString("ddl", stringBuffer[0].toString());
        editor.putLong("ddlValid", System.currentTimeMillis());
        editor.commit();
        return null;
    }
}
