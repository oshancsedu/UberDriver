package com.sifat.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sifat.uberdriver.R;

/**
 * Created by sifat on 1/30/2016.
 */
public class CommonUtilities {

    public static final String LOG_TAG_TOUCH="Log Tpuch";
    public static final String SRC_LATLNG="scr_latlng";
    public static final String DIST_LATLNG="dist_latlng";


    public static SharedPreferences getSharedPref(Context context)
    {
        SharedPreferences sharedPreferences= context.getSharedPreferences(String.valueOf(R.string.sharedPref), Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    /**
     * Method to verify google play services on the device
     * */
    public static boolean checkPlayServices(Context context) {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            return true;
        } else {
            Toast.makeText(context, "Google play service not found", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static void showToast(Context context,String message)
    {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }

}
