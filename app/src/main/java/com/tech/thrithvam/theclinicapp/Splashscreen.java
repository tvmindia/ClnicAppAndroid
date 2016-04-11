package com.tech.thrithvam.theclinicapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class Splashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        }
        else
        {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
         /*   ActionBar actionBar = getActionBar();
            actionBar.hide();*/
        }
        RelativeLayout bg=(RelativeLayout)findViewById(R.id.back);
        TransitionDrawable transition = (TransitionDrawable) bg.getBackground();
        transition.startTransition(3000);



        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent goHome = new Intent(Splashscreen.this, Login.class);
                goHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                goHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(goHome);
                finish();

            }
        }, 3000);
    }
}
