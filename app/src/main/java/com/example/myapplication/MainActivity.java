package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


// 메뉴 창
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onButton1Clicked(View view){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://hokeun.github.io/research/index.html"));
        startActivity(intent);
    }
    public void onButton2Clicked(View view){
        Intent intent = new Intent(this, MainActivity2.class);
        startActivity(intent);
    }
    public void onButton7Clicked(View view){
        Intent intent = new Intent(this, MainActivity3.class);
        startActivity(intent);
    }
    public void onButton10Clicked(View view){
        Intent intent = new Intent(this, MainActivity4.class);
        startActivity(intent);
    }
    public void onButton11Clicked(View view){
        Intent intent = new Intent(this, MainActivity5.class);
        startActivity(intent);
    }

    public void onButton13Clicked(View view){
        Intent intent = new Intent(this, MainActivity6.class);
        startActivity(intent);
    }
}