package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutionException;

public class MainActivity2 extends AppCompatActivity {

    TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }

    public void onBackButton1Clicked(View view){
        finish();
    }
    public void onBackButton2Clicked(View view){
        String resultText = "[NULL]";
        textView2 = (TextView)findViewById(R.id.textView2);
        try{
            resultText = new Task().execute().get();
        } catch(InterruptedException e){
            e.printStackTrace();
        }catch(ExecutionException e){
            e.printStackTrace();
        }

        textView2.setText(resultText);
    }
}