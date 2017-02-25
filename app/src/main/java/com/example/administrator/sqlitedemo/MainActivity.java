package com.example.administrator.sqlitedemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
    }
    public void skip(View view){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this,otherActivity.class);
        startActivity(intent);
    }
}