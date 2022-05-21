package com.catsruletheworld.tmapyourguard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.poi_item.TMapPOIItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener, TMapGpsManager.onLocationChangedCallback {
    EditText startText;
    EditText endText;
    ImageButton search;
    Button safest;
    Button fastest;
    FloatingActionButton call;
    View PlaceLayout;
    View TmapLayout;

    List<DBfire> fireList = null;
    List<DBalarm> alarmList = null;
    List<DBpolice> policeList = null;

    private static final String TAG = "YourGuard";
    public static Context Context1;
    String[] point = new String[2];

    //for sftp connection
    private Session session = null;
    private Channel channel = null;
    private ChannelSftp channelSftp = null;

    TMapView tMapView;
    TMapData tMapData;
    TMapGpsManager tMapGps;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context1 = this;
        this.initSidebar();
        //new MainButton();
        try {
            fireList = DBload();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        addMarker(fireList);

        //tmap 띄우기
        tMapView = new TMapView(this);
        ConstraintLayout TmapLayout = (ConstraintLayout) findViewById(R.id.TmapLayout);
        tMapView.setSKTMapApiKey("l7xxc3d916cc0e294c3eb19d0a4c60c0a212");
        TmapLayout.addView(tMapView);
        tMapView.setCenterPoint(126.9413547, 37.5584009);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        tMapView.setTrackingMode(true); //화면 중심을 단말의 현재 위치로

        //gps 승인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        tMapGps = new TMapGpsManager(this);

        tMapGps.setMinTime(1000);
        tMapGps.setMinDistance(10);
        //tMapGps.setProvider(tMapGps.NETWORK_PROVIDER); //네트워크기반
        tMapGps.setProvider(tMapGps.GPS_PROVIDER); //위성기반
        tMapGps.OpenGps();

        //this.userLocation();

        //출발지&목적지 사용자입력, 찾기 버튼
        startText = (EditText) findViewById(R.id.startText);
        startText.setOnClickListener(this);
        endText = (EditText) findViewById(R.id.endText);
        endText.setOnClickListener(this);
        search = (ImageButton) findViewById(R.id.search);

        //사용자 입력 주소 -> 위경도 좌표
        search.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                final String depart = startText.getText().toString();
                final String dest = endText.getText().toString();
                TMapData tmapdata = new TMapData();

                tmapdata.findAllPOI(depart, new TMapData.FindAllPOIListenerCallback() {
                    @Override
                    public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
                        for (int i = 0; i < poiItem.size(); i++) {
                            TMapPOIItem item = poiItem.get(i);
                            point[0] = item.getPOIPoint().toString();
                            Log.d(TAG, "출발지: " + point[0]);
                        }
                    }
                });

                tmapdata.findAllPOI(dest, new TMapData.FindAllPOIListenerCallback() {
                    @Override
                    public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
                        for (int i = 0; i < poiItem.size(); i++) {
                            TMapPOIItem item = poiItem.get(i);
                            point[1] = item.getPOIPoint().toString();
                            Log.d(TAG, "목적지: " + point[1]);
                        }
                    }
                });

                String realPoint = String.join(",", point);
                Log.d(TAG, "보낼주소: " + realPoint);

                //Connect connect = new Connect();
                //connect.execute(realPoint);
            }
        });

        call = (FloatingActionButton) findViewById(R.id.call);
        call.setOnClickListener(this);
        call.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:112"));
                try {
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        safest = (Button) findViewById(R.id.safest);
        safest.setOnClickListener(this);
        fastest = (Button) findViewById(R.id.fastest);
        fastest.setOnClickListener(this);

    }

    public List<DBfire> DBload() throws SQLException {
        DBhelper dbHelper = new DBhelper(getApplicationContext());
        dbHelper.DBopen();

        fireList = dbHelper.getTableData();
        Log.e("test", String.valueOf(fireList.size()));

        dbHelper.close();
        return fireList;
    }

    public void addMarker(List<DBfire> fireList) {
        for (int i = 0; i < fireList.size(); i++){
            double lat = fireList.get(i).lat;
            double lon = fireList.get(i).lon;

            TMapPoint tMapPoint = new TMapPoint(lat, lon);
            Log.d("tmappoint", String.valueOf(tMapPoint)); //Lat 37.557698 Lon 126.939151

            TMapMarkerItem tMapMarkerItem = new TMapMarkerItem();
            tMapMarkerItem.setPosition(0.5f, 1.0f);
            tMapMarkerItem.setTMapPoint(tMapPoint);

            tMapView.addMarkerItem("fire " + i, tMapMarkerItem);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void userLocation() {
        Location location = null;
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        TMapPoint point = tMapGps.getLocation();

        /*
        1. 기존 경로의 꺾어지는 포인트마다 원그리기
        2. 사용자의 위치를 받아와서 원안에 들어가면 진동 알림

        1. 기존 경로의 꺾어지는 포인트와 사용자의 위치가 일정 거리로 가까워지면 진동 알림
         */
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onClick(View view) {
        TMapPoint tMapPoint;
        TextView route_distance = findViewById(R.id.route_info);
        double lat, lon, weight;

        String safest =
                "37.5584009, 126.9413547, 0.4, " +
                "37.558621, 126.9416095, 0.4, " +
                "37.5582135, 126.9416316, 0.0, " +
                "37.5582268, 126.9421708, 0.0, " +
                "37.5578629, 126.9421915, 0.0, " +
                "37.5578782, 126.9423849, 0.0, " +
                "37.5579048, 126.942828, 0.0, " +
                "37.5579233, 126.9434576, 0.0, " +
                "37.5579832, 126.9446402, 0.0, " +
                "37.5580056, 126.9451575, 0.0, " +
                "37.5580341, 126.9458156, 0.0, " +
                "37.5573445, 126.945881, 0.0, " +
                "37.5570999, 126.9458687, 0.0, " +
                "37.5568476, 126.9462521, 0.0, 671.42";
        String fastest =
                "37.5584009, 126.9413547, 0.4, " +
                "37.558621, 126.9416095, 0.4, " +
                "37.5584838, 126.9420978, 0.0, " +
                "37.5584094, 126.9421862, 0.4, " +
                "37.5584477, 126.9428002, 0.0, " +
                "37.5584777, 126.9434356, 0.0, " +
                "37.5583259, 126.943439, 0.0, " +
                "37.5583611, 126.9446118, 0.0, " +
                "37.5583961, 126.9457812, 0.0, " +
                "37.5580341, 126.9458156, 0.4, " +
                "37.5573445, 126.945881, 0.0, " +
                "37.5570999, 126.9458687, 0.0, " +
                "37.5568476, 126.9462521, 0.0, 653.136";
        String[] SstringArray = safest.split(",");
        String[] FstringArray = fastest.split(",");
        double[] SdoubleArray = Arrays.stream(SstringArray).mapToDouble(Double::parseDouble).toArray();
        double[] FdoubleArray = Arrays.stream(FstringArray).mapToDouble(Double::parseDouble).toArray();
        int Sdistance = (int) SdoubleArray[SdoubleArray.length - 1];
        int Fdistance = (int) FdoubleArray[FdoubleArray.length - 1];

        TMapPolyLine StMapPolyLine = new TMapPolyLine();
        TMapPolyLine FtMapPolyLine = new TMapPolyLine();

        for (int i = 6; i < SdoubleArray.length - 1; i += 3) {
            lat = SdoubleArray[i];
            lon = SdoubleArray[i + 1];
            weight = SdoubleArray[i + 2];

            tMapPoint = new TMapPoint(lat, lon);
            //범위 별로 수정필요!!!
            if (weight == 0.0) {
                StMapPolyLine.setLineColor(Color.RED);
                StMapPolyLine.setOutLineColor(Color.RED);
            } else {
                StMapPolyLine.setLineColor(Color.YELLOW);
                StMapPolyLine.setOutLineColor(Color.YELLOW);
            }
            StMapPolyLine.addLinePoint(tMapPoint);
        }

        for (int i = 9; i < FdoubleArray.length - 1; i += 3) {
            lat = FdoubleArray[i];
            lon = FdoubleArray[i + 1];
            weight = FdoubleArray[i + 2];

            tMapPoint = new TMapPoint(lat, lon);
            if (weight == 0.0) {
                FtMapPolyLine.setLineColor(Color.RED);
                FtMapPolyLine.setOutLineColor(Color.RED);
            } else {
                FtMapPolyLine.setLineColor(Color.YELLOW);
                FtMapPolyLine.setOutLineColor(Color.YELLOW);
            }
            FtMapPolyLine.addLinePoint(tMapPoint);
        }


        switch (view.getId()) {
            case R.id.safest:
                tMapView.removeAllTMapPolyLine();
                tMapView.addTMapPolyLine("SlineItem", StMapPolyLine);
                //route_distance.setBackgroundColor(getResources().getColor(R.color.littlewhite));
                route_distance.setBackgroundResource(R.drawable.round);
                route_distance.setText(String.valueOf(Math.round(Sdistance * 0.015)) + "분(" + String.valueOf(Sdistance) + "m)");
                break;
            case R.id.fastest:
                tMapView.removeAllTMapPolyLine();
                tMapView.addTMapPolyLine("FlineItem", FtMapPolyLine);
                route_distance.setBackgroundResource(R.drawable.round);
                route_distance.setText(String.valueOf(Math.round(Fdistance * 0.015)) + "분(" + String.valueOf(Fdistance) + "m)");
                break;
            case R.id.startText:
            case R.id.endText:
                PlaceLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.call:

                break;
        }
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
                switch (item.getItemId()) {
                    case R.id.menu_map:
                        intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.menu_setting:
                        intent = new Intent(MainActivity.this, Setting_activity.class);
                        startActivity(intent);
                        break;
                    case R.id.menu_help:
                        intent = new Intent(MainActivity.this, Help.class);
                        startActivity(intent);
                        break;
                    case R.id.menu_myplace:
                        intent = new Intent(MainActivity.this, My_place.class);
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
            case android.R.id.home: {
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

    @Override
    public void onLocationChange(Location location) {
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
    }
}