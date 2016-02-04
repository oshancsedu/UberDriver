package com.sifat.Service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.sifat.uberdriver.HireAlertActivity;

import static com.sifat.Utilities.CommonUtilities.*;

/**
 * Created by sifat on 2/4/2016.
 */
public class HireCallService extends IntentService {

    private Bundle latlngs;
    LatLng srcLatLng,distLatLng;

    public HireCallService() {
        super("HireCallService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("Service", "Start");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        srcLatLng = new LatLng(23.89,91.67);
        srcLatLng = new LatLng(23.89,92.67);
        latlngs.putParcelable(SRC_LATLNG, srcLatLng);
        latlngs.putParcelable(DIST_LATLNG, distLatLng);

        Intent dialogIntent = new Intent(this, HireAlertActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        dialogIntent.putExtras(latlngs);
        startActivity(dialogIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Service", "Stopping");
        showToast(this, "Stopping");
    }
}
