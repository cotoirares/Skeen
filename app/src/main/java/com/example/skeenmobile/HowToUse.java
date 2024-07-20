package com.example.skeenmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

public class HowToUse extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_use);

        Toolbar toolbar = findViewById(R.id.toolbarHowToUse) ;
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cum se foloseste");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
}