package com.sifat.uberdriver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.sifat.Custom.CustomMapFragmment;
import com.sifat.Service.HireCallService;
import com.sifat.Utilities.LocationProvider;

import static com.sifat.Utilities.CommonUtilities.*;

public class MapsActivity extends ActionBarActivity implements
        OnMapReadyCallback,
        CustomMapFragmment.OnTouchListener,
        GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationProvider locationProvider;
    private UiSettings mUiSettings;
    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private Toolbar toolbar;
    private DrawerLayout dlMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        init();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        CustomMapFragmment customMapFragmment =
                (CustomMapFragmment) getFragmentManager().findFragmentById(R.id.map);
        customMapFragmment.setListener(this);
        customMapFragmment.getMapAsync(this);
    }

    private void init() {


        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_drawer);
        ab.setDisplayHomeAsUpEnabled(true);
        dlMenu = (DrawerLayout) findViewById(R.id.drawer);

        sharedpreferences = getSharedPref(this);
        editor = sharedpreferences.edit();
        editor.putBoolean("flag", true);
        editor.putBoolean("init", false);
        editor.commit();
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

        mUiSettings = mMap.getUiSettings();
        mapUiSetting(true);
        locationProvider = new LocationProvider(this, mMap, editor, sharedpreferences);
        locationProvider.getMyLocaton();

        Intent in = new Intent(MapsActivity.this, HireCallService.class);
        startService(in);

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

        boolean flag = sharedpreferences.getBoolean("flag", true);
        boolean initMarker = sharedpreferences.getBoolean("init", true);
        if (!flag && initMarker)
            locationProvider.finish();
    }

    @Override
    public void onCusTouchUp() {

    }

    @Override
    public void onCusTouchDown() {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
