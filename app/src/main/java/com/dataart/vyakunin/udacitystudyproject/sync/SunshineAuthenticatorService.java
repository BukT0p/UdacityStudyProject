package com.dataart.vyakunin.udacitystudyproject.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/*
* Implement AbstractAccountAuthenticator and stub out all
* of its methods
*/
public class SunshineAuthenticatorService extends Service {
    private SunshineAuthenticator authenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        authenticator = new SunshineAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}