package com.example.priapo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class Main2Activity extends AppCompatActivity { //Activity 2 que se lanzará desde la MainActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Log.i("--nuevaActivity", "Activity 2");
    }
}
