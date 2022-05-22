package com.catsruletheworld.tmapyourguard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaPlayer;
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

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private shakeDetector mShakeDetector;

    private MediaPlayer mediaPlayer;

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

        //흔들림 센서 감지
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new shakeDetector();
        mShakeDetector.setOnShakeListener(new shakeDetector.OnShakeListener() {
            public void onShake(int count) { //흔들림 감지 시 할 행동
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.siren);
                mediaPlayer.start();
            }
        });

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

        call = (FloatingActionButton) findViewById(R.id.call); //원래 위치: 위 25 오른쪽 16
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

        //alarmbell
        final ArrayList<TMapPoint> alarmArray = new ArrayList<>();
        alarmArray.add(new TMapPoint(37.570105, 126.951837));
        alarmArray.add(new TMapPoint(37.576305, 126.935463));

        //cctv
        final ArrayList<TMapPoint> cctvArray = new ArrayList<>();
        /*double[][] cctv =
                {{126.9448582, 37.54986684}, {126.9484722, 37.55502277}, {126.9484722, 37.55502277}, {126.9484722, 37.55502277}, {126.9484722, 37.55502277}, {126.9489428, 37.55258091}, {126.9489428, 37.55258091}, {126.9489428, 37.55258091}, {126.9444134, 37.55215946}, {126.9444134, 37.55215946}, {126.9444134, 37.55215946}, {126.9438474, 37.55460247}, {126.9440724, 37.55462357}, {126.9448582, 37.54986684}, {126.9448582, 37.54986684}, {126.9370193, 37.54985654}, {126.9442852, 37.54986492}, {126.948232, 37.54998078}, {126.9360617, 37.55003045}, {126.9531701, 37.55023582}, {126.9463115, 37.55059737}, {126.9535944, 37.55121226}, {126.9444053, 37.55160089}, {126.9444053, 37.55160089}, {126.9444053, 37.55160089}, {126.9444053, 37.55160089}, {126.9510321, 37.55188958}, {126.9510319, 37.5522114}, {126.9500158, 37.552215}, {126.9442131, 37.55226144}, {126.9442131, 37.55226144}, {126.9442131, 37.55226144}, {126.9442131, 37.55226144}, {126.9450024, 37.5523608}, {126.9464763, 37.55249403}, {126.9444716, 37.55250705}, {126.9450655, 37.55255181}, {126.9450655, 37.55255181}, {126.9450655, 37.55255181}, {126.9362537, 37.55257177}, {126.9508893, 37.55268216}, {126.9378797, 37.55285693}, {126.9378797, 37.55285693}, {126.9439346, 37.5528608}, {126.9505822, 37.55304558}, {126.949751, 37.55305202}, {126.9446815, 37.55332888}, {126.9446815, 37.55332888}, {126.9385486, 37.55335105}, {126.9377741, 37.5533943}, {126.9453563, 37.55340239}, {126.9453563, 37.55340239}, {126.9453563, 37.55340239}, {126.9453563, 37.55340239}, {126.9441436, 37.55354844}, {126.9487073, 37.55362999}, {126.9393326, 37.55363021}, {126.9446227, 37.55372326}, {126.9405128, 37.55387824}, {126.9390368, 37.55396687}, {126.944245, 37.55405313}, {126.9442432, 37.55405908}, {126.9442394, 37.55406057}, {126.9442394, 37.55406057}, {126.9442357, 37.55406206}, {126.9362711, 37.55408763}, {126.9362711, 37.55408763}, {126.9362711, 37.55408763}, {126.9362711, 37.55408763}, {126.9362711, 37.55408763}, {126.9499808, 37.55413044}, {126.9531863, 37.55420834}, {126.9417709, 37.55434738}, {126.9402168, 37.5543494}, {126.9464619, 37.55435482}, {126.937643, 37.55439897}, {126.9473919, 37.55442305}, {126.9451841, 37.55448957}, {126.9408518, 37.5545055}, {126.9533366, 37.55454542}, {126.9533366, 37.55454542}, {126.9533366, 37.55454542}, {126.9533366, 37.55454542}, {126.9533366, 37.55454542}, {126.9476433, 37.5545948}, {126.94013, 37.55461466}, {126.9500367, 37.55470436}, {126.9428125, 37.5547738}, {126.9381923, 37.55482314}, {126.941598, 37.55485354}, {126.941598, 37.55485354}, {126.941598, 37.55485354}, {126.941598, 37.55485354}, {126.941598, 37.55485354}, {126.9362522, 37.55490566}, {126.9476161, 37.55504563}, {126.9425535, 37.55511542}, {126.9482358, 37.55513859}, {126.9391071, 37.55517609}, {126.9464301, 37.55518784}, {126.9480475, 37.55523907}, {126.9511296, 37.55524798}, {126.9511361, 37.55524873}, {126.9511315, 37.55525021}, {126.9511333, 37.55525245}, {126.9511315, 37.55525319}, {126.94771, 37.55527349}, {126.9470606, 37.55532269}, {126.9470606, 37.55532269}, {126.9409344, 37.5553498}, {126.9504758, 37.55545499}, {126.941466, 37.55550599}, {126.9524083, 37.55550903}, {126.9428078, 37.55559836}, {126.9472912, 37.55565078}, {126.9484994, 37.55570405}, {126.9403409, 37.55575322}, {126.9478492, 37.5557735}, {126.9493034, 37.55577592}, {126.9411806, 37.55586935}, {126.9411806, 37.55586935}, {126.9411806, 37.55586935}, {126.9411806, 37.55586935}, {126.9411806, 37.55586935}, {126.9462503, 37.55590214}, {126.9466345, 37.55591966}, {126.9466345, 37.55591966}, {126.9520565, 37.55605242}, {126.9520565, 37.55605242}, {126.9539328, 37.55611128}, {126.9507494, 37.55612428}, {126.9513299, 37.55615581}, {126.9452733, 37.55626638}, {126.946422, 37.55630388}, {126.946422, 37.55630388}, {126.9469801, 37.55631876}, {126.9532425, 37.55636988}, {126.9484833, 37.55641839}, {126.9481074, 37.55644548}, {126.9481074, 37.55644548}, {126.9481074, 37.55644548}, {126.9481074, 37.55644548}, {126.9498834, 37.55646362}, {126.9468237, 37.55650382}, {126.9526275, 37.55661522}, {126.9526275, 37.55661522}, {126.9516674, 37.5566689}, {126.9516674, 37.5566689}, {126.9533185, 37.55668014}, {126.9533185, 37.55668014}, {126.9533185, 37.55668014}, {126.9519412, 37.55679186}, {126.9532493, 37.55700458}, {126.9532493, 37.55700458}, {126.9543566, 37.55707159}, {126.9543566, 37.55707159}, {126.9543566, 37.55707159}, {126.9543566, 37.55707159}, {126.9543566, 37.55707159}, {126.939151, 37.557698}, {126.9457151, 37.5574763}, {126.9520408, 37.55764538}, {126.9520408, 37.55764538}, {126.9405856, 37.55794391}, {126.9402314, 37.55794867}, {126.952262, 37.5581842}, {126.952262, 37.5581842}, {126.952262, 37.5581842}, {126.952262, 37.5581842}, {126.952262, 37.5581842}, {126.952262, 37.5581842}, {126.952262, 37.5581842}, {126.952262, 37.5581842}, {126.952262, 37.5581842}, {126.952262, 37.5581842}, {126.952262, 37.5581842}, {126.9402737, 37.55834111}, {126.9402737, 37.55834111}, {126.9402737, 37.55834111}, {126.951723, 37.5584407}, {126.951723, 37.5584407}, {126.951723, 37.5584407}, {126.951723, 37.5584407}, {126.951723, 37.5584407}, {126.951723, 37.5584407}, {126.951723, 37.5584407}, {126.951723, 37.5584407}, {126.951723, 37.5584407}, {126.9481188, 37.5587808}, {126.9481188, 37.5587808}, {126.9481188, 37.5587808}, {126.9481188, 37.5587808}, {126.9481188, 37.5587808}, {126.9481188, 37.5587808}, {126.9369872, 37.55579603}, {126.9375422, 37.55598695}, {126.9386039, 37.55630715}, {126.9382258, 37.5569452}, {126.9395964, 37.55695796}, {126.9395964, 37.55695796}, {126.9462414, 37.5573514}, {126.949804, 37.557359}, {126.940184, 37.557377}, {126.9370343, 37.55742834}, {126.9412787, 37.55749678}, {126.9435349, 37.55757008}, {126.9392387, 37.55766903}, {126.9543404, 37.55779565}, {126.9435539, 37.55785713}, {126.9400823, 37.55809041}, {126.9413321, 37.55837633}, {126.942163, 37.558381}, {126.9447367, 37.55854179}, {126.9416371, 37.5585822}, {126.9403407, 37.55862178}, {126.9379602, 37.55873216}, {126.9445116, 37.55877914}, {126.9377752, 37.55724283}, {126.9510176, 37.55726175}, {126.9487861, 37.55749758}, {126.9429593, 37.55750393}, {126.9375606, 37.55754296}, {126.9362471, 37.55772709}, {126.9387583, 37.55785377}, {126.9456862, 37.55820211}, {126.9396954, 37.55859254}, {126.9387498, 37.55867591}, {126.9531095, 37.55884186}, {126.9472766, 37.55884884}, {126.9362482, 37.55890783}, {126.9410465, 37.5590106}, {126.9465711, 37.55882758}};
        for(int i = 0; i < 232; i++) {
            array.add(new TMapPoint(cctv[i][0], cctv[i][1]));
        }*/
        cctvArray.add(new TMapPoint(37.5574763, 126.9457151));
        cctvArray.add(new TMapPoint(37.55749678, 126.9412787));
        cctvArray.add(new TMapPoint(37.55757008, 126.9435349));
        cctvArray.add(new TMapPoint(37.55785713, 126.9435539));
        cctvArray.add(new TMapPoint(37.55837633, 126.9413321));
        cctvArray.add(new TMapPoint(37.558381, 126.942163));
        cctvArray.add(new TMapPoint(37.55854179, 126.9447367));
        cctvArray.add(new TMapPoint(37.5585822, 126.9416371));
        cctvArray.add(new TMapPoint(37.55750393, 126.9429593));
        cctvArray.add(new TMapPoint(37.55820211, 126.9456862));

        //fire station
        final ArrayList<TMapPoint> fireArray = new ArrayList<>();
        fireArray.add(new TMapPoint(37.5828688, 126.9356676));

        //police office
        final ArrayList<TMapPoint> policeArray = new ArrayList<>();
        policeArray.add(new TMapPoint(37.5624604342332, 126.955375056013));
        policeArray.add(new TMapPoint(37.5555698915366, 126.937644406338));
        policeArray.add(new TMapPoint(37.5759828, 126.9240639));
        policeArray.add(new TMapPoint(37.5830066,126.9128523));
        policeArray.add(new TMapPoint(37.5649018,126.9667851));
        policeArray.add(new TMapPoint(37.5585522,126.9430126));
        policeArray.add(new TMapPoint(37.5702587,126.9328507));
        policeArray.add(new TMapPoint(37.562275,126.9638375));
        policeArray.add(new TMapPoint(37.583615,126.936213));
        policeArray.add(new TMapPoint(37.595257,126.946388));
        policeArray.add(new TMapPoint(37.5881789,126.9445502));

        Bitmap alarmBitmap = BitmapFactory.decodeResource(Context1.getResources(),R.drawable.marker_blue);
        Bitmap cctvBitmap = BitmapFactory.decodeResource(Context1.getResources(),R.drawable.marker_yellow);
        Bitmap fireBitmap = BitmapFactory.decodeResource(Context1.getResources(),R.drawable.marker_green);
        Bitmap policeBitmap = BitmapFactory.decodeResource(Context1.getResources(),R.drawable.marker_orange);

        for(int i = 0; i < policeArray.size(); i++) {
            TMapMarkerItem alarmMarker = new TMapMarkerItem();
            TMapMarkerItem cctvMarker = new TMapMarkerItem();
            TMapMarkerItem fireMarker = new TMapMarkerItem();
            TMapMarkerItem policeMarker = new TMapMarkerItem();
            alarmMarker.setIcon(alarmBitmap);
            cctvMarker.setIcon(cctvBitmap);
            fireMarker.setIcon(fireBitmap);
            policeMarker.setIcon(policeBitmap);
            alarmMarker.setTMapPoint(alarmArray.get(i));
            cctvMarker.setTMapPoint(cctvArray.get(i));
            fireMarker.setTMapPoint(fireArray.get(0));
            policeMarker.setTMapPoint(policeArray.get(i));
            tMapView.addMarkerItem("alarm" + i, alarmMarker);
            tMapView.addMarkerItem("cctv" + i, cctvMarker);
            tMapView.addMarkerItem("fire" + i, fireMarker);
            tMapView.addMarkerItem("police" + i, policeMarker);
        }

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
            tMapMarkerItem.setTMapPoint(tMapPoint);

            //tMapView.removeAllMarkerItem();
            //tMapView.addMarkerItem("소방서 " + i, tMapMarkerItem);
        }
    }

    //운동 가속 감지
    public static class shakeDetector implements SensorEventListener {
        private static final float shakeGravity = 2.7F; //흔들림 감지할 때 기준이 되는 가해지는 힘
        private static final int shakeTime = 500; //흔들림 감지할 때 기준이 되는 시간 (ms 단위)
        private static final int shakeNum = 3000; //흔드는 횟수 3초마다 초기화
        private OnShakeListener mListener;
        private long shakeTimeStamp;
        private int shakeCount;

        public void setOnShakeListener(OnShakeListener listener) {
            this.mListener = listener;
        }

        public interface OnShakeListener {
            public void onShake(int count);
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (mListener != null) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                float gX = x / SensorManager.GRAVITY_EARTH; //중력 가속도 값으로 나눔
                float gY = y / SensorManager.GRAVITY_EARTH;
                float gZ = z / SensorManager.GRAVITY_EARTH;

                //gF = 중력가속도를 포함하는 물체가 받는 힘
                //gF = 1 평소 중력 (움직임 x), <= 1 아래로 떨어질 때, >= 1 위로 올라갈 때
                float gF = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);
                if(gF > shakeGravity) { //힘이 기준치 이상일 때 (진동 감지했을 때)
                    final long current = System.currentTimeMillis();
                    if(shakeTimeStamp + shakeTime > current) {
                        return; //진동이 너무 짧으면 무시
                    }
                    if(shakeTimeStamp + shakeNum < current) {
                        shakeCount = 0; // 3초 이싱 걸리면 리셋
                    }
                    shakeTimeStamp = current;
                    shakeCount++;
                    mListener.onShake(shakeCount); //흔들림 감지 시 실행될 행동
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) { // 사용 x
        }
    }

    //리스너 등록
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //리스너 해제 (배터리 소모 줄이기 위해)
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mShakeDetector);
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

        /*
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
         */

        String fastest = "37.5584009, 126.9413547, 0.4, 37.558621, 126.9416095, 0.4, 37.5584838, 126.9420978, 0.0, 37.5584094, 126.9421862, 0.4, 37.5584477, 126.9428002, 0.0, 37.5584777, 126.9434356, 0.0, 37.5583259, 126.943439, 0.0, 37.5583611, 126.9446118, 0.0, 37.5583961, 126.9457812, 0.0, 37.5580341, 126.9458156, 0.4, 37.5573445, 126.945881, 0.0, 37.5570999, 126.9458687, 0.0, 37.5568247, 126.945855, 0.0, 639.837";
        String safest = "37.5584009, 126.9413547, 0.4, 37.558621, 126.9416095, 0.4, 37.5582135, 126.9416316, 0.0, 37.5582268, 126.9421708, 0.0, 37.5578629, 126.9421915, 0.0, 37.5578782, 126.9423849, 0.0, 37.5579048, 126.942828, 0.0, 37.5579233, 126.9434576, 0.0, 37.5577346, 126.9434715, 0.0, 37.5575959, 126.9434817, 0.4, 37.5576573, 126.9446648, 0.0, 37.5572754, 126.9446937, 0.0, 37.5573054, 126.9452099, 0.0, 37.5573445, 126.945881, 0.0, 37.5570999, 126.9458687, 0.0, 37.5568247, 126.945855, 0.0, 661.221";
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