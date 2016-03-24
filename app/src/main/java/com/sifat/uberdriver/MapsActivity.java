package com.sifat.uberdriver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.sifat.Controller.ServerCommunicator;
import com.sifat.Custom.CustomMapFragmment;
import com.sifat.Receiver.DriverLocationReciever;
import com.sifat.Service.DriverLocation;
import com.sifat.Service.HireCallService;
import com.sifat.Utilities.DriverLocationProvider;
import com.sifat.Utilities.LocationProvider;

import java.io.IOException;
import java.util.List;

import info.hoang8f.widget.FButton;

import static com.sifat.Utilities.CommonUtilities.*;

public class MapsActivity extends ActionBarActivity implements
        OnMapReadyCallback,
        CustomMapFragmment.OnTouchListener,
        GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMarkerClickListener,
        View.OnClickListener,
        SearchView.OnQueryTextListener,
        NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private LocationProvider locationProvider;
    private UiSettings mUiSettings;
    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private Toolbar toolbar;
    private DrawerLayout dlMenu;
    private FButton btStatus;
    private boolean isOnline;
    private SearchView searchView;
    private List<Address> addressList;
    private Geocoder geocoder;
    private NavigationView navView;
    private ServerCommunicator serverCommunicator;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;


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

        serverCommunicator = new ServerCommunicator(this);
        navView = (NavigationView) findViewById(R.id.navigation);
        navView.setNavigationItemSelectedListener(this);

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

        /*Intent intent= new Intent(MapsActivity.this, DriverLocation.class);
        startService(intent);*/

        /*Intent in = new Intent(MapsActivity.this, HireCallService.class);
        startService(in);*/
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
            cancelAlarmManager();
            setOfflineButton();
        } else {
            startAlarmManager();
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

    /*******
     * Starting Alarm Manager
     ******/
    private void startAlarmManager() {
        Log.i("Service", "startAlarmManager");
        Context context = getBaseContext();
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent driverLocIntent = new Intent(context, DriverLocationReciever.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, driverLocIntent, 0);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                1000 * 30, // 30 sec
                pendingIntent);
    }

    /*******
     * Stopping Alarm Manager
     ******/
    private void cancelAlarmManager() {
        Log.i("Service", "cancelAlarmManager");
        if(alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    /******
     * Menu Settings
     ****/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_navigation_drawer_items, menu);
        MenuItem item = menu.findItem(R.id.Search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint("Search Location");
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);
        return true;
    }


    /*******
     * Search bar Operation
     ******/
    @Override
    public boolean onQueryTextSubmit(String location) {
        if (location != null && location != "") {
            geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addressList.size() > 0) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                //mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            } else
                Toast.makeText(this, location + " not Found", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return menuNavigation(item,this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        showToast(this,"Item tapped");
        return menuNavigation(item,this);
    }

    private boolean menuNavigation(MenuItem item,Context context)
    {
        showToast(this,"Function called!");
        Intent intent;
        switch (item.getItemId()) {

            case android.R.id.home:
                dlMenu.openDrawer(GravityCompat.START);
                return super.onOptionsItemSelected(item);

            case R.id.Search:
                break;

            case R.id.navigation_item_profile:
                intent = new Intent(context,ProfileActivity.class);
                if(dlMenu.isDrawerOpen(GravityCompat.START))
                    dlMenu.closeDrawers();
                startActivity(intent);
                break;

            case R.id.navigation_item_logout:
                Logout(this);
                if(dlMenu.isDrawerOpen(GravityCompat.START))
                    dlMenu.closeDrawers();
                finish();
                break;

            case R.id.navigation_item_about:
                intent = new Intent(context,AboutActivity.class);
                if(dlMenu.isDrawerOpen(GravityCompat.START))
                    dlMenu.closeDrawers();
                startActivity(intent);
                break;

            case R.id.navigation_item_help:
                intent = new Intent(context,HelpActivity.class);
                if(dlMenu.isDrawerOpen(GravityCompat.START))
                    dlMenu.closeDrawers();
                startActivity(intent);
                break;

            case R.id.navigation_item_history:
                intent = new Intent(context,HistoryActivity.class);
                if(dlMenu.isDrawerOpen(GravityCompat.START))
                    dlMenu.closeDrawers();
                startActivity(intent);
                break;

            case R.id.navigation_item_settings:
                intent = new Intent(context,SettingsActivity.class);
                if(dlMenu.isDrawerOpen(GravityCompat.START))
                    dlMenu.closeDrawers();
                startActivity(intent);
                break;
        }
        return false;
    }
}
