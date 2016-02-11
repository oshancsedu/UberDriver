package com.sifat.Controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.sifat.Domain.FacebookInfo;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.sifat.uberdriver.ValidationActivity;

import org.json.JSONException;
import org.json.JSONObject;

import static com.sifat.Utilities.CommonUtilities.*;

/**
 * Created by sifat on 10/31/2015.
 */
public class FacebookInfoFetcher {

    FacebookInfo facebookInfo;
    String lastName, firstName, email, address, bday, gender, userID;
    private Intent intent;
    private Bundle bundle;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ServerCommunicator serverCommunicator;
    private String gcmRegNum, password;
    private boolean isFacebook;

    public void getFBInfo(String param, final Context context, AccessToken accessToken, final boolean isSigningup) {
        password = "";
        bundle = new Bundle();
        sharedPreferences = getSharedPref(context);
        editor = sharedPreferences.edit();
        serverCommunicator = new ServerCommunicator(context);
        isFacebook = true;
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                JSONObject json = response.getJSONObject();
                try {
                    if (json != null) {
                        email = json.getString("email");
                        firstName = json.getString("first_name");
                        lastName = json.getString("last_name");
                        userID = json.getString("id");
                        if (isSigningup) {
                            bday = json.getString("birthday");
                            //JSONObject loc=json.getJSONObject("location");
                            address = json.getJSONObject("location").getString("name");
                            gender = json.getString("gender");
                            facebookInfo = new FacebookInfo(lastName, firstName, address, bday, gender);
                            bundle.putSerializable(USER_FB_INFO, facebookInfo);
                            intent = new Intent(context, ValidationActivity.class);
                            intent.putExtras(bundle);
                            context.startActivity(intent);
                        } else {
                            saveLoginInfo();
                            gcmRegNum = sharedPreferences.getString(GCM_REGISTER_ID, "");
                            serverCommunicator.login(email, password, gcmRegNum, isFacebook);
                        }
                    }

                } catch (JSONException e) {
                    Toast.makeText(context, "Problem with login", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", param);
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void saveLoginInfo() {
        editor.putString(USER_FACEBOOK_ID, userID);
        editor.commit();
    }
}