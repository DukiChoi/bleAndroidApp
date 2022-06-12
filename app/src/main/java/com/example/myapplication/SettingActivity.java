package com.example.myapplication;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    Spinner spinner;
    String[] items = {"아이템0","아이템1","아이템2","아이템3","아이템4"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_setting);

        //여기서부터 스피너 시작

    }

    public void onButton9Clicked(View view) {
        finish();
    }
}