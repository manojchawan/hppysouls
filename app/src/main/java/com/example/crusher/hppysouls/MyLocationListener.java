package com.example.crusher.hppysouls;

import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

/**
 * Created by crusher on 25/1/17.
 */
public class MyLocationListener implements LocationListener {
    private static final String TAG = "asd";

    @Override
    public void onLocationChanged(Location loc) {
        String longitude = String.valueOf(loc.getLongitude());
        String latitude = String.valueOf(loc.getLatitude());

        Log.d(TAG,latitude);
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}
