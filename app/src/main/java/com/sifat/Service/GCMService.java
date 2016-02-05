package com.sifat.Service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by sifat on 2/5/2016.
 */
public class GCMService extends IntentService {

    public GCMService() {
        super("GCMService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
