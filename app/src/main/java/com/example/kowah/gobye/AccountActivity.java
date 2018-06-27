package com.example.kowah.gobye;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_account);

        ImageButton button = findViewById(R.id.imageButton);
        button.getBackground().setAlpha(0);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountActivity.this.finish();
            }
        });

        SharedPreferences user_info = getSharedPreferences("user_info", 0);
        String card_id = user_info.getString("card_id", "default");
        String stu_id = user_info.getString("stu_id", "default");
        String name = user_info.getString("name", "default");
        String college = user_info.getString("college", "default");

        TextView stuid = findViewById(R.id.stuid);
        stuid.setText("\n姓名：" + stu_id);
        TextView cardid = findViewById(R.id.cardid);
        cardid.setText("\n学号：" + card_id);
        TextView n = findViewById(R.id.name);
        n.setText("\n学院：" + name);
        TextView c = findViewById(R.id.college);
        c.setText("\n校园卡号：" + college);

        Button change_account = findViewById(R.id.button);
        change_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

    }
}
