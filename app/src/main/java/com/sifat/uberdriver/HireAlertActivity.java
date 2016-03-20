package com.sifat.uberdriver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.NotificationCompat;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.sifat.Controller.ServerCommunicator;
import com.sifat.Custom.CustomMapFragmment;
import com.sifat.Service.AcceptRideRequest;
import com.sifat.Service.OnRideService;
import com.sifat.Utilities.LocationProvider;
import com.skyfishjy.library.RippleBackground;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

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
    private LatLng srcLatLng,distLatLng;
    private Marker srcMarker, distMarker;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private Intent intent;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private ServerCommunicator serverCommunicator;

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

        vibrator= (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        mediaPlayer = MediaPlayer.create(this, R.raw.rigntone);
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
        serverCommunicator = new ServerCommunicator(this);

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

        rippleBackground.startRippleAnimation();
        mUiSettings = mMap.getUiSettings();
        mapUiSetting(true);
        locationProvider = new LocationProvider(this, mMap, editor, sharedPreferences, srcLatLng, distLatLng);
        locationProvider.getMyLocaton();
        //getRoute(srcLatLng, distLatLng);
        setSrcDistMarker();
        mediaPlayer.start();
        Notify();
        timer.start();
        long[] pattern = {0, 600, 1000};
        vibrator.vibrate(pattern, 0);
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

    private void setSrcDistMarker() {
        srcMarker = mMap.addMarker(new MarkerOptions()
                .position(srcLatLng)
                .title("Starting Point")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.srcmarker)));
        distMarker = mMap.addMarker(new MarkerOptions()
                .position(distLatLng)
                .title("Destination Point")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.distmarker)));
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        boolean flag = sharedPreferences.getBoolean("flag", true);
        boolean initMarker = sharedPreferences.getBoolean("init", true);
        if (!flag && initMarker)
            locationProvider.finish();

    }

    @Override
    public void onCusTouchUp() {
        //rippleBackground.startRippleAnimation();
    }

    @Override
    public void onCusTouchDown() {
        if(rippleBackground.isRippleAnimationRunning())
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
            /*intent = new Intent(HireAlertActivity.this, AcceptRideRequest.class);
            startService(intent);
            */
            serverCommunicator.acceptRide();
            finishTimer();

             /*intent = new Intent(HireAlertActivity.this, OnRideService.class);

            String message = "OK";
            latlngBundle.putString(SELECTED_USER_NAME, "Sifat Oshan");
            latlngBundle.putString(SELECTED_USER_ID, "U54274");
            latlngBundle.putString(SELECTED_USER_MOBILE, "0167x xxxxxx");
            latlngBundle.putFloat(SELECTED_USER_RATING, 3.5f);
            latlngBundle.putString(HIRE_STATUS_MESSAGE, message);

            intent.putExtras(latlngBundle);
            startService(intent);*/
        }
    }

    private void finishTimer() {
        locationProvider.setCancelDrawPath(true);
        mediaPlayer.stop();
        mediaPlayer.release();
        notificationManager.cancel(NOTIFICATION_ID);
        vibrator.cancel();
        HireAlertActivity.this.finish();
        if(wakeLock.isHeld())
            wakeLock.release();
    }

    @Override
    public void onBackPressed() {
    }

    public void Notify() {
        String message = "Ongoing Call...";
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, HireAlertActivity.class);
        //bundle.putParcelable(NOTIFICATION_MANAGER, (Parcelable) mNotificationManager);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notificationicon)
                .setContentTitle("GoBar")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setOngoing(true)
                .setContentText(message).setSound(sound).setLights(Color.CYAN, 1, 1).setVibrate(new long[]{100, 100, 100, 100, 100});
        builder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
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
