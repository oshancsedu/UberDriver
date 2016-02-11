package com.sifat.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.github.polok.routedrawer.RouteDrawer;
import com.github.polok.routedrawer.RouteRest;
import com.github.polok.routedrawer.model.Routes;
import com.github.polok.routedrawer.model.TravelMode;
import com.github.polok.routedrawer.parser.RouteJsonParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.sifat.Utilities.CommonUtilities.checkPlayServices;

public class LocationProvider implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private LocationRequest mLocationRequest;
    private int UPDATE_INTERVAL = 2000; // 2 sec
    private int FATEST_INTERVAL = 1000; // 1 sec
    private int DISPLACEMENT = 10; // 10 meters
    private int count;
    private float minAccuracy;
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private CameraPosition showMyLocation;
    private GoogleMap googleMap;
    private SharedPreferences.Editor editor;
    private SharedPreferences pref;
    private LatLng srcLatlng, distLatlng;
    private boolean isCalled;

    public LocationProvider(Context c, GoogleMap gmap, SharedPreferences.Editor editor, SharedPreferences sharedPreferences){
        context=c;
        googleMap=gmap;
        this.editor=editor;
        pref=sharedPreferences;
    }


    public LocationProvider(Context c, GoogleMap gmap, SharedPreferences.Editor editor, SharedPreferences sharedPreferences, LatLng src, LatLng dist) {
        this(c, gmap, editor, sharedPreferences);
        srcLatlng = src;
        distLatlng = dist;
        isCalled = true;
    }

    public void getMyLocaton() {
        boolean flag=pref.getBoolean("flag",true);
        //Toast.makeText(context,"Get my Location "+flag,Toast.LENGTH_SHORT).show();
        if(flag)
        {
            editor.putBoolean("flag",false);
            editor.commit();
            if (checkPlayServices(context)) {
                init();
                buildGoogleApiClient();
                mGoogleApiClient.connect();
            }
        }
        //return null;
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }

    private void init() {
        minAccuracy = 50.0f;
    }


    @Override
    public void onConnected(Bundle bundle) {
        createLocationRequest();
        startLocationUpdates();
    }

    private void getLatLng() {
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            float accuracy=mLastLocation.getAccuracy();
            //Toast.makeText(context,"Lat : "+latitude+"\nLong : "+longitude+"\nAccuracy : "+
              //     mLastLocation.getAccuracy() + "\nMin : "+minAccuracy,Toast.LENGTH_SHORT).show();
            if(accuracy < minAccuracy)
            {
                stopLocationUpdates();
            }
            LatLng myLatLng = new LatLng(latitude,longitude);
            showMyLocation = new CameraPosition.Builder().target(myLatLng)
                    .zoom(15.5f)
                    .bearing(340)
                    .tilt(50)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(showMyLocation), 2000, null);

            editor.putBoolean("init", true);
            editor.commit();

        }

    }


    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    public void finish()
    {
        if(mGoogleApiClient.isConnected())
        {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Starting the location updates
     **/
    protected void startLocationUpdates() {
        //Toast.makeText(context,"startLocationUpdates",Toast.LENGTH_SHORT).show();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    /**
     * Stopping the location updates
     * */
    private void stopLocationUpdates() {
        //Toast.makeText(context,"stopLocationUpdates",Toast.LENGTH_SHORT).show();
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        editor.putBoolean("flag",true);
        editor.commit();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        if (!isCalled)
            getLatLng();
        else
            showPath();
    }

    private void showPath() {

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            float accuracy = mLastLocation.getAccuracy();
            //Toast.makeText(context,"Lat : "+latitude+"\nLong : "+longitude+"\nAccuracy : "+
            //     mLastLocation.getAccuracy() + "\nMin : "+minAccuracy,Toast.LENGTH_SHORT).show();

            LatLng myLatLng = new LatLng(latitude, longitude);

            if (accuracy < minAccuracy) {
                stopLocationUpdates();
                getRoute(myLatLng, srcLatlng);
            }
            showMyLocation = new CameraPosition.Builder().target(srcLatlng)
                    .zoom(15.5f)
                    .bearing(340)
                    .tilt(50)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(showMyLocation), 2000, null);

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(context,"Failed to detect current location",Toast.LENGTH_SHORT).show();
        return;
    }

    /********
     * Get A route Between source & destination
     ********/
    private void getRoute(LatLng srcLatLng, LatLng distLatLng) {
        final RouteDrawer routeDrawer = new RouteDrawer.RouteDrawerBuilder(googleMap)
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
}
