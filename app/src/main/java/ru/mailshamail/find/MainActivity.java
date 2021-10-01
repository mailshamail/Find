package ru.mailshamail.find;



import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


import java.sql.SQLException;
import java.util.ArrayList;


import ru.mailshamail.find.bd.BD;
import ru.mailshamail.find.service.MainGpsReciever;
import ru.mailshamail.find.service.testService;


public class MainActivity extends AppCompatActivity {

    MapView map = null;

    String gpsFilter = "GpsLocation";
    String name;
    private MainGpsReciever reciever;


    private int counts;
    private boolean isConnect;
    private addMarkers addMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SharedPreferences settings = getSharedPreferences("setting", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = settings.edit();
        final boolean IsOpenActivity = settings.getBoolean("visible", false);
        final boolean isPermission = settings.getBoolean("isPermission", false);

        name = settings.getString("name", "");

        if(name.equals("")) {
            if (!IsOpenActivity || !isPermission) {
                editor.putBoolean("visible", true);
                editor.putBoolean("isPermission", true);
                editor.apply();
                startActivity(new Intent(this, nameActivity.class));
            }
            startActivity(new Intent(this, nameActivity.class));
        }
        System.out.println(name + "/" + IsOpenActivity + "/" + isPermission);


        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);


        ImageButton myCoord = findViewById(R.id.myLocation2);
        ImageButton follow = findViewById(R.id.followme);
        ImageButton update = findViewById(R.id.updateButton);


        //Intent intent = getIntent();
        //name = intent.getStringExtra("name");


        init();

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.getTileProvider().createTileCache();


        GpsMyLocationProvider gps = new GpsMyLocationProvider(this);
        gps.addLocationSource(LocationManager.NETWORK_PROVIDER);
        gps.setLocationUpdateMinTime(1000 * 60 * 2);
        gps.setLocationUpdateMinDistance(100);


        final MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(gps,map);
        mLocationOverlay.enableMyLocation(gps);
        mLocationOverlay.enableFollowLocation();
        mLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                final GeoPoint myLocation = mLocationOverlay.getMyLocation();
                if (myLocation != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            map.getController().setZoom(17.1);
                            map.getController().animateTo(myLocation);

                        }
                    });
                }
            }
        });
        map.getOverlays().add(mLocationOverlay);


        myCoord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                mLocationOverlay.runOnFirstFix(new Runnable() {
                    final GeoPoint myLocation = mLocationOverlay.getMyLocation();
                    @Override
                    public void run() {

                        if (myLocation != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    map.getController().setZoom(17.1);
                                    map.getController().animateTo(myLocation);
                                    showToast(myLocation.getLatitude() + " / " + myLocation.getLongitude());

                                }
                            });
                           // addToTable(name, mLocationOverlay);
                        }
                    }
                });
            }
        });


        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(mLocationOverlay.isFollowLocationEnabled()) {
                    mLocationOverlay.disableFollowLocation();
                    showToast("Преследование выключено");
                }else{
                    mLocationOverlay.enableFollowLocation();
                    showToast("Преследование включено");
                }
            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                map.getOverlays().clear();
                s2();
                map.getOverlays().add(mLocationOverlay);
                map.invalidate();
                Toast.makeText(getBaseContext(), "update...", Toast.LENGTH_LONG).show();

            }
        });
    }

    private void s2()
    {
        final BD bd = new BD();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{

                    if(!isConnect)
                    {
                        bd.Connect();
                        isConnect = true;

                    }else if(!bd.Connect().isClosed())
                    {
                        counts = bd.getCounts();

                        for(int i = 0; i < counts; i ++){

                            addMarkers = new addMarkers(map,
                                    bd.getNameInBD().get(i),
                                    bd.getShirotaInBD().get(i),
                                    bd.getDolgotaInBD().get(i),
                                    bd.getTimeUpdate().get(i)
                            );
                            addMarkers.addMarker();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();


        s2();
    }

    @Override
    protected void onPause() {

        map.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        unregisterReceiver(reciever);
        Intent service = new Intent(this, testService.class).putExtra("name", name);

        startService(service);

        super.onDestroy();

    }

    private void showToast(String msg)
    {
        if (msg != null & this.getBaseContext() != null)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void init()
    {

        Intent service = new Intent(this, testService.class).putExtra("name", name);
        startService(service);
        //stopService(service);

        IntentFilter mainFilter = new IntentFilter(gpsFilter);
        reciever = new MainGpsReciever();
        registerReceiver(reciever, mainFilter);


    }

}
