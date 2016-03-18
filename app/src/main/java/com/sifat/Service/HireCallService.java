package com.sifat.Service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.sifat.Receiver.GcmBroadcastReceiver;
import com.sifat.uberdriver.HireAlertActivity;

import static com.sifat.Utilities.CommonUtilities.*;

/**
 * Created by sifat on 2/4/2016.
 */
public class HireCallService extends IntentService {

    private Bundle latlngs,gcmMessage;
    LatLng srcLatLng,distLatLng;
    private double srcLat,srcLng,distLat,distLng;

    public HireCallService() {
        super("HireCallService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("Service", "Start");

        /*try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        gcmMessage = intent.getExtras();
        srcLat = Double.parseDouble(gcmMessage.getString(SRC_LAT));
        srcLng = Double.parseDouble(gcmMessage.getString(SRC_LNG));
        distLat = Double.parseDouble(gcmMessage.getString(DIST_LAT));
        distLng = Double.parseDouble(gcmMessage.getString(DIST_LNG));

        latlngs = new Bundle();
        srcLatLng = new LatLng(srcLat, srcLng);
        distLatLng = new LatLng(distLat, distLng);
        latlngs.putParcelable(SRC_LATLNG, srcLatLng);
        latlngs.putParcelable(DIST_LATLNG, distLatLng);

        Intent dialogIntent = new Intent(this, HireAlertActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        dialogIntent.putExtras(latlngs);
        startActivity(dialogIntent);
        GcmBroadcastReceiver.completeWakefulIntent(intent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
