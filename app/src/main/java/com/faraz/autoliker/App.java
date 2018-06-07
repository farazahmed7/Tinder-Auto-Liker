package com.faraz.autoliker;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.solovyev.android.checkout.Billing;

/**
 * Created by abc on 5/27/2018.
 */

public class App extends Application {


    private static App sInstance;
    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;


    private final Billing mBilling = new Billing(this, new Billing.DefaultConfiguration() {
        @Override
        public String getPublicKey() {
            return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhN7zufUvqYBKKcFnUV1mL8ec1cjAdkjo8+Io4sQuFgJ6jsm9OZjTvbfj+EvLyReYe2qLmbdDi3AGtQozf9Z8hJFcJUjNZDapyR/Fw+jJW+pxE5Q2GxtGEsv1scgkK3utS/b5uEs4OinA5b9ENIVNKP5d6gJ1jQFF5n63q1V7kiCKD5UCDYykV8r25ynS12+gZhMH8u+eLtohTiQ4b1jvs1FFWudp0y/1ybMc3fDhd1V0W44zeflcl59d/H7wAwRSasEU8/RgxJ+mpSH6oPfc8FwkRh6lcnPflB6nVteBXeUdsjcB+Um0KhZ2+BNq4zDB4PSEg8o01Vy3MrujtFxDTQIDAQAB";
        }
    });

    public App() {
        sInstance = this;
    }

    public static App get() {
        return sInstance;
    }

    public Billing getBilling() {
        return mBilling;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        sAnalytics = GoogleAnalytics.getInstance(this);

    }

    synchronized public Tracker getDefaultTracker() {
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.global_tracker);
        }

        return sTracker;
    }



}
