package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ExampleActivities.BLEScanActivity;
import com.example.myapplication.ExampleActivities.BluetoothActivity;
import com.example.myapplication.ExampleActivities.GetHttpActivity;
import com.example.myapplication.ExampleActivities.TCPActivity;
import com.example.myapplication.Fragments.Peripheral;


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
        Intent intent = new Intent(this, GetHttpActivity.class);
        startActivity(intent);
    }
    public void onButton7Clicked(View view){
        Intent intent = new Intent(this, TCPActivity.class);
        startActivity(intent);
    }
    public void onButton10Clicked(View view){
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }
    public void onButton11Clicked(View view){
        Intent intent = new Intent(this, WarningActivity.class);
        startActivity(intent);
    }

    public void onButton13Clicked(View view){
        Intent intent = new Intent(this, BluetoothActivity.class);
        startActivity(intent);
    }

    public void onButton14Clicked(View view){
        Intent intent = new Intent(this, BLEScanActivity.class);
        startActivity(intent);
    }

    public void onButton15Clicked(View view){
        //Intent intent = new Intent(this, GattServerActivity.class);
        Intent intent = new Intent(this, Peripheral.class);
        startActivity(intent);
    }

}