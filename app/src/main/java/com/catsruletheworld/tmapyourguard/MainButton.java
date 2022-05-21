package com.catsruletheworld.tmapyourguard;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.skt.Tmap.TMapView;

class MainButton extends AppCompatActivity {
    public static Context Context2;
    TMapView tMapView;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context2 = this;

        //Button safest = ((MainActivity)MainActivity.Context1).safest;
        //Button fastest = ((MainActivity)MainActivity.Context1).fastest;

        Button safest = (Button) findViewById(R.id.safest);
        Button fastest = (Button) findViewById(R.id.fastest);


    }

}
