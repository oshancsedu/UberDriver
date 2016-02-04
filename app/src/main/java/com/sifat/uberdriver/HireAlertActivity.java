package com.sifat.uberdriver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.github.polok.routedrawer.RouteDrawer;
import com.github.polok.routedrawer.RouteRest;
import com.github.polok.routedrawer.model.Routes;
import com.github.polok.routedrawer.model.TravelMode;
import com.github.polok.routedrawer.parser.RouteJsonParser;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.sifat.Custom.CustomMapFragmment;
import com.sifat.Utilities.LocationProvider;
import com.skyfishjy.library.RippleBackground;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.sifat.Utilities.CommonUtilities.*;

/**
 * Created by sifat on 2/3/2016.
 */
public class HireAlertActivity extends ActionBarActivity implements
        OnMapReadyCallback,
        CustomMapFragmment.OnTouchListener,
        GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMarkerClickListener, View.OnClickListener {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private LocationProvider locationProvider;
    private RippleBackground rippleBackground;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private Button btReject,btAccept;
    private TimeRemines timer;
    private TextView tvTimeRemains;
    private int sec;
    private Bundle latlngBundle;
    LatLng srcLatLng,distLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hire_alert);
        init();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        CustomMapFragmment customMapFragmment =
                (CustomMapFragmment) getFragmentManager().findFragmentById(R.id.map);
        customMapFragmment.setListener(this);
        customMapFragmment.getMapAsync(this);
    }

    private void init() {

        rippleBackground=(RippleBackground)findViewById(R.id.content);
        sharedPreferences = getSharedPref(this);
        editor = sharedPreferences.edit();
        editor.putBoolean("flag", true);
        editor.putBoolean("init", false);
        editor.commit();

        Intent in= getIntent();
        latlngBundle = in.getExtras();
        srcLatLng = latlngBundle.getParcelable(SRC_LATLNG);
        distLatLng = latlngBundle.getParcelable(DIST_LATLNG);

        tvTimeRemains = (TextView) findViewById(R.id.tvTimeRemain);
        btReject = (Button) findViewById(R.id.btReject);
        btAccept = (Button) findViewById(R.id.btAccept);
        btAccept.setOnClickListener(this);
        btReject.setOnClickListener(this);
        sec=15;

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
        wakeLock.acquire();

        timer = new TimeRemines(17000,1000);

        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

    }


    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnCameraChangeListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setOnMarkerClickListener(this);
        getRoute(srcLatLng, distLatLng);

        rippleBackground.startRippleAnimation();
        mUiSettings = mMap.getUiSettings();
        mapUiSetting(true);
        locationProvider = new LocationProvider(this, mMap, editor, sharedPreferences);
        locationProvider.getMyLocaton();
        timer.start();
    }

    //UI settings of map
    private void mapUiSetting(boolean flag) {
        mUiSettings.setZoomControlsEnabled(flag);
        mUiSettings.setCompassEnabled(flag);
        mUiSettings.setMyLocationButtonEnabled(flag);
        mUiSettings.setScrollGesturesEnabled(flag);
        mUiSettings.setZoomGesturesEnabled(flag);
        mUiSettings.setTiltGesturesEnabled(flag);
        mUiSettings.setRotateGesturesEnabled(flag);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCusTouchUp() {
        rippleBackground.startRippleAnimation();
    }

    @Override
    public void onCusTouchDown() {
        rippleBackground.stopRippleAnimation();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.btReject)
        {
            timer.cancel();
            finishTimer();
        }

        if(v.getId()==R.id.btAccept)
        {
            timer.cancel();
            finishTimer();
        }
    }

    private void finishTimer() {
        HireAlertActivity.this.finish();
        if(wakeLock.isHeld())
            wakeLock.release();
    }



    /********
     * Get A route Between source & destination
     ********/
    private void getRoute(LatLng srcLatLng, LatLng distLatLng) {
        final RouteDrawer routeDrawer = new RouteDrawer.RouteDrawerBuilder(mMap)
                .withColor(Color.BLUE)
                .withWidth(5)
                .withAlpha(0.0f)
                .withMarkerIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                .build();

        RouteRest routeRest = new RouteRest();
        routeRest.getJsonDirections(srcLatLng, distLatLng, TravelMode.DRIVING)
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<String, Routes>() {
                    @Override
                    public Routes call(String s) {
                        return new RouteJsonParser<Routes>().parse(s, Routes.class);
                    }
                })
                .subscribe(new Action1<Routes>() {
                    @Override
                    public void call(Routes r) {
                        routeDrawer.drawPath(r);
                    }
                });

    }

    private class TimeRemines extends CountDownTimer
    {

        public TimeRemines(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.e("Counter",""+sec);
            tvTimeRemains.setText("Time Remains: " + sec + "s");
            sec--;
        }

        @Override
        public void onFinish() {
            Log.e("Counter","Finish");
            finishTimer();
        }
    }
}
