package com.catsruletheworld.tmapyourguard;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.app.Activity;
import android.location.Location;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        TMapView tMapView = new TMapView(this);

        tMapView.setSKTMapApiKey( "l7xxc3d916cc0e294c3eb19d0a4c60c0a212" );
        linearLayoutTmap.addView( tMapView );

        tMapView.setCenterPoint( 126.941072, 37.556415);

        final ArrayList alTMapPoint = new ArrayList();
        alTMapPoint.add( new TMapPoint(37.5551121, 126.9368882) );
        alTMapPoint.add( new TMapPoint(37.5551118, 126.9371794) );
        alTMapPoint.add( new TMapPoint(37.5551658, 126.9374828) );
        alTMapPoint.add( new TMapPoint(37.5553124, 126.9379203) );
        alTMapPoint.add( new TMapPoint(37.5556549, 126.9389425) );
        alTMapPoint.add( new TMapPoint(37.556027, 126.9400532) );
        alTMapPoint.add( new TMapPoint(37.5563867, 126.9411265) );
        alTMapPoint.add( new TMapPoint(37.5565275, 126.9428987) );
        alTMapPoint.add( new TMapPoint(37.5565991, 126.9440736) );
        alTMapPoint.add( new TMapPoint(37.5566847, 126.9454797) );
        alTMapPoint.add( new TMapPoint(37.5567077, 126.9458567) );

        //final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pin_r_m_a);

        for(int i=0; i<alTMapPoint.size(); i++){
            TMapMarkerItem markerItem1 = new TMapMarkerItem();
            // 마커 아이콘 지정
            //markerItem1.setIcon(bitmap);
            // 마커의 좌표 지정
            markerItem1.setTMapPoint((TMapPoint) alTMapPoint.get(i));
            //지도에 마커 추가
            tMapView.addMarkerItem("markerItem"+i, markerItem1);
        }
    }
}
