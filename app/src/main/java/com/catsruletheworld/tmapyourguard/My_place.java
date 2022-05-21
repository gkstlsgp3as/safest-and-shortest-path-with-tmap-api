package com.catsruletheworld.tmapyourguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class My_place extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_place);

        this.initSidebar();
    }

    public void initSidebar() {
        NavigationView navigationView = findViewById(R.id.navigaionview);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowCustomEnabled(true); //커스터마이징
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //메뉴 버튼 생성
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.morphing_icon_close);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch(item.getItemId()){
                    case R.id.menu_map:
                        intent = new Intent(My_place.this, MainActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.menu_setting:
                        intent = new Intent(My_place.this, Setting_activity.class);
                        startActivity(intent);
                        break;
                    case R.id.menu_help:
                        intent = new Intent(My_place.this, Help.class);
                        startActivity(intent);
                        break;
                    case R.id.menu_myplace:
                        intent = new Intent(My_place.this, My_place.class);
                        startActivity(intent);
                        break;
                }
                drawerLayout.closeDrawer(navigationView);
                return true;
            }
        });
    }

    //메뉴 클릭시 사이드바 나타나기
    public boolean onOptionsItemSelected(MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        switch (item.getItemId()) {
            case android.R.id.home : {
                drawer.openDrawer(GravityCompat.START);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //뒤로가기시 사이드바 닫기
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}