package com.sifat.uberdriver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

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

import info.hoang8f.widget.FButton;

import static com.sifat.Utilities.CommonUtilities.*;

public class MapsActivity extends ActionBarActivity implements
        OnMapReadyCallback,
        CustomMapFragmment.OnTouchListener,
        GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMarkerClickListener, View.OnClickListener {

    private GoogleMap mMap;
    private LocationProvider locationProvider;
    private UiSettings mUiSettings;
    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private Toolbar toolbar;
    private DrawerLayout dlMenu;
    private FButton btStatus;
    private boolean isOnline;

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

    @Override
    protected void onResume() {
        super.onResume();

        isOnline = sharedpreferences.getBoolean(IS_ONLINE, false);
        setButtonStatus();
    }

    private void init() {

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_drawer);
        ab.setDisplayHomeAsUpEnabled(true);
        dlMenu = (DrawerLayout) findViewById(R.id.drawer);
        btStatus = (FButton) findViewById(R.id.btStatus);
        btStatus.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btStatus) {
            if (isOnline) {
                isOnline = false;
                editor.putBoolean(IS_ONLINE, isOnline);
                editor.commit();
            } else {
                isOnline = true;
                editor.putBoolean(IS_ONLINE, isOnline);
                editor.commit();
            }
            setButtonStatus();
        }
    }

    private void setButtonStatus() {
        if (isOnline) {
            setOfflineButton();
        } else {
            setOnlineButton();
        }
    }

    private void setOfflineButton() {
        btStatus.setButtonColor(this.getResources().getColor(R.color.red_500));
        btStatus.setShadowColor(this.getResources().getColor(R.color.red_900));
        btStatus.setText(this.getResources().getString(R.string.online));
    }

    private void setOnlineButton() {
        btStatus.setButtonColor(this.getResources().getColor(R.color.green_500));
        btStatus.setShadowColor(this.getResources().getColor(R.color.green_900));
        btStatus.setText(this.getResources().getString(R.string.offline));
    }
}
