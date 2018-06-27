package com.example.kowah.gobye;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {
    Context mcontext = this;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        setContentView(R.layout.activity_login);

        ImageButton imageButton = findViewById(R.id.imageButton);
        imageButton.getBackground().setAlpha(0);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.finish();
            }
        });

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RESULT_FIRST_USER);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RESULT_FIRST_USER);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, RESULT_FIRST_USER);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"StringFormatInvalid", "StringFormatMatches"})
            @Override
            public void onClick(View v) {
                final ProgressDialog on = new ProgressDialog(LoginActivity.this);
                on.setMessage("正在登录...请稍等");
                on.show();

                EditText card = findViewById(R.id.card_id);
                String card_id = card.getText().toString();
                EditText pw = findViewById(R.id.password);
                String password = pw.getText().toString();

                if (card_id.equals("") || password.equals("")) {
                    on.dismiss();
                    new android.support.v7.app.AlertDialog.Builder(LoginActivity.this)
                            .setTitle("您还未完成输入")
                            .setMessage("请输入正确的信息")
                            .setPositiveButton("确定", null)
                            .show();
                } else {
                    boolean flag = attemptLogin(card_id, password);
                    if (!flag) {
                        on.dismiss();
                        new android.support.v7.app.AlertDialog.Builder(LoginActivity.this)
                                .setTitle("验证错误")
                                .setMessage("请确认您的账号密码是否正确")
                                .setPositiveButton("确定", null)
                                .show();
                    } else {
//                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
//                        Date date = new Date(System.currentTimeMillis());
//                        String expiry_date = simpleDateFormat.format(date);

                        SharedPreferences user_info = getSharedPreferences("user_info", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = user_info.edit();
                        editor.putString("card_id", card_id);
                        editor.putString("password", password);
//                        editor.putString("expiry_date",expiry_date);
                        editor.apply();

                        new LoginTask().execute(mcontext);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    }
                }
            }
        });

    }

    private boolean attemptLogin(final String un, final String pw) {
        final boolean[] loginSucessful = {false};
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> cookies = new HashMap<>();
                try {
                    URL url = new URL("https://authserver.szu.edu.cn/authserver/login");
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                    connection.setRequestProperty("Accept-Encoding", "deflate, br");
                    connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8");
                    connection.setRequestProperty("Connection", "keep-alive");
                    connection.setRequestProperty("Host", "authserver.szu.edu.cn");
                    connection.setRequestProperty("Referer", "https://authserver.szu.edu.cn/authserver/logout?service=/authserver/login");
                    connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");

                    connection.connect();
                    if (connection.getResponseCode() == 200) {
                        Log.d("hhh", "OK");
                    }

                    Map<String, List<String>> responseHeaderMap = connection.getHeaderFields();
                    int size = responseHeaderMap.size();
                    StringBuilder sbResponseHeader = new StringBuilder();
                    for (int i = 0; i < size; i++) {
                        String responseHeaderKey = connection.getHeaderFieldKey(i);
                        String responseHeaderValue = connection.getHeaderField(i);
                        sbResponseHeader.append(responseHeaderKey);
                        sbResponseHeader.append(":");
                        sbResponseHeader.append(responseHeaderValue);
                        sbResponseHeader.append("\n");
                    }
                    Log.d("hhh", sbResponseHeader.toString());

                    InputStreamReader inputStream = new InputStreamReader(connection.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(inputStream);
                    String lines;
                    StringBuffer stringBuffer = new StringBuffer("");
                    while ((lines = reader.readLine()) != null) {
                        lines = new String(lines.getBytes(), "utf-8");
                        //Log.d("hhh",lines);
                        stringBuffer.append(lines);
                    }
                    reader.close();

                    int start = stringBuffer.toString().indexOf("name=\"lt\" value=\"") + 17;
                    int end = stringBuffer.toString().indexOf("\"", start);
                    String lt = stringBuffer.toString().substring(start, end);
                    Log.d("hhh", lt);
                    start = stringBuffer.indexOf("execution\" value=\"") + 18;
                    end = stringBuffer.indexOf("\"", start);
                    String execution = stringBuffer.substring(start, end);
                    start = sbResponseHeader.indexOf("JSESSIONID_auth=") + 16;
                    end = sbResponseHeader.indexOf(";", start);
                    String JSESSIONID_auth = sbResponseHeader.substring(start, end);
                    Log.d("hhh", JSESSIONID_auth);
                    start = sbResponseHeader.indexOf("route=") + 6;
                    end = sbResponseHeader.indexOf("\n", start);
                    String route = sbResponseHeader.substring(start, end);
                    Log.d("hhh", route);
                    cookies.put("JSESSIONID_auth", JSESSIONID_auth);
                    cookies.put("route", route);
                    connection.disconnect();

                    //第二次请求
                    url = new URL("https://authserver.szu.edu.cn/authserver/login;JSESSIONID_auth=" + cookies.get("JSESSIONID_auth"));
                    connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestProperty("Cookie", "route=" + cookies.get("route") + "; JSESSIONID_auth=" + cookies.get("JSESSIONID_auth") + "; org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE=zh_CN");
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Cache-Control", "max-age=0");
                    connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                    connection.setRequestProperty("Accept-Encoding", "deflate, br");
                    connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8");
                    connection.setRequestProperty("Connection", "keep-alive");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Origin", "https://authserver.szu.edu.cn");
                    connection.setRequestProperty("Referer", "https://authserver.szu.edu.cn/authserver/login");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
                    OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                    out.write("username=" + un + "&password=" + pw + "&lt=" + lt + "&dllt=userNamePasswordLogin&execution=" + execution + "&_eventId=submit&rmShown=1");
                    out.flush();
                    out.close();
                    connection.connect();
                    if (connection.getResponseCode() == 302) {
                        Log.d("hhh", "OK2");
                    }
                    responseHeaderMap = connection.getHeaderFields();
                    size = responseHeaderMap.size();
                    sbResponseHeader = new StringBuilder();
                    for (int i = 0; i < size; i++) {
                        String responseHeaderKey = connection.getHeaderFieldKey(i);
                        String responseHeaderValue = connection.getHeaderField(i);
                        sbResponseHeader.append(responseHeaderKey);
                        sbResponseHeader.append(":");
                        sbResponseHeader.append(responseHeaderValue);
                        sbResponseHeader.append("\n");
                    }
                    Log.d("hhh", sbResponseHeader.toString());

                    start = sbResponseHeader.indexOf("CASTGC=") + 7;
                    end = sbResponseHeader.indexOf(";", start);
                    String CASTGC = sbResponseHeader.substring(start, end);
                    Log.d("hhh", CASTGC);
                    start = sbResponseHeader.indexOf("iPlanetDirectoryPro=") + 20;
                    end = sbResponseHeader.indexOf(";", start);
                    String iPlanetDirectoryPro = sbResponseHeader.substring(start, end);
                    Log.d("hhh", iPlanetDirectoryPro);
                    cookies.put("CASTGC", CASTGC);
                    cookies.put("iPlanetDirectoryPro", iPlanetDirectoryPro);
                    connection.disconnect();


                    //第三次请求
                    url = new URL("http://authserver.szu.edu.cn/authserver/index.do");
                    HttpURLConnection connection1 = (HttpURLConnection) url.openConnection();
                    connection1.setRequestProperty("Cookie", "CASTGC=" + cookies.get("CASTGC") + "; route=" + cookies.get("route") + "; JSESSIONID_auth=" + cookies.get("JSESSIONID_auth") + "; org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE=zh_CN");
                    connection1.setRequestMethod("GET");
                    connection1.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                    connection1.setRequestProperty("Accept-Encoding", "deflate");
                    connection1.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8");
                    connection1.setRequestProperty("Connection", "keep-alive");
                    connection1.setRequestProperty("Host", "authserver.szu.edu.cn");
                    connection1.setRequestProperty("Upgrade-Insecure-Requests", "1");
                    connection1.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
                    connection1.setRequestProperty("Cache-Control", "max-age=0");
                    connection1.connect();
                    if (connection1.getResponseCode() == 302) {
                        Log.d("hhh", "OK3");
                    }
                    responseHeaderMap = connection1.getHeaderFields();
                    size = responseHeaderMap.size();
                    sbResponseHeader = new StringBuilder();
                    for (int i = 0; i < size; i++) {
                        String responseHeaderKey = connection1.getHeaderFieldKey(i);
                        String responseHeaderValue = connection1.getHeaderField(i);
                        sbResponseHeader.append(responseHeaderKey);
                        sbResponseHeader.append(":");
                        sbResponseHeader.append(responseHeaderValue);
                        sbResponseHeader.append("\n");
                    }
                    Log.d("hhh", sbResponseHeader.toString());
                    start = sbResponseHeader.indexOf("insert_cookie=") + 14;
                    end = sbResponseHeader.indexOf(";", start);
                    String insert_cookie = sbResponseHeader.substring(start, end);
                    Log.d("hhh", insert_cookie);
                    cookies.put("insert_cookie", insert_cookie);
                    connection1.disconnect();

                    //第四次请求
                    url = new URL("https://authserver.szu.edu.cn/authserver/index.do");
                    connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestProperty("Cookie", "CASTGC=" + cookies.get("CASTGC") + "; route=" + cookies.get("route") + "; JSESSIONID_auth=" + cookies.get("JSESSIONID_auth") + "; org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE=zh_CN; insert_cookie=" + cookies.get("insert_cookie"));
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                    connection.setRequestProperty("Accept-Encoding", "deflate, br");
                    connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8");
                    connection.setRequestProperty("Connection", "keep-alive");
                    connection.setRequestProperty("Host", "authserver.szu.edu.cn");
                    connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
                    connection.setRequestProperty("Cache-Control", "max-age=0");
                    connection.connect();
                    if (connection.getResponseCode() == 302) {
                        Log.d("hhh", "OK4");
                    }
                    responseHeaderMap = connection.getHeaderFields();
                    size = responseHeaderMap.size();
                    sbResponseHeader = new StringBuilder();
                    for (int i = 0; i < size; i++) {
                        String responseHeaderKey = connection.getHeaderFieldKey(i);
                        String responseHeaderValue = connection.getHeaderField(i);
                        sbResponseHeader.append(responseHeaderKey);
                        sbResponseHeader.append(":");
                        sbResponseHeader.append(responseHeaderValue);
                        sbResponseHeader.append("\n");
                    }
                    Log.d("hhh", sbResponseHeader.toString());
                    connection.disconnect();

                    //第五次请求
                    url = new URL("http://authserver.szu.edu.cn/authserver/index.do");
                    connection1 = (HttpURLConnection) url.openConnection();
                    connection1.setRequestProperty("Cookie", "CASTGC=" + cookies.get("CASTGC") + "; route=" + cookies.get("route") + "; JSESSIONID_auth=" + cookies.get("JSESSIONID_auth") + "; org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE=zh_CN");
                    connection1.setRequestMethod("GET");
                    connection1.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                    connection1.setRequestProperty("Accept-Encoding", "deflate");
                    connection1.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8");
                    connection1.setRequestProperty("Connection", "keep-alive");
                    connection1.setRequestProperty("Host", "authserver.szu.edu.cn");
                    connection1.setRequestProperty("Upgrade-Insecure-Requests", "1");
                    connection1.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
                    connection1.setRequestProperty("Cache-Control", "max-age=0");
                    connection1.connect();
                    if (connection1.getResponseCode() == 302) {
                        Log.d("hhh", "OK5");
                    }
                    responseHeaderMap = connection1.getHeaderFields();
                    size = responseHeaderMap.size();
                    sbResponseHeader = new StringBuilder();
                    for (int i = 0; i < size; i++) {
                        String responseHeaderKey = connection1.getHeaderFieldKey(i);
                        String responseHeaderValue = connection1.getHeaderField(i);
                        sbResponseHeader.append(responseHeaderKey);
                        sbResponseHeader.append(":");
                        sbResponseHeader.append(responseHeaderValue);
                        sbResponseHeader.append("\n");
                    }
                    Log.d("hhh", sbResponseHeader.toString());
                    connection1.disconnect();

                    //第六次请求
                    url = new URL("https://authserver.szu.edu.cn/authserver/index.do");
                    connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestProperty("Cookie", "CASTGC=" + cookies.get("CASTGC") + "; route=" + cookies.get("route") + "; JSESSIONID_auth=" + cookies.get("JSESSIONID_auth") + "; org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE=zh_CN; insert_cookie=" + cookies.get("insert_cookie"));
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                    connection.setRequestProperty("Accept-Encoding", "deflate, br");
                    connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8");
                    connection.setRequestProperty("Connection", "keep-alive");
                    connection.setRequestProperty("Host", "authserver.szu.edu.cn");
                    connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
                    connection.setRequestProperty("Cache-Control", "max-age=0");
                    connection.connect();
                    if (connection.getResponseCode() == 200) {
                        Log.d("hhh", "OK6");
                    }
                    responseHeaderMap = connection.getHeaderFields();
                    size = responseHeaderMap.size();
                    sbResponseHeader = new StringBuilder();
                    for (int i = 0; i < size; i++) {
                        String responseHeaderKey = connection.getHeaderFieldKey(i);
                        String responseHeaderValue = connection.getHeaderField(i);
                        sbResponseHeader.append(responseHeaderKey);
                        sbResponseHeader.append(":");
                        sbResponseHeader.append(responseHeaderValue);
                        sbResponseHeader.append("\n");
                    }
                    Log.d("hhh", sbResponseHeader.toString());

                    inputStream = new InputStreamReader(connection.getInputStream(), "UTF-8");
                    reader = new BufferedReader(inputStream);
                    stringBuffer = new StringBuffer("");
                    while ((lines = reader.readLine()) != null) {
                        lines = new String(lines.getBytes(), "utf-8");
                        stringBuffer.append(lines).append("\n");
                    }

                    Log.d("666", stringBuffer.toString());
                    connection.disconnect();
                    loginSucessful[0] = stringBuffer.toString().contains("个人资料");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return loginSucessful[0];
    }

}


class LoginTask extends AsyncTask<Context, Void, Void> {
    private final StringBuffer[] stringBuffer = {new StringBuffer(), new StringBuffer(), new StringBuffer()};

    LoginTask() {
        super();
    }

    @Override
    protected Void doInBackground(Context... params) {
        Context context = params[0];
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
            out.write("TRY=147&user_input=info%26%26" + card_id + "%26%26" + password + "&submit=%E6%8F%90%E4%BA%A4");
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
        String[] info = stringBuffer[0].toString().trim().split(",");
        editor.putString("stu_id", info[0]);
        editor.putString("name", info[1]);
        editor.putString("college", info[2]);
        editor.apply();
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
            out.write("TRY=147&user_input=course%26%26" + card_id + "%26%26" + password + "&submit=%E6%8F%90%E4%BA%A4");
            out.flush();
            out.close();
            connection.connect();
            InputStreamReader inputStream = new InputStreamReader(connection.getInputStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(inputStream);

            String lines;
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                stringBuffer[1].append(lines).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        editor.putString("course", stringBuffer[1].toString());
        editor.apply();
        return null;
    }
}
