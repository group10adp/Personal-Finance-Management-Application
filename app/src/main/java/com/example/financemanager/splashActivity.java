package com.example.financemanager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class splashActivity extends AppCompatActivity {

    ImageView spImg;
    TextView spText;
    Animation top, bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Set navigation bar color
            getWindow().setNavigationBarColor(Color.parseColor("#121212"));

            // Set status bar color
            getWindow().setStatusBarColor(Color.parseColor("#121212"));
        }


        spImg = findViewById(R.id.spImg);
        spText = findViewById(R.id.spText);

        top = AnimationUtils.loadAnimation(this, R.anim.top);
        bottom = AnimationUtils.loadAnimation(this, R.anim.bottom);

        spImg.setAnimation(top);
        spText.setAnimation(bottom);

        Intent iHome = new Intent(splashActivity.this, Login.class);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(iHome);
                finish();
            }
        }, 2000);



    }
}