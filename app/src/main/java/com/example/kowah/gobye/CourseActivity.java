package com.example.kowah.gobye;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;


public class CourseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_course);

        new DateTask().execute(CourseActivity.this);

        ImageButton button = findViewById(R.id.imageButton);
        button.getBackground().setAlpha(0);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CourseActivity.this.finish();
            }
        });

        SharedPreferences user_info = getSharedPreferences("user_info", 0);
        String today = user_info.getString("today","2000年1月1日 星期一 本学期第1周");
        String course = user_info.getString("course", "default");

        TextView date = findViewById(R.id.today);
        date.setText(today);

        WebView webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        webView.setWebViewClient(new WebViewClient());
        webView.loadData(course,"text/html","UTF-8");
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        settings.setLoadWithOverviewMode(true);
    }
}

class DateTask extends AsyncTask<Context, Void, Void> {
    private final StringBuffer[] stringBuffer = {new StringBuffer()};

    DateTask() {
        super();
    }

    @Override
    protected Void doInBackground(Context... contexts) {
        Context context = contexts[0];
        SharedPreferences user_info = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = user_info.edit();

        try {
            URL url = new URL("https://www1.szu.edu.cn/szu.asp");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty(":authority","www1.szu.edu.cn");
            connection.setRequestProperty(":method","GET");
            connection.setRequestProperty(":path","/szu.asp");
            connection.setRequestProperty(":scheme","https");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            connection.setRequestProperty("Accept-Encoding", "deflate");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("dnt","1");
            connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0(Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
            connection.setRequestProperty("Cache-Control", "max-age=0");
            connection.setRequestProperty("cookie","ASPSESSIONIDQASTCTRD=CMFPOMPAPEFFHAAGDMENAICH");
            connection.connect();
            InputStreamReader inputStream = new InputStreamReader(connection.getInputStream(), "gb2312");
            BufferedReader reader = new BufferedReader(inputStream);

            String lines;
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(),"utf-8");
                stringBuffer[0].append(lines).append("\n");
            }
            String s = stringBuffer[0].toString();
            int start = s.indexOf("<a href=\"http://www.szu.edu.cn/xxgk/xl.htm\" class=fontcolor1 title=\"查看校历\">")+74;
            int end = s.indexOf("(查看校历)");
            stringBuffer[0] = new StringBuffer(s.substring(start,end).replace("&nbsp;"," ").replace("\n",""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        editor.putString("today",stringBuffer[0].toString());
        editor.apply();
        return null;
    }
}

