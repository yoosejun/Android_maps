package com.example.maps;

import android.Manifest;
import android.content.Intent;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Address;
import android.location.Geocoder;

import com.google.gson.Gson;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CacheRequest;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private NaverMap naverMap;
    private Spinner map;
    private ArrayAdapter arrayAdapter;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private FusedLocationSource locationSource;
    private InfoWindow infoWindow = new InfoWindow();
    private Geocoder geocoder;
    private Marker long_marker;
    private URL url;
    private HttpURLConnection conn;
    private StringBuilder urlBuilder;

    private EditText editTextAddress;

    NaverAddrApi naverAddrApi;
    NaverAddrGeo naverAddrGeo;

    //String items = resource.getStringArray(R.array.map);
    String[] maplist = {"Basic", "Navi", "Hybrid", "Satellite", "Terrain"};
    private int count;
    private int long_count;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);
        locationSource =
                new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    @UiThread
    public void onMapReady(@NonNull NaverMap naverMap) {

        this.naverMap = naverMap;
        Marker usermarker = new Marker();
        long_marker = new Marker();
        usermarker.setPosition(new LatLng(35.975000, 126.624000));
        usermarker.setMap(naverMap);
        naverMap.setMapType(NaverMap.MapType.Satellite);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        ActivityCompat.requestPermissions(this, PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);

        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(false);

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);

        PolygonOverlay marker_polygon = new PolygonOverlay();

        final String[] items = getResources().getStringArray(R.array.sp_map);


        map = (Spinner) findViewById(R.id.sp_map);
        arrayAdapter = ArrayAdapter.createFromResource(this, R.array.sp_map, android.R.layout.simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        map.setAdapter(arrayAdapter);

        map.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (items[position].equals("Basic"))
                    naverMap.setMapType(NaverMap.MapType.Basic);

                if (items[position].equals("Navi"))
                    naverMap.setMapType(NaverMap.MapType.Navi);

                if (items[position].equals("Satellite"))
                    naverMap.setMapType(NaverMap.MapType.Satellite);

                if (items[position].equals("Hybrid"))
                    naverMap.setMapType(NaverMap.MapType.Hybrid);

                if (items[position].equals("Terrian"))
                    naverMap.setMapType(NaverMap.MapType.Terrain);

                if (items[position].equals("None"))
                    naverMap.setMapType(NaverMap.MapType.None);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, true);
                } else {
                    naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, false);
                }
            }
        });

        Marker marker1 = new Marker();
        Marker marker2 = new Marker();
        Marker marker3 = new Marker();

        marker1.setPosition(new LatLng(35.975323, 126.624492));
        marker2.setPosition(new LatLng(35.966045, 126.737308));
        marker3.setPosition(new LatLng(35.947055, 126.681484));
        marker1.setMap(naverMap);
        marker2.setMap(naverMap);
        marker3.setMap(naverMap);
        marker1.setWidth(50);
        marker1.setHeight(80);
        marker2.setWidth(50);
        marker2.setHeight(80);
        marker3.setWidth(50);
        marker3.setHeight(80);

        PolygonOverlay polygon = new PolygonOverlay();
        polygon.setCoords(Arrays.asList(
                new LatLng(35.975323, 126.624492),
                new LatLng(35.966045, 126.737308),
                new LatLng(35.947055, 126.681484)
        ));
        polygon.setMap(naverMap);
        polygon.setColor(Color.argb(80, 255, 0, 0));

        List<Marker> markers = new ArrayList<>();
        List<Marker> long_markers = new ArrayList<>();
        List<LatLng> coords = new ArrayList<>();

        Handler handler = new Handler(Looper.getMainLooper());

        LocationOverlay locationOverlay = naverMap.getLocationOverlay();
        locationOverlay.setVisible(true);
        locationOverlay.setIcon(OverlayImage.fromResource(R.drawable.location_overlay_icon));
        locationOverlay.setIconWidth(LocationOverlay.SIZE_AUTO);
        locationOverlay.setIconHeight(LocationOverlay.SIZE_AUTO);

        InfoWindow infoWindow = new InfoWindow();

        naverMap.setOnMapClickListener((coord, point) -> {
            infoWindow.close();
        });

        Overlay.OnClickListener listener = overlay -> {
            Marker long_marker = (Marker) overlay;

            if (long_marker.getInfoWindow() == null) {
                // 현재 마커에 정보 창이 열려있지 않을 경우 엶
                infoWindow.open(long_marker);
            } else {
                // 이미 현재 마커에 정보 창이 열려있을 경우 닫음
                infoWindow.close();
            }
            return true;
        };

        naverMap.setOnMapClickListener((point, coord) -> {
            count += 1;
            Marker marker = new Marker();
            marker.setPosition(coord);
            marker.setCaptionText("marker");
            marker.setHeight(80);
            marker.setWidth(50);
            Toast.makeText(this,
                    "위도: " + coord.latitude + "\n" + "경도: " + coord.longitude,
                    Toast.LENGTH_SHORT).show();
            markers.add(marker);
            marker.setMap(naverMap);

            coords.add(coord);

            if (count > 2) {
                marker_polygon.setCoords(coords);
                marker_polygon.setMap(naverMap);
                marker_polygon.setColor(Color.argb(80, 0, 0, 255));

            }

        });

        naverMap.setOnMapLongClickListener(new NaverMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull @org.jetbrains.annotations.NotNull PointF pointF, @NonNull @org.jetbrains.annotations.NotNull LatLng latLng) {
                long_count += 1;
                long_marker.setPosition(latLng);
                long_marker.setCaptionText("long_marker");
                long_marker.setHeight(80);
                long_marker.setWidth(50);
                long_markers.add(long_marker);
                long_marker.setMap(naverMap);

                naverAddrApi = new NaverAddrApi(MainActivity.this);
                naverAddrApi.execute(latLng);
            }
        });



        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.post(() -> {
                    // 메인 스레드
                    for (Marker marker : markers) {
                        marker.setMap(null);
                    }
                    for (Marker long_marker : long_markers) {
                        long_marker.setMap(null);
                    }
                    marker1.setMap(null);
                    marker2.setMap(null);
                    marker3.setMap(null);
                    polygon.setMap(null);
                    marker_polygon.setMap(null);
                    infoWindow.close();
                });
            }
        });

        Button button2 = (Button) findViewById(R.id.button2);
        //Button button3 = (Button) findViewById(R.id.button3);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(new LatLng(35.975323, 126.624492))
                        .include(new LatLng(35.966045, 126.737308))
                        .include(new LatLng(35.947055, 126.681484))
                        .build();

                CameraUpdate cameraUpdate = CameraUpdate.fitBounds(bounds);
                naverMap.moveCamera(cameraUpdate);
            }
        });

//        button3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), ViewAddr.class);
//                startActivity(intent);
//            }
//        });

        Button button5 = (Button) findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextAddress = findViewById(R.id.editTextAddress);
                String Address = editTextAddress.getText().toString();
                naverAddrGeo = new NaverAddrGeo(MainActivity.this);
                naverAddrGeo.execute(Address);
            }
        });

    }

    public void viewAddress(String strAddr) {
        infoWindow.open(long_marker);
        //TODO 받아온 주소값을 infowindow에 출력하는 코드 작성
        infoWindow.setAdapter(new InfoWindow.DefaultViewAdapter(this) {
            @NonNull
            @Override
            protected View getContentView(@NonNull InfoWindow infoWindow) {
                Marker long_marker = infoWindow.getMarker();
                View view = View.inflate(MainActivity.this, R.layout.view_info_window, null);
                ((TextView) view.findViewById(R.id.title)).setText("지번 주소");
                ((TextView) view.findViewById(R.id.details)).setText(strAddr);
                return view;

            }
        });

    }

    public void viewMarker(String Address){

        String[] splitAdr = Address.split(",");
        double split_latitude = Double.parseDouble(splitAdr[1]);
        double split_longitude = Double.parseDouble(splitAdr[0]);

        LatLng targetLatLng;
        targetLatLng = new LatLng(split_latitude, split_longitude);

        //Log.d("myLog","value : " + targetLatLng);

        Marker latlng_marker = new Marker();
        latlng_marker.setPosition(targetLatLng);
        latlng_marker.setMap(naverMap);
        latlng_marker.setHeight(80);
        latlng_marker.setWidth(50);
        latlng_marker.setCaptionText("Destination");

        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(targetLatLng);
        naverMap.moveCamera(cameraUpdate);

        latlng_marker.setOnClickListener(overlay -> {
            latlng_marker.setMap(null);
            return true;
        });
    }
}