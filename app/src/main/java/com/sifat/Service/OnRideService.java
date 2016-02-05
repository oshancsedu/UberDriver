package com.sifat.Service;

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

import com.sifat.uberdriver.DriverTaxiStatus;
import com.sifat.uberdriver.R;

import static com.sifat.Utilities.CommonUtilities.*;

/**
 * Created by sifat on 2/5/2016.
 */
public class OnRideService extends IntentService {

    private Intent intent;
    private String userName, userID, status, message;
    private float userRating;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private Bundle bundle;
    private boolean isHired;
    private Context context;

    public OnRideService() {
        super("OnRideService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        init();
        bundle = intent.getExtras();

        status = bundle.getString(HIRE_STATUS_MESSAGE);
        userName = bundle.getString(SELECTED_USER_NAME);
        userID = bundle.getString(SELECTED_USER_ID);
        userRating = bundle.getFloat(SELECTED_USER_RATING);

        if (status.equalsIgnoreCase("OK")) {
            isHired = true;
            message = "You have successfully got your rider";
        } else {
            isHired = false;
            message = "Sorry !! This rider has hired another taxi.Try another one.";
        }
        bundle.remove(HIRE_STATUS_MESSAGE);
        Notify();
        stopSelf();
    }

    private void init() {
        context = getApplicationContext();
        sharedPreferences = getSharedPref(this);
        editor = sharedPreferences.edit();
    }


    public void Notify() {
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, DriverTaxiStatus.class);
        //bundle.putParcelable(NOTIFICATION_MANAGER, (Parcelable) mNotificationManager);
        intent.putExtras(bundle);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notificationicon)
                .setContentTitle("GoBar")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setContentText(message).setSound(sound).setLights(Color.CYAN, 1, 1).setVibrate(new long[]{100, 100, 100, 100, 100});
        if (isHired) {
            editor.putString(SELECTED_USER_NAME, userName);
            editor.putString(SELECTED_USER_ID, userID);
            editor.putFloat(SELECTED_USER_RATING, userRating);
            editor.putBoolean(IS_ON_HIRE, isHired);
            editor.commit();
            builder.setContentIntent(contentIntent);
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}