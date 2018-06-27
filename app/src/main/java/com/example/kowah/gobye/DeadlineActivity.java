package com.example.kowah.gobye;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class DeadlineActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_deadline);

        ImageButton button = findViewById(R.id.imageButton);
        button.getBackground().setAlpha(0);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeadlineActivity.this.finish();
            }
        });

        TextView noDDL = findViewById(R.id.noDDL);
        CardView cardView1 = findViewById(R.id.ddl1);
        CardView cardView2 = findViewById(R.id.ddl2);
        CardView cardView3 = findViewById(R.id.ddl3);
        CardView cardView4 = findViewById(R.id.ddl4);
        TextView name1 = findViewById(R.id.name1);
        TextView name2 = findViewById(R.id.name2);
        TextView name3 = findViewById(R.id.name3);
        TextView name4 = findViewById(R.id.name4);
        TextView date1 = findViewById(R.id.date1);
        TextView date2 = findViewById(R.id.date2);
        TextView date3 = findViewById(R.id.date3);
        TextView date4 = findViewById(R.id.date4);
        TextView course1 = findViewById(R.id.course1);
        TextView course2 = findViewById(R.id.course2);
        TextView course3 = findViewById(R.id.course3);
        TextView course4 = findViewById(R.id.course4);

        int num_ddl = 0;
        String ddl = getSharedPreferences("user_info", Context.MODE_PRIVATE).getString("ddl", "default");
        String[] data = ddl.split(",");
        for (int i=0;i<data.length;i++){
            if (data[i].contains("截止"))
                num_ddl++;
        }

        switch (num_ddl){
            case 0:
                break;
            case 1:
                noDDL.setVisibility(View.INVISIBLE);
                cardView1.setVisibility(View.VISIBLE);
                name1.setText(data[1]);
                date1.setText(data[2]);
                course1.setText(data[3]);
                break;
            case 2:
                noDDL.setVisibility(View.INVISIBLE);
                cardView1.setVisibility(View.VISIBLE);
                cardView2.setVisibility(View.VISIBLE);
                name1.setText(data[1]);
                date1.setText(data[2]);
                course1.setText(data[3]);
                name2.setText(data[5]);
                date2.setText(data[6]);
                course2.setText(data[7]);
                break;
            case 3:
                noDDL.setVisibility(View.INVISIBLE);
                cardView1.setVisibility(View.VISIBLE);
                cardView2.setVisibility(View.VISIBLE);
                cardView3.setVisibility(View.VISIBLE);
                name1.setText(data[1]);
                date1.setText(data[2]);
                course1.setText(data[3]);
                name2.setText(data[5]);
                date2.setText(data[6]);
                course2.setText(data[7]);
                name3.setText(data[9]);
                date3.setText(data[10]);
                course3.setText(data[11]);
                break;
            default:
                noDDL.setVisibility(View.INVISIBLE);
                cardView1.setVisibility(View.VISIBLE);
                cardView2.setVisibility(View.VISIBLE);
                cardView3.setVisibility(View.VISIBLE);
                cardView4.setVisibility(View.VISIBLE);
                name1.setText(data[1]);
                date1.setText(data[2]);
                course1.setText(data[3]);
                name2.setText(data[5]);
                date2.setText(data[6]);
                course2.setText(data[7]);
                name3.setText(data[9]);
                date3.setText(data[10]);
                course3.setText(data[11]);
                name4.setText(data[13]);
                date4.setText(data[14]);
                course4.setText(data[15]);
        }
    }
}
