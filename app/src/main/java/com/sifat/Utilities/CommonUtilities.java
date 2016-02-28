package com.sifat.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sifat.uberdriver.R;

/**
 * Created by sifat on 1/30/2016.
 */
public class CommonUtilities {


    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final int NOTIFICATION_ID = 101;
    public static final String LOG_TAG_TOUCH="Log Tpuch";
    public static final String SRC_LATLNG="scr_latlng";
    public static final String DIST_LATLNG="dist_latlng";
    public static final String SELECTED_USER_NAME = "selectedUserName";
    public static final String SELECTED_USER_MOBILE = "selectedUserMobile";
    public static final String SELECTED_USER_RATING = "selectedUserRating";
    public static final String SELECTED_USER_ID = "selectedUserId";
    public static final String NOTIFICATION_MANAGER = "notificationManager";
    public static final String IS_ON_HIRE = "isOnHire";
    public static final String IS_ONLINE = "driverStatus";
    public static final String HIRE_STATUS_MESSAGE = "hireStatus";
    public static final String USER_RATING = "userRating";
    public static final String USER_BALANCE = "userBalance";
    public static final String USER_PROFESSION = "userProfession";
    public static final String USER_PRO_PIC_URL = "proPicURL";
    public static final String USER_NID = "userNID";
    public static final String USER_REGISTRATION_ID = "userRegID";
    public static final String USER_FB_INFO = "userFbInfo";
    public static final String USER_NAME = "username";
    public static final String USER_EMAIL = "email";
    public static final String USER_PROFESSON = "profession";
    public static final String USER_FACEBOOK_ID = "userId";
    public static final String USER_ADDRESS = "address";
    public static final String USER_BDAY = "bday";
    public static final String USER_MOBILE_NUM = "mobileNum";
    public static final String USER_FNAME = "fname";
    public static final String USER_LNAME = "lname";
    public static final String USER_GENDER = "gender";
    public static final String USER_PASSWORD = "password";
    public static final String USER_N_ID = "nationalID";
    public static final String USER_PRO_PIC = "profile_Picture";
    public static final String USER_NID_PIC = "nidPic";
    public static final String USER_TYPE_STRING = "userType";
    public static final String USER_TYPE = "Driver";
    /////LOG TAGs
    public static final String LOG_TAG_FACEBOOK = "facebook";
    public static final String LOG_TAG_SERVICE = "Service";
    public static final String LOG_TAG_TAXIPOSITIONSERVICE = "Taxi Position";
    public static final String LOG_TAG_HIRETAXI = "TaxiHire";
    public static final String LOG_TAG_SIGNUP = "singup";
    public static final String LOG_TAG_GCM = "gcmloginfo";
    ///Website URl
    public static final String SIGN_UP_WEBSITE = "http://khep.finder-lbs.com:8001/auth/register/";
    public static final String LOGIN_WEBSITE = "http://inspireitl.com/gober/login.php";
    public static final String LOGOUT_WEBSITE = "http://inspireitl.com/gober/logout.php";
    public static final String TAXI_POSITION_ADDRESS = "http://aimsil.com/uber/taxiposition.txt";
    public static final String PROFILE_INFO_URL = "";
    public static final String LOGIN_WITH_FB = "isLoginWithFacebook";
    ///GCM Registration number
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String SENDER_PROJECT_ID = "307986532903";
    public static final String GCM_REGISTER_ID = "gcmRegID";
    public static String signup_fname, signup_lname, signup_bday, signup_address, signup_email, profileImgUrl,
            signup_mobile, signup_password, signup_gender, signup_confirmPass, signup_profession, signup_NID,signup_username;
    public static Bitmap proPic, NDIPic;

    public static SharedPreferences getSharedPref(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.sharedPref), Context.MODE_PRIVATE);
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
