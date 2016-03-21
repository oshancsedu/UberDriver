package com.sifat.uberdriver;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sifat.Controller.ServerCommunicator;
import com.sifat.Custom.CustomMapFragmment;
import com.sifat.Dialogues.UserRating;
import com.github.polok.routedrawer.RouteApi;
import com.github.polok.routedrawer.RouteDrawer;
import com.github.polok.routedrawer.RouteRest;
import com.github.polok.routedrawer.model.Routes;
import com.github.polok.routedrawer.model.TravelMode;
import com.github.polok.routedrawer.parser.RouteJsonParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.sifat.Utilities.CommonUtilities.*;

/**
 * Created by sifat on 2/5/2016.
 */
public class DriverTaxiStatus extends ActionBarActivity implements RouteApi,
        CustomMapFragmment.OnTouchListener,
        OnMapReadyCallback,
        UserRating.Communicator,
        View.OnClickListener {

    private GoogleMap gMap;
    private UiSettings uiSettings;
    private CameraPosition showMyLocation;
    private LatLng srcLatLng, distLatLng;
    private Bundle bundle;
    private Marker srcMarker, distMarker;
    private TextView tvUsername, tvUserMobile;
    private RatingBar rbDriverRate;
    private float rating,lat,lng;
    private Toolbar toolbar;
    private UserRating userRating;
    private String userName, userId;
    private FloatingActionButton fbReleaseTaxi;
    private NotificationManager mNotificationManager;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ServerCommunicator serverCommunicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onride);
        init();

        bundle = getIntent().getExtras();

        lat = Float.parseFloat(bundle.getString(SRC_LAT));
        lng = Float.parseFloat(bundle.getString(SRC_LNG));

        srcLatLng = new LatLng(lat,lng);

        lat = Float.parseFloat(bundle.getString(DIST_LAT));
        lng = Float.parseFloat(bundle.getString(DIST_LNG));

        distLatLng = new LatLng(lat,lng);

        userName = bundle.getString(SELECTED_USER_NAME);
        tvUsername.setText(userName);
        tvUserMobile.setText(bundle.getString(SELECTED_USER_MOBILE));
        rating = Float.parseFloat(bundle.getString(SELECTED_USER_RATING));
        userId = bundle.getString(SELECTED_USER_ID);

        rbDriverRate.setRating(rating + 0.5f);
        rbDriverRate.setRating(rating);

        CustomMapFragmment customMapFragmment =
                (CustomMapFragmment) getFragmentManager().findFragmentById(R.id.map);
        customMapFragmment.setListener(this);
        customMapFragmment.getMapAsync(this);
    }

    private void init() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        sharedPreferences = getSharedPref(this);
        editor = sharedPreferences.edit();

        serverCommunicator = new ServerCommunicator(this);

        tvUserMobile = (TextView) findViewById(R.id.tvDriverMobileNum);
        tvUsername = (TextView) findViewById(R.id.tvDriverName);
        rbDriverRate = (RatingBar) findViewById(R.id.rbDriverRate);
        userRating = new UserRating();
        fbReleaseTaxi = (FloatingActionButton) findViewById(R.id.fbReleaseTaxi);
        fbReleaseTaxi.setOnClickListener(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        uiSettings = gMap.getUiSettings();
        mapUiSetting(true);
        zoomToMyLocation();
        setSrcDistMarker();
        getRoute(srcLatLng, distLatLng);
    }

    private void setSrcDistMarker() {
        srcMarker = gMap.addMarker(new MarkerOptions()
                .position(srcLatLng)
                .title("Starting Point")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.srcmarker)));
        distMarker = gMap.addMarker(new MarkerOptions()
                .position(distLatLng)
                .title("Destination Point")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.distmarker)));


    }

    private void zoomToMyLocation() {
        showMyLocation = new CameraPosition.Builder().target(srcLatLng)
                .zoom(15.5f)
                .bearing(340)
                .tilt(50)
                .build();
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(showMyLocation), 2000, null);
    }


    //UI settings of map
    private void mapUiSetting(boolean flag) {
        uiSettings.setZoomControlsEnabled(flag);
        uiSettings.setCompassEnabled(flag);
        uiSettings.setMyLocationButtonEnabled(flag);
        uiSettings.setScrollGesturesEnabled(flag);
        uiSettings.setZoomGesturesEnabled(flag);
        uiSettings.setTiltGesturesEnabled(flag);
        uiSettings.setRotateGesturesEnabled(flag);
    }


    /********
     * Get A route Between source & destination
     ********/
    private void getRoute(LatLng srcLatLng, LatLng distLatLng) {
        final RouteDrawer routeDrawer = new RouteDrawer.RouteDrawerBuilder(gMap)
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

    @Override
    public void onCusTouchUp() {

    }

    @Override
    public void onCusTouchDown() {

    }

    @Override
    public Observable<String> getJsonDirections(LatLng latLng, LatLng latLng1, TravelMode travelMode) {
        return null;
    }

    @Override
    public void RatingDialog(float rating) {
        //doBkash();

        serverCommunicator.endRide(rating,userId);

        mNotificationManager.cancel(NOTIFICATION_ID);
        finish();
    }

    @Override
    public void onClick(View view) {
        userRating.show(getFragmentManager(), "Rate the Driver");
    }
}

