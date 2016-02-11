package com.sifat.Controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Base64;
import android.widget.Toast;

import com.sifat.Utilities.LoopjHttpClient;
import com.loopj.android.http.*;
import com.sifat.uberdriver.MapsActivity;
import com.sifat.uberdriver.WelcomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import cz.msebera.android.httpclient.Header;

import static com.sifat.Utilities.CommonUtilities.*;

/**
 * Created by sifat on 11/3/2015.
 */
public class ServerCommunicator {

    Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public ServerCommunicator(Context context) {
        this.context = context;
        setSharedPreferences();
    }

    public void sendImage() {
        ByteArrayOutputStream proPicByteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream nidPicByteArrayOutputStream = new ByteArrayOutputStream();

        proPic.compress(Bitmap.CompressFormat.JPEG, 100, proPicByteArrayOutputStream);
        String encodedProPicImage = Base64.encodeToString(proPicByteArrayOutputStream.toByteArray(), Base64.DEFAULT);

        NDIPic.compress(Bitmap.CompressFormat.JPEG, 100, nidPicByteArrayOutputStream);
        String encodedNidPic = Base64.encodeToString(nidPicByteArrayOutputStream.toByteArray(), Base64.DEFAULT);


        final RequestParams requestParams = new RequestParams();
        requestParams.put("image", encodedProPicImage);
        requestParams.put("nidImage", encodedNidPic);

        final String signupWebsite = "http://aimsil.com/uber/uploadImage.php";
        LoopjHttpClient.post(signupWebsite, requestParams, new AsyncHttpResponseHandler() {


            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                showToast(context, new String(responseBody));
                LoopjHttpClient.debugLoopJ(LOG_TAG_SIGNUP, "sendLocationDataToWebsite - success", signupWebsite, requestParams, responseBody, headers, statusCode, null, context);

                changeActivity(WelcomeActivity.class);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    public void sendSignUpInfo() {
        final RequestParams requestParams = new RequestParams();
        requestParams.put(USER_EMAIL, signup_email);
        requestParams.put("username", signup_mobile);
        requestParams.put("password1", signup_password);
        requestParams.put("password2", signup_password);

        final String signupWebsite = SIGN_UP_WEBSITE;
        Toast.makeText(context, signupWebsite, Toast.LENGTH_SHORT).show();
        LoopjHttpClient.post(signupWebsite, requestParams, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                showToast(context, responseBody.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showToast(context, "Could not Signed up!");
            }
        });
    }


    public void completeUserInfo() {
        String gcmRegNum = sharedPreferences.getString(GCM_REGISTER_ID, "");

        ByteArrayOutputStream proPicByteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream nidPicByteArrayOutputStream = new ByteArrayOutputStream();

        proPic.compress(Bitmap.CompressFormat.JPEG, 100, proPicByteArrayOutputStream);
        String encodedProPicImage = Base64.encodeToString(proPicByteArrayOutputStream.toByteArray(), Base64.DEFAULT);

        NDIPic.compress(Bitmap.CompressFormat.JPEG, 100, nidPicByteArrayOutputStream);
        String encodedNidPic = Base64.encodeToString(nidPicByteArrayOutputStream.toByteArray(), Base64.DEFAULT);

        final RequestParams requestParams = new RequestParams();
        requestParams.put(USER_FNAME, signup_fname);
        requestParams.put(USER_LNAME, signup_lname);
        requestParams.put(USER_ADDRESS, signup_address);
        requestParams.put(USER_BDAY, signup_bday);
        requestParams.put(USER_GENDER, signup_gender);
        requestParams.put(GCM_REGISTER_ID, gcmRegNum);
        requestParams.put(USER_N_ID, signup_NID);
        requestParams.put(USER_PRO_PIC, encodedProPicImage);
        requestParams.put(USER_NID_PIC, encodedNidPic);
        requestParams.put(USER_PROFESSON, signup_profession);
        requestParams.put(USER_TYPE_STRING, USER_TYPE);

        final String signupWebsite = SIGN_UP_WEBSITE;
        Toast.makeText(context, signupWebsite, Toast.LENGTH_SHORT).show();
        LoopjHttpClient.get(signupWebsite, requestParams, new AsyncHttpResponseHandler() {


            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                LoopjHttpClient.debugLoopJ(LOG_TAG_SIGNUP, "sendLocationDataToWebsite - success", signupWebsite, requestParams, responseBody, headers, statusCode, null, context);
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                LoopjHttpClient.debugLoopJ(LOG_TAG_SIGNUP, "sendLocationDataToWebsite - failure", signupWebsite, requestParams, responseBody, headers, statusCode, error, context);
            }

        });

    }

    public void login(String email, String pass, String gcm_regId, boolean isFacebook) {
        final RequestParams requestParams = new RequestParams();
        requestParams.put(USER_EMAIL, email);
        requestParams.put(USER_PASSWORD, pass);
        requestParams.put(GCM_REGISTER_ID, gcm_regId);
        requestParams.put(LOGIN_WITH_FB, isFacebook);

        final String loginWebsite = LOGIN_WEBSITE;
        //Toast.makeText(context,loginWebsite,Toast.LENGTH_SHORT).show();
        LoopjHttpClient.get(loginWebsite, requestParams, new AsyncHttpResponseHandler() {


            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                LoopjHttpClient.debugLoopJ(LOG_TAG_SIGNUP, "sendLocationDataToWebsite - success", loginWebsite, requestParams, responseBody, headers, statusCode, null, context);
                String response = new String(responseBody);
                if (response.equalsIgnoreCase("failed"))
                    showToast(context, "Login has Failed");
                else {
                    //showToast(context, response);
                    saveUserInfo(response);
                    changeActivity(MapsActivity.class);
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                LoopjHttpClient.debugLoopJ(LOG_TAG_SIGNUP, "sendLocationDataToWebsite - failure", loginWebsite, requestParams, responseBody, headers, statusCode, error, context);
            }
        });
    }


    public void logout(String gcmRegID, String userRegID) {
        final RequestParams requestParams = new RequestParams();
        requestParams.put(USER_REGISTRATION_ID, userRegID);
        requestParams.put(GCM_REGISTER_ID, gcmRegID);

        final String logoutWebsite = LOGOUT_WEBSITE;
        //showToast(context, logoutWebsite);

        LoopjHttpClient.get(logoutWebsite, requestParams, new AsyncHttpResponseHandler() {


            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                LoopjHttpClient.debugLoopJ(LOG_TAG_SIGNUP, "sendLocationDataToWebsite - success", logoutWebsite, requestParams, responseBody, headers, statusCode, null, context);
                String response = new String(responseBody);

                if (response.equalsIgnoreCase("-1"))
                    showToast(context, "Couldn't Logout");
                else {
                    showToast(context, "Logout Successfully!");
                    eraseUserInfo();
                    changeActivity(WelcomeActivity.class);
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                LoopjHttpClient.debugLoopJ(LOG_TAG_SIGNUP, "sendLocationDataToWebsite - failure", logoutWebsite, requestParams, responseBody, headers, statusCode, error, context);
            }
        });

    }

    private void eraseUserInfo() {
        editor.remove(USER_EMAIL);
        editor.remove(USER_BDAY);
        editor.remove(USER_GENDER);
        editor.remove(USER_MOBILE_NUM);
        editor.remove(USER_ADDRESS);
        editor.remove(USER_FNAME);
        editor.remove(USER_LNAME);
        editor.remove(USER_REGISTRATION_ID);
        editor.remove(USER_NAME);
        editor.remove(USER_FACEBOOK_ID);
        editor.commit();
    }

    private void saveUserInfo(String response) {
        try {
            JSONObject userInfo = new JSONObject(response);

            editor.putString(USER_RATING, userInfo.getString("userRating"));
            editor.putString(USER_BALANCE, userInfo.getString("userBalance"));
            editor.putString(USER_PRO_PIC_URL, userInfo.getString("proPicURL"));
            editor.putString(USER_PROFESSION, userInfo.getString("userProfession"));
            editor.putString(USER_NID, userInfo.getString("userNID"));
            editor.putString(USER_EMAIL, userInfo.getString("email"));
            editor.putString(USER_BDAY, userInfo.getString("birthday"));
            editor.putString(USER_GENDER, userInfo.getString("gender"));
            editor.putString(USER_MOBILE_NUM, userInfo.getString("mobile"));
            editor.putString(USER_ADDRESS, userInfo.getString("location"));
            editor.putString(USER_FNAME, userInfo.getString("first_name"));
            editor.putString(USER_LNAME, userInfo.getString("last_name"));
            editor.putString(USER_NAME, userInfo.getString("first_name") + " " + userInfo.getString("last_name"));
            editor.putString(USER_REGISTRATION_ID, userInfo.getString("id"));
            editor.commit();

            showToast(context, sharedPreferences.getString(USER_ADDRESS, "") + sharedPreferences.getString(USER_EMAIL, "") +
                    sharedPreferences.getString(USER_FNAME, "") + sharedPreferences.getString(USER_LNAME, "") +
                    sharedPreferences.getString(USER_BDAY, "") + sharedPreferences.getString(USER_GENDER, "") +
                    sharedPreferences.getString(USER_REGISTRATION_ID, "") + sharedPreferences.getString(USER_MOBILE_NUM, ""));


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setSharedPreferences() {
        sharedPreferences = getSharedPref(context);
        editor = sharedPreferences.edit();
    }

    private void changeActivity(Class activityclass) {

        Intent intent = new Intent(context, activityclass);
        context.startActivity(intent);
    }
}